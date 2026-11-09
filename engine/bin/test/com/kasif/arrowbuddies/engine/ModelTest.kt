package com.kasif.arrowbuddies.engine

import com.google.common.truth.Truth.assertThat
import com.kasif.arrowbuddies.engine.codec.LevelCodec
import com.kasif.arrowbuddies.engine.model.Buddy
import com.kasif.arrowbuddies.engine.model.Direction
import com.kasif.arrowbuddies.engine.model.GameState
import com.kasif.arrowbuddies.engine.model.LevelDef
import com.kasif.arrowbuddies.engine.model.Pos
import com.kasif.arrowbuddies.engine.model.Tile
import org.junit.Test

class ModelTest {

    @Test
    fun `Pos arithmetic and string representations`() {
        val p1 = Pos(2, 3)
        val p2 = Pos(1, 1)

        assertThat(p1 + p2).isEqualTo(Pos(3, 4))
        assertThat(p1 - p2).isEqualTo(Pos(1, 2))
        assertThat(p1 + Direction.UP).isEqualTo(Pos(2, 2))
        assertThat(p1.toString()).isEqualTo("(2, 3)")
    }

    @Test
    fun `Direction rotations and opposites`() {
        assertThat(Direction.UP.rotateClockwise()).isEqualTo(Direction.RIGHT)
        assertThat(Direction.UP.turnClockwise90()).isEqualTo(Direction.RIGHT)

        assertThat(Direction.UP.rotateCounterClockwise()).isEqualTo(Direction.LEFT)
        assertThat(Direction.UP.turnCounterClockwise90()).isEqualTo(Direction.LEFT)

        assertThat(Direction.UP.opposite()).isEqualTo(Direction.DOWN)
        assertThat(Direction.RIGHT.opposite()).isEqualTo(Direction.LEFT)
        assertThat(Direction.DOWN.opposite()).isEqualTo(Direction.UP)
        assertThat(Direction.LEFT.opposite()).isEqualTo(Direction.RIGHT)
    }

    @Test
    fun `LevelDef boundary and edge queries`() {
        val tiles = mapOf(
            Pos(0, 0) to Tile.Rock,
            Pos(1, 1) to Tile.Portal(pairId = 2),
            Pos(2, 2) to Tile.Portal(pairId = 2)
        )
        val level = LevelDef(
            id = "test",
            cols = 4,
            rows = 4,
            tiles = tiles,
            buddies = listOf(Buddy(id = "b1", species = "BUNNY", pos = Pos(0, 1), dir = Direction.UP)),
            world = 1,
            index = 1
        )

        assertThat(level.isInside(Pos(0, 0))).isTrue()
        assertThat(level.isInside(Pos(3, 3))).isTrue()
        assertThat(level.isInside(Pos(-1, 0))).isFalse()
        assertThat(level.isInside(Pos(0, 4))).isFalse()

        assertThat(level.isEdge(Pos(0, 2))).isTrue()
        assertThat(level.isEdge(Pos(3, 2))).isTrue()
        assertThat(level.isEdge(Pos(2, 0))).isTrue()
        assertThat(level.isEdge(Pos(2, 3))).isTrue()
        assertThat(level.isEdge(Pos(1, 1))).isFalse()
        assertThat(level.isEdge(Pos(10, 10))).isFalse()

        assertThat(level.portalPositions(2)).containsExactly(Pos(1, 1), Pos(2, 2))
        assertThat(level.tileAt(Pos(3, 3))).isEqualTo(Tile.Empty)
    }

    @Test
    fun `GameState metrics and helper properties`() {
        val b1 = Buddy(id = "b1", species = "BUNNY", pos = Pos(1, 1), dir = Direction.RIGHT)
        val level = LevelDef(
            id = "test",
            cols = 4,
            rows = 4,
            tiles = emptyMap(),
            buddies = listOf(b1),
            world = 1,
            index = 1
        )
        val state = GameState.fromLevel(level)

        assertThat(state.initialBuddyCount).isEqualTo(1)
        assertThat(state.rescuedCount).isEqualTo(0)
        assertThat(state.buddyAt(Pos(1, 1))).isEqualTo(b1)
        assertThat(state.buddyAt(Pos(2, 2))).isNull()
        assertThat(b1.isImmobilized).isFalse()

        val caged = b1.copy(cageColor = "BLUE")
        assertThat(caged.isImmobilized).isTrue()

        val sleepy = b1.copy(sleepy = true)
        assertThat(sleepy.isImmobilized).isTrue()
    }

    @Test(expected = IllegalArgumentException::class)
    fun `LevelCodec rejects column count above 7`() {
        val json = """
            { "id": "t", "world": 1, "index": 1, "cols": 8, "rows": 3, "grid": ["........","........","........"], "buddies": [] }
        """.trimIndent()
        LevelCodec.decode(json)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `LevelCodec rejects row count above 9`() {
        val json = """
            { "id": "t", "world": 1, "index": 1, "cols": 3, "rows": 10, "grid": ["...","...","...","...","...","...","...","...","...","..."], "buddies": [] }
        """.trimIndent()
        LevelCodec.decode(json)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `LevelCodec rejects unknown tile character`() {
        val json = """
            { "id": "t", "world": 1, "index": 1, "cols": 3, "rows": 3, "grid": ["...",".X.","..."], "buddies": [] }
        """.trimIndent()
        LevelCodec.decode(json)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `LevelCodec rejects invalid direction code`() {
        val json = """
            { "id": "t", "world": 1, "index": 1, "cols": 3, "rows": 3, "grid": ["...","...","..."], "buddies": [{"x":1,"y":1,"d":"INVALID","s":"BUNNY"}] }
        """.trimIndent()
        LevelCodec.decode(json)
    }

    @Test
    fun `LevelIdHelper parses and formats campaign level indices accurately`() {
        val helper = com.kasif.arrowbuddies.engine.model.LevelIdHelper

        // Parsing
        assertThat(helper.parseFlatIndex("w1-l01")).isEqualTo(1)
        assertThat(helper.parseFlatIndex("w1-l02")).isEqualTo(2)
        assertThat(helper.parseFlatIndex("w1-l30")).isEqualTo(30)
        assertThat(helper.parseFlatIndex("w2-l01")).isEqualTo(31)
        assertThat(helper.parseFlatIndex("w8-l30")).isEqualTo(240)
        assertThat(helper.parseFlatIndex("w1-l01-dev")).isEqualTo(1)
        assertThat(helper.parseFlatIndex("1")).isEqualTo(1)
        assertThat(helper.parseFlatIndex("42")).isEqualTo(42)
        assertThat(helper.parseFlatIndex("invalid")).isNull()

        // Formatting
        assertThat(helper.formatLevelId(1)).isEqualTo("w1-l01")
        assertThat(helper.formatLevelId(2)).isEqualTo("w1-l02")
        assertThat(helper.formatLevelId(30)).isEqualTo("w1-l30")
        assertThat(helper.formatLevelId(31)).isEqualTo("w2-l01")
        assertThat(helper.formatLevelId(240)).isEqualTo("w8-l30")
    }
}

