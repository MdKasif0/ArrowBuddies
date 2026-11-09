package com.kasif.arrowbuddies.navigation

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.kasif.arrowbuddies.feature.album.AlbumScreen
import com.kasif.arrowbuddies.feature.album.AlbumViewModel
import com.kasif.arrowbuddies.feature.catalog.DesignCatalogScreen
import com.kasif.arrowbuddies.feature.daily.DailyRescueScreen
import com.kasif.arrowbuddies.feature.daily.DailyRescueViewModel
import com.kasif.arrowbuddies.feature.game.GameScreen
import com.kasif.arrowbuddies.feature.game.GameViewModel
import com.kasif.arrowbuddies.feature.home.HomeScreen
import com.kasif.arrowbuddies.feature.home.HomeViewModel
import com.kasif.arrowbuddies.feature.levellab.LevelLabScreen
import com.kasif.arrowbuddies.feature.levellab.LevelLabViewModel
import com.kasif.arrowbuddies.feature.map.WorldMapScreen
import com.kasif.arrowbuddies.feature.map.WorldMapViewModel
import com.kasif.arrowbuddies.feature.sanctuary.SanctuaryScreen
import com.kasif.arrowbuddies.feature.sanctuary.SanctuaryViewModel
import com.kasif.arrowbuddies.feature.settings.SettingsScreen
import com.kasif.arrowbuddies.feature.settings.SettingsViewModel
import com.kasif.arrowbuddies.feature.splash.SplashScreen
import com.kasif.arrowbuddies.feature.splash.SplashViewModel

@Composable
fun ArrowBuddiesNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Route.Splash,
        enterTransition = { fadeIn(animationSpec = tween(220)) },
        exitTransition = { fadeOut(animationSpec = tween(180)) },
        popEnterTransition = { fadeIn(animationSpec = tween(220)) },
        popExitTransition = { fadeOut(animationSpec = tween(180)) },
        modifier = modifier
    ) {
        composable<Route.Splash> {
            val viewModel: SplashViewModel = hiltViewModel()
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            SplashScreen(
                uiState = uiState,
                onNavigateToGame = { levelId ->
                    navController.navigate(Route.Game(levelId = levelId)) {
                        popUpTo(Route.Splash) { inclusive = true }
                    }
                },
                onNavigateToHome = {
                    navController.navigate(Route.Home) {
                        popUpTo(Route.Splash) { inclusive = true }
                    }
                }
            )
        }

        composable<Route.Home> {
            val viewModel: HomeViewModel = hiltViewModel()
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            HomeScreen(
                uiState = uiState,
                onNavigateToGame = { levelId ->
                    navController.navigate(Route.Game(levelId = levelId))
                },
                onNavigateToMap = {
                    navController.navigate(Route.WorldMap)
                },
                onNavigateToSanctuary = {
                    navController.navigate(Route.Sanctuary)
                },
                onNavigateToAlbum = {
                    navController.navigate(Route.Album)
                },
                onNavigateToDaily = {
                    navController.navigate(Route.DailyRescue)
                },
                onNavigateToSettings = {
                    navController.navigate(Route.Settings)
                }
            )
        }

        composable<Route.WorldMap> {
            val viewModel: WorldMapViewModel = hiltViewModel()
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            WorldMapScreen(
                uiState = uiState,
                onNavigateToGame = { levelId ->
                    navController.navigate(Route.Game(levelId = levelId))
                },
                onNavigateBack = {
                    if (!navController.popBackStack()) {
                        navController.navigate(Route.Home) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }
            )
        }

        composable<Route.Game>(
            enterTransition = {
                scaleIn(
                    initialScale = 0.85f,
                    animationSpec = spring(dampingRatio = 0.75f, stiffness = Spring.StiffnessMedium)
                ) + fadeIn(animationSpec = tween(220))
            },
            exitTransition = {
                scaleOut(
                    targetScale = 0.90f,
                    animationSpec = tween(180)
                ) + fadeOut(animationSpec = tween(180))
            },
            popEnterTransition = {
                scaleIn(
                    initialScale = 0.90f,
                    animationSpec = tween(200)
                ) + fadeIn(animationSpec = tween(200))
            },
            popExitTransition = {
                scaleOut(
                    targetScale = 0.85f,
                    animationSpec = tween(180)
                ) + fadeOut(animationSpec = tween(180))
            }
        ) {
            val viewModel: GameViewModel = hiltViewModel()
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            GameScreen(
                uiState = uiState,
                effects = viewModel.effects,
                onEvent = viewModel::onEvent,
                onNavigateNextLevel = { nextLevelId ->
                    viewModel.onEvent(com.kasif.arrowbuddies.feature.game.GameUiEvent.LoadLevel(nextLevelId))
                },
                onNavigateBack = {
                    if (!navController.popBackStack()) {
                        navController.navigate(Route.Home) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }
            )
        }

        composable<Route.Sanctuary> {
            val viewModel: SanctuaryViewModel = hiltViewModel()
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            SanctuaryScreen(
                uiState = uiState,
                onEvent = viewModel::onEvent,
                onNavigateBack = {
                    if (!navController.popBackStack()) {
                        navController.navigate(Route.Home) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }
            )
        }

        composable<Route.Album> {
            val viewModel: AlbumViewModel = hiltViewModel()
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            AlbumScreen(
                uiState = uiState,
                onEvent = viewModel::onEvent,
                onNavigateBack = {
                    if (!navController.popBackStack()) {
                        navController.navigate(Route.Home) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }
            )
        }

        composable<Route.DailyRescue> {
            val viewModel: DailyRescueViewModel = hiltViewModel()
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            DailyRescueScreen(
                uiState = uiState,
                onEvent = viewModel::onEvent,
                onPlayDailyLevel = { levelId ->
                    navController.navigate(Route.Game(levelId = levelId))
                },
                onNavigateBack = {
                    if (!navController.popBackStack()) {
                        navController.navigate(Route.Home) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }
            )
        }

        composable<Route.Settings> {
            val viewModel: SettingsViewModel = hiltViewModel()
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            SettingsScreen(
                uiState = uiState,
                onEvent = viewModel::onEvent,
                onNavigateBack = {
                    if (!navController.popBackStack()) {
                        navController.navigate(Route.Home) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                },
                onReplayTutorial = {
                    navController.navigate(Route.Game(levelId = "w1-l01")) {
                        popUpTo(Route.Home) { inclusive = false }
                    }
                },
                onNavigateToCatalog = {
                    navController.navigate(Route.DesignCatalog)
                },
                onNavigateToGallery = {
                    navController.navigate(Route.BuddyGallery)
                },
                onNavigateToLevelLab = {
                    navController.navigate(Route.LevelLab)
                }
            )
        }

        composable<Route.DesignCatalog> {
            DesignCatalogScreen(
                onNavigateBack = {
                    if (!navController.popBackStack()) {
                        navController.navigate(Route.Home) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }
            )
        }

        composable<Route.BuddyGallery> {
            com.kasif.arrowbuddies.feature.gallery.BuddyGalleryScreen(
                onNavigateBack = {
                    if (!navController.popBackStack()) {
                        navController.navigate(Route.Home) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }
            )
        }

        composable<Route.LevelLab> {
            val viewModel: LevelLabViewModel = hiltViewModel()
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            LevelLabScreen(
                uiState = uiState,
                onEvent = viewModel::onEvent,
                onPlayLevel = { levelId ->
                    val customId = viewModel.prepareForPlay() ?: levelId
                    navController.navigate(Route.Game(levelId = customId))
                },
                onNavigateBack = {
                    if (!navController.popBackStack()) {
                        navController.navigate(Route.Home) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }
            )
        }
    }
}
