package com.swenhao10.localfirst.data

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import java.io.File
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * 自动备份：每次离开知行，数据有变化就打一个 zip（data.json + 照片视频）
 * 存到手机「文档/知行」，只留最新 3 份。卸载知行不会删这个文件夹。
 * 只支持安卓 10 及以上（不用存储权限）；更老的系统跳过。
 */
object AutoBackup {
    private const val PREFS = "zhixing_backup"
    private const val KEY_ON = "enabled"
    private const val KEY_SIG = "last_sig"
    private const val KEY_AT = "last_at"
    private const val KEEP = 3
    const val PREFIX = "知行备份-"
    val FOLDER = Environment.DIRECTORY_DOCUMENTS + "/知行"

    private fun p(c: Context) = c.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    fun isEnabled(c: Context) = p(c).getBoolean(KEY_ON, true)
    fun setEnabled(c: Context, on: Boolean) = p(c).edit().putBoolean(KEY_ON, on).apply()
    fun lastAt(c: Context) = p(c).getLong(KEY_AT, 0L)
    fun supported() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

    /** 离开 App 时调用：排一个后台任务，App 被关掉也能跑完。 */
    fun schedule(c: Context) {
        if (!supported() || !isEnabled(c)) return
        val req = OneTimeWorkRequestBuilder<Worker>().build()
        WorkManager.getInstance(c).enqueueUniqueWork("zhixing-autobackup", ExistingWorkPolicy.REPLACE, req)
    }

    class Worker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {
        override suspend fun doWork(): Result = runCatching { run(applicationContext, force = false) }.fold({ Result.success() }, { Result.success() })
    }

    /** 数据没变就不备份；force = 手动「立即备份」。返回是否写了新文件。 */
    suspend fun run(c: Context, force: Boolean): Boolean {
        if (!supported()) return false
        val db = AppDatabase.get(c)
        val json = DataTransfer.buildJson(db)
        val records = db.records().all()
        val media = records.flatMap { it.images() }.distinct().map { File(it) }.filter { it.exists() }
        val sig = (json.toString().hashCode().toLong() * 31 + media.sumOf { it.length() }).toString()
        if (!force && sig == p(c).getString(KEY_SIG, null)) return false
        if (records.isEmpty() && db.wordProgress().all().isEmpty() && !force) return false

        val name = PREFIX + SimpleDateFormat("yyyyMMdd-HHmmss", Locale.CHINA).format(Date()) + ".zip"
        val resolver = c.contentResolver
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "application/zip")
            put(MediaStore.MediaColumns.RELATIVE_PATH, FOLDER)
            put(MediaStore.MediaColumns.IS_PENDING, 1)
        }
        val collection = MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        val uri = resolver.insert(collection, values) ?: return false
        try {
            resolver.openOutputStream(uri)?.use { writeZip(it, json.toString(2), media) } ?: error("no stream")
            values.clear(); values.put(MediaStore.MediaColumns.IS_PENDING, 0)
            resolver.update(uri, values, null, null)
        } catch (e: Throwable) {
            resolver.delete(uri, null, null); throw e
        }
        p(c).edit().putString(KEY_SIG, sig).putLong(KEY_AT, System.currentTimeMillis()).apply()
        prune(c)
        return true
    }

    private fun writeZip(out: OutputStream, json: String, media: List<File>) {
        ZipOutputStream(out.buffered()).use { zip ->
            zip.putNextEntry(ZipEntry("data.json")); zip.write(json.toByteArray(Charsets.UTF_8)); zip.closeEntry()
            val used = HashSet<String>()
            media.forEach { f ->
                if (!used.add(f.name)) return@forEach
                // 照片视频本身已压缩，直接存，省电省时间
                zip.setLevel(java.util.zip.Deflater.NO_COMPRESSION)
                zip.putNextEntry(ZipEntry("media/" + f.name))
                f.inputStream().use { it.copyTo(zip) }
                zip.closeEntry()
            }
        }
    }

    /** 只留最新 3 份（只能看到、删掉知行自己写的文件）。 */
    private fun prune(c: Context) {
        val collection = MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        val ids = mutableListOf<Long>()
        c.contentResolver.query(
            collection, arrayOf(MediaStore.MediaColumns._ID),
            "${MediaStore.MediaColumns.RELATIVE_PATH} LIKE ? AND ${MediaStore.MediaColumns.DISPLAY_NAME} LIKE ?",
            arrayOf("$FOLDER%", "$PREFIX%"), "${MediaStore.MediaColumns.DISPLAY_NAME} DESC"
        )?.use { cur -> while (cur.moveToNext()) ids += cur.getLong(0) }
        ids.drop(KEEP).forEach { id -> runCatching { c.contentResolver.delete(ContentUris.withAppendedId(collection, id), null, null) } }
    }
}
