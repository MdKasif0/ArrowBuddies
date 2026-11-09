package com.kasif.arrowbuddies.engine

import com.google.common.truth.Truth.assertThat
import com.kasif.arrowbuddies.engine.codec.LevelCodec
import com.kasif.arrowbuddies.engine.model.GameState
import com.kasif.arrowbuddies.engine.model.GameStatus
import com.kasif.arrowbuddies.engine.model.MoveResult
import java.io.File
import org.junit.Test

class HandcraftedDevLevelsTest {

    private fun findLevelFile(filename: String): File {
        val candidates = listOf(
            File("src/main/assets/levels/dev/$filename"),
            File("../app/src/main/assets/levels/dev/$filename"),
            File("app/src/main/assets/levels/dev/$filename")
        )
        return candidates.firstOrNull { it.exists() }
            ?: throw IllegalStateException("Could not find $filename in candidate locations")
    }

    @Test
    fun `level_dev_01 decodes and is solvable`() {
        val file = findLevelFile("level_dev_01.json")
        val level = LevelCodec.decode(file.readText())
        assertThat(level.id).isEqualTo("w1-l01-dev")
        assertThat(level.buddies).hasSize(2)

        var state = GameState.fromLevel(level)
        val out1 = GameEngine.tap(state, "bunny_1")
        assertThat(out1.result).isInstanceOf(MoveResult.Escaped::class.java)

        val out2 = GameEngine.tap(out1.newState, "chick_1")
        assertThat(out2.result).isInstanceOf(MoveResult.Escaped::class.java)
        assertThat(out2.newState.status).isEqualTo(GameStatus.WON)
        assertThat(out2.newState.stars).isEqualTo(3)
    }

    @Test
    fun `level_dev_02 decodes and is solvable with ordering`() {
        val file = findLevelFile("level_dev_02.json")
        val level = LevelCodec.decode(file.readText())
        assertThat(level.id).isEqualTo("w1-l02-dev")
        assertThat(level.buddies).hasSize(3)

        var state = GameState.fromLevel(level)

        // chick_1 tapping first bonks into bunny_1!
        val bonkOutcome = GameEngine.tap(state, "chick_1")
        assertThat(bonkOutcome.result).isInstanceOf(MoveResult.Bonked::class.java)

        // Correct order: bunny_1, then chick_1, then piglet_1
        state = GameEngine.tap(state, "bunny_1").newState
        state = GameEngine.tap(state, "chick_1").newState
        state = GameEngine.tap(state, "piglet_1").newState

        assertThat(state.status).isEqualTo(GameStatus.WON)
        assertThat(state.stars).isEqualTo(3)
    }

    @Test
    fun `level_dev_03 decodes and cascades cleanly`() {
        val file = findLevelFile("level_dev_03.json")
        val level = LevelCodec.decode(file.readText())
        assertThat(level.id).isEqualTo("w1-l03-dev")
        assertThat(level.buddies).hasSize(3)

        var state = GameState.fromLevel(level)

        // Piglet tapping first bonks into Chick!
        val bonkOutcome = GameEngine.tap(state, "piglet_1")
        assertThat(bonkOutcome.result).isInstanceOf(MoveResult.Bonked::class.java)

        // Correct cascading order: bunny_1 -> chick_1 -> piglet_1
        state = GameEngine.tap(state, "bunny_1").newState
        state = GameEngine.tap(state, "chick_1").newState
        state = GameEngine.tap(state, "piglet_1").newState

        assertThat(state.status).isEqualTo(GameStatus.WON)
        assertThat(state.stars).isEqualTo(3)
    }
}
