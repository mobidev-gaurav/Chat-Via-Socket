package com.example.chatapp.utils

import com.example.chatapp.model.ChatUser
import com.example.chatapp.model.Message

object ChatDataObject {
    var user: String = "DevUser"
    var currentScreen:String? = null
    var currentOpenedUser:String? = null
    val currentChatList:ArrayList<ChatUser> = arrayListOf(
        ChatUser("SupportBot", ArrayList()),
        ChatUser("SalesBot", ArrayList()),
        ChatUser("FAQBot", ArrayList())
    )

    fun updateMessages(message: Message) {
        if (message.receiverId == user) {
            val index = currentChatList.indexOfFirst { user -> message.senderId == user.name }
            if(currentOpenedUser != null){
                message.read = currentOpenedUser == message.senderId
            }else{
                message.read = false
            }

            if(index == -1){
                currentChatList.add(ChatUser(message.senderId, arrayListOf(message)))
            }else{
                currentChatList[index].chatList.add(message)
            }
        }
    }

}