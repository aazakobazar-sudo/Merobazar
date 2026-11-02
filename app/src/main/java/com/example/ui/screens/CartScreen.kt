package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Remove
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Product
import com.example.ui.MainViewModel
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.JadeGreen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit,
    onOrderPlaced: () -> Unit
) {
    val isNepali by viewModel.isNepali.collectAsState()
    val cart by viewModel.cart.collectAsState()
    val products by viewModel.filteredProducts.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val deliveryAddresses by viewModel.deliveryAddresses.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var address by remember {
        mutableStateOf(
            if (currentUser != null) "${currentUser?.local}-${currentUser?.ward}, ${currentUser?.dist}" else "Kathmandu-10, Bagmati"
        )
    }
    var phone by remember { mutableStateOf(currentUser?.phone ?: "9841234567") }
    var paymentMode by remember { mutableStateOf("Cash on Delivery") }

    // VAT and Loyalty Coins state
    var isVatIncluded by remember { mutableStateOf(false) }
    var usePointsDiscount by remember { mutableStateOf(false) }

    // Promo code state
    var promoCodeInput by remember { mutableStateOf("") }
    var appliedDiscount by remember { mutableStateOf(0L) }
    var promoSuccessMessage by remember { mutableStateOf<String?>(null) }

    // Direct Quantity Edit Dialog state
    var editingProduct by remember { mutableStateOf<Product?>(null) }
    var editQuantityInput by remember { mutableStateOf("") }

    // Clear cart confirmation dialog
    var showClearCartConfirm by remember { mutableStateOf(false) }

    val cartItems = cart.mapNotNull { (id, qty) ->
        val p = products.find { it.id == id }
        if (p != null) p to qty else null
    }

    val subtotal = cartItems.sumOf { (p, qty) -> p.effectivePrice * qty }
    val deliveryFee = if (subtotal >= 1000L || subtotal == 0L) 0L else 50L
    val vatAmount = if (isVatIncluded) (subtotal * 13 / 100) else 0L
    val userPts = currentUser?.pts ?: 0L
    val pointsDiscount = if (usePointsDiscount) (userPts.coerceAtMost(subtotal)) else 0L
    val totalDiscount = appliedDiscount + pointsDiscount
    val grandTotal = (subtotal + deliveryFee + vatAmount - totalDiscount).coerceAtLeast(0L)

    // Direct Quantity Editing Dialog
    if (editingProduct != null) {
        val prod = editingProduct!!
        AlertDialog(
            onDismissRequest = { editingProduct = null },
            title = {
                Text(
                    text = if (isNepali) "मात्रा परिवर्तन गर्नुहोस्" else "Update Quantity",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = prod.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = if (isNepali) "अधिकतम मौज्दात: ${prod.stock} वटा" else "Available Stock: ${prod.stock}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = editQuantityInput,
                        onValueChange = { editQuantityInput = it.filter { c -> c.isDigit() } },
                        label = { Text(if (isNepali) "संख्या (१ देखि ${prod.stock})" else "Quantity (1-${prod.stock})") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("edit_quantity_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val qty = editQuantityInput.toIntOrNull() ?: 1
                        viewModel.updateCartQuantity(prod.id, qty)
                        editingProduct = null
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                if (isNepali) "मात्रा अद्यावधिक भयो" else "Quantity updated"
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed)
                ) {
                    Text(if (isNepali) "अद्यावधिक" else "Update")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingProduct = null }) {
                    Text(if (isNepali) "रद्द" else "Cancel")
                }
            }
        )
    }

    // Clear cart confirmation
    if (showClearCartConfirm) {
        AlertDialog(
            onDismissRequest = { showClearCartConfirm = false },
            title = { Text(if (isNepali) "झोला खाली गर्ने?" else "Clear Shopping Cart?") },
            text = {
                Text(
                    if (isNepali)
                        "के तपाईं झोलामा रहेका सबै सामानहरू हटाउन चाहनुहुन्छ?"
                    else
                        "Are you sure you want to remove all items from your cart?"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearCart()
                        showClearCartConfirm = false
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                if (isNepali) "झोला खाली भयो" else "Cart cleared"
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed)
                ) {
                    Text(if (isNepali) "हटाउनुहोस्" else "Clear All")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearCartConfirm = false }) {
                    Text(if (isNepali) "रद्द" else "Cancel")
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
                            text = if (isNepali) "किनमेल झोला (Cart)" else "My Shopping Cart",
                            fontWeight = FontWeight.Bold
                        )
                        if (cartItems.isNotEmpty()) {
                            Text(
                                text = "${cartItems.size} " + if (isNepali) "प्रकारका सामानहरू" else "items",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (cartItems.isNotEmpty()) {
                        IconButton(
                            onClick = { showClearCartConfirm = true },
                            modifier = Modifier.testTag("clear_cart_button")
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Clear Cart", tint = CrimsonRed)
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (cartItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShoppingBag,
                            contentDescription = null,
                            tint = CrimsonRed,
                            modifier = Modifier.size(48.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = if (isNepali) "तपाईंको किनमेल झोला खाली छ!" else "Your shopping cart is empty!",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (isNepali) "मेरो बजारका स्थानीय पसलहरूबाट ताजा र उत्कृष्ट सामानहरू छनोट गर्नुहोस्।" else "Explore verified local shops and find great products in Nepal.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = onBack,
                        colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("start_shopping_button")
                    ) {
                        Text(if (isNepali) "🛍️ किनमेल सुरु गर्नुहोस्" else "🛍️ Start Shopping", fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Free delivery notice strip
                item {
                    val remainingForFree = (1000L - subtotal).coerceAtLeast(0L)
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (remainingForFree == 0L) JadeGreen.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = if (remainingForFree == 0L) "🎉" else "🚚", fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (remainingForFree == 0L)
                                    (if (isNepali) "बधाई छ! तपाईंले निःशुल्क डेलिभरी पाउनुभयो!" else "Congratulations! You get FREE Delivery!")
                                else
                                    (if (isNepali) "थप ₨ $remainingForFree को सामान थप्नुहोस् र निःशुल्क डेलिभरी पाउनुहोस्!" else "Add ₨ $remainingForFree more to get FREE Delivery!"),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = if (remainingForFree == 0L) JadeGreen else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                // Cart item cards with Add, Remove, and Update Quantity
                items(cartItems) { (prod, qty) ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("cart_item_${prod.id}"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Category emoji
                                val emoji = when (prod.cat) {
                                    1 -> "🌾"
                                    2 -> "🧱"
                                    3 -> "💡"
                                    4 -> "🎒"
                                    else -> "📢"
                                }
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(emoji, fontSize = 24.sp)
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = prod.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Storefront, contentDescription = null, tint = CrimsonRed, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = prod.shopName,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "₨ ${prod.effectivePrice} / item",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = CrimsonRed,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                // Delete Button
                                IconButton(
                                    onClick = {
                                        viewModel.deleteCartItem(prod.id)
                                        scope.launch {
                                            snackbarHostState.showSnackbar(
                                                if (isNepali) "${prod.name} झोलाबाट हटाइयो" else "${prod.name} removed from cart"
                                            )
                                        }
                                    },
                                    modifier = Modifier.testTag("delete_item_${prod.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteOutline,
                                        contentDescription = "Remove item",
                                        tint = Color.Gray
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            Divider()
                            Spacer(modifier = Modifier.height(8.dp))

                            // Quantity Stepper and Total Price for this item
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                // Stepper with direct click-to-edit
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    // Decrement
                                    IconButton(
                                        onClick = { viewModel.removeFromCart(prod.id) },
                                        modifier = Modifier.size(32.dp).testTag("decrease_qty_${prod.id}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Remove,
                                            contentDescription = "Decrease",
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }

                                    // Clickable number to edit quantity directly
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                        modifier = Modifier
                                            .clickable {
                                                editingProduct = prod
                                                editQuantityInput = "$qty"
                                            }
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                            .testTag("qty_display_${prod.id}")
                                    ) {
                                        Text(
                                            text = "$qty",
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 15.sp,
                                            modifier = Modifier.padding(horizontal = 6.dp)
                                        )
                                    }

                                    // Increment
                                    val canAddMore = qty < prod.stock
                                    IconButton(
                                        onClick = {
                                            if (canAddMore) {
                                                viewModel.addToCart(prod.id)
                                            } else {
                                                scope.launch {
                                                    snackbarHostState.showSnackbar(
                                                        if (isNepali) "अधिकतम मौज्दात पुग्यो (${prod.stock})" else "Max stock reached (${prod.stock})"
                                                    )
                                                }
                                            }
                                        },
                                        enabled = canAddMore,
                                        modifier = Modifier.size(32.dp).testTag("increase_qty_${prod.id}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = "Increase",
                                            tint = if (canAddMore) CrimsonRed else Color.LightGray,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }

                                // Total for this item
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "₨ ${prod.effectivePrice * qty}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = CrimsonRed
                                    )
                                    if (prod.stock <= 5) {
                                        Text(
                                            text = if (isNepali) "${prod.stock} वटा मात्र बाँकी!" else "Only ${prod.stock} left!",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = CrimsonRed
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Promo / Voucher Code section
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.LocalOffer, contentDescription = null, tint = CrimsonRed)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isNepali) "प्रोमो कोड / भौचर (Promo Code)" else "Promo / Discount Voucher",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                OutlinedTextField(
                                    value = promoCodeInput,
                                    onValueChange = { promoCodeInput = it.uppercase() },
                                    placeholder = { Text(if (isNepali) "जस्तै: MEROBAZAR" else "e.g. MEROBAZAR") },
                                    modifier = Modifier.weight(1f).testTag("promo_code_input"),
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = {
                                        if (promoCodeInput.trim().equals("MEROBAZAR", ignoreCase = true) ||
                                            promoCodeInput.trim().equals("DASHAIN", ignoreCase = true)
                                        ) {
                                            appliedDiscount = 100L
                                            promoSuccessMessage = if (isNepali) "₨१०० को विशेष छुट लागू भयो! 🎉" else "₨100 Promo Discount Applied! 🎉"
                                        } else {
                                            scope.launch {
                                                snackbarHostState.showSnackbar(
                                                    if (isNepali) "अमान्य भौचर कोड" else "Invalid voucher code"
                                                )
                                            }
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text(if (isNepali) "लागू" else "Apply")
                                }
                            }

                            if (promoSuccessMessage != null) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = promoSuccessMessage ?: "",
                                    color = JadeGreen,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Delivery Info Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = if (isNepali) "📦 डेलिभरी ठेगाना र सम्पर्क" else "📦 Delivery Details",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            if (deliveryAddresses.isNotEmpty()) {
                                Text(
                                    text = if (isNepali) "तपाईंको सुरक्षित ठेगानाहरू:" else "Your Saved Addresses:",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    deliveryAddresses.forEach { item ->
                                        val formatted = "${item.street}, ${item.cityWard}, ${item.district}"
                                        FilterChip(
                                            selected = address == formatted,
                                            onClick = {
                                                address = formatted
                                                if (item.phone.isNotBlank()) phone = item.phone
                                            },
                                            label = { Text("${item.label}: ${item.street}") },
                                            leadingIcon = {
                                                Icon(
                                                    if (item.isDefault) Icons.Default.CheckCircle else Icons.Default.Storefront,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                            }

                            OutlinedTextField(
                                value = address,
                                onValueChange = { address = it },
                                label = { Text(if (isNepali) "डेलिभरी ठेगाना (गाउँ/नगर-वडा, जिल्ला)" else "Delivery Address") },
                                modifier = Modifier.fillMaxWidth().testTag("delivery_address_input"),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = phone,
                                onValueChange = { phone = it },
                                label = { Text(if (isNepali) "सम्पर्क फोन (Mobile Number)" else "Phone Number") },
                                modifier = Modifier.fillMaxWidth().testTag("delivery_phone_input"),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                            )
                        }
                    }
                }

                // Payment Mode Selection Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = if (isNepali) "💳 भुक्तानी तरिका (Payment Method)" else "💳 Payment Method",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            val modes = listOf(
                                "Cash on Delivery" to if (isNepali) "सामान पाएपछि नगद (Cash on Delivery)" else "Cash on Delivery",
                                "eSewa" to "eSewa Mobile Wallet",
                                "Khalti" to "Khalti Digital Wallet"
                            )
                            modes.forEach { (modeKey, modeTitle) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { paymentMode = modeKey },
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = paymentMode == modeKey,
                                        onClick = { paymentMode = modeKey },
                                        colors = RadioButtonDefaults.colors(selectedColor = CrimsonRed)
                                    )
                                    Text(text = modeTitle, style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                    }
                }

                // VAT and Loyalty Coin Options Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = if (isNepali) "🏷️ भ्याट बिल तथा लोयल्टी छुट" else "🏷️ VAT Bill & Loyalty Discounts",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { isVatIncluded = !isVatIncluded },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = isVatIncluded,
                                    onCheckedChange = { isVatIncluded = it }
                                )
                                Column(modifier = Modifier.padding(start = 6.dp)) {
                                    Text(
                                        text = if (isNepali) "१३% भ्याट बिल लिनुहोस् (IRD लक्की ड्र योग्य)" else "Include 13% Official VAT Bill (Eligible for IRD Tax Prize)",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = if (isNepali) "आन्तरिक राजस्व विभाग (IRD) बाट क्यासब्याक र लक्की ड्र पुरस्कार!" else "Eligible for IRD 10% cashback lottery",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            if (userPts > 0) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { usePointsDiscount = !usePointsDiscount },
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = usePointsDiscount,
                                        onCheckedChange = { usePointsDiscount = it }
                                    )
                                    Column(modifier = Modifier.padding(start = 6.dp)) {
                                        Text(
                                            text = if (isNepali) "लोयल्टी क्वाइन प्रयोग गर्नुहोस् (उपलब्ध: ${userPts} Pts)" else "Redeem Loyalty Coins (Available: ${userPts} Pts)",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            text = if (isNepali) "₨ ${userPts.coerceAtMost(subtotal)} सिधै छुट हुनेछ" else "Discounts Rs ${userPts.coerceAtMost(subtotal)} directly",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = JadeGreen
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Order Price Breakdown Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(if (isNepali) "सामानको मूल्य (Subtotal)" else "Subtotal")
                                Text("₨ $subtotal", fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(if (isNepali) "डेलिभरी शुल्क (Delivery Fee)" else "Delivery Fee")
                                Text(
                                    text = if (deliveryFee == 0L) (if (isNepali) "निःशुल्क (FREE)" else "FREE") else "₨ $deliveryFee",
                                    color = if (deliveryFee == 0L) JadeGreen else MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            if (isVatIncluded) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(if (isNepali) "१३% सरकारी भ्याट (VAT)" else "13% Gov VAT")
                                    Text("₨ $vatAmount", fontWeight = FontWeight.Bold)
                                }
                            }
                            if (appliedDiscount > 0L) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(if (isNepali) "भौचर छुट (Voucher)" else "Voucher Discount", color = JadeGreen)
                                    Text("-₨ $appliedDiscount", fontWeight = FontWeight.Bold, color = JadeGreen)
                                }
                            }
                            if (usePointsDiscount && pointsDiscount > 0L) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(if (isNepali) "लोयल्टी क्वाइन छुट" else "Loyalty Coins Discount", color = JadeGreen)
                                    Text("-₨ $pointsDiscount", fontWeight = FontWeight.Bold, color = JadeGreen)
                                }
                            }
                            Divider(modifier = Modifier.padding(vertical = 10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isNepali) "कुल जम्मा (Total Amount)" else "Total Amount",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = "₨ $grandTotal",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Black,
                                    color = CrimsonRed
                                )
                            }
                        }
                    }
                }

                // Place Order Button
                item {
                    Button(
                        onClick = {
                            if (address.isBlank() || phone.isBlank()) {
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        if (isNepali) "कृपया डेलिभरी ठेगाना र फोन नम्बर लेख्नुहोस्" else "Please complete address and phone number"
                                    )
                                }
                                return@Button
                            }
                            viewModel.placeOrder(
                                address = address,
                                phone = phone,
                                payMode = paymentMode,
                                vat = vatAmount,
                                discountApplied = totalDiscount
                            ) { order ->
                                if (order != null) {
                                    onOrderPlaced()
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .testTag("place_order_button")
                    ) {
                        Text(
                            text = if (isNepali) "अर्डर पक्का गर्नुहोस् (₨ $grandTotal)" else "Place Order (₨ $grandTotal)",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}
