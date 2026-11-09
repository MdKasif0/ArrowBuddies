package com.kasif.arrowbuddies.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.kasif.arrowbuddies.data.db.dao.PlaceholderDao
import com.kasif.arrowbuddies.data.db.entity.PlaceholderEntity

@Database(entities = [PlaceholderEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun placeholderDao(): PlaceholderDao
}

