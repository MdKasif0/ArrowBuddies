package com.kasif.arrowbuddies.feature.levellab

import com.kasif.arrowbuddies.data.repository.LevelRepository
import com.kasif.arrowbuddies.engine.model.LevelDef
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LevelLabViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeLevelRepository: LevelRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeLevelRepository = object : LevelRepository {
            private val map = mutableMapOf<String, LevelDef>()
            override fun loadLevel(levelId: String): LevelDef = map[levelId] ?: error("Not found")
            override fun getAvailableDevLevelIds(): List<String> = emptyList()
            override fun registerCustomLevel(level: LevelDef): String {
                map[level.id] = level
                return level.id
            }
        }
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun generatesLevelAndUpdatesMetrics() = runTest {
        val viewModel = LevelLabViewModel(fakeLevelRepository, null, testDispatcher)
        viewModel.onEvent(LevelLabEvent.Generate)
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNotNull(state.generatedLevel)
        assertNotNull(state.solveResult)
        assertTrue(state.solveResult!!.solvable)
        assertTrue(state.jsonString.isNotBlank())

        // Test updating seed and generating
        viewModel.onEvent(LevelLabEvent.UpdateSeed(54321L))
        viewModel.onEvent(LevelLabEvent.Generate)
        testScheduler.advanceUntilIdle()

        val newState = viewModel.uiState.value
        assertEquals(54321L, newState.seed)
        assertTrue(newState.solveResult!!.solvable)
    }

    @Test
    fun registersLevelForPlay() = runTest {
        val viewModel = LevelLabViewModel(fakeLevelRepository, null, testDispatcher)
        viewModel.onEvent(LevelLabEvent.Generate)
        testScheduler.advanceUntilIdle()

        val levelId = viewModel.prepareForPlay()
        assertNotNull(levelId)
        val loaded = fakeLevelRepository.loadLevel(levelId!!)
        assertEquals(viewModel.uiState.value.generatedLevel, loaded)
    }
}
