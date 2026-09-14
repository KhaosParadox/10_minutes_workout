package com.example.a10minutesworkout.ui

import android.app.Application
import android.os.SystemClock
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.a10minutesworkout.data.*
import com.example.a10minutesworkout.model.*
import com.example.a10minutesworkout.util.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.LocalDate
import java.util.UUID

data class WorkoutUiState(
    val plan: TrainingPlan? = null,
    val progress: SessionProgress = SessionProgress(),
    val remainingTotalMillis: Long = 0,
    val completed: Boolean = false,
    val muted: Boolean = false,
    val saving: Boolean = false,
    val saved: Boolean = false,
    val error: String? = null,
    val audioError: String? = null
)

class WorkoutViewModel(application: Application, private val savedState: SavedStateHandle) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(WorkoutUiState())
    val uiState = _uiState.asStateFlow()
    private val dao = WorkoutDatabase.getDatabase(application).workoutDao()
    private val trackDao = WorkoutDatabase.getDatabase(application).trackDao()
    private val settings = SettingsManager(application)
    private val tts = TTSManager(application)
    private val bell = SoundManager(application)
    private val music = MusicManager(application) { message -> _uiState.update { it.copy(audioError = message) } }
    private var engine: SessionEngine? = null
    private var started = false
    private var foreground = true
    private var lastTick = 0L
    private var ticker: Job? = null
    private val sessionKey = savedState.get<String>("sessionKey") ?: UUID.randomUUID().toString().also { savedState["sessionKey"] = it }

    fun start(gentle: Boolean) {
        if (started) return
        started = true
        viewModelScope.launch {
            try {
                val restoredPlan = savedState.get<String>("plan")?.let { Json.decodeFromString<TrainingPlan>(it) }
                val plan = restoredPlan ?: PersonalProgram.recommended(dao.getAllSessions().first(), gentle = gentle)
                val progress = savedState.get<String>("progress")?.let { Json.decodeFromString<SessionProgress>(it).copy(paused = true) }
                lastTick = SystemClock.elapsedRealtime()
                engine = SessionEngine(plan, progress)
                if (!foreground) engine?.pause(true)
                val enabled = settings.isMusicEnabledByDefault.first()
                music.setPlaylist(trackDao.getAllTracks().first(), settings.isShuffleEnabled.first())
                music.setMute(!enabled)
                _uiState.update { it.copy(plan = plan, muted = !enabled, saved = savedState.get<Boolean>("saved") == true) }
                savedState["plan"] = Json.encodeToString(plan)
                publish()
                if (enabled && engine?.state?.paused == false && engine?.finished == false) music.play()
                tts.setOnReadyListener { if (engine?.state?.paused == false && engine?.finished == false) announce() }
                lastTick = SystemClock.elapsedRealtime()
                ticker = viewModelScope.launch {
                    while (isActive) { delay(100); tick() }
                }
            } catch (e: CancellationException) { throw e }
            catch (e: Exception) { _uiState.update { it.copy(error = "Impossible de préparer la séance. Reviens à l’accueil puis réessaie.") } }
        }
    }

    private fun tick() {
        val now = SystemClock.elapsedRealtime()
        val delta = (now - lastTick).coerceAtLeast(0)
        lastTick = now
        val current = engine ?: return
        if (current.finished || current.state.paused) return
        val previous = current.state
        val previousStep = current.step
        current.advance(delta)
        if (current.finished) {
            music.stop(); tts.stop(); bell.playBell()
        } else if (current.state.index != previous.index) {
            bell.playBell(); announce()
        } else {
            val before = (previous.remainingMillis + 999) / 1000
            val after = (current.state.remainingMillis + 999) / 1000
            if (before != after && after in 1..3) tts.speak(after.toString(), flush = true)
            val half = (previousStep?.seconds ?: 0) * 500L
            if (previousStep?.kind == StepKind.WORK && previous.remainingMillis > half && current.state.remainingMillis <= half) tts.speak("Mi-temps", flush = true)
        }
        publish()
    }

    private fun announce() {
        engine?.step?.let { tts.speak(if (it.kind == StepKind.REST) it.cue else it.name, flush = true) }
    }

    private fun publish() {
        val current = engine ?: return
        _uiState.update { it.copy(progress = current.state, remainingTotalMillis = current.remainingTotalMillis, completed = current.finished) }
        savedState["progress"] = Json.encodeToString(current.state)
    }

    fun pause() {
        tick(); engine?.pause(true)
        music.pause(); tts.stop(); publish()
    }

    fun togglePause() {
        val current = engine ?: return
        if (current.finished) return
        if (!current.state.paused) pause() else {
            lastTick = SystemClock.elapsedRealtime(); current.pause(false)
            if (!_uiState.value.muted) music.play()
            announce(); publish()
        }
    }

    fun onForeground(value: Boolean) { foreground = value; if (!value) pause() }

    fun skip() {
        tick()
        val current = engine ?: return
        if (current.finished) return
        current.skip()
        if (current.finished) { music.stop(); tts.stop(); bell.playBell() }
        else if (!current.state.paused) announce()
        publish()
    }

    fun toggleMusicMute() {
        val muted = !_uiState.value.muted
        music.setMute(muted); _uiState.update { it.copy(muted = muted) }
        if (!muted && engine?.state?.paused == false && engine?.finished == false) music.play()
    }

    fun save(feedback: String, onSaved: () -> Unit) {
        if (_uiState.value.saving) return
        if (_uiState.value.saved) { onSaved(); return }
        pause()
        val current = engine ?: run { onSaved(); return }
        if (current.state.elapsedMillis == 0L) { onSaved(); return }
        _uiState.update { it.copy(saving = true, error = null) }
        viewModelScope.launch {
            try {
                val complete = current.finished && current.state.skippedWork == 0
                dao.insertSession(WorkoutSession(
                    timestamp = System.currentTimeMillis(), dateString = LocalDate.now().toString(),
                    workoutName = current.plan.title, durationInSeconds = (current.state.elapsedMillis / 1000).toInt(),
                    programId = current.plan.id, level = current.plan.level,
                    activeSeconds = (current.state.activeMillis / 1000).toInt(), completed = complete,
                    feedback = feedback, advancesCycle = complete && current.plan.advancesCycle, sessionKey = sessionKey
                ))
                savedState["saved"] = true
                _uiState.update { it.copy(saving = false, saved = true) }; onSaved()
            } catch (e: CancellationException) { throw e }
            catch (e: Exception) { _uiState.update { it.copy(saving = false, error = "Enregistrement impossible. Réessaie : ta séance reste ici.") } }
        }
    }

    override fun onCleared() {
        ticker?.cancel(); music.release(); tts.shutdown(); bell.release(); super.onCleared()
    }
}
