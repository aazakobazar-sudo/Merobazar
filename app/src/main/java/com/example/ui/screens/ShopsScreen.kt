package com.example.ui.screens

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
import androidx.compose.foundation.shape.RoundedCornerShape
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserProfile
import com.example.ui.MainViewModel
import com.example.ui.components.ProductCard
import com.example.ui.components.ShopCard
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.JadeGreen
import com.example.ui.theme.NepalBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShopsScreen(
    viewModel: MainViewModel,
    onOpenShop: (String) -> Unit
) {
    val isNepali by viewModel.isNepali.collectAsState()
    val shops by viewModel.shops.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isNepali) "🏪 पसलहरूको सूची" else "🏪 Verified Shops",
                        fontWeight = FontWeight.Bold
                    )
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(shops) { shop ->
                ShopCard(
                    shop = shop,
                    onClick = { onOpenShop(shop.id) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShopDetailScreen(
    shopId: String,
    viewModel: MainViewModel,
    onBack: () -> Unit,
    onOpenProduct: (String) -> Unit
) {
    val isNepali by viewModel.isNepali.collectAsState()
    val shops by viewModel.shops.collectAsState()
    val allProducts by viewModel.filteredProducts.collectAsState()
    val allReviews by viewModel.reviews.collectAsState()
    val shop = shops.find { it.id == shopId } ?: shops.firstOrNull()

    var showReviewDialog by remember { mutableStateOf(false) }
    var reviewRating by remember { mutableStateOf(5) }
    var reviewText by remember { mutableStateOf("") }

    if (showReviewDialog && shop != null) {
        AlertDialog(
            onDismissRequest = { showReviewDialog = false },
            title = {
                Text(
                    text = if (isNepali) "पसलको मूल्यांकन (Add Review)" else "Rate & Review Shop",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(text = if (isNepali) "तारा छनोट गर्नुहोस्:" else "Select Rating:")
                    Spacer(modifier = Modifier.height(6.dp))
                    Row {
                        (1..5).forEach { star ->
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "$star stars",
                                tint = if (star <= reviewRating) AmberGold else Color.LightGray,
                                modifier = Modifier
                                    .size(32.dp)
                                    .clickable { reviewRating = star }
                                    .padding(2.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = reviewText,
                        onValueChange = { reviewText = it },
                        placeholder = { Text(if (isNepali) "आफ्नो अनुभव लेख्नुहोस्..." else "Write your feedback...") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (reviewText.isNotBlank()) {
                            viewModel.addReview(shop.id, reviewRating, reviewText)
                            reviewText = ""
                            showReviewDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed)
                ) {
                    Text(if (isNepali) "पठाउनुहोस्" else "Submit")
                }
            },
            dismissButton = {
                TextButton(onClick = { showReviewDialog = false }) {
                    Text(if (isNepali) "रद्द" else "Cancel")
                }
            }
        )
    }

    if (shop == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Shop not found")
        }
        return
    }

    val shopProducts = allProducts.filter { it.mid == shop.id }
    val shopReviews = allReviews.filter { it.suid == shop.id }

    val context = LocalContext.current
    val savedShops by viewModel.savedShops.collectAsState()
    val followedShops by viewModel.followedShops.collectAsState()
    val blockedShops by viewModel.blockedShops.collectAsState()

    val isSaved = savedShops.contains(shop.id)
    val isFollowed = followedShops.contains(shop.id)
    val isBlocked = blockedShops.contains(shop.id)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(shop.shop?.name ?: shop.name, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Holiday Notice Banner
            if (shop.shop?.isHoliday == true) {
                item {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFFFF3CD),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🏖️", fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = if (isNepali) "यो पसल अहिले बिदामा छ" else "This shop is on holiday",
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF664D03)
                                )
                                Text(
                                    text = shop.shop.holidayMsg.ifBlank { if (isNepali) "चाँडै खुल्नेछ, कृपया पछि अर्डर गर्नुहोस्।" else "Will open soon." },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF664D03)
                                )
                            }
                        }
                    }
                }
            }

            // Shop Details Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = shop.shop?.name ?: shop.name,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(Icons.Default.CheckCircle, contentDescription = "Verified", tint = JadeGreen)
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, contentDescription = "Location", tint = CrimsonRed, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${shop.local}-${shop.ward}, ${shop.dist}, ${shop.prov}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Call, contentDescription = "Phone", tint = CrimsonRed, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "📞 ${shop.phone}", style = MaterialTheme.typography.bodyMedium)
                        }

                        if (!shop.shop?.pan.isNullOrBlank()) {
                            Text(text = "PAN: ${shop.shop?.pan}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        if (!shop.shop?.lrn.isNullOrBlank()) {
                            Text(text = "Reg. No: ${shop.shop?.lrn}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Rating & Review Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Star, contentDescription = "Rating", tint = AmberGold)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${shop.shop?.rating ?: 4.8} (${shopReviews.size + 12} reviews)",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Button(
                                onClick = { showReviewDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(if (isNepali) "रेटिङ दिनुहोस्" else "Review")
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Operational Action Buttons (Follow, Save, Share, Chat, Direct Call, Block)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Follow Button
                            OutlinedButton(
                                onClick = {
                                    viewModel.toggleFollowShop(shop.id)
                                    Toast.makeText(context, if (!isFollowed) "पसल फलो गरियो! ✓" else "अनफलो गरियो", Toast.LENGTH_SHORT).show()
                                },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(if (isFollowed) (if (isNepali) "✅ फलो गरियो" else "Following") else (if (isNepali) "➕ फलो" else "Follow"), fontSize = 12.sp)
                            }

                            // Save Shop Button
                            OutlinedButton(
                                onClick = {
                                    viewModel.toggleSaveShop(shop.id)
                                    Toast.makeText(context, if (!isSaved) "पसल सुरक्षित गरियो! ❤️" else "हटाइयो", Toast.LENGTH_SHORT).show()
                                },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(if (isSaved) Icons.Default.Favorite else Icons.Default.FavoriteBorder, contentDescription = null, tint = CrimsonRed, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (isSaved) (if (isNepali) "सुरक्षित" else "Saved") else (if (isNepali) "सुरक्षित" else "Save"), fontSize = 12.sp)
                            }

                            // Chat / Messenger Button
                            Button(
                                onClick = { viewModel.navigateTo("messenger") },
                                colors = ButtonDefaults.buttonColors(containerColor = NepalBlue),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (isNepali) "सन्देश / कल" else "Chat / Call", fontSize = 12.sp)
                            }

                            // Direct Call Button
                            Button(
                                onClick = {
                                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${shop.phone}"))
                                    try { context.startActivity(intent) } catch (_: Exception) {}
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = JadeGreen),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (isNepali) "फोन कल" else "Dial", fontSize = 12.sp)
                            }

                            // Share Shop Button
                            OutlinedButton(
                                onClick = {
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_SUBJECT, shop.shop?.name ?: shop.name)
                                        putExtra(Intent.EXTRA_TEXT, "मेरो बजारमा '${shop.shop?.name ?: shop.name}' पसल हेर्नुहोस्: https://merobazar.np/shop/${shop.id}")
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "Share Shop"))
                                },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (isNepali) "सेयर" else "Share", fontSize = 12.sp)
                            }

                            // Block Shop Button
                            OutlinedButton(
                                onClick = {
                                    viewModel.toggleBlockShop(shop.id)
                                    Toast.makeText(context, if (!isBlocked) "पसल ब्लक गरियो" else "अनब्लक गरियो", Toast.LENGTH_SHORT).show()
                                },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Block, contentDescription = null, tint = CrimsonRed, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (isBlocked) (if (isNepali) "अनब्लक" else "Unblock") else (if (isNepali) "ब्लक" else "Block"), color = CrimsonRed, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // Products Header
            item {
                Text(
                    text = if (isNepali) "उपलब्ध सामानहरू (${shopProducts.size})" else "Products in this Shop (${shopProducts.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            // Products in this shop
            items(shopProducts) { prod ->
                ProductCard(
                    product = prod,
                    onAddToCart = { viewModel.addToCart(prod.id) },
                    onClick = { onOpenProduct(prod.id) }
                )
            }

            // Reviews Header
            item {
                Text(
                    text = if (isNepali) "ग्राहकका प्रतिक्रियाहरू (Reviews)" else "Customer Reviews",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 10.dp)
                )
            }

            items(shopReviews) { rev ->
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
                            Text(text = rev.cname, fontWeight = FontWeight.Bold)
                            Row {
                                repeat(rev.r) {
                                    Icon(Icons.Default.Star, contentDescription = null, tint = AmberGold, modifier = Modifier.size(14.dp))
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = rev.t, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}
