package com.jk.chatapp.domain.models

data class ChatModel(
    val username : String = "",
    val phoneNumber : String = "",
    val lastMessage : String = "",
    val timestamp : String = "",
    val unseenMessages : String = ""
)