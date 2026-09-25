package com.example.util

object BanglaTextValidator {

    /**
     * Checks if a character is a Bengali letter or allowable space/separator.
     * Rejects all digits (0-9, ০-৯), English letters, and irrelevant punctuation.
     */
    fun isBengaliLetterOrSpace(c: Char): Boolean {
        if (c.isWhitespace()) return true
        if (c == '-' || c == '(' || c == ')') return true
        // Exclude Bengali digits \u09E6 to \u09EF
        if (c in '\u09E6'..'\u09EF') return false
        // Exclude English letters
        if (c in 'a'..'z' || c in 'A'..'Z') return false
        // Exclude ASCII digits
        if (c.isDigit()) return false
        // Bengali letter and sign block
        return c in '\u0981'..'\u09FA'
    }

    /**
     * Filters input string to contain only Bengali characters and allowed symbols.
     * Prevents digits and non-Bengali alphabet entry.
     */
    fun filterBanglaText(input: String): String {
        return input.filter { isBengaliLetterOrSpace(it) }
    }

    /**
     * Filters input to contain only digits (Bengali or English numbers).
     */
    fun filterDigitsOnly(input: String): String {
        return input.filter { it in '0'..'9' || it in '\u09E6'..'\u09EF' }
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

    fun toEnglishDigits(number: String): String {
        val englishDigits = mapOf(
            '০' to '0', '১' to '1', '২' to '2', '৩' to '3', '৪' to '4',
            '৫' to '5', '৬' to '6', '৭' to '7', '৮' to '8', '৯' to '9'
        )
        return number.map { englishDigits[it] ?: it }.joinToString("")
    }
}
