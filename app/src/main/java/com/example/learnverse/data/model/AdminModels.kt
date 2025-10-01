package com.example.learnverse.data.model

// Represents a single pending verification request in the list
data class PendingVerification(
    val id: String,
    val email: String,
    val fullName: String?,
    val phone: String?,
    val status: String,
    val createdAt: String,
    val documents: Documents
)

// Represents the nested "documents" object
data class Documents(
    val idDocument: DocumentLink?,
    val certificate: DocumentLink?
)

// Represents the "idDocument" or "certificate" object with its links
data class DocumentLink(
    val originalName: String,
    val viewUrl: String,
    val downloadUrl: String
)
