package com.example.data

import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AgreementRepository(
    private val agreementDao: AgreementDao,
    private val addressDao: AddressDao
) {
    val allAgreements: Flow<List<AgreementEntity>> = agreementDao.getAllAgreements()
    val localWorkerAgreement: Flow<AgreementEntity?> = agreementDao.getLocalWorkerAgreement()
    val localWorkerAgreements: Flow<List<AgreementEntity>> = agreementDao.getLocalWorkerAgreements()

    suspend fun getAgreementById(id: Long): AgreementEntity? {
        return agreementDao.getAgreementById(id)
    }

    suspend fun generateNextSerialNo(): String {
        val count = agreementDao.getCount()
        val year = SimpleDateFormat("yyyy", Locale.getDefault()).format(Date())
        val nextNum = count + 1
        return String.format(Locale.US, "RRF-%s-%04d", year, nextNum)
    }

    suspend fun insertAgreement(agreement: AgreementEntity): Long {
        val id = agreementDao.insertAgreement(agreement)
        // Also save entered address for future autocomplete
        saveLearnedAddresses(
            agreement.employeeDistrict,
            agreement.employeeUpazila,
            agreement.employeePostOffice,
            agreement.employeeVillage
        )
        if (!agreement.sameAddress) {
            saveLearnedAddresses(
                agreement.guarantorDistrict,
                agreement.guarantorUpazila,
                agreement.guarantorPostOffice,
                agreement.guarantorVillage
            )
        }
        return id
    }

    suspend fun updateAgreement(agreement: AgreementEntity) {
        agreementDao.updateAgreement(agreement)
        saveLearnedAddresses(
            agreement.employeeDistrict,
            agreement.employeeUpazila,
            agreement.employeePostOffice,
            agreement.employeeVillage
        )
    }

    suspend fun deleteAgreement(agreement: AgreementEntity) {
        agreementDao.deleteAgreement(agreement)
    }

    suspend fun deleteAgreementById(id: Long) {
        agreementDao.deleteAgreementById(id)
    }

    suspend fun deleteLocalWorkerAgreements() {
        agreementDao.deleteLocalWorkerAgreements()
    }

    suspend fun deleteAllAgreements() {
        agreementDao.deleteAllAgreements()
    }

    suspend fun getAllAgreementsList(): List<AgreementEntity> {
        return agreementDao.getAllAgreementsList()
    }

    suspend fun insertAgreements(list: List<AgreementEntity>) {
        agreementDao.insertAgreements(list)
    }

    suspend fun getAllAddressItems(): List<AddressItemEntity> {
        return addressDao.getAllAddressItems()
    }

    suspend fun insertAddressItems(list: List<AddressItemEntity>) {
        addressDao.insertAddressItems(list)
    }

    suspend fun deleteAllAddressItems() {
        addressDao.deleteAllAddressItems()
    }

    suspend fun updatePrintStatus(id: Long, isPrinted: Boolean) {
        val printDate = if (isPrinted) {
            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
        } else {
            null
        }
        agreementDao.updatePrintStatus(id, isPrinted, printDate)
    }

    suspend fun dispatchToBranch(id: Long, branch: String) {
        val dispatchDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
        agreementDao.dispatchToBranch(id, branch.trim(), dispatchDate, "পাঠানো হয়েছে")
    }

    suspend fun updateBranchVerification(id: Long, status: String, notes: String = "") {
        val verificationDate = if (status == "যাচাই সম্পন্ন" || status == "প্রত্যাখ্যাত") {
            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
        } else {
            null
        }
        agreementDao.updateBranchVerification(id, status, verificationDate, notes)
    }

    private suspend fun saveLearnedAddresses(
        district: String,
        upazila: String,
        postOffice: String,
        village: String
    ) {
        if (district.isNotBlank() && upazila.isNotBlank()) {
            if (postOffice.isNotBlank()) {
                addressDao.insertAddressItem(
                    AddressItemEntity(
                        type = "POST_OFFICE",
                        district = district.trim(),
                        upazila = upazila.trim(),
                        name = postOffice.trim()
                    )
                )
            }
            if (village.isNotBlank()) {
                addressDao.insertAddressItem(
                    AddressItemEntity(
                        type = "VILLAGE",
                        district = district.trim(),
                        upazila = upazila.trim(),
                        name = village.trim()
                    )
                )
            }
        }
    }

    fun getPostOffices(district: String, upazila: String): Flow<List<String>> {
        return addressDao.getAddressNames("POST_OFFICE", district.trim(), upazila.trim())
    }

    fun getVillages(district: String, upazila: String): Flow<List<String>> {
        return addressDao.getAddressNames("VILLAGE", district.trim(), upazila.trim())
    }
}
