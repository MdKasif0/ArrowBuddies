package com.kasif.arrowbuddies.feature.game

import com.kasif.arrowbuddies.engine.model.BlockerKind
import com.kasif.arrowbuddies.engine.model.Buddy
import com.kasif.arrowbuddies.engine.model.Direction
import com.kasif.arrowbuddies.engine.model.Pos
import com.kasif.arrowbuddies.engine.model.RefusalReason
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
    LOSE,
    SQUELCH,
    WHOOSH,
    KEY_UNLOCK,
    CAGE_LOCKED,
    SLEEPY_YAWN,
    WAKE_UP
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

    data class PlayMudStop(
        val buddyId: String,
        val path: List<Step>,
        val mudPos: Pos,
        val buddy: Buddy
    ) : GameEffect

    data class PlayRefusal(
        val buddyId: String,
        val reason: RefusalReason,
        val buddyPos: Pos
    ) : GameEffect

    data class PlayKeyUnlock(
        val keyColor: String,
        val fromPos: Pos,
        val targetPositions: List<Pos>
    ) : GameEffect

    data class PlayWakeUp(
        val buddyId: String,
        val buddyPos: Pos
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
