package com.swenhao10.localfirst.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import org.json.JSONArray
import java.io.File
import java.io.FileOutputStream

/** 内置词包里的一个词条（只读，来自 packs/cet46，ECDICT）。 */
data class PackWord(
    val headword: String,
    val phonetic: String?,
    val pos: String?,
    val meaning: String,
    val tags: List<String>,
    val freqRank: Long?
)

data class PackMeta(val packId: String, val name: String, val entryCount: Int)

/**
 * 内置词包读取：APK 里 assets/packs/pack.db 是只读的，
 * 首次打开复制一份到本机内部存储，再以只读方式打开，离线可用。
 */
object PackStore {
    private const val ASSET_PATH = "packs/pack.db"
    private var db: SQLiteDatabase? = null

    fun ensureCopied(context: Context): File {
        val dir = File(context.filesDir, "packs").apply { mkdirs() }
        val out = File(dir, "pack.db")
        if (!out.exists()) {
            context.assets.open(ASSET_PATH).use { input ->
                FileOutputStream(out).use { output -> input.copyTo(output) }
            }
        }
        return out
    }

    private fun open(context: Context): SQLiteDatabase {
        db?.let { if (it.isOpen) return it }
        val file = ensureCopied(context)
        return SQLiteDatabase.openDatabase(file.path, null, SQLiteDatabase.OPEN_READONLY).also { db = it }
    }

    fun loadMeta(context: Context): PackMeta? {
        return try {
            open(context).rawQuery("SELECT pack_id, name, entry_count FROM pack_meta LIMIT 1", null).use { c ->
                if (c.moveToFirst()) PackMeta(c.getString(0), c.getString(1), c.getInt(2)) else null
            }
        } catch (e: Exception) {
            null
        }
    }

    /** 全部词，按词频从常用到生僻排（没词频的放最后）。 */
    fun loadWords(context: Context): List<PackWord> {
        val list = mutableListOf<PackWord>()
        try {
            open(context).rawQuery(
                "SELECT headword, phonetic, pos, senses_json, tags, freq_rank FROM entry ORDER BY freq_rank IS NULL, freq_rank ASC",
                null
            ).use { c ->
                while (c.moveToNext()) {
                    list.add(
                        PackWord(
                            headword = c.getString(0),
                            phonetic = c.getString(1),
                            pos = c.getString(2),
                            meaning = sensesToText(c.getString(3)),
                            tags = parseTags(c.getString(4)),
                            freqRank = if (c.isNull(5)) null else c.getLong(5)
                        )
                    )
                }
            }
        } catch (e: Exception) {
            // 词包读不到就返回空列表，界面会提示，不闪退
        }
        return list
    }

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
}
