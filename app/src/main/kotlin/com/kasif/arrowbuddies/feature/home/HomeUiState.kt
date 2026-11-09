package com.kasif.arrowbuddies.feature.home

import androidx.compose.runtime.Immutable

@Immutable
data class HomeUiState(
    val totalRescued: Int = 0,
    val currentLevelId: String = "1"
)

sealed interface HomeUiEvent {
    data object Refresh : HomeUiEvent
}

