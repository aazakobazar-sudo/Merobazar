package com.example.ui.screens

import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.MeroBazarSeedData
import com.example.data.model.ShopDetails
import com.example.data.model.UserProfile
import com.example.ui.MainViewModel
import com.example.ui.theme.BrandBgLight
import com.example.ui.theme.BrandIndigo
import com.example.ui.theme.BrandIndigoLight
import com.example.ui.theme.BrandSaffron
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.DarkCrimson
import com.example.ui.theme.JadeGreen
import com.example.ui.components.WavingNepalFlag

import androidx.compose.foundation.layout.ExperimentalLayoutApi

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AuthScreen(
    viewModel: MainViewModel,
    onAuthSuccess: () -> Unit,
    onBack: () -> Unit = {},
    onOpenNepalMap: () -> Unit = {}
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val isNepali by viewModel.isNepali.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    // 0 = लगइन (Login), 1 = दर्ता (Register), 2 = फर्गेट पासवर्ड (Forgot Password)
    var selectedTab by remember { mutableIntStateOf(0) }

    // Login state
    var loginEmail by remember { mutableStateOf("") }
    var loginPass by remember { mutableStateOf("") }
    var loginPasswordVisible by remember { mutableStateOf(false) }

    // Register state (Personal info from zip)
    var regName by remember { mutableStateOf("") }
    var regPhone by remember { mutableStateOf("") }
    var regEmail by remember { mutableStateOf("") }
    var regDob by remember { mutableStateOf("2055-04-12") }
    var regGender by remember { mutableStateOf("पुरुष (Male)") }
    var regRole by remember { mutableStateOf("c") } // "c" = Customer, "m" = Merchant, "a" = Admin

    // Address info from zip (7 Provinces & 77 Districts)
    var regProv by remember { mutableStateOf("Bagmati") }
    val districtsForProv = MeroBazarSeedData.provinces[regProv] ?: listOf("Kathmandu")
    var regDist by remember { mutableStateOf("Kathmandu") }
    var regLocal by remember { mutableStateOf("Kathmandu") }
    var regWard by remember { mutableStateOf("10") }
    var regToleRoad by remember { mutableStateOf("नयाँ बानेश्वर मार्ग") }
    var regGpsLocation by remember { mutableStateOf("27.7172, 85.3240 (काठमाडौँ)") }
    var isLocationTaggedOnMap by remember { mutableStateOf(false) }
    var showQuickMapDialog by remember { mutableStateOf(false) }

    // Merchant specific registration fields from zip
    var regShopName by remember { mutableStateOf("") }
    var regShopCatId by remember { mutableIntStateOf(1) }
    var regShopType by remember { mutableStateOf("किराना") }
    var regPan by remember { mutableStateOf("") }
    var regLrn by remember { mutableStateOf("") }
    var regPaymentMethod by remember { mutableStateOf("eSewa") }
    var regRef by remember { mutableStateOf("ESW-981245") }

    // Verification from zip
    val demoOtp = "748291"
    var regOtpInput by remember { mutableStateOf("") }
    var regPass by remember { mutableStateOf("") }
    var regPassConfirm by remember { mutableStateOf("") }
    var regPasswordVisible by remember { mutableStateOf(false) }

    // Forgot password state (फर्गेट पासवर्ड)
    var forgotEmail by remember { mutableStateOf("") }
    var forgotSuccessMsg by remember { mutableStateOf<String?>(null) }

    // Dialogs & Feedback
    var showRegistrationCardDialog by remember { mutableStateOf(false) }
    var newlyCreatedProfile by remember { mutableStateOf<UserProfile?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var infoMessage by remember { mutableStateOf<String?>(null) }

    // Registration Record Card Dialog (दर्ता प्रमाण पत्र from regcard.js)
    if (showRegistrationCardDialog && newlyCreatedProfile != null) {
        val u = newlyCreatedProfile!!
        AlertDialog(
            onDismissRequest = {
                showRegistrationCardDialog = false
                onAuthSuccess()
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("📜", fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = if (isNepali) "मेरो बजार दर्ता प्रमाण" else "Mero Bazar Registration Record",
                            fontWeight = FontWeight.ExtraBold,
                            color = BrandIndigo,
                            fontSize = 17.sp
                        )
                        Text(
                            text = if (isNepali) "आधिकारिक खाता प्रमाण पत्र (Official Card)" else "Official Account Card",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .fillMaxWidth()
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF4E0)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                    ) {
                        Text(
                            text = if (isNepali)
                                "⚠️ यो तपाईंलाई पछि कुनै समस्या आएमा यही दर्ता प्रमाणको आधारमा तपाईंले रिपोर्ट गर्न सक्नुहुनेछ।"
                            else
                                "If you face any problem later, you can report it based on this registration record.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF8A4B00),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(10.dp)
                        )
                    }

                    // Account details table
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDCE5F1)),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            RegCardRow("खाता कोड (Account Code)", u.code, isBold = true, color = BrandIndigo)
                            RegCardRow("दर्ता मिति (Date)", u.regDate.ifBlank { "आज" })
                            RegCardRow("खाता प्रकार (Role)", when (u.role) {
                                "m" -> "🏪 व्यापारी (Merchant)"
                                "a" -> "👑 एडमिन (Admin)"
                                else -> "👤 ग्राहक (Customer)"
                            })
                            RegCardRow("पूरा नाम (Full Name)", u.name)
                            RegCardRow("मोबाइल (Mobile)", u.phone)
                            RegCardRow("इमेल (Email)", u.email)
                            RegCardRow("जन्म मिति (DOB)", u.dob.ifBlank { "2055-04-12" })
                            RegCardRow("ठेगाना (Address)", "${u.prov}, ${u.dist}, ${u.local}, वडा ${u.ward}")

                            if (u.shop != null) {
                                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                                RegCardRow("पसलको नाम (Shop)", u.shop.name)
                                RegCardRow("पसलको प्रकार (Type)", u.shop.type)
                                if (u.shop.pan.isNotBlank()) RegCardRow("PAN (प्यान)", u.shop.pan)
                                if (u.shop.lrn.isNotBlank()) RegCardRow("दर्ता नं. (LRN)", u.shop.lrn)
                                RegCardRow("भुक्तानी माध्यम (Payment)", u.shop.pm)
                                RegCardRow("रेफरन्स ID (Ref)", u.shop.ref.ifBlank { "N/A" })
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                            RegCardRow(
                                "🎁 स्वागत उपहार",
                                "५० क्विन + रु. १०० वालेट",
                                color = JadeGreen,
                                isBold = true
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showRegistrationCardDialog = false
                        onAuthSuccess()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandIndigo),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(if (isNepali) "बजार प्रवेश गर्नुहोस् ➔" else "Enter Bazar ➔", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        clipboardManager.setText(AnnotatedString("खाता कोड: ${u.code} | नाम: ${u.name} | इमेल: ${u.email}"))
                        Toast.makeText(context, if (isNepali) "खाता कोड र विवरण कपी भयो!" else "Account code copied!", Toast.LENGTH_SHORT).show()
                    },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isNepali) "कपी" else "Copy")
                }
            }
        )
    }

    if (showQuickMapDialog) {
        val quickPlaces = listOf(
            Triple("काठमाडौँ नयाँ बानेश्वर (वडा १०, नयाँ बानेश्वर मार्ग)", "Bagmati", "Kathmandu"),
            Triple("काठमाडौँ दरवारमार्ग (वडा १, दरवारमार्ग सडक)", "Bagmati", "Kathmandu"),
            Triple("ललितपुर पाटन दरवार (वडा १२, मंगलबजार मार्ग)", "Bagmati", "Lalitpur"),
            Triple("भक्तपुर दरवार स्क्वायर (वडा ९, दरवार मार्ग)", "Bagmati", "Bhaktapur"),
            Triple("पोखरा लेकसाइड (वडा ६, लेकसाइड रोड)", "Gandaki", "Kaski"),
            Triple("बुटवल ट्राफिक चोक (वडा ६, राजमार्ग चौराहा)", "Lumbini", "Rupandehi"),
            Triple("विराटनगर मेनरोड (वडा ७, मेनरोड)", "Koshi", "Morang"),
            Triple("चितवन नारायणगढ (वडा २, सहिद चोक)", "Bagmati", "Chitwan"),
            Triple("झापा विर्तामोड (वडा ४, मुक्ति चोक)", "Koshi", "Jhapa"),
            Triple("धनगढी चौराहा (वडा १, मेनरोड चौराहा)", "Sudurpashchim", "Kailali")
        )

        AlertDialog(
            onDismissRequest = { showQuickMapDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Map, contentDescription = null, tint = BrandIndigo)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isNepali) "नेपाल नक्सा (अफलाइन गुगल म्याप)" else "Nepal Map (Offline)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = if (isNepali)
                            "📶 १००% डाउनलोड गरिएको अफलाइन नक्सा। ग्राहक र व्यापारी दुबैको लागि स्थान, वडा र बाटो ट्याग अनिवार्य छ।"
                        else
                            "Offline downloaded Nepal map. Location, Ward and Road are mandatory.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF1B5E20)
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(170.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFE2E8F0)),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.nepal_map_google),
                            contentDescription = "Offline Google Maps Nepal",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = BrandIndigo.copy(alpha = 0.9f),
                            modifier = Modifier.padding(6.dp)
                        ) {
                            Text(
                                text = "📍 $regDist | वडा: $regWard | $regToleRoad",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = if (isNepali) "प्रमुख व्यापारिक केन्द्र तथा स्थानहरू (एक ट्यापमा छान्नुहोस्):" else "Quick select locations:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = BrandIndigo
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    androidx.compose.foundation.layout.FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        quickPlaces.take(6).forEach { (pTitle, pProv, pDist) ->
                            Surface(
                                modifier = Modifier.clickable {
                                    regProv = pProv
                                    regDist = pDist
                                    regLocal = if (pDist == "Kathmandu") "काठमाडौँ महानगरपालिका" else if (pDist == "Lalitpur") "ललितपुर महानगरपालिका" else "$pDist नगरपालिका"
                                    val wardNum = if (pTitle.contains("वडा ")) pTitle.substringAfter("वडा ").substringBefore(",") else "10"
                                    regWard = wardNum.trim()
                                    val roadStr = if (pTitle.contains(",")) pTitle.substringAfter(",").substringBefore(")") else "मुख्य सडक"
                                    regToleRoad = roadStr.trim()
                                    regGpsLocation = "27.7172, 85.3240 ($pDist)"
                                    isLocationTaggedOnMap = true
                                    Toast.makeText(context, "$pDist ट्याग गरियो!", Toast.LENGTH_SHORT).show()
                                },
                                shape = RoundedCornerShape(8.dp),
                                color = if (regDist == pDist) BrandIndigo else Color(0xFFF1F5F9),
                                border = if (regDist == pDist) null else androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1))
                            ) {
                                Text(
                                    text = pTitle.substringBefore(" ("),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (regDist == pDist) Color.White else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = regToleRoad,
                        onValueChange = { regToleRoad = it },
                        label = { Text(if (isNepali) "बाटो / सडक / टोलको नाम *" else "Road / Street / Tole *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = regWard,
                        onValueChange = { regWard = it },
                        label = { Text(if (isNepali) "वडा नं. *" else "Ward No. *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        isLocationTaggedOnMap = true
                        showQuickMapDialog = false
                        Toast.makeText(context, if (isNepali) "✅ स्थान र बाटो सफलतापूर्वक ट्याग भयो!" else "Location tagged!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = JadeGreen),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(if (isNepali) "📍 यो स्थान ट्याग गर्नुहोस्" else "Tag This Location", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showQuickMapDialog = false
                        onOpenNepalMap()
                    }
                ) {
                    Text(if (isNepali) "पूरा नक्सा खोल्नुहोस् ➔" else "Open Full Map ➔", color = BrandIndigo)
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isNepali) "मेरो बजार - खाता व्यवस्थापन" else "Mero Bazar - Account",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    if (currentUser != null) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BrandIndigo,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // App Branding Hero Banner with Large "मेरो बजार" and Waving Nepal Flag
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF003893)), // Deep Nepal Blue background
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 18.dp)
                ) {
                    // Waving Nepal Flag (नेपालको झन्डा फरफराएको जस्तो)
                    WavingNepalFlag(
                        modifier = Modifier
                            .size(width = 54.dp, height = 66.dp)
                    )

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isNepali) "मेरो बजार" else "Mero Bazar",
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (isNepali) "नेपालको आफ्नै डिजिटल बजार" else "Nepal's Official Digital Marketplace",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFFFD700) // Golden accent
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0x33FFFFFF)
                        ) {
                            Text(
                                text = if (isNepali) "🇳🇵 सुरक्षित र प्रमाणित स्वदेशी पोर्टल" else "🇳🇵 Verified Nepali Portal",
                                fontSize = 11.sp,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3 TABS: लगइन (Login), दर्ता (Register), फर्गेट पासवर्ड (Forgot Password)
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp))
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = {
                        selectedTab = 0
                        errorMessage = null
                        infoMessage = null
                    },
                    text = {
                        Text(
                            text = if (isNepali) "🔐 लगइन" else "Login",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = if (selectedTab == 0) BrandIndigo else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    modifier = Modifier.testTag("tab_login")
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = {
                        selectedTab = 1
                        errorMessage = null
                        infoMessage = null
                    },
                    text = {
                        Text(
                            text = if (isNepali) "📝 नयाँ दर्ता" else "Register",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = if (selectedTab == 1) BrandIndigo else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    modifier = Modifier.testTag("tab_register")
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = {
                        selectedTab = 2
                        errorMessage = null
                        infoMessage = null
                        forgotSuccessMsg = null
                    },
                    text = {
                        Text(
                            text = if (isNepali) "🔄 फर्गेट पासवर्ड" else "Forgot Pass",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = if (selectedTab == 2) BrandIndigo else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    modifier = Modifier.testTag("tab_forgot_pass")
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Feedback messages (Errors & Info)
            if (errorMessage != null) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
                ) {
                    Text(
                        text = errorMessage ?: "",
                        color = CrimsonRed,
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            if (infoMessage != null) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
                ) {
                    Text(
                        text = infoMessage ?: "",
                        color = Color(0xFF2E7D32),
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // ==========================================
            // TAB 0: 🔐 लगइन (LOGIN)
            // ==========================================
            if (selectedTab == 0) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = if (isNepali) "खातामा लगइन गर्नुहोस्" else "Login to Your Account",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isNepali) "कृपया आफ्नो दर्ता गरिएको इमेल र पासवर्ड राख्नुहोस्" else "Please enter your registered email and password",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Security Notice (High security, credentials kept strictly confidential)
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("🔒", fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isNepali)
                                        "सुरक्षा प्रणाली: प्रत्येक खाताको उच्च गोपनीयता तथा अधिकार संरक्षण (Role-Based Security) सुनिश्चित गरिएको छ।"
                                    else
                                        "Security: High privacy & strict role-based account isolation enforced.",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Quick demo role chips for merchant and customer
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        loginEmail = "merchant@merobazar.np"
                                        loginPass = "Merchant123"
                                    },
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                            ) {
                                Text(
                                    text = "🏪 व्यापारी (Merchant)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                    textAlign = TextAlign.Center
                                )
                            }
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        loginEmail = "customer@merobazar.np"
                                        loginPass = "Customer123"
                                    },
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                            ) {
                                Text(
                                    text = "👤 ग्राहक (Customer)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Google / Gmail Sign In & Verification Button
                        OutlinedButton(
                            onClick = {
                                (context as? Activity)?.let { activity ->
                                    viewModel.signInWithGoogle(
                                        activity = activity,
                                        onSuccess = { onAuthSuccess() },
                                        onError = { err -> errorMessage = err }
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("login_google_btn"),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                        ) {
                            Text(
                                text = "G",
                                color = Color(0xFF4285F4),
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = if (isNepali) "Google / Gmail बाट लगइन गर्नुहोस्" else "Sign In with Google / Gmail",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            HorizontalDivider(modifier = Modifier.weight(1f))
                            Text(
                                text = if (isNepali) " वा इमेल/पासवर्ड " else " OR with Password ",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            HorizontalDivider(modifier = Modifier.weight(1f))
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = loginEmail,
                            onValueChange = { loginEmail = it },
                            label = { Text(if (isNepali) "इमेल वा मोबाइल नम्बर (Email or Phone No.) *" else "Email or Phone Number *") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = BrandIndigo) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("login_email_input")
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = loginPass,
                            onValueChange = { loginPass = it },
                            label = { Text(if (isNepali) "पासवर्ड (Password)" else "Password") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = BrandIndigo) },
                            trailingIcon = {
                                IconButton(onClick = { loginPasswordVisible = !loginPasswordVisible }) {
                                    Icon(
                                        if (loginPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = null
                                    )
                                }
                            },
                            visualTransformation = if (loginPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            modifier = Modifier.fillMaxWidth().testTag("login_password_input")
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Forgot password helper link
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(
                                onClick = {
                                    forgotEmail = loginEmail
                                    selectedTab = 2
                                    errorMessage = null
                                }
                            ) {
                                Text(
                                    text = if (isNepali) "पासवर्ड बिर्सनुभयो? (फर्गेट पासवर्ड) ➔" else "Forgot password? ➔",
                                    color = BrandSaffron,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                if (loginEmail.isBlank() || loginPass.isBlank()) {
                                    errorMessage = if (isNepali) "इमेल वा मोबाइल नम्बर र पासवर्ड प्रविष्ट गर्नुहोस्" else "Please enter email/phone and password"
                                    return@Button
                                }
                                isLoading = true
                                errorMessage = null
                                viewModel.signInWithEmailOrPhone(loginEmail, loginPass) { success, err ->
                                    isLoading = false
                                    if (success) {
                                        onAuthSuccess()
                                    } else {
                                        errorMessage = err
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = BrandIndigo),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().height(50.dp).testTag("login_submit_button"),
                            enabled = !isLoading
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                            } else {
                                Text(
                                    text = if (isNepali) "🔐 लगइन गर्नुहोस्" else "Login",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(12.dp))

                        // Switch to Register
                        TextButton(
                            onClick = {
                                selectedTab = 1
                                errorMessage = null
                            },
                            modifier = Modifier.fillMaxWidth().testTag("switch_to_register_link")
                        ) {
                            Text(
                                text = if (isNepali) "खाता छैन? यहाँ थिचेर नयाँ दर्ता गर्नुहोस् ➔" else "Don't have an account? Register here ➔",
                                color = BrandIndigo,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // ==========================================
            // TAB 1: 📝 दर्ता (REGISTER - Multi-role from zip)
            // ==========================================
            else if (selectedTab == 1) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = if (isNepali) "नयाँ खाता दर्ता फाराम" else "New Account Registration",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isNepali) "नेपालको ७ प्रदेश र ७७ जिल्ला अनुसार आफ्नो भूमिका छान्नुहोस्" else "Choose your role & register across Nepal's 7 provinces",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Role Selector: Customer vs Merchant (Admin registration is permanently closed)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val roles = listOf(
                                Triple("c", if (isNepali) "👤 ग्राहक (Customer)" else "Customer", BrandIndigo),
                                Triple("m", if (isNepali) "🏪 व्यापारी (Merchant)" else "Merchant", BrandSaffron)
                            )
                            roles.forEach { (rCode, rLabel, rColor) ->
                                val isSelected = regRole == rCode
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { regRole = rCode },
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSelected) rColor else MaterialTheme.colorScheme.surfaceVariant,
                                    border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                                ) {
                                    Box(
                                        modifier = Modifier.padding(vertical = 10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = rLabel,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                            fontSize = 12.sp,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }

                        // Merchant KYC Approval Notice
                        if (regRole == "m") {
                            Spacer(modifier = Modifier.height(10.dp))
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF7ED)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF97316)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("🛡️", fontSize = 22.sp)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = if (isNepali) "एडमिन प्रमाणीकरण अनिवार्य (Admin KYC Approval)" else "Admin Approval Required",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = Color(0xFFC2410C)
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = if (isNepali)
                                                "व्यापारी खाता दर्ता भएपछि मुख्य एडमिन द्वारा KYC प्रमाणीकरण गरिनेछ। प्रमाणीकरणपछि मात्र पसल पूर्ण रूपमा सक्रिय हुनेछ।"
                                            else
                                                "Once registered, Admin will review and verify your shop before public transactions can begin.",
                                            fontSize = 11.sp,
                                            color = Color(0xFF7C2D12),
                                            lineHeight = 15.sp
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Google Verification Button (Matches gauth.js from web archive)
                        OutlinedButton(
                            onClick = {
                                (context as? Activity)?.let { activity ->
                                    viewModel.signInWithGoogle(
                                        activity = activity,
                                        onSuccess = {
                                            val u = viewModel.currentUser.value
                                            if (u != null) {
                                                regName = u.name
                                                regEmail = u.email
                                                Toast.makeText(
                                                    context,
                                                    if (isNepali) "✓ Google बाट इमेल प्रमाणित भयो: ${u.email}" else "✓ Email verified with Google: ${u.email}",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        },
                                        onError = { err -> errorMessage = err }
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("reg_google_btn"),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1))
                        ) {
                            Text(
                                text = "G",
                                color = Color(0xFF4285F4),
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = if (regEmail.isNotBlank())
                                    (if (isNepali) "✓ Google प्रमाणित: $regEmail" else "✓ Google Verified: $regEmail")
                                else
                                    (if (isNepali) "Google बाट इमेल प्रमाणित गर्नुहोस्" else "Verify email with Google"),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = BrandIndigo
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // 1. Personal Details
                        Text(
                            text = if (isNepali) "१. व्यक्तिगत विवरण (Personal Details)" else "1. Personal Details",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = BrandIndigo
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = regName,
                            onValueChange = { regName = it },
                            label = { Text(if (isNepali) "पूरा नाम *" else "Full Name *") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = BrandIndigo) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("reg_name_input")
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = regPhone,
                            onValueChange = { regPhone = it },
                            label = { Text(if (isNepali) "मोबाइल नम्बर *" else "Mobile Number *") },
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = BrandIndigo) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier.fillMaxWidth().testTag("reg_phone_input")
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = regEmail,
                            onValueChange = { regEmail = it },
                            label = { Text(if (isNepali) "इमेल ठेगाना *" else "Email Address *") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = BrandIndigo) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            modifier = Modifier.fillMaxWidth().testTag("reg_email_input")
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = regDob,
                                onValueChange = { regDob = it },
                                label = { Text(if (isNepali) "जन्म मिति (वि.सं.)" else "DOB (B.S.)") },
                                leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null, tint = BrandIndigo) },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = regGender,
                                onValueChange = { regGender = it },
                                label = { Text(if (isNepali) "लिङ्ग" else "Gender") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // 2. Address Details
                        Text(
                            text = if (isNepali) "२. ठेगाना विवरण (Address in Nepal)" else "2. Address Details",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = BrandIndigo
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        // Province Dropdown
                        var provExpanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = provExpanded,
                            onExpandedChange = { provExpanded = !provExpanded },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = regProv,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text(if (isNepali) "प्रदेश छान्नुहोस् *" else "Select Province *") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = provExpanded) },
                                modifier = Modifier.menuAnchor().fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = provExpanded,
                                onDismissRequest = { provExpanded = false }
                            ) {
                                MeroBazarSeedData.provinces.keys.forEach { provName ->
                                    DropdownMenuItem(
                                        text = { Text(provName) },
                                        onClick = {
                                            regProv = provName
                                            regDist = MeroBazarSeedData.provinces[provName]?.firstOrNull() ?: ""
                                            provExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // District Dropdown
                        var distExpanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = distExpanded,
                            onExpandedChange = { distExpanded = !distExpanded },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = regDist,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text(if (isNepali) "जिल्ला छान्नुहोस् *" else "Select District *") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = distExpanded) },
                                modifier = Modifier.menuAnchor().fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = distExpanded,
                                onDismissRequest = { distExpanded = false }
                            ) {
                                districtsForProv.forEach { distName ->
                                    DropdownMenuItem(
                                        text = { Text(distName) },
                                        onClick = {
                                            regDist = distName
                                            distExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = regLocal,
                                onValueChange = { regLocal = it },
                                label = { Text(if (isNepali) "नगर / गाउँपालिका *" else "Municipality *") },
                                singleLine = true,
                                modifier = Modifier.weight(1.4f)
                            )
                            OutlinedTextField(
                                value = regWard,
                                onValueChange = { regWard = it },
                                label = { Text(if (isNepali) "वडा नं. *" else "Ward No. *") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = regToleRoad,
                            onValueChange = { regToleRoad = it },
                            label = { Text(if (isNepali) "बाटो / सडक / मार्ग / टोल *" else "Road / Street / Tole *") },
                            leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = BrandIndigo) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("reg_road_input")
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // GPS Pin
                        OutlinedTextField(
                            value = regGpsLocation,
                            onValueChange = { regGpsLocation = it },
                            label = { Text(if (isNepali) "GPS लोकेसन पिन (अक्षांश, देशान्तर)" else "GPS Pin") },
                            leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = BrandIndigo) },
                            trailingIcon = {
                                TextButton(onClick = {
                                    regGpsLocation = "27.7172, 85.3240 ($regDist)"
                                    isLocationTaggedOnMap = true
                                    Toast.makeText(context, if (isNepali) "GPS पिन सेट भयो!" else "GPS pinned!", Toast.LENGTH_SHORT).show()
                                }) {
                                    Text(if (isNepali) "पिन" else "Pin", fontWeight = FontWeight.Bold)
                                }
                            },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // ==========================================
                        // MANDATORY NEPAL MAP TAGGING CARD (अनिवार्य)
                        // ==========================================
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isLocationTaggedOnMap) Color(0xFFF0FDF4) else Color(0xFFFFF7ED)
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.5.dp,
                                if (isLocationTaggedOnMap) Color(0xFF86EFAC) else Color(0xFFFDBA74)
                            )
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (isLocationTaggedOnMap) Icons.Default.CheckCircle else Icons.Default.Map,
                                        contentDescription = null,
                                        tint = if (isLocationTaggedOnMap) JadeGreen else Color(0xFFC2410C),
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (isLocationTaggedOnMap)
                                            (if (isNepali) "✅ नक्सामा लोकेसन प्रमाणित ट्याग भयो" else "Location Tagged & Verified")
                                        else
                                            (if (isNepali) "⚠️ नेपाल नक्सा लोकेसन ट्याग (अनिवार्य / Mandatory)" else "Nepal Map Tag (Mandatory)"),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = if (isLocationTaggedOnMap) Color(0xFF166534) else Color(0xFFC2410C)
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = if (isLocationTaggedOnMap)
                                        "$regDist, $regLocal, वडा $regWard, $regToleRoad\n(GPS: $regGpsLocation)"
                                    else
                                        (if (isNepali)
                                            "ग्राहक तथा व्यापारी दुबैले दर्ता गर्न नेपाल नक्साबाट आफ्नो स्थान, वडा र बाटो अनिवार्य ट्याग गर्नुपर्छ।"
                                        else
                                            "Both Customers and Merchants must tag their exact location on the Nepal map before registration."),
                                    fontSize = 12.sp,
                                    color = if (isLocationTaggedOnMap) Color(0xFF15803D) else Color(0xFF7C2D12),
                                    lineHeight = 16.sp
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = { showQuickMapDialog = true },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isLocationTaggedOnMap) JadeGreen else BrandIndigo
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1.2f).testTag("btn_quick_map_tag")
                                    ) {
                                        Icon(Icons.Default.Map, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (isLocationTaggedOnMap)
                                                (if (isNepali) "पुनः ट्याग" else "Re-tag")
                                            else
                                                (if (isNepali) "🗺️ नक्साबाट ट्याग (अनिवार्य)" else "Tag on Map"),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    OutlinedButton(
                                        onClick = onOpenNepalMap,
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f).testTag("btn_open_nepal_map_auth")
                                    ) {
                                        Text(if (isNepali) "पूरा नक्सा" else "Full Map", fontSize = 12.sp, color = BrandIndigo)
                                    }
                                }
                            }
                        }

                        // 3. Role specific fields
                        if (regRole == "m") {
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = if (isNepali) "३. पसल तथा व्यापारिक विवरण (Merchant Shop Details)" else "3. Shop Details",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = BrandSaffron
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = regShopName,
                                onValueChange = { regShopName = it },
                                label = { Text(if (isNepali) "पसल / व्यवसायको नाम *" else "Shop / Business Name *") },
                                leadingIcon = { Icon(Icons.Default.Storefront, contentDescription = null, tint = BrandSaffron) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("reg_shop_name_input")
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Shop Category Dropdown (1-5 from zip)
                            var catExpanded by remember { mutableStateOf(false) }
                            val currentCat = MeroBazarSeedData.categories.find { it.id == regShopCatId }
                            ExposedDropdownMenuBox(
                                expanded = catExpanded,
                                onExpandedChange = { catExpanded = !catExpanded },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                OutlinedTextField(
                                    value = "${currentCat?.id}. ${currentCat?.name}",
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text(if (isNepali) "मुख्य वर्ग छान्नुहोस्" else "Main Category") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = catExpanded) },
                                    modifier = Modifier.menuAnchor().fillMaxWidth()
                                )
                                ExposedDropdownMenu(
                                    expanded = catExpanded,
                                    onDismissRequest = { catExpanded = false }
                                ) {
                                    MeroBazarSeedData.categories.forEach { cat ->
                                        DropdownMenuItem(
                                            text = { Text("${cat.id}. ${cat.name}") },
                                            onClick = {
                                                regShopCatId = cat.id
                                                regShopType = cat.subTypes.firstOrNull() ?: ""
                                                catExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = regShopType,
                                onValueChange = { regShopType = it },
                                label = { Text(if (isNepali) "पसलको प्रकार (उदा. किराना, हार्डवेयर)" else "Shop Type") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = regPan,
                                    onValueChange = { regPan = it },
                                    label = { Text("PAN / VAT") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = regLrn,
                                    onValueChange = { regLrn = it },
                                    label = { Text(if (isNepali) "दर्ता नं. (LRN)" else "Reg No.") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Subscription notice from zip
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF9E6)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = if (isNepali) "💰 सदस्यता शुल्क: रु. १०० (३५ दिन, २० सामान समावेश)" else "Fee: Rs. 100 for 35 days (20 items included)",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = Color(0xFF9A6B00)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                        listOf("eSewa", "Khalti", "बैंक ट्रान्सफर").forEach { pm ->
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.clickable { regPaymentMethod = pm }
                                            ) {
                                                Icon(
                                                    if (regPaymentMethod == pm) Icons.Default.CheckCircle else Icons.Default.CreditCard,
                                                    contentDescription = null,
                                                    tint = if (regPaymentMethod == pm) BrandSaffron else Color.Gray,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(pm, fontSize = 12.sp, fontWeight = if (regPaymentMethod == pm) FontWeight.Bold else FontWeight.Normal)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // 4. Security & OTP
                        Text(
                            text = if (isNepali) "४. सुरक्षा र पासवर्ड (Security & Password)" else "4. Security & Password",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = BrandIndigo
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        // OTP from zip
                        OutlinedTextField(
                            value = regOtpInput,
                            onValueChange = { regOtpInput = it },
                            label = { Text(if (isNepali) "OTP कोड प्रविष्ट गर्नुहोस्" else "Enter OTP Code") },
                            leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null, tint = BrandIndigo) },
                            trailingIcon = {
                                TextButton(onClick = {
                                    regOtpInput = demoOtp
                                    Toast.makeText(context, if (isNepali) "OTP कोड ($demoOtp) भरियो!" else "OTP filled!", Toast.LENGTH_SHORT).show()
                                }) {
                                    Text(if (isNepali) "कोड भर्नुहोस्" else "Fill OTP", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = regPass,
                            onValueChange = { regPass = it },
                            label = { Text(if (isNepali) "पासवर्ड (कम्तिमा ६ अक्षर) *" else "Password (min 6 chars) *") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = BrandIndigo) },
                            trailingIcon = {
                                IconButton(onClick = { regPasswordVisible = !regPasswordVisible }) {
                                    Icon(
                                        if (regPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = null
                                    )
                                }
                            },
                            visualTransformation = if (regPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            modifier = Modifier.fillMaxWidth().testTag("reg_password_input")
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = regPassConfirm,
                            onValueChange = { regPassConfirm = it },
                            label = { Text(if (isNepali) "पासवर्ड पुष्टि गर्नुहोस् *" else "Confirm Password *") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = BrandIndigo) },
                            visualTransformation = if (regPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            modifier = Modifier.fillMaxWidth().testTag("reg_confirm_password_input")
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        // Submit Registration
                        Button(
                            onClick = {
                                if (regName.isBlank() || regEmail.isBlank() || regPass.length < 6) {
                                    errorMessage = if (isNepali)
                                        "कृपया पूरा नाम, इमेल र कम्तिमा ६ अक्षरको पासवर्ड राख्नुहोस्"
                                    else
                                        "Please check name, email, and password (min 6 chars)"
                                    return@Button
                                }

                                if (regPassConfirm.isNotBlank() && regPass != regPassConfirm) {
                                    errorMessage = if (isNepali) "दुवै पासवर्ड मिलेन" else "Passwords do not match"
                                    return@Button
                                }

                                if (regRole == "m" && regShopName.isBlank()) {
                                    errorMessage = if (isNepali) "कृपया पसलको नाम प्रविष्ट गर्नुहोस्" else "Please enter your shop name"
                                    return@Button
                                }

                                // MANDATORY LOCATION TAGGING ENFORCEMENT (Both Customer & Merchant)
                                if (!isLocationTaggedOnMap) {
                                    errorMessage = if (isNepali)
                                        "⚠️ कृपया पहिले नेपाल नक्साबाट आफ्नो स्थान, वडा नं. र बाटो/सडक अनिवार्य ट्याग गर्नुहोस्! (ग्राहक र व्यापारी दुबैको लागि अनिवार्य)"
                                    else
                                        "Please tag your location on the Nepal map first (Mandatory for both Customer & Merchant)"
                                    return@Button
                                }

                                if (regWard.isBlank() || regToleRoad.isBlank()) {
                                    errorMessage = if (isNepali)
                                        "कृपया वडा नं. र बाटो/सडकको नाम अनिवार्य उल्लेख गर्नुहोस्।"
                                    else
                                        "Please enter Ward number and Road/Street name."
                                    return@Button
                                }

                                isLoading = true
                                errorMessage = null

                                val shopDetails = if (regRole == "m") {
                                    ShopDetails(
                                        name = regShopName,
                                        cat = regShopCatId,
                                        type = regShopType.ifBlank { "किराना" },
                                        pan = regPan,
                                        lrn = regLrn,
                                        pm = regPaymentMethod,
                                        ref = regRef
                                    )
                                } else null

                                viewModel.signUpWithEmail(
                                    name = regName,
                                    email = regEmail,
                                    pass = regPass,
                                    role = regRole,
                                    phone = regPhone.ifBlank { "9841000000" },
                                    shopDetails = shopDetails,
                                    prov = regProv,
                                    dist = regDist,
                                    local = regLocal,
                                    ward = regWard.toIntOrNull() ?: 1,
                                    toleRoad = regToleRoad,
                                    isMapTagged = true,
                                    dob = regDob,
                                    gn = regGender
                                ) { success, err, profile ->
                                    isLoading = false
                                    if (success) {
                                        newlyCreatedProfile = profile
                                        showRegistrationCardDialog = true
                                    } else {
                                        errorMessage = err
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (regRole == "m") BrandSaffron else BrandIndigo
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().height(52.dp).testTag("reg_submit_button"),
                            enabled = !isLoading
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                            } else {
                                val submitLabel = if (regRole == "m") {
                                    if (isNepali) "🏪 व्यापारी खाता दर्ता गर्नुहोस्" else "Register as Merchant"
                                } else {
                                    if (isNepali) "👤 ग्राहक खाता दर्ता गर्नुहोस्" else "Register as Customer"
                                }
                                Text(submitLabel, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Switch to Login
                        TextButton(
                            onClick = {
                                selectedTab = 0
                                errorMessage = null
                            },
                            modifier = Modifier.fillMaxWidth().testTag("switch_to_login_btn")
                        ) {
                            Text(
                                text = if (isNepali) "पहिले नै खाता छ? यहाँ थिचेर लगइन गर्नुहोस् ➔" else "Already have an account? Login here ➔",
                                color = BrandIndigo,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // ==========================================
            // TAB 2: 🔄 फर्गेट पासवर्ड (FORGOT PASSWORD)
            // ==========================================
            else if (selectedTab == 2) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Refresh, contentDescription = null, tint = BrandSaffron, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isNepali) "पासवर्ड रिसेट तथा इमेल प्रमाण" else "Reset Password & Verification",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = if (isNepali)
                                "तपाईंको दर्ता गरिएको इमेल प्रविष्ट गर्नुहोस्। हामी तपाईंलाई पासवर्ड रिसेट गर्ने आधिकारिक लिंक वा नयाँ अस्थायी पासवर्ड पठाउनेछौं।"
                            else
                                "Enter your registered email address to receive password reset or verification instructions.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        OutlinedTextField(
                            value = forgotEmail,
                            onValueChange = { forgotEmail = it },
                            label = { Text(if (isNepali) "दर्ता गरिएको इमेल ठेगाना *" else "Registered Email *") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = BrandIndigo) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            modifier = Modifier.fillMaxWidth().testTag("forgot_email_input")
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        if (forgotSuccessMsg != null) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = JadeGreen)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = forgotSuccessMsg ?: "",
                                        color = Color(0xFF2E7D32),
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }

                        Button(
                            onClick = {
                                if (forgotEmail.isBlank()) {
                                    errorMessage = if (isNepali) "कृपया आफ्नो इमेल ठेगाना लेख्नुहोस्" else "Please enter your email address"
                                    return@Button
                                }
                                isLoading = true
                                errorMessage = null
                                forgotSuccessMsg = null
                                viewModel.resetPassword(forgotEmail) { success, err ->
                                    isLoading = false
                                    if (success) {
                                        forgotSuccessMsg = if (isNepali)
                                            "पासवर्ड रिसेट लिंक तपाईंको इमेल ($forgotEmail) मा पठाइयो! कृपया इनबक्स वा Spam फोल्डर हेर्नुहोस्।"
                                        else
                                            "Reset link sent to $forgotEmail! Check your inbox or Spam folder."
                                    } else {
                                        errorMessage = err
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = BrandSaffron),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().height(50.dp).testTag("forgot_submit_button"),
                            enabled = !isLoading
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                            } else {
                                Text(
                                    text = if (isNepali) "🚀 पासवर्ड रिसेट लिंक पठाउनुहोस्" else "Send Reset Link",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Free Firebase Email Verification Button
                        OutlinedButton(
                            onClick = {
                                if (forgotEmail.isBlank()) {
                                    errorMessage = if (isNepali) "कृपया इमेल लेख्नुहोस्" else "Please enter email"
                                    return@OutlinedButton
                                }
                                isLoading = true
                                errorMessage = null
                                forgotSuccessMsg = null
                                viewModel.sendEmailVerification { success, err ->
                                    isLoading = false
                                    forgotSuccessMsg = if (isNepali)
                                        "✉️ निशुल्क Firebase इमेल प्रमाणीकरण लिंक ($forgotEmail) मा पठाइयो! कृपया इनबक्स प्रमाणीकरण गर्नुहोस्।"
                                    else
                                        "✉️ Firebase email verification sent to $forgotEmail! Please check your inbox."
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BrandIndigo)
                        ) {
                            Icon(Icons.Default.Email, contentDescription = null, tint = BrandIndigo, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isNepali) "✉️ निशुल्क इमेल प्रमाणीकरण पठाउनुहोस्" else "Send Free Email Verification",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = BrandIndigo
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(12.dp))

                        // Return to login
                        OutlinedButton(
                            onClick = {
                                selectedTab = 0
                                errorMessage = null
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("back_to_login_btn")
                        ) {
                            Text(
                                text = if (isNepali) "🔙 लगइन स्क्रिनमा फर्कनुहोस्" else "Back to Login",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun RegCardRow(
    label: String,
    value: String,
    isBold: Boolean = false,
    color: Color = Color.Unspecified
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            color = color
        )
    }
}
