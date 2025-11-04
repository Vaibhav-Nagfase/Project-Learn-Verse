package com.example.learnverse.data.repository

import com.example.learnverse.data.model.*
import com.example.learnverse.data.remote.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class EnrollmentRepository(private val apiService: ApiService) {

    suspend fun initiateEnrollment(request: EnrollmentRequest): OrderResponse {
        return withContext(Dispatchers.IO) {
            val response = apiService.initiateEnrollment(request)
            if (response.isSuccessful && response.body() != null) {
                response.body()!!
            } else {
                throw Exception("Failed to initiate enrollment: ${response.message()}")
            }
        }
    }

    suspend fun completeEnrollment(orderId: String): Enrollment {
        return withContext(Dispatchers.IO) {
            val response = apiService.completeEnrollment(mapOf("orderId" to orderId))
            if (response.isSuccessful && response.body() != null) {
                response.body()!!
            } else {
                throw Exception("Failed to complete enrollment: ${response.message()}")
            }
        }
    }

    suspend fun enrollInFreeActivity(request: EnrollmentRequest): Enrollment {
        return withContext(Dispatchers.IO) {
            val response = apiService.enrollInFreeActivity(request)
            if (response.isSuccessful && response.body() != null) {
                response.body()!!
            } else {
                throw Exception("Failed to enroll in free activity: ${response.message()}")
            }
        }
    }

    suspend fun getMyEnrollments(): List<Enrollment> {
        return withContext(Dispatchers.IO) {
            val response = apiService.getMyEnrollments()
            if (response.isSuccessful && response.body() != null) {
                response.body()!!
            } else {
                emptyList()
            }
        }
    }

    suspend fun checkEnrollment(activityId: String): Boolean {
        return withContext(Dispatchers.IO) {
            val response = apiService.checkEnrollment(activityId)
            response.body()?.get("isEnrolled") ?: false
        }
    }

    suspend fun verifyPayment(request: PaymentVerificationRequest): PaymentVerificationResponse {
        return withContext(Dispatchers.IO) {
            val response = apiService.verifyPayment(request)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                PaymentVerificationResponse(
                    success = body["success"] as? Boolean ?: false,
                    message = body["message"] as? String ?: "Verification failed",
                    orderId = body["orderId"] as? String
                )
            } else {
                throw Exception("Payment verification failed: ${response.message()}")
            }
        }
    }

    suspend fun handlePaymentFailure(razorpayOrderId: String, reason: String) {
        withContext(Dispatchers.IO) {
            apiService.handlePaymentFailure(
                mapOf(
                    "razorpayOrderId" to razorpayOrderId,
                    "reason" to reason
                )
            )
        }
    }
}
