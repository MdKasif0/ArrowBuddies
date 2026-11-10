package com.kasif.arrowbuddies.core.ads

import android.app.Activity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

/**
 * In-memory test double and preview stub for [AdsManager].
 */
class FakeAdsManager(
    initialState: AdState = AdState.Ready,
    override var isAdsEnabled: Boolean = true
) : AdsManager {

    private val _adState = MutableStateFlow(initialState)
    override val adState: StateFlow<AdState> = _adState.asStateFlow()

    private val _isPrivacyOptionsRequired = MutableStateFlow(false)
    override val isPrivacyOptionsRequired: StateFlow<Boolean> = _isPrivacyOptionsRequired.asStateFlow()

    val showCount = AtomicInteger(0)
    val rewardGrantedCount = AtomicInteger(0)
    var shouldFailToShow: Boolean = false
    var failureMessage: String = "Simulated ad display failure"

    fun setAdState(state: AdState) {
        _adState.value = state
    }

    fun setPrivacyOptionsRequired(required: Boolean) {
        _isPrivacyOptionsRequired.value = required
    }

    override fun initialize(activity: Activity) {
        // No-op in test/preview stub
    }

    override fun loadRewardedAd() {
        if (_adState.value is AdState.Unavailable && isAdsEnabled) {
            _adState.value = AdState.Ready
        }
    }

    override fun showRewardedAd(
        activity: Activity,
        placement: AdPlacement,
        onUserEarnedReward: () -> Unit,
        onAdDismissed: () -> Unit,
        onAdFailedToShow: (String) -> Unit
    ) {
        if (!isAdsEnabled || shouldFailToShow || _adState.value != AdState.Ready) {
            onAdFailedToShow(failureMessage)
            return
        }

        showCount.incrementAndGet()

        // Idempotent reward simulation: ensure onUserEarnedReward is only called once
        val rewarded = AtomicBoolean(false)
        if (rewarded.compareAndSet(false, true)) {
            rewardGrantedCount.incrementAndGet()
            onUserEarnedReward()
        }

        onAdDismissed()
    }

    override fun showPrivacyOptionsForm(activity: Activity, onDismissed: () -> Unit) {
        onDismissed()
    }
}

