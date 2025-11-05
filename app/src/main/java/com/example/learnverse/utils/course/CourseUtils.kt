package com.example.learnverse.utils.course

import androidx.compose.ui.graphics.Color

/**
 * Format duration from seconds to HH:MM:SS or MM:SS
 */
fun formatDuration(seconds: Long): String {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val secs = seconds % 60

    return if (hours > 0) {
        String.format("%d:%02d:%02d", hours, minutes, secs)
    } else {
        String.format("%d:%02d", minutes, secs)
    }
}

/**
 * Overload for Int
 */
fun formatDuration(seconds: Int): String {
    return formatDuration(seconds.toLong())
}

/**
 * Format relative time (e.g., "2 hours ago")
 * Simplified version - returns "Recently" for now
 */
fun formatRelativeTime(timestamp: String?): String {
    if (timestamp == null || timestamp.isEmpty()) return "Not started"

    // For now, return a simple message
    // You can enhance this with actual time difference calculation
    return "Recently"
}

/**
 * Get status color based on completion state
 */
fun getStatusColor(completed: Boolean, progressPercentage: Double): Color {
    return when {
        completed -> Color(0xFF4CAF50) // Green
        progressPercentage > 0 -> Color(0xFF2196F3) // Blue
        else -> Color(0xFF9E9E9E) // Gray
    }
}

/**
 * Get status icon emoji
 */
fun getStatusIcon(completed: Boolean, progressPercentage: Double): String {
    return when {
        completed -> "✅"
        progressPercentage > 0 -> "▶️"
        else -> "⚪"
    }
}

/**
 * Format file size
 */
fun formatFileSize(bytes: Long): String {
    val kb = bytes / 1024.0
    val mb = kb / 1024.0

    return when {
        mb >= 1 -> String.format("%.1f MB", mb)
        kb >= 1 -> String.format("%.1f KB", kb)
        else -> "$bytes B"
    }
}

/**
 * Get progress text (e.g., "8 of 12 completed")
 */
fun getProgressText(completed: Int, total: Int, itemName: String): String {
    return "$completed of $total $itemName"
}
