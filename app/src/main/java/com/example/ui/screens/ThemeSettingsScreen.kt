package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Brightness7
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.BrandedAppTitle
import com.example.ui.theme.BrandBlueContainer
import com.example.ui.theme.BrandBlueDark
import com.example.ui.theme.BrandBluePrimary
import com.example.ui.theme.BrandGreenContainer
import com.example.ui.theme.BrandGreenSecondary
import com.example.ui.theme.BrandOrangeAccent
import com.example.ui.theme.BrandPurpleAccent
import com.example.ui.theme.CustomThemeConfig
import com.example.ui.theme.LocalAppThemeColors
import com.example.util.Localization
import com.example.viewmodel.AttendanceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeSettingsScreen(
    viewModel: AttendanceViewModel,
    onBack: () -> Unit
) {
    val lang by viewModel.appLanguage.collectAsState()
    val activeTheme by viewModel.appTheme.collectAsState()
    val savedCustomConfig by viewModel.customThemeConfig.collectAsState()

    val context = LocalContext.current

    // Local custom draft state for live interactive preview before saving
    var draftPrimary by remember(savedCustomConfig) { mutableStateOf(savedCustomConfig.primaryColor) }
    var draftBackground by remember(savedCustomConfig) { mutableStateOf(savedCustomConfig.backgroundColor) }
    var draftButton by remember(savedCustomConfig) { mutableStateOf(savedCustomConfig.buttonColor) }
    var draftText by remember(savedCustomConfig) { mutableStateOf(savedCustomConfig.textColor) }
    var draftCard by remember(savedCustomConfig) { mutableStateOf(savedCustomConfig.cardColor) }

    val currentDraftConfig = CustomThemeConfig(
        primaryColor = draftPrimary,
        backgroundColor = draftBackground,
        buttonColor = draftButton,
        textColor = draftText,
        cardColor = draftCard
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    BrandedAppTitle(
                        fontSize = 18,
                        showSubtitle = true,
                        subtitleText = Localization.get("theme_settings", lang) + " • " + Localization.get("theme", lang)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Banner with Active Theme State
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(
                                            listOf(
                                                MaterialTheme.colorScheme.primary,
                                                MaterialTheme.colorScheme.secondary
                                            )
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Palette,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = Localization.get("theme_settings", lang),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = Localization.get("theme_settings_desc", lang),
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Active Theme Pill
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = when (activeTheme) {
                                    "dark" -> "Dark Mode"
                                    "custom" -> "Custom"
                                    else -> "Light Mode"
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }
            }

            // -----------------------------------------------------------------
            // OPTION 1: DARK THEME
            // -----------------------------------------------------------------
            item {
                ThemeOptionCard(
                    title = Localization.get("dark_theme", lang),
                    description = Localization.get("dark_theme_desc", lang),
                    icon = Icons.Default.DarkMode,
                    isSelected = activeTheme == "dark",
                    previewBg = Color(0xFF0F172A),
                    previewCard = Color(0xFF1E293B),
                    previewPrimary = Color(0xFF1E88E5),
                    previewText = Color(0xFFF8FAFC),
                    testTag = "theme_option_dark",
                    onSelect = {
                        viewModel.setTheme("dark")
                        Toast.makeText(context, "Dark Theme applied", Toast.LENGTH_SHORT).show()
                    }
                )
            }

            // -----------------------------------------------------------------
            // OPTION 2: LIGHT THEME
            // -----------------------------------------------------------------
            item {
                ThemeOptionCard(
                    title = Localization.get("light_theme", lang),
                    description = Localization.get("light_theme_desc", lang),
                    icon = Icons.Default.LightMode,
                    isSelected = activeTheme == "light",
                    previewBg = Color(0xFFF8FAFC),
                    previewCard = Color(0xFFFFFFFF),
                    previewPrimary = Color(0xFF1565C0),
                    previewText = Color(0xFF0F172A),
                    testTag = "theme_option_light",
                    onSelect = {
                        viewModel.setTheme("light")
                        Toast.makeText(context, "Light Theme applied", Toast.LENGTH_SHORT).show()
                    }
                )
            }

            // -----------------------------------------------------------------
            // OPTION 3: CUSTOM THEME
            // -----------------------------------------------------------------
            item {
                ThemeOptionCard(
                    title = Localization.get("custom_theme", lang),
                    description = Localization.get("custom_theme_desc", lang),
                    icon = Icons.Default.ColorLens,
                    isSelected = activeTheme == "custom",
                    previewBg = currentDraftConfig.toBackgroundColor(),
                    previewCard = currentDraftConfig.toCardColor(),
                    previewPrimary = currentDraftConfig.toPrimaryColor(),
                    previewText = currentDraftConfig.toTextColor(),
                    testTag = "theme_option_custom",
                    onSelect = {
                        viewModel.saveCustomTheme(currentDraftConfig)
                        Toast.makeText(context, "Custom Theme applied", Toast.LENGTH_SHORT).show()
                    }
                )
            }

            // -----------------------------------------------------------------
            // CUSTOM THEME CUSTOMIZER & LIVE PREVIEW (Shown for Customizing)
            // -----------------------------------------------------------------
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(
                        1.5.dp,
                        if (activeTheme == "custom") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Custom Palette Studio",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Customize individual elements & preview live",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            TextButton(
                                onClick = {
                                    val def = CustomThemeConfig()
                                    draftPrimary = def.primaryColor
                                    draftBackground = def.backgroundColor
                                    draftButton = def.buttonColor
                                    draftText = def.textColor
                                    draftCard = def.cardColor
                                    Toast.makeText(context, "Draft reset to defaults", Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(Localization.get("reset_theme", lang), fontSize = 11.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // -------------------------------------------------------------
                        // LIVE INTERACTIVE PREVIEW CARD
                        // -------------------------------------------------------------
                        Text(
                            text = Localization.get("live_preview", lang),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        LiveThemePreviewCanvas(
                            config = currentDraftConfig,
                            lang = lang
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        Spacer(modifier = Modifier.height(14.dp))

                        // -------------------------------------------------------------
                        // COLOR CATEGORY 1: PRIMARY COLOR
                        // -------------------------------------------------------------
                        ColorPickerSection(
                            title = Localization.get("primary_color", lang),
                            selectedColorLong = draftPrimary,
                            onColorSelected = { draftPrimary = it },
                            presetColors = listOf(
                                0xFF1565C0L, // Brand Royal Blue
                                0xFF0D47A1L, // Deep Navy
                                0xFF2E7D32L, // Emerald Green
                                0xFF1B5E20L, // Forest Green
                                0xFF7B1FA2L, // Royal Purple
                                0xFFEF6C00L, // Vibrant Orange
                                0xFFC62828L, // Crimson Red
                                0xFF00897BL, // Deep Teal
                                0xFF4F46E5L, // Indigo
                                0xFF0284C7L  // Ocean Sky Blue
                            )
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // -------------------------------------------------------------
                        // COLOR CATEGORY 2: BACKGROUND COLOR
                        // -------------------------------------------------------------
                        ColorPickerSection(
                            title = Localization.get("background_color", lang),
                            selectedColorLong = draftBackground,
                            onColorSelected = { draftBackground = it },
                            presetColors = listOf(
                                0xFFF8FAFCL, // Clean Off-White Slate
                                0xFFFFFFFFL, // Pure White
                                0xFFF1F8E9L, // Soft Pastel Mint
                                0xFFFFF8E1L, // Warm Golden Cream
                                0xFFF3E5F5L, // Soft Lavender
                                0xFF0F172AL, // Dark Charcoal Slate
                                0xFF000000L, // AMOLED Pure Black
                                0xFF111827L, // Midnight Gray
                                0xFF1E293BL, // Steel Dark Slate
                                0xFF1A1A24L  // Deep Indigo Night
                            )
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // -------------------------------------------------------------
                        // COLOR CATEGORY 3: BUTTON COLOR
                        // -------------------------------------------------------------
                        ColorPickerSection(
                            title = Localization.get("button_color", lang),
                            selectedColorLong = draftButton,
                            onColorSelected = { draftButton = it },
                            presetColors = listOf(
                                0xFF1565C0L, // Blue Action Button
                                0xFF2E7D32L, // Green Action Button
                                0xFF7B1FA2L, // Purple Accent Button
                                0xFFEF6C00L, // Orange Action Button
                                0xFFC62828L, // Crimson Action Button
                                0xFF00796BL, // Dark Teal Button
                                0xFF1E293BL, // Dark Sleek Button
                                0xFF2563EBL, // Bright Royal Blue
                                0xFF059669L, // Vibrant Emerald
                                0xFFD97706L  // Amber Bronze Button
                            )
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // -------------------------------------------------------------
                        // COLOR CATEGORY 4: TEXT COLOR
                        // -------------------------------------------------------------
                        ColorPickerSection(
                            title = Localization.get("text_color", lang),
                            selectedColorLong = draftText,
                            onColorSelected = { draftText = it },
                            presetColors = listOf(
                                0xFF0F172AL, // High Contrast Dark
                                0xFF000000L, // Pitch Black
                                0xFF1E293BL, // Dark Slate
                                0xFF334155L, // Charcoal
                                0xFFF8FAFCL, // Pure White Text
                                0xFFFFFFFFL, // Crisp White Text
                                0xFFE2E8F0L, // Soft Light Gray
                                0xFFCBD5E1L, // Muted Silver
                                0xFFFEF3C7L, // Warm Ivory
                                0xFF86EFACL  // Mint Glow Text
                            )
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // -------------------------------------------------------------
                        // COLOR CATEGORY 5: CARD / SURFACE COLOR
                        // -------------------------------------------------------------
                        ColorPickerSection(
                            title = Localization.get("card_color", lang),
                            selectedColorLong = draftCard,
                            onColorSelected = { draftCard = it },
                            presetColors = listOf(
                                0xFFFFFFFFL, // Pure White Card
                                0xFFF1F5F9L, // Light Slate Card
                                0xFFE8F5E9L, // Soft Green Tint Card
                                0xFFE3F2FDL, // Soft Blue Tint Card
                                0xFFFFFDE7L, // Pale Cream Card
                                0xFF1E293BL, // Dark Slate Card
                                0xFF182234L, // Midnight Navy Card
                                0xFF121212L, // Pitch Dark Card
                                0xFF27272AL, // Zinc Dark Card
                                0xFF1F2937L  // Cool Gray Card
                            )
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // -------------------------------------------------------------
                        // "SAVE THEME" ACTION BUTTON
                        // -------------------------------------------------------------
                        Button(
                            onClick = {
                                viewModel.saveCustomTheme(currentDraftConfig)
                                Toast.makeText(context, "Custom theme saved & applied across all screens!", Toast.LENGTH_LONG).show()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = currentDraftConfig.toButtonColor()
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("btn_save_custom_theme")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Save,
                                contentDescription = null,
                                tint = if (currentDraftConfig.toButtonColor().luminance() > 0.5f) Color(0xFF0F172A) else Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = Localization.get("save_theme", lang),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (currentDraftConfig.toButtonColor().luminance() > 0.5f) Color(0xFF0F172A) else Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// THEME OPTION SELECTION CARD (Dark / Light / Custom)
// -----------------------------------------------------------------------------
@Composable
fun ThemeOptionCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    previewBg: Color,
    previewCard: Color,
    previewPrimary: Color,
    previewText: Color,
    testTag: String,
    onSelect: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surface
        ),
        border = androidx.compose.foundation.BorderStroke(
            if (isSelected) 2.dp else 1.dp,
            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Mini palette visual preview badge
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = previewBg,
                    border = androidx.compose.foundation.BorderStroke(1.dp, previewPrimary.copy(alpha = 0.5f)),
                    modifier = Modifier.size(46.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = previewCard,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = previewPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (isSelected) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = MaterialTheme.colorScheme.primary
                            ) {
                                Text(
                                    text = "ACTIVE",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = description,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            RadioButton(
                selected = isSelected,
                onClick = onSelect,
                colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
            )
        }
    }
}

// -----------------------------------------------------------------------------
// LIVE THEME PREVIEW CANVAS
// -----------------------------------------------------------------------------
@Composable
fun LiveThemePreviewCanvas(
    config: CustomThemeConfig,
    lang: String
) {
    val bg = config.toBackgroundColor()
    val card = config.toCardColor()
    val text = config.toTextColor()
    val primary = config.toPrimaryColor()
    val button = config.toButtonColor()

    val isDark = bg.luminance() < 0.5f
    val onBtnText = if (button.luminance() > 0.5f) Color(0xFF0F172A) else Color.White
    val onPrimaryText = if (primary.luminance() > 0.5f) Color(0xFF0F172A) else Color.White

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = bg,
        border = androidx.compose.foundation.BorderStroke(1.5.dp, primary.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "S",
                            fontWeight = FontWeight.Bold,
                            color = onPrimaryText,
                            fontSize = 15.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Sumit Attendance",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = text
                        )
                        Text(
                            text = "Live Theme Demo",
                            fontSize = 10.sp,
                            color = text.copy(alpha = 0.7f)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = primary.copy(alpha = 0.2f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, primary.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = "PREVIEW",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = primary,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Sample Worker Card in Custom Card Color
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = card,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isDark) Color(0xFF475569).copy(alpha = 0.4f) else Color(0xFFE2E8F0)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Ramesh Pawar (W-101)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = text
                        )
                        Text(
                            text = "Sanitation Worker • Present (₹300)",
                            fontSize = 10.sp,
                            color = text.copy(alpha = 0.7f)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF2E7D32).copy(alpha = 0.2f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2E7D32).copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = "P (1.0 Day)",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color(0xFF81C784) else Color(0xFF1B5E20),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Sample Action Buttons (Button Color, PDF, WhatsApp)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Primary App Button
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = button,
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 7.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = onBtnText, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Save Log", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = onBtnText)
                    }
                }

                // PDF Button
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = primary,
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 7.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = onPrimaryText, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("PDF Report", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = onPrimaryText)
                    }
                }

                // WhatsApp Button
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF25D366),
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 7.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("WhatsApp", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// COLOR PICKER COMPONENT WITH PRESETS & SELECTION HIGHLIGHT
// -----------------------------------------------------------------------------
@Composable
fun ColorPickerSection(
    title: String,
    selectedColorLong: Long,
    onColorSelected: (Long) -> Unit,
    presetColors: List<Long>
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = Color(selectedColorLong),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.size(20.dp)
                ) {}
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Surface(
                shape = RoundedCornerShape(4.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Text(
                    text = "#" + java.lang.Long.toHexString(selectedColorLong).uppercase().takeLast(6),
                    fontSize = 10.sp,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Horizontal swatch list
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            presetColors.forEach { colorLong ->
                val isSelected = selectedColorLong == colorLong
                val color = Color(colorLong)
                val isLight = color.luminance() > 0.5f

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(color)
                        .border(
                            width = if (isSelected) 3.dp else 1.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.3f),
                            shape = CircleShape
                        )
                        .clickable { onColorSelected(colorLong) },
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Selected",
                            tint = if (isLight) Color.Black else Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
