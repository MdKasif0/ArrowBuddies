package com.kasif.arrowbuddies.data.repository

import android.content.Context
import com.kasif.arrowbuddies.engine.codec.LevelCodec
import com.kasif.arrowbuddies.engine.model.LevelDef
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

interface LevelRepository {
    fun loadLevel(levelId: String): LevelDef
    fun getAvailableDevLevelIds(): List<String>
}

@Singleton
class DefaultLevelRepository @Inject constructor(
    @ApplicationContext private val context: Context
) : LevelRepository {

    override fun loadLevel(levelId: String): LevelDef {
        val assetPath = when (levelId) {
            "2", "w1-l02-dev" -> "levels/dev/level_dev_02.json"
            "3", "w1-l03-dev" -> "levels/dev/level_dev_03.json"
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

    override fun getAvailableDevLevelIds(): List<String> {
        return listOf("w1-l01-dev", "w1-l02-dev", "w1-l03-dev")
    }

    private fun getEmbeddedDevLevel(levelId: String): LevelDef {
        val json = when (levelId) {
            "2", "w1-l02-dev" -> DEV_02_JSON
            "3", "w1-l03-dev" -> DEV_03_JSON
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
  "grid": [
    "....",
    "....",
    "....",
    "...."
  ],
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
  "grid": [
    "..R..",
    ".....",
    ".....",
    "..R..",
    "....."
  ],
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
  "grid": [
    ".R...",
    ".....",
    "..R..",
    ".....",
    ".R...",
    "....."
  ],
  "buddies": [
    { "x": 3, "y": 1, "d": "U", "s": "BUNNY", "id": "bunny_1" },
    { "x": 3, "y": 3, "d": "U", "s": "CHICK", "id": "chick_1" },
    { "x": 1, "y": 3, "d": "R", "s": "PIGLET", "id": "piglet_1" }
  ]
}"""
    }
}
