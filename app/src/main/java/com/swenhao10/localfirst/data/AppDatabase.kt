package com.swenhao10.localfirst.data
import android.content.Context
import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.flow.Flow
@Entity(tableName = "cards") data class VocabularyCard(@PrimaryKey(autoGenerate = true) val id: Long = 0, val front: String, val back: String, val dueAt: Long = System.currentTimeMillis())
@Entity(tableName = "habits") data class HabitEntry(@PrimaryKey(autoGenerate = true) val id: Long = 0, val title: String, val doneOn: String)
@Entity(tableName = "expenses") data class ExpenseEntry(@PrimaryKey(autoGenerate = true) val id: Long = 0, val title: String, val amountCents: Long, val spentOn: String)

/**
 * v3 统一记录：日常 / 记账 / 单词学习记录都是一条记录。
 * kind: "daily" 日常 | "expense" 记账 | "study" 单词学习记录
 * moneyKind 只在 kind == "expense" 时有值："expense" 支出 | "income" 收入
 * imagePath：v2 时代的单图字段，老数据原样保留（无图为 null）
 * imagePaths：v3 起支持多图，用 "|" 拼起来的本机绝对路径；为空时回落到 imagePath
 */
@Entity(tableName = "records")
data class RecordEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val kind: String,
    val text: String = "",
    val imagePath: String? = null,
    val imagePaths: String? = null,
    val amountCents: Long? = null,
    val category: String? = null,
    val moneyKind: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    /** v5 心情：MoodStore 里的心情 id（内置 mood_xxx / 自定义 c_xxx），没选为 null。 */
    val moodId: String? = null
)

/** 一条记录的全部图片：多图优先，老的单图字段兜底。 */
fun RecordEntry.images(): List<String> {
    val many = imagePaths?.split("|")?.map { it.trim() }?.filter { it.isNotEmpty() } ?: emptyList()
    if (many.isNotEmpty()) return many
    return listOfNotNull(imagePath?.takeIf { it.isNotBlank() })
}

/** 不记得的单词：学习时点了「不认识」的词，单独分组自己复习。 */
@Entity(tableName = "unknown_words", indices = [Index(value = ["headword"], unique = true)])
data class UnknownWord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val headword: String,
    val phonetic: String? = null,
    val meaning: String = "",
    val addedAt: Long = System.currentTimeMillis(),
    /** 复习生词本时连续认识的次数，到 3 次就算记住，移出生词本。 */
    @ColumnInfo(defaultValue = "0") val knownCount: Int = 0
)

/**
 * v4 每个词的复习进度（按遗忘曲线）。以单词本身为键，换词书进度也不丢。
 * stage：0 = 刚记错 / 刚学，越大间隔越长；dueAt 到了就进「待复习」。
 */
@Entity(tableName = "word_progress")
data class WordProgress(
    @PrimaryKey val headword: String,
    val phonetic: String? = null,
    val meaning: String = "",
    val stage: Int = 0,
    val dueAt: Long = System.currentTimeMillis(),
    val wrongCount: Int = 0,
    val firstSeen: Long = System.currentTimeMillis(),
    val lastSeen: Long = System.currentTimeMillis()
)

/** v4 每天学了多少：用来算今日新学、今日复习、学习天数。day = yyyy-MM-dd。 */
@Entity(tableName = "study_days")
data class StudyDay(@PrimaryKey val day: String, val newCount: Int = 0, val reviewCount: Int = 0)

@Dao interface CardDao { @Query("SELECT * FROM cards ORDER BY id DESC") fun observeAll(): Flow<List<VocabularyCard>>; @Query("SELECT COUNT(*) FROM cards") suspend fun count(): Int; @Query("SELECT * FROM cards ORDER BY id ASC") suspend fun all(): List<VocabularyCard>; @Insert suspend fun insert(card: VocabularyCard); @Query("DELETE FROM cards WHERE id = :id") suspend fun delete(id: Long); @Query("DELETE FROM cards WHERE front = :front AND back = :back") suspend fun deleteExact(front: String, back: String) }
@Dao interface HabitDao { @Query("SELECT * FROM habits ORDER BY id DESC") fun observeAll(): Flow<List<HabitEntry>>; @Query("SELECT * FROM habits ORDER BY id ASC") suspend fun all(): List<HabitEntry>; @Insert suspend fun insert(entry: HabitEntry) }
@Dao interface ExpenseDao { @Query("SELECT * FROM expenses ORDER BY id DESC") fun observeAll(): Flow<List<ExpenseEntry>>; @Query("SELECT * FROM expenses ORDER BY id ASC") suspend fun all(): List<ExpenseEntry>; @Insert suspend fun insert(entry: ExpenseEntry) }
@Dao interface RecordDao {
    @Query("SELECT * FROM records ORDER BY createdAt DESC") fun observeAll(): Flow<List<RecordEntry>>
    @Query("SELECT * FROM records ORDER BY createdAt DESC") suspend fun all(): List<RecordEntry>
    @Insert suspend fun insert(entry: RecordEntry): Long
    @Update suspend fun update(entry: RecordEntry)
    @Query("DELETE FROM records WHERE id = :id") suspend fun delete(id: Long)
}
@Dao interface UnknownWordDao {
    @Query("SELECT * FROM unknown_words ORDER BY addedAt DESC") fun observeAll(): Flow<List<UnknownWord>>
    @Query("SELECT * FROM unknown_words ORDER BY addedAt DESC") suspend fun all(): List<UnknownWord>
    @Query("SELECT COUNT(*) FROM unknown_words WHERE headword = :headword") suspend fun countByHeadword(headword: String): Int
    @Insert suspend fun insert(word: UnknownWord)
    @Query("DELETE FROM unknown_words WHERE headword = :headword") suspend fun deleteByHeadword(headword: String)
    @Query("UPDATE unknown_words SET knownCount = :count WHERE headword = :headword") suspend fun setKnownCount(headword: String, count: Int)
}
@Dao interface WordProgressDao {
    @Query("SELECT * FROM word_progress") suspend fun all(): List<WordProgress>
    @Query("SELECT * FROM word_progress WHERE headword = :headword") suspend fun get(headword: String): WordProgress?
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsert(p: WordProgress)
    /** 到期的词：记错多的排前面，再按到期时间。 */
    @Query("SELECT * FROM word_progress WHERE dueAt <= :now ORDER BY wrongCount DESC, dueAt ASC LIMIT :limit") suspend fun due(now: Long, limit: Int): List<WordProgress>
    @Query("SELECT COUNT(*) FROM word_progress WHERE dueAt <= :now") fun observeDueCount(now: Long): Flow<Int>
    @Query("SELECT COUNT(*) FROM word_progress") fun observeLearnedCount(): Flow<Int>
    @Query("SELECT COUNT(*) FROM word_progress WHERE stage >= :stage") fun observeMasteredCount(stage: Int): Flow<Int>
}
@Dao interface StudyDayDao {
    @Query("SELECT * FROM study_days ORDER BY day DESC") fun observeAll(): Flow<List<StudyDay>>
    @Query("SELECT * FROM study_days") suspend fun all(): List<StudyDay>
    @Query("SELECT * FROM study_days WHERE day = :day") suspend fun get(day: String): StudyDay?
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsert(d: StudyDay)
}

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `records` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `kind` TEXT NOT NULL, `text` TEXT NOT NULL, `imagePath` TEXT, `amountCents` INTEGER, `category` TEXT, `moneyKind` TEXT, `createdAt` INTEGER NOT NULL)")
        db.execSQL("CREATE TABLE IF NOT EXISTS `unknown_words` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `headword` TEXT NOT NULL, `phonetic` TEXT, `meaning` TEXT NOT NULL, `addedAt` INTEGER NOT NULL)")
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_unknown_words_headword` ON `unknown_words` (`headword`)")
    }
}

/** v3：记录支持一条多图。只加一列，老数据（含 imagePath 的单图）原样保留。 */
val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE `records` ADD COLUMN `imagePaths` TEXT")
    }
}

/** v4：生词本加「认识次数」；新增单词复习进度和每日学习统计。老数据原样保留。 */
val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE `unknown_words` ADD COLUMN `knownCount` INTEGER NOT NULL DEFAULT 0")
        db.execSQL("CREATE TABLE IF NOT EXISTS `word_progress` (`headword` TEXT NOT NULL, `phonetic` TEXT, `meaning` TEXT NOT NULL, `stage` INTEGER NOT NULL, `dueAt` INTEGER NOT NULL, `wrongCount` INTEGER NOT NULL, `firstSeen` INTEGER NOT NULL, `lastSeen` INTEGER NOT NULL, PRIMARY KEY(`headword`))")
        db.execSQL("CREATE TABLE IF NOT EXISTS `study_days` (`day` TEXT NOT NULL, `newCount` INTEGER NOT NULL, `reviewCount` INTEGER NOT NULL, PRIMARY KEY(`day`))")
    }
}

/** v5：记录可以带一个心情。只加一列（可空），老数据原样保留。 */
val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE `records` ADD COLUMN `moodId` TEXT")
    }
}

@Database(entities = [VocabularyCard::class, HabitEntry::class, ExpenseEntry::class, RecordEntry::class, UnknownWord::class, WordProgress::class, StudyDay::class], version = 5, exportSchema = true)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cards(): CardDao; abstract fun habits(): HabitDao; abstract fun expenses(): ExpenseDao
    abstract fun records(): RecordDao; abstract fun unknownWords(): UnknownWordDao
    abstract fun wordProgress(): WordProgressDao; abstract fun studyDays(): StudyDayDao
    companion object { @Volatile private var instance: AppDatabase? = null
        fun get(context: Context): AppDatabase = instance ?: synchronized(this) { instance ?: Room.databaseBuilder(context, AppDatabase::class.java, "student-local.db").addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5).build().also { instance = it } }
    }
}
