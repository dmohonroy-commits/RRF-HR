package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CloudFunctionsScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    val scrollState = rememberScrollState()

    val isOperating by viewModel.isCloudOperating.collectAsState()
    val operationResult by viewModel.cloudOperationResult.collectAsState()
    val isFirebaseAvailable = remember { viewModel.cloudFunctionsManager.isFirebaseAvailable() }

    var testNid by remember { mutableStateOf("") }
    var testMobile by remember { mutableStateOf("") }
    var testMessage by remember { mutableStateOf("RRF HR চুক্তিপত্র সফলভাবে প্রস্তুত হয়েছে।") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Firebase Cloud Functions",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "ক্লাউড ফাংশন ও কেন্দ্রীয় ডেটা সিঙ্ক",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "ফিরে যান"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Status Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isFirebaseAvailable) Color(0xFFF0FDF4) else Color(0xFFFFFBEB)
                ),
                border = BorderStroke(
                    1.dp,
                    if (isFirebaseAvailable) Color(0xFF86EFAC) else Color(0xFFFDE68A)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(if (isFirebaseAvailable) Color(0xFF22C55E) else Color(0xFFF59E0B)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isFirebaseAvailable) Icons.Default.CloudDone else Icons.Default.CloudQueue,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isFirebaseAvailable) "Firebase Cloud Functions সক্রিয়" else "Firebase কনফিগারেশন মোড",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = if (isFirebaseAvailable) Color(0xFF14532D) else Color(0xFF78350F)
                        )
                        Text(
                            text = if (isFirebaseAvailable) 
                                "সার্ভারের সাথে সরাসরি সংযোগের জন্য প্রস্তুত।"
                            else 
                                "অ্যাপে google-services.json ফাইল থাকলে লাইভ ক্লাউড কল হবে। ফাইল ছাড়া সেইফ ফলব্যাক সক্রিয়।",
                            fontSize = 12.sp,
                            color = if (isFirebaseAvailable) Color(0xFF166534) else Color(0xFF92400E)
                        )
                    }
                }
            }

            // Real-time Loading Indicator
            AnimatedVisibility(visible = isOperating) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            strokeWidth = 2.5.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "ক্লাউড ফাংশন প্রক্রিয়া সম্পন্ন হচ্ছে...",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            // Operation Result Output
            operationResult?.let { resultText ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (resultText.startsWith("ত্রুটি") || resultText.startsWith("ব্যাকআপ ত্রুটি"))
                            Color(0xFFFEF2F2)
                        else
                            Color(0xFFF8FAFC)
                    ),
                    border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "সার্ভার রেসপন্স আউটপুট:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Row {
                                IconButton(
                                    onClick = {
                                        clipboardManager.setText(AnnotatedString(resultText))
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        Icons.Default.ContentCopy,
                                        contentDescription = "কপি করুন",
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                IconButton(
                                    onClick = { viewModel.clearCloudOperationResult() },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "বন্ধ করুন",
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(6.dp))

                        Text(
                            text = resultText,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Action 1: Health Check
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.HealthAndSafety,
                            contentDescription = null,
                            tint = Color(0xFF0F766E),
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "১. সার্ভার হেলথ চেক (checkHealth)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Firebase Cloud Functions ব্যাকএন্ড সার্ভার চালু ও সংযোগ সক্ষম কি-না যাচাই করুন।",
                        fontSize = 12.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = { viewModel.checkCloudHealth() },
                        enabled = !isOperating,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F766E)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("সার্ভার স্ট্যাটাস চেক করুন")
                    }
                }
            }

            // Action 2: Cloud Backup
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Backup,
                            contentDescription = null,
                            tint = Color(0xFF2563EB),
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "২. এগ্রিমেন্ট ফরম ক্লাউড ব্যাকআপ (backupAgreementForms)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "বর্তমান পূরণকৃত ১০০ টাকার স্ট্যাম্প, ২৫ টাকার প্রত্যয়ন ও ৫টি এগ্রিমেন্ট ফরম ক্লাউড ফাংশনের মাধ্যমে ফায়ারবেসে ব্যাকআপ সংরক্ষণ করুন।",
                        fontSize = 12.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = { viewModel.backupCurrentWorkerToCloud() },
                        enabled = !isOperating,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("ক্লাউডে ব্যাকআপ পাঠান")
                    }
                }
            }

            // Action 3: Verify Employee Online
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.VerifiedUser,
                            contentDescription = null,
                            tint = Color(0xFF7C3AED),
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "৩. অনলাইন NID ভেরিফিকেশন (verifyEmployeeOnline)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "ক্লাউড ফাংশন দিয়ে কর্মীর জাতীয় পরিচয়পত্র ও মোবাইল নম্বর কেন্দ্রীয়ভাবে যাচাই করুন।",
                        fontSize = 12.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(10.dp))

                    OutlinedTextField(
                        value = testNid,
                        onValueChange = { testNid = it },
                        label = { Text("NID নম্বর") },
                        placeholder = { Text("যেমন: 1990123456789") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(8.dp))

                    OutlinedTextField(
                        value = testMobile,
                        onValueChange = { testMobile = it },
                        label = { Text("মোবাইল নম্বর") },
                        placeholder = { Text("যেমন: 01712345678") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(12.dp))

                    Button(
                        onClick = { viewModel.verifyEmployeeViaCloud(testNid, testMobile) },
                        enabled = !isOperating && testNid.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("NID অনলাইনে যাচাই করুন")
                    }
                }
            }

            // Action 4: Send Notification Alert
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.SendToMobile,
                            contentDescription = null,
                            tint = Color(0xFFD97706),
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "৪. নোটিফিকেশন অ্যালার্ট প্রেরণ (sendNotification)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "ক্লাউড ফাংশনের মাধ্যমে জামিনদার বা কর্মীর মোবাইলে ভেরিফিকেশন অ্যালার্ট প্রেরণ করুন।",
                        fontSize = 12.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(10.dp))

                    OutlinedTextField(
                        value = testMessage,
                        onValueChange = { testMessage = it },
                        label = { Text("বার্তার বিবরণ") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(12.dp))

                    Button(
                        onClick = { viewModel.sendNotificationViaCloud(testMobile, testMessage) },
                        enabled = !isOperating && testMobile.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("বিজ্ঞপ্তি প্রেরণ করুন")
                    }
                }
            }

            // Instructions on Firebase deployment
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "প্রজেক্টে ক্লাউড ফাংশন ব্যাকএন্ড কোড যুক্ত আছে:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color(0xFF334155)
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "• functions/index.js (ব্যাকএন্ড ফাংশনসমূহ)\n• functions/package.json (ডিপেনডেন্সি কনফিগ)\n• firebase.json (ফায়ারবেস ডিপ্লয়মেন্ট রুল)\n\nডিপ্লয় করতে টার্মিনালে রান করুন:\nfirebase deploy --only functions",
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFF475569)
                    )
                }
            }
        }
    }
}
