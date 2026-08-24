package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PayrollMonthSummary
import com.example.data.RateMode
import com.example.data.SalaryWageConfig
import com.example.data.WorkerPayrollAdjustment
import com.example.data.WorkerSalaryComputation
import com.example.ui.theme.BrandBluePrimary
import com.example.ui.theme.BrandGreenSecondary
import com.example.ui.theme.BrandOrangeAccent
import com.example.ui.theme.BrandPurpleAccent
import com.example.ui.theme.StatusAbsent
import com.example.ui.theme.StatusAbsentBg
import com.example.ui.theme.StatusDoubleDuty
import com.example.ui.theme.StatusDoubleDutyBg
import com.example.ui.theme.StatusHalfDay
import com.example.ui.theme.StatusHalfDayBg
import com.example.ui.theme.StatusPresent
import com.example.ui.theme.StatusPresentBg
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
fun SalaryCalculationScreen(
    viewModel: AttendanceViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val lang by viewModel.appLanguage.collectAsState()
    val selectedMonth by viewModel.selectedMonth.collectAsState()
    val salaryConfig by viewModel.salaryConfig.collectAsState()
    val salaryComputations by viewModel.workerSalaryComputations.collectAsState()
    val payrollSummary by viewModel.payrollMonthSummary.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("ALL") } // ALL, PENDING, PAID
    var isConfigExpanded by remember { mutableStateOf(false) }

    // Dialog States
    var showMonthPicker by remember { mutableStateOf(false) }
    var workerToAdjust by remember { mutableStateOf<WorkerSalaryComputation?>(null) }
    var workerForPayslip by remember { mutableStateOf<WorkerSalaryComputation?>(null) }
    var generatedPdfFile by remember { mutableStateOf<File?>(null) }
    var pdfDialogTitle by remember { mutableStateOf("") }
    var showMarkAllPaidDialog by remember { mutableStateOf(false) }

    // Filtered worker computations
    val filteredComputations = remember(salaryComputations, searchQuery, selectedFilter) {
        salaryComputations.filter { comp ->
            val matchesSearch = searchQuery.isBlank() ||
                    comp.worker.name.contains(searchQuery, ignoreCase = true) ||
                    comp.worker.workerCode.contains(searchQuery, ignoreCase = true) ||
                    comp.worker.roleCategory.contains(searchQuery, ignoreCase = true)

            val matchesFilter = when (selectedFilter) {
                "PAID" -> comp.paymentStatus.startsWith("PAID")
                "PENDING" -> !comp.paymentStatus.startsWith("PAID")
                else -> true
            }

            matchesSearch && matchesFilter
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = Localization.get("salary_calculation", lang),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Muster: ${formatMonthDisplay(selectedMonth, lang)}",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("salary_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    // PDF Paysheet Action
                    IconButton(
                        onClick = {
                            viewModel.generateConfiguredPayrollPdf(context) { file ->
                                generatedPdfFile = file
                                pdfDialogTitle = "Monthly Payroll Sheet - $selectedMonth"
                            }
                        },
                        modifier = Modifier.testTag("salary_export_pdf_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PictureAsPdf,
                            contentDescription = "Export PDF",
                            tint = Color.White
                        )
                    }

                    // Month Selector Action
                    IconButton(
                        onClick = { showMonthPicker = true },
                        modifier = Modifier.testTag("salary_month_picker_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = "Select Month",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BrandBluePrimary
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Month Quick Navigation Bar
            item {
                MonthQuickNavBar(
                    selectedMonth = selectedMonth,
                    lang = lang,
                    onPrev = { viewModel.shiftMonth(-1) },
                    onNext = { viewModel.shiftMonth(1) },
                    onSelectMonth = { showMonthPicker = true }
                )
            }

            // 2. Executive Payroll KPI Dashboard Card
            item {
                ExecutivePayrollKpiCard(
                    summary = payrollSummary,
                    lang = lang,
                    onMarkAllPaidClick = { showMarkAllPaidDialog = true },
                    onGeneratePdf = {
                        viewModel.generateConfiguredPayrollPdf(context) { file ->
                            generatedPdfFile = file
                            pdfDialogTitle = "Monthly Payroll Sheet - $selectedMonth"
                        }
                    }
                )
            }

            // 3. Configurable Wage Rates & Rules Panel (Expandable)
            item {
                WageRateConfigCard(
                    config = salaryConfig,
                    lang = lang,
                    isExpanded = isConfigExpanded,
                    onToggleExpand = { isConfigExpanded = !isConfigExpanded },
                    onSaveConfig = { newConfig ->
                        viewModel.updateSalaryWageConfig(newConfig)
                    }
                )
            }

            // 4. Search and Filter Bar
            item {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("salary_search_input"),
                        placeholder = { Text("Search worker name, code, or role...") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotBlank()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(imageVector = Icons.Default.Close, contentDescription = "Clear")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BrandBluePrimary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                        )
                    )

                    // Status Filter Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = selectedFilter == "ALL",
                            onClick = { selectedFilter = "ALL" },
                            label = { Text("All (${salaryComputations.size})") },
                            leadingIcon = {
                                if (selectedFilter == "ALL") {
                                    Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                }
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = BrandBluePrimary.copy(alpha = 0.15f),
                                selectedLabelColor = BrandBluePrimary
                            ),
                            modifier = Modifier.testTag("salary_filter_all")
                        )

                        FilterChip(
                            selected = selectedFilter == "PENDING",
                            onClick = { selectedFilter = "PENDING" },
                            label = { Text("Pending (${payrollSummary.pendingCount})") },
                            leadingIcon = {
                                if (selectedFilter == "PENDING") {
                                    Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                }
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = StatusHalfDayBg,
                                selectedLabelColor = StatusHalfDay
                            ),
                            modifier = Modifier.testTag("salary_filter_pending")
                        )

                        FilterChip(
                            selected = selectedFilter == "PAID",
                            onClick = { selectedFilter = "PAID" },
                            label = { Text("Paid (${payrollSummary.paidCount})") },
                            leadingIcon = {
                                if (selectedFilter == "PAID") {
                                    Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                }
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = StatusPresentBg,
                                selectedLabelColor = StatusPresent
                            ),
                            modifier = Modifier.testTag("salary_filter_paid")
                        )
                    }
                }
            }

            // 5. Worker Salary Cards List Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Worker Payroll Muster (${filteredComputations.size})",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Net: ₹${formatCurrency(filteredComputations.sumOf { it.netPayableSalary })}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = BrandGreenSecondary
                    )
                }
            }

            // Worker Salary Items
            if (filteredComputations.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Payments,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(44.dp)
                            )
                            Text(
                                text = "No salary records found",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Try adjusting your search query or ensure workers are active.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(filteredComputations, key = { it.worker.id }) { comp ->
                    WorkerSalaryCard(
                        comp = comp,
                        lang = lang,
                        onEditAdjustments = { workerToAdjust = comp },
                        onViewPayslip = { workerForPayslip = comp },
                        onTogglePaid = {
                            val newStatus = if (comp.paymentStatus.startsWith("PAID")) "PENDING" else "PAID_CASH"
                            viewModel.markWorkerPaymentStatus(comp.worker.id, selectedMonth, newStatus)
                        }
                    )
                }
            }
        }
    }

    // 1. Month Picker Dialog
    if (showMonthPicker) {
        MonthSelectorDialog(
            currentMonth = selectedMonth,
            onMonthSelected = { month ->
                viewModel.setSelectedMonth(month)
                showMonthPicker = false
            },
            onDismiss = { showMonthPicker = false }
        )
    }

    // 2. Worker Adjustments Dialog (Advance & Bonus)
    if (workerToAdjust != null) {
        val comp = workerToAdjust!!
        WorkerAdjustmentDialog(
            comp = comp,
            month = selectedMonth,
            lang = lang,
            onSave = { adv, bonus, status, notes, customWage ->
                viewModel.updateWorkerAdjustment(
                    workerId = comp.worker.id,
                    month = selectedMonth,
                    advance = adv,
                    bonus = bonus,
                    status = status,
                    notes = notes,
                    customWage = customWage
                )
                workerToAdjust = null
            },
            onDismiss = { workerToAdjust = null }
        )
    }

    // 3. Worker Individual Payslip Preview Dialog
    if (workerForPayslip != null) {
        val comp = workerForPayslip!!
        WorkerPayslipPreviewDialog(
            comp = comp,
            config = salaryConfig,
            month = selectedMonth,
            lang = lang,
            onSendWhatsApp = {
                viewModel.generateWorkerSalarySlipPdf(context, comp) { file ->
                    if (file != null) {
                        PdfReportGenerator.sharePdfToWhatsApp(
                            context = context,
                            file = file,
                            title = "Salary Slip - ${comp.worker.name} ($selectedMonth)"
                        )
                    }
                }
            },
            onExportPdf = {
                viewModel.generateWorkerSalarySlipPdf(context, comp) { file ->
                    generatedPdfFile = file
                    pdfDialogTitle = "Salary Slip - ${comp.worker.name} ($selectedMonth)"
                }
            },
            onDismiss = { workerForPayslip = null }
        )
    }

    // 4. Mark All Workers Paid Confirmation Dialog
    if (showMarkAllPaidDialog) {
        AlertDialog(
            onDismissRequest = { showMarkAllPaidDialog = false },
            title = {
                Text(
                    text = "Bulk Payment Confirmation",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text("Do you want to mark all ${payrollSummary.totalWorkers} workers as 'PAID (CASH)' for the month of $selectedMonth?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.markAllWorkersPaid(selectedMonth, "PAID_CASH")
                        showMarkAllPaidDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandGreenSecondary)
                ) {
                    Text("Mark All Paid")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showMarkAllPaidDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // 5. PDF Ready & Share Dialog
    if (generatedPdfFile != null) {
        PdfReadyDialog(
            file = generatedPdfFile,
            reportTitle = pdfDialogTitle,
            lang = lang,
            onDismiss = { generatedPdfFile = null }
        )
    }
}

// -------------------------------------------------------------------------
// Subcomponents
// -------------------------------------------------------------------------

@Composable
private fun MonthQuickNavBar(
    selectedMonth: String,
    lang: String,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    onSelectMonth: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = onPrev,
                modifier = Modifier.size(36.dp).testTag("salary_prev_month_btn")
            ) {
                Icon(Icons.Default.ChevronLeft, contentDescription = "Previous Month")
            }

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onSelectMonth() }
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = null,
                    tint = BrandBluePrimary,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = formatMonthDisplay(selectedMonth, lang),
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Icon(
                    imageVector = Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
            }

            IconButton(
                onClick = onNext,
                modifier = Modifier.size(36.dp).testTag("salary_next_month_btn")
            ) {
                Icon(Icons.Default.ChevronRight, contentDescription = "Next Month")
            }
        }
    }
}

@Composable
private fun ExecutivePayrollKpiCard(
    summary: PayrollMonthSummary,
    lang: String,
    onMarkAllPaidClick: () -> Unit,
    onGeneratePdf: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF0D47A1),
                            Color(0xFF1565C0),
                            Color(0xFF00897B)
                        )
                    )
                )
                .padding(18.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = Localization.get("net_salary", lang).uppercase(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.8f),
                            letterSpacing = 0.8.sp
                        )
                        Text(
                            text = "₹${formatCurrency(summary.totalNetPayable)}",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFE8F5E9)
                        )
                    }

                    // Quick PDF Button
                    OutlinedButton(
                        onClick = onGeneratePdf,
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.White.copy(alpha = 0.15f),
                            contentColor = Color.White
                        ),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = Brush.horizontalGradient(listOf(Color.White.copy(alpha = 0.4f), Color.White.copy(alpha = 0.4f)))
                        ),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("salary_kpi_pdf_btn")
                    ) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Paysheet PDF", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                HorizontalDivider(color = Color.White.copy(alpha = 0.2f), thickness = 0.8.dp)

                // 4 Sub-KPIs Grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // 1. Gross Earnings
                    KpiStatCell(
                        label = Localization.get("gross_salary", lang),
                        value = "₹${formatCurrency(summary.totalGrossSalary)}",
                        color = Color.White
                    )

                    // 2. Advances (-)
                    KpiStatCell(
                        label = "Advance (-)",
                        value = "₹${formatCurrency(summary.totalAdvances)}",
                        color = Color(0xFFFFCDD2)
                    )

                    // 3. Bonus (+)
                    KpiStatCell(
                        label = "Bonus (+)",
                        value = "₹${formatCurrency(summary.totalBonuses)}",
                        color = Color(0xFFC8E6C9)
                    )

                    // 4. Man-Days
                    KpiStatCell(
                        label = "Total Days",
                        value = String.format(Locale.ENGLISH, "%.1fd", summary.totalManDays),
                        color = Color.White
                    )
                }

                // Disbursal Progress & Quick Action Bar
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color.Black.copy(alpha = 0.2f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (summary.pendingCount == 0) Color(0xFF4CAF50) else Color(0xFFFFB74D))
                            )
                            Text(
                                text = "Paid: ${summary.paidCount}  |  Pending: ${summary.pendingCount}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White
                            )
                        }

                        if (summary.pendingCount > 0) {
                            TextButton(
                                onClick = onMarkAllPaidClick,
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                                modifier = Modifier.height(26.dp)
                            ) {
                                Text(
                                    text = "Mark All Paid",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFFF59D)
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
private fun KpiStatCell(
    label: String,
    value: String,
    color: Color
) {
    Column {
        Text(
            text = label,
            fontSize = 10.sp,
            color = Color.White.copy(alpha = 0.75f)
        )
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
private fun WageRateConfigCard(
    config: SalaryWageConfig,
    lang: String,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onSaveConfig: (SalaryWageConfig) -> Unit
) {
    var baseWageText by remember(config.baseDailyWage) { mutableStateOf(config.baseDailyWage.toInt().toString()) }
    var halfMode by remember(config.halfDayMode) { mutableStateOf(config.halfDayMode) }
    var halfMultiplier by remember(config.halfDayMultiplier) { mutableStateOf(config.halfDayMultiplier) }
    var halfFixedText by remember(config.halfDayFixedRate) { mutableStateOf(config.halfDayFixedRate.toInt().toString()) }

    var doubleMode by remember(config.doubleDutyMode) { mutableStateOf(config.doubleDutyMode) }
    var doubleMultiplier by remember(config.doubleDutyMultiplier) { mutableStateOf(config.doubleDutyMultiplier) }
    var doubleFixedText by remember(config.doubleDutyFixedRate) { mutableStateOf(config.doubleDutyFixedRate.toInt().toString()) }

    var useRoleRates by remember(config.useRoleBasedRates) { mutableStateOf(config.useRoleBasedRates) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleExpand() },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(BrandOrangeAccent.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            tint = BrandOrangeAccent,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Column {
                        Text(
                            text = Localization.get("wage_rate_config", lang),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        val halfDisplay = if (config.halfDayMode == RateMode.MULTIPLIER)
                            "${(config.halfDayMultiplier * 100).toInt()}% (₹${config.calculateHalfDayRate("").toInt()})"
                        else "₹${config.halfDayFixedRate.toInt()} Fixed"

                        Text(
                            text = "1 Day: ₹${config.baseDailyWage.toInt()} | ½ Day: $halfDisplay | Double: ₹${config.calculateDoubleDutyRate("").toInt()}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(onClick = onToggleExpand) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Toggle Rates Settings"
                    )
                }
            }

            // Expandable Editor Body
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    // 1. Base Full-Day Wage
                    Text(
                        text = "1. " + Localization.get("base_daily_wage", lang),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    OutlinedTextField(
                        value = baseWageText,
                        onValueChange = { baseWageText = it.filter { ch -> ch.isDigit() } },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("salary_base_wage_input"),
                        leadingIcon = { Text("₹", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        label = { Text("Base Full Day Daily Wage (₹ / Day)") }
                    )

                    // Quick Wage Presets
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(300, 350, 400, 450, 500).forEach { preset ->
                            OutlinedButton(
                                onClick = { baseWageText = preset.toString() },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f).height(32.dp)
                            ) {
                                Text("₹$preset", fontSize = 11.sp)
                            }
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    // 2. Half-Day Wage Rate Setting
                    Text(
                        text = "2. " + Localization.get("half_day_rate", lang),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Multiplier Option
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { halfMode = RateMode.MULTIPLIER }
                                .border(
                                    width = if (halfMode == RateMode.MULTIPLIER) 1.5.dp else 1.dp,
                                    color = if (halfMode == RateMode.MULTIPLIER) BrandBluePrimary else MaterialTheme.colorScheme.outlineVariant,
                                    shape = RoundedCornerShape(10.dp)
                                ),
                            shape = RoundedCornerShape(10.dp),
                            color = if (halfMode == RateMode.MULTIPLIER) BrandBluePrimary.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    RadioButton(
                                        selected = halfMode == RateMode.MULTIPLIER,
                                        onClick = { halfMode = RateMode.MULTIPLIER }
                                    )
                                    Text("50% Ratio (0.5x)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                                val baseVal = baseWageText.toDoubleOrNull() ?: 300.0
                                Text("= ₹${(baseVal * 0.5).toInt()} / half day", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        // Fixed Amount Option
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { halfMode = RateMode.FIXED }
                                .border(
                                    width = if (halfMode == RateMode.FIXED) 1.5.dp else 1.dp,
                                    color = if (halfMode == RateMode.FIXED) BrandBluePrimary else MaterialTheme.colorScheme.outlineVariant,
                                    shape = RoundedCornerShape(10.dp)
                                ),
                            shape = RoundedCornerShape(10.dp),
                            color = if (halfMode == RateMode.FIXED) BrandBluePrimary.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    RadioButton(
                                        selected = halfMode == RateMode.FIXED,
                                        onClick = { halfMode = RateMode.FIXED }
                                    )
                                    Text("Fixed Rate (₹)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                                Text("Custom fixed ₹ amount", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    if (halfMode == RateMode.FIXED) {
                        OutlinedTextField(
                            value = halfFixedText,
                            onValueChange = { halfFixedText = it.filter { ch -> ch.isDigit() } },
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = { Text("₹", fontWeight = FontWeight.Bold) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            label = { Text("Fixed Amount for Half-Day (₹)") }
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    // 3. Double-Duty Wage Rate Setting
                    Text(
                        text = "3. " + Localization.get("double_duty_rate", lang),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { doubleMode = RateMode.MULTIPLIER }
                                .border(
                                    width = if (doubleMode == RateMode.MULTIPLIER) 1.5.dp else 1.dp,
                                    color = if (doubleMode == RateMode.MULTIPLIER) BrandBluePrimary else MaterialTheme.colorScheme.outlineVariant,
                                    shape = RoundedCornerShape(10.dp)
                                ),
                            shape = RoundedCornerShape(10.dp),
                            color = if (doubleMode == RateMode.MULTIPLIER) BrandBluePrimary.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    RadioButton(
                                        selected = doubleMode == RateMode.MULTIPLIER,
                                        onClick = { doubleMode = RateMode.MULTIPLIER }
                                    )
                                    Text("2.0x Double Rate", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                                val baseVal = baseWageText.toDoubleOrNull() ?: 300.0
                                Text("= ₹${(baseVal * 2.0).toInt()} / shift", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { doubleMode = RateMode.FIXED }
                                .border(
                                    width = if (doubleMode == RateMode.FIXED) 1.5.dp else 1.dp,
                                    color = if (doubleMode == RateMode.FIXED) BrandBluePrimary else MaterialTheme.colorScheme.outlineVariant,
                                    shape = RoundedCornerShape(10.dp)
                                ),
                            shape = RoundedCornerShape(10.dp),
                            color = if (doubleMode == RateMode.FIXED) BrandBluePrimary.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    RadioButton(
                                        selected = doubleMode == RateMode.FIXED,
                                        onClick = { doubleMode = RateMode.FIXED }
                                    )
                                    Text("Fixed Double (₹)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                                Text("Custom fixed amount", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    if (doubleMode == RateMode.FIXED) {
                        OutlinedTextField(
                            value = doubleFixedText,
                            onValueChange = { doubleFixedText = it.filter { ch -> ch.isDigit() } },
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = { Text("₹", fontWeight = FontWeight.Bold) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            label = { Text("Fixed Amount for Double Duty (₹)") }
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    // 4. Role-Based Wages Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "4. " + Localization.get("role_based_wages", lang),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Apply distinct rates based on sanitation job profile (Driver ₹450, Mukadam ₹550, Sweeper ₹300)",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = useRoleRates,
                            onCheckedChange = { useRoleRates = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = BrandBluePrimary)
                        )
                    }

                    // Save Button
                    Button(
                        onClick = {
                            val newBase = baseWageText.toDoubleOrNull() ?: 300.0
                            val newHalfFixed = halfFixedText.toDoubleOrNull() ?: 150.0
                            val newDoubleFixed = doubleFixedText.toDoubleOrNull() ?: 600.0

                            val updated = config.copy(
                                baseDailyWage = newBase,
                                halfDayMode = halfMode,
                                halfDayMultiplier = halfMultiplier,
                                halfDayFixedRate = newHalfFixed,
                                doubleDutyMode = doubleMode,
                                doubleDutyMultiplier = doubleMultiplier,
                                doubleDutyFixedRate = newDoubleFixed,
                                useRoleBasedRates = useRoleRates
                            )
                            onSaveConfig(updated)
                            onToggleExpand()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("salary_save_config_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandBluePrimary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Apply & Recalculate Payroll", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun WorkerSalaryCard(
    comp: WorkerSalaryComputation,
    lang: String,
    onEditAdjustments: () -> Unit,
    onViewPayslip: () -> Unit,
    onTogglePaid: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .testTag("worker_salary_card_${comp.worker.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Row 1: Worker Header (Avatar, Name, Code, Role, Status)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val avatarColor = try {
                        Color(android.graphics.Color.parseColor(comp.worker.avatarColorHex))
                    } catch (e: Exception) {
                        BrandBluePrimary
                    }

                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(avatarColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = comp.worker.name.take(1).uppercase(),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }

                    Column {
                        Text(
                            text = comp.worker.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = comp.worker.workerCode,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = BrandBluePrimary
                            )
                            Text(
                                text = "•",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = Localization.getRoleName(comp.worker.roleCategory, lang),
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // Payment Status Badge (Clickable to toggle)
                val isPaid = comp.paymentStatus.startsWith("PAID")
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isPaid) StatusPresentBg else StatusHalfDayBg,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onTogglePaid() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = if (isPaid) Icons.Default.CheckCircle else Icons.Default.PendingActions,
                            contentDescription = null,
                            tint = if (isPaid) StatusPresent else StatusHalfDay,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = if (isPaid) "PAID" else "PENDING",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isPaid) StatusPresent else StatusHalfDay
                        )
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

            // Row 2: Attendance Breakdown Counters
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Present (P)
                AttCountMiniCell("P (Full)", "${comp.presentCount}", StatusPresent, StatusPresentBg)
                // Half Day (H)
                AttCountMiniCell("H (Half)", "${comp.halfDayCount}", StatusHalfDay, StatusHalfDayBg)
                // Double (D)
                AttCountMiniCell("D (Double)", "${comp.doubleDutyCount}", StatusDoubleDuty, StatusDoubleDutyBg)
                // Absent (A)
                AttCountMiniCell("A (Absent)", "${comp.absentCount}", StatusAbsent, StatusAbsentBg)
                // Total Working Days
                AttCountMiniCell("Total Days", String.format(Locale.ENGLISH, "%.1fd", comp.calculatedManDays), BrandBluePrimary, BrandBluePrimary.copy(alpha = 0.12f))
            }

            // Row 3: Formula Breakdown & Net Pay
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Wage: ₹${comp.appliedDailyWage.toInt()}/d (Gross: ₹${comp.grossSalary.toInt()})",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Advance / Bonus indicators if present
                    if (comp.advanceDeduction > 0 || comp.bonusAllowance > 0) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (comp.advanceDeduction > 0) {
                                Text(
                                    text = "Adv: -₹${comp.advanceDeduction.toInt()}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = StatusAbsent
                                )
                            }
                            if (comp.bonusAllowance > 0) {
                                Text(
                                    text = "Bonus: +₹${comp.bonusAllowance.toInt()}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = StatusPresent
                                )
                            }
                        }
                    }
                }

                // Final Net Salary Big Highlight
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "NET PAYABLE",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "₹${formatCurrency(comp.netPayableSalary)}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = BrandGreenSecondary
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

            // Row 4: Action Buttons (Adjustments, Payslip Slip, Paid toggle)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Adjustments Button (उचल/बोनस)
                OutlinedButton(
                    onClick = onEditAdjustments,
                    modifier = Modifier.weight(1f).height(36.dp),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Adjustments", fontSize = 11.sp)
                }

                // Payslip & WhatsApp Button (पावती)
                Button(
                    onClick = onViewPayslip,
                    modifier = Modifier.weight(1f).height(36.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandBluePrimary),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ReceiptLong, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Payslip", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun AttCountMiniCell(
    label: String,
    value: String,
    color: Color,
    bgColor: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            fontSize = 9.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Box(
            modifier = Modifier
                .padding(top = 2.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(bgColor)
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text(
                text = value,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

// -------------------------------------------------------------------------
// Dialogs
// -------------------------------------------------------------------------

@Composable
private fun WorkerAdjustmentDialog(
    comp: WorkerSalaryComputation,
    month: String,
    lang: String,
    onSave: (advance: Double, bonus: Double, status: String, notes: String, customWage: Double?) -> Unit,
    onDismiss: () -> Unit
) {
    var advanceText by remember { mutableStateOf(if (comp.advanceDeduction > 0) comp.advanceDeduction.toInt().toString() else "") }
    var bonusText by remember { mutableStateOf(if (comp.bonusAllowance > 0) comp.bonusAllowance.toInt().toString() else "") }
    var selectedStatus by remember { mutableStateOf(comp.paymentStatus) }
    var notesText by remember { mutableStateOf(comp.paymentNotes) }
    var customWageText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "Salary Adjustments (पगार समायोजन)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = "${comp.worker.name} (${comp.worker.workerCode}) • $month",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Advance Loan Deduction (उचल)
                OutlinedTextField(
                    value = advanceText,
                    onValueChange = { advanceText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Advance / Loan Deduction (उचल ₹)") },
                    placeholder = { Text("0") },
                    leadingIcon = { Text("₹", fontWeight = FontWeight.Bold) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().testTag("salary_adj_advance_input"),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp)
                )

                // Bonus / Overtime / Festival Allowance (बोनस / भत्ता)
                OutlinedTextField(
                    value = bonusText,
                    onValueChange = { bonusText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Bonus / Allowance (बोनस/भत्ता ₹)") },
                    placeholder = { Text("0") },
                    leadingIcon = { Text("₹", fontWeight = FontWeight.Bold) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().testTag("salary_adj_bonus_input"),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp)
                )

                // Payment Status Selector
                Text(
                    text = Localization.get("payment_status", lang),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        "PENDING" to "Pending",
                        "PAID_CASH" to "Cash",
                        "PAID_BANK" to "Bank",
                        "PAID_UPI" to "UPI"
                    ).forEach { (statusKey, label) ->
                        FilterChip(
                            selected = selectedStatus == statusKey,
                            onClick = { selectedStatus = statusKey },
                            label = { Text(label, fontSize = 11.sp) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Notes
                OutlinedTextField(
                    value = notesText,
                    onValueChange = { notesText = it },
                    label = { Text("Remarks / Notes (ऐच्छिक टिप)") },
                    placeholder = { Text("e.g. Festival advance deducted") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val adv = advanceText.toDoubleOrNull() ?: 0.0
                    val bonus = bonusText.toDoubleOrNull() ?: 0.0
                    val customWage = customWageText.toDoubleOrNull()
                    onSave(adv, bonus, selectedStatus, notesText, customWage)
                },
                colors = ButtonDefaults.buttonColors(containerColor = BrandBluePrimary),
                modifier = Modifier.testTag("salary_adj_save_btn")
            ) {
                Text("Save Adjustments")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun WorkerPayslipPreviewDialog(
    comp: WorkerSalaryComputation,
    config: SalaryWageConfig,
    month: String,
    lang: String,
    onSendWhatsApp: () -> Unit,
    onExportPdf: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = Localization.get("salary_slip", lang),
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                    Text(
                        text = "${comp.worker.name} • $month",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Identity Card Box
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(text = "Worker Code: ${comp.worker.workerCode}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text(text = "Role: ${comp.worker.roleCategory}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        if (comp.worker.phone.isNotBlank()) {
                            Text(text = "Mobile: ${comp.worker.phone}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                // Attendance Breakdown Box
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Present", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${comp.presentCount} d", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = StatusPresent)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Half Day", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${comp.halfDayCount} d", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = StatusHalfDay)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Double", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${comp.doubleDutyCount} d", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = StatusDoubleDuty)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Absent", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${comp.absentCount} d", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = StatusAbsent)
                        }
                    }
                }

                // Financial Breakdown
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    SalarySlipLine("Basic Daily Rate:", "₹${comp.appliedDailyWage.toInt()} / day")
                    SalarySlipLine("Full-Day Wages (${comp.presentCount} × ₹${comp.appliedDailyWage.toInt()}):", "₹${comp.presentEarnings.toInt()}")
                    SalarySlipLine("Half-Day Wages (${comp.halfDayCount} × ₹${comp.appliedHalfDayWage.toInt()}):", "₹${comp.halfDayEarnings.toInt()}")
                    if (comp.doubleDutyCount > 0) {
                        SalarySlipLine("Double Shift Wages (${comp.doubleDutyCount} × ₹${comp.appliedDoubleDutyWage.toInt()}):", "₹${comp.doubleDutyEarnings.toInt()}")
                    }
                    if (comp.bonusAllowance > 0) {
                        SalarySlipLine("Bonus / Allowance (+):", "+₹${comp.bonusAllowance.toInt()}", color = StatusPresent)
                    }
                    if (comp.advanceDeduction > 0) {
                        SalarySlipLine("Advance Deducted (-):", "-₹${comp.advanceDeduction.toInt()}", color = StatusAbsent)
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                // Net Salary Callout
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = BrandBluePrimary.copy(alpha = 0.12f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Net Payable Salary:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = BrandBluePrimary
                        )
                        Text(
                            text = "₹${formatCurrency(comp.netPayableSalary)}",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp,
                            color = BrandGreenSecondary
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onSendWhatsApp,
                colors = ButtonDefaults.buttonColors(containerColor = BrandGreenSecondary),
                modifier = Modifier.testTag("salary_slip_whatsapp_btn")
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Send via WhatsApp")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onExportPdf,
                modifier = Modifier.testTag("salary_slip_pdf_btn")
            ) {
                Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("PDF Slip")
            }
        }
    )
}

@Composable
private fun SalarySlipLine(
    label: String,
    value: String,
    color: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 11.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = color)
    }
}

@Composable
private fun MonthSelectorDialog(
    currentMonth: String,
    onMonthSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val months = remember {
        val cal = Calendar.getInstance()
        val format = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        val list = mutableListOf<String>()
        // Generate past 12 months and next 2 months
        cal.add(Calendar.MONTH, -12)
        for (i in 0..14) {
            list.add(format.format(cal.time))
            cal.add(Calendar.MONTH, 1)
        }
        list.reversed()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Select Muster Month", fontWeight = FontWeight.Bold)
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(months) { m ->
                    val isSelected = m == currentMonth
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onMonthSelected(m) },
                        color = if (isSelected) BrandBluePrimary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = formatMonthDisplay(m, "en"),
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) BrandBluePrimary else MaterialTheme.colorScheme.onSurface
                            )
                            if (isSelected) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = BrandBluePrimary, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// -------------------------------------------------------------------------
// Helper Formatting Functions
// -------------------------------------------------------------------------

private fun formatCurrency(amount: Double): String {
    return String.format(Locale.ENGLISH, "%,d", amount.toInt())
}

private fun formatMonthDisplay(yearMonth: String, lang: String): String {
    return try {
        val parser = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        val date = parser.parse(yearMonth) ?: Date()
        val outFormat = SimpleDateFormat("MMMM yyyy", Locale.ENGLISH)
        val eng = outFormat.format(date)
        if (lang == "mr" || lang == "hi") {
            val parts = eng.split(" ")
            val monthEng = parts[0]
            val yr = parts.getOrNull(1) ?: ""
            val marathiMonth = when (monthEng.lowercase()) {
                "january" -> "जानेवारी"
                "february" -> "फेब्रुवारी"
                "march" -> "मार्च"
                "april" -> "एप्रिल"
                "may" -> "मे"
                "june" -> "जून"
                "july" -> "जुलै"
                "august" -> "ऑगस्ट"
                "september" -> "सप्टेंबर"
                "october" -> "ऑक्टोबर"
                "november" -> "नोव्हेंबर"
                "december" -> "डिसेंबर"
                else -> monthEng
            }
            "$marathiMonth $yr"
        } else {
            eng
        }
    } catch (e: Exception) {
        yearMonth
    }
}
