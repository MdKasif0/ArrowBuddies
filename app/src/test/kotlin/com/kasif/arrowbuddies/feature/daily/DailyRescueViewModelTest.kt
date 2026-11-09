package com.kasif.arrowbuddies.feature.daily

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.kasif.arrowbuddies.core.audio.SoundManager
import com.kasif.arrowbuddies.core.haptics.HapticsManager
import com.kasif.arrowbuddies.core.model.BuddyAccessory
import com.kasif.arrowbuddies.data.db.entity.DailyRecord
import com.kasif.arrowbuddies.data.db.entity.DecorItem
import com.kasif.arrowbuddies.data.db.entity.PlayerProfile
import com.kasif.arrowbuddies.data.db.entity.SpeciesProgress
import com.kasif.arrowbuddies.data.repository.GameRepository
import com.kasif.arrowbuddies.engine.runtime.DailyRescue
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
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalCoroutinesApi::class)
class DailyRescueViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private class FakeGameRepository : GameRepository {
        val today = LocalDate.now(DailyRescue.TIME_ZONE)
        val todayStr = today.format(DateTimeFormatter.BASIC_ISO_DATE)

        val profileFlow = MutableStateFlow(PlayerProfile(berries = 200, streakDays = 3))
        val dailyRecordFlow = MutableStateFlow<DailyRecord?>(
            DailyRecord(date = todayStr, completed = true, stars = 3, mistakes = 0, completedAt = 1000L)
        )

        override val playerProfile: Flow<PlayerProfile> = profileFlow
        override val speciesProgressList: Flow<List<SpeciesProgress>> = flowOf(emptyList())
        override val decorItems: Flow<List<DecorItem>> = flowOf(emptyList())
        override val achievements = flowOf(emptyList<com.kasif.arrowbuddies.data.db.entity.Achievement>())
        override val totalRescued: Flow<Int> = MutableStateFlow(30)
        override val berries: Flow<Int> = MutableStateFlow(200)
        override val soundEnabled: Flow<Boolean> = flowOf(true)
        override val musicEnabled: Flow<Boolean> = flowOf(true)
        override val hapticsEnabled: Flow<Boolean> = flowOf(true)
        override val reduceMotion: Flow<Boolean> = flowOf(false)
        override val tutorialCompleted: Flow<Boolean> = flowOf(true)
        override val currentLevelIndex: Flow<Int> = flowOf(10)
        override val highestUnlockedLevel: Flow<Int> = flowOf(10)
        override val hintsCount: Flow<Int> = flowOf(3)
        override val undosCount: Flow<Int> = flowOf(3)
        override val rescuedSpecies: Flow<Set<String>> = flowOf(emptySet())
        override val dailyReminderEnabled: Flow<Boolean> = flowOf(false)
        override val reminderPromptShown: Flow<Boolean> = flowOf(false)

        override fun getLevelStars(levelId: String): Flow<Int> = flowOf(3)
        override fun isMechanicSeen(mechanic: String): Flow<Boolean> = flowOf(true)
        override fun getDailyRecord(date: String): Flow<DailyRecord?> = dailyRecordFlow

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
        override suspend fun claimTreatTime(todayDate: String): Boolean = true
        override suspend fun claimDailyStreak(today: LocalDate) =
            com.kasif.arrowbuddies.domain.usecase.StreakClaimResult(true, 1, 20, false)
        override suspend fun equipAccessory(species: String, accessory: BuddyAccessory): Boolean = true
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
    fun `daily rescue loads streak and today completion status`() = runTest(testDispatcher) {
        val repo = FakeGameRepository()
        val viewModel = DailyRescueViewModel(repo, FakeSoundManager(), FakeHapticsManager())

        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.streakDays).isEqualTo(3)
            assertThat(state.isCompletedToday).isTrue()
            assertThat(state.todayStars).isEqualTo(3)
            assertThat(state.todayMistakes).isEqualTo(0)
            assertThat(state.streakRewards.size).isEqualTo(7)
            assertThat(state.streakRewards.last().hasDecorGift).isTrue()
            assertThat(state.shareResultText).contains("Stars: ⭐⭐⭐")
            assertThat(state.shareResultText).contains("Bonks: 0 🎯")
        }
    }
}
