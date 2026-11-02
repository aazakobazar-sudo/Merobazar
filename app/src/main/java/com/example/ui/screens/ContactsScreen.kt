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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.NotificationImportant
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ContactPerson
import com.example.ui.MainViewModel
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.JadeGreen
import com.example.ui.theme.NepalBlue
import com.example.util.NepalCalendarHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactsScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val isNepali by viewModel.isNepali.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val shops by viewModel.shops.collectAsState()
    val orders by viewModel.orders.collectAsState()
    val bills by viewModel.bills.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("all") } // "all", "customers", "merchants", "support"
    var showAddContactDialog by remember { mutableStateOf(false) }

    // Aggregate contacts from shops, orders, and bills
    val contactList = remember(shops, orders, bills, currentUser) {
        val list = mutableListOf<ContactPerson>()

        // 1. Support & Helpline contacts
        list.add(
            ContactPerson(
                id = "sup_1",
                name = "मेरो बजार ग्राहक सेवा (24/7 Helpline)",
                phone = "014234567",
                email = "support@merobazar.np",
                role = "support",
                shopName = "Mero Bazar Nepal",
                address = "New Road, Kathmandu",
                district = "Kathmandu",
                lastActive = "Available 24/7"
            )
        )
        list.add(
            ContactPerson(
                id = "sup_2",
                name = "आन्तरिक राजस्व कर सहायता (IRD Tax Desk)",
                phone = "014412345",
                email = "prize@ird.gov.np",
                role = "support",
                shopName = "Inland Revenue Dept",
                address = "Lazimpat, Kathmandu",
                district = "Kathmandu",
                lastActive = "Office Hours"
            )
        )
        list.add(
            ContactPerson(
                id = "sup_3",
                name = "प्राविधिक तथा एडमिन डेस्क (Admin Support)",
                phone = "9801234567",
                email = "nplbzr@gmail.com",
                role = "admin",
                shopName = "Platform Operations",
                address = "Thamel, Kathmandu",
                district = "Kathmandu",
                lastActive = "Online"
            )
        )

        // 2. Merchant / Shop Contacts
        for (shop in shops) {
            list.add(
                ContactPerson(
                    id = shop.id,
                    name = shop.name,
                    phone = shop.phone.ifBlank { "9841000000" },
                    email = shop.email.ifBlank { "shop@merobazar.np" },
                    role = "merchant",
                    shopName = shop.shop?.name ?: shop.name,
                    address = "${shop.local}-${shop.ward}, ${shop.dist}",
                    district = shop.dist,
                    lastActive = "सक्रिय पसल"
                )
            )
        }

        // 3. Customer Contacts from Orders and Bills
        val customerMap = mutableMapOf<String, ContactPerson>()
        for (order in orders) {
            val phone = order.cphone.ifBlank { "9841111111" }
            val existing = customerMap[phone]
            val totalOrd = (existing?.ordersCount ?: 0) + 1
            val due = existing?.balanceDue ?: 0L
            customerMap[phone] = ContactPerson(
                id = order.cuid.ifBlank { "cust_${order.id}" },
                name = order.cname.ifBlank { "ग्राहक (Customer)" },
                phone = phone,
                email = "customer@merobazar.np",
                role = "customer",
                shopName = order.shopName,
                address = order.caddress,
                district = "Kathmandu",
                balanceDue = due,
                ordersCount = totalOrd,
                lastActive = order.d
            )
        }

        // Add bill/khata records
        for (b in bills) {
            val phone = b.phone.ifBlank { "9851122334" }
            val existing = customerMap[phone]
            val prevDue = existing?.balanceDue ?: 0L
            customerMap[phone] = ContactPerson(
                id = existing?.id ?: "khata_${b.id}",
                name = b.cname.ifBlank { existing?.name ?: "खाता ग्राहक" },
                phone = phone,
                email = existing?.email ?: "khata@merobazar.np",
                role = "customer",
                shopName = b.shopName,
                address = existing?.address ?: "स्थानीय बजार",
                district = "Kathmandu",
                balanceDue = prevDue + b.total,
                ordersCount = (existing?.ordersCount ?: 0) + 1,
                lastActive = b.d
            )
        }

        list.addAll(customerMap.values)
        list
    }

    val filteredContacts = contactList.filter { c ->
        val matchesSearch = searchQuery.isBlank() ||
                c.name.contains(searchQuery, ignoreCase = true) ||
                c.phone.contains(searchQuery, ignoreCase = true) ||
                c.shopName.contains(searchQuery, ignoreCase = true) ||
                c.district.contains(searchQuery, ignoreCase = true)

        val matchesFilter = when (selectedFilter) {
            "customers" -> c.role == "customer"
            "merchants" -> c.role == "merchant"
            "support" -> c.role == "support" || c.role == "admin"
            else -> true
        }

        matchesSearch && matchesFilter
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (isNepali) "सम्पर्क तथा कुराकानी केन्द्र" else "Contacts & Communication Hub",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = if (isNepali) "ग्राहक, विक्रेता र एडमिन बीच सीधा सम्पर्क" else "Seamless customer, seller & admin contact",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CrimsonRed,
                    titleContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddContactDialog = true },
                containerColor = NepalBlue,
                contentColor = Color.White,
                modifier = Modifier.testTag("fab_add_contact")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Contact")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("contact_search_input"),
                placeholder = {
                    Text(if (isNepali) "नाम, मोबाइल, पसल वा ठेगाना खोज्नुहोस्..." else "Search name, phone, shop, address...")
                },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = CrimsonRed)
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // Filter Chips
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedFilter == "all",
                        onClick = { selectedFilter = "all" },
                        label = { Text(if (isNepali) "सबै (${contactList.size})" else "All (${contactList.size})") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CrimsonRed,
                            selectedLabelColor = Color.White
                        )
                    )
                }
                item {
                    FilterChip(
                        selected = selectedFilter == "customers",
                        onClick = { selectedFilter = "customers" },
                        label = { Text(if (isNepali) "ग्राहकहरू (Customers)" else "Customers") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = JadeGreen,
                            selectedLabelColor = Color.White
                        )
                    )
                }
                item {
                    FilterChip(
                        selected = selectedFilter == "merchants",
                        onClick = { selectedFilter = "merchants" },
                        label = { Text(if (isNepali) "पसल/विक्रेता (Sellers)" else "Sellers") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NepalBlue,
                            selectedLabelColor = Color.White
                        )
                    )
                }
                item {
                    FilterChip(
                        selected = selectedFilter == "support",
                        onClick = { selectedFilter = "support" },
                        label = { Text(if (isNepali) "हेल्पलाइन र एडमिन" else "Support & Admin") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AmberGold,
                            selectedLabelColor = Color.Black
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Contact List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredContacts, key = { it.id + it.phone }) { contact ->
                    ContactCardItem(
                        contact = contact,
                        isNepali = isNepali,
                        onCall = { phone ->
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                            try { context.startActivity(intent) } catch (_: Exception) {}
                        },
                        onSms = { phone, name, due ->
                            val msg = if (due > 0) {
                                "नमस्ते $name ज्यू, मेरो बजारबाट हजुरको खातामा रु $due बाँकी छ। कृपया भुक्तानी गरिदिनुहोला। धन्यवाद!"
                            } else {
                                "नमस्ते $name ज्यू, मेरो बजार कारोबार सम्बन्धमा सम्पर्क गरिएको हो।"
                            }
                            val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:$phone")).apply {
                                putExtra("sms_body", msg)
                            }
                            try { context.startActivity(intent) } catch (_: Exception) {}
                        },
                        onWhatsApp = { phone ->
                            val clean = phone.replace("+", "").replace("-", "").replace(" ", "")
                            val finalPhone = if (clean.startsWith("98") || clean.startsWith("97")) "977$clean" else clean
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$finalPhone"))
                            try { context.startActivity(intent) } catch (_: Exception) {}
                        },
                        onEmail = { email ->
                            val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$email")).apply {
                                putExtra(Intent.EXTRA_SUBJECT, "Mero Bazar Communication")
                            }
                            try { context.startActivity(intent) } catch (_: Exception) {}
                        },
                        onSetReminder = { contact ->
                            val ok = NepalCalendarHelper.addKhataDueReminder(
                                context = context,
                                customerName = contact.name,
                                amountRs = contact.balanceDue.coerceAtLeast(500L),
                                dueDateDays = 7
                            )
                            if (ok) {
                                Toast.makeText(context, if (isNepali) "क्यालेन्डरमा सम्झाउनी थपियो" else "Calendar Reminder Added", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                }
            }
        }
    }

    // Add Contact Dialog
    if (showAddContactDialog) {
        var newName by remember { mutableStateOf("") }
        var newPhone by remember { mutableStateOf("") }
        var newEmail by remember { mutableStateOf("") }
        var newShop by remember { mutableStateOf("") }
        var newDueStr by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddContactDialog = false },
            title = {
                Text(
                    text = if (isNepali) "नयाँ सम्पर्क थप्नुहोस्" else "Add New Contact",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text(if (isNepali) "नाम" else "Full Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = newPhone,
                        onValueChange = { newPhone = it },
                        label = { Text(if (isNepali) "सम्पर्क नम्बर (Phone)" else "Phone Number") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = newEmail,
                        onValueChange = { newEmail = it },
                        label = { Text(if (isNepali) "इमेल (Gmail / Email)" else "Email Address") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = newShop,
                        onValueChange = { newShop = it },
                        label = { Text(if (isNepali) "पसल / ठेगाना (Shop / Address)" else "Shop / Address") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = newDueStr,
                        onValueChange = { newDueStr = it.filter { c -> c.isDigit() } },
                        label = { Text(if (isNepali) "खाता बाँकी रकम रु (Khata Due if any)" else "Khata Due (Rs)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newName.isNotBlank() && newPhone.isNotBlank()) {
                            val due = newDueStr.toLongOrNull() ?: 0L
                            viewModel.createBill(newName, newPhone, emptyList())
                            showAddContactDialog = false
                            Toast.makeText(context, if (isNepali) "सम्पर्क सुरक्षित भयो" else "Contact Saved", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed)
                ) {
                    Text(if (isNepali) "सुरक्षित गर्नुहोस्" else "Save Contact")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddContactDialog = false }) {
                    Text(if (isNepali) "रद्द" else "Cancel")
                }
            }
        )
    }
}

@Composable
fun ContactCardItem(
    contact: ContactPerson,
    isNepali: Boolean,
    onCall: (String) -> Unit,
    onSms: (String, String, Long) -> Unit,
    onWhatsApp: (String) -> Unit,
    onEmail: (String) -> Unit,
    onSetReminder: (ContactPerson) -> Unit
) {
    val roleColor = when (contact.role) {
        "admin" -> CrimsonRed
        "support" -> AmberGold
        "merchant" -> NepalBlue
        else -> JadeGreen
    }

    val roleLabel = when (contact.role) {
        "admin" -> if (isNepali) "प्रशासक (Admin)" else "Admin"
        "support" -> if (isNepali) "सहयोग डेस्क (Support)" else "Support Desk"
        "merchant" -> if (isNepali) "विक्रेता (Seller)" else "Seller"
        else -> if (isNepali) "ग्राहक (Customer)" else "Customer"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Avatar, Name & Role Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(roleColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    val icon = when (contact.role) {
                        "admin" -> Icons.Default.SupportAgent
                        "support" -> Icons.Default.Phone
                        "merchant" -> Icons.Default.Store
                        else -> Icons.Default.Person
                    }
                    Icon(icon, contentDescription = null, tint = roleColor, modifier = Modifier.size(24.dp))
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = contact.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (contact.shopName.isNotBlank() && contact.shopName != contact.name) {
                        Text(
                            text = contact.shopName,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Text(
                        text = "${contact.phone} • ${contact.address}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = roleColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = roleLabel,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = roleColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Khata Due Banner if customer has balance
            if (contact.balanceDue > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = AmberGold.copy(alpha = 0.15f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isNepali) "खाता बाँकी उधारो (Khata Due):" else "Khata Balance Due:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF7A4E00)
                        )
                        Text(
                            text = "रु ${contact.balanceDue}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = CrimsonRed
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons: Call, SMS, WhatsApp, Email, Calendar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Call
                OutlinedButton(
                    onClick = { onCall(contact.phone) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Call, contentDescription = "Call", tint = JadeGreen, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isNepali) "कल" else "Call", fontSize = 12.sp)
                }

                // WhatsApp
                Button(
                    onClick = { onWhatsApp(contact.phone) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = JadeGreen),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                ) {
                    Text("WhatsApp", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                // SMS
                OutlinedButton(
                    onClick = { onSms(contact.phone, contact.name, contact.balanceDue) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Message, contentDescription = "SMS", tint = NepalBlue, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isNepali) "एसएमएस" else "SMS", fontSize = 12.sp)
                }

                // Email / Calendar
                if (contact.balanceDue > 0) {
                    IconButton(
                        onClick = { onSetReminder(contact) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = "Calendar Reminder", tint = CrimsonRed)
                    }
                } else {
                    IconButton(
                        onClick = { onEmail(contact.email) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.Email, contentDescription = "Email", tint = NepalBlue)
                    }
                }
            }
        }
    }
}
