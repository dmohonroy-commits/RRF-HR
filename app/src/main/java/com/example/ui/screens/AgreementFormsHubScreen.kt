package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.AllAgreementForms
import com.example.data.getFormStatuses
import com.example.ui.AppScreen
import com.example.ui.MainViewModel
import com.example.ui.components.ResetAllFormsButton
import com.example.ui.components.GlassOrb3DIcon
import com.example.ui.components.GlassmorphicCard
import com.example.ui.components.PastelAmbientBackground
import com.example.util.AgreementFormsPdfGenerator
import com.example.util.ShareHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgreementFormsHubScreen(
    viewModel: MainViewModel,
    allForms: AllAgreementForms,
    onBack: () -> Unit,
    onNavigateToForm: (AppScreen) -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // Infinite transition for subtle 3D glowing pulse effect
    val infiniteTransition = rememberInfiniteTransition(label = "hub_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "এগ্রিমেন্ট ফরম ব্যবস্থাপনা",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF0F2942)
                        )
                        Text(
                            text = "অফিসিয়াল এগ্রিমেন্ট ফরমসমূহ",
                            fontSize = 12.sp,
                            color = Color(0xFF0F766E),
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                navigationIcon = {
                    GlassOrb3DIcon(
                        icon = Icons.AutoMirrored.Filled.ArrowBack,
                        size = 38.dp,
                        iconSize = 18.dp,
                        iconTint = Color(0xFF0F2942),
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .clickable(onClick = onBack)
                            .testTag("btn_back_agreement_hub")
                    )
                },
                actions = {
                    GlassOrb3DIcon(
                        icon = Icons.Default.CloudSync,
                        size = 38.dp,
                        iconSize = 18.dp,
                        iconTint = Color(0xFF0284C7),
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .clickable { onNavigateToForm(AppScreen.CLOUD_FUNCTIONS) }
                            .testTag("btn_hub_cloud_functions")
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFE0F2FE).copy(alpha = 0.85f)
                )
            )
        }
    ) { paddingValues ->
        PastelAmbientBackground(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Instructions Banner
                GlassmorphicCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerShape = RoundedCornerShape(18.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        GlassOrb3DIcon(
                            icon = Icons.Default.Info,
                            size = 40.dp,
                            iconSize = 20.dp,
                            iconTint = Color(0xFF059669)
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = "আপনার সমস্ত ফরম পূরণের পর হোম স্ক্রিনের অপশনে ক্লিক করে প্রিন্ট অথবা শেয়ার করুন।",
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F2942),
                            lineHeight = 19.sp
                        )
                    }
                }

            // Reset All Forms Button
            ResetAllFormsButton(
                viewModel = viewModel
            )

            // Cloud Functions & Backup Banner
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFFEFF6FF),
                border = BorderStroke(1.2.dp, Color(0xFFBFDBFE)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToForm(AppScreen.CLOUD_FUNCTIONS) }
                    .testTag("banner_cloud_functions")
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudSync,
                        contentDescription = null,
                        tint = Color(0xFF2563EB),
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Firebase Cloud Functions ও ব্যাকআপ",
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E3A8A)
                        )
                        Text(
                            text = "সকল ফরম কেন্দ্রীয় ক্লাউড ডেটাবেসে ব্যাকআপ ও অনলাইন ভেরিফিকেশন করুন।",
                            fontSize = 11.5.sp,
                            color = Color(0xFF3B82F6)
                        )
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = Color(0xFF2563EB),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // 1. Staff ID Card Info
            AgreementFormCard(
                serialNumber = "১",
                title = "কর্মীর আইডি কার্ডের তথ্য",
                englishTitle = "Staff Information for ID card",
                description = "শাখা, পিন, যোগদান তারিখ, রক্তের গ্রুপ, মোবাইল ও ইমেইল সহ আইডি কার্ড ফরম।",
                icon = Icons.Default.Badge,
                accentColor = Color(0xFF2563EB),
                gradientColors = listOf(Color(0xFFEFF6FF), Color(0xFFDBEAFE)),
                onEdit = { onNavigateToForm(AppScreen.FORM_ID_CARD) },
                onPreview = {
                    val pdf = AgreementFormsPdfGenerator.generateIdCardFormPdf(context, allForms.idCardForm)
                    ShareHelper.openFile(context, pdf)
                }
            )

            // 2. Training Undertaking
            AgreementFormCard(
                serialNumber = "২",
                title = "প্রশিক্ষণ তথ্য (অঙ্গীকারনামা)",
                englishTitle = "Training Undertaking Form",
                description = "প্রশিক্ষণে অংশগ্রহণ, সময়সূচী, ফি এবং শর্তাবলী সংক্রান্ত অঙ্গীকারনামা।",
                icon = Icons.Default.ModelTraining,
                accentColor = Color(0xFF0D9488),
                gradientColors = listOf(Color(0xFFF0FDFA), Color(0xFFCCFBF1)),
                onEdit = { onNavigateToForm(AppScreen.FORM_TRAINING) },
                onPreview = {
                    val pdf = AgreementFormsPdfGenerator.generateTrainingFormPdf(context, allForms.trainingForm)
                    ShareHelper.openFile(context, pdf)
                }
            )

            // 3. Relationship Declaration
            AgreementFormCard(
                serialNumber = "৩",
                title = "প্রতিষ্ঠানের পরিচিত ব্যক্তির সাথে সম্পর্ক",
                englishTitle = "Relationship Declaration Form",
                description = "আরআরএফ-এ কর্মরত আত্মীয় বা পরিচিত ব্যক্তির বিবরণ অথবা 'I have none' ঘোষণা।",
                icon = Icons.Default.PeopleAlt,
                accentColor = Color(0xFF0F3B7E),
                gradientColors = listOf(Color(0xFFF0FDF4), Color(0xFFDCFCE7)),
                onEdit = { onNavigateToForm(AppScreen.FORM_RELATIONSHIP) },
                onPreview = {
                    val pdf = AgreementFormsPdfGenerator.generateRelationshipFormPdf(context, allForms.relationshipForm)
                    ShareHelper.openFile(context, pdf)
                }
            )

            // 4. Nominee Declaration
            AgreementFormCard(
                serialNumber = "৪",
                title = "নমিনি তথ্য ফরম",
                englishTitle = "Nominee Declaration Form",
                description = "নমিনির নাম, পিতা-মাতার নাম, ঠিকানা, NID, সম্পর্ক ও অংশের হার সহ নমিনেশন ফরম।",
                icon = Icons.Default.AssignmentInd,
                accentColor = Color(0xFFEA580C),
                gradientColors = listOf(Color(0xFFFFF7ED), Color(0xFFFFEDD5)),
                onEdit = { onNavigateToForm(AppScreen.FORM_NOMINEE) },
                onPreview = {
                    val pdf = AgreementFormsPdfGenerator.generateNomineeFormPdf(context, allForms.nomineeForm)
                    ShareHelper.openFile(context, pdf)
                }
            )

            // 5. Personal Information Search
            AgreementFormCard(
                serialNumber = "৫",
                title = "কর্মীর তথ্যানুসন্ধান ফরম",
                englishTitle = "Staff Information Search Form (3-Page)",
                description = "ব্যক্তিগত, শিক্ষা, অভিজ্ঞতা, পরিবার ও জামিনদার সংক্রান্ত ৩ পাতার পূর্ণাঙ্গ ফরম।",
                icon = Icons.Default.Description,
                accentColor = Color(0xFF0F3B7E),
                gradientColors = listOf(Color(0xFFECFDF5), Color(0xFFD1FAE5)),
                onEdit = { onNavigateToForm(AppScreen.FORM_PERSONAL_INFO) },
                onPreview = {
                    val pdf = AgreementFormsPdfGenerator.generatePersonalInfoFormPdf(context, allForms.personalInfoForm)
                    ShareHelper.openFile(context, pdf)
                }
            )

            // 6. Verification Form (তথ্য যাচাই)
            AgreementFormCard(
                serialNumber = "৬",
                title = "তথ্য যাচাই ফরম",
                englishTitle = "Information Verification & Investigation Form (2-Page)",
                description = "পরিচিত/আত্মীয় (২ জন), চেয়ারম্যানের তথ্য, মুচলেকা এবং প্রতিবেশী (২ জন) সংক্রান্ত যাচাই ফরম।",
                icon = Icons.Default.VerifiedUser,
                accentColor = Color(0xFF7C3AED),
                gradientColors = listOf(Color(0xFFF5F3FF), Color(0xFFEDE9FE)),
                onEdit = { onNavigateToForm(AppScreen.FORM_VERIFICATION) },
                onPreview = {
                    val pdf = AgreementFormsPdfGenerator.generateVerificationFormPdf(context, allForms.verificationForm, allForms)
                    ShareHelper.openFile(context, pdf)
                }
            )

            Spacer(Modifier.height(20.dp))
        }
    }
}
}

@Composable
private fun AgreementFormCard(
    serialNumber: String,
    title: String,
    englishTitle: String,
    description: String,
    icon: ImageVector,
    accentColor: Color,
    gradientColors: List<Color>,
    onEdit: () -> Unit,
    onPreview: () -> Unit
) {
    GlassmorphicCard(
        modifier = Modifier.fillMaxWidth(),
        cornerShape = RoundedCornerShape(22.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                GlassOrb3DIcon(
                    icon = icon,
                    size = 52.dp,
                    iconSize = 26.dp,
                    iconTint = accentColor
                )

                Spacer(Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = accentColor,
                            modifier = Modifier.size(22.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = serialNumber,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = title,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F2942)
                        )
                    }
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = englishTitle,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = accentColor
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Text(
                text = description,
                fontSize = 13.sp,
                color = Color(0xFF334155),
                lineHeight = 18.sp
            )

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onEdit,
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                    shape = RoundedCornerShape(12.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
                    modifier = Modifier
                        .weight(1.2f)
                        .height(46.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("ফরম পূরণ", fontSize = 13.5.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onPreview,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.5.dp, accentColor),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = accentColor),
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                ) {
                    Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("প্রিভিউ", fontSize = 13.5.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

