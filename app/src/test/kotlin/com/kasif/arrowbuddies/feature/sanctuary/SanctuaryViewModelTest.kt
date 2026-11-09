package com.kasif.arrowbuddies.feature.sanctuary

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.kasif.arrowbuddies.core.audio.SoundManager
import com.kasif.arrowbuddies.core.haptics.HapticsManager
import com.kasif.arrowbuddies.core.model.BuddyAccessory
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
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class SanctuaryViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private class FakeGameRepository : GameRepository {
        val profileFlow = MutableStateFlow(PlayerProfile(berries = 500, totalRescued = 15, highestUnlockedLevel = 35))
        val decorFlow = MutableStateFlow(
            listOf(
                DecorItem("decor_pond_1", owned = false, slot = "z1_s1", zoneId = 1, costBerries = 150),
                DecorItem("decor_swing_2", owned = false, slot = "z2_s1", zoneId = 2, costBerries = 200)
            )
        )
        val speciesFlow = MutableStateFlow(
            listOf(
                SpeciesProgress("BUNNY", rescuedCount = 12, tier = com.kasif.arrowbuddies.core.model.FriendshipTier.BRONZE, equippedAccessory = BuddyAccessory.BOW, firstRescuedAt = 1000L),
                SpeciesProgress("PUPPY", rescuedCount = 8, tier = com.kasif.arrowbuddies.core.model.FriendshipTier.LOCKED, equippedAccessory = BuddyAccessory.NONE, firstRescuedAt = 2000L)
            )
        )

        var claimedTreatDate: String? = null
        var boughtDecorId: String? = null

        override val playerProfile: Flow<PlayerProfile> = profileFlow
        override val speciesProgressList: Flow<List<SpeciesProgress>> = speciesFlow
        override val decorItems: Flow<List<DecorItem>> = decorFlow
        override val achievements = flowOf(emptyList<com.kasif.arrowbuddies.data.db.entity.Achievement>())
        override val totalRescued: Flow<Int> = MutableStateFlow(15)
        override val berries: Flow<Int> = MutableStateFlow(500)
        override val soundEnabled: Flow<Boolean> = flowOf(true)
        override val musicEnabled: Flow<Boolean> = flowOf(true)
        override val hapticsEnabled: Flow<Boolean> = flowOf(true)
        override val reduceMotion: Flow<Boolean> = flowOf(false)
        override val tutorialCompleted: Flow<Boolean> = flowOf(true)
        override val currentLevelIndex: Flow<Int> = flowOf(35)
        override val highestUnlockedLevel: Flow<Int> = flowOf(35)
        override val hintsCount: Flow<Int> = flowOf(3)
        override val undosCount: Flow<Int> = flowOf(3)
        override val rescuedSpecies: Flow<Set<String>> = flowOf(setOf("BUNNY", "PUPPY"))
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
        override suspend fun addBerries(amount: Int) {}
        override suspend fun spendBerries(amount: Int): Boolean = true
        override suspend fun useHint(): Boolean = true
        override suspend fun buyHintWithBerries(price: Int): Boolean = true
        override suspend fun useUndo(): Boolean = true
        override suspend fun buyUndoWithBerries(price: Int): Boolean = true
        override suspend fun recordLevelWin(levelId: String, stars: Int, berriesEarned: Int, rescuedCount: Int, newSpecies: List<String>) {}
        override suspend fun completeLevel(levelId: String, stars: Int, mistakes: Int, rescuedSpeciesList: List<String>, isGoldenBuddyRescued: Boolean) =
            com.kasif.arrowbuddies.domain.usecase.LevelCompletionResult(berriesEarned = 25, isFirstClear = true, stars = stars, tierUpEvents = emptyList(), unlockedAchievements = emptyList())
        override suspend fun claimTreatTime(todayDate: String): Boolean {
            claimedTreatDate = todayDate
            return true
        }
        override suspend fun claimDailyStreak(today: LocalDate) =
            com.kasif.arrowbuddies.domain.usecase.StreakClaimResult(true, 1, 20, false)
        override suspend fun equipAccessory(species: String, accessory: BuddyAccessory): Boolean = true
        override suspend fun buyDecor(decorId: String): Boolean {
            boughtDecorId = decorId
            decorFlow.value = decorFlow.value.map { if (it.id == decorId) it.copy(owned = true) else it }
            return true
        }
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
    fun `sanctuary initializes with unlocked zones and roaming buddies`() = runTest(testDispatcher) {
        val repo = FakeGameRepository()
        val viewModel = SanctuaryViewModel(repo, FakeSoundManager(), FakeHapticsManager())
        testScheduler.runCurrent()

        val state = viewModel.uiState.value
        // Level 35 = World 2 unlocked => 2 zones unlocked
        assertThat(state.unlockedZonesCount).isEqualTo(2)
        assertThat(state.berries).isEqualTo(500)
        assertThat(state.buddies.isNotEmpty()).isTrue()
        // Verify equipped accessory carries over to buddy model
        val bunnyBuddy = state.buddies.firstOrNull { it.species.name == "BUNNY" }
        assertThat(bunnyBuddy).isNotNull()
        assertThat(bunnyBuddy?.accessory).isEqualTo(BuddyAccessory.BOW)
        viewModel.stopBehaviorLoop()
    }

    @Test
    fun `tapping a buddy triggers happy hop state, bubble speech and heart pop`() = runTest(testDispatcher) {
        val repo = FakeGameRepository()
        val viewModel = SanctuaryViewModel(repo, FakeSoundManager(), FakeHapticsManager())

        testScheduler.runCurrent()
        val firstBuddyId = viewModel.uiState.value.buddies.first().id

        viewModel.onEvent(SanctuaryUiEvent.TapBuddy(firstBuddyId))

        val state = viewModel.uiState.value
        val tappedBuddy = state.buddies.first { it.id == firstBuddyId }
        assertThat(tappedBuddy.state).isEqualTo(SanctuaryBuddyState.HOP)
        assertThat(tappedBuddy.heartPop).isTrue()
        assertThat(tappedBuddy.bubbleText).isNotNull()
        viewModel.stopBehaviorLoop()
    }

    @Test
    fun `treat basket claim triggers rush and calls repository`() = runTest(testDispatcher) {
        val repo = FakeGameRepository()
        val viewModel = SanctuaryViewModel(repo, FakeSoundManager(), FakeHapticsManager())

        testScheduler.runCurrent()
        assertThat(viewModel.uiState.value.isTreatTimeAvailable).isTrue()

        viewModel.onEvent(SanctuaryUiEvent.TapTreatBasket)
        testScheduler.runCurrent()

        assertThat(repo.claimedTreatDate).isNotNull()
        assertThat(viewModel.uiState.value.isTreatScatterActive).isTrue()
        assertThat(viewModel.uiState.value.isTreatTimeAvailable).isFalse()
        viewModel.stopBehaviorLoop()
    }

    @Test
    fun `buying decoration updates ownership and clears buy dialog`() = runTest(testDispatcher) {
        val repo = FakeGameRepository()
        val viewModel = SanctuaryViewModel(repo, FakeSoundManager(), FakeHapticsManager())

        testScheduler.runCurrent()
        viewModel.onEvent(SanctuaryUiEvent.ClickDecorSlot("decor_pond_1"))
        assertThat(viewModel.uiState.value.selectedDecorToBuy?.id).isEqualTo("decor_pond_1")

        viewModel.onEvent(SanctuaryUiEvent.ConfirmBuyDecor("decor_pond_1"))
        testScheduler.runCurrent()

        assertThat(repo.boughtDecorId).isEqualTo("decor_pond_1")
        assertThat(viewModel.uiState.value.selectedDecorToBuy).isNull()
        viewModel.stopBehaviorLoop()
    }
}
