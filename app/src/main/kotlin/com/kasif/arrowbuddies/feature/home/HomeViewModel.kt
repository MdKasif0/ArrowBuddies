package com.kasif.arrowbuddies.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kasif.arrowbuddies.core.buddies.model.BuddySpecies
import com.kasif.arrowbuddies.data.repository.GameRepository
import com.kasif.arrowbuddies.engine.model.WorldSpecies
import com.kasif.arrowbuddies.engine.runtime.DailyRescue
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: GameRepository
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = combine(
        repository.totalRescued,
        repository.berries,
        repository.currentLevelIndex,
        repository.highestUnlockedLevel,
        repository.rescuedSpecies
    ) { totalRescued, berries, currentLevelIndex, highestUnlocked, speciesSet ->
        val safeIndex = currentLevelIndex.coerceIn(1, 240)
        val world = ((safeIndex - 1) / 30) + 1
        val levelInWorld = ((safeIndex - 1) % 30) + 1
        val levelId = "w$world-l${levelInWorld.toString().padStart(2, '0')}"

        val worldPool = WorldSpecies.forWorld(world)
        val speciesName = worldPool[(levelInWorld - 1) % worldPool.size]
        val speciesEnum = runCatching { BuddySpecies.valueOf(speciesName) }.getOrDefault(BuddySpecies.BUNNY)

        // Calculate time to next daily puzzle (midnight in Asia/Kolkata)
        val now = LocalDateTime.now(DailyRescue.TIME_ZONE)
        val midnight = now.toLocalDate().plusDays(1).atStartOfDay()
        val duration = Duration.between(now, midnight)
        val hours = duration.toHours()
        val minutes = duration.toMinutes() % 60
        val countdown = "${hours}h ${minutes}m"

        HomeUiState(
            totalRescued = totalRescued,
            berries = berries,
            currentLevelIndex = safeIndex,
            currentLevelId = levelId,
            nextSpecies = speciesEnum,
            isDailyDone = false,
            dailyCountdownFormatted = countdown,
            isRescueRunUnlocked = highestUnlocked >= 31, // Unlocked after World 1 (level 31+)
            unlockedSpeciesCount = maxOf(1, speciesSet.size),
            sanctuaryBuddiesCount = totalRescued
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState()
    )

    fun onEvent(event: HomeUiEvent) {
        // Event handling
    }
}
