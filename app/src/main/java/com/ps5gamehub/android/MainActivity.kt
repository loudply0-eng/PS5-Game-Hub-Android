package com.ps5gamehub.android

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import androidx.core.content.FileProvider
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest
import kotlin.concurrent.thread

class MainActivity : Activity() {
    private lateinit var selectedFolder: TextView
    private lateinit var updateStatus: TextView
    private lateinit var updateButton: Button
    private lateinit var progress: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pendingInstallFile = savedInstanceState?.getString(STATE_PENDING_INSTALL_FILE)
            ?.let { name -> File(File(cacheDir, UPDATE_DIRECTORY), name).takeIf { it.isFile } }
        val page = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(28, 42, 28, 28)
            setBackgroundColor(Color.rgb(15, 18, 27))
        }
        page.addView(TextView(this).apply {
            text = "PS5 GAME HUB"; textSize = 27f; setTextColor(Color.WHITE)
            typeface = Typeface.DEFAULT_BOLD
        })
        page.addView(TextView(this).apply {
            text = "ANDROID EDITION  •  EXPERIMENTAL"; textSize = 13f
            setTextColor(Color.rgb(150, 170, 200)); setPadding(0, 8, 0, 28)
        })
        page.addView(TextView(this).apply {
            text = "ENGINE STATUS\nKytyPS5 Android: not integrated"; textSize = 16f
            setTextColor(Color.WHITE); setPadding(18, 18, 18, 18)
            setBackgroundColor(Color.rgb(29, 35, 49))
        }, params())
        selectedFolder = TextView(this).apply {
            text = "No game folder selected"; textSize = 14f
            setTextColor(Color.LTGRAY); setPadding(0, 22, 0, 18)
        }
        page.addView(selectedFolder)
        page.addView(Button(this).apply {
            text = "Choose legally owned game folder"
            setOnClickListener { chooseGameFolder() }
        }, params())
        page.addView(Button(this).apply {
            text = "Launch game — engine integration pending"; isEnabled = false
        }, params())
        page.addView(TextView(this).apply {
            text = "APP UPDATES"; textSize = 16f; typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.WHITE); setPadding(0, 28, 0, 8)
        })
        updateStatus = TextView(this).apply {
            text = "Installed version: ${installedVersionName()}"; textSize = 13f
            setTextColor(Color.rgb(180, 190, 205))
        }
        page.addView(updateStatus)
        progress = ProgressBar(this).apply { visibility = View.GONE; isIndeterminate = true }
        page.addView(progress)
        updateButton = Button(this).apply {
            text = "Check for updates"; setOnClickListener { checkForUpdates() }
        }
        page.addView(updateButton, params())
        page.addView(TextView(this).apply {
            text = "\nUpdates are downloaded from this project's official GitHub Releases. The APK must include a GitHub SHA-256 asset digest. Android verifies the installed app's signing identity and asks you to approve installation. The app shell is not yet an emulator; native engine integration and device testing are pending."
            textSize = 13f; setTextColor(Color.rgb(180, 190, 205))
        })
        setContentView(ScrollView(this).apply { addView(page) })
        // Check for a stable update automatically when the app opens; installation still requires user approval.
        checkForUpdates()
    }

    private fun checkForUpdates() {
        setBusy(true, "Checking the latest stable release…")
        thread {
            try {
                val release = getJson(RELEASES_API)
                if (release.optBoolean("draft") || release.optBoolean("prerelease")) {
                    throw IllegalStateException("The latest release is not a stable published release.")
                }
                val tag = release.optString("tag_name")
                val latestVersion = UpdatePolicy.parseVersion(tag)
                    ?: throw IllegalStateException("Release tag is not a valid version: $tag")
                val currentVersion = UpdatePolicy.parseVersion(installedVersionName())
                    ?: throw IllegalStateException("Installed app version is invalid.")
                if (UpdatePolicy.compareVersions(latestVersion, currentVersion) <= 0) {
                    runOnUiThread { setBusy(false, "You are using the latest version (${installedVersionName()}).") }
                    return@thread
                }

                val assets = release.optJSONArray("assets")
                    ?: throw IllegalStateException("Release has no assets.")
                val apkAssets = (0 until assets.length())
                    .map { assets.getJSONObject(it) }
                    .filter { it.optString("name").endsWith(".apk", ignoreCase = true) }
                if (apkAssets.size != 1) {
                    throw IllegalStateException("Release must contain exactly one APK asset; update blocked.")
                }
                val asset = apkAssets.single()
                val digest = asset.optString("digest")
                if (!digest.matches(Regex("^sha256:[0-9a-fA-F]{64}$"))) {
                    throw IllegalStateException("Release APK has no valid GitHub SHA-256 digest. Update blocked for safety.")
                }
                val downloadUrl = asset.optString("browser_download_url")
                val uri = Uri.parse(downloadUrl)
                if (uri.scheme != "https" || uri.host != "github.com") {
                    throw IllegalStateException("APK download host is not the official GitHub host.")
                }
                val notes = release.optString("body").take(2500)
                runOnUiThread {
                    setBusy(false, "Update available: $tag")
                    AlertDialog.Builder(this)
                        .setTitle("Update available — $tag")
                        .setMessage("Current version: ${installedVersionName()}\nNew version: $tag\n\n$notes\n\nThe APK will be downloaded and checked before Android's installer opens.")
                        .setNegativeButton("Later", null)
                        .setPositiveButton("Download and install") { _, _ ->
                            downloadAndInstall(downloadUrl, digest.removePrefix("sha256:"))
                        }.show()
                }
            } catch (e: Exception) {
                runOnUiThread { setBusy(false, "Update check failed: ${e.message ?: "network error"}") }
            }
        }
    }

    private fun downloadAndInstall(downloadUrl: String, expectedSha256: String) {
        setBusy(true, "Downloading update…")
        thread {
            var temp: File? = null
            var connection: HttpURLConnection? = null
            try {
                val updateDirectory = File(cacheDir, UPDATE_DIRECTORY)
                if (!updateDirectory.exists() && !updateDirectory.mkdirs()) {
                    throw IllegalStateException("Cannot create the private update cache.")
                }
                val activeConnection = openVerifiedDownloadConnection(downloadUrl)
                connection = activeConnection
                val length = activeConnection.contentLengthLong
                if (length <= 0L || length > MAX_APK_BYTES) {
                    throw IllegalStateException("APK size is missing or exceeds the safety limit.")
                }
                temp = File(updateDirectory, UPDATE_TEMP_FILE)
                val digest = MessageDigest.getInstance("SHA-256")
                var total = 0L
                activeConnection.inputStream.use { input ->
                    FileOutputStream(temp).use { output ->
                        val buffer = ByteArray(64 * 1024)
                        while (true) {
                            val count = input.read(buffer)
                            if (count < 0) break
                            total += count
                            if (total > MAX_APK_BYTES) throw IllegalStateException("APK exceeds the safety limit.")
                            digest.update(buffer, 0, count)
                            output.write(buffer, 0, count)
                        }
                        output.fd.sync()
                    }
                }
                val actual = digest.digest().joinToString("") { "%02x".format(it) }
                if (total != length) throw IllegalStateException("Downloaded APK size does not match the server response.")
                if (!actual.equals(expectedSha256, ignoreCase = true)) {
                    throw SecurityException("APK SHA-256 mismatch. The downloaded update was rejected.")
                }
                val apkFile = File(updateDirectory, UPDATE_APK_FILE)
                if (apkFile.exists() && !apkFile.delete()) throw IllegalStateException("Cannot replace cached update.")
                if (!temp.renameTo(apkFile)) throw IllegalStateException("Cannot finalize downloaded APK.")
                temp = null

                runOnUiThread {
                    setBusy(false, "Download verified. Preparing Android installer…")
                    if (android.os.Build.VERSION.SDK_INT >= 26 && !packageManager.canRequestPackageInstalls()) {
                        pendingInstallFile = apkFile
                        AlertDialog.Builder(this)
                            .setTitle("Allow app updates")
                            .setMessage("Android needs permission to install updates initiated by this app. Enable it on the next screen, then return here and tap Check for updates again.")
                            .setPositiveButton("Open settings") { _, _ ->
                                startActivity(Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                                    Uri.parse("package:$packageName")))
                            }
                            .setNegativeButton("Cancel", null)
                            .show()
                    } else {
                        openSystemInstaller(apkFile)
                    }
                }
            } catch (e: Exception) {
                temp?.delete()
                runOnUiThread { setBusy(false, "Update download rejected: ${e.message ?: "unknown error"}") }
            } finally {
                connection?.disconnect()
            }
        }
    }

    private fun openVerifiedDownloadConnection(address: String): HttpURLConnection {
        var current = UpdatePolicy.trustedDownloadUrl(address)
            ?: throw SecurityException("APK URL is not on the HTTPS GitHub allowlist.")
        repeat(MAX_REDIRECTS + 1) { hop ->
            val connection = (current.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 15000
                readTimeout = 30000
                instanceFollowRedirects = false
                setRequestProperty("User-Agent", "PS5-Game-Hub-Android")
                setRequestProperty("Accept", "application/octet-stream")
            }
            val status = connection.responseCode
            if (status in 300..399) {
                val location = connection.getHeaderField("Location")
                connection.disconnect()
                if (location.isNullOrBlank() || hop == MAX_REDIRECTS) {
                    throw SecurityException("APK download exceeded the redirect limit or omitted Location.")
                }
                val nextUrl = URL(current, location)
                current = UpdatePolicy.trustedDownloadUrl(nextUrl.toString())
                    ?: throw SecurityException("APK redirect is not on the HTTPS GitHub allowlist.")
            } else {
                if (status !in 200..299) {
                    connection.disconnect()
                    throw IllegalStateException("Download server returned HTTP $status.")
                }
                return connection
            }
        }
        throw SecurityException("APK download redirect limit exceeded.")
    }

    private var pendingInstallFile: File? = null

    override fun onSaveInstanceState(outState: Bundle) {
        pendingInstallFile?.takeIf { it.isFile }?.let { outState.putString(STATE_PENDING_INSTALL_FILE, it.name) }
        super.onSaveInstanceState(outState)
    }

    override fun onResume() {
        super.onResume()
        val pending = pendingInstallFile ?: return
        if (android.os.Build.VERSION.SDK_INT < 26 || packageManager.canRequestPackageInstalls()) {
            pendingInstallFile = null
            openSystemInstaller(pending)
        }
    }

    private fun openSystemInstaller(apkFile: File) {
        try {
            val contentUri = FileProvider.getUriForFile(this, "$packageName.fileprovider", apkFile)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(contentUri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(intent)
            updateStatus.text = "Verified update handed to Android installer. Approve it to complete installation."
        } catch (e: Exception) {
            updateStatus.text = "Could not open Android installer: ${e.message}"
        }
    }

    private fun getJson(address: String): JSONObject {
        // The release metadata endpoint is trusted only on the official GitHub API host.
        val requested = URL(address)
        if (requested.protocol != "https" || requested.host != "api.github.com") {
            throw SecurityException("Update metadata URL is not the official GitHub API host.")
        }
        val connection = (requested.openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 12000
            readTimeout = 12000
            instanceFollowRedirects = false
            setRequestProperty("Accept", "application/vnd.github+json")
            setRequestProperty("User-Agent", "PS5-Game-Hub-Android")
        }
        try {
            val status = connection.responseCode
            if (status == 404) {
                throw IllegalStateException("No official app release has been published yet.")
            }
            if (status !in 200..299) {
                throw IllegalStateException("GitHub API returned HTTP $status.")
            }
            val finalUrl = URL(connection.url.toString())
            if (finalUrl.protocol != "https" || finalUrl.host != "api.github.com") {
                throw SecurityException("Update metadata redirected away from the official GitHub API.")
            }
            val body = connection.inputStream.use { input ->
                val output = java.io.ByteArrayOutputStream()
                val buffer = ByteArray(8192)
                var total = 0
                while (true) {
                    val count = input.read(buffer)
                    if (count < 0) break
                    total += count
                    if (total > 2 * 1024 * 1024) {
                        throw IllegalStateException("GitHub release metadata exceeds the size limit.")
                    }
                    output.write(buffer, 0, count)
                }
                output.toString(Charsets.UTF_8.name())
            }
            return JSONObject(body)
        } finally {
            connection.disconnect()
        }
    }

    private fun installedVersionName(): String =
        packageManager.getPackageInfo(packageName, 0).versionName ?: "0.0.0"

    private fun setBusy(busy: Boolean, message: String) {
        updateButton.isEnabled = !busy
        progress.visibility = if (busy) View.VISIBLE else View.GONE
        updateStatus.text = message
    }

    private fun chooseGameFolder() {
        startActivityForResult(Intent(Intent.ACTION_OPEN_DOCUMENT_TREE), REQUEST_GAME_FOLDER)
    }

    @Deprecated("Use Activity Result APIs in a later iteration")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_GAME_FOLDER && resultCode == RESULT_OK) {
            val uri = data?.data ?: return
            contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            selectedFolder.text = "Selected folder:\n$uri"
        }
    }

    private fun params() = LinearLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
    ).apply { bottomMargin = 14 }

    companion object {
        private const val REQUEST_GAME_FOLDER = 1001
        private const val MAX_APK_BYTES = 700L * 1024L * 1024L
        private const val MAX_REDIRECTS = 5
        private const val UPDATE_DIRECTORY = "update"
        private const val UPDATE_TEMP_FILE = "app-update.apk.part"
        private const val UPDATE_APK_FILE = "app-update.apk"
        private const val STATE_PENDING_INSTALL_FILE = "pending_install_file"
        private const val RELEASES_API = "https://api.github.com/repos/loudply0-eng/PS5-Game-Hub-Android/releases/latest"
    }
}
