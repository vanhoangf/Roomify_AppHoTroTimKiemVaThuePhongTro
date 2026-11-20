package com.roomify.app.ui.landlord.property

import android.content.Intent
import android.os.Bundle
import android.widget.Toast

import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager

import com.roomify.app.data.remote.api.LandlordPropertyApi
import com.roomify.app.data.remote.common.ApiResult
import com.roomify.app.data.remote.dto.landlord.PropertyResponse
import com.roomify.app.data.remote.network.RetrofitClient
import com.roomify.app.data.repository.LandlordPropertyRepository
import com.roomify.app.databinding.ActivityLandlordPropertiesBinding
import com.roomify.app.ui.common.RoleSessionGuard
import com.roomify.app.ui.landlord.room.LandlordRoomsActivity

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

class LandlordPropertiesActivity : AppCompatActivity() {

    companion object {

        const val EXTRA_SELECT_FOR_ROOMS =
            "extra_select_for_rooms"
    }

    private lateinit var binding:
            ActivityLandlordPropertiesBinding

    private lateinit var repository:
            LandlordPropertyRepository

    private var isLoading =
        false

    /*
     * false:
     * Khu trọ -> bấm item -> PropertyDetailActivity
     *
     * true:
     * Phòng trọ -> chọn khu trọ -> LandlordRoomsActivity
     */
    private var selectForRooms =
        false

    private val propertyAdapter by lazy {

        LandlordPropertyAdapter { property ->

            if (selectForRooms) {

                openRooms(
                    property
                )

            } else {

                openPropertyDetail(
                    property
                )
            }
        }
    }

    // ========================================================
    // ON CREATE
    // ========================================================

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        binding =
            ActivityLandlordPropertiesBinding.inflate(
                layoutInflater
            )

        setContentView(
            binding.root
        )

        selectForRooms =
            intent.getBooleanExtra(
                EXTRA_SELECT_FOR_ROOMS,
                false
            )

        setupInsets()
        setupRepository()
        setupRecyclerView()
        setupActions()
        setupScreenMode()
    }

    private fun setupScreenMode() {

        if (selectForRooms) {

            binding.tvPropertiesTitle.text =
                "Chọn khu trọ"

            binding.tvPropertiesSubtitle.text =
                "Chọn khu trọ để quản lý danh sách phòng."

        } else {

            binding.tvPropertiesTitle.text =
                "Khu trọ của bạn"

            binding.tvPropertiesSubtitle.text =
                "Quản lý các cơ sở cho thuê trên Roomify."
        }
    }
    // ========================================================
    // INSETS
    // ========================================================

    private fun setupInsets() {

        ViewCompat.setOnApplyWindowInsetsListener(
            binding.propertiesRoot
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

    // ========================================================
    // REPOSITORY
    // ========================================================

    private fun setupRepository() {

        RetrofitClient.initialize(
            applicationContext
        )

        val api =
            RetrofitClient.createService(
                LandlordPropertyApi::class.java
            )

        repository =
            LandlordPropertyRepository(
                propertyApi = api
            )
    }

    // ========================================================
    // RECYCLER VIEW
    // ========================================================

    private fun setupRecyclerView() {

        binding.rvProperties.apply {

            layoutManager =
                LinearLayoutManager(
                    this@LandlordPropertiesActivity
                )

            adapter =
                propertyAdapter
        }
    }

    // ========================================================
    // ACTIONS
    // ========================================================

    private fun setupActions() {

        // Back
        binding.btnBack
            .setOnClickListener {

                finish()
            }

        // Retry
        binding.btnRetryProperties
            .setOnClickListener {

                loadProperties()
            }

        // Add property
        binding.fabAddProperty
            .setOnClickListener {

                startActivity(
                    Intent(
                        this,
                        AddPropertyActivity::class.java
                    )
                )
            }
    }


    // ========================================================
    // OPEN PROPERTY DETAIL
    // ========================================================

    private fun openPropertyDetail(
        property: PropertyResponse
    ) {

        startActivity(
            Intent(
                this,
                PropertyDetailActivity::class.java
            ).apply {

                putExtra(
                    PropertyDetailActivity.EXTRA_PROPERTY_ID,
                    property.propertyId
                )
            }
        )
    }

    // ========================================================
    // OPEN ROOMS
    // ========================================================

    private fun openRooms(
        property: PropertyResponse
    ) {

        startActivity(
            Intent(
                this,
                LandlordRoomsActivity::class.java
            ).apply {

                putExtra(
                    LandlordRoomsActivity.EXTRA_PROPERTY_ID,
                    property.propertyId
                )

                putExtra(
                    LandlordRoomsActivity.EXTRA_PROPERTY_NAME,
                    property.propertyName
                )
            }
        )
    }

    // ========================================================
    // LOAD PROPERTIES
    // ========================================================

    private fun loadProperties() {

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
                        repository.getProperties()
                ) {

                    is ApiResult.Success -> {

                        val properties =
                            result.data.orEmpty()

                        if (
                            properties.isEmpty()
                        ) {

                            showEmpty()

                        } else {

                            showProperties(
                                properties
                            )
                        }
                    }

                    is ApiResult.Error -> {

                        showError(
                            result.exception.message
                                ?: "Không thể tải danh sách khu trọ."
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
                        ?: "Không thể tải danh sách khu trọ."
                )

            } finally {

                isLoading =
                    false
            }
        }
    }

    // ========================================================
    // UI - LOADING
    // ========================================================

    private fun showLoading() {

        binding.progressProperties.isVisible =
            true

        binding.rvProperties.isVisible =
            false

        binding.emptyContainer.isVisible =
            false

        binding.fabAddProperty.isVisible =
            false
    }

    // ========================================================
    // UI - LIST
    // ========================================================

    private fun showProperties(
        properties: List<PropertyResponse>
    ) {

        binding.progressProperties.isVisible =
            false

        binding.emptyContainer.isVisible =
            false

        binding.rvProperties.isVisible =
            true

        binding.fabAddProperty.isVisible =
            !selectForRooms

        propertyAdapter.submitList(
            properties
        )
    }

    // ========================================================
    // UI - EMPTY
    // ========================================================

    private fun showEmpty() {

        binding.progressProperties.isVisible =
            false

        binding.rvProperties.isVisible =
            false

        binding.emptyContainer.isVisible =
            true

        binding.btnRetryProperties.isVisible =
            false

        if (selectForRooms) {

            binding.tvEmptyTitle.text =
                "Chưa có khu trọ"

            binding.tvEmptyMessage.text =
                "Bạn cần tạo khu trọ trước khi có thể quản lý phòng."

            binding.fabAddProperty.isVisible =
                true

        } else {

            binding.tvEmptyTitle.text =
                "Chưa có khu trọ"

            binding.tvEmptyMessage.text =
                "Bạn chưa tạo khu trọ nào trên Roomify."

            binding.fabAddProperty.isVisible =
                true
        }
    }

    // ========================================================
    // UI - ERROR
    // ========================================================

    private fun showError(
        message: String
    ) {

        binding.progressProperties.isVisible =
            false

        binding.rvProperties.isVisible =
            false

        binding.emptyContainer.isVisible =
            true

        binding.fabAddProperty.isVisible =
            false

        binding.tvEmptyTitle.text =
            "Không thể tải khu trọ"

        binding.tvEmptyMessage.text =
            message

        binding.btnRetryProperties.isVisible =
            true
    }

    // ========================================================
    // SESSION GUARD
    // ========================================================

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

    // ========================================================
    // RELOAD
    // ========================================================

    override fun onResume() {
        super.onResume()

        if (
            ::repository.isInitialized
        ) {

            loadProperties()
        }
    }
}