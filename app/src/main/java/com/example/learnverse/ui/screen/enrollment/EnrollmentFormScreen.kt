package com.example.learnverse.ui.screen.enrollment

import android.app.Activity as AndroidActivity  // ✅ Alias Android Activity
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.learnverse.MainActivity
import com.example.learnverse.data.model.Activity as ActivityModel  // ✅ Alias your model
import com.example.learnverse.viewmodel.EnrollmentUiState
import com.example.learnverse.viewmodel.EnrollmentViewModel
import com.example.learnverse.viewmodel.ActivitiesViewModel
import com.razorpay.Checkout
import com.razorpay.PaymentData
import com.razorpay.PaymentResultWithDataListener
import org.json.JSONObject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnrollmentFormScreen(
    activityId: String,
    androidActivity: AndroidActivity,
    navController: NavController,
    enrollmentViewModel: EnrollmentViewModel,
    activitiesViewModel: ActivitiesViewModel
) {

    LaunchedEffect(Unit) {
        (androidActivity as? MainActivity)?.setPaymentCallbacks(
            onSuccess = { paymentId, orderId, signature ->
                enrollmentViewModel.verifyPayment(orderId, paymentId, signature)
            },
            onFailure = { orderId, reason ->
                enrollmentViewModel.handlePaymentFailure(orderId, reason)
            }
        )
    }

    val uiState by enrollmentViewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }

    // ✅ NEW: Fetch activity directly from ViewModel's StateFlow
    val activityDetails by activitiesViewModel.selectedActivity.collectAsStateWithLifecycle()

    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // ✅ NEW: Fetch fresh data from server on screen load
    LaunchedEffect(activityId) {
        try {
            isLoading = true
            activitiesViewModel.fetchActivityById(activityId)  // Fetches from API
            isLoading = false
        } catch (e: Exception) {
            errorMessage = e.message
            isLoading = false
        }
    }

    // Handle UI state changes
    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is EnrollmentUiState.Success -> {
                snackbarHostState.showSnackbar(state.message)
                enrollmentViewModel.resetUiState()
                navController.popBackStack()
            }
            is EnrollmentUiState.Error -> {
                snackbarHostState.showSnackbar(state.message)
                enrollmentViewModel.resetUiState()
            }
            is EnrollmentUiState.OrderCreated -> {
                startRazorpayPayment(
                    androidActivity = androidActivity,
                    orderResponse = state.orderResponse,
                    onSuccess = { paymentId, orderId, signature ->
                        enrollmentViewModel.verifyPayment(orderId, paymentId, signature)
                    },
                    onFailure = { orderId, reason ->
                        enrollmentViewModel.handlePaymentFailure(orderId, reason)
                    }
                )
                enrollmentViewModel.resetUiState()
            }
            else -> {}
        }
    }

    // ✅ Show loading state
    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CircularProgressIndicator()
                Text(
                    "Loading activity details...",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
        return
    }

    // ✅ Show error state
    if (errorMessage != null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    Icons.Default.Error,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.error
                )
                Text(
                    "Failed to load activity",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    errorMessage ?: "Unknown error",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                Button(onClick = { navController.popBackStack() }) {
                    Text("Go Back")
                }
            }
        }
        return
    }

    // ✅ Show "not found" state
    if (activityDetails == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    Icons.Default.SearchOff,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "Activity not found",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "This activity may have been removed or is no longer available",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 32.dp)
                )
                Spacer(Modifier.height(8.dp))
                Button(onClick = { navController.popBackStack() }) {
                    Text("Go Back")
                }
            }
        }
        return
    }

    // ✅ Main content (activity is guaranteed non-null here)
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Enroll in Activity") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Activity Info Card
            ActivityInfoCard(activity = activityDetails!!)

            // Enrollment Form
            Text(
                "Student Information",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            OutlinedTextField(
                value = enrollmentViewModel.studentName,
                onValueChange = { enrollmentViewModel.studentName = it },
                label = { Text("Full Name *") },
                placeholder = { Text("Enter your full name") },
                leadingIcon = { Icon(Icons.Default.Person, null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = enrollmentViewModel.studentEmail,
                onValueChange = { enrollmentViewModel.studentEmail = it },
                label = { Text("Email Address *") },
                placeholder = { Text("your.email@example.com") },
                leadingIcon = { Icon(Icons.Default.Email, null) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true
            )

            OutlinedTextField(
                value = enrollmentViewModel.studentPhone,
                onValueChange = { enrollmentViewModel.studentPhone = it },
                label = { Text("Phone Number *") },
                placeholder = { Text("9876543210") },
                leadingIcon = { Icon(Icons.Default.Phone, null) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true
            )

            OutlinedTextField(
                value = enrollmentViewModel.educationalBackground,
                onValueChange = { enrollmentViewModel.educationalBackground = it },
                label = { Text("Educational Background (Optional)") },
                placeholder = { Text("e.g., B.Tech CSE, Working Professional") },
                leadingIcon = { Icon(Icons.Default.School, null) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )

            OutlinedTextField(
                value = enrollmentViewModel.reasonForEnrollment,
                onValueChange = { enrollmentViewModel.reasonForEnrollment = it },
                label = { Text("Why do you want to enroll? (Optional)") },
                placeholder = { Text("Tell us your learning goals...") },
                leadingIcon = { Icon(Icons.Default.Edit, null) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // Price Summary
            PriceSummaryCard(activity = activityDetails!!)

            // Terms and Conditions
            var agreedToTerms by remember { mutableStateOf(false) }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = agreedToTerms,
                    onCheckedChange = { agreedToTerms = it }
                )
                Text(
                    "I agree to the Terms & Conditions and Refund Policy",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Enroll Button
            Button(
                onClick = {
                    if (activityDetails!!.pricing?.price ?: 0 > 0) {
                        // Paid activity - create order
                        enrollmentViewModel.initiateEnrollment(activityDetails!!.id)
                    } else {
                        // Free activity - direct enrollment
                        enrollmentViewModel.enrollInFreeActivity(activityDetails!!.id)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = uiState !is EnrollmentUiState.Loading &&
                        enrollmentViewModel.studentName.isNotBlank() &&
                        enrollmentViewModel.studentEmail.isNotBlank() &&
                        enrollmentViewModel.studentPhone.isNotBlank() &&
                        agreedToTerms
            ) {
                if (uiState is EnrollmentUiState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(Icons.Default.CheckCircle, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        if (activityDetails!!.pricing?.price ?: 0 > 0)
                            "Proceed to Payment"
                        else
                            "Enroll Now (Free)",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}



@Composable
private fun ActivityInfoCard(activity: ActivityModel) {  // ✅ Use ActivityModel
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                activity.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                "by ${activity.tutorName}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AssistChip(
                    onClick = {},
                    label = { Text(activity.mode) }
                )
                AssistChip(
                    onClick = {},
                    label = { Text(activity.difficulty ?: "All Levels") }
                )
            }
        }
    }
}

@Composable
private fun PriceSummaryCard(activity: ActivityModel) {  // ✅ Use ActivityModel
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Price Summary",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            val basePrice = activity.pricing?.discountPrice ?: activity.pricing?.price ?: 0
            val originalPrice = activity.pricing?.price ?: 0
            val discount = originalPrice - basePrice
            val tax = basePrice * 0.18
            val total = basePrice + tax

            if (discount > 0) {
                PriceRow(
                    label = "Original Price",
                    amount = originalPrice.toDouble(),
                    strikethrough = true
                )
                PriceRow(
                    label = "Discount",
                    amount = -discount.toDouble(),
                    color = MaterialTheme.colorScheme.error
                )
            }

            PriceRow(label = "Course Price", amount = basePrice.toDouble())
            PriceRow(label = "GST (18%)", amount = tax)

            Divider()

            PriceRow(
                label = "Total Amount",
                amount = total,
                bold = true,
                large = true
            )
        }
    }
}

@Composable
private fun PriceRow(
    label: String,
    amount: Double,
    strikethrough: Boolean = false,
    color: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface,
    bold: Boolean = false,
    large: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            style = if (large) MaterialTheme.typography.titleMedium
            else MaterialTheme.typography.bodyMedium,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal
        )
        Text(
            "₹${String.format("%.2f", amount)}",
            style = if (large) MaterialTheme.typography.titleLarge
            else MaterialTheme.typography.bodyMedium,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
            color = color,
            textDecoration = if (strikethrough)
                androidx.compose.ui.text.style.TextDecoration.LineThrough
            else null
        )
    }
}

/**
 * Start Razorpay payment
 */
private fun startRazorpayPayment(
    androidActivity: AndroidActivity,  // ✅ Use renamed parameter
    orderResponse: com.example.learnverse.data.model.OrderResponse,
    onSuccess: (paymentId: String, orderId: String, signature: String) -> Unit,
    onFailure: (orderId: String, reason: String) -> Unit
) {
    try {
        val checkout = Checkout()
        checkout.setKeyID(orderResponse.razorpayKeyId)

        val options = JSONObject()
        options.put("name", "LearnVerse")
        options.put("description", orderResponse.activityTitle)
        options.put("image", "https://your-logo-url.com/logo.png")
        options.put("order_id", orderResponse.razorpayOrderId)
        options.put("currency", orderResponse.currency)
        options.put("amount", (orderResponse.totalAmount * 100).toInt()) // Amount in paise

        val prefill = JSONObject()
        prefill.put("email", orderResponse.userEmail)
        prefill.put("contact", orderResponse.userPhone)
        options.put("prefill", prefill)

        val theme = JSONObject()
        theme.put("color", "#21808d")
        options.put("theme", theme)

        checkout.open(androidActivity, options)  // ✅ Use androidActivity

        // Set payment listener
        (androidActivity as? PaymentResultWithDataListener)?.let {
            // Payment handled in activity
        } ?: run {
            Log.w("Razorpay", "Activity does not implement PaymentResultWithDataListener")
        }

    } catch (e: Exception) {
        Log.e("Razorpay", "Error starting payment", e)
        onFailure(orderResponse.razorpayOrderId, e.message ?: "Payment initialization failed")
    }
}