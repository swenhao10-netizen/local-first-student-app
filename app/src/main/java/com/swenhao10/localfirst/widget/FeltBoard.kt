package com.swenhao10.localfirst.widget

import android.content.Context
import android.graphics.*
import android.media.ExifInterface
import com.swenhao10.localfirst.data.AppDatabase
import com.swenhao10.localfirst.data.WordStudy
import com.swenhao10.localfirst.data.images
import com.swenhao10.localfirst.data.isVideoPath
import com.swenhao10.localfirst.data.videoFrame
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

/** 毛毡板小组件上要放的东西。全部来自本机数据库，没有就显示空状态。 */
data class FeltData(
    val photoPath: String?,
    val photoCaption: String,
    val photoDate: String,
    val noteTitle: String,
    val noteLines: List<String>,
    val word: String?,
    val phonetic: String?,
    val meaning: String,
    val wordTag: String,
    val due: Int,
    val streak: Int
)

object FeltBoard {
    private val md = SimpleDateFormat("M.d", Locale.CHINA)

    suspend fun load(context: Context): FeltData {
        val db = AppDatabase.get(context)
        val records = db.records().all()
        val photoRec = records.firstOrNull { r -> r.images().any { File(it).exists() } }
        val photo = photoRec?.images()?.firstOrNull { File(it).exists() }
        val daily = records.firstOrNull { it.kind == "daily" && it.text.isNotBlank() }
        val today = WordStudy.today()
        val todayDaily = records.filter { it.kind == "daily" && it.text.isNotBlank() && SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).format(Date(it.createdAt)) == today }
        val noteSource = if (todayDaily.isNotEmpty()) todayDaily else listOfNotNull(daily)
        val noteLines = noteSource.flatMap { it.text.lines() }.map { it.trim() }.filter { it.isNotEmpty() }.take(4)
        val noteTitle = noteSource.firstOrNull()?.let { md.format(Date(it.createdAt)) + " · 日常" } ?: "日常"

        val now = System.currentTimeMillis()
        val due = db.wordProgress().due(now, 500)
        val unknown = db.unknownWords().all()
        val all = if (due.isEmpty() && unknown.isEmpty()) db.wordProgress().all() else emptyList()
        // 优先待复习（记错多的在前），其次生词本，再其次最近学过的
        val (word, phon, mean, tag) = when {
            due.isNotEmpty() -> Quad(due[0].headword, due[0].phonetic, due[0].meaning, "待复习 · 记错 ${due[0].wrongCount} 次")
            unknown.isNotEmpty() -> Quad(unknown[0].headword, unknown[0].phonetic, unknown[0].meaning, "生词本 · 认识 ${unknown[0].knownCount}/${WordStudy.UNKNOWN_GRADUATE}")
            all.isNotEmpty() -> all.maxByOrNull { it.lastSeen }!!.let { Quad(it.headword, it.phonetic, it.meaning, "最近学过") }
            else -> Quad(null, null, "", "单词卡")
        }
        val streak = WordStudy.streak(db.studyDays().all())
        return FeltData(
            photoPath = photo,
            photoCaption = photoRec?.text?.lines()?.firstOrNull { it.isNotBlank() }?.trim()?.take(8) ?: (if (photo != null) "照片" else ""),
            photoDate = photoRec?.let { md.format(Date(it.createdAt)) } ?: "",
            noteTitle = noteTitle,
            noteLines = noteLines,
            word = word, phonetic = phon, meaning = mean, wordTag = tag,
            due = due.size, streak = streak
        )
    }

    private data class Quad(val a: String?, val b: String?, val c: String, val d: String)

    // ---------------- 画图 ----------------
    private const val FELT = 0xFFA88462.toInt()

    /** wide = 4×2（照片 + 日常 + 单词卡）；否则 2×2（只放单词卡）。flipped = 单词卡翻到释义面。 */
    fun render(context: Context, data: FeltData, wPx: Int, hPx: Int, wide: Boolean, flipped: Boolean): Bitmap {
        val w = max(200, wPx); val h = max(200, hPx)
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)
        val u = min(w, h) / 470f   // 设计稿按 470 高来画
        val radius = 44f * u
        val clip = Path().apply { addRoundRect(RectF(0f, 0f, w.toFloat(), h.toFloat()), radius, radius, Path.Direction.CW) }
        c.save(); c.clipPath(clip)
        drawFelt(c, w, h, u)
        if (wide) drawWide(context, c, data, w, h, u, flipped) else drawSmall(c, data, w, h, u, flipped)
        c.restore()
        // 外圈一道很淡的高光
        c.drawRoundRect(RectF(1f, 1f, w - 1f, h - 1f), radius, radius, Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; strokeWidth = 2f; color = 0x46FFF0DC })
        return bmp
    }

    private fun drawFelt(c: Canvas, w: Int, h: Int, u: Float) {
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        p.shader = RadialGradient(w * 0.35f, -h * 0.1f, max(w, h) * 1.2f, intArrayOf(0xFFBE9874.toInt(), FELT, 0xFF7E5E40.toInt()), floatArrayOf(0f, 0.55f, 1f), Shader.TileMode.CLAMP)
        c.drawRect(0f, 0f, w.toFloat(), h.toFloat(), p)
        p.shader = null
        // 毛毡的绒：固定种子，每次刷新纹理一样
        val r = Random(7)
        p.strokeWidth = max(1f, u)
        repeat((w * h / 900)) {
            val x = r.nextFloat() * w; val y = r.nextFloat() * h
            val a = r.nextFloat() * 6.283f; val l = (3 + r.nextFloat() * 5) * u
            val v = r.nextInt(-30, 30)
            p.color = Color.argb(60, (180 + v).coerceIn(0, 255), (145 + v).coerceIn(0, 255), (110 + v).coerceIn(0, 255))
            c.drawLine(x, y, x + l * kotlin.math.cos(a), y + l * kotlin.math.sin(a), p)
        }
        repeat((w * h / 300)) {
            p.color = if (r.nextBoolean()) 0x14FFFFFF else 0x18000000
            c.drawPoint(r.nextFloat() * w, r.nextFloat() * h, p)
        }
        // 边缘压暗一点
        val e = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; strokeWidth = 18f * u; color = 0x30462C14; maskFilter = BlurMaskFilter(10f * u, BlurMaskFilter.Blur.NORMAL) }
        c.drawRoundRect(RectF(0f, 0f, w.toFloat(), h.toFloat()), 44f * u, 44f * u, e)
        // 米白缝线
        val ins = 18f * u
        val st = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; strokeWidth = 3f * u; strokeCap = Paint.Cap.ROUND; pathEffect = DashPathEffect(floatArrayOf(11f * u, 9f * u), 0f) }
        val rr = RectF(ins, ins, w - ins, h - ins)
        st.color = 0x6E462D19; c.save(); c.translate(u, 2 * u); c.drawRoundRect(rr, 28f * u, 28f * u, st); c.restore()
        st.color = 0xEBF5E8D2.toInt(); c.drawRoundRect(rr, 28f * u, 28f * u, st)
    }

    /** 带投影、旋转地把一块东西贴到板上。block 在以 (0,0) 为中心的坐标里画。 */
    private inline fun pinned(c: Canvas, cx: Float, cy: Float, bw: Float, bh: Float, deg: Float, u: Float, block: (RectF) -> Unit) {
        val r = RectF(-bw / 2, -bh / 2, bw / 2, bh / 2)
        c.save(); c.translate(cx, cy); c.rotate(deg)
        val sh = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0x78190C00; maskFilter = BlurMaskFilter(9f * u, BlurMaskFilter.Blur.NORMAL) }
        c.drawRect(RectF(r.left + 4 * u, r.top + 9 * u, r.right + 4 * u, r.bottom + 9 * u), sh)
        block(r)
        c.restore()
    }

    private fun text(c: Canvas, s: String, x: Float, y: Float, size: Float, color: Int, bold: Boolean = false, align: Paint.Align = Paint.Align.LEFT, maxW: Float = 0f) {
        val p = Paint(Paint.ANTI_ALIAS_FLAG).apply { textSize = size; this.color = color; textAlign = align; typeface = if (bold) Typeface.DEFAULT_BOLD else Typeface.DEFAULT }
        var t = s
        if (maxW > 0 && p.measureText(t) > maxW) {
            while (t.isNotEmpty() && p.measureText("$t…") > maxW) t = t.dropLast(1)
            t = "$t…"
        }
        c.drawText(t, x, y, p)
    }

    private fun fitSize(s: String, start: Float, maxW: Float): Float {
        val p = Paint().apply { textSize = start; typeface = Typeface.DEFAULT_BOLD }
        var size = start
        while (size > 10 && p.measureText(s) > maxW) { size -= 2; p.textSize = size }
        return size
    }

    private fun tape(c: Canvas, cx: Float, cy: Float, tw: Float, th: Float, deg: Float, color: Int) {
        c.save(); c.translate(cx, cy); c.rotate(deg)
        val p = Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = color }
        c.drawRect(-tw / 2, -th / 2, tw / 2, th / 2, p)
        p.color = 0x2DFFFFFF; p.strokeWidth = th / 7
        var x = -tw / 2
        while (x < tw / 2) { c.drawLine(x, -th / 2, x - th / 3, th / 2, p); x += th / 2.6f }
        c.restore()
    }

    private fun pin(c: Canvas, cx: Float, cy: Float, r: Float, color: Int) {
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        p.color = 0x6E140A00; p.maskFilter = BlurMaskFilter(r * 0.4f, BlurMaskFilter.Blur.NORMAL)
        c.drawCircle(cx + r * 0.4f, cy + r * 0.6f, r, p); p.maskFilter = null
        p.shader = RadialGradient(cx - r * 0.3f, cy - r * 0.35f, r * 1.3f, intArrayOf(Color.WHITE, color, darker(color)), floatArrayOf(0f, 0.35f, 1f), Shader.TileMode.CLAMP)
        c.drawCircle(cx, cy, r, p)
    }

    private fun darker(c: Int) = Color.rgb((Color.red(c) * 0.6f).toInt(), (Color.green(c) * 0.6f).toInt(), (Color.blue(c) * 0.6f).toInt())

    private fun wordCard(c: Canvas, r: RectF, d: FeltData, u: Float, flipped: Boolean, showHint: Boolean) {
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        p.color = 0xFFFDFCF8.toInt(); c.drawRect(r, p)
        p.color = 0xFFB4CDE6.toInt(); p.strokeWidth = max(1f, u)
        var y = r.top + r.height() * 0.42f
        while (y < r.bottom) { c.drawLine(r.left, y, r.right, y, p); y += 28f * u }
        p.color = 0xFFE66E6E.toInt(); p.strokeWidth = 2f * u
        val head = r.top + r.height() * 0.26f
        c.drawLine(r.left, head, r.right, head, p)
        val pad = 16f * u
        val tagColor = if (d.wordTag.startsWith("生词")) 0xFFC8465A.toInt() else 0xFF1E6E64.toInt()
        text(c, d.wordTag, r.left + pad, r.top + 38f * u, 21f * u, tagColor, true, maxW = r.width() * 0.62f)
        if (showHint && d.word != null) text(c, if (flipped) "点我翻回" else "点我翻面", r.right - pad, r.top + 38f * u, 18f * u, 0xFF969696.toInt(), align = Paint.Align.RIGHT)
        val cx = r.centerX()
        if (d.word == null) {
            text(c, "还没背过单词", cx, r.top + r.height() * 0.62f, 32f * u, 0xFF3C3C40.toInt(), true, Paint.Align.CENTER)
            text(c, "打开知行学第一个词", cx, r.top + r.height() * 0.84f, 20f * u, 0xFF8C8C8C.toInt(), align = Paint.Align.CENTER)
            return
        }
        if (!flipped) {
            val size = fitSize(d.word, 56f * u, r.width() - pad * 2)
            text(c, d.word, cx, r.top + r.height() * 0.66f, size, 0xFF1E1E23.toInt(), true, Paint.Align.CENTER)
            d.phonetic?.takeIf { it.isNotBlank() }?.let { text(c, "/$it/", cx, r.top + r.height() * 0.88f, 22f * u, 0xFF78787D.toInt(), align = Paint.Align.CENTER, maxW = r.width() - pad * 2) }
        } else {
            text(c, d.word, r.left + pad, r.top + r.height() * 0.48f, fitSize(d.word, 34f * u, r.width() - pad * 2), 0xFF1E1E23.toInt(), true, maxW = r.width() - pad * 2)
            val lines = d.meaning.replace("\\n", "\n").split("\n").map { it.trim() }.filter { it.isNotEmpty() }.ifEmpty { listOf("（词书里没有释义）") }
            var ly = r.top + r.height() * 0.68f
            for (l in lines.take(2)) { text(c, l, r.left + pad, ly, 21f * u, 0xFF3C3C41.toInt(), maxW = r.width() - pad * 2); ly += 30f * u }
        }
    }

    private fun badge(c: Canvas, cx: Float, cy: Float, d: Float, n: Int, u: Float) {
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        p.color = 0x6E190C00; p.maskFilter = BlurMaskFilter(8f * u, BlurMaskFilter.Blur.NORMAL); c.drawCircle(cx + 3 * u, cy + 8 * u, d / 2, p); p.maskFilter = null
        p.color = 0xFFCE3E60.toInt(); c.drawCircle(cx, cy, d / 2, p)
        p.color = 0xFFDE4E6E.toInt(); c.drawCircle(cx, cy, d / 2 - 6 * u, p)
        p.style = Paint.Style.STROKE; p.strokeWidth = 2f * u; p.color = 0x96FFFFFF.toInt(); c.drawCircle(cx, cy, d / 2 - 11 * u, p)
        text(c, "待复习", cx, cy - d * 0.17f, d * 0.13f, Color.WHITE, true, Paint.Align.CENTER)
        val s = if (n > 999) "999+" else n.toString()
        text(c, s, cx, cy + d * 0.2f, d * (if (s.length > 2) 0.24f else 0.32f), Color.WHITE, true, Paint.Align.CENTER)
    }

    /** 拼豆小爱心。 */
    private fun heart(c: Canvas, cx: Float, cy: Float, cell: Float) {
        val rows = listOf(".rr.rr.", "rrrrrrr", "rrrrrrr", ".rrrrr.", "..rrr..", "...r...")
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        val ox = cx - 3.5f * cell; val oy = cy - 3f * cell
        p.color = 0xFFFAFAFA.toInt()
        for ((y, row) in rows.withIndex()) for ((x, ch) in row.withIndex()) if (ch == 'r')
            for (dx in -1..1) for (dy in -1..1) c.drawCircle(ox + (x + dx + 0.5f) * cell, oy + (y + dy + 0.5f) * cell, cell / 2, p)
        for ((y, row) in rows.withIndex()) for ((x, ch) in row.withIndex()) if (ch == 'r') {
            p.color = 0xFFE6465A.toInt(); c.drawCircle(ox + (x + 0.5f) * cell, oy + (y + 0.5f) * cell, cell / 2 - 0.5f, p)
            p.color = 0x78FFFFFF; c.drawCircle(ox + (x + 0.35f) * cell, oy + (y + 0.35f) * cell, cell / 6, p)
        }
    }

    private fun drawSmall(c: Canvas, d: FeltData, w: Int, h: Int, u: Float, flipped: Boolean) {
        val cw = w * 0.72f; val ch = h * 0.5f
        pinned(c, w * 0.46f, h * 0.41f, cw, ch, -4f, u) { wordCard(c, it, d, u, flipped, false) }
        tape(c, w * 0.46f, h * 0.41f - ch / 2, 120f * u, 32f * u, 5f, 0xAA78BEB4.toInt())
        if (d.due > 0) badge(c, w * 0.77f, h * 0.78f, 150f * u, d.due, u)
        else streakTag(c, w * 0.66f, h * 0.82f, d.streak, u)
        heart(c, w * 0.2f, h * 0.82f, 8f * u)
    }

    private fun streakTag(c: Canvas, cx: Float, cy: Float, n: Int, u: Float) {
        val s = if (n > 0) "连续 $n 天" else "今天还没背"
        val p = Paint(Paint.ANTI_ALIAS_FLAG).apply { textSize = 23f * u; typeface = Typeface.DEFAULT_BOLD }
        val tw = p.measureText(s) + 36f * u
        pinned(c, cx, cy, tw, 52f * u, -2f, u) { r ->
            c.drawRect(r, Paint().apply { color = 0xFF1E6E64.toInt() })
            text(c, s, 0f, 8f * u, 23f * u, Color.WHITE, true, Paint.Align.CENTER)
        }
    }

    private fun drawWide(context: Context, c: Canvas, d: FeltData, w: Int, h: Int, u: Float, flipped: Boolean) {
        val col = w / 1000f   // 横向按 1000 宽排版
        // 照片（拍立得）
        val pw = 230f * u; val ph = 270f * u
        pinned(c, 150f * col, h * 0.5f, pw, ph, -4f, u) { r ->
            c.drawRect(r, Paint().apply { color = 0xFFFAF7F0.toInt() })
            val m = 11f * u
            val img = RectF(r.left + m, r.top + m, r.right - m, r.top + m + (pw - 2 * m) * 0.85f)
            val bmp = d.photoPath?.let { loadPhoto(it, (img.width() * 1.5f).toInt()) }
            if (bmp != null) {
                val src = centerCrop(bmp.width, bmp.height, img.width() / img.height())
                c.drawBitmap(bmp, src, img, Paint(Paint.FILTER_BITMAP_FLAG))
            } else {
                c.drawRect(img, Paint().apply { color = 0xFFE4DED2.toInt() })
                text(c, "拍一张", img.centerX(), img.centerY() + 8f * u, 24f * u, 0xFF9A9080.toInt(), align = Paint.Align.CENTER)
            }
            text(c, d.photoCaption, r.left + m + 4 * u, img.bottom + 32f * u, 21f * u, 0xFF37322D.toInt(), true, maxW = pw - 2 * m)
            text(c, d.photoDate, r.left + m + 4 * u, img.bottom + 56f * u, 15f * u, 0xFF8C8278.toInt())
        }
        tape(c, 150f * col, h * 0.5f - ph / 2, 110f * u, 32f * u, 6f, 0xAAF0AAB4.toInt())
        // 便签（日常）
        val nw = 250f * u
        pinned(c, 405f * col, h * 0.5f, nw, nw, 3f, u) { r ->
            c.drawRect(r, Paint().apply { color = 0xFFFFE278.toInt() })
            val pad = 18f * u
            text(c, d.noteTitle, r.left + pad, r.top + 38f * u, 21f * u, 0xFF8C5F14.toInt(), true)
            val lines = d.noteLines.ifEmpty { listOf("今天还没记录", "点一下去写一条") }
            val lp = Paint().apply { color = 0xFFE6BE5A.toInt(); strokeWidth = 2f * u }
            for ((i, l) in lines.take(3).withIndex()) {
                val y = r.top + 82f * u + i * 50f * u
                text(c, l, r.left + pad, y, 24f * u, if (d.noteLines.isEmpty()) 0xFF9A7A3C.toInt() else 0xFF46320F.toInt(), maxW = nw - pad * 2)
                c.drawLine(r.left + pad, y + 14f * u, r.right - pad, y + 14f * u, lp)
            }
        }
        pin(c, 405f * col + nw * 0.4f, h * 0.5f - nw / 2 + 8f * u, 13f * u, 0xFFE13C3C.toInt())
        // 单词卡
        val cw = min(330f * u, 330f * col); val chh = 230f * u
        pinned(c, 705f * col, h * 0.45f, cw, chh, -3f, u) { wordCard(c, it, d, u, flipped, true) }
        tape(c, 705f * col, h * 0.45f - chh / 2, 120f * u, 32f * u, -5f, 0xAA78BEB4.toInt())
        // 待复习 + 连续天数
        if (d.due > 0) badge(c, w - 105f * u, h - 105f * u, 160f * u, d.due, u)
        streakTag(c, 680f * col, h - 80f * u, d.streak, u)
        heart(c, 560f * col, h - 70f * u, 8f * u)
    }

    private fun centerCrop(bw: Int, bh: Int, ratio: Float): Rect {
        return if (bw.toFloat() / bh > ratio) { val nw = (bh * ratio).toInt(); Rect((bw - nw) / 2, 0, (bw - nw) / 2 + nw, bh) }
        else { val nh = (bw / ratio).toInt(); Rect(0, (bh - nh) / 3, bw, (bh - nh) / 3 + nh) }
    }

    private fun loadPhoto(path: String, target: Int): Bitmap? = runCatching {
        if (isVideoPath(path)) return@runCatching videoFrame(path, target)
        val o = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(path, o)
        var s = 1
        while (min(o.outWidth, o.outHeight) / (s * 2) >= target) s *= 2
        val b = BitmapFactory.decodeFile(path, BitmapFactory.Options().apply { inSampleSize = s }) ?: return@runCatching null
        val rot = when (runCatching { ExifInterface(path).getAttributeInt(ExifInterface.TAG_ORIENTATION, 1) }.getOrDefault(1)) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90f; ExifInterface.ORIENTATION_ROTATE_180 -> 180f; ExifInterface.ORIENTATION_ROTATE_270 -> 270f; else -> 0f
        }
        if (rot == 0f) b else Bitmap.createBitmap(b, 0, 0, b.width, b.height, Matrix().apply { postRotate(rot) }, true)
    }.getOrNull()
}
