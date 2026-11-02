package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import com.example.ui.MainViewModel
import com.example.ui.screens.AdminPanelScreen
import com.example.ui.screens.AiShoppingAssistantScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.CartScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MerchantDashboardScreen
import com.example.ui.screens.NoticesScreen
import com.example.ui.screens.OrderTrackingScreen
import com.example.ui.screens.PastOrdersScreen
import com.example.ui.screens.ProductDetailScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.ShopDetailScreen
import com.example.ui.screens.ShopsScreen
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.MeroBazarTheme

import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.Alignment
import com.example.ui.screens.ContactsScreen
import com.example.ui.screens.CustomerToolsScreen
import com.example.ui.screens.DataAnalysisScreen
import com.example.ui.screens.RechartsVisualizationScreen
import com.example.ui.screens.MessengerScreen
import com.example.ui.screens.AppUpdateScreen
import com.example.ui.screens.NepalMapScreen
import com.example.ui.screens.VideoBazarScreen
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.TextButton
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.platform.LocalContext

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // STEP 1: Mandatory App Check configuration with launch intent before any UI or Firebase operations
        configureAppCheck(this, intent)

        enableEdgeToEdge()

        setContent {
            MeroBazarTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MeroBazarApp(viewModel)
                }
            }
        }
    }
}

@Composable
fun MeroBazarApp(viewModel: MainViewModel) {
    val context = LocalContext.current
    val currentScreen by viewModel.currentScreen.collectAsState()
    val isNepali by viewModel.isNepali.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val cart by viewModel.cart.collectAsState()
    val updateInfo by viewModel.appUpdateInfo.collectAsState()
    val totalCartItems = cart.values.sum()
    val isMerchant = currentUser?.role == "m"
    val isAdmin = currentUser?.role == "a"

    // Automatic OTA Update Notification Dialog
    if (updateInfo.isUpdateAvailable) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissUpdateAlert() },
            title = {
                Text(
                    text = if (isNepali) "🚀 नयाँ अपडेट v${updateInfo.latestVersion} उपलब्ध छ!" else "New Update v${updateInfo.latestVersion} Available!",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "${updateInfo.releaseNotes}\n\n" + if (isNepali)
                        "अनलाइन जोडिएको छ। के तपाईं नयाँ APK सिधै डाउनलोड गरी इन्स्टल गर्न चाहनुहुन्छ?"
                    else
                        "Online connected. Do you want to download and install the latest APK?",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(updateInfo.githubReleaseUrl))
                        try {
                            context.startActivity(intent)
                        } catch (_: Exception) {}
                        viewModel.dismissUpdateAlert()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed)
                ) {
                    Text(if (isNepali) "अहिले डाउनलोड गर्नुहोस्" else "Download APK")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissUpdateAlert() }) {
                    Text(if (isNepali) "पछि गर्ने" else "Later")
                }
            }
        )
    }

    // Real-time Cloud VoIP Incoming Call Dialog
    val incomingCall by viewModel.incomingCallSession.collectAsState()
    if (incomingCall != null) {
        val call = incomingCall!!
        val isVideo = call.callType == "video"
        AlertDialog(
            onDismissRequest = { /* Require user response */ },
            containerColor = Color(0xFF0F172A),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        if (isVideo) Icons.Default.Videocam else Icons.Default.Call,
                        contentDescription = null,
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = if (isVideo) "भिडियो कल (Video Call)" else "अडियो कल (Audio Call)",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 18.sp
                    )
                }
            },
            text = {
                Column {
                    Text(
                        text = call.callerName,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp,
                        color = Color(0xFF38BDF8)
                    )
                    Text(
                        text = if (isNepali) "क्लाउड सर्भर मार्फत सिधै अनलाइन कल घण्टी बजिरहेको छ..." else "Incoming online call ringing via cloud server...",
                        color = Color.LightGray,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.answerIncomingCall(call.callId)
                        viewModel.navigateTo("messenger")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                ) {
                    Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(if (isNepali) "उठाउनुहोस् (Answer)" else "Answer")
                }
            },
            dismissButton = {
                Button(
                    onClick = { viewModel.declineIncomingCall(call.callId) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Icon(Icons.Default.CallEnd, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(if (isNepali) "काट्नुहोस् (Decline)" else "Decline")
                }
            }
        )
    }

    // Gatekeeper: If user is not authenticated, strictly show AuthScreen (Login / Register / Forgot Password)
    // Direct access to internal portal is strictly blocked until authenticated
    if (currentUser == null) {
        AuthScreen(
            viewModel = viewModel,
            onAuthSuccess = {
                val role = viewModel.currentUser.value?.role
                when (role) {
                    "m" -> viewModel.navigateTo("merchant_dashboard")
                    "a" -> viewModel.navigateTo("admin_dashboard")
                    else -> viewModel.navigateTo("home")
                }
            },
            onBack = { /* Strictly stay on auth until authenticated */ },
            onOpenNepalMap = { viewModel.navigateTo("nepal_map") }
        )
        return
    }

    // Back button handling
    BackHandler(enabled = currentScreen !in listOf("home", "merchant_dashboard", "admin_dashboard")) {
        viewModel.goBack()
    }

    val showBottomBar = currentScreen in listOf("home", "shops", "notices", "cart", "profile", "merchant_dashboard", "admin_dashboard", "past_orders", "contacts", "data_analysis")

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.testTag("main_bottom_nav")
                ) {
                    if (isAdmin) {
                        // Admin Navigation items
                        NavigationBarItem(
                            selected = currentScreen == "admin_dashboard",
                            onClick = { viewModel.navigateTo("admin_dashboard") },
                            icon = { Icon(Icons.Default.AdminPanelSettings, contentDescription = "Admin") },
                            label = { Text(if (isNepali) "एडमिन" else "Admin") },
                            colors = NavigationBarItemDefaults.colors(selectedIconColor = CrimsonRed, indicatorColor = CrimsonRed.copy(alpha = 0.15f)),
                            modifier = Modifier.testTag("nav_admin")
                        )
                        NavigationBarItem(
                            selected = currentScreen == "data_analysis",
                            onClick = { viewModel.navigateTo("data_analysis") },
                            icon = { Icon(Icons.Default.Analytics, contentDescription = "Analytics") },
                            label = { Text(if (isNepali) "विश्लेषण" else "Analytics") },
                            colors = NavigationBarItemDefaults.colors(selectedIconColor = CrimsonRed, indicatorColor = CrimsonRed.copy(alpha = 0.15f)),
                            modifier = Modifier.testTag("nav_admin_analytics")
                        )
                        NavigationBarItem(
                            selected = currentScreen == "contacts",
                            onClick = { viewModel.navigateTo("contacts") },
                            icon = { Icon(Icons.Default.Contacts, contentDescription = "Contacts") },
                            label = { Text(if (isNepali) "सम्पर्क" else "Contacts") },
                            colors = NavigationBarItemDefaults.colors(selectedIconColor = CrimsonRed, indicatorColor = CrimsonRed.copy(alpha = 0.15f)),
                            modifier = Modifier.testTag("nav_admin_contacts")
                        )
                        NavigationBarItem(
                            selected = currentScreen == "past_orders",
                            onClick = { viewModel.navigateTo("past_orders") },
                            icon = { Icon(Icons.Default.Receipt, contentDescription = "Orders") },
                            label = { Text(if (isNepali) "अर्डरहरू" else "Orders") },
                            colors = NavigationBarItemDefaults.colors(selectedIconColor = CrimsonRed, indicatorColor = CrimsonRed.copy(alpha = 0.15f)),
                            modifier = Modifier.testTag("nav_admin_orders")
                        )
                        NavigationBarItem(
                            selected = currentScreen == "profile",
                            onClick = { viewModel.navigateTo("profile") },
                            icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                            label = { Text(if (isNepali) "प्रोफाइल" else "Profile") },
                            colors = NavigationBarItemDefaults.colors(selectedIconColor = CrimsonRed, indicatorColor = CrimsonRed.copy(alpha = 0.15f)),
                            modifier = Modifier.testTag("nav_admin_profile")
                        )
                    } else if (isMerchant) {
                        // Merchant Navigation items
                        NavigationBarItem(
                            selected = currentScreen == "merchant_dashboard",
                            onClick = { viewModel.navigateTo("merchant_dashboard") },
                            icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                            label = { Text(if (isNepali) "ड्यासबोर्ड" else "Dashboard") },
                            colors = NavigationBarItemDefaults.colors(selectedIconColor = CrimsonRed, indicatorColor = CrimsonRed.copy(alpha = 0.15f)),
                            modifier = Modifier.testTag("nav_merchant_dashboard")
                        )
                        NavigationBarItem(
                            selected = currentScreen == "contacts",
                            onClick = { viewModel.navigateTo("contacts") },
                            icon = { Icon(Icons.Default.Contacts, contentDescription = "Contacts") },
                            label = { Text(if (isNepali) "सम्पर्क/खाता" else "Contacts") },
                            colors = NavigationBarItemDefaults.colors(selectedIconColor = CrimsonRed, indicatorColor = CrimsonRed.copy(alpha = 0.15f)),
                            modifier = Modifier.testTag("nav_merchant_contacts")
                        )
                        NavigationBarItem(
                            selected = currentScreen == "data_analysis",
                            onClick = { viewModel.navigateTo("data_analysis") },
                            icon = { Icon(Icons.Default.Analytics, contentDescription = "Analytics") },
                            label = { Text(if (isNepali) "कर/हिसाब" else "Tax & Calc") },
                            colors = NavigationBarItemDefaults.colors(selectedIconColor = CrimsonRed, indicatorColor = CrimsonRed.copy(alpha = 0.15f)),
                            modifier = Modifier.testTag("nav_merchant_analytics")
                        )
                        NavigationBarItem(
                            selected = currentScreen == "past_orders",
                            onClick = { viewModel.navigateTo("past_orders") },
                            icon = { Icon(Icons.Default.Receipt, contentDescription = "Orders") },
                            label = { Text(if (isNepali) "अर्डरहरू" else "Orders") },
                            colors = NavigationBarItemDefaults.colors(selectedIconColor = CrimsonRed, indicatorColor = CrimsonRed.copy(alpha = 0.15f)),
                            modifier = Modifier.testTag("nav_merchant_orders")
                        )
                        NavigationBarItem(
                            selected = currentScreen == "profile",
                            onClick = { viewModel.navigateTo("profile") },
                            icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                            label = { Text(if (isNepali) "प्रोफाइल" else "Profile") },
                            colors = NavigationBarItemDefaults.colors(selectedIconColor = CrimsonRed, indicatorColor = CrimsonRed.copy(alpha = 0.15f)),
                            modifier = Modifier.testTag("nav_merchant_profile")
                        )
                    } else {
                        // Customer Navigation items
                        NavigationBarItem(
                            selected = currentScreen == "home",
                            onClick = { viewModel.navigateTo("home") },
                            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                            label = { Text(if (isNepali) "गृहपृष्ठ" else "Home") },
                            colors = NavigationBarItemDefaults.colors(selectedIconColor = CrimsonRed, indicatorColor = CrimsonRed.copy(alpha = 0.15f)),
                            modifier = Modifier.testTag("nav_home")
                        )
                        NavigationBarItem(
                            selected = currentScreen == "shops",
                            onClick = { viewModel.navigateTo("shops") },
                            icon = { Icon(Icons.Default.Store, contentDescription = "Shops") },
                            label = { Text(if (isNepali) "पसलहरू" else "Shops") },
                            colors = NavigationBarItemDefaults.colors(selectedIconColor = CrimsonRed, indicatorColor = CrimsonRed.copy(alpha = 0.15f)),
                            modifier = Modifier.testTag("nav_shops")
                        )
                        NavigationBarItem(
                            selected = currentScreen == "contacts",
                            onClick = { viewModel.navigateTo("contacts") },
                            icon = { Icon(Icons.Default.Contacts, contentDescription = "Contacts") },
                            label = { Text(if (isNepali) "सम्पर्क" else "Contacts") },
                            colors = NavigationBarItemDefaults.colors(selectedIconColor = CrimsonRed, indicatorColor = CrimsonRed.copy(alpha = 0.15f)),
                            modifier = Modifier.testTag("nav_customer_contacts")
                        )
                        NavigationBarItem(
                            selected = currentScreen == "cart",
                            onClick = { viewModel.navigateTo("cart") },
                            icon = {
                                BadgedBox(
                                    badge = {
                                        if (totalCartItems > 0) {
                                            Badge(containerColor = CrimsonRed) {
                                                Text("$totalCartItems", color = Color.White, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                ) {
                                    Icon(Icons.Default.ShoppingCart, contentDescription = "Cart")
                                }
                            },
                            label = { Text(if (isNepali) "झोला" else "Cart") },
                            colors = NavigationBarItemDefaults.colors(selectedIconColor = CrimsonRed, indicatorColor = CrimsonRed.copy(alpha = 0.15f)),
                            modifier = Modifier.testTag("nav_cart")
                        )
                        NavigationBarItem(
                            selected = currentScreen == "profile",
                            onClick = { viewModel.navigateTo("profile") },
                            icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                            label = { Text(if (isNepali) "प्रोफाइल" else "Profile") },
                            colors = NavigationBarItemDefaults.colors(selectedIconColor = CrimsonRed, indicatorColor = CrimsonRed.copy(alpha = 0.15f)),
                            modifier = Modifier.testTag("nav_profile")
                        )
                    }
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (currentScreen) {
                "home" -> HomeScreen(
                    viewModel = viewModel,
                    onOpenAiChat = { viewModel.navigateTo("ai_chat") },
                    onOpenCart = { viewModel.navigateTo("cart") },
                    onOpenShop = { shopId -> viewModel.openShop(shopId) },
                    onOpenProduct = { prodId -> viewModel.openProduct(prodId) },
                    onOpenPastOrders = { viewModel.navigateTo("past_orders") }
                )
                "shops" -> ShopsScreen(
                    viewModel = viewModel,
                    onOpenShop = { shopId -> viewModel.openShop(shopId) }
                )
                "shop_detail" -> {
                    val shopId = viewModel.selectedShopId.collectAsState().value ?: ""
                    ShopDetailScreen(
                        shopId = shopId,
                        viewModel = viewModel,
                        onBack = { viewModel.goBack() },
                        onOpenProduct = { prodId -> viewModel.openProduct(prodId) }
                    )
                }
                "product_detail" -> {
                    val prodId = viewModel.selectedProductId.collectAsState().value ?: ""
                    ProductDetailScreen(
                        productId = prodId,
                        viewModel = viewModel,
                        onBack = { viewModel.goBack() },
                        onOpenShop = { shopId -> viewModel.openShop(shopId) }
                    )
                }
                "notices" -> NoticesScreen(
                    viewModel = viewModel
                )
                "contacts" -> ContactsScreen(
                    viewModel = viewModel,
                    onBack = { viewModel.goBack() }
                )
                "data_analysis" -> DataAnalysisScreen(
                    viewModel = viewModel,
                    onBack = { viewModel.goBack() }
                )
                "recharts_visualization", "visualization" -> RechartsVisualizationScreen(
                    viewModel = viewModel,
                    onBack = { viewModel.goBack() }
                )
                "cart" -> CartScreen(
                    viewModel = viewModel,
                    onBack = { viewModel.navigateTo("home") },
                    onOrderPlaced = { viewModel.navigateTo("past_orders") }
                )
                "past_orders" -> PastOrdersScreen(
                    viewModel = viewModel,
                    onBack = { viewModel.goBack() },
                    onNavigateToCart = { viewModel.navigateTo("cart") },
                    onBrowseProducts = { viewModel.navigateTo("home") }
                )
                "order_tracking" -> OrderTrackingScreen(
                    viewModel = viewModel,
                    onBack = { viewModel.goBack() }
                )
                "messenger" -> MessengerScreen(
                    viewModel = viewModel,
                    onBack = { viewModel.goBack() }
                )
                "video_bazar" -> VideoBazarScreen(
                    viewModel = viewModel,
                    onBack = { viewModel.goBack() },
                    onOpenProduct = { prodId -> viewModel.openProduct(prodId) },
                    onOpenShop = { shopId -> viewModel.openShop(shopId) },
                    onNavigateToCart = { viewModel.navigateTo("cart") }
                )
                "customer_tools" -> CustomerToolsScreen(
                    viewModel = viewModel,
                    onBack = { viewModel.goBack() },
                    onOpenShop = { shopId -> viewModel.openShop(shopId) },
                    onOpenProduct = { prodId -> viewModel.openProduct(prodId) }
                )
                "admin_dashboard" -> AdminPanelScreen(
                    viewModel = viewModel,
                    onNavigateToCustomerMode = {
                        viewModel.repo.setRole("c")
                        viewModel.navigateTo("home")
                    },
                    onNavigateToMerchantMode = {
                        viewModel.repo.setRole("m")
                        viewModel.navigateTo("merchant_dashboard")
                    }
                )
                "profile" -> ProfileScreen(
                    viewModel = viewModel,
                    onNavigateToAuth = { viewModel.navigateTo("auth") },
                    onNavigateToOrders = { viewModel.navigateTo("past_orders") }
                )
                "auth" -> AuthScreen(
                    viewModel = viewModel,
                    onAuthSuccess = {
                        val role = viewModel.currentUser.value?.role
                        when (role) {
                            "m" -> viewModel.navigateTo("merchant_dashboard")
                            "a" -> viewModel.navigateTo("admin_dashboard")
                            else -> viewModel.navigateTo("home")
                        }
                    },
                    onBack = { viewModel.navigateTo("home") },
                    onOpenNepalMap = { viewModel.navigateTo("nepal_map") }
                )
                "ai_chat" -> AiShoppingAssistantScreen(
                    viewModel = viewModel,
                    onBack = { viewModel.navigateTo("home") },
                    onOpenNepalMap = { viewModel.navigateTo("nepal_map") }
                )
                "nepal_map" -> NepalMapScreen(
                    viewModel = viewModel,
                    onBack = { viewModel.goBack() },
                    onExploreShops = { dist ->
                        viewModel.setDistrict(dist)
                        viewModel.navigateTo("shops")
                    },
                    onExploreProducts = { dist ->
                        viewModel.setDistrict(dist)
                        viewModel.navigateTo("home")
                    }
                )
                "merchant_dashboard" -> MerchantDashboardScreen(
                    viewModel = viewModel
                )
                "app_update" -> AppUpdateScreen(
                    viewModel = viewModel,
                    onBack = { viewModel.goBack() }
                )
                else -> HomeScreen(
                    viewModel = viewModel,
                    onOpenAiChat = { viewModel.navigateTo("ai_chat") },
                    onOpenCart = { viewModel.navigateTo("cart") },
                    onOpenShop = { shopId -> viewModel.openShop(shopId) },
                    onOpenProduct = { prodId -> viewModel.openProduct(prodId) },
                    onOpenPastOrders = { viewModel.navigateTo("past_orders") },
                    onOpenNepalMap = { viewModel.navigateTo("nepal_map") }
                )
            }
        }
    }
}
