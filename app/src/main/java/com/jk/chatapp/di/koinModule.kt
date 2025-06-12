package com.jk.chatapp.di

import androidx.media3.exoplayer.ExoPlayer
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.jk.chatapp.data.ChatRepositoryImpl
import com.jk.chatapp.domain.ChatRepository
import com.jk.chatapp.domain.audio_player.AudioPlayer
import com.jk.chatapp.domain.audio_player.AudioPlayerImpl
import com.jk.chatapp.domain.audio_recorder.AudioRecorder
import com.jk.chatapp.domain.audio_recorder.AudioRecorderImpl
import com.jk.chatapp.presentation.add_contact_screen.AddContactViewModel
import com.jk.chatapp.presentation.auth_screen.AuthViewModel
import com.jk.chatapp.presentation.chat_screen.ChatViewModel
import com.jk.chatapp.presentation.home_screen.HomeViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val koinModule = module {

    single { FirebaseAuth.getInstance() }

    single { FirebaseFirestore.getInstance() }

    single { FirebaseStorage.getInstance() }

    single { ExoPlayer.Builder(get()).build() }

    singleOf(::ChatRepositoryImpl).bind<ChatRepository>()
    singleOf(::AudioRecorderImpl).bind<AudioRecorder>()
    singleOf(::AudioPlayerImpl).bind<AudioPlayer>()

    viewModelOf(::AuthViewModel)
    viewModelOf(::HomeViewModel)
    viewModelOf(::ChatViewModel)
    viewModelOf(::AddContactViewModel)
}