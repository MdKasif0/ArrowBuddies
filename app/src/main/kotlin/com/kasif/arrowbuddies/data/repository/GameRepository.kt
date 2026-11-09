package com.kasif.arrowbuddies.data.repository

import com.kasif.arrowbuddies.data.datastore.GamePreferencesDataStore
import com.kasif.arrowbuddies.data.db.AppDatabase
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

interface GameRepository {
    val totalRescued: Flow<Int>
    val berries: Flow<Int>
    val soundEnabled: Flow<Boolean>
    val musicEnabled: Flow<Boolean>
    val hapticsEnabled: Flow<Boolean>
    val reduceMotion: Flow<Boolean>
    val tutorialCompleted: Flow<Boolean>
    val currentLevelIndex: Flow<Int>
    val highestUnlockedLevel: Flow<Int>
    val hintsCount: Flow<Int>
    val undosCount: Flow<Int>
    val rescuedSpecies: Flow<Set<String>>

    fun getLevelStars(levelId: String): Flow<Int>
    fun isMechanicSeen(mechanic: String): Flow<Boolean>

    suspend fun setSoundEnabled(enabled: Boolean)
    suspend fun setMusicEnabled(enabled: Boolean)
    suspend fun setHapticsEnabled(enabled: Boolean)
    suspend fun setReduceMotion(enabled: Boolean)
    suspend fun setTutorialCompleted(completed: Boolean)
    suspend fun setCurrentLevelIndex(index: Int)
    suspend fun addBerries(amount: Int)
    suspend fun spendBerries(amount: Int): Boolean
    suspend fun useHint(): Boolean
    suspend fun buyHintWithBerries(price: Int = 30): Boolean
    suspend fun useUndo(): Boolean
    suspend fun buyUndoWithBerries(price: Int = 20): Boolean
    suspend fun recordLevelWin(
        levelId: String,
        stars: Int,
        berriesEarned: Int,
        rescuedCount: Int,
        newSpecies: List<String>
    )
    suspend fun resetTutorial()
    suspend fun setMechanicSeen(mechanic: String)
}

@Singleton
class DefaultGameRepository @Inject constructor(
    private val database: AppDatabase,
    private val preferences: GamePreferencesDataStore
) : GameRepository {
    override val totalRescued: Flow<Int> = preferences.totalRescued
    override val berries: Flow<Int> = preferences.berries
    override val soundEnabled: Flow<Boolean> = preferences.soundEnabled
    override val musicEnabled: Flow<Boolean> = preferences.musicEnabled
    override val hapticsEnabled: Flow<Boolean> = preferences.hapticsEnabled
    override val reduceMotion: Flow<Boolean> = preferences.reduceMotion
    override val tutorialCompleted: Flow<Boolean> = preferences.tutorialCompleted
    override val currentLevelIndex: Flow<Int> = preferences.currentLevelIndex
    override val highestUnlockedLevel: Flow<Int> = preferences.highestUnlockedLevel
    override val hintsCount: Flow<Int> = preferences.hintsCount
    override val undosCount: Flow<Int> = preferences.undosCount
    override val rescuedSpecies: Flow<Set<String>> = preferences.rescuedSpecies

    override fun getLevelStars(levelId: String): Flow<Int> = preferences.getLevelStars(levelId)
    override fun isMechanicSeen(mechanic: String): Flow<Boolean> = preferences.isMechanicSeen(mechanic)

    override suspend fun setSoundEnabled(enabled: Boolean) = preferences.setSoundEnabled(enabled)
    override suspend fun setMusicEnabled(enabled: Boolean) = preferences.setMusicEnabled(enabled)
    override suspend fun setHapticsEnabled(enabled: Boolean) = preferences.setHapticsEnabled(enabled)
    override suspend fun setReduceMotion(enabled: Boolean) = preferences.setReduceMotion(enabled)
    override suspend fun setTutorialCompleted(completed: Boolean) = preferences.setTutorialCompleted(completed)
    override suspend fun setCurrentLevelIndex(index: Int) = preferences.setCurrentLevelIndex(index)
    override suspend fun addBerries(amount: Int) = preferences.addBerries(amount)
    override suspend fun spendBerries(amount: Int): Boolean = preferences.spendBerries(amount)
    override suspend fun useHint(): Boolean = preferences.useHint()
    override suspend fun buyHintWithBerries(price: Int): Boolean = preferences.buyHintWithBerries(price)
    override suspend fun useUndo(): Boolean = preferences.useUndo()
    override suspend fun buyUndoWithBerries(price: Int): Boolean = preferences.buyUndoWithBerries(price)
    override suspend fun recordLevelWin(
        levelId: String,
        stars: Int,
        berriesEarned: Int,
        rescuedCount: Int,
        newSpecies: List<String>
    ) = preferences.recordLevelWin(levelId, stars, berriesEarned, rescuedCount, newSpecies)
    override suspend fun resetTutorial() = preferences.resetTutorial()
    override suspend fun setMechanicSeen(mechanic: String) = preferences.setMechanicSeen(mechanic)
}
