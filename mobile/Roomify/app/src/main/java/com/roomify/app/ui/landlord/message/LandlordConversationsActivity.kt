package com.roomify.app.ui.landlord.message

import android.content.Intent
import android.os.Bundle
import android.widget.Toast

import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager

import com.roomify.app.R
import com.roomify.app.data.remote.api.LandlordMessagingApi
import com.roomify.app.data.remote.common.ApiResult
import com.roomify.app.data.remote.dto.messaging.ConversationResponse
import com.roomify.app.data.remote.network.RetrofitClient
import com.roomify.app.data.repository.LandlordMessagingRepository
import com.roomify.app.databinding.ActivityLandlordConversationsBinding
import com.roomify.app.ui.common.RoleSessionGuard
import com.roomify.app.ui.landlord.LandlordHomeActivity
import com.roomify.app.ui.landlord.property.LandlordPropertiesActivity
import com.roomify.app.ui.landlord.rental.LandlordRentalRequestsActivity
import com.roomify.app.ui.landlord.account.LandlordAccountActivity

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

class LandlordConversationsActivity :
    AppCompatActivity() {

    private lateinit var binding:
            ActivityLandlordConversationsBinding

    private lateinit var repository:
            LandlordMessagingRepository

    private val loadedItems =
        mutableListOf<ConversationResponse>()

    private val adapter by lazy {

        LandlordConversationAdapter {
                conversation ->

            openChat(
                conversation.conversationId
            )
        }
    }

    private var currentPage =
        1

    private var hasNextPage =
        false

    private var isLoading =
        false

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(
            savedInstanceState
        )

        binding =
            ActivityLandlordConversationsBinding.inflate(
                layoutInflater
            )

        setContentView(
            binding.root
        )

        setupInsets()
        setupRepository()
        setupRecyclerView()
        setupActions()
        setupBottomNavigation()
    }

    private fun setupInsets() {

        ViewCompat.setOnApplyWindowInsetsListener(
            binding.landlordConversationsRoot
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

        binding.rvConversations.apply {

            layoutManager =
                LinearLayoutManager(
                    this@LandlordConversationsActivity
                )

            adapter =
                this@LandlordConversationsActivity
                    .adapter
        }
    }

    private fun setupActions() {

        binding.btnRetry
            .setOnClickListener {

                loadConversations(
                    reset = true
                )
            }

        binding.btnLoadMore
            .setOnClickListener {

                if (
                    hasNextPage &&
                    !isLoading
                ) {

                    loadConversations(
                        reset = false
                    )
                }
            }
    }

    private fun setupBottomNavigation() {

        binding.bottomNavigationLandlord
            .selectedItemId =
            R.id.navLandlordMessages

        binding.bottomNavigationLandlord
            .setOnItemSelectedListener {
                    item ->

                when (
                    item.itemId
                ) {

                    R.id.navLandlordMessages -> {
                        true
                    }

                    R.id.navLandlordAccount -> {

                        startActivity(
                            Intent(
                                this,
                                LandlordAccountActivity::class.java
                            )
                        )

                        true
                    }

                    R.id.navLandlordHome -> {

                        startActivity(
                            Intent(
                                this,
                                LandlordHomeActivity::class.java
                            )
                        )

                        finish()

                        true
                    }

                    R.id.navLandlordProperties -> {

                        startActivity(
                            Intent(
                                this,
                                LandlordPropertiesActivity::class.java
                            )
                        )

                        true
                    }

                    R.id.navLandlordRequests -> {

                        startActivity(
                            Intent(
                                this,
                                LandlordRentalRequestsActivity::class.java
                            )
                        )

                        true
                    }

                    else -> {
                        false
                    }
                }
            }
    }

    private fun loadConversations(
        reset: Boolean
    ) {

        if (isLoading) {
            return
        }

        if (reset) {

            currentPage =
                1

            loadedItems.clear()

            binding.progressConversations.isVisible =
                true

            binding.rvConversations.isVisible =
                false

            binding.stateContainer.isVisible =
                false

            binding.btnLoadMore.isVisible =
                false

        } else {

            binding.btnLoadMore.isEnabled =
                false

            binding.btnLoadMore.text =
                "Đang tải..."
        }

        isLoading =
            true

        lifecycleScope.launch {

            try {

                when (
                    val result =
                        repository.getConversations(
                            page =
                                currentPage,
                            pageSize =
                                20
                        )
                ) {

                    is ApiResult.Success -> {

                        val data =
                            result.data

                        if (data == null) {

                            showError(
                                "Không nhận được dữ liệu cuộc trò chuyện."
                            )

                            return@launch
                        }

                        loadedItems.addAll(
                            data.items
                        )

                        hasNextPage =
                            data.hasNextPage

                        binding.tvConversationCount.text =
                            "${data.totalCount} cuộc trò chuyện"

                        if (
                            loadedItems.isEmpty()
                        ) {

                            showEmpty()

                        } else {

                            adapter.submitList(
                                loadedItems.toList()
                            )

                            binding.progressConversations.isVisible =
                                false

                            binding.stateContainer.isVisible =
                                false

                            binding.rvConversations.isVisible =
                                true

                            if (hasNextPage) {
                                currentPage++
                            }
                        }
                    }

                    is ApiResult.Error -> {

                        if (
                            loadedItems.isEmpty()
                        ) {

                            showError(
                                result.exception.message
                                    ?: "Không thể tải tin nhắn."
                            )

                        } else {

                            Toast.makeText(
                                this@LandlordConversationsActivity,
                                result.exception.message
                                    ?: "Không thể tải thêm.",
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

            } catch (
                e: Exception
            ) {

                if (
                    loadedItems.isEmpty()
                ) {

                    showError(
                        e.message
                            ?: "Không thể tải tin nhắn."
                    )
                }

            } finally {

                isLoading =
                    false

                binding.btnLoadMore.isEnabled =
                    true

                binding.btnLoadMore.text =
                    "Xem thêm"

                binding.btnLoadMore.isVisible =
                    loadedItems.isNotEmpty() &&
                            hasNextPage
            }
        }
    }

    private fun openChat(
        conversationId: Long
    ) {

        if (
            conversationId <= 0L
        ) {
            return
        }

        startActivity(
            Intent(
                this,
                LandlordChatActivity::class.java
            ).apply {

                putExtra(
                    LandlordChatActivity
                        .EXTRA_CONVERSATION_ID,
                    conversationId
                )
            }
        )
    }

    private fun showEmpty() {

        binding.progressConversations.isVisible =
            false

        binding.rvConversations.isVisible =
            false

        binding.stateContainer.isVisible =
            true

        binding.btnRetry.isVisible =
            false

        binding.btnLoadMore.isVisible =
            false

        binding.tvStateMessage.text =
            "Chưa có cuộc trò chuyện"

        binding.tvStateDescription.text =
            "Khi sinh viên nhắn tin về phòng của bạn, cuộc trò chuyện sẽ xuất hiện tại đây."

        adapter.submitList(
            emptyList()
        )
    }

    private fun showError(
        message: String
    ) {

        binding.progressConversations.isVisible =
            false

        binding.rvConversations.isVisible =
            false

        binding.stateContainer.isVisible =
            true

        binding.btnRetry.isVisible =
            true

        binding.btnLoadMore.isVisible =
            false

        binding.tvStateMessage.text =
            "Không thể tải tin nhắn"

        binding.tvStateDescription.text =
            message
    }

    override fun onResume() {
        super.onResume()

        if (
            ::repository.isInitialized
        ) {

            loadConversations(
                reset = true
            )
        }
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