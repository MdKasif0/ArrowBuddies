package com.kasif.arrowbuddies.engine

import com.kasif.arrowbuddies.engine.runtime.DailyRescue
import com.kasif.arrowbuddies.engine.runtime.RescueRun
import com.kasif.arrowbuddies.engine.solver.LevelSolver
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class RuntimeModesTest {

    @Test
    fun dailyRescueGeneratesSolvableDeterministicLevel() {
        val testDate = LocalDate.of(2026, 4, 15)
        val level1 = DailyRescue.generate(testDate)
        val level2 = DailyRescue.generate(testDate)

        assertEquals("Daily rescue must be identical for the same date", level1, level2)
        assertEquals("daily-20260415", level1.id)

        val result = LevelSolver.solve(level1)
        assertTrue("Daily rescue level must be solvable", result.solvable)
        assertTrue("Daily rescue must have buddies", result.metrics.buddyCount > 0)
    }

    @Test
    fun rescueRunAdvancesAndPrefetches() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        val rescueRun = RescueRun(runSeed = 42L, dispatcher = testDispatcher)

        val stage1 = rescueRun.getCurrentLevel()
        assertEquals(1, rescueRun.currentStage)
        assertEquals("rescuerun-1", stage1.id)

        val result1 = LevelSolver.solve(stage1)
        assertTrue("Stage 1 must be solvable", result1.solvable)

        // Prefetch stage 2
        val scope = TestScope(testDispatcher)
        rescueRun.prefetchNext(scope)
        testScheduler.advanceUntilIdle()

        // Advance to stage 2
        val stage2 = rescueRun.advanceToNext(scope)
        assertEquals(2, rescueRun.currentStage)
        assertEquals("rescuerun-2", stage2.id)

        val result2 = LevelSolver.solve(stage2)
        assertTrue("Stage 2 must be solvable", result2.solvable)
    }
}
