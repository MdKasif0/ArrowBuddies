# Rewarded Ads Architecture & Compliance Guide

ArrowBuddies implements child-safe, opt-in rewarded video ads strictly conforming to **Google Play's Families Policy**, **COPPA**, and global child privacy regulations (GDPR-K).

---

## 1. Core Principles

1. **Child-Safe & Family-Friendly**:
   - Every ad request is configured with `tagForChildDirectedTreatment = TRUE`, `maxAdContentRating = G`, and `tagForUnderAgeOfConsent = TRUE`.
   - Only Google AdMob and Google User Messaging Platform (UMP) SDKs are used. No third-party ad mediation or non-certified ad networks.
2. **100% Player Opt-in (Never Automatic)**:
   - Ads are NEVER shown automatically, on screen transitions, or after failures without explicit user interaction.
   - Absolutely **no interstitials** and **no banner ads** anywhere in the game.
3. **Tutorial Protection**:
   - Levels 1 through 6 are strictly designated as the tutorial experience. No ad buttons or ad placements ever appear during these levels (`LevelIdHelper.parseFlatIndex(levelId) <= 6`).
4. **Resilient & Non-Blocking**:
   - If no ad is ready, the device is offline, or inventory is unavailable, the game never blocks the player. The ad button is either hidden or cleanly disabled showing `"No video right now"`.
5. **Single Remote Kill-Switch**:
   - `BuildConfig.ADS_ENABLED` combined with DataStore preference override (`ADS_ENABLED_OVERRIDE`) enables instant global suppression of all ad UI.

---

## 2. Placements

| Placement | Trigger Location | Reward | Constraints |
| :--- | :--- | :--- | :--- |
| `FAIL_REVIVE` | Level Fail Sheet | Revive with +1 Heart | Resumes gameplay on the existing board state |
| `WIN_DOUBLE_BERRIES` | Level Win Celebration Sheet | Double earned berries (2x) | At most once per cleared level |
| `FREE_HINT` | Booster Shop (when hints = 0) | +1 Free Hint | Max 3 per local calendar day; resets at local midnight |

---

## 3. Architecture & Lifecycle

### Components
- `AdsManager` (`core/ads/AdsManager.kt`): Public interface exposing `adState: StateFlow<AdState>` (`Loading`, `Ready`, `Unavailable`), `isPrivacyOptionsRequired`, and `showRewardedAd(...)`.
- `AdMobAdsManager` (`core/ads/AdMobAdsManager.kt`): Production singleton implementation managing UMP consent, background `MobileAds` initialization, exponential backoff preloading, and audio lifecycle synchronization.
- `FakeAdsManager` (`core/ads/FakeAdsManager.kt`): Fast in-memory test double for unit tests, screenshot tests, and Compose previews without external network calls.

### Audio Lifecycle Integration
During rewarded ad playback, all game music and sound effects are automatically paused via `SoundManager.pauseAll()`. When the ad is dismissed or fails, game audio is safely resumed via `SoundManager.resumeAll()`.

### Reward Idempotency
Rewards are granted strictly inside `onUserEarnedReward` guarded by an atomic boolean compare-and-swap (`AtomicBoolean(false).compareAndSet(false, true)`). Even if SDK callbacks fire multiple times or duplicate events occur, rewards can never be double-credited.

---

## 4. Configuration & Keys

### Debug Builds
Debug builds utilize Google's official, certified public test credentials:
- **App ID**: `ca-app-pub-3940256099942544~3347511713`
- **Rewarded Ad Unit ID**: `ca-app-pub-3940256099942544/5224354917`

### Release Builds
Production IDs are loaded from a git-ignored `ads.properties` file in the project root:
```properties
ADMOB_APP_ID=ca-app-pub-XXXXXXXXXXXXXXXX~XXXXXXXXXX
ADMOB_REWARDED_FAIL_ID=ca-app-pub-XXXXXXXXXXXXXXXX/XXXXXXXXXX
ADMOB_REWARDED_WIN_ID=ca-app-pub-XXXXXXXXXXXXXXXX/XXXXXXXXXX
ADMOB_REWARDED_HINT_ID=ca-app-pub-XXXXXXXXXXXXXXXX/XXXXXXXXXX
ADS_ENABLED=true
```
*Note*: `app/build.gradle.kts` enforces that release builds will fail with a clear diagnostic message if `ads.properties` is missing or incomplete. An example template is provided in `ads.properties.example`.

### App-Ads.txt
A standard `app-ads.txt` template is provided at the repository root:
```text
google.com, pub-XXXXXXXXXXXXXXXX, DIRECT, f08c47fec0942fa0
```
This file must be deployed to the root of the official developer website listed on Google Play Console (e.g. `https://arrowbuddies.com/app-ads.txt`).

---

## 5. Privacy & Consent (UMP)

Under EEA/UK GDPR and applicable child privacy regulations:
1. `UserMessagingPlatform.getConsentInformation(activity)` requests consent updates with `setTagForUnderAgeOfConsent(true)`.
2. A direct **Ad Privacy Options** entry is provided inside the Settings screen (`SettingsScreen.kt`) allowing users to revisit or change consent settings at any time via `adsManager.showPrivacyOptionsForm(activity)`.

---

## 6. Verification Checklist

- [x] `tagForChildDirectedTreatment` set to `TRUE`
- [x] `maxAdContentRating` set to `G`
- [x] `tagForUnderAgeOfConsent` set to `TRUE`
- [x] No interstitials or banner ads in app
- [x] No ads shown in tutorial levels 1 through 6
- [x] Daily hint cap enforced at 3 with local midnight reset
- [x] Atomic idempotency: rewards can never be granted twice
- [x] Game audio pauses during ad and resumes on dismiss/fail
- [x] Ad buttons gracefully show "No video right now" or disable when unavailable
- [x] UMP Privacy Options accessible from Settings
- [x] Release build verification fails if `ads.properties` is missing
- [x] Unit and Compose UI test coverage passing
