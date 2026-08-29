package com.awsc.tvkiosk

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Best-effort auto-start when the TV finishes booting. Newer Android versions
 * restrict launching an activity from the background, so this may not fire on
 * every device — the app also relaunches when the user opens it from the home
 * screen or when the TV resumes it.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val launch = Intent(context, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            try {
                context.startActivity(launch)
            } catch (_: Exception) {
                // Background activity start blocked by the OS — ignore.
            }
        }
    }
}
