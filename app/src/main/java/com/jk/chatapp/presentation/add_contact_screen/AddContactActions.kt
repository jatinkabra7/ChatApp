package com.jk.chatapp.presentation.add_contact_screen

sealed interface AddContactActions {
    data class OnSearchClick(val phoneNumber: String) : AddContactActions
    data class OnTextFieldValueChange(val value : String) : AddContactActions
    data class GetContacts(val contacts : List<Contact>) : AddContactActions
}