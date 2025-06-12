package com.jk.chatapp.domain.models

data class MessageModel(
    val type: MessageType = MessageType.TEXT,
    val content : String = "", // if the type is text, content will have the message, otherwise the url of the image/audio,etc
    val from : String = "",
    val timestamp : String = "",
    val seen : Boolean = false
)

enum class MessageType {
    IMAGE, TEXT, VIDEO, AUDIO, LOCATION
}

