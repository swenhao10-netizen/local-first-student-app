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
