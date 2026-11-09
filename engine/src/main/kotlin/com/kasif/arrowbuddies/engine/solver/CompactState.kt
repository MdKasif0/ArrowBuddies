package com.kasif.arrowbuddies.engine.solver

import com.kasif.arrowbuddies.engine.model.GameState

/**
 * Compact immutable representation of a buddy's dynamic state on the board.
 */
internal data class CompactBuddy(
    val id: String,
    val x: Byte,
    val y: Byte,
    val dir: Byte,
    val cageColor: String?,
    val sleepy: Boolean
) : Comparable<CompactBuddy> {
    override fun compareTo(other: CompactBuddy): Int = id.compareTo(other.id)
}

/**
 * Compact state key used for memoization and visited-state tracking in [LevelSolver].
 */
internal data class CompactStateKey(
    val buddies: List<CompactBuddy>
)

internal fun GameState.toCompactKey(): CompactStateKey {
    val list = this.buddies.map { b ->
        CompactBuddy(
            id = b.id,
            x = b.pos.x.toByte(),
            y = b.pos.y.toByte(),
            dir = b.dir.ordinal.toByte(),
            cageColor = b.cageColor,
            sleepy = b.sleepy
        )
    }.sorted()
    return CompactStateKey(list)
}

