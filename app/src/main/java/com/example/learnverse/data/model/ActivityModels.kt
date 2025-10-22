package com.example.learnverse.data.model

import com.google.gson.annotations.SerializedName

/**
 * Represents a full Activity object as received from the server.
 * This is used for displaying activities in lists, feeds, and detail screens.
 */
data class Activity(
    val id: String,
    val tutorId: String?, // Added to link back to the tutor
    val tutorName: String,
    val title: String,
    val description: String,
    val subject: String,
    val mode: String,
    val difficulty: String,
    val classType: String?,
    val activityType: String?,
    val tags: List<String>?,
    val isActive: Boolean?,
    val isPublic: Boolean?,
    @SerializedName("duration")
    val durationInfo: DurationInfo?,
    @SerializedName("enrollmentInfo")
    val enrollmentInfo: EnrollmentInfo?,
    val pricing: PricingInfo? // Renamed to avoid confusion with the creation model
)

/**
 * Represents the specific JSON body required by the POST /api/activities/create endpoint.
 * Notice it has its own nested Pricing and Duration classes to perfectly match the request format.
 */
data class CreateActivityRequest(
    val tutorId: String,
    val tutorName: String,
    val title: String,
    val description: String,
    val subject: String,
    val classType: String,
    val activityType: String,
    val mode: String,
    val difficulty: String,
    val pricing: Pricing, // Nested class for creation
    val duration: Duration, // Nested class for creation
    val tags: List<String>,
    val isActive: Boolean,
    val isPublic: Boolean
) {
    // This nested class exactly matches the 'pricing' object in the creation JSON
    data class Pricing(
        val price: Double,
        val currency: String,
        val priceType: String
    )

    // This nested class exactly matches the 'duration' object in the creation JSON
    data class Duration(
        val totalDuration: Int,
        val totalSessions: Int,
        val durationDescription: String
    )
}


// --- HELPER DATA CLASSES FOR DISPLAYING AN ACTIVITY ---

data class DurationInfo(
    val totalDuration: Int?, // Made nullable for safety
    val totalSessions: Int?, // Made nullable for safety
    val durationDescription: String? // Made nullable for safety
)

data class EnrollmentInfo(
    val enrolledCount: Int,
    val maxCapacity: Int? // Made nullable for safety
)

// Renamed to PricingInfo to be distinct from the creation model's Pricing class
data class PricingInfo(
    val price: Double,
    val currency: String,
    val discountPrice: Double?,
    val priceType: String?
)


// --- OTHER MODELS (Unchanged) ---

data class ActivityFilter(
    val subjects: List<String>? = null,
    val activityTypes: List<String>? = null,
    val modes: List<String>? = null,
    val difficulties: List<String>? = null,
    val cities: List<String>? = null,
    val minPrice: Int? = null,
    val maxPrice: Int? = null,
    val demoAvailable: Boolean? = null,
    val sortBy: String? = null,
    val sortDirection: String? = null,
    val searchQuery: String? = null
)

data class NaturalSearchRequest(
    val text: String,
    val userLatitude: Double,
    val userLongitude: Double
)

data class PagedResponse<T>(
    val content: List<T>,
    val pageNumber: Int,
    val totalPages: Int,
    val totalElements: Int
)