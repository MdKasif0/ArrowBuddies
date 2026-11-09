package com.kasif.arrowbuddies.feature.sanctuary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kasif.arrowbuddies.core.audio.SoundManager
import com.kasif.arrowbuddies.core.buddies.model.BuddySpecies
import com.kasif.arrowbuddies.core.haptics.HapticsManager
import com.kasif.arrowbuddies.core.model.BuddyAccessory
import com.kasif.arrowbuddies.data.repository.GameRepository
import com.kasif.arrowbuddies.engine.model.Direction
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class SanctuaryViewModel @Inject constructor(
    private val repository: GameRepository,
    private val soundManager: SoundManager,
    private val hapticsManager: HapticsManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(SanctuaryUiState())
    val uiState: StateFlow<SanctuaryUiState> = _uiState.asStateFlow()

    private var treatScatterTicksRemaining = 0
    private var hasInitializedBuddies = false

    init {
        // Collect database flows
        viewModelScope.launch {
            combine(
                repository.playerProfile,
                repository.decorItems,
                repository.speciesProgressList
            ) { profile, decorList, speciesList ->
                val todayStr = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE)
                val treatAvailable = profile.lastTreatTimeDate != todayStr
                val unlockedZones = (((profile.highestUnlockedLevel - 1) / 30) + 1).coerceIn(1, 8)
                val decorMap = decorList.associateBy { it.id }

                val unlockedSpecies = speciesList.filter { it.rescuedCount > 0 }
                val speciesAccessories = speciesList.associate { it.species to it.equippedAccessory }

                if (!hasInitializedBuddies) {
                    hasInitializedBuddies = true
                    initBuddies(unlockedSpecies.map { it.species }, speciesAccessories, unlockedZones)
                }

                _uiState.update { current ->
                    // Update buddy accessories in case player changed them in album
                    val updatedBuddies = current.buddies.map { b ->
                        val acc = speciesAccessories[b.species.name] ?: BuddyAccessory.NONE
                        b.copy(accessory = acc)
                    }

                    current.copy(
                        berries = profile.berries,
                        totalRescued = profile.totalRescued,
                        unlockedZonesCount = unlockedZones,
                        unlockedSpeciesCount = maxOf(1, unlockedSpecies.size),
                        decorItems = decorMap,
                        isTreatTimeAvailable = treatAvailable,
                        buddies = updatedBuddies
                    )
                }
            }.collect {}
        }

        // Start cheap autonomous AI behavior tick loop (one coroutine, ~250ms ticks)
        startBehaviorLoop()
    }

    private fun initBuddies(
        unlockedSpeciesNames: List<String>,
        speciesAccessories: Map<String, BuddyAccessory>,
        unlockedZones: Int
    ) {
        val speciesPool = if (unlockedSpeciesNames.isNotEmpty()) {
            unlockedSpeciesNames.mapNotNull { name ->
                runCatching { BuddySpecies.valueOf(name) }.getOrNull()
            }
        } else {
            listOf(BuddySpecies.BUNNY, BuddySpecies.CHICK, BuddySpecies.PIGLET)
        }

        // Spawn up to 20-25 buddies distributed naturally across the unlocked sanctuary zones
        val maxPanoramicWidth = (unlockedZones * 440f).coerceAtLeast(440f)
        val buddyCount = minOf(24, maxOf(4, speciesPool.size * 2))

        val initialBuddies = (0 until buddyCount).map { i ->
            val species = speciesPool[i % speciesPool.size]
            val accessory = speciesAccessories[species.name] ?: BuddyAccessory.NONE
            val posX = (80f + (i * (maxPanoramicWidth - 140f) / buddyCount) + Random.nextFloat() * 40f)
                .coerceIn(50f, maxPanoramicWidth - 60f)
            val groundY = Random.nextFloat() * 32f

            SanctuaryBuddyUiModel(
                id = "sanctuary_buddy_$i",
                species = species,
                accessory = accessory,
                x = posX,
                groundY = groundY,
                targetX = posX,
                direction = if (Random.nextBoolean()) Direction.RIGHT else Direction.LEFT,
                state = SanctuaryBuddyState.IDLE
            )
        }

        _uiState.update { it.copy(buddies = initialBuddies) }
    }

    private fun startBehaviorLoop() {
        viewModelScope.launch {
            while (isActive) {
                delay(250L)
                tickBehavior()
            }
        }
    }

    private fun tickBehavior() {
        val current = _uiState.value
        if (current.buddies.isEmpty()) return

        val maxZoneWidth = (current.unlockedZonesCount * 440f).coerceAtLeast(440f)
        val isTreatActive = treatScatterTicksRemaining > 0
        if (isTreatActive) {
            treatScatterTicksRemaining--
            if (treatScatterTicksRemaining == 0) {
                _uiState.update { it.copy(isTreatScatterActive = false) }
            }
        }

        val updated = current.buddies.map { buddy ->
            var newX = buddy.x
            var newTargetX = buddy.targetX
            var newState = buddy.state
            var newDir = buddy.direction
            var newBubble = buddy.bubbleText
            var newTimer = buddy.bubbleTimerTicks
            var newHeart = buddy.heartPop

            // Decrement bubble timer
            if (newTimer > 0) {
                newTimer--
                if (newTimer == 0) {
                    newBubble = null
                    newHeart = false
                }
            }

            if (isTreatActive) {
                // Rushing towards treat basket at x=180f
                val basketX = 180f + (Random.nextFloat() * 60f - 30f)
                newTargetX = basketX
                newState = SanctuaryBuddyState.CHEER
                newDir = if (newTargetX > newX) Direction.RIGHT else Direction.LEFT
                val speed = 8f
                newX = if (kotlin.math.abs(newTargetX - newX) <= speed) newTargetX
                else if (newTargetX > newX) newX + speed else newX - speed
            } else {
                when (buddy.state) {
                    SanctuaryBuddyState.WALK -> {
                        val speed = 5.5f
                        if (kotlin.math.abs(buddy.targetX - buddy.x) <= speed) {
                            newX = buddy.targetX
                            newState = if (Random.nextFloat() < 0.4f) SanctuaryBuddyState.HOP else SanctuaryBuddyState.IDLE
                        } else {
                            newX = if (buddy.targetX > buddy.x) buddy.x + speed else buddy.x - speed
                            newDir = if (buddy.targetX > buddy.x) Direction.RIGHT else Direction.LEFT
                        }
                    }
                    SanctuaryBuddyState.HOP -> {
                        if (Random.nextFloat() < 0.25f) {
                            newState = SanctuaryBuddyState.IDLE
                        }
                    }
                    SanctuaryBuddyState.NAP -> {
                        if (Random.nextFloat() < 0.08f) {
                            newState = SanctuaryBuddyState.IDLE
                        }
                    }
                    SanctuaryBuddyState.CHEER -> {
                        if (!isTreatActive && Random.nextFloat() < 0.2f) {
                            newState = SanctuaryBuddyState.IDLE
                        }
                    }
                    SanctuaryBuddyState.REACT_DECOR -> {
                        if (Random.nextFloat() < 0.12f) {
                            newState = SanctuaryBuddyState.IDLE
                        }
                    }
                    SanctuaryBuddyState.IDLE -> {
                        // Random chance to start an action
                        if (Random.nextFloat() < 0.08f) {
                            val roll = Random.nextFloat()
                            when {
                                roll < 0.50f -> {
                                    // Walk to a new spot
                                    val delta = (Random.nextFloat() * 160f - 80f)
                                    newTargetX = (buddy.x + delta).coerceIn(60f, maxZoneWidth - 60f)
                                    newState = SanctuaryBuddyState.WALK
                                    newDir = if (newTargetX > buddy.x) Direction.RIGHT else Direction.LEFT
                                }
                                roll < 0.75f -> {
                                    newState = SanctuaryBuddyState.HOP
                                }
                                roll < 0.90f -> {
                                    newState = SanctuaryBuddyState.NAP
                                }
                                else -> {
                                    newState = SanctuaryBuddyState.REACT_DECOR
                                }
                            }
                        }
                    }
                }
            }

            buddy.copy(
                x = newX,
                targetX = newTargetX,
                direction = newDir,
                state = newState,
                bubbleText = newBubble,
                bubbleTimerTicks = newTimer,
                heartPop = newHeart
            )
        }

        _uiState.update { it.copy(buddies = updated) }
    }

    fun onEvent(event: SanctuaryUiEvent) {
        when (event) {
            is SanctuaryUiEvent.TapBuddy -> {
                soundManager.playPop()
                hapticsManager.tick()

                _uiState.update { current ->
                    val updated = current.buddies.map { b ->
                        if (b.id == event.buddyId) {
                            val displayName = b.species.name.lowercase().replaceFirstChar { it.titlecase() }
                            b.copy(
                                state = SanctuaryBuddyState.HOP,
                                bubbleText = "$displayName! ❤️",
                                bubbleTimerTicks = 9,
                                heartPop = true
                            )
                        } else b
                    }
                    current.copy(buddies = updated)
                }
            }

            is SanctuaryUiEvent.TapTreatBasket -> {
                val current = _uiState.value
                if (current.isTreatTimeAvailable) {
                    val todayStr = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE)
                    viewModelScope.launch {
                        val claimed = repository.claimTreatTime(todayStr)
                        if (claimed) {
                            soundManager.playChime()
                            hapticsManager.confirm()
                            treatScatterTicksRemaining = 16 // 4 seconds of treat rush
                            _uiState.update {
                                it.copy(
                                    isTreatTimeAvailable = false,
                                    isTreatScatterActive = true
                                )
                            }
                        }
                    }
                }
            }

            is SanctuaryUiEvent.ClickDecorSlot -> {
                val decor = _uiState.value.decorItems[event.decorId]
                if (decor != null && !decor.owned) {
                    _uiState.update { it.copy(selectedDecorToBuy = decor) }
                }
            }

            is SanctuaryUiEvent.ConfirmBuyDecor -> {
                viewModelScope.launch {
                    val success = repository.buyDecor(event.decorId)
                    if (success) {
                        soundManager.playChime()
                        hapticsManager.confirm()
                        _uiState.update { it.copy(selectedDecorToBuy = null) }
                    } else {
                        soundManager.playBoing()
                        hapticsManager.reject()
                        _uiState.update { it.copy(showNotEnoughBerriesNotice = true) }
                    }
                }
            }

            is SanctuaryUiEvent.DismissDecorDialog -> {
                _uiState.update { it.copy(selectedDecorToBuy = null, showNotEnoughBerriesNotice = false) }
            }

            is SanctuaryUiEvent.SelectZone -> {
                _uiState.update { it.copy(activeZoneIndex = event.zoneIndex) }
            }
        }
    }
}
