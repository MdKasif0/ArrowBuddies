package com.kasif.arrowbuddies.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.kasif.arrowbuddies.core.model.BuddyAccessory
import com.kasif.arrowbuddies.core.model.FriendshipTier

@Entity(tableName = "species_progress")
data class SpeciesProgress(
    @PrimaryKey val species: String, // BuddySpecies enum name, e.g. "BUNNY"
    val rescuedCount: Int = 0,
    val tier: FriendshipTier = FriendshipTier.LOCKED,
    val equippedAccessory: BuddyAccessory = BuddyAccessory.NONE,
    val firstRescuedAt: Long = 0L
)

