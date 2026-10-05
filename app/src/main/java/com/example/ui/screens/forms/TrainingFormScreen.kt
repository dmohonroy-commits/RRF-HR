package com.example.ui.screens.forms

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import com.example.data.TrainingFormState
import com.example.ui.AppScreen
import com.example.ui.MainViewModel
import com.example.ui.components.*
import com.example.util.AgreementFormsPdfGenerator
import com.example.util.BanglaTextValidator
import com.example.util.BangladeshDistricts
import com.example.util.ShareHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrainingFormScreen(
    viewModel: MainViewModel,
    formState: TrainingFormState,
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

    val isFatherAutoFilled = state.fatherName.isNotBlank() && (
        stampState.employeeFatherName.isNotBlank() ||
        allAgreementForms.idCardForm.fatherName.isNotBlank() ||
        allAgreementForms.personalInfoForm.fatherName.isNotBlank()
    )
    LaunchedEffect(formState) {
        state = formState
        val todayBanglaDate = BanglaTextValidator.toBanglaDigits(java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.US).format(java.util.Date()))
        if (state.declarationDate.isBlank()) {
            val updated = state.copy(declarationDate = todayBanglaDate)
            state = updated
            viewModel.updateTrainingForm(updated)
        }
    }

    val updateField: (TrainingFormState) -> Unit = { newState ->
        state = newState
        viewModel.updateTrainingForm(newState)
    }

    val hasEmptyMandatory = state.employeeName.isBlank() ||
            state.fatherName.isBlank() ||
            state.village.isBlank() ||
            state.postOffice.isBlank() ||
            state.thana.isBlank() ||
            state.district.isBlank() ||
            state.designation.isBlank()

    fun saveAndGeneratePdf() {
        keyboardController?.hide()
        focusManager.clearFocus()
        attemptedSave = true
        if (hasEmptyMandatory) {
            showValidationErrorDialog = true
            return
        }
        viewModel.updateTrainingForm(state)
        AgreementFormsPdfGenerator.generateTrainingFormPdf(context, state)
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
                            text = "প্রশিক্ষণে অঙ্গীকারনামা",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("btn_back_training")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "ফিরে যান", tint = MaterialTheme.colorScheme.primary)
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
                modifier = Modifier.testTag("fab_save_training")
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
                currentScreen = AppScreen.FORM_TRAINING,
                viewModel = viewModel,
                onNavigate = { viewModel.updateScreen(it) }
            )

            // Training info guidance card
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
                        Icons.Default.EditNote,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = "প্রশিক্ষণে অঙ্গীকারনামার বিবরণ বাংলায় পূরণ করুন। সকল তথ্য সরাসরি টাইপ ও সম্পাদনাযোগ্য।",
                        fontSize = 12.5.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 17.sp
                    )
                }
            }

            // Card 1: Employee Basic Info - 3D Stamp Form Theme Card
            StampThemed3DCard(
                badgeText = "১",
                title = "অংশগ্রহণকারী কর্মীর বিবরণ",
                subtitle = "প্রশিক্ষণে অঙ্গীকারনামা (আবশ্যকীয় তথ্য বাংলায় পূরণ করুন)",
                tag = "আবশ্যক"
            ) {

                    AppFormTextField(
                        value = state.employeeName,
                        onValueChange = { updateField(state.copy(employeeName = BanglaTextValidator.filterBengaliOnly(it))) },
                        label = "কর্মীর নাম (আমি) *",
                        placeholder = "বাংলায় নাম লিখুন",
                        isError = attemptedSave && state.employeeName.isBlank(),
                        testTag = "input_tr_emp_name",
                        modifier = Modifier.fillMaxWidth()
                    )

                    AppFormTextField(
                        value = state.fatherName,
                        onValueChange = { updateField(state.copy(fatherName = BanglaTextValidator.filterBengaliOnly(it))) },
                        label = "পিতার নাম *",
                        placeholder = "বাংলায় পিতার নাম লিখুন",
                        isError = attemptedSave && state.fatherName.isBlank(),
                        testTag = "input_tr_father_name",
                        modifier = Modifier.fillMaxWidth()
                    )

                    AppFormTextField(
                        value = state.motherName,
                        onValueChange = { updateField(state.copy(motherName = BanglaTextValidator.filterBengaliOnly(it))) },
                        label = "মাতার নাম",
                        placeholder = "বাংলায় মাতার নাম লিখুন",
                        testTag = "input_tr_mother_name",
                        modifier = Modifier.fillMaxWidth()
                    )

                    AppFormTextField(
                        value = state.village,
                        onValueChange = { updateField(state.copy(village = BanglaTextValidator.filterBengaliOnly(it))) },
                        label = "গ্রাম *",
                        placeholder = "গ্রামের নাম",
                        isError = attemptedSave && state.village.isBlank(),
                        testTag = "input_tr_village",
                        modifier = Modifier.fillMaxWidth()
                    )

                    AppFormTextField(
                        value = state.postOffice,
                        onValueChange = { po ->
                            val bPo = BanglaTextValidator.filterBengaliOnly(po)
                            updateField(state.copy(postOffice = bPo))
                        },
                        label = "ডাকঘর *",
                        placeholder = "ডাকঘরের নাম",
                        isError = attemptedSave && state.postOffice.isBlank(),
                        testTag = "input_tr_post",
                        modifier = Modifier.fillMaxWidth()
                    )

                    AppFormTextField(
                        value = state.postCode,
                        onValueChange = { pc ->
                            val bDigits = BanglaTextValidator.toBanglaDigits(BanglaTextValidator.filterDigitsOnly(pc))
                            updateField(state.copy(postCode = bDigits))
                        },
                        label = "পোস্ট কোড",
                        placeholder = "যেমন: ৭৪০০ / ৯০০০",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        testTag = "input_tr_post_code",
                        modifier = Modifier.fillMaxWidth()
                    )

                    AppFormTextField(
                        value = state.thana,
                        onValueChange = { th ->
                            val bTh = BanglaTextValidator.filterBengaliOnly(th)
                            updateField(state.copy(thana = bTh))
                        },
                        label = "থানা *",
                        placeholder = "থানার নাম",
                        isError = attemptedSave && state.thana.isBlank(),
                        testTag = "input_tr_thana",
                        modifier = Modifier.fillMaxWidth()
                    )

                    AppFormTextField(
                        value = state.district,
                        onValueChange = { dist ->
                            val bDist = BanglaTextValidator.filterBengaliOnly(dist)
                            updateField(state.copy(district = bDist))
                        },
                        label = "জেলা *",
                        placeholder = "জেলার নাম",
                        isError = attemptedSave && state.district.isBlank(),
                        testTag = "input_tr_district",
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Designation Dropdown
                    val designations = listOf("অফিসার (অ্যাকাউন্টস)", "অফিসার (ঋণ)", "সার্ভিস স্টাফ", "অন্যান্য")

                    AppFormDropdown(
                        value = state.designation,
                        onValueChange = { desig ->
                            updateField(state.copy(designation = desig))
                        },
                        label = "পদবী *",
                        options = designations,
                        placeholder = "পদবী নির্বাচন করুন",
                        isError = attemptedSave && state.designation.isBlank(),
                        testTag = "input_tr_designation_dropdown",
                        modifier = Modifier.fillMaxWidth()
                    )

                    // If "অন্যান্য" is selected, show text box
                    if (state.designation == "অন্যান্য") {
                        AppFormTextField(
                            value = state.designation,
                            onValueChange = { updateField(state.copy(designation = BanglaTextValidator.filterBengaliWithPunctuation(it))) },
                            label = "পদবীর নাম লিখুন *",
                            placeholder = "পদবীর নাম",
                            isError = attemptedSave && state.designation.isBlank(),
                            testTag = "input_tr_designation_custom",
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(Modifier.height(4.dp))
                    DateBoxesGridComponent(
                        value = state.declarationDate,
                        onValueChange = { updateField(state.copy(declarationDate = it)) },
                        label = "তারিখ (ছকে অটো পূরণকৃত) *",
                        isEnglish = false,
                        isError = attemptedSave && state.declarationDate.isBlank(),
                        errorMessage = if (attemptedSave && state.declarationDate.isBlank()) "তারিখ আবশ্যক" else "",
                        modifier = Modifier.fillMaxWidth()
                    )
            }

            Spacer(Modifier.height(6.dp))
            Text(
                text = "আবশ্যকীয় তথ্য ছক পূরণ করে সেভ করুন।",
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
            formTitle = "২. প্রশিক্ষণ তথ্য ও অঙ্গীকারনামা",
            nextScreenTitle = "৩. পরিচিত ব্যক্তির সাথে সম্পর্ক",
            onGoToNext = {
                showSavedDialog = false
                viewModel.navigateToNextForm(AppScreen.FORM_TRAINING)
            },
            onViewPdf = {
                showSavedDialog = false
                val pdf = AgreementFormsPdfGenerator.generateTrainingFormPdf(context, state)
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
                        "অংশগ্রহণকারী কর্মীর বিবরণ (নাম, পিতার নাম, গ্রাম, ডাকঘর, থানা, জেলা, পদবী) পূরণ করা আবশ্যক। কোন বাধ্যতামূলক তথ্য খালি রাখা যাবে না।",
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
