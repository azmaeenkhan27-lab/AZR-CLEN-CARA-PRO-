package com.example.util

import java.util.Locale
import kotlin.math.ln
import kotlin.math.pow

fun formatBytes(bytes: Long): String {
    if (bytes <= 0L) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    val digitGroups = (ln(bytes.toDouble()) / ln(1024.0)).toInt().coerceIn(0, units.lastIndex)
    val value = bytes / 1024.0.pow(digitGroups.toDouble())
    return when (digitGroups) {
        0 -> String.format(Locale.US, "%d B", bytes)
        1 -> String.format(Locale.US, "%.0f KB", value)
        2 -> String.format(Locale.US, "%.1f MB", value)
        else -> String.format(Locale.US, "%.2f %s", value, units[digitGroups])
    }
}

fun formatGbShort(bytes: Long): String {
    if (bytes <= 0L) return "0.0 GB"
    val gb = bytes / (1024.0 * 1024.0 * 1024.0)
    return String.format(Locale.US, "%.1f GB", gb)
}
