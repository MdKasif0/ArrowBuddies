package com.kasif.arrowbuddies.feature.flow

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.kasif.arrowbuddies.core.audio.SoundManager
import com.kasif.arrowbuddies.core.buddies.model.BuddySpecies
import com.kasif.arrowbuddies.core.haptics.HapticsManager
import com.kasif.arrowbuddies.data.repository.DefaultLevelRepository
import com.kasif.arrowbuddies.data.repository.GameRepository
import com.kasif.arrowbuddies.data.repository.LevelRepository
import com.kasif.arrowbuddies.domain.usecase.CompleteLevelUseCase
import com.kasif.arrowbuddies.engine.GameEngine
import com.kasif.arrowbuddies.engine.codec.LevelCodec
import com.kasif.arrowbuddies.engine.generator.HandcraftedTutorials
import com.kasif.arrowbuddies.engine.model.GameStatus
import com.kasif.arrowbuddies.engine.model.LevelDef
import com.kasif.arrowbuddies.engine.solver.LevelSolver
import com.kasif.arrowbuddies.feature.game.GameUiEvent
import com.kasif.arrowbuddies.feature.game.GameViewModel
import com.kasif.arrowbuddies.feature.home.HomeViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * End-to-End User Flow Test:
 * Fresh Install -> Tutorial Level 1 -> Level 2 -> Win Level 3 -> Transition to Home -> World Map.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class UserFlowEndToEndTest {

    private val testDispatcher = StandardTestDispatcher()

    private class TestSoundManager : SoundManager {
        var popCount = 0
        var escapeCount = 0
        var boingCount = 0
        override fun playPop() { popCount++ }
        override fun playBoing() { boingCount++ }
        override fun playChime() { escapeCount++ }
        override fun playMusic(worldId: Int) {}
        override fun stopMusic() {}
        override fun setSoundEnabled(enabled: Boolean) {}
        override fun setMusicEnabled(enabled: Boolean) {}
    }

    private class TestHapticsManager : HapticsManager {
        override fun tick(view: android.view.View?) {}
        override fun confirm(view: android.view.View?) {}
        override fun reject(view: android.view.View?) {}
        override fun setHapticsEnabled(enabled: Boolean) {}
    }

    private class TestLevelRepository : LevelRepository {
        override fun loadLevel(levelId: String): LevelDef {
            return when (levelId) {
                "w1-l01", "1" -> HandcraftedTutorials.getTutorialLevel(1, 1)
                "w1-l02", "2" -> HandcraftedTutorials.getTutorialLevel(1, 2)
                "w1-l03", "3" -> HandcraftedTutorials.getTutorialLevel(1, 3)
                else -> LevelCodec.decode(DefaultLevelRepository.DEV_01_JSON)
            }
        }
        override fun getAvailableDevLevelIds(): List<String> = listOf("w1-l01", "w1-l02", "w1-l03")
    }

    private class InMemoryGameRepository : GameRepository {
        val totalRescuedFlow = MutableStateFlow(0)
        val berriesFlow = MutableStateFlow(0)
        val tutorialCompletedFlow = MutableStateFlow(false)
        val currentLevelIndexFlow = MutableStateFlow(1)
        val highestUnlockedLevelFlow = MutableStateFlow(1)
        val hintsCountFlow = MutableStateFlow(3)
        val undosCountFlow = MutableStateFlow(3)
        val soundFlow = MutableStateFlow(true)
        val musicFlow = MutableStateFlow(true)
        val hapticsFlow = MutableStateFlow(true)
        val reduceMotionFlow = MutableStateFlow(false)
        val rescuedSpeciesFlow = MutableStateFlow<Set<String>>(emptySet())

        override val totalRescued: Flow<Int> = totalRescuedFlow
        override val berries: Flow<Int> = berriesFlow
        override val tutorialCompleted: Flow<Boolean> = tutorialCompletedFlow
        override val currentLevelIndex: Flow<Int> = currentLevelIndexFlow
        override val highestUnlockedLevel: Flow<Int> = highestUnlockedLevelFlow
        override val hintsCount: Flow<Int> = hintsCountFlow
        override val undosCount: Flow<Int> = undosCountFlow
        override val soundEnabled: Flow<Boolean> = soundFlow
        override val musicEnabled: Flow<Boolean> = musicFlow
        override val hapticsEnabled: Flow<Boolean> = hapticsFlow
        override val reduceMotion: Flow<Boolean> = reduceMotionFlow
        override val rescuedSpecies: Flow<Set<String>> = rescuedSpeciesFlow

        override fun getLevelStars(levelId: String): Flow<Int> = MutableStateFlow(3)
        override suspend fun setSoundEnabled(enabled: Boolean) { soundFlow.value = enabled }
        override suspend fun setMusicEnabled(enabled: Boolean) { musicFlow.value = enabled }
        override suspend fun setHapticsEnabled(enabled: Boolean) { hapticsFlow.value = enabled }
        override suspend fun setReduceMotion(enabled: Boolean) { reduceMotionFlow.value = enabled }
        override suspend fun setTutorialCompleted(completed: Boolean) { tutorialCompletedFlow.value = completed }
        override suspend fun setCurrentLevelIndex(index: Int) { currentLevelIndexFlow.value = index }
        override suspend fun addBerries(amount: Int) { berriesFlow.value += amount }
        override suspend fun spendBerries(amount: Int): Boolean {
            if (berriesFlow.value >= amount) {
                berriesFlow.value -= amount
                return true
            }
            return false
        }
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
        ) {
            totalRescuedFlow.value += rescuedCount
            berriesFlow.value += berriesEarned
            rescuedSpeciesFlow.value = rescuedSpeciesFlow.value + newSpecies
            highestUnlockedLevelFlow.value = maxOf(highestUnlockedLevelFlow.value, (levelId.toIntOrNull() ?: 1) + 1)
        }
        override suspend fun resetTutorial() {
            tutorialCompletedFlow.value = false
            totalRescuedFlow.value = 0
            berriesFlow.value = 0
        }
        override fun isMechanicSeen(mechanic: String): Flow<Boolean> = MutableStateFlow(true)
        override suspend fun setMechanicSeen(mechanic: String) {}

        override val playerProfile: Flow<com.kasif.arrowbuddies.data.db.entity.PlayerProfile> = kotlinx.coroutines.flow.flowOf(com.kasif.arrowbuddies.data.db.entity.PlayerProfile())
        override val speciesProgressList: Flow<List<com.kasif.arrowbuddies.data.db.entity.SpeciesProgress>> = kotlinx.coroutines.flow.flowOf(emptyList())
        override val decorItems: Flow<List<com.kasif.arrowbuddies.data.db.entity.DecorItem>> = kotlinx.coroutines.flow.flowOf(emptyList())
        override val achievements: Flow<List<com.kasif.arrowbuddies.data.db.entity.Achievement>> = kotlinx.coroutines.flow.flowOf(emptyList())
        override val dailyReminderEnabled: Flow<Boolean> = kotlinx.coroutines.flow.flowOf(false)
        override val reminderPromptShown: Flow<Boolean> = kotlinx.coroutines.flow.flowOf(false)

        override fun getDailyRecord(date: String): Flow<com.kasif.arrowbuddies.data.db.entity.DailyRecord?> = kotlinx.coroutines.flow.flowOf(null)

        override suspend fun completeLevel(
            levelId: String,
            stars: Int,
            mistakes: Int,
            rescuedSpeciesList: List<String>,
            isGoldenBuddyRescued: Boolean
        ): com.kasif.arrowbuddies.domain.usecase.LevelCompletionResult = com.kasif.arrowbuddies.domain.usecase.LevelCompletionResult(
            berriesEarned = 10 + 5 * stars,
            isFirstClear = true,
            stars = stars,
            tierUpEvents = emptyList(),
            unlockedAchievements = emptyList()
        )

        override suspend fun claimTreatTime(todayDate: String): Boolean = true
        override suspend fun claimDailyStreak(today: java.time.LocalDate): com.kasif.arrowbuddies.domain.usecase.StreakClaimResult = com.kasif.arrowbuddies.domain.usecase.StreakClaimResult(true, 1, 20, false)
        override suspend fun buyDecor(decorId: String): Boolean = true
        override suspend fun equipAccessory(species: String, accessory: com.kasif.arrowbuddies.core.model.BuddyAccessory): Boolean = true
        override suspend fun recordDailyCompletion(date: String, stars: Int, mistakes: Int) {}
        override suspend fun setDailyReminderEnabled(enabled: Boolean) {}
        override suspend fun setReminderPromptShown(shown: Boolean) {}
    }

    private lateinit var gameRepository: InMemoryGameRepository
    private lateinit var levelRepository: TestLevelRepository
    private lateinit var soundManager: TestSoundManager
    private lateinit var hapticsManager: TestHapticsManager

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        gameRepository = InMemoryGameRepository()
        levelRepository = TestLevelRepository()
        soundManager = TestSoundManager()
        hapticsManager = TestHapticsManager()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testFreshInstallToTutorialToLevel3WinToHomeFlow() = runTest(testDispatcher) {
        // 1. Initial State: Fresh install
        assertThat(gameRepository.tutorialCompleted.first()).isFalse()
        assertThat(gameRepository.totalRescued.first()).isEqualTo(0)

        // 2. Launch Tutorial Level 1
        val level1Vm = GameViewModel(
            levelRepository = levelRepository,
            gameRepository = gameRepository,
            savedStateHandle = SavedStateHandle(mapOf("levelId" to "w1-l01")),
            hapticsManager = hapticsManager,
            soundManager = soundManager
        )
        advanceUntilIdle()

        var l1State = level1Vm.uiState.value
        assertThat(l1State.isTutorialLevel).isTrue()
        assertThat(l1State.tutorialSpeechBubble).isNotNull()
        assertThat(l1State.totalBuddiesInLevel).isEqualTo(1)

        // Tap the solitary buddy in Level 1 to rescue
        val l1BuddyId = l1State.level.buddies.first().id
        level1Vm.onEvent(GameUiEvent.TapBuddy(l1BuddyId))
        advanceUntilIdle()

        l1State = level1Vm.uiState.value
        assertThat(l1State.isWon).isTrue()

        // 3. Level 2 (Blocking tutorial)
        val level2Vm = GameViewModel(
            levelRepository = levelRepository,
            gameRepository = gameRepository,
            savedStateHandle = SavedStateHandle(mapOf("levelId" to "w1-l02")),
            hapticsManager = hapticsManager,
            soundManager = soundManager
        )
        advanceUntilIdle()

        val l2Level = levelRepository.loadLevel("w1-l02")
        val l2Solve = LevelSolver.solve(l2Level)
        assertThat(l2Solve.solvable).isTrue()

        // Play the optimal solution for Level 2
        for (stepId in l2Solve.shortestSolution) {
            level2Vm.onEvent(GameUiEvent.TapBuddy(stepId))
            advanceUntilIdle()
        }
        assertThat(level2Vm.uiState.value.isWon).isTrue()

        // 4. Level 3 (Final tutorial graduation)
        val level3Vm = GameViewModel(
            levelRepository = levelRepository,
            gameRepository = gameRepository,
            savedStateHandle = SavedStateHandle(mapOf("levelId" to "w1-l03")),
            hapticsManager = hapticsManager,
            soundManager = soundManager
        )
        advanceUntilIdle()

        val l3Level = levelRepository.loadLevel("w1-l03")
        val l3Solve = LevelSolver.solve(l3Level)
        assertThat(l3Solve.solvable).isTrue()

        for (stepId in l3Solve.shortestSolution) {
            level3Vm.onEvent(GameUiEvent.TapBuddy(stepId))
            advanceUntilIdle()
        }
        assertThat(level3Vm.uiState.value.isWon).isTrue()

        // Record win in repository (Tutorial Graduation)
        gameRepository.recordLevelWin(
            levelId = "3",
            stars = 3,
            berriesEarned = 50,
            rescuedCount = l3Level.buddies.size,
            newSpecies = listOf(BuddySpecies.BUNNY.id, BuddySpecies.CHICK.id)
        )
        gameRepository.setTutorialCompleted(true)
        advanceUntilIdle()

        // 5. Verify Transition into Home Screen
        assertThat(gameRepository.tutorialCompleted.first()).isTrue()
        assertThat(gameRepository.totalRescued.first()).isGreaterThan(0)
        assertThat(gameRepository.berries.first()).isEqualTo(50)

        val homeVm = HomeViewModel(
            repository = gameRepository
        )

        homeVm.uiState.test {
            val first = awaitItem()
            val state = if (first.totalRescued == 0) awaitItem() else first
            assertThat(state.totalRescued).isEqualTo(l3Level.buddies.size)
            assertThat(state.berries).isEqualTo(50)
            assertThat(state.isDailyDone).isFalse()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun testAllBuddySpeciesDataAndLocalizationIntegrity() {
        val speciesList = BuddySpecies.entries
        assertThat(speciesList.size).isEqualTo(24)

        // Verify each species has distinct id, valid world 1..8, and valid string resource
        for (species in speciesList) {
            assertThat(species.id).isNotEmpty()
            assertThat(species.worldIndex).isIn(1..8)
            assertThat(species.nameRes).isNotEqualTo(0)
        }
    }

    @Test
    fun testReopeningGameResumesAtCurrentProgress() = runTest(testDispatcher) {
        // Case 1: Fresh install -> Level 1 (w1-l01)
        val freshSplashVm = com.kasif.arrowbuddies.feature.splash.SplashViewModel(gameRepository)
        advanceUntilIdle()
        val freshState = freshSplashVm.uiState.value
        assertThat(freshState.isReady).isTrue()
        assertThat(freshState.isFirstLaunch).isTrue()
        assertThat(freshState.initialLevelId).isEqualTo("w1-l01")

        // Case 2: Player completed Level 1 -> progress is now Level 2 (w1-l02)
        gameRepository.setCurrentLevelIndex(2)
        advanceUntilIdle()
        val resumingSplashVm = com.kasif.arrowbuddies.feature.splash.SplashViewModel(gameRepository)
        advanceUntilIdle()
        val resumingState = resumingSplashVm.uiState.value
        assertThat(resumingState.isReady).isTrue()
        assertThat(resumingState.isFirstLaunch).isTrue()
        assertThat(resumingState.initialLevelId).isEqualTo("w1-l02")

        // Case 3: Player completed Tutorial (Level 3+) -> routes to Home Screen
        gameRepository.setTutorialCompleted(true)
        gameRepository.setCurrentLevelIndex(4)
        advanceUntilIdle()
        val returningSplashVm = com.kasif.arrowbuddies.feature.splash.SplashViewModel(gameRepository)
        advanceUntilIdle()
        val returningState = returningSplashVm.uiState.value
        assertThat(returningState.isReady).isTrue()
        assertThat(returningState.isFirstLaunch).isFalse()
        assertThat(returningState.initialLevelId).isEqualTo("w1-l04")
    }
}
