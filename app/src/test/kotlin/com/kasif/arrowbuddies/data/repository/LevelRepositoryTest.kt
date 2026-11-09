package com.kasif.arrowbuddies.data.repository

import com.google.common.truth.Truth.assertThat
import com.kasif.arrowbuddies.engine.solver.LevelSolver
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class LevelRepositoryTest {

    private val repository = DefaultLevelRepository(RuntimeEnvironment.getApplication())

    @Test
    fun `campaign ids load the baked level`() {
        val level = repository.loadLevel("w2-l05")
        assertThat(level.world).isEqualTo(2)
        assertThat(level.index).isEqualTo(5)
        assertThat(level.buddies).isNotEmpty()
    }

    @Test
    fun `rescue run stages are real, solvable, distinct and stable`() = runTest {
        val stage1 = repository.loadLevelInBackground("rescuerun-1")
        val stage2 = repository.loadLevelInBackground("rescuerun-2")

        assertThat(stage1.id).isEqualTo("rescuerun-1")
        assertThat(stage2.id).isEqualTo("rescuerun-2")
        assertThat(stage1.buddies).isNotEmpty()
        assertThat(stage2.buddies).isNotEmpty()
        assertThat(stage2.buddies).isNotEqualTo(stage1.buddies)
        assertThat(LevelSolver.solve(stage1).solvable).isTrue()
        // Restarting a stage gives the same puzzle
        assertThat(repository.loadLevel("rescuerun-1")).isEqualTo(stage1)
    }

    @Test
    fun `daily id replays that exact date`() {
        val level = repository.loadLevel("daily-20260115")
        assertThat(level.id).isEqualTo("daily-20260115")
        assertThat(repository.loadLevel("daily-20260115")).isEqualTo(level)
    }
}
