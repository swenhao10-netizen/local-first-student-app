package com.swenhao10.localfirst.data

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

/**
 * 检查更新：只在用户点「检查更新」时联网，平时不联网。
 * 国内直连 GitHub 常常不通，所以版本信息和安装包都准备了几条线路，一条不通换下一条。
 * 镜像是第三方的公共加速，可能失效；装的包必须和现在这个签名一致，安卓才会让它覆盖安装。
 */
object Updater {
    private const val REPO = "swenhao10-netizen/zhixing"
    data class Info(val versionCode: Int, val versionName: String, val tag: String, val notes: String, val size: Long)

    private val INFO_URLS = listOf(
        "https://raw.githubusercontent.com/$REPO/main/version.json",
        "https://cdn.jsdelivr.net/gh/$REPO@main/version.json",
        "https://fastly.jsdelivr.net/gh/$REPO@main/version.json",
        "https://ghfast.top/https://raw.githubusercontent.com/$REPO/main/version.json",
        "https://gh-proxy.com/https://raw.githubusercontent.com/$REPO/main/version.json"
    )
    private val MIRRORS = listOf("", "https://ghfast.top/", "https://gh-proxy.com/", "https://ghproxy.net/")

    private fun open(url: String, timeoutMs: Int): HttpURLConnection =
        (URL(url).openConnection() as HttpURLConnection).apply {
            connectTimeout = timeoutMs; readTimeout = timeoutMs * 2
            instanceFollowRedirects = true
            setRequestProperty("User-Agent", "zhixing-app")
        }

    /** 依次试几条线路取最新版本信息，全失败抛错。 */
    fun fetchLatest(): Info {
        var last: Throwable? = null
        for (u in INFO_URLS) {
            try {
                val conn = open(u + "?t=" + System.currentTimeMillis() / 60_000, 6000)
                if (conn.responseCode != 200) { conn.disconnect(); continue }
                val o = JSONObject(conn.inputStream.use { it.readBytes().toString(Charsets.UTF_8) })
                conn.disconnect()
                return Info(o.getInt("versionCode"), o.getString("versionName"), o.getString("tag"), o.optString("notes"), o.optLong("size", 0))
            } catch (e: Throwable) { last = e }
        }
        throw IllegalStateException("连不上更新服务器", last)
    }

    fun apkUrl(info: Info) = "https://github.com/$REPO/releases/download/${info.tag}/zhixing-${info.tag}.apk"

    /** 下载安装包，onProgress(0..1)；返回本地文件。 */
    fun download(context: Context, info: Info, onProgress: (Float) -> Unit): File {
        val dir = File(context.cacheDir, "update").apply { mkdirs() }
        dir.listFiles()?.forEach { it.delete() }
        val out = File(dir, "zhixing-${info.tag}.apk")
        var last: Throwable? = null
        for (m in MIRRORS) {
            try {
                val conn = open(m + apkUrl(info), 10000)
                if (conn.responseCode != 200) { conn.disconnect(); continue }
                val total = conn.contentLengthLong.takeIf { it > 0 } ?: info.size
                var done = 0L
                conn.inputStream.use { input ->
                    out.outputStream().use { o ->
                        val buf = ByteArray(64 * 1024)
                        while (true) {
                            val n = input.read(buf); if (n < 0) break
                            o.write(buf, 0, n); done += n
                            if (total > 0) onProgress((done.toFloat() / total).coerceIn(0f, 1f))
                        }
                    }
                }
                conn.disconnect()
                val head = out.inputStream().use { val b = ByteArray(2); it.read(b); b }
                if (out.length() > 1_000_000 && head[0] == 'P'.code.toByte() && head[1] == 'K'.code.toByte()) return out
                out.delete()
            } catch (e: Throwable) { last = e; out.delete() }
        }
        throw IllegalStateException("安装包下载失败", last)
    }

    fun canInstall(context: Context) = Build.VERSION.SDK_INT < 26 || context.packageManager.canRequestPackageInstalls()

    fun installPermissionIntent(context: Context) =
        Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:${context.packageName}"))

    fun install(context: Context, apk: File) {
        val uri = FileProvider.getUriForFile(context, context.packageName + ".fileprovider", apk)
        context.startActivity(
            Intent(Intent.ACTION_VIEW).setDataAndType(uri, "application/vnd.android.package-archive")
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }

    fun currentVersionCode(context: Context): Int = runCatching {
        val pi = context.packageManager.getPackageInfo(context.packageName, 0)
        if (Build.VERSION.SDK_INT >= 28) pi.longVersionCode.toInt() else @Suppress("DEPRECATION") pi.versionCode
    }.getOrDefault(0)
}
