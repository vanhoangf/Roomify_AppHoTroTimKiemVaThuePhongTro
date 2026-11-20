package com.roomify.app.ui.landlord.payment

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
import com.roomify.app.data.remote.api.LandlordPaymentApi
import com.roomify.app.data.remote.common.ApiResult
import com.roomify.app.data.remote.dto.payments.PaymentResponse
import com.roomify.app.data.remote.network.RetrofitClient
import com.roomify.app.data.repository.LandlordPaymentRepository
import com.roomify.app.databinding.ActivityLandlordPaymentsBinding
import com.roomify.app.ui.common.RoleSessionGuard

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

class LandlordPaymentsActivity :
    AppCompatActivity() {

    private lateinit var binding:
            ActivityLandlordPaymentsBinding

    private lateinit var repository:
            LandlordPaymentRepository

    private val loadedItems =
        mutableListOf<PaymentResponse>()

    private val adapter by lazy {

        LandlordPaymentAdapter {
                payment ->

            startActivity(
                Intent(
                    this,
                    LandlordPaymentDetailActivity::class.java
                ).apply {

                    putExtra(
                        LandlordPaymentDetailActivity
                            .EXTRA_PAYMENT_ID,
                        payment.paymentId
                    )
                }
            )
        }
    }

    private var selectedStatus:
            String? =
        null

    private var selectedType:
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
            ActivityLandlordPaymentsBinding.inflate(
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

    private fun setupInsets() {

        ViewCompat.setOnApplyWindowInsetsListener(
            binding.landlordPaymentsRoot
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
            LandlordPaymentRepository(
                RetrofitClient.createService(
                    LandlordPaymentApi::class.java
                )
            )
    }

    private fun setupRecyclerView() {

        binding.rvPayments.apply {

            layoutManager =
                LinearLayoutManager(
                    this@LandlordPaymentsActivity
                )

            adapter =
                this@LandlordPaymentsActivity
                    .adapter
        }
    }

    private fun setupActions() {

        binding.btnBack
            .setOnClickListener {
                finish()
            }

        binding.btnRetry
            .setOnClickListener {

                loadPayments(
                    reset = true
                )
            }

        binding.btnLoadMore
            .setOnClickListener {

                if (
                    hasNextPage &&
                    !isLoading
                ) {

                    loadPayments(
                        reset = false
                    )
                }
            }
    }

    private fun setupFilters() {

        binding.chipGroupStatus
            .setOnCheckedStateChangeListener {
                    _,
                    checkedIds ->

                selectedStatus =
                    when (
                        checkedIds.firstOrNull()
                    ) {

                        R.id.chipStatusPending ->
                            "PENDING"

                        R.id.chipStatusPaid ->
                            "PAID"

                        R.id.chipStatusFailed ->
                            "FAILED"

                        R.id.chipStatusCancelled ->
                            "CANCELLED"

                        else ->
                            null
                    }

                loadPayments(
                    reset = true
                )
            }

        binding.chipGroupType
            .setOnCheckedStateChangeListener {
                    _,
                    checkedIds ->

                selectedType =
                    when (
                        checkedIds.firstOrNull()
                    ) {

                        R.id.chipTypeDeposit ->
                            "DEPOSIT"

                        R.id.chipTypeRent ->
                            "RENT"

                        R.id.chipTypeUtility ->
                            "UTILITY"

                        R.id.chipTypeOther ->
                            "OTHER"

                        else ->
                            null
                    }

                loadPayments(
                    reset = true
                )
            }
    }

    private fun loadPayments(
        reset: Boolean
    ) {

        if (isLoading) {
            return
        }

        if (reset) {

            currentPage =
                1

            loadedItems.clear()

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
                        repository.getPayments(
                            status =
                                selectedStatus,
                            paymentType =
                                selectedType,
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
                                "Không nhận được dữ liệu thanh toán."
                            )

                            return@launch
                        }

                        loadedItems.addAll(
                            data.items
                        )

                        hasNextPage =
                            data.hasNextPage

                        binding.tvPaymentCount.text =
                            "${data.totalCount} khoản thanh toán"

                        if (
                            loadedItems.isEmpty()
                        ) {

                            showEmpty()

                        } else {

                            adapter.submitList(
                                loadedItems.toList()
                            )

                            binding.progressPayments.isVisible =
                                false

                            binding.emptyContainer.isVisible =
                                false

                            binding.errorContainer.isVisible =
                                false

                            binding.rvPayments.isVisible =
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
                                    ?: "Không thể tải thanh toán."
                            )

                        } else {

                            Toast.makeText(
                                this@LandlordPaymentsActivity,
                                result.exception.message
                                    ?: "Không thể tải thêm dữ liệu.",
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
                            ?: "Không thể tải thanh toán."
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

    private fun showLoading() {

        binding.progressPayments.isVisible =
            true

        binding.rvPayments.isVisible =
            false

        binding.emptyContainer.isVisible =
            false

        binding.errorContainer.isVisible =
            false

        binding.btnLoadMore.isVisible =
            false
    }

    private fun showEmpty() {

        binding.progressPayments.isVisible =
            false

        binding.rvPayments.isVisible =
            false

        binding.emptyContainer.isVisible =
            true

        binding.errorContainer.isVisible =
            false

        binding.btnLoadMore.isVisible =
            false

        binding.tvEmptyMessage.text =
            "Không có khoản thanh toán phù hợp."

        adapter.submitList(
            emptyList()
        )
    }

    private fun showError(
        message: String
    ) {

        binding.progressPayments.isVisible =
            false

        binding.rvPayments.isVisible =
            false

        binding.emptyContainer.isVisible =
            false

        binding.errorContainer.isVisible =
            true

        binding.btnLoadMore.isVisible =
            false

        binding.tvErrorMessage.text =
            message

        binding.tvPaymentCount.text =
            "Không thể tải thanh toán"
    }

    override fun onResume() {
        super.onResume()

        if (
            ::repository.isInitialized
        ) {

            loadPayments(
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