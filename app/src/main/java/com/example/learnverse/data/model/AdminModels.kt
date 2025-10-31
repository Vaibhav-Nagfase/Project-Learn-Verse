package com.example.learnverse.data.model

data class PendingVerification(
    val id: String,
    val email: String,
    val fullName: String?,
    val phone: String?,
    val bio: String?,
    val qualifications: List<String>?,
    val experience: String?,
    val specializations: List<String>?,
    val profilePicture: ProfilePictureData?,
    val status: String?,
    val statusDescription: String?,
    val termsAccepted: Boolean?,
    val createdAt: String?,
    val updatedAt: String?,
    val documents: DocumentsData,
    val rejectionReason: String?
)

data class ProfilePictureData(
    val url: String,
    val originalName: String?
)

data class DocumentsData(
    val idDocument: DocumentInfo?,
    val certificate: DocumentInfo?
)

data class DocumentInfo(
    val originalName: String?,
    val url: String,
    val viewUrl: String,
    val downloadUrl: String
)