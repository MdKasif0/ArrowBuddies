package com.kasif.arrowbuddies.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "arrow_buddies_preferences")

/**
 * Device-level settings only. All game progress (berries, hints, undos, stars, levels, species)
 * lives in Room, which is the single source of truth.
 */
@Singleton
class GamePreferencesDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object PreferencesKeys {
        val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val MUSIC_ENABLED = booleanPreferencesKey("music_enabled")
        val HAPTICS_ENABLED = booleanPreferencesKey("haptics_enabled")
        val REDUCE_MOTION = booleanPreferencesKey("reduce_motion")
        val DAILY_REMINDER_ENABLED = booleanPreferencesKey("daily_reminder_enabled")
        val REMINDER_PROMPT_SHOWN = booleanPreferencesKey("reminder_prompt_shown")
    }

    private fun flag(key: Preferences.Key<Boolean>, default: Boolean): Flow<Boolean> =
        context.dataStore.data.map { preferences -> preferences[key] ?: default }

    private suspend fun setFlag(key: Preferences.Key<Boolean>, value: Boolean) {
        context.dataStore.edit { preferences -> preferences[key] = value }
    }

    val soundEnabled: Flow<Boolean> = flag(PreferencesKeys.SOUND_ENABLED, true)
    val musicEnabled: Flow<Boolean> = flag(PreferencesKeys.MUSIC_ENABLED, true)
    val hapticsEnabled: Flow<Boolean> = flag(PreferencesKeys.HAPTICS_ENABLED, true)
    val reduceMotion: Flow<Boolean> = flag(PreferencesKeys.REDUCE_MOTION, false)
    val dailyReminderEnabled: Flow<Boolean> = flag(PreferencesKeys.DAILY_REMINDER_ENABLED, false)
    val reminderPromptShown: Flow<Boolean> = flag(PreferencesKeys.REMINDER_PROMPT_SHOWN, false)

    suspend fun setSoundEnabled(enabled: Boolean) = setFlag(PreferencesKeys.SOUND_ENABLED, enabled)
    suspend fun setMusicEnabled(enabled: Boolean) = setFlag(PreferencesKeys.MUSIC_ENABLED, enabled)
    suspend fun setHapticsEnabled(enabled: Boolean) = setFlag(PreferencesKeys.HAPTICS_ENABLED, enabled)
    suspend fun setReduceMotion(enabled: Boolean) = setFlag(PreferencesKeys.REDUCE_MOTION, enabled)
    suspend fun setDailyReminderEnabled(enabled: Boolean) = setFlag(PreferencesKeys.DAILY_REMINDER_ENABLED, enabled)
    suspend fun setReminderPromptShown(shown: Boolean) = setFlag(PreferencesKeys.REMINDER_PROMPT_SHOWN, shown)

    fun isMechanicSeen(mechanic: String): Flow<Boolean> =
        flag(booleanPreferencesKey("mechanic_seen_$mechanic"), false)

    suspend fun setMechanicSeen(mechanic: String) =
        setFlag(booleanPreferencesKey("mechanic_seen_$mechanic"), true)
}
