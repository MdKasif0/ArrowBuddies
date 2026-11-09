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
import com.kasif.arrowbuddies.core.audio.SoundManager
import com.kasif.arrowbuddies.core.designsystem.modifier.LocalHaptics
import com.kasif.arrowbuddies.core.designsystem.modifier.LocalSound
import com.kasif.arrowbuddies.core.designsystem.theme.ArrowBuddiesTheme
import com.kasif.arrowbuddies.core.designsystem.theme.ArrowBuddiesThemeTokens
import com.kasif.arrowbuddies.core.haptics.HapticsManager
import com.kasif.arrowbuddies.navigation.ArrowBuddiesNavHost
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var soundManager: SoundManager

    @Inject
    lateinit var hapticsManager: HapticsManager

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

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
}
