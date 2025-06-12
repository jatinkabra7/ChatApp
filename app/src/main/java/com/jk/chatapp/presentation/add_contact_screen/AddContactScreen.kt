package com.jk.chatapp.presentation.add_contact_screen

import android.Manifest
import android.content.Context
import android.provider.ContactsContract
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow.Companion.Ellipsis
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.google.firebase.auth.FirebaseAuth
import com.jk.chatapp.R
import com.jk.chatapp.domain.models.ChatModel
import com.jk.chatapp.presentation.add_contact_screen.components.AddContactTopBar
import com.jk.chatapp.presentation.home_screen.components.ChatCard

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun AddContactScreen(
    modifier: Modifier = Modifier,
    state: AddContactState,
    onActions: (AddContactActions) -> Unit,
    onChatClick : (username : String, phoneNumber : String) -> Unit,
    phoneNumber: String = FirebaseAuth.getInstance().currentUser?.phoneNumber!!
) {

    val contactPermissionState = rememberPermissionState(Manifest.permission.READ_CONTACTS)

    val context = LocalContext.current

    val hasFetchedContacts = remember { mutableStateOf(false) }

    LaunchedEffect(contactPermissionState.status.isGranted) {
        if (contactPermissionState.status.isGranted && !hasFetchedContacts.value) {
            val contacts = readContacts(context)
            onActions(AddContactActions.GetContacts(contacts))
            hasFetchedContacts.value = true
        }
    }

    Scaffold(
        topBar = {
            AddContactTopBar(
                value = state.textFieldValue,
                onTextFieldValueChange = {
                    onActions(AddContactActions.OnTextFieldValueChange(it))
                    onActions(AddContactActions.OnSearchClick(it))
                },
                onSearchClick = {
                    onActions(AddContactActions.OnSearchClick(it))
                }
            )
        }
    ) {innerpadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerpadding)
                .padding(10.dp)
        ) {

            if(!state.isLoading && state.textFieldValue.isEmpty()) {

                Text(
                    text = "Device Contacts",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.tertiary
                )

                Spacer(Modifier.height(20.dp))

                if(contactPermissionState.status.isGranted) {

                    LazyColumn(
                        state = rememberLazyListState()
                    ) {

                        items(state.contacts) {
                            val contactNumber = it.phoneNumber.replace(" ","")
                            if(!phoneNumber.endsWith(contactNumber)) {

                                ChatCardForSearchingUsers(
                                    onChatClick = {username, phoneNumber ->

                                        val updatedPhoneNumber =
                                            if(phoneNumber.startsWith("+91")) phoneNumber.replace(" ","")
                                            else "+91$phoneNumber".replace(" ","")

                                        onChatClick(username,updatedPhoneNumber)
                                    },
                                    chatModel = ChatModel(username = it.name, phoneNumber = it.phoneNumber),
                                    onImageClick = {}
                                )
                            }
                        }
                    }
                }
                else {

                    Button(
                        onClick = { contactPermissionState.launchPermissionRequest() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text(
                            text = "Grant Contact Permission"
                        )
                    }
                }

            }
            else if(state.isLoading) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
            else {
                LazyColumn(
                    state = rememberLazyListState()
                ) {
                    // show result

                    items(state.searchedUsers) {user ->
                        ChatCardForSearchingUsers(
                            onChatClick = {username, phoneNumber ->
                                onChatClick(username,phoneNumber)
                            },
                            chatModel = user,
                            onImageClick = {}
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChatCardForSearchingUsers(
    modifier: Modifier = Modifier,
    chatModel: ChatModel,
    onImageClick : () -> Unit,
    onChatClick : (String, String) -> Unit
) {

    val context = LocalContext.current

    val imageRequest = ImageRequest.Builder(context)
        .data("")
        .crossfade(true)
        .build()

    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        onClick = {
            onChatClick(chatModel.username, chatModel.phoneNumber)
        }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {

            AsyncImage(
                model = imageRequest,
                contentDescription = null,
                placeholder = painterResource(R.drawable.img),
                error = painterResource(R.drawable.img),
                modifier = Modifier
                    .clip(CircleShape)
                    .size(50.dp)
                    .clickable { onImageClick() }
            )

            Spacer(Modifier.width(10.dp))

            Text(
                text = chatModel.username,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = Ellipsis
            )
        }
    }
}


data class Contact(val name : String, val phoneNumber: String)

fun readContacts(context : Context) : List<Contact> {

    val contacts = mutableListOf<Contact>()

    val contentResolver = context.contentResolver

    val cursor = contentResolver.query(
        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
        null,
        null,
        null,
        null
    )

    cursor?.use {
        val usernameIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
        val phoneNumberIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)

        while (it.moveToNext()) {
            val username = it.getString(usernameIndex)
            val phoneNumber = it.getString(phoneNumberIndex).replace("\\s".toRegex(),"")
            contacts.add(Contact(username,phoneNumber))
        }
    }

    return contacts
}