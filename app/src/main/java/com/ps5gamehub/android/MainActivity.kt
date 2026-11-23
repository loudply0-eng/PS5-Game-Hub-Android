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
import org.json.JSONObject
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
            text = "Installed version: $APP_VERSION"; textSize = 13f
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
            text = "\nSecurity note\nUpdates are checked against this project's official GitHub Releases. Android requires user approval to install. This app shell is not yet an emulator; native engine integration and device testing are pending."
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
                    requestMethod = "GET"; connectTimeout = 10000; readTimeout = 10000
                    setRequestProperty("Accept", "application/vnd.github+json")
                    setRequestProperty("User-Agent", "PS5-Game-Hub-Android")
                }
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                connection.disconnect()
                val release = JSONObject(response)
                val tag = release.optString("tag_name", "")
                val pageUrl = release.optString("html_url", RELEASES_PAGE)
                val notes = release.optString("body", "")
                val assets = release.optJSONArray("assets")
                var apkUrl: String? = null
                if (assets != null) {
                    for (i in 0 until assets.length()) {
                        val asset = assets.getJSONObject(i)
                        if (asset.optString("name").endsWith(".apk", ignoreCase = true)) {
                            apkUrl = asset.optString("browser_download_url"); break
                        }
                    }
                }
                runOnUiThread {
                    progress.visibility = View.GONE; updateButton.isEnabled = true
                    if (tag.isBlank()) {
                        updateStatus.text = "No published release found yet."
                        return@runOnUiThread
                    }
                    updateStatus.text = "Latest official release: $tag"
                    AlertDialog.Builder(this).setTitle("Official release $tag")
                        .setMessage("Review the release notes before downloading. Android will ask before installing.\n\n$notes")
                        .setNegativeButton("Close", null)
                        .setPositiveButton(if (apkUrl != null) "Open APK download" else "Open release page") { _, _ ->
                            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(apkUrl ?: pageUrl)))
                        }.show()
                }
            } catch (e: Exception) {
                runOnUiThread {
                    progress.visibility = View.GONE; updateButton.isEnabled = true
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
        private const val APP_VERSION = "0.1.0"
        private const val RELEASES_API = "https://api.github.com/repos/loudply0-eng/PS5-Game-Hub-Android/releases/latest"
        private const val RELEASES_PAGE = "https://github.com/loudply0-eng/PS5-Game-Hub-Android/releases"
    }
}