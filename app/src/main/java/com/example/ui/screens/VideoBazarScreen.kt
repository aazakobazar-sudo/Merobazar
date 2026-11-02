package com.example.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.VideoComment
import com.example.data.model.VideoReelItem
import com.example.ui.MainViewModel
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.DarkCrimson
import com.example.ui.theme.JadeGreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val DeepNavy = Color(0xFF0F172A)
private val ElectricBlue = Color(0xFF38BDF8)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun VideoBazarScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit,
    onOpenProduct: (String) -> Unit = {},
    onOpenShop: (String) -> Unit = {},
    onNavigateToCart: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val isNepali by viewModel.isNepali.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val videoReels by viewModel.videoReels.collectAsState()
    val followedShops by viewModel.followedShops.collectAsState()
    val savedVideos by viewModel.savedProducts.collectAsState()

    var selectedFilterTag by remember { mutableStateOf("सबै") }
    var showUploadModal by remember { mutableStateOf(false) }
    var activeCommentVideoId by remember { mutableStateOf<String?>(null) }
    var isMuted by remember { mutableStateOf(false) }

    val filterTags = listOf("सबै", "अर्गानिक", "छुट", "फेसन", "ग्याजेट", "नयाँ")

    val filteredList = remember(videoReels, selectedFilterTag) {
        if (selectedFilterTag == "सबै") videoReels
        else videoReels.filter { reel ->
            reel.tags.any { it.contains(selectedFilterTag, ignoreCase = true) } ||
                    reel.title.contains(selectedFilterTag, ignoreCase = true) ||
                    reel.description.contains(selectedFilterTag, ignoreCase = true)
        }
    }

    // Modal Bottom Sheet for Comments
    val sheetState = rememberModalBottomSheetState()
    if (activeCommentVideoId != null) {
        val currentVideo = videoReels.firstOrNull { it.id == activeCommentVideoId }
        var commentInput by remember { mutableStateOf("") }
        var localComments by remember(activeCommentVideoId) {
            mutableStateOf(
                listOf(
                    VideoComment("c1", activeCommentVideoId!!, "सुजन श्रेष्ठ", "साँच्चिकै अर्गानिक हो? स्वाद र गुणस्तर राम्रो छ?", "५ मिनेट अगाडि"),
                    VideoComment("c2", activeCommentVideoId!!, currentVideo?.shopName ?: "पसल", "हजुर, पूर्ण रूपमा परीक्षण गरिएको र ताजा छ!", "२ मिनेट अगाडि")
                )
            )
        }

        ModalBottomSheet(
            onDismissRequest = { activeCommentVideoId = null },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .navigationBarsPadding()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isNepali) "💬 भिडियो प्रतिक्रियाहरू (${localComments.size})" else "💬 Comments (${localComments.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = { activeCommentVideoId = null }) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onSurface)
                    }
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .height(280.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(localComments) { comment ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = CrimsonRed.copy(alpha = 0.15f),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = comment.userName.take(1),
                                        fontWeight = FontWeight.Bold,
                                        color = CrimsonRed
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = comment.userName,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = comment.timeAgo,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    text = comment.text,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = commentInput,
                        onValueChange = { commentInput = it },
                        placeholder = { Text(if (isNepali) "प्रतिक्रिया लेख्नुहोस्..." else "Write a comment...") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("video_comment_input"),
                        shape = RoundedCornerShape(24.dp),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (commentInput.isNotBlank()) {
                                val newComment = VideoComment(
                                    id = "c_${System.currentTimeMillis()}",
                                    videoId = activeCommentVideoId!!,
                                    userName = currentUser?.name ?: "प्रयोगकर्ता",
                                    text = commentInput.trim(),
                                    timeAgo = "भर्खरै"
                                )
                                localComments = localComments + newComment
                                commentInput = ""
                            }
                        },
                        modifier = Modifier
                            .background(CrimsonRed, CircleShape)
                            .size(46.dp)
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Post", tint = Color.White)
                    }
                }
            }
        }
    }

    // Upload Reel Dialog (Merchant & Admin)
    if (showUploadModal) {
        var uploadTitle by remember { mutableStateOf("") }
        var uploadDesc by remember { mutableStateOf("") }
        var uploadTags by remember { mutableStateOf("#organic #local #offer") }
        var uploadProdName by remember { mutableStateOf("जुम्ली मार्सी चामल") }
        var uploadProdPrice by remember { mutableStateOf("600") }

        AlertDialog(
            onDismissRequest = { showUploadModal = false },
            title = {
                Text(
                    text = if (isNepali) "🎬 नयाँ भिडियो रिल्स अपलोड" else "🎬 Upload Video Reel",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = uploadTitle,
                        onValueChange = { uploadTitle = it },
                        label = { Text(if (isNepali) "भिडियो शीर्षक" else "Video Title") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = uploadDesc,
                        onValueChange = { uploadDesc = it },
                        label = { Text(if (isNepali) "विवरण (Description)" else "Description") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )
                    OutlinedTextField(
                        value = uploadProdName,
                        onValueChange = { uploadProdName = it },
                        label = { Text(if (isNepali) "देखाइएको सामान (Product)" else "Product Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = uploadProdPrice,
                        onValueChange = { uploadProdPrice = it },
                        label = { Text(if (isNepali) "मूल्य (रु)" else "Price (NPR)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = uploadTags,
                        onValueChange = { uploadTags = it },
                        label = { Text(if (isNepali) "ट्यागहरू (Hashtags)" else "Tags") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (uploadTitle.isNotBlank()) {
                            val priceVal = uploadProdPrice.toLongOrNull() ?: 500L
                            val tagsList = uploadTags.split(" ").filter { it.isNotBlank() }
                            viewModel.uploadVideoReel(
                                shopId = currentUser?.id ?: "shop_1",
                                shopName = currentUser?.shop?.name ?: currentUser?.name ?: "डेमो पसल",
                                title = uploadTitle,
                                description = uploadDesc,
                                videoUrl = "https://assets.merobazar.np/demo_reel.mp4",
                                productId = "p_1_1",
                                productName = uploadProdName,
                                productPrice = priceVal,
                                tags = tagsList
                            )
                            Toast.makeText(
                                context,
                                if (isNepali) "भिडियो रिल्स सफलतापूर्वक प्रकाशित भयो!" else "Reel published successfully!",
                                Toast.LENGTH_SHORT
                            ).show()
                            showUploadModal = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed)
                ) {
                    Text(if (isNepali) "अपलोड गर्नुहोस्" else "Upload")
                }
            },
            dismissButton = {
                TextButton(onClick = { showUploadModal = false }) {
                    Text(if (isNepali) "रद्द" else "Cancel")
                }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        val listState = rememberLazyListState()

        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize()
        ) {
            items(filteredList, key = { it.id }) { reel ->
                VideoReelCard(
                    reel = reel,
                    isNepali = isNepali,
                    isMuted = isMuted,
                    isFollowed = followedShops.contains(reel.shopId),
                    isAdmin = currentUser?.role == "a",
                    onToggleLike = { viewModel.toggleVideoLike(reel.id) },
                    onToggleFollow = { viewModel.toggleFollowShop(reel.shopId) },
                    onOpenComments = { activeCommentVideoId = reel.id },
                    onToggleMute = { isMuted = !isMuted },
                    onShare = {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, reel.title)
                            putExtra(Intent.EXTRA_TEXT, "${reel.title}\nमेरो बजार भिडियो बजारमा हेर्नुहोस्: https://merobazar.np/reels/${reel.id}")
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share Reel"))
                    },
                    onBuyProduct = {
                        viewModel.addToCart(reel.productId)
                        Toast.makeText(
                            context,
                            if (isNepali) "${reel.productName} झोलामा थपियो!" else "${reel.productName} added to cart!",
                            Toast.LENGTH_SHORT
                        ).show()
                        onNavigateToCart()
                    },
                    onOpenProduct = { onOpenProduct(reel.productId) },
                    onOpenShop = { onOpenShop(reel.shopId) },
                    onDeleteReel = {
                        viewModel.deleteVideoReel(reel.id)
                        Toast.makeText(context, if (isNepali) "भिडियो हटाइयो!" else "Video deleted!", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }

        // Top Navigation and Category Filter Chips
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.background(Color.Black.copy(alpha = 0.45f), CircleShape)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (isNepali) "🎬 भिडियो बजार" else "🎬 Video Bazar",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        color = CrimsonRed,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "LIVE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                IconButton(
                    onClick = { showUploadModal = true },
                    modifier = Modifier.background(CrimsonRed, CircleShape)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Upload Reel", tint = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                filterTags.forEach { tag ->
                    val selected = selectedFilterTag == tag
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (selected) CrimsonRed else Color.Black.copy(alpha = 0.5f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (selected) CrimsonRed else Color.White.copy(alpha = 0.3f)),
                        modifier = Modifier.clickable { selectedFilterTag = tag }
                    ) {
                        Text(
                            text = tag,
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun VideoReelCard(
    reel: VideoReelItem,
    isNepali: Boolean,
    isMuted: Boolean,
    isFollowed: Boolean,
    isAdmin: Boolean,
    onToggleLike: () -> Unit,
    onToggleFollow: () -> Unit,
    onOpenComments: () -> Unit,
    onToggleMute: () -> Unit,
    onShare: () -> Unit,
    onBuyProduct: () -> Unit,
    onOpenProduct: () -> Unit,
    onOpenShop: () -> Unit,
    onDeleteReel: () -> Unit
) {
    var isPlaying by remember { mutableStateOf(true) }
    var progress by remember { mutableFloatStateOf(0.35f) }

    // Simulated Reel Video progress
    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            delay(120)
            progress = (progress + 0.01f)
            if (progress > 1f) progress = 0f
        }
    }

    // Dynamic gradient background representing simulated high-definition video frame
    val videoGradients = remember(reel.id) {
        listOf(
            listOf(Color(0xFF1A1A24), Color(0xFF0F2027), Color(0xFF2C5364)),
            listOf(Color(0xFF200122), Color(0xFF6f0000), Color(0xFF110000)),
            listOf(Color(0xFF0F3443), Color(0xFF34E89E), Color(0xFF0B1B2B)),
            listOf(Color(0xFF141E30), Color(0xFF243B55), Color(0xFF0F172A))
        ).random()
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(680.dp)
            .background(Brush.verticalGradient(videoGradients))
            .clickable { isPlaying = !isPlaying }
    ) {
        // Center play/pause indicator when paused
        if (!isPlaying) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .background(Color.Black.copy(alpha = 0.65f), CircleShape)
                    .size(72.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.PlayArrow,
                    contentDescription = "Play",
                    tint = Color.White,
                    modifier = Modifier.size(44.dp)
                )
            }
        }

        // Sound Toggle at Top Right
        IconButton(
            onClick = onToggleMute,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 96.dp, end = 16.dp)
                .background(Color.Black.copy(alpha = 0.45f), CircleShape)
        ) {
            Icon(
                if (isMuted) Icons.Default.VolumeMute else Icons.Default.VolumeUp,
                contentDescription = "Sound",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }

        // Right Action Column (Shop Avatar, Like, Comment, Share, Delete if admin)
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 12.dp, bottom = 100.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Shop Avatar + Follow button
            Box(contentAlignment = Alignment.BottomCenter) {
                Surface(
                    shape = CircleShape,
                    color = DarkCrimson,
                    border = androidx.compose.foundation.BorderStroke(2.dp, Color.White),
                    modifier = Modifier
                        .size(48.dp)
                        .clickable { onOpenShop() }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Store, contentDescription = "Shop", tint = Color.White, modifier = Modifier.size(24.dp))
                    }
                }
                Box(
                    modifier = Modifier
                        .padding(top = 34.dp)
                        .background(if (isFollowed) JadeGreen else CrimsonRed, CircleShape)
                        .size(20.dp)
                        .clickable { onToggleFollow() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (isFollowed) Icons.Default.Check else Icons.Default.Add,
                        contentDescription = "Follow",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            // Like Button
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(
                    onClick = onToggleLike,
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.45f), CircleShape)
                        .size(44.dp)
                ) {
                    Icon(
                        if (reel.isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Like",
                        tint = if (reel.isLiked) CrimsonRed else Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                }
                Text(
                    text = "${reel.likesCount}",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Comments Button
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(
                    onClick = onOpenComments,
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.45f), CircleShape)
                        .size(44.dp)
                ) {
                    Icon(
                        Icons.Default.ChatBubbleOutline,
                        contentDescription = "Comments",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Text(
                    text = "${reel.commentsCount}",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Share Button
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(
                    onClick = onShare,
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.45f), CircleShape)
                        .size(44.dp)
                ) {
                    Icon(
                        Icons.Default.Share,
                        contentDescription = "Share",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Text(
                    text = if (isNepali) "साझा" else "Share",
                    color = Color.White,
                    fontSize = 11.sp
                )
            }

            // Admin Moderation Delete
            if (isAdmin) {
                IconButton(
                    onClick = onDeleteReel,
                    modifier = Modifier
                        .background(DarkCrimson, CircleShape)
                        .size(40.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete Reel", tint = Color.White, modifier = Modifier.size(20.dp))
                }
            }
        }

        // Bottom Details & Floating Product Pin Overlay
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth(0.82f)
                .padding(start = 14.dp, bottom = 18.dp)
        ) {
            // Shop Name & Verified
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { onOpenShop() }
            ) {
                Text(
                    text = "@${reel.shopName}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    Icons.Default.Verified,
                    contentDescription = "Verified",
                    tint = ElectricBlue,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Title & Description
            Text(
                text = reel.title,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (reel.description.isNotBlank()) {
                Text(
                    text = reel.description,
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.85f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Hashtags
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                reel.tags.take(3).forEach { tag ->
                    Text(
                        text = if (tag.startsWith("#")) tag else "#$tag",
                        color = AmberGold,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Product Overlay Banner (Section A 1.4 & Section B 3: Buy From Video)
            if (reel.productName.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.Black.copy(alpha = 0.75f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CrimsonRed.copy(alpha = 0.6f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenProduct() }
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = CrimsonRed.copy(alpha = 0.2f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.ShoppingCart,
                                    contentDescription = "Product",
                                    tint = CrimsonRed,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = reel.productName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "रु ${reel.productPrice}",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 12.sp,
                                color = AmberGold
                            )
                        }

                        Button(
                            onClick = onBuyProduct,
                            colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed),
                            shape = RoundedCornerShape(16.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("buy_from_video_button")
                        ) {
                            Text(
                                text = if (isNepali) "किन्नुहोस्" else "Buy",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Reel Playback Progress Bar
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = CrimsonRed,
                trackColor = Color.White.copy(alpha = 0.2f)
            )
        }
    }
}
