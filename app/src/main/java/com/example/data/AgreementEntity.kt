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
    val lastEditDate: String? = null
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

    val baseFileName: String
        get() {
            val rawName = employeeName.trim()
            val safeName = rawName.replace("[/\\\\:*?\"<>|]".toRegex(), "").trim()
            val base = if (safeName.isNotBlank()) safeName else serialNo
            return when {
                editCount <= 0 -> base
                editCount == 1 -> "${base}_এডিট"
                else -> {
                    val num = com.example.util.BanglaTextValidator.toBanglaDigits((editCount - 1).toString())
                    "${base}_এডিট $num"
                }
            }
        }

    val fileName: String
        get() = "${baseFileName}_এগ্রিমেন্ট"
}
