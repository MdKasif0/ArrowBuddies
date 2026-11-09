# ArrowBuddies — Game Spec (source of truth)

## Pitch
Cute animal buddies are trapped in a maze. Each buddy faces one direction, shown by a big arrow badge.
Tap a buddy and it dashes that way. Clear path to the edge = it escapes and is RESCUED.
Something in the way = it BONKS, bounces back, and you lose a heart. Find the right order to free everyone.

## Platform & stack
- Android only, 100% Kotlin, Jetpack Compose, single Activity, portrait only.
- minSdk 24, compile/target the latest stable SDK.
- Must hold 60 fps on budget phones (3 GB RAM, 720p). Offline-first, no login. Download size under 25 MB.
- Languages: English + Hindi.

## Core rules
- Board: grid from 4x4 up to 7 columns x 9 rows. Never more than 7 columns, so cells stay >= 44dp on a 360dp phone.
- Every board edge is an exit.
- Buddy: occupies exactly 1 cell, has a species and a direction (UP, DOWN, LEFT, RIGHT).
- Tap a buddy -> it travels cell by cell in its direction:
  - Reaches the edge with nothing in the way -> ESCAPES (rescued). Its cell becomes empty.
  - Hits another buddy or a rock -> BONK: it slides up to the blocker, squashes, returns to its start cell. Lose 1 heart. Board state does not change.
- 3 hearts per level. 0 hearts = level failed (gentle retry, never harsh).
- Stars: 3 = no mistakes, 2 = one mistake, 1 = two mistakes.
- Every shipped level is proven solvable by the solver.

## Obstacles (one new mechanic per world)
| Element     | Rule | Tap on it when blocked |
|-------------|------|------------------------|
| Rock        | Static block, never moves. | n/a |
| Mud         | A buddy entering mud STOPS on it (valid move, no heart lost). Next tap continues in the same direction. | n/a |
| Turntable   | A buddy entering it turns 90 degrees clockwise and keeps moving. Its arrow badge updates. | n/a |
| Cage + Key  | A caged buddy cannot move. A key-carrier buddy of the same color opens all matching cages when it is rescued. Max 3 key colors. | Gentle shake, no heart lost |
| Portal pair | Entering portal A exits from portal B, same direction. Blocked exit = bonk. | n/a |
| Sleepy      | Shows Zzz and cannot move. Wakes up when any up/down/left/right neighbor buddy leaves its cell. | Yawn, no heart lost |
Infinite loops (e.g., turntables in a ring) count as a bonk. The generator must avoid them.

## Worlds (8 worlds x 30 levels = 240 levels)
| # | Biome          | New mechanic     | New species (3 per world)        |
|---|----------------|------------------|----------------------------------|
| 1 | Sunny Meadow   | Basics, then rocks | Bunny, Chick, Piglet           |
| 2 | Happy Farm     | Mud              | Puppy, Lamb, Calf                |
| 3 | Jungle Ruins   | Turntables       | Monkey, Tiger Cub, Parrot        |
| 4 | Snowy Peaks    | Cages + keys     | Penguin, Polar Cub, Arctic Fox   |
| 5 | Coral Coast    | Portals          | Turtle, Otter, Crab              |
| 6 | Moonlit Forest | Sleepy buddies   | Owl, Hedgehog, Raccoon           |
| 7 | Desert Oasis   | Mixed            | Camel, Fennec Fox, Meerkat       |
| 8 | Sky Islands    | Mixed finale     | Red Panda, Elephant Calf, Peacock|
- Level 1 of each world teaches its mechanic. Levels 2-3 are handcrafted.
- Sawtooth difficulty: every 5th level is an easy "breather".
- Levels 15 and 30 are "Big Rescue" levels: larger board plus one Golden Buddy (bonus berries).
- Extra modes: Daily Rescue (one date-seeded puzzle for everyone) and Rescue Run (endless, unlocked after World 2).

## Rescue progression
- Every rescued buddy moves into the Sanctuary, the living home screen. 8 zones unlock, one per world.
- Buddies wander, hop, nap and react when tapped.
- Buddy Album: 24 species. Locked ones show as silhouettes. Friendship tiers per species: Bronze 10, Silver 50, Gold 150 rescued; each tier unlocks a cosmetic accessory.
- Berries (soft currency): +10 per level, +5 per star, +50 Daily Rescue, streak bonus.
- Spend berries on: Hint 30, Undo 20, Extra Heart 40, sanctuary decorations 100-600.
- Big "Total buddies rescued" counter on the home screen.
- Child-friendly rules: no energy system, no loot boxes, no fake timers, no dark patterns.

## Boosters
- Hint: one buddy that can move safely right now glows and points its arrow. Start with 3.
- Undo: reverts the last successful move. Start with 3.
- Path Peek (free, always on): long-press a buddy to see a dotted path up to the first blocker.

## Art direction (premium, child-friendly)
- Soft toy / clay look drawn as vectors: rounded shapes, gentle gradient shading, top highlight, soft shadows.
- Buddies: big glossy eyes with 2 highlights, blush cheeks, tiny mouth. All 24 are drawn in code (no bitmaps).
- Palette: Cream #FFF7EC, Mango #FFB627, Coral #FF6B6B, Mint #3DD6A3, Sky #4CC9F0, Grape #7B61FF, Ink #2B2240.
- Each world has its own gradient sky and board texture.
- Font: Baloo 2 (Google Fonts, OFL), which covers Latin and Devanagari.
- Motion: everything springs. Buttons squash when pressed and give a haptic tick.
- Sound: soft pops, "boing" on bonk, chime on rescue, gentle music per world.

## UX rules
- The first launch goes straight into Level 1 with a tutorial hand. No menus first.
- One-thumb play. HUD top: pause, level, hearts, rescued counter. Bottom: Hint, Undo, Restart.
- Text is short and simple. Every tap answers in under 50 ms (visual + haptic).

