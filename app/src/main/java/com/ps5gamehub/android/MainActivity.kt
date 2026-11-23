package com.ps5gamehub.android

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

class MainActivity : Activity() {
    private lateinit var selectedFolder: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val page = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(28, 42, 28, 28)
            setBackgroundColor(Color.rgb(15, 18, 27))
        }

        val title = TextView(this).apply {
            text = "PS5 GAME HUB"
            textSize = 27f
            setTextColor(Color.WHITE)
            typeface = Typeface.DEFAULT_BOLD
        }
        page.addView(title)

        val subtitle = TextView(this).apply {
            text = "ANDROID EDITION  •  EXPERIMENTAL"
            textSize = 13f
            setTextColor(Color.rgb(150, 170, 200))
            setPadding(0, 8, 0, 28)
        }
        page.addView(subtitle)

        val engineStatus = TextView(this).apply {
            text = "ENGINE STATUS\nKytyPS5 Android: not integrated"
            textSize = 16f
            setTextColor(Color.WHITE)
            setPadding(18, 18, 18, 18)
            setBackgroundColor(Color.rgb(29, 35, 49))
        }
        page.addView(engineStatus, params())

        selectedFolder = TextView(this).apply {
            text = "No game folder selected"
            textSize = 14f
            setTextColor(Color.LTGRAY)
            setPadding(0, 22, 0, 18)
        }
        page.addView(selectedFolder)

        page.addView(Button(this).apply {
            text = "Choose legally owned game folder"
            setOnClickListener { chooseGameFolder() }
        }, params())

        page.addView(Button(this).apply {
            text = "Launch game — engine integration pending"
            isEnabled = false
        }, params())

        page.addView(TextView(this).apply {
            text = "\nImportant\nThis first build is an app shell, not an emulator. Native engine integration and testing on the target phone are still required. No games, keys, firmware or proprietary system files are included."
            textSize = 13f
            setTextColor(Color.rgb(180, 190, 205))
        })

        setContentView(ScrollView(this).apply { addView(page) })
    }

    private fun chooseGameFolder() {
        startActivityForResult(
            Intent(Intent.ACTION_OPEN_DOCUMENT_TREE),
            REQUEST_GAME_FOLDER
        )
    }

    @Deprecated("Use Activity Result APIs in a later iteration")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_GAME_FOLDER && resultCode == RESULT_OK) {
            val uri = data?.data ?: return
            contentResolver.takePersistableUriPermission(
                uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            selectedFolder.text = "Selected folder:\n$uri"
        }
    }

    private fun params() = LinearLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.WRAP_CONTENT
    ).apply {
        bottomMargin = 14
    }

    companion object {
        private const val REQUEST_GAME_FOLDER = 1001
    }
}
