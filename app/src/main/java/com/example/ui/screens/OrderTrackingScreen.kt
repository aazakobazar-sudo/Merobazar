package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Order
import com.example.ui.MainViewModel
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.JadeGreen
import com.example.ui.theme.NepalBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderTrackingScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val isNepali by viewModel.isNepali.collectAsState()
    val orders by viewModel.orders.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    var showRatingDialogForShop by remember { mutableStateOf<String?>(null) }
    var ratingStars by remember { mutableIntStateOf(5) }
    var reviewFeedback by remember { mutableStateOf("") }

    if (showRatingDialogForShop != null) {
        val shopId = showRatingDialogForShop!!
        AlertDialog(
            onDismissRequest = { showRatingDialogForShop = null },
            title = {
                Text(
                    text = if (isNepali) "⭐ पसल तथा डेलिभरी मूल्यांकन" else "Rate Shop & Delivery",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(if (isNepali) "तारा छनोट गर्नुहोस्:" else "Select stars:")
                    Row {
                        (1..5).forEach { star ->
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "$star stars",
                                tint = if (star <= ratingStars) AmberGold else Color.LightGray,
                                modifier = Modifier
                                    .size(32.dp)
                                    .clickable { ratingStars = star }
                                    .padding(2.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = reviewFeedback,
                        onValueChange = { reviewFeedback = it },
                        placeholder = { Text(if (isNepali) "सामान र डेलिभरी कस्तो लाग्यो लेख्नुहोस्..." else "Write your experience...") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.addReview(shopId, ratingStars, reviewFeedback.ifBlank { "उत्कृष्ट सेवा र छिटो डेलिभरी!" })
                        showRatingDialogForShop = null
                        reviewFeedback = ""
                        Toast.makeText(context, if (isNepali) "धन्यवाद! रेटिङ सुरक्षित भयो।" else "Rating submitted!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed)
                ) {
                    Text(if (isNepali) "रेटिङ पठाउनुहोस्" else "Submit Rating")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRatingDialogForShop = null }) {
                    Text(if (isNepali) "रद्द" else "Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isNepali) "📦 अर्डर ट्र्याकिङ टाइमलाइन" else "📦 Live Order Tracking", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (orders.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.LocalShipping, contentDescription = null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(60.dp))
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = if (isNepali) "कुनै अर्डर गरिएको छैन" else "No orders yet",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(orders, key = { it.id }) { order ->
                    InteractiveOrderCard(
                        order = order,
                        isNepali = isNepali,
                        isMerchantOrAdmin = currentUser?.role in listOf("m", "a"),
                        onMarkReceived = {
                            viewModel.markOrderReceived(order.id)
                            Toast.makeText(context, if (isNepali) "सामान प्राप्त भएको प्रमाणित भयो! क्यासब्याक पोइन्ट थपियो।" else "Order marked received! Points awarded.", Toast.LENGTH_SHORT).show()
                        },
                        onRateShop = {
                            showRatingDialogForShop = order.muid.ifBlank { "shop_1" }
                        },
                        onUpdateStatus = { nextStatus ->
                            viewModel.updateOrderStatus(order.id, nextStatus)
                            Toast.makeText(context, "स्थिति अद्यावधिक: $nextStatus", Toast.LENGTH_SHORT).show()
                        },
                        onCancel = {
                            viewModel.cancelOrder(order.id)
                            Toast.makeText(context, if (isNepali) "अर्डर रद्द भयो" else "Order cancelled", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun InteractiveOrderCard(
    order: Order,
    isNepali: Boolean,
    isMerchantOrAdmin: Boolean,
    onMarkReceived: () -> Unit,
    onRateShop: () -> Unit,
    onUpdateStatus: (String) -> Unit,
    onCancel: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("track_card_${order.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: ID and Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "#${order.id}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                val badgeColor = when (order.st) {
                    "Delivered" -> JadeGreen
                    "Shipped" -> NepalBlue
                    "Packed" -> AmberGold
                    "Processing", "Accepted" -> NepalBlue
                    "Cancelled" -> CrimsonRed
                    else -> AmberGold
                }
                Badge(containerColor = badgeColor) {
                    Text(
                        text = when (order.st) {
                            "Pending" -> if (isNepali) "अर्डर गरियो" else "Order Placed"
                            "Processing", "Accepted" -> if (isNepali) "पसलले स्वीकार गर्‍यो" else "Accepted by Shop"
                            "Packed" -> if (isNepali) "प्याक भयो" else "Packed"
                            "Shipped" -> if (isNepali) "डेलिभरीमा" else "Out for Delivery"
                            "Delivered" -> if (isNepali) "डेलिभर भयो" else "Delivered"
                            "Cancelled" -> if (isNepali) "रद्द भयो" else "Cancelled"
                            else -> order.st
                        },
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(text = "🏪 पसल: ${order.shopName}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            Text(text = "📅 मिति: ${order.d}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = "📍 डेलिभरी ठेगाना: ${order.caddress}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (order.rcv.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = JadeGreen.copy(alpha = 0.15f),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Text(
                        text = "✅ ग्राहकले पायो: ${order.rcv}",
                        color = JadeGreen,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

            // Order items list
            order.items.forEach { item ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "${item.n} × ${item.q}", style = MaterialTheme.typography.bodyMedium)
                    Text(text = "₨ ${item.p * item.q}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = if (isNepali) "कुल जम्मा रकम:" else "Total Amount:", fontWeight = FontWeight.Bold)
                Text(text = "₨ ${order.total}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = CrimsonRed)
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Real 5-step timeline from ext.js: Pending -> Processing -> Packed -> Shipped -> Delivered
            if (order.st == "Cancelled") {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = CrimsonRed.copy(alpha = 0.12f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Cancel, contentDescription = null, tint = CrimsonRed)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isNepali) "यो अर्डर रद्द गरिएको छ।" else "This order was cancelled.", color = CrimsonRed, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                    }
                }
            } else {
                RealOrderTimeline(currentStatus = order.st, isNepali = isNepali)
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (order.st in listOf("Processing", "Accepted", "Packed", "Shipped", "Delivered") && order.rcv.isBlank()) {
                    Button(
                        onClick = onMarkReceived,
                        colors = ButtonDefaults.buttonColors(containerColor = JadeGreen),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f).testTag("mark_received_btn_${order.id}")
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isNepali) "ग्राहकले पायो ✓" else "Received ✓", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                if (order.st == "Delivered") {
                    Button(
                        onClick = onRateShop,
                        colors = ButtonDefaults.buttonColors(containerColor = AmberGold),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f).testTag("rate_shop_btn_${order.id}")
                    ) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isNepali) "रेटिङ दिनुहोस्" else "Rate", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                if (order.st == "Pending") {
                    OutlinedButton(
                        onClick = onCancel,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Cancel, contentDescription = null, tint = CrimsonRed, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isNepali) "रद्द गर्नुहोस्" else "Cancel", color = CrimsonRed, fontSize = 12.sp)
                    }
                }
            }

            // Merchant / Admin State Advance Controls
            if (isMerchantOrAdmin && order.st != "Cancelled" && order.st != "Delivered") {
                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (isNepali) "⚙️ पसल / एडमिन स्थिति अद्यावधिक:" else "Advance Order Status:",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val nextStates = listOf(
                        "Processing" to if (isNepali) "स्वीकार" else "Accept",
                        "Packed" to if (isNepali) "प्याक" else "Pack",
                        "Shipped" to if (isNepali) "डेलिभरी" else "Ship",
                        "Delivered" to if (isNepali) "डेलिभर" else "Deliver"
                    )
                    nextStates.forEach { (stKey, stLabel) ->
                        OutlinedButton(
                            onClick = { onUpdateStatus(stKey) },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                        ) {
                            Text(stLabel, fontSize = 11.sp, maxLines = 1)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RealOrderTimeline(currentStatus: String, isNepali: Boolean) {
    val steps = listOf("Pending", "Processing", "Packed", "Shipped", "Delivered")
    val currentStepIndex = when (currentStatus) {
        "Delivered" -> 4
        "Shipped" -> 3
        "Packed" -> 2
        "Processing", "Accepted" -> 1
        else -> 0
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        steps.forEachIndexed { index, stepName ->
            val isDone = index <= currentStepIndex
            val stepLabel = when (stepName) {
                "Pending" -> if (isNepali) "अर्डर" else "Order"
                "Processing" -> if (isNepali) "स्वीकृत" else "Accept"
                "Packed" -> if (isNepali) "प्याक" else "Pack"
                "Shipped" -> if (isNepali) "डेलिभरी" else "Ship"
                else -> if (isNepali) "डेलिभर" else "Done"
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(if (isDone) JadeGreen else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (isDone) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                    } else {
                        Text("${index + 1}", fontSize = 10.sp, color = Color.White)
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = stepLabel,
                    fontSize = 9.sp,
                    fontWeight = if (isDone) FontWeight.Bold else FontWeight.Normal,
                    color = if (isDone) JadeGreen else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (index < steps.size - 1) {
                Box(
                    modifier = Modifier
                        .height(2.dp)
                        .weight(1f)
                        .padding(horizontal = 2.dp)
                        .background(if (index < currentStepIndex) JadeGreen else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                )
            }
        }
    }
}
