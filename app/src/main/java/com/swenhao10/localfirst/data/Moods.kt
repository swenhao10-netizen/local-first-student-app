package com.swenhao10.localfirst.data

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.json.JSONArray
import org.json.JSONObject

/** 表情样式：每种心情一张专属的脸。valence = 愉快度（-2 低落 … +2 愉快），自定义心情跟着所选的脸走。 */
enum class FaceStyle(val key: String, val valence: Int) {
    CALM("calm", 0),
    HAPPY("happy", 2),
    SAD("sad", -2),
    ANXIOUS("anxious", -1),
    ANGRY("angry", -2),
    BORED("bored", -1),
    TIRED("tired", -1),
    EXCITED("excited", 2);

    companion object {
        fun of(key: String?): FaceStyle = values().firstOrNull { it.key == key } ?: CALM
    }
}

/** 一种心情。color 是 ARGB。内置心情 id 固定（mood_calm …），自定义的是 c_<时间戳>。 */
data class Mood(val id: String, val name: String, val color: Int, val face: FaceStyle, val builtIn: Boolean) {
    val valence: Int get() = face.valence
}

/**
 * 心情配置：内置 8 种（可隐藏）+ 自定义（名字、颜色、选一张脸）。
 * 存在 SharedPreferences「zhixing_moods」里；状态是 Compose 可观察的，改了各页面马上跟着变。
 * 记录本身只存 moodId（records.moodId），删掉自定义心情后旧记录就不再显示那张脸。
 */
object MoodStore {
    private const val PREFS = "zhixing_moods"
    private const val KEY_HIDDEN = "hidden"
    private const val KEY_CUSTOM = "custom"

    val BUILT_IN: List<Mood> = listOf(
        Mood("mood_calm", "平静", 0xFF7ED3C6.toInt(), FaceStyle.CALM, true),
        Mood("mood_happy", "开心", 0xFFFFCB5C.toInt(), FaceStyle.HAPPY, true),
        Mood("mood_sad", "难过", 0xFF86AEEB.toInt(), FaceStyle.SAD, true),
        Mood("mood_anxious", "焦虑", 0xFFB9A2EE.toInt(), FaceStyle.ANXIOUS, true),
        Mood("mood_angry", "生气", 0xFFF4877F.toInt(), FaceStyle.ANGRY, true),
        Mood("mood_bored", "烦闷", 0xFFAEB9C7.toInt(), FaceStyle.BORED, true),
        Mood("mood_tired", "疲惫", 0xFFD5B795.toInt(), FaceStyle.TIRED, true),
        Mood("mood_excited", "兴奋", 0xFFFF9FC6.toInt(), FaceStyle.EXCITED, true)
    )

    /** 自定义心情可选的颜色（柔和、跟渐变主题搭）。 */
    val PALETTE: List<Int> = listOf(
        0xFF7ED3C6, 0xFFFFCB5C, 0xFF86AEEB, 0xFFB9A2EE, 0xFFF4877F, 0xFFAEB9C7,
        0xFFD5B795, 0xFFFF9FC6, 0xFF9BD67E, 0xFFFFA96B, 0xFF6FC3EA, 0xFFE59BE8
    ).map { it.toInt() }

    var hidden by mutableStateOf<Set<String>>(emptySet())
        private set
    var custom by mutableStateOf<List<Mood>>(emptyList())
        private set
    private var loaded = false

    private fun p(c: Context) = c.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun load(c: Context) {
        if (loaded) return
        loaded = true
        val prefs = p(c)
        hidden = prefs.getStringSet(KEY_HIDDEN, emptySet())?.toSet() ?: emptySet()
        custom = parseCustom(prefs.getString(KEY_CUSTOM, null))
    }

    /** 全部心情（含隐藏的）：查记录上的 moodId 用。 */
    fun all(): List<Mood> = BUILT_IN + custom

    /** 选择器里显示的：没隐藏的内置 + 全部自定义。 */
    fun visible(): List<Mood> = BUILT_IN.filter { it.id !in hidden } + custom

    fun find(id: String?): Mood? = if (id.isNullOrBlank()) null else BUILT_IN.firstOrNull { it.id == id } ?: custom.firstOrNull { it.id == id }

    fun setHidden(c: Context, id: String, hide: Boolean) {
        hidden = if (hide) hidden + id else hidden - id
        p(c).edit().putStringSet(KEY_HIDDEN, hidden).apply()
    }

    fun upsertCustom(c: Context, mood: Mood) {
        custom = if (custom.any { it.id == mood.id }) custom.map { if (it.id == mood.id) mood else it } else custom + mood
        saveCustom(c)
    }

    fun deleteCustom(c: Context, id: String) {
        custom = custom.filterNot { it.id == id }
        saveCustom(c)
    }

    private fun saveCustom(c: Context) {
        p(c).edit().putString(KEY_CUSTOM, customJson(custom).toString()).apply()
    }

    private fun customJson(list: List<Mood>): JSONArray = JSONArray().also { a ->
        list.forEach { a.put(JSONObject().put("id", it.id).put("name", it.name).put("color", it.color).put("face", it.face.key)) }
    }

    private fun parseCustom(s: String?): List<Mood> = runCatching { parseCustomArray(JSONArray(s ?: "[]")) }.getOrDefault(emptyList())

    private fun parseCustomArray(a: JSONArray): List<Mood> = (0 until a.length()).mapNotNull { i ->
        val o = a.optJSONObject(i) ?: return@mapNotNull null
        val id = o.optString("id").takeIf { it.isNotBlank() } ?: return@mapNotNull null
        Mood(id, o.optString("name").ifBlank { "心情" }, o.optInt("color", PALETTE[0]), FaceStyle.of(o.optString("face")), false)
    }

    /** 备份：直接读 prefs（后台自动备份时界面可能没加载过）。 */
    fun toJson(c: Context): JSONObject {
        val prefs = p(c)
        val hid = prefs.getStringSet(KEY_HIDDEN, emptySet())?.toList() ?: emptyList()
        return JSONObject()
            .put("hidden", JSONArray(hid))
            .put("custom", customJson(parseCustom(prefs.getString(KEY_CUSTOM, null))))
    }

    /** 恢复：自定义心情按 id 合并（本机已有的不覆盖），隐藏设置取并集。 */
    fun mergeJson(c: Context, o: JSONObject?) {
        if (o == null) return
        val prefs = p(c)
        val curCustom = parseCustom(prefs.getString(KEY_CUSTOM, null))
        val incoming = o.optJSONArray("custom")?.let { parseCustomArray(it) } ?: emptyList()
        val merged = curCustom + incoming.filter { n -> curCustom.none { it.id == n.id } }
        val hid = (prefs.getStringSet(KEY_HIDDEN, emptySet())?.toSet() ?: emptySet()) +
            (o.optJSONArray("hidden")?.let { a -> (0 until a.length()).map { a.optString(it) }.filter { it.isNotBlank() } } ?: emptyList())
        prefs.edit().putString(KEY_CUSTOM, customJson(merged).toString()).putStringSet(KEY_HIDDEN, hid).apply()
        custom = merged
        hidden = hid
    }
}

/** 一组记录里出现最多的心情（并列取最新那条的）。records 按时间倒序或任意顺序都行。 */
fun dominantMoodId(records: List<RecordEntry>): String? {
    val withMood = records.filter { !it.moodId.isNullOrBlank() }
    if (withMood.isEmpty()) return null
    val counts = withMood.groupingBy { it.moodId!! }.eachCount()
    val max = counts.values.max()
    return withMood.sortedByDescending { it.createdAt }.first { counts[it.moodId] == max }.moodId
}
