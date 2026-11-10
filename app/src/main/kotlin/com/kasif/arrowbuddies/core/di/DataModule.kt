package com.kasif.arrowbuddies.core.di

import android.content.Context
import androidx.room.Room
import com.kasif.arrowbuddies.core.ads.AdMobAdsManager
import com.kasif.arrowbuddies.core.ads.AdsManager
import com.kasif.arrowbuddies.core.audio.DefaultSoundManager
import com.kasif.arrowbuddies.core.audio.SoundManager
import com.kasif.arrowbuddies.core.haptics.DefaultHapticsManager
import com.kasif.arrowbuddies.core.haptics.HapticsManager
import com.kasif.arrowbuddies.core.monetization.DefaultMonetizationManager
import com.kasif.arrowbuddies.core.monetization.MonetizationManager
import com.kasif.arrowbuddies.data.db.AppDatabase
import com.kasif.arrowbuddies.data.db.AppDatabaseMigrations
import com.kasif.arrowbuddies.data.db.dao.AchievementDao
import com.kasif.arrowbuddies.data.db.dao.DailyRecordDao
import com.kasif.arrowbuddies.data.db.dao.DecorDao
import com.kasif.arrowbuddies.data.db.dao.LevelProgressDao
import com.kasif.arrowbuddies.data.db.dao.PlaceholderDao
import com.kasif.arrowbuddies.data.db.dao.PlayerProfileDao
import com.kasif.arrowbuddies.data.db.dao.SpeciesProgressDao
import com.kasif.arrowbuddies.data.repository.DefaultGameRepository
import com.kasif.arrowbuddies.data.repository.DefaultLevelRepository
import com.kasif.arrowbuddies.data.repository.GameRepository
import com.kasif.arrowbuddies.data.repository.LevelRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "arrow_buddies.db"
        )
            .addMigrations(AppDatabaseMigrations.MIGRATION_1_2)
            // No destructive fallback on upgrade: a missing migration must fail loudly in testing
            // instead of silently wiping every player's progress. Downgrades only happen in dev.
            .fallbackToDestructiveMigrationOnDowngrade()
            .build()
    }

    @Provides
    fun providePlaceholderDao(database: AppDatabase): PlaceholderDao = database.placeholderDao()

    @Provides
    fun providePlayerProfileDao(database: AppDatabase): PlayerProfileDao = database.playerProfileDao()

    @Provides
    fun provideLevelProgressDao(database: AppDatabase): LevelProgressDao = database.levelProgressDao()

    @Provides
    fun provideSpeciesProgressDao(database: AppDatabase): SpeciesProgressDao = database.speciesProgressDao()

    @Provides
    fun provideDecorDao(database: AppDatabase): DecorDao = database.decorDao()

    @Provides
    fun provideDailyRecordDao(database: AppDatabase): DailyRecordDao = database.dailyRecordDao()

    @Provides
    fun provideAchievementDao(database: AppDatabase): AchievementDao = database.achievementDao()

    @Provides
    @Singleton
    fun provideSoundManager(
        @ApplicationContext context: Context
    ): SoundManager = DefaultSoundManager(context)
}

@Module
@InstallIn(SingletonComponent::class)
abstract class BindingModule {

    @Binds
    @Singleton
    abstract fun bindGameRepository(
        impl: DefaultGameRepository
    ): GameRepository

    @Binds
    @Singleton
    abstract fun bindHapticsManager(
        impl: DefaultHapticsManager
    ): HapticsManager

    @Binds
    @Singleton
    abstract fun bindLevelRepository(
        impl: DefaultLevelRepository
    ): LevelRepository

    @Binds
    @Singleton
    abstract fun bindMonetizationManager(
        impl: DefaultMonetizationManager
    ): MonetizationManager

    @Binds
    @Singleton
    abstract fun bindAdsManager(
        impl: AdMobAdsManager
    ): AdsManager
}
