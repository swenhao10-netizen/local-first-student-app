package com.swenhao10.localfirst

import android.speech.tts.TextToSpeech
import androidx.compose.runtime.DisposableEffect
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import java.text.SimpleDateFormat
import java.util.Date
import com.swenhao10.localfirst.data.AppDatabase
import com.swenhao10.localfirst.data.PackWordLite
import com.swenhao10.localfirst.data.WordProgress
import com.swenhao10.localfirst.data.WordProgressDao
import com.swenhao10.localfirst.data.WordStudy
import com.swenhao10.localfirst.data.lite
import android.Manifest
import android.app.TimePickerDialog
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.calculateRotation
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronLeft
import com.swenhao10.localfirst.ui.LaunchAnimation
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.togetherWith
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.fadeOut
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import com.swenhao10.localfirst.ui.Motion
import com.swenhao10.localfirst.ui.LocalReduceMotion
import com.swenhao10.localfirst.ui.motion
import com.swenhao10.localfirst.ui.pressScale
import com.swenhao10.localfirst.ui.rememberReduceMotion
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Info
import androidx.compose.ui.text.drawText
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import com.kyant.backdrop.drawBackdrop
import com.swenhao10.localfirst.ui.liquid.LiquidBottomTab
import com.swenhao10.localfirst.ui.liquid.LiquidBottomTabs
import com.swenhao10.localfirst.ui.liquid.LiquidButton
import com.kyant.backdrop.backdrops.LayerBackdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import androidx.compose.ui.graphics.Shape
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.runtime.produceState
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.swenhao10.localfirst.data.CardDao
import com.swenhao10.localfirst.data.DataTransfer
import com.swenhao10.localfirst.data.PackMeta
import com.swenhao10.localfirst.data.PackStore
import com.swenhao10.localfirst.data.PackWord
import com.swenhao10.localfirst.data.PaletteId
import com.swenhao10.localfirst.data.RecordDao
import com.swenhao10.localfirst.data.RecordEntry
import com.swenhao10.localfirst.data.ReminderScheduler
import com.swenhao10.localfirst.data.SettingsStore
import com.swenhao10.localfirst.data.StudySnapshot
import com.swenhao10.localfirst.data.ThemeMode
import com.swenhao10.localfirst.ui.DayNightToggle
import com.swenhao10.localfirst.data.UnknownWord
import com.swenhao10.localfirst.data.UnknownWordDao
import com.swenhao10.localfirst.data.VocabularyCard
import com.swenhao10.localfirst.data.images
import com.swenhao10.localfirst.data.isVideoPath
import com.swenhao10.localfirst.data.isVideoUri
import com.swenhao10.localfirst.data.extensionFor
import com.swenhao10.localfirst.data.videoFrame
import com.swenhao10.localfirst.data.videoDurationMs
import com.swenhao10.localfirst.data.formatDuration
import com.swenhao10.localfirst.data.saveToGallery
import com.swenhao10.localfirst.data.galleryNeedsPermission
import com.swenhao10.localfirst.data.AiExport
import android.content.Intent
import android.content.ClipData
import android.content.ClipboardManager
import android.widget.Toast
import android.widget.VideoView
import androidx.compose.ui.viewinterop.AndroidView
import androidx.activity.result.PickVisualMediaRequest
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animate
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntOffset
import kotlin.math.roundToInt
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material.icons.filled.Check
import androidx.compose.foundation.layout.imePadding
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.key
import com.swenhao10.localfirst.autobook.AutoBook
import com.swenhao10.localfirst.autobook.Payment
import androidx.lifecycle.compose.LifecycleResumeEffect
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.zIndex
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.material3.LocalContentColor
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.Calendar
import java.util.Locale

class MainActivity : ComponentActivity() {
    // 离开 App 时把桌面贴纸板刷新一下
    override fun onStop() {
        super.onStop()
        com.swenhao10.localfirst.widget.StickerWidgetBase.refreshAll(this)
        // 离开知行就自动备份一份到「文档/知行」（数据没变就跳过）
        com.swenhao10.localfirst.data.AutoBackup.schedule(this)
    }
    override fun onCreate(state: Bundle?) {
        installCrashLog()
        super.onCreate(state)
        // 自动记账：打开 App 时把付款通知监听重新绑上，并排好 15 分钟一次的保活
        AutoBook.ensureBound(this)
        AutoBook.scheduleKeepAlive(this)
        val db = AppDatabase.get(this)
        val crashFile = java.io.File(filesDir, "last_crash.txt")
        val lastCrash = runCatching { if (crashFile.exists()) crashFile.readText() else null }.getOrNull()
        setContent {
            // 启动动画只在冷启动放一次（转屏等重建不再放），点一下可跳过
            var showSplash by rememberSaveable { mutableStateOf(state == null) }
            var crashText by remember { mutableStateOf(lastCrash) }
            Box(Modifier.fillMaxSize()) {
                // 启动动画放完之前，别的 Tab 不预先组合，把这段时间全留给启动动画
                LocalFirstApp(db, launchDone = !showSplash)
                if (showSplash) LaunchAnimation(onFinished = { showSplash = false })
                if (!showSplash) RestorePrompt(db)
                crashText?.let { txt ->
                    // 上次闪退的原因：只存在本机，复制给开发者看
                    val clip = androidx.compose.ui.platform.LocalClipboardManager.current
                    androidx.compose.material3.AlertDialog(
                        onDismissRequest = { },
                        title = { Text("上次闪退了") },
                        text = {
                            Column(Modifier.heightIn(max = 360.dp).verticalScroll(rememberScrollState())) {
                                Text("点「复制」把下面的内容发给我，我按它修。", style = MaterialTheme.typography.bodySmall)
                                Spacer(Modifier.height(8.dp))
                                androidx.compose.foundation.text.selection.SelectionContainer {
                                    Text(txt, style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        },
                        confirmButton = {
                            TextButton(onClick = { clip.setText(androidx.compose.ui.text.AnnotatedString(txt)) }) { Text("复制") }
                        },
                        dismissButton = {
                            TextButton(onClick = { crashFile.delete(); crashText = null }) { Text("关闭") }
                        }
                    )
                }
            }
        }
    }

    /** 闪退时把原因写到本机文件，下次打开弹出来，方便复制。 */
    private fun installCrashLog() {
        val prev = Thread.getDefaultUncaughtExceptionHandler()
        val dir = filesDir
        Thread.setDefaultUncaughtExceptionHandler { t, e ->
            runCatching {
                val sw = java.io.StringWriter()
                e.printStackTrace(java.io.PrintWriter(sw))
                val head = "知行 ${runCatching { packageManager.getPackageInfo(packageName, 0).versionName }.getOrNull()} · " +
                    "Android ${android.os.Build.VERSION.RELEASE} · ${android.os.Build.MODEL} · 线程 ${t.name}\n" +
                    java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.CHINA).format(java.util.Date()) + "\n\n"
                java.io.File(dir, "last_crash.txt").writeText(head + sw.toString().take(12000))
            }
            prev?.uncaughtException(t, e)
        }
    }
}

/** v2 底部导航：记录（首页）/ 单词 / 总览 / 我的 */
/** 应用内的轻提示：一颗小胶囊，跟玻璃风格一致，替代默认的大白条 Snackbar。 */
@Composable
private fun TipPill(text: String, actionLabel: String? = null, onAction: () -> Unit = {}) {
    val dark = MaterialTheme.colorScheme.background.red < 0.5f
    val shape = RoundedCornerShape(50)
    Box(Modifier.fillMaxWidth().padding(bottom = 8.dp), contentAlignment = Alignment.Center) {
        Row(
            Modifier
                .widthIn(max = 320.dp)
                .then(if (LocalBackdrop.current != null) Modifier.glass(shape, if (dark) Color(0xB3181C1C) else Color(0xCCFFFFFF)) else Modifier.clip(shape).background(MaterialTheme.colorScheme.inverseSurface))
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier.size(8.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary))
            Spacer(Modifier.width(10.dp))
            Text(
                text,
                style = MaterialTheme.typography.labelLarge,
                color = if (LocalBackdrop.current != null) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.inverseOnSurface,
                maxLines = 2,
                modifier = Modifier.weight(1f, fill = false)
            )
            if (actionLabel != null) {
                Spacer(Modifier.width(4.dp))
                TextButton(onClick = onAction) {
                    Text(
                        actionLabel,
                        fontWeight = FontWeight.Bold,
                        color = if (LocalBackdrop.current != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.inversePrimary
                    )
                }
            }
        }
    }
}

/**
 * 向右滑动移除：拖过宽度的 35%，或者往右甩（> 800dp/s）就删；不够就弹簧弹回。
 * 拖动时位移只在 graphicsLayer 里读，不触发重组；红色底也在 draw 阶段按进度画。
 * 越过阈值那一刻给一次触感。拖动期间卡片改画便宜的半透明底（不做实时模糊），松手落定后再恢复玻璃。
 */
@Composable
private fun SwipeToRemove(onRemove: () -> Unit, content: @Composable () -> Unit) {
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    val flingPx = with(density) { 800.dp.toPx() }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var widthPx by remember { mutableFloatStateOf(1f) }
    var armed by remember { mutableStateOf(false) }
    var moving by remember { mutableStateOf(false) }
    var removed by remember { mutableStateOf(false) }
    var anim by remember { mutableStateOf<Job?>(null) }
    val red = MaterialTheme.colorScheme.error
    val onRed = MaterialTheme.colorScheme.onError
    val haptic = LocalHapticFeedback.current
    val reduce = LocalReduceMotion.current
    val iconScale by animateFloatAsState(
        if (armed) 1.15f else 1f,
        motion(spring(dampingRatio = 0.6f, stiffness = 600f)),
        label = "swipeIcon"
    )
    val shape = RoundedCornerShape(18.dp)
    Box(Modifier.fillMaxWidth().onSizeChanged { widthPx = it.width.toFloat().coerceAtLeast(1f) }) {
        // 背后的红底：只有卡片挪开时才看得到；颜色深浅按进度在 draw 阶段算
        Box(
            Modifier
                .matchParentSize()
                .graphicsLayer { alpha = if (offsetX > 0.5f) 1f else 0f }
                .clip(shape)
                .drawBehind {
                    val p = (offsetX / (widthPx * 0.35f)).coerceIn(0f, 1f)
                    drawRect(red.copy(alpha = 0.25f + 0.6f * p))
                }
                .padding(start = 22.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Delete, null, tint = onRed,
                    modifier = Modifier.graphicsLayer { scaleX = iconScale; scaleY = iconScale }
                )
                Spacer(Modifier.width(8.dp))
                Text(if (armed) "松手移出" else "移出生词本", color = onRed, fontWeight = FontWeight.SemiBold)
            }
        }
        Box(
            Modifier
                .graphicsLayer { translationX = offsetX }
                .draggable(
                    orientation = Orientation.Horizontal,
                    enabled = !removed,
                    onDragStarted = {
                        anim?.cancel()
                        moving = true
                    },
                    state = rememberDraggableState { d ->
                        val next = (offsetX + d).coerceIn(0f, widthPx)
                        offsetX = next
                        val now = next >= widthPx * 0.35f
                        if (now != armed) {
                            armed = now
                            if (now) haptic.performHapticFeedback(HapticFeedbackType.GestureThresholdActivate)
                        }
                    },
                    onDragStopped = { v ->
                        val go = offsetX >= widthPx * 0.35f || v > flingPx
                        anim = scope.launch {
                            if (go) {
                                removed = true
                                if (!armed) haptic.performHapticFeedback(HapticFeedbackType.GestureThresholdActivate)
                                if (reduce) offsetX = widthPx
                                else animate(offsetX, widthPx, initialVelocity = v.coerceAtLeast(0f), animationSpec = spring(dampingRatio = 1f, stiffness = 700f, visibilityThreshold = 4f)) { value, _ -> offsetX = value }
                                onRemove()
                            } else {
                                armed = false
                                if (reduce) offsetX = 0f
                                else animate(offsetX, 0f, initialVelocity = v, animationSpec = spring(dampingRatio = 0.85f, stiffness = 450f, visibilityThreshold = 0.5f)) { value, _ -> offsetX = value }
                                moving = false
                            }
                        }
                    }
                )
        ) {
            CompositionLocalProvider(LocalGlassCheap provides moving) { content() }
        }
    }
}

/** 弹窗宿主里的一项：内容 + 进出状态。调用方离开组合时只把 targetState 设成 false，等退场动画放完宿主再移除。 */
private class OverlayEntry(content: @Composable (MutableTransitionState<Boolean>) -> Unit) {
    var content by mutableStateOf(content)
    val visible = MutableTransitionState(false).apply { targetState = true }
}

/**
 * 统一的弹窗：从底部浮起的圆角面板，跟玻璃风格一致（深色半透明 + 细描边 + 拖动条），
 * 参数跟 AlertDialog 一样，直接替换。
 */
@Composable
private fun ZhAlertDialog(
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit = {},
    dismissButton: (@Composable () -> Unit)? = null,
    title: (@Composable () -> Unit)? = null,
    scrollText: Boolean = true,
    horizontalPadding: Dp = 22.dp,
    text: (@Composable () -> Unit)? = null
) {
    val host = LocalOverlay.current
    val panel: @Composable (MutableTransitionState<Boolean>) -> Unit = { vis -> ZhSheet(vis, onDismissRequest, confirmButton, dismissButton, title, text, scrollText, horizontalPadding) }
    if (host == null) {
        val vis = remember { MutableTransitionState(false).apply { targetState = true } }
        Dialog(onDismissRequest = onDismissRequest, properties = DialogProperties(usePlatformDefaultWidth = false)) { panel(vis) }
        return
    }
    val id = remember { Any() }
    SideEffect {
        val e = host[id]
        if (e == null) host[id] = OverlayEntry(panel) else e.content = panel
    }
    DisposableEffect(id) { onDispose { host[id]?.visible?.targetState = false } }
}

/**
 * 底部浮起的玻璃面板：压暗背后 + 真玻璃（模糊折射背景）+ 细描边 + 拖动条。
 * 进：弹簧升起（不回弹）；出：180ms 下滑；遮罩 200/150ms 渐变。拖动条真能拖：往下拖过 1/4 或甩一下就关。
 * 注意：text 槽外面已经套了 verticalScroll，里面不要再放 verticalScroll / Lazy*，会闪退。
 */
@Composable
private fun ZhSheet(
    visible: MutableTransitionState<Boolean>,
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    dismissButton: (@Composable () -> Unit)?,
    title: (@Composable () -> Unit)?,
    text: (@Composable () -> Unit)?,
    scrollText: Boolean = true,
    horizontalPadding: Dp = 22.dp
) {
    val dark = MaterialTheme.colorScheme.background.red < 0.5f
    // 弹层画在 Scaffold 外面，LocalContentColor 默认是黑色——没写颜色的字在深色玻璃上就成了黑字。
    // 这里统一给面板里的字定颜色：深色 = 白字，次要字 = 白色 75%；浅色 = 主题的深色字。
    val baseScheme = MaterialTheme.colorScheme
    val sheetScheme = if (dark) baseScheme.copy(
        onSurface = Color.White,
        onBackground = Color.White,
        onSurfaceVariant = Color.White.copy(alpha = .75f)
    ) else baseScheme
    val sheetFg = if (dark) Color.White else baseScheme.onSurface
    val glassOn = LocalBackdrop.current != null
    val reduce = LocalReduceMotion.current
    val open = visible.targetState
    BackHandler(enabled = open, onBack = onDismissRequest)
    val scrimTarget = if (dark) .45f else .28f
    // 遮罩从 0 渐变到目标透明度（进 200ms / 出 150ms），不再突然压暗
    val scrimAnim = remember { Animatable(0f) }
    LaunchedEffect(open) { scrimAnim.animateTo(if (open) scrimTarget else 0f, tween(if (open) 200 else 150, easing = Motion.EaseOut)) }
    val scope = rememberCoroutineScope()
    val dragY = remember { Animatable(0f) }
    var panelH by remember { mutableStateOf(1f) }
    val scrimSource = remember { MutableInteractionSource() }
    Box(
        Modifier.fillMaxSize()
            .drawBehind { drawRect(Color.Black.copy(alpha = scrimAnim.value)) }
            .clickable(interactionSource = scrimSource, indication = null, enabled = open, onClick = onDismissRequest),
        contentAlignment = Alignment.BottomCenter
    ) {
        AnimatedVisibility(
            visibleState = visible,
            enter = if (reduce) fadeIn(tween(Motion.SMALL)) else slideInVertically(spring(dampingRatio = 1f, stiffness = 500f)) { it } + fadeIn(tween(120)),
            exit = if (reduce) fadeOut(tween(120)) else slideOutVertically(tween(180, easing = Motion.EaseOut)) { it } + fadeOut(tween(120))
        ) {
            val shape = RoundedCornerShape(30.dp)
            MaterialTheme(colorScheme = sheetScheme) {
            CompositionLocalProvider(LocalContentColor provides sheetFg) {
            Column(
                Modifier
                    .padding(horizontal = 12.dp, vertical = 16.dp)
                    .navigationBarsPadding()
                    .imePadding()
                    .fillMaxWidth()
                    .heightIn(max = 640.dp)
                    .onSizeChanged { panelH = it.height.toFloat().coerceAtLeast(1f) }
                    .offset { IntOffset(0, dragY.value.roundToInt()) }
                    .then(
                        if (glassOn) Modifier.glass(shape, if (dark) Color(0xA6141B1A) else Color(0xC7FFFFFF), blurDp = 24f, lensDp = 18f)
                        else Modifier.clip(shape).background(if (dark) Color(0xF2161D1C) else Color(0xF7FBFDFC))
                    )
                    .border(1.dp, Color.White.copy(alpha = if (dark) .14f else .5f), shape)
                    .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {}
                    .padding(horizontal = horizontalPadding)
                    .padding(bottom = 14.dp)
            ) {
                // 拖动条：往下拖跟手，往上拖有阻力；松手看速度和距离决定关还是弹回
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(28.dp)
                        .draggable(
                            orientation = Orientation.Vertical,
                            enabled = open,
                            state = rememberDraggableState { d ->
                                val cur = dragY.value
                                val next = if (cur + d < 0f) cur + d * 0.3f else cur + d
                                scope.launch { dragY.snapTo(next.coerceAtLeast(-60f)) }
                            },
                            onDragStopped = { v ->
                                if (v > 1000f || dragY.value > panelH * 0.25f) {
                                    onDismissRequest()
                                    delay(60)
                                    // 调用方没有关（比如正在下载），就弹回去
                                    if (visible.targetState) dragY.animateTo(0f, spring(dampingRatio = 0.8f, stiffness = 400f))
                                } else {
                                    dragY.animateTo(0f, spring(dampingRatio = 0.8f, stiffness = 400f), initialVelocity = v)
                                }
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Box(Modifier.size(36.dp, 4.dp).clip(RoundedCornerShape(50)).background(MaterialTheme.colorScheme.onSurface.copy(alpha = .25f)))
                }
                if (title != null) {
                    ProvideTextStyle(MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)) { title() }
                    Spacer(Modifier.height(10.dp))
                }
                if (text != null) {
                    Box(Modifier.weight(1f, fill = false).then(if (scrollText) Modifier.verticalScroll(rememberScrollState()) else Modifier)) {
                        ProvideTextStyle(MaterialTheme.typography.bodyMedium) { text() }
                    }
                }
                Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                    dismissButton?.invoke()
                    confirmButton()
                }
            }
            }
            }
        }
    }
}

private enum class Tab(val label: String) { RECORD("记录"), WORDS("单词"), OVERVIEW("总览"), DATA("我的") }

private val ZhixingColors = lightColorScheme(
    primary = Color(0xFF0B6B68),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD9F1E8),
    onPrimaryContainer = Color(0xFF073D3A),
    secondary = Color(0xFFB56B3C),
    secondaryContainer = Color(0xFFFFDDC7),
    background = Color(0xFFF7F9F5),
    surface = Color.White,
    surfaceVariant = Color(0xFFE7EFEB),
    onSurface = Color(0xFF182323),
    onSurfaceVariant = Color(0xFF536360)
)

/** 深色配色：和白天同一套色板思路，主色提亮、背景/卡片/文字都换深色值。 */
private val ZhixingDarkColors = darkColorScheme(
    primary = Color(0xFF4DB8B2),
    onPrimary = Color(0xFF073D3A),
    primaryContainer = Color(0xFF0B3B38),
    onPrimaryContainer = Color(0xFFD9F1E8),
    secondary = Color(0xFFD99A6C),
    secondaryContainer = Color(0xFF4A2E1E),
    background = Color(0xFF121816),
    surface = Color(0xFF1B2321),
    surfaceVariant = Color(0xFF26312E),
    onSurface = Color(0xFFE6EDEA),
    onSurfaceVariant = Color(0xFFA7B5B1)
)

/** 海蓝：主色偏冷蓝，辅色留一点沙色，白底更清亮。 */
private val OceanColors = lightColorScheme(
    primary = Color(0xFF175D8D),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD6E8F7),
    onPrimaryContainer = Color(0xFF072F49),
    secondary = Color(0xFFA9713F),
    secondaryContainer = Color(0xFFFFDDC4),
    background = Color(0xFFF6F8FB),
    surface = Color.White,
    surfaceVariant = Color(0xFFE4EBF3),
    onSurface = Color(0xFF16212B),
    onSurfaceVariant = Color(0xFF4E5F70)
)

private val OceanDarkColors = darkColorScheme(
    primary = Color(0xFF7FB6E0),
    onPrimary = Color(0xFF05293F),
    primaryContainer = Color(0xFF11384F),
    onPrimaryContainer = Color(0xFFD6E8F7),
    secondary = Color(0xFFD8A572),
    secondaryContainer = Color(0xFF3E2A16),
    background = Color(0xFF0F1519),
    surface = Color(0xFF171F26),
    surfaceVariant = Color(0xFF22303A),
    onSurface = Color(0xFFE4ECF3),
    onSurfaceVariant = Color(0xFFA6B6C4)
)

/** 紫罗兰：偏静的紫，浅色底带一点灰调，深色底不刺眼。 */
private val VioletColors = lightColorScheme(
    primary = Color(0xFF6A52A3),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE9E1F7),
    onPrimaryContainer = Color(0xFF2C1D4A),
    secondary = Color(0xFFA9763F),
    secondaryContainer = Color(0xFFFFDDC4),
    background = Color(0xFFF8F6FB),
    surface = Color.White,
    surfaceVariant = Color(0xFFEDE7F4),
    onSurface = Color(0xFF1D1826),
    onSurfaceVariant = Color(0xFF5B5470)
)

private val VioletDarkColors = darkColorScheme(
    primary = Color(0xFFBB9BE8),
    onPrimary = Color(0xFF2A1A45),
    primaryContainer = Color(0xFF3A2A57),
    onPrimaryContainer = Color(0xFFE9E1F7),
    secondary = Color(0xFFD8A572),
    secondaryContainer = Color(0xFF3E2A16),
    background = Color(0xFF141118),
    surface = Color(0xFF1C1823),
    surfaceVariant = Color(0xFF2A2536),
    onSurface = Color(0xFFE8E3EF),
    onSurfaceVariant = Color(0xFFB0A9BE)
)

/** 暖橙：主色偏暖，辅色压成深绿，避免整页都在同一个暖调里。 */
private val AmberColors = lightColorScheme(
    primary = Color(0xFFB25C1D),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFE2CB),
    onPrimaryContainer = Color(0xFF3E1D06),
    secondary = Color(0xFF3F6A57),
    secondaryContainer = Color(0xFFD7EBDF),
    background = Color(0xFFFDF8F4),
    surface = Color.White,
    surfaceVariant = Color(0xFFF1E6DC),
    onSurface = Color(0xFF241A12),
    onSurfaceVariant = Color(0xFF61544A)
)

private val AmberDarkColors = darkColorScheme(
    primary = Color(0xFFFFB27A),
    onPrimary = Color(0xFF4A2205),
    primaryContainer = Color(0xFF6B3410),
    onPrimaryContainer = Color(0xFFFFE2CB),
    secondary = Color(0xFF95C7B0),
    secondaryContainer = Color(0xFF22402F),
    background = Color(0xFF17110D),
    surface = Color(0xFF201913),
    surfaceVariant = Color(0xFF2C241D),
    onSurface = Color(0xFFF0E6DE),
    onSurfaceVariant = Color(0xFFB7A899)
)

/** 樱粉：粉做主色但不发甜，辅色偏棕，深色底用烟粉。 */
private val RoseColors = lightColorScheme(
    primary = Color(0xFFB23A63),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFDDE6),
    onPrimaryContainer = Color(0xFF40071A),
    secondary = Color(0xFF8A6A3F),
    secondaryContainer = Color(0xFFF2E3CD),
    background = Color(0xFFFDF7F9),
    surface = Color.White,
    surfaceVariant = Color(0xFFF4E5EB),
    onSurface = Color(0xFF23171B),
    onSurfaceVariant = Color(0xFF64545A)
)

private val RoseDarkColors = darkColorScheme(
    primary = Color(0xFFFFA8C2),
    onPrimary = Color(0xFF4B0D22),
    primaryContainer = Color(0xFF641B33),
    onPrimaryContainer = Color(0xFFFFDDE6),
    secondary = Color(0xFFD9B07A),
    secondaryContainer = Color(0xFF3E2A16),
    background = Color(0xFF171013),
    surface = Color(0xFF20171B),
    surfaceVariant = Color(0xFF2C1F25),
    onSurface = Color(0xFFF2E4E9),
    onSurfaceVariant = Color(0xFFC0AAB2)
)

/** 一套主题色 = 浅色 + 深色两个 ColorScheme，按 PaletteId 取。 */
private data class AppPalette(
    val id: String,
    val label: String,
    val light: androidx.compose.material3.ColorScheme,
    val dark: androidx.compose.material3.ColorScheme
)

private val APP_PALETTES = listOf(
    AppPalette(PaletteId.GREEN, "青绿", ZhixingColors, ZhixingDarkColors),
    AppPalette(PaletteId.BLUE, "海蓝", OceanColors, OceanDarkColors),
    AppPalette(PaletteId.PURPLE, "紫罗兰", VioletColors, VioletDarkColors),
    AppPalette(PaletteId.ORANGE, "暖橙", AmberColors, AmberDarkColors),
    AppPalette(PaletteId.PINK, "樱粉", RoseColors, RoseDarkColors)
)

private fun paletteFor(id: String, dark: Boolean, customArgb: Int = 0xFF3A7BD5.toInt()): androidx.compose.material3.ColorScheme {
    if (id == PaletteId.CUSTOM) return customScheme(customArgb, dark)
    val set = APP_PALETTES.firstOrNull { it.id == id } ?: APP_PALETTES.first()
    return if (dark) set.dark else set.light
}

/** HSL 取色：h 0..360，s/l 0..1。 */
private fun hsl(h: Float, s: Float, l: Float): Color {
    val out = FloatArray(3)
    out[0] = ((h % 360f) + 360f) % 360f; out[1] = s.coerceIn(0f, 1f); out[2] = l.coerceIn(0f, 1f)
    return Color(androidx.core.graphics.ColorUtils.HSLToColor(out))
}

/** 用一个种子色生成整套浅色 / 深色 ColorScheme：主色保证对比度，背景和卡片带一点同色调。 */
private fun customScheme(argb: Int, dark: Boolean): androidx.compose.material3.ColorScheme {
    val f = FloatArray(3)
    androidx.core.graphics.ColorUtils.colorToHSL(argb, f)
    val h = f[0]; val s = f[1]
    val sat = if (s < 0.06f) s else s.coerceIn(0.35f, 0.85f)
    val tint = (sat * 0.35f).coerceAtMost(0.25f)
    val sh = h + 150f
    return if (!dark) lightColorScheme(
        primary = hsl(h, sat, f[2].coerceIn(0.30f, 0.45f)),
        onPrimary = Color.White,
        primaryContainer = hsl(h, sat * 0.7f, 0.90f),
        onPrimaryContainer = hsl(h, sat, 0.15f),
        secondary = hsl(sh, 0.45f, 0.45f),
        secondaryContainer = hsl(sh, 0.5f, 0.88f),
        onSecondaryContainer = hsl(sh, 0.5f, 0.15f),
        tertiary = hsl(h + 60f, 0.4f, 0.42f),
        tertiaryContainer = hsl(h + 60f, 0.45f, 0.88f),
        onTertiaryContainer = hsl(h + 60f, 0.45f, 0.15f),
        background = hsl(h, tint, 0.97f),
        onBackground = hsl(h, tint, 0.12f),
        surface = hsl(h, tint, 0.99f),
        surfaceVariant = hsl(h, tint, 0.92f),
        onSurface = hsl(h, tint, 0.12f),
        onSurfaceVariant = hsl(h, tint * 0.8f, 0.38f)
    ) else darkColorScheme(
        primary = hsl(h, sat, 0.70f),
        onPrimary = hsl(h, sat, 0.15f),
        primaryContainer = hsl(h, sat * 0.7f, 0.22f),
        onPrimaryContainer = hsl(h, sat * 0.7f, 0.90f),
        secondary = hsl(sh, 0.45f, 0.68f),
        secondaryContainer = hsl(sh, 0.4f, 0.20f),
        onSecondaryContainer = hsl(sh, 0.4f, 0.88f),
        tertiary = hsl(h + 60f, 0.4f, 0.70f),
        tertiaryContainer = hsl(h + 60f, 0.35f, 0.22f),
        onTertiaryContainer = hsl(h + 60f, 0.35f, 0.88f),
        background = hsl(h, tint, 0.08f),
        onBackground = hsl(h, tint, 0.92f),
        surface = hsl(h, tint, 0.11f),
        surfaceVariant = hsl(h, tint, 0.17f),
        onSurface = hsl(h, tint, 0.92f),
        onSurfaceVariant = hsl(h, tint * 0.8f, 0.70f)
    )
}

private val CUSTOM_PRESETS = listOf(
    0xFF3A7BD5, 0xFF0B6B68, 0xFF2E9D5B, 0xFF8BAA2E, 0xFFE0A000, 0xFFE0662D,
    0xFFD43C3C, 0xFFD6457A, 0xFFA34FC4, 0xFF5B5BD6, 0xFF2AA7C9, 0xFF5E6B73
).map { it.toInt() }

/** 渐变滑条：轨道是一条渐变，圆形拇指可拖可点，按下时放大。 */
@Composable
private fun GradientSlider(value: Float, brush: Brush, thumbColor: Color, onChange: (Float) -> Unit) {
    var widthPx by remember { mutableStateOf(1f) }
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (pressed) 1.25f else 1f, spring(dampingRatio = 0.6f, stiffness = 500f), label = "thumb")
    val haptic = LocalHapticFeedback.current
    val density = LocalDensity.current
    Box(
        Modifier.fillMaxWidth().height(36.dp)
            .onSizeChanged { widthPx = it.width.toFloat().coerceAtLeast(1f) }
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown()
                    pressed = true
                    haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
                    onChange((down.position.x / widthPx).coerceIn(0f, 1f))
                    do {
                        val ev = awaitPointerEvent()
                        val c = ev.changes.firstOrNull() ?: break
                        if (c.pressed) { onChange((c.position.x / widthPx).coerceIn(0f, 1f)); c.consume() }
                    } while (ev.changes.any { it.pressed })
                    pressed = false
                }
            },
        contentAlignment = Alignment.CenterStart
    ) {
        Box(Modifier.fillMaxWidth().height(14.dp).clip(RoundedCornerShape(50)).background(brush).border(1.dp, Color.White.copy(alpha = .35f), RoundedCornerShape(50)))
        val thumb = 26.dp
        val x = with(density) { (value * (widthPx - thumb.toPx()).coerceAtLeast(0f)).toDp() }
        Box(
            Modifier.offset(x = x)
                .size(thumb)
                .graphicsLayer { scaleX = scale; scaleY = scale }
                .shadow(4.dp, CircleShape)
                .clip(CircleShape)
                .background(Color.White)
                .padding(4.dp)
                .clip(CircleShape)
                .background(thumbColor)
        )
    }
}

/** 自定义主题色面板：磨砂玻璃底部面板里，上面实时预览，中间色相/饱和/亮度三条滑条，下面常用色。 */
@Composable
private fun CustomColorSheet(initial: Int, dark: Boolean, onDismiss: () -> Unit, onApply: (Int) -> Unit) {
    val hsv = remember { FloatArray(3).also { android.graphics.Color.colorToHSV(initial, it) } }
    var hue by remember { mutableStateOf(hsv[0]) }
    var sat by remember { mutableStateOf(hsv[1]) }
    var value by remember { mutableStateOf(hsv[2]) }
    val argb = android.graphics.Color.HSVToColor(floatArrayOf(hue, sat, value))
    val scheme = remember(argb, dark) { customScheme(argb, dark) }
    val p by animateColorAsState(scheme.primary, tween(120), label = "pv")
    val pc by animateColorAsState(scheme.primaryContainer, tween(120), label = "pcv")
    val onPc by animateColorAsState(scheme.onPrimaryContainer, tween(120), label = "opcv")
    ZhAlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("自定义主题色") },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } },
        confirmButton = { Button(onClick = { onApply(argb) }, colors = ButtonDefaults.buttonColors(containerColor = p, contentColor = scheme.onPrimary)) { Text("用这个颜色") } },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                // 预览：模拟一张卡片 + 主按钮 + 标签
                Column(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp))
                        .background(Brush.linearGradient(listOf(pc.copy(alpha = .85f), p.copy(alpha = .35f))))
                        .border(1.dp, Color.White.copy(alpha = .4f), RoundedCornerShape(22.dp))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(34.dp).clip(CircleShape).background(p))
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text("今天", fontWeight = FontWeight.Bold, color = onPc)
                            Text("背了 30 个词 · 记了 3 笔", style = MaterialTheme.typography.labelMedium, color = onPc.copy(alpha = .75f))
                        }
                        Text(String.format("#%06X", argb and 0xFFFFFF), style = MaterialTheme.typography.labelLarge, color = onPc)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(Modifier.clip(RoundedCornerShape(50)).background(p).padding(horizontal = 16.dp, vertical = 8.dp)) { Text("记一笔", color = scheme.onPrimary, style = MaterialTheme.typography.labelLarge) }
                        Box(Modifier.clip(RoundedCornerShape(50)).border(1.dp, p, RoundedCornerShape(50)).padding(horizontal = 16.dp, vertical = 8.dp)) { Text("背单词", color = onPc, style = MaterialTheme.typography.labelLarge) }
                    }
                }
                Text("色相", style = MaterialTheme.typography.labelLarge)
                GradientSlider(hue / 360f, Brush.horizontalGradient((0..6).map { hsl(it * 60f, 1f, 0.5f) }), Color(android.graphics.Color.HSVToColor(floatArrayOf(hue, 1f, 1f)))) { hue = it * 360f }
                Text("鲜艳度", style = MaterialTheme.typography.labelLarge)
                GradientSlider(sat, Brush.horizontalGradient(listOf(Color(android.graphics.Color.HSVToColor(floatArrayOf(hue, 0f, value))), Color(android.graphics.Color.HSVToColor(floatArrayOf(hue, 1f, value))))), Color(argb)) { sat = it }
                Text("明暗", style = MaterialTheme.typography.labelLarge)
                GradientSlider(value, Brush.horizontalGradient(listOf(Color.Black, Color(android.graphics.Color.HSVToColor(floatArrayOf(hue, sat, 1f))))), Color(argb)) { value = it.coerceAtLeast(0.05f) }
                Text("常用色", style = MaterialTheme.typography.labelLarge)
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    CUSTOM_PRESETS.forEach { c ->
                        val selected = (c and 0xFFFFFF) == (argb and 0xFFFFFF)
                        // 弹簧会过冲到 0 以下 / 1 以上，padding 和描边不能是负数，夹一下
                        val ringRaw by animateFloatAsState(if (selected) 1f else 0f, spring(dampingRatio = 0.6f, stiffness = 500f), label = "ring")
                        val ring = ringRaw.coerceIn(0f, 1f)
                        val src = remember { MutableInteractionSource() }
                        Box(
                            Modifier.size(38.dp).pressScale(src)
                                .border((2.5f * ring).dp, Color.White.copy(alpha = ring), CircleShape)
                                .padding((4f * ring).dp)
                                .clip(CircleShape)
                                .background(Color(c))
                                .clickable(interactionSource = src, indication = null) {
                                    val t = FloatArray(3); android.graphics.Color.colorToHSV(c, t)
                                    hue = t[0]; sat = t[1]; value = t[2]
                                }
                        )
                    }
                }
            }
        }
    )
}

/** 磨砂玻璃主题开关：开着时背景是柔和渐变，卡片、底栏都是半透明的“玻璃”。 */
private val LocalGlass = staticCompositionLocalOf { false }
/** 玻璃要“透过去”看到的那层背景；null = 没开玻璃，退回普通半透明。 */
private val LocalBackdrop = staticCompositionLocalOf<LayerBackdrop?> { null }
/** 弹窗宿主：ZhAlertDialog 把自己挂到这里，画在主界面最上层。 */
private val LocalOverlay = staticCompositionLocalOf<androidx.compose.runtime.snapshots.SnapshotStateMap<Any, OverlayEntry>?> { null }
/** 这个 Tab 是否是当前显示的那个：保活的隐藏 Tab 里的返回键处理要关掉。 */
private val LocalTabActive = compositionLocalOf { true }
/** 这个 Tab 第几次变成可见（保活后入场动画靠它重放）。 */
private val LocalTabEnter = compositionLocalOf { 1 }
/** 卡片正在被拖动：GlassCard 临时画半透明实底，不做实时模糊/折射（每帧重算模糊会卡）。 */
private val LocalGlassCheap = compositionLocalOf { false }
/** 自定义背景图路径；null = 默认渐变。 */
private val LocalBgPath = staticCompositionLocalOf<String?> { null }

/**
 * 真·玻璃：模糊 + 透镜折射背后的背景，再叠一层淡淡的底色。
 * Android 12 以下没有模糊，库会自动跳过，只剩半透明底色。
 */
@Composable
private fun Modifier.glass(shape: RoundedCornerShape, tint: Color? = null, blurDp: Float = 14f, lensDp: Float = 14f): Modifier {
    val backdrop = LocalBackdrop.current
    val dark = MaterialTheme.colorScheme.background.red < 0.5f
    val fill = tint ?: if (dark) Color(0x52202C2A) else Color(0x66FFFFFF)
    if (backdrop == null) return this.clip(shape).background(MaterialTheme.colorScheme.surface)
    return this.drawBackdrop(
        backdrop = backdrop,
        shape = { shape },
        effects = {
            vibrancy()
            blur(blurDp.dp.toPx())
            lens(lensDp.dp.toPx(), (lensDp * 2).dp.toPx())
        },
        onDrawSurface = { drawRect(fill) }
    )
}

/**
 * 卡片：玻璃主题下是真玻璃（模糊+折射背景），带颜色的卡片叠一层淡淡的同色；
 * 关掉玻璃时就是原来的 Material 卡片。
 */
@Composable
private fun GlassCard(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.surface,
    shape: RoundedCornerShape = RoundedCornerShape(18.dp),
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    if (LocalBackdrop.current == null) {
        if (onClick != null) {
            Card(onClick = onClick, colors = CardDefaults.cardColors(containerColor = color), shape = shape, modifier = modifier, content = content)
        } else {
            Card(colors = CardDefaults.cardColors(containerColor = color), shape = shape, modifier = modifier, content = content)
        }
    } else {
        val scheme = MaterialTheme.colorScheme
        val plain = color == scheme.surface || color == scheme.surfaceVariant || color == scheme.background
        val cheap = LocalGlassCheap.current
        val darkBg = scheme.background.red < 0.5f
        Column(
            modifier
                .then(
                    if (cheap) Modifier.clip(shape).background(if (darkBg) Color(0xE61E2927) else Color(0xEBFFFFFF))
                        .then(if (plain) Modifier else Modifier.background(color.copy(alpha = 0.40f)))
                    else Modifier.glass(shape, if (plain) null else color.copy(alpha = 0.40f))
                )
                .then(if (onClick != null) Modifier.clip(shape).clickable(onClick = onClick) else Modifier),
            content = content
        )
    }
}

/** 玻璃背景：有自定义图就铺图（再压一层暗/亮让字看得清），没有就画默认渐变。 */
@Composable
private fun GlassBackground(dark: Boolean, modifier: Modifier = Modifier) {
    val path = LocalBgPath.current
    val bmp = rememberBitmap(path, 1600)
    Box(modifier.fillMaxSize()) {
        if (bmp != null) {
            Image(bitmap = bmp, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
            Box(Modifier.fillMaxSize().background(if (dark) Color(0x73000000) else Color(0x4DFFFFFF)))
        } else {
            GlassBackdrop(dark)
        }
    }
}

/** 玻璃模式下把卡片色换成半透明，背景交给 GlassBackdrop 画。 */
private fun glassScheme(base: androidx.compose.material3.ColorScheme, dark: Boolean) = base.copy(
    surface = if (dark) Color(0x9922302E) else Color(0xA6FFFFFF),
    surfaceVariant = if (dark) Color(0x80314341) else Color(0x99FFFFFF),
    surfaceContainerHigh = if (dark) Color(0xFF1E2A28) else Color(0xFFF8FBFA),
    background = if (dark) Color(0xFF0F1716) else Color(0xFFEEF4F1)
)

/** 玻璃背景：底色 + 三团大色块（主色、淡紫、暖桃），像透过毛玻璃看到的颜色。 */
@Composable
private fun GlassBackdrop(dark: Boolean, modifier: Modifier = Modifier) {
    val primary = MaterialTheme.colorScheme.primary
    val base = MaterialTheme.colorScheme.background
    Canvas(modifier.fillMaxSize()) {
        drawRect(base)
        val w = size.width; val h = size.height
        val blobs = if (dark) listOf(
            Triple(primary.copy(alpha = 0.35f), Offset(w * 0.1f, h * 0.08f), w * 0.9f),
            Triple(Color(0x66463A8C), Offset(w * 1.0f, h * 0.45f), w * 0.8f),
            Triple(Color(0x55704A36), Offset(w * 0.3f, h * 1.0f), w * 0.9f)
        ) else listOf(
            Triple(primary.copy(alpha = 0.28f), Offset(w * 0.1f, h * 0.06f), w * 0.95f),
            Triple(Color(0x80D5C8F2), Offset(w * 1.0f, h * 0.45f), w * 0.85f),
            Triple(Color(0x80F7D8C3), Offset(w * 0.35f, h * 1.0f), w * 0.95f)
        )
        blobs.forEach { (c, center, r) ->
            drawCircle(Brush.radialGradient(listOf(c, c.copy(alpha = 0f)), center = center, radius = r), radius = r, center = center)
        }
    }
}

/**
 * 玻璃底栏：液态玻璃 Tab（按住能拖，选中块像水珠一样滑过去）+ 右边液态玻璃「+」。
 * 没开玻璃时不会走到这里。
 */
@Composable
private fun GlassBottomBar(selected: Tab, onSelect: (Tab) -> Unit, onAdd: () -> Unit) {
    val dark = MaterialTheme.colorScheme.background.red < 0.5f
    val backdrop = LocalBackdrop.current ?: return
    val tabs = Tab.values()
    Row(
        Modifier.fillMaxWidth().navigationBarsPadding().padding(start = 16.dp, end = 16.dp, bottom = 10.dp, top = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 必须传一个「读 State 的稳定 lambda」：组件内部靠 snapshotFlow 观察它，
        // 直接写 { tabs.indexOf(selected) } 每次重组都是新对象且读的是快照值，点 Tab 时选中块不会跟着走
        val selectedIndexState = androidx.compose.runtime.rememberUpdatedState(tabs.indexOf(selected))
        val selectedIndex = remember { { selectedIndexState.value } }
        LiquidBottomTabs(
            selectedTabIndex = selectedIndex,
            onTabSelected = { onSelect(tabs[it]) },
            backdrop = backdrop,
            tabsCount = tabs.size,
            modifier = Modifier.weight(1f),
            accentColor = MaterialTheme.colorScheme.primary,
            isLightTheme = !dark
        ) {
            tabs.forEach { item ->
                LiquidBottomTab(onClick = { onSelect(item) }) {
                    Icon(iconFor(item), contentDescription = item.label, modifier = Modifier.size(22.dp), tint = MaterialTheme.colorScheme.onSurface)
                    Text(item.label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }
        Spacer(Modifier.width(12.dp))
        LiquidButton(
            onClick = onAdd,
            backdrop = backdrop,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(64.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "发记录", tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(30.dp))
        }
    }
}

/** 液态玻璃按钮；没开玻璃时退回普通按钮样式。tint 为空 = 无色玻璃。 */
@Composable
private fun GlassButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color? = null,
    enabled: Boolean = true,
    content: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit
) {
    val backdrop = LocalBackdrop.current
    val enabledAlpha by animateFloatAsState(if (enabled) 1f else 0.45f, tween(Motion.SMALL), label = "glassEnabled")
    if (backdrop != null) {
        LiquidButton(
            onClick = { if (enabled) onClick() },
            backdrop = backdrop,
            tint = tint ?: Color.Unspecified,
            modifier = modifier.graphicsLayer { alpha = enabledAlpha },
            content = content
        )
    } else if (tint != null) {
        Button(onClick = onClick, enabled = enabled, modifier = modifier, shape = RoundedCornerShape(50), colors = ButtonDefaults.buttonColors(containerColor = tint)) { content() }
    } else {
        OutlinedButton(onClick = onClick, enabled = enabled, modifier = modifier, shape = RoundedCornerShape(50)) { content() }
    }
}

/** 提醒色（待复习、记错次数）：固定的琥珀色，不随主题的 secondary 变（暖橙主题里 secondary 是绿色）。 */
@Composable
private fun warnTone(): Color =
    if (MaterialTheme.colorScheme.background.luminanceApprox() < 0.5f) Color(0xFFE6A85C) else Color(0xFFA35F0E)

private fun Color.luminanceApprox(): Float = 0.2126f * red + 0.7152f * green + 0.0722f * blue

/**
 * 会变的数字：只滚动变了的那几位（新数字从下往上进来，200ms），没变的位不动，停住后不再闪。
 * 按“从右数第几位”对齐，个位永远对着个位。减弱动画时只淡入淡出。
 */
@Composable
private fun RollingText(
    text: String,
    style: androidx.compose.ui.text.TextStyle,
    color: Color = Color.Unspecified,
    fontWeight: FontWeight? = null
) {
    val reduce = LocalReduceMotion.current
    Row {
        text.forEachIndexed { i, ch ->
            key(text.length - 1 - i) {
                AnimatedContent(
                    targetState = ch,
                    transitionSpec = {
                        if (reduce || !targetState.isDigit() || !initialState.isDigit()) {
                            fadeIn(tween(Motion.SMALL)) togetherWith fadeOut(tween(90)) using SizeTransform(clip = false)
                        } else {
                            val up = targetState > initialState
                            (slideInVertically(tween(Motion.STATE, easing = Motion.EaseOut)) { if (up) it else -it } + fadeIn(tween(Motion.STATE)))
                                .togetherWith(slideOutVertically(tween(Motion.STATE, easing = Motion.EaseOut)) { if (up) -it else it } + fadeOut(tween(Motion.EXIT)))
                                .using(SizeTransform(clip = false))
                        }
                    },
                    label = "digit"
                ) { c -> Text(c.toString(), style = style, color = color, fontWeight = fontWeight) }
            }
        }
    }
}

/** 换主题色 / 深浅：每个颜色 300ms 过渡，不整屏闪变。 */
@Composable
private fun animateColorScheme(target: androidx.compose.material3.ColorScheme): androidx.compose.material3.ColorScheme {
    val spec = tween<Color>(300, easing = Motion.EaseInOut)
    @Composable fun a(c: Color, l: String) = animateColorAsState(c, spec, label = l).value
    return target.copy(
        primary = a(target.primary, "primary"),
        onPrimary = a(target.onPrimary, "onPrimary"),
        primaryContainer = a(target.primaryContainer, "primaryContainer"),
        onPrimaryContainer = a(target.onPrimaryContainer, "onPrimaryContainer"),
        inversePrimary = a(target.inversePrimary, "inversePrimary"),
        secondary = a(target.secondary, "secondary"),
        onSecondary = a(target.onSecondary, "onSecondary"),
        secondaryContainer = a(target.secondaryContainer, "secondaryContainer"),
        onSecondaryContainer = a(target.onSecondaryContainer, "onSecondaryContainer"),
        tertiary = a(target.tertiary, "tertiary"),
        tertiaryContainer = a(target.tertiaryContainer, "tertiaryContainer"),
        onTertiaryContainer = a(target.onTertiaryContainer, "onTertiaryContainer"),
        background = a(target.background, "background"),
        onBackground = a(target.onBackground, "onBackground"),
        surface = a(target.surface, "surface"),
        onSurface = a(target.onSurface, "onSurface"),
        surfaceVariant = a(target.surfaceVariant, "surfaceVariant"),
        onSurfaceVariant = a(target.onSurfaceVariant, "onSurfaceVariant"),
        surfaceContainerHigh = a(target.surfaceContainerHigh, "surfaceContainerHigh"),
        surfaceContainer = a(target.surfaceContainer, "surfaceContainer"),
        inverseSurface = a(target.inverseSurface, "inverseSurface"),
        inverseOnSurface = a(target.inverseOnSurface, "inverseOnSurface"),
        outline = a(target.outline, "outline"),
        outlineVariant = a(target.outlineVariant, "outlineVariant")
    )
}

/**
 * 背完一轮：底部浮出一张“本轮完成”小卡（少见场景，给一点完成感）。
 * 勾是画出来的（320ms），配一次确认触感；2.6 秒后自己收起。不撒彩带。
 */
@Composable
private fun RoundDoneCard(count: Int, modifier: Modifier = Modifier) {
    val reduce = LocalReduceMotion.current
    val primary = MaterialTheme.colorScheme.primary
    val onPrimary = MaterialTheme.colorScheme.onPrimary
    val check = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        if (reduce) check.snapTo(1f) else { delay(80); check.animateTo(1f, tween(320, easing = Motion.EaseOut)) }
    }
    val shape = RoundedCornerShape(20.dp)
    Row(
        modifier
            .widthIn(max = 340.dp)
            .then(if (LocalBackdrop.current != null) Modifier.glass(shape) else Modifier.clip(shape).background(MaterialTheme.colorScheme.surfaceContainerHigh))
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Canvas(Modifier.size(32.dp)) {
            drawCircle(primary)
            val path = androidx.compose.ui.graphics.Path().apply {
                moveTo(size.width * 0.28f, size.height * 0.52f)
                lineTo(size.width * 0.44f, size.height * 0.67f)
                lineTo(size.width * 0.73f, size.height * 0.36f)
            }
            val measure = androidx.compose.ui.graphics.PathMeasure().apply { setPath(path, false) }
            val seg = androidx.compose.ui.graphics.Path()
            measure.getSegment(0f, measure.length * check.value, seg, true)
            drawPath(seg, onPrimary, style = Stroke(width = 3.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round, join = androidx.compose.ui.graphics.StrokeJoin.Round))
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Text("本轮完成", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("背了 $count 个词，已记到记录里", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/** 分类配色：只给现有分类上色，其余走灰色。 */
private fun categoryColor(category: String?, primary: Color): Color = when (category) {
    "餐饮" -> Color(0xFFE08A2C)
    "购物" -> Color(0xFFD9536A)
    "交通" -> Color(0xFF3F7FD9)
    "娱乐" -> Color(0xFF965AC8)
    "学习" -> primary
    else -> Color(0xFF8A9592)
}

/** 记账分类（默认值，方案待定项） */private val EXPENSE_CATEGORIES = listOf("餐饮", "购物", "交通", "娱乐", "学习", "其他")
private val INCOME_CATEGORIES = listOf("生活费", "兼职", "其他")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LocalFirstApp(db: AppDatabase, launchDone: Boolean = true) {
    val scope = rememberCoroutineScope()
    var tab by remember { mutableStateOf(Tab.RECORD) }
    var showAddRecord by remember { mutableStateOf(false) }
    // 背完一轮的完成卡：背了几个词；null = 不显示
    var roundDone by remember { mutableStateOf<Int?>(null) }
    var roundDoneShown by remember { mutableStateOf(0) }
    // 正在编辑的记录（null = 新发一条）
    var editingRecord by remember { mutableStateOf<RecordEntry?>(null) }
    // 每次打开发记录页 +1：退场动画还没放完又点开时，用新的一份草稿，不沿用上一次的
    var addSession by remember { mutableStateOf(0) }
    var message by remember { mutableStateOf("") }
    val haptic = LocalHapticFeedback.current
    val reduceMotion = rememberReduceMotion()
    fun selectTab(t: Tab) {
        if (t != tab) haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
        tab = t
    }
    fun openAdd(record: RecordEntry?) {
        editingRecord = record
        addSession += 1
        showAddRecord = true
    }
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val cardsDao = remember(db) { db.cards() }
    val recordsDao = remember(db) { db.records() }
    val unknownDao = remember(db) { db.unknownWords() }
    val cards by cardsDao.observeAll().collectAsState(initial = emptyList())
    val records by recordsDao.observeAll().collectAsState(initial = emptyList())
    // 第一次有了记录、还从没备份过：马上在后台备份一份，「文档/知行」里立刻就有东西
    val hasRecords = records.isNotEmpty()
    LaunchedEffect(hasRecords) {
        if (hasRecords && com.swenhao10.localfirst.data.AutoBackup.lastAt(context) == 0L) {
            com.swenhao10.localfirst.data.AutoBackup.schedule(context)
        }
    }
    val unknownWords by unknownDao.observeAll().collectAsState(initial = emptyList())
    var packWords by remember { mutableStateOf<List<PackWord>>(emptyList()) }
    var packMeta by remember { mutableStateOf<PackMeta?>(null) }
    var packReady by remember { mutableStateOf(false) }
    // 外观：从 SharedPreferences 读上次选的，重启还生效；跟随系统时读系统当前深浅
    var themeMode by remember { mutableStateOf(SettingsStore.getThemeMode(context)) }
    // 主题颜色：默认青绿（原来的主色），可换海蓝/紫罗兰/暖橙/樱粉
    var paletteId by remember { mutableStateOf(SettingsStore.getPalette(context)) }
    var customColor by remember { mutableStateOf(SettingsStore.getCustomColor(context)) }
    // 磨砂玻璃主题：默认开，可在「我的 - 外观」里关
    var glassOn by remember { mutableStateOf(SettingsStore.isGlassEnabled(context)) }
    // 自定义背景图：玻璃模式下铺在最底层
    var bgPath by remember { mutableStateOf(SettingsStore.getBackground(context)) }
    // 首页头像旁的名字：本地存一份，点头像可改
    var nickname by remember { mutableStateOf(SettingsStore.getNickname(context)) }
    // 头像图片：本机文件路径，空就走默认简笔头像
    var avatarPath by remember { mutableStateOf(SettingsStore.getAvatar(context)) }
    var showRename by remember { mutableStateOf(false) }
    var showProfileMenu by remember { mutableStateOf(false) }
    val avatarLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            scope.launch {
                val path = withContext(Dispatchers.IO) {
                    copyUriToInternal(context, uri, "avatar_${System.currentTimeMillis()}.jpg")
                }
                if (path != null) {
                    avatarPath?.let { old -> withContext(Dispatchers.IO) { File(old).delete() } }
                    avatarPath = path
                    SettingsStore.setAvatar(context, path)
                    message = "头像已经换好了"
                }
            }
        }
    }
    val bgLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            scope.launch {
                val path = withContext(Dispatchers.IO) {
                    copyUriToInternal(context, uri, "bg_${System.currentTimeMillis()}.jpg")
                }
                if (path != null) {
                    bgPath?.let { old -> withContext(Dispatchers.IO) { File(old).delete() } }
                    bgPath = path
                    SettingsStore.setBackground(context, path)
                    if (!glassOn) { glassOn = true; SettingsStore.setGlassEnabled(context, true) }
                    message = "背景已经换好了"
                }
            }
        }
    }
    // 背单词的学习进度：提到这里，切 Tab / 重进页面不再从头开始
    var bookId by remember { mutableStateOf(SettingsStore.getBookId(context)) }
    val study = remember { StudyProgress(SettingsStore.getStudyProgress(context), SettingsStore.getBookIndex(context, SettingsStore.getBookId(context))) }
    // 删除先生效，提示条上给「撤销」；提示条超时后才真的删文件（照片/视频）
    val showUndo: (String, suspend () -> Unit, suspend () -> Unit) -> Unit = { msg, undo, commit ->
        scope.launch {
            val r = snackbarHostState.showSnackbar(msg, actionLabel = "撤销", duration = SnackbarDuration.Short)
            runCatching { withContext(Dispatchers.IO) { if (r == SnackbarResult.ActionPerformed) undo() else commit() } }
        }
    }
    fun deleteRecordWithUndo(record: RecordEntry) {
        scope.launch {
            withContext(Dispatchers.IO) { recordsDao.delete(record.id) }
            showUndo(
                "这条已经删了",
                { recordsDao.insert(record) },
                { record.images().forEach { p -> runCatching { File(p).delete() } } }
            )
        }
    }
    val useDarkTheme = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        else -> isSystemInDarkTheme()
    }

    LaunchedEffect(db) {
        // 早期版本第一次打开会自动塞两张示例卡（abandon / focus），这里只清一次，用户自己加的不动
        withContext(Dispatchers.IO) {
            if (!SettingsStore.isSeedCardsCleaned(context)) {
                cardsDao.deleteExact("abandon", "v. 放弃；抛弃")
                cardsDao.deleteExact("focus", "v. 专注；集中")
                SettingsStore.setSeedCardsCleaned(context)
            }
        }
    }
    // 词书：用到哪本就从 assets 复制哪本到本机，再只读加载，离线可用；换书时重新加载
    LaunchedEffect(bookId) {
        packReady = false
        withContext(Dispatchers.IO) {
            packMeta = PackStore.loadMeta(context, bookId)
            packWords = PackStore.loadWords(context, bookId)
        }
        packReady = true
    }

    val baseScheme = paletteFor(paletteId, useDarkTheme, customColor)
    MaterialTheme(colorScheme = animateColorScheme(if (glassOn) glassScheme(baseScheme, useDarkTheme) else baseScheme)) {
        val layerBackdrop = rememberLayerBackdrop()
        val overlays = remember { mutableStateMapOf<Any, OverlayEntry>() }
        CompositionLocalProvider(
            LocalReduceMotion provides reduceMotion,
            LocalContentColor provides MaterialTheme.colorScheme.onBackground,
            LocalOverlay provides overlays,
            LocalGlass provides glassOn,
            LocalBackdrop provides (if (glassOn) layerBackdrop else null),
            LocalBgPath provides bgPath
        ) {
        Box(Modifier.fillMaxSize()) {
        if (glassOn) GlassBackground(useDarkTheme, Modifier.layerBackdrop(layerBackdrop))
        Scaffold(
            containerColor = if (glassOn) Color.Transparent else MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.onBackground,
            snackbarHost = { SnackbarHost(snackbarHostState) { data -> TipPill(data.visuals.message, data.visuals.actionLabel) { data.performAction() } } },
            bottomBar = {
                if (glassOn) {
                    GlassBottomBar(selected = tab, onSelect = { selectTab(it) }, onAdd = { openAdd(null) })
                } else NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                    Tab.values().forEach { item ->
                        NavigationBarItem(
                            selected = tab == item,
                            onClick = { selectTab(item) },
                            icon = { Icon(iconFor(item), contentDescription = item.label) },
                            label = { Text(item.label) },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = MaterialTheme.colorScheme.secondaryContainer,
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }
        ) { padding ->
            // Tab 保活：去过的 Tab 一直留在组合里（滚动位置、子页面、图片都不重建），切换只在 graphicsLayer 里做 ≤180ms 的透明度交叉淡化。
            // 看不见的 Tab 不画（drawWithContent 跳过，玻璃模糊也不算），也不接触摸。
            // 预组合其余 Tab：等启动动画完全放完、再空闲一会儿，才一帧一个地组合，不跟启动动画抢主线程。
            val visitedTabs = remember { mutableStateListOf(tab) }
            if (tab !in visitedTabs) visitedTabs.add(tab)
            LaunchedEffect(launchDone) {
                if (!launchDone) return@LaunchedEffect
                delay(700)
                Tab.values().forEach { t ->
                    if (t !in visitedTabs) {
                        withFrameNanos { }
                        visitedTabs.add(t)
                        delay(250)
                    }
                }
            }
            Box(Modifier.fillMaxSize()) {
            Tab.values().forEach { selected ->
                if (selected in visitedTabs) key(selected) {
                val active = selected == tab
                // 每次这个 Tab 变成可见就 +1：页面里的入场动画（收支条、分类条生长等）按它重新放一遍，
                // 跟 0.9.0 每次切进来都重放一样；在后台预组合时是 0，不会提前把动画放掉。
                val entry = remember { intArrayOf(0, 0) }   // [第几次进入, 上一帧是否可见]
                if (active && entry[1] == 0) entry[0]++
                entry[1] = if (active) 1 else 0
                val enterCount = entry[0]
                val tabAlpha = remember { Animatable(if (active) 1f else 0f) }
                LaunchedEffect(active) {
                    val target = if (active) 1f else 0f
                    if (reduceMotion) tabAlpha.snapTo(target)
                    else tabAlpha.animateTo(target, tween(if (active) 180 else 120, easing = Motion.EaseOut))
                }
                Box(
                    Modifier
                        .fillMaxSize()
                        .zIndex(if (active) 1f else 0f)
                        .graphicsLayer { alpha = tabAlpha.value }
                        .drawWithContent { if (tabAlpha.value > 0.003f) drawContent() }
                        // 当前 Tab 整块都算命中，触摸不会漏到下面看不见的 Tab
                        .then(if (active) Modifier.pointerInput(Unit) { awaitPointerEventScope { while (true) awaitPointerEvent() } } else Modifier)
                ) {
                CompositionLocalProvider(LocalTabActive provides active, LocalTabEnter provides enterCount) {
                when (selected) {
                    Tab.RECORD -> RecordScreen(
                        records = records,
                        nickname = nickname,
                        avatarPath = avatarPath,
                        onEditProfile = { avatarLauncher.launch("image/*") },
                        onAddRecord = { openAdd(null) },
                        onSearch = { message = "还没有记录" },
                        modifier = Modifier.padding(padding),
                        onEditRecord = { record -> openAdd(record) },
                        onDeleteRecord = { record -> deleteRecordWithUndo(record) }
                    )
                    Tab.WORDS -> WordsScreen(
                        packWords = packWords,
                        packReady = packReady,
                        bookId = bookId,
                        onChangeBook = { id ->
                            SettingsStore.setBookIndex(context, bookId, study.index)
                            SettingsStore.setBookId(context, id)
                            study.index = SettingsStore.getBookIndex(context, id)
                            bookId = id
                            message = "已换成「${PackStore.book(id).shortName}」"
                        },
                        unknownWords = unknownWords,
                        cards = cards,
                        db = db,
                        study = study,
                        modifier = Modifier.padding(padding),
                        onMessage = { message = it },
                        onUndoable = { msg, undo -> showUndo(msg, undo, {}) },
                        onRoundDone = { n -> roundDoneShown = n; roundDone = n }
                    )
                    Tab.OVERVIEW -> OverviewScreen(
                        records = records,
                        wordCount = unknownWords.size + cards.size,
                        modifier = Modifier.padding(padding),
                        onDeleteRecord = { record -> deleteRecordWithUndo(record) }
                    )
                    Tab.DATA -> DataScreen(
                        db,
                        Modifier.padding(padding),
                        themeMode,
                        onThemeChange = { mode ->
                            themeMode = mode
                            SettingsStore.setThemeMode(context, mode)
                        },
                        paletteId = paletteId,
                        onPaletteChange = { id ->
                            paletteId = id
                            SettingsStore.setPalette(context, id)
                        },
                        customColor = customColor,
                        darkNow = useDarkTheme,
                        onCustomColor = { c ->
                            customColor = c
                            SettingsStore.setCustomColor(context, c)
                            paletteId = PaletteId.CUSTOM
                            SettingsStore.setPalette(context, PaletteId.CUSTOM)
                        },
                        glassOn = glassOn,
                        onGlassChange = { on ->
                            glassOn = on
                            SettingsStore.setGlassEnabled(context, on)
                        },
                        hasCustomBg = bgPath != null,
                        onPickBg = { bgLauncher.launch("image/*") },
                        onResetBg = {
                            bgPath?.let { File(it).delete() }
                            bgPath = null
                            SettingsStore.setBackground(context, null)
                            message = "已恢复默认背景"
                        },
                        nickname = nickname,
                        avatarPath = avatarPath,
                        onRename = { showRename = true },
                        onChangeAvatar = { avatarLauncher.launch("image/*") }
                    ) { message = it }
                }
                }
                }
                }
            }
            }
        }
        LaunchedEffect(roundDone) {
            if (roundDone != null) {
                haptic.performHapticFeedback(HapticFeedbackType.Confirm)
                delay(2600)
                roundDone = null
            }
        }
        AnimatedVisibility(
            visible = roundDone != null,
            modifier = Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(bottom = 104.dp, start = 16.dp, end = 16.dp),
            enter = scaleIn(tween(Motion.STATE, easing = Motion.EaseOut), initialScale = .9f) + fadeIn(tween(Motion.STATE)),
            exit = fadeOut(tween(Motion.EXIT)) + scaleOut(tween(Motion.EXIT), targetScale = .96f)
        ) { RoundDoneCard(roundDoneShown) }
        LaunchedEffect(message) {
            if (message.isNotBlank()) {
                snackbarHostState.showSnackbar(message)
                message = ""
            }
        }
        // 发记录页：从底部升起（300ms），关闭时下滑退出（200ms）；退场途中内容保持不变
        AnimatedVisibility(
            visible = showAddRecord,
            enter = if (reduceMotion) fadeIn(tween(Motion.SMALL)) else slideInVertically(tween(Motion.SHEET, easing = Motion.EaseSheet)) { it } + fadeIn(tween(Motion.SMALL)),
            exit = if (reduceMotion) fadeOut(tween(Motion.EXIT)) else slideOutVertically(tween(Motion.STATE, easing = Motion.EaseOut)) { it } + fadeOut(tween(Motion.EXIT))
        ) {
          key(addSession) {
            val editing = editingRecord
            AddRecordScreen(
                editing = editing,
                active = showAddRecord,
                onCancel = {
                    showAddRecord = false
                },
                onPublish = { text, newUris, keptPaths, amountCents, category, moneyKind ->
                    scope.launch {
                        val addedPaths = newUris.mapIndexedNotNull { i, uri ->
                            // 图片和视频都复制一份到 App 里：相册里的原文件删了，记录里的还在
                            val prefix = if (isVideoUri(context, uri)) "vid" else "img"
                            copyUriToInternal(context, uri, "${prefix}_${System.currentTimeMillis()}_$i.${extensionFor(context, uri)}")
                        }
                        val finalPaths = (keptPaths + addedPaths).take(9)
                        // 编辑时被删掉的旧图，连文件一起清掉
                        val droppedPaths = editing?.images()?.filter { it !in keptPaths } ?: emptyList()
                        withContext(Dispatchers.IO) {
                            droppedPaths.forEach { p -> runCatching { File(p).delete() } }
                            if (editing == null) {
                                recordsDao.insert(
                                    RecordEntry(
                                        kind = if (amountCents != null) "expense" else "daily",
                                        text = text,
                                        imagePath = finalPaths.firstOrNull(),
                                        imagePaths = finalPaths.takeIf { it.size > 1 }?.joinToString("|"),
                                        amountCents = amountCents ?: 0L,
                                        category = category,
                                        moneyKind = moneyKind
                                    )
                                )
                            } else {
                                recordsDao.update(
                                    editing.copy(
                                        kind = if (amountCents != null) "expense" else "daily",
                                        text = text,
                                        imagePath = finalPaths.firstOrNull(),
                                        imagePaths = finalPaths.takeIf { it.size > 1 }?.joinToString("|"),
                                        amountCents = amountCents ?: 0L,
                                        category = category,
                                        moneyKind = moneyKind
                                    )
                                )
                            }
                        }
                        showAddRecord = false
                        haptic.performHapticFeedback(HapticFeedbackType.Confirm)
                        message = if (editing == null) "已保存到本机" else "这条已经改好了"
                    }
                }
            )
          }
        }
        if (showProfileMenu) {
            ZhAlertDialog(
                onDismissRequest = { showProfileMenu = false },
                title = { Text("头像和昵称") },
                text = { Text("头像只用本机图片，不上传。") },
                confirmButton = {
                    TextButton(onClick = {
                        showProfileMenu = false
                        avatarLauncher.launch("image/*")
                    }) { Text("换头像") }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showProfileMenu = false
                        showRename = true
                    }) { Text("改昵称") }
                }
            )
        }
        if (showRename) {
            RenameDialog(
                current = nickname,
                onCancel = { showRename = false },
                onSave = { name ->
                    nickname = name
                    SettingsStore.setNickname(context, name)
                    showRename = false
                }
            )
        }
        // 弹窗都画在这一层：跟页面在同一个窗口里，玻璃才能透出背景
        overlays.entries.toList().forEach { (k, e) ->
            key(k) {
                e.content(e.visible)
                // 退场动画放完再从宿主里拿掉
                val gone = !e.visible.targetState && !e.visible.currentState && e.visible.isIdle
                if (gone) SideEffect { overlays.remove(k) }
            }
        }
        }
        }
    }
}

/** 列表项进出和让位：新行 200ms 淡入，删掉的行 150ms 淡出，下面的行用弹簧补位（不回弹）。减弱动画时只淡入淡出。 */
private fun LazyItemScope.rowMotion(reduce: Boolean): Modifier =
    if (reduce) Modifier.animateItem(fadeInSpec = tween(Motion.SMALL), placementSpec = null, fadeOutSpec = tween(Motion.SMALL))
    else Modifier.animateItem(
        fadeInSpec = tween(Motion.STATE, easing = Motion.EaseOut),
        placementSpec = spring(dampingRatio = 1f, stiffness = Spring.StiffnessMediumLow, visibilityThreshold = IntOffset.VisibilityThreshold),
        fadeOutSpec = tween(Motion.EXIT)
    )

private fun iconFor(tab: Tab): ImageVector = when (tab) {
    Tab.RECORD -> Icons.Default.Home
    Tab.WORDS -> Icons.Default.MenuBook
    Tab.OVERVIEW -> Icons.Default.CalendarMonth
    Tab.DATA -> Icons.Default.Folder
}

/** 记录（首页）：头像昵称头 + 时间轴，按天分组，最新在前。 */
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun RecordScreen(
    records: List<RecordEntry>,
    nickname: String,
    avatarPath: String?,
    onEditProfile: () -> Unit,
    onAddRecord: () -> Unit,
    onSearch: () -> Unit,
    modifier: Modifier,
    onEditRecord: (RecordEntry) -> Unit,
    onDeleteRecord: (RecordEntry) -> Unit
) {
    var pendingAction by remember { mutableStateOf<RecordEntry?>(null) }
    val saveRecordMedia = rememberGallerySaver()
    // 点图片看大图：当前这条的全部图片 + 点的是第几张
    var viewer by remember { mutableStateOf<Pair<List<String>, Int>?>(null) }
    val dayCount = remember(records) { records.map { dayKey(it.createdAt) }.distinct().size }
    val reduce = LocalReduceMotion.current
    val avatarBitmap = rememberBitmap(avatarPath, 256)
    // 放大镜 = 跳到某一天：日历里只有记过东西的日子能点
    val listState = androidx.compose.foundation.lazy.rememberLazyListState()
    val listScope = rememberCoroutineScope()
    var showDatePick by remember { mutableStateOf(false) }
    var flash by remember { mutableStateOf<Pair<String, Int>?>(null) }
    val recordDays = remember(records) { records.map { dayKey(it.createdAt) }.toSet() }
    if (showDatePick) {
        val utcFmt = remember { SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).apply { timeZone = java.util.TimeZone.getTimeZone("UTC") } }
        val pickState = androidx.compose.material3.rememberDatePickerState(
            initialSelectedDateMillis = records.firstOrNull()?.let { utcFmt.parse(dayKey(it.createdAt))?.time },
            selectableDates = object : androidx.compose.material3.SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long) = utcFmt.format(Date(utcTimeMillis)) in recordDays
            }
        )
        // 跟其它弹层同一个玻璃面板；面板里的日历自己有分页/年份列表，所以不再套滚动（scrollText = false）
        val glassOn = LocalBackdrop.current != null
        val darkBg = MaterialTheme.colorScheme.background.red < 0.5f
        val fg = if (darkBg) Color.White else MaterialTheme.colorScheme.onSurface
        val primary = MaterialTheme.colorScheme.primary
        val onPrimary = MaterialTheme.colorScheme.onPrimary
        val pickerColors = androidx.compose.material3.DatePickerDefaults.colors(
            containerColor = Color.Transparent,
            titleContentColor = fg.copy(alpha = .75f),
            headlineContentColor = fg,
            weekdayContentColor = fg.copy(alpha = .75f),
            subheadContentColor = fg.copy(alpha = .75f),
            navigationContentColor = fg,
            yearContentColor = fg,
            disabledYearContentColor = fg.copy(alpha = .3f),
            currentYearContentColor = primary,
            selectedYearContentColor = onPrimary,
            selectedYearContainerColor = primary,
            dayContentColor = fg,
            disabledDayContentColor = fg.copy(alpha = if (glassOn || darkBg) .28f else .32f),
            selectedDayContentColor = onPrimary,
            disabledSelectedDayContentColor = onPrimary.copy(alpha = .5f),
            selectedDayContainerColor = primary,
            disabledSelectedDayContainerColor = primary.copy(alpha = .3f),
            todayContentColor = primary,
            todayDateBorderColor = primary,
            dividerColor = fg.copy(alpha = .12f)
        )
        ZhAlertDialog(
            onDismissRequest = { showDatePick = false },
            title = { Text("跳到哪一天", modifier = Modifier.padding(start = 16.dp)) },
            scrollText = false,
            horizontalPadding = 6.dp,
            confirmButton = {
                Button(
                    onClick = {
                    showDatePick = false
                    val picked = pickState.selectedDateMillis?.let { utcFmt.format(Date(it)) } ?: return@Button
                    // 算出那一天的标题在列表里是第几项：header 占 1，之后每天 = 1 个标题 + 当天条数
                    var idx = 1
                    var target = -1
                    var targetKey: String? = null
                    for ((key, list) in records.groupBy { dayKey(it.createdAt) }) {
                        if (key <= picked) { target = idx; targetKey = key; break }
                        idx += 1 + list.size
                    }
                    if (target < 0) target = (idx - 1).coerceAtLeast(0)
                    listScope.launch {
                        // 远距离先瞬移到附近，再平滑滚最后一小段，不让人看一长串飞过去
                        val from = listState.firstVisibleItemIndex
                        if (kotlin.math.abs(target - from) > 12) listState.scrollToItem(if (target > from) target - 4 else target + 4)
                        listState.animateScrollToItem(target)
                        // 落地后那一天的标题闪一下，告诉你到了
                        targetKey?.let { flash = it to (flash?.second ?: 0) + 1 }
                    }
                    },
                    enabled = pickState.selectedDateMillis != null,
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = primary, contentColor = onPrimary),
                    modifier = Modifier.padding(end = 10.dp)
                ) { Text("跳过去", fontWeight = FontWeight.SemiBold) }
            },
            dismissButton = { TextButton(onClick = { showDatePick = false }) { Text("取消", color = fg.copy(alpha = .8f)) } }
        ) {
            androidx.compose.material3.DatePicker(
                state = pickState,
                colors = pickerColors,
                title = null,
                showModeToggle = false
            )
        }
    }
    LazyColumn(modifier.fillMaxSize().padding(horizontal = 16.dp), state = listState) {
        item(key = "header") {
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                val avatarSrc = remember { MutableInteractionSource() }
                Box(
                    modifier = Modifier.size(52.dp).pressScale(avatarSrc, haptic = true).clip(CircleShape)
                        .clickable(interactionSource = avatarSrc, indication = androidx.compose.foundation.LocalIndication.current, onClick = onEditProfile)
                ) {
                    if (avatarBitmap != null) {
                        Image(
                            bitmap = avatarBitmap,
                            contentDescription = "头像",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        AvatarFace(name = nickname, modifier = Modifier.fillMaxSize())
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(nickname, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, maxLines = 1)
                    Row {
                        val st = MaterialTheme.typography.labelMedium
                        val c = MaterialTheme.colorScheme.onSurfaceVariant
                        Text("已记录 ", style = st, color = c)
                        RollingText("${records.size}", st, c)
                        Text(" 条 · 第 ", style = st, color = c)
                        RollingText("$dayCount", st, c)
                        Text(" 天", style = st, color = c)
                    }
                }
                if (LocalBackdrop.current != null) {
                    GlassButton(onClick = { if (records.isEmpty()) onSearch() else showDatePick = true }, modifier = Modifier.size(46.dp)) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = "跳到日期", tint = MaterialTheme.colorScheme.onSurface)
                    }
                } else IconButton(
                    onClick = { if (records.isEmpty()) onSearch() else showDatePick = true },
                    modifier = Modifier.size(42.dp).background(MaterialTheme.colorScheme.surface, CircleShape)
                ) { Icon(Icons.Default.CalendarMonth, contentDescription = "跳到日期", tint = MaterialTheme.colorScheme.onSurface) }
                if (!LocalGlass.current) {
                    Spacer(Modifier.width(10.dp))
                    Surface(onClick = onAddRecord, shape = CircleShape, color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(46.dp)) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Add, contentDescription = "发记录", tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
                        }
                    }
                }
            }
            Spacer(Modifier.height(14.dp))
        }
        if (records.isEmpty()) {
            item(key = "empty") { Box(rowMotion(reduce)) { EmptyRecordState(onAddRecord = onAddRecord) } }
        } else {
            // 按天分组，records 已按时间倒序，groupBy 保持最新一天在前
            val groups = records.groupBy { dayKey(it.createdAt) }
            groups.forEach { (key, dayRecords) ->
                item(key = "day-$key") { Box(rowMotion(reduce)) { DayHeader(time = dayRecords.first().createdAt, flashNonce = flash?.takeIf { it.first == key }?.second ?: 0) } }
                items(dayRecords, key = { "rec-${it.id}" }) { record ->
                    Box(rowMotion(reduce)) {
                        TimelineRecord(
                            record = record,
                            onClick = { if (record.kind != "study") onEditRecord(record) },
                            onLongPress = { pendingAction = record },
                            onImageClick = { i -> viewer = record.images() to i }
                        )
                    }
                }
            }
        }
        item(key = "tail") { Spacer(Modifier.height(24.dp)) }
    }
    viewer?.let { (paths, start) ->
        ImageViewer(paths = paths, startIndex = start, onDismiss = { viewer = null })
    }
    pendingAction?.let { target ->
        ZhAlertDialog(
            onDismissRequest = { pendingAction = null },
            title = { Text("这条记录") },
            text = { Text(if (target.kind == "study") "背单词自动记的。删了之后几秒内可以撤销。" else "点一下这条记录可以直接改内容。删了之后几秒内可以撤销。") },
            confirmButton = {
                TextButton(onClick = { onDeleteRecord(target); pendingAction = null }) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                Row {
                    if (target.images().isNotEmpty()) {
                        TextButton(onClick = { saveRecordMedia(target.images()); pendingAction = null }) { Text("存到相册") }
                    }
                    TextButton(onClick = { pendingAction = null }) { Text("取消") }
                }
            }
        )
    }
}

/** 头像：圆形，画一个简笔笑脸，颜色跟着主题走。 */
@Composable
private fun AvatarFace(name: String, modifier: Modifier) {
    val face = MaterialTheme.colorScheme.primaryContainer
    val ink = MaterialTheme.colorScheme.onPrimaryContainer
    Canvas(modifier) {
        drawCircle(color = face)
        val w = size.width
        val h = size.height
        drawCircle(color = ink, radius = w * 0.055f, center = Offset(w * 0.33f, h * 0.4f))
        drawCircle(color = ink, radius = w * 0.055f, center = Offset(w * 0.67f, h * 0.4f))
        drawArc(
            color = ink,
            startAngle = 25f,
            sweepAngle = 130f,
            useCenter = false,
            topLeft = Offset(w * 0.3f, h * 0.45f),
            size = androidx.compose.ui.geometry.Size(w * 0.4f, h * 0.32f),
            style = Stroke(width = w * 0.05f)
        )
    }
}

/** 改昵称：点头像弹出来。 */
@Composable
private fun RenameDialog(current: String, onCancel: () -> Unit, onSave: (String) -> Unit) {
    var value by remember { mutableStateOf(current) }
    ZhAlertDialog(
        onDismissRequest = onCancel,
        title = { Text("改个名字") },
        text = {
            OutlinedTextField(
                value = value,
                onValueChange = { value = it.take(12) },
                singleLine = true,
                label = { Text("昵称") },
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(onClick = { if (value.isNotBlank()) onSave(value.trim()) }, enabled = value.isNotBlank()) { Text("保存") }
        },
        dismissButton = { TextButton(onClick = onCancel) { Text("取消") } }
    )
}

/** 天组头：今天 / 昨天 / M月d日 + 灰色日期星期。 */
@Composable
private fun DayHeader(time: Long, flashNonce: Int = 0) {
    // 跳到这天后背景闪一次（0.6 → 0，600ms），只闪一次，不持续脉动
    val glow = remember { Animatable(0f) }
    LaunchedEffect(flashNonce) {
        if (flashNonce > 0) { glow.snapTo(0.6f); glow.animateTo(0f, tween(600, easing = Motion.EaseOut)) }
    }
    val glowColor = MaterialTheme.colorScheme.primaryContainer
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
            .drawBehind { if (glow.value > 0f) drawRoundRect(glowColor.copy(alpha = glow.value), cornerRadius = CornerRadius(12.dp.toPx())) }
            .padding(top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        Text(dayTitle(time), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.width(10.dp))
        Text(
            daySubtitle(time),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 4.dp)
        )
    }
}

/** 发记录页里的缩略图格子：右上角 × 去掉这张。 */
@Composable
private fun ThumbCell(bitmap: androidx.compose.ui.graphics.ImageBitmap, isVideo: Boolean = false, onRemove: () -> Unit) {
    Box(Modifier.size(84.dp)) {
        Image(
            bitmap = bitmap,
            contentDescription = if (isVideo) "已选视频" else "已选图片",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp))
        )
        if (isVideo) PlayBadge(small = true)
        // 看起来是 20dp 的小圆钮，能点的范围是右上角 48dp
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(48.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onRemove
                ),
            contentAlignment = Alignment.TopEnd
        ) {
            Box(
                Modifier.padding(4.dp).size(20.dp).background(MaterialTheme.colorScheme.surface, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Close, contentDescription = "去掉这张图", modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}

/**
 * 时间轴上的图片：一张铺满大图，多张每行三张方图，全部圆角。
 */
@Composable
private fun RecordImages(paths: List<String>, modifier: Modifier = Modifier, onImageClick: ((Int) -> Unit)? = null) {
    if (paths.isEmpty()) return
    if (paths.size == 1) {
        val path = paths.first()
        val video = isVideoPath(path)
        val bitmap = rememberBitmap(path, 720)
        val exists = remember(path) { java.io.File(path).exists() }
        if (exists) {
            Box(
                modifier = modifier
                    .fillMaxWidth(0.7f)
                    .aspectRatio(3f / 2f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .then(if (onImageClick != null) Modifier.clickable { onImageClick(0) } else Modifier)
            ) {
                // 后台解码完再淡入（200ms）；缓存里有的直接显示
                val imgAlpha by animateFloatAsState(if (bitmap != null) 1f else 0f, tween(Motion.STATE), label = "imgIn")
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap,
                        contentDescription = if (video) "记录视频" else "记录图片",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize().graphicsLayer { alpha = imgAlpha }
                    )
                }
                if (video) {
                    PlayBadge()
                    val dur by androidx.compose.runtime.produceState<Long?>(null, path) { value = withContext(Dispatchers.IO) { videoDurationMs(path) } }
                    dur?.let { DurationTag(it, Modifier.align(Alignment.BottomEnd)) }
                }
            }
        }
        return
    }
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        paths.chunked(3).forEachIndexed { rowIndex, row ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                row.forEachIndexed { colIndex, path ->
                    val bitmap = rememberBitmap(path, 360)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .then(if (onImageClick != null) Modifier.clickable { onImageClick(rowIndex * 3 + colIndex) } else Modifier)
                    ) {
                        val imgAlpha by animateFloatAsState(if (bitmap != null) 1f else 0f, tween(Motion.STATE), label = "imgIn")
                        if (bitmap != null) {
                            Image(
                                bitmap = bitmap,
                                contentDescription = "记录图片",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize().graphicsLayer { alpha = imgAlpha }
                            )
                        }
                        if (isVideoPath(path)) PlayBadge(small = true)
                    }
                }
                repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

/** 一条记录：左时间 + 竖线圆点 + 右侧内容（不套卡片，记账除外）。 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TimelineRecord(record: RecordEntry, onClick: () -> Unit, onLongPress: () -> Unit, onImageClick: (Int) -> Unit = {}) {
    val images = record.images()
    val dotColor = recordDotColor(record)
    val lineColor = MaterialTheme.colorScheme.outlineVariant
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .combinedClickable(onClick = onClick, onLongClick = onLongPress)
            .padding(bottom = 6.dp)
    ) {
        Text(
            timeText(record.createdAt),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(44.dp).padding(top = 4.dp)
        )
        Box(
            modifier = Modifier
                .width(20.dp)
                .fillMaxHeight()
                .drawBehind {
                    val cx = size.width / 2
                    val cy = 8.dp.toPx()
                    drawCircle(color = dotColor, radius = 4.5.dp.toPx(), center = Offset(cx, cy))
                    if (size.height > cy + 8.dp.toPx()) {
                        drawLine(
                            color = lineColor,
                            start = Offset(cx, cy + 7.dp.toPx()),
                            end = Offset(cx, size.height),
                            strokeWidth = 1.2.dp.toPx()
                        )
                    }
                }
        )
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f).padding(bottom = 10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            when (record.kind) {
                "expense" -> ExpenseLine(record, onImageClick)
                "study" -> StudyLine(record)
                else -> {
                    if (record.text.isNotBlank()) {
                        Text(record.text, style = MaterialTheme.typography.bodyLarge, lineHeight = MaterialTheme.typography.bodyLarge.fontSize * 1.45f)
                    }
                    RecordImages(images, onImageClick = onImageClick)
                }
            }
        }
    }
}

/** 看大图：全屏黑底，左右滑切换，双指缩放，双击放大/还原，单击或返回键关闭。 */
@Composable
private fun ImageViewer(paths: List<String>, startIndex: Int, onDismiss: () -> Unit) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        val pager = rememberPagerState(initialPage = startIndex.coerceIn(0, (paths.size - 1).coerceAtLeast(0))) { paths.size }
        Box(Modifier.fillMaxSize().background(Color.Black)) {
            HorizontalPager(state = pager, modifier = Modifier.fillMaxSize()) { page ->
                if (isVideoPath(paths[page])) {
                    VideoPage(path = paths[page], isCurrent = pager.currentPage == page)
                } else {
                    ZoomableImage(path = paths[page], onTap = onDismiss)
                }
            }
            if (paths.size > 1) {
                Text(
                    "${pager.currentPage + 1} / ${paths.size}",
                    color = Color.White,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(bottom = 24.dp)
                )
            }
            val saveToAlbum = rememberGallerySaver()
            Row(
                modifier = Modifier.align(Alignment.TopEnd).statusBarsPadding().padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { paths.getOrNull(pager.currentPage)?.let { saveToAlbum(listOf(it)) } }) {
                    Icon(Icons.Default.Download, contentDescription = "存到相册", tint = Color.White)
                }
                IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, contentDescription = "关闭", tint = Color.White) }
            }
        }
    }
}

@Composable
private fun ZoomableImage(path: String, onTap: () -> Unit) {
    // 大图用 2048 采样，比时间轴缩略图清楚
    val bitmap = rememberBitmap(path, 2048)
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    Box(
        Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onTap() },
                    onDoubleTap = {
                        if (scale > 1f) { scale = 1f; offset = Offset.Zero } else scale = 2.5f
                    }
                )
            }
            .pointerInput(Unit) {
                // 双指缩放随时可用；单指拖动只在放大后接管，没放大时不消费事件，左右滑留给翻页
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false)
                    do {
                        val event = awaitPointerEvent()
                        val pressed = event.changes.count { it.pressed }
                        if (pressed >= 2 || scale > 1f) {
                            val zoom = event.calculateZoom()
                            val pan = event.calculatePan()
                            scale = (scale * zoom).coerceIn(1f, 5f)
                            offset = if (scale <= 1f) Offset.Zero else offset + pan
                            event.changes.forEach { if (it.positionChanged()) it.consume() }
                        }
                    } while (event.changes.any { it.pressed })
                }
            },
        contentAlignment = Alignment.Center
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap,
                contentDescription = "大图",
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize().graphicsLayer {
                    scaleX = scale; scaleY = scale
                    translationX = offset.x; translationY = offset.y
                }
            )
        } else {
            Text("这张图找不到了", color = Color.White)
        }
    }
}

/** 视频缩略图中间的播放标。 */
@Composable
private fun PlayBadge(small: Boolean = false) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Box(
            Modifier
                .size(if (small) 28.dp else 44.dp)
                .background(Color.Black.copy(alpha = 0.45f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.PlayArrow, contentDescription = "视频", tint = Color.White, modifier = Modifier.size(if (small) 18.dp else 28.dp))
        }
    }
}

@Composable
private fun DurationTag(ms: Long, modifier: Modifier = Modifier) {
    Text(
        formatDuration(ms),
        color = Color.White,
        style = MaterialTheme.typography.labelSmall,
        modifier = modifier
            .padding(8.dp)
            .background(Color.Black.copy(alpha = 0.45f), RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    )
}

/**
 * 看视频：系统 VideoView 播放（不加第三方库）。翻到这一页自动播放，翻走暂停；
 * 点一下暂停/继续，底部进度条可拖。
 */
@Composable
private fun VideoPage(path: String, isCurrent: Boolean) {
    var view by remember { mutableStateOf<VideoView?>(null) }
    var playing by remember { mutableStateOf(false) }
    var prepared by remember { mutableStateOf(false) }
    var failed by remember { mutableStateOf(false) }
    var position by remember { mutableStateOf(0) }
    var duration by remember { mutableStateOf(0) }
    val exists = remember(path) { File(path).exists() }
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        if (!exists || failed) {
            Text("这个视频找不到了或放不了", color = Color.White)
            return@Box
        }
        AndroidView(
            factory = { ctx ->
                VideoView(ctx).apply {
                    setOnPreparedListener { mp ->
                        prepared = true
                        duration = mp.duration
                        mp.isLooping = false
                    }
                    setOnCompletionListener { playing = false; position = duration }
                    setOnErrorListener { _, _, _ -> failed = true; true }
                    setVideoPath(path)
                    view = this
                }
            },
            onRelease = { it.stopPlayback() },
            modifier = Modifier.fillMaxWidth()
        )
        // 透明点击层：点一下播放/暂停；左右滑不拦，留给翻页
        Box(
            Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(onTap = {
                        val v = view ?: return@detectTapGestures
                        if (v.isPlaying) { v.pause(); playing = false } else { v.start(); playing = true }
                    })
                }
        )
        if (!playing) {
            Box(
                Modifier.size(64.dp).background(Color.Black.copy(alpha = 0.45f), CircleShape),
                contentAlignment = Alignment.Center
            ) { Icon(Icons.Default.PlayArrow, contentDescription = "播放", tint = Color.White, modifier = Modifier.size(40.dp)) }
        }
        if (prepared && duration > 0) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(start = 16.dp, end = 16.dp, bottom = 56.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(formatDuration(position.toLong()), color = Color.White, style = MaterialTheme.typography.labelMedium)
                Slider(
                    value = position.toFloat().coerceIn(0f, duration.toFloat()),
                    onValueChange = { v -> position = v.toInt(); view?.seekTo(v.toInt()) },
                    valueRange = 0f..duration.toFloat(),
                    modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
                )
                Text(formatDuration(duration.toLong()), color = Color.White, style = MaterialTheme.typography.labelMedium)
            }
        }
    }
    LaunchedEffect(isCurrent, prepared) {
        val v = view ?: return@LaunchedEffect
        if (isCurrent && prepared) { v.start(); playing = true } else if (!isCurrent) { v.pause(); playing = false }
    }
    LaunchedEffect(playing) {
        while (playing) {
            view?.let { position = it.currentPosition }
            delay(250)
        }
    }
}

/**
 * 存到相册：传入本机路径，图片进 Pictures/知行，视频进 Movies/知行。
 * Android 9 及以下先要存储权限；结果用 Toast 提示（看大图是全屏 Dialog，Snackbar 看不到）。
 */
@Composable
private fun rememberGallerySaver(): (List<String>) -> Unit {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var pending by remember { mutableStateOf<List<String>>(emptyList()) }
    val doSave: (List<String>) -> Unit = { paths ->
        if (paths.isEmpty()) {
            Toast.makeText(context, "还没有照片或视频", Toast.LENGTH_SHORT).show()
        } else {
            scope.launch {
                val ok = withContext(Dispatchers.IO) { paths.count { saveToGallery(context, it) } }
                val msg = when {
                    ok == 0 -> "没存成，文件可能已经不在了"
                    ok == paths.size && ok == 1 -> "已存到相册「知行」"
                    ok == paths.size -> "已把 $ok 个照片和视频存到相册「知行」"
                    else -> "存好 $ok 个，${paths.size - ok} 个没存成"
                }
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            }
        }
    }
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        val p = pending
        pending = emptyList()
        if (granted) doSave(p) else Toast.makeText(context, "没有存储权限，存不了相册", Toast.LENGTH_SHORT).show()
    }
    return { paths ->
        if (galleryNeedsPermission() &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
        ) {
            pending = paths
            permission.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        } else doSave(paths)
    }
}

/**
 * 记账：
 * - 带图：不套框。先文字，再图片，图片下面一条玻璃胶囊放「分类 + 金额」，不挡图。
 * - 不带图：一条玻璃小卡，左「备注 · 分类」，右大字号金额。
 */
@Composable
private fun ExpenseLine(record: RecordEntry, onImageClick: (Int) -> Unit = {}) {
    val isIncome = record.moneyKind == "income"
    val amountText = (if (isIncome) "+" else "-") + formatMoney(record.amountCents ?: 0L)
    val amountColor = if (isIncome) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
    val images = record.images()
    if (images.isNotEmpty()) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (record.text.isNotBlank()) {
                Text(record.text, style = MaterialTheme.typography.bodyLarge, lineHeight = MaterialTheme.typography.bodyLarge.fontSize * 1.45f)
            }
            RecordImages(images, onImageClick = onImageClick)
            Row(
                Modifier.fillMaxWidth().glass(RoundedCornerShape(50)).padding(start = 8.dp, end = 16.dp, top = 6.dp, bottom = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val cat = record.category?.takeIf { it.isNotBlank() } ?: if (isIncome) "收入" else "其他"
                Surface(color = amountColor.copy(alpha = 0.18f), shape = RoundedCornerShape(50)) {
                    Text(cat, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, color = amountColor, modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp))
                }
                Spacer(Modifier.weight(1f))
                Text(amountText, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = amountColor)
            }
        }
        return
    }
    Row(
        modifier = Modifier.fillMaxWidth().glass(RoundedCornerShape(16.dp)).padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            listOf(record.text, record.category).filter { it.isNullOrBlank().not() }.joinToString(" · "),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            maxLines = 2,
            modifier = Modifier.weight(1f)
        )
        Spacer(Modifier.width(10.dp))
        Text(amountText, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = amountColor)
    }
}

/** 单词：一行「背了 N 个词 · 认识 N」。 */
@Composable
private fun StudyLine(record: RecordEntry) {
    Text(
        record.text.ifBlank { "背了 0 个词" },
        style = MaterialTheme.typography.bodyLarge,
        fontWeight = FontWeight.SemiBold
    )
}

@Composable
private fun EmptyRecordState(onAddRecord: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 60.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.size(64.dp)) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Add, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
            }
        }
        Spacer(Modifier.height(14.dp))
        Text("还没有记录", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(4.dp))
        Text("点右上角的 + 发第一条：文字、图片、记账都行", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(16.dp))
        Button(onClick = onAddRecord) { Text("发第一条") }
    }
}

/** 圆点颜色按类型区分，颜色全部取自主题色板。说说=主色，图片=主色淡，记账=辅色，单词=第三色。 */
@Composable
private fun recordDotColor(record: RecordEntry): Color = when {
    record.kind == "expense" -> MaterialTheme.colorScheme.secondary
    record.kind == "study" -> MaterialTheme.colorScheme.tertiary
    record.images().isNotEmpty() -> MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
    else -> MaterialTheme.colorScheme.primary
}


/** 一条记录：文字 / 图片 / 金额（记账）按有无显示。 */
@Composable
private fun RecordItemCard(record: RecordEntry, onDelete: () -> Unit, showDate: Boolean = false) {
    GlassCard(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(18.dp)) {
        Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                val kindLabel = when (record.kind) {
                    "expense" -> if (record.moneyKind == "income") "收入" else "记账"
                    "study" -> "背单词"
                    else -> "日常"
                }
                Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(10.dp)) {
                    Text(kindLabel, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                }
                if (showDate) {
                    Spacer(Modifier.width(8.dp))
                    Text(dayHeader(record.createdAt), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(Modifier.weight(1f))
                Text(timeText(record.createdAt), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "删除", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                }
            }
            if (record.text.isNotBlank()) Text(record.text)
            RecordImages(record.images())
            if (record.kind == "expense" && record.amountCents != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(record.category ?: "", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.weight(1f))
                    val isIncome = record.moneyKind == "income"
                    Text(
                        (if (isIncome) "+" else "-") + formatMoney(record.amountCents),
                        fontWeight = FontWeight.Bold,
                        color = if (isIncome) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
    }
}

/** 发记录：全屏页，三张白卡（文字+多图 / 记账 / 单词提示），底部相册与拍照。 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AddRecordScreen(
    editing: RecordEntry? = null,
    active: Boolean = true,
    onCancel: () -> Unit,
    onPublish: (text: String, newUris: List<Uri>, keptPaths: List<String>, amountCents: Long?, category: String?, moneyKind: String?) -> Unit
) {
    var text by remember { mutableStateOf(editing?.text ?: "") }
    // 新的图（相册/拍照选的）和编辑时留下的旧图分开存，发布时再合并
    val newUris = remember { mutableStateListOf<Uri>() }
    val keptPaths = remember { mutableStateListOf<String>().apply { addAll(editing?.images() ?: emptyList()) } }
    var pendingPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var recordMoney by remember { mutableStateOf(editing?.amountCents?.let { it > 0 } ?: false) }
    var amount by remember {
        mutableStateOf(
            editing?.amountCents?.takeIf { it > 0 }?.let { "%.2f".format(Locale.CHINA, it / 100.0) } ?: ""
        )
    }
    var moneyKind by remember { mutableStateOf(editing?.moneyKind ?: "expense") }
    var category by remember {
        mutableStateOf(
            editing?.category?.takeIf { it.isNotBlank() }
                ?: (if (editing?.moneyKind == "income") INCOME_CATEGORIES[0] else EXPENSE_CATEGORIES[0])
        )
    }
    var nowText by remember { mutableStateOf(timeText(System.currentTimeMillis())) }
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val categories = if (moneyKind == "income") INCOME_CATEGORIES else EXPENSE_CATEGORIES
    val totalImages = newUris.size + keptPaths.size
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickMultipleVisualMedia(9)) { uris ->
        val room = (9 - newUris.size - keptPaths.size).coerceAtLeast(0)
        if (uris.isNotEmpty() && room > 0) {
            uris.map { it.toString() }.distinct().forEach { s ->
                if (newUris.size < room && newUris.none { it.toString() == s }) newUris.add(Uri.parse(s))
            }
        }
    }
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { ok ->
        val uri = pendingPhotoUri
        if (ok && uri != null && newUris.size + keptPaths.size < 9) newUris.add(uri)
    }
    var wantVideo by remember { mutableStateOf(false) }
    var pendingVideoUri by remember { mutableStateOf<Uri?>(null) }
    val videoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CaptureVideo()) { ok ->
        val uri = pendingVideoUri
        if (ok && uri != null && newUris.size + keptPaths.size < 9) newUris.add(uri)
    }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            if (wantVideo) {
                val uri = photoUri(context, createVideoFile(context))
                pendingVideoUri = uri
                videoLauncher.launch(uri)
            } else {
                val file = createPhotoFile(context)
                val uri = photoUri(context, file)
                pendingPhotoUri = uri
                cameraLauncher.launch(uri)
            }
        }
    }
    val amountCents = amount.toDoubleOrNull()?.let { (it * 100).toLong() }?.takeIf { it > 0 }
    val canPublish = text.isNotBlank() || totalImages > 0 || (recordMoney && amountCents != null)
    // 草稿有没有改动：新发的看有没有填东西；编辑的看和原来比有没有变
    val origImages = remember { editing?.images() ?: emptyList() }
    val origAmount = remember { editing?.amountCents?.takeIf { it > 0 }?.let { "%.2f".format(Locale.CHINA, it / 100.0) } ?: "" }
    val dirty = if (editing == null) {
        text.isNotBlank() || totalImages > 0 || amount.isNotBlank()
    } else {
        text != editing.text || newUris.isNotEmpty() || keptPaths.toList() != origImages ||
            recordMoney != ((editing.amountCents ?: 0L) > 0L) || amount != origAmount ||
            (recordMoney && (moneyKind != (editing.moneyKind ?: "expense") || category != (editing.category ?: category)))
    }
    var showDiscard by remember { mutableStateOf(false) }
    val requestClose: () -> Unit = { if (dirty) showDiscard = true else onCancel() }
    // 系统返回：有没发的内容先问一句，没有就直接关
    BackHandler(enabled = active) { requestClose() }
    // 点了发布：复制图片/视频期间按钮里转圈，避免以为没反应又点一次
    var publishing by remember { mutableStateOf(false) }
    val publish: () -> Unit = {
        if (!publishing) {
            publishing = true
            onPublish(
                text.trim(), newUris.toList(), keptPaths.toList(),
                if (recordMoney) amountCents else null,
                if (recordMoney) category else null,
                if (recordMoney) moneyKind else null
            )
        }
    }
    val publishLabel: @Composable (Color) -> Unit = { c ->
        AnimatedContent(publishing, transitionSpec = { fadeIn(tween(Motion.SMALL)) togetherWith fadeOut(tween(90)) using SizeTransform(clip = false) }, label = "publish") { busy ->
            if (busy) androidx.compose.material3.CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = if (c == Color.Unspecified) androidx.compose.material3.LocalContentColor.current else c)
            else Text(if (editing == null) "发布" else "保存", fontWeight = FontWeight.SemiBold, color = c)
        }
    }
    if (showDiscard) {
        ZhAlertDialog(
            onDismissRequest = { showDiscard = false },
            title = { Text(if (editing == null) "不发了？" else "不保存修改？") },
            text = { Text(if (editing == null) "刚写的内容和选的图会丢掉。" else "这次的修改会丢掉，原来的记录不变。") },
            confirmButton = {
                TextButton(onClick = { showDiscard = false; onCancel() }) { Text("丢掉", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { showDiscard = false }) { Text("继续写") } }
        )
    }

    LaunchedEffect(Unit) {
        while (true) {
            nowText = timeText(System.currentTimeMillis())
            delay(20_000)
        }
    }

    val glass = LocalGlass.current
    if (glass) GlassBackground(MaterialTheme.colorScheme.background.red < 0.5f)
    Surface(modifier = Modifier.fillMaxSize(), color = if (glass) Color.Transparent else MaterialTheme.colorScheme.background) {
        Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
            // 顶栏：取消 / 标题 / 主色胶囊发布
            Box(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp)) {
                TextButton(onClick = requestClose, modifier = Modifier.align(Alignment.CenterStart)) {
                    Text("取消", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text(
                    if (editing == null) "发一条记录" else "改这条记录",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Center)
                )
                if (LocalBackdrop.current != null) {
                    GlassButton(
                        onClick = publish,
                        enabled = canPublish && !publishing,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.align(Alignment.CenterEnd)
                    ) { publishLabel(MaterialTheme.colorScheme.onPrimary) }
                } else {
                    val pubSrc = remember { MutableInteractionSource() }
                    Button(
                        onClick = publish,
                        enabled = canPublish && !publishing,
                        interactionSource = pubSrc,
                        shape = RoundedCornerShape(50),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 22.dp, vertical = 8.dp),
                        modifier = Modifier.align(Alignment.CenterEnd).pressScale(pubSrc)
                    ) { publishLabel(Color.Unspecified) }
                }
            }

            Column(
                modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // 第一张卡：文字 + 字数 + 图片
                Surface(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        BasicTextField(
                            value = text,
                            onValueChange = { text = it.take(500) },
                            textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface, lineHeight = 26.sp),
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                            modifier = Modifier.fillMaxWidth().heightIn(min = 96.dp),
                            decorationBox = { inner ->
                                if (text.isEmpty()) {
                                    Text(
                                        "今天发生了什么…",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                inner()
                            }
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "${text.length} / 500",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.align(Alignment.End)
                        )
                        Spacer(Modifier.height(10.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            keptPaths.forEach { path ->
                                rememberBitmap(path, 512)?.let { old ->
                                    ThumbCell(bitmap = old, isVideo = isVideoPath(path), onRemove = { keptPaths.remove(path) })
                                }
                            }
                            newUris.forEach { uri ->
                                val uriIsVideo = remember(uri) { isVideoUri(context, uri) }
                                rememberUriBitmap(uri)?.let { bmp ->
                                    ThumbCell(bitmap = bmp, isVideo = uriIsVideo, onRemove = { newUris.remove(uri) })
                                }
                            }
                            if (totalImages < 9) {
                                val dashColor = MaterialTheme.colorScheme.outline
                                Box(
                                    modifier = Modifier
                                        .size(84.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .drawBehind {
                                            drawRoundRect(
                                                color = dashColor,
                                                cornerRadius = CornerRadius(12.dp.toPx()),
                                                style = Stroke(
                                                    width = 1.dp.toPx(),
                                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(5.dp.toPx(), 5.dp.toPx()))
                                                )
                                            )
                                        }
                                        .clickable { galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "加图", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }

                // 第二张卡：记一笔账
                Surface(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.secondaryContainer) {
                                Text(
                                    "账",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                )
                            }
                            Spacer(Modifier.width(8.dp))
                            Text("记一笔账", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                            Switch(checked = recordMoney, onCheckedChange = {
                                haptic.performHapticFeedback(if (it) HapticFeedbackType.ToggleOn else HapticFeedbackType.ToggleOff)
                                recordMoney = it
                            })
                        }
                        // 打开「记一笔账」：先让出空间再淡入；关掉时收起
                        AnimatedVisibility(
                            visible = recordMoney,
                            enter = expandVertically(motion(spring(dampingRatio = 1f, stiffness = 500f)), expandFrom = Alignment.Top) + fadeIn(tween(Motion.SMALL, delayMillis = 60)),
                            exit = shrinkVertically(motion(tween(180, easing = Motion.EaseOut))) + fadeOut(tween(100))
                        ) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            // 支出/收入：底下一块高亮滑过去，不是跳过去
                            BoxWithConstraints(
                                Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .padding(3.dp)
                            ) {
                                val segW = maxWidth / 2
                                val segX by animateDpAsState(
                                    if (moneyKind == "income") segW else 0.dp,
                                    motion(spring(dampingRatio = 1f, stiffness = 700f)),
                                    label = "seg"
                                )
                                Box(
                                    Modifier
                                        .offset { IntOffset(segX.roundToPx(), 0) }
                                        .width(segW)
                                        .height(44.dp)
                                        .clip(RoundedCornerShape(11.dp))
                                        .background(MaterialTheme.colorScheme.surface)
                                )
                                Row(Modifier.fillMaxWidth()) {
                                    listOf("expense" to "支出", "income" to "收入").forEach { (kind, label) ->
                                        val sel = moneyKind == kind
                                        val segFg by animateColorAsState(
                                            if (sel) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                                            tween(Motion.SMALL), label = "segFg"
                                        )
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(44.dp)
                                                .clip(RoundedCornerShape(11.dp))
                                                .clickable {
                                                    if (moneyKind != kind) {
                                                        haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
                                                        moneyKind = kind
                                                        category = if (kind == "income") INCOME_CATEGORIES[0] else EXPENSE_CATEGORIES[0]
                                                    }
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(label, fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal, color = segFg)
                                        }
                                    }
                                }
                            }
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    "¥",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(bottom = 6.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                BasicTextField(
                                    value = amount,
                                    onValueChange = { amount = it.filter { c -> c.isDigit() || c == '.' }.take(12) },
                                    singleLine = true,
                                    textStyle = MaterialTheme.typography.headlineMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (moneyKind == "expense") MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                                    ),
                                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    modifier = Modifier.weight(1f),
                                    decorationBox = { inner ->
                                        if (amount.isEmpty()) {
                                            Text("0.00", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        inner()
                                    }
                                )
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val fill = if (moneyKind == "expense") MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                                val onFill = if (moneyKind == "expense") MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onPrimary
                                categories.forEach { c ->
                                    val sel = category == c
                                    val catSrc = remember { MutableInteractionSource() }
                                    val catBg by animateColorAsState(if (sel) fill else MaterialTheme.colorScheme.surface, tween(Motion.SMALL), label = "catBg")
                                    Surface(
                                        onClick = { category = c },
                                        interactionSource = catSrc,
                                        modifier = Modifier.pressScale(catSrc),
                                        shape = RoundedCornerShape(50),
                                        color = catBg,
                                        border = if (sel) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                    ) {
                                        Text(
                                            c,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = if (sel) onFill else MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                                        )
                                    }
                                }
                            }
                        }
                        }
                    }
                }

                // 第三张卡：单词提示
                Surface(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.tertiaryContainer) {
                            Text(
                                "词",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "背单词会自动记一条，不用手动发",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
            }

            // 底部工具栏
            Surface(color = MaterialTheme.colorScheme.surface, tonalElevation = 0.dp) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BottomPill("相册", Icons.Default.Image) { galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)) }
                    Spacer(Modifier.width(10.dp))
                    BottomPill("拍照", Icons.Default.PhotoCamera) { wantVideo = false; permissionLauncher.launch(android.Manifest.permission.CAMERA) }
                    Spacer(Modifier.width(10.dp))
                    BottomPill("录像", Icons.Default.Videocam) { wantVideo = true; permissionLauncher.launch(android.Manifest.permission.CAMERA) }
                    Spacer(Modifier.weight(1f))
                    Text(
                        "现在 $nowText",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun BottomPill(label: String, icon: ImageVector, onClick: () -> Unit) {
    Surface(onClick = onClick, shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.surfaceVariant) {
        Row(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.width(6.dp))
            Text(label, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun ChoiceChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val src = remember { MutableInteractionSource() }
    val haptic = LocalHapticFeedback.current
    val bg by animateColorAsState(if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant, tween(Motion.SMALL), label = "chipBg")
    val fg by animateColorAsState(if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant, tween(Motion.SMALL), label = "chipFg")
    Surface(
        onClick = { if (!selected) haptic.performHapticFeedback(HapticFeedbackType.SegmentTick); onClick() },
        interactionSource = src,
        modifier = Modifier.pressScale(src),
        shape = RoundedCornerShape(16.dp),
        color = bg,
        contentColor = fg
    ) {
        Text(label, style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp))
    }
}

/** 主题颜色选择：左边一个色点，后面是颜色名，选中态用当前主色填充。 */
@Composable
private fun PaletteChip(label: String, dotColor: Color, selected: Boolean, onClick: () -> Unit) {
    val src = remember { MutableInteractionSource() }
    val haptic = LocalHapticFeedback.current
    val bg by animateColorAsState(if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant, tween(Motion.SMALL), label = "paletteBg")
    val fg by animateColorAsState(if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant, tween(Motion.SMALL), label = "paletteFg")
    Surface(
        onClick = { if (!selected) haptic.performHapticFeedback(HapticFeedbackType.SegmentTick); onClick() },
        interactionSource = src,
        modifier = Modifier.pressScale(src),
        shape = RoundedCornerShape(16.dp),
        color = bg,
        contentColor = fg
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier.size(12.dp).background(dotColor, RoundedCornerShape(6.dp)))
            Spacer(Modifier.width(6.dp))
            Text(label, style = MaterialTheme.typography.labelLarge)
        }
    }
}

/** 单词：选词书、卡片背词（复习优先）、今日统计、生词本、自己加的卡片。 */
@Composable
private fun WordsScreen(
    packWords: List<PackWord>,
    packReady: Boolean,
    bookId: String,
    onChangeBook: (String) -> Unit,
    unknownWords: List<UnknownWord>,
    cards: List<VocabularyCard>,
    db: AppDatabase,
    study: StudyProgress,
    modifier: Modifier,
    onMessage: (String) -> Unit,
    onUndoable: (String, suspend () -> Unit) -> Unit,
    onRoundDone: (Int) -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val recordsDao = remember(db) { db.records() }
    val unknownDao = remember(db) { db.unknownWords() }
    val cardsDao = remember(db) { db.cards() }
    val progressDao = remember(db) { db.wordProgress() }
    val dayDao = remember(db) { db.studyDays() }
    val book = PackStore.book(bookId)
    var showAddCard by remember { mutableStateOf(false) }
    var showBookPicker by remember { mutableStateOf(false) }
    var detailWord by remember { mutableStateOf<PackWordLite?>(null) }
    var showUnknownBook by rememberSaveable { mutableStateOf(false) }
    val total = packWords.size
    val wordMap = remember(packWords) { packWords.associateBy { it.headword } }
    val haptic = LocalHapticFeedback.current
    val reduceMotion = LocalReduceMotion.current

    // 统计：待复习 / 今日 / 学习天数；每次进出学习刷新一下「现在」
    val nowKey = remember(study.inStudy) { System.currentTimeMillis() }
    val dueCount by remember(nowKey) { progressDao.observeDueCount(nowKey) }.collectAsState(initial = 0)
    val learnedCount by remember { progressDao.observeLearnedCount() }.collectAsState(initial = 0)
    val masteredCount by remember { progressDao.observeMasteredCount(WordStudy.MASTERED_STAGE) }.collectAsState(initial = 0)
    val days by remember { dayDao.observeAll() }.collectAsState(initial = emptyList())
    val today = days.firstOrNull { it.day == WordStudy.today() }
    val studyDays = days.count { it.newCount + it.reviewCount > 0 }
    val streak = WordStudy.streak(days)

    // 发音：用手机自带的英文朗读，离线可用
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    DisposableEffect(Unit) {
        var engine: TextToSpeech? = null
        engine = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) { engine?.language = Locale.US; tts = engine }
        }
        onDispose { engine?.shutdown(); tts = null }
    }
    fun speak(word: String) {
        val t = tts
        if (t == null) onMessage("手机没有可用的英文朗读引擎") else t.speak(word, TextToSpeech.QUEUE_FLUSH, null, word)
    }

    fun saveProgress() {
        SettingsStore.setStudyProgress(context, study.snapshot())
        SettingsStore.setBookIndex(context, bookId, study.index)
    }

    fun finishSession() {
        val reviewed = study.known + study.unknown
        if (reviewed > 0) {
            scope.launch {
                withContext(Dispatchers.IO) {
                    recordsDao.insert(RecordEntry(kind = "study", text = "背了 $reviewed 个词"))
                }
            }
            onRoundDone(reviewed)
        }
        study.showMeaning = false; study.known = 0; study.unknown = 0; study.inStudy = false
        study.queue.clear(); study.reviewPos = 0; study.mode = StudyMode.BOOK
        saveProgress()
    }

    /** 开始背词：先过到期的复习词（记错多的优先），再接着学新词。 */
    fun startSession(resume: Boolean = true) {
        if (total == 0) { onMessage("词书还没准备好"); return }
        scope.launch {
            val due = withContext(Dispatchers.IO) { progressDao.due(System.currentTimeMillis(), 200) }
            study.queue.clear(); study.queue.addAll(due.map { it.lite() })
            study.reviewPos = 0
            study.mode = StudyMode.BOOK
            study.index = study.index.coerceIn(0, total - 1)
            if (!resume) { study.index = 0; study.known = 0; study.unknown = 0 }
            study.inStudy = true
            study.showMeaning = false
            saveProgress()
        }
    }

    /** 只复习生词本。 */
    fun startUnknownReview() {
        if (unknownWords.isEmpty()) { onMessage("生词本是空的"); return }
        study.queue.clear()
        study.queue.addAll(unknownWords.sortedBy { it.knownCount }.map { it.lite() })
        study.reviewPos = 0
        study.mode = StudyMode.UNKNOWN
        study.known = 0; study.unknown = 0
        study.showMeaning = false
        study.inStudy = true
    }

    if (study.inStudy && (packWords.isNotEmpty() || study.mode == StudyMode.UNKNOWN)) {
        val inQueue = study.reviewPos < study.queue.size
        if (study.mode == StudyMode.UNKNOWN && !inQueue) {
            // 生词本过完一轮
            LaunchedEffect(Unit) { finishSession() }
            return
        }
        val index = study.index.coerceIn(0, (total - 1).coerceAtLeast(0))
        val isReview = inQueue
        val lite: PackWordLite = if (inQueue) study.queue[study.reviewPos] else packWords[index].lite()
        val full: PackWord? = wordMap[lite.headword]
        var progress by remember(lite.headword) { mutableStateOf<WordProgress?>(null) }
        LaunchedEffect(lite.headword) { progress = withContext(Dispatchers.IO) { progressDao.get(lite.headword) } }
        val unknownEntry = unknownWords.firstOrNull { it.headword == lite.headword }
        val warnColor = warnTone()

        fun answer(known: Boolean) {
            // 结果触感和释义出现在同一帧
            haptic.performHapticFeedback(if (known) HapticFeedbackType.Confirm else HapticFeedbackType.Reject)
            if (known) study.known += 1 else study.unknown += 1
            study.showMeaning = true
            saveProgress()
            val wasNew = progress == null
            scope.launch {
                // 写库出错不能把整个 App 带崩：最多这一次进度没记上
                runCatching { withContext(Dispatchers.IO) {
                    progressDao.upsert(WordStudy.answer(progressDao.get(lite.headword), lite, known))
                    WordStudy.bumpToday(dayDao, isNew = wasNew && !isReview)
                    if (study.mode == StudyMode.UNKNOWN) {
                        val cur = unknownDao.all().firstOrNull { it.headword == lite.headword }
                        if (cur != null) {
                            if (!known) unknownDao.setKnownCount(lite.headword, 0)
                            else if (cur.knownCount + 1 >= WordStudy.UNKNOWN_GRADUATE) unknownDao.deleteByHeadword(lite.headword)
                            else unknownDao.setKnownCount(lite.headword, cur.knownCount + 1)
                        }
                    } else if (!known) {
                        if (unknownDao.countByHeadword(lite.headword) == 0) {
                            unknownDao.insert(UnknownWord(headword = lite.headword, phonetic = lite.phonetic, meaning = lite.meaning))
                        } else unknownDao.setKnownCount(lite.headword, 0)
                    }
                } }.onFailure { android.util.Log.w("WordStudy", "answer save failed", it) }
            }
        }
        fun next() {
            study.showMeaning = false
            if (isReview) {
                study.reviewPos += 1
                if (study.mode == StudyMode.UNKNOWN && study.reviewPos >= study.queue.size) finishSession()
            } else if (study.index + 1 >= total) finishSession()
            else { study.index += 1; saveProgress() }
        }

        val label = when {
            study.mode == StudyMode.UNKNOWN -> "生词本"
            isReview -> "${book.shortName} · 复习"
            else -> "${book.shortName} · 新词"
        }
        val counter = if (isReview) "${study.reviewPos + 1} / ${study.queue.size}" else "${index + 1} / $total"
        // 系统返回 = 点「结束」：本轮记到记录里，回到单词首页，不再直接退出 App
        BackHandler(enabled = LocalTabActive.current) { finishSession() }
        val frac = if (isReview) (study.reviewPos + 1f) / study.queue.size.coerceAtLeast(1) else if (total == 0) 0f else (index + 1f) / total

        Column(modifier.fillMaxSize().padding(horizontal = 20.dp)) {
            Spacer(Modifier.height(4.dp))
            Box(Modifier.fillMaxWidth()) {
                TextButton(
                    onClick = { finishSession() },
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(start = 4.dp, end = 12.dp),
                    modifier = Modifier.align(Alignment.CenterStart).minimumInteractiveComponentSize()
                ) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text("结束", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                }
                Text(label, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Center))
                Text(counter, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.align(Alignment.CenterEnd))
            }
            Spacer(Modifier.height(8.dp))
            // 进度条平滑前进，不是一格一格跳
            val fracAnim by animateFloatAsState(frac, motion(spring(dampingRatio = 1f, stiffness = Spring.StiffnessLow)), label = "studyProgress")
            LinearProgressIndicator(
                progress = { fracAnim },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(50)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                drawStopIndicator = {}
            )
            Spacer(Modifier.height(12.dp))
            val wrong = progress?.wrongCount ?: 0
            // 「记错 N 次」胶囊：展开出现，不把卡片一下子推下去
            AnimatedVisibility(
                visible = wrong > 0 || (study.mode == StudyMode.UNKNOWN && unknownEntry != null),
                enter = fadeIn(tween(Motion.SMALL)) + expandVertically(Motion.settle()),
                exit = fadeOut(tween(100)) + shrinkVertically(tween(Motion.EXIT, easing = Motion.EaseOut))
            ) {
                Column {
                    Surface(color = warnColor.copy(alpha = .14f), shape = RoundedCornerShape(50)) {
                        Text(
                            if (study.mode == StudyMode.UNKNOWN) "生词 · 已认识 ${unknownEntry?.knownCount ?: 0}/${WordStudy.UNKNOWN_GRADUATE}" else "记错 $wrong 次 · 优先复习",
                            style = MaterialTheme.typography.labelLarge, color = warnColor,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                }
            }
            Box(Modifier.fillMaxWidth()) {
            // 卡片堆：下面露出接下来几张卡的边缘（还剩几张就叠几层，最多 2 层）
            val remaining = if (isReview) study.queue.size - study.reviewPos - 1 else total - index - 1
            WordStackPeek(remaining.coerceIn(0, 2))
            GlassCard(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier.fillMaxWidth(),
                onClick = { if (study.showMeaning) detailWord = lite }
            ) {
                Column(
                    Modifier.fillMaxWidth().heightIn(min = 330.dp)
                        .animateContentSize(motion(spring(dampingRatio = 1f, stiffness = Spring.StiffnessMediumLow)))
                        .padding(22.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(Modifier.height(36.dp))
                    // 换词：卡片不动，只换里面的词，新词从右边 1/6 处进来
                    AnimatedContent(
                        targetState = lite,
                        contentKey = { it.headword },
                        transitionSpec = {
                            if (reduceMotion) fadeIn(tween(Motion.SMALL)) togetherWith fadeOut(tween(100))
                            else (slideInHorizontally(tween(Motion.STATE, easing = Motion.EaseOut)) { it / 6 } + fadeIn(tween(Motion.STATE)))
                                .togetherWith(slideOutHorizontally(tween(Motion.EXIT, easing = Motion.EaseOut)) { -it / 6 } + fadeOut(tween(Motion.EXIT)))
                                .using(SizeTransform(clip = false))
                        },
                        label = "word"
                    ) { w ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(w.headword, style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (!w.phonetic.isNullOrBlank()) {
                                    Text("/${w.phonetic}/", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                IconButton(onClick = { speak(w.headword) }) {
                                    Icon(Icons.Default.VolumeUp, contentDescription = "发音", tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(24.dp))
                    HorizontalDivider()
                    Spacer(Modifier.height(6.dp))
                    // 释义：容器先定尺寸（animateContentSize），内容晚 40ms 淡入
                    AnimatedContent(
                        targetState = if (study.showMeaning) lite else null,
                        contentKey = { it?.headword },
                        transitionSpec = {
                            if (reduceMotion) fadeIn(tween(Motion.SMALL)) togetherWith fadeOut(tween(100))
                            else (fadeIn(tween(180, delayMillis = 40)) + slideInVertically(tween(Motion.STATE, easing = Motion.EaseOut)) { 12 })
                                .togetherWith(fadeOut(tween(100)))
                                .using(SizeTransform(clip = false))
                        },
                        label = "meaning"
                    ) { shown ->
                        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            if (shown != null) {
                                Text(shown.meaning.replace("；", "\n"), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
                                val forms = wordMap[shown.headword]?.forms.orEmpty().filter { it.first != "原形" }
                                if (forms.isNotEmpty()) {
                                    Text(forms.joinToString(" · ") { it.second }, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                                }
                            } else {
                                Text(
                                    "先想一想它的意思\n点「认识」或「不认识」后显示",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 18.dp)
                                )
                            }
                        }
                    }
                }
            }
            }
            Spacer(Modifier.weight(1f))
            val glassUi = LocalBackdrop.current != null
            val last = if (isReview) study.reviewPos + 1 >= study.queue.size && study.mode == StudyMode.UNKNOWN else index + 1 >= total
            // 认识/不认识 → 下一个：同一块区域原地交叉淡化，不是一下子换掉
            AnimatedContent(
                targetState = study.showMeaning,
                transitionSpec = { fadeIn(tween(Motion.SMALL, delayMillis = 30)) togetherWith fadeOut(tween(90)) using SizeTransform(clip = false) },
                label = "answerButtons"
            ) { showing ->
            Column {
            if (!showing) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (glassUi) {
                        GlassButton(onClick = { answer(false) }, modifier = Modifier.weight(1f).height(54.dp)) {
                            Text("不认识", fontWeight = FontWeight.SemiBold, color = warnColor)
                        }
                        GlassButton(onClick = { answer(true) }, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.weight(1f).height(54.dp)) {
                            Text("认识", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onPrimary)
                        }
                    } else {
                        val noSrc = remember { MutableInteractionSource() }
                        val yesSrc = remember { MutableInteractionSource() }
                        OutlinedButton(
                            onClick = { answer(false) },
                            modifier = Modifier.weight(1f).height(54.dp).pressScale(noSrc),
                            interactionSource = noSrc,
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = warnColor)
                        ) { Text("不认识", fontWeight = FontWeight.SemiBold) }
                        Button(
                            onClick = { answer(true) },
                            modifier = Modifier.weight(1f).height(54.dp).pressScale(yesSrc),
                            interactionSource = yesSrc,
                            shape = RoundedCornerShape(50),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) { Text("认识", fontWeight = FontWeight.SemiBold) }
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    if (study.mode == StudyMode.UNKNOWN) "连续认识 ${WordStudy.UNKNOWN_GRADUATE} 次就移出生词本" else WordStudy.nextHint(progress),
                    style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()
                )
            } else {
                // 「下一个 → 完成」在同一个按钮里换字
                val nextLabel: @Composable () -> Unit = {
                    AnimatedContent(last, transitionSpec = { fadeIn(tween(Motion.SMALL)) togetherWith fadeOut(tween(90)) }, label = "nextLabel") { isLast ->
                        Text(if (isLast) "完成" else "下一个", fontWeight = FontWeight.SemiBold, color = if (glassUi) MaterialTheme.colorScheme.onPrimary else androidx.compose.ui.graphics.Color.Unspecified)
                    }
                }
                if (glassUi) {
                    GlassButton(onClick = { next() }, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.fillMaxWidth().height(54.dp)) {
                        nextLabel()
                    }
                } else {
                    val nextSrc = remember { MutableInteractionSource() }
                    Button(
                        onClick = { next() },
                        modifier = Modifier.fillMaxWidth().height(54.dp).pressScale(nextSrc),
                        interactionSource = nextSrc,
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) { nextLabel() }
                }
            }
            }
            }
            Spacer(Modifier.height(20.dp))
        }
        detailWord?.let { w ->
            WordDetailDialog(w, wordMap[w.headword], unknownWords.firstOrNull { it.headword == w.headword }, progressDao, unknownDao, onSpeak = { speak(it) }, onDismiss = { detailWord = null })
        }
        return
    }

    if (showUnknownBook) {
        BackHandler(enabled = LocalTabActive.current) { showUnknownBook = false }
        LazyColumn(modifier.fillMaxSize().padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { showUnknownBook = false }) { Icon(Icons.Default.ChevronLeft, contentDescription = "返回") }
                    Column(Modifier.weight(1f)) {
                        Text("生词本", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        Text("${unknownWords.size} 个 · 连续认识 ${WordStudy.UNKNOWN_GRADUATE} 次自动移出", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            if (unknownWords.isNotEmpty()) item {
                Button(
                    onClick = { startUnknownReview() },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) { Icon(Icons.Default.PlayArrow, null); Spacer(Modifier.width(6.dp)); Text("复习生词本", fontWeight = FontWeight.SemiBold) }
            }
        if (unknownWords.isEmpty()) {
            item { EmptyCard("生词本是空的", "背词时点「不认识」的词会自动放进来") }
        } else {
            items(unknownWords, key = { "unknown-${it.id}" }) { word ->
                var expanded by remember { mutableStateOf(false) }
                Box(rowMotion(reduceMotion)) {
                SwipeToRemove(onRemove = {
                    scope.launch {
                        withContext(Dispatchers.IO) { unknownDao.deleteByHeadword(word.headword) }
                        onUndoable("已把 ${word.headword} 移出生词本") {
                            if (unknownDao.countByHeadword(word.headword) == 0) unknownDao.insert(word)
                        }
                    }
                }) {
                GlassCard(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(18.dp), onClick = { detailWord = word.lite() }) {
                    Column(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(word.headword, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text(
                                    listOfNotNull(word.phonetic?.takeIf { it.isNotBlank() }?.let { "/${PackStore.cleanPhonetic(it)}/" }, "认识 ${word.knownCount}/${WordStudy.UNKNOWN_GRADUATE}").joinToString(" · "),
                                    style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            val pillSrc = remember { MutableInteractionSource() }
                            Surface(
                                onClick = { expanded = !expanded },
                                interactionSource = pillSrc,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = .14f),
                                shape = RoundedCornerShape(50),
                                modifier = Modifier.pressScale(pillSrc)
                            ) {
                                AnimatedContent(expanded, transitionSpec = { fadeIn(tween(Motion.SMALL)) togetherWith fadeOut(tween(90)) using SizeTransform(clip = false) }, label = "pill") { ex ->
                                    Text(if (ex) "收起" else "看意思", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp))
                                }
                            }
                        }
                        // 展开释义：先让出空间再淡入，不是直接冒出来
                        AnimatedVisibility(
                            visible = expanded,
                            enter = expandVertically(spring(dampingRatio = 1f, stiffness = 500f)) + fadeIn(tween(Motion.SMALL, delayMillis = 50)),
                            exit = shrinkVertically(tween(Motion.EXIT, easing = Motion.EaseOut)) + fadeOut(tween(80))
                        ) { Text(word.meaning.replace("；", "\n")) }
                    }
                }
                }
                }
            }
        }
            item { Spacer(Modifier.height(88.dp)) }
        }
        detailWord?.let { w ->
            WordDetailDialog(w, wordMap[w.headword], unknownWords.firstOrNull { it.headword == w.headword }, progressDao, unknownDao, onSpeak = { speak(it) }, onDismiss = { detailWord = null })
        }
        return
    }

    LazyColumn(modifier.fillMaxSize().padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Spacer(Modifier.height(4.dp))
            Text("背单词", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text("一词一卡，认识就下一个", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        item {
            // 词书卡：书名 / 进度 / 继续学习 · 换词书 · 从头开始
            GlassCard(color = MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(24.dp)) {
                Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val onC = MaterialTheme.colorScheme.onPrimaryContainer
                    Text(book.fullName + if (book.id == "dev" || book.id == "java") "" else "（ECDICT）", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = onC)
                    Text(
                        if (!packReady) "词书加载中…"
                        else if (total == 0) "词书还没准备好"
                        else if (study.index > 0) "上次学到第 ${study.index + 1} 个 / 共 $total 个"
                        else "共 $total 个词 · 离线可用",
                        color = onC
                    )
                    LinearProgressIndicator(
                        progress = { if (total == 0) 0f else study.index.toFloat() / total },
                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(50)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = onC.copy(alpha = .15f),
                        drawStopIndicator = {}
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Button(
                            onClick = { startSession(resume = true) },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null)
                            Spacer(Modifier.width(4.dp))
                            Text(if (study.index > 0) "继续学习" else "开始学习")
                        }
                        OutlinedButton(
                            onClick = { showBookPicker = true },
                            border = BorderStroke(1.dp, onC.copy(alpha = .4f)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = onC),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 14.dp)
                        ) { Text("换词书") }
                        if (study.index > 0) {
                            TextButton(onClick = { startSession(resume = false) }) { Text("从头开始", color = onC) }
                        }
                    }
                }
            }
        }
        item {
            // 今日统计：今日新学 / 复习 / 待复习 / 学习天数
            GlassCard(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(20.dp)) {
                Column(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        StatColumn("今日新学", "${today?.newCount ?: 0}")
                        StatColumn("复习", "${today?.reviewCount ?: 0}")
                        StatColumn("待复习", "$dueCount", highlight = dueCount > 0)
                        StatColumn("学习天数", "$studyDays 天")
                    }
                    Text(
                        "连续 $streak 天 · 已学 $learnedCount 个 · 掌握 $masteredCount 个",
                        style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        item {
            // 生词本只放一个入口，点进去单独一页
            GlassCard(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(20.dp), onClick = { showUnknownBook = true }) {
                Row(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(42.dp).clip(RoundedCornerShape(14.dp)).background(MaterialTheme.colorScheme.primary.copy(alpha = .16f)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.MenuBook, null, tint = MaterialTheme.colorScheme.primary)
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text("生词本", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(
                            if (unknownWords.isEmpty()) "背词时点「不认识」会放进来" else "${unknownWords.size} 个词 · 连续认识 ${WordStudy.UNKNOWN_GRADUATE} 次移出",
                            style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (unknownWords.isNotEmpty()) {
                        Surface(color = MaterialTheme.colorScheme.primary.copy(alpha = .14f), shape = RoundedCornerShape(50)) {
                            Text("${unknownWords.size}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp))
                        }
                        Spacer(Modifier.width(4.dp))
                    }
                    Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("我的卡片", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                IconButton(onClick = { showAddCard = true }) { Icon(Icons.Default.Add, contentDescription = "添加卡片") }
            }
        }
        if (cards.isEmpty()) {
            item { EmptyCard("还没有卡片", "点上面 + 添加第一张卡片") }
        } else {
            items(cards, key = { "card-${it.id}" }) { card ->
                GlassCard(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(18.dp), modifier = rowMotion(reduceMotion)) {
                    ListItem(
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        trailingContent = {
                            IconButton(onClick = {
                                scope.launch {
                                    withContext(Dispatchers.IO) { cardsDao.delete(card.id) }
                                    onUndoable("已删除「${card.front}」") { cardsDao.insert(card) }
                                }
                            }) {
                                Icon(Icons.Default.Delete, contentDescription = "删除这张卡片", tint = MaterialTheme.colorScheme.error)
                            }
                        },
                        headlineContent = { Text(card.front, fontWeight = FontWeight.SemiBold) },
                        supportingContent = { Text(card.back, color = MaterialTheme.colorScheme.onSurfaceVariant) },
                        leadingContent = { Surface(color = MaterialTheme.colorScheme.secondaryContainer, shape = RoundedCornerShape(12.dp)) { Icon(Icons.Default.MenuBook, null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.padding(10.dp)) } }
                    )
                }
            }
        }
        item { Spacer(Modifier.height(88.dp)) }
    }

    if (showBookPicker) {
        ZhAlertDialog(
            onDismissRequest = { showBookPicker = false },
            title = { Text("换词书") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    PackStore.BOOKS.chunked(2).forEach { pair ->
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            pair.forEach { b ->
                                val sel = b.id == bookId
                                val idx = SettingsStore.getBookIndex(context, b.id)
                                val pct = if (b.count == 0) 0f else (idx.toFloat() / b.count).coerceIn(0f, 1f)
                                val fg by animateColorAsState(if (sel) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface, tween(Motion.SMALL), label = "bookFg")
                                val bookBg by animateColorAsState(if (sel) MaterialTheme.colorScheme.primary.copy(alpha = .88f) else MaterialTheme.colorScheme.onSurface.copy(alpha = .07f), tween(Motion.SMALL), label = "bookBg")
                                val bookSrc = remember { MutableInteractionSource() }
                                Column(
                                    Modifier
                                        .weight(1f)
                                        .pressScale(bookSrc)
                                        .clip(RoundedCornerShape(18.dp))
                                        .background(bookBg)
                                        .border(1.dp, Color.White.copy(alpha = if (sel) 0f else .08f), RoundedCornerShape(18.dp))
                                        .clickable(interactionSource = bookSrc, indication = androidx.compose.foundation.LocalIndication.current) {
                                            showBookPicker = false
                                            if (!sel) onChangeBook(b.id)
                                        }
                                        .padding(horizontal = 14.dp, vertical = 12.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(b.shortName, fontWeight = FontWeight.Bold, color = fg, modifier = Modifier.weight(1f))
                                        // 对勾是小图标，允许轻轻弹一下
                                        AnimatedVisibility(sel, enter = scaleIn(spring(dampingRatio = 0.5f, stiffness = 600f), initialScale = .6f) + fadeIn(tween(100)), exit = fadeOut(tween(100))) {
                                            Icon(Icons.Default.Check, null, tint = fg, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                    Text(if (idx > 0) "学到 ${idx + 1} / %,d".format(b.count) else "%,d 词".format(b.count), style = MaterialTheme.typography.labelMedium, color = fg.copy(alpha = .72f))
                                    Box(Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(50)).background(fg.copy(alpha = .14f))) {
                                        Box(Modifier.fillMaxWidth(pct.coerceAtLeast(if (idx > 0) 0.03f else 0f)).fillMaxHeight().clip(RoundedCornerShape(50)).background(if (sel) fg else MaterialTheme.colorScheme.primary))
                                    }
                                }
                            }
                            if (pair.size == 1) Spacer(Modifier.weight(1f))
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = { TextButton(onClick = { showBookPicker = false }) { Text("关闭") } }
        )
    }
    detailWord?.let { w ->
        WordDetailDialog(w, wordMap[w.headword], unknownWords.firstOrNull { it.headword == w.headword }, progressDao, unknownDao, onSpeak = { speak(it) }, onDismiss = { detailWord = null })
    }
    if (showAddCard) {
        AddCardDialog(dismiss = { showAddCard = false }) { front, back ->
            scope.launch {
                withContext(Dispatchers.IO) { cardsDao.insert(VocabularyCard(front = front, back = back)) }
                showAddCard = false
                onMessage("已保存到本机")
            }
        }
    }
}

@Composable
private fun StatColumn(label: String, value: String, highlight: Boolean = false) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        val c by animateColorAsState(if (highlight) warnTone() else MaterialTheme.colorScheme.onSurface, tween(Motion.STATE), label = "statColor")
        RollingText(value, MaterialTheme.typography.titleLarge, c, FontWeight.Bold)
    }
}

/** 词详情：音标 + 发音、考试标签、释义、词形变化、我的记录；可加入 / 移出生词本。 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun WordDetailDialog(
    word: PackWordLite,
    full: PackWord?,
    unknown: UnknownWord?,
    progressDao: WordProgressDao,
    unknownDao: UnknownWordDao,
    onSpeak: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var progress by remember(word.headword) { mutableStateOf<WordProgress?>(null) }
    LaunchedEffect(word.headword) { progress = withContext(Dispatchers.IO) { progressDao.get(word.headword) } }
    val dateFmt = remember { SimpleDateFormat("M 月 d 日", Locale.CHINA) }
    ZhAlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(word.headword, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (!word.phonetic.isNullOrBlank()) Text("/${PackStore.cleanPhonetic(word.phonetic)}/", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    TextButton(onClick = { onSpeak(word.headword) }) {
                        Icon(Icons.Default.VolumeUp, contentDescription = null)
                        Spacer(Modifier.width(4.dp)); Text("发音")
                    }
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                val tags = full?.tags.orEmpty().mapNotNull { PackStore.tagLabel(it) }
                if (tags.isNotEmpty()) {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        tags.forEach { t ->
                            Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(50)) {
                                Text(t, style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
                            }
                        }
                    }
                }
                Text("释义", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Text(word.meaning.replace("；", "\n").ifBlank { "（词书里没有释义）" })
                val forms = full?.forms.orEmpty()
                if (forms.isNotEmpty()) {
                    Text("词形变化", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    forms.forEach { (k, v) ->
                        Row { Text(k, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(96.dp)); Text(v, fontWeight = FontWeight.Medium) }
                    }
                }
                Text("我的记录", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                val p = progress
                if (p == null) Text("还没学过", color = MaterialTheme.colorScheme.onSurfaceVariant)
                else {
                    Text("第一次学：${dateFmt.format(Date(p.firstSeen))} · 记错 ${p.wrongCount} 次")
                    Text(
                        if (p.dueAt <= System.currentTimeMillis()) "现在该复习了" else "下次复习：${dateFmt.format(Date(p.dueAt))}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    LinearProgressIndicator(
                        progress = { (p.stage.toFloat() / WordStudy.MASTERED_STAGE).coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(50)),
                        drawStopIndicator = {}
                    )
                }
            }
        },
        confirmButton = {
            if (unknown == null) {
                TextButton(onClick = {
                    scope.launch { withContext(Dispatchers.IO) { unknownDao.insert(UnknownWord(headword = word.headword, phonetic = word.phonetic, meaning = word.meaning)) } }
                    onDismiss()
                }) { Text("加入生词本") }
            } else {
                TextButton(onClick = {
                    scope.launch { withContext(Dispatchers.IO) { unknownDao.deleteByHeadword(word.headword) } }
                    onDismiss()
                }) { Text("已记住，移出生词本") }
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("关闭") } }
    )
}

/** 总览：顶部两部分——(a) 我记录的事（按月的日历）(b) 收支记录（本月收支，可切月份）。 */
@Composable
private fun OverviewScreen(records: List<RecordEntry>, wordCount: Int, modifier: Modifier, onDeleteRecord: (RecordEntry) -> Unit) {
    val now = Calendar.getInstance()
    val reduce = LocalReduceMotion.current
    val haptic = LocalHapticFeedback.current
    var year by remember { mutableStateOf(now.get(Calendar.YEAR)) }
    var month by remember { mutableStateOf(now.get(Calendar.MONTH)) }
    var selectedDay by remember { mutableStateOf<Int?>(now.get(Calendar.DAY_OF_MONTH)) }
    val monthRecords = useMemoized(records, year, month) { records.filter { monthOf(it.createdAt) == year to month } }
    val income = monthRecords.filter { it.kind == "expense" && it.moneyKind == "income" }.sumOf { it.amountCents ?: 0 }
    val expense = monthRecords.filter { it.kind == "expense" && it.moneyKind == "expense" }.sumOf { it.amountCents ?: 0 }
    val studyDays = monthRecords.filter { it.kind == "study" }.map { dayKey(it.createdAt) }.distinct().size
    val isCurrentMonth = year == now.get(Calendar.YEAR) && month == now.get(Calendar.MONTH)

    fun shiftMonth(delta: Int) {
        val cal = Calendar.getInstance().apply { set(year, month, 1); add(Calendar.MONTH, delta) }
        year = cal.get(Calendar.YEAR); month = cal.get(Calendar.MONTH)
        selectedDay = null
    }

    LazyColumn(modifier.fillMaxSize().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Spacer(Modifier.height(4.dp))
            Text("总览", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text("看看这个月都记了什么、花了多少", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        item {
            // 本月结余：玻璃主题下是玻璃大卡，关掉玻璃时是主色大卡
            val glass = LocalGlass.current
            val primary = MaterialTheme.colorScheme.primary
            val onP = MaterialTheme.colorScheme.onPrimary
            val fg = if (glass) MaterialTheme.colorScheme.onSurface else onP
            val sub = if (glass) MaterialTheme.colorScheme.onSurfaceVariant else onP.copy(alpha = .8f)
            val balance = income - expense
            GlassCard(color = if (glass) MaterialTheme.colorScheme.surface else primary, shape = RoundedCornerShape(26.dp)) {
                Column(
                    Modifier.fillMaxWidth()
                        .then(if (glass) Modifier.border(1.dp, Color.White.copy(alpha = .12f), RoundedCornerShape(26.dp)) else Modifier)
                        .padding(22.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("本月结余", color = sub, style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(1f))
                        Surface(color = (if (glass) primary else onP).copy(alpha = .16f), shape = RoundedCornerShape(50)) {
                            Text(if (balance >= 0) "有结余" else "超支", color = if (glass) primary else onP, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp))
                        }
                    }
                    RollingText(formatMoney(balance), MaterialTheme.typography.displaySmall, fg, FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                    val total = (income + expense).takeIf { it > 0 } ?: 1L
                    // 收入占比条：在 drawBehind 里按动画后的比例画，不触发重新布局
                    val incomeTarget = income.toFloat() / total
                    val tabEnter = LocalTabEnter.current
                    val incomeAnim = remember(tabEnter) { Animatable(0f) }
                    val incomeSpec = motion(tween<Float>(500, easing = Motion.EaseEnter))
                    LaunchedEffect(incomeTarget, tabEnter) { if (tabEnter > 0) incomeAnim.animateTo(incomeTarget, incomeSpec) }
                    val barColor = if (glass) primary else onP.copy(alpha = .7f)
                    Box(
                        Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(50)).background(fg.copy(alpha = .12f))
                            .drawBehind {
                                drawRoundRect(barColor, size = size.copy(width = size.width * incomeAnim.value), cornerRadius = CornerRadius(size.height / 2))
                            }
                    )
                    Row(Modifier.fillMaxWidth()) {
                        Text("收入 ${formatMoney(income)}", color = sub, style = MaterialTheme.typography.labelMedium, modifier = Modifier.weight(1f))
                        Text("支出 ${formatMoney(expense)}", color = sub, style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
        item {
            // 三块小统计：学习天数 / 条记录 / 个单词
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatTile("$studyDays", "学习天数", Modifier.weight(1f))
                StatTile("${monthRecords.size}", "条记录", Modifier.weight(1f))
                StatTile("$wordCount", "个单词", Modifier.weight(1f))
            }
        }
        // 这个月花在哪：按分类汇总本月支出，从多到少，没有支出就不显示
        val byCategory = monthRecords
            .filter { it.kind == "expense" && it.moneyKind == "expense" && (it.amountCents ?: 0) > 0 }
            .groupBy { it.category?.takeIf { c -> c.isNotBlank() } ?: "其他" }
            .mapValues { (_, list) -> list.sumOf { it.amountCents ?: 0L } }
            .toList().sortedByDescending { it.second }
        if (byCategory.isNotEmpty()) {
            item {
                val maxValue = byCategory.first().second.coerceAtLeast(1L)
                val primary = MaterialTheme.colorScheme.primary
                GlassCard(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(20.dp)) {
                    Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Text("这个月花在哪", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        byCategory.forEachIndexed { i, (cat, cents) ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(cat, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.width(44.dp))
                                // 分类条从左长出来，条与条错开 60ms
                                val target = (cents.toFloat() / maxValue).coerceIn(0.04f, 1f)
                                val tabEnter = LocalTabEnter.current
                                val grow = remember(cat, tabEnter) { Animatable(0f) }
                                val growSpec = motion(tween<Float>(500, delayMillis = i * 60, easing = Motion.EaseEnter))
                                LaunchedEffect(target, tabEnter) { if (tabEnter > 0) grow.animateTo(target, growSpec) }
                                val catColor = categoryColor(cat, primary)
                                Box(
                                    Modifier.weight(1f).height(10.dp).clip(RoundedCornerShape(50)).background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                                        .drawBehind {
                                            drawRoundRect(catColor, size = size.copy(width = size.width * grow.value), cornerRadius = CornerRadius(size.height / 2))
                                        }
                                )
                                Spacer(Modifier.width(10.dp))
                                Text(formatMoney(cents), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
        item { SectionTitle("每天记了什么", "点一天看当天的记录") }
        item {
            GlassCard(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(20.dp)) {
                Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { shiftMonth(-1) }) {
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "上个月")
                        }
                        Text(monthLabel(year, month), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                        IconButton(onClick = { shiftMonth(1) }) {
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "下个月")
                        }
                    }
                    if (!isCurrentMonth) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                            TextButton(onClick = {
                                year = now.get(Calendar.YEAR); month = now.get(Calendar.MONTH)
                                selectedDay = now.get(Calendar.DAY_OF_MONTH)
                            }) { Text("回本月") }
                        }
                    }
                    Row(Modifier.fillMaxWidth()) {
                        listOf("日", "一", "二", "三", "四", "五", "六").forEach { w ->
                            Text(w, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
                        }
                    }
                    // 换月：下个月从右边进、上个月从左边进（1/8 宽），200ms
                    AnimatedContent(
                        targetState = year * 12 + month,
                        transitionSpec = {
                            val fwd = targetState > initialState
                            if (reduce) fadeIn(tween(Motion.SMALL)) togetherWith fadeOut(tween(90)) using SizeTransform(clip = false)
                            else (slideInHorizontally(tween(Motion.STATE, easing = Motion.EaseOut)) { if (fwd) it / 8 else -it / 8 } + fadeIn(tween(Motion.STATE)))
                                .togetherWith(slideOutHorizontally(tween(Motion.EXIT, easing = Motion.EaseOut)) { if (fwd) -it / 8 else it / 8 } + fadeOut(tween(Motion.EXIT)))
                                .using(SizeTransform(clip = false))
                        },
                        label = "month"
                    ) { ym ->
                        val y = ym / 12
                        val m = ym % 12
                        val cellRecords = remember(records, ym) { records.filter { monthOf(it.createdAt) == y to m }.groupBy { dayOf(it.createdAt) } }
                        val isThisMonth = y == now.get(Calendar.YEAR) && m == now.get(Calendar.MONTH)
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            monthCells(y, m).chunked(7).forEach { week ->
                                Row(Modifier.fillMaxWidth()) {
                                    week.forEach { day ->
                                        val dayRecords = if (day == null) emptyList() else cellRecords[day].orEmpty()
                                        val isToday = isThisMonth && day == now.get(Calendar.DAY_OF_MONTH)
                                        val selected = day != null && day == selectedDay && ym == year * 12 + month
                                        val cellBg by animateColorAsState(
                                            when {
                                                selected -> MaterialTheme.colorScheme.primaryContainer
                                                dayRecords.isNotEmpty() -> MaterialTheme.colorScheme.surfaceVariant
                                                else -> Color.Transparent
                                            },
                                            tween(Motion.SMALL), label = "cellBg"
                                        )
                                        Box(
                                            modifier = Modifier.weight(1f).height(62.dp).padding(2.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(cellBg)
                                                .then(
                                                    if (day != null) Modifier.clickable {
                                                        if (selectedDay != day) haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
                                                        selectedDay = day
                                                    } else Modifier
                                                ),
                                            contentAlignment = Alignment.TopCenter
                                        ) {
                                            if (day != null) {
                                                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(top = 4.dp)) {
                                                    Text(
                                                        "$day",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        fontWeight = if (isToday || selected) FontWeight.Bold else FontWeight.Normal,
                                                        color = when {
                                                            selected -> MaterialTheme.colorScheme.onPrimaryContainer
                                                            isToday -> MaterialTheme.colorScheme.primary
                                                            else -> MaterialTheme.colorScheme.onSurface
                                                        }
                                                    )
                                                    dayRecords.take(2).forEach { r ->
                                                        Text(
                                                            labelFor(r),
                                                            style = MaterialTheme.typography.labelSmall,
                                                            maxLines = 1,
                                                            color = if (r.kind == "expense") warnTone() else MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        // 选中那天的记录：换一天时淡入 + 8dp 上移
        item {
            AnimatedContent(
                targetState = selectedDay?.let { year * 10000 + month * 100 + it },
                transitionSpec = {
                    if (reduce) fadeIn(tween(Motion.SMALL)) togetherWith fadeOut(tween(90)) using SizeTransform(clip = false)
                    else (fadeIn(tween(Motion.STATE, easing = Motion.EaseOut)) + slideInVertically(tween(Motion.STATE, easing = Motion.EaseOut)) { it.coerceAtMost(24) })
                        .togetherWith(fadeOut(tween(90)))
                        .using(SizeTransform(clip = false))
                },
                label = "dayDetail"
            ) { key ->
                if (key == null) {
                    Spacer(Modifier.height(1.dp))
                } else {
                    val y = key / 10000
                    val m = (key / 100) % 100
                    val d = key % 100
                    val list = remember(records, key) { records.filter { monthOf(it.createdAt) == y to m && dayOf(it.createdAt) == d } }
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            "${m + 1}月${d}日 · " + if (list.isEmpty()) "这天没有记录" else "${list.size} 条",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        list.forEach { r -> RecordItemCard(r, onDelete = { onDeleteRecord(r) }) }
                    }
                }
            }
        }
        item { Spacer(Modifier.height(88.dp)) }
    }
}

/** 总览里的一个小统计块。 */
@Composable
private fun StatTile(value: String, label: String, modifier: Modifier = Modifier) {
    GlassCard(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(18.dp),
        modifier = modifier
    ) {
        Column(Modifier.fillMaxWidth().padding(vertical = 14.dp, horizontal = 12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            RollingText(value, MaterialTheme.typography.headlineSmall, MaterialTheme.colorScheme.onSurface, FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/** 我的：保留现有的数据页（导出本地备份、从备份恢复、隐私说明、离线可用），
 * 下面接设置区：通知设置 / 外观 / 关于。 */
@Composable
private fun DataScreen(
    db: AppDatabase,
    modifier: Modifier,
    themeMode: String,
    onThemeChange: (String) -> Unit,
    paletteId: String,
    onPaletteChange: (String) -> Unit,
    customColor: Int,
    darkNow: Boolean,
    onCustomColor: (Int) -> Unit,
    glassOn: Boolean,
    onGlassChange: (Boolean) -> Unit,
    hasCustomBg: Boolean,
    onPickBg: () -> Unit,
    onResetBg: () -> Unit,
    nickname: String,
    avatarPath: String?,
    onRename: () -> Unit,
    onChangeAvatar: () -> Unit,
    onMessage: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var reviewEnabled by remember { mutableStateOf(SettingsStore.isReviewReminderEnabled(context)) }
    var expenseEnabled by remember { mutableStateOf(SettingsStore.isExpenseReminderEnabled(context)) }
    val (initialHour, initialMinute) = SettingsStore.getReminderTime(context)
    var reminderHour by remember { mutableStateOf(initialHour) }
    var reminderMinute by remember { mutableStateOf(initialMinute) }
    var pendingKind by remember { mutableStateOf<String?>(null) }
    var showColorPicker by remember { mutableStateOf(false) }
    val saveAllMedia = rememberGallerySaver()
    var showAiExport by remember { mutableStateOf(false) }
    var aiWithMedia by remember { mutableStateOf(true) }
    var aiExporting by remember { mutableStateOf(false) }
    fun exportForAi(days: Int?, withMedia: Boolean) {
        scope.launch {
            aiExporting = true
            val result = runCatching {
                val root = File(context.cacheDir, "export").apply { deleteRecursively(); mkdirs() }
                val stamp = SimpleDateFormat("yyyyMMdd_HHmm", Locale.CHINA).format(Date())
                val dir = File(root, "知行导出_$stamp").apply { mkdirs() }
                val text = withContext(Dispatchers.IO) {
                    val all = db.records().all()
                    val names = if (withMedia) AiExport.exportMedia(AiExport.mediaInRange(all, days), dir) else null
                    AiExport.build(all, nickname, days, names?.takeIf { it.isNotEmpty() })
                }
                val md = File(dir, "知行_生活记录.md")
                withContext(Dispatchers.IO) { md.writeText(text) }
                val hasImages = File(dir, "图片").listFiles()?.isNotEmpty() == true
                // 有图就打成一个压缩包：文字和图片在一起；没图就只发 md
                val out = if (hasImages) File(root, "知行导出_$stamp.zip").also { z -> withContext(Dispatchers.IO) { AiExport.zipDir(dir, z) } } else md
                val mime = if (hasImages) "application/zip" else "text/markdown"
                val saved = withContext(Dispatchers.IO) { AiExport.saveToDownloads(context, out, mime) }
                val copied = text.length <= 100_000
                if (copied) {
                    val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    cm.setPrimaryClip(ClipData.newPlainText("知行生活记录", text))
                }
                val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", out)
                val send = Intent(Intent.ACTION_SEND).apply {
                    type = if (hasImages) "application/zip" else "text/plain"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_SUBJECT, "我的生活记录（知行导出）")
                    clipData = ClipData.newRawUri("知行生活记录", uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(send, "发给 AI").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                saved
            }
            aiExporting = false
            onMessage(result.fold({ saved -> if (saved != null) "已存到 $saved，也可以直接分享" else "已打包，直接分享给 AI 吧" }, { "导出失败：${it.message ?: "未知错误"}" }))
        }
    }
    if (showAiExport) {
        ZhAlertDialog(
            onDismissRequest = { showAiExport = false },
            title = { Text("导出给 AI") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("把记录整理成一份文字，开头带给 AI 的说明。发给任意 AI，它读完就能大致了解你。")
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { aiWithMedia = !aiWithMedia }) {
                        Column(Modifier.weight(1f)) {
                            Text("把照片和视频一起打包", fontWeight = FontWeight.SemiBold)
                            Text("文字和图片放进一个压缩包，存到「下载/知行」；视频截开头、中间、结尾 3 帧拼成一张图。文字里写明每条记录对应哪张图。", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(checked = aiWithMedia, onCheckedChange = { aiWithMedia = it })
                    }
                    Text("压缩包发给能读 zip 的 AI；不收 zip 的，解压后把文字和图片一起发。文字同时复制好了，只想发文字直接粘贴。", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    listOf(7 to "最近 7 天", 30 to "最近 30 天", null to "全部记录").forEach { (d, label) ->
                        OutlinedButton(onClick = { showAiExport = false; exportForAi(d, aiWithMedia) }, enabled = !aiExporting, modifier = Modifier.fillMaxWidth()) { Text(label) }
                    }
                }
            },
            confirmButton = {},
            dismissButton = { TextButton(onClick = { showAiExport = false }) { Text("取消") } }
        )
    }
    val versionName = remember {
        runCatching {
            @Suppress("DEPRECATION")
            context.packageManager.getPackageInfo(context.packageName, 0).versionName
        }.getOrNull() ?: "0.6.0"
    }

    fun timeText() = "%02d:%02d".format(reminderHour, reminderMinute)

    /** 开关打开且权限已有时真正生效：存开关 + 排一份每日任务。 */
    fun enableReminder(kind: String) {
        if (kind == ReminderScheduler.KIND_REVIEW) {
            reviewEnabled = true
            SettingsStore.setReviewReminderEnabled(context, true)
        } else {
            expenseEnabled = true
            SettingsStore.setExpenseReminderEnabled(context, true)
        }
        ReminderScheduler.schedule(context, kind)
        onMessage("已开启，每天 ${timeText()} 提醒")
    }

    // Android 13+ 开提醒要先拿到 POST_NOTIFICATIONS 权限；拒绝就开关保持关，只提示一句。
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        val kind = pendingKind
        pendingKind = null
        if (kind == null) return@rememberLauncherForActivityResult
        if (granted) enableReminder(kind)
        else onMessage("需要通知权限才能提醒，可在系统设置里打开")
    }

    fun setReminder(kind: String, enabled: Boolean) {
        if (!enabled) {
            if (kind == ReminderScheduler.KIND_REVIEW) {
                reviewEnabled = false
                SettingsStore.setReviewReminderEnabled(context, false)
            } else {
                expenseEnabled = false
                SettingsStore.setExpenseReminderEnabled(context, false)
            }
            ReminderScheduler.cancel(context, kind)
            onMessage("已关闭提醒")
            return
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            pendingKind = kind
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            return
        }
        enableReminder(kind)
    }

    /** 改提醒时间：存下来，已开的提醒按新时间重排。 */
    fun pickReminderTime() {
        TimePickerDialog(context, { _, hour, minute ->
            reminderHour = hour
            reminderMinute = minute
            SettingsStore.setReminderTime(context, hour, minute)
            if (reviewEnabled) ReminderScheduler.schedule(context, ReminderScheduler.KIND_REVIEW)
            if (expenseEnabled) ReminderScheduler.schedule(context, ReminderScheduler.KIND_EXPENSE)
            onMessage("提醒时间已改成 ${timeText()}")
        }, reminderHour, reminderMinute, true).show()
    }

    val export = androidx.activity.compose.rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri != null) scope.launch {
            val result = runCatching { withContext(Dispatchers.IO) { DataTransfer.export(context, uri, db) } }
            onMessage(result.fold({ "备份已导出" }, { "导出失败：${it.message ?: "未知错误"}" }))
        }
    }
    // 手动恢复：用户自己点的，可以开系统文件选择器，并尽量直接停在「文档/知行」
    val import = androidx.activity.compose.rememberLauncherForActivityResult(OpenBackupDocument()) { uri ->
        if (uri != null) scope.launch {
            val result = runCatching { withContext(Dispatchers.IO) { DataTransfer.import(context, uri, db) } }
            onMessage(result.fold({ "数据已导入" }, { "导入失败：${it.message ?: "未知错误"}" }))
        }
    }
    var page by rememberSaveable { mutableStateOf("main") }   // main | settings | privacy | about | board
    var showUpdate by remember { mutableStateOf(false) }
    if (showUpdate) UpdateDialog(onDismiss = { showUpdate = false })
    if (page != "main") BackHandler(enabled = LocalTabActive.current) { page = "main" }
    @Composable
    fun SubHeader(title: String, sub: String) {
        Spacer(Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { page = "main" }) { Icon(Icons.Default.ChevronLeft, contentDescription = "返回") }
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text(sub, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
    // 我的 → 设置 / 隐私 / 关于：有层级的转场，进去从右边 1/5 处滑入，返回反向
    val reducePage = LocalReduceMotion.current
    AnimatedContent(
        targetState = page,
        transitionSpec = {
            val fwd = targetState != "main"
            if (reducePage) fadeIn(tween(Motion.SMALL)) togetherWith fadeOut(tween(90))
            else (slideInHorizontally(tween(250, easing = Motion.EaseOut)) { if (fwd) it / 5 else -it / 5 } + fadeIn(tween(Motion.STATE)))
                .togetherWith(slideOutHorizontally(tween(Motion.STATE, easing = Motion.EaseOut)) { if (fwd) -it / 10 else it / 10 } + fadeOut(tween(120)))
        },
        label = "minePage"
    ) { pg ->
    when (pg) {
    "settings" -> LazyColumn(modifier.fillMaxSize().padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { SubHeader("设置", "头像、通知、外观") }
        item {
            Text("头像和昵称", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            val avatarThumb = rememberBitmap(avatarPath, 256)
            GlassCard(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(20.dp)) {
                Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(44.dp).clip(CircleShape)) {
                            if (avatarThumb != null) {
                                Image(bitmap = avatarThumb, contentDescription = "头像", contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                            } else {
                                AvatarFace(name = nickname, modifier = Modifier.fillMaxSize())
                            }
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text("头像", fontWeight = FontWeight.SemiBold)
                        }
                        TextButton(onClick = onChangeAvatar) { Text("换头像") }
                    }
                    HorizontalDivider()
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("昵称", fontWeight = FontWeight.SemiBold)
                            Text(nickname, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        TextButton(onClick = onRename) { Text("改昵称") }
                    }
                }
            }
        }
        item {
            Text("通知设置", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            GlassCard(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(20.dp)) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("背单词复习提醒", fontWeight = FontWeight.SemiBold)
                        }
                        Switch(checked = reviewEnabled, onCheckedChange = { setReminder(ReminderScheduler.KIND_REVIEW, it) })
                    }
                    HorizontalDivider()
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("记账提醒", fontWeight = FontWeight.SemiBold)
                        }
                        Switch(checked = expenseEnabled, onCheckedChange = { setReminder(ReminderScheduler.KIND_EXPENSE, it) })
                    }
                    HorizontalDivider()
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("提醒时间", fontWeight = FontWeight.SemiBold)
                        }
                        Text(timeText(), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(4.dp))
                        TextButton(onClick = { pickReminderTime() }) { Text("改时间") }
                    }
                }
            }
        }
        item {
            // 自动记账：读微信 / 支付宝 / 云闪付的付款通知
            val ctx = LocalContext.current
            var hasAccess by remember { mutableStateOf(AutoBook.hasAccess(ctx)) }
            var autoOn by remember { mutableStateOf(AutoBook.isEnabled(ctx)) }
            var askFirst by remember { mutableStateOf(AutoBook.askFirst(ctx)) }
            var bgOk by remember { mutableStateOf(AutoBook.ignoresBatteryOpt(ctx)) }
            LifecycleResumeEffect(Unit) {
                hasAccess = AutoBook.hasAccess(ctx)
                bgOk = AutoBook.ignoresBatteryOpt(ctx)
                onPauseOrDispose { }
            }
            Text("自动记账", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            GlassCard(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(20.dp)) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("付款后自动记一笔", fontWeight = FontWeight.SemiBold)
                            Text(
                                if (hasAccess) "已开启：会读微信、支付宝、云闪付的付款通知" else "要先在系统里给知行「通知使用权」",
                                style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (hasAccess) {
                            Switch(checked = autoOn, onCheckedChange = { autoOn = it; AutoBook.setEnabled(ctx, it) })
                        } else {
                            TextButton(onClick = { runCatching { ctx.startActivity(AutoBook.accessSettingsIntent(ctx)) } }) { Text("去开启") }
                        }
                    }
                    if (hasAccess) {
                        HorizontalDivider()
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text("入账前先问我", fontWeight = FontWeight.SemiBold)
                            }
                            Switch(checked = askFirst, onCheckedChange = { askFirst = it; AutoBook.setAskFirst(ctx, it) })
                        }
                        HorizontalDivider()
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text("允许后台运行", fontWeight = FontWeight.SemiBold)
                                Text(if (bgOk) "已允许：关掉知行也能记" else "没允许的话，关掉知行后系统会停掉记账", style = MaterialTheme.typography.labelMedium, color = if (bgOk) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.error)
                            }
                            if (!bgOk) TextButton(onClick = { runCatching { ctx.startActivity(AutoBook.batteryOptIntent(ctx)) }.onFailure { runCatching { ctx.startActivity(AutoBook.appDetailsIntent(ctx)) } } }) { Text("去允许") }
                        }
                        HorizontalDivider()
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text("自启动和后台耗电", fontWeight = FontWeight.SemiBold)
                                Text("OPPO：应用详情 → 耗电管理，打开「允许自启动」和「允许后台行为」", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            TextButton(onClick = { runCatching { ctx.startActivity(AutoBook.appDetailsIntent(ctx)) } }) { Text("去设置") }
                        }
                        HorizontalDivider()
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text("试一下", fontWeight = FontWeight.SemiBold)
                            }
                            TextButton(onClick = { AutoBook.prompt(ctx, Payment(3650, "示例商家", "微信支付", "餐饮"), demo = true) }) { Text("试一下") }
                        }
                    }
                }
            }
        }
        item {
            Text("外观", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            GlassCard(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(20.dp)) {
                Column(Modifier.fillMaxWidth().padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(if (darkNow) "黑夜" else "白天", fontWeight = FontWeight.SemiBold)
                        }
                        DayNightToggle(dark = darkNow, onToggle = { toDark -> onThemeChange(if (toDark) ThemeMode.DARK else ThemeMode.LIGHT) })
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("跟随系统", fontWeight = FontWeight.SemiBold)
                        }
                        Switch(
                            checked = themeMode == ThemeMode.SYSTEM,
                            onCheckedChange = { on -> onThemeChange(if (on) ThemeMode.SYSTEM else if (darkNow) ThemeMode.DARK else ThemeMode.LIGHT) }
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("磨砂玻璃", fontWeight = FontWeight.SemiBold)
                        }
                        Switch(checked = glassOn, onCheckedChange = onGlassChange)
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("自定义背景", fontWeight = FontWeight.SemiBold)
                        }
                        if (hasCustomBg) TextButton(onClick = onResetBg) { Text("恢复默认") }
                        TextButton(onClick = onPickBg) { Text("换图片") }
                    }
                    Spacer(Modifier.height(16.dp))
                    Text("主题颜色", fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(10.dp))
                    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        APP_PALETTES.forEach { palette ->
                            PaletteChip(
                                label = palette.label,
                                dotColor = palette.light.primary,
                                selected = paletteId == palette.id
                            ) { onPaletteChange(palette.id) }
                        }
                        PaletteChip(
                            label = "自定义",
                            dotColor = Color(customColor),
                            selected = paletteId == PaletteId.CUSTOM
                        ) { showColorPicker = true }
                    }
                    if (showColorPicker) {
                        CustomColorSheet(
                            initial = customColor,
                            dark = darkNow,
                            onDismiss = { showColorPicker = false },
                            onApply = { c -> onCustomColor(c); showColorPicker = false; onMessage("主题色已换好") }
                        )
                    }
                }
            }
        }
        item { Spacer(Modifier.height(88.dp)) }
    }
    "board" -> Column(modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        SubHeader("贴纸板", "拖动摆放，两指缩放旋转；桌面可以加贴纸板小组件")
        Spacer(Modifier.height(8.dp))
        StickerBoardEditor(db, onMessage, Modifier.fillMaxWidth().weight(1f).padding(bottom = 96.dp))
    }
    "about" -> LazyColumn(modifier.fillMaxSize().padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { SubHeader("关于知行", "v$versionName") }
        item {
            GlassCard(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(20.dp)) {
                ExplodedRecordShowcase(Modifier.fillMaxWidth().padding(vertical = 18.dp, horizontal = 8.dp))
            }
        }
        item {
            Text(
                "一条记录由这几层组成：文字、金额、图片和时间都存在知行自己的文件夹里，平时不联网，不上传。",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        item { Spacer(Modifier.height(88.dp)) }
    }
    "privacy" -> LazyColumn(modifier.fillMaxSize().padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { SubHeader("隐私说明", "你的记录只属于你") }
        val points = listOf(
            Triple(Icons.Default.CloudOff, "平时不联网", "记录、单词、账目、照片和视频都只存在这台手机里，不会上传。只有你点「检查更新」时才联网，只下载版本信息和安装包，不发送你的任何数据。"),
            Triple(Icons.Default.PhotoCamera, "用到的权限", "相机：拍照和录像；通知：每日提醒，可随时关；通知使用权：自动记账读付款通知，只看微信、支付宝、云闪付；网络和安装应用：只给「检查更新」用；存储：只在 Android 9 及以下存相册时用。"),
            Triple(Icons.Default.Download, "导出由你决定", "备份、存相册、导出给 AI 都只在你点了之后才生成文件；发给谁、发不发，由你自己选。"),
            Triple(Icons.Default.Delete, "删除", "删掉的记录连同照片和视频一起清掉。卸载知行会删除 App 里的数据，但「文档/知行」里的自动备份会留着，重装后可以恢复。")
        )
        items(points.size) { i ->
            val (icon, title, body) = points[i]
            GlassCard(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(20.dp)) {
                Row(Modifier.fillMaxWidth().padding(18.dp)) {
                    Box(Modifier.size(38.dp).clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.primary.copy(alpha = .16f)), contentAlignment = Alignment.Center) {
                        Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(title, fontWeight = FontWeight.Bold)
                        Text(body, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
        item { Spacer(Modifier.height(88.dp)) }
    }
    else -> LazyColumn(modifier.fillMaxSize().padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            // 头部：头像 + 昵称 + 设置入口
            Spacer(Modifier.height(4.dp))
            val avatarThumb = rememberBitmap(avatarPath, 256)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(56.dp).clip(CircleShape).clickable { page = "settings" }) {
                    if (avatarThumb != null) Image(bitmap = avatarThumb, contentDescription = "头像", contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                    else AvatarFace(name = nickname, modifier = Modifier.fillMaxSize())
                }
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text(nickname, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                }
            }
        }
        item {
            GlassCard(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(20.dp)) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.CloudOff, null, tint = MaterialTheme.colorScheme.primary); Spacer(Modifier.width(8.dp)); Text("离线可用", fontWeight = FontWeight.Bold) }
                    Text("记录、单词、账目都保存在这台手机。离开知行会自动备份到「文档/知行」，换机时把那个文件拷过去恢复。", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        item {
            Text("备份与恢复", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Button(onClick = { export.launch("zhixing-backup.json") }, modifier = Modifier.fillMaxWidth()) { Text("导出本地备份") }
            OutlinedButton(onClick = { import.launch(arrayOf("application/zip", "application/json", "text/plain", "application/octet-stream")) }, modifier = Modifier.fillMaxWidth()) { Text("从备份恢复") }
            if (com.swenhao10.localfirst.data.AutoBackup.supported()) {
                var autoBackupOn by remember { mutableStateOf(com.swenhao10.localfirst.data.AutoBackup.isEnabled(context)) }
                var lastBackup by remember { mutableStateOf(com.swenhao10.localfirst.data.AutoBackup.lastAt(context)) }
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("自动备份", fontWeight = FontWeight.SemiBold)
                        Text(
                            if (lastBackup > 0) "上次 ${SimpleDateFormat("M月d日 HH:mm", Locale.CHINA).format(Date(lastBackup))} · 存在${com.swenhao10.localfirst.data.AutoBackup.locationLabel()}，留最近 3 份" else "离开知行时自动备份到${com.swenhao10.localfirst.data.AutoBackup.locationLabel()}，留最近 3 份",
                            style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    TextButton(onClick = {
                        scope.launch {
                            val r = runCatching { withContext(Dispatchers.IO) { com.swenhao10.localfirst.data.AutoBackup.run(context, force = true) } }
                            lastBackup = com.swenhao10.localfirst.data.AutoBackup.lastAt(context)
                            onMessage(r.fold({ "已备份到${com.swenhao10.localfirst.data.AutoBackup.locationLabel()}" }, { "备份失败：${it.message ?: "未知错误"}" }))
                        }
                    }) { Text("立即备份") }
                    Switch(checked = autoBackupOn, onCheckedChange = { autoBackupOn = it; com.swenhao10.localfirst.data.AutoBackup.setEnabled(context, it) })
                }
            }
            OutlinedButton(
                onClick = {
                    scope.launch {
                        val all = withContext(Dispatchers.IO) { db.records().all().flatMap { it.images() }.filter { File(it).exists() } }
                        saveAllMedia(all)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("照片和视频全部存到相册") }
            OutlinedButton(onClick = { showAiExport = true }, modifier = Modifier.fillMaxWidth()) { Text("导出给 AI，让它了解我") }
            Text(
                "记录里的照片和视频都另存了一份在知行里，相册里删了也不影响。卸载或换机前点上面这个，会存一份到相册「知行」。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        item {
            GlassCard(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(20.dp)) {
                Column(Modifier.fillMaxWidth()) {
                    MineRow(Icons.Default.Image, "贴纸板", "把记录的照片和小便签贴在夹板上，桌面也能看") { page = "board" }
                    HorizontalDivider(Modifier.padding(start = 70.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = .08f))
                    MineRow(Icons.Default.Settings, "设置", "头像和昵称、通知、主题和外观") { page = "settings" }
                    HorizontalDivider(Modifier.padding(start = 70.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = .08f))
                    MineRow(Icons.Default.Lock, "隐私说明", "平时不联网，记录只存在这台手机") { page = "privacy" }
                    HorizontalDivider(Modifier.padding(start = 70.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = .08f))
                    MineRow(Icons.Default.Download, "检查更新", "当前 v$versionName，点了才联网") { showUpdate = true }
                    HorizontalDivider(Modifier.padding(start = 70.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = .08f))
                    MineRow(Icons.Default.Info, "关于知行", "一条记录是怎么存的") { page = "about" }
                }
            }
        }
        item {
            // 关于：居中的小标识 + 版本胶囊
            Column(Modifier.fillMaxWidth().padding(top = 18.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(Modifier.size(44.dp).clip(RoundedCornerShape(14.dp)).background(MaterialTheme.colorScheme.primary), contentAlignment = Alignment.Center) {
                    Text("知", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                }
                Text("知行", fontWeight = FontWeight.Bold)
                Surface(color = MaterialTheme.colorScheme.onSurface.copy(alpha = .08f), shape = RoundedCornerShape(50)) {
                    Text("v$versionName", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp))
                }
                Text("记下今天的每一件小事", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        item { Spacer(Modifier.height(88.dp)) }
    }
    }
    }
}

/**
 * 关于页的分层展开图（Dimensional Drawing 的做法）：一条记录拆成 6 层——
 * 手机 → 记录卡片 → 时间轴 → 图片 → 文字/金额 → 锁，从平面变形到轴测视角并各自升起，层与层间隔 100ms。
 * 单色、只画结构；先描边，填充在每层动画 55% 之后才淡入；ease-in-out，不回弹。
 * 画面是进度 t 的纯函数。减弱动画时直接显示展开后的静态图。点一下收起 / 再展开（收起倒序、更快）。
 */
private class ExplodedLayer(val label: String, val z: Float, val anchor: Offset, val draw: ExplodedPen.() -> Unit)

private class ExplodedPen(
    val scope: androidx.compose.ui.graphics.drawscope.DrawScope,
    val project: (Float, Float) -> Offset,
    val stroke: Color,
    val fill: Color,
    val slot: Color,
    val strokeAlpha: Float,
    val fillAlpha: Float,
    val strokePx: Float
) {
    fun rect(l: Float, t: Float, r: Float, b: Float, filled: Boolean = true, slotFill: Boolean = false) {
        val path = androidx.compose.ui.graphics.Path().apply {
            val a = project(l, t); val bb = project(r, t); val c = project(r, b); val d = project(l, b)
            moveTo(a.x, a.y); lineTo(bb.x, bb.y); lineTo(c.x, c.y); lineTo(d.x, d.y); close()
        }
        if (filled && fillAlpha > 0f) scope.drawPath(path, (if (slotFill) slot else fill).copy(alpha = (if (slotFill) slot.alpha else 1f) * fillAlpha))
        scope.drawPath(path, stroke.copy(alpha = stroke.alpha * strokeAlpha), style = Stroke(width = strokePx))
    }
    fun line(vararg pts: Pair<Float, Float>) {
        val path = androidx.compose.ui.graphics.Path()
        pts.forEachIndexed { i, (x, y) -> val o = project(x, y); if (i == 0) path.moveTo(o.x, o.y) else path.lineTo(o.x, o.y) }
        scope.drawPath(path, stroke.copy(alpha = stroke.alpha * strokeAlpha), style = Stroke(width = strokePx, cap = androidx.compose.ui.graphics.StrokeCap.Round))
    }
    fun dot(x: Float, y: Float, r: Float) {
        val o = project(x, y)
        scope.drawCircle(stroke.copy(alpha = stroke.alpha * strokeAlpha), radius = r, center = o)
    }
}

@Composable
private fun ExplodedRecordShowcase(modifier: Modifier = Modifier) {
    val reduce = LocalReduceMotion.current
    val haptic = LocalHapticFeedback.current
    val scheme = MaterialTheme.colorScheme
    val strokeColor = scheme.onSurfaceVariant
    val fillColor = scheme.surface.copy(alpha = 1f)
    val slotColor = scheme.onSurface.copy(alpha = .12f)
    val labelColor = scheme.onSurfaceVariant
    val measurer = androidx.compose.ui.text.rememberTextMeasurer()
    val labelStyle = MaterialTheme.typography.labelSmall
    // 平面坐标：手机 120 × 220；z 是每层升起的高度
    val layers = remember {
        listOf(
            ExplodedLayer("这台手机", 0f, Offset(0f, 220f)) {
                rect(0f, 0f, 120f, 220f); line(50f to 9f, 70f to 9f)
            },
            ExplodedLayer("记录卡片", 34f, Offset(10f, 180f)) { rect(10f, 40f, 110f, 180f) },
            ExplodedLayer("时间轴", 64f, Offset(20f, 170f)) { dot(20f, 52f, 3f * scope.density); line(20f to 60f, 20f to 170f) },
            ExplodedLayer("图片", 98f, Offset(30f, 140f)) {
                rect(30f, 90f, 100f, 140f); line(30f to 140f, 52f to 116f, 66f to 129f, 82f to 110f, 100f to 136f)
            },
            ExplodedLayer("文字 · 金额", 128f, Offset(30f, 160f)) {
                rect(30f, 50f, 95f, 57f, slotFill = true); rect(30f, 63f, 78f, 70f, slotFill = true)
                rect(30f, 150f, 66f, 158f, slotFill = true); rect(74f, 149f, 100f, 160f, slotFill = true)
            },
            ExplodedLayer("只在本机", 168f, Offset(50f, 112f)) {
                rect(50f, 98f, 70f, 112f); line(54f to 98f, 54f to 90f, 66f to 90f, 66f to 98f)
            }
        )
    }
    val stepMs = 100f
    val layerMs = 900f
    val totalMs = layerMs + stepMs * (layers.size - 1) + 300f   // 最后 300ms 给结尾那句话
    val t = remember { Animatable(if (reduce) 1f else 0f) }
    var exploded by remember { mutableStateOf(true) }
    LaunchedEffect(exploded) {
        val target = if (exploded) 1f else 0f
        if (reduce) t.snapTo(target)
        else t.animateTo(target, tween(if (exploded) totalMs.toInt() else (totalMs * 0.6f).toInt(), easing = LinearEasingForTime))
    }
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Canvas(
            Modifier.fillMaxWidth().height(300.dp)
                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {
                    haptic.performHapticFeedback(HapticFeedbackType.GestureThresholdActivate)
                    exploded = !exploded
                }
        ) {
            val ms = t.value * totalMs
            val cw = size.width; val ch = size.height
            val s = ch / 262f
            val strokePx = 1.dp.toPx()
            // 平面：手机居中；轴测：矩阵 (.94, .17, -.34, .47)，整组放在右侧 60% 处，左边留给层名
            val flatX0 = cw / 2f - s * 60f
            val flatY0 = ch / 2f - s * 110f
            val isoX0 = cw * 0.6f - s * 19f
            val isoY0 = ch / 2f - s * 2.5f
            layers.forEachIndexed { i, layer ->
                val raw = ((ms - i * stepMs) / layerMs).coerceIn(0f, 1f)
                val l = Motion.EaseLayer.transform(raw)
                val project: (Float, Float) -> Offset = { x, y ->
                    val fx = flatX0 + s * x
                    val fy = flatY0 + s * y
                    val ix = isoX0 + s * (0.94f * x - 0.34f * y)
                    val iy = isoY0 + s * (0.17f * x + 0.47f * y) - s * layer.z
                    Offset(fx + (ix - fx) * l, fy + (iy - fy) * l)
                }
                // 先描边：线稿一直在；填充在这一层 55% 之后才淡入
                val fillA = ((raw - 0.55f) / 0.45f).coerceIn(0f, 1f)
                ExplodedPen(this, project, strokeColor, fillColor, slotColor, 1f, fillA, strokePx).apply(layer.draw)
                // 层名：引线先出，标签后到
                val leadA = ((raw - 0.7f) / 0.15f).coerceIn(0f, 1f)
                val labelA = ((raw - 0.85f) / 0.15f).coerceIn(0f, 1f)
                if (leadA > 0f) {
                    val anchor = project(layer.anchor.x, layer.anchor.y)
                    val laid = measurer.measure(layer.label, labelStyle)
                    val lx = 4.dp.toPx()
                    val ly = anchor.y - laid.size.height / 2f
                    val lineStart = Offset(lx + laid.size.width + 6.dp.toPx(), anchor.y)
                    val lineEnd = Offset(lineStart.x + (anchor.x - 4.dp.toPx() - lineStart.x) * leadA, anchor.y)
                    drawLine(strokeColor.copy(alpha = .5f * leadA), lineStart, lineEnd, strokeWidth = strokePx)
                    if (labelA > 0f) drawText(laid, color = labelColor.copy(alpha = labelA), topLeft = Offset(lx, ly))
                }
            }
        }
        val endA = ((t.value * totalMs - (totalMs - 300f)) / 300f).coerceIn(0f, 1f)
        Text(
            "数据只存在这台手机上",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.graphicsLayer { alpha = endA }
        )
    }
}

/** 分层展开的总进度按时间匀速走，每层自己的 ease-in-out 在画的时候算。 */
private val LinearEasingForTime = androidx.compose.animation.core.LinearEasing

/** 单词卡堆：在当前卡下面露出接下来几张卡的边缘，剩得越多叠得越厚（最多 2 张）。只画卡片下方露出的那一条，玻璃卡不会透出来。 */
@Composable
private fun androidx.compose.foundation.layout.BoxScope.WordStackPeek(count: Int) {
    val shown by animateFloatAsState(count.toFloat(), motion(spring(dampingRatio = 1f, stiffness = 400f)), label = "stack")
    val ink = MaterialTheme.colorScheme.onSurface
    Canvas(Modifier.matchParentSize()) {
        val r = 28.dp.toPx()
        clipRect(top = size.height - 1f, bottom = size.height + 40.dp.toPx()) {
            for (i in 1 downTo 0) {
                val a = (shown - i).coerceIn(0f, 1f)
                if (a <= 0f) continue
                val inset = 14.dp.toPx() * (i + 1)
                val drop = (8 + 6 * i).dp.toPx()
                drawRoundRect(
                    color = ink.copy(alpha = (if (i == 0) .10f else .06f) * a),
                    topLeft = Offset(inset, drop),
                    size = androidx.compose.ui.geometry.Size(size.width - inset * 2, size.height),
                    cornerRadius = CornerRadius(r)
                )
            }
        }
    }
}

/** 「我的」页里的一行入口：图标 + 标题 + 说明 + 右箭头。 */
@Composable
private fun MineRow(icon: ImageVector, title: String, sub: String, onClick: () -> Unit) {
    // 整行只缩到 0.985，避免整行抖动
    val src = remember { MutableInteractionSource() }
    Row(
        Modifier.fillMaxWidth().pressScale(src, pressed = 0.985f)
            .clickable(interactionSource = src, indication = androidx.compose.foundation.LocalIndication.current, onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(38.dp).clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.primary.copy(alpha = .16f)), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.SemiBold)
            Text(sub, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun AddCardDialog(dismiss: () -> Unit, save: (String, String) -> Unit) {
    var front by remember { mutableStateOf("") }
    var back by remember { mutableStateOf("") }
    ZhAlertDialog(
        onDismissRequest = dismiss,
        title = { Text("添加学习卡片") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(value = front, onValueChange = { front = it }, singleLine = true, label = { Text("名称 / 正面") })
                OutlinedTextField(value = back, onValueChange = { back = it }, minLines = 2, label = { Text("释义 / 背面") })
            }
        },
        confirmButton = { TextButton(onClick = { save(front.trim(), back.trim()) }, enabled = front.isNotBlank()) { Text("保存") } },
        dismissButton = { TextButton(onClick = dismiss) { Text("取消") } }
    )
}

@Composable
private fun SectionTitle(title: String, subtitle: String) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
        Text(subtitle, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun EmptyCard(title: String, subtitle: String) {
    GlassCard(color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(18.dp)) {
        Column(Modifier.fillMaxWidth().padding(20.dp)) { Text(title, fontWeight = FontWeight.SemiBold); Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant) }
    }
}

private fun formatMoney(cents: Long): String = "¥%.2f".format(Locale.CHINA, cents / 100.0)

private fun formatMoneyShort(cents: Long): String =
    if (cents % 100 == 0L) "¥${cents / 100}" else "¥${String.format(Locale.CHINA, "%.1f", cents / 100.0)}"

private fun dayKey(time: Long): String = SimpleDateFormat("yyyy-MM-dd", Locale.SIMPLIFIED_CHINESE).format(Date(time))
private fun dayHeader(time: Long): String = SimpleDateFormat("M月d日 EEEE", Locale.SIMPLIFIED_CHINESE).format(Date(time))
private fun timeText(time: Long): String = SimpleDateFormat("HH:mm", Locale.SIMPLIFIED_CHINESE).format(Date(time))

/** 天组头的大字：今天 / 昨天 / M月d日。 */
private fun dayTitle(time: Long): String {
    val today = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }.timeInMillis
    val day = Calendar.getInstance().apply {
        timeInMillis = time
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }.timeInMillis
    val diff = ((today - day) / (24 * 60 * 60 * 1000L)).toInt()
    return when (diff) {
        0 -> "今天"
        1 -> "昨天"
        else -> SimpleDateFormat("M月d日", Locale.SIMPLIFIED_CHINESE).format(Date(time))
    }
}

/** 天组头的灰色小字：11月10日 星期二。 */
private fun daySubtitle(time: Long): String {
    val cal = Calendar.getInstance().apply { timeInMillis = time }
    val weekday = arrayOf("星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六")[cal.get(Calendar.DAY_OF_WEEK) - 1]
    return "${cal.get(Calendar.MONTH) + 1}月${cal.get(Calendar.DAY_OF_MONTH)}日 $weekday"
}

private fun monthLabel(year: Int, month: Int): String = "${year}年${month + 1}月"

private fun monthOf(time: Long): Pair<Int, Int> {
    val cal = Calendar.getInstance().apply { timeInMillis = time }
    return cal.get(Calendar.YEAR) to cal.get(Calendar.MONTH)
}

private fun dayOf(time: Long): Int = Calendar.getInstance().apply { timeInMillis = time }.get(Calendar.DAY_OF_MONTH)

/** 某月的日历格（周日开头，空位填 null）。 */
private fun monthCells(year: Int, month: Int): List<Int?> {
    val cal = Calendar.getInstance().apply { set(year, month, 1) }
    val offset = cal.get(Calendar.DAY_OF_WEEK) - Calendar.SUNDAY
    val days = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    val total = ((offset + days + 6) / 7) * 7
    return List(total) { i -> val d = i - offset + 1; if (d in 1..days) d else null }
}

/** 日历格里一条记录的短标签。 */
private fun labelFor(record: RecordEntry): String = when (record.kind) {
    "expense" -> (if (record.moneyKind == "income") "+" else "-") + formatMoneyShort(record.amountCents ?: 0)
    "study" -> "背单词"
    else -> if (record.text.isNotBlank()) record.text.take(4) else if (record.images().any { isVideoPath(it) }) "视频" else "图片"
}

/** 按图片路径解码一张缩略图（本机文件不存在或损坏返回 null）。 */
@Composable
private fun rememberBitmap(path: String?, maxDim: Int = 1024): androidx.compose.ui.graphics.ImageBitmap? {
    // 解码放到后台线程 + 内存缓存：以前在主线程同步解码，时间轴一滑就卡
    val key = "$path@$maxDim"
    val state = androidx.compose.runtime.produceState(initialValue = path?.let { BitmapCache.get(key) }, key) {
        if (value == null && path != null) {
            value = withContext(Dispatchers.IO) { decodeBitmapFile(path, maxDim) }?.also { BitmapCache.put(key, it) }
        }
    }
    return state.value
}

/** 解码过的图按字节数缓存，最多用 1/8 的可用内存。 */
private object BitmapCache {
    private val cache = object : android.util.LruCache<String, androidx.compose.ui.graphics.ImageBitmap>(
        (Runtime.getRuntime().maxMemory() / 8).coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
    ) {
        override fun sizeOf(key: String, value: androidx.compose.ui.graphics.ImageBitmap) = value.width * value.height * 4
    }
    fun get(key: String) = cache.get(key)
    fun put(key: String, bmp: androidx.compose.ui.graphics.ImageBitmap) { cache.put(key, bmp) }
}

private fun decodeBitmapFile(path: String, maxDim: Int): androidx.compose.ui.graphics.ImageBitmap? {
    if (isVideoPath(path)) return videoFrame(path, maxDim)?.asImageBitmap()
    return try {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(path, bounds)
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null
        var sample = 1
        while (bounds.outWidth / sample > maxDim || bounds.outHeight / sample > maxDim) sample *= 2
        val opts = BitmapFactory.Options().apply { inSampleSize = sample }
        BitmapFactory.decodeFile(path, opts)?.asImageBitmap()
    } catch (e: Throwable) {
        null
    }
}

/** 把相册/拍照拿到的 content Uri 解码成缩略图预览（缩小采样，避免大图撑爆内存）。 */
@Composable
private fun rememberUriBitmap(uri: Uri?, maxDim: Int = 512): androidx.compose.ui.graphics.ImageBitmap? {
    val context = LocalContext.current
    return remember(uri) {
        if (uri == null) return@remember null
        if (isVideoUri(context, uri)) return@remember videoFrame(context, uri, maxDim)?.asImageBitmap()
        try {
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
            if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return@remember null
            var sample = 1
            while (bounds.outWidth / sample > maxDim || bounds.outHeight / sample > maxDim) sample *= 2
            val opts = BitmapFactory.Options().apply { inSampleSize = sample }
            context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, opts) }?.asImageBitmap()
        } catch (e: Exception) {
            null
        }
    }
}

/** 背单词的进度：放在 App 顶层 remember，切 Tab 或重进页面不会回到第一个词。 */
private enum class StudyMode { BOOK, UNKNOWN }

private class StudyProgress(snapshot: StudySnapshot, bookIndex: Int) {
    var index by mutableStateOf(bookIndex)
    /** 本轮要先过的复习词（或生词本），过完再学新词。 */
    val queue = mutableStateListOf<PackWordLite>()
    var reviewPos by mutableStateOf(0)
    var mode by mutableStateOf(StudyMode.BOOK)
    var known by mutableStateOf(snapshot.known)
    var unknown by mutableStateOf(snapshot.unknown)
    var inStudy by mutableStateOf(snapshot.inStudy)
    var showMeaning by mutableStateOf(false)

    fun snapshot() = StudySnapshot(index, known, unknown, inStudy)
}

/** 把相册/拍照拿到的 content Uri 复制到本机内部存储，返回绝对路径。 */
private suspend fun copyUriToInternal(context: Context, uri: Uri, name: String): String? = withContext(Dispatchers.IO) {
    try {
        val dir = File(context.filesDir, "records").apply { mkdirs() }
        val out = File(dir, name)
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(out).use { output -> input.copyTo(output) }
        }
        out.absolutePath
    } catch (e: Exception) {
        null
    }
}

private fun createVideoFile(context: Context): File {
    val dir = File(context.cacheDir, "photos").apply { mkdirs() }
    return File(dir, "video_${System.currentTimeMillis()}.mp4")
}

private fun createPhotoFile(context: Context): File {
    val dir = File(context.cacheDir, "photos").apply { mkdirs() }
    return File(dir, "photo_${System.currentTimeMillis()}.jpg")
}

private fun photoUri(context: Context, file: File): Uri =
    FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)

/** remember 的简写：依赖变了才重算。 */
@Composable
private fun <T> useMemoized(vararg keys: Any?, calculate: () -> T): T {
    return remember(*keys) { calculate() }
}


/** 「从备份恢复」用的系统文件选择器：尽量一打开就停在「文档/知行」（系统不认就从默认位置开始）。 */
private class OpenBackupDocument : ActivityResultContracts.OpenDocument() {
    override fun createIntent(context: android.content.Context, input: Array<String>): android.content.Intent {
        val intent = super.createIntent(context, arrayOf("application/zip", "application/json", "text/plain", "application/octet-stream"))
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) runCatching {
            val initial = android.provider.DocumentsContract.buildDocumentUri(
                "com.android.externalstorage.documents", "primary:" + com.swenhao10.localfirst.data.AutoBackup.FOLDER
            )
            intent.putExtra(android.provider.DocumentsContract.EXTRA_INITIAL_URI, initial)
        }
        return intent
    }
}

/**
 * 第一次打开（没有任何记录）：自动去「文档/知行」找知行能读到的最新备份。
 * 找到了才问一句「之前用过知行？」，点一下就恢复；找不到 / 读不到就什么都不弹（绝不弹文件选择器）。
 * 同时把「文档/知行」文件夹建好，之后的自动备份都放这里。
 */
@Composable
private fun RestorePrompt(db: AppDatabase) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("zhixing_backup", android.content.Context.MODE_PRIVATE) }
    var found by remember { mutableStateOf<com.swenhao10.localfirst.data.AutoBackup.Found?>(null) }
    var restoring by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val AB = com.swenhao10.localfirst.data.AutoBackup
    LaunchedEffect(Unit) {
        val latest = withContext(Dispatchers.IO) {
            val empty = db.records().all().isEmpty() && db.wordProgress().all().isEmpty()
            val hit = if (empty && !prefs.getBoolean("restore_asked", false)) AB.findLatest(context) else null
            if (!empty) prefs.edit().putBoolean("restore_asked", true).apply()
            // 找完再建文件夹（放一个说明小文件），这样不会把说明文件当备份
            AB.ensureFolder(context)
            hit
        }
        found = latest
    }
    val f = found ?: return
    fun close() { found = null; prefs.edit().putBoolean("restore_asked", true).apply() }
    val label = Regex("""(\d{4})(\d{2})(\d{2})-(\d{2})(\d{2})""").find(f.name)?.destructured
        ?.let { (_, m, d, h, mi) -> "${m.toInt()}月${d.toInt()}日 $h:$mi" }
    ZhAlertDialog(
        onDismissRequest = { if (!restoring) close() },
        title = { Text("之前用过知行？") },
        text = { Text("在手机「文档/知行」里找到一份备份" + (label?.let { "（$it）" } ?: "") + "，点「恢复」就能把记录、单词和照片找回来。") },
        confirmButton = {
            TextButton(enabled = !restoring, onClick = {
                restoring = true
                scope.launch {
                    val r = runCatching { withContext(Dispatchers.IO) { com.swenhao10.localfirst.data.DataTransfer.import(context, f.uri, db) } }
                    restoring = false
                    close()
                    android.widget.Toast.makeText(context, r.fold({ "已恢复" }, { "恢复失败：${it.message ?: "未知错误"}" }), android.widget.Toast.LENGTH_LONG).show()
                }
            }) { Text(if (restoring) "正在恢复…" else "恢复") }
        },
        dismissButton = { TextButton(enabled = !restoring, onClick = { close() }) { Text("不用了") } }
    )
}

/** 检查更新：只有点了才联网。 */
@Composable
private fun UpdateDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var state by remember { mutableStateOf("checking") }  // checking | latest | found | downloading | failed | needPerm
    var info by remember { mutableStateOf<com.swenhao10.localfirst.data.Updater.Info?>(null) }
    var progress by remember { mutableStateOf(0f) }
    var error by remember { mutableStateOf("") }
    var apk by remember { mutableStateOf<File?>(null) }
    val U = com.swenhao10.localfirst.data.Updater
    fun check() {
        state = "checking"
        scope.launch {
            val r = runCatching { withContext(Dispatchers.IO) { U.fetchLatest() } }
            r.onSuccess { info = it; state = if (it.versionCode > U.currentVersionCode(context)) "found" else "latest" }
             .onFailure { error = it.message ?: "网络不通"; state = "failed" }
        }
    }
    fun tryInstall(file: File) {
        if (!U.canInstall(context)) { state = "needPerm"; runCatching { context.startActivity(U.installPermissionIntent(context)) }; return }
        runCatching { U.install(context, file) }.onFailure { error = it.message ?: "打不开安装器"; state = "failed" }
    }
    LaunchedEffect(Unit) { check() }
    val i = info
    val closeBtn: (@Composable () -> Unit)? = if (state == "latest" || state == "downloading") null else {
        { TextButton(onClick = onDismiss) { Text("关闭") } }
    }
    ZhAlertDialog(
        onDismissRequest = { if (state != "downloading") onDismiss() },
        title = { Text(when (state) { "checking" -> "正在检查更新"; "latest" -> "已经是最新版"; "found" -> "有新版本 v${i?.versionName}"; "downloading" -> "正在下载"; "needPerm" -> "需要允许安装"; else -> "检查失败" }) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                when (state) {
                    "checking" -> LinearProgressIndicator(Modifier.fillMaxWidth())
                    "latest" -> Text("当前 v${i?.versionName ?: ""} 就是最新的。")
                    "found" -> Text(i?.notes?.ifBlank { "修复和改进" } ?: "", style = MaterialTheme.typography.bodyMedium)
                    "downloading" -> {
                        LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth())
                        Text("${(progress * 100).toInt()}%，GitHub 不通会自动换加速线路", style = MaterialTheme.typography.labelMedium)
                    }
                    "needPerm" -> Text("在系统设置里允许「知行」安装应用，回来再点「安装」。")
                    else -> Text("$error。可能是网络连不上 GitHub 和加速线路，稍后再试，或者直接装我发你的安装包。")
                }
            }
        },
        confirmButton = {
            when (state) {
                "found" -> TextButton(onClick = {
                    val inf = i ?: return@TextButton
                    state = "downloading"; progress = 0f
                    scope.launch {
                        val r = runCatching { withContext(Dispatchers.IO) { U.download(context, inf) { p -> progress = p } } }
                        r.onSuccess { apk = it; state = "found"; tryInstall(it) }.onFailure { error = it.message ?: "下载失败"; state = "failed" }
                    }
                }) { Text(if (apk != null) "安装" else "下载并安装") }
                "needPerm" -> TextButton(onClick = { apk?.let { tryInstall(it) } }) { Text("安装") }
                "failed" -> TextButton(onClick = { check() }) { Text("重试") }
                "downloading", "checking" -> {}
                else -> TextButton(onClick = onDismiss) { Text("好") }
            }
        },
        dismissButton = closeBtn
    )
}

// ======================= 贴纸板编辑 =======================

/** 贴纸板上的一个按钮：玻璃胶囊，按下缩小。 */
@Composable
private fun BoardButton(label: String, icon: ImageVector, enabled: Boolean = true, onClick: () -> Unit) {
    val src = remember { MutableInteractionSource() }
    val shape = RoundedCornerShape(50)
    Row(
        Modifier.pressScale(src)
            .then(if (LocalBackdrop.current != null) Modifier.glass(shape) else Modifier.clip(shape).background(MaterialTheme.colorScheme.surfaceVariant))
            .border(1.dp, Color.White.copy(alpha = .25f), shape)
            .clickable(interactionSource = src, indication = null, enabled = enabled, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp)
            .graphicsLayer { alpha = if (enabled) 1f else .4f },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(6.dp))
        Text(label, style = MaterialTheme.typography.labelLarge)
    }
}

/**
 * 贴纸板编辑：拖动摆放，两指缩放旋转；选中后右下角圆点单指转和缩，上面有删除。
 * 改动 0.5 秒后自动存，并刷新桌面小组件。
 */
@Composable
private fun StickerBoardEditor(db: AppDatabase, onMessage: (String) -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    var board by remember { mutableStateOf(com.swenhao10.localfirst.widget.StickerStore.load(context)) }
    val history = remember { mutableStateListOf<com.swenhao10.localfirst.widget.BoardState>() }
    var selected by remember { mutableStateOf<Long?>(null) }
    var showPhotos by remember { mutableStateOf(false) }
    var noteEdit by remember { mutableStateOf<com.swenhao10.localfirst.widget.Sticker?>(null) }
    var dirty by remember { mutableStateOf(false) }
    fun snapshot() { history.add(board); if (history.size > 40) history.removeAt(0) }
    fun update(id: Long, f: (com.swenhao10.localfirst.widget.Sticker) -> com.swenhao10.localfirst.widget.Sticker) {
        board = board.copy(stickers = board.stickers.map { if (it.id == id) f(it) else it }); dirty = true
    }
    fun bringToFront(id: Long) {
        val s = board.stickers.firstOrNull { it.id == id } ?: return
        if (board.stickers.lastOrNull()?.id != id) board = board.copy(stickers = board.stickers.filter { it.id != id } + s)
    }
    fun add(s: com.swenhao10.localfirst.widget.Sticker) {
        if (board.stickers.size >= 30) { onMessage("一块板最多贴 30 张"); return }
        snapshot(); board = board.copy(stickers = board.stickers + s); selected = s.id; dirty = true
        haptic.performHapticFeedback(HapticFeedbackType.Confirm)
    }
    LaunchedEffect(board) {
        if (!dirty) return@LaunchedEffect
        delay(500)
        withContext(Dispatchers.IO) { com.swenhao10.localfirst.widget.StickerStore.save(context, board) }
        com.swenhao10.localfirst.widget.StickerWidgetBase.refreshAll(context)
    }

    Column(modifier) {
        BoxWithConstraints(
            Modifier.fillMaxWidth().weight(1f, fill = false),
            contentAlignment = Alignment.TopCenter
        ) {
            // 板子按 1:1.2 放进可用空间
            val bw = minOf(maxWidth, maxHeight / com.swenhao10.localfirst.widget.StickerArt.ASPECT)
            val bh = bw * com.swenhao10.localfirst.widget.StickerArt.ASPECT
            val density = LocalDensity.current
            val wPx = with(density) { bw.toPx() }
            val hPx = with(density) { bh.toPx() }
            val basePx = (wPx * com.swenhao10.localfirst.widget.StickerArt.BASE).toInt().coerceAtLeast(40)
            Box(
                Modifier.size(bw, bh)
                    .pointerInput(Unit) { detectTapGestures { selected = null } }
            ) {
                // 板子纹理有几万个颗粒，只在换板子或尺寸变了时画一次，拖贴纸时不重画
                val boardStyle = board.board
                val boardImg by produceState<androidx.compose.ui.graphics.ImageBitmap?>(null, boardStyle, wPx.toInt()) {
                    value = withContext(Dispatchers.Default) {
                        val b = android.graphics.Bitmap.createBitmap(wPx.toInt().coerceAtLeast(1), hPx.toInt().coerceAtLeast(1), android.graphics.Bitmap.Config.ARGB_8888)
                        com.swenhao10.localfirst.widget.StickerArt.drawBoard(android.graphics.Canvas(b), wPx, hPx, boardStyle)
                        b.asImageBitmap()
                    }
                }
                boardImg?.let { Image(it, contentDescription = null, modifier = Modifier.fillMaxSize()) }
                if (board.stickers.isEmpty()) {
                    Column(Modifier.align(Alignment.Center).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("还没有贴纸", fontWeight = FontWeight.Bold, color = Color.White)
                        Text("点下面「照片」或「便签」贴一张", style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = .85f))
                    }
                }
                board.stickers.forEach { s ->
                    key(s.id) {
                        val art by produceState<androidx.compose.ui.graphics.ImageBitmap?>(null, s.artKey, basePx) {
                            value = withContext(Dispatchers.IO) { runCatching { com.swenhao10.localfirst.widget.StickerArt.renderSticker(s, basePx).asImageBitmap() }.getOrNull() }
                        }
                        val aOrNull = art
                        if (aOrNull != null) { val a: androidx.compose.ui.graphics.ImageBitmap = aOrNull
                        val isSel = selected == s.id
                        val lift by animateFloatAsState(if (isSel) 1.04f else 1f, spring(dampingRatio = 0.7f, stiffness = 500f), label = "lift")
                        Box(
                            Modifier
                                .offset { IntOffset((s.x * wPx - a.width / 2f).roundToInt(), (s.y * hPx - a.height / 2f).roundToInt()) }
                                .size(with(density) { a.width.toDp() }, with(density) { a.height.toDp() })
                                .pointerInput(s.id, wPx, hPx) {
                                    awaitEachGesture {
                                        val down = awaitFirstDown()
                                        down.consume()
                                        selected = s.id
                                        snapshot(); bringToFront(s.id)
                                        haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
                                        do {
                                            val ev = awaitPointerEvent()
                                            val pan = ev.calculatePan()
                                            val zoom = ev.calculateZoom()
                                            val rot = ev.calculateRotation()
                                            if (pan != Offset.Zero || zoom != 1f || rot != 0f) {
                                                update(s.id) { cur ->
                                                    cur.copy(
                                                        x = (cur.x + pan.x / wPx).coerceIn(0.02f, 0.98f),
                                                        y = (cur.y + pan.y / hPx).coerceIn(0.06f, 0.98f),
                                                        scale = (cur.scale * zoom).coerceIn(0.35f, 2.6f),
                                                        rot = cur.rot + rot
                                                    )
                                                }
                                            }
                                            ev.changes.forEach { if (it.positionChanged()) it.consume() }
                                        } while (ev.changes.any { it.pressed })
                                    }
                                }
                                .graphicsLayer {
                                    scaleX = s.scale * lift; scaleY = s.scale * lift; rotationZ = s.rot
                                }
                        ) {
                            Image(a, contentDescription = null, modifier = Modifier.fillMaxSize())
                        }
                    }
                    }
                }
                // 选中的贴纸：右下角转/缩把手 + 上方删除
                val sel = board.stickers.firstOrNull { it.id == selected }
                if (sel != null) {
                    val half = basePx * sel.scale * 0.55f
                    val rad = Math.toRadians(sel.rot.toDouble())
                    val cx = sel.x * wPx; val cy = sel.y * hPx
                    val hx = cx + (half * kotlin.math.cos(rad) - half * kotlin.math.sin(rad)).toFloat()
                    val hy = cy + (half * kotlin.math.sin(rad) + half * kotlin.math.cos(rad)).toFloat()
                    val knob = with(density) { 30.dp.toPx() }
                    Box(
                        Modifier
                            .offset { IntOffset((hx - knob / 2).roundToInt(), (hy - knob / 2).roundToInt()) }
                            .size(30.dp)
                            .shadow(4.dp, CircleShape)
                            .clip(CircleShape)
                            .background(Color.White)
                            .padding(5.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                            .pointerInput(sel.id, wPx, hPx) {
                                awaitEachGesture {
                                    val down = awaitFirstDown(); down.consume()
                                    snapshot()
                                    val start = board.stickers.firstOrNull { it.id == sel.id } ?: return@awaitEachGesture
                                    val c0 = Offset(start.x * wPx, start.y * hPx)
                                    val r0 = Math.toRadians(start.rot.toDouble())
                                    val h0 = basePx * start.scale * 0.55f
                                    var finger = Offset(c0.x + (h0 * kotlin.math.cos(r0) - h0 * kotlin.math.sin(r0)).toFloat(), c0.y + (h0 * kotlin.math.sin(r0) + h0 * kotlin.math.cos(r0)).toFloat())
                                    val v0 = finger - c0
                                    val a0 = kotlin.math.atan2(v0.y, v0.x); val d0 = v0.getDistance().coerceAtLeast(1f)
                                    do {
                                        val ev = awaitPointerEvent()
                                        val ch = ev.changes.firstOrNull() ?: break
                                        finger += ch.positionChange()
                                        ch.consume()
                                        val v = finger - c0
                                        val ang = Math.toDegrees((kotlin.math.atan2(v.y, v.x) - a0).toDouble()).toFloat()
                                        val k = v.getDistance() / d0
                                        update(sel.id) { it.copy(rot = start.rot + ang, scale = (start.scale * k).coerceIn(0.35f, 2.6f)) }
                                    } while (ev.changes.any { it.pressed })
                                }
                            }
                    )
                    val del = with(density) { 34.dp.toPx() }
                    Box(
                        Modifier
                            .offset { IntOffset((cx - del / 2).roundToInt(), (cy - half * 1.25f - del).roundToInt().coerceAtLeast(0)) }
                            .size(34.dp)
                            .shadow(4.dp, CircleShape)
                            .clip(CircleShape)
                            .background(Color.White)
                            .clickable {
                                snapshot()
                                board = board.copy(stickers = board.stickers.filter { it.id != sel.id }); dirty = true
                                selected = null
                                haptic.performHapticFeedback(HapticFeedbackType.Confirm)
                            },
                        contentAlignment = Alignment.Center
                    ) { Icon(Icons.Default.Delete, contentDescription = "删除", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp)) }
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        val sel = board.stickers.firstOrNull { it.id == selected }
        AnimatedVisibility(visible = sel != null, enter = fadeIn(tween(150)) + expandVertically(), exit = fadeOut(tween(120)) + shrinkVertically()) {
            Row(Modifier.fillMaxWidth().padding(bottom = 8.dp).horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val cur = sel
                if (cur?.type == "photo") {
                    BoardButton(if (cur.style == "polaroid") "换成白边贴纸" else "换成拍立得", Icons.Default.Image) {
                        snapshot(); update(cur.id) { it.copy(style = if (it.style == "polaroid") "cut" else "polaroid") }
                    }
                }
                if (cur?.type == "note") {
                    BoardButton("改文字", Icons.Default.Edit) { noteEdit = cur }
                }
                if (cur != null) {
                    BoardButton("摆正", Icons.Default.Check) { snapshot(); update(cur.id) { it.copy(rot = 0f) } }
                }
            }
        }
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            BoardButton("照片", Icons.Default.Image) { showPhotos = true }
            BoardButton("便签", Icons.Default.Edit) { noteEdit = com.swenhao10.localfirst.widget.Sticker(id = 0, type = "note") }
            BoardButton("换板子·" + com.swenhao10.localfirst.widget.StickerArt.boardLabel(board.board), Icons.Default.Folder) {
                val all = com.swenhao10.localfirst.widget.StickerArt.BOARDS
                snapshot(); board = board.copy(board = all[(all.indexOf(board.board) + 1) % all.size]); dirty = true
            }
            BoardButton("撤销", Icons.AutoMirrored.Filled.Undo, enabled = history.isNotEmpty()) {
                if (history.isNotEmpty()) { board = history.removeAt(history.size - 1); dirty = true; selected = null }
            }
        }
    }

    if (showPhotos) {
        var paths by remember { mutableStateOf<List<String>?>(null) }
        LaunchedEffect(Unit) {
            paths = withContext(Dispatchers.IO) {
                db.records().all().flatMap { it.images() }.filter { !isVideoPath(it) && File(it).exists() }.distinct().take(90)
            }
        }
        ZhAlertDialog(
            onDismissRequest = { showPhotos = false },
            title = { Text("从记录里选照片") },
            dismissButton = null,
            confirmButton = { TextButton(onClick = { showPhotos = false }) { Text("关闭") } },
            text = {
                val list = paths
                when {
                    list == null -> Text("正在读取…")
                    list.isEmpty() -> Text("记录里还没有照片。先去「记录」拍一张或从相册选一张吧。")
                    else -> FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        list.forEach { p ->
                            val thumb by produceState<androidx.compose.ui.graphics.ImageBitmap?>(null, p) {
                                value = withContext(Dispatchers.IO) { com.swenhao10.localfirst.widget.StickerArt.loadPhoto(p, 240)?.asImageBitmap() }
                            }
                            val src = remember { MutableInteractionSource() }
                            Box(
                                Modifier.size(84.dp).pressScale(src).clip(RoundedCornerShape(14.dp))
                                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = .08f))
                                    .clickable(interactionSource = src, indication = null) {
                                        showPhotos = false
                                        add(com.swenhao10.localfirst.widget.Sticker(
                                            id = System.currentTimeMillis(), type = "photo", path = p,
                                            x = 0.35f + kotlin.random.Random.nextFloat() * 0.3f, y = 0.35f + kotlin.random.Random.nextFloat() * 0.3f,
                                            rot = kotlin.random.Random.nextFloat() * 14f - 7f
                                        ))
                                    }
                            ) {
                                thumb?.let { Image(it, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()) }
                            }
                        }
                    }
                }
            }
        )
    }

    noteEdit?.let { editing ->
        var text by remember(editing.id) { mutableStateOf(editing.text) }
        var color by remember(editing.id) { mutableStateOf(editing.color) }
        ZhAlertDialog(
            onDismissRequest = { noteEdit = null },
            title = { Text(if (editing.id == 0L) "写一张小便签" else "改便签") },
            dismissButton = { TextButton(onClick = { noteEdit = null }) { Text("取消") } },
            confirmButton = {
                Button(enabled = text.isNotBlank(), onClick = {
                    if (editing.id == 0L) {
                        add(editing.copy(id = System.currentTimeMillis(), text = text.trim(), color = color,
                            x = 0.35f + kotlin.random.Random.nextFloat() * 0.3f, y = 0.4f + kotlin.random.Random.nextFloat() * 0.3f,
                            rot = kotlin.random.Random.nextFloat() * 10f - 5f))
                    } else {
                        snapshot(); update(editing.id) { it.copy(text = text.trim(), color = color) }
                    }
                    noteEdit = null
                }) { Text(if (editing.id == 0L) "贴上去" else "改好了") }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = text, onValueChange = { if (it.length <= 60) text = it },
                        placeholder = { Text("今天背了 30 个词") },
                        minLines = 2, maxLines = 4, modifier = Modifier.fillMaxWidth()
                    )
                    Text("纸的颜色", style = MaterialTheme.typography.labelLarge)
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        com.swenhao10.localfirst.widget.StickerArt.NOTE_COLORS.forEach { c ->
                            val on = c == color
                            val ringRaw by animateFloatAsState(if (on) 1f else 0f, spring(dampingRatio = 0.6f, stiffness = 500f), label = "noteRing")
                            val ring = ringRaw.coerceIn(0f, 1f)
                            Box(
                                Modifier.size(38.dp)
                                    .border((2.5f * ring).dp, MaterialTheme.colorScheme.primary.copy(alpha = ring), CircleShape)
                                    .padding((4f * ring).dp)
                                    .clip(CircleShape)
                                    .background(Color(c))
                                    .border(1.dp, Color.Black.copy(alpha = .08f), CircleShape)
                                    .clickable { color = c }
                            )
                        }
                    }
                }
            }
        )
    }
}
