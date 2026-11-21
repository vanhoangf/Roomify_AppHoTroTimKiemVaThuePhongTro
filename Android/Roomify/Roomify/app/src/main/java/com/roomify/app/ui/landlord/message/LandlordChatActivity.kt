package com.roomify.app.ui.landlord.message

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

import com.roomify.app.R

import com.roomify.app.data.remote.api.LandlordMessagingApi
import com.roomify.app.data.remote.common.ApiResult
import com.roomify.app.data.remote.network.RetrofitClient
import com.roomify.app.data.repository.LandlordMessagingRepository
import com.roomify.app.ui.common.RoleSessionGuard

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class LandlordChatActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_CONVERSATION_ID =
            "extra_landlord_conversation_id"
    }

    private lateinit var landlordChatRoot: View

    private lateinit var btnBack: ImageButton

    private lateinit var tvStudentAvatar: TextView
    private lateinit var tvStudentName: TextView
    private lateinit var tvRoomTitle: TextView

    private lateinit var rvMessages: RecyclerView

    private lateinit var messageInputContainer: LinearLayout

    private lateinit var progressMessages:
            CircularProgressIndicator

    private lateinit var messageStateContainer:
            LinearLayout

    private lateinit var tvMessageStateTitle:
            TextView

    private lateinit var tvMessageStateDescription:
            TextView

    private lateinit var btnRetryMessages:
            MaterialButton

    private lateinit var tilMessage:
            TextInputLayout

    private lateinit var edtMessage:
            TextInputEditText

    private lateinit var btnSend:
            MaterialButton

    private lateinit var repository:
            LandlordMessagingRepository

    private lateinit var adapter:
            LandlordChatMessagesAdapter

    private var conversationId =
        0L

    private var refreshJob:
            Job? = null

    private var isSending =
        false

    private var initialLoad =
        true

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        setContentView(
            R.layout.activity_landlord_chat
        )

        bindViews()

        conversationId =
            intent.getLongExtra(
                EXTRA_CONVERSATION_ID,
                0L
            )

        if (conversationId <= 0L) {

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

    private fun bindViews() {

        landlordChatRoot =
            findViewById(
                R.id.landlordChatRoot
            )

        messageInputContainer =
            findViewById(
                R.id.messageInputContainer
            )

        btnBack =
            findViewById(
                R.id.btnBack
            )

        tvStudentAvatar =
            findViewById(
                R.id.tvStudentAvatar
            )

        tvStudentName =
            findViewById(
                R.id.tvStudentName
            )

        tvRoomTitle =
            findViewById(
                R.id.tvRoomTitle
            )

        rvMessages =
            findViewById(
                R.id.rvMessages
            )

        progressMessages =
            findViewById(
                R.id.progressMessages
            )

        messageStateContainer =
            findViewById(
                R.id.messageStateContainer
            )

        tvMessageStateTitle =
            findViewById(
                R.id.tvMessageStateTitle
            )

        tvMessageStateDescription =
            findViewById(
                R.id.tvMessageStateDescription
            )

        btnRetryMessages =
            findViewById(
                R.id.btnRetryMessages
            )

        tilMessage =
            findViewById(
                R.id.tilMessage
            )

        edtMessage =
            findViewById(
                R.id.edtMessage
            )

        btnSend =
            findViewById(
                R.id.btnSend
            )
    }

    private fun setupInsets() {

        ViewCompat.setOnApplyWindowInsetsListener(
            landlordChatRoot
        ) { view, insets ->

            val statusBars =
                insets.getInsets(
                    WindowInsetsCompat.Type.statusBars()
                )

            val navigationBars =
                insets.getInsets(
                    WindowInsetsCompat.Type.navigationBars()
                )

            val density =
                resources.displayMetrics.density

            fun dp(value: Int): Int =
                (value * density).toInt()

            view.setPadding(
                0,
                statusBars.top,
                0,
                0
            )

            messageInputContainer.setPadding(
                dp(12),
                dp(10),
                dp(12),
                dp(10) + navigationBars.bottom
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

        rvMessages.layoutManager =
            LinearLayoutManager(
                this
            ).apply {

                stackFromEnd = true
            }

        rvMessages.adapter =
            adapter
    }

    private fun setupActions() {

        btnBack.setOnClickListener {
            finish()
        }

        btnSend.setOnClickListener {
            sendMessage()
        }

        btnRetryMessages
            .setOnClickListener {

                initialLoad = true

                loadConversation()

                loadMessages(
                    scrollToBottom = true
                )
            }
    }

    private fun loadConversation() {

        lifecycleScope.launch {

            try {

                when (
                    val result =
                        repository
                            .getConversation(
                                conversationId
                            )
                ) {

                    is ApiResult.Success -> {

                        result.data
                            ?.let {
                                    conversation ->

                                val studentName =
                                    conversation
                                        .studentName
                                        .ifBlank {
                                            "Sinh viên"
                                        }

                                tvStudentName.text =
                                    studentName

                                tvRoomTitle.text =
                                    conversation
                                        .roomTitle
                                        .ifBlank {
                                            "Phòng Roomify"
                                        }

                                tvStudentAvatar.text =
                                    studentName
                                        .trim()
                                        .firstOrNull()
                                        ?.uppercase()
                                        ?: "S"
                            }
                    }

                    is ApiResult.Error -> {

                        if (initialLoad) {

                            Toast.makeText(
                                this@LandlordChatActivity,
                                result
                                    .exception
                                    .message
                                    ?: "Không thể tải thông tin cuộc trò chuyện.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }

                    ApiResult.Loading -> Unit
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

        if (initialLoad) {
            showLoading()
        }

        lifecycleScope.launch {

            try {

                when (
                    val result =
                        repository
                            .getMessages(
                                conversationId
                            )
                ) {

                    is ApiResult.Success -> {

                        val messages =
                            result.data
                                .orEmpty()

                        adapter.submitList(
                            messages
                        ) {

                            if (
                                scrollToBottom &&
                                messages.isNotEmpty()
                            ) {

                                rvMessages
                                    .scrollToPosition(
                                        messages.lastIndex
                                    )
                            }
                        }

                        if (
                            messages.isEmpty()
                        ) {
                            showEmpty()
                        } else {
                            showMessages()
                        }

                        markAsRead()
                    }

                    is ApiResult.Error -> {

                        if (
                            adapter.itemCount == 0
                        ) {

                            showMessageError(
                                result
                                    .exception
                                    .message
                                    ?: "Không thể tải tin nhắn."
                            )
                        }
                    }

                    ApiResult.Loading -> Unit
                }

            } catch (
                e: CancellationException
            ) {

                throw e

            } catch (
                e: Exception
            ) {

                if (
                    adapter.itemCount == 0
                ) {

                    showMessageError(
                        e.message
                            ?: "Không thể tải tin nhắn."
                    )
                }

            } finally {

                progressMessages
                    .isVisible = false

                initialLoad = false
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

        tilMessage.error =
            null

        val content =
            edtMessage.text
                ?.toString()
                ?.trim()
                .orEmpty()

        if (content.isBlank()) {

            tilMessage.error =
                "Vui lòng nhập tin nhắn."

            return
        }

        if (
            content.length > 2000
        ) {

            tilMessage.error =
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
                        repository
                            .sendMessage(
                                conversationId =
                                    conversationId,
                                content =
                                    content
                            )
                ) {

                    is ApiResult.Success -> {

                        edtMessage
                            .setText("")

                        tilMessage.error =
                            null

                        loadMessages(
                            scrollToBottom = true
                        )
                    }

                    is ApiResult.Error -> {

                        Toast.makeText(
                            this@LandlordChatActivity,
                            result
                                .exception
                                .message
                                ?: "Không thể gửi tin nhắn.",
                            Toast.LENGTH_LONG
                        ).show()
                    }

                    ApiResult.Loading -> Unit
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

        btnSend.isEnabled =
            !value

        edtMessage.isEnabled =
            !value

        btnSend.text =
            if (value) {
                "..."
            } else {
                "Gửi"
            }
    }

    private fun showLoading() {

        progressMessages
            .isVisible = true

        rvMessages
            .isVisible = false

        messageStateContainer
            .isVisible = false
    }

    private fun showMessages() {

        progressMessages
            .isVisible = false

        messageStateContainer
            .isVisible = false

        rvMessages
            .isVisible = true
    }

    private fun showEmpty() {

        progressMessages
            .isVisible = false

        rvMessages
            .isVisible = false

        messageStateContainer
            .isVisible = true

        tvMessageStateTitle.text =
            "Chưa có tin nhắn"

        tvMessageStateDescription.text =
            "Hãy gửi lời chào để bắt đầu cuộc trò chuyện với sinh viên."

        btnRetryMessages
            .isVisible = false
    }

    private fun showMessageError(
        message: String
    ) {

        progressMessages
            .isVisible = false

        rvMessages
            .isVisible = false

        messageStateContainer
            .isVisible = true

        tvMessageStateTitle.text =
            "Không thể tải tin nhắn"

        tvMessageStateDescription.text =
            message

        btnRetryMessages
            .isVisible = true
    }

    private fun startAutoRefresh() {

        refreshJob?.cancel()

        refreshJob =
            lifecycleScope.launch {

                while (isActive) {

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
        refreshJob = null
    }

    override fun onStart() {

        super.onStart()

        RoleSessionGuard.verify(
            activity = this,
            expectedRole = "LANDLORD",
            onNetworkError = {
                    message ->

                Toast.makeText(
                    this,
                    message,
                    Toast.LENGTH_LONG
                ).show()
            }
        )
    }
}