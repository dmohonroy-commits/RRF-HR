package com.example.data.cloud

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.FirebaseFunctionsException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

/**
 * Result wrapper for Firebase Cloud Functions operations.
 */
sealed class CloudFunctionResult<out T> {
    data class Success<out T>(val data: T) : CloudFunctionResult<T>()
    data class Error(val message: String, val code: String? = null, val exception: Throwable? = null) : CloudFunctionResult<Nothing>()
}

/**
 * Manager class to interact with Firebase Cloud Functions from the Android client.
 * Provides safe calls with fallback when Firebase is not yet configured with google-services.json.
 */
class FirebaseFunctionsManager(private val context: Context) {

    companion object {
        private const val TAG = "FirebaseFunctionsMgr"
        
        @Volatile
        private var instance: FirebaseFunctionsManager? = null

        fun getInstance(context: Context): FirebaseFunctionsManager {
            return instance ?: synchronized(this) {
                instance ?: FirebaseFunctionsManager(context.applicationContext).also { instance = it }
            }
        }
    }

    /**
     * Check if Firebase is initialized in the app.
     */
    fun isFirebaseAvailable(): Boolean {
        return try {
            FirebaseApp.getApps(context).isNotEmpty()
        } catch (e: Exception) {
            Log.w(TAG, "Firebase not initialized: ${e.message}")
            false
        }
    }

    private fun getFunctions(): FirebaseFunctions? {
        return if (isFirebaseAvailable()) {
            try {
                FirebaseFunctions.getInstance()
            } catch (e: Exception) {
                Log.e(TAG, "Error getting FirebaseFunctions instance", e)
                null
            }
        } else {
            null
        }
    }

    /**
     * Generic helper to invoke an HTTPS Callable Cloud Function with coroutines.
     */
    private suspend fun <T> callFunction(
        functionName: String,
        data: Any?,
        transform: (Any?) -> T
    ): CloudFunctionResult<T> = withContext(Dispatchers.IO) {
        val functions = getFunctions()
        if (functions == null) {
            return@withContext CloudFunctionResult.Error(
                message = "Firebase Cloud Functions নিষ্ক্রিয়: অনুগ্রহ করে google-services.json ফাইল যুক্ত করুন।",
                code = "FIREBASE_NOT_CONFIGURED"
            )
        }

        try {
            suspendCancellableCoroutine<CloudFunctionResult<T>> { continuation ->
                functions.getHttpsCallable(functionName)
                    .call(data)
                    .addOnSuccessListener { result ->
                        try {
                            val parsed = transform(result.data)
                            continuation.resume(CloudFunctionResult.Success(parsed))
                        } catch (e: Exception) {
                            Log.e(TAG, "Failed to parse result from $functionName", e)
                            continuation.resume(
                                CloudFunctionResult.Error(
                                    message = "ফাংশনের উত্তর রূপান্তরে সমস্যা হয়েছে: ${e.message}",
                                    code = "PARSE_ERROR",
                                    exception = e
                                )
                            )
                        }
                    }
                    .addOnFailureListener { exception ->
                        Log.e(TAG, "Cloud function $functionName failed", exception)
                        val code = (exception as? FirebaseFunctionsException)?.code?.name ?: "UNKNOWN"
                        val userMsg = when ((exception as? FirebaseFunctionsException)?.code) {
                            FirebaseFunctionsException.Code.UNAUTHENTICATED -> "অনুমোদন প্রয়োজন (Unauthenticated)"
                            FirebaseFunctionsException.Code.PERMISSION_DENIED -> "অনুমতি নেই (Permission Denied)"
                            FirebaseFunctionsException.Code.NOT_FOUND -> "ক্লাউড ফাংশনটি পাওয়া যায়নি (Not Found)"
                            FirebaseFunctionsException.Code.UNAVAILABLE -> "ক্লাউড সার্ভার সংযোগ পাওয়া যায়নি (Unavailable)"
                            else -> exception.localizedMessage ?: "ক্লাউড ফাংশন ব্যর্থ হয়েছে"
                        }
                        continuation.resume(
                            CloudFunctionResult.Error(
                                message = userMsg,
                                code = code,
                                exception = exception
                            )
                        )
                    }
            }
        } catch (e: Exception) {
            CloudFunctionResult.Error(
                message = e.localizedMessage ?: "অপ্রত্যাশিত ত্রুটি ঘটেছে",
                exception = e
            )
        }
    }

    /**
     * Check health status of Cloud Functions backend.
     */
    suspend fun checkHealth(): CloudFunctionResult<Map<String, Any?>> {
        return callFunction("checkHealth", null) { rawData ->
            @Suppress("UNCHECKED_CAST")
            (rawData as? Map<String, Any?>) ?: mapOf("status" to "ok")
        }
    }

    /**
     * Backup complete worker agreement forms to Firebase Cloud Functions / Firestore.
     */
    suspend fun backupAgreementForms(
        employeeName: String,
        employeeNid: String,
        formsPayloadJson: String
    ): CloudFunctionResult<Map<String, Any?>> {
        val payload = mapOf(
            "employeeName" to employeeName,
            "employeeNid" to employeeNid,
            "formsData" to formsPayloadJson,
            "timestamp" to System.currentTimeMillis()
        )
        return callFunction("backupAgreementForms", payload) { rawData ->
            @Suppress("UNCHECKED_CAST")
            (rawData as? Map<String, Any?>) ?: mapOf("success" to true)
        }
    }

    /**
     * Synchronize worker profile details to Firebase Cloud Functions.
     */
    suspend fun syncWorkerProfile(workerData: Map<String, Any?>): CloudFunctionResult<Map<String, Any?>> {
        return callFunction("syncWorkerProfile", workerData) { rawData ->
            @Suppress("UNCHECKED_CAST")
            (rawData as? Map<String, Any?>) ?: mapOf("success" to true)
        }
    }

    /**
     * Verify worker information against central organization records via Cloud Functions.
     */
    suspend fun verifyEmployeeOnline(nid: String, mobile: String): CloudFunctionResult<Map<String, Any?>> {
        val payload = mapOf(
            "nid" to nid,
            "mobile" to mobile
        )
        return callFunction("verifyEmployeeOnline", payload) { rawData ->
            @Suppress("UNCHECKED_CAST")
            (rawData as? Map<String, Any?>) ?: mapOf("verified" to true)
        }
    }

    /**
     * Send notification or SMS alert for employee verification through Cloud Functions.
     */
    suspend fun sendVerificationNotification(
        mobile: String,
        message: String,
        recipientRole: String = "Guarantor"
    ): CloudFunctionResult<Map<String, Any?>> {
        val payload = mapOf(
            "mobile" to mobile,
            "message" to message,
            "role" to recipientRole
        )
        return callFunction("sendVerificationNotification", payload) { rawData ->
            @Suppress("UNCHECKED_CAST")
            (rawData as? Map<String, Any?>) ?: mapOf("sent" to true)
        }
    }
}
