package com.kasif.arrowbuddies.core.ads

import android.app.Activity
import kotlinx.coroutines.flow.StateFlow

/**
 * Ad placements in ArrowBuddies.
 * Player always explicitly taps to choose; ads are NEVER shown automatically.
 */
enum class AdPlacement {
    /**
     * Fail sheet: Watch a short video to revive with +1 heart and continue on the current board.
     */
    FAIL_REVIVE,

    /**
     * Win sheet: Watch a short video to double the berries earned in the cleared level (once per level).
     */
    WIN_DOUBLE_BERRIES,

    /**
     * Hint button when hints = 0: Watch a short video for +1 free hint (max 3/day).
     */
    FREE_HINT
}

/**
 * Observable lifecycle state of the preloaded rewarded ad.
 */
sealed interface AdState {
    /** Ad is currently being loaded or retried with backoff. */
    data object Loading : AdState

    /** Ad is preloaded and ready to be shown immediately. */
    data object Ready : AdState

    /** Ad is unavailable (offline, no inventory, retries exhausted, or ads disabled). */
    data object Unavailable : AdState
}

/**
 * Child-safe, COPPA-compliant Ads Manager interface for Google AdMob rewarded ads.
 */
interface AdsManager {
    /**
     * Real-time state of the preloaded rewarded ad.
     */
    val adState: StateFlow<AdState>

    /**
     * True if European/GDPR Privacy Options consent form is required to be accessible in Settings.
     */
    val isPrivacyOptionsRequired: StateFlow<Boolean>

    /**
     * Whether ads are globally enabled (BuildConfig flag + DataStore override).
     */
    val isAdsEnabled: Boolean

    /**
     * Initialize UMP consent flow and MobileAds SDK on app startup.
     */
    fun initialize(activity: Activity)

    /**
     * Preload or reload a rewarded ad.
     */
    fun loadRewardedAd()

    /**
     * Show a rewarded ad for the specified [placement].
     * Pauses all game audio while playing, grants reward idempotently, and preloads the next ad.
     */
    fun showRewardedAd(
        activity: Activity,
        placement: AdPlacement,
        onUserEarnedReward: () -> Unit,
        onAdDismissed: () -> Unit = {},
        onAdFailedToShow: (String) -> Unit = {}
    )

    /**
     * Reopen the UMP privacy options consent form from Settings.
     */
    fun showPrivacyOptionsForm(activity: Activity, onDismissed: () -> Unit = {})
}

