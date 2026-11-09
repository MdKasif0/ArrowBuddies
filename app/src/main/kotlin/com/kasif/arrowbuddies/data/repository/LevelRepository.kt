package com.kasif.arrowbuddies.data.repository

import android.content.Context
import com.kasif.arrowbuddies.engine.codec.LevelCodec
import com.kasif.arrowbuddies.engine.generator.HandcraftedTutorials
import com.kasif.arrowbuddies.engine.model.LevelDef
import com.kasif.arrowbuddies.engine.runtime.DailyRescue
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

interface LevelRepository {
    fun loadLevel(levelId: String): LevelDef
    fun loadWorldLevels(world: Int): List<LevelDef> = emptyList()
    fun loadDailyRescue(): LevelDef = loadLevel("1")
    fun registerCustomLevel(level: LevelDef): String = level.id
    fun getAvailableDevLevelIds(): List<String>
}

@Singleton
class DefaultLevelRepository @Inject constructor(
    @ApplicationContext private val context: Context
) : LevelRepository {

    private val worldCache = ConcurrentHashMap<Int, List<LevelDef>>()
    private val customLevels = ConcurrentHashMap<String, LevelDef>()

    override fun registerCustomLevel(level: LevelDef): String {
        customLevels[level.id] = level
        return level.id
    }

    override fun loadLevel(levelId: String): LevelDef {
        customLevels[levelId]?.let { return it }

        // Daily rescue routing
        if (levelId == "daily" || levelId.startsWith("daily-")) {
            return DailyRescue.generate()
        }

        // Campaign level pattern: w{world}-l{level} (e.g. w1-l01 ... w8-l30)
        val campaignMatch = Regex("""w([1-8])-l([0-3][0-9])""").matchEntire(levelId)
        if (campaignMatch != null) {
            val world = campaignMatch.groupValues[1].toInt()
            val index = campaignMatch.groupValues[2].toInt()
            val worldLevels = loadWorldLevels(world)
            val level = worldLevels.firstOrNull { it.id == levelId || it.index == index }
            if (level != null) return level
            if (index in 1..3) return HandcraftedTutorials.getTutorialLevel(world, index)
        }

        // Dev level routing & backward compatibility
        val assetPath = when (levelId) {
            "2", "w1-l02-dev" -> "levels/dev/level_dev_02.json"
            "3", "w1-l03-dev" -> "levels/dev/level_dev_03.json"
            "4", "w2-l04-dev" -> "levels/dev/level_dev_04.json"
            "5", "w3-l05-dev" -> "levels/dev/level_dev_05.json"
            "6", "w4-l06-dev" -> "levels/dev/level_dev_06.json"
            "7", "w5-l07-dev" -> "levels/dev/level_dev_07.json"
            "8", "w6-l08-dev" -> "levels/dev/level_dev_08.json"
            "9", "w7-l09-dev" -> "levels/dev/level_dev_09.json"
            else -> "levels/dev/level_dev_01.json"
        }
        return runCatching {
            context.assets.open(assetPath).bufferedReader().use { it.readText() }.let { json ->
                LevelCodec.decode(json)
            }
        }.getOrElse {
            getEmbeddedDevLevel(levelId)
        }
    }

    override fun loadWorldLevels(world: Int): List<LevelDef> {
        require(world in 1..8) { "Invalid world: $world" }
        return worldCache.getOrPut(world) {
            runCatching {
                val assetPath = "levels/world_$world.json"
                val json = context.assets.open(assetPath).bufferedReader().use { it.readText() }
                LevelCodec.decodeList(json)
            }.getOrElse {
                (1..3).map { idx -> HandcraftedTutorials.getTutorialLevel(world, idx) }
            }
        }
    }

    override fun loadDailyRescue(): LevelDef {
        return DailyRescue.generate()
    }

    override fun getAvailableDevLevelIds(): List<String> {
        return listOf(
            "w1-l01-dev",
            "w1-l02-dev",
            "w1-l03-dev",
            "w2-l04-dev",
            "w3-l05-dev",
            "w4-l06-dev",
            "w5-l07-dev",
            "w6-l08-dev",
            "w7-l09-dev"
        )
    }

    private fun getEmbeddedDevLevel(levelId: String): LevelDef {
        val json = when (levelId) {
            "2", "w1-l02-dev" -> DEV_02_JSON
            "3", "w1-l03-dev" -> DEV_03_JSON
            "4", "w2-l04-dev" -> DEV_04_JSON
            "5", "w3-l05-dev" -> DEV_05_JSON
            "6", "w4-l06-dev" -> DEV_06_JSON
            "7", "w5-l07-dev" -> DEV_07_JSON
            "8", "w6-l08-dev" -> DEV_08_JSON
            "9", "w7-l09-dev" -> DEV_09_JSON
            else -> DEV_01_JSON
        }
        return LevelCodec.decode(json)
    }

    companion object {
        const val DEV_01_JSON = """{
  "id": "w1-l01-dev",
  "world": 1,
  "index": 1,
  "cols": 4,
  "rows": 4,
  "grid": ["....", "....", "....", "...."],
  "buddies": [
    { "x": 1, "y": 1, "d": "U", "s": "BUNNY", "id": "bunny_1" },
    { "x": 2, "y": 2, "d": "D", "s": "CHICK", "id": "chick_1" }
  ]
}"""

        const val DEV_02_JSON = """{
  "id": "w1-l02-dev",
  "world": 1,
  "index": 2,
  "cols": 5,
  "rows": 5,
  "grid": ["..R..", ".....", ".....", "..R..", "....."],
  "buddies": [
    { "x": 1, "y": 2, "d": "L", "s": "BUNNY", "id": "bunny_1" },
    { "x": 2, "y": 2, "d": "L", "s": "CHICK", "id": "chick_1" },
    { "x": 3, "y": 2, "d": "R", "s": "PIGLET", "id": "piglet_1" }
  ]
}"""

        const val DEV_03_JSON = """{
  "id": "w1-l03-dev",
  "world": 1,
  "index": 3,
  "cols": 5,
  "rows": 6,
  "grid": [".R...", ".....", "..R..", ".....", ".R...", "....."],
  "buddies": [
    { "x": 3, "y": 1, "d": "U", "s": "BUNNY", "id": "bunny_1" },
    { "x": 3, "y": 3, "d": "U", "s": "CHICK", "id": "chick_1" },
    { "x": 1, "y": 3, "d": "R", "s": "PIGLET", "id": "piglet_1" }
  ]
}"""

        const val DEV_04_JSON = """{
  "id": "w2-l04-dev",
  "world": 2,
  "index": 4,
  "cols": 5,
  "rows": 5,
  "grid": [".....", ".....", "..M..", ".....", "....."],
  "buddies": [
    { "x": 2, "y": 4, "d": "U", "s": "PUPPY", "id": "puppy_1" },
    { "x": 0, "y": 2, "d": "R", "s": "LAMB", "id": "lamb_1" },
    { "x": 4, "y": 1, "d": "L", "s": "CALF", "id": "calf_1" }
  ]
}"""

        const val DEV_05_JSON = """{
  "id": "w3-l05-dev",
  "world": 3,
  "index": 5,
  "cols": 5,
  "rows": 5,
  "grid": [".....", ".....", "..T..", ".....", "....."],
  "buddies": [
    { "x": 0, "y": 2, "d": "R", "s": "MONKEY", "id": "monkey_1" },
    { "x": 2, "y": 1, "d": "U", "s": "TIGER_CUB", "id": "tiger_1" },
    { "x": 4, "y": 4, "d": "L", "s": "PARROT", "id": "parrot_1" }
  ]
}"""

        const val DEV_06_JSON = """{
  "id": "w4-l06-dev",
  "world": 4,
  "index": 6,
  "cols": 5,
  "rows": 5,
  "grid": [".....", ".....", ".....", ".....", "....."],
  "buddies": [
    { "x": 1, "y": 3, "d": "L", "s": "PENGUIN", "id": "penguin_1", "keyColor": "gold" },
    { "x": 2, "y": 2, "d": "U", "s": "ARCTIC_FOX", "id": "fox_1", "cageColor": "gold" },
    { "x": 3, "y": 4, "d": "D", "s": "POLAR_CUB", "id": "polar_1" }
  ]
}"""

        const val DEV_07_JSON = """{
  "id": "w5-l07-dev",
  "world": 5,
  "index": 7,
  "cols": 5,
  "rows": 5,
  "grid": [".....", ".....", ".1.1.", ".....", "....."],
  "buddies": [
    { "x": 0, "y": 2, "d": "R", "s": "TURTLE", "id": "turtle_1" },
    { "x": 3, "y": 4, "d": "U", "s": "OTTER", "id": "otter_1" },
    { "x": 4, "y": 0, "d": "D", "s": "CRAB", "id": "crab_1" }
  ]
}"""

        const val DEV_08_JSON = """{
  "id": "w6-l08-dev",
  "world": 6,
  "index": 8,
  "cols": 5,
  "rows": 5,
  "grid": [".....", ".....", ".....", ".....", "....."],
  "buddies": [
    { "x": 2, "y": 1, "d": "U", "s": "HEDGEHOG", "id": "hedgehog_1" },
    { "x": 2, "y": 2, "d": "U", "s": "OWL", "id": "owl_1", "sleepy": true },
    { "x": 4, "y": 3, "d": "R", "s": "RACCOON", "id": "raccoon_1" }
  ]
}"""

        const val DEV_09_JSON = """{
  "id": "w7-l09-dev",
  "world": 7,
  "index": 9,
  "cols": 5,
  "rows": 6,
  "grid": [".....", ".T...", "...M.", ".1.1.", ".....", "....."],
  "buddies": [
    { "x": 1, "y": 4, "d": "U", "s": "CAMEL", "id": "camel_1", "keyColor": "gold" },
    { "x": 3, "y": 5, "d": "U", "s": "FENNEC_FOX", "id": "fennec_1", "cageColor": "gold" },
    { "x": 4, "y": 5, "d": "U", "s": "MEERKAT", "id": "meerkat_1", "sleepy": true }
  ]
}"""
    }
}
