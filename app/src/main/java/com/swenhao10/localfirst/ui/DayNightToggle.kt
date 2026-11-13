package com.swenhao10.localfirst.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private val NightGlow = Color(0xFF7C8CFF)
private val DayGlow = Color(0xFFFFB020)
private val NightTrack = Color(0xFF15171C)
private val DayTrack = Color(0xFFECEBE8)
private val NightThumb = Color(0xFF3A3F4B)
private val DayThumb = Color(0xFFFBFAF7)
private val NightIcon = Color(0xFFEDEFF7)
private val DayIcon = Color(0xFF3B3B3B)

/** 一个粒子：在单位圆里的起点 + 飞散方向和速度。 */
private class Particle(val x: Float, val y: Float, val dx: Float, val dy: Float, val r: Float)

/**
 * 黑夜 / 白天开关：拇指带月亮或太阳，周围发光（夜蓝 / 日橙），
 * 切换时拇指滑过去，图标碎成粒子再聚成另一个图标。
 * dark = true 拇指在左（月亮），false 在右（太阳）。
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
    // 位置：0 = 黑夜（左），1 = 白天（右）
    val pos = remember { Animatable(if (dark) 0f else 1f) }
    // 粒子进度：0 → 1 走完一次「碎开再聚合」
    val burst = remember { Animatable(1f) }
    LaunchedEffect(dark) {
        val target = if (dark) 0f else 1f
        if (pos.value == target) return@LaunchedEffect
        if (reduce) { pos.snapTo(target); burst.snapTo(1f); return@LaunchedEffect }
        burst.snapTo(0f)
        coroutineScope {
            launch { pos.animateTo(target, spring(dampingRatio = 0.72f, stiffness = Spring.StiffnessLow)) }
            launch { burst.animateTo(1f, tween(620, easing = Motion.EaseInOut)) }
        }
    }
    val particles = remember {
        val rnd = Random(7)
        List(46) {
            val a = rnd.nextFloat() * 2f * PI.toFloat()
            val d = kotlin.math.sqrt(rnd.nextFloat())
            val fa = a + (rnd.nextFloat() - .5f) * 1.2f
            val sp = .6f + rnd.nextFloat() * .9f
            Particle(cos(a) * d, sin(a) * d, cos(fa) * sp, sin(fa) * sp, .6f + rnd.nextFloat() * .9f)
        }
    }

    val p = pos.value.coerceIn(-0.1f, 1.1f)
    val pc = p.coerceIn(0f, 1f)
    val glow = lerp(NightGlow, DayGlow, pc)
    val trackShape = RoundedCornerShape(height / 2)
    val pad = 4.dp
    val thumb = height - pad * 2
    val thumbShape = RoundedCornerShape(thumb * 0.36f)
    val travel = width - thumb - pad * 2

    Box(
        modifier
            .size(width, height)
            .clip(trackShape)
            .background(
                Brush.verticalGradient(
                    listOf(
                        lerp(Color(0xFF0B0C10), Color(0xFFDAD8D3), pc),
                        lerp(NightTrack, DayTrack, pc),
                    )
                )
            )
            .border(1.dp, lerp(Color.White.copy(alpha = .06f), Color.White.copy(alpha = .9f), pc), trackShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                role = Role.Switch,
            ) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onToggle(!dark)
            },
        contentAlignment = Alignment.CenterStart,
    ) {
        // 背景里淡淡的小星星（黑夜时）
        Canvas(Modifier.fillMaxSize()) {
            val a = (1f - pc) * .55f
            if (a > 0.01f) {
                val pts = listOf(.62f to .32f, .74f to .62f, .84f to .38f, .9f to .7f, .7f to .22f)
                pts.forEach { (x, y) -> drawCircle(Color.White.copy(alpha = a), 1.1.dp.toPx(), Offset(size.width * x, size.height * y)) }
            }
        }
        Box(
            Modifier
                .padding(start = pad)
                .offset(x = travel * p)
                .size(thumb)
                .shadow(14.dp, thumbShape, ambientColor = glow, spotColor = glow)
                .clip(thumbShape)
                .background(
                    Brush.linearGradient(
                        listOf(
                            lerp(Color(0xFF4A5060), Color.White, pc),
                            lerp(NightThumb, DayThumb, pc),
                        )
                    )
                )
                .border(
                    1.6.dp,
                    Brush.linearGradient(listOf(glow.copy(alpha = .95f), glow.copy(alpha = .25f), glow.copy(alpha = .9f))),
                    thumbShape,
                )
        ) {
            Canvas(Modifier.fillMaxSize().padding(thumb * 0.2f)) {
                val t = burst.value
                val iconColor = lerp(NightIcon, DayIcon, pc)
                // 前半程旧图标淡出，后半程新图标淡入；粒子在中间最散
                val showDay = !dark
                val oldA = (1f - t * 2f).coerceIn(0f, 1f)
                val newA = (t * 2f - 1f).coerceIn(0f, 1f)
                val scatter = sin(PI.toFloat() * t)
                if (t >= 1f) {
                    if (showDay) drawSun(iconColor, 1f, 0f) else drawMoon(iconColor, 1f)
                } else {
                    if (showDay) { drawMoon(iconColor, oldA); drawSun(iconColor, newA, (1f - t) * 90f) }
                    else { drawSun(iconColor, oldA, t * 90f); drawMoon(iconColor, newA) }
                    val c = Offset(size.width / 2, size.height / 2)
                    val rad = size.minDimension * .38f
                    particles.forEach { q ->
                        val o = Offset(
                            c.x + (q.x + q.dx * scatter * .9f) * rad,
                            c.y + (q.y + q.dy * scatter * .9f) * rad,
                        )
                        drawCircle(lerp(iconColor, glow, scatter * .6f).copy(alpha = scatter * .95f), q.r.dp.toPx(), o)
                    }
                }
            }
        }
    }
}

private fun DrawScope.drawMoon(color: Color, alpha: Float) {
    if (alpha <= 0.01f) return
    val c = Offset(size.width / 2, size.height / 2)
    val r = size.minDimension * .38f
    val moon = Path().apply { addOval(androidx.compose.ui.geometry.Rect(c, r)) }
    val bite = Path().apply { addOval(androidx.compose.ui.geometry.Rect(Offset(c.x + r * .55f, c.y - r * .45f), r * .82f)) }
    val shape = Path().apply { op(moon, bite, PathOperation.Difference) }
    drawPath(shape, color.copy(alpha = alpha))
    // 两颗小星
    drawCircle(color.copy(alpha = alpha * .9f), size.minDimension * .045f, Offset(c.x + r * .95f, c.y - r * .95f))
    drawCircle(color.copy(alpha = alpha * .7f), size.minDimension * .03f, Offset(c.x + r * 1.15f, c.y - r * .25f))
}

private fun DrawScope.drawSun(color: Color, alpha: Float, spin: Float) {
    if (alpha <= 0.01f) return
    val c = Offset(size.width / 2, size.height / 2)
    val r = size.minDimension * .2f
    drawCircle(color.copy(alpha = alpha), r, c)
    rotate(spin, c) {
        val inner = size.minDimension * .3f
        val outer = size.minDimension * .44f
        for (i in 0 until 8) {
            val a = i * PI.toFloat() / 4f
            drawLine(
                color.copy(alpha = alpha),
                Offset(c.x + cos(a) * inner, c.y + sin(a) * inner),
                Offset(c.x + cos(a) * outer, c.y + sin(a) * outer),
                strokeWidth = size.minDimension * .075f,
                cap = StrokeCap.Round,
            )
        }
    }
}
