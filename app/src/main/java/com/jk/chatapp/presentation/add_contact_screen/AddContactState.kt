package com.jk.chatapp.presentation.add_contact_screen

import com.jk.chatapp.domain.models.ChatModel

data class AddContactState(
    val textFieldValue : String = "",
    val searchedUsers : List<ChatModel> = emptyList(),
    val contacts : List<Contact> = emptyList(),
    val isLoading : Boolean = false
)
