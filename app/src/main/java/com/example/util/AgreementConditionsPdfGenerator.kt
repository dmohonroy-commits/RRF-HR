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
import java.io.File
import java.io.FileOutputStream

object AgreementConditionsPdfGenerator {

    private const val PAGE_WIDTH = 595
    private const val PAGE_HEIGHT = 842
    private const val MARGIN = 36f

    fun generateConditionsPdf(context: Context): File {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        drawConditionsDocument(canvas)

        pdfDocument.finishPage(page)
        val file = File(context.cacheDir, "RRF_Appointment_Conditions_${System.currentTimeMillis()}.pdf")
        FileOutputStream(file).use { fos ->
            pdfDocument.writeTo(fos)
            fos.flush()
        }
        pdfDocument.close()
        return file
    }

    private fun drawConditionsDocument(canvas: Canvas) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        canvas.drawColor(Color.WHITE)

        val left = MARGIN + 10f
        val right = PAGE_WIDTH - MARGIN - 10f
        val printableWidth = (right - left).toInt()

        val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(25, 25, 25)
            textSize = 9.0f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        }

        // Header Title
        paint.color = Color.rgb(15, 15, 15)
        paint.textSize = 16f
        paint.typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("রুরাল রিকনস্ট্রাকশন ফাউন্ডেশন", (PAGE_WIDTH / 2).toFloat(), 44f, paint)

        paint.textSize = 9.0f
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        paint.color = Color.rgb(70, 70, 70)
        canvas.drawText("আরআরএফ ভবন, সিএন্ডবি রোড, কারবালা, যশোর-৭৪০০", (PAGE_WIDTH / 2).toFloat(), 58f, paint)

        // Subhead
        paint.textSize = 12.5f
        paint.typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        paint.color = Color.rgb(10, 10, 10)
        canvas.drawText("নিয়োগ প্রাপ্তির প্রয়োজনীয় শর্তাবলী সমূহ :", (PAGE_WIDTH / 2).toFloat(), 78f, paint)

        // Line under header
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 0.8f
        paint.color = Color.rgb(180, 180, 180)
        canvas.drawLine(left, 86f, right, 86f, paint)

        // Clause 1
        var y = 96f
        val clause1 = "০১। নিয়োগগ্রহণে ইচ্ছুক প্রার্থীর বাবা (বাবার অবর্তমানে বড়ভাই/বিবাহিত হলে শ্বশুর) জামিনদার হবেন। জামিনদারের সাথে ৩০০ টাকার (১০০ টাকার তিনটি) ননজুডিশিয়াল স্ট্যাম্পে চুক্তি সম্পন্ন করা হবে। উক্ত স্ট্যাম্পটি জামিনদারের নামে ক্রয় করতে হবে এবং জামিনদারকে স্ব-শরীরে উপস্থিত হয়ে চুক্তি সম্পন্ন করতে হবে। জামিনদারের এক কপি পাসপোর্ট সাইজের ছবি ও জাতীয় পরিচয়পত্রের ফটোকপি জমা দিতে হবে।"
        y += drawParagraph(canvas, clause1, left, y, printableWidth, textPaint, 8.8f, 12.5f) + 6f

        // Clause 2 + Table
        val clause2 = "০২। ফেরতযোগ্য নির্ধারিত হারে জামানত জমা দিতে হবে।"
        y += drawParagraph(canvas, clause2, left, y, printableWidth, textPaint, 8.8f, 12.5f) + 4f

        // Table
        val tableTop = y
        val col0W = 30f
        val col1W = 345f
        val col2W = printableWidth.toFloat() - col0W - col1W
        val headerH = 16f
        val rowH = 15f

        // Table Header
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 0.8f
        paint.color = Color.BLACK
        canvas.drawRect(left, tableTop, right, tableTop + headerH, paint)
        canvas.drawLine(left + col0W, tableTop, left + col0W, tableTop + headerH, paint)
        canvas.drawLine(left + col0W + col1W, tableTop, left + col0W + col1W, tableTop + headerH, paint)

        paint.style = Paint.Style.FILL
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        paint.textSize = 8.5f
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("ক্রম", left + (col0W / 2), tableTop + 11.5f, paint)
        canvas.drawText("পদবী", left + col0W + (col1W / 2), tableTop + 11.5f, paint)
        canvas.drawText("জামানতের পরিমান", left + col0W + col1W + (col2W / 2), tableTop + 11.5f, paint)

        val tableData = listOf(
            Triple("০১", "সহকারী পরিচালক", "৪০,০০০/-"),
            Triple("০২", "আঞ্চলিক ব্যবস্থাপক, সহকারী আঞ্চলিক ব্যবস্থাপক, বিজনেস ডেভেলপমেন্ট ম্যানেজার", "৩০,০০০/-"),
            Triple("০৩", "শাখা ব্যবস্থাপক, উপ-শাখা ব্যবস্থাপক, বিজনেস ডেভেলপমেন্ট অফিসার", "২৫,০০০/-"),
            Triple("০৪", "অফিসার (অ্যাকাউন্টস)", "২০,০০০/-"),
            Triple("০৫", "অফিসার (ঋণ), সহকারী অফিসার (ঋণ)", "১৫,০০০/-"),
            Triple("০৬", "সার্ভিস স্টাফ", "৫,০০০/-")
        )

        var curRowTop = tableTop + headerH
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        paint.textSize = 8.0f

        for (item in tableData) {
            paint.style = Paint.Style.STROKE
            canvas.drawRect(left, curRowTop, right, curRowTop + rowH, paint)
            canvas.drawLine(left + col0W, curRowTop, left + col0W, curRowTop + rowH, paint)
            canvas.drawLine(left + col0W + col1W, curRowTop, left + col0W + col1W, curRowTop + rowH, paint)

            paint.style = Paint.Style.FILL
            paint.textAlign = Paint.Align.CENTER
            canvas.drawText(item.first, left + (col0W / 2), curRowTop + 11f, paint)
            paint.textAlign = Paint.Align.LEFT
            canvas.drawText(item.second, left + col0W + 5f, curRowTop + 11f, paint)
            paint.textAlign = Paint.Align.RIGHT
            canvas.drawText(item.third, right - 8f, curRowTop + 11f, paint)

            curRowTop += rowH
        }

        y = curRowTop + 6f

        // Remaining Clauses 3 to 10
        val remainingClauses = listOf(
            "০৩। নিম্নলিখিত প্রত্যয়নপত্র মোতাবেক স্থানীয় ২ জন বিশিষ্ট ব্যক্তির (শিক্ষক/ব্যবসায়ী/ডাক্তার) নামে ২টি ২৫ টাকার ননজুডিশিয়াল স্ট্যাম্প ক্রয় করতে হবে এবং উক্ত স্ট্যাম্পে তাদের নিকট হতে (নিম্নলিখিত প্রত্যয়ন অনুসারে) প্রত্যয়ন পত্র এবং উক্ত ব্যক্তির জাতীয় পরিচয়পত্রের ফটোকপি ও ১ কপি রঙিন ছবি জমা দিতে হবে।",
            "০৪। নমিনি-এর জাতীয় পরিচয়পত্রের ফটোকপি ও ১ (ল্যাব প্রিন্ট) কপি রঙিন ছবি জমা দিতে হবে।",
            "০৫। শিক্ষাগত যোগ্যতার সকল একাডেমিক পাশের মূল সার্টিফিকেট সমূহ চাকুরী কালীন সময়ের জন্য সংস্থায় জমা রাখতে হবে।",
            "০৬। আইডি কার্ডের জন্য স্ট্যাম্প/পাসপোর্ট সাইজের সদ্যতোলা ২ কপি রঙিন ছবি, ব্লাড গ্রুপ রিপোর্ট, জাতীয় পরিচয়পত্রের ফটোকপি, কোভিড-১৯ ভ্যাকসিনেশনের সার্টিফিকেট এবং নগদ ২০০ টাকা অফিসে জমা দিতে হবে।",
            "০৭। সংস্থায় কর্মএলাকায় যেকোনো স্থানে কাজের মানসিকতা থাকতে হবে।",
            "০৮। প্রার্থীর নামে ১টি ডামি পেপার (কার্টিজ পেপার) সঙ্গে আনতে হবে।",
            "০৯। উপরোক্ত টেবিলের ক্রম ০১ এর পদবী সমূহের জন্য ড্রাইভিং লাইসেন্স, মোটর সাইকেলের রেজিস্ট্রেশনের ফটোকপি এবং পূর্ববর্তী প্রতিষ্ঠান সমূহের চাকুরীর চূড়ান্ত ছাড়পত্র ও অভিজ্ঞতা সনদ এর ফটোকপি আনতে হবে।",
            "১০। সকল প্রকার ছবি ডিজিটাল ল্যাব প্রিন্ট হতে হবে।"
        )

        for (clause in remainingClauses) {
            y += drawParagraph(canvas, clause, left, y, printableWidth, textPaint, 8.5f, 12f) + 4f
        }

        // Section: প্রত্যয়ন পত্র (নমুনা)
        y += 4f
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 0.5f
        paint.color = Color.rgb(200, 200, 200)
        canvas.drawLine(left + 50f, y, right - 50f, y, paint)

        y += 14f
        paint.style = Paint.Style.FILL
        paint.textSize = 11.5f
        paint.typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        paint.color = Color.BLACK
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("প্রত্যয়ন পত্র (নমুনা)", (PAGE_WIDTH / 2).toFloat(), y, paint)

        y += 8f
        val sampleText = "আমি এই মর্মে প্রত্যয়ন করিতেছি যে, নাম : ..................................................................... পিতার নাম : .............................................................. মাতার নাম : .............................................................. ঠিকানা-গ্রাম : .............................................................. উপজেলা : .............................................................. জেলা : .............................................................. কে আমি ব্যক্তিগত ভাবে চিনি এবং জানি। আমার জানা মতে সে কোন অসামাজিক ও অনৈতিক কার্যক্রমের সাথে যুক্ত নয়। উক্ত ব্যক্তি দ্বারা কোন অনিয়ম বা সমাজ ও রাষ্ট্রবিরোধী কোন কার্যক্রম সংঘটিত হলে আমি তার দায়িত্ব গ্রহণ করিলাম।"
        y += drawParagraph(canvas, sampleText, left, y, printableWidth, textPaint, 8.5f, 12.5f) + 12f

        // Signature Fields (Neat 2-Column or Right-Aligned block)
        val sigX = right - 195f
        paint.textSize = 8.5f
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        paint.textAlign = Paint.Align.LEFT
        paint.color = Color.BLACK

        canvas.drawText("স্বাক্ষর : ................................................................", sigX, y, paint)
        y += 13f
        canvas.drawText("নাম : ................................................................", sigX, y, paint)
        y += 13f
        canvas.drawText("পেশা : ................................................................", sigX, y, paint)
        y += 13f
        canvas.drawText("ঠিকানা : ................................................................", sigX, y, paint)
        y += 13f
        canvas.drawText("মোবাইল নং : ................................................................", sigX, y, paint)
        y += 13f
        canvas.drawText("জাতীয় পরিচয়পত্র নং : ................................................", sigX, y, paint)
    }

    private fun drawParagraph(
        canvas: Canvas,
        text: String,
        x: Float,
        y: Float,
        width: Int,
        paint: TextPaint,
        textSize: Float,
        lineSpacing: Float
    ): Float {
        paint.textSize = textSize
        val normalized = text.replace('ঃ', ':')

        canvas.save()
        canvas.translate(x, y)

        val layout = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val builder = StaticLayout.Builder.obtain(normalized, 0, normalized.length, paint, width)
                .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                .setLineSpacing(lineSpacing - textSize, 1f)
                .setIncludePad(false)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                builder.setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_WORD)
            }
            builder.build()
        } else {
            @Suppress("DEPRECATION")
            StaticLayout(
                normalized,
                paint,
                width,
                Layout.Alignment.ALIGN_NORMAL,
                1f,
                lineSpacing - textSize,
                false
            )
        }

        layout.draw(canvas)
        canvas.restore()
        return layout.height.toFloat()
    }
}
