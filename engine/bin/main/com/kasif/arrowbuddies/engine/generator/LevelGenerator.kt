package com.kasif.arrowbuddies.engine.generator

import com.kasif.arrowbuddies.engine.model.Buddy
import com.kasif.arrowbuddies.engine.model.Direction
import com.kasif.arrowbuddies.engine.model.LevelDef
import com.kasif.arrowbuddies.engine.model.Pos
import com.kasif.arrowbuddies.engine.model.Tile
import com.kasif.arrowbuddies.engine.model.WorldSpecies
import com.kasif.arrowbuddies.engine.solver.LevelSolver
import com.kasif.arrowbuddies.engine.solver.SolveResult
import kotlin.random.Random

/**
 * Deterministic procedural level generator using reverse construction.
 *
 * Placed buddies build a dependency DAG in reverse time, guaranteeing solvability.
 * Validates candidates against [LevelSolver] metrics to match target difficulty.
 */
object LevelGenerator {

    private const val MAX_ATTEMPTS = 200

    /**
     * Deterministic 64-bit FNV-1a hash for level seed derivation.
     */
    fun stableHash(prefix: String, world: Int, level: Int): Long {
        var h = 0xcbf29ce484222325UL.toLong()
        val prime = 0x100000001b3UL.toLong()
        for (ch in prefix) {
            h = (h xor ch.code.toLong()) * prime
        }
        h = (h xor world.toLong()) * prime
        h = (h xor level.toLong()) * prime
        return h
    }

    /**
     * Generates a fully validated, solvable [LevelDef] based on [params].
     *
     * @throws IllegalStateException if a valid level matching criteria cannot be found within [MAX_ATTEMPTS].
     */
    fun generate(params: GenParams): LevelDef {
        var bestCandidate: Pair<LevelDef, SolveResult>? = null
        var bestScoreDiff = Double.MAX_VALUE

        for (attempt in 0 until MAX_ATTEMPTS) {
            // Deterministically derive a seed for each attempt
            val attemptSeed = params.seed + (attempt * 10007L)
            val random = Random(attemptSeed)

            val candidate = generateCandidate(params, random, attempt) ?: continue
            val solveResult = LevelSolver.solve(candidate)

            if (!solveResult.solvable) continue
            if (solveResult.metrics.buddyCount != params.buddyCount) continue

            // 1. Initial free moves check (no more than 50% free at start, except for very small buddy counts)
            val maxFree = maxOf(2, (params.buddyCount * 0.50).toInt())
            if (solveResult.metrics.initialFreeMoves > maxFree) continue

            // 2. Dead-end check (reject if dead ends / buddyCount > 0.40)
            val deadEndRatio = solveResult.metrics.deadEndMoves.toDouble() / params.buddyCount
            if (deadEndRatio > 0.40) continue

            // 3. Spatial bounding box check (prevent all buddies clustering in one corner)
            if (!hasGoodSpread(candidate.buddies, params.cols, params.rows)) continue

            val score = solveResult.metrics.difficultyScore
            if (score in params.targetScoreRange) {
                // Perfect hit within target difficulty range!
                return candidate
            }

            // Track closest candidate in case no attempt lands perfectly within range
            val targetCenter = (params.targetScoreRange.start + params.targetScoreRange.endInclusive) / 2.0
            val diff = kotlin.math.abs(score - targetCenter)
            if (diff < bestScoreDiff) {
                bestScoreDiff = diff
                bestCandidate = candidate to solveResult
            }
        }

        // If target range wasn't hit exactly, use closest valid candidate
        bestCandidate?.let {
            return it.first
        }

        throw IllegalStateException(
            "Failed to generate valid level for params: cols=${params.cols}, rows=${params.rows}, " +
                    "buddies=${params.buddyCount}, mechanics=${params.allowedMechanics}, target=${params.targetScoreRange}"
        )
    }

    private fun generateCandidate(params: GenParams, random: Random, attempt: Int): LevelDef? {
        val cols = params.cols
        val rows = params.rows
        val tiles = mutableMapOf<Pos, Tile>()

        // 1. Place terrain obstacles based on allowedMechanics and obstacleBudget
        placeTerrain(params, tiles, random)

        // 2. Place buddies in reverse order
        val placedBuddies = mutableListOf<Buddy>()
        val occupiedCells = mutableSetOf<Pos>()
        val candidateTrajectories = mutableMapOf<Int, List<Pos>>() // buddyIndex -> path of cells traversed

        for (i in 0 until params.buddyCount) {
            val isFirst = (i == 0)
            var placed = false

            // 80% chance to block an existing buddy's path, 20% free space (or if first buddy)
            val tryBlock = !isFirst && (random.nextDouble() < 0.80)

            if (tryBlock && candidateTrajectories.isNotEmpty()) {
                // Collect empty candidate cells along escape trajectories of already placed buddies
                val candidatePositions = candidateTrajectories.values.flatten()
                    .filter { pos -> pos !in occupiedCells && tiles[pos] !is Tile.Rock && tiles[pos] !is Tile.Turntable && tiles[pos] !is Tile.Portal }
                    .distinct()
                    .shuffled(random)

                for (pos in candidatePositions) {
                    val directions = Direction.entries.shuffled(random)
                    for (dir in directions) {
                        val path = traceEscapePath(pos, dir, cols, rows, tiles, occupiedCells)
                        if (path != null) {
                            val buddy = createBuddy(i, pos, dir, params, random)
                            placedBuddies.add(buddy)
                            occupiedCells.add(pos)
                            candidateTrajectories[i] = path
                            placed = true
                            break
                        }
                    }
                    if (placed) break
                }
            }

            if (!placed) {
                // Place in any available free cell with an unobstructed escape path
                val allEmptyCells = (0 until cols).flatMap { x ->
                    (0 until rows).map { y -> Pos(x, y) }
                }.filter { pos ->
                    pos !in occupiedCells && tiles[pos] !is Tile.Rock && tiles[pos] !is Tile.Turntable && tiles[pos] !is Tile.Portal
                }.shuffled(random)

                for (pos in allEmptyCells) {
                    val directions = Direction.entries.shuffled(random)
                    for (dir in directions) {
                        val path = traceEscapePath(pos, dir, cols, rows, tiles, occupiedCells)
                        if (path != null) {
                            val buddy = createBuddy(i, pos, dir, params, random)
                            placedBuddies.add(buddy)
                            occupiedCells.add(pos)
                            candidateTrajectories[i] = path
                            placed = true
                            break
                        }
                    }
                    if (placed) break
                }
            }

            if (!placed) {
                return null // Unable to place buddy i, retry attempt
            }
        }

        // 3. Post-process mechanics (Cage+Key, Sleepy, Golden Buddy)
        val finalizedBuddies = applyMechanics(placedBuddies, params, random)

        return LevelDef(
            id = "w${params.world}-l${params.index.toString().padStart(2, '0')}",
            cols = cols,
            rows = rows,
            tiles = tiles,
            buddies = finalizedBuddies,
            world = params.world,
            index = params.index
        )
    }

    private fun placeTerrain(params: GenParams, tiles: MutableMap<Pos, Tile>, random: Random) {
        val cols = params.cols
        val rows = params.rows
        val allowed = params.allowedMechanics

        // Reserve center / interior positions for obstacles
        val interiorCells = (1 until cols - 1).flatMap { x ->
            (1 until rows - 1).map { y -> Pos(x, y) }
        }.shuffled(random).toMutableList()

        var budget = params.obstacleBudget

        // 1. Turntable (World 3+)
        if ("turntable" in allowed && interiorCells.isNotEmpty() && budget > 0) {
            val count = minOf(random.nextInt(1, 3), interiorCells.size)
            for (k in 0 until count) {
                val pos = interiorCells.removeAt(0)
                tiles[pos] = Tile.Turntable
                budget--
            }
        }

        // 2. Mud (World 2+)
        if ("mud" in allowed && interiorCells.isNotEmpty() && budget > 0) {
            val count = minOf(random.nextInt(1, 3), interiorCells.size)
            for (k in 0 until count) {
                val pos = interiorCells.removeAt(0)
                tiles[pos] = Tile.Mud
                budget--
            }
        }

        // 3. Portals (World 5+)
        if ("portal" in allowed && interiorCells.size >= 2 && budget >= 2) {
            val p1 = interiorCells.removeAt(0)
            val p2Index = interiorCells.indexOfFirst { it.manhattanDistance(p1) >= 2 }
            if (p2Index >= 0) {
                val p2 = interiorCells.removeAt(p2Index)
                tiles[p1] = Tile.Portal(1)
                tiles[p2] = Tile.Portal(1)
                budget -= 2
            }
        }

        // 4. Rocks (World 1+)
        if ("rock" in allowed && budget > 0) {
            val rockCount = minOf(budget, random.nextInt(1, maxOf(2, budget + 1)))
            val rockCandidates = (0 until cols).flatMap { x ->
                (0 until rows).map { y -> Pos(x, y) }
            }.filter { pos ->
                // Avoid placing rocks on all 4 corners or blocking exits entirely
                pos !in tiles && !isCorner(pos, cols, rows)
            }.shuffled(random)

            var placedRocks = 0
            for (pos in rockCandidates) {
                if (placedRocks >= rockCount) break
                // Avoid 2x2 rock clusters
                if (!creates2x2Cluster(pos, tiles)) {
                    tiles[pos] = Tile.Rock
                    placedRocks++
                }
            }
        }
    }

    private fun isCorner(pos: Pos, cols: Int, rows: Int): Boolean {
        return (pos.x == 0 || pos.x == cols - 1) && (pos.y == 0 || pos.y == rows - 1)
    }

    private fun creates2x2Cluster(pos: Pos, tiles: Map<Pos, Tile>): Boolean {
        val offsets = listOf(
            listOf(Pos(-1, -1), Pos(-1, 0), Pos(0, -1)),
            listOf(Pos(1, -1), Pos(1, 0), Pos(0, -1)),
            listOf(Pos(-1, 1), Pos(-1, 0), Pos(0, 1)),
            listOf(Pos(1, 1), Pos(1, 0), Pos(0, 1))
        )
        return offsets.any { trio ->
            trio.all { offset -> tiles[pos + offset] is Tile.Rock }
        }
    }

    /**
     * Traces the escape path of a buddy from [start] facing [dir].
     * Returns list of non-start cells visited if buddy successfully escapes without hitting [occupiedCells] or looping.
     * Returns null if path bonks on rocks, hits an existing blocker, or enters a cycle.
     */
    private fun traceEscapePath(
        start: Pos,
        dir: Direction,
        cols: Int,
        rows: Int,
        tiles: Map<Pos, Tile>,
        occupiedCells: Set<Pos>
    ): List<Pos>? {
        if (start in occupiedCells || tiles[start] is Tile.Rock) return null

        val path = mutableListOf<Pos>()
        val visited = mutableSetOf<Pair<Pos, Direction>>()
        var currPos = start
        var currDir = dir

        var steps = 0
        while (steps < 40) {
            steps++
            if (!visited.add(currPos to currDir)) {
                return null // Loop detected
            }

            val nextPos = currPos + currDir
            // Check if step exits the grid
            if (nextPos.x !in 0 until cols || nextPos.y !in 0 until rows) {
                // Escaped board!
                return path
            }

            // Collided with another buddy
            if (nextPos in occupiedCells) {
                return null
            }

            when (val tile = tiles[nextPos] ?: Tile.Empty) {
                Tile.Rock -> return null // Bonk on rock
                Tile.Empty -> {
                    path.add(nextPos)
                    currPos = nextPos
                }
                Tile.Mud -> {
                    path.add(nextPos)
                    currPos = nextPos
                    // Buddy pauses on mud, then continues in same direction on next tap
                }
                Tile.Turntable -> {
                    path.add(nextPos)
                    currPos = nextPos
                    currDir = currDir.rotateClockwise()
                }
                is Tile.Portal -> {
                    path.add(nextPos)
                    val twin = tiles.entries.firstOrNull { it.value is Tile.Portal && (it.value as Tile.Portal).pairId == tile.pairId && it.key != nextPos }?.key
                        ?: return null
                    if (twin in occupiedCells) return null
                    path.add(twin)
                    currPos = twin
                }
            }
        }
        return null
    }

    private fun createBuddy(
        index: Int,
        pos: Pos,
        dir: Direction,
        params: GenParams,
        random: Random
    ): Buddy {
        val speciesPool = if (params.speciesPool.isNotEmpty()) params.speciesPool else WorldSpecies.forWorld(params.world)
        // Alternate/distribute species evenly
        val species = speciesPool[index % speciesPool.size]
        return Buddy(
            id = "${species.lowercase()}_${index + 1}",
            species = species,
            pos = pos,
            dir = dir
        )
    }

    private fun applyMechanics(
        buddies: List<Buddy>,
        params: GenParams,
        random: Random
    ): List<Buddy> {
        val result = buddies.toMutableList()
        val allowed = params.allowedMechanics

        // In reverse order: buddy at higher index was placed LATER, so in forward game it escapes EARLIER.
        // Therefore: key buddy must be at higher index than caged buddy!
        if ("cage" in allowed && result.size >= 4) {
            val cageColors = listOf("gold", "silver")
            val pairsCount = if (result.size >= 8) minOf(2, cageColors.size) else 1

            for (c in 0 until pairsCount) {
                val color = cageColors[c]
                // Pick caged buddy among first half (escapes later)
                val cagedIdx = random.nextInt(0, result.size / 2)
                // Pick key buddy among second half (escapes earlier)
                val keyIdx = random.nextInt(result.size / 2, result.size)

                if (cagedIdx != keyIdx && result[cagedIdx].cageColor == null && result[keyIdx].keyColor == null) {
                    result[cagedIdx] = result[cagedIdx].copy(cageColor = color)
                    result[keyIdx] = result[keyIdx].copy(keyColor = color)
                }
            }
        }

        // Sleepy mechanic (World 6+)
        // Buddy at index i can be sleepy if there is an adjacent neighbor at index j > i (neighbor escapes earlier)
        if ("sleepy" in allowed && result.size >= 3) {
            val sleepyCandidates = mutableListOf<Int>()
            for (i in 0 until result.size - 1) {
                val hasEarlierEscapingNeighbor = (i + 1 until result.size).any { j ->
                    result[i].pos.manhattanDistance(result[j].pos) == 1
                }
                if (hasEarlierEscapingNeighbor && result[i].cageColor == null && result[i].keyColor == null) {
                    sleepyCandidates.add(i)
                }
            }

            if (sleepyCandidates.isNotEmpty()) {
                val pick = sleepyCandidates.random(random)
                result[pick] = result[pick].copy(sleepy = true)
            }
        }

        // Golden Buddy for Big Rescue levels (15 & 30)
        if (params.isBigRescue || params.index == 15 || params.index == 30) {
            // Pick the buddy closest to the center of the board
            val center = Pos(params.cols / 2, params.rows / 2)
            val goldenIdx = result.indices.minByOrNull { idx: Int -> result[idx].pos.manhattanDistance(center) } ?: 0
            result[goldenIdx] = result[goldenIdx].copy(isGolden = true)
        }

        // Ensure at least 2 distinct species are used
        val speciesPool = if (params.speciesPool.isNotEmpty()) params.speciesPool else WorldSpecies.forWorld(params.world)
        if (speciesPool.size >= 2) {
            val distinctSpecies = result.map { it.species }.toSet()
            if (distinctSpecies.size < 2 && result.size >= 2) {
                val secondSpecies = speciesPool.first { it != result[0].species }
                result[1] = result[1].copy(
                    species = secondSpecies,
                    id = "${secondSpecies.lowercase()}_2"
                )
            }
        }

        return result
    }

    private fun hasGoodSpread(buddies: List<Buddy>, cols: Int, rows: Int): Boolean {
        if (buddies.isEmpty()) return false
        val xs = buddies.map { it.pos.x }
        val ys = buddies.map { it.pos.y }
        val width = xs.max() - xs.min() + 1
        val height = ys.max() - ys.min() + 1

        // Buddies should span at least 60% of cols and rows
        val minWidth = maxOf(2, (cols * 0.60).toInt())
        val minHeight = maxOf(2, (rows * 0.60).toInt())
        return width >= minWidth && height >= minHeight
    }
}
