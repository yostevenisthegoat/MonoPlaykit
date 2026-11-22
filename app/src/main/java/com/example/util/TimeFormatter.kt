package com.example.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object TimeFormatter {
    fun formatStopwatch(ms: Long): String {
        val safeMs = ms.coerceAtLeast(0L)
        val totalSeconds = safeMs / 1000L
        val centis = (safeMs % 1000L) / 10L
        val minutes = (totalSeconds / 60L) % 60L
        val hours = totalSeconds / 3600L
        val seconds = totalSeconds % 60L
        return if (hours > 0) {
            String.format(Locale.US, "%d:%02d:%02d.%02d", hours, minutes, seconds, centis)
        } else {
            String.format(Locale.US, "%02d:%02d.%02d", minutes, seconds, centis)
        }
    }

    fun formatCubeTime(ms: Long, penalty: String = ""): String {
        if (penalty == "DNF") return "DNF"
        val effectiveMs = (ms + if (penalty == "+2") 2000L else 0L).coerceAtLeast(0L)
        val totalSeconds = effectiveMs / 1000L
        val centis = (effectiveMs % 1000L) / 10L
        val minutes = totalSeconds / 60L
        val seconds = totalSeconds % 60L
        val suffix = if (penalty == "+2") "+" else ""
        return if (minutes > 0) {
            String.format(Locale.US, "%d:%02d.%02d%s", minutes, seconds, centis, suffix)
        } else {
            String.format(Locale.US, "%d.%02d%s", seconds, centis, suffix)
        }
    }

    fun formatClock(ms: Long): String {
        val safeMs = ms.coerceAtLeast(0L)
        val totalSeconds = (safeMs + 999L) / 1000L
        val hours = totalSeconds / 3600L
        val minutes = (totalSeconds % 3600L) / 60L
        val seconds = totalSeconds % 60L
        return when {
            hours > 0 -> String.format(Locale.US, "%d:%02d:%02d", hours, minutes, seconds)
            safeMs in 1..19_999L -> {
                val sec = safeMs / 1000L
                val tenths = (safeMs % 1000L) / 100L
                String.format(Locale.US, "%02d.%d", sec, tenths)
            }
            else -> String.format(Locale.US, "%02d:%02d", minutes, seconds)
        }
    }

    fun formatMinSec(ms: Long): String {
        val totalSeconds = (ms.coerceAtLeast(0L) + 999L) / 1000L
        val hours = totalSeconds / 3600L
        val minutes = (totalSeconds % 3600L) / 60L
        val seconds = totalSeconds % 60L
        return if (hours > 0) {
            String.format(Locale.US, "%d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format(Locale.US, "%02d:%02d", minutes, seconds)
        }
    }

    fun formatTimestamp(timestampMs: Long): String {
        val sdf = SimpleDateFormat("MMM d, HH:mm", Locale.getDefault())
        return sdf.format(Date(timestampMs))
    }
}
