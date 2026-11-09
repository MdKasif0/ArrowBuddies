package com.kasif.arrowbuddies.feature.settings

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.kasif.arrowbuddies.core.audio.SoundManager
import com.kasif.arrowbuddies.core.haptics.HapticsManager
import com.kasif.arrowbuddies.data.repository.GameRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private class FakeGameRepository : GameRepository {
        val soundFlow = MutableStateFlow(true)
        val musicFlow = MutableStateFlow(true)
        val hapticsFlow = MutableStateFlow(true)
        val reduceMotionFlow = MutableStateFlow(false)
        val rescuedFlow = MutableStateFlow(0)
        val berriesFlow = MutableStateFlow(0)

        override val totalRescued: Flow<Int> = rescuedFlow
        override val berries: Flow<Int> = berriesFlow
        override val soundEnabled: Flow<Boolean> = soundFlow
        override val musicEnabled: Flow<Boolean> = musicFlow
        override val hapticsEnabled: Flow<Boolean> = hapticsFlow
        override val reduceMotion: Flow<Boolean> = reduceMotionFlow
        override val tutorialCompleted: Flow<Boolean> = MutableStateFlow(true)
        override val currentLevelIndex: Flow<Int> = MutableStateFlow(1)
        override val highestUnlockedLevel: Flow<Int> = MutableStateFlow(1)
        override val hintsCount: Flow<Int> = MutableStateFlow(3)
        override val undosCount: Flow<Int> = MutableStateFlow(3)
        override val rescuedSpecies: Flow<Set<String>> = MutableStateFlow(emptySet())

        override fun getLevelStars(levelId: String): Flow<Int> = kotlinx.coroutines.flow.flowOf(0)
        override fun isMechanicSeen(mechanic: String): Flow<Boolean> = kotlinx.coroutines.flow.flowOf(true)

        override suspend fun setSoundEnabled(enabled: Boolean) {
            soundFlow.value = enabled
        }

        override suspend fun setMusicEnabled(enabled: Boolean) {
            musicFlow.value = enabled
        }

        override suspend fun setHapticsEnabled(enabled: Boolean) {
            hapticsFlow.value = enabled
        }

        override suspend fun setReduceMotion(enabled: Boolean) {
            reduceMotionFlow.value = enabled
        }

        override suspend fun setTutorialCompleted(completed: Boolean) {}
        override suspend fun setCurrentLevelIndex(index: Int) {}
        override suspend fun addBerries(amount: Int) {}
        override suspend fun spendBerries(amount: Int): Boolean = true
        override suspend fun useHint(): Boolean = true
        override suspend fun buyHintWithBerries(price: Int): Boolean = true
        override suspend fun useUndo(): Boolean = true
        override suspend fun buyUndoWithBerries(price: Int): Boolean = true
        override suspend fun recordLevelWin(
            levelId: String,
            stars: Int,
            berriesEarned: Int,
            rescuedCount: Int,
            newSpecies: List<String>
        ) {}
        override suspend fun resetTutorial() {}
        override suspend fun setMechanicSeen(mechanic: String) {}
    }

    private class FakeSoundManager : SoundManager {
        var currentSound = true
        var currentMusic = true
        override fun playPop() {}
        override fun playBoing() {}
        override fun playChime() {}
        override fun playMusic(worldId: Int) {}
        override fun stopMusic() {}
        override fun setSoundEnabled(enabled: Boolean) { currentSound = enabled }
        override fun setMusicEnabled(enabled: Boolean) { currentMusic = enabled }
    }

    private class FakeHapticsManager : HapticsManager {
        var currentHaptics = true
        override fun tick() {}
        override fun bonk() {}
        override fun rescueSuccess() {}
        override fun setHapticsEnabled(enabled: Boolean) { currentHaptics = enabled }
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
    fun togglingSoundUpdatesState() = runTest(testDispatcher) {
        val repository = FakeGameRepository()
        val soundManager = FakeSoundManager()
        val hapticsManager = FakeHapticsManager()
        val viewModel = SettingsViewModel(repository, soundManager, hapticsManager)

        viewModel.uiState.test {
            val initial = awaitItem()
            assertThat(initial.soundEnabled).isTrue()

            viewModel.onEvent(SettingsUiEvent.ToggleSound(false))
            testScheduler.advanceUntilIdle()

            val updated = awaitItem()
            assertThat(updated.soundEnabled).isFalse()
            assertThat(soundManager.currentSound).isFalse()
        }
    }

    @Test
    fun togglingHapticsUpdatesState() = runTest(testDispatcher) {
        val repository = FakeGameRepository()
        val soundManager = FakeSoundManager()
        val hapticsManager = FakeHapticsManager()
        val viewModel = SettingsViewModel(repository, soundManager, hapticsManager)

        viewModel.uiState.test {
            val initial = awaitItem()
            assertThat(initial.hapticsEnabled).isTrue()

            viewModel.onEvent(SettingsUiEvent.ToggleHaptics(false))
            testScheduler.advanceUntilIdle()

            val updated = awaitItem()
            assertThat(updated.hapticsEnabled).isFalse()
            assertThat(hapticsManager.currentHaptics).isFalse()
        }
    }

    @Test
    fun togglingReduceMotionUpdatesState() = runTest(testDispatcher) {
        val repository = FakeGameRepository()
        val soundManager = FakeSoundManager()
        val hapticsManager = FakeHapticsManager()
        val viewModel = SettingsViewModel(repository, soundManager, hapticsManager)

        viewModel.uiState.test {
            val initial = awaitItem()
            assertThat(initial.reduceMotion).isFalse()

            viewModel.onEvent(SettingsUiEvent.ToggleReduceMotion(true))
            testScheduler.advanceUntilIdle()

            val updated = awaitItem()
            assertThat(updated.reduceMotion).isTrue()
        }
    }

    @Test
    fun changingLanguageUpdatesState() = runTest(testDispatcher) {
        val repository = FakeGameRepository()
        val soundManager = FakeSoundManager()
        val hapticsManager = FakeHapticsManager()
        val viewModel = SettingsViewModel(repository, soundManager, hapticsManager)

        viewModel.uiState.test {
            val initial = awaitItem()

            viewModel.onEvent(SettingsUiEvent.SetLanguage("hi"))
            testScheduler.advanceUntilIdle()

            val updated = awaitItem()
            assertThat(updated.currentLanguage).isEqualTo("hi")
        }
    }

    @Test
    fun dialogTogglesWork() = runTest(testDispatcher) {
        val repository = FakeGameRepository()
        val soundManager = FakeSoundManager()
        val hapticsManager = FakeHapticsManager()
        val viewModel = SettingsViewModel(repository, soundManager, hapticsManager)

        viewModel.uiState.test {
            val initial = awaitItem()
            assertThat(initial.showPrivacyPolicyDialog).isFalse()
            assertThat(initial.showCreditsDialog).isFalse()

            viewModel.onEvent(SettingsUiEvent.OpenPrivacyPolicy)
            val policyOpened = awaitItem()
            assertThat(policyOpened.showPrivacyPolicyDialog).isTrue()

            viewModel.onEvent(SettingsUiEvent.DismissPrivacyPolicy)
            val policyDismissed = awaitItem()
            assertThat(policyDismissed.showPrivacyPolicyDialog).isFalse()

            viewModel.onEvent(SettingsUiEvent.OpenCredits)
            val creditsOpened = awaitItem()
            assertThat(creditsOpened.showCreditsDialog).isTrue()

            viewModel.onEvent(SettingsUiEvent.DismissCredits)
            val creditsDismissed = awaitItem()
            assertThat(creditsDismissed.showCreditsDialog).isFalse()
        }
    }
}
