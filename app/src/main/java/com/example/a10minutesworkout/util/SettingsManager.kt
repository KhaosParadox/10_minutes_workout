package com.example.a10minutesworkout.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsManager(private val context: Context) {

    companion object {
        val EFFORT_DURATION = intPreferencesKey("effort_duration")
        val REST_DURATION = intPreferencesKey("rest_duration")
        val NUMBER_OF_ROUNDS = intPreferencesKey("number_of_rounds")
        val MUSIC_ENABLED_BY_DEFAULT = booleanPreferencesKey("music_enabled_by_default")
        val SHUFFLE_ENABLED = booleanPreferencesKey("shuffle_enabled")
    }

    val effortDuration: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[EFFORT_DURATION] ?: 30
    }

    val restDuration: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[REST_DURATION] ?: 10
    }

    val numberOfRounds: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[NUMBER_OF_ROUNDS] ?: 1
    }

    val isMusicEnabledByDefault: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[MUSIC_ENABLED_BY_DEFAULT] ?: true
    }

    val isShuffleEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[SHUFFLE_ENABLED] ?: false
    }

    suspend fun saveEffortDuration(duration: Int) {
        context.dataStore.edit { preferences -> preferences[EFFORT_DURATION] = duration }
    }

    suspend fun saveRestDuration(duration: Int) {
        context.dataStore.edit { preferences -> preferences[REST_DURATION] = duration }
    }

    suspend fun saveNumberOfRounds(rounds: Int) {
        context.dataStore.edit { preferences -> preferences[NUMBER_OF_ROUNDS] = rounds }
    }

    suspend fun saveMusicEnabledByDefault(enabled: Boolean) {
        context.dataStore.edit { preferences -> preferences[MUSIC_ENABLED_BY_DEFAULT] = enabled }
    }

    suspend fun saveShuffleEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences -> preferences[SHUFFLE_ENABLED] = enabled }
    }
}
