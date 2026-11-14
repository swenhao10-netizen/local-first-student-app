package com.swenhao10.localfirst.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.swenhao10.localfirst.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * 知行启动动画 v6（新图标 Q 弹出现 + 眨眼，约 2.4 秒，点一下跳过）
 * 1. 图标从小 Q 弹放大，落定时压扁再回弹
 * 2. 小猫眨两下眼（在眼睛上画同色眼皮）
 * 3. 下方浮出「知行」和 slogan
 * 资源：zhixing_icon_tile.png（720×720，圆角透明）。眼睛中心按 1024 原图换算。
 */
private val BG = Color(0xFFFFF9F4)
private val TEAL = Color(0xFF0B6B68)
private val LID = Color(0xFFF9E4F3)
private val LASH = Color(0xFF0E7F7E)
private val EYES = listOf(Offset(370f / 1024f, 565f / 1024f), Offset(668f / 1024f, 565f / 1024f))
private const val EYE_R = 106f / 1024f

@Composable
fun LaunchAnimation(onFinished: () -> Unit) {
    val pop = remember { Animatable(0f) }
    val squish = remember { Animatable(0f) }
    val blink = remember { Animatable(0f) }
    val word = remember { Animatable(0f) }
    var done by remember { mutableStateOf(false) }
    val finish = { if (!done) { done = true; onFinished() } }

    LaunchedEffect(Unit) {
        launch { pop.animateTo(1f, spring(dampingRatio = 0.48f, stiffness = 260f)) }
        delay(260)
        launch {
            squish.animateTo(1f, tween(140, easing = FastOutSlowInEasing))
            squish.animateTo(0f, spring(dampingRatio = 0.35f, stiffness = 500f))
        }
        delay(420)
        launch { word.animateTo(1f, tween(500, easing = LinearOutSlowInEasing)) }
        repeat(2) {
            blink.animateTo(1f, tween(90, easing = FastOutLinearInEasing))
            blink.animateTo(0f, tween(130, easing = LinearOutSlowInEasing))
            delay(140)
        }
        delay(650)
        finish()
    }

    val d = 168.dp
    Box(
        Modifier.fillMaxSize().background(BG)
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { finish() },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.offset(y = (-24).dp)) {
            Box(
                Modifier.size(d).graphicsLayer {
                    val p = pop.value
                    val s = squish.value
                    alpha = (p * 3f).coerceIn(0f, 1f)
                    scaleX = (0.2f + 0.8f * p) * (1f + 0.08f * s)
                    scaleY = (0.2f + 0.8f * p) * (1f - 0.10f * s)
                    transformOrigin = TransformOrigin(0.5f, 0.85f)
                }
            ) {
                Image(painterResource(R.drawable.zhixing_icon_tile), null, Modifier.matchParentSize())
                Canvas(Modifier.matchParentSize()) {
                    val b = blink.value
                    if (b > 0.01f) {
                        val r = size.width * EYE_R
                        EYES.forEach { e ->
                            val c = Offset(e.x * size.width, e.y * size.height)
                            val circle = Path().apply { addOval(androidx.compose.ui.geometry.Rect(c, r)) }
                            val h = 2 * r * b
                            clipPath(circle) {
                                drawRect(LID, Offset(c.x - r, c.y - r), Size(2 * r, h))
                            }
                            if (b > 0.6f) {
                                val y = c.y + r * 0.1f
                                drawArc(
                                    LASH, 20f, 140f, false,
                                    Offset(c.x - r * 0.7f, y - r * 0.35f), Size(r * 1.4f, r * 0.5f),
                                    style = Stroke(width = r * 0.14f, cap = StrokeCap.Round),
                                    alpha = (b - 0.6f) / 0.4f
                                )
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(36.dp))
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.alpha(word.value).offset(y = ((1 - word.value) * 14).dp)
            ) {
                Text("知行", color = TEAL, fontSize = 30.sp, fontWeight = FontWeight.ExtraBold)
                Spacer(Modifier.height(8.dp))
                Text("记下今天的每一件小事", color = Color(0xFF828C8A), fontSize = 13.sp)
            }
        }
    }
}
