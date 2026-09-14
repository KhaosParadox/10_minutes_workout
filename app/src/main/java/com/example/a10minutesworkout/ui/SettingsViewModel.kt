package com.example.a10minutesworkout.ui

import android.app.Application
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.a10minutesworkout.R
import com.example.a10minutesworkout.data.Track
import com.example.a10minutesworkout.data.WorkoutDatabase
import com.example.a10minutesworkout.util.SettingsManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class SettingsUiState(
    val effortDuration: Int = 30,
    val restDuration: Int = 10,
    val numberOfRounds: Int = 1,
    val isMusicEnabledByDefault: Boolean = true,
    val isShuffleEnabled: Boolean = false,
    val tracks: List<Track> = emptyList()
)

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val settingsManager = SettingsManager(application)
    private val trackDao = WorkoutDatabase.getDatabase(application).trackDao()
    private val workoutDao = WorkoutDatabase.getDatabase(application).workoutDao()

    val uiState: StateFlow<SettingsUiState> = combine(
        settingsManager.effortDuration,
        settingsManager.restDuration,
        settingsManager.numberOfRounds,
        settingsManager.isMusicEnabledByDefault,
        settingsManager.isShuffleEnabled,
        trackDao.getAllTracks()
    ) { params ->
        SettingsUiState(
            effortDuration = params[0] as Int,
            restDuration = params[1] as Int,
            numberOfRounds = params[2] as Int,
            isMusicEnabledByDefault = params[3] as Boolean,
            isShuffleEnabled = params[4] as Boolean,
            tracks = params[5] as List<Track>
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsUiState()
    )

    init {
        viewModelScope.launch {
            if (trackDao.getTrackCount() == 0) {
                val packageName = getApplication<Application>().packageName
                trackDao.insertTrack(
                    Track(
                        title = "Musique par défaut",
                        uriString = "android.resource://$packageName/${R.raw.background_music}",
                        orderIndex = 0
                    )
                )
            }
        }
    }

    fun updateEffortDuration(duration: Int) = viewModelScope.launch {
        settingsManager.saveEffortDuration(duration)
    }

    fun updateRestDuration(duration: Int) = viewModelScope.launch {
        settingsManager.saveRestDuration(duration)
    }

    fun updateNumberOfRounds(rounds: Int) = viewModelScope.launch {
        settingsManager.saveNumberOfRounds(rounds)
    }

    fun updateMusicEnabledByDefault(enabled: Boolean) = viewModelScope.launch {
        settingsManager.saveMusicEnabledByDefault(enabled)
    }

    fun updateShuffleEnabled(enabled: Boolean) = viewModelScope.launch {
        settingsManager.saveShuffleEnabled(enabled)
    }

    fun addTrack(uri: Uri, context: Context) = viewModelScope.launch {
        val title = getFileName(context, uri) ?: "Musique inconnue"
        val nextIndex = (uiState.value.tracks.maxOfOrNull { it.orderIndex } ?: -1) + 1
        trackDao.insertTrack(Track(title = title, uriString = uri.toString(), orderIndex = nextIndex))
    }

    fun deleteTrack(track: Track) = viewModelScope.launch {
        trackDao.deleteTrack(track)
        // Normalize order indices
        val remaining = uiState.value.tracks.filter { it.id != track.id }
            .mapIndexed { index, t -> t.copy(orderIndex = index) }
        trackDao.updateTracks(remaining)
    }

    fun moveTrack(index: Int, up: Boolean) = viewModelScope.launch {
        val list = uiState.value.tracks.toMutableList()
        val targetIndex = if (up) index - 1 else index + 1
        if (targetIndex in list.indices) {
            val track = list.removeAt(index)
            list.add(targetIndex, track)
            val updated = list.mapIndexed { i, t -> t.copy(orderIndex = i) }
            trackDao.updateTracks(updated)
        }
    }

    fun clearHistory() = viewModelScope.launch {
        workoutDao.deleteAllSessions()
    }

    private fun getFileName(context: Context, uri: Uri): String? {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex != -1) result = cursor.getString(nameIndex)
                }
            } finally {
                cursor?.close()
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result?.lastIndexOf('/') ?: -1
            if (cut != -1) result = result?.substring(cut + 1)
        }
        return result
    }
}
