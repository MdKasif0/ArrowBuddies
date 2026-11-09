package com.kasif.arrowbuddies.feature.album

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.kasif.arrowbuddies.core.audio.SoundManager
import com.kasif.arrowbuddies.core.haptics.HapticsManager
import com.kasif.arrowbuddies.core.model.AchievementType
import com.kasif.arrowbuddies.core.model.BuddyAccessory
import com.kasif.arrowbuddies.core.model.FriendshipTier
import com.kasif.arrowbuddies.data.db.entity.Achievement
import com.kasif.arrowbuddies.data.db.entity.DecorItem
import com.kasif.arrowbuddies.data.db.entity.PlayerProfile
import com.kasif.arrowbuddies.data.db.entity.SpeciesProgress
import com.kasif.arrowbuddies.data.repository.GameRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class AlbumViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private class FakeGameRepository : GameRepository {
        val profileFlow = MutableStateFlow(PlayerProfile(berries = 250, totalRescued = 60))
        val speciesFlow = MutableStateFlow(
            listOf(
                SpeciesProgress("BUNNY", rescuedCount = 55, tier = FriendshipTier.SILVER, equippedAccessory = BuddyAccessory.BOW, firstRescuedAt = 1000L),
                SpeciesProgress("CHICK", rescuedCount = 15, tier = FriendshipTier.BRONZE, equippedAccessory = BuddyAccessory.NONE, firstRescuedAt = 2000L)
            )
        )
        val achievementsFlow = MutableStateFlow(
            listOf(
                Achievement("first_rescue", unlockedAt = 12345L, progress = 1, maxProgress = 1),
                Achievement("rescued_10", unlockedAt = 12345L, progress = 10, maxProgress = 10),
                Achievement("rescued_100", unlockedAt = 0L, progress = 60, maxProgress = 100)
            )
        )

        var equippedAccessoryMap = mutableMapOf<String, BuddyAccessory>()
        var grantedBerries = 0

        override val playerProfile: Flow<PlayerProfile> = profileFlow
        override val speciesProgressList: Flow<List<SpeciesProgress>> = speciesFlow
        override val decorItems: Flow<List<DecorItem>> = flowOf(emptyList())
        override val achievements: Flow<List<Achievement>> = achievementsFlow
        override val totalRescued: Flow<Int> = MutableStateFlow(60)
        override val berries: Flow<Int> = MutableStateFlow(250)
        override val soundEnabled: Flow<Boolean> = flowOf(true)
        override val musicEnabled: Flow<Boolean> = flowOf(true)
        override val hapticsEnabled: Flow<Boolean> = flowOf(true)
        override val reduceMotion: Flow<Boolean> = flowOf(false)
        override val tutorialCompleted: Flow<Boolean> = flowOf(true)
        override val currentLevelIndex: Flow<Int> = flowOf(10)
        override val highestUnlockedLevel: Flow<Int> = flowOf(10)
        override val hintsCount: Flow<Int> = flowOf(3)
        override val undosCount: Flow<Int> = flowOf(3)
        override val rescuedSpecies: Flow<Set<String>> = flowOf(setOf("BUNNY", "CHICK"))
        override val dailyReminderEnabled: Flow<Boolean> = flowOf(false)
        override val reminderPromptShown: Flow<Boolean> = flowOf(false)

        override fun getLevelStars(levelId: String): Flow<Int> = flowOf(3)
        override fun isMechanicSeen(mechanic: String): Flow<Boolean> = flowOf(true)
        override fun getDailyRecord(date: String) = flowOf(null)

        override suspend fun setSoundEnabled(enabled: Boolean) {}
        override suspend fun setMusicEnabled(enabled: Boolean) {}
        override suspend fun setHapticsEnabled(enabled: Boolean) {}
        override suspend fun setReduceMotion(enabled: Boolean) {}
        override suspend fun setTutorialCompleted(completed: Boolean) {}
        override suspend fun setCurrentLevelIndex(index: Int) {}
        override suspend fun addBerries(amount: Int) {
            grantedBerries += amount
            profileFlow.value = profileFlow.value.copy(berries = profileFlow.value.berries + amount)
        }
        override suspend fun spendBerries(amount: Int): Boolean = true
        override suspend fun useHint(): Boolean = true
        override suspend fun buyHintWithBerries(price: Int): Boolean = true
        override suspend fun useUndo(): Boolean = true
        override suspend fun buyUndoWithBerries(price: Int): Boolean = true
        override suspend fun recordLevelWin(levelId: String, stars: Int, berriesEarned: Int, rescuedCount: Int, newSpecies: List<String>) {}
        override suspend fun completeLevel(levelId: String, stars: Int, mistakes: Int, rescuedSpeciesList: List<String>, isGoldenBuddyRescued: Boolean) =
            com.kasif.arrowbuddies.domain.usecase.LevelCompletionResult(berriesEarned = 25, isFirstClear = true, stars = stars, tierUpEvents = emptyList(), unlockedAchievements = emptyList())
        override suspend fun claimTreatTime(todayDate: String): Boolean = true
        override suspend fun claimDailyStreak(today: LocalDate) =
            com.kasif.arrowbuddies.domain.usecase.StreakClaimResult(true, 1, 20, false)
        override suspend fun equipAccessory(species: String, accessory: BuddyAccessory): Boolean {
            equippedAccessoryMap[species] = accessory
            speciesFlow.value = speciesFlow.value.map {
                if (it.species == species) it.copy(equippedAccessory = accessory) else it
            }
            return true
        }
        override suspend fun buyDecor(decorId: String): Boolean = true
        override suspend fun recordDailyCompletion(date: String, stars: Int, mistakes: Int) {}
        override suspend fun resetTutorial() {}
        override suspend fun setMechanicSeen(mechanic: String) {}
        override suspend fun setDailyReminderEnabled(enabled: Boolean) {}
        override suspend fun setReminderPromptShown(shown: Boolean) {}
    }

    private class FakeSoundManager : SoundManager {
        override fun playPop() {}
        override fun playBoing() {}
        override fun playChime() {}
        override fun playMusic(worldId: Int) {}
        override fun stopMusic() {}
        override fun setSoundEnabled(enabled: Boolean) {}
        override fun setMusicEnabled(enabled: Boolean) {}
    }

    private class FakeHapticsManager : HapticsManager {
        override fun setHapticsEnabled(enabled: Boolean) {}
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `album loads all 24 species and correctly marks unlocked ones`() = runTest(testDispatcher) {
        val repo = FakeGameRepository()
        val viewModel = AlbumViewModel(repo, FakeSoundManager(), FakeHapticsManager())
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(state.speciesList.size).isEqualTo(24)
        assertThat(state.discoveredSpeciesCount).isEqualTo(2) // BUNNY and CHICK

        val bunny = state.speciesList.first { it.species.name == "BUNNY" }
        assertThat(bunny.unlocked).isTrue()
        assertThat(bunny.tier).isEqualTo(FriendshipTier.SILVER)
        assertThat(bunny.rescuedCount).isEqualTo(55)
        assertThat(bunny.equippedAccessory).isEqualTo(BuddyAccessory.BOW)

        val lockedSpecies = state.speciesList.first { it.species.name == "TIGER_CUB" }
        assertThat(lockedSpecies.unlocked).isFalse()
        assertThat(lockedSpecies.rescuedCount).isEqualTo(0)
    }

    @Test
    fun `album loads all 12 achievements with their status and progress`() = runTest(testDispatcher) {
        val repo = FakeGameRepository()
        val viewModel = AlbumViewModel(repo, FakeSoundManager(), FakeHapticsManager())

        testScheduler.advanceUntilIdle()
        val state = viewModel.uiState.value

        assertThat(state.achievementsList.size).isEqualTo(12)
        val firstRescue = state.achievementsList.first { it.type == AchievementType.FIRST_RESCUE }
        assertThat(firstRescue.unlocked).isTrue()

        val rescue100 = state.achievementsList.first { it.type == AchievementType.RESCUED_100 }
        assertThat(rescue100.unlocked).isFalse()
        assertThat(rescue100.progress).isEqualTo(60)
    }

    @Test
    fun `equipping accessory updates species and calls repository`() = runTest(testDispatcher) {
        val repo = FakeGameRepository()
        val viewModel = AlbumViewModel(repo, FakeSoundManager(), FakeHapticsManager())

        testScheduler.advanceUntilIdle()
        viewModel.onEvent(AlbumUiEvent.EquipAccessory("BUNNY", BuddyAccessory.SCARF))
        testScheduler.advanceUntilIdle()

        assertThat(repo.equippedAccessoryMap["BUNNY"]).isEqualTo(BuddyAccessory.SCARF)
    }

    @Test
    fun `granting debug berries increases berry balance`() = runTest(testDispatcher) {
        val repo = FakeGameRepository()
        val viewModel = AlbumViewModel(repo, FakeSoundManager(), FakeHapticsManager())

        testScheduler.advanceUntilIdle()
        viewModel.onEvent(AlbumUiEvent.GrantDebugBerries(100))
        testScheduler.advanceUntilIdle()

        assertThat(repo.grantedBerries).isEqualTo(100)
    }
}
