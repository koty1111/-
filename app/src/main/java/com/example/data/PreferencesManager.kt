package com.example.data

import android.content.Context
import android.content.SharedPreferences

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("hayya_al_falah_prefs", Context.MODE_PRIVATE)

    var tasbihCount: Int
        get() = prefs.getInt("tasbih_count", 0)
        set(value) = prefs.edit().putInt("tasbih_count", value).apply()

    var prayerAlertsEnabled: Boolean
        get() = prefs.getBoolean("prayer_alerts", true)
        set(value) = prefs.edit().putBoolean("prayer_alerts", value).apply()

    var visualEffectsEnabled: Boolean
        get() = prefs.getBoolean("visual_effects", true)
        set(value) = prefs.edit().putBoolean("visual_effects", value).apply()

    var backgroundAnimationEnabled: Boolean
        get() = prefs.getBoolean("background_anim", true)
        set(value) = prefs.edit().putBoolean("background_anim", value).apply()

    var darkModeEnabled: Boolean
        get() = prefs.getBoolean("dark_mode", true)
        set(value) = prefs.edit().putBoolean("dark_mode", value).apply()

    var saveTasbihEnabled: Boolean
        get() = prefs.getBoolean("save_tasbih", true)
        set(value) = prefs.edit().putBoolean("save_tasbih", value).apply()

    var adhanSoundEnabled: Boolean
        get() = prefs.getBoolean("adhan_sound", true)
        set(value) = prefs.edit().putBoolean("adhan_sound", value).apply()

    var lastCity: String
        get() = prefs.getString("last_city", "القاهرة، مصر") ?: "القاهرة، مصر"
        set(value) = prefs.edit().putString("last_city", value).apply()

    var lastLat: Float
        get() = prefs.getFloat("last_lat", 30.0444f)
        set(value) = prefs.edit().putFloat("last_lat", value).apply()

    var lastLon: Float
        get() = prefs.getFloat("last_lon", 31.2357f)
        set(value) = prefs.edit().putFloat("last_lon", value).apply()
}
