package com.kasif.arrowbuddies.feature.map

import androidx.compose.runtime.Immutable
import com.kasif.arrowbuddies.core.buddies.model.BuddySpecies

@Immutable
data class WorldUnlockGate(
    val world: Int,
    val worldName: String,
    val speciesSilhouettes: List<String>
)

@Immutable
data class WorldMapUiState(
    val currentLevelIndex: Int = 1,
    val highestUnlockedLevel: Int = 1,
    val starsMap: Map<String, Int> = emptyMap(),
    val unlockedWorldGate: WorldUnlockGate? = null
)

sealed interface WorldMapUiEvent {
    data class ShowWorldGate(val world: Int) : WorldMapUiEvent
    data object DismissWorldGate : WorldMapUiEvent
}
