package com.example.a10minutesworkout.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tracks")
data class Track(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val uriString: String,
    val orderIndex: Int
)
