package com.kasif.arrowbuddies.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_record")
data class DailyRecord(
    @PrimaryKey val date: String, // "yyyyMMdd"
    val completed: Boolean = false,
    val stars: Int = 0,
    val mistakes: Int = 0,
    val completedAt: Long = 0L
)

