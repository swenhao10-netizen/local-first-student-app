package com.swenhao10.localfirst.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import com.swenhao10.localfirst.data.FaceStyle
import com.swenhao10.localfirst.data.Mood
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/** 一个心情的专属表情：圆脸 + 每种心情自己的眼睛 / 嘴 / 眉毛 / 小配件，全部用 Canvas 画（矢量，无版权问题）。 */
@Composable
fun MoodFace(mood: Mood, size: Dp, modifier: Modifier = Modifier, describe: Boolean = true) {
    MoodFaceOf(mood.face, Color(mood.color), size, modifier, if (describe) mood.name else null)
}

@Composable
fun MoodFaceOf(style: FaceStyle, color: Color, size: Dp, modifier: Modifier = Modifier, description: String? = null) {
    Canvas(
        modifier
            .size(size)
            .then(if (description != null) Modifier.semantics { contentDescription = description } else Modifier)
    ) { drawMoodFace(style, color) }
}

private val Ink = Color(0xFF3B2D2A)
private val Tongue = Color(0xFFFF7D8E)
private val Drop = Color(0xFF8FD3FF)

fun DrawScope.drawMoodFace(style: FaceStyle, base: Color) {
    val s = size.minDimension
    val ox = (size.width - s) / 2f
    val oy = (size.height - s) / 2f
    fun p(x: Float, y: Float) = Offset(ox + x * s, oy + y * s)
    fun sz(w: Float, h: Float) = Size(w * s, h * s)
    val sw = s * 0.058f
    val stroke = Stroke(width = sw, cap = StrokeCap.Round, join = StrokeJoin.Round)
    val thin = Stroke(width = sw * 0.7f, cap = StrokeCap.Round, join = StrokeJoin.Round)

    // 脸：上亮下实的柔和渐变 + 左上一点高光
    drawCircle(
        brush = Brush.verticalGradient(listOf(lerp(base, Color.White, 0.35f), base), startY = oy, endY = oy + s),
        radius = s / 2f,
        center = p(0.5f, 0.5f)
    )
    drawOval(Color.White.copy(alpha = 0.32f), topLeft = p(0.22f, 0.1f), size = sz(0.26f, 0.13f))

    fun arc(cx: Float, cy: Float, w: Float, h: Float, start: Float, sweep: Float, st: Stroke = stroke, color: Color = Ink) =
        drawArc(color, start, sweep, useCenter = false, topLeft = p(cx - w / 2, cy - h / 2), size = sz(w, h), style = st)
    fun fillArc(cx: Float, cy: Float, w: Float, h: Float, start: Float, sweep: Float, color: Color = Ink) =
        drawArc(color, start, sweep, useCenter = true, topLeft = p(cx - w / 2, cy - h / 2), size = sz(w, h))
    fun dot(cx: Float, cy: Float, r: Float, color: Color = Ink) = drawCircle(color, r * s, p(cx, cy))
    fun line(x1: Float, y1: Float, x2: Float, y2: Float, w: Float = sw) = drawLine(Ink, p(x1, y1), p(x2, y2), strokeWidth = w, cap = StrokeCap.Round)
    fun cheeks(color: Color, alpha: Float) {
        drawOval(color.copy(alpha = alpha), topLeft = p(0.13f, 0.56f), size = sz(0.17f, 0.09f))
        drawOval(color.copy(alpha = alpha), topLeft = p(0.70f, 0.56f), size = sz(0.17f, 0.09f))
    }
    fun drop(cx: Float, cy: Float, r: Float) {
        val path = Path().apply {
            moveTo(ox + cx * s, oy + (cy - r * 2.1f) * s)
            quadraticTo(ox + (cx + r * 1.05f) * s, oy + (cy - r * 0.6f) * s, ox + (cx + r) * s, oy + cy * s)
            lineTo(ox + (cx - r) * s, oy + cy * s)
            quadraticTo(ox + (cx - r * 1.05f) * s, oy + (cy - r * 0.6f) * s, ox + cx * s, oy + (cy - r * 2.1f) * s)
            close()
        }
        drawPath(path, Drop)
        drawCircle(Drop, r * s, p(cx, cy))
        drawCircle(Color.White.copy(alpha = 0.7f), r * 0.32f * s, p(cx - r * 0.35f, cy - r * 0.2f))
    }
    fun star(cx: Float, cy: Float, r: Float) {
        val path = Path()
        for (i in 0 until 10) {
            val rr = if (i % 2 == 0) r else r * 0.45f
            val a = -PI / 2 + i * PI / 5
            val x = ox + (cx + rr * cos(a).toFloat()) * s
            val y = oy + (cy + rr * sin(a).toFloat()) * s
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        path.close()
        drawPath(path, Ink)
        drawPath(path, Ink, style = Stroke(width = sw * 0.5f, join = StrokeJoin.Round))
    }
    val pink = Color(0xFFFF6F91)

    when (style) {
        FaceStyle.CALM -> {
            // 平静：闭着的放松眼睛 + 一道浅浅的微笑
            arc(0.33f, 0.43f, 0.15f, 0.1f, 20f, 140f)
            arc(0.67f, 0.43f, 0.15f, 0.1f, 20f, 140f)
            arc(0.5f, 0.6f, 0.22f, 0.12f, 25f, 130f)
            cheeks(pink, 0.22f)
        }
        FaceStyle.HAPPY -> {
            // 开心：^ ^ 眯眼 + 张嘴大笑带小舌头 + 红脸蛋
            arc(0.33f, 0.46f, 0.16f, 0.15f, 200f, 140f)
            arc(0.67f, 0.46f, 0.16f, 0.15f, 200f, 140f)
            fillArc(0.5f, 0.6f, 0.34f, 0.3f, 0f, 180f)
            drawOval(Tongue, topLeft = p(0.42f, 0.66f), size = sz(0.16f, 0.08f))
            cheeks(pink, 0.4f)
        }
        FaceStyle.SAD -> {
            // 难过：八字眉 + 下垂嘴 + 一滴眼泪
            line(0.23f, 0.35f, 0.39f, 0.29f)
            line(0.61f, 0.29f, 0.77f, 0.35f)
            dot(0.33f, 0.46f, 0.045f)
            dot(0.67f, 0.46f, 0.045f)
            arc(0.5f, 0.74f, 0.24f, 0.15f, 205f, 130f)
            drop(0.3f, 0.64f, 0.045f)
        }
        FaceStyle.ANXIOUS -> {
            // 焦虑：瞪圆的小眼睛 + 波浪嘴 + 头上一滴汗
            listOf(0.33f, 0.67f).forEach { x ->
                dot(x, 0.45f, 0.085f, Color.White)
                drawCircle(Ink, 0.085f * s, p(x, 0.45f), style = thin)
                dot(x, 0.46f, 0.035f)
            }
            line(0.24f, 0.3f, 0.38f, 0.27f, sw * 0.8f)
            line(0.62f, 0.27f, 0.76f, 0.3f, sw * 0.8f)
            val wave = Path()
            val x0 = 0.34f; val x1 = 0.66f; val y0 = 0.68f; val steps = 24
            for (i in 0..steps) {
                val t = i / steps.toFloat()
                val x = ox + (x0 + (x1 - x0) * t) * s
                val y = oy + (y0 + 0.028f * sin(t * 3 * 2 * PI).toFloat()) * s
                if (i == 0) wave.moveTo(x, y) else wave.lineTo(x, y)
            }
            drawPath(wave, Ink, style = stroke)
            drop(0.83f, 0.26f, 0.05f)
        }
        FaceStyle.ANGRY -> {
            // 生气：倒八字眉 + 撇嘴 + 通红的脸蛋 + 头上「井」字
            line(0.21f, 0.3f, 0.42f, 0.39f, sw * 1.15f)
            line(0.58f, 0.39f, 0.79f, 0.3f, sw * 1.15f)
            dot(0.34f, 0.48f, 0.045f)
            dot(0.66f, 0.48f, 0.045f)
            arc(0.5f, 0.73f, 0.22f, 0.11f, 205f, 130f)
            cheeks(Color(0xFFE53935), 0.5f)
            val red = Color(0xFFE53935)
            val c = p(0.82f, 0.17f)
            val r = 0.07f * s
            listOf(0f, 90f, 180f, 270f).forEach { a ->
                drawArc(red, a + 20f, 50f, useCenter = false, topLeft = Offset(c.x - r, c.y - r), size = Size(r * 2, r * 2), style = Stroke(sw * 0.7f, cap = StrokeCap.Round))
            }
        }
        FaceStyle.BORED -> {
            // 烦闷：半耷拉的眼皮 + 眼珠斜向一边 + 一字嘴
            listOf(0.33f, 0.67f).forEach { x ->
                dot(x, 0.47f, 0.085f, Color.White)
                dot(x + 0.035f, 0.49f, 0.04f)
                fillArc(x, 0.47f, 0.18f, 0.18f, 180f, 180f, lerp(base, Color.Black, 0.12f))
                line(x - 0.09f, 0.47f, x + 0.09f, 0.47f, sw * 0.8f)
            }
            line(0.41f, 0.69f, 0.61f, 0.67f)
        }
        FaceStyle.TIRED -> {
            // 疲惫：眯成一条缝的困眼 + 黑眼圈 + 打哈欠的小 o 嘴 + zzz
            line(0.25f, 0.46f, 0.41f, 0.46f)
            line(0.59f, 0.46f, 0.75f, 0.46f)
            arc(0.33f, 0.52f, 0.14f, 0.07f, 20f, 140f, thin, Ink.copy(alpha = 0.3f))
            arc(0.67f, 0.52f, 0.14f, 0.07f, 20f, 140f, thin, Ink.copy(alpha = 0.3f))
            drawOval(Ink, topLeft = p(0.45f, 0.63f), size = sz(0.1f, 0.11f), style = stroke)
            fun z(x: Float, y: Float, w: Float) {
                val path = Path().apply {
                    moveTo(ox + x * s, oy + y * s)
                    lineTo(ox + (x + w) * s, oy + y * s)
                    lineTo(ox + x * s, oy + (y + w) * s)
                    lineTo(ox + (x + w) * s, oy + (y + w) * s)
                }
                drawPath(path, Ink, style = Stroke(sw * 0.65f, cap = StrokeCap.Round, join = StrokeJoin.Round))
            }
            z(0.7f, 0.16f, 0.1f)
            z(0.84f, 0.04f, 0.07f)
        }
        FaceStyle.EXCITED -> {
            // 兴奋：星星眼 + 大张嘴 + 红脸蛋
            star(0.33f, 0.44f, 0.1f)
            star(0.67f, 0.44f, 0.1f)
            fillArc(0.5f, 0.58f, 0.38f, 0.38f, 0f, 180f)
            drawOval(Tongue, topLeft = p(0.41f, 0.68f), size = sz(0.18f, 0.08f))
            cheeks(pink, 0.4f)
        }
    }
}
