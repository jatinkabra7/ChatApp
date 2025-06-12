package com.jk.chatapp.presentation.home_screen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow.Companion.Ellipsis
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.jk.chatapp.R
import com.jk.chatapp.domain.models.ChatModel

@Composable
fun ChatCard(
    modifier: Modifier = Modifier,
    chatModel: ChatModel,
    onImageClick : () -> Unit,
    onChatClick : (String, String) -> Unit
) {

    val unreadMessagesAvailable = chatModel.unseenMessages.toInt() > 0

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

            Column(
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = chatModel.username ?: "",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1,
                    overflow = Ellipsis
                )

                Text(
                    text = getMessageTypeFromLastMessage(chatModel.lastMessage),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.secondary,
                    maxLines = 1,
                    overflow = Ellipsis,
                    fontWeight = if(unreadMessagesAvailable) FontWeight.Bold else FontWeight.Normal
                )
            }

            Spacer(Modifier.weight(1f))

            if(unreadMessagesAvailable) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Text(
                        text = if(chatModel.unseenMessages.toInt() > 9) "9+" else chatModel.unseenMessages,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(Modifier.width(10.dp))
        }
    }
}

fun getMessageTypeFromLastMessage(lastMessage: String): String {
    return when {
        lastMessage.contains("firebasestorage") -> "Media"
        else -> lastMessage
    }
}