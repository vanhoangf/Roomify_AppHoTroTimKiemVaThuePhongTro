package com.roomify.app.ui.landlord.room

import android.os.Bundle
import android.widget.Toast
import android.content.Intent

import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager

import com.roomify.app.data.remote.api.LandlordRoomApi
import com.roomify.app.data.remote.common.ApiResult
import com.roomify.app.data.remote.dto.landlord.RoomResponse
import com.roomify.app.data.remote.network.RetrofitClient
import com.roomify.app.data.repository.LandlordRoomRepository
import com.roomify.app.databinding.ActivityLandlordRoomsBinding
import com.roomify.app.ui.common.RoleSessionGuard

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

class LandlordRoomsActivity : AppCompatActivity() {

    companion object {

        const val EXTRA_PROPERTY_ID =
            "extra_property_id"

        const val EXTRA_PROPERTY_NAME =
            "extra_property_name"
    }

    private lateinit var binding:
            ActivityLandlordRoomsBinding

    private lateinit var repository:
            LandlordRoomRepository

    private var propertyId =
        0L

    private var propertyName =
        ""

    private var isLoading =
        false

    private val roomAdapter by lazy {

        LandlordRoomAdapter { room ->

            startActivity(
                Intent(
                    this,
                    LandlordRoomDetailActivity::class.java
                ).apply {

                    putExtra(
                        LandlordRoomDetailActivity.EXTRA_ROOM_ID,
                        room.roomId
                    )
                }
            )
        }
    }

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        binding =
            ActivityLandlordRoomsBinding.inflate(
                layoutInflater
            )

        setContentView(
            binding.root
        )

        propertyId =
            intent.getLongExtra(
                EXTRA_PROPERTY_ID,
                0L
            )

        propertyName =
            intent.getStringExtra(
                EXTRA_PROPERTY_NAME
            )
                ?.trim()
                .orEmpty()

        if (propertyId <= 0L) {

            Toast.makeText(
                this,
                "Không tìm thấy khu trọ.",
                Toast.LENGTH_SHORT
            ).show()

            finish()
            return
        }

        setupInsets()
        setupRepository()
        setupRecyclerView()
        setupActions()

        binding.tvPropertyName.text =
            propertyName.ifBlank {
                "Danh sách phòng của khu trọ"
            }
    }

    private fun setupInsets() {

        ViewCompat.setOnApplyWindowInsetsListener(
            binding.roomsRoot
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
            LandlordRoomRepository(
                RetrofitClient.createService(
                    LandlordRoomApi::class.java
                )
            )
    }

    private fun setupRecyclerView() {

        binding.rvRooms.apply {

            layoutManager =
                LinearLayoutManager(
                    this@LandlordRoomsActivity
                )

            adapter =
                roomAdapter
        }
    }

    private fun setupActions() {

        binding.btnBack
            .setOnClickListener {
                finish()
            }

        binding.btnRetryRooms
            .setOnClickListener {
                loadRooms()
            }

        binding.fabAddRoom
            .setOnClickListener {

                startActivity(
                    Intent(
                        this,
                        LandlordAddRoomActivity::class.java
                    ).apply {

                        putExtra(
                            LandlordAddRoomActivity.EXTRA_PROPERTY_ID,
                            propertyId
                        )

                        putExtra(
                            LandlordAddRoomActivity.EXTRA_PROPERTY_NAME,
                            propertyName
                        )
                    }
                )
            }
    }
    private fun loadRooms() {

        if (isLoading) {
            return
        }

        isLoading =
            true

        showLoading()

        lifecycleScope.launch {

            try {

                when (
                    val result =
                        repository.getRooms(
                            propertyId
                        )
                ) {

                    is ApiResult.Success -> {

                        val rooms =
                            result.data.orEmpty()

                        if (rooms.isEmpty()) {

                            showEmpty()

                        } else {

                            showRooms(
                                rooms
                            )
                        }
                    }

                    is ApiResult.Error -> {

                        showError(
                            result.exception.message
                                ?: "Không thể tải danh sách phòng."
                        )
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

                showError(
                    e.message
                        ?: "Không thể tải danh sách phòng."
                )

            } finally {

                isLoading =
                    false
            }
        }
    }

    private fun showLoading() {

        binding.progressRooms.isVisible =
            true

        binding.rvRooms.isVisible =
            false

        binding.emptyContainer.isVisible =
            false

        binding.errorContainer.isVisible =
            false

        binding.fabAddRoom.isVisible =
            false
    }

    private fun showRooms(
        rooms: List<RoomResponse>
    ) {

        binding.progressRooms.isVisible =
            false

        binding.emptyContainer.isVisible =
            false

        binding.errorContainer.isVisible =
            false

        binding.rvRooms.isVisible =
            true

        binding.fabAddRoom.isVisible =
            true

        roomAdapter.submitList(
            rooms
        )
    }

    private fun showEmpty() {

        binding.progressRooms.isVisible =
            false

        binding.rvRooms.isVisible =
            false

        binding.errorContainer.isVisible =
            false

        binding.emptyContainer.isVisible =
            true

        binding.fabAddRoom.isVisible =
            true
    }

    private fun showError(
        message: String
    ) {

        binding.progressRooms.isVisible =
            false

        binding.rvRooms.isVisible =
            false

        binding.emptyContainer.isVisible =
            false

        binding.errorContainer.isVisible =
            true

        binding.fabAddRoom.isVisible =
            false

        binding.tvErrorMessage.text =
            message
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

    override fun onResume() {
        super.onResume()

        if (
            ::repository.isInitialized &&
            propertyId > 0L
        ) {
            loadRooms()
        }
    }
}