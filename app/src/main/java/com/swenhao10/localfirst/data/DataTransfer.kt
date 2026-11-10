package com.swenhao10.localfirst.data

import android.content.Context
import android.net.Uri
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

object DataTransfer {
    suspend fun export(context: Context, uri: Uri, db: AppDatabase) {
        val root = JSONObject().put("schema_version", 2).put("exported_at", System.currentTimeMillis())
        root.put("cards", JSONArray().also { array -> db.cards().all().forEach { array.put(JSONObject().put("front", it.front).put("back", it.back).put("due_at", it.dueAt)) } })
        root.put("habits", JSONArray().also { array -> db.habits().all().forEach { array.put(JSONObject().put("title", it.title).put("done_on", it.doneOn)) } })
        root.put("expenses", JSONArray().also { array -> db.expenses().all().forEach { array.put(JSONObject().put("title", it.title).put("amount_cents", it.amountCents).put("spent_on", it.spentOn)) } })
        root.put("records", JSONArray().also { array -> db.records().all().forEach { array.put(JSONObject().put("kind", it.kind).put("text", it.text).put("image_path", it.imagePath).put("amount_cents", it.amountCents).put("category", it.category).put("money_kind", it.moneyKind).put("created_at", it.createdAt)) } })
        root.put("unknown_words", JSONArray().also { array -> db.unknownWords().all().forEach { array.put(JSONObject().put("headword", it.headword).put("phonetic", it.phonetic).put("meaning", it.meaning).put("added_at", it.addedAt)) } })
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
        val records = root.optJSONArray("records") ?: JSONArray()
        for (i in 0 until records.length()) {
            val item = records.getJSONObject(i)
            // 图片文件不在备份里：只保留本机还存在的那张图，不存在就置空
            val imagePath = item.optString("image_path").takeIf { it.isNotBlank() }?.let { p -> if (File(p).exists()) p else null }
            db.records().insert(
                RecordEntry(
                    kind = item.optString("kind", "daily"),
                    text = item.optString("text"),
                    imagePath = imagePath,
                    amountCents = if (item.isNull("amount_cents")) null else item.optLong("amount_cents"),
                    category = item.optString("category").takeIf { it.isNotBlank() },
                    moneyKind = item.optString("money_kind").takeIf { it.isNotBlank() },
                    createdAt = item.optLong("created_at", System.currentTimeMillis())
                )
            )
        }
        val unknown = root.optJSONArray("unknown_words") ?: JSONArray()
        for (i in 0 until unknown.length()) {
            val item = unknown.getJSONObject(i)
            val headword = item.optString("headword")
            if (headword.isNotBlank() && db.unknownWords().countByHeadword(headword) == 0) {
                db.unknownWords().insert(UnknownWord(headword = headword, phonetic = item.optString("phonetic").takeIf { it.isNotBlank() }, meaning = item.optString("meaning"), addedAt = item.optLong("added_at", System.currentTimeMillis())))
            }
        }
    }
}
