package com.kasif.arrowbuddies.domain.usecase

import androidx.room.withTransaction
import com.kasif.arrowbuddies.data.db.AppDatabase
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SpendBerriesUseCase @Inject constructor(
    private val database: AppDatabase
) {
    suspend operator fun invoke(amount: Int): Boolean = database.withTransaction {
        val profileDao = database.playerProfileDao()
        val profile = profileDao.getPlayerProfileOnce() ?: return@withTransaction false
        if (profile.berries >= amount) {
            profileDao.insertOrUpdate(profile.copy(berries = profile.berries - amount))
            true
        } else {
            false
        }
    }
}
