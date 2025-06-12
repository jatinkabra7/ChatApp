package com.jk.chatapp.presentation.chat_screen

import android.net.Uri
import com.jk.chatapp.domain.models.MessageModel
import java.io.File

sealed interface ChatActions {

    data class OnTextFieldValueChange(val value : String) : ChatActions
    data class OnSendMessageClick(val messageModel : MessageModel, val receiverPhoneNumber : String, val receiverUsername : String) : ChatActions
    data class UploadImageAndSendMessage(val uri : Uri, val receiverPhoneNumber : String, val receiverUsername : String) : ChatActions
    data class OnRecordAudioClick(val outputFile : File) : ChatActions
    data object OnCancelRecording : ChatActions
    data class OnSendRecordingClick(val uri : Uri, val receiverPhoneNumber : String, val receiverUsername : String) : ChatActions
    data class OnPlayPauseAudioClick(val url : String, val messageId : String) : ChatActions
}