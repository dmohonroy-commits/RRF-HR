package com.example.util

/**
 * Helper object for automated bilingual address processing between Bengali and English.
 * Automatically formats and synchronizes addresses in:
 * Bangla: "গ্রাম- বুড়িয়া, পোস্ট-বড়দল, থানা-আশাশুনি, জেলা-সাতক্ষীরা"
 * English: "VILLEGE- BURIA, POST-BARADAL, THANA-ASHASHUNI, DISTRICT-SATKHIRA"
 */
object BanglaAddressHelper {

    // Standard English mappings for all 64 districts of Bangladesh
    val districtEnglishMap = mapOf(
        "বাগেরহাট" to "BAGERHAT",
        "বান্দরবান" to "BANDARBAN",
        "বরগুনা" to "BARGUNA",
        "বরিশাল" to "BARISHAL",
        "ভোলা" to "BHOLA",
        "বগুড়া" to "BOGURA",
        "বগুড়া" to "BOGURA",
        "ব্রাহ্মণবাড়িয়া" to "BRAHMANBARIA",
        "ব্রাহ্মণবাড়িয়া" to "BRAHMANBARIA",
        "চাঁদপুর" to "CHANDPUR",
        "চাঁপাইনবাবগঞ্জ" to "CHAPAINAWABGANJ",
        "চট্টগ্রাম" to "CHATTOGRAM",
        "চুয়াডাঙ্গা" to "CHUADANGA",
        "চুয়াডাঙ্গা" to "CHUADANGA",
        "কুমিল্লা" to "CUMILLA",
        "কক্সবাজার" to "COXS BAZAR",
        "ঢাকা" to "DHAKA",
        "দিনাজপুর" to "DINAJPUR",
        "ফরিদপুর" to "FARIDPUR",
        "ফেনী" to "FENI",
        "গাইবান্ধা" to "GAIBANDHA",
        "গাজীপুর" to "GAZIPUR",
        "গোপালগঞ্জ" to "GOPALGANJ",
        "হবিগঞ্জ" to "HABIGANJ",
        "জামালপুর" to "JAMALPUR",
        "যশোর" to "JASHORE",
        "ঝালকাঠি" to "JHALOKATHI",
        "ঝিনাইদহ" to "JHENAIDAH",
        "জয়পুরহাট" to "JOYPURHAT",
        "জয়পুরহাট" to "JOYPURHAT",
        "খাগড়াছড়ি" to "KHAGRACHARI",
        "খাগড়াছড়ি" to "KHAGRACHARI",
        "খুলনা" to "KHULNA",
        "কিশোরগঞ্জ" to "KISHOREGANJ",
        "কুড়িগ্রাম" to "KURIGRAM",
        "কুড়িগ্রাম" to "KURIGRAM",
        "কুষ্টিয়া" to "KUSHTIA",
        "কুষ্টিয়া" to "KUSHTIA",
        "লক্ষ্মীপুর" to "LAKSHMIPUR",
        "লালমনিরহাট" to "LALMONIRHAT",
        "মাদারীপুর" to "MADARIPUR",
        "মাগুরা" to "MAGURA",
        "মানিকগঞ্জ" to "MANIKGANJ",
        "মেহেরপুর" to "MEHERPUR",
        "মৌলভীবাজার" to "MOULVIBAZAR",
        "মুন্সীগঞ্জ" to "MUNSHIGANJ",
        "ময়মনসিংহ" to "MYMENSINGH",
        "ময়মনসিংহ" to "MYMENSINGH",
        "নওগাঁ" to "NAOGAON",
        "নড়াইল" to "NARAIL",
        "নড়াইল" to "NARAIL",
        "নারায়ণগঞ্জ" to "NARAYANGANJ",
        "নারায়ণগঞ্জ" to "NARAYANGANJ",
        "নরসিংদী" to "NARSINGDI",
        "নাটোর" to "NATORE",
        "নেত্রকোণা" to "NETROKONA",
        "নেত্রকোনা" to "NETROKONA",
        "নীলফামারী" to "NILPHAMARI",
        "নোয়াখালী" to "NOAKHALI",
        "নোয়াখালী" to "NOAKHALI",
        "পাবনা" to "PABNA",
        "পঞ্চগড়" to "PANCHAGARH",
        "পঞ্চগড়" to "PANCHAGARH",
        "পটুয়াখালী" to "PATUAKHALI",
        "পটুয়াখালী" to "PATUAKHALI",
        "পিরোজপুর" to "PIROJPUR",
        "রাজবাড়ী" to "RAJBARI",
        "রাজবাড়ী" to "RAJBARI",
        "রাজশাহী" to "RAJSHAHI",
        "রাঙ্গামাটি" to "RANGAMATI",
        "রংপুর" to "RANGPUR",
        "সাতক্ষীরা" to "SATKHIRA",
        "শরীয়তপুর" to "SHARIATPUR",
        "শরিয়তপুর" to "SHARIATPUR",
        "শেরপুর" to "SHERPUR",
        "সিরাজগঞ্জ" to "SIRAJGANJ",
        "সুনামগঞ্জ" to "SUNAMGANJ",
        "সিলেট" to "SYLHET",
        "টাঙ্গাইল" to "TANGAIL",
        "ঠাকুরগাঁও" to "THAKURGAON"
    )

    fun getEnglishDistrict(districtBangla: String): String {
        val clean = cleanPrefix(districtBangla).trim()
        if (clean.isBlank()) return ""
        districtEnglishMap[clean]?.let { return it }
        val entry = districtEnglishMap.entries.firstOrNull { clean.contains(it.key) || it.key.contains(clean) }
        if (entry != null) return entry.value
        return if (BanglaTextValidator.containsBengali(clean)) transliterateBanglaToEnglish(clean) else BanglaTextValidator.capitalizeWords(clean)
    }

    // Common Upazilas / Thanas English dictionary
    private val commonUpazilasMap = mapOf(
        "আশাশুনি" to "ASHASHUNI",
        "দেবহাটা" to "DEBHATA",
        "কালীগঞ্জ" to "KALIGANJ",
        "কালিগঞ্জ" to "KALIGANJ",
        "কলারোয়া" to "KALAROA",
        "কলারোয়া" to "KALAROA",
        "শ্যামনগর" to "SHYAMNAGAR",
        "তালা" to "TALA",
        "সাতক্ষীরা সদর" to "SATKHIRA SADAR",
        "অভয়নগর" to "ABHAYNAGAR",
        "অভয়নগর" to "ABHAYNAGAR",
        "বাঘারপাড়া" to "BAGHARPARA",
        "বাঘারপাড়া" to "BAGHARPARA",
        "চৌগাছা" to "CHOUGACHHA",
        "ঝিকরগাছা" to "JHIKARGACHHA",
        "কেশবপুর" to "KESHABPUR",
        "মণিরামপুর" to "MANIRAMPUR"
    )

    /**
     * Converts any English string (e.g. name or address) to standard Bangla phonetic transliteration.
     */
    fun transliterateEnglishToBangla(input: String): String {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) return ""
        if (BanglaTextValidator.containsBengali(trimmed)) return trimmed

        val uppercase = trimmed.uppercase()
        val words = uppercase.split(Regex("\\s+"))
        val convertedWords = words.map { word ->
            when (word) {
                "BEGUM", "BEGUM." -> "বেগম"
                "KHATUN", "KHATUN." -> "খাতুন"
                "BIBI" -> "বিবি"
                "RANI" -> "রানী"
                "AKTER", "AKHTER" -> "আক্তার"
                "SULTANA" -> "সুলতানা"
                "NAHAR" -> "নাহার"
                "NESA", "NESSA" -> "নেছা"
                "PARVIN", "PARVEEN" -> "পারভীন"
                "JAHAN" -> "জাহান"
                "BANU" -> "বানু"
                "ROKEYA", "ROKEYA." -> "রোকেয়া"
                "RAHIMA" -> "রহিমা"
                "FATEMA" -> "ফাতেমা"
                "MORIOM", "MARYAM", "MARIAM" -> "মরিয়ম"
                "SALMA" -> "সালমা"
                "NASIMA" -> "নাসিমা"
                "SAHIDA", "SHAHIDA" -> "শাহিদা"
                "MST", "MST.", "MOST" -> "মোসাঃ"
                "MD", "MD.", "MOHAMMAD" -> "মোঃ"
                "SK", "SK.", "SHEIKH" -> "শেখ"
                "SYED", "SYEDA" -> "সৈয়দা"
                "SM", "SM." -> "এস এম"
                "LATE" -> "মৃত"
                "MRS", "MRS." -> "মিসেস"
                else -> {
                    // If word is already in Bengali or clean, return as is
                    if (BanglaTextValidator.containsBengali(word)) {
                        word
                    } else {
                        // Do not substitute independent vowels for letters in names
                        // Returning the clean word avoids generating scrambled gibberish like সআবইতআ
                        word
                    }
                }
            }
        }
        val result = convertedWords.joinToString(" ").trim()
        return if (BanglaTextValidator.isScrambledBangla(result)) "" else result
    }

    /**
     * Converts any Bangla string to standard Title Case English phonetic transliteration.
     */
    fun transliterateBanglaToEnglish(input: String): String {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) return ""

        val rawResult = districtEnglishMap[trimmed]
            ?: commonUpazilasMap[trimmed]
            ?: run {
                val cleanWord = trimmed
                    .replace(Regex("^(গ্রাম|পোস্ট|ডাকঘর|থানা|উপজেলা|জেলা|গ্রাম-|পোস্ট-|ডাকঘর-|থানা-|উপজেলা-|জেলা-|গ্রাম:|পোস্ট:|ডাকঘর:|থানা:|উপজেলা:|জেলা:)\\s*"), "")
                    .trim()
                districtEnglishMap[cleanWord]
                    ?: commonUpazilasMap[cleanWord]
                    ?: transliteratePhonetic(cleanWord)
            }

        return BanglaTextValidator.capitalizeWords(rawResult)
    }

    private fun transliteratePhonetic(bangla: String): String {
        val sb = StringBuilder()
        var i = 0
        val len = bangla.length

        while (i < len) {
            val c = bangla[i]

            // Check if already English/digit/symbol
            if (c in 'a'..'z' || c in 'A'..'Z' || c in '0'..'9' || c.isWhitespace() || c in listOf('-', ',', '.', ':', '/', '(', ')')) {
                sb.append(c.uppercaseChar())
                i++
                continue
            }

            // Check multi-character conjuncts & special characters
            if (i + 1 < len) {
                val pair = bangla.substring(i, i + 2)
                if (pair == "ক্ষ") {
                    val nextIsVowelSign = i + 2 < len && isVowelSign(bangla[i + 2])
                    val nextIsHasant = i + 2 < len && bangla[i + 2] == '্'
                    sb.append("KH")
                    if (!nextIsVowelSign && !nextIsHasant && i + 2 < len && isConsonant(bangla[i + 2])) {
                        sb.append("A")
                    }
                    i += 2
                    continue
                }
                if (pair == "জ্ঞ") {
                    sb.append("GY")
                    i += 2
                    continue
                }
                if (pair == "শ্র") {
                    sb.append("SHR")
                    i += 2
                    continue
                }
                if (pair == "ব্র") {
                    sb.append("BR")
                    i += 2
                    continue
                }
                if (pair == "প্র") {
                    sb.append("PR")
                    i += 2
                    continue
                }
                if (pair == "ত্র") {
                    sb.append("TR")
                    i += 2
                    continue
                }
                if (pair == "গ্র") {
                    sb.append("GR")
                    i += 2
                    continue
                }
                if (pair == "ক্র") {
                    sb.append("KR")
                    i += 2
                    continue
                }
                if (pair == "দ্র") {
                    sb.append("DR")
                    i += 2
                    continue
                }
            }

            // Independent Vowels (স্বরবর্ণ)
            val indVowel = when (c) {
                'অ' -> "A"
                'আ' -> "A"
                'ই' -> "I"
                'ঈ' -> "I"
                'উ' -> "U"
                'ঊ' -> "U"
                'ঋ' -> "RI"
                'এ' -> "E"
                'ঐ' -> "OI"
                'ও' -> "O"
                'ঔ' -> "OU"
                else -> null
            }
            if (indVowel != null) {
                sb.append(indVowel)
                i++
                continue
            }

            // Dependent Vowel Signs (কার)
            val depVowel = when (c) {
                'া' -> "A"
                'ি' -> "I"
                'ী' -> "I"
                'ু' -> "U"
                'ূ' -> "U"
                'ৃ' -> "RI"
                'ে' -> "E"
                'ৈ' -> "OI"
                'ো' -> "O"
                'ৌ' -> "OU"
                else -> null
            }
            if (depVowel != null) {
                sb.append(depVowel)
                i++
                continue
            }

            // Consonants (ব্যঞ্জনবর্ণ)
            val cons = when (c) {
                'ক' -> "K"
                'খ' -> "KH"
                'গ' -> "G"
                'ঘ' -> "GH"
                'ঙ' -> "NG"
                'চ' -> "CH"
                'ছ' -> "CHH"
                'জ' -> "J"
                'ঝ' -> "JH"
                'ঞ' -> "N"
                'ট' -> "T"
                'ঠ' -> "TH"
                'ড' -> "D"
                'ঢ' -> "DH"
                'ণ' -> "N"
                'ত' -> "T"
                'থ' -> "TH"
                'দ' -> "D"
                'ধ' -> "DH"
                'ন' -> "N"
                'প' -> "P"
                'ফ' -> "F"
                'ব' -> "B"
                'ভ' -> "BH"
                'ম' -> "M"
                'য' -> "J"
                'র' -> "R"
                'ল' -> "L"
                'শ' -> "SH"
                'ষ' -> "SH"
                'স' -> "S"
                'হ' -> "H"
                '\u09DC' -> "R" // ড়
                '\u09DD' -> "RH" // ঢ়
                '\u09DF' -> "A" // য়
                '\u09BC' -> "" // Nukta
                '\u09CE' -> "T" // ৎ
                '\u0982' -> "NG" // ং
                '\u0983' -> "H" // ঃ
                '\u0981' -> "N" // ঁ
                '\u09CD' -> "" // Hasant
                else -> ""
            }

            if (cons.isNotEmpty()) {
                sb.append(cons)
                // Determine whether to add inherent vowel 'A'
                val hasNext = i + 1 < len
                if (hasNext) {
                    val nextChar = bangla[i + 1]
                    val nextIsVowelSign = isVowelSign(nextChar)
                    val nextIsHasant = nextChar == '\u09CD'
                    val nextIsConsonant = isConsonant(nextChar)

                    // If followed by consonant, add inherent 'A' unless special pattern
                    if (!nextIsVowelSign && !nextIsHasant) {
                        if (nextIsConsonant) {
                            // Add inherent 'A' (e.g. বড়দল -> B-A-R-A-D-A-L)
                            sb.append("A")
                        }
                    }
                }
                i++
                continue
            }

            i++
        }

        // Clean up common duplicate vowel glitches
        return sb.toString()
            .replace("AA", "A")
            .replace("II", "I")
            .replace("UU", "U")
            .replace("SHSH", "SH")
            .replace("KKH", "KH")
    }

    private fun isVowelSign(c: Char): Boolean {
        return c in listOf('া', 'ি', 'ী', 'ু', 'ূ', 'ৃ', 'ে', 'ৈ', 'ো', 'ৌ')
    }

    private fun isConsonant(c: Char): Boolean {
        return c in 'ক'..'হ' || c in listOf('\u09DC', '\u09DD', '\u09DF', '\u09CE', '\u0982', '\u0983')
    }

    /**
     * Formats full Bengali address line from individual parts:
     * Example: "গ্রাম- বুড়িয়া, পোস্ট-বড়দল, থানা-আশাশুনি, জেলা-সাতক্ষীরা"
     */
    fun formatBanglaAddress(
        village: String,
        postOffice: String,
        thanaOrUpazila: String,
        district: String
    ): String {
        val cleanVill = cleanPrefix(village)
        val cleanPost = cleanPrefix(postOffice)
        val cleanThana = cleanPrefix(thanaOrUpazila)
        val cleanDist = cleanPrefix(district)

        val parts = mutableListOf<String>()
        if (cleanVill.isNotBlank()) parts.add("গ্রাম- $cleanVill")
        if (cleanPost.isNotBlank()) parts.add("পোস্ট-$cleanPost")
        if (cleanThana.isNotBlank()) parts.add("থানা-$cleanThana")
        if (cleanDist.isNotBlank()) parts.add("জেলা-$cleanDist")

        return parts.joinToString(", ")
    }

    /**
     * Formats full English address line from individual parts in Title Case:
     * Example: "Village- Buria, Post- Baradal, Thana- Ashashuni, District- Satkhira"
     */
    fun formatEnglishAddress(
        village: String,
        postOffice: String,
        thanaOrUpazila: String,
        district: String
    ): String {
        val cleanVill = cleanPrefix(village)
        val cleanPost = cleanPrefix(postOffice)
        val cleanThana = cleanPrefix(thanaOrUpazila)
        val cleanDist = cleanPrefix(district)

        val parts = mutableListOf<String>()
        if (cleanVill.isNotBlank()) {
            val enVill = if (BanglaTextValidator.containsBengali(cleanVill)) transliterateBanglaToEnglish(cleanVill) else BanglaTextValidator.capitalizeWords(cleanVill)
            parts.add("Village- $enVill")
        }
        if (cleanPost.isNotBlank()) {
            val enPost = if (BanglaTextValidator.containsBengali(cleanPost)) transliterateBanglaToEnglish(cleanPost) else BanglaTextValidator.capitalizeWords(cleanPost)
            parts.add("Post- $enPost")
        }
        if (cleanThana.isNotBlank()) {
            val enThana = if (BanglaTextValidator.containsBengali(cleanThana)) transliterateBanglaToEnglish(cleanThana) else BanglaTextValidator.capitalizeWords(cleanThana)
            parts.add("Thana- $enThana")
        }
        if (cleanDist.isNotBlank()) {
            val enDist = if (BanglaTextValidator.containsBengali(cleanDist)) transliterateBanglaToEnglish(cleanDist) else BanglaTextValidator.capitalizeWords(cleanDist)
            parts.add("District- $enDist")
        }

        return parts.joinToString(", ")
    }

    private fun cleanPrefix(text: String): String {
        return text.replace(Regex("^(গ্রাম[:\\-\\s]+|ডাকঘর[:\\-\\s]+|পোস্ট[:\\-\\s]+|থানা[:\\-\\s]+|উপজেলা[:\\-\\s]+|জেলা[:\\-\\s]+|VILLEGE[:\\-\\s]+|VILLAGE[:\\-\\s]+|POST[:\\-\\s]+|THANA[:\\-\\s]+|DISTRICT[:\\-\\s]+)", RegexOption.IGNORE_CASE), "").trim()
    }
}
