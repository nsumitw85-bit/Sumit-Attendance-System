package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "attendance_records",
    indices = [
        Index(value = ["date", "workerId"], unique = true),
        Index(value = ["date"]),
        Index(value = ["workerId"])
    ]
)
data class AttendanceEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val date: String,             // Format: "YYYY-MM-DD"
    val workerId: Int,            // References WorkerEntity.id
    val status: String,           // "P" (Present), "A" (Absent), "H" (Half Day), "D" (Double Duty)
    val timestamp: Long = System.currentTimeMillis(),
    val notes: String = ""
)
