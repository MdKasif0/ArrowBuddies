package com.kasif.arrowbuddies.engine

import com.kasif.arrowbuddies.engine.model.BlockerKind
import com.kasif.arrowbuddies.engine.model.Buddy
import com.kasif.arrowbuddies.engine.model.Direction
import com.kasif.arrowbuddies.engine.model.GameState
import com.kasif.arrowbuddies.engine.model.GameStatus
import com.kasif.arrowbuddies.engine.model.MoveOutcome
import com.kasif.arrowbuddies.engine.model.MoveResult
import com.kasif.arrowbuddies.engine.model.PeekingPath
import com.kasif.arrowbuddies.engine.model.Pos
import com.kasif.arrowbuddies.engine.model.RefusalReason
import com.kasif.arrowbuddies.engine.model.Step
import com.kasif.arrowbuddies.engine.model.StepEvent
import com.kasif.arrowbuddies.engine.model.Tile
import com.kasif.arrowbuddies.engine.solver.LevelSolver

/**
 * Pure Kotlin rule engine for ArrowBuddies.
 * Zero Android dependencies, 100% deterministic, immutable data flow.
 */
object GameEngine {

    /**
     * Executes a tap action on a buddy identified by [buddyId].
     *
     * @param state Current immutable [GameState].
     * @param buddyId Unique ID of the buddy to activate.
     * @return [MoveOutcome] containing the new game state and the detailed result.
     */
    fun tap(state: GameState, buddyId: String): MoveOutcome {
        // 1. Guard: Cannot tap if game is not active
        if (state.status != GameStatus.PLAYING) {
            return MoveOutcome(state, MoveResult.Ignored)
        }

        // 2. Guard: Buddy must be present on the board
        val buddy = state.findBuddy(buddyId) ?: return MoveOutcome(state, MoveResult.Ignored)

        // 3. Immobilization checks (Reserved for Cages & Sleepy mechanics)
        if (buddy.cageColor != null) {
            return MoveOutcome(state, MoveResult.Refused(RefusalReason.CAGED))
        }
        if (buddy.sleepy) {
            return MoveOutcome(state, MoveResult.Refused(RefusalReason.SLEEPY))
        }

        // 4. Simulate movement step by step
        var currentPos = buddy.pos
        var currentDir = buddy.dir

        val path = mutableListOf<Step>()
        path.add(Step(pos = currentPos, dirAfter = currentDir, event = StepEvent.NONE))

        val visited = mutableSetOf<Pair<Pos, Direction>>()
        visited.add(currentPos to currentDir)

        while (true) {
            val nextPos = currentPos + currentDir

            // A. Did the buddy reach beyond the board boundaries? -> ESCAPED!
            if (!state.level.isInside(nextPos)) {
                val remainingBuddies = state.buddies.filter { it.id != buddy.id }

                // World 4: Unlock cages matching keyColor
                val unlockedBuddies = if (buddy.keyColor != null) {
                    remainingBuddies.map {
                        if (it.cageColor == buddy.keyColor) it.copy(cageColor = null) else it
                    }
                } else {
                    remainingBuddies
                }

                // World 6: Wake sleepy orthogonal neighbors of vacated starting cell
                val newBuddies = unlockedBuddies.map {
                    if (it.sleepy && it.pos.isOrthogonalTo(buddy.pos)) {
                        it.copy(sleepy = false)
                    } else {
                        it
                    }
                }

                val newRescuedIds = state.rescuedIds + buddy.id
                val newStatus = if (newBuddies.isEmpty()) GameStatus.WON else GameStatus.PLAYING

                // Clean snapshot without nested history tree
                val historyEntry = state.copy(history = emptyList())
                val nextState = state.copy(
                    buddies = newBuddies,
                    rescuedIds = newRescuedIds,
                    moveCount = state.moveCount + 1,
                    status = newStatus,
                    history = state.history + historyEntry
                )
                return MoveOutcome(
                    newState = nextState,
                    result = MoveResult.Escaped(path = path, exitSide = currentDir)
                )
            }

            // B. Collision check: Is another buddy occupying nextPos?
            val blockerBuddy = state.buddyAt(nextPos)
            if (blockerBuddy != null && blockerBuddy.id != buddy.id) {
                return executeBonk(
                    state = state,
                    path = path,
                    blockerPos = nextPos,
                    blockerKind = BlockerKind.BUDDY
                )
            }

            // C. Terrain tile interaction
            when (val tile = state.level.tileAt(nextPos)) {
                is Tile.Rock -> {
                    // Collision with rock
                    return executeBonk(
                        state = state,
                        path = path,
                        blockerPos = nextPos,
                        blockerKind = BlockerKind.ROCK
                    )
                }

                is Tile.Mud -> {
                    // Mud stops buddy on it safely (valid move, no heart lost)
                    path.add(Step(pos = nextPos, dirAfter = currentDir, event = StepEvent.STOP))
                    val updatedBuddy = buddy.copy(pos = nextPos, dir = currentDir)

                    // World 6: If buddy left its initial cell, wake its orthogonal sleepy neighbors
                    val newBuddies = state.buddies.map {
                        when {
                            it.id == buddy.id -> updatedBuddy
                            it.sleepy && nextPos != buddy.pos && it.pos.isOrthogonalTo(buddy.pos) -> it.copy(sleepy = false)
                            else -> it
                        }
                    }

                    val historyEntry = state.copy(history = emptyList())
                    val nextState = state.copy(
                        buddies = newBuddies,
                        moveCount = state.moveCount + 1,
                        history = state.history + historyEntry
                    )
                    return MoveOutcome(
                        newState = nextState,
                        result = MoveResult.Stopped(path = path)
                    )
                }

                is Tile.Turntable -> {
                    // Turn 90 degrees clockwise and keep moving
                    currentDir = currentDir.turnClockwise90()
                    currentPos = nextPos
                    path.add(Step(pos = currentPos, dirAfter = currentDir, event = StepEvent.TURN))

                    if (!visited.add(currentPos to currentDir)) {
                        // Loop detected!
                        return executeBonk(
                            state = state,
                            path = path,
                            blockerPos = nextPos,
                            blockerKind = BlockerKind.LOOP
                        )
                    }
                }

                is Tile.Portal -> {
                    // 1. Enter portal A
                    path.add(Step(pos = nextPos, dirAfter = currentDir, event = StepEvent.NONE))

                    // 2. Locate twin portal B
                    val twins = state.level.portalPositions(tile.pairId)
                    val exitPos = twins.firstOrNull { it != nextPos } ?: nextPos

                    // 3. Blocked exit check: Is another buddy occupying portal B?
                    val blockerAtExit = state.buddyAt(exitPos)
                    if (blockerAtExit != null && blockerAtExit.id != buddy.id) {
                        return executeBonk(
                            state = state,
                            path = path,
                            blockerPos = exitPos,
                            blockerKind = BlockerKind.BUDDY
                        )
                    }

                    // 4. Teleport step at portal B
                    currentPos = exitPos
                    path.add(Step(pos = currentPos, dirAfter = currentDir, event = StepEvent.TELEPORT))

                    if (!visited.add(currentPos to currentDir)) {
                        return executeBonk(
                            state = state,
                            path = path,
                            blockerPos = nextPos,
                            blockerKind = BlockerKind.LOOP
                        )
                    }
                }

                is Tile.Empty -> {
                    // Open path
                    currentPos = nextPos
                    path.add(Step(pos = currentPos, dirAfter = currentDir, event = StepEvent.NONE))

                    if (!visited.add(currentPos to currentDir)) {
                        return executeBonk(
                            state = state,
                            path = path,
                            blockerPos = nextPos,
                            blockerKind = BlockerKind.LOOP
                        )
                    }
                }
            }
        }
    }

    private fun executeBonk(
        state: GameState,
        path: List<Step>,
        blockerPos: Pos,
        blockerKind: BlockerKind
    ): MoveOutcome {
        val newHearts = state.hearts - 1
        val newMistakes = state.mistakes + 1
        val newStatus = if (newHearts <= 0) GameStatus.LOST else GameStatus.PLAYING

        // Board state does NOT change on bonk (buddy returns to start cell)
        // History does NOT add bonk states (bonks cannot be undone; they cost a heart)
        val nextState = state.copy(
            hearts = newHearts,
            mistakes = newMistakes,
            moveCount = state.moveCount + 1,
            status = newStatus
        )
        return MoveOutcome(
            newState = nextState,
            result = MoveResult.Bonked(
                path = path,
                blockerPos = blockerPos,
                blockerKind = blockerKind
            )
        )
    }

    /**
     * Undoes the last successful move.
     * Restores the previous board configuration while preserving current hearts and mistakes.
     * Bonks cannot be undone.
     *
     * @return Updated [GameState] or `null` if history stack is empty.
     */
    fun undo(state: GameState): GameState? {
        if (state.history.isEmpty()) return null
        val previousState = state.history.last()
        val remainingHistory = state.history.dropLast(1)

        return previousState.copy(
            hearts = state.hearts,       // Preserves current hearts
            mistakes = state.mistakes,   // Preserves mistakes
            status = GameStatus.PLAYING,
            history = remainingHistory
        )
    }

    /**
     * Hint API: Returns all buddies that can currently be tapped without causing a bonk.
     */
    fun safeMoves(state: GameState): List<Buddy> {
        if (state.status != GameStatus.PLAYING) return emptyList()
        return state.buddies.filter { buddy ->
            val outcome = tap(state, buddy.id)
            outcome.result is MoveResult.Escaped || outcome.result is MoveResult.Stopped
        }
    }

    /**
     * Upgraded Hint API: Returns a buddy ID whose move keeps the level solvable,
     * preferring moves on the shortest solution. Returns null if level is not solvable.
     */
    fun findHint(state: GameState): String? {
        if (state.status != GameStatus.PLAYING) return null

        val solveResult = LevelSolver.solve(state, maxNodes = 6_000, timeoutMs = 150L)
        if (solveResult.solvable && solveResult.shortestSolution.isNotEmpty()) {
            return solveResult.shortestSolution.first()
        }

        val safe = safeMoves(state)
        for (buddy in safe) {
            val nextState = tap(state, buddy.id).newState
            if (LevelSolver.isSolvable(nextState, maxNodes = 600, timeoutMs = 30L)) {
                return buddy.id
            }
        }

        return null
    }

    /**
     * Simulates and computes the full path peek for [buddyId] without mutating game state.
     * Traces turns through turntables, teleports through portals, stops on mud,
     * and identifies blockers (rock, buddy, loop) or successful escape.
     */
    fun peekPath(state: GameState, buddyId: String): PeekingPath? {
        if (state.status != GameStatus.PLAYING) return null
        val buddy = state.findBuddy(buddyId) ?: return null

        if (buddy.cageColor != null) {
            return PeekingPath(
                buddyId = buddy.id,
                startPos = buddy.pos,
                steps = emptyList(),
                endPos = buddy.pos,
                isEscape = false,
                refusalReason = RefusalReason.CAGED
            )
        }

        if (buddy.sleepy) {
            return PeekingPath(
                buddyId = buddy.id,
                startPos = buddy.pos,
                steps = emptyList(),
                endPos = buddy.pos,
                isEscape = false,
                refusalReason = RefusalReason.SLEEPY
            )
        }

        val steps = mutableListOf<Pos>()
        var currentPos = buddy.pos
        var currentDir = buddy.dir
        val visited = mutableSetOf<Pair<Pos, Direction>>()
        visited.add(currentPos to currentDir)

        while (true) {
            val nextPos = currentPos + currentDir

            // Beyond edge = escape!
            if (!state.level.isInside(nextPos)) {
                steps.add(nextPos)
                return PeekingPath(
                    buddyId = buddy.id,
                    startPos = buddy.pos,
                    steps = steps,
                    endPos = nextPos,
                    isEscape = true
                )
            }

            // Blocker buddy
            val blockerBuddy = state.buddyAt(nextPos)
            if (blockerBuddy != null && blockerBuddy.id != buddy.id) {
                steps.add(nextPos)
                return PeekingPath(
                    buddyId = buddy.id,
                    startPos = buddy.pos,
                    steps = steps,
                    endPos = nextPos,
                    isEscape = false,
                    blockerKind = BlockerKind.BUDDY
                )
            }

            when (val tile = state.level.tileAt(nextPos)) {
                is Tile.Rock -> {
                    steps.add(nextPos)
                    return PeekingPath(
                        buddyId = buddy.id,
                        startPos = buddy.pos,
                        steps = steps,
                        endPos = nextPos,
                        isEscape = false,
                        blockerKind = BlockerKind.ROCK
                    )
                }

                is Tile.Mud -> {
                    steps.add(nextPos)
                    return PeekingPath(
                        buddyId = buddy.id,
                        startPos = buddy.pos,
                        steps = steps,
                        endPos = nextPos,
                        isEscape = true
                    )
                }

                is Tile.Turntable -> {
                    steps.add(nextPos)
                    currentDir = currentDir.turnClockwise90()
                    currentPos = nextPos
                    if (!visited.add(currentPos to currentDir)) {
                        return PeekingPath(
                            buddyId = buddy.id,
                            startPos = buddy.pos,
                            steps = steps,
                            endPos = nextPos,
                            isEscape = false,
                            blockerKind = BlockerKind.LOOP
                        )
                    }
                }

                is Tile.Portal -> {
                    steps.add(nextPos)
                    val twins = state.level.portalPositions(tile.pairId)
                    val exitPos = twins.firstOrNull { it != nextPos } ?: nextPos
                    val blockerAtExit = state.buddyAt(exitPos)
                    if (blockerAtExit != null && blockerAtExit.id != buddy.id) {
                        steps.add(exitPos)
                        return PeekingPath(
                            buddyId = buddy.id,
                            startPos = buddy.pos,
                            steps = steps,
                            endPos = exitPos,
                            isEscape = false,
                            blockerKind = BlockerKind.BUDDY
                        )
                    }
                    steps.add(exitPos)
                    currentPos = exitPos
                    if (!visited.add(currentPos to currentDir)) {
                        return PeekingPath(
                            buddyId = buddy.id,
                            startPos = buddy.pos,
                            steps = steps,
                            endPos = nextPos,
                            isEscape = false,
                            blockerKind = BlockerKind.LOOP
                        )
                    }
                }

                is Tile.Empty -> {
                    steps.add(nextPos)
                    currentPos = nextPos
                    if (!visited.add(currentPos to currentDir)) {
                        return PeekingPath(
                            buddyId = buddy.id,
                            startPos = buddy.pos,
                            steps = steps,
                            endPos = nextPos,
                            isEscape = false,
                            blockerKind = BlockerKind.LOOP
                        )
                    }
                }
            }
        }
    }
}
