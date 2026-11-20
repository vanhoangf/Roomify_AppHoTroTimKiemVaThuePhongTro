package com.roomify.app.ui.landlord.property

import android.os.Bundle
import android.widget.Toast
import android.content.Intent
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

import kotlinx.coroutines.launch

class LandlordPropertiesActivity : AppCompatActivity() {

    private lateinit var binding:
            ActivityLandlordPropertiesBinding

    private lateinit var repository:
            LandlordPropertyRepository

    private var isLoading = false

    private val propertyAdapter by lazy {

        LandlordPropertyAdapter { property ->

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
    }

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        binding =
            ActivityLandlordPropertiesBinding.inflate(
                layoutInflater
            )

        setContentView(binding.root)

        setupInsets()
        setupRepository()
        setupRecyclerView()
        setupActions()
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

            adapter = propertyAdapter
        }
    }

    // ========================================================
    // ACTIONS
    // ========================================================

    private fun setupActions() {

        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnRetryProperties
            .setOnClickListener {
                loadProperties()
            }

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
    // LOAD DATA
    // ========================================================

    private fun loadProperties() {

        if (isLoading) {
            return
        }

        isLoading = true
        showLoading()

        lifecycleScope.launch {

            try {

                val result = kotlinx.coroutines.withTimeout(
                    12_000L
                ) {
                    repository.getProperties()
                }

                when (result) {

                    is ApiResult.Success -> {

                        val properties =
                            result.data.orEmpty()

                        if (properties.isEmpty()) {

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
                                ?: "Không thể kết nối đến máy chủ."
                        )
                    }

                    ApiResult.Loading -> Unit
                }

            } catch (
                e: kotlinx.coroutines.TimeoutCancellationException
            ) {

                showError(
                    "Không thể kết nối đến máy chủ. Vui lòng thử lại."
                )

            } catch (
                e: kotlinx.coroutines.CancellationException
            ) {

                throw e

            } catch (
                _: Exception
            ) {

                showError(
                    "Đã xảy ra lỗi khi tải danh sách khu trọ."
                )

            } finally {

                isLoading = false
            }
        }
    }

    // ========================================================
    // UI STATES
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
            true

        propertyAdapter.submitList(
            properties
        )
    }

    private fun showEmpty() {

        binding.progressProperties.isVisible =
            false

        binding.rvProperties.isVisible =
            false

        binding.emptyContainer.isVisible =
            true

        binding.fabAddProperty.isVisible =
            true

        binding.tvEmptyTitle.text =
            "Chưa có khu trọ"

        binding.tvEmptyMessage.text =
            "Bạn chưa tạo khu trọ nào trên Roomify."

        binding.btnRetryProperties.isVisible =
            false
    }

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
    override fun onResume() {
        super.onResume()

        if (::repository.isInitialized) {
            loadProperties()
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