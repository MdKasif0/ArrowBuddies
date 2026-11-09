package com.kasif.arrowbuddies.feature.map

import androidx.compose.runtime.Immutable

@Immutable
data class WorldMapUiState(
    val unlockedWorldCount: Int = 1,
    val selectedWorldId: Int = 1
)

sealed interface WorldMapUiEvent {
    data class SelectWorld(val worldId: Int) : WorldMapUiEvent
}

