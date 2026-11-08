package com.swenhao10.localfirst.data
import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow
@Entity(tableName = "cards") data class VocabularyCard(@PrimaryKey(autoGenerate = true) val id: Long = 0, val front: String, val back: String, val dueAt: Long = System.currentTimeMillis())
@Entity(tableName = "habits") data class HabitEntry(@PrimaryKey(autoGenerate = true) val id: Long = 0, val title: String, val doneOn: String)
@Entity(tableName = "expenses") data class ExpenseEntry(@PrimaryKey(autoGenerate = true) val id: Long = 0, val title: String, val amountCents: Long, val spentOn: String)
@Dao interface CardDao { @Query("SELECT * FROM cards ORDER BY id DESC") fun observeAll(): Flow<List<VocabularyCard>>; @Query("SELECT COUNT(*) FROM cards") suspend fun count(): Int; @Query("SELECT * FROM cards ORDER BY id ASC") suspend fun all(): List<VocabularyCard>; @Insert suspend fun insert(card: VocabularyCard) }
@Dao interface HabitDao { @Query("SELECT * FROM habits ORDER BY id DESC") fun observeAll(): Flow<List<HabitEntry>>; @Query("SELECT * FROM habits ORDER BY id ASC") suspend fun all(): List<HabitEntry>; @Insert suspend fun insert(entry: HabitEntry) }
@Dao interface ExpenseDao { @Query("SELECT * FROM expenses ORDER BY id DESC") fun observeAll(): Flow<List<ExpenseEntry>>; @Query("SELECT * FROM expenses ORDER BY id ASC") suspend fun all(): List<ExpenseEntry>; @Insert suspend fun insert(entry: ExpenseEntry) }
@Database(entities = [VocabularyCard::class, HabitEntry::class, ExpenseEntry::class], version = 1, exportSchema = true)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cards(): CardDao; abstract fun habits(): HabitDao; abstract fun expenses(): ExpenseDao
    companion object { @Volatile private var instance: AppDatabase? = null
        fun get(context: Context): AppDatabase = instance ?: synchronized(this) { instance ?: Room.databaseBuilder(context, AppDatabase::class.java, "student-local.db").build().also { instance = it } }
    }
}

