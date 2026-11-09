package com.kasif.arrowbuddies.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kasif.arrowbuddies.data.db.entity.DecorItem
import kotlinx.coroutines.flow.Flow

@Dao
interface DecorDao {
    @Query("SELECT * FROM decor_item")
    fun getAllDecorItems(): Flow<List<DecorItem>>

    @Query("SELECT * FROM decor_item")
    suspend fun getAllDecorItemsOnce(): List<DecorItem>

    @Query("SELECT * FROM decor_item WHERE id = :id LIMIT 1")
    suspend fun getDecorItem(id: String): DecorItem?

    @Query("SELECT * FROM decor_item WHERE zoneId = :zoneId")
    fun getDecorItemsForZone(zoneId: Int): Flow<List<DecorItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(item: DecorItem)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(items: List<DecorItem>)

    @Query("UPDATE decor_item SET owned = 1 WHERE id = :id")
    suspend fun markOwned(id: String)
}

