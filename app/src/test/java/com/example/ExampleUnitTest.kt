package com.example

import com.example.data.AgreementEntity
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun testAgreementEditNamingConvention() {
        val baseAgreement = AgreementEntity(
            serialNo = "RRF-20260324-001",
            submissionDate = "24/03/2026",
            employeeName = "করিম আহমেদ",
            employeeFatherName = "রহিম আহমেদ",
            designation = "সার্ভিস স্টাফ",
            employeeDistrict = "যশোর",
            employeeUpazila = "যশোর সদর",
            employeePostOffice = "যশোর",
            employeeVillage = "চাঁচড়া",
            guarantorName = "জামাল হোসেন",
            guarantorFatherName = "কামাল হোসেন",
            guarantorMotherName = "মরিয়ম বেগম",
            guarantorRelationship = "ভাই",
            guarantorNid = "19901234567890",
            guarantorDistrict = "যশোর",
            guarantorUpazila = "যশোর সদর",
            guarantorPostOffice = "যশোর",
            guarantorVillage = "চাঁচড়া",
            editCount = 0
        )

        // 0 edits (Initial submission): No edit suffix
        assertEquals("করিম আহমেদ", baseAgreement.baseFileName)
        assertEquals("", baseAgreement.editLabel)

        // 1st edit: File name has "_এডিট", label is "এডিট"
        val edit1 = baseAgreement.copy(editCount = 1)
        assertEquals("করিম আহমেদ_এডিট", edit1.baseFileName)
        assertEquals("এডিট", edit1.editLabel)

        // 2nd edit: File name has "_এডিট ১", label is "এডিট ১"
        val edit2 = baseAgreement.copy(editCount = 2)
        assertEquals("করিম আহমেদ_এডিট ১", edit2.baseFileName)
        assertEquals("এডিট ১", edit2.editLabel)

        // 3rd edit: File name has "_এডিট ২", label is "এডিট ২"
        val edit3 = baseAgreement.copy(editCount = 3)
        assertEquals("করিম আহমেদ_এডিট ২", edit3.baseFileName)
        assertEquals("এডিট ২", edit3.editLabel)
    }
}

