package com.swenhao10.localfirst.data

import android.content.Context
import android.net.Uri
import org.json.JSONArray
import org.json.JSONObject

object DataTransfer {
    suspend fun export(context: Context, uri: Uri, db: AppDatabase) {
        val root = JSONObject().put("schema_version", 1).put("exported_at", System.currentTimeMillis())
        root.put("cards", JSONArray().also { array -> db.cards().all().forEach { array.put(JSONObject().put("front", it.front).put("back", it.back).put("due_at", it.dueAt)) } })
        root.put("habits", JSONArray().also { array -> db.habits().all().forEach { array.put(JSONObject().put("title", it.title).put("done_on", it.doneOn)) } })
        root.put("expenses", JSONArray().also { array -> db.expenses().all().forEach { array.put(JSONObject().put("title", it.title).put("amount_cents", it.amountCents).put("spent_on", it.spentOn)) } })
        context.contentResolver.openOutputStream(uri)?.use { it.write(root.toString(2).toByteArray(Charsets.UTF_8)) } ?: error("无法写入导出文件")
    }

    suspend fun import(context: Context, uri: Uri, db: AppDatabase) {
        val text = context.contentResolver.openInputStream(uri)?.use { it.readBytes().toString(Charsets.UTF_8) } ?: error("无法读取备份文件")
        val root = JSONObject(text)
        val cards = root.optJSONArray("cards") ?: JSONArray()
        for (i in 0 until cards.length()) { val item = cards.getJSONObject(i); db.cards().insert(VocabularyCard(front = item.optString("front"), back = item.optString("back"), dueAt = item.optLong("due_at", System.currentTimeMillis()))) }
        val habits = root.optJSONArray("habits") ?: JSONArray()
        for (i in 0 until habits.length()) { val item = habits.getJSONObject(i); db.habits().insert(HabitEntry(title = item.optString("title"), doneOn = item.optString("done_on"))) }
        val expenses = root.optJSONArray("expenses") ?: JSONArray()
        for (i in 0 until expenses.length()) { val item = expenses.getJSONObject(i); db.expenses().insert(ExpenseEntry(title = item.optString("title"), amountCents = item.optLong("amount_cents"), spentOn = item.optString("spent_on"))) }
    }
}

