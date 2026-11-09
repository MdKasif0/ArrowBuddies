package com.kasif.arrowbuddies.domain.usecase

import androidx.room.withTransaction
import com.kasif.arrowbuddies.core.model.AchievementType
import com.kasif.arrowbuddies.data.db.AppDatabase
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BuyDecorUseCase @Inject constructor(
    private val database: AppDatabase
) {
    suspend operator fun invoke(decorId: String): Boolean = database.withTransaction {
        val profileDao = database.playerProfileDao()
        val decorDao = database.decorDao()
        val achievementDao = database.achievementDao()

        val decor = decorDao.getDecorItem(decorId) ?: return@withTransaction false
        if (decor.owned) return@withTransaction true

        val profile = profileDao.getPlayerProfileOnce() ?: return@withTransaction false
        if (profile.berries < decor.costBerries) return@withTransaction false

        profileDao.insertOrUpdate(profile.copy(berries = profile.berries - decor.costBerries))
        decorDao.markOwned(decorId)

        // Check Decorator achievement (own all 8 decorations)
        val allDecor = decorDao.getAllDecorItemsOnce()
        val ownedCount = allDecor.count { it.owned || it.id == decorId }
        achievementDao.updateProgress(AchievementType.DECORATOR.id, ownedCount)
        if (ownedCount >= 8) {
            achievementDao.unlock(AchievementType.DECORATOR.id)
        }

        true
    }
}
