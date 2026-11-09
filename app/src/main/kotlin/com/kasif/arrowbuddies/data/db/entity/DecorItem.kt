package com.kasif.arrowbuddies.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "decor_item")
data class DecorItem(
    @PrimaryKey val id: String, // e.g. "decor_pond_1"
    val owned: Boolean = false,
    val slot: String, // e.g. "z1_s1"
    val zoneId: Int, // 1 to 8
    val costBerries: Int
)

