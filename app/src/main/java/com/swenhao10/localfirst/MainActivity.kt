package com.swenhao10.localfirst

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.swenhao10.localfirst.data.AppDatabase
import com.swenhao10.localfirst.data.DataTransfer
import com.swenhao10.localfirst.data.ExpenseEntry
import com.swenhao10.localfirst.data.HabitEntry
import com.swenhao10.localfirst.data.VocabularyCard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        setContent { LocalFirstApp(AppDatabase.get(this)) }
    }
}

private enum class Tab(val label: String) { CARDS("卡片"), HABITS("习惯"), MONEY("记账"), DATA("我的数据") }

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LocalFirstApp(db: AppDatabase) {
    val scope = rememberCoroutineScope()
    var tab by remember { mutableStateOf(Tab.CARDS) }
    var showAdd by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }
    val cardsDao = remember(db) { db.cards() }
    val habitsDao = remember(db) { db.habits() }
    val expensesDao = remember(db) { db.expenses() }
    val cards by cardsDao.observeAll().collectAsState(initial = emptyList())
    val habits by habitsDao.observeAll().collectAsState(initial = emptyList())
    val expenses by expensesDao.observeAll().collectAsState(initial = emptyList())

    LaunchedEffect(cardsDao) {
        withContext(Dispatchers.IO) {
            if (cardsDao.count() == 0) {
                cardsDao.insert(VocabularyCard(front = "abandon", back = "v. 放弃；抛弃"))
                cardsDao.insert(VocabularyCard(front = "focus", back = "v. 专注；集中"))
            }
        }
    }

    MaterialTheme(colorScheme = ZhixingColors) {
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
                if (tab != Tab.DATA) {
                    FloatingActionButton(
                        onClick = { showAdd = true },
                        shape = RoundedCornerShape(18.dp),
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ) { Icon(Icons.Default.Add, contentDescription = "添加") }
                }
            },
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
                    Tab.CARDS -> CardsScreen(cards, Modifier.padding(padding)) { message = it }
                    Tab.HABITS -> HabitsScreen(habits, Modifier.padding(padding))
                    Tab.MONEY -> MoneyScreen(expenses, Modifier.padding(padding))
                    Tab.DATA -> DataScreen(db, Modifier.padding(padding)) { message = it }
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

    if (showAdd) {
        AddDialog(tab, dismiss = { showAdd = false }) { title, back, amount ->
            scope.launch {
                withContext(Dispatchers.IO) {
                    when (tab) {
                        Tab.CARDS -> cardsDao.insert(VocabularyCard(front = title, back = back))
                        Tab.HABITS -> habitsDao.insert(HabitEntry(title = title, doneOn = today()))
                        Tab.MONEY -> expensesDao.insert(ExpenseEntry(title = title, amountCents = ((amount.toDoubleOrNull() ?: 0.0) * 100).toLong(), spentOn = today()))
                        Tab.DATA -> Unit
                    }
                }
                showAdd = false
                message = "已保存到本机"
            }
        }
    }
}

private fun iconFor(tab: Tab): ImageVector = when (tab) {
    Tab.CARDS -> Icons.Default.MenuBook
    Tab.HABITS -> Icons.Default.CheckCircle
    Tab.MONEY -> Icons.Default.AccountBalanceWallet
    Tab.DATA -> Icons.Default.Folder
}

@Composable
private fun CardsScreen(items: List<VocabularyCard>, modifier: Modifier, onMessage: (String) -> Unit) {
    LazyColumn(modifier.fillMaxSize().padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Spacer(Modifier.height(4.dp))
            Text("今天先完成一小步", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("离线也能用，进度始终在你手里", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer), shape = RoundedCornerShape(24.dp)) {
                Column(Modifier.fillMaxWidth().padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(color = MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(14.dp), modifier = Modifier.size(48.dp)) {
                            Icon(Icons.Default.MenuBook, contentDescription = null, tint = Color.White, modifier = Modifier.padding(12.dp))
                        }
                        Spacer(Modifier.width(12.dp))
                        Column { Text("今日复习", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onPrimaryContainer); Text("${items.size} 张卡片", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer) }
                    }
                    Spacer(Modifier.height(14.dp))
                    LinearProgressIndicator(progress = { if (items.isEmpty()) 0f else 0.28f }, modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.primary, trackColor = Color.White.copy(alpha = .55f))
                    Spacer(Modifier.height(14.dp))
                    Button(onClick = { onMessage("已进入今日复习") }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null); Spacer(Modifier.width(6.dp)); Text("开始复习")
                    }
                }
            }
        }
        item { SectionTitle("我的卡片", "可导入词表，也可以直接添加") }
        if (items.isEmpty()) {
            item { EmptyCard("还没有卡片", "点击右下角 + 添加第一张卡片") }
        } else {
            items(items, key = { it.id }) { card ->
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
}

@Composable
private fun HabitsScreen(items: List<HabitEntry>, modifier: Modifier) {
    LazyColumn(modifier.fillMaxSize().padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Spacer(Modifier.height(4.dp))
            Text("习惯与专注", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("让想做的事，变成今天能完成的事", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer), shape = RoundedCornerShape(24.dp)) {
                Row(Modifier.fillMaxWidth().padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                    Surface(color = MaterialTheme.colorScheme.secondary, shape = RoundedCornerShape(14.dp), modifier = Modifier.size(48.dp)) { Icon(Icons.Default.Timer, null, tint = Color.White, modifier = Modifier.padding(12.dp)) }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) { Text("专注 25 分钟", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold); Text("今天完成一次，就算赢", color = MaterialTheme.colorScheme.onSecondaryContainer) }
                    IconButton(onClick = {}) { Icon(Icons.Default.PlayArrow, contentDescription = "开始专注") }
                }
            }
        }
        item { SectionTitle("今天的习惯", "${items.size} 条本地记录") }
        if (items.isEmpty()) item { EmptyCard("还没有习惯", "点击右下角 + 添加一个想坚持的目标") }
        else items(items, key = { it.id }) { habit ->
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(18.dp)) {
                ListItem(
                    headlineContent = { Text(habit.title, fontWeight = FontWeight.SemiBold) },
                    supportingContent = { Text("已记录 · ${habit.doneOn}") },
                    leadingContent = { Icon(Icons.Default.CheckCircle, null, tint = MaterialTheme.colorScheme.primary) }
                )
            }
        }
        item { Spacer(Modifier.height(88.dp)) }
    }
}

@Composable
private fun MoneyScreen(items: List<ExpenseEntry>, modifier: Modifier) {
    val total = items.sumOf { it.amountCents }
    LazyColumn(modifier.fillMaxSize().padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Spacer(Modifier.height(4.dp))
            Text("轻量记账", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("记下每一笔，也看见自己的选择", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary), shape = RoundedCornerShape(24.dp)) {
                Column(Modifier.fillMaxWidth().padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.Savings, null, tint = Color.White); Spacer(Modifier.width(8.dp)); Text("本地累计支出", color = Color.White.copy(alpha = .85f)) }
                    Text(formatMoney(total), style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("数据只保存在这台设备", color = Color.White.copy(alpha = .8f))
                }
            }
        }
        item { SectionTitle("最近记录", "${items.size} 笔") }
        if (items.isEmpty()) item { EmptyCard("还没有账目", "点击右下角 + 添加第一笔") }
        else items(items, key = { it.id }) { expense ->
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(18.dp)) {
                ListItem(
                    headlineContent = { Text(expense.title, fontWeight = FontWeight.SemiBold) },
                    supportingContent = { Text(expense.spentOn) },
                    trailingContent = { Text(formatMoney(expense.amountCents), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold) },
                    leadingContent = { Icon(Icons.Default.AccountBalanceWallet, null, tint = MaterialTheme.colorScheme.primary) }
                )
            }
        }
        item { Spacer(Modifier.height(88.dp)) }
    }
}

@Composable
private fun DataScreen(db: AppDatabase, modifier: Modifier, onMessage: (String) -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
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
                    Text("学习卡片、习惯和账目都保存在这台手机。换机前请先导出备份。", color = MaterialTheme.colorScheme.onSurfaceVariant)
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
        item { Spacer(Modifier.height(88.dp)) }
    }
}

@Composable
private fun AddDialog(tab: Tab, dismiss: () -> Unit, save: (String, String, String) -> Unit) {
    var title by remember { mutableStateOf("") }
    var back by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = dismiss,
        title = { Text(when (tab) { Tab.CARDS -> "添加学习卡片"; Tab.HABITS -> "添加习惯"; Tab.MONEY -> "添加账目"; Tab.DATA -> "" }) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(value = title, onValueChange = { title = it }, singleLine = true, label = { Text(if (tab == Tab.MONEY) "项目" else "名称 / 正面") })
                if (tab == Tab.CARDS) OutlinedTextField(value = back, onValueChange = { back = it }, minLines = 2, label = { Text("释义 / 背面") })
                if (tab == Tab.MONEY) OutlinedTextField(value = amount, onValueChange = { amount = it }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), label = { Text("金额（元）") })
            }
        },
        confirmButton = { TextButton(onClick = { save(title.trim(), back.trim(), amount.trim()) }, enabled = title.isNotBlank()) { Text("保存") } },
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
private fun today(): String = SimpleDateFormat("yyyy-MM-dd", Locale.SIMPLIFIED_CHINESE).format(Date())

