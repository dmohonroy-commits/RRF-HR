package com.example.ui.screens

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.util.AppGuidelinesPdfGenerator
import com.example.util.ShareHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

data class TutorialStep(
    val stepNo: Int,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val primaryColor: Color,
    val visualBadge: String,
    val bulletPoints: List<String>,
    val narrationBengali: String,
    val sampleFields: List<Pair<String, String>>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppGuidelinesScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val isDark = isSystemInDarkTheme()
    val scope = rememberCoroutineScope()
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Video & Voice Tutorial, 1: PDF User Guide

    // Tutorial Steps Definition
    val tutorialSteps = remember {
        listOf(
            TutorialStep(
                stepNo = 1,
                title = "পরিচিতি ও অটো-ফিল সুবিধা",
                subtitle = "অ্যাপের মূল সুবিধা ও পরিচিতি",
                icon = Icons.Default.AutoAwesome,
                primaryColor = Color(0xFF0F766E),
                visualBadge = "মাস্টার পোর্টাল",
                bulletPoints = listOf(
                    "রুরাল রিকনস্ট্রাকশন ফাউন্ডেশনের সকল এগ্রিমেন্ট ও স্ট্যাম্প ফরম একটি অ্যাপেই অন্তর্ভুক্ত।",
                    "স্মার্ট অটো-ফিল: একটি ফরমে তথ্য পূরণ করলে পরবর্তী ফরমগুলোতে স্বয়ংক্রিয়ভাবে বসে যায়।",
                    "অটো-ফিল হওয়া ছকসমূহ সবুজ কালারে লকড থাকে যাতে বারবার টাইপ করা না লাগে।"
                ),
                narrationBengali = "রুরাল রিকনস্ট্রাকশন ফাউন্ডেশনের কর্মী চুক্তিপত্র ও এগ্রিমেন্ট ব্যবস্থাপনা পোর্টালে আপনাকে স্বাগতম। এই অ্যাপের মাধ্যমে আপনি ১০০ টাকার স্ট্যাম্প চুক্তিপত্র ও ২৫ টাকার প্রত্যয়ন পত্র সহ সকল এগ্রিমেন্ট ফরম সহজেই পূরণ ও প্রিন্ট করতে পারবেন। যেকোনো একটি ফরমে তথ্য দিলে বাকি ফরমগুলোতে তা স্বয়ংক্রিয়ভাবে সবুজ কালারে অটো-ফিল হয়ে যাবে।",
                sampleFields = listOf(
                    "১০০ ও ২৫ টাকার স্ট্যাম্প" to "লিগ্যাল সাইজ প্রিন্ট",
                    "সকল এগ্রিমেন্ট ফরম" to "A4 সাইজ ফরম্যাট",
                    "অটো-ফিল সিস্টেম" to "সবুজ কালারে লকড"
                )
            ),
            TutorialStep(
                stepNo = 2,
                title = "১. আইডি কার্ডের তথ্য (Employee ID Card)",
                subtitle = "ইংরেজি ফরম - নির্ভুল স্টাফ প্রোফাইল",
                icon = Icons.Default.Badge,
                primaryColor = Color(0xFF1E3A8A),
                visualBadge = "English Form",
                bulletPoints = listOf(
                    "কর্মীর নাম, পিতার নাম ও মাতার নাম ইংরেজি অক্ষরে লিখুন।",
                    "ঠিকানা ক্রমান্বয়ে পূরণ করুন: জেলা, থানা, পোস্ট ও গ্রাম।",
                    "১১ ডিজিটের সচল মোবাইল নম্বর ও সঠিক ইমেইল এড্রেস প্রদান করুন।"
                ),
                narrationBengali = "প্রথম ধাপে কর্মীর আইডি কার্ডের তথ্য পূরণ করুন। এটি একটি ইংরেজি ফরম। এখানে কর্মীর নাম, পিতার নাম, মাতার নাম এবং ঠিকানা ইংরেজি ক্যাপিটাল অক্ষরে লিখুন। ১১ ডিজিটের মোবাইল নম্বর ও ইমেইল অবশ্যই নির্ভুলভাবে প্রদান করবেন।",
                sampleFields = listOf(
                    "Employee Name" to "Md. Ashraful Islam",
                    "Father's Name" to "Md. Rafiqul Islam",
                    "Mobile No" to "017XXXXXXXX"
                )
            ),
            TutorialStep(
                stepNo = 3,
                title = "২. প্রশিক্ষণ তথ্য ও অঙ্গীকারনামা",
                subtitle = "বাংলা ফরম - প্রশিক্ষণ অঙ্গীকারপত্র",
                icon = Icons.Default.School,
                primaryColor = Color(0xFF065F46),
                visualBadge = "বাংলা ফরম",
                bulletPoints = listOf(
                    "অংশগ্রহণকারী কর্মীর বিবরণ ও অঙ্গীকারনামা বাংলায় পূরণ করুন।",
                    "নাম, পিতা, গ্রাম, ডাকঘর, পোস্ট কোড, থানা ও জেলা বাংলায় লিখুন।",
                    "পদবী ড্রপডাউন থেকে সিলেক্ট করুন (অন্যান্য হলে পদবীর নাম লিখুন)।"
                ),
                narrationBengali = "দ্বিতীয় ধাপে প্রশিক্ষণের অঙ্গীকারনামা বাংলায় পূরণ করুন। এখানে কর্মীর নাম, পিতার নাম, গ্রাম, ডাকঘর, পোস্ট কোড, থানা ও জেলা বাংলায় লিখবেন। পদবী ড্রপডাউন থেকে নির্বাচন করুন। এখানে দেওয়া ঠিকানা স্বয়ংক্রিয়ভাবে পরবর্তী ফরমগুলোতে সিঙ্ক হয়ে যাবে।",
                sampleFields = listOf(
                    "কর্মীর নাম" to "মোঃ আশরাফুল ইসলাম",
                    "পিতার নাম" to "মোঃ রফিকুল ইসলাম",
                    "পদবী" to "অফিসার (ঋণ)"
                )
            ),
            TutorialStep(
                stepNo = 4,
                title = "৩. পরিচিত ব্যক্তির সম্পর্ক ঘোষণা",
                subtitle = "Kinship Declaration (English Form)",
                icon = Icons.Default.People,
                primaryColor = Color(0xFF4C1D95),
                visualBadge = "English Form",
                bulletPoints = listOf(
                    "আরআরএফ-এ কর্মরত কোনো আত্মীয় বা পরিচিত না থাকলে 'I have none' টিক দিন।",
                    "আত্মীয় কর্মরত থাকলে তার নাম, পদবী ও সম্পর্ক ইংরেজিতে পূরণ করুন।"
                ),
                narrationBengali = "তৃতীয় ধাপে আরআরএফ-এ কর্মরত পরিচিত আত্মীয়ের তথ্য ঘোষণা করুন। যদি কোনো আত্মীয় না থাকে, তবে আই হ্যাভ নান অপশনে টিক দিন। আর আত্মীয় থাকলে তার নাম, পদবী ও সম্পর্ক ইংরেজিতে লিখুন।",
                sampleFields = listOf(
                    "Employee Name" to "Md. Ashraful Islam",
                    "Kinship Status" to "I have none (টিক অপশন)",
                    "Declaration Date" to "স্বয়ংক্রিয় তারিখ"
                )
            ),
            TutorialStep(
                stepNo = 5,
                title = "৪. নমিনি তথ্য ফরম (Nominee Info)",
                subtitle = "Nominee Information in English",
                icon = Icons.Default.PersonSearch,
                primaryColor = Color(0xFFB45309),
                visualBadge = "English Form",
                bulletPoints = listOf(
                    "নমিনির পূর্ণ নাম, পিতার নাম ও মাতার নাম ইংরেজিতে পূরণ করুন।",
                    "কর্মীর স্থায়ী ঠিকানার সাথে নমিনির ঠিকানা একই হলে টিক অপশন দিন।",
                    "নমিনির জাতীয় পরিচয়পত্র (NID) নম্বর ও শেয়ারের শতকরা হার (100%) দিন।"
                ),
                narrationBengali = "চতুর্থ ধাপে নমিনির তথ্যাবলী ইংরেজিতে প্রদান করুন। নমিনির নাম ও পিতা মাতার নাম লিখুন। নমিনির ঠিকানা যদি কর্মীর ঠিকানার সাথে একই হয়, তবে বক্সে টিক দিলে স্বয়ংক্রিয়ভাবে ঠিকানা বসে যাবে। নমিনির জাতীয় পরিচয়পত্র নম্বর ও শেয়ারের হার শতভাগ প্রদান করুন।",
                sampleFields = listOf(
                    "Nominee's Name" to "Mrs. Fatema Begum",
                    "Relationship" to "Mother (মা)",
                    "Percent of Shares" to "100%"
                )
            ),
            TutorialStep(
                stepNo = 6,
                title = "৫. কর্মীর তথ্যানুসন্ধান ফরম (৩ পাতা)",
                subtitle = "Personal Info - শিক্ষাগত যোগ্যতা ও জামিনদার",
                icon = Icons.Default.Description,
                primaryColor = Color(0xFF047857),
                visualBadge = "৩ পাতার মূল ফরম",
                bulletPoints = listOf(
                    "১ম পাতা: কর্মীর বাংলা ও ইংরেজি নাম, NID (শুরুতে ফাঁকা থাকবে), বৈবাহিক অবস্থা, আয় ও ঠিকানা।",
                    "২য় পাতা: এসএসসি, এইচএসসি, ডিগ্রি বিষয় ও বোর্ড ড্রপডাউন থেকে নির্বাচন।",
                    "৩য় পাতা: পরিবারের সদস্য ও জামিনদারের তথ্যাবলী পূরণ করুন।"
                ),
                narrationBengali = "পঞ্চম ধাপে তিন পাতার বিস্তারিত তথ্যানুসন্ধান ফরম পূরণ করুন। প্রথম পাতায় কর্মীর জাতীয় পরিচয়পত্র ফাঁকা থাকবে, যা পূরণ করার পর সেভ হবে। দ্বিতীয় পাতায় এসএসসি, এইচএসসি এবং স্নাতক পরীক্ষার বোর্ড ও বিষয় ড্রপডাউন থেকে সিলেক্ট করবেন। তৃতীয় পাতায় পরিবার এবং জামিনদারের বিবরণ বাংলায় পূরণ করবেন।",
                sampleFields = listOf(
                    "জাতীয় পরিচয়পত্র" to "কর্মীর NID নম্বর",
                    "শিক্ষাগত যোগ্যতা" to "বিজ্ঞান/বাণিজ্য/মানবিক ড্রপডাউন",
                    "জামিনদারের তথ্য" to "পিতা/মাতা/ভাই/অন্যান্য"
                )
            ),
            TutorialStep(
                stepNo = 7,
                title = "৬. তথ্য যাচাই ফরম (Verification - ২ পাতা)",
                subtitle = "চেয়ারম্যান, মুচলেকা ও প্রতিবেশী সংক্রান্ত",
                icon = Icons.Default.VerifiedUser,
                primaryColor = Color(0xFF7C3AED),
                visualBadge = "২ পাতার যাচাই ফরম",
                bulletPoints = listOf(
                    "চেয়ারম্যানের নাম এবং গ্রাম, ডাকঘর, থানা ও জেলা বক্স আকারে পূরণ করুন।",
                    "মুচলেকায় কর্মীর নাম ও পিতার নাম স্বয়ংক্রিয়ভাবে বসে যাবে।",
                    "প্রতিবেশী ১ ও ২ এর বিবরণ (নাম, পেশা, ঠিকানা, মোবাইল ও NID) পূরণ করুন।",
                    "তদন্তকারী কর্মকর্তার মন্তব্যের ঘরটি কর্মীদের জন্য স্থায়ীভাবে লকড থাকবে।"
                ),
                narrationBengali = "ষষ্ঠ ধাপে দুই পাতার তথ্য যাচাই ফরম পূরণ করুন। এখানে চেয়ারম্যানের নাম ও বক্স আকারে পৃথক ঠিকানা লিখবেন। মুচলেকায় কর্মীর নাম স্বয়ংক্রিয়ভাবে চলে আসবে। দুই জন প্রতিবেশীর তথ্যাবলী পূরণ করবেন যা পরবর্তী ২৫ টাকার প্রত্যয়ন পত্রে স্বয়ংক্রিয়ভাবে চলে যাবে।",
                sampleFields = listOf(
                    "চেয়ারম্যানের ঠিকানা" to "গ্রাম, ডাকঘর, থানা, জেলা বক্সে",
                    "প্রতিবেশী ১ ও ২" to "প্রত্যয়ন পত্রের জন্য অটো সিঙ্ক",
                    "কর্মকর্তার মন্তব্য" to "লকড / অফিসিয়াল"
                )
            ),
            TutorialStep(
                stepNo = 8,
                title = "৭. ১০০ টাকার স্ট্যাম্প চুক্তিপত্র (৩ পাতা)",
                subtitle = "কর্মী জামানতনামা ও লিগ্যাল সাইজ প্রিন্ট",
                icon = Icons.Default.Gavel,
                primaryColor = Color(0xFF0C5D3E),
                visualBadge = "১০০ টাকার স্ট্যাম্প",
                bulletPoints = listOf(
                    "কর্মী ও জামিনদারের যৌথ অঙ্গীকারনামা ১০০ টাকার ৩টি স্ট্যাম্পের জন্য তৈরি।",
                    "স্ট্যাম্প পেপারের উপরের ফাঁকা অংশ বজায় রেখে নিখুঁত লিগ্যাল মার্জিনে প্রিন্ট হয়।",
                    "লাল নোট: প্রিন্ট করুন এবং প্রধান কার্যালয়ে গিয়ে জামিনদারের স্বাক্ষর করবে।"
                ),
                narrationBengali = "সপ্তম ধাপে একশত টাকার তিনটি নন জুডিশিয়াল স্ট্যাম্প চুক্তিপত্র প্রস্তুত হবে। স্ট্যাম্প পেপারের উপরের ফাঁকা অংশ বজায় রেখে এটি নিখুঁত লিগ্যাল সাইজে প্রিন্ট হয়। এটি প্রিন্ট করার পর প্রধান কার্যালয়ে নিয়ে গিয়ে জামিনদারের স্বাক্ষর নিতে হবে।",
                sampleFields = listOf(
                    "স্ট্যাম্প মূল্য" to "১০০ টাকার ৩টি স্ট্যাম্প",
                    "কাগজের সাইজ" to "Legal Size (8.5 x 14 inch)",
                    "স্বাক্ষর নোট" to "প্রধান কার্যালয়ে জামিনদারের স্বাক্ষর"
                )
            ),
            TutorialStep(
                stepNo = 9,
                title = "৮. ২৫ টাকার প্রত্যয়ন পত্র (প্রতিবেশী প্রত্যয়ন)",
                subtitle = "২ জন প্রতিবেশীর প্রত্যয়ন পত্র",
                icon = Icons.Default.Verified,
                primaryColor = Color(0xFFB45309),
                visualBadge = "২৫ টাকার স্ট্যাম্প",
                bulletPoints = listOf(
                    "উপরের অংশ: পূর্বের পূরণকৃত ফরম থেকে কর্মীর নাম, পিতার নাম, মাতার নাম ও ঠিকানা নিয়ে অটোমেটিক পূরণ।",
                    "নিচের অংশ: প্রতিবেশী ১ ও ২ এর তথ্য নিয়ে প্রত্যয়নকারী হিসেবে অটোমেটিক পূরণ।",
                    "লাল নোট: প্রিন্ট করুন এবং প্রত্যয়নকারীদের স্বাক্ষর নিয়ে আসবেন।"
                ),
                narrationBengali = "অষ্টম ধাপে কর্মীর প্রতিবেশী দুই জনের তথ্য নিয়ে ২৫ টাকার নন জুডিশিয়াল স্ট্যাম্প প্রত্যয়ন পত্র প্রস্তুত হবে। উপরের অংশে কর্মীর বিবরণ এবং নিচের অংশে প্রতিবেশীদ্বয়ের বিবরণ অটোমেটিক পূরণ থাকবে। এটি প্রিন্ট করার পর প্রত্যয়নকারী প্রতিবেশীদের স্বাক্ষর গ্রহণ করতে হবে।",
                sampleFields = listOf(
                    "স্ট্যাম্প মূল্য" to "২৫ টাকার স্ট্যাম্প (২ পাতা)",
                    "প্রত্যয়নকারী" to "প্রতিবেশী ১ ও প্রতিবেশী ২",
                    "স্বাক্ষর নোট" to "প্রত্যয়নকারীদের স্বাক্ষর নিয়ে আসবেন"
                )
            ),
            TutorialStep(
                stepNo = 10,
                title = "৯. পিডিএফ প্রিন্ট, ডাউনলোড ও হোয়াটসঅ্যাপ শেয়ার",
                subtitle = "এক ক্লিকে সকল ফরম প্রস্তুত",
                icon = Icons.Default.Share,
                primaryColor = Color(0xFF0F3B7E),
                visualBadge = "প্রিন্ট ও শেয়ার",
                bulletPoints = listOf(
                    "হোম পেজে 'স্ট্যাম্প প্রিন্ট' অপশনে চাপ দিয়ে ১০০ টাকা ও ২৫ টাকার স্ট্যাম্প প্রিন্ট করুন।",
                    "হোম পেজে 'এগ্রিমেন্ট ফরম প্রিন্ট' অপশনে চাপ দিয়ে মার্জ আকারে সকল ফরম প্রিন্ট করুন।",
                    "হোয়াটসঅ্যাপ ও ডাউনলোডের মাধ্যমে এক ক্লিকে সরাসরি ডকুমেন্ট সংরক্ষণ ও শেয়ার করুন।"
                ),
                narrationBengali = "সব ফরম পূরণ শেষে হোম পেজ থেকে ১০০ টাকার স্ট্যাম্প, ২৫ টাকার প্রত্যয়ন পত্র অথবা সকল এগ্রিমেন্ট ফরম এক সাথে ওপেন, প্রিন্ট, ডাউনলোড ও সরাসরি হোয়াটসঅ্যাপে শেয়ার করতে পারবেন। ধন্যবাদ।",
                sampleFields = listOf(
                    "স্ট্যাম্প প্রিন্ট" to "১০০ টাকা ও ২৫ টাকার অপশন",
                    "এগ্রিমেন্ট প্রিন্ট" to "মার্জকৃত সকল ফরম (৯ পাতা)",
                    "WhatsApp Share" to "সরাসরি প্রেরণ"
                )
            )
        )
    }

    var currentStepIndex by remember { mutableIntStateOf(0) }
    val currentStep = tutorialSteps[currentStepIndex]

    // TextToSpeech State
    var tts: TextToSpeech? by remember { mutableStateOf(null) }
    var isTtsInitialized by remember { mutableStateOf(false) }
    var isPlaying by remember { mutableStateOf(false) }
    var isVoiceMuted by remember { mutableStateOf(false) }
    var speechSpeed by remember { mutableFloatStateOf(0.92f) }

    // Initialize Female Bengali TTS Engine
    DisposableEffect(speechSpeed) {
        var ttsEngine: TextToSpeech? = null
        ttsEngine = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                ttsEngine?.let { engine ->
                    // Try Bengali Locale (BD first, then generic Bengali)
                    val result = engine.setLanguage(Locale("bn", "BD"))
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        engine.setLanguage(Locale("bn"))
                    }

                    // Acoustic tuning for real, sweet, natural female Bengali voice
                    engine.setPitch(1.22f)
                    engine.setSpeechRate(speechSpeed)

                    // Find and select the highest quality natural female Bengali voice
                    try {
                        val voiceSet = engine.voices
                        if (voiceSet != null) {
                            val femaleBengaliVoice = voiceSet.firstOrNull { v ->
                                v.locale.language == "bn" && (
                                    v.name.contains("bdf", ignoreCase = true) ||
                                    v.name.contains("bif", ignoreCase = true) ||
                                    v.name.contains("female", ignoreCase = true) ||
                                    v.name.contains("woman", ignoreCase = true) ||
                                    v.name.contains("f0", ignoreCase = true) ||
                                    v.name.contains("-f-", ignoreCase = true) ||
                                    v.name.contains("_f_", ignoreCase = true) ||
                                    (v.name.contains("bn-bd", ignoreCase = true) && !v.name.contains("male", ignoreCase = true))
                                )
                            } ?: voiceSet.firstOrNull { it.locale.language == "bn" }

                            if (femaleBengaliVoice != null) {
                                engine.voice = femaleBengaliVoice
                            }
                        }
                    } catch (_: Exception) {}

                    isTtsInitialized = true
                }
            }
        }

        ttsEngine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                isPlaying = true
            }

            override fun onDone(utteranceId: String?) {
                scope.launch {
                    isPlaying = false
                    // Auto-advance to next step if playing
                    if (currentStepIndex < tutorialSteps.size - 1 && !isVoiceMuted) {
                        delay(1200)
                        currentStepIndex++
                    }
                }
            }

            override fun onError(utteranceId: String?) {
                isPlaying = false
            }
        })

        tts = ttsEngine

        onDispose {
            ttsEngine.stop()
            ttsEngine.shutdown()
        }
    }

    // Play narration function
    fun speakCurrentStep() {
        if (isVoiceMuted) {
            isPlaying = false
            tts?.stop()
            return
        }
        val textToSpeak = currentStep.narrationBengali
        val params = Bundle()
        params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "TUTORIAL_STEP_${currentStep.stepNo}")
        tts?.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, params, "TUTORIAL_STEP_${currentStep.stepNo}")
        isPlaying = true
    }

    fun stopSpeaking() {
        tts?.stop()
        isPlaying = false
    }

    // Auto-trigger narration when step changes if playing was active
    LaunchedEffect(currentStepIndex) {
        if (isPlaying || (isTtsInitialized && !isVoiceMuted)) {
            speakCurrentStep()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "অ্যাপ ব্যবহারের গাইডলাইন",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "সহজ PDF ও ভিডিও টিউটোরিয়াল",
                            fontSize = 11.5.sp,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.85f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            stopSpeaking()
                            onBack()
                        },
                        modifier = Modifier.testTag("btn_back_guidelines")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "ফিরে যান")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            val pdf = AppGuidelinesPdfGenerator.generateGuidelinesPdf(context)
                            ShareHelper.shareFile(context, pdf, "আরআরএফ অ্যাপ ব্যবহারের পূর্ণাঙ্গ গাইডলাইন (A4 PDF)")
                        },
                        modifier = Modifier.testTag("btn_share_guidelines")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "শেয়ার PDF")
                    }
                    IconButton(
                        onClick = {
                            val pdf = AppGuidelinesPdfGenerator.generateGuidelinesPdf(context)
                            ShareHelper.openFile(context, pdf)
                        },
                        modifier = Modifier.testTag("btn_print_guidelines")
                    ) {
                        Icon(Icons.Default.Print, contentDescription = "প্রিন্ট PDF")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val pdf = AppGuidelinesPdfGenerator.generateGuidelinesPdf(context)
                            ShareHelper.shareFile(context, pdf, "আরআরএফ অ্যাপ ব্যবহারের গাইডলাইন (A4 PDF)")
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("গাইডলাইন শেয়ার", fontWeight = FontWeight.Bold, fontSize = 12.sp, maxLines = 1)
                    }

                    Button(
                        onClick = {
                            val pdf = AppGuidelinesPdfGenerator.generateGuidelinesPdf(context)
                            ShareHelper.openFile(context, pdf)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF047857)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1.2f)
                            .height(46.dp)
                    ) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("A4 PDF ওপেন ও প্রিন্ট", fontWeight = FontWeight.Bold, fontSize = 12.sp, maxLines = 1)
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Tab Row
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.PlayCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("ভিডিও ও ভয়েস টিউটোরিয়াল", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = {
                        stopSpeaking()
                        selectedTab = 1
                    },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("সহজ PDF ইউজার গাইড", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                )
            }

            if (selectedTab == 0) {
                // Video & Voice Interactive Player Tab
                VoiceTutorialPlayerView(
                    steps = tutorialSteps,
                    currentStepIndex = currentStepIndex,
                    isPlaying = isPlaying,
                    isVoiceMuted = isVoiceMuted,
                    speechSpeed = speechSpeed,
                    onSpeedChange = { speed ->
                        speechSpeed = speed
                        tts?.setSpeechRate(speed)
                    },
                    onStepSelected = { idx ->
                        currentStepIndex = idx
                    },
                    onTogglePlay = {
                        if (isPlaying) {
                            stopSpeaking()
                        } else {
                            speakCurrentStep()
                        }
                    },
                    onToggleMute = {
                        isVoiceMuted = !isVoiceMuted
                        if (isVoiceMuted) {
                            stopSpeaking()
                        } else {
                            speakCurrentStep()
                        }
                    },
                    onNextStep = {
                        if (currentStepIndex < tutorialSteps.size - 1) {
                            currentStepIndex++
                        }
                    },
                    onPrevStep = {
                        if (currentStepIndex > 0) {
                            currentStepIndex--
                        }
                    },
                    onReplayStep = {
                        speakCurrentStep()
                    }
                )
            } else {
                // PDF & User Manual Overview Tab
                PdfUserManualView(
                    steps = tutorialSteps,
                    onOpenPdf = {
                        val pdf = AppGuidelinesPdfGenerator.generateGuidelinesPdf(context)
                        ShareHelper.openFile(context, pdf)
                    }
                )
            }
        }
    }
}

@Composable
private fun VoiceTutorialPlayerView(
    steps: List<TutorialStep>,
    currentStepIndex: Int,
    isPlaying: Boolean,
    isVoiceMuted: Boolean,
    speechSpeed: Float,
    onSpeedChange: (Float) -> Unit,
    onStepSelected: (Int) -> Unit,
    onTogglePlay: () -> Unit,
    onToggleMute: () -> Unit,
    onNextStep: () -> Unit,
    onPrevStep: () -> Unit,
    onReplayStep: () -> Unit
) {
    val scrollState = rememberScrollState()
    val isDark = isSystemInDarkTheme()
    val currentStep = steps[currentStepIndex]

    // Animated pulse for AI voice wave
    val infiniteTransition = rememberInfiniteTransition(label = "tts_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "voice_pulse"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Interactive Animated Video Canvas Player
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = if (isDark) Color(0xFF0F172A) else Color(0xFF1E293B),
            shadowElevation = 8.dp,
            border = BorderStroke(1.5.dp, Color(0xFF059669)),
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color(0xFF064E3B).copy(alpha = 0.85f),
                                Color(0xFF0F172A)
                            )
                        )
                    )
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Top Player Bar: Step badge, AI Voice indicator
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF059669),
                            modifier = Modifier.shadow(2.dp)
                        ) {
                            Text(
                                text = "ধাপ ${currentStep.stepNo} / ${steps.size} • ${currentStep.visualBadge}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        // AI Sweet Female Voice Indicator
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color.White.copy(alpha = 0.15f))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .scale(if (isPlaying) pulseScale else 1f)
                                    .clip(CircleShape)
                                    .background(if (isPlaying) Color(0xFF10B981) else Color(0xFF9CA3AF))
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = if (isPlaying) "AI ভয়েস বর্ণনা করছে..." else "অডিও টিউটোরিয়াল",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
                    }

                    // Middle Simulated Video Canvas Frame
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White.copy(alpha = 0.08f),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(vertical = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(10.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = currentStep.icon,
                                contentDescription = null,
                                tint = Color(0xFF34D399),
                                modifier = Modifier
                                    .size(36.dp)
                                    .scale(if (isPlaying) pulseScale else 1f)
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = currentStep.title,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                textAlign = TextAlign.Center
                            )
                            Spacer(Modifier.height(4.dp))

                            // Sample live data simulation box
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                currentStep.sampleFields.take(2).forEach { (k, v) ->
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color(0xFF047857).copy(alpha = 0.5f),
                                        modifier = Modifier.padding(horizontal = 4.dp)
                                    ) {
                                        Text(
                                            text = "$k: $v",
                                            fontSize = 9.5.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Color(0xFFECFDF5),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Live Subtitle Display Bar
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.Black.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "❝ ${currentStep.narrationBengali} ❞",
                            fontSize = 11.5.sp,
                            color = Color(0xFFE2E8F0),
                            lineHeight = 15.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        // Interactive Video/Voice Controls Card
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 3.dp,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Control Buttons Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Previous Step
                    IconButton(
                        onClick = onPrevStep,
                        enabled = currentStepIndex > 0
                    ) {
                        Icon(Icons.Default.SkipPrevious, contentDescription = "পূর্ববর্তী ধাপ", modifier = Modifier.size(28.dp))
                    }

                    // Replay Current Voice
                    IconButton(onClick = onReplayStep) {
                        Icon(Icons.Default.Replay, contentDescription = "পুনরায় শুনুন", tint = MaterialTheme.colorScheme.primary)
                    }

                    // Main Play/Pause Button
                    Button(
                        onClick = onTogglePlay,
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isPlaying) Color(0xFFDC2626) else Color(0xFF059669)
                        ),
                        modifier = Modifier.size(54.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "থামুন" else "প্লে করুন",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    // Voice Mute Toggle
                    IconButton(onClick = onToggleMute) {
                        Icon(
                            imageVector = if (isVoiceMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                            contentDescription = "ভয়েস চালু/বন্ধ",
                            tint = if (isVoiceMuted) Color(0xFFDC2626) else Color(0xFF059669)
                        )
                    }

                    // Next Step
                    IconButton(
                        onClick = onNextStep,
                        enabled = currentStepIndex < steps.size - 1
                    ) {
                        Icon(Icons.Default.SkipNext, contentDescription = "পরবর্তী ধাপ", modifier = Modifier.size(28.dp))
                    }
                }

                // Voice Speed Selector Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "কণ্ঠের গতি:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.width(8.dp))
                    val speeds = listOf(0.85f to "ধীর", 0.92f to "স্বাভাবিক", 1.05f to "দ্রুত")
                    speeds.forEach { (spd, label) ->
                        val isSelected = kotlin.math.abs(speechSpeed - spd) < 0.04f
                        Surface(
                            onClick = { onSpeedChange(spd) },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) Color(0xFF059669) else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        ) {
                            Text(
                                text = label,
                                fontSize = 10.5.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }

                // Step Progress Indicator Slider Beads
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    steps.forEachIndexed { idx, _ ->
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 3.dp)
                                .size(if (idx == currentStepIndex) 10.dp else 6.dp)
                                .clip(CircleShape)
                                .background(
                                    if (idx == currentStepIndex) Color(0xFF059669)
                                    else if (idx < currentStepIndex) Color(0xFF34D399)
                                    else MaterialTheme.colorScheme.outlineVariant
                                )
                                .clickable { onStepSelected(idx) }
                        )
                    }
                }
            }
        }

        // Detailed Current Step Info Card
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF059669),
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "${currentStep.stepNo}",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = currentStep.title,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = currentStep.subtitle,
                            fontSize = 11.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(Modifier.height(12.dp))

                Text(
                    text = "গুরুত্বপূর্ণ নির্দেশনাবলী :",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF047857)
                )

                Spacer(Modifier.height(6.dp))

                currentStep.bulletPoints.forEach { pt ->
                    Row(
                        modifier = Modifier.padding(vertical = 3.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text("• ", fontWeight = FontWeight.Bold, color = Color(0xFF059669))
                        Text(
                            text = pt,
                            fontSize = 12.5.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 17.sp
                        )
                    }
                }
            }
        }

        // Step Quick Selector Grid
        Text(
            text = "সকল ধাপসমূহ (যেকোনো ধাপে ক্লিক করুন) :",
            fontSize = 13.5.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        steps.forEachIndexed { index, step ->
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (index == currentStepIndex) (if (isDark) Color(0xFF064E3B).copy(alpha = 0.5f) else Color(0xFFECFDF5))
                else MaterialTheme.colorScheme.surface,
                border = BorderStroke(
                    1.2.dp,
                    if (index == currentStepIndex) Color(0xFF059669) else MaterialTheme.colorScheme.outlineVariant
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onStepSelected(index) }
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = if (index == currentStepIndex) Color(0xFF059669) else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.size(26.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "${step.stepNo}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (index == currentStepIndex) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = step.title,
                            fontSize = 13.sp,
                            fontWeight = if (index == currentStepIndex) FontWeight.Bold else FontWeight.Medium,
                            color = if (index == currentStepIndex) Color(0xFF047857) else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = step.subtitle,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (index == currentStepIndex) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = null,
                            tint = Color(0xFF059669),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(20.dp))
    }
}

@Composable
private fun PdfUserManualView(
    steps: List<TutorialStep>,
    onOpenPdf: () -> Unit
) {
    val scrollState = rememberScrollState()
    val isDark = isSystemInDarkTheme()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // PDF Action Banner
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = if (isDark) Color(0xFF064E3B).copy(alpha = 0.4f) else Color(0xFFECFDF5),
            border = BorderStroke(1.2.dp, Color(0xFF10B981)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.PictureAsPdf,
                    contentDescription = null,
                    tint = Color(0xFF059669),
                    modifier = Modifier.size(32.dp)
                )
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "অফিসিয়াল PDF ইউজার গাইড ডাউনলোড",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF065F46)
                    )
                    Text(
                        text = "২ পাতার A4 সাইজের প্রিন্ট উপযোগী পুর্ণাঙ্গ নির্দেশিকা",
                        fontSize = 11.5.sp,
                        color = Color(0xFF047857)
                    )
                }
                Button(
                    onClick = onOpenPdf,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("ওপেন PDF", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }

        // Summary Manual
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "অ্যাপ ব্যবহারের মূল নিয়মাবলী ও শর্তসমূহ :",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(Modifier.height(10.dp))
                HorizontalDivider()
                Spacer(Modifier.height(10.dp))

                val rules = listOf(
                    "১. ক্রমানুসারে ফরম পূরণ: প্রথমে আইডি কার্ড ➔ প্রশিক্ষণ ➔ সম্পর্ক ➔ নমিনি ➔ তথ্যানুসন্ধান ➔ তথ্য যাচাই ➔ ১০০ টাকার স্ট্যাম্প পূরণ করবেন।",
                    "২. অটো-ফিল সুবিধা: একবার যে নাম বা ঠিকানা দিবেন, তা অন্য ফরমে অটো চলে আসবে এবং সবুজ রঙে সুরক্ষিত থাকবে।",
                    "৩. ভাষা ও ফিল্টারিং নিয়ম: বাংলা ছকে বাংলা, ইংরেজি ছকে ইংরেজি এবং সংখ্যার ছকে শুধুমাত্র সংখ্যা টাইপ হবে।",
                    "৪. কর্মীর NID: তথ্যানুসন্ধান ফরম ওপেন করলে কর্মীর NID ফাঁকা থাকবে এবং টাইপ করার পর তা স্থায়ীভাবে সেভ হবে।",
                    "৫. প্রিন্টিং সুবিধা: হোম পেজের প্রিন্ট অপশনে ক্লিক করে যেকোনো সময় সরাসরি প্রিন্ট বা হোয়াটসঅ্যাপে শেয়ার করা যাবে।"
                )

                rules.forEach { rule ->
                    Text(
                        text = rule,
                        fontSize = 12.5.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 18.sp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }

        Spacer(Modifier.height(20.dp))
    }
}
