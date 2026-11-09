# ArrowBuddies Quality Assurance (QA) Checklist

This comprehensive QA test checklist verifies the release readiness of **ArrowBuddies** across device form factors, Android versions, accessibility modes, localization, and edge-case battery/interruption behaviors.

---

## 1. First-Time User Experience (FTUE) & Tutorial
- [ ] **Fresh Install Launch**: App opens with system splash screen, transitioning smoothly into the 1.2s bouncy logo intro.
- [ ] **Direct Tutorial Entry**: First launch directly opens Tutorial Level 1 (`w1-l01`) without showing the Home Screen.
- [ ] **Level 1 Guide**: An animated hand points to the solitary Bunny buddy with speech bubble: *"Tap me to help me escape!"*. Tapping releases the buddy.
- [ ] **Level 2 Blocking Lesson**: Two buddies (one blocking the other). Tapping the blocked buddy triggers a friendly cartoon bonk and bubble: *"Oops! Free your friend first!"*. No hearts are deducted during tutorial levels.
- [ ] **Level 3 Independence**: 4 buddies with no hand pointer. Completing Level 3 triggers the full celebration fanfare, awards 50 bonus berries, marks `tutorialCompleted = true`, and transitions into Sanctuary Home.
- [ ] **Subsequent Launches**: Reopening the app lands on Sanctuary Home with the player's rescued buddies roaming happily.

---

## 2. Core Mechanics Verification
- [ ] **Rocks**: Stone obstacles with tactile moss flecks; buddies cleanly bonk into them, lose 1 heart, and bounce back to origin.
- [ ] **Mud (World 2)**: Entering mud stops the buddy (`Stopped` outcome, 0 hearts lost). Muddy feet splatters appear. The next tap resumes movement in the same direction.
- [ ] **Turntable (World 3)**: Entering wooden disc rotates buddy 90° clockwise and continues moving. Infinite loops are caught deterministically as bonks.
- [ ] **Cage & Key (World 4)**: Caged buddy cannot move (shuffles bars, `Refused(CAGED)`). Rescuing key carrier of matching color unlocks all cages of that color (Gold, Silver, Ruby) with crisp latch audio.
- [ ] **Portals (World 5)**: Entering portal ring A teleports buddy to portal ring B facing the same direction. Dual rings flash cyan/purple/amber with distinct geometric symbols (Circle, Diamond, Star).
- [ ] **Sleepy Buddies (World 6)**: Buddy wearing cute nightcap sleeps; first tap wakes them up with gentle yawn (`Refused(SLEEPING)`, 0 hearts lost). Second tap rescues them.

---

## 3. Screen Form Factors & Responsiveness
- [ ] **Budget Compact Phones (360 x 640 dp)**: Grid cells scale dynamically; HUD elements and bottom boosters fit comfortably without overlapping.
- [ ] **Tall Aspect Ratios (20:9 & 21:9)**: Board remains vertically centered; living sky gradient and background hills fill display with edge-to-edge rendering.
- [ ] **Tablets & Large Foldables**: Board width is capped at `560 dp` to prevent awkward stretching; layout stays centered with plush toy-tray aesthetic.
- [ ] **Display Cutouts & Notches**: Safe drawing padding protects top HUD and bottom booster buttons from camera punch-holes.
- [ ] **Font Scaling (up to 1.3x)**: With system font size set to Large/Largest, text within pills, cards, and speech bubbles wraps cleanly without clipping.

---

## 4. Accessibility (A11y)
- [ ] **TalkBack Screen Reader**:
  - Focusing on a buddy announces species, facing direction, row, and column: *"Bunny, facing up, row 2, column 3"*.
  - Custom TalkBack action: *"Help escape"* activates the buddy tap.
  - HUD announcements: *"3 hearts remaining"*, *"4 of 8 buddies rescued"*, *"Pause game"*, *"Undo move"*.
- [ ] **Colorblind Accessibility**:
  - Direction badges feature high-contrast dark outlines and geometric arrows.
  - Cage and key pairs feature unique geometric identification symbols:
    - **Gold**: Circle
    - **Silver**: Diamond
    - **Ruby**: Triangle
  - Portals feature distinct symbols: Pair 1 = Circle, Pair 2 = Diamond, Pair 3 = Star.
- [ ] **Reduce Motion**:
  - In Settings, enabling *Reduce Motion* disables screen shake on bonk, suppresses victory confetti bursts, and accelerates transition times.

---

## 5. Audio Engine & Sound Focus
- [ ] **SFX Latency**: SoundPool preloading plays tap, swoosh, bonk, squelch, unlock, and chime instantaneously (< 20 ms).
- [ ] **Rising Escape Pitch**: Successive buddy rescues within the same level trigger rising musical chime pitches (`1.0x` up to `1.8x`).
- [ ] **SFX Ducking**: Background music gently lowers in volume (to 40%) during SFX playback and restores over 350 ms.
- [ ] **Audio Focus (Incoming Calls & Notifications)**:
  - Receiving a phone call pauses music immediately.
  - Incoming message notification sound ducks music temporarily and resumes full volume.
  - Background music app (e.g., Spotify): ArrowBuddies politely yields or ducks according to focus settings.
- [ ] **Lifecycle Audio**: Locking the screen or pressing the Home button pauses music and active SoundPool audio; reopening resumes playback seamlessly.
- [ ] **Settings Toggles**: Sound FX and Music toggles immediately mute their respective audio channels.

---

## 6. Interruptions, Battery & Persistence
- [ ] **Process Death & Recreation**: Using Android Developer Options *"Don't keep activities"*, navigating away and back preserves active level board state, hearts, and move history via `SavedStateHandle`.
- [ ] **Atomic Level Save**: Winning a level records stars, berries, unlocked species, and progress in a single Room database transaction. Force quitting the app immediately after win does not corrupt or duplicate rewards.
- [ ] **Low Battery / Battery Saver Mode**: Animation frame rates remain smooth without causing CPU or thermal throttling. Zero memory allocations inside draw loops.
- [ ] **Daily Reminder**: Daily local notification schedules at 6:00 PM via WorkManager without waking network radios. Tapping notification deep-links to Daily Rescue.
- [ ] **Offline Flight Mode**: App functions 100% identically with Wi-Fi and Cellular data turned off. Zero network requests made.

---

## 7. Language
- [ ] Every screen, dialog, tutorial bubble and notification is in English; no other language appears anywhere in the game.
