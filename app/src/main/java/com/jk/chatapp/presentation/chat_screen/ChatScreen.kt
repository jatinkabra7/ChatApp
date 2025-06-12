package com.jk.chatapp.presentation.chat_screen

import android.Manifest
import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.ProgressIndicatorDefaults.drawStopIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.jk.chatapp.R
import com.jk.chatapp.domain.models.MessageModel
import com.jk.chatapp.domain.models.MessageType
import com.jk.chatapp.presentation.chat_screen.components.BottomTextField
import com.jk.chatapp.presentation.chat_screen.components.ChatScreenTopBar
import com.jk.chatapp.presentation.chat_screen.components.MessageSelectedTopBar
import com.jk.chatapp.presentation.home_screen.HomeViewModel
import org.koin.androidx.compose.koinViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class, ExperimentalPermissionsApi::class)
@Composable
fun ChatScreen(
    modifier: Modifier = Modifier,
    state: ChatState,
    onBackClick: () -> Unit,
    onActions: (ChatActions) -> Unit,
    onImageClick: (imageUrl: String) -> Unit,
    homeViewModel: HomeViewModel = koinViewModel<HomeViewModel>()
) {

    val context = LocalContext.current

    val lazyColumnState = rememberLazyListState()

    LaunchedEffect(state) {
        homeViewModel.updateMessages(state.phoneNumber)
    }

    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty()) lazyColumnState.animateScrollToItem(0)
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            onActions(
                ChatActions.UploadImageAndSendMessage(
                    uri = uri,
                    receiverPhoneNumber = state.phoneNumber,
                    receiverUsername = state.username
                )
            )
        }
    }

    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    var photoUri by remember { mutableStateOf<Uri?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { result ->

        if (result && photoUri != null) {
            onActions(
                ChatActions.UploadImageAndSendMessage(
                    uri = photoUri!!,
                    receiverUsername = state.username,
                    receiverPhoneNumber = state.phoneNumber
                )
            )
        }
    }

    var isMessageSelectedTopBarVisible by rememberSaveable {
        mutableStateOf(false)
    }

    var selectedMessageTimestamp by rememberSaveable {
        mutableStateOf("")
    }

    fun createImageFileUri(context: Context): Uri {
        val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val file = File.createTempFile("image_${System.currentTimeMillis()}", ".jpg", storageDir)
        return FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
    }

    val audioRecorderPermissionState = rememberPermissionState(Manifest.permission.RECORD_AUDIO)

    fun createAudioFile(): File {
        val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_AUDIOBOOKS)
        val file = File.createTempFile("audio_${System.currentTimeMillis()}", ".mp3", storageDir)
        return file
    }

    var audioFile by rememberSaveable {
        mutableStateOf<File?>(null)
    }

    Scaffold(
        topBar = {

            Crossfade(isMessageSelectedTopBarVisible) {
                if (it) {

                    MessageSelectedTopBar(
                        onDownload = {},
                        onCancel = {
                            isMessageSelectedTopBarVisible = false
                            selectedMessageTimestamp = ""
                        }
                    )

                } else {
                    ChatScreenTopBar(
                        imageUrl = state.imageUrl,
                        username = state.username,
                        onBackClick = { onBackClick() }
                    )
                }
            }
        },
        bottomBar = {
            BottomTextField(
                value = state.textFieldValue,
                onTextFieldValueChange = { onActions(ChatActions.OnTextFieldValueChange(it)) },
                onSendMessageClick = { messageModel ->
                    onActions(
                        ChatActions.OnSendMessageClick(
                            messageModel,
                            state.phoneNumber,
                            state.username
                        )
                    )
                },
                onImageIconClick = {
                    launcher.launch("image/*")
                },
                onCameraIconClick = {
                    if (cameraPermissionState.status.isGranted) {
                        val uri = createImageFileUri(context)
                        photoUri = uri
                        cameraLauncher.launch(uri)
                    } else {
                        cameraPermissionState.launchPermissionRequest()
                    }
                },
                isAudioRecording = state.isAudioRecording,
                onMicIconClick = {
                    if (audioRecorderPermissionState.status.isGranted) {

                        audioFile = createAudioFile()

                        audioFile?.let { onActions(ChatActions.OnRecordAudioClick(it)) }

                    } else {
                        audioRecorderPermissionState.launchPermissionRequest()
                    }
                },
                onCancelRecording = {
                    onActions(ChatActions.OnCancelRecording)
                    audioFile = null
                },
                onSendRecordingClick = {
                    audioFile?.let {
                        onActions(
                            ChatActions.OnSendRecordingClick(
                                it.toUri(),
                                state.phoneNumber,
                                state.username
                            )
                        )
                    }
                },
                modifier = Modifier
                    .imePadding()
            )
        }
    ) { innerpadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerpadding)

        ) {

            LazyColumn(
                reverseLayout = true,
                state = lazyColumnState,
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)

            ) {
                if (state.messages.isNotEmpty()) {

                    items(state.messages, key = { it.timestamp }) { message ->
                        MessageItem(
                            message = message,
                            onImageClick = { onImageClick(it) },
                            isSelected = message.timestamp == selectedMessageTimestamp,
                            onPlayPauseAudioClick = {
                                onActions(ChatActions.OnPlayPauseAudioClick(message.content, message.timestamp))
                            },
                            onLongPress = {
                                selectedMessageTimestamp = message.timestamp
                                isMessageSelectedTopBarVisible = true
                            },
                            currentAudioPlayingMessageId = state.currentAudioPlayingMessageId,
                            progress = state.progress,
                            duration = state.duration
                        )
                    }
                }
            }

            if (state.isLoading) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MessageItem(
    modifier: Modifier = Modifier,
    message: MessageModel,
    isSelected: Boolean,
    onImageClick: (imageUrl: String) -> Unit,
    onPlayPauseAudioClick: () -> Unit,
    currentAudioPlayingMessageId: String?,
    onLongPress: () -> Unit,
    progress : Long,
    duration : Long
) {

    val fromMe: Boolean = message.from == "me"

    val horizontalArrangement = if (fromMe) Arrangement.End else Arrangement.Start

    val cornerShape =
        if (fromMe) RoundedCornerShape(topEnd = 10.dp, bottomStart = 10.dp, topStart = 10.dp)
        else RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp, bottomEnd = 10.dp)

    val messageColor =
        if (fromMe) MaterialTheme.colorScheme.tertiaryContainer
        else MaterialTheme.colorScheme.secondaryContainer

    val textColor =
        if (fromMe) MaterialTheme.colorScheme.onTertiaryContainer
        else MaterialTheme.colorScheme.onSecondaryContainer

    val tickIconColor =
        if (message.seen) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.secondary

    val selectedMessageColor =
        if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(0.5f)
        else Color.Transparent

    val playPauseIcon =
        if (currentAudioPlayingMessageId == message.timestamp) R.drawable.baseline_pause_24
        else R.drawable.baseline_play_arrow_24

    val context = LocalContext.current

    Row(
        horizontalArrangement = horizontalArrangement,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp)
            .background(selectedMessageColor)
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = {}
                )
            }
    ) {

        Column(
            modifier = Modifier
                .widthIn(max = 250.dp)
                .clip(cornerShape)
                .background(messageColor)

        ) {

            Box(
                modifier = Modifier
                    .padding(5.dp)
                    .align(if (fromMe) Alignment.End else Alignment.Start)
            ) {

                if (message.type == MessageType.TEXT) {

                    Text(
                        text = message.content.trim(),
                        style = MaterialTheme.typography.bodyLarge,
                        color = textColor,
                        modifier = Modifier
                            .align(Alignment.Center)
                    )
                } else if (message.type == MessageType.IMAGE) {

                    val imageRequest = ImageRequest.Builder(context)
                        .data(message.content)
                        .crossfade(true)
                        .build()

                    AsyncImage(
                        model = imageRequest,
                        contentDescription = null,
                        error = painterResource(R.drawable.baseline_image_24),
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                onImageClick(message.content)
                            }
                    )
                } else if (message.type == MessageType.AUDIO) {

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {

                        IconButton(
                            onClick = { onPlayPauseAudioClick() }
                        ) {
                            Icon(
                                painter = painterResource(playPauseIcon),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(30.dp)
                            )
                        }

                        val progressFraction = if (duration > 0) {
                            progress.toFloat() / duration
                        } else 0f

                        LinearProgressIndicator(
                            progress = {
                                if(currentAudioPlayingMessageId == message.timestamp) progressFraction
                                else 0f
                            },
                            strokeCap = StrokeCap.Round,
                            modifier = Modifier
                                .fillMaxWidth(1f),
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(0.2f)
                        )
                    }

                }
            }

            if (fromMe) {

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(start = 10.dp, bottom = 5.dp)
                ) {

                    Text(
                        text = dateTimeFormatter(message.timestamp.toLong()),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )

                    if (message.seen) {

                        Icon(
                            painter = painterResource(R.drawable.double_tick_svgrepo_com),
                            contentDescription = null,
                            tint = tickIconColor,
                            modifier = Modifier
                                .padding(horizontal = 5.dp)
                                .size(24.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = tickIconColor,
                            modifier = Modifier
                                .padding(horizontal = 5.dp)
                                .size(20.dp)
                        )
                    }

                }

            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start,
                    modifier = Modifier
                        .align(Alignment.Start)
                        .padding(end = 10.dp, bottom = 5.dp)
                ) {
                    Text(
                        text = dateTimeFormatter(message.timestamp.toLong()),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(start = 5.dp)
                    )
                }
            }
        }
    }
}

fun dateTimeFormatter(timestamp: Long): String {
    val date = Date(timestamp)
    val formatter = SimpleDateFormat("hh:mm a", Locale.getDefault())
    return formatter.format(date)
}