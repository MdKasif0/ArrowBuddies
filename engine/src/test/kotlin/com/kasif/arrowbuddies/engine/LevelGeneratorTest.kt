package com.kasif.arrowbuddies.engine

import com.kasif.arrowbuddies.engine.generator.GenParams
import com.kasif.arrowbuddies.engine.generator.LevelGenerator
import com.kasif.arrowbuddies.engine.solver.LevelSolver
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LevelGeneratorTest {

    @Test
    fun generatesSolvableLevelDeterministically() {
        val seed = 12345L
        val params = GenParams(
            seed = seed,
            cols = 5,
            rows = 5,
            buddyCount = 6,
            allowedMechanics = setOf("rock"),
            obstacleBudget = 2,
            targetScoreRange = 10.0..30.0,
            speciesPool = listOf("BUNNY", "CHICK", "PIGLET"),
            world = 1,
            index = 5
        )

        val level1 = LevelGenerator.generate(params)
        val level2 = LevelGenerator.generate(params)

        assertEquals("Generator must be deterministic for identical seeds", level1, level2)

        val solveResult = LevelSolver.solve(level1)
        assertTrue("Generated level must be solvable", solveResult.solvable)
        assertEquals(6, solveResult.metrics.buddyCount)
        assertTrue(
            "Initial free moves must not exceed 50%",
            solveResult.metrics.initialFreeMoves <= 3
        )
    }

    @Test
    fun generatesLevelWithMechanics() {
        val params = GenParams(
            seed = 99999L,
            cols = 6,
            rows = 6,
            buddyCount = 8,
            allowedMechanics = setOf("rock", "mud", "turntable", "cage", "portal", "sleepy"),
            obstacleBudget = 3,
            targetScoreRange = 20.0..50.0,
            speciesPool = listOf("TURTLE", "OTTER", "CRAB"),
            world = 5,
            index = 12
        )

        val level = LevelGenerator.generate(params)
        val solveResult = LevelSolver.solve(level)
        assertTrue("Generated mechanic level must be solvable", solveResult.solvable)
        assertEquals(8, level.buddies.size)
    }

    @Test
    fun generatesBigRescueLevelWithGoldenBuddy() {
        val params = GenParams(
            seed = LevelGenerator.stableHash("ArrowBuddies-v1", 1, 15),
            cols = 6,
            rows = 7,
            buddyCount = 12,
            allowedMechanics = setOf("rock"),
            obstacleBudget = 3,
            targetScoreRange = 35.0..65.0,
            speciesPool = listOf("BUNNY", "CHICK", "PIGLET"),
            world = 1,
            index = 15,
            isBigRescue = true
        )

        val level = LevelGenerator.generate(params)
        assertTrue("Big Rescue level 15 must contain exactly one golden buddy", level.buddies.any { it.isGolden })
        val solveResult = LevelSolver.solve(level)
        assertTrue("Big rescue level must be solvable", solveResult.solvable)
    }
}
