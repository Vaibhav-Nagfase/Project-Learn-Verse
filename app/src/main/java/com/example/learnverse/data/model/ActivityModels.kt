package com.example.learnverse.data.model

import com.google.gson.annotations.SerializedName
import java.util.Date

/**
 * Represents a full Activity object as received from the server.
 * This matches the backend MongoDB Activity document structure.
 */
data class Activity(
    val id: String,
    val tutorId: String,
    val tutorName: String,
    val title: String,
    val description: String,
    val subject: String,
    val classType: String?,
    val activityType: String?,
    val mode: String,
    val bannerImageUrl: String?,
    val location: Location?,
    val videoContent: VideoContent?,
    val suitableAgeGroup: SuitableAgeGroup?,
    val difficulty: String?,
    val prerequisites: List<String>?,
    val instructorDetails: InstructorDetails?,
    val reviews: Reviews?,
    val pricing: PricingInfo?,
    @SerializedName("duration")
    val duration: DurationInfo?,
    val schedule: Schedule?,
    val enrollmentInfo: EnrollmentInfo?,
    val demoAvailable: Boolean?,
    val demoDetails: DemoDetails?,
    val contactInfo: ContactInfo?,
    val tags: List<String>?,
    val isActive: Boolean?,
    val isPublic: Boolean?,
    val featured: Boolean?,
    val createdAt: Date?,
    val updatedAt: Date?,
    val publishedAt: Date?
) {
    // === Nested Data Classes ===

    data class Location(
        val address: String?,
        val city: String?,
        val state: String?,
        val coordinates: Coordinates?,
        val proximityRadius: Int?,
        val landmark: String?,
        val facilities: List<String>?
    ) {
        data class Coordinates(
            val type: String,
            val coordinates: List<Double>
        )
    }

    data class VideoContent(
        val platform: String?,
        val meetingLink: String?,
        val meetingId: String?,
        val passcode: String?,
        val recordedVideos: List<Video>?,
        val totalVideoCount: Int?,
        val totalVideoDuration: Int?,
        val streamingQuality: List<String>?,
        val downloadAllowed: Boolean?,
        val offlineViewing: Boolean?,
        val subtitlesAvailable: Boolean?,
        val languages: List<String>?
    ) {
        data class Video(
            val videoId: String,
            val title: String,
            val description: String?,
            val duration: Int?,
            val videoUrl: String,
            val thumbnailUrl: String?,
            val order: Int?,
            val isPreview: Boolean?,
            val resources: List<Resource>?
        ) {
            data class Resource(
                val type: String,
                val title: String,
                val url: String
            )
        }
    }

    data class SuitableAgeGroup(
        val minAge: Int?,
        val maxAge: Int?,
        val ageDescription: String?
    )

    data class InstructorDetails(
        val bio: String?,
        val qualifications: List<String>?,
        val experience: String?,
        val specializations: List<String>?,
        val profileImage: String?,
        val socialProof: SocialProof?
    ) {
        data class SocialProof(
            val studentsCount: Int?,
            val totalStudentsTaught: Int?,
            val coursesCount: Int?,
            val yearsTeaching: Int?
        )
    }

    data class Reviews(
        val averageRating: Double?,
        val totalReviews: Int?,
        val ratingDistribution: Map<String, Int>?,
        val recentReviews: List<RecentReview>?
    ) {
        data class RecentReview(
            val studentName: String,
            val rating: Int,
            val comment: String?,
            val date: String
        )
    }

    data class PricingInfo(
        val price: Int?,
        val currency: String?,
        val discountPrice: Int?,
        val priceType: String?,
        val installmentAvailable: Boolean?,
        val freeTrialDays: Int?,
        val moneyBackGuarantee: Int?
    )

    data class DurationInfo(
        val totalDuration: Int?,
        val estimatedDuration: Int?,
        val totalSessions: Int?,
        val durationDescription: String?,
        val lifetimeAccess: Boolean?
    )

    data class Schedule(
        val timingsPerWeek: Int?,
        val sessionDays: List<String>?,
        val sessionTime: String?,
        val timezone: String?,
        val startDate: String?,
        val endDate: String?,
        val flexibleScheduling: Boolean?,
        val selfPaced: Boolean?,
        val accessDuration: Int?,
        val completionDeadline: String?
    )

    data class EnrollmentInfo(
        val enrolledCount: Int?,
        val maxCapacity: Int?,
        val waitlistCount: Int?,
        val enrollmentStatus: String?,
        val autoEnrollment: Boolean?
    )

    data class DemoDetails(
        val demoVideoUrl: String?,
        val demoSessionDate: String?,
        val demoDuration: Int?,
        val freeTrial: Boolean?,
        val trialDuration: Int?
    )

    data class ContactInfo(
        val enrollmentLink: String?,
        val whatsappNumber: String?,
        val email: String?,
        val socialLinks: SocialLinks?,
        val supportHours: String?
    ) {
        data class SocialLinks(
            val youtube: String?,
            val instagram: String?
        )
    }
}

/**
 * Represents the specific JSON body required by the POST /api/activities/create endpoint.
 */
data class CreateActivityRequest(
    val tutorId: String,
    val tutorName: String,
    val title: String,
    val description: String,
    val subject: String,
    val classType: String?,
    val activityType: String,
    val mode: String,
    val difficulty: String,
    val pricing: Pricing,
    val suitableAgeGroup: SuitableAgeGroup?,
    val prerequisites: List<String>?,
    val duration: Duration,
    val schedule: Schedule?,
    val demoAvailable: Boolean?,
    val demoDetails: DemoDetails?,
    val tags: List<String>,
    val isActive: Boolean,
    val isPublic: Boolean,
    val featured: Boolean?
) {
    data class Pricing(
        val price: Int,
        val currency: String,
        val priceType: String,
        val discountPrice: Int?,
        val installmentAvailable: Boolean?
    )

    data class SuitableAgeGroup(
        val minAge: Int,
        val maxAge: Int,
        val ageDescription: String?
    )

    data class Duration(
        val totalSessions: Int,
        val estimatedDuration: Int,
        val durationDescription: String,
        val lifetimeAccess: Boolean?
    )

    data class Schedule(
        val selfPaced: Boolean?,
        val accessDuration: Int?,
        val flexibleScheduling: Boolean?
    )

    data class DemoDetails(
        val freeTrial: Boolean?,
        val trialDuration: Int?
    )
}

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