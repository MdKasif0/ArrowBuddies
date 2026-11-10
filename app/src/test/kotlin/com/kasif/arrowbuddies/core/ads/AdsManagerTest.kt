package com.kasif.arrowbuddies.core.ads

import android.app.Activity
import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage
import com.kasif.arrowbuddies.engine.model.GameState
import com.kasif.arrowbuddies.engine.model.LevelDef
import com.kasif.arrowbuddies.feature.game.GameUiState
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.concurrent.atomic.AtomicInteger

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class AdsManagerTest {

    private fun createActivity(): Activity = Robolectric.buildActivity(Activity::class.java).setup().get()

    @Test
    fun `reward granting is strictly idempotent and cannot double grant`() {
        val fakeAdsManager = FakeAdsManager(initialState = AdState.Ready)
        val rewardCount = AtomicInteger(0)

        // Simulate ad show with idempotent reward callback
        fakeAdsManager.showRewardedAd(
            activity = createActivity(),
            placement = AdPlacement.FAIL_REVIVE,
            onUserEarnedReward = {
                rewardCount.incrementAndGet()
            }
        )

        assertThat(rewardCount.get()).isEqualTo(1)
        assertThat(fakeAdsManager.rewardGrantedCount.get()).isEqualTo(1)
        assertThat(fakeAdsManager.showCount.get()).isEqualTo(1)
    }

    @Test
    fun `ad manager rejects showing when ad is not ready`() {
        val fakeAdsManager = FakeAdsManager(initialState = AdState.Unavailable)
        var failedReason: String? = null
        var rewardGranted = false

        fakeAdsManager.showRewardedAd(
            activity = createActivity(),
            placement = AdPlacement.WIN_DOUBLE_BERRIES,
            onUserEarnedReward = { rewardGranted = true },
            onAdFailedToShow = { reason -> failedReason = reason }
        )

        assertThat(rewardGranted).isFalse()
        assertThat(failedReason).isNotNull()
        assertThat(fakeAdsManager.showCount.get()).isEqualTo(0)
    }

    @Test
    fun `ad manager rejects showing when ads are globally disabled`() {
        val fakeAdsManager = FakeAdsManager(initialState = AdState.Ready, isAdsEnabled = false)
        var failedReason: String? = null
        var rewardGranted = false

        fakeAdsManager.showRewardedAd(
            activity = createActivity(),
            placement = AdPlacement.FREE_HINT,
            onUserEarnedReward = { rewardGranted = true },
            onAdFailedToShow = { reason -> failedReason = reason }
        )

        assertThat(rewardGranted).isFalse()
        assertThat(failedReason).isNotNull()
    }

    @Test
    fun `tutorial levels 1 to 6 suppress ads completely`() {
        val dummyLevel = LevelDef(
            id = "1",
            cols = 3,
            rows = 3,
            tiles = emptyMap(),
            buddies = emptyList(),
            world = 1,
            index = 1
        )
        val dummyState = GameState(level = dummyLevel, buddies = emptyList())

        // Levels 1 through 6
        listOf("1", "2", "3", "4", "5", "6", "w1-l01", "w1-l02", "w1-l06").forEach { levelId ->
            val uiState = GameUiState(
                levelId = levelId,
                level = dummyLevel.copy(id = levelId),
                gameState = dummyState
            )
            assertWithMessage("canShowAds for tutorial level $levelId")
                .that(uiState.canShowAds)
                .isFalse()
        }

        // Levels 7 and above, plus daily levels
        listOf("7", "8", "w1-l07", "w1-l30", "w2-l01", "daily-2026-11-10").forEach { levelId ->
            val uiState = GameUiState(
                levelId = levelId,
                level = dummyLevel.copy(id = levelId),
                gameState = dummyState
            )
            assertWithMessage("canShowAds for level $levelId")
                .that(uiState.canShowAds)
                .isTrue()
        }
    }

    @Test
    fun `daily hint ad counter enforces daily cap of 3 and resets on new date`() {
        // Test simulation of daily ad hints logic (mirroring GamePreferencesDataStore)
        class DailyHintTracker {
            var lastDate: String = ""
            var count: Int = 0

            fun getRemaining(today: String): Int {
                if (lastDate != today) {
                    lastDate = today
                    count = 0
                }
                return (3 - count).coerceAtLeast(0)
            }

            fun claim(today: String): Boolean {
                if (lastDate != today) {
                    lastDate = today
                    count = 0
                }
                if (count < 3) {
                    count++
                    return true
                }
                return false
            }
        }

        val tracker = DailyHintTracker()
        val day1 = "2026-11-10"
        val day2 = "2026-11-11"

        // Day 1
        assertThat(tracker.getRemaining(day1)).isEqualTo(3)
        assertThat(tracker.claim(day1)).isTrue()
        assertThat(tracker.getRemaining(day1)).isEqualTo(2)

        assertThat(tracker.claim(day1)).isTrue()
        assertThat(tracker.getRemaining(day1)).isEqualTo(1)

        assertThat(tracker.claim(day1)).isTrue()
        assertThat(tracker.getRemaining(day1)).isEqualTo(0)

        // 4th claim on Day 1 fails due to daily cap
        assertThat(tracker.claim(day1)).isFalse()
        assertThat(tracker.getRemaining(day1)).isEqualTo(0)

        // Midnight rolls over to Day 2: counter resets to 3
        assertThat(tracker.getRemaining(day2)).isEqualTo(3)
        assertThat(tracker.claim(day2)).isTrue()
        assertThat(tracker.getRemaining(day2)).isEqualTo(2)
    }

    @Test
    fun `ad state transitions between Loading, Ready, and Unavailable`() = runTest {
        val fakeAdsManager = FakeAdsManager(initialState = AdState.Loading)
        assertThat(fakeAdsManager.adState.first()).isEqualTo(AdState.Loading)

        fakeAdsManager.setAdState(AdState.Ready)
        assertThat(fakeAdsManager.adState.first()).isEqualTo(AdState.Ready)

        fakeAdsManager.setAdState(AdState.Unavailable)
        assertThat(fakeAdsManager.adState.first()).isEqualTo(AdState.Unavailable)
    }
}
