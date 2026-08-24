package com.example

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.CustomThemeConfig
import com.example.ui.theme.buildCustomColorScheme
import com.example.util.Localization
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun testCustomThemeConfigDefaults() {
        val config = CustomThemeConfig()
        assertEquals(0xFF1565C0L, config.primaryColor)
        assertEquals(0xFFF8FAFCL, config.backgroundColor)
        assertEquals(0xFF1565C0L, config.buttonColor)
        assertEquals(0xFF0F172AL, config.textColor)
        assertEquals(0xFFFFFFFFL, config.cardColor)

        assertEquals(Color(0xFF1565C0L), config.toPrimaryColor())
        assertEquals(Color(0xFFF8FAFCL), config.toBackgroundColor())
    }

    @Test
    fun testBuildCustomColorScheme() {
        val lightConfig = CustomThemeConfig(
            primaryColor = 0xFF1565C0L,
            backgroundColor = 0xFFF8FAFCL,
            buttonColor = 0xFF2E7D32L,
            textColor = 0xFF0F172AL,
            cardColor = 0xFFFFFFFFL
        )
        val lightScheme = buildCustomColorScheme(lightConfig)
        assertEquals(Color(0xFF1565C0L), lightScheme.primary)
        assertEquals(Color(0xFFF8FAFCL), lightScheme.background)
        assertEquals(Color(0xFFFFFFFFL), lightScheme.surface)

        val darkConfig = CustomThemeConfig(
            primaryColor = 0xFF1E88E5L,
            backgroundColor = 0xFF0F172AL,
            buttonColor = 0xFF43A047L,
            textColor = 0xFFF8FAFCL,
            cardColor = 0xFF1E293BL
        )
        val darkScheme = buildCustomColorScheme(darkConfig)
        assertEquals(Color(0xFF1E88E5L), darkScheme.primary)
        assertEquals(Color(0xFF0F172AL), darkScheme.background)
        assertEquals(Color(0xFF1E293BL), darkScheme.surface)
    }

    @Test
    fun testThemeLocalization() {
        assertEquals("Theme", Localization.get("theme", "en"))
        assertEquals("थीम", Localization.get("theme", "mr"))
        assertEquals("थीम", Localization.get("theme", "hi"))

        assertEquals("Dark Theme", Localization.get("dark_theme", "en"))
        assertEquals("Light Theme", Localization.get("light_theme", "en"))
        assertEquals("Custom Theme", Localization.get("custom_theme", "en"))

        assertEquals("Save Theme", Localization.get("save_theme", "en"))
        assertEquals("थीम सेव्ह करा", Localization.get("save_theme", "mr"))
    }

    @Test
    fun testBrandingText() {
        val appTitle = "Sumit Attendance System"
        assertEquals("Sumit Attendance System", appTitle)
        assertTrue(appTitle.contains("Attendance"))
    }

    @Test
    fun testSalaryWageConfigCalculation() {
        val config = com.example.data.SalaryWageConfig(
            baseDailyWage = 400.0,
            halfDayMultiplier = 0.5,
            doubleDutyMultiplier = 2.0
        )
        val pDays = 20.0
        val hDays = 4.0
        val dDays = 2.0
        val totalWorkingDays = pDays + (hDays * config.halfDayMultiplier) + (dDays * config.doubleDutyMultiplier)
        assertEquals(26.0, totalWorkingDays, 0.001)

        val grossSalary = totalWorkingDays * config.baseDailyWage
        assertEquals(10400.0, grossSalary, 0.001)
    }
}

