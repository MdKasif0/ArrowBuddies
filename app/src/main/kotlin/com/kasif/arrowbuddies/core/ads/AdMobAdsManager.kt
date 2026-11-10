package com.kasif.arrowbuddies.core.ads

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import com.kasif.arrowbuddies.BuildConfig
import com.kasif.arrowbuddies.core.audio.SoundManager
import com.kasif.arrowbuddies.data.datastore.GamePreferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdMobAdsManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val soundManager: SoundManager,
    private val preferences: GamePreferencesDataStore
) : AdsManager {

    private val tag = "AdMobAdsManager"
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val mainHandler = Handler(Looper.getMainLooper())

    private val _adState = MutableStateFlow<AdState>(AdState.Loading)
    override val adState: StateFlow<AdState> = _adState.asStateFlow()

    private val _isPrivacyOptionsRequired = MutableStateFlow(false)
    override val isPrivacyOptionsRequired: StateFlow<Boolean> = _isPrivacyOptionsRequired.asStateFlow()

    private var adsEnabledOverride: Boolean = true
    override val isAdsEnabled: Boolean
        get() = BuildConfig.ADS_ENABLED && adsEnabledOverride

    private var preloadedRewardedAd: RewardedAd? = null
    private var isAdLoading = false
    private var isSdkInitialized = false
    private var retryAttempt = 0
    private val maxRetries = 3

    init {
        scope.launch {
            preferences.adsEnabled.collect { enabled ->
                adsEnabledOverride = enabled
                if (!enabled) {
                    _adState.value = AdState.Unavailable
                } else if (_adState.value == AdState.Unavailable && isSdkInitialized) {
                    loadRewardedAd()
                }
            }
        }
    }

    override fun initialize(activity: Activity) {
        if (!isAdsEnabled) {
            Log.d(tag, "Ads are globally disabled via BuildConfig or preferences.")
            _adState.value = AdState.Unavailable
            return
        }

        // Configure strict child-directed and Families Policy RequestConfiguration
        val requestConfiguration = MobileAds.getRequestConfiguration().toBuilder()
            .setTagForChildDirectedTreatment(RequestConfiguration.TAG_FOR_CHILD_DIRECTED_TREATMENT_TRUE)
            .setMaxAdContentRating(RequestConfiguration.MAX_AD_CONTENT_RATING_G)
            .setTagForUnderAgeOfConsent(RequestConfiguration.TAG_FOR_UNDER_AGE_OF_CONSENT_TRUE)
            .build()
        MobileAds.setRequestConfiguration(requestConfiguration)

        // 1. Google UMP Consent flow for EEA/UK regulations
        val params = ConsentRequestParameters.Builder()
            .setTagForUnderAgeOfConsent(true)
            .build()

        val consentInformation = UserMessagingPlatform.getConsentInformation(activity)
        consentInformation.requestConsentInfoUpdate(
            activity,
            params,
            {
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { formError ->
                    if (formError != null) {
                        Log.w(tag, "Consent form error: ${formError.message}")
                    }
                    _isPrivacyOptionsRequired.value =
                        consentInformation.privacyOptionsRequirementStatus == ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED

                    // 2. Initialize MobileAds off the main thread
                    initializeMobileAds()
                }
            },
            { requestConsentError ->
                Log.w(tag, "Consent info update failed: ${requestConsentError.message}")
                initializeMobileAds()
            }
        )
    }

    private fun initializeMobileAds() {
        if (isSdkInitialized) return
        scope.launch(Dispatchers.IO) {
            try {
                MobileAds.initialize(context) { initStatus ->
                    Log.d(tag, "MobileAds initialized: $initStatus")
                    isSdkInitialized = true
                    scope.launch(Dispatchers.Main) {
                        loadRewardedAd()
                    }
                }
            } catch (e: Exception) {
                Log.e(tag, "MobileAds initialization failed", e)
                scope.launch(Dispatchers.Main) {
                    _adState.value = AdState.Unavailable
                }
            }
        }
    }

    override fun loadRewardedAd() {
        if (!isAdsEnabled) {
            _adState.value = AdState.Unavailable
            return
        }
        if (!isSdkInitialized) {
            // SDK is still initializing asynchronously; load will trigger upon completion
            return
        }
        if (preloadedRewardedAd != null) {
            _adState.value = AdState.Ready
            return
        }
        if (isAdLoading) return

        isAdLoading = true
        _adState.value = AdState.Loading

        val adUnitId = BuildConfig.ADMOB_REWARDED_FAIL_ID
        val adRequest = AdRequest.Builder().build()

        RewardedAd.load(
            context,
            adUnitId,
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(rewardedAd: RewardedAd) {
                    Log.d(tag, "RewardedAd loaded successfully.")
                    preloadedRewardedAd = rewardedAd
                    isAdLoading = false
                    retryAttempt = 0
                    _adState.value = AdState.Ready
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    Log.w(tag, "RewardedAd failed to load: ${loadAdError.message} (code ${loadAdError.code})")
                    preloadedRewardedAd = null
                    isAdLoading = false

                    if (retryAttempt < maxRetries) {
                        val delayMillis = (1L shl retryAttempt) * 2000L // 2s, 4s, 8s
                        retryAttempt++
                        Log.d(tag, "Retrying ad load in ${delayMillis}ms (attempt $retryAttempt/$maxRetries)")
                        mainHandler.postDelayed({ loadRewardedAd() }, delayMillis)
                    } else {
                        Log.w(tag, "Max ad load retries ($maxRetries) reached. Marking ad as Unavailable.")
                        _adState.value = AdState.Unavailable
                    }
                }
            }
        )
    }

    override fun showRewardedAd(
        activity: Activity,
        placement: AdPlacement,
        onUserEarnedReward: () -> Unit,
        onAdDismissed: () -> Unit,
        onAdFailedToShow: (String) -> Unit
    ) {
        val ad = preloadedRewardedAd
        if (ad == null || _adState.value != AdState.Ready) {
            Log.w(tag, "Attempted to show ad for placement $placement, but ad is not ready.")
            onAdFailedToShow("Ad not ready")
            loadRewardedAd()
            return
        }

        // Idempotency guard: reward can be granted at most once
        val rewardGranted = AtomicBoolean(false)

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdShowedFullScreenContent() {
                Log.d(tag, "RewardedAd showed full screen content.")
                soundManager.pauseAll()
            }

            override fun onAdDismissedFullScreenContent() {
                Log.d(tag, "RewardedAd dismissed by user.")
                soundManager.resumeAll()
                preloadedRewardedAd = null
                _adState.value = AdState.Loading
                loadRewardedAd()
                onAdDismissed()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                Log.w(tag, "RewardedAd failed to show: ${adError.message}")
                soundManager.resumeAll()
                preloadedRewardedAd = null
                _adState.value = AdState.Unavailable
                loadRewardedAd()
                onAdFailedToShow(adError.message)
            }
        }

        try {
            ad.show(activity) { rewardItem ->
                Log.d(tag, "onUserEarnedReward earned: ${rewardItem.amount} ${rewardItem.type}")
                if (rewardGranted.compareAndSet(false, true)) {
                    onUserEarnedReward()
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "Exception during ad.show: ${e.message}", e)
            soundManager.resumeAll()
            preloadedRewardedAd = null
            _adState.value = AdState.Unavailable
            loadRewardedAd()
            onAdFailedToShow(e.message ?: "Failed to display ad")
        }
    }

    override fun showPrivacyOptionsForm(activity: Activity, onDismissed: () -> Unit) {
        UserMessagingPlatform.showPrivacyOptionsForm(activity) { formError ->
            if (formError != null) {
                Log.w(tag, "Privacy options form error: ${formError.message}")
            }
            onDismissed()
        }
    }
}
