package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.example.util.GoogleDocsHelper
import com.example.util.NepalCalendarHelper
import com.example.ui.MainViewModel
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.JadeGreen
import com.example.ui.theme.NepalBlue
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PastOrdersScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit,
    onNavigateToCart: () -> Unit = {},
    onBrowseProducts: () -> Unit = {}
) {
    val isNepali by viewModel.isNepali.collectAsState()
    val orders by viewModel.orders.collectAsState()

    var selectedStatusFilter by remember { mutableStateOf("ALL") }
    var orderToCancel by remember { mutableStateOf<Order?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Filter orders based on status tab
    val filteredOrders = remember(orders, selectedStatusFilter) {
        when (selectedStatusFilter) {
            "Pending" -> orders.filter { it.st.equals("Pending", ignoreCase = true) }
            "Delivered" -> orders.filter { it.st.equals("Delivered", ignoreCase = true) }
            "Cancelled" -> orders.filter { it.st.equals("Cancelled", ignoreCase = true) }
            "Accepted" -> orders.filter { it.st.equals("Accepted", ignoreCase = true) }
            else -> orders
        }
    }

    // Confirmation dialog for cancelling order
    if (orderToCancel != null) {
        val targetOrder = orderToCancel!!
        AlertDialog(
            onDismissRequest = { orderToCancel = null },
            icon = {
                Icon(
                    imageVector = Icons.Default.Cancel,
                    contentDescription = null,
                    tint = CrimsonRed,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = if (isNepali) "अर्डर रद्द गर्ने?" else "Cancel Order?",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = if (isNepali)
                        "के तपाईं अर्डर #${targetOrder.id} साँच्चिकै रद्द गर्न चाहनुहुन्छ? यो प्रक्रिया उल्टाउन सकिँदैन।"
                    else
                        "Are you sure you want to cancel order #${targetOrder.id}? This action cannot be reversed."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.cancelOrder(targetOrder.id)
                        orderToCancel = null
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                if (isNepali) "अर्डर रद्द गरियो।" else "Order was successfully cancelled."
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed)
                ) {
                    Text(if (isNepali) "हो, रद्द गर्नुहोस्" else "Yes, Cancel Order")
                }
            },
            dismissButton = {
                TextButton(onClick = { orderToCancel = null }) {
                    Text(if (isNepali) "फर्कनुहोस्" else "Keep Order")
                }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (isNepali) "📦 अर्डर इतिहास (Past Orders)" else "📦 Past Orders",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${orders.size} " + if (isNepali) "वटा कुल अर्डर" else "total orders",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("past_orders_back_btn")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Status Tabs (All, Pending, Delivered, Cancelled)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val statusTabs = listOf(
                    "ALL" to if (isNepali) "सबै (${orders.size})" else "All (${orders.size})",
                    "Pending" to if (isNepali) "पर्खाइमा (${orders.count { it.st == "Pending" }})" else "Pending (${orders.count { it.st == "Pending" }})",
                    "Delivered" to if (isNepali) "डेलिभर (${orders.count { it.st == "Delivered" }})" else "Delivered (${orders.count { it.st == "Delivered" }})",
                    "Cancelled" to if (isNepali) "रद्द भएको (${orders.count { it.st == "Cancelled" }})" else "Cancelled (${orders.count { it.st == "Cancelled" }})"
                )

                statusTabs.forEach { (statusKey, label) ->
                    val isSelected = selectedStatusFilter == statusKey
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedStatusFilter = statusKey },
                        label = {
                            Text(
                                text = label,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = when (statusKey) {
                                "Delivered" -> JadeGreen.copy(alpha = 0.18f)
                                "Pending" -> AmberGold.copy(alpha = 0.2f)
                                "Cancelled" -> CrimsonRed.copy(alpha = 0.18f)
                                else -> CrimsonRed.copy(alpha = 0.15f)
                            },
                            selectedLabelColor = when (statusKey) {
                                "Delivered" -> JadeGreen
                                "Pending" -> Color(0xFFB78103)
                                "Cancelled" -> CrimsonRed
                                else -> CrimsonRed
                            }
                        ),
                        modifier = Modifier.testTag("order_status_chip_$statusKey")
                    )
                }
            }

            if (filteredOrders.isEmpty()) {
                // Empty state for this filter
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.size(80.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.ShoppingBag,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.outline,
                                    modifier = Modifier.size(40.dp)
                                )
                            }
                        }
                        Text(
                            text = if (isNepali) "कुनै अर्डर भेटिएन" else "No orders found",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isNepali)
                                "तपाईंसँग यो स्थितिमा कुनै अर्डरहरू छैनन्।"
                            else
                                "You have no orders matching this status.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Button(
                            onClick = onBrowseProducts,
                            colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(if (isNepali) "सामानहरू हेर्नुहोस् (Browse Products)" else "Browse Products")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 32.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(filteredOrders, key = { it.id }) { order ->
                        PastOrderCard(
                            order = order,
                            isNepali = isNepali,
                            onCancelClick = { orderToCancel = order },
                            onReorderClick = {
                                viewModel.reorder(order)
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        if (isNepali) "सामानहरू झोलामा थपियो!" else "Items added to cart!"
                                    )
                                }
                                onNavigateToCart()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PastOrderCard(
    order: Order,
    isNepali: Boolean,
    onCancelClick: () -> Unit,
    onReorderClick: () -> Unit
) {
    val context = LocalContext.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("order_card_${order.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Order ID & Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "#${order.id}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Distinct Status Badge
                val (badgeColor, statusText, statusIcon) = when (order.st) {
                    "Delivered" -> Triple(JadeGreen, if (isNepali) "✓ डेलिभर भयो (Delivered)" else "Delivered", Icons.Default.CheckCircle)
                    "Accepted" -> Triple(NepalBlue, if (isNepali) "📦 स्वीकृत (Accepted)" else "Accepted", Icons.Default.LocalShipping)
                    "Cancelled" -> Triple(CrimsonRed, if (isNepali) "✕ रद्द भएको (Cancelled)" else "Cancelled", Icons.Default.Cancel)
                    else -> Triple(AmberGold, if (isNepali) "🕒 पर्खाइमा (Pending)" else "Pending", Icons.Default.HourglassTop)
                }

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = badgeColor.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, badgeColor.copy(alpha = 0.4f))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = statusIcon,
                            contentDescription = null,
                            tint = badgeColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = statusText,
                            color = badgeColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Metadata: Shop Name & Order Date
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Storefront,
                    contentDescription = null,
                    tint = CrimsonRed,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = order.shopName.ifBlank { "मेरो बजार पसल (Mero Bazar Shop)" },
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isNepali) "📅 मिति: ${order.d}" else "📅 Date: ${order.d}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "• ${order.payMode}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Divider(modifier = Modifier.padding(vertical = 12.dp))

            // Order Summary Details: Itemized List
            Text(
                text = if (isNepali) "अर्डर गरिएका सामानहरू (Items Summary):" else "Items Summary:",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(6.dp))

            order.items.forEach { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = item.n,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = "×${item.q}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = "₨ ${item.p * item.q}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Divider(modifier = Modifier.padding(vertical = 10.dp))

            // Delivery Details & Total summary
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp).padding(top = 2.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${order.cname} (${order.cphone})",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = order.caddress.ifBlank { "काठमाडौं, नेपाल" },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Total Price display
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = if (isNepali) "जम्मा रकम (Total)" else "Grand Total",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "₨ ${order.total}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = CrimsonRed
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Order Status Tracker / Cancelled alert
            if (order.st == "Cancelled") {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = CrimsonRed.copy(alpha = 0.1f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Cancel,
                            contentDescription = null,
                            tint = CrimsonRed,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isNepali)
                                "यो अर्डर रद्द गरिएको छ। थप जानकारीका लागि पसललाई सम्पर्क गर्नुहोस्।"
                            else
                                "This order was cancelled. Contact merchant for details.",
                            style = MaterialTheme.typography.bodySmall,
                            color = CrimsonRed,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            } else {
                // Progress Timeline
                PastOrderTimeline(status = order.st, isNepali = isNepali)
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Google Docs Invoice & Calendar reminder actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        val docText = GoogleDocsHelper.generateInvoiceDoc(order)
                        GoogleDocsHelper.shareDocument(
                            context = context,
                            docTitle = "MeroBazar_Invoice_${order.id}",
                            content = docText
                        )
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isNepali) "📄 Docs बिजक" else "📄 Docs Bill", fontSize = 11.sp, maxLines = 1)
                }

                OutlinedButton(
                    onClick = {
                        NepalCalendarHelper.addOrderDeliveryReminder(
                            context = context,
                            orderId = order.id,
                            shopName = order.shopName
                        )
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isNepali) "📅 क्यालेन्डर" else "📅 Calendar", fontSize = 11.sp, maxLines = 1)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action Buttons: Cancel (if Pending) & Reorder
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (order.st == "Pending") {
                    OutlinedButton(
                        onClick = onCancelClick,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = CrimsonRed),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Cancel, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isNepali) "रद्द गर्नुहोस्" else "Cancel Order", fontSize = 12.sp)
                    }
                }

                Button(
                    onClick = onReorderClick,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Replay, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isNepali) "पुन: अर्डर (Buy Again)" else "Buy Again", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun PastOrderTimeline(status: String, isNepali: Boolean) {
    val steps = listOf("Pending", "Accepted", "Delivered")
    val currentStep = when (status) {
        "Delivered" -> 2
        "Accepted" -> 1
        else -> 0
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        steps.forEachIndexed { index, stepName ->
            val isDone = index <= currentStep
            val label = when (stepName) {
                "Pending" -> if (isNepali) "अर्डर प्राप्त" else "Placed"
                "Accepted" -> if (isNepali) "प्याक भयो" else "Packed"
                else -> if (isNepali) "डेलिभर" else "Delivered"
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(if (isDone) JadeGreen else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (isDone) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                    } else {
                        Text("${index + 1}", fontSize = 10.sp, color = Color.White)
                    }
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = if (isDone) FontWeight.Bold else FontWeight.Normal,
                    color = if (isDone) JadeGreen else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (index < steps.size - 1) {
                Box(
                    modifier = Modifier
                        .height(2.dp)
                        .weight(1f)
                        .padding(horizontal = 4.dp)
                        .background(if (index < currentStep) JadeGreen else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                )
            }
        }
    }
}
