package com.kasif.arrowbuddies.engine

import com.kasif.arrowbuddies.engine.codec.LevelCodec
import com.kasif.arrowbuddies.engine.generator.LevelPackBaker
import com.kasif.arrowbuddies.engine.solver.LevelSolver
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import kotlin.system.measureTimeMillis

class LevelPackSolvabilityTest {

    @Test
    fun all240BakedLevelsAreSolvableInUnder15Seconds() {
        val assetsDir = File("../app/src/main/assets/levels").let {
            if (it.exists()) it else File("app/src/main/assets/levels")
        }

        assertTrue("Assets levels directory must exist: ${assetsDir.absolutePath}", assetsDir.exists())

        var verifiedCount = 0
        val elapsed = measureTimeMillis {
            for (world in 1..LevelPackBaker.TOTAL_WORLDS) {
                val worldFile = File(assetsDir, "world_$world.json")
                assertTrue("World file $world must exist", worldFile.exists())
                val levels = LevelCodec.decodeList(worldFile.readText())
                assertEquals(LevelPackBaker.LEVELS_PER_WORLD, levels.size)

                for (level in levels) {
                    val result = LevelSolver.solve(level)
                    assertTrue("Level ${level.id} must be solvable", result.solvable)
                    assertTrue("Level ${level.id} shortest solution must not be empty", result.shortestSolution.isNotEmpty())
                    verifiedCount++
                }
            }
        }

        assertEquals(240, verifiedCount)
        assertTrue("Solving all 240 levels took $elapsed ms, must be under 15,000 ms", elapsed < 15000L)
    }
}

