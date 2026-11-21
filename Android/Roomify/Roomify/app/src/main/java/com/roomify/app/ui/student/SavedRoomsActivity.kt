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

import com.google.android.material.dialog.MaterialAlertDialogBuilder

import com.roomify.app.data.remote.api.SavedRoomApi
import com.roomify.app.data.remote.common.ApiResult
import com.roomify.app.data.remote.dto.savedrooms.SavedRoomResponse
import com.roomify.app.data.remote.network.RetrofitClient
import com.roomify.app.data.repository.SavedRoomRepository
import com.roomify.app.databinding.ActivitySavedRoomsBinding
import com.roomify.app.R
import kotlinx.coroutines.launch

class SavedRoomsActivity :
    AppCompatActivity() {

    private fun setupBottomNavigation() {

        StudentBottomNavHelper.setup(
            activity = this,
            bottomNavigation =
                binding.bottomNavigation,
            selectedItemId =
                R.id.navSaved
        )
    }
    private lateinit var binding:
            ActivitySavedRoomsBinding

    private lateinit var repository:
            SavedRoomRepository

    private lateinit var adapter:
            SavedRoomsAdapter

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        binding =
            ActivitySavedRoomsBinding.inflate(
                layoutInflater
            )

        setContentView(
            binding.root
        )

        StudentBottomNavHelper.setup(
            activity = this,
            bottomNavigation = binding.bottomNavigation,
            selectedItemId = R.id.navSaved
        )

        setupInsets()
        setupRepository()
        setupRecyclerView()
        setupListeners()
        setupBottomNavigation()

        loadSavedRooms()
    }

    override fun onResume() {
        super.onResume()

        if (
            ::repository.isInitialized &&
            ::binding.isInitialized
        ) {
            loadSavedRooms()
        }
    }

    private fun setupInsets() {

        ViewCompat.setOnApplyWindowInsetsListener(
            binding.savedRoomsRoot
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
         * Root đã cộng navigation bar bottom.
         * Không cho BottomNavigationView cộng thêm lần nữa.
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
            SavedRoomRepository(
                RetrofitClient.createService(
                    SavedRoomApi::class.java
                )
            )
    }

    private fun setupRecyclerView() {

        adapter =
            SavedRoomsAdapter(
                onDetail = {

                    startActivity(
                        Intent(
                            this,
                            RoomDetailActivity::class.java
                        ).apply {

                            putExtra(
                                RoomDetailActivity.EXTRA_ROOM_ID,
                                it.roomId
                            )
                        }
                    )
                },

                onRemove = {

                    confirmRemove(it)
                }
            )

        binding.rvSavedRooms.apply {

            layoutManager =
                LinearLayoutManager(
                    this@SavedRoomsActivity
                )

            adapter =
                this@SavedRoomsActivity.adapter
        }
    }

    private fun setupListeners() {


        binding.btnRetry
            .setOnClickListener {
                loadSavedRooms()
            }
    }

    private fun loadSavedRooms() {

        binding.progressSavedRooms
            .isVisible = true

        binding.rvSavedRooms
            .isVisible = false

        binding.stateContainer
            .isVisible = false

        lifecycleScope.launch {

            when (
                val result =
                    repository.getSavedRooms()
            ) {

                is ApiResult.Success -> {

                    val rooms =
                        result.data.orEmpty()

                    binding.progressSavedRooms
                        .isVisible = false

                    adapter.submitList(
                        rooms
                    )

                    binding.tvCount.text =
                        if (rooms.isEmpty()) {
                            "Bạn chưa lưu phòng nào"
                        } else {
                            "Bạn đã lưu ${rooms.size} phòng"
                        }

                    if (rooms.isEmpty()) {

                        binding.stateContainer
                            .isVisible = true

                        binding.tvStateMessage.text =
                            "Bạn chưa lưu phòng nào."

                        binding.btnRetry
                            .isVisible = false

                    } else {

                        binding.rvSavedRooms
                            .isVisible = true
                    }
                }

                is ApiResult.Error -> {

                    binding.progressSavedRooms
                        .isVisible = false

                    binding.stateContainer
                        .isVisible = true

                    binding.btnRetry
                        .isVisible = true

                    binding.tvStateMessage.text =
                        result.exception.message

                    binding.tvCount.text =
                        "Không thể tải phòng đã lưu"
                }

                ApiResult.Loading ->
                    Unit
            }
        }
    }

    private fun confirmRemove(
        room: SavedRoomResponse
    ) {

        MaterialAlertDialogBuilder(
            this
        )
            .setTitle(
                "Bỏ lưu phòng?"
            )
            .setMessage(
                "Bạn có muốn bỏ \"${room.title}\" khỏi danh sách đã lưu?"
            )
            .setNegativeButton(
                "Không",
                null
            )
            .setPositiveButton(
                "Bỏ lưu"
            ) { _, _ ->

                removeRoom(
                    room.roomId
                )
            }
            .show()
    }

    private fun removeRoom(
        roomId: Long
    ) {

        lifecycleScope.launch {

            when (
                val result =
                    repository.deleteSavedRoom(
                        roomId
                    )
            ) {

                is ApiResult.Success -> {

                    Toast.makeText(
                        this@SavedRoomsActivity,
                        "Đã bỏ lưu phòng.",
                        Toast.LENGTH_SHORT
                    ).show()

                    loadSavedRooms()
                }

                is ApiResult.Error -> {

                    Toast.makeText(
                        this@SavedRoomsActivity,
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