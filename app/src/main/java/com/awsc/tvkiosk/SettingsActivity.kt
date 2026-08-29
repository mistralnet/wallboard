package com.awsc.tvkiosk

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {

    private lateinit var urlInput: EditText
    private lateinit var intervalGroup: RadioGroup

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        urlInput = findViewById(R.id.url_input)
        intervalGroup = findViewById(R.id.interval_group)

        urlInput.setText(Prefs.getUrl(this))
        intervalGroup.check(radioIdForHours(Prefs.getIntervalHours(this)))

        findViewById<Button>(R.id.save_button).setOnClickListener { save() }
        findViewById<Button>(R.id.refresh_button).setOnClickListener {
            save(showToast = false)
            // Returning to MainActivity triggers a reload in its onResume.
            finish()
        }
    }

    private fun save(showToast: Boolean = true) {
        var url = urlInput.text.toString().trim()
        if (url.isEmpty()) url = Prefs.DEFAULT_URL
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "https://$url"
        }
        Prefs.setUrl(this, url)
        Prefs.setIntervalHours(this, hoursForRadioId(intervalGroup.checkedRadioButtonId))
        if (showToast) {
            Toast.makeText(this, "נשמר", Toast.LENGTH_SHORT).show()
            finish()
        }
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
}
