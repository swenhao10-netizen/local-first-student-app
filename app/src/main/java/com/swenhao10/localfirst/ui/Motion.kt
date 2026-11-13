package com.swenhao10.localfirst.ui

import android.provider.Settings
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback

/**
 * 动效 token：曲线、时长、弹簧。全 App 的交互动画都从这里取，别在页面里手写数字。
 * 依据：Emil Kowalski（strong ease-out / drawer）、Taste motion tokens、Apple 弹簧参数（dampingRatio 1 = 不回弹）。
 */
object Motion {
    // 曲线
    val EaseOut = CubicBezierEasing(0.23f, 1f, 0.32f, 1f)     // 进入 / 退出 / 反馈
    val EaseInOut = CubicBezierEasing(0.77f, 0f, 0.175f, 1f)  // 屏幕内移动 / 变形
    val EaseSheet = CubicBezierEasing(0.32f, 0.72f, 0f, 1f)   // 底部面板
    val EaseEnter = CubicBezierEasing(0.22f, 1f, 0.36f, 1f)   // 一次性的入场落定
    val EaseLayer = CubicBezierEasing(0.65f, 0f, 0.35f, 1f)   // 分层展开（不回弹）

    // 时长（ms）
    const val PRESS = 100
    const val SMALL = 150
    const val STATE = 200
    const val EXIT = 150
    const val SHEET = 300

    // 弹簧
    fun <T> settle(): SpringSpec<T> = spring(dampingRatio = 1f, stiffness = Spring.StiffnessMediumLow)
    fun <T> snappy(): SpringSpec<T> = spring(dampingRatio = 1f, stiffness = Spring.StiffnessMedium)
    fun <T> flick(): SpringSpec<T> = spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessMediumLow)
    fun <T> press(): TweenSpec<T> = tween(PRESS, easing = EaseOut)
    fun <T> state(ms: Int = STATE, delay: Int = 0): TweenSpec<T> = tween(ms, delayMillis = delay, easing = EaseOut)
}

/**
 * 系统「移除动画」时为 true：只认 ANIMATOR_DURATION_SCALE 恰好等于 0（读不到时默认 1 = 有动画）。
 * 0.5x / 1.5x 之类都不算减弱；默认值 false = 动画照常放。
 */
val LocalReduceMotion = staticCompositionLocalOf { false }

@Composable
fun rememberReduceMotion(): Boolean {
    val ctx = LocalContext.current
    return remember {
        runCatching {
            Settings.Global.getFloat(ctx.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f
        }.getOrDefault(false)
    }
}

/** 减弱动画时直接跳到终点；否则用传进来的规格。 */
@Composable
fun <T> motion(spec: FiniteAnimationSpec<T>): FiniteAnimationSpec<T> =
    if (LocalReduceMotion.current) snap() else spec

/**
 * 按下缩放：按下用快弹簧缩到 [pressed]，松手带一点点回弹弹回去。
 * 整行用 0.97，小控件用默认 0.94。[haptic] 为 true 时按下给一次轻触感。
 * 减弱动画时不缩放。
 */
fun Modifier.pressScale(
    source: MutableInteractionSource,
    pressed: Float = 0.94f,
    haptic: Boolean = false
): Modifier = composed {
    val isPressed by source.collectIsPressedAsState()
    val reduce = LocalReduceMotion.current
    val hapticFeedback = LocalHapticFeedback.current
    if (haptic) {
        LaunchedEffect(isPressed) {
            if (isPressed) hapticFeedback.performHapticFeedback(HapticFeedbackType.ContextClick)
        }
    }
    val s by animateFloatAsState(
        targetValue = if (isPressed && !reduce) pressed else 1f,
        animationSpec = if (isPressed) spring(dampingRatio = 0.9f, stiffness = 1200f) else spring(dampingRatio = 0.6f, stiffness = 500f),
        label = "press"
    )
    this.graphicsLayer { scaleX = s; scaleY = s }
}
