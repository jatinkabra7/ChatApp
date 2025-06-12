package com.jk.chatapp.presentation.chat_screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jk.chatapp.domain.ChatRepository
import com.jk.chatapp.domain.audio_player.AudioPlayer
import com.jk.chatapp.domain.audio_recorder.AudioRecorder
import com.jk.chatapp.domain.models.MessageModel
import com.jk.chatapp.domain.models.MessageType
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChatViewModel(
    private val chatRepository: ChatRepository,
    private val recorder: AudioRecorder,
    private val audioPlayer: AudioPlayer
) : ViewModel() {

    private val _state = MutableStateFlow(ChatState())
    val state = _state.asStateFlow()

    fun initialise(username: String, phoneNumber: String) {
        viewModelScope.launch {
            _state.update { it.copy(username = username, phoneNumber = phoneNumber) }
        }
    }

    fun getMessages(receiverPhoneNumber: String) {
        viewModelScope.launch {
            chatRepository.getMessages(receiverPhoneNumber).collect { messages ->
                _state.update { it.copy(messages = messages) }
            }
        }
    }


    fun onAction(action: ChatActions) {
        viewModelScope.launch {

            when (action) {
                is ChatActions.OnTextFieldValueChange -> {
                    _state.update { it.copy(textFieldValue = action.value) }
                }

                is ChatActions.OnSendMessageClick -> {
                    _state.update { it.copy(textFieldValue = "") }

                    withContext(NonCancellable) {
                        chatRepository.sendMessage(
                            action.messageModel,
                            action.receiverPhoneNumber,
                            action.receiverUsername
                        )
                    }
                }

                is ChatActions.UploadImageAndSendMessage -> {

                    withContext(NonCancellable) {

                        _state.update { it.copy(isLoading = true) }

                        val downloadUrl = chatRepository.uploadImageAndGetDownloadUrl(action.uri)

                        chatRepository.sendMessage(
                            messageModel = MessageModel(
                                type = MessageType.IMAGE,
                                content = downloadUrl,
                                from = "me",
                                timestamp = System.currentTimeMillis().toString()
                            ),
                            receiverPhoneNumber = action.receiverPhoneNumber,
                            receiverUsername = action.receiverUsername
                        )

                        _state.update { it.copy(isLoading = false) }
                    }
                }

                is ChatActions.OnRecordAudioClick -> {
                    _state.update { it.copy(isAudioRecording = true) }

                    recorder.startAudioRecording(action.outputFile)
                }

                ChatActions.OnCancelRecording -> {
                    _state.update { it.copy(isAudioRecording = false) }
                    recorder.stopAudioRecording()
                }

                is ChatActions.OnSendRecordingClick -> {
                    withContext(NonCancellable) {

                        _state.update { it.copy(isAudioRecording = false, isLoading = true) }

                        recorder.stopAudioRecording()

                        val audioUrl = chatRepository.uploadAudioAndGetDownloadUrl(action.uri)

                        chatRepository.sendMessage(
                            MessageModel(
                                type = MessageType.AUDIO,
                                content = audioUrl,
                                from = "me",
                                timestamp = System.currentTimeMillis().toString()
                            ),
                            receiverPhoneNumber = action.receiverPhoneNumber,
                            receiverUsername = action.receiverUsername
                        )

                        _state.update { it.copy(isLoading = false) }
                    }
                }

                is ChatActions.OnPlayPauseAudioClick -> {
                    withContext(NonCancellable) {

                        if (audioPlayer.isPlaying()) {

                            _state.update {
                                it.copy(
                                    isAudioPlaying = false,
                                    currentAudioPlayingMessageId = null
                                )
                            }
                            audioPlayer.pause()

                        } else {

                            _state.update {
                                it.copy(
                                    isAudioPlaying = true,
                                    currentAudioPlayingMessageId = action.messageId,
                                    duration = audioPlayer.duration()
                                )
                            }

                            audioPlayer.play(action.url)

                            while (state.value.isAudioPlaying) {
                                _state.update {
                                    it.copy(
                                        progress = audioPlayer.progress(),
                                        duration = audioPlayer.duration()
                                    )
                                }

                                delay(1000)

                                audioPlayer.setOnCompleteListener {
                                    _state.update {
                                        it.copy(
                                            isAudioPlaying = false,
                                            currentAudioPlayingMessageId = null,
                                            progress = 0L
                                        )
                                    }
                                }
                            }

                        }
                    }
                }
            }
        }
    }
}