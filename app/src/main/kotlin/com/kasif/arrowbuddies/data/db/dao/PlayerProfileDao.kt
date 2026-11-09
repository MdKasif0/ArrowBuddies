package com.kasif.arrowbuddies.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.kasif.arrowbuddies.data.db.entity.PlayerProfile
import kotlinx.coroutines.flow.Flow

@Dao
interface PlayerProfileDao {
    @Query("SELECT * FROM player_profile WHERE id = 1 LIMIT 1")
    fun getPlayerProfile(): Flow<PlayerProfile?>

    @Query("SELECT * FROM player_profile WHERE id = 1 LIMIT 1")
    suspend fun getPlayerProfileOnce(): PlayerProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(profile: PlayerProfile)

    @Update
    suspend fun update(profile: PlayerProfile)

    @Query("UPDATE player_profile SET berries = berries + :amount WHERE id = 1")
    suspend fun addBerries(amount: Int)

    @Query("UPDATE player_profile SET berries = berries - :amount WHERE id = 1 AND berries >= :amount")
    suspend fun spendBerries(amount: Int): Int

    @Query("UPDATE player_profile SET hints = hints - 1 WHERE id = 1 AND hints > 0")
    suspend fun useHint(): Int

    @Query("UPDATE player_profile SET undos = undos - 1 WHERE id = 1 AND undos > 0")
    suspend fun useUndo(): Int
}

