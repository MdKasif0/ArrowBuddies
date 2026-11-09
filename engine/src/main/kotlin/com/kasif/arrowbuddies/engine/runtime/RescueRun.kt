package com.kasif.arrowbuddies.engine.runtime

import com.kasif.arrowbuddies.engine.generator.GenParams
import com.kasif.arrowbuddies.engine.generator.LevelGenerator
import com.kasif.arrowbuddies.engine.model.LevelDef
import com.kasif.arrowbuddies.engine.model.WorldSpecies
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlin.math.min

/**
 * Endless procedural puzzle sequence with slowly rising difficulty.
 * Supports background prefetching on [Dispatchers.Default] for instantaneous transitions.
 */
class RescueRun(
    private val runSeed: Long = System.currentTimeMillis(),
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default
) {
    var currentStage: Int = 1
        private set

    private var currentLevel: LevelDef? = null
    private var prefetchedNextLevel: Deferred<LevelDef>? = null

    /**
     * Builds generation parameters for a given stage in this endless run.
     */
    fun getParamsForStage(stage: Int): GenParams {
        val stageSeed = LevelGenerator.stableHash("RescueRun-$runSeed", stage, stage * 13)
        val world = ((stage - 1) % 8) + 1
        val cols = min(4 + (stage / 5), 7)
        val rows = min(5 + (stage / 4), 8)
        val buddyCount = min(4 + (stage / 2), 16)
        val obstacleBudget = min(1 + (stage / 6), 4)

        // Slowly rising target score: starts at 15..25 and climbs toward 65..80
        val baseScore = (15.0 + (stage * 1.8)).coerceAtMost(70.0)
        val targetRange = baseScore..(baseScore + 15.0)

        val allowedMechanics = when {
            stage < 3 -> setOf("rock")
            stage < 6 -> setOf("rock", "mud")
            stage < 9 -> setOf("rock", "mud", "turntable")
            stage < 12 -> setOf("rock", "mud", "turntable", "cage")
            stage < 15 -> setOf("rock", "mud", "turntable", "cage", "portal")
            else -> setOf("rock", "mud", "turntable", "cage", "portal", "sleepy")
        }

        return GenParams(
            seed = stageSeed,
            cols = cols,
            rows = rows,
            buddyCount = buddyCount,
            allowedMechanics = allowedMechanics,
            obstacleBudget = obstacleBudget,
            targetScoreRange = targetRange,
            speciesPool = WorldSpecies.forWorld(world),
            world = world,
            index = stage,
            isBigRescue = (stage % 10 == 0)
        )
    }

    /**
     * Synchronously generates or returns the current stage level.
     */
    fun getCurrentLevel(): LevelDef {
        if (currentLevel == null) {
            currentLevel = LevelGenerator.generate(getParamsForStage(currentStage)).copy(
                id = "rescuerun-$currentStage",
                comment = "Rescue Run Stage $currentStage"
            )
        }
        return currentLevel!!
    }

    /**
     * Initiates asynchronous prefetching of the next stage level on [dispatcher].
     */
    fun prefetchNext(scope: CoroutineScope) {
        val nextStage = currentStage + 1
        prefetchedNextLevel = scope.async(dispatcher) {
            val params = getParamsForStage(nextStage)
            LevelGenerator.generate(params).copy(
                id = "rescuerun-$nextStage",
                comment = "Rescue Run Stage $nextStage"
            )
        }
    }

    /**
     * Advances to the next stage, awaiting the prefetched level if available.
     */
    suspend fun advanceToNext(scope: CoroutineScope): LevelDef {
        currentStage++
        val nextLevel = prefetchedNextLevel?.await() ?: scope.async(dispatcher) {
            LevelGenerator.generate(getParamsForStage(currentStage)).copy(
                id = "rescuerun-$currentStage",
                comment = "Rescue Run Stage $currentStage"
            )
        }.await()

        currentLevel = nextLevel
        prefetchedNextLevel = null

        // Trigger prefetching for stage + 1 immediately
        prefetchNext(scope)
        return nextLevel
    }
}
