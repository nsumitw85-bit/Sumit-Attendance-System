package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.AppRepository
import com.example.data.model.AttendanceEntity
import com.example.data.model.WorkerEntity
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CloudBackupManager {

    suspend fun createBackupJson(repository: AppRepository): String {
        val workers = repository.getActiveWorkersSnapshot()
        val attendance = repository.getAllRecordsSnapshot()

        val root = JSONObject()
        root.put("appName", "Sumit Attendance System")
        root.put("version", "1.0")
        root.put("backupDate", SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()))

        // Workers Array
        val workersArray = JSONArray()
        for (w in workers) {
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
        for (a in attendance) {
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

        return root.toString(2)
    }

    suspend fun exportBackupFile(context: Context, repository: AppRepository): File? {
        return try {
            val jsonString = createBackupJson(repository)
            val dir = File(context.cacheDir, "reports").apply { mkdirs() }
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val file = File(dir, "SumitAttendance_CloudBackup_$timeStamp.json")

            FileOutputStream(file).use {
                it.write(jsonString.toByteArray())
            }
            file
        } catch (e: Exception) {
            Log.e("CloudBackup", "Error creating backup file", e)
            null
        }
    }

    fun shareBackupFile(context: Context, file: File) {
        try {
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/json"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Sumit Attendance System - Cloud Backup")
                putExtra(Intent.EXTRA_TEXT, "Sumit Attendance System Database Backup File.\nKeep this safe to restore your workers and attendance anytime.")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            val chooser = Intent.createChooser(shareIntent, "Share Backup via...")
            context.startActivity(chooser)
        } catch (e: Exception) {
            Toast.makeText(context, "Error sharing backup: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    suspend fun restoreFromJson(repository: AppRepository, jsonString: String): Boolean {
        return try {
            val root = JSONObject(jsonString)
            val workersArray = root.optJSONArray("workers") ?: JSONArray()
            val attArray = root.optJSONArray("attendance") ?: JSONArray()

            val workers = mutableListOf<WorkerEntity>()
            for (i in 0 until workersArray.length()) {
                val obj = workersArray.getJSONObject(i)
                workers.add(
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

            val attendanceList = mutableListOf<AttendanceEntity>()
            for (i in 0 until attArray.length()) {
                val obj = attArray.getJSONObject(i)
                attendanceList.add(
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

            if (workers.isNotEmpty()) {
                for (w in workers) {
                    repository.insertWorker(w)
                }
            }
            if (attendanceList.isNotEmpty()) {
                repository.saveAllAttendance(attendanceList)
            }
            true
        } catch (e: Exception) {
            Log.e("CloudBackup", "Restore failed", e)
            false
        }
    }
}
