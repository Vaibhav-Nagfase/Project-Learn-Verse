package com.example.learnverse.ui.screen.community

import androidx.compose.runtime.*
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlin.math.abs

/**
 * FIXED: Video Visibility Tracker - No Deadlock
 * Simplified logic: plays video when 50%+ visible, that's it!
 */
class VideoVisibilityTracker {
    private val _currentlyPlayingVideoId = mutableStateOf<String?>(null)
    val currentlyPlayingVideoId: State<String?> = _currentlyPlayingVideoId

    private val videoVisibilityMap = mutableStateMapOf<String, Float>()

    fun shouldPlayVideo(videoId: String): Boolean {
        return _currentlyPlayingVideoId.value == videoId
    }

    fun updateVideoVisibility(videoId: String, visibility: Float) {
        videoVisibilityMap[videoId] = visibility

        // Find most visible video (if any above 50%)
        val mostVisibleVideo = videoVisibilityMap
            .filter { it.value > 0.5f }
            .maxByOrNull { it.value }

        // Update playing video
        _currentlyPlayingVideoId.value = mostVisibleVideo?.key
    }

    fun removeVideo(videoId: String) {
        videoVisibilityMap.remove(videoId)
    }
}

@Composable
fun rememberVideoVisibilityTracker(): VideoVisibilityTracker {
    return remember { VideoVisibilityTracker() }
}

/**
 * SIMPLIFIED: Just track visibility percentage, tracker decides what plays
 */
@Composable
fun Modifier.trackVideoVisibility(
    videoId: String,
    visibilityTracker: VideoVisibilityTracker,
    onVisibilityChanged: (Float) -> Unit
): Modifier {
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current

    // Get actual screen height
    val screenHeightPx = with(density) {
        configuration.screenHeightDp.dp.toPx()
    }

    DisposableEffect(videoId) {
        onDispose {
            visibilityTracker.removeVideo(videoId)
        }
    }

    return this.onGloballyPositioned { layoutCoordinates ->
        val bounds = layoutCoordinates.boundsInWindow()

        // Calculate what portion of video is visible on screen
        val videoTop = bounds.top
        val videoBottom = bounds.bottom
        val videoHeight = bounds.height

        // Screen boundaries (0 to screen height)
        val screenTop = 0f
        val screenBottom = screenHeightPx

        // Calculate visible portion
        val visibleTop = maxOf(videoTop, screenTop)
        val visibleBottom = minOf(videoBottom, screenBottom)
        val visibleHeight = maxOf(0f, visibleBottom - visibleTop)

        // Visibility percentage (0.0 to 1.0)
        val visibilityPercentage = if (videoHeight > 0) {
            (visibleHeight / videoHeight).coerceIn(0f, 1f)
        } else {
            0f
        }

        // Update tracker with this video's visibility
        visibilityTracker.updateVideoVisibility(videoId, visibilityPercentage)
        onVisibilityChanged(visibilityPercentage)
    }
}