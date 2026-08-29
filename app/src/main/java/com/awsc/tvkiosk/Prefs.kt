package com.awsc.tvkiosk

import android.content.Context

/** Central place for stored settings. */
object Prefs {
    private const val FILE = "awsc_tv_prefs"
    private const val KEY_URL = "home_url"
    private const val KEY_INTERVAL_HOURS = "refresh_interval_hours"
    private const val KEY_ZOOM = "zoom_percent"
    private const val KEY_UPDATE_URL = "update_url"
    private const val KEY_BOOT_ENABLED = "boot_enabled"

    const val DEFAULT_URL = "https://awsc.co.il/"
    const val DEFAULT_INTERVAL_HOURS = 6
    const val DEFAULT_ZOOM = 100
    const val DEFAULT_UPDATE_URL = "https://awsc.co.il/awsc-tv.apk"

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

    /** Display zoom as a percentage (WebView initial scale). */
    fun getZoom(ctx: Context): Int =
        prefs(ctx).getInt(KEY_ZOOM, DEFAULT_ZOOM)

    fun setZoom(ctx: Context, percent: Int) =
        prefs(ctx).edit().putInt(KEY_ZOOM, percent).apply()

    /** URL of the APK used by the in-app updater. */
    fun getUpdateUrl(ctx: Context): String =
        prefs(ctx).getString(KEY_UPDATE_URL, DEFAULT_UPDATE_URL) ?: DEFAULT_UPDATE_URL

    fun setUpdateUrl(ctx: Context, url: String) =
        prefs(ctx).edit().putString(KEY_UPDATE_URL, url).apply()

    /** Whether the app launches automatically when the TV boots. */
    fun isBootEnabled(ctx: Context): Boolean =
        prefs(ctx).getBoolean(KEY_BOOT_ENABLED, true)

    fun setBootEnabled(ctx: Context, enabled: Boolean) =
        prefs(ctx).edit().putBoolean(KEY_BOOT_ENABLED, enabled).apply()
}
