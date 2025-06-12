package com.jk.chatapp.data

import android.net.Uri
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import com.google.firebase.storage.FirebaseStorage
import com.jk.chatapp.domain.ChatRepository
import com.jk.chatapp.domain.models.ChatModel
import com.jk.chatapp.domain.models.MessageModel
import com.jk.chatapp.domain.models.MessageType
import com.jk.chatapp.presentation.add_contact_screen.Contact
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.util.UUID

class ChatRepositoryImpl(
    private val auth : FirebaseAuth,
    private val dbRef : FirebaseFirestore,
    private val storage : FirebaseStorage
) : ChatRepository {

    private val phoneNumber = auth.currentUser?.phoneNumber.toString()
    private val usersCollection = dbRef.collection("users")
    private val chatCollection = usersCollection.document(phoneNumber).collection("chats")

    override suspend fun insertUser(currentPhoneNumber: String) {

        val snapshot = usersCollection.document(currentPhoneNumber).get().await()

        if (!snapshot.exists()) {
            val userMap = mapOf(
                "createdAt" to System.currentTimeMillis().toString()
            )

            usersCollection.document(currentPhoneNumber).set(userMap).await()
        }
    }

    override suspend fun getChatList(): Flow<List<ChatModel>> {

        return flow {

            try {
                usersCollection.document(phoneNumber).collection("chats").snapshots()
                    .collect { snapshot ->
                        val chatList = snapshot.toObjects(ChatModel::class.java).sortedByDescending { it.timestamp }
                        emit(chatList)
                    }
            } catch (e: Exception) {
                Log.d("chatlist", e.message.toString())
                emit(emptyList())
            }

        }
    }

    override suspend fun getMessages(receiverPhoneNumber: String): Flow<List<MessageModel>> {
        val messageCollection = chatCollection.document(receiverPhoneNumber).collection("messages")

        return flow {

            try {

                messageCollection.snapshots().collect { snapshot ->
                    val messages = snapshot.toObjects(MessageModel::class.java)
                        .sortedByDescending { it.timestamp }

                    emit(messages)
                }
            } catch (e: Exception) {
                emit(emptyList())
            }
        }
    }

    override suspend fun sendMessage(messageModel: MessageModel, receiverPhoneNumber: String, receiverUsername : String) {

        // sending from sender
        chatCollection.document(receiverPhoneNumber).collection("messages").add(
            messageModel
        ).await()

        // reflecting to receiver

        // creating a message on the receiver side
        usersCollection.document(receiverPhoneNumber).collection("chats").document(phoneNumber)
            .collection("messages")
            .add(
                MessageModel(
                    type = messageModel.type,
                    content = messageModel.content,
                    from = "them",
                    timestamp = messageModel.timestamp,
                    seen = false
                )
            ).await()

        // updating the unseen message count

        val unseenMessages = usersCollection.document(receiverPhoneNumber).collection("chats")
            .document(phoneNumber).get().await().get("unseenMessages").toString()

        val newUnseenMessages = if (unseenMessages == "null") 1 else unseenMessages.toInt() + 1

        val map1 = mapOf(
            "lastMessage" to messageModel.content,
            "phoneNumber" to receiverPhoneNumber,
            "timestamp" to messageModel.timestamp,
            "username" to receiverUsername,
            "unseenMessages" to "0"
        )

        val senderUsername = usersCollection.document(receiverPhoneNumber).collection("chats").document(phoneNumber).get().await().get("username")

        val map2 = mapOf(
            "lastMessage" to messageModel.content,
            "phoneNumber" to phoneNumber,
            "timestamp" to messageModel.timestamp,
            "username" to senderUsername,
            "unseenMessages" to "$newUnseenMessages"
        )

        chatCollection.document(receiverPhoneNumber).set(map1).await()
        usersCollection.document(receiverPhoneNumber).collection("chats").document(phoneNumber)
            .set(map2).await()

    }

    override suspend fun searchUser(searchPhoneNumber: String): Flow<List<ChatModel>> {

        return flow {
            try {

                val users = usersCollection.get().await()

                val userList = users
                    .map {
                        ChatModel(username = it.id, phoneNumber = it.id)
                    }
                    .filter {
                        it.phoneNumber.contains(searchPhoneNumber)
                    }

                Log.d("userlist", "${userList.size}")

                emit(userList)

            } catch (e: Exception) {
                emit(emptyList())
            }
        }
    }

    override suspend fun updateMessages(receiverPhoneNumber: String) {

        val myMessageCollection =
            chatCollection.document(receiverPhoneNumber).collection("messages")

        val receiverMessageCollection =
            usersCollection.document(receiverPhoneNumber).collection("chats").document(phoneNumber)
                .collection("messages")

        val myMessages =
            myMessageCollection
            .whereEqualTo("seen", false)
            .whereEqualTo("from","them")
            .get()
            .await()

        val receiverMessages =
            receiverMessageCollection
                .whereEqualTo("seen", false)
                .whereEqualTo("from", "me")
                .get()
                .await()

        val batch = dbRef.batch()

        for(i in myMessages) {
            batch.update(i.reference,"seen",true)
        }

        for(i in receiverMessages) {
            batch.update(i.reference,"seen",true)
        }

        batch.commit().await()

        // now lets update the unread messages

        chatCollection.document(receiverPhoneNumber).update("unseenMessages", "0")
    }

    override suspend fun uploadImageAndGetDownloadUrl(uri: Uri) : String {
        val storageRef = storage.reference

        val fileName = "images/${UUID.randomUUID()}.jpg"
        val imageRef = storageRef.child(fileName)

        imageRef.putFile(uri).await()

        return imageRef.downloadUrl.await().toString()
    }

    override suspend fun uploadAudioAndGetDownloadUrl(uri: Uri): String {

        val storageRef = storage.reference

        val fileName = "audio/${UUID.randomUUID()}.mp3"
        val audioRef = storageRef.child(fileName)

        audioRef.putFile(uri).await()

        return audioRef.downloadUrl.await().toString()
    }

    override suspend fun getContacts(contacts: List<Contact>): Flow<List<Contact>> {
        return flow {
            try {

                val users = usersCollection.get().await()

                val list = contacts.filter {contact ->
                    users.any {
                        it.id.endsWith(contact.phoneNumber)
                    }
                }

                emit(list)

            } catch (e : Exception) {
                emit(emptyList())
            }
        }
    }
}