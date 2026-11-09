package com.kasif.arrowbuddies.engine.solver

import kotlinx.serialization.Serializable

/**
 * Detailed metrics computed by [LevelSolver].
 */
@Serializable
data class LevelMetrics(
    val buddyCount: Int,
    val initialFreeMoves: Int,
    val dependencyDepth: Int,
    val deadEndMoves: Int,
    val mechanicsUsed: Set<String>,
    val difficultyScore: Double
)

/**
 * Result of solving an ArrowBuddies puzzle level.
 *
 * @param solvable True if a valid non-bonk sequence of moves rescues all buddies.
 * @param shortestSolution The shortest sequence of buddy IDs tapped to win.
 * @param solutionCount Total distinct winning solutions found (capped at 50).
 * @param metrics Difficulty and structure metrics.
 * @param timeMs Elapsed execution time in milliseconds.
 * @param exploredNodes Total state nodes visited in the search graph.
 */
@Serializable
data class SolveResult(
    val solvable: Boolean,
    val shortestSolution: List<String>,
    val solutionCount: Int,
    val metrics: LevelMetrics,
    val timeMs: Long,
    val exploredNodes: Int
)

