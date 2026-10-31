package com.cronoszip.app.data

import android.content.Context

data class CronosSettings(
    val keepFreeGb: Int = 2,
    val keepArchiveByDefault: Boolean = false,
    val verifyFirstByDefault: Boolean = false,
    val animations: Boolean = true,
    val customRoots: List<StorageRoot> = emptyList(),
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
        customRoots = decodeRoots(prefs.getString("customRoots", null)),
    )

    fun save(settings: CronosSettings) {
        prefs.edit()
            .putInt("keepFreeGb", settings.keepFreeGb)
            .putBoolean("keepArchive", settings.keepArchiveByDefault)
            .putBoolean("verifyFirst", settings.verifyFirstByDefault)
            .putBoolean("animations", settings.animations)
            .putString("customRoots", encodeRoots(settings.customRoots))
            .apply()
    }

    private fun encodeRoots(roots: List<StorageRoot>): String =
        roots.joinToString("\n") { "${it.label.replace("\n", " ")}\t${it.path}" }

    private fun decodeRoots(raw: String?): List<StorageRoot> {
        if (raw.isNullOrBlank()) return emptyList()
        return raw.lineSequence()
            .mapNotNull { line ->
                val tab = line.indexOf('\t')
                if (tab <= 0 || tab == line.lastIndex) null
                else StorageRoot(line.substring(0, tab), line.substring(tab + 1))
            }
            .filter { it.path.isNotBlank() }
            .toList()
    }
}
