package com.cronoszip.app.data

import android.content.Context
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class HistoryStore(context: Context) {
    private val prefs = context.getSharedPreferences("cronoszip.history", Context.MODE_PRIVATE)
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    fun load(): List<HistoryEntry> {
        val raw = prefs.getString("entries", null) ?: return emptyList()
        return runCatching {
            json.decodeFromString<List<HistoryEntry>>(raw)
        }.getOrDefault(emptyList())
    }

    fun add(entry: HistoryEntry): List<HistoryEntry> {
        val updated = (listOf(entry) + load().filterNot { it.path == entry.path })
            .sortedByDescending { it.timestamp }
            .take(12)
        prefs.edit().putString("entries", json.encodeToString(updated)).apply()
        return updated
    }

    fun clear() {
        prefs.edit().remove("entries").apply()
    }
}
