package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.WorkerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkerDao {
    @Query("SELECT * FROM workers WHERE isActive = 1 ORDER BY name ASC")
    fun getAllActiveWorkers(): Flow<List<WorkerEntity>>

    @Query("SELECT * FROM workers ORDER BY id ASC")
    fun getAllWorkersList(): Flow<List<WorkerEntity>>

    @Query("SELECT * FROM workers WHERE id = :id LIMIT 1")
    suspend fun getWorkerById(id: Int): WorkerEntity?

    @Query("SELECT COUNT(*) FROM workers WHERE isActive = 1")
    fun getActiveWorkerCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorker(worker: WorkerEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkers(workers: List<WorkerEntity>)

    @Update
    suspend fun updateWorker(worker: WorkerEntity)

    @Delete
    suspend fun deleteWorker(worker: WorkerEntity)

    @Query("DELETE FROM workers WHERE id = :id")
    suspend fun deleteWorkerById(id: Int)

    @Query("SELECT * FROM workers WHERE isActive = 1")
    suspend fun getActiveWorkersSnapshot(): List<WorkerEntity>

    @Query("SELECT * FROM workers ORDER BY id ASC")
    suspend fun getAllWorkersSnapshot(): List<WorkerEntity>
}
