package com.example.learnverse.ui.screen.chatbot

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.ui.graphics.*
import kotlinx.coroutines.delay
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.learnverse.viewmodel.ChatMessage
import com.example.learnverse.viewmodel.ChatViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale



// Main Screen Composable
@Composable
fun ChatScreen(
    navController: NavController,
    chatViewModel: ChatViewModel
) {
    val messages by chatViewModel.messages.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Automatic scroll to new messages
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            coroutineScope.launch {
                listState.animateScrollToItem(index = messages.size - 1)
            }
        }
    }

    // Using a Column to structure the screen as per the reference code
    Box(modifier = Modifier
        .fillMaxSize()
        .background(Color(0xFFF0E7FE))
        .navigationBarsPadding() // Pushes content above the navigation bar
        .imePadding() // Pushes content above the keyboard when it's open
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // 1. Header

            Spacer(modifier = Modifier.height(32.dp))
            ChatHeader()
            Spacer(modifier = Modifier.height(16.dp))

            // 2. Messages List (takes up the remaining space)
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                items(messages) { message ->
                    MessageBubble(message = message)
                }
            }

            // 3. Input Bar at the bottom
            ChatInputBar(onSend = { text ->
                chatViewModel.sendMessage(text)
            })
        }
    }
}

// Styled Header Composable
@Composable
fun ChatHeader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .background(
                color = Color(0xFFA88FDC),
                shape = RoundedCornerShape(
                    bottomStart = 32.dp,
                    bottomEnd = 32.dp
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Ask me anything 💬",
            fontSize = 24.sp,
            color = Color.Black,
            style = MaterialTheme.typography.headlineSmall
        )
    }
}

// Styled Input Bar Composable
@Composable
fun ChatInputBar(onSend: (String) -> Unit) {
    var textState by remember { mutableStateOf(TextFieldValue("")) }

    Row(
        modifier = Modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Card wrapper for the text field, giving it elevation and shape
        Card(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            BasicTextField(
                value = textState,
                onValueChange = { textState = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                textStyle = TextStyle(fontSize = 16.sp, color = Color.Black),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                decorationBox = { innerTextField ->
                    if (textState.text.isEmpty()) {
                        Text("Type a message...", color = Color.Gray)
                    }
                    innerTextField()
                }
            )
        }
        Spacer(modifier = Modifier.width(8.dp))

        // Send Button
        IconButton(
            onClick = {
                if (textState.text.isNotBlank()) {
                    onSend(textState.text)
                    textState = TextFieldValue("") // Clear input
                }
            },
            enabled = textState.text.isNotBlank(),
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape),
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = Color(0xFF9673E1),
                contentColor = Color.White,
                disabledContainerColor = Color(0xFF252222)
            )
        ) {
            Icon(Icons.Default.Send, contentDescription = "Send Message", modifier = Modifier.size(28.dp))
        }
    }
}

// Styled Message Bubble Composable
@Composable
fun MessageBubble(message: ChatMessage) {
    val isUser = message.isFromUser
    val alignment = if (isUser) Alignment.End else Alignment.Start
    val backgroundColor = if (isUser) Color(0xFF673AB7) else Color.White
    val textColor = if (isUser) Color.White else Color.Black
    val authorName = if (isUser) "You" else "Assistant"
    val bubbleShape = if (isUser) {
        RoundedCornerShape(20.dp, 4.dp, 20.dp, 20.dp)
    } else {
        RoundedCornerShape(4.dp, 20.dp, 20.dp, 20.dp)
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        Card(
            shape = bubbleShape,
            colors = CardDefaults.cardColors(containerColor = backgroundColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                if (message.isTyping) {
                    TypingIndicator(dotColor = textColor)
                } else {
                    Text(text = message.text, style = MaterialTheme.typography.bodyLarge, color = textColor)
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "$authorName • ${getCurrentTime()}",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
    }
}

@Composable
fun TypingIndicator(dotColor: Color) {
    val dots = listOf(remember { Animatable(0f) }, remember { Animatable(0f) }, remember { Animatable(0f) })

    dots.forEachIndexed { index, animatable ->
        LaunchedEffect(animatable) {
            delay(index * 200L) // Stagger the animation start
            animatable.animateTo(
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = keyframes {
                        durationMillis = 1200
                        0f at 0
                        1f at 300
                        0f at 600
                        0f at 1200
                    },
                    repeatMode = RepeatMode.Restart
                )
            )
        }
    }

    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        dots.forEach { animatable ->
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .graphicsLayer { alpha = animatable.value }
                    .clip(CircleShape)
                    .background(dotColor)
            )
        }
    }
}

// Helper function to get a formatted time string
private fun getCurrentTime(): String {
    return SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
}

