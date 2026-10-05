package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.AllAgreementForms
import com.example.ui.FormState
import com.example.ui.AppScreen
import com.example.ui.MainViewModel

data class FormChainStep(
    val stepIndex: Int,          // 1 to 7
    val totalSteps: Int = 7,
    val title: String,
    val subtitle: String,
    val screen: AppScreen,
    val isCompleted: Boolean,
    val missingSummary: String
)

fun getFormChainSteps(formState: FormState, allForms: AllAgreementForms): List<FormChainStep> {
    val step1Completed = formState.employeeName.isNotBlank() && formState.guarantorName.isNotBlank() && formState.employeeDistrict.isNotBlank()
    val step1Missing = buildList {
        if (formState.employeeName.isBlank()) add("কর্মীর নাম")
        if (formState.guarantorName.isBlank()) add("জামিনদারের নাম")
        if (formState.employeeDistrict.isBlank()) add("জেলা")
    }.joinToString(", ")

    val step2Completed = allForms.idCardForm.employeeName.isNotBlank() && allForms.idCardForm.fatherName.isNotBlank() && allForms.idCardForm.bloodGroup.isNotBlank()
    val step2Missing = buildList {
        if (allForms.idCardForm.employeeName.isBlank()) add("কর্মীর নাম")
        if (allForms.idCardForm.fatherName.isBlank()) add("পিতার নাম")
        if (allForms.idCardForm.bloodGroup.isBlank()) add("রক্তের গ্রুপ")
    }.joinToString(", ")

    val step3Completed = allForms.trainingForm.employeeName.isNotBlank() && allForms.trainingForm.fatherName.isNotBlank() && allForms.trainingForm.permanentAddress.isNotBlank()
    val step3Missing = buildList {
        if (allForms.trainingForm.employeeName.isBlank()) add("কর্মীর নাম")
        if (allForms.trainingForm.fatherName.isBlank()) add("পিতার নাম")
        if (allForms.trainingForm.permanentAddress.isBlank()) add("স্থায়ী ঠিকানা")
    }.joinToString(", ")

    val step4Completed = allForms.relationshipForm.employeeName.isNotBlank() && (allForms.relationshipForm.hasNoKinship || allForms.relationshipForm.kinshipList.any { it.nameAndAddress.isNotBlank() })
    val step4Missing = buildList {
        if (allForms.relationshipForm.employeeName.isBlank()) add("কর্মীর নাম")
        if (!allForms.relationshipForm.hasNoKinship && allForms.relationshipForm.kinshipList.none { it.nameAndAddress.isNotBlank() }) add("পরিচিত ব্যক্তির তথ্য বা ঘোষণা")
    }.joinToString(", ")

    val step5Completed = allForms.nomineeForm.employeeName.isNotBlank() && allForms.nomineeForm.nominees.isNotEmpty() && allForms.nomineeForm.nominees.first().isFilled
    val step5Missing = buildList {
        if (allForms.nomineeForm.employeeName.isBlank()) add("কর্মীর নাম")
        if (allForms.nomineeForm.nominees.isEmpty() || !allForms.nomineeForm.nominees.first().isFilled) add("নমিনির বিবরণ")
    }.joinToString(", ")

    val step6Completed = allForms.personalInfoForm.employeeNameBangla.isNotBlank() && allForms.personalInfoForm.fatherName.isNotBlank() && allForms.personalInfoForm.mobileNumber.isNotBlank()
    val step6Missing = buildList {
        if (allForms.personalInfoForm.employeeNameBangla.isBlank()) add("কর্মীর পূর্ণ নাম (বাংলা)")
        if (allForms.personalInfoForm.fatherName.isBlank()) add("পিতার নাম")
        if (allForms.personalInfoForm.mobileNumber.isBlank()) add("মোবাইল নম্বর")
    }.joinToString(", ")

    val step7Completed = allForms.verificationForm.staffFullName.isNotBlank() && (allForms.verificationForm.chairmanName.isNotBlank() || allForms.verificationForm.rel1Name.isNotBlank())
    val step7Missing = buildList {
        if (allForms.verificationForm.staffFullName.isBlank()) add("মুচলেকায় কর্মীর নাম")
        if (allForms.verificationForm.chairmanName.isBlank() && allForms.verificationForm.rel1Name.isBlank()) add("চেয়ারম্যান/পরিচিতের তথ্য")
    }.joinToString(", ")

    return listOf(
        FormChainStep(1, 7, "১. আইডি কার্ডের তথ্য", "Staff Info for ID Card", AppScreen.FORM_ID_CARD, step2Completed, step2Missing),
        FormChainStep(2, 7, "২. প্রশিক্ষণ তথ্য", "Training Undertaking Form", AppScreen.FORM_TRAINING, step3Completed, step3Missing),
        FormChainStep(3, 7, "৩. পরিচিত ব্যক্তির সম্পর্ক", "Relationship Declaration", AppScreen.FORM_RELATIONSHIP, step4Completed, step4Missing),
        FormChainStep(4, 7, "৪. নমিনি তথ্য ফরম", "Nominee Declaration", AppScreen.FORM_NOMINEE, step5Completed, step5Missing),
        FormChainStep(5, 7, "৫. কর্মীর তথ্যানুসন্ধান (৩ পাতা)", "Staff Information Search", AppScreen.FORM_PERSONAL_INFO, step6Completed, step6Missing),
        FormChainStep(6, 7, "৬. তথ্য যাচাই ফরম (২ পাতা)", "Information Verification", AppScreen.FORM_VERIFICATION, step7Completed, step7Missing),
        FormChainStep(7, 7, "৭. ১০০ টাকার স্ট্যাম্প চুক্তিপত্র", "কর্মী ও জামিনদার জামানতনামা", AppScreen.WORKER_PANEL, step1Completed, step1Missing)
    )
}

/**
 * Top Stepper & Quick Form Link Banner
 * Displayed at the top of every form screen to link all forms together.
 */
@Composable
fun FormLinkHeader(
    currentScreen: AppScreen,
    viewModel: MainViewModel,
    onNavigate: (AppScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    val formState by viewModel.formState.collectAsState()
    val allAgreementForms by viewModel.allAgreementForms.collectAsState()
    val chainSteps = remember(formState, allAgreementForms) {
        getFormChainSteps(formState, allAgreementForms)
    }

    val currentStep = chainSteps.firstOrNull { it.screen == currentScreen }
        ?: FormChainStep(1, 7, "ফরম", "", currentScreen, false, "")

    val currentIndex = chainSteps.indexOfFirst { it.screen == currentScreen }
    val prevStep = if (currentIndex > 0) chainSteps[currentIndex - 1] else null
    val nextStep = if (currentIndex in 0 until chainSteps.size - 1) chainSteps[currentIndex + 1] else null

    val completedCount = chainSteps.count { it.isCompleted }
    val incompleteCount = chainSteps.size - completedCount

    var showStatusDialog by remember { mutableStateOf(false) }

    if (showStatusDialog) {
        FormStatusDialog(
            chainSteps = chainSteps,
            currentScreen = currentScreen,
            onNavigate = { screen: AppScreen ->
                showStatusDialog = false
                onNavigate(screen)
            },
            onDismiss = { showStatusDialog = false }
        )
    }

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color.White.copy(alpha = 0.08f),
        border = BorderStroke(1.2.dp, Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.5f), Color.White.copy(alpha = 0.1f)))),
        shadowElevation = 8.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.White.copy(alpha = 0.12f),
                            Color.White.copy(alpha = 0.03f)
                        )
                    )
                )
        ) {
        Column(modifier = Modifier.padding(10.dp)) {
            // Top Stepper Navigation Row
            var showFinalCompletionDialog by remember { mutableStateOf(false) }

            if (showFinalCompletionDialog) {
                AlertDialog(
                    onDismissRequest = { showFinalCompletionDialog = false },
                    icon = {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF059669), modifier = Modifier.size(44.dp))
                    },
                    title = {
                        Text("ফরম পূরণ সম্পন্ন", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF059669))
                    },
                    text = {
                        Text(
                            text = "আপনার ফরম পূরণ সম্পন্ন হয়েছে। অনুগ্রহ করে হোমপেজে গিয়ে আপনার ফরম প্রিন্ট ও শেয়ার করুন।",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                showFinalCompletionDialog = false
                                onNavigate(AppScreen.HOME)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669))
                        ) {
                            Text("হোম পেজে যান (প্রিন্ট ও শেয়ার)", fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showFinalCompletionDialog = false }) {
                            Text("বন্ধ করুন")
                        }
                    }
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Previous Form Button
                FilledTonalButton(
                    onClick = {
                        if (prevStep != null) {
                            onNavigate(prevStep.screen)
                        } else {
                            onNavigate(AppScreen.HOME)
                        }
                    },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "পূর্ববর্তী ফরম",
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = if (prevStep != null) "পূর্ববর্তী ফরম" else "হোম পেজ",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Center Current Form Badge
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                    modifier = Modifier.clickable { showStatusDialog = true }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "${currentStep.stepIndex}",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(Modifier.width(5.dp))
                        Text(
                            text = "ফরম ${currentStep.stepIndex}/৭",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(3.dp))
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "স্থিতি দেখুন",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }

                // Next Form Button
                if (nextStep != null) {
                    Button(
                        onClick = { onNavigate(nextStep.screen) },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Text(
                            text = "পরবর্তী ফরম",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "পরবর্তী ফরম",
                            modifier = Modifier.size(14.dp)
                        )
                    }
                } else {
                    Button(
                        onClick = { showFinalCompletionDialog = true },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Text(
                            text = "সমাপ্তি ➔",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // Progress Bar & Status Pill
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF8FAFC))
                    .clickable { showStatusDialog = true }
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (currentStep.isCompleted) Icons.Default.CheckCircle else Icons.Default.PendingActions,
                        contentDescription = null,
                        tint = if (currentStep.isCompleted) Color(0xFF059669) else Color(0xFFEA580C),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = if (currentStep.isCompleted) "এই ফরম সম্পন্ন ✓" else "এই ফরমটি পূরণ বাকি ⚠",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (currentStep.isCompleted) Color(0xFF059669) else Color(0xFFEA580C)
                    )
                }

                // Global summary button
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF059669).copy(alpha = 0.12f),
                    border = BorderStroke(0.8.dp, Color(0xFF059669).copy(alpha = 0.3f))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "লিঙ্ক ও স্থিতি: $completedCount/৭ সম্পন্ন",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF059669)
                        )
                        Spacer(Modifier.width(3.dp))
                        Icon(
                            imageVector = Icons.Default.Launch,
                            contentDescription = null,
                            tint = Color(0xFF059669),
                            modifier = Modifier.size(11.dp)
                        )
                    }
                }
            }
        }
    }
}
}

/**
 * Dialog showing all 7 linked forms and their completion / incomplete status
 */
@Composable
fun FormStatusDialog(
    chainSteps: List<FormChainStep>,
    currentScreen: AppScreen,
    onNavigate: (AppScreen) -> Unit,
    onDismiss: () -> Unit
) {
    val completedCount = chainSteps.count { it.isCompleted }
    val incompleteCount = chainSteps.size - completedCount

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            shadowElevation = 16.dp,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF059669).copy(alpha = 0.12f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Checklist,
                                    contentDescription = null,
                                    tint = Color(0xFF059669),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "সকল ফরমের স্থিতি ও লিঙ্ক",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "স্ট্যাম্প সহ মোট ৭টি এগ্রিমেন্ট ফরম",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "বন্ধ করুন", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Spacer(Modifier.height(12.dp))

                val isDark = isSystemInDarkTheme()

                // Summary Overview Banner
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (incompleteCount == 0) (if (isDark) Color(0xFF064E3B).copy(alpha = 0.4f) else Color(0xFFECFDF5))
                    else (if (isDark) Color(0xFF78350F).copy(alpha = 0.4f) else Color(0xFFFFFBEB)),
                    border = BorderStroke(
                        1.dp,
                        if (incompleteCount == 0) (if (isDark) Color(0xFF059669) else Color(0xFF6EE7B7))
                        else (if (isDark) Color(0xFFD97706) else Color(0xFFFCD34D))
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (incompleteCount == 0) Icons.Default.Verified else Icons.Default.WarningAmber,
                            contentDescription = null,
                            tint = if (incompleteCount == 0) Color(0xFF059669) else Color(0xFFD97706),
                            modifier = Modifier.size(26.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (incompleteCount == 0)
                                    "অভিনন্দন! সকল ফরম (৭টি) সফলভাবে পূরণ করা হয়েছে।"
                                else
                                    "মোট ৭টি ফরমের মধ্যে $completedCount টি সম্পন্ন এবং $incompleteCount টি এখনো পূরণ বাকি আছে।",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (incompleteCount == 0) (if (isDark) Color(0xFF6EE7B7) else Color(0xFF065F46)) else (if (isDark) Color(0xFFFDE68A) else Color(0xFF92400E))
                            )
                            Text(
                                text = "যেকোনো ফরমে ট্যাপ করে সরাসরি পূরণ করতে পারেন।",
                                fontSize = 10.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Form List
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    chainSteps.forEach { step ->
                        val isCurrent = step.screen == currentScreen
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isCurrent) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            else if (step.isCompleted) (if (isDark) Color(0xFF064E3B).copy(alpha = 0.3f) else Color(0xFFF0FDF4))
                            else (if (isDark) Color(0xFF450A0A).copy(alpha = 0.3f) else Color(0xFFFEF2F2)),
                            border = BorderStroke(
                                1.2.dp,
                                if (isCurrent) MaterialTheme.colorScheme.primary
                                else if (step.isCompleted) (if (isDark) Color(0xFF059669) else Color(0xFF86EFAC))
                                else (if (isDark) Color(0xFF991B1B) else Color(0xFFFECACA))
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onNavigate(step.screen) }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(12.dp)
                            ) {
                                // Step number circle
                                Surface(
                                    shape = CircleShape,
                                    color = if (step.isCompleted) Color(0xFF059669) else Color(0xFFEA580C),
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        if (step.isCompleted) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        } else {
                                            Text(
                                                text = "${step.stepIndex}",
                                                color = Color.White,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }

                                Spacer(Modifier.width(10.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = step.title,
                                            fontSize = 13.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                        )
                                        if (isCurrent) {
                                            Spacer(Modifier.width(6.dp))
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = MaterialTheme.colorScheme.primary
                                            ) {
                                                Text(
                                                    text = "বর্তমান",
                                                    fontSize = 9.sp,
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                                )
                                            }
                                        }
                                    }


                                    if (!step.isCompleted && step.missingSummary.isNotBlank()) {
                                        Text(
                                            text = "বাকি: ${step.missingSummary}",
                                            fontSize = 10.5.sp,
                                            color = if (isDark) Color(0xFFF87171) else Color(0xFFDC2626),
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }

                                Spacer(Modifier.width(6.dp))

                                // Status Badge
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (step.isCompleted) Color(0xFF059669) else Color(0xFFDC2626)
                                ) {
                                    Text(
                                        text = if (step.isCompleted) "✓ সম্পন্ন" else "⚠ অসম্পূর্ণ",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(10.dp))

                // Close button
                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                ) {
                    Text("ঠিক আছে", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

/**
 * Notification / Dialog shown when a form is saved.
 * Automatically confirms completion and offers to transition to the next linked form.
 */
@Composable
fun FormSavedAdvanceDialog(
    formTitle: String,
    nextScreenTitle: String?,
    onGoToNext: (() -> Unit)?,
    onViewPdf: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Surface(
                shape = CircleShape,
                color = Color(0xFF059669).copy(alpha = 0.15f),
                modifier = Modifier.size(54.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF059669),
                        modifier = Modifier.size(34.dp)
                    )
                }
            }
        },
        title = {
            Text(
                text = "$formTitle সংরক্ষিত হয়েছে!",
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                textAlign = TextAlign.Center
            )
        },
        text = {
            val isDark = isSystemInDarkTheme()
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "তথ্য সফলভাবে সংরক্ষিত হয়েছে এবং অন্যান্য সকল ফরমে স্বয়ংক্রিয়ভাবে লিংক করা হয়েছে।",
                    fontSize = 12.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                if (nextScreenTitle != null) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isDark) Color(0xFF064E3B).copy(alpha = 0.4f) else Color(0xFFECFDF5),
                        border = BorderStroke(1.dp, if (isDark) Color(0xFF059669) else Color(0xFFA7F3D0)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = Color(0xFF059669),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "পরবর্তী লিংকড ফরম:",
                                    fontSize = 10.sp,
                                    color = if (isDark) Color(0xFFD1FAE5) else Color(0xFF065F46)
                                )
                                Text(
                                    text = nextScreenTitle,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDark) Color(0xFF34D399) else Color(0xFF047857)
                                )
                            }
                        }
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isDark) Color(0xFF064E3B).copy(alpha = 0.4f) else Color(0xFFECFDF5),
                    border = BorderStroke(1.dp, if (isDark) Color(0xFF059669) else Color(0xFFA7F3D0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = Color(0xFF059669),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = if (nextScreenTitle != null)
                                "আপনার সমস্ত ফরম পূরণের পর হোম স্ক্রিনের অপশনে ক্লিক করে প্রিন্ট অথবা শেয়ার করুন।"
                            else
                                "আপনার ফরম পূরণ সম্পন্ন হয়েছে। অনুগ্রহ করে হোমপেজে গিয়ে আপনার ফরম প্রিন্ট ও শেয়ার করুন।",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color(0xFFD1FAE5) else Color(0xFF065F46),
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        },
        confirmButton = {
            if (onGoToNext != null) {
                Button(
                    onClick = onGoToNext,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("পরবর্তী ফরমে যান ➔", fontWeight = FontWeight.Bold)
                }
            } else {
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("হোম পেজে যান (প্রিন্ট ও শেয়ার)", fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onViewPdf,
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("প্রিভিউ")
            }
        }
    )
}

/**
 * Small Green Clear Button for each form, aligned to the right
 */
@Composable
fun ClearFormButton(
    formName: String,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showConfirmDialog by remember { mutableStateOf(false) }

    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.CenterEnd
    ) {
        OutlinedButton(
            onClick = { showConfirmDialog = true },
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF059669)),
            border = BorderStroke(1.2.dp, Color(0xFF059669)),
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
            modifier = Modifier
                .height(36.dp)
                .testTag("btn_clear_form")
        ) {
            Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFF059669))
            Spacer(Modifier.width(5.dp))
            Text("মুছুন", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF059669))
        }
    }

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            icon = {
                Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(32.dp))
            },
            title = {
                Text("ফরমের তথ্য মুছুন", fontWeight = FontWeight.Bold)
            },
            text = {
                Text("আপনি কি নিশ্চিত যে $formName-এর তথ্য মুছে ফেলতে চান? শুধু এই ফরমের তথ্য মুছে যাবে।")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmDialog = false
                        onClear()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669))
                ) {
                    Text("হ্যাঁ, মুছুন")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showConfirmDialog = false }) {
                    Text("বাতিল")
                }
            }
        )
    }
}

/**
 * Text field component that supports auto-filled, filled state and blocked/locked state
 * with an unlock toggle if needed.
 * "ফরম পূরণ করতে গেলে যদি দেখা যায় অলরেডি পূরণ করা আছে তবে সেটা সবুজ কালার থাকবে।"
 */
@Composable
fun AutoFilledBlockedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    isAutoFilled: Boolean = false,
    effectiveLocked: Boolean = false,
    placeholder: String = "",
    leadingIcon: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    keyboardOptions: androidx.compose.foundation.text.KeyboardOptions = androidx.compose.foundation.text.KeyboardOptions.Default,
    singleLine: Boolean = true
) {
    var isUnlocked by remember { mutableStateOf(false) }
    val isLocked = effectiveLocked || (isAutoFilled && !isUnlocked)
    val isDark = isSystemInDarkTheme()
    val isFilled = value.isNotBlank()

    // Green color scheme for filled or auto-filled fields
    val greenBorder = if (isDark) Color(0xFF34D399) else Color(0xFF16A34A)
    val greenContainer = if (isDark) Color(0xFF064E3B).copy(alpha = 0.35f) else Color(0xFFF0FDF4)
    val greenText = if (isDark) Color(0xFF6EE7B7) else Color(0xFF15803D)

    val redBorder = Color(0xFFDC2626)
    val redContainer = if (isDark) Color(0xFF450A0A).copy(alpha = 0.5f) else Color(0xFFFEF2F2)
    val redText = Color(0xFFDC2626)

    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = { if (!isLocked) onValueChange(it) },
            readOnly = isLocked,
            label = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = label,
                        color = if (isError) redText else if (isFilled) greenText else Color.Unspecified,
                        fontWeight = if (isError || isFilled) FontWeight.Bold else FontWeight.Normal
                    )
                    if (isError) {
                        Spacer(Modifier.width(4.dp))
                        Text("* (ফাঁকা)", color = redText, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                    } else if (isFilled) {
                        Spacer(Modifier.width(4.dp))
                        Text("✓", color = greenText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            },
            placeholder = { Text(placeholder) },
            leadingIcon = leadingIcon,
            trailingIcon = {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(end = 8.dp)) {
                    if (isError) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = "ফাঁকা ঘর",
                            tint = redBorder,
                            modifier = Modifier.padding(end = 4.dp).size(20.dp)
                        )
                    } else if (isFilled) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "পূরণকৃত",
                            tint = greenBorder,
                            modifier = Modifier.padding(end = 4.dp).size(18.dp)
                        )
                    }
                    if (isAutoFilled && !effectiveLocked) {
                        IconButton(onClick = { isUnlocked = !isUnlocked }, modifier = Modifier.size(28.dp)) {
                            Icon(
                                imageVector = if (isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                                contentDescription = if (isLocked) "লক" else "লক খোলা",
                                tint = if (isLocked) (if (isDark) Color(0xFF34D399) else Color(0xFF059669)) else (if (isDark) Color(0xFFFBBF24) else Color(0xFFD97706)),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    } else if (effectiveLocked || isLocked) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "লকড",
                            tint = if (isDark) Color(0xFF34D399) else Color(0xFF059669),
                            modifier = Modifier.padding(end = 4.dp).size(18.dp)
                        )
                    }
                }
            },
            isError = isError,
            keyboardOptions = keyboardOptions,
            singleLine = singleLine,
            textStyle = androidx.compose.ui.text.TextStyle(
                color = if (isError) redText else if (isFilled) greenText else MaterialTheme.colorScheme.onSurface,
                fontWeight = if (isError || isFilled || isAutoFilled || isLocked) FontWeight.Bold else FontWeight.Normal,
                fontSize = 15.sp
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = if (isError) redContainer else if (isFilled) greenContainer else MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = if (isError) redContainer else if (isFilled) greenContainer else MaterialTheme.colorScheme.surface,
                disabledContainerColor = if (isError) redContainer else if (isDark) Color(0xFF064E3B).copy(alpha = 0.3f) else Color(0xFFECFDF5),
                focusedBorderColor = if (isError) redBorder else if (isFilled) greenBorder else MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = if (isError) redBorder else if (isFilled) greenBorder.copy(alpha = 0.85f) else MaterialTheme.colorScheme.outline,
                errorContainerColor = redContainer,
                errorBorderColor = redBorder,
                errorLabelColor = redText,
                errorTextColor = redText,
                focusedTextColor = if (isError) redText else if (isFilled) greenText else MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = if (isError) redText else if (isFilled) greenText else MaterialTheme.colorScheme.onSurface,
                focusedLabelColor = if (isError) redText else if (isFilled) greenText else MaterialTheme.colorScheme.primary,
                unfocusedLabelColor = if (isError) redText else if (isFilled) greenText else MaterialTheme.colorScheme.onSurfaceVariant
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        )
        if (isError) {
            Text(
                text = "⚠ এই ঘরটি পূরণ করা আবশ্যক",
                color = redText,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 6.dp, top = 2.dp)
            )
        }
    }
}

/**
 * Standard Form Text Field that turns green when filled with data.
 * "ফরম পূরণ করতে গেলে যদি দেখা যায় অলরেডি পূরণ করা আছে তবে সেটা সবুজ কালার থাকবে।"
 */
@Composable
fun AppFormTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    errorMessage: String = "",
    readOnly: Boolean = false,
    keyboardOptions: androidx.compose.foundation.text.KeyboardOptions = androidx.compose.foundation.text.KeyboardOptions.Default,
    singleLine: Boolean = true,
    testTag: String = ""
) {
    val isDark = isSystemInDarkTheme()
    val isFilled = value.isNotBlank()

    val greenBorder = if (isDark) Color(0xFF34D399) else Color(0xFF16A34A)
    val greenContainer = if (isDark) Color(0xFF064E3B).copy(alpha = 0.35f) else Color(0xFFF0FDF4)
    val greenText = if (isDark) Color(0xFF6EE7B7) else Color(0xFF15803D)

    val redBorder = Color(0xFFDC2626)
    val redContainer = if (isDark) Color(0xFF450A0A).copy(alpha = 0.5f) else Color(0xFFFEF2F2)
    val redText = Color(0xFFDC2626)

    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            readOnly = readOnly,
            label = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = label,
                        color = if (isError) redText else if (isFilled) greenText else Color.Unspecified,
                        fontWeight = if (isError || isFilled) FontWeight.Bold else FontWeight.Normal
                    )
                    if (isError) {
                        Spacer(Modifier.width(4.dp))
                        Text("* (ফাঁকা)", color = redText, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                    } else if (isFilled) {
                        Spacer(Modifier.width(4.dp))
                        Text("✓", color = greenText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            },
            placeholder = { Text(placeholder) },
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon ?: if (isError) {
                {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = "ফাঁকা ঘর",
                        tint = redBorder,
                        modifier = Modifier.padding(end = 12.dp).size(20.dp)
                    )
                }
            } else if (isFilled) {
                {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "পূরণকৃত",
                        tint = greenBorder,
                        modifier = Modifier.padding(end = 12.dp).size(18.dp)
                    )
                }
            } else null,
            isError = isError,
            keyboardOptions = keyboardOptions,
            singleLine = singleLine,
            textStyle = androidx.compose.ui.text.TextStyle(
                color = if (isError) redText else if (isFilled) greenText else MaterialTheme.colorScheme.onSurface,
                fontWeight = if (isError || isFilled) FontWeight.Bold else FontWeight.Normal,
                fontSize = 15.sp
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = if (isError) redContainer else if (isFilled) greenContainer else MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = if (isError) redContainer else if (isFilled) greenContainer else MaterialTheme.colorScheme.surface,
                focusedBorderColor = if (isError) redBorder else if (isFilled) greenBorder else MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = if (isError) redBorder else if (isFilled) greenBorder.copy(alpha = 0.85f) else MaterialTheme.colorScheme.outline,
                errorContainerColor = redContainer,
                errorBorderColor = redBorder,
                errorLabelColor = redText,
                errorTextColor = redText,
                focusedTextColor = if (isError) redText else if (isFilled) greenText else MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = if (isError) redText else if (isFilled) greenText else MaterialTheme.colorScheme.onSurface,
                focusedLabelColor = if (isError) redText else if (isFilled) greenText else MaterialTheme.colorScheme.primary,
                unfocusedLabelColor = if (isError) redText else if (isFilled) greenText else MaterialTheme.colorScheme.onSurfaceVariant
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().testTag(testTag)
        )
        if (isError) {
            val displayErr = if (errorMessage.isNotBlank()) errorMessage else "এই ঘরটি পূরণ করা আবশ্যক"
            Text(
                text = "⚠ $displayErr",
                color = redText,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 6.dp, top = 2.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppFormDropdown(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    options: List<String>,
    modifier: Modifier = Modifier,
    placeholder: String = "নির্বাচন করুন",
    isError: Boolean = false,
    testTag: String = ""
) {
    var expanded by remember { mutableStateOf(false) }
    val isDark = isSystemInDarkTheme()
    val isFilled = value.isNotBlank()

    val greenBorder = if (isDark) Color(0xFF34D399) else Color(0xFF16A34A)
    val greenContainer = if (isDark) Color(0xFF064E3B).copy(alpha = 0.35f) else Color(0xFFF0FDF4)
    val greenText = if (isDark) Color(0xFF6EE7B7) else Color(0xFF15803D)

    val redBorder = Color(0xFFDC2626)
    val redContainer = if (isDark) Color(0xFF450A0A).copy(alpha = 0.5f) else Color(0xFFFEF2F2)
    val redText = Color(0xFFDC2626)

    Column(modifier = modifier) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = {},
                readOnly = true,
                label = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = label,
                            color = if (isError) redText else if (isFilled) greenText else Color.Unspecified,
                            fontWeight = if (isError || isFilled) FontWeight.Bold else FontWeight.Normal
                        )
                        if (isError) {
                            Spacer(Modifier.width(4.dp))
                            Text("* (ফাঁকা)", color = redText, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                        } else if (isFilled) {
                            Spacer(Modifier.width(4.dp))
                            Text("✓", color = greenText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                },
                placeholder = { Text(placeholder) },
                trailingIcon = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (isError) {
                            Icon(
                                imageVector = Icons.Default.Error,
                                contentDescription = "ফাঁকা ঘর",
                                tint = redBorder,
                                modifier = Modifier.padding(end = 4.dp).size(20.dp)
                            )
                        } else if (isFilled) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "পূরণকৃত",
                                tint = greenBorder,
                                modifier = Modifier.padding(end = 4.dp).size(18.dp)
                            )
                        }
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                    }
                },
                isError = isError,
                textStyle = androidx.compose.ui.text.TextStyle(
                    color = if (isError) redText else if (isFilled) greenText else MaterialTheme.colorScheme.onSurface,
                    fontWeight = if (isError || isFilled) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 15.sp
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = if (isError) redContainer else if (isFilled) greenContainer else MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = if (isError) redContainer else if (isFilled) greenContainer else MaterialTheme.colorScheme.surface,
                    focusedBorderColor = if (isError) redBorder else if (isFilled) greenBorder else MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = if (isError) redBorder else if (isFilled) greenBorder.copy(alpha = 0.85f) else MaterialTheme.colorScheme.outline,
                    errorContainerColor = redContainer,
                    errorBorderColor = redBorder,
                    errorLabelColor = redText,
                    errorTextColor = redText,
                    focusedTextColor = if (isError) redText else if (isFilled) greenText else MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = if (isError) redText else if (isFilled) greenText else MaterialTheme.colorScheme.onSurface,
                    focusedLabelColor = if (isError) redText else if (isFilled) greenText else MaterialTheme.colorScheme.primary,
                    unfocusedLabelColor = if (isError) redText else if (isFilled) greenText else MaterialTheme.colorScheme.onSurfaceVariant
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.menuAnchor().fillMaxWidth().testTag(testTag)
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEach { opt ->
                    DropdownMenuItem(
                        text = { Text(opt) },
                        onClick = {
                            onValueChange(opt)
                            expanded = false
                        }
                    )
                }
            }
        }
        if (isError) {
            Text(
                text = "⚠ এই ঘরটি নির্বাচন করা আবশ্যক",
                color = redText,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 6.dp, top = 2.dp)
            )
        }
    }
}

/**
 * Foolproof Date Field with Calendar Picker dialog and 4-digit Year validation.
 * "তারিখ লেখার ছক এমন হবে যাতে ভুল করতে পারবে না। সন সাল ছাড়া গ্রহণ করবে না।"
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDateField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "DD/MM/YYYY (যেমন: ১৫/০৮/১৯৯৫)",
    isError: Boolean = false,
    errorMessage: String = "",
    requireYear: Boolean = true,
    testTag: String = ""
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val isDark = isSystemInDarkTheme()
    val isFilled = value.isNotBlank()
    val isValidDate = com.example.util.BanglaTextValidator.isValidDateWith4DigitYear(value)
    val hasError = isError || (requireYear && isFilled && !isValidDate)

    val greenBorder = if (isDark) Color(0xFF34D399) else Color(0xFF16A34A)
    val greenContainer = if (isDark) Color(0xFF064E3B).copy(alpha = 0.3f) else Color(0xFFF0FDF4)
    val greenText = if (isDark) Color(0xFF6EE7B7) else Color(0xFF15803D)

    fun openDatePicker() {
        val cal = java.util.Calendar.getInstance()
        if (value.isNotBlank()) {
            val eng = com.example.util.BanglaTextValidator.toEnglishDigits(value)
            val parts = eng.split('/', '-', '.')
            if (parts.size == 3) {
                val d = parts[0].toIntOrNull() ?: cal.get(java.util.Calendar.DAY_OF_MONTH)
                val m = (parts[1].toIntOrNull() ?: (cal.get(java.util.Calendar.MONTH) + 1)) - 1
                val y = parts[2].toIntOrNull() ?: cal.get(java.util.Calendar.YEAR)
                if (y in 1900..2100 && m in 0..11 && d in 1..31) {
                    cal.set(y, m, d)
                }
            }
        }
        val datePickerDialog = android.app.DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val formattedD = String.format("%02d", dayOfMonth)
                val formattedM = String.format("%02d", month + 1)
                val formattedY = String.format("%04d", year)
                val dateStr = "$formattedD/$formattedM/$formattedY"
                onValueChange(com.example.util.BanglaTextValidator.toBanglaDigits(dateStr))
            },
            cal.get(java.util.Calendar.YEAR),
            cal.get(java.util.Calendar.MONTH),
            cal.get(java.util.Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = { input ->
                val formatted = com.example.util.BanglaTextValidator.formatDateInputStrict(input)
                onValueChange(formatted)
            },
            label = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = label,
                        color = if (isFilled && !hasError) greenText else Color.Unspecified,
                        fontWeight = if (isFilled) FontWeight.SemiBold else FontWeight.Normal
                    )
                    if (isFilled && !hasError) {
                        Spacer(Modifier.width(4.dp))
                        Text("✓", color = greenText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            },
            placeholder = { Text(placeholder) },
            leadingIcon = {
                IconButton(onClick = { openDatePicker() }) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = "ক্যালেন্ডার থেকে তারিখ নির্বাচন করুন",
                        tint = if (isFilled && !hasError) greenBorder else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            },
            trailingIcon = {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(end = 4.dp)) {
                    if (isFilled && !hasError) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "সঠিক তারিখ",
                            tint = greenBorder,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(onClick = { openDatePicker() }) {
                        Icon(
                            imageVector = Icons.Default.EditCalendar,
                            contentDescription = "ক্যালেন্ডার",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            },
            isError = hasError,
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
            ),
            singleLine = true,
            textStyle = androidx.compose.ui.text.TextStyle(
                color = if (isFilled && !hasError) greenText else MaterialTheme.colorScheme.onSurface,
                fontWeight = if (isFilled) FontWeight.Bold else FontWeight.Normal,
                fontSize = 15.sp
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = if (isFilled && !hasError) greenContainer else MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = if (isFilled && !hasError) greenContainer else MaterialTheme.colorScheme.surface,
                focusedBorderColor = if (isFilled && !hasError) greenBorder else MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = if (isFilled && !hasError) greenBorder.copy(alpha = 0.85f) else MaterialTheme.colorScheme.outline,
                focusedTextColor = if (isFilled && !hasError) greenText else MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = if (isFilled && !hasError) greenText else MaterialTheme.colorScheme.onSurface,
                focusedLabelColor = if (isFilled && !hasError) greenText else MaterialTheme.colorScheme.primary,
                unfocusedLabelColor = if (isFilled && !hasError) greenText else MaterialTheme.colorScheme.onSurfaceVariant
            ),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth().testTag(testTag)
        )
        if (requireYear && isFilled && !isValidDate) {
            Text(
                text = "সঠিক সন/সাল সহ তারিখ দিন (দিন/মাস/পূর্ণাঙ্গ সাল যেমন: ১৫/০৮/১৯৯৫)",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                fontSize = 11.sp,
                modifier = Modifier.padding(start = 6.dp, top = 2.dp)
            )
        }
    }
}

/**
 * Prominent button on the first form to reset all 7 forms and all saved PDFs
 */
@Composable
fun ResetAllFormsButton(
    viewModel: MainViewModel,
    onResetComplete: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val isDark = isSystemInDarkTheme()
    var showConfirmDialog by remember { mutableStateOf(false) }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isDark) Color(0xFF450A0A).copy(alpha = 0.4f) else Color(0xFFFEF2F2),
        border = BorderStroke(1.5.dp, if (isDark) Color(0xFF991B1B) else Color(0xFFFCA5A5)),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.RestartAlt,
                    contentDescription = null,
                    tint = if (isDark) Color(0xFFF87171) else Color(0xFFDC2626),
                    modifier = Modifier.size(22.dp)
                )
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(
                        text = "সব তথ্য রিসেট করুন",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color(0xFFFCA5A5) else Color(0xFF991B1B)
                    )
                    Text(
                        text = "সব ফরমের ফিল্ড ও পিডিএফ মুছে নতুন করে শুরু করুন",
                        fontSize = 11.5.sp,
                        color = if (isDark) Color(0xFFFECACA) else Color(0xFFB91C1C)
                    )
                }
            }
            Button(
                onClick = { showConfirmDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.testTag("btn_reset_all_forms")
            ) {
                Icon(Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("রিসেট", fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
            }
        }
    }

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            icon = {
                Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(36.dp))
            },
            title = {
                Text("সকল তথ্য রিসেট করুন?", fontWeight = FontWeight.Bold, color = if (isDark) Color(0xFFF87171) else Color(0xFF991B1B))
            },
            text = {
                Text(
                    "আপনি কি নিশ্চিত যে ৭টি সকল ফরমের পূরণকৃত তথ্য এবং জেনারেটকৃত পিডিএফসমূহ মুছে ফেলতে চান?\n\nরিসেট করার পর নতুন তথ্য পূরণ করা হলে সেই অনুযায়ী নতুন পিডিএফ তৈরি হবে।",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 13.5.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmDialog = false
                        viewModel.resetAllFormsAndPdfs()
                        onResetComplete()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Text("হ্যাঁ, সব রিসেট করুন")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showConfirmDialog = false }) {
                    Text("বাতিল")
                }
            }
        )
    }
}

/**
 * 3D Card with Gradient Header and Circular Badge matching the Stamp Form theme.
 * Dark Mode safe with dynamic surface colors and contrasting text.
 */
@Composable
fun StampThemed3DCard(
    badgeText: String,
    title: String,
    subtitle: String = "",
    tag: String = "আবশ্যক",
    tagColor: Color = Color.White.copy(alpha = 0.2f),
    tagTextColor: Color = Color.White,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val isDark = isSystemInDarkTheme()
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        border = BorderStroke(1.2.dp, if (isDark) Color(0xFF334155) else Color(0xFFD6E2F0)),
        modifier = modifier.fillMaxWidth()
    ) {
        // 3D Header Banner with Gradient & Badge
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF0D3268),
                            Color(0xFF1553A3),
                            Color(0xFF2278DB)
                        )
                    )
                )
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color.White,
                    border = BorderStroke(2.dp, Color(0xFFB9D7F9)),
                    shadowElevation = 3.dp,
                    modifier = Modifier.size(34.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = badgeText,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            color = Color(0xFF0D3268)
                        )
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        ),
                        color = Color.White
                    )
                    if (subtitle.isNotBlank()) {
                        Text(
                            text = subtitle,
                            fontSize = 11.5.sp,
                            color = Color(0xFFD4E5FB)
                        )
                    }
                }
                if (tag.isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = tagColor,
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.35f))
                    ) {
                        Text(
                            text = tag,
                            color = tagTextColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            content()
        }
    }
}

/**
 * Pulsing Save Button that animates (scales up and down) when mandatory info is filled
 * and hides keyboard when ready.
 * "শেষ ছক পূরণ হলে অথবা আবশ্যকিয় তথ্য পূরণ হয়ে গেলে ফোনের কি-প্যাড সরে যাবে এবং সেভ বাটন বড় ছোট অ্যানিমেশন হবে"
 */
@Composable
fun PulsingSaveButton(
    onClick: () -> Unit,
    text: String = "সংরক্ষণ করুন (Save)",
    isReady: Boolean = true,
    modifier: Modifier = Modifier,
    testTag: String = "btn_save_pulsing"
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    LaunchedEffect(isReady) {
        if (isReady) {
            keyboardController?.hide()
            focusManager.clearFocus()
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "save_pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (isReady) 1.06f else 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isReady) Color(0xFF059669) else Color(0xFF64748B)
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .graphicsLayer(
                scaleX = if (isReady) scale else 1f,
                scaleY = if (isReady) scale else 1f
            )
            .testTag(testTag)
    ) {
        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(20.dp), tint = Color.White)
        Spacer(Modifier.width(8.dp))
        Text(text, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
    }
}

/**
 * Floating Save Button on the right side, slightly below middle,
 * stays fixed while scrolling.
 * "ফরমের যতটা সম্ভব ডান পাশে মাঝখানের একটু নিচে এমন সেভ বাটন যুক্ত করতে হবে যেটা স্ক্রল করলেও একই যাইগায় থাকবে"
 */
@Composable
fun FloatingSaveFab(
    onClick: () -> Unit,
    text: String = "সেভ",
    modifier: Modifier = Modifier,
    testTag: String = "fab_floating_save"
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    Surface(
        onClick = {
            keyboardController?.hide()
            focusManager.clearFocus()
            onClick()
        },
        shape = RoundedCornerShape(percent = 50),
        color = Color(0xFF2DD4BF), // Teal / cyan glass style matching user screenshot
        contentColor = Color(0xFF0F172A),
        shadowElevation = 8.dp,
        border = BorderStroke(1.5.dp, Color.White.copy(alpha = 0.8f)),
        modifier = modifier
            .size(width = 72.dp, height = 44.dp)
            .testTag(testTag)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(horizontal = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Save,
                    contentDescription = null,
                    modifier = Modifier.size(15.dp),
                    tint = Color(0xFF0F172A)
                )
                Spacer(Modifier.width(3.dp))
                Text(
                    text = text,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Color(0xFF0F172A)
                )
            }
        }
    }
}

@Composable
fun DateBoxesGridComponent(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    isEnglish: Boolean = false,
    isError: Boolean = false,
    errorMessage: String = "",
    testTag: String = "date_boxes_grid"
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val isDark = isSystemInDarkTheme()
    val digitsClean = if (isEnglish) {
        com.example.util.BanglaTextValidator.toEnglishDigits(com.example.util.BanglaTextValidator.filterDigitsOnly(value))
    } else {
        com.example.util.BanglaTextValidator.toBanglaDigits(com.example.util.BanglaTextValidator.filterDigitsOnly(value))
    }
    val paddedDigits = digitsClean.padEnd(8, ' ').take(8)

    val boxBorder = if (isError) MaterialTheme.colorScheme.error else if (isDark) Color(0xFF0D9488) else Color(0xFF0F766E)
    val boxBg = if (isDark) Color(0xFF134E4A).copy(alpha = 0.4f) else Color(0xFFF0FDFA)
    val boxTextColor = if (isDark) Color(0xFF5EEAD4) else Color(0xFF0F766E)

    fun openDatePicker() {
        val cal = java.util.Calendar.getInstance()
        if (value.isNotBlank()) {
            val eng = com.example.util.BanglaTextValidator.toEnglishDigits(value)
            val parts = eng.split('/', '-', '.')
            if (parts.size == 3) {
                val d = parts[0].toIntOrNull() ?: cal.get(java.util.Calendar.DAY_OF_MONTH)
                val m = (parts[1].toIntOrNull() ?: (cal.get(java.util.Calendar.MONTH) + 1)) - 1
                val y = parts[2].toIntOrNull() ?: cal.get(java.util.Calendar.YEAR)
                if (y in 1900..2100 && m in 0..11 && d in 1..31) {
                    cal.set(y, m, d)
                }
            }
        }
        val datePickerDialog = android.app.DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val formattedD = String.format("%02d", dayOfMonth)
                val formattedM = String.format("%02d", month + 1)
                val formattedY = String.format("%04d", year)
                val dateStr = "$formattedD/$formattedM/$formattedY"
                val res = if (isEnglish) com.example.util.BanglaTextValidator.toEnglishDigits(dateStr) else com.example.util.BanglaTextValidator.toBanglaDigits(dateStr)
                onValueChange(res)
            },
            cal.get(java.util.Calendar.YEAR),
            cal.get(java.util.Calendar.MONTH),
            cal.get(java.util.Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = label,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Bold,
                color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.weight(1f))
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = if (isDark) Color(0xFF0F766E).copy(alpha = 0.4f) else Color(0xFFCCFBF1),
                border = BorderStroke(0.8.dp, boxBorder),
                modifier = Modifier.clickable { openDatePicker() }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = "তারিখ নির্বাচন",
                        tint = boxBorder,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = if (isEnglish) "Pick Date" else "তারিখ পরিবর্তন",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = boxBorder
                    )
                }
            }
        }

        // 8-cell date box grid: [D][D] - [M][M] - [Y][Y][Y][Y]
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { openDatePicker() }
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Day 2 boxes
            for (i in 0..1) {
                val ch = paddedDigits.getOrNull(i)?.toString()?.trim().orEmpty()
                DateDigitBox(char = ch, bg = boxBg, border = boxBorder, textColor = boxTextColor)
                Spacer(Modifier.width(4.dp))
            }
            Text("/", fontWeight = FontWeight.Bold, color = boxBorder, modifier = Modifier.padding(horizontal = 2.dp))
            // Month 2 boxes
            for (i in 2..3) {
                val ch = paddedDigits.getOrNull(i)?.toString()?.trim().orEmpty()
                DateDigitBox(char = ch, bg = boxBg, border = boxBorder, textColor = boxTextColor)
                Spacer(Modifier.width(4.dp))
            }
            Text("/", fontWeight = FontWeight.Bold, color = boxBorder, modifier = Modifier.padding(horizontal = 2.dp))
            // Year 4 boxes
            for (i in 4..7) {
                val ch = paddedDigits.getOrNull(i)?.toString()?.trim().orEmpty()
                DateDigitBox(char = ch, bg = boxBg, border = boxBorder, textColor = boxTextColor)
                if (i < 7) Spacer(Modifier.width(4.dp))
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = if (isEnglish) "Day (DD) / Month (MM) / Year (YYYY)" else "দিন (DD) / মাস (MM) / বছর (YYYY)",
                fontSize = 10.5.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (isError && errorMessage.isNotBlank()) {
            Text(
                text = errorMessage,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
}

@Composable
private fun DateDigitBox(
    char: String,
    bg: Color,
    border: Color,
    textColor: Color
) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = bg,
        border = BorderStroke(1.2.dp, border),
        modifier = Modifier.size(width = 32.dp, height = 38.dp)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Text(
                text = char.ifBlank { "·" },
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (char.isBlank()) border.copy(alpha = 0.5f) else textColor
            )
        }
    }
}

/**
 * Scrollable Date of Birth Selector (Day 1-31, Month 1-12, Year 1950-2015)
 * Allows scrolling and picking Day, Month, and Year directly.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BanglaScrollableDobPicker(
    value: String,
    onValueChange: (String) -> Unit,
    label: String = "কর্মীর জন্ম তারিখ *",
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    errorMessage: String = ""
) {
    val isDark = isSystemInDarkTheme()

    // Parse existing value if available (DD/MM/YYYY or DD-MM-YYYY)
    val englishDob = com.example.util.BanglaTextValidator.toEnglishDigits(value).trim()
    val parts = englishDob.split('/', '-', '.')
    val currentDay = if (parts.size >= 1 && parts[0].isNotBlank()) parts[0].padStart(2, '0') else ""
    val currentMonth = if (parts.size >= 2 && parts[1].isNotBlank()) parts[1].padStart(2, '0') else ""
    val currentYear = if (parts.size >= 3 && parts[2].isNotBlank()) parts[2] else ""

    // Options for Day (1 to 31 in Bangla digits)
    val daysList = (1..31).map { d ->
        val str = d.toString().padStart(2, '0')
        val banglaStr = com.example.util.BanglaTextValidator.toBanglaDigits(str)
        str to banglaStr
    }

    // Options for Month (1 to 12 with Bengali names)
    val monthNames = listOf(
        "জানুয়ারি", "ফেব্রুয়ারি", "মার্চ", "এপ্রিল", "মে", "জুন",
        "জুলাই", "আগস্ট", "সেপ্টেম্বর", "অক্টোবর", "নভেম্বর", "ডিসেম্বর"
    )
    val monthsList = (1..12).map { m ->
        val str = m.toString().padStart(2, '0')
        val banglaStr = com.example.util.BanglaTextValidator.toBanglaDigits(str)
        val name = monthNames[m - 1]
        str to "$banglaStr ($name)"
    }

    // Options for Year (2020 down to 1950 in Bangla digits)
    val yearsList = (2020 downTo 1950).map { y ->
        val str = y.toString()
        val banglaStr = com.example.util.BanglaTextValidator.toBanglaDigits(str)
        str to banglaStr
    }

    fun updateDob(newDay: String, newMonth: String, newYear: String) {
        val d = newDay.ifBlank { "01" }
        val m = newMonth.ifBlank { "01" }
        val y = newYear.ifBlank { "1998" }
        val banglaDate = "${com.example.util.BanglaTextValidator.toBanglaDigits(d)}/${com.example.util.BanglaTextValidator.toBanglaDigits(m)}/${com.example.util.BanglaTextValidator.toBanglaDigits(y)}"
        onValueChange(banglaDate)
    }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                shape = CircleShape,
                color = if (isDark) Color(0xFF0284C7).copy(alpha = 0.4f) else Color(0xFFE0F2FE),
                modifier = Modifier.size(24.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7),
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
            Spacer(Modifier.width(8.dp))
            Text(
                text = label,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Bold,
                color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
            )
            if (value.isNotBlank()) {
                Spacer(Modifier.width(6.dp))
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (isDark) Color(0xFF064E3B) else Color(0xFFDCFCE7)
                ) {
                    Text(
                        text = value,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color(0xFF34D399) else Color(0xFF15803D),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }

        // 3 Selectors: দিন (১-৩১), মাস (১-১২), সাল (১৯৫০-২০১৫)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 1. দিন (Day: 1 to 31)
            var expandedDay by remember { mutableStateOf(false) }
            val selectedDayDisplay = daysList.firstOrNull { it.first == currentDay }?.second ?: if (currentDay.isNotBlank()) com.example.util.BanglaTextValidator.toBanglaDigits(currentDay) else "দিন"

            ExposedDropdownMenuBox(
                expanded = expandedDay,
                onExpandedChange = { expandedDay = it },
                modifier = Modifier.weight(0.9f)
            ) {
                OutlinedTextField(
                    value = selectedDayDisplay,
                    onValueChange = {},
                    readOnly = true,
                    textStyle = TextStyle(fontSize = 11.5.sp),
                    label = { Text("দিন", fontSize = 10.sp) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDay) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF0284C7),
                        unfocusedBorderColor = if (isDark) Color(0xFF475569) else Color(0xFFCBD5E1)
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expandedDay,
                    onDismissRequest = { expandedDay = false },
                    modifier = Modifier.heightIn(max = 240.dp)
                ) {
                    daysList.forEach { (engD, bngD) ->
                        DropdownMenuItem(
                            text = { Text(bngD, fontSize = 13.sp, fontWeight = if (engD == currentDay) FontWeight.Bold else FontWeight.Normal) },
                            onClick = {
                                updateDob(engD, currentMonth, currentYear)
                                expandedDay = false
                            },
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // 2. মাস (Month: 1 to 12)
            var expandedMonth by remember { mutableStateOf(false) }
            val selectedMonthDisplay = monthsList.firstOrNull { it.first == currentMonth }?.second ?: if (currentMonth.isNotBlank()) com.example.util.BanglaTextValidator.toBanglaDigits(currentMonth) else "মাস"

            ExposedDropdownMenuBox(
                expanded = expandedMonth,
                onExpandedChange = { expandedMonth = it },
                modifier = Modifier.weight(1.5f)
            ) {
                OutlinedTextField(
                    value = selectedMonthDisplay,
                    onValueChange = {},
                    readOnly = true,
                    textStyle = TextStyle(fontSize = 11.5.sp),
                    label = { Text("মাস", fontSize = 10.sp) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedMonth) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF0284C7),
                        unfocusedBorderColor = if (isDark) Color(0xFF475569) else Color(0xFFCBD5E1)
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expandedMonth,
                    onDismissRequest = { expandedMonth = false },
                    modifier = Modifier.heightIn(max = 240.dp)
                ) {
                    monthsList.forEach { (engM, bngM) ->
                        DropdownMenuItem(
                            text = { Text(bngM, fontSize = 12.5.sp, fontWeight = if (engM == currentMonth) FontWeight.Bold else FontWeight.Normal) },
                            onClick = {
                                updateDob(currentDay, engM, currentYear)
                                expandedMonth = false
                            },
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // 3. সাল (Year: 2015 down to 1950)
            var expandedYear by remember { mutableStateOf(false) }
            val selectedYearDisplay = yearsList.firstOrNull { it.first == currentYear }?.second ?: if (currentYear.isNotBlank()) com.example.util.BanglaTextValidator.toBanglaDigits(currentYear) else "সাল"

            ExposedDropdownMenuBox(
                expanded = expandedYear,
                onExpandedChange = { expandedYear = it },
                modifier = Modifier.weight(1.1f)
            ) {
                OutlinedTextField(
                    value = selectedYearDisplay,
                    onValueChange = {},
                    readOnly = true,
                    textStyle = TextStyle(fontSize = 11.5.sp),
                    label = { Text("সাল", fontSize = 10.sp) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedYear) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF0284C7),
                        unfocusedBorderColor = if (isDark) Color(0xFF475569) else Color(0xFFCBD5E1)
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expandedYear,
                    onDismissRequest = { expandedYear = false },
                    modifier = Modifier.heightIn(max = 240.dp)
                ) {
                    yearsList.forEach { (engY, bngY) ->
                        DropdownMenuItem(
                            text = { Text(bngY, fontSize = 13.sp, fontWeight = if (engY == currentYear) FontWeight.Bold else FontWeight.Normal) },
                            onClick = {
                                updateDob(currentDay, currentMonth, engY)
                                expandedYear = false
                            },
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        if (isError && errorMessage.isNotBlank()) {
            Text(
                text = errorMessage,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
}
