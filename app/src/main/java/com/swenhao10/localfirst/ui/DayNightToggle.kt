package com.swenhao10.localfirst.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private val NightGlow = Color(0xFF7C8CFF)
private val DayGlow = Color(0xFFFFB020)
private val NightIcon = Color(0xFFEDEFF7)
private val DayIcon = Color(0xFF3B3B3B)

/** 粒子：单位圆里的起点 + 飞散方向和速度，全部放在平铺的 FloatArray 里，画的时候不分配对象。 */
private class Particles(n: Int) {
    val x = FloatArray(n); val y = FloatArray(n); val dx = FloatArray(n); val dy = FloatArray(n); val r = FloatArray(n)
    val size = n
    init {
        val rnd = Random(7)
        for (i in 0 until n) {
            val a = rnd.nextFloat() * 2f * PI.toFloat()
            val d = kotlin.math.sqrt(rnd.nextFloat())
            val fa = a + (rnd.nextFloat() - .5f) * 1.2f
            val sp = .6f + rnd.nextFloat() * .9f
            x[i] = cos(a) * d; y[i] = sin(a) * d; dx[i] = cos(fa) * sp; dy[i] = sin(fa) * sp; r[i] = .6f + rnd.nextFloat() * .9f
        }
    }
}

private val StarX = floatArrayOf(.62f, .74f, .84f, .9f, .7f)
private val StarY = floatArrayOf(.32f, .62f, .38f, .7f, .22f)

/** 动画时长：拇指滑动 + 粒子换图标都在这个时间内走完，走完才真正切换主题。 */
private const val TOGGLE_MS = 500

/**
 * 黑夜 / 白天开关：拇指带月亮或太阳，周围发光（夜蓝 / 日橙），
 * 切换时拇指滑过去，图标碎成粒子再聚成另一个图标。
 * dark = true 拇指在左（月亮），false 在右（太阳）。
 *
 * 开关自己持有视觉状态：点下去立刻播动画，动画走完（约 500ms）才调用 onToggle 换主题，
 * 这样换主题的那一帧重排不会和开关动画抢帧。所有动画值只在绘制阶段读取，不触发重组。
 */
@Composable
fun DayNightToggle(
    dark: Boolean,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    width: Dp = 104.dp,
    height: Dp = 56.dp,
) {
    val reduce = LocalReduceMotion.current
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    val latestToggle by rememberUpdatedState(onToggle)
    val latestDark by rememberUpdatedState(dark)
    // 位置：0 = 黑夜（左），1 = 白天（右）
    val pos = remember { Animatable(if (dark) 0f else 1f) }
    // 粒子进度：0 → 1 走完一次「碎开再聚合」
    val burst = remember { Animatable(1f) }
    // 本地视觉状态（可能领先于外部 dark 半秒）
    var showDark by remember { mutableStateOf(dark) }
    var job by remember { mutableStateOf<Job?>(null) }
    val particles = remember { Particles(46) }

    suspend fun play(toDark: Boolean) {
        val target = if (toDark) 0f else 1f
        if (reduce) { pos.snapTo(target); burst.snapTo(1f); return }
        burst.snapTo(0f)
        coroutineScope {
            launch { pos.animateTo(target, spring(dampingRatio = 0.78f, stiffness = 520f)) }
            launch { burst.animateTo(1f, tween(TOGGLE_MS, easing = Motion.EaseInOut)) }
        }
    }

    // 外部变化（跟随系统、别处改了主题）：没在点按动画中时跟上
    LaunchedEffect(dark) {
        if (job?.isActive == true) return@LaunchedEffect
        if (showDark != dark) {
            showDark = dark
            play(dark)
        }
    }

    Spacer(
        modifier
            .size(width, height)
            .clip(RoundedCornerShape(height / 2))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                role = Role.Switch,
            ) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                val next = !showDark
                showDark = next
                job?.cancel()
                job = scope.launch {
                    play(next)
                    if (latestDark != next) latestToggle(next)
                }
            }
            .drawWithCache {
                val w = size.width; val h = size.height
                val padPx = 4.dp.toPx()
                val thumb = h - padPx * 2
                val travel = w - thumb - padPx * 2
                val trackR = CornerRadius(h / 2, h / 2)
                val thumbR = CornerRadius(thumb * .36f, thumb * .36f)
                val thumbSize = Size(thumb, thumb)
                // 两套画刷都预先建好，切换时按进度叠加透明度，不在每帧新建
                val nightTrack = Brush.verticalGradient(listOf(Color(0xFF0B0C10), Color(0xFF15171C)))
                val dayTrack = Brush.verticalGradient(listOf(Color(0xFFDAD8D3), Color(0xFFECEBE8)))
                val nightThumb = Brush.linearGradient(listOf(Color(0xFF4A5060), Color(0xFF3A3F4B)), Offset.Zero, Offset(thumb, thumb))
                val dayThumb = Brush.linearGradient(listOf(Color.White, Color(0xFFFBFAF7)), Offset.Zero, Offset(thumb, thumb))
                fun rim(g: Color) = Brush.linearGradient(listOf(g.copy(alpha = .95f), g.copy(alpha = .25f), g.copy(alpha = .9f)), Offset.Zero, Offset(thumb, thumb))
                val nightRim = rim(NightGlow); val dayRim = rim(DayGlow)
                val glowR = thumb * .95f
                val thumbCenter = Offset(thumb / 2, thumb / 2)
                fun halo(g: Color) = Brush.radialGradient(listOf(g.copy(alpha = .55f), g.copy(alpha = 0f)), thumbCenter, glowR)
                val nightHalo = halo(NightGlow); val dayHalo = halo(DayGlow)
                val rimStroke = Stroke(1.6.dp.toPx())
                val trackStroke = Stroke(1.dp.toPx())
                val starR = 1.1.dp.toPx()
                val iconInset = thumb * .2f
                val iconBox = thumb - iconInset * 2
                val moon = moonPath(iconBox)
                val dp1 = 1.dp.toPx()

                onDrawBehind {
                    val p = pos.value.coerceIn(-0.1f, 1.1f)
                    val pc = p.coerceIn(0f, 1f)
                    val glow = lerp(NightGlow, DayGlow, pc)
                    // 轨道
                    drawRoundRect(nightTrack, cornerRadius = trackR)
                    if (pc > 0f) drawRoundRect(dayTrack, cornerRadius = trackR, alpha = pc)
                    drawRoundRect(
                        lerp(Color.White.copy(alpha = .06f), Color.White.copy(alpha = .9f), pc),
                        cornerRadius = trackR, style = trackStroke,
                    )
                    // 黑夜时的小星星
                    val sa = (1f - pc) * .55f
                    if (sa > 0.01f) {
                        for (i in StarX.indices) drawCircle(Color.White.copy(alpha = sa), starR, Offset(w * StarX[i], h * StarY[i]))
                    }
                    translate(padPx + travel * p, padPx) {
                        // 光晕
                        if (pc < 1f) drawCircle(nightHalo, glowR, thumbCenter, alpha = 1f - pc)
                        if (pc > 0f) drawCircle(dayHalo, glowR, thumbCenter, alpha = pc)
                        // 拇指
                        drawRoundRect(nightThumb, size = thumbSize, cornerRadius = thumbR)
                        if (pc > 0f) drawRoundRect(dayThumb, size = thumbSize, cornerRadius = thumbR, alpha = pc)
                        if (pc < 1f) drawRoundRect(nightRim, size = thumbSize, cornerRadius = thumbR, style = rimStroke, alpha = 1f - pc)
                        if (pc > 0f) drawRoundRect(dayRim, size = thumbSize, cornerRadius = thumbR, style = rimStroke, alpha = pc)
                        // 图标 + 粒子
                        translate(iconInset, iconInset) {
                            val t = burst.value
                            val iconColor = lerp(NightIcon, DayIcon, pc)
                            val showDay = !showDark
                            val c = Offset(iconBox / 2, iconBox / 2)
                            if (t >= 1f) {
                                if (showDay) drawSun(iconBox, iconColor, 1f, 0f) else drawMoon(moon, iconBox, iconColor, 1f)
                            } else {
                                val oldA = (1f - t * 2f).coerceIn(0f, 1f)
                                val newA = (t * 2f - 1f).coerceIn(0f, 1f)
                                val scatter = sin(PI.toFloat() * t)
                                if (showDay) { drawMoon(moon, iconBox, iconColor, oldA); drawSun(iconBox, iconColor, newA, (1f - t) * 90f) }
                                else { drawSun(iconBox, iconColor, oldA, t * 90f); drawMoon(moon, iconBox, iconColor, newA) }
                                val rad = iconBox * .38f
                                val pColor = lerp(iconColor, glow, scatter * .6f).copy(alpha = scatter * .95f)
                                val k = scatter * .9f
                                for (i in 0 until particles.size) {
                                    drawCircle(
                                        pColor,
                                        particles.r[i] * dp1,
                                        Offset(c.x + (particles.x[i] + particles.dx[i] * k) * rad, c.y + (particles.y[i] + particles.dy[i] * k) * rad),
                                    )
                                }
                            }
                        }
                    }
                }
            }
    )
}

/** 月亮形状（含缺口）只在尺寸变化时算一次。 */
private fun moonPath(box: Float): Path {
    val c = Offset(box / 2, box / 2)
    val r = box * .38f
    val moon = Path().apply { addOval(Rect(c, r)) }
    val bite = Path().apply { addOval(Rect(Offset(c.x + r * .55f, c.y - r * .45f), r * .82f)) }
    return Path().apply { op(moon, bite, PathOperation.Difference) }
}

private fun DrawScope.drawMoon(moon: Path, box: Float, color: Color, alpha: Float) {
    if (alpha <= 0.01f) return
    val c = Offset(box / 2, box / 2)
    val r = box * .38f
    drawPath(moon, color, alpha = alpha)
    // 两颗小星
    drawCircle(color.copy(alpha = alpha * .9f), box * .045f, Offset(c.x + r * .95f, c.y - r * .95f))
    drawCircle(color.copy(alpha = alpha * .7f), box * .03f, Offset(c.x + r * 1.15f, c.y - r * .25f))
}

private fun DrawScope.drawSun(box: Float, color: Color, alpha: Float, spin: Float) {
    if (alpha <= 0.01f) return
    val c = Offset(box / 2, box / 2)
    val col = color.copy(alpha = alpha)
    drawCircle(col, box * .2f, c)
    rotate(spin, c) {
        val inner = box * .3f
        val outer = box * .44f
        val sw = box * .075f
        for (i in 0 until 8) {
            val a = i * PI.toFloat() / 4f
            val ca = cos(a); val sa = sin(a)
            drawLine(col, Offset(c.x + ca * inner, c.y + sa * inner), Offset(c.x + ca * outer, c.y + sa * outer), strokeWidth = sw, cap = StrokeCap.Round)
        }
    }
}
