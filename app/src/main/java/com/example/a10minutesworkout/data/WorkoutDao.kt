package com.example.a10minutesworkout.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertSession(session: WorkoutSession)

    @Query("SELECT * FROM workout_sessions ORDER BY timestamp DESC")
    fun getAllSessions(): Flow<List<WorkoutSession>>

    @Query("SELECT * FROM workout_sessions WHERE dateString = :date")
    suspend fun getSessionsByDate(date: String): List<WorkoutSession>

    @Query("DELETE FROM workout_sessions")
    suspend fun deleteAllSessions()

}
