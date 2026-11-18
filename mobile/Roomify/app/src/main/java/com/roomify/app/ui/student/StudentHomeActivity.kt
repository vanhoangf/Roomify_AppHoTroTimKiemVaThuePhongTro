package com.roomify.app.ui.student

import android.content.Intent
import android.os.Bundle
import android.widget.Toast

import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager

import com.roomify.app.R
import com.roomify.app.data.remote.api.RoomApi
import com.roomify.app.data.remote.common.ApiResult
import com.roomify.app.data.remote.network.RetrofitClient
import com.roomify.app.data.repository.RoomRepository
import com.roomify.app.databinding.ActivityStudentHomeBinding
import com.roomify.app.ui.common.LogoutHandler
import com.roomify.app.ui.common.RoleSessionGuard

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class StudentHomeActivity :
    AppCompatActivity() {

    private lateinit var binding:
            ActivityStudentHomeBinding

    private lateinit var roomRepository:
            RoomRepository

    private var isLoadingRooms =
        false

    private val roomAdapter by lazy {

        StudentRoomAdapter(
            horizontal = true,
            onRoomClick = { room ->

                openRoomDetail(
                    room.roomId
                )
            }
        )
    }

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        binding =
            ActivityStudentHomeBinding.inflate(
                layoutInflater
            )

        setContentView(
            binding.root
        )

        setupInsets()
        setupRooms()
        setupNavigation()
        setupActions()
        startAutoRefreshRooms()
    }

    // =========================================================
    // INSETS
    // =========================================================

    private fun setupInsets() {

        ViewCompat.setOnApplyWindowInsetsListener(
            binding.studentHomeRoot
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

    // =========================================================
    // ROOM SECTION
    // =========================================================

    private fun setupRooms() {

        RetrofitClient.initialize(
            applicationContext
        )

        roomRepository =
            RoomRepository(
                RetrofitClient.createService(
                    RoomApi::class.java
                )
            )

        binding.rvRooms.apply {

            layoutManager =
                LinearLayoutManager(
                    this@StudentHomeActivity,
                    LinearLayoutManager.HORIZONTAL,
                    false
                )

            adapter =
                roomAdapter

            isNestedScrollingEnabled =
                false
        }

        binding.btnRetryRooms
            .setOnClickListener {

                loadNewestRooms()
            }

        binding.btnRefreshRooms
            .setOnClickListener {

                loadNewestRooms()
            }

        loadNewestRooms()
    }

    // =========================================================
    // NAVIGATION
    // =========================================================

    private fun setupNavigation() {

        StudentBottomNavHelper.setup(
            activity = this,
            bottomNavigation =
                binding.bottomNavigation,
            selectedItemId =
                R.id.navHome
        )

        binding.cardSearch
            .setOnClickListener {

                openRoomSearch()
            }

        binding.btnSeeAllRooms
            .setOnClickListener {

                openRoomSearch()
            }

        binding.btnRoomMap
            .setOnClickListener {

                startActivity(
                    Intent(
                        this,
                        RoomMapActivity::class.java
                    )
                )
            }

        binding.btnNotifications
            .setOnClickListener {

                startActivity(
                    Intent(
                        this,
                        NotificationsActivity::class.java
                    )
                )
            }
    }

    // =========================================================
    // ACTIONS
    // =========================================================

    private fun setupActions() {

        binding.btnLogout
            .setOnClickListener {

                LogoutHandler.logout(
                    activity = this,
                    logoutButton =
                        binding.btnLogout
                )
            }
    }

    // =========================================================
    // OPEN SCREENS
    // =========================================================

    private fun openRoomSearch() {

        startActivity(
            Intent(
                this,
                RoomSearchActivity::class.java
            )
        )
    }

    private fun openRoomDetail(
        roomId: Long
    ) {

        startActivity(
            Intent(
                this,
                RoomDetailActivity::class.java
            ).apply {

                putExtra(
                    RoomDetailActivity.EXTRA_ROOM_ID,
                    roomId
                )
            }
        )
    }

    // =========================================================
    // LOAD ROOMS
    // =========================================================

    private fun loadNewestRooms(
        showSpinner: Boolean = true
    ) {

        if (isLoadingRooms) {
            return
        }

        isLoadingRooms =
            true

        val hasOldRooms =
            roomAdapter.currentList
                .isNotEmpty()

        binding.btnRefreshRooms.isEnabled =
            false

        binding.btnRetryRooms.isEnabled =
            false

        if (
            showSpinner &&
            !hasOldRooms
        ) {

            binding.progressRooms.isVisible =
                true

            binding.cardRoomEmpty.isVisible =
                false
        }

        lifecycleScope.launch {

            try {

                when (
                    val result =
                        roomRepository.getNewestRooms(
                            page = 1,
                            pageSize = 6
                        )
                ) {

                    is ApiResult.Success -> {

                        val rooms =
                            result.data
                                ?.items
                                .orEmpty()

                        roomAdapter.submitList(
                            rooms
                        )

                        binding.tvConnectionWarning
                            .isVisible =
                            false

                        binding.btnRetryRooms
                            .isVisible =
                            false

                        if (rooms.isEmpty()) {

                            binding.rvRooms.isVisible =
                                false

                            binding.cardRoomEmpty.isVisible =
                                true

                            binding.tvRoomState.text =
                                "Hiện chưa có phòng được đăng công khai."

                        } else {

                            binding.rvRooms.isVisible =
                                true

                            binding.cardRoomEmpty.isVisible =
                                false
                        }
                    }

                    is ApiResult.Error -> {

                        showRoomsError(
                            "Không thể kết nối Roomify.API. Vui lòng thử lại."
                        )
                    }

                    ApiResult.Loading ->
                        Unit
                }

            } catch (
                exception: CancellationException
            ) {

                throw exception

            } catch (
                exception: Exception
            ) {

                showRoomsError(
                    "Đã xảy ra lỗi khi tải danh sách phòng."
                )

            } finally {

                isLoadingRooms =
                    false

                binding.progressRooms.isVisible =
                    false

                binding.btnRefreshRooms.isEnabled =
                    true

                binding.btnRetryRooms.isEnabled =
                    true
            }
        }
    }

    private fun showRoomsError(
        message: String
    ) {

        val hasOldRooms =
            roomAdapter.currentList
                .isNotEmpty()

        if (hasOldRooms) {

            binding.rvRooms.isVisible =
                true

            binding.cardRoomEmpty.isVisible =
                false

            binding.tvConnectionWarning.text =
                "$message\nĐang hiển thị dữ liệu đã tải trước đó."

            binding.tvConnectionWarning.isVisible =
                true

        } else {

            binding.rvRooms.isVisible =
                false

            binding.tvConnectionWarning.isVisible =
                false

            binding.cardRoomEmpty.isVisible =
                true

            binding.tvRoomState.text =
                message

            binding.btnRetryRooms.isVisible =
                true
        }
    }

    // =========================================================
    // AUTO REFRESH
    // =========================================================

    private fun startAutoRefreshRooms() {

        lifecycleScope.launch {

            repeatOnLifecycle(
                Lifecycle.State.STARTED
            ) {

                while (true) {

                    delay(
                        15_000L
                    )

                    loadNewestRooms(
                        showSpinner = false
                    )
                }
            }
        }
    }

    // =========================================================
    // SESSION
    // =========================================================

    override fun onStart() {
        super.onStart()

        RoleSessionGuard.verify(
            activity = this,
            expectedRole = "STUDENT",
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