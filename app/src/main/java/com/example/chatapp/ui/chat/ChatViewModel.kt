package com.example.chatapp.ui.chat

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatapp.model.ChatUser
import com.example.chatapp.model.Message
import com.example.chatapp.repository.InternalChatRepository
import com.example.chatapp.service.WebSocketListener
import com.example.chatapp.utils.ChatDataObject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: InternalChatRepository
) : ViewModel() {

    private val _databaseUpdated:MutableLiveData<String> = MutableLiveData("sdsd")
    val databaseUpdated = _databaseUpdated

    private var _currentStatus:MutableLiveData<String> = MutableLiveData()
    var currentStatus = _currentStatus
    var retryCount = 0

    var _currentUser:MutableLiveData<String> = MutableLiveData(ChatDataObject.user)
    var currentUser = _currentUser

    lateinit var webSocketListener: WebSocketListener
    val okHttpClient = OkHttpClient()
    var webSocket: WebSocket? = null
    val _socketStatus = MutableLiveData(false)
    val socketStatus: LiveData<Boolean> = _socketStatus

    private val _messages = MutableLiveData<Pair<Boolean, String>>()
    val messages: LiveData<Pair<Boolean, String>> = _messages

    val chatUsers: Flow<List<ChatUser>> = chatRepository.getChatUsers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addMessage(message: Pair<Boolean, String>) = viewModelScope.launch(Dispatchers.Main) {
        if (_socketStatus.value == true) {
            _messages.value = message
        }
    }

    fun updateDatabase(){
        _databaseUpdated.postValue(System.currentTimeMillis().toString())
    }

    fun updateStatus(status:String){
        _currentStatus.postValue(status)
    }

    fun updateCurrentUser(user:String){
        _currentUser.postValue(user)
        ChatDataObject.user = user
    }

    fun getUserById(userId: String):Flow<ChatUser> {
        return chatRepository.getChatUserById(userId)
    }

    fun setStatus(status: Boolean) = viewModelScope.launch(Dispatchers.Main) {
        _socketStatus.value = status
        if(!status && retryCount < 3){
            viewModelScope.launch {
                delay(1000)
                connectServer()
            }
        }
    }

    // Fetch messages for the current user
    fun getMessagesForUser(userId: String): Flow<List<Message>> {
        return chatRepository.getMessagesForUser(userId)
    }

    // Send a message to the user
    fun sendMessage(userId: String, message: String) {
        viewModelScope.launch {
            chatRepository.sendMessage(userId, message)
        }
    }

    fun connectServer() {
        if (socketStatus.value == false) {
            retryCount++
            webSocket = okHttpClient.newWebSocket(createRequest(), webSocketListener)
        }
    }

    private fun createRequest(): Request {
        val websocketURL =
            "wss://s14523.blr1.piesocket.com/v3/1?api_key=UO8btjsuO4NuJgaKZ4N022wy2rN7NjVvZgdGW3Tx"
        return Request.Builder()
            .url(websocketURL)
            .build()
    }

    fun updateList(message: Message?) {

    }
}
