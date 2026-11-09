package com.kasif.arrowbuddies.feature.splash

import androidx.compose.runtime.Immutable

@Immutable
data class SplashUiState(
    val isLoading: Boolean = false,
    val isReady: Boolean = false,
    val isFirstLaunch: Boolean = true,
    val initialLevelId: String = "w1-l01"
)

sealed interface SplashUiEvent {
    data object FinishIntro : SplashUiEvent
}
