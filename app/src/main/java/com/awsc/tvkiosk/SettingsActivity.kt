package com.awsc.tvkiosk

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {

    private lateinit var urlInput: EditText
    private lateinit var intervalGroup: RadioGroup
    private lateinit var zoomGroup: RadioGroup
    private lateinit var updateUrlInput: EditText
    private lateinit var updateButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        urlInput = findViewById(R.id.url_input)
        intervalGroup = findViewById(R.id.interval_group)
        zoomGroup = findViewById(R.id.zoom_group)
        updateUrlInput = findViewById(R.id.update_url_input)
        updateButton = findViewById(R.id.update_button)

        urlInput.setText(Prefs.getUrl(this))
        intervalGroup.check(radioIdForHours(Prefs.getIntervalHours(this)))
        zoomGroup.check(radioIdForZoom(Prefs.getZoom(this)))
        updateUrlInput.setText(Prefs.getUpdateUrl(this))

        val version = try {
            packageManager.getPackageInfo(packageName, 0).versionName
        } catch (_: Exception) {
            "?"
        }
        findViewById<TextView>(R.id.version_label).text =
            getString(R.string.current_version) + ": " + version

        findViewById<Button>(R.id.save_button).setOnClickListener { save() }
        findViewById<Button>(R.id.refresh_button).setOnClickListener {
            save(showToast = false)
            finish()
        }
        updateButton.setOnClickListener { startUpdate() }
    }

    private fun save(showToast: Boolean = true) {
        var url = urlInput.text.toString().trim()
        if (url.isEmpty()) url = Prefs.DEFAULT_URL
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "https://$url"
        }
        Prefs.setUrl(this, url)
        Prefs.setIntervalHours(this, hoursForRadioId(intervalGroup.checkedRadioButtonId))
        Prefs.setZoom(this, zoomForRadioId(zoomGroup.checkedRadioButtonId))

        var updateUrl = updateUrlInput.text.toString().trim()
        if (updateUrl.isNotEmpty() &&
            !updateUrl.startsWith("http://") && !updateUrl.startsWith("https://")
        ) {
            updateUrl = "https://$updateUrl"
        }
        if (updateUrl.isEmpty()) updateUrl = Prefs.DEFAULT_UPDATE_URL
        Prefs.setUpdateUrl(this, updateUrl)

        if (showToast) {
            Toast.makeText(this, "נשמר", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun startUpdate() {
        // Persist the current update URL first so the downloader uses it.
        var updateUrl = updateUrlInput.text.toString().trim()
        if (updateUrl.isNotEmpty() &&
            !updateUrl.startsWith("http://") && !updateUrl.startsWith("https://")
        ) {
            updateUrl = "https://$updateUrl"
        }
        if (updateUrl.isEmpty()) updateUrl = Prefs.DEFAULT_UPDATE_URL
        Prefs.setUpdateUrl(this, updateUrl)

        updateButton.isEnabled = false
        updateButton.text = "מוריד..."
        Toast.makeText(this, "מוריד עדכון...", Toast.LENGTH_SHORT).show()

        Updater.downloadAndInstall(this, object : Updater.Callback {
            override fun onProgress(percent: Int) {
                updateButton.text = "מוריד... $percent%"
            }

            override fun onError(message: String) {
                updateButton.isEnabled = true
                updateButton.text = getString(R.string.update_now)
                Toast.makeText(this@SettingsActivity, "עדכון נכשל: $message", Toast.LENGTH_LONG).show()
            }

            override fun onReadyToInstall() {
                updateButton.isEnabled = true
                updateButton.text = getString(R.string.update_now)
            }
        })
    }

    private fun radioIdForHours(hours: Int): Int = when (hours) {
        0 -> R.id.interval_off
        1 -> R.id.interval_1
        3 -> R.id.interval_3
        12 -> R.id.interval_12
        24 -> R.id.interval_24
        else -> R.id.interval_6
    }

    private fun hoursForRadioId(id: Int): Int = when (id) {
        R.id.interval_off -> 0
        R.id.interval_1 -> 1
        R.id.interval_3 -> 3
        R.id.interval_12 -> 12
        R.id.interval_24 -> 24
        else -> 6
    }

    private fun radioIdForZoom(zoom: Int): Int = when (zoom) {
        75 -> R.id.zoom_75
        90 -> R.id.zoom_90
        110 -> R.id.zoom_110
        125 -> R.id.zoom_125
        else -> R.id.zoom_100
    }

    private fun zoomForRadioId(id: Int): Int = when (id) {
        R.id.zoom_75 -> 75
        R.id.zoom_90 -> 90
        R.id.zoom_110 -> 110
        R.id.zoom_125 -> 125
        else -> 100
    }
}
