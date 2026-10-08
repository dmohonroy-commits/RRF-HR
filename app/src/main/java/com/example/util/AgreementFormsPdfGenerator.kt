package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import com.example.data.*
import java.io.File
import java.io.FileOutputStream

object AgreementFormsPdfGenerator {

    // A4 Standard Dimensions at 72 dpi: 595 x 842 points
    private const val PAGE_WIDTH = 595
    private const val PAGE_HEIGHT = 842
    private const val MARGIN = 36f // 0.5 inch margin

    // -------------------------------------------------------------------------
    // 0. GENERATE ALL MERGED AGREEMENT FORMS PDF (মার্জ করা সকল এগ্রিমেন্ট ফরম)
    // -------------------------------------------------------------------------
    fun generateAllMergedAgreementFormsPdf(context: Context, allForms: AllAgreementForms): File {
        val pdfDocument = PdfDocument()
        var pageNum = 1

        fun addPage(drawBlock: (Canvas) -> Unit) {
            val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNum++).create()
            val page = pdfDocument.startPage(pageInfo)
            drawBlock(page.canvas)
            pdfDocument.finishPage(page)
        }

        // 1. Staff ID Card
        addPage { drawIdCardPage(it, allForms.idCardForm) }
        // 2. Training Undertaking
        addPage { drawTrainingPage(it, allForms.trainingForm) }
        // 3. Relationship Declaration
        addPage { drawRelationshipPage(it, allForms.relationshipForm) }
        // 4. Nominee Declaration
        addPage { drawNomineePage(it, allForms.nomineeForm) }
        // 5. Personal Info Page 1
        addPage { drawPersonalInfoPage1(it, allForms.personalInfoForm) }
        // 6. Personal Info Page 2
        addPage { drawPersonalInfoPage2(it, allForms.personalInfoForm) }
        // 7. Personal Info Page 3
        addPage { drawPersonalInfoPage3(it, allForms.personalInfoForm) }
        // 8. Verification Form Page 1
        addPage { drawVerificationPage1(it, allForms.verificationForm, allForms) }
        // 9. Verification Form Page 2
        addPage { drawVerificationPage2(it, allForms.verificationForm) }

        val file = File(context.cacheDir, "RRF_All_Agreement_Forms_Merged.pdf")
        if (file.exists()) file.delete()
        FileOutputStream(file).use { fos ->
            pdfDocument.writeTo(fos)
            fos.flush()
        }
        pdfDocument.close()
        return file
    }

    // -------------------------------------------------------------------------
    // 1. GENERATE ID CARD FORM PDF
    // -------------------------------------------------------------------------
    fun generateIdCardFormPdf(context: Context, form: IdCardFormState): File {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        drawIdCardPage(canvas, form)

        pdfDocument.finishPage(page)
        val file = File(context.cacheDir, "Staff_ID_Card_Info.pdf")
        if (file.exists()) file.delete()
        FileOutputStream(file).use { fos ->
            pdfDocument.writeTo(fos)
            fos.flush()
        }
        pdfDocument.close()
        return file
    }

    private fun drawIdCardPage(canvas: Canvas, form: IdCardFormState) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 0.8f
            color = Color.rgb(180, 180, 180)
        }
        
        // Background White
        canvas.drawColor(Color.WHITE)

        val left = 45f
        val rightEdge = 555f

        // Title Header
        paint.color = Color.rgb(20, 20, 20)
        paint.textSize = 16f
        paint.typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("Rural Reconstruction Foundation", (PAGE_WIDTH / 2).toFloat(), 55f, paint)

        paint.textSize = 12f
        paint.typeface = Typeface.create(Typeface.SERIF, Typeface.NORMAL)
        canvas.drawText("Staff Information for ID card", (PAGE_WIDTH / 2).toFloat(), 75f, paint)

        // Stamp Size Photo Box (Top Right)
        val photoW = 68f
        val photoH = 80f
        val photoLeft = rightEdge - photoW
        val photoTop = 42f
        
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 0.8f
        paint.color = Color.rgb(60, 60, 60)
        canvas.drawRect(photoLeft, photoTop, photoLeft + photoW, photoTop + photoH, paint)

        paint.style = Paint.Style.FILL
        paint.textSize = 8f
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("Stamp Size", photoLeft + (photoW / 2), photoTop + 36f, paint)
        canvas.drawText("Photo", photoLeft + (photoW / 2), photoTop + 48f, paint)

        // Fields Layout
        paint.textAlign = Paint.Align.LEFT
        var y = 140f
        val lineSpacing = 28f
        val labelX = left
        val colonX = labelX + 130f
        val contentX = colonX + 10f

        // Helper to draw clean dotted row with value on top
        fun drawRow(num: String, label: String, value: String, customDraw: ((Float) -> Unit)? = null) {
            paint.textSize = 10f
            paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            paint.color = Color.rgb(30, 30, 30)
            paint.style = Paint.Style.FILL

            canvas.drawText("$num $label", labelX, y + 13f, paint)
            canvas.drawText(":", colonX, y + 13f, paint)

            if (customDraw != null) {
                customDraw(y)
            } else {
                paint.style = Paint.Style.FILL
                // Continuous dotted line across the field
                paint.color = Color.rgb(130, 130, 130)
                canvas.drawText(getFillDots(contentX, rightEdge, paint), contentX, y + 13f, paint)

                if (value.isNotBlank()) {
                    paint.color = Color.BLACK
                    paint.textSize = 10f
                    paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                    canvas.drawText(value, contentX + 4f, y + 13f - 2.5f, paint)
                }
            }
            y += lineSpacing
        }

        // 01. Branch Name & Pin Code
        drawRow("01.", "Branch Name", "") { rowY ->
            val branchWidth = 190f
            paint.style = Paint.Style.FILL
            paint.color = Color.rgb(130, 130, 130)
            canvas.drawText(getFillDots(contentX, contentX + branchWidth, paint), contentX, rowY + 13f, paint)
            if (form.branchName.isNotBlank()) {
                paint.color = Color.BLACK
                paint.textSize = 10f
                paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                canvas.drawText(form.branchName, contentX + 4f, rowY + 13f - 2.5f, paint)
            }

            // Pin Code
            val pinLabelX = contentX + branchWidth + 15f
            paint.color = Color.rgb(30, 30, 30)
            paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            canvas.drawText("Pin Code :", pinLabelX, rowY + 13f, paint)
            val pinValX = pinLabelX + 58f
            paint.color = Color.rgb(130, 130, 130)
            canvas.drawText(getFillDots(pinValX, rightEdge, paint), pinValX, rowY + 13f, paint)
            if (form.pinCode.isNotBlank()) {
                paint.color = Color.BLACK
                paint.textSize = 10f
                paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                canvas.drawText(BanglaTextValidator.toEnglishDigits(form.pinCode), pinValX + 4f, rowY + 13f - 2.5f, paint)
            }
        }

        // 02. Joining Date & File ID
        drawRow("02.", "Joining Date", "") { rowY ->
            paint.style = Paint.Style.FILL
            paint.textSize = 9.5f
            paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            
            // Day
            paint.color = Color.rgb(30, 30, 30)
            canvas.drawText("Day :", contentX, rowY + 13f, paint)
            val dayX = contentX + 28f
            paint.color = Color.rgb(130, 130, 130)
            canvas.drawText(getFillDots(dayX, dayX + 28f, paint), dayX, rowY + 13f, paint)
            if (form.joiningDay.isNotBlank()) {
                paint.color = Color.BLACK
                paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                canvas.drawText(BanglaTextValidator.toEnglishDigits(form.joiningDay), dayX + 3f, rowY + 13f - 2.5f, paint)
            }

            // Month
            val monthLabelX = dayX + 34f
            paint.color = Color.rgb(30, 30, 30)
            paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            canvas.drawText("Month :", monthLabelX, rowY + 13f, paint)
            val monthX = monthLabelX + 40f
            paint.color = Color.rgb(130, 130, 130)
            canvas.drawText(getFillDots(monthX, monthX + 32f, paint), monthX, rowY + 13f, paint)
            if (form.joiningMonth.isNotBlank()) {
                paint.color = Color.BLACK
                paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                canvas.drawText(BanglaTextValidator.toEnglishDigits(form.joiningMonth), monthX + 3f, rowY + 13f - 2.5f, paint)
            }

            // Year
            val yearLabelX = monthX + 38f
            paint.color = Color.rgb(30, 30, 30)
            paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            canvas.drawText("Year :", yearLabelX, rowY + 13f, paint)
            val yearX = yearLabelX + 32f
            paint.color = Color.rgb(130, 130, 130)
            canvas.drawText(getFillDots(yearX, yearX + 38f, paint), yearX, rowY + 13f, paint)
            if (form.joiningYear.isNotBlank()) {
                paint.color = Color.BLACK
                paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                canvas.drawText(BanglaTextValidator.toEnglishDigits(form.joiningYear), yearX + 3f, rowY + 13f - 2.5f, paint)
            }

            // File ID
            val fileIdLabelX = yearX + 46f
            paint.color = Color.rgb(30, 30, 30)
            paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            canvas.drawText("File ID :", fileIdLabelX, rowY + 13f, paint)
            val fileIdX = fileIdLabelX + 40f
            paint.color = Color.rgb(130, 130, 130)
            canvas.drawText(getFillDots(fileIdX, rightEdge, paint), fileIdX, rowY + 13f, paint)
            if (form.fileId.isNotBlank()) {
                paint.color = Color.BLACK
                paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                canvas.drawText(BanglaTextValidator.toEnglishDigits(form.fileId), fileIdX + 4f, rowY + 13f - 2.5f, paint)
            }
        }

        // Direct verbatim English address from form fields (without phonetic transliteration)
        val effectiveAddress = if (form.village.isNotBlank() || form.postOffice.isNotBlank() || form.thana.isNotBlank() || form.district.isNotBlank()) {
            buildString {
                if (form.village.isNotBlank()) append("Village: ${form.village.trim()}, ")
                if (form.postOffice.isNotBlank()) append("Post: ${form.postOffice.trim()}, ")
                if (form.thana.isNotBlank()) append("Thana: ${form.thana.trim()}, ")
                if (form.district.isNotBlank()) append("District: ${form.district.trim()}")
            }.trim().removeSuffix(",")
        } else if (form.address.isNotBlank()) {
            form.address
        } else ""

        val effectiveDesignation = when (form.designation) {
            "অফিসার (অ্যাকাউন্টস)" -> "OFFICER (ACCOUNTS)"
            "অফিসার (লোন)", "অফিসার (ঋণ)" -> "OFFICER (LOAN)"
            "সার্ভিস স্টাফ" -> "SERVICE STAFF"
            "অন্যান্য" -> form.customDesignation
            else -> form.designation.ifBlank { form.customDesignation }
        }

        drawRow("03.", "Employee Name", form.employeeName)
        drawRow("04.", "Father's Name", form.fatherName)
        drawRow("05.", "Mother's Name", form.motherName)
        drawRow("06.", "Address", effectiveAddress)
        drawRow("07.", "Designation", effectiveDesignation)
        drawRow("08.", "Blood Group", form.bloodGroup)
        drawRow("09.", "Mobile Number", BanglaTextValidator.toEnglishDigits(form.mobileNumber))
        drawRow("10.", "E-mail Address", form.emailAddress)

        // Signatures at bottom
        val sigY = y + 55f
        paint.color = Color.BLACK
        paint.textSize = 10f
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)

        canvas.drawLine(left, sigY, left + 140f, sigY, linePaint)
        canvas.drawText("Signature of Candidate", left + 8f, sigY + 16f, paint)

        canvas.drawLine(rightEdge - 130f, sigY, rightEdge, sigY, linePaint)
        canvas.drawText("Approved by", rightEdge - 100f, sigY + 16f, paint)
    }

    // -------------------------------------------------------------------------
    // 2. GENERATE TRAINING UNDERTAKING FORM PDF (প্রশিক্ষণ অঙ্গীকারনামা)
    // -------------------------------------------------------------------------
    fun generateTrainingFormPdf(context: Context, form: TrainingFormState): File {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        drawTrainingPage(canvas, form)

        pdfDocument.finishPage(page)
        val file = File(context.cacheDir, "Training_Undertaking.pdf")
        if (file.exists()) file.delete()
        FileOutputStream(file).use { fos ->
            pdfDocument.writeTo(fos)
            fos.flush()
        }
        pdfDocument.close()
        return file
    }

    private fun drawTrainingPage(canvas: Canvas, form: TrainingFormState) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        canvas.drawColor(Color.WHITE)

        // Top Official Header
        drawRrfOfficialHeader(canvas, 40f)

        // Title
        paint.color = Color.BLACK
        paint.textSize = 14f
        paint.typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("প্রশিক্ষণে অংশগ্রহণ সংক্রান্ত অঙ্গীকারনামা", (PAGE_WIDTH / 2).toFloat(), 155f, paint)

        // Line under title
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 0.8f
        canvas.drawLine(150f, 162f, (PAGE_WIDTH - 150).toFloat(), 162f, paint)

        // Body Text
        var y = 182f
        val left = MARGIN + 10f
        val right = PAGE_WIDTH - MARGIN - 10f
        val maxWidth = right - left
        paint.style = Paint.Style.FILL
        paint.textSize = 11f
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        paint.textAlign = Paint.Align.LEFT

        // Format and convert data to Bangla
        val empName = form.employeeName.trim()
        val fatherName = form.fatherName.trim()
        val motherName = form.motherName.trim()
        val rawAddr = form.effectiveAddress.ifBlank { form.permanentAddress }
        val address = BanglaTextValidator.filterBengaliWithPunctuation(rawAddr).ifBlank { rawAddr }
        val startDate = BanglaTextValidator.toBanglaDigits(form.trainingStartDate)
        val endDate = BanglaTextValidator.toBanglaDigits(form.trainingEndDate)
        // Training duration days - leaves blank if empty as requested by user
        val days = BanglaTextValidator.toBanglaDigits(form.trainingDurationDays)
        val fee = BanglaTextValidator.toBanglaDigits(form.trainingFeeAmount)
        val declDate = BanglaTextValidator.toBanglaDigits(form.declarationDate)
        val desig = form.designation

        // Construct lines with dotted placeholders (NO solid underline lines)
        val nameDots = if (empName.isNotBlank()) "$empName " + ".".repeat(maxOf(2, 38 - empName.length)) else "..........................................................."
        val fatherDots = if (fatherName.isNotBlank()) "$fatherName " + ".".repeat(maxOf(2, 35 - fatherName.length)) else "..........................................................."
        val motherDots = if (motherName.isNotBlank()) "$motherName " + ".".repeat(maxOf(2, 35 - motherName.length)) else "..........................................................."
        val addrDots = if (address.isNotBlank()) "$address " + ".".repeat(maxOf(2, 75 - address.length)) else "..................................................................................................................................."
        val sDateDots = if (startDate.isNotBlank()) "$startDate " + ".".repeat(maxOf(2, 18 - startDate.length)) else "........................"
        val eDateDots = if (endDate.isNotBlank()) "$endDate " + ".".repeat(maxOf(2, 18 - endDate.length)) else "........................"
        val daysDots = if (days.isNotBlank()) "$days" else "..........."

        val line1 = if (motherName.isNotBlank()) "আমি $nameDots, পিতা: $fatherDots, মাতা: $motherDots" else "আমি $nameDots, পিতা: $fatherDots"
        val line2 = "স্থায়ী ঠিকানা : $addrDots"
        val line3 = "রূরাল রিকনস্ট্রাকশন ফাউন্ডেশনে আগামী $sDateDots তারিখ থেকে $eDateDots তারিখ পর্যন্ত $daysDots দিনের সংস্থা কর্তৃক আয়োজিত প্রশিক্ষণে অংশগ্রহণ করতে ইচ্ছুক।"

        y = drawWrappedText(canvas, line1, left, y, maxWidth, 11f, 21f) + 14f
        y = drawWrappedText(canvas, line2, left, y, maxWidth, 11f, 21f) + 14f
        y = drawWrappedText(canvas, line3, left, y, maxWidth, 11f, 21f) + 18f

        // Undertaking Statement
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        paint.textSize = 11f
        canvas.drawText("আমি এ মর্মে অঙ্গীকার করছি যে,", left, y, paint)
        y += 24f

        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        val feeDots = if (fee.isNotBlank()) "$fee " + ".".repeat(maxOf(2, 15 - fee.length)) else "...................................."

        val clauses = listOf(
            "১. সংস্থা কর্তৃক আয়োজিত প্রশিক্ষণে আমি সম্পূর্ণরূপে মনোনিবেশ করবো, অবহেলা বা অমনোযোগী হবো না।",
            "২. প্রশিক্ষণ শেষে আমি আমার জন্য নির্ধারিত কর্মস্থলে নির্ধারিত তারিখে যোগদান করতে বাধ্য থাকবো।",
            "৩. প্রশিক্ষণ মূল্যায়নে অবশ্যই সন্তোষজনক ফলাফল অর্জন করবো।",
            "৪. প্রশিক্ষণ মূল্যায়নের ফলাফল বিবেচনায় প্রতিষ্ঠান আমাকে চাকুরীতে যোগদান না নিলে আমার কোন আপত্তি থাকবে না।",
            "৫. প্রশিক্ষণের ফলাফল বিবেচনায় চাকুরীতে যোগদানের সুযোগ না পেলে সংস্থা কর্তৃক নির্ধারিত $feeDots টাকা প্রশিক্ষণ বাবদ ফি আমি জমা দিতে বাধ্য থাকবো বা আমার নিকট হতে কর্তন হলে আমার কোন আপত্তি থাকবে না।",
            "৬. প্রশিক্ষণ সমাপনান্তে আমি পরবর্তী কমপক্ষে ৬ (ছয়) মাস পর্যন্ত রুরাল রিকনস্ট্রাকশন ফাউন্ডেশন (আরআরএফ) এ চাকুরী করতে বাধ্য থাকবো।",
            "৭. প্রশিক্ষণ শেষে সংস্থাতে কোন কারণবশত যোগদান করতে ব্যর্থ হলে চাকুরী বিধিমালা মোতাবেক আমার বিরুদ্ধে যে কোন ধরনের শাস্তিমূলক ব্যবস্থা গ্রহণ করা হলে আমার কোন আপত্তি থাকবে না।"
        )

        for (clause in clauses) {
            y = drawWrappedText(canvas, clause, left, y, maxWidth, 10.5f, 18.5f) + 12f
        }

        // Signatures Section
        val sigY = y + 20f
        paint.textSize = 10f

        // Candidate Signature Block (Right)
        val rightSigX = PAGE_WIDTH - MARGIN - 180f
        canvas.drawText("স্বাক্ষর : ........................................", rightSigX, sigY, paint)

        val sigNameVal = if (empName.isNotBlank()) "$empName " + ".".repeat(maxOf(2, 30 - empName.length)) else "................................................"
        canvas.drawText("নাম : $sigNameVal", rightSigX, sigY + 22f, paint)

        val sigDesigVal = if (desig.isNotBlank()) "$desig " + ".".repeat(maxOf(2, 30 - desig.length)) else "................................................"
        canvas.drawText("পদবী : $sigDesigVal", rightSigX, sigY + 44f, paint)

        // Date grid below designation (পদবীর নিচে তারিখের ছক)
        canvas.drawText("তারিখ :", rightSigX, sigY + 66f, paint)

        val tDateRaw = if (form.declarationDate.isNotBlank()) form.declarationDate else java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.US).format(java.util.Date())
        val tClean = BanglaTextValidator.toBanglaDigits(BanglaTextValidator.filterDigitsOnly(tDateRaw))
        val tDigits = tClean.padEnd(8, ' ').take(8)

        var tBoxX = rightSigX + 38f
        val tBoxY = sigY + 53f
        val tBoxW = 13.5f
        val tBoxH = 16f
        for (i in 0 until 8) {
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 0.9f
            paint.color = Color.BLACK
            canvas.drawRect(tBoxX, tBoxY, tBoxX + tBoxW, tBoxY + tBoxH, paint)
            val ch = tDigits.getOrNull(i)?.toString()?.trim() ?: ""
            if (ch.isNotEmpty()) {
                paint.style = Paint.Style.FILL
                paint.textSize = 9.5f
                paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                paint.textAlign = Paint.Align.CENTER
                canvas.drawText(ch, tBoxX + (tBoxW / 2f), tBoxY + 12f, paint)
                paint.style = Paint.Style.STROKE
                paint.textAlign = Paint.Align.LEFT
            }
            tBoxX += (tBoxW + 1.2f)
        }
        paint.style = Paint.Style.FILL
        paint.textAlign = Paint.Align.LEFT

        // Witness Block (Left)
        canvas.drawText("স্বাক্ষীর স্বাক্ষর (নাম ও ঠিকানাসহ) :", left, sigY, paint)

        val w1Val = if (form.witness1.isNotBlank()) "${form.witness1} " + ".".repeat(maxOf(2, 40 - form.witness1.length)) else "................................................................"
        canvas.drawText("১. $w1Val", left, sigY + 24f, paint)

        val w2Val = if (form.witness2.isNotBlank()) "${form.witness2} " + ".".repeat(maxOf(2, 40 - form.witness2.length)) else "................................................................"
        canvas.drawText("২. $w2Val", left, sigY + 48f, paint)

        // Bottom Institutional Footer
        drawRrfOfficialFooter(canvas)
    }

    // -------------------------------------------------------------------------
    // 3. GENERATE RELATIONSHIP DECLARATION FORM PDF
    // -------------------------------------------------------------------------
    fun generateRelationshipFormPdf(context: Context, form: RelationshipFormState): File {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        drawRelationshipPage(canvas, form)

        pdfDocument.finishPage(page)
        val file = File(context.cacheDir, "Relationship_Declaration.pdf")
        if (file.exists()) file.delete()
        FileOutputStream(file).use { fos ->
            pdfDocument.writeTo(fos)
            fos.flush()
        }
        pdfDocument.close()
        return file
    }

    private fun drawRelationshipPage(canvas: Canvas, form: RelationshipFormState) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        canvas.drawColor(Color.WHITE)

        // Header
        drawRrfOfficialHeader(canvas, 40f)

        // Title
        paint.color = Color.BLACK
        paint.textSize = 15f
        paint.typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("Relationship Declaration Form", (PAGE_WIDTH / 2).toFloat(), 155f, paint)

        // To section
        paint.textAlign = Paint.Align.LEFT
        paint.textSize = 11f
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        var y = 195f
        val left = MARGIN + 15f
        val right = PAGE_WIDTH - MARGIN - 15f

        canvas.drawText("To", left, y, paint)
        y += 16f
        canvas.drawText("The Executive Director", left, y, paint)
        y += 16f
        canvas.drawText("RRF", left, y, paint)

        y += 30f
        canvas.drawText("I do hereby declare that I have kinship with the following persons of RRF.", left, y, paint)

        y += 24f
        // Table
        val colSl = 35f
        val colName = 210f
        val colDesig = 130f
        val colRel = (right - left) - colSl - colName - colDesig

        val tableTop = y
        val rowHeight = 70f

        // Table Header
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        canvas.drawRect(left, tableTop, right, tableTop + 30f, paint)

        paint.style = Paint.Style.FILL
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        paint.textSize = 10f
        paint.textAlign = Paint.Align.CENTER

        canvas.drawText("SL", left + (colSl / 2), tableTop + 19f, paint)
        canvas.drawText("Name and Address", left + colSl + (colName / 2), tableTop + 19f, paint)
        canvas.drawText("Designation in RRF", left + colSl + colName + (colDesig / 2), tableTop + 19f, paint)
        canvas.drawText("Relationship", left + colSl + colName + colDesig + (colRel / 2), tableTop + 19f, paint)

        // Vertical Header Lines
        paint.style = Paint.Style.STROKE
        canvas.drawLine(left + colSl, tableTop, left + colSl, tableTop + 30f, paint)
        canvas.drawLine(left + colSl + colName, tableTop, left + colSl + colName, tableTop + 30f, paint)
        canvas.drawLine(left + colSl + colName + colDesig, tableTop, left + colSl + colName + colDesig, tableTop + 30f, paint)

        var curRowTop = tableTop + 30f

        // 3 Kinship Rows
        for (i in 0 until 3) {
            val item = form.kinshipList.getOrNull(i) ?: KinshipPerson()
            paint.style = Paint.Style.STROKE
            canvas.drawRect(left, curRowTop, right, curRowTop + rowHeight, paint)
            canvas.drawLine(left + colSl, curRowTop, left + colSl, curRowTop + rowHeight, paint)
            canvas.drawLine(left + colSl + colName, curRowTop, left + colSl + colName, curRowTop + rowHeight, paint)
            canvas.drawLine(left + colSl + colName + colDesig, curRowTop, left + colSl + colName + colDesig, curRowTop + rowHeight, paint)

            paint.style = Paint.Style.FILL
            paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            paint.textSize = 10f
            paint.textAlign = Paint.Align.CENTER
            canvas.drawText("${i + 1}.", left + (colSl / 2), curRowTop + 38f, paint)

            if (!form.hasNoKinship && item.nameAndAddress.isNotBlank()) {
                paint.textAlign = Paint.Align.LEFT
                drawWrappedText(canvas, item.nameAndAddress, left + colSl + 6f, curRowTop + 18f, colName - 12f, 9.5f, 13f)
                canvas.drawText(item.effectiveDesignation, left + colSl + colName + 6f, curRowTop + 38f, paint)
                canvas.drawText(item.effectiveRelationship, left + colSl + colName + colDesig + 6f, curRowTop + 38f, paint)
            }
            curRowTop += rowHeight
        }

        // Row 4: "I have none. [Tick]"
        val lastRowH = 34f
        paint.style = Paint.Style.STROKE
        canvas.drawRect(left, curRowTop, right, curRowTop + lastRowH, paint)
        canvas.drawLine(left + colSl, curRowTop, left + colSl, curRowTop + lastRowH, paint)
        canvas.drawLine(left + colSl + colName + colDesig, curRowTop, left + colSl + colName + colDesig, curRowTop + lastRowH, paint)

        paint.style = Paint.Style.FILL
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("4.", left + (colSl / 2), curRowTop + 21f, paint)
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText("I have none. (আরআরএফ)", left + colSl + 10f, curRowTop + 21f, paint)

        val tickBoxX = left + colSl + colName + colDesig + (colRel / 2) - 8f
        val tickBoxY = curRowTop + 8f
        paint.style = Paint.Style.STROKE
        canvas.drawRect(tickBoxX, tickBoxY, tickBoxX + 16f, tickBoxY + 16f, paint)
        if (form.hasNoKinship) {
            paint.style = Paint.Style.FILL
            paint.textSize = 13f
            paint.textAlign = Paint.Align.CENTER
            canvas.drawText("✓", tickBoxX + 8f, tickBoxY + 13f, paint)
        }

        // Bottom
        y = curRowTop + lastRowH + 35f
        paint.style = Paint.Style.FILL
        paint.textSize = 11f
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText("Thanks,", left, y, paint)

        y += 55f
        canvas.drawText("Employee Name and Signature: ${form.employeeName}", left, y, paint)
        y += 22f
        canvas.drawText("Date: ${form.declarationDate}", left, y, paint)

        // Footer
        drawRrfOfficialFooter(canvas)
    }

    // -------------------------------------------------------------------------
    // 4. GENERATE NOMINEE DECLARATION FORM PDF
    // -------------------------------------------------------------------------
    fun generateNomineeFormPdf(context: Context, form: NomineeFormState): File {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        drawNomineePage(canvas, form)

        pdfDocument.finishPage(page)
        val file = File(context.cacheDir, "Nominee_Declaration.pdf")
        if (file.exists()) file.delete()
        FileOutputStream(file).use { fos ->
            pdfDocument.writeTo(fos)
            fos.flush()
        }
        pdfDocument.close()
        return file
    }

    private fun drawNomineePage(canvas: Canvas, form: NomineeFormState) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        canvas.drawColor(Color.WHITE)

        // Header Title
        paint.color = Color.rgb(20, 20, 20)
        paint.textSize = 18f
        paint.typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("Rural Reconstruction Foundation", (PAGE_WIDTH / 2).toFloat(), 55f, paint)

        paint.textSize = 13f
        paint.typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        canvas.drawText("NOMINEE DECLARATION FORM", (PAGE_WIDTH / 2).toFloat(), 75f, paint)

        // Date Box (Top Right)
        val left = MARGIN + 10f
        val right = PAGE_WIDTH - MARGIN - 10f
        paint.textSize = 9.5f
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        paint.textAlign = Paint.Align.RIGHT
        paint.color = Color.BLACK
        canvas.drawText("Date / তারিখ :", right - 124f, 102f, paint)

        // Date Boxes (8 boxes: DD MM YYYY)
        val nDateRaw = if (form.declarationDate.isNotBlank()) form.declarationDate else java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.US).format(java.util.Date())
        val nClean = BanglaTextValidator.toEnglishDigits(BanglaTextValidator.filterDigitsOnly(nDateRaw))
        val nDigits = nClean.padEnd(8, ' ').take(8)

        var boxX = right - 118f
        val boxY = 89f
        val boxW = 13.5f
        val boxH = 16f
        for (i in 0 until 8) {
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 1f
            paint.color = Color.BLACK
            canvas.drawRect(boxX, boxY, boxX + boxW, boxY + boxH, paint)
            val ch = nDigits.getOrNull(i)?.toString()?.trim() ?: ""
            if (ch.isNotEmpty()) {
                paint.style = Paint.Style.FILL
                paint.textSize = 10f
                paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                paint.textAlign = Paint.Align.CENTER
                canvas.drawText(ch, boxX + (boxW / 2f), boxY + 12f, paint)
            }
            boxX += (boxW + 1.2f)
        }

        // Sub-labels above date boxes: D D M M Y Y Y Y
        paint.style = Paint.Style.FILL
        paint.textSize = 6.5f
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        paint.color = Color.rgb(80, 80, 80)
        paint.textAlign = Paint.Align.CENTER
        val dateBoxLabels = listOf("D", "D", "M", "M", "Y", "Y", "Y", "Y")
        var labelBoxX = right - 118f
        for (lbl in dateBoxLabels) {
            canvas.drawText(lbl, labelBoxX + (boxW / 2f), boxY - 3f, paint)
            labelBoxX += (boxW + 1.2f)
        }

        // Declaration paragraph
        var y = 125f
        paint.style = Paint.Style.FILL
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        paint.textSize = 9.5f
        paint.textAlign = Paint.Align.LEFT
        val intro = "I do hereby nominate the following persons for making payment of the balance held at the organization in case of my death. I reserve the right to change the nomination at any time. It's the nominee's sole responsibility for distributing the balance of my account among my heirs as per prevailing Bangladesh law. In such case I do also agree that, the organization will not be made liable for such payment as per my instruction or such distribution."
        y = drawWrappedText(canvas, intro, left, y, right - left, 9.5f, 13f) + 16f

        // Subhead: Nominee Information
        paint.textSize = 13f
        paint.typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("Nominee Information", (PAGE_WIDTH / 2).toFloat(), y, paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 0.8f
        canvas.drawLine(PAGE_WIDTH / 2f - 70f, y + 3f, PAGE_WIDTH / 2f + 70f, y + 3f, paint)

        y += 18f

        // Big Table
        val colSl = 24f
        val colName = 105f
        val colAddr = 95f
        val colNid = 65f
        val colRel = 65f
        val colShare = 45f
        val colPhoto = 65f
        val colSig = (right - left) - colSl - colName - colAddr - colNid - colRel - colShare - colPhoto

        val tableTop = y
        val headerH = 32f
        val rowH = 95f

        // Table Header Box
        paint.style = Paint.Style.STROKE
        canvas.drawRect(left, tableTop, right, tableTop + headerH, paint)

        paint.style = Paint.Style.FILL
        paint.textSize = 8.5f
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        paint.textAlign = Paint.Align.CENTER

        var cx = left
        fun drawHeaderCol(w: Float, text: String, text2: String? = null) {
            if (text2 == null) {
                canvas.drawText(text, cx + (w / 2), tableTop + 19f, paint)
            } else {
                canvas.drawText(text, cx + (w / 2), tableTop + 14f, paint)
                canvas.drawText(text2, cx + (w / 2), tableTop + 24f, paint)
            }
            cx += w
            paint.style = Paint.Style.STROKE
            canvas.drawLine(cx, tableTop, cx, tableTop + headerH, paint)
            paint.style = Paint.Style.FILL
        }

        drawHeaderCol(colSl, "Sl.", "No")
        drawHeaderCol(colName, "Nominee's Name &", "Father's/Mother's")
        drawHeaderCol(colAddr, "Full Address", "of Nominee")
        drawHeaderCol(colNid, "NID No.", "of Nominee")
        drawHeaderCol(colRel, "Relationship", "with Employee")
        drawHeaderCol(colShare, "Percent", "of shares")
        drawHeaderCol(colPhoto, "Photo of", "nominee")
        drawHeaderCol(colSig, "Signature")

        // 3 Data Rows
        var rowTop = tableTop + headerH
        for (i in 0 until 3) {
            val nominee = form.nominees.getOrNull(i)
            paint.style = Paint.Style.STROKE
            canvas.drawRect(left, rowTop, right, rowTop + rowH, paint)

            // Column dividers
            var divX = left
            listOf(colSl, colName, colAddr, colNid, colRel, colShare, colPhoto).forEach { w ->
                divX += w
                canvas.drawLine(divX, rowTop, divX, rowTop + rowH, paint)
            }

            paint.style = Paint.Style.FILL
            paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            paint.textSize = 9f
            paint.textAlign = Paint.Align.CENTER
            canvas.drawText("${i + 1}", left + (colSl / 2), rowTop + (rowH / 2), paint)

            if (nominee != null && nominee.isFilled) {
                paint.textAlign = Paint.Align.LEFT
                drawWrappedText(canvas, nominee.getFormattedNameAndParents(), left + colSl + 4f, rowTop + 16f, colName - 8f, 8.5f, 12f)
                drawWrappedText(canvas, nominee.getFormattedAddress(), left + colSl + colName + 4f, rowTop + 16f, colAddr - 8f, 8.5f, 12f)
                canvas.drawText(BanglaTextValidator.toEnglishDigits(nominee.nidNo), left + colSl + colName + colAddr + 3f, rowTop + (rowH / 2), paint)
                canvas.drawText(nominee.getEffectiveRelationship(), left + colSl + colName + colAddr + colNid + 3f, rowTop + (rowH / 2), paint)
                paint.textAlign = Paint.Align.CENTER
                canvas.drawText(BanglaTextValidator.toEnglishDigits(nominee.percentOfShares), left + colSl + colName + colAddr + colNid + colRel + (colShare / 2), rowTop + (rowH / 2), paint)
            }
            rowTop += rowH
        }

        // Bottom Signatures Section
        y = rowTop + 40f
        paint.textSize = 10f
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        paint.textAlign = Paint.Align.LEFT

        // Employee
        canvas.drawText("Signature of the Employee: .......................................", left + 10f, y, paint)
        val empVal = if (form.employeeName.isNotBlank()) form.employeeName else "......................................."
        val desigVal = if (form.designation.isNotBlank()) form.designation else "......................................."
        canvas.drawText("Employee Name: $empVal", left + 10f, y + 20f, paint)
        canvas.drawText("Designation: $desigVal", left + 10f, y + 38f, paint)

        // Nominee & Administration (Right)
        val rightSigX = right - 180f
        canvas.drawText("Signature of the Nominee: ...................................", rightSigX, y, paint)
        canvas.drawText("Attested by Administration: ................................", rightSigX, y + 56f, paint)

        // Bottom Note
        y += 85f
        paint.textSize = 8.5f
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.ITALIC)
        val note = "Note: Employee may nominate more than one nominee, in that case s/he will use separate form and must mention the share his/her percent each nominee will get."
        drawWrappedText(canvas, note, left, y, right - left, 8.5f, 12f)
    }

    // -------------------------------------------------------------------------
    // 5. GENERATE PERSONAL INFORMATION 3-PAGE FORM PDF (তথ্যানুসন্ধান ৩ পাতার ফরম)
    // -------------------------------------------------------------------------
    fun generatePersonalInfoFormPdf(context: Context, form: PersonalInfoFormState): File {
        val pdfDocument = PdfDocument()

        for (pageNum in 1..3) {
            val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNum).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas

            when (pageNum) {
                1 -> drawPersonalInfoPage1(canvas, form)
                2 -> drawPersonalInfoPage2(canvas, form)
                3 -> drawPersonalInfoPage3(canvas, form)
            }

            pdfDocument.finishPage(page)
        }

        val file = File(context.cacheDir, "Worker_Information_Form.pdf")
        if (file.exists()) file.delete()
        FileOutputStream(file).use { fos ->
            pdfDocument.writeTo(fos)
            fos.flush()
        }
        pdfDocument.close()
        return file
    }

    private fun drawPersonalInfoPage1(canvas: Canvas, form: PersonalInfoFormState) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        canvas.drawColor(Color.WHITE)

        val left = 40f
        val right = 555f
        val contentWidth = right - left

        // Top Header
        paint.textSize = 10f
        paint.textAlign = Paint.Align.RIGHT
        paint.color = Color.BLACK
        canvas.drawText("RRF", right, 42f, paint)

        paint.textSize = 14f
        paint.typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("তথ্যানুসন্ধান ফরম", (PAGE_WIDTH / 2).toFloat(), 52f, paint)

        paint.textSize = 8.5f
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        canvas.drawText("(অঙ্গীকারনামা প্রদানকারী সংক্রান্ত তথ্য ব্যতীত অন্যান্য সকল তথ্য কর্মী কর্তৃক পূরণীয়)", (PAGE_WIDTH / 2).toFloat(), 66f, paint)

        paint.textSize = 11.5f
        paint.typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        canvas.drawText("কর্মীর ব্যক্তিগত তথ্যাবলী :", (PAGE_WIDTH / 2).toFloat(), 88f, paint)

        // Photo Box (compact and clean)
        val photoW = 60f
        val photoH = 70f
        val photoLeft = right - photoW
        val photoTop = 42f
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 0.8f
        canvas.drawRect(photoLeft, photoTop, photoLeft + photoW, photoTop + photoH, paint)
        paint.style = Paint.Style.FILL
        paint.textSize = 7f
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("১ কপি ছবি", photoLeft + (photoW / 2), photoTop + 32f, paint)
        canvas.drawText("(পাসপোর্ট সাইজ)", photoLeft + (photoW / 2), photoTop + 44f, paint)

        // Fields
        paint.textAlign = Paint.Align.LEFT
        var y = 125f
        val stepY = 22.5f

        val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 0.8f
            color = Color.rgb(100, 100, 100)
        }

        fun drawField(label: String, value: String, lineY: Float = y, startX: Float = left, endX: Float = right) {
            paint.textSize = 9f
            paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            paint.color = Color.BLACK
            canvas.drawText(label, startX, lineY, paint)
            val labelW = paint.measureText(label)
            val valX = startX + labelW + 4f
            val availWidth = endX - valX
            if (availWidth > 0f) {
                canvas.save()
                canvas.clipRect(valX, lineY - 14f, endX, lineY + 6f)
                // Draw continuous dotted line across the whole field
                paint.color = Color.rgb(130, 130, 130)
                canvas.drawText(getFillDots(valX, endX, paint), valX, lineY, paint)
                // If value exists, draw text slightly above dots (no solid underline)
                if (value.isNotBlank()) {
                    paint.color = Color.BLACK
                    paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                    val originalSize = paint.textSize
                    val measuredW = paint.measureText(value)
                    if (measuredW > availWidth) {
                        paint.textSize = (originalSize * (availWidth / measuredW)).coerceAtLeast(6.5f)
                    }
                    canvas.drawText(value, valX, lineY - 2.5f, paint)
                    paint.textSize = originalSize
                }
                canvas.restore()
            }
        }

        fun drawTwoFields(
            label1: String, val1: String,
            label2: String, val2: String,
            lineY: Float = y,
            splitX: Float = left + 270f
        ) {
            drawField(label1, val1, lineY, startX = left, endX = splitX - 10f)
            drawField(label2, val2, lineY, startX = splitX, endX = right)
        }

        drawField("ক) কর্মীর নাম (বাংলায়) :", form.employeeNameBangla)
        y += stepY

        drawTwoFields("    (ইংরেজীতে) :", form.employeeNameEnglish, "মহিলা/পুরুষ :", form.gender)
        y += stepY

        drawField("    জাতীয় পরিচয় পত্র নং (কর্মী) :", BanglaTextValidator.toBanglaDigits(form.employeeNid))
        y += stepY

        drawTwoFields("খ) পিতার নাম :", form.fatherName, "মাতার নাম :", form.motherName)
        y += stepY

        drawTwoFields("    জাতীয় পরিচয় পত্র নং (পিতা) :", BanglaTextValidator.toBanglaDigits(form.fatherNid), "জাতীয় পরিচয় পত্র নং (মাতা) :", BanglaTextValidator.toBanglaDigits(form.motherNid))
        y += stepY

        drawField("    জাতীয় পরিচয় পত্র নং (জামিনদার) :", BanglaTextValidator.toBanglaDigits(form.guarantorNid))
        y += stepY

        drawField("গ) স্বামী/স্ত্রীর নাম :", form.spouseName)
        y += stepY

        // Permanent Address lines
        drawField("ঘ) স্থায়ী ঠিকানা : গ্রাম :", form.permVillage, startX = left, endX = left + 185f)
        drawField("ডাকঘর :", form.permPostOffice, startX = left + 195f, endX = left + 360f)
        drawField("পোস্ট কোড :", BanglaTextValidator.toBanglaDigits(form.permPostCode), startX = left + 370f, endX = right)
        y += stepY

        val permDistStr = form.permDistrict
        drawField("    উপজেলা :", form.permUpazila, startX = left, endX = left + 185f)
        drawField("থানা :", form.permThana, startX = left + 195f, endX = left + 360f)
        drawField("জেলা :", permDistStr, startX = left + 370f, endX = right)
        y += stepY

        // Current Address lines
        drawField("ঙ) বর্তমান ঠিকানা : গ্রাম/বাড়ী :", form.currVillageOrHouse, startX = left, endX = left + 330f)
        drawField("রোড নং :", form.currRoadNo, startX = left + 340f, endX = right)
        y += stepY

        drawField("    ডাকঘর :", form.currPostOffice, startX = left, endX = left + 185f)
        drawField("পোস্ট কোড :", BanglaTextValidator.toBanglaDigits(form.currPostCode), startX = left + 195f, endX = left + 340f)
        drawField("উপজেলা :", form.currUpazila, startX = left + 350f, endX = right)
        y += stepY

        val currDistStr = form.currDistrict
        drawField("    থানা :", form.currThana, startX = left, endX = left + 270f)
        drawField("জেলা :", currDistStr, startX = left + 280f, endX = right)
        y += stepY

        drawTwoFields("চ) মোবাইল/ফোন নম্বর :", BanglaTextValidator.toBanglaDigits(form.mobileNumber), "অনুরোধ নম্বর :", BanglaTextValidator.toBanglaDigits(form.alternateNumber))
        y += stepY

        val bDob = BanglaTextValidator.toBanglaDigits(form.dateOfBirth)
        val calculatedAge = if (form.dateOfBirth.isNotBlank()) BanglaTextValidator.calculateDetailedAge(form.dateOfBirth) else null
        val ageStr = if (calculatedAge != null) {
            calculatedAge.toDisplayString()
        } else if (form.ageYears.isNotBlank()) {
            val cleanYears = form.ageYears.trim()
            if (cleanYears.contains("বছর")) BanglaTextValidator.toBanglaDigits(cleanYears)
            else "${BanglaTextValidator.toBanglaDigits(cleanYears)} বছর"
        } else {
            ""
        }
        drawTwoFields("ছ) জন্ম তারিখ :", bDob, "বয়স :", ageStr)
        y += stepY

        drawTwoFields("    Driving license Number :", BanglaTextValidator.toBanglaDigits(form.drivingLicenseNo), "Passport Number :", BanglaTextValidator.toBanglaDigits(form.passportNo))
        y += stepY

        val caseStr = when {
            form.hasPoliceCase -> {
                val countStr = if (form.policeCaseCount.isNotBlank()) "সংখ্যা: ${BanglaTextValidator.toBanglaDigits(form.policeCaseCount)}টি" else ""
                val detailStr = if (form.policeCaseDetails.isNotBlank()) "বিবরণ: ${form.policeCaseDetails}" else ""
                listOf("হ্যাঁ", countStr, detailStr).filter { it.isNotBlank() }.joinToString(", ")
            }
            form.policeCaseDetails.isNotBlank() -> form.policeCaseDetails
            else -> "না"
        }
        drawField("জ) কর্মীর স্থানীয় থানায় কোন মামলা আছে/ছিল কি না :", caseStr)
        y += stepY

        drawTwoFields("ঝ) কর্মীর ব্যাংক অ্যাকাউন্ট নম্বর :", BanglaTextValidator.toBanglaDigits(form.bankAccountNo), "হিসাবের ধরণ :", form.bankAccountType, splitX = left + 310f)
        y += stepY
        drawTwoFields("    ব্যাংকের নাম :", form.bankName, "শাখার নাম :", form.branchName, splitX = left + 270f)
        y += stepY

        drawTwoFields("ঞ) কর্মীর মাসিক আয় :", BanglaTextValidator.toBanglaDigits(form.monthlyIncome), "কথায় :", form.monthlyIncomeWords, splitX = left + 240f)
        y += stepY
        drawTwoFields("    বাৎসরিক আয় :", BanglaTextValidator.toBanglaDigits(form.yearlyIncome), "কথায় :", form.yearlyIncomeWords, splitX = left + 240f)
        y += stepY

        drawField("ট) কর্মীর Tax Identification Number (TIN) নম্বর :", BanglaTextValidator.toBanglaDigits(form.tinNumber))
        y += stepY

        val gMonthly = if (form.guardianMonthlyIncome.isNotBlank()) form.guardianMonthlyIncome else if (form.guardianYearlyIncome.isNotBlank()) {
            val engVal = BanglaTextValidator.toEnglishDigits(form.guardianYearlyIncome).toLongOrNull() ?: 0L
            if (engVal > 0) BanglaTextValidator.toBanglaDigits((engVal / 12).toString()) else ""
        } else ""
        val gMonthlyWords = if (form.guardianMonthlyIncomeWords.isNotBlank()) form.guardianMonthlyIncomeWords else if (gMonthly.isNotBlank()) BanglaTextValidator.convertNumberToBanglaWords(gMonthly) else ""

        val gYearly = if (form.guardianYearlyIncome.isNotBlank()) form.guardianYearlyIncome else if (gMonthly.isNotBlank()) {
            val engVal = BanglaTextValidator.toEnglishDigits(gMonthly).toLongOrNull() ?: 0L
            if (engVal > 0) BanglaTextValidator.toBanglaDigits((engVal * 12).toString()) else ""
        } else ""
        val gYearlyWords = if (form.guardianYearlyIncomeWords.isNotBlank()) form.guardianYearlyIncomeWords else if (gYearly.isNotBlank()) BanglaTextValidator.convertNumberToBanglaWords(gYearly) else ""

        drawTwoFields("ঠ) জামিনদারের মাসিক আয় :", BanglaTextValidator.toBanglaDigits(gMonthly), "কথায় :", gMonthlyWords, splitX = left + 260f)
        y += stepY
        drawTwoFields("    বাৎসরিক আয় :", BanglaTextValidator.toBanglaDigits(gYearly), "কথায় :", gYearlyWords, splitX = left + 260f)
        y += stepY

        drawField("ড) কর্মীর সম্পত্তির বিবরণ (যদি থাকে) :", form.propertyDetails)

        // Page Number
        drawPageFooter(canvas, 1, 3)
    }

    private fun drawPersonalInfoPage2(canvas: Canvas, form: PersonalInfoFormState) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        canvas.drawColor(Color.WHITE)
        val left = 40f
        val right = 555f

        paint.textSize = 10f
        paint.textAlign = Paint.Align.RIGHT
        paint.color = Color.BLACK
        canvas.drawText("RRF", right, 42f, paint)

        // Table 1: শিক্ষাগত যোগ্যতা সংক্রান্ত
        paint.textSize = 11.5f
        paint.typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        paint.textAlign = Paint.Align.CENTER
        var y = 60f
        canvas.drawText("কর্মীর শিক্ষাগত যোগ্যতা সংক্রান্ত :", (PAGE_WIDTH / 2).toFloat(), y, paint)

        y += 16f
        val colW1 = floatArrayOf(24f, 70f, 110f, 75f, 60f, 50f, 80f, 46f)
        val headers1 = arrayOf("ক্রমিক", "ডিগ্রীর নাম", "শিক্ষা প্রতিষ্ঠানের নাম", "বিভাগ/বিষয়", "প্রাপ্ত নম্বর/জিপিএ", "ফলাফল", "বোর্ড/বিশ্ববিদ্যালয়", "পাশের সন")
        
        y = drawGenericTable(canvas, left, y, right, colW1, headers1, form.educations.map {
            arrayOf(it.degreeName, it.instituteName, it.subjectGroup, it.marksOrCgpa, it.result, it.boardOrUniversity, it.passingYear)
        }, rowHeight = 24f, maxRows = 5)

        y += 22f
        // Table 2: পেশাগত অভিজ্ঞতা
        paint.textSize = 11.5f
        paint.typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("কর্মীর পেশাগত অভিজ্ঞতা সংক্রান্ত তথ্য (যদি থাকে) :", (PAGE_WIDTH / 2).toFloat(), y, paint)

        y += 16f
        val colW2 = floatArrayOf(24f, 130f, 85f, 85f, 55f, 136f)
        val headers2 = arrayOf("ক্রমিক", "প্রতিষ্ঠানের নাম (ফোনসহ)", "পদবী", "সময়কাল (হতে-পর্যন্ত)", "মেয়াদ", "সম্পাদিত দায়িত্ব")
        y = drawGenericTable(canvas, left, y, right, colW2, headers2, form.experiences.map {
            arrayOf(it.organizationName, it.designation, it.periodFromTo, it.duration, it.duties)
        }, rowHeight = 28f, maxRows = 4)

        y += 16f
        paint.textSize = 9.5f
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText("অর্জিত অভিজ্ঞতার মেয়াদ : ${form.totalExperience.ifBlank { ".................. বছর .................. মাস" }}", left + 10f, y, paint)

        y += 22f
        // Table 3: প্রশিক্ষণ সংক্রান্ত তথ্য
        paint.textSize = 11.5f
        paint.typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("কর্মীর প্রশিক্ষণ সংক্রান্ত তথ্য (যদি থাকে) :", (PAGE_WIDTH / 2).toFloat(), y, paint)

        y += 16f
        val colW3 = floatArrayOf(24f, 155f, 165f, 95f, 76f)
        val headers3 = arrayOf("ক্রমিক", "প্রতিষ্ঠানের নাম", "প্রশিক্ষণের বিষয়/সনদপত্রের নাম", "সময়কাল", "মেয়াদ")
        drawGenericTable(canvas, left, y, right, colW3, headers3, form.trainings.map {
            arrayOf(it.organizationName, it.trainingTopic, it.period, it.duration)
        }, rowHeight = 26f, maxRows = 4)

        drawPageFooter(canvas, 2, 3)
    }

    private fun drawPersonalInfoPage3(canvas: Canvas, form: PersonalInfoFormState) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        canvas.drawColor(Color.WHITE)
        val left = 40f
        val right = 555f
        val contentWidth = right - left

        paint.textSize = 10f
        paint.textAlign = Paint.Align.RIGHT
        paint.color = Color.BLACK
        canvas.drawText("RRF", right, 42f, paint)

        // Family Table
        paint.textSize = 11.5f
        paint.typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        paint.textAlign = Paint.Align.CENTER
        var y = 60f
        canvas.drawText("কর্মীর পরিবার সংক্রান্ত (পিতা/মাতা/ভাই/বোন/স্বামী/স্ত্রী/পুত্র/কন্যা ইত্যাদি) :", (PAGE_WIDTH / 2).toFloat(), y, paint)

        y += 16f
        val colW = floatArrayOf(24f, 110f, 60f, 75f, 65f, 85f, 36f, 60f)
        val headers = arrayOf("ক্রমিক", "সদস্যের নাম", "সম্পর্ক", "শিক্ষাগত যোগ্যতা", "পেশা", "মোবাইল নম্বর", "বয়স", "মন্তব্য")
        y = drawGenericTable(canvas, left, y, right, colW, headers, form.familyMembers.map {
            arrayOf(it.name, it.relationship, it.education, it.occupation, it.mobileNumber, it.age, it.remarks)
        }, rowHeight = 22f, maxRows = 8)

        y += 26f
        // Section: অঙ্গীকারনামা প্রদানকারী সংক্রান্ত
        paint.textSize = 11.5f
        paint.typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("কর্মীর অঙ্গীকারনামা প্রদানকারী সংক্রান্ত (অঙ্গীকারনামা প্রদানকারী কর্তৃক পূরণীয়) :", (PAGE_WIDTH / 2).toFloat(), y, paint)

        y += 20f
        paint.textSize = 9f
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        paint.textAlign = Paint.Align.LEFT

        val gName = form.guarantorName.ifBlank { "..........................................................." }
        val empFather = form.fatherName.ifBlank { "..........................................................." }
        val gRel = form.guarantorRelationship.ifBlank { "..................." }
        val eName = form.employeeNameBangla.ifBlank { "..........................................................." }

        val gText = "আমি নিম্নস্বাক্ষরকারী $gName স্বজ্ঞানে শপথ করছি যে, জনাব $eName, পিতার নাম : $empFather, আমার (সম্পর্ক) : $gRel। তার দেয়া উপর্যুক্ত যাবতীয় তথ্যাবলী সত্য ও নির্ভুল। চাকুরিতে যোগদানের পর ভবিষ্যতে জনাব $eName দ্বারা সংস্থার যে কোন ক্ষতি সংঘটিত হলে আমি তা পূরণ করার অঙ্গীকারে আবদ্ধ হলাম। ক্ষতিপূরণে ব্যর্থ হলে সংস্থা আমার বিরুদ্ধে যে কোন আইনানুগ ব্যবস্থা গ্রহণ করতে পারবে সে ক্ষেত্রে আমার কোন আপত্তি থাকবে না।"
        y = drawWrappedText(canvas, gText, left, y, contentWidth, 9f, 14f) + 20f

        // Fingerprint & Signature (No box around thumbprint)
        val boxWidth = 140f
        val boxHeight = 50f
        val rightBoxX = right - boxWidth

        paint.style = Paint.Style.FILL
        paint.textSize = 8.5f
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("অঙ্গীকারনামা প্রদানকারীর বৃদ্ধাঙ্গুলের ছাপ", rightBoxX + (boxWidth / 2), y + 28f, paint)

        paint.textAlign = Paint.Align.LEFT
        paint.textSize = 9f
        canvas.drawText("অঙ্গীকারনামা প্রদানকারীর স্বাক্ষর ও তারিখ : ........................................", left, y + 28f, paint)

        y += boxHeight + 16f
        val linePaintP3 = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 0.8f
            color = Color.rgb(100, 100, 100)
        }

        fun drawP3Field(label: String, value: String, lineY: Float = y, startX: Float = left, endX: Float = right) {
            paint.textSize = 9f
            paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            paint.color = Color.BLACK
            canvas.drawText(label, startX, lineY, paint)
            val labelW = paint.measureText(label)
            val valX = startX + labelW + 4f
            val availWidth = endX - valX
            if (availWidth > 0f) {
                canvas.save()
                canvas.clipRect(valX, lineY - 14f, endX, lineY + 6f)
                // Draw continuous dotted line across the whole field
                paint.color = Color.rgb(130, 130, 130)
                canvas.drawText(getFillDots(valX, endX, paint), valX, lineY, paint)
                // If value exists, draw text slightly above dots (no solid underline)
                if (value.isNotBlank()) {
                    paint.color = Color.BLACK
                    paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                    canvas.drawText(value, valX, lineY - 2.5f, paint)
                }
                canvas.restore()
            }
        }

        drawP3Field("অঙ্গীকারনামা প্রদানকারীর পূর্ণ নাম :", form.guarantorName)
        y += 16f
        drawP3Field("পিতার নাম :", form.guarantorFatherName, startX = left, endX = left + 260f)
        drawP3Field("মাতার নাম :", form.guarantorMotherName, startX = left + 270f, endX = right)
        y += 16f
        drawP3Field("পেশা :", form.guarantorOccupation, startX = left, endX = left + 160f)
        drawP3Field("গ্রাম/বাড়ী :", form.guarantorVillage, startX = left + 170f, endX = left + 360f)
        drawP3Field("প্লট নং :", form.guarantorPlotRoad, startX = left + 370f, endX = right)
        y += 16f
        drawP3Field("ডাকঘর :", form.guarantorPostOffice, startX = left, endX = left + 160f)
        drawP3Field("পোস্ট কোড :", form.guarantorPostCode, startX = left + 170f, endX = left + 330f)
        drawP3Field("থানা :", form.guarantorThana, startX = left + 340f, endX = right)
        y += 16f
        drawP3Field("উপজেলা :", form.guarantorUpazila, startX = left, endX = left + 240f)
        val gDistStr = form.guarantorDistrict
        drawP3Field("জেলা :", gDistStr, startX = left + 250f, endX = right)

        drawPageFooter(canvas, 3, 3)
    }

    private fun getFillDots(startX: Float, endX: Float, paint: Paint): String {
        val avail = endX - startX
        if (avail <= 0f) return ""
        val dotW = paint.measureText(".")
        val count = (avail / dotW).toInt()
        return ".".repeat(maxOf(0, count))
    }

    // -------------------------------------------------------------------------
    // DRAW HELPERS
    // -------------------------------------------------------------------------
    private fun drawRrfOfficialHeader(canvas: Canvas, startY: Float) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.color = Color.BLACK

        paint.textSize = 18f
        paint.typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("রুরাল রিকনস্ট্রাকশন ফাউন্ডেশন", (PAGE_WIDTH / 2).toFloat(), startY + 20f, paint)

        paint.textSize = 15f
        canvas.drawText("Rural Reconstruction Foundation", (PAGE_WIDTH / 2).toFloat(), startY + 40f, paint)

        paint.textSize = 8.5f
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        canvas.drawText("A National Level Non-Governmental Voluntary Development Organization for the underprivileged", (PAGE_WIDTH / 2).toFloat(), startY + 56f, paint)
        canvas.drawText("Reg. No. DSS: Jashore-24/85, Societies Act: Khulna-84, FD: 284/89, MRA-01372-00199-00026", (PAGE_WIDTH / 2).toFloat(), startY + 68f, paint)

        // Divider
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        canvas.drawLine(MARGIN, startY + 76f, PAGE_WIDTH - MARGIN, startY + 76f, paint)
    }

    private fun drawRrfOfficialFooter(canvas: Canvas) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val y = PAGE_HEIGHT - 65f

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 0.8f
        paint.color = Color.DKGRAY
        canvas.drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, paint)

        paint.style = Paint.Style.FILL
        paint.textSize = 7.5f
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        paint.textAlign = Paint.Align.LEFT
        paint.color = Color.BLACK

        val left = MARGIN + 5f
        canvas.drawText("Head Office     : RRF Bhaban, C&B Road, Karbala, P.O. Box #07, Jashore-7400, Bangladesh. Phone: 88 024777-66357, 65704", left, y + 14f, paint)
        canvas.drawText("                       Mobile: 01733 073076, 01733 075095, Fax: 88 024777-60090, e-mail: info@rrf-bd.org, web: www.rrf-bd.org", left, y + 25f, paint)
        canvas.drawText("Dhaka Office   : Office Space No: A-7 (7th Floor), Rupayan Shopping Square, Plot No: C-2, Block-G, Bashundhara R/A, Dhaka.", left, y + 36f, paint)
        canvas.drawText("Training Center: Ramnagar, P.O Rajarhat, Jashore, Bangladesh. Phone: 88 024777-60203, Mobile: 01711 182334, 01714 341593", left, y + 47f, paint)
    }

    private fun drawPageFooter(canvas: Canvas, pageNum: Int, totalPages: Int) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.textSize = 8.5f
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        paint.textAlign = Paint.Align.CENTER
        paint.color = Color.DKGRAY
        canvas.drawText("পৃষ্ঠা $pageNum থেকে $totalPages", (PAGE_WIDTH / 2).toFloat(), PAGE_HEIGHT - 22f, paint)
    }

    private fun drawGenericTable(
        canvas: Canvas,
        left: Float,
        top: Float,
        right: Float,
        colWidths: FloatArray,
        headers: Array<String>,
        rows: List<Array<String>>,
        rowHeight: Float,
        maxRows: Int
    ): Float {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        var curY = top
        val totalTableWidth = right - left

        val sumWidths = colWidths.sum()
        val scaledWidths = if (sumWidths > 0f) {
            colWidths.map { (it / sumWidths) * totalTableWidth }.toFloatArray()
        } else colWidths

        // Header
        val headerH = 22f
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 0.8f
        paint.color = Color.BLACK
        canvas.drawRect(left, curY, right, curY + headerH, paint)

        var cx = left
        for (i in scaledWidths.indices) {
            val w = scaledWidths[i]
            if (i > 0) canvas.drawLine(cx, curY, cx, curY + headerH, paint)

            paint.style = Paint.Style.FILL
            paint.textSize = 7.8f
            paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            paint.textAlign = Paint.Align.CENTER
            val headerText = headers.getOrElse(i) { "" }
            val maxTextW = w - 4f
            val measuredW = paint.measureText(headerText)
            if (measuredW > maxTextW && headerText.contains("/")) {
                val parts = headerText.split("/")
                canvas.drawText(parts[0], cx + (w / 2), curY + 10f, paint)
                if (parts.size > 1) canvas.drawText(parts[1], cx + (w / 2), curY + 19f, paint)
            } else {
                canvas.drawText(headerText, cx + (w / 2), curY + 14f, paint)
            }
            cx += w
        }
        curY += headerH

        // Rows
        for (r in 0 until maxRows) {
            val rowData = rows.getOrNull(r)
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 0.8f
            canvas.drawRect(left, curY, right, curY + rowHeight, paint)

            cx = left
            for (c in scaledWidths.indices) {
                val w = scaledWidths[c]
                if (c > 0) canvas.drawLine(cx, curY, cx, curY + rowHeight, paint)

                paint.style = Paint.Style.FILL
                paint.textSize = 8f
                paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
                paint.textAlign = if (c == 0) Paint.Align.CENTER else Paint.Align.LEFT

                val cellText = if (c == 0) "${r + 1}" else rowData?.getOrNull(c - 1) ?: ""
                val cellClipLeft = cx + 2f
                val cellClipRight = cx + w - 2f
                val drawX = if (c == 0) cx + (w / 2) else cx + 4f
                val drawY = curY + (rowHeight / 2) + 3f

                if (cellText.isNotEmpty()) {
                    canvas.save()
                    canvas.clipRect(cellClipLeft, curY + 1f, cellClipRight, curY + rowHeight - 1f)
                    canvas.drawText(cellText, drawX, drawY, paint)
                    canvas.restore()
                }

                cx += w
            }
            curY += rowHeight
        }
        return curY
    }

    private fun drawWrappedText(
        canvas: Canvas,
        text: String,
        x: Float,
        startY: Float,
        maxWidth: Float,
        textSize: Float,
        lineHeight: Float
    ): Float {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.textSize = textSize
        paint.color = Color.BLACK

        var curY = startY
        val paragraphs = text.split("\n")
        for (paragraph in paragraphs) {
            if (paragraph.isEmpty()) {
                curY += lineHeight
                continue
            }
            val words = paragraph.split(" ")
            var currentLine = ""
            for (word in words) {
                val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
                val width = paint.measureText(testLine)
                if (width > maxWidth) {
                    canvas.drawText(currentLine, x, curY, paint)
                    curY += lineHeight
                    currentLine = word
                } else {
                    currentLine = testLine
                }
            }
            if (currentLine.isNotEmpty()) {
                canvas.drawText(currentLine, x, curY, paint)
                curY += lineHeight
            }
        }
        return curY
    }

    // -------------------------------------------------------------------------
    // 6. GENERATE INFORMATION VERIFICATION FORM PDF (2 Pages)
    // -------------------------------------------------------------------------
    fun generateVerificationFormPdf(context: Context, form: VerificationFormState, allForms: AllAgreementForms? = null): File {
        val pdfDocument = PdfDocument()

        // Page 1
        val pageInfo1 = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val page1 = pdfDocument.startPage(pageInfo1)
        drawVerificationPage1(page1.canvas, form, allForms)
        pdfDocument.finishPage(page1)

        // Page 2
        val pageInfo2 = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 2).create()
        val page2 = pdfDocument.startPage(pageInfo2)
        drawVerificationPage2(page2.canvas, form)
        pdfDocument.finishPage(page2)

        val file = File(context.cacheDir, "Information_Verification_Form.pdf")
        if (file.exists()) file.delete()
        FileOutputStream(file).use { fos ->
            pdfDocument.writeTo(fos)
            fos.flush()
        }
        pdfDocument.close()
        return file
    }

    private fun drawVerificationPage1(canvas: Canvas, form: VerificationFormState, allForms: AllAgreementForms? = null) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        canvas.drawColor(Color.WHITE)

        val left = 40f
        val right = 555f
        val contentWidth = right - left

        paint.textSize = 10f
        paint.textAlign = Paint.Align.RIGHT
        paint.color = Color.BLACK
        canvas.drawText("RRF", right, 36f, paint)

        paint.textSize = 11.5f
        paint.typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("আরআরএফ-এ কর্মরত কর্মীর পরিচিত/আত্মীয় এমন ২ জন ব্যক্তি সংক্রান্ত (যদি থাকে) :", (PAGE_WIDTH / 2).toFloat(), 55f, paint)

        var y = 80f

        fun drawField(label: String, value: String, lineY: Float, startX: Float = left, endX: Float = right) {
            paint.textSize = 9.5f
            paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            paint.textAlign = Paint.Align.LEFT
            paint.color = Color.BLACK
            canvas.drawText(label, startX, lineY, paint)
            val labelW = paint.measureText(label)
            val valX = startX + labelW + 4f
            val availWidth = endX - valX
            if (availWidth > 0f) {
                canvas.save()
                canvas.clipRect(valX, lineY - 14f, endX, lineY + 6f)
                // Draw continuous dotted line across the whole field
                paint.color = Color.rgb(130, 130, 130)
                canvas.drawText(getFillDots(valX, endX, paint), valX, lineY, paint)
                // If value exists, draw text slightly above dots (no solid underline)
                if (value.isNotBlank()) {
                    paint.color = Color.BLACK
                    paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                    canvas.drawText(value, valX, lineY - 2.5f, paint)
                }
                canvas.restore()
            }
        }

        if (form.noRrfRelative) {
            paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            paint.textSize = 10f
            paint.textAlign = Paint.Align.LEFT
            canvas.drawText("• আরআরএফ-এ আমার কোনো পরিচিত/আত্মীয় কর্মরত নেই (I have none)", left, y, paint)
            y += 35f
        } else {
            // Relative 1 Block
            paint.typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            paint.textSize = 11f
            paint.textAlign = Paint.Align.CENTER
            canvas.drawText("আরআরএফ-এ কর্মরত পরিচিত/আত্মীয় - ১", (PAGE_WIDTH / 2).toFloat(), y, paint)
            y += 20f

            drawField("আরআরএফ-এ কর্মরত আপনার পরিচিত/আত্মীয় কর্মীর নাম :", form.rel1Name, y, left, right)
            y += 20f
            drawField("পিতার নাম :", form.rel1Father, y, left, left + 260f)
            drawField("সম্পর্ক :", form.effectiveRel1Relationship, y, left + 270f, left + 410f)
            drawField("পিন :", form.rel1Pin, y, left + 420f, right)
            y += 20f
            drawField("পদবী :", form.effectiveRel1Designation, y, left, left + 320f)
            drawField("বর্তমান কর্মস্থল :", form.rel1Workplace, y, left + 330f, right)
            y += 28f

            // Relative 2 Block
            paint.typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            paint.textSize = 11f
            paint.textAlign = Paint.Align.CENTER
            canvas.drawText("আরআরএফ-এ কর্মরত পরিচিত/আত্মীয় - ২", (PAGE_WIDTH / 2).toFloat(), y, paint)
            y += 20f

            drawField("আরআরএফ-এ কর্মরত আপনার পরিচিত/আত্মীয় কর্মীর নাম :", form.rel2Name, y, left, right)
            y += 20f
            drawField("পিতার নাম :", form.rel2Father, y, left, left + 260f)
            drawField("সম্পর্ক :", form.effectiveRel2Relationship, y, left + 270f, left + 410f)
            drawField("পিন :", form.rel2Pin, y, left + 420f, right)
            y += 20f
            drawField("পদবী :", form.effectiveRel2Designation, y, left, left + 320f)
            drawField("বর্তমান কর্মস্থল :", form.rel2Workplace, y, left + 330f, right)
            y += 28f
        }

        // Chairman Section
        paint.typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        paint.textSize = 11f
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("কর্মীর ইউনিয়ন পরিষদ/পৌরসভার চেয়ারম্যান সংক্রান্ত :", (PAGE_WIDTH / 2).toFloat(), y, paint)
        y += 20f

        drawField("চেয়ারম্যানের নাম :", form.chairmanName, y, left, right)
        y += 20f
        drawField("ইউনিয়ন পরিষদ/পৌরসভার নাম :", form.unionName, y, left, right)
        y += 20f
        val chAddress = form.chairmanAddress.ifBlank {
            listOf(
                if (form.chairmanVillage.isNotBlank()) "গ্রাম: ${form.chairmanVillage}" else "",
                if (form.chairmanPostOffice.isNotBlank()) "ডাকঘর: ${form.chairmanPostOffice}" else "",
                if (form.chairmanThana.isNotBlank()) "থানা: ${form.chairmanThana}" else "",
                if (form.chairmanDistrict.isNotBlank()) "জেলা: ${form.chairmanDistrict}" else ""
            ).filter { it.isNotBlank() }.joinToString(", ")
        }
        drawField("চেয়ারম্যানের ঠিকানা :", chAddress, y, left, right)

        y += 28f

        // Undertaking (মুচলেকা) Section
        paint.typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        paint.textSize = 11f
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("মুচলেকা (কর্মী কর্তৃক পূরণীয়) :", (PAGE_WIDTH / 2).toFloat(), y, paint)
        y += 18f

        paint.textSize = 9f
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        
        val effectiveStaffName = form.staffFullName.ifBlank {
            allForms?.personalInfoForm?.employeeNameBangla?.ifBlank {
                allForms.trainingForm.employeeName.ifBlank {
                    allForms.idCardForm.employeeName
                }
            }.orEmpty()
        }
        val effectiveStaffFather = form.staffFatherName.ifBlank {
            allForms?.personalInfoForm?.fatherName?.ifBlank {
                allForms.trainingForm.fatherName
            }.orEmpty()
        }

        val sName = effectiveStaffName.ifBlank { "..........................................................." }
        val sFather = effectiveStaffFather.ifBlank { "..........................................................." }
        val mText = "আমি নিম্নস্বাক্ষরকারী $sName, পিতার নাম : $sFather স্বজ্ঞানে শপথ করছি যে, আমার দেয়া উপর্যুক্ত যাবতীয় তথ্যাবলী সত্য ও নির্ভুল। আরআরএফ-এ যোগদানের পর ভবিষ্যতে যে কোন সময় উপর্যুক্ত তথ্যাবলী ভুল বা অসত্য প্রমাণিত হলে সংস্থা আমার বিরুদ্ধে যে কোন আইনানুগ ব্যবস্থা গ্রহণ করতে পারবে সে ক্ষেত্রে আমার কোন আপত্তি থাকবে না।"
        y = drawWrappedText(canvas, mText, left, y, contentWidth, 9f, 14f) + 16f

        // Thumbprint & Signature
        val rightBoxX = right - 145f
        val ovalCenterX = rightBoxX + 70f
        val ovalWidth = 120f
        val ovalHeight = 78f
        val ovalTop = y + 14f
        val ovalBottom = ovalTop + ovalHeight
        val ovalRect = RectF(
            ovalCenterX - (ovalWidth / 2f),
            ovalTop,
            ovalCenterX + (ovalWidth / 2f),
            ovalBottom
        )

        // Draw horizontal flattened circle (আড়ে চ্যাপ্টা ডিম্বাকৃতি / Horizontal Oval) for thumb impression
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        paint.color = Color.BLACK
        canvas.drawOval(ovalRect, paint)

        // Label below oval
        paint.style = Paint.Style.FILL
        paint.textSize = 8.5f
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("কর্মীর বৃদ্ধাঙ্গুলের ছাপ", ovalCenterX, ovalBottom + 14f, paint)

        // Signature, Date, Name and Father Name on the left with clean spacing
        val sigY = y + 20f
        paint.textAlign = Paint.Align.LEFT
        paint.textSize = 9.5f
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        canvas.drawText("কর্মীর স্বাক্ষর ও তারিখ : ${form.staffSignatureDate.ifBlank { ".................................." }}", left, sigY, paint)

        // Move employee's full name and father's name down cleanly below signature
        var fieldY = sigY + 34f
        drawField("কর্মীর পূর্ণ নাম :", effectiveStaffName, fieldY, left, rightBoxX - 15f)
        fieldY += 24f
        drawField("কর্মীর পিতার নাম :", effectiveStaffFather, fieldY, left, rightBoxX - 15f)

        drawPageFooter(canvas, 1, 2)
    }

    private fun drawVerificationPage2(canvas: Canvas, form: VerificationFormState) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        canvas.drawColor(Color.WHITE)

        val left = 40f
        val right = 555f
        val contentWidth = right - left

        // 1. Top Right "RRF"
        paint.textSize = 10f
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        paint.textAlign = Paint.Align.RIGHT
        paint.color = Color.BLACK
        canvas.drawText("RRF", right, 36f, paint)

        // 2. Header (Centered)
        paint.textSize = 11.5f
        paint.typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("কর্মীর প্রতিবেশী সংক্রান্ত :", (PAGE_WIDTH / 2).toFloat(), 55f, paint)
        canvas.drawText("কর্মীর প্রতিবেশী - ১", (PAGE_WIDTH / 2).toFloat(), 75f, paint)

        // Helper for dotted field
        fun drawDottedField(label: String, value: String, lineY: Float, startX: Float, endX: Float) {
            paint.textSize = 9.5f
            paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            paint.textAlign = Paint.Align.LEFT
            paint.color = Color.BLACK
            canvas.drawText(label, startX, lineY, paint)
            val labelW = paint.measureText(label)
            val valX = startX + labelW + 3f
            val availWidth = endX - valX
            if (availWidth > 0f) {
                canvas.save()
                canvas.clipRect(valX, lineY - 14f, endX, lineY + 6f)
                paint.color = Color.rgb(130, 130, 130)
                canvas.drawText(getFillDots(valX, endX, paint), valX, lineY, paint)
                if (value.isNotBlank()) {
                    paint.color = Color.BLACK
                    paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                    canvas.drawText(value, valX, lineY - 2.5f, paint)
                }
                canvas.restore()
            }
        }

        // Helper for drawing a neighbor section
        fun drawNeighborBlock(
            headerY: Float,
            name: String,
            profession: String,
            father: String,
            mobile: String,
            village: String,
            postOffice: String,
            postal: String,
            upazila: String,
            police: String,
            district: String
        ): Float {
            var cy = headerY + 24f

            // Line 1: প্রতিবেশীর পূর্ণ নাম :..................... পেশাঃ.....................
            drawDottedField("প্রতিবেশীর পূর্ণ নাম :", name, cy, left, left + 340f)
            drawDottedField("পেশাঃ", profession, cy, left + 348f, right)
            cy += 20f

            // Line 2: পিতার নাম :..................... মোবাইল নং :.....................
            drawDottedField("পিতার নাম :", father, cy, left, left + 270f)
            drawDottedField("মোবাইল নং :", BanglaTextValidator.toBanglaDigits(mobile), cy, left + 278f, right)
            cy += 20f

            // Line 3: গ্রাম :..................... ডাকঘর :..................... পোস্ট কোড :.....................
            drawDottedField("গ্রাম :", village, cy, left, left + 215f)
            drawDottedField("ডাকঘর :", postOffice, cy, left + 222f, left + 380f)
            drawDottedField("পোস্ট কোড :", BanglaTextValidator.toBanglaDigits(postal), cy, left + 388f, right)
            cy += 20f

            // Line 4: উপজেলা :..................... থানা :..................... জেলা :..................... বাংলাদেশ।
            val bdText = "বাংলাদেশ।"
            paint.textSize = 9.5f
            paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            val bdW = paint.measureText(bdText)
            drawDottedField("উপজেলা :", upazila, cy, left, left + 140f)
            drawDottedField("থানা :", police, cy, left + 145f, left + 290f)
            drawDottedField("জেলা :", district, cy, left + 295f, right - bdW - 4f)
            paint.color = Color.BLACK
            paint.textAlign = Paint.Align.RIGHT
            canvas.drawText(bdText, right, cy, paint)
            cy += 14f

            // Box for remarks
            val boxTop = cy
            val boxHeight = 56f
            val boxBottom = boxTop + boxHeight
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 0.8f
            paint.color = Color.BLACK
            canvas.drawRect(left, boxTop, right, boxBottom, paint)

            // Center text inside box at top
            paint.style = Paint.Style.FILL
            paint.textSize = 8.5f
            paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            paint.textAlign = Paint.Align.CENTER
            canvas.drawText("মন্তব্য : (তদন্তকারী কর্মকর্তা কর্তৃক পূরণীয়)", (PAGE_WIDTH / 2).toFloat(), boxTop + 13f, paint)

            return boxBottom
        }

        // Draw Neighbor 1
        val n1BoxBottom = drawNeighborBlock(
            headerY = 75f,
            name = form.neigh1Name,
            profession = form.neigh1Profession,
            father = form.neigh1Father,
            mobile = form.neigh1Mobile,
            village = form.neigh1Village,
            postOffice = form.neigh1PostOffice,
            postal = form.neigh1Postal,
            upazila = form.neigh1Upazila,
            police = form.neigh1Police,
            district = form.neigh1District
        )

        // Draw Neighbor 2 Header
        val n2HeaderY = n1BoxBottom + 26f
        paint.textSize = 11.5f
        paint.typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        paint.textAlign = Paint.Align.CENTER
        paint.color = Color.BLACK
        canvas.drawText("কর্মীর প্রতিবেশী - ২", (PAGE_WIDTH / 2).toFloat(), n2HeaderY, paint)

        // Draw Neighbor 2
        val n2BoxBottom = drawNeighborBlock(
            headerY = n2HeaderY,
            name = form.neigh2Name,
            profession = form.neigh2Profession,
            father = form.neigh2Father,
            mobile = form.neigh2Mobile,
            village = form.neigh2Village,
            postOffice = form.neigh2PostOffice,
            postal = form.neigh2Postal,
            upazila = form.neigh2Upazila,
            police = form.neigh2Police,
            district = form.neigh2District
        )

        // 3. Two lines below Neighbor 2
        var optY = n2BoxBottom + 24f
        paint.textSize = 9f
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        paint.textAlign = Paint.Align.LEFT
        paint.color = Color.BLACK

        val unitLabel1 = "নিয়োগ গ্রহণকারীর আবাসস্থলের নিকটবর্তী ইউনিট অফিস থাকলে তার নাম"
        canvas.drawText(unitLabel1, left, optY, paint)
        val unitLabel1W = paint.measureText(unitLabel1)

        val unitLabel2 = " ও পরিবারের কেউ উক্ত অফিসের"
        val unitLabel2W = paint.measureText(unitLabel2)
        val unitLabel2X = right - unitLabel2W
        canvas.drawText(unitLabel2, unitLabel2X, optY, paint)

        val dotsStartX = left + unitLabel1W + 2f
        val dotsEndX = unitLabel2X - 2f
        if (dotsEndX > dotsStartX) {
            canvas.save()
            canvas.clipRect(dotsStartX, optY - 14f, dotsEndX, optY + 6f)
            paint.color = Color.rgb(130, 130, 130)
            canvas.drawText(getFillDots(dotsStartX, dotsEndX, paint), dotsStartX, optY, paint)
            if (form.nearbyUnitOffice.isNotBlank()) {
                paint.color = Color.BLACK
                paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                canvas.drawText(form.nearbyUnitOffice, dotsStartX, optY, paint)
            }
            canvas.restore()
        }

        optY += 18f
        drawDottedField("সদস্য হলে তার নাম", form.familyMemberInUnitOffice, optY, left, right)

        // 4. Dashed divider: "--------------------- অফিস কর্তৃক পূরণীয় ---------------------"
        val dividerY = optY + 26f
        val officeTitle = "অফিস কর্তৃক পূরণীয়"
        paint.textSize = 10f
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        paint.textAlign = Paint.Align.CENTER
        paint.color = Color.BLACK
        val titleW = paint.measureText(officeTitle)

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 0.8f
        paint.pathEffect = DashPathEffect(floatArrayOf(4f, 3f), 0f)
        val pathLeft = Path().apply {
            moveTo(left, dividerY - 3f)
            lineTo((PAGE_WIDTH / 2) - (titleW / 2f) - 6f, dividerY - 3f)
        }
        canvas.drawPath(pathLeft, paint)
        val pathRight = Path().apply {
            moveTo((PAGE_WIDTH / 2) + (titleW / 2f) + 6f, dividerY - 3f)
            lineTo(right, dividerY - 3f)
        }
        canvas.drawPath(pathRight, paint)
        paint.pathEffect = null

        paint.style = Paint.Style.FILL
        canvas.drawText(officeTitle, (PAGE_WIDTH / 2).toFloat(), dividerY, paint)

        // Subtitle under office divider
        val subY = dividerY + 14f
        paint.textSize = 8f
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        canvas.drawText(
            "(উপর্যুক্ত তথ্যের আলোকে তদন্তকারী কর্মকর্তার যদি কোন মন্তব্য থাকে তাহলে তা নিম্নের ফাঁকা ঘরে সবিস্তারে উল্লেখ করতে হবে)",
            (PAGE_WIDTH / 2).toFloat(),
            subY,
            paint
        )

        // Office Remarks Box
        val officeBoxTop = subY + 8f
        val officeBoxHeight = 58f
        val officeBoxBottom = officeBoxTop + officeBoxHeight
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 0.8f
        paint.color = Color.BLACK
        canvas.drawRect(left, officeBoxTop, right, officeBoxBottom, paint)

        if (form.investigationOfficerComment.isNotBlank()) {
            paint.style = Paint.Style.FILL
            paint.textSize = 9f
            paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            paint.textAlign = Paint.Align.LEFT
            drawWrappedText(canvas, form.investigationOfficerComment, left + 8f, officeBoxTop + 14f, contentWidth - 16f, 9f, 15f)
        }

        // 5. Signatures (3 Columns)
        val sigHeaderY = officeBoxBottom + 30f
        val colW = contentWidth / 3f

        paint.style = Paint.Style.FILL
        paint.color = Color.BLACK
        paint.textAlign = Paint.Align.LEFT

        fun drawSignatureCol(title: String, nameVal: String, dateVal: String, startColX: Float, lineW: Float) {
            paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            paint.textSize = 9.5f
            canvas.drawText(title, startColX, sigHeaderY, paint)

            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 0.8f
            canvas.drawLine(startColX, sigHeaderY + 4f, startColX + lineW, sigHeaderY + 4f, paint)
            paint.style = Paint.Style.FILL

            var sy = sigHeaderY + 18f
            paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            paint.textSize = 9f

            val nameStr = if (nameVal.isNotBlank()) "$nameVal " + ".".repeat(maxOf(2, 28 - nameVal.length)) else "...................................."
            canvas.drawText("নাম : $nameStr", startColX, sy, paint)
            sy += 15f

            val dateStr = if (dateVal.isNotBlank()) BanglaTextValidator.toBanglaDigits(dateVal) else ""
            canvas.drawText("তারিখ : $dateStr", startColX, sy, paint)
            sy += 15f

            canvas.drawText("সীল : ", startColX, sy, paint)
        }

        val colLineW = 145f
        drawSignatureCol("তদন্তকারী কর্মকর্তা", form.investigatingOfficerName, form.investigatingOfficerDate, left, colLineW)
        drawSignatureCol("যাচাইকারী কর্মকর্তা", form.verifyingOfficerName, form.verifyingOfficerDate, left + colW, colLineW)
        drawSignatureCol("অনুমোদনকারী", form.approvingAuthorityName, form.approvingAuthorityDate, left + (colW * 2f), colLineW)

        // 6. Page Footer
        paint.textAlign = Paint.Align.CENTER
        paint.textSize = 8.5f
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        paint.color = Color.BLACK
        canvas.drawText("পৃষ্ঠা ৫ থেকে ৫", (PAGE_WIDTH / 2).toFloat(), 725f, paint)
    }

    // -------------------------------------------------------------------------
    // 7. GENERATE NEIGHBOR CERTIFICATION PDF (কর্মীর প্রতিবেশী ১ ও ২ এর তথ্য)
    // -------------------------------------------------------------------------
    fun generateNeighborCertificationPdf(
        context: Context,
        form: VerificationFormState,
        pInfo: PersonalInfoFormState? = null
    ): File {
        val pdfDocument = PdfDocument()

        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        drawVerificationPage2(page.canvas, form)
        pdfDocument.finishPage(page)

        val empName = pInfo?.employeeNameBangla?.trim()?.ifBlank { "কর্মীর_নাম" } ?: "কর্মীর_নাম"
        val safeName = empName.replace("[/\\\\:*?\"<>|]".toRegex(), "").replace("\\s+".toRegex(), "_")
        val file = File(context.cacheDir, "${safeName}_তথ্য_যাচাই_ফরম.pdf")
        if (file.exists()) file.delete()
        FileOutputStream(file).use { fos ->
            pdfDocument.writeTo(fos)
            fos.flush()
        }
        pdfDocument.close()
        return file
    }


}
