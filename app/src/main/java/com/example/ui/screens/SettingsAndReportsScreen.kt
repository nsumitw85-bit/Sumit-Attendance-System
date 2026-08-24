package com.example.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.BrandedAppTitle
import com.example.ui.components.DatabaseBackupSection
import com.example.ui.components.MonthNavigatorBar
import com.example.ui.components.SumitAttendanceLogo
import com.example.ui.components.createSumitLogoBitmap
import com.example.ui.theme.BrandBlueContainer
import com.example.ui.theme.BrandBlueDark
import com.example.ui.theme.BrandBluePrimary
import com.example.ui.theme.BrandGreenContainer
import com.example.ui.theme.BrandGreenSecondary
import com.example.ui.theme.BrandOrangeAccent
import com.example.ui.theme.BrandOrangeContainer
import com.example.ui.theme.BrandPurpleAccent
import com.example.ui.theme.BrandPurpleContainer
import com.example.ui.theme.StatusAbsent
import com.example.util.CloudBackupManager
import com.example.util.Localization
import com.example.util.PdfReportGenerator
import com.example.viewmodel.AttendanceViewModel
import kotlinx.coroutines.launch
import java.io.File
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsAndReportsScreen(
    viewModel: AttendanceViewModel,
    onBack: () -> Unit,
    onNavigateToMonthlySummary: () -> Unit = {},
    onNavigateToSalaryCalculation: () -> Unit = {},
    onNavigateToTheme: () -> Unit = {},
    onLogout: () -> Unit
) {
    val lang by viewModel.appLanguage.collectAsState()
    val theme by viewModel.appTheme.collectAsState()
    val dailyWage by viewModel.dailyWage.collectAsState()
    val ttsEnabled by viewModel.ttsEnabled.collectAsState()
    val selectedMonth by viewModel.selectedMonth.collectAsState()
    val monthlyStats by viewModel.monthlyStats.collectAsState()

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var activePdfFile by remember { mutableStateOf<File?>(null) }
    var activePdfTitle by remember { mutableStateOf("") }
    var showPdfDialog by remember { mutableStateOf(false) }

    var showWageDialog by remember { mutableStateOf(false) }
    var customWageInput by remember { mutableStateOf(dailyWage.toInt().toString()) }
    var showLogoDialog by remember { mutableStateOf(false) }

    var isGeneratingPdf by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    BrandedAppTitle(
                        fontSize = 18,
                        showSubtitle = true,
                        subtitleText = "Reports & Settings • अहवाल व सेटिंग्ज",
                        showLogo = true,
                        logoSize = 32.dp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = BrandBluePrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Interactive Monthly Logs View Button Card
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = BrandGreenContainer,
                    border = androidx.compose.foundation.BorderStroke(1.dp, BrandGreenSecondary.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToMonthlySummary() }
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Assessment,
                                contentDescription = null,
                                tint = BrandGreenSecondary,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = Localization.get("monthly_summary", lang),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = BrandGreenSecondary
                                )
                                Text(
                                    text = "Interactive day-by-day logs & worker muster roll",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Open",
                            tint = BrandGreenSecondary
                        )
                    }
                }
            }

            // Interactive Configurable Salary Module Button Card
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFE8F5E9),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2E7D32).copy(alpha = 0.4f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToSalaryCalculation() }
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CurrencyRupee,
                                contentDescription = null,
                                tint = Color(0xFF2E7D32),
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = Localization.get("salary_calculation", lang),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color(0xFF2E7D32)
                                )
                                Text(
                                    text = "Configurable day/half-day wages, advances, bonuses & payslips",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Open",
                            tint = Color(0xFF2E7D32)
                        )
                    }
                }
            }

            // SECTION 1: PDF REPORTS GENERATOR (Top Priority)
            item {
                Text(
                    text = "PDF Reports Generator (अहवाल तयार करा)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))

                // Month Switcher for Monthly Reports
                MonthNavigatorBar(
                    currentMonth = selectedMonth,
                    onPreviousMonth = { viewModel.shiftMonth(-1) },
                    onNextMonth = { viewModel.shiftMonth(1) },
                    onPickMonth = { /* month picker */ }
                )
            }

            // Report 1: Daily Attendance PDF
            item {
                ReportCardItem(
                    title = Localization.get("daily_attendance_pdf", lang),
                    description = "A4 format daily sheet with P/A/H/D badges, worker roles & signatures (No salary)",
                    icon = Icons.Default.Assessment,
                    gradient = listOf(BrandBluePrimary, Color(0xFF0288D1)),
                    lang = lang,
                    isLoading = isGeneratingPdf,
                    onDownloadClick = {
                        isGeneratingPdf = true
                        viewModel.generateDailyAttendancePdf(context) { file ->
                            isGeneratingPdf = false
                            activePdfFile = file
                            activePdfTitle = "Daily Attendance Report"
                            showPdfDialog = true
                            if (file != null) {
                                PdfReportGenerator.viewPdf(context, file)
                            }
                        }
                    },
                    onWhatsAppClick = {
                        isGeneratingPdf = true
                        viewModel.generateDailyAttendancePdf(context) { file ->
                            isGeneratingPdf = false
                            if (file != null) {
                                PdfReportGenerator.sharePdfToWhatsApp(context, file, "Daily Attendance Report")
                            }
                        }
                    },
                    testTag = "card_daily_attendance_pdf"
                )
            }

            // Report 2: Monthly Attendance Master Sheet PDF
            item {
                ReportCardItem(
                    title = Localization.get("monthly_attendance_pdf", lang),
                    description = "Complete monthly muster roll with total Present, Absent, Half Day & Double Duty counts",
                    icon = Icons.Default.CalendarMonth,
                    gradient = listOf(BrandGreenSecondary, Color(0xFF00897B)),
                    lang = lang,
                    isLoading = isGeneratingPdf,
                    onDownloadClick = {
                        isGeneratingPdf = true
                        viewModel.generateMonthlyAttendancePdf(context) { file ->
                            isGeneratingPdf = false
                            activePdfFile = file
                            activePdfTitle = "Monthly Attendance Master Sheet"
                            showPdfDialog = true
                            if (file != null) {
                                PdfReportGenerator.viewPdf(context, file)
                            }
                        }
                    },
                    onWhatsAppClick = {
                        isGeneratingPdf = true
                        viewModel.generateMonthlyAttendancePdf(context) { file ->
                            isGeneratingPdf = false
                            if (file != null) {
                                PdfReportGenerator.sharePdfToWhatsApp(context, file, "Monthly Attendance Master Sheet")
                            }
                        }
                    },
                    testTag = "card_monthly_attendance_pdf"
                )
            }

            // Report 3: Monthly Salary & Wage PDF
            item {
                val totalStaff = monthlyStats.size
                val grandSalary = monthlyStats.sumOf { it.finalSalary }
                ReportCardItem(
                    title = Localization.get("monthly_salary_pdf", lang),
                    description = "Configured monthly payroll muster: $totalStaff Staff • Rate: ₹${dailyWage.toInt()}/day • Grand Total: ₹${String.format(Locale.ENGLISH, "%,d", grandSalary.toInt())}",
                    icon = Icons.Default.CurrencyRupee,
                    gradient = listOf(BrandOrangeAccent, Color(0xFFE65100)),
                    lang = lang,
                    isLoading = isGeneratingPdf,
                    onDownloadClick = {
                        isGeneratingPdf = true
                        viewModel.generateConfiguredPayrollPdf(context) { file ->
                            isGeneratingPdf = false
                            activePdfFile = file
                            activePdfTitle = "Monthly Salary & Wage Statement"
                            showPdfDialog = true
                            if (file != null) {
                                PdfReportGenerator.viewPdf(context, file)
                            }
                        }
                    },
                    onWhatsAppClick = {
                        isGeneratingPdf = true
                        viewModel.generateConfiguredPayrollPdf(context) { file ->
                            isGeneratingPdf = false
                            if (file != null) {
                                PdfReportGenerator.sharePdfToWhatsApp(context, file, "Monthly Salary & Wage Statement")
                            }
                        }
                    },
                    testTag = "card_monthly_salary_pdf"
                )
            }

            item {
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
            }

            // SECTION 2: DAILY WAGE CONFIGURATION
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
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
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(BrandGreenContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.CurrencyRupee, contentDescription = null, tint = BrandGreenSecondary)
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = Localization.get("daily_wage", lang),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                    Text(
                                        text = "Current rate: ₹${dailyWage.toInt()} per day",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Button(
                                onClick = { showWageDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = BrandGreenSecondary),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("change_wage_btn")
                            ) {
                                Text("Change Rate", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Quick preset rate buttons
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(300.0, 350.0, 400.0, 500.0).forEach { preset ->
                                val isSelected = dailyWage == preset
                                OutlinedButton(
                                    onClick = { viewModel.setDailyWage(preset) },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = if (isSelected) ButtonDefaults.outlinedButtonColors(containerColor = BrandGreenContainer) else ButtonDefaults.outlinedButtonColors(),
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text(
                                        text = "₹${preset.toInt()}",
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) BrandGreenSecondary else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // SECTION 3: THEME SETTINGS MENU ITEM
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToTheme() }
                        .testTag("menu_item_theme")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
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
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = Localization.get("theme", lang),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = MaterialTheme.colorScheme.primaryContainer
                                    ) {
                                        Text(
                                            text = when (theme) {
                                                "dark" -> "Dark Theme"
                                                "custom" -> "Custom Theme"
                                                else -> "Light Theme"
                                            },
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = Localization.get("theme_settings_desc", lang),
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Open Theme Settings",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            // SECTION 3.5: OFFICIAL BRAND LOGO & APP ICON SHOWCASE
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFD54F).copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showLogoDialog = true }
                        .testTag("card_brand_logo_showcase")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                SumitAttendanceLogo(
                                    size = 46.dp,
                                    showCurvedText = false
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "Official App Logo",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = Color(0xFFFFF8E1),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFD54F))
                                        ) {
                                            Text(
                                                text = "Ashoka Chakra Gold",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFFB78103),
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Circular Navy & 3D Metallic Gold • 1024×1024 PNG",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "View Logo",
                                tint = Color(0xFFB78103),
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { showLogoDialog = true },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Preview Emblem", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = {
                                    try {
                                        val bitmap = createSumitLogoBitmap(size = 1024, transparentBackground = true)
                                        val cacheDir = File(context.cacheDir, "branding").apply { mkdirs() }
                                        val file = File(cacheDir, "sumit_attendance_logo_1024x1024.png")
                                        java.io.FileOutputStream(file).use { out ->
                                            bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, out)
                                        }
                                        val uri = androidx.core.content.FileProvider.getUriForFile(
                                            context,
                                            "${context.packageName}.fileprovider",
                                            file
                                        )
                                        val intent = Intent(Intent.ACTION_SEND).apply {
                                            type = "image/png"
                                            putExtra(Intent.EXTRA_STREAM, uri)
                                            putExtra(Intent.EXTRA_SUBJECT, "Sumit Attendance System - Official Brand Logo")
                                            putExtra(Intent.EXTRA_TEXT, "Sumit Attendance System Official Circular Logo (Ashoka Chakra 24-Spokes, Metallic Gold 'S', Midnight Navy Blue background - 1024x1024 PNG)")
                                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                        }
                                        context.startActivity(Intent.createChooser(intent, "Export / Share Logo (1024x1024 PNG)"))
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Export error: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF0B192C),
                                    contentColor = Color(0xFFFFD54F)
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1.3f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FileDownload,
                                    contentDescription = null,
                                    tint = Color(0xFFFFD54F),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Export 1024×1024", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // SECTION 4: LANGUAGE SELECTION
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(BrandBlueContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Language, contentDescription = null, tint = BrandBluePrimary)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = Localization.get("language", lang),
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            LanguageOptionCard(
                                title = "मराठी (Marathi)",
                                isSelected = lang == "mr",
                                onClick = { viewModel.setLanguage("mr") },
                                modifier = Modifier.weight(1f)
                            )
                            LanguageOptionCard(
                                title = "हिंदी (Hindi)",
                                isSelected = lang == "hi",
                                onClick = { viewModel.setLanguage("hi") },
                                modifier = Modifier.weight(1f)
                            )
                            LanguageOptionCard(
                                title = "English",
                                isSelected = lang == "en",
                                onClick = { viewModel.setLanguage("en") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // SECTION 4: VOICE TTS FEEDBACK
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(BrandPurpleContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.RecordVoiceOver, contentDescription = null, tint = BrandPurpleAccent)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = Localization.get("voice_feedback", lang),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = "Speak Present/Absent on button tap",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { viewModel.testTts() }) {
                                Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Test voice", tint = BrandPurpleAccent)
                            }
                            Switch(
                                checked = ttsEnabled,
                                onCheckedChange = { viewModel.setTtsEnabled(it) },
                                colors = SwitchDefaults.colors(checkedThumbColor = BrandPurpleAccent)
                            )
                        }
                    }
                }
            }

            // SECTION 5: DATABASE BACKUP & LOCAL STORAGE EXPORT
            item {
                DatabaseBackupSection(
                    viewModel = viewModel,
                    lang = lang
                )
            }

            // SECTION 6: SHARE APPLICATION
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(BrandOrangeContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, tint = BrandOrangeAccent)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = Localization.get("share_app", lang),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = "Share app link with supervisors",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Button(
                            onClick = {
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(
                                        Intent.EXTRA_TEXT,
                                        "Install Sumit Attendance System for Sanitation Department Staff & Worker Attendance Management:\nhttps://play.google.com/store/apps/details?id=${context.packageName}"
                                    )
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Share App Link"))
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = BrandOrangeAccent),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("btn_share_app")
                        ) {
                            Text("Share Link", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // SECTION 7: ADMIN LOGOUT
            item {
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedButton(
                    onClick = {
                        viewModel.logout()
                        onLogout()
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusAbsent),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("admin_logout_btn")
                ) {
                    Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, tint = StatusAbsent)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = Localization.get("logout", lang),
                        fontWeight = FontWeight.Bold,
                        color = StatusAbsent
                    )
                }
            }
        }
    }

    // Daily Wage Change Dialog
    if (showWageDialog) {
        AlertDialog(
            onDismissRequest = { showWageDialog = false },
            title = { Text("Set Daily Wage Rate (₹)", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        text = "Enter base daily wage per worker for calculating 1.0 Day Present, 0.5 Half Day, and 2.0 Double Duty.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    OutlinedTextField(
                        value = customWageInput,
                        onValueChange = { customWageInput = it },
                        label = { Text("Daily Wage Amount (₹)") },
                        leadingIcon = { Icon(Icons.Default.CurrencyRupee, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amount = customWageInput.toDoubleOrNull()
                        if (amount != null && amount > 0) {
                            viewModel.setDailyWage(amount)
                        }
                        showWageDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandGreenSecondary)
                ) {
                    Text("Save Rate", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showWageDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // PDF Ready Dialog for Viewing & Sharing
    if (showPdfDialog) {
        PdfReadyDialog(
            file = activePdfFile,
            reportTitle = activePdfTitle,
            lang = lang,
            onDismiss = { showPdfDialog = false }
        )
    }

    // Official App Logo & Branding Modal Dialog
    if (showLogoDialog) {
        AlertDialog(
            onDismissRequest = { showLogoDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Assessment,
                        contentDescription = null,
                        tint = Color(0xFFD4AF37)
                    )
                    Text("Official App Logo & Emblem", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(190.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF070F1E))
                            .padding(6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        SumitAttendanceLogo(
                            size = 175.dp,
                            showCurvedText = true
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Sumit Attendance System",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Ashoka Chakra • Metallic Gold • Luxury Navy Blue",
                        fontSize = 11.5.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "Design Specifications:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.5.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("• 24-Spoke Ashoka Chakra concentric inner ring", fontSize = 10.5.sp)
                            Text("• 3D Metallic Gold luxury bevel letter 'S'", fontSize = 10.5.sp)
                            Text("• Dark Navy Blue circular gradient base (#0B192C)", fontSize = 10.5.sp)
                            Text("• Curved 'SUMIT ATTENDANCE SYSTEM' typography", fontSize = 10.5.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        try {
                            val bitmap = createSumitLogoBitmap(size = 1024, transparentBackground = true)
                            val cacheDir = File(context.cacheDir, "branding").apply { mkdirs() }
                            val file = File(cacheDir, "sumit_attendance_logo_1024x1024.png")
                            java.io.FileOutputStream(file).use { out ->
                                bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, out)
                            }
                            val uri = androidx.core.content.FileProvider.getUriForFile(
                                context,
                                "${context.packageName}.fileprovider",
                                file
                            )
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "image/png"
                                putExtra(Intent.EXTRA_STREAM, uri)
                                putExtra(Intent.EXTRA_SUBJECT, "Sumit Attendance System - Official Brand Logo")
                                putExtra(Intent.EXTRA_TEXT, "Sumit Attendance System Official Circular Logo (Ashoka Chakra 24-Spokes, Metallic Gold 'S', Midnight Navy Blue background - 1024x1024 PNG)")
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(Intent.createChooser(intent, "Share Official Logo"))
                        } catch (e: Exception) {
                            Toast.makeText(context, "Share error: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF0B192C),
                        contentColor = Color(0xFFFFD54F)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                        tint = Color(0xFFFFD54F),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Export & Share PNG", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
fun ReportCardItem(
    title: String,
    description: String,
    icon: ImageVector,
    gradient: List<Color>,
    lang: String,
    isLoading: Boolean,
    onDownloadClick: () -> Unit,
    onWhatsAppClick: () -> Unit,
    testTag: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.horizontalGradient(gradient))
                .padding(16.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.25f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                        Text(
                            text = description,
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.85f),
                            lineHeight = 14.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // The Two Explicit Action Buttons: Download PDF & Share on WhatsApp
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 1. Download PDF Button
                    Button(
                        onClick = onDownloadClick,
                        enabled = !isLoading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = Color(0xFF1E293B)
                        ),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                            .testTag("${testTag}_download_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.FileDownload,
                            contentDescription = null,
                            tint = Color(0xFFD32F2F),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = Localization.get("download_pdf", lang),
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }

                    // 2. Share on WhatsApp Button (WhatsApp Green)
                    Button(
                        onClick = onWhatsAppClick,
                        enabled = !isLoading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF25D366),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                        modifier = Modifier
                            .weight(1.25f)
                            .height(42.dp)
                            .testTag("${testTag}_whatsapp_btn")
                    ) {
                        com.example.ui.components.WhatsAppIcon(
                            tint = Color.White,
                            size = 18.dp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = Localization.get("share_pdf_on_whatsapp", lang),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LanguageOptionCard(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg = if (isSelected) BrandBluePrimary else MaterialTheme.colorScheme.surfaceVariant
    val textClr = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
    val border = if (isSelected) BrandBluePrimary else Color.Transparent

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(vertical = 10.dp, horizontal = 4.dp)
    ) {
        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = textClr,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}
