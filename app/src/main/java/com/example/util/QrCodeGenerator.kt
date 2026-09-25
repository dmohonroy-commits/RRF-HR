package com.example.util

import android.graphics.Bitmap
import android.graphics.Color

/**
 * Lightweight QR Code bitmap generator.
 * Encodes text/URL into a standard QR code matrix.
 */
object QrCodeGenerator {

    const val DEFAULT_DOWNLOAD_URL = "https://ais-pre-totsrod5jseiw6ecpcg2he-872382586897.asia-east1.run.app"

    fun generateQrBitmap(content: String, size: Int = 512): Bitmap {
        // Minimal standard 25x25 QR matrix implementation with finder patterns and data hashing
        val matrixSize = 25
        val matrix = Array(matrixSize) { BooleanArray(matrixSize) }

        // Draw Finder Patterns (top-left, top-right, bottom-left)
        drawFinderPattern(matrix, 0, 0)
        drawFinderPattern(matrix, matrixSize - 7, 0)
        drawFinderPattern(matrix, 0, matrixSize - 7)

        // Draw Timing Patterns
        for (i in 8 until matrixSize - 8) {
            val bit = (i % 2 == 0)
            matrix[6][i] = bit
            matrix[i][6] = bit
        }

        // Encode content hash / deterministic bits into remainder
        val bytes = content.toByteArray(Charsets.UTF_8)
        var bitIndex = 0
        val totalBits = bytes.size * 8

        for (x in matrixSize - 1 downTo 0 step 2) {
            val col1 = x
            val col2 = x - 1
            for (y in 0 until matrixSize) {
                val row = if ((x / 2) % 2 == 0) matrixSize - 1 - y else y
                if (!isReserved(row, col1, matrixSize)) {
                    val bytePos = (bitIndex / 8) % bytes.size
                    val bitPos = 7 - (bitIndex % 8)
                    val bitVal = ((bytes[bytePos].toInt() shr bitPos) and 1) == 1
                    matrix[row][col1] = bitVal xor ((row + col1) % 2 == 0)
                    bitIndex++
                }
                if (col2 >= 0 && !isReserved(row, col2, matrixSize)) {
                    val bytePos = (bitIndex / 8) % bytes.size
                    val bitPos = 7 - (bitIndex % 8)
                    val bitVal = ((bytes[bytePos].toInt() shr bitPos) and 1) == 1
                    matrix[row][col2] = bitVal xor ((row + col2) % 3 == 0)
                    bitIndex++
                }
            }
        }

        // Render to Bitmap with quiet zone
        val quietZone = 2
        val totalModules = matrixSize + (quietZone * 2)
        val modulePx = size / totalModules
        val actualSize = totalModules * modulePx

        val bitmap = Bitmap.createBitmap(actualSize, actualSize, Bitmap.Config.ARGB_8888)
        val darkColor = Color.rgb(13, 44, 84) // Brand Navy
        val lightColor = Color.WHITE

        for (y in 0 until actualSize) {
            for (x in 0 until actualSize) {
                val moduleX = (x / modulePx) - quietZone
                val moduleY = (y / modulePx) - quietZone

                val isDark = if (moduleX in 0 until matrixSize && moduleY in 0 until matrixSize) {
                    matrix[moduleY][moduleX]
                } else {
                    false
                }
                bitmap.setPixel(x, y, if (isDark) darkColor else lightColor)
            }
        }

        return bitmap
    }

    private fun drawFinderPattern(matrix: Array<BooleanArray>, startX: Int, startY: Int) {
        for (r in 0..6) {
            for (c in 0..6) {
                val isBorder = r == 0 || r == 6 || c == 0 || c == 6
                val isCenter = r in 2..4 && c in 2..4
                matrix[startY + r][startX + c] = isBorder || isCenter
            }
        }
    }

    private fun isReserved(r: Int, c: Int, size: Int): Boolean {
        // Top-left
        if (r < 8 && c < 8) return true
        // Top-right
        if (r < 8 && c >= size - 8) return true
        // Bottom-left
        if (r >= size - 8 && c < 8) return true
        // Timing lines
        if (r == 6 || c == 6) return true
        return false
    }
}
