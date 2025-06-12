package com.jk.chatapp.presentation.home_screen

import com.jk.chatapp.domain.models.ChatModel
import com.jk.chatapp.presentation.add_contact_screen.Contact

data class HomeState(
    val chatList : List<ChatModel> = emptyList()
)
