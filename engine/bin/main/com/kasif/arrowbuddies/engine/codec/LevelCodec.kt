package com.kasif.arrowbuddies.engine.codec

import com.kasif.arrowbuddies.engine.model.Buddy
import com.kasif.arrowbuddies.engine.model.Direction
import com.kasif.arrowbuddies.engine.model.LevelDef
import com.kasif.arrowbuddies.engine.model.Pos
import com.kasif.arrowbuddies.engine.model.Tile
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
internal data class LevelDto(
    val id: String,
    val world: Int,
    val index: Int,
    val cols: Int,
    val rows: Int,
    val grid: List<String>,
    val buddies: List<BuddyDto>,
    val comment: String? = null
)

@Serializable
internal data class BuddyDto(
    val x: Int,
    val y: Int,
    val d: String,
    val s: String,
    val id: String? = null,
    val cageColor: String? = null,
    val keyColor: String? = null,
    val sleepy: Boolean = false,
    val isGolden: Boolean = false
)

/**
 * Human-readable JSON serializer and parser for ArrowBuddies levels.
 * Validates board bounds, obstacles, and buddy placements with clear error diagnostics.
 */
object LevelCodec {

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    /**
     * Parses a JSON level string into a validated [LevelDef].
     *
     * @throws IllegalArgumentException on syntax, dimensions, bounds, or placement violations.
     */
    fun decode(jsonString: String): LevelDef {
        val dto = json.decodeFromString<LevelDto>(jsonString)
        return fromDto(dto)
    }

    /**
     * Parses a JSON string containing an array of level objects into validated [LevelDef]s.
     */
    fun decodeList(jsonString: String): List<LevelDef> {
        val list = json.decodeFromString<List<LevelDto>>(jsonString)
        return list.map { fromDto(it) }
    }

    private fun fromDto(dto: LevelDto): LevelDef {
        // 1. Validate board dimensions (Spec: 4x4 up to 7 cols x 9 rows)
        require(dto.cols in 1..7) {
            "Invalid grid columns: ${dto.cols}. ArrowBuddies boards must have between 1 and 7 columns."
        }
        require(dto.rows in 1..9) {
            "Invalid grid rows: ${dto.rows}. ArrowBuddies boards must have between 1 and 9 rows."
        }

        // 2. Validate grid structure
        require(dto.grid.size == dto.rows) {
            "Grid row count (${dto.grid.size}) does not match level rows (${dto.rows})."
        }

        val tilesMap = mutableMapOf<Pos, Tile>()
        for (y in 0 until dto.rows) {
            val row = dto.grid[y]
            require(row.length == dto.cols) {
                "Grid row $y length (${row.length}) does not match level cols (${dto.cols})."
            }
            for (x in 0 until dto.cols) {
                val c = row[x]
                val pos = Pos(x, y)
                when (c) {
                    '.' -> { /* Empty cell */ }
                    'R' -> tilesMap[pos] = Tile.Rock
                    'M' -> tilesMap[pos] = Tile.Mud
                    'T' -> tilesMap[pos] = Tile.Turntable
                    in '1'..'9' -> tilesMap[pos] = Tile.Portal(pairId = c.digitToInt())
                    else -> throw IllegalArgumentException("Unknown tile character '$c' at ($x, $y).")
                }
            }
        }

        // 3. Validate buddies
        val occupiedCells = mutableSetOf<Pos>()
        val buddies = dto.buddies.mapIndexed { index, bDto ->
            val pos = Pos(bDto.x, bDto.y)

            // Bounds check
            require(pos.x in 0 until dto.cols && pos.y in 0 until dto.rows) {
                "Buddy '${bDto.s}' at $pos is out of bounds on a ${dto.cols}x${dto.rows} board."
            }

            // Rock collision check
            require(tilesMap[pos] !is Tile.Rock) {
                "Buddy '${bDto.s}' at $pos cannot be placed on a Rock."
            }

            // Cell collision check
            require(occupiedCells.add(pos)) {
                "Multiple buddies placed on the same cell: $pos."
            }

            val dir = when (bDto.d.uppercase()) {
                "U", "UP" -> Direction.UP
                "D", "DOWN" -> Direction.DOWN
                "L", "LEFT" -> Direction.LEFT
                "R", "RIGHT" -> Direction.RIGHT
                else -> throw IllegalArgumentException("Invalid direction code '${bDto.d}' for buddy at $pos.")
            }

            val id = bDto.id ?: "${bDto.s.lowercase()}_${index + 1}"

            Buddy(
                id = id,
                species = bDto.s.uppercase(),
                pos = pos,
                dir = dir,
                cageColor = bDto.cageColor,
                keyColor = bDto.keyColor,
                sleepy = bDto.sleepy,
                isGolden = bDto.isGolden
            )
        }

        return LevelDef(
            id = dto.id,
            cols = dto.cols,
            rows = dto.rows,
            tiles = tilesMap,
            buddies = buddies,
            world = dto.world,
            index = dto.index,
            comment = dto.comment
        )
    }

    private fun toDto(level: LevelDef): LevelDto {
        val gridRows = (0 until level.rows).map { y ->
            (0 until level.cols).map { x ->
                val pos = Pos(x, y)
                when (val tile = level.tileAt(pos)) {
                    is Tile.Empty -> '.'
                    is Tile.Rock -> 'R'
                    is Tile.Mud -> 'M'
                    is Tile.Turntable -> 'T'
                    is Tile.Portal -> ('0' + tile.pairId)
                }
            }.joinToString("")
        }

        val buddiesDto = level.buddies.map { buddy ->
            val dirCode = when (buddy.dir) {
                Direction.UP -> "U"
                Direction.DOWN -> "D"
                Direction.LEFT -> "L"
                Direction.RIGHT -> "R"
            }
            BuddyDto(
                x = buddy.pos.x,
                y = buddy.pos.y,
                d = dirCode,
                s = buddy.species,
                id = buddy.id,
                cageColor = buddy.cageColor,
                keyColor = buddy.keyColor,
                sleepy = buddy.sleepy,
                isGolden = buddy.isGolden
            )
        }

        return LevelDto(
            id = level.id,
            world = level.world,
            index = level.index,
            cols = level.cols,
            rows = level.rows,
            grid = gridRows,
            buddies = buddiesDto,
            comment = level.comment
        )
    }

    /**
     * Serializes a [LevelDef] to formatted JSON.
     */
    fun encode(level: LevelDef): String {
        return json.encodeToString(toDto(level))
    }

    /**
     * Serializes a list of [LevelDef]s to formatted JSON.
     */
    fun encodeList(levels: List<LevelDef>): String {
        return json.encodeToString(levels.map { toDto(it) })
    }
}

