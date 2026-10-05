package com.example.util

import android.content.Context
import com.example.data.AgreementEntity
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * BranchReportExporter
 * Generates CSV / Excel report files for Branch Dispatch & Verification statistics.
 */
object BranchReportExporter {

    data class BranchSummary(
        val branchName: String,
        val totalDispatched: Int,
        val verifiedCount: Int,
        val pendingCount: Int,
        val rejectedCount: Int,
        val dispatchDates: String,
        val verificationDates: String
    )

    fun calculateBranchSummaries(agreements: List<AgreementEntity>): List<BranchSummary> {
        val branchMap = agreements
            .filter { it.assignedBranch.isNotBlank() }
            .groupBy { it.assignedBranch.trim() }

        val list = mutableListOf<BranchSummary>()

        for ((branch, items) in branchMap) {
            val total = items.size
            val verified = items.count { it.verificationStatus == "যাচাই সম্পন্ন" }
            val rejected = items.count { it.verificationStatus == "প্রত্যাখ্যাত" }
            val pending = total - verified - rejected

            val dDates = items.mapNotNull { it.dispatchDate }.distinct().joinToString(", ")
            val vDates = items.mapNotNull { it.verificationDate }.distinct().joinToString(", ")

            list.add(
                BranchSummary(
                    branchName = branch,
                    totalDispatched = total,
                    verifiedCount = verified,
                    pendingCount = pending,
                    rejectedCount = rejected,
                    dispatchDates = dDates.ifBlank { "-" },
                    verificationDates = vDates.ifBlank { "-" }
                )
            )
        }

        return list.sortedByDescending { it.totalDispatched }
    }

    /**
     * Exports Branch Verification Summary to CSV Excel file with UTF-8 BOM.
     */
    fun exportBranchReportToCsv(context: Context, agreements: List<AgreementEntity>): File {
        val summaries = calculateBranchSummaries(agreements)
        val fileDateSuffix = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val fileName = "RRF_Branch_Verification_Report_$fileDateSuffix.csv"

        val dir = File(context.cacheDir, "reports")
        if (!dir.exists()) dir.mkdirs()

        val file = File(dir, fileName)

        FileOutputStream(file).use { fos ->
            // UTF-8 BOM for Excel Bangla encoding
            fos.write(byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()))

            val sb = StringBuilder()
            sb.append("RRF HR - শাখা ভিত্তিক ফরম প্রেরন ও তথ্য যাচাইকরণ প্রতিবেদন\n")
            sb.append("প্রতিবেদন তৈরির সময়: ${SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date())}\n\n")

            // Table 1: Branch Summary
            sb.append("শাখার নাম,মোট পাঠানো ফরম,তথ্য যাচাই সম্পন্ন,যাচাই অপেক্ষমান,প্রত্যাখ্যাত,প্রেরণের তারিখসমূহ,যাচাইয়ের তারিখসমূহ\n")
            for (s in summaries) {
                sb.append("\"${s.branchName}\",${s.totalDispatched},${s.verifiedCount},${s.pendingCount},${s.rejectedCount},\"${s.dispatchDates}\",\"${s.verificationDates}\"\n")
            }

            sb.append("\n\n")
            sb.append("বিস্তারিত কর্মী তালিকা (শাখা ভিত্তিক):\n")
            sb.append("ক্রমিক,সিরিয়াল নং,কর্মীর নাম,পদবী,জেলা,শাখা অফিস,প্রেরণের তারিখ,যাচাই স্ট্যাটাস,যাচাইয়ের তারিখ,শাখা মন্তব্য\n")

            val dispatchedItems = agreements.filter { it.assignedBranch.isNotBlank() }.sortedBy { it.assignedBranch }
            var index = 1
            for (item in dispatchedItems) {
                sb.append("$index,\"${item.serialNo}\",\"${item.employeeName}\",\"${item.designation}\",\"${item.employeeDistrict}\",\"${item.assignedBranch}\",\"${item.dispatchDate ?: ""}\",\"${item.verificationStatus}\",\"${item.verificationDate ?: ""}\",\"${item.branchNotes}\"\n")
                index++
            }

            fos.write(sb.toString().toByteArray(Charsets.UTF_8))
        }

        try {
            ShareHelper.saveFileToDownloads(context, file, fileName)
        } catch (_: Exception) {}

        return file
    }
}
