package com.example.ui.components

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BrandBlueDark
import com.example.ui.theme.BrandBluePrimary
import com.example.ui.theme.BrandGreenSecondary
import com.example.ui.theme.BrandOrangeAccent
import com.example.ui.theme.BrandPurpleAccent
import com.example.util.AutoBackupConfig
import com.example.util.DatabaseBackupManager
import com.example.util.DatabaseSummaryStats
import com.example.util.ExportFormat
import com.example.util.Localization
import com.example.util.RestoreResult
import com.example.util.StoredBackupFile
import com.example.viewmodel.AttendanceViewModel
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DatabaseBackupSection(
    viewModel: AttendanceViewModel,
    lang: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val dbStats by viewModel.dbSummaryStats.collectAsState()
    val autoBackupConfig by viewModel.autoBackupConfig.collectAsState()
    val storedBackups by viewModel.storedAutoBackups.collectAsState()

    var showExportDialog by remember { mutableStateOf(false) }
    var showRestoreDialog by remember { mutableStateOf(false) }
    var exportSuccessFile by remember { mutableStateOf<File?>(null) }
    var exportSuccessFormat by remember { mutableStateOf(ExportFormat.JSON) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var isProcessing by remember { mutableStateOf(false) }
    var isAutoBackingUpNow by remember { mutableStateOf(false) }

    // Dialog state for restoring a stored local archive
    var fileToRestore by remember { mutableStateOf<File?>(null) }
    var fileToDelete by remember { mutableStateOf<File?>(null) }

    LaunchedEffect(Unit) {
        viewModel.refreshDbStats(context)
        viewModel.refreshStoredAutoBackups(context)
    }

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(14.dp)) {

        // 1. Primary Database Backup & Export Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Header
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
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(listOf(Color(0xFF2E7D32), Color(0xFF1B5E20)))
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Storage, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = Localization.get("database_backup", lang),
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "Export JSON / CSV files to local storage",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFE8F5E9),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2E7D32).copy(alpha = 0.3f))
                    ) {
                        Text(
                            text = "LOCAL ROOM DB",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E7D32),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Database Live Metrics Matrix
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DbMetricPill(
                        icon = Icons.Default.People,
                        label = "Workers",
                        value = "${dbStats?.totalWorkers ?: 0}",
                        tint = BrandBluePrimary,
                        modifier = Modifier.weight(1f)
                    )
                    DbMetricPill(
                        icon = Icons.Default.Assessment,
                        label = "Attendance Logs",
                        value = "${dbStats?.totalAttendanceRecords ?: 0}",
                        tint = Color(0xFF2E7D32),
                        modifier = Modifier.weight(1f)
                    )
                    DbMetricPill(
                        icon = Icons.Default.Storage,
                        label = "Est. Size",
                        value = "${dbStats?.estimatedDbSizeKb ?: 1} KB",
                        tint = BrandOrangeAccent,
                        modifier = Modifier.weight(1f)
                    )
                }

                if (!dbStats?.lastBackupDate.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 2.dp)
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF2E7D32),
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Last exported: ${dbStats?.lastBackupDate}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Quick Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 1. JSON Export Button
                    Button(
                        onClick = {
                            isProcessing = true
                            viewModel.exportDatabaseJson(context) { file ->
                                isProcessing = false
                                if (file != null) {
                                    exportSuccessFile = file
                                    exportSuccessFormat = ExportFormat.JSON
                                    showSuccessDialog = true
                                }
                            }
                        },
                        enabled = !isProcessing,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B5E20)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_export_json_backup")
                    ) {
                        Icon(Icons.Default.DataObject, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Export JSON", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    // 2. CSV Export Options Button
                    Button(
                        onClick = { showExportDialog = true },
                        enabled = !isProcessing,
                        colors = ButtonDefaults.buttonColors(containerColor = BrandBluePrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_export_csv_backup")
                    ) {
                        Icon(Icons.Default.TableChart, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Export CSV", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    // 3. Restore Button
                    OutlinedButton(
                        onClick = { showRestoreDialog = true },
                        enabled = !isProcessing,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("btn_restore_database")
                    ) {
                        Icon(Icons.Default.Restore, contentDescription = null, tint = BrandPurpleAccent, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Restore", fontSize = 12.sp, color = BrandPurpleAccent, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 2. Dedicated Automatic Daily Backup Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (autoBackupConfig.isEnabled) Color(0xFFF1F8E9) else MaterialTheme.colorScheme.surface
            ),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (autoBackupConfig.isEnabled) Color(0xFF81C784).copy(alpha = 0.6f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Header with Switch
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
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(
                                    if (autoBackupConfig.isEnabled) Color(0xFF2E7D32) else Color(0xFF757575)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Autorenew, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = Localization.get("auto_daily_backup", lang),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = if (autoBackupConfig.isEnabled) Color(0xFF1B5E20) else MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = if (autoBackupConfig.isEnabled) Color(0xFF2E7D32) else Color(0xFF9E9E9E)
                                ) {
                                    Text(
                                        text = if (autoBackupConfig.isEnabled) "ACTIVE" else "OFF",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                    )
                                }
                            }
                            Text(
                                text = Localization.get("auto_backup_desc", lang),
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Switch(
                        checked = autoBackupConfig.isEnabled,
                        onCheckedChange = { enabled ->
                            viewModel.updateAutoBackupConfig(
                                context = context,
                                isEnabled = enabled,
                                format = autoBackupConfig.format,
                                retentionDays = autoBackupConfig.retentionDays
                            )
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF2E7D32)
                        ),
                        modifier = Modifier.testTag("switch_auto_backup")
                    )
                }

                if (autoBackupConfig.isEnabled) {
                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = Color(0xFF81C784).copy(alpha = 0.3f))
                    Spacer(modifier = Modifier.height(12.dp))

                    // Status details
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color.White.copy(alpha = 0.7f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFC8E6C9)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Schedule, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = Localization.get("auto_backup_last_run", lang) + ":",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF1B5E20)
                                    )
                                }
                                Text(
                                    text = autoBackupConfig.lastBackupTime ?: "Pending first run",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2E7D32)
                                )
                            }

                            if (!autoBackupConfig.lastStatus.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Status: ${autoBackupConfig.lastStatus}",
                                    fontSize = 10.sp,
                                    color = Color(0xFF388E3C)
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Folder, contentDescription = null, tint = Color(0xFF558B2F), modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Saved in: Local Storage + Downloads/SumitAttendance/AutoBackups",
                                    fontSize = 10.sp,
                                    color = Color(0xFF558B2F)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Backup Format Options
                    Text(
                        text = "Backup Format:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FormatOptionChip(
                            label = "JSON (Complete)",
                            isSelected = autoBackupConfig.format == ExportFormat.JSON,
                            onClick = {
                                viewModel.updateAutoBackupConfig(
                                    context = context,
                                    isEnabled = true,
                                    format = ExportFormat.JSON,
                                    retentionDays = autoBackupConfig.retentionDays
                                )
                            },
                            modifier = Modifier.weight(1f)
                        )
                        FormatOptionChip(
                            label = "CSV Attendance",
                            isSelected = autoBackupConfig.format == ExportFormat.CSV_ATTENDANCE,
                            onClick = {
                                viewModel.updateAutoBackupConfig(
                                    context = context,
                                    isEnabled = true,
                                    format = ExportFormat.CSV_ATTENDANCE,
                                    retentionDays = autoBackupConfig.retentionDays
                                )
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Retention Duration Options
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Auto-Prune History:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf(7, 14, 30).forEach { days ->
                                val isSelected = autoBackupConfig.retentionDays == days
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSelected) Color(0xFF2E7D32) else Color.White,
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (isSelected) Color(0xFF2E7D32) else Color(0xFFC8E6C9)
                                    ),
                                    modifier = Modifier.clickable {
                                        viewModel.updateAutoBackupConfig(
                                            context = context,
                                            isEnabled = true,
                                            format = autoBackupConfig.format,
                                            retentionDays = days
                                        )
                                    }
                                ) {
                                    Text(
                                        text = "$days Days",
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) Color.White else Color(0xFF2E7D32),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // "Backup to Storage Now" Trigger Button
                    Button(
                        onClick = {
                            isAutoBackingUpNow = true
                            viewModel.checkAndRunDailyAutoBackup(context, force = true) { result ->
                                isAutoBackingUpNow = false
                                if (result.success && result.file != null) {
                                    Toast.makeText(context, "Saved to local storage: ${result.file.name}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        enabled = !isAutoBackingUpNow,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_trigger_daily_backup_now")
                    ) {
                        if (isAutoBackingUpNow) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Saving Backup to Local Storage...", fontSize = 13.sp)
                        } else {
                            Icon(Icons.Default.Backup, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(Localization.get("auto_backup_run_now", lang), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // 3. Stored Local Auto-Backups Archive List
        if (storedBackups.isNotEmpty()) {
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
                            Icon(Icons.Default.Folder, contentDescription = null, tint = BrandBluePrimary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Local Auto-Backup Archives (${storedBackups.size})",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                        IconButton(
                            onClick = { viewModel.refreshStoredAutoBackups(context) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = BrandBluePrimary, modifier = Modifier.size(18.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    storedBackups.forEach { backup ->
                        StoredBackupItemRow(
                            backup = backup,
                            onRestore = { fileToRestore = backup.file },
                            onShare = { DatabaseBackupManager.shareExportFile(context, backup.file, backup.format.mimeType, "Daily Backup: ${backup.fileName}") },
                            onSaveToDownloads = { DatabaseBackupManager.copyFileToPublicDownloads(context, backup.file) },
                            onDelete = { fileToDelete = backup.file }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }

    // Export Options Dialog
    if (showExportDialog) {
        ExportDatabaseOptionsDialog(
            viewModel = viewModel,
            lang = lang,
            onDismiss = { showExportDialog = false },
            onFileReady = { file, format ->
                showExportDialog = false
                exportSuccessFile = file
                exportSuccessFormat = format
                showSuccessDialog = true
            }
        )
    }

    // Restore Database Dialog
    if (showRestoreDialog) {
        RestoreDatabaseDialog(
            viewModel = viewModel,
            lang = lang,
            onDismiss = { showRestoreDialog = false },
            onRestored = { result ->
                Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
                showRestoreDialog = false
            }
        )
    }

    // Success & Action Dialog
    if (showSuccessDialog && exportSuccessFile != null) {
        BackupExportSuccessDialog(
            file = exportSuccessFile!!,
            format = exportSuccessFormat,
            lang = lang,
            onDismiss = { showSuccessDialog = false }
        )
    }

    // Confirm Restore Archive Dialog
    if (fileToRestore != null) {
        AlertDialog(
            onDismissRequest = { fileToRestore = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = BrandOrangeAccent)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Restore Database Archive?", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Text(
                    text = "Are you sure you want to restore data from '${fileToRestore?.name}'? This will merge and update workers and attendance records from this backup point.",
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val file = fileToRestore
                        fileToRestore = null
                        if (file != null) {
                            viewModel.restoreFromStoredBackupFile(context, file) { result ->
                                Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandPurpleAccent)
                ) {
                    Text("Yes, Restore Data")
                }
            },
            dismissButton = {
                TextButton(onClick = { fileToRestore = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Confirm Delete Archive Dialog
    if (fileToDelete != null) {
        AlertDialog(
            onDismissRequest = { fileToDelete = null },
            title = {
                Text("Delete Backup Archive?", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            },
            text = {
                Text(
                    text = "Do you want to delete '${fileToDelete?.name}' from local storage? This file cannot be recovered once removed.",
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val file = fileToDelete
                        fileToDelete = null
                        if (file != null) {
                            viewModel.deleteStoredBackupFile(context, file)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete File")
                }
            },
            dismissButton = {
                TextButton(onClick = { fileToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun FormatOptionChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) Color(0xFF2E7D32) else Color.White,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isSelected) Color(0xFF2E7D32) else Color(0xFFC8E6C9)
        ),
        modifier = modifier.clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (isSelected) {
                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color.White else Color(0xFF2E7D32)
            )
        }
    }
}

@Composable
fun StoredBackupItemRow(
    backup: StoredBackupFile,
    onRestore: () -> Unit,
    onShare: () -> Unit,
    onSaveToDownloads: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = if (backup.fileName.endsWith(".json")) Color(0xFF1B5E20) else BrandBluePrimary
                    ) {
                        Text(
                            text = if (backup.fileName.endsWith(".json")) "JSON" else "CSV",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = backup.fileName,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            maxLines = 1
                        )
                        Text(
                            text = "${backup.formattedDate} • ${backup.sizeKb} KB",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    IconButton(onClick = onRestore, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Restore, contentDescription = "Restore", tint = BrandPurpleAccent, modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onShare, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Share, contentDescription = "Share", tint = Color(0xFF2E7D32), modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onSaveToDownloads, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Download, contentDescription = "Save to Downloads", tint = BrandBluePrimary, modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun DbMetricPill(
    icon: ImageVector,
    label: String,
    value: String,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = tint.copy(alpha = 0.08f),
        border = androidx.compose.foundation.BorderStroke(1.dp, tint.copy(alpha = 0.2f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = label,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}

@Composable
fun ExportDatabaseOptionsDialog(
    viewModel: AttendanceViewModel,
    lang: String,
    onDismiss: () -> Unit,
    onFileReady: (File, ExportFormat) -> Unit
) {
    val context = LocalContext.current
    var selectedFormat by remember { mutableStateOf(ExportFormat.CSV_ATTENDANCE) }
    var isExporting by remember { mutableStateOf(false) }

    val options = listOf(
        ExportFormat.JSON to "JSON Complete Database (Full Backup with Settings)",
        ExportFormat.CSV_ATTENDANCE to "CSV Attendance Log (All records with worker names & codes)",
        ExportFormat.CSV_WORKERS to "CSV Workers Master (ID, phone, role, active status)",
        ExportFormat.CSV_COMPLETE to "CSV Complete System Master (Flat unified table)",
        ExportFormat.CSV_MONTHLY_PAYROLL to "CSV Monthly Payroll Muster (Current month wages & tallies)"
    )

    AlertDialog(
        onDismissRequest = { if (!isExporting) onDismiss() },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.FileDownload, contentDescription = null, tint = Color(0xFF2E7D32))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Export Database to Local Storage", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Select format and data table to export for offline storage or Excel spreadsheet processing:",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                options.forEach { (format, description) ->
                    val isSelected = selectedFormat == format
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) Color(0xFFE8F5E9) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) Color(0xFF2E7D32) else Color.Transparent
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { selectedFormat = format }
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { selectedFormat = format },
                                colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF2E7D32))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = format.displayName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = if (isSelected) Color(0xFF1B5E20) else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = description,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
                    isExporting = true
                    viewModel.exportDatabaseCsv(context, selectedFormat) { file ->
                        isExporting = false
                        if (file != null) {
                            onFileReady(file, selectedFormat)
                        } else {
                            Toast.makeText(context, "Export failed. Try again.", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                enabled = !isExporting,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                shape = RoundedCornerShape(8.dp)
            ) {
                if (isExporting) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Exporting...")
                } else {
                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Export & Save")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isExporting
            ) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun RestoreDatabaseDialog(
    viewModel: AttendanceViewModel,
    lang: String,
    onDismiss: () -> Unit,
    onRestored: (RestoreResult) -> Unit
) {
    val context = LocalContext.current
    var isRestoring by remember { mutableStateOf(false) }
    var jsonTextInput by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf(0) } // 0: File Picker, 1: Paste JSON

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            isRestoring = true
            viewModel.restoreDatabaseFromUri(context, uri) { result ->
                isRestoring = false
                onRestored(result)
            }
        }
    }

    AlertDialog(
        onDismissRequest = { if (!isRestoring) onDismiss() },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Restore, contentDescription = null, tint = BrandPurpleAccent)
                Spacer(modifier = Modifier.width(8.dp))
                Text(Localization.get("restore_database", lang), fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Restore workers and attendance history from a previously exported .json backup file:",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Toggle tabs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TabPillButton(
                        text = "📁 Select File",
                        isSelected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        modifier = Modifier.weight(1f)
                    )
                    TabPillButton(
                        text = "📋 Paste JSON",
                        isSelected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (selectedTab == 0) {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudDownload,
                                contentDescription = null,
                                tint = BrandPurpleAccent,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Choose .json Backup File",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Browse storage or Downloads folder",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { filePickerLauncher.launch("application/json") },
                                enabled = !isRestoring,
                                colors = ButtonDefaults.buttonColors(containerColor = BrandPurpleAccent),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Browse Device Files")
                            }
                        }
                    }
                } else {
                    OutlinedTextField(
                        value = jsonTextInput,
                        onValueChange = { jsonTextInput = it },
                        label = { Text("Paste JSON Backup Content") },
                        placeholder = { Text("{\"appName\": \"Sumit Attendance System\", ...}") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp),
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp
                        )
                    )
                }
            }
        },
        confirmButton = {
            if (selectedTab == 1) {
                Button(
                    onClick = {
                        if (jsonTextInput.isNotBlank()) {
                            isRestoring = true
                            viewModel.restoreDatabaseFromJson(context, jsonTextInput) { result ->
                                isRestoring = false
                                onRestored(result)
                            }
                        } else {
                            Toast.makeText(context, "Please paste JSON backup text", Toast.LENGTH_SHORT).show()
                        }
                    },
                    enabled = !isRestoring && jsonTextInput.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandPurpleAccent),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    if (isRestoring) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Restoring...")
                    } else {
                        Text("Apply & Restore")
                    }
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isRestoring
            ) {
                Text("Close")
            }
        }
    )
}

@Composable
fun TabPillButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) BrandPurpleAccent else MaterialTheme.colorScheme.surfaceVariant,
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            Text(
                text = text,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun BackupExportSuccessDialog(
    file: File,
    format: ExportFormat,
    lang: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val fileSizeKb = maxOf(1L, file.length() / 1024L)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE8F5E9)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF2E7D32),
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text("Export Successful!", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Your database records have been exported and saved locally:",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(10.dp))

                // File metadata card
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (format == ExportFormat.JSON) Icons.Default.DataObject else Icons.Default.TableChart,
                                contentDescription = null,
                                tint = if (format == ExportFormat.JSON) Color(0xFF2E7D32) else BrandBluePrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = file.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                maxLines = 1
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Format: ${format.name.replace("_", " ")}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(text = "Size: $fileSizeKb KB", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Direct Actions
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 1. Save to Device Public Downloads
                    Button(
                        onClick = {
                            DatabaseBackupManager.copyFileToPublicDownloads(context, file)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().testTag("btn_save_to_downloads")
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(Localization.get("save_to_local_storage", lang), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    // 2. Share to WhatsApp
                    Button(
                        onClick = {
                            DatabaseBackupManager.shareToWhatsApp(context, file, format.displayName)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().testTag("btn_share_backup_whatsapp")
                    ) {
                        com.example.ui.components.WhatsAppIcon(tint = Color.White, size = 16.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Share via WhatsApp", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    // 3. Share / Save via standard Android system picker
                    OutlinedButton(
                        onClick = {
                            DatabaseBackupManager.shareExportFile(context, file, format.mimeType, format.displayName)
                        },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().testTag("btn_share_backup_system")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Share / Save to Files & Cloud", fontSize = 12.sp)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done", fontWeight = FontWeight.Bold)
            }
        }
    )
}
