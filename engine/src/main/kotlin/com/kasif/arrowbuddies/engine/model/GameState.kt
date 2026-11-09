package com.kasif.arrowbuddies.engine.model

import kotlinx.serialization.Serializable

/**
 * Game session lifecycle status.
 */
@Serializable
enum class GameStatus {
    PLAYING,
    WON,
    LOST
}

/**
 * Complete immutable snapshot of an active puzzle session.
 *
 * @param level The immutable [LevelDef] being played.
 * @param buddies Current active buddies remaining on the board.
 * @param rescuedIds Set of unique buddy IDs that have successfully escaped.
 * @param hearts Remaining heart lives (starts at 3, decrements on bonk, 0 = Lost).
 * @param mistakes Count of bonks incurred (used for star calculation).
 * @param moveCount Total count of moves attempted.
 * @param status Current game lifecycle status [GameStatus].
 * @param history Stack of previous valid states for successful move undoing.
 */
@Serializable
data class GameState(
    val level: LevelDef,
    val buddies: List<Buddy>,
    val rescuedIds: Set<String> = emptySet(),
    val hearts: Int = 3,
    val mistakes: Int = 0,
    val moveCount: Int = 0,
    val status: GameStatus = GameStatus.PLAYING,
    val history: List<GameState> = emptyList()
) {
    /**
     * Star rating awarded upon level clearance:
     * - 3 stars = 0 mistakes (flawless)
     * - 2 stars = 1 mistake
     * - 1 star = 2 mistakes (or completed with 1 heart left)
     * - 0 stars = level failed or still in progress.
     */
    val stars: Int
        get() = when {
            status != GameStatus.WON -> 0
            mistakes == 0 -> 3
            mistakes == 1 -> 2
            else -> 1
        }

    /**
     * Locates a buddy occupying [pos] on the board, if any.
     */
    fun buddyAt(pos: Pos): Buddy? = buddies.firstOrNull { it.pos == pos }

    /**
     * Locates a buddy by its [id].
     */
    fun findBuddy(id: String): Buddy? = buddies.firstOrNull { it.id == id }

    /**
     * Total initial buddy count in the level.
     */
    val initialBuddyCount: Int
        get() = level.buddies.size

    /**
     * Number of rescued buddies so far.
     */
    val rescuedCount: Int
        get() = rescuedIds.size

    companion object {
        /**
         * Creates a fresh initial game state from a [LevelDef].
         */
        fun fromLevel(level: LevelDef): GameState = GameState(
            level = level,
            buddies = level.buddies,
            rescuedIds = emptySet(),
            hearts = 3,
            mistakes = 0,
            moveCount = 0,
            status = GameStatus.PLAYING,
            history = emptyList()
        )
    }
}

