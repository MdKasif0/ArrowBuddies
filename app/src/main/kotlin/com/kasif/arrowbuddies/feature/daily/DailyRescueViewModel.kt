package com.kasif.arrowbuddies.feature.daily

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kasif.arrowbuddies.core.audio.SoundManager
import com.kasif.arrowbuddies.core.economy.EconomyConfig
import com.kasif.arrowbuddies.core.haptics.HapticsManager
import com.kasif.arrowbuddies.data.repository.GameRepository
import com.kasif.arrowbuddies.engine.runtime.DailyRescue
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import javax.inject.Inject

@HiltViewModel
class DailyRescueViewModel @Inject constructor(
    private val repository: GameRepository,
    private val soundManager: SoundManager,
    private val hapticsManager: HapticsManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(DailyRescueUiState())
    val uiState: StateFlow<DailyRescueUiState> = _uiState.asStateFlow()

    private val today = LocalDate.now(DailyRescue.TIME_ZONE)
    private val todayStr = today.format(DateTimeFormatter.BASIC_ISO_DATE)

    init {
        val todayFormatted = today.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL))
        val world = ((today.dayOfYear - 1) % 8) + 1

        viewModelScope.launch {
            combine(
                repository.playerProfile,
                repository.getDailyRecord(todayStr)
            ) { profile, dailyRecord ->
                val isCompleted = dailyRecord?.completed == true
                val stars = dailyRecord?.stars ?: 0
                val mistakes = dailyRecord?.mistakes ?: 0
                val streak = profile.streakDays.coerceIn(0, 7)

                val streakList = EconomyConfig.STREAK_REWARDS.mapIndexed { idx, berries ->
                    val dayNum = idx + 1
                    val isClaimed = if (isCompleted) dayNum <= streak else dayNum < streak
                    val isCurrent = if (isCompleted) dayNum == streak else dayNum == (streak + 1).coerceAtMost(7)

                    StreakDayReward(
                        dayNumber = dayNum,
                        berries = berries,
                        hasDecorGift = dayNum == 7,
                        isClaimed = isClaimed,
                        isCurrent = isCurrent
                    )
                }

                val starsEmoji = if (stars > 0) "⭐".repeat(stars) else "⭐⭐⭐"
                val shareText = "ArrowBuddies Daily Rescue 🐾\n" +
                        "Date: $todayFormatted\n" +
                        "Stars: $starsEmoji\n" +
                        "Bonks: $mistakes 🎯\n" +
                        "Free cute buddies with every tap!"

                _uiState.update { current ->
                    current.copy(
                        todayDateFormatted = todayFormatted,
                        dateRaw = todayStr,
                        streakDays = streak,
                        isCompletedToday = isCompleted,
                        todayStars = stars,
                        todayMistakes = mistakes,
                        streakRewards = streakList,
                        berries = profile.berries,
                        worldThemeIndex = world,
                        buddyCount = 8,
                        dailyLevelId = "daily-$todayStr",
                        shareResultText = shareText
                    )
                }
            }.collect {}
        }
    }

    fun onEvent(event: DailyRescueUiEvent) {
        when (event) {
            is DailyRescueUiEvent.PlayDailyRescue -> {
                soundManager.playPop()
                hapticsManager.tick()
            }
            is DailyRescueUiEvent.ShareResult -> {
                soundManager.playChime()
                hapticsManager.confirm()
            }
        }
    }
}
