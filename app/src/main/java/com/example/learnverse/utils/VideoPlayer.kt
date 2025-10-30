package com.example.learnverse.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

@OptIn(UnstableApi::class)
@RequiresApi(Build.VERSION_CODES.R)
@SuppressLint("SourceLockedOrientationActivity")
@Composable
fun VideoPlayer(
    videoUrl: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val view = LocalView.current
    val activity = remember { context as? Activity }

    // Track fullscreen state
    var isFullscreen by remember { mutableStateOf(false) }

    // Remember original orientation
    val originalOrientation = remember { activity?.requestedOrientation }

    // Create and remember ExoPlayer
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            playWhenReady = false // Don't auto-play
        }
    }

    // Load video
    LaunchedEffect(videoUrl) {
        val mediaItem = MediaItem.fromUri(Uri.parse(videoUrl))
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
    }

    // Handle fullscreen mode changes
    LaunchedEffect(isFullscreen) {
        activity?.let { act ->
            if (isFullscreen) {
                // ✅ Force landscape and lock it
                act.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE

                val windowInsetsController = WindowCompat.getInsetsController(act.window, view)
                windowInsetsController.apply {
                    hide(WindowInsetsCompat.Type.systemBars())
                    systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                }
            } else {
                // ✅ Return to portrait
                act.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

                val windowInsetsController = WindowCompat.getInsetsController(act.window, view)
                windowInsetsController.show(WindowInsetsCompat.Type.systemBars())
            }
        }
    }


    // Release when composable leaves
    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
            // Restore orientation on dispose
            activity?.let { act ->
                originalOrientation?.let { act.requestedOrientation = it }
            }
        }
    }

    // ✅ Normal mode (portrait)
    if (!isFullscreen) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .aspectRatio(16 / 9f)
        ) {
            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        player = exoPlayer
                        useController = true
                        setShowNextButton(false)
                        setShowPreviousButton(false)
                        // ✅ Removed setShowFullscreenButton - doesn't exist
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            // ✅ Custom fullscreen button overlay (bottom-right corner)
            IconButton(
                onClick = { isFullscreen = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 60.dp, bottom = 8.dp)  // Positioned LEFT of settings
                    .size(40.dp)
                    .background(
                        Color.Black.copy(alpha = 0.5f),
                        shape = MaterialTheme.shapes.small
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Fullscreen,
                    contentDescription = "Enter Fullscreen",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    } else {
        // ✅ Fullscreen mode (landscape)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        player = exoPlayer
                        useController = true
                        setShowNextButton(false)
                        setShowPreviousButton(false)
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            // ✅ Exit fullscreen button (top-right)
            IconButton(
                onClick = { isFullscreen = false },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .size(40.dp)
                    .background(
                        Color.Black.copy(alpha = 0.5f),
                        shape = MaterialTheme.shapes.small
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.FullscreenExit,
                    contentDescription = "Exit Fullscreen",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // ✅ Allow back press to exit fullscreen
        BackHandler(enabled = true) {
            isFullscreen = false
        }
    }
}