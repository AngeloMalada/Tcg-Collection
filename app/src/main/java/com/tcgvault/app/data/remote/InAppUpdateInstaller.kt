package com.tcgvault.app.data.remote

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

sealed interface UpdateDownloadState {
    data object Idle : UpdateDownloadState
    data class Downloading(val progress: Float) : UpdateDownloadState
    data class ReadyToInstall(val apkFile: File, val needsPermission: Boolean) : UpdateDownloadState
    data class Error(val message: String) : UpdateDownloadState
}

/**
 * Downloads a GitHub Release APK privately into cacheDir with live progress and launches
 * Android's built-in Package Installer overlay so the user updates in-place without
 * leaving the app or manually managing APK files.
 */
object InAppUpdateInstaller {

    private val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .build()

    suspend fun downloadApk(
        context: Context,
        url: String,
        version: String,
        onProgress: (Float) -> Unit
    ): Result<File> = withContext(Dispatchers.IO) {
        runCatching {
            val updatesDir = File(context.cacheDir, "updates").apply { mkdirs() }
            // Clean up any older cached APKs
            updatesDir.listFiles()?.forEach { it.delete() }

            val targetFile = File(updatesDir, "update-$version.apk")
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "TcgCollectionTracker")
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw IllegalStateException("Download failed (HTTP ${response.code})")
                }
                val body = response.body ?: throw IllegalStateException("Empty APK response")
                val totalBytes = body.contentLength()

                body.byteStream().use { input ->
                    FileOutputStream(targetFile).use { output ->
                        val buffer = ByteArray(16 * 1024)
                        var bytesReadTotal = 0L
                        var read: Int
                        while (input.read(buffer).also { read = it } != -1) {
                            output.write(buffer, 0, read)
                            bytesReadTotal += read
                            if (totalBytes > 0L) {
                                onProgress((bytesReadTotal.toFloat() / totalBytes.toFloat()).coerceIn(0f, 1f))
                            }
                        }
                        output.flush()
                    }
                }
            }
            onProgress(1f)
            targetFile
        }
    }

    fun canInstallPackages(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.packageManager.canRequestPackageInstalls()
        } else {
            true
        }
    }

    fun openInstallPermissionSettings(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val intent = Intent(
                Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                Uri.parse("package:${context.packageName}")
            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }

    fun launchSystemInstaller(context: Context, apkFile: File) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            apkFile
        )
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}
