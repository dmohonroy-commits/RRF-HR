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
import com.example.data.AllAgreementForms
import com.example.data.AgreementEntity
import java.io.File
import java.io.FileOutputStream

object Attestation25TkPdfGenerator {

    // Legal Paper: 8.5 x 14 inches = 612 x 1008 points (Standard for Bangladesh Non-Judicial Stamps)
    const val PAGE_WIDTH = 612
    const val PAGE_HEIGHT = 1008
    const val MARGIN_HORIZONTAL = 46
    const val MARGIN_TOP_25_TK_STAMP = 280 // Top blank margin for 25 Taka Non-Judicial Stamp Header

    data class CertifierInfo(
        val serialNumber: String,
        val name: String,
        val profession: String,
        val fatherName: String,
        val village: String,
        val postOffice: String,
        val postalCode: String,
        val thana: String,
        val district: String,
        val mobile: String,
        val nid: String
    )

    fun generate25TkAttestationPdf(
        context: Context,
        allForms: AllAgreementForms,
        stampAgr: AgreementEntity? = null
    ): File {
        val document = PdfDocument()

        val textPaint = TextPaint().apply {
            isAntiAlias = true
            color = Color.BLACK
            textSize = 12.0f
            typeface = Typeface.create(Typeface.SERIF, Typeface.NORMAL)
        }

        val boldTextPaint = TextPaint().apply {
            isAntiAlias = true
            color = Color.BLACK
            textSize = 12.0f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        }

        val titlePaint = TextPaint().apply {
            isAntiAlias = true
            color = Color.BLACK
            textSize = 18f
            isFakeBoldText = true
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        }

        val contentWidth = PAGE_WIDTH - (MARGIN_HORIZONTAL * 2)

        // Resolve Worker Data
        val pInfo = allForms.personalInfoForm
        val tForm = allForms.trainingForm
        val vForm = allForms.verificationForm

        val empName = pInfo.employeeNameBangla.ifBlank {
            if (BanglaTextValidator.containsBengali(tForm.employeeName)) tForm.employeeName
            else if (BanglaTextValidator.containsBengali(vForm.staffFullName)) vForm.staffFullName
            else stampAgr?.employeeName.orEmpty()
        }

        val empFather = pInfo.fatherName.ifBlank {
            if (BanglaTextValidator.containsBengali(tForm.fatherName)) tForm.fatherName
            else if (BanglaTextValidator.containsBengali(vForm.staffFatherName)) vForm.staffFatherName
            else stampAgr?.employeeFatherName.orEmpty()
        }

        val eMotherRaw = pInfo.motherName.ifBlank {
            pInfo.familyMembers.firstOrNull { it.relationship == "মাতা" }?.name.orEmpty().ifBlank {
                if (stampAgr != null && stampAgr.guarantorMotherName.isNotBlank() && BanglaTextValidator.isValidBanglaName(stampAgr.guarantorMotherName)) stampAgr.guarantorMotherName
                else ""
            }
        }
        val empMother = if (BanglaTextValidator.isValidBanglaName(eMotherRaw)) eMotherRaw else ""

        val empVillage = pInfo.permVillage.ifBlank {
            pInfo.currVillageOrHouse.ifBlank {
                tForm.village.ifBlank { stampAgr?.employeeVillage.orEmpty() }
            }
        }

        val empPost = pInfo.permPostOffice.ifBlank {
            pInfo.currPostOffice.ifBlank {
                tForm.postOffice.ifBlank { stampAgr?.employeePostOffice.orEmpty() }
            }
        }

        val empThana = pInfo.permUpazila.ifBlank {
            pInfo.permThana.ifBlank {
                tForm.thana.ifBlank { stampAgr?.employeeUpazila.orEmpty() }
            }
        }

        val empDistrict = pInfo.permDistrict.ifBlank {
            tForm.district.ifBlank { stampAgr?.employeeDistrict.orEmpty() }
        }

        // Neighbor 1
        val certifier1 = CertifierInfo(
            serialNumber = "১",
            name = vForm.neigh1Name.ifBlank { pInfo.neighbor1.name },
            profession = vForm.neigh1Profession.ifBlank { pInfo.neighbor1.occupation },
            fatherName = vForm.neigh1Father.ifBlank { pInfo.neighbor1.fatherName },
            village = vForm.neigh1Village.ifBlank { pInfo.neighbor1.village.ifBlank { empVillage } },
            postOffice = vForm.neigh1PostOffice.ifBlank { pInfo.neighbor1.postOffice.ifBlank { empPost } },
            postalCode = vForm.neigh1Postal.ifBlank { pInfo.neighbor1.postCode },
            thana = vForm.neigh1Upazila.ifBlank { vForm.neigh1Police.ifBlank { pInfo.neighbor1.upazila.ifBlank { pInfo.neighbor1.thana.ifBlank { empThana } } } },
            district = vForm.neigh1District.ifBlank { pInfo.neighbor1.district.ifBlank { empDistrict } },
            mobile = vForm.neigh1Mobile.ifBlank { pInfo.neighbor1.mobileNumber },
            nid = vForm.neigh1Nid
        )

        // Neighbor 2
        val certifier2 = CertifierInfo(
            serialNumber = "২",
            name = vForm.neigh2Name.ifBlank { pInfo.neighbor2.name },
            profession = vForm.neigh2Profession.ifBlank { pInfo.neighbor2.occupation },
            fatherName = vForm.neigh2Father.ifBlank { pInfo.neighbor2.fatherName },
            village = vForm.neigh2Village.ifBlank { pInfo.neighbor2.village.ifBlank { empVillage } },
            postOffice = vForm.neigh2PostOffice.ifBlank { pInfo.neighbor2.postOffice.ifBlank { empPost } },
            postalCode = vForm.neigh2Postal.ifBlank { pInfo.neighbor2.postCode },
            thana = vForm.neigh2Upazila.ifBlank { vForm.neigh2Police.ifBlank { pInfo.neighbor2.upazila.ifBlank { pInfo.neighbor2.thana.ifBlank { empThana } } } },
            district = vForm.neigh2District.ifBlank { pInfo.neighbor2.district.ifBlank { empDistrict } },
            mobile = vForm.neigh2Mobile.ifBlank { pInfo.neighbor2.mobileNumber },
            nid = vForm.neigh2Nid
        )

        val certifiersList = listOf(certifier1, certifier2)

        certifiersList.forEachIndexed { index, certifier ->
            val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, index + 1).create()
            val page = document.startPage(pageInfo)
            val canvas: Canvas = page.canvas

            var yPos = MARGIN_TOP_25_TK_STAMP.toFloat()

            // 1. Centered Title: প্রত্যয়ন পত্র
            val titleText = "প্রত্যয়ন পত্র"
            val titleWidth = titlePaint.measureText(titleText)
            val titleX = (PAGE_WIDTH - titleWidth) / 2f
            canvas.drawText(titleText, titleX, yPos + 18f, titlePaint)
            yPos += 45f

            // 2. Main Attestation Text
            val eNameVal = empName.ifBlank { "........................................................" }
            val eFatherVal = empFather.ifBlank { "............................................" }
            val eMotherVal = empMother.ifBlank { "............................................" }
            val eVillageVal = empVillage.ifBlank { "...................................." }
            val ePostVal = empPost.ifBlank { "...................................." }
            val eThanaVal = empThana.ifBlank { "...................................." }
            val eDistrictVal = empDistrict.ifBlank { "...................................." }

            val mainBody = "আমি এই মর্মে প্রত্যয়ন করিতেছি যে, নাম: $eNameVal, পিতার নাম: $eFatherVal, মাতার নাম: $eMotherVal, ঠিকানা-গ্রাম: $eVillageVal, পোস্ট: $ePostVal, থানা: $eThanaVal, জেলা: $eDistrictVal কে আমি ব্যক্তিগত ভাবে চিনি ও জানি। আমার জানা মতে সে কোন অসামাজিক ও অনৈতিক কার্যক্রমের সাথে যুক্ত নয়। উক্ত ব্যক্তি দ্বারা কোন অনিয়ম বা সমাজ ও রাষ্ট্রবিরোধী কোন কার্যক্রম সংঘটিত হলে আমি তার দায়িত্ব গ্রহণ করিলাম।"

            val bodyHeight = drawJustifiedParagraph(
                canvas = canvas,
                text = mainBody,
                x = MARGIN_HORIZONTAL.toFloat(),
                y = yPos,
                width = contentWidth,
                paint = textPaint,
                lineSpacingExtra = 10f
            )

            yPos += bodyHeight + 50f

            // 3. Certifier Signature Block on Right
            val blockLeft = PAGE_WIDTH - MARGIN_HORIZONTAL - 240f
            val dotLine = ".................................................."

            val sigFields = listOf(
                "স্বাক্ষর :" to (if (certifier.name.isNotBlank()) "" else dotLine),
                "নাম :" to certifier.name.ifBlank { dotLine },
                "পেশা :" to certifier.profession.ifBlank { dotLine },
                "ঠিকানা-গ্রাম :" to certifier.village.ifBlank { dotLine },
                "পোস্ট :" to certifier.postOffice.ifBlank { dotLine },
                "থানা :" to certifier.thana.ifBlank { dotLine },
                "জেলা :" to certifier.district.ifBlank { dotLine },
                "মোবাইল নং :" to (if (certifier.mobile.isNotBlank()) BanglaTextValidator.toBanglaDigits(certifier.mobile) else dotLine),
                "জাতীয় পরিচয়পত্র নং :" to (if (certifier.nid.isNotBlank()) BanglaTextValidator.toBanglaDigits(certifier.nid) else dotLine)
            )

            for ((label, valText) in sigFields) {
                canvas.drawText(label, blockLeft, yPos, boldTextPaint)
                val labelWidth = boldTextPaint.measureText(label) + 6f
                canvas.drawText(valText, blockLeft + labelWidth, yPos, textPaint)
                yPos += 24f
            }

            document.finishPage(page)
        }

        val baseName = if (stampAgr != null) {
            stampAgr.stamp25FileName
        } else {
            val empName = allForms.personalInfoForm.employeeNameBangla.trim()
                .ifBlank { allForms.idCardForm.employeeName.trim() }
                .ifBlank { "কর্মীর_নাম" }
            val safeName = empName.replace("[/\\\\:*?\"<>|]".toRegex(), "").replace("\\s+".toRegex(), "_")
            "${safeName}_২৫টাকা_প্রত্যয়ন"
        }

        val outputFile = File(context.cacheDir, "${baseName}.pdf")
        if (outputFile.exists()) outputFile.delete()
        FileOutputStream(outputFile).use { out ->
            document.writeTo(out)
        }
        document.close()
        return outputFile
    }

    private fun drawJustifiedParagraph(
        canvas: Canvas,
        text: String,
        x: Float,
        y: Float,
        width: Int,
        paint: TextPaint,
        lineSpacingExtra: Float = 6f
    ): Float {
        val staticLayout = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            StaticLayout.Builder.obtain(text, 0, text.length, paint, width)
                .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                .setLineSpacing(lineSpacingExtra, 1.0f)
                .setIncludePad(false)
                .build()
        } else {
            @Suppress("DEPRECATION")
            StaticLayout(
                text,
                paint,
                width,
                Layout.Alignment.ALIGN_NORMAL,
                1.0f,
                lineSpacingExtra,
                false
            )
        }

        canvas.save()
        canvas.translate(x, y)
        staticLayout.draw(canvas)
        canvas.restore()

        return staticLayout.height.toFloat()
    }
}
