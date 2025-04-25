package com.example.chatapp.ui.chat

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chatapp.R
import com.example.chatapp.adapter.ChatAdapter
import com.example.chatapp.databinding.FragmentChatBinding
import com.example.chatapp.model.ChatUser
import com.example.chatapp.model.Message
import com.example.chatapp.utils.ChatDataObject
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChatFragment : Fragment(R.layout.fragment_chat) {

    private val viewModel: ChatViewModel by activityViewModels()
    private lateinit var chatAdapter: ChatAdapter

    private var userId: String? = null
    var currentUser:ChatUser? = null

    lateinit var binding: FragmentChatBinding
    val currentId = "6"
    var index = ChatDataObject.currentChatList.indexOfFirst { user -> userId == user.name }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentChatBinding.bind(view)
        super.onViewCreated(view, savedInstanceState)

        userId = arguments?.getString("userId")
        index = ChatDataObject.currentChatList.indexOfFirst { user -> userId == user.name }
        startObservers()
        for(i in ChatDataObject.currentChatList[index].chatList.indices){
            ChatDataObject.currentChatList[index].chatList[i].read = true
        }
        viewModel.updateDatabase()
        chatAdapter = ChatAdapter(viewModel.currentUser.value ?: "")
        binding.messageRecyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false);
        binding.messageRecyclerView.adapter = chatAdapter
        chatAdapter.submitList(ChatDataObject.currentChatList[index].chatList)

        binding.noChat.visibility = if(ChatDataObject.currentChatList[index].chatList.isEmpty()) View.VISIBLE else View.GONE
        binding.messageRecyclerView.visibility = if(ChatDataObject.currentChatList[index].chatList.isEmpty()) View.GONE else View.VISIBLE

        // Handle send button click
        binding.sendButton.setOnClickListener {
            val messageText = binding.editMessage.text.toString().trim()
            if (messageText.isNotEmpty()) {
                viewModel.webSocket?.send(messageText)
                userId?.let {
                    viewModel.sendMessage(it, messageText)
                    val currentMessage = Message(messageText, viewModel.currentUser.value ?: "", userId?:"","", false, true)
                    ChatDataObject.currentChatList[index].chatList.add(currentMessage)
                    viewModel.updateDatabase()

                    if (viewModel.socketStatus.value != false) {
                        currentMessage.sent = true
                        val messageJson = Gson().toJson(currentMessage)
                        viewModel.webSocket?.send(messageJson?:"")
                    }

                    chatAdapter.submitList(ChatDataObject.currentChatList[index].chatList)
                    binding.messageRecyclerView.scrollToPosition(ChatDataObject.currentChatList[index].chatList.size - 1)  // Scroll to the latest message
                    binding.editMessage.text.clear()  // Clear input after sending
                    binding.noChat.visibility = if(ChatDataObject.currentChatList[index].chatList.isEmpty()) View.VISIBLE else View.GONE
                    binding.messageRecyclerView.visibility = if(ChatDataObject.currentChatList[index].chatList.isEmpty()) View.GONE else View.VISIBLE
                }
            }
        }
    }

    private fun startObservers(){
        lifecycleScope.launchWhenStarted {
            viewModel.getUserById(userId?:"").collect{
                currentUser = it
                binding.textView2.text = it.name
            }
            userId?.let {
                chatAdapter.submitList(ChatDataObject.currentChatList[index].chatList)
            }
        }

        viewModel.databaseUpdated.observe(viewLifecycleOwner){
            chatAdapter.notifyDataSetChanged()
            Log.e("ChatFragment", "startObservers: updated")
        }

        viewModel.socketStatus.observe(viewLifecycleOwner){
            if(it){
                binding.connectionStatus.text = "Connection Status: Connected"
            }else{
                binding.connectionStatus.text = "Connection Status: Not Connected"
            }
        }
    }

    override fun onResume() {
        super.onResume()
        ChatDataObject.currentScreen = ChatFragment::class.java.name
        ChatDataObject.currentOpenedUser = userId
    }

}
