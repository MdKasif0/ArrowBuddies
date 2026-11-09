package com.kasif.arrowbuddies.feature.album

import androidx.compose.runtime.Immutable

@Immutable
data class AlbumUiState(
    val discoveredSpeciesCount: Int = 0,
    val totalSpeciesCount: Int = 24
)

sealed interface AlbumUiEvent {
    data object Refresh : AlbumUiEvent
}

