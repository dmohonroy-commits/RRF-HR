package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File

object ShareHelper {

    val PRESET_WHATSAPP_NUMBERS = listOf(
        "01733-073076",
        "01329-644275",
        "01732-650021",
        "01402619434"
    )

    const val DEFAULT_BACKUP_EMAIL = "d.mohonroy@gmail.com"

    fun getFileUri(context: Context, file: File): Uri {
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }

    /**
     * Shares a file to WhatsApp with a specific phone number or general WhatsApp share.
     */
    fun shareToWhatsApp(
        context: Context,
        file: File,
        rawPhoneNumber: String? = null,
        caption: String = "RRF HR - জামানতনামা এগ্রিমেন্ট ফাইল"
    ) {
        try {
            val uri = getFileUri(context, file)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = when {
                    file.name.endsWith(".pdf", ignoreCase = true) -> "application/pdf"
                    file.name.endsWith(".doc", ignoreCase = true) -> "application/msword"
                    file.name.endsWith(".csv", ignoreCase = true) -> "text/csv"
                    else -> "*/*"
                }
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_TEXT, caption)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                setPackage("com.whatsapp")
            }

            if (!rawPhoneNumber.isNullOrBlank()) {
                val cleanPhone = cleanPhoneNumberForWhatsApp(rawPhoneNumber)
                intent.putExtra("jid", "$cleanPhone@s.whatsapp.net")
            }

            context.startActivity(intent)
        } catch (_: Exception) {
            // Fallback to regular chooser if direct WhatsApp package fails
            fallbackShare(context, file, caption)
        }
    }

    /**
     * Shares to email with default d.mohonroy@gmail.com.
     */
    fun shareViaEmail(
        context: Context,
        file: File,
        recipientEmail: String = DEFAULT_BACKUP_EMAIL,
        subject: String,
        body: String
    ) {
        try {
            val uri = getFileUri(context, file)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "message/rfc822"
                putExtra(Intent.EXTRA_EMAIL, arrayOf(recipientEmail))
                putExtra(Intent.EXTRA_SUBJECT, subject)
                putExtra(Intent.EXTRA_TEXT, body)
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "মেইলে পাঠান ($recipientEmail)"))
        } catch (e: Exception) {
            Toast.makeText(context, "মেইল অ্যাপ খুঁজে পাওয়া যায়নি: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun fallbackShare(context: Context, file: File, title: String) {
        try {
            val uri = getFileUri(context, file)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "*/*"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_TEXT, title)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "শেয়ার করুন"))
        } catch (e: Exception) {
            Toast.makeText(context, "শেয়ার করা সম্ভব হয়নি: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun openFile(context: Context, file: File) {
        try {
            val uri = getFileUri(context, file)
            val mimeType = when {
                file.name.endsWith(".pdf", ignoreCase = true) -> "application/pdf"
                file.name.endsWith(".doc", ignoreCase = true) -> "application/msword"
                file.name.endsWith(".csv", ignoreCase = true) -> "text/csv"
                else -> "*/*"
            }
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, mimeType)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            fallbackShare(context, file, file.name)
        }
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
