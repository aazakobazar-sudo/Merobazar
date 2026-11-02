package com.example.data.model

data class UserProfile(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val role: String = "c", // "c" = customer, "m" = merchant, "a" = admin
    val code: String = "", // e.g. "AB-C-45892", "AB-M-78214", "AB-A-10001"
    val dob: String = "",
    val gn: String = "Male", // "Male", "Female", "Other"
    val prov: String = "Bagmati",
    val dist: String = "Kathmandu",
    val local: String = "Kathmandu",
    val ward: Int = 10,
    val toleRoad: String = "",
    val isMapTagged: Boolean = false,
    val la: Double? = null,
    val lo: Double? = null,
    val photo: String = "",
    val wallet: Long = 0L,
    val pts: Long = 100L,
    val regDate: String = "",
    val isEmailVerified: Boolean = false,
    val isGoogleVerified: Boolean = false,
    val shop: ShopDetails? = null
)

data class ContactPerson(
    val id: String = "",
    val name: String = "",
    val phone: String = "",
    val email: String = "",
    val role: String = "customer", // "customer", "merchant", "admin", "support"
    val shopName: String = "",
    val address: String = "",
    val district: String = "",
    val balanceDue: Long = 0L,
    val ordersCount: Int = 0,
    val lastActive: String = ""
)

data class ShopDetails(
    val name: String = "",
    val cat: Int = 1, // 1 to 5
    val type: String = "किराना",
    val open: String = "08:00",
    val close: String = "20:00",
    val rad: Int = 5,
    val min: Long = 0L,
    val ok: Boolean = true,
    val exp: String = "2027-12-31",
    val demo: Boolean = false,
    val pan: String = "",
    val lrn: String = "",
    val doc: String = "",
    val pm: String = "eSewa",
    val ref: String = "",
    val rating: Double = 4.8,
    val reviewCount: Int = 12,
    val isHoliday: Boolean = false,
    val holidayMsg: String = ""
)

data class AppUpdateInfo(
    val currentVersion: String = "1.0.0",
    val latestVersion: String = "1.0.1",
    val isUpdateAvailable: Boolean = false,
    val releaseNotes: String = "नयाँ दर्ता प्रमाण कार्ड, अद्यावधिक डिजाइन, सुदृढ कार्यसम्पादन तथा बग सुधारहरू।",
    val githubReleaseUrl: String = "https://github.com/aazakobazar/mero-bazar/releases",
    val directApkUrl: String = "https://github.com/aazakobazar/mero-bazar/releases/download/v1.0.1/mero-bazar.apk",
    val autoUpgradeEnabled: Boolean = true,
    val lastSyncTime: String = "भर्खरै"
)

data class Category(
    val id: Int,
    val name: String,
    val emoji: String,
    val subTypes: List<String>
)

data class Product(
    val id: String = "",
    val mid: String = "",
    val shopName: String = "",
    val name: String = "",
    val type: String = "",
    val price: Long = 0L,
    val dis: Long = 0L,
    val stock: Int = 0,
    val img: String = "",
    val cat: Int = 1,
    val isFeatured: Boolean = false
) {
    val effectivePrice: Long get() = (price - dis).coerceAtLeast(0L)
    val inStock: Boolean get() = stock > 0
}

data class TimelineEvent(
    val status: String = "",
    val time: String = ""
)

data class DeliveryAddress(
    val id: String = "",
    val label: String = "घर",
    val address: String = "",
    val isDefault: Boolean = false,
    val street: String = "",
    val cityWard: String = "",
    val district: String = "",
    val phone: String = ""
)

data class KhataEntry(
    val id: String = "",
    val mid: String = "",
    val name: String = "",
    val phone: String = "",
    val amt: Long = 0L,
    val paid: Long = 0L,
    val due: Long = 0L,
    val note: String = "",
    val date: String = ""
) {
    val customerName: String get() = name
    val totalDebt: Long get() = amt
    val totalPaid: Long get() = paid
    val balance: Long get() = due
    val description: String get() = note
}

data class PointHistory(
    val id: String = "",
    val uid: String = "",
    val title: String = "",
    val amount: Long = 0L,
    val date: String = ""
)

data class Notice(
    val id: String = "",
    val mid: String = "",
    val shopName: String = "",
    val type: String = "",
    val title: String = "",
    val body: String = "",
    val date: String = "",
    val cat: Int = 5
)

data class OrderItem(
    val pid: String = "",
    val n: String = "",
    val q: Int = 1,
    val p: Long = 0L
) {
    val total: Long get() = q * p
}

data class Order(
    val id: String = "",
    val cuid: String = "",
    val cname: String = "",
    val cphone: String = "",
    val caddress: String = "",
    val muid: String = "",
    val shopName: String = "",
    val items: List<OrderItem> = emptyList(),
    val total: Long = 0L,
    val st: String = "Pending", // Pending, Accepted, Packed, Shipped, Delivered, Cancelled
    val d: String = "",
    val payMode: String = "Cash on Delivery",
    val tl: List<TimelineEvent> = emptyList(),
    val rcv: String = "",
    val irdUp: String = "",
    val vat: Long = 0L,
    val discountApplied: Long = 0L
)

data class Review(
    val id: String = "",
    val cuid: String = "",
    val cname: String = "",
    val suid: String = "",
    val r: Int = 5,
    val t: String = "",
    val d: String = ""
)

data class Bill(
    val id: String = "",
    val mid: String = "",
    val shopName: String = "",
    val cname: String = "",
    val phone: String = "",
    val items: List<OrderItem> = emptyList(),
    val total: Long = 0L,
    val d: String = "",
    val pan: String = "",
    val lrn: String = "",
    val irdUp: String = ""
)

data class ChatMessage(
    val id: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val receiverId: String = "",
    val text: String = "",
    val photoUrl: String = "",
    val videoUrl: String = "",
    val attachedProductId: String = "",
    val attachedProductName: String = "",
    val attachedProductPrice: Long = 0L,
    val isCallEvent: Boolean = false,
    val callType: String = "", // "audio" or "video"
    val callDurationSec: Int = 0,
    val isAi: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

data class LiveCallSession(
    val callId: String = "",
    val callerId: String = "",
    val callerName: String = "",
    val receiverId: String = "",
    val receiverName: String = "",
    val callType: String = "audio", // "audio" or "video"
    val status: String = "idle", // "ringing", "accepted", "declined", "ended"
    val timestamp: Long = System.currentTimeMillis()
)

data class CallLogEntry(
    val id: String = "",
    val targetId: String = "",
    val targetName: String = "",
    val targetPhone: String = "",
    val callType: String = "audio", // "audio" or "video"
    val durationSeconds: Int = 0,
    val isOutgoing: Boolean = true,
    val timestamp: Long = System.currentTimeMillis()
)

data class VideoReelItem(
    val id: String = "",
    val shopId: String = "",
    val shopName: String = "",
    val title: String = "",
    val description: String = "",
    val videoUrl: String = "",
    val productId: String = "",
    val productName: String = "",
    val productPrice: Long = 0L,
    val likesCount: Int = 42,
    val commentsCount: Int = 5,
    val isLiked: Boolean = false,
    val uploadDate: String = "आज",
    val tags: List<String> = emptyList()
)

data class VideoComment(
    val id: String = "",
    val videoId: String = "",
    val userName: String = "",
    val text: String = "",
    val timeAgo: String = "२ मिनेट अगाडि"
)

data class TeamTask(
    val id: String = "",
    val title: String = "",
    val department: String = "Frontend UI",
    val assignee: String = "",
    val status: String = "Completed", // Completed, In Progress, Pending
    val storyPoints: Int = 5,
    val completedDate: String = "2026-11-01",
    val dayOfWeek: String = "शुक्र (Fri)",
    val sprint: String = "Sprint 14"
)

data class ProjectProgressMetric(
    val id: String = "",
    val milestone: String = "",
    val category: String = "Core Architecture",
    val targetPercentage: Int = 100,
    val currentPercentage: Int = 85,
    val status: String = "On Track", // Completed, On Track, In Progress
    val updatedDate: String = "२०८३-०७-१६",
    val phase: String = "Phase 2"
)

data class FirestoreProjectConfig(
    val projectName: String = "Mero bazar",
    val projectId: String = "gen-lang-client-0042520297",
    val projectNumber: String = "909476023440",
    val environment: String = "Unspecified / Cloud Firestore",
    val publicFacingName: String = "Merobazar",
    val supportEmail: String = "aazakobazar@gmail.com"
)

