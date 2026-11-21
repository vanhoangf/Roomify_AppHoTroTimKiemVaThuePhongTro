package com.roomify.app.ui.student

import android.content.Intent
import android.os.Bundle
import android.widget.Toast

import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager

import com.roomify.app.data.remote.api.NotificationApi
import com.roomify.app.data.remote.common.ApiResult
import com.roomify.app.data.remote.dto.notifications.NotificationResponse
import com.roomify.app.data.remote.network.RetrofitClient
import com.roomify.app.data.repository.NotificationRepository
import com.roomify.app.databinding.ActivityNotificationsBinding

import kotlinx.coroutines.launch

import java.util.Locale

class NotificationsActivity :
    AppCompatActivity() {

    private lateinit var binding:
            ActivityNotificationsBinding

    private lateinit var repository:
            NotificationRepository

    private lateinit var adapter:
            NotificationsAdapter

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        binding =
            ActivityNotificationsBinding.inflate(
                layoutInflater
            )

        setContentView(binding.root)

        setupInsets()
        setupRepository()
        setupRecyclerView()
        setupListeners()

        loadNotifications()
    }

    private fun setupInsets() {

        ViewCompat
            .setOnApplyWindowInsetsListener(
                binding.notificationsRoot
            ) { view, insets ->

                val bars =
                    insets.getInsets(
                        WindowInsetsCompat
                            .Type.systemBars()
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
            NotificationRepository(
                RetrofitClient.createService(
                    NotificationApi::class.java
                )
            )
    }

    private fun setupRecyclerView() {

        adapter =
            NotificationsAdapter {

                handleNotification(it)
            }

        binding.rvNotifications.apply {

            layoutManager =
                LinearLayoutManager(
                    this@NotificationsActivity
                )

            adapter =
                this@NotificationsActivity.adapter
        }
    }

    private fun setupListeners() {

        binding.btnBack
            .setOnClickListener {
                finish()
            }

        binding.btnRetry
            .setOnClickListener {
                loadNotifications()
            }

        binding.btnReadAll
            .setOnClickListener {
                markAllRead()
            }
    }

    private fun loadNotifications() {

        binding.progress.isVisible =
            true

        binding.rvNotifications.isVisible =
            false

        binding.stateContainer.isVisible =
            false

        lifecycleScope.launch {

            when (
                val result =
                    repository.getNotifications()
            ) {

                is ApiResult.Success -> {

                    val data =
                        result.data

                    val items =
                        data?.items.orEmpty()

                    binding.progress.isVisible =
                        false

                    adapter.submitList(items)

                    val unread =
                        data?.unreadCount ?: 0

                    binding.tvUnreadCount.text =
                        if (unread == 0) {
                            "Không có thông báo chưa đọc"
                        } else {
                            "$unread thông báo chưa đọc"
                        }

                    binding.btnReadAll.isEnabled =
                        unread > 0

                    if (items.isEmpty()) {

                        binding.stateContainer.isVisible =
                            true

                        binding.tvState.text =
                            "Bạn chưa có thông báo nào."

                        binding.btnRetry.isVisible =
                            false

                    } else {

                        binding.rvNotifications.isVisible =
                            true
                    }
                }

                is ApiResult.Error -> {

                    binding.progress.isVisible =
                        false

                    binding.stateContainer.isVisible =
                        true

                    binding.btnRetry.isVisible =
                        true

                    binding.tvState.text =
                        result.exception.message
                }

                ApiResult.Loading ->
                    Unit
            }
        }
    }

    private fun handleNotification(
        notification: NotificationResponse
    ) {

        if (notification.isRead) {

            openRelatedScreen(
                notification
            )

            return
        }

        lifecycleScope.launch {

            when (
                val result =
                    repository.markAsRead(
                        notification.notificationId
                    )
            ) {

                is ApiResult.Success -> {

                    val updated =
                        result.data
                            ?: notification

                    loadNotifications()

                    openRelatedScreen(
                        updated
                    )
                }

                is ApiResult.Error -> {

                    Toast.makeText(
                        this@NotificationsActivity,
                        result.exception.message,
                        Toast.LENGTH_LONG
                    ).show()
                }

                ApiResult.Loading ->
                    Unit
            }
        }
    }

    private fun openRelatedScreen(
        notification: NotificationResponse
    ) {

        val type =
            notification.relatedEntityType
                ?.uppercase(Locale.ROOT)

        val id =
            notification.relatedEntityId

        when {

            type == "CONVERSATION" &&
                    id != null &&
                    id > 0 -> {

                startActivity(
                    Intent(
                        this,
                        ChatActivity::class.java
                    ).apply {

                        putExtra(
                            ChatActivity.EXTRA_CONVERSATION_ID,
                            id
                        )
                    }
                )
            }

            else -> {

                Toast.makeText(
                    this,
                    "Đã đánh dấu thông báo là đã đọc.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun markAllRead() {

        binding.btnReadAll.isEnabled =
            false

        lifecycleScope.launch {

            when (
                val result =
                    repository.markAllAsRead()
            ) {

                is ApiResult.Success -> {

                    Toast.makeText(
                        this@NotificationsActivity,
                        "Đã đánh dấu tất cả là đã đọc.",
                        Toast.LENGTH_SHORT
                    ).show()

                    loadNotifications()
                }

                is ApiResult.Error -> {

                    binding.btnReadAll.isEnabled =
                        true

                    Toast.makeText(
                        this@NotificationsActivity,
                        result.exception.message,
                        Toast.LENGTH_LONG
                    ).show()
                }

                ApiResult.Loading ->
                    Unit
            }
        }
    }
}