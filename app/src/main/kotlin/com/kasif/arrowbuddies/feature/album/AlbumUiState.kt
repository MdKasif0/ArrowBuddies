package com.kasif.arrowbuddies.feature.album

import androidx.compose.runtime.Immutable
import com.kasif.arrowbuddies.R
import com.kasif.arrowbuddies.core.buddies.model.BuddySpecies
import com.kasif.arrowbuddies.core.model.AchievementType
import com.kasif.arrowbuddies.core.model.BuddyAccessory
import com.kasif.arrowbuddies.core.model.FriendshipTier

enum class AlbumTab {
    BUDDIES,
    ACHIEVEMENTS
}

@Immutable
data class SpeciesAlbumUiModel(
    val species: BuddySpecies,
    val rescuedCount: Int = 0,
    val tier: FriendshipTier = FriendshipTier.LOCKED,
    val equippedAccessory: BuddyAccessory = BuddyAccessory.NONE,
    val unlocked: Boolean = false,
    val funFactRes: Int
)

@Immutable
data class AchievementAlbumUiModel(
    val type: AchievementType,
    val progress: Int = 0,
    val maxProgress: Int = 1,
    val unlocked: Boolean = false,
    val unlockedAt: Long = 0L
)

@Immutable
data class TierUpCelebrationModel(
    val species: BuddySpecies,
    val tier: FriendshipTier,
    val accessory: BuddyAccessory
)

@Immutable
data class AlbumUiState(
    val selectedTab: AlbumTab = AlbumTab.BUDDIES,
    val discoveredSpeciesCount: Int = 0,
    val totalSpeciesCount: Int = 24,
    val totalRescued: Int = 0,
    val berries: Int = 100,
    val speciesList: List<SpeciesAlbumUiModel> = emptyList(),
    val achievementsList: List<AchievementAlbumUiModel> = emptyList(),
    val selectedSpeciesModal: SpeciesAlbumUiModel? = null,
    val tierUpCelebration: TierUpCelebrationModel? = null,
    val showDebugPanel: Boolean = false
)

fun BuddySpecies.getFunFactRes(): Int = when (this) {
    BuddySpecies.BUNNY -> R.string.fact_bunny
    BuddySpecies.CHICK -> R.string.fact_chick
    BuddySpecies.PIGLET -> R.string.fact_piglet
    BuddySpecies.PUPPY -> R.string.fact_puppy
    BuddySpecies.LAMB -> R.string.fact_lamb
    BuddySpecies.CALF -> R.string.fact_calf
    BuddySpecies.MONKEY -> R.string.fact_monkey
    BuddySpecies.TIGER_CUB -> R.string.fact_tiger_cub
    BuddySpecies.PARROT -> R.string.fact_parrot
    BuddySpecies.PENGUIN -> R.string.fact_penguin
    BuddySpecies.POLAR_CUB -> R.string.fact_polar_cub
    BuddySpecies.ARCTIC_FOX -> R.string.fact_arctic_fox
    BuddySpecies.TURTLE -> R.string.fact_turtle
    BuddySpecies.OTTER -> R.string.fact_otter
    BuddySpecies.CRAB -> R.string.fact_crab
    BuddySpecies.OWL -> R.string.fact_owl
    BuddySpecies.HEDGEHOG -> R.string.fact_hedgehog
    BuddySpecies.RACCOON -> R.string.fact_raccoon
    BuddySpecies.CAMEL -> R.string.fact_camel
    BuddySpecies.FENNEC_FOX -> R.string.fact_fennec_fox
    BuddySpecies.MEERKAT -> R.string.fact_meerkat
    BuddySpecies.RED_PANDA -> R.string.fact_red_panda
    BuddySpecies.ELEPHANT_CALF -> R.string.fact_elephant_calf
    BuddySpecies.PEACOCK -> R.string.fact_peacock
}

sealed interface AlbumUiEvent {
    data class SelectTab(val tab: AlbumTab) : AlbumUiEvent
    data class ClickSpecies(val species: BuddySpecies) : AlbumUiEvent
    data class EquipAccessory(val species: String, val accessory: BuddyAccessory) : AlbumUiEvent
    data object DismissSpeciesModal : AlbumUiEvent
    data object DismissCelebration : AlbumUiEvent
    data class GrantDebugBerries(val amount: Int) : AlbumUiEvent
    data object ToggleDebugPanel : AlbumUiEvent
}
