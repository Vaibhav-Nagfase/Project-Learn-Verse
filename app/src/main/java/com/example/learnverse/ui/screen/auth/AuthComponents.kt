package com.example.learnverse.ui.screen.auth

import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle

@Composable
fun ClickableLoginText(
    onSignUpClicked: () -> Unit
) {
    val annotatedString = buildAnnotatedString {
        append("Don't have an account? ")
        pushStringAnnotation(tag = "SignUp", annotation = "SignUp")
        withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary)) {
            append("Sign up")
        }
        pop()
    }

    ClickableText(
        text = annotatedString,
        onClick = { offset ->
            annotatedString.getStringAnnotations(tag = "SignUp", start = offset, end = offset)
                .firstOrNull()?.let {
                    onSignUpClicked()
                }
        }
    )
}

@Composable
fun ClickableSignUpText(
    onSignInClicked: () -> Unit
) {
    val annotatedString = buildAnnotatedString {
        append("Already have an account? ")
        pushStringAnnotation(tag = "SignIn", annotation = "SignIn")
        withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary)) {
            append("Sign in")
        }
        pop()
    }

    ClickableText(
        text = annotatedString,
        onClick = { offset ->
            annotatedString.getStringAnnotations(tag = "SignIn", start = offset, end = offset)
                .firstOrNull()?.let {
                    onSignInClicked()
                }
        }
    )
}