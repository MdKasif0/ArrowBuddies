package com.kasif.arrowbuddies.engine.model

import kotlinx.serialization.Serializable

/**
 * Visual or mechanics event that occurred at a specific path step.
 */
@Serializable
enum class StepEvent {
    NONE,
    TURN,      // Rotated 90 degrees (e.g. turntable)
    TELEPORT,  // Teleported through a portal
    STOP       // Came to rest on a terrain obstacle (e.g. mud)
}

/**
 * An individual cell step along a buddy's dashed path.
 * Used by UI animations to execute smooth multi-segment paths and rotations.
 *
 * @param pos Coordinate of the cell entered.
 * @param dirAfter Buddy facing direction after entering this cell.
 * @param event Event triggered upon entering this cell.
 */
@Serializable
data class Step(
    val pos: Pos,
    val dirAfter: Direction,
    val event: StepEvent = StepEvent.NONE
)

/**
 * Obstacle type that halted a buddy and triggered a bonk.
 */
@Serializable
enum class BlockerKind {
    BUDDY,     // Collided with another buddy
    ROCK,      // Collided with static rock obstacle
    LOOP,      // Infinite loop guard triggered
    OBSTACLE   // Generic blocker
}

/**
 * Reason a tapped buddy refused to move.
 */
@Serializable
enum class RefusalReason {
    CAGED,     // Buddy is locked in a cage
    SLEEPY     // Buddy is asleep (Zzz)
}

/**
 * Result of a tap action on a buddy.
 */
@Serializable
sealed interface MoveResult {
    /**
     * The buddy had a clear path to the board edge, exited, and was rescued.
     *
     * @param path The cell-by-cell path traversed from start to exit edge.
     * @param exitSide The outer boundary side through which the buddy departed.
     */
    @Serializable
    data class Escaped(
        val path: List<Step>,
        val exitSide: Direction
    ) : MoveResult

    /**
     * The buddy collided with another buddy, rock, or infinite loop.
     * Bonks, squashes, bounces back to start cell, and costs 1 heart.
     *
     * @param path The path travelled up to the blocker.
     * @param blockerPos The coordinate of the obstacle cell.
     * @param blockerKind The category of blocker encountered.
     */
    @Serializable
    data class Bonked(
        val path: List<Step>,
        val blockerPos: Pos,
        val blockerKind: BlockerKind
    ) : MoveResult

    /**
     * Reserved (World 2): The buddy entered mud and safely stopped on it.
     * Valid move, no heart lost.
     */
    @Serializable
    data class Stopped(
        val path: List<Step>
    ) : MoveResult

    /**
     * The buddy is immobilized and gently shakes in place without losing a heart.
     */
    @Serializable
    data class Refused(
        val reason: RefusalReason
    ) : MoveResult

    /**
     * Tap was ignored (level already Won or Lost, or buddy ID not found on board).
     */
    @Serializable
    data object Ignored : MoveResult
}

/**
 * Comprehensive outcome of invoking [com.kasif.arrowbuddies.engine.GameEngine.tap].
 *
 * @param newState The resulting updated [GameState].
 * @param result The specific [MoveResult] detailing path and consequences.
 */
@Serializable
data class MoveOutcome(
    val newState: GameState,
    val result: MoveResult
)

