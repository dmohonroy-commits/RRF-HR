package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AgreementEntity
import com.example.ui.components.ShareSheetDialog
import com.example.ui.theme.*
import com.example.util.AgreementDocumentContent
import com.example.util.BanglaTextValidator
import com.example.util.PdfGenerator
import com.example.util.ShareHelper
import com.example.util.WordDocGenerator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgreementPreviewScreen(
    agreement: AgreementEntity,
    isAdmin: Boolean,
    onBack: () -> Unit,
    onTogglePrintStatus: (AgreementEntity) -> Unit,
    onEditAgreement: ((AgreementEntity) -> Unit)? = null
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }
    var useStampMargin by remember { mutableStateOf(true) }
    var showShareSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("এগ্রিমেন্ট প্রিভিউ", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            if (agreement.editCount > 0) {
                                Spacer(Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color(0xFFFFC107)
                                ) {
                                    Text(
                                        agreement.editLabel,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }
                        Text(
                            "${agreement.baseFileName}.pdf (${agreement.serialNo})",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("preview_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    if (onEditAgreement != null) {
                        IconButton(
                            onClick = { onEditAgreement(agreement) },
                            modifier = Modifier.testTag("preview_edit_button")
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.White)
                        }
                    }
                    IconButton(
                        onClick = { showShareSheet = true },
                        modifier = Modifier.testTag("preview_share_button")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Share", tint = Color.White)
                    }
                    IconButton(
                        onClick = {
                            val pdfFile = PdfGenerator.generateAgreementPdf(context, agreement, useStampMargin)
                            ShareHelper.openFile(context, pdfFile)
                        },
                        modifier = Modifier.testTag("preview_print_button")
                    ) {
                        Icon(Icons.Default.Print, contentDescription = "Print", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                )
            )
        },
        bottomBar = {
            Surface(
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val pdfFile = PdfGenerator.generateAgreementPdf(context, agreement, useStampMargin)
                                ShareHelper.openFile(context, pdfFile)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_pdf_download"),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = null, Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("PDF প্রিন্ট")
                        }

                        Button(
                            onClick = {
                                val docFile = WordDocGenerator.generateAgreementDoc(context, agreement, useStampMargin)
                                ShareHelper.openFile(context, docFile)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_word_download"),
                            colors = ButtonDefaults.buttonColors(containerColor = LightGreenDark)
                        ) {
                            Icon(Icons.Default.Article, contentDescription = null, Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("ওয়ার্ড (.doc)")
                        }
                    }

                    if (onEditAgreement != null) {
                        Spacer(Modifier.height(8.dp))
                        Button(
                            onClick = { onEditAgreement(agreement) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("btn_preview_edit"),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE65100))
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("তথ্য সংশোধন / এডিট করুন")
                        }
                    }

                    if (isAdmin) {
                        Spacer(Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = { onTogglePrintStatus(agreement) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = if (agreement.isPrinted) AlertRed else LightGreenDark
                            )
                        ) {
                            Icon(
                                if (agreement.isPrinted) Icons.Default.Close else Icons.Default.Check,
                                contentDescription = null,
                                Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                if (agreement.isPrinted) "প্রিন্ট সম্পন্ন বাতিল করুন" else "১০০ টাকার স্ট্যাম্পে প্রিন্ট সম্পন্ন চিহ্নিত করুন"
                            )
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Status and Stamp Margin Bar
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Stamp status badge
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (agreement.isPrinted) LightGreenContainer else Color(0xFFFFF3CD)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    if (agreement.isPrinted) Icons.Default.CheckCircle else Icons.Default.AccessTime,
                                    contentDescription = null,
                                    tint = if (agreement.isPrinted) LightGreenDark else Color(0xFFB78103),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    if (agreement.isPrinted) "স্ট্যাম্পে প্রিন্ট সম্পন্ন (${agreement.printDate ?: ""})" else "প্রিন্ট অপেক্ষমান",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (agreement.isPrinted) LightGreenDark else Color(0xFF856404)
                                )
                            }
                        }

                        // Stamp margin toggle
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("১০০ টাকার স্ট্যাম্প মার্জিন (৪.৫ ইঞ্চি)", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                            Spacer(Modifier.width(6.dp))
                            Switch(
                                checked = useStampMargin,
                                onCheckedChange = { useStampMargin = it },
                                modifier = Modifier.height(28.dp)
                            )
                        }
                    }
                }
            }

            // Agreement 1 / Agreement 2 / সত্যপাঠ Tabs (3 Legal Pages)
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Agreement 1 (পৃষ্ঠা ১)", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Agreement 2 (পৃষ্ঠা ২)", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("সত্যপাঠ (পৃষ্ঠা ৩)", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                )
            }

            // Document Sheet Display
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFE9EEF4))
                    .padding(12.dp)
            ) {
                Card(
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                start = 20.dp,
                                end = 20.dp,
                                top = if (useStampMargin) 48.dp else 24.dp,
                                bottom = 28.dp
                            )
                    ) {
                        when (selectedTab) {
                            0 -> {
                            // Agreement 1
                            if (useStampMargin) {
                                Surface(
                                    color = Color(0xFFFFF8E7),
                                    shape = RoundedCornerShape(8.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2C482)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(140.dp)
                                        .padding(bottom = 16.dp)
                                ) {
                                    Box(
                                        modifier = Modifier.fillMaxSize().padding(10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            "↑ পৃষ্ঠা ১: ১০০ টাকার স্ট্যাম্পের মূল সিলমোহরের জন্য ৪.৫ ইঞ্চি স্থান ফাঁকা (Legal Size)",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Color(0xFF8A6D3B),
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }

                            Text(
                                "জামানতনামা",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(Modifier.height(14.dp))

                            val rel = agreement.effectiveGuarantorRelationship
                            val nid = BanglaTextValidator.toBanglaDigits(agreement.guarantorNid)

                            Text(
                                "আমি ${agreement.guarantorName}, পিতা : ${agreement.guarantorFatherName}, মাতা : ${agreement.guarantorMotherName}, সম্পর্ক : $rel।",
                                fontSize = 12.5.sp,
                                lineHeight = 19.sp,
                                textAlign = TextAlign.Justify,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Text(
                                "ভোটার আইডি নংঃ $nid",
                                fontSize = 12.5.sp,
                                lineHeight = 19.sp
                            )
                            Text(
                                "বর্তমান ঠিকানা :",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                            Text(
                                "গ্রাম : ${agreement.guarantorVillage}, ডাকঘর : ${agreement.guarantorPostOffice}, উপজেলা : ${agreement.guarantorUpazila}, জেলা : ${agreement.guarantorDistrict}।",
                                fontSize = 12.5.sp,
                                lineHeight = 19.sp,
                                textAlign = TextAlign.Justify,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(Modifier.height(8.dp))

                            Text(
                                "রুরাল রিকনস্ট্রাকশন ফাউন্ডেশন এ ${agreement.designation} পদে নিয়োজিত জনাব ${agreement.employeeName}, পিতাঃ ${agreement.employeeFatherName}।",
                                fontSize = 12.5.sp,
                                lineHeight = 19.sp,
                                textAlign = TextAlign.Justify,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Text(
                                "গ্রাম : ${agreement.employeeVillage}, ডাকঘর : ${agreement.employeePostOffice}, উপজেলা : ${agreement.employeeUpazila}, জেলা : ${agreement.employeeDistrict}।",
                                fontSize = 12.5.sp,
                                lineHeight = 19.sp,
                                textAlign = TextAlign.Justify,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Text(
                                "এর জন্য এবং তাহার পক্ষে সার্বিক দায়-দায়িত্ব স্বীকার করিয়া জামিনদার হিসাবে নিম্নলিখিত শর্তাবলী সাপেক্ষে অঙ্গীকারবদ্ধ হইলাম :",
                                fontSize = 12.5.sp,
                                lineHeight = 19.sp,
                                textAlign = TextAlign.Justify,
                                modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                            )
                            Spacer(Modifier.height(10.dp))

                            Text(
                                "শর্তাবলী :-",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(6.dp))

                            Text(
                                "১। যেহেতু রুরাল রিকনস্ট্রাকশন ফাউন্ডেশন জনাব ${agreement.employeeName} কে ${agreement.designation} পদে চাকুরী প্রদান করিয়াছে সেহেতু আমি জনাব ${agreement.guarantorName} এর জন্য জামিনদার বহাল থাকিয়া রুরাল রিকনস্ট্রাকশন ফাউন্ডেশন বরাবরে অত্র জামানত নামা প্রদান করিলাম।",
                                fontSize = 12.5.sp,
                                lineHeight = 19.sp,
                                textAlign = TextAlign.Justify,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(Modifier.height(8.dp))

                            Text(
                                "২। রুরাল রিকনস্ট্রাকশন ফাউন্ডেশন এ চাকুরীরত থাকাকালীন উল্লেখিত ব্যক্তির কর্তব্যকাজে অবহেলা, ইচ্ছাকৃত ত্রুটি, স্বীয়-স্বার্থ আদায়ের লক্ষ্যে উদ্দেশ্য প্রণোদিতভাবে কোন কার্য সম্পাদন ফৌজদারী বা দেওয়ানী আইনে শাস্তিযোগ্য অপরাধের দ্বারা প্রত্যক্ষ বা পরোক্ষভাবে রুরাল রিকনস্ট্রাকশন ফাউন্ডেশন এর কোন ক্ষতিসাধন, দেশের প্রচলিত আইন কানুন, নিয়ম শৃঙ্খলা ও বিধি বিধানের পরিপন্থী কোন বে- আইনী বা অনৈতিক কাজে নিজেকে জড়িত করা, রুরাল রিকনস্ট্রাকশন ফাউন্ডেশন এর প্রাতিষ্ঠানিক বা প্রশাসনিক আইন বা বেআইনী বা নিয়ম শৃঙ্খলা পরিপন্থি কোন কর্মকান্ডে জড়িত হওয়া অথবা প্রত্যক্ষ বা পরোক্ষভাবে রুরাল রিকনস্ট্রাকশন ফাউন্ডেশনের আর্থিক ক্ষতি সাধন করা, ব্যাংক হতে নগদ উত্তোলন বা নগদে অথবা নানাবিধ উপায়ে আর্থিক সুবিধা লাভ করা কিংবা অর্থ আত্নসাৎ করা, রুরাল রিকনস্ট্রাকশন ফাউন্ডেশন এর কোন স্থাবর বা অস্থাবর সম্পত্তি বিনষ্ট বা হস্তগত করা কিংবা তার ক্ষতি সাধন করা, রুরাল রিকনস্ট্রাকশন ফাউন্ডেশন এর সুনাম ক্ষুন্ন হইতে পারে এবং রাষ্ট্রবিরোধী কোন কার্যকলাপে জড়িত হওয়া বা প্রত্যক্ষ ও পরোক্ষভাবে ইত্যাদি যে কোন প্রকার কাজে দায়ী বা দোষী হইলে আমি তাহার সকল প্রকার দায় দায়িত্ব নিজে বহন করিব বা করিতে আইনতঃ বাধ্য থাকিব এবং রুরাল রিকনস্ট্রাকশন ফাউন্ডেশন এর দাবীকৃত বা আদালত কর্তৃক ঘোষিত ও নির্ধারিত যে কোন অংকের আর্থিক ক্ষতিপূরণ প্রদানে বাধ্য থাকিব।",
                                fontSize = 12.5.sp,
                                lineHeight = 19.sp,
                                textAlign = TextAlign.Justify,
                                modifier = Modifier.fillMaxWidth()
                            )

                            }
                            1 -> {
                                // Agreement 2 (পৃষ্ঠা ২: শর্ত ৩, ৪, ৫)
                                if (useStampMargin) {
                                    Surface(
                                        color = Color(0xFFFFF8E7),
                                        shape = RoundedCornerShape(8.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2C482)),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(140.dp)
                                            .padding(bottom = 16.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier.fillMaxSize().padding(10.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                "↑ পৃষ্ঠা ২: ১০০ টাকার স্ট্যাম্পের মূল সিলমোহরের জন্য ৪.৫ ইঞ্চি স্থান ফাঁকা (Legal Size)",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = Color(0xFF8A6D3B),
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }
                                }

                                Text(
                                    "৩। উল্লেখিত ব্যক্তি সম্পূর্ণ সততা, বিশ্বস্ততা, শৃঙ্খলা ও নিয়মানুবর্তিতার সহিত তাহার উপর অর্পিত দায়িত্ব ও কর্তব্য পালন করিবেন।",
                                    fontSize = 12.5.sp,
                                    lineHeight = 19.sp,
                                    textAlign = TextAlign.Justify,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(Modifier.height(12.dp))

                                Text(
                                    "৪। আমি জামিনদার হিসাবে আমার দায়িত্ব পালন না করিলে বা দায়িত্ব পালনে কোন প্রকার অনীহা প্রকাশ করিলে কিংবা প্রদত্ত অঙ্গীকার ভঙ্গ করিলে আমার বিরুদ্ধে রুরাল রিকনস্ট্রাকশন ফাউন্ডেশন কর্তৃপক্ষ অত্র জামানতনামা বলে আইন অনুযায়ী যে কোন প্রকার ব্যবস্থা গ্রহণ করিতে পারিবেন এবং সে ক্ষেত্রে আমার কোন প্রকার ওজর আপত্তি সর্ব আদালতে অগ্রাহ্য, বাতিল ও নামঞ্জুর বলিয়া ঘোষিত হইবে।",
                                    fontSize = 12.5.sp,
                                    lineHeight = 19.sp,
                                    textAlign = TextAlign.Justify,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(Modifier.height(12.dp))

                                Text(
                                    "৫। আমি এই মর্মে অঙ্গীকার ও স্বীকার করিতেছি যে, উল্লেখিত ব্যক্তির যে কোন প্রকার বে আইনী কার্যকলাপ, নিয়মশৃঙ্খলা ও অনৈতিক আচার আচরণের জন্য এবং ২ নং অনুচ্ছেদে বর্ণিত যে কোন কারণে অথবা অর্থ আত্নসাতের দায়ে উল্লেখিত ব্যক্তি দোষী সাব্যস্ত হলে আমি নগদ অর্থে ক্ষতিপূরণ দিতে বাধ্য রহিলাম বা আইনতঃ বাধ্য থাকিব। রুরাল রিকনস্ট্রাকশন ফাউন্ডেশন কর্তৃপক্ষ আমার প্রদত্ত অত্র জামানতনামা বলে আমার যে কোন স্থাবর বা অস্থাবর সম্পত্তি আদালতযোগে উপযুক্ত আইনের আশ্রয়ে বাজেয়াপ্ত ঘোষণা ক্রমে উপরে ঘোষিত টাকার সমপরিমাণ ক্ষতিপূরণ আমার নিকট হইতে আদায় করিতে পারিবেন।",
                                    fontSize = 12.5.sp,
                                    lineHeight = 19.sp,
                                    textAlign = TextAlign.Justify,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            2 -> {
                                // সত্যপাঠ ও সাক্ষীগণ (পৃষ্ঠা ৩: ১০০ টাকার স্ট্যাম্প স্থান ও ডানপাশে প্রান্তিকৃত স্বাক্ষর)
                                if (useStampMargin) {
                                    Surface(
                                        color = Color(0xFFFFF8E7),
                                        shape = RoundedCornerShape(8.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2C482)),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(140.dp)
                                            .padding(bottom = 16.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier.fillMaxSize().padding(10.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                "↑ সত্যপাঠ (পৃষ্ঠা ৩): ১০০ টাকার স্ট্যাম্পের মূল সিলমোহরের জন্য ৪.৫ ইঞ্চি স্থান ফাঁকা (Legal Size)",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = Color(0xFF8A6D3B),
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }
                                }

                                Text(
                                    "সত্যপাঠ",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(Modifier.height(12.dp))

                                val banglaDate = BanglaTextValidator.toBanglaDigits(agreement.submissionDate)
                                Text(
                                    "আমি উক্ত শর্তাবলী স্বজ্ঞানে, সুস্থ শরীরে এবং কাহারো দ্বারা প্ররোচিত না হইয়া এবং ইহার ভবিষ্যৎ ফলাফল ভাবিয়া চিন্তিয়া, বুঝিয়া-গুনিয়া ও পড়িয়া স্বেচ্ছায় অঙ্গীকারাবদ্ধ হইয়া নিম্নে বর্ণিত সাক্ষীগণের উপস্থিতিতে আমার নাম স্বাক্ষর করিলাম।",
                                    fontSize = 12.5.sp,
                                    lineHeight = 19.sp,
                                    textAlign = TextAlign.Justify,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    "তারিখ : $banglaDate খ্রিষ্টাব্দ।",
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Normal
                                )

                                Spacer(Modifier.height(28.dp))
                                Text(
                                    "স্বাক্ষীগণের স্বাক্ষর",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Normal
                                )
                                Spacer(Modifier.height(14.dp))

                                // Witness 1 & Guarantor (Right side shifted all the way to the right, internally left-aligned)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1.05f)) {
                                        Text("১। স্বাক্ষর : . . . . . . . . . . . . . . . . . . . . . . .", fontSize = 13.sp)
                                        Text("   নাম : . . . . . . . . . . . . . . . . . . . . . . . . . . . .", fontSize = 13.sp)
                                        Text("   স্বামী/পিতার নাম : . . . . . . . . . . . . . . . . .", fontSize = 13.sp)
                                        Text("   পূর্ণ ঠিকানা : . . . . . . . . . . . . . . . . . . . . . . . .", fontSize = 13.sp)
                                    }
                                    Box(
                                        modifier = Modifier.weight(0.95f),
                                        contentAlignment = Alignment.TopEnd
                                    ) {
                                        Column(horizontalAlignment = Alignment.Start) {
                                            Text(
                                                "জামিনদারের টিপসহি",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Normal
                                            )
                                            Spacer(Modifier.height(34.dp))
                                            Text(
                                                "জামিনদারের স্বাক্ষর",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Normal
                                            )
                                            Text(
                                                "তারিখ : $banglaDate খ্রিষ্টাব্দ।",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Normal
                                            )
                                        }
                                    }
                                }
                                Spacer(Modifier.height(22.dp))

                                // Witness 2 & Verification (Right side shifted all the way to the right, internally left-aligned)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1.05f)) {
                                        Text("২। স্বাক্ষর : . . . . . . . . . . . . . . . . . . . . . . .", fontSize = 13.sp)
                                        Text("   নাম : . . . . . . . . . . . . . . . . . . . . . . . . . . . .", fontSize = 13.sp)
                                        Text("   স্বামী/পিতার নাম : . . . . . . . . . . . . . . . . .", fontSize = 13.sp)
                                        Text("   পূর্ণ ঠিকানা : . . . . . . . . . . . . . . . . . . . . . . . .", fontSize = 13.sp)
                                    }
                                    Box(
                                        modifier = Modifier.weight(0.95f),
                                        contentAlignment = Alignment.TopEnd
                                    ) {
                                        Column(horizontalAlignment = Alignment.Start) {
                                            Text(
                                                "উল্লেখিত জামিনদার আমার সম্মুখে",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Normal
                                            )
                                            Text(
                                                "তার নিজ নাম সই করিয়াছে।",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Normal
                                            )
                                        }
                                    }
                                }
                                Spacer(Modifier.height(22.dp))

                                // Witness 3 & Advocate (Right side shifted all the way to the right, internally left-aligned)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1.05f)) {
                                        Text("৩। স্বাক্ষর : . . . . . . . . . . . . . . . . . . . . . . .", fontSize = 13.sp)
                                        Text("   নাম : . . . . . . . . . . . . . . . . . . . . . . . . . . . .", fontSize = 13.sp)
                                        Text("   স্বামী/পিতার নাম : . . . . . . . . . . . . . . . . .", fontSize = 13.sp)
                                        Text("   পূর্ণ ঠিকানা : . . . . . . . . . . . . . . . . . . . . . . . .", fontSize = 13.sp)
                                    }
                                    Box(
                                        modifier = Modifier.weight(0.95f),
                                        contentAlignment = Alignment.TopEnd
                                    ) {
                                        Column(horizontalAlignment = Alignment.Start) {
                                            Text(
                                                "এ্যাডভোকেটঃ",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Normal
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showShareSheet) {
        ShareSheetDialog(
            agreement = agreement,
            useStampMargin = useStampMargin,
            onDismiss = { showShareSheet = false }
        )
    }
}
