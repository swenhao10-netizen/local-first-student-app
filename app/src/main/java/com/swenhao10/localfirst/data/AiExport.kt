package com.swenhao10.localfirst.data

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * 「导出给 AI」：把记录整理成一份 Markdown 文字档，开头带一段给 AI 的说明，
 * 用户分享给任意 AI App 或复制粘贴，AI 读完就能大致了解他。
 * 默认只导出文字；选「带上照片和视频」时，图片压缩后一起分享，视频截 3 帧拼成一张图，
 * 文字里每条记录写明对应的文件名，AI 能把图和记录对上。
 */
object AiExport {
    private val dayFmt = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA)
    private val timeFmt = SimpleDateFormat("HH:mm", Locale.CHINA)
    private val fullFmt = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA)
    private val weekNames = arrayOf("周日", "周一", "周二", "周三", "周四", "周五", "周六")

    private fun money(cents: Long) = "¥%.2f".format(Locale.CHINA, cents / 100.0)

    /** days 为 null 表示全部记录。 */
    fun build(records: List<RecordEntry>, nickname: String, days: Int?, mediaNames: Map<String, String>? = null): String {
        val now = System.currentTimeMillis()
        val from = days?.let { now - it * 24L * 3600_000L } ?: Long.MIN_VALUE
        val list = records.filter { it.createdAt >= from }.sortedBy { it.createdAt }
        val sb = StringBuilder()
        val range = if (days == null) "全部记录" else "最近 $days 天"
        sb.appendLine("# ${nickname.ifBlank { "我" }}的生活记录（知行导出）")
        sb.appendLine()
        sb.appendLine("导出时间：${fullFmt.format(Date(now))} · 范围：$range · 共 ${list.size} 条")
        sb.appendLine()
        sb.appendLine("> 给 AI 的说明：下面是我用「知行」App 随手记下的生活记录，包括日常小事、记账和背单词。")
        sb.appendLine("> 请先通读，然后用几句话总结你对我的了解：作息和日常、在意的事、花钱习惯、学习情况，以及你觉得我可以改进的地方。")
        sb.appendLine("> 之后和我聊天、给我建议时，请参考这些记录。不确定的地方直接问我，不要编。")
        sb.appendLine()
        if (list.isEmpty()) {
            sb.appendLine("（这段时间还没有记录）")
            return sb.toString()
        }

        // 概况
        val daily = list.count { it.kind == "daily" }
        val expenseRecs = list.filter { it.kind == "expense" }
        val study = list.filter { it.kind == "study" }
        val out = expenseRecs.filter { it.moneyKind != "income" }
        val inc = expenseRecs.filter { it.moneyKind == "income" }
        val outSum = out.sumOf { it.amountCents ?: 0L }
        val inSum = inc.sumOf { it.amountCents ?: 0L }
        val activeDays = list.map { dayFmt.format(Date(it.createdAt)) }.distinct().size
        val studyDays = study.map { dayFmt.format(Date(it.createdAt)) }.distinct().size
        val wordsLearned = study.sumOf { Regex("(\\d+)").find(it.text)?.value?.toIntOrNull() ?: 0 }
        val photos = list.sumOf { r -> r.images().count { !isVideoPath(it) } }
        val videos = list.sumOf { r -> r.images().count { isVideoPath(it) } }

        sb.appendLine("## 概况")
        sb.appendLine("- 有记录的天数：$activeDays 天（${dayFmt.format(Date(list.first().createdAt))} 到 ${dayFmt.format(Date(list.last().createdAt))}）")
        sb.appendLine("- 日常 $daily 条，记账 ${expenseRecs.size} 条，背单词 ${study.size} 次")
        if (expenseRecs.isNotEmpty()) {
            sb.appendLine("- 支出 ${money(outSum)}，收入 ${money(inSum)}")
            val byCat = out.groupBy { it.category?.ifBlank { null } ?: "未分类" }
                .mapValues { e -> e.value.sumOf { it.amountCents ?: 0L } }
                .entries.sortedByDescending { it.value }.take(5)
            if (byCat.isNotEmpty()) sb.appendLine("- 支出最多的分类：" + byCat.joinToString("，") { "${it.key} ${money(it.value)}" })
        }
        if (study.isNotEmpty()) sb.appendLine("- 背单词 $studyDays 天，共约 $wordsLearned 个词")
        if (photos + videos > 0) {
            if (mediaNames == null) sb.appendLine("- 附带照片 $photos 张、视频 $videos 段（文件没有导出，只记数量）")
            else sb.appendLine("- 附带照片 $photos 张、视频 $videos 段，已作为图片一起发给你：照片是原图压缩，视频是从开头、中间、结尾各截一帧拼成的一张图。每条记录后面的［］里写了对应的文件名，请结合图片内容一起理解。")
        }
        val hours = list.filter { it.kind != "study" }.map { Calendar.getInstance().apply { timeInMillis = it.createdAt }.get(Calendar.HOUR_OF_DAY) }
        if (hours.isNotEmpty()) {
            val night = hours.count { it >= 23 || it < 5 }
            sb.appendLine("- 记录时间大多在 ${hours.groupingBy { it }.eachCount().maxByOrNull { it.value }!!.key} 点左右；23 点到 5 点之间记了 $night 条")
        }
        sb.appendLine()

        // 每天
        sb.appendLine("## 每天的记录")
        list.groupBy { dayFmt.format(Date(it.createdAt)) }.forEach { (day, recs) ->
            val cal = Calendar.getInstance().apply { timeInMillis = recs.first().createdAt }
            sb.appendLine()
            sb.appendLine("### $day ${weekNames[cal.get(Calendar.DAY_OF_WEEK) - 1]}")
            recs.forEach { r -> sb.appendLine("- ${timeFmt.format(Date(r.createdAt))} ${line(r, mediaNames)}") }
        }
        return sb.toString()
    }

    private fun line(r: RecordEntry, mediaNames: Map<String, String>?): String {
        val media = r.images()
        val p = media.count { !isVideoPath(it) }
        val v = media.count { isVideoPath(it) }
        val named = mediaNames?.let { m -> media.mapNotNull { m[it] } }.orEmpty()
        val mediaTag = if (named.isNotEmpty()) named.joinToString("、", "［", "］")
        else listOfNotNull(
            if (p > 0) "$p 张照片" else null,
            if (v > 0) "$v 段视频" else null
        ).takeIf { it.isNotEmpty() }?.joinToString("，", "［", "］") ?: ""
        val text = r.text.replace("\n", " / ").trim()
        return when (r.kind) {
            "expense" -> {
                val kind = if (r.moneyKind == "income") "收入" else "支出"
                listOf("记账：$kind ${r.category?.ifBlank { null } ?: "未分类"} ${money(r.amountCents ?: 0L)}", text.ifBlank { null }, mediaTag.ifBlank { null })
                    .filterNotNull().joinToString(" · ")
            }
            "study" -> "背单词：${text.ifBlank { "背了一些词" }}"
            else -> listOf(text.ifBlank { null }, mediaTag.ifBlank { null }).filterNotNull().joinToString(" ").ifBlank { "（空记录）" }
        }
    }

    /** 范围内带媒体的记录，按时间顺序。 */
    fun mediaInRange(records: List<RecordEntry>, days: Int?): List<String> {
        val now = System.currentTimeMillis()
        val from = days?.let { now - it * 24L * 3600_000L } ?: Long.MIN_VALUE
        return records.filter { it.createdAt >= from }.sortedBy { it.createdAt }.flatMap { it.images() }
    }

    /**
     * 把照片压缩到长边 1600、视频截 3 帧拼图，写进 dir，返回「原路径 -> 文件名」。
     * 读不到的文件跳过。
     */
    fun exportMedia(paths: List<String>, dir: java.io.File): Map<String, String> {
        val out = linkedMapOf<String, String>()
        var pi = 0; var vi = 0
        for (path in paths) {
            if (out.containsKey(path)) continue
            runCatching {
                if (isVideoPath(path)) {
                    val name = "视频_%03d.jpg".format(++vi)
                    val strip = videoStrip(path) ?: return@runCatching
                    java.io.FileOutputStream(java.io.File(dir, name)).use { strip.compress(android.graphics.Bitmap.CompressFormat.JPEG, 85, it) }
                    out[path] = name
                } else {
                    val bmp = decodeScaled(path, 1600) ?: return@runCatching
                    val name = "照片_%03d.jpg".format(++pi)
                    java.io.FileOutputStream(java.io.File(dir, name)).use { bmp.compress(android.graphics.Bitmap.CompressFormat.JPEG, 85, it) }
                    out[path] = name
                }
            }
        }
        return out
    }

    private fun decodeScaled(path: String, maxDim: Int): android.graphics.Bitmap? {
        val o = android.graphics.BitmapFactory.Options().apply { inJustDecodeBounds = true }
        android.graphics.BitmapFactory.decodeFile(path, o)
        if (o.outWidth <= 0) return null
        var sample = 1
        while (maxOf(o.outWidth, o.outHeight) / (sample * 2) >= maxDim) sample *= 2
        val bmp = android.graphics.BitmapFactory.decodeFile(path, android.graphics.BitmapFactory.Options().apply { inSampleSize = sample }) ?: return null
        val scale = maxDim.toFloat() / maxOf(bmp.width, bmp.height)
        val rotated = rotateByExif(path, bmp)
        return if (scale < 1f) android.graphics.Bitmap.createScaledBitmap(rotated, (rotated.width * scale).toInt(), (rotated.height * scale).toInt(), true) else rotated
    }

    private fun rotateByExif(path: String, bmp: android.graphics.Bitmap): android.graphics.Bitmap = runCatching {
        val deg = when (android.media.ExifInterface(path).getAttributeInt(android.media.ExifInterface.TAG_ORIENTATION, 1)) {
            android.media.ExifInterface.ORIENTATION_ROTATE_90 -> 90f
            android.media.ExifInterface.ORIENTATION_ROTATE_180 -> 180f
            android.media.ExifInterface.ORIENTATION_ROTATE_270 -> 270f
            else -> 0f
        }
        if (deg == 0f) bmp else android.graphics.Bitmap.createBitmap(bmp, 0, 0, bmp.width, bmp.height, android.graphics.Matrix().apply { postRotate(deg) }, true)
    }.getOrDefault(bmp)

    /** 视频开头、中间、结尾各一帧，横着拼成一张图。 */
    private fun videoStrip(path: String): android.graphics.Bitmap? {
        val r = android.media.MediaMetadataRetriever()
        try {
            r.setDataSource(path)
            val dur = (r.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L) * 1000
            val times = if (dur > 0) listOf(dur / 10, dur / 2, dur * 9 / 10) else listOf(0L)
            val frames = times.mapNotNull { t -> r.getFrameAtTime(t, android.media.MediaMetadataRetriever.OPTION_CLOSEST_SYNC) }
            if (frames.isEmpty()) return null
            val h = 720
            val scaled = frames.map { f -> android.graphics.Bitmap.createScaledBitmap(f, (f.width * h.toFloat() / f.height).toInt().coerceAtLeast(1), h, true) }
            val gap = 8
            val w = scaled.sumOf { it.width } + gap * (scaled.size - 1)
            val out = android.graphics.Bitmap.createBitmap(w, h, android.graphics.Bitmap.Config.RGB_565)
            val c = android.graphics.Canvas(out)
            c.drawColor(android.graphics.Color.BLACK)
            var x = 0f
            scaled.forEach { c.drawBitmap(it, x, 0f, null); x += it.width + gap }
            return out
        } catch (e: Exception) {
            return null
        } finally { runCatching { r.release() } }
    }
}
