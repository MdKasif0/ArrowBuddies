package com.kasif.arrowbuddies.feature.sanctuary

import com.kasif.arrowbuddies.R
import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import com.kasif.arrowbuddies.core.buddies.model.BuddySpecies
import com.kasif.arrowbuddies.core.model.BuddyAccessory
import com.kasif.arrowbuddies.data.db.entity.DecorItem
import com.kasif.arrowbuddies.engine.model.Direction

enum class SanctuaryBuddyState {
    IDLE,
    WALK,
    HOP,
    NAP,
    CHEER,
    REACT_DECOR
}

@Immutable
data class SanctuaryBuddyUiModel(
    val id: String,
    val species: BuddySpecies,
    val accessory: BuddyAccessory = BuddyAccessory.NONE,
    val x: Float, // horizontal position in dp along the panoramic ground
    val groundY: Float = 0f, // vertical offset in dp (0..35dp)
    val targetX: Float = x,
    val direction: Direction = Direction.RIGHT,
    val state: SanctuaryBuddyState = SanctuaryBuddyState.IDLE,
    val bubbleText: String? = null,
    val bubbleTimerTicks: Int = 0,
    val heartPop: Boolean = false
)

@Immutable
data class SanctuaryUiState(
    val berries: Int = 100,
    val totalRescued: Int = 0,
    val unlockedZonesCount: Int = 1,
    val unlockedSpeciesCount: Int = 1,
    val decorItems: Map<String, DecorItem> = emptyMap(),
    val isTreatTimeAvailable: Boolean = true,
    val buddies: List<SanctuaryBuddyUiModel> = emptyList(),
    val activeZoneIndex: Int = 0,
    val selectedDecorToBuy: DecorItem? = null,
    val showNotEnoughBerriesNotice: Boolean = false,
    val isTreatScatterActive: Boolean = false
) {
    @get:StringRes
    val activeZoneNameRes: Int
        get() = when (activeZoneIndex + 1) {
            2 -> R.string.ui_zone_name_2
            3 -> R.string.ui_zone_name_3
            4 -> R.string.ui_zone_name_4
            5 -> R.string.ui_zone_name_5
            6 -> R.string.ui_zone_name_6
            7 -> R.string.ui_zone_name_7
            8 -> R.string.ui_zone_name_8
            else -> R.string.ui_zone_name_1
        }
}

sealed interface SanctuaryUiEvent {
    data class TapBuddy(val buddyId: String) : SanctuaryUiEvent
    data object TapTreatBasket : SanctuaryUiEvent
    data class ClickDecorSlot(val decorId: String) : SanctuaryUiEvent
    data class ConfirmBuyDecor(val decorId: String) : SanctuaryUiEvent
    data object DismissDecorDialog : SanctuaryUiEvent
    data class SelectZone(val zoneIndex: Int) : SanctuaryUiEvent
}
