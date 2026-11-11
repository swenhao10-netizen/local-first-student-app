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
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import com.swenhao10.localfirst.R
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.sin

/**
 * 知行启动动画 v5（小猫落地 + 招手，约 3.2 秒，点一下跳过）
 * 1. 浅青圆底弹出来
 * 2. 小猫从屏幕上方掉下来，落地时压扁再弹回（Q 弹），影子跟着变大
 * 3. 落地瞬间蹦出说/图/账/词四色小点，身体不动，只有举起的那只爪子招两下手
 * 4. 下方出现「知行」和 slogan
 * 小芽是图片自带的，不再单独做动画。
 *
 * 资源：zhixing_cat_body.png（身体，去掉了举起的手）和 zhixing_cat_arm.png（只有手），
 * 两张都是 720×938 透明底、画布完全对齐，放到 res/drawable-nodpi/。手绕肩膀 (21%, 58%) 转。
 * 窗口背景设为 #FFF9F4，避免启动白闪。
 */
private val BG = Color(0xFFFFF9F4)
private val TEAL = Color(0xFF0B6B68)
private val DISC = Color(0xFFDEF0EC)
private val TYPE_COLORS = listOf(Color(0xFF0B6B68), Color(0xFF5A78D2), Color(0xFFDE8C28), Color(0xFF965AC8))

@Composable
fun LaunchAnimation(onFinished: () -> Unit) {
    val disc = remember { Animatable(0f) }
    val fall = remember { Animatable(0f) }
    val land = remember { Animatable(0f) }
    val wave = remember { Animatable(0f) }
    val burst = remember { Animatable(0f) }
    val word = remember { Animatable(0f) }
    var done by remember { mutableStateOf(false) }
    val finish = { if (!done) { done = true; onFinished() } }

    LaunchedEffect(Unit) {
        launch { disc.animateTo(1f, spring(dampingRatio = 0.6f, stiffness = 300f)) }
        delay(350)
        fall.animateTo(1f, tween(500, easing = CubicBezierEasing(0.55f, 0f, 1f, 0.45f))) // 加速下落
        launch { land.animateTo(1f, tween(500, easing = LinearEasing)) }
        launch { burst.animateTo(1f, tween(750, easing = LinearOutSlowInEasing)) }
        delay(450)
        launch {
            wave.animateTo(1f, keyframes {
                durationMillis = 900
                0f at 0; 14f at 200 using FastOutSlowInEasing; 3f at 400 using FastOutSlowInEasing
                13f at 630 using FastOutSlowInEasing; 0f at 900 using FastOutSlowInEasing
            })
        }
        delay(450)
        word.animateTo(1f, tween(550, easing = LinearOutSlowInEasing))
        delay(450)
        finish()
    }

    val catW = 240.dp
    val catH = 312.dp
    Box(
        Modifier.fillMaxSize().background(BG)
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { finish() },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.offset(y = (-20).dp)) {
            Box(Modifier.size(catW, catH)) {
                // 圆底 + 影子
                Canvas(Modifier.matchParentSize()) {
                    val u = 1.dp.toPx()
                    val r = 150 * u * disc.value
                    if (r > 0f) drawCircle(DISC, r, Offset(size.width / 2, size.height / 2 + 10 * u))
                    val f = fall.value
                    if (f > 0f) {
                        val k = ((f - 0.4f) / 0.6f).coerceIn(0f, 1f)
                        val sw = size.width * 0.40f * (0.4f + 0.6f * k)
                        drawOval(TEAL.copy(alpha = 0.16f * k), Offset(size.width / 2 - sw, size.height - 7 * u), Size(sw * 2, 14 * u))
                    }
                }
                // 小猫：身体 + 手两层，一起落地、一起压扁；招手只转手
                val p = land.value
                val s = if (p > 0f) (sin(p * PI * 2.2) * exp(-p * 3.2)).toFloat() else 0f
                Box(
                    Modifier.matchParentSize().graphicsLayer {
                        val f = fall.value
                        alpha = if (f > 0f) 1f else 0f
                        translationY = -(1f - f) * size.height * 1.6f
                        scaleX = 1f + 0.10f * s
                        scaleY = 1f - 0.12f * s
                        transformOrigin = TransformOrigin(0.5f, 1f)
                    }
                ) {
                    Image(painterResource(R.drawable.zhixing_cat_body), null, Modifier.matchParentSize())
                    Image(
                        painterResource(R.drawable.zhixing_cat_arm), null,
                        Modifier.matchParentSize().graphicsLayer {
                            rotationZ = wave.value          // 正数 = 顺时针，手往脸这边摆
                            transformOrigin = TransformOrigin(0.2102f, 0.5807f)
                        }
                    )
                }
                // 四色小点
                Canvas(Modifier.matchParentSize()) {
                    val b = burst.value
                    if (b in 0.001f..0.999f) {
                        val angles = listOf(-160.0, -20.0, -130.0, -50.0)
                        TYPE_COLORS.forEachIndexed { i, c ->
                            val rad = Math.toRadians(angles[i])
                            val r = size.width * (0.3f + 0.45f * (1 - (1 - b) * (1 - b) * (1 - b)))
                            val x = size.width / 2 + (cos(rad) * r).toFloat()
                            val y = size.height - 20.dp.toPx() + (sin(rad) * r * 0.9).toFloat()
                            drawCircle(c.copy(alpha = 1f - b * b * b), 7.dp.toPx() * sin(b * PI).toFloat(), Offset(x, y))
                        }
                    }
                }
            }
            Spacer(Modifier.height(40.dp))
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.alpha(word.value).offset(y = ((1 - word.value) * 16).dp)
            ) {
                Text("知行", color = TEAL, fontSize = 30.sp, fontWeight = FontWeight.ExtraBold)
                Spacer(Modifier.height(8.dp))
                Text("记下今天的每一件小事", color = Color(0xFF828C8A), fontSize = 13.sp)
            }
        }
    }
}
