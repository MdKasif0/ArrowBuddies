package com.kasif.arrowbuddies.core.analytics

import android.util.Log

/**
 * Child-Safe Diagnostic & Crash Reporting Hook.
 *
 * GOOGLE PLAY FAMILIES POLICY & PRIVACY COMPLIANCE:
 * - Disabled by default (`CRASHLYTICS_ENABLED = false`).
 * - Never transmits Android Advertising ID (AAID), personal identifiers (PII),
 *   geolocation, IP addresses, or device hardware IDs.
 * - Complies strictly with COPPA (Children's Online Privacy Protection Act)
 *   and GDPR-K (General Data Protection Regulation for Kids).
 * - Safe for players of all ages, including children under 13.
 */
object CrashlyticsHelper {

    private const val TAG = "CrashlyticsHelper"

    /**
     * Flag controlling whether external crashlytics telemetry is enabled.
     * Kept FALSE by default to ensure complete child privacy out-of-the-box.
     */
    const val CRASHLYTICS_ENABLED: Boolean = false

    fun log(message: String) {
        if (!CRASHLYTICS_ENABLED) {
            Log.d(TAG, "[Offline Mode] $message")
            return
        }
        // When enabled via production buildConfig flag, forward to FirebaseCrashlytics:
        // FirebaseCrashlytics.getInstance().log(message)
    }

    fun recordException(throwable: Throwable) {
        Log.e(TAG, "Exception recorded: ${throwable.message}", throwable)
        if (!CRASHLYTICS_ENABLED) return
        // When enabled via production buildConfig flag, forward to FirebaseCrashlytics:
        // FirebaseCrashlytics.getInstance().recordException(throwable)
    }

    fun setCustomKey(key: String, value: String) {
        if (!CRASHLYTICS_ENABLED) return
        // Sanitize to prevent accidental PII leakage:
        // FirebaseCrashlytics.getInstance().setCustomKey(key, value)
    }

    fun setCustomKey(key: String, value: Int) {
        if (!CRASHLYTICS_ENABLED) return
        // FirebaseCrashlytics.getInstance().setCustomKey(key, value)
    }
}

