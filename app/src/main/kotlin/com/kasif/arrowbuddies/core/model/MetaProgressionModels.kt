package com.kasif.arrowbuddies.core.model

import androidx.annotation.StringRes
import com.kasif.arrowbuddies.R

enum class FriendshipTier {
    LOCKED,
    BRONZE,
    SILVER,
    GOLD;

    val requiredRescues: Int
        get() = when (this) {
            LOCKED -> 0
            BRONZE -> 10
            SILVER -> 50
            GOLD -> 150
        }
}

enum class BuddyAccessory {
    NONE,
    BOW,
    SCARF,
    CROWN;

    val unlockedAtTier: FriendshipTier
        get() = when (this) {
            NONE -> FriendshipTier.LOCKED
            BOW -> FriendshipTier.BRONZE
            SCARF -> FriendshipTier.SILVER
            CROWN -> FriendshipTier.GOLD
        }
}

enum class AchievementType(
    val id: String,
    @StringRes val titleRes: Int,
    @StringRes val descRes: Int,
    val maxProgress: Int,
    val iconEmoji: String
) {
    FIRST_RESCUE("first_rescue", R.string.achievement_first_rescue_title, R.string.achievement_first_rescue_desc, 1, "🎉"),
    RESCUED_10("rescued_10", R.string.achievement_rescued_10_title, R.string.achievement_rescued_10_desc, 10, "🐾"),
    RESCUED_100("rescued_100", R.string.achievement_rescued_100_title, R.string.achievement_rescued_100_desc, 100, "⭐"),
    RESCUED_500("rescued_500", R.string.achievement_rescued_500_title, R.string.achievement_rescued_500_desc, 500, "🌟"),
    RESCUED_1000("rescued_1000", R.string.achievement_rescued_1000_title, R.string.achievement_rescued_1000_desc, 1000, "👑"),
    PERFECT_WORLD("perfect_world", R.string.achievement_perfect_world_title, R.string.achievement_perfect_world_desc, 1, "🏆"),
    MUD_MASTER("mud_master", R.string.achievement_mud_master_title, R.string.achievement_mud_master_desc, 10, "💧"),
    PORTAL_PRO("portal_pro", R.string.achievement_portal_pro_title, R.string.achievement_portal_pro_desc, 20, "🌀"),
    STREAK_7("streak_7", R.string.achievement_streak_7_title, R.string.achievement_streak_7_desc, 7, "🔥"),
    COLLECTOR("collector", R.string.achievement_collector_title, R.string.achievement_collector_desc, 24, "📖"),
    DECORATOR("decorator", R.string.achievement_decorator_title, R.string.achievement_decorator_desc, 8, "🏡"),
    ZERO_BONK_BOSS("zero_bonk_boss", R.string.achievement_zero_bonk_boss_title, R.string.achievement_zero_bonk_boss_desc, 1, "✨");

    companion object {
        fun fromId(id: String): AchievementType? = entries.firstOrNull { it.id == id }
    }
}

