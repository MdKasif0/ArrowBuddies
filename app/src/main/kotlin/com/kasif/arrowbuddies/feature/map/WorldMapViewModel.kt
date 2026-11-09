package com.kasif.arrowbuddies.feature.map

import com.kasif.arrowbuddies.R
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kasif.arrowbuddies.data.repository.GameRepository
import com.kasif.arrowbuddies.engine.model.WorldSpecies
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WorldMapViewModel @Inject constructor(
    private val repository: GameRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WorldMapUiState())
    val uiState: StateFlow<WorldMapUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                repository.currentLevelIndex,
                repository.highestUnlockedLevel
            ) { currentIndex, highestUnlocked ->
                Pair(currentIndex, highestUnlocked)
            }.collect { (currentIndex, highestUnlocked) ->
                _uiState.update {
                    it.copy(
                        currentLevelIndex = currentIndex.coerceIn(1, 240),
                        highestUnlockedLevel = highestUnlocked.coerceIn(1, 240)
                    )
                }
            }
        }
    }

    fun onEvent(event: WorldMapUiEvent) {
        when (event) {
            is WorldMapUiEvent.ShowWorldGate -> {
                val world = event.world.coerceIn(1, 8)
                val species = WorldSpecies.forWorld(world)
                _uiState.update {
                    it.copy(
                        unlockedWorldGate = WorldUnlockGate(
                            world = world,
                            worldNameRes = worldNameRes(world),
                            speciesSilhouettes = species
                        )
                    )
                }
            }
            is WorldMapUiEvent.DismissWorldGate -> {
                _uiState.update { it.copy(unlockedWorldGate = null) }
            }
        }
    }

    companion object {
        @StringRes
        fun worldNameRes(world: Int): Int = when (world) {
            2 -> R.string.ui_world_name_2
            3 -> R.string.ui_world_name_3
            4 -> R.string.ui_world_name_4
            5 -> R.string.ui_world_name_5
            6 -> R.string.ui_world_name_6
            7 -> R.string.ui_world_name_7
            8 -> R.string.ui_world_name_8
            else -> R.string.ui_world_name_1
        }
    }
}
