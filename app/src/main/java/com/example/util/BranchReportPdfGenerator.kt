package com.example.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import com.example.data.AgreementEntity
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * BranchReportPdfGenerator
 * Generates a clean PDF document report of Branch Assignments, Verification Statuses, and Daily Totals.
 */
object BranchReportPdfGenerator {

    private const val PAGE_WIDTH = 595 // A4 Width in points (72 dpi)
    private const val PAGE_HEIGHT = 842 // A4 Height in points

    fun generateBranchReportPdf(context: Context, agreements: List<AgreementEntity>): File {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val page = document.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        val titlePaint = TextPaint().apply {
            isAntiAlias = true
            color = Color.rgb(5, 150, 105)
            textSize = 16f
            isFakeBoldText = true
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val subTitlePaint = TextPaint().apply {
            isAntiAlias = true
            color = Color.rgb(6, 95, 70)
            textSize = 12f
            isFakeBoldText = true
        }

        val bodyPaint = TextPaint().apply {
            isAntiAlias = true
            color = Color.BLACK
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }

        val headerPaint = TextPaint().apply {
            isAntiAlias = true
            color = Color.WHITE
            textSize = 10f
            isFakeBoldText = true
        }

        val linePaint = Paint().apply {
            color = Color.LTGRAY
            strokeWidth = 1f
            style = Paint.Style.STROKE
        }

        val headerBgPaint = Paint().apply {
            color = Color.rgb(5, 150, 105)
            style = Paint.Style.FILL
        }

        var y = 40f
        val margin = 36f
        val contentWidth = PAGE_WIDTH - (margin * 2)

        // Header
        canvas.drawText("Rural Reconstruction Foundation (RRF)", margin, y, titlePaint)
        y += 20f
        canvas.drawText("শাখা ভিত্তিক এগ্রিমেন্ট বিতরণ ও তথ্য যাচাইকরণ প্রতিবেদন", margin, y, subTitlePaint)
        y += 16f

        val dateStr = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date())
        bodyPaint.color = Color.DKGRAY
        canvas.drawText("প্রতিবেদন তৈরির তারিখ: $dateStr", margin, y, bodyPaint)
        y += 20f

        // Table 1: Branch Verification Summary Header
        val colWidths = floatArrayOf(150f, 85f, 95f, 95f, 98f)
        val headers = arrayOf("শাখার নাম", "পাঠানো ফরম", "যাচাই সম্পন্ন", "অপেক্ষমান", "সর্বশেষ তারিখ")

        canvas.drawRect(margin, y, margin + contentWidth, y + 24f, headerBgPaint)
        var x = margin + 6f
        for (i in headers.indices) {
            canvas.drawText(headers[i], x, y + 16f, headerPaint)
            x += colWidths[i]
        }
        y += 24f

        val summaries = BranchReportExporter.calculateBranchSummaries(agreements)
        bodyPaint.color = Color.BLACK

        if (summaries.isEmpty()) {
            canvas.drawText("কোনো শাখায় এগ্রিমেন্ট ফরম পাঠানো হয়নি।", margin + 10f, y + 18f, bodyPaint)
            y += 30f
        } else {
            for (s in summaries) {
                x = margin + 6f
                canvas.drawRect(margin, y, margin + contentWidth, y + 22f, Paint().apply {
                    color = Color.rgb(245, 247, 246)
                    style = Paint.Style.FILL
                })

                canvas.drawText(s.branchName, x, y + 15f, bodyPaint)
                x += colWidths[0]
                canvas.drawText("${s.totalDispatched} টি", x, y + 15f, bodyPaint)
                x += colWidths[1]
                canvas.drawText("${s.verifiedCount} টি", x, y + 15f, bodyPaint)
                x += colWidths[2]
                canvas.drawText("${s.pendingCount} টি", x, y + 15f, bodyPaint)
                x += colWidths[3]
                canvas.drawText(s.verificationDates.take(12), x, y + 15f, bodyPaint)

                canvas.drawLine(margin, y + 22f, margin + contentWidth, y + 22f, linePaint)
                y += 22f
            }
        }

        y += 20f

        // Section 2: Date-wise Submissions Breakdown
        canvas.drawText("তারিখ ভিত্তিক এগ্রিমেন্ট ফরম পূরণের হিসাব:", margin, y, subTitlePaint)
        y += 18f

        val dateGroups = agreements.groupBy { it.submissionDate }.entries.sortedByDescending { it.key }
        val dateColWidths = floatArrayOf(150f, 120f, 253f)

        canvas.drawRect(margin, y, margin + contentWidth, y + 22f, headerBgPaint)
        x = margin + 6f
        canvas.drawText("তারিখ", x, y + 15f, headerPaint)
        x += dateColWidths[0]
        canvas.drawText("পূরণকৃত ফরম সংখ্যা", x, y + 15f, headerPaint)
        x += dateColWidths[1]
        canvas.drawText("বিবরণ / স্ট্যাটাস", x, y + 15f, headerPaint)
        y += 22f

        for ((subDate, items) in dateGroups.take(10)) {
            x = margin + 6f
            canvas.drawRect(margin, y, margin + contentWidth, y + 20f, Paint().apply {
                color = Color.rgb(250, 250, 250)
                style = Paint.Style.FILL
            })

            canvas.drawText(subDate, x, y + 14f, bodyPaint)
            x += dateColWidths[0]
            canvas.drawText("${items.size} টি ফরম", x, y + 14f, bodyPaint)
            x += dateColWidths[1]
            val printed = items.count { it.isPrinted }
            canvas.drawText("প্রিন্ট সম্পন্ন: $printed, প্রিন্ট বাকী: ${items.size - printed}", x, y + 14f, bodyPaint)

            canvas.drawLine(margin, y + 20f, margin + contentWidth, y + 20f, linePaint)
            y += 20f
        }

        // Bottom Signature Section
        val sigY = PAGE_HEIGHT - 60f
        canvas.drawLine(margin, sigY, margin + 140f, sigY, linePaint)
        canvas.drawText("প্রস্তুতকারী কর্মকর্তা", margin + 10f, sigY + 15f, bodyPaint)

        canvas.drawLine(margin + 200f, sigY, margin + 340f, sigY, linePaint)
        canvas.drawText("শাখা ব্যবস্থাপক", margin + 220f, sigY + 15f, bodyPaint)

        canvas.drawLine(PAGE_WIDTH - margin - 140f, sigY, PAGE_WIDTH - margin, sigY, linePaint)
        canvas.drawText("এইচআর প্রধান / প্রশাসন", PAGE_WIDTH - margin - 130f, sigY + 15f, bodyPaint)

        document.finishPage(page)

        val fileDateSuffix = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val fileName = "RRF_Branch_Verification_Report_$fileDateSuffix.pdf"
        val dir = File(context.cacheDir, "reports")
        if (!dir.exists()) dir.mkdirs()

        val pdfFile = File(dir, fileName)
        FileOutputStream(pdfFile).use { fos ->
            document.writeTo(fos)
        }
        document.close()

        try {
            ShareHelper.saveFileToDownloads(context, pdfFile, fileName)
        } catch (_: Exception) {}

        return pdfFile
    }
}
