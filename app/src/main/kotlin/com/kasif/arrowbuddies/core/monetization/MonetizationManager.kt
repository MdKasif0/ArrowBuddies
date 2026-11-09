package com.kasif.arrowbuddies.core.monetization

import android.app.Activity
import android.util.Log
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Child-Safe Monetization & Rewarded Ad Integration Hooks.
 *
 * GOOGLE PLAY FAMILIES POLICY & DESIGN COMMITMENTS:
 * 1. Disabled by default: ArrowBuddies is 100% playable, enjoyable, and beatable completely offline
 *    with ZERO mandatory purchases or mandatory interstitials.
 * 2. No Predatory Mechanics: No loot boxes, no energy timers, no paywalls, and no countdown purchase pressure.
 * 3. Child-Directed Ad Guidelines: If ads are enabled in the future, only Google Play Families Self-Certified
 *    Ad Networks may be used, with `tagForChildDirectedTreatment = TRUE`.
 * 4. Supporter Pack: Optional, single-time tip-jar purchase that unlocks cosmetic golden crowns.
 */
interface MonetizationManager {
    val isMonetizationEnabled: Boolean
    val isRewardedReviveAvailable: Boolean
    val isRewardedHintAvailable: Boolean
    val isSupporterPackAvailable: Boolean

    fun showRewardedRevive(
        activity: Activity,
        onRewarded: () -> Unit,
        onFailed: (String) -> Unit
    )

    fun showRewardedHint(
        activity: Activity,
        onRewarded: () -> Unit,
        onFailed: (String) -> Unit
    )

    fun launchSupporterPackPurchase(
        activity: Activity,
        onSuccess: () -> Unit,
        onCancelled: () -> Unit
    )
}

@Singleton
class DefaultMonetizationManager @Inject constructor() : MonetizationManager {

    private val tag = "MonetizationManager"

    /**
     * Master monetization flag. Defaults to false.
     */
    override val isMonetizationEnabled: Boolean = false

    override val isRewardedReviveAvailable: Boolean
        get() = isMonetizationEnabled

    override val isRewardedHintAvailable: Boolean
        get() = isMonetizationEnabled

    override val isSupporterPackAvailable: Boolean
        get() = isMonetizationEnabled

    override fun showRewardedRevive(
        activity: Activity,
        onRewarded: () -> Unit,
        onFailed: (String) -> Unit
    ) {
        if (!isMonetizationEnabled) {
            Log.d(tag, "Rewarded Revive requested while monetization is disabled. Granting reward directly.")
            onRewarded()
            return
        }
        // When Google Play Families SDK is initialized:
        // GoogleMobileAds.showRewardedAd(activity, AD_UNIT_REVIVE, onRewarded, onFailed)
    }

    override fun showRewardedHint(
        activity: Activity,
        onRewarded: () -> Unit,
        onFailed: (String) -> Unit
    ) {
        if (!isMonetizationEnabled) {
            Log.d(tag, "Rewarded Hint requested while monetization is disabled. Granting reward directly.")
            onRewarded()
            return
        }
        // When Google Play Families SDK is initialized:
        // GoogleMobileAds.showRewardedAd(activity, AD_UNIT_HINT, onRewarded, onFailed)
    }

    override fun launchSupporterPackPurchase(
        activity: Activity,
        onSuccess: () -> Unit,
        onCancelled: () -> Unit
    ) {
        if (!isMonetizationEnabled) {
            Log.d(tag, "Supporter Pack purchase requested while Play Billing is disabled.")
            onCancelled()
            return
        }
        // When Google Play Billing Client is connected:
        // billingClient.launchBillingFlow(activity, supporterPackBillingParams)
    }
}

