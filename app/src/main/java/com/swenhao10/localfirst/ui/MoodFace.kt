package com.swenhao10.localfirst.ui

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.swenhao10.localfirst.data.FaceStyle
import com.swenhao10.localfirst.data.Mood
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sign
import kotlin.math.sin
import kotlin.math.tan

/** 一个心情的专属表情：「果冻团子」——超椭圆软身体 + 白色小眼睛 + 每种心情自己的小配件，全部用 Canvas 画。 */
@Composable
fun MoodFace(mood: Mood, size: Dp, modifier: Modifier = Modifier, describe: Boolean = true) {
    MoodFaceOf(mood.face, Color(mood.color), size, modifier, if (describe) mood.name else null)
}

@Composable
fun MoodFaceOf(style: FaceStyle, color: Color, size: Dp, modifier: Modifier = Modifier, description: String? = null) {
    // 待机眨眼：只给 ≥38dp 的大图，列表/日历里的小图保持静态
    val blink: State<Float>? = if (size >= 38.dp && style.blinks()) {
        val period = 3000 + (style.ordinal * 370) % 1000
        val transition = rememberInfiniteTransition(label = "moodBlink")
        transition.animateFloat(
            initialValue = 1f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = keyframes {
                    durationMillis = period
                    1f at period - 150
                    0.1f at period - 75
                    1f at period
                },
                repeatMode = RepeatMode.Restart,
                initialStartOffset = StartOffset((style.ordinal * 530) % 3000)
            ),
            label = "moodBlinkEye"
        )
    } else null
    Canvas(
        modifier
            .size(size)
            .then(if (description != null) Modifier.semantics { contentDescription = description } else Modifier)
    ) { drawMoodFace(style, color, blink?.value ?: 1f) }
}

/** 弧线眼（平静、开心）和横线眼（疲惫）不眨。 */
private fun FaceStyle.blinks(): Boolean = when (this) {
    FaceStyle.CALM, FaceStyle.HAPPY, FaceStyle.TIRED -> false
    else -> true
}

private class JellyShape(val ryf: Float, val p: Float)

private fun FaceStyle.shape(): JellyShape = when (this) {
    FaceStyle.CALM -> JellyShape(1.00f, 2.3f)
    FaceStyle.HAPPY -> JellyShape(0.96f, 2.8f)
    FaceStyle.SAD -> JellyShape(0.90f, 2.3f)
    FaceStyle.ANXIOUS -> JellyShape(1.02f, 2.3f)
    FaceStyle.ANGRY -> JellyShape(0.94f, 3.2f)
    FaceStyle.BORED -> JellyShape(0.82f, 3.0f)
    FaceStyle.TIRED -> JellyShape(0.74f, 2.6f)
    FaceStyle.EXCITED -> JellyShape(1.04f, 2.5f)
}

private val EyeWhite = Color.White
private val Tear = Color(0xFFE1F2FF)
private val Sweat = Color(0xFFD7EEFF)
private val Blush = Color(0xFFFF8C8C).copy(alpha = 0.43f)
private val ShadowInk = Color(0xFF281E3C)

private fun shade(c: Color, mul: Float, add: Float): Color = Color(
    red = (c.red * mul + add).coerceIn(0f, 1f),
    green = (c.green * mul + add).coerceIn(0f, 1f),
    blue = (c.blue * mul + add).coerceIn(0f, 1f),
    alpha = 1f
)

/** 超椭圆 |x/rx|^p + |y/ry|^p = 1 */
private fun superellipse(cx: Float, cy: Float, rx: Float, ry: Float, p: Float, n: Int = 96): Path {
    val path = Path()
    val e = 2f / p
    for (i in 0 until n) {
        val t = 2.0 * PI * i / n
        val c = cos(t).toFloat()
        val s = sin(t).toFloat()
        val x = cx + rx * sign(c) * abs(c).pow(e)
        val y = cy + ry * sign(s) * abs(s).pow(e)
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    return path
}

fun DrawScope.drawMoodFace(style: FaceStyle, base: Color, blink: Float = 1f) {
    val s = size.minDimension
    val ox = (size.width - s) / 2f
    val oy = (size.height - s) / 2f
    val small = s.toDp() < 24.dp
    val shape = style.shape()

    val r = 0.38f * s
    val rx = r
    val ry = r * shape.ryf
    val cx = ox + 0.5f * s
    val groundY = oy + 0.9f * s
    val cy = groundY - ry
    val ecy = cy - 0.02f * ry

    // 地面阴影：扁椭圆，黑紫色渐隐
    if (!small) {
        val shW = 0.9f * r
        val shH = 0.24f * r
        withTransform({ scale(1f, shH / shW, pivot = Offset(cx, groundY)) }) {
            drawCircle(
                brush = Brush.radialGradient(
                    0f to ShadowInk.copy(alpha = 0.2f),
                    0.55f to ShadowInk.copy(alpha = 0.13f),
                    1f to ShadowInk.copy(alpha = 0f),
                    center = Offset(cx, groundY),
                    radius = shW
                ),
                radius = shW,
                center = Offset(cx, groundY)
            )
        }
    }

    // 身体：左上亮、右下暗的线性渐变
    val body = superellipse(cx, cy, rx, ry, shape.p)
    val bright = shade(base, 1.12f, 18f / 255f)
    val dark = shade(base, 0.8f, 0f)
    val gx = -0.35f / rx
    val gy = -0.45f / ry
    val g2 = gx * gx + gy * gy
    val start = Offset(cx - 0.55f * gx / g2, cy - 0.55f * gy / g2)
    val end = Offset(cx + 0.45f * gx / g2, cy + 0.45f * gy / g2)
    drawPath(body, Brush.linearGradient(listOf(dark, bright), start = start, end = end, tileMode = TileMode.Clamp))
    // 边缘略暗
    withTransform({ scale(1f, ry / rx, pivot = Offset(cx, cy)) }) {
        clipPath(superellipse(cx, cy, rx, rx, shape.p)) {
            drawCircle(
                brush = Brush.radialGradient(
                    0f to Color.Black.copy(alpha = 0f),
                    0.84f to Color.Black.copy(alpha = 0f),
                    1f to Color.Black.copy(alpha = 0.12f),
                    center = Offset(cx, cy),
                    radius = rx
                ),
                radius = rx * 1.2f,
                center = Offset(cx, cy)
            )
        }
    }

    // 左上椭圆高光（径向渐隐代替模糊）
    if (!small) {
        val hc = Offset(cx - 0.42f * rx, cy - 0.64f * ry)
        val hw = 0.2f * rx + 0.1f * r
        val hh = 0.14f * ry + 0.1f * r
        withTransform({ scale(1f, hh / hw, pivot = hc) }) {
            drawCircle(
                brush = Brush.radialGradient(
                    0f to Color.White.copy(alpha = 0.35f),
                    0.5f to Color.White.copy(alpha = 0.24f),
                    1f to Color.White.copy(alpha = 0f),
                    center = hc,
                    radius = hw
                ),
                radius = hw,
                center = hc
            )
        }
    }

    // ---------- 五官 ----------
    fun pill(px: Float, py: Float, w: Float, h: Float, angle: Float = 0f, color: Color = EyeWhite) {
        val rr = minOf(w, h) / 2f
        rotate(angle, pivot = Offset(px, py)) {
            drawRoundRect(color, topLeft = Offset(px - w / 2f, py - h / 2f), size = Size(w, h), cornerRadius = CornerRadius(rr, rr))
        }
    }
    fun arcEye(ex: Float, ey: Float, w: Float, h: Float, up: Boolean, width: Float) {
        val path = Path()
        val n = 32
        for (i in 0..n) {
            val t = PI * (0.12 + 0.76 * i / n)
            val x = ex - (w / 2f) * cos(t).toFloat()
            val y = if (up) ey - (h / 2f) * sin(t).toFloat() else ey + (h / 2f) * sin(t).toFloat() - h / 2f
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        drawPath(path, EyeWhite, style = Stroke(width = width, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
    fun blushes() {
        listOf(-1f, 1f).forEach { sd ->
            val bx = cx + sd * 0.52f * r
            val by = ecy + 0.24f * r
            val br = 0.07f * r
            drawOval(Blush, topLeft = Offset(bx - br * 1.4f, by - br * 0.8f), size = Size(br * 2.8f, br * 1.6f))
        }
    }
    fun drop(dx: Float, dy: Float, top: Float, half: Float, color: Color) {
        val path = Path().apply {
            moveTo(dx, dy - top)
            lineTo(dx - half, dy + 0.15f * half)
            lineTo(dx + half, dy + 0.15f * half)
            close()
        }
        drawPath(path, color)
        drawCircle(color, half, Offset(dx, dy + 0.3f * half))
    }
    fun sparkle(px: Float, py: Float, k: Float, color: Color) {
        val path = Path().apply {
            moveTo(px, py - k)
            lineTo(px + k * 0.22f, py - k * 0.22f)
            lineTo(px + k, py)
            lineTo(px + k * 0.22f, py + k * 0.22f)
            lineTo(px, py + k)
            lineTo(px - k * 0.22f, py + k * 0.22f)
            lineTo(px - k, py)
            lineTo(px - k * 0.22f, py - k * 0.22f)
            close()
        }
        drawPath(path, color)
    }
    fun blinking(block: DrawScope.() -> Unit) {
        if (blink >= 0.999f) block()
        else withTransform({ scale(1f, blink, pivot = Offset(cx, ecy)) }) { block() }
    }

    when (style) {
        FaceStyle.CALM -> {
            listOf(-1f, 1f).forEach { sd -> arcEye(cx + sd * 0.3f * r, ecy + 0.03f * r, 0.3f * r, 0.16f * r, up = false, width = 0.085f * r) }
        }
        FaceStyle.HAPPY -> {
            listOf(-1f, 1f).forEach { sd -> arcEye(cx + sd * 0.3f * r, ecy + 0.06f * r, 0.3f * r, 0.26f * r, up = true, width = 0.095f * r) }
            blushes()
        }
        FaceStyle.SAD -> {
            blinking {
                listOf(-1f, 1f).forEach { sd -> pill(cx + sd * 0.28f * r, ecy + 0.06f * r, 0.16f * r, 0.3f * r, angle = -12f * sd) }
            }
            drop(cx - 0.33f * r, ecy + 0.36f * r, 0.1f * r, 0.055f * r, Tear)
        }
        FaceStyle.ANXIOUS -> {
            blinking {
                listOf(-1f, 1f).forEach { sd -> drawCircle(EyeWhite, 0.1f * r, Offset(cx + sd * 0.28f * r, ecy)) }
            }
            drop(cx + 0.55f * r, ecy - 0.45f * r, 0.14f * r, 0.07f * r, Sweat)
        }
        FaceStyle.ANGRY -> {
            blinking {
                listOf(-1f, 1f).forEach { sd ->
                    val ex = cx + sd * 0.28f * r
                    val w = 0.25f * r
                    val y0 = ecy - 0.03f * r
                    val t = tan(Math.toRadians(28.0 * sd)).toFloat()
                    // 斜切掉眼睛上方：在眼睛自身上做差集裁剪，不用身体色块覆盖
                    val lid = Path().apply {
                        moveTo(ex - w, y0 + t * w)
                        lineTo(ex + w, y0 - t * w)
                        lineTo(ex + w, y0 - 0.4f * r)
                        lineTo(ex - w, y0 - 0.4f * r)
                        close()
                    }
                    clipPath(lid, clipOp = ClipOp.Difference) {
                        pill(ex, ecy + 0.06f * r, 0.18f * r, 0.3f * r)
                    }
                }
            }
        }
        FaceStyle.BORED -> {
            blinking {
                listOf(-1f, 1f).forEach { sd ->
                    val ex = cx + sd * 0.28f * r + 0.06f * r
                    val lid = Path().apply {
                        addRect(androidx.compose.ui.geometry.Rect(ex - 0.14f * r, ecy - 0.2f * r, ex + 0.14f * r, ecy + 0.03f * r))
                    }
                    clipPath(lid, clipOp = ClipOp.Difference) {
                        pill(ex, ecy + 0.06f * r, 0.2f * r, 0.3f * r)
                    }
                }
            }
        }
        FaceStyle.TIRED -> {
            listOf(-1f, 1f).forEach { sd -> pill(cx + sd * 0.28f * r, ecy + 0.1f * r, 0.26f * r, 0.075f * r) }
            val zc = lerp(base, Color(0xFF5A5A64), 0.4f)
            val zx = cx + 0.62f * r
            val zy = ecy - 0.62f * r
            listOf(Triple(0f, 0f, 0.16f), Triple(0.2f, -0.2f, 0.12f), Triple(0.36f, -0.36f, 0.09f)).forEach { (dx, dy, sz) ->
                val x0 = zx + dx * r
                val y0 = zy + dy * r
                val k = sz * r
                val z = Path().apply {
                    moveTo(x0, y0)
                    lineTo(x0 + k, y0)
                    lineTo(x0, y0 + k)
                    lineTo(x0 + k, y0 + k)
                }
                drawPath(z, zc, style = Stroke(width = 0.035f * r, cap = StrokeCap.Round, join = StrokeJoin.Round))
            }
        }
        FaceStyle.EXCITED -> {
            blinking {
                listOf(-1f, 1f).forEach { sd ->
                    val ex = cx + sd * 0.28f * r
                    pill(ex, ecy, 0.2f * r, 0.38f * r)
                    drawCircle(base, 0.035f * r, Offset(ex + 0.02f * r, ecy - 0.075f * r))
                }
            }
            blushes()
            sparkle(cx - 1.08f * r, ecy - 0.72f * r, 0.13f * r, base)
            sparkle(cx + 1.05f * r, ecy - 0.5f * r, 0.09f * r, base)
            sparkle(cx + 0.85f * r, ecy - 0.95f * r, 0.06f * r, base)
        }
    }
}
