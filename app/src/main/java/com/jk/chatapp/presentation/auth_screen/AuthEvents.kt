package com.jk.chatapp.presentation.auth_screen

sealed interface AuthEvents {
    data class ShowToast(val message : String) : AuthEvents
}