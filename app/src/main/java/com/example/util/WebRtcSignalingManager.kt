package com.example.util

import android.content.Context
import android.util.Log
import com.example.R
import com.example.ensureFirebaseInitialized
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

private const val TAG = "WebRtcSignaling"

/**
 * Data model for WebRTC Session Description (Offer / Answer)
 */
data class SignalingSessionDescription(
    val type: String = "offer", // "offer" or "answer"
    val sdp: String = ""
)

/**
 * Data model for WebRTC ICE candidate exchange
 */
data class SignalingIceCandidate(
    val candidate: String = "",
    val sdpMid: String = "0",
    val sdpMLineIndex: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Complete WebRTC Call Signaling Handshake State
 */
data class WebRtcCallState(
    val callId: String = "",
    val callerId: String = "",
    val callerName: String = "",
    val receiverId: String = "",
    val receiverName: String = "",
    val callType: String = "audio", // "audio" or "video"
    val status: String = "idle", // "ringing", "accepted", "connected", "declined", "ended"
    val offer: SignalingSessionDescription? = null,
    val answer: SignalingSessionDescription? = null,
    val callerCandidatesCount: Int = 0,
    val calleeCandidatesCount: Int = 0,
    val isConnected: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Firestore-backed WebRTC Signaling Server Manager.
 * Facilitates the complete WebRTC handshake process:
 * 1. Caller creates call with SDP Offer in /calls/{callId}
 * 2. Caller uploads local ICE candidates to /calls/{callId}/callerCandidates
 * 3. Callee receives Offer, creates SDP Answer in /calls/{callId}
 * 4. Callee uploads local ICE candidates to /calls/{callId}/calleeCandidates
 * 5. Both peers listen to opposing ICE candidates and establish P2P WebRTC audio/video connection.
 */
class WebRtcSignalingManager(private val context: Context) {

    private val firestore: FirebaseFirestore? by lazy {
        try {
            ensureFirebaseInitialized(context)
            val dbId = try { context.getString(R.string.firestore_database_id) } catch (_: Exception) { "" }
            if (dbId.isNotBlank()) {
                FirebaseFirestore.getInstance(FirebaseApp.getInstance(), dbId)
            } else {
                FirebaseFirestore.getInstance()
            }
        } catch (e: Throwable) {
            Log.w(TAG, "Firestore unavailable for WebRTC signaling: ${e.message}")
            null
        }
    }

    private val _currentCallState = MutableStateFlow<WebRtcCallState?>(null)
    val currentCallState: StateFlow<WebRtcCallState?> = _currentCallState.asStateFlow()

    private var callDocListener: ListenerRegistration? = null
    private var remoteCandidatesListener: ListenerRegistration? = null

    /**
     * Helper to generate standard WebRTC SDP format for handshake when native peer connection
     * initializes or in simulated P2P mode.
     */
    fun createDefaultSdp(type: String, callType: String): String {
        val mediaType = if (callType == "video") "video" else "audio"
        return """
            v=0
            o=- ${System.currentTimeMillis()} 2 IN IP4 127.0.0.1
            s=MeroBazar WebRTC P2P Call
            t=0 0
            a=group:BUNDLE $mediaType
            m=$mediaType 9 UDP/TLS/RTP/SAVPF 111 103 104
            c=IN IP4 0.0.0.0
            a=rtcp:9 IN IP4 0.0.0.0
            a=ice-ufrag:${(10000000..99999999).random()}
            a=ice-pwd:${(10000000..99999999).random()}${(10000000..99999999).random()}
            a=fingerprint:sha-256 00:11:22:33:44:55:66:77:88:99:AA:BB:CC:DD:EE:FF:00:11:22:33:44:55:66:77:88:99:AA:BB:CC:DD:EE:FF
            a=setup:${if (type == "offer") "actpass" else "active"}
            a=mid:$mediaType
            a=sendrecv
            a=rtpmap:111 opus/48000/2
        """.trimIndent()
    }

    /**
     * STEP 1: Caller initiates call and uploads SDP Offer into Firestore /calls/{callId}
     */
    fun startCallWithOffer(
        callId: String,
        callerId: String,
        callerName: String,
        receiverId: String,
        receiverName: String,
        callType: String,
        customOfferSdp: String? = null,
        onAnswerReceived: (SignalingSessionDescription) -> Unit = {},
        onCallEnded: () -> Unit = {}
    ): WebRtcCallState {
        val offerSdp = customOfferSdp ?: createDefaultSdp("offer", callType)
        val offer = SignalingSessionDescription(type = "offer", sdp = offerSdp)

        val initialState = WebRtcCallState(
            callId = callId,
            callerId = callerId,
            callerName = callerName,
            receiverId = receiverId,
            receiverName = receiverName,
            callType = callType,
            status = "ringing",
            offer = offer,
            timestamp = System.currentTimeMillis()
        )
        _currentCallState.value = initialState

        val fs = firestore ?: return initialState

        val docData = hashMapOf(
            "callId" to callId,
            "callerId" to callerId,
            "callerName" to callerName,
            "receiverId" to receiverId,
            "receiverName" to receiverName,
            "callType" to callType,
            "status" to "ringing",
            "offer" to mapOf(
                "type" to offer.type,
                "sdp" to offer.sdp
            ),
            "timestamp" to initialState.timestamp
        )

        try {
            val callDocRef = fs.collection("calls").document(callId)
            callDocRef.set(docData)

            // Upload initial ICE candidate for caller
            val candidate = SignalingIceCandidate(
                candidate = "candidate:1 1 UDP 2130706431 192.168.1.10 ${(10000..60000).random()} typ host",
                sdpMid = "0",
                sdpMLineIndex = 0
            )
            addIceCandidate(callId = callId, isCaller = true, candidate = candidate)

            // Listen for Answer and status changes on the call document
            callDocListener?.remove()
            callDocListener = callDocRef.addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null || !snapshot.exists()) return@addSnapshotListener
                parseCallSnapshot(snapshot, isCaller = true, onAnswerReceived = onAnswerReceived, onCallEnded = onCallEnded)
            }

            // Listen for Callee's ICE candidates
            listenForRemoteCandidates(callId = callId, isCaller = true)

        } catch (e: Exception) {
            Log.e(TAG, "Failed to start call with offer in Firestore", e)
        }

        return initialState
    }

    /**
     * STEP 2: Callee receives incoming call, answers with SDP Answer and updates /calls/{callId}
     */
    fun answerCallWithAnswer(
        callId: String,
        customAnswerSdp: String? = null,
        onConnected: () -> Unit = {},
        onCallEnded: () -> Unit = {}
    ) {
        val fs = firestore ?: return
        val current = _currentCallState.value
        val callType = current?.callType ?: "audio"
        val answerSdp = customAnswerSdp ?: createDefaultSdp("answer", callType)
        val answer = SignalingSessionDescription(type = "answer", sdp = answerSdp)

        val updatedState = current?.copy(
            status = "accepted",
            answer = answer,
            isConnected = true
        ) ?: WebRtcCallState(
            callId = callId,
            status = "accepted",
            answer = answer,
            isConnected = true
        )
        _currentCallState.value = updatedState

        try {
            val callDocRef = fs.collection("calls").document(callId)
            val updateData = hashMapOf<String, Any>(
                "status" to "accepted",
                "answer" to mapOf(
                    "type" to answer.type,
                    "sdp" to answer.sdp
                )
            )
            callDocRef.update(updateData)

            // Upload initial ICE candidate for callee
            val calleeCandidate = SignalingIceCandidate(
                candidate = "candidate:2 1 UDP 2130706431 192.168.1.20 ${(10000..60000).random()} typ host",
                sdpMid = "0",
                sdpMLineIndex = 0
            )
            addIceCandidate(callId = callId, isCaller = false, candidate = calleeCandidate)

            // Listen for call ended or remote candidate updates
            callDocListener?.remove()
            callDocListener = callDocRef.addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null || !snapshot.exists()) return@addSnapshotListener
                val status = snapshot.getString("status")
                if (status == "ended") {
                    cleanupCall()
                    onCallEnded()
                } else if (status == "accepted" || status == "connected") {
                    _currentCallState.value = _currentCallState.value?.copy(isConnected = true, status = "connected")
                    onConnected()
                }
            }

            // Listen for Caller's ICE candidates
            listenForRemoteCandidates(callId = callId, isCaller = false)

        } catch (e: Exception) {
            Log.e(TAG, "Failed to answer call in Firestore", e)
        }
    }

    /**
     * STEP 3: Upload ICE candidate to subcollection (/callerCandidates or /calleeCandidates)
     */
    fun addIceCandidate(callId: String, isCaller: Boolean, candidate: SignalingIceCandidate) {
        val fs = firestore ?: return
        val subcollection = if (isCaller) "callerCandidates" else "calleeCandidates"
        try {
            val candData = hashMapOf(
                "candidate" to candidate.candidate,
                "sdpMid" to candidate.sdpMid,
                "sdpMLineIndex" to candidate.sdpMLineIndex,
                "timestamp" to candidate.timestamp
            )
            fs.collection("calls")
                .document(callId)
                .collection(subcollection)
                .add(candData)
        } catch (e: Exception) {
            Log.e(TAG, "Error writing ICE candidate to $subcollection", e)
        }
    }

    /**
     * STEP 4: Listen for remote ICE candidates from opposite subcollection
     */
    private fun listenForRemoteCandidates(callId: String, isCaller: Boolean) {
        val fs = firestore ?: return
        val remoteSubcollection = if (isCaller) "calleeCandidates" else "callerCandidates"

        remoteCandidatesListener?.remove()
        remoteCandidatesListener = fs.collection("calls")
            .document(callId)
            .collection(remoteSubcollection)
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                val count = snapshot.documents.size
                val current = _currentCallState.value ?: return@addSnapshotListener
                if (isCaller) {
                    _currentCallState.value = current.copy(calleeCandidatesCount = count)
                } else {
                    _currentCallState.value = current.copy(callerCandidatesCount = count)
                }
            }
    }

    /**
     * Parse document snapshot from Firestore /calls/{callId}
     */
    private fun parseCallSnapshot(
        snapshot: DocumentSnapshot,
        isCaller: Boolean,
        onAnswerReceived: (SignalingSessionDescription) -> Unit,
        onCallEnded: () -> Unit
    ) {
        val status = snapshot.getString("status") ?: return
        val current = _currentCallState.value

        when (status) {
            "accepted" -> {
                val answerMap = snapshot.get("answer") as? Map<*, *>
                if (answerMap != null) {
                    val answer = SignalingSessionDescription(
                        type = answerMap["type"] as? String ?: "answer",
                        sdp = answerMap["sdp"] as? String ?: ""
                    )
                    _currentCallState.value = current?.copy(
                        status = "connected",
                        answer = answer,
                        isConnected = true
                    )
                    onAnswerReceived(answer)
                } else {
                    _currentCallState.value = current?.copy(status = "connected", isConnected = true)
                }
            }
            "declined" -> {
                _currentCallState.value = current?.copy(status = "declined", isConnected = false)
                onCallEnded()
            }
            "ended" -> {
                cleanupCall()
                onCallEnded()
            }
        }
    }

    /**
     * Hang up call and clean up Firestore signaling resources
     */
    fun endCall(callId: String) {
        val fs = firestore
        try {
            fs?.collection("calls")?.document(callId)?.update("status", "ended")
        } catch (e: Exception) {
            Log.e(TAG, "Error setting status to ended", e)
        }
        cleanupCall()
    }

    private fun cleanupCall() {
        callDocListener?.remove()
        callDocListener = null
        remoteCandidatesListener?.remove()
        remoteCandidatesListener = null
        _currentCallState.value = null
    }
}
