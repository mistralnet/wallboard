package com.awsc.tvkiosk

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var progress: ProgressBar
    private lateinit var settingsButton: Button

    private val handler = Handler(Looper.getMainLooper())
    private var loadedUrl: String = ""

    private val autoRefresh = object : Runnable {
        override fun run() {
            webView.reload()
            scheduleAutoRefresh()
        }
    }

    private val hideButton = Runnable { settingsButton.visibility = View.GONE }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        webView = findViewById(R.id.webview)
        progress = findViewById(R.id.progress)
        settingsButton = findViewById(R.id.settings_button)

        // This is a signage display: the web page is not interactive, so keep
        // focus off the WebView so the on-screen settings button is reachable.
        webView.isFocusable = false
        webView.isFocusableInTouchMode = false

        settingsButton.setOnClickListener { openSettings() }

        with(webView.settings) {
            javaScriptEnabled = true
            domStorageEnabled = true
            loadWithOverviewMode = true
            useWideViewPort = true
            mediaPlaybackRequiresUserGesture = false
            cacheMode = WebSettings.LOAD_DEFAULT
            builtInZoomControls = false
            displayZoomControls = false
            // Ignore the TV's system font-size setting (fixes "everything huge").
            textZoom = 100
        }

        webView.webViewClient = WebViewClient()
        webView.webChromeClient = WebChromeClient()

        loadHomeUrl()
        showSettingsButton()
    }

    private fun loadHomeUrl() {
        loadedUrl = Prefs.getUrl(this)
        webView.setInitialScale(Prefs.getZoom(this))
        progress.visibility = View.VISIBLE
        webView.loadUrl(loadedUrl)
        webView.postDelayed({ progress.visibility = View.GONE }, 1500)
    }

    private fun scheduleAutoRefresh() {
        handler.removeCallbacks(autoRefresh)
        val hours = Prefs.getIntervalHours(this)
        if (hours > 0) {
            handler.postDelayed(autoRefresh, hours * 60L * 60L * 1000L)
        }
    }

    /** Briefly reveal the settings button, then auto-hide to keep the board clean. */
    private fun showSettingsButton() {
        settingsButton.visibility = View.VISIBLE
        settingsButton.requestFocus()
        handler.removeCallbacks(hideButton)
        handler.postDelayed(hideButton, 6000)
    }

    override fun onResume() {
        super.onResume()
        enableImmersive()

        val currentUrl = Prefs.getUrl(this)
        webView.setInitialScale(Prefs.getZoom(this))
        if (currentUrl != loadedUrl) {
            loadHomeUrl()
        } else {
            webView.reload()
        }
        scheduleAutoRefresh()
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(autoRefresh)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) enableImmersive()
    }

    private fun enableImmersive() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }

    private fun openSettings() {
        startActivity(Intent(this, SettingsActivity::class.java))
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_MENU -> {
                openSettings()
                return true
            }
            KeyEvent.KEYCODE_BACK -> {
                event.startTracking()
                return true
            }
            KeyEvent.KEYCODE_MEDIA_PLAY,
            KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE -> {
                webView.reload()
                return true
            }
            KeyEvent.KEYCODE_DPAD_UP,
            KeyEvent.KEYCODE_DPAD_DOWN,
            KeyEvent.KEYCODE_DPAD_LEFT,
            KeyEvent.KEYCODE_DPAD_RIGHT,
            KeyEvent.KEYCODE_DPAD_CENTER -> {
                // Any navigation key reveals the settings button.
                showSettingsButton()
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onKeyLongPress(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            openSettings()
            return true
        }
        return super.onKeyLongPress(keyCode, event)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && !event.isCanceled) {
            // Short press never exits the kiosk; reveal the settings button instead.
            showSettingsButton()
            return true
        }
        return super.onKeyUp(keyCode, event)
    }

    override fun onDestroy() {
        handler.removeCallbacks(autoRefresh)
        handler.removeCallbacks(hideButton)
        super.onDestroy()
    }
}
