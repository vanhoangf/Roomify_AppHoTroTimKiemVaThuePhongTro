
package com.roomify.app.ui.student

import android.os.Bundle
import android.widget.Toast

import androidx.appcompat.app.AppCompatActivity

import com.roomify.app.databinding.ActivityStudentHomeBinding
import com.roomify.app.ui.common.LogoutHandler
import com.roomify.app.ui.common.RoleSessionGuard
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager

import com.roomify.app.data.remote.api.RoomApi
import com.roomify.app.data.remote.common.ApiResult
import com.roomify.app.data.remote.network.RetrofitClient
import com.roomify.app.data.repository.RoomRepository

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.delay

import android.content.Intent
import androidx.core.view.isVisible
import com.roomify.app.R

class StudentHomeActivity : AppCompatActivity() {


    private fun openRoomSearch() {

        val intent = Intent(
            this,
            RoomSearchActivity::class.java
        )

        startActivity(intent)
    }

    private fun setupRoomSearchNavigation() {

        // Thanh tìm kiếm trên Student Home.
        binding.cardSearch.isEnabled = true

        binding.cardSearch.setOnClickListener {
            openRoomSearch()
        }

        // Nút Xem tất cả.
        binding.btnSeeAllRooms.isVisible = true
        binding.btnSeeAllRooms.isEnabled = true

        binding.btnSeeAllRooms.setOnClickListener {
            openRoomSearch()
        }

        // Nút Bản đồ.
// Màn hình Map thật sẽ triển khai ở bước riêng.
        binding.btnRoomMap.setOnClickListener {

            Toast.makeText(
                this,
                "Bản đồ phòng trọ sẽ được phát triển ở bước tiếp theo.",
                Toast.LENGTH_SHORT
            ).show()
        }

        // Bật tab Tìm phòng.
        binding.bottomNavigation.menu
            .findItem(R.id.navSearch)
            ?.isEnabled = true

        binding.bottomNavigation.setOnItemSelectedListener {
                item ->

            when (item.itemId) {

                R.id.navHome -> true

                R.id.navSearch -> {
                    openRoomSearch()

                    // Giữ tab Trang chủ được chọn vì
                    // Search là một Activity riêng.
                    false
                }

                else -> false
            }
        }
    }

    private fun startAutoRefreshRooms() {

        lifecycleScope.launch {

            repeatOnLifecycle(Lifecycle.State.STARTED) {

                while (true) {

                    // Kiểm tra lại API sau mỗi 15 giây
                    delay(15_000L)

                    loadNewestRooms(showSpinner = false)
                }
            }
        }
    }

    private lateinit var roomRepository: RoomRepository

    private val roomAdapter by lazy {
        StudentRoomAdapter(
            horizontal = true
        )
    }

    private var isLoadingRooms = false


    private fun setupRoomsSection() {

        // Dùng lại RetrofitClient từ Android Core
        RetrofitClient.initialize(applicationContext)

        val roomApi = RetrofitClient.createService(
            RoomApi::class.java
        )

        roomRepository = RoomRepository(roomApi)

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


        binding.btnRetryRooms.setOnClickListener {
            loadNewestRooms()
        }

        binding.btnRefreshRooms.setOnClickListener {
            loadNewestRooms()
        }


        // Tải dữ liệu ngay khi mở trang chủ
        loadNewestRooms()
    }



    private fun loadNewestRooms(
        showSpinner: Boolean = true
    ) {

        if (isLoadingRooms) return

        isLoadingRooms = true

        val hasOldRooms =
            roomAdapter.currentList.isNotEmpty()

        binding.btnRefreshRooms.isEnabled = false
        binding.btnRetryRooms.isEnabled = false

        // Chỉ hiện loading lớn khi chưa có dữ liệu cũ.
        if (showSpinner && !hasOldRooms) {
            binding.progressRooms.isVisible = true
            binding.cardRoomEmpty.isVisible = false
        }

        lifecycleScope.launch {

            try {

                val result = roomRepository.getNewestRooms(
                    page = 1,
                    pageSize = 6
                )

                when (result) {

                    is ApiResult.Success -> {

                        val rooms = result.data
                            ?.items
                            .orEmpty()

                        roomAdapter.submitList(rooms)

                        // Kết nối đã hoạt động trở lại.
                        binding.tvConnectionWarning.isVisible = false
                        binding.btnRetryRooms.isVisible = false

                        if (rooms.isEmpty()) {

                            binding.rvRooms.isVisible = false
                            binding.cardRoomEmpty.isVisible = true

                            binding.tvRoomState.text =
                                "Hiện chưa có phòng được đăng công khai."

                        } else {

                            binding.rvRooms.isVisible = true
                            binding.cardRoomEmpty.isVisible = false
                        }
                    }

                    is ApiResult.Error -> {

                        showRoomsError(
                            "Không thể kết nối Roomify.API. " +
                                    "Vui lòng thử lại."
                        )
                    }

                    ApiResult.Loading -> Unit
                }

            } catch (e: CancellationException) {
                throw e

            } catch (_: Exception) {

                showRoomsError(
                    "Đã xảy ra lỗi khi tải danh sách phòng."
                )

            } finally {

                isLoadingRooms = false

                binding.progressRooms.isVisible = false
                binding.btnRefreshRooms.isEnabled = true
                binding.btnRetryRooms.isEnabled = true
            }
        }
    }



    private fun showRoomsError(message: String) {

        val hasOldRooms =
            roomAdapter.currentList.isNotEmpty()

        if (hasOldRooms) {

            // Giữ các phòng đã tải trước đó.
            binding.rvRooms.isVisible = true
            binding.cardRoomEmpty.isVisible = false

            binding.tvConnectionWarning.text =
                "$message\nĐang hiển thị dữ liệu đã tải trước đó."

            binding.tvConnectionWarning.isVisible = true

        } else {

            // Chưa từng tải được phòng.
            binding.rvRooms.isVisible = false
            binding.tvConnectionWarning.isVisible = false

            binding.cardRoomEmpty.isVisible = true
            binding.tvRoomState.text = message
            binding.btnRetryRooms.isVisible = true
        }
    }



    private lateinit var binding: ActivityStudentHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityStudentHomeBinding.inflate(
            layoutInflater
        )

        setContentView(binding.root)
        setupRoomsSection()
        setupRoomSearchNavigation()
        startAutoRefreshRooms()

        ViewCompat.setOnApplyWindowInsetsListener(
            binding.studentHomeRoot
        ) { view, insets ->

            val systemBars = insets.getInsets(
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


        // Root đã xử lý systemBars.bottom. Không để Material
        // BottomNavigationView tự cộng thêm inset lần thứ hai.
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

        // NÚT ĐĂNG XUẤT
        binding.btnLogout.setOnClickListener {

            LogoutHandler.logout(
                activity = this,
                logoutButton = binding.btnLogout
            )
        }
    }

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
