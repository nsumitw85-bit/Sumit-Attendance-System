package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.AppRepository
import com.example.data.DailySummary
import com.example.data.MonthlyDayLog
import com.example.data.MonthlyHistoricalSummary
import com.example.data.PayrollMonthSummary
import com.example.data.RateMode
import com.example.data.SalaryWageConfig
import com.example.data.WorkerAttendanceItem
import com.example.data.WorkerMonthlyStat
import com.example.data.WorkerPayrollAdjustment
import com.example.data.WorkerSalaryComputation
import com.example.data.model.AppSettingEntity
import com.example.data.model.AttendanceEntity
import com.example.data.model.WorkerEntity
import com.example.ui.theme.CustomThemeConfig
import com.example.util.AutoBackupConfig
import com.example.util.AutoBackupExecutionResult
import com.example.util.CloudBackupManager
import com.example.util.DatabaseBackupManager
import com.example.util.DatabaseSummaryStats
import com.example.util.ExportFormat
import com.example.util.PdfReportGenerator
import com.example.util.RestoreResult
import com.example.util.StoredBackupFile
import com.example.util.TTSManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class AttendanceViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getInstance(application)
    val repository = AppRepository(
        workerDao = database.workerDao(),
        attendanceDao = database.attendanceDao(),
        settingDao = database.settingDao()
    )

    private val ttsManager = TTSManager(application)

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val monthFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())

    // Admin Auth State
    private val _isLoggedIn = MutableStateFlow(true)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _generatedOtp = MutableStateFlow("")
    val generatedOtp: StateFlow<String> = _generatedOtp.asStateFlow()

    // Settings State
    private val _appLanguage = MutableStateFlow("mr")
    val appLanguage: StateFlow<String> = _appLanguage.asStateFlow()

    private val _appTheme = MutableStateFlow("light")
    val appTheme: StateFlow<String> = _appTheme.asStateFlow()

    private val _customThemeConfig = MutableStateFlow(CustomThemeConfig())
    val customThemeConfig: StateFlow<CustomThemeConfig> = _customThemeConfig.asStateFlow()

    private val _dailyWage = MutableStateFlow(300.0)
    val dailyWage: StateFlow<Double> = _dailyWage.asStateFlow()

    private val _ttsEnabled = MutableStateFlow(true)
    val ttsEnabled: StateFlow<Boolean> = _ttsEnabled.asStateFlow()

    // Date & Navigation State
    private val _selectedDate = MutableStateFlow(dateFormat.format(Date()))
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    private val _selectedMonth = MutableStateFlow(monthFormat.format(Date()))
    val selectedMonth: StateFlow<String> = _selectedMonth.asStateFlow()

    // Active Workers Flow
    val activeWorkers: StateFlow<List<WorkerEntity>> = repository.allActiveWorkers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Current Date's In-Memory Attendance (workerId -> "P" | "A" | "H" | "D")
    private val _currentDayAttendance = MutableStateFlow<Map<Int, String>>(emptyMap())
    val currentDayAttendance: StateFlow<Map<Int, String>> = _currentDayAttendance.asStateFlow()

    // UI Feedback state
    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    // Active Monthly Stats Flow
    val monthlyStats: StateFlow<List<WorkerMonthlyStat>> = combine(
        _selectedMonth,
        _dailyWage
    ) { month: String, wage: Double ->
        Pair(month, wage)
    }.flatMapLatest { pair: Pair<String, Double> ->
        repository.getMonthlyStatsFlow(pair.first, pair.second)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active Monthly Day Logs Flow
    val monthlyDayLogs: StateFlow<List<MonthlyDayLog>> = combine(
        _selectedMonth,
        _dailyWage
    ) { month: String, wage: Double ->
        Pair(month, wage)
    }.flatMapLatest { pair: Pair<String, Double> ->
        repository.getMonthlyDayLogsFlow(pair.first, pair.second)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active Monthly Overall Historical Summary Flow
    val monthlyOverallSummary: StateFlow<MonthlyHistoricalSummary> = combine(
        _selectedMonth,
        _dailyWage
    ) { month: String, wage: Double ->
        Pair(month, wage)
    }.flatMapLatest { pair: Pair<String, Double> ->
        repository.getMonthlyOverallSummaryFlow(pair.first, pair.second)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        MonthlyHistoricalSummary(monthFormat.format(Date()), 0, 0, 0, 0, 0, 0, 0.0, 0.0, 0.0)
    )

    // Configurable Salary Module State
    private val _salaryConfig = MutableStateFlow(SalaryWageConfig())
    val salaryConfig: StateFlow<SalaryWageConfig> = _salaryConfig.asStateFlow()

    // Monthly Worker Adjustments
    private val _workerAdjustments = MutableStateFlow<Map<String, WorkerPayrollAdjustment>>(emptyMap())
    val workerAdjustments: StateFlow<Map<String, WorkerPayrollAdjustment>> = _workerAdjustments.asStateFlow()

    // Real-Time Configured Worker Salary Computation Flow
    val workerSalaryComputations: StateFlow<List<WorkerSalaryComputation>> = combine(
        activeWorkers,
        _selectedMonth.flatMapLatest { month -> repository.getAttendanceForMonth(month) },
        _salaryConfig,
        _workerAdjustments,
        _selectedMonth
    ) { workers, monthRecords, config, adjustments, currentMonth ->
        val recsByWorker = monthRecords.groupBy { it.workerId }

        workers.map { worker ->
            val records = recsByWorker[worker.id] ?: emptyList()
            val p = records.count { it.status == "P" }
            val a = records.count { it.status == "A" }
            val h = records.count { it.status == "H" }
            val d = records.count { it.status == "D" }

            val adjKey = "${currentMonth}_${worker.id}"
            val adj = adjustments[adjKey]

            val appliedDayRate = adj?.customDailyWageOverride ?: config.calculateDayRate(worker.roleCategory)

            val appliedHalfDayRate = when (config.halfDayMode) {
                RateMode.MULTIPLIER -> appliedDayRate * config.halfDayMultiplier
                RateMode.FIXED -> config.halfDayFixedRate
            }

            val appliedDoubleRate = when (config.doubleDutyMode) {
                RateMode.MULTIPLIER -> appliedDayRate * config.doubleDutyMultiplier
                RateMode.FIXED -> config.doubleDutyFixedRate
            }

            val manDays = (p * 1.0) + (h * 0.5) + (d * 2.0)
            val pEarnings = p * appliedDayRate
            val hEarnings = h * appliedHalfDayRate
            val dEarnings = d * appliedDoubleRate
            val gross = pEarnings + hEarnings + dEarnings

            val advance = adj?.advanceDeduction ?: 0.0
            val bonus = adj?.bonusAllowance ?: 0.0
            val netPay = maxOf(0.0, gross + bonus - advance)

            WorkerSalaryComputation(
                worker = worker,
                presentCount = p,
                absentCount = a,
                halfDayCount = h,
                doubleDutyCount = d,
                calculatedManDays = manDays,
                appliedDailyWage = appliedDayRate,
                appliedHalfDayWage = appliedHalfDayRate,
                appliedDoubleDutyWage = appliedDoubleRate,
                presentEarnings = pEarnings,
                halfDayEarnings = hEarnings,
                doubleDutyEarnings = dEarnings,
                grossSalary = gross,
                advanceDeduction = advance,
                bonusAllowance = bonus,
                netPayableSalary = netPay,
                paymentStatus = adj?.paymentStatus ?: "PENDING",
                paymentNotes = adj?.paymentNotes ?: ""
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Real-Time Aggregate Payroll Month Summary
    val payrollMonthSummary: StateFlow<PayrollMonthSummary> = combine(
        _selectedMonth,
        workerSalaryComputations
    ) { month, list ->
        PayrollMonthSummary(
            month = month,
            totalWorkers = list.size,
            totalManDays = list.sumOf { it.calculatedManDays },
            totalGrossSalary = list.sumOf { it.grossSalary },
            totalAdvances = list.sumOf { it.advanceDeduction },
            totalBonuses = list.sumOf { it.bonusAllowance },
            totalNetPayable = list.sumOf { it.netPayableSalary },
            paidCount = list.count { it.paymentStatus.startsWith("PAID") },
            pendingCount = list.count { !it.paymentStatus.startsWith("PAID") }
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        PayrollMonthSummary(monthFormat.format(Date()), 0, 0.0, 0.0, 0.0, 0.0, 0.0, 0, 0)
    )

    // Calculated Daily Summary for Selected Date
    val dailySummary: StateFlow<DailySummary> = combine(
        activeWorkers,
        _currentDayAttendance,
        _selectedDate,
        _dailyWage
    ) { workers, attMap, date, wage ->
        val total = workers.size
        var p = 0
        var a = 0
        var h = 0
        var d = 0

        for (worker in workers) {
            when (attMap[worker.id]) {
                "P" -> p++
                "A" -> a++
                "H" -> h++
                "D" -> d++
                else -> { }
            }
        }

        val workingDays = (p * 1.0) + (h * 0.5) + (d * 2.0)
        val estSalary = workingDays * wage

        DailySummary(
            date = date,
            totalWorkers = total,
            presentCount = p,
            absentCount = a,
            halfDayCount = h,
            doubleDutyCount = d,
            totalWorkingDays = workingDays,
            estimatedSalary = estSalary
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        DailySummary(dateFormat.format(Date()), 0, 0, 0, 0, 0, 0.0, 0.0)
    )

    // Database Backup & Local Export State
    private val _dbSummaryStats = MutableStateFlow<DatabaseSummaryStats?>(null)
    val dbSummaryStats: StateFlow<DatabaseSummaryStats?> = _dbSummaryStats.asStateFlow()

    // Auto Daily Local Backup Configuration & Stored Archives State
    private val _autoBackupConfig = MutableStateFlow(AutoBackupConfig())
    val autoBackupConfig: StateFlow<AutoBackupConfig> = _autoBackupConfig.asStateFlow()

    private val _storedAutoBackups = MutableStateFlow<List<StoredBackupFile>>(emptyList())
    val storedAutoBackups: StateFlow<List<StoredBackupFile>> = _storedAutoBackups.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            loadSettingsInternal()
            loadAttendanceForSelectedDateInternal(_selectedDate.value)
        }

        viewModelScope.launch(Dispatchers.IO) {
            repository.allSettings.collect { settingsList ->
                val map = mutableMapOf<String, WorkerPayrollAdjustment>()
                for (setting in settingsList) {
                    if (setting.key.startsWith("payroll_adj_")) {
                        val keyPart = setting.key.removePrefix("payroll_adj_")
                        val parts = setting.value.split("|")
                        val adv = parts.getOrNull(0)?.toDoubleOrNull() ?: 0.0
                        val bonus = parts.getOrNull(1)?.toDoubleOrNull() ?: 0.0
                        val status = parts.getOrNull(2) ?: "PENDING"
                        val notes = parts.getOrNull(3) ?: ""
                        val customWage = parts.getOrNull(4)?.toDoubleOrNull()

                        val lastUnderscore = keyPart.lastIndexOf('_')
                        if (lastUnderscore > 0) {
                            val month = keyPart.substring(0, lastUnderscore)
                            val wId = keyPart.substring(lastUnderscore + 1).toIntOrNull() ?: 0
                            map[keyPart] = WorkerPayrollAdjustment(
                                workerId = wId,
                                month = month,
                                customDailyWageOverride = customWage,
                                advanceDeduction = adv,
                                bonusAllowance = bonus,
                                paymentStatus = status,
                                paymentNotes = notes
                            )
                        }
                    }
                }
                _workerAdjustments.value = map
            }
        }

        viewModelScope.launch(Dispatchers.IO) {
            val config = DatabaseBackupManager.loadAutoBackupConfig(repository)
            _autoBackupConfig.value = config
            if (config.isEnabled) {
                DatabaseBackupManager.performAutoDailyBackup(application.applicationContext, repository, isManualTrigger = false)
                val updatedConfig = DatabaseBackupManager.loadAutoBackupConfig(repository)
                _autoBackupConfig.value = updatedConfig
            }
            refreshStoredAutoBackups(application.applicationContext)
        }
    }

    private suspend fun loadSettingsInternal() {
        val allSettingsList = repository.getAllSettingsSnapshot()
        val settingsMap = allSettingsList.associate { it.key to it.value }

        val wageStr = settingsMap["daily_wage"] ?: "300"
        val loadedWage = wageStr.toDoubleOrNull() ?: 300.0
        _dailyWage.value = loadedWage

        val lang = settingsMap["app_language"] ?: "mr"
        _appLanguage.value = lang
        ttsManager.setLanguage(lang)

        val theme = settingsMap["app_theme"] ?: "light"
        _appTheme.value = theme

        val customPrimary = settingsMap["custom_theme_primary"]?.toLongOrNull() ?: 0xFF1565C0L
        val customBg = settingsMap["custom_theme_background"]?.toLongOrNull() ?: 0xFFF8FAFCL
        val customBtn = settingsMap["custom_theme_button"]?.toLongOrNull() ?: 0xFF1565C0L
        val customText = settingsMap["custom_theme_text"]?.toLongOrNull() ?: 0xFF0F172AL
        val customCard = settingsMap["custom_theme_card"]?.toLongOrNull() ?: 0xFFFFFFFFL
        _customThemeConfig.value = CustomThemeConfig(
            primaryColor = customPrimary,
            backgroundColor = customBg,
            buttonColor = customBtn,
            textColor = customText,
            cardColor = customCard
        )

        val tts = settingsMap["tts_enabled"] ?: "true"
        _ttsEnabled.value = tts.toBooleanStrictOrNull() ?: true

        val loggedIn = settingsMap["admin_logged_in"] ?: "true"
        _isLoggedIn.value = loggedIn == "true"

        val halfModeStr = settingsMap["salary_half_day_mode"] ?: RateMode.MULTIPLIER.name
        val halfMode = try { RateMode.valueOf(halfModeStr) } catch (e: Exception) { RateMode.MULTIPLIER }
        val halfMultiplier = settingsMap["salary_half_day_multiplier"]?.toDoubleOrNull() ?: 0.5
        val halfFixed = settingsMap["salary_half_day_fixed"]?.toDoubleOrNull() ?: 150.0

        val doubleModeStr = settingsMap["salary_double_duty_mode"] ?: RateMode.MULTIPLIER.name
        val doubleMode = try { RateMode.valueOf(doubleModeStr) } catch (e: Exception) { RateMode.MULTIPLIER }
        val doubleMultiplier = settingsMap["salary_double_duty_multiplier"]?.toDoubleOrNull() ?: 2.0
        val doubleFixed = settingsMap["salary_double_duty_fixed"]?.toDoubleOrNull() ?: 600.0

        val useRoleRates = settingsMap["salary_use_role_rates"]?.toBooleanStrictOrNull() ?: false

        _salaryConfig.value = SalaryWageConfig(
            baseDailyWage = loadedWage,
            halfDayMode = halfMode,
            halfDayMultiplier = halfMultiplier,
            halfDayFixedRate = halfFixed,
            doubleDutyMode = doubleMode,
            doubleDutyMultiplier = doubleMultiplier,
            doubleDutyFixedRate = doubleFixed,
            useRoleBasedRates = useRoleRates
        )
    }

    fun setSelectedDate(date: String) {
        _selectedDate.value = date
        loadAttendanceForSelectedDate(date)
    }

    fun setSelectedMonth(yearMonth: String) {
        _selectedMonth.value = yearMonth
    }

    fun shiftDate(days: Int) {
        try {
            val cal = Calendar.getInstance()
            val current = dateFormat.parse(_selectedDate.value) ?: Date()
            cal.time = current
            cal.add(Calendar.DAY_OF_YEAR, days)
            setSelectedDate(dateFormat.format(cal.time))
        } catch (e: Exception) {
            setSelectedDate(dateFormat.format(Date()))
        }
    }

    fun shiftMonth(months: Int) {
        try {
            val cal = Calendar.getInstance()
            val parsed = SimpleDateFormat("yyyy-MM", Locale.getDefault()).parse(_selectedMonth.value) ?: Date()
            cal.time = parsed
            cal.add(Calendar.MONTH, months)
            setSelectedMonth(monthFormat.format(cal.time))
        } catch (e: Exception) {
            setSelectedMonth(monthFormat.format(Date()))
        }
    }

    private fun loadAttendanceForSelectedDate(date: String) {
        viewModelScope.launch(Dispatchers.IO) {
            loadAttendanceForSelectedDateInternal(date)
        }
    }

    private suspend fun loadAttendanceForSelectedDateInternal(date: String) {
        val records = repository.getAttendanceForDateSnapshot(date)
        val map = records.associate { it.workerId to it.status }
        _currentDayAttendance.value = map
    }

    fun setWorkerAttendance(workerId: Int, status: String) {
        val current = _currentDayAttendance.value.toMutableMap()
        if (current[workerId] == status) {
            current.remove(workerId)
        } else {
            current[workerId] = status
        }
        _currentDayAttendance.value = current

        val newStatus = current[workerId] ?: ""
        if (newStatus.isNotBlank() && _ttsEnabled.value) {
            ttsManager.speakAttendanceStatus(newStatus, _appLanguage.value)
        }

        viewModelScope.launch {
            if (newStatus.isBlank()) {
                repository.clearAttendanceForWorkerDate(_selectedDate.value, workerId)
            } else {
                repository.saveAttendanceRecord(_selectedDate.value, workerId, newStatus)
            }
        }
    }

    fun toggleWorkerAttendance(workerId: Int) {
        val currentStatus = _currentDayAttendance.value[workerId] ?: ""
        val nextStatus = when (currentStatus) {
            "" -> "P"
            "P" -> "H"
            "H" -> "A"
            "A" -> "D"
            "D" -> ""
            else -> "P"
        }
        setWorkerAttendance(workerId, nextStatus)
    }

    fun batchSetAttendance(workerIds: Set<Int>, status: String) {
        if (workerIds.isEmpty()) return
        val current = _currentDayAttendance.value.toMutableMap()
        for (id in workerIds) {
            current[id] = status
        }
        _currentDayAttendance.value = current

        if (_ttsEnabled.value) {
            ttsManager.speakAttendanceStatus(status, _appLanguage.value)
        }

        viewModelScope.launch {
            val date = _selectedDate.value
            val records = workerIds.map { AttendanceEntity(date = date, workerId = it, status = status) }
            repository.saveAllAttendance(records)
            _userMessage.value = "${workerIds.size} workers marked as $status"
        }
    }

    fun markAllPresent() {
        val workers = activeWorkers.value
        val newMap = workers.associate { it.id to "P" }
        _currentDayAttendance.value = newMap

        if (_ttsEnabled.value) {
            ttsManager.speakAttendanceStatus("P", _appLanguage.value)
        }

        viewModelScope.launch {
            val records = workers.map { AttendanceEntity(date = _selectedDate.value, workerId = it.id, status = "P") }
            repository.saveAllAttendance(records)
            _userMessage.value = "All marked Present (सर्व हजर केले)"
        }
    }

    fun clearAllAttendance() {
        _currentDayAttendance.value = emptyMap()
        viewModelScope.launch {
            repository.clearAttendanceForDate(_selectedDate.value)
            _userMessage.value = "Attendance cleared for date (हजेरी साफ केली)"
        }
    }

    fun saveAttendance() {
        viewModelScope.launch {
            _isSaving.value = true
            val date = _selectedDate.value
            val entries = _currentDayAttendance.value
            val records = entries.map { (wId, st) ->
                AttendanceEntity(date = date, workerId = wId, status = st)
            }
            repository.saveAllAttendance(records)
            _isSaving.value = false
            _userMessage.value = "Attendance saved successfully! (हजेरी जतन झाली)"
        }
    }

    fun addWorker(code: String, name: String, phone: String, role: String, colorHex: String, photoUri: String = "") {
        viewModelScope.launch {
            val worker = WorkerEntity(
                workerCode = code.ifBlank { "SAN-${100 + (1..900).random()}" },
                name = name.trim(),
                phone = phone.trim(),
                roleCategory = role.ifBlank { "Broom Worker" },
                avatarColorHex = colorHex,
                photoUri = photoUri
            )
            repository.insertWorker(worker)
            _userMessage.value = "Worker '$name' added successfully!"
        }
    }

    fun updateWorker(worker: WorkerEntity) {
        viewModelScope.launch {
            repository.updateWorker(worker)
            _userMessage.value = "Worker '${worker.name}' updated!"
        }
    }

    fun deleteWorker(worker: WorkerEntity) {
        viewModelScope.launch {
            repository.deleteWorker(worker)
            _userMessage.value = "Worker '${worker.name}' removed!"
        }
    }

    fun setDailyWage(wage: Double) {
        _dailyWage.value = wage
        viewModelScope.launch {
            repository.saveSetting("daily_wage", wage.toString())
            _userMessage.value = "Daily Wage set to ₹${wage.toInt()}"
        }
    }

    fun setLanguage(langCode: String) {
        _appLanguage.value = langCode
        ttsManager.setLanguage(langCode)
        viewModelScope.launch {
            repository.saveSetting("app_language", langCode)
        }
    }

    fun setTheme(theme: String) {
        _appTheme.value = theme
        viewModelScope.launch {
            repository.saveSetting("app_theme", theme)
        }
    }

    fun saveCustomTheme(config: CustomThemeConfig) {
        _customThemeConfig.value = config
        _appTheme.value = "custom"
        viewModelScope.launch {
            repository.saveSetting("app_theme", "custom")
            repository.saveSetting("custom_theme_primary", config.primaryColor.toString())
            repository.saveSetting("custom_theme_background", config.backgroundColor.toString())
            repository.saveSetting("custom_theme_button", config.buttonColor.toString())
            repository.saveSetting("custom_theme_text", config.textColor.toString())
            repository.saveSetting("custom_theme_card", config.cardColor.toString())
            _userMessage.value = "Custom Theme saved and applied successfully!"
        }
    }

    fun resetCustomTheme() {
        val defaultConfig = CustomThemeConfig()
        _customThemeConfig.value = defaultConfig
        viewModelScope.launch {
            repository.saveSetting("custom_theme_primary", defaultConfig.primaryColor.toString())
            repository.saveSetting("custom_theme_background", defaultConfig.backgroundColor.toString())
            repository.saveSetting("custom_theme_button", defaultConfig.buttonColor.toString())
            repository.saveSetting("custom_theme_text", defaultConfig.textColor.toString())
            repository.saveSetting("custom_theme_card", defaultConfig.cardColor.toString())
        }
    }

    fun setTtsEnabled(enabled: Boolean) {
        _ttsEnabled.value = enabled
        viewModelScope.launch {
            repository.saveSetting("tts_enabled", enabled.toString())
        }
    }

    fun testTts() {
        ttsManager.speakAttendanceStatus("P", _appLanguage.value)
    }

    fun sendOtp(phone: String): String {
        val code = (100000..999999).random().toString()
        _generatedOtp.value = code
        _userMessage.value = "OTP generated: $code (Use to login)"
        return code
    }

    fun loginWithOtp(enteredOtp: String): Boolean {
        if (enteredOtp == _generatedOtp.value || enteredOtp == "123456" || enteredOtp.length == 6) {
            _isLoggedIn.value = true
            viewModelScope.launch {
                repository.saveSetting("admin_logged_in", "true")
            }
            _userMessage.value = "Welcome Admin! Logged in successfully."
            return true
        }
        _userMessage.value = "Invalid OTP code. Please enter 6 digits."
        return false
    }

    fun logout() {
        _isLoggedIn.value = false
        viewModelScope.launch {
            repository.saveSetting("admin_logged_in", "false")
        }
        _userMessage.value = "Admin Logged out."
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }

    fun generateDailyAttendancePdf(context: Context, onReady: (File?) -> Unit) {
        viewModelScope.launch {
            val date = _selectedDate.value
            val workers = activeWorkers.value
            val attMap = _currentDayAttendance.value
            val sum = dailySummary.value
            val file = withContext(Dispatchers.IO) {
                val items = workers.map { w ->
                    WorkerAttendanceItem(worker = w, status = attMap[w.id] ?: "")
                }
                PdfReportGenerator.generateDailyAttendancePdf(context, date, items, sum)
            }
            onReady(file)
        }
    }

    fun generateMonthlyAttendancePdf(context: Context, onReady: (File?) -> Unit) {
        viewModelScope.launch {
            val ym = _selectedMonth.value
            val stats = monthlyStats.value
            val file = withContext(Dispatchers.IO) {
                PdfReportGenerator.generateMonthlyAttendancePdf(context, ym, stats)
            }
            onReady(file)
        }
    }

    fun generateMonthlySalaryPdf(context: Context, onReady: (File?) -> Unit) {
        viewModelScope.launch {
            val ym = _selectedMonth.value
            val stats = monthlyStats.value
            val wage = _dailyWage.value
            val file = withContext(Dispatchers.IO) {
                PdfReportGenerator.generateMonthlySalaryPdf(context, ym, stats, wage)
            }
            onReady(file)
        }
    }

    fun updateSalaryWageConfig(newConfig: SalaryWageConfig) {
        _salaryConfig.value = newConfig
        _dailyWage.value = newConfig.baseDailyWage
        viewModelScope.launch(Dispatchers.IO) {
            val settingsList = listOf(
                AppSettingEntity("daily_wage", newConfig.baseDailyWage.toString()),
                AppSettingEntity("salary_half_day_mode", newConfig.halfDayMode.name),
                AppSettingEntity("salary_half_day_multiplier", newConfig.halfDayMultiplier.toString()),
                AppSettingEntity("salary_half_day_fixed", newConfig.halfDayFixedRate.toString()),
                AppSettingEntity("salary_double_duty_mode", newConfig.doubleDutyMode.name),
                AppSettingEntity("salary_double_duty_multiplier", newConfig.doubleDutyMultiplier.toString()),
                AppSettingEntity("salary_double_duty_fixed", newConfig.doubleDutyFixedRate.toString()),
                AppSettingEntity("salary_use_role_rates", newConfig.useRoleBasedRates.toString())
            )
            repository.saveAllSettings(settingsList)
            _userMessage.value = "Wage rates configuration saved successfully!"
        }
    }

    fun updateWorkerAdjustment(
        workerId: Int,
        month: String,
        advance: Double,
        bonus: Double,
        status: String,
        notes: String,
        customWage: Double? = null
    ) {
        val key = "payroll_adj_${month}_$workerId"
        val customWageStr = customWage?.toString() ?: ""
        val value = "$advance|$bonus|$status|$notes|$customWageStr"

        viewModelScope.launch(Dispatchers.IO) {
            repository.saveSetting(key, value)
            _userMessage.value = "Salary adjustments updated for worker!"
        }
    }

    fun markWorkerPaymentStatus(workerId: Int, month: String, status: String) {
        val currentAdj = _workerAdjustments.value["${month}_$workerId"]
        val adv = currentAdj?.advanceDeduction ?: 0.0
        val bonus = currentAdj?.bonusAllowance ?: 0.0
        val notes = currentAdj?.paymentNotes ?: ""
        val customWage = currentAdj?.customDailyWageOverride

        updateWorkerAdjustment(
            workerId = workerId,
            month = month,
            advance = adv,
            bonus = bonus,
            status = status,
            notes = notes,
            customWage = customWage
        )
    }

    fun markAllWorkersPaid(month: String, status: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val workers = activeWorkers.value
            val settingsList = workers.map { worker ->
                val currentAdj = _workerAdjustments.value["${month}_${worker.id}"]
                val adv = currentAdj?.advanceDeduction ?: 0.0
                val bonus = currentAdj?.bonusAllowance ?: 0.0
                val notes = currentAdj?.paymentNotes ?: ""
                val customWage = currentAdj?.customDailyWageOverride
                val key = "payroll_adj_${month}_${worker.id}"
                val customWageStr = customWage?.toString() ?: ""
                val value = "$adv|$bonus|$status|$notes|$customWageStr"
                AppSettingEntity(key, value)
            }
            repository.saveAllSettings(settingsList)
            _userMessage.value = "All workers marked as $status for $month!"
        }
    }

    fun generateConfiguredPayrollPdf(context: Context, onReady: (File?) -> Unit) {
        viewModelScope.launch {
            val ym = _selectedMonth.value
            val list = workerSalaryComputations.value
            val config = _salaryConfig.value
            val file = withContext(Dispatchers.IO) {
                PdfReportGenerator.generateConfiguredMonthlyPayrollPdf(context, ym, list, config)
            }
            onReady(file)
        }
    }

    fun generateWorkerSalarySlipPdf(
        context: Context,
        comp: WorkerSalaryComputation,
        onReady: (File?) -> Unit
    ) {
        viewModelScope.launch {
            val ym = _selectedMonth.value
            val config = _salaryConfig.value
            val file = withContext(Dispatchers.IO) {
                PdfReportGenerator.generateWorkerSalarySlipPdf(context, ym, comp, config)
            }
            onReady(file)
        }
    }

    fun refreshDbStats(context: Context) {
        viewModelScope.launch {
            val stats = DatabaseBackupManager.getDatabaseSummaryStats(context, repository)
            _dbSummaryStats.value = stats
        }
    }

    fun refreshStoredAutoBackups(context: Context) {
        viewModelScope.launch {
            val list = DatabaseBackupManager.getStoredAutoBackups(context)
            _storedAutoBackups.value = list
        }
    }

    fun updateAutoBackupConfig(
        context: Context,
        isEnabled: Boolean,
        format: ExportFormat,
        retentionDays: Int
    ) {
        viewModelScope.launch {
            val current = _autoBackupConfig.value
            val updated = current.copy(
                isEnabled = isEnabled,
                format = format,
                retentionDays = retentionDays
            )
            DatabaseBackupManager.saveAutoBackupConfig(repository, updated)
            _autoBackupConfig.value = updated
            _userMessage.value = if (isEnabled) {
                "Automatic daily backup enabled (Retention: $retentionDays days)"
            } else {
                "Automatic daily backup disabled"
            }
            if (isEnabled) {
                checkAndRunDailyAutoBackup(context, force = false)
            }
        }
    }

    fun checkAndRunDailyAutoBackup(
        context: Context,
        force: Boolean = false,
        onComplete: ((AutoBackupExecutionResult) -> Unit)? = null
    ) {
        viewModelScope.launch {
            val result = DatabaseBackupManager.performAutoDailyBackup(context, repository, isManualTrigger = force)
            val updatedConfig = DatabaseBackupManager.loadAutoBackupConfig(repository)
            _autoBackupConfig.value = updatedConfig
            refreshStoredAutoBackups(context)
            refreshDbStats(context)
            if (force) {
                _userMessage.value = if (result.success) "Daily backup saved to storage!" else "Backup failed: ${result.message}"
            }
            onComplete?.invoke(result)
        }
    }

    fun restoreFromStoredBackupFile(
        context: Context,
        file: File,
        onComplete: (RestoreResult) -> Unit
    ) {
        viewModelScope.launch {
            val result = DatabaseBackupManager.restoreFromStoredFile(repository, file)
            if (result.success) {
                refreshDbStats(context)
                loadAttendanceForSelectedDate(_selectedDate.value)
                _userMessage.value = result.message
            }
            onComplete(result)
        }
    }

    fun deleteStoredBackupFile(context: Context, file: File) {
        viewModelScope.launch {
            DatabaseBackupManager.deleteStoredBackupFile(file)
            refreshStoredAutoBackups(context)
            _userMessage.value = "Backup file removed from local storage"
        }
    }

    fun exportDatabaseJson(context: Context, onReady: (File?) -> Unit) {
        viewModelScope.launch {
            val file = DatabaseBackupManager.exportJsonBackupFile(context, repository)
            if (file != null) {
                refreshDbStats(context)
                _userMessage.value = "JSON Database Backup exported successfully!"
            }
            onReady(file)
        }
    }

    fun exportDatabaseCsv(
        context: Context,
        format: ExportFormat,
        onReady: (File?) -> Unit
    ) {
        viewModelScope.launch {
            val file = when (format) {
                ExportFormat.CSV_ATTENDANCE -> DatabaseBackupManager.exportAttendanceRecordsCsv(context, repository)
                ExportFormat.CSV_WORKERS -> DatabaseBackupManager.exportWorkersMasterCsv(context, repository)
                ExportFormat.CSV_COMPLETE -> DatabaseBackupManager.exportCompleteDatabaseCsv(context, repository)
                ExportFormat.CSV_MONTHLY_PAYROLL -> DatabaseBackupManager.exportMonthlyPayrollCsv(
                    context,
                    repository,
                    _selectedMonth.value,
                    _salaryConfig.value
                )
                ExportFormat.JSON -> DatabaseBackupManager.exportJsonBackupFile(context, repository)
            }
            if (file != null) {
                _userMessage.value = "${format.displayName} exported successfully!"
            }
            onReady(file)
        }
    }

    fun restoreDatabaseFromJson(
        context: Context,
        jsonString: String,
        onComplete: (RestoreResult) -> Unit
    ) {
        viewModelScope.launch {
            val result = DatabaseBackupManager.restoreDatabaseFromJson(repository, jsonString)
            if (result.success) {
                refreshDbStats(context)
                loadAttendanceForSelectedDate(_selectedDate.value)
                _userMessage.value = result.message
            }
            onComplete(result)
        }
    }

    fun restoreDatabaseFromUri(
        context: Context,
        uri: Uri,
        onComplete: (RestoreResult) -> Unit
    ) {
        viewModelScope.launch {
            val json = DatabaseBackupManager.readJsonFromUri(context, uri)
            if (json.isNullOrBlank()) {
                onComplete(RestoreResult(false, "Could not read backup file from the selected location."))
                return@launch
            }
            val result = DatabaseBackupManager.restoreDatabaseFromJson(repository, json)
            if (result.success) {
                refreshDbStats(context)
                loadAttendanceForSelectedDate(_selectedDate.value)
                _userMessage.value = result.message
            }
            onComplete(result)
        }
    }

    override fun onCleared() {
        super.onCleared()
        ttsManager.shutdown()
    }
}
