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
import com.roomify.app.data.remote.api.RentalRequestApi
import com.roomify.app.data.remote.common.ApiResult
import com.roomify.app.data.remote.dto.rentals.RentalRequestResponse
import com.roomify.app.data.remote.network.RetrofitClient
import com.roomify.app.data.repository.RentalRequestRepository
import com.roomify.app.databinding.ActivityMyRentalRequestsBinding

import kotlinx.coroutines.launch

class MyRentalRequestsActivity :
    AppCompatActivity() {

    private lateinit var binding:
            ActivityMyRentalRequestsBinding

    private lateinit var repository:
            RentalRequestRepository

    private lateinit var adapter:
            MyRentalRequestsAdapter

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(
            savedInstanceState
        )

        binding =
            ActivityMyRentalRequestsBinding
                .inflate(
                    layoutInflater
                )

        setContentView(
            binding.root
        )

        setupInsets()
        setupRepository()
        setupRecyclerView()
        setupListeners()

        loadRequests()
    }

    private fun setupInsets() {

        ViewCompat
            .setOnApplyWindowInsetsListener(
                binding.myRentalRequestsRoot
            ) { view, insets ->

                val bars =
                    insets.getInsets(
                        WindowInsetsCompat
                            .Type.systemBars()
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
            RentalRequestRepository(
                RetrofitClient.createService(
                    RentalRequestApi::class.java
                )
            )
    }

    private fun setupRecyclerView() {

        adapter =
            MyRentalRequestsAdapter {

                confirmCancel(
                    it
                )
            }

        binding.rvRentalRequests.apply {

            layoutManager =
                LinearLayoutManager(
                    this@MyRentalRequestsActivity
                )

            adapter =
                this@MyRentalRequestsActivity
                    .adapter
        }
    }

    private fun setupListeners() {
        binding.btnMyContracts
            .setOnClickListener {

                startActivity(
                    Intent(
                        this,
                        MyContractsActivity::class.java
                    )
                )
            }

        binding.btnBack
            .setOnClickListener {
                finish()
            }

        binding.btnRetry
            .setOnClickListener {
                loadRequests()
            }
    }

    private fun loadRequests() {

        showLoading()

        lifecycleScope.launch {

            when (
                val result =
                    repository
                        .getMyRentalRequests(
                            page = 1,
                            pageSize = 20
                        )
            ) {

                is ApiResult.Success -> {

                    val data =
                        result.data

                    val items =
                        data?.items.orEmpty()

                    adapter.submitList(
                        items
                    )

                    binding.progressRequests
                        .isVisible =
                        false

                    binding.tvRequestCount.text =
                        "Bạn có ${data?.totalCount ?: items.size} yêu cầu thuê"

                    if (
                        items.isEmpty()
                    ) {

                        showEmpty()

                    } else {

                        binding.rvRentalRequests
                            .isVisible =
                            true

                        binding.stateContainer
                            .isVisible =
                            false
                    }
                }

                is ApiResult.Error -> {

                    showError(
                        result.exception.message
                    )
                }

                ApiResult.Loading ->
                    Unit
            }
        }
    }

    private fun confirmCancel(
        item: RentalRequestResponse
    ) {

        MaterialAlertDialogBuilder(
            this
        )
            .setTitle(
                "Hủy yêu cầu thuê?"
            )
            .setMessage(
                "Bạn có chắc muốn hủy yêu cầu thuê \"${item.roomTitle}\"?"
            )
            .setNegativeButton(
                "Không",
                null
            )
            .setPositiveButton(
                "Hủy yêu cầu"
            ) { _, _ ->

                cancelRequest(
                    item.rentalRequestId
                )
            }
            .show()
    }

    private fun cancelRequest(
        requestId: Long
    ) {

        lifecycleScope.launch {

            when (
                val result =
                    repository.cancel(
                        requestId
                    )
            ) {

                is ApiResult.Success -> {

                    Toast.makeText(
                        this@MyRentalRequestsActivity,
                        "Đã hủy yêu cầu thuê.",
                        Toast.LENGTH_SHORT
                    ).show()

                    loadRequests()
                }

                is ApiResult.Error -> {

                    Toast.makeText(
                        this@MyRentalRequestsActivity,
                        result.exception.message,
                        Toast.LENGTH_LONG
                    ).show()
                }

                ApiResult.Loading ->
                    Unit
            }
        }
    }

    private fun showLoading() {

        binding.progressRequests
            .isVisible = true

        binding.rvRentalRequests
            .isVisible = false

        binding.stateContainer
            .isVisible = false
    }

    private fun showEmpty() {

        binding.progressRequests
            .isVisible = false

        binding.rvRentalRequests
            .isVisible = false

        binding.stateContainer
            .isVisible = true

        binding.btnRetry
            .isVisible = false

        binding.tvRequestCount.text =
            "Chưa có yêu cầu thuê"
    }

    private fun showError(
        message: String
    ) {

        binding.progressRequests
            .isVisible = false

        binding.rvRentalRequests
            .isVisible = false

        binding.stateContainer
            .isVisible = true

        binding.btnRetry
            .isVisible = true

        binding.tvStateMessage.text =
            message

        binding.tvRequestCount.text =
            "Không thể tải yêu cầu thuê"
    }
}