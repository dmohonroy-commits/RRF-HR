package com.example.ui.screens.forms

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.NomineeFormState
import com.example.data.NomineeItem
import com.example.ui.AppScreen
import com.example.ui.MainViewModel
import com.example.ui.components.*
import com.example.ui.components.FormSavedAdvanceDialog
import com.example.ui.components.StampThemed3DCard
import com.example.util.AgreementFormsPdfGenerator
import com.example.util.BanglaAddressHelper
import com.example.util.BanglaTextValidator
import com.example.util.ShareHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NomineeFormScreen(
    viewModel: MainViewModel,
    formState: NomineeFormState,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()
    val stampState by viewModel.formState.collectAsState()
    val allAgreementForms by viewModel.allAgreementForms.collectAsState()

    var state by remember(formState) { mutableStateOf(formState) }
    var showSavedDialog by remember { mutableStateOf(false) }
    var showValidationErrorDialog by remember { mutableStateOf(false) }
    var attemptedSave by remember { mutableStateOf(false) }

    LaunchedEffect(formState, stampState, allAgreementForms) {
        state = formState
        val rawName = allAgreementForms.idCardForm.employeeName.ifBlank { stampState.employeeName.ifBlank { allAgreementForms.personalInfoForm.employeeNameEnglish.ifBlank { allAgreementForms.personalInfoForm.employeeNameBangla } } }
        val engName = if (BanglaTextValidator.containsBengali(rawName)) BanglaAddressHelper.transliterateBanglaToEnglish(rawName) else rawName

        val rawDesig = allAgreementForms.idCardForm.designation.ifBlank { stampState.designation }
        val engDesig = when (rawDesig) {
            "অফিসার (অ্যাকাউন্টস)", "Officer (Accounts)" -> "Officer (Accounts)"
            "অফিসার (ঋণ)", "অফিসার (লোন)", "Officer (loan)", "Officer (Loan)" -> "Officer (loan)"
            "সার্ভিস স্টাফ", "Service Staff" -> "Service Staff"
            else -> if (BanglaTextValidator.containsBengali(rawDesig)) BanglaAddressHelper.transliterateBanglaToEnglish(rawDesig) else rawDesig
        }

        var updated = state
        if (updated.employeeName.isBlank() && engName.isNotBlank()) {
            updated = updated.copy(employeeName = engName)
        }
        if (updated.designation.isBlank() && engDesig.isNotBlank()) {
            updated = updated.copy(designation = engDesig)
        }
        val todayDate = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.US).format(java.util.Date())
        if (updated.declarationDate.isBlank()) {
            updated = updated.copy(declarationDate = todayDate)
        }
        if (updated != state) {
            state = updated
            viewModel.updateNomineeForm(updated)
        }
    }

    val updateField: (NomineeFormState) -> Unit = { newState ->
        state = newState
        viewModel.updateNomineeForm(newState)
    }

    val isEmpNameAutoFilled = stampState.employeeName.isNotBlank() && state.employeeName == stampState.employeeName
    val isDesigAutoFilled = stampState.designation.isNotBlank() && state.designation == stampState.designation

    val nominee = state.nominees.firstOrNull() ?: NomineeItem()

    val isRelEmpty = if (nominee.relationship.equals("Others", ignoreCase = true) || nominee.relationship.equals("Other", ignoreCase = true) || nominee.relationship == "অন্যান্য") {
        nominee.customRelationship.isBlank()
    } else {
        nominee.relationship.isBlank()
    }

    val hasEmptyMandatory = state.employeeName.isBlank() ||
            state.designation.isBlank() ||
            state.declarationDate.isBlank() ||
            (nominee.nomineeName.isBlank() && nominee.nomineeNameAndParents.isBlank()) ||
            (nominee.village.isBlank() && nominee.fullAddress.isBlank()) ||
            nominee.nidNo.isBlank() ||
            isRelEmpty ||
            nominee.percentOfShares.isBlank()

    fun saveAndGeneratePdf() {
        keyboardController?.hide()
        focusManager.clearFocus()
        attemptedSave = true
        if (hasEmptyMandatory) {
            showValidationErrorDialog = true
            return
        }
        viewModel.updateNomineeForm(state)
        AgreementFormsPdfGenerator.generateNomineeFormPdf(context, state)
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
                            text = "নমিনি তথ্য ফরম",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("btn_back_nominee")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "ফিরে যান", tint = MaterialTheme.colorScheme.primary)
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            viewModel.updateNomineeForm(state)
                            val pdf = AgreementFormsPdfGenerator.generateNomineeFormPdf(context, state)
                            ShareHelper.shareFile(context, pdf, "নমিনি তথ্য ফরম (A4 PDF)")
                        }
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "শেয়ার PDF", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(
                        onClick = {
                            viewModel.updateNomineeForm(state)
                            val pdf = AgreementFormsPdfGenerator.generateNomineeFormPdf(context, state)
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
                        text = "সেভ করুন ➔",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                elevation = FloatingActionButtonDefaults.elevation(8.dp),
                modifier = Modifier.testTag("fab_save_nominee")
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
                currentScreen = AppScreen.FORM_NOMINEE,
                viewModel = viewModel,
                onNavigate = { viewModel.updateScreen(it) }
            )

            // Notice banner
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
                    Column {
                        Text(
                            text = "নমিনির নাম, পিতা/মাতার নাম, ঠিকানা ও সম্পর্কের তথ্যসমূহ স্ব-বক্স অনুযায়ী নিচে পূরণ করুন।",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 17.sp
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "নোট: ফরমেট টি ইংরেজিতে (English) পূরণ করা বাধ্যতামূলক।",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFFDC2626)
                        )
                    }
                }
            }

            // Employee Card - Stamp 3D Theme Card
            StampThemed3DCard(
                badgeText = "১",
                title = "কর্মীর তথ্যাবলী",
                subtitle = "Employee Information (English Form)",
                tag = "আবশ্যক"
            ) {
                    AutoFilledBlockedTextField(
                        value = state.employeeName,
                        onValueChange = { updateField(state.copy(employeeName = BanglaTextValidator.filterEnglishText(it))) },
                        label = "Employee Name (কর্মীর নাম - English) *",
                        isAutoFilled = isEmpNameAutoFilled,
                        isError = attemptedSave && state.employeeName.isBlank(),
                        modifier = Modifier.testTag("input_nom_emp_name")
                    )

                    AutoFilledBlockedTextField(
                        value = state.designation,
                        onValueChange = { updateField(state.copy(designation = BanglaTextValidator.filterEnglishText(it))) },
                        label = "Designation (পদবী - English) *",
                        isAutoFilled = isDesigAutoFilled,
                        isError = attemptedSave && state.designation.isBlank(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    DateBoxesGridComponent(
                        value = state.declarationDate,
                        onValueChange = { updateField(state.copy(declarationDate = it)) },
                        label = "Date / তারিখের ছক (অটো পূরণকৃত) *",
                        isEnglish = true,
                        isError = attemptedSave && state.declarationDate.isBlank(),
                        errorMessage = if (attemptedSave && state.declarationDate.isBlank()) "Date is required" else "",
                        modifier = Modifier.fillMaxWidth()
                    )
            }

            // Nominees Details Card - Stamp 3D Theme Card
            StampThemed3DCard(
                badgeText = "২",
                title = "নমিনি এর তথ্যাবলী",
                subtitle = "Nominee Information in English",
                tag = "আবশ্যক"
            ) {
                    val nominee = state.nominees.firstOrNull() ?: NomineeItem()

                    fun updateNomineeItem(transform: (NomineeItem) -> NomineeItem) {
                        val updated = state.nominees.toMutableList()
                        if (updated.isEmpty()) updated.add(NomineeItem())
                        updated[0] = transform(updated[0])
                        updateField(state.copy(nominees = updated))
                    }

                    // 1. Nominee Name, Father Name, Mother Name Boxes
                    AppFormTextField(
                        value = nominee.nomineeName,
                        onValueChange = { updateNomineeItem { item -> item.copy(nomineeName = BanglaTextValidator.filterEnglishText(it)) } },
                        label = "Nominee's Name (নমিনির নাম) *",
                        placeholder = "Enter nominee's name",
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                        isError = attemptedSave && nominee.nomineeName.isBlank() && nominee.nomineeNameAndParents.isBlank(),
                        modifier = Modifier.fillMaxWidth().testTag("input_nom_name")
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        AppFormTextField(
                            value = nominee.fatherName,
                            onValueChange = { updateNomineeItem { item -> item.copy(fatherName = BanglaTextValidator.filterEnglishText(it)) } },
                            label = "Father's Name (নমিনির পিতার নাম)",
                            placeholder = "Nominee's father's name",
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                            modifier = Modifier.weight(1f)
                        )
                        AppFormTextField(
                            value = nominee.motherName,
                            onValueChange = { updateNomineeItem { item -> item.copy(motherName = BanglaTextValidator.filterEnglishText(it)) } },
                            label = "Mother's Name (নমিনির মাতার নাম)",
                            placeholder = "Nominee's mother's name",
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 1.dp)

                    var isSameAddressChecked by remember { mutableStateOf(false) }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val nextChecked = !isSameAddressChecked
                                isSameAddressChecked = nextChecked
                                if (nextChecked) {
                                    val emp = viewModel.allAgreementForms.value.personalInfoForm
                                    val idCard = viewModel.allAgreementForms.value.idCardForm
                                    val stamp = viewModel.formState.value

                                    val vill = if (idCard.village.isNotBlank() && !BanglaTextValidator.containsBengali(idCard.village)) idCard.village
                                        else emp.permVillage.ifBlank { stamp.employeeVillage.ifBlank { idCard.village } }
                                    val post = if (idCard.postOffice.isNotBlank() && !BanglaTextValidator.containsBengali(idCard.postOffice)) idCard.postOffice
                                        else emp.permPostOffice.ifBlank { stamp.employeePostOffice.ifBlank { idCard.postOffice } }
                                    val thana = if (idCard.thana.isNotBlank() && !BanglaTextValidator.containsBengali(idCard.thana)) idCard.thana
                                        else emp.permUpazila.ifBlank { emp.permThana.ifBlank { stamp.employeeUpazila.ifBlank { idCard.thana } } }
                                    val dist = if (idCard.district.isNotBlank() && !BanglaTextValidator.containsBengali(idCard.district)) idCard.district
                                        else emp.permDistrict.ifBlank { stamp.employeeDistrict.ifBlank { idCard.district } }

                                    val engVill = if (BanglaTextValidator.containsBengali(vill)) BanglaAddressHelper.transliterateBanglaToEnglish(vill) else vill
                                    val engPost = if (BanglaTextValidator.containsBengali(post)) BanglaAddressHelper.transliterateBanglaToEnglish(post) else post
                                    val engThana = if (BanglaTextValidator.containsBengali(thana)) BanglaAddressHelper.transliterateBanglaToEnglish(thana) else thana
                                    val engDist = if (dist.isNotBlank()) BanglaAddressHelper.getEnglishDistrict(dist) else ""

                                    updateNomineeItem { item ->
                                        item.copy(
                                            village = engVill,
                                            postOffice = engPost,
                                            thana = engThana,
                                            district = engDist
                                        )
                                    }
                                } else {
                                    updateNomineeItem { item ->
                                        item.copy(
                                            village = "",
                                            postOffice = "",
                                            thana = "",
                                            district = "",
                                            fullAddress = ""
                                        )
                                    }
                                }
                            }
                            .padding(vertical = 4.dp)
                    ) {
                        Checkbox(
                            checked = isSameAddressChecked,
                            onCheckedChange = { checked ->
                                isSameAddressChecked = checked
                                if (checked) {
                                    val emp = viewModel.allAgreementForms.value.personalInfoForm
                                    val idCard = viewModel.allAgreementForms.value.idCardForm
                                    val stamp = viewModel.formState.value

                                    val vill = if (idCard.village.isNotBlank() && !BanglaTextValidator.containsBengali(idCard.village)) idCard.village
                                        else emp.permVillage.ifBlank { stamp.employeeVillage.ifBlank { idCard.village } }
                                    val post = if (idCard.postOffice.isNotBlank() && !BanglaTextValidator.containsBengali(idCard.postOffice)) idCard.postOffice
                                        else emp.permPostOffice.ifBlank { stamp.employeePostOffice.ifBlank { idCard.postOffice } }
                                    val thana = if (idCard.thana.isNotBlank() && !BanglaTextValidator.containsBengali(idCard.thana)) idCard.thana
                                        else emp.permUpazila.ifBlank { emp.permThana.ifBlank { stamp.employeeUpazila.ifBlank { idCard.thana } } }
                                    val dist = if (idCard.district.isNotBlank() && !BanglaTextValidator.containsBengali(idCard.district)) idCard.district
                                        else emp.permDistrict.ifBlank { stamp.employeeDistrict.ifBlank { idCard.district } }

                                    val engVill = if (BanglaTextValidator.containsBengali(vill)) BanglaAddressHelper.transliterateBanglaToEnglish(vill) else vill
                                    val engPost = if (BanglaTextValidator.containsBengali(post)) BanglaAddressHelper.transliterateBanglaToEnglish(post) else post
                                    val engThana = if (BanglaTextValidator.containsBengali(thana)) BanglaAddressHelper.transliterateBanglaToEnglish(thana) else thana
                                    val engDist = if (dist.isNotBlank()) BanglaAddressHelper.getEnglishDistrict(dist) else ""

                                    updateNomineeItem { item ->
                                        item.copy(
                                            village = engVill,
                                            postOffice = engPost,
                                            thana = engThana,
                                            district = engDist
                                        )
                                    }
                                } else {
                                    updateNomineeItem { item ->
                                        item.copy(
                                            village = "",
                                            postOffice = "",
                                            thana = "",
                                            district = "",
                                            fullAddress = ""
                                        )
                                    }
                                }
                            }
                        )
                        Text("নমিনির ঠিকানা কর্মীর স্থায়ী ঠিকানার সাথে একই (Same as Employee Permanent Address)", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    }

                    Text(
                        text = "নমিনির ঠিকানা (Nominee Address in English)",
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // 2. Address Boxes: Village, Post, Thana, District
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        AppFormTextField(
                            value = nominee.village,
                            onValueChange = { updateNomineeItem { item -> item.copy(village = BanglaTextValidator.filterEnglishText(it)) } },
                            label = "Village / House (গ্রাম) *",
                            placeholder = "Village / House",
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                            isError = attemptedSave && nominee.village.isBlank() && nominee.fullAddress.isBlank(),
                            modifier = Modifier.weight(1f).testTag("input_nom_village")
                        )
                        AppFormTextField(
                            value = nominee.postOffice,
                            onValueChange = { updateNomineeItem { item -> item.copy(postOffice = BanglaTextValidator.filterEnglishText(it)) } },
                            label = "Post (পোস্ট) *",
                            placeholder = "Post Office",
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                            isError = attemptedSave && nominee.postOffice.isBlank() && nominee.fullAddress.isBlank(),
                            modifier = Modifier.weight(1f).testTag("input_nom_post")
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        AppFormTextField(
                            value = nominee.thana,
                            onValueChange = { updateNomineeItem { item -> item.copy(thana = BanglaTextValidator.filterEnglishText(it)) } },
                            label = "Thana / Upazila (থানা) *",
                            placeholder = "Thana / Upazila",
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                            isError = attemptedSave && nominee.thana.isBlank() && nominee.fullAddress.isBlank(),
                            modifier = Modifier.weight(1f).testTag("input_nom_thana")
                        )
                        AppFormTextField(
                            value = nominee.district,
                            onValueChange = { updateNomineeItem { item -> item.copy(district = BanglaTextValidator.filterEnglishText(it)) } },
                            label = "District (জেলা) *",
                            placeholder = "District",
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                            isError = attemptedSave && nominee.district.isBlank() && nominee.fullAddress.isBlank(),
                            modifier = Modifier.weight(1f).testTag("input_nom_district")
                        )
                    }

                    HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 1.dp)

                    // 3. Relationship Dropdown & Custom Field
                    val relationshipOptions = listOf(
                        "Father (বাবা)",
                        "Mother (মা)",
                        "Brother (ভাই)",
                        "Sister (বোন)",
                        "Wife (স্ত্রী)",
                        "Husband (স্বামী)",
                        "Son (পুত্র)",
                        "Daughter (কন্যা)",
                        "Others (অন্যান্য)"
                    )

                    val currentRelDisplay = when {
                        nominee.relationship.equals("Father", ignoreCase = true) -> "Father (বাবা)"
                        nominee.relationship.equals("Mother", ignoreCase = true) -> "Mother (মা)"
                        nominee.relationship.equals("Brother", ignoreCase = true) -> "Brother (ভাই)"
                        nominee.relationship.equals("Sister", ignoreCase = true) -> "Sister (বোন)"
                        nominee.relationship.equals("Wife", ignoreCase = true) -> "Wife (স্ত্রী)"
                        nominee.relationship.equals("Husband", ignoreCase = true) -> "Husband (স্বামী)"
                        nominee.relationship.equals("Son", ignoreCase = true) -> "Son (পুত্র)"
                        nominee.relationship.equals("Daughter", ignoreCase = true) -> "Daughter (কন্যা)"
                        nominee.relationship.equals("Others", ignoreCase = true) || nominee.relationship == "অন্যান্য" -> "Others (অন্যান্য)"
                        else -> nominee.relationship
                    }

                    AppFormDropdown(
                        value = currentRelDisplay,
                        onValueChange = { opt ->
                            val key = when (opt) {
                                "Father (বাবা)" -> "Father"
                                "Mother (মা)" -> "Mother"
                                "Brother (ভাই)" -> "Brother"
                                "Sister (বোন)" -> "Sister"
                                "Wife (স্ত্রী)" -> "Wife"
                                "Husband (স্বামী)" -> "Husband"
                                "Son (পুত্র)" -> "Son"
                                "Daughter (কন্যা)" -> "Daughter"
                                "Others (অন্যান্য)" -> "Others"
                                else -> opt
                            }
                            updateNomineeItem { item -> item.copy(relationship = key) }
                        },
                        label = "Relationship with Employee (সম্পর্ক) *",
                        options = relationshipOptions,
                        placeholder = "Select Relationship",
                        isError = attemptedSave && nominee.relationship.isBlank(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (nominee.relationship.equals("Others", ignoreCase = true) || nominee.relationship.equals("Other", ignoreCase = true) || nominee.relationship == "অন্যান্য") {
                        AppFormTextField(
                            value = nominee.customRelationship,
                            onValueChange = { updateNomineeItem { item -> item.copy(customRelationship = BanglaTextValidator.filterEnglishText(it)) } },
                            label = "Specify Other Relationship (অন্যান্য সম্পর্ক লিখুন) *",
                            placeholder = "Relationship name",
                            isError = attemptedSave && nominee.customRelationship.isBlank(),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // 4. NID & Share
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        AppFormTextField(
                            value = nominee.nidNo,
                            onValueChange = { updateNomineeItem { item -> item.copy(nidNo = BanglaTextValidator.filterDigitsOnly(it)) } },
                            label = "NID No. *",
                            placeholder = "NID Number",
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            isError = attemptedSave && nominee.nidNo.isBlank(),
                            modifier = Modifier.weight(1.2f)
                        )
                        AppFormTextField(
                            value = nominee.percentOfShares,
                            onValueChange = { newVal ->
                                val filtered = newVal.filter { it in '0'..'9' || it in '\u09E6'..'\u09EF' || it == '%' }
                                updateNomineeItem { item -> item.copy(percentOfShares = filtered) }
                            },
                            label = "Percent of shares (যেমন 100%) *",
                            placeholder = "100%",
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            isError = attemptedSave && nominee.percentOfShares.isBlank(),
                            modifier = Modifier.weight(1f)
                        )
                    }
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
            formTitle = "৪. নমিনি তথ্য ফরম",
            nextScreenTitle = "৫. কর্মীর তথ্যানুসন্ধান ফরম (৩ পাতা)",
            onGoToNext = {
                showSavedDialog = false
                viewModel.navigateToNextForm(AppScreen.FORM_NOMINEE)
            },
            onViewPdf = {
                showSavedDialog = false
                val pdf = AgreementFormsPdfGenerator.generateNomineeFormPdf(context, state)
                ShareHelper.openFile(context, pdf)
            },
            onDismiss = { showSavedDialog = false }
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
                        "ফরমের সকল আবশ্যকীয় তথ্য ইংরেজি বর্ণমালায় পূরণ করা বাধ্যতামূলক। অনুগ্রহ করে লাল চিহ্নিত ঘরগুলো পূরণ করুন।",
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
