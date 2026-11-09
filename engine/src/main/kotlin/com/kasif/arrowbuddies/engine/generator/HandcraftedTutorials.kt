package com.kasif.arrowbuddies.engine.generator

import com.kasif.arrowbuddies.engine.model.Buddy
import com.kasif.arrowbuddies.engine.model.Direction
import com.kasif.arrowbuddies.engine.model.LevelDef
import com.kasif.arrowbuddies.engine.model.Pos
import com.kasif.arrowbuddies.engine.model.Tile

/**
 * Handcrafted tutorial levels 1 to 3 for all 8 worlds (24 levels total).
 * Each level introduces that world's mechanic gently with an educational comment.
 */
object HandcraftedTutorials {

    fun getTutorialLevel(world: Int, index: Int): LevelDef {
        require(world in 1..8) { "Invalid world $world" }
        require(index in 1..3) { "Invalid tutorial index $index (must be 1..3)" }

        return when (world) {
            1 -> when (index) {
                1 -> LevelDef(
                    id = "w1-l01",
                    cols = 4,
                    rows = 4,
                    tiles = emptyMap(),
                    buddies = listOf(
                        Buddy("bunny_1", "BUNNY", Pos(1, 1), Direction.UP),
                        Buddy("chick_1", "CHICK", Pos(2, 2), Direction.DOWN)
                    ),
                    world = 1,
                    index = 1,
                    comment = "Tutorial 1: Tap a buddy to dash to the edge and escape to safety!"
                )
                2 -> LevelDef(
                    id = "w1-l02",
                    cols = 4,
                    rows = 4,
                    tiles = emptyMap(),
                    buddies = listOf(
                        Buddy("piglet_1", "PIGLET", Pos(1, 2), Direction.RIGHT),
                        Buddy("bunny_1", "BUNNY", Pos(2, 2), Direction.RIGHT)
                    ),
                    world = 1,
                    index = 2,
                    comment = "Tutorial 2: Order matters! Free the buddy in front first so the way is clear."
                )
                else -> LevelDef(
                    id = "w1-l03",
                    cols = 5,
                    rows = 5,
                    tiles = mapOf(Pos(2, 1) to Tile.Rock),
                    buddies = listOf(
                        Buddy("chick_1", "CHICK", Pos(1, 1), Direction.DOWN),
                        Buddy("bunny_1", "BUNNY", Pos(2, 3), Direction.DOWN),
                        Buddy("piglet_1", "PIGLET", Pos(3, 1), Direction.DOWN)
                    ),
                    world = 1,
                    index = 3,
                    comment = "Tutorial 3: Watch out for Rocks! They block your path and cause a bonk."
                )
            }
            2 -> when (index) {
                1 -> LevelDef(
                    id = "w2-l01",
                    cols = 4,
                    rows = 5,
                    tiles = mapOf(Pos(1, 2) to Tile.Mud),
                    buddies = listOf(
                        Buddy("puppy_1", "PUPPY", Pos(1, 4), Direction.UP),
                        Buddy("lamb_1", "LAMB", Pos(2, 1), Direction.RIGHT)
                    ),
                    world = 2,
                    index = 1,
                    comment = "Tutorial 1: Mud squelch! Entering mud stops a buddy safely without losing a heart."
                )
                2 -> LevelDef(
                    id = "w2-l02",
                    cols = 5,
                    rows = 5,
                    tiles = mapOf(Pos(2, 2) to Tile.Mud),
                    buddies = listOf(
                        Buddy("puppy_1", "PUPPY", Pos(2, 4), Direction.UP),
                        Buddy("lamb_1", "LAMB", Pos(1, 2), Direction.RIGHT),
                        Buddy("calf_1", "CALF", Pos(3, 2), Direction.RIGHT)
                    ),
                    world = 2,
                    index = 2,
                    comment = "Tutorial 2: Mud pit stop. Use mud as a stepping stone to escape in two taps."
                )
                else -> LevelDef(
                    id = "w2-l03",
                    cols = 5,
                    rows = 5,
                    tiles = mapOf(Pos(2, 2) to Tile.Mud),
                    buddies = listOf(
                        Buddy("puppy_1", "PUPPY", Pos(2, 4), Direction.UP),
                        Buddy("calf_1", "CALF", Pos(0, 2), Direction.RIGHT),
                        Buddy("lamb_1", "LAMB", Pos(4, 2), Direction.RIGHT)
                    ),
                    world = 2,
                    index = 3,
                    comment = "Tutorial 3: Mud roadblock. A buddy resting on mud blocks others until rescued."
                )
            }
            3 -> when (index) {
                1 -> LevelDef(
                    id = "w3-l01",
                    cols = 5,
                    rows = 5,
                    tiles = mapOf(Pos(2, 2) to Tile.Turntable),
                    buddies = listOf(
                        Buddy("monkey_1", "MONKEY", Pos(2, 4), Direction.UP),
                        Buddy("tiger_1", "TIGER_CUB", Pos(0, 1), Direction.RIGHT)
                    ),
                    world = 3,
                    index = 1,
                    comment = "Tutorial 1: Wheee! A turntable spins the buddy 90 degrees clockwise as it enters."
                )
                2 -> LevelDef(
                    id = "w3-l02",
                    cols = 5,
                    rows = 5,
                    tiles = mapOf(Pos(2, 1) to Tile.Rock, Pos(2, 3) to Tile.Turntable),
                    buddies = listOf(
                        Buddy("parrot_1", "PARROT", Pos(2, 4), Direction.UP),
                        Buddy("monkey_1", "MONKEY", Pos(4, 3), Direction.UP)
                    ),
                    world = 3,
                    index = 2,
                    comment = "Tutorial 2: Spin around obstacles! Turntables redirect buddies safely away from rocks."
                )
                else -> LevelDef(
                    id = "w3-l03",
                    cols = 5,
                    rows = 5,
                    tiles = mapOf(Pos(1, 1) to Tile.Turntable, Pos(3, 3) to Tile.Turntable),
                    buddies = listOf(
                        Buddy("tiger_1", "TIGER_CUB", Pos(1, 4), Direction.UP),
                        Buddy("parrot_1", "PARROT", Pos(0, 3), Direction.RIGHT),
                        Buddy("monkey_1", "MONKEY", Pos(3, 4), Direction.UP)
                    ),
                    world = 3,
                    index = 3,
                    comment = "Tutorial 3: Multi-turn maze. Chart the spin before you tap!"
                )
            }
            4 -> when (index) {
                1 -> LevelDef(
                    id = "w4-l01",
                    cols = 5,
                    rows = 5,
                    tiles = emptyMap(),
                    buddies = listOf(
                        Buddy("penguin_1", "PENGUIN", Pos(1, 3), Direction.LEFT, keyColor = "gold"),
                        Buddy("fox_1", "ARCTIC_FOX", Pos(2, 2), Direction.UP, cageColor = "gold")
                    ),
                    world = 4,
                    index = 1,
                    comment = "Tutorial 1: Key to freedom! Free the key-bearer first to pop open matching cages."
                )
                2 -> LevelDef(
                    id = "w4-l02",
                    cols = 5,
                    rows = 5,
                    tiles = emptyMap(),
                    buddies = listOf(
                        Buddy("polar_1", "POLAR_CUB", Pos(2, 4), Direction.UP),
                        Buddy("fox_1", "ARCTIC_FOX", Pos(2, 2), Direction.UP, cageColor = "gold"),
                        Buddy("penguin_1", "PENGUIN", Pos(1, 2), Direction.LEFT, keyColor = "gold")
                    ),
                    world = 4,
                    index = 2,
                    comment = "Tutorial 2: Unlock the chain! Free trapped buddies to clear the line behind them."
                )
                else -> LevelDef(
                    id = "w4-l03",
                    cols = 5,
                    rows = 6,
                    tiles = emptyMap(),
                    buddies = listOf(
                        Buddy("penguin_1", "PENGUIN", Pos(1, 4), Direction.LEFT, keyColor = "gold"),
                        Buddy("polar_1", "POLAR_CUB", Pos(3, 1), Direction.RIGHT, keyColor = "silver"),
                        Buddy("fox_1", "ARCTIC_FOX", Pos(2, 3), Direction.UP, cageColor = "gold"),
                        Buddy("fox_2", "ARCTIC_FOX", Pos(3, 4), Direction.DOWN, cageColor = "silver")
                    ),
                    world = 4,
                    index = 3,
                    comment = "Tutorial 3: Double lock! Match the golden and silver keys to the right cages."
                )
            }
            5 -> when (index) {
                1 -> LevelDef(
                    id = "w5-l01",
                    cols = 5,
                    rows = 5,
                    tiles = mapOf(Pos(2, 1) to Tile.Portal(1), Pos(2, 3) to Tile.Portal(1)),
                    buddies = listOf(
                        Buddy("turtle_1", "TURTLE", Pos(2, 4), Direction.UP),
                        Buddy("otter_1", "OTTER", Pos(0, 2), Direction.RIGHT)
                    ),
                    world = 5,
                    index = 1,
                    comment = "Tutorial 1: Wormhole magic! Enter portal A and instantly emerge from portal B."
                )
                2 -> LevelDef(
                    id = "w5-l02",
                    cols = 5,
                    rows = 6,
                    tiles = mapOf(Pos(2, 2) to Tile.Rock, Pos(2, 3) to Tile.Portal(1), Pos(2, 1) to Tile.Portal(1)),
                    buddies = listOf(
                        Buddy("crab_1", "CRAB", Pos(2, 4), Direction.UP),
                        Buddy("otter_1", "OTTER", Pos(4, 1), Direction.UP),
                        Buddy("turtle_1", "TURTLE", Pos(0, 3), Direction.LEFT)
                    ),
                    world = 5,
                    index = 2,
                    comment = "Tutorial 2: Leap over rocks! Portals bypass obstacles completely."
                )
                else -> LevelDef(
                    id = "w5-l03",
                    cols = 5,
                    rows = 6,
                    tiles = mapOf(Pos(1, 2) to Tile.Portal(1), Pos(3, 2) to Tile.Portal(1)),
                    buddies = listOf(
                        Buddy("crab_1", "CRAB", Pos(1, 4), Direction.UP),
                        Buddy("turtle_1", "TURTLE", Pos(3, 1), Direction.UP),
                        Buddy("otter_1", "OTTER", Pos(4, 4), Direction.RIGHT)
                    ),
                    world = 5,
                    index = 3,
                    comment = "Tutorial 3: Portal traffic! Make sure the exit portal's path is clear before warping."
                )
            }
            6 -> when (index) {
                1 -> LevelDef(
                    id = "w6-l01",
                    cols = 5,
                    rows = 5,
                    tiles = emptyMap(),
                    buddies = listOf(
                        Buddy("hedgehog_1", "HEDGEHOG", Pos(2, 1), Direction.UP),
                        Buddy("owl_1", "OWL", Pos(2, 2), Direction.UP, sleepy = true)
                    ),
                    world = 6,
                    index = 1,
                    comment = "Tutorial 1: Wake up, sleepyhead! A sleeping buddy awakens when an adjacent neighbor departs."
                )
                2 -> LevelDef(
                    id = "w6-l02",
                    cols = 5,
                    rows = 5,
                    tiles = emptyMap(),
                    buddies = listOf(
                        Buddy("raccoon_1", "RACCOON", Pos(2, 3), Direction.DOWN),
                        Buddy("owl_1", "OWL", Pos(2, 2), Direction.UP, sleepy = true),
                        Buddy("hedgehog_1", "HEDGEHOG", Pos(1, 2), Direction.LEFT)
                    ),
                    world = 6,
                    index = 2,
                    comment = "Tutorial 2: Morning alarm! Tap friends next to sleepy buddies to awaken them."
                )
                else -> LevelDef(
                    id = "w6-l03",
                    cols = 5,
                    rows = 6,
                    tiles = emptyMap(),
                    buddies = listOf(
                        Buddy("hedgehog_1", "HEDGEHOG", Pos(2, 1), Direction.UP),
                        Buddy("owl_1", "OWL", Pos(2, 2), Direction.UP, sleepy = true),
                        Buddy("raccoon_1", "RACCOON", Pos(2, 3), Direction.DOWN),
                        Buddy("owl_2", "OWL", Pos(3, 2), Direction.RIGHT, sleepy = true)
                    ),
                    world = 6,
                    index = 3,
                    comment = "Tutorial 3: Domino wake-up! One departure can awaken friends in a chain."
                )
            }
            7 -> when (index) {
                1 -> LevelDef(
                    id = "w7-l01",
                    cols = 5,
                    rows = 5,
                    tiles = mapOf(Pos(2, 2) to Tile.Turntable, Pos(3, 2) to Tile.Mud),
                    buddies = listOf(
                        Buddy("camel_1", "CAMEL", Pos(2, 4), Direction.UP),
                        Buddy("fennec_1", "FENNEC_FOX", Pos(0, 1), Direction.RIGHT)
                    ),
                    world = 7,
                    index = 1,
                    comment = "Tutorial 1: Oasis crossing! Spin on turntables and slide through mud."
                )
                2 -> LevelDef(
                    id = "w7-l02",
                    cols = 5,
                    rows = 6,
                    tiles = mapOf(Pos(2, 3) to Tile.Turntable),
                    buddies = listOf(
                        Buddy("fennec_1", "FENNEC_FOX", Pos(2, 5), Direction.UP, keyColor = "gold"),
                        Buddy("camel_1", "CAMEL", Pos(1, 2), Direction.RIGHT),
                        Buddy("meerkat_1", "MEERKAT", Pos(4, 2), Direction.UP, cageColor = "gold")
                    ),
                    world = 7,
                    index = 2,
                    comment = "Tutorial 2: Desert key carrier! Direct keys through turntables to reach cages."
                )
                else -> LevelDef(
                    id = "w7-l03",
                    cols = 5,
                    rows = 6,
                    tiles = mapOf(Pos(2, 2) to Tile.Mud, Pos(1, 3) to Tile.Rock),
                    buddies = listOf(
                        Buddy("meerkat_1", "MEERKAT", Pos(2, 4), Direction.UP, sleepy = true),
                        Buddy("camel_1", "CAMEL", Pos(2, 5), Direction.UP),
                        Buddy("fennec_1", "FENNEC_FOX", Pos(3, 4), Direction.RIGHT)
                    ),
                    world = 7,
                    index = 3,
                    comment = "Tutorial 3: Desert expedition! Master sleeping and mud together."
                )
            }
            else -> when (index) {
                1 -> LevelDef(
                    id = "w8-l01",
                    cols = 5,
                    rows = 6,
                    tiles = mapOf(Pos(2, 4) to Tile.Portal(1), Pos(2, 2) to Tile.Portal(1), Pos(2, 1) to Tile.Turntable),
                    buddies = listOf(
                        Buddy("panda_1", "RED_PANDA", Pos(2, 5), Direction.UP),
                        Buddy("elephant_1", "ELEPHANT_CALF", Pos(0, 3), Direction.LEFT)
                    ),
                    world = 8,
                    index = 1,
                    comment = "Tutorial 1: Sky transit! Warp through portals directly into turntables."
                )
                2 -> LevelDef(
                    id = "w8-l02",
                    cols = 6,
                    rows = 6,
                    tiles = emptyMap(),
                    buddies = listOf(
                        Buddy("peacock_1", "PEACOCK", Pos(2, 2), Direction.UP, sleepy = true, keyColor = "gold"),
                        Buddy("panda_1", "RED_PANDA", Pos(2, 1), Direction.UP),
                        Buddy("elephant_1", "ELEPHANT_CALF", Pos(4, 2), Direction.RIGHT, cageColor = "gold")
                    ),
                    world = 8,
                    index = 2,
                    comment = "Tutorial 2: Sleeping guardian! Wake the sleepy buddy to claim the golden key."
                )
                else -> LevelDef(
                    id = "w8-l03",
                    cols = 6,
                    rows = 6,
                    tiles = mapOf(Pos(2, 3) to Tile.Mud, Pos(3, 1) to Tile.Portal(1), Pos(1, 4) to Tile.Portal(1)),
                    buddies = listOf(
                        Buddy("elephant_1", "ELEPHANT_CALF", Pos(2, 5), Direction.UP),
                        Buddy("panda_1", "RED_PANDA", Pos(1, 5), Direction.UP),
                        Buddy("peacock_1", "PEACOCK", Pos(4, 4), Direction.RIGHT),
                        Buddy("panda_2", "RED_PANDA", Pos(0, 1), Direction.RIGHT)
                    ),
                    world = 8,
                    index = 3,
                    comment = "Tutorial 3: Grand sky flight! All mechanics together in harmony."
                )
            }
        }
    }
}
