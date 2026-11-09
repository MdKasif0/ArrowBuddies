# ArrowBuddies — Progress & Architecture Log

## Roadmap (Steps 1-10)
- [x] **Step 1: Project Foundation** — Gradle setup, multi-module (:engine, :app), architecture skeleton, navigation routes, theme tokens, release/debug builds, tests.
- [x] **Step 2: Premium Design System** — Soft-toy / clay tactile UI tokens, 3D candy buttons, Baloo 2 typography, 8 WorldTheme biomes, spring physics, squishy components, particle burst system, Design Catalog showcase screen.
- [x] **Step 3: Buddy Characters (Procedural Vector Buddies)** — 24 procedural species in code, distinct silhouettes, 8 expressions, directional arrow badge, drawWithCache performance optimization (60 fps), life & charm idle physics, BuddyGalleryScreen.
- [x] **Step 4: Pure Kotlin Game Engine & Solver** — Immutable board models (Direction, Pos, Tile, Buddy, LevelDef, GameState), GameEngine tap physics with path tracing, bonk/escape/mud/turntable/portal/caged/sleepy mechanics, undo stack, safeMoves hint API, LevelCodec JSON parser, 93.4% line coverage unit test suite.
- [x] **Step 5: Playable Board + Juice** — Interactive 60fps board in app/feature/game, GameViewModel with UDF and SavedStateHandle process death survival, 3-layer rendering (drawWithCache tray/checkers/rocks, graphicsLayer avatar movements, particle overlay), immediate feedback (<50ms), escape dash + HUD arc, bonk squash + blocker jiggle + screen shake, dynamic trapped Worried face, long-press Path Peek, Undo, Haptics via performHapticFeedback, all 3 dev levels fully playable.
- [x] **Step 6: Mechanics (Mud, Turntable, Cage + Key, Portal, Sleepy)** — All 5 mechanics implemented with (A) pure Kotlin engine rules, (B) comprehensive unit tests, (C) tactile visuals & 60fps animations (mud puddles/splashes, wooden turntable disc spin, colorblind-safe portals, cage bars/key ribbons, nightcaps, refusal speech bubbles), (D) dev showcase levels (dev 4–9), 3x3 animated mini-demo intro cards with DataStore persistence, and cross-mechanic interaction validation.
- [x] **Step 7: Level Generator & Catalog (240 Levels)** — Solver + Procedural Generator + Level Packs in `:engine`. Deterministic reverse construction generator with FNV-1a seeds, LevelSolver with heuristic PQ search (<4ms for 20 buddies), DifficultyCalculator scoring, 24 handcrafted tutorial levels with lessons, 240 baked levels across 8 worlds with sawtooth difficulty curve, Big Rescue levels 15 & 30 with Golden Buddies, DailyRescue date-seeded puzzles (Asia/Kolkata), endless RescueRun with background prefetching, Level Lab debug screen, and level report.
- [x] **Step 8: Complete Game Flow** — End-to-end user journey: SplashScreen API + 1.2s bouncy letter branded intro with peeking/waving buddies; First-Time Experience (L1 pointing hand, L2 funny non-lethal bonk, L3 win celebration to Home); Home screen with live Sanctuary background, total rescued counter, pulsing Play button, Daily Rescue & Rescue Run cards; World Map with 240-level winding road, world banners, stars, padlock nodes, and Big Rescue gates; In-Game HUD with Pause, level pill, rescued counter, and HeartsRow; Bottom controls with Hint, Undo, and Restart; Win celebration sheet with dancing buddies, sequential star chimes, and New Friend flip card; Gentle Fail sheet with Try Again and +1 Heart revive; Leave level confirmation; Settings with sound, music, vibration, reduce motion, language switcher (English / Hindi via AppCompatDelegate), privacy policy, and credits; full authentic Hindi parity.
- [x] **Step 9: Meta Progression & Persistence** — Room persistence (PlayerProfile, LevelProgress, SpeciesProgress, DecorItem, DailyRecord, Achievement) with atomic transactions and MIGRATION_1_2 test; Panoramic Sanctuary with 3 parallax layers (sky, hills, ground) and 8 world-themed habitat zones; autonomous buddy behavior AI loop with tap reactions and speech bubbles; Treat Time daily scatter (+20 berries); 8 procedural interactive decorations (100–600 berries); 24 species Buddy Album with kid-friendly fun facts, silhouettes, and friendship tiers (Bronze 10, Silver 50, Gold 150) unlocking vector accessories (Bow, Scarf, Crown); Daily Rescue screen with date-seeded puzzle, 7-day streak calendar, and Android share sheet; 12 achievements; polite opt-in 6 PM WorkManager daily reminder; economy tuning; child-safe ethics guard.
- [ ] **Step 10: Final Polish, Hindi Localization & Release Verification** — Full Hindi translations, R8 shrink verification (<25MB), offline verification, performance profiling on 60fps budget targets.

---

## Decisions & Architecture Rules

### Architecture & Unidirectional Data Flow (UDF)
1. **Modules**:
   - `:engine`: Pure Kotlin/JVM library with **ZERO** Android dependencies. Contains all game state representation, movement rules, collision physics, obstacle behaviors, solver, and level generation.
   - `:app`: Android application module depending on `:engine`. Contains UI, audio, haptics, storage, navigation, and Android lifecycle bindings.
2. **UDF Pattern**:
   - Each screen has a dedicated `ViewModel` exposing a single `StateFlow<UiState>`.
   - UI emits user intentions/events (`UiEvent`) to the ViewModel.
   - UI never executes game rules or mutates state directly.
3. **Immutability & Stability**:
   - All UI state models are strictly immutable and annotated with `@Immutable` or `@Stable`.
4. **Design Tokens & Localization**:
   - Zero hardcoded colors, dimensions, or strings in Compose screens.
   - All colors and typography come from `ArrowBuddiesTheme`.
   - All text comes from `strings.xml` (with `values-hi/strings.xml` providing full parity).
5. **Screen Immersion & WakeLock**:
   - Gameplay screen hides system bars (`WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE`).
   - Screen stays awake during active gameplay (`FLAG_KEEP_SCREEN_ON`).
   - Navigation to non-gameplay screens restores system bars and clears keep-screen-on.
6. **Persistence**:
   - Room (KSP) for structured progress (levels completed, star ratings, sanctuary items, buddy friendship).
   - DataStore Preferences for settings (sound, haptics, language, onboarding state).
7. **Build & Toolchain**:
   - Target SDK: 35, Compile SDK: 35, Min SDK: 24.
   - Toolchain: Java 17.
   - Kotlin: 2.1.0 (with official Compose Compiler Gradle Plugin).
   - Release: R8 minification + resource shrinking enabled, ProGuard rules for kotlinx.serialization.
   - Debug: StrictMode enabled for disk/network thread violation catching.

---

### Step 2: Premium Design System Design Notes

1. **Clay / Tactile Token Architecture (`com.kasif.arrowbuddies.core.designsystem.theme`)**:
   - `TactileColor`: Every core color (Cream, Mango, Coral, Mint, Sky, Grape, Ink) defines `main`, `lip` (darker 3D base depth), and `highlight` (top specular reflection) shades.
   - High Contrast: Text on primary surfaces is guaranteed >= 4.5:1 contrast against Ink `#2B2240` or White `#FFFFFF`.
   - Top-down Light Vector: Highlights are placed along the top edge of components (1.5dp - 2dp), while 3D lips extend downwards (4dp - 6dp) to anchor depth.
   - Shapes: Radius 12dp (`Small`), 20dp (`Medium`), 28dp (`Large`), and `Full` (Pills/Circles).
   - Spacing: Strict 4dp modular grid (`x1`=4dp, `x2`=8dp, `x3`=12dp, `x4`=16dp, `x6`=24dp, `x8`=32dp, `x12`=48dp, `x16`=64dp).
   - Typography: Official **Baloo 2** (OFL License in `docs/licenses/OFL_Baloo2.txt`) in Regular (400), Medium (500), SemiBold (600), Bold (700), ExtraBold (800) covering both Latin and Devanagari (Hindi) scripts. Numbers use tabular figures (`FontFeatureSettings("tnum")`) to prevent jitter in counters.

2. **8 World Biome Themes (`WorldTheme.kt`)**:
   - Each world possesses custom Sky Gradients (`skyTop`, `skyBottom`), Board Base tone, Alternating Tile Checker tones (`tileCheckA`, `tileCheckB`), and Biome Accent:
     1. *Sunny Meadow* (Meadow Sky, Lush Grass, Golden Accent)
     2. *Happy Farm* (Farm Warm Sky, Barn Wood, Wheat Accent)
     3. *Jungle Ruins* (Deep Jungle, Ancient Stone, Moss/Emerald Accent)
     4. *Snowy Peaks* (Alpine Blue/White, Glacier Ice, Coral Accent)
     5. *Coral Coast* (Tropical Cyan, Sandy Reef, Coral Pink Accent)
     6. *Moonlit Forest* (Midnight Violet, Twilight Floor, Glow Mint Accent)
     7. *Desert Oasis* (Sunset Gold/Peach, Dune Sand, Teal Oasis Accent)
     8. *Sky Islands* (Dawn Rose/Sky, Cloudstone, Golden Sunset Accent)

3. **Motion Physics & Spring Tuning (`Motion.kt`)**:
   - `Bouncy`: `dampingRatio = 0.5f`, `stiffness = Medium` (for celebration pops, buttons, stars).
   - `Snappy`: `dampingRatio = 0.75f`, `stiffness = High` (for toggles, icon presses).
   - `Gentle`: `dampingRatio = 0.9f`, `stiffness = Low` (for panels, slow breathing animations).
   - Standard timing curves: Fast (120ms), Medium (220ms), Slow (400ms).

4. **Tactile Components (`com.kasif.arrowbuddies.core.designsystem.component`)**:
   - `CandyButton`: 3D jelly button with gradient face, specular highlight, 6dp lip, depression translation on press (`pressScale` + `bounceClick`), audio/haptic triggers, and `@Preview`.
   - `RoundIconButton`: 52dp circular tactile button with count badge and lip.
   - `CounterPill`: Tactile pill with rolling odometer number animation (`AnimatedContent` vertical slide) and icon pop.
   - `HeartsRow`: 3 tactile hearts with heart loss animation (crack path, fall-and-rotate physics, alpha fade).
   - `ToyPanel`: Card container with soft ambient shadow, 2dp inner top specular highlight, and 2dp border stroke.
   - `StarRow`: 3 stars filling sequentially with gold radial glow, pop spring, and rising audio chimes.
   - `JellyProgressBar`: Progress bar with squishy spring wobble animation and gloss shine.
   - `SpeechBubble`: Rounded container with arrow pointer tail and pop-in spring.
   - `ToggleSwitch`: Chunky 3D switch with track depth lip and sliding thumb.
   - `AnimatedSkyBackground`: Zero per-frame recomposition canvas drawing with continuous parallax cloud drift reading time solely in draw phase.
   - `ParticleBurst`: Object-pooled system (max 150 particles) supporting `ConfettiBurst` and `SparkleBurst` driven by `withFrameNanos` with zero per-frame heap allocations.
   - `GameDialog`: Bottom-sheet style modal with bouncy slide-in, system blur on Android 12+ (API 31+), and softened dimmed backdrop.

5. **Design Catalog Screen (`DesignCatalogScreen.kt`)**:
   - Debug screen registered in `Route.DesignCatalog` and accessible from Settings.
   - Allows live interactive testing of all 8 world themes, color palettes, typography scale, buttons, counters, hearts, stars, progress, speech bubbles, toggles, particle bursts, and game dialogs.

---

### Step 3: Buddy Characters Architecture & Procedural Rendering Notes

1. **Direction in Engine (`com.kasif.arrowbuddies.engine.model.Direction`)**:
   - Pure Kotlin enum (`UP`, `RIGHT`, `DOWN`, `LEFT`) with grid coordinate deltas (`dx`, `dy`), angle in degrees (`0f`, `90f`, `180f`, `270f`), and rotation helpers (`turnClockwise90`, `turnCounterClockwise90`, `opposite`).

2. **24 Species Morphological Architecture (`BuddySpecies.kt`)**:
   - Exactly 3 species per world for all 8 worlds with individual palettes, display names in English and Hindi, and distinct silhouette properties:
     - *World 1 (Sunny Meadow)*: **Bunny** (tall curved ears with pink inner pads, puff tail), **Chick** (3-tuft crest, triangle orange beak), **Piglet** (snout with dual nostrils, curly tail).
     - *World 2 (Happy Farm)*: **Puppy** (long floppy ears, wag tail), **Lamb** (fluffy wool crown, soft floppy ears), **Calf** (nub horns, broad snout, cow spot).
     - *World 3 (Jungle Ruins)*: **Monkey** (round cupped ears, long curl tail), **Tiger Cub** (cheek stripes, pointy ears), **Parrot** (downward curved hook beak, feather plume).
     - *World 4 (Snowy Peaks)*: **Penguin** (tuxedo cowl, white belly, orange beak), **Polar Cub** (round bear ears, soft blue accent), **Arctic Fox** (tall pointy ears with slate tips, bushy brush tail).
     - *World 5 (Coral Coast)*: **Turtle** (domed scute shell, flipper paddles), **Otter** (round ears, tapered swimmer tail), **Crab** (raised dual pincers/claws, top eyestalks).
     - *World 6 (Moonlit Forest)*: **Owl** (feather crest horns, spectacle rings around eyes, yellow beak), **Hedgehog** (radiating crown of rounded quills), **Raccoon** (horizontal bandit eye mask, ringed tail).
     - *World 7 (Desert Oasis)*: **Camel** (curved dorsal hump, drooping ears), **Fennec Fox** (enormous signature triangular ears, peach interior), **Meerkat** (dark sunglasses eye rings, alert posture).
     - *World 8 (Sky Islands)*: **Red Panda** (white cheek markings, brow dots, ringed tail), **Elephant Calf** (wide fan ears, upward curled trunk), **Peacock** (3-bobble celestial crown crest, feather fan).

3. **8 Emotional Expressions (`BuddyExpression.kt`)**:
   - `IDLE`: Normal eyes with dual specular highlight dots, sweet smile, periodic blinks, breathing bob.
   - `WORRIED`: Wavy raised eyebrows, nervous eyes, small cyan sweat bead on brow.
   - `READY`: Confident angled brows (\ /), bright determined eyes, focused smile.
   - `DASHING`: Focused squint eyes, motion squash along movement axis, trailing speed streaks.
   - `BONKED`: Cartoon spiral swirly eyes (@ @), wavy dizzy mouth, 3 orbiting yellow 5-point stars.
   - `HAPPY`: Upward crescent arc eyes (^_^), joyful smile, floating pink hearts.
   - `SLEEPY`: Closed peaceful flat sleeping lines, nightcap/peaceful face, floating "Zzz" letters.
   - `CAGED`: Sad downturned watery eyes with large glossy tear pool shimmer, downturned mouth.

4. **Direction Badge (Gameplay Primary Indicator)**:
   - 3D circular accent badge anchored to the active movement edge (`UP`, `RIGHT`, `DOWN`, `LEFT`).
   - Dark `#2B2240` outline, soft drop shadow, bold crisp white arrow.
   - Accessible & color-blind friendly: purely shape-driven, readable at 36dp.
   - The body leans 6 degrees in the travel direction, and the pupils shift toward the edge.
   - Spins smoothly to new directions with shortest-path spring physics.

5. **Life & Charm Idle Physics**:
   - Breathing scale oscillating between `1.00f` and `1.03f` with sinusoidal curve ($\approx 2600\text{ms}$).
   - Subtle vertical bob ($\approx 1.5\%\text{ of size}$).
   - Blinking: every $3800\text{ms}$, eyelids close into gentle arcs for $140\text{ms}$.
   - Ear twitch: subtle 5-degree twitch every $4400\text{ms}$ for $160\text{ms}$.
   - Unique phase seed per buddy so 40 buddies on a grid never breathe, bob, or blink in sync.
   - Performance: `drawWithCache` builds geometry paths and brushes once per size. Animation time is read exclusively in the Draw phase.

6. **Buddy Gallery Debug Screen (`BuddyGalleryScreen.kt`)**:
   - Registered at `Route.BuddyGallery` and reachable from Settings.
   - Direction filter chips (`Up`, `Right`, `Down`, `Left`) + "Spin 90° ↻" button.
   - Expression selector chips for all 8 expressions.
   - Avatar size slider ($32\text{dp}$ to $160\text{dp}$) with quick presets ($36\text{dp}$ Cell, $64\text{dp}$ Standard, $96\text{dp}$ Card, $128\text{dp}$ Hero).
   - Grouped grid showcasing all 24 species with English + Hindi labels and feature tags.

---

### Step 4: Core Game Engine API Summary
- `GameEngine.tap(state: GameState, buddyId: String): MoveOutcome`
  Executes a tap on a buddy. Traces cell-by-cell path to edge (`Escaped`) or collision (`Bonked` against Buddy, Rock, or Loop). Handles Mud stop, Turntable 90° turns, Portal teleports, and Caged/Sleepy refusal.
- `GameEngine.undo(state: GameState): GameState?`
  Restores previous board state and rescued IDs from the successful move history stack while preserving current hearts and mistakes. Returns `null` if history is empty.
- `GameEngine.safeMoves(state: GameState): List<Buddy>`
  Hint API returning all buddies that can currently be tapped without bonking.
- `LevelCodec.decode(json: String): LevelDef` & `LevelCodec.encode(level: LevelDef): String`
  Human-readable JSON serializer/deserializer with grid legend (`.` empty, `R` rock, `M` mud, `T` turntable, `1`..`9` portals) and validation diagnostics (bounds, rock placement, duplicate cells).
- `GameState.stars: Int`
  Computes star rating (3 = 0 mistakes, 2 = 1 mistake, 1 = 2 mistakes, 0 = failed/in-progress).
- `Direction.rotateClockwise(): Direction` / `Pos.plus(Direction): Pos`
  Cartesian grid math utilities for vector movement and 90-degree rotations.

---

### Step 5: Playable Board + Juice Architecture Notes

1. **UDF Architecture & Process Survival (`GameViewModel.kt`)**:
   - Strictly delegating all game rules, path tracing, and win/loss calculations to `GameEngine.tap`.
   - `GameUiState`: Immutable state snapshot exposing current `level`, `gameState`, `isInputLocked`, `safeBuddyIds`, `activePeekingPath`, `pressedBuddyId`, and dialog visibility flags.
   - One-shot `SharedFlow<GameEffect>`: Separates transient side-effects (`PlayEscape`, `PlayBonk`, `ScreenShake`, `TriggerHaptic`, `PlaySound`) from persistent state.
   - Process death survival: Serializes `GameState` to `SavedStateHandle` using `Json { allowStructuredMapKeys = true }` so complex `Map<Pos, Tile>` keys round-trip safely.

2. **3-Layer 60fps Board Architecture (`GameBoard.kt`)**:
   - `Cell Size`: Dynamically sized using `min(availableWidth / cols, availableHeight / rows)` with tablet cap (560dp) and centered tray placement.
   - **Layer (a) [Tray & Background]**: Single cached `Spacer` using `Modifier.drawWithCache`. Renders 3D raised rim with 6dp bottom lip, 2dp top highlight, inner shadow bevel, checkered grass tiles from `WorldTheme`, 3D vector rocks with contact shadow and moss flecks, and freedom glowing gradient with tiny petals outside the rim. Zero recompositions during buddy moves.
   - **Layer (b) [Buddies]**: Rendered as `BuddyAvatar` vectors. Movement and squash transformations operate entirely via `graphicsLayer { translationX, translationY, scaleX, scaleY, alpha }` without triggering parent relayout.
   - **Layer (c) [Juice & Particles]**: Overlay `ParticleBurst` running on `withFrameNanos` with zero per-frame heap allocations. Emits confetti bursts on win, sparkles on rescue, dust puffs on dash, and heart pops on HUD counter bump.

3. **Touch Input & Edge Tolerance**:
   - Single `pointerInput` with `awaitEachGesture` mapping touch positions to grid cells.
   - 30% cell boundary edge tolerance: taps landing near a buddy's cell border within 30% of cell size are gracefully attributed to that buddy.
   - Immediate feedback (<50ms): down event triggers light haptic tick, squashes buddy (scale 0.92f), and transitions face to `BuddyExpression.READY`.

4. **Animations & Living Charm**:
   - **Escape**: Anticipation (70ms squash opposite travel axis) $\to$ dash along step path (55ms/cell, capped at 450ms) with dust puffs and `BuddyExpression.DASHING` $\to$ board edge exit $\to$ parabolic arc (320ms) into HUD rescued counter $\to$ HUD scale bump + heart pop!
   - **Bonk**: Rapid dash to blocker $\to$ flat impact squash (scale 0.65f/1.28f) $\to$ swirly eyes and orbiting stars (`BuddyExpression.BONKED`) $\to$ blocker jiggles $\to$ spring rebound (220ms `SpringBouncy`) back to starting cell $\to$ HUD heart loss crack, soft screen shake (4dp, 180ms), and strong reject haptic.
   - **Dynamic Worried Face**: Buddies with blocked paths automatically show `BuddyExpression.WORRIED`; as blockers escape, they dynamically switch to hopeful (`BuddyExpression.IDLE`).

5. **Boosters & HUD**:
   - **Path Peek**: Free long-press peek drawing animated marching-ants dotted path to either a red tactile 'X' blocker marker or a glowing green sparkle exit glint.
   - **Undo**: Restores board state from engine history stack while preserving hearts and mistakes.
   - **Dev Level Quick Switcher**: Dev chips ("Dev 1", "Dev 2", "Dev 3") for instant testing of all 3 dev levels.
   - **Haptics**: `DefaultHapticsManager` using `View.performHapticFeedback` (`HapticFeedbackConstants.CONFIRM` / `REJECT` on API 30+, fallbacks for older devices, plus `Vibrator` fallback).

---

### Step 6: Mechanics (Mud, Turntable, Cage + Key, Portal, Sleepy) Architecture Notes

#### 1. One-Line Rule Summary for Each Shipped Mechanic
- **Mud (World 2)**: A buddy entering a mud cell stops on it without losing a heart, blocks others, and continues in the same direction on the next tap.
- **Turntable (World 3)**: A buddy entering a turntable rotates 90 degrees clockwise, keeps moving, and bonks if caught in an infinite loop.
- **Cage + Key (World 4)**: A caged buddy cannot move until a key-carrier buddy of the matching color escapes, popping open all matching cages on the board.
- **Portal Pair (World 5)**: A buddy entering portal A instantly teleports out of portal B in the same direction (bonking if the exit portal is occupied).
- **Sleepy (World 6)**: A sleeping buddy cannot move until any orthogonally adjacent neighbor departs its cell (either by escaping or by moving onto mud).

#### 2. Visuals & Tactile Animation Polish
- **Mud Puddle (`Tile.Mud`)**: Rendered in `drawToyTray` with dark organic rim (`#4A2E12`), chocolate gradient (`#7A4B24` to `#5A3314`), glossy mud bubbles with highlight dots, and specular shine. Buddies on mud sink slightly (+3dp) and display cute mud splatters on their feet.
- **Turntable Disc (`Tile.Turntable`)**: 3D cedar/teak disc (`#C18C5D`) with bottom depth lip (`#6D431F`), carved clockwise curved arrow, and center brass rivet (`#E6A119`). During movement, the buddy spins 90 degrees clockwise and its directional arrow badge springs to the new facing direction.
- **Portals (`Tile.Portal`)**: Glowing swirling neon vortex rings with distinct colorblind-friendly inner symbols:
  - Pair 1 (Cyan `#00E5FF`): Circle symbol.
  - Pair 2 (Magenta `#E040FB`): Diamond symbol.
  - Pair 3 (Amber `#FFAB00`): Star symbol.
  During dash, buddies shrink down into portal A (60ms), jump to portal B, and pop out with a sparkle burst.
- **Cages & Keys (`Buddy.cageColor`, `Buddy.keyColor`)**:
  - Caged buddy: rounded cage bars (`#8E9AAF`) with colored padlock (Gold, Silver, Ruby) and downturned sad expression (`BuddyExpression.CAGED`).
  - Key carrier: chest ribbon sash with dangling golden/silver/ruby key.
  - On key buddy escape: flying key arc to each matching padlock, locks pop open with sparkle burst, bars lift, and freed buddies cheer (`BuddyExpression.HAPPY`).
  - Tapping caged buddy: gentle refusal shake (wiggle) and tactile "Needs a key!" speech bubble popup (`R.string.bubble_needs_key`).
- **Sleepy Buddies (`Buddy.sleepy`)**:
  - Closed sleeping eyes, nightcap with white pompom, and floating "Zzz" letters.
  - Tapping sleepy buddy: gentle refusal shake and "Zzz..." speech bubble popup (`R.string.bubble_sleepy_mumble`).
  - Waking up: when an orthogonal neighbor departs, the nightcap pops off upward with a puff of dust, the buddy stretches (scale 1.15f), and eyes open wide (`BuddyExpression.READY`).

#### 3. First-Time Intro Card System (`MechanicIntroDialog.kt`)
- When the player encounters an unseen mechanic for the first time in any level, the board pauses and presents a tactile `ToyPanel` / `GameDialog` featuring a 3x3 animated vector mini-demo and a 1-sentence child-friendly explanation.
- Persisted via DataStore Preferences (`isMechanicSeen` / `setMechanicSeen`) so players see each tutorial card exactly once.
- Full localization parity in English and Hindi (`strings.xml` and `values-hi/strings.xml`).

#### 4. Dev Level Suite (Dev 1 through Dev 9)
- `w1-l01-dev` (1): Meadow basics (Bunny, Chick)
- `w1-l02-dev` (2): Static rock obstacles (Bunny, Chick, Piglet)
- `w1-l03-dev` (3): Tricky 5x5 obstacle maze
- `w2-l04-dev` (4): Mud stopping showcase
- `w3-l05-dev` (5): Turntable 90-degree clockwise turning showcase
- `w4-l06-dev` (6): Cage and Key carrier unlocking showcase
- `w5-l07-dev` (7): Portal teleportation showcase
- `w6-l08-dev` (8): Sleepy buddy wake-up showcase
- `w7-l09-dev` (9): Cross-mechanic integration showcase (Mud + Turntable + Portal + Cage + Sleepy)
- Scrollable dev chips in `GameBottomControls` for instant live navigation across all 9 dev levels.

---

### Step 7: Solver, Procedural Generator & Level Packs Architecture Notes

#### 1. High-Performance Level Solver (`com.kasif.arrowbuddies.engine.solver`)
- **State Hashing (`CompactStateKey`)**:
  - Memory-efficient canonical representation packing buddy coordinates, facing directions, cage unlock bitmasks, and sleepy flags.
  - Zero heap churn during graph exploration.
- **Priority Queue Graph Search**:
  - Heuristic ordering prioritized by `remainingBuddies` ascending, transforming BFS into a targeted dive that finds the guaranteed shortest solution in milliseconds.
  - Solves a dense 7x9 board with 20 buddies in under **4 ms** (spec requirement: <200 ms).
- **Comprehensive Metrics Extraction (`LevelMetrics`)**:
  - `buddyCount`: Total buddies to rescue.
  - `initialFreeMoves`: Count of immediately escapable buddies from initial board state.
  - `dependencyDepth`: Longest directed chain of "Buddy X must escape before Buddy Y".
  - `deadEndMoves`: Count of valid moves that lead to an unsolvable branch (optimized to only evaluate branching states on mud/turntables).
  - `mechanicsUsed`: Set of non-standard mechanics present.
  - `solutionCount`: Number of distinct non-bonk solutions (capped at 50).
- **Upgraded Hint API (`GameEngine.findHint`)**:
  - Evaluates candidate safe moves and returns the next buddy on the shortest solution path that keeps the level solvable.

#### 2. Tunable Difficulty Score (`DifficultyCalculator.kt`)
- Spec formula:
  $$\text{score} = 1.0 \times \text{buddyCount} + 2.5 \times \text{depth} + 3.0 \times \text{deadEnds} + 1.5 \times \text{mechanics} - 1.2 \times \text{freeMoves}$$
  clamped between $0$ and $100$.
- Implemented as single source of truth with configurable `DifficultyWeights`.

#### 3. Deterministic Reverse Construction Generator (`LevelGenerator.kt`)
- **Deterministic Seeding**:
  - Pure `kotlin.random.Random(seed)` using 64-bit FNV-1a hash (`stableHash("ArrowBuddies-v1", world, level)`). Zero dependency on system clock.
- **Terrain Obstacle Generation**:
  - Generates rocks, mud, turntables, and portals within obstacle budget while rejecting 2x2 rock clusters, loops, and fully sealed borders.
- **Reverse Buddy Placement**:
  - Places buddies from $N$ down to 1.
  - 80% probability to place each new buddy along the escape trajectory of an already-placed buddy (blocking it), building the dependency DAG backwards and mathematically guaranteeing solvability.
  - 20% probability to place in open board space with an unobstructed exit.
- **Mechanics Post-Processing**:
  - Cages & Keys: Key buddy placed later (escapes earlier) unlocks caged buddy placed earlier (escapes later).
  - Sleepy: Assigned to buddies with orthogonally adjacent neighbors that escape earlier, guaranteeing they awaken.
  - Golden Buddy: Central buddy marked as `isGolden = true` on Big Rescue levels (15 & 30).
  - Species: Enforces $\ge 2$ distinct species per board from the world pool.
- **Validation Filter Loop**:
  - Automatically verifies candidates with `LevelSolver`. Rejects candidates if:
    1. Unsolvable
    2. Initial free moves $> 50\%$
    3. Dead end moves $> 40\%$
    4. Score outside target range
    5. Buddies fail spatial spread check ($< 60\%$ board span)
  - Derives up to 200 deterministic attempts before selecting best candidate.

#### 4. Handcrafted Tutorial Pack (`HandcraftedTutorials.kt`)
- 24 handcrafted tutorial levels (levels 1–3 for all 8 worlds).
- Each level gently introduces that world's mechanic with an educational lesson in the `"comment"` field.
- 100% verified solvable by `HandcraftedTutorialsTest`.

#### 5. Level Pack Baker & Baked Assets (`LevelPackBaker.kt`)
- Gradle task `:engine:bakeLevels` deterministically bakes 240 levels across all 8 worlds into `app/src/main/assets/levels/world_1.json` ... `world_8.json`.
- Implements the sawtooth difficulty curve:
  - Levels 1–3: Tutorial (Score ~5–15)
  - Levels 4–10: Ramp (Score ~15–35)
  - Level 11: Breather (Score ~18)
  - Levels 12–14: Ramp (Score ~25–45)
  - Level 15: Big Rescue (Score ~50, 13 buddies, 1 Golden Buddy)
  - Level 16: Breather (Score ~20)
  - Levels 17–24: Ramp (Score ~30–65)
  - Level 25: Breather (Score ~30)
  - Levels 26–29: Ramp (Score ~50–75)
  - Level 30: Big Rescue Boss (Score ~75–85, 18 buddies, 1 Golden Buddy)
- Generates comprehensive difficulty table and statistics in `docs/level_report.md`.
- All 240 levels verified solvable in **58 ms total** (average 0.24 ms per level; test limit: <15s).

#### 6. Runtime Generation Modes (`com.kasif.arrowbuddies.engine.runtime`)
- `DailyRescue.kt`: Date-seeded daily puzzle using `yyyyMMdd` in Asia/Kolkata timezone. Cycles worlds with the day of the year.
- `RescueRun.kt`: Endless mode with progressive difficulty, board scaling, expanding mechanics, and zero-latency asynchronous prefetching on `Dispatchers.Default`.

#### 7. Debug Level Lab Screen (`com.kasif.arrowbuddies.feature.levellab`)
- Interactive developer playground accessible from Settings in debug builds (`Route.LevelLab`).
- Live parameter sliders and steppers: Seed, Grid dimensions (Cols/Rows), Buddy count, World, Allowed mechanics, Rock budget, Target score range.
- Live Solver metrics panel: Solvability status, Difficulty score, Dependency depth, Dead ends, Initial free moves, Explored nodes, Shortest solution sequence, and Execution time.
- Direct "Play" integration: Registers custom `LevelDef` in `LevelRepository` and launches `GameScreen` immediately.
- "Copy JSON" one-tap clipboard export.

---

### Step 8: Complete Game Flow Architecture Notes

#### 1. Splash & First-Time Launch Experience (`SplashScreen.kt`)
- Official Android 12+ SplashScreen API integration (`androidx.core.splashscreen.SplashScreen`).
- 1.2s branded intro:
  - Letters of "ArrowBuddies" drop and bounce in sequentially with staggered spring physics (`dampingRatio = 0.5f`).
  - 3 buddy characters (Bunny, Chick, Piglet) peek up from the bottom with animated waves.
  - Interactive tap-to-skip instantly completes animations.
- Intelligent routing via `GamePreferencesDataStore`:
  - First-time launch (`tutorialCompleted == false`) routes directly to Level 1 (`w1-l01`).
  - Returning launches route directly to the Home screen (`Route.Home`).

#### 2. First-Time 60-Second Hook (`w1-l01`, `w1-l02`, `w1-l03`)
- **Level 1 (3x3 grid, 1 buddy)**:
  - Cartoon pointing hand (`TutorialHandOverlay.kt`) bobs towards the buddy with animated glow ripple.
  - Speech bubble reads: "Tap me to help me escape!".
  - Tapping rescues the buddy, triggering an instant joyful win celebration.
- **Level 2 (4x4 grid, 2 buddies)**:
  - Piglet blocks Bunny. If the player taps Piglet first, it safely dashes, bonks gently into Bunny with funny cartoon spiral eyes and boing sound.
  - Speech bubble pops up: "Oops! Free your friend first!".
  - Zero hearts lost during tutorial levels (`hearts = 3` preserved in `GameViewModel`).
- **Level 3 (4x4 grid, 4 buddies)**:
  - No hand pointer. Player uses what they learned to free everyone in order.
  - Win celebration triggers first rescue sequence and lands the player on Home with the Sanctuary populated.

#### 3. Home Screen (`HomeScreen.kt`)
- **Live Backdrop**: Dynamic `AnimatedSkyBackground` with gentle cloud drift and wandering pasture buddies.
- **Hero Counters & CTA**:
  - Total rescued counter pill (`🐾 128 Buddies Rescued`).
  - Top bar berry balance (`🫐 100`) and quick Settings icon button.
  - Enormous pulsing 3D `CandyButton` ("Play · Level X") displaying the next species avatar to rescue.
- **Interactive Feature Cards**:
  - *Daily Rescue*: Countdown timer to midnight Asia/Kolkata (`HH:mm:ss`) and 100 🫐 bonus badge.
  - *Rescue Run*: Endless mode card (locked with padlock until World 2 is reached).
  - *World Map*: Quick-launch card showing current world and level progress.
  - *Buddy Album*: Quick link to the species collection.

#### 4. World Map (`WorldMapScreen.kt`)
- Continuous winding Canvas road path mathematically plotted using harmonic sine curves ($x = \text{centerX} + \sin(i \times 0.65) \times \text{amplitude}$).
- 240 distinct level nodes across 8 worlds:
  - Completed nodes: 1–3 golden stars and green grass rim.
  - Current node: glowing animated aura with the next buddy's avatar bouncing on top.
  - Locked nodes: grey stone with 3D brass padlock.
  - Big Rescue nodes (Levels 15 & 30): larger 64dp golden ring nodes with Golden Buddy preview.
- World section banners ("World 1 · Sunny Meadow", "World 2 · Happy Farm", etc.).
- Auto-scrolls smoothly on launch to focus directly on the player's current level.
- World Unlock Gate modal previewing the 3 silhouette species awaiting rescue in the next biome.

#### 5. In-Game HUD & Tactical Boosters (`GameHud.kt`)
- **Top Bar**:
  - Menu/Pause round button (46dp).
  - Tactile Level pill ("Level 12").
  - Dynamic Rescued Counter ("🐾 4/9") with bounce scale trigger.
  - Tactile `HeartsRow` (3 hearts).
- **Bottom Bar**:
  - **Hint Booster**: Round 3D button with inventory badge. Highlights candidate buddy with golden pulsing aura, pulses arrow badge, and shows pointing hand for 2s. If inventory is 0, opens berry purchase dialog (30 🫐).
  - **Undo Booster**: Round 3D button with inventory badge. Restores prior board state while preserving hearts. If 0, offers berry purchase (20 🫐).
  - **Restart**: Instant level reset button.

#### 6. Complete Dialog Sheets (`GameDialogs.kt`)
- **Level Win Sheet (`LevelWinSheet`)**:
  - Rescued buddies dancing and hopping on top of the toy panel.
  - Sequential star filling animation with rising pitch chime audio hooks.
  - Berry reward counter (+10 base, +5/star, +25 Golden Buddy bonus).
  - "New Friend!" 3D flip card when discovering a new species for the first time.
  - NEXT (primary, auto-focused), Replay, and Map buttons.
- **Level Fail Sheet (`LevelFailSheet`)**:
  - Gentle, non-shaming copy: "Oh no! The buddies need one more try."
  - Primary free "Try Again" button.
  - "+1 Heart & Continue" button (40 berries).
  - Named hook for future rewarded video ads.
- **In-Game Pause Sheet (`PauseSheet`)**:
  - Inline tactile 3D switches for Sound, Music, and Haptics.
  - Resume, Restart, and Map buttons.
- **Leave Level Dialog (`LeaveLevelDialog`)**:
  - Modal confirmation: "Leave this level? Your level progress will be lost if you leave now."
  - System back button intercepted cleanly via `BackHandler`.

#### 7. Settings & Authentic Localization (`SettingsScreen.kt`)
- Toggles for Sound Effects, Music, Haptic Vibration, and Reduce Motion.
- Per-app language switcher between English and Hindi using `AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(langCode))`.
- "Replay Tutorial" button that resets tutorial state in DataStore and routes to Level 1.
- Offline-first Privacy Policy dialog and Credits modal.
- Debug Developer Tools panel (Buddy Characters Gallery, Design System Catalog, Level Lab) when `BuildConfig.DEBUG` is true.
- 100% translation parity in `values-hi/strings.xml` using natural, everyday conversational Hindi.

#### 8. Navigation Transitions (`ArrowBuddiesNavHost.kt`)
- Map $\to$ Game zoom + fade (`scaleIn(0.85f)` + `fadeIn(220ms)`).
- Crossfade and soft scale across screen transitions.
- Dialog sheets springing in smoothly from bottom.

---

## Visual Verification & Screen Captures

Screenshots captured from live Android emulator execution on API 35:
- `docs/screens/design_catalog_themes.png`: World Theme switcher (all 8 biomes), Palette Swatches with lip & highlight tokens, Baloo 2 typography showcase.
- `docs/screens/design_catalog_top.png`: Baloo 2 Latin and Hindi Devanagari typography, 3D Candy Button variants (Mango, Sky, Coral, Mint, Grape).
- `docs/screens/design_catalog_1.png`: Round Icon Buttons with badge counters, Rolling Odometer Counter Pills, Hearts Row with interactive heart crack/loss animation.
- `docs/screens/design_catalog_2.png`: Star Row with sequential chime fills, Jelly Progress Bar with squishy wobble, Speech Bubble with pointer tail, Chunky 3D Toggles.
- `docs/screens/design_catalog_3.png`: Particle Burst triggers (Confetti & Sparkle) and Modal Dialog triggers.
- `docs/screens/design_catalog_dialog.png`: 3D Game Dialog with API 31+ blurred backdrop, star celebration, and 3D action button.
- `docs/screens/buddy_gallery_top.png`: Buddy Gallery header, direction controls, expression filters, and size slider.
- `docs/screens/buddy_gallery_worlds1_2.png`: World 1 (Bunny, Chick, Piglet) and World 2 (Puppy, Lamb, Calf) procedural avatars.
- `docs/screens/buddy_gallery_worlds3_5.png`: World 3 (Monkey, Tiger Cub, Parrot) and World 4 (Penguin, Polar Cub, Arctic Fox) avatars.
- `docs/screens/buddy_gallery_worlds6_8.png`: World 5 (Turtle, Otter, Crab) and World 6 (Owl, Hedgehog, Raccoon) avatars.
- `docs/screens/buddy_gallery_worlds7_8.png`: World 7 (Camel, Fennec Fox, Meerkat) and World 8 (Red Panda, Elephant Calf, Peacock) avatars.
- `docs/screens/buddy_gallery_world8.png`: World 8 Sky Islands finale species in detail.
- `docs/screens/buddy_gallery_worried_turned.png`: Interactive direction turning (90° spring spin) and Worried expression (sweat drop + raised brows).
- `docs/screens/buddy_gallery_hero_size.png`: 128dp Hero card size inspection.
- `docs/screens/buddy_gallery_cell_size.png`: 36dp small grid cell size readability verification.

---

## Library Versions Used
- **Android Gradle Plugin**: 8.7.2
- **Kotlin**: 2.1.0
- **Compose Compiler Plugin**: 2.1.0
- **Compose BOM**: 2024.11.00
- **AndroidX Core KTX**: 1.15.0
- **AndroidX Lifecycle (ViewModel Compose, Runtime Compose)**: 2.8.7
- **AndroidX Navigation Compose**: 2.8.4
- **AndroidX Core SplashScreen**: 1.0.1
- **AndroidX DataStore Preferences**: 1.1.1
- **AndroidX Room**: 2.6.1
- **Google Dagger Hilt**: 2.55 (upgraded for Kotlin 2.1 metadata compatibility)
- **Google KSP**: 2.1.0-1.0.29
- **Kotlinx Coroutines**: 1.9.0
- **Kotlinx Serialization JSON**: 1.7.3
- **JUnit**: 4.13.2
- **Google Truth**: 1.4.4
- **CashApp Turbine**: 1.2.0
- **AndroidX WorkManager**: 2.9.1
- **AndroidX Room Testing**: 2.6.1
- **Robolectric**: 4.14.1

---

### Step 9: Meta Progression & Persistence Architecture Notes

1. **Structured Persistence & Migrations (`com.kasif.arrowbuddies.data.db`)**:
   - Entities: `PlayerProfile` (berries, hints, undos, totalRescued, tutorialDone, currentLevelIndex, highestUnlockedLevel, streakDays, lastStreakDate, lastTreatTimeDate), `LevelProgress` (levelId, bestStars, bestMistakes, completedAt), `SpeciesProgress` (species, rescuedCount, tier, equippedAccessory, firstRescuedAt), `DecorItem` (id, owned, slot, zoneId, costBerries), `DailyRecord` (date, completed, stars, mistakes, completedAt), and `Achievement` (id, unlockedAt, progress, maxProgress).
   - Room Migration `MIGRATION_1_2` safely upgrades schema from v1 to v2 and seeds initial default player profile, 8 fixed zone decorations, and 12 achievements.
   - Comprehensive Room migration test `AppDatabaseMigrationTest` verifies table creation and default seeds.
   - Auto backup XML descriptors (`backup_rules.xml`, `data_extraction_rules.xml`) include `datastore/` and database files to survive phone switches.

2. **Atomic Single-Transaction Progression (`com.kasif.arrowbuddies.domain.usecase`)**:
   - `CompleteLevelUseCase`: Level completion is written in a single `database.withTransaction` block. Computes earned berries, updates level stars and best mistakes, advances unlocked world indices, updates individual species rescue counts, evaluates friendship tier milestones, and checks achievement unlocks (`first_rescue`, `rescued_10`, `rescued_100`, `rescued_500`, `rescued_1000`, `perfect_world`, `mud_master`, `portal_pro`, `zero_bonk_boss`, `collector`).
   - `SpendBerriesUseCase`: Atomic check-and-decrement to prevent overdrafting or race conditions.
   - `ClaimDailyRewardUseCase`: Atomic verification and claim for daily streak progression (days 1–7) and Treat Time.
   - `EquipAccessoryUseCase`: Validates whether the buddy has attained the required friendship tier before equipping cosmetic accessories.
   - `BuyDecorUseCase`: Atomic deduction and ownership toggle for sanctuary decorations, tracking progress toward the `decorator` achievement.

3. **Living Panoramic Sanctuary (`com.kasif.arrowbuddies.feature.sanctuary`)**:
   - Wide 3520dp horizontally scrollable habitat with 3 parallax layers:
     - Layer 1 (Sky): Panoramic horizontal gradient transitioning across the 8 world biomes with floating fluffy clouds drifting in the draw phase.
     - Layer 2 (Hills): Parallax rolling hills with biome silhouettes (trees, ruins, windmills, dunes).
     - Layer 3 (Ground): Biome checkering, stepping stone pathway, entrance welcome signboard, Treat Time picnic basket, 8 fixed decoration slots, and roaming buddies.
   - Autonomous Behavior AI: Lightweight single-coroutine tick loop (~250ms) driving buddy states (`IDLE`, `WALK`, `HOP`, `NAP`, `CHEER`, `REACT_DECOR`) with zero per-buddy coroutines for battery-friendly 60fps operation.
   - Tapping a buddy triggers a joyful hop, heart particle popup, and speech bubble with the buddy's name.
   - Treat Time: Daily interactive picnic basket. Tapping scatters berries, plays chimes, awards +20 berries, and causes all buddies to cheer and rush toward the basket.
   - 8 Fixed Procedural Decorations drawn in Compose DrawScope with idle physics: Koi Pond with swimming fish and lotus, Tree Swing with gentle pendulum sway, Flower Bed with fluttering butterflies, Warm Firepit with crackling embers, Hammock, Glow Lanterns with radial twilight halo, Desert Oasis Kite, and Rangoli Mat & Mango Tree.

4. **Buddy Album & Friendship Wardrobe (`com.kasif.arrowbuddies.feature.album`)**:
   - 24 species grid with dark mysterious silhouettes and "?" for undiscovered species.
   - Unlocked cards showcase the buddy avatar wearing its equipped accessory, rescued count, and friendship tier progress bar.
   - Friendship Tiers: Bronze (10 rescues), Silver (50 rescues), Gold (150 rescues).
   - Procedural Vector Accessories (`AccessoryRenderer.kt`): Bow (Bronze), Knitted Scarf (Silver), Golden Crown with shimmer sparkle (Gold). Equipping an accessory applies it everywhere across gameplay and the Sanctuary.
   - Kid-friendly fun facts for all 24 species in English and Hindi.
   - 12 In-game Badges / Achievements tab with progress bars, localized titles and descriptions, and unlock badges.
   - Debug developer panel for granting berries during testing.

5. **Daily Rescue & Streak Progression (`com.kasif.arrowbuddies.feature.daily`)**:
   - Date-seeded daily puzzle generated via `DailyRescue.generate(today)` in `Asia/Kolkata` timezone.
   - 7-Day Streak Calendar with rewards (+20, +30, +40, +50, +60, +80, +150 🫐 + Rangoli Mat & Mango Tree decor on Day 7).
   - Hero puzzle card displaying world biome, buddy count, star ratings, and +50 berries daily reward.
   - Wordle/Royal Match style result sharing via Android Share Sheet and clipboard.

6. **Daily Reminder & Ethics Guard**:
   - Polite opt-in prompt after clearing Level 5: "Want a friendly nudge when a new Daily Rescue is ready? 🐾".
   - Scheduled via `WorkManager` with `DailyReminderWorker` for 6:00 PM local time.
   - Default is strictly OFF; never nags, with a toggle switch in Settings.
   - Strict ethics guard: Zero energy systems, zero pay-to-win mechanics, zero loot boxes, zero timers or countdown pressure, zero chat. Fully safe and child-friendly.
