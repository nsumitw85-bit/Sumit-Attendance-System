package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.AttendanceDao
import com.example.data.dao.SettingDao
import com.example.data.dao.WorkerDao
import com.example.data.model.AppSettingEntity
import com.example.data.model.AttendanceEntity
import com.example.data.model.WorkerEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Database(
    entities = [
        WorkerEntity::class,
        AttendanceEntity::class,
        AppSettingEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun workerDao(): WorkerDao
    abstract fun attendanceDao(): AttendanceDao
    abstract fun settingDao(): SettingDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "sumit_attendance.db"
                )
                    .setJournalMode(JournalMode.WRITE_AHEAD_LOGGING)
                    .fallbackToDestructiveMigration()
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            CoroutineScope(Dispatchers.IO).launch {
                                populateInitialData(getInstance(context))
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private suspend fun populateInitialData(database: AppDatabase) {
            val settingDao = database.settingDao()
            val workerDao = database.workerDao()
            val attendanceDao = database.attendanceDao()

            // Default Settings
            settingDao.setSettings(
                listOf(
                    AppSettingEntity("daily_wage", "300"),
                    AppSettingEntity("app_language", "mr"),
                    AppSettingEntity("app_theme", "light"),
                    AppSettingEntity("tts_enabled", "true"),
                    AppSettingEntity("admin_phone", "9876543210"),
                    AppSettingEntity("admin_name", "Sumit (Admin)"),
                    AppSettingEntity("department_name", "Sanitation Department / स्वच्छता विभाग"),
                    AppSettingEntity("admin_logged_in", "true") // Keep session logged in
                )
            )

            // Initial Sanitation Team Workers (Representative set of 52 sanitation workers)
            val initialWorkers = listOf(
                WorkerEntity(1, "SAN-101", "Santosh Waghmare", "9823101234", "Broom Worker", "#1565C0"),
                WorkerEntity(2, "SAN-102", "Sunil Kamble", "9823101235", "Broom Worker", "#2E7D32"),
                WorkerEntity(3, "SAN-103", "Ramesh Shinde", "9823101236", "Drain Cleaning Worker", "#EF6C00"),
                WorkerEntity(4, "SAN-104", "Anand Gaikwad", "9823101237", "Garbage Vehicle Driver", "#7B1FA2"),
                WorkerEntity(5, "SAN-105", "Prakash Jadhav", "9823101238", "Garbage Vehicle Helper", "#00897B"),
                WorkerEntity(6, "SAN-106", "Deepak Thorat", "9823101239", "Road Sweeper", "#C2185B"),
                WorkerEntity(7, "SAN-107", "Kailash Salve", "9823101240", "Public Toilet Cleaner", "#F57C00"),
                WorkerEntity(8, "SAN-108", "Baburao More", "9823101241", "Broom Worker", "#3949AB"),
                WorkerEntity(9, "SAN-109", "Vijay Mane", "9823101242", "Drain Cleaning Worker", "#00ACC1"),
                WorkerEntity(10, "SAN-110", "Dattatray Pawar", "9823101243", "Supervisor / Mukadam", "#D81B60"),
                WorkerEntity(11, "SAN-111", "Ganesh Kadam", "9823101244", "Broom Worker", "#43A047"),
                WorkerEntity(12, "SAN-112", "Sambhaji Sawant", "9823101245", "Garbage Vehicle Driver", "#8E24AA"),
                WorkerEntity(13, "SAN-113", "Rajesh Bhosale", "9823101246", "Garbage Vehicle Helper", "#FB8C00"),
                WorkerEntity(14, "SAN-114", "Sachin Patil", "9823101247", "Drain Cleaning Worker", "#1E88E5"),
                WorkerEntity(15, "SAN-115", "Mahesh Chavan", "9823101248", "Road Sweeper", "#5E35B1"),
                WorkerEntity(16, "SAN-116", "Nitin Sonawane", "9823101249", "Broom Worker", "#00897B"),
                WorkerEntity(17, "SAN-117", "Ashok Magar", "9823101250", "Garbage Vehicle Driver", "#E53935"),
                WorkerEntity(18, "SAN-118", "Sandip Ghorpade", "9823101251", "Garbage Vehicle Helper", "#039BE5"),
                WorkerEntity(19, "SAN-119", "Kiran Lokhande", "9823101252", "Drain Cleaning Worker", "#7CB342"),
                WorkerEntity(20, "SAN-120", "Vilas Khot", "9823101253", "Public Toilet Cleaner", "#6D4C41"),
                WorkerEntity(21, "SAN-121", "Tanaji Surve", "9823101254", "Broom Worker", "#546E7A"),
                WorkerEntity(22, "SAN-122", "Rahul Ingle", "9823101255", "Broom Worker", "#1565C0"),
                WorkerEntity(23, "SAN-123", "Shankar Nikam", "9823101256", "Drain Cleaning Worker", "#2E7D32"),
                WorkerEntity(24, "SAN-124", "Bhagwan Jagtap", "9823101257", "Garbage Vehicle Helper", "#EF6C00"),
                WorkerEntity(25, "SAN-125", "Pandurang Nalawade", "9823101258", "Road Sweeper", "#7B1FA2"),
                WorkerEntity(26, "SAN-126", "Subhash Deshmukh", "9823101259", "Supervisor / Mukadam", "#00897B"),
                WorkerEntity(27, "SAN-127", "Vithal Ghodke", "9823101260", "Broom Worker", "#C2185B"),
                WorkerEntity(28, "SAN-128", "Arun Bandgar", "9823101261", "Garbage Vehicle Driver", "#3949AB"),
                WorkerEntity(29, "SAN-129", "Uttam Maske", "9823101262", "Garbage Vehicle Helper", "#00ACC1"),
                WorkerEntity(30, "SAN-130", "Suresh Sathe", "9823101263", "Drain Cleaning Worker", "#D81B60")
            )
            workerDao.insertWorkers(initialWorkers)

            // Populate sample attendance for past few days and today so reports are instantly rich
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val cal = Calendar.getInstance()

            val sampleStatuses = listOf("P", "P", "P", "H", "D", "P", "A", "P", "P", "P")

            for (dayOffset in 0..6) {
                val pastCal = Calendar.getInstance()
                pastCal.add(Calendar.DAY_OF_YEAR, -dayOffset)
                val dateStr = dateFormat.format(pastCal.time)

                val records = initialWorkers.mapIndexed { index, worker ->
                    val status = if (dayOffset == 0) {
                        // Today's attendance preset mostly Present with a few H and D
                        when ((index + dayOffset) % 10) {
                            0, 1 -> "H"
                            2 -> "D"
                            3 -> "A"
                            else -> "P"
                        }
                    } else {
                        sampleStatuses[(index + dayOffset * 3) % sampleStatuses.size]
                    }
                    AttendanceEntity(
                        date = dateStr,
                        workerId = worker.id,
                        status = status
                    )
                }
                attendanceDao.insertOrUpdateAll(records)
            }
        }
    }
}
