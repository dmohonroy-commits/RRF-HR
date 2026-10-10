@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.ui.screens.forms

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
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
import kotlinx.coroutines.launch
import com.example.data.*
import com.example.ui.AppScreen
import com.example.ui.FormState
import com.example.ui.MainViewModel
import com.example.ui.components.*
import com.example.ui.components.StampThemed3DCard
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import android.Manifest
import android.speech.tts.TextToSpeech
import java.util.Locale
import com.example.util.AgreementFormsPdfGenerator
import com.example.util.BanglaAddressHelper
import com.example.util.BanglaTextValidator
import com.example.util.BangladeshDistricts
import com.example.util.ShareHelper
import com.example.util.SimNumberHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalInfoFormScreen(
    viewModel: MainViewModel,
    formState: PersonalInfoFormState,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val isDark = isSystemInDarkTheme()
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    val stampState by viewModel.formState.collectAsState()
    val allAgreementForms by viewModel.allAgreementForms.collectAsState()

    var ttsEngine: TextToSpeech? by remember { mutableStateOf(null) }
    DisposableEffect(context) {
        var tts: TextToSpeech? = null
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.let { engine ->
                    val result = engine.setLanguage(Locale("bn", "BD"))
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        engine.setLanguage(Locale("bn"))
                    }
                    engine.setPitch(1.15f)
                    engine.setSpeechRate(0.95f)
                }
            }
        }
        ttsEngine = tts
        onDispose {
            tts?.stop()
            tts?.shutdown()
        }
    }

    val speakGuarantorIncome: (String, String, String) -> Unit = { mDigits, mWords, yWords ->
        val effectiveMWords = mWords.ifBlank { if (mDigits.isNotBlank()) BanglaTextValidator.convertNumberToBanglaWords(mDigits) else "" }
        val effectiveYWords = yWords.ifBlank {
            val engVal = BanglaTextValidator.toEnglishDigits(mDigits).toLongOrNull() ?: 0L
            if (engVal > 0) BanglaTextValidator.convertNumberToBanglaWords(BanglaTextValidator.toBanglaDigits((engVal * 12).toString())) else ""
        }
        val textToSpeak = if (effectiveMWords.isNotBlank()) {
            "জামিনদারের মাসিক আয় $effectiveMWords। বাৎসরিক আয় $effectiveYWords।"
        } else {
            "জামিনদারের মাসিক আয় এখনো লেখা হয়নি"
        }
        ttsEngine?.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, null, "GUARANTOR_INCOME")
    }

    val currentDesig = allAgreementForms.trainingForm.designation.ifBlank { stampState.designation }
    val isServiceStaff = currentDesig == "সার্ভিস স্টাফ" || currentDesig == "Service Staff"

    var state by remember(formState) { mutableStateOf(formState) }
    var selectedTab by remember { mutableStateOf(0) }
    var showSavedDialog by remember { mutableStateOf(false) }
    var showValidationErrorDialog by remember { mutableStateOf(false) }
    var attemptedSave by remember { mutableStateOf(false) }

    val phonePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val simNums = SimNumberHelper.getSimPhoneNumbers(context)
            val s1 = simNums.getOrNull(0).orEmpty()
            val s2 = simNums.getOrNull(1).orEmpty()
            var u = state
            if (s1.isNotBlank() && u.mobileNumber.isBlank()) {
                u = u.copy(mobileNumber = s1)
            }
            if (s2.isNotBlank() && u.alternateNumber.isBlank()) {
                u = u.copy(alternateNumber = s2)
            }
            if (u != state) {
                state = u
                viewModel.updatePersonalInfoForm(u)
            }
        }
    }

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            phonePermissionLauncher.launch(Manifest.permission.READ_PHONE_STATE)
        }
    }

    LaunchedEffect(formState, stampState, allAgreementForms) {
        state = formState

        // 1. Employee Bangla Name
        val autoBanglaName = if (state.employeeNameBangla.isNotBlank()) {
            state.employeeNameBangla
        } else if (stampState.employeeName.isNotBlank() && BanglaTextValidator.containsBengali(stampState.employeeName)) {
            stampState.employeeName
        } else if (allAgreementForms.trainingForm.employeeName.isNotBlank() && BanglaTextValidator.containsBengali(allAgreementForms.trainingForm.employeeName)) {
            allAgreementForms.trainingForm.employeeName
        } else if (allAgreementForms.verificationForm.staffFullName.isNotBlank() && BanglaTextValidator.containsBengali(allAgreementForms.verificationForm.staffFullName)) {
            allAgreementForms.verificationForm.staffFullName
        } else if (stampState.employeeName.isNotBlank()) {
            stampState.employeeName
        } else {
            allAgreementForms.trainingForm.employeeName.ifBlank { allAgreementForms.verificationForm.staffFullName }
        }

        // 2. Employee English Name
        val autoEnglishName = if (state.employeeNameEnglish.isNotBlank()) {
            state.employeeNameEnglish
        } else if (allAgreementForms.idCardForm.employeeName.isNotBlank()) {
            allAgreementForms.idCardForm.employeeName
        } else if (allAgreementForms.relationshipForm.employeeName.isNotBlank()) {
            allAgreementForms.relationshipForm.employeeName
        } else if (allAgreementForms.nomineeForm.employeeName.isNotBlank()) {
            allAgreementForms.nomineeForm.employeeName
        } else if (stampState.employeeName.isNotBlank()) {
            if (!BanglaTextValidator.containsBengali(stampState.employeeName)) stampState.employeeName
            else BanglaAddressHelper.transliterateBanglaToEnglish(stampState.employeeName)
        } else {
            ""
        }

        // 3. Father Name (Bengali strictly prioritized - Select fullest candidate)
        val fatherCandidates = listOf(
            state.fatherName,
            stampState.employeeFatherName,
            allAgreementForms.personalInfoForm.fatherName,
            allAgreementForms.verificationForm.staffFatherName,
            allAgreementForms.trainingForm.fatherName
        ).filter { it.isNotBlank() && BanglaTextValidator.isValidBanglaName(it) }
        val autoFather = fatherCandidates.maxByOrNull { it.length } ?: ""

        // 4. Mother Name (Bengali strictly required - Select fullest candidate)
        val motherCandidates = listOf(
            state.motherName,
            allAgreementForms.personalInfoForm.motherName,
            allAgreementForms.trainingForm.motherName,
            allAgreementForms.nomineeForm.nominees.firstOrNull { BanglaTextValidator.isValidBanglaName(it.motherName) }?.motherName ?: "",
            if (stampState.guarantorRelationship == "মাতা") stampState.guarantorName else ""
        ).filter { it.isNotBlank() && BanglaTextValidator.isValidBanglaName(it) }
        val autoMother = motherCandidates.maxByOrNull { it.length } ?: ""

        // 5. Permanent Address
        val autoVillage = if (state.permVillage.isNotBlank()) {
            state.permVillage
        } else if (stampState.employeeVillage.isNotBlank()) {
            stampState.employeeVillage
        } else if (allAgreementForms.trainingForm.village.isNotBlank()) {
            allAgreementForms.trainingForm.village
        } else {
            allAgreementForms.idCardForm.village
        }

        val autoPost = if (state.permPostOffice.isNotBlank()) {
            state.permPostOffice
        } else if (stampState.employeePostOffice.isNotBlank()) {
            stampState.employeePostOffice
        } else if (allAgreementForms.trainingForm.postOffice.isNotBlank()) {
            allAgreementForms.trainingForm.postOffice
        } else {
            allAgreementForms.idCardForm.postOffice
        }

        val autoUpazila = if (state.permUpazila.isNotBlank()) {
            state.permUpazila
        } else if (stampState.employeeUpazila.isNotBlank()) {
            stampState.employeeUpazila
        } else if (allAgreementForms.trainingForm.thana.isNotBlank()) {
            allAgreementForms.trainingForm.thana
        } else {
            allAgreementForms.idCardForm.thana
        }

        val autoDistrict = if (state.permDistrict.isNotBlank()) {
            state.permDistrict
        } else if (stampState.employeeDistrict.isNotBlank()) {
            stampState.employeeDistrict
        } else if (allAgreementForms.trainingForm.district.isNotBlank()) {
            allAgreementForms.trainingForm.district
        } else {
            allAgreementForms.idCardForm.district
        }

        // 6. National IDs
        val autoEmployeeNid = if (state.employeeNid.isNotBlank()) {
            state.employeeNid
        } else if (allAgreementForms.nomineeForm.nominees.firstOrNull { it.nidNo.isNotBlank() }?.nidNo?.isNotBlank() == true) {
            allAgreementForms.nomineeForm.nominees.first { it.nidNo.isNotBlank() }.nidNo
        } else if (allAgreementForms.idCardForm.pinCode.length >= 10 && allAgreementForms.idCardForm.pinCode.all { it.isDigit() }) {
            allAgreementForms.idCardForm.pinCode
        } else {
            ""
        }

        val autoGuarantorNid = state.guarantorNid

        // 7. Guarantor Information (Page 3)
        val autoGName = if (state.guarantorName.isNotBlank()) state.guarantorName else stampState.guarantorName
        val autoGFather = if (state.guarantorFatherName.isNotBlank()) state.guarantorFatherName else stampState.guarantorFatherName
        val autoGMother = if (state.guarantorMotherName.isNotBlank()) state.guarantorMotherName else stampState.guarantorMotherName
        val autoGRel = if (state.guarantorRelationship.isNotBlank()) {
            state.guarantorRelationship
        } else if (stampState.guarantorRelationship == "অন্যান্য" && stampState.guarantorRelationshipCustom.isNotBlank()) {
            stampState.guarantorRelationshipCustom
        } else {
            stampState.guarantorRelationship
        }
        // 8. Mobile Number & Alternate Mobile Number (SIM 1 -> Primary Mobile, SIM 2 -> Alternate Mobile)
        val simNumbers = SimNumberHelper.getSimPhoneNumbers(context)
        val sim1 = simNumbers.getOrNull(0).orEmpty()
        val sim2 = simNumbers.getOrNull(1).orEmpty()

        val autoMobile = if (state.mobileNumber.isNotBlank()) {
            state.mobileNumber
        } else if (sim1.isNotBlank()) {
            sim1
        } else {
            allAgreementForms.idCardForm.mobileNumber
        }

        val autoAlternateMobile = if (state.alternateNumber.isNotBlank()) {
            state.alternateNumber
        } else if (sim2.isNotBlank()) {
            sim2
        } else {
            ""
        }

        // 9. Post Code (Bangla digits)
        val autoPostCode = if (state.permPostCode.length >= 4) {
            BanglaTextValidator.toBanglaDigits(state.permPostCode)
        } else if (allAgreementForms.trainingForm.postCode.length >= 4) {
            BanglaTextValidator.toBanglaDigits(allAgreementForms.trainingForm.postCode)
        } else if (allAgreementForms.verificationForm.neigh1Postal.length >= 4) {
            BanglaTextValidator.toBanglaDigits(allAgreementForms.verificationForm.neigh1Postal)
        } else {
            BangladeshDistricts.lookupPostCode(district = autoDistrict, upazila = autoUpazila, postOffice = autoPost)
        }

        // 10. Date of Birth & Age calculation
        val detailedAge = if (state.dateOfBirth.isNotBlank()) {
            BanglaTextValidator.calculateDetailedAge(state.dateOfBirth)
        } else null
        val autoAgeYears = detailedAge?.years.orEmpty()
        val autoAgeMonths = detailedAge?.months.orEmpty()
        val autoAgeDays = detailedAge?.days.orEmpty()

        // 11. Family members list initialization (User enters manually, all empty)
        val currentFamList = if (state.familyMembers.isNotEmpty()) state.familyMembers.toMutableList() else mutableListOf(
            FamilyMemberRow(),
            FamilyMemberRow(),
            FamilyMemberRow(),
            FamilyMemberRow()
        )
        while (currentFamList.size < 4) {
            currentFamList.add(FamilyMemberRow())
        }
        val famUpdated = false

        // Guarantor Income words & yearly auto-sync ("অঙ্গিকারনামা প্রদানকারী ফরমে জামিনদারের আয় আগের তথ্য থেকে অটো বসবে")
        val gMDigits = state.guardianMonthlyIncome
        val autoGMWords = if (gMDigits.isNotBlank() && state.guardianMonthlyIncomeWords.isBlank()) BanglaTextValidator.convertNumberToBanglaWords(gMDigits) else state.guardianMonthlyIncomeWords
        val engVal = BanglaTextValidator.toEnglishDigits(gMDigits).toLongOrNull() ?: 0L
        val autoGYVal = if (engVal > 0) (engVal * 12).toString() else ""
        val autoGYDigits = if (autoGYVal.isNotBlank() && state.guardianYearlyIncome.isBlank()) BanglaTextValidator.toBanglaDigits(autoGYVal) else state.guardianYearlyIncome
        val autoGYWords = if (autoGYDigits.isNotBlank() && state.guardianYearlyIncomeWords.isBlank()) BanglaTextValidator.convertNumberToBanglaWords(autoGYDigits) else state.guardianYearlyIncomeWords

        // Guarantor address only auto-fills from worker address if sameAddress checkbox is ticked
        val autoGVill = if (state.guarantorSameAddress) autoVillage else state.guarantorVillage
        val autoGPost = if (state.guarantorSameAddress) autoPost else state.guarantorPostOffice
        val autoGUpazila = if (state.guarantorSameAddress) autoUpazila else state.guarantorUpazila
        val autoGDistrict = if (state.guarantorSameAddress) autoDistrict else state.guarantorDistrict
        val autoGPostCode = if (state.guarantorSameAddress) autoPostCode else state.guarantorPostCode

        val isCurrentMotherScrambled = BanglaTextValidator.isScrambledBangla(state.motherName)
        val isCurrentFatherScrambled = BanglaTextValidator.isScrambledBangla(state.fatherName)

        val cleanMother = if (isCurrentMotherScrambled) autoMother else if (state.motherName.isBlank()) autoMother else state.motherName
        val cleanFather = if (isCurrentFatherScrambled) autoFather else if (state.fatherName.isBlank()) autoFather else state.fatherName

        val needsUpdate = (state.employeeNameBangla.isBlank() && autoBanglaName.isNotBlank()) ||
                (state.employeeNameEnglish.isBlank() && autoEnglishName.isNotBlank()) ||
                isCurrentFatherScrambled ||
                (state.fatherName.isBlank() && autoFather.isNotBlank()) ||
                isCurrentMotherScrambled ||
                (state.motherName.isBlank() && autoMother.isNotBlank()) ||
                (state.permVillage.isBlank() && autoVillage.isNotBlank()) ||
                (state.permPostOffice.isBlank() && autoPost.isNotBlank()) ||
                ((state.permPostCode.isBlank() || state.permPostCode.length < 4) && autoPostCode.isNotBlank()) ||
                (state.permUpazila.isBlank() && autoUpazila.isNotBlank()) ||
                (state.permDistrict.isBlank() && autoDistrict.isNotBlank()) ||
                (state.employeeNid.isBlank() && autoEmployeeNid.isNotBlank()) ||
                (state.guarantorSameAddress && state.guarantorVillage.isBlank() && autoGVill.isNotBlank()) ||
                (state.guarantorSameAddress && state.guarantorPostOffice.isBlank() && autoGPost.isNotBlank()) ||
                (state.guarantorSameAddress && state.guarantorUpazila.isBlank() && autoGUpazila.isNotBlank()) ||
                (state.guarantorSameAddress && state.guarantorDistrict.isBlank() && autoGDistrict.isNotBlank()) ||
                (state.mobileNumber.isBlank() && autoMobile.isNotBlank()) ||
                (state.alternateNumber.isBlank() && autoAlternateMobile.isNotBlank()) ||
                (state.dateOfBirth.isNotBlank() && state.ageYears.isBlank() && autoAgeYears.isNotBlank()) ||
                (state.guardianMonthlyIncome.isNotBlank() && state.guardianMonthlyIncomeWords.isBlank() && autoGMWords.isNotBlank()) ||
                (state.guardianMonthlyIncome.isNotBlank() && state.guardianYearlyIncome.isBlank() && autoGYDigits.isNotBlank()) ||
                (state.guardianYearlyIncome.isNotBlank() && state.guardianYearlyIncomeWords.isBlank() && autoGYWords.isNotBlank()) ||
                state.familyMembers.isEmpty()

        if (needsUpdate) {
            val updated = state.copy(
                employeeNameBangla = if (state.employeeNameBangla.isBlank()) autoBanglaName else state.employeeNameBangla,
                employeeNameEnglish = if (state.employeeNameEnglish.isBlank()) autoEnglishName else state.employeeNameEnglish,
                fatherName = cleanFather,
                motherName = cleanMother,
                permVillage = if (state.permVillage.isBlank()) autoVillage else state.permVillage,
                permPostOffice = if (state.permPostOffice.isBlank()) autoPost else state.permPostOffice,
                permPostCode = if (state.permPostCode.length >= 4) BanglaTextValidator.toBanglaDigits(state.permPostCode) else autoPostCode,
                permUpazila = if (state.permUpazila.isBlank()) autoUpazila else state.permUpazila,
                permThana = if (state.permThana.isBlank()) autoUpazila else state.permThana,
                permDistrict = if (state.permDistrict.isBlank()) autoDistrict else state.permDistrict,
                currVillageOrHouse = state.currVillageOrHouse,
                currPostOffice = state.currPostOffice,
                currPostCode = state.currPostCode,
                currUpazila = state.currUpazila,
                currThana = state.currThana,
                currDistrict = state.currDistrict,
                employeeNid = if (state.employeeNid.isBlank()) autoEmployeeNid else state.employeeNid,
                // Guarantor details are filled manually; address is synced if sameAddress checkbox is true
                guarantorVillage = if (state.guarantorSameAddress && state.guarantorVillage.isBlank()) autoGVill else state.guarantorVillage,
                guarantorPostOffice = if (state.guarantorSameAddress && state.guarantorPostOffice.isBlank()) autoGPost else state.guarantorPostOffice,
                guarantorPostCode = if (state.guarantorSameAddress && state.guarantorPostCode.isBlank()) autoGPostCode else state.guarantorPostCode,
                guarantorUpazila = if (state.guarantorSameAddress && state.guarantorUpazila.isBlank()) autoGUpazila else state.guarantorUpazila,
                guarantorThana = if (state.guarantorSameAddress && state.guarantorThana.isBlank()) autoGUpazila else state.guarantorThana,
                guarantorDistrict = if (state.guarantorSameAddress && state.guarantorDistrict.isBlank()) autoGDistrict else state.guarantorDistrict,
                mobileNumber = if (state.mobileNumber.isBlank()) autoMobile else state.mobileNumber,
                alternateNumber = if (state.alternateNumber.isBlank()) autoAlternateMobile else state.alternateNumber,
                ageYears = if (state.ageYears.isBlank() && autoAgeYears.isNotBlank()) autoAgeYears else state.ageYears,
                ageMonths = if (state.ageMonths.isBlank() && autoAgeMonths.isNotBlank()) autoAgeMonths else state.ageMonths,
                ageDays = if (state.ageDays.isBlank() && autoAgeDays.isNotBlank()) autoAgeDays else state.ageDays,
                guardianMonthlyIncome = state.guardianMonthlyIncome,
                guardianMonthlyIncomeWords = autoGMWords,
                guardianYearlyIncome = autoGYDigits,
                guardianYearlyIncomeWords = autoGYWords,
                familyMembers = if (state.familyMembers.isEmpty()) currentFamList else state.familyMembers
            )
            state = updated
            viewModel.updatePersonalInfoForm(updated)
        }
    }

    val updateField: (PersonalInfoFormState) -> Unit = { newState ->
        state = newState
        viewModel.updatePersonalInfoForm(newState)
    }

    val isSameAddress = state.currVillageOrHouse.isNotBlank() &&
            state.currVillageOrHouse == state.permVillage &&
            state.currPostOffice == state.permPostOffice &&
            state.currDistrict == state.permDistrict

    val hasPage1EmptyMandatory = (state.employeeNameBangla.isBlank() && state.employeeNameEnglish.isBlank()) ||
            state.employeeNid.isBlank() ||
            state.fatherName.isBlank() ||
            state.motherName.isBlank() ||
            state.permVillage.isBlank() ||
            state.permPostOffice.isBlank() ||
            (state.permUpazila.isBlank() && state.permThana.isBlank()) ||
            state.permDistrict.isBlank() ||
            (!isSameAddress && state.currVillageOrHouse.isBlank()) ||
            (!isSameAddress && state.currPostOffice.isBlank()) ||
            (!isSameAddress && state.currDistrict.isBlank()) ||
            state.mobileNumber.isBlank() ||
            (state.isMarried && state.spouseName.isBlank()) ||
            (state.hasPoliceCase && (state.policeCaseCount.isBlank() || state.policeCaseDetails.isBlank()))

    val hasPage2EmptyMandatory = state.educations.take(3).any { it.instituteName.isBlank() }

    val hasPage3EmptyMandatory = state.guarantorName.isBlank() ||
            state.guarantorFatherName.isBlank() ||
            state.guarantorMotherName.isBlank() ||
            state.guarantorOccupation.isBlank() ||
            state.guarantorRelationship.isBlank() ||
            state.guarantorVillage.isBlank() ||
            state.guarantorPostOffice.isBlank() ||
            (state.guarantorUpazila.isBlank() && state.guarantorThana.isBlank()) ||
            state.guarantorDistrict.isBlank()

    val hasEmptyMandatory = hasPage1EmptyMandatory || hasPage2EmptyMandatory || hasPage3EmptyMandatory

    fun handleSaveAndNext() {
        keyboardController?.hide()
        focusManager.clearFocus()
        attemptedSave = true
        when (selectedTab) {
            0 -> {
                if (hasPage1EmptyMandatory) {
                    showValidationErrorDialog = true
                    return
                }
                viewModel.updatePersonalInfoForm(state)
                selectedTab = 1
                coroutineScope.launch { scrollState.animateScrollTo(0) }
            }
            1 -> {
                if (hasPage1EmptyMandatory) {
                    selectedTab = 0
                    showValidationErrorDialog = true
                    return
                }
                if (hasPage2EmptyMandatory) {
                    showValidationErrorDialog = true
                    return
                }
                viewModel.updatePersonalInfoForm(state)
                selectedTab = 2
                coroutineScope.launch { scrollState.animateScrollTo(0) }
            }
            2 -> {
                if (hasPage1EmptyMandatory) {
                    selectedTab = 0
                    showValidationErrorDialog = true
                    return
                }
                if (hasPage2EmptyMandatory) {
                    selectedTab = 1
                    showValidationErrorDialog = true
                    return
                }
                if (hasPage3EmptyMandatory) {
                    showValidationErrorDialog = true
                    return
                }
                viewModel.updatePersonalInfoForm(state)
                AgreementFormsPdfGenerator.generatePersonalInfoFormPdf(context, state)
                AgreementFormsPdfGenerator.generateAllMergedAgreementFormsPdf(context, viewModel.allAgreementForms.value)
                showSavedDialog = true
            }
        }
    }

    val tabTitles = listOf(
        "১. ব্যক্তিগত",
        "২. শিক্ষাগত ও পেশা",
        "৩. পরিবার ও জামিনদার",
        "৪. তথ্য যাচাই ফরম ➔"
    )

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
                            text = "কর্মীর ব্যক্তিগত তথ্যানুসন্ধান ফরম",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("btn_back_personal")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "ফিরে যান", tint = MaterialTheme.colorScheme.primary)
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            viewModel.updatePersonalInfoForm(state)
                            val pdf = AgreementFormsPdfGenerator.generatePersonalInfoFormPdf(context, state)
                            ShareHelper.shareFile(context, pdf, "কর্মীর ব্যক্তিগত তথ্যানুসন্ধান ফরম (৩ পাতা A4 PDF)")
                        }
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "শেয়ার PDF", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(
                        onClick = {
                            viewModel.updatePersonalInfoForm(state)
                            val pdf = AgreementFormsPdfGenerator.generatePersonalInfoFormPdf(context, state)
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
                onClick = { handleSaveAndNext() },
                icon = { Icon(Icons.Default.Save, contentDescription = "সেভ করুন") },
                text = {
                    Text(
                        text = if (selectedTab == 2) "সেভ করুন ➔" else "সেভ করুন ➔",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                elevation = FloatingActionButtonDefaults.elevation(8.dp),
                modifier = Modifier.testTag("fab_save_personal")
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
            ) {
            // Form Link Header connecting all 7 forms
            FormLinkHeader(
                currentScreen = AppScreen.FORM_PERSONAL_INFO,
                viewModel = viewModel,
                onNavigate = { viewModel.updateScreen(it) },
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
            )

            // Scrollable Tab Row for 3 pages + Verification shortcut
            ScrollableTabRow(
                selectedTabIndex = selectedTab.coerceAtMost(2),
                edgePadding = 12.dp,
                containerColor = Color.White,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = {
                            if (index > selectedTab) {
                                attemptedSave = true
                                if (selectedTab == 0 && hasPage1EmptyMandatory) {
                                    showValidationErrorDialog = true
                                    return@Tab
                                }
                                if (selectedTab == 1 && (hasPage1EmptyMandatory || hasPage2EmptyMandatory)) {
                                    if (hasPage1EmptyMandatory) selectedTab = 0
                                    showValidationErrorDialog = true
                                    return@Tab
                                }
                            }
                            if (index == 3) {
                                attemptedSave = true
                                if (hasEmptyMandatory) {
                                    if (hasPage1EmptyMandatory) selectedTab = 0
                                    else if (hasPage2EmptyMandatory) selectedTab = 1
                                    else selectedTab = 2
                                    showValidationErrorDialog = true
                                } else {
                                    viewModel.updatePersonalInfoForm(state)
                                    viewModel.updateScreen(AppScreen.FORM_VERIFICATION)
                                }
                            } else {
                                selectedTab = index
                            }
                        },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (index == 3) {
                                    Icon(
                                        Icons.Default.VerifiedUser,
                                        contentDescription = null,
                                        tint = Color(0xFF7C3AED),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(Modifier.width(4.dp))
                                }
                                Text(
                                    text = title,
                                    fontSize = 13.sp,
                                    fontWeight = if (selectedTab == index || index == 3) FontWeight.Bold else FontWeight.Normal,
                                    color = if (index == 3) Color(0xFF7C3AED) else Color.Unspecified
                                )
                            }
                        }
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                when (selectedTab) {
                    0 -> {
                        Page1PersonalInfo(
                            state = state,
                            stampState = stampState,
                            allAgreementForms = allAgreementForms,
                            attemptedSave = attemptedSave,
                            speakGuarantorIncome = speakGuarantorIncome,
                            onStateChange = updateField
                        )

                        // Bottom Next Page Button with Arrow
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF0F3B7E).copy(alpha = 0.15f) else Color(0xFFF0F7FF)),
                            border = BorderStroke(1.5.dp, Color(0xFF0F3B7E)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    handleSaveAndNext()
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("পরবর্তী পৃষ্ঠা (২/৩): শিক্ষাগত যোগ্যতা ➔", fontWeight = FontWeight.Bold, color = if (isDark) Color(0xFF6EE7B7) else Color(0xFF047857), fontSize = 14.sp)
                                    Text("২য় ধাপে শিক্ষাগত বিবরণ ও অভিজ্ঞতা পূরণ করতে এখানে চাপুন", fontSize = 11.5.sp, color = if (isDark) Color(0xFFA7F3D0) else Color(0xFF065F46))
                                }
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFF0F3B7E),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "পরবর্তী", tint = Color.White, modifier = Modifier.size(20.dp))
                                    }
                                }
                            }
                        }
                    }
                    1 -> {
                        Page2EducationExperience(
                            state = state,
                            attemptedSave = attemptedSave,
                            isServiceStaff = isServiceStaff,
                            onStateChange = updateField
                        )

                        // Bottom Navigation Buttons (Previous / Next)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    coroutineScope.launch { scrollState.animateScrollTo(0) }
                                    selectedTab = 0
                                },
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.2.dp, if (isDark) Color(0xFF64748B) else Color(0xFF94A3B8)),
                                modifier = Modifier.weight(1f).height(48.dp)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("⬅ ১ম ধাপ: ব্যক্তিগত", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }

                            Button(
                                onClick = {
                                    handleSaveAndNext()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F3B7E)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1.3f).height(48.dp)
                            ) {
                                Text("সংরক্ষণ ও পরবর্তী ৩য় ধাপ ➔", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Spacer(Modifier.width(4.dp))
                                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                    2 -> {
                        Page3FamilyGuarantor(
                            state = state,
                            stampState = stampState,
                            allAgreementForms = allAgreementForms,
                            attemptedSave = attemptedSave,
                            speakGuarantorIncome = speakGuarantorIncome,
                            onStateChange = updateField,
                            onNavigateToVerification = {
                                handleSaveAndNext()
                            }
                        )

                        // Bottom Navigation Buttons (Previous / Verification Form)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    coroutineScope.launch { scrollState.animateScrollTo(0) }
                                    selectedTab = 1
                                },
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.2.dp, Color(0xFF64748B)),
                                modifier = Modifier.weight(1f).height(48.dp)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("⬅ ২য় ধাপ: শিক্ষাগত", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }

                            Button(
                                onClick = {
                                    handleSaveAndNext()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1.3f).height(48.dp)
                            ) {
                                Text("সংরক্ষণ ও ৬. যাচাই ফরম ➔", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }

                Spacer(Modifier.height(10.dp))

                Spacer(Modifier.height(32.dp))
            }
        }
    }

    if (showSavedDialog) {
        FormSavedAdvanceDialog(
            formTitle = "৫. কর্মীর তথ্যানুসন্ধান ফরম (৩ পাতা)",
            nextScreenTitle = "৬. তথ্য যাচাই ফরম (২ পাতা)",
            onGoToNext = {
                showSavedDialog = false
                viewModel.navigateToNextForm(AppScreen.FORM_PERSONAL_INFO)
            },
            onViewPdf = {
                showSavedDialog = false
                val pdf = AgreementFormsPdfGenerator.generatePersonalInfoFormPdf(context, state)
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
                Text(
                    "ব্যাংক, বিকল্প মোবাইল নং, আয়, টিন ও সম্পত্তি বাদে অন্যান্য সকল আবশ্যকীয় তথ্য পূরণ করা বাধ্যতামূলক। লাল চিহ্নিত ঘরগুলো পূরণ করুন।",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
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
}

// -------------------------------------------------------------------------
// Tab 1: Personal Information (Page 1)
// -------------------------------------------------------------------------
@Composable
fun Page1PersonalInfo(
    state: PersonalInfoFormState,
    stampState: FormState,
    allAgreementForms: AllAgreementForms,
    attemptedSave: Boolean,
    speakGuarantorIncome: (String, String, String) -> Unit = { _, _, _ -> },
    onStateChange: (PersonalInfoFormState) -> Unit
) {
    val isNameBanglaAutoFilled = state.employeeNameBangla.isNotBlank() && (
        stampState.employeeName.isNotBlank() ||
        allAgreementForms.trainingForm.employeeName.isNotBlank()
    )
    val isNameEnglishAutoFilled = state.employeeNameEnglish.isNotBlank() && (
        allAgreementForms.idCardForm.employeeName.isNotBlank() ||
        allAgreementForms.relationshipForm.employeeName.isNotBlank() ||
        allAgreementForms.nomineeForm.employeeName.isNotBlank()
    )
    val isFatherAutoFilled = state.fatherName.isNotBlank() && (
        stampState.employeeFatherName.isNotBlank() ||
        allAgreementForms.trainingForm.fatherName.isNotBlank() ||
        allAgreementForms.idCardForm.fatherName.isNotBlank()
    )
    val isMotherAutoFilled = BanglaTextValidator.isValidBanglaName(state.motherName) && (
        BanglaTextValidator.isValidBanglaName(allAgreementForms.trainingForm.motherName) ||
        allAgreementForms.nomineeForm.nominees.any { BanglaTextValidator.isValidBanglaName(it.motherName) }
    )
    val isPermVillAutoFilled = state.permVillage.isNotBlank() && (
        stampState.employeeVillage.isNotBlank() ||
        allAgreementForms.trainingForm.village.isNotBlank() ||
        allAgreementForms.idCardForm.village.isNotBlank()
    )
    val isPermPostAutoFilled = state.permPostOffice.isNotBlank() && (
        stampState.employeePostOffice.isNotBlank() ||
        allAgreementForms.trainingForm.postOffice.isNotBlank() ||
        allAgreementForms.idCardForm.postOffice.isNotBlank()
    )
    val isPermUpazilaAutoFilled = state.permUpazila.isNotBlank() && (
        stampState.employeeUpazila.isNotBlank() ||
        allAgreementForms.trainingForm.thana.isNotBlank() ||
        allAgreementForms.idCardForm.thana.isNotBlank()
    )
    val isPermDistAutoFilled = state.permDistrict.isNotBlank() && (
        stampState.employeeDistrict.isNotBlank() ||
        allAgreementForms.trainingForm.district.isNotBlank() ||
        allAgreementForms.idCardForm.district.isNotBlank()
    )
    val isMobileAutoFilled = state.mobileNumber.isNotBlank() && allAgreementForms.idCardForm.mobileNumber.isNotBlank()
    val isDarkPage1 = isSystemInDarkTheme()

    var isSameAddressChecked by remember { mutableStateOf(false) }

    val hasOptionalData = state.alternateNumber.isNotBlank() ||
            state.drivingLicenseNo.isNotBlank() ||
            state.passportNo.isNotBlank() ||
            state.bankAccountNo.isNotBlank() ||
            state.tinNumber.isNotBlank() ||
            state.propertyDetails.isNotBlank()
    var isOptionalExpanded by remember { mutableStateOf(hasOptionalData) }

    StampThemed3DCard(
        badgeText = "১",
        title = "কর্মীর ব্যক্তিগত বিবরণ",
        subtitle = "পৃষ্ঠা ১: নাম, পিতা-মাতা, বৈবাহিক অবস্থা ও NID",
        tag = "আবশ্যক"
    ) {
        AutoFilledBlockedTextField(
            value = state.employeeNameBangla,
            onValueChange = { onStateChange(state.copy(employeeNameBangla = BanglaTextValidator.filterBanglaText(it))) },
            label = "কর্মীর নাম (বাংলায়) *",
            isAutoFilled = isNameBanglaAutoFilled,
            isError = attemptedSave && state.employeeNameBangla.isBlank(),
            modifier = Modifier.fillMaxWidth().testTag("input_pers_emp_name_bangla")
        )

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            AutoFilledBlockedTextField(
                value = state.employeeNameEnglish,
                onValueChange = { onStateChange(state.copy(employeeNameEnglish = BanglaTextValidator.filterEnglishTitleCaseText(it))) },
                label = "Name (English) *",
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                isAutoFilled = isNameEnglishAutoFilled,
                isError = attemptedSave && state.employeeNameEnglish.isBlank(),
                modifier = Modifier.weight(1.3f).testTag("input_pers_emp_name_english")
            )
            // Gender Dropdown
            AppFormDropdown(
                value = state.gender,
                onValueChange = { onStateChange(state.copy(gender = it)) },
                label = "পুরুষ/মহিলা *",
                options = listOf("পুরুষ", "মহিলা"),
                placeholder = "লিঙ্গ নির্বাচন",
                modifier = Modifier.weight(1f)
            )
        }

        AppFormTextField(
            value = state.employeeNid,
            onValueChange = { onStateChange(state.copy(employeeNid = BanglaTextValidator.filterDigitsOnly(it))) },
            label = "জাতীয় পরিচয় পত্র নং (কর্মী) *",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            isError = attemptedSave && state.employeeNid.isBlank(),
            modifier = Modifier.fillMaxWidth().testTag("input_pers_emp_nid")
        )

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            AutoFilledBlockedTextField(
                value = state.fatherName,
                onValueChange = { onStateChange(state.copy(fatherName = BanglaTextValidator.filterBanglaText(it))) },
                label = "পিতার নাম *",
                isAutoFilled = isFatherAutoFilled,
                isError = attemptedSave && state.fatherName.isBlank(),
                modifier = Modifier.weight(1f)
            )
            AutoFilledBlockedTextField(
                value = state.motherName,
                onValueChange = { onStateChange(state.copy(motherName = BanglaTextValidator.filterBanglaText(it))) },
                label = "মাতার নাম *",
                isAutoFilled = isMotherAutoFilled,
                isError = attemptedSave && state.motherName.isBlank(),
                modifier = Modifier.weight(1f)
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            AppFormTextField(
                value = state.fatherNid,
                onValueChange = { onStateChange(state.copy(fatherNid = BanglaTextValidator.filterDigitsOnly(it))) },
                label = "পিতার NID",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )
            AppFormTextField(
                value = state.motherNid,
                onValueChange = { onStateChange(state.copy(motherNid = BanglaTextValidator.filterDigitsOnly(it))) },
                label = "মাতার NID",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )
        }

        AppFormTextField(
            value = state.guarantorNid,
            onValueChange = { onStateChange(state.copy(guarantorNid = BanglaTextValidator.filterDigitsOnly(it))) },
            label = "জাতীয় পরিচয় পত্র নং (জামিনদার)",
            placeholder = "জামিনদারের NID নম্বর",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth().testTag("input_pers_guarantor_nid")
        )

        Text("বৈবাহিক অবস্থা :", fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable {
                    onStateChange(state.copy(isMarried = false, spouseName = ""))
                }
            ) {
                RadioButton(
                    selected = !state.isMarried,
                    onClick = { onStateChange(state.copy(isMarried = false, spouseName = "")) }
                )
                Spacer(Modifier.width(4.dp))
                Text("অবিবাহিত", fontSize = 13.5.sp)
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable {
                    onStateChange(state.copy(isMarried = true))
                }
            ) {
                RadioButton(
                    selected = state.isMarried,
                    onClick = { onStateChange(state.copy(isMarried = true)) }
                )
                Spacer(Modifier.width(4.dp))
                Text("বিবাহিত", fontSize = 13.5.sp)
            }
        }

        if (state.isMarried) {
            AppFormTextField(
                value = state.spouseName,
                onValueChange = { onStateChange(state.copy(spouseName = BanglaTextValidator.filterBanglaText(it))) },
                label = "স্বামী/স্ত্রীর নাম *",
                isError = attemptedSave && state.isMarried && state.spouseName.isBlank(),
                modifier = Modifier.fillMaxWidth().testTag("input_pers_spouse_name")
            )
        }
    }

    // Permanent & Current Address Card
    StampThemed3DCard(
        badgeText = "২",
        title = "ঠিকানা সংক্রান্ত",
        subtitle = "স্থায়ী ও বর্তমান ঠিকানা",
        tag = "আবশ্যক"
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("ঠিকানা সংক্রান্ত বিবরণ", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(6.dp))
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Text(
                    text = "নোট: কর্মীর ঠিকানা",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
        }

        Text("স্থায়ী ঠিকানা (কর্মীর ঠিকানা) :", fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            AutoFilledBlockedTextField(
                value = state.permVillage,
                onValueChange = { v ->
                    val bVill = BanglaTextValidator.filterBanglaText(v)
                    if (isSameAddressChecked) {
                        onStateChange(state.copy(permVillage = bVill, currVillageOrHouse = bVill))
                    } else {
                        onStateChange(state.copy(permVillage = bVill))
                    }
                },
                label = "গ্রাম *",
                isAutoFilled = isPermVillAutoFilled,
                isError = attemptedSave && state.permVillage.isBlank(),
                modifier = Modifier.weight(1f)
            )
            AutoFilledBlockedTextField(
                value = state.permPostOffice,
                onValueChange = { po ->
                    val bPo = BanglaTextValidator.filterBanglaText(po)
                    val foundPc = BangladeshDistricts.lookupPostCode(state.permDistrict, state.permUpazila, bPo)
                    val autoPc = if (foundPc.isNotBlank()) foundPc else state.permPostCode
                    if (isSameAddressChecked) {
                        onStateChange(state.copy(permPostOffice = bPo, currPostOffice = bPo, permPostCode = autoPc, currPostCode = autoPc))
                    } else {
                        onStateChange(state.copy(permPostOffice = bPo, permPostCode = autoPc))
                    }
                },
                label = "ডাকঘর *",
                isAutoFilled = isPermPostAutoFilled,
                isError = attemptedSave && state.permPostOffice.isBlank(),
                modifier = Modifier.weight(1f)
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            AppFormTextField(
                value = state.permPostCode,
                onValueChange = { pc ->
                    val bDigits = BanglaTextValidator.toBanglaDigits(pc.filter { it.isDigit() || it in '০'..'৯' })
                    if (isSameAddressChecked) {
                        onStateChange(state.copy(permPostCode = bDigits, currPostCode = bDigits))
                    } else {
                        onStateChange(state.copy(permPostCode = bDigits))
                    }
                },
                label = "পোস্ট কোড",
                placeholder = "যেমন: ৭৪০০ / ৯০০০",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )
            AutoFilledBlockedTextField(
                value = state.permUpazila,
                onValueChange = { up ->
                    val bUp = BanglaTextValidator.filterBanglaText(up)
                    val foundPc = BangladeshDistricts.lookupPostCode(state.permDistrict, bUp, state.permPostOffice)
                    val autoPc = if (foundPc.isNotBlank()) foundPc else state.permPostCode
                    if (isSameAddressChecked) {
                        onStateChange(state.copy(permUpazila = bUp, currUpazila = bUp, currThana = bUp, permPostCode = autoPc, currPostCode = autoPc))
                    } else {
                        onStateChange(state.copy(permUpazila = bUp, permPostCode = autoPc))
                    }
                },
                label = "উপজেলা/থানা *",
                isAutoFilled = isPermUpazilaAutoFilled,
                isError = attemptedSave && state.permUpazila.isBlank() && state.permThana.isBlank(),
                modifier = Modifier.weight(1f)
            )
            AutoFilledBlockedTextField(
                value = state.permDistrict,
                onValueChange = { dist ->
                    val bDist = BanglaTextValidator.filterBanglaText(dist)
                    val foundPc = BangladeshDistricts.lookupPostCode(bDist, state.permUpazila, state.permPostOffice)
                    val autoPc = if (foundPc.isNotBlank()) foundPc else state.permPostCode
                    if (isSameAddressChecked) {
                        onStateChange(state.copy(permDistrict = bDist, currDistrict = bDist, permPostCode = autoPc, currPostCode = autoPc))
                    } else {
                        onStateChange(state.copy(permDistrict = bDist, permPostCode = autoPc))
                    }
                },
                label = "জেলা *",
                isAutoFilled = isPermDistAutoFilled,
                isError = attemptedSave && state.permDistrict.isBlank(),
                modifier = Modifier.weight(1f)
            )
        }

        val permBanglaAddr = BanglaAddressHelper.formatBanglaAddress(
            village = state.permVillage,
            postOffice = state.permPostOffice,
            thanaOrUpazila = state.permUpazila.ifBlank { state.permThana },
            district = state.permDistrict
        )
        val permEnglishAddr = BanglaAddressHelper.formatEnglishAddress(
            village = state.permVillage,
            postOffice = state.permPostOffice,
            thanaOrUpazila = state.permUpazila.ifBlank { state.permThana },
            district = state.permDistrict
        )

        if (permBanglaAddr.isNotBlank()) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(15.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("দ্বৈত ঠিকানা রূপান্তর (ID Card & Training Sync):", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    Text("• বাংলা: $permBanglaAddr", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                    Text("• ইংরেজি: $permEnglishAddr", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)

        // Same Address Checkbox Option
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    val newSame = !isSameAddressChecked
                    isSameAddressChecked = newSame
                    if (newSame) {
                        onStateChange(
                            state.copy(
                                currVillageOrHouse = state.permVillage,
                                currPostOffice = state.permPostOffice,
                                currPostCode = state.permPostCode,
                                currUpazila = state.permUpazila,
                                currThana = state.permThana,
                                currDistrict = state.permDistrict
                            )
                        )
                    } else {
                        onStateChange(
                            state.copy(
                                currVillageOrHouse = "",
                                currPostOffice = "",
                                currPostCode = "",
                                currUpazila = "",
                                currThana = "",
                                currDistrict = ""
                            )
                        )
                    }
                }
                .padding(vertical = 4.dp)
        ) {
            Checkbox(
                checked = isSameAddressChecked,
                onCheckedChange = { checked ->
                    isSameAddressChecked = checked
                    if (checked) {
                        onStateChange(
                            state.copy(
                                currVillageOrHouse = state.permVillage,
                                currPostOffice = state.permPostOffice,
                                currPostCode = state.permPostCode,
                                currUpazila = state.permUpazila,
                                currThana = state.permThana,
                                currDistrict = state.permDistrict
                            )
                        )
                    } else {
                        onStateChange(
                            state.copy(
                                currVillageOrHouse = "",
                                currPostOffice = "",
                                currPostCode = "",
                                currUpazila = "",
                                currThana = "",
                                currDistrict = ""
                            )
                        )
                    }
                }
            )
            Spacer(Modifier.width(6.dp))
            Text("বর্তমান ঠিকানা ও স্থায়ী ঠিকানা একই (Same as Permanent)", fontSize = 13.sp, fontWeight = FontWeight.Medium)
        }

        if (!isSameAddressChecked) {
            Text("বর্তমান ঠিকানা (কর্মীর ঠিকানা) :", fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold)
            AppFormTextField(
                value = state.currVillageOrHouse,
                onValueChange = { onStateChange(state.copy(currVillageOrHouse = BanglaTextValidator.filterBanglaText(it))) },
                label = "গ্রাম/বাড়ী *",
                isError = attemptedSave && !isSameAddressChecked && state.currVillageOrHouse.isBlank(),
                modifier = Modifier.fillMaxWidth()
            )

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                AppFormTextField(
                    value = state.currPostOffice,
                    onValueChange = { po ->
                        val bPo = BanglaTextValidator.filterBanglaText(po)
                        val foundPc = BangladeshDistricts.lookupPostCode(state.currDistrict, state.currUpazila, bPo)
                        val autoPc = if (foundPc.isNotBlank()) foundPc else state.currPostCode
                        onStateChange(state.copy(currPostOffice = bPo, currPostCode = autoPc))
                    },
                    label = "ডাকঘর *",
                    isError = attemptedSave && !isSameAddressChecked && state.currPostOffice.isBlank(),
                    modifier = Modifier.weight(1f)
                )
                AppFormTextField(
                    value = state.currPostCode,
                    onValueChange = { pc ->
                        val bDigits = BanglaTextValidator.toBanglaDigits(pc.filter { it.isDigit() || it in '০'..'৯' })
                        onStateChange(state.copy(currPostCode = bDigits))
                    },
                    label = "পোস্ট কোড",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                AppFormTextField(
                    value = state.currUpazila,
                    onValueChange = { up ->
                        val bUp = BanglaTextValidator.filterBanglaText(up)
                        val foundPc = BangladeshDistricts.lookupPostCode(state.currDistrict, bUp, state.currPostOffice)
                        val autoPc = if (foundPc.isNotBlank()) foundPc else state.currPostCode
                        onStateChange(state.copy(currUpazila = bUp, currThana = bUp, currPostCode = autoPc))
                    },
                    label = "উপজেলা/থানা *",
                    isError = attemptedSave && !isSameAddressChecked && state.currUpazila.isBlank(),
                    modifier = Modifier.weight(1f)
                )
                AppFormTextField(
                    value = state.currDistrict,
                    onValueChange = { dist ->
                        val bDist = BanglaTextValidator.filterBanglaText(dist)
                        val foundPc = BangladeshDistricts.lookupPostCode(bDist, state.currUpazila, state.currPostOffice)
                        val autoPc = if (foundPc.isNotBlank()) foundPc else state.currPostCode
                        onStateChange(state.copy(currDistrict = bDist, currPostCode = autoPc))
                    },
                    label = "জেলা *",
                    isError = attemptedSave && !isSameAddressChecked && state.currDistrict.isBlank(),
                    modifier = Modifier.weight(1f)
                )
            }
        } else {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("বর্তমান ঠিকানা স্থায়ী ঠিকানার অনুরূপ সংরক্ষিত হচ্ছে", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }

    // Contact, Cases & Financial Card - 3D Stamp Theme
    StampThemed3DCard(
        badgeText = "৩",
        title = "যোগাযোগ, জন্ম তারিখ ও অন্যান্য",
        subtitle = "মোবাইল নম্বর, বয়স ও ব্যাংক হিসাব বিবরণ",
        tag = "আবশ্যক"
    ) {
            // Contact & DOB
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                AutoFilledBlockedTextField(
                    value = state.mobileNumber,
                    onValueChange = { onStateChange(state.copy(mobileNumber = BanglaTextValidator.filterMobileInput(it))) },
                    label = "মোবাইল নং (১১ ডিজিট) *",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isAutoFilled = isMobileAutoFilled,
                    isError = attemptedSave && state.mobileNumber.isBlank(),
                    modifier = Modifier.weight(1f)
                )
                AppFormTextField(
                    value = state.alternateNumber,
                    onValueChange = { onStateChange(state.copy(alternateNumber = BanglaTextValidator.filterMobileInput(it))) },
                    label = "বিকল্প/অনুরোধ নম্বর (১১ ডিজিট)",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }

            // Date of Birth - Scrollable Selector for Day (1-31), Month (1-12), Year (1950-2015)
            BanglaScrollableDobPicker(
                value = state.dateOfBirth,
                onValueChange = { dobInput ->
                    val detailed = BanglaTextValidator.calculateDetailedAge(dobInput)
                    if (detailed != null) {
                        onStateChange(
                            state.copy(
                                dateOfBirth = dobInput,
                                ageYears = detailed.years,
                                ageMonths = detailed.months,
                                ageDays = detailed.days
                            )
                        )
                    } else {
                        onStateChange(state.copy(dateOfBirth = dobInput))
                    }
                },
                label = "কর্মীর জন্ম তারিখ *",
                modifier = Modifier.fillMaxWidth()
            )

            val ageDisplay = when {
                state.ageYears.isNotBlank() && (state.ageMonths.isNotBlank() || state.ageDays.isNotBlank()) ->
                    "${state.ageYears} বছর ${if (state.ageMonths.isNotBlank() && state.ageMonths != "০") "${state.ageMonths} মাস " else ""}${if (state.ageDays.isNotBlank() && state.ageDays != "০") "${state.ageDays} দিন" else ""}".trim()
                state.ageYears.isNotBlank() -> "${state.ageYears} বছর"
                else -> ""
            }

            if (ageDisplay.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isDarkPage1) Color(0xFF0369A1).copy(alpha = 0.25f) else Color(0xFFF0F9FF),
                    border = BorderStroke(1.dp, if (isDarkPage1) Color(0xFF0284C7) else Color(0xFFBAE6FD)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Schedule, contentDescription = null, tint = Color(0xFF0284C7), modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "স্বয়ংক্রিয় বয়স: $ageDisplay",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDarkPage1) Color(0xFF7DD3FC) else Color(0xFF0369A1)
                        )
                    }
                }
            }

            HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 0.8.dp)

            // Police Case Status Section (Tick / Radio Option with Conditional Details & Count)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("স্থানীয় থানায় কর্মীর কোন মামলা আছে/ছিল কি না :", fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable {
                            onStateChange(state.copy(hasPoliceCase = false, policeCaseCount = "", policeCaseDetails = ""))
                        }
                    ) {
                        RadioButton(
                            selected = !state.hasPoliceCase,
                            onClick = { onStateChange(state.copy(hasPoliceCase = false, policeCaseCount = "", policeCaseDetails = "")) }
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("না (কোন মামলা নেই)", fontSize = 13.5.sp)
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable {
                            onStateChange(state.copy(hasPoliceCase = true))
                        }
                    ) {
                        RadioButton(
                            selected = state.hasPoliceCase,
                            onClick = { onStateChange(state.copy(hasPoliceCase = true)) }
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "হ্যাঁ (মামলা আছে)",
                            fontSize = 13.5.sp,
                            color = if (state.hasPoliceCase) Color(0xFFDC2626) else Color.Unspecified,
                            fontWeight = if (state.hasPoliceCase) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }

                if (state.hasPoliceCase) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFFEF2F2),
                        border = BorderStroke(1.dp, Color(0xFFFECACA)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("মামলার বিবরণ ও মামলা কতটি লিখুন :", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB91C1C))
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                AppFormTextField(
                                    value = state.policeCaseCount,
                                    onValueChange = { onStateChange(state.copy(policeCaseCount = BanglaTextValidator.filterDigitsOnly(it))) },
                                    label = "মামলা কতটি (সংখ্যা) *",
                                    placeholder = "১",
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    isError = attemptedSave && state.hasPoliceCase && state.policeCaseCount.isBlank(),
                                    modifier = Modifier.weight(1f).testTag("input_pers_case_count")
                                )
                                AppFormTextField(
                                    value = state.policeCaseDetails,
                                    onValueChange = { onStateChange(state.copy(policeCaseDetails = BanglaTextValidator.filterBanglaText(it))) },
                                    label = "মামলার বিবরণ *",
                                    placeholder = "মামলার নম্বর, ধারা বা বিবরণ",
                                    isError = attemptedSave && state.hasPoliceCase && state.policeCaseDetails.isBlank(),
                                    modifier = Modifier.weight(1.8f).testTag("input_pers_case_details")
                                )
                            }
                        }
                    }
                }
            }

            HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 0.8.dp)

            // Income Section (আয়ের ছক - কর্মী ও জামিনদারের আয়)
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isDarkPage1) Color(0xFF042F2E).copy(alpha = 0.35f) else Color(0xFFF8FAFC),
                border = BorderStroke(1.dp, if (isDarkPage1) Color(0xFF0D9488).copy(alpha = 0.5f) else Color(0xFFCBD5E1)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = if (isDarkPage1) Color(0xFF0D9488).copy(alpha = 0.3f) else Color(0xFFE6FFFA),
                            modifier = Modifier.size(22.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "৳",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDarkPage1) Color(0xFF2DD4BF) else Color(0xFF0F766E)
                                )
                            }
                        }
                        Spacer(Modifier.width(6.dp))
                        Text("আয়ের বিবরণীর ছক (কর্মী ও জামিনদার)", fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = if (isDarkPage1) Color(0xFF2DD4BF) else Color(0xFF0F766E))
                    }

                    // Worker Monthly & Yearly Income
                    Text("• কর্মীর মাসিক ও বাৎসরিক আয় :", fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AppFormTextField(
                            value = state.monthlyIncome,
                            onValueChange = { input ->
                                val mDigits = BanglaTextValidator.filterBanglaDigitsOnly(input)
                                val mWords = BanglaTextValidator.convertNumberToBanglaWords(mDigits)
                                val engVal = BanglaTextValidator.toEnglishDigits(mDigits).toLongOrNull() ?: 0L
                                val yVal = if (engVal > 0) (engVal * 12).toString() else ""
                                val yDigits = BanglaTextValidator.toBanglaDigits(yVal)
                                val yWords = BanglaTextValidator.convertNumberToBanglaWords(yDigits)
                                onStateChange(state.copy(
                                    monthlyIncome = mDigits,
                                    monthlyIncomeWords = mWords,
                                    yearlyIncome = if (yDigits.isNotBlank()) yDigits else state.yearlyIncome,
                                    yearlyIncomeWords = if (yWords.isNotBlank()) yWords else state.yearlyIncomeWords
                                ))
                            },
                            label = "মাসিক আয় (সংখ্যা) *",
                            placeholder = "২৫০০০",
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f).testTag("input_pers_monthly_income")
                        )
                        AppFormTextField(
                            value = state.monthlyIncomeWords,
                            onValueChange = { onStateChange(state.copy(monthlyIncomeWords = BanglaTextValidator.filterBanglaText(it))) },
                            label = "মাসিক আয় (কথায়)",
                            modifier = Modifier.weight(1.3f)
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AppFormTextField(
                            value = state.yearlyIncome,
                            onValueChange = { input ->
                                val yDigits = BanglaTextValidator.filterBanglaDigitsOnly(input)
                                val yWords = BanglaTextValidator.convertNumberToBanglaWords(yDigits)
                                onStateChange(state.copy(
                                    yearlyIncome = yDigits,
                                    yearlyIncomeWords = yWords
                                ))
                            },
                            label = "বাৎসরিক আয় (সংখ্যা)",
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        AppFormTextField(
                            value = state.yearlyIncomeWords,
                            onValueChange = { onStateChange(state.copy(yearlyIncomeWords = BanglaTextValidator.filterBanglaText(it))) },
                            label = "বাৎসরিক আয় (কথায়)",
                            modifier = Modifier.weight(1.3f)
                        )
                    }

                    // Guarantor Monthly & Annual Income (ছকে জামিনদারের মাসিক ও বাৎসরিক আয় - ছকের মাঝে ফাঁকা স্থান কমানো হয়েছে)
                    Row(
                        modifier = Modifier.padding(top = 1.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = if (isDarkPage1) Color(0xFF1E3A8A).copy(alpha = 0.5f) else Color(0xFFDBEAFE)
                        ) {
                            Text("ছকে আবশ্যক", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (isDarkPage1) Color(0xFF93C5FD) else Color(0xFF1E40AF), modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                        Spacer(Modifier.width(6.dp))
                        Text("• জামিনদারের মাসিক ও বাৎসরিক আয় :", fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = if (isDarkPage1) Color(0xFF93C5FD) else Color(0xFF1E40AF))
                    }

                    // Row 1: জামিনদারের মাসিক আয় (উপরে)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AppFormTextField(
                            value = state.guardianMonthlyIncome,
                            onValueChange = { input ->
                                val gMDigits = BanglaTextValidator.filterBanglaDigitsOnly(input)
                                if (gMDigits.isBlank()) {
                                    onStateChange(state.copy(
                                        guardianMonthlyIncome = "",
                                        guardianMonthlyIncomeWords = "",
                                        guardianYearlyIncome = "",
                                        guardianYearlyIncomeWords = ""
                                    ))
                                } else {
                                    val gMWords = BanglaTextValidator.convertNumberToBanglaWords(gMDigits)
                                    val engVal = BanglaTextValidator.toEnglishDigits(gMDigits).toLongOrNull() ?: 0L
                                    val gYVal = if (engVal > 0) (engVal * 12).toString() else ""
                                    val gYDigits = BanglaTextValidator.toBanglaDigits(gYVal)
                                    val gYWords = BanglaTextValidator.convertNumberToBanglaWords(gYDigits)
                                    onStateChange(state.copy(
                                        guardianMonthlyIncome = gMDigits,
                                        guardianMonthlyIncomeWords = gMWords,
                                        guardianYearlyIncome = gYDigits,
                                        guardianYearlyIncomeWords = gYWords
                                    ))
                                }
                            },
                            label = "জামিনদারের মাসিক আয় (সংখ্যা) *",
                            placeholder = "৩০০০০",
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f).testTag("input_pers_guardian_monthly_income")
                        )
                        AppFormTextField(
                            value = state.guardianMonthlyIncomeWords,
                            onValueChange = { onStateChange(state.copy(guardianMonthlyIncomeWords = BanglaTextValidator.filterBanglaText(it))) },
                            label = "মাসিক আয় (কথায়)",
                            modifier = Modifier.weight(1.3f)
                        )
                    }

                    // Row 2: জামিনদারের বাৎসরিক আয় (নিচে)
                    Spacer(Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AppFormTextField(
                            value = state.guardianYearlyIncome,
                            onValueChange = { input ->
                                val gYDigits = BanglaTextValidator.filterBanglaDigitsOnly(input)
                                if (gYDigits.isBlank()) {
                                    onStateChange(state.copy(
                                        guardianYearlyIncome = "",
                                        guardianYearlyIncomeWords = ""
                                    ))
                                } else {
                                    val gYWords = BanglaTextValidator.convertNumberToBanglaWords(gYDigits)
                                    onStateChange(state.copy(
                                        guardianYearlyIncome = gYDigits,
                                        guardianYearlyIncomeWords = gYWords
                                    ))
                                }
                            },
                            label = "জামিনদারের বাৎসরিক আয় (সংখ্যা)",
                            placeholder = "৩৬০০০০",
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f).testTag("input_pers_guardian_yearly_income")
                        )
                        AppFormTextField(
                            value = state.guardianYearlyIncomeWords,
                            onValueChange = { onStateChange(state.copy(guardianYearlyIncomeWords = BanglaTextValidator.filterBanglaText(it))) },
                            label = "বাৎসরিক আয় (কথায়)",
                            modifier = Modifier.weight(1.3f)
                        )
                    }
                }
            }

            // Optional Information Card - Direct Front View (No Collapsible Arrow)
            val isDarkOpt = isSystemInDarkTheme()

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (state.hasNoBankOrPropertyInfo) (if (isDarkOpt) Color(0xFF064E3B).copy(alpha = 0.35f) else Color(0xFFF0FDF4)) else MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, if (state.hasNoBankOrPropertyInfo) Color(0xFF16A34A) else Color(0xFFCBD5E1)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onStateChange(state.copy(hasNoBankOrPropertyInfo = !state.hasNoBankOrPropertyInfo))
                    }
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = state.hasNoBankOrPropertyInfo,
                        onCheckedChange = { onStateChange(state.copy(hasNoBankOrPropertyInfo = it)) },
                        colors = CheckboxDefaults.colors(checkedColor = Color(0xFF16A34A))
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "নোট: ব্যাংক, ড্রাইভিং লাইসেন্স, টিন ও সম্পত্তির তথ্য নেই (না থাকলে টিক দিন)",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (state.hasNoBankOrPropertyInfo) Color(0xFF16A34A) else MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            if (!state.hasNoBankOrPropertyInfo) {
                StampThemed3DCard(
                    badgeText = "৩.১",
                    title = "অন্যান্য ঐচ্ছিক তথ্য",
                    subtitle = "ব্যাংক, লাইসেন্স, পাসপোর্ট, টিন ও সম্পত্তি বিবরণ",
                    tag = "ঐচ্ছিক"
                ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AppFormTextField(
                        value = state.drivingLicenseNo,
                        onValueChange = { onStateChange(state.copy(drivingLicenseNo = BanglaTextValidator.filterDigitsOnly(it))) },
                        label = "ড্রাইভিং লাইসেন্স নং",
                        placeholder = "লাইসেন্স নম্বর",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    AppFormTextField(
                        value = state.passportNo,
                        onValueChange = { onStateChange(state.copy(passportNo = BanglaTextValidator.filterDigitsOnly(it))) },
                        label = "পাসপোর্ট নং",
                        placeholder = "পাসপোর্ট নম্বর",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AppFormTextField(
                        value = state.bankAccountNo,
                        onValueChange = { onStateChange(state.copy(bankAccountNo = BanglaTextValidator.filterDigitsOnly(it))) },
                        label = "ব্যাংক অ্যাকাউন্ট নম্বর",
                        placeholder = "হিসাব নম্বর",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1.3f)
                    )
                    AppFormDropdown(
                        value = state.bankAccountType,
                        onValueChange = { onStateChange(state.copy(bankAccountType = it)) },
                        label = "হিসাবের ধরণ",
                        options = listOf("চলতি", "সঞ্চয়ী", "স্থায়ী"),
                        placeholder = "নির্বাচন করুন",
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AppFormTextField(
                        value = state.bankName,
                        onValueChange = { onStateChange(state.copy(bankName = BanglaTextValidator.filterBanglaText(it))) },
                        label = "ব্যাংকের নাম (বাংলায়)",
                        placeholder = "ব্যাংকের নাম",
                        modifier = Modifier.weight(1.2f)
                    )
                    AppFormTextField(
                        value = state.branchName,
                        onValueChange = { onStateChange(state.copy(branchName = BanglaTextValidator.filterBanglaText(it))) },
                        label = " শাখা (বাংলায়)",
                        placeholder = "শাখার নাম",
                        modifier = Modifier.weight(1f)
                    )
                }

                AppFormTextField(
                    value = state.tinNumber,
                    onValueChange = { onStateChange(state.copy(tinNumber = BanglaTextValidator.filterDigitsOnly(it))) },
                    label = "TIN নম্বর",
                    placeholder = "টি আই এন নম্বর",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                AppFormTextField(
                    value = state.propertyDetails,
                    onValueChange = { onStateChange(state.copy(propertyDetails = BanglaTextValidator.filterBanglaText(it))) },
                    label = "সম্পত্তির বিবরণ (বাংলায়)",
                    placeholder = "জমিজমা/বাড়ি/অন্যান্য বিবরণ",
                    modifier = Modifier.fillMaxWidth()
                )
              }
            }
        }
}

// -------------------------------------------------------------------------
// Tab 2: Education & Experience (Page 2)
// -------------------------------------------------------------------------
@Composable
fun Page2EducationExperience(
    state: PersonalInfoFormState,
    attemptedSave: Boolean = false,
    isServiceStaff: Boolean = false,
    onStateChange: (PersonalInfoFormState) -> Unit
) {
    val sscHscGroupOptions = listOf(
        "বিজ্ঞান",
        "ব্যবসায় শিক্ষা / বাণিজ্য",
        "মানবিক",
        "অন্যান্য"
    )

    val sscHscBoardOptions = listOf(
        "ঢাকা",
        "রাজশাহী",
        "কুমিল্লা",
        "যশোর",
        "চট্টগ্রাম",
        "বরিশাল",
        "সিলেট",
        "দিনাজপুর",
        "ময়মনসিংহ",
        "বাংলাদেশ মাদ্রাসা শিক্ষা বোর্ড",
        "বাংলাদেশ কারিগরি শিক্ষা বোর্ড",
        "অন্যান্য"
    )

    val degreeBoardOptions = listOf(
        "জাতীয় বিশ্ববিদ্যালয়",
        "ঢাকা বিশ্ববিদ্যালয়",
        "রাজশাহী বিশ্ববিদ্যালয়",
        "চট্টগ্রাম বিশ্ববিদ্যালয়",
        "জাহাঙ্গীরনগর বিশ্ববিদ্যালয়",
        "বাংলাদেশ উন্মুক্ত বিশ্ববিদ্যালয়",
        "ইসলামী বিশ্ববিদ্যালয়",
        "শাহজালাল বিজ্ঞান ও প্রযুক্তি বিশ্ববিদ্যালয়",
        "খুলনা বিশ্ববিদ্যালয়",
        "জগন্নাথ বিশ্ববিদ্যালয়",
        "কুমিল্লা বিশ্ববিদ্যালয়",
        "জাতীয় কবি কাজী নজরুল ইসলাম বিশ্ববিদ্যালয়",
        "বেগম রোকেয়া বিশ্ববিদ্যালয়",
        "বাংলাদেশ ইউনিভার্সিটি অব প্রফেশনালস (BUP)",
        "বেসরকারি বিশ্ববিদ্যালয়",
        "অন্যান্য"
    )

    val degreeSubjectOptions = listOf(
        "হিসাববিজ্ঞান",
        "ব্যবস্থাপনা",
        "ফিন্যান্স ও ব্যাংকিং",
        "মার্কেটিং",
        "অর্থনীতি",
        "বাংলা",
        "ইংরেজি",
        "ইতিহাস",
        "ভূগোল",
        "দর্শন",
        "রাষ্ট্র বিজ্ঞান",
        "সমাজ বিজ্ঞান",
        "আইন",
        "গণিত",
        "পরিসংখ্যান",
        "পদার্থবিজ্ঞান",
        "রসায়ন",
        "উদ্ভিদবিজ্ঞান",
        "প্রাণিবিজ্ঞান",
        "কম্পিউটার সায়েন্স / আইসিটি",
        "অন্যান্য"
    )

    StampThemed3DCard(
        badgeText = "৪",
        title = "শিক্ষাগত যোগ্যতা",
        subtitle = "পৃষ্ঠা ২: এসএসসি, এইচএসসি, স্নাতক ও অন্যান্য বিবরণ",
        tag = "আবশ্যক"
    ) {
            val isDarkEdu = isSystemInDarkTheme()

            state.educations.forEachIndexed { i, edu ->
                val isSscHsc = i == 0 || i == 1
                val isEduMandatory = if (isServiceStaff) i == 0 else i <= 2

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isDarkEdu) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else Color(0xFFF8FAFC),
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isDarkEdu) Color(0xFF475569) else Color(0xFFE2E8F0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("${edu.degreeName}${if (isEduMandatory) " *" else ""}", fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        
                        // Row 1: Institute Name & Subject/Group Dropdown
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            AppFormTextField(
                                value = edu.instituteName,
                                onValueChange = { v ->
                                    val list = state.educations.toMutableList()
                                    list[i] = list[i].copy(instituteName = BanglaTextValidator.filterBanglaText(v))
                                    onStateChange(state.copy(educations = list))
                                },
                                label = if (isEduMandatory) "শিক্ষা প্রতিষ্ঠান *" else "শিক্ষা প্রতিষ্ঠান",
                                isError = attemptedSave && isEduMandatory && edu.instituteName.isBlank(),
                                modifier = Modifier.weight(1.3f)
                            )

                            // Subject / Group Dropdown
                            val options = if (isSscHsc) sscHscGroupOptions else degreeSubjectOptions
                            val isCustomSubj = edu.subjectGroup.isNotBlank() && !options.contains(edu.subjectGroup)

                            AppFormDropdown(
                                value = if (isCustomSubj) "অন্যান্য: ${edu.subjectGroup}" else edu.subjectGroup,
                                onValueChange = { opt ->
                                    val list = state.educations.toMutableList()
                                    list[i] = list[i].copy(subjectGroup = if (opt == "অন্যান্য") "" else opt)
                                    onStateChange(state.copy(educations = list))
                                },
                                label = if (isSscHsc) "বিভাগ/গ্রুপ *" else "বিষয়/বিভাগ",
                                options = options,
                                placeholder = if (isSscHsc) "বিভাগ/গ্রুপ" else "বিষয়/বিভাগ",
                                modifier = Modifier.weight(1.1f)
                            )
                        }

                        // If "অন্যান্য" or custom subject selected
                        val currentSubjOptions = if (isSscHsc) sscHscGroupOptions else degreeSubjectOptions
                        if (edu.subjectGroup == "অন্যান্য" || (edu.subjectGroup.isNotBlank() && !currentSubjOptions.contains(edu.subjectGroup))) {
                            AppFormTextField(
                                value = edu.subjectGroup,
                                onValueChange = { v ->
                                    val list = state.educations.toMutableList()
                                    list[i] = list[i].copy(subjectGroup = BanglaTextValidator.filterBanglaText(v))
                                    onStateChange(state.copy(educations = list))
                                },
                                label = "অন্যান্য বিষয়/বিভাগ লিখুন (বাংলায়)",
                                placeholder = "বিষয়ের নাম লিখুন",
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        // Row 2A: Board / University Dropdown
                        val boardList = if (isSscHsc) sscHscBoardOptions else degreeBoardOptions
                        val isCustomBoard = edu.boardOrUniversity.isNotBlank() && !boardList.contains(edu.boardOrUniversity)

                        AppFormDropdown(
                            value = if (isCustomBoard) "অন্যান্য: ${edu.boardOrUniversity}" else edu.boardOrUniversity,
                            onValueChange = { opt ->
                                val list = state.educations.toMutableList()
                                list[i] = list[i].copy(boardOrUniversity = if (opt == "অন্যান্য") "" else opt)
                                onStateChange(state.copy(educations = list))
                            },
                            label = if (isSscHsc) "বোর্ড *" else "বোর্ড/বিশ্ববিদ্যালয়",
                            options = boardList,
                            placeholder = if (isSscHsc) "বোর্ড নির্বাচন" else "বিশ্ববিদ্যালয় নির্বাচন",
                            modifier = Modifier.fillMaxWidth()
                        )

                        // If "অন্যান্য" or custom board selected
                        val curBoardList = if (isSscHsc) sscHscBoardOptions else degreeBoardOptions
                        if (edu.boardOrUniversity == "অন্যান্য" || (edu.boardOrUniversity.isNotBlank() && !curBoardList.contains(edu.boardOrUniversity))) {
                            AppFormTextField(
                                value = edu.boardOrUniversity,
                                onValueChange = { v ->
                                    val list = state.educations.toMutableList()
                                    list[i] = list[i].copy(boardOrUniversity = BanglaTextValidator.filterBanglaText(v))
                                    onStateChange(state.copy(educations = list))
                                },
                                label = "অন্যান্য বোর্ড/বিশ্ববিদ্যালয়ের নাম লিখুন (বাংলায়)",
                                placeholder = "বিশ্ববিদ্যালয়/বোর্ডের নাম",
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        // Row 2B: Spacious GPA/Marks & Passing Year
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            AppFormTextField(
                                value = edu.marksOrCgpa,
                                onValueChange = { v ->
                                    val filtered = BanglaTextValidator.filterGpaInput(v)
                                    val list = state.educations.toMutableList()
                                    list[i] = list[i].copy(marksOrCgpa = filtered)
                                    onStateChange(state.copy(educations = list))
                                },
                                label = "জিপিএ/ফল",
                                placeholder = "যেমন: ৫.০০",
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )

                            AppFormTextField(
                                value = edu.passingYear,
                                onValueChange = { v ->
                                    val filtered = BanglaTextValidator.filterPassingYear(v)
                                    val list = state.educations.toMutableList()
                                    list[i] = list[i].copy(passingYear = filtered)
                                    onStateChange(state.copy(educations = list))
                                },
                                label = "পাসের বছর (সন)",
                                placeholder = "যেমন: ২০২০",
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

    StampThemed3DCard(
        badgeText = "৫",
        title = "কর্মীর পেশাগত অভিজ্ঞতা",
        subtitle = "পূর্ববর্তী চাকুরীর বিবরণ (যদি থাকে)",
        tag = "ঐচ্ছিক"
    ) {
        val isDarkExp = isSystemInDarkTheme()

        // Checkbox: কোনো পেশাগত অভিজ্ঞতা নেই
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = if (state.hasNoExperience) Color(0xFFFEF3C7) else if (isDarkExp) MaterialTheme.colorScheme.surfaceVariant else Color(0xFFF1F5F9),
            border = BorderStroke(1.dp, if (state.hasNoExperience) Color(0xFFF59E0B) else if (isDarkExp) Color(0xFF475569) else Color(0xFFCBD5E1)),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onStateChange(state.copy(hasNoExperience = !state.hasNoExperience)) }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = state.hasNoExperience,
                    onCheckedChange = { onStateChange(state.copy(hasNoExperience = it)) },
                    colors = CheckboxDefaults.colors(checkedColor = Color(0xFFD97706))
                )
                Spacer(Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "কর্মীর কোনো পেশাগত অভিজ্ঞতা নেই",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (state.hasNoExperience) Color(0xFF92400E) else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "টিক দিলে নিচের ছকটি হাইড থাকবে কিন্তু পিডিএফ প্রিন্টে ছক অপরিবর্তিত থাকবে",
                        fontSize = 11.5.sp,
                        color = if (state.hasNoExperience) Color(0xFFB45309) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        if (state.hasNoExperience) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFECFDF5),
                border = BorderStroke(1.dp, Color(0xFFA7F3D0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF059669), modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "কোনো পেশাগত অভিজ্ঞতা নেই চিহ্নিত করা হয়েছে। ছকটি হাইড করা হয়েছে (পিডিএফ ফরম্যাটে ছকটি অপরিবর্তিত থাকবে)।",
                        fontSize = 12.5.sp,
                        color = Color(0xFF065F46),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        } else {
            state.experiences.forEachIndexed { i, exp ->
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isDarkExp) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else Color(0xFFF8FAFC),
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isDarkExp) Color(0xFF475569) else Color(0xFFE2E8F0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("অভিজ্ঞতা #${i + 1}", fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        AppFormTextField(
                            value = exp.organizationName,
                            onValueChange = { v ->
                                val list = state.experiences.toMutableList()
                                list[i] = list[i].copy(organizationName = BanglaTextValidator.filterBanglaText(v))
                                onStateChange(state.copy(experiences = list))
                            },
                            label = "প্রতিষ্ঠানের নাম (ফোন নম্বরসহ)",
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            AppFormTextField(
                                value = exp.designation,
                                onValueChange = { v ->
                                    val list = state.experiences.toMutableList()
                                    list[i] = list[i].copy(designation = BanglaTextValidator.filterBanglaText(v))
                                    onStateChange(state.copy(experiences = list))
                                },
                                label = "পদবী",
                                modifier = Modifier.weight(1f)
                            )
                            AppFormTextField(
                                value = exp.periodFromTo,
                                onValueChange = { v ->
                                    val list = state.experiences.toMutableList()
                                    list[i] = list[i].copy(periodFromTo = BanglaTextValidator.filterBanglaText(v))
                                    onStateChange(state.copy(experiences = list))
                                },
                                label = "সময়কাল (হতে-পর্যন্ত)",
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            AppFormTextField(
                                value = exp.duration,
                                onValueChange = { v ->
                                    val list = state.experiences.toMutableList()
                                    list[i] = list[i].copy(duration = BanglaTextValidator.filterBanglaText(v))
                                    onStateChange(state.copy(experiences = list))
                                },
                                label = "মেয়াদ",
                                modifier = Modifier.weight(1f)
                            )
                            AppFormTextField(
                                value = exp.duties,
                                onValueChange = { v ->
                                    val list = state.experiences.toMutableList()
                                    list[i] = list[i].copy(duties = BanglaTextValidator.filterBanglaText(v))
                                    onStateChange(state.copy(experiences = list))
                                },
                                label = "সম্পাদিত দায়িত্ব",
                                modifier = Modifier.weight(1.3f)
                            )
                        }
                    }
                }
            }

            AppFormTextField(
                value = state.totalExperience,
                onValueChange = { onStateChange(state.copy(totalExperience = BanglaTextValidator.filterBanglaText(it))) },
                label = "মোট অর্জিত অভিজ্ঞতার মেয়াদ (যেমন: ২ বছর ৬ মাস)",
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    StampThemed3DCard(
        badgeText = "৬",
        title = "কর্মীর প্রশিক্ষণ সংক্রান্ত তথ্য",
        subtitle = "প্রশিক্ষণ বিবরণ (যদি থাকে)",
        tag = "ঐচ্ছিক"
    ) {
        val isDarkTr = isSystemInDarkTheme()

        // Checkbox: কোনো প্রশিক্ষণ তথ্য নেই
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = if (state.hasNoTraining) Color(0xFFFEF3C7) else if (isDarkTr) MaterialTheme.colorScheme.surfaceVariant else Color(0xFFF1F5F9),
            border = BorderStroke(1.dp, if (state.hasNoTraining) Color(0xFFF59E0B) else if (isDarkTr) Color(0xFF475569) else Color(0xFFCBD5E1)),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onStateChange(state.copy(hasNoTraining = !state.hasNoTraining)) }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = state.hasNoTraining,
                    onCheckedChange = { onStateChange(state.copy(hasNoTraining = it)) },
                    colors = CheckboxDefaults.colors(checkedColor = Color(0xFFD97706))
                )
                Spacer(Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "কর্মীর কোনো প্রশিক্ষণ সংক্রান্ত তথ্য নেই",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (state.hasNoTraining) Color(0xFF92400E) else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "টিক দিলে নিচের ছকটি হাইড থাকবে কিন্তু পিডিএফ প্রিন্টে ছক অপরিবর্তিত থাকবে",
                        fontSize = 11.5.sp,
                        color = if (state.hasNoTraining) Color(0xFFB45309) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        if (state.hasNoTraining) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFECFDF5),
                border = BorderStroke(1.dp, Color(0xFFA7F3D0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF0F3B7E), modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "কোনো প্রশিক্ষণ তথ্য নেই চিহ্নিত করা হয়েছে। ছকটি হাইড করা হয়েছে (পিডিএফ ফরম্যাটে ছকটি অপরিবর্তিত থাকবে)।",
                        fontSize = 12.5.sp,
                        color = Color(0xFF065F46),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        } else {
            state.trainings.forEachIndexed { i, tr ->
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isDarkTr) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else Color(0xFFF8FAFC),
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isDarkTr) Color(0xFF475569) else Color(0xFFE2E8F0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("প্রশিক্ষণ #${i + 1}", fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        AppFormTextField(
                            value = tr.organizationName,
                            onValueChange = { v ->
                                val list = state.trainings.toMutableList()
                                list[i] = list[i].copy(organizationName = BanglaTextValidator.filterBanglaText(v))
                                onStateChange(state.copy(trainings = list))
                            },
                            label = "প্রতিষ্ঠানের নাম",
                            modifier = Modifier.fillMaxWidth()
                        )
                        AppFormTextField(
                            value = tr.trainingTopic,
                            onValueChange = { v ->
                                val list = state.trainings.toMutableList()
                                list[i] = list[i].copy(trainingTopic = BanglaTextValidator.filterBanglaText(v))
                                onStateChange(state.copy(trainings = list))
                            },
                            label = "প্রশিক্ষণের বিষয়/সনদপত্রের নাম",
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            AppFormTextField(
                                value = tr.period,
                                onValueChange = { v ->
                                    val list = state.trainings.toMutableList()
                                    list[i] = list[i].copy(period = BanglaTextValidator.filterBanglaText(v))
                                    onStateChange(state.copy(trainings = list))
                                },
                                label = "সময়কাল",
                                modifier = Modifier.weight(1f)
                            )
                            AppFormTextField(
                                value = tr.duration,
                                onValueChange = { v ->
                                    val list = state.trainings.toMutableList()
                                    list[i] = list[i].copy(duration = BanglaTextValidator.filterBanglaText(v))
                                    onStateChange(state.copy(trainings = list))
                                },
                                label = "মেয়াদ",
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------------------
// Tab 3: Family & Guarantor (Page 3)
// -------------------------------------------------------------------------
@Composable
fun Page3FamilyGuarantor(
    state: PersonalInfoFormState,
    stampState: FormState,
    allAgreementForms: AllAgreementForms,
    attemptedSave: Boolean = false,
    speakGuarantorIncome: (String, String, String) -> Unit = { _, _, _ -> },
    onStateChange: (PersonalInfoFormState) -> Unit,
    onNavigateToVerification: () -> Unit
) {
    val isGuarantorNameAutoFilled = state.guarantorName.isNotBlank() && (stampState.guarantorName.isNotBlank() || state.guarantorName == state.fatherName || state.guarantorName == state.motherName)
    val isGuarantorFatherAutoFilled = state.guarantorFatherName.isNotBlank() && stampState.guarantorFatherName.isNotBlank()
    val isGuarantorMotherAutoFilled = state.guarantorMotherName.isNotBlank() && stampState.guarantorMotherName.isNotBlank()
    val isGuarantorVillAutoFilled = state.guarantorVillage.isNotBlank() && (stampState.guarantorVillage.isNotBlank() || state.guarantorSameAddress)
    val isGuarantorPostAutoFilled = state.guarantorPostOffice.isNotBlank() && (stampState.guarantorPostOffice.isNotBlank() || state.guarantorSameAddress)
    val isGuarantorUpazilaAutoFilled = state.guarantorUpazila.isNotBlank() && (stampState.guarantorUpazila.isNotBlank() || state.guarantorSameAddress)
    val isGuarantorDistAutoFilled = state.guarantorDistrict.isNotBlank() && (stampState.guarantorDistrict.isNotBlank() || state.guarantorSameAddress)

    val eduDropdownOptions = listOf("মাষ্টার্স", "অনার্স", "এইচ.এস.সি", "এস.এস.সি", "জি.এস.সি", "অন্যান্য")
    val occDropdownOptions = listOf("সরকারী চাকরি", "চিকিৎসক", "বেসরকারি চাকরি", "ব্যবসায়", "কৃষি", "দিনমজুর", "গৃহিনী", "অন্যান্য")

    StampThemed3DCard(
        badgeText = "৭",
        title = "কর্মীর পরিবার সংক্রান্ত তথ্য",
        subtitle = "পিতা/মাতা/ভাই/বোন/স্ত্রী/স্বামী বিবরণ",
        tag = "আবশ্যক"
    ) {
        val isDarkFam = isSystemInDarkTheme()
        Text("কর্মীর পরিবার সংক্রান্ত তথ্য", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

        val famRelOptions = listOf("স্ত্রী", "স্বামী", "ভাই", "বোন", "পিতা", "মাতা", "পুত্র", "কন্যা", "শ্বশুর", "শাশুড়ী", "অন্যান্য")

        state.familyMembers.forEachIndexed { i, fm ->
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (isDarkFam) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else Color(0xFFF8FAFC),
                border = androidx.compose.foundation.BorderStroke(1.dp, if (isDarkFam) Color(0xFF475569) else Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val memberTitle = if (fm.relationship.isNotBlank()) "সদস্য #${i + 1} (${fm.relationship})" else "সদস্য #${i + 1}"
                    Text(memberTitle, fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)

                    val isCustomFamRel = fm.relationship.isNotBlank() && !famRelOptions.contains(fm.relationship)

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AppFormTextField(
                            value = fm.name,
                            onValueChange = { v ->
                                val list = state.familyMembers.toMutableList()
                                list[i] = list[i].copy(name = BanglaTextValidator.filterBanglaText(v))
                                onStateChange(state.copy(familyMembers = list))
                            },
                            label = "সদস্যের নাম (বাংলায়)",
                            placeholder = "সদস্যের নাম লিখুন",
                            modifier = Modifier.weight(1.3f)
                        )

                        AppFormDropdown(
                            value = if (isCustomFamRel) "অন্যান্য: ${fm.relationship}" else fm.relationship,
                            onValueChange = { opt ->
                                val list = state.familyMembers.toMutableList()
                                val newRel = if (opt == "অন্যান্য") "" else opt
                                list[i] = list[i].copy(relationship = newRel)
                                onStateChange(state.copy(familyMembers = list))
                            },
                            label = "সম্পর্ক",
                            options = famRelOptions,
                            placeholder = "সম্পর্ক নির্বাচন",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    if (fm.relationship == "অন্যান্য" || isCustomFamRel) {
                        AppFormTextField(
                            value = fm.relationship,
                            onValueChange = { v ->
                                val list = state.familyMembers.toMutableList()
                                list[i] = list[i].copy(relationship = BanglaTextValidator.filterBanglaText(v))
                                onStateChange(state.copy(familyMembers = list))
                            },
                            label = "অন্যান্য সম্পর্ক লিখুন (বাংলায়)",
                            placeholder = "যেমন: চাচা / মামা / ভাইপো",
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Educational Qualification & Occupation Dropdowns with "অন্যান্য" option
                    val isCustomEdu = fm.education.isNotBlank() && !eduDropdownOptions.contains(fm.education)
                    val isCustomOcc = fm.occupation.isNotBlank() && !occDropdownOptions.contains(fm.occupation)

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AppFormDropdown(
                            value = if (isCustomEdu) "অন্যান্য: ${fm.education}" else fm.education,
                            onValueChange = { opt ->
                                val list = state.familyMembers.toMutableList()
                                list[i] = list[i].copy(education = if (opt == "অন্যান্য") "" else opt)
                                onStateChange(state.copy(familyMembers = list))
                            },
                            label = "শিক্ষাগত যোগ্যতা",
                            options = eduDropdownOptions,
                            placeholder = "শিক্ষাগত যোগ্যতা",
                            modifier = Modifier.weight(1f)
                        )

                        // Occupation Dropdown with "অন্যান্য" option
                        AppFormDropdown(
                            value = if (isCustomOcc) "অন্যান্য: ${fm.occupation}" else fm.occupation,
                            onValueChange = { opt ->
                                val list = state.familyMembers.toMutableList()
                                list[i] = list[i].copy(occupation = if (opt == "অন্যান্য") "" else opt)
                                onStateChange(state.copy(familyMembers = list))
                            },
                            label = "পেশা",
                            options = occDropdownOptions,
                            placeholder = "পেশা নির্বাচন",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Custom fields if "অন্যান্য" or non-standard selected
                    if (isCustomEdu || fm.education == "অন্যান্য") {
                        AppFormTextField(
                            value = if (fm.education == "অন্যান্য") "" else fm.education,
                            onValueChange = { v ->
                                val list = state.familyMembers.toMutableList()
                                list[i] = list[i].copy(education = BanglaTextValidator.filterBanglaText(v))
                                onStateChange(state.copy(familyMembers = list))
                            },
                            label = "অন্যান্য শিক্ষাগত যোগ্যতা লিখুন (বাংলায়)",
                            placeholder = "যেমন: কামিল / ডিপ্লোমা",
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    if (isCustomOcc || fm.occupation == "অন্যান্য") {
                        AppFormTextField(
                            value = if (fm.occupation == "অন্যান্য") "" else fm.occupation,
                            onValueChange = { v ->
                                val list = state.familyMembers.toMutableList()
                                list[i] = list[i].copy(occupation = BanglaTextValidator.filterBanglaText(v))
                                onStateChange(state.copy(familyMembers = list))
                            },
                            label = "অন্যান্য পেশা লিখুন (বাংলায়)",
                            placeholder = "যেমন: শিক্ষকতা / ড্রাইভার",
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AppFormTextField(
                            value = fm.mobileNumber,
                            onValueChange = { v ->
                                val filtered = BanglaTextValidator.filterMobileInput(v)
                                val list = state.familyMembers.toMutableList()
                                list[i] = list[i].copy(mobileNumber = filtered)
                                onStateChange(state.copy(familyMembers = list))
                            },
                            label = "মোবাইল নং (সর্বোচ্চ ১১ ডিজিট)",
                            placeholder = "০১XXXXXXXXX",
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1.2f)
                        )
                        AppFormTextField(
                            value = fm.age,
                            onValueChange = { v ->
                                val filtered = BanglaTextValidator.filterAgeInput(v)
                                val list = state.familyMembers.toMutableList()
                                list[i] = list[i].copy(age = filtered)
                                onStateChange(state.copy(familyMembers = list))
                            },
                            label = "বয়স (সর্বোচ্চ ২ ডিজিট)",
                            placeholder = "যেমন: ৪৫",
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(0.8f)
                        )
                    }
                }
            }
        }
    }

    StampThemed3DCard(
        badgeText = "৮",
        title = "অঙ্গীকারনামা প্রদানকারী (জামিনদার) বিবরণ",
        subtitle = "পৃষ্ঠা ৩: জামিনদারের ব্যক্তিগত ও ঠিকানা সংক্রান্ত তথ্য",
        tag = "আবশ্যক"
    ) {
        val isDarkG = isSystemInDarkTheme()
        Text("পৃষ্ঠা ৩: অঙ্গীকারনামা প্রদানকারী (জামিনদার) বিবরণ", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

        // Prompt / Note for Father / Mother Guarantor Selection
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = if (isDarkG) Color(0xFF064E3B).copy(alpha = 0.35f) else Color(0xFFF0FDF4),
            border = BorderStroke(1.dp, if (isDarkG) Color(0xFF0F3B7E) else Color(0xFFBFDBFE)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "নোট: পিতা জামিনদার হলে টিক দিন এবং মাতা জামিনদার হলে টিক দিন।",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.5.sp,
                        color = if (isDarkG) Color(0xFF86EFAC) else Color(0xFF166534)
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Checkbox 1: পিতা জামিনদার হলে টিক দিন
                    val isFatherGuarantor = state.guarantorRelationship == "পিতা"
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isFatherGuarantor) (if (isDarkG) Color(0xFF047857).copy(alpha = 0.5f) else Color(0xFFDCFCE7)) else MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, if (isFatherGuarantor) Color(0xFF16A34A) else Color(0xFFCBD5E1)),
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                val autoF = state.fatherName.ifBlank { stampState.employeeFatherName }
                                val fatherRow = state.familyMembers.firstOrNull { it.relationship == "পিতা" || it.relationship.contains("বাবা") || (state.fatherName.isNotBlank() && it.name == state.fatherName) }
                                val fMob = fatherRow?.mobileNumber.orEmpty().ifBlank { state.mobileNumber }
                                val fAge = fatherRow?.age.orEmpty()
                                val fOcc = fatherRow?.occupation.orEmpty().ifBlank { "কৃষি" }
                                val fEdu = fatherRow?.education.orEmpty()
                                val fNid = state.fatherNid
                                if (isFatherGuarantor) {
                                    onStateChange(state.copy(
                                        guarantorName = "",
                                        guarantorFatherName = "",
                                        guarantorMotherName = "",
                                        guarantorNid = "",
                                        guarantorRelationship = "",
                                        guarantorOccupation = "",
                                        guarantorEducation = "",
                                        guarantorMobile = "",
                                        guarantorAge = "",
                                        guarantorVillage = "",
                                        guarantorPostOffice = "",
                                        guarantorUpazila = "",
                                        guarantorDistrict = "",
                                        guarantorPostCode = "",
                                        guarantorSameAddress = false
                                    ))
                                } else {
                                    onStateChange(state.copy(
                                        guarantorName = autoF,
                                        guarantorRelationship = "পিতা",
                                        guarantorNid = fNid,
                                        guarantorMobile = fMob,
                                        guarantorAge = fAge,
                                        guarantorOccupation = fOcc,
                                        guarantorEducation = fEdu,
                                        guarantorSameAddress = true,
                                        guarantorVillage = state.permVillage,
                                        guarantorPostOffice = state.permPostOffice,
                                        guarantorPostCode = state.permPostCode,
                                        guarantorUpazila = state.permUpazila,
                                        guarantorThana = state.permThana,
                                        guarantorDistrict = state.permDistrict
                                    ))
                                }
                            }
                    ) {
                        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = isFatherGuarantor,
                                onCheckedChange = { checked ->
                                    val autoF = state.fatherName.ifBlank { stampState.employeeFatherName }
                                    val fatherRow = state.familyMembers.firstOrNull { it.relationship == "পিতা" || it.relationship.contains("বাবা") || (state.fatherName.isNotBlank() && it.name == state.fatherName) }
                                    val fMob = fatherRow?.mobileNumber.orEmpty().ifBlank { state.mobileNumber }
                                    val fAge = fatherRow?.age.orEmpty()
                                    val fOcc = fatherRow?.occupation.orEmpty().ifBlank { "কৃষি" }
                                    val fEdu = fatherRow?.education.orEmpty()
                                    val fNid = state.fatherNid
                                    if (checked) {
                                        onStateChange(state.copy(
                                            guarantorName = autoF,
                                            guarantorRelationship = "পিতা",
                                            guarantorNid = fNid,
                                            guarantorMobile = fMob,
                                            guarantorAge = fAge,
                                            guarantorOccupation = fOcc,
                                            guarantorEducation = fEdu,
                                            guarantorSameAddress = true,
                                            guarantorVillage = state.permVillage,
                                            guarantorPostOffice = state.permPostOffice,
                                            guarantorPostCode = state.permPostCode,
                                            guarantorUpazila = state.permUpazila,
                                            guarantorThana = state.permThana,
                                            guarantorDistrict = state.permDistrict
                                        ))
                                    } else {
                                        onStateChange(state.copy(
                                            guarantorName = "",
                                            guarantorFatherName = "",
                                            guarantorMotherName = "",
                                            guarantorNid = "",
                                            guarantorRelationship = "",
                                            guarantorOccupation = "",
                                            guarantorEducation = "",
                                            guarantorMobile = "",
                                            guarantorAge = "",
                                            guarantorVillage = "",
                                            guarantorPostOffice = "",
                                            guarantorUpazila = "",
                                            guarantorDistrict = "",
                                            guarantorPostCode = "",
                                            guarantorSameAddress = false
                                        ))
                                    }
                                },
                                colors = CheckboxDefaults.colors(checkedColor = Color(0xFF16A34A))
                            )
                            Spacer(Modifier.width(4.dp))
                            Text("পিতা জামিনদার হলে টিক দিন", fontSize = 12.sp, fontWeight = if (isFatherGuarantor) FontWeight.Bold else FontWeight.Medium)
                        }
                    }

                    // Checkbox 2: মাতা জামিনদার হলে টিক দিন
                    val isMotherGuarantor = state.guarantorRelationship == "মাতা"
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isMotherGuarantor) (if (isDarkG) Color(0xFF047857).copy(alpha = 0.5f) else Color(0xFFDCFCE7)) else MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, if (isMotherGuarantor) Color(0xFF16A34A) else Color(0xFFCBD5E1)),
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                val autoM = if (BanglaTextValidator.isValidBanglaName(state.motherName)) state.motherName else if (BanglaTextValidator.isValidBanglaName(allAgreementForms.personalInfoForm.motherName)) allAgreementForms.personalInfoForm.motherName else ""
                                val motherRow = state.familyMembers.firstOrNull { it.relationship == "মাতা" || it.relationship.contains("মা") || (state.motherName.isNotBlank() && it.name == state.motherName) }
                                val mMob = motherRow?.mobileNumber.orEmpty().ifBlank { state.mobileNumber }
                                val mAge = motherRow?.age.orEmpty()
                                val mOcc = motherRow?.occupation.orEmpty().ifBlank { "গৃহিণী" }
                                val mEdu = motherRow?.education.orEmpty()
                                val mNid = state.motherNid
                                if (isMotherGuarantor) {
                                    onStateChange(state.copy(
                                        guarantorName = "",
                                        guarantorFatherName = "",
                                        guarantorMotherName = "",
                                        guarantorNid = "",
                                        guarantorRelationship = "",
                                        guarantorOccupation = "",
                                        guarantorEducation = "",
                                        guarantorMobile = "",
                                        guarantorAge = "",
                                        guarantorVillage = "",
                                        guarantorPostOffice = "",
                                        guarantorUpazila = "",
                                        guarantorDistrict = "",
                                        guarantorPostCode = "",
                                        guarantorSameAddress = false
                                    ))
                                } else {
                                    onStateChange(state.copy(
                                        guarantorName = autoM,
                                        guarantorRelationship = "মাতা",
                                        guarantorNid = mNid,
                                        guarantorMobile = mMob,
                                        guarantorAge = mAge,
                                        guarantorOccupation = mOcc,
                                        guarantorEducation = mEdu,
                                        guarantorSameAddress = true,
                                        guarantorVillage = state.permVillage,
                                        guarantorPostOffice = state.permPostOffice,
                                        guarantorPostCode = state.permPostCode,
                                        guarantorUpazila = state.permUpazila,
                                        guarantorThana = state.permThana,
                                        guarantorDistrict = state.permDistrict
                                    ))
                                }
                            }
                    ) {
                        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = isMotherGuarantor,
                                onCheckedChange = { checked ->
                                    val autoM = if (BanglaTextValidator.isValidBanglaName(state.motherName)) state.motherName else if (BanglaTextValidator.isValidBanglaName(allAgreementForms.personalInfoForm.motherName)) allAgreementForms.personalInfoForm.motherName else ""
                                    val motherRow = state.familyMembers.firstOrNull { it.relationship == "মাতা" || it.relationship.contains("মা") || (state.motherName.isNotBlank() && it.name == state.motherName) }
                                    val mMob = motherRow?.mobileNumber.orEmpty().ifBlank { state.mobileNumber }
                                    val mAge = motherRow?.age.orEmpty()
                                    val mOcc = motherRow?.occupation.orEmpty().ifBlank { "গৃহিণী" }
                                    val mEdu = motherRow?.education.orEmpty()
                                    val mNid = state.motherNid
                                    if (checked) {
                                        onStateChange(state.copy(
                                            guarantorName = autoM,
                                            guarantorRelationship = "মাতা",
                                            guarantorNid = mNid,
                                            guarantorMobile = mMob,
                                            guarantorAge = mAge,
                                            guarantorOccupation = mOcc,
                                            guarantorEducation = mEdu,
                                            guarantorSameAddress = true,
                                            guarantorVillage = state.permVillage,
                                            guarantorPostOffice = state.permPostOffice,
                                            guarantorPostCode = state.permPostCode,
                                            guarantorUpazila = state.permUpazila,
                                            guarantorThana = state.permThana,
                                            guarantorDistrict = state.permDistrict
                                        ))
                                    } else {
                                        onStateChange(state.copy(
                                            guarantorName = "",
                                            guarantorFatherName = "",
                                            guarantorMotherName = "",
                                            guarantorNid = "",
                                            guarantorRelationship = "",
                                            guarantorOccupation = "",
                                            guarantorEducation = "",
                                            guarantorMobile = "",
                                            guarantorAge = "",
                                            guarantorVillage = "",
                                            guarantorPostOffice = "",
                                            guarantorUpazila = "",
                                            guarantorDistrict = "",
                                            guarantorPostCode = "",
                                            guarantorSameAddress = false
                                        ))
                                    }
                                },
                                colors = CheckboxDefaults.colors(checkedColor = Color(0xFF16A34A))
                            )
                            Spacer(Modifier.width(4.dp))
                            Text("মাতা জামিনদার হলে টিক দিন", fontSize = 12.sp, fontWeight = if (isMotherGuarantor) FontWeight.Bold else FontWeight.Medium)
                        }
                    }
                }
            }
        }

        AppFormTextField(
            value = state.guarantorName,
            onValueChange = { onStateChange(state.copy(guarantorName = BanglaTextValidator.filterBanglaText(it))) },
            label = "অঙ্গীকারনামা প্রদানকারীর পূর্ণ নাম *",
            placeholder = "পূর্ণ নাম বাংলায় লিখুন",
            isError = attemptedSave && state.guarantorName.isBlank(),
            modifier = Modifier.fillMaxWidth()
        )

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            AppFormTextField(
                value = state.guarantorFatherName,
                onValueChange = { onStateChange(state.copy(guarantorFatherName = BanglaTextValidator.filterBanglaText(it))) },
                label = "পিতার নাম *",
                placeholder = "পিতার নাম বাংলায় লিখুন",
                isError = attemptedSave && state.guarantorFatherName.isBlank(),
                modifier = Modifier.weight(1f)
            )
            AppFormTextField(
                value = state.guarantorMotherName,
                onValueChange = { onStateChange(state.copy(guarantorMotherName = BanglaTextValidator.filterBanglaText(it))) },
                label = "মাতার নাম *",
                placeholder = "মাতার নাম বাংলায় লিখুন",
                isError = attemptedSave && state.guarantorMotherName.isBlank(),
                modifier = Modifier.weight(1f)
            )
        }

        val gOccOptions = listOf("সরকারি চাকরি", "বেসরকারি চাকরি", "ব্যবসায়", "কৃষি", "গৃহিণী", "দিনমজুর", "অন্যান্য")
        val isCustomGOcc = state.guarantorOccupation.isNotBlank() && !gOccOptions.contains(state.guarantorOccupation)

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            AppFormTextField(
                value = state.guarantorNid,
                onValueChange = { onStateChange(state.copy(guarantorNid = BanglaTextValidator.toBanglaDigits(BanglaTextValidator.filterDigitsOnly(it)))) },
                label = "জাতীয় পরিচয় পত্র নং",
                placeholder = "১০/১৩/১৭ ডিজিট (ঐচ্ছিক)",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                testTag = "input_pers_guarantor_nid"
            )
            AppFormDropdown(
                value = if (isCustomGOcc) "অন্যান্য: ${state.guarantorOccupation}" else state.guarantorOccupation,
                onValueChange = { opt ->
                    onStateChange(state.copy(guarantorOccupation = if (opt == "অন্যান্য") "" else opt))
                },
                label = "পেশা *",
                options = gOccOptions,
                placeholder = "পেশা নির্বাচন",
                isError = attemptedSave && state.guarantorOccupation.isBlank(),
                modifier = Modifier.weight(1f)
            )
        }

        if (state.guarantorOccupation == "অন্যান্য" || isCustomGOcc) {
            AppFormTextField(
                value = if (state.guarantorOccupation == "অন্যান্য") "" else state.guarantorOccupation,
                onValueChange = { onStateChange(state.copy(guarantorOccupation = BanglaTextValidator.filterBanglaText(it))) },
                label = "অন্যান্য পেশা লিখুন (বাংলায়) *",
                placeholder = "যেমন: শিক্ষক / ড্রাইভার",
                isError = attemptedSave && state.guarantorOccupation.isBlank(),
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Guarantor Mobile (strictly max 11 digits, exact 11 validation) & Age (strictly max 3 digits)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            val isMobInvalid = state.guarantorMobile.isNotBlank() && state.guarantorMobile.length != 11
            AppFormTextField(
                value = state.guarantorMobile,
                onValueChange = { onStateChange(state.copy(guarantorMobile = BanglaTextValidator.filterMobileInput(it))) },
                label = "জামিনদারের মোবাইল নং (১১ ডিজিট)",
                placeholder = "০১XXXXXXXXX (১১ ডিজিট)",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = isMobInvalid,
                errorMessage = if (isMobInvalid) "১১ ডিজিটের মোবাইল নম্বর আবশ্যক" else "",
                modifier = Modifier.weight(1.2f)
            )
            AppFormTextField(
                value = state.guarantorAge,
                onValueChange = { onStateChange(state.copy(guarantorAge = BanglaTextValidator.filterAgeInput(it))) },
                label = "বয়স (সর্বোচ্চ ৩ সংখ্যা)",
                placeholder = "যেমন: ৫০",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(0.8f)
            )
        }

        // Guarantor Educational Qualification (শিক্ষাগত যোগ্যতা)
        AppFormTextField(
            value = state.guarantorEducation,
            onValueChange = { onStateChange(state.copy(guarantorEducation = BanglaTextValidator.filterBanglaText(it))) },
            label = "শিক্ষাগত যোগ্যতা",
            placeholder = "যেমন: এসএসসি / এইচএসসি / স্নাতক / অষ্টম শ্রেণি",
            modifier = Modifier.fillMaxWidth()
        )

        // Guarantor Relationship Dropdown
        val gRelOptions = listOf("পিতা", "মাতা", "স্ত্রী", "স্বামী", "ভাই", "বোন", "চাচা", "মামা", "ফুফা", "অন্যান্য")
        val isCustomGRel = state.guarantorRelationship.isNotBlank() && !gRelOptions.contains(state.guarantorRelationship)

        AppFormDropdown(
            value = if (isCustomGRel) "অন্যান্য" else state.guarantorRelationship,
            onValueChange = { opt ->
                if (opt == "পিতা") {
                    val autoF = state.fatherName.ifBlank { stampState.employeeFatherName }
                    val fatherRow = state.familyMembers.firstOrNull { it.relationship == "পিতা" || it.relationship.contains("বাবা") || (state.fatherName.isNotBlank() && it.name == state.fatherName) }
                    val fMob = fatherRow?.mobileNumber.orEmpty().ifBlank { state.mobileNumber }
                    val fAge = fatherRow?.age.orEmpty()
                    val fOcc = fatherRow?.occupation.orEmpty().ifBlank { "কৃষি" }
                    val fEdu = fatherRow?.education.orEmpty()
                    val fNid = state.fatherNid
                    onStateChange(state.copy(
                        guarantorName = autoF,
                        guarantorRelationship = "পিতা",
                        guarantorNid = fNid,
                        guarantorMobile = fMob,
                        guarantorAge = fAge,
                        guarantorOccupation = fOcc,
                        guarantorEducation = fEdu,
                        guarantorSameAddress = true,
                        guarantorVillage = state.permVillage,
                        guarantorPostOffice = state.permPostOffice,
                        guarantorPostCode = state.permPostCode,
                        guarantorUpazila = state.permUpazila,
                        guarantorThana = state.permThana,
                        guarantorDistrict = state.permDistrict
                    ))
                } else if (opt == "মাতা") {
                    val autoM = if (BanglaTextValidator.isValidBanglaName(state.motherName)) state.motherName else if (BanglaTextValidator.isValidBanglaName(allAgreementForms.personalInfoForm.motherName)) allAgreementForms.personalInfoForm.motherName else ""
                    val motherRow = state.familyMembers.firstOrNull { it.relationship == "মাতা" || it.relationship.contains("মা") || (state.motherName.isNotBlank() && it.name == state.motherName) }
                    val mMob = motherRow?.mobileNumber.orEmpty().ifBlank { state.mobileNumber }
                    val mAge = motherRow?.age.orEmpty()
                    val mOcc = motherRow?.occupation.orEmpty().ifBlank { "গৃহিণী" }
                    val mEdu = motherRow?.education.orEmpty()
                    val mNid = state.motherNid
                    onStateChange(state.copy(
                        guarantorName = autoM,
                        guarantorRelationship = "মাতা",
                        guarantorNid = mNid,
                        guarantorMobile = mMob,
                        guarantorAge = mAge,
                        guarantorOccupation = mOcc,
                        guarantorEducation = mEdu,
                        guarantorSameAddress = true,
                        guarantorVillage = state.permVillage,
                        guarantorPostOffice = state.permPostOffice,
                        guarantorPostCode = state.permPostCode,
                        guarantorUpazila = state.permUpazila,
                        guarantorThana = state.permThana,
                        guarantorDistrict = state.permDistrict
                    ))
                } else {
                    onStateChange(state.copy(guarantorRelationship = if (opt == "অন্যান্য") "" else opt))
                }
            },
            label = "কর্মীর সাথে সম্পর্ক *",
            options = gRelOptions,
            placeholder = "সম্পর্ক নির্বাচন করুন",
            isError = attemptedSave && state.guarantorRelationship.isBlank(),
            modifier = Modifier.fillMaxWidth()
        )

        if (state.guarantorRelationship.isBlank() || isCustomGRel || state.guarantorRelationship == "অন্যান্য") {
            AppFormTextField(
                value = if (state.guarantorRelationship == "অন্যান্য") "" else state.guarantorRelationship,
                onValueChange = { onStateChange(state.copy(guarantorRelationship = BanglaTextValidator.filterBanglaText(it))) },
                label = "অন্যান্য সম্পর্ক লিখুন (বাংলায়) *",
                placeholder = "যেমন: মামাতো ভাই / প্রতিবেশী",
                isError = attemptedSave && state.guarantorRelationship.isBlank(),
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Guarantor Address Section & Same Address Checkbox with Note
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = if (state.guarantorSameAddress) (if (isDarkG) Color(0xFF064E3B).copy(alpha = 0.35f) else Color(0xFFF0FDF4)) else (if (isDarkG) MaterialTheme.colorScheme.surfaceVariant else Color(0xFFF8FAFC)),
            border = BorderStroke(1.dp, if (state.guarantorSameAddress) Color(0xFF16A34A) else Color(0xFFCBD5E1)),
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    val newSame = !state.guarantorSameAddress
                    if (newSame) {
                        val wVill = state.permVillage.ifBlank { state.currVillageOrHouse }
                        val wPo = state.permPostOffice.ifBlank { state.currPostOffice }
                        val wPc = state.permPostCode.ifBlank { state.currPostCode }
                        val wUp = state.permUpazila.ifBlank { state.currUpazila }
                        val wDist = state.permDistrict.ifBlank { state.currDistrict }
                        onStateChange(
                            state.copy(
                                guarantorSameAddress = true,
                                guarantorVillage = wVill,
                                guarantorPostOffice = wPo,
                                guarantorPostCode = wPc,
                                guarantorUpazila = wUp,
                                guarantorThana = wUp,
                                guarantorDistrict = wDist
                            )
                        )
                    } else {
                        onStateChange(
                            state.copy(
                                guarantorSameAddress = false,
                                guarantorVillage = "",
                                guarantorPostOffice = "",
                                guarantorPostCode = "",
                                guarantorUpazila = "",
                                guarantorThana = "",
                                guarantorDistrict = ""
                            )
                        )
                    }
                }
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = state.guarantorSameAddress,
                    onCheckedChange = { checked ->
                        if (checked) {
                            val wVill = state.permVillage.ifBlank { state.currVillageOrHouse }
                            val wPo = state.permPostOffice.ifBlank { state.currPostOffice }
                            val wPc = state.permPostCode.ifBlank { state.currPostCode }
                            val wUp = state.permUpazila.ifBlank { state.currUpazila }
                            val wDist = state.permDistrict.ifBlank { state.currDistrict }
                            onStateChange(
                                state.copy(
                                    guarantorSameAddress = true,
                                    guarantorVillage = wVill,
                                    guarantorPostOffice = wPo,
                                    guarantorPostCode = wPc,
                                    guarantorUpazila = wUp,
                                    guarantorThana = wUp,
                                    guarantorDistrict = wDist
                                )
                            )
                        } else {
                            onStateChange(
                                state.copy(
                                    guarantorSameAddress = false,
                                    guarantorVillage = "",
                                    guarantorPostOffice = "",
                                    guarantorPostCode = "",
                                    guarantorUpazila = "",
                                    guarantorThana = "",
                                    guarantorDistrict = ""
                                )
                            )
                        }
                    },
                    colors = CheckboxDefaults.colors(checkedColor = Color(0xFF16A34A))
                )
                Spacer(Modifier.width(8.dp))
                Column {
                    Text(
                        "নোট: জামিনদারের ঠিকানা ও আপনার ঠিকানা একই হলে টিক দিন।",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = if (state.guarantorSameAddress) (if (isDarkG) Color(0xFF86EFAC) else Color(0xFF166534)) else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        "টিক দিলে কর্মীর স্থায়ী ঠিকানা স্বয়ংক্রিয়ভাবে জামিনদারের ঠিকানায় বসে যাবে",
                        fontSize = 11.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Text("জামিনদারের ঠিকানা :", fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            AutoFilledBlockedTextField(
                value = state.guarantorVillage,
                onValueChange = { onStateChange(state.copy(guarantorVillage = BanglaTextValidator.filterBanglaText(it))) },
                label = "গ্রাম/বাড়ি *",
                isAutoFilled = isGuarantorVillAutoFilled,
                isError = attemptedSave && state.guarantorVillage.isBlank(),
                modifier = Modifier.weight(1f)
            )
            AutoFilledBlockedTextField(
                value = state.guarantorPostOffice,
                onValueChange = { po ->
                    val bPo = BanglaTextValidator.filterBanglaText(po)
                    val foundPc = BangladeshDistricts.lookupPostCode(state.guarantorDistrict, state.guarantorUpazila, bPo)
                    val autoPc = if (foundPc.isNotBlank()) foundPc else state.guarantorPostCode
                    onStateChange(state.copy(guarantorPostOffice = bPo, guarantorPostCode = autoPc))
                },
                label = "ডাকঘর *",
                isAutoFilled = isGuarantorPostAutoFilled,
                isError = attemptedSave && state.guarantorPostOffice.isBlank(),
                modifier = Modifier.weight(1f)
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            AppFormTextField(
                value = state.guarantorPostCode,
                onValueChange = { pc ->
                    val bDigits = BanglaTextValidator.toBanglaDigits(pc.filter { it.isDigit() || it in '০'..'৯' })
                    onStateChange(state.copy(guarantorPostCode = bDigits))
                },
                label = "পোস্ট কোড",
                placeholder = "যেমন: ৭৪০০ / ৯০০০",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )
            AutoFilledBlockedTextField(
                value = state.guarantorUpazila,
                onValueChange = { up ->
                    val bUp = BanglaTextValidator.filterBanglaText(up)
                    val foundPc = BangladeshDistricts.lookupPostCode(state.guarantorDistrict, bUp, state.guarantorPostOffice)
                    val autoPc = if (foundPc.isNotBlank()) foundPc else state.guarantorPostCode
                    onStateChange(state.copy(guarantorUpazila = bUp, guarantorThana = bUp, guarantorPostCode = autoPc))
                },
                label = "উপজেলা *",
                isAutoFilled = isGuarantorUpazilaAutoFilled,
                isError = attemptedSave && state.guarantorUpazila.isBlank() && state.guarantorThana.isBlank(),
                modifier = Modifier.weight(1f)
            )
            AutoFilledBlockedTextField(
                value = state.guarantorDistrict,
                onValueChange = { dist ->
                    val bDist = BanglaTextValidator.filterBanglaText(dist)
                    val foundPc = BangladeshDistricts.lookupPostCode(bDist, state.guarantorUpazila, state.guarantorPostOffice)
                    val autoPc = if (foundPc.isNotBlank()) foundPc else state.guarantorPostCode
                    onStateChange(state.copy(guarantorDistrict = bDist, guarantorPostCode = autoPc))
                },
                label = "জেলা *",
                isAutoFilled = isGuarantorDistAutoFilled,
                isError = attemptedSave && state.guarantorDistrict.isBlank(),
                modifier = Modifier.weight(1f)
            )
        }

        // Guarantor Monthly & Yearly Income (মাসিক আয় উপরে, বাৎসরিক আয় নিচে)
        Text("জামিনদারের মাসিক ও বাৎসরিক আয়", fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            AppFormTextField(
                value = state.guardianMonthlyIncome,
                onValueChange = { input ->
                    val gMDigits = BanglaTextValidator.filterBanglaDigitsOnly(input)
                    if (gMDigits.isBlank()) {
                        onStateChange(state.copy(
                            guardianMonthlyIncome = "",
                            guardianMonthlyIncomeWords = "",
                            guardianYearlyIncome = "",
                            guardianYearlyIncomeWords = ""
                        ))
                    } else {
                        val gMWords = BanglaTextValidator.convertNumberToBanglaWords(gMDigits)
                        val engVal = BanglaTextValidator.toEnglishDigits(gMDigits).toLongOrNull() ?: 0L
                        val gYVal = if (engVal > 0) (engVal * 12).toString() else ""
                        val gYDigits = BanglaTextValidator.toBanglaDigits(gYVal)
                        val gYWords = BanglaTextValidator.convertNumberToBanglaWords(gYDigits)
                        onStateChange(state.copy(
                            guardianMonthlyIncome = gMDigits,
                            guardianMonthlyIncomeWords = gMWords,
                            guardianYearlyIncome = gYDigits,
                            guardianYearlyIncomeWords = gYWords
                        ))
                    }
                },
                label = "জামিনদারের মাসিক আয় (সংখ্যা) *",
                placeholder = "৩০০০০",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )
            AppFormTextField(
                value = state.guardianMonthlyIncomeWords,
                onValueChange = { onStateChange(state.copy(guardianMonthlyIncomeWords = BanglaTextValidator.filterBanglaText(it))) },
                label = "মাসিক আয় (কথায়)",
                modifier = Modifier.weight(1.3f)
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            AppFormTextField(
                value = state.guardianYearlyIncome,
                onValueChange = { input ->
                    val gYDigits = BanglaTextValidator.filterBanglaDigitsOnly(input)
                    if (gYDigits.isBlank()) {
                        onStateChange(state.copy(
                            guardianYearlyIncome = "",
                            guardianYearlyIncomeWords = ""
                        ))
                    } else {
                        val gYWords = BanglaTextValidator.convertNumberToBanglaWords(gYDigits)
                        onStateChange(state.copy(
                            guardianYearlyIncome = gYDigits,
                            guardianYearlyIncomeWords = gYWords
                        ))
                    }
                },
                label = "জামিনদারের বাৎসরিক আয় (সংখ্যা)",
                placeholder = "৩৬০০০০",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )
            AppFormTextField(
                value = state.guardianYearlyIncomeWords,
                onValueChange = { onStateChange(state.copy(guardianYearlyIncomeWords = BanglaTextValidator.filterBanglaText(it))) },
                label = "বাৎসরিক আয় (কথায়)",
                modifier = Modifier.weight(1.3f)
            )
        }
    }

    Spacer(Modifier.height(14.dp))

    // Direct option card to jump to Verification Form
    val isDarkJump = isSystemInDarkTheme()
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDarkJump) Color(0xFF3B0764).copy(alpha = 0.35f) else Color(0xFFF5F3FF)
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.2.dp,
            if (isDarkJump) Color(0xFF7C3AED).copy(alpha = 0.6f) else Color(0xFFC4B5FD)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onNavigateToVerification() }
            .testTag("card_to_verification_form")
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = androidx.compose.foundation.shape.CircleShape,
                color = if (isDarkJump) Color(0xFF7C3AED).copy(alpha = 0.3f) else Color(0xFF7C3AED).copy(alpha = 0.15f),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.VerifiedUser,
                        contentDescription = null,
                        tint = if (isDarkJump) Color(0xFFA78BFA) else Color(0xFF7C3AED),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "পরবর্তী ধাপ: তথ্য যাচাই ফরম (২ পাতা)",
                    fontWeight = FontWeight.Bold,
                    color = if (isDarkJump) Color(0xFFDDD6FE) else Color(0xFF5B21B6),
                    fontSize = 14.sp
                )
                Text(
                    text = "কর্মীর তথ্যানুসন্ধানের পরবর্তী অংশ তথ্য যাচাই ফরম পূরণ করতে এখানে চাপুন",
                    fontSize = 11.5.sp,
                    color = if (isDarkJump) Color(0xFFC4B5FD) else Color(0xFF6B21A8)
                )
            }
            Icon(
                Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = if (isDarkJump) Color(0xFFA78BFA) else Color(0xFF7C3AED)
            )
        }
    }
}
