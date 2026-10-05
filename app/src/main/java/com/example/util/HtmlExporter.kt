package com.example.util

import android.content.Context
import android.os.Environment
import com.example.data.AgreementEntity
import java.io.File
import java.io.FileOutputStream

object HtmlExporter {

    private fun getOutputDirectory(context: Context): File {
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val rrfDir = File(downloadsDir, "RRF_HR_Forms")
        if (!rrfDir.exists()) {
            rrfDir.mkdirs()
        }
        return if (rrfDir.exists()) rrfDir else context.cacheDir
    }

    /**
     * Converts a single AgreementEntity into a fully responsive HTML web document.
     * Works seamlessly on phone screens, laptop displays, and supports direct browser printing.
     */
    fun exportAgreementToHtml(context: Context, agreement: AgreementEntity): File {
        val dir = getOutputDirectory(context)
        val fileName = "${agreement.baseFileName}_রেসপন্সিভ_এগ্রিমেন্ট.html"
        val outputFile = File(dir, fileName)

        val htmlContent = buildString {
            append("<!DOCTYPE html>\n")
            append("<html lang=\"bn\">\n")
            append("<head>\n")
            append("  <meta charset=\"UTF-8\">\n")
            append("  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n")
            append("  <title>${agreement.employeeName} - RRF এগ্রিমেন্ট ফরম</title>\n")
            append("  <style>\n")
            append("    @import url('https://fonts.googleapis.com/css2?family=Noto+Sans+Bengali:wght@400;600;700&display=swap');\n")
            append("    * { box-sizing: border-box; margin: 0; padding: 0; }\n")
            append("    body {\n")
            append("      font-family: 'Noto Sans Bengali', 'SolaimanLipi', 'Kalpurush', Arial, sans-serif;\n")
            append("      background-color: #f4f6f9;\n")
            append("      color: #1f2937;\n")
            append("      padding: 16px;\n")
            append("      line-height: 1.6;\n")
            append("    }\n")
            append("    .container {\n")
            append("      max-width: 850px;\n")
            append("      margin: 0 auto;\n")
            append("      background: #ffffff;\n")
            append("      border-radius: 12px;\n")
            append("      box-shadow: 0 4px 12px rgba(0,0,0,0.08);\n")
            append("      padding: 24px;\n")
            append("    }\n")
            append("    .action-bar {\n")
            append("      display: flex;\n")
            append("      justify-content: space-between;\n")
            append("      align-items: center;\n")
            append("      background: #e8f5e9;\n")
            append("      padding: 12px 18px;\n")
            append("      border-radius: 8px;\n")
            append("      margin-bottom: 20px;\n")
            append("      border: 1px solid #c8e6c9;\n")
            append("    }\n")
            append("    .btn {\n")
            append("      background-color: #059669;\n")
            append("      color: white;\n")
            append("      border: none;\n")
            append("      padding: 8px 16px;\n")
            append("      font-size: 14px;\n")
            append("      font-weight: bold;\n")
            append("      border-radius: 6px;\n")
            append("      cursor: pointer;\n")
            append("      text-decoration: none;\n")
            append("    }\n")
            append("    .btn:hover { background-color: #047857; }\n")
            append("    .header {\n")
            append("      text-align: center;\n")
            append("      border-bottom: 2px solid #059669;\n")
            append("      padding-bottom: 14px;\n")
            append("      margin-bottom: 20px;\n")
            append("    }\n")
            append("    .header h1 { color: #059669; font-size: 22px; margin-bottom: 4px; }\n")
            append("    .header h2 { color: #1e3a8a; font-size: 16px; font-weight: 600; }\n")
            append("    .header p { font-size: 12px; color: #6b7280; }\n")
            append("    .serial-badge {\n")
            append("      display: inline-block;\n")
            append("      background-color: #1e3a8a;\n")
            append("      color: white;\n")
            append("      padding: 4px 12px;\n")
            append("      border-radius: 20px;\n")
            append("      font-size: 12px;\n")
            append("      font-weight: bold;\n")
            append("      margin-top: 8px;\n")
            append("    }\n")
            append("    .section-title {\n")
            append("      background-color: #f3f4f6;\n")
            append("      border-left: 4px solid #059669;\n")
            append("      padding: 8px 12px;\n")
            append("      font-size: 15px;\n")
            append("      font-weight: bold;\n")
            append("      color: #111827;\n")
            append("      margin: 18px 0 10px 0;\n")
            append("    }\n")
            append("    table.data-table {\n")
            append("      width: 100%;\n")
            append("      border-collapse: collapse;\n")
            append("      margin-bottom: 14px;\n")
            append("    }\n")
            append("    table.data-table th, table.data-table td {\n")
            append("      border: 1px solid #e5e7eb;\n")
            append("      padding: 10px 12px;\n")
            append("      font-size: 13px;\n")
            append("      text-align: left;\n")
            append("    }\n")
            append("    table.data-table th {\n")
            append("      background-color: #f9fafb;\n")
            append("      width: 32%;\n")
            append("      color: #374151;\n")
            append("    }\n")
            append("    .stamp-box {\n")
            append("      border: 2px dashed #059669;\n")
            append("      background-color: #f0fdf4;\n")
            append("      padding: 16px;\n")
            append("      border-radius: 8px;\n")
            append("      margin: 16px 0;\n")
            append("    }\n")
            append("    .stamp-box h3 { color: #059669; font-size: 15px; margin-bottom: 8px; }\n")
            append("    .footer {\n")
            append("      margin-top: 30px;\n")
            append("      padding-top: 16px;\n")
            append("      border-top: 1px solid #e5e7eb;\n")
            append("      display: flex;\n")
            append("      justify-content: space-between;\n")
            append("      align-items: center;\n")
            append("      font-size: 12px;\n")
            append("      color: #6b7280;\n")
            append("    }\n")
            append("    @media print {\n")
            append("      body { background: none; padding: 0; }\n")
            append("      .container { box-shadow: none; max-width: 100%; border-radius: 0; padding: 0; }\n")
            append("      .action-bar { display: none !important; }\n")
            append("    }\n")
            append("    @media (max-width: 600px) {\n")
            append("      body { padding: 8px; }\n")
            append("      .container { padding: 14px; }\n")
            append("      table.data-table th, table.data-table td { padding: 6px 8px; font-size: 12px; }\n")
            append("      .action-bar { flex-direction: column; gap: 8px; text-align: center; }\n")
            append("    }\n")
            append("  </style>\n")
            append("</head>\n")
            append("<body>\n")
            append("  <div class=\"container\">\n")
            append("    <div class=\"action-bar\">\n")
            append("      <div><strong>RRF HR ম্যানেজমেন্ট:</strong> রেসপন্সিভ এগ্রিমেন্ট ফরম</div>\n")
            append("      <div><button class=\"btn\" onclick=\"window.print()\">🖨️ প্রিন্ট / PDF সংরক্ষণ</button></div>\n")
            append("    </div>\n")

            append("    <div class=\"header\">\n")
            append("      <h1>রুরাল রিকনস্ট্রাকশন ফাউন্ডেশন (RRF)</h1>\n")
            append("      <h2>এইচআর ডিপার্টমেন্ট - চাকুরীর চুক্তিপত্র ও অঙ্গীকারনামা</h2>\n")
            append("      <p>আরআরএফ ভবন, ঘোপ নওয়াপাড়া রোড, যশোর-৭৪০০</p>\n")
            append("      <div class=\"serial-badge\">সিরিয়াল নং: ${agreement.serialNo}</div>\n")
            append("    </div>\n")

            append("    <div class=\"section-title\">১. কর্মী পরিচিতি ও তথ্য</div>\n")
            append("    <table class=\"data-table\">\n")
            append("      <tr><th>কর্মীর নাম:</th><td><strong>${agreement.employeeName}</strong></td></tr>\n")
            append("      <tr><th>পিতার নাম:</th><td>${agreement.employeeFatherName}</td></tr>\n")
            append("      <tr><th>পদবী:</th><td>${agreement.designation}</td></tr>\n")
            append("      <tr><th>গ্রাম / মহল্লা:</th><td>${agreement.employeeVillage}</td></tr>\n")
            append("      <tr><th>ডাকঘর:</th><td>${agreement.employeePostOffice}</td></tr>\n")
            append("      <tr><th>উপজেলা / থানা:</th><td>${agreement.employeeUpazila}</td></tr>\n")
            append("      <tr><th>জেলা:</th><td>${agreement.employeeDistrict}</td></tr>\n")
            append("      <tr><th>ফরম জমাদানের তারিখ:</th><td>${agreement.submissionDate}</td></tr>\n")
            if (agreement.editCount > 0) {
                append("      <tr><th>সংশোধন নম্বর:</th><td>${agreement.editLabel}</td></tr>\n")
            }
            append("    </table>\n")

            append("    <div class=\"section-title\">২. জামিনদার পরিচিতি ও ঠিকানা</div>\n")
            append("    <table class=\"data-table\">\n")
            append("      <tr><th>জামিনদারের নাম:</th><td><strong>${agreement.guarantorName}</strong></td></tr>\n")
            append("      <tr><th>পিতার নাম:</th><td>${agreement.guarantorFatherName}</td></tr>\n")
            append("      <tr><th>মাতার নাম:</th><td>${agreement.guarantorMotherName}</td></tr>\n")
            append("      <tr><th>সম্পর্ক:</th><td>${agreement.effectiveGuarantorRelationship}</td></tr>\n")
            append("      <tr><th>এনআইডি (NID) নম্বর:</th><td>${agreement.guarantorNid}</td></tr>\n")
            append("      <tr><th>গ্রাম / মহল্লা:</th><td>${agreement.guarantorVillage}</td></tr>\n")
            append("      <tr><th>ডাকঘর:</th><td>${agreement.guarantorPostOffice}</td></tr>\n")
            append("      <tr><th>উপজেলা / থানা:</th><td>${agreement.guarantorUpazila}</td></tr>\n")
            append("      <tr><th>জেলা:</th><td>${agreement.guarantorDistrict}</td></tr>\n")
            append("    </table>\n")

            append("    <div class=\"stamp-box\">\n")
            append("      <h3>📜 ১০০ টাকার স্ট্যাম্প তথ্য</h3>\n")
            append("      <p>এই এগ্রিমেন্ট ফরমটি ১০০ টাকার জুডিশিয়াল স্ট্যাম্পে প্রিন্ট করার জন্য প্রসেস করা হয়েছে। সরকারি নিয়মানুযায়ী স্ট্যাম্প পেপারের উপরের অংশ খালি রেখে প্রিন্ট সম্পন্ন করতে হবে।</p>\n")
            append("    </div>\n")

            append("    <div class=\"stamp-box\" style=\"border-color: #1e3a8a; background-color: #eff6ff;\">\n")
            append("      <h3>📄 ২৫ টাকার প্রত্যয়নপত্র ও অঙ্গিকারনামা</h3>\n")
            append("      <p>কর্মকর্তা/কর্মচারী ও জামিনদার কর্তৃক স্বাক্ষরিত প্রত্যয়নপত্র ও তথ্য যাচাই ফরম সংযুক্ত রয়েছে।</p>\n")
            append("    </div>\n")

            if (agreement.assignedBranch.isNotBlank()) {
                append("    <div class=\"section-title\">৩. শাখা অফিস ও তথ্য যাচাই স্ট্যাটাস</div>\n")
                append("    <table class=\"data-table\">\n")
                append("      <tr><th>বরাদ্দকৃত শাখা:</th><td><strong>${agreement.assignedBranch}</strong></td></tr>\n")
                append("      <tr><th>যাচাই স্ট্যাটাস:</th><td>${agreement.verificationStatus}</td></tr>\n")
                if (!agreement.dispatchDate.isNull_or_blank()) {
                    append("      <tr><th>প্রেশনের তারিখ:</th><td>${agreement.dispatchDate}</td></tr>\n")
                }
                if (agreement.branchNotes.isNotBlank()) {
                    append("      <tr><th>শাখার মন্তব্য:</th><td>${agreement.branchNotes}</td></tr>\n")
                }
                append("    </table>\n")
            }

            append("    <div class=\"footer\">\n")
            append("      <div>রুরাল রিকনস্ট্রাকশন ফাউন্ডেশন (RRF) - এইচআর শাখা</div>\n")
            append("      <div>ডকুমেন্ট জেনারেটেড: " + System.currentTimeMillis() + "</div>\n")
            append("    </div>\n")
            append("  </div>\n")
            append("</body>\n")
            append("</html>\n")
        }

        FileOutputStream(outputFile).use { out ->
            out.write(htmlContent.toByteArray(Charsets.UTF_8))
        }

        return outputFile
    }

    private fun String?.isNull_or_blank(): Boolean = this == null || this.isBlank()

    /**
     * Exports all agreements list into a clean, searchable, responsive HTML report.
     */
    fun exportAllAgreementsToHtml(context: Context, agreements: List<AgreementEntity>): File {
        val dir = getOutputDirectory(context)
        val fileName = "RRF_HR_সকল_এগ্রিমেন্ট_রিপোর্ট.html"
        val outputFile = File(dir, fileName)

        val htmlContent = buildString {
            append("<!DOCTYPE html>\n")
            append("<html lang=\"bn\">\n")
            append("<head>\n")
            append("  <meta charset=\"UTF-8\">\n")
            append("  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n")
            append("  <title>RRF HR - সকল এগ্রিমেন্ট রিপোর্ট</title>\n")
            append("  <style>\n")
            append("    @import url('https://fonts.googleapis.com/css2?family=Noto+Sans+Bengali:wght@400;600;700&display=swap');\n")
            append("    body { font-family: 'Noto Sans Bengali', Arial, sans-serif; background: #f8fafc; padding: 20px; color: #1e293b; }\n")
            append("    .wrapper { max-width: 1100px; margin: 0 auto; background: #ffffff; padding: 24px; border-radius: 12px; box-shadow: 0 4px 10px rgba(0,0,0,0.05); }\n")
            append("    .header { text-align: center; margin-bottom: 24px; border-bottom: 2px solid #059669; padding-bottom: 12px; }\n")
            append("    .header h1 { color: #059669; font-size: 24px; }\n")
            append("    .btn-print { background: #059669; color: white; border: none; padding: 10px 20px; border-radius: 6px; cursor: pointer; font-weight: bold; margin-bottom: 16px; }\n")
            append("    table { width: 100%; border-collapse: collapse; margin-top: 12px; font-size: 13px; }\n")
            append("    th, td { border: 1px solid #cbd5e1; padding: 10px; text-align: left; }\n")
            append("    th { background: #f1f5f9; color: #0f172a; }\n")
            append("    tr:nth-child(even) { background: #f8fafc; }\n")
            append("    .badge { background: #dcfce7; color: #166534; padding: 3px 8px; border-radius: 12px; font-weight: bold; font-size: 11px; }\n")
            append("    @media print { .btn-print { display: none; } body { padding: 0; background: white; } .wrapper { box-shadow: none; } }\n")
            append("    @media (max-width: 768px) { table { font-size: 11px; } th, td { padding: 6px; } }\n")
            append("  </style>\n")
            append("</head>\n")
            append("<body>\n")
            append("  <div class=\"wrapper\">\n")
            append("    <div class=\"header\">\n")
            append("      <h1>রুরাল রিকনস্ট্রাকশন ফাউন্ডেশন (RRF)</h1>\n")
            append("      <h3>এইচআর ম্যানেজমেন্ট - সম্পূর্ণ এগ্রিমেন্ট তালিকা রিপোর্ট</h3>\n")
            append("      <p>মোট সংরক্ষিত এগ্রিমেন্ট: ${agreements.size} টি</p>\n")
            append("    </div>\n")
            append("    <button class=\"btn-print\" onclick=\"window.print()\">🖨️ এক্সেল/ওয়েব রিপোর্ট প্রিন্ট করুন</button>\n")
            append("    <table>\n")
            append("      <thead>\n")
            append("        <tr>\n")
            append("          <th>ক্রমিক</th>\n")
            append("          <th>সিরিয়াল নং</th>\n")
            append("          <th>কর্মীর নাম</th>\n")
            append("          <th>পদবী</th>\n")
            append("          <th>পিতার নাম</th>\n")
            append("          <th>ঠিকানা (গ্রাম, জেলা)</th>\n")
            append("          <th>জামিনদার</th>\n")
            append("          <th>তারিখ</th>\n")
            append("          <th>শাখা office</th>\n")
            append("        </tr>\n")
            append("      </thead>\n")
            append("      <tbody>\n")

            agreements.forEachIndexed { index, item ->
                append("        <tr>\n")
                append("          <td>${index + 1}</td>\n")
                append("          <td><strong>${item.serialNo}</strong></td>\n")
                append("          <td>${item.employeeName}</td>\n")
                append("          <td>${item.designation}</td>\n")
                append("          <td>${item.employeeFatherName}</td>\n")
                append("          <td>${item.employeeVillage}, ${item.employeeDistrict}</td>\n")
                append("          <td>${item.guarantorName} (${item.effectiveGuarantorRelationship})</td>\n")
                append("          <td>${item.submissionDate}</td>\n")
                append("          <td><span class=\"badge\">${if (item.assignedBranch.isNotBlank()) item.assignedBranch else "অনির্ধারিত"}</span></td>\n")
                append("        </tr>\n")
            }

            append("      </tbody>\n")
            append("    </table>\n")
            append("  </div>\n")
            append("</body>\n")
            append("</html>\n")
        }

        FileOutputStream(outputFile).use { out ->
            out.write(htmlContent.toByteArray(Charsets.UTF_8))
        }

        return outputFile
    }
}
