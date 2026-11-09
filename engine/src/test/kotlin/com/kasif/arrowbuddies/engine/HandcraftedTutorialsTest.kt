package com.kasif.arrowbuddies.engine

import com.kasif.arrowbuddies.engine.generator.HandcraftedTutorials
import com.kasif.arrowbuddies.engine.solver.LevelSolver
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class HandcraftedTutorialsTest {

    @Test
    fun all24TutorialLevelsAreSolvable() {
        for (world in 1..8) {
            for (index in 1..3) {
                val level = HandcraftedTutorials.getTutorialLevel(world, index)
                assertNotNull("Tutorial comment must not be null", level.comment)
                val result = LevelSolver.solve(level)
                assertTrue("Tutorial level w$world-l0$index must be solvable: ${level.comment}", result.solvable)
                assertTrue("Shortest solution must not be empty", result.shortestSolution.isNotEmpty())
            }
        }
    }
}
