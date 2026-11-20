package com.roomify.app.ui.landlord.message

import android.os.Bundle
import android.widget.Toast

import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager

import com.roomify.app.data.remote.api.LandlordMessagingApi
import com.roomify.app.data.remote.common.ApiResult
import com.roomify.app.data.remote.network.RetrofitClient
import com.roomify.app.data.repository.LandlordMessagingRepository
import com.roomify.app.databinding.ActivityLandlordChatBinding
import com.roomify.app.ui.common.RoleSessionGuard

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class LandlordChatActivity :
    AppCompatActivity() {

    companion object {

        const val EXTRA_CONVERSATION_ID =
            "extra_landlord_conversation_id"
    }

    private lateinit var binding:
            ActivityLandlordChatBinding

    private lateinit var repository:
            LandlordMessagingRepository

    private lateinit var adapter:
            LandlordChatMessagesAdapter

    private var conversationId =
        0L

    private var refreshJob:
            Job? =
        null

    private var isSending =
        false

    private var initialLoad =
        true

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(
            savedInstanceState
        )

        binding =
            ActivityLandlordChatBinding.inflate(
                layoutInflater
            )

        setContentView(
            binding.root
        )

        conversationId =
            intent.getLongExtra(
                EXTRA_CONVERSATION_ID,
                0L
            )

        if (
            conversationId <= 0L
        ) {

            Toast.makeText(
                this,
                "Không tìm thấy cuộc trò chuyện.",
                Toast.LENGTH_LONG
            ).show()

            finish()

            return
        }

        setupInsets()
        setupRepository()
        setupRecyclerView()
        setupActions()

        loadConversation()
        loadMessages(
            scrollToBottom = true
        )
    }

    private fun setupInsets() {

        ViewCompat.setOnApplyWindowInsetsListener(
            binding.landlordChatRoot
        ) { view, insets ->

            val bars =
                insets.getInsets(
                    WindowInsetsCompat.Type.systemBars()
                )

            view.setPadding(
                bars.left,
                bars.top,
                bars.right,
                bars.bottom
            )

            insets
        }
    }

    private fun setupRepository() {

        RetrofitClient.initialize(
            applicationContext
        )

        repository =
            LandlordMessagingRepository(
                RetrofitClient.createService(
                    LandlordMessagingApi::class.java
                )
            )
    }

    private fun setupRecyclerView() {

        adapter =
            LandlordChatMessagesAdapter()

        binding.rvMessages.apply {

            layoutManager =
                LinearLayoutManager(
                    this@LandlordChatActivity
                ).apply {

                    stackFromEnd =
                        true
                }

            adapter =
                this@LandlordChatActivity
                    .adapter
        }
    }

    private fun setupActions() {

        binding.btnBack
            .setOnClickListener {

                finish()
            }

        binding.btnSend
            .setOnClickListener {

                sendMessage()
            }
    }

    private fun loadConversation() {

        lifecycleScope.launch {

            try {

                when (
                    val result =
                        repository.getConversation(
                            conversationId
                        )
                ) {

                    is ApiResult.Success -> {

                        result.data
                            ?.let {
                                    conversation ->

                                binding.tvStudentName.text =
                                    conversation.studentName

                                binding.tvRoomTitle.text =
                                    conversation.roomTitle
                            }
                    }

                    is ApiResult.Error -> {

                        Toast.makeText(
                            this@LandlordChatActivity,
                            result.exception.message
                                ?: "Không thể tải thông tin cuộc trò chuyện.",
                            Toast.LENGTH_LONG
                        ).show()
                    }

                    ApiResult.Loading ->
                        Unit
                }

            } catch (
                e: CancellationException
            ) {

                throw e
            }
        }
    }

    private fun loadMessages(
        scrollToBottom: Boolean
    ) {

        if (
            initialLoad
        ) {

            binding.progressMessages.isVisible =
                true
        }

        lifecycleScope.launch {

            try {

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

                        markAsRead()
                    }

                    is ApiResult.Error -> {

                        if (
                            adapter.itemCount == 0
                        ) {

                            Toast.makeText(
                                this@LandlordChatActivity,
                                result.exception.message
                                    ?: "Không thể tải tin nhắn.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }

                    ApiResult.Loading ->
                        Unit
                }

            } catch (
                e: CancellationException
            ) {

                throw e

            } finally {

                binding.progressMessages.isVisible =
                    false

                initialLoad =
                    false
            }
        }
    }

    private fun markAsRead() {

        lifecycleScope.launch {

            try {

                repository.markAsRead(
                    conversationId
                )

            } catch (
                e: CancellationException
            ) {

                throw e

            } catch (
                _: Exception
            ) {
            }
        }
    }

    private fun sendMessage() {

        if (isSending) {
            return
        }

        binding.tilMessage.error =
            null

        val content =
            binding.edtMessage.text
                ?.toString()
                ?.trim()
                .orEmpty()

        if (
            content.isBlank()
        ) {

            binding.tilMessage.error =
                "Vui lòng nhập tin nhắn."

            return
        }

        if (
            content.length > 2000
        ) {

            binding.tilMessage.error =
                "Tin nhắn tối đa 2000 ký tự."

            return
        }

        setSending(
            true
        )

        lifecycleScope.launch {

            try {

                when (
                    val result =
                        repository.sendMessage(
                            conversationId =
                                conversationId,
                            content =
                                content
                        )
                ) {

                    is ApiResult.Success -> {

                        binding.edtMessage
                            .setText("")

                        loadMessages(
                            scrollToBottom = true
                        )
                    }

                    is ApiResult.Error -> {

                        Toast.makeText(
                            this@LandlordChatActivity,
                            result.exception.message
                                ?: "Không thể gửi tin nhắn.",
                            Toast.LENGTH_LONG
                        ).show()
                    }

                    ApiResult.Loading ->
                        Unit
                }

            } catch (
                e: CancellationException
            ) {

                throw e

            } catch (
                e: Exception
            ) {

                Toast.makeText(
                    this@LandlordChatActivity,
                    e.message
                        ?: "Không thể gửi tin nhắn.",
                    Toast.LENGTH_LONG
                ).show()

            } finally {

                setSending(
                    false
                )
            }
        }
    }

    private fun setSending(
        value: Boolean
    ) {

        isSending =
            value

        binding.btnSend.isEnabled =
            !value

        binding.edtMessage.isEnabled =
            !value

        binding.btnSend.text =
            if (value) {
                "..."
            } else {
                "Gửi"
            }
    }

    private fun startAutoRefresh() {

        refreshJob?.cancel()

        refreshJob =
            lifecycleScope.launch {

                while (
                    isActive
                ) {

                    delay(
                        5000L
                    )

                    loadMessages(
                        scrollToBottom = false
                    )
                }
            }
    }

    override fun onResume() {
        super.onResume()

        if (
            ::repository.isInitialized
        ) {

            startAutoRefresh()
        }
    }

    override fun onPause() {
        super.onPause()

        refreshJob?.cancel()

        refreshJob =
            null
    }

    override fun onStart() {
        super.onStart()

        RoleSessionGuard.verify(
            activity = this,
            expectedRole = "LANDLORD",
            onNetworkError = { message ->

                Toast.makeText(
                    this,
                    message,
                    Toast.LENGTH_LONG
                ).show()
            }
        )
    }
}