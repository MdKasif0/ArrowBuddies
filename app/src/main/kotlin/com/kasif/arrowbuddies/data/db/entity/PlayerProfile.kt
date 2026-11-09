package com.kasif.arrowbuddies.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.kasif.arrowbuddies.core.economy.EconomyConfig

@Entity(tableName = "player_profile")
data class PlayerProfile(
    @PrimaryKey val id: Int = 1,
    val berries: Int = EconomyConfig.INITIAL_BERRIES,
    val hints: Int = EconomyConfig.INITIAL_HINTS,
    val undos: Int = EconomyConfig.INITIAL_UNDOS,
    val totalRescued: Int = 0,
    val tutorialDone: Boolean = false,
    val currentLevelIndex: Int = 1,
    val highestUnlockedLevel: Int = 1,
    val streakDays: Int = 0,
    val lastStreakDate: String? = null,
    val lastTreatTimeDate: String? = null
)

