package com.example.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.WorkerEntity
import com.example.ui.components.BrandedAppTitle
import com.example.ui.theme.BrandBlueContainer
import com.example.ui.theme.BrandBluePrimary
import com.example.ui.theme.BrandGreenContainer
import com.example.ui.theme.BrandGreenSecondary
import com.example.ui.theme.BrandOrangeAccent
import com.example.ui.theme.BrandPurpleAccent
import com.example.ui.theme.StatusAbsent
import com.example.util.Localization
import com.example.viewmodel.AttendanceViewModel

enum class WorkerSortOption(val title: String) {
    NAME_ASC("Name (A to Z)"),
    NAME_DESC("Name (Z to A)"),
    CODE_ASC("Worker Code (Asc)"),
    ID_DESC("Recently Added")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkerManagementScreen(
    viewModel: AttendanceViewModel,
    onBack: () -> Unit
) {
    val lang by viewModel.appLanguage.collectAsState()
    val workers by viewModel.activeWorkers.collectAsState()
    val monthlyStats by viewModel.monthlyStats.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedRoleFilter by remember { mutableStateOf<String?>(null) }
    var currentSort by remember { mutableStateOf(WorkerSortOption.NAME_ASC) }
    var showSortMenu by remember { mutableStateOf(false) }

    var workerToView by remember { mutableStateOf<WorkerEntity?>(null) }
    var workerToEdit by remember { mutableStateOf<WorkerEntity?>(null) }
    var workerToDelete by remember { mutableStateOf<WorkerEntity?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current

    val filteredWorkers = remember(workers, searchQuery, selectedRoleFilter, currentSort) {
        val filtered = workers.filter { w ->
            val matchesSearch = searchQuery.isBlank() ||
                w.name.contains(searchQuery, ignoreCase = true) ||
                w.workerCode.contains(searchQuery, ignoreCase = true) ||
                w.phone.contains(searchQuery)
            val matchesRole = selectedRoleFilter == null || w.roleCategory == selectedRoleFilter
            matchesSearch && matchesRole
        }
        when (currentSort) {
            WorkerSortOption.NAME_ASC -> filtered.sortedBy { it.name.lowercase() }
            WorkerSortOption.NAME_DESC -> filtered.sortedByDescending { it.name.lowercase() }
            WorkerSortOption.CODE_ASC -> filtered.sortedBy { it.workerCode.lowercase() }
            WorkerSortOption.ID_DESC -> filtered.sortedByDescending { it.id }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    BrandedAppTitle(
                        fontSize = 18,
                        showSubtitle = true,
                        subtitleText = "Sanitation Workers Roster (${workers.size} Total)"
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("btn_back_workers")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = BrandBluePrimary
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showAddDialog = true },
                        modifier = Modifier.testTag("btn_top_add_worker")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PersonAdd,
                            contentDescription = "Add Worker",
                            tint = BrandOrangeAccent
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = BrandOrangeAccent,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("fab_add_worker")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Worker")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = Localization.get("add_worker", lang),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Top Summary Bar
            Surface(
                color = BrandBlueContainer.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Groups,
                            contentDescription = null,
                            tint = BrandBluePrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${workers.size} Total Registered Staff",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = BrandBluePrimary
                        )
                    }

                    // Sort menu button
                    Box {
                        TextButton(
                            onClick = { showSortMenu = true },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("btn_sort_workers")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Sort,
                                contentDescription = null,
                                tint = BrandBluePrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = currentSort.title,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = BrandBluePrimary
                            )
                        }

                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false }
                        ) {
                            WorkerSortOption.values().forEach { option ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = option.title,
                                            fontWeight = if (currentSort == option) FontWeight.Bold else FontWeight.Normal,
                                            color = if (currentSort == option) BrandBluePrimary else MaterialTheme.colorScheme.onSurface
                                        )
                                    },
                                    onClick = {
                                        currentSort = option
                                        showSortMenu = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Search Input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text(Localization.get("search_worker", lang), fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = BrandBluePrimary) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BrandBluePrimary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("worker_search_bar")
            )

            // Sanitation Category Filter Chips
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                item {
                    RoleFilterChip(
                        title = "All (${workers.size})",
                        isSelected = selectedRoleFilter == null,
                        onClick = { selectedRoleFilter = null }
                    )
                }
                items(Localization.sanitationRoles) { (roleKey, _) ->
                    val roleCount = workers.count { it.roleCategory == roleKey }
                    if (roleCount > 0) {
                        RoleFilterChip(
                            title = "${Localization.getRoleName(roleKey, lang)} ($roleCount)",
                            isSelected = selectedRoleFilter == roleKey,
                            onClick = {
                                selectedRoleFilter = if (selectedRoleFilter == roleKey) null else roleKey
                            }
                        )
                    }
                }
            }

            // Workers List
            if (workers.isEmpty()) {
                // Empty state when no workers exist at all
                EmptyWorkersState(
                    title = "No Sanitation Workers Added",
                    description = "Start by adding your municipal or contract sanitation staff to record attendance and generate daily/monthly reports.",
                    buttonText = Localization.get("add_worker", lang),
                    onAddClick = { showAddDialog = true }
                )
            } else if (filteredWorkers.isEmpty()) {
                // Empty state when filter or search gives no matches
                EmptySearchResultsState(
                    query = searchQuery,
                    onClearFilter = {
                        searchQuery = ""
                        selectedRoleFilter = null
                    }
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(
                        items = filteredWorkers,
                        key = { it.id }
                    ) { worker ->
                        val workerMonthly = monthlyStats.find { it.worker.id == worker.id }
                        WorkerCard(
                            worker = worker,
                            lang = lang,
                            workingDays = workerMonthly?.calculatedWorkingDays ?: 0.0,
                            monthlySalary = workerMonthly?.finalSalary ?: 0.0,
                            onClick = { workerToView = worker },
                            onEdit = { workerToEdit = worker },
                            onDelete = { workerToDelete = worker },
                            onCall = {
                                if (worker.phone.isNotBlank()) {
                                    try {
                                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${worker.phone}"))
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Could not open dialer", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    // Worker Profile Detail Dialog (Read)
    workerToView?.let { worker ->
        val stat = monthlyStats.find { it.worker.id == worker.id }
        WorkerProfileDialog(
            worker = worker,
            stat = stat,
            lang = lang,
            onDismiss = { workerToView = null },
            onEdit = {
                workerToView = null
                workerToEdit = worker
            },
            onCall = {
                if (worker.phone.isNotBlank()) {
                    try {
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${worker.phone}"))
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        Toast.makeText(context, "Could not open dialer", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        )
    }

    // Add / Edit Worker Dialog (Create / Update)
    if (showAddDialog || workerToEdit != null) {
        val editingWorker = workerToEdit
        AddEditWorkerDialog(
            worker = editingWorker,
            lang = lang,
            onDismiss = {
                showAddDialog = false
                workerToEdit = null
            },
            onSave = { code, name, phone, role, colorHex ->
                if (editingWorker != null) {
                    viewModel.updateWorker(
                        editingWorker.copy(
                            workerCode = code,
                            name = name,
                            phone = phone,
                            roleCategory = role,
                            avatarColorHex = colorHex
                        )
                    )
                    Toast.makeText(context, "Worker '${name}' updated successfully", Toast.LENGTH_SHORT).show()
                } else {
                    viewModel.addWorker(
                        code = code,
                        name = name,
                        phone = phone,
                        role = role,
                        colorHex = colorHex
                    )
                    Toast.makeText(context, "Worker '${name}' added successfully", Toast.LENGTH_SHORT).show()
                }
                showAddDialog = false
                workerToEdit = null
            }
        )
    }

    // Delete Confirmation Dialog (Delete)
    workerToDelete?.let { worker ->
        DeleteWorkerConfirmationDialog(
            worker = worker,
            lang = lang,
            onDismiss = { workerToDelete = null },
            onConfirmDelete = {
                viewModel.deleteWorker(worker)
                Toast.makeText(context, "Worker '${worker.name}' removed from roster", Toast.LENGTH_SHORT).show()
                workerToDelete = null
            }
        )
    }
}

@Composable
fun WorkerCard(
    worker: WorkerEntity,
    lang: String,
    workingDays: Double,
    monthlySalary: Double,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onCall: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("worker_card_${worker.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Worker Avatar / Photo Badge
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(parseHexColor(worker.avatarColorHex)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = worker.name.trim().take(2).uppercase(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Details
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(BrandBlueContainer)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = worker.workerCode,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrandBluePrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = worker.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Text(
                    text = Localization.getRoleName(worker.roleCategory, lang),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp)
                )

                if (worker.phone.isNotBlank()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Icon(
                            Icons.Default.Phone,
                            contentDescription = null,
                            tint = BrandGreenSecondary,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = worker.phone,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = BrandGreenSecondary
                        )
                    }
                }
            }

            // Quick Call & Edit & Delete Actions
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (worker.phone.isNotBlank()) {
                    IconButton(
                        onClick = onCall,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(BrandGreenContainer)
                            .testTag("call_worker_${worker.id}")
                    ) {
                        Icon(
                            Icons.Default.Call,
                            contentDescription = "Call",
                            tint = BrandGreenSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                }

                IconButton(
                    onClick = onEdit,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(BrandBlueContainer)
                        .testTag("edit_worker_${worker.id}")
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = BrandBluePrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFEBEE))
                        .testTag("delete_worker_${worker.id}")
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = StatusAbsent,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun RoleFilterChip(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bg = if (isSelected) BrandBluePrimary else MaterialTheme.colorScheme.surface
    val textClr = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
    val border = if (isSelected) BrandBluePrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = textClr
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditWorkerDialog(
    worker: WorkerEntity?,
    lang: String,
    onDismiss: () -> Unit,
    onSave: (code: String, name: String, phone: String, role: String, colorHex: String) -> Unit
) {
    var name by remember { mutableStateOf(worker?.name ?: "") }
    var code by remember { mutableStateOf(worker?.workerCode ?: "SAN-${(100..999).random()}") }
    var phone by remember { mutableStateOf(worker?.phone ?: "") }
    var selectedRole by remember { mutableStateOf(worker?.roleCategory ?: "Broom Worker") }
    var selectedColor by remember { mutableStateOf(worker?.avatarColorHex ?: "#1565C0") }
    var roleExpanded by remember { mutableStateOf(false) }
    var hasNameError by remember { mutableStateOf(false) }

    val presetColors = listOf("#1565C0", "#2E7D32", "#EF6C00", "#7B1FA2", "#00897B", "#C2185B", "#3949AB", "#D81B60")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(parseHexColor(selectedColor)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (name.isNotBlank()) name.take(2).uppercase() else "W",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = if (worker == null) Localization.get("add_worker", lang) else Localization.get("edit_worker", lang),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Name Field (Required)
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        if (it.isNotBlank()) hasNameError = false
                    },
                    label = { Text(Localization.get("worker_name", lang)) },
                    placeholder = { Text("e.g. Ramesh Shinde") },
                    isError = hasNameError,
                    supportingText = if (hasNameError) {
                        { Text(Localization.get("worker_name_required", lang), color = StatusAbsent, fontSize = 11.sp) }
                    } else null,
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_worker_name")
                )

                // Worker ID Field with Auto-Generate Helper
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = code,
                        onValueChange = { code = it },
                        label = { Text(Localization.get("worker_id", lang)) },
                        placeholder = { Text("SAN-101") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_worker_code")
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedButton(
                        onClick = {
                            code = "SAN-${(100..999).random()}"
                        },
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp),
                        modifier = Modifier
                            .height(54.dp)
                            .testTag("btn_auto_code")
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Auto", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(Localization.get("generate_id", lang), fontSize = 11.sp)
                    }
                }

                // Mobile Number Field
                OutlinedTextField(
                    value = phone,
                    onValueChange = { input ->
                        val digits = input.filter { it.isDigit() }
                        if (digits.length <= 10) phone = digits
                    },
                    label = { Text(Localization.get("mobile_number", lang)) },
                    placeholder = { Text("9876543210") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    leadingIcon = {
                        Icon(Icons.Default.Phone, contentDescription = null, tint = BrandGreenSecondary, modifier = Modifier.size(18.dp))
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_worker_phone")
                )

                // Role Category Dropdown
                ExposedDropdownMenuBox(
                    expanded = roleExpanded,
                    onExpandedChange = { roleExpanded = !roleExpanded }
                ) {
                    OutlinedTextField(
                        value = Localization.getRoleName(selectedRole, lang),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(Localization.get("role_category", lang)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = roleExpanded) },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                            .testTag("input_worker_role")
                    )
                    ExposedDropdownMenu(
                        expanded = roleExpanded,
                        onDismissRequest = { roleExpanded = false }
                    ) {
                        Localization.sanitationRoles.forEach { (roleKey, _) ->
                            DropdownMenuItem(
                                text = { Text(Localization.getRoleName(roleKey, lang)) },
                                onClick = {
                                    selectedRole = roleKey
                                    roleExpanded = false
                                }
                            )
                        }
                    }
                }

                // Avatar Color Picker
                Text(
                    text = "Worker Badge Color",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 4.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    presetColors.forEach { hex ->
                        val isPicked = selectedColor.equals(hex, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(parseHexColor(hex))
                                .border(
                                    if (isPicked) 2.5.dp else 0.dp,
                                    if (isPicked) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                    CircleShape
                                )
                                .clickable { selectedColor = hex },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isPicked) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.trim().isBlank()) {
                        hasNameError = true
                    } else {
                        onSave(code.trim().ifBlank { "SAN-101" }, name.trim(), phone.trim(), selectedRole, selectedColor)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = BrandGreenSecondary),
                modifier = Modifier.testTag("save_worker_button")
            ) {
                Text(Localization.get("save", lang), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_worker_button")
            ) {
                Text(Localization.get("cancel", lang))
            }
        }
    )
}

@Composable
fun DeleteWorkerConfirmationDialog(
    worker: WorkerEntity,
    lang: String,
    onDismiss: () -> Unit,
    onConfirmDelete: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFFEBEE)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = StatusAbsent,
                    modifier = Modifier.size(28.dp)
                )
            }
        },
        title = {
            Text(
                text = Localization.get("delete_worker", lang),
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = Localization.get("delete_confirm", lang),
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(parseHexColor(worker.avatarColorHex)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = worker.name.take(2).uppercase(),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(text = worker.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(
                                text = "${worker.workerCode} • ${Localization.getRoleName(worker.roleCategory, lang)}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Text(
                    text = Localization.get("delete_warning_subtext", lang),
                    fontSize = 11.sp,
                    color = StatusAbsent,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirmDelete,
                colors = ButtonDefaults.buttonColors(containerColor = StatusAbsent),
                modifier = Modifier.testTag("btn_confirm_delete_worker")
            ) {
                Text(Localization.get("delete", lang), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("btn_cancel_delete_worker")
            ) {
                Text(Localization.get("cancel", lang))
            }
        }
    )
}

@Composable
fun WorkerProfileDialog(
    worker: WorkerEntity,
    stat: com.example.data.WorkerMonthlyStat?,
    lang: String,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onCall: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(parseHexColor(worker.avatarColorHex)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = worker.name.take(2).uppercase(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(text = worker.name, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                    Text(
                        text = "${worker.workerCode} • ${Localization.getRoleName(worker.roleCategory, lang)}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (worker.phone.isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = BrandGreenContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Phone, contentDescription = null, tint = BrandGreenSecondary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = worker.phone, fontWeight = FontWeight.Bold, color = BrandGreenSecondary)
                            }
                            IconButton(onClick = onCall, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.Call, contentDescription = "Call", tint = BrandGreenSecondary)
                            }
                        }
                    }
                }

                // Monthly Performance Stats
                stat?.let { s ->
                    Text(
                        text = "This Month Performance",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        ProfileMiniStat("Present", "${s.presentCount}d", BrandGreenSecondary, BrandGreenContainer, Modifier.weight(1f))
                        ProfileMiniStat("Absent", "${s.absentCount}d", StatusAbsent, Color(0xFFFFEBEE), Modifier.weight(1f))
                        ProfileMiniStat("Half Day", "${s.halfDayCount}d", BrandOrangeAccent, Color(0xFFFFF3E0), Modifier.weight(1f))
                        ProfileMiniStat("Double", "${s.doubleDutyCount}d", BrandPurpleAccent, Color(0xFFF3E5F5), Modifier.weight(1f))
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = BrandBlueContainer,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Calculated Work Days:", fontSize = 12.sp, color = BrandBluePrimary)
                            Text(
                                text = "${String.format(java.util.Locale.ENGLISH, "%.1f", s.calculatedWorkingDays)} Days",
                                fontWeight = FontWeight.Bold,
                                color = BrandBluePrimary
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFE8F5E9),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Total Monthly Salary:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = BrandGreenSecondary)
                            Text(
                                text = "₹" + String.format(java.util.Locale.ENGLISH, "%,d", s.finalSalary.toInt()),
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Black,
                                color = BrandGreenSecondary
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onEdit,
                colors = ButtonDefaults.buttonColors(containerColor = BrandBluePrimary)
            ) {
                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(Localization.get("edit_worker", lang))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(Localization.get("cancel", lang))
            }
        }
    )
}

@Composable
fun EmptyWorkersState(
    title: String,
    description: String,
    buttonText: String,
    onAddClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(BrandOrangeAccent.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.PersonAdd,
                contentDescription = null,
                tint = BrandOrangeAccent,
                modifier = Modifier.size(36.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = title,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = description,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            lineHeight = 18.sp
        )
        Spacer(modifier = Modifier.height(20.dp))
        Button(
            onClick = onAddClick,
            colors = ButtonDefaults.buttonColors(containerColor = BrandOrangeAccent),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = buttonText, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun EmptySearchResultsState(
    query: String,
    onClearFilter: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.outline,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "No Workers Found",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
        if (query.isNotBlank()) {
            Text(
                text = "No matches for '$query'",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedButton(
            onClick = onClearFilter,
            shape = RoundedCornerShape(10.dp)
        ) {
            Text("Clear Search & Filters")
        }
    }
}

@Composable
fun ProfileMiniStat(title: String, value: String, color: Color, bg: Color, modifier: Modifier) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = bg,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = title, fontSize = 9.sp, color = color, fontWeight = FontWeight.SemiBold)
            Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.Black, color = color)
        }
    }
}

fun parseHexColor(hex: String): Color {
    return try {
        val clean = hex.replace("#", "")
        val colorInt = clean.toLong(16).toInt() or 0xFF000000.toInt()
        Color(colorInt)
    } catch (e: Exception) {
        BrandBluePrimary
    }
}
