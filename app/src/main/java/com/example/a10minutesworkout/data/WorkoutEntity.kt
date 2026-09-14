package com.example.a10minutesworkout.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import androidx.room.Index

@Entity(tableName = "workout_sessions", indices = [Index(value = ["sessionKey"], unique = true)])
data class WorkoutSession(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val timestamp: Long,
    val dateString: String, // Format 'YYYY-MM-DD'
    val workoutName: String,
    val durationInSeconds: Int,
    @ColumnInfo(defaultValue = "''") val programId: String = "",
    @ColumnInfo(defaultValue = "0") val level: Int = 0,
    @ColumnInfo(defaultValue = "0") val activeSeconds: Int = 0,
    @ColumnInfo(defaultValue = "1") val completed: Boolean = true,
    @ColumnInfo(defaultValue = "''") val feedback: String = "",
    @ColumnInfo(defaultValue = "0") val advancesCycle: Boolean = false,
    val sessionKey: String? = null
)
