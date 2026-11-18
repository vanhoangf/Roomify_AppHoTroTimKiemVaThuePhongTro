package com.roomify.app.ui.student

import android.os.Bundle
import android.widget.Toast

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager

import com.roomify.app.data.remote.api.MessagingApi
import com.roomify.app.data.remote.common.ApiResult
import com.roomify.app.data.remote.network.RetrofitClient
import com.roomify.app.data.repository.MessagingRepository
import com.roomify.app.databinding.ActivityChatBinding

import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class ChatActivity :
    AppCompatActivity() {

    companion object {

        const val EXTRA_CONVERSATION_ID =
            "extra_conversation_id"
    }

    private lateinit var binding:
            ActivityChatBinding

    private lateinit var repository:
            MessagingRepository

    private lateinit var adapter:
            ChatMessagesAdapter

    private var conversationId =
        -1L

    private var refreshJob:
            Job? = null

    private var isSending =
        false

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        binding =
            ActivityChatBinding.inflate(
                layoutInflater
            )

        setContentView(binding.root)
        setupInsets()

        conversationId =
            intent.getLongExtra(
                EXTRA_CONVERSATION_ID,
                -1L
            )

        if (conversationId <= 0) {
            finish()
            return
        }

        RetrofitClient.initialize(
            applicationContext
        )

        repository =
            MessagingRepository(
                RetrofitClient.createService(
                    MessagingApi::class.java
                )
            )

        adapter =
            ChatMessagesAdapter()

        binding.rvMessages.apply {

            layoutManager =
                LinearLayoutManager(
                    this@ChatActivity
                ).apply {
                    stackFromEnd = true
                }

            adapter =
                this@ChatActivity.adapter
        }

        binding.btnBack
            .setOnClickListener {
                finish()
            }

        binding.btnSend
            .setOnClickListener {
                sendMessage()
            }

        loadConversation()
        loadMessages()
    }

    override fun onResume() {
        super.onResume()

        startAutoRefresh()
    }

    override fun onPause() {
        super.onPause()

        refreshJob?.cancel()
        refreshJob = null
    }

    private fun setupInsets() {

        ViewCompat.setOnApplyWindowInsetsListener(
            binding.chatRoot
        ) { view, insets ->

            val systemBars =
                insets.getInsets(
                    WindowInsetsCompat.Type.systemBars()
                )

            view.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                systemBars.bottom
            )

            insets
        }
    }
    private fun loadConversation() {

        lifecycleScope.launch {

            when (
                val result =
                    repository.getConversation(
                        conversationId
                    )
            ) {

                is ApiResult.Success -> {

                    result.data?.let {

                        binding.tvLandlordName.text =
                            it.landlordName

                        binding.tvRoomTitle.text =
                            it.roomTitle
                    }
                }

                else -> Unit
            }
        }
    }

    private fun loadMessages(
        scrollToBottom: Boolean = true
    ) {

        lifecycleScope.launch {

            when (
                val result =
                    repository.getMessages(
                        conversationId
                    )
            ) {

                is ApiResult.Success -> {

                    val messages =
                        result.data.orEmpty()

                    adapter.submitList(
                        messages
                    ) {

                        if (
                            scrollToBottom &&
                            messages.isNotEmpty()
                        ) {

                            binding.rvMessages
                                .scrollToPosition(
                                    messages.lastIndex
                                )
                        }
                    }

                    repository.markAsRead(
                        conversationId
                    )
                }

                is ApiResult.Error -> {

                    if (adapter.itemCount == 0) {

                        Toast.makeText(
                            this@ChatActivity,
                            result.exception.message,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

                ApiResult.Loading ->
                    Unit
            }
        }
    }

    private fun sendMessage() {

        if (isSending) {
            return
        }

        val content =
            binding.edtMessage.text
                ?.toString()
                ?.trim()
                .orEmpty()

        if (content.isBlank()) {
            return
        }

        isSending =
            true

        binding.btnSend.isEnabled =
            false

        lifecycleScope.launch {

            try {

                when (
                    val result =
                        repository.sendMessage(
                            conversationId,
                            content
                        )
                ) {

                    is ApiResult.Success -> {

                        binding.edtMessage
                            .setText("")

                        loadMessages()
                    }

                    is ApiResult.Error -> {

                        Toast.makeText(
                            this@ChatActivity,
                            result.exception.message,
                            Toast.LENGTH_LONG
                        ).show()
                    }

                    ApiResult.Loading ->
                        Unit
                }

            } finally {

                isSending =
                    false

                binding.btnSend.isEnabled =
                    true
            }
        }
    }

    private fun startAutoRefresh() {

        refreshJob?.cancel()

        refreshJob =
            lifecycleScope.launch {

                while (isActive) {

                    delay(5000)

                    loadMessages(
                        scrollToBottom = false
                    )
                }
            }
    }
}