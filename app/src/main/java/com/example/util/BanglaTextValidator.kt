package com.example.util

object BanglaTextValidator {

    /**
     * Replaces Bangla visarga 'ঃ' with standard colon ':' across any string.
     */
    fun normalizeColons(input: String): String {
        return input.replace('ঃ', ':')
    }

    /**
     * Checks if a character is allowable in Bengali text format.
     * Rejects English alphabet letters (a-z, A-Z).
     */
    fun isBengaliCharacterOrPunctuation(c: Char): Boolean {
        if (c.isWhitespace()) return true
        if (c in listOf('-', '(', ')', '/', '.', ',', ':', '+', '@', '_', '%', '\'', '"')) return true
        // Exclude English letters strictly
        if (c in 'a'..'z' || c in 'A'..'Z') return false
        // Allow Bengali Unicode block \u0980..\u09FF (letters, vowel signs, digits)
        return c in '\u0980'..'\u09FF'
    }

    /**
     * Filters input string to strictly contain Bengali characters, digits, and allowed punctuation.
     * Prevents English letters from being typed.
     */
    fun filterBanglaText(input: String): String {
        return normalizeColons(input.filter { isBengaliCharacterOrPunctuation(it) })
    }

    fun filterBengaliOnly(input: String): String {
        return normalizeColons(input.filter { isBengaliCharacterOrPunctuation(it) })
    }

    fun filterBengaliWithPunctuation(input: String): String {
        return normalizeColons(input.filter { isBengaliCharacterOrPunctuation(it) })
    }

    /**
     * Strictly Bengali characters, spaces, and punctuation only.
     * Rejects any English letters.
     */
    fun filterBengaliTextOnly(input: String): String {
        return normalizeColons(input.filter { isBengaliCharacterOrPunctuation(it) })
    }

    /**
     * Checks if a character is allowable in English text format.
     * Rejects Bengali script characters strictly (\u0980..\u09FF).
     */
    fun isEnglishCharacterOrPunctuation(c: Char): Boolean {
        if (c.isWhitespace()) return true
        if (c in listOf('-', '(', ')', '/', '.', ',', ':', '+', '@', '_', '%', '\'', '"', '&', '#', ';', '!', '?', '<', '>')) return true
        // Exclude Bengali script
        if (c in '\u0980'..'\u09FF') return false
        // Allow standard ASCII printable characters
        return c in ' '..'~'
    }

    /**
     * Capitalizes the first letter of each word and sets subsequent letters in words to lowercase.
     */
    fun capitalizeWords(input: String): String {
        if (input.isBlank()) return input
        val sb = StringBuilder()
        var capitalizeNext = true
        for (c in input) {
            if (c.isWhitespace() || c == '-' || c == '(' || c == ')' || c == '/' || c == '.' || c == ',' || c == ':' || c == '&') {
                sb.append(c)
                capitalizeNext = true
            } else if (capitalizeNext && c.isLetter()) {
                sb.append(c.uppercaseChar())
                capitalizeNext = false
            } else if (c.isLetter()) {
                sb.append(c.lowercaseChar())
            } else {
                sb.append(c)
            }
        }
        return sb.toString()
    }

    /**
     * Filters input string to strictly contain English letters, numbers, and allowed punctuation.
     * Prevents Bengali characters from being typed.
     */
    fun filterEnglishText(input: String): String {
        return filterEnglishTitleCaseText(input)
    }

    /**
     * Filters input to strictly lowercase characters for email addresses.
     * Disallows uppercase letters and forces lowercase.
     */
    fun filterEmailAddress(input: String): String {
        return input.lowercase().filter { c ->
            (c in 'a'..'z') || (c in '0'..'9') || c == '@' || c == '.' || c == '_' || c == '-' || c == '+'
        }.trim()
    }

    /**
     * Filters input to strictly English characters and transforms to Title Case (Word Capitalization).
     */
    fun filterEnglishTitleCaseText(input: String): String {
        val filtered = input.filter { isEnglishCharacterOrPunctuation(it) }
        return normalizeColons(capitalizeWords(filtered))
    }

    /**
     * Legacy helper redirecting to Title Case (Word Capitalization).
     */
    fun filterEnglishUppercaseText(input: String): String {
        return filterEnglishTitleCaseText(input)
    }

    /**
     * Filters input to contain ONLY digits (0-9 or ০-৯).
     * Strictly rejects all alphabet letters (both English and Bengali letters).
     */
    fun filterDigitsStrict(input: String): String {
        return input.filter { it in '0'..'9' || it in '\u09E6'..'\u09EF' }
    }

    /**
     * Filters input to contain only digits (Bengali or English numbers).
     */
    fun filterDigitsOnly(input: String): String {
        return filterDigitsStrict(input)
    }

    /**
     * Filters input to contain only digits and strictly converts any English digits to Bengali numerals (০-৯).
     * Rejects all non-digit characters and English letters.
     */
    fun filterBanglaDigitsOnly(input: String): String {
        return toBanglaDigits(filterDigitsStrict(input))
    }

    fun filterDateInput(input: String): String {
        return input.filter { it in '0'..'9' || it in '\u09E6'..'\u09EF' || it == '/' || it == '-' || it == '.' }
    }

    /**
     * Strictly formats input into DD/MM/YYYY with slashes and maximum 10 characters (including 4-digit year).
     */
    fun formatDateInputStrict(input: String): String {
        val rawDigits = toEnglishDigits(input).filter { it.isDigit() }
        val sb = StringBuilder()
        for (i in rawDigits.indices) {
            if (i == 2 || i == 4) {
                sb.append('/')
            }
            if (sb.length < 10) {
                sb.append(rawDigits[i])
            }
        }
        return toBanglaDigits(sb.toString())
    }

    /**
     * Validates whether a date string has day (1-31), month (1-12), and a valid 4-digit year (1900-2100).
     * "সন সাল ছাড়া গ্রহণ করবে না"
     */
    fun isValidDateWith4DigitYear(dateStr: String): Boolean {
        if (dateStr.isBlank()) return false
        val english = toEnglishDigits(dateStr).trim()
        val parts = english.split('/', '-', '.')
        if (parts.size != 3) {
            val digits = english.filter { it.isDigit() }
            if (digits.length != 8) return false
            val d = digits.substring(0, 2).toIntOrNull() ?: return false
            val m = digits.substring(2, 4).toIntOrNull() ?: return false
            val y = digits.substring(4, 8).toIntOrNull() ?: return false
            return d in 1..31 && m in 1..12 && y in 1900..2100
        }
        val d = parts[0].toIntOrNull() ?: return false
        val m = parts[1].toIntOrNull() ?: return false
        val yStr = parts[2].trim()
        if (yStr.length != 4) return false
        val y = yStr.toIntOrNull() ?: return false
        return d in 1..31 && m in 1..12 && y in 1900..2100
    }

    fun filterDateStrict(input: String): String {
        return filterDateInput(input)
    }

    fun filterMobileInput(input: String): String {
        val digits = filterDigitsStrict(input)
        return if (digits.length <= 11) digits else digits.substring(0, 11)
    }

    /**
     * Filters age input to strictly allow maximum 2 digits.
     */
    fun filterAgeInput(input: String): String {
        val digits = filterDigitsStrict(input)
        return if (digits.length <= 2) digits else digits.substring(0, 2)
    }

    /**
     * Filters input for GPA / Result to strictly follow format: 1 digit before dot, dot, and up to 2 digits after dot (e.g. 5.00).
     */
    fun filterGpaInput(input: String): String {
        val clean = input.filter { it in '0'..'9' || it in '\u09E6'..'\u09EF' || it == '.' }
        val dotIndex = clean.indexOf('.')
        return if (dotIndex != -1) {
            val intPart = clean.substring(0, dotIndex).filter { it.isDigit() }.let { if (it.isNotEmpty()) it.substring(0, minOf(1, it.length)) else "" }
            val decPart = clean.substring(dotIndex + 1).filter { it.isDigit() }.let { if (it.length > 2) it.substring(0, 2) else it }
            "$intPart.$decPart"
        } else {
            val digits = clean.filter { it.isDigit() }
            if (digits.isNotEmpty()) digits.substring(0, minOf(1, digits.length)) else clean
        }
    }

    /**
     * Filters input for GPA / Marks to strictly allow digits (Bengali or English) and decimal point (.).
     */
    fun filterGpaOrMarksInput(input: String): String {
        return filterGpaInput(input)
    }

    /**
     * Filters input for Passing Year to strictly allow digits (Bengali or English) up to 4 digits.
     */
    fun filterPassingYear(input: String): String {
        val digits = filterDigitsStrict(input)
        return if (digits.length <= 4) digits else digits.substring(0, 4)
    }

    /**
     * Converts English numbers to Bengali numerals.
     */
    fun toBanglaDigits(number: String): String {
        val banglaDigits = mapOf(
            '0' to '০', '1' to '১', '2' to '২', '3' to '৩', '4' to '৪',
            '5' to '৫', '6' to '৬', '7' to '৭', '8' to '৮', '9' to '৯'
        )
        return number.map { banglaDigits[it] ?: it }.joinToString("")
    }

    fun containsBengali(input: String): Boolean {
        return input.any { it in '\u0980'..'\u09FF' }
    }

    /**
     * Checks if a Bangla string contains scrambled transliteration artifacts,
     * such as independent vowels placed directly after consonants (e.g. সআবইতআ, পআরইমআল, রওয়).
     */
    fun isScrambledBangla(input: String): Boolean {
        if (input.isBlank()) return false
        // Matches consonant followed by independent vowel: [ক-হ][অআইঈউঊঋএঐওঔ]
        return input.contains(Regex("[\\u0995-\\u09B9][\\u0985-\\u0994]"))
    }

    /**
     * Checks if a string is a clean, genuine Bangla name without scrambled artifacts.
     */
    fun isValidBanglaName(input: String): Boolean {
        val trimmed = input.trim()
        if (trimmed.isBlank()) return false
        if (!containsBengali(trimmed)) return false
        if (isScrambledBangla(trimmed)) return false
        return true
    }

    fun toEnglishDigits(number: String): String {
        val englishDigits = mapOf(
            '০' to '0', '১' to '1', '২' to '2', '৩' to '3', '৪' to '4',
            '৫' to '5', '৬' to '6', '৭' to '7', '৮' to '8', '৯' to '9'
        )
        return number.map { englishDigits[it] ?: it }.joinToString("")
    }

    data class DetailedAge(
        val years: String,
        val months: String,
        val days: String
    ) {
        fun toDisplayString(): String {
            val parts = mutableListOf<String>()
            if (years.isNotBlank()) parts.add("$years বছর")
            if (months.isNotBlank() && months != "০") parts.add("$months মাস")
            if (days.isNotBlank() && days != "০") parts.add("$days দিন")
            return if (parts.isNotEmpty()) parts.joinToString(" ") else if (years.isNotBlank()) "$years বছর" else ""
        }
    }

    /**
     * Calculates detailed age (years, months, days) in Bangla from Date of Birth.
     * Supports formats: DD/MM/YYYY, DD-MM-YYYY, YYYY-MM-DD, or raw 8 digits DDMMYYYY.
     */
    fun calculateDetailedAge(dobStr: String): DetailedAge? {
        if (dobStr.isBlank()) return null
        val englishDob = toEnglishDigits(dobStr).trim()
        val digitsOnly = englishDob.filter { it.isDigit() }
        val parts = englishDob.split('/', '-', '.')
        var year = 0
        var month = 0
        var day = 0

        if (parts.size == 3) {
            if (parts[0].length == 4) {
                year = parts[0].toIntOrNull() ?: 0
                month = parts[1].toIntOrNull() ?: 0
                day = parts[2].toIntOrNull() ?: 0
            } else {
                day = parts[0].toIntOrNull() ?: 0
                month = parts[1].toIntOrNull() ?: 0
                year = parts[2].toIntOrNull() ?: 0
            }
        } else if (digitsOnly.length == 8) {
            day = digitsOnly.substring(0, 2).toIntOrNull() ?: 0
            month = digitsOnly.substring(2, 4).toIntOrNull() ?: 0
            year = digitsOnly.substring(4, 8).toIntOrNull() ?: 0
        }

        val cal = java.util.Calendar.getInstance()
        val currentYear = cal.get(java.util.Calendar.YEAR)
        val currentMonth = cal.get(java.util.Calendar.MONTH) + 1
        val currentDay = cal.get(java.util.Calendar.DAY_OF_MONTH)

        if (year in 1920..currentYear && month in 1..12 && day in 1..31) {
            var y = currentYear - year
            var m = currentMonth - month
            var d = currentDay - day

            if (d < 0) {
                m -= 1
                val prevCal = java.util.Calendar.getInstance()
                prevCal.set(currentYear, currentMonth - 2, 1)
                d += prevCal.getActualMaximum(java.util.Calendar.DAY_OF_MONTH)
            }
            if (m < 0) {
                y -= 1
                m += 12
            }

            if (y >= 0) {
                return DetailedAge(
                    years = toBanglaDigits(y.toString()),
                    months = toBanglaDigits(m.toString()),
                    days = toBanglaDigits(d.toString())
                )
            }
        }
        return null
    }

    /**
     * Calculates age string in Bangla (e.g. "৩০ বছর" or "৩০ বছর ১ মাস") from Date of Birth string.
     */
    fun calculateAgeFromDob(dobStr: String): String {
        return calculateDetailedAge(dobStr)?.toDisplayString().orEmpty()
    }

    /**
     * Converts a numeric amount to Bangla words (e.g. "পঁচিশ হাজার টাকা মাত্র").
     */
    fun convertNumberToBanglaWords(amountStr: String): String {
        if (amountStr.isBlank()) return ""
        val englishDigits = toEnglishDigits(amountStr).filter { it.isDigit() }
        val amount = englishDigits.toLongOrNull() ?: return ""
        if (amount == 0L) return "শূন্য টাকা মাত্র"

        val ones = arrayOf(
            "", "এক", "দুই", "তিন", "চার", "পাঁচ", "ছয়", "সাত", "আট", "নয়",
            "দশ", "এগারো", "বারো", "তেরো", "চৌদ্দ", "পনেরো", "ষোলো", "সতেরো", "আঠারো", "উনিশ",
            "বিশ", "একুশ", "বাইশ", "তেইশ", "চব্বিশ", "পঁচিশ", "ছাব্বিশ", "সাতাশ", "আটাশ", "উনত্রিশ",
            "ত্রিশ", "একত্রিশ", "বত্রিশ", "তেত্রিশ", "চৌত্রিশ", "পঁয়ত্রিশ", "ছত্রিশ", "সাইত্রিশ", "আটত্রিশ", "উনচল্লিশ",
            "চল্লিশ", "একচল্লিশ", "বিয়াল্লিশ", "তেতাল্লিশ", "চৌয়াল্লিশ", "পঁয়তাল্লিশ", "ছয়চল্লিশ", "সাতচল্লিশ", "আটচল্লিশ", "উনপঞ্চাশ",
            "পঞ্চাশ", "একান্ন", "বায়ান্ন", "তিপ্পান্ন", "চৌয়ান্ন", "পঞ্চান্ন", "ছাপ্পান্ন", "সাতান্ন", "আটান্ন", "উনষাট",
            "ষাট", "একষট্টি", "বাষট্টি", "তেষট্টি", "চৌষট্টি", "পঁয়ষট্টি", "ছেষট্টি", "সাতষট্টি", "আটষট্টি", "উনসত্তর",
            "সত্তর", "একাত্তর", "বাহাত্তর", "তিয়াত্তর", "চৌহাত্তর", "পঁচাত্তর", "ছিয়াত্তর", "সাতাত্তর", "আটাত্তর", "উনআশি",
            "আশি", "একাশি", "বিরাশি", "তিরাশি", "চৌরাশি", "পঁচাশী", "ছিয়াশি", "সাতোআশি", "অষ্টআশি", "উননব্বই",
            "নব্বই", "একানব্বই", "বানব্বই", "তেরানব্বই", "চৌরানব্বই", "পঁচানব্বই", "ছিয়ানব্বই", "সাতানব্বই", "আটানব্বই", "নিরানব্বই"
        )

        fun convertBelowHundred(n: Int): String {
            return if (n in 1..99) ones[n] else ""
        }

        fun convertBelowThousand(n: Int): String {
            val hundred = n / 100
            val rest = n % 100
            val parts = mutableListOf<String>()
            if (hundred > 0) {
                parts.add("${ones[hundred]} শত")
            }
            val restStr = convertBelowHundred(rest)
            if (restStr.isNotEmpty()) {
                parts.add(restStr)
            }
            return parts.joinToString(" ")
        }

        var num = amount
        val parts = mutableListOf<String>()

        val crore = num / 10000000
        num %= 10000000

        val lakh = num / 100000
        num %= 100000

        val thousand = num / 1000
        num %= 1000

        val hundredAndBelow = num.toInt()

        if (crore > 0) {
            val croreStr = convertNumberToBanglaWords(crore.toString()).removeSuffix(" টাকা মাত্র")
            if (croreStr.isNotBlank()) parts.add("$croreStr কোটি")
        }
        if (lakh > 0) {
            val lakhStr = convertBelowHundred(lakh.toInt())
            if (lakhStr.isNotBlank()) parts.add("$lakhStr লক্ষ")
        }
        if (thousand > 0) {
            val thousandStr = convertBelowHundred(thousand.toInt())
            if (thousandStr.isNotBlank()) parts.add("$thousandStr হাজার")
        }
        if (hundredAndBelow > 0) {
            val hStr = convertBelowThousand(hundredAndBelow)
            if (hStr.isNotBlank()) parts.add(hStr)
        }

        val words = parts.joinToString(" ").trim()
        return if (words.isNotEmpty()) "$words টাকা মাত্র" else ""
    }
}
