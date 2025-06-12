package com.jk.chatapp.domain

import android.net.Uri
import com.jk.chatapp.domain.models.MessageModel
import com.jk.chatapp.domain.models.ChatModel
import com.jk.chatapp.presentation.add_contact_screen.Contact
import kotlinx.coroutines.flow.Flow

interface ChatRepository {

    suspend fun insertUser(phoneNumber : String)
    suspend fun getChatList() : Flow<List<ChatModel>>
    suspend fun getMessages(receiverPhoneNumber: String) : Flow<List<MessageModel>>
    suspend fun sendMessage(messageModel: MessageModel, receiverPhoneNumber : String, receiverUsername : String)
    suspend fun searchUser(phoneNumber: String) : Flow<List<ChatModel>>
    suspend fun updateMessages(receiverPhoneNumber: String)
    suspend fun uploadImageAndGetDownloadUrl(uri : Uri) : String
    suspend fun getContacts(contacts : List<Contact>) : Flow<List<Contact>>
    suspend fun uploadAudioAndGetDownloadUrl(uri : Uri) : String
}