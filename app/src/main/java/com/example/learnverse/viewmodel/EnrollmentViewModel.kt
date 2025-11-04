package com.example.learnverse.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.learnverse.data.model.*
import com.example.learnverse.data.repository.EnrollmentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class EnrollmentUiState {
    object Idle : EnrollmentUiState()
    object Loading : EnrollmentUiState()
    data class Success(val message: String) : EnrollmentUiState()
    data class Error(val message: String) : EnrollmentUiState()
    data class OrderCreated(val orderResponse: OrderResponse) : EnrollmentUiState()
}

class EnrollmentViewModel(
    private val repository: EnrollmentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<EnrollmentUiState>(EnrollmentUiState.Idle)
    val uiState: StateFlow<EnrollmentUiState> = _uiState.asStateFlow()

    private val _myEnrollments = MutableStateFlow<List<Enrollment>>(emptyList())
    val myEnrollments: StateFlow<List<Enrollment>> = _myEnrollments.asStateFlow()

    private val _isEnrolled = MutableStateFlow(false)
    val isEnrolled: StateFlow<Boolean> = _isEnrolled.asStateFlow()

    // Form fields
    var studentName by mutableStateOf("")
    var studentEmail by mutableStateOf("")
    var studentPhone by mutableStateOf("")
    var educationalBackground by mutableStateOf("")
    var reasonForEnrollment by mutableStateOf("")

    fun resetForm() {
        studentName = ""
        studentEmail = ""
        studentPhone = ""
        educationalBackground = ""
        reasonForEnrollment = ""
    }

    fun resetUiState() {
        _uiState.value = EnrollmentUiState.Idle
    }

    /**
     * Initiate enrollment - creates order for paid activity
     */
    fun initiateEnrollment(activityId: String) {
        viewModelScope.launch {
            try {
                _uiState.value = EnrollmentUiState.Loading

                val request = EnrollmentRequest(
                    activityId = activityId,
                    studentName = studentName,
                    studentEmail = studentEmail,
                    studentPhone = studentPhone,
                    educationalBackground = educationalBackground.takeIf { it.isNotBlank() },
                    reasonForEnrollment = reasonForEnrollment.takeIf { it.isNotBlank() }
                )

                val orderResponse = repository.initiateEnrollment(request)
                _uiState.value = EnrollmentUiState.OrderCreated(orderResponse)

            } catch (e: Exception) {
                _uiState.value = EnrollmentUiState.Error(
                    e.message ?: "Failed to initiate enrollment"
                )
            }
        }
    }

    /**
     * Complete enrollment after successful payment
     */
    fun completeEnrollment(orderId: String) {
        viewModelScope.launch {
            try {
                _uiState.value = EnrollmentUiState.Loading

                val enrollment = repository.completeEnrollment(orderId)

                _uiState.value = EnrollmentUiState.Success(
                    "Successfully enrolled in ${enrollment.activityTitle}!"
                )

                // Refresh enrollments
                fetchMyEnrollments()

            } catch (e: Exception) {
                _uiState.value = EnrollmentUiState.Error(
                    e.message ?: "Failed to complete enrollment"
                )
            }
        }
    }

    /**
     * Enroll in free activity
     */
    fun enrollInFreeActivity(activityId: String) {
        viewModelScope.launch {
            try {
                _uiState.value = EnrollmentUiState.Loading

                val request = EnrollmentRequest(
                    activityId = activityId,
                    studentName = studentName,
                    studentEmail = studentEmail,
                    studentPhone = studentPhone,
                    educationalBackground = educationalBackground.takeIf { it.isNotBlank() },
                    reasonForEnrollment = reasonForEnrollment.takeIf { it.isNotBlank() }
                )

                val enrollment = repository.enrollInFreeActivity(request)

                _uiState.value = EnrollmentUiState.Success(
                    "Successfully enrolled in ${enrollment.activityTitle}!"
                )

                resetForm()
                fetchMyEnrollments()

            } catch (e: Exception) {
                _uiState.value = EnrollmentUiState.Error(
                    e.message ?: "Failed to enroll in free activity"
                )
            }
        }
    }

    /**
     * Verify payment after Razorpay success
     */
    fun verifyPayment(
        razorpayOrderId: String,
        razorpayPaymentId: String,
        razorpaySignature: String
    ) {
        viewModelScope.launch {
            try {
                _uiState.value = EnrollmentUiState.Loading

                val request = PaymentVerificationRequest(
                    razorpayOrderId = razorpayOrderId,
                    razorpayPaymentId = razorpayPaymentId,
                    razorpaySignature = razorpaySignature
                )

                val response = repository.verifyPayment(request)

                if (response.success && response.orderId != null) {
                    // Complete enrollment
                    completeEnrollment(response.orderId)
                } else {
                    _uiState.value = EnrollmentUiState.Error(response.message)
                }

            } catch (e: Exception) {
                _uiState.value = EnrollmentUiState.Error(
                    e.message ?: "Payment verification failed"
                )
            }
        }
    }

    /**
     * Handle payment failure
     */
    fun handlePaymentFailure(razorpayOrderId: String, reason: String) {
        viewModelScope.launch {
            try {
                repository.handlePaymentFailure(razorpayOrderId, reason)
                _uiState.value = EnrollmentUiState.Error("Payment failed: $reason")
            } catch (e: Exception) {
                _uiState.value = EnrollmentUiState.Error("Payment failed")
            }
        }
    }

    /**
     * Fetch user's enrollments
     */
    fun fetchMyEnrollments() {
        viewModelScope.launch {
            try {
                val enrollments = repository.getMyEnrollments()
                _myEnrollments.value = enrollments
            } catch (e: Exception) {
                // Handle error silently
            }
        }
    }

    /**
     * Check if user is already enrolled
     */
    fun checkEnrollment(activityId: String) {
        viewModelScope.launch {
            try {
                val enrolled = repository.checkEnrollment(activityId)
                _isEnrolled.value = enrolled
            } catch (e: Exception) {
                _isEnrolled.value = false
            }
        }
    }
}