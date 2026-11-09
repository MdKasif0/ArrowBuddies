package com.kasif.arrowbuddies.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kasif.arrowbuddies.data.db.entity.LevelProgress
import kotlinx.coroutines.flow.Flow

@Dao
interface LevelProgressDao {
    @Query("SELECT * FROM level_progress WHERE levelId = :levelId LIMIT 1")
    fun getLevelProgress(levelId: String): Flow<LevelProgress?>

    @Query("SELECT * FROM level_progress WHERE levelId = :levelId LIMIT 1")
    suspend fun getLevelProgressOnce(levelId: String): LevelProgress?

    @Query("SELECT * FROM level_progress")
    fun getAllProgress(): Flow<List<LevelProgress>>

    @Query("SELECT * FROM level_progress")
    suspend fun getAllProgressOnce(): List<LevelProgress>

    @Query("SELECT bestStars FROM level_progress WHERE levelId = :levelId LIMIT 1")
    fun getLevelStars(levelId: String): Flow<Int?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(progress: LevelProgress)
}
