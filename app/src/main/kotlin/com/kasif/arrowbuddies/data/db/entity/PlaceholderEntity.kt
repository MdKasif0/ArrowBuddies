package com.kasif.arrowbuddies.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "placeholder")
data class PlaceholderEntity(
    @PrimaryKey val id: String = "default"
)

