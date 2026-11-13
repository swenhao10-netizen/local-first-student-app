package com.swenhao10.localfirst.autobook

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.swenhao10.localfirst.MainActivity
import com.swenhao10.localfirst.R
import com.swenhao10.localfirst.data.AppDatabase
import com.swenhao10.localfirst.data.RecordEntry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * 自动记账：开关、去重、发「记一笔？」通知、真正写进账本。
 * 安卓 16（ColorOS 16）上请求「实时活动」提升，系统会把它放进流体云胶囊；更早的系统是普通悬浮通知。
 */
object AutoBook {
    private const val PREFS = "zhixing_autobook"
    private const val KEY_ON = "enabled"
    private const val KEY_ASK = "ask_first"
    private const val KEY_LAST = "last_sig"
    private const val KEY_LAST_AT = "last_at"
    const val CHANNEL = "autobook"
    private val ACTS = listOf(AutoBookReceiver.CONFIRM, AutoBookReceiver.NEXT_CAT, AutoBookReceiver.IGNORE, AutoBookReceiver.UNDO)
    val CATS = listOf("餐饮", "购物", "交通", "娱乐", "学习", "其他")

    internal val io = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private fun p(c: Context) = c.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    fun isEnabled(c: Context) = p(c).getBoolean(KEY_ON, true)
    fun setEnabled(c: Context, on: Boolean) = p(c).edit().putBoolean(KEY_ON, on).apply()
    fun askFirst(c: Context) = p(c).getBoolean(KEY_ASK, true)
    fun setAskFirst(c: Context, on: Boolean) = p(c).edit().putBoolean(KEY_ASK, on).apply()

    /** 系统设置里是否已给知行「通知使用权」。 */
    fun markConnected(c: Context) = p(c).edit().putLong("connected_at", System.currentTimeMillis()).apply()

    /**
     * 让系统把监听服务重新绑上。打开 App 时和每 15 分钟的保活任务里调用。
     * forceToggle：先禁用再启用组件，系统一定会重新绑定（用于很久没连上的情况）。
     */
    fun ensureBound(c: Context, forceToggle: Boolean = false) {
        if (!hasAccess(c)) return
        val cn = ComponentName(c, PayListener::class.java)
        runCatching {
            if (forceToggle) {
                val pm = c.packageManager
                pm.setComponentEnabledSetting(cn, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP)
                pm.setComponentEnabledSetting(cn, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) android.service.notification.NotificationListenerService.requestRebind(cn)
        }
    }

    /** 是否已允许后台运行（不受电池优化限制）。 */
    fun ignoresBatteryOpt(c: Context): Boolean =
        (c.getSystemService(Context.POWER_SERVICE) as android.os.PowerManager).isIgnoringBatteryOptimizations(c.packageName)

    fun batteryOptIntent(c: Context): Intent =
        Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, android.net.Uri.parse("package:" + c.packageName))

    /** 应用详情页：ColorOS 的「自启动」「后台耗电」都在这里进去。 */
    fun appDetailsIntent(c: Context): Intent =
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, android.net.Uri.parse("package:" + c.packageName))

    /** 每 15 分钟检查一次监听服务，断了就重新绑。 */
    fun scheduleKeepAlive(c: Context) {
        val req = androidx.work.PeriodicWorkRequestBuilder<KeepAliveWorker>(15, java.util.concurrent.TimeUnit.MINUTES).build()
        androidx.work.WorkManager.getInstance(c).enqueueUniquePeriodicWork("zhixing-autobook-keepalive", androidx.work.ExistingPeriodicWorkPolicy.KEEP, req)
    }

    class KeepAliveWorker(ctx: Context, params: androidx.work.WorkerParameters) : androidx.work.Worker(ctx, params) {
        override fun doWork(): Result {
            if (isEnabled(applicationContext)) ensureBound(applicationContext)
            return Result.success()
        }
    }

    fun hasAccess(c: Context) = NotificationManagerCompat.getEnabledListenerPackages(c).contains(c.packageName)

    fun accessSettingsIntent(c: Context): Intent =
        if (Build.VERSION.SDK_INT >= 30)
            Intent(Settings.ACTION_NOTIFICATION_LISTENER_DETAIL_SETTINGS)
                .putExtra(Settings.EXTRA_NOTIFICATION_LISTENER_COMPONENT_NAME, ComponentName(c, PayListener::class.java).flattenToString())
        else Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)

    /** 同一笔钱微信和银行可能各推一次，或者通知被更新：同金额 3 分钟内只算一次。 */
    @Synchronized
    fun isDuplicate(c: Context, pay: Payment): Boolean {
        val now = System.currentTimeMillis()
        val sig = "${pay.amountCents}"
        val pr = p(c)
        val dup = pr.getString(KEY_LAST, null) == sig && now - pr.getLong(KEY_LAST_AT, 0) < 3 * 60_000
        pr.edit().putString(KEY_LAST, sig).putLong(KEY_LAST_AT, now).apply()
        return dup
    }

    fun handle(c: Context, pay: Payment) {
        if (askFirst(c)) prompt(c, pay) else io.launch { val id = save(c, pay); done(c, pay, id) }
    }

    fun money(cents: Long) = "¥" + String.format("%d.%02d", cents / 100, cents % 100)

    private fun channel(c: Context) {
        val nm = c.getSystemService(NotificationManager::class.java)
        if (nm.getNotificationChannel(CHANNEL) == null) {
            nm.createNotificationChannel(NotificationChannel(CHANNEL, "自动记账", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "付款后问你要不要记一笔"
                setShowBadge(false)
            })
        }
    }

    private fun canPost(c: Context) = Build.VERSION.SDK_INT < 33 ||
        ContextCompat.checkSelfPermission(c, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

    private fun action(c: Context, act: String, nid: Int, pay: Payment, recordId: Long = 0, demo: Boolean = false): PendingIntent {
        val i = Intent(c, AutoBookReceiver::class.java).setAction(act)
            .putExtra("nid", nid).putExtra("cents", pay.amountCents).putExtra("merchant", pay.merchant)
            .putExtra("source", pay.source).putExtra("cat", pay.category).putExtra("rid", recordId).putExtra("demo", demo)
        return PendingIntent.getBroadcast(c, nid * 10 + ACTS.indexOf(act) + 1, i, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }

    private fun openApp(c: Context) = PendingIntent.getActivity(
        c, 7, Intent(c, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    /** 「记一笔 ¥36.50？」：入账 / 换分类 / 忽略。10 分钟没理就自己消失。 */
    fun prompt(c: Context, pay: Payment, nidIn: Int = 0, demo: Boolean = false) {
        if (!canPost(c)) return
        channel(c)
        val nid = if (nidIn != 0) nidIn else (System.currentTimeMillis() % 100000).toInt() + 1000
        val amount = money(pay.amountCents)
        val title = (if (demo) "【示例】" else "") + "记一笔 $amount ？"
        val body = listOfNotNull(pay.source, pay.merchant, "分到「${pay.category}」").joinToString(" · ")
        val notif: Notification = if (Build.VERSION.SDK_INT >= 36) {
            Notification.Builder(c, CHANNEL)
                .setSmallIcon(R.drawable.ic_stat_autobook)
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(Notification.BigTextStyle().bigText(body))
                .setOngoing(true)
                // 请求「实时活动」提升（等同 setRequestPromotedOngoing，用 extra 写，老一点的 SDK 也能编译）
                .addExtras(android.os.Bundle().apply { putBoolean("android.requestPromotedOngoing", true) })
                .setShortCriticalText(amount)
                .setCategory(Notification.CATEGORY_STATUS)
                .setTimeoutAfter(10 * 60_000L)
                .setContentIntent(openApp(c))
                .addAction(Notification.Action.Builder(null, "入账", action(c, AutoBookReceiver.CONFIRM, nid, pay, demo = demo)).build())
                .addAction(Notification.Action.Builder(null, "换分类", action(c, AutoBookReceiver.NEXT_CAT, nid, pay, demo = demo)).build())
                .addAction(Notification.Action.Builder(null, "忽略", action(c, AutoBookReceiver.IGNORE, nid, pay, demo = demo)).build())
                .build()
        } else {
            NotificationCompat.Builder(c, CHANNEL)
                .setSmallIcon(R.drawable.ic_stat_autobook)
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(NotificationCompat.BigTextStyle().bigText(body))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setOnlyAlertOnce(nidIn != 0)
                .setAutoCancel(true)
                .setTimeoutAfter(10 * 60_000L)
                .setContentIntent(openApp(c))
                .addAction(0, "入账", action(c, AutoBookReceiver.CONFIRM, nid, pay, demo = demo))
                .addAction(0, "换分类", action(c, AutoBookReceiver.NEXT_CAT, nid, pay, demo = demo))
                .addAction(0, "忽略", action(c, AutoBookReceiver.IGNORE, nid, pay, demo = demo))
                .build()
        }
        runCatching { NotificationManagerCompat.from(c).notify(nid, notif) }
    }

    /** 直接入账后的轻提示，带「撤销」。 */
    fun done(c: Context, pay: Payment, recordId: Long) {
        if (!canPost(c)) return
        channel(c)
        val nid = (System.currentTimeMillis() % 100000).toInt() + 1000
        val n = NotificationCompat.Builder(c, CHANNEL)
            .setSmallIcon(R.drawable.ic_stat_autobook)
            .setContentTitle("已记入 ${money(pay.amountCents)} · ${pay.category}")
            .setContentText(listOfNotNull(pay.source, pay.merchant).joinToString(" · "))
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(true)
            .setTimeoutAfter(60_000L)
            .setContentIntent(openApp(c))
            .addAction(0, "撤销", action(c, AutoBookReceiver.UNDO, nid, pay, recordId))
            .build()
        runCatching { NotificationManagerCompat.from(c).notify(nid, n) }
    }

    suspend fun save(c: Context, pay: Payment): Long =
        AppDatabase.get(c).records().insert(
            RecordEntry(
                kind = "expense",
                text = listOfNotNull(pay.merchant, "（${pay.source}自动记账）").joinToString(" "),
                amountCents = pay.amountCents,
                category = pay.category,
                moneyKind = "expense"
            )
        )
}
