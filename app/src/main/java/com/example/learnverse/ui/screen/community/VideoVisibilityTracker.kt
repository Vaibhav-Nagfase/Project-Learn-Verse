package com.example.learnverse.ui.screen.community

import androidx.compose.runtime.*
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import android.util.Log

/**
 * FIXED: Video Visibility Tracker with Center Position
 * Only plays video when it's centered AND visible enough
 */
class VideoVisibilityTracker {
    private val _currentlyPlayingVideoId = mutableStateOf<String?>(null)
    val currentlyPlayingVideoId: State<String?> = _currentlyPlayingVideoId

    private val videoVisibilityMap = mutableStateMapOf<String, Float>()
    private val videoCenterMap = mutableStateMapOf<String, Float>()  // ✅ NEW: Track center positions

    fun shouldPlayVideo(videoId: String): Boolean {
        return _currentlyPlayingVideoId.value == videoId
    }

    fun updateVideoVisibility(videoId: String, visibility: Float, centerY: Float) {
        videoVisibilityMap[videoId] = visibility
        videoCenterMap[videoId] = centerY

        Log.d("VideoTracker", "Video $videoId: ${(visibility * 100).toInt()}% visible, center at ${(centerY * 100).toInt()}%")

        // ✅ Video must be 70%+ visible AND centered in the sweet spot
        val candidates = videoVisibilityMap
            .filter { it.value >= 0.7f }  // ✅ Must be 70%+ visible
            .filter { entry ->
                val center = videoCenterMap[entry.key] ?: 0.5f
                center in 0.35f..0.65f  // ✅ Center must be in middle 30% (sweet spot!)
            }

        val mostVisibleVideo = candidates.maxByOrNull { it.value }
        val winnerId = mostVisibleVideo?.key

        if (winnerId != _currentlyPlayingVideoId.value) {
            Log.d("VideoTracker", "Winner changed: ${_currentlyPlayingVideoId.value} → $winnerId")
            _currentlyPlayingVideoId.value = winnerId
        }
    }

    fun removeVideo(videoId: String) {
        videoVisibilityMap.remove(videoId)
        videoCenterMap.remove(videoId)  // ✅ Clean up center too
    }
}

@Composable
fun rememberVideoVisibilityTracker(): VideoVisibilityTracker {
    return remember { VideoVisibilityTracker() }
}

/**
 * FIXED: Track video with center position
 */
@Composable
fun Modifier.trackVideoVisibility(
    videoId: String,
    visibilityTracker: VideoVisibilityTracker,
    onVisibilityChanged: (Float) -> Unit
): Modifier {
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current

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

        val videoTop = bounds.top
        val videoBottom = bounds.bottom
        val videoHeight = bounds.height

        val screenTop = 0f
        val screenBottom = screenHeightPx

        // Calculate visibility
        val visibleTop = maxOf(videoTop, screenTop)
        val visibleBottom = minOf(videoBottom, screenBottom)
        val visibleHeight = maxOf(0f, visibleBottom - visibleTop)

        val visibilityPercentage = if (videoHeight > 0) {
            (visibleHeight / videoHeight).coerceIn(0f, 1f)
        } else {
            0f
        }

        // ✅ Calculate center position (0.0 = top, 0.5 = center, 1.0 = bottom)
        val videoCenterY = (videoTop + videoBottom) / 2f
        val centerPositionOnScreen = (videoCenterY / screenHeightPx).coerceIn(0f, 1f)

        // Update with both visibility AND center position
        visibilityTracker.updateVideoVisibility(
            videoId,
            visibilityPercentage,
            centerPositionOnScreen  // ✅ Pass center position
        )

        onVisibilityChanged(visibilityPercentage)
    }
}