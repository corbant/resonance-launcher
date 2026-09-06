package io.github.corbant.resonancelauncher.ui.navigation

import kotlinx.serialization.Serializable

sealed interface Route {

    @Serializable
    data object Home : Route

    @Serializable
    data object Setup : Route

    @Serializable
    data class MediaDetails(val mediaId: Int, val mediaType: String) : Route
}