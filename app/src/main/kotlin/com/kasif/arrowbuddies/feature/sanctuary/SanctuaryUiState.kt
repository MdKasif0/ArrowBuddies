package com.kasif.arrowbuddies.feature.sanctuary

import androidx.compose.runtime.Immutable

@Immutable
data class SanctuaryUiState(
    val unlockedZonesCount: Int = 1,
    val activeZoneName: String = "Sunny Meadow"
)

sealed interface SanctuaryUiEvent {
    data class ChangeZone(val zoneName: String) : SanctuaryUiEvent
}

