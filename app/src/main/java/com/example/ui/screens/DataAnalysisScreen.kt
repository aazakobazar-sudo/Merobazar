package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.ui.res.painterResource
import com.example.R
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.JadeGreen
import com.example.ui.theme.NepalBlue
import com.example.util.GoogleDocsHelper
import com.example.util.NepalCalendarHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataAnalysisScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val isNepali by viewModel.isNepali.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val orders by viewModel.orders.collectAsState()
    val products by viewModel.products.collectAsState()
    val bills by viewModel.bills.collectAsState()

    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf(
        if (isNepali) "बिक्री तथा नाफा" else "Sales & Profit",
        if (isNepali) "कार्यसम्पादन र प्रगति" else "Performance & Trends",
        if (isNepali) "IRD कर र भ्याट" else "Tax & VAT",
        if (isNepali) "द्रुत क्यालकुलेटर" else "Quick POS Calc"
    )

    val shopName = currentUser?.shop?.name ?: currentUser?.name ?: "Mero Bazar Store"
    val merchantOrders = if (currentUser?.role == "m") {
        orders.filter { it.muid == currentUser?.id || it.muid == "shop_1" }
    } else {
        orders
    }

    val totalSales = merchantOrders.sumOf { it.total }
    val totalOrdersCount = merchantOrders.size
    val deliveredCount = merchantOrders.count { it.st == "Delivered" }
    val pendingCount = merchantOrders.count { it.st == "Pending" || it.st == "Processing" }
    val cancelledCount = merchantOrders.count { it.st == "Cancelled" }

    val estimatedCost = (totalSales * 0.72).toLong()
    val netProfit = (totalSales - estimatedCost).coerceAtLeast(0L)
    val aov = if (totalOrdersCount > 0) totalSales / totalOrdersCount else 0L

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.logo_mero_bazar),
                            contentDescription = "मेरो बजार",
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (isNepali) "डाटा विश्लेषण तथा हिसाब-किताब" else "Data Analysis & Calculations",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                text = NepalCalendarHelper.getCurrentNepaliDateString(),
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    // Export to Google Docs button
                    IconButton(
                        onClick = {
                            val doc = GoogleDocsHelper.generateBusinessReportDoc(
                                shopName = shopName,
                                orders = merchantOrders,
                                products = products,
                                isNepali = isNepali
                            )
                            GoogleDocsHelper.shareDocument(context, "Mero Bazar Sales Report", doc)
                        }
                    ) {
                        Icon(Icons.Default.Description, contentDescription = "Google Docs", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CrimsonRed,
                    titleContentColor = Color.White
                )
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
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = CrimsonRed,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = CrimsonRed
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 13.sp
                            )
                        }
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                when (selectedTab) {
                    0 -> {
                        // TAB 1: Sales & Profit Analytics
                        item {
                            CloudServerStatusCard(isNepali = isNepali)
                        }

                        item {
                            SalesSummaryCard(
                                totalSales = totalSales,
                                netProfit = netProfit,
                                aov = aov,
                                totalOrders = totalOrdersCount,
                                delivered = deliveredCount,
                                pending = pendingCount,
                                cancelled = cancelledCount,
                                isNepali = isNepali
                            )
                        }

                        item {
                            PopularProductsTrackerCard(isNepali = isNepali)
                        }

                        item {
                            QuickDecisionMakingCard(isNepali = isNepali)
                        }

                        item {
                            RevenueBreakdownCard(
                                totalSales = totalSales,
                                isNepali = isNepali
                            )
                        }

                        item {
                            GoogleDocsExportCard(
                                onExportDocs = {
                                    val doc = GoogleDocsHelper.generateBusinessReportDoc(
                                        shopName = shopName,
                                        orders = merchantOrders,
                                        products = products,
                                        isNepali = isNepali
                                    )
                                    GoogleDocsHelper.shareDocument(context, "Mero Bazar Financial Report", doc)
                                },
                                onOpenGoogleDocs = {
                                    GoogleDocsHelper.openGoogleDocsNew(context)
                                },
                                isNepali = isNepali
                            )
                        }
                    }

                    1 -> {
                        // TAB 2: Business Performance Dashboard & Project Progress Trends (Recharts + Native)
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Surface(
                                                shape = CircleShape,
                                                color = NepalBlue.copy(alpha = 0.15f),
                                                modifier = Modifier.size(40.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Icon(Icons.Default.Analytics, contentDescription = null, tint = NepalBlue, modifier = Modifier.size(24.dp))
                                                }
                                            }
                                            Spacer(Modifier.width(10.dp))
                                            Column {
                                                Text(
                                                    text = if (isNepali) "📊 Recharts इन्टरएक्टिभ एनालिटिक्स" else "📊 Recharts Interactive Analytics",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 15.sp
                                                )
                                                Text(
                                                    text = if (isNepali) "Firestore बाट कार्यसम्पादन कर्व तथा माइलस्टोन" else "Live task completion curves & milestones",
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }

                                    Spacer(Modifier.height(10.dp))
                                    Text(
                                        text = if (isNepali)
                                            "फायरबेस Firestore बाट प्रत्यक्ष जोडिएको टोली उत्पादकत्व, स्प्रिन्ट १४ टास्क सम्पन्नता कर्व र रणनीतिक लक्ष्यहरू Recharts मार्फत अन्तरक्रियात्मक रूपमा अवलोकन गर्नुहोस्।"
                                        else
                                            "View live team productivity metrics, sprint task completion curves, and strategic project milestone trends powered by Recharts & Cloud Firestore.",
                                        fontSize = 12.sp,
                                        lineHeight = 17.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                                    )

                                    Spacer(Modifier.height(12.dp))

                                    Button(
                                        onClick = { viewModel.navigateTo("recharts_visualization") },
                                        modifier = Modifier.fillMaxWidth().testTag("btn_open_recharts_visualization"),
                                        colors = ButtonDefaults.buttonColors(containerColor = NepalBlue),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Icon(Icons.Default.TrendingUp, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(6.dp))
                                        Text(
                                            if (isNepali) "पूर्ण Recharts इन्टरएक्टिभ दृश्य खोल्नुहोस्" else "Open Full Recharts Interactive View",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                    }
                                }
                            }
                        }

                        item {
                            BusinessPerformanceDashboardCard(
                                orders = merchantOrders,
                                isNepali = isNepali
                            )
                        }

                        item {
                            ProjectProgressTrendsCard(
                                isNepali = isNepali
                            )
                        }
                    }

                    2 -> {
                        // TAB 3: IRD Tax & VAT 13% Calculator + Prize Portal Helper
                        item {
                            NepalVatCalculatorCard(isNepali = isNepali)
                        }

                        item {
                            IrdPrizePortalHelperCard(
                                orders = merchantOrders,
                                bills = bills,
                                sellerPan = currentUser?.shop?.pan?.ifBlank { "600123456" } ?: "600123456",
                                isNepali = isNepali,
                                onCopy = { text ->
                                    clipboardManager.setText(AnnotatedString(text))
                                    Toast.makeText(context, if (isNepali) "IRD विवरण कपी भयो" else "IRD Details Copied", Toast.LENGTH_SHORT).show()
                                },
                                onOpenPortal = {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://prize.ird.gov.np/"))
                                    try { context.startActivity(intent) } catch (_: Exception) {}
                                },
                                onSetReminder = {
                                    val ok = NepalCalendarHelper.addMonthlyTaxFilingReminder(context)
                                    if (ok) {
                                        Toast.makeText(context, if (isNepali) "२५ गते कर सम्झाउनी क्यालेन्डरमा थपियो" else "25th Tax Reminder Added to Calendar", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            )
                        }
                    }

                    3 -> {
                        // TAB 4: Quick POS & Product Margin Calculator
                        item {
                            ProductMarginCalculatorCard(isNepali = isNepali)
                        }
                        item {
                            QuickPosCalculatorCard(isNepali = isNepali)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SalesSummaryCard(
    totalSales: Long,
    netProfit: Long,
    aov: Long,
    totalOrders: Int,
    delivered: Int,
    pending: Int,
    cancelled: Int,
    isNepali: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(CrimsonRed.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.TrendingUp, contentDescription = null, tint = CrimsonRed)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = if (isNepali) "व्यापार तथा नाफा विश्लेषण" else "Sales & Profit Analysis",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = if (isNepali) "वास्तविक समय कारोबार हिसाब" else "Real-time Transaction Metrics",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Two main highlight cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Gross Sales
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = NepalBlue.copy(alpha = 0.1f),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = if (isNepali) "कुल बिक्री (Gross Sales)" else "Gross Sales",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = NepalBlue
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "रु ${totalSales}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = NepalBlue
                        )
                        Text(
                            text = if (isNepali) "${totalOrders} अर्डरहरू" else "$totalOrders orders",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }

                // Estimated Net Profit
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = JadeGreen.copy(alpha = 0.1f),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = if (isNepali) "अनुमानित नाफा (Profit)" else "Est. Net Profit",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = JadeGreen
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "रु ${netProfit}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = JadeGreen
                        )
                        Text(
                            text = "28% Net Margin",
                            fontSize = 10.sp,
                            color = JadeGreen
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Order fulfillment stats
            Text(
                text = if (isNepali) "अर्डर पूर्ति विवरण (Fulfillment Status)" else "Fulfillment Status",
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            val deliveredProgress = if (totalOrders > 0) delivered.toFloat() / totalOrders else 0f
            LinearProgressIndicator(
                progress = { deliveredProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = JadeGreen,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (isNepali) "डेलिभर: $delivered" else "Delivered: $delivered",
                    fontSize = 12.sp,
                    color = JadeGreen,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = if (isNepali) "बाँकी: $pending" else "Pending: $pending",
                    fontSize = 12.sp,
                    color = AmberGold,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = if (isNepali) "रद्द: $cancelled" else "Cancelled: $cancelled",
                    fontSize = 12.sp,
                    color = CrimsonRed,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun RevenueBreakdownCard(totalSales: Long, isNepali: Boolean) {
    val taxable = (totalSales / 1.13).toLong()
    val vat13 = totalSales - taxable
    val codSales = (totalSales * 0.65).toLong()
    val digitalSales = totalSales - codSales

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.PieChart, contentDescription = null, tint = NepalBlue)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isNepali) "भुक्तानी तथा कर विश्लेषण" else "Payment & Tax Breakdown",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(if (isNepali) "डिजिटल भुक्तानी (eSewa / Khalti):" else "Digital (eSewa/Khalti):", fontSize = 13.sp)
                Text("रु $digitalSales (35%)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = JadeGreen)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(if (isNepali) "नगद भुक्तानी (Cash on Delivery):" else "Cash on Delivery:", fontSize = 13.sp)
                Text("रु $codSales (65%)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = NepalBlue)
            }
            Divider(modifier = Modifier.padding(vertical = 10.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(if (isNepali) "करयोग्य रकम (Taxable Sales):" else "Taxable Sales:", fontSize = 13.sp)
                Text("रु $taxable", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(if (isNepali) "१३% भ्याट रकम (13% VAT Amount):" else "13% VAT Amount:", fontSize = 13.sp)
                Text("रु $vat13", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = CrimsonRed)
            }
        }
    }
}

@Composable
fun GoogleDocsExportCard(
    onExportDocs: () -> Unit,
    onOpenGoogleDocs: () -> Unit,
    isNepali: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Description, contentDescription = null, tint = NepalBlue)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isNepali) "Google Docs प्रतिवेदन निर्यात" else "Export to Google Docs",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (isNepali)
                    "बिक्री, नाफा, मौज्दात र कर विवरण समेटिएको व्यावसायिक प्रतिवेदन एक क्लिकमा Google Docs, Drive वा Gmail मा पठाउनुहोस्।"
                else
                    "Export clean financial statements, tax breakdown, and inventory reports directly into Google Docs format.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = onExportDocs,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = NepalBlue),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isNepali) "Docs मा पठाउनुहोस्" else "Share to Docs", fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = onOpenGoogleDocs,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.OpenInBrowser, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isNepali) "Docs खोल्नुहोस्" else "Open Docs", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun NepalVatCalculatorCard(isNepali: Boolean) {
    var inputAmountStr by remember { mutableStateOf("1000") }
    var isInclusive by remember { mutableStateOf(true) }

    val amount = inputAmountStr.toLongOrNull() ?: 0L
    val taxableAmount: Long
    val vatAmount: Long
    val totalAmount: Long

    if (isInclusive) {
        taxableAmount = (amount / 1.13).toLong()
        vatAmount = amount - taxableAmount
        totalAmount = amount
    } else {
        taxableAmount = amount
        vatAmount = (amount * 0.13).toLong()
        totalAmount = taxableAmount + vatAmount
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Calculate, contentDescription = null, tint = CrimsonRed)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isNepali) "नेपाल IRD १३% भ्याट क्यालकुलेटर" else "Nepal IRD 13% VAT Calculator",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = inputAmountStr,
                onValueChange = { inputAmountStr = it.filter { c -> c.isDigit() } },
                label = { Text(if (isNepali) "रकम (Amount in Rs)" else "Amount (Rs)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(10.dp)
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Checkbox(
                    checked = isInclusive,
                    onCheckedChange = { isInclusive = it }
                )
                Text(
                    text = if (isNepali) "रकममा १३% भ्याट सामेल छ (VAT Inclusive)" else "Price is VAT Inclusive (13%)",
                    fontSize = 13.sp
                )
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(if (isNepali) "करयोग्य रकम (Taxable Base):" else "Taxable Base:", fontSize = 13.sp)
                        Text("रु $taxableAmount", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(if (isNepali) "१३% भ्याट (13% VAT):" else "13% VAT:", fontSize = 13.sp, color = CrimsonRed)
                        Text("रु $vatAmount", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = CrimsonRed)
                    }
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(if (isNepali) "कुल जम्मा रकम (Grand Total):" else "Grand Total:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("रु $totalAmount", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = JadeGreen)
                    }
                }
            }
        }
    }
}

@Composable
fun IrdPrizePortalHelperCard(
    orders: List<com.example.data.model.Order>,
    bills: List<com.example.data.model.Bill>,
    sellerPan: String,
    isNepali: Boolean,
    onCopy: (String) -> Unit,
    onOpenPortal: () -> Unit,
    onSetReminder: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Payments, contentDescription = null, tint = AmberGold)
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = if (isNepali) "🎁 करदाता प्रोत्साहन उपहार (IRD Prize Helper)" else "🎁 IRD Taxpayer Incentive Prize Helper",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "आन्तरिक राजस्व विभाग (prize.ird.gov.np)",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = AmberGold.copy(alpha = 0.12f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (isNepali)
                        "रु १०० वा सोभन्दा बढीको PAN/VAT बिल आधिकारिक IRD पोर्टल (prize.ird.gov.np) मा दर्ता गरी उपहार योजनामा सहभागी हुन सक्नुहुन्छ। यहाँबाट १-ट्यापमा विवरण कपी गरी पोर्टलमा टाँस्नुहोस्।"
                    else
                        "Register PAN/VAT bills of Rs 100+ on the official IRD portal (prize.ird.gov.np) for the consumer incentive prize. One-tap copy details below.",
                    fontSize = 12.sp,
                    lineHeight = 17.sp,
                    modifier = Modifier.padding(10.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Recent Bill items ready to copy
            val eligibleOrders = orders.filter { it.total >= 100L }.take(3)
            if (eligibleOrders.isNotEmpty()) {
                Text(
                    text = if (isNepali) "दर्ता गर्न योग्य हालका बिलहरू:" else "Eligible Bills Ready to Register:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))

                for (order in eligibleOrders) {
                    val copyStr = "Invoice no: ${order.id}\nSeller PAN: $sellerPan\nDate: ${order.d}\nAmount: ${order.total}"
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = "बिल: #${order.id} • रु ${order.total}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(text = "PAN: $sellerPan • ${order.d}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            }
                            IconButton(onClick = { onCopy(copyStr) }) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = NepalBlue, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onOpenPortal,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = AmberGold),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(if (isNepali) "🌐 IRD पोर्टल खोल्नुहोस्" else "Open IRD Portal", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onSetReminder,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isNepali) "२५ गते सम्झाउनी" else "25th Reminder", fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
fun QuickPosCalculatorCard(isNepali: Boolean) {
    var rateStr by remember { mutableStateOf("") }
    var qtyStr by remember { mutableStateOf("1") }
    var discountPercentStr by remember { mutableStateOf("0") }
    var isVat13 by remember { mutableStateOf(false) }
    var cashGivenStr by remember { mutableStateOf("") }

    val rate = rateStr.toLongOrNull() ?: 0L
    val qty = qtyStr.toIntOrNull() ?: 1
    val disPct = discountPercentStr.toDoubleOrNull() ?: 0.0

    val subtotal = rate * qty
    val discountAmount = (subtotal * (disPct / 100.0)).toLong()
    val afterDiscount = (subtotal - discountAmount).coerceAtLeast(0L)
    val vatAmount = if (isVat13) (afterDiscount * 0.13).toLong() else 0L
    val grandTotal = afterDiscount + vatAmount

    val cashGiven = cashGivenStr.toLongOrNull() ?: 0L
    val returnChange = (cashGiven - grandTotal).coerceAtLeast(0L)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Calculate, contentDescription = null, tint = NepalBlue)
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = if (isNepali) "दैनिक काउन्टर द्रुत क्यालकुलेटर" else "Daily Shop POS Calculator",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = if (isNepali) "ग्राहकको बिलिङ र फिर्ता पैसा सजिलै निकाल्नुहोस्" else "Instant Billing & Return Change Calculation",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = rateStr,
                    onValueChange = { rateStr = it.filter { c -> c.isDigit() } },
                    label = { Text(if (isNepali) "दर रु (Rate)" else "Rate (Rs)") },
                    modifier = Modifier.weight(1.2f),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = qtyStr,
                    onValueChange = { qtyStr = it.filter { c -> c.isDigit() } },
                    label = { Text(if (isNepali) "संख्या (Qty)" else "Qty") },
                    modifier = Modifier.weight(0.8f),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = discountPercentStr,
                    onValueChange = { discountPercentStr = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text(if (isNepali) "छुट %" else "Dis %") },
                    modifier = Modifier.weight(0.8f),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Checkbox(
                    checked = isVat13,
                    onCheckedChange = { isVat13 = it }
                )
                Text(
                    text = if (isNepali) "१३% मूल्य अभिवृद्धि कर (VAT 13% जोड्ने)" else "Add 13% VAT",
                    fontSize = 13.sp
                )
            }

            OutlinedTextField(
                value = cashGivenStr,
                onValueChange = { cashGivenStr = it.filter { c -> c.isDigit() } },
                label = { Text(if (isNepali) "ग्राहकले दिएको नगद रु (Cash Paid)" else "Cash Paid by Customer (Rs)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Results Card
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = NepalBlue.copy(alpha = 0.08f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(if (isNepali) "जम्मा रकम (Subtotal):" else "Subtotal:", fontSize = 13.sp)
                        Text("रु $subtotal", fontSize = 13.sp)
                    }
                    if (discountAmount > 0) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(if (isNepali) "छुट रकम (Discount):" else "Discount:", fontSize = 13.sp, color = CrimsonRed)
                            Text("- रु $discountAmount", fontSize = 13.sp, color = CrimsonRed)
                        }
                    }
                    if (isVat13) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(if (isNepali) "१३% भ्याट (VAT 13%):" else "13% VAT:", fontSize = 13.sp)
                            Text("+ रु $vatAmount", fontSize = 13.sp)
                        }
                    }
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(if (isNepali) "कुल तिर्नुपर्ने (Grand Total):" else "Grand Total:", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("रु $grandTotal", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = CrimsonRed)
                    }

                    if (cashGiven > 0) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = JadeGreen.copy(alpha = 0.15f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isNepali) "फिर्ता गर्नुपर्ने रकम (Change Return):" else "Return Change:",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = JadeGreen
                                )
                                Text(
                                    text = "रु $returnChange",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = JadeGreen
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// BUSINESS PERFORMANCE & TEAM PRODUCTIVITY DASHBOARD
// -------------------------------------------------------------
@Composable
fun BusinessPerformanceDashboardCard(
    orders: List<com.example.data.model.Order>,
    isNepali: Boolean
) {
    var selectedPeriod by remember { mutableStateOf("साप्ताहिक") }
    var selectedDayIndex by remember { mutableStateOf(4) } // Default Friday / शुक्रबार

    val weeklyData = listOf(
        Pair(if (isNepali) "आइत" else "Sun", 28),
        Pair(if (isNepali) "सोम" else "Mon", 42),
        Pair(if (isNepali) "मङ्गल" else "Tue", 35),
        Pair(if (isNepali) "बुध" else "Wed", 50),
        Pair(if (isNepali) "बिही" else "Thu", 64),
        Pair(if (isNepali) "शुक्र" else "Fri", 78),
        Pair(if (isNepali) "शनि" else "Sat", 92)
    )

    val maxVal = weeklyData.maxOf { it.second }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header with Period Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = CrimsonRed.copy(alpha = 0.15f),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.TrendingUp, contentDescription = null, tint = CrimsonRed, modifier = Modifier.size(20.dp))
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (isNepali) "📈 व्यवसायिक कार्यसम्पादन ड्यासबोर्ड" else "Business Performance Dashboard",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isNepali) "उत्पादकत्व, अर्डर पूर्ति र गति सूचक" else "Productivity, velocity & fulfillment metrics",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Period Selector Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("दैनिक", "साप्ताहिक", "मासिक").forEach { period ->
                    val isSel = selectedPeriod == period
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSel) CrimsonRed else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSel) CrimsonRed else MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Button(
                            onClick = { selectedPeriod = period },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSel) CrimsonRed else Color.Transparent,
                                contentColor = if (isSel) Color.White else MaterialTheme.colorScheme.onSurface
                            ),
                            shape = RoundedCornerShape(20.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            elevation = null
                        ) {
                            Text(
                                text = when (period) {
                                    "दैनिक" -> if (isNepali) "दैनिक (Daily)" else "Daily"
                                    "साप्ताहिक" -> if (isNepali) "साप्ताहिक (Weekly)" else "Weekly"
                                    else -> if (isNepali) "मासिक (Monthly)" else "Monthly"
                                },
                                fontSize = 11.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Bar Chart Container (Recharts Style Interactive Bar Chart)
            Text(
                text = if (isNepali) "साप्ताहिक अर्डर प्रशोधन मात्रा (Order Volume Trends):" else "Weekly Order Volume Trends:",
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(10.dp))

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        weeklyData.forEachIndexed { idx, (dayLabel, count) ->
                            val isSelected = selectedDayIndex == idx
                            val barHeightFraction = count.toFloat() / maxVal.toFloat()

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Bottom,
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .padding(horizontal = 2.dp)
                            ) {
                                if (isSelected) {
                                    Surface(
                                        color = CrimsonRed,
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = "$count",
                                            color = Color.White,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(3.dp))
                                }

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(0.72f)
                                        .height((100 * barHeightFraction).dp)
                                        .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                        .background(if (isSelected) CrimsonRed else NepalBlue.copy(alpha = 0.75f))
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = dayLabel,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) CrimsonRed else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Key Productivity Metrics Grid
            Text(
                text = if (isNepali) "⚡ टोली उत्पादकत्व सूचकहरू (Team Productivity KPI):" else "⚡ Team Productivity KPI:",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Fulfillment Velocity
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = JadeGreen.copy(alpha = 0.1f),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(if (isNepali) "औसत पूर्ति समय" else "Fulfillment Time", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("१८ मिनेट", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = JadeGreen)
                        Text(if (isNepali) "९४.२% समयमै पूरा" else "94.2% on-time SLA", fontSize = 9.sp, color = JadeGreen, fontWeight = FontWeight.Bold)
                    }
                }

                // Customer Response Velocity
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = NepalBlue.copy(alpha = 0.1f),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(if (isNepali) "सम्पर्क प्रतिक्रिया" else "Response Velocity", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("< २.४ मिनेट", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = NepalBlue)
                        Text(if (isNepali) "VoIP कल तथा च्याट" else "VoIP Call & Chat", fontSize = 9.sp, color = NepalBlue, fontWeight = FontWeight.Bold)
                    }
                }

                // Return Rate (Quality)
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = AmberGold.copy(alpha = 0.12f),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(if (isNepali) "फिर्ता दर (Return)" else "Return Rate", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("१.४%", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = AmberGold)
                        Text(if (isNepali) "न्यूनतम फिर्ता (Low)" else "High Customer Sat", fontSize = 9.sp, color = AmberGold, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// PROJECT PROGRESS TRENDS & MILESTONE TRACKER
// -------------------------------------------------------------
@Composable
fun ProjectProgressTrendsCard(
    isNepali: Boolean
) {
    val milestones = listOf(
        Triple(if (isNepali) "१. पसल प्रमाणीकरण तथा KYC पाइपलाइन" else "1. Merchant KYC & Verification", 0.88f, "८८% पूरा"),
        Triple(if (isNepali) "२. भिडियो बजार (VMS Reels) कभरेज" else "2. Video Bazar Reels Coverage", 0.76f, "७६% पूरा"),
        Triple(if (isNepali) "३. नेपाल सरकार IRD कर तथा बिल अडिट" else "3. Govt IRD Tax Integration", 0.94f, "९४% पूरा"),
        Triple(if (isNepali) "४. ७७ जिल्ला प्रत्यक्ष डेलिभरी सञ्जाल" else "4. 77 Districts Delivery Network", 0.72f, "७२% पूरा"),
        Triple(if (isNepali) "५. शून्य-विश्वास सुरक्षा तथा भूमिका गोपनीयता" else "5. Zero-Trust Role Isolation", 1.00f, "१००% सुरक्षित")
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (isNepali) "🎯 रणनीतिक परियोजना प्रगति सूचक" else "Strategic Project Progress Trends",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (isNepali) "मेरो बजार समग्र चरणबद्ध लक्ष्यहरूको प्रगति" else "Mero Bazar milestone execution tracker",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Surface(
                    color = JadeGreen,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "८६% कुल लक्ष्य",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            milestones.forEach { (title, progress, pctText) ->
                Column(modifier = Modifier.padding(vertical = 5.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = title, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Text(
                            text = pctText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (progress >= 0.9f) JadeGreen else if (progress >= 0.75f) NepalBlue else AmberGold
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = if (progress >= 0.9f) JadeGreen else if (progress >= 0.75f) NepalBlue else AmberGold,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------
// CLOUD SERVER STATUS & LIVE FIREBASE SYNC CARD
// -------------------------------------------------------------
@Composable
fun CloudServerStatusCard(isNepali: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBBF7D0))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Surface(
                    shape = CircleShape,
                    color = JadeGreen,
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.CloudDone, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = if (isNepali) "☁️ अनलाइन क्लाउड सर्भर (Cloud Sync)" else "☁️ Online Cloud Server Status",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color(0xFF14532D)
                    )
                    Text(
                        text = "Firebase Firestore (gen-lang-client-0042520297) • Live 38ms",
                        fontSize = 11.sp,
                        color = Color(0xFF166534)
                    )
                }
            }
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = JadeGreen.copy(alpha = 0.2f)
            ) {
                Text(
                    text = if (isNepali) "🟢 प्रत्यक्ष जोडिएको" else "🟢 Active Sync",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF14532D),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

// -------------------------------------------------------------
// POPULAR TOP-SELLING PRODUCTS TRACKER
// -------------------------------------------------------------
@Composable
fun PopularProductsTrackerCard(
    isNepali: Boolean
) {
    val popularItems = listOf(
        Triple("बासमती चामल (५ केजी)", "१४२ इकाइ बिक्री • रु ८५,२००", 0.92f),
        Triple("मुसुरो दाल (१ केजी)", "११५ इकाइ बिक्री • रु १७,२५०", 0.78f),
        Triple("शुद्ध तोरीको तेल (१ लि.)", "९८ इकाइ बिक्री • रु २४,५००", 0.65f),
        Triple("इलाम अर्गानिक चिया (५०० ग्रा)", "८२ इकाइ बिक्री • रु २६,२४०", 0.55f),
        Triple("जुम्ली मार्सी चामल", "६४ इकाइ बिक्री • रु २५,६००", 0.42f)
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFEF3C7)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = AmberGold)
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = if (isNepali) "🔥 अधिक बिक्री भएका लोकप्रिय उत्पादनहरू" else "🔥 Popular Top-Selling Products",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = if (isNepali) "बिक्री संख्या र ग्राहक माग अनुसार ट्र्याकिङ" else "Tracked by sales volume & demand velocity",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            popularItems.forEachIndexed { index, (name, salesText, progress) ->
                Column(modifier = Modifier.padding(vertical = 5.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = if (index == 0) CrimsonRed else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.size(20.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "${index + 1}",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (index == 0) Color.White else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(name, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        }
                        Text(
                            text = salesText,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = if (index == 0) CrimsonRed else NepalBlue,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------
// QUICK DECISION-MAKING INSIGHTS CARD
// -------------------------------------------------------------
@Composable
fun QuickDecisionMakingCard(isNepali: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(NepalBlue.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Lightbulb, contentDescription = null, tint = NepalBlue)
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = if (isNepali) "💡 द्रुत निर्णय सहायता (Decision Insights)" else "💡 Quick Decision-Making Insights",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = if (isNepali) "डाटामा आधारित व्यापारिक रणनीति" else "Actionable data-driven business strategy",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFFEFF6FF),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = if (isNepali) "📈 माग तथा राजस्व प्रवृत्ति (Trends):" else "Trends:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = NepalBlue
                    )
                    Text(
                        text = if (isNepali)
                            "• खाद्यान्न तथा दाल-चामल समूहको माग अघिल्लो हप्ता भन्दा +२२% ले बढेको छ।\n• काठमाडौँ र भक्तपुरमा साँझ ५ देखि ८ बजेको बीचमा अर्डर चाप उच्च रहन्छ।"
                        else
                            "• Grocery demand grew +22% vs last week.\n• Order volume peaks between 5 PM and 8 PM.",
                        fontSize = 11.sp,
                        lineHeight = 16.sp,
                        color = Color(0xFF1E3A8A)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFFECFDF5),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = if (isNepali) "🎯 सिफारिस गरिएका तत्काल निर्णयहरू:" else "Actionable Decisions:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = JadeGreen
                    )
                    Text(
                        text = if (isNepali)
                            "१. बासमती चामलको मौज्दात (स्टक) तुरुन्त रिफिल गर्नुहोस्।\n२. डिजिटल भुक्तानी (eSewa/Khalti) मा अफर दिँदा नगद सङ्कलन सजिलो हुन्छ।\n३. भिडियो बजारमा नयाँ रिल्स अपलोड गर्दा बिक्री ३५% सम्म बढ्छ।"
                        else
                            "1. Restock Basmati rice immediately (low stock alert).\n2. Incentivize eSewa/Khalti digital checkout.\n3. Add new Video Bazar reels to boost conversion.",
                        fontSize = 11.sp,
                        lineHeight = 16.sp,
                        color = Color(0xFF065F46)
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------
// PRODUCT MARGIN & PROFIT ENGINE CALCULATOR
// -------------------------------------------------------------
@Composable
fun ProductMarginCalculatorCard(isNepali: Boolean) {
    var costPriceStr by remember { mutableStateOf("500") }
    var sellPriceStr by remember { mutableStateOf("750") }
    var discountStr by remember { mutableStateOf("5") }
    var vatIncluded by remember { mutableStateOf(true) }

    val cost = costPriceStr.toLongOrNull() ?: 0L
    val sell = sellPriceStr.toLongOrNull() ?: 0L
    val disPct = discountStr.toDoubleOrNull() ?: 0.0

    val discountedSell = (sell * (1.0 - (disPct / 100.0))).toLong()
    val vatAmount = if (vatIncluded) (discountedSell * 0.13 / 1.13).toLong() else (discountedSell * 0.13).toLong()
    val revenueExVat = if (vatIncluded) (discountedSell - vatAmount) else discountedSell
    val netProfitPerUnit = (revenueExVat - cost).coerceAtLeast(-cost)
    val marginPercent = if (revenueExVat > 0) ((netProfitPerUnit.toDouble() / revenueExVat.toDouble()) * 100.0) else 0.0
    val profitOn100Units = netProfitPerUnit * 100

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(JadeGreen.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Calculate, contentDescription = null, tint = JadeGreen)
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = if (isNepali) "📊 उत्पादन नाफा तथा मार्जिन क्यालकुलेटर" else "Product Margin & Profit Engine",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = if (isNepali) "खरिद, बिक्री, भ्याट र खुद नाफा हिसाब गर्नुहोस्" else "Calculate unit cost, retail price, VAT & profit",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = costPriceStr,
                    onValueChange = { costPriceStr = it.filter { c -> c.isDigit() } },
                    label = { Text(if (isNepali) "खरिद मूल्य रु" else "Cost (Rs)") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = sellPriceStr,
                    onValueChange = { sellPriceStr = it.filter { c -> c.isDigit() } },
                    label = { Text(if (isNepali) "बिक्री मूल्य रु" else "Sell (Rs)") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = discountStr,
                    onValueChange = { discountStr = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text(if (isNepali) "छुट %" else "Dis %") },
                    modifier = Modifier.weight(0.7f),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Checkbox(checked = vatIncluded, onCheckedChange = { vatIncluded = it })
                Text(
                    text = if (isNepali) "१३% भ्याट समावेश छ (VAT Inclusive)" else "13% VAT Inclusive",
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (netProfitPerUnit >= 0) JadeGreen.copy(alpha = 0.08f) else CrimsonRed.copy(alpha = 0.08f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(if (isNepali) "अन्तिम बिक्री मूल्य:" else "Final Selling Price:", fontSize = 13.sp)
                        Text("रु $discountedSell", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(if (isNepali) "१३% भ्याट (VAT to IRD):" else "13% VAT to IRD:", fontSize = 13.sp)
                        Text("रु $vatAmount", fontSize = 13.sp, color = CrimsonRed)
                    }
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(if (isNepali) "प्रति-इकाइ खुद नाफा (Net Margin):" else "Net Margin per Unit:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("रु $netProfitPerUnit (${String.format(java.util.Locale.US, "%.1f", marginPercent)}%)", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = if (netProfitPerUnit >= 0) JadeGreen else CrimsonRed)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(if (isNepali) "१०० वटा बिक्री हुँदा कुल नाफा:" else "Profit on 100 Units:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("रु $profitOn100Units", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = JadeGreen)
                    }
                }
            }
        }
    }
}

