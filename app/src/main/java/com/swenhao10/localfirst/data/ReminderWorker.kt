package com.swenhao10.localfirst.data

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.swenhao10.localfirst.MainActivity
import java.util.Calendar
import java.util.concurrent.TimeUnit

/**
 * 每日提醒：WorkManager 的 PeriodicWork（24 小时周期），两个提醒各一份独立任务，
 * 到点发一条通知。任务名就是提醒种类，改时间/开关变化时只动对应的那份任务。
 *
 * 触发时间计算：按 SettingsStore 里的提醒时间（默认 21:00）算出下一次到点的时间；
 * 今天的 21:00 还没过就今天发，过了就明天同一时间。周期任务只保证「每天一次、
 * 大约在那个时间」，系统为省电可能前后浮动几分钟到十几分钟，这是 WorkManager
 * 的正常行为，不是 bug。
 */
object ReminderScheduler {
    const val CHANNEL_ID = "zhixing_reminder"
    const val CHANNEL_NAME = "知行提醒"

    const val KIND_REVIEW = "review"
    const val KIND_EXPENSE = "expense"

    /** 每个提醒的唯一任务名。 */
    fun workName(kind: String): String = "reminder_${kind}_daily"

    /** 下一次提醒到点的延迟（毫秒）：今天没过就今天，过就明天。 */
    fun delayToNextReminder(hour: Int, minute: Int, nowMillis: Long = System.currentTimeMillis()): Long {
        val target = Calendar.getInstance().apply {
            timeInMillis = nowMillis
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (target.timeInMillis <= nowMillis) target.add(Calendar.DAY_OF_YEAR, 1)
        return target.timeInMillis - nowMillis
    }

    /** 开启（或改时间后重排）某一个提醒：取消旧任务，按新时间重新排一份每日任务。 */
    fun schedule(context: Context, kind: String) {
        ensureChannel(context)
        val (hour, minute) = SettingsStore.getReminderTime(context)
        val request = PeriodicWorkRequestBuilder<ReminderWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(delayToNextReminder(hour, minute), TimeUnit.MILLISECONDS)
            .setInputData(Data.Builder().putString(ReminderWorker.KEY_KIND, kind).build())
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            workName(kind),
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    /** 关掉某一个提醒：只取消它自己的任务，另一个不受影响。 */
    fun cancel(context: Context, kind: String) {
        WorkManager.getInstance(context).cancelUniqueWork(workName(kind))
    }

    /** 通知渠道只建一个（id zhixing_reminder，名字「知行提醒」），两个提醒共用。 */
    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (manager.getNotificationChannel(CHANNEL_ID) == null) {
            manager.createNotificationChannel(
                NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT)
            )
        }
    }
}

/** 到点执行的任务：按 inputData 里的 kind 发对应的那条提醒。 */
class ReminderWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    companion object {
        const val KEY_KIND = "kind"
        private const val NOTIFICATION_ID_REVIEW = 1001
        private const val NOTIFICATION_ID_EXPENSE = 1002
    }

    override fun doWork(): Result {
        val kind = inputData.getString(KEY_KIND) ?: return Result.failure()
        val (title, text, id) = when (kind) {
            ReminderScheduler.KIND_REVIEW ->
                Triple("该复习单词了", "今天的单词还没复习，打开知行看一眼", NOTIFICATION_ID_REVIEW)
            ReminderScheduler.KIND_EXPENSE ->
                Triple("今天的账记了吗", "花了钱就顺手记一笔，别等忘了", NOTIFICATION_ID_EXPENSE)
            else -> return Result.failure()
        }
        ReminderScheduler.ensureChannel(applicationContext)
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pending = PendingIntent.getActivity(
            applicationContext, id, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(applicationContext, ReminderScheduler.CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(text)
            .setContentIntent(pending)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        return try {
            // 用户在系统设置里关了通知权限时，notify 不抛错也不显示，直接按成功算，
            // 下次周期还会再跑；开关状态本身不受影响。
            NotificationManagerCompat.from(applicationContext).notify(id, notification)
            Result.success()
        } catch (e: SecurityException) {
            Result.success()
        }
    }
}
