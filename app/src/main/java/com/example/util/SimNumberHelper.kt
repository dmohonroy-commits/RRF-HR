package com.example.util

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import androidx.core.content.ContextCompat

object SimNumberHelper {

    /**
     * Reads active SIM phone numbers installed in the phone (SIM 1 and SIM 2).
     * Returns a list of formatted 11-digit mobile numbers in Bangla digits.
     */
    @SuppressLint("HardwareIds", "MissingPermission")
    fun getSimPhoneNumbers(context: Context): List<String> {
        val numbers = mutableListOf<String>()

        val hasPhoneStatePermission = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.READ_PHONE_STATE
        ) == PackageManager.PERMISSION_GRANTED

        val hasPhoneNumbersPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.READ_PHONE_NUMBERS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }

        if (!hasPhoneStatePermission && !hasPhoneNumbersPermission) {
            return emptyList()
        }

        try {
            val subscriptionManager = context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as? SubscriptionManager
            if (subscriptionManager != null) {
                val activeList = subscriptionManager.activeSubscriptionInfoList
                if (!activeList.isNullOrEmpty()) {
                    for (info in activeList) {
                        var rawNum = ""
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            try {
                                rawNum = subscriptionManager.getPhoneNumber(info.subscriptionId)
                            } catch (_: Exception) {
                                rawNum = info.number ?: ""
                            }
                        } else {
                            @Suppress("DEPRECATION")
                            rawNum = info.number ?: ""
                        }

                        val formatted = formatPhoneNumber(rawNum)
                        if (formatted.isNotBlank() && !numbers.contains(formatted)) {
                            numbers.add(formatted)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (numbers.isEmpty()) {
            try {
                val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
                @Suppress("DEPRECATION")
                val line1 = tm?.line1Number.orEmpty()
                val formatted = formatPhoneNumber(line1)
                if (formatted.isNotBlank()) {
                    numbers.add(formatted)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        return numbers
    }

    /**
     * Formats raw phone number string to standard 11-digit Bangladeshi mobile format in Bangla digits.
     */
    fun formatPhoneNumber(raw: String): String {
        if (raw.isBlank()) return ""
        val englishDigits = BanglaTextValidator.toEnglishDigits(raw).filter { it.isDigit() }
        if (englishDigits.length >= 11) {
            val last11 = englishDigits.takeLast(11)
            if (last11.startsWith("01")) {
                return BanglaTextValidator.toBanglaDigits(last11)
            }
        }
        return if (englishDigits.isNotBlank()) BanglaTextValidator.toBanglaDigits(englishDigits.take(11)) else ""
    }
}
