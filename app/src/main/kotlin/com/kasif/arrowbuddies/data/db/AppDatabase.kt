package com.kasif.arrowbuddies.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.kasif.arrowbuddies.data.db.dao.AchievementDao
import com.kasif.arrowbuddies.data.db.dao.DailyRecordDao
import com.kasif.arrowbuddies.data.db.dao.DecorDao
import com.kasif.arrowbuddies.data.db.dao.LevelProgressDao
import com.kasif.arrowbuddies.data.db.dao.PlaceholderDao
import com.kasif.arrowbuddies.data.db.dao.PlayerProfileDao
import com.kasif.arrowbuddies.data.db.dao.SpeciesProgressDao
import com.kasif.arrowbuddies.data.db.entity.Achievement
import com.kasif.arrowbuddies.data.db.entity.DailyRecord
import com.kasif.arrowbuddies.data.db.entity.DecorItem
import com.kasif.arrowbuddies.data.db.entity.LevelProgress
import com.kasif.arrowbuddies.data.db.entity.PlaceholderEntity
import com.kasif.arrowbuddies.data.db.entity.PlayerProfile
import com.kasif.arrowbuddies.data.db.entity.SpeciesProgress

@Database(
    entities = [
        PlaceholderEntity::class,
        PlayerProfile::class,
        LevelProgress::class,
        SpeciesProgress::class,
        DecorItem::class,
        DailyRecord::class,
        Achievement::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun placeholderDao(): PlaceholderDao
    abstract fun playerProfileDao(): PlayerProfileDao
    abstract fun levelProgressDao(): LevelProgressDao
    abstract fun speciesProgressDao(): SpeciesProgressDao
    abstract fun decorDao(): DecorDao
    abstract fun dailyRecordDao(): DailyRecordDao
    abstract fun achievementDao(): AchievementDao
}
