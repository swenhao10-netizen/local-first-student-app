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
 * 安卓 10 及以上走 MediaStore（不用任何存储权限，文件夹第一次写的时候系统自动建）；
 * 安卓 8/9 静默存到 App 自己的外部目录（Android/data/…/files/Documents/知行），不弹任何选择框。
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
    /** 每个系统版本都能备份：10+ 进「文档/知行」，更老的进 App 自己的目录。 */
    fun supported() = true
    private fun mediaStore() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
    /** 给界面显示的存放位置。 */
    fun locationLabel() = if (mediaStore()) "「文档/知行」" else "App 自己的文件夹"

    /** 安卓 8/9：App 专属外部目录里的 知行 文件夹（不用权限，卸载会一起删）。 */
    private fun legacyDir(c: Context): File? =
        c.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)?.let { File(it, "知行") }?.apply { mkdirs() }

    /** 找到的一份备份：uri 可以直接交给 DataTransfer.import。 */
    data class Found(val uri: android.net.Uri, val name: String)

    /**
     * 找最新的一份备份（只找得到知行自己能读的：安卓 11+ 卸载重装后，旧安装写的文件通常读不到，这时返回 null，界面什么都不弹）。
     */
    fun findLatest(c: Context): Found? = runCatching {
        if (mediaStore()) {
            val collection = MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            c.contentResolver.query(
                collection, arrayOf(MediaStore.MediaColumns._ID, MediaStore.MediaColumns.DISPLAY_NAME),
                "${MediaStore.MediaColumns.RELATIVE_PATH} LIKE ? AND ${MediaStore.MediaColumns.DISPLAY_NAME} LIKE ?",
                arrayOf("$FOLDER%", "$PREFIX%"), "${MediaStore.MediaColumns.DISPLAY_NAME} DESC"
            )?.use { cur ->
                while (cur.moveToNext()) {
                    val uri = ContentUris.withAppendedId(collection, cur.getLong(0))
                    // 列表里有但打不开（别的安装写的）就跳过
                    val ok = runCatching { c.contentResolver.openInputStream(uri)?.use { it.read() >= 0 } ?: false }.getOrDefault(false)
                    if (ok) return@runCatching Found(uri, cur.getString(1) ?: "")
                }
            }
            null
        } else {
            legacyDir(c)?.listFiles { f -> f.name.startsWith(PREFIX) && f.name.endsWith(".zip") }
                ?.maxByOrNull { it.name }?.let { Found(android.net.Uri.fromFile(it), it.name) }
        }
    }.getOrNull()

    /**
     * 第一次打开就把「文档/知行」建出来：放一个说明小文件（只写一次）。之后第一次备份也会自动建。
     */
    fun ensureFolder(c: Context) {
        val prefs = p(c)
        if (prefs.getBoolean("folder_made", false)) return
        runCatching {
            if (mediaStore()) {
                val values = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, "关于这个文件夹.txt")
                    put(MediaStore.MediaColumns.MIME_TYPE, "text/plain")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, FOLDER)
                }
                val uri = c.contentResolver.insert(MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY), values)
                    ?: error("insert failed")
                c.contentResolver.openOutputStream(uri)?.use {
                    it.write("这是「知行」的自动备份文件夹。每次离开知行，数据有变化就在这里存一份「知行备份-日期.zip」，留最近 3 份。\n卸载知行不会删除这里的文件；重装后可以在「我的 → 从备份恢复」里选它恢复。\n".toByteArray(Charsets.UTF_8))
                }
            } else legacyDir(c)
            prefs.edit().putBoolean("folder_made", true).apply()
        }
    }

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
        val db = AppDatabase.get(c)
        val json = DataTransfer.buildJson(db, c)
        val records = db.records().all()
        val media = records.flatMap { it.images() }.distinct().map { File(it) }.filter { it.exists() }
        val sig = (json.toString().hashCode().toLong() * 31 + media.sumOf { it.length() }).toString()
        if (!force && sig == p(c).getString(KEY_SIG, null)) return false
        if (records.isEmpty() && db.wordProgress().all().isEmpty() && !force) return false

        val name = PREFIX + SimpleDateFormat("yyyyMMdd-HHmmss", Locale.CHINA).format(Date()) + ".zip"
        if (!mediaStore()) {
            val dir = legacyDir(c) ?: return false
            val f = File(dir, name)
            try { f.outputStream().use { writeZip(it, json.toString(2), media) } } catch (e: Throwable) { f.delete(); throw e }
            p(c).edit().putString(KEY_SIG, sig).putLong(KEY_AT, System.currentTimeMillis()).apply()
            dir.listFiles { x -> x.name.startsWith(PREFIX) }?.sortedByDescending { it.name }?.drop(KEEP)?.forEach { it.delete() }
            return true
        }
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
        if (!mediaStore()) return
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
