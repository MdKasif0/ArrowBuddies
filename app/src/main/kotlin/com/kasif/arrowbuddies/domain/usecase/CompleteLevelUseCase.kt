package com.kasif.arrowbuddies.domain.usecase

import androidx.room.withTransaction
import com.kasif.arrowbuddies.core.economy.EconomyConfig
import com.kasif.arrowbuddies.core.model.AchievementType
import com.kasif.arrowbuddies.core.model.BuddyAccessory
import com.kasif.arrowbuddies.core.model.FriendshipTier
import com.kasif.arrowbuddies.data.db.AppDatabase
import com.kasif.arrowbuddies.data.db.entity.LevelProgress
import com.kasif.arrowbuddies.data.db.entity.PlayerProfile
import com.kasif.arrowbuddies.data.db.entity.SpeciesProgress
import javax.inject.Inject
import javax.inject.Singleton

data class TierUpEvent(
    val species: String,
    val newTier: FriendshipTier,
    val unlockedAccessory: BuddyAccessory
)

data class LevelCompletionResult(
    val berriesEarned: Int,
    val isFirstClear: Boolean,
    val stars: Int,
    val tierUpEvents: List<TierUpEvent>,
    val unlockedAchievements: List<AchievementType>
)

@Singleton
class CompleteLevelUseCase @Inject constructor(
    private val database: AppDatabase
) {
    suspend operator fun invoke(
        levelId: String,
        stars: Int,
        mistakes: Int,
        rescuedSpeciesList: List<String>,
        isGoldenBuddyRescued: Boolean = false
    ): LevelCompletionResult = database.withTransaction {
        val levelDao = database.levelProgressDao()
        val profileDao = database.playerProfileDao()
        val speciesDao = database.speciesProgressDao()
        val achievementDao = database.achievementDao()

        val existingProgress = levelDao.getLevelProgressOnce(levelId)
        val isFirstClear = existingProgress == null
        val bestStars = maxOf(existingProgress?.bestStars ?: 0, stars)
        val bestMistakes = if (existingProgress != null) minOf(existingProgress.bestMistakes, mistakes) else mistakes

        // Calculate berry rewards
        var earnedBerries = 0
        if (isFirstClear) {
            earnedBerries += EconomyConfig.BERRIES_LEVEL_COMPLETION
            earnedBerries += stars * EconomyConfig.BERRIES_PER_STAR
            if (isGoldenBuddyRescued) {
                earnedBerries += EconomyConfig.BERRIES_GOLDEN_BUDDY_BONUS
            }
        } else {
            // Replay star improvement
            val starDiff = (stars - (existingProgress?.bestStars ?: 0)).coerceAtLeast(0)
            earnedBerries += starDiff * EconomyConfig.BERRIES_PER_STAR
        }

        // 1. Save level progress
        levelDao.insertOrUpdate(
            LevelProgress(
                levelId = levelId,
                bestStars = bestStars,
                bestMistakes = bestMistakes,
                completedAt = System.currentTimeMillis()
            )
        )

        // 2. Update player profile
        val currentProfile = profileDao.getPlayerProfileOnce() ?: PlayerProfile()
        val levelIndex = levelId.filter { it.isDigit() }.toIntOrNull() ?: currentProfile.currentLevelIndex
        val nextLevel = levelIndex + 1
        val newHighest = maxOf(currentProfile.highestUnlockedLevel, nextLevel)
        val newCurrent = maxOf(currentProfile.currentLevelIndex, nextLevel)
        val newTotalRescued = currentProfile.totalRescued + rescuedSpeciesList.size

        profileDao.insertOrUpdate(
            currentProfile.copy(
                berries = currentProfile.berries + earnedBerries,
                totalRescued = newTotalRescued,
                tutorialDone = true,
                currentLevelIndex = newCurrent,
                highestUnlockedLevel = newHighest
            )
        )

        // 3. Update species progress & check tier ups
        val tierUpEvents = mutableListOf<TierUpEvent>()
        val speciesCounts = rescuedSpeciesList.groupingBy { it }.eachCount()

        for ((speciesName, count) in speciesCounts) {
            val existing = speciesDao.getSpeciesProgressOnce(speciesName)
            val currentRescued = (existing?.rescuedCount ?: 0) + count
            val oldTier = existing?.tier ?: FriendshipTier.LOCKED

            val newTier = when {
                currentRescued >= EconomyConfig.TIER_GOLD_THRESHOLD -> FriendshipTier.GOLD
                currentRescued >= EconomyConfig.TIER_SILVER_THRESHOLD -> FriendshipTier.SILVER
                currentRescued >= EconomyConfig.TIER_BRONZE_THRESHOLD -> FriendshipTier.BRONZE
                else -> FriendshipTier.LOCKED
            }

            val newlyUnlockedAccessory = when {
                oldTier < FriendshipTier.BRONZE && newTier >= FriendshipTier.BRONZE -> BuddyAccessory.BOW
                oldTier < FriendshipTier.SILVER && newTier >= FriendshipTier.SILVER -> BuddyAccessory.SCARF
                oldTier < FriendshipTier.GOLD && newTier >= FriendshipTier.GOLD -> BuddyAccessory.CROWN
                else -> null
            }

            if (newlyUnlockedAccessory != null) {
                tierUpEvents.add(
                    TierUpEvent(
                        species = speciesName,
                        newTier = newTier,
                        unlockedAccessory = newlyUnlockedAccessory
                    )
                )
            }

            val equipped = existing?.equippedAccessory ?: BuddyAccessory.NONE
            speciesDao.insertOrUpdate(
                SpeciesProgress(
                    species = speciesName,
                    rescuedCount = currentRescued,
                    tier = newTier,
                    equippedAccessory = equipped,
                    firstRescuedAt = existing?.firstRescuedAt?.takeIf { it > 0 } ?: System.currentTimeMillis()
                )
            )
        }

        // 4. Check achievements
        val newlyUnlockedAchievements = mutableListOf<AchievementType>()
        suspend fun checkAndUnlock(type: AchievementType, condition: Boolean) {
            if (condition) {
                val updated = achievementDao.unlock(type.id)
                if (updated > 0) newlyUnlockedAchievements.add(type)
            }
        }

        checkAndUnlock(AchievementType.FIRST_RESCUE, newTotalRescued >= 1)
        checkAndUnlock(AchievementType.RESCUED_10, newTotalRescued >= 10)
        checkAndUnlock(AchievementType.RESCUED_100, newTotalRescued >= 100)
        checkAndUnlock(AchievementType.RESCUED_500, newTotalRescued >= 500)
        checkAndUnlock(AchievementType.RESCUED_1000, newTotalRescued >= 1000)

        // Perfect World check
        val allProgress = levelDao.getAllProgressOnce()
        for (world in 1..8) {
            val worldLevels = (1..30).map { String.format("w%d-l%02d", world, it) }
            val hasAll3Stars = worldLevels.all { wId ->
                allProgress.any { it.levelId == wId && it.bestStars == 3 }
            }
            if (hasAll3Stars) {
                checkAndUnlock(AchievementType.PERFECT_WORLD, true)
                break
            }
        }

        // Zero-Bonk Big Rescue check (levels 15 and 30)
        val isBigRescue = levelId.endsWith("-l15") || levelId.endsWith("-l30") || levelId in listOf("15", "30")
        if (isBigRescue && mistakes == 0) {
            checkAndUnlock(AchievementType.ZERO_BONK_BOSS, true)
        }

        // Collector (all 24 species discovered)
        val allSpeciesProgress = speciesDao.getAllSpeciesProgressOnce()
        if (allSpeciesProgress.size >= 24) {
            checkAndUnlock(AchievementType.COLLECTOR, true)
        }

        LevelCompletionResult(
            berriesEarned = earnedBerries,
            isFirstClear = isFirstClear,
            stars = stars,
            tierUpEvents = tierUpEvents,
            unlockedAchievements = newlyUnlockedAchievements
        )
    }
}

