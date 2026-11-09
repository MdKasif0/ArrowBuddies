package com.kasif.arrowbuddies.engine.generator

import com.kasif.arrowbuddies.engine.codec.LevelCodec
import com.kasif.arrowbuddies.engine.model.LevelDef
import com.kasif.arrowbuddies.engine.model.WorldSpecies
import com.kasif.arrowbuddies.engine.solver.LevelSolver
import com.kasif.arrowbuddies.engine.solver.SolveResult
import java.io.File
import kotlin.system.measureTimeMillis

/**
 * Bakes all 240 levels (8 worlds x 30 levels) deterministically into JSON packs,
 * validates every level via [LevelSolver], and outputs the difficulty report.
 */
object LevelPackBaker {

    const val TOTAL_WORLDS = 8
    const val LEVELS_PER_WORLD = 30

    /**
     * Constructs the [GenParams] for a given [world] and [level] following the sawtooth curve.
     */
    fun getParamsForLevel(world: Int, level: Int): GenParams {
        require(world in 1..TOTAL_WORLDS) { "Invalid world: $world" }
        require(level in 1..LEVELS_PER_WORLD) { "Invalid level: $level" }

        val seed = LevelGenerator.stableHash("ArrowBuddies-v1", world, level)
        val speciesPool = WorldSpecies.forWorld(world)

        val allowedMechanics = when (world) {
            1 -> setOf("rock")
            2 -> setOf("rock", "mud")
            3 -> setOf("rock", "turntable")
            4 -> setOf("rock", "cage")
            5 -> setOf("rock", "portal")
            6 -> setOf("rock", "sleepy")
            7 -> setOf("rock", "mud", "turntable", "cage")
            else -> setOf("rock", "mud", "turntable", "cage", "portal", "sleepy")
        }

        return when (level) {
            4 -> GenParams(seed, 4, 5, 4, allowedMechanics, 1, 12.0..25.0, speciesPool, world, level)
            5 -> GenParams(seed, 5, 5, 5, allowedMechanics, 1, 15.0..28.0, speciesPool, world, level)
            6 -> GenParams(seed, 5, 5, 6, allowedMechanics, 2, 18.0..30.0, speciesPool, world, level)
            7 -> GenParams(seed, 5, 5, 6, allowedMechanics, 2, 20.0..32.0, speciesPool, world, level)
            8 -> GenParams(seed, 5, 6, 7, allowedMechanics, 2, 22.0..34.0, speciesPool, world, level)
            9 -> GenParams(seed, 5, 6, 7, allowedMechanics, 2, 25.0..36.0, speciesPool, world, level)
            10 -> GenParams(seed, 6, 6, 8, allowedMechanics, 3, 28.0..38.0, speciesPool, world, level)

            // Breather 1
            11 -> GenParams(seed, 5, 5, 5, allowedMechanics, 1, 15.0..25.0, speciesPool, world, level)

            12 -> GenParams(seed, 5, 6, 7, allowedMechanics, 2, 25.0..35.0, speciesPool, world, level)
            13 -> GenParams(seed, 6, 6, 8, allowedMechanics, 2, 30.0..42.0, speciesPool, world, level)
            14 -> GenParams(seed, 6, 6, 9, allowedMechanics, 3, 35.0..48.0, speciesPool, world, level)

            // Big Rescue 1 (Golden Buddy)
            15 -> GenParams(seed, 6, 7, 13, allowedMechanics, 3, 45.0..62.0, speciesPool, world, level, isBigRescue = true)

            // Breather 2
            16 -> GenParams(seed, 5, 5, 6, allowedMechanics, 1, 20.0..30.0, speciesPool, world, level)

            17 -> GenParams(seed, 6, 6, 8, allowedMechanics, 2, 28.0..40.0, speciesPool, world, level)
            18 -> GenParams(seed, 6, 6, 9, allowedMechanics, 2, 32.0..45.0, speciesPool, world, level)
            19 -> GenParams(seed, 6, 7, 10, allowedMechanics, 3, 38.0..50.0, speciesPool, world, level)
            20 -> GenParams(seed, 6, 7, 11, allowedMechanics, 3, 42.0..55.0, speciesPool, world, level)
            21 -> GenParams(seed, 6, 7, 11, allowedMechanics, 3, 45.0..58.0, speciesPool, world, level)
            22 -> GenParams(seed, 7, 7, 12, allowedMechanics, 3, 48.0..62.0, speciesPool, world, level)
            23 -> GenParams(seed, 7, 7, 12, allowedMechanics, 4, 52.0..65.0, speciesPool, world, level)
            24 -> GenParams(seed, 7, 7, 13, allowedMechanics, 4, 55.0..68.0, speciesPool, world, level)

            // Breather 3
            25 -> GenParams(seed, 6, 6, 7, allowedMechanics, 2, 25.0..38.0, speciesPool, world, level)

            26 -> GenParams(seed, 7, 7, 13, allowedMechanics, 3, 50.0..65.0, speciesPool, world, level)
            27 -> GenParams(seed, 7, 8, 14, allowedMechanics, 4, 55.0..70.0, speciesPool, world, level)
            28 -> GenParams(seed, 7, 8, 15, allowedMechanics, 4, 60.0..75.0, speciesPool, world, level)
            29 -> GenParams(seed, 7, 8, 16, allowedMechanics, 4, 65.0..80.0, speciesPool, world, level)

            // Big Rescue Boss (Golden Buddy)
            else -> GenParams(seed, 7, 8, 18, allowedMechanics, 5, 70.0..88.0, speciesPool, world, level, isBigRescue = true)
        }
    }

    /**
     * Bakes all 240 levels, validates solvability, writes JSON files, and generates Markdown report.
     */
    fun bakeAll(
        outputDir: File = File("app/src/main/assets/levels"),
        reportFile: File = File("docs/level_report.md")
    ): Map<Int, List<Pair<LevelDef, SolveResult>>> {
        outputDir.mkdirs()
        reportFile.parentFile?.mkdirs()

        val results = mutableMapOf<Int, List<Pair<LevelDef, SolveResult>>>()
        val markdownReport = StringBuilder()

        markdownReport.appendLine("# ArrowBuddies — Level Pack & Difficulty Report")
        markdownReport.appendLine()
        markdownReport.appendLine("Deterministic generation of 240 levels across 8 worlds verified with `LevelSolver`.")
        markdownReport.appendLine()
        markdownReport.appendLine("| World | Level | ID | Grid | Buddies | Depth | DeadEnds | Mechanics | Score | Time (ms) |")
        markdownReport.appendLine("|---|---|---|---|---|---|---|---|---|---|")

        var totalSolveTimeMs = 0L
        var totalLevelsCount = 0

        for (world in 1..TOTAL_WORLDS) {
            val worldLevels = mutableListOf<LevelDef>()
            val worldPairs = mutableListOf<Pair<LevelDef, SolveResult>>()

            for (level in 1..LEVELS_PER_WORLD) {
                val levelDef = if (level in 1..3) {
                    HandcraftedTutorials.getTutorialLevel(world, level)
                } else {
                    val params = getParamsForLevel(world, level)
                    LevelGenerator.generate(params)
                }

                val solveResult = LevelSolver.solve(levelDef)
                check(solveResult.solvable) {
                    "Level ${levelDef.id} in World $world is not solvable!"
                }

                totalSolveTimeMs += solveResult.timeMs
                totalLevelsCount++

                worldLevels.add(levelDef)
                worldPairs.add(levelDef to solveResult)

                val mechanicsStr = if (solveResult.metrics.mechanicsUsed.isEmpty()) "none" else solveResult.metrics.mechanicsUsed.joinToString(",")
                val scoreStr = "%.1f".format(solveResult.metrics.difficultyScore)

                markdownReport.appendLine(
                    "| W$world | L$level | `${levelDef.id}` | ${levelDef.cols}x${levelDef.rows} | " +
                            "${solveResult.metrics.buddyCount} | ${solveResult.metrics.dependencyDepth} | " +
                            "${solveResult.metrics.deadEndMoves} | $mechanicsStr | $scoreStr | ${solveResult.timeMs} |"
                )
            }

            // Write world json file
            val worldFile = File(outputDir, "world_$world.json")
            val jsonContent = LevelCodec.encodeList(worldLevels)
            worldFile.writeText(jsonContent)

            results[world] = worldPairs
        }

        markdownReport.appendLine()
        markdownReport.appendLine("## Summary")
        markdownReport.appendLine("- Total levels: $totalLevelsCount")
        markdownReport.appendLine("- Total solver verification time: ${totalSolveTimeMs} ms")
        markdownReport.appendLine("- Average solve time per level: ${"%.2f".format(totalSolveTimeMs.toDouble() / totalLevelsCount)} ms")
        markdownReport.appendLine("- All 240 levels verified 100% solvable with deterministic solutions.")

        reportFile.writeText(markdownReport.toString())

        return results
    }

    @JvmStatic
    fun main(args: Array<String>) {
        val rootDir = if (args.isNotEmpty()) File(args[0]) else File(".")
        val assetsLevelsDir = File(rootDir, "app/src/main/assets/levels")
        val reportFile = File(rootDir, "docs/level_report.md")

        println("Baking 240 ArrowBuddies levels into: ${assetsLevelsDir.absolutePath}")
        val elapsed = measureTimeMillis {
            val results = bakeAll(assetsLevelsDir, reportFile)
            println("Baking complete! Generated ${results.values.flatten().size} levels across 8 worlds.")
        }
        println("Done in ${elapsed} ms. Report saved to: ${reportFile.absolutePath}")
    }
}
