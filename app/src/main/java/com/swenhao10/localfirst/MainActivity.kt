package com.swenhao10.localfirst

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
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.material.icons.filled.ChevronRight
import com.swenhao10.localfirst.ui.LaunchAnimation
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import com.swenhao10.localfirst.data.AppDatabase
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        val db = AppDatabase.get(this)
        setContent {
            // 启动动画只在冷启动放一次（转屏等重建不再放），点一下可跳过
            var showSplash by rememberSaveable { mutableStateOf(state == null) }
            Box(Modifier.fillMaxSize()) {
                LocalFirstApp(db)
                if (showSplash) LaunchAnimation(onFinished = { showSplash = false })
            }
        }
    }
}

/** v2 底部导航：记录（首页）/ 单词 / 总览 / 我的 */
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

private fun paletteFor(id: String, dark: Boolean): androidx.compose.material3.ColorScheme {
    val set = APP_PALETTES.firstOrNull { it.id == id } ?: APP_PALETTES.first()
    return if (dark) set.dark else set.light
}

/** 磨砂玻璃主题开关：开着时背景是柔和渐变，卡片、底栏都是半透明的“玻璃”。 */
private val LocalGlass = staticCompositionLocalOf { false }
/** 玻璃要“透过去”看到的那层背景；null = 没开玻璃，退回普通半透明。 */
private val LocalBackdrop = staticCompositionLocalOf<LayerBackdrop?> { null }
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
        Column(
            modifier
                .glass(shape, if (plain) null else color.copy(alpha = 0.40f))
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
            Icon(Icons.Default.Add, contentDescription = "发记录", tint = Color.White, modifier = Modifier.size(30.dp))
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
    if (backdrop != null) {
        LiquidButton(
            onClick = { if (enabled) onClick() },
            backdrop = backdrop,
            tint = tint ?: Color.Unspecified,
            modifier = modifier.graphicsLayer { alpha = if (enabled) 1f else 0.45f },
            content = content
        )
    } else if (tint != null) {
        Button(onClick = onClick, enabled = enabled, modifier = modifier, shape = RoundedCornerShape(50), colors = ButtonDefaults.buttonColors(containerColor = tint)) { content() }
    } else {
        OutlinedButton(onClick = onClick, enabled = enabled, modifier = modifier, shape = RoundedCornerShape(50)) { content() }
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
private fun LocalFirstApp(db: AppDatabase) {
    val scope = rememberCoroutineScope()
    var tab by remember { mutableStateOf(Tab.RECORD) }
    var showAddRecord by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val cardsDao = remember(db) { db.cards() }
    val recordsDao = remember(db) { db.records() }
    val unknownDao = remember(db) { db.unknownWords() }
    val cards by cardsDao.observeAll().collectAsState(initial = emptyList())
    val records by recordsDao.observeAll().collectAsState(initial = emptyList())
    val unknownWords by unknownDao.observeAll().collectAsState(initial = emptyList())
    var packWords by remember { mutableStateOf<List<PackWord>>(emptyList()) }
    var packMeta by remember { mutableStateOf<PackMeta?>(null) }
    var packReady by remember { mutableStateOf(false) }
    // 外观：从 SharedPreferences 读上次选的，重启还生效；跟随系统时读系统当前深浅
    var themeMode by remember { mutableStateOf(SettingsStore.getThemeMode(context)) }
    // 主题颜色：默认青绿（原来的主色），可换海蓝/紫罗兰/暖橙/樱粉
    var paletteId by remember { mutableStateOf(SettingsStore.getPalette(context)) }
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
    // 正在编辑的记录（null = 新发一条）
    var editingRecord by remember { mutableStateOf<RecordEntry?>(null) }
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
    val study = remember { StudyProgress(SettingsStore.getStudyProgress(context)) }
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
        // 内置词包：首次打开把 pack.db 从 assets 复制到本机，再只读加载，离线可用
        withContext(Dispatchers.IO) {
            packMeta = PackStore.loadMeta(context)
            packWords = PackStore.loadWords(context)
            packReady = true
        }
    }

    val baseScheme = paletteFor(paletteId, useDarkTheme)
    MaterialTheme(colorScheme = if (glassOn) glassScheme(baseScheme, useDarkTheme) else baseScheme) {
        val layerBackdrop = rememberLayerBackdrop()
        CompositionLocalProvider(
            LocalGlass provides glassOn,
            LocalBackdrop provides (if (glassOn) layerBackdrop else null),
            LocalBgPath provides bgPath
        ) {
        Box(Modifier.fillMaxSize()) {
        if (glassOn) GlassBackground(useDarkTheme, Modifier.layerBackdrop(layerBackdrop))
        Scaffold(
            containerColor = if (glassOn) Color.Transparent else MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.onBackground,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            bottomBar = {
                if (glassOn) {
                    GlassBottomBar(selected = tab, onSelect = { tab = it }, onAdd = {
                        editingRecord = null
                        showAddRecord = true
                    })
                } else NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                    Tab.values().forEach { item ->
                        NavigationBarItem(
                            selected = tab == item,
                            onClick = { tab = item },
                            icon = { Icon(iconFor(item), contentDescription = item.label) },
                            label = { Text(item.label) },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = Color.Transparent,
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
            AnimatedContent(targetState = tab, label = "tab-transition") { selected ->
                when (selected) {
                    Tab.RECORD -> RecordScreen(
                        records = records,
                        nickname = nickname,
                        avatarPath = avatarPath,
                        onEditProfile = { avatarLauncher.launch("image/*") },
                        onAddRecord = {
                            editingRecord = null
                            showAddRecord = true
                        },
                        onSearch = { message = "搜索这次只放了入口，功能还没做" },
                        modifier = Modifier.padding(padding),
                        onEditRecord = { record ->
                            editingRecord = record
                            showAddRecord = true
                        },
                        onDeleteRecord = { record ->
                            scope.launch {
                                withContext(Dispatchers.IO) {
                                    recordsDao.delete(record.id)
                                    record.images().forEach { p -> runCatching { File(p).delete() } }
                                }
                                message = "这条已经删了"
                            }
                        }
                    )
                    Tab.WORDS -> WordsScreen(
                        packWords = packWords,
                        packMeta = packMeta,
                        packReady = packReady,
                        unknownWords = unknownWords,
                        cards = cards,
                        recordsDao = recordsDao,
                        unknownDao = unknownDao,
                        cardsDao = cardsDao,
                        study = study,
                        modifier = Modifier.padding(padding),
                        onMessage = { message = it }
                    )
                    Tab.OVERVIEW -> OverviewScreen(
                        records = records,
                        wordCount = unknownWords.size + cards.size,
                        modifier = Modifier.padding(padding),
                        onDeleteRecord = { record ->
                            scope.launch {
                                withContext(Dispatchers.IO) {
                                    recordsDao.delete(record.id)
                                    record.images().forEach { p -> runCatching { File(p).delete() } }
                                }
                            }
                        }
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
        LaunchedEffect(message) {
            if (message.isNotBlank()) {
                snackbarHostState.showSnackbar(message)
                message = ""
            }
        }
        if (showAddRecord) {
            val editing = editingRecord
            AddRecordScreen(
                editing = editing,
                onCancel = {
                    showAddRecord = false
                    editingRecord = null
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
                        editingRecord = null
                        message = if (editing == null) "已保存到本机" else "这条已经改好了"
                    }
                }
            )
        }
        if (showProfileMenu) {
            AlertDialog(
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
        }
        }
    }
}

private fun iconFor(tab: Tab): ImageVector = when (tab) {
    Tab.RECORD -> Icons.Default.Home
    Tab.WORDS -> Icons.Default.MenuBook
    Tab.OVERVIEW -> Icons.Default.CalendarMonth
    Tab.DATA -> Icons.Default.Folder
}

/** 记录（首页）：头像昵称头 + 时间轴，按天分组，最新在前。 */
@OptIn(ExperimentalFoundationApi::class)
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
    val avatarBitmap = rememberBitmap(avatarPath, 256)
    LazyColumn(modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        item(key = "header") {
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(52.dp).clip(CircleShape).clickable(onClick = onEditProfile)
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
                    Text(
                        "已记录 ${records.size} 条 · 第 $dayCount 天",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (LocalBackdrop.current != null) {
                    GlassButton(onClick = onSearch, modifier = Modifier.size(46.dp)) {
                        Icon(Icons.Default.Search, contentDescription = "搜索", tint = MaterialTheme.colorScheme.onSurface)
                    }
                } else IconButton(
                    onClick = onSearch,
                    modifier = Modifier.size(42.dp).background(MaterialTheme.colorScheme.surface, CircleShape)
                ) { Icon(Icons.Default.Search, contentDescription = "搜索", tint = MaterialTheme.colorScheme.onSurface) }
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
            item(key = "empty") { EmptyRecordState(onAddRecord = onAddRecord) }
        } else {
            // 按天分组，records 已按时间倒序，groupBy 保持最新一天在前
            val groups = records.groupBy { dayKey(it.createdAt) }
            groups.forEach { (key, dayRecords) ->
                item(key = "day-$key") { DayHeader(time = dayRecords.first().createdAt) }
                items(dayRecords, key = { "rec-${it.id}" }) { record ->
                    TimelineRecord(
                        record = record,
                        onClick = { if (record.kind != "study") onEditRecord(record) },
                        onLongPress = { pendingAction = record },
                        onImageClick = { i -> viewer = record.images() to i }
                    )
                }
            }
        }
        item(key = "tail") { Spacer(Modifier.height(24.dp)) }
    }
    viewer?.let { (paths, start) ->
        ImageViewer(paths = paths, startIndex = start, onDismiss = { viewer = null })
    }
    pendingAction?.let { target ->
        AlertDialog(
            onDismissRequest = { pendingAction = null },
            title = { Text("删除这条记录？") },
            text = { Text(if (target.kind == "study") "背单词自动记的，删了就没了。" else "点一下这条记录可以直接改内容；删了就找不回来。") },
            confirmButton = {
                TextButton(onClick = { onDeleteRecord(target); pendingAction = null }) {
                    Text("删除", color = MaterialTheme.colorScheme.secondary)
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
    AlertDialog(
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
private fun DayHeader(time: Long) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 20.dp, bottom = 8.dp),
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
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(4.dp)
                .size(20.dp)
                .background(MaterialTheme.colorScheme.surface, CircleShape)
                .clip(CircleShape)
                .clickable(onClick = onRemove),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Close, contentDescription = "去掉这张图", modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurface)
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
        val bitmap = rememberBitmap(path)
        if (bitmap != null || video) {
            Box(
                modifier = modifier
                    .fillMaxWidth(0.7f)
                    .aspectRatio(3f / 2f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .then(if (onImageClick != null) Modifier.clickable { onImageClick(0) } else Modifier)
            ) {
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap,
                        contentDescription = if (video) "记录视频" else "记录图片",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                if (video) {
                    PlayBadge()
                    val dur = remember(path) { videoDurationMs(path) }
                    if (dur != null) DurationTag(dur, Modifier.align(Alignment.BottomEnd))
                }
            }
        }
        return
    }
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        paths.chunked(3).forEachIndexed { rowIndex, row ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                row.forEachIndexed { colIndex, path ->
                    val bitmap = rememberBitmap(path)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .then(if (onImageClick != null) Modifier.clickable { onImageClick(rowIndex * 3 + colIndex) } else Modifier)
                    ) {
                        if (bitmap != null) {
                            Image(
                                bitmap = bitmap,
                                contentDescription = "记录图片",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
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
                IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
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
                TextButton(onClick = onCancel, modifier = Modifier.align(Alignment.CenterStart)) {
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
                        onClick = {
                            onPublish(
                                text.trim(), newUris.toList(), keptPaths.toList(),
                                if (recordMoney) amountCents else null,
                                if (recordMoney) category else null,
                                if (recordMoney) moneyKind else null
                            )
                        },
                        enabled = canPublish,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.align(Alignment.CenterEnd)
                    ) { Text(if (editing == null) "发布" else "保存", fontWeight = FontWeight.SemiBold, color = Color.White) }
                } else Button(
                    onClick = {
                        onPublish(
                            text.trim(), newUris.toList(), keptPaths.toList(),
                            if (recordMoney) amountCents else null,
                            if (recordMoney) category else null,
                            if (recordMoney) moneyKind else null
                        )
                    },
                    enabled = canPublish,
                    shape = RoundedCornerShape(50),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 22.dp, vertical = 8.dp),
                    modifier = Modifier.align(Alignment.CenterEnd)
                ) { Text(if (editing == null) "发布" else "保存", fontWeight = FontWeight.SemiBold) }
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
                            Switch(checked = recordMoney, onCheckedChange = { recordMoney = it })
                        }
                        if (recordMoney) {
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .padding(3.dp)
                            ) {
                                listOf("expense" to "支出", "income" to "收入").forEach { (kind, label) ->
                                    val sel = moneyKind == kind
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(11.dp))
                                            .background(if (sel) MaterialTheme.colorScheme.surface else Color.Transparent)
                                            .clickable {
                                                moneyKind = kind
                                                category = if (kind == "income") INCOME_CATEGORIES[0] else EXPENSE_CATEGORIES[0]
                                            }
                                            .padding(vertical = 10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            label,
                                            fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal,
                                            color = if (sel) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
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
                                    Surface(
                                        onClick = { category = c },
                                        shape = RoundedCornerShape(50),
                                        color = if (sel) fill else MaterialTheme.colorScheme.surface,
                                        border = if (sel) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                    ) {
                                        Text(
                                            c,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = if (sel) onFill else MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 9.dp)
                                        )
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
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
        contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
    ) {
        Text(label, style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
    }
}

/** 主题颜色选择：左边一个色点，后面是颜色名，选中态用当前主色填充。 */
@Composable
private fun PaletteChip(label: String, dotColor: Color, selected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
        contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier.size(12.dp).background(dotColor, RoundedCornerShape(6.dp)))
            Spacer(Modifier.width(6.dp))
            Text(label, style = MaterialTheme.typography.labelLarge)
        }
    }
}

/** 单词：内置四六级词包卡片流 + 不记得的单词 + 自己加的卡片。 */
@Composable
private fun WordsScreen(
    packWords: List<PackWord>,
    packMeta: PackMeta?,
    packReady: Boolean,
    unknownWords: List<UnknownWord>,
    cards: List<VocabularyCard>,
    recordsDao: RecordDao,
    unknownDao: UnknownWordDao,
    cardsDao: CardDao,
    study: StudyProgress,
    modifier: Modifier,
    onMessage: (String) -> Unit
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var showAddCard by remember { mutableStateOf(false) }
    // 不记得的单词：收进一个单独页面，单词页上只留一个入口
    var showUnknownPage by remember { mutableStateOf(false) }
    var cardToDelete by remember { mutableStateOf<VocabularyCard?>(null) }
    val total = packWords.size
    // 进度在外层保存（切 Tab 不丢），这里每次变动写回本机
    fun saveProgress() = SettingsStore.setStudyProgress(context, study.snapshot())

    fun finishSession() {
        val reviewed = study.known + study.unknown
        if (reviewed > 0) {
            scope.launch {
                withContext(Dispatchers.IO) {
                    recordsDao.insert(RecordEntry(kind = "study", text = "背了 $reviewed 个词"))
                }
            }
            onMessage("已记到记录里")
        }
        // 退出学习不清零位置：下次点「继续学习」还从这一张接着背
        study.showMeaning = false; study.known = 0; study.unknown = 0; study.inStudy = false
        saveProgress()
    }
    fun nextWord() {
        study.showMeaning = false
        if (study.index + 1 >= total) finishSession() else { study.index += 1; saveProgress() }
    }
    fun startSession(resume: Boolean = true) {
        if (total == 0) {
            onMessage("内置词包还没准备好")
            return
        }
        study.index = study.index.coerceIn(0, total - 1)
        // 继续学习就接着上次的位置；只有「重新开始」才从头来
        if (!resume) { study.index = 0; study.known = 0; study.unknown = 0 }
        study.inStudy = true
        study.showMeaning = false
        saveProgress()
    }

    if (study.inStudy && packWords.isNotEmpty()) {
        val index = study.index.coerceIn(0, total - 1)
        val word = packWords[index]
        val warnColor = MaterialTheme.colorScheme.secondary
        Column(modifier.fillMaxSize().padding(horizontal = 20.dp)) {
            Spacer(Modifier.height(4.dp))
            // 顶栏：左边退出，中间进度计数
            Box(Modifier.fillMaxWidth()) {
                TextButton(
                    onClick = { finishSession() },
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
                    modifier = Modifier.align(Alignment.CenterStart)
                ) { Text("‹ 结束", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold) }
                Text(
                    "${index + 1} / $total",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { if (total == 0) 0f else (index + 1).toFloat() / total },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(50)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                drawStopIndicator = {}
            )
            Spacer(Modifier.height(16.dp))
            // 词卡：标签 / 单词 / 音标 / 分隔线 / 释义 / 来源
            GlassCard(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(28.dp), modifier = Modifier.fillMaxWidth()) {
                Column(
                    Modifier.fillMaxWidth().heightIn(min = 330.dp).padding(22.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(50)) {
                        Text(
                            "四六级单词",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                        )
                    }
                    Spacer(Modifier.height(40.dp))
                    Text(
                        word.headword,
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (!word.phonetic.isNullOrBlank()) {
                        Text(
                            word.phonetic,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Spacer(Modifier.height(40.dp))
                    HorizontalDivider()
                    Spacer(Modifier.height(6.dp))
                    // 意思先藏起来，点过「认识 / 不认识」才显示
                    if (study.showMeaning) {
                        Text(word.meaning, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                    } else {
                        Text(
                            "先想一想，点「认识」或「不认识」后显示意思",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 18.dp)
                        )
                    }
                    Spacer(Modifier.height(20.dp))
                    Text(
                        "来自 四六级词包",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
            }
            Spacer(Modifier.weight(1f))
            // 没作答：不认识（描边）/ 认识（实心）；作答后显示意思，按钮换成「下一个」
            if (LocalBackdrop.current != null) {
                if (!study.showMeaning) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        GlassButton(
                            onClick = {
                                study.unknown += 1
                                study.showMeaning = true
                                saveProgress()
                                scope.launch {
                                    withContext(Dispatchers.IO) {
                                        if (unknownDao.countByHeadword(word.headword) == 0) {
                                            unknownDao.insert(UnknownWord(headword = word.headword, phonetic = word.phonetic, meaning = word.meaning))
                                        }
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f).height(54.dp)
                        ) { Text("不认识", fontWeight = FontWeight.SemiBold, color = warnColor) }
                        GlassButton(
                            onClick = {
                                study.known += 1
                                study.showMeaning = true
                                saveProgress()
                            },
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f).height(54.dp)
                        ) { Text("认识", fontWeight = FontWeight.SemiBold, color = Color.White) }
                    }
                } else {
                    GlassButton(
                        onClick = { nextWord() },
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.fillMaxWidth().height(54.dp)
                    ) { Text(if (index + 1 >= total) "完成" else "下一个", fontWeight = FontWeight.SemiBold, color = Color.White) }
                }
            } else if (!study.showMeaning) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick = {
                            study.unknown += 1
                            study.showMeaning = true
                            saveProgress()
                            scope.launch {
                                withContext(Dispatchers.IO) {
                                    if (unknownDao.countByHeadword(word.headword) == 0) {
                                        unknownDao.insert(UnknownWord(headword = word.headword, phonetic = word.phonetic, meaning = word.meaning))
                                    }
                                }
                            }
                        },
                        modifier = Modifier.weight(1f).height(54.dp),
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = warnColor)
                    ) { Text("不认识", fontWeight = FontWeight.SemiBold) }
                    Button(
                        onClick = {
                            study.known += 1
                            study.showMeaning = true
                            saveProgress()
                        },
                        modifier = Modifier.weight(1f).height(54.dp),
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) { Text("认识", fontWeight = FontWeight.SemiBold) }
                }
            } else {
                Button(
                    onClick = { nextWord() },
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) { Text(if (index + 1 >= total) "完成" else "下一个", fontWeight = FontWeight.SemiBold) }
            }
            Spacer(Modifier.height(20.dp))
        }
        return
    }

    if (showUnknownPage) {
        BackHandler { showUnknownPage = false }
        LazyColumn(modifier.fillMaxSize().padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                Spacer(Modifier.height(4.dp))
                TextButton(
                    onClick = { showUnknownPage = false },
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                ) { Text("‹ 返回", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold) }
                Text("不记得的单词", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Text("共 ${unknownWords.size} 个，记住了就点「已记住」", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (unknownWords.isEmpty()) {
                item { EmptyCard("还没有不记得的单词", "学习时点「不认识」的词会进这里，自己复习") }
            } else {
                items(unknownWords, key = { "unknown-${it.id}" }) { word ->
                    var expanded by remember { mutableStateOf(false) }
                    GlassCard(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(18.dp)) {
                        Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(word.headword, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                                TextButton(onClick = { expanded = !expanded }) { Text(if (expanded) "收起" else "看意思") }
                                TextButton(onClick = {
                                    scope.launch { withContext(Dispatchers.IO) { unknownDao.deleteByHeadword(word.headword) } }
                                }) { Text("已记住") }
                            }
                            if (!word.phonetic.isNullOrBlank()) Text(word.phonetic, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            if (expanded) Text(word.meaning)
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(88.dp)) }
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
            GlassCard(color = MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(24.dp)) {
                Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(packMeta?.name ?: "内置词包", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Text(
                        if (!packReady) "词包加载中…"
                        else if (total == 0) "内置词包还没准备好"
                        else if (study.inStudy) "正在学第 ${study.index + 1} 个"
                        else if (study.index > 0) "上次学到第 ${study.index + 1} 个 / 共 $total 个"
                        else "四六级全部单词 · $total 个 · 离线可用",
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Button(
                            onClick = { startSession(resume = true) },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null)
                            Spacer(Modifier.width(6.dp))
                            Text(if (study.inStudy || study.index > 0) "继续学习" else "开始学习")
                        }
                        if (study.index > 0) {
                            TextButton(onClick = { startSession(resume = false) }) { Text("从头开始", color = MaterialTheme.colorScheme.primary) }
                        }
                    }
                }
            }
        }
        item {
            // 入口卡：点进去才看到全部不记得的单词
            GlassCard(
                onClick = { showUnknownPage = true },
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(18.dp)
            ) {
                Row(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("不记得的单词", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(
                            if (unknownWords.isEmpty()) "背词时点「不认识」的会放这里" else "${unknownWords.size} 个，点进去复习",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text("${unknownWords.size}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("我的卡片", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                IconButton(onClick = { showAddCard = true }) { Icon(Icons.Default.Add, contentDescription = "添加卡片") }
            }
            Text("自己加的词，不走词包", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (cards.isEmpty()) {
            item { EmptyCard("还没有卡片", "点上面 + 添加第一张卡片") }
        } else {
            items(cards, key = { "card-${it.id}" }) { card ->
                GlassCard(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(18.dp)) {
                    ListItem(
                        trailingContent = {
                            IconButton(onClick = { cardToDelete = card }) {
                                Icon(Icons.Default.Delete, contentDescription = "删除这张卡片", tint = MaterialTheme.colorScheme.onSurfaceVariant)
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

    cardToDelete?.let { target ->
        AlertDialog(
            onDismissRequest = { cardToDelete = null },
            title = { Text("删除「${target.front}」？") },
            text = { Text("删了就找不回来。") },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch { withContext(Dispatchers.IO) { cardsDao.delete(target.id) } }
                    cardToDelete = null
                }) { Text("删除", color = MaterialTheme.colorScheme.secondary) }
            },
            dismissButton = { TextButton(onClick = { cardToDelete = null }) { Text("取消") } }
        )
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

/** 总览：顶部两部分——(a) 我记录的事（按月的日历）(b) 收支记录（本月收支，可切月份）。 */
@Composable
private fun OverviewScreen(records: List<RecordEntry>, wordCount: Int, modifier: Modifier, onDeleteRecord: (RecordEntry) -> Unit) {
    val now = Calendar.getInstance()
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
            // 本月结余：深色大卡 + 一条收支比例条
            val glass = LocalGlass.current
            val primary = MaterialTheme.colorScheme.primary
            Card(colors = CardDefaults.cardColors(containerColor = if (glass) Color.Transparent else primary), shape = RoundedCornerShape(24.dp)) {
                Column(
                    Modifier.fillMaxWidth()
                        .then(if (glass) Modifier.background(Brush.linearGradient(listOf(primary, Color(0xFF6A8FC8), Color(0xFF8A72D6)))) else Modifier)
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("本月结余", color = Color.White.copy(alpha = .85f), style = MaterialTheme.typography.labelLarge)
                    Text(formatMoney(income - expense), style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold, color = Color.White)
                    Spacer(Modifier.height(4.dp))
                    val total = (income + expense).takeIf { it > 0 } ?: 1L
                    Box(Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(50)).background(Color.White.copy(alpha = .25f))) {
                        Box(
                            Modifier.fillMaxWidth(income.toFloat() / total).fillMaxHeight()
                                .clip(RoundedCornerShape(50))
                                .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = .55f))
                        )
                    }
                    Row(Modifier.fillMaxWidth()) {
                        Text("收入 ${formatMoney(income)}", color = Color.White.copy(alpha = .9f), style = MaterialTheme.typography.labelMedium, modifier = Modifier.weight(1f))
                        Text("支出 ${formatMoney(expense)}", color = Color.White.copy(alpha = .9f), style = MaterialTheme.typography.labelMedium)
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
                        byCategory.forEach { (cat, cents) ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(cat, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.width(44.dp))
                                Box(Modifier.weight(1f).height(10.dp).clip(RoundedCornerShape(50)).background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))) {
                                    Box(
                                        Modifier.fillMaxWidth((cents.toFloat() / maxValue).coerceIn(0.04f, 1f)).fillMaxHeight()
                                            .clip(RoundedCornerShape(50)).background(categoryColor(cat, primary))
                                    )
                                }
                                Spacer(Modifier.width(10.dp))
                                Text(formatMoney(cents), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
        item { SectionTitle("每天记了什么", "有记录的日子会标出来") }
        item {
            GlassCard(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(20.dp)) {
                Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TextButton(onClick = { shiftMonth(-1) }) { Text("< 上个月") }
                        Text(monthLabel(year, month), fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                        TextButton(onClick = { shiftMonth(1) }) { Text("下个月 >") }
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
                            Text(w, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = androidx.compose.ui.text.style.TextAlign.Center, modifier = Modifier.weight(1f))
                        }
                    }
                    monthCells(year, month).chunked(7).forEach { week ->
                        Row(Modifier.fillMaxWidth()) {
                            week.forEach { day ->
                                val dayRecords = if (day == null) emptyList() else monthRecords.filter { dayOf(it.createdAt) == day }
                                val isToday = isCurrentMonth && day == now.get(Calendar.DAY_OF_MONTH)
                                val selected = isToday
                                Box(
                                    modifier = Modifier.weight(1f).height(62.dp).padding(2.dp)
                                        .background(
                                            when {
                                                selected -> MaterialTheme.colorScheme.primaryContainer
                                                dayRecords.isNotEmpty() -> MaterialTheme.colorScheme.surfaceVariant
                                                else -> Color.Transparent
                                            },
                                            RoundedCornerShape(10.dp)
                                        ),
                                    contentAlignment = Alignment.TopCenter
                                ) {
                                    if (day != null) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(top = 4.dp)) {
                                            Text(
                                                "$day",
                                                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                                fontSize = 13.sp
                                            )
                                            dayRecords.take(2).forEach { r ->
                                                Text(
                                                    labelFor(r),
                                                    fontSize = 9.sp,
                                                    maxLines = 1,
                                                    color = if (r.kind == "expense") MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant
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
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
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
    val saveAllMedia = rememberGallerySaver()
    var showAiExport by remember { mutableStateOf(false) }
    fun exportForAi(days: Int?) {
        scope.launch {
            val result = runCatching {
                val text = withContext(Dispatchers.IO) { AiExport.build(db.records().all(), nickname, days) }
                val dir = File(context.cacheDir, "export").apply { mkdirs() }
                val file = File(dir, "知行_生活记录.md")
                withContext(Dispatchers.IO) { file.writeText(text) }
                // 不太长就顺手复制，粘贴进任何 AI 对话框都行
                if (text.length <= 100_000) {
                    val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    cm.setPrimaryClip(ClipData.newPlainText("知行生活记录", text))
                }
                val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                val send = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_SUBJECT, "我的生活记录（知行导出）")
                    clipData = ClipData.newRawUri("知行生活记录", uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(send, "发给 AI").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                text.length <= 100_000
            }
            onMessage(result.fold({ copied -> if (copied) "已复制，也可以直接分享给 AI App" else "记录太多没法复制，用分享发文件吧" }, { "导出失败：${it.message ?: "未知错误"}" }))
        }
    }
    if (showAiExport) {
        AlertDialog(
            onDismissRequest = { showAiExport = false },
            title = { Text("导出给 AI") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("把记录整理成一份文字，开头带给 AI 的说明。发给任意 AI，它读完就能大致了解你。")
                    Text("只导出文字、账目和背词情况，照片视频只写数量。导出后由你自己决定发给谁。", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    listOf(7 to "最近 7 天", 30 to "最近 30 天", null to "全部记录").forEach { (d, label) ->
                        OutlinedButton(onClick = { showAiExport = false; exportForAi(d) }, modifier = Modifier.fillMaxWidth()) { Text(label) }
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
        }.getOrNull() ?: "0.5.0"
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
    val import = androidx.activity.compose.rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) scope.launch {
            val result = runCatching { withContext(Dispatchers.IO) { DataTransfer.import(context, uri, db) } }
            onMessage(result.fold({ "数据已导入" }, { "导入失败：${it.message ?: "未知错误"}" }))
        }
    }
    LazyColumn(modifier.fillMaxSize().padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("我的", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Text("数据只在这台手机", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.size(38.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("知", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
            }
        }
        item {
            GlassCard(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(20.dp)) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.CloudOff, null, tint = MaterialTheme.colorScheme.primary); Spacer(Modifier.width(8.dp)); Text("离线可用", fontWeight = FontWeight.Bold) }
                    Text("记录、单词、账目都保存在这台手机。换机前请先导出备份。", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
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
                            Text("只用本机图片，不上传", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                            Text("每天到点提醒你复习单词", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(checked = reviewEnabled, onCheckedChange = { setReminder(ReminderScheduler.KIND_REVIEW, it) })
                    }
                    HorizontalDivider()
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("记账提醒", fontWeight = FontWeight.SemiBold)
                            Text("每天到点提醒你记一笔账", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(checked = expenseEnabled, onCheckedChange = { setReminder(ReminderScheduler.KIND_EXPENSE, it) })
                    }
                    HorizontalDivider()
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("提醒时间", fontWeight = FontWeight.SemiBold)
                            Text("两个提醒用同一个时间", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text(timeText(), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(4.dp))
                        TextButton(onClick = { pickReminderTime() }) { Text("改时间") }
                    }
                }
            }
        }
        item {
            Text("外观", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            GlassCard(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(20.dp)) {
                Column(Modifier.fillMaxWidth().padding(16.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ThemeMode.ALL.forEach { mode ->
                            ChoiceChip(ThemeMode.label(mode), themeMode == mode) { onThemeChange(mode) }
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("磨砂玻璃", fontWeight = FontWeight.SemiBold)
                            Text("渐变背景 + 半透明卡片和底栏", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(checked = glassOn, onCheckedChange = onGlassChange)
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("自定义背景", fontWeight = FontWeight.SemiBold)
                            Text(if (hasCustomBg) "正在用你选的图" else "默认渐变；选一张自己的图（会打开磨砂玻璃）", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        if (hasCustomBg) TextButton(onClick = onResetBg) { Text("恢复默认") }
                        TextButton(onClick = onPickBg) { Text("换图片") }
                    }
                    Spacer(Modifier.height(16.dp))
                    Text("主题颜色", fontWeight = FontWeight.SemiBold)
                    Text("默认青绿；换一个色，主色、按钮、底部导航跟着变", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(10.dp))
                    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        APP_PALETTES.forEach { palette ->
                            PaletteChip(
                                label = palette.label,
                                dotColor = palette.light.primary,
                                selected = paletteId == palette.id
                            ) { onPaletteChange(palette.id) }
                        }
                    }
                }
            }
        }
        item {
            Text("备份与恢复", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Button(onClick = { export.launch("zhixing-backup.json") }, modifier = Modifier.fillMaxWidth()) { Text("导出本地备份") }
            OutlinedButton(onClick = { import.launch(arrayOf("application/json", "text/plain")) }, modifier = Modifier.fillMaxWidth()) { Text("从备份恢复") }
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
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            ListItem(leadingContent = { Icon(Icons.Default.Settings, null) }, headlineContent = { Text("隐私说明") }, supportingContent = { Text("首版不上传学习、习惯或账目记录") })
        }
        item {
            Text("关于", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            ListItem(headlineContent = { Text("知行") }, supportingContent = { Text("版本 $versionName") })
        }
        item { Spacer(Modifier.height(88.dp)) }
    }
}

@Composable
private fun AddCardDialog(dismiss: () -> Unit, save: (String, String) -> Unit) {
    var front by remember { mutableStateOf("") }
    var back by remember { mutableStateOf("") }
    AlertDialog(
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
    return remember(path) {
        if (path == null) return@remember null
        if (isVideoPath(path)) return@remember videoFrame(path, maxDim)?.asImageBitmap()
        try {
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(path, bounds)
            if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return@remember null
            var sample = 1
            while (bounds.outWidth / sample > maxDim || bounds.outHeight / sample > maxDim) sample *= 2
            val opts = BitmapFactory.Options().apply { inSampleSize = sample }
            BitmapFactory.decodeFile(path, opts)?.asImageBitmap()
        } catch (e: Exception) {
            null
        }
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
private class StudyProgress(snapshot: StudySnapshot) {
    var index by mutableStateOf(snapshot.index)
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
