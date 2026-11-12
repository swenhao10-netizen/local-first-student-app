package com.swenhao10.localfirst.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.RemoteViews
import com.swenhao10.localfirst.MainActivity
import com.swenhao10.localfirst.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * 毛毡板桌面小组件。整块板画成一张图放进 RemoteViews（桌面小组件不支持任意旋转和阴影），
 * 上面盖两块透明点击区：单词卡区域点一下翻面，其他地方点开 App。
 * 刷新时机：放到桌面时、离开 App 时（MainActivity.onStop）、系统每 30 分钟一次。
 */
abstract class FeltWidgetBase(private val wide: Boolean) : AppWidgetProvider() {
    override fun onUpdate(context: Context, mgr: AppWidgetManager, ids: IntArray) {
        val pending = goAsync()
        scope.launch {
            try { ids.forEach { draw(context, mgr, it, wide) } } finally { pending.finish() }
        }
    }

    override fun onAppWidgetOptionsChanged(context: Context, mgr: AppWidgetManager, id: Int, newOptions: Bundle) {
        onUpdate(context, mgr, intArrayOf(id))
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_FLIP) {
            val id = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
            if (id != AppWidgetManager.INVALID_APPWIDGET_ID) {
                val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                prefs.edit().putBoolean("flip_$id", !prefs.getBoolean("flip_$id", false)).apply()
                onUpdate(context, AppWidgetManager.getInstance(context), intArrayOf(id))
            }
            return
        }
        super.onReceive(context, intent)
    }

    override fun onDeleted(context: Context, ids: IntArray) {
        val e = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
        ids.forEach { e.remove("flip_$it") }
        e.apply()
    }

    companion object {
        const val ACTION_FLIP = "com.swenhao10.localfirst.widget.FLIP"
        private const val PREFS = "felt_widget"
        private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

        /** 数据变了（记了一条、背了词）就调一下，所有毛毡板都重画。 */
        fun refreshAll(context: Context) {
            val app = context.applicationContext
            val mgr = AppWidgetManager.getInstance(app)
            scope.launch {
                runCatching {
                    mgr.getAppWidgetIds(ComponentName(app, FeltWidgetWide::class.java)).forEach { draw(app, mgr, it, true) }
                    mgr.getAppWidgetIds(ComponentName(app, FeltWidgetSmall::class.java)).forEach { draw(app, mgr, it, false) }
                }
            }
        }

        private suspend fun draw(context: Context, mgr: AppWidgetManager, id: Int, wide: Boolean) {
            val opts = mgr.getAppWidgetOptions(id)
            val dm = context.resources.displayMetrics
            val wDp = opts.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH).takeIf { it > 0 } ?: if (wide) 320 else 160
            val hDp = opts.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT).takeIf { it > 0 } ?: 160
            // 位图不要太大：RemoteViews 有内存上限，这里最长边压到 1000px 左右
            var wPx = (wDp * dm.density).toInt(); var hPx = (hDp * dm.density).toInt()
            val scale = minOf(1f, 1000f / maxOf(wPx, hPx))
            wPx = (wPx * scale).toInt(); hPx = (hPx * scale).toInt()
            val flipped = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean("flip_$id", false)
            val data = runCatching { FeltBoard.load(context) }.getOrElse {
                FeltData(null, "", "", "日常", emptyList(), null, null, "", "单词卡", 0, 0)
            }
            val bmp = FeltBoard.render(context, data, wPx, hPx, wide, flipped)

            val views = RemoteViews(context.packageName, if (wide) R.layout.widget_felt_wide else R.layout.widget_felt_small)
            views.setImageViewBitmap(R.id.felt_board, bmp)
            val open = PendingIntent.getActivity(
                context, id, Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val cls = if (wide) FeltWidgetWide::class.java else FeltWidgetSmall::class.java
            val flip = PendingIntent.getBroadcast(
                context, id, Intent(context, cls).setAction(ACTION_FLIP).putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.felt_open, open)
            views.setOnClickPendingIntent(R.id.felt_flip, if (data.word != null) flip else open)
            mgr.updateAppWidget(id, views)
        }
    }
}

/** 4×2：照片 + 日常 + 单词卡 + 待复习 + 连续天数。 */
class FeltWidgetWide : FeltWidgetBase(true)

/** 2×2：只放单词卡 + 待复习。 */
class FeltWidgetSmall : FeltWidgetBase(false)
