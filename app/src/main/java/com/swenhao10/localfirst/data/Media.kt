package com.swenhao10.localfirst.data

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

/**
 * 记录里的图片和视频共用一个路径列表（RecordEntry.images()），靠扩展名区分，
 * 所以加视频不用改数据库。所有文件都复制在 App 自己的 files/records/ 里：
 * 相册里的原图/原视频删了，记录里的这份还在；想再存回相册用 saveToGallery。
 */
private val VIDEO_EXT = setOf("mp4", "m4v", "3gp", "3gpp", "webm", "mkv", "mov", "avi", "ts")

fun isVideoPath(path: String?): Boolean =
    path != null && path.substringAfterLast('.', "").lowercase() in VIDEO_EXT

fun isVideoUri(context: Context, uri: Uri): Boolean {
    val type = runCatching { context.contentResolver.getType(uri) }.getOrNull()
    if (type != null) return type.startsWith("video/")
    return isVideoPath(uri.lastPathSegment)
}

/** 按 content Uri 的类型给复制到本机的文件取扩展名。 */
fun extensionFor(context: Context, uri: Uri): String {
    val type = runCatching { context.contentResolver.getType(uri) }.getOrNull()
    val fromMime = type?.let { MimeTypeMap.getSingleton().getExtensionFromMimeType(it) }
    if (!fromMime.isNullOrBlank()) return fromMime
    val fromName = uri.lastPathSegment?.substringAfterLast('.', "")?.lowercase()
    if (!fromName.isNullOrBlank() && fromName.length <= 5) return fromName
    return if (type?.startsWith("video/") == true) "mp4" else "jpg"
}

/** 视频取一帧当缩略图，缩到 maxDim 以内。 */
fun videoFrame(path: String, maxDim: Int): Bitmap? = runCatching {
    val r = MediaMetadataRetriever()
    try {
        r.setDataSource(path)
        scaleDown(r.getFrameAtTime(500_000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC) ?: r.frameAtTime, maxDim)
    } finally { runCatching { r.release() } }
}.getOrNull()

fun videoFrame(context: Context, uri: Uri, maxDim: Int): Bitmap? = runCatching {
    val r = MediaMetadataRetriever()
    try {
        r.setDataSource(context, uri)
        scaleDown(r.getFrameAtTime(500_000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC) ?: r.frameAtTime, maxDim)
    } finally { runCatching { r.release() } }
}.getOrNull()

/** 视频时长（毫秒），读不到返回 null。 */
fun videoDurationMs(path: String): Long? = runCatching {
    val r = MediaMetadataRetriever()
    try {
        r.setDataSource(path)
        r.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull()
    } finally { runCatching { r.release() } }
}.getOrNull()

fun formatDuration(ms: Long): String {
    val s = (ms / 1000).coerceAtLeast(0)
    return "%d:%02d".format(s / 60, s % 60)
}

private fun scaleDown(bmp: Bitmap?, maxDim: Int): Bitmap? {
    if (bmp == null) return null
    val big = maxOf(bmp.width, bmp.height)
    if (big <= maxDim) return bmp
    val k = maxDim.toFloat() / big
    return Bitmap.createScaledBitmap(bmp, (bmp.width * k).toInt().coerceAtLeast(1), (bmp.height * k).toInt().coerceAtLeast(1), true)
}

/** Android 9 及以下往公共相册写文件要存储权限；10 起不用。 */
fun galleryNeedsPermission(): Boolean = Build.VERSION.SDK_INT < Build.VERSION_CODES.Q

/**
 * 把一张图或一段视频存进系统相册：图片放 Pictures/知行，视频放 Movies/知行。
 * 在 IO 线程调用。成功返回 true。
 */
fun saveToGallery(context: Context, path: String): Boolean {
    val src = File(path)
    if (!src.exists()) return false
    val video = isVideoPath(path)
    val ext = src.extension.lowercase().ifBlank { if (video) "mp4" else "jpg" }
    val mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext) ?: if (video) "video/mp4" else "image/jpeg"
    val name = "zhixing_${src.nameWithoutExtension}.$ext"
    return runCatching {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val collection = if (video) MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            else MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            val values = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, name)
                put(MediaStore.MediaColumns.MIME_TYPE, mime)
                put(MediaStore.MediaColumns.RELATIVE_PATH, (if (video) Environment.DIRECTORY_MOVIES else Environment.DIRECTORY_PICTURES) + "/知行")
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }
            val resolver = context.contentResolver
            val uri = resolver.insert(collection, values) ?: return false
            try {
                resolver.openOutputStream(uri)?.use { out -> FileInputStream(src).use { it.copyTo(out) } } ?: error("no stream")
                values.clear(); values.put(MediaStore.MediaColumns.IS_PENDING, 0)
                resolver.update(uri, values, null, null)
            } catch (e: Exception) {
                resolver.delete(uri, null, null); throw e
            }
        } else {
            @Suppress("DEPRECATION")
            val base = Environment.getExternalStoragePublicDirectory(if (video) Environment.DIRECTORY_MOVIES else Environment.DIRECTORY_PICTURES)
            val dir = File(base, "知行").apply { mkdirs() }
            var out = File(dir, name)
            var i = 1
            while (out.exists()) { out = File(dir, "zhixing_${src.nameWithoutExtension}_$i.$ext"); i++ }
            FileInputStream(src).use { input -> FileOutputStream(out).use { input.copyTo(it) } }
            MediaScannerConnection.scanFile(context, arrayOf(out.absolutePath), arrayOf(mime), null)
        }
        true
    }.getOrDefault(false)
}
