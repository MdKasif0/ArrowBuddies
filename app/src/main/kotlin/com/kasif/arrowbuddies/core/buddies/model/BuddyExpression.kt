package com.kasif.arrowbuddies.core.buddies.model

import androidx.annotation.StringRes
import com.kasif.arrowbuddies.R

/**
 * Expressive facial and emotional states for game buddies.
 */
enum class BuddyExpression(@StringRes val labelRes: Int) {
    /**
     * Resting idle state: gentle breathing, sweet smile, periodic blinks.
     */
    IDLE(R.string.expression_idle),

    /**
     * Trapped state: raised wavy eyebrows, nervous sweat bead on brow.
     */
    WORRIED(R.string.expression_worried),

    /**
     * Tapped / selected state: wide determined eyes, confident tilted brows.
     */
    READY(R.string.expression_ready),

    /**
     * Dash movement: motion squash along travel axis, speed lines.
     */
    DASHING(R.string.expression_dashing),

    /**
     * Collision bonk: cartoon spiral swirly eyes, wavy mouth, orbiting stars.
     */
    BONKED(R.string.expression_bonked),

    /**
     * Escaped / Victory state: curved arc eyes (^_^), joyful smile, floating hearts.
     */
    HAPPY(R.string.expression_happy),

    /**
     * Snoozing state (World 6 mechanic): closed sleeping eyes, floating Zzz.
     */
    SLEEPY(R.string.expression_sleepy),

    /**
     * Trapped in cage (World 4 mechanic): sad glossy eyes, tear shimmer.
     */
    CAGED(R.string.expression_caged);
}

