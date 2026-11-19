package com.example.util

import java.text.SimpleDateFormat
import java.util.Date
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

fun formatRelativeTime(timestampMs: Long, nowMs: Long = System.currentTimeMillis()): String {
    if (timestampMs <= 0L) return "Usage untracked"
    val diffMs = (nowMs - timestampMs).coerceAtLeast(0L)
    val minutes = diffMs / 60_000L
    val hours = diffMs / 3_600_000L
    val days = diffMs / 86_400_000L
    return when {
        minutes < 2L -> "Just now"
        minutes < 60L -> "${minutes}m ago"
        hours < 24L -> "${hours}h ago"
        days < 30L -> "${days}d ago"
        else -> SimpleDateFormat("MMM dd", Locale.US).format(Date(timestampMs))
    }
}

fun formatHistoryTime(timestampMs: Long): String {
    if (timestampMs <= 0L) return ""
    return SimpleDateFormat("HH:mm • MMM dd", Locale.US).format(Date(timestampMs))
}
