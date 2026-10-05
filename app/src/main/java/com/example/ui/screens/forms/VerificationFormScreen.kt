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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.VerificationFormState
import com.example.ui.AppScreen
import com.example.ui.MainViewModel
import com.example.ui.components.AppDateField
import com.example.ui.components.AppFormDropdown
import com.example.ui.components.AppFormTextField
import com.example.ui.components.AutoFilledBlockedTextField
import com.example.ui.components.FormLinkHeader
import com.example.ui.components.FormSavedAdvanceDialog
import com.example.ui.components.StampThemed3DCard
import com.example.util.AgreementFormsPdfGenerator
import com.example.util.BanglaTextValidator
import com.example.util.ShareHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerificationFormScreen(
    viewModel: MainViewModel,
    formState: VerificationFormState,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val isDarkVer = isSystemInDarkTheme()
    val scrollState = rememberScrollState()
    val stampState by viewModel.formState.collectAsState()
    val allAgreementForms by viewModel.allAgreementForms.collectAsState()

    var state by remember(formState) { mutableStateOf(formState) }
    var showSavedDialog by remember { mutableStateOf(false) }
    var showValidationErrorDialog by remember { mutableStateOf(false) }
    var attemptedSave by remember { mutableStateOf(false) }

    LaunchedEffect(formState, stampState, allAgreementForms) {
        state = formState
        val autoName = if (allAgreementForms.personalInfoForm.employeeNameBangla.isNotBlank()) {
            allAgreementForms.personalInfoForm.employeeNameBangla
        } else if (stampState.employeeName.isNotBlank()) {
            stampState.employeeName
        } else if (allAgreementForms.trainingForm.employeeName.isNotBlank()) {
            allAgreementForms.trainingForm.employeeName
        } else {
            allAgreementForms.idCardForm.employeeName
        }

        val autoFather = if (allAgreementForms.personalInfoForm.fatherName.isNotBlank()) {
            allAgreementForms.personalInfoForm.fatherName
        } else if (stampState.employeeFatherName.isNotBlank()) {
            stampState.employeeFatherName
        } else {
            allAgreementForms.trainingForm.fatherName
        }

        val autoUnion = allAgreementForms.personalInfoForm.unionOrPouroshovaName
        val todayDate = BanglaTextValidator.toBanglaDigits(java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.US).format(java.util.Date()))

        var updated = state
        if (autoName.isNotBlank() && (updated.staffFullName.isBlank() || updated.staffFullName.length < 3 || allAgreementForms.personalInfoForm.employeeNameBangla.isNotBlank())) {
            updated = updated.copy(staffFullName = autoName)
        }
        if (autoFather.isNotBlank() && (updated.staffFatherName.isBlank() || updated.staffFatherName.length < 3 || allAgreementForms.personalInfoForm.fatherName.isNotBlank())) {
            updated = updated.copy(staffFatherName = autoFather)
        }
        if (updated.unionName.isBlank() && autoUnion.isNotBlank()) updated = updated.copy(unionName = autoUnion)
        if (updated.staffSignatureDate.isBlank()) updated = updated.copy(staffSignatureDate = todayDate)

        if (updated != state) {
            state = updated
            viewModel.updateVerificationForm(updated)
        }
    }

    val updateField: (VerificationFormState) -> Unit = { newState ->
        state = newState
        viewModel.updateVerificationForm(newState)
    }

    val isEmpNameAutoFilled = stampState.employeeName.isNotBlank() && state.staffFullName == stampState.employeeName
    val isFatherAutoFilled = stampState.employeeFatherName.isNotBlank() && state.staffFatherName == stampState.employeeFatherName

    val hasEmptyMandatory = state.staffFullName.isBlank() ||
            state.staffFatherName.isBlank() ||
            state.staffSignatureDate.isBlank() ||
            state.neigh1Name.isBlank() ||
            state.neigh1Profession.isBlank() ||
            state.neigh1Father.isBlank() ||
            state.neigh1Mobile.isBlank() ||
            state.neigh1Village.isBlank() ||
            state.neigh1PostOffice.isBlank() ||
            state.neigh1Postal.isBlank() ||
            state.neigh1Upazila.isBlank() ||
            state.neigh1Police.isBlank() ||
            state.neigh1District.isBlank() ||
            state.neigh1Nid.isBlank() ||
            state.neigh2Name.isBlank() ||
            state.neigh2Profession.isBlank() ||
            state.neigh2Father.isBlank() ||
            state.neigh2Mobile.isBlank() ||
            state.neigh2Village.isBlank() ||
            state.neigh2PostOffice.isBlank() ||
            state.neigh2Postal.isBlank() ||
            state.neigh2Upazila.isBlank() ||
            state.neigh2Police.isBlank() ||
            state.neigh2District.isBlank() ||
            state.neigh2Nid.isBlank()

    fun saveAndGeneratePdf() {
        keyboardController?.hide()
        focusManager.clearFocus()
        attemptedSave = true
        if (hasEmptyMandatory) {
            showValidationErrorDialog = true
            return
        }
        viewModel.updateVerificationForm(state)
        AgreementFormsPdfGenerator.generateVerificationFormPdf(context, state, viewModel.allAgreementForms.value)
        AgreementFormsPdfGenerator.generateAllMergedAgreementFormsPdf(context, viewModel.allAgreementForms.value)
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
                            text = "তথ্য যাচাই ফরম (Verification)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("btn_back_verification")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "ফিরে যান", tint = MaterialTheme.colorScheme.primary)
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            viewModel.updateVerificationForm(state)
                            val pdf = AgreementFormsPdfGenerator.generateVerificationFormPdf(context, state, viewModel.allAgreementForms.value)
                            ShareHelper.shareFile(context, pdf, "তথ্য যাচাই ফরম (A4 PDF)")
                        }
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "শেয়ার PDF", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(
                        onClick = {
                            viewModel.updateVerificationForm(state)
                            val pdf = AgreementFormsPdfGenerator.generateVerificationFormPdf(context, state, viewModel.allAgreementForms.value)
                            ShareHelper.openFile(context, pdf)
                        }
                    ) {
                        Icon(Icons.Default.Print, contentDescription = "প্রিন্ট PDF", tint = MaterialTheme.colorScheme.primary)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { saveAndGeneratePdf() },
                icon = { Icon(Icons.Default.Save, contentDescription = "সেভ করুন") },
                text = {
                    Text(
                        text = "সেভ করুন",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                elevation = FloatingActionButtonDefaults.elevation(8.dp),
                modifier = Modifier.testTag("fab_save_verification")
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
                currentScreen = AppScreen.FORM_VERIFICATION,
                viewModel = viewModel,
                onNavigate = { viewModel.updateScreen(it) }
            )

            // Section 1: RRF Relatives (Relative 1 & 2) - Stamp 3D Theme Card
            StampThemed3DCard(
                badgeText = "১",
                title = "আরআরএফ-এ কর্মরত পরিচিত/আত্মীয় (২ জন)",
                subtitle = "যদি পরিচিত/আত্মীয় থাকে তবে পূরণ করুন (ঐচ্ছিক)",
                tag = "ঐচ্ছিক"
            ) {
                    val isDarkVer = isSystemInDarkTheme()
                    Text(
                        text = "নোট: যদি পরিচিত/আত্মীয় থাকে তবে পূরণ করুন। যদি না থাকে তবে টিক দিন।",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isDarkVer) Color(0xFFC084FC) else Color(0xFF6B21A8)
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val newChecked = !state.noRrfRelative
                                updateField(
                                    state.copy(
                                        noRrfRelative = newChecked,
                                        rel1Name = if (newChecked) "" else state.rel1Name,
                                        rel2Name = if (newChecked) "" else state.rel2Name
                                    )
                                )
                            }
                            .padding(vertical = 4.dp)
                    ) {
                        Checkbox(
                            checked = state.noRrfRelative,
                            onCheckedChange = { checked ->
                                updateField(
                                    state.copy(
                                        noRrfRelative = checked,
                                        rel1Name = if (checked) "" else state.rel1Name,
                                        rel2Name = if (checked) "" else state.rel2Name
                                    )
                                )
                            }
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "আরআরএফ-এ আমার কোনো পরিচিত/আত্মীয় কর্মরত নেই (I have none)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (state.noRrfRelative) (if (isDarkVer) Color(0xFFA78BFA) else Color(0xFF7C3AED)) else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    if (!state.noRrfRelative) {
                        val designationOptions = listOf(
                            "পরিচালক",
                            "উপ-পরিচালক",
                            "সহকারী পরিচালক",
                            "আঞ্চলিক ব্যবস্থাপক",
                            "শাখা ব্যবস্থাপক",
                            "অফিসার (অ্যাকাউন্টস)",
                            "অফিসার (ঋণ)",
                            "অন্যান্য"
                        )

                        val relationshipOptions = listOf(
                            "পিতা",
                            "মাতা",
                            "বোন",
                            "ভাই",
                            "বন্ধু",
                            "চাচা/মামা/ফুফা",
                            "অন্যান্য"
                        )

                        // Relative 1
                        Text(text = "পরিচিত/আত্মীয় - ১", fontWeight = FontWeight.SemiBold, color = if (isDarkVer) Color(0xFFC084FC) else Color(0xFF4C1D95))
                        AppFormTextField(
                            value = state.rel1Name,
                            onValueChange = { updateField(state.copy(rel1Name = BanglaTextValidator.filterBengaliWithPunctuation(it))) },
                            label = "কর্মীর নাম",
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            AppFormTextField(
                                value = state.rel1Father,
                                onValueChange = { updateField(state.copy(rel1Father = BanglaTextValidator.filterBengaliWithPunctuation(it))) },
                                label = "পিতার নাম",
                                modifier = Modifier.weight(1f)
                            )

                            // Rel 1 Relationship Dropdown
                            AppFormDropdown(
                                value = state.rel1Relationship,
                                onValueChange = { updateField(state.copy(rel1Relationship = it)) },
                                label = "সম্পর্ক",
                                options = relationshipOptions,
                                placeholder = "সম্পর্ক নির্বাচন",
                                modifier = Modifier.weight(1f)
                            )
                        }

                        if (state.rel1Relationship == "অন্যান্য") {
                            AppFormTextField(
                                value = state.rel1RelationshipCustom,
                                onValueChange = { updateField(state.copy(rel1RelationshipCustom = BanglaTextValidator.filterBengaliWithPunctuation(it))) },
                                label = "অন্যান্য সম্পর্ক লিখুন (বাংলায়)",
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            AppFormTextField(
                                value = state.rel1Pin,
                                onValueChange = { updateField(state.copy(rel1Pin = BanglaTextValidator.filterDigitsOnly(it))) },
                                label = "পিন নং (শুধুমাত্র সংখ্যা)",
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )

                            // Rel 1 Designation Dropdown
                            AppFormDropdown(
                                value = state.rel1Designation,
                                onValueChange = { updateField(state.copy(rel1Designation = it)) },
                                label = "পদবী",
                                options = designationOptions,
                                placeholder = "পদবী নির্বাচন",
                                modifier = Modifier.weight(1.5f)
                            )
                        }

                        if (state.rel1Designation == "অন্যান্য") {
                            AppFormTextField(
                                value = state.rel1DesignationCustom,
                                onValueChange = { updateField(state.copy(rel1DesignationCustom = BanglaTextValidator.filterBengaliWithPunctuation(it))) },
                                label = "অন্যান্য পদবী লিখুন (বাংলায়)",
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        AppFormTextField(
                            value = state.rel1Workplace,
                            onValueChange = { updateField(state.copy(rel1Workplace = BanglaTextValidator.filterBengaliWithPunctuation(it))) },
                            label = "বর্তমান কর্মস্থল",
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(Modifier.height(8.dp))
                        HorizontalDivider()
                        Spacer(Modifier.height(8.dp))

                        // Relative 2
                        Text(text = "পরিচিত/আত্মীয় - ২", fontWeight = FontWeight.SemiBold, color = if (isDarkVer) Color(0xFFC084FC) else Color(0xFF4C1D95))
                        AppFormTextField(
                            value = state.rel2Name,
                            onValueChange = { updateField(state.copy(rel2Name = BanglaTextValidator.filterBengaliWithPunctuation(it))) },
                            label = "কর্মীর নাম",
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            AppFormTextField(
                                value = state.rel2Father,
                                onValueChange = { updateField(state.copy(rel2Father = BanglaTextValidator.filterBengaliWithPunctuation(it))) },
                                label = "পিতার নাম",
                                modifier = Modifier.weight(1f)
                            )

                            // Rel 2 Relationship Dropdown
                            AppFormDropdown(
                                value = state.rel2Relationship,
                                onValueChange = { updateField(state.copy(rel2Relationship = it)) },
                                label = "সম্পর্ক",
                                options = relationshipOptions,
                                placeholder = "সম্পর্ক নির্বাচন",
                                modifier = Modifier.weight(1f)
                            )
                        }

                        if (state.rel2Relationship == "অন্যান্য") {
                            AppFormTextField(
                                value = state.rel2RelationshipCustom,
                                onValueChange = { updateField(state.copy(rel2RelationshipCustom = BanglaTextValidator.filterBengaliWithPunctuation(it))) },
                                label = "অন্যান্য সম্পর্ক লিখুন (বাংলায়)",
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            AppFormTextField(
                                value = state.rel2Pin,
                                onValueChange = { updateField(state.copy(rel2Pin = BanglaTextValidator.filterDigitsOnly(it))) },
                                label = "পিন নং (শুধুমাত্র সংখ্যা)",
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )

                            // Rel 2 Designation Dropdown
                            AppFormDropdown(
                                value = state.rel2Designation,
                                onValueChange = { updateField(state.copy(rel2Designation = it)) },
                                label = "পদবী",
                                options = designationOptions,
                                placeholder = "পদবী নির্বাচন",
                                modifier = Modifier.weight(1.5f)
                            )
                        }

                        if (state.rel2Designation == "অন্যান্য") {
                            AppFormTextField(
                                value = state.rel2DesignationCustom,
                                onValueChange = { updateField(state.copy(rel2DesignationCustom = BanglaTextValidator.filterBengaliWithPunctuation(it))) },
                                label = "অন্যান্য পদবী লিখুন (বাংলায়)",
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        AppFormTextField(
                            value = state.rel2Workplace,
                            onValueChange = { updateField(state.copy(rel2Workplace = BanglaTextValidator.filterBengaliWithPunctuation(it))) },
                            label = "বর্তমান কর্মস্থল",
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
            }

            // Section 2: Chairman Information - Stamp 3D Theme Card
            StampThemed3DCard(
                badgeText = "২",
                title = "ইউনিয়ন পরিষদ/পৌরসভার চেয়ারম্যান সংক্রান্ত",
                subtitle = "চেয়ারম্যানের বিবরণ ও প্রত্যয়ন",
                tag = "ঐচ্ছিক"
            ) {
                    AppFormTextField(
                        value = state.chairmanName,
                        onValueChange = { updateField(state.copy(chairmanName = BanglaTextValidator.filterBengaliWithPunctuation(it))) },
                        label = "চেয়ারম্যানের নাম (ঐচ্ছিক)",
                        modifier = Modifier.fillMaxWidth()
                    )
                    AppFormTextField(
                        value = state.unionName,
                        onValueChange = { updateField(state.copy(unionName = BanglaTextValidator.filterBengaliWithPunctuation(it))) },
                        label = "ইউনিয়ন পরিষদ/পৌরসভার নাম (স্বয়ংক্রিয়)",
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Text(
                        text = "চেয়ারম্যানের ঠিকানা (গ্রাম, ডাকঘর, থানা, জেলা)",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        AppFormTextField(
                            value = state.chairmanVillage,
                            onValueChange = { updateField(state.copy(chairmanVillage = BanglaTextValidator.filterBengaliWithPunctuation(it))) },
                            label = "গ্রাম",
                            modifier = Modifier.weight(1f)
                        )
                        AppFormTextField(
                            value = state.chairmanPostOffice,
                            onValueChange = { updateField(state.copy(chairmanPostOffice = BanglaTextValidator.filterBengaliWithPunctuation(it))) },
                            label = "ডাকঘর",
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        AppFormTextField(
                            value = state.chairmanThana,
                            onValueChange = { updateField(state.copy(chairmanThana = BanglaTextValidator.filterBengaliWithPunctuation(it))) },
                            label = "থানা / উপজেলা",
                            modifier = Modifier.weight(1f)
                        )
                        AppFormTextField(
                            value = state.chairmanDistrict,
                            onValueChange = { updateField(state.copy(chairmanDistrict = BanglaTextValidator.filterBengaliWithPunctuation(it))) },
                            label = "জেলা",
                            modifier = Modifier.weight(1f)
                        )
                    }
            }

            // Section 3: Undertaking (মুচলেকা) & Staff - Stamp 3D Theme Card
            StampThemed3DCard(
                badgeText = "৩",
                title = "মুচলেকা (কর্মী কর্তৃক পূরণীয়)",
                subtitle = "কর্মী ও পিতার পূর্ণ নাম বাংলায় লিখুন",
                tag = "আবশ্যক"
            ) {
                    AppFormTextField(
                        value = state.staffFullName,
                        onValueChange = { updateField(state.copy(staffFullName = BanglaTextValidator.filterBengaliWithPunctuation(it))) },
                        label = "কর্মীর পূর্ণ নাম *",
                        placeholder = "কর্মীর পূর্ণ নাম বাংলায় লিখুন",
                        isError = attemptedSave && state.staffFullName.isBlank(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    AppFormTextField(
                        value = state.staffFatherName,
                        onValueChange = { updateField(state.copy(staffFatherName = BanglaTextValidator.filterBengaliWithPunctuation(it))) },
                        label = "কর্মীর পিতার নাম *",
                        placeholder = "পিতার নাম বাংলায় লিখুন",
                        isError = attemptedSave && state.staffFatherName.isBlank(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    AppDateField(
                        value = state.staffSignatureDate,
                        onValueChange = { updateField(state.copy(staffSignatureDate = it)) },
                        label = "তারিখ *",
                        isError = attemptedSave && state.staffSignatureDate.isBlank(),
                        errorMessage = if (attemptedSave && state.staffSignatureDate.isBlank()) "সঠিক তারিখ (সন/সাল সহ) আবশ্যক" else "",
                        modifier = Modifier.fillMaxWidth()
                    )
            }

            // Section 4: Neighbors / Certifiers - Stamp 3D Theme Card
            StampThemed3DCard(
                badgeText = "৪",
                title = "কর্মীর প্রতিবেশী সংক্রান্ত (২ জন)",
                subtitle = "যাচাইকারী প্রতিবেশী বিবরণ",
                tag = "আবশ্যক"
            ) {
                    Text(
                        text = "নোট: কর্মীর প্রতিবেশী সংক্রান্ত তথ্য এখানে পূরণ করুন",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF047857)
                    )

                    val pInfo = allAgreementForms.personalInfoForm
                    val wVill = pInfo.permVillage.ifBlank { pInfo.currVillageOrHouse }
                    val wPost = pInfo.permPostOffice.ifBlank { pInfo.currPostOffice }
                    val wPostal = pInfo.permPostCode.ifBlank { pInfo.currPostCode }
                    val wUpazila = pInfo.permUpazila.ifBlank { pInfo.currUpazila }
                    val wPolice = pInfo.permThana.ifBlank { pInfo.currThana }
                    val wDistrict = pInfo.permDistrict.ifBlank { pInfo.currDistrict }

                    // Neighbor 1
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "প্রতিবেশী - ১", fontWeight = FontWeight.Bold, color = if (isDarkVer) Color(0xFFC084FC) else Color(0xFF4C1D95))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable {
                                val newCheck = !state.isNeighbor1SameAsWorker
                                if (newCheck) {
                                    updateField(state.copy(
                                        isNeighbor1SameAsWorker = true,
                                        neigh1Village = wVill,
                                        neigh1PostOffice = wPost,
                                        neigh1Postal = wPostal,
                                        neigh1Upazila = wUpazila,
                                        neigh1Police = wPolice,
                                        neigh1District = wDistrict
                                    ))
                                } else {
                                    updateField(state.copy(
                                        isNeighbor1SameAsWorker = false,
                                        neigh1Village = "",
                                        neigh1PostOffice = "",
                                        neigh1Postal = "",
                                        neigh1Upazila = "",
                                        neigh1Police = "",
                                        neigh1District = ""
                                    ))
                                }
                            }
                        ) {
                            Checkbox(
                                checked = state.isNeighbor1SameAsWorker,
                                onCheckedChange = { checked ->
                                    if (checked) {
                                        updateField(state.copy(
                                            isNeighbor1SameAsWorker = true,
                                            neigh1Village = wVill,
                                            neigh1PostOffice = wPost,
                                            neigh1Postal = wPostal,
                                            neigh1Upazila = wUpazila,
                                            neigh1Police = wPolice,
                                            neigh1District = wDistrict
                                        ))
                                    } else {
                                        updateField(state.copy(
                                            isNeighbor1SameAsWorker = false,
                                            neigh1Village = "",
                                            neigh1PostOffice = "",
                                            neigh1Postal = "",
                                            neigh1Upazila = "",
                                            neigh1Police = "",
                                            neigh1District = ""
                                        ))
                                    }
                                }
                            )
                            Text("কর্মীর ঠিকানার অনুরূপ", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                    
                    val neighProfOptions = listOf("সরকারী চাকরি", "শিক্ষক", "ডাক্তার", "ব্যবসায়ী", "অন্যান্য")
                    val isCustomN1Prof = state.neigh1Profession.isNotBlank() && !neighProfOptions.contains(state.neigh1Profession)

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        AppFormTextField(
                            value = state.neigh1Name,
                            onValueChange = { updateField(state.copy(neigh1Name = BanglaTextValidator.filterBengaliWithPunctuation(it))) },
                            label = "প্রতিবেশীর পূর্ণ নাম",
                            isError = attemptedSave && state.neigh1Name.isBlank(),
                            modifier = Modifier.weight(1.5f)
                        )
                        AppFormDropdown(
                            value = if (isCustomN1Prof) "অন্যান্য: ${state.neigh1Profession}" else state.neigh1Profession,
                            onValueChange = { opt ->
                                updateField(state.copy(neigh1Profession = if (opt == "অন্যান্য") "" else opt))
                            },
                            label = "পেশা",
                            options = neighProfOptions,
                            placeholder = "পেশা নির্বাচন",
                            isError = attemptedSave && state.neigh1Profession.isBlank(),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (state.neigh1Profession == "অন্যান্য" || isCustomN1Prof) {
                        AppFormTextField(
                            value = if (state.neigh1Profession == "অন্যান্য") "" else state.neigh1Profession,
                            onValueChange = { updateField(state.copy(neigh1Profession = BanglaTextValidator.filterBengaliWithPunctuation(it))) },
                            label = "অন্যান্য পেশা লিখুন (বাংলায়)",
                            placeholder = "যেমন: প্রকৌশলী / কৃষি",
                            isError = attemptedSave && state.neigh1Profession.isBlank(),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        AppFormTextField(
                            value = state.neigh1Father,
                            onValueChange = { updateField(state.copy(neigh1Father = BanglaTextValidator.filterBengaliWithPunctuation(it))) },
                            label = "পিতার নাম",
                            isError = attemptedSave && state.neigh1Father.isBlank(),
                            modifier = Modifier.weight(1f)
                        )
                        AppFormTextField(
                            value = state.neigh1Mobile,
                            onValueChange = { updateField(state.copy(neigh1Mobile = BanglaTextValidator.filterMobileInput(it))) },
                            label = "মোবাইল নং (১১ ডিজিট)",
                            isError = attemptedSave && (state.neigh1Mobile.isBlank() || state.neigh1Mobile.length != 11),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        AppFormTextField(
                            value = state.neigh1Village,
                            onValueChange = { v ->
                                updateField(state.copy(neigh1Village = BanglaTextValidator.filterBengaliWithPunctuation(v)))
                            },
                            label = "গ্রাম",
                            isError = attemptedSave && state.neigh1Village.isBlank(),
                            modifier = Modifier.weight(1.5f)
                        )
                        AppFormTextField(
                            value = state.neigh1PostOffice,
                            onValueChange = { po ->
                                updateField(state.copy(neigh1PostOffice = BanglaTextValidator.filterBengaliWithPunctuation(po)))
                            },
                            label = "ডাকঘর",
                            isError = attemptedSave && state.neigh1PostOffice.isBlank(),
                            modifier = Modifier.weight(1.2f)
                        )
                        AppFormTextField(
                            value = state.neigh1Postal,
                            onValueChange = { pc ->
                                updateField(state.copy(neigh1Postal = BanglaTextValidator.filterDigitsOnly(pc)))
                            },
                            label = "পোস্ট কোড",
                            isError = attemptedSave && state.neigh1Postal.isBlank(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        AppFormTextField(
                            value = state.neigh1Upazila,
                            onValueChange = { up ->
                                updateField(state.copy(neigh1Upazila = BanglaTextValidator.filterBengaliWithPunctuation(up)))
                            },
                            label = "উপজেলা",
                            isError = attemptedSave && state.neigh1Upazila.isBlank(),
                            modifier = Modifier.weight(1f)
                        )
                        AppFormTextField(
                            value = state.neigh1Police,
                            onValueChange = { th ->
                                updateField(state.copy(neigh1Police = BanglaTextValidator.filterBengaliWithPunctuation(th)))
                            },
                            label = "থানা",
                            isError = attemptedSave && state.neigh1Police.isBlank(),
                            modifier = Modifier.weight(1f)
                        )
                        AppFormTextField(
                            value = state.neigh1District,
                            onValueChange = { dist ->
                                updateField(state.copy(neigh1District = BanglaTextValidator.filterBengaliWithPunctuation(dist)))
                            },
                            label = "জেলা",
                            isError = attemptedSave && state.neigh1District.isBlank(),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    AppFormTextField(
                        value = state.neigh1Nid,
                        onValueChange = { nid ->
                            updateField(state.copy(neigh1Nid = BanglaTextValidator.filterDigitsOnly(nid)))
                        },
                        label = "প্রতিবেশীর জাতীয় পরিচয়পত্র (NID - সংখ্যা)",
                        isError = attemptedSave && state.neigh1Nid.isBlank(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(8.dp))
                    HorizontalDivider()
                    Spacer(Modifier.height(8.dp))

                    // Neighbor 2
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "প্রতিবেশী - ২", fontWeight = FontWeight.Bold, color = if (isDarkVer) Color(0xFFC084FC) else Color(0xFF4C1D95))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable {
                                val newCheck = !state.isNeighbor2SameAsWorker
                                if (newCheck) {
                                    updateField(state.copy(
                                        isNeighbor2SameAsWorker = true,
                                        neigh2Village = wVill,
                                        neigh2PostOffice = wPost,
                                        neigh2Postal = wPostal,
                                        neigh2Upazila = wUpazila,
                                        neigh2Police = wPolice,
                                        neigh2District = wDistrict
                                    ))
                                } else {
                                    updateField(state.copy(
                                        isNeighbor2SameAsWorker = false,
                                        neigh2Village = "",
                                        neigh2PostOffice = "",
                                        neigh2Postal = "",
                                        neigh2Upazila = "",
                                        neigh2Police = "",
                                        neigh2District = ""
                                    ))
                                }
                            }
                        ) {
                            Checkbox(
                                checked = state.isNeighbor2SameAsWorker,
                                onCheckedChange = { checked ->
                                    if (checked) {
                                        updateField(state.copy(
                                            isNeighbor2SameAsWorker = true,
                                            neigh2Village = wVill,
                                            neigh2PostOffice = wPost,
                                            neigh2Postal = wPostal,
                                            neigh2Upazila = wUpazila,
                                            neigh2Police = wPolice,
                                            neigh2District = wDistrict
                                        ))
                                    } else {
                                        updateField(state.copy(
                                            isNeighbor2SameAsWorker = false,
                                            neigh2Village = "",
                                            neigh2PostOffice = "",
                                            neigh2Postal = "",
                                            neigh2Upazila = "",
                                            neigh2Police = "",
                                            neigh2District = ""
                                        ))
                                    }
                                }
                            )
                            Text("কর্মীর ঠিকানার অনুরূপ", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                    
                    val isCustomN2Prof = state.neigh2Profession.isNotBlank() && !neighProfOptions.contains(state.neigh2Profession)

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        AppFormTextField(
                            value = state.neigh2Name,
                            onValueChange = { updateField(state.copy(neigh2Name = BanglaTextValidator.filterBengaliWithPunctuation(it))) },
                            label = "প্রতিবেশীর পূর্ণ নাম",
                            isError = attemptedSave && state.neigh2Name.isBlank(),
                            modifier = Modifier.weight(1.5f)
                        )
                        AppFormDropdown(
                            value = if (isCustomN2Prof) "অন্যান্য: ${state.neigh2Profession}" else state.neigh2Profession,
                            onValueChange = { opt ->
                                updateField(state.copy(neigh2Profession = if (opt == "অন্যান্য") "" else opt))
                            },
                            label = "পেশা",
                            options = neighProfOptions,
                            placeholder = "পেশা নির্বাচন",
                            isError = attemptedSave && state.neigh2Profession.isBlank(),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (state.neigh2Profession == "অন্যান্য" || isCustomN2Prof) {
                        AppFormTextField(
                            value = if (state.neigh2Profession == "অন্যান্য") "" else state.neigh2Profession,
                            onValueChange = { updateField(state.copy(neigh2Profession = BanglaTextValidator.filterBengaliWithPunctuation(it))) },
                            label = "অন্যান্য পেশা লিখুন (বাংলায়)",
                            placeholder = "যেমন: প্রকৌশলী / কৃষি",
                            isError = attemptedSave && state.neigh2Profession.isBlank(),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        AppFormTextField(
                            value = state.neigh2Father,
                            onValueChange = { updateField(state.copy(neigh2Father = BanglaTextValidator.filterBengaliWithPunctuation(it))) },
                            label = "পিতার নাম",
                            isError = attemptedSave && state.neigh2Father.isBlank(),
                            modifier = Modifier.weight(1f)
                        )
                        AppFormTextField(
                            value = state.neigh2Mobile,
                            onValueChange = { updateField(state.copy(neigh2Mobile = BanglaTextValidator.filterMobileInput(it))) },
                            label = "মোবাইল নং (১১ ডিজিট)",
                            isError = attemptedSave && (state.neigh2Mobile.isBlank() || state.neigh2Mobile.length != 11),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        AppFormTextField(
                            value = state.neigh2Village,
                            onValueChange = { updateField(state.copy(neigh2Village = BanglaTextValidator.filterBengaliWithPunctuation(it))) },
                            label = "গ্রাম",
                            isError = attemptedSave && state.neigh2Village.isBlank(),
                            modifier = Modifier.weight(1.5f)
                        )
                        AppFormTextField(
                            value = state.neigh2PostOffice,
                            onValueChange = { updateField(state.copy(neigh2PostOffice = BanglaTextValidator.filterBengaliWithPunctuation(it))) },
                            label = "ডাকঘর",
                            isError = attemptedSave && state.neigh2PostOffice.isBlank(),
                            modifier = Modifier.weight(1.2f)
                        )
                        AppFormTextField(
                            value = state.neigh2Postal,
                            onValueChange = { updateField(state.copy(neigh2Postal = BanglaTextValidator.filterDigitsOnly(it))) },
                            label = "পোস্ট কোড",
                            isError = attemptedSave && state.neigh2Postal.isBlank(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        AppFormTextField(
                            value = state.neigh2Upazila,
                            onValueChange = { updateField(state.copy(neigh2Upazila = BanglaTextValidator.filterBengaliWithPunctuation(it))) },
                            label = "উপজেলা",
                            isError = attemptedSave && state.neigh2Upazila.isBlank(),
                            modifier = Modifier.weight(1f)
                        )
                        AppFormTextField(
                            value = state.neigh2Police,
                            onValueChange = { updateField(state.copy(neigh2Police = BanglaTextValidator.filterBengaliWithPunctuation(it))) },
                            label = "থানা",
                            isError = attemptedSave && state.neigh2Police.isBlank(),
                            modifier = Modifier.weight(1f)
                        )
                        AppFormTextField(
                            value = state.neigh2District,
                            onValueChange = { updateField(state.copy(neigh2District = BanglaTextValidator.filterBengaliWithPunctuation(it))) },
                            label = "জেলা",
                            isError = attemptedSave && state.neigh2District.isBlank(),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    AppFormTextField(
                        value = state.neigh2Nid,
                        onValueChange = { nid ->
                            updateField(state.copy(neigh2Nid = BanglaTextValidator.filterDigitsOnly(nid)))
                        },
                        label = "প্রতিবেশীর জাতীয় পরিচয়পত্র (NID - সংখ্যা)",
                        isError = attemptedSave && state.neigh2Nid.isBlank(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
            }

            // Investigation Officer Comment Card - Stamp 3D Theme Card (Blocked for Worker)
            StampThemed3DCard(
                badgeText = "৫",
                title = "তদন্তকারী কর্মকর্তার মন্তব্য",
                subtitle = "অফিসিয়াল ব্যবহারের জন্য সংরক্ষিত (কর্মী কর্তৃক পূরণ প্রযোজ্য নয়)",
                tag = "লকড / অফিসিয়াল"
            ) {
                    AutoFilledBlockedTextField(
                        value = state.investigationOfficerComment.ifBlank { "তদন্তকারী কর্মকর্তার মন্তব্য (শুধুমাত্র নিয়োগকারী ও তদন্তকারী অফিসারের জন্য সংরক্ষিত)" },
                        onValueChange = { },
                        label = "তদন্তকারী কর্মকর্তার বিশদ মন্তব্য",
                        effectiveLocked = true,
                        isAutoFilled = false,
                        modifier = Modifier.fillMaxWidth()
                    )
            }

            Spacer(Modifier.height(6.dp))
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
            formTitle = "৬. তথ্য যাচাই ফরম (২ পাতা)",
            nextScreenTitle = "৭. ১০০ টাকার স্ট্যাম্প চুক্তিপত্র",
            onGoToNext = {
                showSavedDialog = false
                viewModel.updateScreen(AppScreen.WORKER_PANEL)
            },
            onViewPdf = {
                showSavedDialog = false
                val pdf = AgreementFormsPdfGenerator.generateVerificationFormPdf(context, state, viewModel.allAgreementForms.value)
                ShareHelper.openFile(context, pdf)
            },
            onDismiss = {
                showSavedDialog = false
                viewModel.updateScreen(AppScreen.WORKER_PANEL)
            }
        )
    }

    if (showValidationErrorDialog) {
        AlertDialog(
            onDismissRequest = { showValidationErrorDialog = false },
            icon = {
                Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(36.dp))
            },
            title = {
                Text(
                    "আপনার সমস্ত তথ্য পূরণ করুন",
                    color = Color(0xFFDC2626),
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Column {
                    Text(
                        "চেয়ারম্যানের তথ্য, ইউনিয়ন/পৌরসভার নাম এবং মুচলেকার তথ্য (কর্মীর নাম, পিতার নাম, তারিখ) পূরণ করা আবশ্যক।",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showValidationErrorDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Text("ঠিক আছে")
                }
            }
        )
    }
}

