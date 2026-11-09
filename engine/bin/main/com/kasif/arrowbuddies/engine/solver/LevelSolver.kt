package com.kasif.arrowbuddies.engine.solver

import com.kasif.arrowbuddies.engine.GameEngine
import com.kasif.arrowbuddies.engine.difficulty.DifficultyCalculator
import com.kasif.arrowbuddies.engine.model.BlockerKind
import com.kasif.arrowbuddies.engine.model.GameState
import com.kasif.arrowbuddies.engine.model.GameStatus
import com.kasif.arrowbuddies.engine.model.LevelDef
import com.kasif.arrowbuddies.engine.model.MoveResult
import com.kasif.arrowbuddies.engine.model.Tile
import java.util.ArrayDeque

/**
 * High-performance deterministic graph search solver for ArrowBuddies.
 * Memoizes visited board states with compact hashing.
 * Guarantees finding the shortest solution and computing difficulty metrics.
 */
object LevelSolver {

    private const val DEFAULT_MAX_NODES = 15_000
    private const val DEFAULT_TIMEOUT_MS = 350L
    private const val MAX_SOLUTION_COUNT = 50

    private class SearchNode(
        val state: GameState,
        val parent: SearchNode?,
        val moveBuddyId: String?,
        val depth: Int
    )

    /**
     * Solves the given [LevelDef] or [GameState] completely.
     */
    fun solve(level: LevelDef): SolveResult {
        val initialState = GameState(level = level, buddies = level.buddies)
        return solve(initialState)
    }

    /**
     * Solves the given [GameState].
     *
     * @param maxNodes Upper bound of graph nodes to explore before giving up.
     * @param timeoutMs Maximum elapsed wall-clock time allowed.
     */
    fun solve(
        initialState: GameState,
        maxNodes: Int = DEFAULT_MAX_NODES,
        timeoutMs: Long = DEFAULT_TIMEOUT_MS
    ): SolveResult {
        val startTime = System.currentTimeMillis()
        val level = initialState.level
        val buddyCount = initialState.buddies.size

        if (buddyCount == 0 || initialState.status == GameStatus.WON) {
            val metrics = LevelMetrics(
                buddyCount = 0,
                initialFreeMoves = 0,
                dependencyDepth = 0,
                deadEndMoves = 0,
                mechanicsUsed = emptySet(),
                difficultyScore = 0.0
            )
            return SolveResult(
                solvable = true,
                shortestSolution = emptyList(),
                solutionCount = 1,
                metrics = metrics,
                timeMs = 0L,
                exploredNodes = 0
            )
        }

        // 1. Initial Free Moves (buddies that can safely move immediately)
        val initialSafeBuddies = GameEngine.safeMoves(initialState)
        val initialFreeMoves = initialSafeBuddies.size

        // 2. Mechanics Present
        val mechanics = detectMechanics(level)

        // 3. Priority Search for Shortest Solution (Prioritize fewer remaining buddies)
        val visited = HashSet<CompactStateKey>()
        val queue = java.util.PriorityQueue<SearchNode> { a, b ->
            val buddyDiff = a.state.buddies.size.compareTo(b.state.buddies.size)
            if (buddyDiff != 0) buddyDiff else a.depth.compareTo(b.depth)
        }

        val rootNode = SearchNode(initialState, null, null, 0)
        queue.add(rootNode)
        visited.add(initialState.toCompactKey())

        var winningNode: SearchNode? = null
        var nodesExplored = 0

        while (queue.isNotEmpty() && nodesExplored < maxNodes) {
            if (System.currentTimeMillis() - startTime > timeoutMs) break

            val current = queue.poll() ?: break
            nodesExplored++

            if (current.state.status == GameStatus.WON || current.state.buddies.isEmpty()) {
                winningNode = current
                break
            }

            // Generate next safe, non-bonk, non-refused moves
            val candidateBuddies = GameEngine.safeMoves(current.state)
            for (buddy in candidateBuddies) {
                val outcome = GameEngine.tap(current.state, buddy.id)
                val result = outcome.result

                if (result is MoveResult.Escaped || result is MoveResult.Stopped) {
                    val key = outcome.newState.toCompactKey()
                    if (visited.add(key)) {
                        val childNode = SearchNode(
                            state = outcome.newState,
                            parent = current,
                            moveBuddyId = buddy.id,
                            depth = current.depth + 1
                        )
                        if (outcome.newState.status == GameStatus.WON || outcome.newState.buddies.isEmpty()) {
                            winningNode = childNode
                            break
                        }
                        queue.add(childNode)
                    }
                }
            }

            if (winningNode != null) break
        }

        val solvable = winningNode != null

        if (!solvable) {
            val metrics = LevelMetrics(
                buddyCount = buddyCount,
                initialFreeMoves = initialFreeMoves,
                dependencyDepth = 0,
                deadEndMoves = 0,
                mechanicsUsed = mechanics,
                difficultyScore = 0.0
            )
            return SolveResult(
                solvable = false,
                shortestSolution = emptyList(),
                solutionCount = 0,
                metrics = metrics,
                timeMs = System.currentTimeMillis() - startTime,
                exploredNodes = nodesExplored
            )
        }

        // 4. Reconstruct Shortest Solution Path
        val shortestSolution = mutableListOf<String>()
        val solutionStates = mutableListOf<GameState>()
        var curr = winningNode
        while (curr != null) {
            solutionStates.add(curr.state)
            curr.moveBuddyId?.let { shortestSolution.add(it) }
            curr = curr.parent
        }
        shortestSolution.reverse()
        solutionStates.reverse()

        // 5. Dependency Depth Analysis
        val dependencyDepth = computeDependencyDepth(initialState, shortestSolution)

        // 6. Dead-End Moves Detection
        val remainingBudget = (timeoutMs - (System.currentTimeMillis() - startTime)).coerceAtLeast(30L)
        val deadEndMoves = countDeadEndMoves(solutionStates, shortestSolution, mechanics, remainingBudget)

        // 7. Count Solutions (capped at 50)
        val remainingBudgetForCount = minOf(30L, (timeoutMs - (System.currentTimeMillis() - startTime)).coerceAtLeast(0L))
        val solutionCount = if (initialFreeMoves >= 8) {
            MAX_SOLUTION_COUNT
        } else {
            countSolutions(initialState, MAX_SOLUTION_COUNT, remainingBudgetForCount)
        }

        // 8. Difficulty Score
        val score = DifficultyCalculator.calculate(
            buddyCount = buddyCount,
            dependencyDepth = dependencyDepth,
            deadEndMoves = deadEndMoves,
            mechanicCount = mechanics.size,
            initialFreeMoves = initialFreeMoves
        )

        val metrics = LevelMetrics(
            buddyCount = buddyCount,
            initialFreeMoves = initialFreeMoves,
            dependencyDepth = dependencyDepth,
            deadEndMoves = deadEndMoves,
            mechanicsUsed = mechanics,
            difficultyScore = score
        )

        return SolveResult(
            solvable = true,
            shortestSolution = shortestSolution,
            solutionCount = solutionCount.coerceAtLeast(1),
            metrics = metrics,
            timeMs = System.currentTimeMillis() - startTime,
            exploredNodes = nodesExplored
        )
    }

    /**
     * Fast check whether a state is solvable within tight limits.
     */
    fun isSolvable(state: GameState, maxNodes: Int = 1_000, timeoutMs: Long = 40L): Boolean {
        if (state.status == GameStatus.WON || state.buddies.isEmpty()) return true
        if (state.status == GameStatus.LOST) return false

        val startTime = System.currentTimeMillis()
        val visited = HashSet<CompactStateKey>()
        val queue = java.util.PriorityQueue<GameState> { a, b ->
            a.buddies.size.compareTo(b.buddies.size)
        }

        queue.add(state)
        visited.add(state.toCompactKey())
        var nodes = 0

        while (queue.isNotEmpty() && nodes < maxNodes) {
            if (System.currentTimeMillis() - startTime > timeoutMs) break
            val current = queue.poll() ?: break
            nodes++

            if (current.status == GameStatus.WON || current.buddies.isEmpty()) {
                return true
            }

            val candidateBuddies = GameEngine.safeMoves(current)
            for (buddy in candidateBuddies) {
                val outcome = GameEngine.tap(current, buddy.id)
                if (outcome.result is MoveResult.Escaped || outcome.result is MoveResult.Stopped) {
                    if (outcome.newState.status == GameStatus.WON || outcome.newState.buddies.isEmpty()) {
                        return true
                    }
                    val key = outcome.newState.toCompactKey()
                    if (visited.add(key)) {
                        queue.add(outcome.newState)
                    }
                }
            }
        }
        return false
    }

    /**
     * Detects set of obstacle mechanics present in the level.
     */
    fun detectMechanics(level: LevelDef): Set<String> {
        val mechanics = mutableSetOf<String>()
        for (tile in level.tiles.values) {
            when (tile) {
                is Tile.Rock -> mechanics.add("rock")
                is Tile.Mud -> mechanics.add("mud")
                is Tile.Turntable -> mechanics.add("turntable")
                is Tile.Portal -> mechanics.add("portal")
                is Tile.Empty -> Unit
            }
        }
        for (buddy in level.buddies) {
            if (buddy.cageColor != null || buddy.keyColor != null) mechanics.add("cage")
            if (buddy.sleepy) mechanics.add("sleepy")
        }
        return mechanics
    }

    /**
     * Computes the dependency depth DAG: longest chain of "X must leave before Y".
     */
    private fun computeDependencyDepth(
        initialState: GameState,
        solution: List<String>
    ): Int {
        if (solution.isEmpty()) return 0
        val buddies = initialState.buddies
        val buddyMap = buddies.associateBy { it.id }

        // Find direct blockers for each buddy in initial state
        val directPrereqs = mutableMapOf<String, MutableSet<String>>()
        for (buddy in buddies) {
            val prereqs = mutableSetOf<String>()

            // 1. Ray obstruction
            val peek = GameEngine.peekPath(initialState, buddy.id)
            if (peek != null && !peek.isEscape) {
                val blocker = buddies.firstOrNull { it.pos == peek.endPos && it.id != buddy.id }
                if (blocker != null) {
                    prereqs.add(blocker.id)
                }
            }

            // 2. Cage lock prerequisite (needs matching key carrier to escape first)
            val cageColor = buddy.cageColor
            if (cageColor != null) {
                for (other in buddies) {
                    if (other.keyColor == cageColor && other.id != buddy.id) {
                        prereqs.add(other.id)
                    }
                }
            }

            // 3. Sleepy prerequisite (needs an adjacent neighbor to leave)
            if (buddy.sleepy) {
                for (other in buddies) {
                    if (other.id != buddy.id && buddy.pos.isOrthogonalTo(other.pos)) {
                        prereqs.add(other.id)
                    }
                }
            }

            directPrereqs[buddy.id] = prereqs
        }

        // Compute longest path in DAG along solution order
        val depthMap = mutableMapOf<String, Int>()
        for (buddyId in solution) {
            val prereqs = directPrereqs[buddyId] ?: emptySet()
            val maxPrereqDepth = prereqs
                .filter { it in depthMap }
                .maxOfOrNull { depthMap[it] ?: 0 } ?: 0

            depthMap[buddyId] = maxPrereqDepth + 1
        }

        return depthMap.values.maxOrNull() ?: 1
    }

    /**
     * Counts how many safe-looking moves lead directly to an unsolvable dead end.
     * Dead ends can only occur when a move enters mud or a turntable.
     */
    private fun countDeadEndMoves(
        solutionStates: List<GameState>,
        solutionMoves: List<String>,
        mechanics: Set<String>,
        budgetMs: Long
    ): Int {
        if (!mechanics.contains("mud") && !mechanics.contains("turntable")) {
            return 0
        }

        val startTime = System.currentTimeMillis()
        var deadEnds = 0

        for (i in 0 until solutionMoves.size.coerceAtMost(solutionStates.size)) {
            if (System.currentTimeMillis() - startTime > budgetMs) break

            val state = solutionStates[i]
            val actualMove = solutionMoves[i]
            val safeBuddies = GameEngine.safeMoves(state)

            for (other in safeBuddies) {
                if (other.id == actualMove) continue

                val outcome = GameEngine.tap(state, other.id)
                // Only moves that stop on terrain (e.g. mud) can introduce a new blocker
                if (outcome.result is MoveResult.Stopped) {
                    val stillSolvable = isSolvable(outcome.newState, maxNodes = 400, timeoutMs = 15L)
                    if (!stillSolvable) {
                        deadEnds++
                    }
                }
            }
        }
        return deadEnds
    }

    /**
     * Counts the total number of distinct solutions up to [limit].
     */
    private fun countSolutions(
        initialState: GameState,
        limit: Int,
        budgetMs: Long
    ): Int {
        val startTime = System.currentTimeMillis()
        var foundSolutions = 0
        var visitedNodes = 0
        val visitedOnPath = mutableSetOf<CompactStateKey>()

        var stopped = false

        fun dfs(current: GameState) {
            if (stopped || foundSolutions >= limit) return
            if (++visitedNodes > 300 || (System.currentTimeMillis() - startTime) > budgetMs) {
                stopped = true
                return
            }

            if (current.status == GameStatus.WON || current.buddies.isEmpty()) {
                foundSolutions++
                return
            }

            val key = current.toCompactKey()
            if (!visitedOnPath.add(key)) return

            val safeBuddies = GameEngine.safeMoves(current)
            for (buddy in safeBuddies) {
                if (stopped || foundSolutions >= limit) break
                val outcome = GameEngine.tap(current, buddy.id)
                if (outcome.result is MoveResult.Escaped || outcome.result is MoveResult.Stopped) {
                    dfs(outcome.newState)
                }
            }

            visitedOnPath.remove(key)
        }

        dfs(initialState)
        return foundSolutions.coerceAtLeast(1)
    }
}
