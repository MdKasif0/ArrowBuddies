package com.kasif.arrowbuddies.engine.difficulty

/**
 * Tunable weights for calculating level difficulty scores.
 */
data class DifficultyWeights(
    val buddyCountWeight: Double = 1.0,
    val dependencyDepthWeight: Double = 2.5,
    val deadEndMovesWeight: Double = 3.0,
    val mechanicCountWeight: Double = 1.5,
    val initialFreeMovesWeight: Double = -1.2
)

/**
 * Single source of truth for difficulty calculation in ArrowBuddies.
 * Formula:
 * score = 1.0*buddyCount + 2.5*dependencyDepth + 3.0*deadEndMoves + 1.5*mechanicCount - 1.2*initialFreeMoves
 * Clamped to 0..100.
 */
object DifficultyCalculator {

    var defaultWeights: DifficultyWeights = DifficultyWeights()

    fun calculate(
        buddyCount: Int,
        dependencyDepth: Int,
        deadEndMoves: Int,
        mechanicCount: Int,
        initialFreeMoves: Int,
        weights: DifficultyWeights = defaultWeights
    ): Double {
        val raw = (weights.buddyCountWeight * buddyCount) +
                (weights.dependencyDepthWeight * dependencyDepth) +
                (weights.deadEndMovesWeight * deadEndMoves) +
                (weights.mechanicCountWeight * mechanicCount) +
                (weights.initialFreeMovesWeight * initialFreeMoves)

        return raw.coerceIn(0.0, 100.0)
    }
}
