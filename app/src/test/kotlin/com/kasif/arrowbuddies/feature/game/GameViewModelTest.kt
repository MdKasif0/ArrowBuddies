package com.kasif.arrowbuddies.feature.game

import android.view.View
import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.kasif.arrowbuddies.core.audio.SoundManager
import com.kasif.arrowbuddies.core.haptics.HapticsManager
import com.kasif.arrowbuddies.data.repository.DefaultLevelRepository
import com.kasif.arrowbuddies.data.repository.LevelRepository
import com.kasif.arrowbuddies.engine.codec.LevelCodec
import com.kasif.arrowbuddies.engine.model.GameStatus
import com.kasif.arrowbuddies.engine.model.LevelDef
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
            val json = when (levelId) {
                "2", "w1-l02-dev" -> DefaultLevelRepository.DEV_02_JSON
                "3", "w1-l03-dev" -> DefaultLevelRepository.DEV_03_JSON
                else -> DefaultLevelRepository.DEV_01_JSON
            }
            return LevelCodec.decode(json)
        }

        override fun getAvailableDevLevelIds(): List<String> {
            return listOf("w1-l01-dev", "w1-l02-dev", "w1-l03-dev")
        }
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
    private lateinit var hapticsManager: FakeHapticsManager
    private lateinit var soundManager: FakeSoundManager

    @Before
    fun setUp() {
        levelRepository = FakeLevelRepository()
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
        val viewModel = GameViewModel(levelRepository, savedStateHandle, hapticsManager, soundManager)

        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.levelId).isEqualTo("w1-l01-dev")
            assertThat(state.heartsRemaining).isEqualTo(3)
            assertThat(state.rescuedCount).isEqualTo(0)
            assertThat(state.totalBuddiesInLevel).isEqualTo(2)
            assertThat(state.isInputLocked).isFalse()
            assertThat(state.isWon).isFalse()
            assertThat(state.isLost).isFalse()
            // In dev 01 (4x4), bunny at (1,1) facing UP can escape directly, chick at (2,2) facing DOWN can escape directly
            assertThat(state.safeBuddyIds).containsExactly("bunny_1", "chick_1")
        }
    }

    @Test
    fun `tapping escaping buddy locks input, plays escape effect, updates state, and wins on last buddy`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val savedStateHandle = SavedStateHandle(mapOf("levelId" to "1"))
        val viewModel = GameViewModel(levelRepository, savedStateHandle, hapticsManager, soundManager)

        // Tap bunny_1 -> escapes
        viewModel.onEvent(GameUiEvent.TapBuddy("bunny_1"))
        advanceTimeBy(50) // Immediate feedback
        assertThat(viewModel.uiState.value.isInputLocked).isTrue()

        // Advance past escape animation duration (70 + dash + 320)
        advanceTimeBy(600)
        assertThat(viewModel.uiState.value.isInputLocked).isFalse()
        assertThat(viewModel.uiState.value.rescuedCount).isEqualTo(1)
        assertThat(viewModel.uiState.value.gameState.buddies.map { it.id }).containsExactly("chick_1")

        // Now tap chick_1 -> final rescue -> Win
        viewModel.onEvent(GameUiEvent.TapBuddy("chick_1"))
        advanceTimeBy(600)
        advanceTimeBy(600) // celebration delay

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
        val viewModel = GameViewModel(levelRepository, savedStateHandle, hapticsManager, soundManager)

        // Level 2 has bunny at (1,2) facing LEFT (clear), chick at (2,2) facing LEFT (blocked by bunny!), piglet at (3,2) facing RIGHT (clear)
        assertThat(viewModel.uiState.value.safeBuddyIds).containsExactly("bunny_1", "piglet_1")

        // Tapping chick_1 should bonk against bunny_1
        viewModel.onEvent(GameUiEvent.TapBuddy("chick_1"))
        advanceTimeBy(50)
        assertThat(viewModel.uiState.value.isInputLocked).isTrue()

        advanceTimeBy(600)
        assertThat(viewModel.uiState.value.isInputLocked).isFalse()
        assertThat(viewModel.uiState.value.heartsRemaining).isEqualTo(2)
        assertThat(viewModel.uiState.value.gameState.mistakes).isEqualTo(1)

        // Second bonk
        viewModel.onEvent(GameUiEvent.TapBuddy("chick_1"))
        advanceTimeBy(600)
        assertThat(viewModel.uiState.value.heartsRemaining).isEqualTo(1)

        // Third bonk -> 0 hearts -> Lost!
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
        val viewModel = GameViewModel(levelRepository, savedStateHandle, hapticsManager, soundManager)

        // Tap bunny_1 -> escapes
        viewModel.onEvent(GameUiEvent.TapBuddy("bunny_1"))
        advanceTimeBy(600)
        assertThat(viewModel.uiState.value.rescuedCount).isEqualTo(1)
        assertThat(viewModel.uiState.value.canUndo).isTrue()

        // Call undo
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
        val viewModel = GameViewModel(levelRepository, savedStateHandle, hapticsManager, soundManager)

        // Bonk once
        viewModel.onEvent(GameUiEvent.TapBuddy("chick_1"))
        advanceTimeBy(600)
        assertThat(viewModel.uiState.value.heartsRemaining).isEqualTo(2)

        // Restart
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
        val viewModel = GameViewModel(levelRepository, savedStateHandle, hapticsManager, soundManager)

        // Path peek on bunny_1 (facing LEFT, exit is clear)
        viewModel.onEvent(GameUiEvent.StartPathPeek("bunny_1"))
        val bunnyPeek = viewModel.uiState.value.activePeekingPath
        assertThat(bunnyPeek).isNotNull()
        assertThat(bunnyPeek!!.isEscape).isTrue()

        viewModel.onEvent(GameUiEvent.EndPathPeek)
        assertThat(viewModel.uiState.value.activePeekingPath).isNull()

        // Path peek on chick_1 (facing LEFT, blocked by bunny_1 at (1,2))
        viewModel.onEvent(GameUiEvent.StartPathPeek("chick_1"))
        val chickPeek = viewModel.uiState.value.activePeekingPath
        assertThat(chickPeek).isNotNull()
        assertThat(chickPeek!!.isEscape).isFalse()
        assertThat(chickPeek.endPos.x).isEqualTo(1)
        assertThat(chickPeek.endPos.y).isEqualTo(2)
    }

    @Test
    fun `survives process recreation by restoring serialized GameState from SavedStateHandle`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val savedStateHandle = SavedStateHandle(mapOf("levelId" to "1"))
        val vm1 = GameViewModel(levelRepository, savedStateHandle, hapticsManager, soundManager)

        // Free buddy_1
        vm1.onEvent(GameUiEvent.TapBuddy("bunny_1"))
        advanceTimeBy(600)
        assertThat(vm1.uiState.value.rescuedCount).isEqualTo(1)

        // SavedStateHandle now contains saved_game_state
        val savedJson = savedStateHandle.get<String>(GameViewModel.KEY_SAVED_GAME_STATE)
        assertThat(savedJson).isNotNull()

        // Simulate new ViewModel instance restored with same SavedStateHandle
        val vm2 = GameViewModel(levelRepository, savedStateHandle, hapticsManager, soundManager)
        assertThat(vm2.uiState.value.rescuedCount).isEqualTo(1)
        assertThat(vm2.uiState.value.gameState.buddies.map { it.id }).containsExactly("chick_1")
    }
}
