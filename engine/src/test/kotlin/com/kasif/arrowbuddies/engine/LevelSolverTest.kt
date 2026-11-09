package com.kasif.arrowbuddies.engine

import com.google.common.truth.Truth.assertThat
import com.kasif.arrowbuddies.engine.codec.LevelCodec
import com.kasif.arrowbuddies.engine.difficulty.DifficultyCalculator
import com.kasif.arrowbuddies.engine.model.Buddy
import com.kasif.arrowbuddies.engine.model.Direction
import com.kasif.arrowbuddies.engine.model.GameState
import com.kasif.arrowbuddies.engine.model.LevelDef
import com.kasif.arrowbuddies.engine.model.Pos
import com.kasif.arrowbuddies.engine.model.Tile
import com.kasif.arrowbuddies.engine.solver.LevelSolver
import org.junit.Test

class LevelSolverTest {

    @Test
    fun `solves simple 2-buddy level and computes metrics`() {
        // Dev level 1: Bunny at (1,1) facing UP, Chick at (2,2) facing DOWN
        val buddies = listOf(
            Buddy("b1", "BUNNY", Pos(1, 1), Direction.UP),
            Buddy("b2", "CHICK", Pos(2, 2), Direction.DOWN)
        )
        val level = LevelDef(
            id = "test-1",
            cols = 4,
            rows = 4,
            tiles = emptyMap(),
            buddies = buddies,
            world = 1,
            index = 1
        )

        val result = LevelSolver.solve(level)
        assertThat(result.solvable).isTrue()
        assertThat(result.shortestSolution.size).isEqualTo(2)
        assertThat(result.solutionCount).isAtLeast(1)
        assertThat(result.metrics.buddyCount).isEqualTo(2)
        assertThat(result.metrics.initialFreeMoves).isEqualTo(2)
        assertThat(result.metrics.dependencyDepth).isEqualTo(1)
    }

    @Test
    fun `computes dependency depth correctly for linear dependency chain`() {
        // b1 is at (1, 1) facing LEFT (clear to exit)
        // b2 is at (2, 1) facing LEFT (blocked by b1)
        // b3 is at (3, 1) facing LEFT (blocked by b2)
        // Order must be b1 -> b2 -> b3. Depth must be 3.
        val buddies = listOf(
            Buddy("b1", "BUNNY", Pos(1, 1), Direction.LEFT),
            Buddy("b2", "CHICK", Pos(2, 1), Direction.LEFT),
            Buddy("b3", "PIGLET", Pos(3, 1), Direction.LEFT)
        )
        val level = LevelDef(
            id = "test-chain",
            cols = 5,
            rows = 4,
            tiles = emptyMap(),
            buddies = buddies,
            world = 1,
            index = 2
        )

        val result = LevelSolver.solve(level)
        assertThat(result.solvable).isTrue()
        assertThat(result.shortestSolution).containsExactly("b1", "b2", "b3").inOrder()
        assertThat(result.metrics.dependencyDepth).isEqualTo(3)
        assertThat(result.metrics.initialFreeMoves).isEqualTo(1)
    }

    @Test
    fun `detects unsolvable level where two buddies face each other directly`() {
        // b1 at (1,1) facing RIGHT, b2 at (2,1) facing LEFT -> mutual head-on bonk!
        val buddies = listOf(
            Buddy("b1", "BUNNY", Pos(1, 1), Direction.RIGHT),
            Buddy("b2", "CHICK", Pos(2, 1), Direction.LEFT)
        )
        val level = LevelDef(
            id = "test-unsolvable",
            cols = 4,
            rows = 4,
            tiles = emptyMap(),
            buddies = buddies,
            world = 1,
            index = 3
        )

        val result = LevelSolver.solve(level)
        assertThat(result.solvable).isFalse()
        assertThat(result.shortestSolution).isEmpty()
    }

    @Test
    fun `solves 7x9 board with 20 buddies in under 200 ms`() {
        // Construct a large 7x9 board with 20 buddies
        val buddies = mutableListOf<Buddy>()
        var id = 1
        for (y in 1..7) {
            buddies.add(Buddy("b${id++}", "BUNNY", Pos(0, y), Direction.LEFT))
            buddies.add(Buddy("b${id++}", "CHICK", Pos(6, y), Direction.RIGHT))
        }
        for (x in 1..6) {
            buddies.add(Buddy("b${id++}", "PIGLET", Pos(x, 0), Direction.UP))
        }

        assertThat(buddies.size).isEqualTo(20)

        val level = LevelDef(
            id = "test-large-20",
            cols = 7,
            rows = 9,
            tiles = emptyMap(),
            buddies = buddies,
            world = 1,
            index = 30
        )

        val result = LevelSolver.solve(level)
        assertThat(result.solvable).isTrue()
        assertThat(result.timeMs).isLessThan(200L)
        assertThat(result.shortestSolution.size).isEqualTo(20)
    }

    @Test
    fun `findHint returns a move on the shortest solution`() {
        val buddies = listOf(
            Buddy("b1", "BUNNY", Pos(1, 1), Direction.LEFT),
            Buddy("b2", "CHICK", Pos(2, 1), Direction.LEFT)
        )
        val level = LevelDef("test-hint", 4, 4, emptyMap(), buddies, 1, 1)
        val state = GameState(level, buddies)

        val hint = GameEngine.findHint(state)
        assertThat(hint).isEqualTo("b1")
    }

    @Test
    fun `difficulty score formula clamps to 0-100 and matches spec weights`() {
        // Score = 1.0*buddyCount + 2.5*dependencyDepth + 3.0*deadEndMoves + 1.5*mechanicCount - 1.2*initialFreeMoves
        val score = DifficultyCalculator.calculate(
            buddyCount = 10,
            dependencyDepth = 4,
            deadEndMoves = 2,
            mechanicCount = 3,
            initialFreeMoves = 2
        )
        // 1.0*10 + 2.5*4 + 3.0*2 + 1.5*3 - 1.2*2 = 10 + 10 + 6 + 4.5 - 2.4 = 28.1
        assertThat(score).isWithin(0.01).of(28.1)

        // Clamping check
        val negativeScore = DifficultyCalculator.calculate(
            buddyCount = 1,
            dependencyDepth = 0,
            deadEndMoves = 0,
            mechanicCount = 0,
            initialFreeMoves = 10
        )
        assertThat(negativeScore).isEqualTo(0.0)

        val excessiveScore = DifficultyCalculator.calculate(
            buddyCount = 50,
            dependencyDepth = 30,
            deadEndMoves = 20,
            mechanicCount = 10,
            initialFreeMoves = 0
        )
        assertThat(excessiveScore).isEqualTo(100.0)
    }
}
