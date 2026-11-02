package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Publish
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.MainViewModel
import com.example.ui.theme.BrandBgLight
import com.example.ui.theme.BrandIndigo
import com.example.ui.theme.BrandSaffron
import com.example.ui.theme.DarkCrimson
import com.example.ui.theme.JadeGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppUpdateScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val isNepali by viewModel.isNepali.collectAsState()
    val updateInfo by viewModel.appUpdateInfo.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val isAdmin = currentUser?.email.equals("aazakobazar@gmail.com", ignoreCase = true) && currentUser?.role == "a"

    var isChecking by remember { mutableStateOf(false) }

    // Admin APK management state
    var editVersion by remember { mutableStateOf(updateInfo.latestVersion) }
    var editDirectApk by remember { mutableStateOf(updateInfo.directApkUrl) }
    var editGithubRelease by remember { mutableStateOf(updateInfo.githubReleaseUrl) }
    var editNotes by remember { mutableStateOf(updateInfo.releaseNotes) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isNepali) "सफ्टवेयर तथा APK अपडेट (GitHub)" else "Software & APK Update",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // App Branding Icon & Current Version
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDCE5F1))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.logo_mero_bazar),
                        contentDescription = "Logo",
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(BrandIndigo.copy(alpha = 0.1f))
                            .padding(4.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (isNepali) "मेरो बजार (Mero Bazar)" else "Mero Bazar",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = BrandIndigo
                    )
                    Text(
                        text = "हालको संस्करण: v${updateInfo.currentVersion} (Build 24)",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFFE8F5E9)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.CloudDone, contentDescription = null, tint = JadeGreen, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isNepali) "🟢 अनलाइन क्लाउड जोडिएको (${updateInfo.lastSyncTime})" else "🟢 Online Cloud Synced",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = JadeGreen
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // GitHub & Admin Account Badge
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDCE5F1))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Email, contentDescription = null, tint = BrandIndigo, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "आधिकारिक प्रणाली व्यवस्थापक:",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "मेरो बजार मुख्य प्रशासक (Master Admin)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = BrandIndigo
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "रेपोजिटरी: https://github.com/aazakobazar/mero-bazar",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Auto-Upgrade Setting Switch Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDCE5F1))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.CloudSync, contentDescription = null, tint = BrandIndigo, modifier = Modifier.size(32.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isNepali) "अनलाइन जोडिँदा स्वतः अपग्रेड" else "Auto-Upgrade on Connect",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = BrandIndigo
                        )
                        Text(
                            text = if (isNepali)
                                "नयाँ भर्सन रिलिज हुनासाथ एपले स्वतः नयाँ APK पत्ता लगाई अपग्रेड गर्नेछ।"
                            else
                                "App automatically detects new release and prepares upgrade.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = updateInfo.autoUpgradeEnabled,
                        onCheckedChange = { viewModel.setAutoUpgrade(it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = BrandIndigo)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Update Status & Release Information Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (updateInfo.isUpdateAvailable) Color(0xFFFFF9E6) else Color.White
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (updateInfo.isUpdateAvailable) BrandSaffron else Color(0xFFDCE5F1)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (updateInfo.isUpdateAvailable) Icons.Default.SystemUpdate else Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = if (updateInfo.isUpdateAvailable) BrandSaffron else JadeGreen
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (updateInfo.isUpdateAvailable)
                                (if (isNepali) "🚀 नयाँ अपडेट v${updateInfo.latestVersion} उपलब्ध छ!" else "Update v${updateInfo.latestVersion} Available!")
                            else
                                (if (isNepali) "तपाईंको एप पछिल्लो संस्करणमा छ (v${updateInfo.currentVersion})" else "Your app is up to date"),
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = if (updateInfo.isUpdateAvailable) Color(0xFF7A4E00) else JadeGreen
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = if (isNepali) "नयाँ सुधारहरू (Release Notes):" else "Release Notes:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = updateInfo.releaseNotes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            // ADMIN EXCLUSIVE: Publish / Edit APK Release Section
            if (isAdmin) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF3F0FF)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF7C3AED))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = Color(0xFF7C3AED))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "👑 मुख्य एडमिन: नयाँ APK रिलिज प्रकाशित गर्नुहोस्",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF7C3AED),
                                fontSize = 14.sp
                            )
                        }
                        Text(
                            text = "यहीँबाट नयाँ APK लिङ्क लेखेर सार्वजनिक गर्नुहोस्, सम्पूर्ण एपमा अटोमेटिक सूचना जानेछ:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 6.dp)
                        )

                        OutlinedTextField(
                            value = editVersion,
                            onValueChange = { editVersion = it },
                            label = { Text("नयाँ भर्सन (उदा. 1.0.2)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = editDirectApk,
                            onValueChange = { editDirectApk = it },
                            label = { Text("सिधै APK डाउनलोड URL (Direct APK Link)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = editGithubRelease,
                            onValueChange = { editGithubRelease = it },
                            label = { Text("GitHub Release पृष्ठ URL") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = editNotes,
                            onValueChange = { editNotes = it },
                            label = { Text("अपडेट विवरण (Release Notes)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                viewModel.updateAppReleaseInfo(
                                    version = editVersion,
                                    directApkUrl = editDirectApk,
                                    githubReleaseUrl = editGithubRelease,
                                    notes = editNotes
                                )
                                Toast.makeText(context, "नयाँ APK रिलिज v$editVersion सार्वजनिक गरियो!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) {
                            Icon(Icons.Default.Publish, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("🚀 नयाँ APK अपडेट सार्वजनिक गर्नुहोस्", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Action 1: Check for Updates Button
            Button(
                onClick = {
                    isChecking = true
                    viewModel.checkForAppUpdates()
                    Toast.makeText(context, if (isNepali) "क्लाउडमा नयाँ संस्करण जाँच गरियो!" else "Checked for updates!", Toast.LENGTH_SHORT).show()
                    isChecking = false
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrandIndigo),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("check_update_button"),
                enabled = !isChecking
            ) {
                if (isChecking) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                } else {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isNepali) "अपडेट जाँच गर्नुहोस् (Check for Updates)" else "Check for Updates",
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action 2: GitHub APK Download Button
            Button(
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(updateInfo.directApkUrl))
                    try {
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        Toast.makeText(context, "URL: ${updateInfo.directApkUrl}", Toast.LENGTH_LONG).show()
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrandSaffron),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("apk_download_button")
            ) {
                Icon(Icons.Default.CloudDownload, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isNepali) "GitHub बाट APK डाउनलोड गर्नुहोस् (Download APK)" else "Download APK from GitHub",
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action 3: GitHub Releases Page
            OutlinedButton(
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(updateInfo.githubReleaseUrl))
                    try {
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        Toast.makeText(context, "URL: ${updateInfo.githubReleaseUrl}", Toast.LENGTH_LONG).show()
                    }
                },
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, BrandIndigo),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("direct_apk_install_btn")
            ) {
                Icon(Icons.Default.OpenInBrowser, contentDescription = null, tint = BrandIndigo)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isNepali) "GitHub रिलिज पृष्ठ खोल्नुहोस् (Releases)" else "Open GitHub Releases",
                    fontWeight = FontWeight.SemiBold,
                    color = BrandIndigo
                )
            }
        }
    }
}
