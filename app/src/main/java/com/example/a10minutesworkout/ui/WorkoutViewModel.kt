package com.example.a10minutesworkout.ui

import android.app.Application
import android.media.AudioAttributes
import android.media.MediaPlayer
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.a10minutesworkout.data.ExerciseData
import com.example.a10minutesworkout.data.WorkoutDatabase
import com.example.a10minutesworkout.data.WorkoutSession
import com.example.a10minutesworkout.model.Exercise
import com.example.a10minutesworkout.util.MusicManager
import com.example.a10minutesworkout.util.SettingsManager
import com.example.a10minutesworkout.util.SoundManager
import com.example.a10minutesworkout.util.TTSManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

enum class WorkoutPhase {
    PREPARATION, EFFORT, REST, COMPLETED
}

data class WorkoutUiState(
    val phase: WorkoutPhase = WorkoutPhase.PREPARATION,
    val currentExerciseIndex: Int = 0,
    val timeLeftMillis: Long = 10000L,
    val isPaused: Boolean = false,
    val isMusicMuted: Boolean = false,
    val isCompleted: Boolean = false,
    val totalExercises: Int = 0,
    val exercises: List<Exercise> = emptyList(),
    val effortDuration: Int = 30,
    val restDuration: Int = 10
) {
    val currentExercise: Exercise? = if (currentExerciseIndex < exercises.size) {
        exercises[currentExerciseIndex]
    } else null
}

class WorkoutViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(WorkoutUiState())
    val uiState: StateFlow<WorkoutUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null
    
    private val ttsManager = TTSManager(application)
    private val soundManager = SoundManager(application)
    private val musicManager = MusicManager(application)
    private val settingsManager = SettingsManager(application)
    private val trackDao = WorkoutDatabase.getDatabase(application).trackDao()
    private val workoutDao = WorkoutDatabase.getDatabase(application).workoutDao()

    init {
        viewModelScope.launch {
            // 1. Fetch settings and tracks
            val effort = settingsManager.effortDuration.first()
            val rest = settingsManager.restDuration.first()
            val rounds = settingsManager.numberOfRounds.first()
            val musicEnabled = settingsManager.isMusicEnabledByDefault.first()
            val shuffle = settingsManager.isShuffleEnabled.first()
            val tracks = trackDao.getAllTracks().first()

            // 2. Build workout list
            val baseList = ExerciseData.workoutExercises
            val fullList = mutableListOf<Exercise>()
            repeat(rounds) { fullList.addAll(baseList) }
            
            // 3. Update UI state
            _uiState.update { it.copy(
                exercises = fullList,
                totalExercises = fullList.size,
                effortDuration = effort,
                restDuration = rest,
                isMusicMuted = !musicEnabled
            ) }

            // 4. Configure and Launch music
            musicManager.setPlaylist(tracks, shuffle)
            musicManager.setMute(!musicEnabled)
            if (musicEnabled) {
                musicManager.play()
            }
            
            // 5. Start timer and TTS
            startTimer()
            ttsManager.setOnReadyListener {
                announceNextExercise(WorkoutPhase.PREPARATION, 0)
            }
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                val currentState = _uiState.value
                if (currentState.phase == WorkoutPhase.COMPLETED) break

                if (!currentState.isPaused) {
                    delay(100L)
                    val oldTime = _uiState.value.timeLeftMillis
                    val newTime = oldTime - 100L
                    
                    handleTimeEvents(oldTime, newTime)

                    if (newTime <= 0) {
                        var phaseToAnnounce: WorkoutPhase? = null
                        var indexToAnnounce = 0
                        var isCompletedNow = false

                        _uiState.update { state ->
                            val nextState = moveToNextState(state)
                            if (nextState.phase == WorkoutPhase.COMPLETED) {
                                isCompletedNow = true
                            } else {
                                phaseToAnnounce = nextState.phase
                                indexToAnnounce = nextState.currentExerciseIndex
                            }
                            nextState
                        }

                        if (isCompletedNow) {
                            handleWorkoutCompletion()
                            break 
                        } else if (phaseToAnnounce != null) {
                            viewModelScope.launch {
                                delay(1200)
                                announceNextExercise(phaseToAnnounce!!, indexToAnnounce)
                            }
                        }
                    } else {
                        _uiState.update { it.copy(timeLeftMillis = newTime) }
                    }
                } else {
                    delay(100L)
                }
            }
        }
    }

    private fun handleTimeEvents(oldTime: Long, newTime: Long) {
        val oldSec = (oldTime / 1000).toInt()
        val newSec = (newTime / 1000).toInt()

        if (oldTime > 300L && newTime <= 300L) {
            soundManager.playBell()
        }

        if (oldSec != newSec && newSec >= 0) {
            if (oldSec == 4 && newSec == 3) ttsManager.speak("3", flush = true)
            if (oldSec == 3 && newSec == 2) ttsManager.speak("2", flush = true)
            if (oldSec == 2 && newSec == 1) ttsManager.speak("1", flush = true)
            
            if (_uiState.value.phase == WorkoutPhase.EFFORT && newSec == 15 && oldSec == 16) {
                ttsManager.speak("Mi-temps", flush = true)
            }
        }
    }

    private fun announceNextExercise(phase: WorkoutPhase, index: Int) {
        val nextEx = if (phase == WorkoutPhase.PREPARATION) {
            _uiState.value.exercises.getOrNull(index)
        } else if (phase == WorkoutPhase.REST) {
            _uiState.value.exercises.getOrNull(index + 1)
        } else null

        if (nextEx != null) {
            ttsManager.speak("Prochain exercice : ${nextEx.name}")
        }
    }

    private fun moveToNextState(currentState: WorkoutUiState): WorkoutUiState {
        return when (currentState.phase) {
            WorkoutPhase.PREPARATION -> {
                currentState.copy(
                    phase = WorkoutPhase.EFFORT,
                    timeLeftMillis = currentState.effortDuration * 1000L
                )
            }
            WorkoutPhase.EFFORT -> {
                val isLastExercise = currentState.currentExerciseIndex == currentState.totalExercises - 1
                if (isLastExercise) {
                    currentState.copy(
                        phase = WorkoutPhase.COMPLETED,
                        timeLeftMillis = 0
                    )
                } else {
                    currentState.copy(
                        phase = WorkoutPhase.REST,
                        timeLeftMillis = currentState.restDuration * 1000L
                    )
                }
            }
            WorkoutPhase.REST -> {
                val nextIndex = currentState.currentExerciseIndex + 1
                currentState.copy(
                    phase = WorkoutPhase.EFFORT,
                    currentExerciseIndex = nextIndex,
                    timeLeftMillis = currentState.effortDuration * 1000L
                )
            }
            WorkoutPhase.COMPLETED -> currentState
        }
    }

    private fun handleWorkoutCompletion() {
        musicManager.stop()
        _uiState.update { it.copy(isCompleted = true) }
        viewModelScope.launch {
            delay(1000)
            ttsManager.speak("Entraînement terminé, félicitations !", flush = true)
            val session = WorkoutSession(
                timestamp = System.currentTimeMillis(),
                dateString = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE),
                workoutName = "10 Minutes Workout",
                durationInSeconds = _uiState.value.exercises.size * _uiState.value.effortDuration
            )
            workoutDao.insertSession(session)
        }
    }

    fun togglePause() {
        var isNowPaused = false
        _uiState.update { 
            isNowPaused = !it.isPaused
            it.copy(isPaused = isNowPaused)
        }
        if (isNowPaused) musicManager.pause() else musicManager.play()
    }

    fun toggleMusicMute() {
        var isNowMuted = false
        _uiState.update { 
            isNowMuted = !it.isMusicMuted
            it.copy(isMusicMuted = isNowMuted)
        }
        musicManager.setMute(isNowMuted)
    }

    fun dismissCompletionDialog(onDismissed: () -> Unit) {
        stopAllAudio()
        _uiState.update { it.copy(isCompleted = false) }
        onDismissed()
    }

    private fun stopAllAudio() {
        musicManager.stop()
        ttsManager.shutdown()
        soundManager.release()
    }

    fun skipForward() {
        soundManager.playBell()
        var phaseToAnnounce: WorkoutPhase? = null
        var indexToAnnounce = 0
        _uiState.update { state ->
            val nextState = moveToNextState(state)
            phaseToAnnounce = nextState.phase
            indexToAnnounce = nextState.currentExerciseIndex
            nextState
        }
        phaseToAnnounce?.let { announceNextExercise(it, indexToAnnounce) }
    }

    fun skipBackward() {
        var phaseToAnnounce: WorkoutPhase? = null
        var indexToAnnounce = 0
        _uiState.update { state ->
            val newState = if (state.currentExerciseIndex > 0) {
                state.copy(
                    phase = WorkoutPhase.EFFORT,
                    currentExerciseIndex = state.currentExerciseIndex - 1,
                    timeLeftMillis = state.effortDuration * 1000L
                )
            } else if (state.phase != WorkoutPhase.PREPARATION) {
                state.copy(
                    phase = WorkoutPhase.PREPARATION,
                    timeLeftMillis = 10000L
                )
            } else {
                state
            }
            phaseToAnnounce = newState.phase
            indexToAnnounce = newState.currentExerciseIndex
            newState
        }
        phaseToAnnounce?.let { announceNextExercise(it, indexToAnnounce) }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        stopAllAudio()
    }
}
