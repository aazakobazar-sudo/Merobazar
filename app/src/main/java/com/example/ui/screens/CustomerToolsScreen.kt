package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Compare
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Stars
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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Product
import com.example.ui.MainViewModel
import com.example.ui.components.ProductCard
import com.example.ui.components.ShopCard
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.JadeGreen
import com.example.ui.theme.NepalBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerToolsScreen(
    viewModel: MainViewModel,
    initialTab: Int = 0,
    onBack: () -> Unit,
    onOpenShop: (String) -> Unit = {},
    onOpenProduct: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val isNepali by viewModel.isNepali.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val shops by viewModel.shops.collectAsState()
    val products by viewModel.products.collectAsState()
    val orders by viewModel.orders.collectAsState()
    val savedShops by viewModel.savedShops.collectAsState()
    val savedProducts by viewModel.savedProducts.collectAsState()
    val blockedShops by viewModel.blockedShops.collectAsState()
    val deliveryAddresses by viewModel.deliveryAddresses.collectAsState()
    val pointHistory by viewModel.pointHistory.collectAsState()

    var selectedTab by remember { mutableIntStateOf(initialTab) }

    // Dialog for Adding New Address
    var showAddAddressDialog by remember { mutableStateOf(false) }
    var newAddressLabel by remember { mutableStateOf("घर") }
    var newAddressText by remember { mutableStateOf("") }

    if (showAddAddressDialog) {
        AlertDialog(
            onDismissRequest = { showAddAddressDialog = false },
            title = { Text(if (isNepali) "नयाँ ठेगाना थप्नुहोस्" else "Add New Delivery Address", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = newAddressLabel,
                        onValueChange = { newAddressLabel = it },
                        label = { Text(if (isNepali) "लेबल (जस्तै: घर, पसल, अफिस)" else "Label (e.g. Home, Office)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = newAddressText,
                        onValueChange = { newAddressText = it },
                        label = { Text(if (isNepali) "पूरा ठेगाना (गाउँ/नगर, वडा, जिल्ला)" else "Full Address") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newAddressText.isNotBlank()) {
                            viewModel.addDeliveryAddress(newAddressLabel, newAddressText)
                            newAddressText = ""
                            showAddAddressDialog = false
                            Toast.makeText(context, if (isNepali) "ठेगाना थपियो" else "Address saved", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed)
                ) {
                    Text(if (isNepali) "सुरक्षित गर्नुहोस्" else "Save Address")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddAddressDialog = false }) {
                    Text(if (isNepali) "रद्द" else "Cancel")
                }
            }
        )
    }

    val tabTitles = listOf(
        if (isNepali) "❤️ सुरक्षित" else "Favorites",
        if (isNepali) "📍 ठेगानाहरू" else "Addresses",
        if (isNepali) "🪙 लोयल्टी" else "Loyalty",
        if (isNepali) "🔎 तुलना" else "Compare",
        if (isNepali) "🎁 IRD कर उपहार" else "IRD Tax Prize",
        if (isNepali) "🚫 ब्लक" else "Blocked"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isNepali) "🛠️ ग्राहक सेवा तथा सुविधाहरू" else "Customer Utilities & Tools",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                edgePadding = 16.dp,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title, fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal) }
                    )
                }
            }

            when (selectedTab) {
                0 -> {
                    // TAB 0: Saved items & shops
                    val savedShopList = shops.filter { savedShops.contains(it.id) }
                    val savedProdList = products.filter { savedProducts.contains(it.id) }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        item {
                            Text(
                                text = if (isNepali) "❤️ मनपरेका पसलहरू (${savedShopList.size})" else "Saved Shops (${savedShopList.size})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (savedShopList.isEmpty()) {
                            item {
                                Text(
                                    text = if (isNepali) "कुनै पसल सुरक्षित गरिएको छैन।" else "No saved shops.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            items(savedShopList) { shop ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(
                                            modifier = Modifier.weight(1f).clickable { onOpenShop(shop.id) }
                                        ) {
                                            Text(shop.shop?.name ?: shop.name, fontWeight = FontWeight.Bold)
                                            Text("🏪 ${shop.shop?.type} · ${shop.local}", style = MaterialTheme.typography.bodySmall)
                                        }
                                        IconButton(onClick = { viewModel.toggleSaveShop(shop.id) }) {
                                            Icon(Icons.Default.Favorite, contentDescription = "Unsave", tint = CrimsonRed)
                                        }
                                    }
                                }
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (isNepali) "🛍️ मनपरेका सामानहरू (${savedProdList.size})" else "Saved Products (${savedProdList.size})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (savedProdList.isEmpty()) {
                            item {
                                Text(
                                    text = if (isNepali) "कुनै सामान सुरक्षित गरिएको छैन।" else "No saved products.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            items(savedProdList) { prod ->
                                ProductCard(
                                    product = prod,
                                    onAddToCart = { viewModel.addToCart(prod.id) },
                                    onClick = { onOpenProduct(prod.id) }
                                )
                            }
                        }
                    }
                }

                1 -> {
                    // TAB 1: Address Management (xaddr)
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isNepali) "📍 डेलिभरी ठेगानाहरू" else "Delivery Addresses",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Button(
                                    onClick = { showAddAddressDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(if (isNepali) "नयाँ ठेगाना" else "Add New")
                                }
                            }
                        }

                        items(deliveryAddresses) { addr ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "📍 ${addr.label}",
                                                fontWeight = FontWeight.Bold,
                                                style = MaterialTheme.typography.titleSmall
                                            )
                                            if (addr.isDefault) {
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Badge(containerColor = JadeGreen) {
                                                    Text(if (isNepali) "डिफल्ट" else "Default", color = Color.White, modifier = Modifier.padding(horizontal = 4.dp))
                                                }
                                            }
                                        }

                                        Row {
                                            if (!addr.isDefault) {
                                                TextButton(onClick = { viewModel.setDefaultAddress(addr.id) }) {
                                                    Text(if (isNepali) "डिफल्ट बनाउनुहोस्" else "Set Default", fontSize = 12.sp)
                                                }
                                            }
                                            IconButton(onClick = { viewModel.deleteDeliveryAddress(addr.id) }) {
                                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Gray, modifier = Modifier.size(18.dp))
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = addr.address,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                2 -> {
                    // TAB 2: Loyalty points & ledger (xloy)
                    val pts = currentUser?.pts ?: 100L
                    val tier = when {
                        pts >= 2000L -> "Gold 🏆"
                        pts >= 500L -> "Silver 🥈"
                        else -> "Bronze 🥉"
                    }
                    val nextTier = when {
                        pts >= 2000L -> 2000L
                        pts >= 500L -> 2000L
                        else -> 500L
                    }
                    val progress = if (pts >= 2000L) 1.0f else (pts.toFloat() / nextTier)

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = NepalBlue)
                            ) {
                                Column(modifier = Modifier.padding(18.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = if (isNepali) "🪙 लोयल्टी पोइन्ट ब्यालेन्स" else "Loyalty Points",
                                            color = Color.White.copy(alpha = 0.9f),
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = AmberGold
                                        ) {
                                            Text(
                                                text = tier,
                                                color = Color.Black,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "$pts पोइन्ट (Coins)",
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.Black,
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    LinearProgressIndicator(
                                        progress = { progress },
                                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                                        color = AmberGold,
                                        trackColor = Color.White.copy(alpha = 0.3f),
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = if (pts >= 2000L)
                                            (if (isNepali) "तपाईं उच्च गोल्ड टियरमा हुनुहुन्छ!" else "You are at maximum Gold Tier!")
                                        else
                                            (if (isNepali) "अर्को टियरसम्म पुग्न: ${nextTier - pts} पोइन्ट बाँकी" else "Points to next tier: ${nextTier - pts}"),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White.copy(alpha = 0.85f)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = if (isNepali) "💡 प्रत्येक रु १०० खरिदमा १० पोइन्ट प्राप्त हुन्छ।" else "Earn 10 points for every Rs 100 spent.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = AmberGold,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }

                        item {
                            Text(
                                text = if (isNepali) "📜 पोइन्ट आम्दानी तथा खर्च विवरण" else "Points Ledger History",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        items(pointHistory) { item ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(item.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                        Text(item.date, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Text(
                                        text = if (item.amount > 0) "+${item.amount}" else "${item.amount}",
                                        fontWeight = FontWeight.Black,
                                        color = if (item.amount > 0) JadeGreen else CrimsonRed,
                                        fontSize = 16.sp
                                    )
                                }
                            }
                        }
                    }
                }

                3 -> {
                    // TAB 3: Smart Product Finder & Comparison Tool (xai)
                    var prod1Id by remember { mutableStateOf(products.firstOrNull()?.id ?: "") }
                    var prod2Id by remember { mutableStateOf(products.getOrNull(1)?.id ?: "") }

                    val p1 = products.find { it.id == prod1Id } ?: products.firstOrNull()
                    val p2 = products.find { it.id == prod2Id } ?: products.getOrNull(1)

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = if (isNepali) "⚖️ उत्पादन तुलना (Product Comparison)" else "Product Comparison Tool",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = if (isNepali) "दुईवटा सामान छनोट गरी मूल्य, छुट र स्टक सिधै तुलना गर्नुहोस्।" else "Compare two items side-by-side.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        if (p1 != null && p2 != null) {
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Card(
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Text(p1.name, fontWeight = FontWeight.Bold, maxLines = 1)
                                            Text("🏪 ${p1.shopName}", style = MaterialTheme.typography.bodySmall)
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text("मूल्य: ₨ ${p1.effectivePrice}", fontWeight = FontWeight.ExtraBold, color = CrimsonRed)
                                            if (p1.dis > 0) Text("छुट: ₨ ${p1.dis}", color = JadeGreen, fontSize = 12.sp)
                                            Text("स्टक: ${p1.stock} वटा", style = MaterialTheme.typography.bodySmall)
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Button(
                                                onClick = { viewModel.addToCart(p1.id) },
                                                colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed),
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Text(if (isNepali) "झोलामा हाल्नुहोस्" else "Add", fontSize = 12.sp)
                                            }
                                        }
                                    }

                                    Card(
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Text(p2.name, fontWeight = FontWeight.Bold, maxLines = 1)
                                            Text("🏪 ${p2.shopName}", style = MaterialTheme.typography.bodySmall)
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text("मूल्य: ₨ ${p2.effectivePrice}", fontWeight = FontWeight.ExtraBold, color = CrimsonRed)
                                            if (p2.dis > 0) Text("छुट: ₨ ${p2.dis}", color = JadeGreen, fontSize = 12.sp)
                                            Text("स्टक: ${p2.stock} वटा", style = MaterialTheme.typography.bodySmall)
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Button(
                                                onClick = { viewModel.addToCart(p2.id) },
                                                colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed),
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Text(if (isNepali) "झोलामा हाल्नुहोस्" else "Add", fontSize = 12.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                4 -> {
                    // TAB 4: IRD Tax Prize bill registrations (tax.js)
                    val qualifyingOrders = orders.filter { it.st in listOf("Processing", "Packed", "Shipped", "Delivered", "Accepted") }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = if (isNepali) "🎁 करदाता प्रोत्साहन उपहार (IRD)" else "🎁 Taxpayer Incentive Prize (IRD)",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = if (isNepali)
                                            "आन्तरिक राजस्व विभाग (IRD) को लक्की ड्र पोर्टलमा बिल दर्ता गर्नुहोस्। यहाँ तपाईंको स्वीकृत बिलको विवरण (बिल नं., विक्रेताको PAN, मिति, रकम) तयार छ।"
                                        else
                                            "Details prepared for IRD lucky draw invoice registry portal.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Button(
                                        onClick = {
                                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://prize.ird.gov.np/"))
                                            try { context.startActivity(intent) } catch (_: Exception) {}
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = NepalBlue),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(Icons.Default.OpenInBrowser, contentDescription = null)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(if (isNepali) "🌐 आधिकारिक पोर्टल खोल्नुहोस् (prize.ird.gov.np)" else "Open prize.ird.gov.np", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        items(qualifyingOrders) { o ->
                            val shopPan = shops.find { it.id == o.muid }?.shop?.pan ?: "600123456"
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Invoice: ${o.id}", fontWeight = FontWeight.Bold)
                                        Text("₨ ${o.total}", fontWeight = FontWeight.ExtraBold, color = CrimsonRed)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("विक्रेता: ${o.shopName} · मिति: ${o.d}", style = MaterialTheme.typography.bodySmall)
                                    Text("PAN: $shopPan", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        OutlinedButton(
                                            onClick = {
                                                val copyText = "Invoice no: ${o.id}\nSeller PAN: $shopPan\nDate: ${o.d}\nAmount: ${o.total}"
                                                clipboardManager.setText(AnnotatedString(copyText))
                                                Toast.makeText(context, if (isNepali) "विवरण कपी भयो!" else "Details copied!", Toast.LENGTH_SHORT).show()
                                            },
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(15.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(if (isNepali) "विवरण कपी" else "Copy", fontSize = 12.sp)
                                        }

                                        Button(
                                            onClick = {
                                                viewModel.markOrderIrdUploaded(o.id)
                                                Toast.makeText(context, "अपडेट भयो", Toast.LENGTH_SHORT).show()
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (o.irdUp.isNotBlank()) JadeGreen else Color(0xFF6B7280)
                                            ),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(15.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = if (o.irdUp.isNotBlank()) (if (isNepali) "✓ दर्ता गरियो" else "Uploaded") else (if (isNepali) "दर्ता गरेँ भन्नुहोस्" else "Mark Done"),
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                5 -> {
                    // TAB 5: Blocked shops (xblk)
                    val blockedList = shops.filter { blockedShops.contains(it.id) }
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        item {
                            Text(
                                text = if (isNepali) "🚫 ब्लक गरिएका पसलहरू (${blockedList.size})" else "Blocked Shops (${blockedList.size})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (blockedList.isEmpty()) {
                            item {
                                Text(
                                    text = if (isNepali) "कुनै पनि पसल ब्लक गरिएको छैन।" else "No blocked shops.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            items(blockedList) { shop ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(shop.shop?.name ?: shop.name, fontWeight = FontWeight.Bold)
                                        Button(
                                            onClick = { viewModel.toggleBlockShop(shop.id) },
                                            colors = ButtonDefaults.buttonColors(containerColor = JadeGreen),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text(if (isNepali) "अनब्लक गर्नुहोस्" else "Unblock")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
