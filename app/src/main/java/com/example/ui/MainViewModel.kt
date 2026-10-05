package com.example.ui

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.example.data.cloud.CloudFunctionResult
import com.example.data.cloud.FirebaseFunctionsManager
import com.example.util.BanglaTextValidator
import com.example.util.BanglaAddressHelper
import com.example.util.JsonDataBackupManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class AppScreen {
    HOME,
    WORKER_PANEL,
    ADMIN_PANEL,
    AGREEMENT_PREVIEW,
    ADMIN_EDIT_FORM,
    AGREEMENT_CONDITIONS,
    APP_GUIDELINES,
    AGREEMENTS_HUB,
    FORM_ID_CARD,
    FORM_TRAINING,
    FORM_RELATIONSHIP,
    FORM_NOMINEE,
    FORM_PERSONAL_INFO,
    FORM_VERIFICATION,
    CLOUD_FUNCTIONS
}

data class FormState(
    val employeeName: String = "",
    val employeeFatherName: String = "",
    val designation: String = "অফিসার (ঋণ)",
    val employeeDistrict: String = "",
    val employeeUpazila: String = "",
    val employeePostOffice: String = "",
    val employeeVillage: String = "",
    
    val guarantorName: String = "",
    val guarantorFatherName: String = "",
    val guarantorMotherName: String = "",
    val guarantorRelationship: String = "পিতা",
    val guarantorRelationshipCustom: String = "",
    val guarantorNid: String = "",
    
    val sameAddress: Boolean = false,
    val guarantorDistrict: String = "",
    val guarantorUpazila: String = "",
    val guarantorPostOffice: String = "",
    val guarantorVillage: String = "",

    val validationErrors: Set<String> = emptySet(),
    val showValidationErrorDialog: Boolean = false,
    val isEditingByAdmin: Boolean = false,
    val isEditingWorker: Boolean = false,
    val editingAgreementId: Long? = null,
    val editingSerialNo: String? = null,
    val currentEditCount: Int = 0,
    val orgLogoPath: String? = null
) {
    val isEditing: Boolean
        get() = editingAgreementId != null
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AgreementRepository

    init {
        val db = AppDatabase.getInstance(application)
        repository = AgreementRepository(db.agreementDao(), db.addressDao())
    }

    val allAgreements: StateFlow<List<AgreementEntity>> = repository.allAgreements
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val localWorkerAgreement: StateFlow<AgreementEntity?> = repository.localWorkerAgreement
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _currentScreen = MutableStateFlow(AppScreen.HOME)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val _isAdminLoggedIn = MutableStateFlow(false)
    val isAdminLoggedIn: StateFlow<Boolean> = _isAdminLoggedIn.asStateFlow()

    private val _showAdminLoginDialog = MutableStateFlow(false)
    val showAdminLoginDialog: StateFlow<Boolean> = _showAdminLoginDialog.asStateFlow()

    private val _adminPasswordInput = MutableStateFlow("")
    val adminPasswordInput: StateFlow<String> = _adminPasswordInput.asStateFlow()

    private val _adminLoginError = MutableStateFlow(false)
    val adminLoginError: StateFlow<Boolean> = _adminLoginError.asStateFlow()

    // Form
    private val _formState = MutableStateFlow(FormState(orgLogoPath = getSavedOrgLogo()))
    val formState: StateFlow<FormState> = _formState.asStateFlow()

    // Flag for allowing worker to fill a new form even if an agreement was previously submitted
    private val _isCreatingNewWorkerForm = MutableStateFlow(false)
    val isCreatingNewWorkerForm: StateFlow<Boolean> = _isCreatingNewWorkerForm.asStateFlow()

    // Save confirmation popup
    private val _savedConfirmation = MutableStateFlow<AgreementEntity?>(null)
    val savedConfirmation: StateFlow<AgreementEntity?> = _savedConfirmation.asStateFlow()

    // Selected agreement for Preview
    private val _selectedAgreement = MutableStateFlow<AgreementEntity?>(null)
    val selectedAgreement: StateFlow<AgreementEntity?> = _selectedAgreement.asStateFlow()

    // Stamp margin toggle for PDF
    private val _use100TkStampMargin = MutableStateFlow(true)
    val use100TkStampMargin: StateFlow<Boolean> = _use100TkStampMargin.asStateFlow()

    // Admin filters
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedDesignationFilter = MutableStateFlow("সকল")
    val selectedDesignationFilter: StateFlow<String> = _selectedDesignationFilter.asStateFlow()

    private val _selectedDateFilter = MutableStateFlow("সকল")
    val selectedDateFilter: StateFlow<String> = _selectedDateFilter.asStateFlow()

    private val _selectedDocumentCategoryFilter = MutableStateFlow("সকল")
    val selectedDocumentCategoryFilter: StateFlow<String> = _selectedDocumentCategoryFilter.asStateFlow()

    // Dialogs
    private val _showShareDialog = MutableStateFlow(false)
    val showShareDialog: StateFlow<Boolean> = _showShareDialog.asStateFlow()

    private val _showQrDialog = MutableStateFlow(false)
    val showQrDialog: StateFlow<Boolean> = _showQrDialog.asStateFlow()

    private val prefs = application.getSharedPreferences("rrf_form_prefs", android.content.Context.MODE_PRIVATE)

    private val _orgLogoUri = MutableStateFlow(prefs.getString("org_logo_uri", null))
    val orgLogoUri: StateFlow<String?> = _orgLogoUri.asStateFlow()

    fun setOrgLogoUri(uriStr: String?) {
        _orgLogoUri.value = uriStr
        prefs.edit().putString("org_logo_uri", uriStr).apply()
    }

    private val _customLeadershipUris = MutableStateFlow<List<String>>(
        listOfNotNull(
            prefs.getString("lead_uri_0", null),
            prefs.getString("lead_uri_1", null),
            prefs.getString("lead_uri_2", null),
            prefs.getString("lead_uri_3", null)
        ).filter { it.isNotBlank() }
    )
    val customLeadershipUris: StateFlow<List<String>> = _customLeadershipUris.asStateFlow()

    fun setCustomLeadershipUris(uris: List<String>) {
        _customLeadershipUris.value = uris
        val editor = prefs.edit()
        for (i in 0 until 4) {
            if (i < uris.size) {
                editor.putString("lead_uri_$i", uris[i])
            } else {
                editor.remove("lead_uri_$i")
            }
        }
        editor.apply()
    }

    // Learned addresses
    private val _learnedPostOffices = MutableStateFlow<List<String>>(emptyList())
    val learnedPostOffices: StateFlow<List<String>> = _learnedPostOffices.asStateFlow()

    private val _learnedVillages = MutableStateFlow<List<String>>(emptyList())
    val learnedVillages: StateFlow<List<String>> = _learnedVillages.asStateFlow()

    private val _learnedGuarantorPostOffices = MutableStateFlow<List<String>>(emptyList())
    val learnedGuarantorPostOffices: StateFlow<List<String>> = _learnedGuarantorPostOffices.asStateFlow()

    private val _learnedGuarantorVillages = MutableStateFlow<List<String>>(emptyList())
    val learnedGuarantorVillages: StateFlow<List<String>> = _learnedGuarantorVillages.asStateFlow()

    // Agreement 5-Forms State
    private val _allAgreementForms = MutableStateFlow(AllAgreementForms())
    val allAgreementForms: StateFlow<AllAgreementForms> = _allAgreementForms.asStateFlow()

    // Firebase Cloud Functions
    val cloudFunctionsManager: FirebaseFunctionsManager = FirebaseFunctionsManager.getInstance(application)

    private val _isCloudOperating = MutableStateFlow(false)
    val isCloudOperating: StateFlow<Boolean> = _isCloudOperating.asStateFlow()

    private val _cloudOperationResult = MutableStateFlow<String?>(null)
    val cloudOperationResult: StateFlow<String?> = _cloudOperationResult.asStateFlow()

    fun clearCloudOperationResult() {
        _cloudOperationResult.value = null
    }

    fun checkCloudHealth() {
        viewModelScope.launch {
            _isCloudOperating.value = true
            _cloudOperationResult.value = "সার্ভার স্ট্যাটাস যাচাই করা হচ্ছে..."
            when (val res = cloudFunctionsManager.checkHealth()) {
                is CloudFunctionResult.Success -> {
                    _cloudOperationResult.value = "সার্ভার সক্রিয়! রেসপন্স: ${res.data}"
                }
                is CloudFunctionResult.Error -> {
                    _cloudOperationResult.value = "ত্রুটি: ${res.message}"
                }
            }
            _isCloudOperating.value = false
        }
    }

    fun backupCurrentWorkerToCloud() {
        viewModelScope.launch {
            _isCloudOperating.value = true
            _cloudOperationResult.value = "ক্লাউডে ডেটা ব্যাকআপ হচ্ছে..."
            val empName = _formState.value.employeeName.ifBlank { _allAgreementForms.value.personalInfoForm.employeeNameBangla }
            val empNid = _formState.value.guarantorNid.ifBlank { _allAgreementForms.value.personalInfoForm.employeeNid }

            val payloadStr = "Employee: $empName, NID: $empNid, Designation: ${_formState.value.designation}, Village: ${_formState.value.employeeVillage}"

            when (val res = cloudFunctionsManager.backupAgreementForms(empName, empNid, payloadStr)) {
                is CloudFunctionResult.Success -> {
                    _cloudOperationResult.value = "সফলভাবে ব্যাকআপ সম্পন্ন হয়েছে! ডকুমেন্ট আইডি: ${res.data["docId"] ?: "সংরক্ষিত"}"
                }
                is CloudFunctionResult.Error -> {
                    _cloudOperationResult.value = "ব্যাকআপ ত্রুটি: ${res.message}"
                }
            }
            _isCloudOperating.value = false
        }
    }

    fun verifyEmployeeViaCloud(nid: String, mobile: String) {
        viewModelScope.launch {
            _isCloudOperating.value = true
            _cloudOperationResult.value = "এনআইডি যাচাই করা হচ্ছে..."
            when (val res = cloudFunctionsManager.verifyEmployeeOnline(nid, mobile)) {
                is CloudFunctionResult.Success -> {
                    _cloudOperationResult.value = "যাচাই ফলাফল: ${res.data["message"] ?: res.data}"
                }
                is CloudFunctionResult.Error -> {
                    _cloudOperationResult.value = "যাচাই ত্রুটি: ${res.message}"
                }
            }
            _isCloudOperating.value = false
        }
    }

    fun sendNotificationViaCloud(mobile: String, message: String) {
        viewModelScope.launch {
            _isCloudOperating.value = true
            _cloudOperationResult.value = "বিজ্ঞপ্তি প্রেরণ করা হচ্ছে..."
            when (val res = cloudFunctionsManager.sendVerificationNotification(mobile, message)) {
                is CloudFunctionResult.Success -> {
                    _cloudOperationResult.value = "বিজ্ঞপ্তি সফলভাবে প্রেরণ হয়েছে! রেসপন্স: ${res.data}"
                }
                is CloudFunctionResult.Error -> {
                    _cloudOperationResult.value = "প্রেরণ ত্রুটি: ${res.message}"
                }
            }
            _isCloudOperating.value = false
        }
    }

    init {
        val app = application
        val loadedStamp = DraftStorageManager.loadStampFormDraft(app, _formState.value)
        _formState.value = loadedStamp

        val loadedAgreements = DraftStorageManager.loadAllAgreementFormsDraft(app, _allAgreementForms.value)
        _allAgreementForms.value = loadedAgreements

        val lastScreen = DraftStorageManager.getLastActiveScreen(app)
        if (lastScreen != null) {
            _currentScreen.value = lastScreen
        }

        refreshLearnedAddresses(_formState.value.employeeDistrict, _formState.value.employeeUpazila)
        refreshGuarantorLearnedAddresses(_formState.value.guarantorDistrict, _formState.value.guarantorUpazila)
    }

    fun syncAgreementFormsWithStampData() {
        val form = _formState.value
        val local = if (_isCreatingNewWorkerForm.value) null else localWorkerAgreement.value

        val empName = if (form.employeeName.isNotBlank()) form.employeeName else local?.employeeName.orEmpty()
        val empFather = if (form.employeeFatherName.isNotBlank()) form.employeeFatherName else local?.employeeFatherName.orEmpty()
        val desig = form.designation
        val village = if (form.employeeVillage.isNotBlank()) form.employeeVillage else local?.employeeVillage.orEmpty()
        val post = if (form.employeePostOffice.isNotBlank()) form.employeePostOffice else local?.employeePostOffice.orEmpty()
        val upazila = if (form.employeeUpazila.isNotBlank()) form.employeeUpazila else local?.employeeUpazila.orEmpty()
        val district = if (form.employeeDistrict.isNotBlank()) form.employeeDistrict else local?.employeeDistrict.orEmpty()

        val gName = if (form.guarantorName.isNotBlank()) form.guarantorName else local?.guarantorName.orEmpty()
        val gFather = if (form.guarantorFatherName.isNotBlank()) form.guarantorFatherName else local?.guarantorFatherName.orEmpty()
        val gMother = if (form.guarantorMotherName.isNotBlank()) form.guarantorMotherName else local?.guarantorMotherName.orEmpty()
        val gNid = if (form.guarantorRelationship.isNotBlank() && form.guarantorNid.isNotBlank()) form.guarantorNid else ""
        val gRel = if (form.guarantorRelationship.isNotBlank()) form.guarantorRelationship else local?.effectiveGuarantorRelationship.orEmpty()
        val gVill = if (form.sameAddress) village else if (form.guarantorVillage.isNotBlank()) form.guarantorVillage else local?.guarantorVillage.orEmpty()
        val gPost = if (form.sameAddress) post else if (form.guarantorPostOffice.isNotBlank()) form.guarantorPostOffice else local?.guarantorPostOffice.orEmpty()
        val gUpazila = if (form.sameAddress) upazila else if (form.guarantorUpazila.isNotBlank()) form.guarantorUpazila else local?.guarantorUpazila.orEmpty()
        val gDistrict = if (form.sameAddress) district else if (form.guarantorDistrict.isNotBlank()) form.guarantorDistrict else local?.guarantorDistrict.orEmpty()

        _allAgreementForms.value = AgreementDataAutoFiller.populateFromStamp(
            current = _allAgreementForms.value,
            employeeName = empName,
            employeeFatherName = empFather,
            designation = desig,
            village = village,
            postOffice = post,
            upazila = upazila,
            district = district,
            guarantorName = gName,
            guarantorFatherName = gFather,
            guarantorMotherName = gMother,
            guarantorNid = gNid,
            guarantorRelationship = gRel,
            guarantorVillage = gVill,
            guarantorPostOffice = gPost,
            guarantorUpazila = gUpazila,
            guarantorDistrict = gDistrict
        )
        DraftStorageManager.saveAllAgreementFormsDraft(getApplication(), _allAgreementForms.value)
    }

    fun propagateSharedData(
        empName: String = "",
        empFather: String = "",
        desig: String = "",
        vill: String = "",
        post: String = "",
        upazila: String = "",
        dist: String = "",
        gName: String = "",
        gFather: String = "",
        gMother: String = "",
        gNid: String = "",
        gRel: String = "",
        gVill: String = "",
        gPost: String = "",
        gUpazila: String = "",
        gDist: String = ""
    ) {
        val current = _formState.value
        val updated = current.copy(
            employeeName = if (empName.isNotBlank() && (BanglaTextValidator.containsBengali(empName) || !BanglaTextValidator.containsBengali(current.employeeName))) empName else current.employeeName,
            employeeFatherName = if (empFather.isNotBlank() && (BanglaTextValidator.containsBengali(empFather) || !BanglaTextValidator.containsBengali(current.employeeFatherName))) empFather else current.employeeFatherName,
            designation = if (desig.isNotBlank()) desig else current.designation,
            employeeVillage = if (vill.isNotBlank()) vill else current.employeeVillage,
            employeePostOffice = if (post.isNotBlank()) post else current.employeePostOffice,
            employeeUpazila = if (upazila.isNotBlank()) upazila else current.employeeUpazila,
            employeeDistrict = if (dist.isNotBlank()) dist else current.employeeDistrict,
            guarantorName = if (gName.isNotBlank() && (BanglaTextValidator.containsBengali(gName) || !BanglaTextValidator.containsBengali(current.guarantorName))) gName else current.guarantorName,
            guarantorFatherName = if (gFather.isNotBlank() && (BanglaTextValidator.containsBengali(gFather) || !BanglaTextValidator.containsBengali(current.guarantorFatherName))) gFather else current.guarantorFatherName,
            guarantorMotherName = if (gMother.isNotBlank() && (BanglaTextValidator.containsBengali(gMother) || !BanglaTextValidator.containsBengali(current.guarantorMotherName))) gMother else current.guarantorMotherName,
            guarantorNid = gNid,
            guarantorRelationship = gRel,
            guarantorVillage = if (gVill.isNotBlank()) gVill else current.guarantorVillage,
            guarantorPostOffice = if (gPost.isNotBlank()) gPost else current.guarantorPostOffice,
            guarantorUpazila = if (gUpazila.isNotBlank()) gUpazila else current.guarantorUpazila,
            guarantorDistrict = if (gDist.isNotBlank()) gDist else current.guarantorDistrict
        )
        if (updated != current) {
            _formState.value = updated
            DraftStorageManager.saveStampFormDraft(getApplication(), updated)
        }
        syncAgreementFormsWithStampData()
        DraftStorageManager.saveAllAgreementFormsDraft(getApplication(), _allAgreementForms.value)
    }

    fun syncStampFromAgreementForms() {
        val current = _formState.value
        val allForms = _allAgreementForms.value
        val pInfo = allForms.personalInfoForm
        val tForm = allForms.trainingForm
        val vForm = allForms.verificationForm

        // Strictly prefer Bangla names and Bangla text
        val bEmpName = pInfo.employeeNameBangla.ifBlank {
            if (BanglaTextValidator.containsBengali(tForm.employeeName)) tForm.employeeName
            else if (BanglaTextValidator.containsBengali(vForm.staffFullName)) vForm.staffFullName
            else ""
        }
        val bFatherName = pInfo.fatherName.ifBlank {
            if (BanglaTextValidator.containsBengali(tForm.fatherName)) tForm.fatherName
            else if (BanglaTextValidator.containsBengali(vForm.staffFatherName)) vForm.staffFatherName
            else ""
        }
        val bDesig = if (tForm.designation.isNotBlank()) {
            tForm.designation
        } else if (_allAgreementForms.value.idCardForm.designation.isNotBlank()) {
            _allAgreementForms.value.idCardForm.designation
        } else {
            current.designation
        }
        val bVill = pInfo.permVillage.ifBlank { pInfo.currVillageOrHouse }
        val bPost = pInfo.permPostOffice.ifBlank { pInfo.currPostOffice }
        val bUpazila = pInfo.permUpazila.ifBlank { pInfo.permThana }
        val bDist = pInfo.permDistrict

        val bGName = pInfo.guarantorName
        val bGFather = pInfo.guarantorFatherName
        val bGMother = pInfo.guarantorMotherName
        val bGNid = BanglaTextValidator.toBanglaDigits(pInfo.guarantorNid)
        val bGRel = pInfo.guarantorRelationship
        val bGVill = pInfo.guarantorVillage
        val bGPost = pInfo.guarantorPostOffice
        val bGUpazila = pInfo.guarantorUpazila.ifBlank { pInfo.guarantorThana }
        val bGDist = pInfo.guarantorDistrict

        val updated = current.copy(
            employeeName = if (bEmpName.isNotBlank() && (BanglaTextValidator.containsBengali(bEmpName) || current.employeeName.isBlank())) bEmpName else current.employeeName,
            employeeFatherName = if (bFatherName.isNotBlank() && (BanglaTextValidator.containsBengali(bFatherName) || current.employeeFatherName.isBlank())) bFatherName else current.employeeFatherName,
            designation = if (bDesig.isNotBlank()) bDesig else current.designation,
            employeeVillage = if (bVill.isNotBlank()) bVill else current.employeeVillage,
            employeePostOffice = if (bPost.isNotBlank()) bPost else current.employeePostOffice,
            employeeUpazila = if (bUpazila.isNotBlank()) bUpazila else current.employeeUpazila,
            employeeDistrict = if (bDist.isNotBlank()) bDist else current.employeeDistrict,
            guarantorName = if (bGName.isNotBlank()) bGName else current.guarantorName,
            guarantorFatherName = if (bGFather.isNotBlank()) bGFather else current.guarantorFatherName,
            guarantorMotherName = if (bGMother.isNotBlank()) bGMother else current.guarantorMotherName,
            guarantorNid = bGNid,
            guarantorRelationship = bGRel,
            guarantorVillage = if (bGVill.isNotBlank()) bGVill else current.guarantorVillage,
            guarantorPostOffice = if (bGPost.isNotBlank()) bGPost else current.guarantorPostOffice,
            guarantorUpazila = if (bGUpazila.isNotBlank()) bGUpazila else current.guarantorUpazila,
            guarantorDistrict = if (bGDist.isNotBlank()) bGDist else current.guarantorDistrict
        )

        if (updated != current) {
            _formState.value = updated
            DraftStorageManager.saveStampFormDraft(getApplication(), updated)
        }
    }

    fun updateIdCardForm(state: IdCardFormState) {
        var updatedForms = _allAgreementForms.value.copy(idCardForm = state)
        // Auto-sync English name if entered
        if (state.employeeName.isNotBlank() && updatedForms.personalInfoForm.employeeNameEnglish != state.employeeName) {
            updatedForms = updatedForms.copy(
                personalInfoForm = updatedForms.personalInfoForm.copy(
                    employeeNameEnglish = state.employeeName
                )
            )
        }
        _allAgreementForms.value = updatedForms
        DraftStorageManager.saveAllAgreementFormsDraft(getApplication(), updatedForms)
        DraftStorageManager.saveLastActiveScreen(getApplication(), AppScreen.FORM_ID_CARD)
    }

    fun updateTrainingForm(state: TrainingFormState) {
        var updatedForms = _allAgreementForms.value.copy(trainingForm = state)
        if (BanglaTextValidator.isValidBanglaName(state.motherName) && (updatedForms.personalInfoForm.motherName.isBlank() || BanglaTextValidator.isScrambledBangla(updatedForms.personalInfoForm.motherName))) {
            updatedForms = updatedForms.copy(
                personalInfoForm = updatedForms.personalInfoForm.copy(motherName = state.motherName)
            )
        }
        _allAgreementForms.value = updatedForms
        propagateSharedData(
            empName = state.employeeName,
            empFather = state.fatherName,
            desig = state.designation
        )
        DraftStorageManager.saveAllAgreementFormsDraft(getApplication(), _allAgreementForms.value)
        DraftStorageManager.saveLastActiveScreen(getApplication(), AppScreen.FORM_TRAINING)
    }

    fun updateRelationshipForm(state: RelationshipFormState) {
        _allAgreementForms.value = _allAgreementForms.value.copy(relationshipForm = state)
        propagateSharedData(empName = state.employeeName)
        DraftStorageManager.saveAllAgreementFormsDraft(getApplication(), _allAgreementForms.value)
        DraftStorageManager.saveLastActiveScreen(getApplication(), AppScreen.FORM_RELATIONSHIP)
    }

    fun updateNomineeForm(state: NomineeFormState) {
        _allAgreementForms.value = _allAgreementForms.value.copy(nomineeForm = state)
        propagateSharedData(empName = state.employeeName)
        DraftStorageManager.saveAllAgreementFormsDraft(getApplication(), _allAgreementForms.value)
        DraftStorageManager.saveLastActiveScreen(getApplication(), AppScreen.FORM_NOMINEE)
    }

    fun updatePersonalInfoForm(state: PersonalInfoFormState) {
        var updatedForms = _allAgreementForms.value.copy(personalInfoForm = state)
        // Auto-sync English name to ID Card form
        if (state.employeeNameEnglish.isNotBlank() && updatedForms.idCardForm.employeeName != state.employeeNameEnglish) {
            updatedForms = updatedForms.copy(
                idCardForm = updatedForms.idCardForm.copy(
                    employeeName = state.employeeNameEnglish
                )
            )
        }
        _allAgreementForms.value = updatedForms

        propagateSharedData(
            empName = state.employeeNameBangla,
            empFather = state.fatherName,
            vill = state.permVillage,
            post = state.permPostOffice,
            upazila = state.permUpazila.ifBlank { state.permThana },
            dist = state.permDistrict,
            gName = state.guarantorName,
            gFather = state.guarantorFatherName,
            gMother = state.guarantorMotherName,
            gNid = state.guarantorNid,
            gRel = state.guarantorRelationship,
            gVill = state.guarantorVillage,
            gPost = state.guarantorPostOffice,
            gUpazila = state.guarantorUpazila.ifBlank { state.guarantorThana },
            gDist = state.guarantorDistrict
        )
        DraftStorageManager.saveAllAgreementFormsDraft(getApplication(), _allAgreementForms.value)
        DraftStorageManager.saveLastActiveScreen(getApplication(), AppScreen.FORM_PERSONAL_INFO)
    }

    fun updateVerificationForm(state: VerificationFormState) {
        _allAgreementForms.value = _allAgreementForms.value.copy(verificationForm = state)
        propagateSharedData(
            empName = state.staffFullName,
            empFather = state.staffFatherName
        )
        DraftStorageManager.saveAllAgreementFormsDraft(getApplication(), _allAgreementForms.value)
        DraftStorageManager.saveLastActiveScreen(getApplication(), AppScreen.FORM_VERIFICATION)
    }

    fun clearStampWorkerForm() {
        _formState.value = FormState(orgLogoPath = _orgLogoUri.value)
        DraftStorageManager.saveStampFormDraft(getApplication(), _formState.value)
    }

    fun clearIdCardForm() {
        val updated = _allAgreementForms.value.copy(idCardForm = IdCardFormState())
        _allAgreementForms.value = updated
        _formState.value = _formState.value.copy(designation = updated.trainingForm.designation)
        DraftStorageManager.saveAllAgreementFormsDraft(getApplication(), updated)
        DraftStorageManager.saveStampFormDraft(getApplication(), _formState.value)
    }

    fun clearTrainingForm() {
        val updated = _allAgreementForms.value.copy(trainingForm = TrainingFormState())
        _allAgreementForms.value = updated
        _formState.value = _formState.value.copy(designation = updated.idCardForm.designation)
        DraftStorageManager.saveAllAgreementFormsDraft(getApplication(), updated)
        DraftStorageManager.saveStampFormDraft(getApplication(), _formState.value)
    }

    fun clearRelationshipForm() {
        val updated = _allAgreementForms.value.copy(relationshipForm = RelationshipFormState())
        _allAgreementForms.value = updated
        DraftStorageManager.saveAllAgreementFormsDraft(getApplication(), updated)
    }

    fun clearNomineeForm() {
        val updated = _allAgreementForms.value.copy(nomineeForm = NomineeFormState())
        _allAgreementForms.value = updated
        DraftStorageManager.saveAllAgreementFormsDraft(getApplication(), updated)
    }

    fun clearPersonalInfoForm() {
        val updated = _allAgreementForms.value.copy(personalInfoForm = PersonalInfoFormState())
        _allAgreementForms.value = updated
        DraftStorageManager.saveAllAgreementFormsDraft(getApplication(), updated)
        try {
            val f = java.io.File(getApplication<android.app.Application>().cacheDir, "Personal_Information_Search_Form.pdf")
            if (f.exists()) f.delete()
            val fMerged = java.io.File(getApplication<android.app.Application>().cacheDir, "Merged_All_Agreement_Forms.pdf")
            if (fMerged.exists()) fMerged.delete()
        } catch (_: Exception) {}
    }

    fun clearVerificationForm() {
        val updated = _allAgreementForms.value.copy(verificationForm = VerificationFormState())
        _allAgreementForms.value = updated
        DraftStorageManager.saveAllAgreementFormsDraft(getApplication(), updated)
    }

    fun resetAllFormsAndPdfs() {
        _formState.value = FormState(orgLogoPath = _orgLogoUri.value)
        _allAgreementForms.value = AllAgreementForms()
        _isCreatingNewWorkerForm.value = true
        _selectedAgreement.value = null
        DraftStorageManager.clearAllDraftsAndPdfs(getApplication())
        viewModelScope.launch {
            repository.deleteAllAgreements()
        }
    }

    private fun resolveToBanglaDesignation(raw: String): String {
        val t = raw.trim()
        return when {
            t.contains("loan", ignoreCase = true) || t.contains("ঋণ") || t.contains("লোন") -> "অফিসার (ঋণ)"
            t.contains("account", ignoreCase = true) || t.contains("অ্যাকাউন্ট") || t.contains("হিসাব") -> "অফিসার (অ্যাকাউন্টস)"
            t.contains("service", ignoreCase = true) || t.contains("সার্ভিস") -> "সার্ভিস স্টাফ"
            t.contains("branch", ignoreCase = true) || t.contains("শাখা") -> "শাখা ব্যবস্থাপক"
            t.contains("field", ignoreCase = true) || t.contains("ফিল্ড") -> "ফিল্ড অফিসার"
            t.isNotBlank() && t != "অন্যান্য" -> {
                if (BanglaTextValidator.containsBengali(t)) t
                else BanglaAddressHelper.transliterateEnglishToBangla(t)
            }
            else -> "অফিসার (ঋণ)"
        }
    }

    fun getStampAgreementForPreview(): AgreementEntity {
        val form = _formState.value
        val local = localWorkerAgreement.value
        val allForms = _allAgreementForms.value

        // Resolve candidate designation from all forms, converting to Bangla designation
        val rawDesig = listOf(
            allForms.trainingForm.designation,
            allForms.idCardForm.designation,
            allForms.idCardForm.customDesignation,
            form.designation,
            local?.designation.orEmpty()
        ).firstOrNull { it.isNotBlank() && it != "অন্যান্য" } ?: ""
        val resolvedDesig = resolveToBanglaDesignation(rawDesig)

        // Resolve employee name in Bangla from all sources
        val resolvedEmpName = listOf(
            form.employeeName,
            allForms.personalInfoForm.employeeNameBangla,
            allForms.trainingForm.employeeName,
            allForms.verificationForm.staffFullName,
            local?.employeeName.orEmpty()
        ).firstOrNull { it.isNotBlank() && BanglaTextValidator.containsBengali(it) }
            ?: listOf(form.employeeName, local?.employeeName.orEmpty(), allForms.trainingForm.employeeName).firstOrNull { it.isNotBlank() }
            ?: ""

        // Resolve employee father name from all sources
        val resolvedFatherName = listOf(
            form.employeeFatherName,
            allForms.personalInfoForm.fatherName,
            allForms.trainingForm.fatherName,
            allForms.verificationForm.staffFatherName,
            local?.employeeFatherName.orEmpty()
        ).firstOrNull { it.isNotBlank() } ?: ""

        // Resolve guarantor details from all sources
        val resolvedGuarantorName = listOf(
            form.guarantorName,
            allForms.personalInfoForm.guarantorName,
            local?.guarantorName.orEmpty()
        ).firstOrNull { it.isNotBlank() } ?: ""

        val resolvedGuarantorFather = listOf(
            form.guarantorFatherName,
            allForms.personalInfoForm.guarantorFatherName,
            local?.guarantorFatherName.orEmpty()
        ).firstOrNull { it.isNotBlank() } ?: ""

        val resolvedGuarantorMother = listOf(
            form.guarantorMotherName,
            allForms.personalInfoForm.guarantorMotherName,
            local?.guarantorMotherName.orEmpty()
        ).firstOrNull { it.isNotBlank() } ?: ""

        val resolvedGuarantorNid = if (form.guarantorNid.isNotBlank()) {
            form.guarantorNid
        } else {
            allForms.personalInfoForm.guarantorNid
        }

        val resolvedGuarantorRel = if (form.guarantorRelationship.isNotBlank()) {
            form.guarantorRelationship
        } else {
            allForms.personalInfoForm.guarantorRelationship
        }

        val resolvedEmpVill = listOf(form.employeeVillage, allForms.personalInfoForm.permVillage, allForms.trainingForm.village, local?.employeeVillage.orEmpty()).firstOrNull { it.isNotBlank() } ?: ""
        val resolvedEmpPost = listOf(form.employeePostOffice, allForms.personalInfoForm.permPostOffice, allForms.trainingForm.postOffice, local?.employeePostOffice.orEmpty()).firstOrNull { it.isNotBlank() } ?: ""
        val resolvedEmpUpazila = listOf(form.employeeUpazila, allForms.personalInfoForm.permUpazila, allForms.trainingForm.thana, local?.employeeUpazila.orEmpty()).firstOrNull { it.isNotBlank() } ?: ""
        val resolvedEmpDist = listOf(form.employeeDistrict, allForms.personalInfoForm.permDistrict, allForms.trainingForm.district, local?.employeeDistrict.orEmpty()).firstOrNull { it.isNotBlank() } ?: ""

        val resolvedGVill = listOf(form.guarantorVillage, allForms.personalInfoForm.guarantorVillage, local?.guarantorVillage.orEmpty()).firstOrNull { it.isNotBlank() } ?: ""
        val resolvedGPost = listOf(form.guarantorPostOffice, allForms.personalInfoForm.guarantorPostOffice, local?.guarantorPostOffice.orEmpty()).firstOrNull { it.isNotBlank() } ?: ""
        val resolvedGUpazila = listOf(form.guarantorUpazila, allForms.personalInfoForm.guarantorUpazila, local?.guarantorUpazila.orEmpty()).firstOrNull { it.isNotBlank() } ?: ""
        val resolvedGDist = listOf(form.guarantorDistrict, allForms.personalInfoForm.guarantorDistrict, local?.guarantorDistrict.orEmpty()).firstOrNull { it.isNotBlank() } ?: ""

        val serial = form.editingSerialNo ?: local?.serialNo ?: "RRF-${SimpleDateFormat("yyyyMMdd-HHmm", Locale.US).format(Date())}"
        val date = SimpleDateFormat("dd/MM/yyyy", Locale.US).format(Date())

        return local?.copy(
            employeeName = resolvedEmpName,
            employeeFatherName = resolvedFatherName,
            designation = resolvedDesig,
            employeeDistrict = resolvedEmpDist,
            employeeUpazila = resolvedEmpUpazila,
            employeePostOffice = resolvedEmpPost,
            employeeVillage = resolvedEmpVill,
            guarantorName = resolvedGuarantorName,
            guarantorFatherName = resolvedGuarantorFather,
            guarantorMotherName = resolvedGuarantorMother,
            guarantorNid = resolvedGuarantorNid,
            guarantorRelationship = resolvedGuarantorRel,
            guarantorRelationshipCustom = form.guarantorRelationshipCustom.ifBlank { local.guarantorRelationshipCustom },
            guarantorDistrict = resolvedGDist,
            guarantorUpazila = resolvedGUpazila,
            guarantorPostOffice = resolvedGPost,
            guarantorVillage = resolvedGVill,
            orgLogoPath = _orgLogoUri.value
        ) ?: AgreementEntity(
            serialNo = serial,
            submissionDate = date,
            employeeName = resolvedEmpName,
            employeeFatherName = resolvedFatherName,
            designation = resolvedDesig,
            employeeDistrict = resolvedEmpDist,
            employeeUpazila = resolvedEmpUpazila,
            employeePostOffice = resolvedEmpPost,
            employeeVillage = resolvedEmpVill,
            guarantorName = resolvedGuarantorName,
            guarantorFatherName = resolvedGuarantorFather,
            guarantorMotherName = resolvedGuarantorMother,
            guarantorRelationship = resolvedGuarantorRel,
            guarantorRelationshipCustom = form.guarantorRelationshipCustom,
            guarantorNid = resolvedGuarantorNid,
            sameAddress = form.sameAddress,
            guarantorDistrict = resolvedGDist,
            guarantorUpazila = resolvedGUpazila,
            guarantorPostOffice = resolvedGPost,
            guarantorVillage = resolvedGVill,
            orgLogoPath = _orgLogoUri.value
        )
    }

    fun saveAllDrafts() {
        val app = getApplication<Application>()
        DraftStorageManager.saveStampFormDraft(app, _formState.value)
        DraftStorageManager.saveAllAgreementFormsDraft(app, _allAgreementForms.value)
        if (DraftStorageManager.isFormScreen(_currentScreen.value)) {
            DraftStorageManager.saveLastActiveScreen(app, _currentScreen.value)
        }
    }

    fun navigateToNextForm(currentScreen: AppScreen) {
        val next = getNextScreen(currentScreen) ?: AppScreen.HOME
        updateScreen(next)
    }

    fun getNextScreen(screen: AppScreen): AppScreen? = when (screen) {
        AppScreen.FORM_ID_CARD -> AppScreen.FORM_TRAINING
        AppScreen.FORM_TRAINING -> AppScreen.FORM_RELATIONSHIP
        AppScreen.FORM_RELATIONSHIP -> AppScreen.FORM_NOMINEE
        AppScreen.FORM_NOMINEE -> AppScreen.FORM_PERSONAL_INFO
        AppScreen.FORM_PERSONAL_INFO -> AppScreen.FORM_VERIFICATION
        AppScreen.FORM_VERIFICATION -> AppScreen.WORKER_PANEL
        AppScreen.WORKER_PANEL -> AppScreen.HOME
        else -> null
    }

    fun getNextScreenTitle(screen: AppScreen): String? = when (screen) {
        AppScreen.FORM_ID_CARD -> "২. প্রশিক্ষণ তথ্য ও অঙ্গীকারনামা"
        AppScreen.FORM_TRAINING -> "৩. পরিচিত ব্যক্তির সাথে সম্পর্ক"
        AppScreen.FORM_RELATIONSHIP -> "৪. নমিনি তথ্য ফরম"
        AppScreen.FORM_NOMINEE -> "৫. কর্মীর তথ্যানুসন্ধান ফরম (৩ পাতা)"
        AppScreen.FORM_PERSONAL_INFO -> "৬. তথ্য যাচাই ফরম (২ পাতা)"
        AppScreen.FORM_VERIFICATION -> "৭. ১০০ টাকার স্ট্যাম্প চুক্তিপত্র"
        AppScreen.WORKER_PANEL -> "হোম পেজ (প্রিন্ট ও শেয়ার)"
        else -> null
    }

    fun getPreviousScreen(screen: AppScreen): AppScreen? = when (screen) {
        AppScreen.FORM_ID_CARD -> AppScreen.HOME
        AppScreen.FORM_TRAINING -> AppScreen.FORM_ID_CARD
        AppScreen.FORM_RELATIONSHIP -> AppScreen.FORM_TRAINING
        AppScreen.FORM_NOMINEE -> AppScreen.FORM_RELATIONSHIP
        AppScreen.FORM_PERSONAL_INFO -> AppScreen.FORM_NOMINEE
        AppScreen.FORM_VERIFICATION -> AppScreen.FORM_PERSONAL_INFO
        AppScreen.WORKER_PANEL -> AppScreen.FORM_VERIFICATION
        else -> null
    }

    fun updateScreen(screen: AppScreen) {
        if (screen in listOf(
                AppScreen.AGREEMENTS_HUB,
                AppScreen.FORM_ID_CARD,
                AppScreen.FORM_TRAINING,
                AppScreen.FORM_RELATIONSHIP,
                AppScreen.FORM_NOMINEE,
                AppScreen.FORM_PERSONAL_INFO,
                AppScreen.FORM_VERIFICATION,
                AppScreen.WORKER_PANEL
            )
        ) {
            syncStampFromAgreementForms()
            syncAgreementFormsWithStampData()
        }

        if (screen == AppScreen.ADMIN_PANEL && !_isAdminLoggedIn.value) {
            _showAdminLoginDialog.value = true
        } else {
            _currentScreen.value = screen
            if (DraftStorageManager.isFormScreen(screen)) {
                DraftStorageManager.saveLastActiveScreen(getApplication(), screen)
            }
        }
    }

    fun openAdminLoginDialog() {
        _adminPasswordInput.value = ""
        _adminLoginError.value = false
        _showAdminLoginDialog.value = true
    }

    fun closeAdminLoginDialog() {
        _showAdminLoginDialog.value = false
        _adminPasswordInput.value = ""
        _adminLoginError.value = false
    }

    fun onAdminPasswordChange(password: String) {
        _adminPasswordInput.value = password
        _adminLoginError.value = false
    }

    fun verifyAdminPassword(): Boolean {
        if (_adminPasswordInput.value == "39039820") {
            _isAdminLoggedIn.value = true
            _showAdminLoginDialog.value = false
            _adminLoginError.value = false
            _currentScreen.value = AppScreen.ADMIN_PANEL
            return true
        } else {
            _adminLoginError.value = true
            return false
        }
    }

    fun adminLogout() {
        _isAdminLoggedIn.value = false
        _currentScreen.value = AppScreen.HOME
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onDesignationFilterChange(filter: String) {
        _selectedDesignationFilter.value = filter
    }

    fun onDateFilterChange(filter: String) {
        _selectedDateFilter.value = filter
    }

    fun onDocumentCategoryFilterChange(category: String) {
        _selectedDocumentCategoryFilter.value = category
    }

    fun toggle100TkStampMargin(enabled: Boolean) {
        _use100TkStampMargin.value = enabled
    }

    fun setShowShareDialog(show: Boolean) {
        _showShareDialog.value = show
    }

    fun setShowQrDialog(show: Boolean) {
        _showQrDialog.value = show
    }

    // Branch Dispatch and Verification
    fun dispatchAgreementToBranch(agreementId: Long, branchName: String) {
        viewModelScope.launch {
            repository.dispatchToBranch(agreementId, branchName)
        }
    }

    fun updateBranchVerification(agreementId: Long, status: String, notes: String = "") {
        viewModelScope.launch {
            repository.updateBranchVerification(agreementId, status, notes)
        }
    }

    // =========================================================
    // JSON DATA BACKUP & RESTORE (PORTABILITY)
    // =========================================================
    private val _isBackupExporting = MutableStateFlow(false)
    val isBackupExporting: StateFlow<Boolean> = _isBackupExporting.asStateFlow()

    private val _lastExportResult = MutableStateFlow<JsonDataBackupManager.ExportResult?>(null)
    val lastExportResult: StateFlow<JsonDataBackupManager.ExportResult?> = _lastExportResult.asStateFlow()

    private val _importSummary = MutableStateFlow<JsonDataBackupManager.BackupSummary?>(null)
    val importSummary: StateFlow<JsonDataBackupManager.BackupSummary?> = _importSummary.asStateFlow()

    private val _isRestoring = MutableStateFlow(false)
    val isRestoring: StateFlow<Boolean> = _isRestoring.asStateFlow()

    private val _restoreResult = MutableStateFlow<JsonDataBackupManager.RestoreResult?>(null)
    val restoreResult: StateFlow<JsonDataBackupManager.RestoreResult?> = _restoreResult.asStateFlow()

    fun exportLocalDataToJson(context: Context, onComplete: ((JsonDataBackupManager.ExportResult) -> Unit)? = null) {
        viewModelScope.launch {
            _isBackupExporting.value = true
            try {
                val db = AppDatabase.getInstance(context)
                val result = JsonDataBackupManager.exportAllDataToJson(context, db)
                _lastExportResult.value = result
                onComplete?.invoke(result)
            } finally {
                _isBackupExporting.value = false
            }
        }
    }

    fun inspectBackupUri(context: Context, uri: Uri) {
        viewModelScope.launch {
            val summary = JsonDataBackupManager.parseBackupUri(context, uri)
            _importSummary.value = summary
        }
    }

    fun dismissImportDialog() {
        _importSummary.value = null
    }

    fun dismissRestoreResultDialog() {
        _restoreResult.value = null
    }

    fun dismissExportResultDialog() {
        _lastExportResult.value = null
    }

    fun executeRestore(context: Context, replaceExisting: Boolean, restoreDrafts: Boolean) {
        val summary = _importSummary.value ?: return
        viewModelScope.launch {
            _isRestoring.value = true
            try {
                val db = AppDatabase.getInstance(context)
                val res = JsonDataBackupManager.restoreData(
                    context = context,
                    database = db,
                    summary = summary,
                    replaceExisting = replaceExisting,
                    restoreDrafts = restoreDrafts
                )
                _restoreResult.value = res
                _importSummary.value = null
                // Reload form draft
                _formState.value = DraftStorageManager.loadStampFormDraft(context, _formState.value)
            } finally {
                _isRestoring.value = false
            }
        }
    }

    fun selectAgreementForPreview(agreement: AgreementEntity) {
        _selectedAgreement.value = agreement
        _currentScreen.value = AppScreen.AGREEMENT_PREVIEW
    }

    fun closeConfirmationDialog() {
        _savedConfirmation.value = null
    }

    fun dismissValidationErrorDialog() {
        _formState.value = _formState.value.copy(showValidationErrorDialog = false)
    }

    private fun notifyStampFormUpdated() {
        val current = _formState.value
        DraftStorageManager.saveStampFormDraft(getApplication(), current)
        syncAgreementFormsWithStampData()
        if (DraftStorageManager.isFormScreen(_currentScreen.value)) {
            DraftStorageManager.saveLastActiveScreen(getApplication(), _currentScreen.value)
        }
    }

    // Form Field Updates
    fun updateEmployeeName(name: String) {
        val filtered = BanglaTextValidator.filterBanglaText(name)
        _formState.value = _formState.value.copy(
            employeeName = filtered,
            validationErrors = _formState.value.validationErrors - "employeeName"
        )
        notifyStampFormUpdated()
    }

    fun updateEmployeeFatherName(name: String) {
        val filtered = BanglaTextValidator.filterBanglaText(name)
        _formState.value = _formState.value.copy(
            employeeFatherName = filtered,
            validationErrors = _formState.value.validationErrors - "employeeFatherName"
        )
        notifyStampFormUpdated()
    }

    fun updateDesignation(desig: String) {
        _formState.value = _formState.value.copy(designation = desig)
        notifyStampFormUpdated()
    }

    fun updateEmployeeDistrict(district: String) {
        val filtered = BanglaTextValidator.filterBanglaText(district)
        _formState.value = _formState.value.copy(
            employeeDistrict = filtered,
            guarantorDistrict = if (_formState.value.sameAddress) filtered else _formState.value.guarantorDistrict,
            validationErrors = _formState.value.validationErrors - "employeeDistrict"
        )
        notifyStampFormUpdated()
    }

    fun updateEmployeeUpazila(upazila: String) {
        val filtered = BanglaTextValidator.filterBanglaText(upazila)
        _formState.value = _formState.value.copy(
            employeeUpazila = filtered,
            guarantorUpazila = if (_formState.value.sameAddress) filtered else _formState.value.guarantorUpazila,
            validationErrors = _formState.value.validationErrors - "employeeUpazila"
        )
        notifyStampFormUpdated()
    }

    fun updateEmployeePostOffice(postOffice: String) {
        val filtered = BanglaTextValidator.filterBanglaText(postOffice)
        _formState.value = _formState.value.copy(
            employeePostOffice = filtered,
            guarantorPostOffice = if (_formState.value.sameAddress) filtered else _formState.value.guarantorPostOffice,
            validationErrors = _formState.value.validationErrors - "employeePostOffice"
        )
        notifyStampFormUpdated()
    }

    fun updateEmployeeVillage(village: String) {
        val filtered = BanglaTextValidator.filterBanglaText(village)
        _formState.value = _formState.value.copy(
            employeeVillage = filtered,
            guarantorVillage = if (_formState.value.sameAddress) filtered else _formState.value.guarantorVillage,
            validationErrors = _formState.value.validationErrors - "employeeVillage"
        )
        notifyStampFormUpdated()
    }

    fun updateGuarantorName(name: String) {
        val filtered = BanglaTextValidator.filterBanglaText(name)
        _formState.value = _formState.value.copy(
            guarantorName = filtered,
            validationErrors = _formState.value.validationErrors - "guarantorName"
        )
        notifyStampFormUpdated()
    }

    fun updateGuarantorFatherName(name: String) {
        val filtered = BanglaTextValidator.filterBanglaText(name)
        _formState.value = _formState.value.copy(
            guarantorFatherName = filtered,
            validationErrors = _formState.value.validationErrors - "guarantorFatherName"
        )
        notifyStampFormUpdated()
    }

    fun updateGuarantorMotherName(name: String) {
        val filtered = BanglaTextValidator.filterBanglaText(name)
        _formState.value = _formState.value.copy(
            guarantorMotherName = filtered,
            validationErrors = _formState.value.validationErrors - "guarantorMotherName"
        )
        notifyStampFormUpdated()
    }

    fun updateGuarantorRelationship(rel: String) {
        _formState.value = _formState.value.copy(guarantorRelationship = rel)
        notifyStampFormUpdated()
    }

    fun updateGuarantorRelationshipCustom(custom: String) {
        val filtered = BanglaTextValidator.filterBanglaText(custom)
        _formState.value = _formState.value.copy(
            guarantorRelationshipCustom = filtered,
            validationErrors = _formState.value.validationErrors - "guarantorRelationshipCustom"
        )
        notifyStampFormUpdated()
    }

    fun updateGuarantorNid(nid: String) {
        val filtered = BanglaTextValidator.filterDigitsOnly(nid)
        _formState.value = _formState.value.copy(
            guarantorNid = filtered,
            validationErrors = _formState.value.validationErrors - "guarantorNid"
        )
        notifyStampFormUpdated()
    }

    fun toggleSameAddress(checked: Boolean) {
        val cur = _formState.value
        if (checked) {
            _formState.value = cur.copy(
                sameAddress = true,
                guarantorDistrict = cur.employeeDistrict,
                guarantorUpazila = cur.employeeUpazila,
                guarantorPostOffice = cur.employeePostOffice,
                guarantorVillage = cur.employeeVillage,
                validationErrors = cur.validationErrors - setOf(
                    "guarantorDistrict", "guarantorUpazila", "guarantorPostOffice", "guarantorVillage"
                )
            )
            refreshGuarantorLearnedAddresses(cur.employeeDistrict, cur.employeeUpazila)
        } else {
            _formState.value = cur.copy(
                sameAddress = false,
                guarantorDistrict = "",
                guarantorUpazila = "",
                guarantorPostOffice = "",
                guarantorVillage = ""
            )
        }
        notifyStampFormUpdated()
    }

    fun updateGuarantorDistrict(district: String) {
        val filtered = BanglaTextValidator.filterBanglaText(district)
        _formState.value = _formState.value.copy(
            guarantorDistrict = filtered,
            validationErrors = _formState.value.validationErrors - "guarantorDistrict"
        )
        notifyStampFormUpdated()
    }

    fun updateGuarantorUpazila(upazila: String) {
        val filtered = BanglaTextValidator.filterBanglaText(upazila)
        _formState.value = _formState.value.copy(
            guarantorUpazila = filtered,
            validationErrors = _formState.value.validationErrors - "guarantorUpazila"
        )
        notifyStampFormUpdated()
    }

    fun updateGuarantorPostOffice(postOffice: String) {
        val filtered = BanglaTextValidator.filterBanglaText(postOffice)
        _formState.value = _formState.value.copy(
            guarantorPostOffice = filtered,
            validationErrors = _formState.value.validationErrors - "guarantorPostOffice"
        )
        notifyStampFormUpdated()
    }

    fun updateGuarantorVillage(village: String) {
        val filtered = BanglaTextValidator.filterBanglaText(village)
        _formState.value = _formState.value.copy(
            guarantorVillage = filtered,
            validationErrors = _formState.value.validationErrors - "guarantorVillage"
        )
        notifyStampFormUpdated()
    }

    private fun refreshLearnedAddresses(district: String, upazila: String) {
        viewModelScope.launch {
            repository.getPostOffices(district, upazila).collect {
                _learnedPostOffices.value = it
            }
        }
        viewModelScope.launch {
            repository.getVillages(district, upazila).collect {
                _learnedVillages.value = it
            }
        }
    }

    private fun refreshGuarantorLearnedAddresses(district: String, upazila: String) {
        viewModelScope.launch {
            repository.getPostOffices(district, upazila).collect {
                _learnedGuarantorPostOffices.value = it
            }
        }
        viewModelScope.launch {
            repository.getVillages(district, upazila).collect {
                _learnedGuarantorVillages.value = it
            }
        }
    }

    fun getSavedOrgLogo(): String? {
        val prefs = getApplication<Application>().getSharedPreferences("rrf_prefs", android.content.Context.MODE_PRIVATE)
        val path = prefs.getString("org_logo_path", null)
        return if (path != null && java.io.File(path).exists()) path else null
    }

    fun saveUploadedLogo(context: android.content.Context, uri: android.net.Uri): String? {
        return try {
            val destFile = java.io.File(context.filesDir, "uploaded_org_logo.png")
            context.contentResolver.openInputStream(uri)?.use { input ->
                java.io.FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            }
            val path = destFile.absolutePath
            updateOrgLogo(path)
            path
        } catch (_: Exception) {
            null
        }
    }

    fun updateOrgLogo(path: String?) {
        _formState.value = _formState.value.copy(orgLogoPath = path)
        val prefs = getApplication<Application>().getSharedPreferences("rrf_prefs", android.content.Context.MODE_PRIVATE)
        prefs.edit().putString("org_logo_path", path).apply()
    }

    fun resetOrgLogo() {
        val destFile = java.io.File(getApplication<Application>().filesDir, "uploaded_org_logo.png")
        if (destFile.exists()) {
            destFile.delete()
        }
        updateOrgLogo(null)
    }

    fun prepareEditForWorker(agreement: AgreementEntity) {
        _formState.value = FormState(
            employeeName = agreement.employeeName,
            employeeFatherName = agreement.employeeFatherName,
            designation = agreement.designation,
            employeeDistrict = agreement.employeeDistrict,
            employeeUpazila = agreement.employeeUpazila,
            employeePostOffice = agreement.employeePostOffice,
            employeeVillage = agreement.employeeVillage,
            guarantorName = agreement.guarantorName,
            guarantorFatherName = agreement.guarantorFatherName,
            guarantorMotherName = agreement.guarantorMotherName,
            guarantorRelationship = agreement.guarantorRelationship,
            guarantorRelationshipCustom = agreement.guarantorRelationshipCustom,
            guarantorNid = agreement.guarantorNid,
            sameAddress = agreement.sameAddress,
            guarantorDistrict = agreement.guarantorDistrict,
            guarantorUpazila = agreement.guarantorUpazila,
            guarantorPostOffice = agreement.guarantorPostOffice,
            guarantorVillage = agreement.guarantorVillage,
            isEditingByAdmin = false,
            isEditingWorker = true,
            editingAgreementId = agreement.id,
            editingSerialNo = agreement.serialNo,
            currentEditCount = agreement.editCount,
            orgLogoPath = agreement.orgLogoPath ?: getSavedOrgLogo()
        )
        _currentScreen.value = AppScreen.WORKER_PANEL
    }

    fun cancelWorkerEdit() {
        resetForm()
    }

    fun prepareEditForAdmin(agreement: AgreementEntity) {
        _formState.value = FormState(
            employeeName = agreement.employeeName,
            employeeFatherName = agreement.employeeFatherName,
            designation = agreement.designation,
            employeeDistrict = agreement.employeeDistrict,
            employeeUpazila = agreement.employeeUpazila,
            employeePostOffice = agreement.employeePostOffice,
            employeeVillage = agreement.employeeVillage,
            guarantorName = agreement.guarantorName,
            guarantorFatherName = agreement.guarantorFatherName,
            guarantorMotherName = agreement.guarantorMotherName,
            guarantorRelationship = agreement.guarantorRelationship,
            guarantorRelationshipCustom = agreement.guarantorRelationshipCustom,
            guarantorNid = agreement.guarantorNid,
            sameAddress = agreement.sameAddress,
            guarantorDistrict = agreement.guarantorDistrict,
            guarantorUpazila = agreement.guarantorUpazila,
            guarantorPostOffice = agreement.guarantorPostOffice,
            guarantorVillage = agreement.guarantorVillage,
            isEditingByAdmin = true,
            isEditingWorker = false,
            editingAgreementId = agreement.id,
            editingSerialNo = agreement.serialNo,
            currentEditCount = agreement.editCount,
            orgLogoPath = agreement.orgLogoPath ?: getSavedOrgLogo()
        )
        _currentScreen.value = AppScreen.ADMIN_EDIT_FORM
    }

    fun prepareNewFormForAdmin() {
        _formState.value = FormState(isEditingByAdmin = true, orgLogoPath = getSavedOrgLogo())
        _currentScreen.value = AppScreen.ADMIN_EDIT_FORM
    }

    fun startNewWorkerForm() {
        resetForm()
        _isCreatingNewWorkerForm.value = true
        _currentScreen.value = AppScreen.WORKER_PANEL
    }

    fun cancelNewWorkerForm() {
        resetForm()
        _isCreatingNewWorkerForm.value = false
        _currentScreen.value = AppScreen.WORKER_PANEL
    }

    fun resetForm() {
        _formState.value = FormState(orgLogoPath = getSavedOrgLogo())
    }

    fun submitForm(isAdmin: Boolean = false): Boolean {
        val form = _formState.value
        val errors = mutableSetOf<String>()

        if (form.employeeName.trim().isBlank()) errors.add("employeeName")
        if (form.employeeFatherName.trim().isBlank()) errors.add("employeeFatherName")
        if (form.employeeDistrict.trim().isBlank()) errors.add("employeeDistrict")
        if (form.employeeUpazila.trim().isBlank()) errors.add("employeeUpazila")
        if (form.employeePostOffice.trim().isBlank()) errors.add("employeePostOffice")
        if (form.employeeVillage.trim().isBlank()) errors.add("employeeVillage")

        if (form.guarantorName.trim().isBlank()) errors.add("guarantorName")
        if (form.guarantorFatherName.trim().isBlank()) errors.add("guarantorFatherName")
        if (form.guarantorMotherName.trim().isBlank()) errors.add("guarantorMotherName")
        if (form.guarantorRelationship == "অন্যান্য" && form.guarantorRelationshipCustom.trim().isBlank()) {
            errors.add("guarantorRelationshipCustom")
        }
        if (form.guarantorNid.trim().isBlank()) errors.add("guarantorNid")

        if (!form.sameAddress) {
            if (form.guarantorDistrict.trim().isBlank()) errors.add("guarantorDistrict")
            if (form.guarantorUpazila.trim().isBlank()) errors.add("guarantorUpazila")
            if (form.guarantorPostOffice.trim().isBlank()) errors.add("guarantorPostOffice")
            if (form.guarantorVillage.trim().isBlank()) errors.add("guarantorVillage")
        }

        if (errors.isNotEmpty()) {
            _formState.value = form.copy(
                validationErrors = errors,
                showValidationErrorDialog = true
            )
            return false
        }

        viewModelScope.launch {
            val dateStr = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())

            if (form.editingAgreementId != null) {
                // Update existing (Edit mode)
                val existing = repository.getAgreementById(form.editingAgreementId)
                if (existing != null) {
                    val nextEditCount = existing.editCount + 1
                    val updated = existing.copy(
                        employeeName = form.employeeName.trim(),
                        employeeFatherName = form.employeeFatherName.trim(),
                        designation = form.designation,
                        employeeDistrict = form.employeeDistrict.trim(),
                        employeeUpazila = form.employeeUpazila.trim(),
                        employeePostOffice = form.employeePostOffice.trim(),
                        employeeVillage = form.employeeVillage.trim(),
                        guarantorName = form.guarantorName.trim(),
                        guarantorFatherName = form.guarantorFatherName.trim(),
                        guarantorMotherName = form.guarantorMotherName.trim(),
                        guarantorRelationship = form.guarantorRelationship,
                        guarantorRelationshipCustom = form.guarantorRelationshipCustom.trim(),
                        guarantorNid = form.guarantorNid.trim(),
                        sameAddress = form.sameAddress,
                        guarantorDistrict = if (form.sameAddress) form.employeeDistrict.trim() else form.guarantorDistrict.trim(),
                        guarantorUpazila = if (form.sameAddress) form.employeeUpazila.trim() else form.guarantorUpazila.trim(),
                        guarantorPostOffice = if (form.sameAddress) form.employeePostOffice.trim() else form.guarantorPostOffice.trim(),
                        guarantorVillage = if (form.sameAddress) form.employeeVillage.trim() else form.guarantorVillage.trim(),
                        editCount = nextEditCount,
                        lastEditDate = dateStr,
                        orgLogoPath = form.orgLogoPath
                    )
                    repository.updateAgreement(updated)
                    _selectedAgreement.value = updated
                    _savedConfirmation.value = updated

                    if (form.isEditingByAdmin) {
                        _currentScreen.value = AppScreen.ADMIN_PANEL
                    } else {
                        _currentScreen.value = AppScreen.WORKER_PANEL
                    }
                }
            } else {
                // Insert new
                val serial = repository.generateNextSerialNo()
                val entity = AgreementEntity(
                    serialNo = serial,
                    submissionDate = dateStr,
                    employeeName = form.employeeName.trim(),
                    employeeFatherName = form.employeeFatherName.trim(),
                    designation = form.designation,
                    employeeDistrict = form.employeeDistrict.trim(),
                    employeeUpazila = form.employeeUpazila.trim(),
                    employeePostOffice = form.employeePostOffice.trim(),
                    employeeVillage = form.employeeVillage.trim(),
                    guarantorName = form.guarantorName.trim(),
                    guarantorFatherName = form.guarantorFatherName.trim(),
                    guarantorMotherName = form.guarantorMotherName.trim(),
                    guarantorRelationship = form.guarantorRelationship,
                    guarantorRelationshipCustom = form.guarantorRelationshipCustom.trim(),
                    guarantorNid = form.guarantorNid.trim(),
                    sameAddress = form.sameAddress,
                    guarantorDistrict = if (form.sameAddress) form.employeeDistrict.trim() else form.guarantorDistrict.trim(),
                    guarantorUpazila = if (form.sameAddress) form.employeeUpazila.trim() else form.guarantorUpazila.trim(),
                    guarantorPostOffice = if (form.sameAddress) form.employeePostOffice.trim() else form.guarantorPostOffice.trim(),
                    guarantorVillage = if (form.sameAddress) form.employeeVillage.trim() else form.guarantorVillage.trim(),
                    isSubmittedByThisDevice = !isAdmin,
                    editCount = 0,
                    orgLogoPath = form.orgLogoPath
                )

                val id = repository.insertAgreement(entity)
                val insertedEntity = entity.copy(id = id)
                _savedConfirmation.value = insertedEntity
                _selectedAgreement.value = insertedEntity

                if (isAdmin) {
                    _currentScreen.value = AppScreen.ADMIN_PANEL
                } else {
                    _currentScreen.value = AppScreen.WORKER_PANEL
                }
            }

            // Auto-generate updated Stamp PDF & Merged Agreement PDF (replacing previous versions)
            val savedEntity = _selectedAgreement.value
            if (savedEntity != null) {
                try {
                    com.example.util.PdfGenerator.generateAgreementPdf(getApplication(), savedEntity, forStampPaper = true)
                    com.example.util.AgreementFormsPdfGenerator.generateAllMergedAgreementFormsPdf(getApplication(), _allAgreementForms.value)
                } catch (_: Exception) {}
            }

            _formState.value = FormState(orgLogoPath = getSavedOrgLogo())
            _isCreatingNewWorkerForm.value = false
        }

        return true
    }

    fun deleteAgreement(agreement: AgreementEntity) {
        viewModelScope.launch {
            repository.deleteAgreement(agreement)
            if (_selectedAgreement.value?.id == agreement.id) {
                _selectedAgreement.value = null
            }
        }
    }

    fun togglePrintStatus(agreement: AgreementEntity) {
        viewModelScope.launch {
            val newStatus = !agreement.isPrinted
            repository.updatePrintStatus(agreement.id, newStatus)
            if (_selectedAgreement.value?.id == agreement.id) {
                val printDate = if (newStatus) SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date()) else null
                _selectedAgreement.value = _selectedAgreement.value?.copy(isPrinted = newStatus, printDate = printDate)
            }
        }
    }
}
