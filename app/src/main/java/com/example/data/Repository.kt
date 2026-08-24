package com.example.data

import com.example.data.dao.AttendanceDao
import com.example.data.dao.SettingDao
import com.example.data.dao.WorkerDao
import com.example.data.model.AppSettingEntity
import com.example.data.model.AttendanceEntity
import com.example.data.model.WorkerEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

data class WorkerAttendanceItem(
    val worker: WorkerEntity,
    val status: String // "P", "A", "H", "D" or empty
)

data class WorkerMonthlyStat(
    val worker: WorkerEntity,
    val presentCount: Int,
    val absentCount: Int,
    val halfDayCount: Int,
    val doubleDutyCount: Int,
    val calculatedWorkingDays: Double,
    val dailyWage: Double,
    val finalSalary: Double
)

data class MonthlyDayLog(
    val date: String,
    val totalRecords: Int,
    val presentCount: Int,
    val absentCount: Int,
    val halfDayCount: Int,
    val doubleDutyCount: Int,
    val netWorkingDays: Double,
    val estimatedSalary: Double
)

data class MonthlyHistoricalSummary(
    val month: String,
    val totalLoggedDays: Int,
    val totalActiveWorkers: Int,
    val totalPresent: Int,
    val totalAbsent: Int,
    val totalHalfDay: Int,
    val totalDoubleDuty: Int,
    val totalNetManDays: Double,
    val grandSalary: Double,
    val averageAttendanceRate: Double
)

data class DailySummary(
    val date: String,
    val totalWorkers: Int,
    val presentCount: Int,
    val absentCount: Int,
    val halfDayCount: Int,
    val doubleDutyCount: Int,
    val totalWorkingDays: Double,
    val estimatedSalary: Double
)

class AppRepository(
    private val workerDao: WorkerDao,
    private val attendanceDao: AttendanceDao,
    private val settingDao: SettingDao
) {
    val allActiveWorkers: Flow<List<WorkerEntity>> = workerDao.getAllActiveWorkers()
    val allWorkers: Flow<List<WorkerEntity>> = workerDao.getAllWorkersList()
    val allSettings: Flow<List<AppSettingEntity>> = settingDao.getAllSettings()

    fun getAttendanceForDate(date: String): Flow<List<AttendanceEntity>> =
        attendanceDao.getAttendanceForDate(date)

    suspend fun getAttendanceForDateSnapshot(date: String): List<AttendanceEntity> =
        attendanceDao.getAttendanceForDateSnapshot(date)

    fun getAttendanceForMonth(yearMonth: String): Flow<List<AttendanceEntity>> =
        attendanceDao.getAttendanceForMonth(yearMonth)

    suspend fun getAttendanceForMonthSnapshot(yearMonth: String): List<AttendanceEntity> =
        attendanceDao.getAttendanceForMonthSnapshot(yearMonth)

    fun getAttendanceForWorker(workerId: Int): Flow<List<AttendanceEntity>> =
        attendanceDao.getAttendanceForWorker(workerId)

    fun getSettingFlow(key: String): Flow<AppSettingEntity?> =
        settingDao.getSettingFlow(key)

    suspend fun getSetting(key: String): String? =
        settingDao.getSetting(key)?.value

    suspend fun saveSetting(key: String, value: String) {
        settingDao.setSetting(AppSettingEntity(key, value))
    }

    suspend fun insertWorker(worker: WorkerEntity): Long =
        workerDao.insertWorker(worker)

    suspend fun updateWorker(worker: WorkerEntity) =
        workerDao.updateWorker(worker)

    suspend fun deleteWorker(worker: WorkerEntity) {
        workerDao.deleteWorker(worker)
        attendanceDao.deleteForWorker(worker.id)
    }

    suspend fun saveAttendanceRecord(date: String, workerId: Int, status: String) {
        attendanceDao.insertOrUpdate(
            AttendanceEntity(
                date = date,
                workerId = workerId,
                status = status
            )
        )
    }

    suspend fun saveAllAttendance(records: List<AttendanceEntity>) {
        attendanceDao.insertOrUpdateAll(records)
    }

    suspend fun clearAttendanceForDate(date: String) {
        attendanceDao.deleteForDate(date)
    }

    suspend fun clearAttendanceForWorkerDate(date: String, workerId: Int) {
        attendanceDao.deleteForWorkerDate(date, workerId)
    }

    suspend fun getActiveWorkersSnapshot(): List<WorkerEntity> =
        workerDao.getActiveWorkersSnapshot()

    suspend fun getAllWorkersSnapshot(): List<WorkerEntity> =
        workerDao.getAllWorkersSnapshot()

    suspend fun getAllRecordsSnapshot(): List<AttendanceEntity> =
        attendanceDao.getAllRecordsSnapshot()

    suspend fun getAllSettingsSnapshot(): List<AppSettingEntity> =
        settingDao.getAllSettingsSnapshot()

    suspend fun insertWorkers(workers: List<WorkerEntity>) =
        workerDao.insertWorkers(workers)

    suspend fun saveAllSettings(settings: List<AppSettingEntity>) =
        settingDao.setSettings(settings)

    // Monthly Calculation helper
    fun getMonthlyStatsFlow(yearMonth: String, dailyWage: Double): Flow<List<WorkerMonthlyStat>> {
        return combine(
            workerDao.getAllActiveWorkers(),
            attendanceDao.getAttendanceForMonth(yearMonth)
        ) { workers, attendanceRecords ->
            val attendanceByWorker = attendanceRecords.groupBy { it.workerId }

            workers.map { worker ->
                val records = attendanceByWorker[worker.id] ?: emptyList()
                val p = records.count { it.status == "P" }
                val a = records.count { it.status == "A" }
                val h = records.count { it.status == "H" }
                val d = records.count { it.status == "D" }

                // Formula: Present (1.0) + Half Day (0.5) + Double Duty (2.0)
                val workingDays = (p * 1.0) + (h * 0.5) + (d * 2.0)
                val salary = workingDays * dailyWage

                WorkerMonthlyStat(
                    worker = worker,
                    presentCount = p,
                    absentCount = a,
                    halfDayCount = h,
                    doubleDutyCount = d,
                    calculatedWorkingDays = workingDays,
                    dailyWage = dailyWage,
                    finalSalary = salary
                )
            }
        }
    }

    fun getMonthlyDayLogsFlow(yearMonth: String, dailyWage: Double): Flow<List<MonthlyDayLog>> {
        return attendanceDao.getAttendanceForMonth(yearMonth).map { records ->
            val groupedByDate = records.groupBy { it.date }
            groupedByDate.map { (date, recs) ->
                val p = recs.count { it.status == "P" }
                val a = recs.count { it.status == "A" }
                val h = recs.count { it.status == "H" }
                val d = recs.count { it.status == "D" }
                val netDays = (p * 1.0) + (h * 0.5) + (d * 2.0)
                MonthlyDayLog(
                    date = date,
                    totalRecords = recs.size,
                    presentCount = p,
                    absentCount = a,
                    halfDayCount = h,
                    doubleDutyCount = d,
                    netWorkingDays = netDays,
                    estimatedSalary = netDays * dailyWage
                )
            }.sortedByDescending { it.date }
        }
    }

    fun getMonthlyOverallSummaryFlow(yearMonth: String, dailyWage: Double): Flow<MonthlyHistoricalSummary> {
        return combine(
            workerDao.getAllActiveWorkers(),
            attendanceDao.getAttendanceForMonth(yearMonth)
        ) { workers, records ->
            val distinctDates = records.map { it.date }.distinct().size
            val p = records.count { it.status == "P" }
            val a = records.count { it.status == "A" }
            val h = records.count { it.status == "H" }
            val d = records.count { it.status == "D" }
            val netDays = (p * 1.0) + (h * 0.5) + (d * 2.0)
            val grandSalary = netDays * dailyWage
            val totalLogs = records.size
            val rate = if (totalLogs > 0) (p.toDouble() / totalLogs.toDouble()) * 100.0 else 0.0

            MonthlyHistoricalSummary(
                month = yearMonth,
                totalLoggedDays = distinctDates,
                totalActiveWorkers = workers.size,
                totalPresent = p,
                totalAbsent = a,
                totalHalfDay = h,
                totalDoubleDuty = d,
                totalNetManDays = netDays,
                grandSalary = grandSalary,
                averageAttendanceRate = rate
            )
        }
    }
}
