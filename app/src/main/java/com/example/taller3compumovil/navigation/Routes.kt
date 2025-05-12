package com.example.taller3compumovil.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed interface Routes {
    @Serializable
    object Home

    @Serializable
    object Register

    @Serializable
    object Authorized

    @Serializable
    object Login

    @Serializable
    object Profile
}