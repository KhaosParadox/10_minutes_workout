package com.example.a10minutesworkout.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.a10minutesworkout.data.WorkoutDatabase
import com.example.a10minutesworkout.data.WorkoutSession
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class CalendarViewModel(application: Application) : AndroidViewModel(application) {
    private val workoutDao = WorkoutDatabase.getDatabase(application).workoutDao()

    val allSessions: StateFlow<List<WorkoutSession>> = workoutDao.getAllSessions()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}
