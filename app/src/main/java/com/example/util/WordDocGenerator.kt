package com.example.util

import android.content.Context
import com.example.data.AgreementEntity
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets

object WordDocGenerator {

    /**
     * Generates a Word-compatible .doc file formatted in Bengali with exact justified layout.
     */
    fun generateAgreementDoc(
        context: Context,
        agreement: AgreementEntity,
        forStampPaper: Boolean = true
    ): File {
        val rel = agreement.effectiveGuarantorRelationship
        val nid = BanglaTextValidator.toBanglaDigits(agreement.guarantorNid)
        val banglaDate = BanglaTextValidator.toBanglaDigits(agreement.submissionDate)

        val htmlContent = """
            <!DOCTYPE html>
            <html xmlns:o='urn:schemas-microsoft-com:office:office' xmlns:w='urn:schemas-microsoft-com:office:word' xmlns='http://www.w3.org/TR/REC-html40'>
            <head>
            <meta charset="utf-8">
            <title>জামানতনামা - ${agreement.employeeName}</title>
            <style>
                @page Section1 {
                    size: 8.5in 14.0in; /* Legal Size */
                    margin: 0.6in 0.6in 0.6in 0.6in;
                    mso-header-margin: 0.3in;
                    mso-footer-margin: 0.3in;
                }
                div.Section1 { page: Section1; }
                body {
                    font-family: 'SolaimanLipi', 'Vrinda', 'Kalpurush', 'SutonnyMJ', 'Nirmala UI', serif;
                    font-size: 11pt;
                    line-height: 1.4;
                    color: #000000;
                }
                h1 {
                    text-align: center;
                    font-size: 16pt;
                    margin-bottom: 18px;
                    font-weight: bold;
                }
                h2 {
                    text-align: center;
                    font-size: 15pt;
                    margin-top: 18px;
                    margin-bottom: 15px;
                    font-weight: bold;
                }
                p {
                    text-align: justify;
                    text-justify: inter-word;
                    margin-top: 8px;
                    margin-bottom: 8px;
                }
                .page-break {
                    page-break-before: always;
                }
                .witness-table {
                    width: 100%;
                    border-collapse: collapse;
                    margin-top: 20px;
                }
                .witness-table td {
                    vertical-align: top;
                    padding: 6px 4px;
                    font-size: 11pt;
                }
            </style>
            </head>
            <body>
            <div class="Section1">
                ${if (forStampPaper) "<div style=\"height: 4.5in;\"></div>" else ""}
                <!-- PAGE 1: জামানতনামা -->
                <h1>জামানতনামা</h1>
                
                <p>আমি <strong>${agreement.guarantorName}</strong>, পিতা : <strong>${agreement.guarantorFatherName}</strong>, মাতা : <strong>${agreement.guarantorMotherName}</strong>, সম্পর্ক : <strong>$rel</strong>।<br>
                ভোটার আইডি নংঃ <strong>$nid</strong><br>
                বর্তমান ঠিকানা :<br>
                গ্রাম : <strong>${agreement.guarantorVillage}</strong>, ডাকঘর : <strong>${agreement.guarantorPostOffice}</strong>, উপজেলা : <strong>${agreement.guarantorUpazila}</strong>, জেলা : <strong>${agreement.guarantorDistrict}</strong>।</p>

                <p>রুরাল রিকনস্ট্রাকশন ফাউন্ডেশন এ <strong>${agreement.designation}</strong> পদে নিয়োজিত জনাব <strong>${agreement.employeeName}</strong>, পিতাঃ <strong>${agreement.employeeFatherName}</strong>।<br>
                গ্রাম : <strong>${agreement.employeeVillage}</strong>, ডাকঘর : <strong>${agreement.employeePostOffice}</strong>, উপজেলা : <strong>${agreement.employeeUpazila}</strong>, জেলা : <strong>${agreement.employeeDistrict}</strong>।<br>
                এর জন্য এবং তাহার পক্ষে সার্বিক দায়-দায়িত্ব স্বীকার করিয়া জামিনদার হিসাবে নিম্নলিখিত শর্তাবলী সাপেক্ষে অঙ্গীকারবদ্ধ হইলাম :</p>

                <p><strong>শর্তাবলী :-</strong></p>

                <p>১। যেহেতু রুরাল রিকনস্ট্রাকশন ফাউন্ডেশন জনাব <strong>${agreement.employeeName}</strong> কে <strong>${agreement.designation}</strong> পদে চাকুরী প্রদান করিয়াছে সেহেতু আমি জনাব <strong>${agreement.guarantorName}</strong> এর জন্য জামিনদার বহাল থাকিয়া রুরাল রিকনস্ট্রাকশন ফাউন্ডেশন বরাবরে অত্র জামানত নামা প্রদান করিলাম।</p>

                <p>২। রুরাল রিকনস্ট্রাকশন ফাউন্ডেশন এ চাকুরীরত থাকাকালীন উল্লেখিত ব্যক্তির কর্তব্যকাজে অবহেলা, ইচ্ছাকৃত ত্রুটি, স্বীয়-স্বার্থ আদায়ের লক্ষ্যে উদ্দেশ্য প্রণোদিতভাবে কোন কার্য সম্পাদন ফৌজদারী বা দেওয়ানী আইনে শাস্তিযোগ্য অপরাধের দ্বারা প্রত্যক্ষ বা পরোক্ষভাবে রুরাল রিকনস্ট্রাকশন ফাউন্ডেশন এর কোন ক্ষতিসাধন, দেশের প্রচলিত আইন কানুন, নিয়ম শৃঙ্খলা ও বিধি বিধানের পরিপন্থী কোন বে- আইনী বা অনৈতিক কাজে নিজেকে জড়িত করা, রুরাল রিকনস্ট্রাকশন ফাউন্ডেশন এর প্রাতিষ্ঠানিক বা প্রশাসনিক আইন বা বেআইনী বা নিয়ম শৃঙ্খলা পরিপন্থি কোন কর্মকান্ডে জড়িত হওয়া অথবা প্রত্যক্ষ বা পরোক্ষভাবে রুরাল রিকনস্ট্রাকশন ফাউন্ডেশনের আর্থিক ক্ষতি সাধন করা, ব্যাংক হতে নগদ উত্তোলন বা নগদে অথবা নানাবিধ উপায়ে আর্থিক সুবিধা লাভ করা কিংবা অর্থ আত্নসাৎ করা, রুরাল রিকনস্ট্রাকশন ফাউন্ডেশন এর কোন স্থাবর বা অস্থাবর সম্পত্তি বিনষ্ট বা হস্তগত করা কিংবা তার ক্ষতি সাধন করা, রুরাল রিকনস্ট্রাকশন ফাউন্ডেশন এর সুনাম ক্ষুন্ন হইতে পারে এবং রাষ্ট্রবিরোধী কোন কার্যকলাপে জড়িত হওয়া বা প্রত্যক্ষ ও পরোক্ষভাবে ইত্যাদি যে কোন প্রকার কাজে দায়ী বা দোষী হইলে আমি তাহার সকল প্রকার দায় দায়িত্ব নিজে বহন করিব বা করিতে আইনতঃ বাধ্য থাকিব এবং রুরাল রিকনস্ট্রাকশন ফাউন্ডেশন এর দাবীকৃত বা আদালত কর্তৃক ঘোষিত ও নির্ধারিত যে কোন অংকের আর্থিক ক্ষতিপূরণ প্রদানে বাধ্য থাকিব।</p>

                <div class="page-break"></div>

                <!-- PAGE 2: Terms 3, 4, 5 (100 Tk Stamp space) -->
                ${if (forStampPaper) "<div style=\"height: 4.5in;\"></div>" else ""}
                <p>৩। উল্লেখিত ব্যক্তি সম্পূর্ণ সততা, বিশ্বস্ততা, শৃঙ্খলা ও নিয়মানুবর্তিতার সহিত তাহার উপর অর্পিত দায়িত্ব ও কর্তব্য পালন করিবেন।</p>

                <p>৪। আমি জামিনদার হিসাবে আমার দায়িত্ব পালন না করিলে বা দায়িত্ব পালনে কোন প্রকার অনীহা প্রকাশ করিলে কিংবা প্রদত্ত অঙ্গীকার ভঙ্গ করিলে আমার বিরুদ্ধে রুরাল রিকনস্ট্রাকশন ফাউন্ডেশন কর্তৃপক্ষ অত্র জামানতনামা বলে আইন অনুযায়ী যে কোন প্রকার ব্যবস্থা গ্রহণ করিতে পারিবেন এবং সে ক্ষেত্রে আমার কোন প্রকার ওজর আপত্তি সর্ব আদালতে অগ্রাহ্য, বাতিল ও নামঞ্জুর বলিয়া ঘোষিত হইবে।</p>

                <p>৫। আমি এই মর্মে অঙ্গীকার ও স্বীকার করিতেছি যে, উল্লেখিত ব্যক্তির যে কোন প্রকার বে আইনী কার্যকলাপ, নিয়মশৃঙ্খলা ও অনৈতিক আচার আচরণের জন্য এবং ২ নং অনুচ্ছেদে বর্ণিত যে কোন কারণে অথবা অর্থ আত্নসাতের দায়ে উল্লেখিত ব্যক্তি দোষী সাব্যস্ত হলে আমি নগদ অর্থে ক্ষতিপূরণ দিতে বাধ্য রহিলাম বা আইনতঃ বাধ্য থাকিব। রুরাল রিকনস্ট্রাকশন ফাউন্ডেশন কর্তৃপক্ষ আমার প্রদত্ত অত্র জামানতনামা বলে আমার যে কোন স্থাবর বা অস্থাবর সম্পত্তি আদালতযোগে উপযুক্ত আইনের আশ্রয়ে বাজেয়াপ্ত ঘোষণা ক্রমে উপরে ঘোষিত টাকার সমপরিমাণ ক্ষতিপূরণ আমার নিকট হইতে আদায় করিতে পারিবেন।</p>

                <div class="page-break"></div>

                <!-- PAGE 3: Satyapath and Signatures (100 Tk Stamp space) -->
                ${if (forStampPaper) "<div style=\"height: 4.5in;\"></div>" else ""}
                <h2>সত্যপাঠ</h2>

                <p>আমি উক্ত শর্তাবলী স্বজ্ঞানে, সুস্থ শরীরে এবং কাহারো দ্বারা প্ররোচিত না হইয়া এবং ইহার ভবিষ্যৎ ফলাফল ভাবিয়া চিন্তিয়া, বুঝিয়া-গুনিয়া ও পড়িয়া স্বেচ্ছায় অঙ্গীকারাবদ্ধ হইয়া নিম্নে বর্ণিত সাক্ষীগণের উপস্থিতিতে আমার নাম স্বাক্ষর করিলাম।</p>
                <p>তারিখ : $banglaDate খ্রিষ্টাব্দ।</p>

                <p style="margin-top: 30px;">স্বাক্ষীগণের স্বাক্ষর</p>

                <table class="witness-table">
                    <tr>
                        <td style="width: 55%; vertical-align: top;">
                            ১। স্বাক্ষর : . . . . . . . . . . . . . . . . . . . . . . .<br>
                            &nbsp;&nbsp;&nbsp;&nbsp;নাম : . . . . . . . . . . . . . . . . . . . . . . . . . . . .<br>
                            &nbsp;&nbsp;&nbsp;&nbsp;স্বামী/পিতার নাম : . . . . . . . . . . . . . . . . .<br>
                            &nbsp;&nbsp;&nbsp;&nbsp;পূর্ণ ঠিকানা : . . . . . . . . . . . . . . . . . . . . . . . .
                        </td>
                        <td style="width: 45%; text-align: right; vertical-align: top;">
                            <div style="display: inline-block; text-align: left;">
                                <div style="margin-bottom: 25px;">জামিনদারের টিপসহি</div>
                                <div>জামিনদারের স্বাক্ষর</div>
                                <div>তারিখ : $banglaDate খ্রিষ্টাব্দ।</div>
                            </div>
                        </td>
                    </tr>
                    <tr>
                        <td style="width: 55%; padding-top: 20px;">
                            ২। স্বাক্ষর : . . . . . . . . . . . . . . . . . . . . . . .<br>
                            &nbsp;&nbsp;&nbsp;&nbsp;নাম : . . . . . . . . . . . . . . . . . . . . . . . . . . . .<br>
                            &nbsp;&nbsp;&nbsp;&nbsp;স্বামী/পিতার নাম : . . . . . . . . . . . . . . . . .<br>
                            &nbsp;&nbsp;&nbsp;&nbsp;পূর্ণ ঠিকানা : . . . . . . . . . . . . . . . . . . . . . . . .
                        </td>
                        <td style="width: 45%; text-align: right; padding-top: 20px;">
                            <div style="display: inline-block; text-align: left;">
                                উল্লেখিত জামিনদার আমার সম্মুখে<br>
                                তার নিজ নাম সই করিয়াছে।
                            </div>
                        </td>
                    </tr>
                    <tr>
                        <td style="width: 55%; padding-top: 20px;">
                            ৩। স্বাক্ষর : . . . . . . . . . . . . . . . . . . . . . . .<br>
                            &nbsp;&nbsp;&nbsp;&nbsp;নাম : . . . . . . . . . . . . . . . . . . . . . . . . . . . .<br>
                            &nbsp;&nbsp;&nbsp;&nbsp;স্বামী/পিতার নাম : . . . . . . . . . . . . . . . . .<br>
                            &nbsp;&nbsp;&nbsp;&nbsp;পূর্ণ ঠিকানা : . . . . . . . . . . . . . . . . . . . . . . . .
                        </td>
                        <td style="width: 45%; text-align: right; padding-top: 20px;">
                            <div style="display: inline-block; text-align: left;">
                                এ্যাডভোকেটঃ
                            </div>
                        </td>
                    </tr>
                </table>
            </div>
            </body>
            </html>
        """.trimIndent()

        val fileName = "${agreement.baseFileName}.doc"
        val file = File(context.cacheDir, fileName)
        val writer = OutputStreamWriter(FileOutputStream(file), StandardCharsets.UTF_8)
        writer.write(htmlContent)
        writer.flush()
        writer.close()

        return file
    }
}
