package com.jk.chatapp.presentation.navigation

import android.app.Activity
import android.util.Log
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.jk.chatapp.presentation.add_contact_screen.AddContactScreen
import com.jk.chatapp.presentation.add_contact_screen.AddContactViewModel
import com.jk.chatapp.presentation.auth_screen.AuthScreen
import com.jk.chatapp.presentation.auth_screen.AuthViewModel
import com.jk.chatapp.presentation.chat_screen.ChatScreen
import com.jk.chatapp.presentation.chat_screen.ChatViewModel
import com.jk.chatapp.presentation.home_screen.HomeScreen
import com.jk.chatapp.presentation.home_screen.HomeState
import com.jk.chatapp.presentation.home_screen.HomeViewModel
import com.jk.chatapp.presentation.image_screen.ImageScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun NavGraph(
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel = koinViewModel<AuthViewModel>(),
    addContactViewModel: AddContactViewModel = koinViewModel<AddContactViewModel>(),
    homeViewModel : HomeViewModel = koinViewModel<HomeViewModel>(),
    chatViewModel: ChatViewModel = koinViewModel<ChatViewModel>()
) {
    val navController = rememberNavController()

    val startDestination = if (FirebaseAuth.getInstance().currentUser != null) Route.Home else Route.Auth

    val scope = rememberCoroutineScope()

    val view = LocalView.current

    val window = (view.context as Activity).window

    window.statusBarColor = MaterialTheme.colorScheme.surfaceContainer.toArgb()
    window.navigationBarColor = MaterialTheme.colorScheme.surfaceContainer.toArgb()

    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        modifier = Modifier
            .windowInsetsPadding(WindowInsets.systemBars)
    ) {

        composable<Route.Auth> {

            val authState = authViewModel.state.collectAsStateWithLifecycle().value

            val authEvent = authViewModel.event

            AuthScreen(
                state = authState,
                event = authEvent,
                onAction = authViewModel::onAction,
                navigateToHome = {
                    scope.launch {
                        // Wait until user is logged in
                        while (FirebaseAuth.getInstance().currentUser?.uid == null) {
                            delay(100)
                        }

                        homeViewModel.getChatList()

                        navController.navigate(Route.Home) {
                            popUpTo(Route.Auth) { inclusive = true }
                        }
                    }
                }

            )
        }

        composable<Route.Home> {

            val state = homeViewModel.state.collectAsStateWithLifecycle().value

            val homeState = HomeState(
                chatList = state.chatList
            )

            HomeScreen(
                state = homeState,
                onChatClick = {username, phoneNumber ->
                    homeViewModel.updateMessages(receiverPhoneNumber = phoneNumber)
                    chatViewModel.getMessages(phoneNumber)
                    navController.navigate(Route.Chat(username, phoneNumber))
                },
                onAddContactClick = {
                    navController.navigate(Route.AddContact)
                }
            )
        }
        composable<Route.Chat> {

            val username = it.arguments?.getString("username").toString()
            val phoneNumber = it.arguments?.getString("phoneNumber").toString()

            chatViewModel.initialise(username, phoneNumber)

            LaunchedEffect(chatViewModel) {
                chatViewModel.getMessages(phoneNumber)
            }


            val state = chatViewModel.state.collectAsStateWithLifecycle().value

            ChatScreen(
                state = state,
                onBackClick = {
                    navController.popBackStack()
                },
                onActions = chatViewModel::onAction,
                onImageClick = {imageUrl ->
                    navController.navigate(Route.Image(imageUrl = imageUrl))
                }
            )
        }

        composable<Route.AddContact> {

            val state = addContactViewModel.state.collectAsStateWithLifecycle().value

            AddContactScreen(
                state = state,
                onActions = addContactViewModel::onAction,
                onChatClick = { username, phoneNumber ->
                    chatViewModel.getMessages(phoneNumber)
                    homeViewModel.updateMessages(receiverPhoneNumber = phoneNumber)
                    navController.navigate(Route.Chat(username,phoneNumber)) {
                        popUpTo(Route.AddContact) {inclusive = true}
                    }
                }
            )
        }

        composable<Route.Image> {

            val imageUrl = it.arguments?.getString("imageUrl").toString()

            ImageScreen(
                imageUrl = imageUrl
            )
        }
    }
}