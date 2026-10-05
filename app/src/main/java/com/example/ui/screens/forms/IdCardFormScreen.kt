package com.example.ui.screens.forms

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.IdCardFormState
import com.example.ui.AppScreen
import com.example.ui.MainViewModel
import com.example.ui.components.AppFormDropdown
import com.example.ui.components.AppFormTextField
import com.example.ui.components.AutoFilledBlockedTextField
import com.example.ui.components.FormLinkHeader
import com.example.ui.components.FormSavedAdvanceDialog
import com.example.ui.components.ResetAllFormsButton
import com.example.ui.components.StampThemed3DCard
import com.example.util.AgreementFormsPdfGenerator
import com.example.util.BanglaAddressHelper
import com.example.util.BanglaTextValidator
import com.example.util.ShareHelper
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IdCardFormScreen(
    viewModel: MainViewModel,
    formState: IdCardFormState,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()
    val stampState by viewModel.formState.collectAsState()

    var state by remember(formState) { mutableStateOf(formState) }
    var savedPdfFile by remember { mutableStateOf<File?>(null) }
    var showSavedDialog by remember { mutableStateOf(false) }
    var showValidationErrorDialog by remember { mutableStateOf(false) }
    var attemptedSave by remember { mutableStateOf(false) }

    val designations = listOf("Officer (Accounts)", "Officer (loan)", "Service Staff", "Others")
    var designationExpanded by remember { mutableStateOf(false) }

    val bloodGroups = listOf("A+", "A-", "B+", "B-", "O+", "O-", "AB+", "AB-")
    var bloodGroupExpanded by remember { mutableStateOf(false) }

    // Auto-prefill / sync address and designation from stamp state if empty
    LaunchedEffect(formState, stampState) {
        val dist = formState.district.ifBlank { BanglaAddressHelper.getEnglishDistrict(stampState.employeeDistrict) }
        val thana = formState.thana
        val po = formState.postOffice
        val vill = formState.village

        val empName = formState.employeeName.ifBlank {
            if (stampState.employeeName.isNotBlank()) BanglaAddressHelper.transliterateBanglaToEnglish(stampState.employeeName) else ""
        }
        val father = formState.fatherName.ifBlank {
            if (stampState.employeeFatherName.isNotBlank()) BanglaAddressHelper.transliterateBanglaToEnglish(stampState.employeeFatherName) else ""
        }

        val mappedDesig = formState.designation
        val customDes = formState.customDesignation

        val fullAddr = if (formState.address.isNotBlank()) formState.address else buildString {
            if (vill.isNotBlank()) append("Village- $vill, ")
            if (po.isNotBlank()) append("Post- $po, ")
            if (thana.isNotBlank()) append("Thana- $thana, ")
            if (dist.isNotBlank()) append("District- $dist")
        }.trim().removeSuffix(",")

        if (formState.employeeName.isBlank() && formState.pinCode.isBlank()) {
            savedPdfFile = null
        }
        state = formState.copy(
            employeeName = empName,
            fatherName = father,
            district = dist,
            thana = thana,
            postOffice = po,
            village = vill,
            address = fullAddr,
            designation = mappedDesig,
            customDesignation = customDes
        )
    }

    fun updateAddressFields(
        district: String = state.district,
        thana: String = state.thana,
        postOffice: String = state.postOffice,
        village: String = state.village
    ) {
        val fullAddr = buildString {
            if (village.isNotBlank()) append("Village- $village, ")
            if (postOffice.isNotBlank()) append("Post- $postOffice, ")
            if (thana.isNotBlank()) append("Thana- $thana, ")
            if (district.isNotBlank()) append("District- $district")
        }.trim().removeSuffix(",")

        state = state.copy(
            district = district,
            thana = thana,
            postOffice = postOffice,
            village = village,
            address = fullAddr
        )
        viewModel.updateIdCardForm(state)
    }

    val isEmpNameAutoFilled = stampState.employeeName.isNotBlank() && state.employeeName.isNotBlank()
    val isFatherAutoFilled = stampState.employeeFatherName.isNotBlank() && state.fatherName.isNotBlank()

    val effectiveDesig = if (state.designation == "অন্যান্য") state.customDesignation else state.designation

    val hasEmptyMandatory = state.employeeName.isBlank() ||
            state.fatherName.isBlank() ||
            state.motherName.isBlank() ||
            state.district.isBlank() ||
            state.thana.isBlank() ||
            state.postOffice.isBlank() ||
            state.village.isBlank() ||
            state.designation.isBlank() ||
            (state.designation == "অন্যান্য" && state.customDesignation.isBlank()) ||
            state.bloodGroup.isBlank() ||
            state.mobileNumber.isBlank() ||
            state.mobileNumber.length != 11

    fun saveAndGeneratePdf() {
        keyboardController?.hide()
        focusManager.clearFocus()
        attemptedSave = true
        if (hasEmptyMandatory) {
            showValidationErrorDialog = true
            return
        }
        viewModel.updateIdCardForm(state)
        val pdf = AgreementFormsPdfGenerator.generateIdCardFormPdf(context, state)
        AgreementFormsPdfGenerator.generateAllMergedAgreementFormsPdf(context, viewModel.allAgreementForms.value)
        savedPdfFile = pdf
        showSavedDialog = true
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "RRF HR",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "কর্মীর আইডি কার্ডের তথ্য",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("btn_back_idcard")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "ফিরে যান", tint = MaterialTheme.colorScheme.primary)
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            viewModel.updateIdCardForm(state)
                            val pdf = AgreementFormsPdfGenerator.generateIdCardFormPdf(context, state)
                            ShareHelper.openFile(context, pdf)
                        },
                        modifier = Modifier.testTag("btn_preview_idcard_top")
                    ) {
                        Icon(Icons.Default.Visibility, contentDescription = "প্রিভিউ", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("প্রিভিউ", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    keyboardController?.hide()
                    focusManager.clearFocus()
                    saveAndGeneratePdf()
                },
                icon = { Icon(Icons.Default.Save, contentDescription = "সেভ করুন") },
                text = {
                    Text(
                        text = "সেভ করুন ➔",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                elevation = FloatingActionButtonDefaults.elevation(8.dp),
                modifier = Modifier.testTag("fab_save_idcard")
            )
        },
        floatingActionButtonPosition = FabPosition.End
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
            // Form Link Header connecting all 7 forms
            FormLinkHeader(
                currentScreen = AppScreen.FORM_ID_CARD,
                viewModel = viewModel,
                onNavigate = { viewModel.updateScreen(it) }
            )

            // Notice Banner
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = "এটি আইডি কার্ডের ফরম। তথ্যসমূহ ইংরেজি অক্ষরে পূরণ করুন। যোগদানের পরের তথ্যসমূহ পিডিএফ-এ অফিসিয়ালি থাকবে।",
                        fontSize = 12.5.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 17.sp
                    )
                }
            }

            // Top Reset Box: সমস্ত তথ্য মুছুন (Attractive Red Design)
            var showResetConfirmDialog by remember { mutableStateOf(false) }
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = if (isSystemInDarkTheme()) Color(0xFF3F0B0B).copy(alpha = 0.6f) else Color(0xFFFEF2F2),
                border = BorderStroke(1.5.dp, Color(0xFFEF4444)),
                shadowElevation = 2.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showResetConfirmDialog = true }
                    .testTag("btn_reset_all_forms_box")
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp, horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "সমস্ত তথ্য মুছুন",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFDC2626),
                        letterSpacing = 0.3.sp
                    )
                }
            }

            if (showResetConfirmDialog) {
                AlertDialog(
                    onDismissRequest = { showResetConfirmDialog = false },
                    title = { Text("সকল তথ্য রিসেট করতে চান?", fontWeight = FontWeight.Bold) },
                    text = { Text("এটি করলে আপনার পূরণকৃত সকল ফরমের তথ্য ও সেভ করা পিডিএফ ফাইল মুছে যাবে এবং প্রথম থেকে শুরু হবে।") },
                    confirmButton = {
                        Button(
                            onClick = {
                                showResetConfirmDialog = false
                                viewModel.resetAllFormsAndPdfs()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                        ) {
                            Text("হ্যাঁ, রিসেট করুন")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showResetConfirmDialog = false }) {
                            Text("বাতিল")
                        }
                    }
                )
            }

            // Employee Personal Details - 3D Stamp Form Theme Card
            StampThemed3DCard(
                badgeText = "১",
                title = "Employee Personal Details",
                subtitle = "Staff Information for ID Card (English Form)",
                tag = "আবশ্যক"
            ) {

                    // 01. Employee Name
                    AutoFilledBlockedTextField(
                        value = state.employeeName,
                        onValueChange = {
                            val v = BanglaTextValidator.filterEnglishTitleCaseText(it)
                            state = state.copy(employeeName = v)
                            viewModel.updateIdCardForm(state.copy(employeeName = v))
                        },
                        label = "01. Employee Name *",
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                        isAutoFilled = isEmpNameAutoFilled,
                        isError = attemptedSave && state.employeeName.isBlank(),
                        modifier = Modifier.testTag("input_id_emp_name")
                    )

                    // 02. Father's Name
                    AutoFilledBlockedTextField(
                        value = state.fatherName,
                        onValueChange = {
                            val v = BanglaTextValidator.filterEnglishTitleCaseText(it)
                            state = state.copy(fatherName = v)
                            viewModel.updateIdCardForm(state.copy(fatherName = v))
                        },
                        label = "02. Father's Name *",
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                        isAutoFilled = isFatherAutoFilled,
                        isError = attemptedSave && state.fatherName.isBlank(),
                        modifier = Modifier.testTag("input_id_father_name")
                    )

                    // 03. Mother's Name
                    AppFormTextField(
                        value = state.motherName,
                        onValueChange = {
                            val v = BanglaTextValidator.filterEnglishTitleCaseText(it)
                            state = state.copy(motherName = v)
                            viewModel.updateIdCardForm(state.copy(motherName = v))
                        },
                        label = "03. Mother's Name *",
                        placeholder = "Enter Mother's Name",
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                        isError = attemptedSave && state.motherName.isBlank(),
                        modifier = Modifier.fillMaxWidth().testTag("input_id_mother_name")
                    )

                    // 04. Address Fields Boxed Sequentially (জেলা, থানা, পোস্ট, গ্রাম)
                    Text(
                        text = "04. Address Details (ঠিকানা ক্রমন্বয়ে পূরণ করুন) *",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    // 1. জেলা (District)
                    AppFormTextField(
                        value = state.district,
                        onValueChange = {
                            val v = BanglaTextValidator.filterEnglishTitleCaseText(it)
                            updateAddressFields(district = v)
                        },
                        label = "১. জেলা (District) *",
                        placeholder = "District name",
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                        isError = attemptedSave && state.district.isBlank(),
                        modifier = Modifier.fillMaxWidth().testTag("input_id_district")
                    )

                    // 2. থানা/উপজেলা (Thana / Upazila)
                    AppFormTextField(
                        value = state.thana,
                        onValueChange = {
                            val v = BanglaTextValidator.filterEnglishTitleCaseText(it)
                            updateAddressFields(thana = v)
                        },
                        label = "২. থানা / উপজেলা (Thana / Upazila) *",
                        placeholder = "Thana / Upazila name",
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                        isError = attemptedSave && state.thana.isBlank(),
                        modifier = Modifier.fillMaxWidth().testTag("input_id_thana")
                    )

                    // 3. পোস্ট অফিস (Post Office)
                    AppFormTextField(
                        value = state.postOffice,
                        onValueChange = {
                            val v = BanglaTextValidator.filterEnglishTitleCaseText(it)
                            updateAddressFields(postOffice = v)
                        },
                        label = "৩. পোস্ট অফিস (Post Office) *",
                        placeholder = "Post Office name",
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                        isError = attemptedSave && state.postOffice.isBlank(),
                        modifier = Modifier.fillMaxWidth().testTag("input_id_post_office")
                    )

                    // 4. গ্রাম/মহল্লা/বাড়ি নং (Village / Road / House)
                    AppFormTextField(
                        value = state.village,
                        onValueChange = {
                            val v = BanglaTextValidator.filterEnglishTitleCaseText(it)
                            updateAddressFields(village = v)
                        },
                        label = "৪. গ্রাম / মহল্লা / বাড়ি নং (Village / Road) *",
                        placeholder = "Village / Road / House",
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                        isError = attemptedSave && state.village.isBlank(),
                        modifier = Modifier.fillMaxWidth().testTag("input_id_village")
                    )

                    // 05. Designation Dropdown
                    AppFormDropdown(
                        value = state.designation,
                        onValueChange = { desig ->
                            state = state.copy(
                                designation = desig,
                                customDesignation = if (desig == "Others") state.customDesignation else ""
                            )
                            viewModel.updateIdCardForm(state)
                        },
                        label = "05. Designation (পদবী) *",
                        options = designations,
                        placeholder = "Select Designation",
                        isError = attemptedSave && state.designation.isBlank(),
                        modifier = Modifier.fillMaxWidth().testTag("input_id_designation_dropdown")
                    )

                    // If "Others" is selected, show text box for custom designation
                    if (state.designation == "Others" || state.designation == "others" || state.designation == "অন্যান্য") {
                        AppFormTextField(
                            value = state.customDesignation,
                            onValueChange = {
                                val v = BanglaTextValidator.filterEnglishText(it)
                                state = state.copy(customDesignation = v)
                                viewModel.updateIdCardForm(state)
                            },
                            label = "Write Custom Designation (অন্যান্য পদবীর নাম) *",
                            placeholder = "Custom designation",
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                            isError = attemptedSave && state.customDesignation.isBlank(),
                            modifier = Modifier.fillMaxWidth().testTag("input_id_custom_designation")
                        )
                    }

                    // 06. Blood Group Dropdown
                    AppFormDropdown(
                        value = state.bloodGroup,
                        onValueChange = { bg ->
                            state = state.copy(bloodGroup = bg)
                            viewModel.updateIdCardForm(state.copy(bloodGroup = bg))
                        },
                        label = "06. Blood Group *",
                        options = bloodGroups,
                        placeholder = "Select Blood Group",
                        isError = attemptedSave && state.bloodGroup.isBlank(),
                        modifier = Modifier.fillMaxWidth().testTag("input_id_bloodgroup")
                    )

                    // 07. Mobile Number
                    AppFormTextField(
                        value = state.mobileNumber,
                        onValueChange = {
                            val v = BanglaTextValidator.filterMobileInput(it)
                            state = state.copy(mobileNumber = v)
                            viewModel.updateIdCardForm(state.copy(mobileNumber = v))
                        },
                        label = "07. Mobile Number * (১১ ডিজিট)",
                        placeholder = "01XXXXXXXXX",
                        isError = attemptedSave && (state.mobileNumber.isBlank() || state.mobileNumber.length != 11),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth().testTag("input_id_mobile")
                    )

                    // 08. E-mail Address (Optional)
                    AppFormTextField(
                        value = state.emailAddress,
                        onValueChange = {
                            val v = BanglaTextValidator.filterEmailAddress(it)
                            state = state.copy(emailAddress = v)
                            viewModel.updateIdCardForm(state.copy(emailAddress = v))
                        },
                        label = "08. E-mail Address (ঐচ্ছিক)",
                        placeholder = "example@gmail.com (যদি থাকে)",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, capitalization = KeyboardCapitalization.None),
                        modifier = Modifier.fillMaxWidth().testTag("input_id_email")
                    )
                    Text(
                        text = "নোট: ইমেইল ঠিকানা ঐচ্ছিক (যদি থাকে)",
                        fontSize = 11.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
            }

            Spacer(Modifier.height(8.dp))
            Text(
                text = "আবশ্যকীয় তথ্য পূরণ করে সেভ করুন।",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFDC2626),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(10.dp))

            Spacer(Modifier.height(240.dp))
            }
        }
    }

    if (showSavedDialog) {
        FormSavedAdvanceDialog(
            formTitle = "১. কর্মীর আইডি কার্ডের তথ্য",
            nextScreenTitle = "২. প্রশিক্ষণ তথ্য ও অঙ্গীকারনামা",
            onGoToNext = {
                showSavedDialog = false
                viewModel.navigateToNextForm(AppScreen.FORM_ID_CARD)
            },
            onViewPdf = {
                showSavedDialog = false
                savedPdfFile?.let { ShareHelper.openFile(context, it) }
            },
            onDismiss = { showSavedDialog = false }
        )
    }

    if (showValidationErrorDialog) {
        AlertDialog(
            onDismissRequest = { showValidationErrorDialog = false },
            icon = {
                Icon(Icons.Default.Error, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(32.dp))
            },
            title = {
                Text("তথ্য অসম্পূর্ণ", fontWeight = FontWeight.Bold, color = Color(0xFFDC2626))
            },
            text = {
                Text(
                    text = "আইডি কার্ড ফরমের লাল চিহ্নিত আবশ্যিক ঘরগুলো সঠিকভাবে পূরণ করুন।\n(যেমন: নাম, পিতা, মাতা, ঠিকানা (জেলা, থানা, পোস্ট, গ্রাম), পদবী, রক্তের গ্রুপ, মোবাইল ও ইমেইল)",
                    fontSize = 13.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = { showValidationErrorDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669))
                ) {
                    Text("ঠিক আছে")
                }
            }
        )
    }
}
