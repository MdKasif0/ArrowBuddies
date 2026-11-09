package com.kasif.arrowbuddies.feature.home

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
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
class HomeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private class FakeGameRepository : GameRepository {
        val rescuedFlow = MutableStateFlow(0)
        override val totalRescued: Flow<Int> = rescuedFlow
        override val berries: Flow<Int> = MutableStateFlow(0)
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

        override fun getLevelStars(levelId: String): Flow<Int> = kotlinx.coroutines.flow.flowOf(0)
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
        override fun isMechanicSeen(mechanic: String): Flow<Boolean> = kotlinx.coroutines.flow.flowOf(true)
        override suspend fun setMechanicSeen(mechanic: String) {}
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
    fun totalRescuedReflectsRepositoryState() = runTest(testDispatcher) {
        val repository = FakeGameRepository()
        val viewModel = HomeViewModel(repository)

        viewModel.uiState.test {
            val initial = awaitItem()
            assertThat(initial.totalRescued).isEqualTo(0)

            repository.rescuedFlow.value = 42
            testScheduler.advanceUntilIdle()

            val updated = awaitItem()
            assertThat(updated.totalRescued).isEqualTo(42)
        }
    }
}

