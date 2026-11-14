package com.swenhao10.localfirst.data

import android.content.Context
import android.net.Uri
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

object DataTransfer {
    suspend fun export(context: Context, uri: Uri, db: AppDatabase) {
        val root = buildJson(db, context)
        context.contentResolver.openOutputStream(uri)?.use { it.write(root.toString(2).toByteArray(Charsets.UTF_8)) } ?: error("无法写入导出文件")
    }

    /** 全部数据转成 JSON。图片只写文件名（media_names），配合 zip 备份里的 media/ 目录恢复。 */
    suspend fun buildJson(db: AppDatabase, context: Context): JSONObject {
        val root = JSONObject().put("schema_version", 3).put("exported_at", System.currentTimeMillis())
        root.put("cards", JSONArray().also { array -> db.cards().all().forEach { array.put(JSONObject().put("front", it.front).put("back", it.back).put("due_at", it.dueAt)) } })
        root.put("habits", JSONArray().also { array -> db.habits().all().forEach { array.put(JSONObject().put("title", it.title).put("done_on", it.doneOn)) } })
        root.put("expenses", JSONArray().also { array -> db.expenses().all().forEach { array.put(JSONObject().put("title", it.title).put("amount_cents", it.amountCents).put("spent_on", it.spentOn)) } })
        root.put("records", JSONArray().also { array -> db.records().all().forEach { array.put(JSONObject().put("kind", it.kind).put("text", it.text).put("image_path", it.imagePath).put("image_paths", it.imagePaths).put("media_names", JSONArray(it.images().map { p -> File(p).name })).put("amount_cents", it.amountCents).put("category", it.category).put("money_kind", it.moneyKind).put("created_at", it.createdAt).put("mood_id", it.moodId)) } })
        root.put("moods", MoodStore.toJson(context))
        root.put("unknown_words", JSONArray().also { array -> db.unknownWords().all().forEach { array.put(JSONObject().put("headword", it.headword).put("phonetic", it.phonetic).put("meaning", it.meaning).put("added_at", it.addedAt).put("known_count", it.knownCount)) } })
        root.put("word_progress", JSONArray().also { array -> db.wordProgress().all().forEach { array.put(JSONObject().put("headword", it.headword).put("phonetic", it.phonetic).put("meaning", it.meaning).put("stage", it.stage).put("due_at", it.dueAt).put("wrong_count", it.wrongCount).put("first_seen", it.firstSeen).put("last_seen", it.lastSeen)) } })
        root.put("study_days", JSONArray().also { array -> db.studyDays().all().forEach { array.put(JSONObject().put("day", it.day).put("new_count", it.newCount).put("review_count", it.reviewCount)) } })
        return root
    }

    suspend fun import(context: Context, uri: Uri, db: AppDatabase) {
        val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: error("无法读取备份文件")
        val mediaDir = File(context.filesDir, "records").apply { mkdirs() }
        val text: String
        if (bytes.size > 4 && bytes[0] == 'P'.code.toByte() && bytes[1] == 'K'.code.toByte()) {
            // zip 备份：data.json + media/ 里的照片视频，照片放回 files/records/
            var json: String? = null
            java.util.zip.ZipInputStream(bytes.inputStream()).use { zin ->
                while (true) {
                    val e = zin.nextEntry ?: break
                    when {
                        e.name == "data.json" -> json = zin.readBytes().toString(Charsets.UTF_8)
                        e.name.startsWith("media/") && !e.isDirectory -> {
                            val name = File(e.name).name
                            if (name.isNotBlank() && !name.contains("..")) {
                                val out = File(mediaDir, name)
                                if (!out.exists()) out.outputStream().use { zin.copyTo(it) }
                            }
                        }
                    }
                }
            }
            text = json ?: error("备份里没有 data.json")
        } else text = bytes.toString(Charsets.UTF_8)
        val root = JSONObject(text)
        MoodStore.mergeJson(context, root.optJSONObject("moods"))
        val cards = root.optJSONArray("cards") ?: JSONArray()
        for (i in 0 until cards.length()) { val item = cards.getJSONObject(i); db.cards().insert(VocabularyCard(front = item.optString("front"), back = item.optString("back"), dueAt = item.optLong("due_at", System.currentTimeMillis()))) }
        val habits = root.optJSONArray("habits") ?: JSONArray()
        for (i in 0 until habits.length()) { val item = habits.getJSONObject(i); db.habits().insert(HabitEntry(title = item.optString("title"), doneOn = item.optString("done_on"))) }
        val expenses = root.optJSONArray("expenses") ?: JSONArray()
        for (i in 0 until expenses.length()) { val item = expenses.getJSONObject(i); db.expenses().insert(ExpenseEntry(title = item.optString("title"), amountCents = item.optLong("amount_cents"), spentOn = item.optString("spent_on"))) }
        val records = root.optJSONArray("records") ?: JSONArray()
        // 已经有的记录（同一时间、同样内容）不再重复插入，恢复两次也不会多出一份
        val existing = db.records().all().map { "${it.createdAt}|${it.kind}|${it.text}" }.toMutableSet()
        for (i in 0 until records.length()) {
            val item = records.getJSONObject(i)
            val kind = item.optString("kind", "daily")
            val text0 = item.optString("text")
            val createdAt = item.optLong("created_at", System.currentTimeMillis())
            if (!existing.add("$createdAt|$kind|$text0")) continue
            // 先按文件名在本机找（zip 恢复会放回 files/records/），再看老路径还在不在
            val names = item.optJSONArray("media_names")
            val paths = mutableListOf<String>()
            if (names != null) for (k in 0 until names.length()) {
                val f = File(mediaDir, names.optString(k))
                if (names.optString(k).isNotBlank() && f.exists()) paths += f.absolutePath
            }
            if (paths.isEmpty()) {
                val old = (item.optString("image_paths").takeIf { it.isNotBlank() && it != "null" }?.split("|") ?: emptyList()) +
                    listOfNotNull(item.optString("image_path").takeIf { it.isNotBlank() && it != "null" })
                old.map { it.trim() }.filter { it.isNotEmpty() }.distinct().forEach { p ->
                    val f = File(p).takeIf { it.exists() } ?: File(mediaDir, File(p).name).takeIf { it.exists() }
                    if (f != null) paths += f.absolutePath
                }
            }
            db.records().insert(
                RecordEntry(
                    kind = kind,
                    text = text0,
                    imagePath = paths.firstOrNull(),
                    imagePaths = paths.takeIf { it.size > 1 }?.joinToString("|"),
                    amountCents = if (item.isNull("amount_cents")) null else item.optLong("amount_cents"),
                    category = item.optString("category").takeIf { it.isNotBlank() && it != "null" },
                    moneyKind = item.optString("money_kind").takeIf { it.isNotBlank() && it != "null" },
                    createdAt = createdAt,
                    moodId = item.optString("mood_id").takeIf { it.isNotBlank() && it != "null" }
                )
            )
        }
        val unknown = root.optJSONArray("unknown_words") ?: JSONArray()
        for (i in 0 until unknown.length()) {
            val item = unknown.getJSONObject(i)
            val headword = item.optString("headword")
            if (headword.isNotBlank() && db.unknownWords().countByHeadword(headword) == 0) {
                db.unknownWords().insert(UnknownWord(headword = headword, phonetic = item.optString("phonetic").takeIf { it.isNotBlank() }, meaning = item.optString("meaning"), addedAt = item.optLong("added_at", System.currentTimeMillis()), knownCount = item.optInt("known_count", 0)))
            }
        }
        val progress = root.optJSONArray("word_progress") ?: JSONArray()
        for (i in 0 until progress.length()) {
            val item = progress.getJSONObject(i)
            val headword = item.optString("headword")
            if (headword.isNotBlank() && db.wordProgress().get(headword) == null) {
                db.wordProgress().upsert(WordProgress(headword = headword, phonetic = item.optString("phonetic").takeIf { it.isNotBlank() && it != "null" }, meaning = item.optString("meaning"), stage = item.optInt("stage"), dueAt = item.optLong("due_at"), wrongCount = item.optInt("wrong_count"), firstSeen = item.optLong("first_seen"), lastSeen = item.optLong("last_seen")))
            }
        }
        val days = root.optJSONArray("study_days") ?: JSONArray()
        for (i in 0 until days.length()) {
            val item = days.getJSONObject(i)
            val day = item.optString("day")
            if (day.isNotBlank() && db.studyDays().get(day) == null) db.studyDays().upsert(StudyDay(day, item.optInt("new_count"), item.optInt("review_count")))
        }
    }
}
