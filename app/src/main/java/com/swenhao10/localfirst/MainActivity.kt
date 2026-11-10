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
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PlayArrow
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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
import com.swenhao10.localfirst.data.RecordDao
import com.swenhao10.localfirst.data.RecordEntry
import com.swenhao10.localfirst.data.ReminderScheduler
import com.swenhao10.localfirst.data.SettingsStore
import com.swenhao10.localfirst.data.ThemeMode
import com.swenhao10.localfirst.data.UnknownWord
import com.swenhao10.localfirst.data.UnknownWordDao
import com.swenhao10.localfirst.data.VocabularyCard
import kotlinx.coroutines.Dispatchers
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
        setContent { LocalFirstApp(AppDatabase.get(this)) }
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

/** 记账分类（默认值，方案待定项） */
private val EXPENSE_CATEGORIES = listOf("餐饮", "购物", "交通", "娱乐", "学习", "其他")
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
    val useDarkTheme = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        else -> isSystemInDarkTheme()
    }

    LaunchedEffect(db) {
        withContext(Dispatchers.IO) {
            if (cardsDao.count() == 0) {
                cardsDao.insert(VocabularyCard(front = "abandon", back = "v. 放弃；抛弃"))
                cardsDao.insert(VocabularyCard(front = "focus", back = "v. 专注；集中"))
            }
        }
        // 内置词包：首次打开把 pack.db 从 assets 复制到本机，再只读加载，离线可用
        withContext(Dispatchers.IO) {
            packMeta = PackStore.loadMeta(context)
            packWords = PackStore.loadWords(context)
            packReady = true
        }
    }

    MaterialTheme(colorScheme = if (useDarkTheme) ZhixingDarkColors else ZhixingColors) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text("知行", fontWeight = FontWeight.Bold)
                            Text("把今天的小事，做成长期的进步", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    },
                    actions = {
                        IconButton(onClick = { message = "所有记录只保存在本机" }) { Icon(Icons.Default.MoreHoriz, contentDescription = "更多") }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
                )
            },
            floatingActionButton = {
                if (tab == Tab.RECORD) {
                    FloatingActionButton(
                        onClick = { showAddRecord = true },
                        shape = RoundedCornerShape(18.dp),
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ) { Icon(Icons.Default.Add, contentDescription = "发记录") }
                }
            },
            floatingActionButtonPosition = FabPosition.Start,
            bottomBar = {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                    Tab.values().forEach { item ->
                        NavigationBarItem(
                            selected = tab == item,
                            onClick = { tab = item },
                            icon = { Icon(iconFor(item), contentDescription = item.label) },
                            label = { Text(item.label) }
                        )
                    }
                }
            }
        ) { padding ->
            AnimatedContent(targetState = tab, label = "tab-transition") { selected ->
                when (selected) {
                    Tab.RECORD -> RecordScreen(
                        records = records,
                        modifier = Modifier.padding(padding),
                        onDeleteRecord = { record ->
                            scope.launch {
                                withContext(Dispatchers.IO) {
                                    recordsDao.delete(record.id)
                                    record.imagePath?.let { File(it).delete() }
                                }
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
                        modifier = Modifier.padding(padding),
                        onMessage = { message = it }
                    )
                    Tab.OVERVIEW -> OverviewScreen(
                        records = records,
                        modifier = Modifier.padding(padding),
                        onDeleteRecord = { record ->
                            scope.launch {
                                withContext(Dispatchers.IO) {
                                    recordsDao.delete(record.id)
                                    record.imagePath?.let { File(it).delete() }
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
                        }
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
    }

    if (showAddRecord) {
        AddRecordDialog(dismiss = { showAddRecord = false }) { text, imageUri, amountCents, category, moneyKind ->
            scope.launch {
                val imagePath = imageUri?.let { copyUriToInternal(context, it, "img_${System.currentTimeMillis()}.jpg") }
                withContext(Dispatchers.IO) {
                    recordsDao.insert(
                        RecordEntry(
                            kind = if (amountCents != null) "expense" else "daily",
                            text = text,
                            imagePath = imagePath,
                            amountCents = amountCents,
                            category = category,
                            moneyKind = moneyKind
                        )
                    )
                }
                showAddRecord = false
                message = "已保存到本机"
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

/** 记录（首页）：朋友圈式信息流，左边时间轴按天分组，最新在前。 */
@Composable
private fun RecordScreen(records: List<RecordEntry>, modifier: Modifier, onDeleteRecord: (RecordEntry) -> Unit) {
    LazyColumn(modifier.fillMaxSize().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Spacer(Modifier.height(4.dp))
            Text("记录", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("把今天的小事记下来，往下翻就是往事", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(6.dp))
        }
        if (records.isEmpty()) {
            item { EmptyCard("还没有记录", "点左下角 + 发第一条：文字、图片、记账都可以") }
        } else {
            // 按天分组，records 已按时间倒序，groupBy 保持最新一天在前
            val groups = records.groupBy { dayKey(it.createdAt) }
            groups.forEach { (_, dayRecords) ->
                item {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 6.dp)) {
                        Box(modifier = Modifier.size(10.dp).background(MaterialTheme.colorScheme.primary, RoundedCornerShape(5.dp)))
                        Spacer(Modifier.width(8.dp))
                        Text(dayHeader(dayRecords.first().createdAt), fontWeight = FontWeight.Bold)
                        Spacer(Modifier.width(8.dp))
                        Text("${dayRecords.size} 条", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                items(dayRecords, key = { it.id }) { record ->
                    RecordItemCard(record = record, onDelete = { onDeleteRecord(record) })
                }
            }
        }
        item { Spacer(Modifier.height(88.dp)) }
    }
}

/** 一条记录：文字 / 图片 / 金额（记账）按有无显示。 */
@Composable
private fun RecordItemCard(record: RecordEntry, onDelete: () -> Unit, showDate: Boolean = false) {
    val bitmap = rememberBitmap(record.imagePath)
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(18.dp)) {
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
            if (bitmap != null) {
                Image(
                    bitmap = bitmap,
                    contentDescription = "记录图片",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxWidth().height(200.dp).background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                )
            }
            if (record.amountCents != null) {
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

/** 发记录：文字、图片、记账全部选填，但至少填一项才能发。 */
@Composable
private fun AddRecordDialog(
    dismiss: () -> Unit,
    save: (text: String, imageUri: Uri?, amountCents: Long?, category: String?, moneyKind: String?) -> Unit
) {
    var text by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var pendingPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var recordMoney by remember { mutableStateOf(false) }
    var amount by remember { mutableStateOf("") }
    var moneyKind by remember { mutableStateOf("expense") }
    var category by remember { mutableStateOf(EXPENSE_CATEGORIES[0]) }
    val context = LocalContext.current
    val categories = if (moneyKind == "income") INCOME_CATEGORIES else EXPENSE_CATEGORIES
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) imageUri = uri
    }
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { ok ->
        if (ok) imageUri = pendingPhotoUri
    }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            val file = createPhotoFile(context)
            val uri = photoUri(context, file)
            pendingPhotoUri = uri
            cameraLauncher.launch(uri)
        }
    }
    val amountCents = amount.toDoubleOrNull()?.let { (it * 100).toLong() }?.takeIf { it > 0 }
    val canSave = text.isNotBlank() || imageUri != null || (recordMoney && amountCents != null)
    AlertDialog(
        onDismissRequest = dismiss,
        title = { Text("发一条记录") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = text, onValueChange = { text = it }, minLines = 3,
                    label = { Text("写点什么（可选）") }, modifier = Modifier.fillMaxWidth()
                )
                Text("图片（可选）", style = MaterialTheme.typography.labelLarge)
                if (imageUri == null) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { galleryLauncher.launch("image/*") }) {
                            Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp)); Text("从相册选")
                        }
                        OutlinedButton(onClick = { permissionLauncher.launch(android.Manifest.permission.CAMERA) }) {
                            Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp)); Text("拍照")
                        }
                    }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("已选 1 张图", color = MaterialTheme.colorScheme.primary)
                        TextButton(onClick = { imageUri = null }) { Text("去掉") }
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("记一笔账（可选）", style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(1f))
                    Switch(checked = recordMoney, onCheckedChange = { recordMoney = it })
                }
                if (recordMoney) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ChoiceChip("支出", moneyKind == "expense") { moneyKind = "expense"; category = EXPENSE_CATEGORIES[0] }
                        ChoiceChip("收入", moneyKind == "income") { moneyKind = "income"; category = INCOME_CATEGORIES[0] }
                    }
                    OutlinedTextField(
                        value = amount, onValueChange = { amount = it }, singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        label = { Text("金额（元）") }, modifier = Modifier.fillMaxWidth()
                    )
                    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        categories.forEach { c -> ChoiceChip(c, category == c) { category = c } }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    save(
                        text.trim(), imageUri,
                        if (recordMoney) amountCents else null,
                        if (recordMoney) category else null,
                        if (recordMoney) moneyKind else null
                    )
                },
                enabled = canSave
            ) { Text("发布") }
        },
        dismissButton = { TextButton(onClick = dismiss) { Text("取消") } }
    )
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
    modifier: Modifier,
    onMessage: (String) -> Unit
) {
    val scope = rememberCoroutineScope()
    var phase by remember { mutableStateOf("list") }
    var index by remember { mutableStateOf(0) }
    var showMeaning by remember { mutableStateOf(false) }
    var knownCount by remember { mutableStateOf(0) }
    var unknownCount by remember { mutableStateOf(0) }
    var showAddCard by remember { mutableStateOf(false) }
    val total = packWords.size

    fun finishSession() {
        val reviewed = knownCount + unknownCount
        if (reviewed > 0) {
            scope.launch {
                withContext(Dispatchers.IO) {
                    recordsDao.insert(RecordEntry(kind = "study", text = "背单词：学习了 $reviewed 个词，认识 $knownCount 个，不认识 $unknownCount 个"))
                }
            }
            onMessage("已记到记录里")
        }
        phase = "list"; index = 0; showMeaning = false; knownCount = 0; unknownCount = 0
    }
    fun nextWord() {
        showMeaning = false
        if (index + 1 >= total) finishSession() else index++
    }

    if (phase == "study" && packWords.isNotEmpty()) {
        val word = packWords[index.coerceIn(0, total - 1)]
        LazyColumn(modifier.fillMaxSize().padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                Spacer(Modifier.height(4.dp))
                Text("背单词", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text("第 ${index + 1} / $total 个 · 认识 $knownCount · 不认识 $unknownCount", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            item {
                LinearProgressIndicator(progress = { if (total == 0) 0f else index.toFloat() / total }, modifier = Modifier.fillMaxWidth())
            }
            item {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(24.dp)) {
                    Column(Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(word.headword, style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)
                        if (!word.phonetic.isNullOrBlank()) Text(word.phonetic, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        if (showMeaning) {
                            HorizontalDivider()
                            Text(word.meaning, style = MaterialTheme.typography.bodyLarge)
                        } else {
                            Text("认识吗？", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
            item {
                if (!showMeaning) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = { knownCount++; nextWord() },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) { Text("认识") }
                        OutlinedButton(
                            onClick = {
                                showMeaning = true
                                unknownCount++
                                scope.launch {
                                    withContext(Dispatchers.IO) {
                                        if (unknownDao.countByHeadword(word.headword) == 0) {
                                            unknownDao.insert(UnknownWord(headword = word.headword, phonetic = word.phonetic, meaning = word.meaning))
                                        }
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) { Text("不认识") }
                    }
                } else {
                    Button(onClick = { nextWord() }, modifier = Modifier.fillMaxWidth()) { Text("下一个") }
                }
            }
            item {
                OutlinedButton(onClick = { finishSession() }, modifier = Modifier.fillMaxWidth()) { Text("结束学习") }
                Spacer(Modifier.height(88.dp))
            }
        }
        return
    }

    LazyColumn(modifier.fillMaxSize().padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Spacer(Modifier.height(4.dp))
            Text("背单词", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("一词一卡，认识就下一个", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer), shape = RoundedCornerShape(24.dp)) {
                Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(packMeta?.name ?: "内置词包", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Text(
                        if (!packReady) "词包加载中…"
                        else if (total == 0) "内置词包还没准备好"
                        else "四六级全部单词 · $total 个 · 离线可用",
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Button(
                        onClick = {
                            if (total > 0) { phase = "study"; index = 0; showMeaning = false; knownCount = 0; unknownCount = 0 }
                            else onMessage("内置词包还没准备好")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null); Spacer(Modifier.width(6.dp)); Text("开始学习")
                    }
                }
            }
        }
        item { SectionTitle("不记得的单词", "${unknownWords.size} 个") }
        if (unknownWords.isEmpty()) {
            item { EmptyCard("还没有不记得的单词", "学习时点「不认识」的词会进这里，自己复习") }
        } else {
            items(unknownWords, key = { it.id }) { word ->
                var expanded by remember { mutableStateOf(false) }
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(18.dp)) {
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
            items(cards, key = { it.id }) { card ->
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(18.dp)) {
                    ListItem(
                        headlineContent = { Text(card.front, fontWeight = FontWeight.SemiBold) },
                        supportingContent = { Text(card.back, color = MaterialTheme.colorScheme.onSurfaceVariant) },
                        leadingContent = { Surface(color = MaterialTheme.colorScheme.secondaryContainer, shape = RoundedCornerShape(12.dp)) { Icon(Icons.Default.MenuBook, null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.padding(10.dp)) } }
                    )
                }
            }
        }
        item { Spacer(Modifier.height(88.dp)) }
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
private fun OverviewScreen(records: List<RecordEntry>, modifier: Modifier, onDeleteRecord: (RecordEntry) -> Unit) {
    val now = Calendar.getInstance()
    var year by remember { mutableStateOf(now.get(Calendar.YEAR)) }
    var month by remember { mutableStateOf(now.get(Calendar.MONTH)) }
    var selectedDay by remember { mutableStateOf<Int?>(now.get(Calendar.DAY_OF_MONTH)) }
    val monthRecords = useMemoized(records, year, month) { records.filter { monthOf(it.createdAt) == year to month } }
    val income = monthRecords.filter { it.kind == "expense" && it.moneyKind == "income" }.sumOf { it.amountCents ?: 0 }
    val expense = monthRecords.filter { it.kind == "expense" && it.moneyKind == "expense" }.sumOf { it.amountCents ?: 0 }
    val isCurrentMonth = year == now.get(Calendar.YEAR) && month == now.get(Calendar.MONTH)

    fun shiftMonth(delta: Int) {
        val cal = Calendar.getInstance().apply { set(year, month, 1); add(Calendar.MONTH, delta) }
        year = cal.get(Calendar.YEAR); month = cal.get(Calendar.MONTH)
        selectedDay = null
    }

    LazyColumn(modifier.fillMaxSize().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Spacer(Modifier.height(4.dp))
            Text("总览", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("看看这个月都记了什么、花了多少", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        item { SectionTitle("我记录的事", "点一天，看当天的记录") }
        item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(20.dp)) {
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
                                val selected = selectedDay == day && day != null
                                Box(
                                    modifier = Modifier.weight(1f).height(62.dp).padding(2.dp)
                                        .background(
                                            when {
                                                selected -> MaterialTheme.colorScheme.primaryContainer
                                                dayRecords.isNotEmpty() -> MaterialTheme.colorScheme.surfaceVariant
                                                else -> Color.Transparent
                                            },
                                            RoundedCornerShape(10.dp)
                                        )
                                        .clickable(enabled = day != null) { selectedDay = day },
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
        if (selectedDay != null) {
            val dayRecords = monthRecords.filter { dayOf(it.createdAt) == selectedDay }
            item { SectionTitle("${month + 1}月${selectedDay}日", "${dayRecords.size} 条") }
            if (dayRecords.isEmpty()) {
                item { EmptyCard("这天没有记录", "去记录里发一条吧") }
            } else {
                items(dayRecords, key = { it.id }) { record ->
                    RecordItemCard(record = record, onDelete = { onDeleteRecord(record) })
                }
            }
        }
        item { SectionTitle("收支记录", monthLabel(year, month)) }
        item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary), shape = RoundedCornerShape(24.dp)) {
                Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("本月结余", color = Color.White.copy(alpha = .85f))
                    Text(formatMoney(income - expense), style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold, color = Color.White)
                    Row {
                        Text("收入 ${formatMoney(income)}", color = Color.White.copy(alpha = .9f), modifier = Modifier.weight(1f))
                        Text("支出 ${formatMoney(expense)}", color = Color.White.copy(alpha = .9f))
                    }
                    Text("只按记录里的账统计", color = Color.White.copy(alpha = .7f), style = MaterialTheme.typography.labelMedium)
                }
            }
        }
        item { Spacer(Modifier.height(88.dp)) }
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
    val versionName = remember {
        runCatching {
            @Suppress("DEPRECATION")
            context.packageManager.getPackageInfo(context.packageName, 0).versionName
        }.getOrNull() ?: "0.3.0"
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
            Text("我的数据", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("不注册、不上传，数据由你掌控", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(20.dp)) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.CloudOff, null, tint = MaterialTheme.colorScheme.primary); Spacer(Modifier.width(8.dp)); Text("离线可用", fontWeight = FontWeight.Bold) }
                    Text("记录、单词、账目都保存在这台手机。换机前请先导出备份。", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        item {
            Text("通知设置", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(20.dp)) {
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
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ThemeMode.ALL.forEach { mode ->
                    ChoiceChip(ThemeMode.label(mode), themeMode == mode) { onThemeChange(mode) }
                }
            }
        }
        item {
            Text("备份与恢复", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Button(onClick = { export.launch("zhixing-backup.json") }, modifier = Modifier.fillMaxWidth()) { Text("导出本地备份") }
            OutlinedButton(onClick = { import.launch(arrayOf("application/json", "text/plain")) }, modifier = Modifier.fillMaxWidth()) { Text("从备份恢复") }
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
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant), shape = RoundedCornerShape(18.dp)) {
        Column(Modifier.fillMaxWidth().padding(20.dp)) { Text(title, fontWeight = FontWeight.SemiBold); Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant) }
    }
}

private fun formatMoney(cents: Long): String = "¥%.2f".format(Locale.CHINA, cents / 100.0)

private fun formatMoneyShort(cents: Long): String =
    if (cents % 100 == 0L) "¥${cents / 100}" else "¥${String.format(Locale.CHINA, "%.1f", cents / 100.0)}"

private fun dayKey(time: Long): String = SimpleDateFormat("yyyy-MM-dd", Locale.SIMPLIFIED_CHINESE).format(Date(time))
private fun dayHeader(time: Long): String = SimpleDateFormat("M月d日 EEEE", Locale.SIMPLIFIED_CHINESE).format(Date(time))
private fun timeText(time: Long): String = SimpleDateFormat("HH:mm", Locale.SIMPLIFIED_CHINESE).format(Date(time))
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
    else -> if (record.text.isBlank()) "图片" else record.text.take(4)
}

/** 按图片路径解码一张缩略图（本机文件不存在或损坏返回 null）。 */
@Composable
private fun rememberBitmap(path: String?, maxDim: Int = 1024): androidx.compose.ui.graphics.ImageBitmap? {
    return remember(path) {
        if (path == null) return@remember null
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
