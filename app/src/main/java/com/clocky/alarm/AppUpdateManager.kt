package com.clocky.alarm

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

data class UpdateInfo(
    val versionCode: Int,
    val versionName: String,
    val apkUrl: String,
    val changelog: String
)

sealed class UpdateState {
    object Idle : UpdateState()
    object Checking : UpdateState()
    data class Available(val info: UpdateInfo) : UpdateState()
    object UpToDate : UpdateState()
    data class Downloading(val progress: Float) : UpdateState()
    data class ReadyToInstall(val apkFile: File) : UpdateState()
    data class Error(val message: String) : UpdateState()
}

object AppUpdateManager {

    fun getCurrentVersionCode(context: Context): Int {
        return try {
            val pInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(context.packageName, PackageManager.PackageInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(context.packageName, 0)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                pInfo.longVersionCode.toInt()
            } else {
                @Suppress("DEPRECATION")
                pInfo.versionCode
            }
        } catch (e: Exception) {
            1
        }
    }

    fun getCurrentVersionName(context: Context): String {
        return try {
            val pInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(context.packageName, PackageManager.PackageInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(context.packageName, 0)
            }
            pInfo.versionName ?: "1.0.0"
        } catch (e: Exception) {
            "1.0.0"
        }
    }

    /**
     * Checks remote endpoint for updates.
     * Supports both custom version.json schema and GitHub Releases API schema.
     */
    suspend fun checkForUpdate(endpointUrl: String, currentVersionCode: Int): UpdateState = withContext(Dispatchers.IO) {
        var connection: HttpURLConnection? = null
        try {
            val url = URL(endpointUrl)
            connection = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 8000
                readTimeout = 8000
                requestMethod = "GET"
                setRequestProperty("Accept", "application/json")
                setRequestProperty("User-Agent", "Clocky-Android")
            }

            val responseCode = connection.responseCode
            if (responseCode !in 200..299) {
                return@withContext UpdateState.Error("Server returned code $responseCode")
            }

            val jsonText = connection.inputStream.bufferedReader().use { it.readText() }
            val json = JSONObject(jsonText)

            val updateInfo = parseUpdateJson(json)
                ?: return@withContext UpdateState.Error("Invalid update payload format")

            if (updateInfo.versionCode > currentVersionCode) {
                UpdateState.Available(updateInfo)
            } else {
                UpdateState.UpToDate
            }
        } catch (e: Exception) {
            UpdateState.Error(e.message ?: "Failed to check for updates")
        } finally {
            connection?.disconnect()
        }
    }

    private fun parseUpdateJson(json: JSONObject): UpdateInfo? {
        // Schema 1: Dedicated version.json
        // { "versionCode": 2, "versionName": "1.1.0", "apkUrl": "...", "changelog": "..." }
        if (json.has("versionCode") && json.has("apkUrl")) {
            return UpdateInfo(
                versionCode = json.getInt("versionCode"),
                versionName = json.optString("versionName", "New Version"),
                apkUrl = json.getString("apkUrl"),
                changelog = json.optString("changelog", "Bug fixes and performance improvements.")
            )
        }

        // Schema 2: GitHub Releases latest API
        // { "tag_name": "v1.1.0", "body": "...", "assets": [ { "name": "...apk", "browser_download_url": "..." } ] }
        if (json.has("tag_name") && json.has("assets")) {
            val tagName = json.getString("tag_name")
            val rawVersion = tagName.removePrefix("v").trim()
            val assets = json.optJSONArray("assets")
            var apkUrl: String? = null
            if (assets != null) {
                for (i in 0 until assets.length()) {
                    val asset = assets.getJSONObject(i)
                    val name = asset.optString("name", "")
                    if (name.endsWith(".apk", ignoreCase = true)) {
                        apkUrl = asset.getString("browser_download_url")
                        break
                    }
                }
            }

            if (apkUrl != null) {
                // Compute integer code from semantic version (e.g. 1.2.0 -> 10200)
                val code = parseSemanticVersionToCode(rawVersion)
                return UpdateInfo(
                    versionCode = code,
                    versionName = rawVersion,
                    apkUrl = apkUrl,
                    changelog = json.optString("body", "What's new in $rawVersion")
                )
            }
        }

        return null
    }

    private fun parseSemanticVersionToCode(v: String): Int {
        val parts = v.split(".").mapNotNull { it.filter { c -> c.isDigit() }.toIntOrNull() }
        return when (parts.size) {
            1 -> parts[0] * 10000
            2 -> parts[0] * 10000 + parts[1] * 100
            else -> parts[0] * 10000 + parts[1] * 100 + parts[2]
        }
    }

    /**
     * Downloads the APK file to cache with progress callbacks.
     */
    suspend fun downloadApk(
        context: Context,
        apkUrl: String,
        onProgress: (Float) -> Unit
    ): File? = withContext(Dispatchers.IO) {
        var connection: HttpURLConnection? = null
        try {
            val url = URL(apkUrl)
            connection = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 15000
                readTimeout = 30000
                instanceFollowRedirects = true
            }

            val totalBytes = connection.contentLength
            val destinationFile = File(context.cacheDir, "Clocky_update.apk")
            if (destinationFile.exists()) destinationFile.delete()

            connection.inputStream.use { input ->
                FileOutputStream(destinationFile).use { output ->
                    val buffer = ByteArray(8192)
                    var bytesRead: Int
                    var totalRead = 0L

                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        totalRead += bytesRead
                        if (totalBytes > 0) {
                            val prog = (totalRead.toFloat() / totalBytes.toFloat()).coerceIn(0f, 1f)
                            onProgress(prog)
                        }
                    }
                    output.flush()
                }
            }
            destinationFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        } finally {
            connection?.disconnect()
        }
    }

    /**
     * Triggers Android's PackageInstaller to update the application seamlessly.
     */
    fun installApk(context: Context, apkFile: File) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (!context.packageManager.canRequestPackageInstalls()) {
                    Toast.makeText(context, "Please allow Clocky to install updates", Toast.LENGTH_LONG).show()
                    val permissionIntent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                        data = Uri.parse("package:${context.packageName}")
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(permissionIntent)
                    return
                }
            }

            val apkUri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                apkFile
            )

            val installIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(apkUri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            context.startActivity(installIntent)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Installation failed: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
