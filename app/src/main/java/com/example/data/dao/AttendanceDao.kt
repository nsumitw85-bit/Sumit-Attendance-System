package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.AttendanceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AttendanceDao {
    @Query("SELECT * FROM attendance_records WHERE date = :date")
    fun getAttendanceForDate(date: String): Flow<List<AttendanceEntity>>

    @Query("SELECT * FROM attendance_records WHERE date = :date")
    suspend fun getAttendanceForDateSnapshot(date: String): List<AttendanceEntity>

    @Query("SELECT * FROM attendance_records WHERE date LIKE :yearMonth || '%'")
    fun getAttendanceForMonth(yearMonth: String): Flow<List<AttendanceEntity>>

    @Query("SELECT * FROM attendance_records WHERE date LIKE :yearMonth || '%'")
    suspend fun getAttendanceForMonthSnapshot(yearMonth: String): List<AttendanceEntity>

    @Query("SELECT * FROM attendance_records WHERE workerId = :workerId ORDER BY date DESC")
    fun getAttendanceForWorker(workerId: Int): Flow<List<AttendanceEntity>>

    @Query("SELECT * FROM attendance_records")
    fun getAllRecords(): Flow<List<AttendanceEntity>>

    @Query("SELECT * FROM attendance_records")
    suspend fun getAllRecordsSnapshot(): List<AttendanceEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(record: AttendanceEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateAll(records: List<AttendanceEntity>)

    @Query("DELETE FROM attendance_records WHERE date = :date")
    suspend fun deleteForDate(date: String)

    @Query("DELETE FROM attendance_records WHERE date = :date AND workerId = :workerId")
    suspend fun deleteForWorkerDate(date: String, workerId: Int)

    @Query("DELETE FROM attendance_records WHERE workerId = :workerId")
    suspend fun deleteForWorker(workerId: Int)
}
