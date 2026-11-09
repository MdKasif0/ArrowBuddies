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
        override suspend fun setSoundEnabled(enabled: Boolean) {}
        override suspend fun setMusicEnabled(enabled: Boolean) {}
        override suspend fun setHapticsEnabled(enabled: Boolean) {}
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

