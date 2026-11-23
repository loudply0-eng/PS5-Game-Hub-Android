package com.ps5gamehub.android

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread

class MainActivity : Activity() {
    private lateinit var selectedFolder: TextView
    private lateinit var updateStatus: TextView
    private lateinit var updateButton: Button
    private lateinit var progress: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
            text = "Installed version: ${BuildConfig.VERSION_NAME}"; textSize = 13f
            setTextColor(Color.rgb(180, 190, 205))
        }
        page.addView(updateStatus)
        progress = ProgressBar(this).apply { visibility = View.GONE }
        page.addView(progress)
        updateButton = Button(this).apply {
            text = "Check for updates"; setOnClickListener { checkForUpdates() }
        }
        page.addView(updateButton, params())
        page.addView(TextView(this).apply {
            text = "\nSecurity note\nUpdates are checked against this project's official GitHub Releases. The app opens the official release page; review the release and verify its publisher before installing. Android checks APK signing during installation. This app shell is not yet an emulator; native engine integration and device testing are pending."
            textSize = 13f; setTextColor(Color.rgb(180, 190, 205))
        })
        setContentView(ScrollView(this).apply { addView(page) })
    }

    private fun checkForUpdates() {
        updateButton.isEnabled = false
        progress.visibility = View.VISIBLE
        updateStatus.text = "Checking official GitHub Releases…"
        thread {
            try {
                val connection = (URL(RELEASES_API).openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = 10000
                    readTimeout = 10000
                    instanceFollowRedirects = false
                    useCaches = false
                    setRequestProperty("Accept", "application/vnd.github+json")
                    setRequestProperty("User-Agent", "PS5-Game-Hub-Android")
                    setRequestProperty("X-GitHub-Api-Version", "2022-11-28")
                }
                val responseCode = connection.responseCode
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    connection.disconnect()
                    throw IllegalStateException("GitHub API returned HTTP $responseCode")
                }
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                connection.disconnect()

                val releases = JSONArray(response)
                if (releases.length() == 0) {
                    runOnUiThread {
                        progress.visibility = View.GONE
                        updateButton.isEnabled = true
                        updateStatus.text = "No official release published yet."
                    }
                    return@thread
                }

                val release = releases.getJSONObject(0)
                val tag = release.optString("tag_name", "").trim()
                val pageUrl = release.optString("html_url", "")
                val uri = Uri.parse(pageUrl)
                val trustedReleaseUrl = uri.scheme == "https" &&
                    uri.host == "github.com" &&
                    uri.path?.startsWith("/loudply0-eng/PS5-Game-Hub-Android/releases/") == true

                if (tag.isBlank() || !trustedReleaseUrl) {
                    throw IllegalStateException("Release metadata failed validation")
                }

                val notes = release.optString("body", "").take(4000)
                runOnUiThread {
                    progress.visibility = View.GONE
                    updateButton.isEnabled = true
                    updateStatus.text = "Latest official release: $tag"
                    AlertDialog.Builder(this)
                        .setTitle("Official release $tag")
                        .setMessage(
                            "Installed version: ${BuildConfig.VERSION_NAME}\nLatest release: $tag\n\n" +
                                "Review the release notes and verify the APK publisher before installing. " +
                                "The app will open only the official GitHub release page.\n\n$notes"
                        )
                        .setNegativeButton("Close", null)
                        .setPositiveButton("Open official release") { _, _ ->
                            startActivity(Intent(Intent.ACTION_VIEW, uri))
                        }
                        .show()
                }
            } catch (e: Exception) {
                runOnUiThread {
                    progress.visibility = View.GONE
                    updateButton.isEnabled = true
                    updateStatus.text = "Update check failed: " + (e.message ?: "network error")
                }
            }
        }
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
        private const val RELEASES_API = "https://api.github.com/repos/loudply0-eng/PS5-Game-Hub-Android/releases?per_page=1"
    }
}
