package com.swenhao10.localfirst.ui

import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import kotlinx.coroutines.delay
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer

/** 渐变主题的强调渐变；null = 单色主题，「!」用 onSurfaceVariant。由 MainActivity 提供。 */
val LocalInfoTipBrush = staticCompositionLocalOf<Brush?> { null }

/**
 * 标题后面的小「!」：长按（或点一下）弹出一个说明气泡，点别处或 6 秒后收起。
 * 只用在真正需要解释的地方，代替标题下面那行灰色小字。
 */
@Composable
fun InfoTip(text: String, modifier: Modifier = Modifier) {
    val haptic = LocalHapticFeedback.current
    val shown = remember { MutableTransitionState(false) }
    val gap = with(LocalDensity.current) { 6.dp.roundToPx() }
    val margin = with(LocalDensity.current) { 12.dp.roundToPx() }
    Box(
        modifier
            .size(32.dp)
            .semantics { contentDescription = "说明" }
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        shown.targetState = true
                    },
                    onTap = { shown.targetState = !shown.currentState },
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            Icons.Outlined.Info,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp).then(
                LocalInfoTipBrush.current?.let { b ->
                    Modifier.graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
                        .drawWithContent { drawContent(); drawRect(b, blendMode = BlendMode.SrcIn) }
                } ?: Modifier
            ),
        )
        if (shown.currentState || shown.targetState) {
            LaunchedEffect(shown.targetState) {
                if (shown.targetState) { delay(6000); shown.targetState = false }
            }
            val provider = remember(gap, margin) { AboveOrBelow(gap, margin) }
            Popup(
                popupPositionProvider = provider,
                onDismissRequest = { shown.targetState = false },
                properties = PopupProperties(focusable = true),
            ) {
                AnimatedVisibility(
                    visibleState = shown,
                    enter = fadeIn(tween(Motion.SMALL)) + scaleIn(tween(Motion.SMALL, easing = Motion.EaseOut), initialScale = .92f),
                    exit = fadeOut(tween(Motion.EXIT)) + scaleOut(tween(Motion.EXIT), targetScale = .96f),
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.inverseSurface,
                        contentColor = MaterialTheme.colorScheme.inverseOnSurface,
                        shape = RoundedCornerShape(12.dp),
                        shadowElevation = 6.dp,
                        modifier = Modifier.widthIn(max = 280.dp),
                    ) {
                        Text(text, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp))
                    }
                }
            }
        }
    }
}

/** 气泡放在图标上方居中，放不下就放下方；左右不出屏幕。 */
private class AboveOrBelow(private val gap: Int, private val margin: Int) : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize,
    ): IntOffset {
        val maxX = (windowSize.width - popupContentSize.width - margin).coerceAtLeast(margin)
        val x = (anchorBounds.center.x - popupContentSize.width / 2).coerceIn(margin, maxX)
        val above = anchorBounds.top - gap - popupContentSize.height
        val y = if (above >= margin) above else anchorBounds.bottom + gap
        return IntOffset(x, y)
    }
}
