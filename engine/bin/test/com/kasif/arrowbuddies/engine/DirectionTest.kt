package com.kasif.arrowbuddies.engine

import com.google.common.truth.Truth.assertThat
import com.kasif.arrowbuddies.engine.model.Direction
import org.junit.Test

class DirectionTest {

    @Test
    fun `turnClockwise90 completes a full 360 rotation cycle`() {
        var dir = Direction.UP
        dir = dir.turnClockwise90()
        assertThat(dir).isEqualTo(Direction.RIGHT)
        dir = dir.turnClockwise90()
        assertThat(dir).isEqualTo(Direction.DOWN)
        dir = dir.turnClockwise90()
        assertThat(dir).isEqualTo(Direction.LEFT)
        dir = dir.turnClockwise90()
        assertThat(dir).isEqualTo(Direction.UP)
    }

    @Test
    fun `turnCounterClockwise90 reverses clockwise rotation`() {
        assertThat(Direction.UP.turnCounterClockwise90()).isEqualTo(Direction.LEFT)
        assertThat(Direction.LEFT.turnCounterClockwise90()).isEqualTo(Direction.DOWN)
        assertThat(Direction.DOWN.turnCounterClockwise90()).isEqualTo(Direction.RIGHT)
        assertThat(Direction.RIGHT.turnCounterClockwise90()).isEqualTo(Direction.UP)
    }

    @Test
    fun `opposite reverses direction correctly`() {
        assertThat(Direction.UP.opposite()).isEqualTo(Direction.DOWN)
        assertThat(Direction.DOWN.opposite()).isEqualTo(Direction.UP)
        assertThat(Direction.LEFT.opposite()).isEqualTo(Direction.RIGHT)
        assertThat(Direction.RIGHT.opposite()).isEqualTo(Direction.LEFT)
    }

    @Test
    fun `grid delta values match Cartesian screen coordinates`() {
        assertThat(Direction.UP.dx).isEqualTo(0)
        assertThat(Direction.UP.dy).isEqualTo(-1)

        assertThat(Direction.DOWN.dx).isEqualTo(0)
        assertThat(Direction.DOWN.dy).isEqualTo(1)

        assertThat(Direction.LEFT.dx).isEqualTo(-1)
        assertThat(Direction.LEFT.dy).isEqualTo(0)

        assertThat(Direction.RIGHT.dx).isEqualTo(1)
        assertThat(Direction.RIGHT.dy).isEqualTo(0)
    }
}

