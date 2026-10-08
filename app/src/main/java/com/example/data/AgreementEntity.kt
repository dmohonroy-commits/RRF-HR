package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "agreements")
data class AgreementEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val serialNo: String,
    val submissionDate: String,
    val submissionTimestamp: Long = System.currentTimeMillis(),
    
    // Employee details
    val employeeName: String,
    val employeeFatherName: String,
    val designation: String,
    val employeeDistrict: String,
    val employeeUpazila: String,
    val employeePostOffice: String,
    val employeeVillage: String,
    
    // Guarantor details
    val guarantorName: String,
    val guarantorFatherName: String,
    val guarantorMotherName: String,
    val guarantorRelationship: String,
    val guarantorRelationshipCustom: String = "",
    val guarantorNid: String,
    val sameAddress: Boolean = false,
    val guarantorDistrict: String,
    val guarantorUpazila: String,
    val guarantorPostOffice: String,
    val guarantorVillage: String,
    
    // Status
    val isPrinted: Boolean = false,
    val printDate: String? = null,
    val isSubmittedByThisDevice: Boolean = true,
    
    // Edit tracking
    val editCount: Int = 0,
    val lastEditDate: String? = null,

    // Organization Logo
    val orgLogoPath: String? = null,

    // Branch assignment & verification tracking
    val assignedBranch: String = "",
    val dispatchDate: String? = null,
    val verificationStatus: String = "অপেক্ষমান", // "অপেক্ষমান", "পাঠানো হয়েছে", "যাচাই সম্পন্ন", "প্রত্যাখ্যাত"
    val verificationDate: String? = null,
    val branchNotes: String = ""
) {
    val effectiveGuarantorRelationship: String
        get() = if (guarantorRelationship == "অন্যান্য" && guarantorRelationshipCustom.isNotBlank()) {
            guarantorRelationshipCustom
        } else {
            guarantorRelationship
        }

    val editLabel: String
        get() = when {
            editCount <= 0 -> ""
            editCount == 1 -> "এডিট"
            else -> "এডিট ${com.example.util.BanglaTextValidator.toBanglaDigits((editCount - 1).toString())}"
        }

    /**
     * Formats filename with employee name and short address (গ্রাম, পোস্ট, থানা, জেলা)
     * Example: "করিম আহমেদ_গ্রাম-চাঁচড়া_পো-যশোর_থানা-যশোর সদর_জেলা-যশোর"
     */
    val formattedFileNameWithAddress: String
        get() {
            val rawName = employeeName.trim()
            val safeName = rawName.replace("[/\\\\:*?\"<>|]".toRegex(), "").trim()
            val baseName = if (safeName.isNotBlank()) safeName else serialNo

            val addrParts = listOf(
                if (employeeVillage.isNotBlank()) "গ্রাম-${employeeVillage.trim()}" else "",
                if (employeePostOffice.isNotBlank()) "পো-${employeePostOffice.trim()}" else "",
                if (employeeUpazila.isNotBlank()) "থানা-${employeeUpazila.trim()}" else "",
                if (employeeDistrict.isNotBlank()) "জেলা-${employeeDistrict.trim()}" else ""
            ).filter { it.isNotBlank() }

            val addrString = if (addrParts.isNotEmpty()) "_" + addrParts.joinToString("_").replace("[/\\\\:*?\"<>|]".toRegex(), "") else ""

            val editSuffix = when {
                editCount <= 0 -> ""
                editCount == 1 -> "_এডিট"
                else -> {
                    val num = com.example.util.BanglaTextValidator.toBanglaDigits((editCount - 1).toString())
                    "_এডিট $num"
                }
            }

            return "${baseName}${addrString}${editSuffix}"
        }

    val baseFileName: String
        get() = formattedFileNameWithAddress

    val fileName: String
        get() = "${baseFileName}_এগ্রিমেন্ট"

    val stamp100FileName: String
        get() = "${baseFileName}_১০০টাকা_স্ট্যাম্প"

    val stamp25FileName: String
        get() = "${baseFileName}_২৫টাকা_প্রত্যয়ন"

    val verificationFileName: String
        get() = "${baseFileName}_তথ্য_যাচাই_ফরম"
}
