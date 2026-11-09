package com.kasif.arrowbuddies.feature.map

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
                val worldName = getWorldName(world)
                val species = WorldSpecies.forWorld(world)
                _uiState.update {
                    it.copy(
                        unlockedWorldGate = WorldUnlockGate(
                            world = world,
                            worldName = worldName,
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
        fun getWorldName(world: Int): String = when (world) {
            1 -> "Sunny Meadow"
            2 -> "Happy Farm"
            3 -> "Jungle Ruins"
            4 -> "Snowy Peaks"
            5 -> "Coral Coast"
            6 -> "Moonlit Forest"
            7 -> "Desert Oasis"
            8 -> "Sky Islands"
            else -> "Sunny Meadow"
        }
    }
}
