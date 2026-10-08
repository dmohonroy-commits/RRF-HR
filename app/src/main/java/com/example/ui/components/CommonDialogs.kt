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
import androidx.compose.foundation.shape.CircleShape
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
import com.example.util.AgreementConditionsPdfGenerator
import com.example.util.HtmlExporter
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
    onShare: () -> Unit,
    onGoToHome: () -> Unit
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
                    text = "আপনার ফরম পূরণ সম্পন্ন হয়েছে। অনুগ্রহ করে হোমপেজে গিয়ে আপনার ফরম প্রিন্ট ও শেয়ার করুন।",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF047857),
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = {
                        onDismiss()
                        onGoToHome()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Home, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("হোম পেজে যান (প্রিন্ট ও শেয়ার)", fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = {
                        onDismiss()
                        onViewAgreement()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("view_agreement_btn"),
                    shape = RoundedCornerShape(10.dp)
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
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("শেয়ার করুন")
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
                    "WhatsApp গ্রুপ বা যেকোনো চ্যাটে এক ক্লিকে পিডিএফ পাঠান:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))

                Button(
                    onClick = {
                        val pdfFile = PdfGenerator.generateAgreementPdf(context, agreement, useStampMargin)
                        val caption = "রুরাল রিকনস্ট্রাকশন ফাউন্ডেশন (আরআরএফ) - কর্মী জামানতনামা ও চুক্তিপত্র\nক্রমিক নং: ${agreement.serialNo}\nকর্মী: ${agreement.employeeName} (${agreement.designation})\nজামিনদার: ${agreement.guarantorName}\nতারিখ: ${agreement.submissionDate}"
                        ShareHelper.shareToWhatsApp(context, pdfFile, null, caption)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreen)
                ) {
                    Icon(Icons.Default.Send, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("WhatsApp গ্রুপ বা যেকোনো চ্যাটে পাঠান", fontWeight = FontWeight.Bold, color = Color.White)
                }

                Spacer(Modifier.height(6.dp))
                Text(
                    "অথবা নির্দিষ্ট মোবাইল নম্বরে পাঠান:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(6.dp))

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

                Spacer(Modifier.height(6.dp))

                OutlinedButton(
                    onClick = {
                        val htmlFile = HtmlExporter.exportAgreementToHtml(context, agreement)
                        ShareHelper.openFile(context, htmlFile)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Language, contentDescription = null, tint = Color(0xFF059669))
                    Spacer(Modifier.width(8.dp))
                    Text("সম্পূর্ণ HTML প্যাকেজ (ফোন ও ল্যাপটপে দেখুন)", color = Color(0xFF059669), fontWeight = FontWeight.Bold)
                }

                Spacer(Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val stamp25Html = HtmlExporter.exportStamp25ToHtml(context, agreement)
                            ShareHelper.openFile(context, stamp25Html)
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("২৫টাকা HTML", fontSize = 11.5.sp, color = Color(0xFF1E3A8A))
                    }

                    OutlinedButton(
                        onClick = {
                            val verifHtml = HtmlExporter.exportVerificationToHtml(context, agreement)
                            ShareHelper.openFile(context, verifHtml)
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("তথ্য যাচাই HTML", fontSize = 11.5.sp, color = Color(0xFF059669))
                    }
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

@Composable
fun StampInfoDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                modifier = Modifier.size(50.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Print,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        },
        title = {
            Text(
                text = "১০০ টাকার স্ট্যাম্প নির্দেশিকা",
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                fontSize = 18.sp
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "১. নন-জুডিশিয়াল স্ট্যাম্পের ধরন:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "কর্মী জামানতনামা ও চুক্তিপত্রটি ৩টি ১০০ টাকার নন-জুডিশিয়াল স্ট্যাম্প অথবা ৩০০ টাকার জুডিশিয়াল স্ট্যাম্পে প্রিন্ট করা যাবে।",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )

                Text(
                    text = "২. মার্জিন ও স্পেসিং নির্দেশনা:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "প্রথম পাতার উপরের অংশে ৪.০ ইঞ্চি মার্জিন স্বয়ংক্রিয়ভাবে সংরক্ষিত রয়েছে যাতে স্ট্যাম্পের সরকারী সিল ও মনোগ্রামের নিচে চুক্তিপত্রের লেখা নিখুঁতভাবে বসে।",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )

                Text(
                    text = "৩. কাগজের মাপ:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "প্রিন্ট করার সময় লিগাল সাইজ (Legal Paper: ৮.৫ × ১৪ ইঞ্চি) নির্বাচন করুন এবং স্কেলিং ১০০% (Default) রাখুন।",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("ঠিক আছে")
            }
        }
    )
}

@Composable
fun OrgInfoDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Surface(
                shape = CircleShape,
                color = Color(0xFF0D9488).copy(alpha = 0.1f),
                modifier = Modifier.size(50.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Apartment,
                        contentDescription = null,
                        tint = Color(0xFF0D9488),
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        },
        title = {
            Text(
                text = "সংস্থা পরিচিতি ও কার্যালয়",
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                fontSize = 18.sp
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "রুরাল রিকনস্ট্রাকশন ফাউন্ডেশন (RRF)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "রুরাল রিকনস্ট্রাকশন ফাউন্ডেশন (আরআরএফ) ১৯৮২ সালে প্রতিষ্ঠিত একটি জাতীয় পর্যায়ের শীর্ষস্থানীয় বেসরকারী উন্নয়ন সংস্থা। সংস্থাটি বাংলাদেশের তৃণমূল জনগোষ্ঠীর অর্থনৈতিক ও সামাজিক ক্ষমতায়নে নিরলস কাজ করে যাচ্ছে।",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "প্রধান কার্যালয়:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color(0xFF0D9488)
                )
                Text(
                    text = "আরআরএফ ভবন, সিএন্ডবি রোড, কারবালা, যশোর-৭৪০০",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D9488))
            ) {
                Text("ঠিক আছে")
            }
        }
    )
}

@Composable
fun TermsInfoDialog(
    onDismiss: () -> Unit,
    onViewFullScreen: (() -> Unit)? = null
) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Surface(
                shape = CircleShape,
                color = Color(0xFF059669).copy(alpha = 0.1f),
                modifier = Modifier.size(50.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Gavel,
                        contentDescription = null,
                        tint = Color(0xFF059669),
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        },
        title = {
            Text(
                text = "নিয়োগ প্রাপ্তির শর্তাবলী",
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                fontSize = 18.sp
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "রুরাল রিকনস্ট্রাকশন ফাউন্ডেশন",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "১. জামিনদারের সাথে ৩০০ টাকার (১০০ টাকার ৩টি) নন-জুডিশিয়াল স্ট্যাম্পে চুক্তি সম্পন্ন করতে হবে。\n২. নির্ধারিত হারে ফেরতযোগ্য জামানত জমা দিতে হবে (পদবী অনুযায়ী ৫,০০০/- থেকে ৪০,০০০/- টাকা)।\n৩. স্থানীয় ২ জন বিশিষ্ট ব্যক্তির প্রত্যয়নপত্র (২৫ টাকার স্ট্যাম্পে) জমা দিতে হবে。\n৪. নমিনির ১ কপি ছবি ও এনআইডি জমা দিতে হবে。\n৫. শিক্ষাগত যোগ্যতার মূল সনদ চাকুরীকালীন সময়ে জমা রাখতে হবে।",
                    fontSize = 12.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onDismiss()
                    if (onViewFullScreen != null) {
                        onViewFullScreen()
                    } else {
                        val pdf = AgreementConditionsPdfGenerator.generateConditionsPdf(context)
                        ShareHelper.openFile(context, pdf)
                    }
                },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669))
            ) {
                Text("সম্পূর্ণ শর্তাবলী ও PDF")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("বন্ধ করুন")
            }
        }
    )
}

@Composable
fun ContactInfoDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Surface(
                shape = CircleShape,
                color = Color(0xFFD97706).copy(alpha = 0.1f),
                modifier = Modifier.size(50.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = null,
                        tint = Color(0xFFD97706),
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        },
        title = {
            Text(
                text = "যোগাযোগ ও হেল্পলাইন",
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                fontSize = 18.sp
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "আরআরএফ মানবসম্পদ বিভাগ (HR Division):",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "• প্রধান কার্যালয়: আরআরএফ ভবন, সিএন্ডবি রোড, কারবালা, যশোর-৭৪০০\n• ফোন: ০২৪৭৭৭৬৩৮০১, ০২৪৭৭৭৬৩৮০২, ০২৪৭৭৭৬৩৮০৩\n• ফ্যাক্স: ০২৪৭৭৭৬৩৮০৪\n• ইমেইল: info@rrf-bd.org / hr@rrf-bd.org\n• ওয়েবসাইট: www.rrf-bd.org",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 20.sp
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706))
            ) {
                Text("ঠিক আছে")
            }
        }
    )
}

