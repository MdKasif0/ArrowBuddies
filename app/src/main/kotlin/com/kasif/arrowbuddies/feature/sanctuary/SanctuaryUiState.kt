package com.kasif.arrowbuddies.feature.sanctuary

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
    val activeZoneName: String
        get() = when (activeZoneIndex + 1) {
            1 -> "Sunny Meadow"
            2 -> "Sticky Bog"
            3 -> "Clockwork Garden"
            4 -> "Ancient Ruins"
            5 -> "Mystic Forest"
            6 -> "Cozy Burrow"
            7 -> "Twilight Oasis"
            8 -> "Celebration Grove"
            else -> "Sunny Meadow"
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
