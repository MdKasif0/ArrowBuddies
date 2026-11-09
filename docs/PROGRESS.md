# ArrowBuddies — Progress & Architecture Log

## Roadmap (Steps 1-10)
- [x] **Step 1: Project Foundation** — Gradle setup, multi-module (:engine, :app), architecture skeleton, navigation routes, theme tokens, release/debug builds, tests.
- [x] **Step 2: Premium Design System** — Soft-toy / clay tactile UI tokens, 3D candy buttons, Baloo 2 typography, 8 WorldTheme biomes, spring physics, squishy components, particle burst system, Design Catalog showcase screen.
- [x] **Step 3: Buddy Characters (Procedural Vector Buddies)** — 24 procedural species in code, distinct silhouettes, 8 expressions, directional arrow badge, drawWithCache performance optimization (60 fps), life & charm idle physics, BuddyGalleryScreen.
- [x] **Step 4: Pure Kotlin Game Engine & Solver** — Immutable board models (Direction, Pos, Tile, Buddy, LevelDef, GameState), GameEngine tap physics with path tracing, bonk/escape/mud/turntable/portal/caged/sleepy mechanics, undo stack, safeMoves hint API, LevelCodec JSON parser, 93.4% line coverage unit test suite.
- [ ] **Step 5: Level Generator & Catalog** — 240 level generator with sawtooth difficulty, solvability validator, Daily Rescue seed generator, JSON/binary level serialisation.
- [ ] **Step 6: Core Gameplay Screen** — Interactive board, tap dashing, path peek, bonk bounce, escape animations, HUD (hearts, pause, boosters), 60fps budget optimization.
- [ ] **Step 7: Sanctuary & World Progression** — Living home screen, 8 biomes, buddies wandering/interacting, decorations, total rescue counter.
- [ ] **Step 8: Buddy Album & Friendship System** — 24 species album, silhouettes, Bronze/Silver/Gold tiers, cosmetics, berry shop.
- [ ] **Step 9: Audio & Polish** — Procedural/synthesized or low-size sound effects, chimes, background music per world, haptics under 50ms.
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
