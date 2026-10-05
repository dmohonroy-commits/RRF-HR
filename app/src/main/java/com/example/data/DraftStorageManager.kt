package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.ui.AppScreen
import com.example.ui.FormState
import com.example.util.BanglaTextValidator
import org.json.JSONArray
import org.json.JSONObject

object DraftStorageManager {

    private const val PREFS_NAME = "rrf_form_drafts_v1"
    private const val KEY_LAST_ACTIVE_SCREEN = "last_active_form_screen"
    private const val KEY_STAMP_FORM = "draft_stamp_form"
    private const val KEY_ID_CARD_FORM = "draft_id_card_form"
    private const val KEY_TRAINING_FORM = "draft_training_form"
    private const val KEY_RELATIONSHIP_FORM = "draft_relationship_form"
    private const val KEY_NOMINEE_FORM = "draft_nominee_form"
    private const val KEY_PERSONAL_INFO_FORM = "draft_personal_info_form"
    private const val KEY_VERIFICATION_FORM = "draft_verification_form"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun saveLastActiveScreen(context: Context, screen: AppScreen?) {
        val prefs = getPrefs(context)
        if (screen != null && isFormScreen(screen)) {
            prefs.edit().putString(KEY_LAST_ACTIVE_SCREEN, screen.name).apply()
        }
    }

    fun getLastActiveScreen(context: Context): AppScreen? {
        val prefs = getPrefs(context)
        val name = prefs.getString(KEY_LAST_ACTIVE_SCREEN, null) ?: return null
        return try {
            val screen = AppScreen.valueOf(name)
            if (isFormScreen(screen)) screen else null
        } catch (_: Exception) {
            null
        }
    }

    fun isFormScreen(screen: AppScreen): Boolean {
        return screen in listOf(
            AppScreen.WORKER_PANEL,
            AppScreen.FORM_ID_CARD,
            AppScreen.FORM_TRAINING,
            AppScreen.FORM_RELATIONSHIP,
            AppScreen.FORM_NOMINEE,
            AppScreen.FORM_PERSONAL_INFO,
            AppScreen.FORM_VERIFICATION
        )
    }

    fun saveStampFormDraft(context: Context, state: FormState) {
        val json = JSONObject().apply {
            put("employeeName", state.employeeName)
            put("employeeFatherName", state.employeeFatherName)
            put("designation", state.designation)
            put("employeeDistrict", state.employeeDistrict)
            put("employeeUpazila", state.employeeUpazila)
            put("employeePostOffice", state.employeePostOffice)
            put("employeeVillage", state.employeeVillage)
            put("guarantorName", state.guarantorName)
            put("guarantorFatherName", state.guarantorFatherName)
            put("guarantorMotherName", state.guarantorMotherName)
            put("guarantorRelationship", state.guarantorRelationship)
            put("guarantorRelationshipCustom", state.guarantorRelationshipCustom)
            put("guarantorNid", state.guarantorNid)
            put("sameAddress", state.sameAddress)
            put("guarantorDistrict", state.guarantorDistrict)
            put("guarantorUpazila", state.guarantorUpazila)
            put("guarantorPostOffice", state.guarantorPostOffice)
            put("guarantorVillage", state.guarantorVillage)
            put("orgLogoPath", state.orgLogoPath ?: "")
        }
        getPrefs(context).edit().putString(KEY_STAMP_FORM, json.toString()).apply()
    }

    fun loadStampFormDraft(context: Context, defaultState: FormState): FormState {
        val jsonStr = getPrefs(context).getString(KEY_STAMP_FORM, null) ?: return defaultState
        return try {
            val json = JSONObject(jsonStr)
            defaultState.copy(
                employeeName = json.optString("employeeName", defaultState.employeeName),
                employeeFatherName = json.optString("employeeFatherName", defaultState.employeeFatherName),
                designation = json.optString("designation", defaultState.designation),
                employeeDistrict = json.optString("employeeDistrict", defaultState.employeeDistrict),
                employeeUpazila = json.optString("employeeUpazila", defaultState.employeeUpazila),
                employeePostOffice = json.optString("employeePostOffice", defaultState.employeePostOffice),
                employeeVillage = json.optString("employeeVillage", defaultState.employeeVillage),
                guarantorName = json.optString("guarantorName", defaultState.guarantorName),
                guarantorFatherName = json.optString("guarantorFatherName", defaultState.guarantorFatherName),
                guarantorMotherName = json.optString("guarantorMotherName", defaultState.guarantorMotherName),
                guarantorRelationship = json.optString("guarantorRelationship", defaultState.guarantorRelationship),
                guarantorRelationshipCustom = json.optString("guarantorRelationshipCustom", defaultState.guarantorRelationshipCustom),
                guarantorNid = json.optString("guarantorNid", defaultState.guarantorNid),
                sameAddress = json.optBoolean("sameAddress", defaultState.sameAddress),
                guarantorDistrict = json.optString("guarantorDistrict", defaultState.guarantorDistrict),
                guarantorUpazila = json.optString("guarantorUpazila", defaultState.guarantorUpazila),
                guarantorPostOffice = json.optString("guarantorPostOffice", defaultState.guarantorPostOffice),
                guarantorVillage = json.optString("guarantorVillage", defaultState.guarantorVillage),
                orgLogoPath = json.optString("orgLogoPath").ifBlank { defaultState.orgLogoPath }
            )
        } catch (_: Exception) {
            defaultState
        }
    }

    fun saveAllAgreementFormsDraft(context: Context, forms: AllAgreementForms) {
        val prefs = getPrefs(context).edit()
        
        // 1. ID Card
        val idJson = JSONObject().apply {
            put("branchName", forms.idCardForm.branchName)
            put("pinCode", forms.idCardForm.pinCode)
            put("joiningDay", forms.idCardForm.joiningDay)
            put("joiningMonth", forms.idCardForm.joiningMonth)
            put("joiningYear", forms.idCardForm.joiningYear)
            put("fileId", forms.idCardForm.fileId)
            put("employeeName", forms.idCardForm.employeeName)
            put("fatherName", forms.idCardForm.fatherName)
            put("motherName", forms.idCardForm.motherName)
            put("district", forms.idCardForm.district)
            put("thana", forms.idCardForm.thana)
            put("postOffice", forms.idCardForm.postOffice)
            put("village", forms.idCardForm.village)
            put("address", forms.idCardForm.address)
            put("designation", forms.idCardForm.designation)
            put("customDesignation", forms.idCardForm.customDesignation)
            put("bloodGroup", forms.idCardForm.bloodGroup)
            put("mobileNumber", forms.idCardForm.mobileNumber)
            put("emailAddress", forms.idCardForm.emailAddress)
            put("photoUri", forms.idCardForm.photoUri ?: "")
        }
        prefs.putString(KEY_ID_CARD_FORM, idJson.toString())

        // 2. Training
        val trJson = JSONObject().apply {
            put("employeeName", forms.trainingForm.employeeName)
            put("fatherName", forms.trainingForm.fatherName)
            put("motherName", forms.trainingForm.motherName)
            put("permanentAddress", forms.trainingForm.permanentAddress)
            put("village", forms.trainingForm.village)
            put("postOffice", forms.trainingForm.postOffice)
            put("postCode", forms.trainingForm.postCode)
            put("thana", forms.trainingForm.thana)
            put("district", forms.trainingForm.district)
            put("trainingStartDate", forms.trainingForm.trainingStartDate)
            put("trainingEndDate", forms.trainingForm.trainingEndDate)
            put("trainingDurationDays", forms.trainingForm.trainingDurationDays)
            put("trainingFeeAmount", forms.trainingForm.trainingFeeAmount)
            put("declarationDate", forms.trainingForm.declarationDate)
            put("designation", forms.trainingForm.designation)
            put("witness1", forms.trainingForm.witness1)
            put("witness2", forms.trainingForm.witness2)
        }
        prefs.putString(KEY_TRAINING_FORM, trJson.toString())

        // 3. Relationship
        val relJson = JSONObject().apply {
            put("employeeName", forms.relationshipForm.employeeName)
            put("declarationDate", forms.relationshipForm.declarationDate)
            put("hasNoKinship", forms.relationshipForm.hasNoKinship)
            val arr = JSONArray()
            forms.relationshipForm.kinshipList.forEach { k ->
                arr.put(JSONObject().apply {
                    put("nameAndAddress", k.nameAndAddress)
                    put("designationInRrf", k.designationInRrf)
                    put("designationCustom", k.designationCustom)
                    put("relationship", k.relationship)
                    put("relationshipCustom", k.relationshipCustom)
                })
            }
            put("kinshipList", arr)
        }
        prefs.putString(KEY_RELATIONSHIP_FORM, relJson.toString())

        // 4. Nominee
        val nomJson = JSONObject().apply {
            put("employeeName", forms.nomineeForm.employeeName)
            put("designation", forms.nomineeForm.designation)
            put("employeeId", forms.nomineeForm.employeeId)
            put("declarationDate", forms.nomineeForm.declarationDate)
            val nomArr = JSONArray()
            forms.nomineeForm.nominees.forEach { n ->
                nomArr.put(JSONObject().apply {
                    put("nomineeName", n.nomineeName)
                    put("fatherName", n.fatherName)
                    put("motherName", n.motherName)
                    put("village", n.village)
                    put("postOffice", n.postOffice)
                    put("thana", n.thana)
                    put("district", n.district)
                    put("customRelationship", n.customRelationship)
                    put("nomineeNameAndParents", n.nomineeNameAndParents)
                    put("fullAddress", n.fullAddress)
                    put("nidNo", n.nidNo)
                    put("relationship", n.relationship)
                    put("percentOfShares", n.percentOfShares)
                    put("photoUri", n.photoUri ?: "")
                })
            }
            put("nominees", nomArr)
        }
        prefs.putString(KEY_NOMINEE_FORM, nomJson.toString())

        // 5. Personal Info
        val p = forms.personalInfoForm
        val persJson = JSONObject().apply {
            put("employeeNameBangla", p.employeeNameBangla)
            put("joinedUnit", p.joinedUnit)
            put("employeeNameEnglish", p.employeeNameEnglish)
            put("gender", p.gender)
            put("employeeNid", p.employeeNid)
            put("fatherName", p.fatherName)
            put("motherName", p.motherName)
            put("fatherNid", p.fatherNid)
            put("motherNid", p.motherNid)
            put("guarantorNid", p.guarantorNid)
            put("isMarried", p.isMarried)
            put("spouseName", p.spouseName)
            put("permVillage", p.permVillage)
            put("permPostOffice", p.permPostOffice)
            put("permPostCode", p.permPostCode)
            put("permUpazila", p.permUpazila)
            put("permThana", p.permThana)
            put("permDistrict", p.permDistrict)
            put("currVillageOrHouse", p.currVillageOrHouse)
            put("currRoadNo", p.currRoadNo)
            put("currPostOffice", p.currPostOffice)
            put("currPostCode", p.currPostCode)
            put("currUpazila", p.currUpazila)
            put("currThana", p.currThana)
            put("currDistrict", p.currDistrict)
            put("mobileNumber", p.mobileNumber)
            put("alternateNumber", p.alternateNumber)
            put("dateOfBirth", p.dateOfBirth)
            put("ageYears", p.ageYears)
            put("ageMonths", p.ageMonths)
            put("ageDays", p.ageDays)
            put("drivingLicenseNo", p.drivingLicenseNo)
            put("passportNo", p.passportNo)
            put("hasPoliceCase", p.hasPoliceCase)
            put("policeCaseCount", p.policeCaseCount)
            put("policeCaseDetails", p.policeCaseDetails)
            put("bankAccountNo", p.bankAccountNo)
            put("bankAccountType", p.bankAccountType)
            put("bankName", p.bankName)
            put("branchName", p.branchName)
            put("monthlyIncome", p.monthlyIncome)
            put("monthlyIncomeWords", p.monthlyIncomeWords)
            put("yearlyIncome", p.yearlyIncome)
            put("yearlyIncomeWords", p.yearlyIncomeWords)
            put("tinNumber", p.tinNumber)
            put("guardianMonthlyIncome", p.guardianMonthlyIncome)
            put("guardianMonthlyIncomeWords", p.guardianMonthlyIncomeWords)
            put("guardianYearlyIncome", p.guardianYearlyIncome)
            put("guardianYearlyIncomeWords", p.guardianYearlyIncomeWords)
            put("propertyDetails", p.propertyDetails)
            put("hasNoBankOrPropertyInfo", p.hasNoBankOrPropertyInfo)
            put("guarantorName", p.guarantorName)
            put("guarantorFatherName", p.guarantorFatherName)
            put("guarantorMotherName", p.guarantorMotherName)
            put("guarantorOccupation", p.guarantorOccupation)
            put("guarantorRelationship", p.guarantorRelationship)
            put("guarantorMobile", p.guarantorMobile)
            put("guarantorAge", p.guarantorAge)
            put("guarantorEducation", p.guarantorEducation)
            put("guarantorSameAddress", p.guarantorSameAddress)
            put("guarantorVillage", p.guarantorVillage)
            put("guarantorPlotRoad", p.guarantorPlotRoad)
            put("guarantorPostOffice", p.guarantorPostOffice)
            put("guarantorPostCode", p.guarantorPostCode)
            put("guarantorThana", p.guarantorThana)
            put("guarantorUpazila", p.guarantorUpazila)
            put("guarantorDistrict", p.guarantorDistrict)
            put("chairmanName", p.chairmanName)
            put("unionOrPouroshovaName", p.unionOrPouroshovaName)
            put("chairmanAddress", p.chairmanAddress)
            put("chairmanVillage", p.chairmanVillage)
            put("chairmanPostOffice", p.chairmanPostOffice)
            put("chairmanThana", p.chairmanThana)
            put("chairmanDistrict", p.chairmanDistrict)
            put("nearUnitOfficeName", p.nearUnitOfficeName)
            put("familyMemberInUnitOffice", p.familyMemberInUnitOffice)
            put("hasNoExperience", p.hasNoExperience)
            put("hasNoTraining", p.hasNoTraining)
            put("totalExperience", p.totalExperience)

            val eduArr = JSONArray()
            p.educations.forEach { e ->
                eduArr.put(JSONObject().apply {
                    put("degreeName", e.degreeName)
                    put("instituteName", e.instituteName)
                    put("subjectGroup", e.subjectGroup)
                    put("marksOrCgpa", e.marksOrCgpa)
                    put("result", e.result)
                    put("boardOrUniversity", e.boardOrUniversity)
                    put("passingYear", e.passingYear)
                })
            }
            put("educations", eduArr)

            val expArr = JSONArray()
            p.experiences.forEach { exp ->
                expArr.put(JSONObject().apply {
                    put("organizationName", exp.organizationName)
                    put("designation", exp.designation)
                    put("periodFromTo", exp.periodFromTo)
                    put("duration", exp.duration)
                    put("duties", exp.duties)
                })
            }
            put("experiences", expArr)

            val trArr = JSONArray()
            p.trainings.forEach { tr ->
                trArr.put(JSONObject().apply {
                    put("organizationName", tr.organizationName)
                    put("trainingTopic", tr.trainingTopic)
                    put("period", tr.period)
                    put("duration", tr.duration)
                })
            }
            put("trainings", trArr)

            val famArr = JSONArray()
            p.familyMembers.forEach { fm ->
                famArr.put(JSONObject().apply {
                    put("name", fm.name)
                    put("relationship", fm.relationship)
                    put("education", fm.education)
                    put("occupation", fm.occupation)
                    put("mobileNumber", fm.mobileNumber)
                    put("age", fm.age)
                    put("remarks", fm.remarks)
                })
            }
            put("familyMembers", famArr)
        }
        prefs.putString(KEY_PERSONAL_INFO_FORM, persJson.toString())

        // 6. Verification
        val v = forms.verificationForm
        val verJson = JSONObject().apply {
            put("rel1Name", v.rel1Name)
            put("rel1Father", v.rel1Father)
            put("rel1Relationship", v.rel1Relationship)
            put("rel1Pin", v.rel1Pin)
            put("rel1Designation", v.rel1Designation)
            put("rel1Workplace", v.rel1Workplace)
            put("rel1Comment", v.rel1Comment)
            put("rel2Name", v.rel2Name)
            put("rel2Father", v.rel2Father)
            put("rel2Relationship", v.rel2Relationship)
            put("rel2Pin", v.rel2Pin)
            put("rel2Designation", v.rel2Designation)
            put("rel2Workplace", v.rel2Workplace)
            put("rel2Comment", v.rel2Comment)
            put("chairmanName", v.chairmanName)
            put("unionName", v.unionName)
            put("chairmanAddress", v.chairmanAddress)
            put("chairmanVillage", v.chairmanVillage)
            put("chairmanPostOffice", v.chairmanPostOffice)
            put("chairmanThana", v.chairmanThana)
            put("chairmanDistrict", v.chairmanDistrict)
            put("staffFullName", v.staffFullName)
            put("staffFatherName", v.staffFatherName)
            put("staffSignatureDate", v.staffSignatureDate)
            put("isNeighborAddressSameAsWorker", v.isNeighborAddressSameAsWorker)
            put("isNeigh1AddressSameAsWorker", v.isNeigh1AddressSameAsWorker)
            put("isNeigh2AddressSameAsWorker", v.isNeigh2AddressSameAsWorker)
            put("neigh1Name", v.neigh1Name)
            put("neigh1Profession", v.neigh1Profession)
            put("neigh1Father", v.neigh1Father)
            put("neigh1Mobile", v.neigh1Mobile)
            put("neigh1Village", v.neigh1Village)
            put("neigh1PostOffice", v.neigh1PostOffice)
            put("neigh1Postal", v.neigh1Postal)
            put("neigh1Upazila", v.neigh1Upazila)
            put("neigh1Police", v.neigh1Police)
            put("neigh1District", v.neigh1District)
            put("neigh1Comment", v.neigh1Comment)
            put("neigh2Name", v.neigh2Name)
            put("neigh2Profession", v.neigh2Profession)
            put("neigh2Father", v.neigh2Father)
            put("neigh2Mobile", v.neigh2Mobile)
            put("neigh2Village", v.neigh2Village)
            put("neigh2PostOffice", v.neigh2PostOffice)
            put("neigh2Postal", v.neigh2Postal)
            put("neigh2Upazila", v.neigh2Upazila)
            put("neigh2Police", v.neigh2Police)
            put("neigh2District", v.neigh2District)
            put("neigh2Comment", v.neigh2Comment)
            put("nearbyUnitOffice", v.nearbyUnitOffice)
            put("familyMemberInUnitOffice", v.familyMemberInUnitOffice)
            put("investigationOfficerComment", v.investigationOfficerComment)
            put("investigatingOfficerName", v.investigatingOfficerName)
            put("investigatingOfficerDate", v.investigatingOfficerDate)
            put("verifyingOfficerName", v.verifyingOfficerName)
            put("verifyingOfficerDate", v.verifyingOfficerDate)
            put("approvingAuthorityName", v.approvingAuthorityName)
            put("approvingAuthorityDate", v.approvingAuthorityDate)
        }
        prefs.putString(KEY_VERIFICATION_FORM, verJson.toString())

        prefs.apply()
    }

    fun loadAllAgreementFormsDraft(context: Context, defaultForms: AllAgreementForms): AllAgreementForms {
        val prefs = getPrefs(context)
        
        var idCard = defaultForms.idCardForm
        prefs.getString(KEY_ID_CARD_FORM, null)?.let {
            try {
                val j = JSONObject(it)
                idCard = idCard.copy(
                    branchName = j.optString("branchName", idCard.branchName),
                    pinCode = j.optString("pinCode", idCard.pinCode),
                    joiningDay = j.optString("joiningDay", idCard.joiningDay),
                    joiningMonth = j.optString("joiningMonth", idCard.joiningMonth),
                    joiningYear = j.optString("joiningYear", idCard.joiningYear),
                    fileId = j.optString("fileId", idCard.fileId),
                    employeeName = j.optString("employeeName", idCard.employeeName),
                    fatherName = j.optString("fatherName", idCard.fatherName),
                    motherName = j.optString("motherName", idCard.motherName),
                    district = j.optString("district", idCard.district),
                    thana = j.optString("thana", idCard.thana),
                    postOffice = j.optString("postOffice", idCard.postOffice),
                    village = j.optString("village", idCard.village),
                    address = j.optString("address", idCard.address),
                    designation = j.optString("designation", idCard.designation),
                    customDesignation = j.optString("customDesignation", idCard.customDesignation),
                    bloodGroup = j.optString("bloodGroup", idCard.bloodGroup),
                    mobileNumber = j.optString("mobileNumber", idCard.mobileNumber),
                    emailAddress = j.optString("emailAddress", idCard.emailAddress),
                    photoUri = j.optString("photoUri").ifBlank { idCard.photoUri }
                )
            } catch (_: Exception) {}
        }

        var training = defaultForms.trainingForm
        prefs.getString(KEY_TRAINING_FORM, null)?.let {
            try {
                val j = JSONObject(it)
                training = training.copy(
                    employeeName = j.optString("employeeName", training.employeeName),
                    fatherName = j.optString("fatherName", training.fatherName),
                    motherName = j.optString("motherName", training.motherName),
                    permanentAddress = j.optString("permanentAddress", training.permanentAddress),
                    village = j.optString("village", training.village),
                    postOffice = j.optString("postOffice", training.postOffice),
                    postCode = j.optString("postCode", training.postCode),
                    thana = j.optString("thana", training.thana),
                    district = j.optString("district", training.district),
                    trainingStartDate = j.optString("trainingStartDate", training.trainingStartDate),
                    trainingEndDate = j.optString("trainingEndDate", training.trainingEndDate),
                    trainingDurationDays = j.optString("trainingDurationDays", training.trainingDurationDays),
                    trainingFeeAmount = j.optString("trainingFeeAmount", training.trainingFeeAmount),
                    declarationDate = j.optString("declarationDate", training.declarationDate),
                    designation = j.optString("designation", training.designation),
                    witness1 = j.optString("witness1", training.witness1),
                    witness2 = j.optString("witness2", training.witness2)
                )
            } catch (_: Exception) {}
        }

        var relationship = defaultForms.relationshipForm
        prefs.getString(KEY_RELATIONSHIP_FORM, null)?.let {
            try {
                val j = JSONObject(it)
                val list = mutableListOf<KinshipPerson>()
                val arr = j.optJSONArray("kinshipList")
                if (arr != null) {
                    for (i in 0 until arr.length()) {
                        val item = arr.getJSONObject(i)
                        list.add(KinshipPerson(
                            nameAndAddress = item.optString("nameAndAddress", item.optString("name")),
                            designationInRrf = item.optString("designationInRrf", item.optString("designation")),
                            designationCustom = item.optString("designationCustom"),
                            relationship = item.optString("relationship"),
                            relationshipCustom = item.optString("relationshipCustom")
                        ))
                    }
                }
                relationship = relationship.copy(
                    employeeName = j.optString("employeeName", relationship.employeeName),
                    declarationDate = j.optString("declarationDate", relationship.declarationDate),
                    hasNoKinship = j.optBoolean("hasNoKinship", j.optBoolean("hasNoRelativeInRrf", relationship.hasNoKinship)),
                    kinshipList = if (list.isNotEmpty()) list else relationship.kinshipList
                )
            } catch (_: Exception) {}
        }

        var nominee = defaultForms.nomineeForm
        prefs.getString(KEY_NOMINEE_FORM, null)?.let {
            try {
                val j = JSONObject(it)
                val nList = mutableListOf<NomineeItem>()
                val arr = j.optJSONArray("nominees")
                if (arr != null) {
                    for (i in 0 until arr.length()) {
                        val item = arr.getJSONObject(i)
                        nList.add(NomineeItem(
                            nomineeName = item.optString("nomineeName"),
                            fatherName = item.optString("fatherName"),
                            motherName = item.optString("motherName"),
                            village = item.optString("village"),
                            postOffice = item.optString("postOffice"),
                            thana = item.optString("thana"),
                            district = item.optString("district"),
                            customRelationship = item.optString("customRelationship"),
                            nomineeNameAndParents = item.optString("nomineeNameAndParents"),
                            fullAddress = item.optString("fullAddress"),
                            nidNo = item.optString("nidNo"),
                            relationship = item.optString("relationship").ifBlank { "Father" },
                            percentOfShares = item.optString("percentOfShares").ifBlank { "100%" },
                            photoUri = item.optString("photoUri").ifBlank { null }
                        ))
                    }
                }
                nominee = nominee.copy(
                    employeeName = j.optString("employeeName", nominee.employeeName),
                    designation = j.optString("designation", nominee.designation),
                    declarationDate = j.optString("declarationDate", nominee.declarationDate),
                    employeeId = j.optString("employeeId", nominee.employeeId),
                    nominees = if (nList.isNotEmpty()) nList else nominee.nominees
                )
            } catch (_: Exception) {}
        }

        var personal = defaultForms.personalInfoForm
        prefs.getString(KEY_PERSONAL_INFO_FORM, null)?.let {
            try {
                val j = JSONObject(it)
                personal = personal.copy(
                    employeeNameBangla = j.optString("employeeNameBangla", personal.employeeNameBangla),
                    joinedUnit = j.optString("joinedUnit", personal.joinedUnit),
                    employeeNameEnglish = j.optString("employeeNameEnglish", personal.employeeNameEnglish),
                    gender = j.optString("gender", personal.gender),
                    employeeNid = j.optString("employeeNid", personal.employeeNid),
                    fatherName = j.optString("fatherName", personal.fatherName).let { if (BanglaTextValidator.isScrambledBangla(it)) "" else it },
                    motherName = j.optString("motherName", personal.motherName).let { if (BanglaTextValidator.isScrambledBangla(it)) "" else it },
                    fatherNid = j.optString("fatherNid", personal.fatherNid),
                    motherNid = j.optString("motherNid", personal.motherNid),
                    guarantorNid = j.optString("guarantorNid", personal.guarantorNid),
                    isMarried = j.optBoolean("isMarried", personal.isMarried || j.optString("spouseName", "").isNotBlank()),
                    spouseName = j.optString("spouseName", personal.spouseName),
                    permVillage = j.optString("permVillage", personal.permVillage),
                    permPostOffice = j.optString("permPostOffice", personal.permPostOffice),
                    permPostCode = j.optString("permPostCode", personal.permPostCode),
                    permUpazila = j.optString("permUpazila", personal.permUpazila),
                    permThana = j.optString("permThana", personal.permThana),
                    permDistrict = j.optString("permDistrict", personal.permDistrict),
                    currVillageOrHouse = j.optString("currVillageOrHouse", personal.currVillageOrHouse),
                    currRoadNo = j.optString("currRoadNo", personal.currRoadNo),
                    currPostOffice = j.optString("currPostOffice", personal.currPostOffice),
                    currPostCode = j.optString("currPostCode", personal.currPostCode),
                    currUpazila = j.optString("currUpazila", personal.currUpazila),
                    currThana = j.optString("currThana", personal.currThana),
                    currDistrict = j.optString("currDistrict", personal.currDistrict),
                    mobileNumber = j.optString("mobileNumber", personal.mobileNumber),
                    alternateNumber = j.optString("alternateNumber", personal.alternateNumber),
                    dateOfBirth = j.optString("dateOfBirth", personal.dateOfBirth),
                    ageYears = j.optString("ageYears", personal.ageYears),
                    ageMonths = j.optString("ageMonths", personal.ageMonths),
                    ageDays = j.optString("ageDays", personal.ageDays),
                    drivingLicenseNo = j.optString("drivingLicenseNo", personal.drivingLicenseNo),
                    passportNo = j.optString("passportNo", personal.passportNo),
                    hasPoliceCase = j.optBoolean("hasPoliceCase", personal.hasPoliceCase),
                    policeCaseCount = j.optString("policeCaseCount", personal.policeCaseCount),
                    policeCaseDetails = j.optString("policeCaseDetails", personal.policeCaseDetails),
                    bankAccountNo = j.optString("bankAccountNo", personal.bankAccountNo),
                    bankAccountType = j.optString("bankAccountType", personal.bankAccountType),
                    bankName = j.optString("bankName", personal.bankName),
                    branchName = j.optString("branchName", personal.branchName),
                    monthlyIncome = j.optString("monthlyIncome", personal.monthlyIncome),
                    monthlyIncomeWords = j.optString("monthlyIncomeWords", personal.monthlyIncomeWords),
                    yearlyIncome = j.optString("yearlyIncome", personal.yearlyIncome),
                    yearlyIncomeWords = j.optString("yearlyIncomeWords", personal.yearlyIncomeWords),
                    tinNumber = j.optString("tinNumber", personal.tinNumber),
                    guardianMonthlyIncome = j.optString("guardianMonthlyIncome", personal.guardianMonthlyIncome),
                    guardianMonthlyIncomeWords = j.optString("guardianMonthlyIncomeWords", personal.guardianMonthlyIncomeWords),
                    guardianYearlyIncome = j.optString("guardianYearlyIncome", personal.guardianYearlyIncome),
                    guardianYearlyIncomeWords = j.optString("guardianYearlyIncomeWords", personal.guardianYearlyIncomeWords),
                    propertyDetails = j.optString("propertyDetails", personal.propertyDetails),
                    hasNoBankOrPropertyInfo = j.optBoolean("hasNoBankOrPropertyInfo", personal.hasNoBankOrPropertyInfo),
                    guarantorName = j.optString("guarantorName", personal.guarantorName),
                    guarantorFatherName = j.optString("guarantorFatherName", personal.guarantorFatherName),
                    guarantorMotherName = j.optString("guarantorMotherName", personal.guarantorMotherName),
                    guarantorOccupation = j.optString("guarantorOccupation", personal.guarantorOccupation),
                    guarantorRelationship = j.optString("guarantorRelationship", personal.guarantorRelationship),
                    guarantorMobile = j.optString("guarantorMobile", personal.guarantorMobile),
                    guarantorAge = j.optString("guarantorAge", personal.guarantorAge),
                    guarantorEducation = j.optString("guarantorEducation", personal.guarantorEducation),
                    guarantorSameAddress = j.optBoolean("guarantorSameAddress", personal.guarantorSameAddress),
                    guarantorVillage = j.optString("guarantorVillage", personal.guarantorVillage),
                    guarantorPlotRoad = j.optString("guarantorPlotRoad", personal.guarantorPlotRoad),
                    guarantorPostOffice = j.optString("guarantorPostOffice", personal.guarantorPostOffice),
                    guarantorPostCode = j.optString("guarantorPostCode", personal.guarantorPostCode),
                    guarantorThana = j.optString("guarantorThana", personal.guarantorThana),
                    guarantorUpazila = j.optString("guarantorUpazila", personal.guarantorUpazila),
                    guarantorDistrict = j.optString("guarantorDistrict", personal.guarantorDistrict),
                    chairmanName = j.optString("chairmanName", personal.chairmanName),
                    unionOrPouroshovaName = j.optString("unionOrPouroshovaName", personal.unionOrPouroshovaName),
                    chairmanAddress = j.optString("chairmanAddress", personal.chairmanAddress),
                    chairmanVillage = j.optString("chairmanVillage", personal.chairmanVillage),
                    chairmanPostOffice = j.optString("chairmanPostOffice", personal.chairmanPostOffice),
                    chairmanThana = j.optString("chairmanThana", personal.chairmanThana),
                    chairmanDistrict = j.optString("chairmanDistrict", personal.chairmanDistrict),
                    nearUnitOfficeName = j.optString("nearUnitOfficeName", personal.nearUnitOfficeName),
                    familyMemberInUnitOffice = j.optString("familyMemberInUnitOffice", personal.familyMemberInUnitOffice),
                    hasNoExperience = j.optBoolean("hasNoExperience", personal.hasNoExperience),
                    hasNoTraining = j.optBoolean("hasNoTraining", personal.hasNoTraining),
                    totalExperience = j.optString("totalExperience", personal.totalExperience)
                )

                j.optJSONArray("educations")?.let { arr ->
                    val list = mutableListOf<EducationRow>()
                    for (k in 0 until arr.length()) {
                        val obj = arr.optJSONObject(k) ?: continue
                        list.add(EducationRow(
                            degreeName = obj.optString("degreeName"),
                            instituteName = obj.optString("instituteName"),
                            subjectGroup = obj.optString("subjectGroup"),
                            marksOrCgpa = obj.optString("marksOrCgpa"),
                            result = obj.optString("result"),
                            boardOrUniversity = obj.optString("boardOrUniversity"),
                            passingYear = obj.optString("passingYear")
                        ))
                    }
                    if (list.isNotEmpty()) personal = personal.copy(educations = list)
                }

                j.optJSONArray("experiences")?.let { arr ->
                    val list = mutableListOf<ExperienceRow>()
                    for (k in 0 until arr.length()) {
                        val obj = arr.optJSONObject(k) ?: continue
                        list.add(ExperienceRow(
                            organizationName = obj.optString("organizationName"),
                            designation = obj.optString("designation"),
                            periodFromTo = obj.optString("periodFromTo"),
                            duration = obj.optString("duration"),
                            duties = obj.optString("duties")
                        ))
                    }
                    if (list.isNotEmpty()) personal = personal.copy(experiences = list)
                }

                j.optJSONArray("trainings")?.let { arr ->
                    val list = mutableListOf<TrainingHistoryRow>()
                    for (k in 0 until arr.length()) {
                        val obj = arr.optJSONObject(k) ?: continue
                        list.add(TrainingHistoryRow(
                            organizationName = obj.optString("organizationName"),
                            trainingTopic = obj.optString("trainingTopic"),
                            period = obj.optString("period"),
                            duration = obj.optString("duration")
                        ))
                    }
                    if (list.isNotEmpty()) personal = personal.copy(trainings = list)
                }

                j.optJSONArray("familyMembers")?.let { arr ->
                    val list = mutableListOf<FamilyMemberRow>()
                    for (k in 0 until arr.length()) {
                        val obj = arr.optJSONObject(k) ?: continue
                        list.add(FamilyMemberRow(
                            name = obj.optString("name"),
                            relationship = obj.optString("relationship"),
                            education = obj.optString("education"),
                            occupation = obj.optString("occupation"),
                            mobileNumber = obj.optString("mobileNumber"),
                            age = obj.optString("age"),
                            remarks = obj.optString("remarks")
                        ))
                    }
                    if (list.isNotEmpty()) personal = personal.copy(familyMembers = list)
                }
            } catch (_: Exception) {}
        }

        var verification = defaultForms.verificationForm
        prefs.getString(KEY_VERIFICATION_FORM, null)?.let {
            try {
                val j = JSONObject(it)
                verification = verification.copy(
                    rel1Name = j.optString("rel1Name", verification.rel1Name),
                    rel1Father = j.optString("rel1Father", verification.rel1Father),
                    rel1Relationship = j.optString("rel1Relationship", verification.rel1Relationship),
                    rel1Pin = j.optString("rel1Pin", verification.rel1Pin),
                    rel1Designation = j.optString("rel1Designation", verification.rel1Designation),
                    rel1Workplace = j.optString("rel1Workplace", verification.rel1Workplace),
                    rel1Comment = j.optString("rel1Comment", verification.rel1Comment),
                    rel2Name = j.optString("rel2Name", verification.rel2Name),
                    rel2Father = j.optString("rel2Father", verification.rel2Father),
                    rel2Relationship = j.optString("rel2Relationship", verification.rel2Relationship),
                    rel2Pin = j.optString("rel2Pin", verification.rel2Pin),
                    rel2Designation = j.optString("rel2Designation", verification.rel2Designation),
                    rel2Workplace = j.optString("rel2Workplace", verification.rel2Workplace),
                    rel2Comment = j.optString("rel2Comment", verification.rel2Comment),
                    chairmanName = j.optString("chairmanName", verification.chairmanName),
                    unionName = j.optString("unionName", verification.unionName),
                    chairmanAddress = j.optString("chairmanAddress", verification.chairmanAddress),
                    chairmanVillage = j.optString("chairmanVillage", verification.chairmanVillage),
                    chairmanPostOffice = j.optString("chairmanPostOffice", verification.chairmanPostOffice),
                    chairmanThana = j.optString("chairmanThana", verification.chairmanThana),
                    chairmanDistrict = j.optString("chairmanDistrict", verification.chairmanDistrict),
                    staffFullName = j.optString("staffFullName", verification.staffFullName),
                    staffFatherName = j.optString("staffFatherName", verification.staffFatherName),
                    staffSignatureDate = j.optString("staffSignatureDate", verification.staffSignatureDate),
                    isNeighborAddressSameAsWorker = j.optBoolean("isNeighborAddressSameAsWorker", verification.isNeighborAddressSameAsWorker),
                    isNeigh1AddressSameAsWorker = j.optBoolean("isNeigh1AddressSameAsWorker", verification.isNeigh1AddressSameAsWorker),
                    isNeigh2AddressSameAsWorker = j.optBoolean("isNeigh2AddressSameAsWorker", verification.isNeigh2AddressSameAsWorker),
                    neigh1Name = j.optString("neigh1Name", verification.neigh1Name),
                    neigh1Profession = j.optString("neigh1Profession", verification.neigh1Profession),
                    neigh1Father = j.optString("neigh1Father", verification.neigh1Father),
                    neigh1Mobile = j.optString("neigh1Mobile", verification.neigh1Mobile),
                    neigh1Village = j.optString("neigh1Village", verification.neigh1Village),
                    neigh1PostOffice = j.optString("neigh1PostOffice", verification.neigh1PostOffice),
                    neigh1Postal = j.optString("neigh1Postal", verification.neigh1Postal),
                    neigh1Upazila = j.optString("neigh1Upazila", verification.neigh1Upazila),
                    neigh1Police = j.optString("neigh1Police", verification.neigh1Police),
                    neigh1District = j.optString("neigh1District", verification.neigh1District),
                    neigh1Comment = j.optString("neigh1Comment", verification.neigh1Comment),
                    neigh2Name = j.optString("neigh2Name", verification.neigh2Name),
                    neigh2Profession = j.optString("neigh2Profession", verification.neigh2Profession),
                    neigh2Father = j.optString("neigh2Father", verification.neigh2Father),
                    neigh2Mobile = j.optString("neigh2Mobile", verification.neigh2Mobile),
                    neigh2Village = j.optString("neigh2Village", verification.neigh2Village),
                    neigh2PostOffice = j.optString("neigh2PostOffice", verification.neigh2PostOffice),
                    neigh2Postal = j.optString("neigh2Postal", verification.neigh2Postal),
                    neigh2Upazila = j.optString("neigh2Upazila", verification.neigh2Upazila),
                    neigh2Police = j.optString("neigh2Police", verification.neigh2Police),
                    neigh2District = j.optString("neigh2District", verification.neigh2District),
                    neigh2Comment = j.optString("neigh2Comment", verification.neigh2Comment),
                    nearbyUnitOffice = j.optString("nearbyUnitOffice", verification.nearbyUnitOffice),
                    familyMemberInUnitOffice = j.optString("familyMemberInUnitOffice", verification.familyMemberInUnitOffice),
                    investigationOfficerComment = j.optString("investigationOfficerComment", verification.investigationOfficerComment),
                    investigatingOfficerName = j.optString("investigatingOfficerName", verification.investigatingOfficerName),
                    investigatingOfficerDate = j.optString("investigatingOfficerDate", verification.investigatingOfficerDate),
                    verifyingOfficerName = j.optString("verifyingOfficerName", verification.verifyingOfficerName),
                    verifyingOfficerDate = j.optString("verifyingOfficerDate", verification.verifyingOfficerDate),
                    approvingAuthorityName = j.optString("approvingAuthorityName", verification.approvingAuthorityName),
                    approvingAuthorityDate = j.optString("approvingAuthorityDate", verification.approvingAuthorityDate)
                )
            } catch (_: Exception) {}
        }

        return AllAgreementForms(
            idCardForm = idCard,
            trainingForm = training,
            relationshipForm = relationship,
            nomineeForm = nominee,
            personalInfoForm = personal,
            verificationForm = verification
        )
    }

    fun clearAllDraftsAndPdfs(context: Context) {
        try {
            getPrefs(context).edit().clear().apply()
            
            val dirs = listOfNotNull(
                context.cacheDir,
                context.externalCacheDir,
                context.filesDir
            )

            for (dir in dirs) {
                dir.walkTopDown().forEach { file ->
                    if (file.isFile && (
                        file.name.endsWith(".pdf", ignoreCase = true) ||
                        file.name.endsWith(".tmp", ignoreCase = true) ||
                        file.name.contains("RRF", ignoreCase = true) ||
                        file.name.contains("Agreement", ignoreCase = true) ||
                        file.name.contains("Form", ignoreCase = true)
                    )) {
                        file.delete()
                    }
                }
            }
        } catch (_: Exception) {}
    }

    /**
     * Exports all form drafts and active screen states from SharedPreferences into a JSON Object.
     */
    fun exportAllDraftsToJson(context: Context): JSONObject {
        val prefs = getPrefs(context)
        val obj = JSONObject()
        val allEntries = prefs.all
        for ((key, value) in allEntries) {
            when (value) {
                is String -> {
                    val trimmed = value.trim()
                    try {
                        if (trimmed.startsWith("{") && trimmed.endsWith("}")) {
                            obj.put(key, JSONObject(trimmed))
                        } else if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
                            obj.put(key, JSONArray(trimmed))
                        } else {
                            obj.put(key, value)
                        }
                    } catch (_: Exception) {
                        obj.put(key, value)
                    }
                }
                is Boolean -> obj.put(key, value)
                is Int -> obj.put(key, value)
                is Long -> obj.put(key, value)
                is Float -> obj.put(key, value.toDouble())
            }
        }
        return obj
    }

    /**
     * Restores form drafts into SharedPreferences from an imported JSON Object.
     */
    fun restoreDraftsFromJson(context: Context, json: JSONObject) {
        val prefs = getPrefs(context)
        val editor = prefs.edit()
        val keys = json.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            val value = json.opt(key)
            when (value) {
                is JSONObject, is JSONArray -> {
                    editor.putString(key, value.toString())
                }
                is String -> {
                    editor.putString(key, value)
                }
                is Boolean -> {
                    editor.putBoolean(key, value)
                }
                is Int -> {
                    editor.putInt(key, value)
                }
                is Long -> {
                    editor.putLong(key, value)
                }
                is Double -> {
                    editor.putFloat(key, value.toFloat())
                }
            }
        }
        editor.apply()
    }
}
