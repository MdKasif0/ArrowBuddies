package com.kasif.arrowbuddies.domain.usecase

import com.kasif.arrowbuddies.core.model.BuddyAccessory
import com.kasif.arrowbuddies.data.db.AppDatabase
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EquipAccessoryUseCase @Inject constructor(
    private val database: AppDatabase
) {
    suspend operator fun invoke(species: String, accessory: BuddyAccessory): Boolean {
        val speciesDao = database.speciesProgressDao()
        val progress = speciesDao.getSpeciesProgressOnce(species) ?: return false

        // Check if unlocked for tier
        val canEquip = when (accessory) {
            BuddyAccessory.NONE -> true
            BuddyAccessory.BOW -> progress.tier >= com.kasif.arrowbuddies.core.model.FriendshipTier.BRONZE
            BuddyAccessory.SCARF -> progress.tier >= com.kasif.arrowbuddies.core.model.FriendshipTier.SILVER
            BuddyAccessory.CROWN -> progress.tier >= com.kasif.arrowbuddies.core.model.FriendshipTier.GOLD
        }

        if (canEquip) {
            speciesDao.equipAccessory(species, accessory)
            return true
        }
        return false
    }
}
