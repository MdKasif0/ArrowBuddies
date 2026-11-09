package com.kasif.arrowbuddies.navigation

import kotlinx.serialization.Serializable

sealed interface Route {
    @Serializable
    data object Splash : Route

    @Serializable
    data object Home : Route

    @Serializable
    data object WorldMap : Route

    @Serializable
    data class Game(val levelId: String = "1") : Route

    @Serializable
    data object Sanctuary : Route

    @Serializable
    data object Album : Route

    @Serializable
    data object DailyRescue : Route

    @Serializable
    data object Settings : Route
}

