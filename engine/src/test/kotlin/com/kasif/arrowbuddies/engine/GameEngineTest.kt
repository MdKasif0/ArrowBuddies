package com.kasif.arrowbuddies.engine

import com.google.common.truth.Truth.assertThat
import com.kasif.arrowbuddies.engine.codec.LevelCodec
import com.kasif.arrowbuddies.engine.model.BlockerKind
import com.kasif.arrowbuddies.engine.model.Buddy
import com.kasif.arrowbuddies.engine.model.Direction
import com.kasif.arrowbuddies.engine.model.GameState
import com.kasif.arrowbuddies.engine.model.GameStatus
import com.kasif.arrowbuddies.engine.model.LevelDef
import com.kasif.arrowbuddies.engine.model.MoveResult
import com.kasif.arrowbuddies.engine.model.Pos
import com.kasif.arrowbuddies.engine.model.RefusalReason
import com.kasif.arrowbuddies.engine.model.StepEvent
import com.kasif.arrowbuddies.engine.model.Tile
import org.junit.Test

class GameEngineTest {

    private fun createEmptyLevel(
        cols: Int = 5,
        rows: Int = 5,
        buddies: List<Buddy> = emptyList(),
        tiles: Map<Pos, Tile> = emptyMap()
    ): LevelDef = LevelDef(
        id = "test-level",
        cols = cols,
        rows = rows,
        tiles = tiles,
        buddies = buddies,
        world = 1,
        index = 1
    )

    // ==========================================
    // 1. Escaping in every direction
    // ==========================================

    @Test
    fun `buddy escapes upwards`() {
        val buddy = Buddy(id = "b1", species = "BUNNY", pos = Pos(2, 2), dir = Direction.UP)
        val state = GameState.fromLevel(createEmptyLevel(buddies = listOf(buddy)))

        val outcome = GameEngine.tap(state, "b1")
        assertThat(outcome.result).isInstanceOf(MoveResult.Escaped::class.java)
        val escaped = outcome.result as MoveResult.Escaped
        assertThat(escaped.exitSide).isEqualTo(Direction.UP)
        assertThat(escaped.path).hasSize(3) // (2,2), (2,1), (2,0)
        assertThat(outcome.newState.status).isEqualTo(GameStatus.WON)
        assertThat(outcome.newState.rescuedIds).contains("b1")
        assertThat(outcome.newState.buddies).isEmpty()
    }

    @Test
    fun `buddy escapes downwards`() {
        val buddy = Buddy(id = "b1", species = "BUNNY", pos = Pos(2, 2), dir = Direction.DOWN)
        val state = GameState.fromLevel(createEmptyLevel(buddies = listOf(buddy)))

        val outcome = GameEngine.tap(state, "b1")
        val escaped = outcome.result as MoveResult.Escaped
        assertThat(escaped.exitSide).isEqualTo(Direction.DOWN)
        assertThat(escaped.path).hasSize(3) // (2,2), (2,3), (2,4)
        assertThat(outcome.newState.status).isEqualTo(GameStatus.WON)
    }

    @Test
    fun `buddy escapes leftwards`() {
        val buddy = Buddy(id = "b1", species = "BUNNY", pos = Pos(2, 2), dir = Direction.LEFT)
        val state = GameState.fromLevel(createEmptyLevel(buddies = listOf(buddy)))

        val outcome = GameEngine.tap(state, "b1")
        val escaped = outcome.result as MoveResult.Escaped
        assertThat(escaped.exitSide).isEqualTo(Direction.LEFT)
        assertThat(escaped.path).hasSize(3) // (2,2), (1,2), (0,2)
        assertThat(outcome.newState.status).isEqualTo(GameStatus.WON)
    }

    @Test
    fun `buddy escapes rightwards`() {
        val buddy = Buddy(id = "b1", species = "BUNNY", pos = Pos(2, 2), dir = Direction.RIGHT)
        val state = GameState.fromLevel(createEmptyLevel(buddies = listOf(buddy)))

        val outcome = GameEngine.tap(state, "b1")
        val escaped = outcome.result as MoveResult.Escaped
        assertThat(escaped.exitSide).isEqualTo(Direction.RIGHT)
        assertThat(escaped.path).hasSize(3) // (2,2), (3,2), (4,2)
        assertThat(outcome.newState.status).isEqualTo(GameStatus.WON)
    }

    // ==========================================
    // 2. Buddy at the edge escaping immediately
    // ==========================================

    @Test
    fun `buddy at the edge escapes immediately`() {
        val buddy = Buddy(id = "b1", species = "BUNNY", pos = Pos(0, 3), dir = Direction.LEFT)
        val state = GameState.fromLevel(createEmptyLevel(buddies = listOf(buddy)))

        val outcome = GameEngine.tap(state, "b1")
        val escaped = outcome.result as MoveResult.Escaped
        assertThat(escaped.exitSide).isEqualTo(Direction.LEFT)
        assertThat(escaped.path).hasSize(1)
        assertThat(escaped.path.first().pos).isEqualTo(Pos(0, 3))
        assertThat(outcome.newState.status).isEqualTo(GameStatus.WON)
    }

    // ==========================================
    // 3. Bonking mechanics
    // ==========================================

    @Test
    fun `bonk against adjacent buddy costs 1 heart and leaves board unchanged`() {
        val b1 = Buddy(id = "b1", species = "BUNNY", pos = Pos(2, 2), dir = Direction.RIGHT)
        val b2 = Buddy(id = "b2", species = "CHICK", pos = Pos(3, 2), dir = Direction.UP)
        val state = GameState.fromLevel(createEmptyLevel(buddies = listOf(b1, b2)))

        val outcome = GameEngine.tap(state, "b1")
        assertThat(outcome.result).isInstanceOf(MoveResult.Bonked::class.java)
        val bonked = outcome.result as MoveResult.Bonked
        assertThat(bonked.blockerKind).isEqualTo(BlockerKind.BUDDY)
        assertThat(bonked.blockerPos).isEqualTo(Pos(3, 2))
        assertThat(bonked.path).hasSize(1) // Started at (2,2), stopped before (3,2)

        assertThat(outcome.newState.hearts).isEqualTo(2)
        assertThat(outcome.newState.mistakes).isEqualTo(1)
        assertThat(outcome.newState.buddies).containsExactly(b1, b2)
        assertThat(outcome.newState.status).isEqualTo(GameStatus.PLAYING)
    }

    @Test
    fun `bonk at a distance records the full traversed path`() {
        val b1 = Buddy(id = "b1", species = "BUNNY", pos = Pos(0, 2), dir = Direction.RIGHT)
        val b2 = Buddy(id = "b2", species = "CHICK", pos = Pos(4, 2), dir = Direction.DOWN)
        val state = GameState.fromLevel(createEmptyLevel(buddies = listOf(b1, b2)))

        val outcome = GameEngine.tap(state, "b1")
        val bonked = outcome.result as MoveResult.Bonked
        assertThat(bonked.blockerKind).isEqualTo(BlockerKind.BUDDY)
        assertThat(bonked.blockerPos).isEqualTo(Pos(4, 2))
        assertThat(bonked.path).hasSize(4) // (0,2), (1,2), (2,2), (3,2)
    }

    @Test
    fun `bonk against static rock`() {
        val rockPos = Pos(2, 0)
        val b1 = Buddy(id = "b1", species = "PIGLET", pos = Pos(2, 3), dir = Direction.UP)
        val state = GameState.fromLevel(
            createEmptyLevel(
                tiles = mapOf(rockPos to Tile.Rock),
                buddies = listOf(b1)
            )
        )

        val outcome = GameEngine.tap(state, "b1")
        val bonked = outcome.result as MoveResult.Bonked
        assertThat(bonked.blockerKind).isEqualTo(BlockerKind.ROCK)
        assertThat(bonked.blockerPos).isEqualTo(rockPos)
        assertThat(outcome.newState.hearts).isEqualTo(2)
    }

    // ==========================================
    // 4. Life & Game status (Losing at 0 hearts, Winning)
    // ==========================================

    @Test
    fun `3 bonks exhaust hearts and transition to Lost status`() {
        val b1 = Buddy(id = "b1", species = "BUNNY", pos = Pos(2, 2), dir = Direction.RIGHT)
        val b2 = Buddy(id = "b2", species = "CHICK", pos = Pos(3, 2), dir = Direction.UP)
        var state = GameState.fromLevel(createEmptyLevel(buddies = listOf(b1, b2)))

        // Bonk 1
        state = GameEngine.tap(state, "b1").newState
        assertThat(state.hearts).isEqualTo(2)
        assertThat(state.status).isEqualTo(GameStatus.PLAYING)

        // Bonk 2
        state = GameEngine.tap(state, "b1").newState
        assertThat(state.hearts).isEqualTo(1)
        assertThat(state.status).isEqualTo(GameStatus.PLAYING)

        // Bonk 3 -> LOST
        state = GameEngine.tap(state, "b1").newState
        assertThat(state.hearts).isEqualTo(0)
        assertThat(state.status).isEqualTo(GameStatus.LOST)
        assertThat(state.mistakes).isEqualTo(3)
        assertThat(state.stars).isEqualTo(0)

        // Further taps are ignored
        val afterLostOutcome = GameEngine.tap(state, "b2")
        assertThat(afterLostOutcome.result).isEqualTo(MoveResult.Ignored)
    }

    @Test
    fun `winning unlocks correct stars based on mistakes`() {
        val b1 = Buddy(id = "b1", species = "BUNNY", pos = Pos(0, 0), dir = Direction.UP)
        val level = createEmptyLevel(buddies = listOf(b1))

        // 0 mistakes -> 3 stars
        val winState0 = GameEngine.tap(GameState.fromLevel(level), "b1").newState
        assertThat(winState0.status).isEqualTo(GameStatus.WON)
        assertThat(winState0.stars).isEqualTo(3)

        // 1 mistake -> 2 stars
        val state1 = GameState.fromLevel(level).copy(mistakes = 1)
        val winState1 = GameEngine.tap(state1, "b1").newState
        assertThat(winState1.stars).isEqualTo(2)

        // 2 mistakes -> 1 star
        val state2 = GameState.fromLevel(level).copy(mistakes = 2)
        val winState2 = GameEngine.tap(state2, "b1").newState
        assertThat(winState2.stars).isEqualTo(1)
    }

    // ==========================================
    // 5. Undo system
    // ==========================================

    @Test
    fun `undo restores board state but keeps current hearts and mistakes`() {
        val b1 = Buddy(id = "b1", species = "BUNNY", pos = Pos(0, 1), dir = Direction.LEFT)
        val b2 = Buddy(id = "b2", species = "CHICK", pos = Pos(2, 2), dir = Direction.RIGHT)
        val b3 = Buddy(id = "b3", species = "PIGLET", pos = Pos(3, 2), dir = Direction.UP)
        var state = GameState.fromLevel(createEmptyLevel(buddies = listOf(b1, b2, b3)))

        // Step 1: b1 escapes successfully (history recorded)
        state = GameEngine.tap(state, "b1").newState
        assertThat(state.rescuedIds).contains("b1")
        assertThat(state.history).hasSize(1)

        // Step 2: b2 bonks into b3 (costs a heart, no history entry added)
        state = GameEngine.tap(state, "b2").newState
        assertThat(state.hearts).isEqualTo(2)
        assertThat(state.mistakes).isEqualTo(1)
        assertThat(state.history).hasSize(1)

        // Step 3: Undo successful move b1
        val undoneState = GameEngine.undo(state)
        assertThat(undoneState).isNotNull()
        assertThat(undoneState!!.buddies).containsExactly(b1, b2, b3)
        assertThat(undoneState.rescuedIds).isEmpty()
        assertThat(undoneState.hearts).isEqualTo(2) // Hearts preserved!
        assertThat(undoneState.mistakes).isEqualTo(1) // Mistakes preserved!
        assertThat(undoneState.history).isEmpty()

        // Calling undo on empty history returns null
        assertThat(GameEngine.undo(undoneState)).isNull()
    }

    // ==========================================
    // 6. Safe Moves / Hint API
    // ==========================================

    @Test
    fun `safeMoves returns only buddies that can move without bonking`() {
        val bBlocked = Buddy(id = "b1", species = "BUNNY", pos = Pos(2, 2), dir = Direction.RIGHT)
        val bBlocker = Buddy(id = "b2", species = "CHICK", pos = Pos(3, 2), dir = Direction.DOWN)
        val state = GameState.fromLevel(createEmptyLevel(buddies = listOf(bBlocked, bBlocker)))

        val safe = GameEngine.safeMoves(state)
        assertThat(safe).containsExactly(bBlocker)
    }

    // ==========================================
    // 7. Reserved mechanics (Mud, Turntable, Loop, Caged, Sleepy)
    // ==========================================

    @Test
    fun `mud stops buddy safely as valid move`() {
        val mudPos = Pos(2, 1)
        val buddy = Buddy(id = "b1", species = "PUPPY", pos = Pos(2, 3), dir = Direction.UP)
        val state = GameState.fromLevel(
            createEmptyLevel(
                tiles = mapOf(mudPos to Tile.Mud),
                buddies = listOf(buddy)
            )
        )

        val outcome = GameEngine.tap(state, "b1")
        assertThat(outcome.result).isInstanceOf(MoveResult.Stopped::class.java)
        val stopped = outcome.result as MoveResult.Stopped
        assertThat(stopped.path.last().event).isEqualTo(StepEvent.STOP)
        assertThat(outcome.newState.hearts).isEqualTo(3) // No heart lost!
        assertThat(outcome.newState.buddyAt(mudPos)).isNotNull()
    }

    @Test
    fun `turntable turns buddy 90 degrees clockwise`() {
        val turnPos = Pos(2, 2)
        val buddy = Buddy(id = "b1", species = "MONKEY", pos = Pos(0, 2), dir = Direction.RIGHT)
        val state = GameState.fromLevel(
            createEmptyLevel(
                tiles = mapOf(turnPos to Tile.Turntable),
                buddies = listOf(buddy)
            )
        )

        val outcome = GameEngine.tap(state, "b1")
        val escaped = outcome.result as MoveResult.Escaped
        // Entered facing RIGHT, turned to DOWN at (2,2), exited DOWN
        assertThat(escaped.exitSide).isEqualTo(Direction.DOWN)
    }

    @Test
    fun `turntable loop triggers loop guard bonk`() {
        // 4 turntables arranged in a ring creating an infinite loop
        val tiles = mapOf(
            Pos(1, 1) to Tile.Turntable,
            Pos(2, 1) to Tile.Turntable,
            Pos(2, 2) to Tile.Turntable,
            Pos(1, 2) to Tile.Turntable
        )
        val buddy = Buddy(id = "b1", species = "MONKEY", pos = Pos(0, 1), dir = Direction.RIGHT)
        val state = GameState.fromLevel(createEmptyLevel(tiles = tiles, buddies = listOf(buddy)))

        val outcome = GameEngine.tap(state, "b1")
        assertThat(outcome.result).isInstanceOf(MoveResult.Bonked::class.java)
        val bonked = outcome.result as MoveResult.Bonked
        assertThat(bonked.blockerKind).isEqualTo(BlockerKind.LOOP)
    }

    @Test
    fun `portal pair teleports buddy`() {
        val portal1 = Pos(1, 2)
        val portal2 = Pos(3, 2)
        val tiles = mapOf(
            portal1 to Tile.Portal(pairId = 1),
            portal2 to Tile.Portal(pairId = 1)
        )
        val buddy = Buddy(id = "b1", species = "TURTLE", pos = Pos(0, 2), dir = Direction.RIGHT)
        val state = GameState.fromLevel(createEmptyLevel(tiles = tiles, buddies = listOf(buddy)))

        val outcome = GameEngine.tap(state, "b1")
        assertThat(outcome.result).isInstanceOf(MoveResult.Escaped::class.java)
        val escaped = outcome.result as MoveResult.Escaped
        assertThat(escaped.path.any { it.event == StepEvent.TELEPORT }).isTrue()
    }

    @Test
    fun `caged buddy refuses to move without losing a heart`() {
        val buddy = Buddy(id = "b1", species = "PENGUIN", pos = Pos(1, 1), dir = Direction.UP, cageColor = "BLUE")
        val state = GameState.fromLevel(createEmptyLevel(buddies = listOf(buddy)))

        val outcome = GameEngine.tap(state, "b1")
        assertThat(outcome.result).isEqualTo(MoveResult.Refused(RefusalReason.CAGED))
        assertThat(outcome.newState.hearts).isEqualTo(3)
    }

    @Test
    fun `sleepy buddy refuses to move without losing a heart`() {
        val buddy = Buddy(id = "b1", species = "OWL", pos = Pos(1, 1), dir = Direction.UP, sleepy = true)
        val state = GameState.fromLevel(createEmptyLevel(buddies = listOf(buddy)))

        val outcome = GameEngine.tap(state, "b1")
        assertThat(outcome.result).isEqualTo(MoveResult.Refused(RefusalReason.SLEEPY))
        assertThat(outcome.newState.hearts).isEqualTo(3)
    }

    @Test
    fun `tap with unknown buddy ID is ignored`() {
        val state = GameState.fromLevel(createEmptyLevel())
        val outcome = GameEngine.tap(state, "non_existent")
        assertThat(outcome.result).isEqualTo(MoveResult.Ignored)
    }

    // ==========================================
    // 8. LevelCodec: Round-trip & Validation
    // ==========================================

    @Test
    fun `LevelCodec round-trip encodes and decodes level identically`() {
        val jsonInput = """
            {
              "id": "w1-l01",
              "world": 1,
              "index": 1,
              "cols": 5,
              "rows": 6,
              "grid": [
                ".....",
                "..R..",
                ".....",
                ".....",
                ".....",
                "....."
              ],
              "buddies": [
                {
                  "x": 1,
                  "y": 2,
                  "d": "U",
                  "s": "BUNNY"
                }
              ]
            }
        """.trimIndent()

        val decoded = LevelCodec.decode(jsonInput)
        assertThat(decoded.id).isEqualTo("w1-l01")
        assertThat(decoded.cols).isEqualTo(5)
        assertThat(decoded.rows).isEqualTo(6)
        assertThat(decoded.tileAt(Pos(2, 1))).isEqualTo(Tile.Rock)
        assertThat(decoded.buddies).hasSize(1)
        assertThat(decoded.buddies.first().species).isEqualTo("BUNNY")
        assertThat(decoded.buddies.first().pos).isEqualTo(Pos(1, 2))
        assertThat(decoded.buddies.first().dir).isEqualTo(Direction.UP)

        val reEncoded = LevelCodec.encode(decoded)
        val reDecoded = LevelCodec.decode(reEncoded)
        assertThat(reDecoded).isEqualTo(decoded)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `LevelCodec rejects buddy placed on a rock`() {
        val json = """
            {
              "id": "bad-rock",
              "world": 1,
              "index": 1,
              "cols": 3,
              "rows": 3,
              "grid": [
                "...",
                ".R.",
                "..."
              ],
              "buddies": [
                { "x": 1, "y": 1, "d": "U", "s": "BUNNY" }
              ]
            }
        """.trimIndent()
        LevelCodec.decode(json)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `LevelCodec rejects buddy out of bounds`() {
        val json = """
            {
              "id": "bad-bounds",
              "world": 1,
              "index": 1,
              "cols": 3,
              "rows": 3,
              "grid": [
                "...",
                "...",
                "..."
              ],
              "buddies": [
                { "x": 5, "y": 1, "d": "U", "s": "BUNNY" }
              ]
            }
        """.trimIndent()
        LevelCodec.decode(json)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `LevelCodec rejects multiple buddies on same cell`() {
        val json = """
            {
              "id": "duplicate-cells",
              "world": 1,
              "index": 1,
              "cols": 3,
              "rows": 3,
              "grid": [
                "...",
                "...",
                "..."
              ],
              "buddies": [
                { "x": 1, "y": 1, "d": "U", "s": "BUNNY" },
                { "x": 1, "y": 1, "d": "D", "s": "CHICK" }
              ]
            }
        """.trimIndent()
        LevelCodec.decode(json)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `LevelCodec rejects invalid grid row length`() {
        val json = """
            {
              "id": "bad-row-len",
              "world": 1,
              "index": 1,
              "cols": 3,
              "rows": 2,
              "grid": [
                "....",
                "..."
              ],
              "buddies": []
            }
        """.trimIndent()
        LevelCodec.decode(json)
    }
}
