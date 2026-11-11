package com.swenhao10.localfirst.data

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * 「导出给 AI」：把记录整理成一份 Markdown 文字档，开头带一段给 AI 的说明，
 * 用户分享给任意 AI App 或复制粘贴，AI 读完就能大致了解他。
 * 只导出文字信息；照片和视频只写数量，不上传任何文件。
 */
object AiExport {
    private val dayFmt = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA)
    private val timeFmt = SimpleDateFormat("HH:mm", Locale.CHINA)
    private val fullFmt = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA)
    private val weekNames = arrayOf("周日", "周一", "周二", "周三", "周四", "周五", "周六")

    private fun money(cents: Long) = "¥%.2f".format(Locale.CHINA, cents / 100.0)

    /** days 为 null 表示全部记录。 */
    fun build(records: List<RecordEntry>, nickname: String, days: Int?): String {
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
        if (photos + videos > 0) sb.appendLine("- 附带照片 $photos 张、视频 $videos 段（文件没有导出，只记数量）")
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
            recs.forEach { r -> sb.appendLine("- ${timeFmt.format(Date(r.createdAt))} ${line(r)}") }
        }
        return sb.toString()
    }

    private fun line(r: RecordEntry): String {
        val media = r.images()
        val p = media.count { !isVideoPath(it) }
        val v = media.count { isVideoPath(it) }
        val mediaTag = listOfNotNull(
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
}
