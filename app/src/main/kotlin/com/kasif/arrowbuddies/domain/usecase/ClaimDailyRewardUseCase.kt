package com.kasif.arrowbuddies.domain.usecase

import androidx.room.withTransaction
import com.kasif.arrowbuddies.core.economy.EconomyConfig
import com.kasif.arrowbuddies.core.model.AchievementType
import com.kasif.arrowbuddies.data.db.AppDatabase
import com.kasif.arrowbuddies.data.db.entity.PlayerProfile
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

data class StreakClaimResult(
    val success: Boolean,
    val streakDays: Int,
    val berriesAwarded: Int,
    val day7DecorUnlocked: Boolean
)

@Singleton
class ClaimDailyRewardUseCase @Inject constructor(
    private val database: AppDatabase
) {
    suspend fun claimTreatTime(todayDate: String): Boolean = database.withTransaction {
        val profileDao = database.playerProfileDao()
        val profile = profileDao.getPlayerProfileOnce() ?: PlayerProfile()
        if (profile.lastTreatTimeDate == todayDate) {
            return@withTransaction false
        }

        profileDao.insertOrUpdate(
            profile.copy(
                berries = profile.berries + EconomyConfig.BERRIES_TREAT_TIME,
                lastTreatTimeDate = todayDate
            )
        )
        true
    }

    suspend fun claimDailyStreak(today: LocalDate): StreakClaimResult = database.withTransaction {
        val profileDao = database.playerProfileDao()
        val decorDao = database.decorDao()
        val achievementDao = database.achievementDao()

        val profile = profileDao.getPlayerProfileOnce() ?: PlayerProfile()
        val formatter = DateTimeFormatter.BASIC_ISO_DATE // "yyyyMMdd"
        val todayStr = today.format(formatter)
        val yesterdayStr = today.minusDays(1).format(formatter)

        if (profile.lastStreakDate == todayStr) {
            return@withTransaction StreakClaimResult(
                success = false,
                streakDays = profile.streakDays,
                berriesAwarded = 0,
                day7DecorUnlocked = false
            )
        }

        val newStreakDays = if (profile.lastStreakDate == yesterdayStr) {
            (profile.streakDays % 7) + 1
        } else {
            1
        }

        val rewardIdx = (newStreakDays - 1).coerceIn(0, 6)
        val berries = EconomyConfig.STREAK_REWARDS[rewardIdx]
        val unlockedDecor = newStreakDays == 7

        if (unlockedDecor) {
            decorDao.markOwned(EconomyConfig.DAY_7_STREAK_DECOR_ID)
            achievementDao.unlock(AchievementType.STREAK_7.id)
        }

        profileDao.insertOrUpdate(
            profile.copy(
                berries = profile.berries + berries,
                streakDays = newStreakDays,
                lastStreakDate = todayStr
            )
        )

        StreakClaimResult(
            success = true,
            streakDays = newStreakDays,
            berriesAwarded = berries,
            day7DecorUnlocked = unlockedDecor
        )
    }
}

