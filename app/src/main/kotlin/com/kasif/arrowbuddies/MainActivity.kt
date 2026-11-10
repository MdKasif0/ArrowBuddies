package com.kasif.arrowbuddies

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.kasif.arrowbuddies.core.ads.AdsManager
import com.kasif.arrowbuddies.core.audio.SoundManager
import com.kasif.arrowbuddies.core.designsystem.modifier.LocalHaptics
import com.kasif.arrowbuddies.core.designsystem.modifier.LocalSound
import com.kasif.arrowbuddies.core.designsystem.theme.ArrowBuddiesTheme
import com.kasif.arrowbuddies.core.designsystem.theme.ArrowBuddiesThemeTokens
import com.kasif.arrowbuddies.core.haptics.HapticsManager
import com.kasif.arrowbuddies.data.repository.GameRepository
import com.kasif.arrowbuddies.navigation.ArrowBuddiesNavHost
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var soundManager: SoundManager

    @Inject
    lateinit var hapticsManager: HapticsManager

    @Inject
    lateinit var gameRepository: GameRepository

    @Inject
    lateinit var adsManager: AdsManager

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize child-safe rewarded ads (UMP consent + background MobileAds)
        adsManager.initialize(this)

        // Apply saved audio/haptic preferences app-wide (not only once Settings is opened),
        // and keep them in sync with toggles from the pause sheet or Settings.
        lifecycleScope.launch {
            launch { gameRepository.soundEnabled.collect { soundManager.setSoundEnabled(it) } }
            launch { gameRepository.musicEnabled.collect { soundManager.setMusicEnabled(it) } }
            launch { gameRepository.hapticsEnabled.collect { hapticsManager.setHapticsEnabled(it) } }
        }

        setContent {
            CompositionLocalProvider(
                LocalSound provides soundManager,
                LocalHaptics provides hapticsManager
            ) {
                ArrowBuddiesTheme {
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .safeDrawingPadding(),
                        color = ArrowBuddiesThemeTokens.colors.background
                    ) {
                        ArrowBuddiesNavHost()
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        soundManager.resumeAll()
    }

    override fun onPause() {
        super.onPause()
        soundManager.pauseAll()
    }

    override fun onDestroy() {
        super.onDestroy()
        // SoundManager is an app-wide singleton: never release it here, or recreated activities
        // (rotation, dark mode, relaunch) would be left silent.
        if (isFinishing) soundManager.stopMusic()
    }
}
