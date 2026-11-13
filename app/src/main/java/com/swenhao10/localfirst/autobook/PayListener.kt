package com.swenhao10.localfirst.autobook

import android.app.Notification
import android.content.ComponentName
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

/**
 * 读付款通知。只看微信、支付宝、云闪付三个包；其他 App 的通知直接跳过，不读不存。
 * 认出一笔支出后交给 AutoBook：问一下再入账，或直接入账。
 *
 * ColorOS 等系统会在后台把监听服务断开：断开时马上请求重新绑定；重新连上时把通知栏里
 * 还留着的、30 分钟内的付款通知补记一遍（有去重，不会记两次）。
 */
class PayListener : NotificationListenerService() {
    override fun onNotificationPosted(sbn: StatusBarNotification) {
        handle(sbn)
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        AutoBook.markConnected(this)
        val since = System.currentTimeMillis() - 30 * 60_000L
        runCatching { activeNotifications }.getOrNull()
            ?.filter { it.packageName in PayParser.SOURCES && it.postTime >= since }
            ?.sortedBy { it.postTime }
            ?.forEach { handle(it) }
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            runCatching { requestRebind(ComponentName(this, PayListener::class.java)) }
        }
    }

    private fun handle(sbn: StatusBarNotification) {
        if (sbn.packageName !in PayParser.SOURCES) return
        if (!AutoBook.isEnabled(this)) return
        val ex = sbn.notification.extras ?: return
        val title = ex.getCharSequence(Notification.EXTRA_TITLE)?.toString()
        val text = (ex.getCharSequence(Notification.EXTRA_BIG_TEXT) ?: ex.getCharSequence(Notification.EXTRA_TEXT))?.toString()
        val pay = PayParser.parse(sbn.packageName, title, text) ?: return
        if (AutoBook.isDuplicate(this, pay)) return
        AutoBook.handle(this, pay)
    }
}
