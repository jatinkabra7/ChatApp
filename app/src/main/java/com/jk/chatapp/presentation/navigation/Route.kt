package com.jk.chatapp.presentation.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed interface Route {

    @Serializable
    data object Home : Route

    @Serializable
    data object Auth : Route

    @Serializable
    data class Chat(val username : String, val phoneNumber : String) : Route

    @Serializable
    data object AddContact : Route

    @Serializable
    data class Image(val imageUrl : String) : Route
}