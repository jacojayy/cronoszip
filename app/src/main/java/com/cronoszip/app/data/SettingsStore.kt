package com.cronoszip.app.data

import android.content.Context

data class CronosSettings(
    val keepFreeGb: Int = 2,
    val keepArchiveByDefault: Boolean = false,
    val verifyFirstByDefault: Boolean = false,
    val animations: Boolean = true,
) {
    val keepFreeBytes: Long
        get() = keepFreeGb.coerceIn(1, 50) * 1024L * 1024L * 1024L
}

class SettingsStore(context: Context) {
    private val prefs = context.getSharedPreferences("cronoszip.settings", Context.MODE_PRIVATE)

    fun load(): CronosSettings = CronosSettings(
        keepFreeGb = prefs.getInt("keepFreeGb", 2),
        keepArchiveByDefault = prefs.getBoolean("keepArchive", false),
        verifyFirstByDefault = prefs.getBoolean("verifyFirst", false),
        animations = prefs.getBoolean("animations", true),
    )

    fun save(settings: CronosSettings) {
        prefs.edit()
            .putInt("keepFreeGb", settings.keepFreeGb)
            .putBoolean("keepArchive", settings.keepArchiveByDefault)
            .putBoolean("verifyFirst", settings.verifyFirstByDefault)
            .putBoolean("animations", settings.animations)
            .apply()
    }
}
