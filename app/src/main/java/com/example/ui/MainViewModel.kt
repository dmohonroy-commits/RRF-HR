package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AgreementEntity
import com.example.data.AgreementRepository
import com.example.data.AppDatabase
import com.example.util.BanglaTextValidator
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
    WORKER_PANEL,
    ADMIN_PANEL,
    AGREEMENT_PREVIEW,
    ADMIN_EDIT_FORM
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
    val currentEditCount: Int = 0
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

    private val _currentScreen = MutableStateFlow(AppScreen.WORKER_PANEL)
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
    private val _formState = MutableStateFlow(FormState())
    val formState: StateFlow<FormState> = _formState.asStateFlow()

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

    // Dialogs
    private val _showShareDialog = MutableStateFlow(false)
    val showShareDialog: StateFlow<Boolean> = _showShareDialog.asStateFlow()

    private val _showQrDialog = MutableStateFlow(false)
    val showQrDialog: StateFlow<Boolean> = _showQrDialog.asStateFlow()

    // Learned addresses
    private val _learnedPostOffices = MutableStateFlow<List<String>>(emptyList())
    val learnedPostOffices: StateFlow<List<String>> = _learnedPostOffices.asStateFlow()

    private val _learnedVillages = MutableStateFlow<List<String>>(emptyList())
    val learnedVillages: StateFlow<List<String>> = _learnedVillages.asStateFlow()

    private val _learnedGuarantorPostOffices = MutableStateFlow<List<String>>(emptyList())
    val learnedGuarantorPostOffices: StateFlow<List<String>> = _learnedGuarantorPostOffices.asStateFlow()

    private val _learnedGuarantorVillages = MutableStateFlow<List<String>>(emptyList())
    val learnedGuarantorVillages: StateFlow<List<String>> = _learnedGuarantorVillages.asStateFlow()

    init {
        refreshLearnedAddresses(_formState.value.employeeDistrict, _formState.value.employeeUpazila)
        refreshGuarantorLearnedAddresses(_formState.value.guarantorDistrict, _formState.value.guarantorUpazila)
    }

    fun updateScreen(screen: AppScreen) {
        if (screen == AppScreen.ADMIN_PANEL && !_isAdminLoggedIn.value) {
            _showAdminLoginDialog.value = true
        } else {
            _currentScreen.value = screen
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
        _currentScreen.value = AppScreen.WORKER_PANEL
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

    fun toggle100TkStampMargin(enabled: Boolean) {
        _use100TkStampMargin.value = enabled
    }

    fun setShowShareDialog(show: Boolean) {
        _showShareDialog.value = show
    }

    fun setShowQrDialog(show: Boolean) {
        _showQrDialog.value = show
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

    // Form Field Updates
    fun updateEmployeeName(name: String) {
        val filtered = BanglaTextValidator.filterBanglaText(name)
        _formState.value = _formState.value.copy(
            employeeName = filtered,
            validationErrors = _formState.value.validationErrors - "employeeName"
        )
    }

    fun updateEmployeeFatherName(name: String) {
        val filtered = BanglaTextValidator.filterBanglaText(name)
        _formState.value = _formState.value.copy(
            employeeFatherName = filtered,
            validationErrors = _formState.value.validationErrors - "employeeFatherName"
        )
    }

    fun updateDesignation(desig: String) {
        _formState.value = _formState.value.copy(designation = desig)
    }

    fun updateEmployeeDistrict(district: String) {
        val filtered = BanglaTextValidator.filterBanglaText(district)
        _formState.value = _formState.value.copy(
            employeeDistrict = filtered,
            guarantorDistrict = if (_formState.value.sameAddress) filtered else _formState.value.guarantorDistrict,
            validationErrors = _formState.value.validationErrors - "employeeDistrict"
        )
    }

    fun updateEmployeeUpazila(upazila: String) {
        val filtered = BanglaTextValidator.filterBanglaText(upazila)
        _formState.value = _formState.value.copy(
            employeeUpazila = filtered,
            guarantorUpazila = if (_formState.value.sameAddress) filtered else _formState.value.guarantorUpazila,
            validationErrors = _formState.value.validationErrors - "employeeUpazila"
        )
    }

    fun updateEmployeePostOffice(postOffice: String) {
        val filtered = BanglaTextValidator.filterBanglaText(postOffice)
        _formState.value = _formState.value.copy(
            employeePostOffice = filtered,
            guarantorPostOffice = if (_formState.value.sameAddress) filtered else _formState.value.guarantorPostOffice,
            validationErrors = _formState.value.validationErrors - "employeePostOffice"
        )
    }

    fun updateEmployeeVillage(village: String) {
        val filtered = BanglaTextValidator.filterBanglaText(village)
        _formState.value = _formState.value.copy(
            employeeVillage = filtered,
            guarantorVillage = if (_formState.value.sameAddress) filtered else _formState.value.guarantorVillage,
            validationErrors = _formState.value.validationErrors - "employeeVillage"
        )
    }

    fun updateGuarantorName(name: String) {
        val filtered = BanglaTextValidator.filterBanglaText(name)
        _formState.value = _formState.value.copy(
            guarantorName = filtered,
            validationErrors = _formState.value.validationErrors - "guarantorName"
        )
    }

    fun updateGuarantorFatherName(name: String) {
        val filtered = BanglaTextValidator.filterBanglaText(name)
        _formState.value = _formState.value.copy(
            guarantorFatherName = filtered,
            validationErrors = _formState.value.validationErrors - "guarantorFatherName"
        )
    }

    fun updateGuarantorMotherName(name: String) {
        val filtered = BanglaTextValidator.filterBanglaText(name)
        _formState.value = _formState.value.copy(
            guarantorMotherName = filtered,
            validationErrors = _formState.value.validationErrors - "guarantorMotherName"
        )
    }

    fun updateGuarantorRelationship(rel: String) {
        _formState.value = _formState.value.copy(guarantorRelationship = rel)
    }

    fun updateGuarantorRelationshipCustom(custom: String) {
        val filtered = BanglaTextValidator.filterBanglaText(custom)
        _formState.value = _formState.value.copy(
            guarantorRelationshipCustom = filtered,
            validationErrors = _formState.value.validationErrors - "guarantorRelationshipCustom"
        )
    }

    fun updateGuarantorNid(nid: String) {
        val filtered = BanglaTextValidator.filterDigitsOnly(nid)
        _formState.value = _formState.value.copy(
            guarantorNid = filtered,
            validationErrors = _formState.value.validationErrors - "guarantorNid"
        )
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
            _formState.value = cur.copy(sameAddress = false)
        }
    }

    fun updateGuarantorDistrict(district: String) {
        val filtered = BanglaTextValidator.filterBanglaText(district)
        _formState.value = _formState.value.copy(
            guarantorDistrict = filtered,
            validationErrors = _formState.value.validationErrors - "guarantorDistrict"
        )
    }

    fun updateGuarantorUpazila(upazila: String) {
        val filtered = BanglaTextValidator.filterBanglaText(upazila)
        _formState.value = _formState.value.copy(
            guarantorUpazila = filtered,
            validationErrors = _formState.value.validationErrors - "guarantorUpazila"
        )
    }

    fun updateGuarantorPostOffice(postOffice: String) {
        val filtered = BanglaTextValidator.filterBanglaText(postOffice)
        _formState.value = _formState.value.copy(
            guarantorPostOffice = filtered,
            validationErrors = _formState.value.validationErrors - "guarantorPostOffice"
        )
    }

    fun updateGuarantorVillage(village: String) {
        val filtered = BanglaTextValidator.filterBanglaText(village)
        _formState.value = _formState.value.copy(
            guarantorVillage = filtered,
            validationErrors = _formState.value.validationErrors - "guarantorVillage"
        )
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
            currentEditCount = agreement.editCount
        )
        _currentScreen.value = AppScreen.WORKER_PANEL
    }

    fun cancelWorkerEdit() {
        _formState.value = FormState()
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
            currentEditCount = agreement.editCount
        )
        _currentScreen.value = AppScreen.ADMIN_EDIT_FORM
    }

    fun prepareNewFormForAdmin() {
        _formState.value = FormState(isEditingByAdmin = true)
        _currentScreen.value = AppScreen.ADMIN_EDIT_FORM
    }

    fun resetForm() {
        _formState.value = FormState()
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
                        lastEditDate = dateStr
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
                    editCount = 0
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

            _formState.value = FormState()
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
