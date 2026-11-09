package com.kasif.arrowbuddies.feature.game

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.navigation.toRoute
import com.kasif.arrowbuddies.navigation.Route
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class GameViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val route = runCatching { savedStateHandle.toRoute<Route.Game>() }.getOrNull()
    private val levelId = route?.levelId ?: savedStateHandle.get<String>("levelId") ?: "1"

    private val _uiState = MutableStateFlow(GameUiState(levelId = levelId))
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    fun onEvent(event: GameUiEvent) {
        // Core gameplay handled in Step 5
    }
}

