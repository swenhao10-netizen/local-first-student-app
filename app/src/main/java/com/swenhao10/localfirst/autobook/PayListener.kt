package com.swenhao10.localfirst.autobook

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

/**
 * 读付款通知。只看微信、支付宝、云闪付三个包；其他 App 的通知直接跳过，不读不存。
 * 认出一笔支出后交给 AutoBook：问一下再入账，或直接入账。
 */
class PayListener : NotificationListenerService() {
    override fun onNotificationPosted(sbn: StatusBarNotification) {
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
