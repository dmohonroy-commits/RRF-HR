package com.example.util

import android.content.Context
import com.example.data.AgreementEntity
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ExcelExporter {

    /**
     * Generates a UTF-8 CSV spreadsheet with BOM, perfectly compatible with Microsoft Excel,
     * Google Sheets, and LibreOffice for Bengali script.
     */
    fun exportToExcelCsv(context: Context, agreements: List<AgreementEntity>): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val file = File(context.cacheDir, "RRF_HR_Agreements_$timeStamp.csv")

        val fos = FileOutputStream(file)
        // Write UTF-8 BOM so Excel on Windows recognizes UTF-8 without garbled characters
        fos.write(byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()))

        val writer = OutputStreamWriter(fos, StandardCharsets.UTF_8)

        // CSV Header
        val headers = listOf(
            "ক্রমিক নং",
            "সিরিয়াল নং",
            "আবেদনের তারিখ",
            "কর্মীর নাম",
            "কর্মীর পিতার নাম",
            "পদের নাম",
            "কর্মীর জেলা",
            "কর্মীর উপজেলা",
            "কর্মীর ডাকঘর",
            "কর্মীর গ্রাম",
            "জামিনদারের নাম",
            "জামিনদারের পিতার নাম",
            "জামিনদারের মাতার নাম",
            "সম্পর্ক",
            "জামিনদারের ভোটার আইডি (NID)",
            "জামিনদারের জেলা",
            "জামিনদারের উপজেলা",
            "জামিনদারের ডাকঘর",
            "জামিনদারের গ্রাম",
            "১০০ টাকার স্ট্যাম্পে প্রিন্ট স্ট্যাটাস",
            "প্রিন্টের তারিখ"
        )
        writer.write(headers.joinToString(",") { escapeCsv(it) } + "\n")

        agreements.forEachIndexed { index, item ->
            val row = listOf(
                (index + 1).toString(),
                item.serialNo,
                item.submissionDate,
                item.employeeName,
                item.employeeFatherName,
                item.designation,
                item.employeeDistrict,
                item.employeeUpazila,
                item.employeePostOffice,
                item.employeeVillage,
                item.guarantorName,
                item.guarantorFatherName,
                item.guarantorMotherName,
                item.effectiveGuarantorRelationship,
                BanglaTextValidator.toBanglaDigits(item.guarantorNid),
                item.guarantorDistrict,
                item.guarantorUpazila,
                item.guarantorPostOffice,
                item.guarantorVillage,
                if (item.isPrinted) "প্রিন্ট সম্পন্ন" else "অপেক্ষমান",
                item.printDate ?: "-"
            )
            writer.write(row.joinToString(",") { escapeCsv(it) } + "\n")
        }

        writer.flush()
        writer.close()
        return file
    }

    private fun escapeCsv(value: String): String {
        var escaped = value.replace("\"", "\"\"")
        if (escaped.contains(",") || escaped.contains("\n") || escaped.contains("\"")) {
            escaped = "\"$escaped\""
        }
        return escaped
    }
}
