package com.example.ui.screens

import android.app.DatePickerDialog
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
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.WorkerEntity
import com.example.ui.components.BrandedAppTitle
import com.example.ui.components.DateNavigatorBar
import com.example.ui.theme.BrandBlueContainer
import com.example.ui.theme.BrandBluePrimary
import com.example.ui.theme.BrandGreenContainer
import com.example.ui.theme.BrandGreenSecondary
import com.example.ui.theme.BrandOrangeAccent
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
import com.example.viewmodel.AttendanceViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceScreen(
    viewModel: AttendanceViewModel,
    onBack: () -> Unit
) {
    val lang by viewModel.appLanguage.collectAsState()
    val workers by viewModel.activeWorkers.collectAsState()
    val attendanceMap by viewModel.currentDayAttendance.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val dailySummary by viewModel.dailySummary.collectAsState()
    val dailyWage by viewModel.dailyWage.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedStatusFilter by remember { mutableStateOf<String?>("ALL") } // "ALL", "UNMARKED", "P", "A", "H", "D"
    var isSelectionMode by remember { mutableStateOf(false) }
    var selectedWorkerIds by remember { mutableStateOf<Set<Int>>(emptySet()) }

    val context = LocalContext.current
    var activePdfFile by remember { mutableStateOf<java.io.File?>(null) }
    var showPdfDialog by remember { mutableStateOf(false) }

    val filteredWorkers = remember(workers, searchQuery, selectedStatusFilter, attendanceMap) {
        workers.filter { worker ->
            val matchesSearch = if (searchQuery.isBlank()) {
                true
            } else {
                worker.name.contains(searchQuery, ignoreCase = true) ||
                worker.workerCode.contains(searchQuery, ignoreCase = true) ||
                worker.roleCategory.contains(searchQuery, ignoreCase = true)
            }

            val status = attendanceMap[worker.id] ?: ""
            val matchesFilter = when (selectedStatusFilter) {
                "ALL" -> true
                "UNMARKED" -> status.isBlank()
                "P" -> status == "P"
                "A" -> status == "A"
                "H" -> status == "H"
                "D" -> status == "D"
                else -> true
            }

            matchesSearch && matchesFilter
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    BrandedAppTitle(
                        fontSize = 18,
                        showSubtitle = true,
                        subtitleText = "Attendance Sheet • हजेरी नोंदवही"
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
                    // PDF Download & WhatsApp Share Trigger
                    IconButton(
                        onClick = {
                            viewModel.generateDailyAttendancePdf(context) { file ->
                                activePdfFile = file
                                showPdfDialog = true
                            }
                        },
                        modifier = Modifier.testTag("daily_pdf_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PictureAsPdf,
                            contentDescription = "Daily PDF",
                            tint = Color(0xFFD32F2F)
                        )
                    }

                    // Save Button
                    Button(
                        onClick = { viewModel.saveAttendance() },
                        colors = ButtonDefaults.buttonColors(containerColor = BrandGreenSecondary),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .testTag("save_attendance_btn")
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = Localization.get("save", lang),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        bottomBar = {
            // Live Summary & Estimated Wage Bottom Bar
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Counter Pills
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        MiniTallyPill("P", "${dailySummary.presentCount}", StatusPresent, StatusPresentBg)
                        MiniTallyPill("A", "${dailySummary.absentCount}", StatusAbsent, StatusAbsentBg)
                        MiniTallyPill("H", "${dailySummary.halfDayCount}", StatusHalfDay, StatusHalfDayBg)
                        MiniTallyPill("D", "${dailySummary.doubleDutyCount}", StatusDoubleDuty, StatusDoubleDutyBg)
                    }

                    // Total Day's Wage Calculation
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Est. Salary (₹${dailyWage.toInt()}/d)",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "₹" + String.format(Locale.ENGLISH, "%,d", dailySummary.estimatedSalary.toInt()),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            color = BrandGreenSecondary
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Date Switcher Header
            DateNavigatorBar(
                currentDate = selectedDate,
                onPreviousDay = { viewModel.shiftDate(-1) },
                onNextDay = { viewModel.shiftDate(1) },
                onPickDate = {
                    try {
                        val cal = Calendar.getInstance()
                        val current = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(selectedDate) ?: Date()
                        cal.time = current
                        DatePickerDialog(
                            context,
                            { _, y, m, d ->
                                val pickedCal = Calendar.getInstance().apply { set(y, m, d) }
                                val newDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(pickedCal.time)
                                viewModel.setSelectedDate(newDate)
                            },
                            cal.get(Calendar.YEAR),
                            cal.get(Calendar.MONTH),
                            cal.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    } catch (e: Exception) {
                        // ignore
                    }
                },
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
            )

            // Real-time Summary Header Card
            DailyAttendanceSummaryHeader(
                totalWorkers = workers.size,
                presentCount = dailySummary.presentCount,
                absentCount = dailySummary.absentCount,
                halfDayCount = dailySummary.halfDayCount,
                doubleDutyCount = dailySummary.doubleDutyCount,
                unmarkedCount = workers.size - (dailySummary.presentCount + dailySummary.absentCount + dailySummary.halfDayCount + dailySummary.doubleDutyCount),
                lang = lang,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            )

            // Fast Search Bar & Selection Mode Switcher
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text(Localization.get("search_worker", lang), fontSize = 12.sp) },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp))
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
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
                        .weight(1f)
                        .height(46.dp)
                        .testTag("attendance_search_input")
                )

                // Multi-select toggle button
                OutlinedButton(
                    onClick = {
                        isSelectionMode = !isSelectionMode
                        if (!isSelectionMode) selectedWorkerIds = emptySet()
                    },
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (isSelectionMode) BrandBlueContainer else MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                    modifier = Modifier
                        .height(46.dp)
                        .testTag("toggle_selection_mode_btn")
                ) {
                    Icon(
                        imageVector = if (isSelectionMode) Icons.Default.CheckBox else Icons.Default.CheckBoxOutlineBlank,
                        contentDescription = "Select Workers",
                        tint = BrandBluePrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isSelectionMode) "Done" else "Select",
                        fontWeight = FontWeight.Bold,
                        color = BrandBluePrimary,
                        fontSize = 12.sp
                    )
                }

                // Quick Bulk: Mark All Present
                OutlinedButton(
                    onClick = { viewModel.markAllPresent() },
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                    modifier = Modifier
                        .height(46.dp)
                        .testTag("mark_all_present_btn")
                ) {
                    Icon(Icons.Default.DoneAll, contentDescription = null, tint = BrandGreenSecondary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "All P",
                        fontWeight = FontWeight.Bold,
                        color = BrandGreenSecondary,
                        fontSize = 12.sp
                    )
                }
            }

            // Quick Status Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                StatusFilterChip(
                    label = "All (${workers.size})",
                    isSelected = selectedStatusFilter == "ALL",
                    onClick = { selectedStatusFilter = "ALL" }
                )
                StatusFilterChip(
                    label = "Unmarked (${workers.count { (attendanceMap[it.id] ?: "").isBlank() }})",
                    isSelected = selectedStatusFilter == "UNMARKED",
                    onClick = { selectedStatusFilter = "UNMARKED" }
                )
                StatusFilterChip(
                    label = "Present (${dailySummary.presentCount})",
                    isSelected = selectedStatusFilter == "P",
                    color = StatusPresent,
                    onClick = { selectedStatusFilter = "P" }
                )
                StatusFilterChip(
                    label = "Absent (${dailySummary.absentCount})",
                    isSelected = selectedStatusFilter == "A",
                    color = StatusAbsent,
                    onClick = { selectedStatusFilter = "A" }
                )
                StatusFilterChip(
                    label = "Half-Day (${dailySummary.halfDayCount})",
                    isSelected = selectedStatusFilter == "H",
                    color = StatusHalfDay,
                    onClick = { selectedStatusFilter = "H" }
                )
                StatusFilterChip(
                    label = "Double (${dailySummary.doubleDutyCount})",
                    isSelected = selectedStatusFilter == "D",
                    color = StatusDoubleDuty,
                    onClick = { selectedStatusFilter = "D" }
                )
            }

            // Contextual Batch Action Bar (When in selection mode or workers selected)
            AnimatedVisibility(
                visible = isSelectionMode || selectedWorkerIds.isNotEmpty(),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Surface(
                    color = BrandBlueContainer.copy(alpha = 0.7f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = selectedWorkerIds.size == filteredWorkers.size && filteredWorkers.isNotEmpty(),
                                onCheckedChange = { checked ->
                                    selectedWorkerIds = if (checked) filteredWorkers.map { it.id }.toSet() else emptySet()
                                },
                                colors = CheckboxDefaults.colors(checkedColor = BrandBluePrimary),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${selectedWorkerIds.size} Selected",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = BrandBluePrimary
                            )
                        }

                        // Batch Action Buttons
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            BatchStatusButton(
                                label = "P",
                                color = StatusPresent,
                                onClick = {
                                    viewModel.batchSetAttendance(selectedWorkerIds, "P")
                                    selectedWorkerIds = emptySet()
                                }
                            )
                            BatchStatusButton(
                                label = "A",
                                color = StatusAbsent,
                                onClick = {
                                    viewModel.batchSetAttendance(selectedWorkerIds, "A")
                                    selectedWorkerIds = emptySet()
                                }
                            )
                            BatchStatusButton(
                                label = "H",
                                color = StatusHalfDay,
                                onClick = {
                                    viewModel.batchSetAttendance(selectedWorkerIds, "H")
                                    selectedWorkerIds = emptySet()
                                }
                            )
                            BatchStatusButton(
                                label = "D",
                                color = StatusDoubleDuty,
                                onClick = {
                                    viewModel.batchSetAttendance(selectedWorkerIds, "D")
                                    selectedWorkerIds = emptySet()
                                }
                            )
                        }
                    }
                }
            }

            // Grid Header: Worker Name | P (Present) | A (Absent) | H (Half Day) | D (Double)
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 2.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Worker Details (${filteredWorkers.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("P (1.0)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = StatusPresent, modifier = Modifier.width(36.dp), textAlign = TextAlign.Center)
                        Text("A (0)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = StatusAbsent, modifier = Modifier.width(36.dp), textAlign = TextAlign.Center)
                        Text("H (0.5)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = StatusHalfDay, modifier = Modifier.width(36.dp), textAlign = TextAlign.Center)
                        Text("D (2.0)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = StatusDoubleDuty, modifier = Modifier.width(36.dp), textAlign = TextAlign.Center)
                    }
                }
            }

            // Workers Attendance List
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(
                    items = filteredWorkers,
                    key = { it.id }
                ) { worker ->
                    val isChecked = selectedWorkerIds.contains(worker.id)
                    WorkerAttendanceRow(
                        worker = worker,
                        selectedStatus = attendanceMap[worker.id] ?: "",
                        lang = lang,
                        dailyWage = dailyWage,
                        isSelectionMode = isSelectionMode,
                        isSelected = isChecked,
                        onToggleSelect = {
                            selectedWorkerIds = if (isChecked) {
                                selectedWorkerIds - worker.id
                            } else {
                                selectedWorkerIds + worker.id
                            }
                        },
                        onRowClick = {
                            if (isSelectionMode) {
                                selectedWorkerIds = if (isChecked) selectedWorkerIds - worker.id else selectedWorkerIds + worker.id
                            } else {
                                // Cycle status directly on card tap: Unset -> P -> H -> A -> D -> Unset
                                viewModel.toggleWorkerAttendance(worker.id)
                            }
                        },
                        onStatusSelected = { status ->
                            viewModel.setWorkerAttendance(worker.id, status)
                        }
                    )
                }
            }
        }
    }

    if (showPdfDialog) {
        PdfReadyDialog(
            file = activePdfFile,
            reportTitle = "Daily Attendance Report",
            lang = lang,
            onDismiss = { showPdfDialog = false }
        )
    }
}

@Composable
fun WorkerAttendanceRow(
    worker: WorkerEntity,
    selectedStatus: String,
    lang: String,
    dailyWage: Double,
    isSelectionMode: Boolean,
    isSelected: Boolean,
    onToggleSelect: () -> Unit,
    onRowClick: () -> Unit,
    onStatusSelected: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onRowClick() }
            .testTag("worker_row_${worker.id}"),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) BrandBlueContainer.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surface
        ),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, BrandBluePrimary) else null,
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox in selection mode
            if (isSelectionMode) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onToggleSelect() },
                    colors = CheckboxDefaults.colors(checkedColor = BrandBluePrimary),
                    modifier = Modifier
                        .padding(end = 4.dp)
                        .size(24.dp)
                )
            }

            // Worker Code & Name Column
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(BrandBlueContainer)
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = worker.workerCode,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrandBluePrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = worker.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.5.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    Text(
                        text = Localization.getRoleName(worker.roleCategory, lang),
                        fontSize = 10.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (selectedStatus.isNotEmpty()) {
                        Spacer(modifier = Modifier.width(6.dp))
                        val statusLabel = when (selectedStatus) {
                            "P" -> "Present (1.0 d)"
                            "A" -> "Absent (0 d)"
                            "H" -> "Half-Day (0.5 d)"
                            "D" -> "Double (2.0 d)"
                            else -> ""
                        }
                        Text(
                            text = "• $statusLabel",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = when (selectedStatus) {
                                "P" -> StatusPresent
                                "A" -> StatusAbsent
                                "H" -> StatusHalfDay
                                "D" -> StatusDoubleDuty
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                }
            }

            // Attendance Action Option Buttons: P (Present) | A (Absent) | H (Half Day) | D (Double)
            Row(
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. Present (P)
                AttendanceOptionButton(
                    symbol = "P",
                    isSelected = selectedStatus == "P",
                    activeColor = StatusPresent,
                    activeBg = StatusPresentBg,
                    activeBorder = StatusPresentBorder,
                    onClick = { onStatusSelected("P") },
                    testTag = "btn_p_${worker.id}"
                )

                // 2. Absent (A)
                AttendanceOptionButton(
                    symbol = "A",
                    isSelected = selectedStatus == "A",
                    activeColor = StatusAbsent,
                    activeBg = StatusAbsentBg,
                    activeBorder = StatusAbsentBorder,
                    onClick = { onStatusSelected("A") },
                    testTag = "btn_a_${worker.id}"
                )

                // 3. Half Day (H)
                AttendanceOptionButton(
                    symbol = "H",
                    isSelected = selectedStatus == "H",
                    activeColor = StatusHalfDay,
                    activeBg = StatusHalfDayBg,
                    activeBorder = StatusHalfDayBorder,
                    onClick = { onStatusSelected("H") },
                    testTag = "btn_h_${worker.id}"
                )

                // 4. Double Duty (D)
                AttendanceOptionButton(
                    symbol = "D",
                    isSelected = selectedStatus == "D",
                    activeColor = StatusDoubleDuty,
                    activeBg = StatusDoubleDutyBg,
                    activeBorder = StatusDoubleDutyBorder,
                    onClick = { onStatusSelected("D") },
                    testTag = "btn_d_${worker.id}"
                )
            }
        }
    }
}

@Composable
fun AttendanceOptionButton(
    symbol: String,
    isSelected: Boolean,
    activeColor: Color,
    activeBg: Color,
    activeBorder: Color,
    onClick: () -> Unit,
    testTag: String
) {
    val bg = if (isSelected) activeColor else activeBg
    val textClr = if (isSelected) Color.White else activeColor
    val borderClr = if (isSelected) activeColor else activeBorder

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(width = 36.dp, height = 36.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .border(1.5.dp, borderClr, RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .testTag(testTag)
    ) {
        Text(
            text = symbol,
            color = textClr,
            fontSize = 14.sp,
            fontWeight = FontWeight.Black
        )
    }
}

@Composable
fun BatchStatusButton(
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(width = 34.dp, height = 30.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(color)
            .clickable { onClick() }
    ) {
        Text(
            text = label,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun StatusFilterChip(
    label: String,
    isSelected: Boolean,
    color: Color = BrandBluePrimary,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) color.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isSelected) color else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
        ),
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) color else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun MiniTallyPill(symbol: String, count: String, color: Color, bg: Color) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$symbol:",
            fontWeight = FontWeight.Black,
            fontSize = 10.5.sp,
            color = color
        )
        Spacer(modifier = Modifier.width(3.dp))
        Text(
            text = count,
            fontWeight = FontWeight.Bold,
            fontSize = 11.5.sp,
            color = color
        )
    }
}

/**
 * Summary header displayed at the top of the daily attendance screen showing
 * real-time counts for Present, Absent, Half-day, Double Duty, and total workers.
 */
@Composable
fun DailyAttendanceSummaryHeader(
    totalWorkers: Int,
    presentCount: Int,
    absentCount: Int,
    halfDayCount: Int,
    doubleDutyCount: Int,
    unmarkedCount: Int,
    lang: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.testTag("daily_attendance_summary_header"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = when (lang) {
                        "mr" -> "आजचा हजेरी सारांश (Real-time)"
                        "hi" -> "आज का उपस्थिति सारांश (Real-time)"
                        else -> "Today's Attendance Summary (Real-time)"
                    },
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Total: $totalWorkers Workers",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = BrandBluePrimary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // 1. Present Card
                SummaryMetricItem(
                    label = when (lang) {
                        "mr" -> "उपस्थित"
                        "hi" -> "उपस्थित"
                        else -> "Present"
                    },
                    symbol = "P",
                    count = presentCount,
                    color = StatusPresent,
                    bgColor = StatusPresentBg,
                    borderColor = StatusPresentBorder,
                    modifier = Modifier.weight(1f),
                    testTag = "summary_present_count"
                )

                // 2. Absent Card
                SummaryMetricItem(
                    label = when (lang) {
                        "mr" -> "अनुपस्थित"
                        "hi" -> "अनुपस्थित"
                        else -> "Absent"
                    },
                    symbol = "A",
                    count = absentCount,
                    color = StatusAbsent,
                    bgColor = StatusAbsentBg,
                    borderColor = StatusAbsentBorder,
                    modifier = Modifier.weight(1f),
                    testTag = "summary_absent_count"
                )

                // 3. Half-Day Card
                SummaryMetricItem(
                    label = when (lang) {
                        "mr" -> "अर्धा दिवस"
                        "hi" -> "आधा दिन"
                        else -> "Half-Day"
                    },
                    symbol = "H",
                    count = halfDayCount,
                    color = StatusHalfDay,
                    bgColor = StatusHalfDayBg,
                    borderColor = StatusHalfDayBorder,
                    modifier = Modifier.weight(1f),
                    testTag = "summary_halfday_count"
                )

                // 4. Double Duty Card
                SummaryMetricItem(
                    label = when (lang) {
                        "mr" -> "डबल ड्युटी"
                        "hi" -> "डबल ड्यूटी"
                        else -> "Double"
                    },
                    symbol = "D",
                    count = doubleDutyCount,
                    color = StatusDoubleDuty,
                    bgColor = StatusDoubleDutyBg,
                    borderColor = StatusDoubleDutyBorder,
                    modifier = Modifier.weight(1f),
                    testTag = "summary_double_count"
                )
            }
        }
    }
}

@Composable
fun SummaryMetricItem(
    label: String,
    symbol: String,
    count: Int,
    color: Color,
    bgColor: Color,
    borderColor: Color,
    modifier: Modifier = Modifier,
    testTag: String
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = bgColor,
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
        modifier = modifier.testTag(testTag)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "$count",
                fontSize = 17.sp,
                fontWeight = FontWeight.Black,
                color = color
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = symbol,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    color = color
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = label,
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

