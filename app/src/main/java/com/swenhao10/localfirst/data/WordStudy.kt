package com.swenhao10.localfirst.data

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 背词的复习规则（简化的遗忘曲线）：
 * - 新词点「认识」：1 天后复习；点「不认识」：10 分钟后就可以再复习，并进生词本。
 * - 复习时点「认识」：间隔按 1、2、4、7、15、30、60 天往后推；点「不认识」：回到起点，记错次数 +1。
 * - 待复习里记错次数多的排前面。
 */
object WordStudy {
    private const val MIN = 60_000L
    private const val DAY = 24 * 60 * MIN
    private val intervalsDays = longArrayOf(1, 2, 4, 7, 15, 30, 60)
    /** 达到这个阶段（间隔 ≥ 15 天）算「掌握」。 */
    const val MASTERED_STAGE = 5
    /** 生词本里连续认识几次算记住。 */
    const val UNKNOWN_GRADUATE = 3

    private val dayFmt = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA)
    fun today(): String = dayFmt.format(Date())

    /** 新学一个词或复习一个词后，算出新的进度。 */
    fun answer(old: WordProgress?, word: PackWordLite, known: Boolean, now: Long = System.currentTimeMillis()): WordProgress {
        val base = old ?: WordProgress(headword = word.headword, phonetic = word.phonetic, meaning = word.meaning, stage = 0, dueAt = now, firstSeen = now, lastSeen = now)
        return if (known) {
            val nextStage = if (old == null) 1 else (base.stage + 1).coerceAtMost(intervalsDays.size)
            val days = intervalsDays[(nextStage - 1).coerceIn(0, intervalsDays.size - 1)]
            base.copy(stage = nextStage, dueAt = now + days * DAY, lastSeen = now, phonetic = word.phonetic ?: base.phonetic, meaning = word.meaning.ifBlank { base.meaning })
        } else {
            base.copy(stage = 0, dueAt = now + 10 * MIN, wrongCount = base.wrongCount + 1, lastSeen = now, phonetic = word.phonetic ?: base.phonetic, meaning = word.meaning.ifBlank { base.meaning })
        }
    }

    /** 下次复习的提示文字。 */
    fun nextHint(old: WordProgress?): String {
        val nextStage = if (old == null) 1 else (old.stage + 1).coerceAtMost(intervalsDays.size)
        val days = intervalsDays[(nextStage - 1).coerceIn(0, intervalsDays.size - 1)]
        return "认识 → $days 天后复习 · 不认识 → 10 分钟后再来"
    }

    /** 记一笔今天学了什么。 */
    suspend fun bumpToday(dao: StudyDayDao, isNew: Boolean) {
        val d = today()
        val cur = dao.get(d) ?: StudyDay(d)
        dao.upsert(if (isNew) cur.copy(newCount = cur.newCount + 1) else cur.copy(reviewCount = cur.reviewCount + 1))
    }

    /** 连续学习天数：从今天（今天没学就从昨天）往前数。 */
    fun streak(days: List<StudyDay>): Int {
        val set = days.filter { it.newCount + it.reviewCount > 0 }.map { it.day }.toSet()
        var t = System.currentTimeMillis()
        if (dayFmt.format(Date(t)) !in set) t -= DAY
        var n = 0
        while (dayFmt.format(Date(t)) in set) { n++; t -= DAY }
        return n
    }
}

/** 复习时只需要这几样。 */
data class PackWordLite(val headword: String, val phonetic: String?, val meaning: String)

fun PackWord.lite() = PackWordLite(headword, phonetic, meaning)
fun WordProgress.lite() = PackWordLite(headword, phonetic, meaning)
fun UnknownWord.lite() = PackWordLite(headword, phonetic, meaning)
