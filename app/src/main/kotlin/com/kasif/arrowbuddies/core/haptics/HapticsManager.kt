package com.kasif.arrowbuddies.core.haptics

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

interface HapticsManager {
    fun tick()
    fun bonk()
    fun rescueSuccess()
    fun setHapticsEnabled(enabled: Boolean)
}

@Singleton
class DefaultHapticsManager @Inject constructor(
    @ApplicationContext private val context: Context
) : HapticsManager {

    private var hapticsEnabled = true

    private val vibrator: Vibrator? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    override fun tick() {
        if (!hapticsEnabled) return
        vibrator?.let { v ->
            if (v.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    v.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
                } else {
                    @Suppress("DEPRECATION")
                    v.vibrate(15)
                }
            }
        }
    }

    override fun bonk() {
        if (!hapticsEnabled) return
        vibrator?.let { v ->
            if (v.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    v.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK))
                } else {
                    @Suppress("DEPRECATION")
                    v.vibrate(40)
                }
            }
        }
    }

    override fun rescueSuccess() {
        if (!hapticsEnabled) return
        vibrator?.let { v ->
            if (v.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    v.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 20, 50, 25), -1))
                } else {
                    @Suppress("DEPRECATION")
                    v.vibrate(longArrayOf(0, 20, 50, 25), -1)
                }
            }
        }
    }

    override fun setHapticsEnabled(enabled: Boolean) {
        this.hapticsEnabled = enabled
    }
}

