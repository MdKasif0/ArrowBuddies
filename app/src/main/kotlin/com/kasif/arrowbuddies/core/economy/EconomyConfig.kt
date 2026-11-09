package com.kasif.arrowbuddies.core.economy

/**
 * Single source of truth for all economic balances and thresholds in ArrowBuddies.
 */
object EconomyConfig {
    // Earning
    const val BERRIES_LEVEL_COMPLETION = 10
    const val BERRIES_PER_STAR = 5
    const val BERRIES_GOLDEN_BUDDY_BONUS = 25
    const val BERRIES_DAILY_RESCUE = 50
    const val BERRIES_TREAT_TIME = 20

    // 7-day streak calendar rewards: (20, 30, 40, 50, 60, 80, 150 + decor on day 7)
    val STREAK_REWARDS = listOf(20, 30, 40, 50, 60, 80, 150)
    const val DAY_7_STREAK_DECOR_ID = "decor_rangoli_mat_8"

    // Spending
    const val COST_HINT = 30
    const val COST_UNDO = 20
    const val COST_EXTRA_HEART = 40

    // Friendship Tiers (species rescue thresholds)
    const val TIER_BRONZE_THRESHOLD = 10
    const val TIER_SILVER_THRESHOLD = 50
    const val TIER_GOLD_THRESHOLD = 150

    // Starting Profile Balance
    const val INITIAL_BERRIES = 100
    const val INITIAL_HINTS = 3
    const val INITIAL_UNDOS = 3
}

