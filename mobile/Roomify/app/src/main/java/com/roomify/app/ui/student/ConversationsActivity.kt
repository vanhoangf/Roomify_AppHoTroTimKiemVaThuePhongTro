package com.roomify.app.ui.student

import android.content.Intent
import android.os.Bundle

import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager

import com.roomify.app.R
import com.roomify.app.data.remote.api.MessagingApi
import com.roomify.app.data.remote.common.ApiResult
import com.roomify.app.data.remote.network.RetrofitClient
import com.roomify.app.data.repository.MessagingRepository
import com.roomify.app.databinding.ActivityConversationsBinding

import kotlinx.coroutines.launch

class ConversationsActivity :
    AppCompatActivity() {

    private lateinit var binding:
            ActivityConversationsBinding

    private lateinit var repository:
            MessagingRepository

    private lateinit var adapter:
            ConversationsAdapter

    private var isLoading =
        false

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        binding =
            ActivityConversationsBinding.inflate(
                layoutInflater
            )

        setContentView(
            binding.root
        )

        setupInsets()
        setupRepository()
        setupRecyclerView()
        setupBottomNavigation()
        setupListeners()

        loadConversations()
    }

    override fun onResume() {
        super.onResume()

        if (::repository.isInitialized) {
            loadConversations()
        }
    }

    private fun setupInsets() {

        ViewCompat.setOnApplyWindowInsetsListener(
            binding.conversationsRoot
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

        /*
         * Đồng bộ chiều cao nav với Home/Search/Saved.
         */
        ViewCompat.setOnApplyWindowInsetsListener(
            binding.bottomNavigation
        ) { view, insets ->

            view.setPadding(
                view.paddingLeft,
                view.paddingTop,
                view.paddingRight,
                0
            )

            insets
        }
    }

    private fun setupRepository() {

        RetrofitClient.initialize(
            applicationContext
        )

        repository =
            MessagingRepository(
                RetrofitClient.createService(
                    MessagingApi::class.java
                )
            )
    }

    private fun setupRecyclerView() {

        adapter =
            ConversationsAdapter {

                openChat(
                    it.conversationId
                )
            }

        binding.rvConversations.apply {

            layoutManager =
                LinearLayoutManager(
                    this@ConversationsActivity
                )

            adapter =
                this@ConversationsActivity.adapter
        }
    }

    private fun setupBottomNavigation() {

        StudentBottomNavHelper.setup(
            activity = this,
            bottomNavigation =
                binding.bottomNavigation,
            selectedItemId =
                R.id.navMessages
        )
    }

    private fun setupListeners() {

        binding.btnRetry
            .setOnClickListener {

                loadConversations()
            }
    }

    private fun loadConversations() {

        if (isLoading) {
            return
        }

        isLoading =
            true

        val hasOldData =
            adapter.currentList
                .isNotEmpty()

        if (!hasOldData) {

            binding.progress.isVisible =
                true

            binding.rvConversations.isVisible =
                false

            binding.stateContainer.isVisible =
                false
        }

        lifecycleScope.launch {

            try {

                when (
                    val result =
                        repository.getConversations()
                ) {

                    is ApiResult.Success -> {

                        val items =
                            result.data
                                ?.items
                                .orEmpty()

                        adapter.submitList(
                            items
                        )

                        binding.progress.isVisible =
                            false

                        if (items.isEmpty()) {

                            binding.rvConversations.isVisible =
                                false

                            binding.stateContainer.isVisible =
                                true

                            binding.tvState.text =
                                "Bạn chưa có cuộc trò chuyện nào"

                            binding.btnRetry.isVisible =
                                false

                        } else {

                            binding.rvConversations.isVisible =
                                true

                            binding.stateContainer.isVisible =
                                false
                        }
                    }

                    is ApiResult.Error -> {

                        binding.progress.isVisible =
                            false

                        if (hasOldData) {

                            binding.rvConversations.isVisible =
                                true

                        } else {

                            binding.rvConversations.isVisible =
                                false

                            binding.stateContainer.isVisible =
                                true

                            binding.tvState.text =
                                result.exception.message

                            binding.btnRetry.isVisible =
                                true
                        }
                    }

                    ApiResult.Loading ->
                        Unit
                }

            } finally {

                isLoading =
                    false
            }
        }
    }

    private fun openChat(
        conversationId: Long
    ) {

        if (conversationId <= 0L) {
            return
        }

        startActivity(
            Intent(
                this,
                ChatActivity::class.java
            ).apply {

                putExtra(
                    ChatActivity.EXTRA_CONVERSATION_ID,
                    conversationId
                )
            }
        )
    }
}