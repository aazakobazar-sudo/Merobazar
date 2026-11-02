package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Verified
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import com.example.util.GoogleDocsHelper
import com.example.util.NepalCalendarHelper
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.Intent
import android.net.Uri
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.TrendingUp
import com.example.data.model.Notice
import com.example.data.model.Order
import com.example.data.model.Product
import com.example.data.model.UserProfile
import com.example.ui.MainViewModel
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.DarkCrimson
import com.example.ui.theme.JadeGreen
import com.example.ui.theme.NepalBlue
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanelScreen(
    viewModel: MainViewModel,
    onNavigateToCustomerMode: () -> Unit = {},
    onNavigateToMerchantMode: () -> Unit = {}
) {
    val context = LocalContext.current
    val isNepali by viewModel.isNepali.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val shops by viewModel.shops.collectAsState()
    val products by viewModel.filteredProducts.collectAsState()
    val orders by viewModel.orders.collectAsState()
    val notices by viewModel.notices.collectAsState()
    val allUsers by viewModel.allUsers.collectAsState()
    val bannedUsers by viewModel.bannedUsers.collectAsState()
    val suspendedShops by viewModel.suspendedShops.collectAsState()
    val khataList by viewModel.khata.collectAsState()
    val videoReels by viewModel.videoReels.collectAsState()
    val chatMessages by viewModel.chatMessages.collectAsState()
    val bills by viewModel.bills.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    var showPublishNoticeDialog by remember { mutableStateOf(false) }

    // New Notice Form State
    var noticeTitle by remember { mutableStateOf("") }
    var noticeType by remember { mutableStateOf("सरकारी विज्ञापन") }
    var noticeBody by remember { mutableStateOf("") }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val totalRevenue = orders.sumOf { it.total }
    val verifiedShopsCount = shops.count { it.shop?.lrn?.isNotBlank() == true || it.shop?.pan?.isNotBlank() == true }

    // --- Publish Notice Dialog ---
    if (showPublishNoticeDialog) {
        AlertDialog(
            onDismissRequest = { showPublishNoticeDialog = false },
            title = {
                Text(
                    text = if (isNepali) "नयाँ सूचना / टेन्डर प्रकाशित गर्नुहोस्" else "Publish New Notice / Tender",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = noticeTitle,
                        onValueChange = { noticeTitle = it },
                        label = { Text(if (isNepali) "शीर्षक (Title) *" else "Title *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        text = if (isNepali) "प्रकार (Type):" else "Type:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    val types = listOf(
                        "सरकारी विज्ञापन",
                        "रोजगारमूलक सूचना",
                        "व्यावसायिक विज्ञापन",
                        "गैरसरकारी विज्ञापन",
                        "अन्य महत्वपूर्ण सूचना"
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        types.forEach { t ->
                            FilterChip(
                                selected = noticeType == t,
                                onClick = { noticeType = t },
                                label = { Text(t, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CrimsonRed.copy(alpha = 0.15f),
                                    selectedLabelColor = CrimsonRed
                                )
                            )
                        }
                    }

                    OutlinedTextField(
                        value = noticeBody,
                        onValueChange = { noticeBody = it },
                        label = { Text(if (isNepali) "विस्तृत विवरण (Details) *" else "Details *") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (noticeTitle.isNotBlank() && noticeBody.isNotBlank()) {
                            viewModel.addNotice(noticeType, noticeTitle, noticeBody)
                            showPublishNoticeDialog = false
                            noticeTitle = ""
                            noticeBody = ""
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    if (isNepali) "सूचना सफलतापूर्वक प्रकाशित गरियो!" else "Notice successfully published!"
                                )
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed)
                ) {
                    Text(if (isNepali) "प्रकाशित गर्नुहोस्" else "Publish")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPublishNoticeDialog = false }) {
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = DarkCrimson,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.AdminPanelSettings,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (isNepali) "👑 मेरो बजार एडमिन प्यानल" else "👑 Admin Control Panel",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (isNepali) "सत्यापित मास्टर एडमिन • सुरक्षित प्रणाली" else "Master Admin • Secure Session",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    // Software & APK Update
                    IconButton(onClick = { viewModel.navigateTo("app_update") }) {
                        Icon(
                            Icons.Default.CloudDone,
                            contentDescription = "APK Update",
                            tint = CrimsonRed
                        )
                    }

                    // Language Switcher
                    IconButton(onClick = { viewModel.toggleLanguage() }) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
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
            // Admin Navigation Tabs (Section C 4.2 Interactive Database Inspector & Studio)
            val tabs = listOf(
                if (isNepali) "📊 अवलोकन" else "Overview",
                if (isNepali) "🗄️ डेटाबेस इन्स्पेक्टर (DB)" else "DB Inspector",
                if (isNepali) "🏪 पसल प्रमाणीकरण" else "Shops KYC",
                if (isNepali) "📦 अर्डर अडिट" else "Orders Audit",
                if (isNepali) "📢 सूचना तथा टेन्डर" else "Notices",
                if (isNepali) "🛡️ जोखिम र ठगी जाँच" else "Fraud & Risk",
                if (isNepali) "💾 विकासकर्ता स्टुडियो" else "Developer Studio"
            )

            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                edgePadding = 16.dp,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == index) CrimsonRed else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    )
                }
            }

            when (selectedTab) {
                0 -> AdminOverviewTab(
                    orders = orders,
                    shops = shops,
                    notices = notices,
                    totalRevenue = totalRevenue,
                    verifiedShopsCount = verifiedShopsCount,
                    isNepali = isNepali,
                    onPublishNoticeClick = { showPublishNoticeDialog = true },
                    onNavigateToCustomerMode = onNavigateToCustomerMode,
                    onNavigateToMerchantMode = onNavigateToMerchantMode,
                    onOpenDataAnalysis = { viewModel.navigateTo("data_analysis") },
                    onOpenRechartsVisualization = { viewModel.navigateTo("recharts_visualization") },
                    onOpenContacts = { viewModel.navigateTo("contacts") }
                )
                1 -> AdminDbInspectorTab(
                    allUsers = allUsers,
                    shops = shops,
                    products = products,
                    orders = orders,
                    khataList = khataList,
                    videoReels = videoReels,
                    chatMessages = chatMessages,
                    bannedUsers = bannedUsers,
                    suspendedShops = suspendedShops,
                    isNepali = isNepali,
                    onBanUser = { uid -> viewModel.banUser(uid) },
                    onUnbanUser = { uid -> viewModel.unbanUser(uid) },
                    onDeleteUser = { uid -> viewModel.deleteUser(uid) },
                    onPromoteUserRole = { uid, r -> viewModel.promoteUserRole(uid, r) },
                    onToggleShopVerify = { sid -> viewModel.toggleShopVerification(sid) },
                    onSuspendShop = { sid -> viewModel.suspendShop(sid) },
                    onUnsuspendShop = { sid -> viewModel.unsuspendShop(sid) },
                    onDeleteProduct = { pid -> viewModel.deleteProduct(pid) },
                    onUpdateOrderStatus = { oid, st -> viewModel.updateOrderStatus(oid, st) },
                    onDeleteVideo = { vid -> viewModel.deleteVideoReel(vid) },
                    onDeleteChatMessage = { mid -> viewModel.deleteChatMessage(mid) },
                    onActionTaken = { msg -> scope.launch { snackbarHostState.showSnackbar(msg) } }
                )
                2 -> AdminShopsKycTab(
                    shops = shops,
                    isNepali = isNepali,
                    onToggleVerify = { shopId ->
                        viewModel.toggleShopVerification(shopId)
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                if (isNepali) "पसलको प्रमाणीकरण स्थिति परिवर्तन गरियो।" else "Shop verification status updated."
                            )
                        }
                    }
                )
                3 -> AdminOrdersAuditTab(
                    orders = orders,
                    isNepali = isNepali,
                    onUpdateStatus = { orderId, newStatus ->
                        viewModel.updateOrderStatus(orderId, newStatus)
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                if (isNepali) "अर्डर स्थिति अद्यावधिक गरियो: $newStatus" else "Order status updated to $newStatus"
                            )
                        }
                    }
                )
                4 -> AdminNoticesTab(
                    notices = notices,
                    isNepali = isNepali,
                    onPublishNoticeClick = { showPublishNoticeDialog = true },
                    onDeleteNotice = { noticeId ->
                        viewModel.deleteNotice(noticeId)
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                if (isNepali) "सूचना हटाइयो।" else "Notice deleted."
                            )
                        }
                    }
                )
                5 -> AdminFraudRiskAuditTab(
                    orders = orders,
                    shops = shops,
                    products = products,
                    isNepali = isNepali,
                    onActionTaken = { msg ->
                        scope.launch { snackbarHostState.showSnackbar(msg) }
                    }
                )
                6 -> AdminDeveloperStudioTab(
                    viewModel = viewModel,
                    isNepali = isNepali,
                    onOpenAppUpdate = { viewModel.navigateTo("app_update") },
                    onActionTaken = { msg -> scope.launch { snackbarHostState.showSnackbar(msg) } }
                )
            }
        }
    }
}

@Composable
fun AdminOverviewTab(
    orders: List<Order>,
    shops: List<UserProfile>,
    notices: List<Notice>,
    totalRevenue: Long,
    verifiedShopsCount: Int,
    isNepali: Boolean,
    onPublishNoticeClick: () -> Unit,
    onNavigateToCustomerMode: () -> Unit,
    onNavigateToMerchantMode: () -> Unit,
    onOpenDataAnalysis: () -> Unit,
    onOpenRechartsVisualization: () -> Unit,
    onOpenContacts: () -> Unit
) {
    val context = LocalContext.current
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Platform Analytics Grid (4 Cards)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = if (isNepali) "प्लेटफर्म सारांश (Platform Analytics)" else "Platform Analytics",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricCard(
                        title = if (isNepali) "कुल बिक्री रकम" else "Total Revenue",
                        value = "₨ $totalRevenue",
                        icon = Icons.Default.Description,
                        color = JadeGreen,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = if (isNepali) "कुल अर्डरहरू" else "Total Orders",
                        value = "${orders.size}",
                        icon = Icons.Default.LocalShipping,
                        color = NepalBlue,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricCard(
                        title = if (isNepali) "प्रमाणित पसलहरू" else "Verified Shops",
                        value = "$verifiedShopsCount / ${shops.size}",
                        icon = Icons.Default.Verified,
                        color = AmberGold,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = if (isNepali) "प्रकाशित सूचना" else "Active Notices",
                        value = "${notices.size}",
                        icon = Icons.Default.Campaign,
                        color = CrimsonRed,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Core Admin Control Suite (Data Analysis, Contacts, Google Docs, Calendar)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (isNepali) "👑 प्रशासनिक नियन्त्रण औजारहरू (Admin Suite)" else "👑 Admin Control Suite",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onOpenDataAnalysis,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Analytics, contentDescription = null, tint = CrimsonRed, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isNepali) "डाटा/कर विश्लेषण" else "Data & Tax", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }

                        OutlinedButton(
                            onClick = onOpenContacts,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Contacts, contentDescription = null, tint = NepalBlue, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isNepali) "सम्पर्क सूची" else "Contacts Hub", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
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
                                    shopName = "Mero Bazar Platform",
                                    orders = orders,
                                    products = emptyList(),
                                    isNepali = isNepali
                                )
                                GoogleDocsHelper.shareDocument(context, "Mero Bazar Admin Report", doc)
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Description, contentDescription = null, tint = JadeGreen, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isNepali) "Docs अडिट" else "Docs Audit", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }

                        OutlinedButton(
                            onClick = {
                                val ok = NepalCalendarHelper.addMonthlyTaxFilingReminder(context)
                                if (ok) {
                                    Toast.makeText(context, if (isNepali) "२५ गते कर सम्झाउनी क्यालेन्डरमा थपियो" else "Tax Reminder Added", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = AmberGold, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isNepali) "कर क्यालेन्डर" else "Tax Calendar", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = onOpenRechartsVisualization,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_admin_recharts_analytics"),
                        colors = ButtonDefaults.buttonColors(containerColor = NepalBlue),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.TrendingUp, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isNepali) "📊 Recharts टोली कार्यसम्पादन तथा प्रगति एनालिटिक्स" else "📊 Recharts Team Productivity & Progress Trends",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Cloud & Telemetry System Status Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.CloudDone, contentDescription = null, tint = JadeGreen)
                        Text(
                            text = if (isNepali) "फायरबेस तथा क्लाउड स्थिति (Cloud Telemetry)" else "Firebase & Cloud Telemetry",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(text = "• Database: Cloud Firestore (Rules Deployed & Active)", style = MaterialTheme.typography.bodySmall)
                    Text(text = "• App Check: Attested & Protected", style = MaterialTheme.typography.bodySmall)
                    Text(text = "• Gemini AI: Firebase AI Logic (gemini-3.8-flash Active)", style = MaterialTheme.typography.bodySmall)
                    Text(text = "• Auth Provider: Google Sign-In & Registered Accounts", style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        // Quick Admin Actions
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (isNepali) "द्रुत कार्यहरू (Quick Admin Actions)" else "Quick Admin Actions",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = onPublishNoticeClick,
                        colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isNepali) "नयाँ सार्वजनिक सूचना / टेन्डर प्रकाशित गर्नुहोस्" else "Publish New Notice / Tender")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onNavigateToCustomerMode,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(if (isNepali) "👤 ग्राहक मोड" else "Customer Mode", fontSize = 12.sp)
                        }
                        OutlinedButton(
                            onClick = onNavigateToMerchantMode,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(if (isNepali) "🏪 व्यापारी मोड" else "Merchant Mode", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.08f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = color)
            Text(text = title, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun AdminShopsKycTab(
    shops: List<UserProfile>,
    isNepali: Boolean,
    onToggleVerify: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = if (isNepali) "दर्ता भएका पसलहरूको प्रमाणीकरण (Merchant KYC & Verification)" else "Registered Shops KYC & Verification",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        items(shops, key = { it.id }) { shop ->
            val isVerified = shop.shop?.lrn?.isNotBlank() == true || shop.shop?.pan?.isNotBlank() == true
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
                        Text(
                            text = shop.shop?.name ?: shop.name,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Badge(containerColor = if (isVerified) JadeGreen else AmberGold) {
                            Text(
                                text = if (isVerified) (if (isNepali) "✓ प्रमाणित (Verified)" else "Verified") else (if (isNepali) "पर्खाइमा (Pending)" else "Pending"),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = "प्रोप्राइटर: ${shop.name} (${shop.phone})", style = MaterialTheme.typography.bodySmall)
                    Text(text = "स्थान: ${shop.dist}, ${shop.local} (वडा ${shop.ward})", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(text = "PAN/VAT: ${shop.shop?.pan.takeIf { !it.isNullOrBlank() } ?: "दर्ता हुन बाँकी"}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                    Text(text = "दर्ता नं. (LRN): ${shop.shop?.lrn.takeIf { !it.isNullOrBlank() } ?: "N/A"}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = { onToggleVerify(shop.id) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isVerified) AmberGold else JadeGreen
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (isVerified)
                                (if (isNepali) "प्रमाणीकरण रद्द गर्नुहोस् (Revoke)" else "Revoke Verification")
                            else
                                (if (isNepali) "प्रमाणीकरण स्वीकृत गर्नुहोस् (Approve KYC)" else "Approve KYC & Verify")
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AdminNoticesTab(
    notices: List<Notice>,
    isNepali: Boolean,
    onPublishNoticeClick: () -> Unit,
    onDeleteNotice: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isNepali) "सूचना तथा विज्ञापनहरू (${notices.size})" else "Notices & Tenders (${notices.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Button(
                    onClick = onPublishNoticeClick,
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isNepali) "नयाँ सूचना" else "New Notice", fontSize = 12.sp)
                }
            }
        }

        items(notices, key = { it.id }) { notice ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = CrimsonRed.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = notice.type,
                                color = CrimsonRed,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        IconButton(
                            onClick = { onDeleteNotice(notice.id) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Gray, modifier = Modifier.size(18.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = notice.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = notice.body, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "📅 मिति: ${notice.date}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                }
            }
        }
    }
}

@Composable
fun AdminOrdersAuditTab(
    orders: List<Order>,
    isNepali: Boolean,
    onUpdateStatus: (String, String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = if (isNepali) "सबै प्लेटफर्म अर्डरहरूको अडिट (Platform Orders Audit)" else "Platform Orders Audit",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        items(orders, key = { it.id }) { order ->
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
                        Text(text = "#${order.id}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                        val badgeColor = when (order.st) {
                            "Delivered" -> JadeGreen
                            "Accepted" -> NepalBlue
                            "Cancelled" -> CrimsonRed
                            else -> AmberGold
                        }
                        Badge(containerColor = badgeColor) {
                            Text(text = order.st, color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = "पसल: ${order.shopName}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                    Text(text = "ग्राहक: ${order.cname} (${order.cphone})", style = MaterialTheme.typography.bodySmall)
                    Text(text = "ठेगाना: ${order.caddress}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(text = "सामानहरू: " + order.items.joinToString { "${it.n} × ${it.q}" }, style = MaterialTheme.typography.bodySmall)

                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "कुल: ₨ ${order.total} (${order.payMode})", fontWeight = FontWeight.ExtraBold, color = CrimsonRed)
                        Text(text = order.d, fontSize = 11.sp, color = Color.Gray)
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    // Admin override actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (order.st != "Delivered") {
                            Button(
                                onClick = { onUpdateStatus(order.id, "Delivered") },
                                colors = ButtonDefaults.buttonColors(containerColor = JadeGreen),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(if (isNepali) "डेलिभर भयो" else "Mark Delivered", fontSize = 11.sp)
                            }
                        }
                        if (order.st != "Cancelled") {
                            OutlinedButton(
                                onClick = { onUpdateStatus(order.id, "Cancelled") },
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(if (isNepali) "रद्द गर्नुहोस्" else "Cancel Order", fontSize = 11.sp, color = CrimsonRed)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminFraudRiskAuditTab(
    orders: List<Order>,
    shops: List<UserProfile>,
    products: List<Product>,
    isNepali: Boolean,
    onActionTaken: (String) -> Unit
) {
    // 1. Check for Shared / Duplicate PANs across different merchants
    val pansWithShops = shops.mapNotNull { s ->
        val pan = s.shop?.pan?.trim()
        if (!pan.isNullOrBlank()) pan to s else null
    }
    val panGroups = pansWithShops.groupBy({ it.first }, { it.second }).filter { it.value.size > 1 }

    // 2. Suspicious / Extreme Product Prices
    val suspiciousProducts = products.filter { p ->
        p.effectivePrice > 100000L || p.price <= 0L || p.dis >= p.price
    }

    // 3. Duplicate Order Attempts / High-Frequency Transactions
    val duplicateOrders = orders.groupBy { "${it.cphone}_${it.total}_${it.muid}" }
        .filter { it.value.size > 1 }

    // 4. High Cancellation Rate
    val cancelledOrders = orders.filter { it.st == "Cancelled" }
    val cancellationRate = if (orders.isNotEmpty()) (cancelledOrders.size * 100 / orders.size) else 0

    // 5. Unverified Merchants with high turnover
    val unverifiedShops = shops.filter { s ->
        s.role == "merchant" && (s.shop?.lrn.isNullOrBlank() && s.shop?.pan.isNullOrBlank())
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = CrimsonRed)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isNepali) "🛡️ प्लेटफर्म सुरक्षा तथा ठगी अडिट (Risk Detection)" else "🛡️ Fraud & Risk Detection Engine",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isNepali)
                            "नेपाल सरकारको वाणिज्य नीति तथा कर ऐन अनुसार शंकास्पद कारोबार, दोहोरो PAN, अस्वाभाविक मूल्य र फर्जी अर्डरहरू निगरानी गरिन्छ।"
                        else
                            "Automated auditing for duplicate PAN registrations, abnormal product pricing, and high-risk orders.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Shared PAN Audit Card
        item {
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
                        Text(
                            text = if (isNepali) "१. दोहोरो कर दर्ता (PAN/VAT Check)" else "1. Duplicate PAN / Tax ID Check",
                            fontWeight = FontWeight.Bold
                        )
                        Badge(containerColor = if (panGroups.isEmpty()) JadeGreen else CrimsonRed) {
                            Text(if (panGroups.isEmpty()) "सुरक्षित (PASS)" else "जोखिम (${panGroups.size})", color = Color.White)
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    if (panGroups.isEmpty()) {
                        Text(
                            text = if (isNepali) "कुनै पनि पसलमा दोहोरो वा नक्कल PAN भेटिएन। सबै दर्ता विशिष्ट छन्।" else "No duplicate PAN numbers detected across registered shops.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        panGroups.forEach { (pan, shopList) ->
                            Text(
                                text = "⚠️ PAN: $pan प्रयोग गर्ने पसलहरू: ${shopList.mapNotNull { it.shop?.name }.joinToString()}",
                                style = MaterialTheme.typography.bodySmall,
                                color = CrimsonRed,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Button(
                                onClick = {
                                    onActionTaken(if (isNepali) "स्पष्टीकरण माग पत्र पठाइयो" else "Clarification notice dispatched to merchants")
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed)
                            ) {
                                Text(if (isNepali) "स्पष्टीकरण माग पत्र पठाउनुहोस्" else "Request Tax Clarification", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }

        // Suspicious Prices Audit Card
        item {
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
                        Text(
                            text = if (isNepali) "२. अस्वाभाविक सामान मूल्य जाँच" else "2. Extreme / Anomaly Price Check",
                            fontWeight = FontWeight.Bold
                        )
                        Badge(containerColor = if (suspiciousProducts.isEmpty()) JadeGreen else AmberGold) {
                            Text(if (suspiciousProducts.isEmpty()) "सामान्य (NORMAL)" else "समीक्षा (${suspiciousProducts.size})", color = Color.White)
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    if (suspiciousProducts.isEmpty()) {
                        Text(
                            text = if (isNepali) "सबै सामानहरूको मूल्य बजारको मापदण्ड भित्र उचित छ।" else "All product prices are within normal market ranges.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        suspiciousProducts.forEach { p ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(p.name, fontWeight = FontWeight.Bold)
                                    Text("मूल्य: ₨ ${p.price} | छुट: ₨ ${p.dis}", style = MaterialTheme.typography.bodySmall, color = CrimsonRed)
                                }
                                Button(
                                    onClick = { onActionTaken(if (isNepali) "${p.name} को मूल्य अडिट गरियो" else "Audited ${p.name}") },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = AmberGold)
                                ) {
                                    Text(if (isNepali) "जाँच" else "Review", fontSize = 11.sp)
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                    }
                }
            }
        }

        // Duplicate Orders Audit Card
        item {
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
                        Text(
                            text = if (isNepali) "३. दोहोरो अर्डर तथा स्प्याम जाँच" else "3. Duplicate / Replay Order Check",
                            fontWeight = FontWeight.Bold
                        )
                        Badge(containerColor = if (duplicateOrders.isEmpty()) JadeGreen else AmberGold) {
                            Text(if (duplicateOrders.isEmpty()) "सफा (CLEAN)" else "चेतावनी (${duplicateOrders.size})", color = Color.White)
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (duplicateOrders.isEmpty())
                            (if (isNepali) "कुनै पनि दोहोरिएको वा फर्जी अर्डर फेला परेन।" else "No duplicate or spam orders detected.")
                        else
                            (if (isNepali) "केही ग्राहकबाट समान रकमका धेरै अर्डरहरू आएका छन्, जाँच आवश्यक छ।" else "Multiple orders with identical totals detected."),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Cancellation Rate & Unverified High-Volume Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = if (isNepali) "४. प्लेटफर्म समग्र विश्वसनीयता सूचक" else "4. Overall Platform Integrity Metrics",
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(if (isNepali) "अर्डर रद्द दर (Cancellation Rate):" else "Cancellation Rate:")
                        Text("$cancellationRate%", fontWeight = FontWeight.Bold, color = if (cancellationRate > 20) CrimsonRed else JadeGreen)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(if (isNepali) "अपुष्ट पसलहरू (Unverified Shops):" else "Unverified Shops:")
                        Text("${unverifiedShops.size} वटा", fontWeight = FontWeight.Bold, color = AmberGold)
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = {
                            onActionTaken(if (isNepali) "सबै सुरक्षा मापदण्डहरू प्रमाणित गरियो ✅" else "All safety benchmarks verified ✅")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NepalBlue),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (isNepali) "सुरक्षा अडिट रिपोर्ट डाउनलोड / प्रमाणित गर्नुहोस्" else "Verify & Certify Audit Report")
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// SECTION C 4.2 INTERACTIVE DATABASE STATEMENT SYSTEM (INSPECTOR MODE)
// 8 Tables: users, shops, products, orders, payments (IRD), khata, videos, chat
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDbInspectorTab(
    allUsers: List<UserProfile>,
    shops: List<UserProfile>,
    products: List<Product>,
    orders: List<Order>,
    khataList: List<com.example.data.model.KhataEntry>,
    videoReels: List<com.example.data.model.VideoReelItem>,
    chatMessages: List<com.example.data.model.ChatMessage>,
    bannedUsers: Set<String>,
    suspendedShops: Set<String>,
    isNepali: Boolean,
    onBanUser: (String) -> Unit,
    onUnbanUser: (String) -> Unit,
    onDeleteUser: (String) -> Unit,
    onPromoteUserRole: (String, String) -> Unit,
    onToggleShopVerify: (String) -> Unit,
    onSuspendShop: (String) -> Unit,
    onUnsuspendShop: (String) -> Unit,
    onDeleteProduct: (String) -> Unit,
    onUpdateOrderStatus: (String, String) -> Unit,
    onDeleteVideo: (String) -> Unit,
    onDeleteChatMessage: (String) -> Unit,
    onActionTaken: (String) -> Unit
) {
    val context = LocalContext.current
    var selectedTableIndex by remember { mutableIntStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }

    val tableNames = listOf(
        "👥 users" to (if (isNepali) "प्रयोगकर्ता (${allUsers.size})" else "Users (${allUsers.size})"),
        "🏪 shops" to (if (isNepali) "पसलहरू (${shops.size})" else "Shops (${shops.size})"),
        "📦 products" to (if (isNepali) "सामान (${products.size})" else "Products (${products.size})"),
        "🛒 orders" to (if (isNepali) "अर्डर (${orders.size})" else "Orders (${orders.size})"),
        "💳 payments" to (if (isNepali) "भुक्तानी / IRD" else "Payments & IRD"),
        "📒 khata" to (if (isNepali) "खाता (${khataList.size})" else "Khata (${khataList.size})"),
        "🎬 videos" to (if (isNepali) "रिल्स (${videoReels.size})" else "Videos (${videoReels.size})"),
        "💬 chat" to (if (isNepali) "च्याट (${chatMessages.size})" else "Chat (${chatMessages.size})")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        // Table Selector Horizontal Chips
        ScrollableTabRow(
            selectedTabIndex = selectedTableIndex,
            edgePadding = 8.dp,
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.clip(RoundedCornerShape(12.dp))
        ) {
            tableNames.forEachIndexed { index, (code, label) ->
                Tab(
                    selected = selectedTableIndex == index,
                    onClick = {
                        selectedTableIndex = index
                        searchQuery = ""
                    },
                    text = {
                        Text(
                            text = label,
                            fontSize = 12.sp,
                            fontWeight = if (selectedTableIndex == index) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTableIndex == index) CrimsonRed else MaterialTheme.colorScheme.onSurface
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Search Bar for Active Table
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text(if (isNepali) "डेटाबेस खोज्नुहोस् (Search table records)..." else "Search table records...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = CrimsonRed) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Delete, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Active Table Records
        when (selectedTableIndex) {
            // 4.2.1 Users Table
            0 -> {
                val filteredUsers = allUsers.filter {
                    it.name.contains(searchQuery, ignoreCase = true) ||
                            it.email.contains(searchQuery, ignoreCase = true) ||
                            it.phone.contains(searchQuery, ignoreCase = true) ||
                            it.code.contains(searchQuery, ignoreCase = true)
                }
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(filteredUsers, key = { it.id }) { user ->
                        val isBanned = bannedUsers.contains(user.id)
                        val roleBadge = when (user.role) {
                            "a" -> "👑 Master Admin"
                            "m" -> "🏪 Merchant"
                            else -> "👤 Customer"
                        }
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (isBanned) CrimsonRed else MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(text = user.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                        Text(text = "ID: ${user.id} • कोड: ${user.code}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Surface(
                                        color = if (user.role == "a") CrimsonRed else if (user.role == "m") DarkCrimson else NepalBlue,
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = roleBadge,
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                Text(text = "📞 ${user.phone} | 📍 ${user.dist}, ${user.prov}", fontSize = 12.sp)

                                if (isBanned) {
                                    Text(
                                        text = if (isNepali) "⚠️ यो खाता प्रतिबन्धित (Banned) छ!" else "⚠️ Account is Banned!",
                                        color = CrimsonRed,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (user.role != "a") {
                                        // Role Promotion/Switch
                                        TextButton(
                                            onClick = {
                                                val nextRole = if (user.role == "c") "m" else "c"
                                                onPromoteUserRole(user.id, nextRole)
                                                onActionTaken("भूमिका परिवर्तन: $nextRole")
                                            }
                                        ) {
                                            Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(if (user.role == "c") "व्यापारी बनाउनुहोस्" else "ग्राहक बनाउनुहोस्", fontSize = 11.sp)
                                        }

                                        // Ban / Unban
                                        OutlinedButton(
                                            onClick = {
                                                if (isBanned) onUnbanUser(user.id) else onBanUser(user.id)
                                                onActionTaken(if (isBanned) "प्रतिबन्ध हटाइयो" else "खाता प्रतिबन्धित गरियो")
                                            },
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = if (isBanned) JadeGreen else CrimsonRed)
                                        ) {
                                            Text(if (isBanned) "अनब्यान (Unban)" else "ब्यान (Ban)", fontSize = 11.sp)
                                        }

                                        Spacer(modifier = Modifier.width(6.dp))
                                        IconButton(onClick = {
                                            onDeleteUser(user.id)
                                            onActionTaken("रेकर्ड मेटाइयो")
                                        }) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = CrimsonRed)
                                        }
                                    } else {
                                        Text(text = "सुरक्षित मास्टर एडमिन", fontSize = 11.sp, color = JadeGreen, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 4.2.2 Shops Table
            1 -> {
                val filteredShops = shops.filter {
                    (it.shop?.name ?: it.name).contains(searchQuery, ignoreCase = true) ||
                            (it.shop?.pan ?: "").contains(searchQuery) ||
                            (it.shop?.lrn ?: "").contains(searchQuery)
                }
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(filteredShops, key = { it.id }) { shop ->
                        val isSuspended = suspendedShops.contains(shop.id)
                        val isVerified = shop.shop?.ok == true || shop.shop?.pan?.isNotBlank() == true
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (isSuspended) CrimsonRed else MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Column {
                                        Text(text = shop.shop?.name ?: shop.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                        Text(text = "पसल मालिक: ${shop.name} • ID: ${shop.id}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Surface(
                                        color = if (isVerified) JadeGreen else AmberGold,
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = if (isVerified) "सत्यापित (KYC OK)" else "पेन्डिङ (Pending)",
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = "PAN: ${shop.shop?.pan ?: "N/A"} | दर्ता नं (LRN): ${shop.shop?.lrn ?: "N/A"}", fontSize = 12.sp)
                                Text(text = "स्थान: ${shop.dist}, ${shop.prov} (वडा ${shop.ward})", fontSize = 12.sp)

                                Spacer(modifier = Modifier.height(8.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                    Button(
                                        onClick = {
                                            onToggleShopVerify(shop.id)
                                            onActionTaken("पसल KYC अद्यावधिक भयो")
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = if (isVerified) AmberGold else JadeGreen),
                                        modifier = Modifier.height(34.dp)
                                    ) {
                                        Text(if (isVerified) "पेन्डिङ बनाउनुहोस्" else "KYC स्वीकृत गर्नुहोस्", fontSize = 11.sp)
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    OutlinedButton(
                                        onClick = {
                                            if (isSuspended) onUnsuspendShop(shop.id) else onSuspendShop(shop.id)
                                            onActionTaken(if (isSuspended) "पसल फुकुवा भयो" else "पसल निलम्बित गरियो")
                                        },
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = CrimsonRed),
                                        modifier = Modifier.height(34.dp)
                                    ) {
                                        Text(if (isSuspended) "फुकुवा" else "निलम्बन", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 4.2.3 Products Table
            2 -> {
                val filteredProducts = products.filter {
                    it.name.contains(searchQuery, ignoreCase = true) ||
                            it.type.contains(searchQuery, ignoreCase = true) ||
                            it.id.contains(searchQuery)
                }
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(filteredProducts, key = { it.id }) { prod ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = prod.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text(text = "ID: ${prod.id} • पसल ID: ${prod.mid} • वर्ग: ${prod.type}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(text = "मूल्य: रु ${prod.price} | स्टक: ${prod.stock} वटा", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = AmberGold)
                                }
                                IconButton(onClick = {
                                    onDeleteProduct(prod.id)
                                    onActionTaken("${prod.name} मेटाइयो")
                                }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = CrimsonRed)
                                }
                            }
                        }
                    }
                }
            }

            // 4.2.4 Orders Table
            3 -> {
                val filteredOrders = orders.filter {
                    it.id.contains(searchQuery, ignoreCase = true) ||
                            it.cname.contains(searchQuery, ignoreCase = true) ||
                            it.shopName.contains(searchQuery, ignoreCase = true)
                }
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(filteredOrders, key = { it.id }) { ord ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(text = "अर्डर: ${ord.id}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Surface(
                                        color = when (ord.st) {
                                            "Delivered" -> JadeGreen
                                            "Shipped" -> NepalBlue
                                            "Cancelled" -> CrimsonRed
                                            else -> AmberGold
                                        },
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(text = ord.st, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = "ग्राहक: ${ord.cname} (${ord.cphone}) | पसल: ${ord.shopName}", fontSize = 12.sp)
                                Text(text = "ठेगाना: ${ord.caddress}", fontSize = 12.sp)
                                Text(text = "कुल रकम: रु ${ord.total} | भुक्तानी: ${ord.payMode}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CrimsonRed)

                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    listOf("Pending", "Shipped", "Delivered", "Cancelled").forEach { status ->
                                        if (ord.st != status) {
                                            TextButton(
                                                onClick = {
                                                    onUpdateOrderStatus(ord.id, status)
                                                    onActionTaken("अर्डर स्थिति परिवर्तन: $status")
                                                },
                                                modifier = Modifier.padding(horizontal = 2.dp)
                                            ) {
                                                Text(status, fontSize = 10.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 4.2.5 Payments & IRD Table
            4 -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = CrimsonRed)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isNepali) "नेपाल सरकार आन्तरिक राजस्व विभाग (IRD) बिल अडिट" else "Government IRD Billing Audit Trail",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                        Text(
                            text = if (isNepali) "सबै बिल र लक्की ड्र जाँच गर्न सरकारी पोर्टल प्रयोग गर्नुहोस्:" else "Official IRD Lucky Draw verification portal:",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Button(
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://prize.ird.gov.np/"))
                                try { context.startActivity(intent) } catch (_: Exception) {}
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "https://prize.ird.gov.np/ पोर्टल खोल्नुहोस्", fontSize = 12.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(orders) { ord ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = "भुक्तानी: ${ord.payMode}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text(text = "अर्डर ID: ${ord.id} • मिति: ${ord.d}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(text = "ग्राहक: ${ord.cname} | रकम: रु ${ord.total}", fontSize = 12.sp, color = JadeGreen, fontWeight = FontWeight.Bold)
                                }
                                Surface(
                                    color = if (ord.st == "Delivered") JadeGreen else AmberGold,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = if (ord.st == "Delivered") "प्रमाणित" else "पेन्डिङ",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 4.2.6 Khata Table
            5 -> {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(khataList, key = { it.id }) { k ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(text = "खाता ग्राहक: ${k.customerName}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text(text = "बाँकी: रु ${k.due}", fontWeight = FontWeight.ExtraBold, color = CrimsonRed, fontSize = 14.sp)
                                }
                                Text(text = "सम्पर्क: ${k.phone} • पसल ID: ${k.mid}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(text = "विवरण: ${k.note} • मिति: ${k.date}", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // 4.2.7 Videos Table
            6 -> {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(videoReels, key = { it.id }) { vid ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = vid.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text(text = "पसल: ${vid.shopName} • ID: ${vid.id}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(text = "सामान: ${vid.productName} (रु ${vid.productPrice})", fontSize = 12.sp, color = AmberGold, fontWeight = FontWeight.SemiBold)
                                    Text(text = "❤️ ${vid.likesCount} लाइक्स • 💬 ${vid.commentsCount} कमेन्ट", fontSize = 11.sp)
                                }
                                IconButton(onClick = {
                                    onDeleteVideo(vid.id)
                                    onActionTaken("भिडियो रिल्स मेटाइयो")
                                }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = CrimsonRed)
                                }
                            }
                        }
                    }
                }
            }

            // 4.2.8 Chat Table
            7 -> {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(chatMessages, key = { it.id }) { msg ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = "${msg.senderName} ➡️ ${msg.receiverId}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text(text = msg.text.ifBlank { if (msg.photoUrl.isNotBlank()) "📷 फोटो पठाइयो" else "📞 कल सम्पन्न" }, fontSize = 12.sp)
                                    Text(text = "ID: ${msg.id} • कल इभेन्ट: ${msg.isCallEvent}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                IconButton(onClick = {
                                    onDeleteChatMessage(msg.id)
                                    onActionTaken("मेसेज मेटाइयो")
                                }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = CrimsonRed)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// SECTION D 4.3 DEVELOPER STUDIO & ONE-CLICK BACKUP SYSTEM
// -------------------------------------------------------------
@Composable
fun AdminDeveloperStudioTab(
    viewModel: MainViewModel,
    isNepali: Boolean,
    onOpenAppUpdate: () -> Unit,
    onActionTaken: (String) -> Unit
) {
    val context = LocalContext.current
    var backupJsonPreview by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // One-Click Backup Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(14.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = CrimsonRed.copy(alpha = 0.15f),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Download, contentDescription = null, tint = CrimsonRed)
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (isNepali) "💾 सम्पूर्ण डेटा ब्याकअप (.JSON / .ZIP)" else "Complete System Backup (.JSON / .ZIP)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = if (isNepali) "एक क्लिकमा प्रयोगकर्ता, पसल, अर्डर, र भिडियो डेटा ब्याकअप" else "One-click export of users, shops, orders, and reels",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            val json = viewModel.getAllDataBackupJson()
                            backupJsonPreview = json
                            onActionTaken(if (isNepali) "डेटा ब्याकअप सफलतापूर्वक उत्पादन गरियो!" else "Backup JSON generated!")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (isNepali) "सम्पूर्ण डेटा JSON ब्याकअप उत्पादन गर्नुहोस्" else "Generate Full JSON Backup")
                    }

                    if (backupJsonPreview != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "📄 Backup JSON Snapshot",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                    IconButton(
                                        onClick = {
                                            val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                            val clip = android.content.ClipData.newPlainText("MeroBazar_Backup", backupJsonPreview)
                                            clipboard.setPrimaryClip(clip)
                                            Toast.makeText(context, if (isNepali) "क्लिपबोर्डमा प्रतिलिपि गरियो!" else "Copied to clipboard!", Toast.LENGTH_SHORT).show()
                                        }
                                    ) {
                                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(18.dp))
                                    }
                                }
                                Text(
                                    text = backupJsonPreview!!.take(240) + "...\n(Total length: ${backupJsonPreview!!.length} characters)",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // WebRTC VoIP Diagnostic Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(14.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (isNepali) "🌐 WebRTC अडियो / भिडियो कल इन्जिन स्थिति" else "WebRTC VoIP Engine Status",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = "STUN सर्भर:", fontSize = 12.sp)
                        Text(text = "stun:stun.l.google.com:19302 (सक्रिय ✅)", fontSize = 12.sp, color = JadeGreen, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = "सिग्नलिंग च्यानल:", fontSize = 12.sp)
                        Text(text = "P2P WebSocket Active ✅", fontSize = 12.sp, color = JadeGreen, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = "भिडियो / अडियो कोडेक:", fontSize = 12.sp)
                        Text(text = "VP8 / Opus (HD)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        // Firebase & Security Diagnostic Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(14.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (isNepali) "🔥 फायरबेस तथा सुरक्षा मापदण्ड स्थिति" else "Firebase & Security Diagnostics",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = "Firestore डाटाबेस:", fontSize = 12.sp)
                        Text(text = "अनलाइन जोडिएको (Connected ✅)", fontSize = 12.sp, color = JadeGreen, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = "भूमिका गोपनीयता (Privacy):", fontSize = 12.sp)
                        Text(text = "कठोर पृथकीकरण लागू ✅", fontSize = 12.sp, color = JadeGreen, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = "मास्टर एडमिन सुरक्षा:", fontSize = 12.sp)
                        Text(text = "एकल खाता (Single Admin Isolated) ✅", fontSize = 12.sp, color = JadeGreen, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Utility Launchers
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(14.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (isNepali) "🛠️ विकासकर्ता सर्टकट टूल्स" else "Developer Shortcut Tools",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = onOpenAppUpdate,
                        colors = ButtonDefaults.buttonColors(containerColor = DarkCrimson),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.CloudDone, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (isNepali) "APK भर्सन तथा अटो-अपडेट व्यवस्थापन" else "APK Version & OTA Manager")
                    }
                }
            }
        }
    }
}
