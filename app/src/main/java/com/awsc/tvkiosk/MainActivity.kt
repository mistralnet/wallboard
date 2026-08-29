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
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var progress: ProgressBar

    private val handler = Handler(Looper.getMainLooper())
    private var loadedUrl: String = ""

    private val autoRefresh = object : Runnable {
        override fun run() {
            webView.reload()
            scheduleAutoRefresh()
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        webView = findViewById(R.id.webview)
        progress = findViewById(R.id.progress)

        with(webView.settings) {
            javaScriptEnabled = true
            domStorageEnabled = true
            loadWithOverviewMode = true
            useWideViewPort = true
            mediaPlaybackRequiresUserGesture = false
            cacheMode = WebSettings.LOAD_DEFAULT
            builtInZoomControls = false
            displayZoomControls = false
        }

        // Keep all navigation inside the WebView (no external browser).
        webView.webViewClient = WebViewClient()
        webView.webChromeClient = WebChromeClient()

        loadHomeUrl()
    }

    private fun loadHomeUrl() {
        loadedUrl = Prefs.getUrl(this)
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

    override fun onResume() {
        super.onResume()
        enableImmersive()

        val currentUrl = Prefs.getUrl(this)
        if (currentUrl != loadedUrl) {
            // URL changed in settings — load the new page.
            loadHomeUrl()
        } else {
            // Same page — refresh so returning to the app shows fresh content.
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
                // Track for a possible long-press (opens settings).
                event.startTracking()
                return true
            }
            KeyEvent.KEYCODE_MEDIA_PLAY,
            KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE -> {
                webView.reload()
                return true
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
            // Short press: go back in web history, but never exit the kiosk.
            if (webView.canGoBack()) webView.goBack()
            return true
        }
        return super.onKeyUp(keyCode, event)
    }

    override fun onDestroy() {
        handler.removeCallbacks(autoRefresh)
        super.onDestroy()
    }
}
