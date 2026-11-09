package com.kasif.arrowbuddies.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kasif.arrowbuddies.core.buddies.model.BuddySpecies
import com.kasif.arrowbuddies.data.repository.GameRepository
import com.kasif.arrowbuddies.engine.model.LevelIdHelper
import com.kasif.arrowbuddies.engine.model.WorldSpecies
import com.kasif.arrowbuddies.engine.runtime.DailyRescue
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: GameRepository
) : ViewModel() {

    private val todayKey: String =
        LocalDate.now(DailyRescue.TIME_ZONE).format(DateTimeFormatter.BASIC_ISO_DATE)

    val uiState: StateFlow<HomeUiState> = combine(
        combine(repository.totalRescued, repository.berries, ::Pair),
        repository.currentLevelIndex,
        repository.highestUnlockedLevel,
        repository.rescuedSpecies,
        repository.getDailyRecord(todayKey)
    ) { (totalRescued, berries), currentLevelIndex, highestUnlocked, speciesSet, dailyRecord ->
        val safeIndex = currentLevelIndex.coerceIn(1, 240)
        val levelId = LevelIdHelper.formatLevelId(safeIndex)
        val world = ((safeIndex - 1) / 30) + 1
        val levelInWorld = ((safeIndex - 1) % 30) + 1

        val worldPool = WorldSpecies.forWorld(world)
        val speciesName = worldPool[(levelInWorld - 1) % worldPool.size]
        val speciesEnum = runCatching { BuddySpecies.valueOf(speciesName) }.getOrDefault(BuddySpecies.BUNNY)

        val countdown = dailyCountdownText()

        HomeUiState(
            totalRescued = totalRescued,
            berries = berries,
            currentLevelIndex = safeIndex,
            currentLevelId = levelId,
            nextSpecies = speciesEnum,
            isDailyDone = dailyRecord?.completed == true,
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

/** Time left until the next Daily Rescue (midnight in the daily puzzle's time zone), e.g. "5h 12m". */
fun dailyCountdownText(): String {
    val now = LocalDateTime.now(DailyRescue.TIME_ZONE)
    val midnight = now.toLocalDate().plusDays(1).atStartOfDay()
    val duration = Duration.between(now, midnight)
    return "${duration.toHours()}h ${duration.toMinutes() % 60}m"
}
