package com.kasif.arrowbuddies.data.repository

import com.kasif.arrowbuddies.core.economy.EconomyConfig
import com.kasif.arrowbuddies.core.model.AchievementType
import com.kasif.arrowbuddies.core.model.BuddyAccessory
import com.kasif.arrowbuddies.data.datastore.GamePreferencesDataStore
import com.kasif.arrowbuddies.data.db.AppDatabase
import com.kasif.arrowbuddies.data.db.entity.Achievement
import com.kasif.arrowbuddies.data.db.entity.DailyRecord
import com.kasif.arrowbuddies.data.db.entity.DecorItem
import com.kasif.arrowbuddies.data.db.entity.LevelProgress
import com.kasif.arrowbuddies.data.db.entity.PlayerProfile
import com.kasif.arrowbuddies.data.db.entity.SpeciesProgress
import com.kasif.arrowbuddies.domain.usecase.BuyDecorUseCase
import com.kasif.arrowbuddies.domain.usecase.ClaimDailyRewardUseCase
import com.kasif.arrowbuddies.domain.usecase.CompleteLevelUseCase
import com.kasif.arrowbuddies.domain.usecase.EquipAccessoryUseCase
import com.kasif.arrowbuddies.domain.usecase.LevelCompletionResult
import com.kasif.arrowbuddies.domain.usecase.SpendBerriesUseCase
import com.kasif.arrowbuddies.domain.usecase.StreakClaimResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
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

    // Extended Meta Progression Flows
    val playerProfile: Flow<PlayerProfile>
    val speciesProgressList: Flow<List<SpeciesProgress>>
    val decorItems: Flow<List<DecorItem>>
    val achievements: Flow<List<Achievement>>
    val dailyReminderEnabled: Flow<Boolean>
    val reminderPromptShown: Flow<Boolean>

    fun getLevelStars(levelId: String): Flow<Int>
    fun isMechanicSeen(mechanic: String): Flow<Boolean>
    fun getDailyRecord(date: String): Flow<DailyRecord?>

    suspend fun setSoundEnabled(enabled: Boolean)
    suspend fun setMusicEnabled(enabled: Boolean)
    suspend fun setHapticsEnabled(enabled: Boolean)
    suspend fun setReduceMotion(enabled: Boolean)
    suspend fun setTutorialCompleted(completed: Boolean)
    suspend fun setCurrentLevelIndex(index: Int)
    suspend fun addBerries(amount: Int)
    suspend fun spendBerries(amount: Int): Boolean
    suspend fun useHint(): Boolean
    suspend fun buyHintWithBerries(price: Int = EconomyConfig.COST_HINT): Boolean
    suspend fun useUndo(): Boolean
    suspend fun buyUndoWithBerries(price: Int = EconomyConfig.COST_UNDO): Boolean
    suspend fun recordLevelWin(
        levelId: String,
        stars: Int,
        berriesEarned: Int,
        rescuedCount: Int,
        newSpecies: List<String>
    )
    suspend fun completeLevel(
        levelId: String,
        stars: Int,
        mistakes: Int,
        rescuedSpeciesList: List<String>,
        isGoldenBuddyRescued: Boolean = false
    ): LevelCompletionResult
    suspend fun claimTreatTime(todayDate: String): Boolean
    suspend fun claimDailyStreak(today: LocalDate): StreakClaimResult
    suspend fun equipAccessory(species: String, accessory: BuddyAccessory): Boolean
    suspend fun buyDecor(decorId: String): Boolean
    suspend fun recordDailyCompletion(date: String, stars: Int, mistakes: Int)
    suspend fun resetTutorial()
    suspend fun setMechanicSeen(mechanic: String)
    suspend fun setDailyReminderEnabled(enabled: Boolean)
    suspend fun setReminderPromptShown(shown: Boolean)
}

@Singleton
class DefaultGameRepository @Inject constructor(
    private val database: AppDatabase,
    private val preferences: GamePreferencesDataStore,
    private val completeLevelUseCase: CompleteLevelUseCase,
    private val spendBerriesUseCase: SpendBerriesUseCase,
    private val claimDailyRewardUseCase: ClaimDailyRewardUseCase,
    private val equipAccessoryUseCase: EquipAccessoryUseCase,
    private val buyDecorUseCase: BuyDecorUseCase
) : GameRepository {

    private val profileDao = database.playerProfileDao()
    private val levelDao = database.levelProgressDao()
    private val speciesDao = database.speciesProgressDao()
    private val decorDao = database.decorDao()
    private val dailyDao = database.dailyRecordDao()
    private val achievementDao = database.achievementDao()

    init {
        CoroutineScope(Dispatchers.IO).launch {
            initDefaults()
        }
    }

    private suspend fun initDefaults() {
        // Ensure default player profile exists
        if (profileDao.getPlayerProfileOnce() == null) {
            profileDao.insertOrUpdate(PlayerProfile())
        }

        // Initialize 8 Zone Decorations if not yet inserted
        val defaultDecor = listOf(
            DecorItem("decor_pond_1", owned = false, slot = "z1_s1", zoneId = 1, costBerries = 150),
            DecorItem("decor_swing_2", owned = false, slot = "z2_s1", zoneId = 2, costBerries = 200),
            DecorItem("decor_flower_bed_3", owned = false, slot = "z3_s1", zoneId = 3, costBerries = 250),
            DecorItem("decor_firepit_4", owned = false, slot = "z4_s1", zoneId = 4, costBerries = 300),
            DecorItem("decor_hammock_5", owned = false, slot = "z5_s1", zoneId = 5, costBerries = 350),
            DecorItem("decor_lanterns_6", owned = false, slot = "z6_s1", zoneId = 6, costBerries = 400),
            DecorItem("decor_kite_7", owned = false, slot = "z7_s1", zoneId = 7, costBerries = 450),
            DecorItem("decor_rangoli_mat_8", owned = false, slot = "z8_s1", zoneId = 8, costBerries = 600)
        )
        decorDao.insertAll(defaultDecor)

        // Initialize 12 Achievements
        val defaultAchievements = AchievementType.entries.map { type ->
            Achievement(
                id = type.id,
                unlockedAt = 0L,
                progress = 0,
                maxProgress = type.maxProgress
            )
        }
        achievementDao.insertAll(defaultAchievements)
    }

    override val playerProfile: Flow<PlayerProfile> = profileDao.getPlayerProfile().map { it ?: PlayerProfile() }
    override val speciesProgressList: Flow<List<SpeciesProgress>> = speciesDao.getAllSpeciesProgress()
    override val decorItems: Flow<List<DecorItem>> = decorDao.getAllDecorItems()
    override val achievements: Flow<List<Achievement>> = achievementDao.getAllAchievements()
    override val dailyReminderEnabled: Flow<Boolean> = preferences.dailyReminderEnabled
    override val reminderPromptShown: Flow<Boolean> = preferences.reminderPromptShown

    override val totalRescued: Flow<Int> = playerProfile.map { it.totalRescued }
    override val berries: Flow<Int> = playerProfile.map { it.berries }
    override val soundEnabled: Flow<Boolean> = preferences.soundEnabled
    override val musicEnabled: Flow<Boolean> = preferences.musicEnabled
    override val hapticsEnabled: Flow<Boolean> = preferences.hapticsEnabled
    override val reduceMotion: Flow<Boolean> = preferences.reduceMotion
    override val tutorialCompleted: Flow<Boolean> = playerProfile.map { it.tutorialDone }
    override val currentLevelIndex: Flow<Int> = playerProfile.map { it.currentLevelIndex }
    override val highestUnlockedLevel: Flow<Int> = playerProfile.map { it.highestUnlockedLevel }
    override val hintsCount: Flow<Int> = playerProfile.map { it.hints }
    override val undosCount: Flow<Int> = playerProfile.map { it.undos }
    override val rescuedSpecies: Flow<Set<String>> = speciesProgressList.map { list -> list.map { it.species }.toSet() }

    override fun getLevelStars(levelId: String): Flow<Int> =
        levelDao.getLevelStars(levelId).map { it ?: 0 }

    override fun isMechanicSeen(mechanic: String): Flow<Boolean> = preferences.isMechanicSeen(mechanic)
    override fun getDailyRecord(date: String): Flow<DailyRecord?> = dailyDao.getDailyRecord(date)

    override suspend fun setSoundEnabled(enabled: Boolean) = preferences.setSoundEnabled(enabled)
    override suspend fun setMusicEnabled(enabled: Boolean) = preferences.setMusicEnabled(enabled)
    override suspend fun setHapticsEnabled(enabled: Boolean) = preferences.setHapticsEnabled(enabled)
    override suspend fun setReduceMotion(enabled: Boolean) = preferences.setReduceMotion(enabled)
    override suspend fun setTutorialCompleted(completed: Boolean) {
        val p = profileDao.getPlayerProfileOnce() ?: PlayerProfile()
        profileDao.insertOrUpdate(p.copy(tutorialDone = completed))
        preferences.setTutorialCompleted(completed)
    }
    override suspend fun setCurrentLevelIndex(index: Int) {
        val p = profileDao.getPlayerProfileOnce() ?: PlayerProfile()
        profileDao.insertOrUpdate(p.copy(currentLevelIndex = index, highestUnlockedLevel = maxOf(p.highestUnlockedLevel, index)))
        preferences.setCurrentLevelIndex(index)
    }

    override suspend fun addBerries(amount: Int) {
        profileDao.addBerries(amount)
        preferences.addBerries(amount)
    }

    override suspend fun spendBerries(amount: Int): Boolean {
        val success = spendBerriesUseCase(amount)
        if (success) preferences.spendBerries(amount)
        return success
    }

    override suspend fun useHint(): Boolean {
        val updated = profileDao.useHint()
        val success = updated > 0
        if (success) preferences.useHint()
        return success
    }

    override suspend fun buyHintWithBerries(price: Int): Boolean {
        val p = profileDao.getPlayerProfileOnce() ?: return false
        if (p.berries >= price) {
            profileDao.insertOrUpdate(p.copy(berries = p.berries - price, hints = p.hints + 1))
            preferences.buyHintWithBerries(price)
            return true
        }
        return false
    }

    override suspend fun useUndo(): Boolean {
        val updated = profileDao.useUndo()
        val success = updated > 0
        if (success) preferences.useUndo()
        return success
    }

    override suspend fun buyUndoWithBerries(price: Int): Boolean {
        val p = profileDao.getPlayerProfileOnce() ?: return false
        if (p.berries >= price) {
            profileDao.insertOrUpdate(p.copy(berries = p.berries - price, undos = p.undos + 1))
            preferences.buyUndoWithBerries(price)
            return true
        }
        return false
    }

    override suspend fun recordLevelWin(
        levelId: String,
        stars: Int,
        berriesEarned: Int,
        rescuedCount: Int,
        newSpecies: List<String>
    ) {
        completeLevelUseCase(
            levelId = levelId,
            stars = stars,
            mistakes = (3 - stars).coerceAtLeast(0),
            rescuedSpeciesList = newSpecies
        )
        preferences.recordLevelWin(levelId, stars, berriesEarned, rescuedCount, newSpecies)
    }

    override suspend fun completeLevel(
        levelId: String,
        stars: Int,
        mistakes: Int,
        rescuedSpeciesList: List<String>,
        isGoldenBuddyRescued: Boolean
    ): LevelCompletionResult {
        val result = completeLevelUseCase(
            levelId = levelId,
            stars = stars,
            mistakes = mistakes,
            rescuedSpeciesList = rescuedSpeciesList,
            isGoldenBuddyRescued = isGoldenBuddyRescued
        )
        preferences.recordLevelWin(levelId, stars, result.berriesEarned, rescuedSpeciesList.size, rescuedSpeciesList)
        return result
    }

    override suspend fun claimTreatTime(todayDate: String): Boolean =
        claimDailyRewardUseCase.claimTreatTime(todayDate)

    override suspend fun claimDailyStreak(today: LocalDate): StreakClaimResult =
        claimDailyRewardUseCase.claimDailyStreak(today)

    override suspend fun equipAccessory(species: String, accessory: BuddyAccessory): Boolean =
        equipAccessoryUseCase(species, accessory)

    override suspend fun buyDecor(decorId: String): Boolean =
        buyDecorUseCase(decorId)

    override suspend fun recordDailyCompletion(date: String, stars: Int, mistakes: Int) {
        val existing = dailyDao.getDailyRecordOnce(date)
        dailyDao.insertOrUpdate(
            DailyRecord(
                date = date,
                completed = true,
                stars = maxOf(existing?.stars ?: 0, stars),
                mistakes = if (existing != null) minOf(existing.mistakes, mistakes) else mistakes,
                completedAt = System.currentTimeMillis()
            )
        )
        // Check 7-day streak claim
        val today = runCatching { LocalDate.parse(date, DateTimeFormatter.BASIC_ISO_DATE) }.getOrDefault(LocalDate.now())
        claimDailyStreak(today)
    }

    override suspend fun resetTutorial() {
        val p = profileDao.getPlayerProfileOnce() ?: PlayerProfile()
        profileDao.insertOrUpdate(p.copy(tutorialDone = false))
        preferences.resetTutorial()
    }

    override suspend fun setMechanicSeen(mechanic: String) = preferences.setMechanicSeen(mechanic)

    override suspend fun setDailyReminderEnabled(enabled: Boolean) = preferences.setDailyReminderEnabled(enabled)

    override suspend fun setReminderPromptShown(shown: Boolean) = preferences.setReminderPromptShown(shown)
}
