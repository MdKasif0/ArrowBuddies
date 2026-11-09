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
    fun isMechanicSeen(mechanic: String): Flow<Boolean>
    suspend fun setSoundEnabled(enabled: Boolean)
    suspend fun setMusicEnabled(enabled: Boolean)
    suspend fun setHapticsEnabled(enabled: Boolean)
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
    override fun isMechanicSeen(mechanic: String): Flow<Boolean> = preferences.isMechanicSeen(mechanic)

    override suspend fun setSoundEnabled(enabled: Boolean) = preferences.setSoundEnabled(enabled)
    override suspend fun setMusicEnabled(enabled: Boolean) = preferences.setMusicEnabled(enabled)
    override suspend fun setHapticsEnabled(enabled: Boolean) = preferences.setHapticsEnabled(enabled)
    override suspend fun setMechanicSeen(mechanic: String) = preferences.setMechanicSeen(mechanic)
}

