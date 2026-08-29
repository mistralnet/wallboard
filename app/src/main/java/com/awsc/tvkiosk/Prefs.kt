package com.awsc.tvkiosk

import android.content.Context

/** Central place for stored settings. */
object Prefs {
    private const val FILE = "awsc_tv_prefs"
    private const val KEY_URL = "home_url"
    private const val KEY_INTERVAL_HOURS = "refresh_interval_hours"

    const val DEFAULT_URL = "https://awsc.co.il/"
    const val DEFAULT_INTERVAL_HOURS = 6

    private fun prefs(ctx: Context) =
        ctx.getSharedPreferences(FILE, Context.MODE_PRIVATE)

    fun getUrl(ctx: Context): String =
        prefs(ctx).getString(KEY_URL, DEFAULT_URL) ?: DEFAULT_URL

    fun setUrl(ctx: Context, url: String) =
        prefs(ctx).edit().putString(KEY_URL, url).apply()

    /** Auto-refresh interval in hours. 0 means disabled. */
    fun getIntervalHours(ctx: Context): Int =
        prefs(ctx).getInt(KEY_INTERVAL_HOURS, DEFAULT_INTERVAL_HOURS)

    fun setIntervalHours(ctx: Context, hours: Int) =
        prefs(ctx).edit().putInt(KEY_INTERVAL_HOURS, hours).apply()
}
