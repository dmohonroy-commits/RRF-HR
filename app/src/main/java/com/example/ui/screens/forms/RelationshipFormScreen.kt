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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.KinshipPerson
import com.example.data.RelationshipFormState
import com.example.ui.AppScreen
import com.example.ui.MainViewModel
import com.example.ui.components.AppFormDropdown
import com.example.ui.components.AppFormTextField
import com.example.ui.components.AutoFilledBlockedTextField
import com.example.ui.components.FormLinkHeader
import com.example.ui.components.FormSavedAdvanceDialog
import com.example.ui.components.StampThemed3DCard
import com.example.util.AgreementFormsPdfGenerator
import com.example.util.BanglaAddressHelper
import com.example.util.BanglaTextValidator
import com.example.util.ShareHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RelationshipFormScreen(
    viewModel: MainViewModel,
    formState: RelationshipFormState,
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
        val englishName = if (BanglaTextValidator.containsBengali(rawName)) {
            BanglaAddressHelper.transliterateBanglaToEnglish(rawName)
        } else {
            rawName
        }
        var updated = state
        if (updated.employeeName.isBlank() && englishName.isNotBlank()) {
            updated = updated.copy(employeeName = englishName)
        }
        if (updated.declarationDate.isBlank()) {
            val todayDate = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.US).format(java.util.Date())
            updated = updated.copy(declarationDate = todayDate)
        }
        if (updated != state) {
            state = updated
            viewModel.updateRelationshipForm(updated)
        }
    }

    val updateField: (RelationshipFormState) -> Unit = { newState ->
        state = newState
        viewModel.updateRelationshipForm(newState)
    }

    val isEmpNameAutoFilled = stampState.employeeName.isNotBlank() && state.employeeName == stampState.employeeName

    val hasEmptyMandatory = state.employeeName.isBlank() ||
            state.declarationDate.isBlank() ||
            (!state.hasNoKinship && (
                state.kinshipList.getOrNull(0)?.nameAndAddress.orEmpty().isBlank() ||
                state.kinshipList.getOrNull(0)?.designationInRrf.orEmpty().isBlank() ||
                state.kinshipList.getOrNull(0)?.relationship.orEmpty().isBlank()
            ))

    fun saveAndGeneratePdf() {
        keyboardController?.hide()
        focusManager.clearFocus()
        attemptedSave = true
        if (hasEmptyMandatory) {
            showValidationErrorDialog = true
            return
        }
        viewModel.updateRelationshipForm(state)
        AgreementFormsPdfGenerator.generateRelationshipFormPdf(context, state)
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
                            text = "প্রতিষ্ঠানের পরিচিত ব্যক্তির সম্পর্ক",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("btn_back_relationship")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "ফিরে যান", tint = MaterialTheme.colorScheme.primary)
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            viewModel.updateRelationshipForm(state)
                            val pdf = AgreementFormsPdfGenerator.generateRelationshipFormPdf(context, state)
                            ShareHelper.openFile(context, pdf)
                        },
                        modifier = Modifier.testTag("btn_preview_rel_top")
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
                modifier = Modifier.testTag("fab_save_rel")
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
                currentScreen = AppScreen.FORM_RELATIONSHIP,
                viewModel = viewModel,
                onNavigate = { viewModel.updateScreen(it) }
            )

            // Header Info Card - Stamp 3D Theme Card
            StampThemed3DCard(
                badgeText = "১",
                title = "ঘোষণাকারীর তথ্যাবলী",
                subtitle = "Employee Declaration Information (English Form)",
                tag = "আবশ্যক"
            ) {
                    AutoFilledBlockedTextField(
                        value = state.employeeName,
                        onValueChange = { updateField(state.copy(employeeName = BanglaTextValidator.filterEnglishText(it))) },
                        label = "Employee Name (English Only) *",
                        isAutoFilled = isEmpNameAutoFilled,
                        isError = attemptedSave && state.employeeName.isBlank(),
                        modifier = Modifier.testTag("input_rel_emp_name")
                    )

                    AppFormTextField(
                        value = state.declarationDate,
                        onValueChange = { updateField(state.copy(declarationDate = BanglaTextValidator.toEnglishDigits(BanglaTextValidator.filterDateInput(it)))) },
                        label = "Date (DD/MM/YYYY) *",
                        placeholder = "DD/MM/YYYY",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        isError = attemptedSave && state.declarationDate.isBlank(),
                        modifier = Modifier.fillMaxWidth().testTag("input_rel_date")
                    )
            }

            // Option: I have none Checkbox
            val isDarkRel = isSystemInDarkTheme()
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = if (state.hasNoKinship) (if (isDarkRel) Color(0xFF1E3A8A).copy(alpha = 0.35f) else Color(0xFFEFF6FF))
                else (if (isDarkRel) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(
                    1.5.dp,
                    if (state.hasNoKinship) MaterialTheme.colorScheme.primary else (if (isDarkRel) Color(0xFF475569) else Color(0xFFE2E8F0))
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { updateField(state.copy(hasNoKinship = !state.hasNoKinship)) }
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = state.hasNoKinship,
                        onCheckedChange = { updateField(state.copy(hasNoKinship = it)) }
                    )
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "I have none. (আরআরএফ-এ আমার কোনো আত্মীয় বা পরিচিত কর্মরত নেই)",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (state.hasNoKinship) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "যদি আরআরএফ-এ কোনো আত্মীয় না থাকে তাহলে এটি সিলেক্ট করুন। (English Only Form)",
                            fontSize = 11.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Kinship Persons Cards (1, 2)
            if (!state.hasNoKinship) {
                for (i in 0 until 2) {
                    val person = state.kinshipList.getOrElse(i) { KinshipPerson() }
                    val isMandatoryPerson = (i == 0)

                    StampThemed3DCard(
                        badgeText = "${i + 2}",
                        title = "Person / Relative #${i + 1}",
                        subtitle = if (isMandatoryPerson) "Required Information (English Only)" else "Optional Information",
                        tag = if (isMandatoryPerson) "আবশ্যক" else "ঐচ্ছিক"
                    ) {
                            AppFormTextField(
                                value = person.nameAndAddress,
                                onValueChange = { newVal ->
                                    val filtered = BanglaTextValidator.filterEnglishText(newVal)
                                    val updated = state.kinshipList.toMutableList()
                                    while (updated.size <= i) updated.add(KinshipPerson())
                                    updated[i] = updated[i].copy(nameAndAddress = filtered)
                                    updateField(state.copy(kinshipList = updated))
                                },
                                label = if (isMandatoryPerson) "Name and Address (English Only) *" else "Name and Address (English Only)",
                                placeholder = "Name and Address",
                                isError = attemptedSave && isMandatoryPerson && person.nameAndAddress.isBlank(),
                                singleLine = false,
                                modifier = Modifier.fillMaxWidth()
                            )

                            val designationOptions = listOf(
                                "Director",
                                "Deputy Director",
                                "Assistant Director",
                                "Regional Manager",
                                "Branch Manager",
                                "Officer (Accounts)",
                                "Officer (Loan)",
                                "Others"
                            )

                            val relationshipOptions = listOf(
                                "Father",
                                "Mother",
                                "Sister",
                                "Brother",
                                "Friend",
                                "Uncle",
                                "Others"
                            )

                            // Designation in RRF Dropdown
                            AppFormDropdown(
                                value = person.designationInRrf,
                                onValueChange = { opt ->
                                    val updated = state.kinshipList.toMutableList()
                                    while (updated.size <= i) updated.add(KinshipPerson())
                                    updated[i] = updated[i].copy(designationInRrf = opt)
                                    updateField(state.copy(kinshipList = updated))
                                },
                                label = if (isMandatoryPerson) "Designation in RRF (English Only) *" else "Designation in RRF (English Only)",
                                options = designationOptions,
                                placeholder = "Select Designation",
                                isError = attemptedSave && isMandatoryPerson && person.designationInRrf.isBlank(),
                                modifier = Modifier.fillMaxWidth()
                            )

                            if (person.designationInRrf == "Others") {
                                AppFormTextField(
                                    value = person.designationCustom,
                                    onValueChange = { newVal ->
                                        val filtered = BanglaTextValidator.filterEnglishText(newVal)
                                        val updated = state.kinshipList.toMutableList()
                                        while (updated.size <= i) updated.add(KinshipPerson())
                                        updated[i] = updated[i].copy(designationCustom = filtered)
                                        updateField(state.copy(kinshipList = updated))
                                    },
                                    label = "Specify Designation (English Only)",
                                    placeholder = "Designation name",
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            // Relationship Dropdown
                            AppFormDropdown(
                                value = person.relationship,
                                onValueChange = { opt ->
                                    val updated = state.kinshipList.toMutableList()
                                    while (updated.size <= i) updated.add(KinshipPerson())
                                    updated[i] = updated[i].copy(relationship = opt)
                                    updateField(state.copy(kinshipList = updated))
                                },
                                label = if (isMandatoryPerson) "Relationship (English Only) *" else "Relationship (English Only)",
                                options = relationshipOptions,
                                placeholder = "Select Relationship",
                                isError = attemptedSave && isMandatoryPerson && person.relationship.isBlank(),
                                modifier = Modifier.fillMaxWidth()
                            )

                            if (person.relationship == "Others") {
                                AppFormTextField(
                                    value = person.relationshipCustom,
                                    onValueChange = { newVal ->
                                        val filtered = BanglaTextValidator.filterEnglishText(newVal)
                                        val updated = state.kinshipList.toMutableList()
                                        while (updated.size <= i) updated.add(KinshipPerson())
                                        updated[i] = updated[i].copy(relationshipCustom = filtered)
                                        updateField(state.copy(kinshipList = updated))
                                    },
                                    label = "Specify Relationship (English Only)",
                                    placeholder = "Relationship name",
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                    }
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
            formTitle = "৩. পরিচিত ব্যক্তির সাথে সম্পর্ক",
            nextScreenTitle = "৪. নমিনি তথ্য ফরম",
            onGoToNext = {
                showSavedDialog = false
                viewModel.navigateToNextForm(AppScreen.FORM_RELATIONSHIP)
            },
            onViewPdf = {
                showSavedDialog = false
                val pdf = AgreementFormsPdfGenerator.generateRelationshipFormPdf(context, state)
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
                        "ঘোষণাকারীর তথ্যাবলী (কর্মীর নাম, তারিখ) এবং পরিচিত ব্যক্তির তথ্য (যদি 'I have none' সিলেক্ট করা না থাকে) পূরণ করা আবশ্যক।",
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
