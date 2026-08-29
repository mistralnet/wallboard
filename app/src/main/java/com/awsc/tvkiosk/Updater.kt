package com.awsc.tvkiosk

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.Looper
import androidx.core.content.FileProvider
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

/**
 * Downloads the latest APK from a configurable URL (hosted on the user's own
 * site) and launches the system installer. Because every build is signed with
 * the same release key, the new version installs over the old one in place.
 */
object Updater {

    interface Callback {
        fun onProgress(percent: Int)
        fun onError(message: String)
        fun onReadyToInstall()
    }

    fun downloadAndInstall(context: Context, callback: Callback) {
        val ctx = context.applicationContext
        val urlStr = Prefs.getUpdateUrl(ctx)
        val main = Handler(Looper.getMainLooper())

        Thread {
            try {
                val dir = ctx.getExternalFilesDir("updates") ?: ctx.filesDir
                val apk = File(dir, "awsc-tv-update.apk")
                if (apk.exists()) apk.delete()

                var conn = URL(urlStr).openConnection() as HttpURLConnection
                conn.instanceFollowRedirects = true
                conn.connectTimeout = 20000
                conn.readTimeout = 20000
                conn.connect()

                // Follow a cross-protocol redirect manually if needed.
                var redirects = 0
                while (conn.responseCode in intArrayOf(301, 302, 303, 307, 308) && redirects < 5) {
                    val loc = conn.getHeaderField("Location") ?: break
                    conn.disconnect()
                    conn = URL(loc).openConnection() as HttpURLConnection
                    conn.instanceFollowRedirects = true
                    conn.connectTimeout = 20000
                    conn.readTimeout = 20000
                    conn.connect()
                    redirects++
                }

                if (conn.responseCode !in 200..299) {
                    main.post { callback.onError("שגיאת שרת: ${conn.responseCode}") }
                    return@Thread
                }

                val total = conn.contentLength
                conn.inputStream.use { input ->
                    apk.outputStream().use { output ->
                        val buffer = ByteArray(16 * 1024)
                        var readTotal = 0L
                        while (true) {
                            val read = input.read(buffer)
                            if (read < 0) break
                            output.write(buffer, 0, read)
                            readTotal += read
                            if (total > 0) {
                                val pct = ((readTotal * 100) / total).toInt()
                                main.post { callback.onProgress(pct) }
                            }
                        }
                    }
                }
                conn.disconnect()

                if (apk.length() < 10_000) {
                    main.post { callback.onError("הקובץ שהתקבל אינו תקין") }
                    return@Thread
                }

                val uri: Uri = FileProvider.getUriForFile(
                    ctx, "${ctx.packageName}.fileprovider", apk
                )
                val install = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, "application/vnd.android.package-archive")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                main.post {
                    callback.onReadyToInstall()
                    ctx.startActivity(install)
                }
            } catch (e: Exception) {
                main.post { callback.onError(e.localizedMessage ?: "שגיאה בהורדה") }
            }
        }.start()
    }
}
