package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import com.example.R
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BeachAccess
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Bill
import com.example.data.model.KhataEntry
import com.example.data.model.OrderItem
import com.example.data.model.Product
import com.example.ui.MainViewModel
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.DarkCrimson
import com.example.ui.theme.JadeGreen
import com.example.ui.theme.NepalBlue
import com.example.util.GoogleDocsHelper
import com.example.util.NepalCalendarHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MerchantDashboardScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val isNepali by viewModel.isNepali.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val products by viewModel.products.collectAsState()
    val orders by viewModel.orders.collectAsState()
    val bills by viewModel.bills.collectAsState()
    val khataList by viewModel.khata.collectAsState()
    val merchantInsight by viewModel.merchantInsight.collectAsState()
    val isAiLoading by viewModel.isAiLoading.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }

    // Dialog states
    var showAddProductDialog by remember { mutableStateOf(false) }
    var showNewBillDialog by remember { mutableStateOf(false) }
    var showHolidayDialog by remember { mutableStateOf(false) }
    var showAddKhataDialog by remember { mutableStateOf(false) }
    var paymentKhataEntry by remember { mutableStateOf<KhataEntry?>(null) }
    var selectedBillForView by remember { mutableStateOf<Bill?>(null) }

    // Holiday dialog state
    var holidayMsgInput by remember { mutableStateOf(currentUser?.shop?.holidayMsg.takeIf { !it.isNullOrBlank() } ?: "चाडपर्वको अवसरमा पसल बन्द छ") }

    // New product dialog fields
    var pName by remember { mutableStateOf("") }
    var pType by remember { mutableStateOf("किराना") }
    var pPriceStr by remember { mutableStateOf("") }
    var pDisStr by remember { mutableStateOf("0") }
    var pStockStr by remember { mutableStateOf("20") }
    var pCat by remember { mutableIntStateOf(1) }

    // New bill dialog fields
    var bCname by remember { mutableStateOf("") }
    var bPhone by remember { mutableStateOf("") }
    var bItemName by remember { mutableStateOf("") }
    var bItemRateStr by remember { mutableStateOf("") }
    var bItemQtyStr by remember { mutableStateOf("1") }

    // New Khata dialog fields
    var kName by remember { mutableStateOf("") }
    var kPhone by remember { mutableStateOf("") }
    var kAmountStr by remember { mutableStateOf("") }
    var kDesc by remember { mutableStateOf("") }

    // Khata payment dialog
    var payAmountStr by remember { mutableStateOf("") }

    val shopDetails = currentUser?.shop
    val isHoliday = shopDetails?.isHoliday == true
    val shopName = shopDetails?.name ?: currentUser?.name ?: "Mero Bazar Store"
    val merchantOrders = orders.filter { it.muid == currentUser?.id || it.muid == "shop_1" }
    val todaySales = merchantOrders.sumOf { it.total }

    // --- Holiday Settings Dialog ---
    if (showHolidayDialog) {
        AlertDialog(
            onDismissRequest = { showHolidayDialog = false },
            title = {
                Text(
                    text = if (isNepali) "🏖️ पसल बिदा तथा सूचना (Holiday Mode)" else "🏖️ Shop Holiday Settings",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = if (isNepali)
                            "बिदा सक्रिय गर्दा ग्राहकहरूले अर्डर गर्दा सूचना देख्नेछन्।"
                        else
                            "Activating holiday mode notifies shoppers that your shop is temporarily closed.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = holidayMsgInput,
                        onValueChange = { holidayMsgInput = it },
                        label = { Text(if (isNepali) "बिदा सूचना (Notice Message)" else "Holiday Message") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = false,
                        maxLines = 3
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val newHolidayStatus = !isHoliday
                        viewModel.setShopHoliday(newHolidayStatus, holidayMsgInput)
                        showHolidayDialog = false
                        Toast.makeText(
                            context,
                            if (newHolidayStatus)
                                (if (isNepali) "पसल बिदा सक्रिय भयो" else "Shop set to holiday mode")
                            else
                                (if (isNepali) "पसल खुला गरियो" else "Shop is now open"),
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = if (isHoliday) JadeGreen else CrimsonRed)
                ) {
                    Text(
                        if (isHoliday)
                            (if (isNepali) "पसल खोल्नुहोस् (End Holiday)" else "Reopen Shop")
                        else
                            (if (isNepali) "बिदा घोषणा (Turn On Holiday)" else "Turn On Holiday")
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showHolidayDialog = false }) {
                    Text(if (isNepali) "रद्द" else "Cancel")
                }
            }
        )
    }

    // --- Add Product Dialog ---
    if (showAddProductDialog) {
        AlertDialog(
            onDismissRequest = { showAddProductDialog = false },
            title = { Text(if (isNepali) "नयाँ सामान थप्नुहोस्" else "Add New Product") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = pName,
                        onValueChange = { pName = it },
                        label = { Text(if (isNepali) "सामानको नाम" else "Product Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = pType,
                        onValueChange = { pType = it },
                        label = { Text(if (isNepali) "प्रकार (Sub-category)" else "Sub-type") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = pPriceStr,
                            onValueChange = { pPriceStr = it.filter { c -> c.isDigit() } },
                            label = { Text(if (isNepali) "मूल्य (₨)" else "Price (₨)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = pDisStr,
                            onValueChange = { pDisStr = it.filter { c -> c.isDigit() } },
                            label = { Text(if (isNepali) "छुट (₨)" else "Discount") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                    OutlinedTextField(
                        value = pStockStr,
                        onValueChange = { pStockStr = it.filter { c -> c.isDigit() } },
                        label = { Text(if (isNepali) "मौज्दात (Stock Qty)" else "Stock Units") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val pr = pPriceStr.toLongOrNull() ?: 0L
                        val dis = pDisStr.toLongOrNull() ?: 0L
                        val st = pStockStr.toIntOrNull() ?: 10
                        if (pName.isNotBlank() && pr > 0) {
                            viewModel.addProduct(pName, pType, pr, dis, st, pCat)
                            pName = ""
                            pPriceStr = ""
                            pDisStr = "0"
                            showAddProductDialog = false
                            Toast.makeText(context, if (isNepali) "सामान थपियो!" else "Product Added!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed)
                ) {
                    Text(if (isNepali) "सुरक्षित गर्नुहोस्" else "Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddProductDialog = false }) {
                    Text(if (isNepali) "रद्द" else "Cancel")
                }
            }
        )
    }

    // --- New Bill Dialog ---
    if (showNewBillDialog) {
        AlertDialog(
            onDismissRequest = { showNewBillDialog = false },
            title = { Text(if (isNepali) "नयाँ डिजिटल बिल जारी गर्नुहोस्" else "Issue Digital Bill") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = bCname,
                        onValueChange = { bCname = it },
                        label = { Text(if (isNepali) "ग्राहकको नाम" else "Customer Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = bPhone,
                        onValueChange = { bPhone = it },
                        label = { Text(if (isNepali) "सम्पर्क फोन" else "Phone") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = bItemName,
                        onValueChange = { bItemName = it },
                        label = { Text(if (isNepali) "सामान (Item)" else "Item Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = bItemRateStr,
                            onValueChange = { bItemRateStr = it.filter { c -> c.isDigit() } },
                            label = { Text(if (isNepali) "दर (Rate)" else "Rate") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = bItemQtyStr,
                            onValueChange = { bItemQtyStr = it.filter { c -> c.isDigit() } },
                            label = { Text(if (isNepali) "परिमाण (Qty)" else "Qty") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val rate = bItemRateStr.toLongOrNull() ?: 0L
                        val qty = bItemQtyStr.toIntOrNull() ?: 1
                        if (bCname.isNotBlank() && bItemName.isNotBlank() && rate > 0) {
                            val items = listOf(OrderItem(n = bItemName, p = rate, q = qty))
                            viewModel.createBill(bCname, bPhone, items)
                            bCname = ""
                            bPhone = ""
                            bItemName = ""
                            bItemRateStr = ""
                            bItemQtyStr = "1"
                            showNewBillDialog = false
                            Toast.makeText(context, if (isNepali) "बिल जारी भयो!" else "Bill Created!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed)
                ) {
                    Text(if (isNepali) "जारी गर्नुहोस्" else "Issue")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewBillDialog = false }) {
                    Text(if (isNepali) "रद्द" else "Cancel")
                }
            }
        )
    }

    // --- Add Khata Entry Dialog ---
    if (showAddKhataDialog) {
        AlertDialog(
            onDismissRequest = { showAddKhataDialog = false },
            title = { Text(if (isNepali) "नयाँ ग्राहक खाता (उधारो थप्नुहोस्)" else "Add Khata Debit Record") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = kName,
                        onValueChange = { kName = it },
                        label = { Text(if (isNepali) "ग्राहकको नाम" else "Customer Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = kPhone,
                        onValueChange = { kPhone = it },
                        label = { Text(if (isNepali) "फोन नम्बर" else "Phone Number") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = kAmountStr,
                        onValueChange = { kAmountStr = it.filter { c -> c.isDigit() } },
                        label = { Text(if (isNepali) "उधारो रकम (₨)" else "Debt Amount (₨)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = kDesc,
                        onValueChange = { kDesc = it },
                        label = { Text(if (isNepali) "विवरण (चामल, तेल आदि)" else "Description") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val debt = kAmountStr.toLongOrNull() ?: 0L
                        if (kName.isNotBlank() && kPhone.isNotBlank() && debt > 0) {
                            viewModel.addKhataEntry(kName, kPhone, debt, kDesc)
                            kName = ""
                            kPhone = ""
                            kAmountStr = ""
                            kDesc = ""
                            showAddKhataDialog = false
                            Toast.makeText(context, if (isNepali) "खातामा सुरक्षित भयो!" else "Khata Record Saved!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed)
                ) {
                    Text(if (isNepali) "सुरक्षित गर्नुहोस्" else "Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddKhataDialog = false }) {
                    Text(if (isNepali) "रद्द" else "Cancel")
                }
            }
        )
    }

    // --- Record Khata Payment Dialog ---
    if (paymentKhataEntry != null) {
        val entry = paymentKhataEntry!!
        AlertDialog(
            onDismissRequest = { paymentKhataEntry = null },
            title = { Text(if (isNepali) "उधारो भुक्तानी दाखिला" else "Record Khata Payment") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("ग्राहक: ${entry.customerName} (बाँकी: ₨ ${entry.balance})", fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = payAmountStr,
                        onValueChange = { payAmountStr = it.filter { c -> c.isDigit() } },
                        label = { Text(if (isNepali) "दाखिला रकम (₨)" else "Amount Received (₨)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val paid = payAmountStr.toLongOrNull() ?: 0L
                        if (paid > 0) {
                            viewModel.recordKhataPayment(entry.id, paid)
                            paymentKhataEntry = null
                            payAmountStr = ""
                            Toast.makeText(context, if (isNepali) "भुक्तानी दाखिला भयो!" else "Payment Recorded!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = JadeGreen)
                ) {
                    Text(if (isNepali) "दाखिला गर्नुहोस्" else "Record")
                }
            },
            dismissButton = {
                TextButton(onClick = { paymentKhataEntry = null }) {
                    Text(if (isNepali) "रद्द" else "Cancel")
                }
            }
        )
    }

    // --- Bill View Dialog ---
    if (selectedBillForView != null) {
        val b = selectedBillForView!!
        AlertDialog(
            onDismissRequest = { selectedBillForView = null },
            title = { Text("🧾 ${b.id} - ${b.shopName}", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("मिति (Date): ${b.d}")
                    Text("ग्राहक (Customer): ${b.cname} (${b.phone})")
                    if (b.pan.isNotBlank()) Text("PAN: ${b.pan}")
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    b.items.forEach { itm ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("${itm.n} × ${itm.q}")
                            Text("₨ ${itm.total}", fontWeight = FontWeight.Bold)
                        }
                    }
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("कुल जम्मा (Total):", fontWeight = FontWeight.Bold)
                        Text("₨ ${b.total}", fontWeight = FontWeight.ExtraBold, color = CrimsonRed, fontSize = 18.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { selectedBillForView = null },
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed)
                ) {
                    Text(if (isNepali) "बन्द गर्नुहोस्" else "Close")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                painter = painterResource(id = R.drawable.logo_mero_bazar),
                                contentDescription = "Mero Bazar Logo",
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("🏪 $shopName", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            if (isHoliday) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Badge(containerColor = AmberGold) {
                                    Text(if (isNepali) "बिदा" else "Holiday", color = Color.White)
                                }
                            }
                        }
                        Text(
                            text = if (isNepali) "विक्रेता ड्यासबोर्ड (Seller Control Desk)" else "Seller & Merchant Dashboard",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showHolidayDialog = true },
                        modifier = Modifier.testTag("merchant_holiday_toggle_button")
                    ) {
                        Icon(
                            Icons.Default.BeachAccess,
                            contentDescription = "Holiday Settings",
                            tint = if (isHoliday) AmberGold else MaterialTheme.colorScheme.onSurface
                        )
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
            // Tab Row
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text(if (isNepali) "सारांश" else "Overview", fontSize = 12.sp) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(if (isNepali) "अर्डरहरू" else "Orders", fontSize = 12.sp)
                            if (merchantOrders.isNotEmpty()) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Badge(containerColor = CrimsonRed) {
                                    Text("${merchantOrders.size}", color = Color.White)
                                }
                            }
                        }
                    }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text(if (isNepali) "मौज्दात" else "Stock", fontSize = 12.sp) }
                )
                Tab(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    text = { Text(if (isNepali) "डिजिटल खाता" else "Khata", fontSize = 12.sp) }
                )
            }

            // Tab Content
            when (selectedTab) {
                0 -> {
                    // TAB 0: OVERVIEW & CORE TOOLS
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Holiday Alert Banner (if holiday is active)
                        if (isHoliday) {
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = AmberGold.copy(alpha = 0.15f)),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.BeachAccess, contentDescription = null, tint = AmberGold)
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = if (isNepali) "पसल हाल बिदामा छ!" else "Shop is Currently on Holiday!",
                                                fontWeight = FontWeight.Bold,
                                                color = AmberGold
                                            )
                                            Text(
                                                text = currentUser?.shop?.holidayMsg.takeIf { !it.isNullOrBlank() }
                                                    ?: "अर्डर आउँदा ग्राहकलाई बिदाको जानकारी दिइनेछ।",
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }
                                        TextButton(onClick = { showHolidayDialog = true }) {
                                            Text(if (isNepali) "परिवर्तन" else "Edit")
                                        }
                                    }
                                }
                            }
                        }

                        // Metrics Row
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Card(
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Text(if (isNepali) "आजको बिक्री" else "Today's Sales", style = MaterialTheme.typography.bodySmall)
                                        Text("₨ $todaySales", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = CrimsonRed)
                                        Text("${merchantOrders.size} " + if (isNepali) "अर्डरहरू" else "orders", style = MaterialTheme.typography.labelSmall)
                                    }
                                }

                                Card(
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Text(if (isNepali) "खाता / बिल संख्या" else "Bills Issued", style = MaterialTheme.typography.bodySmall)
                                        Text("${bills.size}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = NepalBlue)
                                        Text("₨ ${bills.sumOf { it.total }}", style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                            }
                        }

                        // Quick Tools Suite (Data Analysis, Contacts & Khata, Docs, Calendar)
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = if (isNepali) "⚡ मुख्य व्यापारिक औजारहरू (Business Suite)" else "⚡ Core Business Tools",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedButton(
                                            onClick = { viewModel.navigateTo("data_analysis") },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(10.dp),
                                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 8.dp)
                                        ) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Icon(Icons.Default.Analytics, contentDescription = null, tint = CrimsonRed, modifier = Modifier.size(20.dp))
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(if (isNepali) "डाटा/कर विश्लेषण" else "Data & Tax", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                            }
                                        }

                                        OutlinedButton(
                                            onClick = { viewModel.navigateTo("contacts") },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(10.dp),
                                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 8.dp)
                                        ) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Icon(Icons.Default.Contacts, contentDescription = null, tint = NepalBlue, modifier = Modifier.size(20.dp))
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(if (isNepali) "ग्राहक खाता/सम्पर्क" else "Customer Contacts", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedButton(
                                            onClick = {
                                                val doc = GoogleDocsHelper.generateBusinessReportDoc(
                                                    shopName = shopName,
                                                    orders = merchantOrders,
                                                    products = products,
                                                    isNepali = isNepali
                                                )
                                                GoogleDocsHelper.shareDocument(context, "Mero Bazar Sales Report", doc)
                                            },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(10.dp),
                                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 8.dp)
                                        ) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Icon(Icons.Default.Description, contentDescription = null, tint = JadeGreen, modifier = Modifier.size(20.dp))
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(if (isNepali) "Google Docs रिपोर्ट" else "Docs Report", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                            }
                                        }

                                        OutlinedButton(
                                            onClick = {
                                                val ok = NepalCalendarHelper.addMonthlyTaxFilingReminder(context)
                                                if (ok) {
                                                    Toast.makeText(context, if (isNepali) "२५ गते कर सम्झाउनी क्यालेन्डरमा थपियो" else "25th Tax Reminder Added to Calendar", Toast.LENGTH_SHORT).show()
                                                }
                                            },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(10.dp),
                                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 8.dp)
                                        ) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = AmberGold, modifier = Modifier.size(20.dp))
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(if (isNepali) "२५ गते कर क्यालेन्डर" else "Tax Calendar", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = { viewModel.navigateTo("video_bazar") },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(10.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed),
                                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 8.dp)
                                        ) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text("🎬", fontSize = 16.sp)
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(if (isNepali) "भिडियो मार्केटिङ (VMS)" else "Video Reels (VMS)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                            }
                                        }

                                        Button(
                                            onClick = { viewModel.navigateTo("messenger") },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(10.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = DarkCrimson),
                                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 8.dp)
                                        ) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text("📞", fontSize = 16.sp)
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(if (isNepali) "अडियो/भिडियो कल (VoIP)" else "VoIP Audio/Video", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Button(
                                        onClick = { viewModel.navigateTo("recharts_visualization") },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("btn_merchant_recharts_analytics"),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = NepalBlue),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp)
                                    ) {
                                        Icon(Icons.Default.TrendingUp, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = if (isNepali) "📊 Recharts कार्यसम्पादन तथा प्रगति एनालिटिक्स" else "📊 Recharts Performance & Progress Trends",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }

                        // Gemini AI Business Insights Box
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFBE9E7))
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = CrimsonRed)
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = if (isNepali) "AI व्यापार सल्लाहकार (Gemini)" else "Gemini Business Insights",
                                                fontWeight = FontWeight.Bold,
                                                color = CrimsonRed
                                            )
                                        }
                                        if (isAiLoading) {
                                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = CrimsonRed, strokeWidth = 2.dp)
                                        } else {
                                            OutlinedButton(
                                                onClick = { viewModel.loadMerchantAiInsights(shopName) },
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Text(if (isNepali) "सुझाव लिनुहोस्" else "Get Tips", fontSize = 12.sp)
                                            }
                                        }
                                    }

                                    if (merchantInsight != null) {
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Text(
                                            text = merchantInsight ?: "",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = Color(0xFF3E2723)
                                        )
                                    } else {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = if (isNepali) "Gemini ले तपाईंको पसलको मौज्दात, माग र छुटको विश्लेषण गरी बिक्री बढाउन सुझाव दिन्छ।" else "Gemini will predict demand and recommend restock items.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.Gray
                                        )
                                    }
                                }
                            }
                        }

                        // Issued Bills List Section
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isNepali) "📒 जारी गरिएका बिलहरू (${bills.size})" else "Issued Bills (${bills.size})",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                TextButton(onClick = { showNewBillDialog = true }) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(if (isNepali) "नयाँ बिल" else "New Bill")
                                }
                            }
                        }

                        items(bills) { b ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(text = "🧾 ${b.id} · ${b.cname}", fontWeight = FontWeight.Bold)
                                        Text(text = "${b.d} · ₨ ${b.total}", style = MaterialTheme.typography.bodySmall)
                                    }
                                    Button(
                                        onClick = { selectedBillForView = b },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed)
                                    ) {
                                        Text(if (isNepali) "हेर्नुहोस्" else "View")
                                    }
                                }
                            }
                        }
                    }
                }

                1 -> {
                    // TAB 1: CUSTOMER ORDERS & LIVE TRACKING
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Text(
                                text = if (isNepali) "📦 ग्राहक अर्डर व्यवस्थापन (${merchantOrders.size})" else "Customer Orders Management (${merchantOrders.size})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (merchantOrders.isEmpty()) {
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(Icons.Default.Storefront, contentDescription = null, modifier = Modifier.size(40.dp), tint = Color.Gray)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = if (isNepali) "अहिले कुनै नयाँ अर्डर छैन।" else "No customer orders yet.",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        } else {
                            items(merchantOrders) { order ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(order.id, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                            Badge(
                                                containerColor = when (order.st) {
                                                    "Delivered" -> JadeGreen
                                                    "Shipped", "Packed", "Processing", "Accepted" -> NepalBlue
                                                    else -> AmberGold
                                                }
                                            ) {
                                                Text(order.st, color = Color.White)
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("ग्राहक: ${order.cname} · 📞 ${order.cphone}", style = MaterialTheme.typography.bodyMedium)
                                        Text("ठेगाना: ${order.caddress}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text("सामान संख्या: ${order.items.size} वटा · कुल: ₨ ${order.total}", fontWeight = FontWeight.Bold, color = CrimsonRed)

                                        Spacer(modifier = Modifier.height(10.dp))

                                        // Status Advancement Flow
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            when (order.st) {
                                                "Pending" -> {
                                                    Button(
                                                        onClick = { viewModel.updateOrderStatus(order.id, "Accepted") },
                                                        colors = ButtonDefaults.buttonColors(containerColor = NepalBlue),
                                                        shape = RoundedCornerShape(8.dp),
                                                        modifier = Modifier.weight(1f)
                                                    ) {
                                                        Text(if (isNepali) "स्वीकार (Accept)" else "Accept", fontSize = 12.sp)
                                                    }
                                                }
                                                "Accepted" -> {
                                                    Button(
                                                        onClick = { viewModel.updateOrderStatus(order.id, "Packed") },
                                                        colors = ButtonDefaults.buttonColors(containerColor = AmberGold),
                                                        shape = RoundedCornerShape(8.dp),
                                                        modifier = Modifier.weight(1f)
                                                    ) {
                                                        Text(if (isNepali) "प्याक भयो (Packed)" else "Packed", fontSize = 12.sp)
                                                    }
                                                }
                                                "Packed" -> {
                                                    Button(
                                                        onClick = { viewModel.updateOrderStatus(order.id, "Shipped") },
                                                        colors = ButtonDefaults.buttonColors(containerColor = NepalBlue),
                                                        shape = RoundedCornerShape(8.dp),
                                                        modifier = Modifier.weight(1f)
                                                    ) {
                                                        Text(if (isNepali) "पठाइयो (Shipped)" else "Shipped", fontSize = 12.sp)
                                                    }
                                                }
                                                "Shipped" -> {
                                                    Button(
                                                        onClick = { viewModel.updateOrderStatus(order.id, "Delivered") },
                                                        colors = ButtonDefaults.buttonColors(containerColor = JadeGreen),
                                                        shape = RoundedCornerShape(8.dp),
                                                        modifier = Modifier.weight(1f)
                                                    ) {
                                                        Text(if (isNepali) "डेलिभर (Delivered)" else "Delivered", fontSize = 12.sp)
                                                    }
                                                }
                                            }

                                            // Chat with Customer button
                                            OutlinedButton(
                                                onClick = {
                                                    viewModel.navigateTo("messenger")
                                                },
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Icon(Icons.Default.Message, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(if (isNepali) "च्याट" else "Chat", fontSize = 12.sp)
                                            }

                                            // Order Tracking View
                                            OutlinedButton(
                                                onClick = {
                                                    viewModel.selectOrderForTracking(order.id)
                                                    viewModel.navigateTo("order_tracking")
                                                },
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Text(if (isNepali) "ट्र्याक" else "Track", fontSize = 12.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                2 -> {
                    // TAB 2: PRODUCTS, STOCK TOGGLE & FEATURED TOGGLE
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isNepali) "🏷️ पसलका सामान तथा मौज्दात" else "Shop Inventory & Stock",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Button(
                                    onClick = { showAddProductDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(if (isNepali) "सामान थप्नुहोस्" else "Add Product", fontSize = 12.sp)
                                }
                            }
                        }

                        items(products) { product ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(product.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                                if (product.isFeatured) {
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Icon(Icons.Default.Star, contentDescription = "Featured", tint = AmberGold, modifier = Modifier.size(18.dp))
                                                }
                                            }
                                            Text(
                                                text = "${product.type} · ₨ ${product.effectivePrice} (मौज्दात: ${product.stock} वटा)",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }

                                        // Featured Star Button
                                        IconButton(
                                            onClick = {
                                                viewModel.toggleProductFeatured(product.id, !product.isFeatured)
                                                Toast.makeText(
                                                    context,
                                                    if (!product.isFeatured)
                                                        (if (isNepali) "विशेष सामानमा राखियो ⭐" else "Marked as Featured ⭐")
                                                    else
                                                        (if (isNepali) "विशेष सामानबाट हटाइयो" else "Removed from Featured"),
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        ) {
                                            Icon(
                                                Icons.Default.Star,
                                                contentDescription = "Toggle Featured",
                                                tint = if (product.isFeatured) AmberGold else Color.LightGray
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))
                                    Divider()
                                    Spacer(modifier = Modifier.height(6.dp))

                                    // In-Stock vs Out-of-Stock Switch
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = if (product.inStock)
                                                (if (isNepali) "✅ बिक्रीमा उपलब्ध (In Stock)" else "✅ In Stock")
                                            else
                                                (if (isNepali) "❌ सकियो (Out of Stock)" else "❌ Out of Stock"),
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium,
                                            color = if (product.inStock) JadeGreen else CrimsonRed
                                        )

                                        Switch(
                                            checked = product.inStock,
                                            onCheckedChange = { isChecked ->
                                                viewModel.toggleProductStock(product.id, isChecked)
                                                Toast.makeText(
                                                    context,
                                                    if (isChecked)
                                                        (if (isNepali) "सामान उपलब्ध गरियो" else "Item marked in stock")
                                                    else
                                                        (if (isNepali) "सामान मौज्दात सकियो (Out of stock) गरियो" else "Item marked out of stock"),
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            },
                                            colors = SwitchDefaults.colors(
                                                checkedThumbColor = JadeGreen,
                                                checkedTrackColor = JadeGreen.copy(alpha = 0.4f)
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                3 -> {
                    // TAB 3: DIGITAL KHATA & PAYMENT REMINDERS (SMS / WHATSAPP / CSV)
                    val totalDebt = khataList.sumOf { it.balance }
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Khata Balance Card
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFBE9E7))
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = if (isNepali) "📒 डिजिटल खाता कुल बाँकी (Receivable Debt)" else "📒 Total Outstanding Khata Balance",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "₨ $totalDebt",
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.Black,
                                        color = CrimsonRed
                                    )
                                    Text(
                                        text = "${khataList.size} " + if (isNepali) "जना ग्राहकहरूको उधारो अभिलेख" else "customers with khata records",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = { showAddKhataDialog = true },
                                            colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(if (isNepali) "नयाँ खाता" else "Add Debit", fontSize = 12.sp)
                                        }

                                        OutlinedButton(
                                            onClick = {
                                                val csvContent = buildString {
                                                    append("Date,Customer,Phone,TotalDebt,TotalPaid,Balance,Description\n")
                                                    khataList.forEach { k ->
                                                        append("${k.date},\"${k.customerName}\",${k.phone},${k.totalDebt},${k.totalPaid},${k.balance},\"${k.description}\"\n")
                                                    }
                                                }
                                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                                    type = "text/plain"
                                                    putExtra(Intent.EXTRA_SUBJECT, "Mero Bazar Khata Report.csv")
                                                    putExtra(Intent.EXTRA_TEXT, csvContent)
                                                }
                                                context.startActivity(Intent.createChooser(shareIntent, "Share Khata CSV"))
                                            },
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(if (isNepali) "CSV सेयर" else "Export CSV", fontSize = 12.sp)
                                        }
                                    }
                                }
                            }
                        }

                        // Khata Entries List
                        items(khataList) { entry ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(entry.customerName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                            Text("📞 ${entry.phone} · ${entry.date}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                        }
                                        Badge(containerColor = if (entry.balance > 0) CrimsonRed else JadeGreen) {
                                            Text(
                                                if (entry.balance > 0) "बाँकी ₨ ${entry.balance}" else "चुक्ता भयो",
                                                color = Color.White
                                            )
                                        }
                                    }

                                    if (entry.description.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("विवरण: ${entry.description}", style = MaterialTheme.typography.bodySmall)
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("कुल उधारो: ₨ ${entry.totalDebt}", style = MaterialTheme.typography.bodySmall)
                                        Text("बुझाएको: ₨ ${entry.totalPaid}", style = MaterialTheme.typography.bodySmall, color = JadeGreen)
                                    }

                                    if (entry.balance > 0) {
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            // Record Payment Button
                                            Button(
                                                onClick = { paymentKhataEntry = entry },
                                                colors = ButtonDefaults.buttonColors(containerColor = JadeGreen),
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Text(if (isNepali) "भुक्तानी दाखिला" else "Pay", fontSize = 11.sp)
                                            }

                                            // SMS Reminder Button
                                            OutlinedButton(
                                                onClick = {
                                                    val reminderMsg = if (isNepali)
                                                        "नमस्ते ${entry.customerName} ज्यू, मेरो बजार (${shopName}) मा तपाईंको बाँकी उधारो ₨ ${entry.balance} रहेको छ। कृपया यथाशीघ्र भुक्तानी गरिदिनुहोला।"
                                                    else
                                                        "Namaste ${entry.customerName}, your pending khata balance at $shopName is Rs ${entry.balance}. Please clear it at your earliest convenience."
                                                    val smsIntent = Intent(Intent.ACTION_VIEW).apply {
                                                        data = Uri.parse("sms:${entry.phone}")
                                                        putExtra("sms_body", reminderMsg)
                                                    }
                                                    context.startActivity(smsIntent)
                                                },
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Text("SMS", fontSize = 11.sp)
                                            }

                                            // WhatsApp Reminder Button
                                            OutlinedButton(
                                                onClick = {
                                                    val reminderMsg = if (isNepali)
                                                        "नमस्ते ${entry.customerName} ज्यू, मेरो बजार (${shopName}) मा तपाईंको बाँकी उधारो ₨ ${entry.balance} रहेको छ। कृपया यथाशीघ्र भुक्तानी गरिदिनुहोला।"
                                                    else
                                                        "Namaste ${entry.customerName}, your pending khata balance at $shopName is Rs ${entry.balance}. Please clear it at your earliest convenience."
                                                    val clean = entry.phone.filter { it.isDigit() }
                                                    val fullPhone = if (clean.startsWith("977")) clean else "977$clean"
                                                    val waIntent = Intent(Intent.ACTION_VIEW).apply {
                                                        data = Uri.parse("https://api.whatsapp.com/send?phone=$fullPhone&text=${Uri.encode(reminderMsg)}")
                                                    }
                                                    context.startActivity(waIntent)
                                                },
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Text("WhatsApp", fontSize = 11.sp)
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
}
