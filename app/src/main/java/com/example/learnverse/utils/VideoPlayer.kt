package com.example.learnverse.utils

import android.annotation.SuppressLint
import android.app.Activity
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
import androidx.compose.ui.window.Dialog
import androidx.core.view.WindowCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

@OptIn(UnstableApi::class)
@RequiresApi(Build.VERSION_CODES.R)
@SuppressLint("WrongConstant")
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

    // Create and remember ExoPlayer
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build()
    }

    // Load video
    LaunchedEffect(videoUrl) {
        val mediaItem = MediaItem.fromUri(Uri.parse(videoUrl))
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
    }

    // Release when composable leaves
    DisposableEffect(Unit) {
        onDispose { exoPlayer.release() }
    }

    // Handle system UI visibility when fullscreen
    LaunchedEffect(isFullscreen) {
        val windowInsetsController = activity?.window?.let {
            WindowCompat.getInsetsController(it, it.decorView)
        }
        windowInsetsController?.isAppearanceLightStatusBars = !isFullscreen
        if (isFullscreen) {
            windowInsetsController?.hide(android.view.WindowInsets.Type.systemBars())
        } else {
            windowInsetsController?.show(android.view.WindowInsets.Type.systemBars())
        }
    }

    // Normal mode
    if (!isFullscreen) {
        Box(modifier = modifier.fillMaxWidth().aspectRatio(16 / 9f)) {
            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        player = exoPlayer
                        useController = true
                        setShowNextButton(false)
                        setShowPreviousButton(false)
                    }
                },
                modifier = Modifier.matchParentSize()
            )

            // Fullscreen toggle button (to the left of settings icon)
            Row(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { isFullscreen = true },
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.3f), shape = MaterialTheme.shapes.small)
                ) {
                    Icon(
                        imageVector = Icons.Default.Fullscreen,
                        contentDescription = "Enter Fullscreen",
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(32.dp).height(16.dp))
            }
        }
    } else {
        // Fullscreen mode
        Dialog(onDismissRequest = { isFullscreen = false }) {
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
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // Exit fullscreen button (top-right)
                IconButton(
                    onClick = { isFullscreen = false },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                        .background(Color.Black.copy(alpha = 0.4f), shape = MaterialTheme.shapes.small)
                ) {
                    Icon(
                        imageVector = Icons.Default.FullscreenExit,
                        contentDescription = "Exit Fullscreen",
                        tint = Color.White
                    )
                }
            }

            // Allow back press to exit fullscreen
            BackHandler(enabled = true) { isFullscreen = false }
        }
    }
}
