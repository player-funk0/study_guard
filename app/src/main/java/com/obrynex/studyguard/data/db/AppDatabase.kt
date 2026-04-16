package com.obrynex.studyguard.data.db

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

// ── Entity ────────────────────────────────────────────────────────────────────

@Entity(tableName = "study_sessions")
data class StudySession(
    @PrimaryKey(autoGenerate = true) val id      : Long   = 0,
    val subject    : String,         // e.g. "Math", "Physics"
    val startMs    : Long,           // epoch millis
    val endMs      : Long,           // epoch millis
    val durationMin: Int,            // cached for fast queries
    val dateKey    : String          // "yyyy-MM-dd" for grouping
)

// ── DAO ───────────────────────────────────────────────────────────────────────

@Dao
interface StudySessionDao {

    @Insert
    suspend fun insert(session: StudySession)

    @Delete
    suspend fun delete(session: StudySession)

    @Query("SELECT * FROM study_sessions ORDER BY startMs DESC")
    fun allSessions(): Flow<List<StudySession>>

    @Query("SELECT * FROM study_sessions WHERE dateKey = :date ORDER BY startMs DESC")
    fun sessionsByDate(date: String): Flow<List<StudySession>>

    // Total minutes per day (last 7 days)
    @Query("""
        SELECT dateKey, SUM(durationMin) as totalMin
        FROM study_sessions
        WHERE dateKey >= :fromDate
        GROUP BY dateKey
        ORDER BY dateKey ASC
    """)
    fun weeklyStats(fromDate: String): Flow<List<DayStat>>

    @Query("SELECT COUNT(DISTINCT dateKey) FROM study_sessions")
    suspend fun totalStudyDays(): Int

    @Query("SELECT SUM(durationMin) FROM study_sessions WHERE dateKey = :date")
    suspend fun minutesOnDate(date: String): Int?
}

data class DayStat(val dateKey: String, val totalMin: Int)

// ── Database ──────────────────────────────────────────────────────────────────

@Database(entities = [StudySession::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun studySessionDao(): StudySessionDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun get(context: Context): AppDatabase = INSTANCE ?: synchronized(this) {
            Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "studyguard.db")
                .build()
                .also { INSTANCE = it }
        }
    }
}
