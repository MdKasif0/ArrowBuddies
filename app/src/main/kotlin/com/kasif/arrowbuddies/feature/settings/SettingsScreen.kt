package com.kasif.arrowbuddies.feature.settings

import com.kasif.arrowbuddies.core.notification.rememberNotificationPermissionRequester
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kasif.arrowbuddies.BuildConfig
import com.kasif.arrowbuddies.R
import com.kasif.arrowbuddies.core.designsystem.component.AnimatedSkyBackground
import com.kasif.arrowbuddies.core.designsystem.component.CandyButton
import com.kasif.arrowbuddies.core.designsystem.component.CandyButtonVariant
import com.kasif.arrowbuddies.core.designsystem.component.GameDialog
import com.kasif.arrowbuddies.core.designsystem.component.RoundIconButton
import com.kasif.arrowbuddies.core.designsystem.component.ToggleSwitch
import com.kasif.arrowbuddies.core.designsystem.component.ToyPanel
import com.kasif.arrowbuddies.core.designsystem.theme.ArrowBuddiesThemeTokens
import com.kasif.arrowbuddies.core.designsystem.theme.Ink
import com.kasif.arrowbuddies.core.designsystem.theme.WhitePure
import com.kasif.arrowbuddies.core.designsystem.theme.worldThemeFor

@Composable
fun SettingsScreen(
    uiState: SettingsUiState,
    onEvent: (SettingsUiEvent) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    onReplayTutorial: (() -> Unit)? = null,
    onNavigateToCatalog: (() -> Unit)? = null,
    onNavigateToGallery: (() -> Unit)? = null,
    onNavigateToLevelLab: (() -> Unit)? = null
) {
    // Reminders only work if the app may post notifications (runtime permission on Android 13+)
    val requestNotificationPermission = rememberNotificationPermissionRequester { granted ->
        if (granted) onEvent(SettingsUiEvent.ToggleDailyReminder(true))
    }
    val typography = ArrowBuddiesThemeTokens.typography
    val colors = ArrowBuddiesThemeTokens.colors
    val worldTheme = worldThemeFor(1) // Sunny Meadow theme for settings

    Box(modifier = modifier.fillMaxSize()) {
        AnimatedSkyBackground(
            worldTheme = worldTheme,
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                RoundIconButton(
                    icon = Icons.AutoMirrored.Filled.ArrowBack,
                    onClick = onNavigateBack,
                    contentDescription = stringResource(R.string.action_back),
                    size = 46.dp
                )

                Box(
                    modifier = Modifier
                        .shadow(elevation = 3.dp, shape = RoundedCornerShape(16.dp))
                        .background(WhitePure, RoundedCornerShape(16.dp))
                        .border(1.5.dp, Color(0xFFE2D6C0), RoundedCornerShape(16.dp))
                        .padding(horizontal = 20.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.screen_settings),
                        style = typography.title.copy(fontSize = 18.sp),
                        color = colors.textPrimary
                    )
                }

                // Balance spacer to keep title centered
                Box(modifier = Modifier.padding(23.dp))
            }

            // Scrollable Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Audio & Haptics Card
                ToyPanel(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 480.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.settings_audio_vibration),
                            style = typography.title.copy(fontSize = 17.sp, fontWeight = FontWeight.Bold),
                            color = Ink
                        )

                        // Sounds
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.settings_sound_effects),
                                style = typography.body,
                                color = Ink
                            )
                            ToggleSwitch(
                                checked = uiState.soundEnabled,
                                onCheckedChange = { onEvent(SettingsUiEvent.ToggleSound(it)) }
                            )
                        }

                        // Music
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.settings_music),
                                style = typography.body,
                                color = Ink
                            )
                            ToggleSwitch(
                                checked = uiState.musicEnabled,
                                onCheckedChange = { onEvent(SettingsUiEvent.ToggleMusic(it)) }
                            )
                        }

                        // Haptics
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.settings_vibration),
                                style = typography.body,
                                color = Ink
                            )
                            ToggleSwitch(
                                checked = uiState.hapticsEnabled,
                                onCheckedChange = { onEvent(SettingsUiEvent.ToggleHaptics(it)) }
                            )
                        }

                        // Reduce Motion
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.settings_reduce_motion),
                                style = typography.body,
                                color = Ink
                            )
                            ToggleSwitch(
                                checked = uiState.reduceMotion,
                                onCheckedChange = { onEvent(SettingsUiEvent.ToggleReduceMotion(it)) }
                            )
                        }

                        // Daily Reminder (6:00 PM)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                Text(
                                    text = stringResource(R.string.reminder_setting_title),
                                    style = typography.body,
                                    color = Ink
                                )
                                Text(
                                    text = stringResource(R.string.reminder_setting_subtitle),
                                    style = typography.caption.copy(fontSize = 11.sp),
                                    color = Ink.copy(alpha = 0.6f)
                                )
                            }
                            ToggleSwitch(
                                checked = uiState.dailyReminderEnabled,
                                onCheckedChange = { enabled ->
                                    if (enabled) {
                                        requestNotificationPermission()
                                    } else {
                                        onEvent(SettingsUiEvent.ToggleDailyReminder(false))
                                    }
                                }
                            )
                        }
                    }
                }

                // Game Options & Info Card
                ToyPanel(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 480.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.settings_general),
                            style = typography.title.copy(fontSize = 17.sp, fontWeight = FontWeight.Bold),
                            color = Ink
                        )

                        // Replay Tutorial
                        CandyButton(
                            text = stringResource(R.string.settings_replay_tutorial),
                            onClick = {
                                onEvent(SettingsUiEvent.ReplayTutorial)
                                onReplayTutorial?.invoke()
                            },
                            variant = CandyButtonVariant.Secondary,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CandyButton(
                                text = stringResource(R.string.settings_privacy_policy),
                                onClick = { onEvent(SettingsUiEvent.OpenPrivacyPolicy) },
                                variant = CandyButtonVariant.Ghost,
                                modifier = Modifier.weight(1f)
                            )
                            CandyButton(
                                text = stringResource(R.string.settings_credits),
                                onClick = { onEvent(SettingsUiEvent.OpenCredits) },
                                variant = CandyButtonVariant.Ghost,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Debug Developer Tools (if BuildConfig.DEBUG)
                if (BuildConfig.DEBUG) {
                    ToyPanel(
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 480.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "🛠️ Developer Tools",
                                style = typography.title.copy(fontSize = 17.sp, fontWeight = FontWeight.Bold),
                                color = Ink
                            )

                            if (onNavigateToGallery != null) {
                                CandyButton(
                                    text = "🐾 Buddy Characters Gallery",
                                    variant = CandyButtonVariant.Primary,
                                    onClick = onNavigateToGallery,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            if (onNavigateToCatalog != null) {
                                CandyButton(
                                    text = "🎨 Design System Catalog",
                                    variant = CandyButtonVariant.Secondary,
                                    onClick = onNavigateToCatalog,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            if (onNavigateToLevelLab != null) {
                                CandyButton(
                                    text = "🔬 Level Lab (Generator & Solver)",
                                    variant = CandyButtonVariant.Primary,
                                    onClick = onNavigateToLevelLab,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Version Footer
                Text(
                    text = "ArrowBuddies v${BuildConfig.VERSION_NAME} (Build ${BuildConfig.VERSION_CODE})",
                    style = typography.caption,
                    color = Ink.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // Privacy Policy Dialog
        if (uiState.showPrivacyPolicyDialog) {
            GameDialog(
                visible = true,
                onDismissRequest = { onEvent(SettingsUiEvent.DismissPrivacyPolicy) }
            ) {
                ToyPanel(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.settings_privacy_policy),
                            style = typography.title.copy(fontSize = 22.sp),
                            color = Ink
                        )
                        Text(
                            text = stringResource(R.string.settings_privacy_policy_body),
                            style = typography.body,
                            color = Ink.copy(alpha = 0.85f),
                            textAlign = TextAlign.Center
                        )
                        CandyButton(
                            text = stringResource(R.string.action_ok),
                            onClick = { onEvent(SettingsUiEvent.DismissPrivacyPolicy) },
                            variant = CandyButtonVariant.Primary,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        // Credits Dialog
        if (uiState.showCreditsDialog) {
            GameDialog(
                visible = true,
                onDismissRequest = { onEvent(SettingsUiEvent.DismissCredits) }
            ) {
                ToyPanel(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.settings_credits),
                            style = typography.title.copy(fontSize = 22.sp),
                            color = Ink
                        )
                        Text(
                            text = stringResource(R.string.settings_credits_body),
                            style = typography.body,
                            color = Ink.copy(alpha = 0.85f),
                            textAlign = TextAlign.Center
                        )
                        CandyButton(
                            text = stringResource(R.string.action_ok),
                            onClick = { onEvent(SettingsUiEvent.DismissCredits) },
                            variant = CandyButtonVariant.Primary,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}
