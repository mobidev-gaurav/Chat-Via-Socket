package com.example.chatapp.ui.chat

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.EditText
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.chatapp.R
import com.example.chatapp.adapter.ChatUserAdapter
import com.example.chatapp.databinding.FragmentChatListBinding
import com.example.chatapp.utils.ChatDataObject
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChatListFragment : Fragment(R.layout.fragment_chat_list) {

    private val viewModel: ChatViewModel by activityViewModels()

    private lateinit var chatAdapter: ChatUserAdapter
    private lateinit var binding:FragmentChatListBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentChatListBinding.bind(view)
        chatAdapter = ChatUserAdapter { userId ->
            val action = ChatListFragmentDirections
                .actionChatListFragmentToChatFragment(userId = userId)
            findNavController().navigate(action)
        }

        ChatDataObject.currentScreen = this::class.java.name
        ChatDataObject.currentOpenedUser = null

        viewModel.currentUser.observe(viewLifecycleOwner){
            binding.textView2.text = "Current user $it"
        }

        viewModel.databaseUpdated.observe(viewLifecycleOwner){
            chatAdapter.notifyDataSetChanged()
        }

        viewModel.socketStatus.observe(viewLifecycleOwner){
            if(it){
                binding.connectionStatus.text = "Connection Status: Connected"
            }else{
                binding.connectionStatus.text = "Connection Status: Not Connected"
            }
        }

        binding.editUserName.setOnClickListener {
            showDialog()
        }

        binding.chatRecyclerView.adapter = chatAdapter
        chatAdapter.submitList(ChatDataObject.currentChatList)
        // Observe chat users
        lifecycleScope.launchWhenStarted {
            viewModel.chatUsers.collect { users ->
                binding.progressBar.isVisible = users.isEmpty()
                chatAdapter.submitList(users)
            }
        }
    }

    fun showDialog(){
        val editText = EditText(requireContext());
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Title")
            .setMessage("Message")
            .setView(editText)
            .setPositiveButton("OK"
            ) { dialog, which ->
                var editTextInput = editText.getText().toString()
                binding.textView2.text = "Current User: $editTextInput"
                viewModel.updateCurrentUser(editTextInput)
            }
            .setNegativeButton("Cancel", null)
            .create();
        dialog.show();
    }

}
