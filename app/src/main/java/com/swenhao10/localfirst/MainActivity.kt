package com.swenhao10.localfirst
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import com.swenhao10.localfirst.data.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() { override fun onCreate(state: Bundle?) { super.onCreate(state); val db = AppDatabase.get(this); setContent { LocalFirstApp(db) } } }
private enum class Tab(val label: String) { CARDS("卡片"), HABITS("习惯·专注"), MONEY("记账"), DATA("我的数据") }
@OptIn(ExperimentalMaterial3Api::class)
@Composable private fun LocalFirstApp(db: AppDatabase) {
    val scope = rememberCoroutineScope(); var tab by remember { mutableStateOf(Tab.CARDS) }; var showAdd by remember { mutableStateOf(false) }
    val cards by db.cards().observeAll().collectAsState(initial = emptyList()); val habits by db.habits().observeAll().collectAsState(initial = emptyList()); val expenses by db.expenses().observeAll().collectAsState(initial = emptyList())
    LaunchedEffect(Unit) { if (db.cards().count() == 0) { db.cards().insert(VocabularyCard(front = "abandon", back = "v. 放弃；抛弃")); db.cards().insert(VocabularyCard(front = "focus", back = "v. 专注；集中")) } }
    MaterialTheme { Scaffold(topBar = { TopAppBar(title = { Text("知行") }) }, floatingActionButton = { if (tab != Tab.DATA) FloatingActionButton({ showAdd = true }) { Icon(Icons.Default.Add, "添加") } }, bottomBar = { NavigationBar { Tab.values().forEach { t -> NavigationBarItem(tab == t, { tab = t }, icon = { Icon(when(t){Tab.CARDS->Icons.Default.Style;Tab.HABITS->Icons.Default.CheckCircle;Tab.MONEY->Icons.Default.AccountBalanceWallet;Tab.DATA->Icons.Default.Folder}, t.label) }, label = { Text(t.label) }) } } }) { pad ->
        when(tab) { Tab.CARDS -> CardsScreen(cards, Modifier.padding(pad)); Tab.HABITS -> HabitsScreen(habits, Modifier.padding(pad)); Tab.MONEY -> MoneyScreen(expenses, Modifier.padding(pad)); Tab.DATA -> DataScreen(db, Modifier.padding(pad)) }
    }
    if(showAdd) AddDialog(tab, { showAdd = false }) { title, back, amount -> scope.launch { when(tab) { Tab.CARDS -> db.cards().insert(VocabularyCard(front=title, back=back)); Tab.HABITS -> db.habits().insert(HabitEntry(title, today())); Tab.MONEY -> db.expenses().insert(ExpenseEntry(title, (amount.toDoubleOrNull()?.times(100) ?: 0).toLong(), today())); Tab.DATA -> Unit }; showAdd=false } }
} }
@Composable private fun CardsScreen(items: List<VocabularyCard>, modifier: Modifier) { Column(modifier.fillMaxSize().padding(16.dp)) { Text("今天要复习什么？", style=MaterialTheme.typography.headlineSmall); Text(if(items.isEmpty()) "还没有卡片，可以先添加原创示例卡或导入词表。" else "待复习 " + items.size + " 张"); Spacer(Modifier.height(12.dp)); LazyColumn { items(items) { Card(Modifier.fillMaxWidth().padding(vertical=4.dp)) { Column(Modifier.padding(16.dp)) { Text(it.front, style=MaterialTheme.typography.titleLarge); Text(it.back) } } } } } }
@Composable private fun HabitsScreen(items: List<HabitEntry>, modifier: Modifier) { Column(modifier.fillMaxSize().padding(16.dp)) { Text("今天想坚持什么？", style=MaterialTheme.typography.headlineSmall); Text("本地记录，不需要账号。"); Spacer(Modifier.height(12.dp)); if(items.isEmpty()) Text("还没有习惯，点击右下角添加第一条。") else LazyColumn { items(items) { ListItem({ Text(it.title) }, supportingContent={Text("已记录 " + it.doneOn)}) } } } }
@Composable private fun MoneyScreen(items: List<ExpenseEntry>, modifier: Modifier) { Column(modifier.fillMaxSize().padding(16.dp)) { Text("今天花了什么？", style=MaterialTheme.typography.headlineSmall); Text("记录保存在这台手机。"); Spacer(Modifier.height(12.dp)); if(items.isEmpty()) Text("还没有账目，点击右下角添加第一笔。") else LazyColumn { items(items) { ListItem({ Text(it.title) }, trailingContent={Text("¥%.2f".format(it.amountCents/100.0))}, supportingContent={Text(it.spentOn)}) } } } }
@Composable private fun DataScreen(db: AppDatabase, modifier: Modifier) {
    val context = LocalContext.current; val scope = rememberCoroutineScope(); var message by remember { mutableStateOf("") }
    val export = androidx.activity.compose.rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri -> if (uri != null) scope.launch { runCatching { DataTransfer.export(context, uri, db); message = "已导出本地数据" }.onFailure { message = "导出失败：" + (it.message ?: "未知错误") } } }
    val import = androidx.activity.compose.rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri -> if (uri != null) scope.launch { runCatching { DataTransfer.import(context, uri, db); message = "已导入数据" }.onFailure { message = "导入失败：" + (it.message ?: "未知错误") } } }
    Column(modifier.fillMaxSize().padding(16.dp)) { Text("我的数据", style=MaterialTheme.typography.headlineSmall); Spacer(Modifier.height(8.dp)); Text("记录保存在本机，无需账号。"); Spacer(Modifier.height(16.dp)); Button(onClick={ import.launch(arrayOf("application/json", "text/plain")) }) { Text("导入数据") }; OutlinedButton(onClick={ export.launch("local-first-student-backup.json") }) { Text("导出 / 备份") }; OutlinedButton(onClick={}) { Text("隐私说明") }; if(message.isNotBlank()) Text(message, modifier=Modifier.padding(top=16.dp)); Text("首版不上传学习、习惯或账目记录。", modifier=Modifier.padding(top=16.dp)) }
}
@Composable private fun AddDialog(tab: Tab, dismiss: () -> Unit, save: (String,String,String)->Unit) { var title by remember { mutableStateOf("") }; var back by remember { mutableStateOf("") }; var amount by remember { mutableStateOf("") }; AlertDialog(onDismissRequest=dismiss, title={Text(when(tab){Tab.CARDS->"添加卡片";Tab.HABITS->"添加习惯";Tab.MONEY->"添加账目";Tab.DATA->""})}, text={Column { OutlinedTextField(title,{title=it},label={Text(if(tab==Tab.MONEY)"项目" else "名称 / 正面")}); if(tab==Tab.CARDS) OutlinedTextField(back,{back=it},label={Text("释义 / 背面")}); if(tab==Tab.MONEY) OutlinedTextField(amount,{amount=it},label={Text("金额（元）")}) }}, confirmButton={TextButton(onClick={save(title,back,amount)}, enabled=title.isNotBlank()){Text("保存")}}, dismissButton={TextButton(onClick={dismiss}){Text("取消")}}) }
private fun today() = SimpleDateFormat("yyyy-MM-dd", Locale.SIMPLIFIED_CHINESE).format(Date())

