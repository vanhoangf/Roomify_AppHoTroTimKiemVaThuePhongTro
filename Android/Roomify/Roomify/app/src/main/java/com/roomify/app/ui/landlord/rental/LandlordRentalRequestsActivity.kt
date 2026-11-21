package com.roomify.app.ui.landlord.rental

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
import com.roomify.app.data.remote.api.LandlordRentalRequestApi
import com.roomify.app.data.remote.common.ApiResult
import com.roomify.app.data.remote.dto.rentals.RentalRequestResponse
import com.roomify.app.data.remote.network.RetrofitClient
import com.roomify.app.data.repository.LandlordRentalRequestRepository
import com.roomify.app.databinding.ActivityLandlordRentalRequestsBinding
import com.roomify.app.ui.common.RoleSessionGuard

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

class LandlordRentalRequestsActivity :
    AppCompatActivity() {

    private lateinit var binding:
            ActivityLandlordRentalRequestsBinding

    private lateinit var repository:
            LandlordRentalRequestRepository

    private val loadedRequests =
        mutableListOf<RentalRequestResponse>()

    private val adapter by lazy {

        LandlordRentalRequestAdapter {
                request ->

            openRequestDetail(
                request
            )
        }
    }

    private var selectedStatus:
            String? =
        null

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
            ActivityLandlordRentalRequestsBinding
                .inflate(
                    layoutInflater
                )

        setContentView(
            binding.root
        )

        setupInsets()
        setupRepository()
        setupRecyclerView()
        setupActions()
        setupFilters()
    }

    // ============================================================
    // INSETS
    // ============================================================

    private fun setupInsets() {

        ViewCompat.setOnApplyWindowInsetsListener(
            binding.landlordRentalRequestsRoot
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

    // ============================================================
    // REPOSITORY
    // ============================================================

    private fun setupRepository() {

        RetrofitClient.initialize(
            applicationContext
        )

        repository =
            LandlordRentalRequestRepository(
                RetrofitClient.createService(
                    LandlordRentalRequestApi::class.java
                )
            )
    }

    // ============================================================
    // RECYCLER VIEW
    // ============================================================

    private fun setupRecyclerView() {

        binding.rvRentalRequests.apply {

            layoutManager =
                LinearLayoutManager(
                    this@LandlordRentalRequestsActivity
                )

            adapter =
                this@LandlordRentalRequestsActivity
                    .adapter
        }
    }

    // ============================================================
    // ACTIONS
    // ============================================================

    private fun setupActions() {

        binding.btnBack
            .setOnClickListener {

                finish()
            }

        binding.btnRetry
            .setOnClickListener {

                loadRequests(
                    reset = true
                )
            }

        binding.btnLoadMore
            .setOnClickListener {

                if (
                    hasNextPage &&
                    !isLoading
                ) {

                    loadRequests(
                        reset = false
                    )
                }
            }
    }

    // ============================================================
    // DETAIL
    // ============================================================

    private fun openRequestDetail(
        request:
        RentalRequestResponse
    ) {

        startActivity(
            Intent(
                this,
                LandlordRentalRequestDetailActivity::class.java
            ).apply {

                putExtra(
                    LandlordRentalRequestDetailActivity
                        .EXTRA_RENTAL_REQUEST_ID,
                    request.rentalRequestId
                )
            }
        )
    }

    // ============================================================
    // FILTER
    // ============================================================

    private fun setupFilters() {

        binding.chipGroupStatus
            .setOnCheckedStateChangeListener {
                    _,
                    checkedIds ->

                val checkedId =
                    checkedIds.firstOrNull()
                        ?: R.id.chipAll

                selectedStatus =
                    when (checkedId) {

                        R.id.chipPending ->
                            "PENDING"

                        R.id.chipApproved ->
                            "APPROVED"

                        R.id.chipRejected ->
                            "REJECTED"

                        R.id.chipCancelled ->
                            "CANCELLED"

                        else ->
                            null
                    }

                loadRequests(
                    reset = true
                )
            }
    }

    // ============================================================
    // LOAD
    // ============================================================

    private fun loadRequests(
        reset: Boolean
    ) {

        if (isLoading) {
            return
        }

        if (reset) {

            currentPage =
                1

            loadedRequests.clear()

            showLoading()

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
                        repository.getRentalRequests(
                            status =
                                selectedStatus,
                            page =
                                currentPage,
                            pageSize =
                                20
                        )
                ) {

                    is ApiResult.Success -> {

                        val data =
                            result.data

                        if (
                            data == null
                        ) {

                            showError(
                                "Không nhận được dữ liệu yêu cầu thuê."
                            )

                            return@launch
                        }

                        loadedRequests.addAll(
                            data.items
                        )

                        hasNextPage =
                            data.hasNextPage

                        updateRequestCount(
                            data.totalCount
                        )

                        if (
                            loadedRequests.isEmpty()
                        ) {

                            showEmpty()

                        } else {

                            showRequests()

                            if (
                                hasNextPage
                            ) {

                                currentPage++
                            }
                        }
                    }

                    is ApiResult.Error -> {

                        if (
                            loadedRequests.isEmpty()
                        ) {

                            showError(
                                result.exception.message
                                    ?: "Không thể tải yêu cầu thuê."
                            )

                        } else {

                            Toast.makeText(
                                this@LandlordRentalRequestsActivity,
                                result.exception.message
                                    ?: "Không thể tải thêm dữ liệu.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }

                    ApiResult.Loading -> {
                        // Không cần xử lý thêm.
                    }
                }

            } catch (
                e: CancellationException
            ) {

                throw e

            } catch (
                e: Exception
            ) {

                if (
                    loadedRequests.isEmpty()
                ) {

                    showError(
                        e.message
                            ?: "Không thể tải yêu cầu thuê."
                    )

                } else {

                    Toast.makeText(
                        this@LandlordRentalRequestsActivity,
                        e.message
                            ?: "Không thể tải thêm dữ liệu.",
                        Toast.LENGTH_LONG
                    ).show()
                }

            } finally {

                isLoading =
                    false

                binding.btnLoadMore.isEnabled =
                    true

                binding.btnLoadMore.text =
                    "Xem thêm"

                binding.btnLoadMore.isVisible =
                    loadedRequests.isNotEmpty() &&
                            hasNextPage
            }
        }
    }

    // ============================================================
    // COUNT
    // ============================================================

    private fun updateRequestCount(
        totalCount: Int
    ) {

        binding.tvRequestCount.text =
            when (selectedStatus) {

                "PENDING" ->
                    "$totalCount yêu cầu đang chờ xử lý"

                "APPROVED" ->
                    "$totalCount yêu cầu đã duyệt"

                "REJECTED" ->
                    "$totalCount yêu cầu đã từ chối"

                "CANCELLED" ->
                    "$totalCount yêu cầu đã hủy"

                else ->
                    "$totalCount yêu cầu thuê"
            }
    }

    // ============================================================
    // STATES
    // ============================================================

    private fun showLoading() {

        binding.progressRequests
            .isVisible =
            true

        binding.rvRentalRequests
            .isVisible =
            false

        binding.emptyContainer
            .isVisible =
            false

        binding.errorContainer
            .isVisible =
            false

        binding.btnLoadMore
            .isVisible =
            false
    }

    private fun showRequests() {

        binding.progressRequests
            .isVisible =
            false

        binding.emptyContainer
            .isVisible =
            false

        binding.errorContainer
            .isVisible =
            false

        binding.rvRentalRequests
            .isVisible =
            true

        adapter.submitList(
            loadedRequests.toList()
        )

        binding.btnLoadMore
            .isVisible =
            hasNextPage
    }

    private fun showEmpty() {

        binding.progressRequests
            .isVisible =
            false

        binding.rvRentalRequests
            .isVisible =
            false

        binding.errorContainer
            .isVisible =
            false

        binding.emptyContainer
            .isVisible =
            true

        binding.btnLoadMore
            .isVisible =
            false

        binding.tvEmptyMessage.text =
            when (selectedStatus) {

                "PENDING" ->
                    "Không có yêu cầu nào đang chờ xử lý."

                "APPROVED" ->
                    "Chưa có yêu cầu nào được duyệt."

                "REJECTED" ->
                    "Chưa có yêu cầu nào bị từ chối."

                "CANCELLED" ->
                    "Chưa có yêu cầu nào đã hủy."

                else ->
                    "Yêu cầu thuê từ sinh viên sẽ xuất hiện tại đây."
            }

        adapter.submitList(
            emptyList()
        )
    }

    private fun showError(
        message: String
    ) {

        binding.progressRequests
            .isVisible =
            false

        binding.rvRentalRequests
            .isVisible =
            false

        binding.emptyContainer
            .isVisible =
            false

        binding.errorContainer
            .isVisible =
            true

        binding.btnLoadMore
            .isVisible =
            false

        binding.tvErrorMessage.text =
            message

        binding.tvRequestCount.text =
            "Không thể tải yêu cầu thuê"
    }

    // ============================================================
    // LIFECYCLE
    // ============================================================

    override fun onResume() {
        super.onResume()

        if (
            ::repository.isInitialized
        ) {

            loadRequests(
                reset = true
            )
        }
    }

    override fun onStart() {
        super.onStart()

        RoleSessionGuard.verify(
            activity =
                this,
            expectedRole =
                "LANDLORD",
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