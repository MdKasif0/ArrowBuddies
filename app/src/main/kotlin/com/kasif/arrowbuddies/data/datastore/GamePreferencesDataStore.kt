package com.kasif.arrowbuddies.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.kasif.arrowbuddies.BuildConfig
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
        val ADS_ENABLED_OVERRIDE = booleanPreferencesKey("ads_enabled_override")
        val DAILY_AD_HINTS_COUNT = intPreferencesKey("daily_ad_hints_count")
        val LAST_AD_HINT_DATE = stringPreferencesKey("last_ad_hint_date")
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

    val adsEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        (preferences[PreferencesKeys.ADS_ENABLED_OVERRIDE] ?: true) && BuildConfig.ADS_ENABLED
    }

    suspend fun setAdsEnabledOverride(enabled: Boolean) =
        setFlag(PreferencesKeys.ADS_ENABLED_OVERRIDE, enabled)

    suspend fun getRemainingDailyAdHints(todayDate: String): Int {
        var remaining = 3
        context.dataStore.edit { preferences ->
            val lastDate = preferences[PreferencesKeys.LAST_AD_HINT_DATE] ?: ""
            if (lastDate != todayDate) {
                preferences[PreferencesKeys.LAST_AD_HINT_DATE] = todayDate
                preferences[PreferencesKeys.DAILY_AD_HINTS_COUNT] = 0
                remaining = 3
            } else {
                val currentCount = preferences[PreferencesKeys.DAILY_AD_HINTS_COUNT] ?: 0
                remaining = (3 - currentCount).coerceAtLeast(0)
            }
        }
        return remaining
    }

    suspend fun claimDailyAdHint(todayDate: String): Boolean {
        var success = false
        context.dataStore.edit { preferences ->
            val lastDate = preferences[PreferencesKeys.LAST_AD_HINT_DATE] ?: ""
            val currentCount = if (lastDate == todayDate) {
                preferences[PreferencesKeys.DAILY_AD_HINTS_COUNT] ?: 0
            } else {
                preferences[PreferencesKeys.LAST_AD_HINT_DATE] = todayDate
                0
            }

            if (currentCount < 3) {
                preferences[PreferencesKeys.DAILY_AD_HINTS_COUNT] = currentCount + 1
                success = true
            }
        }
        return success
    }

    fun getDailyAdHintsCount(todayDate: String): Flow<Int> = context.dataStore.data.map { preferences ->
        val lastDate = preferences[PreferencesKeys.LAST_AD_HINT_DATE] ?: ""
        if (lastDate == todayDate) {
            preferences[PreferencesKeys.DAILY_AD_HINTS_COUNT] ?: 0
        } else {
            0
        }
    }
}
