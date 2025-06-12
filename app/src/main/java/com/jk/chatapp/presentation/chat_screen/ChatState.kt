package com.jk.chatapp.presentation.chat_screen

import com.jk.chatapp.domain.models.MessageModel

data class ChatState(
    val imageUrl : String = "",
    val username : String = "",
    val textFieldValue : String = "",
    val phoneNumber: String = "",
    val messages : List<MessageModel> = emptyList(),
    val isLoading : Boolean = false,
    val isAudioRecording : Boolean = false,
    val isAudioPlaying : Boolean = false,
    val currentAudioPlayingMessageId : String? = null,
    val progress : Long = 0,
    val duration : Long = 0
)
