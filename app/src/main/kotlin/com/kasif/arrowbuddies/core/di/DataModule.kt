package com.kasif.arrowbuddies.core.di

import android.content.Context
import androidx.room.Room
import com.kasif.arrowbuddies.core.audio.DefaultSoundManager
import com.kasif.arrowbuddies.core.audio.SoundManager
import com.kasif.arrowbuddies.core.haptics.DefaultHapticsManager
import com.kasif.arrowbuddies.core.haptics.HapticsManager
import com.kasif.arrowbuddies.data.db.AppDatabase
import com.kasif.arrowbuddies.data.db.dao.PlaceholderDao
import com.kasif.arrowbuddies.data.repository.DefaultGameRepository
import com.kasif.arrowbuddies.data.repository.GameRepository
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
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun providePlaceholderDao(database: AppDatabase): PlaceholderDao {
        return database.placeholderDao()
    }

    @Provides
    @Singleton
    fun provideSoundManager(): SoundManager {
        return DefaultSoundManager()
    }
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
}
