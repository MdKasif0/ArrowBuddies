package com.kasif.arrowbuddies.feature.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kasif.arrowbuddies.data.repository.GameRepository
import com.kasif.arrowbuddies.engine.model.LevelIdHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val gameRepository: GameRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SplashUiState())
    val uiState: StateFlow<SplashUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val tutorialDone = gameRepository.tutorialCompleted.first()
            val currentIndex = gameRepository.currentLevelIndex.first().coerceIn(1, 240)
            val levelId = LevelIdHelper.formatLevelId(currentIndex)
            _uiState.update {
                it.copy(
                    isReady = true,
                    isFirstLaunch = !tutorialDone && currentIndex == 1,
                    initialLevelId = levelId
                )
            }
        }
    }
}
