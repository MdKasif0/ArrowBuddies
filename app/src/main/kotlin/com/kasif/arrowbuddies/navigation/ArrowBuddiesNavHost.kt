package com.kasif.arrowbuddies.navigation

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
import com.kasif.arrowbuddies.feature.daily.DailyRescueScreen
import com.kasif.arrowbuddies.feature.daily.DailyRescueViewModel
import com.kasif.arrowbuddies.feature.game.GameScreen
import com.kasif.arrowbuddies.feature.game.GameViewModel
import com.kasif.arrowbuddies.feature.home.HomeScreen
import com.kasif.arrowbuddies.feature.home.HomeViewModel
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
        modifier = modifier
    ) {
        composable<Route.Splash> {
            val viewModel: SplashViewModel = hiltViewModel()
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            SplashScreen(
                uiState = uiState,
                onNavigateToGame = { levelId ->
                    navController.navigate(Route.Game(levelId = levelId))
                },
                onNavigateToHome = {
                    navController.navigate(Route.Home)
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
                    navController.popBackStack()
                }
            )
        }

        composable<Route.Game> {
            val viewModel: GameViewModel = hiltViewModel()
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            GameScreen(
                uiState = uiState,
                onNavigateNextLevel = { nextLevelId ->
                    navController.navigate(Route.Game(levelId = nextLevelId))
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable<Route.Sanctuary> {
            val viewModel: SanctuaryViewModel = hiltViewModel()
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            SanctuaryScreen(
                uiState = uiState,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable<Route.Album> {
            val viewModel: AlbumViewModel = hiltViewModel()
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            AlbumScreen(
                uiState = uiState,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable<Route.DailyRescue> {
            val viewModel: DailyRescueViewModel = hiltViewModel()
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            DailyRescueScreen(
                uiState = uiState,
                onNavigateBack = {
                    navController.popBackStack()
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
                    navController.popBackStack()
                }
            )
        }
    }
}

