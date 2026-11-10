# Google Play Data Safety & Privacy Disclosure

ArrowBuddies is committed to protecting the privacy of players and their families. ArrowBuddies complies with **Google Play's Families Policy**, **COPPA (Children’s Online Privacy Protection Act)**, and global child privacy regulations.

---

## 1. Data Collection & Usage Summary

| Category | Collected? | Shared? | Ephemeral? | Purpose |
| :--- | :--- | :--- | :--- | :--- |
| **Personal Info** (Name, Email, Phone, Address) | **No** | **No** | N/A | None. No accounts or registrations required. |
| **Financial Info** (Credit Card, Purchases) | **No** | **No** | N/A | No real-money transactions or credit card purchases. |
| **Location Data** (Precise or Approximate) | **No** | **No** | N/A | Location is neither requested nor collected. |
| **Photos & Media** | **No** | **No** | N/A | No storage permissions requested. |
| **Contacts & Messages** | **No** | **No** | N/A | None. |
| **Device or Other Identifiers** | **Yes** (via AdMob) | **No** | Yes | Handled by Google Mobile Ads SDK strictly for fraud prevention, ad delivery, and compliance. Tagged child-directed (`tagForChildDirectedTreatment = true`). |
| **App Activity / Gameplay State** | **No** | **No** | N/A | Stored 100% locally on the device (Room Database). |

---

## 2. Google Mobile Ads SDK & UMP Disclosure

ArrowBuddies integrates Google Mobile Ads (AdMob) and Google User Messaging Platform (UMP) solely for child-safe rewarded video ads:
- **Child-Directed Flag**: Every ad request enforces `tagForChildDirectedTreatment = TRUE` and `tagForUnderAgeOfConsent = TRUE`.
- **Content Rating**: Maximum content rating is clamped to `G` (General Audiences suitable for all ages).
- **Personalized Advertising**: Disabled. Under COPPA and Families policy, behavioral targeting, cross-app profiling, and persistent personalized identifiers are strictly disallowed.
- **Opt-in Only**: Ads never auto-play. Players must deliberately tap a button to watch a rewarded video (e.g. revive with +1 heart or double earned berries).
- **Tutorial Protection**: Levels 1 to 6 contain zero ad placements.

---

## 3. Security Practices

- **Encryption in Transit**: Any communication conducted by the Google Mobile Ads / UMP SDKs uses secure TLS / HTTPS encryption.
- **No Third-Party Sharing**: We do not sell, rent, or trade player data to data brokers or third parties.
- **Local Persistence**: All player profile stats, buddy rescue counts, sanctuary decor, and level completion stars reside exclusively in local storage on the player's device (`arrow_buddies.db` Room database).

---

## 4. Privacy Choices & Rights

- **In-App Privacy Options**: Players in the EEA/UK can access the UMP Consent management form at any time via **Settings > Ad Privacy Options**.
- **Data Deletion**: Since ArrowBuddies does not retain data on external servers, uninstalling the app or clearing app storage immediately deletes all local progress and history.

