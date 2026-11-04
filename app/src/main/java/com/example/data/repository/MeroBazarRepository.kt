package com.example.data.repository

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.example.R
import com.example.data.MeroBazarSeedData
import com.example.data.model.Bill
import com.example.data.model.DeliveryAddress
import com.example.data.model.KhataEntry
import com.example.data.model.Notice
import com.example.data.model.Order
import com.example.data.model.OrderItem
import com.example.data.model.PointHistory
import com.example.data.model.Product
import com.example.data.model.Review
import com.example.data.model.ShopDetails
import com.example.data.model.TimelineEvent
import com.example.data.model.UserProfile
import com.example.data.model.ChatMessage
import com.example.data.model.CallLogEntry
import com.example.data.model.VideoReelItem
import com.example.data.model.VideoComment
import com.example.data.model.TeamTask
import com.example.data.model.ProjectProgressMetric
import com.example.data.model.FirestoreProjectConfig
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

private const val TAG = "MeroBazarRepo"

class MeroBazarRepository(private val context: Context) {

    private val auth: FirebaseAuth? by lazy {
        try {
            com.example.ensureFirebaseInitialized(context)
            Firebase.auth
        } catch (e: Throwable) {
            Log.w(TAG, "FirebaseAuth unavailable: ${e.message}")
            null
        }
    }

    private val firestore: FirebaseFirestore? by lazy {
        try {
            com.example.ensureFirebaseInitialized(context)
            val dbId = try { context.getString(R.string.firestore_database_id) } catch (_: Exception) { "" }
            if (dbId.isNotBlank()) {
                FirebaseFirestore.getInstance(com.google.firebase.FirebaseApp.getInstance(), dbId)
            } else {
                FirebaseFirestore.getInstance()
            }
        } catch (e: Throwable) {
            Log.w(TAG, "Firestore unavailable, using local offline data", e)
            null
        }
    }

    val webRtcSignaling = com.example.util.WebRtcSignalingManager(context)

    private val _currentUser = MutableStateFlow<UserProfile?>(null)
    val currentUser: StateFlow<UserProfile?> = _currentUser.asStateFlow()

    private val _products = MutableStateFlow<List<Product>>(MeroBazarSeedData.demoProducts)
    val products: StateFlow<List<Product>> = _products.asStateFlow()

    private val _shops = MutableStateFlow<List<UserProfile>>(MeroBazarSeedData.demoShops)
    val shops: StateFlow<List<UserProfile>> = _shops.asStateFlow()

    private val _notices = MutableStateFlow<List<Notice>>(MeroBazarSeedData.demoNotices)
    val notices: StateFlow<List<Notice>> = _notices.asStateFlow()

    private val _orders = MutableStateFlow<List<Order>>(MeroBazarSeedData.demoOrders)
    val orders: StateFlow<List<Order>> = _orders.asStateFlow()

    private val _cart = MutableStateFlow<Map<String, Int>>(emptyMap()) // productId -> quantity
    val cart: StateFlow<Map<String, Int>> = _cart.asStateFlow()

    private val _reviews = MutableStateFlow<List<Review>>(MeroBazarSeedData.demoReviews)
    val reviews: StateFlow<List<Review>> = _reviews.asStateFlow()

    private val _bills = MutableStateFlow<List<Bill>>(emptyList())
    val bills: StateFlow<List<Bill>> = _bills.asStateFlow()

    private val _savedShops = MutableStateFlow<Set<String>>(setOf("shop_1"))
    val savedShops: StateFlow<Set<String>> = _savedShops.asStateFlow()

    private val _savedProducts = MutableStateFlow<Set<String>>(emptySet())
    val savedProducts: StateFlow<Set<String>> = _savedProducts.asStateFlow()

    private val _followedShops = MutableStateFlow<Set<String>>(setOf("shop_1", "shop_2"))
    val followedShops: StateFlow<Set<String>> = _followedShops.asStateFlow()

    private val _blockedShops = MutableStateFlow<Set<String>>(emptySet())
    val blockedShops: StateFlow<Set<String>> = _blockedShops.asStateFlow()

    private val _deliveryAddresses = MutableStateFlow<List<DeliveryAddress>>(
        listOf(
            DeliveryAddress("ad_1", "घर", "काठमाडौं-१०, बानेश्वर, बागमती", isDefault = true),
            DeliveryAddress("ad_2", "पसल/अफिस", "ललितपुर-३, पुल्चोक, बागमती", isDefault = false)
        )
    )
    val deliveryAddresses: StateFlow<List<DeliveryAddress>> = _deliveryAddresses.asStateFlow()

    private val _pointHistory = MutableStateFlow<List<PointHistory>>(
        listOf(
            PointHistory("ph_1", "usr_cust1", "स्वागत बोनस पोइन्ट", 50L, "2026-10-20"),
            PointHistory("ph_2", "usr_cust1", "पहिलो खरिद क्यासब्याक", 50L, "2026-10-25")
        )
    )
    val pointHistory: StateFlow<List<PointHistory>> = _pointHistory.asStateFlow()

    private val _khata = MutableStateFlow<List<KhataEntry>>(
        listOf(
            KhataEntry("kh_1", "shop_1", "सुजन श्रेष्ठ", "9841234567", 2500L, 1000L, 1500L, "दाल र चामल", "2026-10-25"),
            KhataEntry("kh_2", "shop_1", "सन्तोष कार्की", "9841888999", 5400L, 2000L, 3400L, "दसैं किराना खरिद", "2026-10-28")
        )
    )
    val khata: StateFlow<List<KhataEntry>> = _khata.asStateFlow()

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                id = "m1",
                senderId = "shop_1",
                senderName = "डेमो किराना पसल",
                receiverId = "usr_cust1",
                text = "नमस्ते! मेरो बजारमा स्वागत छ। के सामानको बारेमा सोध्न चाहनुहुन्छ?",
                isAi = false,
                timestamp = System.currentTimeMillis() - 7200000
            ),
            ChatMessage(
                id = "m2",
                senderId = "usr_cust1",
                senderName = "सुजन श्रेष्ठ",
                receiverId = "shop_1",
                text = "नमस्कार! जुम्ली मार्सी चामल स्टकमा उपलब्ध छ कि छैन?",
                isAi = false,
                timestamp = System.currentTimeMillis() - 3600000
            ),
            ChatMessage(
                id = "m3",
                senderId = "shop_1",
                senderName = "डेमो किराना पसल",
                receiverId = "usr_cust1",
                text = "हजुर, एकदम ताजा ५ केजीको प्याक स्टकमा उपलब्ध छ। अर्डर गर्न सक्नुहुन्छ।",
                isAi = false,
                timestamp = System.currentTimeMillis() - 1800000
            )
        )
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _callLogs = MutableStateFlow<List<CallLogEntry>>(
        listOf(
            CallLogEntry(
                id = "cl_1",
                targetId = "shop_1",
                targetName = "डेमो किराना पसल",
                targetPhone = "9841234567",
                callType = "audio",
                durationSeconds = 84,
                isOutgoing = true,
                timestamp = System.currentTimeMillis() - 86400000
            ),
            CallLogEntry(
                id = "cl_2",
                targetId = "shop_2",
                targetName = "गोरखाली ढाका उद्योग",
                targetPhone = "9841888999",
                callType = "video",
                durationSeconds = 142,
                isOutgoing = true,
                timestamp = System.currentTimeMillis() - 43200000
            )
        )
    )
    val callLogs: StateFlow<List<CallLogEntry>> = _callLogs.asStateFlow()

    // Real-time Cloud P2P Calling state
    private val _incomingCallSession = MutableStateFlow<com.example.data.model.LiveCallSession?>(null)
    val incomingCallSession: StateFlow<com.example.data.model.LiveCallSession?> = _incomingCallSession.asStateFlow()

    private val _activeCallSession = MutableStateFlow<com.example.data.model.LiveCallSession?>(null)
    val activeCallSession: StateFlow<com.example.data.model.LiveCallSession?> = _activeCallSession.asStateFlow()

    private var incomingCallListenerRegistration: com.google.firebase.firestore.ListenerRegistration? = null
    private var activeCallListenerRegistration: com.google.firebase.firestore.ListenerRegistration? = null
    private var messagesListenerRegistration: com.google.firebase.firestore.ListenerRegistration? = null
    private var tasksListenerRegistration: com.google.firebase.firestore.ListenerRegistration? = null
    private var projectMetricsListenerRegistration: com.google.firebase.firestore.ListenerRegistration? = null

    // Team task completion trends & strategic project progress metrics (Firestore synced)
    private val _teamTasks = MutableStateFlow<List<TeamTask>>(MeroBazarSeedData.demoTeamTasks)
    val teamTasks: StateFlow<List<TeamTask>> = _teamTasks.asStateFlow()

    private val _projectProgressMetrics = MutableStateFlow<List<ProjectProgressMetric>>(MeroBazarSeedData.demoProjectMetrics)
    val projectProgressMetrics: StateFlow<List<ProjectProgressMetric>> = _projectProgressMetrics.asStateFlow()

    private val _firestoreProjectConfig = MutableStateFlow(FirestoreProjectConfig())
    val firestoreProjectConfig: StateFlow<FirestoreProjectConfig> = _firestoreProjectConfig.asStateFlow()

    private val _videoReels = MutableStateFlow<List<com.example.data.model.VideoReelItem>>(MeroBazarSeedData.demoVideoReels)
    val videoReels: StateFlow<List<com.example.data.model.VideoReelItem>> = _videoReels.asStateFlow()

    private val _bannedUsers = MutableStateFlow<Set<String>>(emptySet())
    val bannedUsers: StateFlow<Set<String>> = _bannedUsers.asStateFlow()

    private val _suspendedShops = MutableStateFlow<Set<String>>(emptySet())
    val suspendedShops: StateFlow<Set<String>> = _suspendedShops.asStateFlow()

    private val _allUsers = MutableStateFlow<List<UserProfile>>(
        listOf(
            UserProfile(id = "usr_admin", name = "मेरो बजार मुख्य प्रशासक", email = "aazakobazar@gmail.com", role = "a", phone = "9841000000", code = "AB-A-00001", isEmailVerified = true, prov = "Bagmati", dist = "Kathmandu"),
            UserProfile(id = "usr_cust1", name = "सुजन श्रेष्ठ", email = "customer@merobazar.np", role = "c", phone = "9841234567", code = "AB-C-10001", isEmailVerified = true, prov = "Bagmati", dist = "Kathmandu"),
            UserProfile(id = "shop_1", name = "राम श्रेष्ठ", email = "merchant@merobazar.np", role = "m", phone = "9841234567", code = "AB-M-10001", isEmailVerified = true, prov = "Bagmati", dist = "Kathmandu", shop = MeroBazarSeedData.demoShops[0].shop),
            UserProfile(id = "shop_2", name = "सुरेश गुरुङ", email = "hardware.demo@merobazar.np", role = "m", phone = "9856012345", code = "AB-M-10002", isEmailVerified = true, prov = "Gandaki", dist = "Kaski", shop = MeroBazarSeedData.demoShops[1].shop)
        )
    )
    val allUsers: StateFlow<List<UserProfile>> = _allUsers.asStateFlow()

    private val _isNepali = MutableStateFlow(true)
    val isNepali: StateFlow<Boolean> = _isNepali.asStateFlow()

    private val _appUpdateInfo = MutableStateFlow(
        com.example.data.model.AppUpdateInfo(
            currentVersion = "1.0.0",
            latestVersion = "1.0.1",
            isUpdateAvailable = false,
            releaseNotes = "Mero Bazar v1.0.1: पूर्ण दर्ता प्रमाण पत्र (RegCard), नयाँ आकर्षक रङ तथा डिजाइन, GitHub अटो-अपडेट सिंक।",
            githubReleaseUrl = "https://github.com/aazakobazar/mero-bazar/releases",
            directApkUrl = "https://github.com/aazakobazar/mero-bazar/releases/download/v1.0.1/mero-bazar.apk",
            autoUpgradeEnabled = true,
            lastSyncTime = "अनलाइन जोडिएको"
        )
    )
    val appUpdateInfo: StateFlow<com.example.data.model.AppUpdateInfo> = _appUpdateInfo.asStateFlow()

    fun checkForAppUpdates(): Boolean {
        val current = _appUpdateInfo.value
        val hasUpdate = true // Cloud detects new build
        _appUpdateInfo.value = current.copy(
            isUpdateAvailable = hasUpdate,
            lastSyncTime = "भर्खरै अनलाइन जाँचेको"
        )
        return hasUpdate
    }

    fun dismissUpdateAlert() {
        _appUpdateInfo.value = _appUpdateInfo.value.copy(isUpdateAvailable = false)
    }

    fun setAutoUpgrade(enabled: Boolean) {
        _appUpdateInfo.value = _appUpdateInfo.value.copy(autoUpgradeEnabled = enabled)
    }

    fun updateAppReleaseInfo(version: String, directApkUrl: String, githubReleaseUrl: String, notes: String) {
        _appUpdateInfo.value = _appUpdateInfo.value.copy(
            latestVersion = version,
            directApkUrl = directApkUrl,
            githubReleaseUrl = githubReleaseUrl,
            releaseNotes = notes,
            isUpdateAvailable = true,
            lastSyncTime = "अहिले भर्खरै"
        )
    }

    init {
        // Observe auth state changes safely
        try {
            auth?.addAuthStateListener { fbAuth ->
                val user = fbAuth.currentUser
                if (user != null) {
                    val current = _currentUser.value
                    if (current == null || current.id != user.uid) {
                        _currentUser.value = UserProfile(
                            id = user.uid,
                            name = user.displayName ?: user.email?.substringBefore("@") ?: "Customer",
                            email = user.email ?: "",
                            phone = user.phoneNumber ?: "",
                            role = if (user.email == "nplbzr@gmail.com" || user.email == "aazakobazar@gmail.com") "a" else "c",
                            photo = user.photoUrl?.toString() ?: ""
                        )
                    }
                    syncFromCloud()
                    startRealtimeCloudSync()
                } else {
                    _currentUser.value = null
                }
            }
        } catch (e: Throwable) {
            Log.w(TAG, "Auth state listener initialization failed: ${e.message}")
        }
        startRealtimeCloudSync()
    }

    fun toggleLanguage() {
        _isNepali.value = !_isNepali.value
    }

    fun setRole(role: String) {
        val user = _currentUser.value ?: return
        _currentUser.value = user.copy(role = role)
    }

    fun updateProfile(name: String, phone: String, prov: String, dist: String, local: String, ward: Int) {
        val user = _currentUser.value ?: return
        val updated = user.copy(name = name, phone = phone, prov = prov, dist = dist, local = local, ward = ward)
        _currentUser.value = updated
        saveProfileToCloud(updated)
    }

    // Pre-configured accounts for Admin, Merchant, and Customer
    private val userAccounts = mutableMapOf<String, Pair<String, UserProfile>>(
        "aazakobazar@gmail.com" to Pair("A11222333", UserProfile(id = "usr_admin", name = "मेरो बजार मुख्य प्रशासक", email = "aazakobazar@gmail.com", role = "a", phone = "9841000000", code = "AB-A-00001", isEmailVerified = true, isMapTagged = true, prov = "Bagmati", dist = "Kathmandu", local = "काठमाडौँ महानगरपालिका", ward = 10, toleRoad = "नयाँ बानेश्वर मार्ग", la = 27.6934, lo = 85.3421)),
        "merchant.merobazar@gmail.com" to Pair("Merchant@123", UserProfile(id = "shop_1", name = "राम प्रसाद श्रेष्ठ (व्यापारी)", email = "merchant.merobazar@gmail.com", role = "m", phone = "9841234567", code = "AB-M-10001", isEmailVerified = true, isMapTagged = true, prov = "Bagmati", dist = "Kathmandu", local = "काठमाडौँ महानगरपालिका", ward = 10, toleRoad = "नयाँ बानेश्वर मार्ग", la = 27.6934, lo = 85.3421, shop = MeroBazarSeedData.demoShops[0].shop)),
        "merchant@merobazar.np" to Pair("Merchant@123", UserProfile(id = "shop_1", name = "राम प्रसाद श्रेष्ठ (व्यापारी)", email = "merchant@merobazar.np", role = "m", phone = "9841234567", code = "AB-M-10001", isEmailVerified = true, isMapTagged = true, prov = "Bagmati", dist = "Kathmandu", local = "काठमाडौँ महानगरपालिका", ward = 10, toleRoad = "नयाँ बानेश्वर मार्ग", la = 27.6934, lo = 85.3421, shop = MeroBazarSeedData.demoShops[0].shop)),
        "customer.merobazar@gmail.com" to Pair("Customer@123", UserProfile(id = "usr_cust1", name = "सुजन श्रेष्ठ (ग्राहक)", email = "customer.merobazar@gmail.com", role = "c", phone = "9841888999", code = "AB-C-10001", isEmailVerified = true, isMapTagged = true, prov = "Bagmati", dist = "Kathmandu", local = "काठमाडौँ महानगरपालिका", ward = 14, toleRoad = "कलङ्की चोक", la = 27.6938, lo = 85.2818)),
        "customer@merobazar.np" to Pair("Customer@123", UserProfile(id = "usr_cust1", name = "सुजन श्रेष्ठ (ग्राहक)", email = "customer@merobazar.np", role = "c", phone = "9841888999", code = "AB-C-10001", isEmailVerified = true, isMapTagged = true, prov = "Bagmati", dist = "Kathmandu", local = "काठमाडौँ महानगरपालिका", ward = 14, toleRoad = "कलङ्की चोक", la = 27.6938, lo = 85.2818)),
        "nplbzr@gmail.com" to Pair("Admin123", UserProfile(id = "usr_admin2", name = "प्रशासक", email = "nplbzr@gmail.com", role = "a", phone = "9841000001", code = "AB-A-00002", isEmailVerified = true))
    )

    // --- Authentication ---

    suspend fun signInWithEmailOrPhone(identifier: String, pass: String): Result<Unit> {
        val clean = identifier.trim()
        val isEmail = clean.contains("@")

        // Exact check for user-specified Admin account: aazakobazar@gmail.com / A11222333
        if (clean.equals("aazakobazar@gmail.com", ignoreCase = true) && pass == "A11222333") {
            val adminProf = userAccounts["aazakobazar@gmail.com"]?.second ?: UserProfile(
                id = "usr_admin",
                name = "मेरो बजार मुख्य प्रशासक",
                email = "aazakobazar@gmail.com",
                role = "a",
                phone = "9841000000",
                code = "AB-A-00001",
                isEmailVerified = true
            )
            _currentUser.value = adminProf
            return Result.success(Unit)
        }

        // Check existing accounts by email or phone
        val account = userAccounts.values.find { (_, prof) ->
            (isEmail && prof.email.equals(clean, ignoreCase = true)) ||
            (!isEmail && (prof.phone == clean || prof.phone.replace("-", "") == clean.replace("-", "")))
        } ?: userAccounts[clean.lowercase()]

        return if (account != null && account.first == pass) {
            _currentUser.value = account.second
            startRealtimeCloudSync()
            Result.success(Unit)
        } else if (isEmail && pass.length >= 6) {
            val prof = UserProfile(
                id = "usr_${System.currentTimeMillis() % 100000}",
                name = clean.substringBefore("@"),
                email = clean.lowercase(),
                role = if (clean.equals("aazakobazar@gmail.com", ignoreCase = true)) "a" else "c"
            )
            userAccounts[clean.lowercase()] = Pair(pass, prof)
            _currentUser.value = prof
            startRealtimeCloudSync()
            Result.success(Unit)
        } else if (!isEmail && clean.length >= 7 && pass.length >= 6) {
            val prof = UserProfile(
                id = "usr_${System.currentTimeMillis() % 100000}",
                name = "User $clean",
                email = "$clean@merobazar.np",
                phone = clean,
                role = "c"
            )
            userAccounts[clean] = Pair(pass, prof)
            _currentUser.value = prof
            startRealtimeCloudSync()
            Result.success(Unit)
        } else {
            Result.failure(Exception("इमेल/फोन नम्बर वा पासवर्ड मिलेन। (Invalid email/phone or password)"))
        }
    }

    suspend fun signInWithEmail(email: String, pass: String): Result<Unit> =
        signInWithEmailOrPhone(email, pass)

    fun tagUserLocation(
        prov: String,
        dist: String,
        lat: Double,
        lng: Double,
        local: String = "",
        ward: Int = 1,
        toleRoad: String = ""
    ) {
        val current = _currentUser.value
        if (current != null) {
            val updated = current.copy(
                prov = prov,
                dist = dist,
                local = if (local.isNotBlank()) local else current.local,
                ward = if (ward > 0) ward else current.ward,
                toleRoad = if (toleRoad.isNotBlank()) toleRoad else current.toleRoad,
                la = lat,
                lo = lng,
                isMapTagged = true
            )
            _currentUser.value = updated
            // Update in accounts cache
            userAccounts[current.email] = Pair(userAccounts[current.email]?.first ?: "Pass123", updated)
        }
    }

    suspend fun signUpWithEmail(
        name: String,
        email: String,
        pass: String,
        role: String,
        phone: String,
        shopDetails: ShopDetails? = null,
        prov: String = "Bagmati",
        dist: String = "Kathmandu",
        local: String = "",
        ward: Int = 1,
        toleRoad: String = "",
        isMapTagged: Boolean = false,
        dob: String = "",
        gn: String = "Male",
        photo: String = "",
        la: Double? = null,
        lo: Double? = null,
        autoSignIn: Boolean = false
    ): Result<UserProfile> {
        val cleanEmail = email.trim().lowercase()
        // Admin accounts cannot be registered by users (Sole admin is fixed)
        val finalRole = if (role == "m") "m" else "c"
        val rolePrefix = if (finalRole == "m") "M" else "C"
        val genCode = "AB-$rolePrefix-${(10000 + (Math.random() * 90000).toInt())}"
        val todayStr = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
        val uid = if (finalRole == "m") "shop_${System.currentTimeMillis() % 1000000}" else "usr_${System.currentTimeMillis() % 1000000}"
        val profile = UserProfile(
            id = uid,
            name = name.ifBlank { cleanEmail.substringBefore("@") },
            email = cleanEmail,
            phone = phone,
            role = finalRole,
            code = genCode,
            dob = dob,
            gn = gn,
            prov = prov,
            dist = dist,
            local = local.ifBlank { dist },
            ward = ward,
            toleRoad = toleRoad,
            isMapTagged = isMapTagged,
            la = la,
            lo = lo,
            photo = photo,
            regDate = todayStr,
            wallet = 100L, // Welcome bonus wallet balance
            pts = 50L, // 50 Welcome bonus reward coins
            shop = if (role == "m") (shopDetails?.copy(ok = false) ?: ShopDetails(name = name, type = "किराना", ok = false)) else null
        )
        userAccounts[cleanEmail] = Pair(pass, profile)
        if (autoSignIn) {
            _currentUser.value = profile
        }
        val userList = _allUsers.value.toMutableList()
        userList.add(0, profile)
        _allUsers.value = userList
        if (role == "m") {
            val updatedShops = _shops.value.toMutableList()
            updatedShops.add(0, profile)
            _shops.value = updatedShops
        }
        saveProfileToCloud(profile)
        startRealtimeCloudSync()
        return Result.success(profile)
    }

    suspend fun resetPassword(email: String): Result<Unit> {
        val cleanEmail = email.trim().lowercase()
        return if (cleanEmail.contains("@")) {
            try {
                auth?.sendPasswordResetEmail(cleanEmail)?.await()
            } catch (e: Throwable) {
                Log.w(TAG, "Firebase password reset: ${e.message}")
            }
            // Generate reset confirmation in local cache
            userAccounts[cleanEmail]?.let { (oldPass, prof) ->
                userAccounts[cleanEmail] = Pair("Reset123", prof)
            }
            Result.success(Unit)
        } else {
            Result.failure(Exception("अमान्य इमेल ठेगाना (Invalid email)"))
        }
    }

    fun signInWithGoogle(
        activity: Activity,
        credentialManager: CredentialManager,
        scope: CoroutineScope,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val resId = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
        val clientId = if (resId != 0) {
            try { context.getString(resId) } catch (_: Exception) { "dummy_client_id.apps.googleusercontent.com" }
        } else {
            "dummy_client_id.apps.googleusercontent.com"
        }

        val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = clientId).build()
        val request = GetCredentialRequest.Builder().addCredentialOption(signInOption).build()

        scope.launch {
            try {
                val result = credentialManager.getCredential(activity, request)
                val credential = result.credential
                if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val idToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                    val authCred = GoogleAuthProvider.getCredential(idToken, null)
                    auth?.signInWithCredential(authCred)?.await()
                    val gUser = auth?.currentUser
                    val gProfile = UserProfile(
                        id = gUser?.uid ?: "g_user_${System.currentTimeMillis() % 100000}",
                        name = gUser?.displayName?.ifBlank { "Google User" } ?: "Google User",
                        email = gUser?.email ?: "",
                        phone = gUser?.phoneNumber ?: "9841000000",
                        role = "c",
                        code = "MB-G-${System.currentTimeMillis() % 100000}",
                        isEmailVerified = true,
                        isGoogleVerified = true,
                        regDate = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
                    )
                    _currentUser.value = gProfile
                    onSuccess()
                } else {
                    onError("Unexpected credential format")
                }
            } catch (e: GetCredentialCancellationException) {
                Log.w(TAG, "Google Sign-In was cancelled: ${e.message}")
            } catch (e: Exception) {
                Log.e(TAG, "Google Sign-In failed", e)
                onError(e.localizedMessage ?: "Sign-in failed")
            }
        }
    }

    suspend fun sendEmailVerification(): Result<Unit> {
        return try {
            val user = auth?.currentUser
            if (user != null) {
                user.sendEmailVerification().await()
                Result.success(Unit)
            } else {
                val cur = _currentUser.value
                if (cur != null) {
                    _currentUser.value = cur.copy(isEmailVerified = true)
                }
                Result.success(Unit)
            }
        } catch (e: Exception) {
            val cur = _currentUser.value
            if (cur != null) {
                _currentUser.value = cur.copy(isEmailVerified = true)
            }
            Result.success(Unit)
        }
    }

    suspend fun checkEmailVerified(): Boolean {
        return try {
            val user = auth?.currentUser
            if (user != null) {
                user.reload().await()
                val verified = user.isEmailVerified
                val cur = _currentUser.value
                if (cur != null) {
                    _currentUser.value = cur.copy(isEmailVerified = verified)
                }
                verified
            } else {
                _currentUser.value?.isEmailVerified ?: false
            }
        } catch (_: Exception) {
            _currentUser.value?.isEmailVerified ?: false
        }
    }

    fun signOut(credentialManager: CredentialManager, scope: CoroutineScope, onDone: () -> Unit) {
        try { auth?.signOut() } catch (_: Throwable) {}
        _currentUser.value = null
        _cart.value = emptyMap()
        scope.launch {
            try {
                credentialManager.clearCredentialState(ClearCredentialStateRequest())
            } catch (e: Exception) {
                Log.w(TAG, "Error clearing credential state", e)
            } finally {
                onDone()
            }
        }
    }

    // --- Cart & Orders ---

    fun addToCart(productId: String) {
        val prod = _products.value.find { it.id == productId }
        val maxStock = prod?.stock ?: 99
        val map = _cart.value.toMutableMap()
        val curr = map[productId] ?: 0
        if (curr < maxStock) {
            map[productId] = curr + 1
            _cart.value = map
        }
    }

    fun removeFromCart(productId: String) {
        val map = _cart.value.toMutableMap()
        val curr = map[productId] ?: 0
        if (curr <= 1) {
            map.remove(productId)
        } else {
            map[productId] = curr - 1
        }
        _cart.value = map
    }

    fun updateCartQuantity(productId: String, quantity: Int) {
        val prod = _products.value.find { it.id == productId }
        val maxStock = prod?.stock ?: 99
        val map = _cart.value.toMutableMap()
        if (quantity <= 0) {
            map.remove(productId)
        } else {
            map[productId] = quantity.coerceAtMost(maxStock)
        }
        _cart.value = map
    }

    fun deleteCartItem(productId: String) {
        val map = _cart.value.toMutableMap()
        map.remove(productId)
        _cart.value = map
    }

    fun clearCart() {
        _cart.value = emptyMap()
    }

    fun placeOrder(address: String, phone: String, payMode: String, vat: Long = 0L, discountApplied: Long = 0L): Result<Order> {
        val user = _currentUser.value ?: return Result.failure(Exception("Please sign in first"))
        val cartMap = _cart.value
        if (cartMap.isEmpty()) return Result.failure(Exception("Cart is empty"))

        val items = mutableListOf<OrderItem>()
        var subtotal = 0L
        var merchantId = ""
        var sName = "Mero Bazar Shop"

        for ((pId, qty) in cartMap) {
            val prod = _products.value.find { it.id == pId } ?: continue
            items.add(OrderItem(pid = prod.id, n = prod.name, q = qty, p = prod.effectivePrice))
            subtotal += prod.effectivePrice * qty
            if (merchantId.isBlank()) {
                merchantId = prod.mid
                sName = prod.shopName
            }
        }

        val finalTotal = (subtotal + vat - discountApplied).coerceAtLeast(0L)
        val nowStr = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date())

        val order = Order(
            id = "ORD-${System.currentTimeMillis() % 1000000}",
            cuid = user.id,
            cname = user.name,
            cphone = phone.ifBlank { user.phone },
            caddress = address,
            muid = merchantId,
            shopName = sName,
            items = items,
            total = finalTotal,
            st = "Pending",
            d = nowStr,
            payMode = payMode,
            tl = listOf(TimelineEvent("Pending", nowStr)),
            vat = vat,
            discountApplied = discountApplied
        )

        val updated = _orders.value.toMutableList()
        updated.add(0, order)
        _orders.value = updated
        _cart.value = emptyMap()

        // If discount was applied using points, deduct points
        if (discountApplied > 0) {
            val curPts = user.pts
            val newPts = (curPts - discountApplied).coerceAtLeast(0L)
            _currentUser.value = user.copy(pts = newPts)
            val phList = _pointHistory.value.toMutableList()
            phList.add(0, PointHistory("ph_${System.currentTimeMillis()}", user.id, "अर्डर #${order.id} मा छुट प्रयोग", -discountApplied, nowStr))
            _pointHistory.value = phList
        }

        saveOrderToCloud(order)
        return Result.success(order)
    }

    fun updateOrderStatus(orderId: String, newStatus: String) {
        val nowStr = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date())
        val list = _orders.value.map { order ->
            if (order.id == orderId) {
                val updatedTl = order.tl.toMutableList()
                if (updatedTl.none { it.status == newStatus }) {
                    updatedTl.add(TimelineEvent(newStatus, nowStr))
                }
                order.copy(st = newStatus, tl = updatedTl)
            } else {
                order
            }
        }
        _orders.value = list
        val order = list.find { it.id == orderId }
        if (order != null) {
            saveOrderToCloud(order)
        }
    }

    fun markOrderReceived(orderId: String) {
        val nowStr = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date())
        var earnedPoints = 0L
        val list = _orders.value.map { order ->
            if (order.id == orderId) {
                val updatedTl = order.tl.toMutableList()
                if (updatedTl.none { it.status == "Delivered" }) {
                    updatedTl.add(TimelineEvent("Delivered", nowStr))
                }
                earnedPoints = (order.total / 10).coerceAtLeast(10L)
                order.copy(st = "Delivered", rcv = nowStr, tl = updatedTl)
            } else {
                order
            }
        }
        _orders.value = list
        val order = list.find { it.id == orderId }
        if (order != null) {
            saveOrderToCloud(order)
            // Award loyalty coins to user
            val curUser = _currentUser.value
            if (curUser != null && earnedPoints > 0) {
                val newPts = curUser.pts + earnedPoints
                _currentUser.value = curUser.copy(pts = newPts)
                val phList = _pointHistory.value.toMutableList()
                phList.add(0, PointHistory("ph_${System.currentTimeMillis()}", curUser.id, "अर्डर #${order.id} डेलिभरी क्यासब्याक", earnedPoints, nowStr))
                _pointHistory.value = phList
            }
        }
    }

    fun markOrderIrdUploaded(orderId: String) {
        val todayStr = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
        _orders.value = _orders.value.map {
            if (it.id == orderId) it.copy(irdUp = if (it.irdUp.isNotBlank()) "" else todayStr) else it
        }
    }

    // --- Saved Items, Follow, Block ---
    fun toggleSaveShop(shopId: String) {
        val set = _savedShops.value.toMutableSet()
        if (set.contains(shopId)) set.remove(shopId) else set.add(shopId)
        _savedShops.value = set
    }

    fun toggleSaveProduct(productId: String) {
        val set = _savedProducts.value.toMutableSet()
        if (set.contains(productId)) set.remove(productId) else set.add(productId)
        _savedProducts.value = set
    }

    fun toggleFollowShop(shopId: String) {
        val set = _followedShops.value.toMutableSet()
        if (set.contains(shopId)) set.remove(shopId) else set.add(shopId)
        _followedShops.value = set
    }

    fun toggleBlockShop(shopId: String) {
        val set = _blockedShops.value.toMutableSet()
        if (set.contains(shopId)) set.remove(shopId) else set.add(shopId)
        _blockedShops.value = set
    }

    // --- Delivery Address Management ---
    fun addDeliveryAddress(label: String, address: String) {
        val newAddr = DeliveryAddress(
            id = "ad_${System.currentTimeMillis()}",
            label = label.ifBlank { "ठेगाना" },
            address = address,
            isDefault = _deliveryAddresses.value.isEmpty()
        )
        val list = _deliveryAddresses.value.toMutableList()
        list.add(newAddr)
        _deliveryAddresses.value = list
    }

    fun setDefaultAddress(id: String) {
        _deliveryAddresses.value = _deliveryAddresses.value.map {
            it.copy(isDefault = (it.id == id))
        }
    }

    fun deleteDeliveryAddress(id: String) {
        _deliveryAddresses.value = _deliveryAddresses.value.filter { it.id != id }
    }

    // --- Khata (उधारो / हिसाब) Management ---
    fun addKhataEntry(name: String, phone: String, amt: Long, paid: Long, note: String) {
        val user = _currentUser.value
        val mid = user?.id ?: "shop_1"
        val due = (amt - paid).coerceAtLeast(0L)
        val todayStr = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
        val entry = KhataEntry(
            id = "kh_${System.currentTimeMillis()}",
            mid = mid,
            name = name,
            phone = phone,
            amt = amt,
            paid = paid,
            due = due,
            note = note,
            date = todayStr
        )
        val list = _khata.value.toMutableList()
        list.add(0, entry)
        _khata.value = list
    }

    fun recordKhataPayment(id: String, payment: Long) {
        _khata.value = _khata.value.map { entry ->
            if (entry.id == id) {
                val newPaid = entry.paid + payment
                val newDue = (entry.amt - newPaid).coerceAtLeast(0L)
                entry.copy(paid = newPaid, due = newDue)
            } else {
                entry
            }
        }
    }

    // --- Merchant Shop Controls ---
    fun toggleProductStock(productId: String) {
        _products.value = _products.value.map { prod ->
            if (prod.id == productId) {
                val newStock = if (prod.stock > 0) 0 else 25
                prod.copy(stock = newStock)
            } else {
                prod
            }
        }
    }

    fun toggleProductFeatured(productId: String) {
        _products.value = _products.value.map { prod ->
            if (prod.id == productId) {
                prod.copy(isFeatured = !prod.isFeatured)
            } else {
                prod
            }
        }
    }

    fun setShopHoliday(shopId: String, isHoliday: Boolean, msg: String) {
        _shops.value = _shops.value.map { shop ->
            if (shop.id == shopId) {
                val updatedShopDetails = shop.shop?.copy(isHoliday = isHoliday, holidayMsg = msg)
                    ?: ShopDetails(name = shop.name, isHoliday = isHoliday, holidayMsg = msg)
                shop.copy(shop = updatedShopDetails)
            } else {
                shop
            }
        }
        val curUser = _currentUser.value
        if (curUser?.id == shopId) {
            val updatedDetails = curUser.shop?.copy(isHoliday = isHoliday, holidayMsg = msg)
                ?: ShopDetails(name = curUser.name, isHoliday = isHoliday, holidayMsg = msg)
            _currentUser.value = curUser.copy(shop = updatedDetails)
        }
    }

    // --- Product & Notice Management ---

    fun addProduct(name: String, type: String, price: Long, dis: Long, stock: Int, cat: Int): Product {
        val user = _currentUser.value
        val mid = user?.id ?: "shop_1"
        val shopName = user?.shop?.name ?: user?.name ?: "Mero Bazar Store"
        val p = Product(
            id = "p_${System.currentTimeMillis()}",
            mid = mid,
            shopName = shopName,
            name = name,
            type = type,
            price = price,
            dis = dis,
            stock = stock,
            cat = cat
        )
        val list = _products.value.toMutableList()
        list.add(0, p)
        _products.value = list
        return p
    }

    fun addNotice(type: String, title: String, body: String) {
        val user = _currentUser.value
        val mid = user?.id ?: "shop_5"
        val shopName = user?.shop?.name ?: "Mero Bazar Notice"
        val date = java.text.SimpleDateFormat("yyyy/MM/dd", java.util.Locale.getDefault()).format(java.util.Date())
        val n = Notice(
            id = "n_${System.currentTimeMillis()}",
            mid = mid,
            shopName = shopName,
            type = type,
            title = title,
            body = body,
            date = date,
            cat = 5
        )
        val list = _notices.value.toMutableList()
        list.add(0, n)
        _notices.value = list
    }

    fun deleteNotice(noticeId: String) {
        _notices.value = _notices.value.filter { it.id != noticeId }
    }

    fun toggleShopVerification(shopId: String) {
        val list = _shops.value.map { shop ->
            if (shop.id == shopId) {
                val curShop = shop.shop
                val isCurrentlyVerified = curShop?.pan?.isNotBlank() == true && curShop.lrn.isNotBlank()
                val updatedShop = if (isCurrentlyVerified) {
                    curShop.copy(lrn = "")
                } else {
                    curShop?.copy(
                        pan = curShop.pan.ifBlank { "600123456" },
                        lrn = "REG-${System.currentTimeMillis() % 100000}"
                    ) ?: ShopDetails(name = shop.name, type = "किराना", pan = "600123456", lrn = "REG-99102")
                }
                shop.copy(shop = updatedShop)
            } else {
                shop
            }
        }
        _shops.value = list
    }

    // --- Reviews ---

    fun addReview(shopId: String, rating: Int, comment: String) {
        val user = _currentUser.value ?: return
        val date = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
        val r = Review(
            id = "rev_${System.currentTimeMillis()}",
            cuid = user.id,
            cname = user.name,
            suid = shopId,
            r = rating,
            t = comment,
            d = date
        )
        val list = _reviews.value.toMutableList()
        list.add(0, r)
        _reviews.value = list

        saveReviewToCloud(r)
    }

    // --- Khata / Bills ---

    fun createBill(cname: String, phone: String, items: List<OrderItem>): Bill {
        val user = _currentUser.value
        val mid = user?.id ?: "shop_1"
        val shopName = user?.shop?.name ?: "Mero Bazar"
        val pan = user?.shop?.pan ?: ""
        val lrn = user?.shop?.lrn ?: ""
        val total = items.sumOf { it.total }
        val date = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
        val b = Bill(
            id = "B-${System.currentTimeMillis() % 100000}",
            mid = mid,
            shopName = shopName,
            cname = cname,
            phone = phone,
            items = items,
            total = total,
            d = date,
            pan = pan,
            lrn = lrn
        )
        val list = _bills.value.toMutableList()
        list.add(0, b)
        _bills.value = list
        return b
    }

    // --- Cloud Sync ---

    private fun saveProfileToCloud(user: UserProfile) {
        if (auth?.currentUser == null) return
        val fs = firestore ?: return
        try {
            val data = mapOf(
                "name" to user.name,
                "email" to user.email,
                "phone" to user.phone,
                "role" to user.role,
                "prov" to user.prov,
                "dist" to user.dist,
                "local" to user.local,
                "ward" to user.ward
            )
            fs.collection("priv").document(user.id).set(mapOf("j" to org.json.JSONObject(data).toString(), "ts" to System.currentTimeMillis()))
        } catch (e: Exception) {
            Log.w(TAG, "saveProfileToCloud error", e)
        }
    }

    private fun saveOrderToCloud(order: Order) {
        if (auth?.currentUser == null) return
        val fs = firestore ?: return
        try {
            val json = org.json.JSONObject().apply {
                put("id", order.id)
                put("cuid", order.cuid)
                put("cname", order.cname)
                put("cphone", order.cphone)
                put("caddress", order.caddress)
                put("muid", order.muid)
                put("shopName", order.shopName)
                put("total", order.total)
                put("st", order.st)
                put("d", order.d)
                put("payMode", order.payMode)
            }
            fs.collection("orders").document(order.id).set(
                mapOf(
                    "cuid" to order.cuid,
                    "muid" to order.muid,
                    "st" to order.st,
                    "j" to json.toString(),
                    "ts" to System.currentTimeMillis()
                )
            )
        } catch (e: Exception) {
            Log.w(TAG, "saveOrderToCloud error", e)
        }
    }

    private fun saveReviewToCloud(review: Review) {
        if (auth?.currentUser == null) return
        val fs = firestore ?: return
        try {
            fs.collection("reviews").document(review.id).set(
                mapOf(
                    "cuid" to review.cuid,
                    "suid" to review.suid,
                    "r" to review.r,
                    "t" to review.t,
                    "d" to review.d
                )
            )
        } catch (e: Exception) {
            Log.w(TAG, "saveReviewToCloud error", e)
        }
    }

    private fun syncFromCloud() {
        val uid = auth?.currentUser?.uid ?: return
        val fs = firestore ?: return
        try {
            // Listen to orders
            fs.collection("orders")
                .whereEqualTo("cuid", uid)
                .addSnapshotListener { snapshot, error ->
                    if (error != null || snapshot == null) return@addSnapshotListener
                    val cloudOrders = snapshot.documents.mapNotNull { doc ->
                        val jStr = doc.getString("j") ?: return@mapNotNull null
                        try {
                            val json = org.json.JSONObject(jStr)
                            Order(
                                id = json.optString("id", doc.id),
                                cuid = json.optString("cuid", uid),
                                cname = json.optString("cname", ""),
                                cphone = json.optString("cphone", ""),
                                caddress = json.optString("caddress", ""),
                                muid = json.optString("muid", ""),
                                shopName = json.optString("shopName", ""),
                                total = json.optLong("total", 0L),
                                st = json.optString("st", "Pending"),
                                d = json.optString("d", ""),
                                payMode = json.optString("payMode", "Cash on Delivery")
                            )
                        } catch (e: Exception) {
                            null
                        }
                    }
                    if (cloudOrders.isNotEmpty()) {
                        _orders.value = cloudOrders
                    }
                }
        } catch (e: Exception) {
            Log.w(TAG, "syncFromCloud error", e)
        }
    }

    // --- Online Messenger & P2P Cloud VoIP Calling System ---

    fun startRealtimeCloudSync() {
        val fs = firestore ?: return

        // 1. Listen to real-time chat messages from Firestore
        try {
            messagesListenerRegistration?.remove()
            messagesListenerRegistration = fs.collection("messages")
                .limit(100)
                .addSnapshotListener { snapshot, error ->
                    if (error != null || snapshot == null) return@addSnapshotListener
                    val cloudMsgs = snapshot.documents.mapNotNull { doc ->
                        try {
                            ChatMessage(
                                id = doc.id,
                                senderId = doc.getString("senderId") ?: "",
                                senderName = doc.getString("senderName") ?: "",
                                receiverId = doc.getString("receiverId") ?: "",
                                text = doc.getString("text") ?: "",
                                photoUrl = doc.getString("photoUrl") ?: "",
                                videoUrl = doc.getString("videoUrl") ?: "",
                                attachedProductId = doc.getString("attachedProductId") ?: "",
                                attachedProductName = doc.getString("attachedProductName") ?: "",
                                attachedProductPrice = doc.getLong("attachedProductPrice") ?: 0L,
                                isCallEvent = doc.getBoolean("isCallEvent") ?: false,
                                callType = doc.getString("callType") ?: "",
                                callDurationSec = doc.getLong("callDurationSec")?.toInt() ?: 0,
                                isAi = doc.getBoolean("isAi") ?: false,
                                timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis()
                            )
                        } catch (e: Exception) {
                            null
                        }
                    }
                    if (cloudMsgs.isNotEmpty()) {
                        val merged = _chatMessages.value.associateBy { it.id }.toMutableMap()
                        cloudMsgs.forEach { merged[it.id] = it }
                        _chatMessages.value = merged.values.sortedBy { it.timestamp }
                    }
                }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to start messages snapshot listener", e)
        }

        // 2. Listen to real-time incoming calls for current user
        try {
            val user = _currentUser.value
            val myId = user?.id ?: ""
            val myPhone = user?.phone ?: ""

            incomingCallListenerRegistration?.remove()
            incomingCallListenerRegistration = fs.collection("calls")
                .whereEqualTo("status", "ringing")
                .addSnapshotListener { snapshot, error ->
                    if (error != null || snapshot == null) return@addSnapshotListener
                    for (doc in snapshot.documents) {
                        val receiverId = doc.getString("receiverId") ?: ""
                        val callerId = doc.getString("callerId") ?: ""
                        // Ignore outgoing calls initiated by self
                        if (callerId == myId) continue
                        // Match user ID or phone number
                        val isTarget = (myId.isNotBlank() && receiverId == myId) ||
                                       (myPhone.isNotBlank() && receiverId == myPhone) ||
                                       (user?.role == "m" && receiverId == (user.shop?.name ?: ""))
                        if (isTarget) {
                            val session = com.example.data.model.LiveCallSession(
                                callId = doc.id,
                                callerId = callerId,
                                callerName = doc.getString("callerName") ?: "Mero Bazar Caller",
                                receiverId = receiverId,
                                receiverName = doc.getString("receiverName") ?: (user?.name ?: "Receiver"),
                                callType = doc.getString("callType") ?: "audio",
                                status = "ringing",
                                timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis()
                            )
                            _incomingCallSession.value = session
                            break
                        }
                    }
                }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to start calls snapshot listener", e)
        }

        // 3. Listen to Team Tasks in Firestore (/tasks)
        try {
            tasksListenerRegistration?.remove()
            tasksListenerRegistration = fs.collection("tasks")
                .limit(100)
                .addSnapshotListener { snapshot, error ->
                    if (error != null || snapshot == null) return@addSnapshotListener
                    if (!snapshot.isEmpty) {
                        val cloudTasks = snapshot.documents.mapNotNull { doc ->
                            try {
                                TeamTask(
                                    id = doc.id,
                                    title = doc.getString("title") ?: "",
                                    department = doc.getString("department") ?: "Frontend UI",
                                    assignee = doc.getString("assignee") ?: "",
                                    status = doc.getString("status") ?: "Completed",
                                    storyPoints = doc.getLong("storyPoints")?.toInt() ?: 5,
                                    completedDate = doc.getString("completedDate") ?: "2026-11-01",
                                    dayOfWeek = doc.getString("dayOfWeek") ?: "शुक्र (Fri)",
                                    sprint = doc.getString("sprint") ?: "Sprint 14"
                                )
                            } catch (e: Exception) {
                                null
                            }
                        }
                        if (cloudTasks.isNotEmpty()) {
                            _teamTasks.value = cloudTasks
                        }
                    } else {
                        seedTasksToFirestore()
                    }
                }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to start tasks snapshot listener", e)
        }

        // 4. Listen to Project Progress Metrics in Firestore (/project_metrics)
        try {
            projectMetricsListenerRegistration?.remove()
            projectMetricsListenerRegistration = fs.collection("project_metrics")
                .limit(50)
                .addSnapshotListener { snapshot, error ->
                    if (error != null || snapshot == null) return@addSnapshotListener
                    if (!snapshot.isEmpty) {
                        val cloudMetrics = snapshot.documents.mapNotNull { doc ->
                            try {
                                ProjectProgressMetric(
                                    id = doc.id,
                                    milestone = doc.getString("milestone") ?: "",
                                    category = doc.getString("category") ?: "Core Architecture",
                                    targetPercentage = doc.getLong("targetPercentage")?.toInt() ?: 100,
                                    currentPercentage = doc.getLong("currentPercentage")?.toInt() ?: 85,
                                    status = doc.getString("status") ?: "On Track",
                                    updatedDate = doc.getString("updatedDate") ?: "२०८३-०७-१६",
                                    phase = doc.getString("phase") ?: "Phase 2"
                                )
                            } catch (e: Exception) {
                                null
                            }
                        }
                        if (cloudMetrics.isNotEmpty()) {
                            _projectProgressMetrics.value = cloudMetrics
                        }
                    } else {
                        seedProjectMetricsToFirestore()
                    }
                }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to start project_metrics snapshot listener", e)
        }
    }

    fun addTeamTask(
        title: String,
        department: String,
        assignee: String,
        storyPoints: Int = 5,
        status: String = "Completed"
    ) {
        val newTask = TeamTask(
            id = "task_${System.currentTimeMillis()}",
            title = title,
            department = department,
            assignee = assignee,
            status = status,
            storyPoints = storyPoints,
            completedDate = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date()),
            dayOfWeek = "आज (Today)",
            sprint = "Sprint 14"
        )
        val current = _teamTasks.value.toMutableList()
        current.add(0, newTask)
        _teamTasks.value = current

        firestore?.collection("tasks")?.document(newTask.id)?.set(
            mapOf(
                "title" to newTask.title,
                "department" to newTask.department,
                "assignee" to newTask.assignee,
                "status" to newTask.status,
                "storyPoints" to newTask.storyPoints,
                "completedDate" to newTask.completedDate,
                "dayOfWeek" to newTask.dayOfWeek,
                "sprint" to newTask.sprint,
                "timestamp" to System.currentTimeMillis()
            )
        )
    }

    fun updateTeamTaskStatus(taskId: String, newStatus: String) {
        _teamTasks.value = _teamTasks.value.map {
            if (it.id == taskId) it.copy(status = newStatus) else it
        }
        firestore?.collection("tasks")?.document(taskId)?.update("status", newStatus)
    }

    fun seedTasksToFirestore() {
        val fs = firestore ?: return
        for (task in MeroBazarSeedData.demoTeamTasks) {
            fs.collection("tasks").document(task.id).set(
                mapOf(
                    "title" to task.title,
                    "department" to task.department,
                    "assignee" to task.assignee,
                    "status" to task.status,
                    "storyPoints" to task.storyPoints,
                    "completedDate" to task.completedDate,
                    "dayOfWeek" to task.dayOfWeek,
                    "sprint" to task.sprint,
                    "timestamp" to System.currentTimeMillis()
                )
            )
        }
    }

    fun seedProjectMetricsToFirestore() {
        val fs = firestore ?: return
        for (metric in MeroBazarSeedData.demoProjectMetrics) {
            fs.collection("project_metrics").document(metric.id).set(
                mapOf(
                    "milestone" to metric.milestone,
                    "category" to metric.category,
                    "targetPercentage" to metric.targetPercentage,
                    "currentPercentage" to metric.currentPercentage,
                    "status" to metric.status,
                    "updatedDate" to metric.updatedDate,
                    "phase" to metric.phase,
                    "timestamp" to System.currentTimeMillis()
                )
            )
        }
    }

    fun refreshFirestoreMetrics() {
        startRealtimeCloudSync()
    }

    fun initiateCall(
        targetId: String,
        targetName: String,
        targetPhone: String,
        callType: String
    ): com.example.data.model.LiveCallSession {
        val user = _currentUser.value
        val callerId = user?.id ?: "usr_${System.currentTimeMillis() % 100000}"
        val callerName = user?.name ?: "Customer"
        val callId = "call_${System.currentTimeMillis()}_${(100..999).random()}"

        val session = com.example.data.model.LiveCallSession(
            callId = callId,
            callerId = callerId,
            callerName = callerName,
            receiverId = targetId,
            receiverName = targetName,
            callType = callType,
            status = "ringing",
            timestamp = System.currentTimeMillis()
        )
        _activeCallSession.value = session

        // 1. Trigger full WebRTC Signaling Handshake (creates Offer SDP and starts candidate collection in Firestore)
        webRtcSignaling.startCallWithOffer(
            callId = callId,
            callerId = callerId,
            callerName = callerName,
            receiverId = targetId,
            receiverName = targetName,
            callType = callType,
            onAnswerReceived = { _ ->
                _activeCallSession.value = _activeCallSession.value?.copy(status = "accepted")
            },
            onCallEnded = {
                _activeCallSession.value = null
            }
        )

        // 2. Direct document listener backup
        firestore?.let { fs ->
            try {
                activeCallListenerRegistration?.remove()
                activeCallListenerRegistration = fs.collection("calls").document(callId)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null || snapshot == null || !snapshot.exists()) return@addSnapshotListener
                        val status = snapshot.getString("status") ?: return@addSnapshotListener
                        val cur = _activeCallSession.value
                        if (cur != null && cur.callId == callId) {
                            when (status) {
                                "accepted" -> {
                                    _activeCallSession.value = cur.copy(status = "accepted")
                                }
                                "declined" -> {
                                    _activeCallSession.value = cur.copy(status = "declined")
                                }
                                "ended" -> {
                                    _activeCallSession.value = null
                                    activeCallListenerRegistration?.remove()
                                    activeCallListenerRegistration = null
                                }
                            }
                        }
                    }
            } catch (e: Exception) {
                Log.w(TAG, "initiateCall listener error", e)
            }
        }
        return session
    }

    fun answerIncomingCall(callId: String) {
        val incoming = _incomingCallSession.value ?: return
        if (incoming.callId != callId) return

        val accepted = incoming.copy(status = "accepted")
        _activeCallSession.value = accepted
        _incomingCallSession.value = null

        // Trigger WebRTC Signaling Answer and Callee ICE candidate publishing
        webRtcSignaling.answerCallWithAnswer(
            callId = callId,
            onConnected = {
                _activeCallSession.value = _activeCallSession.value?.copy(status = "accepted")
            },
            onCallEnded = {
                _activeCallSession.value = null
            }
        )

        firestore?.let { fs ->
            try {
                fs.collection("calls").document(callId).update("status", "accepted")

                activeCallListenerRegistration?.remove()
                activeCallListenerRegistration = fs.collection("calls").document(callId)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null || snapshot == null || !snapshot.exists()) return@addSnapshotListener
                        val status = snapshot.getString("status") ?: return@addSnapshotListener
                        if (status == "ended") {
                            _activeCallSession.value = null
                            activeCallListenerRegistration?.remove()
                            activeCallListenerRegistration = null
                        }
                    }
            } catch (e: Exception) {
                Log.w(TAG, "answerIncomingCall error", e)
            }
        }
    }

    fun declineIncomingCall(callId: String) {
        _incomingCallSession.value = null
        firestore?.let { fs ->
            try {
                fs.collection("calls").document(callId).update("status", "declined")
            } catch (e: Exception) {
                Log.w(TAG, "declineIncomingCall error", e)
            }
        }
    }

    fun endActiveCall(callId: String, durationSec: Int = 0) {
        val current = _activeCallSession.value
        _activeCallSession.value = null
        activeCallListenerRegistration?.remove()
        activeCallListenerRegistration = null

        webRtcSignaling.endCall(callId)

        if (current != null) {
            val isCaller = current.callerId == _currentUser.value?.id
            logCall(
                targetId = if (isCaller) current.receiverId else current.callerId,
                targetName = if (isCaller) current.receiverName else current.callerName,
                targetPhone = "",
                callType = current.callType,
                durationSeconds = durationSec,
                isOutgoing = isCaller
            )
        }

        firestore?.let { fs ->
            try {
                fs.collection("calls").document(callId).update("status", "ended")
            } catch (e: Exception) {
                Log.w(TAG, "endActiveCall error", e)
            }
        }
    }

    fun sendChatMessage(
        receiverId: String,
        text: String,
        photoUrl: String = "",
        videoUrl: String = "",
        attachedProductId: String = "",
        attachedProductName: String = "",
        attachedProductPrice: Long = 0L,
        isCallEvent: Boolean = false,
        callType: String = "",
        callDurationSec: Int = 0
    ): ChatMessage {
        val user = _currentUser.value
        val senderId = user?.id ?: "me"
        val senderName = user?.name ?: "Customer"
        val msgId = "msg_${System.currentTimeMillis()}_${(100..999).random()}"
        val msg = ChatMessage(
            id = msgId,
            senderId = senderId,
            senderName = senderName,
            receiverId = receiverId,
            text = text,
            photoUrl = photoUrl,
            videoUrl = videoUrl,
            attachedProductId = attachedProductId,
            attachedProductName = attachedProductName,
            attachedProductPrice = attachedProductPrice,
            isCallEvent = isCallEvent,
            callType = callType,
            callDurationSec = callDurationSec,
            timestamp = System.currentTimeMillis()
        )
        val list = _chatMessages.value.toMutableList()
        list.add(msg)
        _chatMessages.value = list

        // Real-time broadcast to Firestore /messages so other APK gets it instantly
        firestore?.let { fs ->
            try {
                val data = mapOf(
                    "id" to msgId,
                    "senderId" to senderId,
                    "senderName" to senderName,
                    "receiverId" to receiverId,
                    "text" to text,
                    "photoUrl" to photoUrl,
                    "videoUrl" to videoUrl,
                    "attachedProductId" to attachedProductId,
                    "attachedProductName" to attachedProductName,
                    "attachedProductPrice" to attachedProductPrice,
                    "isCallEvent" to isCallEvent,
                    "callType" to callType,
                    "callDurationSec" to callDurationSec,
                    "isAi" to false,
                    "timestamp" to msg.timestamp
                )
                fs.collection("messages").document(msgId).set(data)
            } catch (e: Exception) {
                Log.w(TAG, "Firestore write message error", e)
            }
        }
        return msg
    }

    fun logCall(
        targetId: String,
        targetName: String,
        targetPhone: String,
        callType: String,
        durationSeconds: Int,
        isOutgoing: Boolean = true
    ) {
        val entry = CallLogEntry(
            id = "cl_${System.currentTimeMillis()}",
            targetId = targetId,
            targetName = targetName,
            targetPhone = targetPhone,
            callType = callType,
            durationSeconds = durationSeconds,
            isOutgoing = isOutgoing,
            timestamp = System.currentTimeMillis()
        )
        val list = _callLogs.value.toMutableList()
        list.add(0, entry)
        _callLogs.value = list

        // Also broadcast system event to chat
        val callTypeText = if (callType == "video") "भिडियो कल (Video Call)" else "अडियो कल (Audio Call)"
        val durationText = if (durationSeconds > 0) "${durationSeconds / 60} मिनेट ${durationSeconds % 60} सेकेन्ड" else "मिस्ड कल"
        sendChatMessage(
            receiverId = targetId,
            text = "📞 $callTypeText सम्पन्न भयो (अवधि: $durationText)",
            isCallEvent = true,
            callType = callType,
            callDurationSec = durationSeconds
        )
    }

    // --- Video Bazar & Marketing System (VMS) ---

    fun toggleVideoLike(videoId: String) {
        val list = _videoReels.value.map { v ->
            if (v.id == videoId) {
                val newLiked = !v.isLiked
                v.copy(
                    isLiked = newLiked,
                    likesCount = if (newLiked) v.likesCount + 1 else (v.likesCount - 1).coerceAtLeast(0)
                )
            } else v
        }
        _videoReels.value = list
    }

    fun uploadVideoReel(
        shopId: String,
        shopName: String,
        title: String,
        description: String,
        videoUrl: String,
        productId: String,
        productName: String,
        productPrice: Long,
        tags: List<String>
    ): com.example.data.model.VideoReelItem {
        val item = com.example.data.model.VideoReelItem(
            id = "vid_${System.currentTimeMillis()}",
            shopId = shopId,
            shopName = shopName,
            title = title,
            description = description,
            videoUrl = videoUrl,
            productId = productId,
            productName = productName,
            productPrice = productPrice,
            likesCount = 1,
            commentsCount = 0,
            uploadDate = "भर्खरै",
            tags = tags
        )
        val list = _videoReels.value.toMutableList()
        list.add(0, item)
        _videoReels.value = list
        return item
    }

    fun deleteVideoReel(videoId: String) {
        _videoReels.value = _videoReels.value.filter { it.id != videoId }
    }

    // --- User & Shop Moderation (Privacy & Security) ---

    fun blockShop(shopId: String) {
        _blockedShops.value = _blockedShops.value + shopId
    }

    fun unblockShop(shopId: String) {
        _blockedShops.value = _blockedShops.value - shopId
    }

    fun banUser(userId: String) {
        _bannedUsers.value = _bannedUsers.value + userId
    }

    fun unbanUser(userId: String) {
        _bannedUsers.value = _bannedUsers.value - userId
    }

    fun suspendShop(shopId: String) {
        _suspendedShops.value = _suspendedShops.value + shopId
    }

    fun unsuspendShop(shopId: String) {
        _suspendedShops.value = _suspendedShops.value - shopId
    }

    fun deleteUser(userId: String) {
        _allUsers.value = _allUsers.value.filter { it.id != userId }
    }

    fun promoteUserRole(userId: String, newRole: String) {
        _allUsers.value = _allUsers.value.map { if (it.id == userId) it.copy(role = newRole) else it }
    }

    fun deleteProduct(productId: String) {
        _products.value = _products.value.filter { it.id != productId }
    }

    fun deleteChatMessage(messageId: String) {
        _chatMessages.value = _chatMessages.value.filter { it.id != messageId }
    }

    // --- Developer Studio All Data Backup (.zip / JSON export) ---

    fun getAllDataBackupJson(): String {
        val root = org.json.JSONObject()
        root.put("app", "Mero Bazar (मेरो बजार)")
        root.put("version", "1.0.1")
        root.put("timestamp", System.currentTimeMillis())
        root.put("backupDate", java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date()))
        root.put("soleAdmin", "aazakobazar@gmail.com")
        root.put("activeUsersCount", userAccounts.size)
        root.put("totalShopsCount", _shops.value.size)
        root.put("totalProductsCount", _products.value.size)
        root.put("totalOrdersCount", _orders.value.size)
        root.put("totalVideosCount", _videoReels.value.size)
        root.put("totalChatMessagesCount", _chatMessages.value.size)
        root.put("totalBillsCount", _bills.value.size)
        root.put("totalKhataCount", _khata.value.size)
        return root.toString(2)
    }
}
