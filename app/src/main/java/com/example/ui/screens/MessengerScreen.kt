package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.ShoppingBag
import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.Link
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.compose.ui.platform.LocalLifecycleOwner
import coil.compose.AsyncImage
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material.icons.filled.VolumeUp
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CallLogEntry
import com.example.data.model.ChatMessage
import com.example.ui.MainViewModel
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.DarkCrimson
import com.example.ui.theme.JadeGreen
import com.example.ui.theme.NepalBlue
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessengerScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit,
    targetShopId: String? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val isNepali by viewModel.isNepali.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val shops by viewModel.shops.collectAsState()
    val allMessages by viewModel.chatMessages.collectAsState()
    val callLogs by viewModel.callLogs.collectAsState()
    val blockedShops by viewModel.blockedShops.collectAsState()
    val products by viewModel.products.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    var activeChatShopId by remember {
        mutableStateOf(targetShopId ?: shops.firstOrNull()?.id ?: "shop_1")
    }

    val selectedShop = shops.find { it.id == activeChatShopId } ?: shops.firstOrNull()
    val shopName = selectedShop?.shop?.name ?: selectedShop?.name ?: "Mero Bazar Shop"
    val shopPhone = selectedShop?.phone ?: "9841234567"
    val isShopBlocked = blockedShops.contains(activeChatShopId)

    var messageInput by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Dialogs
    var showProductAttachDialog by remember { mutableStateOf(false) }
    var showVideoAttachDialog by remember { mutableStateOf(false) }
    var showBlockDialog by remember { mutableStateOf(false) }

    // Cloud Live Call Session from Firestore
    val activeCallSession by viewModel.activeCallSession.collectAsState()
    var isMicMuted by remember { mutableStateOf(false) }
    var isVideoOff by remember { mutableStateOf(false) }
    var isSpeakerOn by remember { mutableStateOf(true) }
    var isFrontCamera by remember { mutableStateOf(true) }
    var callSeconds by remember { mutableIntStateOf(0) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { _ -> }

    // Live call duration timer & status feedback
    LaunchedEffect(activeCallSession?.status) {
        val status = activeCallSession?.status
        if (status == "accepted") {
            callSeconds = 0
            while (activeCallSession?.status == "accepted") {
                delay(1000)
                callSeconds++
            }
        } else if (status == "declined") {
            Toast.makeText(context, if (isNepali) "कल अस्वीकार गरियो (Call declined)" else "Call was declined", Toast.LENGTH_SHORT).show()
            delay(1200)
            viewModel.endActiveCall(activeCallSession?.callId ?: "")
        }
    }

    // Direct phone dialer number
    var dialerNumber by remember { mutableStateOf("") }

    // RENDER FULLSCREEN ACTIVE CALL OVERLAY (REAL CLOUD VOIP CALL)
    if (activeCallSession != null) {
        val currentCall = activeCallSession!!
        val isCaller = currentCall.callerId == currentUser?.id
        val targetDisplayName = if (isCaller) (if (currentCall.receiverName.isNotBlank()) currentCall.receiverName else shopName) else currentCall.callerName

        FullScreenCallScreen(
            callType = currentCall.callType,
            targetName = targetDisplayName,
            targetPhone = shopPhone,
            isConnected = currentCall.status == "accepted",
            durationSeconds = callSeconds,
            isMicMuted = isMicMuted,
            isVideoOff = isVideoOff,
            isSpeakerOn = isSpeakerOn,
            isFrontCamera = isFrontCamera,
            isNepali = isNepali,
            onToggleMic = { isMicMuted = !isMicMuted },
            onToggleVideo = { isVideoOff = !isVideoOff },
            onToggleSpeaker = { isSpeakerOn = !isSpeakerOn },
            onFlipCamera = {
                isFrontCamera = !isFrontCamera
                Toast.makeText(context, if (isNepali) "क्यामेरा परिवर्तन गरियो" else "Camera switched", Toast.LENGTH_SHORT).show()
            },
            onEndCall = {
                viewModel.endActiveCall(currentCall.callId, callSeconds)
                Toast.makeText(context, if (isNepali) "कल समाप्त भयो" else "Call ended", Toast.LENGTH_SHORT).show()
            }
        )
        return
    }

    // Attach Product Dialog
    if (showProductAttachDialog) {
        val shopProducts = products.filter { it.mid == activeChatShopId }.take(8)
        AlertDialog(
            onDismissRequest = { showProductAttachDialog = false },
            title = {
                Text(
                    text = if (isNepali) "सामान छान्नुहोस् र सोधपुछ पठाउनुहोस्" else "Attach Product Inquiry",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().height(280.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(if (shopProducts.isNotEmpty()) shopProducts else products.take(6)) { prod ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.sendChatMessage(
                                        receiverId = activeChatShopId,
                                        text = "नमस्ते! मलाई यो सामानबारे सोध्नु थियो: ${prod.name} (रु. ${prod.effectivePrice})",
                                        attachedProductId = prod.id,
                                        attachedProductName = prod.name,
                                        attachedProductPrice = prod.effectivePrice
                                    )
                                    showProductAttachDialog = false
                                },
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("🛍️", fontSize = 20.sp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(prod.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("रु. ${prod.effectivePrice}", color = CrimsonRed, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                }
                                Text("पठाउनुहोस् ➔", color = NepalBlue, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showProductAttachDialog = false }) {
                    Text(if (isNepali) "बन्द गर्नुहोस्" else "Cancel")
                }
            }
        )
    }

    // Video & Video Link Share Dialog
    if (showVideoAttachDialog) {
        var videoUrlInput by remember { mutableStateOf("") }
        var videoTitleInput by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showVideoAttachDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.VideoLibrary, contentDescription = null, tint = CrimsonRed)
                    Spacer(Modifier.width(8.dp))
                    Text(if (isNepali) "भिडियो तथा भिडियो लिङ्क पठाउनुहोस्" else "Share Video / Video Link", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = if (isNepali) "YouTube, Reels वा MP4 भिडियोको लिङ्क हाल्नुहोस् वा नमुनाबाट छान्नुहोस्:" else "Enter YouTube, Reels, or MP4 link or pick a preset:",
                        style = MaterialTheme.typography.bodySmall
                    )
                    OutlinedTextField(
                        value = videoTitleInput,
                        onValueChange = { videoTitleInput = it },
                        label = { Text(if (isNepali) "भिडियो शीर्षक (Title)" else "Video Title") },
                        placeholder = { Text("जस्तै: जुम्ली मार्सी चामल भिडियो") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = videoUrlInput,
                        onValueChange = { videoUrlInput = it },
                        label = { Text(if (isNepali) "भिडियो लिङ्क URL" else "Video URL") },
                        placeholder = { Text("https://www.youtube.com/watch?v=...") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Text(if (isNepali) "द्रुत नमुनाहरू (Quick Samples):" else "Quick Samples:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.clickable {
                                videoTitleInput = "नेपाली अर्गानिक मार्सी चामल"
                                videoUrlInput = "https://www.youtube.com/watch?v=nepal_organic_rice"
                            }
                        ) {
                            Text("🌾 मार्सी चामल", fontSize = 11.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                        }
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.clickable {
                                videoTitleInput = "पाल्पाली ढाका टोपी प्रदर्शन"
                                videoUrlInput = "https://www.youtube.com/watch?v=palpali_dhaka"
                            }
                        ) {
                            Text("🧵 ढाका टोपी", fontSize = 11.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                        }
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.clickable {
                                videoTitleInput = "HD उत्पादन डेमो भिडियो"
                                videoUrlInput = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
                            }
                        ) {
                            Text("🎬 HD Demo MP4", fontSize = 11.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val finalUrl = videoUrlInput.ifBlank { "https://www.youtube.com/watch?v=merobazar_products" }
                        val finalTitle = videoTitleInput.ifBlank { "Mero Bazar Product Video" }
                        viewModel.sendChatMessage(
                            receiverId = activeChatShopId,
                            text = "🎥 भिडियो सेयर गरियो: $finalTitle",
                            videoUrl = finalUrl
                        )
                        showVideoAttachDialog = false
                        Toast.makeText(context, if (isNepali) "भिडियो पठाइयो" else "Video sent", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed)
                ) {
                    Text(if (isNepali) "पठाउनुहोस् (Send)" else "Send")
                }
            },
            dismissButton = {
                TextButton(onClick = { showVideoAttachDialog = false }) {
                    Text(if (isNepali) "रद्द" else "Cancel")
                }
            }
        )
    }

    // Block Confirmation Dialog
    if (showBlockDialog) {
        AlertDialog(
            onDismissRequest = { showBlockDialog = false },
            title = { Text(if (isNepali) "पसल ब्लक गर्नुहोस्" else "Block Shop", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    if (isNepali)
                        "के तपाईं $shopName लाई ब्लक गर्न चाहनुहुन्छ? ब्लक गरेपछि उहाँहरूले तपाईंलाई सन्देश वा कल गर्न सक्नुहुन्न।"
                    else
                        "Do you want to block $shopName? They will not be able to message or call you."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (isShopBlocked) {
                            viewModel.unblockShop(activeChatShopId)
                            Toast.makeText(context, if (isNepali) "अनब्लक गरियो" else "Unblocked", Toast.LENGTH_SHORT).show()
                        } else {
                            viewModel.blockShop(activeChatShopId)
                            Toast.makeText(context, if (isNepali) "ब्लक गरियो" else "Blocked", Toast.LENGTH_SHORT).show()
                        }
                        showBlockDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = if (isShopBlocked) NepalBlue else CrimsonRed)
                ) {
                    Text(if (isShopBlocked) (if (isNepali) "अनब्लक गर्नुहोस्" else "Unblock") else (if (isNepali) "ब्लक गर्नुहोस्" else "Block"))
                }
            },
            dismissButton = {
                TextButton(onClick = { showBlockDialog = false }) {
                    Text(if (isNepali) "रद्द" else "Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (isNepali) "💬 मेसेन्जर तथा अनलाइन कल" else "💬 Messenger & Calls",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                        Text(
                            text = "WebRTC P2P Server • Mero Bazar Live",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showBlockDialog = true }) {
                        Icon(
                            Icons.Default.Block,
                            contentDescription = "Block",
                            tint = if (isShopBlocked) CrimsonRed else MaterialTheme.colorScheme.onSurfaceVariant
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
            // Tabs: 1. Chat & Web Call, 2. Call History, 3. Direct Phone
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text(if (isNepali) "💬 च्याट र अनलाइन कल" else "Chat & Live Call", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text(if (isNepali) "📞 कल इतिहास (${callLogs.size})" else "Calls (${callLogs.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text(if (isNepali) "📱 फोन डाइलर" else "Direct Dialer", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                )
            }

            when (selectedTab) {
                0 -> {
                    // TAB 0: CHAT & LIVE CALLING
                    // Shop selector bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        shops.take(6).forEach { s ->
                            val sName = s.shop?.name ?: s.name
                            val isSelected = s.id == activeChatShopId
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = if (isSelected) NepalBlue else MaterialTheme.colorScheme.surface,
                                border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                                modifier = Modifier.clickable { activeChatShopId = s.id }
                            ) {
                                Text(
                                    text = "🏪 $sName",
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    // Chat Target Header & Audio/Video Call Triggers
                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = shopName,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Text(
                                    text = if (isShopBlocked) "🚫 ब्लक गरिएको" else "🟢 अनलाइन (P2P कलका लागि तयार)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isShopBlocked) CrimsonRed else JadeGreen,
                                    fontSize = 11.sp
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                // Live VoIP Audio Call Button
                                IconButton(
                                    onClick = {
                                        if (isShopBlocked) {
                                            Toast.makeText(context, if (isNepali) "पहिले अनब्लक गर्नुहोस्" else "Unblock first", Toast.LENGTH_SHORT).show()
                                        } else {
                                            permissionLauncher.launch(arrayOf(Manifest.permission.RECORD_AUDIO))
                                            viewModel.initiateCall(activeChatShopId, shopName, shopPhone, "audio")
                                        }
                                    },
                                    modifier = Modifier.testTag("audio_call_btn")
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = NepalBlue,
                                        modifier = Modifier.size(38.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(Icons.Default.Call, contentDescription = "Audio Call", tint = Color.White, modifier = Modifier.size(19.dp))
                                        }
                                    }
                                }

                                // Live WebRTC Video Call Button
                                IconButton(
                                    onClick = {
                                        if (isShopBlocked) {
                                            Toast.makeText(context, if (isNepali) "पहिले अनब्लक गर्नुहोस्" else "Unblock first", Toast.LENGTH_SHORT).show()
                                        } else {
                                            permissionLauncher.launch(arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO))
                                            viewModel.initiateCall(activeChatShopId, shopName, shopPhone, "video")
                                        }
                                    },
                                    modifier = Modifier.testTag("video_call_btn")
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = CrimsonRed,
                                        modifier = Modifier.size(38.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(Icons.Default.Videocam, contentDescription = "Video Call", tint = Color.White, modifier = Modifier.size(20.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Filter messages strictly for current active conversation
                    val conversationMessages = remember(allMessages, activeChatShopId, currentUser) {
                        val myId = currentUser?.id ?: "me"
                        allMessages.filter { msg ->
                            (msg.senderId == myId && msg.receiverId == activeChatShopId) ||
                            (msg.senderId == activeChatShopId && msg.receiverId == myId) ||
                            (msg.receiverId == activeChatShopId) ||
                            (msg.senderId == activeChatShopId)
                        }
                    }

                    // Auto-scroll to latest message on arrival
                    LaunchedEffect(conversationMessages.size) {
                        if (conversationMessages.isNotEmpty()) {
                            listState.animateScrollToItem(conversationMessages.size - 1)
                        }
                    }

                    // Chat Messages List
                    if (conversationMessages.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Surface(
                                    shape = CircleShape,
                                    color = NepalBlue.copy(alpha = 0.1f),
                                    modifier = Modifier.size(68.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text("💬", fontSize = 32.sp)
                                    }
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = if (isNepali) "नयाँ कुराकानी सुरु गर्नुहोस्" else "Start a new conversation",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (isNepali)
                                        "$shopName सँग प्रत्यक्ष अनलाइन च्याट गर्न तल सन्देश, सामान सोधपुछ, फोटो वा भिडियो पठाउनुहोस्।"
                                    else
                                        "Send a message, product inquiry, photo, or video to chat with $shopName.",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp),
                            contentPadding = PaddingValues(vertical = 10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(conversationMessages) { msg ->
                            val isMe = msg.senderId == (currentUser?.id ?: "me")

                            if (msg.isCallEvent) {
                                // Call log message bubble
                                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                    ) {
                                        Text(
                                            text = msg.text,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontWeight = FontWeight.Medium,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                        )
                                    }
                                }
                            } else {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(
                                            topStart = 16.dp,
                                            topEnd = 16.dp,
                                            bottomStart = if (isMe) 16.dp else 2.dp,
                                            bottomEnd = if (isMe) 2.dp else 16.dp
                                        ),
                                        color = if (isMe) NepalBlue else MaterialTheme.colorScheme.surfaceVariant,
                                        border = if (isMe) null else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                        modifier = Modifier.widthIn(max = 290.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                                            if (!isMe) {
                                                Text(
                                                    text = msg.senderName,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = NepalBlue,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Spacer(modifier = Modifier.height(2.dp))
                                            }

                                            // Attached Product card inside message
                                            if (msg.attachedProductName.isNotBlank()) {
                                                Surface(
                                                    shape = RoundedCornerShape(8.dp),
                                                    color = if (isMe) Color(0x33FFFFFF) else MaterialTheme.colorScheme.surface,
                                                    modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)
                                                ) {
                                                    Row(
                                                        modifier = Modifier.padding(8.dp),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Text("🛍️", fontSize = 16.sp)
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                        Column {
                                                            Text(
                                                                text = msg.attachedProductName,
                                                                fontSize = 11.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                color = if (isMe) Color.White else MaterialTheme.colorScheme.onSurface
                                                            )
                                                            if (msg.attachedProductPrice > 0L) {
                                                                Text(
                                                                    text = "रु. ${msg.attachedProductPrice}",
                                                                    fontSize = 11.sp,
                                                                    fontWeight = FontWeight.SemiBold,
                                                                    color = if (isMe) Color(0xFFFFD700) else CrimsonRed
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                            }

                                            // Photo attached
                                            if (msg.photoUrl.isNotBlank()) {
                                                Surface(
                                                    shape = RoundedCornerShape(10.dp),
                                                    color = Color.Black.copy(alpha = 0.2f),
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(150.dp)
                                                        .padding(bottom = 6.dp)
                                                        .clip(RoundedCornerShape(10.dp))
                                                        .clickable {
                                                            try {
                                                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(msg.photoUrl))
                                                                context.startActivity(intent)
                                                            } catch (_: Exception) {}
                                                        }
                                                ) {
                                                    Box(contentAlignment = Alignment.Center) {
                                                        AsyncImage(
                                                            model = msg.photoUrl,
                                                            contentDescription = "Shared photo",
                                                            modifier = Modifier.fillMaxSize(),
                                                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                                        )
                                                        Surface(
                                                            shape = RoundedCornerShape(8.dp),
                                                            color = Color.Black.copy(alpha = 0.6f),
                                                            modifier = Modifier.align(Alignment.BottomStart).padding(6.dp)
                                                        ) {
                                                            Text("📷 फोटो प्रमाण", color = Color.White, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                                        }
                                                    }
                                                }
                                            }

                                            // Video clip / Video link attached
                                            if (msg.videoUrl.isNotBlank()) {
                                                Surface(
                                                    shape = RoundedCornerShape(10.dp),
                                                    color = if (isMe) Color(0x33FFFFFF) else Color(0xFF1E293B),
                                                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isMe) Color.White.copy(alpha = 0.4f) else CrimsonRed.copy(alpha = 0.6f)),
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(bottom = 6.dp)
                                                        .clickable {
                                                            try {
                                                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(msg.videoUrl))
                                                                context.startActivity(intent)
                                                            } catch (e: Exception) {
                                                                Toast.makeText(context, "भिडियो लिङ्क: ${msg.videoUrl}", Toast.LENGTH_SHORT).show()
                                                            }
                                                        }
                                                ) {
                                                    Row(
                                                        modifier = Modifier.padding(10.dp),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Surface(
                                                            shape = CircleShape,
                                                            color = CrimsonRed,
                                                            modifier = Modifier.size(36.dp)
                                                        ) {
                                                            Box(contentAlignment = Alignment.Center) {
                                                                Icon(Icons.Default.PlayArrow, contentDescription = "Play Video", tint = Color.White, modifier = Modifier.size(22.dp))
                                                            }
                                                        }
                                                        Spacer(modifier = Modifier.width(10.dp))
                                                        Column(modifier = Modifier.weight(1f)) {
                                                            Text(
                                                                text = "🎬 अनलाइन भिडियो हेर्नुहोस् (Play Video)",
                                                                color = if (isMe) Color.White else Color(0xFF38BDF8),
                                                                fontSize = 12.sp,
                                                                fontWeight = FontWeight.Bold
                                                            )
                                                            Text(
                                                                text = msg.videoUrl,
                                                                color = if (isMe) Color.White.copy(alpha = 0.7f) else Color.LightGray,
                                                                fontSize = 10.sp,
                                                                maxLines = 1,
                                                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                                            )
                                                        }
                                                    }
                                                }
                                            }

                                            Text(
                                                text = msg.text,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = if (isMe) Color.White else MaterialTheme.colorScheme.onSurface
                                            )

                                            Spacer(modifier = Modifier.height(4.dp))
                                            Row(
                                                modifier = Modifier.align(Alignment.End),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                val timeFormatted = remember(msg.timestamp) {
                                                    try {
                                                        java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault()).format(java.util.Date(msg.timestamp))
                                                    } catch (_: Exception) { "" }
                                                }
                                                Text(
                                                    text = timeFormatted,
                                                    fontSize = 9.sp,
                                                    color = if (isMe) Color.White.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                                )
                                                if (isMe) {
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(
                                                        text = "✓✓",
                                                        fontSize = 9.sp,
                                                        color = Color(0xFF86EFAC),
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                    // Quick Smart Reply Pills (Section B 5.2 AI Quick Reply)
                    val quickReplies = listOf(
                        "नमस्कार! के सामान उपलब्ध छ?",
                        "डेलिभरी कति समयमा हुन्छ?",
                        "सामानको वारेन्टी कति समय छ?",
                        "अनलाइन भुक्तानी स्वीकृत छ?",
                        "धन्यवाद!"
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        quickReplies.forEach { reply ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                modifier = Modifier.clickable { messageInput = reply }
                            ) {
                                Text(
                                    text = reply,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Chat Input Bar with Attachments
                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Photo attach button
                            IconButton(onClick = {
                                viewModel.sendChatMessage(
                                    receiverId = activeChatShopId,
                                    text = "📷 सामानको फोटो प्रमाण पठाइयो।",
                                    photoUrl = "https://images.unsplash.com/photo-1542838132-92c53300491e?auto=format&fit=crop&w=400&q=80"
                                )
                                Toast.makeText(context, if (isNepali) "फोटो जोडिएर पठाइयो" else "Photo sent", Toast.LENGTH_SHORT).show()
                            }) {
                                Icon(Icons.Default.CameraAlt, contentDescription = "Send Photo", tint = NepalBlue)
                            }

                            // Video & Video Link attach button
                            IconButton(onClick = { showVideoAttachDialog = true }) {
                                Icon(Icons.Default.VideoLibrary, contentDescription = "Send Video", tint = CrimsonRed)
                            }

                            // Product attach button
                            IconButton(onClick = { showProductAttachDialog = true }) {
                                Icon(Icons.Default.ShoppingBag, contentDescription = "Attach Product", tint = JadeGreen)
                            }

                            OutlinedTextField(
                                value = messageInput,
                                onValueChange = { messageInput = it },
                                placeholder = { Text(if (isNepali) "सन्देश लेख्नुहोस्..." else "Type message...") },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("chat_input_field"),
                                shape = RoundedCornerShape(20.dp),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.width(6.dp))

                            IconButton(
                                onClick = {
                                    if (messageInput.isNotBlank()) {
                                        viewModel.sendChatMessage(
                                            receiverId = activeChatShopId,
                                            text = messageInput.trim()
                                        )
                                        messageInput = ""
                                        // Auto-reply simulation only for demo fallback shop_1
                                        if (activeChatShopId == "shop_1") {
                                            scope.launch {
                                                delay(1800)
                                                viewModel.sendChatMessage(
                                                    receiverId = currentUser?.id ?: "me",
                                                    text = "नमस्ते! तपाईंको सन्देश प्राप्त भयो। अर्डर तुरुन्तै प्याकिङ प्रक्रियामा राखिनेछ।"
                                                )
                                            }
                                        }
                                    }
                                },
                                modifier = Modifier.testTag("chat_send_btn")
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = NepalBlue,
                                    modifier = Modifier.size(42.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = Color.White, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }
                }

                1 -> {
                    // TAB 1: CALL HISTORY (SECTION A 4)
                    if (callLogs.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                text = if (isNepali) "हालसम्म कुनै कल रेकर्ड छैन।" else "No call history yet.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize().padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(callLogs) { entry ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            shape = CircleShape,
                                            color = if (entry.callType == "video") CrimsonRed.copy(alpha = 0.15f) else NepalBlue.copy(alpha = 0.15f),
                                            modifier = Modifier.size(44.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    if (entry.callType == "video") Icons.Default.Videocam else Icons.Default.Call,
                                                    contentDescription = null,
                                                    tint = if (entry.callType == "video") CrimsonRed else NepalBlue
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(entry.targetName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            val durStr = if (entry.durationSeconds > 0)
                                                "${entry.durationSeconds / 60}m ${entry.durationSeconds % 60}s"
                                            else "मिस्ड कल"
                                            Text(
                                                text = "${if (entry.callType == "video") "भिडियो कल" else "अडियो कल"} • $durStr",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }

                                        // Call back button
                                        IconButton(onClick = {
                                            activeChatShopId = entry.targetId
                                            permissionLauncher.launch(
                                                if (entry.callType == "video")
                                                    arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
                                                else
                                                    arrayOf(Manifest.permission.RECORD_AUDIO)
                                            )
                                            viewModel.initiateCall(
                                                targetId = entry.targetId,
                                                targetName = entry.targetName,
                                                targetPhone = entry.targetPhone,
                                                callType = entry.callType
                                            )
                                        }) {
                                            Icon(
                                                if (entry.callType == "video") Icons.Default.Videocam else Icons.Default.Call,
                                                contentDescription = "Call Back",
                                                tint = NepalBlue
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                2 -> {
                    // TAB 2: DIRECT PHONE CELLULAR DIALER
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
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = if (isNepali) "📱 डाइरेक्ट मोबाइल कलिङ" else "📱 Direct Phone Dialer",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = if (isNepali)
                                            "सिम तथा साधारण फोन नेटवर्कबाट सिधै कल गर्न तल नम्बर प्रविष्ट गर्नुहोस् वा पसलको आधिकारिक नम्बरमा सम्पर्क गर्नुहोस्।"
                                        else
                                            "Place a traditional cellular call directly using your device SIM network.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    OutlinedTextField(
                                        value = dialerNumber,
                                        onValueChange = { dialerNumber = it.filter { c -> c.isDigit() || c == '+' } },
                                        label = { Text(if (isNepali) "फोन नम्बर (98XXXXXXXX) *" else "Phone Number *") },
                                        modifier = Modifier.fillMaxWidth().testTag("direct_dial_input"),
                                        singleLine = true
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Button(
                                        onClick = {
                                            val num = dialerNumber.ifBlank { shopPhone }
                                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$num"))
                                            try {
                                                context.startActivity(intent)
                                            } catch (e: Exception) {
                                                Toast.makeText(context, "डाइलर खोल्न सकिएन: ${e.message}", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = JadeGreen),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.fillMaxWidth().height(48.dp).testTag("direct_call_submit_btn")
                                    ) {
                                        Icon(Icons.Default.Phone, contentDescription = null)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(if (isNepali) "सिमबाट सिधै कल गर्नुहोस्" else "Call via Phone SIM")
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

/**
 * Interactive Fullscreen Live Audio / Video Call UI with WebRTC P2P simulation,
 * live timer, local video PIP, microphone mute, camera toggle, and sound ripples.
 */
@Composable
fun FullScreenCallScreen(
    callType: String, // "audio" or "video"
    targetName: String,
    targetPhone: String,
    isConnected: Boolean,
    durationSeconds: Int,
    isMicMuted: Boolean,
    isVideoOff: Boolean,
    isSpeakerOn: Boolean,
    isFrontCamera: Boolean,
    isNepali: Boolean,
    onToggleMic: () -> Unit,
    onToggleVideo: () -> Unit,
    onToggleSpeaker: () -> Unit,
    onFlipCamera: () -> Unit,
    onEndCall: () -> Unit
) {
    val durationFormatted = String.format("%02d:%02d", durationSeconds / 60, durationSeconds % 60)

    val transition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by transition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (callType == "video" && !isVideoOff) Color(0xFF0A0F1D) else Color(0xFF0F172A))
    ) {
        if (callType == "video" && !isVideoOff) {
            // Simulated Fullscreen Remote Video Feed
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF1E293B), Color(0xFF0F172A), Color(0xFF020617))
                        )
                    )
            ) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.1f),
                        modifier = Modifier.size(120.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("🏪", fontSize = 54.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "HD 1080p Live Stream (WebRTC)",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 12.sp
                    )
                }

                // Floating Picture-in-Picture Local Camera View
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF1E293B),
                    border = androidx.compose.foundation.BorderStroke(2.dp, Color.White),
                    modifier = Modifier
                        .size(width = 110.dp, height = 150.dp)
                        .align(Alignment.TopEnd)
                        .padding(top = 48.dp, end = 16.dp)
                        .clip(RoundedCornerShape(16.dp))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        LiveCameraPreviewView(
                            isFrontCamera = isFrontCamera,
                            modifier = Modifier.fillMaxSize()
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color.Black.copy(alpha = 0.55f),
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 6.dp)
                        ) {
                            Text(
                                text = if (isFrontCamera) "अगाडि (Front)" else "पछाडि (Back)",
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        } else {
            // Audio Call Mode: Center Pulse Avatar
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (isConnected) {
                        Box(
                            modifier = Modifier
                                .size(160.dp)
                                .scale(pulseScale)
                                .clip(CircleShape)
                                .background(NepalBlue.copy(alpha = 0.25f))
                        )
                    }
                    Surface(
                        shape = CircleShape,
                        color = NepalBlue,
                        modifier = Modifier.size(110.dp),
                        shadowElevation = 8.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("🏪", fontSize = 48.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = targetName,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = targetPhone,
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (isConnected) JadeGreen.copy(alpha = 0.2f) else Color(0x33FFFFFF)
                ) {
                    Text(
                        text = if (isConnected) "🟢 $durationFormatted (इन्क्रिप्टेड कल)" else "📞 घन्टी जाँदैछ (Ringing)...",
                        color = if (isConnected) JadeGreen else Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }
            }
        }

        // Top Bar info during call
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 44.dp, start = 18.dp, end = 18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = targetName,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = if (isConnected) "🟢 $durationFormatted" else "कलिङ हुँदैछ...",
                    color = if (isConnected) JadeGreen else Color.White.copy(alpha = 0.8f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            if (callType == "video") {
                IconButton(onClick = onFlipCamera) {
                    Surface(
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.2f),
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Cameraswitch, contentDescription = "Flip Camera", tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
        }

        // Bottom Call Controls Bar
        Surface(
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            color = Color(0xFF1E293B).copy(alpha = 0.95f),
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 22.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Mic Mute / Unmute
                IconButton(onClick = onToggleMic) {
                    Surface(
                        shape = CircleShape,
                        color = if (isMicMuted) CrimsonRed else Color.White.copy(alpha = 0.2f),
                        modifier = Modifier.size(50.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                if (isMicMuted) Icons.Default.MicOff else Icons.Default.Mic,
                                contentDescription = "Mute Mic",
                                tint = Color.White
                            )
                        }
                    }
                }

                // Video On / Off (if in video call)
                if (callType == "video") {
                    IconButton(onClick = onToggleVideo) {
                        Surface(
                            shape = CircleShape,
                            color = if (isVideoOff) CrimsonRed else Color.White.copy(alpha = 0.2f),
                            modifier = Modifier.size(50.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    if (isVideoOff) Icons.Default.VideocamOff else Icons.Default.Videocam,
                                    contentDescription = "Video On/Off",
                                    tint = Color.White
                                )
                            }
                        }
                    }
                }

                // Speaker Toggle
                IconButton(onClick = onToggleSpeaker) {
                    Surface(
                        shape = CircleShape,
                        color = if (isSpeakerOn) NepalBlue else Color.White.copy(alpha = 0.2f),
                        modifier = Modifier.size(50.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.VolumeUp, contentDescription = "Speaker", tint = Color.White)
                        }
                    }
                }

                // End Call Button (Big Red)
                IconButton(onClick = onEndCall) {
                    Surface(
                        shape = CircleShape,
                        color = CrimsonRed,
                        modifier = Modifier.size(58.dp),
                        shadowElevation = 8.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.CallEnd, contentDescription = "End Call", tint = Color.White, modifier = Modifier.size(28.dp))
                        }
                    }
                }
            }
        }
    }
}

/**
 * CameraX live preview composable for video call PIP and live camera streaming
 */
@Composable
fun LiveCameraPreviewView(
    isFrontCamera: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx)
            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
            cameraProviderFuture.addListener({
                try {
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }
                    val selector = if (isFrontCamera) CameraSelector.DEFAULT_FRONT_CAMERA else CameraSelector.DEFAULT_BACK_CAMERA
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(lifecycleOwner, selector, preview)
                } catch (e: Exception) {
                    android.util.Log.e("CameraPreview", "Camera preview init exception: ${e.message}")
                }
            }, ContextCompat.getMainExecutor(ctx))
            previewView
        },
        update = { previewView ->
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            cameraProviderFuture.addListener({
                try {
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }
                    val selector = if (isFrontCamera) CameraSelector.DEFAULT_FRONT_CAMERA else CameraSelector.DEFAULT_BACK_CAMERA
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(lifecycleOwner, selector, preview)
                } catch (e: Exception) {
                    android.util.Log.e("CameraPreview", "Camera preview update exception: ${e.message}")
                }
            }, ContextCompat.getMainExecutor(context))
        },
        modifier = modifier
    )
}
