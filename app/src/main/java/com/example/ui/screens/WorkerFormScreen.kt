package com.example.ui.screens

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.AgreementEntity
import com.example.ui.FormState
import com.example.ui.MainViewModel
import com.example.ui.components.ShareSheetDialog
import com.example.ui.theme.*
import com.example.util.PdfGenerator
import com.example.util.ShareHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkerFormScreen(
    viewModel: MainViewModel,
    formState: FormState,
    localAgreement: AgreementEntity?,
    onOpenAdminLogin: () -> Unit,
    onOpenQrDialog: () -> Unit,
    onViewAgreement: (AgreementEntity) -> Unit
) {
    val context = LocalContext.current
    var showLocalShareSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("RRF HR", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                            Spacer(Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = LightGreenPrimary.copy(alpha = 0.25f)
                            ) {
                                Text(
                                    "রুরাল রিকনস্ট্রাকশন ফাউন্ডেশন",
                                    fontSize = 11.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            "কর্মী জামানতনামা ও এগ্রিমেন্ট পোর্টাল",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onOpenQrDialog, modifier = Modifier.testTag("btn_qr_code")) {
                        Icon(Icons.Default.QrCode, contentDescription = "QR Code", tint = Color.White)
                    }
                    IconButton(onClick = onOpenAdminLogin, modifier = Modifier.testTag("btn_admin_login")) {
                        Icon(Icons.Default.AdminPanelSettings, contentDescription = "Admin Login", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
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
            if (localAgreement != null && !formState.isEditingWorker) {
                // Single submission active: Worker has already submitted
                WorkerAlreadySubmittedView(
                    agreement = localAgreement,
                    onView = { onViewAgreement(localAgreement) },
                    onEdit = { viewModel.prepareEditForWorker(localAgreement) },
                    onPrintPdf = {
                        val pdf = PdfGenerator.generateAgreementPdf(context, localAgreement, forStampPaper = true)
                        ShareHelper.openFile(context, pdf)
                    },
                    onShare = { showLocalShareSheet = true }
                )
            } else {
                // Form view (New submission or Editing)
                WorkerInputForm(
                    viewModel = viewModel,
                    formState = formState,
                    onCancelEdit = if (formState.isEditingWorker) { { viewModel.cancelWorkerEdit() } } else null
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
    onView: () -> Unit,
    onEdit: () -> Unit,
    onPrintPdf: () -> Unit,
    onShare: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(12.dp))
        Surface(
            shape = RoundedCornerShape(40.dp),
            color = if (agreement.isPrinted) LightGreenContainer else NavyContainer,
            modifier = Modifier.size(80.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    if (agreement.isPrinted) Icons.Default.CheckCircle else Icons.Default.AssignmentTurnedIn,
                    contentDescription = null,
                    tint = if (agreement.isPrinted) LightGreenPrimary else NavyPrimary,
                    modifier = Modifier.size(48.dp)
                )
            }
        }

        Spacer(Modifier.height(16.dp))

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

        Spacer(Modifier.height(20.dp))

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

        // Action Buttons
        // 1. Edit Option (Prominent for workers to correct mistakes)
        Button(
            onClick = onEdit,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("btn_worker_edit_agreement"),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE65100))
        ) {
            Icon(Icons.Default.Edit, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("তথ্য সংশোধন / এডিট করুন", fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(8.dp))

        // 2. View Agreement
        Button(
            onClick = onView,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("btn_worker_view_agreement"),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Icon(Icons.Default.Description, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("এগ্রিমেন্ট দেখুন (Agreement 1 ও 2)")
        }

        Spacer(Modifier.height(8.dp))

        // 3. Print
        Button(
            onClick = onPrintPdf,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("btn_worker_print_pdf"),
            colors = ButtonDefaults.buttonColors(containerColor = LightGreenDark)
        ) {
            Icon(Icons.Default.Print, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("১০০ টাকার স্ট্যাম্পে প্রিন্ট / Legal PDF")
        }

        Spacer(Modifier.height(8.dp))

        // 4. Share
        OutlinedButton(
            onClick = onShare,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Share, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("হোয়াটসঅ্যাপ ও মেইলে শেয়ার করুন")
        }

        Spacer(Modifier.height(16.dp))

        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(10.dp))
                Text(
                    "ফর্ম পূরণে কোনো ভুল হলে 'তথ্য সংশোধন / এডিট করুন' বাটনে চাপ দিয়ে সংশোধন ও পুনরায় সেভ করে শেয়ার করতে পারবেন। প্রতিবার এডিট করলে ফাইলের নামের পর স্বয়ংক্রিয়ভাবে 'এডিট' ও পরে 'এডিট ১' লেখা থাকবে।",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
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
    onCancelEdit: (() -> Unit)? = null
) {
    val designations = listOf("অফিসার (অ্যাকাউন্টস)", "অফিসার (ঋণ)", "সার্ভিস স্টাফ")
    val relationships = listOf("পিতা", "মাতা", "স্ত্রী", "স্বামী", " শ্বশুর", "ভাই", "অন্যান্য")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
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

        // Form Banner
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Feed, contentDescription = null, tint = LightGreenPrimary, modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "১০০ টাকার স্ট্যাম্পে এগ্রিমেন্ট ফরম",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    "সকল তথ্য বাংলায় প্রদান করুন। কোনো তথ্য খালি রাখা যাবে না। সংখ্যা বা অসংলগ্ন বর্ণ প্রবেশ নিষিদ্ধ।",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 12.sp
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // ---------------- SECTION 1: কর্মীর তথ্য ----------------
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = NavyContainer,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("১", fontWeight = FontWeight.Bold, color = NavyPrimary)
                        }
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "কর্মীর তথ্য",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(Modifier.height(14.dp))

                // Employee Name
                ValidatedBanglaTextField(
                    label = "কর্মীর নাম *",
                    value = formState.employeeName,
                    onValueChange = { viewModel.updateEmployeeName(it) },
                    hasError = formState.validationErrors.contains("employeeName"),
                    placeholder = "বাংলায় কর্মীর নাম লিখুন",
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
                    testTag = "input_employee_father_name"
                )

                Spacer(Modifier.height(10.dp))

                // Designation Dropdown
                Text(
                    "পদের নাম *",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(4.dp))
                DropdownSelector(
                    items = designations,
                    selectedItem = formState.designation,
                    onItemSelected = { viewModel.updateDesignation(it) },
                    testTag = "dropdown_designation"
                )

                Spacer(Modifier.height(14.dp))
                Text(
                    "কর্মীর বর্তমান ঠিকানা",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(8.dp))

                // জেলা
                ValidatedBanglaTextField(
                    label = "জেলা *",
                    value = formState.employeeDistrict,
                    onValueChange = { viewModel.updateEmployeeDistrict(it) },
                    hasError = formState.validationErrors.contains("employeeDistrict"),
                    placeholder = "জেলার নাম লিখুন (যেমন: যশোর)",
                    leadingIcon = {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    },
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
                    leadingIcon = {
                        Icon(
                            Icons.Default.Place,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    },
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
                    leadingIcon = {
                        Icon(
                            Icons.Default.MarkunreadMailbox,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    },
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
                    leadingIcon = {
                        Icon(
                            Icons.Default.Home,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    testTag = "input_employee_village"
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // ---------------- SECTION 2: জামিনদারের তথ্য ----------------
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = LightGreenContainer,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("২", fontWeight = FontWeight.Bold, color = LightGreenDark)
                        }
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "জামিনদারের তথ্য",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(Modifier.height(14.dp))

                // Guarantor Name
                ValidatedBanglaTextField(
                    label = "জামিনদারের নাম *",
                    value = formState.guarantorName,
                    onValueChange = { viewModel.updateGuarantorName(it) },
                    hasError = formState.validationErrors.contains("guarantorName"),
                    placeholder = "বাংলায় জামিনদারের নাম লিখুন",
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
                    testTag = "input_guarantor_mother_name"
                )

                Spacer(Modifier.height(10.dp))

                // Relationship Dropdown
                Text("সম্পর্ক *", style = MaterialTheme.typography.labelMedium)
                Spacer(Modifier.height(4.dp))
                DropdownSelector(
                    items = relationships,
                    selectedItem = formState.guarantorRelationship,
                    onItemSelected = { viewModel.updateGuarantorRelationship(it) },
                    testTag = "dropdown_relationship"
                )

                if (formState.guarantorRelationship == "অন্যান্য") {
                    Spacer(Modifier.height(10.dp))
                    ValidatedBanglaTextField(
                        label = "অন্যান্য সম্পর্ক লিখুন *",
                        value = formState.guarantorRelationshipCustom,
                        onValueChange = { viewModel.updateGuarantorRelationshipCustom(it) },
                        hasError = formState.validationErrors.contains("guarantorRelationshipCustom"),
                        placeholder = "সম্পর্ক বাংলায় লিখুন",
                        testTag = "input_custom_relationship"
                    )
                }

                Spacer(Modifier.height(10.dp))

                // NID (Digits Only)
                OutlinedTextField(
                    value = formState.guarantorNid,
                    onValueChange = { viewModel.updateGuarantorNid(it) },
                    label = { Text("ভোটার আইডি নং (NID) *") },
                    placeholder = { Text("জাতীয় পরিচয়পত্র নম্বর (শুধুমাত্র সংখ্যা)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = formState.validationErrors.contains("guarantorNid"),
                    supportingText = {
                        if (formState.validationErrors.contains("guarantorNid")) {
                            Text("ভোটার আইডি নম্বর দিন (সংখ্যা ছাড়া অন্য কিছু নয়)", color = AlertRed)
                        } else {
                            Text("শুধুমাত্র সংখ্যা লিখুন", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_guarantor_nid")
                )

                Spacer(Modifier.height(14.dp))

                // Checkbox: জামিনদার ও কর্মীর ঠিকানা এক
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (formState.sameAddress) LightGreenContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.toggleSameAddress(!formState.sameAddress) }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = formState.sameAddress,
                            onCheckedChange = { viewModel.toggleSameAddress(it) },
                            modifier = Modifier.testTag("checkbox_same_address")
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "জামিনদার ও কর্মীর ঠিকানা একই (টিক দিন)",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                    }
                }

                if (!formState.sameAddress) {
                    Spacer(Modifier.height(14.dp))
                    Text(
                        "জামিনদারের বর্তমান ঠিকানা",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(8.dp))

                    // জেলা
                    ValidatedBanglaTextField(
                        label = "জেলা *",
                        value = formState.guarantorDistrict,
                        onValueChange = { viewModel.updateGuarantorDistrict(it) },
                        hasError = formState.validationErrors.contains("guarantorDistrict"),
                        placeholder = "জেলার নাম লিখুন",
                        leadingIcon = {
                            Icon(
                                Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        },
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
                        leadingIcon = {
                            Icon(
                                Icons.Default.Place,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        },
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
                        leadingIcon = {
                            Icon(
                                Icons.Default.MarkunreadMailbox,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        },
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
                        leadingIcon = {
                            Icon(
                                Icons.Default.Home,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        testTag = "input_guarantor_village"
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        // Submit Button
        Button(
            onClick = {
                viewModel.submitForm(isAdmin = formState.isEditingByAdmin)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("submit_agreement_form"),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (formState.isEditing) LightGreenDark else MaterialTheme.colorScheme.primary
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(if (formState.isEditing) Icons.Default.SaveAs else Icons.Default.Save, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(
                if (formState.isEditing) "সংশোধন সংরক্ষণ ও পুনরায় শেয়ার করুন" else "সংরক্ষণ ও জামানতনামা তৈরি করুন",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        if (onCancelEdit != null) {
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

        Spacer(Modifier.height(30.dp))
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
    leadingIcon: @Composable (() -> Unit)? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        leadingIcon = leadingIcon,
        singleLine = true,
        isError = hasError,
        supportingText = {
            if (hasError) {
                Text("এই তথ্যটি পূরণ করা আবশ্যক (বাংলায়)", color = AlertRed)
            }
        },
        colors = OutlinedTextFieldDefaults.colors(
            errorBorderColor = AlertRed,
            errorLabelColor = AlertRed
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownSelector(
    items: List<String>,
    selectedItem: String,
    onItemSelected: (String) -> Unit,
    testTag: String
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedItem,
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
                .testTag(testTag)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            items.forEach { item ->
                DropdownMenuItem(
                    text = { Text(item) },
                    onClick = {
                        onItemSelected(item)
                        expanded = false
                    }
                )
            }
        }
    }
}

