package com.example.ui.screens

import android.app.Activity
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material.icons.filled.MarkEmailUnread
import androidx.compose.material.icons.filled.Message
import android.widget.Toast
import com.example.ui.theme.AmberGold
import com.example.ui.theme.JadeGreen
import com.example.ui.theme.NepalBlue
import com.example.util.GoogleDocsHelper
import com.example.util.NepalCalendarHelper
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import com.example.ui.theme.CrimsonRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: MainViewModel,
    onNavigateToAuth: () -> Unit,
    onNavigateToOrders: () -> Unit
) {
    val context = LocalContext.current
    val isNepali by viewModel.isNepali.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    var showEditProfileDialog by remember { mutableStateOf(false) }
    var editName by remember { mutableStateOf(currentUser?.name ?: "") }
    var editPhone by remember { mutableStateOf(currentUser?.phone ?: "") }
    var editDist by remember { mutableStateOf(currentUser?.dist ?: "Kathmandu") }
    var editWard by remember { mutableStateOf(currentUser?.ward?.toString() ?: "10") }

    if (showEditProfileDialog) {
        AlertDialog(
            onDismissRequest = { showEditProfileDialog = false },
            title = { Text(if (isNepali) "विवरण सम्पादन गर्नुहोस्" else "Edit Profile") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text(if (isNepali) "नाम" else "Full Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editPhone,
                        onValueChange = { editPhone = it },
                        label = { Text(if (isNepali) "फोन नम्बर" else "Phone") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editDist,
                        onValueChange = { editDist = it },
                        label = { Text(if (isNepali) "जिल्ला" else "District") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editWard,
                        onValueChange = { editWard = it.filter { c -> c.isDigit() } },
                        label = { Text(if (isNepali) "वडा नं." else "Ward No.") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val w = editWard.toIntOrNull() ?: 10
                        viewModel.repo.updateProfile(editName, editPhone, "Bagmati", editDist, editDist, w)
                        showEditProfileDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed)
                ) {
                    Text(if (isNepali) "बचत गर्नुहोस्" else "Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditProfileDialog = false }) {
                    Text(if (isNepali) "रद्द" else "Cancel")
                }
            }
        )
    }

    var showRegCardDialog by remember { mutableStateOf(false) }

    if (showRegCardDialog && currentUser != null) {
        val u = currentUser!!
        AlertDialog(
            onDismissRequest = { showRegCardDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("📜", fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = if (isNepali) "मेरो बजार दर्ता प्रमाण" else "Mero Bazar Registration Record",
                            fontWeight = FontWeight.Bold,
                            color = CrimsonRed
                        )
                        Text(
                            text = "खाता कोड: ${u.code.ifBlank { "AB-C-10001" }}",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = if (isNepali)
                            "⚠️ यो तपाईंलाई पछि कुनै समस्या आएमा यही दर्ता प्रमाणको आधारमा तपाईंले रिपोर्ट गर्न सक्नुहुनेछ।"
                        else
                            "If you face any problem later, you can report it based on this registration record.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF8A4B00),
                        fontWeight = FontWeight.Bold
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    Text("• नाम: ${u.name}")
                    Text("• इमेल: ${u.email}")
                    Text("• मोबाइल: ${u.phone}")
                    Text("• लिङ्ग: ${u.gn}")
                    Text("• ठेगाना: ${u.prov}, ${u.dist}, ${u.local}, वडा ${u.ward}${if (u.toleRoad.isNotBlank()) ", बाटो: " + u.toleRoad else ""}")
                    Text(
                        text = "• नेपाल नक्सा: ${if (u.isMapTagged) "✅ प्रमाणित ट्याग (अफलाइन गुगल म्याप)" else "⚠️ नक्सा ट्याग गर्न बाँकी"}",
                        fontWeight = FontWeight.Bold,
                        color = if (u.isMapTagged) Color(0xFF166534) else Color(0xFFC2410C)
                    )
                    if (u.shop != null) {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        Text("• पसलको नाम: ${u.shop.name}")
                        Text("• पसल प्रकार: ${u.shop.type}")
                        if (u.shop.pan.isNotBlank()) Text("• PAN: ${u.shop.pan}")
                        if (u.shop.lrn.isNotBlank()) Text("• स्थानीय दर्ता नं.: ${u.shop.lrn}")
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    Text("• दर्ता मिति: ${u.regDate.ifBlank { "आज" }}")
                    Text("• क्विन ब्यालेन्स: ${u.pts} क्विन | वालेट: ₨${u.wallet}", fontWeight = FontWeight.Bold)
                }
            },
            confirmButton = {
                Button(
                    onClick = { showRegCardDialog = false },
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
                title = { Text(if (isNepali) "👤 प्रोफाइल तथा सेटिङ" else "Profile & Settings", fontWeight = FontWeight.Bold) }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // User Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(CrimsonRed),
                            contentAlignment = Alignment.Center
                        ) {
                            val initial = currentUser?.name?.take(1)?.uppercase() ?: "म"
                            Text(text = initial, color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = currentUser?.name ?: (if (isNepali) "साइन-इन गरिएको छैन" else "Not Signed In"),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = currentUser?.email ?: (if (isNepali) "साइन इन गरिएको छैन" else "Not signed in"),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        val roleLabel = when (currentUser?.role) {
                            "m" -> if (isNepali) "🏪 व्यापारी खाता (Merchant)" else "Merchant"
                            "a" -> if (isNepali) "👑 प्रशासक (Admin)" else "Admin"
                            else -> if (isNepali) "👤 ग्राहक खाता (Customer)" else "Customer"
                        }
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = CrimsonRed.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = roleLabel,
                                color = CrimsonRed,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                            )
                        }

                        if (currentUser != null) {
                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedButton(
                                onClick = {
                                    editName = currentUser?.name ?: ""
                                    editPhone = currentUser?.phone ?: ""
                                    editDist = currentUser?.dist ?: "Kathmandu"
                                    editWard = currentUser?.ward?.toString() ?: "10"
                                    showEditProfileDialog = true
                                },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (isNepali) "विवरण सम्पादन गर्नुहोस्" else "Edit Profile", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // Role Switcher Tile (Allows user to toggle between Customer, Merchant, and Admin views)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = if (isNepali) "भूमिका परिवर्तन (Switch Mode)" else "Switch App Mode",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val curRole = currentUser?.role ?: "c"
                            Button(
                                onClick = {
                                    viewModel.repo.setRole("c")
                                    viewModel.navigateTo("home")
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (curRole == "c") CrimsonRed else MaterialTheme.colorScheme.surfaceVariant
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    "👤 Customer",
                                    color = if (curRole == "c") Color.White else MaterialTheme.colorScheme.onSurface,
                                    fontSize = 11.sp
                                )
                            }
                            Button(
                                onClick = {
                                    viewModel.repo.setRole("m")
                                    viewModel.navigateTo("merchant_dashboard")
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (curRole == "m") CrimsonRed else MaterialTheme.colorScheme.surfaceVariant
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    "🏪 Merchant",
                                    color = if (curRole == "m") Color.White else MaterialTheme.colorScheme.onSurface,
                                    fontSize = 11.sp
                                )
                            }
                            Button(
                                onClick = {
                                    viewModel.repo.setRole("a")
                                    viewModel.navigateTo("admin_dashboard")
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (curRole == "a") CrimsonRed else MaterialTheme.colorScheme.surfaceVariant
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    "👑 Admin",
                                    color = if (curRole == "a") Color.White else MaterialTheme.colorScheme.onSurface,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }

            // Gmail & Security Verification Card
            if (currentUser != null) {
                item {
                    val isVerified = currentUser?.isEmailVerified == true || currentUser?.isGoogleVerified == true
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isVerified) JadeGreen.copy(alpha = 0.08f) else AmberGold.copy(alpha = 0.1f)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(if (isVerified) JadeGreen.copy(alpha = 0.15f) else AmberGold.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        if (isVerified) Icons.Default.MarkEmailRead else Icons.Default.MarkEmailUnread,
                                        contentDescription = null,
                                        tint = if (isVerified) JadeGreen else Color(0xFF8A4B00)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (isNepali) "Gmail / इमेल सुरक्षा प्रमाणीकरण" else "Gmail & Account Verification",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = if (isVerified)
                                            (if (isNepali) "✅ तपाईंको Gmail खाता प्रमाणित छ" else "✅ Verified with Google")
                                        else
                                            (if (isNepali) "⚠️ इमेल प्रमाणीकरण बाँकी छ" else "⚠️ Email not yet verified"),
                                        fontSize = 12.sp,
                                        color = if (isVerified) JadeGreen else Color(0xFF8A4B00),
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            if (!isVerified) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = if (isNepali)
                                        "बलियो सुरक्षाका लागि आफ्नो Gmail मा प्रमाणीकरण लिङ्क पठाउनुहोस्। इनबक्स र Spam फोल्डर हेर्नुहोला।"
                                    else
                                        "For strong security, send a verification link to your Gmail. Check your inbox and Spam folder.",
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(
                                        onClick = {
                                            viewModel.sendEmailVerification { ok, err ->
                                                if (ok) {
                                                    Toast.makeText(
                                                        context,
                                                        if (isNepali) "प्रमाणीकरण लिङ्क पठाइयो! Spam फोल्डर पनि हेर्नुहोला।" else "Verification link sent! Check Inbox/Spam.",
                                                        Toast.LENGTH_LONG
                                                    ).show()
                                                } else {
                                                    Toast.makeText(context, err ?: "Error", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(if (isNepali) "लिङ्क पठाउनुहोस्" else "Send Link", fontSize = 12.sp)
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            viewModel.checkEmailVerification { verified ->
                                                Toast.makeText(
                                                    context,
                                                    if (verified)
                                                        (if (isNepali) "बधाई छ! इमेल प्रमाणित भयो।" else "Verified successfully!")
                                                    else
                                                        (if (isNepali) "प्रमाणीकरण अझै पूरा भएको छैन। लिङ्कमा क्लिक गर्नुहोस्।" else "Not verified yet. Click link in email."),
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(if (isNepali) "पुनः जाँच" else "Check Status", fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Quick Actions List
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        // Customer Tools Hub (Saved, Addresses, Points, Compare, IRD Tax Prize)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.navigateTo("customer_tools") }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Dashboard, contentDescription = null, tint = AmberGold)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(if (isNepali) "🎁 ग्राहक औजार, लोयल्टी र IRD पुरस्कार" else "Customer Hub: Saved, Points & IRD Tax", fontWeight = FontWeight.Medium)
                                    Text(
                                        text = if (isNepali) "सुरक्षित सामान, ठेगाना, क्वाइन, तुलना र कर लक्की ड्र" else "Saved Items, Delivery Addresses, Points & Tax Lottery",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Icon(Icons.Default.ChevronRight, contentDescription = null)
                        }

                        HorizontalDivider()

                        // Messenger & P2P WebRTC / SIM Calling
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.navigateTo("messenger") }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Message, contentDescription = null, tint = JadeGreen)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(if (isNepali) "💬 सन्देश तथा अडियो/भिडियो कल (Messenger)" else "Mero Bazar Messenger & Calls", fontWeight = FontWeight.Medium)
                                    Text(
                                        text = if (isNepali) "१:१ च्याट, WebRTC कल तथा सिधै फोन सम्पर्क" else "Realtime chat, WebRTC audio/video & SIM call",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Icon(Icons.Default.ChevronRight, contentDescription = null)
                        }

                        HorizontalDivider()

                        // Contacts & Communication Center
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.navigateTo("contacts") }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Contacts, contentDescription = null, tint = NepalBlue)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(if (isNepali) "📞 सम्पर्क तथा कुराकानी केन्द्र" else "Contacts & Communication Hub", fontWeight = FontWeight.Medium)
                                    Text(
                                        text = if (isNepali) "ग्राहक, पसल तथा एडमिन डाइरेक्टरी" else "Customer, Merchant & Admin Directory",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Icon(Icons.Default.ChevronRight, contentDescription = null)
                        }

                        HorizontalDivider()

                        // Data Analysis & Calculations
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.navigateTo("data_analysis") }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Analytics, contentDescription = null, tint = JadeGreen)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(if (isNepali) "📊 डाटा विश्लेषण तथा IRD भ्याट क्यालकुलेटर" else "Data Analysis & Tax Calculator", fontWeight = FontWeight.Medium)
                                    Text(
                                        text = if (isNepali) "बिक्री, नाफा, १३% भ्याट तथा POS क्यालकुलेटर" else "Sales, Profit, 13% VAT & POS Calculator",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Icon(Icons.Default.ChevronRight, contentDescription = null)
                        }

                        HorizontalDivider()

                        // Google Docs & Calendar Export
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val doc = GoogleDocsHelper.generateInvoiceDoc(
                                        order = com.example.data.model.Order(
                                            id = "MB-${System.currentTimeMillis() % 100000}",
                                            cname = currentUser?.name ?: "Customer",
                                            cphone = currentUser?.phone ?: "9800000000",
                                            caddress = "${currentUser?.local ?: "Kathmandu"}, ${currentUser?.dist ?: "Kathmandu"}",
                                            shopName = "Mero Bazar Official Store",
                                            total = 2500L,
                                            st = "Verified",
                                            items = listOf(
                                                com.example.data.model.OrderItem(n = "हिमाली च्याङ्ग्रा घिउ", q = 1, p = 1500L),
                                                com.example.data.model.OrderItem(n = "इलाम अर्गानिक चिया", q = 2, p = 500L)
                                            )
                                        )
                                    )
                                    GoogleDocsHelper.shareDocument(context, "Mero Bazar Invoice Sample", doc)
                                }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Description, contentDescription = null, tint = NepalBlue)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(if (isNepali) "📄 Google Docs प्रतिवेदन तथा बिजक" else "Google Docs Report & Invoice", fontWeight = FontWeight.Medium)
                                    Text(
                                        text = if (isNepali) "बिल र हिसाब Google Docs मा निर्यात" else "Export receipts & ledgers to Google Docs",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Icon(Icons.Default.ChevronRight, contentDescription = null)
                        }

                        HorizontalDivider()

                        // Language toggle
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.toggleLanguage() }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Language, contentDescription = null, tint = CrimsonRed)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(if (isNepali) "भाषा (Language)" else "Language", fontWeight = FontWeight.Medium)
                            }
                            Text(if (isNepali) "नेपाली (Nepali)" else "English", color = CrimsonRed, fontWeight = FontWeight.Bold)
                        }

                        HorizontalDivider()

                        // Past Orders
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onNavigateToOrders() }
                                .padding(16.dp)
                                .testTag("profile_orders_button"),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Description, contentDescription = null, tint = CrimsonRed)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(if (isNepali) "अर्डर इतिहास र ट्र्याकिङ (Past Orders)" else "Past Orders & Tracking", fontWeight = FontWeight.Medium)
                            }
                            Icon(Icons.Default.ChevronRight, contentDescription = null)
                        }

                        if (currentUser != null) {
                            HorizontalDivider()

                            // Registration Record Card (दर्ता प्रमाण पत्र from zip regcard.js)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showRegCardDialog = true }
                                    .padding(16.dp)
                                    .testTag("profile_regcard_button"),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Badge, contentDescription = null, tint = CrimsonRed)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(if (isNepali) "📜 मेरो दर्ता प्रमाण पत्र" else "Registration Record Card", fontWeight = FontWeight.Medium)
                                        Text(
                                            text = "कोड: ${currentUser?.code?.ifBlank { "AB-C-10001" }}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                Icon(Icons.Default.ChevronRight, contentDescription = null)
                            }
                        }

                        HorizontalDivider()

                        // Nepal Map & Location Tagging
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.navigateTo("nepal_map") }
                                .padding(16.dp)
                                .testTag("profile_nepal_map_button"),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Map, contentDescription = null, tint = CrimsonRed)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(if (isNepali) "🗺️ नेपाल नक्सा तथा लोकेसन ट्याग" else "Nepal Map & Location Tag", fontWeight = FontWeight.Medium)
                                    Text(
                                        text = if (isNepali) "७ प्रदेश र ७७ जिल्ला अनुसार लोकेसन छान्नुहोस्" else "Tag your district across 7 provinces",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Icon(Icons.Default.ChevronRight, contentDescription = null)
                        }

                        HorizontalDivider()

                        // Software & APK Update & GitHub Auto-Upgrade Center (As requested by user)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.navigateTo("app_update") }
                                .padding(16.dp)
                                .testTag("profile_app_update_button"),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CloudDownload, contentDescription = null, tint = CrimsonRed)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(if (isNepali) "📱 सफ्टवेयर तथा APK अपडेट (GitHub Sync)" else "Software & APK Update", fontWeight = FontWeight.Medium)
                                    Text(
                                        text = if (isNepali) "नयाँ APK डाउनलोड र अटो-अपग्रेड" else "Download APK & Auto-Upgrade",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Icon(Icons.Default.ChevronRight, contentDescription = null)
                        }

                        if (currentUser?.role == "a") {
                            HorizontalDivider()
                            // Admin Panel shortcut
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.navigateTo("admin_dashboard") }
                                    .padding(16.dp)
                                    .testTag("profile_admin_panel_button"),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = CrimsonRed)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(if (isNepali) "प्रशासक प्यानल (Admin Panel)" else "Admin Control Panel", fontWeight = FontWeight.Medium)
                                }
                                Icon(Icons.Default.ChevronRight, contentDescription = null)
                            }
                        }

                        if (currentUser?.role == "m") {
                            HorizontalDivider()
                            // Merchant Dashboard shortcut
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.navigateTo("merchant_dashboard") }
                                    .padding(16.dp)
                                    .testTag("profile_merchant_panel_button"),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Dashboard, contentDescription = null, tint = CrimsonRed)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(if (isNepali) "व्यापारी ड्यासबोर्ड (Merchant Dashboard)" else "Merchant Dashboard", fontWeight = FontWeight.Medium)
                                }
                                Icon(Icons.Default.ChevronRight, contentDescription = null)
                            }
                        }
                    }
                }
            }

            // Auth Action Button
            item {
                if (currentUser == null) {
                    Button(
                        onClick = onNavigateToAuth,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("profile_signin_button")
                    ) {
                        Text(
                            text = if (isNepali) "साइन इन / खाता खोल्नुहोस्" else "Sign In / Register",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                } else {
                    OutlinedButton(
                        onClick = {
                            (context as? Activity)?.let { activity ->
                                viewModel.signOut(activity) {
                                    // Signed out
                                }
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("profile_signout_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, tint = CrimsonRed)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isNepali) "खाताबाट बाहिरिनुहोस् (Log Out)" else "Sign Out",
                            color = CrimsonRed,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
