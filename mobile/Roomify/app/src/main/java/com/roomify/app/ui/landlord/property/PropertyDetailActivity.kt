package com.roomify.app.ui.landlord.property

import android.os.Bundle
import android.widget.Toast
import android.content.Intent

import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope

import com.roomify.app.R
import com.roomify.app.data.remote.api.LandlordPropertyApi
import com.roomify.app.data.remote.common.ApiResult
import com.roomify.app.data.remote.dto.landlord.PropertyResponse
import com.roomify.app.data.remote.network.RetrofitClient
import com.roomify.app.data.repository.LandlordPropertyRepository
import com.roomify.app.databinding.ActivityPropertyDetailBinding
import com.roomify.app.ui.common.RoleSessionGuard
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.roomify.app.ui.landlord.room.LandlordRoomsActivity

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

class PropertyDetailActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_PROPERTY_ID =
            "extra_property_id"
    }

    private lateinit var binding:
            ActivityPropertyDetailBinding

    private lateinit var repository:
            LandlordPropertyRepository

    private var propertyId:
            Long = 0L

    private var isLoading =
        false

    private var currentStatus =
        "ACTIVE"

    private var isUpdatingStatus =
        false

    private var currentPropertyName =
        ""


    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        binding =
            ActivityPropertyDetailBinding.inflate(
                layoutInflater
            )

        setContentView(binding.root)

        propertyId =
            intent.getLongExtra(
                EXTRA_PROPERTY_ID,
                0L
            )

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
        setupActions()
    }

    // ========================================================
    // INSETS
    // ========================================================

    private fun setupInsets() {

        ViewCompat.setOnApplyWindowInsetsListener(
            binding.propertyDetailRoot
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

        repository =
            LandlordPropertyRepository(
                propertyApi =
                    RetrofitClient.createService(
                        LandlordPropertyApi::class.java
                    )
            )
    }

    // ========================================================
    // ACTIONS
    // ========================================================

    private fun setupActions() {

        binding.btnBack
            .setOnClickListener {
                finish()
            }

        binding.btnRetry
            .setOnClickListener {
                loadProperty()
            }

        binding.btnEditProperty
            .setOnClickListener {

                startActivity(
                    Intent(
                        this,
                        EditPropertyActivity::class.java
                    ).apply {

                        putExtra(
                            EditPropertyActivity.EXTRA_PROPERTY_ID,
                            propertyId
                        )
                    }
                )
            }

        binding.btnToggleStatus
            .setOnClickListener {

                confirmToggleStatus()
            }

        binding.btnManageRooms
            .setOnClickListener {

                startActivity(
                    Intent(
                        this,
                        LandlordRoomsActivity::class.java
                    ).apply {

                        putExtra(
                            LandlordRoomsActivity.EXTRA_PROPERTY_ID,
                            propertyId
                        )

                        putExtra(
                            LandlordRoomsActivity.EXTRA_PROPERTY_NAME,
                            currentPropertyName
                        )
                    }
                )
            }
    }

    private fun confirmToggleStatus() {

        if (isUpdatingStatus) {
            return
        }

        val currentlyActive =
            currentStatus.equals(
                "ACTIVE",
                ignoreCase = true
            )

        val newStatus =
            if (currentlyActive) {
                "INACTIVE"
            } else {
                "ACTIVE"
            }

        val title =
            if (currentlyActive) {
                "Ngừng hoạt động?"
            } else {
                "Bật hoạt động?"
            }

        val message =
            if (currentlyActive) {
                "Khu trọ sẽ chuyển sang trạng thái ngừng hoạt động."
            } else {
                "Khu trọ sẽ được kích hoạt trở lại."
            }

        MaterialAlertDialogBuilder(this)
            .setTitle(title)
            .setMessage(message)
            .setNegativeButton(
                "Hủy"
            ) { dialog, _ ->

                dialog.dismiss()
            }
            .setPositiveButton(
                "Xác nhận"
            ) { dialog, _ ->

                dialog.dismiss()

                updatePropertyStatus(
                    newStatus
                )
            }
            .show()
    }

    private fun updatePropertyStatus(
        status: String
    ) {

        if (isUpdatingStatus) {
            return
        }

        isUpdatingStatus =
            true

        binding.btnToggleStatus.isEnabled =
            false

        binding.btnToggleStatus.text =
            "Đang cập nhật..."

        lifecycleScope.launch {

            try {

                when (
                    val result =
                        repository
                            .updatePropertyStatus(
                                propertyId,
                                status
                            )
                ) {

                    is ApiResult.Success -> {

                        Toast.makeText(
                            this@PropertyDetailActivity,
                            if (
                                status.equals(
                                    "ACTIVE",
                                    ignoreCase = true
                                )
                            ) {
                                "Đã bật hoạt động khu trọ."
                            } else {
                                "Đã ngừng hoạt động khu trọ."
                            },
                            Toast.LENGTH_SHORT
                        ).show()

                        currentStatus =
                            status

                        loadProperty()
                    }

                    is ApiResult.Error -> {

                        Toast.makeText(
                            this@PropertyDetailActivity,
                            result.exception.message,
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
                    this@PropertyDetailActivity,
                    e.message
                        ?: "Không thể cập nhật trạng thái.",
                    Toast.LENGTH_LONG
                ).show()

            } finally {

                isUpdatingStatus =
                    false

                binding.btnToggleStatus.isEnabled =
                    true

                showStatus(
                    currentStatus
                )
            }
        }
    }

    // ========================================================
    // LOAD PROPERTY
    // ========================================================

    private fun loadProperty() {

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
                        repository.getProperty(
                            propertyId
                        )
                ) {

                    is ApiResult.Success -> {

                        val property =
                            result.data

                        if (property == null) {

                            showError(
                                "Không nhận được thông tin khu trọ."
                            )

                        } else {

                            showProperty(
                                property
                            )
                        }
                    }

                    is ApiResult.Error -> {

                        showError(
                            result.exception.message
                                ?: "Không thể tải thông tin khu trọ."
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
                        ?: "Đã xảy ra lỗi khi tải khu trọ."
                )

            } finally {

                isLoading =
                    false
            }
        }
    }

    // ========================================================
    // DISPLAY
    // ========================================================

    private fun showProperty(
        property: PropertyResponse
    ) {

        binding.progressDetail.isVisible =
            false

        binding.errorContainer.isVisible =
            false

        binding.scrollContent.isVisible =
            true

        currentStatus =
            property.status

        currentPropertyName =
            property.propertyName

        binding.tvPropertyName.text =
            property.propertyName

        binding.tvPropertyType.text =
            property.propertyTypeName

        binding.tvAddress.text =
            property.addressLine

        binding.tvWard.text =
            property.wardName

        binding.tvProvince.text =
            property.provinceName

        binding.tvRoomCount.text =
            "${property.roomCount} phòng"

        binding.tvDescription.text =
            property.description
                ?.takeIf {
                    it.isNotBlank()
                }
                ?: "Chưa có mô tả."

        binding.tvCreatedAt.text =
            formatDateTime(
                property.createdAt
            )

        binding.tvUpdatedAt.text =
            formatDateTime(
                property.updatedAt
            )

        showStatus(
            property.status
        )
    }

    private fun showStatus(
        status: String
    ) {

        val active =
            status.equals(
                "ACTIVE",
                ignoreCase = true
            )

        binding.tvStatus.text =
            if (active) {
                "Hoạt động"
            } else {
                "Ngừng hoạt động"
            }

        binding.tvStatus
            .setBackgroundResource(
                if (active) {
                    R.drawable
                        .bg_property_status_active
                } else {
                    R.drawable
                        .bg_property_status_inactive
                }
            )

        binding.tvStatus
            .setTextColor(
                ContextCompat.getColor(
                    this,
                    if (active) {
                        R.color.status_success
                    } else {
                        R.color.text_muted
                    }
                )
            )

        binding.btnToggleStatus.text =
            if (active) {
                "Ngừng hoạt động"
            } else {
                "Bật hoạt động"
            }
    }

    // ========================================================
    // DATE
    // ========================================================

    private fun formatDateTime(
        value: String
    ): String {

        if (value.isBlank()) {
            return "-"
        }

        /*
         * Backend trả ISO datetime.
         * Ví dụ:
         * 2026-11-20T14:05:20.123
         */

        val clean =
            value
                .replace(
                    "T",
                    " "
                )
                .substringBefore(".")

        return if (
            clean.length >= 16
        ) {
            clean.take(16)
        } else {
            clean
        }
    }

    // ========================================================
    // STATES
    // ========================================================

    private fun showLoading() {

        binding.progressDetail.isVisible =
            true

        binding.scrollContent.isVisible =
            false

        binding.errorContainer.isVisible =
            false
    }

    private fun showError(
        message: String
    ) {

        binding.progressDetail.isVisible =
            false

        binding.scrollContent.isVisible =
            false

        binding.errorContainer.isVisible =
            true

        binding.tvErrorMessage.text =
            message
    }

    // ========================================================
    // SESSION
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
    override fun onResume() {
        super.onResume()

        if (
            ::repository.isInitialized &&
            propertyId > 0L
        ) {
            loadProperty()
        }
    }
}