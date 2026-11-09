package com.kasif.arrowbuddies.feature.game

import com.kasif.arrowbuddies.engine.model.BlockerKind
import com.kasif.arrowbuddies.engine.model.Buddy
import com.kasif.arrowbuddies.engine.model.Direction
import com.kasif.arrowbuddies.engine.model.Pos
import com.kasif.arrowbuddies.engine.model.Step

enum class HapticType {
    TICK,
    CONFIRM,
    REJECT
}

enum class SoundType {
    TAP,
    ESCAPE,
    BONK,
    WIN,
    LOSE
}

/**
 * One-shot UI effects emitted by [GameViewModel].
 */
sealed interface GameEffect {
    data class PlayEscape(
        val buddyId: String,
        val path: List<Step>,
        val exitSide: Direction,
        val buddy: Buddy
    ) : GameEffect

    data class PlayBonk(
        val buddyId: String,
        val path: List<Step>,
        val blockerPos: Pos,
        val blockerKind: BlockerKind,
        val buddy: Buddy
    ) : GameEffect

    data class ScreenShake(
        val intensityDp: Float = 4f,
        val durationMs: Long = 180L
    ) : GameEffect

    data class TriggerHaptic(
        val type: HapticType
    ) : GameEffect

    data class PlaySound(
        val type: SoundType
    ) : GameEffect
}

