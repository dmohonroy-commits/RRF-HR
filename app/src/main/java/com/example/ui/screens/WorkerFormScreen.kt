package com.example.ui.screens

import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.data.AgreementEntity
import com.example.ui.AppScreen
import com.example.ui.FormState
import com.example.ui.MainViewModel
import com.example.ui.components.FormLinkHeader
import com.example.ui.components.PulsingSaveButton
import com.example.ui.components.ResetAllFormsButton
import com.example.ui.components.ShareSheetDialog
import com.example.ui.theme.*
import com.example.util.BanglaAddressHelper
import com.example.util.PdfGenerator
import com.example.util.ShareHelper
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkerFormScreen(
    viewModel: MainViewModel,
    formState: FormState,
    localAgreement: AgreementEntity?,
    onOpenMenu: () -> Unit,
    onNavigateHome: () -> Unit = {},
    onOpenAdminLogin: () -> Unit,
    onOpenQrDialog: () -> Unit,
    onViewAgreement: (AgreementEntity) -> Unit
) {
    val context = LocalContext.current
    val isDarkScreen = isSystemInDarkTheme()
    var showLocalShareSheet by remember { mutableStateOf(false) }
    val isCreatingNew by viewModel.isCreatingNewWorkerForm.collectAsState()
    val allAgreementForms by viewModel.allAgreementForms.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize().imePadding(),
        topBar = {
            CenterAlignedTopAppBar(
                modifier = Modifier.shadow(
                    elevation = 4.dp,
                    spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                ),
                navigationIcon = {
                    IconButton(
                        onClick = onOpenMenu,
                        modifier = Modifier.testTag("btn_menu_drawer")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "মেনু বার",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                },
                title = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "RRF HR",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 22.sp,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "কর্মী জামানতনামা ও এগ্রিমেন্ট পোর্টাল",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                            textAlign = TextAlign.Center
                        )
                    }
                },
                actions = { },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                    actionIconContentColor = MaterialTheme.colorScheme.primary,
                    navigationIconContentColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (localAgreement != null && !formState.isEditingWorker && !isCreatingNew) {
                // Single submission active: Worker has already submitted
                WorkerAlreadySubmittedView(
                    agreement = localAgreement,
                    onPreview = {
                        val pdf = PdfGenerator.generateAgreementPdf(context, localAgreement, forStampPaper = true)
                        ShareHelper.openFile(context, pdf)
                    },
                    onEdit = { viewModel.prepareEditForWorker(localAgreement) },
                    onAddNewForm = {
                        viewModel.startNewWorkerForm()
                    },
                    onClear = {
                        viewModel.clearStampWorkerForm()
                        viewModel.startNewWorkerForm()
                    }
                )
            } else {
                // Form view (New submission, Editing, or Creating New Worker Form)
                WorkerInputForm(
                    viewModel = viewModel,
                    formState = formState,
                    isCreatingNew = isCreatingNew,
                    onCancelEdit = when {
                        formState.isEditingWorker -> { { viewModel.cancelWorkerEdit() } }
                        isCreatingNew -> { { viewModel.cancelNewWorkerForm() } }
                        localAgreement != null -> { { viewModel.cancelNewWorkerForm(); viewModel.cancelWorkerEdit() } }
                        else -> null
                    }
                )
            }
        }
    }

    if (showLocalShareSheet && localAgreement != null) {
        ShareSheetDialog(
            agreement = localAgreement,
            useStampMargin = true,
            onDismiss = { showLocalShareSheet = false }
        )
    }

    // Validation Error Dialog (Required: "লাল কালি দিয়ে পপআপ আসবে আপনার সমস্ত তথ্য পূরণ করুন")
    if (formState.showValidationErrorDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissValidationErrorDialog() },
            icon = {
                Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = AlertRed, modifier = Modifier.size(36.dp))
            },
            title = {
                Text(
                    "আপনার সমস্ত তথ্য পূরণ করুন",
                    color = AlertRed,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Column {
                    Text(
                        "ফর্মের চিহ্নিত লাল অংশগুলো পূরণ করা আবশ্যক। কোন তথ্য খালি রাখা যাবে না। অনুগ্রহ করে সঠিক তথ্য প্রদান করুন।",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.dismissValidationErrorDialog() },
                    colors = ButtonDefaults.buttonColors(containerColor = AlertRed)
                ) {
                    Text("ঠিক আছে")
                }
            }
        )
    }
}

@Composable
fun WorkerAlreadySubmittedView(
    agreement: AgreementEntity,
    onPreview: () -> Unit,
    onEdit: () -> Unit,
    onAddNewForm: () -> Unit,
    onClear: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(8.dp))

        Surface(
            shape = RoundedCornerShape(40.dp),
            color = if (agreement.isPrinted) LightGreenContainer else NavyContainer,
            modifier = Modifier.size(76.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    if (agreement.isPrinted) Icons.Default.CheckCircle else Icons.Default.AssignmentTurnedIn,
                    contentDescription = null,
                    tint = if (agreement.isPrinted) LightGreenPrimary else NavyPrimary,
                    modifier = Modifier.size(44.dp)
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        Text(
            if (agreement.editCount > 0) "আপনার সংশোধিত এগ্রিমেন্ট ফরম সংরক্ষিত রয়েছে" else "আপনার জামানতনামা এগ্রিমেন্ট ফরম সফলভাবে জমা হয়েছে",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(8.dp))

        // Edit version badge
        if (agreement.editCount > 0) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFFE8F5E9),
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.EditNote,
                        contentDescription = null,
                        tint = LightGreenDark,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "ফাইলের নাম: ${agreement.baseFileName}.pdf (${agreement.editLabel})",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = LightGreenDark
                    )
                }
            }
        }

        // Stamp print status badge
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = if (agreement.isPrinted) LightGreenContainer else Color(0xFFFFF3CD),
            modifier = Modifier.padding(vertical = 4.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    if (agreement.isPrinted) Icons.Default.CheckCircle else Icons.Default.Schedule,
                    contentDescription = null,
                    tint = if (agreement.isPrinted) LightGreenDark else Color(0xFFB78103),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    if (agreement.isPrinted) "১০০ টাকার স্ট্যাম্পে প্রিন্ট সম্পন্ন (${agreement.printDate ?: ""})" else "১০০ টাকার স্ট্যাম্পে প্রিন্ট অপেক্ষমান",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (agreement.isPrinted) LightGreenDark else Color(0xFF856404)
                )
            }
        }

        Spacer(Modifier.height(18.dp))

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                DetailRow("সিরিয়াল নং :", agreement.serialNo, isBold = true)
                HorizontalDivider(Modifier.padding(vertical = 8.dp))
                if (agreement.editCount > 0) {
                    DetailRow("ফাইলের নাম :", "${agreement.baseFileName}.pdf", isBold = true)
                    HorizontalDivider(Modifier.padding(vertical = 8.dp))
                    DetailRow("সংস্করণ :", agreement.editLabel, isBold = true)
                    HorizontalDivider(Modifier.padding(vertical = 8.dp))
                }
                DetailRow("আবেদনের তারিখ :", agreement.submissionDate)
                HorizontalDivider(Modifier.padding(vertical = 8.dp))
                DetailRow("কর্মীর নাম :", agreement.employeeName, isBold = true)
                HorizontalDivider(Modifier.padding(vertical = 8.dp))
                DetailRow("পিতার নাম :", agreement.employeeFatherName)
                HorizontalDivider(Modifier.padding(vertical = 8.dp))
                DetailRow("পদের নাম :", agreement.designation, isBold = true)
                HorizontalDivider(Modifier.padding(vertical = 8.dp))
                DetailRow("ঠিকানা :", "গ্রাম: ${agreement.employeeVillage}, ডাক: ${agreement.employeePostOffice}, উপজেলা: ${agreement.employeeUpazila}, জেলা: ${agreement.employeeDistrict}")
                HorizontalDivider(Modifier.padding(vertical = 8.dp))
                DetailRow("জামিনদারের নাম :", agreement.guarantorName)
                HorizontalDivider(Modifier.padding(vertical = 8.dp))
                DetailRow("সম্পর্ক :", agreement.effectiveGuarantorRelationship)
                HorizontalDivider(Modifier.padding(vertical = 8.dp))
                DetailRow("জামিনদারের NID :", agreement.guarantorNid)
            }
        }

        Spacer(Modifier.height(20.dp))

        Spacer(Modifier.height(16.dp))

        // Prominent instruction banner
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = Color(0xFFECFDF5),
            border = BorderStroke(1.2.dp, Color(0xFFA7F3D0)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = Color(0xFF059669),
                    modifier = Modifier.size(26.dp)
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text = "আপনার সমস্ত ফরম পূরণের পর হোম স্ক্রিনের অপশনে ক্লিক করে প্রিন্ট অথবা শেয়ার করুন।",
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF065F46),
                    lineHeight = 19.sp
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        // Action Buttons
        // 1. Preview PDF
        Button(
            onClick = onPreview,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("btn_worker_preview_pdf"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Icon(Icons.Default.Visibility, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text("স্ট্যাম্প প্রিভিউ (PDF দেখুন)", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
        }

        Spacer(Modifier.height(10.dp))

        // 2. Edit Option
        Button(
            onClick = onEdit,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("btn_worker_edit_agreement"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE65100))
        ) {
            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text("তথ্য সংশোধন / এডিট করুন", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }

        Spacer(Modifier.height(10.dp))

        // 3. New Form Option
        Button(
            onClick = onAddNewForm,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("btn_worker_add_new_form"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PurpleAccent)
        ) {
            Icon(Icons.Default.AddCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text("+ নতুন ফরম তৈরি করুন (নতুন কর্মী)", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
        }

        Spacer(Modifier.height(12.dp))

        Spacer(Modifier.height(20.dp))
    }
}

@Composable
fun DetailRow(label: String, value: String, isBold: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f, fill = false)
        )
    }
}

@Composable
fun WorkerInputForm(
    viewModel: MainViewModel,
    formState: FormState,
    isCreatingNew: Boolean = false,
    onCancelEdit: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val isDarkScreen = isSystemInDarkTheme()
    val allAgreementForms by viewModel.allAgreementForms.collectAsState()
    val designations = listOf("অফিসার (অ্যাকাউন্টস)", "অফিসার (ঋণ)", "সার্ভিস স্টাফ")
    val relationships = listOf("পিতা", "মাতা", "স্ত্রী", "স্বামী", " শ্বশুর", "ভাই", "অন্যান্য")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Form Link Header connecting 100-Taka Stamp to all other forms
        FormLinkHeader(
            currentScreen = AppScreen.WORKER_PANEL,
            viewModel = viewModel,
            onNavigate = { viewModel.updateScreen(it) }
        )

        Spacer(Modifier.height(10.dp))

        // Prominent Reset All Forms Button
        ResetAllFormsButton(
            viewModel = viewModel,
            onResetComplete = {}
        )

        Spacer(Modifier.height(10.dp))

        // Navigation Breadcrumb
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Description,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "ষ্ট্যাম্প",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Text("  ›  ", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    text = "ফর্ম পূরণ (কর্মী ও জামিনদার)",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Welcome Tagline Badge
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp, bottom = 14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(vertical = 8.dp, horizontal = 12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.VolunteerActivism,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "রুরাল রিকনস্ট্রাকশন ফাউন্ডেশন পরিবারে আপনাকে স্বাগতম",
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )
            }
        }

        // Edit Mode Banner
        if (formState.isEditing) {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3CD)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFC107)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("worker_edit_mode_banner")
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Edit, contentDescription = null, tint = Color(0xFF856404), modifier = Modifier.size(22.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "তথ্য সংশোধন / এডিট মোড",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF856404),
                            fontSize = 15.sp
                        )
                        Spacer(Modifier.weight(1f))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFFFEBAA)
                        ) {
                            val nextLabel = when (formState.currentEditCount) {
                                0 -> "এডিট"
                                1 -> "এডিট ১"
                                else -> "এডিট ${com.example.util.BanglaTextValidator.toBanglaDigits((formState.currentEditCount).toString())}"
                            }
                            Text(
                                "পরবর্তী ফাইল: $nextLabel",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF856404),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "ভুল তথ্য সংশোধন করে নিচে 'সংশোধন সংরক্ষণ ও পুনরায় শেয়ার করুন' বাটনে চাপ দিন।",
                        fontSize = 12.sp,
                        color = Color(0xFF664D03)
                    )
                }
            }
            Spacer(Modifier.height(14.dp))
        }

        // ---------------- SECTION 1: কর্মীর তথ্য ----------------
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            border = BorderStroke(1.2.dp, Color(0xFFD6E2F0)),
            modifier = Modifier.fillMaxWidth()
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
                            Text("১", fontWeight = FontWeight.ExtraBold, fontSize = 17.sp, color = NavyPrimary)
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "কর্মীর তথ্য",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp
                            ),
                            color = Color.White
                        )
                        Text(
                            "মূল ব্যক্তিগত ও পদবি সংক্রান্ত বিবরণ",
                            fontSize = 11.5.sp,
                            color = Color(0xFFD4E5FB)
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.White.copy(alpha = 0.2f),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.35f))
                    ) {
                        Text(
                            "আবশ্যক",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            Column(modifier = Modifier.padding(16.dp)) {
                // Employee Name
                ValidatedBanglaTextField(
                    label = "কর্মীর নাম *",
                    value = formState.employeeName,
                    onValueChange = { viewModel.updateEmployeeName(it) },
                    hasError = formState.validationErrors.contains("employeeName"),
                    placeholder = "বাংলায় কর্মীর নাম লিখুন",
                    leadingVector = Icons.Default.Person,
                    accentColor = Color(0xFF1553A3),
                    iconTint = NavyPrimary,
                    iconBackground = NavyContainer,
                    testTag = "input_employee_name"
                )

                Spacer(Modifier.height(10.dp))

                // Employee Father Name
                ValidatedBanglaTextField(
                    label = "কর্মীর পিতার নাম *",
                    value = formState.employeeFatherName,
                    onValueChange = { viewModel.updateEmployeeFatherName(it) },
                    hasError = formState.validationErrors.contains("employeeFatherName"),
                    placeholder = "বাংলায় পিতার নাম লিখুন",
                    leadingVector = Icons.Default.Badge,
                    accentColor = Color(0xFF1553A3),
                    iconTint = NavyPrimary,
                    iconBackground = NavyContainer,
                    testTag = "input_employee_father_name"
                )

                Spacer(Modifier.height(10.dp))

                // Designation Dropdown
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 6.dp, start = 2.dp)
                ) {
                    val isDesigFilled = formState.designation.isNotBlank()
                    val desigColor = Color(0xFF16A34A)
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(if (isDesigFilled) desigColor else Color(0xFF1553A3))
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "পদের নাম *",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp
                        ),
                        color = if (isDesigFilled) desigColor else MaterialTheme.colorScheme.onSurface
                    )
                    if (isDesigFilled) {
                        Spacer(Modifier.width(4.dp))
                        Text("✓", color = desigColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
                DropdownSelector(
                    items = designations,
                    selectedItem = formState.designation,
                    onItemSelected = { viewModel.updateDesignation(it) },
                    testTag = "dropdown_designation",
                    leadingVector = Icons.Default.Work,
                    accentColor = Color(0xFF1553A3),
                    iconTint = NavyPrimary,
                    iconBackground = NavyContainer
                )

                Spacer(Modifier.height(16.dp))

                // Subsection: কর্মীর বর্তমান ঠিকানা
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isDarkScreen) Color(0xFF1E3A8A).copy(alpha = 0.3f) else Color(0xFFF0F6FE),
                    border = BorderStroke(1.dp, if (isDarkScreen) Color(0xFF2563EB).copy(alpha = 0.5f) else Color(0xFFC7DEFC)),
                    shadowElevation = 1.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (isDarkScreen) Color(0xFF1D4ED8).copy(alpha = 0.4f) else Color(0xFFDCEAF9),
                            modifier = Modifier.size(24.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = if (isDarkScreen) Color(0xFF93C5FD) else Color(0xFF1553A3),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Spacer(Modifier.width(10.dp))
                        Text(
                            "কর্মীর বর্তমান ঠিকানা",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp,
                            color = if (isDarkScreen) Color(0xFFDBEAFE) else Color(0xFF0F3E78)
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                // জেলা
                ValidatedBanglaTextField(
                    label = "জেলা *",
                    value = formState.employeeDistrict,
                    onValueChange = { viewModel.updateEmployeeDistrict(it) },
                    hasError = formState.validationErrors.contains("employeeDistrict"),
                    placeholder = "জেলার নাম লিখুন (যেমন: যশোর)",
                    leadingVector = Icons.Default.LocationCity,
                    accentColor = Color(0xFF1553A3),
                    iconTint = NavyPrimary,
                    iconBackground = NavyContainer,
                    testTag = "input_employee_district"
                )

                Spacer(Modifier.height(10.dp))

                // উপজেলা / থানা
                ValidatedBanglaTextField(
                    label = "উপজেলা / থানা *",
                    value = formState.employeeUpazila,
                    onValueChange = { viewModel.updateEmployeeUpazila(it) },
                    hasError = formState.validationErrors.contains("employeeUpazila"),
                    placeholder = "উপজেলা বা থানার নাম লিখুন",
                    leadingVector = Icons.Default.Place,
                    accentColor = Color(0xFF1553A3),
                    iconTint = NavyPrimary,
                    iconBackground = NavyContainer,
                    testTag = "input_employee_upazila"
                )

                Spacer(Modifier.height(10.dp))

                // ডাকঘর
                ValidatedBanglaTextField(
                    label = "ডাকঘর / পোষ্টের নাম *",
                    value = formState.employeePostOffice,
                    onValueChange = { viewModel.updateEmployeePostOffice(it) },
                    hasError = formState.validationErrors.contains("employeePostOffice"),
                    placeholder = "ডাকঘরের নাম লিখুন",
                    leadingVector = Icons.Default.MarkunreadMailbox,
                    accentColor = Color(0xFF1553A3),
                    iconTint = NavyPrimary,
                    iconBackground = NavyContainer,
                    testTag = "input_employee_post_office"
                )

                Spacer(Modifier.height(10.dp))

                // গ্রাম
                ValidatedBanglaTextField(
                    label = "গ্রাম / গ্রামের নাম *",
                    value = formState.employeeVillage,
                    onValueChange = { viewModel.updateEmployeeVillage(it) },
                    hasError = formState.validationErrors.contains("employeeVillage"),
                    placeholder = "গ্রামের নাম লিখুন",
                    leadingVector = Icons.Default.Home,
                    accentColor = Color(0xFF1553A3),
                    iconTint = NavyPrimary,
                    iconBackground = NavyContainer,
                    testTag = "input_employee_village"
                )

                // Bilingual Address Live Preview
                val previewBanglaAddr = BanglaAddressHelper.formatBanglaAddress(
                    village = formState.employeeVillage,
                    postOffice = formState.employeePostOffice,
                    thanaOrUpazila = formState.employeeUpazila,
                    district = formState.employeeDistrict
                )
                val previewEnglishAddr = BanglaAddressHelper.formatEnglishAddress(
                    village = formState.employeeVillage,
                    postOffice = formState.employeePostOffice,
                    thanaOrUpazila = formState.employeeUpazila,
                    district = formState.employeeDistrict
                )

                if (previewBanglaAddr.isNotBlank()) {
                    Spacer(Modifier.height(12.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isDarkScreen) Color(0xFF1E3A8A).copy(alpha = 0.3f) else Color(0xFFEFF6FF),
                        border = BorderStroke(1.dp, if (isDarkScreen) Color(0xFF2563EB).copy(alpha = 0.5f) else Color(0xFFBFDBFE)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = if (isDarkScreen) Color(0xFF60A5FA) else Color(0xFF1D4ED8), modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("স্বয়ংক্রিয় ঠিকানা দ্বৈত ফরম্যাট (Auto Sync):", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (isDarkScreen) Color(0xFF93C5FD) else Color(0xFF1E40AF))
                            }
                            Text("• বাংলা: $previewBanglaAddr", fontSize = 11.5.sp, color = if (isDarkScreen) Color(0xFFDBEAFE) else Color(0xFF1E3A8A))
                            Text("• ইংরেজি (ID Card): $previewEnglishAddr", fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold, color = if (isDarkScreen) Color(0xFFBFDBFE) else Color(0xFF1E3A8A))
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // ---------------- SECTION 2: জামিনদারের তথ্য ----------------
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            border = BorderStroke(1.2.dp, Color(0xFFCCE8DA)),
            modifier = Modifier.fillMaxWidth()
        ) {
            // 3D Header Banner with Gradient & Badge
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF0C5D3E),
                                Color(0xFF147A53),
                                Color(0xFF1EA06E)
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
                        border = BorderStroke(2.dp, Color(0xFFBAEBD2)),
                        shadowElevation = 3.dp,
                        modifier = Modifier.size(34.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("২", fontWeight = FontWeight.ExtraBold, fontSize = 17.sp, color = LightGreenDark)
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "জামিনদারের তথ্য",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp
                            ),
                            color = Color.White
                        )
                        Text(
                            "আইনগত জামিনদার ও ঠিকানার বিবরণ",
                            fontSize = 11.5.sp,
                            color = Color(0xFFDCF8EA)
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.White.copy(alpha = 0.2f),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.35f))
                    ) {
                        Text(
                            "বাধ্যতামূলক",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            Column(modifier = Modifier.padding(16.dp)) {
                // Quick Guarantor Selection Note & Checkboxes
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isDarkScreen) Color(0xFF064E3B).copy(alpha = 0.35f) else Color(0xFFF0FDF4),
                    border = BorderStroke(1.dp, if (isDarkScreen) Color(0xFF059669) else Color(0xFF86EFAC)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(
                                "নোট: পিতা জামিনদার হলে টিক দিন এবং মাতা জামিনদার হলে টিক দিন।",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.5.sp,
                                color = if (isDarkScreen) Color(0xFF86EFAC) else Color(0xFF166534)
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Checkbox 1: পিতা জামিনদার হলে টিক দিন
                            val fatherNidVal = allAgreementForms.personalInfoForm.fatherNid
                            val motherNameVal = if (formState.guarantorMotherName.isNotBlank()) formState.guarantorMotherName else allAgreementForms.personalInfoForm.motherName
                            val motherNidVal = allAgreementForms.personalInfoForm.motherNid

                            val isFatherG = formState.guarantorRelationship == "পিতা"
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isFatherG) (if (isDarkScreen) Color(0xFF047857).copy(alpha = 0.5f) else Color(0xFFDCFCE7)) else MaterialTheme.colorScheme.surface,
                                border = BorderStroke(1.dp, if (isFatherG) Color(0xFF16A34A) else Color(0xFFCBD5E1)),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        if (!isFatherG) {
                                            viewModel.updateGuarantorName(formState.employeeFatherName)
                                            viewModel.updateGuarantorRelationship("পিতা")
                                            viewModel.updateGuarantorNid(fatherNidVal)
                                        } else {
                                            viewModel.updateGuarantorName("")
                                            viewModel.updateGuarantorRelationship("")
                                            viewModel.updateGuarantorNid("")
                                        }
                                    }
                            ) {
                                Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(
                                        checked = isFatherG,
                                        onCheckedChange = { checked ->
                                            if (checked) {
                                                viewModel.updateGuarantorName(formState.employeeFatherName)
                                                viewModel.updateGuarantorRelationship("পিতা")
                                                viewModel.updateGuarantorNid(fatherNidVal)
                                            } else {
                                                viewModel.updateGuarantorName("")
                                                viewModel.updateGuarantorRelationship("")
                                                viewModel.updateGuarantorNid("")
                                            }
                                        },
                                        colors = CheckboxDefaults.colors(checkedColor = Color(0xFF16A34A))
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text("পিতা জামিনদার হলে টিক দিন", fontSize = 12.sp, fontWeight = if (isFatherG) FontWeight.Bold else FontWeight.Medium)
                                }
                            }

                            // Checkbox 2: মাতা জামিনদার হলে টিক দিন
                            val isMotherG = formState.guarantorRelationship == "মাতা"
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isMotherG) (if (isDarkScreen) Color(0xFF047857).copy(alpha = 0.5f) else Color(0xFFDCFCE7)) else MaterialTheme.colorScheme.surface,
                                border = BorderStroke(1.dp, if (isMotherG) Color(0xFF16A34A) else Color(0xFFCBD5E1)),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        if (!isMotherG) {
                                            viewModel.updateGuarantorName(motherNameVal)
                                            viewModel.updateGuarantorRelationship("মাতা")
                                            viewModel.updateGuarantorNid(motherNidVal)
                                        } else {
                                            viewModel.updateGuarantorName("")
                                            viewModel.updateGuarantorRelationship("")
                                            viewModel.updateGuarantorNid("")
                                        }
                                    }
                            ) {
                                Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(
                                        checked = isMotherG,
                                        onCheckedChange = { checked ->
                                            if (checked) {
                                                viewModel.updateGuarantorName(motherNameVal)
                                                viewModel.updateGuarantorRelationship("মাতা")
                                                viewModel.updateGuarantorNid(motherNidVal)
                                            } else {
                                                viewModel.updateGuarantorName("")
                                                viewModel.updateGuarantorRelationship("")
                                                viewModel.updateGuarantorNid("")
                                            }
                                        },
                                        colors = CheckboxDefaults.colors(checkedColor = Color(0xFF16A34A))
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text("মাতা জামিনদার হলে টিক দিন", fontSize = 12.sp, fontWeight = if (isMotherG) FontWeight.Bold else FontWeight.Medium)
                                }
                            }
                        }
                    }
                }

                // Guarantor Name
                ValidatedBanglaTextField(
                    label = "জামিনদারের নাম *",
                    value = formState.guarantorName,
                    onValueChange = { viewModel.updateGuarantorName(it) },
                    hasError = formState.validationErrors.contains("guarantorName"),
                    placeholder = "বাংলায় জামিনদারের নাম লিখুন",
                    leadingVector = Icons.Default.Security,
                    accentColor = Color(0xFF147A53),
                    iconTint = LightGreenDark,
                    iconBackground = LightGreenContainer,
                    testTag = "input_guarantor_name"
                )

                Spacer(Modifier.height(10.dp))

                // Guarantor Father Name
                ValidatedBanglaTextField(
                    label = "জামিনদারের পিতার নাম *",
                    value = formState.guarantorFatherName,
                    onValueChange = { viewModel.updateGuarantorFatherName(it) },
                    hasError = formState.validationErrors.contains("guarantorFatherName"),
                    placeholder = "বাংলায় পিতার নাম লিখুন",
                    leadingVector = Icons.Default.Person,
                    accentColor = Color(0xFF147A53),
                    iconTint = LightGreenDark,
                    iconBackground = LightGreenContainer,
                    testTag = "input_guarantor_father_name"
                )

                Spacer(Modifier.height(10.dp))

                // Guarantor Mother Name
                ValidatedBanglaTextField(
                    label = "জামিনদারের মাতার নাম *",
                    value = formState.guarantorMotherName,
                    onValueChange = { viewModel.updateGuarantorMotherName(it) },
                    hasError = formState.validationErrors.contains("guarantorMotherName"),
                    placeholder = "বাংলায় মাতার নাম লিখুন",
                    leadingVector = Icons.Default.Face,
                    accentColor = Color(0xFF147A53),
                    iconTint = LightGreenDark,
                    iconBackground = LightGreenContainer,
                    testTag = "input_guarantor_mother_name"
                )

                Spacer(Modifier.height(10.dp))

                // Relationship Dropdown
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 6.dp, start = 2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF147A53))
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "সম্পর্ক *",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp
                        ),
                        color = Color(0xFF334155)
                    )
                }
                DropdownSelector(
                    items = relationships,
                    selectedItem = formState.guarantorRelationship,
                    onItemSelected = { viewModel.updateGuarantorRelationship(it) },
                    testTag = "dropdown_relationship",
                    leadingVector = Icons.Default.People,
                    accentColor = Color(0xFF147A53),
                    iconTint = LightGreenDark,
                    iconBackground = LightGreenContainer
                )

                if (formState.guarantorRelationship == "অন্যান্য") {
                    Spacer(Modifier.height(10.dp))
                    ValidatedBanglaTextField(
                        label = "অন্যান্য সম্পর্ক লিখুন *",
                        value = formState.guarantorRelationshipCustom,
                        onValueChange = { viewModel.updateGuarantorRelationshipCustom(it) },
                        hasError = formState.validationErrors.contains("guarantorRelationshipCustom"),
                        placeholder = "সম্পর্ক বাংলায় লিখুন",
                        leadingVector = Icons.Default.Edit,
                        accentColor = Color(0xFF147A53),
                        iconTint = LightGreenDark,
                        iconBackground = LightGreenContainer,
                        testTag = "input_custom_relationship"
                    )
                }

                Spacer(Modifier.height(10.dp))

                // NID in 3D
                ValidatedBanglaTextField(
                    label = "ভোটার আইডি নং (NID) *",
                    value = formState.guarantorNid,
                    onValueChange = { viewModel.updateGuarantorNid(it) },
                    hasError = formState.validationErrors.contains("guarantorNid"),
                    placeholder = "জাতীয় পরিচয়পত্র নম্বর (শুধুমাত্র সংখ্যা)",
                    leadingVector = Icons.Default.CreditCard,
                    accentColor = Color(0xFF147A53),
                    iconTint = LightGreenDark,
                    iconBackground = LightGreenContainer,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    errorText = "ভোটার আইডি নম্বর দিন (সংখ্যা ছাড়া অন্য কিছু নয়)",
                    helperText = "১০, ১৩ বা ১৭ ডিজিটের জাতীয় পরিচয়পত্র নম্বর",
                    testTag = "input_guarantor_nid"
                )

                Spacer(Modifier.height(16.dp))

                // 3D Elevated Checkbox: জামিনদার ও কর্মীর ঠিকানা এক
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (formState.sameAddress) (if (isDarkScreen) Color(0xFF064E3B).copy(alpha = 0.3f) else Color(0xFFE9F9F0))
                    else (if (isDarkScreen) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else Color(0xFFF8FAFC)),
                    border = BorderStroke(
                        1.5.dp,
                        if (formState.sameAddress) (if (isDarkScreen) Color(0xFF059669) else Color(0xFF2EB872))
                        else (if (isDarkScreen) Color(0xFF475569) else Color(0xFFCBD5E1))
                    ),
                    shadowElevation = 2.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.toggleSameAddress(!formState.sameAddress) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = formState.sameAddress,
                            onCheckedChange = { viewModel.toggleSameAddress(it) },
                            colors = CheckboxDefaults.colors(checkedColor = LightGreenDark),
                            modifier = Modifier.testTag("checkbox_same_address")
                        )
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(
                                "জামিনদার ও কর্মীর ঠিকানা একই (টিক দিন)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.5.sp,
                                color = if (formState.sameAddress) (if (isDarkScreen) Color(0xFF6EE7B7) else Color(0xFF0F5A37)) else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                "টিক দিলে জামিনদারের জন্য আলাদা ঠিকানা লিখতে হবে না",
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                if (!formState.sameAddress) {
                    Spacer(Modifier.height(16.dp))

                    // Subsection: জামিনদারের বর্তমান ঠিকানা
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isDarkScreen) Color(0xFF064E3B).copy(alpha = 0.3f) else Color(0xFFEAF8F0),
                        border = BorderStroke(1.dp, if (isDarkScreen) Color(0xFF059669).copy(alpha = 0.5f) else Color(0xFFBFE9D2)),
                        shadowElevation = 1.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isDarkScreen) Color(0xFF047857).copy(alpha = 0.4f) else Color(0xFFD4F7E3),
                                modifier = Modifier.size(24.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.LocationOn,
                                        contentDescription = null,
                                        tint = if (isDarkScreen) Color(0xFF6EE7B7) else LightGreenDark,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Spacer(Modifier.width(10.dp))
                            Text(
                                "জামিনদারের বর্তমান ঠিকানা",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.5.sp,
                                color = if (isDarkScreen) Color(0xFFD1FAE5) else Color(0xFF0B5838)
                            )
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    // জেলা
                    ValidatedBanglaTextField(
                        label = "জেলা *",
                        value = formState.guarantorDistrict,
                        onValueChange = { viewModel.updateGuarantorDistrict(it) },
                        hasError = formState.validationErrors.contains("guarantorDistrict"),
                        placeholder = "জেলার নাম লিখুন",
                        leadingVector = Icons.Default.LocationCity,
                        accentColor = Color(0xFF147A53),
                        iconTint = LightGreenDark,
                        iconBackground = LightGreenContainer,
                        testTag = "input_guarantor_district"
                    )

                    Spacer(Modifier.height(10.dp))

                    // উপজেলা / থানা
                    ValidatedBanglaTextField(
                        label = "উপজেলা / থানা *",
                        value = formState.guarantorUpazila,
                        onValueChange = { viewModel.updateGuarantorUpazila(it) },
                        hasError = formState.validationErrors.contains("guarantorUpazila"),
                        placeholder = "উপজেলা বা থানার নাম লিখুন",
                        leadingVector = Icons.Default.Place,
                        accentColor = Color(0xFF147A53),
                        iconTint = LightGreenDark,
                        iconBackground = LightGreenContainer,
                        testTag = "input_guarantor_upazila"
                    )

                    Spacer(Modifier.height(10.dp))

                    // ডাকঘর
                    ValidatedBanglaTextField(
                        label = "ডাকঘর / পোষ্টের নাম *",
                        value = formState.guarantorPostOffice,
                        onValueChange = { viewModel.updateGuarantorPostOffice(it) },
                        hasError = formState.validationErrors.contains("guarantorPostOffice"),
                        placeholder = "ডাকঘরের নাম লিখুন",
                        leadingVector = Icons.Default.MarkunreadMailbox,
                        accentColor = Color(0xFF147A53),
                        iconTint = LightGreenDark,
                        iconBackground = LightGreenContainer,
                        testTag = "input_guarantor_post_office"
                    )

                    Spacer(Modifier.height(10.dp))

                    // গ্রাম
                    ValidatedBanglaTextField(
                        label = "গ্রাম / গ্রামের নাম *",
                        value = formState.guarantorVillage,
                        onValueChange = { viewModel.updateGuarantorVillage(it) },
                        hasError = formState.validationErrors.contains("guarantorVillage"),
                        placeholder = "গ্রামের নাম লিখুন",
                        leadingVector = Icons.Default.Home,
                        accentColor = Color(0xFF147A53),
                        iconTint = LightGreenDark,
                        iconBackground = LightGreenContainer,
                        testTag = "input_guarantor_village"
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        // Dual Action Row: প্রিভিউ বাটন বাম পাশে ও সংরক্ষণ বাটন ডান পাশে
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // 1. Preview Button (বাম পাশে)
            OutlinedButton(
                onClick = {
                    val stampAgr = viewModel.getStampAgreementForPreview()
                    val pdf = PdfGenerator.generateAgreementPdf(context, stampAgr, forStampPaper = true)
                    ShareHelper.openFile(context, pdf)
                },
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.5.dp, Color(0xFF059669)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF059669)),
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
                    .testTag("btn_worker_preview_bottom")
            ) {
                Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("প্রিভিউ", fontWeight = FontWeight.Bold, fontSize = 14.5.sp)
            }

            // 2. Save Button (ডান পাশে) with Pulsing Animation and auto hide keyboard
            val isReady = formState.validationErrors.isEmpty() && formState.employeeName.isNotBlank() && formState.guarantorName.isNotBlank()
            PulsingSaveButton(
                onClick = { viewModel.submitForm(isAdmin = formState.isEditingByAdmin) },
                text = if (formState.isEditing) "সংশোধন সংরক্ষণ" else "সংরক্ষণ করুন",
                isReady = isReady,
                modifier = Modifier.weight(1.4f),
                testTag = "submit_agreement_form"
            )
        }

        if (formState.isEditing && onCancelEdit != null) {
            Spacer(Modifier.height(10.dp))
            OutlinedButton(
                onClick = onCancelEdit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("cancel_worker_edit_btn"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Close, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("সংশোধন বাতিল করুন")
            }
        }

        // Small box button to return to previous ("পূর্বে ফিরে যান")
        if (onCancelEdit != null) {
            Spacer(Modifier.height(16.dp))
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    onClick = onCancelEdit,
                    shape = RoundedCornerShape(10.dp),
                    color = Color.White,
                    border = BorderStroke(1.5.dp, Color(0xFFCBD5E1)),
                    shadowElevation = 3.dp,
                    modifier = Modifier.testTag("btn_back_to_previous")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 22.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                            tint = NavyPrimary,
                            modifier = Modifier.size(17.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "পূর্বে ফিরে যান",
                            color = NavyPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(140.dp))
    }
}

@Composable
fun ValidatedBanglaTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    hasError: Boolean,
    placeholder: String,
    testTag: String,
    leadingIcon: (@Composable () -> Unit)? = null,
    leadingVector: ImageVector? = null,
    accentColor: Color = CorporateBlueGradientEnd,
    iconTint: Color = NavyPrimary,
    iconBackground: Color = NavyContainer,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    errorText: String = "এই তথ্যটি পূরণ করা আবশ্যক (বাংলায়)",
    helperText: String? = null
) {
    var isFocused by remember { mutableStateOf(false) }
    val isDark = isSystemInDarkTheme()

    val isFilled = value.isNotBlank() && !hasError
    val greenColor = if (isDark) Color(0xFF34D399) else Color(0xFF16A34A)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        // Label with 3D status bead
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 6.dp, start = 2.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(if (hasError) AlertRed else if (isFilled) greenColor else if (isFocused) accentColor else (if (isDark) Color(0xFF64748B) else Color(0xFF94A3B8)))
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.5.sp
                ),
                color = if (hasError) AlertRed else if (isFilled) greenColor else if (isFocused) accentColor else MaterialTheme.colorScheme.onSurface
            )
            if (isFilled) {
                Spacer(Modifier.width(4.dp))
                Text("✓", color = greenColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        // 3D Elevated Input Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = if (isFocused) 6.dp else 2.5.dp,
                    shape = RoundedCornerShape(13.dp),
                    spotColor = if (hasError) AlertRed.copy(alpha = 0.35f) else if (isFilled) greenColor.copy(alpha = 0.3f) else if (isFocused) accentColor.copy(alpha = 0.3f) else Color(0x18000000),
                    ambientColor = Color(0x0F000000)
                )
                .background(
                    brush = Brush.verticalGradient(
                        colors = if (isDark) listOf(
                            MaterialTheme.colorScheme.surfaceVariant,
                            if (isFilled) Color(0xFF064E3B).copy(alpha = 0.3f) else if (isFocused) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f) else MaterialTheme.colorScheme.surface
                        ) else listOf(
                            if (isFilled) Color(0xFFF0FDF4) else Color.White,
                            if (isFilled) Color(0xFFDCFCE7) else if (isFocused) Color(0xFFFAFDFF) else Color(0xFFF8FAFC)
                        )
                    ),
                    shape = RoundedCornerShape(13.dp)
                )
                .border(
                    width = if (isFocused || isFilled) 1.8.dp else 1.2.dp,
                    brush = if (hasError) {
                        Brush.linearGradient(listOf(AlertRed, Color(0xFFFF5252)))
                    } else if (isFilled) {
                        Brush.linearGradient(listOf(greenColor, greenColor.copy(alpha = 0.8f)))
                    } else if (isFocused) {
                        Brush.linearGradient(listOf(accentColor, accentColor.copy(alpha = 0.8f)))
                    } else {
                        if (isDark) Brush.verticalGradient(listOf(Color(0xFF475569), Color(0xFF334155)))
                        else Brush.verticalGradient(listOf(Color(0xFFE2E8F0), Color(0xFFCBD5E1)))
                    },
                    shape = RoundedCornerShape(13.dp)
                )
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = {},
                readOnly = true,
                placeholder = {
                    Text(
                        placeholder,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        fontSize = 13.5.sp
                    )
                },
                singleLine = true,
                keyboardOptions = keyboardOptions,
                isError = hasError,
                trailingIcon = if (isFilled) {
                    {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "পূরণকৃত",
                            tint = greenColor,
                            modifier = Modifier.padding(end = 8.dp).size(20.dp)
                        )
                    }
                } else null,
                leadingIcon = {
                    if (leadingVector != null) {
                        Surface(
                            shape = RoundedCornerShape(9.dp),
                            color = if (hasError) AlertRedContainer else if (isFocused) iconBackground else (if (isDark) MaterialTheme.colorScheme.surfaceVariant else iconBackground.copy(alpha = 0.85f)),
                            border = BorderStroke(
                                1.dp,
                                if (hasError) AlertRed.copy(alpha = 0.4f) else iconTint.copy(alpha = 0.25f)
                            ),
                            shadowElevation = 1.dp,
                            modifier = Modifier
                                .padding(start = 6.dp, end = 2.dp)
                                .size(34.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = leadingVector,
                                    contentDescription = null,
                                    tint = if (hasError) AlertRed else iconTint,
                                    modifier = Modifier.size(19.dp)
                                )
                            }
                        }
                    } else if (leadingIcon != null) {
                        Surface(
                            shape = RoundedCornerShape(9.dp),
                            color = if (hasError) AlertRedContainer else (if (isDark) MaterialTheme.colorScheme.surfaceVariant else iconBackground),
                            border = BorderStroke(1.dp, iconTint.copy(alpha = 0.25f)),
                            shadowElevation = 1.dp,
                            modifier = Modifier
                                .padding(start = 6.dp, end = 2.dp)
                                .size(34.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                leadingIcon()
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { isFocused = it.isFocused }
                    .testTag(testTag),
                shape = RoundedCornerShape(13.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    errorBorderColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    errorContainerColor = Color.Transparent,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }

        // Supporting error / helper text
        if (hasError) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 4.dp, start = 4.dp)
            ) {
                Icon(
                    Icons.Default.ErrorOutline,
                    contentDescription = null,
                    tint = AlertRed,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = errorText,
                    color = AlertRed,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        } else if (helperText != null) {
            Text(
                text = helperText,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.5.sp,
                modifier = Modifier.padding(top = 3.dp, start = 4.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownSelector(
    items: List<String>,
    selectedItem: String,
    onItemSelected: (String) -> Unit,
    testTag: String,
    leadingVector: ImageVector? = null,
    iconTint: Color = NavyPrimary,
    iconBackground: Color = NavyContainer,
    accentColor: Color = CorporateBlueGradientEnd
) {
    var expanded by remember { mutableStateOf(false) }
    val isDark = isSystemInDarkTheme()

    val isFilled = selectedItem.isNotBlank()
    val greenColor = if (isDark) Color(0xFF34D399) else Color(0xFF16A34A)

    ExposedDropdownMenuBox(
        expanded = false,
        onExpandedChange = {},
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
                .shadow(
                    elevation = if (expanded) 6.dp else 2.5.dp,
                    shape = RoundedCornerShape(13.dp),
                    spotColor = if (expanded) accentColor.copy(alpha = 0.35f) else if (isFilled) greenColor.copy(alpha = 0.3f) else Color(0x18000000),
                    ambientColor = Color(0x0F000000)
                )
                .background(
                    brush = Brush.verticalGradient(
                        colors = if (isDark) listOf(
                            MaterialTheme.colorScheme.surfaceVariant,
                            if (isFilled) Color(0xFF064E3B).copy(alpha = 0.3f) else if (expanded) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f) else MaterialTheme.colorScheme.surface
                        ) else listOf(
                            if (isFilled) Color(0xFFF0FDF4) else Color.White,
                            if (isFilled) Color(0xFFDCFCE7) else if (expanded) Color(0xFFFAFDFF) else Color(0xFFF8FAFC)
                        )
                    ),
                    shape = RoundedCornerShape(13.dp)
                )
                .border(
                    width = if (expanded || isFilled) 1.8.dp else 1.2.dp,
                    brush = if (expanded) {
                        Brush.linearGradient(listOf(accentColor, accentColor.copy(alpha = 0.8f)))
                    } else if (isFilled) {
                        Brush.linearGradient(listOf(greenColor, greenColor.copy(alpha = 0.8f)))
                    } else {
                        if (isDark) Brush.verticalGradient(listOf(Color(0xFF475569), Color(0xFF334155)))
                        else Brush.verticalGradient(listOf(Color(0xFFE2E8F0), Color(0xFFCBD5E1)))
                    },
                    shape = RoundedCornerShape(13.dp)
                )
        ) {
            OutlinedTextField(
                value = selectedItem,
                onValueChange = {},
                readOnly = true,
                leadingIcon = if (leadingVector != null) {
                    {
                        Surface(
                            shape = RoundedCornerShape(9.dp),
                            color = if (isDark) MaterialTheme.colorScheme.surfaceVariant else if (isFilled) Color(0xFFECFDF5) else iconBackground,
                            border = BorderStroke(1.dp, if (isFilled) greenColor.copy(alpha = 0.4f) else iconTint.copy(alpha = 0.25f)),
                            shadowElevation = 1.dp,
                            modifier = Modifier
                                .padding(start = 6.dp, end = 2.dp)
                                .size(34.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = leadingVector,
                                    contentDescription = null,
                                    tint = if (isFilled) greenColor else iconTint,
                                    modifier = Modifier.size(19.dp)
                                )
                            }
                        }
                    }
                } else null,
                trailingIcon = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (isFilled) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "পূরণকৃত",
                                tint = greenColor,
                                modifier = Modifier.padding(end = 4.dp).size(20.dp)
                            )
                        }
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(testTag),
                shape = RoundedCornerShape(13.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedTextColor = if (isFilled) greenColor else MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = if (isFilled) greenColor else MaterialTheme.colorScheme.onSurface
                )
            )
        }

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
        ) {
            items.forEach { item ->
                DropdownMenuItem(
                    text = {
                        Text(
                            item,
                            fontWeight = if (item == selectedItem) FontWeight.Bold else FontWeight.Normal,
                            color = if (item == selectedItem) accentColor else MaterialTheme.colorScheme.onSurface
                        )
                    },
                    onClick = {
                        onItemSelected(item)
                        expanded = false
                    },
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
                )
            }
        }
    }
}


@Composable
private fun FeaturePill(icon: ImageVector, label: String) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = Color.White.copy(alpha = 0.2f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
            Spacer(Modifier.width(4.dp))
            Text(label, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Medium)
        }
    }
}


