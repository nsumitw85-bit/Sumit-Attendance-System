package com.example.ui.screens

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MonthlyDayLog
import com.example.data.MonthlyHistoricalSummary
import com.example.data.WorkerMonthlyStat
import com.example.ui.components.BrandedAppTitle
import com.example.ui.components.MonthNavigatorBar
import com.example.ui.components.WhatsAppIcon
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
import com.example.ui.theme.StatusAbsentBg
import com.example.ui.theme.StatusAbsentBorder
import com.example.ui.theme.StatusDoubleDuty
import com.example.ui.theme.StatusDoubleDutyBg
import com.example.ui.theme.StatusDoubleDutyBorder
import com.example.ui.theme.StatusHalfDay
import com.example.ui.theme.StatusHalfDayBg
import com.example.ui.theme.StatusHalfDayBorder
import com.example.ui.theme.StatusPresent
import com.example.ui.theme.StatusPresentBg
import com.example.ui.theme.StatusPresentBorder
import com.example.util.Localization
import com.example.util.PdfReportGenerator
import com.example.viewmodel.AttendanceViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthlyHistoryScreen(
    viewModel: AttendanceViewModel,
    onBack: () -> Unit,
    onNavigateToDateAttendance: (String) -> Unit
) {
    val lang by viewModel.appLanguage.collectAsState()
    val selectedMonth by viewModel.selectedMonth.collectAsState()
    val monthlyStats by viewModel.monthlyStats.collectAsState()
    val monthlyDayLogs by viewModel.monthlyDayLogs.collectAsState()
    val overallSummary by viewModel.monthlyOverallSummary.collectAsState()
    val dailyWage by viewModel.dailyWage.collectAsState()

    var selectedTabIndex by remember { mutableIntStateOf(0) } // 0: Day-by-Day Logs, 1: Worker Muster Roll
    var searchQuery by remember { mutableStateOf("") }
    var showMonthPicker by remember { mutableStateOf(false) }

    // PDF Preview & WhatsApp share state
    val context = LocalContext.current
    var activePdfFile by remember { mutableStateOf<File?>(null) }
    var activePdfTitle by remember { mutableStateOf("") }
    var showPdfDialog by remember { mutableStateOf(false) }
    var isGeneratingPdf by remember { mutableStateOf(false) }

    val filteredWorkerStats = remember(monthlyStats, searchQuery) {
        if (searchQuery.isBlank()) {
            monthlyStats
        } else {
            monthlyStats.filter {
                it.worker.name.contains(searchQuery, ignoreCase = true) ||
                it.worker.workerCode.contains(searchQuery, ignoreCase = true) ||
                it.worker.roleCategory.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    BrandedAppTitle(
                        fontSize = 18,
                        showSubtitle = true,
                        subtitleText = "Monthly Logs • मासिक हजेरी अहवाल"
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
                actions = {
                    // Export Master PDF Button
                    IconButton(
                        onClick = {
                            isGeneratingPdf = true
                            viewModel.generateMonthlyAttendancePdf(context) { file ->
                                isGeneratingPdf = false
                                activePdfFile = file
                                activePdfTitle = "Monthly Attendance Master Sheet"
                                showPdfDialog = true
                            }
                        },
                        modifier = Modifier.testTag("monthly_history_export_pdf_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PictureAsPdf,
                            contentDescription = "Monthly PDF",
                            tint = Color(0xFFD32F2F)
                        )
                    }

                    // Direct WhatsApp Share Button
                    IconButton(
                        onClick = {
                            viewModel.generateMonthlyAttendancePdf(context) { file ->
                                if (file != null) {
                                    PdfReportGenerator.sharePdfToWhatsApp(
                                        context,
                                        file,
                                        "Monthly Attendance Report ($selectedMonth)"
                                    )
                                }
                            }
                        },
                        modifier = Modifier.testTag("monthly_history_whatsapp_btn")
                    ) {
                        WhatsAppIcon(tint = Color(0xFF25D366), size = 22.dp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Month Selector Bar (Previous Month, Month Title, Next Month, Picker)
            MonthNavigatorBar(
                currentMonth = selectedMonth,
                onPreviousMonth = { viewModel.shiftMonth(-1) },
                onNextMonth = { viewModel.shiftMonth(1) },
                onPickMonth = { showMonthPicker = true },
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )

            // Monthly Historical Summary KPI Banner
            MonthlyHistoricalKpiBanner(
                summary = overallSummary,
                dailyWage = dailyWage,
                lang = lang,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            )

            // Navigation Tabs: Day-by-Day Logs vs. Worker Muster Roll
            PrimaryTabRow(
                selectedTabIndex = selectedTabIndex,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 2.dp)
            ) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = Localization.get("day_wise_breakdown", lang) + " (${monthlyDayLogs.size})",
                                fontSize = 12.sp,
                                fontWeight = if (selectedTabIndex == 0) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    },
                    modifier = Modifier.testTag("tab_day_wise_logs")
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Groups, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = Localization.get("worker_wise_summary", lang) + " (${monthlyStats.size})",
                                fontSize = 12.sp,
                                fontWeight = if (selectedTabIndex == 1) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    },
                    modifier = Modifier.testTag("tab_worker_muster_roll")
                )
            }

            // Tab Content
            when (selectedTabIndex) {
                0 -> {
                    // Tab 1: Day-by-Day Historical Logs List
                    DayByDayLogsTabContent(
                        dayLogs = monthlyDayLogs,
                        totalWorkers = overallSummary.totalActiveWorkers,
                        dailyWage = dailyWage,
                        lang = lang,
                        onOpenDayAttendance = { date ->
                            viewModel.setSelectedDate(date)
                            onNavigateToDateAttendance(date)
                        }
                    )
                }
                1 -> {
                    // Tab 2: Worker-by-Worker Muster Roll Summary
                    WorkerMusterRollTabContent(
                        workerStats = filteredWorkerStats,
                        searchQuery = searchQuery,
                        onSearchChange = { searchQuery = it },
                        dailyWage = dailyWage,
                        lang = lang
                    )
                }
            }
        }
    }

    // Interactive Month / Year Picker Dialog
    if (showMonthPicker) {
        MonthYearPickerDialog(
            initialMonth = selectedMonth,
            lang = lang,
            onDismiss = { showMonthPicker = false },
            onMonthSelected = { newYearMonth ->
                viewModel.setSelectedMonth(newYearMonth)
                showMonthPicker = false
            }
        )
    }

    // PDF Preview & WhatsApp dialog
    if (showPdfDialog && activePdfFile != null) {
        PdfReadyDialog(
            file = activePdfFile,
            reportTitle = activePdfTitle,
            lang = lang,
            onDismiss = { showPdfDialog = false }
        )
    }
}

/**
 * Top KPI Summary Banner for the Selected Month.
 */
@Composable
fun MonthlyHistoricalKpiBanner(
    summary: MonthlyHistoricalSummary,
    dailyWage: Double,
    lang: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.testTag("monthly_historical_kpi_banner"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = BrandBlueContainer.copy(alpha = 0.45f)
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            BrandBluePrimary.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Header Row: Month title & Avg attendance badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = BrandBlueDark,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Historical Month Summary (${summary.month})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = BrandBlueDark
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (summary.averageAttendanceRate >= 75) BrandGreenContainer else BrandOrangeContainer
                ) {
                    Text(
                        text = "Avg: ${String.format(Locale.ENGLISH, "%.1f", summary.averageAttendanceRate)}%",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (summary.averageAttendanceRate >= 75) BrandGreenSecondary else BrandOrangeAccent,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Primary 3 Metrics: Days Logged | Net Man-Days | Total Payroll
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 1. Days Logged
                HistoricalMetricBox(
                    title = Localization.get("total_days_recorded", lang),
                    value = "${summary.totalLoggedDays} Days",
                    color = BrandBluePrimary,
                    bgColor = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.weight(1f)
                )

                // 2. Net Man-Days
                HistoricalMetricBox(
                    title = Localization.get("net_man_days", lang),
                    value = "${String.format(Locale.ENGLISH, "%.1f", summary.totalNetManDays)} d",
                    color = BrandOrangeAccent,
                    bgColor = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.weight(1f)
                )

                // 3. Grand Wages
                HistoricalMetricBox(
                    title = Localization.get("grand_total", lang),
                    value = "₹" + String.format(Locale.ENGLISH, "%,d", summary.grandSalary.toInt()),
                    color = BrandGreenSecondary,
                    bgColor = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.weight(1.2f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Secondary Status Counts Strip: P | A | H | D
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Total Shifts:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MiniCountTag("P (Present)", "${summary.totalPresent}", StatusPresent)
                    MiniCountTag("A (Absent)", "${summary.totalAbsent}", StatusAbsent)
                    MiniCountTag("H (Half-Day)", "${summary.totalHalfDay}", StatusHalfDay)
                    MiniCountTag("D (Double)", "${summary.totalDoubleDuty}", StatusDoubleDuty)
                }
            }
        }
    }
}

@Composable
fun HistoricalMetricBox(
    title: String,
    value: String,
    color: Color,
    bgColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = bgColor,
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.25f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                color = color,
                maxLines = 1
            )
            Text(
                text = title,
                fontSize = 9.5.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun MiniCountTag(label: String, count: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(3.dp))
        Text(
            text = "$count",
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

/**
 * Tab 1: Day-by-Day Historical Logs List
 */
@Composable
fun DayByDayLogsTabContent(
    dayLogs: List<MonthlyDayLog>,
    totalWorkers: Int,
    dailyWage: Double,
    lang: String,
    onOpenDayAttendance: (String) -> Unit
) {
    if (dayLogs.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "No attendance logs found for this month.\n(या महिन्यासाठी कोणत्याही हजेरी नोंदी आढळल्या नाहीत)",
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(items = dayLogs, key = { it.date }) { log ->
                DayLogItemCard(
                    log = log,
                    totalWorkers = totalWorkers,
                    lang = lang,
                    onClick = { onOpenDayAttendance(log.date) }
                )
            }
        }
    }
}

@Composable
fun DayLogItemCard(
    log: MonthlyDayLog,
    totalWorkers: Int,
    lang: String,
    onClick: () -> Unit
) {
    val dayOfWeek = try {
        val parsed = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(log.date) ?: Date()
        SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault()).format(parsed)
    } catch (e: Exception) {
        log.date
    }

    val attendanceRatio = if (log.totalRecords > 0) {
        (log.presentCount.toFloat() / log.totalRecords.toFloat()).coerceIn(0f, 1f)
    } else {
        0f
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("day_log_card_${log.date}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Row 1: Date & Day + Daily Wage + Edit / View Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = BrandBlueContainer,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            val dayNum = try {
                                log.date.split("-").last()
                            } catch (e: Exception) {
                                ""
                            }
                            Text(
                                text = dayNum,
                                fontWeight = FontWeight.Black,
                                fontSize = 15.sp,
                                color = BrandBlueDark
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = dayOfWeek,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${log.totalRecords} Workers Marked (${String.format(Locale.ENGLISH, "%.1f", log.netWorkingDays)} Man-Days)",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Daily Wage Tag & Edit Action
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "₹" + String.format(Locale.ENGLISH, "%,d", log.estimatedSalary.toInt()),
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp,
                        color = BrandGreenSecondary
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.EditCalendar,
                            contentDescription = "Edit Day",
                            tint = BrandBluePrimary,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "View/Edit",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = BrandBluePrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Progress bar showing Present Attendance
            LinearProgressIndicator(
                progress = { attendanceRatio },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = StatusPresent,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Row 2: Detailed Badges for P, A, H, D
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatusMiniBadge("P: ${log.presentCount}", StatusPresent, StatusPresentBg, StatusPresentBorder, Modifier.weight(1f))
                StatusMiniBadge("A: ${log.absentCount}", StatusAbsent, StatusAbsentBg, StatusAbsentBorder, Modifier.weight(1f))
                StatusMiniBadge("H: ${log.halfDayCount}", StatusHalfDay, StatusHalfDayBg, StatusHalfDayBorder, Modifier.weight(1f))
                StatusMiniBadge("D: ${log.doubleDutyCount}", StatusDoubleDuty, StatusDoubleDutyBg, StatusDoubleDutyBorder, Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun StatusMiniBadge(
    text: String,
    color: Color,
    bgColor: Color,
    borderColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = bgColor,
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
        modifier = modifier
    ) {
        Text(
            text = text,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = color,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 3.dp)
        )
    }
}

/**
 * Tab 2: Worker-by-Worker Muster Roll Summary
 */
@Composable
fun WorkerMusterRollTabContent(
    workerStats: List<WorkerMonthlyStat>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    dailyWage: Double,
    lang: String
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Search Worker Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            placeholder = { Text(Localization.get("search_worker", lang), fontSize = 12.sp) },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp))
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchChange("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = BrandBluePrimary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp)
                .height(48.dp)
                .testTag("monthly_worker_search_input")
        )

        if (workerStats.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No workers found matching '$searchQuery'",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(items = workerStats, key = { it.worker.id }) { stat ->
                    WorkerMonthlyStatCard(
                        stat = stat,
                        lang = lang
                    )
                }
            }
        }
    }
}

@Composable
fun WorkerMonthlyStatCard(
    stat: WorkerMonthlyStat,
    lang: String
) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded }
            .testTag("worker_stat_card_${stat.worker.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Row 1: Code, Name, Role & Salary
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(BrandBlueContainer)
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = stat.worker.workerCode,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrandBluePrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = stat.worker.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = Localization.getRoleName(stat.worker.roleCategory, lang),
                            fontSize = 10.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Total Salary & Calculated Working Days
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "₹" + String.format(Locale.ENGLISH, "%,d", stat.finalSalary.toInt()),
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp,
                        color = BrandGreenSecondary
                    )
                    Text(
                        text = "${String.format(Locale.ENGLISH, "%.1f", stat.calculatedWorkingDays)} Days",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandBlueDark
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Row 2: Status Breakdown Pills
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatusMiniBadge("P: ${stat.presentCount}", StatusPresent, StatusPresentBg, StatusPresentBorder, Modifier.weight(1f))
                StatusMiniBadge("A: ${stat.absentCount}", StatusAbsent, StatusAbsentBg, StatusAbsentBorder, Modifier.weight(1f))
                StatusMiniBadge("H: ${stat.halfDayCount}", StatusHalfDay, StatusHalfDayBg, StatusHalfDayBorder, Modifier.weight(1f))
                StatusMiniBadge("D: ${stat.doubleDutyCount}", StatusDoubleDuty, StatusDoubleDutyBg, StatusDoubleDutyBorder, Modifier.weight(1f))
            }

            // Expandable details (Salary breakdown formula & mobile info)
            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                ) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                        thickness = 1.dp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Mobile: ${stat.worker.phone.ifBlank { "Not provided" }}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Rate: ₹${stat.dailyWage.toInt()}/day",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Formula: (${stat.presentCount} × 1.0) + (${stat.halfDayCount} × 0.5) + (${stat.doubleDutyCount} × 2.0) = ${stat.calculatedWorkingDays} Days × ₹${stat.dailyWage.toInt()} = ₹${stat.finalSalary.toInt()}",
                        fontSize = 10.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

/**
 * Month & Year Picker Dialog
 */
@Composable
fun MonthYearPickerDialog(
    initialMonth: String, // "yyyy-MM"
    lang: String,
    onDismiss: () -> Unit,
    onMonthSelected: (String) -> Unit
) {
    val initialYear = try {
        initialMonth.split("-")[0].toInt()
    } catch (e: Exception) {
        Calendar.getInstance().get(Calendar.YEAR)
    }

    val initialMonthIndex = try {
        initialMonth.split("-")[1].toInt() - 1
    } catch (e: Exception) {
        Calendar.getInstance().get(Calendar.MONTH)
    }

    var selectedYear by remember { mutableIntStateOf(initialYear) }
    var selectedMonthIndex by remember { mutableIntStateOf(initialMonthIndex) }

    val monthNames = listOf(
        "Jan" to "जानेवारी",
        "Feb" to "फेब्रुवारी",
        "Mar" to "मार्च",
        "Apr" to "एप्रिल",
        "May" to "मे",
        "Jun" to "जून",
        "Jul" to "जुलै",
        "Aug" to "ऑगस्ट",
        "Sep" to "सप्टेंबर",
        "Oct" to "ऑक्टोबर",
        "Nov" to "नोव्हेंबर",
        "Dec" to "डिसेंबर"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = Localization.get("select_month", lang),
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Year Selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { selectedYear-- }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Prev Year")
                    }
                    Text(
                        text = "$selectedYear",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = BrandBluePrimary
                    )
                    IconButton(onClick = { selectedYear++ }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next Year")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 3x4 Month Grid
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    for (row in 0 until 4) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            for (col in 0 until 3) {
                                val monthIdx = row * 3 + col
                                val (enName, mrName) = monthNames[monthIdx]
                                val isSelected = selectedMonthIndex == monthIdx

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSelected) BrandBluePrimary else MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { selectedMonthIndex = monthIdx }
                                ) {
                                    Column(
                                        modifier = Modifier.padding(vertical = 8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = if (lang == "mr") mrName else enName,
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val formattedMonth = String.format(Locale.ENGLISH, "%04d-%02d", selectedYear, selectedMonthIndex + 1)
                    onMonthSelected(formattedMonth)
                },
                colors = ButtonDefaults.buttonColors(containerColor = BrandBluePrimary)
            ) {
                Text("Select", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
