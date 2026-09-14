package com.example.a10minutesworkout.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: WorkoutSession)

    @Query("SELECT * FROM workout_sessions ORDER BY timestamp DESC")
    fun getAllSessions(): Flow<List<WorkoutSession>>

    @Query("SELECT * FROM workout_sessions WHERE dateString = :date")
    suspend fun getSessionsByDate(date: String): List<WorkoutSession>

    @Query("DELETE FROM workout_sessions")
    suspend fun deleteAllSessions()

    // Music Playlist Management
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrack(track: Track)

    @Query("SELECT * FROM tracks ORDER BY orderIndex ASC")
    fun getAllTracks(): Flow<List<Track>>

    @Query("DELETE FROM tracks WHERE id = :trackId")
    suspend fun deleteTrack(trackId: Int)

    @Query("DELETE FROM tracks")
    suspend fun deleteAllTracks()

    @Update
    suspend fun updateTracks(tracks: List<Track>)

    @Query("SELECT COUNT(*) FROM tracks")
    suspend fun getTrackCount(): Int
}
