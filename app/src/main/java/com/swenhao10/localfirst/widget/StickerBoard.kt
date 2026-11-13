package com.swenhao10.localfirst.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.media.ExifInterface
import android.os.Bundle
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.widget.RemoteViews
import com.swenhao10.localfirst.MainActivity
import com.swenhao10.localfirst.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

/**
 * 贴纸板：一块夹板，上面贴记录里的照片（白边贴纸 / 拍立得）和自己写的小便签。
 * 坐标都是相对板子的比例（0..1），这样 App 里和桌面小组件大小不同也能对上。
 */
data class Sticker(
    val id: Long,
    val type: String,          // photo | note
    val path: String? = null,  // 照片路径（记录里的文件）
    val text: String = "",     // 便签文字
    val color: Int = 0xFFF9E27D.toInt(), // 便签纸颜色
    val style: String = "cut", // 照片：cut = 白边贴纸，polaroid = 拍立得
    val x: Float = 0.5f,
    val y: Float = 0.5f,
    val scale: Float = 1f,
    val rot: Float = 0f
) {
    /** 决定贴纸长相的字段；位置变了不用重画贴纸图。 */
    val artKey: String get() = "$type|$path|$text|$color|$style"
}

data class BoardState(val board: String = "cork", val stickers: List<Sticker> = emptyList())

object StickerStore {
    private fun file(c: Context) = File(c.filesDir, "sticker_board.json")

    fun load(context: Context): BoardState = runCatching {
        val f = file(context)
        if (!f.exists()) return BoardState()
        val o = JSONObject(f.readText())
        val arr = o.optJSONArray("stickers") ?: JSONArray()
        val list = (0 until arr.length()).map { i ->
            val s = arr.getJSONObject(i)
            Sticker(
                id = s.optLong("id"), type = s.optString("type", "photo"),
                path = s.optString("path").ifBlank { null }, text = s.optString("text"),
                color = s.optInt("color", 0xFFF9E27D.toInt()), style = s.optString("style", "cut"),
                x = s.optDouble("x", .5).toFloat(), y = s.optDouble("y", .5).toFloat(),
                scale = s.optDouble("scale", 1.0).toFloat(), rot = s.optDouble("rot", 0.0).toFloat()
            )
        }
        BoardState(o.optString("board", "cork"), list)
    }.getOrDefault(BoardState())

    fun save(context: Context, state: BoardState) {
        val arr = JSONArray()
        state.stickers.forEach { s ->
            arr.put(JSONObject().apply {
                put("id", s.id); put("type", s.type); put("path", s.path ?: ""); put("text", s.text)
                put("color", s.color); put("style", s.style)
                put("x", s.x.toDouble()); put("y", s.y.toDouble()); put("scale", s.scale.toDouble()); put("rot", s.rot.toDouble())
            })
        }
        val o = JSONObject().put("board", state.board).put("stickers", arr)
        val f = file(context)
        val tmp = File(f.parentFile, f.name + ".tmp")
        tmp.writeText(o.toString())
        tmp.renameTo(f)
    }
}

object StickerArt {
    /** 板子高 = 宽 × 这个比例。 */
    const val ASPECT = 1.2f
    /** 贴纸在 scale = 1 时的宽度占板宽的比例。 */
    const val BASE = 0.34f

    val NOTE_COLORS = listOf(0xFFF9E27D, 0xFFF6B8C8, 0xFFB8D8F6, 0xFFC5E8B7, 0xFFFAF7EE).map { it.toInt() }
    val BOARDS = listOf("cork", "wood", "white")
    fun boardLabel(b: String) = when (b) { "wood" -> "原木"; "white" -> "奶白"; else -> "软木" }

    /** 读一张照片并按 EXIF 转正，最长边压到 target 左右。 */
    fun loadPhoto(path: String, target: Int): Bitmap? = runCatching {
        val o = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(path, o)
        if (o.outWidth <= 0) return null
        var sample = 1
        while (max(o.outWidth, o.outHeight) / (sample * 2) >= target) sample *= 2
        val bmp = BitmapFactory.decodeFile(path, BitmapFactory.Options().apply { inSampleSize = sample }) ?: return null
        val rot = when (runCatching { ExifInterface(path).getAttributeInt(ExifInterface.TAG_ORIENTATION, 1) }.getOrDefault(1)) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90f; ExifInterface.ORIENTATION_ROTATE_180 -> 180f; ExifInterface.ORIENTATION_ROTATE_270 -> 270f; else -> 0f
        }
        if (rot == 0f) bmp else Bitmap.createBitmap(bmp, 0, 0, bmp.width, bmp.height, Matrix().apply { postRotate(rot) }, true)
    }.getOrNull()

    /**
     * 画一张贴纸（带阴影留白）。basePx = scale 为 1 时贴纸的宽度。
     * 照片 cut：圆角 + 粗白边；polaroid：白框底边宽；便签：彩色方纸 + 胶带 + 文字。
     */
    fun renderSticker(s: Sticker, basePx: Int): Bitmap {
        val w = max(40, basePx)
        val shadow = w * 0.06f
        return when (s.type) {
            "note" -> note(s, w, shadow)
            else -> photo(s, w, shadow)
        }
    }

    private fun canvasFor(cw: Float, ch: Float, pad: Float): Pair<Bitmap, Canvas> {
        val bmp = Bitmap.createBitmap((cw + pad * 2).toInt().coerceAtLeast(1), (ch + pad * 2).toInt().coerceAtLeast(1), Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp); c.translate(pad, pad)
        return bmp to c
    }

    private fun shadowPaint(color: Int, r: Float) = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = color
        setShadowLayer(r, 0f, r * 0.45f, 0x55000000)
    }

    private fun photo(s: Sticker, w: Int, pad: Float): Bitmap {
        val src = s.path?.let { if (File(it).exists()) loadPhoto(it, w * 2) else null }
        val polaroid = s.style == "polaroid"
        // 照片比例限制在 0.7..1.4 之间，太长的居中裁
        val ratio = if (src == null) 1f else (src.height.toFloat() / src.width).coerceIn(0.7f, 1.4f)
        val border = if (polaroid) w * 0.06f else w * 0.05f
        val imgW = w - border * 2
        val imgH = if (polaroid) imgW else imgW * ratio
        val ch = imgH + border + (if (polaroid) w * 0.24f else border)
        val (bmp, c) = canvasFor(w.toFloat(), ch, pad)
        val outer = RectF(0f, 0f, w.toFloat(), ch)
        val outerR = if (polaroid) w * 0.012f else w * 0.12f
        c.drawRoundRect(outer, outerR, outerR, shadowPaint(if (polaroid) 0xFFFBFAF6.toInt() else Color.WHITE, pad * 0.7f))
        val inner = RectF(border, border, border + imgW, border + imgH)
        val innerR = if (polaroid) 0f else w * 0.08f
        if (src != null) {
            val crop = centerCrop(src.width, src.height, imgH / imgW)
            val path = Path().apply { addRoundRect(inner, innerR, innerR, Path.Direction.CW) }
            c.save(); c.clipPath(path)
            c.drawBitmap(src, crop, inner, Paint(Paint.FILTER_BITMAP_FLAG or Paint.ANTI_ALIAS_FLAG))
            c.restore()
        } else {
            c.drawRoundRect(inner, innerR, innerR, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFFE6E1D8.toInt() })
            text(c, "照片不见了", inner.centerX(), inner.centerY(), w * 0.08f, 0xFF8A8478.toInt())
        }
        if (polaroid) {
            // 拍立得内侧一圈细阴影，像相纸压在框下
            c.drawRect(inner, Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; strokeWidth = w * 0.004f; color = 0x22000000 })
        }
        return bmp
    }

    private fun note(s: Sticker, w: Int, pad: Float): Bitmap {
        val nw = w * 0.86f
        val tp = TextPaint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFF3B3328.toInt(); typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD) }
        val inset = nw * 0.12f
        val textW = (nw - inset * 2).toInt().coerceAtLeast(10)
        var size = nw * 0.15f
        val body = s.text.ifBlank { " " }
        var layout: StaticLayout
        // 字多就缩小，最多缩到 0.07
        while (true) {
            tp.textSize = size
            layout = StaticLayout.Builder.obtain(body, 0, body.length, tp, textW).setAlignment(Layout.Alignment.ALIGN_CENTER).setLineSpacing(0f, 1.1f).build()
            if (layout.height <= nw - inset * 2.2f || size <= nw * 0.07f) break
            size *= 0.9f
        }
        val nh = max(nw, layout.height + inset * 2.4f)
        val (bmp, c) = canvasFor(nw, nh + pad * 0.3f, pad)
        val r = nw * 0.02f
        c.drawRoundRect(RectF(0f, pad * 0.3f, nw, nh + pad * 0.3f), r, r, shadowPaint(s.color, pad * 0.6f))
        // 右下角微微卷起的阴影
        val curl = Path().apply {
            moveTo(nw, nh + pad * 0.3f - nw * 0.16f); lineTo(nw, nh + pad * 0.3f); lineTo(nw - nw * 0.16f, nh + pad * 0.3f); close()
        }
        c.drawPath(curl, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0x18000000 })
        // 胶带
        c.save(); c.rotate(-4f, nw / 2, pad * 0.3f)
        c.drawRect(RectF(nw * 0.32f, pad * 0.3f - nw * 0.05f, nw * 0.68f, pad * 0.3f + nw * 0.07f), Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0x99FFFFFF.toInt() })
        c.restore()
        c.save(); c.translate(inset, pad * 0.3f + (nh - layout.height) / 2f)
        layout.draw(c); c.restore()
        return bmp
    }

    private fun centerCrop(bw: Int, bh: Int, ratio: Float): Rect {
        return if (bh.toFloat() / bw > ratio) {
            val h = (bw * ratio).toInt(); val top = (bh - h) / 2; Rect(0, top, bw, top + h)
        } else {
            val w = (bh / ratio).toInt(); val left = (bw - w) / 2; Rect(left, 0, left + w, bh)
        }
    }

    private fun text(c: Canvas, s: String, x: Float, y: Float, size: Float, color: Int) {
        val p = Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = color; textSize = size; textAlign = Paint.Align.CENTER }
        c.drawText(s, x, y + size * 0.35f, p)
    }

    /** 画板子 + 夹子。w × h 是整块区域，板子从夹子下面一点开始。 */
    fun drawBoard(c: Canvas, w: Float, h: Float, board: String) {
        val top = h * 0.045f
        val rect = RectF(w * 0.015f, top, w * 0.985f, h * 0.99f)
        val r = w * 0.06f
        val base = when (board) { "wood" -> 0xFFD9B68A.toInt(); "white" -> 0xFFF3EFE6.toInt(); else -> 0xFFB27A4B.toInt() }
        c.drawRoundRect(rect, r, r, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = base; setShadowLayer(w * 0.02f, 0f, w * 0.01f, 0x44000000) })
        c.save()
        c.clipPath(Path().apply { addRoundRect(rect, r, r, Path.Direction.CW) })
        val rnd = Random(7)
        when (board) {
            "wood" -> {
                val p = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; strokeWidth = w * 0.004f }
                var y = rect.top
                while (y < rect.bottom) {
                    p.color = if (rnd.nextBoolean()) 0x22814F23 else 0x14FFFFFF
                    val path = Path().apply {
                        moveTo(rect.left, y)
                        var x = rect.left
                        while (x < rect.right) { x += w * 0.12f; quadTo(x - w * 0.06f, y + (rnd.nextFloat() - .5f) * h * 0.012f, x, y + (rnd.nextFloat() - .5f) * h * 0.004f) }
                    }
                    c.drawPath(path, p); y += h * (0.008f + rnd.nextFloat() * 0.018f)
                }
            }
            else -> {
                // 软木 / 奶白毡：随机小颗粒
                val p = Paint(Paint.ANTI_ALIAS_FLAG)
                val n = ((w * h) / 90f).toInt().coerceAtMost(40000)
                repeat(n) {
                    val dark = rnd.nextFloat() < 0.55f
                    p.color = if (board == "white") (if (dark) 0x14000000 else 0x30FFFFFF) else (if (dark) 0x26472510 else 0x22FFE2B8)
                    c.drawCircle(rect.left + rnd.nextFloat() * rect.width(), rect.top + rnd.nextFloat() * rect.height(), w * (0.0015f + rnd.nextFloat() * 0.003f), p)
                }
            }
        }
        // 底部压印“知行”
        val stamp = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = if (board == "white") 0x22000000 else 0x33000000; textSize = w * 0.05f; textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        }
        c.drawText("知 行", w / 2, rect.bottom - h * 0.03f, stamp)
        c.restore()
        drawClip(c, w, h)
    }

    private fun drawClip(c: Canvas, w: Float, h: Float) {
        val cx = w / 2
        val plateW = w * 0.34f; val plateTop = h * 0.035f; val plateBot = h * 0.115f
        // 拉环
        val ringR = w * 0.045f
        val ringCy = plateTop - ringR * 0.1f
        val metal = LinearGradient(0f, ringCy - ringR, 0f, plateBot, intArrayOf(0xFFF2F2F2.toInt(), 0xFF9C9C9C.toInt(), 0xFFD8D8D8.toInt(), 0xFF7E7E7E.toInt()), floatArrayOf(0f, .35f, .6f, 1f), Shader.TileMode.CLAMP)
        val ring = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; strokeWidth = ringR * 0.45f; shader = metal; setShadowLayer(w * 0.008f, 0f, w * 0.004f, 0x55000000) }
        c.drawCircle(cx, ringCy, ringR, ring)
        // 夹板主体：梯形
        val plate = Path().apply {
            moveTo(cx - plateW * 0.32f, plateTop)
            lineTo(cx + plateW * 0.32f, plateTop)
            lineTo(cx + plateW * 0.5f, plateBot - h * 0.012f)
            quadTo(cx + plateW * 0.5f, plateBot, cx + plateW * 0.46f, plateBot)
            lineTo(cx - plateW * 0.46f, plateBot)
            quadTo(cx - plateW * 0.5f, plateBot, cx - plateW * 0.5f, plateBot - h * 0.012f)
            close()
        }
        c.drawPath(plate, Paint(Paint.ANTI_ALIAS_FLAG).apply { shader = metal; setShadowLayer(w * 0.012f, 0f, w * 0.008f, 0x66000000) })
        // 高光条 + 两颗铆钉
        c.drawRect(RectF(cx - plateW * 0.44f, plateBot - h * 0.022f, cx + plateW * 0.44f, plateBot - h * 0.016f), Paint().apply { color = 0x55FFFFFF })
        val rivet = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFF6E6E6E.toInt() }
        val hole = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFFE8E8E8.toInt() }
        listOf(-0.3f, 0.3f).forEach { k ->
            c.drawCircle(cx + plateW * k, plateTop + (plateBot - plateTop) * 0.45f, w * 0.014f, rivet)
            c.drawCircle(cx + plateW * k, plateTop + (plateBot - plateTop) * 0.45f, w * 0.007f, hole)
        }
    }

    /** 整块贴纸板画成一张图（给桌面小组件用）。 */
    fun renderBoard(state: BoardState, wPx: Int): Bitmap {
        val w = max(160, wPx); val h = (w * ASPECT).toInt()
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)
        drawBoard(c, w.toFloat(), h.toFloat(), state.board)
        val base = (w * BASE).toInt()
        if (state.stickers.isEmpty()) {
            val hint = renderSticker(Sticker(0, "note", text = "在知行「我的 → 贴纸板」\n贴照片和小便签", color = NOTE_COLORS[0]), (base * 1.5f).toInt())
            drawAt(c, hint, w / 2f, h * 0.55f, 1f, -3f)
        }
        state.stickers.forEach { s ->
            val art = runCatching { renderSticker(s, base) }.getOrNull() ?: return@forEach
            drawAt(c, art, s.x * w, s.y * h, s.scale, s.rot)
        }
        return bmp
    }

    private fun drawAt(c: Canvas, art: Bitmap, cx: Float, cy: Float, scale: Float, rot: Float) {
        val m = Matrix().apply {
            postTranslate(-art.width / 2f, -art.height / 2f)
            postScale(scale, scale); postRotate(rot); postTranslate(cx, cy)
        }
        c.drawBitmap(art, m, Paint(Paint.FILTER_BITMAP_FLAG or Paint.ANTI_ALIAS_FLAG))
    }
}

/** 贴纸板桌面小组件：整块板画成一张图；点一下打开知行。 */
abstract class StickerWidgetBase : AppWidgetProvider() {
    override fun onUpdate(context: Context, mgr: AppWidgetManager, ids: IntArray) {
        val pending = goAsync()
        scope.launch { try { ids.forEach { draw(context, mgr, it) } } finally { pending.finish() } }
    }

    override fun onAppWidgetOptionsChanged(context: Context, mgr: AppWidgetManager, id: Int, newOptions: Bundle) {
        onUpdate(context, mgr, intArrayOf(id))
    }

    companion object {
        private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

        /** 贴纸板改了、离开 App 时调用，所有贴纸板小组件重画。 */
        fun refreshAll(context: Context) {
            val app = context.applicationContext
            val mgr = AppWidgetManager.getInstance(app)
            scope.launch {
                runCatching {
                    (mgr.getAppWidgetIds(ComponentName(app, StickerWidgetLarge::class.java)) +
                        mgr.getAppWidgetIds(ComponentName(app, StickerWidgetSmall::class.java))).forEach { draw(app, mgr, it) }
                }
            }
        }

        private fun draw(context: Context, mgr: AppWidgetManager, id: Int) {
            runCatching {
                val opts = mgr.getAppWidgetOptions(id)
                val dm = context.resources.displayMetrics
                val wDp = opts.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH).takeIf { it > 0 } ?: 250
                val hDp = opts.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT).takeIf { it > 0 } ?: 300
                // 按板子比例取能放下的宽度；RemoteViews 有内存上限，最宽 760px
                val fitW = min(wDp * dm.density, hDp * dm.density / StickerArt.ASPECT)
                val wPx = min(760f, fitW).toInt()
                val bmp = StickerArt.renderBoard(StickerStore.load(context), wPx)
                val views = RemoteViews(context.packageName, R.layout.widget_sticker)
                views.setImageViewBitmap(R.id.sticker_board, bmp)
                val open = PendingIntent.getActivity(
                    context, id, Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.sticker_root, open)
                mgr.updateAppWidget(id, views)
            }
        }
    }
}

class StickerWidgetLarge : StickerWidgetBase()
class StickerWidgetSmall : StickerWidgetBase()
