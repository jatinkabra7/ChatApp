package com.jk.chatapp.presentation.home_screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import com.google.firebase.auth.FirebaseAuth
import com.jk.chatapp.presentation.home_screen.components.ChatCard
import com.jk.chatapp.presentation.home_screen.components.HomeScreenTopBar
import org.koin.androidx.compose.koinViewModel

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    state: HomeState,
    onChatClick: (String, String) -> Unit,
    onAddContactClick: () -> Unit
) {
    val lazyColumnState = rememberLazyListState()

    Scaffold(
        topBar = {
            HomeScreenTopBar()
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onAddContactClick() },
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                shape = CircleShape
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
        ) {

            LazyColumn(
                state = lazyColumnState
            ) {

                items(state.chatList, key = {it.timestamp}) { chatModel ->

                    ChatCard(
                        onChatClick = { username, phoneNumber ->
                            onChatClick(username, phoneNumber)
                        },
                        chatModel = chatModel,
                        onImageClick = {}
                    )
                }

            }
        }
    }
}