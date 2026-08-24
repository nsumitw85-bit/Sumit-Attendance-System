package com.example.util

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.AppRepository
import com.example.data.SalaryWageConfig
import com.example.data.model.AppSettingEntity
import com.example.data.model.AttendanceEntity
import com.example.data.model.WorkerEntity
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class ExportFormat(val extension: String, val mimeType: String, val displayName: String) {
    JSON("json", "application/json", "JSON (Complete Database Dump)"),
    CSV_ATTENDANCE("csv", "text/csv", "CSV (All Attendance Records)"),
    CSV_WORKERS("csv", "text/csv", "CSV (Workers Master Directory)"),
    CSV_COMPLETE("csv", "text/csv", "CSV (Complete System Data)"),
    CSV_MONTHLY_PAYROLL("csv", "text/csv", "CSV (Monthly Payroll Muster)")
}

data class AutoBackupConfig(
    val isEnabled: Boolean = true,
    val format: ExportFormat = ExportFormat.JSON,
    val retentionDays: Int = 7,
    val lastBackupDate: String? = null,
    val lastBackupTime: String? = null,
    val lastStatus: String? = null
)

data class StoredBackupFile(
    val file: File,
    val fileName: String,
    val sizeKb: Long,
    val lastModifiedTimestamp: Long,
    val formattedDate: String,
    val format: ExportFormat
)

data class DatabaseSummaryStats(
    val totalWorkers: Int,
    val activeWorkers: Int,
    val totalAttendanceRecords: Int,
    val distinctDatesCount: Int,
    val distinctMonthsCount: Int,
    val estimatedDbSizeKb: Long,
    val lastBackupDate: String?
)

data class RestoreResult(
    val success: Boolean,
    val message: String,
    val restoredWorkers: Int = 0,
    val restoredAttendance: Int = 0,
    val restoredSettings: Int = 0
)

data class AutoBackupExecutionResult(
    val success: Boolean,
    val file: File?,
    val message: String,
    val timestamp: String
)

object DatabaseBackupManager {

    private const val TAG = "DatabaseBackup"

    // -------------------------------------------------------------------------
    // 1. Database Stats & Metrics Calculation
    // -------------------------------------------------------------------------

    suspend fun getDatabaseSummaryStats(context: Context, repository: AppRepository): DatabaseSummaryStats {
        return try {
            val allWorkers = repository.getAllWorkersSnapshot()
            val allRecords = repository.getAllRecordsSnapshot()
            val lastBackup = repository.getSetting("last_db_backup_timestamp")

            val distinctDates = allRecords.map { it.date }.distinct().size
            val distinctMonths = allRecords.map { it.date.take(7) }.filter { it.length == 7 }.distinct().size

            // Approximate size
            val workerBytes = allWorkers.size * 256L
            val attendanceBytes = allRecords.size * 128L
            val estimatedKb = maxOf(1L, (workerBytes + attendanceBytes + 1024L) / 1024L)

            DatabaseSummaryStats(
                totalWorkers = allWorkers.size,
                activeWorkers = allWorkers.count { it.isActive },
                totalAttendanceRecords = allRecords.size,
                distinctDatesCount = distinctDates,
                distinctMonthsCount = distinctMonths,
                estimatedDbSizeKb = estimatedKb,
                lastBackupDate = lastBackup
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error calculating DB stats", e)
            DatabaseSummaryStats(0, 0, 0, 0, 0, 0L, null)
        }
    }

    // -------------------------------------------------------------------------
    // 2. JSON Backup Export (Complete Database Snapshot)
    // -------------------------------------------------------------------------

    suspend fun generateBackupJson(repository: AppRepository): String {
        val allWorkers = repository.getAllWorkersSnapshot()
        val allRecords = repository.getAllRecordsSnapshot()
        val allSettings = repository.getAllSettingsSnapshot()

        val root = JSONObject()
        root.put("appName", "Sumit Attendance System")
        root.put("appVersion", "2.0")
        root.put("schemaVersion", 2)
        root.put("department", "Sanitation Department")
        val timestampStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH).format(Date())
        root.put("exportTimestamp", timestampStr)
        root.put("totalWorkersCount", allWorkers.size)
        root.put("totalAttendanceRecordsCount", allRecords.size)

        // Workers Array
        val workersArray = JSONArray()
        for (w in allWorkers) {
            val obj = JSONObject()
            obj.put("id", w.id)
            obj.put("workerCode", w.workerCode)
            obj.put("name", w.name)
            obj.put("phone", w.phone)
            obj.put("roleCategory", w.roleCategory)
            obj.put("avatarColorHex", w.avatarColorHex)
            obj.put("photoUri", w.photoUri)
            obj.put("isActive", w.isActive)
            obj.put("createdAt", w.createdAt)
            workersArray.put(obj)
        }
        root.put("workers", workersArray)

        // Attendance Array
        val attArray = JSONArray()
        for (a in allRecords) {
            val obj = JSONObject()
            obj.put("id", a.id)
            obj.put("date", a.date)
            obj.put("workerId", a.workerId)
            obj.put("status", a.status)
            obj.put("timestamp", a.timestamp)
            obj.put("notes", a.notes)
            attArray.put(obj)
        }
        root.put("attendance", attArray)

        // Settings Array
        val settingsArray = JSONArray()
        for (s in allSettings) {
            val obj = JSONObject()
            obj.put("key", s.key)
            obj.put("value", s.value)
            settingsArray.put(obj)
        }
        root.put("settings", settingsArray)

        return root.toString(2)
    }

    suspend fun exportJsonBackupFile(context: Context, repository: AppRepository): File? {
        return try {
            val json = generateBackupJson(repository)
            val dir = getExportDirectory(context)
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(Date())
            val file = File(dir, "SumitAttendance_DatabaseBackup_$timeStamp.json")

            FileOutputStream(file).use { it.write(json.toByteArray(Charsets.UTF_8)) }

            // Record timestamp in settings
            val formattedNow = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.ENGLISH).format(Date())
            repository.saveSetting("last_db_backup_timestamp", formattedNow)

            file
        } catch (e: Exception) {
            Log.e(TAG, "Failed to export JSON backup file", e)
            null
        }
    }

    // -------------------------------------------------------------------------
    // 3. CSV Exports (Attendance, Workers, Complete Master, Monthly Payroll)
    // -------------------------------------------------------------------------

    suspend fun generateAttendanceCsvString(repository: AppRepository): String {
        val workers = repository.getAllWorkersSnapshot().associateBy { it.id }
        val records = repository.getAllRecordsSnapshot().sortedWith(compareByDescending<AttendanceEntity> { it.date }.thenBy { it.workerId })

        val sb = StringBuilder()
        sb.append("Record_ID,Date,Worker_ID,Worker_Code,Worker_Name,Role_Category,Status_Code,Status_Description,Logged_Timestamp,Remarks\n")

        for (rec in records) {
            val worker = workers[rec.workerId]
            val wCode = worker?.workerCode ?: "W-${rec.workerId}"
            val wName = worker?.name ?: "Unknown Worker"
            val wRole = worker?.roleCategory ?: "Sanitation Staff"
            val statusDesc = when (rec.status) {
                "P" -> "Present (1.0 Day)"
                "A" -> "Absent (0.0 Day)"
                "H" -> "Half Day (0.5 Day)"
                "D" -> "Double Duty (2.0 Days)"
                else -> rec.status
            }
            val loggedTimeStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH).format(Date(rec.timestamp))

            sb.append(rec.id).append(",")
            sb.append(escapeCsv(rec.date)).append(",")
            sb.append(rec.workerId).append(",")
            sb.append(escapeCsv(wCode)).append(",")
            sb.append(escapeCsv(wName)).append(",")
            sb.append(escapeCsv(wRole)).append(",")
            sb.append(escapeCsv(rec.status)).append(",")
            sb.append(escapeCsv(statusDesc)).append(",")
            sb.append(escapeCsv(loggedTimeStr)).append(",")
            sb.append(escapeCsv(rec.notes)).append("\n")
        }
        return sb.toString()
    }

    suspend fun exportAttendanceRecordsCsv(context: Context, repository: AppRepository): File? {
        return try {
            val csvContent = generateAttendanceCsvString(repository)
            val dir = getExportDirectory(context)
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(Date())
            val file = File(dir, "SumitAttendance_AttendanceRecords_$timeStamp.csv")

            FileOutputStream(file).use { it.write(csvContent.toByteArray(Charsets.UTF_8)) }
            file
        } catch (e: Exception) {
            Log.e(TAG, "Failed to export Attendance CSV", e)
            null
        }
    }

    suspend fun exportWorkersMasterCsv(context: Context, repository: AppRepository): File? {
        return try {
            val workers = repository.getAllWorkersSnapshot().sortedBy { it.id }

            val sb = StringBuilder()
            sb.append("Worker_ID,Worker_Code,Full_Name,Phone_Number,Role_Category,Active_Status,Registration_Date\n")

            for (w in workers) {
                val regDateStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH).format(Date(w.createdAt))
                sb.append(w.id).append(",")
                sb.append(escapeCsv(w.workerCode)).append(",")
                sb.append(escapeCsv(w.name)).append(",")
                sb.append(escapeCsv(w.phone)).append(",")
                sb.append(escapeCsv(w.roleCategory)).append(",")
                sb.append(if (w.isActive) "Active" else "Inactive").append(",")
                sb.append(escapeCsv(regDateStr)).append("\n")
            }

            val dir = getExportDirectory(context)
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(Date())
            val file = File(dir, "SumitAttendance_WorkersMaster_$timeStamp.csv")

            FileOutputStream(file).use { it.write(sb.toString().toByteArray(Charsets.UTF_8)) }
            file
        } catch (e: Exception) {
            Log.e(TAG, "Failed to export Workers CSV", e)
            null
        }
    }

    suspend fun exportCompleteDatabaseCsv(context: Context, repository: AppRepository): File? {
        return try {
            val workers = repository.getAllWorkersSnapshot()
            val attendance = repository.getAllRecordsSnapshot()
            val workersMap = workers.associateBy { it.id }

            val sb = StringBuilder()
            sb.append("Data_Type,ID,Code_or_Date,Name_or_WorkerID,Phone_or_Status,Role_or_Description,Timestamp_or_Active,Notes_or_Extra\n")

            // Workers Section
            for (w in workers) {
                sb.append("WORKER,")
                sb.append(w.id).append(",")
                sb.append(escapeCsv(w.workerCode)).append(",")
                sb.append(escapeCsv(w.name)).append(",")
                sb.append(escapeCsv(w.phone)).append(",")
                sb.append(escapeCsv(w.roleCategory)).append(",")
                sb.append(if (w.isActive) "ACTIVE" else "INACTIVE").append(",")
                sb.append(escapeCsv("Created: ${SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(Date(w.createdAt))}")).append("\n")
            }

            // Attendance Section
            for (a in attendance) {
                val worker = workersMap[a.workerId]
                sb.append("ATTENDANCE,")
                sb.append(a.id).append(",")
                sb.append(escapeCsv(a.date)).append(",")
                sb.append(escapeCsv("${worker?.name ?: "Worker"} (ID:${a.workerId})")).append(",")
                sb.append(escapeCsv(a.status)).append(",")
                sb.append(escapeCsv(when (a.status) { "P" -> "Present"; "A" -> "Absent"; "H" -> "Half Day"; "D" -> "Double Duty"; else -> a.status })).append(",")
                sb.append(escapeCsv(SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ENGLISH).format(Date(a.timestamp)))).append(",")
                sb.append(escapeCsv(a.notes)).append("\n")
            }

            val dir = getExportDirectory(context)
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(Date())
            val file = File(dir, "SumitAttendance_CompleteSystemBackup_$timeStamp.csv")

            FileOutputStream(file).use { it.write(sb.toString().toByteArray(Charsets.UTF_8)) }
            file
        } catch (e: Exception) {
            Log.e(TAG, "Failed to export Complete CSV", e)
            null
        }
    }

    suspend fun exportMonthlyPayrollCsv(
        context: Context,
        repository: AppRepository,
        yearMonth: String,
        config: SalaryWageConfig
    ): File? {
        return try {
            val workers = repository.getActiveWorkersSnapshot().sortedBy { it.name }
            val monthRecords = repository.getAttendanceForMonthSnapshot(yearMonth).groupBy { it.workerId }
            val allSettings = repository.getAllSettingsSnapshot().associate { it.key to it.value }

            val sb = StringBuilder()
            sb.append("Muster_Month,Worker_ID,Worker_Code,Worker_Name,Role_Category,Present_Count,HalfDay_Count,DoubleDuty_Count,Absent_Count,Total_Man_Days,Daily_Rate_Rs,Gross_Earnings_Rs,Advance_Deduction_Rs,Bonus_Allowance_Rs,Net_Payable_Rs,Payment_Status,Payment_Notes\n")

            for (w in workers) {
                val recs = monthRecords[w.id] ?: emptyList()
                val p = recs.count { it.status == "P" }
                val h = recs.count { it.status == "H" }
                val d = recs.count { it.status == "D" }
                val a = recs.count { it.status == "A" }

                val adjSetting = allSettings["payroll_adj_${yearMonth}_${w.id}"]
                var customWage: Double? = null
                var adv = 0.0
                var bonus = 0.0
                var status = "PENDING"
                var notes = ""

                if (adjSetting != null) {
                    val parts = adjSetting.split("|")
                    adv = parts.getOrNull(0)?.toDoubleOrNull() ?: 0.0
                    bonus = parts.getOrNull(1)?.toDoubleOrNull() ?: 0.0
                    status = parts.getOrNull(2) ?: "PENDING"
                    notes = parts.getOrNull(3) ?: ""
                    customWage = parts.getOrNull(4)?.toDoubleOrNull()
                }

                val dayRate = customWage ?: config.calculateDayRate(w.roleCategory)
                val halfRate = config.calculateHalfDayRate(w.roleCategory)
                val doubleRate = config.calculateDoubleDutyRate(w.roleCategory)

                val manDays = (p * 1.0) + (h * 0.5) + (d * 2.0)
                val gross = (p * dayRate) + (h * halfRate) + (d * doubleRate)
                val netPay = maxOf(0.0, gross + bonus - adv)

                sb.append(escapeCsv(yearMonth)).append(",")
                sb.append(w.id).append(",")
                sb.append(escapeCsv(w.workerCode)).append(",")
                sb.append(escapeCsv(w.name)).append(",")
                sb.append(escapeCsv(w.roleCategory)).append(",")
                sb.append(p).append(",")
                sb.append(h).append(",")
                sb.append(d).append(",")
                sb.append(a).append(",")
                sb.append(String.format(Locale.ENGLISH, "%.1f", manDays)).append(",")
                sb.append(dayRate.toInt()).append(",")
                sb.append(gross.toInt()).append(",")
                sb.append(adv.toInt()).append(",")
                sb.append(bonus.toInt()).append(",")
                sb.append(netPay.toInt()).append(",")
                sb.append(escapeCsv(status)).append(",")
                sb.append(escapeCsv(notes)).append("\n")
            }

            val dir = getExportDirectory(context)
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(Date())
            val file = File(dir, "SumitAttendance_PayrollMuster_${yearMonth}_$timeStamp.csv")

            FileOutputStream(file).use { it.write(sb.toString().toByteArray(Charsets.UTF_8)) }
            file
        } catch (e: Exception) {
            Log.e(TAG, "Failed to export Monthly Payroll CSV", e)
            null
        }
    }

    // -------------------------------------------------------------------------
    // 4. Restore & Import Database Snapshot
    // -------------------------------------------------------------------------

    suspend fun restoreDatabaseFromJson(
        repository: AppRepository,
        jsonString: String
    ): RestoreResult {
        return try {
            val root = JSONObject(jsonString)
            val workersArray = root.optJSONArray("workers") ?: JSONArray()
            val attArray = root.optJSONArray("attendance") ?: JSONArray()
            val settingsArray = root.optJSONArray("settings") ?: JSONArray()

            val workersToInsert = mutableListOf<WorkerEntity>()
            for (i in 0 until workersArray.length()) {
                val obj = workersArray.getJSONObject(i)
                workersToInsert.add(
                    WorkerEntity(
                        id = obj.optInt("id", 0),
                        workerCode = obj.optString("workerCode", "SAN-${100 + i}"),
                        name = obj.optString("name", "Worker"),
                        phone = obj.optString("phone", ""),
                        roleCategory = obj.optString("roleCategory", "Broom Worker"),
                        avatarColorHex = obj.optString("avatarColorHex", "#1565C0"),
                        photoUri = obj.optString("photoUri", ""),
                        isActive = obj.optBoolean("isActive", true),
                        createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                    )
                )
            }

            val attendanceToInsert = mutableListOf<AttendanceEntity>()
            for (i in 0 until attArray.length()) {
                val obj = attArray.getJSONObject(i)
                attendanceToInsert.add(
                    AttendanceEntity(
                        id = obj.optInt("id", 0),
                        date = obj.optString("date", ""),
                        workerId = obj.optInt("workerId", 0),
                        status = obj.optString("status", "P"),
                        timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                        notes = obj.optString("notes", "")
                    )
                )
            }

            val settingsToInsert = mutableListOf<AppSettingEntity>()
            for (i in 0 until settingsArray.length()) {
                val obj = settingsArray.getJSONObject(i)
                val key = obj.optString("key", "")
                val value = obj.optString("value", "")
                if (key.isNotBlank()) {
                    settingsToInsert.add(AppSettingEntity(key, value))
                }
            }

            if (workersToInsert.isNotEmpty()) {
                repository.insertWorkers(workersToInsert)
            }

            if (attendanceToInsert.isNotEmpty()) {
                repository.saveAllAttendance(attendanceToInsert)
            }

            if (settingsToInsert.isNotEmpty()) {
                repository.saveAllSettings(settingsToInsert)
            }

            RestoreResult(
                success = true,
                message = "Database restored successfully!",
                restoredWorkers = workersToInsert.size,
                restoredAttendance = attendanceToInsert.size,
                restoredSettings = settingsToInsert.size
            )
        } catch (e: Exception) {
            Log.e(TAG, "Restore error", e)
            RestoreResult(
                success = false,
                message = "Failed to parse and restore database backup: ${e.localizedMessage}"
            )
        }
    }

    suspend fun readJsonFromUri(context: Context, uri: Uri): String? {
        return try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            inputStream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }
        } catch (e: Exception) {
            Log.e(TAG, "Error reading JSON from Uri", e)
            null
        }
    }

    // -------------------------------------------------------------------------
    // 5. File Sharing & Local Storage Copy Helpers
    // -------------------------------------------------------------------------

    fun shareExportFile(context: Context, file: File, mimeType: String, title: String) {
        try {
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, title)
                putExtra(
                    Intent.EXTRA_TEXT,
                    "$title\nFile: ${file.name}\nGenerated from Sumit Attendance System.\nKeep safe for local storage and records."
                )
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(shareIntent, "Save or Share Database Backup via...")
            context.startActivity(chooser)
        } catch (e: Exception) {
            Log.e(TAG, "Error sharing export file", e)
            Toast.makeText(context, "Sharing failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun shareToWhatsApp(context: Context, file: File, title: String) {
        try {
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = if (file.name.endsWith(".csv")) "text/csv" else "application/json"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_TEXT, "📊 $title\n📁 File: ${file.name}")
                setPackage("com.whatsapp")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            // Fallback to normal share
            shareExportFile(context, file, if (file.name.endsWith(".csv")) "text/csv" else "application/json", title)
        }
    }

    fun copyFileToPublicDownloads(context: Context, file: File): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, file.name)
                    put(MediaStore.MediaColumns.MIME_TYPE, if (file.name.endsWith(".csv")) "text/csv" else "application/json")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/SumitAttendance")
                }
                val uri = context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                if (uri != null) {
                    context.contentResolver.openOutputStream(uri)?.use { out ->
                        FileInputStream(file).use { input ->
                            input.copyTo(out)
                        }
                    }
                    Toast.makeText(context, "Saved to Downloads/SumitAttendance/${file.name}", Toast.LENGTH_LONG).show()
                    return true
                }
            }
            // For older SDKs or fallback: file is stored in app documents/cache
            Toast.makeText(context, "File saved locally: ${file.name}", Toast.LENGTH_LONG).show()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error copying to public downloads", e)
            false
        }
    }

    // -------------------------------------------------------------------------
    // 6. Automatic Daily Local Backup Engine
    // -------------------------------------------------------------------------

    fun getAutoBackupDirectory(context: Context): File {
        val dir = File(context.filesDir, "auto_backups")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    suspend fun loadAutoBackupConfig(repository: AppRepository): AutoBackupConfig {
        return try {
            val isEnabled = (repository.getSetting("auto_backup_enabled") ?: "true") == "true"
            val formatStr = repository.getSetting("auto_backup_format") ?: ExportFormat.JSON.name
            val format = try { ExportFormat.valueOf(formatStr) } catch (e: Exception) { ExportFormat.JSON }
            val retention = repository.getSetting("auto_backup_retention_days")?.toIntOrNull() ?: 7
            val lastDate = repository.getSetting("auto_backup_last_date")
            val lastTime = repository.getSetting("auto_backup_last_time")
            val lastStatus = repository.getSetting("auto_backup_last_status")

            AutoBackupConfig(
                isEnabled = isEnabled,
                format = format,
                retentionDays = retention,
                lastBackupDate = lastDate,
                lastBackupTime = lastTime,
                lastStatus = lastStatus
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error loading auto backup config", e)
            AutoBackupConfig()
        }
    }

    suspend fun saveAutoBackupConfig(repository: AppRepository, config: AutoBackupConfig) {
        try {
            repository.saveSetting("auto_backup_enabled", config.isEnabled.toString())
            repository.saveSetting("auto_backup_format", config.format.name)
            repository.saveSetting("auto_backup_retention_days", config.retentionDays.toString())
            if (config.lastBackupDate != null) repository.saveSetting("auto_backup_last_date", config.lastBackupDate)
            if (config.lastBackupTime != null) repository.saveSetting("auto_backup_last_time", config.lastBackupTime)
            if (config.lastStatus != null) repository.saveSetting("auto_backup_last_status", config.lastStatus)
        } catch (e: Exception) {
            Log.e(TAG, "Error saving auto backup config", e)
        }
    }

    fun getStoredAutoBackups(context: Context): List<StoredBackupFile> {
        val dir = getAutoBackupDirectory(context)
        val files = dir.listFiles() ?: return emptyList()
        val dateFormatter = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.ENGLISH)

        return files.filter { it.isFile && (it.name.endsWith(".json") || it.name.endsWith(".csv")) }
            .map { file ->
                val sizeKb = maxOf(1L, file.length() / 1024L)
                val format = if (file.name.endsWith(".csv")) ExportFormat.CSV_ATTENDANCE else ExportFormat.JSON
                StoredBackupFile(
                    file = file,
                    fileName = file.name,
                    sizeKb = sizeKb,
                    lastModifiedTimestamp = file.lastModified(),
                    formattedDate = dateFormatter.format(Date(file.lastModified())),
                    format = format
                )
            }
            .sortedByDescending { it.lastModifiedTimestamp }
    }

    suspend fun performAutoDailyBackup(
        context: Context,
        repository: AppRepository,
        isManualTrigger: Boolean = false
    ): AutoBackupExecutionResult {
        return try {
            val config = loadAutoBackupConfig(repository)
            if (!config.isEnabled && !isManualTrigger) {
                return AutoBackupExecutionResult(false, null, "Automatic daily backup is disabled", "")
            }

            val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(Date())
            // If already backed up today and not manually triggered, skip
            if (!isManualTrigger && config.lastBackupDate == todayDate) {
                return AutoBackupExecutionResult(true, null, "Already backed up today ($todayDate)", config.lastBackupTime ?: "")
            }

            val timestampDisplay = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.ENGLISH).format(Date())
            val fileDateStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(Date())
            val autoDir = getAutoBackupDirectory(context)

            val generatedFile: File = when (config.format) {
                ExportFormat.JSON -> {
                    val jsonString = generateBackupJson(repository)
                    val targetFile = File(autoDir, "SumitAttendance_AutoBackup_${fileDateStamp}.json")
                    FileOutputStream(targetFile).use { out ->
                        out.write(jsonString.toByteArray(Charsets.UTF_8))
                    }
                    targetFile
                }
                ExportFormat.CSV_ATTENDANCE, ExportFormat.CSV_COMPLETE, ExportFormat.CSV_WORKERS, ExportFormat.CSV_MONTHLY_PAYROLL -> {
                    val csvString = generateAttendanceCsvString(repository)
                    val targetFile = File(autoDir, "SumitAttendance_AutoBackup_${fileDateStamp}.csv")
                    FileOutputStream(targetFile).use { out ->
                        out.write(csvString.toByteArray(Charsets.UTF_8))
                    }
                    targetFile
                }
            }

            // Also silently copy to Public Downloads for external visibility and permanence
            copyToPublicAutoBackupFolder(context, generatedFile)

            // Prune backups beyond retention policy
            pruneOldBackups(context, config.retentionDays)

            val sizeKb = maxOf(1L, generatedFile.length() / 1024L)
            val allRecordsCount = repository.getAllRecordsSnapshot().size
            val statusMsg = "Success ($sizeKb KB, $allRecordsCount records saved)"

            // Update Repository Settings
            val updatedConfig = config.copy(
                lastBackupDate = todayDate,
                lastBackupTime = timestampDisplay,
                lastStatus = statusMsg
            )
            saveAutoBackupConfig(repository, updatedConfig)
            repository.saveSetting("last_db_backup_timestamp", timestampDisplay)

            Log.i(TAG, "Auto backup completed successfully: ${generatedFile.name}")
            AutoBackupExecutionResult(
                success = true,
                file = generatedFile,
                message = statusMsg,
                timestamp = timestampDisplay
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error performing auto backup", e)
            val errorMsg = "Failed: ${e.localizedMessage ?: "Unknown error"}"
            repository.saveSetting("auto_backup_last_status", errorMsg)
            AutoBackupExecutionResult(false, null, errorMsg, "")
        }
    }

    private fun copyToPublicAutoBackupFolder(context: Context, file: File) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, file.name)
                    put(MediaStore.MediaColumns.MIME_TYPE, if (file.name.endsWith(".csv")) "text/csv" else "application/json")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/SumitAttendance/AutoBackups")
                }
                val uri = context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                if (uri != null) {
                    context.contentResolver.openOutputStream(uri)?.use { out ->
                        FileInputStream(file).use { input ->
                            input.copyTo(out)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Optional public auto backup copy failed: ${e.message}")
        }
    }

    private fun pruneOldBackups(context: Context, retentionDays: Int) {
        try {
            val dir = getAutoBackupDirectory(context)
            val files = dir.listFiles()?.filter { it.isFile } ?: return
            val retentionMillis = retentionDays.toLong() * 24L * 60L * 60L * 1000L
            val threshold = System.currentTimeMillis() - retentionMillis

            for (f in files) {
                if (f.lastModified() < threshold) {
                    val deleted = f.delete()
                    if (deleted) {
                        Log.d(TAG, "Pruned old auto backup: ${f.name}")
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error pruning old backups", e)
        }
    }

    suspend fun restoreFromStoredFile(repository: AppRepository, file: File): RestoreResult {
        return try {
            val json = file.readText(Charsets.UTF_8)
            restoreDatabaseFromJson(repository, json)
        } catch (e: Exception) {
            RestoreResult(false, "Could not read backup file: ${e.message}")
        }
    }

    fun deleteStoredBackupFile(file: File): Boolean {
        return try {
            file.delete()
        } catch (e: Exception) {
            false
        }
    }

    private fun getExportDirectory(context: Context): File {
        val dir = File(context.cacheDir, "reports")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    private fun escapeCsv(value: String): String {
        var str = value
        if (str.contains(",") || str.contains("\"") || str.contains("\n") || str.contains("\r")) {
            str = str.replace("\"", "\"\"")
            return "\"$str\""
        }
        return str
    }
}
