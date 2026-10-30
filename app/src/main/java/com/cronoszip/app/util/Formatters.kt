package com.cronoszip.app.util

import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

fun formatBytes(bytes: Long): String {
    if (bytes < 1024) return "$bytes B"
    val units = arrayOf("KB", "MB", "GB", "TB")
    var value = bytes.toDouble()
    var unit = -1
    while (value >= 1024 && unit < units.lastIndex) {
        value /= 1024.0
        unit++
    }
    return if (value >= 100) {
        String.format(Locale.US, "%.0f %s", value, units[unit])
    } else {
        String.format(Locale.US, "%.1f %s", value, units[unit])
    }
}

fun formatSpeed(bytesPerSecond: Double): String {
    if (bytesPerSecond <= 0.0 || !bytesPerSecond.isFinite()) return "--"
    return "${formatBytes(bytesPerSecond.toLong())}/s"
}

fun formatDuration(seconds: Long): String {
    if (seconds <= 0 || seconds == Long.MAX_VALUE) return "--"
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val secs = seconds % 60
    return when {
        hours > 0 -> "${hours}h ${minutes}m"
        minutes > 0 -> "${minutes}m ${secs}s"
        else -> "${secs}s"
    }
}

fun formatTime(millis: Long?): String {
    if (millis == null || millis <= 0) return ""
    val format = SimpleDateFormat("d MMM, HH:mm", Locale.getDefault())
    return format.format(Date(millis))
}

fun fileName(path: String): String = path.substringAfterLast('/').ifEmpty { path }

fun parentPath(path: String): String? {
    val parent = File(path).parent ?: return null
    return parent.takeIf { it != path }
}

fun formatPercent(fraction: Float): String =
    String.format(Locale.US, "%d%%", (fraction.coerceIn(0f, 1f) * 100).toInt())

fun formatCount(value: Int): String =
    String.format(Locale.US, "%,d", value)

fun formatRatio(done: Long, total: Long): String =
    "${formatBytes(done)} / ${formatBytes(total)}"

fun prettyFormat(format: String): String = when (format.lowercase()) {
    "zip" -> "ZIP"
    "7z" -> "7Z"
    "tar" -> "TAR"
    "tar.gz", "tgz" -> "TAR.GZ"
    "rar" -> "RAR"
    else -> format.uppercase(Locale.US)
}

fun isArchive(name: String): Boolean {
    val lower = name.lowercase(Locale.US)
    if (lower.endsWith(".zip") || lower.endsWith(".7z") || lower.endsWith(".tar") ||
        lower.endsWith(".tar.gz") || lower.endsWith(".tgz") || lower.endsWith(".rar") ||
        lower.endsWith(".exe")
    ) {
        return true
    }
    val tail = lower.substringAfterLast('.', "")
    return tail.length == 3 && tail.all { it.isDigit() }
}

fun normalizedSpeed(bytesPerSecond: Double): Double =
    if (bytesPerSecond.isFinite() && abs(bytesPerSecond) > 1.0) bytesPerSecond else 0.0
