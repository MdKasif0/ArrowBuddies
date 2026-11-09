# ArrowBuddies — Progress & Architecture Log

## Roadmap (Steps 1-10)
- [x] **Step 1: Project Foundation** — Gradle setup, multi-module (:engine, :app), architecture skeleton, navigation routes, theme tokens, release/debug builds, tests.
- [ ] **Step 2: Pure Kotlin Engine** — Board representation, buddy movement, collisions, obstacle mechanics (rock, mud, turntable, cage/key, portal, sleepy), solver, test suite.
- [ ] **Step 3: Level Generator & Catalog** — 240 level generator with sawtooth difficulty, solvability validator, Daily Rescue seed generator, JSON/binary level serialisation.
- [ ] **Step 4: Vector Buddy Art & Design System** — 24 procedural vector buddies (no bitmaps), clay/soft-toy look, color palette tokens, typography (Baloo 2), spring animations, sound/haptic abstractions.
- [ ] **Step 5: Core Gameplay Screen** — Interactive board, tap dashing, path peek, bonk bounce, escape animations, HUD (hearts, pause, boosters), 60fps budget optimization.
- [ ] **Step 6: Sanctuary & World Progression** — Living home screen, 8 biomes, buddies wandering/interacting, decorations, total rescue counter.
- [ ] **Step 7: Buddy Album & Friendship System** — 24 species album, silhouettes, Bronze/Silver/Gold tiers, cosmetics, berry shop.
- [ ] **Step 8: Audio & Polish** — Procedural/synthesized or low-size sound effects, chimes, background music per world, haptics under 50ms.
- [ ] **Step 9: Daily Rescue & Rescue Run** — Date-seeded puzzle, endless mode after World 2, streak tracking, berry rewards.
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
- **Google Dagger Hilt**: 2.52
- **Google KSP**: 2.1.0-1.0.29
- **Kotlinx Coroutines**: 1.9.0
- **Kotlinx Serialization JSON**: 1.7.3
- **JUnit**: 4.13.2
- **Google Truth**: 1.4.4
- **CashApp Turbine**: 1.2.0
