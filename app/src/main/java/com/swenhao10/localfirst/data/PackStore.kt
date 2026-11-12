package com.swenhao10.localfirst.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream

/** 词书里的一个词条（只读，来自 assets/packs/<id>/pack.db，ECDICT）。 */
data class PackWord(
    val headword: String,
    val phonetic: String?,
    val pos: String?,
    val meaning: String,
    val tags: List<String>,
    val freqRank: Long?,
    /** 词形变化：过去式、复数……已按中文标签排好。 */
    val forms: List<Pair<String, String>> = emptyList()
)

data class PackMeta(val packId: String, val name: String, val entryCount: Int)

/** 一本词书：按考试分。 */
data class WordBook(val id: String, val shortName: String, val fullName: String, val count: Int)

/**
 * 内置词书：APK 里 assets/packs/<id>/pack.db 是只读的，
 * 用到哪本就复制哪本到本机内部存储，再以只读方式打开，离线可用。
 */
object PackStore {
    val BOOKS = listOf(
        WordBook("zk", "中考", "中考词汇", 1603),
        WordBook("gk", "高考", "高考词汇", 3677),
        WordBook("cet46", "四六级", "大学英语四六级词汇", 5805),
        WordBook("ky", "考研", "考研词汇", 4801),
        WordBook("ielts", "雅思", "雅思词汇", 5040),
        WordBook("toefl", "托福", "托福词汇", 6974),
        WordBook("gre", "GRE", "GRE 词汇", 7504),
        WordBook("dev", "程序员", "程序员英语词汇", 260)
    )
    const val DEFAULT_BOOK = "cet46"

    fun book(id: String): WordBook = BOOKS.firstOrNull { it.id == id } ?: BOOKS.first { it.id == DEFAULT_BOOK }

    /** ECDICT 的标签 -> 中文考试名。 */
    fun tagLabel(tag: String): String? = when (tag) {
        "zk" -> "中考"; "gk" -> "高考"; "cet4" -> "四级"; "cet6" -> "六级"
        "ky" -> "考研"; "ielts" -> "雅思"; "toefl" -> "托福"; "gre" -> "GRE"; "dev" -> "程序员"
        else -> null
    }

    private val dbs = mutableMapOf<String, SQLiteDatabase>()

    private fun ensureCopied(context: Context, id: String): File {
        val dir = File(context.filesDir, "packs").apply { mkdirs() }
        val out = File(dir, "$id.db")
        val versionMark = File(dir, "$id.version")
        val version = "2026.11.12"
        if (!out.exists() || versionMark.takeIf { it.exists() }?.readText() != version) {
            context.assets.open("packs/$id/pack.db").use { input ->
                FileOutputStream(out).use { output -> input.copyTo(output) }
            }
            versionMark.writeText(version)
        }
        return out
    }

    @Synchronized
    private fun open(context: Context, id: String): SQLiteDatabase {
        dbs[id]?.let { if (it.isOpen) return it }
        val file = ensureCopied(context, id)
        return SQLiteDatabase.openDatabase(file.path, null, SQLiteDatabase.OPEN_READONLY).also { dbs[id] = it }
    }

    fun loadMeta(context: Context, id: String = DEFAULT_BOOK): PackMeta? {
        return try {
            open(context, id).rawQuery("SELECT pack_id, name, entry_count FROM pack_meta LIMIT 1", null).use { c ->
                if (c.moveToFirst()) PackMeta(c.getString(0), c.getString(1), c.getInt(2)) else null
            }
        } catch (e: Exception) {
            null
        }
    }

    /** 全部词，按词频从常用到生僻排（没词频的放最后）。 */
    fun loadWords(context: Context, id: String = DEFAULT_BOOK): List<PackWord> {
        val list = mutableListOf<PackWord>()
        try {
            open(context, id).rawQuery(
                "SELECT headword, phonetic, pos, senses_json, tags, freq_rank, exchange_json FROM entry ORDER BY freq_rank IS NULL, freq_rank ASC",
                null
            ).use { c ->
                while (c.moveToNext()) {
                    list.add(
                        PackWord(
                            headword = c.getString(0),
                            phonetic = c.getString(1)?.let { cleanPhonetic(it) },
                            pos = c.getString(2),
                            meaning = sensesToText(c.getString(3)),
                            tags = parseTags(c.getString(4)),
                            freqRank = if (c.isNull(5)) null else c.getLong(5),
                            forms = parseForms(c.getString(6))
                        )
                    )
                }
            }
        } catch (e: Exception) {
            // 词书读不到就返回空列表，界面会提示，不闪退
        }
        return list
    }

    /** ECDICT 音标里用的是 ә（西里尔字母），换成国际音标的 ə。 */
    fun cleanPhonetic(p: String): String = p.replace('ә', 'ə').trim()

    /** senses_json: [{"pos":"vt.","meaning":"放弃, 抛弃"}] -> "vt. 放弃, 抛弃；..." */
    private fun sensesToText(json: String?): String {
        if (json.isNullOrBlank()) return ""
        return try {
            val arr = JSONArray(json)
            val parts = mutableListOf<String>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val pos = obj.optString("pos").trim()
                val meaning = obj.optString("meaning").trim()
                if (meaning.isNotEmpty()) parts.add(if (pos.isNotEmpty()) "$pos $meaning" else meaning)
            }
            parts.joinToString("；")
        } catch (e: Exception) {
            ""
        }
    }

    private fun parseTags(json: String?): List<String> {
        if (json.isNullOrBlank()) return emptyList()
        return try {
            val arr = JSONArray(json)
            List(arr.length()) { arr.getString(it) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private val formLabels = listOf(
        "p" to "过去式", "d" to "过去分词", "i" to "现在分词", "3" to "第三人称单数",
        "s" to "复数", "r" to "比较级", "t" to "最高级", "0" to "原形"
    )

    private fun parseForms(json: String?): List<Pair<String, String>> {
        if (json.isNullOrBlank()) return emptyList()
        return try {
            val obj = JSONObject(json)
            formLabels.mapNotNull { (k, label) -> obj.optString(k).takeIf { it.isNotBlank() }?.let { label to it } }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
