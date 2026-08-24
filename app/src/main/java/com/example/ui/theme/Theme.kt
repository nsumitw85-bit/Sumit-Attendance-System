package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

data class CustomThemeConfig(
    val primaryColor: Long = 0xFF1565C0L,      // Primary Accent
    val backgroundColor: Long = 0xFFF8FAFCL,   // Screen Background
    val buttonColor: Long = 0xFF1565C0L,       // Action Button Color
    val textColor: Long = 0xFF0F172AL,         // Main Text Color
    val cardColor: Long = 0xFFFFFFFFL          // Surface / Card Color
) {
    fun toPrimaryColor(): Color = Color(primaryColor)
    fun toBackgroundColor(): Color = Color(backgroundColor)
    fun toButtonColor(): Color = Color(buttonColor)
    fun toTextColor(): Color = Color(textColor)
    fun toCardColor(): Color = Color(cardColor)
}

data class AppThemeColors(
    val primary: Color,
    val background: Color,
    val button: Color,
    val text: Color,
    val card: Color,
    val isDark: Boolean
)

val LocalAppThemeColors = staticCompositionLocalOf {
    AppThemeColors(
        primary = BrandBluePrimary,
        background = SurfaceLight,
        button = BrandBluePrimary,
        text = TextPrimaryLight,
        card = SurfaceCardLight,
        isDark = false
    )
}

private val DarkColorScheme = darkColorScheme(
    primary = BrandBlueLight,
    onPrimary = Color.White,
    primaryContainer = BrandBlueDark,
    onPrimaryContainer = BrandBlueContainer,
    secondary = BrandGreenLight,
    onSecondary = Color.White,
    secondaryContainer = BrandGreenDark,
    onSecondaryContainer = BrandGreenContainer,
    tertiary = BrandOrangeLight,
    onTertiary = Color.White,
    tertiaryContainer = BrandOrangeAccent,
    onTertiaryContainer = BrandOrangeContainer,
    background = SurfaceDark,
    onBackground = TextPrimaryDark,
    surface = SurfaceCardDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = Color(0xFF1E293B),
    onSurfaceVariant = TextSecondaryDark,
    outline = DividerDark,
    outlineVariant = Color(0xFF334155)
)

private val LightColorScheme = lightColorScheme(
    primary = BrandBluePrimary,
    onPrimary = Color.White,
    primaryContainer = BrandBlueContainer,
    onPrimaryContainer = BrandBlueDark,
    secondary = BrandGreenSecondary,
    onSecondary = Color.White,
    secondaryContainer = BrandGreenContainer,
    onSecondaryContainer = BrandGreenDark,
    tertiary = BrandOrangeAccent,
    onTertiary = Color.White,
    tertiaryContainer = BrandOrangeContainer,
    onTertiaryContainer = Color(0xFFE65100),
    background = SurfaceLight,
    onBackground = TextPrimaryLight,
    surface = SurfaceCardLight,
    onSurface = TextPrimaryLight,
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = TextSecondaryLight,
    outline = DividerLight,
    outlineVariant = Color(0xFFE2E8F0)
)

fun buildCustomColorScheme(config: CustomThemeConfig): ColorScheme {
    val primary = config.toPrimaryColor()
    val bg = config.toBackgroundColor()
    val button = config.toButtonColor()
    val text = config.toTextColor()
    val card = config.toCardColor()

    val isBgDark = bg.luminance() < 0.5f
    val onPrimaryColor = if (primary.luminance() > 0.5f) Color(0xFF0F172A) else Color.White
    val onButtonColor = if (button.luminance() > 0.5f) Color(0xFF0F172A) else Color.White

    return if (isBgDark) {
        darkColorScheme(
            primary = primary,
            onPrimary = onPrimaryColor,
            primaryContainer = primary.copy(alpha = 0.3f),
            onPrimaryContainer = Color.White,
            secondary = button,
            onSecondary = onButtonColor,
            secondaryContainer = button.copy(alpha = 0.3f),
            onSecondaryContainer = Color.White,
            tertiary = BrandOrangeAccent,
            onTertiary = Color.White,
            background = bg,
            onBackground = text,
            surface = card,
            onSurface = text,
            surfaceVariant = card.copy(alpha = 0.85f),
            onSurfaceVariant = text.copy(alpha = 0.7f),
            outline = if (isBgDark) Color(0xFF475569) else Color(0xFFCBD5E1),
            outlineVariant = if (isBgDark) Color(0xFF334155) else Color(0xFFE2E8F0)
        )
    } else {
        lightColorScheme(
            primary = primary,
            onPrimary = onPrimaryColor,
            primaryContainer = primary.copy(alpha = 0.15f),
            onPrimaryContainer = primary,
            secondary = button,
            onSecondary = onButtonColor,
            secondaryContainer = button.copy(alpha = 0.15f),
            onSecondaryContainer = button,
            tertiary = BrandOrangeAccent,
            onTertiary = Color.White,
            background = bg,
            onBackground = text,
            surface = card,
            onSurface = text,
            surfaceVariant = Color(0xFFF1F5F9),
            onSurfaceVariant = text.copy(alpha = 0.75f),
            outline = Color(0xFFCBD5E1),
            outlineVariant = Color(0xFFE2E8F0)
        )
    }
}

@Composable
fun SumitAttendanceTheme(
    themeMode: String = "light", // "light", "dark", "custom", "system"
    customConfig: CustomThemeConfig = CustomThemeConfig(),
    content: @Composable () -> Unit
) {
    val isSystemDark = isSystemInDarkTheme()

    val (colorScheme, appColors) = when (themeMode.lowercase()) {
        "dark" -> {
            DarkColorScheme to AppThemeColors(
                primary = BrandBlueLight,
                background = SurfaceDark,
                button = BrandBlueLight,
                text = TextPrimaryDark,
                card = SurfaceCardDark,
                isDark = true
            )
        }
        "custom" -> {
            val customScheme = buildCustomColorScheme(customConfig)
            val isDark = customConfig.toBackgroundColor().luminance() < 0.5f
            customScheme to AppThemeColors(
                primary = customConfig.toPrimaryColor(),
                background = customConfig.toBackgroundColor(),
                button = customConfig.toButtonColor(),
                text = customConfig.toTextColor(),
                card = customConfig.toCardColor(),
                isDark = isDark
            )
        }
        "system" -> {
            if (isSystemDark) {
                DarkColorScheme to AppThemeColors(
                    primary = BrandBlueLight,
                    background = SurfaceDark,
                    button = BrandBlueLight,
                    text = TextPrimaryDark,
                    card = SurfaceCardDark,
                    isDark = true
                )
            } else {
                LightColorScheme to AppThemeColors(
                    primary = BrandBluePrimary,
                    background = SurfaceLight,
                    button = BrandBluePrimary,
                    text = TextPrimaryLight,
                    card = SurfaceCardLight,
                    isDark = false
                )
            }
        }
        else -> { // "light"
            LightColorScheme to AppThemeColors(
                primary = BrandBluePrimary,
                background = SurfaceLight,
                button = BrandBluePrimary,
                text = TextPrimaryLight,
                card = SurfaceCardLight,
                isDark = false
            )
        }
    }

    CompositionLocalProvider(
        LocalAppThemeColors provides appColors
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}

