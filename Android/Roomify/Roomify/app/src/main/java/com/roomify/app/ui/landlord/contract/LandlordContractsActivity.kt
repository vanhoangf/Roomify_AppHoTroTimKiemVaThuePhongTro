package com.roomify.app.ui.landlord.contract

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
import com.roomify.app.data.remote.api.LandlordRentalContractApi
import com.roomify.app.data.remote.common.ApiResult
import com.roomify.app.data.remote.dto.contracts.RentalContractResponse
import com.roomify.app.data.remote.network.RetrofitClient
import com.roomify.app.data.repository.LandlordRentalContractRepository
import com.roomify.app.databinding.ActivityLandlordContractsBinding
import com.roomify.app.ui.common.RoleSessionGuard

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

class LandlordContractsActivity :
    AppCompatActivity() {

    private lateinit var binding:
            ActivityLandlordContractsBinding

    private lateinit var repository:
            LandlordRentalContractRepository

    private val items =
        mutableListOf<RentalContractResponse>()

    private val adapter by lazy {

        LandlordContractAdapter {
                contract ->

            startActivity(
                Intent(
                    this,
                    LandlordContractDetailActivity::class.java
                ).apply {

                    putExtra(
                        LandlordContractDetailActivity
                            .EXTRA_CONTRACT_ID,
                        contract.contractId
                    )
                }
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
            ActivityLandlordContractsBinding
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

    private fun setupInsets() {

        ViewCompat.setOnApplyWindowInsetsListener(
            binding.landlordContractsRoot
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
            LandlordRentalContractRepository(
                RetrofitClient.createService(
                    LandlordRentalContractApi::class.java
                )
            )
    }

    private fun setupRecyclerView() {

        binding.rvContracts.apply {

            layoutManager =
                LinearLayoutManager(
                    this@LandlordContractsActivity
                )

            adapter =
                this@LandlordContractsActivity
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

                loadContracts(
                    reset = true
                )
            }

        binding.btnLoadMore
            .setOnClickListener {

                if (
                    hasNextPage &&
                    !isLoading
                ) {

                    loadContracts(
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

                val checkedId =
                    checkedIds.firstOrNull()
                        ?: R.id.chipAll

                selectedStatus =
                    when (checkedId) {

                        R.id.chipPending ->
                            "PENDING"

                        R.id.chipActive ->
                            "ACTIVE"

                        R.id.chipCompleted ->
                            "COMPLETED"

                        R.id.chipCancelled ->
                            "CANCELLED"

                        else ->
                            null
                    }

                loadContracts(
                    reset = true
                )
            }
    }

    private fun loadContracts(
        reset: Boolean
    ) {

        if (isLoading) {
            return
        }

        if (reset) {

            currentPage =
                1

            items.clear()

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
                        repository.getContracts(
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

                        if (data == null) {

                            showError(
                                "Không nhận được dữ liệu hợp đồng."
                            )

                            return@launch
                        }

                        items.addAll(
                            data.items
                        )

                        hasNextPage =
                            data.hasNextPage

                        updateCount(
                            data.totalCount
                        )

                        if (
                            items.isEmpty()
                        ) {

                            showEmpty()

                        } else {

                            adapter.submitList(
                                items.toList()
                            )

                            binding.progressContracts
                                .isVisible =
                                false

                            binding.emptyContainer
                                .isVisible =
                                false

                            binding.errorContainer
                                .isVisible =
                                false

                            binding.rvContracts
                                .isVisible =
                                true

                            if (
                                hasNextPage
                            ) {

                                currentPage++
                            }
                        }
                    }

                    is ApiResult.Error -> {

                        if (
                            items.isEmpty()
                        ) {

                            showError(
                                result.exception.message
                                    ?: "Không thể tải hợp đồng."
                            )

                        } else {

                            Toast.makeText(
                                this@LandlordContractsActivity,
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

                showError(
                    e.message
                        ?: "Không thể tải hợp đồng."
                )

            } finally {

                isLoading =
                    false

                binding.btnLoadMore.isEnabled =
                    true

                binding.btnLoadMore.text =
                    "Xem thêm"

                binding.btnLoadMore.isVisible =
                    items.isNotEmpty() &&
                            hasNextPage
            }
        }
    }

    private fun updateCount(
        count: Int
    ) {

        binding.tvContractCount.text =
            when (selectedStatus) {

                "PENDING" ->
                    "$count hợp đồng chờ xác nhận"

                "ACTIVE" ->
                    "$count hợp đồng đang hiệu lực"

                "COMPLETED" ->
                    "$count hợp đồng đã hoàn thành"

                "CANCELLED" ->
                    "$count hợp đồng đã hủy"

                else ->
                    "$count hợp đồng"
            }
    }

    private fun showLoading() {

        binding.progressContracts.isVisible =
            true

        binding.rvContracts.isVisible =
            false

        binding.emptyContainer.isVisible =
            false

        binding.errorContainer.isVisible =
            false

        binding.btnLoadMore.isVisible =
            false
    }

    private fun showEmpty() {

        binding.progressContracts.isVisible =
            false

        binding.rvContracts.isVisible =
            false

        binding.emptyContainer.isVisible =
            true

        binding.errorContainer.isVisible =
            false

        binding.btnLoadMore.isVisible =
            false

        binding.tvEmptyMessage.text =
            "Không có hợp đồng phù hợp."

        adapter.submitList(
            emptyList()
        )
    }

    private fun showError(
        message: String
    ) {

        binding.progressContracts.isVisible =
            false

        binding.rvContracts.isVisible =
            false

        binding.emptyContainer.isVisible =
            false

        binding.errorContainer.isVisible =
            true

        binding.btnLoadMore.isVisible =
            false

        binding.tvErrorMessage.text =
            message

        binding.tvContractCount.text =
            "Không thể tải hợp đồng"
    }

    override fun onResume() {
        super.onResume()

        if (
            ::repository.isInitialized
        ) {

            loadContracts(
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