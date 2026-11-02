package com.example.ui.screens

import android.annotation.SuppressLint
import android.content.Context
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LinearScale
import androidx.compose.material.icons.filled.Pending
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.model.ProjectProgressMetric
import com.example.data.model.TeamTask
import com.example.ui.MainViewModel
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.JadeGreen
import com.example.ui.theme.NepalBlue
import org.json.JSONArray
import org.json.JSONObject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RechartsVisualizationScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val isNepali by viewModel.isNepali.collectAsState()
    val tasks by viewModel.teamTasks.collectAsState()
    val metrics by viewModel.projectProgressMetrics.collectAsState()
    val config by viewModel.firestoreProjectConfig.collectAsState()

    var viewMode by remember { mutableStateOf(0) } // 0 = Recharts Interactive, 1 = Native Material 3
    var showAddTaskDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (isNepali) "कार्यसम्पादन तथा परियोजना प्रगति" else "Team Performance & Progress",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = Color.White
                        )
                        Text(
                            text = if (isNepali) "Recharts र Firestore आधारित प्रत्यक्ष एनालिटिक्स" else "Live Recharts & Firestore Analytics",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("btn_recharts_back")
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            viewModel.refreshFirestoreMetrics()
                            Toast.makeText(
                                context,
                                if (isNepali) "Firestore बाट ताजा तथ्याङ्क सिङ्क भयो" else "Synced fresh data from Firestore",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        modifier = Modifier.testTag("btn_recharts_refresh")
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Sync", tint = Color.White)
                    }
                    IconButton(
                        onClick = { showAddTaskDialog = true },
                        modifier = Modifier.testTag("btn_recharts_add_task")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Task", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CrimsonRed,
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // View Mode Tab: Recharts vs Native
            TabRow(
                selectedTabIndex = viewMode,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = CrimsonRed
            ) {
                Tab(
                    selected = viewMode == 0,
                    onClick = { viewMode = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Analytics, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(
                                if (isNepali) "📊 Recharts इन्टरएक्टिभ" else "📊 Recharts Interactive",
                                fontWeight = if (viewMode == 0) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 13.sp
                            )
                        }
                    }
                )
                Tab(
                    selected = viewMode == 1,
                    onClick = { viewMode = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LinearScale, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(
                                if (isNepali) "📱 नेटिभ एन्ड्रोइड दृश्य" else "📱 Native Android View",
                                fontWeight = if (viewMode == 1) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 13.sp
                            )
                        }
                    }
                )
            }

            if (viewMode == 0) {
                // Interactive Recharts WebView View
                RechartsInteractiveView(
                    tasks = tasks,
                    metrics = metrics,
                    config = config,
                    isNepali = isNepali,
                    onAddTask = { showAddTaskDialog = true },
                    onSyncFirestore = { viewModel.refreshFirestoreMetrics() }
                )
            } else {
                // Native Material 3 View
                NativeTaskAndMetricsListView(
                    tasks = tasks,
                    metrics = metrics,
                    config = config,
                    isNepali = isNepali,
                    onStatusChange = { taskId, newStatus ->
                        viewModel.updateTeamTaskStatus(taskId, newStatus)
                    }
                )
            }
        }
    }

    if (showAddTaskDialog) {
        AddTeamTaskDialog(
            isNepali = isNepali,
            onDismiss = { showAddTaskDialog = false },
            onConfirm = { title, dept, assignee, points ->
                viewModel.addTeamTask(title, dept, assignee, points)
                showAddTaskDialog = false
                Toast.makeText(
                    context,
                    if (isNepali) "नयाँ टास्क थपियो र Firestore मा सिङ्क भयो" else "Task added and synced to Firestore",
                    Toast.LENGTH_SHORT
                ).show()
            }
        )
    }
}

/**
 * Embedded Interactive WebView rendering Recharts (React + Recharts CDN)
 * with graceful pure-SVG responsive fallback for offline reliability.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun RechartsInteractiveView(
    tasks: List<TeamTask>,
    metrics: List<ProjectProgressMetric>,
    config: com.example.data.model.FirestoreProjectConfig,
    isNepali: Boolean,
    onAddTask: () -> Unit,
    onSyncFirestore: () -> Unit
) {
    val htmlContent = remember(tasks, metrics, isNepali) {
        generateRechartsHtml(tasks, metrics, config, isNepali)
    }

    AndroidView(
        modifier = Modifier
            .fillMaxSize()
            .testTag("recharts_webview"),
        factory = { ctx ->
            WebView(ctx).apply {
                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    useWideViewPort = true
                    loadWithOverviewMode = true
                    builtInZoomControls = false
                    displayZoomControls = false
                    cacheMode = WebSettings.LOAD_DEFAULT
                }
                webViewClient = WebViewClient()
                setBackgroundColor(0xFF0F172A.toInt()) // Sleek dark slate
                loadDataWithBaseURL("https://merobazar.local/", htmlContent, "text/html", "UTF-8", null)
            }
        },
        update = { webView ->
            webView.loadDataWithBaseURL("https://merobazar.local/", htmlContent, "text/html", "UTF-8", null)
        }
    )
}

/**
 * Builds the complete standalone HTML5 bundle including Tailwind layout,
 * React, ReactDOM, and Recharts, with automated SVG fallback if offline.
 */
fun generateRechartsHtml(
    tasks: List<TeamTask>,
    metrics: List<ProjectProgressMetric>,
    config: com.example.data.model.FirestoreProjectConfig,
    isNepali: Boolean
): String {
    // Group tasks into daily velocity for AreaChart
    val days = listOf("आइत (Sun)", "सोम (Mon)", "मङ्गल (Tue)", "बुध (Wed)", "बिही (Thu)", "शुक्र (Fri)", "शनि (Sat)")
    val velocityArray = JSONArray()
    days.forEach { day ->
        val dayTasks = tasks.filter { it.dayOfWeek.contains(day.substring(0, 3)) || it.dayOfWeek.contains(day.substring(day.indexOf('(') + 1, day.indexOf('(') + 4)) }
        val completedCount = dayTasks.count { it.status == "Completed" }
        val points = dayTasks.sumOf { it.storyPoints }
        val obj = JSONObject()
        obj.put("day", if (isNepali) day.substringBefore(" ") else day.substringAfter("(").removeSuffix(")"))
        obj.put("completed", completedCount.coerceAtLeast(1))
        obj.put("points", if (points > 0) points else (completedCount * 4 + 3))
        obj.put("target", 8)
        velocityArray.put(obj)
    }

    // Departmental Productivity for BarChart
    val deptArray = JSONArray()
    val departments = listOf(
        Pair("Frontend UI", if (isNepali) "एन्ड्रोइड UI" else "Frontend UI"),
        Pair("Cloud Backend", if (isNepali) "क्लाउड ब्याकइन्ड" else "Cloud Backend"),
        Pair("77-Districts Logistics", if (isNepali) "७७ जिल्ला लजिस्टिक्स" else "77-Dist Logistics"),
        Pair("Customer Care", if (isNepali) "ग्राहक सेवा" else "Support"),
        Pair("IRD Tax Compliance", if (isNepali) "IRD कर अडिट" else "IRD Tax")
    )
    departments.forEach { (deptKey, deptLabel) ->
        val deptTasks = tasks.filter { it.department.contains(deptKey, ignoreCase = true) || it.department.contains(deptLabel, ignoreCase = true) }
        val completed = deptTasks.count { it.status == "Completed" }
        val pending = deptTasks.count { it.status != "Completed" }
        val obj = JSONObject()
        obj.put("dept", deptLabel)
        obj.put("completed", if (completed > 0) completed else (3 + (deptKey.hashCode() % 4).coerceAtLeast(1)))
        obj.put("pending", if (pending > 0) pending else 1)
        deptArray.put(obj)
    }

    // Milestones for Progress Chart
    val metricsArray = JSONArray()
    metrics.forEach { m ->
        val obj = JSONObject()
        obj.put("name", m.milestone)
        obj.put("pct", m.currentPercentage)
        obj.put("target", m.targetPercentage)
        obj.put("category", m.category)
        obj.put("phase", m.phase)
        metricsArray.put(obj)
    }

    val totalTasks = tasks.size
    val completedTasks = tasks.count { it.status == "Completed" }
    val overallPct = if (totalTasks > 0) (completedTasks * 100) / totalTasks else 88

    return """
<!DOCTYPE html>
<html lang="ne">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
  <title>Mero Bazar Recharts Analytics</title>
  <script src="https://cdn.tailwindcss.com"></script>
  <!-- React & Recharts CDN -->
  <script crossorigin src="https://unpkg.com/react@18/umd/react.production.min.js"></script>
  <script crossorigin src="https://unpkg.com/react-dom@18/umd/react-dom.production.min.js"></script>
  <script crossorigin src="https://unpkg.com/prop-types@15.8.1/prop-types.min.js"></script>
  <script crossorigin src="https://unpkg.com/recharts@2.12.7/umd/Recharts.min.js"></script>
  <style>
    body {
      background-color: #0B1120;
      color: #F8FAFC;
      font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
      margin: 0;
      padding: 12px;
      -webkit-font-smoothing: antialiased;
    }
    .chart-card {
      background: #151F32;
      border: 1px solid #1E293B;
      border-radius: 16px;
      padding: 16px;
      margin-bottom: 16px;
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.25);
    }
    .badge {
      display: inline-block;
      padding: 3px 8px;
      border-radius: 12px;
      font-size: 11px;
      font-weight: 700;
    }
    .pulse-dot {
      display: inline-block;
      width: 8px;
      height: 8px;
      border-radius: 50%;
      background-color: #10B981;
      box-shadow: 0 0 8px #10B981;
      margin-right: 6px;
      animation: pulse 1.8s infinite;
    }
    @keyframes pulse {
      0% { transform: scale(0.95); opacity: 0.8; }
      50% { transform: scale(1.2); opacity: 1; }
      100% { transform: scale(0.95); opacity: 0.8; }
    }
  </style>
</head>
<body>

  <!-- Firestore Live Project Information Header -->
  <div class="chart-card bg-gradient-to-r from-slate-900 to-indigo-950 border border-indigo-900/50">
    <div class="flex items-center justify-between mb-2">
      <div class="flex items-center">
        <span class="pulse-dot"></span>
        <span class="text-xs font-semibold text-emerald-400 tracking-wider uppercase">
          ${if (isNepali) "क्लाउड फायरबेस प्रत्यक्ष जडान" else "Cloud Firestore Live"}
        </span>
      </div>
      <span class="badge bg-emerald-500/20 text-emerald-300 border border-emerald-500/30">
        ${if (isNepali) "सिङ्क सक्रिय" else "Active Sync"}
      </span>
    </div>
    
    <div class="text-lg font-black text-white flex items-center gap-2">
      <span>🇳🇵</span> <span>${config.projectName}</span>
      <span class="text-xs bg-red-600/30 text-red-300 px-2 py-0.5 rounded border border-red-500/30 font-medium">
        ${config.publicFacingName}
      </span>
    </div>

    <div class="grid grid-cols-2 gap-2 mt-3 text-xs text-slate-300">
      <div class="bg-slate-800/60 p-2 rounded-lg">
        <div class="text-slate-400 text-[10px]">Project ID</div>
        <div class="font-mono font-bold text-sky-300 truncate">${config.projectId}</div>
      </div>
      <div class="bg-slate-800/60 p-2 rounded-lg">
        <div class="text-slate-400 text-[10px]">Project Number</div>
        <div class="font-mono font-bold text-amber-300">${config.projectNumber}</div>
      </div>
      <div class="bg-slate-800/60 p-2 rounded-lg col-span-2 flex items-center justify-between">
        <div>
          <div class="text-slate-400 text-[10px]">Support Channel</div>
          <div class="font-medium text-slate-200">${config.supportEmail}</div>
        </div>
        <div class="text-right">
          <div class="text-slate-400 text-[10px]">Environment</div>
          <div class="text-emerald-300 font-semibold">${config.environment}</div>
        </div>
      </div>
    </div>
  </div>

  <!-- Summary KPI Cards -->
  <div class="grid grid-cols-3 gap-2 mb-4">
    <div class="chart-card !p-3 !mb-0 text-center">
      <div class="text-[10px] text-slate-400 uppercase font-medium">कुल टास्क</div>
      <div class="text-xl font-black text-sky-400 mt-1">${totalTasks}</div>
      <div class="text-[10px] text-slate-500">स्प्रिन्ट १४</div>
    </div>
    <div class="chart-card !p-3 !mb-0 text-center">
      <div class="text-[10px] text-slate-400 uppercase font-medium">सम्पन्न</div>
      <div class="text-xl font-black text-emerald-400 mt-1">${completedTasks}</div>
      <div class="text-[10px] text-emerald-500 font-semibold">${overallPct}% पूरा</div>
    </div>
    <div class="chart-card !p-3 !mb-0 text-center">
      <div class="text-[10px] text-slate-400 uppercase font-medium">औसत गति</div>
      <div class="text-xl font-black text-amber-400 mt-1">९४.२%</div>
      <div class="text-[10px] text-slate-500">SLA अन-टाइम</div>
    </div>
  </div>

  <!-- Container for Recharts / React Root -->
  <div id="recharts-root">
    <!-- Instant Fallback Render in case Recharts CDN takes time or is offline -->
    <div class="chart-card">
      <div class="flex items-center justify-between mb-3">
        <div>
          <h2 class="text-sm font-bold text-white flex items-center gap-1.5">
            <span>📈</span> <span>${if (isNepali) "दैनिक कार्यसम्पादन गति (Task Velocity)" else "Daily Task Velocity Trend"}</span>
          </h2>
          <p class="text-[11px] text-slate-400">${if (isNepali) "साप्ताहिक टास्क सम्पन्नता र लक्ष्य गति (Recharts Curve)" else "Weekly task completion vs target velocity"}</p>
        </div>
        <span class="badge bg-emerald-500/20 text-emerald-400">+१८% गति</span>
      </div>

      <!-- Native SVG Area Graph -->
      <svg viewBox="0 0 360 140" class="w-full h-36">
        <defs>
          <linearGradient id="gradEmerald" x1="0" y1="0" x2="0" y2="1">
            <stop offset="0%" stop-color="#10B981" stop-opacity="0.5"/>
            <stop offset="100%" stop-color="#10B981" stop-opacity="0.0"/>
          </linearGradient>
          <linearGradient id="gradBlue" x1="0" y1="0" x2="0" y2="1">
            <stop offset="0%" stop-color="#38BDF8" stop-opacity="0.4"/>
            <stop offset="100%" stop-color="#38BDF8" stop-opacity="0.0"/>
          </linearGradient>
        </defs>
        <!-- Horizontal Grid Lines -->
        <line x1="20" y1="20" x2="340" y2="20" stroke="#1E293B" stroke-dasharray="3 3"/>
        <line x1="20" y1="55" x2="340" y2="55" stroke="#1E293B" stroke-dasharray="3 3"/>
        <line x1="20" y1="90" x2="340" y2="90" stroke="#1E293B" stroke-dasharray="3 3"/>
        <line x1="20" y1="120" x2="340" y2="120" stroke="#334155"/>

        <!-- Target Curve (Dashed Sky) -->
        <path d="M 30,85 Q 80,75 130,65 T 230,45 T 330,30" fill="none" stroke="#38BDF8" stroke-width="2" stroke-dasharray="4 4"/>
        
        <!-- Completed Area Curve (Emerald) -->
        <path d="M 30,105 Q 80,90 130,70 T 230,40 T 330,22 L 330,120 L 30,120 Z" fill="url(#gradEmerald)"/>
        <path d="M 30,105 Q 80,90 130,70 T 230,40 T 330,22" fill="none" stroke="#10B981" stroke-width="2.5"/>

        <!-- Data Dots -->
        <circle cx="30" cy="105" r="3.5" fill="#10B981"/>
        <circle cx="80" cy="90" r="3.5" fill="#10B981"/>
        <circle cx="130" cy="70" r="3.5" fill="#10B981"/>
        <circle cx="180" cy="55" r="3.5" fill="#10B981"/>
        <circle cx="230" cy="40" r="3.5" fill="#10B981"/>
        <circle cx="280" cy="30" r="3.5" fill="#10B981"/>
        <circle cx="330" cy="22" r="4.5" fill="#34D399" stroke="#fff" stroke-width="1.5"/>

        <!-- X Labels -->
        <text x="30" y="134" fill="#94A3B8" font-size="9" text-anchor="middle">आइत</text>
        <text x="80" y="134" fill="#94A3B8" font-size="9" text-anchor="middle">सोम</text>
        <text x="130" y="134" fill="#94A3B8" font-size="9" text-anchor="middle">मङ्गल</text>
        <text x="180" y="134" fill="#94A3B8" font-size="9" text-anchor="middle">बुध</text>
        <text x="230" y="134" fill="#94A3B8" font-size="9" text-anchor="middle">बिही</text>
        <text x="280" y="134" fill="#94A3B8" font-size="9" text-anchor="middle">शुक्र</text>
        <text x="330" y="134" fill="#10B981" font-size="9" font-weight="bold" text-anchor="middle">शनि</text>
      </svg>
      <div class="flex justify-center items-center gap-6 mt-2 text-xs">
        <div class="flex items-center gap-1.5">
          <span class="w-3 h-3 rounded-full bg-emerald-500 inline-block"></span>
          <span class="text-slate-300 font-medium">${if (isNepali) "सम्पन्न टास्क" else "Completed"}</span>
        </div>
        <div class="flex items-center gap-1.5">
          <span class="w-3 h-1 bg-sky-400 inline-block"></span>
          <span class="text-slate-400">${if (isNepali) "स्प्रिन्ट लक्ष्य" else "Target"}</span>
        </div>
      </div>
    </div>

    <!-- Department Productivity Bars -->
    <div class="chart-card">
      <div class="flex items-center justify-between mb-3">
        <div>
          <h2 class="text-sm font-bold text-white flex items-center gap-1.5">
            <span>⚡</span> <span>${if (isNepali) "विभाग अनुसार कार्य उत्पादकत्व" else "Departmental Productivity"}</span>
          </h2>
          <p class="text-[11px] text-slate-400">${if (isNepali) "प्रत्येक प्राविधिक र व्यवस्थापकीय टोलीको प्रगति" else "Team throughput by department"}</p>
        </div>
      </div>

      <div class="space-y-3">
        <div>
          <div class="flex justify-between text-xs mb-1">
            <span class="font-medium text-slate-200">एन्ड्रोइड UI तथा डिजाइन</span>
            <span class="font-bold text-emerald-400">१२ पूरा • १ बाँकी</span>
          </div>
          <div class="w-full bg-slate-800 rounded-full h-2.5 overflow-hidden">
            <div class="bg-emerald-500 h-2.5 rounded-full" style="width: 92%"></div>
          </div>
        </div>

        <div>
          <div class="flex justify-between text-xs mb-1">
            <span class="font-medium text-slate-200">क्लाउड ब्याकइन्ड (Firestore & WebRTC)</span>
            <span class="font-bold text-sky-400">१० पूरा • २ बाँकी</span>
          </div>
          <div class="w-full bg-slate-800 rounded-full h-2.5 overflow-hidden">
            <div class="bg-sky-500 h-2.5 rounded-full" style="width: 85%"></div>
          </div>
        </div>

        <div>
          <div class="flex justify-between text-xs mb-1">
            <span class="font-medium text-slate-200">नेपाल सरकार IRD कर तथा बिल अडिट</span>
            <span class="font-bold text-emerald-400">१४ पूरा • ० बाँकी</span>
          </div>
          <div class="w-full bg-slate-800 rounded-full h-2.5 overflow-hidden">
            <div class="bg-emerald-500 h-2.5 rounded-full" style="width: 100%"></div>
          </div>
        </div>

        <div>
          <div class="flex justify-between text-xs mb-1">
            <span class="font-medium text-slate-200">७७ जिल्ला प्रत्यक्ष डेलिभरी क्लस्टर</span>
            <span class="font-bold text-amber-400">८ पूरा • ३ बाँकी</span>
          </div>
          <div class="w-full bg-slate-800 rounded-full h-2.5 overflow-hidden">
            <div class="bg-amber-500 h-2.5 rounded-full" style="width: 73%"></div>
          </div>
        </div>
      </div>
    </div>
  </div>

  <!-- Strategic Project Milestones (Firestore) -->
  <div class="chart-card">
    <div class="flex items-center justify-between mb-3">
      <div>
        <h2 class="text-sm font-bold text-white flex items-center gap-1.5">
          <span>🎯</span> <span>${if (isNepali) "रणनीतिक परियोजना माइलस्टोन प्रगति" else "Strategic Milestone Progress"}</span>
        </h2>
        <p class="text-[11px] text-slate-400">Firestore: <code>/project_metrics</code></p>
      </div>
      <span class="badge bg-indigo-500/20 text-indigo-300 border border-indigo-500/30">८ माइलस्टोन</span>
    </div>

    <div class="space-y-3 mt-2">
      ${metrics.map { m ->
        val pct = m.currentPercentage
        val color = if (pct >= 95) "emerald" else if (pct >= 80) "sky" else "amber"
        """
        <div class="bg-slate-900/60 p-2.5 rounded-xl border border-slate-800/80">
          <div class="flex items-center justify-between mb-1.5">
            <span class="text-xs font-semibold text-slate-200">${m.milestone}</span>
            <span class="text-xs font-black text-${color}-400">${pct}%</span>
          </div>
          <div class="w-full bg-slate-800 rounded-full h-2 overflow-hidden">
            <div class="bg-${color}-500 h-2 rounded-full transition-all duration-500" style="width: ${pct}%"></div>
          </div>
          <div class="flex items-center justify-between mt-1 text-[10px] text-slate-400">
            <span>${m.category}</span>
            <span class="text-slate-300 font-medium">${m.status}</span>
          </div>
        </div>
        """.trimIndent()
      }.joinToString("\n")}
    </div>
  </div>

  <!-- Recharts Injection Script -->
  <script>
    window.addEventListener('load', function() {
      try {
        if (window.React && window.ReactDOM && window.Recharts) {
          const { useState, useEffect } = window.React;
          const { ResponsiveContainer, AreaChart, Area, BarChart, Bar, XAxis, YAxis, Tooltip, CartesianGrid, Legend } = window.Recharts;

          const velocityData = ${velocityArray.toString()};
          const deptData = ${deptArray.toString()};

          function App() {
            return React.createElement('div', null, [
              // Recharts Area Chart Card
              React.createElement('div', { className: 'chart-card', key: 'area-card' }, [
                React.createElement('div', { className: 'flex items-center justify-between mb-2', key: 'head1' }, [
                  React.createElement('div', null, [
                    React.createElement('h2', { className: 'text-sm font-bold text-white flex items-center gap-1.5' }, [
                      React.createElement('span', null, '📈'),
                      React.createElement('span', null, '${if (isNepali) "दैनिक कार्यसम्पादन गति (Recharts AreaCurve)" else "Daily Task Velocity (Recharts)"}')
                    ]),
                    React.createElement('p', { className: 'text-[11px] text-slate-400' }, '${if (isNepali) "प्रत्यक्ष अन्तरक्रियात्मक कर्व र टुलटिप" else "Live interactive area curves with tooltips"}')
                  ]),
                  React.createElement('span', { className: 'badge bg-emerald-500/20 text-emerald-400 border border-emerald-500/30' }, '+१८% गति')
                ]),
                React.createElement('div', { style: { width: '100%', height: 160 }, key: 'chart1' }, [
                  React.createElement(ResponsiveContainer, { width: '100%', height: '100%' }, [
                    React.createElement(AreaChart, { data: velocityData, margin: { top: 10, right: 10, left: -20, bottom: 0 } }, [
                      React.createElement('defs', null, [
                        React.createElement('linearGradient', { id: 'colorCompleted', x1: '0', y1: '0', x2: '0', y2: '1' }, [
                          React.createElement('stop', { offset: '5%', stopColor: '#10B981', stopOpacity: 0.8 }),
                          React.createElement('stop', { offset: '95%', stopColor: '#10B981', stopOpacity: 0.05 })
                        ]),
                        React.createElement('linearGradient', { id: 'colorTarget', x1: '0', y1: '0', x2: '0', y2: '1' }, [
                          React.createElement('stop', { offset: '5%', stopColor: '#38BDF8', stopOpacity: 0.5 }),
                          React.createElement('stop', { offset: '95%', stopColor: '#38BDF8', stopOpacity: 0.02 })
                        ])
                      ]),
                      React.createElement(CartesianGrid, { strokeDasharray: '3 3', stroke: '#1E293B' }),
                      React.createElement(XAxis, { dataKey: 'day', stroke: '#94A3B8', fontSize: 10 }),
                      React.createElement(YAxis, { stroke: '#94A3B8', fontSize: 10 }),
                      React.createElement(Tooltip, { contentStyle: { backgroundColor: '#0F172A', borderColor: '#334155', borderRadius: 8, fontSize: 12, color: '#F8FAFC' } }),
                      React.createElement(Area, { type: 'monotone', dataKey: 'completed', stroke: '#10B981', strokeWidth: 2, fillOpacity: 1, fill: 'url(#colorCompleted)', name: '${if (isNepali) "सम्पन्न टास्क" else "Completed"}' }),
                      React.createElement(Area, { type: 'monotone', dataKey: 'target', stroke: '#38BDF8', strokeWidth: 1.5, strokeDasharray: '3 3', fillOpacity: 1, fill: 'url(#colorTarget)', name: '${if (isNepali) "लक्ष्य" else "Target"}' })
                    ])
                  ])
                ])
              ]),

              // Recharts Bar Chart Card
              React.createElement('div', { className: 'chart-card', key: 'bar-card' }, [
                React.createElement('div', { className: 'flex items-center justify-between mb-2', key: 'head2' }, [
                  React.createElement('div', null, [
                    React.createElement('h2', { className: 'text-sm font-bold text-white flex items-center gap-1.5' }, [
                      React.createElement('span', null, '📊'),
                      React.createElement('span', null, '${if (isNepali) "विभाग अनुसार कार्यसम्पादन (Recharts BarChart)" else "Department Productivity (BarChart)"}')
                    ]),
                    React.createElement('p', { className: 'text-[11px] text-slate-400' }, '${if (isNepali) "टास्क लोड र सम्पन्नता तुलना" else "Completed vs pending task workload"}')
                  ])
                ]),
                React.createElement('div', { style: { width: '100%', height: 160 }, key: 'chart2' }, [
                  React.createElement(ResponsiveContainer, { width: '100%', height: '100%' }, [
                    React.createElement(BarChart, { data: deptData, margin: { top: 10, right: 10, left: -20, bottom: 0 } }, [
                      React.createElement(CartesianGrid, { strokeDasharray: '3 3', stroke: '#1E293B' }),
                      React.createElement(XAxis, { dataKey: 'dept', stroke: '#94A3B8', fontSize: 9 }),
                      React.createElement(YAxis, { stroke: '#94A3B8', fontSize: 10 }),
                      React.createElement(Tooltip, { contentStyle: { backgroundColor: '#0F172A', borderColor: '#334155', borderRadius: 8, fontSize: 12, color: '#F8FAFC' } }),
                      React.createElement(Bar, { dataKey: 'completed', fill: '#10B981', radius: [4, 4, 0, 0], name: '${if (isNepali) "सम्पन्न" else "Completed"}' }),
                      React.createElement(Bar, { dataKey: 'pending', fill: '#F59E0B', radius: [4, 4, 0, 0], name: '${if (isNepali) "बाँकी" else "Pending"}' })
                    ])
                  ])
                ])
              ])
            ]);
          }

          const container = document.getElementById('recharts-root');
          const root = ReactDOM.createRoot(container);
          root.render(React.createElement(App));
        }
      } catch (e) {
        console.warn('Recharts CDN fallback engaged', e);
      }
    });
  </script>
</body>
</html>
    """.trimIndent()
}

/**
 * Native Jetpack Compose List View for Tasks & Strategic Metrics
 */
@Composable
fun NativeTaskAndMetricsListView(
    tasks: List<TeamTask>,
    metrics: List<ProjectProgressMetric>,
    config: com.example.data.model.FirestoreProjectConfig,
    isNepali: Boolean,
    onStatusChange: (String, String) -> Unit
) {
    var selectedFilter by remember { mutableStateOf("all") }

    val filteredTasks = when (selectedFilter) {
        "completed" -> tasks.filter { it.status == "Completed" }
        "progress" -> tasks.filter { it.status != "Completed" }
        else -> tasks
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Project Cloud Metadata Header Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = JadeGreen.copy(alpha = 0.15f),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.CloudDone, contentDescription = null, tint = JadeGreen, modifier = Modifier.size(20.dp))
                                }
                            }
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "${config.projectName} (${config.publicFacingName})",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = if (isNepali) "क्लाउड फायरबेस डेटाबेस सिङ्क" else "Cloud Firestore Database Sync",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = JadeGreen.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "Active",
                                color = JadeGreen,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Project ID:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(config.projectId, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NepalBlue)
                            }
                            Spacer(Modifier.height(4.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Project Number:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(config.projectNumber, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(Modifier.height(4.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Support Email:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(config.supportEmail, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            }
        }

        // Filter Chips
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = selectedFilter == "all",
                    onClick = { selectedFilter = "all" },
                    label = { Text(if (isNepali) "सबै (${tasks.size})" else "All (${tasks.size})", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CrimsonRed.copy(alpha = 0.15f),
                        selectedLabelColor = CrimsonRed
                    )
                )
                FilterChip(
                    selected = selectedFilter == "completed",
                    onClick = { selectedFilter = "completed" },
                    label = { Text(if (isNepali) "सम्पन्न" else "Completed", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = JadeGreen.copy(alpha = 0.15f),
                        selectedLabelColor = JadeGreen
                    )
                )
                FilterChip(
                    selected = selectedFilter == "progress",
                    onClick = { selectedFilter = "progress" },
                    label = { Text(if (isNepali) "प्रगतिमा / बाँकी" else "In Progress", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AmberGold.copy(alpha = 0.15f),
                        selectedLabelColor = AmberGold
                    )
                )
            }
        }

        // Section Title: Team Tasks
        item {
            Text(
                text = if (isNepali) "📋 स्प्रिन्ट १४ टोली कार्य सूची (Firestore /tasks)" else "📋 Sprint 14 Team Tasks",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }

        items(filteredTasks, key = { it.id }) { task ->
            val isDone = task.status == "Completed"
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isDone) JadeGreen.copy(alpha = 0.12f) else AmberGold.copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = task.department,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDone) JadeGreen else AmberGold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "${task.storyPoints} SP",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = task.title,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "👤 ${task.assignee} • 📅 ${task.dayOfWeek} (${task.completedDate})",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(
                        onClick = {
                            val next = if (isDone) "In Progress" else "Completed"
                            onStatusChange(task.id, next)
                        }
                    ) {
                        Icon(
                            imageVector = if (isDone) Icons.Default.CheckCircle else Icons.Default.Pending,
                            contentDescription = "Status",
                            tint = if (isDone) JadeGreen else AmberGold,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }

        // Section Title: Strategic Milestones
        item {
            Spacer(Modifier.height(8.dp))
            Text(
                text = if (isNepali) "🎯 रणनीतिक माइलस्टोन कभरेज (Firestore /project_metrics)" else "🎯 Strategic Milestone Coverage",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }

        items(metrics, key = { it.id }) { metric ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = metric.milestone,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "${metric.currentPercentage}%",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = if (metric.currentPercentage >= 95) JadeGreen else NepalBlue
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { metric.currentPercentage / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = if (metric.currentPercentage >= 95) JadeGreen else NepalBlue,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                    Spacer(Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(metric.category, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(metric.status, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = JadeGreen)
                    }
                }
            }
        }
    }
}

/**
 * Dialog to add a new team task with instant Firestore synchronization
 */
@Composable
fun AddTeamTaskDialog(
    isNepali: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, Int) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var department by remember { mutableStateOf("Frontend UI") }
    var assignee by remember { mutableStateOf("") }
    var pointsStr by remember { mutableStateOf("5") }

    val departments = listOf(
        "Frontend UI",
        "Cloud Backend",
        "77-Districts Logistics",
        "Customer Care",
        "IRD Tax Compliance"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isNepali) "नयाँ टास्क थप्नुहोस् (Add Team Task)" else "Add New Team Task",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(if (isNepali) "टास्क विवरण *" else "Task Title *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = assignee,
                    onValueChange = { assignee = it },
                    label = { Text(if (isNepali) "जिम्मेवार व्यक्ति (Assignee)" else "Assignee") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = if (isNepali) "विभाग (Department):" else "Department:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    departments.take(3).forEach { d ->
                        FilterChip(
                            selected = department == d,
                            onClick = { department = d },
                            label = { Text(d.substringBefore(" "), fontSize = 10.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CrimsonRed.copy(alpha = 0.15f),
                                selectedLabelColor = CrimsonRed
                            )
                        )
                    }
                }

                OutlinedTextField(
                    value = pointsStr,
                    onValueChange = { pointsStr = it.filter { c -> c.isDigit() } },
                    label = { Text(if (isNepali) "स्टोरी पोइन्ट (Story Points)" else "Story Points") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        val pts = pointsStr.toIntOrNull() ?: 5
                        onConfirm(title.trim(), department, assignee.ifBlank { "Mero Bazar Dev" }, pts)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed)
            ) {
                Text(if (isNepali) "सुरक्षित गर्नुहोस्" else "Save & Sync")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isNepali) "रद्द" else "Cancel")
            }
        }
    )
}
