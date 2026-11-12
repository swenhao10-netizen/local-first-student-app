package com.swenhao10.localfirst.autobook

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import com.swenhao10.localfirst.data.AppDatabase
import kotlinx.coroutines.launch

/** 通知上的按钮：入账、换分类、忽略、撤销。 */
class AutoBookReceiver : BroadcastReceiver() {
    companion object {
        const val CONFIRM = "com.swenhao10.localfirst.autobook.CONFIRM"
        const val NEXT_CAT = "com.swenhao10.localfirst.autobook.NEXT_CAT"
        const val IGNORE = "com.swenhao10.localfirst.autobook.IGNORE"
        const val UNDO = "com.swenhao10.localfirst.autobook.UNDO"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val nid = intent.getIntExtra("nid", 0)
        val pay = Payment(
            amountCents = intent.getLongExtra("cents", 0),
            merchant = intent.getStringExtra("merchant"),
            source = intent.getStringExtra("source") ?: "付款",
            category = intent.getStringExtra("cat") ?: "其他"
        )
        val demo = intent.getBooleanExtra("demo", false)
        val nm = NotificationManagerCompat.from(context)
        when (intent.action) {
            IGNORE -> nm.cancel(nid)
            NEXT_CAT -> {
                val cats = AutoBook.CATS
                val next = cats[(cats.indexOf(pay.category) + 1).mod(cats.size)]
                AutoBook.prompt(context, pay.copy(category = next), nid, demo)
            }
            CONFIRM -> {
                nm.cancel(nid)
                if (pay.amountCents <= 0 || demo) return
                val pending = goAsync()
                AutoBook.io.launch {
                    try { AutoBook.save(context, pay) } finally { pending.finish() }
                }
            }
            UNDO -> {
                nm.cancel(nid)
                val rid = intent.getLongExtra("rid", 0)
                if (rid <= 0) return
                val pending = goAsync()
                AutoBook.io.launch {
                    try { AppDatabase.get(context).records().delete(rid) } finally { pending.finish() }
                }
            }
        }
    }
}
