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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MeroBazarSeedData
import com.example.ui.MainViewModel
import com.example.ui.components.FilterDialog
import com.example.ui.components.ProductCard
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.DarkCrimson
import com.example.ui.theme.SaffronOrange

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onOpenAiChat: () -> Unit,
    onOpenCart: () -> Unit,
    onOpenShop: (String) -> Unit,
    onOpenProduct: (String) -> Unit,
    onOpenPastOrders: () -> Unit = {},
    onOpenNepalMap: () -> Unit = {}
) {
    val isNepali by viewModel.isNepali.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val filter by viewModel.filterState.collectAsState()
    val filteredProducts by viewModel.filteredProducts.collectAsState()
    val shops by viewModel.shops.collectAsState()
    val cart by viewModel.cart.collectAsState()
    val totalCartItems = cart.values.sum()

    var showFilterDialog by remember { mutableStateOf(false) }
    var showCustomPriceInputs by remember { mutableStateOf(false) }
    var customMinInput by remember { mutableStateOf("") }
    var customMaxInput by remember { mutableStateOf("") }

    if (showFilterDialog) {
        FilterDialog(
            selectedCategory = filter.category,
            selectedDistrict = filter.district,
            inStockOnly = filter.inStockOnly,
            maxPrice = filter.maxPrice,
            sortOption = filter.sortOption,
            isNepali = isNepali,
            onApply = { cat, dist, inStock, price, sort ->
                viewModel.setFilters(cat, dist, inStock, price, sort)
                showFilterDialog = false
            },
            onDismiss = { showFilterDialog = false }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(CrimsonRed),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "म", color = Color.White, fontWeight = FontWeight.Black, fontSize = 20.sp)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (isNepali) "मेरो बजार" else "Mero Bazar",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = if (isNepali) "नेपाली अनलाइन बजार" else "Nepali Online Market",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    // Language Switcher
                    IconButton(
                        onClick = { viewModel.toggleLanguage() },
                        modifier = Modifier.testTag("lang_toggle")
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = if (isNepali) "EN" else "नेपाली",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    // Nepal Map Icon Button
                    IconButton(
                        onClick = onOpenNepalMap,
                        modifier = Modifier.testTag("nepal_map_icon_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Map,
                            contentDescription = if (isNepali) "नेपाल नक्सा" else "Nepal Map",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Past Orders Icon Button
                    IconButton(
                        onClick = onOpenPastOrders,
                        modifier = Modifier.testTag("past_orders_icon_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Receipt,
                            contentDescription = if (isNepali) "अर्डर इतिहास" else "Past Orders",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Cart Icon with badge
                    IconButton(
                        onClick = onOpenCart,
                        modifier = Modifier.testTag("cart_icon_button")
                    ) {
                        BadgedBox(
                            badge = {
                                if (totalCartItems > 0) {
                                    Badge(containerColor = CrimsonRed) {
                                        Text("$totalCartItems", color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.ShoppingCart,
                                contentDescription = "Shopping Cart"
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onOpenAiChat,
                containerColor = CrimsonRed,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("ai_assistant_fab")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "AI Assistant"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isNepali) "AI सहायक" else "AI Assistant",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // Guest mode prompt banner: prompt user to register first
            if (currentUser == null) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                            .clickable { viewModel.navigateTo("auth") }
                            .testTag("guest_register_banner"),
                        colors = CardDefaults.cardColors(containerColor = CrimsonRed.copy(alpha = 0.08f)),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🎁", fontSize = 24.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isNepali) "सर्वप्रथम खाता दर्ता गर्नुहोस्!" else "Register your account!",
                                    fontWeight = FontWeight.Bold,
                                    color = CrimsonRed,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = if (isNepali)
                                        "५० क्विन उपहार, छुट र अनलाइन अर्डर गर्न कृपया दर्ता गर्नुहोस्।"
                                    else
                                        "Get 50 bonus coins, discounts, and place orders by registering.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Button(
                                onClick = { viewModel.navigateTo("auth") },
                                colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = if (isNepali) "दर्ता" else "Register",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Top Search Bar & Filtering Options (Category + Price Range)
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    // 1. Search Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = filter.query,
                            onValueChange = { viewModel.setSearchQuery(it) },
                            placeholder = {
                                Text(
                                    if (isNepali) "सामान वा पसल खोज्नुहोस्..." else "Search products or shops..."
                                )
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Search, contentDescription = "Search", tint = CrimsonRed)
                            },
                            trailingIcon = {
                                if (filter.query.isNotEmpty()) {
                                    IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CrimsonRed,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("search_text_field")
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        IconButton(
                            onClick = { showFilterDialog = true },
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (filter.district.isNotBlank() || filter.inStockOnly || filter.maxPrice != null || filter.category > 0) CrimsonRed else MaterialTheme.colorScheme.surfaceVariant)
                                .testTag("filter_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.FilterList,
                                contentDescription = "Filters",
                                tint = if (filter.district.isNotBlank() || filter.inStockOnly || filter.maxPrice != null || filter.category > 0) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 2. Category Filtering Options
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isNepali) "📂 वर्गीकरण (Category):" else "📂 Category:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (filter.category > 0) {
                            Text(
                                text = if (isNepali) "रिसेट" else "Reset",
                                style = MaterialTheme.typography.labelSmall,
                                color = CrimsonRed,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .clickable { viewModel.setCategory(0) }
                                    .padding(4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // "All" Category Chip
                        val isAllSelected = filter.category == 0
                        FilterChip(
                            selected = isAllSelected,
                            onClick = { viewModel.setCategory(0) },
                            label = {
                                Text(
                                    text = if (isNepali) "सबै (All)" else "All",
                                    fontWeight = if (isAllSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CrimsonRed,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.testTag("cat_chip_0")
                        )

                        MeroBazarSeedData.categories.forEach { cat ->
                            val isSelected = filter.category == cat.id
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.setCategory(cat.id) },
                                leadingIcon = {
                                    Text(text = cat.emoji, fontSize = 14.sp)
                                },
                                label = {
                                    Text(
                                        text = cat.name,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CrimsonRed,
                                    selectedLabelColor = Color.White
                                ),
                                modifier = Modifier.testTag("cat_chip_${cat.id}")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 3. Price Range Filtering Options
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isNepali) "💰 मूल्य दायरा (Price Range):" else "💰 Price Range:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (filter.minPrice != null || filter.maxPrice != null || filter.pricePreset != "all") {
                            Text(
                                text = if (isNepali) "रिसेट" else "Reset",
                                style = MaterialTheme.typography.labelSmall,
                                color = CrimsonRed,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .clickable {
                                        viewModel.setPricePreset("all")
                                        showCustomPriceInputs = false
                                        customMinInput = ""
                                        customMaxInput = ""
                                    }
                                    .padding(4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val pricePresets = listOf(
                            "all" to if (isNepali) "सबै मूल्य (All)" else "All Prices",
                            "under_500" to if (isNepali) "≤ ₨ ५००" else "≤ ₨500",
                            "500_1500" to if (isNepali) "₨ ५०० - १,५००" else "₨500 - ₨1.5k",
                            "1500_5000" to if (isNepali) "₨ १,५०० - ५,०००" else "₨1.5k - ₨5k",
                            "above_5000" to if (isNepali) "≥ ₨ ५,०००" else "≥ ₨5,000",
                            "custom" to if (isNepali) "कस्टम (Custom)" else "Custom"
                        )

                        pricePresets.forEach { (presetKey, presetLabel) ->
                            val isSelected = filter.pricePreset == presetKey || (presetKey == "custom" && showCustomPriceInputs)
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    if (presetKey == "custom") {
                                        showCustomPriceInputs = !showCustomPriceInputs
                                    } else {
                                        showCustomPriceInputs = false
                                        viewModel.setPricePreset(presetKey)
                                    }
                                },
                                label = {
                                    Text(
                                        text = presetLabel,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CrimsonRed.copy(alpha = 0.85f),
                                    selectedLabelColor = Color.White
                                ),
                                modifier = Modifier.testTag("price_chip_$presetKey")
                            )
                        }
                    }

                    // Expandable Custom Price Range Inputs
                    if (showCustomPriceInputs) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = if (isNepali) "कस्टम मूल्य दायरा प्रविष्ट गर्नुहोस् (₨):" else "Enter Custom Price Range (₨):",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = customMinInput,
                                        onValueChange = { customMinInput = it.filter { c -> c.isDigit() } },
                                        placeholder = { Text(if (isNepali) "न्यूनतम" else "Min") },
                                        singleLine = true,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text("—", fontWeight = FontWeight.Bold)
                                    OutlinedTextField(
                                        value = customMaxInput,
                                        onValueChange = { customMaxInput = it.filter { c -> c.isDigit() } },
                                        placeholder = { Text(if (isNepali) "अधिकतम" else "Max") },
                                        singleLine = true,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Button(
                                        onClick = {
                                            val min = customMinInput.toLongOrNull()
                                            val max = customMaxInput.toLongOrNull()
                                            viewModel.setPriceRange(min, max)
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(if (isNepali) "लागू" else "Apply")
                                    }
                                }
                            }
                        }
                    }

                    // 4. Active Filters & Matching Count summary
                    val hasActiveFilters = filter.category > 0 || filter.minPrice != null || filter.maxPrice != null || filter.district.isNotBlank() || filter.inStockOnly || filter.query.isNotBlank()

                    if (hasActiveFilters) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${filteredProducts.size} " + if (isNepali) "सामान भेटिए:" else "found:",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            if (filter.query.isNotBlank()) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = CrimsonRed.copy(alpha = 0.12f),
                                    modifier = Modifier.clickable { viewModel.setSearchQuery("") }
                                ) {
                                    Text(
                                        text = "🔍 \"${filter.query}\" ✕",
                                        color = CrimsonRed,
                                        style = MaterialTheme.typography.labelSmall,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            if (filter.category > 0) {
                                val catName = MeroBazarSeedData.categories.find { it.id == filter.category }?.name ?: "Cat ${filter.category}"
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = CrimsonRed.copy(alpha = 0.12f),
                                    modifier = Modifier.clickable { viewModel.setCategory(0) }
                                ) {
                                    Text(
                                        text = "📂 $catName ✕",
                                        color = CrimsonRed,
                                        style = MaterialTheme.typography.labelSmall,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            if (filter.minPrice != null || filter.maxPrice != null) {
                                val pText = when {
                                    filter.minPrice != null && filter.maxPrice != null -> "₨ ${filter.minPrice} - ${filter.maxPrice}"
                                    filter.minPrice != null -> "≥ ₨ ${filter.minPrice}"
                                    else -> "≤ ₨ ${filter.maxPrice}"
                                }
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = CrimsonRed.copy(alpha = 0.12f),
                                    modifier = Modifier.clickable {
                                        viewModel.setPricePreset("all")
                                        showCustomPriceInputs = false
                                    }
                                ) {
                                    Text(
                                        text = "💰 $pText ✕",
                                        color = CrimsonRed,
                                        style = MaterialTheme.typography.labelSmall,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            if (filter.district.isNotBlank()) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = CrimsonRed.copy(alpha = 0.12f),
                                    modifier = Modifier.clickable { viewModel.setDistrict("") }
                                ) {
                                    Text(
                                        text = "📍 ${filter.district} ✕",
                                        color = CrimsonRed,
                                        style = MaterialTheme.typography.labelSmall,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            if (filter.inStockOnly) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = CrimsonRed.copy(alpha = 0.12f),
                                    modifier = Modifier.clickable { viewModel.setInStockOnly(false) }
                                ) {
                                    Text(
                                        text = "✓ In-Stock ✕",
                                        color = CrimsonRed,
                                        style = MaterialTheme.typography.labelSmall,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.clickable {
                                    viewModel.clearFilters()
                                    showCustomPriceInputs = false
                                }
                            ) {
                                Text(
                                    text = if (isNepali) "सबै हटाउनुहोस्" else "Clear All",
                                    color = MaterialTheme.colorScheme.onSurface,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Promotional Banner
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(DarkCrimson, CrimsonRed, SaffronOrange)
                                )
                            )
                            .padding(16.dp)
                    ) {
                        Column {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color.White.copy(alpha = 0.25f)
                            ) {
                                Text(
                                    text = if (isNepali) "🎁 विशेष दशैँ-तिहार अफर" else "🎁 Special Festive Offer",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = if (isNepali) "सबै स्थानीय सामानमा २०% सम्म छुट!" else "Up to 20% OFF Local Nepali Goods!",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = if (isNepali) "सिधै स्थानीय पसलबाट खरिद गर्नुहोस् र घरमै डेलिभरी पाउनुहोस्।" else "Shop directly from certified Nepali merchants.",
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 12.sp,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                }
            }

            // Quick Service Hub (Contacts, Data Analysis, Nepal Map, AI Assistant)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Contacts Hub
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { viewModel.navigateTo("contacts") }
                            .testTag("home_contacts_card"),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("📞", fontSize = 20.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (isNepali) "सम्पर्क" else "Contacts",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                maxLines = 1
                            )
                        }
                    }

                    // Data Analysis & Tax
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { viewModel.navigateTo("data_analysis") }
                            .testTag("home_data_analysis_card"),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("📊", fontSize = 20.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (isNepali) "कर/हिसाब" else "Tax & Calc",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                maxLines = 1
                            )
                        }
                    }

                    // Nepal Map
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onOpenNepalMap() }
                            .testTag("home_map_card"),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("🗺️", fontSize = 20.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (isNepali) "नक्सा" else "Map",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                maxLines = 1
                            )
                        }
                    }

                    // AI Assistant
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onOpenAiChat() }
                            .testTag("home_ai_card"),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("✨", fontSize = 20.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (isNepali) "AI सहायक" else "AI Help",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            // Video Bazar Live Reels Hero Card (PDF Section A 1.4)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .clickable { viewModel.navigateTo("video_bazar") }
                        .testTag("home_video_bazar_banner"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(Color(0xFF141E30), DarkCrimson, CrimsonRed)
                                )
                            )
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color.White.copy(alpha = 0.2f),
                                    modifier = Modifier.size(46.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text("🎬", fontSize = 24.sp)
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = if (isNepali) "भिडियो बजार (Video Reels)" else "Video Bazar Reels",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = Color.White
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            color = CrimsonRed,
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                text = "LIVE",
                                                color = Color.White,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = if (isNepali) "ठाडो रिल्स हेर्दै सिधै सामान अर्डर गर्नुहोस्" else "Watch vertical reels and buy directly from video",
                                        fontSize = 11.sp,
                                        color = Color.White.copy(alpha = 0.85f)
                                    )
                                }
                            }
                            Button(
                                onClick = { viewModel.navigateTo("video_bazar") },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = if (isNepali) "हेर्नुहोस्" else "Watch",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CrimsonRed
                                )
                            }
                        }
                    }
                }
            }

            // Category Chips Row
            item {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    Text(
                        text = if (isNepali) "वर्गीकरण (Categories)" else "Categories",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // "All" chip
                        val isAllSelected = filter.category == 0
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (isAllSelected) CrimsonRed else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .clickable { viewModel.setCategory(0) }
                                .testTag("cat_chip_0")
                        ) {
                            Text(
                                text = if (isNepali) "सबै (All)" else "All",
                                color = if (isAllSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                fontWeight = if (isAllSelected) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                fontSize = 13.sp
                            )
                        }

                        MeroBazarSeedData.categories.forEach { cat ->
                            val isSelected = filter.category == cat.id
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = if (isSelected) CrimsonRed else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .clickable { viewModel.setCategory(cat.id) }
                                    .testTag("cat_chip_${cat.id}")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Text(text = cat.emoji)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = cat.name,
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Popular Verified Shops Section
            item {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isNepali) "🏪 प्रमाणित स्थानीय पसलहरू" else "🏪 Verified Local Shops",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isNepali) "सबै (${shops.size})" else "All (${shops.size})",
                            style = MaterialTheme.typography.labelMedium,
                            color = CrimsonRed,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.clickable { viewModel.navigateTo("shops") }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(shops) { shop ->
                            Card(
                                modifier = Modifier
                                    .width(220.dp)
                                    .clickable { onOpenShop(shop.id) },
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = shop.shop?.name ?: shop.name,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Text(text = "⭐ ${shop.shop?.rating ?: 4.8}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "${shop.shop?.type} · ${shop.local}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Products Section
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isNepali) "🛍️ सामानहरू (Products)" else "🛍️ Products",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${filteredProducts.size} " + if (isNepali) "वटा उपलब्ध" else "available",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Products grid (chunked by 2)
            if (filteredProducts.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isNepali) "कुनै सामान फेला परेन। कृपया फिल्टर वा खोज शब्द परिवर्तन गर्नुहोस्।" else "No products found. Please adjust filters or query.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                val chunks = filteredProducts.chunked(2)
                items(chunks) { rowProducts ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        for (prod in rowProducts) {
                            Box(modifier = Modifier.weight(1f)) {
                                ProductCard(
                                    product = prod,
                                    onAddToCart = { viewModel.addToCart(prod.id) },
                                    onClick = { onOpenProduct(prod.id) }
                                )
                            }
                        }
                        if (rowProducts.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}
