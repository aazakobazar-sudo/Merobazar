package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChatMessage
import com.example.data.repository.AiModule
import com.example.ui.MainViewModel
import com.example.ui.theme.BrandBgLight
import com.example.ui.theme.BrandIndigo
import com.example.ui.theme.BrandSaffron

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiShoppingAssistantScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit,
    onOpenNepalMap: () -> Unit = {}
) {
    val isNepali by viewModel.isNepali.collectAsState()
    val messages by viewModel.aiMessages.collectAsState()
    val isAiLoading by viewModel.isAiLoading.collectAsState()
    val activeModule by viewModel.activeAiModule.collectAsState()
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(BrandSaffron),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (isNepali) "मेरो बजार स्मार्ट AI" else "Mero Bazar Smart AI",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = if (isNepali) "लोकेसन • पसल • समस्या • सहयोग • डाटा एनालाइसिस" else "Location • Shops • Issues • Support • Analytics",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 10.sp
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
                    TextButton(onClick = onOpenNepalMap) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Map, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isNepali) "नेपाल नक्सा" else "Map", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BrandIndigo)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(BrandBgLight)
        ) {
            // 5 Dedicated AI Module Selector Chips
            Surface(
                color = Color.White,
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AiModule.values().forEach { module ->
                        val isSelected = activeModule == module
                        Surface(
                            modifier = Modifier
                                .clickable {
                                    viewModel.setAiModule(module)
                                    inputText = module.defaultQuery
                                }
                                .testTag("ai_module_${module.name.lowercase()}"),
                            shape = RoundedCornerShape(20.dp),
                            color = if (isSelected) BrandIndigo else Color(0xFFF1F5F9),
                            border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(module.icon, fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isNepali) module.titleNe else module.titleEn,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            // Chat conversation list
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item { Spacer(modifier = Modifier.height(6.dp)) }

                items(messages) { msg ->
                    ChatBubble(message = msg)
                }

                if (isAiLoading) {
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(8.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = BrandIndigo,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isNepali) "स्मार्ट AI विश्लेषण गर्दैछ..." else "AI is analyzing system data...",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(6.dp)) }
            }

            // Quick Prompt suggestions based on active module
            val suggestions = when (activeModule) {
                AiModule.LOCATION -> if (isNepali) listOf(
                    "काठमाडौँ र बागमतीका पसलहरू देखाउनुहोस्",
                    "पोखरामा कुन कुन सामान उपलब्ध छन्?",
                    "विराटनगर र चितवनमा डेलिभरी समय कति लाग्छ?"
                ) else listOf(
                    "Show shops in Kathmandu & Bagmati",
                    "What items are in Pokhara?",
                    "Delivery times for Biratnagar & Chitwan"
                )

                AiModule.SHOPS -> if (isNepali) listOf(
                    "किराना र खाद्यान्नका उत्कृष्ट पसलहरू",
                    "हार्डवेयर र निर्माण सामग्री पसल",
                    "व्यापारीले सामानको विवरण कसरी राम्रो लेख्ने?"
                ) else listOf(
                    "Best grocery and food shops",
                    "Hardware & construction shops",
                    "How to write great product titles?"
                )

                AiModule.PROBLEM -> if (isNepali) listOf(
                    "मेरो अर्डर ढिलो भयो, के गर्ने?",
                    "eSewa भुक्तानी भयो तर अर्डर देखिएन",
                    "सामान फिर्ता गर्ने नियम के हो?"
                ) else listOf(
                    "Order delayed, what should I do?",
                    "Paid via eSewa but order not verified",
                    "What is the return policy?"
                )

                AiModule.SUPPORT -> if (isNepali) listOf(
                    "५० क्विन उपहार कसरी प्रयोग गर्ने?",
                    "व्यापारी दर्ता र रु १०० को सदस्यता",
                    "दर्ता प्रमाण पत्र कहाँ हेर्ने?"
                ) else listOf(
                    "How to use 50 bonus coins?",
                    "Merchant registration & Rs 100 subscription",
                    "Where to view registration certificate?"
                )

                AiModule.ANALYTICS -> if (isNepali) listOf(
                    "मेरो बजारको कुल सामान र स्टक विश्लेषण",
                    "सबैभन्दा बढी माग भएका सामानहरू",
                    "जिल्ला अनुसार पसलको फैलावट"
                ) else listOf(
                    "Total catalog & stock analytics",
                    "Highest demand product categories",
                    "Shop distribution across districts"
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                suggestions.forEach { suggestion ->
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color.White,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDCE5F1)),
                        modifier = Modifier
                            .clickable {
                                inputText = suggestion
                                viewModel.sendAiMessage(suggestion, activeModule)
                                inputText = ""
                            }
                            .testTag("ai_suggestion_chip")
                    ) {
                        Text(
                            text = suggestion,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            color = BrandIndigo,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Bottom input bar
            Surface(
                color = Color.White,
                shadowElevation = 4.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = {
                            Text(
                                text = if (isNepali) "AI लाई प्रश्न सोध्नुहोस्..." else "Ask AI assistant...",
                                fontSize = 13.sp
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("ai_input_field"),
                        shape = RoundedCornerShape(24.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color(0xFFCBD5E1),
                            focusedBorderColor = BrandIndigo
                        )
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = {
                            if (inputText.isNotBlank()) {
                                val text = inputText.trim()
                                inputText = ""
                                viewModel.sendAiMessage(text, activeModule)
                            }
                        },
                        enabled = inputText.isNotBlank() && !isAiLoading,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(if (inputText.isNotBlank()) BrandIndigo else Color(0xFFE2E8F0))
                            .testTag("ai_send_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            tint = if (inputText.isNotBlank()) Color.White else Color.Gray,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatBubble(message: ChatMessage) {
    val isUser = !message.isAi

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(BrandSaffron),
                contentAlignment = Alignment.Center
            ) {
                Text("🤖", fontSize = 14.sp)
            }
            Spacer(modifier = Modifier.width(6.dp))
        }

        Surface(
            shape = RoundedCornerShape(
                topStart = 14.dp,
                topEnd = 14.dp,
                bottomStart = if (isUser) 14.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 14.dp
            ),
            color = if (isUser) BrandIndigo else Color.White,
            border = if (isUser) null else androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDCE5F1)),
            modifier = Modifier.widthIn(max = 300.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                if (!isUser) {
                    Text(
                        text = message.senderName,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandSaffron,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isUser) Color.White else MaterialTheme.colorScheme.onSurface,
                    lineHeight = 20.sp
                )
            }
        }

        if (isUser) {
            Spacer(modifier = Modifier.width(6.dp))
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(BrandIndigo),
                contentAlignment = Alignment.Center
            ) {
                Text("👤", fontSize = 14.sp)
            }
        }
    }
}
