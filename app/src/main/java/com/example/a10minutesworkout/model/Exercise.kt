package com.example.a10minutesworkout.model

data class Exercise(
    val id: Int,
    val name: String,
    val duration: Long = 30000L,
    val restTime: Long = 10000L,
    val gifResourceName: String
)
