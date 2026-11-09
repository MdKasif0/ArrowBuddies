package com.kasif.arrowbuddies.feature.splash

import androidx.compose.runtime.Immutable

@Immutable
data class SplashUiState(
    val isLoading: Boolean = true,
    val initialLevelId: String = "1"
)

sealed interface SplashUiEvent {
    data object ContinueToGame : SplashUiEvent
    data object ContinueToHome : SplashUiEvent
}

