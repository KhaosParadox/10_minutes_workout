package com.example.a10minutesworkout.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "music_tracks")
data class MusicTrack(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val uri: String,
    val fileName: String,
    val order: Int
)
