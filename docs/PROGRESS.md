# ArrowBuddies — Progress & Architecture Log

## Roadmap (Steps 1-10)
- [x] **Step 1: Project Foundation** — Gradle setup, multi-module (:engine, :app), architecture skeleton, navigation routes, theme tokens, release/debug builds, tests.
- [x] **Step 2: Premium Design System** — Soft-toy / clay tactile UI tokens, 3D candy buttons, Baloo 2 typography, 8 WorldTheme biomes, spring physics, squishy components, particle burst system, Design Catalog showcase screen.
- [x] **Step 3: Buddy Characters (Procedural Vector Buddies)** — 24 procedural species in code, distinct silhouettes, 8 expressions, directional arrow badge, drawWithCache performance optimization (60 fps), life & charm idle physics, BuddyGalleryScreen.
- [x] **Step 4: Pure Kotlin Game Engine & Solver** — Immutable board models (Direction, Pos, Tile, Buddy, LevelDef, GameState), GameEngine tap physics with path tracing, bonk/escape/mud/turntable/portal/caged/sleepy mechanics, undo stack, safeMoves hint API, LevelCodec JSON parser, 93.4% line coverage unit test suite.
- [x] **Step 5: Playable Board + Juice** — Interactive 60fps board in app/feature/game, GameViewModel with UDF and SavedStateHandle process death survival, 3-layer rendering (drawWithCache tray/checkers/rocks, graphicsLayer avatar movements, particle overlay), immediate feedback (<50ms), escape dash + HUD arc, bonk squash + blocker jiggle + screen shake, dynamic trapped Worried face, long-press Path Peek, Undo, Haptics via performHapticFeedback, all 3 dev levels fully playable.
- [x] **Step 6: Mechanics (Mud, Turntable, Cage + Key, Portal, Sleepy)** — All 5 mechanics implemented with (A) pure Kotlin engine rules, (B) comprehensive unit tests, (C) tactile visuals & 60fps animations (mud puddles/splashes, wooden turntable disc spin, colorblind-safe portals, cage bars/key ribbons, nightcaps, refusal speech bubbles), (D) dev showcase levels (dev 4–9), 3x3 animated mini-demo intro cards with DataStore persistence, and cross-mechanic interaction validation.
- [ ] **Step 7: Level Generator & Catalog (240 Levels)** — 240 level generator with sawtooth difficulty, solvability validator, Daily Rescue seed generator, JSON/binary level serialisation.
- [ ] **Step 8: Sanctuary & World Progression** — Living home screen, 8 biomes, buddies wandering/interacting, decorations, total rescue counter.
- [ ] **Step 9: Buddy Album & Friendship System** — 24 species album, silhouettes, Bronze/Silver/Gold tiers, cosmetics, berry shop.
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
