package com.example.learnverse.data.model

import java.util.Date

data class OrderResponse(
    val orderId: String,
    val razorpayOrderId: String,
    val razorpayKeyId: String,
    val activityTitle: String,
    val amount: Double,
    val discountAmount: Double,
    val taxAmount: Double,
    val totalAmount: Double,
    val currency: String,
    val userName: String,
    val userEmail: String,
    val userPhone: String,
    val callback_url: String?
)

data class PaymentVerificationRequest(
    val razorpayOrderId: String,
    val razorpayPaymentId: String,
    val razorpaySignature: String
)

data class PaymentVerificationResponse(
    val success: Boolean,
    val message: String,
    val orderId: String?
)

data class Order(
    val id: String,
    val orderId: String,
    val razorpayOrderId: String,
    val razorpayPaymentId: String?,
    val razorpaySignature: String?,

    // User & Activity details
    val userId: String,
    val userName: String,
    val userEmail: String,
    val userPhone: String,
    val activityId: String,
    val activityTitle: String,
    val tutorId: String,

    // Student enrollment details
    val educationalBackground: String?,
    val reasonForEnrollment: String?,

    // Payment details
    val amount: Double,
    val discountAmount: Double,
    val taxAmount: Double,
    val totalAmount: Double,
    val currency: String,

    // Status tracking
    val status: OrderStatus,
    val paymentMethod: String?,

    // Timestamps
    val createdAt: Date,
    val paidAt: Date?,
    val failedAt: Date?,
    val refundedAt: Date?,

    // Additional info
    val failureReason: String?,
    val refundReason: String?
) {
    enum class OrderStatus {
        PENDING,
        PROCESSING,
        COMPLETED,
        FAILED,
        REFUNDED,
        CANCELLED
    }
}
