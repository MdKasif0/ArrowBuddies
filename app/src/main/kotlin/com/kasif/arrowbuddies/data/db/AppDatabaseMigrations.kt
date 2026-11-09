package com.kasif.arrowbuddies.data.db

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object AppDatabaseMigrations {
    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `player_profile` (
                    `id` INTEGER NOT NULL,
                    `berries` INTEGER NOT NULL,
                    `hints` INTEGER NOT NULL,
                    `undos` INTEGER NOT NULL,
                    `totalRescued` INTEGER NOT NULL,
                    `tutorialDone` INTEGER NOT NULL,
                    `currentLevelIndex` INTEGER NOT NULL,
                    `highestUnlockedLevel` INTEGER NOT NULL,
                    `streakDays` INTEGER NOT NULL,
                    `lastStreakDate` TEXT,
                    `lastTreatTimeDate` TEXT,
                    PRIMARY KEY(`id`)
                )
                """.trimIndent()
            )

            // Insert initial default player profile
            db.execSQL(
                """
                INSERT OR IGNORE INTO `player_profile` 
                (`id`, `berries`, `hints`, `undos`, `totalRescued`, `tutorialDone`, `currentLevelIndex`, `highestUnlockedLevel`, `streakDays`)
                VALUES (1, 100, 3, 3, 0, 0, 1, 1, 0)
                """.trimIndent()
            )

            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `level_progress` (
                    `levelId` TEXT NOT NULL,
                    `bestStars` INTEGER NOT NULL,
                    `bestMistakes` INTEGER NOT NULL,
                    `completedAt` INTEGER NOT NULL,
                    PRIMARY KEY(`levelId`)
                )
                """.trimIndent()
            )

            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `species_progress` (
                    `species` TEXT NOT NULL,
                    `rescuedCount` INTEGER NOT NULL,
                    `tier` TEXT NOT NULL,
                    `equippedAccessory` TEXT NOT NULL,
                    `firstRescuedAt` INTEGER NOT NULL,
                    PRIMARY KEY(`species`)
                )
                """.trimIndent()
            )

            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `decor_item` (
                    `id` TEXT NOT NULL,
                    `owned` INTEGER NOT NULL,
                    `slot` TEXT NOT NULL,
                    `zoneId` INTEGER NOT NULL,
                    `costBerries` INTEGER NOT NULL,
                    PRIMARY KEY(`id`)
                )
                """.trimIndent()
            )

            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `daily_record` (
                    `date` TEXT NOT NULL,
                    `completed` INTEGER NOT NULL,
                    `stars` INTEGER NOT NULL,
                    `mistakes` INTEGER NOT NULL,
                    `completedAt` INTEGER NOT NULL,
                    PRIMARY KEY(`date`)
                )
                """.trimIndent()
            )

            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `achievement` (
                    `id` TEXT NOT NULL,
                    `unlockedAt` INTEGER NOT NULL,
                    `progress` INTEGER NOT NULL,
                    `maxProgress` INTEGER NOT NULL,
                    PRIMARY KEY(`id`)
                )
                """.trimIndent()
            )
        }
    }
}
