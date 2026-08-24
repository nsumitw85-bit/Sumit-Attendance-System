package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "workers",
    indices = [
        Index(value = ["isActive"]),
        Index(value = ["workerCode"])
    ]
)
data class WorkerEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val workerCode: String,      // e.g. "SW-101"
    val name: String,            // Full Name
    val phone: String,           // Mobile Number
    val roleCategory: String,    // Sanitation Role
    val avatarColorHex: String = "#1565C0",
    val photoUri: String = "",   // Optional photo URI or custom avatar
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
