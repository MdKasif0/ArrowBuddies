package com.kasif.arrowbuddies.feature.album

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kasif.arrowbuddies.core.audio.SoundManager
import com.kasif.arrowbuddies.core.buddies.model.BuddySpecies
import com.kasif.arrowbuddies.core.haptics.HapticsManager
import com.kasif.arrowbuddies.core.model.AchievementType
import com.kasif.arrowbuddies.core.model.BuddyAccessory
import com.kasif.arrowbuddies.core.model.FriendshipTier
import com.kasif.arrowbuddies.data.repository.GameRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AlbumViewModel @Inject constructor(
    private val repository: GameRepository,
    private val soundManager: SoundManager,
    private val hapticsManager: HapticsManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(AlbumUiState())
    val uiState: StateFlow<AlbumUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                repository.playerProfile,
                repository.speciesProgressList,
                repository.achievements
            ) { profile, speciesList, achievementsList ->
                val speciesMap = speciesList.associateBy { it.species }
                val achievementMap = achievementsList.associateBy { it.id }

                val allSpeciesModels = BuddySpecies.entries.map { species ->
                    val progress = speciesMap[species.name]
                    val rescues = progress?.rescuedCount ?: 0
                    SpeciesAlbumUiModel(
                        species = species,
                        rescuedCount = rescues,
                        tier = progress?.tier ?: FriendshipTier.LOCKED,
                        equippedAccessory = progress?.equippedAccessory ?: BuddyAccessory.NONE,
                        unlocked = rescues > 0,
                        funFactRes = species.getFunFactRes()
                    )
                }

                val allAchievementModels = AchievementType.entries.map { type ->
                    val record = achievementMap[type.id]
                    val isUnlocked = (record?.unlockedAt ?: 0L) > 0L
                    AchievementAlbumUiModel(
                        type = type,
                        progress = record?.progress ?: if (isUnlocked) type.maxProgress else 0,
                        maxProgress = type.maxProgress,
                        unlocked = isUnlocked,
                        unlockedAt = record?.unlockedAt ?: 0L
                    )
                }

                _uiState.update { current ->
                    // Also refresh selected modal model if open
                    val updatedModal = current.selectedSpeciesModal?.let { modal ->
                        allSpeciesModels.firstOrNull { it.species == modal.species }
                    }

                    current.copy(
                        berries = profile.berries,
                        totalRescued = profile.totalRescued,
                        discoveredSpeciesCount = allSpeciesModels.count { it.unlocked },
                        speciesList = allSpeciesModels,
                        achievementsList = allAchievementModels,
                        selectedSpeciesModal = updatedModal
                    )
                }
            }.collect {}
        }
    }

    fun onEvent(event: AlbumUiEvent) {
        when (event) {
            is AlbumUiEvent.SelectTab -> {
                soundManager.playPop()
                hapticsManager.tick()
                _uiState.update { it.copy(selectedTab = event.tab) }
            }

            is AlbumUiEvent.ClickSpecies -> {
                val model = _uiState.value.speciesList.firstOrNull { it.species == event.species }
                if (model != null && model.unlocked) {
                    soundManager.playPop()
                    hapticsManager.tick()
                    _uiState.update { it.copy(selectedSpeciesModal = model) }
                } else {
                    soundManager.playBoing()
                    hapticsManager.bonk()
                }
            }

            is AlbumUiEvent.EquipAccessory -> {
                viewModelScope.launch {
                    val success = repository.equipAccessory(event.species, event.accessory)
                    if (success) {
                        soundManager.playChime()
                        hapticsManager.confirm()
                    } else {
                        soundManager.playBoing()
                        hapticsManager.reject()
                    }
                }
            }

            is AlbumUiEvent.DismissSpeciesModal -> {
                _uiState.update { it.copy(selectedSpeciesModal = null) }
            }

            is AlbumUiEvent.DismissCelebration -> {
                _uiState.update { it.copy(tierUpCelebration = null) }
            }

            is AlbumUiEvent.GrantDebugBerries -> {
                viewModelScope.launch {
                    repository.addBerries(event.amount)
                    soundManager.playChime()
                    hapticsManager.confirm()
                }
            }

            is AlbumUiEvent.ToggleDebugPanel -> {
                _uiState.update { it.copy(showDebugPanel = !it.showDebugPanel) }
            }
        }
    }
}
