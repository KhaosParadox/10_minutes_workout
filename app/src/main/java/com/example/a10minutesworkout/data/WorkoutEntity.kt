package com.example.a10minutesworkout.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workout_sessions")
data class WorkoutSession(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val timestamp: Long,
    val dateString: String, // Format 'YYYY-MM-DD'
    val workoutName: String,
    val durationInSeconds: Int
)
