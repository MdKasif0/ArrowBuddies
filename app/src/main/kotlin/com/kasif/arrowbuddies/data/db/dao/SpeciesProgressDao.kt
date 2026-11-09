package com.kasif.arrowbuddies.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kasif.arrowbuddies.core.model.BuddyAccessory
import com.kasif.arrowbuddies.data.db.entity.SpeciesProgress
import kotlinx.coroutines.flow.Flow

@Dao
interface SpeciesProgressDao {
    @Query("SELECT * FROM species_progress")
    fun getAllSpeciesProgress(): Flow<List<SpeciesProgress>>

    @Query("SELECT * FROM species_progress")
    suspend fun getAllSpeciesProgressOnce(): List<SpeciesProgress>

    @Query("SELECT * FROM species_progress WHERE species = :species LIMIT 1")
    fun getSpeciesProgress(species: String): Flow<SpeciesProgress?>

    @Query("SELECT * FROM species_progress WHERE species = :species LIMIT 1")
    suspend fun getSpeciesProgressOnce(species: String): SpeciesProgress?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(progress: SpeciesProgress)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateAll(progressList: List<SpeciesProgress>)

    @Query("UPDATE species_progress SET equippedAccessory = :accessory WHERE species = :species")
    suspend fun equipAccessory(species: String, accessory: BuddyAccessory)
}
