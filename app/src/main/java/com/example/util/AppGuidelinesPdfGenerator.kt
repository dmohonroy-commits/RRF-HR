package com.example.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import java.io.File
import java.io.FileOutputStream

object AppGuidelinesPdfGenerator {

    private const val PAGE_WIDTH = 595 // A4 standard width in points
    private const val PAGE_HEIGHT = 842 // A4 standard height in points
    private const val MARGIN = 36

    fun generateGuidelinesPdf(context: Context): File {
        val pdfDocument = PdfDocument()
        val paint = Paint().apply { isAntiAlias = true }

        // --- PAGE 1: Overview & Step 1 to 4 ---
        val pageInfo1 = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val page1 = pdfDocument.startPage(pageInfo1)
        val canvas1 = page1.canvas
        drawPage1(canvas1, paint)
        pdfDocument.finishPage(page1)

        // --- PAGE 2: Step 5 to 7 & Auto-Fill / Print Guide ---
        val pageInfo2 = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 2).create()
        val page2 = pdfDocument.startPage(pageInfo2)
        val canvas2 = page2.canvas
        drawPage2(canvas2, paint)
        pdfDocument.finishPage(page2)

        val outputFile = File(context.cacheDir, "RRF_App_Usage_Guidelines.pdf")
        FileOutputStream(outputFile).use { out ->
            pdfDocument.writeTo(out)
        }
        pdfDocument.close()
        return outputFile
    }

    private fun drawPage1(canvas: Canvas, paint: Paint) {
        var y = MARGIN.toFloat() + 15f
        val left = MARGIN.toFloat()
        val right = (PAGE_WIDTH - MARGIN).toFloat()
        val contentWidth = right - left

        // Header Background Banner
        paint.color = Color.parseColor("#0F766E") // Emerald / Teal header
        canvas.drawRoundRect(left, y, right, y + 65f, 8f, 8f, paint)

        // Header Titles
        paint.color = Color.WHITE
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 15f
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("রুরাল রিকনস্ট্রাকশন ফাউন্ডেশন (আরআরএফ)", PAGE_WIDTH / 2f, y + 24f, paint)

        paint.textSize = 11f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("কর্মী চুক্তিপত্র ও এগ্রিমেন্ট ব্যবস্থাপনা পোর্টাল • অ্যাপ ব্যবহারের গাইডলাইন ও ইউজার ম্যানুয়াল", PAGE_WIDTH / 2f, y + 42f, paint)

        paint.textSize = 9.5f
        canvas.drawText("প্রধান কার্যালয়: আরআরএফ ভবন, সিএন্ডবি রোড, কারবালা, যশোর-৭৪০০", PAGE_WIDTH / 2f, y + 56f, paint)

        y += 80f
        paint.textAlign = Paint.Align.LEFT

        // Section: অ্যাপ ব্যবহারের মূল উদ্দেশ্য
        paint.color = Color.parseColor("#047857")
        paint.textSize = 12f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("১. অ্যাপের মূল বৈশিষ্ট্য ও অটো-ফিল সুবিধা", left, y, paint)
        y += 16f

        paint.color = Color.parseColor("#1F2937")
        paint.textSize = 9.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        val introLines = listOf(
            "• ১০০ টাকার স্ট্যাম্প চুক্তিপত্র, ২৫ টাকার প্রত্যয়ন পত্র এবং ৬টি এগ্রিমেন্ট ফরম সমন্বিত ও স্বয়ংক্রিয়ভাবে সংযুক্ত।",
            "• স্মার্ট অটো-ফিল সিস্টেম: একটি ফরমে নাম, পিতা, ঠিকানা ইত্যাদি পূরণ করলে পরবর্তী সকল ফরমে তা স্বয়ংক্রিয়ভাবে বসে যাবে।",
            "• অটো-পূরণকৃত ছকসমূহ সবুজ কালার এবং লকড থাকবে যাতে কর্মীকে দ্বিতীয়বার পূরণ করতে না হয়।",
            "• তথ্যানুসন্ধান ফরমে কর্মীর NID শুরুতে ফাঁকা থাকবে এবং টাইপ করার পর তা স্থায়ীভাবে সংরক্ষিত হবে।",
            "• সব ফরমের জন্য: বাংলা ছকে বাংলা, ইংরেজি ছকে ইংরেজি এবং সংখ্যার ছকে শুধুমাত্র সংখ্যা পূরণ করতে হবে।"
        )
        for (line in introLines) {
            canvas.drawText(line, left + 8f, y, paint)
            y += 14f
        }

        y += 8f
        paint.color = Color.parseColor("#047857")
        paint.textSize = 12f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("২. ফরমভিত্তিক ক্রমান্বয়ে পূরণ নির্দেশিকা (ধাপ ১ হতে ৪)", left, y, paint)
        y += 18f

        // Step 1 Card
        drawStepBox(
            canvas = canvas,
            paint = paint,
            left = left,
            top = y,
            right = right,
            height = 68f,
            stepNo = "১",
            title = "কর্মীর আইডি কার্ডের তথ্য (Employee ID Card Details - English Form)",
            descList = listOf(
                "• এই ফরমটি সম্পূর্ণ ইংরেজিতে পূরণ করুন (Employee Name, Father, Mother, Address)।",
                "• জেলা, থানা, ডাকঘর ও গ্রাম ক্রমান্বয়ে ড্রপডাউন বা ইংরেজি বক্সে টাইপ করুন।",
                "• ১১ ডিজিটের সচল মোবাইল নম্বর ও সঠিক ইমেইল অ্যাড্রেস প্রদান করুন।"
            )
        )
        y += 76f

        // Step 2 Card
        drawStepBox(
            canvas = canvas,
            paint = paint,
            left = left,
            top = y,
            right = right,
            height = 68f,
            stepNo = "২",
            title = "প্রশিক্ষণ তথ্য ও অঙ্গীকারনামা (Training Agreement - বাংলা ফরম)",
            descList = listOf(
                "• প্রশিক্ষণ চলাকালীন অঙ্গীকারনামার বিবরণ বাংলায় পূরণ করতে হবে।",
                "• কর্মীর নাম, পিতার নাম, গ্রাম, ডাকঘর, পোস্ট কোড, থানা ও জেলা বাংলায় লিখুন।",
                "• পদবী ড্রপডাউন থেকে নির্বাচন করুন (অন্যান্য হলে পদবীর নাম বাংলায় লিখুন)।"
            )
        )
        y += 76f

        // Step 3 Card
        drawStepBox(
            canvas = canvas,
            paint = paint,
            left = left,
            top = y,
            right = right,
            height = 68f,
            stepNo = "৩",
            title = "পরিচিত ব্যক্তির সাথে সম্পর্ক সংক্রান্ত ঘোষণা (Kinship Declaration - English Form)",
            descList = listOf(
                "• আরআরএফ-এ কর্মরত কোনো আত্মীয় না থাকলে 'I have none' বক্সে টিক দিন।",
                "• আত্মীয় কর্মরত থাকলে তার নাম, ঠিকানা, পদবী ও সম্পর্ক ইংরেজিতে পূরণ করুন।"
            )
        )
        y += 76f

        // Step 4 Card
        drawStepBox(
            canvas = canvas,
            paint = paint,
            left = left,
            top = y,
            right = right,
            height = 74f,
            stepNo = "৪",
            title = "নমিনি তথ্য ফরম (Nominee Information Form - English Form)",
            descList = listOf(
                "• নমিনির নাম, পিতা/মাতার নাম ইংরেজিতে পূরণ করুন।",
                "• কর্মীর স্থায়ী ঠিকানা ও নমিনির ঠিকানা একই হলে টিক অপশন নির্বাচন করুন।",
                "• নমিনির জাতীয় পরিচয়পত্র (NID) নম্বর ও শেয়ারের শতকরা হার (যেমন: 100%) প্রদান করুন।"
            )
        )

        // Footer Page 1
        paint.color = Color.parseColor("#9CA3AF")
        paint.textSize = 8.5f
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("পৃষ্ঠা ১ • রুরাল রিকনস্ট্রাকশন ফাউন্ডেশন (আরআরএফ) - অ্যাপ ব্যবহারের গাইডলাইন", PAGE_WIDTH / 2f, PAGE_HEIGHT - 20f, paint)
    }

    private fun drawPage2(canvas: Canvas, paint: Paint) {
        var y = MARGIN.toFloat() + 15f
        val left = MARGIN.toFloat()
        val right = (PAGE_WIDTH - MARGIN).toFloat()

        // Section Title: ধাপ ৫ হতে ৮
        paint.color = Color.parseColor("#047857")
        paint.textSize = 12f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("৩. ফরমভিত্তিক ক্রমান্বয়ে পূরণ নির্দেশিকা (ধাপ ৫ হতে ৮)", left, y, paint)
        y += 18f

        // Step 5 Card
        drawStepBox(
            canvas = canvas,
            paint = paint,
            left = left,
            top = y,
            right = right,
            height = 74f,
            stepNo = "৫",
            title = "কর্মীর তথ্যানুসন্ধান ফরম (Personal Info - ৩ পাতার মূল ফরম)",
            descList = listOf(
                "• পাতা ১: কর্মীর নাম, NID (শুরুতে ফাঁকা থাকবে), বৈবাহিক অবস্থা, আয় ও ঠিকানা।",
                "• পাতা ২: শিক্ষাগত যোগ্যতা (এসএসসি, এইচএসসি, ডিগ্রি ড্রপডাউন ও বোর্ড) ও অভিজ্ঞতা।",
                "• পাতা ৩: পরিবারের সদস্যদের বিবরণ এবং অঙ্গীকারনামা প্রদানকারী জামিনদারের পূর্ণ বিবরণ।"
            )
        )
        y += 80f

        // Step 6 Card
        drawStepBox(
            canvas = canvas,
            paint = paint,
            left = left,
            top = y,
            right = right,
            height = 74f,
            stepNo = "৬",
            title = "তথ্য যাচাই ফরম (Verification Form - ২ পাতা)",
            descList = listOf(
                "• সেকশন ১: পরিচিত/আত্মীয় কর্মকর্তা এবং সেকশন ২: চেয়ারম্যানের পৃথক বক্স ঠিকানা।",
                "• সেকশন ৩: মুচলেকা (অটো পূরণ) এবং সেকশন ৪: প্রতিবেশী ১ ও ২ এর পূর্ণ বিবরণ।",
                "• তদন্তকারী কর্মকর্তার মন্তব্যের ঘরটি কর্মীদের জন্য সুরক্ষিত ও লকড থাকবে।"
            )
        )
        y += 80f

        // Step 7 Card
        drawStepBox(
            canvas = canvas,
            paint = paint,
            left = left,
            top = y,
            right = right,
            height = 68f,
            stepNo = "৭",
            title = "১০০ টাকার স্ট্যাম্প চুক্তিপত্র (100-Taka Non-Judicial Stamp Agreement)",
            descList = listOf(
                "• কর্মী ও জামিনদারের তথ্যাবলী দিয়ে ১০০ টাকার তিনটি নন-জুডিশিয়াল স্ট্যাম্পে প্রিন্ট।",
                "• নোট: প্রিন্ট করুন এবং প্রধান কার্যালয়ে গিয়ে জামিনদারের স্বাক্ষর করবে।"
            )
        )
        y += 74f

        // Step 8 Card
        drawStepBox(
            canvas = canvas,
            paint = paint,
            left = left,
            top = y,
            right = right,
            height = 68f,
            stepNo = "৮",
            title = "২৫ টাকার প্রত্যয়ন পত্র (25-Taka Stamp Attestation Certificate)",
            descList = listOf(
                "• উপরের অংশে কর্মীর তথ্য ও নিচের অংশে প্রতিবেশী ১ ও ২ এর তথ্য নিয়ে অটোমেটিক পূরণ।",
                "• নোট: প্রিন্ট করুন এবং প্রত্যয়নকারীদের স্বাক্ষর নিয়ে আসবেন।"
            )
        )
        y += 74f

        // Section: প্রিন্টিং ও শেয়ারিং গাইডলাইন
        paint.color = Color.parseColor("#047857")
        paint.textSize = 12f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("৪. প্রিন্ট, ডাউনলোড ও শেয়ারিং নির্দেশিকা", left, y, paint)
        y += 16f

        paint.color = Color.parseColor("#1F2937")
        paint.textSize = 9.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        val printGuides = listOf(
            "১. হোম স্ক্রিনের 'স্ট্যাম্প প্রিন্ট' অপশনে চাপ দিয়ে ১০০ টাকা বা ২৫ টাকার স্ট্যাম্প প্রিন্ট করা যায়।",
            "২. হোম স্ক্রিনের 'এগ্রিমেন্ট ফরম প্রিন্ট' অপশনে চাপ দিয়ে মার্জকৃত ৯ পাতার A4 PDF প্রিন্ট করা যায়।",
            "৩. হোয়াটসঅ্যাপ (WhatsApp) ও ডাউনলোডের মাধ্যমে এক ক্লিকে সহজে ডকুমেন্ট সংরক্ষণ ও পাঠানো যায়।",
            "৪. নতুন কর্মীর ডাটা এন্ট্রি: হোম স্ক্রিনের নেভিগেশন বাটন অথবা ড্রয়ার মেনু হতে সরাসরি শুরু করুন।"
        )
        for (g in printGuides) {
            canvas.drawText(g, left + 8f, y, paint)
            y += 14f
        }

        y += 12f

        // Contact Helpline Box
        paint.color = Color.parseColor("#F0F7FF")
        canvas.drawRoundRect(left, y, right, y + 42f, 8f, 8f, paint)
        paint.color = Color.parseColor("#0033A0")
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        canvas.drawRoundRect(left, y, right, y + 42f, 8f, 8f, paint)
        paint.style = Paint.Style.FILL

        paint.color = Color.parseColor("#0A2540")
        paint.textSize = 9.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("জরুরি সহায়তা ও হেল্পলাইন: মানবসম্পদ বিভাগ, আরআরএফ। ফোন: ০২৪৭৭৭৬৩৪৭৫, ০১৭৩৩-৩৩৩২০২", left + 12f, y + 18f, paint)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("ইমেইল: hr@rrf-bd.org • ওয়েবসাইট: www.rrf-bd.org", left + 12f, y + 32f, paint)

        // Footer Page 2
        paint.color = Color.parseColor("#9CA3AF")
        paint.textSize = 8.5f
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("পৃষ্ঠা ২ • রুরাল রিকনস্ট্রাকশন ফাউন্ডেশন (আরআরএফ) - অ্যাপ ব্যবহারের গাইডলাইন", PAGE_WIDTH / 2f, PAGE_HEIGHT - 20f, paint)
    }

    private fun drawStepBox(
        canvas: Canvas,
        paint: Paint,
        left: Float,
        top: Float,
        right: Float,
        height: Float,
        stepNo: String,
        title: String,
        descList: List<String>
    ) {
        // Background card
        paint.color = Color.parseColor("#F8FAFC")
        paint.style = Paint.Style.FILL
        canvas.drawRoundRect(left, top, right, top + height, 6f, 6f, paint)

        paint.color = Color.parseColor("#E2E8F0")
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        canvas.drawRoundRect(left, top, right, top + height, 6f, 6f, paint)
        paint.style = Paint.Style.FILL

        // Step Badge
        paint.color = Color.parseColor("#047857")
        canvas.drawCircle(left + 16f, top + 15f, 10f, paint)

        paint.color = Color.WHITE
        paint.textSize = 9f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText(stepNo, left + 16f, top + 18f, paint)

        // Title
        paint.color = Color.parseColor("#0F172A")
        paint.textSize = 10f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText(title, left + 32f, top + 18f, paint)

        // Bullet descriptions
        var lineY = top + 32f
        paint.color = Color.parseColor("#374151")
        paint.textSize = 8.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        for (desc in descList) {
            canvas.drawText(desc, left + 16f, lineY, paint)
            lineY += 12f
        }
    }
}
