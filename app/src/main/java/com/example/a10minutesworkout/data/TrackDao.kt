package com.example.a10minutesworkout.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackDao {
    @Query("SELECT * FROM tracks ORDER BY orderIndex ASC")
    fun getAllTracks(): Flow<List<Track>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrack(track: Track)

    @Delete
    suspend fun deleteTrack(track: Track)

    @Update
    suspend fun updateTracks(tracks: List<Track>)

    @Query("SELECT COUNT(*) FROM tracks")
    suspend fun getTrackCount(): Int
}
