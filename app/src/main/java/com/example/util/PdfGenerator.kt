package com.example.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import com.example.data.AgreementEntity
import java.io.File
import java.io.FileOutputStream

object PdfGenerator {

    // Legal Paper: 8.5 x 14 inches = 612 x 1008 points
    const val PAGE_WIDTH = 612
    const val PAGE_HEIGHT = 1008
    const val MARGIN_HORIZONTAL = 42
    const val MARGIN_TOP_DEFAULT = 48
    const val MARGIN_TOP_100_TK_STAMP = 324 // Exactly 4.5 inches (4.5 * 72 = 324 points)
    const val MARGIN_BOTTOM = 42

    /**
     * Generates a 3-page Legal size PDF file.
     * @param forStampPaper If true, leaves 4.5 inches top blank margin on each page for 100 Tk non-judicial stamp.
     */
    fun generateAgreementPdf(
        context: Context,
        agreement: AgreementEntity,
        forStampPaper: Boolean = false
    ): File {
        val document = PdfDocument()

        val textPaint = TextPaint().apply {
            isAntiAlias = true
            color = Color.BLACK
            textSize = 11.0f
            typeface = Typeface.create(Typeface.SERIF, Typeface.NORMAL)
        }

        val titlePaint = TextPaint().apply {
            isAntiAlias = true
            color = Color.BLACK
            textSize = 16f
            isFakeBoldText = true
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        }

        val contentWidth = PAGE_WIDTH - (MARGIN_HORIZONTAL * 2)

        // ---------------- PAGE 1 ----------------
        val page1Info = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val page1 = document.startPage(page1Info)
        val canvas1: Canvas = page1.canvas

        var yPos = if (forStampPaper) MARGIN_TOP_100_TK_STAMP.toFloat() else MARGIN_TOP_DEFAULT.toFloat()

        // Page 1 Title (Centered)
        val titleText = "জামানতনামা"
        val titleWidth = titlePaint.measureText(titleText)
        val titleX = (PAGE_WIDTH - titleWidth) / 2f
        canvas1.drawText(titleText, titleX, yPos + 14f, titlePaint)
        yPos += 28f

        // Page 1 Body
        val rel = agreement.effectiveGuarantorRelationship
        val nid = BanglaTextValidator.toBanglaDigits(agreement.guarantorNid)

        val page1Content = buildString {
            append("আমি ${agreement.guarantorName}, পিতা : ${agreement.guarantorFatherName}, মাতা : ${agreement.guarantorMotherName}, সম্পর্ক : ${rel}।\n")
            append("ভোটার আইডি নংঃ $nid\n")
            append("বর্তমান ঠিকানা :\n")
            append("গ্রাম : ${agreement.guarantorVillage}, ডাকঘর : ${agreement.guarantorPostOffice}, উপজেলা : ${agreement.guarantorUpazila}, জেলা : ${agreement.guarantorDistrict}।\n\n")
            append("রুরাল রিকনস্ট্রাকশন ফাউন্ডেশন এ ${agreement.designation} পদে নিয়োজিত জনাব ${agreement.employeeName}, পিতাঃ ${agreement.employeeFatherName}।\n")
            append("গ্রাম : ${agreement.employeeVillage}, ডাকঘর : ${agreement.employeePostOffice}, উপজেলা : ${agreement.employeeUpazila}, জেলা : ${agreement.employeeDistrict}।\n")
            append("এর জন্য এবং তাহার পক্ষে সার্বিক দায়-দায়িত্ব স্বীকার করিয়া জামিনদার হিসাবে নিম্নলিখিত শর্তাবলী সাপেক্ষে অঙ্গীকারবদ্ধ হইলাম :\n\n")
            append("শর্তাবলী :-\n\n")
            append("১। যেহেতু রুরাল রিকনস্ট্রাকশন ফাউন্ডেশন জনাব ${agreement.employeeName} কে ${agreement.designation} পদে চাকুরী প্রদান করিয়াছে সেহেতু আমি জনাব ${agreement.guarantorName} এর জন্য জামিনদার বহাল থাকিয়া রুরাল রিকনস্ট্রাকশন ফাউন্ডেশন বরাবরে অত্র জামানত নামা প্রদান করিলাম।\n\n")
            append("২। রুরাল রিকনস্ট্রাকশন ফাউন্ডেশন এ চাকুরীরত থাকাকালীন উল্লেখিত ব্যক্তির কর্তব্যকাজে অবহেলা, ইচ্ছাকৃত ত্রুটি, স্বীয়-স্বার্থ আদায়ের লক্ষ্যে উদ্দেশ্য প্রণোদিতভাবে কোন কার্য সম্পাদন ফৌজদারী বা দেওয়ানী আইনে শাস্তিযোগ্য অপরাধের দ্বারা প্রত্যক্ষ বা পরোক্ষভাবে রুরাল রিকনস্ট্রাকশন ফাউন্ডেশন এর কোন ক্ষতিসাধন, দেশের প্রচলিত আইন কানুন, নিয়ম শৃঙ্খলা ও বিধি বিধানের পরিপন্থী কোন বে- আইনী বা অনৈতিক কাজে নিজেকে জড়িত করা, রুরাল রিকনস্ট্রাকশন ফাউন্ডেশন এর প্রাতিষ্ঠানিক বা প্রশাসনিক আইন বা বেআইনী বা নিয়ম শৃঙ্খলা পরিপন্থি কোন কর্মকান্ডে জড়িত হওয়া অথবা প্রত্যক্ষ বা পরোক্ষভাবে রুরাল রিকনস্ট্রাকশন ফাউন্ডেশনের আর্থিক ক্ষতি সাধন করা, ব্যাংক হতে নগদ উত্তোলন বা নগদে অথবা নানাবিধ উপায়ে আর্থিক সুবিধা লাভ করা কিংবা অর্থ আত্নসাৎ করা, রুরাল রিকনস্ট্রাকশন ফাউন্ডেশন এর কোন স্থাবর বা অস্থাবর সম্পত্তি বিনষ্ট বা হস্তগত করা কিংবা তার ক্ষতি সাধন করা, রুরাল রিকনস্ট্রাকশন ফাউন্ডেশন এর সুনাম ক্ষুন্ন হইতে পারে এবং রাষ্ট্রবিরোধী কোন কার্যকলাপে জড়িত হওয়া বা প্রত্যক্ষ ও পরোক্ষভাবে ইত্যাদি যে কোন প্রকার কাজে দায়ী বা দোষী হইলে আমি তাহার সকল প্রকার দায় দায়িত্ব নিজে বহন করিব বা করিতে আইনতঃ বাধ্য থাকিব এবং রুরাল রিকনস্ট্রাকশন ফাউন্ডেশন এর দাবীকৃত বা আদালত কর্তৃক ঘোষিত ও নির্ধারিত যে কোন অংকের আর্থিক ক্ষতিপূরণ প্রদানে বাধ্য থাকিব।")
        }

        drawJustifiedParagraph(canvas1, page1Content, MARGIN_HORIZONTAL.toFloat(), yPos, contentWidth, textPaint)
        document.finishPage(page1)

        // ---------------- PAGE 2 (Terms 3, 4, 5) ----------------
        val page2Info = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 2).create()
        val page2 = document.startPage(page2Info)
        val canvas2: Canvas = page2.canvas

        val page2Y = if (forStampPaper) MARGIN_TOP_100_TK_STAMP.toFloat() else (MARGIN_TOP_DEFAULT.toFloat() + 30f)
        val page2Content = buildString {
            append("৩। উল্লেখিত ব্যক্তি সম্পূর্ণ সততা, বিশ্বস্ততা, শৃঙ্খলা ও নিয়মানুবর্তিতার সহিত তাহার উপর অর্পিত দায়িত্ব ও কর্তব্য পালন করিবেন।\n\n")
            append("৪। আমি জামিনদার হিসাবে আমার দায়িত্ব পালন না করিলে বা দায়িত্ব পালনে কোন প্রকার অনীহা প্রকাশ করিলে কিংবা প্রদত্ত অঙ্গীকার ভঙ্গ করিলে আমার বিরুদ্ধে রুরাল রিকনস্ট্রাকশন ফাউন্ডেশন কর্তৃপক্ষ অত্র জামানতনামা বলে আইন অনুযায়ী যে কোন প্রকার ব্যবস্থা গ্রহণ করিতে পারিবেন এবং সে ক্ষেত্রে আমার কোন প্রকার ওজর আপত্তি সর্ব আদালতে অগ্রাহ্য, বাতিল ও নামঞ্জুর বলিয়া ঘোষিত হইবে।\n\n")
            append("৫। আমি এই মর্মে অঙ্গীকার ও স্বীকার করিতেছি যে, উল্লেখিত ব্যক্তির যে কোন প্রকার বে আইনী কার্যকলাপ, নিয়মশৃঙ্খলা ও অনৈতিক আচার আচরণের জন্য এবং ২ নং অনুচ্ছেদে বর্ণিত যে কোন কারণে অথবা অর্থ আত্নসাতের দায়ে উল্লেখিত ব্যক্তি দোষী সাব্যস্ত হলে আমি নগদ অর্থে ক্ষতিপূরণ দিতে বাধ্য রহিলাম বা আইনতঃ বাধ্য থাকিব। রুরাল রিকনস্ট্রাকশন ফাউন্ডেশন কর্তৃপক্ষ আমার প্রদত্ত অত্র জামানতনামা বলে আমার যে কোন স্থাবর বা অস্থাবর সম্পত্তি আদালতযোগে উপযুক্ত আইনের আশ্রয়ে বাজেয়াপ্ত ঘোষণা ক্রমে উপরে ঘোষিত টাকার সমপরিমাণ ক্ষতিপূরণ আমার নিকট হইতে আদায় করিতে পারিবেন।")
        }
        drawJustifiedParagraph(canvas2, page2Content, MARGIN_HORIZONTAL.toFloat(), page2Y, contentWidth, textPaint)
        document.finishPage(page2)

        // ---------------- PAGE 3 (Satyapath & Witnesses) ----------------
        val page3Info = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 3).create()
        val page3 = document.startPage(page3Info)
        val canvas3: Canvas = page3.canvas

        var page3Y = if (forStampPaper) MARGIN_TOP_100_TK_STAMP.toFloat() else (MARGIN_TOP_DEFAULT.toFloat() + 30f)

        // Satyapath Header
        val satyapathTitle = "সত্যপাঠ"
        val satyapathWidth = titlePaint.measureText(satyapathTitle)
        canvas3.drawText(satyapathTitle, (PAGE_WIDTH - satyapathWidth) / 2f, page3Y + 14f, titlePaint)
        page3Y += 30f

        val banglaDate = BanglaTextValidator.toBanglaDigits(agreement.submissionDate)
        val satyapathBody = buildString {
            append("আমি উক্ত শর্তাবলী স্বজ্ঞানে, সুস্থ শরীরে এবং কাহারো দ্বারা প্ররোচিত না হইয়া এবং ইহার ভবিষ্যৎ ফলাফল ভাবিয়া চিন্তিয়া, বুঝিয়া-গুনিয়া ও পড়িয়া স্বেচ্ছায় অঙ্গীকারাবদ্ধ হইয়া নিম্নে বর্ণিত সাক্ষীগণের উপস্থিতিতে আমার নাম স্বাক্ষর করিলাম।\n\n")
            append("তারিখ : $banglaDate খ্রিষ্টাব্দ।")
        }
        page3Y += drawJustifiedParagraph(canvas3, satyapathBody, MARGIN_HORIZONTAL.toFloat(), page3Y, contentWidth, textPaint) + 24f

        // Witnesses & Signatures layout
        val witnessHeader = "স্বাক্ষীগণের স্বাক্ষর"
        canvas3.drawText(witnessHeader, MARGIN_HORIZONTAL.toFloat(), page3Y, textPaint)
        page3Y += 22f

        val leftMargin = MARGIN_HORIZONTAL.toFloat()
        val rightMargin = (PAGE_WIDTH - MARGIN_HORIZONTAL).toFloat()

        // Calculate right block alignment:
        // Positioned at right side, with internal left-alignment so "তারিখ" starts directly aligned with "জামিনদারের"
        val guarantorText1 = "জামিনদারের টিপসহি"
        val guarantorText2 = "জামিনদারের স্বাক্ষর"
        val dateText = "তারিখ : $banglaDate খ্রিষ্টাব্দ।"
        val maxGuarantorWidth = maxOf(
            textPaint.measureText(guarantorText1),
            textPaint.measureText(guarantorText2),
            textPaint.measureText(dateText)
        )
        val guarantorBlockX = rightMargin - maxGuarantorWidth

        // Witness 1 & Guarantor Signatures (right side left-aligned inside right-aligned block)
        // "জামিনদারের টিপসহি" is placed higher to make more room for "জামিনদারের স্বাক্ষর"
        val w1Y = page3Y
        canvas3.drawText("১। স্বাক্ষর : . . . . . . . . . . . . . . . . . . . . . . .", leftMargin, w1Y, textPaint)
        canvas3.drawText("   নাম : . . . . . . . . . . . . . . . . . . . . . . . . . . . .", leftMargin, w1Y + 16f, textPaint)
        canvas3.drawText("   স্বামী/পিতার নাম : . . . . . . . . . . . . . . . . .", leftMargin, w1Y + 32f, textPaint)
        canvas3.drawText("   পূর্ণ ঠিকানা : . . . . . . . . . . . . . . . . . . . . . . . .", leftMargin, w1Y + 48f, textPaint)

        canvas3.drawText(guarantorText1, guarantorBlockX, w1Y - 2f, textPaint)
        canvas3.drawText(guarantorText2, guarantorBlockX, w1Y + 48f, textPaint)
        canvas3.drawText(dateText, guarantorBlockX, w1Y + 64f, textPaint)

        page3Y = w1Y + 90f

        // Witness 2 & Verification text
        val w2Y = page3Y
        canvas3.drawText("২। স্বাক্ষর : . . . . . . . . . . . . . . . . . . . . . . .", leftMargin, w2Y, textPaint)
        canvas3.drawText("   নাম : . . . . . . . . . . . . . . . . . . . . . . . . . . . .", leftMargin, w2Y + 16f, textPaint)
        canvas3.drawText("   স্বামী/পিতার নাম : . . . . . . . . . . . . . . . . .", leftMargin, w2Y + 32f, textPaint)
        canvas3.drawText("   পূর্ণ ঠিকানা : . . . . . . . . . . . . . . . . . . . . . . . .", leftMargin, w2Y + 48f, textPaint)

        val verif1 = "উল্লেখিত জামিনদার আমার সম্মুখে"
        val verif2 = "তার নিজ নাম সই করিয়াছে।"
        val maxVerifWidth = maxOf(textPaint.measureText(verif1), textPaint.measureText(verif2))
        val verifBlockX = rightMargin - maxVerifWidth
        canvas3.drawText(verif1, verifBlockX, w2Y + 18f, textPaint)
        canvas3.drawText(verif2, verifBlockX, w2Y + 34f, textPaint)

        page3Y = w2Y + 70f

        // Witness 3 & Advocate
        val w3Y = page3Y
        canvas3.drawText("৩। স্বাক্ষর : . . . . . . . . . . . . . . . . . . . . . . .", leftMargin, w3Y, textPaint)
        canvas3.drawText("   নাম : . . . . . . . . . . . . . . . . . . . . . . . . . . . .", leftMargin, w3Y + 16f, textPaint)
        canvas3.drawText("   স্বামী/পিতার নাম : . . . . . . . . . . . . . . . . .", leftMargin, w3Y + 32f, textPaint)
        canvas3.drawText("   পূর্ণ ঠিকানা : . . . . . . . . . . . . . . . . . . . . . . . .", leftMargin, w3Y + 48f, textPaint)

        val advText = "এ্যাডভোকেটঃ"
        val advBlockX = rightMargin - textPaint.measureText(advText)
        canvas3.drawText(advText, advBlockX, w3Y + 32f, textPaint)

        document.finishPage(page3)

        // Save PDF to cache dir with staff's exact clean name and edit versioning
        val fileName = "${agreement.baseFileName}.pdf"
        val file = File(context.cacheDir, fileName)
        val fos = FileOutputStream(file)
        document.writeTo(fos)
        fos.flush()
        fos.close()
        document.close()

        return file
    }

    private fun drawJustifiedParagraph(
        canvas: Canvas,
        text: String,
        x: Float,
        y: Float,
        width: Int,
        paint: TextPaint
    ): Float {
        canvas.save()
        canvas.translate(x, y)

        val layout = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val builder = StaticLayout.Builder.obtain(text, 0, text.length, paint, width)
                .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                .setLineSpacing(2.5f, 1.12f)
                .setIncludePad(false)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                builder.setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_WORD)
            }
            builder.build()
        } else {
            @Suppress("DEPRECATION")
            StaticLayout(
                text,
                paint,
                width,
                Layout.Alignment.ALIGN_NORMAL,
                1.12f,
                2.5f,
                false
            )
        }

        layout.draw(canvas)
        canvas.restore()
        return layout.height.toFloat()
    }

    private fun drawPlainText(
        canvas: Canvas,
        text: String,
        x: Float,
        y: Float,
        width: Int,
        paint: TextPaint
    ): Float {
        canvas.save()
        canvas.translate(x, y)

        val layout = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            StaticLayout.Builder.obtain(text, 0, text.length, paint, width)
                .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                .setLineSpacing(4f, 1.2f)
                .setIncludePad(false)
                .build()
        } else {
            @Suppress("DEPRECATION")
            StaticLayout(
                text,
                paint,
                width,
                Layout.Alignment.ALIGN_NORMAL,
                1.2f,
                4f,
                false
            )
        }

        layout.draw(canvas)
        canvas.restore()
        return layout.height.toFloat()
    }
}
