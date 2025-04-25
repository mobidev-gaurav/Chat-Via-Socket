package com.example.chatapp.service

import com.example.chatapp.model.Message
import com.example.chatapp.ui.chat.ChatViewModel
import com.example.chatapp.utils.ChatDataObject
import com.google.gson.Gson
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener

class WebSocketListener(
    private val viewModel: ChatViewModel
): WebSocketListener() {

    override fun onOpen(webSocket: WebSocket, response: Response) {
        super.onOpen(webSocket, response)
        viewModel.setStatus(true)
        for(i in ChatDataObject.currentChatList.indices){
            for (j in ChatDataObject.currentChatList[i].chatList.indices){
                if(!ChatDataObject.currentChatList[i].chatList[j].sent){
                    ChatDataObject.currentChatList[i].chatList[j].sent = true
                    val messageJson = Gson().toJson(ChatDataObject.currentChatList[i].chatList[j])
                    webSocket.send(messageJson?:"")
                }
            }
        }
        viewModel.updateDatabase()
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        super.onMessage(webSocket, text)
        try {
            val message = Gson().fromJson(text, Message::class.java)
            ChatDataObject.updateMessages(message)
            viewModel.updateDatabase()
        }catch (e:Exception){
            e.printStackTrace()
        }
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        super.onClosing(webSocket, code, reason)
    }

    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
        super.onClosed(webSocket, code, reason)
        viewModel.setStatus(false)
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        viewModel.setStatus(false)
        super.onFailure(webSocket, t, response)
    }
}