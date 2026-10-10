package com.example.util

import android.content.ClipData
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

object ShareHelper {

    val PRESET_WHATSAPP_NUMBERS = listOf(
        "01733-073076",
        "01329-644275",
        "01732-650021",
        "01402619434"
    )

    const val DEFAULT_BACKUP_EMAIL = "d.mohonroy@gmail.com"

    fun getMimeType(file: File): String {
        return when {
            file.name.endsWith(".pdf", ignoreCase = true) -> "application/pdf"
            file.name.endsWith(".doc", ignoreCase = true) || file.name.endsWith(".docx", ignoreCase = true) -> "application/msword"
            file.name.endsWith(".html", ignoreCase = true) || file.name.endsWith(".htm", ignoreCase = true) -> "text/html"
            file.name.endsWith(".csv", ignoreCase = true) -> "text/csv"
            file.name.endsWith(".json", ignoreCase = true) -> "application/json"
            file.name.endsWith(".txt", ignoreCase = true) -> "text/plain"
            file.name.endsWith(".jpg", ignoreCase = true) || file.name.endsWith(".jpeg", ignoreCase = true) -> "image/jpeg"
            file.name.endsWith(".png", ignoreCase = true) -> "image/png"
            else -> "*/*"
        }
    }

    fun getFileUri(context: Context, file: File): Uri {
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }

    /**
     * Shares any file (PDF, DOC, CSV) via standard Android Share Sheet.
     * Ensures ClipData and URI permissions are properly granted.
     */
    fun shareFile(context: Context, file: File, title: String = "শেয়ার করুন") {
        try {
            if (!file.exists() || file.length() == 0L) {
                Toast.makeText(context, "ফাইল তৈরি করা সম্ভব হয়নি বা ফাইলটি খালি।", Toast.LENGTH_SHORT).show()
                return
            }

            val uri = getFileUri(context, file)
            val mimeType = getMimeType(file)

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, title)
                putExtra(Intent.EXTRA_TEXT, title)
                clipData = ClipData.newRawUri(file.name, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooserIntent = Intent.createChooser(shareIntent, title).apply {
                clipData = ClipData.newRawUri(file.name, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            // Grant read permission to all potential matching targets
            val resolveInfoList = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.queryIntentActivities(
                    chooserIntent,
                    PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_DEFAULT_ONLY.toLong())
                )
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.queryIntentActivities(chooserIntent, PackageManager.MATCH_DEFAULT_ONLY)
            }

            for (resolveInfo in resolveInfoList) {
                val packageName = resolveInfo.activityInfo.packageName
                try {
                    context.grantUriPermission(packageName, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                } catch (_: Exception) {}
            }

            context.startActivity(chooserIntent)
        } catch (e: Exception) {
            Toast.makeText(context, "শেয়ার করা সম্ভব হয়নি: ${e.localizedMessage ?: e.message}", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Shares a file directly to WhatsApp (with or without specific recipient number).
     */
    fun shareToWhatsApp(
        context: Context,
        file: File,
        rawPhoneNumber: String? = null,
        caption: String = "RRF HR - জামানতনামা এগ্রিমেন্ট ফাইল"
    ) {
        try {
            if (!file.exists() || file.length() == 0L) {
                Toast.makeText(context, "ফাইল তৈরি করা সম্ভব হয়নি বা ফাইলটি খালি।", Toast.LENGTH_SHORT).show()
                return
            }

            val uri = getFileUri(context, file)
            val mimeType = getMimeType(file)

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_TEXT, caption)
                clipData = ClipData.newRawUri(file.name, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                setPackage("com.whatsapp")
            }

            if (!rawPhoneNumber.isNullOrBlank()) {
                val cleanPhone = cleanPhoneNumberForWhatsApp(rawPhoneNumber)
                intent.putExtra("jid", "$cleanPhone@s.whatsapp.net")
            }

            try {
                context.grantUriPermission("com.whatsapp", uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                context.startActivity(intent)
            } catch (_: Exception) {
                // Try WhatsApp Business package
                try {
                    val w4bIntent = Intent(intent).apply {
                        setPackage("com.whatsapp.w4b")
                    }
                    context.grantUriPermission("com.whatsapp.w4b", uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    context.startActivity(w4bIntent)
                } catch (_: Exception) {
                    // Fallback to regular chooser if WhatsApp is not installed
                    shareFile(context, file, caption)
                }
            }
        } catch (_: Exception) {
            shareFile(context, file, caption)
        }
    }

    /**
     * Shares to email with default d.mohonroy@gmail.com or custom address.
     */
    fun shareViaEmail(
        context: Context,
        file: File,
        recipientEmail: String = DEFAULT_BACKUP_EMAIL,
        subject: String,
        body: String
    ) {
        try {
            if (!file.exists() || file.length() == 0L) {
                Toast.makeText(context, "ফাইল তৈরি করা সম্ভব হয়নি বা ফাইলটি খালি।", Toast.LENGTH_SHORT).show()
                return
            }

            val uri = getFileUri(context, file)
            val mimeType = getMimeType(file)

            val emailIntent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_EMAIL, arrayOf(recipientEmail))
                putExtra(Intent.EXTRA_SUBJECT, subject)
                putExtra(Intent.EXTRA_TEXT, body)
                putExtra(Intent.EXTRA_STREAM, uri)
                clipData = ClipData.newRawUri(file.name, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(emailIntent, "মেইলে পাঠান ($recipientEmail)").apply {
                clipData = ClipData.newRawUri(file.name, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            context.startActivity(chooser)
        } catch (e: Exception) {
            Toast.makeText(context, "মেইল অ্যাপ খুঁজে পাওয়া যায়নি: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Opens PDF / file in system default viewer or print app.
     */
    fun openFile(context: Context, file: File) {
        try {
            if (!file.exists() || file.length() == 0L) {
                Toast.makeText(context, "ফাইল তৈরি করা সম্ভব হয়নি বা ফাইলটি খালি।", Toast.LENGTH_SHORT).show()
                return
            }

            if (file.name.endsWith(".html", ignoreCase = true) || file.name.endsWith(".htm", ignoreCase = true)) {
                openHtmlInBrowser(context, file)
                return
            }

            val uri = getFileUri(context, file)
            val mimeType = getMimeType(file)

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, mimeType)
                clipData = ClipData.newRawUri(file.name, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            val chooser = Intent.createChooser(intent, "ফাইল ওপেন / প্রিন্ট করুন").apply {
                clipData = ClipData.newRawUri(file.name, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            val resolveInfoList = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.queryIntentActivities(
                    chooser,
                    PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_DEFAULT_ONLY.toLong())
                )
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.queryIntentActivities(chooser, PackageManager.MATCH_DEFAULT_ONLY)
            }

            for (resolveInfo in resolveInfoList) {
                val packageName = resolveInfo.activityInfo.packageName
                try {
                    context.grantUriPermission(packageName, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                } catch (_: Exception) {}
            }

            context.startActivity(chooser)
        } catch (_: Exception) {
            shareFile(context, file, file.name)
        }
    }

    /**
     * Dedicated robust method to open HTML files in Chrome or external browsers.
     * Saves copy to public Downloads so external browsers can safely read local HTML.
     */
    fun openHtmlInBrowser(context: Context, file: File) {
        try {
            if (!file.exists() || file.length() == 0L) {
                Toast.makeText(context, "এইচটিএমএল ফাইল পাওয়া যায়নি!", Toast.LENGTH_SHORT).show()
                return
            }

            // Copy to public Downloads/RRF_HR_Forms for browser accessibility
            saveFileToDownloads(context, file, "RRF_HR_Web_Portal.html")

            val uri = getFileUri(context, file)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "text/html")
                clipData = ClipData.newRawUri(file.name, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            val chooser = Intent.createChooser(intent, "ওয়েব ব্রাউজারে অপেন করুন (Chrome/Browser)").apply {
                clipData = ClipData.newRawUri(file.name, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            context.startActivity(chooser)
        } catch (e: Exception) {
            fallbackShare(context, file, "আরআরএফ ওয়েব ভার্সন পোর্টাল")
        }
    }

    /**
     * Saves PDF copy to device Downloads folder so the user can easily find and share it.
     */
    fun saveFileToDownloads(context: Context, file: File, displayName: String = file.name): Boolean {
        return try {
            if (!file.exists()) return false

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, displayName)
                    put(MediaStore.MediaColumns.MIME_TYPE, getMimeType(file))
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/RRF_HR_Forms")
                }
                val uri = context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                if (uri != null) {
                    context.contentResolver.openOutputStream(uri)?.use { output ->
                        FileInputStream(file).use { input ->
                            input.copyTo(output)
                        }
                    }
                    Toast.makeText(context, "ডাউনলোড ফোল্ডারে সেভ হয়েছে: $displayName", Toast.LENGTH_LONG).show()
                    true
                } else {
                    false
                }
            } else {
                @Suppress("DEPRECATION")
                val targetDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "RRF_HR_Forms")
                if (!targetDir.exists()) targetDir.mkdirs()
                val targetFile = File(targetDir, displayName)
                FileInputStream(file).use { input ->
                    FileOutputStream(targetFile).use { output ->
                        input.copyTo(output)
                    }
                }
                Toast.makeText(context, "ডাউনলোড ফোল্ডারে সেভ হয়েছে: $displayName", Toast.LENGTH_LONG).show()
                true
            }
        } catch (e: Exception) {
            Toast.makeText(context, "ডাউনলোড সেভ ত্রুটি: ${e.message}", Toast.LENGTH_SHORT).show()
            false
        }
    }

    fun fallbackShare(context: Context, file: File, title: String) {
        shareFile(context, file, title)
    }

    fun cleanPhoneNumberForWhatsApp(phone: String): String {
        var digits = phone.filter { it.isDigit() }
        if (digits.startsWith("0")) {
            digits = "88" + digits
        } else if (!digits.startsWith("88") && digits.length == 10) {
            digits = "880" + digits
        }
        return digits
    }
}
