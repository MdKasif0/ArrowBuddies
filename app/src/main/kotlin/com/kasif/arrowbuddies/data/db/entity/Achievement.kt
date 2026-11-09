package com.kasif.arrowbuddies.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "achievement")
data class Achievement(
    @PrimaryKey val id: String,
    val unlockedAt: Long = 0L,
    val progress: Int = 0,
    val maxProgress: Int = 1
) {
    val isUnlocked: Boolean get() = unlockedAt > 0L
}

