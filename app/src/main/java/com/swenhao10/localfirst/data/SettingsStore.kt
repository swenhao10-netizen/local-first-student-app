package com.swenhao10.localfirst.data

import android.content.Context

/**
 * 设置存在 SharedPreferences（文件 zhixing_settings），不走数据库：
 * 通知开关/提醒时间、外观三选一。默认值：两个提醒都关，提醒时间 21:00，外观跟随系统。
 */
object SettingsStore {
    private const val FILE = "zhixing_settings"

    // 通知
    const val KEY_REVIEW_ENABLED = "reminder_review_enabled"
    const val KEY_EXPENSE_ENABLED = "reminder_expense_enabled"
    const val KEY_REMINDER_HOUR = "reminder_hour"
    const val KEY_REMINDER_MINUTE = "reminder_minute"

    // 外观：ThemeMode 的取值
    const val KEY_THEME_MODE = "theme_mode"

    // 外观：主题颜色（PaletteId 的取值，默认绿色）
    const val KEY_PALETTE = "theme_palette"

    // 首页头像名字
    const val KEY_NICKNAME = "nickname"
    const val DEFAULT_NICKNAME = "同学"

    // 头像图片（本机文件路径，空 = 用默认简笔头像）
    const val KEY_AVATAR = "avatar_path"

    // 背单词进度
    const val KEY_STUDY_INDEX = "study_index"
    const val KEY_STUDY_KNOWN = "study_known"
    const val KEY_STUDY_UNKNOWN = "study_unknown"
    const val KEY_STUDY_ACTIVE = "study_active"

    const val DEFAULT_HOUR = 21
    const val DEFAULT_MINUTE = 0

    private fun prefs(context: Context) =
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE)

    fun isReviewReminderEnabled(context: Context): Boolean =
        prefs(context).getBoolean(KEY_REVIEW_ENABLED, false)

    fun setReviewReminderEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_REVIEW_ENABLED, enabled).apply()
    }

    fun isExpenseReminderEnabled(context: Context): Boolean =
        prefs(context).getBoolean(KEY_EXPENSE_ENABLED, false)

    fun setExpenseReminderEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_EXPENSE_ENABLED, enabled).apply()
    }

    /** 提醒时间（小时, 分钟），两个提醒共用同一个时间。 */
    fun getReminderTime(context: Context): Pair<Int, Int> {
        val p = prefs(context)
        return p.getInt(KEY_REMINDER_HOUR, DEFAULT_HOUR) to
            p.getInt(KEY_REMINDER_MINUTE, DEFAULT_MINUTE)
    }

    fun setReminderTime(context: Context, hour: Int, minute: Int) {
        prefs(context).edit()
            .putInt(KEY_REMINDER_HOUR, hour)
            .putInt(KEY_REMINDER_MINUTE, minute)
            .apply()
    }

    fun getThemeMode(context: Context): String =
        prefs(context).getString(KEY_THEME_MODE, ThemeMode.SYSTEM) ?: ThemeMode.SYSTEM

    fun setThemeMode(context: Context, mode: String) {
        prefs(context).edit().putString(KEY_THEME_MODE, mode).apply()
    }

    /** 主题颜色，默认绿色（原来的主色）。 */
    fun getPalette(context: Context): String =
        prefs(context).getString(KEY_PALETTE, PaletteId.GREEN) ?: PaletteId.GREEN

    fun setPalette(context: Context, id: String) {
        prefs(context).edit().putString(KEY_PALETTE, id).apply()
    }

    /** 自定义背景图（本机文件路径），空 = 默认渐变。 */
    fun getBackground(context: Context): String? =
        prefs(context).getString("bg_image", null)?.takeIf { it.isNotBlank() }

    fun setBackground(context: Context, path: String?) {
        prefs(context).edit().putString("bg_image", path ?: "").apply()
    }

    /** 磨砂玻璃主题，默认开。 */
    fun isGlassEnabled(context: Context): Boolean =
        prefs(context).getBoolean("glass_theme", true)

    fun setGlassEnabled(context: Context, on: Boolean) {
        prefs(context).edit().putBoolean("glass_theme", on).apply()
    }

    /** 早期版本自动塞的两张示例卡是否已经清掉（只清一次）。 */
    fun isSeedCardsCleaned(context: Context): Boolean =
        prefs(context).getBoolean("seed_cards_cleaned", false)

    fun setSeedCardsCleaned(context: Context) {
        prefs(context).edit().putBoolean("seed_cards_cleaned", true).apply()
    }

    /** 首页头像旁的名字，默认「同学」。 */
    fun getNickname(context: Context): String =
        prefs(context).getString(KEY_NICKNAME, DEFAULT_NICKNAME) ?: DEFAULT_NICKNAME

    fun setNickname(context: Context, name: String) {
        prefs(context).edit().putString(KEY_NICKNAME, name).apply()
    }

    /** 头像文件路径，空字符串表示还没换过，用默认简笔头像。 */
    fun getAvatar(context: Context): String? =
        prefs(context).getString(KEY_AVATAR, null)?.takeIf { it.isNotBlank() }

    fun setAvatar(context: Context, path: String?) {
        prefs(context).edit().putString(KEY_AVATAR, path).apply()
    }

    /** 当前在学的词书 id（默认四六级）。 */
    fun getBookId(context: Context): String = prefs(context).getString("word_book", null) ?: PackStore.DEFAULT_BOOK
    fun setBookId(context: Context, id: String) { prefs(context).edit().putString("word_book", id).apply() }

    /** 高考词书是否跳过中考词（两本重合 1554 个，默认跳过）。 */
    fun isSkipLower(context: Context): Boolean = prefs(context).getBoolean("skip_lower_book", true)
    fun setSkipLower(context: Context, on: Boolean) { prefs(context).edit().putBoolean("skip_lower_book", on).apply() }

    /** 每本词书各自记「学到第几个」；老版本只有四六级一本，进度沿用。 */
    fun getBookIndex(context: Context, id: String): Int {
        val p = prefs(context)
        val key = "book_index_$id"
        if (p.contains(key)) return p.getInt(key, 0)
        return if (id == PackStore.DEFAULT_BOOK) p.getInt(KEY_STUDY_INDEX, 0) else 0
    }
    fun setBookIndex(context: Context, id: String, index: Int) { prefs(context).edit().putInt("book_index_$id", index).apply() }

    /** 背单词的进度：上次学到第几个、认识几个、不认识几个；关掉 App 再进来不再从头开始。 */
    fun getStudyProgress(context: Context): StudySnapshot {
        val p = prefs(context)
        return StudySnapshot(
            index = p.getInt(KEY_STUDY_INDEX, 0),
            known = p.getInt(KEY_STUDY_KNOWN, 0),
            unknown = p.getInt(KEY_STUDY_UNKNOWN, 0),
            inStudy = p.getBoolean(KEY_STUDY_ACTIVE, false)
        )
    }

    fun setStudyProgress(context: Context, snapshot: StudySnapshot) {
        prefs(context).edit()
            .putInt(KEY_STUDY_INDEX, snapshot.index)
            .putInt(KEY_STUDY_KNOWN, snapshot.known)
            .putInt(KEY_STUDY_UNKNOWN, snapshot.unknown)
            .putBoolean(KEY_STUDY_ACTIVE, snapshot.inStudy)
            .apply()
    }
}

/** 背单词进度的快照，用来存 SharedPreferences。 */
data class StudySnapshot(
    val index: Int = 0,
    val known: Int = 0,
    val unknown: Int = 0,
    val inStudy: Boolean = false
)

/** 主题颜色可选值：绿（默认）/ 蓝 / 紫 / 橙 / 粉。 */
object PaletteId {
    const val GREEN = "green"
    const val BLUE = "blue"
    const val PURPLE = "purple"
    const val ORANGE = "orange"
    const val PINK = "pink"

    val ALL = listOf(GREEN, BLUE, PURPLE, ORANGE, PINK)

    fun label(id: String): String = when (id) {
        BLUE -> "海蓝"
        PURPLE -> "紫罗兰"
        ORANGE -> "暖橙"
        PINK -> "樱粉"
        else -> "青绿"
    }
}

/** 外观三选一：白天 / 黑夜 / 跟随系统（默认）。 */
object ThemeMode {
    const val LIGHT = "light"
    const val DARK = "dark"
    const val SYSTEM = "system"

    val ALL = listOf(LIGHT, DARK, SYSTEM)

    fun label(mode: String): String = when (mode) {
        LIGHT -> "白天"
        DARK -> "黑夜"
        else -> "跟随系统"
    }
}
