package com.example.ui.screens

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.BrandedAppTitle
import com.example.ui.components.DashboardStatBox
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
import com.example.ui.theme.StatusDoubleDuty
import com.example.ui.theme.StatusDoubleDutyBg
import com.example.ui.theme.StatusHalfDay
import com.example.ui.theme.StatusHalfDayBg
import com.example.ui.theme.StatusPresent
import com.example.ui.theme.StatusPresentBg
import com.example.util.Localization
import com.example.viewmodel.AttendanceViewModel

@Composable
fun HomeScreen(
    viewModel: AttendanceViewModel,
    onNavigateToAttendance: () -> Unit,
    onNavigateToMonthlySummary: () -> Unit,
    onNavigateToSalaryCalculation: () -> Unit,
    onNavigateToDailyPdf: () -> Unit,
    onNavigateToWorkers: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val lang by viewModel.appLanguage.collectAsState()
    val dailySummary by viewModel.dailySummary.collectAsState()
    val dailyWage by viewModel.dailyWage.collectAsState()
    val activeWorkers by viewModel.activeWorkers.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val selectedMonth by viewModel.selectedMonth.collectAsState()

    val context = LocalContext.current

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // Branded Top App Bar & Header
            item {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 3.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            BrandedAppTitle(
                                fontSize = 22,
                                showSubtitle = true,
                                subtitleText = Localization.get("department_title", lang),
                                showLogo = true,
                                logoSize = 38.dp
                            )

                            // Quick Audio test or settings indicator
                            IconButton(
                                onClick = { viewModel.testTts() },
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(BrandGreenContainer)
                                    .size(38.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                    contentDescription = "Voice test",
                                    tint = BrandGreenSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Daily Status Highlight Ribbon
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = BrandBlueContainer,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
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
                                        text = selectedDate,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = BrandBlueDark
                                    )
                                }

                                Text(
                                    text = "Daily Wage: ₹${dailyWage.toInt()}",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp,
                                    color = BrandBlueDark
                                )
                            }
                        }
                    }
                }
            }

            // Quick KPI Live Overview
            item {
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
                    Text(
                        text = "Today's Attendance Summary (आजचा हजेरी तक्ता)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DashboardStatBox(
                            title = Localization.get("present", lang),
                            value = "${dailySummary.presentCount}",
                            icon = Icons.Default.CheckCircle,
                            color = StatusPresent,
                            bgColor = StatusPresentBg,
                            modifier = Modifier.weight(1f)
                        )
                        DashboardStatBox(
                            title = Localization.get("absent", lang),
                            value = "${dailySummary.absentCount}",
                            icon = Icons.Default.AssignmentTurnedIn,
                            color = StatusAbsent,
                            bgColor = StatusAbsentBg,
                            modifier = Modifier.weight(1f)
                        )
                        DashboardStatBox(
                            title = Localization.get("half_day", lang),
                            value = "${dailySummary.halfDayCount}",
                            icon = Icons.Default.Speed,
                            color = StatusHalfDay,
                            bgColor = StatusHalfDayBg,
                            modifier = Modifier.weight(1f)
                        )
                        DashboardStatBox(
                            title = Localization.get("double_duty", lang),
                            value = "${dailySummary.doubleDutyCount}",
                            icon = Icons.Default.Badge,
                            color = StatusDoubleDuty,
                            bgColor = StatusDoubleDutyBg,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Main Operations Dashboard Action Cards
            item {
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Main Operations (मुख्य विभाग)",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // 1. Attendance Button Card (Blue / Cyan)
                    DashboardActionCard(
                        title = Localization.get("attendance", lang),
                        subtitle = "Mark & edit sanitation worker daily attendance with voice TTS",
                        icon = Icons.Default.AssignmentTurnedIn,
                        badge = "${dailySummary.presentCount}/${activeWorkers.size} Marked",
                        gradientColors = listOf(BrandBluePrimary, Color(0xFF0288D1)),
                        onClick = onNavigateToAttendance,
                        testTag = "home_attendance_card"
                    )

                    // 2. Monthly Historical Summary & Logs Card (Teal / Green)
                    DashboardActionCard(
                        title = Localization.get("monthly_summary", lang),
                        subtitle = "Select month & view historical muster roll, day-by-day logs ($selectedMonth)",
                        icon = Icons.Default.CalendarMonth,
                        badge = "Historical Logs",
                        gradientColors = listOf(Color(0xFF00897B), Color(0xFF004D40)),
                        onClick = onNavigateToMonthlySummary,
                        testTag = "home_monthly_history_card"
                    )

                    // 3. Configurable Salary Calculation Module Card (Gold / Amber / Green Gradient)
                    DashboardActionCard(
                        title = Localization.get("salary_calculation", lang),
                        subtitle = "Configurable wage rates, advance/bonus deductions, payslips & PDF payroll",
                        icon = Icons.Default.MonetizationOn,
                        badge = "Payroll & Payslips",
                        gradientColors = listOf(Color(0xFF2E7D32), Color(0xFF1B5E20)),
                        onClick = onNavigateToSalaryCalculation,
                        testTag = "home_salary_calculation_card"
                    )

                    // 4. Daily Attendance PDF Button Card (Green)
                    DashboardActionCard(
                        title = Localization.get("daily_attendance_pdf", lang),
                        subtitle = "Generate colorful A4 PDF without salary, save & share on WhatsApp",
                        icon = Icons.Default.PictureAsPdf,
                        badge = "A4 PDF • No Salary",
                        gradientColors = listOf(BrandGreenSecondary, Color(0xFF00897B)),
                        onClick = onNavigateToDailyPdf,
                        testTag = "home_daily_pdf_card"
                    )

                    // 5. Workers Button Card (Orange / Amber)
                    DashboardActionCard(
                        title = Localization.get("workers", lang),
                        subtitle = "Add, edit, view profiles & sanitation worker photos (${activeWorkers.size} active)",
                        icon = Icons.Default.Groups,
                        badge = "${activeWorkers.size} Workers",
                        gradientColors = listOf(BrandOrangeAccent, Color(0xFFF57C00)),
                        onClick = onNavigateToWorkers,
                        testTag = "home_workers_card"
                    )

                    // 6. Settings & Reports Button Card (Purple)
                    DashboardActionCard(
                        title = Localization.get("settings_and_reports", lang),
                        subtitle = "Monthly Salary PDF, Attendance Master, DB Backup (JSON / CSV) & Settings",
                        icon = Icons.Default.Settings,
                        badge = "Settings & Reports",
                        gradientColors = listOf(BrandPurpleAccent, Color(0xFF6A1B9A)),
                        onClick = onNavigateToSettings,
                        testTag = "home_settings_card"
                    )
                }
            }

            // Cloud Status & Quick Summary Banner
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(BrandGreenContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CloudDone,
                                    contentDescription = null,
                                    tint = BrandGreenSecondary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Cloud Database Synchronized",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Permanent attendance history safe",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    badge: String,
    gradientColors: List<Color>,
    onClick: () -> Unit,
    testTag: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag(testTag),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.horizontalGradient(gradientColors))
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.22f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = title,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = subtitle,
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.88f),
                            lineHeight = 16.sp
                        )
                    }
                }

                // Badge pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.25f))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = badge,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}
