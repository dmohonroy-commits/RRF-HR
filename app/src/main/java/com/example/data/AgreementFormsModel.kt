package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.ui.AppScreen
import com.example.ui.FormState
import com.example.util.BanglaAddressHelper
import com.example.util.BanglaTextValidator
import com.example.util.BangladeshDistricts
import org.json.JSONArray
import org.json.JSONObject

// =========================================================================
// 1. Staff Information for ID Card (কর্মীর আইডি কার্ডের তথ্য)
// =========================================================================
data class IdCardFormState(
    val branchName: String = "",
    val pinCode: String = "",
    val joiningDay: String = "",
    val joiningMonth: String = "",
    val joiningYear: String = "",
    val fileId: String = "",
    val employeeName: String = "",
    val fatherName: String = "",
    val motherName: String = "",
    val district: String = "",
    val thana: String = "",
    val postOffice: String = "",
    val village: String = "",
    val address: String = "",
    val designation: String = "",
    val customDesignation: String = "",
    val bloodGroup: String = "",
    val mobileNumber: String = "",
    val emailAddress: String = "",
    val photoUri: String? = null
)

// =========================================================================
// 2. Training Undertaking Form (প্রশিক্ষণ তথ্য - অঙ্গীকারনামা)
// =========================================================================
data class TrainingFormState(
    val employeeName: String = "",
    val fatherName: String = "",
    val motherName: String = "",
    val permanentAddress: String = "",
    val village: String = "",
    val postOffice: String = "",
    val postCode: String = "",
    val thana: String = "",
    val district: String = "",
    val trainingStartDate: String = "",
    val trainingEndDate: String = "",
    val trainingDurationDays: String = "",
    val trainingFeeAmount: String = "",
    val declarationDate: String = "",
    val designation: String = "",
    val witness1: String = "",
    val witness2: String = ""
) {
    val effectiveAddress: String
        get() {
            val parts = mutableListOf<String>()
            if (village.isNotBlank()) parts.add("গ্রাম- ${village.trim()}")
            if (postOffice.isNotBlank()) {
                val p = if (postCode.isNotBlank()) "${postOffice.trim()}-${postCode.trim()}" else postOffice.trim()
                parts.add("পোস্ট- $p")
            } else if (postCode.isNotBlank()) {
                parts.add("পোস্ট কোড- ${postCode.trim()}")
            }
            if (thana.isNotBlank()) parts.add("থানা- ${thana.trim()}")
            if (district.isNotBlank()) parts.add("জেলা- ${district.trim()}")
            return if (parts.isNotEmpty()) parts.joinToString(", ") else permanentAddress
        }
}

// =========================================================================
// 3. Relationship Declaration Form (প্রতিষ্ঠানের পরিচিত ব্যক্তির সাথে সম্পর্ক)
// =========================================================================
data class KinshipPerson(
    val nameAndAddress: String = "",
    val designationInRrf: String = "",
    val designationCustom: String = "",
    val relationship: String = "",
    val relationshipCustom: String = ""
) {
    val effectiveDesignation: String
        get() = if (designationInRrf == "Others" && designationCustom.isNotBlank()) designationCustom else designationInRrf

    val effectiveRelationship: String
        get() = if (relationship == "Others" && relationshipCustom.isNotBlank()) relationshipCustom else relationship
}

data class RelationshipFormState(
    val kinshipList: List<KinshipPerson> = listOf(
        KinshipPerson(),
        KinshipPerson(),
        KinshipPerson()
    ),
    val hasNoKinship: Boolean = false,
    val employeeName: String = "",
    val declarationDate: String = ""
)

// =========================================================================
// 4. Nominee Declaration Form (নমিনি তথ্য)
// =========================================================================
data class NomineeItem(
    val nomineeName: String = "",
    val fatherName: String = "",
    val motherName: String = "",
    val village: String = "",
    val postOffice: String = "",
    val thana: String = "",
    val district: String = "",
    val customRelationship: String = "",
    val nomineeNameAndParents: String = "",
    val fullAddress: String = "",
    val nidNo: String = "",
    val relationship: String = "Father",
    val percentOfShares: String = "100%",
    val photoUri: String? = null
) {
    fun getFormattedNameAndParents(): String {
        val baseName = nomineeName.ifBlank { nomineeNameAndParents }
        val parts = mutableListOf<String>()
        if (baseName.isNotBlank()) parts.add(baseName)
        if (fatherName.isNotBlank()) parts.add("Father: $fatherName")
        if (motherName.isNotBlank()) parts.add("Mother: $motherName")
        return parts.joinToString("\n")
    }

    fun getFormattedAddress(): String {
        if (village.isBlank() && postOffice.isBlank() && thana.isBlank() && district.isBlank()) {
            return fullAddress
        }
        val parts = mutableListOf<String>()
        if (village.isNotBlank()) parts.add("VILLAGE- $village")
        if (postOffice.isNotBlank()) parts.add("POST- $postOffice")
        if (thana.isNotBlank()) parts.add("THANA- $thana")
        if (district.isNotBlank()) parts.add("DISTRICT- $district")
        return parts.joinToString("\n")
    }

    fun getEffectiveRelationship(): String {
        return if (relationship.equals("Others", ignoreCase = true) || relationship.equals("Other", ignoreCase = true) || relationship.equals("অন্যান্য", ignoreCase = true)) {
            customRelationship.ifBlank { "Others" }
        } else {
            relationship
        }
    }

    val isFilled: Boolean
        get() = (nomineeName.isNotBlank() || nomineeNameAndParents.isNotBlank())
}

data class NomineeFormState(
    val declarationDate: String = "",
    val nominees: List<NomineeItem> = listOf(NomineeItem()),
    val employeeName: String = "",
    val designation: String = "",
    val employeeId: String = ""
)

// =========================================================================
// 5. Personal Information Search Form (কর্মীর ব্যক্তিগত তথ্যাবলী - তথ্যানুসন্ধান ফরম)
// =========================================================================
data class EducationRow(
    val degreeName: String = "",
    val instituteName: String = "",
    val subjectGroup: String = "",
    val marksOrCgpa: String = "",
    val result: String = "",
    val boardOrUniversity: String = "",
    val passingYear: String = ""
)

data class ExperienceRow(
    val organizationName: String = "",
    val designation: String = "",
    val periodFromTo: String = "",
    val duration: String = "",
    val duties: String = ""
)

data class TrainingHistoryRow(
    val organizationName: String = "",
    val trainingTopic: String = "",
    val period: String = "",
    val duration: String = ""
)

data class FamilyMemberRow(
    val name: String = "",
    val relationship: String = "",
    val education: String = "",
    val occupation: String = "",
    val mobileNumber: String = "",
    val age: String = "",
    val remarks: String = ""
)

data class KnownRrfPerson(
    val name: String = "",
    val fatherName: String = "",
    val relationship: String = "",
    val pin: String = "",
    val designation: String = "",
    val currentWorkplace: String = ""
)

data class NeighborInfo(
    val name: String = "",
    val occupation: String = "",
    val fatherName: String = "",
    val mobileNumber: String = "",
    val village: String = "",
    val postOffice: String = "",
    val postCode: String = "",
    val upazila: String = "",
    val thana: String = "",
    val district: String = ""
)

data class PersonalInfoFormState(
    // Page 1: ব্যক্তিগত তথ্যাবলী
    val employeeNameBangla: String = "",
    val joinedUnit: String = "",
    val employeeNameEnglish: String = "",
    val gender: String = "",
    val employeeNid: String = "",
    val fatherName: String = "",
    val motherName: String = "",
    val fatherNid: String = "",
    val motherNid: String = "",
    val guarantorNid: String = "",
    val isMarried: Boolean = false,
    val spouseName: String = "",
    
    // Permanent Address
    val permVillage: String = "",
    val permPostOffice: String = "",
    val permPostCode: String = "",
    val permUpazila: String = "",
    val permThana: String = "",
    val permDistrict: String = "",
    
    // Current Address
    val currVillageOrHouse: String = "",
    val currRoadNo: String = "",
    val currPostOffice: String = "",
    val currPostCode: String = "",
    val currUpazila: String = "",
    val currThana: String = "",
    val currDistrict: String = "",
    
    val mobileNumber: String = "",
    val alternateNumber: String = "",
    val dateOfBirth: String = "",
    val ageYears: String = "",
    val ageMonths: String = "",
    val ageDays: String = "",
    val drivingLicenseNo: String = "",
    val passportNo: String = "",
    val hasPoliceCase: Boolean = false,
    val policeCaseCount: String = "",
    val policeCaseDetails: String = "",
    val bankAccountNo: String = "",
    val bankAccountType: String = "",
    val bankName: String = "",
    val branchName: String = "",
    val monthlyIncome: String = "",
    val monthlyIncomeWords: String = "",
    val yearlyIncome: String = "",
    val yearlyIncomeWords: String = "",
    val tinNumber: String = "",
    val guardianMonthlyIncome: String = "",
    val guardianMonthlyIncomeWords: String = "",
    val guardianYearlyIncome: String = "",
    val guardianYearlyIncomeWords: String = "",
    val propertyDetails: String = "",
    val hasNoBankOrPropertyInfo: Boolean = false,
    
    // Page 2: শিক্ষাগত ও অভিজ্ঞতা
    val educations: List<EducationRow> = listOf(
        EducationRow(degreeName = "এসএসসি / সমমান"),
        EducationRow(degreeName = "এইচএসসি / সমমান"),
        EducationRow(degreeName = "স্নাতক / ডিগ্রি"),
        EducationRow(degreeName = "মাস্টার্স / স্নাতকোত্তর")
    ),
    val hasNoExperience: Boolean = false,
    val experiences: List<ExperienceRow> = listOf(
        ExperienceRow(),
        ExperienceRow()
    ),
    val totalExperience: String = "",
    val hasNoTraining: Boolean = false,
    val trainings: List<TrainingHistoryRow> = listOf(
        TrainingHistoryRow(),
        TrainingHistoryRow()
    ),
    
    // Page 3: পরিবার ও অঙ্গীকারনামা
    val familyMembers: List<FamilyMemberRow> = listOf(
        FamilyMemberRow(),
        FamilyMemberRow(),
        FamilyMemberRow(),
        FamilyMemberRow()
    ),
    val guarantorName: String = "",
    val guarantorFatherName: String = "",
    val guarantorMotherName: String = "",
    val guarantorOccupation: String = "",
    val guarantorRelationship: String = "",
    val guarantorMobile: String = "",
    val guarantorAge: String = "",
    val guarantorEducation: String = "",
    val guarantorSameAddress: Boolean = false,
    val guarantorVillage: String = "",
    val guarantorPlotRoad: String = "",
    val guarantorPostOffice: String = "",
    val guarantorPostCode: String = "",
    val guarantorThana: String = "",
    val guarantorUpazila: String = "",
    val guarantorDistrict: String = "",
    
    // Page 4: পরিচিত ব্যক্তি, চেয়ারম্যান ও মুচলেকা
    val knownPerson1: KnownRrfPerson = KnownRrfPerson(),
    val knownPerson2: KnownRrfPerson = KnownRrfPerson(),
    val chairmanName: String = "",
    val unionOrPouroshovaName: String = "",
    val chairmanAddress: String = "",
    val chairmanVillage: String = "",
    val chairmanPostOffice: String = "",
    val chairmanThana: String = "",
    val chairmanDistrict: String = "",
    
    // Page 5: প্রতিবেশী ও অফিস
    val neighbor1: NeighborInfo = NeighborInfo(),
    val neighbor2: NeighborInfo = NeighborInfo(),
    val nearUnitOfficeName: String = "",
    val familyMemberInUnitOffice: String = ""
)

data class VerificationFormState(
    val noRrfRelative: Boolean = false,
    val rel1Name: String = "",
    val rel1Father: String = "",
    val rel1Relationship: String = "",
    val rel1RelationshipCustom: String = "",
    val rel1Pin: String = "",
    val rel1Designation: String = "",
    val rel1DesignationCustom: String = "",
    val rel1Workplace: String = "",
    val rel1Comment: String = "",

    val rel2Name: String = "",
    val rel2Father: String = "",
    val rel2Relationship: String = "",
    val rel2RelationshipCustom: String = "",
    val rel2Pin: String = "",
    val rel2Designation: String = "",
    val rel2DesignationCustom: String = "",
    val rel2Workplace: String = "",
    val rel2Comment: String = "",

    val chairmanName: String = "",
    val unionName: String = "",
    val chairmanAddress: String = "",
    val chairmanVillage: String = "",
    val chairmanPostOffice: String = "",
    val chairmanThana: String = "",
    val chairmanDistrict: String = "",

    val staffFullName: String = "",
    val staffFatherName: String = "",
    val staffSignatureDate: String = "",

    val isNeighborAddressSameAsWorker: Boolean = false,
    val isNeighbor1SameAsWorker: Boolean = false,
    val isNeighbor2SameAsWorker: Boolean = false,
    val isNeigh1AddressSameAsWorker: Boolean = false,
    val isNeigh2AddressSameAsWorker: Boolean = false,
    val neigh1Name: String = "",
    val neigh1Profession: String = "",
    val neigh1Father: String = "",
    val neigh1Mobile: String = "",
    val neigh1Village: String = "",
    val neigh1PostOffice: String = "",
    val neigh1Postal: String = "",
    val neigh1Upazila: String = "",
    val neigh1Police: String = "",
    val neigh1District: String = "",
    val neigh1Nid: String = "",
    val neigh1Comment: String = "",

    val neigh2Name: String = "",
    val neigh2Profession: String = "",
    val neigh2Father: String = "",
    val neigh2Mobile: String = "",
    val neigh2Village: String = "",
    val neigh2PostOffice: String = "",
    val neigh2Postal: String = "",
    val neigh2Upazila: String = "",
    val neigh2Police: String = "",
    val neigh2District: String = "",
    val neigh2Nid: String = "",
    val neigh2Comment: String = "",

    val nearbyUnitOffice: String = "",
    val familyMemberInUnitOffice: String = "",
    val investigationOfficerComment: String = "",

    val investigatingOfficerName: String = "",
    val investigatingOfficerDate: String = "",
    val verifyingOfficerName: String = "",
    val verifyingOfficerDate: String = "",
    val approvingAuthorityName: String = "",
    val approvingAuthorityDate: String = ""
) {
    val effectiveRel1Designation: String
        get() = if ((rel1Designation == "Others" || rel1Designation == "অন্যান্য") && rel1DesignationCustom.isNotBlank()) rel1DesignationCustom else rel1Designation

    val effectiveRel2Designation: String
        get() = if ((rel2Designation == "Others" || rel2Designation == "অন্যান্য") && rel2DesignationCustom.isNotBlank()) rel2DesignationCustom else rel2Designation

    val effectiveRel1Relationship: String
        get() = if ((rel1Relationship == "Others" || rel1Relationship == "অন্যান্য") && rel1RelationshipCustom.isNotBlank()) rel1RelationshipCustom else rel1Relationship

    val effectiveRel2Relationship: String
        get() = if ((rel2Relationship == "Others" || rel2Relationship == "অন্যান্য") && rel2RelationshipCustom.isNotBlank()) rel2RelationshipCustom else rel2Relationship
}

// =========================================================================
// Aggregated Forms Container & Auto-Fill Service
// =========================================================================
data class AllAgreementForms(
    val idCardForm: IdCardFormState = IdCardFormState(),
    val trainingForm: TrainingFormState = TrainingFormState(),
    val relationshipForm: RelationshipFormState = RelationshipFormState(),
    val nomineeForm: NomineeFormState = NomineeFormState(),
    val personalInfoForm: PersonalInfoFormState = PersonalInfoFormState(),
    val verificationForm: VerificationFormState = VerificationFormState()
)

data class FormStatusInfo(
    val id: Int,
    val serialNumber: String,
    val title: String,
    val screen: AppScreen,
    val isCompleted: Boolean,
    val missingFields: List<String>
)

fun AllAgreementForms.getFormStatuses(): List<FormStatusInfo> {
    return listOf(
        FormStatusInfo(
            id = 1,
            serialNumber = "১",
            title = "কর্মীর আইডি কার্ডের তথ্য",
            screen = AppScreen.FORM_ID_CARD,
            isCompleted = idCardForm.employeeName.isNotBlank() && idCardForm.fatherName.isNotBlank() && idCardForm.bloodGroup.isNotBlank(),
            missingFields = buildList {
                if (idCardForm.employeeName.isBlank()) add("কর্মীর নাম")
                if (idCardForm.fatherName.isBlank()) add("পিতার নাম")
                if (idCardForm.bloodGroup.isBlank()) add("রক্তের গ্রুপ")
            }
        ),
        FormStatusInfo(
            id = 2,
            serialNumber = "২",
            title = "প্রশিক্ষণ সনদপত্রের তথ্য",
            screen = AppScreen.FORM_TRAINING,
            isCompleted = trainingForm.employeeName.isNotBlank() && trainingForm.fatherName.isNotBlank() && trainingForm.permanentAddress.isNotBlank(),
            missingFields = buildList {
                if (trainingForm.employeeName.isBlank()) add("কর্মীর নাম")
                if (trainingForm.fatherName.isBlank()) add("পিতার নাম")
                if (trainingForm.permanentAddress.isBlank()) add("স্থায়ী ঠিকানা")
            }
        ),
        FormStatusInfo(
            id = 3,
            serialNumber = "৩",
            title = "পরিচিত ব্যক্তির সম্পর্ক প্রত্যয়ন",
            screen = AppScreen.FORM_RELATIONSHIP,
            isCompleted = relationshipForm.employeeName.isNotBlank() && (relationshipForm.hasNoKinship || relationshipForm.kinshipList.any { it.nameAndAddress.isNotBlank() }),
            missingFields = buildList {
                if (relationshipForm.employeeName.isBlank()) add("কর্মীর নাম")
                if (!relationshipForm.hasNoKinship && relationshipForm.kinshipList.none { it.nameAndAddress.isNotBlank() }) add("পরিচিত ব্যক্তির বিবরণ অথবা 'কোন আত্মীয় নেই' ঘোষণা")
            }
        ),
        FormStatusInfo(
            id = 4,
            serialNumber = "৪",
            title = "নমিনি সংক্রান্ত তথ্য",
            screen = AppScreen.FORM_NOMINEE,
            isCompleted = nomineeForm.employeeName.isNotBlank() && nomineeForm.nominees.isNotEmpty() && nomineeForm.nominees.first().isFilled,
            missingFields = buildList {
                if (nomineeForm.employeeName.isBlank()) add("কর্মীর নাম")
                if (nomineeForm.nominees.isEmpty() || !nomineeForm.nominees.first().isFilled) add("নমিনির বিবরণ")
            }
        ),
        FormStatusInfo(
            id = 5,
            serialNumber = "৫",
            title = "কর্মীর তথ্যানুসন্ধান ফরম (৩ পাতা)",
            screen = AppScreen.FORM_PERSONAL_INFO,
            isCompleted = personalInfoForm.employeeNameBangla.isNotBlank() && personalInfoForm.fatherName.isNotBlank() && personalInfoForm.mobileNumber.isNotBlank(),
            missingFields = buildList {
                if (personalInfoForm.employeeNameBangla.isBlank()) add("কর্মীর পূর্ণ নাম (বাংলা)")
                if (personalInfoForm.fatherName.isBlank()) add("পিতার নাম")
                if (personalInfoForm.mobileNumber.isBlank()) add("মোবাইল নম্বর")
            }
        ),
        FormStatusInfo(
            id = 6,
            serialNumber = "৬",
            title = "তথ্য যাচাই ফরম (২ পাতা)",
            screen = AppScreen.FORM_VERIFICATION,
            isCompleted = verificationForm.staffFullName.isNotBlank() && (verificationForm.chairmanName.isNotBlank() || verificationForm.rel1Name.isNotBlank()),
            missingFields = buildList {
                if (verificationForm.staffFullName.isBlank()) add("মুচলেকায় কর্মীর নাম")
                if (verificationForm.chairmanName.isBlank() && verificationForm.rel1Name.isBlank()) add("চেয়ারম্যান অথবা পরিচিত ব্যক্তির তথ্য")
            }
        )
    )
}

object AgreementDataAutoFiller {

    fun populateFromStamp(
        current: AllAgreementForms,
        employeeName: String,
        employeeFatherName: String,
        designation: String,
        village: String,
        postOffice: String,
        upazila: String,
        district: String,
        guarantorName: String,
        guarantorFatherName: String,
        guarantorMotherName: String,
        guarantorNid: String,
        guarantorRelationship: String,
        guarantorVillage: String,
        guarantorPostOffice: String,
        guarantorUpazila: String,
        guarantorDistrict: String
    ): AllAgreementForms {
        // Resolve most complete Bangla employee name and father name across all Bangla forms
        val resolvedEmpName = employeeName.ifBlank {
            current.personalInfoForm.employeeNameBangla.ifBlank {
                current.trainingForm.employeeName.ifBlank {
                    current.verificationForm.staffFullName.ifBlank {
                        current.nomineeForm.employeeName.ifBlank {
                            current.relationshipForm.employeeName
                        }
                    }
                }
            }
        }

        val resolvedFatherName = employeeFatherName.ifBlank {
            current.personalInfoForm.fatherName.ifBlank {
                current.trainingForm.fatherName.ifBlank {
                    current.verificationForm.staffFatherName
                }
            }
        }

        // Resolve English name separately for English ID Card and English Personal Info
        val resolvedEnglishName = current.personalInfoForm.employeeNameEnglish.ifBlank {
            current.idCardForm.employeeName.ifBlank {
                if (resolvedEmpName.isNotBlank() && !BanglaTextValidator.containsBengali(resolvedEmpName)) resolvedEmpName
                else if (resolvedEmpName.isNotBlank()) BanglaAddressHelper.transliterateBanglaToEnglish(resolvedEmpName)
                else ""
            }
        }

        val resolvedVillage = village.ifBlank { current.personalInfoForm.permVillage }
        val resolvedPost = postOffice.ifBlank { current.personalInfoForm.permPostOffice }
        val resolvedUpazila = upazila.ifBlank { current.personalInfoForm.permUpazila }
        val resolvedDistrict = district.ifBlank { current.personalInfoForm.permDistrict }

        val resolvedGuarantorName = guarantorName.ifBlank { current.personalInfoForm.guarantorName }
        val resolvedGuarantorFather = guarantorFatherName.ifBlank { current.personalInfoForm.guarantorFatherName }
        val resolvedGuarantorMother = guarantorMotherName.ifBlank { current.personalInfoForm.guarantorMotherName }
        val resolvedGuarantorRel = guarantorRelationship.ifBlank { current.personalInfoForm.guarantorRelationship }
        val resolvedGuarantorNid = if (resolvedGuarantorRel.isNotBlank()) guarantorNid else ""
        val resolvedGuarantorVill = guarantorVillage.ifBlank { current.personalInfoForm.guarantorVillage }
        val resolvedGuarantorPost = guarantorPostOffice.ifBlank { current.personalInfoForm.guarantorPostOffice }
        val resolvedGuarantorUpazila = guarantorUpazila.ifBlank { current.personalInfoForm.guarantorUpazila }
        val resolvedGuarantorDist = guarantorDistrict.ifBlank { current.personalInfoForm.guarantorDistrict }

        val motherCandidatesList = listOf(
            current.personalInfoForm.motherName,
            current.trainingForm.motherName,
            current.nomineeForm.nominees.firstOrNull { BanglaTextValidator.isValidBanglaName(it.motherName) }?.motherName ?: "",
            if (resolvedGuarantorRel == "মাতা") resolvedGuarantorName else ""
        ).filter { it.isNotBlank() && BanglaTextValidator.isValidBanglaName(it) }

        val resolvedMotherName = motherCandidatesList.maxByOrNull { it.length } ?: ""

        val resolvedEmployeeNid = current.personalInfoForm.employeeNid

        val resolvedPostCode = current.trainingForm.postCode
        val resolvedGuarantorPostCode = current.personalInfoForm.guarantorPostCode

        val fullWorkerAddress = BanglaAddressHelper.formatBanglaAddress(
            village = resolvedVillage,
            postOffice = resolvedPost,
            thanaOrUpazila = resolvedUpazila,
            district = resolvedDistrict
        )

        val fullEnglishAddress = BanglaAddressHelper.formatEnglishAddress(
            village = resolvedVillage,
            postOffice = resolvedPost,
            thanaOrUpazila = resolvedUpazila,
            district = resolvedDistrict
        )

        val fullGuarantorAddress = BanglaAddressHelper.formatBanglaAddress(
            village = resolvedGuarantorVill,
            postOffice = resolvedGuarantorPost,
            thanaOrUpazila = resolvedGuarantorUpazila,
            district = resolvedGuarantorDist
        )

        val resolvedFatherEnglishName = current.idCardForm.fatherName.ifBlank {
            if (resolvedFatherName.isNotBlank()) BanglaAddressHelper.transliterateBanglaToEnglish(resolvedFatherName) else ""
        }

        val resolvedDesigEnglish = current.idCardForm.designation.ifBlank {
            if (designation.isNotBlank()) BanglaAddressHelper.transliterateBanglaToEnglish(designation) else ""
        }

        val mappedDesigEng = when (designation) {
            "অফিসার (অ্যাকাউন্টস)", "Officer (Accounts)" -> "Officer (Accounts)"
            "অফিসার (ঋণ)", "অফিসার (লোন)", "Officer (loan)", "Officer (Loan)" -> "Officer (loan)"
            "সার্ভিস স্টাফ", "Service Staff" -> "Service Staff"
            else -> designation
        }

        // 1. ID Card Form Auto-fill (English format: Preserve verbatim user-entered English address)
        val updatedIdCard = current.idCardForm.copy(
            employeeName = if (current.idCardForm.employeeName.isNotBlank()) current.idCardForm.employeeName else if (resolvedEnglishName.isNotBlank()) resolvedEnglishName else if (resolvedEmpName.isNotBlank()) BanglaAddressHelper.transliterateBanglaToEnglish(resolvedEmpName) else current.idCardForm.employeeName,
            fatherName = if (current.idCardForm.fatherName.isNotBlank()) current.idCardForm.fatherName else if (resolvedFatherEnglishName.isNotBlank()) resolvedFatherEnglishName else current.idCardForm.fatherName,
            district = if (current.idCardForm.district.isNotBlank()) current.idCardForm.district else if (resolvedDistrict.isNotBlank()) BanglaAddressHelper.getEnglishDistrict(resolvedDistrict) else current.idCardForm.district,
            thana = if (current.idCardForm.thana.isNotBlank()) current.idCardForm.thana else current.idCardForm.thana,
            postOffice = if (current.idCardForm.postOffice.isNotBlank()) current.idCardForm.postOffice else current.idCardForm.postOffice,
            village = if (current.idCardForm.village.isNotBlank()) current.idCardForm.village else current.idCardForm.village,
            address = if (current.idCardForm.address.isNotBlank()) current.idCardForm.address else if (fullEnglishAddress.isNotBlank()) fullEnglishAddress else current.idCardForm.address,
            designation = current.idCardForm.designation,
            mobileNumber = if (current.idCardForm.mobileNumber.isNotBlank()) current.idCardForm.mobileNumber else if (current.personalInfoForm.mobileNumber.isNotBlank()) current.personalInfoForm.mobileNumber else current.idCardForm.mobileNumber
        )

        // 2. Training Form - User enters directly in TrainingFormScreen with date auto-sync
        val todayBanglaDate = BanglaTextValidator.toBanglaDigits(java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.US).format(java.util.Date()))
        val updatedTraining = current.trainingForm.copy(
            declarationDate = if (current.trainingForm.declarationDate.isBlank()) todayBanglaDate else current.trainingForm.declarationDate
        )

        // 3. Relationship Form Auto-fill (English format)
        val updatedRel = current.relationshipForm.copy(
            employeeName = if (resolvedEnglishName.isNotBlank()) resolvedEnglishName else if (resolvedEmpName.isNotBlank()) BanglaAddressHelper.transliterateBanglaToEnglish(resolvedEmpName) else current.relationshipForm.employeeName
        )

        // 4. Nominee Form Auto-fill (English format)
        val nomineeNameEng = if (current.personalInfoForm.guarantorName.isNotBlank()) {
            current.personalInfoForm.guarantorName
        } else if (guarantorName.isNotBlank()) {
            guarantorName
        } else ""

        val nomineeFatherEng = if (current.personalInfoForm.guarantorFatherName.isNotBlank()) {
            current.personalInfoForm.guarantorFatherName
        } else if (guarantorFatherName.isNotBlank()) {
            guarantorFatherName
        } else ""

        val nomineeParentsEng = if (nomineeFatherEng.isNotBlank()) {
            val engFather = if (BanglaTextValidator.containsBengali(nomineeFatherEng)) BanglaAddressHelper.transliterateBanglaToEnglish(nomineeFatherEng) else nomineeFatherEng
            "FATHER- $engFather"
        } else ""

        val nomineeNameFullEng = if (nomineeNameEng.isNotBlank()) {
            val engName = if (BanglaTextValidator.containsBengali(nomineeNameEng)) BanglaAddressHelper.transliterateBanglaToEnglish(nomineeNameEng) else nomineeNameEng
            if (nomineeParentsEng.isNotBlank()) "$engName ($nomineeParentsEng)" else engName
        } else ""

        val nomineeRelEng = when (resolvedGuarantorRel) {
            "পিতা" -> "FATHER"
            "মাতা" -> "MOTHER"
            "স্ত্রী" -> "WIFE"
            "স্বামী" -> "HUSBAND"
            "ভাই" -> "BROTHER"
            "অন্যান্য" -> "OTHER"
            else -> if (resolvedGuarantorRel.isNotBlank()) {
                if (BanglaTextValidator.containsBengali(resolvedGuarantorRel)) BanglaAddressHelper.transliterateBanglaToEnglish(resolvedGuarantorRel) else resolvedGuarantorRel
            } else ""
        }

        val defaultNominee = NomineeItem(
            nomineeNameAndParents = nomineeNameFullEng,
            fullAddress = if (fullEnglishAddress.isNotBlank()) fullEnglishAddress else fullGuarantorAddress,
            nidNo = resolvedGuarantorNid,
            relationship = nomineeRelEng,
            percentOfShares = "100%"
        )
        val updatedNominee = current.nomineeForm.copy(
            employeeName = if (resolvedEnglishName.isNotBlank()) resolvedEnglishName else if (resolvedEmpName.isNotBlank()) BanglaAddressHelper.transliterateBanglaToEnglish(resolvedEmpName) else current.nomineeForm.employeeName,
            designation = mappedDesigEng,
            declarationDate = if (current.nomineeForm.declarationDate.isBlank()) {
                java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).format(java.util.Date())
            } else current.nomineeForm.declarationDate,
            nominees = current.nomineeForm.nominees
        )

        // 5. Personal Info Form (Family members and guarantor monthly income are manual, never auto-filled)
        val existingFam = current.personalInfoForm.familyMembers.toMutableList()
        while (existingFam.size < 4) {
            existingFam.add(FamilyMemberRow())
        }

        val resolvedGuardianMonthly = current.personalInfoForm.guardianMonthlyIncome
        val resolvedGuardianMonthlyWords = current.personalInfoForm.guardianMonthlyIncomeWords
        val resolvedGuardianYearly = current.personalInfoForm.guardianYearlyIncome
        val resolvedGuardianYearlyWords = current.personalInfoForm.guardianYearlyIncomeWords

        val updatedPersonal = current.personalInfoForm.copy(
            employeeNameBangla = if (resolvedEmpName.isNotBlank()) resolvedEmpName else current.personalInfoForm.employeeNameBangla,
            employeeNameEnglish = if (resolvedEnglishName.isNotBlank()) resolvedEnglishName else current.personalInfoForm.employeeNameEnglish,
            fatherName = if (resolvedFatherName.isNotBlank()) resolvedFatherName else current.personalInfoForm.fatherName,
            motherName = if (resolvedMotherName.isNotBlank()) resolvedMotherName else if (BanglaTextValidator.isValidBanglaName(current.personalInfoForm.motherName)) current.personalInfoForm.motherName else "",
            employeeNid = if (resolvedEmployeeNid.isNotBlank()) resolvedEmployeeNid else current.personalInfoForm.employeeNid,
            familyMembers = existingFam,
            guardianMonthlyIncome = resolvedGuardianMonthly,
            guardianMonthlyIncomeWords = resolvedGuardianMonthlyWords,
            guardianYearlyIncome = resolvedGuardianYearly,
            guardianYearlyIncomeWords = resolvedGuardianYearlyWords,
            permVillage = if (resolvedVillage.isNotBlank()) resolvedVillage else current.personalInfoForm.permVillage,
            permPostOffice = if (resolvedPost.isNotBlank()) resolvedPost else current.personalInfoForm.permPostOffice,
            permPostCode = if (current.personalInfoForm.permPostCode.length >= 4) BanglaTextValidator.toBanglaDigits(current.personalInfoForm.permPostCode) else resolvedPostCode,
            permUpazila = if (resolvedUpazila.isNotBlank()) resolvedUpazila else current.personalInfoForm.permUpazila,
            permThana = if (resolvedUpazila.isNotBlank()) resolvedUpazila else current.personalInfoForm.permThana,
            permDistrict = if (resolvedDistrict.isNotBlank()) resolvedDistrict else current.personalInfoForm.permDistrict,
            currVillageOrHouse = current.personalInfoForm.currVillageOrHouse,
            currPostOffice = current.personalInfoForm.currPostOffice,
            currPostCode = current.personalInfoForm.currPostCode,
            currUpazila = current.personalInfoForm.currUpazila,
            currThana = current.personalInfoForm.currThana,
            currDistrict = current.personalInfoForm.currDistrict,
            mobileNumber = if (current.personalInfoForm.mobileNumber.isBlank() && current.idCardForm.mobileNumber.isNotBlank()) current.idCardForm.mobileNumber else current.personalInfoForm.mobileNumber,
            
            // Guarantor details: personal details filled manually by user; address synced only if guarantorSameAddress checkbox is ticked
            guarantorVillage = if (current.personalInfoForm.guarantorSameAddress && current.personalInfoForm.guarantorVillage.isBlank()) resolvedVillage else current.personalInfoForm.guarantorVillage,
            guarantorPostOffice = if (current.personalInfoForm.guarantorSameAddress && current.personalInfoForm.guarantorPostOffice.isBlank()) resolvedPost else current.personalInfoForm.guarantorPostOffice,
            guarantorPostCode = if (current.personalInfoForm.guarantorSameAddress && current.personalInfoForm.guarantorPostCode.isBlank()) resolvedPostCode else current.personalInfoForm.guarantorPostCode,
            guarantorUpazila = if (current.personalInfoForm.guarantorSameAddress && current.personalInfoForm.guarantorUpazila.isBlank()) resolvedUpazila else current.personalInfoForm.guarantorUpazila,
            guarantorThana = if (current.personalInfoForm.guarantorSameAddress && current.personalInfoForm.guarantorThana.isBlank()) resolvedUpazila else current.personalInfoForm.guarantorThana,
            guarantorDistrict = if (current.personalInfoForm.guarantorSameAddress && current.personalInfoForm.guarantorDistrict.isBlank()) resolvedDistrict else current.personalInfoForm.guarantorDistrict
        )

        // 6. Verification Form Auto-fill (Staff full name and father name in Bangla - "মুচলেকায় কর্মীর নাম সঠিক ভাবে পূর্ণ নাম বসবে")
        val resolvedStaffBangla = current.personalInfoForm.employeeNameBangla.ifBlank {
            resolvedEmpName.ifBlank {
                current.verificationForm.staffFullName
            }
        }
        val resolvedStaffFather = current.personalInfoForm.fatherName.ifBlank {
            resolvedFatherName.ifBlank {
                current.verificationForm.staffFatherName
            }
        }

        val updatedVerification = current.verificationForm.copy(
            staffFullName = resolvedStaffBangla,
            staffFatherName = resolvedStaffFather
        )

        return AllAgreementForms(
            idCardForm = updatedIdCard,
            trainingForm = updatedTraining,
            relationshipForm = updatedRel,
            nomineeForm = updatedNominee,
            personalInfoForm = updatedPersonal,
            verificationForm = updatedVerification
        )
    }
}
