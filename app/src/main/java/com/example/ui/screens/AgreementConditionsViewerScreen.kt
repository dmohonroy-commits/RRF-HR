package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.util.AgreementConditionsPdfGenerator
import com.example.util.ShareHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgreementConditionsViewerScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "চুক্তিপত্রের নিয়মাবলী ও শর্তাবলী",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "নিয়োগ প্রাপ্তির প্রয়োজনীয় শর্তাবলী সমূহ",
                            fontSize = 11.5.sp,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("btn_back_conditions")) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "ফিরে যান"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            val pdf = AgreementConditionsPdfGenerator.generateConditionsPdf(context)
                            ShareHelper.shareFile(context, pdf, "নিয়োগ প্রাপ্তির প্রয়োজনীয় শর্তাবলী (A4 PDF)")
                        },
                        modifier = Modifier.testTag("btn_share_conditions")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "শেয়ার PDF")
                    }
                    IconButton(
                        onClick = {
                            val pdf = AgreementConditionsPdfGenerator.generateConditionsPdf(context)
                            ShareHelper.openFile(context, pdf)
                        },
                        modifier = Modifier.testTag("btn_print_conditions")
                    ) {
                        Icon(Icons.Default.Print, contentDescription = "প্রিন্ট PDF")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        bottomBar = {
            Surface(
                color = Color.White,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val pdf = AgreementConditionsPdfGenerator.generateConditionsPdf(context)
                            ShareHelper.shareFile(context, pdf, "নিয়োগ প্রাপ্তির প্রয়োজনীয় শর্তাবলী (A4 PDF)")
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("শেয়ার", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            val pdf = AgreementConditionsPdfGenerator.generateConditionsPdf(context)
                            ShareHelper.openFile(context, pdf)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1.3f)
                            .height(48.dp)
                    ) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("A4 PDF / প্রিন্ট", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF8FAFC))
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                shadowElevation = 3.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    // Organization Header
                    Text(
                        text = "রুরাল রিকনস্ট্রাকশন ফাউন্ডেশন",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF0F172A),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "আরআরএফ ভবন, সিএন্ডবি রোড, কারবালা, যশোর-৭৪০০",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(10.dp))
                    HorizontalDivider(color = Color(0xFFE2E8F0))
                    Spacer(Modifier.height(12.dp))

                    Text(
                        text = "নিয়োগ প্রাপ্তির প্রয়োজনীয় শর্তাবলী সমূহ :",
                        fontSize = 15.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(14.dp))

                    // Clause 1
                    ConditionParagraph(
                        number = "০১।",
                        text = "নিয়োগগ্রহণে ইচ্ছুক প্রার্থীর বাবা (বাবার অবর্তমানে বড়ভাই/বিবাহিত হলে শ্বশুর) জামিনদার হবেন। জামিনদারের সাথে ৩০০ টাকার (১০০ টাকার তিনটি) ননজুডিশিয়াল স্ট্যাম্পে চুক্তি সম্পন্ন করা হবে। উক্ত স্ট্যাম্পটি জামিনদারের নামে ক্রয় করতে হবে এবং জামিনদারকে স্ব-শরীরে উপস্থিত হয়ে চুক্তি সম্পন্ন করতে হবে। জামিনদারের এক কপি পাসপোর্ট সাইজের ছবি ও জাতীয় পরিচয়পত্রের ফটোকপি জমা দিতে হবে।"
                    )

                    Spacer(Modifier.height(12.dp))

                    // Clause 2 + Table
                    ConditionParagraph(
                        number = "০২।",
                        text = "ফেরতযোগ্য নির্ধারিত হারে জামানত জমা দিতে হবে।"
                    )

                    Spacer(Modifier.height(8.dp))

                    // Deposit Table
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            // Table Header
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFF1F5F9))
                                    .padding(vertical = 8.dp, horizontal = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("ক্রম", fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(36.dp), textAlign = TextAlign.Center)
                                Text("পদবী", fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                                Text("জামানতের পরিমান", fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(95.dp), textAlign = TextAlign.End)
                            }
                            HorizontalDivider(color = Color(0xFFCBD5E1))

                            val rows = listOf(
                                Triple("০১", "সহকারী পরিচালক", "৪০,০০০/-"),
                                Triple("০২", "আঞ্চলিক ব্যবস্থাপক, সহকারী আঞ্চলিক ব্যবস্থাপক, বিজনেস ডেভেলপমেন্ট ম্যানেজার", "৩০,০০০/-"),
                                Triple("০৩", "শাখা ব্যবস্থাপক, উপ-শাখা ব্যবস্থাপক, বিজনেস ডেভেলপমেন্ট অফিসার", "২৫,০০০/-"),
                                Triple("০৪", "অফিসার (অ্যাকাউন্টস)", "২০,০০০/-"),
                                Triple("০৫", "অফিসার (ঋণ), সহকারী অফিসার (ঋণ)", "১৫,০০০/-"),
                                Triple("০৬", "সার্ভিস স্টাফ", "৫,০০০/-")
                            )

                            rows.forEachIndexed { idx, r ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(if (idx % 2 == 1) Color(0xFFF8FAFC) else Color.White)
                                        .padding(vertical = 7.dp, horizontal = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(r.first, fontSize = 11.5.sp, modifier = Modifier.width(36.dp), textAlign = TextAlign.Center)
                                    Text(r.second, fontSize = 11.5.sp, modifier = Modifier.weight(1f), lineHeight = 16.sp)
                                    Text(r.third, fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.width(95.dp), textAlign = TextAlign.End, color = MaterialTheme.colorScheme.primary)
                                }
                                if (idx < rows.size - 1) {
                                    HorizontalDivider(color = Color(0xFFF1F5F9))
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    // Remaining Clauses 3 to 10
                    val remaining = listOf(
                        "০৩।" to "নিম্নলিখিত প্রত্যয়নপত্র মোতাবেক স্থানীয় ২ জন বিশিষ্ট ব্যক্তির (শিক্ষক/ব্যবসায়ী/ডাক্তার) নামে ২টি ২৫ টাকার ননজুডিশিয়াল স্ট্যাম্প ক্রয় করতে হবে এবং উক্ত স্ট্যাম্পে তাদের নিকট হতে (নিম্নলিখিত প্রত্যয়ন অনুসারে) প্রত্যয়ন পত্র এবং উক্ত ব্যক্তির জাতীয় পরিচয়পত্রের ফটোকপি ও ১ কপি রঙিন ছবি জমা দিতে হবে।",
                        "০৪।" to "নমিনি-এর জাতীয় পরিচয়পত্রের ফটোকপি ও ১ (ল্যাব প্রিন্ট) কপি রঙিন ছবি জমা দিতে হবে।",
                        "০৫।" to "শিক্ষাগত যোগ্যতার সকল একাডেমিক পাশের মূল সার্টিফিকেট সমূহ চাকুরী কালীন সময়ের জন্য সংস্থায় জমা রাখতে হবে।",
                        "০৬।" to "আইডি কার্ডের জন্য স্ট্যাম্প/পাসপোর্ট সাইজের সদ্যতোলা ২ কপি রঙিন ছবি, ব্লাড গ্রুপ রিপোর্ট, জাতীয় পরিচয়পত্রের ফটোকপি, কোভিড-১৯ ভ্যাকসিনেশনের সার্টিফিকেট এবং নগদ ২০০ টাকা অফিসে জমা দিতে হবে।",
                        "০৭।" to "সংস্থায় কর্মএলাকায় যেকোনো স্থানে কাজের মানসিকতা থাকতে হবে।",
                        "০৮।" to "প্রার্থীর নামে ১টি ডামি পেপার (কার্টিজ পেপার) সঙ্গে আনতে হবে।",
                        "০৯।" to "উপরোক্ত টেবিলের ক্রম ০১ এর পদবী সমূহের জন্য ড্রাইভিং লাইসেন্স, মোটর সাইকেলের রেজিস্ট্রেশনের ফটোকপি এবং পূর্ববর্তী প্রতিষ্ঠান সমূহের চাকুরীর চূড়ান্ত ছাড়পত্র ও অভিজ্ঞতা সনদ এর ফটোকপি আনতে হবে।",
                        "১০।" to "সকল প্রকার ছবি ডিজিটাল ল্যাব প্রিন্ট হতে হবে।"
                    )

                    for (item in remaining) {
                        ConditionParagraph(number = item.first, text = item.second)
                        Spacer(Modifier.height(10.dp))
                    }

                    Spacer(Modifier.height(12.dp))
                    HorizontalDivider(color = Color(0xFFCBD5E1))
                    Spacer(Modifier.height(12.dp))

                    // Sample Testimonial (প্রত্যয়ন পত্র নমুনা)
                    Text(
                        text = "প্রত্যয়ন পত্র (নমুনা)",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(8.dp))

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFF8FAFC),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "আমি এই মর্মে প্রত্যয়ন করিতেছি যে, নাম : ..................................................................... পিতার নাম : .............................................................. মাতার নাম : .............................................................. ঠিকানা-গ্রাম : .............................................................. উপজেলা : .............................................................. জেলা : .............................................................. কে আমি ব্যক্তিগত ভাবে চিনি এবং জানি। আমার জানা মতে সে কোন অসামাজিক ও অনৈতিক কার্যক্রমের সাথে যুক্ত নয়। উক্ত ব্যক্তি দ্বারা কোন অনিয়ম বা সমাজ ও রাষ্ট্রবিরোধী কোন কার্যক্রম সংঘটিত হলে আমি তার দায়িত্ব গ্রহণ করিলাম।",
                                fontSize = 12.5.sp,
                                color = Color(0xFF334155),
                                lineHeight = 20.sp
                            )

                            Spacer(Modifier.height(16.dp))

                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.End
                            ) {
                                Text("স্বাক্ষর : ................................................................", fontSize = 12.sp)
                                Spacer(Modifier.height(4.dp))
                                Text("নাম : ................................................................", fontSize = 12.sp)
                                Spacer(Modifier.height(4.dp))
                                Text("পেশা : ................................................................", fontSize = 12.sp)
                                Spacer(Modifier.height(4.dp))
                                Text("ঠিকানা : ................................................................", fontSize = 12.sp)
                                Spacer(Modifier.height(4.dp))
                                Text("মোবাইল নং : ................................................................", fontSize = 12.sp)
                                Spacer(Modifier.height(4.dp))
                                Text("জাতীয় পরিচয়পত্র নং : ................................................", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ConditionParagraph(number: String, text: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = number,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.width(32.dp)
        )
        Text(
            text = text,
            fontSize = 13.sp,
            color = Color(0xFF334155),
            lineHeight = 19.sp,
            modifier = Modifier.weight(1f)
        )
    }
}
