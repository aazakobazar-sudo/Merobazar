package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.VerifiedUser
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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
import com.example.ui.components.WavingNepalFlag
import com.example.ui.theme.BrandBgLight
import com.example.ui.theme.BrandIndigo
import com.example.ui.theme.BrandIndigoLight
import com.example.ui.theme.BrandSaffron
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.DarkCrimson
import com.example.ui.theme.JadeGreen
import kotlinx.coroutines.delay

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

    // 4 Distinct Sequential Screens:
    // 1 = पहिलो पृष्ठ (Landing Page: Branding, Map Explore, Google Login, Login, Sign Up)
    // 2 = दोस्रो पृष्ठ (Selection & Profile Setup: Login form OR Role, Avatar, Docs & Mandatory Map Tagging)
    // 3 = तेस्रो पृष्ठ (Pre-OTP Review & Verification Flow)
    // 4 = चौथो पृष्ठ (Password Creation & Force Manual Login)
    var currentStep by remember { mutableIntStateOf(1) }
    var isLoginMode by remember { mutableStateOf(false) }

    // Screen 2 Login state
    var loginEmail by remember { mutableStateOf("") }
    var loginPass by remember { mutableStateOf("") }
    var loginPasswordVisible by remember { mutableStateOf(false) }

    // Screen 2 Registration state (Personal & Profile info)
    var regName by remember { mutableStateOf("") }
    var regPhone by remember { mutableStateOf("") }
    var regEmail by remember { mutableStateOf("") }
    var regDob by remember { mutableStateOf("2055-04-12") }
    var regGender by remember { mutableStateOf("पुरुष (Male)") }
    var regRole by remember { mutableStateOf("c") } // "c" = Customer, "m" = Merchant
    var selectedAvatarIndex by remember { mutableIntStateOf(0) }
    val avatarIcons = listOf("👤", "🧑‍💼", "👨‍🌾", "👩‍💻", "🏪")
    var docUploadedName by remember { mutableStateOf<String?>(null) }

    // Screen 2 Address & Mandatory Map Tagging
    var regProv by remember { mutableStateOf("Bagmati") }
    val districtsForProv = MeroBazarSeedData.provinces[regProv] ?: listOf("Kathmandu")
    var regDist by remember { mutableStateOf("Kathmandu") }
    var regLocal by remember { mutableStateOf("काठमाडौँ महानगरपालिका") }
    var regWard by remember { mutableStateOf("10") }
    var regToleRoad by remember { mutableStateOf("नयाँ बानेश्वर मार्ग") }
    var regGpsLocation by remember { mutableStateOf("27.7172, 85.3240") }
    var isLocationTaggedOnMap by remember { mutableStateOf(false) }
    var showQuickMapDialog by remember { mutableStateOf(false) }

    // Screen 2 Merchant fields
    var regShopName by remember { mutableStateOf("") }
    var regShopCatId by remember { mutableIntStateOf(1) }
    var regShopType by remember { mutableStateOf("किराना") }
    var regPan by remember { mutableStateOf("") }
    var regLrn by remember { mutableStateOf("") }
    var regPaymentMethod by remember { mutableStateOf("eSewa") }
    var regRef by remember { mutableStateOf("ESW-981245") }

    // Screen 3 Pre-OTP state
    var isOtpSent by remember { mutableStateOf(false) }
    var generatedOtp by remember { mutableStateOf("748291") }
    var regOtpInput by remember { mutableStateOf("") }
    var otpTimerSeconds by remember { mutableIntStateOf(60) }
    var isOtpTimerRunning by remember { mutableStateOf(false) }
    var isOtpVerified by remember { mutableStateOf(false) }

    // Screen 4 Password state
    var regPass by remember { mutableStateOf("") }
    var regPassConfirm by remember { mutableStateOf("") }
    var regPasswordVisible by remember { mutableStateOf(false) }

    // Dialogs & Feedback
    var showForgotPasswordDialog by remember { mutableStateOf(false) }
    var forgotEmail by remember { mutableStateOf("") }
    var showRegistrationCardDialog by remember { mutableStateOf(false) }
    var newlyCreatedProfile by remember { mutableStateOf<UserProfile?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var infoMessage by remember { mutableStateOf<String?>(null) }

    // Countdown Timer for OTP
    LaunchedEffect(isOtpTimerRunning, otpTimerSeconds) {
        if (isOtpTimerRunning && otpTimerSeconds > 0) {
            delay(1000L)
            otpTimerSeconds -= 1
        } else if (otpTimerSeconds == 0) {
            isOtpTimerRunning = false
        }
    }

    // Auto-navigate if already authenticated
    LaunchedEffect(currentUser) {
        if (currentUser != null) {
            onAuthSuccess()
        }
    }

    // Registration Card Dialog (दर्ता प्रमाण पत्र)
    if (showRegistrationCardDialog && newlyCreatedProfile != null) {
        val u = newlyCreatedProfile!!
        AlertDialog(
            onDismissRequest = {
                showRegistrationCardDialog = false
                // Force user to log in manually with their new password
                isLoginMode = true
                currentStep = 2
                loginEmail = u.email
                loginPass = ""
                infoMessage = if (isNepali)
                    "✅ खाता सफलतापूर्वक बन्यो! सुरक्षाको लागि कृपया आफ्नो इमेल र नयाँ पासवर्ड हालेर लगइन गर्नुहोस्।"
                else
                    "✅ Account created! For security, please enter your new password to sign in."
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("📜", fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = if (isNepali) "मेरो बजार आधिकारिक दर्ता प्रमाण" else "Mero Bazar Registration Card",
                            fontWeight = FontWeight.ExtraBold,
                            color = BrandIndigo,
                            fontSize = 16.sp
                        )
                        Text(
                            text = if (isNepali) "सुरक्षा दर्ता रेकर्ड कार्ड (Security Card)" else "Official Security Record",
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
                                "Keep this registration code safely for account verification and future support.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF8A4B00),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(10.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDCE5F1)),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            RegCardRow("खाता कोड (Code)", u.code, isBold = true, color = BrandIndigo)
                            RegCardRow("दर्ता मिति (Date)", u.regDate.ifBlank { "आज" })
                            RegCardRow("खाता प्रकार (Role)", when (u.role) {
                                "m" -> "🏪 व्यापारी (Merchant)"
                                "a" -> "👑 एडमिन (Admin)"
                                else -> "👤 ग्राहक (Customer)"
                            })
                            RegCardRow("नाम (Name)", u.name)
                            RegCardRow("मोबाइल (Mobile)", u.phone)
                            RegCardRow("इमेल (Email)", u.email)
                            RegCardRow("ठेगाना (Address)", "${u.prov}, ${u.dist}, ${u.local}, वडा ${u.ward}")
                            RegCardRow("नक्सा GPS", "${u.la}, ${u.lo}", isBold = true, color = JadeGreen)

                            if (u.shop != null) {
                                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                                RegCardRow("पसलको नाम (Shop)", u.shop.name)
                                if (u.shop.pan.isNotBlank()) RegCardRow("PAN (प्यान)", u.shop.pan)
                                if (u.shop.lrn.isNotBlank()) RegCardRow("दर्ता नं. (LRN)", u.shop.lrn)
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                            RegCardRow(
                                "🎁 स्वागत उपहार",
                                "५० क्वाइन + रु. १०० वालेट",
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
                        isLoginMode = true
                        currentStep = 2
                        loginEmail = u.email
                        loginPass = ""
                        infoMessage = if (isNepali)
                            "✅ खाता सफलतापूर्वक बन्यो! सुरक्षाको लागि कृपया आफ्नो इमेल र नयाँ पासवर्ड हालेर लगइन गर्नुहोस्।"
                        else
                            "✅ Account created! For security, please enter your new password to sign in."
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandIndigo),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("reg_card_proceed_login_button")
                ) {
                    Text(if (isNepali) "लगइन गर्नुहोस् ➔" else "Proceed to Login ➔", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        clipboardManager.setText(AnnotatedString("खाता कोड: ${u.code} | नाम: ${u.name} | इमेल: ${u.email}"))
                        Toast.makeText(context, if (isNepali) "खाता कोड कपी गरियो!" else "Copied to clipboard!", Toast.LENGTH_SHORT).show()
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

    // Quick Map Sheet Selection Dialog for Step 2 Tagging
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
                        text = if (isNepali) "🗺️ नेपाल नक्सा लोकेसन ट्याग" else "Nepal Map Location Tagging",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .fillMaxWidth()
                ) {
                    Text(
                        text = if (isNepali)
                            "नक्सा सिट र GPS अनुसार आफ्नो पसल वा डेलिभरी लोकेसन छान्नुहोस्:"
                        else
                            "Select and pin your exact shop or delivery coordinates on the Nepal map:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        quickPlaces.forEach { (pTitle, pProv, pDist) ->
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

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = regToleRoad,
                        onValueChange = { regToleRoad = it },
                        label = { Text(if (isNepali) "बाटो / सडक / टोल *" else "Road / Tole *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("map_dialog_road_input")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = regWard,
                        onValueChange = { regWard = it },
                        label = { Text(if (isNepali) "वडा नं. *" else "Ward No. *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("map_dialog_ward_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        isLocationTaggedOnMap = true
                        showQuickMapDialog = false
                        Toast.makeText(context, if (isNepali) "✅ नक्सा लोकेसन सफलतापूर्वक ट्याग गरियो!" else "Location Tagged!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = JadeGreen),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("confirm_map_location_button")
                ) {
                    Text(if (isNepali) "📍 यो लोकेसन ट्याग गर्नुहोस्" else "Tag This Location", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showQuickMapDialog = false
                        onOpenNepalMap()
                    }
                ) {
                    Text(if (isNepali) "पूरा नक्सा हेर्नुहोस् ➔" else "Open Full Map ➔", color = BrandIndigo)
                }
            }
        )
    }

    // Forgot Password Dialog
    if (showForgotPasswordDialog) {
        AlertDialog(
            onDismissRequest = { showForgotPasswordDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LockReset, contentDescription = null, tint = BrandIndigo)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isNepali) "पासवर्ड रिसेट गर्नुहोस्" else "Reset Password", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column {
                    Text(
                        text = if (isNepali)
                            "आफ्नो दर्ता गरिएको इमेल हाल्नुहोस्। पासवर्ड रिसेट लिङ्क पठाइनेछ:"
                        else
                            "Enter your registered email to receive password reset instructions:",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = forgotEmail,
                        onValueChange = { forgotEmail = it },
                        label = { Text(if (isNepali) "इमेल ठेगाना" else "Email Address") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("forgot_password_email_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (forgotEmail.isNotBlank()) {
                            viewModel.resetPassword(forgotEmail.trim()) { ok, msg ->
                                if (ok) {
                                    Toast.makeText(context, if (isNepali) "रिसेट लिङ्क पठाइयो!" else "Reset email sent!", Toast.LENGTH_SHORT).show()
                                    showForgotPasswordDialog = false
                                } else {
                                    Toast.makeText(context, msg ?: "Error", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed)
                ) {
                    Text(if (isNepali) "लिङ्क पठाउनुहोस्" else "Send Reset Link")
                }
            },
            dismissButton = {
                TextButton(onClick = { showForgotPasswordDialog = false }) {
                    Text(if (isNepali) "रद्द गर्नुहोस्" else "Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.logo_mero_bazar),
                            contentDescription = "मेरो बजार आधिकारिक लोगो",
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isNepali) "मेरो बजार" else "Mero Bazar",
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }
                },
                navigationIcon = {
                    if (currentStep > 1) {
                        IconButton(
                            onClick = {
                                if (currentStep == 2) {
                                    currentStep = 1
                                } else {
                                    currentStep -= 1
                                }
                            },
                            modifier = Modifier.testTag("auth_step_back_button")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.toggleLanguage() },
                        modifier = Modifier.testTag("auth_lang_toggle")
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = if (isNepali) "EN" else "नेपाली",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkCrimson)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(BrandBgLight)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Error / Info Message banners
            errorMessage?.let { msg ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = CrimsonRed.copy(alpha = 0.12f)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("⚠️", fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(msg, color = CrimsonRed, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }

            infoMessage?.let { msg ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = JadeGreen.copy(alpha = 0.12f)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("✅", fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(msg, color = JadeGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }

            when (currentStep) {
                // ==============================================================
                // १. पहिलो पृष्ठ (First Screen / Landing Page)
                // ==============================================================
                1 -> {
                    // Logo & Branding Banner
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(DarkCrimson, CrimsonRed, BrandIndigo)
                                    )
                                )
                                .padding(20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                // Official Mero Bazar Logo
                                Image(
                                    painter = painterResource(id = R.drawable.logo_mero_bazar),
                                    contentDescription = "मेरो बजार आधिकारिक लोगो",
                                    modifier = Modifier
                                        .size(105.dp)
                                        .clip(CircleShape)
                                        .border(2.5.dp, Color.White, CircleShape)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    WavingNepalFlag(modifier = Modifier.size(24.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (isNepali) "मेरो बजार" else "Mero Bazar",
                                        fontSize = 28.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color.White
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = if (isNepali)
                                        "नेपाली डिजिटल बजार — अनलाइन च्याट, भिडियो कल र नक्सा लोकेसन प्रणाली"
                                    else
                                        "Nepali Online Market — Realtime Chat, Video Call & Nepal Map",
                                    color = Color.White.copy(alpha = 0.95f),
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Dedicated Nepal Map Explorer Section (नक्सा हेर्नुहोस् सेक्सन)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenNepalMap() }
                            .testTag("explore_nepal_map_card"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF93C5FD))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = BrandIndigo,
                                modifier = Modifier.size(46.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Map, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                                }
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isNepali) "🗺️ नेपाल नक्सा हेर्नुहोस्" else "🗺️ Explore Nepal Map",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = BrandIndigo
                                )
                                Text(
                                    text = if (isNepali)
                                        "७ प्रदेश, ७७ जिल्लाका पसलहरू, उत्पादन र आफ्नो लोकेसन ट्र्याक गर्नुहोस्"
                                    else
                                        "Find merchant shops, locations & tracks across 7 provinces",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = BrandIndigo)
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Three Primary Options (मुख्य विकल्पहरू तीनवटा स्पष्ट देखिनुपर्छ)
                    Text(
                        text = if (isNepali) "प्रवेश वा खाता छान्नुहोस्:" else "Choose an option to enter:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.align(Alignment.Start).padding(start = 4.dp, bottom = 8.dp)
                    )

                    // 1. Google Login
                    Button(
                        onClick = {
                            errorMessage = null
                            val activity = context as? android.app.Activity
                            if (activity != null) {
                                isLoading = true
                                viewModel.signInWithGoogle(
                                    activity = activity,
                                    onSuccess = {
                                        isLoading = false
                                        onAuthSuccess()
                                    },
                                    onError = { msg ->
                                        isLoading = false
                                        errorMessage = msg
                                    }
                                )
                            } else {
                                Toast.makeText(context, "Activity not available", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("google_login_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD1D5DB)),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🌐", fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = if (isNepali) "Google Login (गुगलबाट लगइन)" else "Sign in with Google",
                                color = Color(0xFF1F2937),
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 2. लगइन (Login) -> Opens Screen 2 in Login Mode
                    Button(
                        onClick = {
                            errorMessage = null
                            isLoginMode = true
                            currentStep = 2
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("landing_login_option_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandIndigo),
                        shape = RoundedCornerShape(12.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Key, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = if (isNepali) "लगइन (Login)" else "Login (Sign In)",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 3. नयाँ दर्ता (Sign Up) -> Opens Screen 2 in Sign-up Mode
                    Button(
                        onClick = {
                            errorMessage = null
                            isLoginMode = false
                            currentStep = 2
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("landing_signup_option_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed),
                        shape = RoundedCornerShape(12.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = if (isNepali) "नयाँ दर्ता (Sign Up)" else "Sign Up (Register)",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                    }
                }

                // ==============================================================
                // २. दोस्रो पृष्ठ (Second Screen / Selection & Profile Setup)
                // ==============================================================
                2 -> {
                    if (isLoginMode) {
                        // ------------------------------------------------------
                        // Login Mode Form
                        // ------------------------------------------------------
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = if (isNepali) "🔑 मेरो बजार लगइन" else "🔑 Mero Bazar Login",
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = BrandIndigo
                                        )
                                        Text(
                                            text = if (isNepali) "आफ्नो इमेल वा फोन नम्बर हाल्नुहोस्" else "Sign in with your email or phone",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Image(
                                        painter = painterResource(id = R.drawable.logo_mero_bazar),
                                        contentDescription = "मेरो बजार आधिकारिक लोगो",
                                        modifier = Modifier
                                            .size(56.dp)
                                            .clip(CircleShape)
                                            .border(1.5.dp, BrandIndigo.copy(alpha = 0.3f), CircleShape)
                                    )
                                }

                                Spacer(modifier = Modifier.height(18.dp))

                                OutlinedTextField(
                                    value = loginEmail,
                                    onValueChange = { loginEmail = it },
                                    label = { Text(if (isNepali) "इमेल वा फोन नम्बर" else "Email or Phone") },
                                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth().testTag("login_email_input")
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                OutlinedTextField(
                                    value = loginPass,
                                    onValueChange = { loginPass = it },
                                    label = { Text(if (isNepali) "पासवर्ड" else "Password") },
                                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                                    trailingIcon = {
                                        IconButton(onClick = { loginPasswordVisible = !loginPasswordVisible }) {
                                            Icon(
                                                imageVector = if (loginPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                                contentDescription = null
                                            )
                                        }
                                    },
                                    visualTransformation = if (loginPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth().testTag("login_password_input")
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    TextButton(onClick = { showForgotPasswordDialog = true }) {
                                        Text(
                                            text = if (isNepali) "पासवर्ड बिर्सनुभयो? (Forgot?)" else "Forgot Password?",
                                            color = CrimsonRed,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Button(
                                    onClick = {
                                        errorMessage = null
                                        if (loginEmail.isBlank() || loginPass.isBlank()) {
                                            errorMessage = if (isNepali) "कृपया इमेल र पासवर्ड दुवै हाल्नुहोस्!" else "Enter email and password"
                                            return@Button
                                        }
                                        isLoading = true
                                        viewModel.signInWithEmail(loginEmail.trim(), loginPass.trim()) { ok, msg ->
                                            isLoading = false
                                            if (ok) {
                                                onAuthSuccess()
                                            } else {
                                                errorMessage = msg ?: "Login failed"
                                            }
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp)
                                        .testTag("submit_login_button"),
                                    colors = ButtonDefaults.buttonColors(containerColor = BrandIndigo),
                                    shape = RoundedCornerShape(12.dp),
                                    enabled = !isLoading
                                ) {
                                    if (isLoading) {
                                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                                    } else {
                                        Text(if (isNepali) "लगइन गर्नुहोस् ➔" else "Sign In ➔", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                HorizontalDivider()

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (isNepali) "खाता छैन? " else "Don't have an account? ",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    Text(
                                        text = if (isNepali) "नयाँ दर्ता गर्नुहोस्" else "Sign Up",
                                        color = CrimsonRed,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.clickable {
                                            errorMessage = null
                                            isLoginMode = false
                                        }
                                    )
                                }
                            }
                        }
                    } else {
                        // ------------------------------------------------------
                        // Sign-Up Mode: Role, Avatar, Docs & Mandatory Map Tagging
                        // ------------------------------------------------------
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                // Stepper Header
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (isNepali) "चरण २/४: विवरण र लोकेसन सेटअप" else "Step 2 of 4: Profile & Map Setup",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 16.sp,
                                        color = BrandIndigo
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = BrandIndigo.copy(alpha = 0.1f)
                                    ) {
                                        Text(
                                            text = "५०% पूरा",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = BrandIndigo,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // Role Selection (व्यापारी vs ग्राहक)
                                Text(
                                    text = if (isNepali) "खाता प्रकार छान्नुहोस् (Choose Role) *" else "Select Account Role *",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    // Customer Role Card
                                    Card(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { regRole = "c" }
                                            .testTag("role_customer_card"),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (regRole == "c") BrandIndigo.copy(alpha = 0.1f) else Color(0xFFF8FAFC)
                                        ),
                                        border = androidx.compose.foundation.BorderStroke(
                                            width = if (regRole == "c") 2.dp else 1.dp,
                                            color = if (regRole == "c") BrandIndigo else Color(0xFFCBD5E1)
                                        )
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(12.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text("👤", fontSize = 28.sp)
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = if (isNepali) "ग्राहक (Customer)" else "Customer",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = if (regRole == "c") BrandIndigo else Color.Unspecified
                                            )
                                            Text(
                                                text = if (isNepali) "किनमेल र अर्डर" else "Buy & Shop",
                                                fontSize = 10.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    // Merchant Role Card
                                    Card(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { regRole = "m" }
                                            .testTag("role_merchant_card"),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (regRole == "m") CrimsonRed.copy(alpha = 0.1f) else Color(0xFFF8FAFC)
                                        ),
                                        border = androidx.compose.foundation.BorderStroke(
                                            width = if (regRole == "m") 2.dp else 1.dp,
                                            color = if (regRole == "m") CrimsonRed else Color(0xFFCBD5E1)
                                        )
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(12.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text("🏪", fontSize = 28.sp)
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = if (isNepali) "व्यापारी (Merchant)" else "Merchant",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = if (regRole == "m") CrimsonRed else Color.Unspecified
                                            )
                                            Text(
                                                text = if (isNepali) "पसल र सामान बिक्री" else "Sell & Manage",
                                                fontSize = 10.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // Profile Picture Avatar Selector & Document Upload
                                Text(
                                    text = if (isNepali) "प्रोफाइल अवतार र कागजात सेटअप:" else "Profile Avatar & Documents:",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Avatars row
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        avatarIcons.forEachIndexed { idx, emo ->
                                            Box(
                                                modifier = Modifier
                                                    .size(40.dp)
                                                    .clip(CircleShape)
                                                    .background(if (selectedAvatarIndex == idx) BrandIndigo else Color(0xFFE2E8F0))
                                                    .clickable { selectedAvatarIndex = idx },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(emo, fontSize = 20.sp)
                                            }
                                        }
                                    }

                                    // Document upload simulator button
                                    OutlinedButton(
                                        onClick = {
                                            docUploadedName = if (regRole == "m") "PAN_Darta_Certificate.pdf" else "Nagarikta_Proof.jpg"
                                            Toast.makeText(context, if (isNepali) "कागजात सफलतापूर्वक अपलोड भयो!" else "Document attached!", Toast.LENGTH_SHORT).show()
                                        },
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Icon(Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (docUploadedName != null) "✓ संलग्न भयो" else (if (isNepali) "कागजात अपलोड" else "Attach Doc"),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (docUploadedName != null) JadeGreen else MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // Mandatory Nepal Map Tagging Section
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isLocationTaggedOnMap) Color(0xFFF0FDF4) else Color(0xFFFFF1F2)
                                    ),
                                    border = androidx.compose.foundation.BorderStroke(
                                        width = 1.5.dp,
                                        color = if (isLocationTaggedOnMap) JadeGreen else CrimsonRed
                                    )
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.LocationOn,
                                                    contentDescription = null,
                                                    tint = if (isLocationTaggedOnMap) JadeGreen else CrimsonRed
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = if (isNepali) "नक्सा लोकेसन ट्याग (अनिवार्य) *" else "Map Location Tag (Mandatory) *",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp,
                                                    color = if (isLocationTaggedOnMap) JadeGreen else CrimsonRed
                                                )
                                            }

                                            Button(
                                                onClick = { showQuickMapDialog = true },
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = if (isLocationTaggedOnMap) JadeGreen else CrimsonRed
                                                ),
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.testTag("tag_location_on_map_button")
                                            ) {
                                                Text(
                                                    text = if (isLocationTaggedOnMap) "✓ ट्याग भयो" else (if (isNepali) "🗺️ नक्सामा ट्याग" else "Tag Map"),
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = if (isLocationTaggedOnMap)
                                                "📍 ट्याग गरिएको: $regDist ($regLocal, वडा $regWard) GPS: $regGpsLocation"
                                            else
                                                "⚠️ ग्राहक र व्यापारीले पसल वा डेलिभरी लोकेसन खोज्नका लागि नक्सा ट्याग अनिवार्य गरिएको छ।",
                                            fontSize = 11.sp,
                                            color = if (isLocationTaggedOnMap) Color(0xFF166534) else Color(0xFF991B1B)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // Basic Personal Fields
                                OutlinedTextField(
                                    value = regName,
                                    onValueChange = { regName = it },
                                    label = { Text(if (isNepali) "पूरा नाम (Full Name) *" else "Full Name *") },
                                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth().testTag("reg_name_input")
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                OutlinedTextField(
                                    value = regPhone,
                                    onValueChange = { regPhone = it },
                                    label = { Text(if (isNepali) "मोबाइल नम्बर (Mobile No.) *" else "Mobile Number *") },
                                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth().testTag("reg_phone_input")
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                OutlinedTextField(
                                    value = regEmail,
                                    onValueChange = { regEmail = it },
                                    label = { Text(if (isNepali) "इमेल ठेगाना (Email) *" else "Email Address *") },
                                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth().testTag("reg_email_input")
                                )

                                // If Merchant: Shop details
                                if (regRole == "m") {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Card(
                                        shape = RoundedCornerShape(10.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFDE68A)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.Storefront, contentDescription = null, tint = Color(0xFFB45309))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = if (isNepali) "पसल विवरण (Shop Details):" else "Shop Details:",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 12.sp,
                                                    color = Color(0xFFB45309)
                                                )
                                            }

                                            Spacer(modifier = Modifier.height(8.dp))

                                            OutlinedTextField(
                                                value = regShopName,
                                                onValueChange = { regShopName = it },
                                                label = { Text(if (isNepali) "पसलको नाम (Shop Name) *" else "Shop Name *") },
                                                singleLine = true,
                                                modifier = Modifier.fillMaxWidth().testTag("reg_shop_name_input")
                                            )

                                            Spacer(modifier = Modifier.height(8.dp))

                                            OutlinedTextField(
                                                value = regPan,
                                                onValueChange = { regPan = it },
                                                label = { Text(if (isNepali) "PAN नम्बर / दर्ता नं." else "PAN / Reg Number") },
                                                singleLine = true,
                                                modifier = Modifier.fillMaxWidth().testTag("reg_pan_input")
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(18.dp))

                                // Proceed to Step 3 Button
                                Button(
                                    onClick = {
                                        errorMessage = null
                                        if (regName.isBlank()) {
                                            errorMessage = if (isNepali) "कृपया आफ्नो नाम हाल्नुहोस्!" else "Enter full name"
                                            return@Button
                                        }
                                        if (regPhone.isBlank()) {
                                            errorMessage = if (isNepali) "कृपया मोबाइल नम्बर हाल्नुहोस्!" else "Enter phone number"
                                            return@Button
                                        }
                                        if (regEmail.isBlank()) {
                                            errorMessage = if (isNepali) "कृपया इमेल हाल्नुहोस्!" else "Enter email address"
                                            return@Button
                                        }
                                        if (regRole == "m" && regShopName.isBlank()) {
                                            errorMessage = if (isNepali) "कृपया पसलको नाम हाल्नुहोस्!" else "Enter shop name"
                                            return@Button
                                        }
                                        // Mandatory map check
                                        if (!isLocationTaggedOnMap) {
                                            errorMessage = if (isNepali)
                                                "⚠️ नक्सामा आफ्नो लोकेसन ट्याग गर्न अनिवार्य छ! माथिको '🗺️ नक्सामा ट्याग' थिच्नुहोस्।"
                                            else
                                                "Map location tagging is mandatory! Click 'Tag Map' button above."
                                            return@Button
                                        }

                                        // Proceed to Step 3
                                        currentStep = 3
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp)
                                        .testTag("proceed_to_step_3_button"),
                                    colors = ButtonDefaults.buttonColors(containerColor = BrandIndigo),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(if (isNepali) "तेस्रो पृष्ठमा जानुहोस् (समीक्षा) ➔" else "Proceed to Step 3 (Review) ➔", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }

                // ==============================================================
                // ३. तेस्रो पृष्ठ (Third Screen / Pre-OTP & Verification Flow)
                // ==============================================================
                3 -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            // Stepper Header
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isNepali) "चरण ३/४: विवरण समीक्षा र OTP" else "Step 3 of 4: Review & OTP",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 16.sp,
                                    color = BrandIndigo
                                )
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = BrandIndigo.copy(alpha = 0.1f)
                                ) {
                                    Text(
                                        text = "७५% पूरा",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BrandIndigo,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Review Card of Screen 2 details
                            Text(
                                text = if (isNepali) "तपाईंले भर्नुभएको विवरण समीक्षा गर्नुहोस्:" else "Review Entered Details:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                color = Color(0xFFF8FAFC)
                            ) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    RegCardRow("खाता प्रकार (Role)", if (regRole == "m") "🏪 व्यापारी (Merchant)" else "👤 ग्राहक (Customer)")
                                    RegCardRow("पूरा नाम (Name)", regName)
                                    RegCardRow("मोबाइल (Phone)", regPhone)
                                    RegCardRow("इमेल (Email)", regEmail)
                                    RegCardRow("ठेगाना (Address)", "$regProv, $regDist, $regLocal, वडा $regWard")
                                    RegCardRow("टोल / बाटो", regToleRoad)
                                    RegCardRow("नक्सा स्थिति", "✅ ट्याग प्रमाणित ($regGpsLocation)", color = JadeGreen, isBold = true)
                                    if (regRole == "m") {
                                        RegCardRow("पसलको नाम", regShopName)
                                        if (regPan.isNotBlank()) RegCardRow("PAN", regPan)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Pre-OTP Submit Button: Only when clicked does system send OTP
                            if (!isOtpSent) {
                                Button(
                                    onClick = {
                                        isOtpSent = true
                                        isOtpTimerRunning = true
                                        otpTimerSeconds = 60
                                        generatedOtp = "748291"
                                        infoMessage = if (isNepali)
                                            "✅ ६-अंकको OTP कोड तपाईंको मोबाइल ($regPhone) र इमेल ($regEmail) मा पठाइयो! कोड: 748291"
                                        else
                                            "✅ 6-digit OTP sent to $regPhone and $regEmail! Code: 748291"
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp)
                                        .testTag("send_otp_button"),
                                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Security, contentDescription = null)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(if (isNepali) "विवरण सबमिट र ६-अंकको OTP पठाउनुहोस्" else "Submit Details & Send OTP", fontWeight = FontWeight.Bold)
                                    }
                                }
                            } else {
                                // OTP Verification Box
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBFDBFE))
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Text(
                                            text = if (isNepali) "६-अंकको OTP कोड हाल्नुहोस् (Enter OTP):" else "Enter 6-digit OTP code:",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = BrandIndigo
                                        )

                                        Spacer(modifier = Modifier.height(8.dp))

                                        OutlinedTextField(
                                            value = regOtpInput,
                                            onValueChange = { if (it.length <= 6) regOtpInput = it },
                                            placeholder = { Text("748291") },
                                            leadingIcon = { Icon(Icons.Default.Key, contentDescription = null) },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            singleLine = true,
                                            modifier = Modifier.fillMaxWidth().testTag("otp_input_field")
                                        )

                                        Spacer(modifier = Modifier.height(10.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = if (otpTimerSeconds > 0)
                                                    "समय बाँकी: ००:${if (otpTimerSeconds < 10) "०$otpTimerSeconds" else otpTimerSeconds}"
                                                else
                                                    "समय सकियो!",
                                                fontSize = 12.sp,
                                                color = if (otpTimerSeconds > 0) Color(0xFF1E40AF) else CrimsonRed,
                                                fontWeight = FontWeight.Bold
                                            )

                                            TextButton(
                                                onClick = {
                                                    otpTimerSeconds = 60
                                                    isOtpTimerRunning = true
                                                    Toast.makeText(context, if (isNepali) "नयाँ कोड पठाइयो: 748291" else "OTP Resent: 748291", Toast.LENGTH_SHORT).show()
                                                },
                                                enabled = otpTimerSeconds == 0
                                            ) {
                                                Text(if (isNepali) "पुन: पठाउनुहोस्" else "Resend OTP", fontWeight = FontWeight.Bold)
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))

                                        Button(
                                            onClick = {
                                                errorMessage = null
                                                if (regOtpInput.trim() == generatedOtp || regOtpInput.trim() == "748291") {
                                                    isOtpVerified = true
                                                    infoMessage = if (isNepali) "✅ OTP कोड प्रमाणित भयो!" else "OTP verified!"
                                                    currentStep = 4
                                                } else {
                                                    errorMessage = if (isNepali) "गलत OTP कोड! कृपया 748291 हाल्नुहोस्।" else "Invalid OTP code"
                                                }
                                            },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(48.dp)
                                                .testTag("verify_otp_button"),
                                            colors = ButtonDefaults.buttonColors(containerColor = JadeGreen),
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.CheckCircle, contentDescription = null)
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(if (isNepali) "ओटिपी प्रमाणित गर्नुहोस् (Verify OTP)" else "Verify OTP", fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // ==============================================================
                // ४. चौथो पृष्ठ (Fourth Screen / Password Creation & Direct Login Flow)
                // ==============================================================
                4 -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            // Stepper Header
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isNepali) "चरण ४/४: पासवर्ड सिर्जना र खाता सम्पन्न" else "Step 4 of 4: Password & Completion",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 16.sp,
                                    color = BrandIndigo
                                )
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = JadeGreen.copy(alpha = 0.1f)
                                ) {
                                    Text(
                                        text = "१००% अन्तिम चरण",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = JadeGreen,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Text(
                                text = if (isNepali) "🔐 आफ्नो खाताको लागि बलियो पासवर्ड सिर्जना गर्नुहोस्:" else "Create a secure password for your account:",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Password Field
                            OutlinedTextField(
                                value = regPass,
                                onValueChange = { regPass = it },
                                label = { Text(if (isNepali) "नयाँ पासवर्ड (Password) *" else "New Password *") },
                                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                                trailingIcon = {
                                    IconButton(onClick = { regPasswordVisible = !regPasswordVisible }) {
                                        Icon(
                                            imageVector = if (regPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                            contentDescription = null
                                        )
                                    }
                                },
                                visualTransformation = if (regPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("reg_password_input")
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Confirm Password Field
                            OutlinedTextField(
                                value = regPassConfirm,
                                onValueChange = { regPassConfirm = it },
                                label = { Text(if (isNepali) "पासवर्ड पुन: हाल्नुहोस् (Confirm) *" else "Confirm Password *") },
                                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                                visualTransformation = if (regPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("reg_confirm_password_input")
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Real-time match indicator
                            if (regPass.isNotEmpty() && regPassConfirm.isNotEmpty()) {
                                if (regPass == regPassConfirm) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = JadeGreen, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(if (isNepali) "✅ दुवै पासवर्ड मिल्यो" else "Passwords match", color = JadeGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                } else {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("❌", fontSize = 12.sp)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(if (isNepali) "दुवै पासवर्ड एकै हुनुपर्छ" else "Passwords do not match", color = CrimsonRed, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(18.dp))

                            // Security Notice: Explicitly notes manual login will be required
                            Card(
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = if (isNepali)
                                        "🛡️ सुरक्षा नीति: दर्ता सम्पन्न भएपछि तपाईंलाई सिधै भित्र नलगी लगइन स्क्रिनमा पठाइनेछ जहाँ तपाईंले आफ्नो नयाँ पासवर्ड हालेर सुरक्षित लगइन गर्नुपर्नेछ।"
                                    else
                                        "Security notice: After registration, for your safety you will be directed to login using your new password.",
                                    fontSize = 11.sp,
                                    color = Color(0xFF334155),
                                    modifier = Modifier.padding(10.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Finish Registration Button
                            Button(
                                onClick = {
                                    errorMessage = null
                                    if (regPass.length < 6) {
                                        errorMessage = if (isNepali) "पासवर्ड कम्तीमा ६ अक्षरको हुनुपर्छ!" else "Password must be at least 6 characters"
                                        return@Button
                                    }
                                    if (regPass != regPassConfirm) {
                                        errorMessage = if (isNepali) "दुवै पासवर्ड मिलेन!" else "Passwords do not match"
                                        return@Button
                                    }

                                    isLoading = true
                                    val shopDetails = if (regRole == "m") {
                                        ShopDetails(
                                            name = regShopName,
                                            cat = regShopCatId,
                                            type = regShopType,
                                            pan = regPan,
                                            lrn = regLrn,
                                            pm = regPaymentMethod,
                                            ref = regRef
                                        )
                                    } else null

                                    // Strictly autoSignIn = false to fulfill the security redirect requirement
                                    viewModel.signUpWithEmail(
                                        name = regName.trim(),
                                        email = regEmail.trim(),
                                        pass = regPass.trim(),
                                        role = regRole,
                                        phone = regPhone.trim(),
                                        shopDetails = shopDetails,
                                        prov = regProv,
                                        dist = regDist,
                                        local = regLocal,
                                        ward = regWard.toIntOrNull() ?: 1,
                                        toleRoad = regToleRoad,
                                        isMapTagged = isLocationTaggedOnMap,
                                        dob = regDob,
                                        gn = regGender,
                                        photo = avatarIcons.getOrElse(selectedAvatarIndex) { "👤" },
                                        la = 27.7172,
                                        lo = 85.3240,
                                        autoSignIn = false
                                    ) { success, msg, profile ->
                                        isLoading = false
                                        if (success && profile != null) {
                                            newlyCreatedProfile = profile
                                            showRegistrationCardDialog = true
                                        } else {
                                            errorMessage = msg ?: "Registration failed"
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .testTag("finish_registration_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = BrandIndigo),
                                shape = RoundedCornerShape(12.dp),
                                enabled = !isLoading
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                                } else {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.VerifiedUser, contentDescription = null)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = if (isNepali) "खाता सुरक्षित गर्नुहोस् र दर्ता सम्पन्न गर्नुहोस्" else "Finish Registration & Secure Account",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
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
