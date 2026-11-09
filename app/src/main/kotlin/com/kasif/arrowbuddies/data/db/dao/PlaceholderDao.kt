package com.kasif.arrowbuddies.data.db.dao

import androidx.room.Dao
import androidx.room.Query

@Dao
interface PlaceholderDao {
    @Query("SELECT COUNT(*) FROM placeholder")
    suspend fun count(): Int
}

