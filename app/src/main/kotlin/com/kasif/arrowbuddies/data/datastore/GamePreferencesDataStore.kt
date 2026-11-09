package com.kasif.arrowbuddies.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "arrow_buddies_preferences")

@Singleton
class GamePreferencesDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object PreferencesKeys {
        val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val MUSIC_ENABLED = booleanPreferencesKey("music_enabled")
        val HAPTICS_ENABLED = booleanPreferencesKey("haptics_enabled")
        val TOTAL_RESCUED = intPreferencesKey("total_rescued")
        val BERRIES = intPreferencesKey("berries")
    }

    val soundEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.SOUND_ENABLED] ?: true
    }

    val musicEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.MUSIC_ENABLED] ?: true
    }

    val hapticsEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.HAPTICS_ENABLED] ?: true
    }

    val totalRescued: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.TOTAL_RESCUED] ?: 0
    }

    val berries: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.BERRIES] ?: 0
    }

    suspend fun setSoundEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SOUND_ENABLED] = enabled
        }
    }

    suspend fun setMusicEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.MUSIC_ENABLED] = enabled
        }
    }

    suspend fun setHapticsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.HAPTICS_ENABLED] = enabled
        }
    }

    fun isMechanicSeen(mechanic: String): Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[booleanPreferencesKey("mechanic_seen_$mechanic")] ?: false
    }

    suspend fun setMechanicSeen(mechanic: String) {
        context.dataStore.edit { preferences ->
            preferences[booleanPreferencesKey("mechanic_seen_$mechanic")] = true
        }
    }
}

