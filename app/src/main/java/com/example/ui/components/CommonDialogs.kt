package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.AgreementEntity
import com.example.ui.theme.*
import com.example.util.PdfGenerator
import com.example.util.QrCodeGenerator
import com.example.util.ShareHelper
import com.example.util.WordDocGenerator

@Composable
fun AdminLoginDialog(
    passwordInput: String,
    onPasswordChange: (String) -> Unit,
    hasError: Boolean,
    onDismiss: () -> Unit,
    onSubmit: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Lock,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text("অ্যাডমিন লগ ইন", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column {
                Text(
                    "অ্যাডমিন প্যানেলে প্রবেশের জন্য পাসওয়ার্ড দিন:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = passwordInput,
                    onValueChange = onPasswordChange,
                    label = { Text("পাসওয়ার্ড") },
                    placeholder = { Text("পাসওয়ার্ড লিখুন") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    isError = hasError,
                    supportingText = {
                        if (hasError) {
                            Text("ভুল পাসওয়ার্ড! পুনরায় চেষ্টা করুন।", color = MaterialTheme.colorScheme.error)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_password_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onSubmit,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier.testTag("admin_login_submit_button")
            ) {
                Text("লগ ইন করুন")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("বাতিল")
            }
        }
    )
}

@Composable
fun SubmissionConfirmationDialog(
    agreement: AgreementEntity,
    onDismiss: () -> Unit,
    onViewAgreement: () -> Unit,
    onShare: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = RoundedCornerShape(30.dp),
                    color = LightGreenContainer,
                    modifier = Modifier.size(60.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = LightGreenPrimary,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
                Text(
                    if (agreement.editCount > 0) "সংশোধিত এগ্রিমেন্ট সংরক্ষিত হয়েছে!" else "সফলভাবে সংরক্ষিত হয়েছে!",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )
                if (agreement.editCount > 0) {
                    Spacer(Modifier.height(4.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFFFF3CD)
                    ) {
                        Text(
                            "সংস্করণ: ${agreement.editLabel}",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF856404),
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
                Spacer(Modifier.height(14.dp))
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("সিরিয়াল নং :", fontWeight = FontWeight.SemiBold)
                            Text(agreement.serialNo, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                        Spacer(Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            val displayFileName = "${agreement.baseFileName}.pdf"
                            Text("ফাইলের নাম :", fontWeight = FontWeight.SemiBold)
                            Text(displayFileName, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                        Spacer(Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("পদের নাম :", fontWeight = FontWeight.SemiBold)
                            Text(agreement.designation, fontWeight = FontWeight.Medium)
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    if (agreement.editCount > 0)
                        "আপনার সংশোধিত এগ্রিমেন্ট ফরমটি সফলভাবে সংরক্ষিত হয়েছে। অবিলম্বে ফাইলটি পুনরায় শেয়ার বা প্রিন্ট করতে পারেন।"
                    else
                        "আপনার এগ্রিমেন্ট ফরমটি সফলভাবে অ্যাডমিন সিস্টেমে জমা হয়েছে।",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(20.dp))
                Button(
                    onClick = {
                        onDismiss()
                        onViewAgreement()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("view_agreement_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.Description, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("এগ্রিমেন্ট দেখুন ও প্রিন্ট করুন")
                }
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = {
                        onDismiss()
                        onShare()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Share, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("হোয়াটসঅ্যাপে শেয়ার করুন")
                }
            }
        }
    }
}

@Composable
fun ShareSheetDialog(
    agreement: AgreementEntity,
    useStampMargin: Boolean,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var customPhone by remember { mutableStateOf("") }
    var customEmail by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Share, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(8.dp))
                Text("শেয়ার ও ব্যাকআপ", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFE8F5E9),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Description, contentDescription = null, tint = LightGreenDark, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "ফাইল: ${agreement.baseFileName}.pdf",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = LightGreenDark
                        )
                    }
                }

                Text(
                    "১. হোয়াটসঅ্যাপে পাঠান (WhatsApp)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = LightGreenDark
                )
                Text(
                    "নির্ধারিত মোবাইল নম্বরে এক ক্লিকে পিডিএফ পাঠান:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))

                ShareHelper.PRESET_WHATSAPP_NUMBERS.forEach { phone ->
                    OutlinedButton(
                        onClick = {
                            val pdfFile = PdfGenerator.generateAgreementPdf(context, agreement, useStampMargin)
                            ShareHelper.shareToWhatsApp(context, pdfFile, phone)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Phone, contentDescription = null, Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(phone, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.weight(1f))
                        Text("PDF", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                    }
                }

                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = customPhone,
                    onValueChange = { customPhone = it },
                    label = { Text("অন্য মোবাইল নম্বরে পাঠান") },
                    placeholder = { Text("01XXXXXXXXX") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    trailingIcon = {
                        if (customPhone.isNotBlank()) {
                            IconButton(onClick = {
                                val pdfFile = PdfGenerator.generateAgreementPdf(context, agreement, useStampMargin)
                                ShareHelper.shareToWhatsApp(context, pdfFile, customPhone)
                            }) {
                                Icon(Icons.Default.Send, contentDescription = "পাঠান", tint = LightGreenPrimary)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(Modifier.height(16.dp))

                Text(
                    "২. ওয়ার্ড ও গুগল ড্রাইভ / ইমেইল ব্যাকআপ",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(8.dp))

                Button(
                    onClick = {
                        val pdfFile = PdfGenerator.generateAgreementPdf(context, agreement, useStampMargin)
                        ShareHelper.shareViaEmail(
                            context = context,
                            file = pdfFile,
                            recipientEmail = ShareHelper.DEFAULT_BACKUP_EMAIL,
                            subject = "RRF জামানতনামা ${if (agreement.editCount > 0) "(${agreement.editLabel}) " else ""}- ${agreement.employeeName} (${agreement.serialNo})",
                            body = "সম্মানিত কর্তৃপক্ষ,\nসংযুক্ত হলো ${agreement.employeeName}-এর জামানতনামা এগ্রিমেন্ট ফাইল।"
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.Email, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("d.mohonroy@gmail.com -এ পাঠান")
                }

                Spacer(Modifier.height(6.dp))

                OutlinedButton(
                    onClick = {
                        val docFile = WordDocGenerator.generateAgreementDoc(context, agreement, useStampMargin)
                        ShareHelper.fallbackShare(context, docFile, "RRF জামানতনামা ওয়ার্ড ফাইল")
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Article, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("ওয়ার্ড (.doc) ফাইল শেয়ার করুন")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("বন্ধ করুন")
            }
        }
    )
}

@Composable
fun AppShareQrDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val downloadUrl = QrCodeGenerator.DEFAULT_DOWNLOAD_URL
    val qrBitmap = remember(downloadUrl) {
        QrCodeGenerator.generateQrBitmap(downloadUrl, 500)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.QrCode, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(8.dp))
                Text("অ্যাপ ডাউনলোড ও কিউআর কোড", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "অন্যান্য কর্মীরা এই কিউআর কোড স্ক্যান করে অ্যাপে প্রবেশ করতে পারবেন:",
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(16.dp))

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    shadowElevation = 3.dp,
                    color = androidx.compose.ui.graphics.Color.White,
                    modifier = Modifier.padding(8.dp)
                ) {
                    Image(
                        bitmap = qrBitmap.asImageBitmap(),
                        contentDescription = "App Download QR Code",
                        modifier = Modifier
                            .size(220.dp)
                            .padding(12.dp)
                    )
                }

                Spacer(Modifier.height(12.dp))
                Text(
                    "অ্যাপ লিঙ্ক (URL):",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("RRF App URL", downloadUrl))
                            Toast.makeText(context, "লিঙ্ক কপি করা হয়েছে!", Toast.LENGTH_SHORT).show()
                        }
                        .padding(2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            downloadUrl,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.weight(1f),
                            maxLines = 1
                        )
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy", Modifier.size(18.dp))
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("RRF App URL", downloadUrl))
                    Toast.makeText(context, "লিঙ্ক কপি করা হয়েছে!", Toast.LENGTH_SHORT).show()
                }
            ) {
                Icon(Icons.Default.ContentCopy, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text("লিঙ্ক কপি করুন")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("বন্ধ করুন")
            }
        }
    )
}
