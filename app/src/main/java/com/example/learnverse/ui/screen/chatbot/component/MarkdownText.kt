package com.example.learnverse.ui.screen.chatbot.component

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MarkdownText(
    text: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    // Normalize line breaks from streaming
    val normalizedText = text
        .replace("\r\n", "\n")
        .replace("\r", "\n")
        .replace(":★", ":\n") // Handle your specific case
        .replace("★", "\n")

    Column(modifier = modifier) {
        parseMarkdown(normalizedText).forEach { element ->
            when (element) {
                is MarkdownElement.Heading -> {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = element.text,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        ),
                        color = color
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                is MarkdownElement.BulletPoint -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 12.dp, top = 4.dp, bottom = 4.dp, end = 8.dp)
                    ) {
                        Text(
                            text = "• ",
                            style = MaterialTheme.typography.bodyMedium,
                            color = color,
                            modifier = Modifier.padding(end = 4.dp)
                        )
                        Text(
                            text = buildAnnotatedString {
                                appendInlineFormatting(element.text, color)
                            },
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Normal,
                                fontSize = 15.sp
                            ),
                            color = color,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                is MarkdownElement.Paragraph -> {
                    if (element.text.isNotBlank()) {
                        Text(
                            text = buildAnnotatedString {
                                appendInlineFormatting(element.text, color)
                            },
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Normal,
                                fontSize = 15.sp
                            ),
                            color = color,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                        )
                    }
                }
            }
        }
    }
}

sealed class MarkdownElement {
    data class Heading(val text: String) : MarkdownElement()
    data class BulletPoint(val text: String) : MarkdownElement()
    data class Paragraph(val text: String) : MarkdownElement()
}

private fun parseMarkdown(text: String): List<MarkdownElement> {
    val lines = text.split("\n")
    val elements = mutableListOf<MarkdownElement>()

    for (line in lines) {
        val trimmedLine = line.trim()

        when {
            // Skip empty lines
            trimmedLine.isEmpty() -> continue

            // Heading: **Text:** or **Text**:
            trimmedLine.matches(Regex("""\*\*[^*]+\*\*:?""")) -> {
                val headingText = trimmedLine
                    .removePrefix("**")
                    .removeSuffix("**")
                    .removeSuffix(":")
                    .trim()
                elements.add(MarkdownElement.Heading(headingText))
            }

            // Bullet point: starts with * or -
            trimmedLine.startsWith("* ") || trimmedLine.startsWith("- ") ||
                    trimmedLine.startsWith("•") -> {
                val bulletText = trimmedLine
                    .removePrefix("* ")
                    .removePrefix("- ")
                    .removePrefix("• ")
                    .trim()
                if (bulletText.isNotBlank()) {
                    elements.add(MarkdownElement.BulletPoint(bulletText))
                }
            }

            // Regular paragraph
            else -> {
                elements.add(MarkdownElement.Paragraph(trimmedLine))
            }
        }
    }

    return elements
}

private fun AnnotatedString.Builder.appendInlineFormatting(text: String, baseColor: Color) {
    var currentIndex = 0

    // Find **bold text** patterns
    val boldPattern = Regex("""\*\*([^*]+?)\*\*""")

    val matches = boldPattern.findAll(text).toList()

    if (matches.isEmpty()) {
        // No bold formatting, append as normal text
        withStyle(style = SpanStyle(fontWeight = FontWeight.Normal, color = baseColor)) {
            append(text)
        }
        return
    }

    matches.forEach { matchResult ->
        // Append normal text before bold
        if (matchResult.range.first > currentIndex) {
            withStyle(style = SpanStyle(fontWeight = FontWeight.Normal, color = baseColor)) {
                append(text.substring(currentIndex, matchResult.range.first))
            }
        }

        // Append bold text
        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = baseColor)) {
            append(matchResult.groupValues[1])
        }

        currentIndex = matchResult.range.last + 1
    }

    // Append remaining normal text
    if (currentIndex < text.length) {
        withStyle(style = SpanStyle(fontWeight = FontWeight.Normal, color = baseColor)) {
            append(text.substring(currentIndex))
        }
    }
}
