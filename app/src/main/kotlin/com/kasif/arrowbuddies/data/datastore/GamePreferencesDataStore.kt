package com.kasif.arrowbuddies.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
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
        val REDUCE_MOTION = booleanPreferencesKey("reduce_motion")
        val TUTORIAL_COMPLETED = booleanPreferencesKey("tutorial_completed")
        val CURRENT_LEVEL_INDEX = intPreferencesKey("current_level_index")
        val HIGHEST_UNLOCKED_LEVEL = intPreferencesKey("highest_unlocked_level")
        val TOTAL_RESCUED = intPreferencesKey("total_rescued")
        val BERRIES = intPreferencesKey("berries")
        val HINTS_COUNT = intPreferencesKey("hints_count")
        val UNDOS_COUNT = intPreferencesKey("undos_count")
        val RESCUED_SPECIES = stringSetPreferencesKey("rescued_species")
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

    val reduceMotion: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.REDUCE_MOTION] ?: false
    }

    val tutorialCompleted: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.TUTORIAL_COMPLETED] ?: false
    }

    val currentLevelIndex: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.CURRENT_LEVEL_INDEX] ?: 1
    }

    val highestUnlockedLevel: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.HIGHEST_UNLOCKED_LEVEL] ?: 1
    }

    val totalRescued: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.TOTAL_RESCUED] ?: 0
    }

    val berries: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.BERRIES] ?: 100
    }

    val hintsCount: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.HINTS_COUNT] ?: 3
    }

    val undosCount: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.UNDOS_COUNT] ?: 3
    }

    val rescuedSpecies: Flow<Set<String>> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.RESCUED_SPECIES] ?: emptySet()
    }

    fun getLevelStars(levelId: String): Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[intPreferencesKey("stars_$levelId")] ?: 0
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

    suspend fun setReduceMotion(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.REDUCE_MOTION] = enabled
        }
    }

    suspend fun setTutorialCompleted(completed: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.TUTORIAL_COMPLETED] = completed
        }
    }

    suspend fun setCurrentLevelIndex(index: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.CURRENT_LEVEL_INDEX] = index
            val currentHighest = preferences[PreferencesKeys.HIGHEST_UNLOCKED_LEVEL] ?: 1
            if (index > currentHighest) {
                preferences[PreferencesKeys.HIGHEST_UNLOCKED_LEVEL] = index
            }
        }
    }

    suspend fun addBerries(amount: Int) {
        context.dataStore.edit { preferences ->
            val curr = preferences[PreferencesKeys.BERRIES] ?: 100
            preferences[PreferencesKeys.BERRIES] = (curr + amount).coerceAtLeast(0)
        }
    }

    suspend fun spendBerries(amount: Int): Boolean {
        var success = false
        context.dataStore.edit { preferences ->
            val curr = preferences[PreferencesKeys.BERRIES] ?: 100
            if (curr >= amount) {
                preferences[PreferencesKeys.BERRIES] = curr - amount
                success = true
            }
        }
        return success
    }

    suspend fun useHint(): Boolean {
        var used = false
        context.dataStore.edit { preferences ->
            val curr = preferences[PreferencesKeys.HINTS_COUNT] ?: 3
            if (curr > 0) {
                preferences[PreferencesKeys.HINTS_COUNT] = curr - 1
                used = true
            }
        }
        return used
    }

    suspend fun buyHintWithBerries(price: Int = 30): Boolean {
        var bought = false
        context.dataStore.edit { preferences ->
            val berries = preferences[PreferencesKeys.BERRIES] ?: 100
            if (berries >= price) {
                preferences[PreferencesKeys.BERRIES] = berries - price
                val hints = preferences[PreferencesKeys.HINTS_COUNT] ?: 3
                preferences[PreferencesKeys.HINTS_COUNT] = hints + 1
                bought = true
            }
        }
        return bought
    }

    suspend fun useUndo(): Boolean {
        var used = false
        context.dataStore.edit { preferences ->
            val curr = preferences[PreferencesKeys.UNDOS_COUNT] ?: 3
            if (curr > 0) {
                preferences[PreferencesKeys.UNDOS_COUNT] = curr - 1
                used = true
            }
        }
        return used
    }

    suspend fun buyUndoWithBerries(price: Int = 20): Boolean {
        var bought = false
        context.dataStore.edit { preferences ->
            val berries = preferences[PreferencesKeys.BERRIES] ?: 100
            if (berries >= price) {
                preferences[PreferencesKeys.BERRIES] = berries - price
                val undos = preferences[PreferencesKeys.UNDOS_COUNT] ?: 3
                preferences[PreferencesKeys.UNDOS_COUNT] = undos + 1
                bought = true
            }
        }
        return bought
    }

    suspend fun recordLevelWin(
        levelId: String,
        stars: Int,
        berriesEarned: Int,
        rescuedCount: Int,
        newSpecies: List<String>
    ) {
        context.dataStore.edit { preferences ->
            // Update stars
            val currentBestStars = preferences[intPreferencesKey("stars_$levelId")] ?: 0
            if (stars > currentBestStars) {
                preferences[intPreferencesKey("stars_$levelId")] = stars
            }

            // Update berries
            val currBerries = preferences[PreferencesKeys.BERRIES] ?: 100
            preferences[PreferencesKeys.BERRIES] = currBerries + berriesEarned

            // Update total rescued
            val currRescued = preferences[PreferencesKeys.TOTAL_RESCUED] ?: 0
            preferences[PreferencesKeys.TOTAL_RESCUED] = currRescued + rescuedCount

            // Update unlocked species
            val currSpecies = preferences[PreferencesKeys.RESCUED_SPECIES] ?: emptySet()
            if (newSpecies.isNotEmpty()) {
                preferences[PreferencesKeys.RESCUED_SPECIES] = currSpecies + newSpecies
            }

            // Unlock next level if this was the current highest
            val match = Regex("""w([1-8])-l([0-3][0-9])""").matchEntire(levelId)
            if (match != null) {
                val world = match.groupValues[1].toInt()
                val idx = match.groupValues[2].toInt()
                val flatIndex = ((world - 1) * 30) + idx
                val currentHighest = preferences[PreferencesKeys.HIGHEST_UNLOCKED_LEVEL] ?: 1
                if (flatIndex >= currentHighest) {
                    preferences[PreferencesKeys.HIGHEST_UNLOCKED_LEVEL] = flatIndex + 1
                    preferences[PreferencesKeys.CURRENT_LEVEL_INDEX] = flatIndex + 1
                }
            }
        }
    }

    suspend fun resetTutorial() {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.TUTORIAL_COMPLETED] = false
            preferences[PreferencesKeys.CURRENT_LEVEL_INDEX] = 1
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
