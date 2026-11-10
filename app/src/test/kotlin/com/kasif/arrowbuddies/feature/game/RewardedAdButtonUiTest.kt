package com.kasif.arrowbuddies.feature.game

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.kasif.arrowbuddies.core.ads.AdState
import com.kasif.arrowbuddies.core.designsystem.theme.ArrowBuddiesTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class RewardedAdButtonUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `fail sheet displays active ad revive button when ad is ready`() {
        composeTestRule.setContent {
            ArrowBuddiesTheme {
                LevelFailSheet(
                    berries = 50,
                    canShowAds = true,
                    adState = AdState.Ready,
                    onRestart = {},
                    onReviveWithHeart = {},
                    onReviveWithAd = {},
                    onNavigateToMap = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Watch Video for +1 Heart")
            .assertExists()
            .assertHasClickAction()
    }

    @Test
    fun `fail sheet displays disabled no video button when ad is unavailable`() {
        composeTestRule.setContent {
            ArrowBuddiesTheme {
                LevelFailSheet(
                    berries = 50,
                    canShowAds = true,
                    adState = AdState.Unavailable,
                    onRestart = {},
                    onReviveWithHeart = {},
                    onReviveWithAd = {},
                    onNavigateToMap = {}
                )
            }
        }

        composeTestRule.onNodeWithText("No video right now")
            .assertExists()
            .assertIsNotEnabled()
    }

    @Test
    fun `fail sheet suppresses ad button during tutorial levels`() {
        composeTestRule.setContent {
            ArrowBuddiesTheme {
                LevelFailSheet(
                    berries = 50,
                    canShowAds = false, // Tutorial level suppression
                    adState = AdState.Ready,
                    onRestart = {},
                    onReviveWithHeart = {},
                    onReviveWithAd = {},
                    onNavigateToMap = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Watch Video for +1 Heart").assertDoesNotExist()
        composeTestRule.onNodeWithText("No video right now").assertDoesNotExist()
    }

    @Test
    fun `win sheet displays double berries button when ad is ready`() {
        composeTestRule.setContent {
            ArrowBuddiesTheme {
                LevelWinSheet(
                    stars = 3,
                    rescuedBuddies = emptyList(),
                    berriesEarned = 25,
                    goldenBonus = 0,
                    newSpecies = null,
                    canShowAds = true,
                    adState = AdState.Ready,
                    isBerriesDoubled = false,
                    onDoubleBerriesWithAd = {},
                    onNextLevel = {},
                    onRestart = {},
                    onNavigateToMap = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Double Berries (+25 🫐 with Video)")
            .assertExists()
            .assertHasClickAction()
    }

    @Test
    fun `booster shop displays free hint ad button when ad is ready`() {
        composeTestRule.setContent {
            ArrowBuddiesTheme {
                BoosterShopDialog(
                    type = BoosterType.Hint,
                    berries = 10,
                    canShowAds = true,
                    adState = AdState.Ready,
                    remainingDailyAdHints = 2,
                    onBuy = {},
                    onFreeHintWithAd = {},
                    onDismiss = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Free Hint with Video (2/3 today)")
            .assertExists()
            .assertHasClickAction()
    }
}
