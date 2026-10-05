package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.util.BanglaTextValidator
import com.example.util.BangladeshDistricts
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("RRF HR", appName)
    }

    @Test
    fun `bangla text validator filters english and keeps bangla`() {
        val input = "Mr রহিম 123"
        val filtered = BanglaTextValidator.filterBanglaText(input)
        assertEquals("রহিম", filtered.trim())
    }

    @Test
    fun `bangla digits conversion works`() {
        val input = "01733073076"
        val converted = BanglaTextValidator.toBanglaDigits(input)
        assertEquals("০১৭৩৩০৭৩০৭৬", converted)
    }

    @Test
    fun `district list contains 64 districts`() {
        assertTrue(BangladeshDistricts.districts.size >= 64)
        assertTrue(BangladeshDistricts.districts.contains("যশোর"))
    }

    @Test
    fun `json backup parsing and data validation works`() {
        val validJson = """
            {
              "metadata": {
                "appName": "RRF HR Management",
                "schemaVersion": 1,
                "exportDate": "04/10/2026 15:30:00",
                "exportTimestamp": 1728056700000,
                "totalAgreements": 1,
                "totalAddressItems": 1,
                "includesDrafts": true
              },
              "agreements": [
                {
                  "id": 101,
                  "serialNo": "RRF-2026-0042",
                  "submissionDate": "04/10/2026",
                  "submissionTimestamp": 1728056700000,
                  "employeeName": "মোঃ রফিকুল ইসলাম",
                  "employeeFatherName": "আব্দুল মজিদ",
                  "designation": "অফিসার (ঋণ)",
                  "employeeDistrict": "যশোর",
                  "employeeUpazila": "যশোর সদর",
                  "employeePostOffice": "যশোর প্রধান ডাকঘর",
                  "employeeVillage": "উপশহর",
                  "guarantorName": "সালমা খাতুন",
                  "guarantorFatherName": "মফিজুল হক",
                  "guarantorMotherName": "আনোয়ারা বেগম",
                  "guarantorRelationship": "মা",
                  "guarantorRelationshipCustom": "",
                  "guarantorNid": "19851234567890",
                  "sameAddress": false,
                  "guarantorDistrict": "যশোর",
                  "guarantorUpazila": "যশোর সদর",
                  "guarantorPostOffice": "যশোর প্রধান ডাকঘর",
                  "guarantorVillage": "ঘোপ",
                  "isPrinted": true,
                  "printDate": "04/10/2026",
                  "isSubmittedByThisDevice": true,
                  "editCount": 2,
                  "lastEditDate": "04/10/2026"
                }
              ],
              "addressItems": [
                {
                  "type": "POST_OFFICE",
                  "district": "যশোর",
                  "upazila": "যশোর সদর",
                  "name": "যশোর প্রধান ডাকঘর"
                }
              ],
              "drafts": {
                "draft_stamp_form": {
                  "employeeName": "মোঃ রফিকুল ইসলাম"
                }
              }
            }
        """.trimIndent()

        val summary = com.example.util.JsonDataBackupManager.parseBackupString(validJson)
        assertTrue(summary.isValid)
        assertEquals("RRF HR Management", summary.appName)
        assertEquals("04/10/2026 15:30:00", summary.exportDate)
        assertEquals(1, summary.agreementsCount)
        assertEquals(1, summary.addressItemsCount)
        assertTrue(summary.hasDrafts)

        val item = summary.agreements[0]
        assertEquals("RRF-2026-0042", item.serialNo)
        assertEquals("মোঃ রফিকুল ইসলাম", item.employeeName)
        assertEquals("অফিসার (ঋণ)", item.designation)
        assertEquals("যশোর", item.employeeDistrict)
        assertEquals("সালমা খাতুন", item.guarantorName)
        assertEquals("মা", item.guarantorRelationship)
        assertTrue(item.isPrinted)
        assertEquals(2, item.editCount)

        val addr = summary.addressItems[0]
        assertEquals("POST_OFFICE", addr.type)
        assertEquals("যশোর প্রধান ডাকঘর", addr.name)
    }
}
