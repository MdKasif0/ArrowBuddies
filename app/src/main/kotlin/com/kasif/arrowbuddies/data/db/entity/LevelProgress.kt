package com.kasif.arrowbuddies.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "level_progress")
data class LevelProgress(
    @PrimaryKey val levelId: String,
    val bestStars: Int,
    val bestMistakes: Int,
    val completedAt: Long = System.currentTimeMillis()
)

