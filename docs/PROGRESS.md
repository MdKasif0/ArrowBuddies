# ArrowBuddies — Progress & Architecture Log

## Roadmap (Steps 1-10)
- [x] **Step 1: Project Foundation** — Gradle setup, multi-module (:engine, :app), architecture skeleton, navigation routes, theme tokens, release/debug builds, tests.
- [x] **Step 2: Premium Design System** — Soft-toy / clay tactile UI tokens, 3D candy buttons, Baloo 2 typography, 8 WorldTheme biomes, spring physics, squishy components, particle burst system, Design Catalog showcase screen.
- [ ] **Step 3: Pure Kotlin Game Engine & Solver** — Board representation, buddy movement, collisions, obstacle mechanics (rock, mud, turntable, cage/key, portal, sleepy), solver, test suite.
- [ ] **Step 4: Procedural Vector Buddies** — 24 procedural vector buddies (no bitmaps), clay look, eyes with dual highlights, blush cheeks, accessory slots.
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

## Visual Verification & Screen Captures

Screenshots captured from live Android emulator execution on API 35:
- `docs/screens/design_catalog_themes.png`: World Theme switcher (all 8 biomes), Palette Swatches with lip & highlight tokens, Baloo 2 typography showcase.
- `docs/screens/design_catalog_top.png`: Baloo 2 Latin and Hindi Devanagari typography, 3D Candy Button variants (Mango, Sky, Coral, Mint, Grape).
- `docs/screens/design_catalog_1.png`: Round Icon Buttons with badge counters, Rolling Odometer Counter Pills, Hearts Row with interactive heart crack/loss animation.
- `docs/screens/design_catalog_2.png`: Star Row with sequential chime fills, Jelly Progress Bar with squishy wobble, Speech Bubble with pointer tail, Chunky 3D Toggles.
- `docs/screens/design_catalog_3.png`: Particle Burst triggers (Confetti & Sparkle) and Modal Dialog triggers.
- `docs/screens/design_catalog_dialog.png`: 3D Game Dialog with API 31+ blurred backdrop, star celebration, and 3D action button.

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
