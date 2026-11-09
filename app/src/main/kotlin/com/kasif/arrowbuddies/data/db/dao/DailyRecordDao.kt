package com.kasif.arrowbuddies.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kasif.arrowbuddies.data.db.entity.DailyRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyRecordDao {
    @Query("SELECT * FROM daily_record WHERE date = :date LIMIT 1")
    fun getDailyRecord(date: String): Flow<DailyRecord?>

    @Query("SELECT * FROM daily_record WHERE date = :date LIMIT 1")
    suspend fun getDailyRecordOnce(date: String): DailyRecord?

    @Query("SELECT * FROM daily_record ORDER BY date DESC")
    fun getAllRecords(): Flow<List<DailyRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(record: DailyRecord)
}
