package com.example.learnverse.data.model


/**
 * Data class for the JSON body when asking a question to the learning assistant.
 * Used for POST /api/learning-assistant/ask.
 */
data class AskRequest(
    val question: String
)

/**
 * A sealed class to represent the two possible outcomes from the /ask endpoint.
 * This makes it easy and safe to handle either a real answer or a "profile needed" message.
 */
sealed class AssistantResponse {
    data class Answer(
        val studentName: String,
        val timestamp: String,
        val question: String,
        val answer: String
    ) : AssistantResponse()

    data class ProfileNeeded(
        val message: String
    ) : AssistantResponse()
}

// These are helper classes for JSON parsing inside the repository. They are not used directly by the UI.
internal data class ChatSuccessResponse(
    val success: Boolean,
    val studentName: String,
    val timestamp: String,
    val question: String,
    val answer: String
)

internal data class ProfileNeededResponse(
    val success: Boolean,
    val needsProfile: Boolean,
    val message: String,
    val profileSetupUrl: String
)
