package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.util.BanglaTextValidator
import com.example.util.BangladeshDistricts
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("RRF HR", appName)
    }

    @Test
    fun `bangla text validator filters english and keeps bangla`() {
        val input = "Mr রহিম 123"
        val filtered = BanglaTextValidator.filterBanglaText(input)
        assertEquals("রহিম", filtered.trim())
    }

    @Test
    fun `bangla digits conversion works`() {
        val input = "01733073076"
        val converted = BanglaTextValidator.toBanglaDigits(input)
        assertEquals("০১৭৩৩০৭৩০৭৬", converted)
    }

    @Test
    fun `district list contains 64 districts`() {
        assertTrue(BangladeshDistricts.districts.size >= 64)
        assertTrue(BangladeshDistricts.districts.contains("যশোর"))
    }
}
