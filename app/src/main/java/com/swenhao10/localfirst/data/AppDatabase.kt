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
 * v2 统一记录：日常 / 记账 / 单词学习记录都是一条记录。
 * kind: "daily" 日常 | "expense" 记账 | "study" 单词学习记录
 * moneyKind 只在 kind == "expense" 时有值："expense" 支出 | "income" 收入
 * imagePath：图片存在本机内部存储的绝对路径（无图为 null）
 */
@Entity(tableName = "records")
data class RecordEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val kind: String,
    val text: String = "",
    val imagePath: String? = null,
    val amountCents: Long? = null,
    val category: String? = null,
    val moneyKind: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

/** 不记得的单词：学习时点了「不认识」的词，单独分组自己复习。 */
@Entity(tableName = "unknown_words", indices = [Index(value = ["headword"], unique = true)])
data class UnknownWord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val headword: String,
    val phonetic: String? = null,
    val meaning: String = "",
    val addedAt: Long = System.currentTimeMillis()
)

@Dao interface CardDao { @Query("SELECT * FROM cards ORDER BY id DESC") fun observeAll(): Flow<List<VocabularyCard>>; @Query("SELECT COUNT(*) FROM cards") suspend fun count(): Int; @Query("SELECT * FROM cards ORDER BY id ASC") suspend fun all(): List<VocabularyCard>; @Insert suspend fun insert(card: VocabularyCard) }
@Dao interface HabitDao { @Query("SELECT * FROM habits ORDER BY id DESC") fun observeAll(): Flow<List<HabitEntry>>; @Query("SELECT * FROM habits ORDER BY id ASC") suspend fun all(): List<HabitEntry>; @Insert suspend fun insert(entry: HabitEntry) }
@Dao interface ExpenseDao { @Query("SELECT * FROM expenses ORDER BY id DESC") fun observeAll(): Flow<List<ExpenseEntry>>; @Query("SELECT * FROM expenses ORDER BY id ASC") suspend fun all(): List<ExpenseEntry>; @Insert suspend fun insert(entry: ExpenseEntry) }
@Dao interface RecordDao {
    @Query("SELECT * FROM records ORDER BY createdAt DESC") fun observeAll(): Flow<List<RecordEntry>>
    @Query("SELECT * FROM records ORDER BY createdAt DESC") suspend fun all(): List<RecordEntry>
    @Insert suspend fun insert(entry: RecordEntry): Long
    @Query("DELETE FROM records WHERE id = :id") suspend fun delete(id: Long)
}
@Dao interface UnknownWordDao {
    @Query("SELECT * FROM unknown_words ORDER BY addedAt DESC") fun observeAll(): Flow<List<UnknownWord>>
    @Query("SELECT * FROM unknown_words ORDER BY addedAt DESC") suspend fun all(): List<UnknownWord>
    @Query("SELECT COUNT(*) FROM unknown_words WHERE headword = :headword") suspend fun countByHeadword(headword: String): Int
    @Insert suspend fun insert(word: UnknownWord)
    @Query("DELETE FROM unknown_words WHERE headword = :headword") suspend fun deleteByHeadword(headword: String)
}

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `records` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `kind` TEXT NOT NULL, `text` TEXT NOT NULL, `imagePath` TEXT, `amountCents` INTEGER, `category` TEXT, `moneyKind` TEXT, `createdAt` INTEGER NOT NULL)")
        db.execSQL("CREATE TABLE IF NOT EXISTS `unknown_words` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `headword` TEXT NOT NULL, `phonetic` TEXT, `meaning` TEXT NOT NULL, `addedAt` INTEGER NOT NULL)")
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_unknown_words_headword` ON `unknown_words` (`headword`)")
    }
}

@Database(entities = [VocabularyCard::class, HabitEntry::class, ExpenseEntry::class, RecordEntry::class, UnknownWord::class], version = 2, exportSchema = true)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cards(): CardDao; abstract fun habits(): HabitDao; abstract fun expenses(): ExpenseDao
    abstract fun records(): RecordDao; abstract fun unknownWords(): UnknownWordDao
    companion object { @Volatile private var instance: AppDatabase? = null
        fun get(context: Context): AppDatabase = instance ?: synchronized(this) { instance ?: Room.databaseBuilder(context, AppDatabase::class.java, "student-local.db").addMigrations(MIGRATION_1_2).build().also { instance = it } }
    }
}
