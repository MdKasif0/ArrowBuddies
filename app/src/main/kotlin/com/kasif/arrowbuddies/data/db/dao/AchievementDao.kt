package com.kasif.arrowbuddies.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kasif.arrowbuddies.data.db.entity.Achievement
import kotlinx.coroutines.flow.Flow

@Dao
interface AchievementDao {
    @Query("SELECT * FROM achievement")
    fun getAllAchievements(): Flow<List<Achievement>>

    @Query("SELECT * FROM achievement")
    suspend fun getAllAchievementsOnce(): List<Achievement>

    @Query("SELECT * FROM achievement WHERE id = :id LIMIT 1")
    suspend fun getAchievementOnce(id: String): Achievement?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(achievement: Achievement)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(achievements: List<Achievement>)

    @Query("UPDATE achievement SET unlockedAt = :timestamp, progress = maxProgress WHERE id = :id AND unlockedAt = 0")
    suspend fun unlock(id: String, timestamp: Long = System.currentTimeMillis()): Int

    @Query("UPDATE achievement SET progress = :progress WHERE id = :id AND unlockedAt = 0")
    suspend fun updateProgress(id: String, progress: Int)
}

