package com.tcgvault.app.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

data class AppUpdateInfo(
    val currentVersion: String,
    val latestVersion: String,
    val releaseTitle: String,
    val releaseNotes: String,
    val downloadUrl: String,
    val hasDirectApk: Boolean
)

/**
 * Checks GitHub Releases (no custom backend or auth needed) for a newer published version.
 */
class GitHubUpdateChecker(
    private val owner: String = "AngeloMalada",
    private val repo: String = "Tcg-Collection"
) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val json = Json { ignoreUnknownKeys = true }

    suspend fun checkForUpdate(currentVersion: String): AppUpdateInfo? = withContext(Dispatchers.IO) {
        runCatching {
            val request = Request.Builder()
                .url("https://api.github.com/repos/$owner/$repo/releases/latest")
                .header("User-Agent", "TcgCollectionTracker/$currentVersion")
                .header("Accept", "application/vnd.github+json")
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@use null
                val body = response.body?.string().orEmpty()
                if (body.isBlank()) return@use null

                val root = json.parseToJsonElement(body).jsonObject
                val tagName = root["tag_name"]?.jsonPrimitive?.contentOrNull?.trim() ?: return@use null
                if (!isNewerVersion(latestVersion = tagName, currentVersion = currentVersion)) {
                    return@use null
                }

                val releaseName = root["name"]?.jsonPrimitive?.contentOrNull
                    ?.takeIf { it.isNotBlank() } ?: tagName
                val releaseNotes = root["body"]?.jsonPrimitive?.contentOrNull?.trim().orEmpty()
                val htmlUrl = root["html_url"]?.jsonPrimitive?.contentOrNull
                    ?: "https://github.com/$owner/$repo/releases/latest"

                val apkAssetUrl = root["assets"]?.jsonArray
                    ?.mapNotNull { it.jsonObject }
                    ?.firstOrNull { asset ->
                        val assetName = asset["name"]?.jsonPrimitive?.contentOrNull.orEmpty()
                        assetName.endsWith(".apk", ignoreCase = true)
                    }
                    ?.get("browser_download_url")?.jsonPrimitive?.contentOrNull

                AppUpdateInfo(
                    currentVersion = cleanVersion(currentVersion),
                    latestVersion = cleanVersion(tagName),
                    releaseTitle = releaseName,
                    releaseNotes = releaseNotes,
                    downloadUrl = apkAssetUrl ?: htmlUrl,
                    hasDirectApk = apkAssetUrl != null
                )
            }
        }.getOrNull()
    }

    companion object {
        fun cleanVersion(raw: String): String =
            raw.trim().removePrefix("v").removePrefix("V")

        fun isNewerVersion(latestVersion: String, currentVersion: String): Boolean {
            val latestParts = parseNumericParts(latestVersion)
            val currentParts = parseNumericParts(currentVersion)
            if (latestParts.isEmpty() || currentParts.isEmpty()) return true

            val maxLen = maxOf(latestParts.size, currentParts.size)
            for (i in 0 until maxLen) {
                val l = latestParts.getOrElse(i) { 0 }
                val c = currentParts.getOrElse(i) { 0 }
                if (l != c) return l < c
            }
            return true
        }

        private fun parseNumericParts(version: String): List<Int> {
            val cleaned = cleanVersion(version).substringBefore("-")
            return cleaned.split(".")
                .mapNotNull { it.trim().toIntOrNull() }
        }
    }
}
