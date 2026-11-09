package com.kasif.arrowbuddies.engine.runtime

import com.kasif.arrowbuddies.engine.generator.GenParams
import com.kasif.arrowbuddies.engine.generator.LevelGenerator
import com.kasif.arrowbuddies.engine.model.LevelDef
import com.kasif.arrowbuddies.engine.model.WorldSpecies
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Daily Rescue puzzle generator.
 *
 * Everyone plays the exact same level each calendar day, seeded from the date
 * in the Asia/Kolkata timezone (e.g., 20260415).
 */
object DailyRescue {

    val TIME_ZONE: ZoneId = ZoneId.of("Asia/Kolkata")
    private val DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd")

    fun getTodaySeed(zoneId: ZoneId = TIME_ZONE): Long {
        val dateStr = LocalDate.now(zoneId).format(DATE_FORMATTER)
        return dateStr.toLong()
    }

    fun getSeedForDate(date: LocalDate): Long {
        return date.format(DATE_FORMATTER).toLong()
    }

    /**
     * Generates the deterministic Daily Rescue level for [date].
     */
    fun generate(date: LocalDate = LocalDate.now(TIME_ZONE)): LevelDef {
        val seed = getSeedForDate(date)
        val dayOfYear = date.dayOfYear
        val world = ((dayOfYear - 1) % 8) + 1 // Cycles through worlds 1..8 daily

        val params = GenParams(
            seed = seed,
            cols = 6,
            rows = 6,
            buddyCount = 8,
            allowedMechanics = when (world) {
                1 -> setOf("rock")
                2 -> setOf("rock", "mud")
                3 -> setOf("rock", "turntable")
                4 -> setOf("rock", "cage")
                5 -> setOf("rock", "portal")
                6 -> setOf("rock", "sleepy")
                7 -> setOf("rock", "mud", "turntable")
                else -> setOf("rock", "mud", "turntable", "cage", "sleepy")
            },
            obstacleBudget = 3,
            targetScoreRange = 25.0..45.0,
            speciesPool = WorldSpecies.forWorld(world),
            world = world,
            index = dayOfYear % 30 + 1
        )

        val level = LevelGenerator.generate(params)
        return level.copy(
            id = "daily-${date.format(DATE_FORMATTER)}",
            comment = "Daily Rescue for $date"
        )
    }
}
