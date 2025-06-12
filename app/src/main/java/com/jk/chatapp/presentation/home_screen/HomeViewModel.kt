package com.jk.chatapp.presentation.home_screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jk.chatapp.domain.ChatRepository
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.internal.wait

class HomeViewModel(
    private val chatRepository: ChatRepository
) : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state = _state.asStateFlow()

    fun getChatList() {
        viewModelScope.launch {
            chatRepository.getChatList().collect {chatList ->
                _state.update { it.copy(chatList = chatList) }
            }
        }
    }

    fun updateMessages(receiverPhoneNumber : String) {
        viewModelScope.launch {
            withContext(NonCancellable) {

                chatRepository.updateMessages(receiverPhoneNumber)
            }
        }
    }
}