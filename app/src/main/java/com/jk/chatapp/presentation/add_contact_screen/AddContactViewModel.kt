package com.jk.chatapp.presentation.add_contact_screen

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jk.chatapp.domain.ChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AddContactViewModel(
    private val chatRepository: ChatRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AddContactState())
    val state = _state.asStateFlow()

    fun onAction(action : AddContactActions) {
        viewModelScope.launch {

            when(action) {
                is AddContactActions.OnSearchClick -> {
                    _state.update { it.copy(isLoading = true) }

                    chatRepository.searchUser(action.phoneNumber).collect {searchedUsers ->
                        _state.update { it.copy(searchedUsers = searchedUsers, isLoading = false) }
                    }
                }
                is AddContactActions.OnTextFieldValueChange -> {
                    _state.update { it.copy(textFieldValue = action.value) }
                }

                is AddContactActions.GetContacts -> {
                    _state.update {it.copy(isLoading = true)}

                    chatRepository.getContacts(action.contacts).collect {contacts ->
                        _state.update { it.copy(contacts = contacts, isLoading = false) }
                    }
                }
            }
        }
    }
}