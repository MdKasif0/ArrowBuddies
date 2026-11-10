package com.kasif.arrowbuddies.feature.game

import android.view.View
import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.kasif.arrowbuddies.core.ads.FakeAdsManager
import com.kasif.arrowbuddies.core.audio.SoundManager
import com.kasif.arrowbuddies.core.haptics.HapticsManager
import com.kasif.arrowbuddies.data.repository.DefaultLevelRepository
import com.kasif.arrowbuddies.data.repository.GameRepository
import com.kasif.arrowbuddies.data.repository.LevelRepository
import com.kasif.arrowbuddies.engine.codec.LevelCodec
import com.kasif.arrowbuddies.engine.model.GameStatus
import com.kasif.arrowbuddies.engine.model.LevelDef
import com.kasif.arrowbuddies.engine.model.Pos
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GameViewModelTest {

    private class FakeLevelRepository : LevelRepository {
        override fun loadLevel(levelId: String): LevelDef {
            if (levelId == "w1-l01") return com.kasif.arrowbuddies.engine.generator.HandcraftedTutorials.getTutorialLevel(1, 1)
            if (levelId == "w1-l02") return com.kasif.arrowbuddies.engine.generator.HandcraftedTutorials.getTutorialLevel(1, 2)
            if (levelId == "w1-l03") return com.kasif.arrowbuddies.engine.generator.HandcraftedTutorials.getTutorialLevel(1, 3)
            val json = when (levelId) {
                "2", "w1-l02-dev" -> DefaultLevelRepository.DEV_02_JSON
                "3", "w1-l03-dev" -> DefaultLevelRepository.DEV_03_JSON
                "4", "w2-l04-dev" -> DefaultLevelRepository.DEV_04_JSON
                "5", "w3-l05-dev" -> DefaultLevelRepository.DEV_05_JSON
                "6", "w4-l06-dev" -> DefaultLevelRepository.DEV_06_JSON
                "7", "w5-l07-dev" -> DefaultLevelRepository.DEV_07_JSON
                "8", "w6-l08-dev" -> DefaultLevelRepository.DEV_08_JSON
                "9", "w7-l09-dev" -> DefaultLevelRepository.DEV_09_JSON
                else -> DefaultLevelRepository.DEV_01_JSON
            }
            return LevelCodec.decode(json)
        }

        override fun getAvailableDevLevelIds(): List<String> {
            return listOf(
                "w1-l01-dev",
                "w1-l02-dev",
                "w1-l03-dev",
                "w2-l04-dev",
                "w3-l05-dev",
                "w4-l06-dev",
                "w5-l07-dev",
                "w6-l08-dev",
                "w7-l09-dev"
            )
        }
    }

    private class FakeGameRepository : GameRepository {
        val seenMechanics = mutableSetOf<String>()

        override val totalRescued: Flow<Int> = MutableStateFlow(0)
        override val berries: Flow<Int> = MutableStateFlow(100)
        override val soundEnabled: Flow<Boolean> = MutableStateFlow(true)
        override val musicEnabled: Flow<Boolean> = MutableStateFlow(true)
        override val hapticsEnabled: Flow<Boolean> = MutableStateFlow(true)
        override val reduceMotion: Flow<Boolean> = MutableStateFlow(false)
        override val tutorialCompleted: Flow<Boolean> = MutableStateFlow(true)
        override val currentLevelIndex: Flow<Int> = MutableStateFlow(1)
        override val highestUnlockedLevel: Flow<Int> = MutableStateFlow(1)
        override val hintsCount: Flow<Int> = MutableStateFlow(3)
        override val undosCount: Flow<Int> = MutableStateFlow(3)
        override val rescuedSpecies: Flow<Set<String>> = MutableStateFlow(emptySet())

        override fun getLevelStars(levelId: String): Flow<Int> = flowOf(0)
        override fun isMechanicSeen(mechanic: String): Flow<Boolean> =
            flowOf(seenMechanics.contains(mechanic))

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
        override suspend fun recordLevelWin(
            levelId: String,
            stars: Int,
            berriesEarned: Int,
            rescuedCount: Int,
            newSpecies: List<String>
        ) {}
        override suspend fun resetTutorial() {}
        override suspend fun setMechanicSeen(mechanic: String) {
            seenMechanics.add(mechanic)
        }

        override val playerProfile: Flow<com.kasif.arrowbuddies.data.db.entity.PlayerProfile> = flowOf(com.kasif.arrowbuddies.data.db.entity.PlayerProfile())
        override val speciesProgressList: Flow<List<com.kasif.arrowbuddies.data.db.entity.SpeciesProgress>> = flowOf(emptyList())
        override val decorItems: Flow<List<com.kasif.arrowbuddies.data.db.entity.DecorItem>> = flowOf(emptyList())
        override val achievements: Flow<List<com.kasif.arrowbuddies.data.db.entity.Achievement>> = flowOf(emptyList())
        override val dailyReminderEnabled: Flow<Boolean> = flowOf(false)
        override val reminderPromptShown: Flow<Boolean> = flowOf(false)

        override fun getDailyRecord(date: String): Flow<com.kasif.arrowbuddies.data.db.entity.DailyRecord?> = flowOf(null)

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
        override suspend fun claimDailyStreak(today: java.time.LocalDate): com.kasif.arrowbuddies.domain.usecase.StreakClaimResult =
            com.kasif.arrowbuddies.domain.usecase.StreakClaimResult(success = true, streakDays = 1, berriesAwarded = 20, day7DecorUnlocked = false)
        override suspend fun equipAccessory(species: String, accessory: com.kasif.arrowbuddies.core.model.BuddyAccessory): Boolean = true
        override suspend fun buyDecor(decorId: String): Boolean = true
        override suspend fun recordDailyCompletion(date: String, stars: Int, mistakes: Int) {}
        override suspend fun setDailyReminderEnabled(enabled: Boolean) {}
        override suspend fun setReminderPromptShown(shown: Boolean) {}
        override val adsEnabled: Flow<Boolean> = flowOf(true)
        override fun getRemainingDailyAdHints(): Flow<Int> = flowOf(3)
        override suspend fun claimFreeAdHint(): Boolean = true
        override suspend fun setAdsEnabled(enabled: Boolean) {}
    }

    private class FakeHapticsManager : HapticsManager {
        var tickCount = 0
        var confirmCount = 0
        var rejectCount = 0

        override fun tick(view: View?) { tickCount++ }
        override fun bonk(view: View?) { reject(view) }
        override fun rescueSuccess(view: View?) { confirm(view) }
        override fun confirm(view: View?) { confirmCount++ }
        override fun reject(view: View?) { rejectCount++ }
        override fun setHapticsEnabled(enabled: Boolean) {}
    }

    private class FakeSoundManager : SoundManager {
        var popCount = 0
        var boingCount = 0
        var chimeCount = 0

        override fun playPop() { popCount++ }
        override fun playBoing() { boingCount++ }
        override fun playChime() { chimeCount++ }
        override fun playMusic(worldId: Int) {}
        override fun stopMusic() {}
        override fun setSoundEnabled(enabled: Boolean) {}
        override fun setMusicEnabled(enabled: Boolean) {}
    }

    private lateinit var levelRepository: FakeLevelRepository
    private lateinit var gameRepository: FakeGameRepository
    private lateinit var adsManager: FakeAdsManager
    private lateinit var hapticsManager: FakeHapticsManager
    private lateinit var soundManager: FakeSoundManager

    @Before
    fun setUp() {
        levelRepository = FakeLevelRepository()
        gameRepository = FakeGameRepository()
        adsManager = FakeAdsManager()
        hapticsManager = FakeHapticsManager()
        soundManager = FakeSoundManager()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state loads dev level 1 with 3 hearts and safe moves`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val savedStateHandle = SavedStateHandle(mapOf("levelId" to "1"))
        val viewModel = GameViewModel(levelRepository, gameRepository, adsManager, savedStateHandle, hapticsManager, soundManager)
        advanceUntilIdle() // let the level finish loading in the background

        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.levelId).isEqualTo("w1-l01-dev")
            assertThat(state.heartsRemaining).isEqualTo(3)
            assertThat(state.rescuedCount).isEqualTo(0)
            assertThat(state.totalBuddiesInLevel).isEqualTo(2)
            assertThat(state.isInputLocked).isFalse()
            assertThat(state.isWon).isFalse()
            assertThat(state.isLost).isFalse()
            assertThat(state.safeBuddyIds).containsExactly("bunny_1", "chick_1")
        }
    }

    @Test
    fun `tapping escaping buddy locks input, plays escape effect, updates state, and wins on last buddy`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val savedStateHandle = SavedStateHandle(mapOf("levelId" to "1"))
        val viewModel = GameViewModel(levelRepository, gameRepository, adsManager, savedStateHandle, hapticsManager, soundManager)
        advanceUntilIdle() // let the level finish loading in the background

        viewModel.onEvent(GameUiEvent.TapBuddy("bunny_1"))
        advanceTimeBy(50)
        assertThat(viewModel.uiState.value.isInputLocked).isTrue()

        advanceTimeBy(600)
        assertThat(viewModel.uiState.value.isInputLocked).isFalse()
        assertThat(viewModel.uiState.value.rescuedCount).isEqualTo(1)
        assertThat(viewModel.uiState.value.gameState.buddies.map { it.id }).containsExactly("chick_1")

        viewModel.onEvent(GameUiEvent.TapBuddy("chick_1"))
        advanceTimeBy(600)
        advanceTimeBy(600)

        val finalState = viewModel.uiState.value
        assertThat(finalState.isWon).isTrue()
        assertThat(finalState.rescuedCount).isEqualTo(2)
        assertThat(finalState.starsEarned).isEqualTo(3)
        assertThat(finalState.showLevelCompleteDialog).isTrue()
    }

    @Test
    fun `tapping blocked buddy in level 2 triggers bonk, decrements heart, and causes game over after 3 bonks`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val savedStateHandle = SavedStateHandle(mapOf("levelId" to "2"))
        val viewModel = GameViewModel(levelRepository, gameRepository, adsManager, savedStateHandle, hapticsManager, soundManager)
        advanceUntilIdle() // let the level finish loading in the background

        assertThat(viewModel.uiState.value.safeBuddyIds).containsExactly("bunny_1", "piglet_1")

        viewModel.onEvent(GameUiEvent.TapBuddy("chick_1"))
        advanceTimeBy(50)
        assertThat(viewModel.uiState.value.isInputLocked).isTrue()

        advanceTimeBy(600)
        assertThat(viewModel.uiState.value.isInputLocked).isFalse()
        assertThat(viewModel.uiState.value.heartsRemaining).isEqualTo(2)
        assertThat(viewModel.uiState.value.gameState.mistakes).isEqualTo(1)

        viewModel.onEvent(GameUiEvent.TapBuddy("chick_1"))
        advanceTimeBy(600)
        assertThat(viewModel.uiState.value.heartsRemaining).isEqualTo(1)

        viewModel.onEvent(GameUiEvent.TapBuddy("chick_1"))
        advanceTimeBy(1000)

        val lostState = viewModel.uiState.value
        assertThat(lostState.isLost).isTrue()
        assertThat(lostState.heartsRemaining).isEqualTo(0)
        assertThat(lostState.showLevelFailedDialog).isTrue()
    }

    @Test
    fun `undo restores board state from successful move history while keeping current hearts`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val savedStateHandle = SavedStateHandle(mapOf("levelId" to "1"))
        val viewModel = GameViewModel(levelRepository, gameRepository, adsManager, savedStateHandle, hapticsManager, soundManager)
        advanceUntilIdle() // let the level finish loading in the background

        viewModel.onEvent(GameUiEvent.TapBuddy("bunny_1"))
        advanceTimeBy(600)
        assertThat(viewModel.uiState.value.rescuedCount).isEqualTo(1)
        assertThat(viewModel.uiState.value.canUndo).isTrue()

        viewModel.onEvent(GameUiEvent.Undo)
        advanceUntilIdle()

        val restored = viewModel.uiState.value
        assertThat(restored.rescuedCount).isEqualTo(0)
        assertThat(restored.gameState.buddies.size).isEqualTo(2)
        assertThat(restored.canUndo).isFalse()
    }

    @Test
    fun `restart resets level to initial state with 3 hearts and full buddy list`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val savedStateHandle = SavedStateHandle(mapOf("levelId" to "2"))
        val viewModel = GameViewModel(levelRepository, gameRepository, adsManager, savedStateHandle, hapticsManager, soundManager)
        advanceUntilIdle() // let the level finish loading in the background

        viewModel.onEvent(GameUiEvent.TapBuddy("chick_1"))
        advanceTimeBy(600)
        assertThat(viewModel.uiState.value.heartsRemaining).isEqualTo(2)

        viewModel.onEvent(GameUiEvent.RestartLevel)
        advanceUntilIdle()

        val restarted = viewModel.uiState.value
        assertThat(restarted.heartsRemaining).isEqualTo(3)
        assertThat(restarted.gameState.mistakes).isEqualTo(0)
        assertThat(restarted.rescuedCount).isEqualTo(0)
        assertThat(restarted.gameState.buddies.size).isEqualTo(3)
    }

    @Test
    fun `path peek computes escape for clear buddy and blocker for blocked buddy`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val savedStateHandle = SavedStateHandle(mapOf("levelId" to "2"))
        val viewModel = GameViewModel(levelRepository, gameRepository, adsManager, savedStateHandle, hapticsManager, soundManager)
        advanceUntilIdle() // let the level finish loading in the background

        viewModel.onEvent(GameUiEvent.StartPathPeek("bunny_1"))
        val bunnyPeek = viewModel.uiState.value.activePeekingPath
        assertThat(bunnyPeek).isNotNull()
        assertThat(bunnyPeek!!.isEscape).isTrue()

        viewModel.onEvent(GameUiEvent.EndPathPeek)
        assertThat(viewModel.uiState.value.activePeekingPath).isNull()

        viewModel.onEvent(GameUiEvent.StartPathPeek("chick_1"))
        val chickPeek = viewModel.uiState.value.activePeekingPath
        assertThat(chickPeek).isNotNull()
        assertThat(chickPeek!!.isEscape).isFalse()
        assertThat(chickPeek.endPos.x).isEqualTo(1)
        assertThat(chickPeek.endPos.y).isEqualTo(2)
    }

    @Test
    fun `level loads in the background and shows a loading state until ready`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val savedStateHandle = SavedStateHandle(mapOf("levelId" to "1"))
        val viewModel = GameViewModel(levelRepository, gameRepository, adsManager, savedStateHandle, hapticsManager, soundManager)

        assertThat(viewModel.uiState.value.isLoading).isTrue()
        assertThat(viewModel.uiState.value.gameState.buddies).isEmpty()

        advanceUntilIdle()
        assertThat(viewModel.uiState.value.isLoading).isFalse()
        assertThat(viewModel.uiState.value.gameState.buddies).isNotEmpty()
    }

    @Test
    fun `survives process recreation by restoring serialized GameState from SavedStateHandle`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val savedStateHandle = SavedStateHandle(mapOf("levelId" to "1"))
        val vm1 = GameViewModel(levelRepository, gameRepository, adsManager, savedStateHandle, hapticsManager, soundManager)
        advanceUntilIdle() // let the level finish loading in the background

        vm1.onEvent(GameUiEvent.TapBuddy("bunny_1"))
        advanceTimeBy(600)
        assertThat(vm1.uiState.value.rescuedCount).isEqualTo(1)

        val savedJson = savedStateHandle.get<String>(GameViewModel.KEY_SAVED_GAME_STATE)
        assertThat(savedJson).isNotNull()

        val vm2 = GameViewModel(levelRepository, gameRepository, adsManager, savedStateHandle, hapticsManager, soundManager)
        assertThat(vm2.uiState.value.rescuedCount).isEqualTo(1)
        assertThat(vm2.uiState.value.gameState.buddies.map { it.id }).containsExactly("chick_1")
    }

    @Test
    fun `level 4 mud mechanic stops buddy without losing hearts`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val savedStateHandle = SavedStateHandle(mapOf("levelId" to "4"))
        val viewModel = GameViewModel(levelRepository, gameRepository, adsManager, savedStateHandle, hapticsManager, soundManager)
        advanceUntilIdle() // let the level finish loading in the background

        // In level 4, puppy is at (2,4) facing UP towards mud at (2,2)
        val initialPuppy = viewModel.uiState.value.gameState.findBuddy("puppy_1")
        assertThat(initialPuppy).isNotNull()
        assertThat(initialPuppy!!.pos).isEqualTo(Pos(2, 4))

        // Tap puppy -> stops on mud (2,2)
        viewModel.onEvent(GameUiEvent.TapBuddy("puppy_1"))
        advanceTimeBy(500)

        val stateAfterMud = viewModel.uiState.value
        assertThat(stateAfterMud.heartsRemaining).isEqualTo(3)
        assertThat(stateAfterMud.gameState.mistakes).isEqualTo(0)
        val movedPuppy = stateAfterMud.gameState.findBuddy("puppy_1")
        assertThat(movedPuppy!!.pos).isEqualTo(Pos(2, 2))

        // Next tap continues from mud in the same direction -> escapes!
        viewModel.onEvent(GameUiEvent.TapBuddy("puppy_1"))
        advanceTimeBy(600)

        assertThat(viewModel.uiState.value.rescuedCount).isEqualTo(1)
        assertThat(viewModel.uiState.value.heartsRemaining).isEqualTo(3)
    }

    @Test
    fun `level 6 cage and key unlocks matching cages when key carrier escapes`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val savedStateHandle = SavedStateHandle(mapOf("levelId" to "6"))
        val viewModel = GameViewModel(levelRepository, gameRepository, adsManager, savedStateHandle, hapticsManager, soundManager)
        advanceUntilIdle() // let the level finish loading in the background

        // Tapping caged fox refuses without heart loss
        viewModel.onEvent(GameUiEvent.TapBuddy("fox_1"))
        advanceTimeBy(200)

        assertThat(viewModel.uiState.value.heartsRemaining).isEqualTo(3)
        val cagedBuddy = viewModel.uiState.value.gameState.findBuddy("fox_1")
        assertThat(cagedBuddy!!.cageColor).isEqualTo("gold")

        // Free key carrier (penguin_1 carries gold key)
        viewModel.onEvent(GameUiEvent.TapBuddy("penguin_1"))
        advanceTimeBy(700)

        // Fox should now be unlocked!
        val unlockedBuddy = viewModel.uiState.value.gameState.findBuddy("fox_1")
        assertThat(unlockedBuddy!!.cageColor).isNull()
    }

    @Test
    fun `level 8 sleepy buddy wakes up when neighbor vacates cell`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val savedStateHandle = SavedStateHandle(mapOf("levelId" to "8"))
        val viewModel = GameViewModel(levelRepository, gameRepository, adsManager, savedStateHandle, hapticsManager, soundManager)
        advanceUntilIdle() // let the level finish loading in the background

        // Tapping sleepy owl refuses without heart loss
        viewModel.onEvent(GameUiEvent.TapBuddy("owl_1"))
        advanceTimeBy(200)

        assertThat(viewModel.uiState.value.heartsRemaining).isEqualTo(3)
        assertThat(viewModel.uiState.value.gameState.findBuddy("owl_1")!!.sleepy).isTrue()

        // Hedgehog (neighbor) escapes!
        viewModel.onEvent(GameUiEvent.TapBuddy("hedgehog_1"))
        advanceTimeBy(700)

        // Owl should now be awake!
        assertThat(viewModel.uiState.value.gameState.findBuddy("owl_1")!!.sleepy).isFalse()

        // Now owl can be tapped and escapes!
        viewModel.onEvent(GameUiEvent.TapBuddy("owl_1"))
        advanceTimeBy(700)
        assertThat(viewModel.uiState.value.rescuedCount).isEqualTo(2)
    }

    @Test
    fun `unseen mechanic triggers activeMechanicIntro dialog and dismissing marks it seen`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val savedStateHandle = SavedStateHandle(mapOf("levelId" to "4"))
        val viewModel = GameViewModel(levelRepository, gameRepository, adsManager, savedStateHandle, hapticsManager, soundManager)
        advanceUntilIdle()

        // Level 4 introduces MUD -> intro dialog shown
        assertThat(viewModel.uiState.value.activeMechanicIntro).isEqualTo(MechanicType.MUD)

        // Dismiss intro
        viewModel.onEvent(GameUiEvent.DismissMechanicIntro)
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.activeMechanicIntro).isNull()
        assertThat(gameRepository.seenMechanics).contains("mud")

        // Reloading level 4 does not show intro again
        viewModel.onEvent(GameUiEvent.LoadLevel("4"))
        advanceUntilIdle()
        assertThat(viewModel.uiState.value.activeMechanicIntro).isNull()
    }

    @Test
    fun `switching across dev levels 1 through 9 loads correct level definitions`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val savedStateHandle = SavedStateHandle(mapOf("levelId" to "1"))
        val viewModel = GameViewModel(levelRepository, gameRepository, adsManager, savedStateHandle, hapticsManager, soundManager)
        advanceUntilIdle() // let the level finish loading in the background

        val devLevelIds = levelRepository.getAvailableDevLevelIds()
        assertThat(devLevelIds.size).isEqualTo(9)

        for (id in devLevelIds) {
            viewModel.onEvent(GameUiEvent.LoadLevel(id))
            advanceUntilIdle()
            assertThat(viewModel.uiState.value.level.id).isEqualTo(id)
        }
    }

    @Test
    fun `level 1 starts with tutorial hand pointer and friendly speech bubble`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val savedStateHandle = SavedStateHandle(mapOf("levelId" to "w1-l01"))
        val viewModel = GameViewModel(levelRepository, gameRepository, adsManager, savedStateHandle, hapticsManager, soundManager)
        advanceUntilIdle() // let the level finish loading in the background

        val state = viewModel.uiState.value
        assertThat(state.isTutorialLevel).isTrue()
        assertThat(state.tutorialHandPos).isEqualTo(Pos(1, 1))
        assertThat(state.tutorialSpeechBubble).isEqualTo(com.kasif.arrowbuddies.R.string.ui_tutorial_tap_me)
    }

    @Test
    fun `level 2 tutorial protects hearts on bonk and shows helpful speech bubble`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val savedStateHandle = SavedStateHandle(mapOf("levelId" to "w1-l02"))
        val viewModel = GameViewModel(levelRepository, gameRepository, adsManager, savedStateHandle, hapticsManager, soundManager)
        advanceUntilIdle() // let the level finish loading in the background

        // Blocked buddy bonks into blocker
        viewModel.onEvent(GameUiEvent.TapBuddy("piglet_1"))
        advanceUntilIdle()

        // Hearts remain intact during tutorial!
        assertThat(viewModel.uiState.value.heartsRemaining).isEqualTo(3)
        assertThat(viewModel.uiState.value.tutorialSpeechBubble).isEqualTo(com.kasif.arrowbuddies.R.string.ui_tutorial_free_friend)
    }

    @Test
    fun `request hint highlights solvable buddy and points hand`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val savedStateHandle = SavedStateHandle(mapOf("levelId" to "1"))
        val viewModel = GameViewModel(levelRepository, gameRepository, adsManager, savedStateHandle, hapticsManager, soundManager)
        viewModel.computeDispatcher = StandardTestDispatcher(testScheduler)
        advanceUntilIdle()

        viewModel.onEvent(GameUiEvent.RequestHint)
        advanceTimeBy(50)

        val state = viewModel.uiState.value
        assertThat(state.hintBuddyId).isNotNull()
        assertThat(state.tutorialHandPos).isNotNull()

        advanceTimeBy(2100)
        assertThat(viewModel.uiState.value.hintBuddyId).isNull()
    }

    @Test
    fun `pause and leave level dialog events toggle visibility correctly`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val savedStateHandle = SavedStateHandle(mapOf("levelId" to "1"))
        val viewModel = GameViewModel(levelRepository, gameRepository, adsManager, savedStateHandle, hapticsManager, soundManager)
        advanceUntilIdle() // let the level finish loading in the background

        viewModel.onEvent(GameUiEvent.OpenPauseSheet)
        assertThat(viewModel.uiState.value.showPauseSheet).isTrue()

        viewModel.onEvent(GameUiEvent.DismissPauseSheet)
        assertThat(viewModel.uiState.value.showPauseSheet).isFalse()

        viewModel.onEvent(GameUiEvent.RequestLeaveLevel)
        assertThat(viewModel.uiState.value.showLeaveLevelDialog).isTrue()

        viewModel.onEvent(GameUiEvent.DismissLeaveLevel)
        assertThat(viewModel.uiState.value.showLeaveLevelDialog).isFalse()
    }
}
