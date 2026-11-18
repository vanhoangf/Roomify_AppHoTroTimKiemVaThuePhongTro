package com.roomify.app.ui.student

import android.content.Intent
import android.os.Bundle

import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager

import com.roomify.app.data.remote.api.PaymentApi
import com.roomify.app.data.remote.common.ApiResult
import com.roomify.app.data.remote.network.RetrofitClient
import com.roomify.app.data.repository.PaymentRepository
import com.roomify.app.databinding.ActivityMyPaymentsBinding

import kotlinx.coroutines.launch

class MyPaymentsActivity :
    AppCompatActivity() {

    private lateinit var binding:
            ActivityMyPaymentsBinding

    private lateinit var repository:
            PaymentRepository

    private lateinit var adapter:
            MyPaymentsAdapter

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        binding =
            ActivityMyPaymentsBinding.inflate(
                layoutInflater
            )

        setContentView(
            binding.root
        )

        setupInsets()
        setupRepository()
        setupRecyclerView()
        setupListeners()

        loadPayments()
    }

    override fun onResume() {
        super.onResume()

        if (
            ::repository.isInitialized &&
            ::binding.isInitialized
        ) {
            loadPayments()
        }
    }

    private fun setupInsets() {

        ViewCompat
            .setOnApplyWindowInsetsListener(
                binding.myPaymentsRoot
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
            PaymentRepository(
                RetrofitClient.createService(
                    PaymentApi::class.java
                )
            )
    }

    private fun setupRecyclerView() {

        adapter =
            MyPaymentsAdapter {

                startActivity(
                    Intent(
                        this,
                        PaymentDetailActivity::class.java
                    ).apply {

                        putExtra(
                            PaymentDetailActivity
                                .EXTRA_PAYMENT_ID,
                            it.paymentId
                        )
                    }
                )
            }

        binding.rvPayments.apply {

            layoutManager =
                LinearLayoutManager(
                    this@MyPaymentsActivity
                )

            adapter =
                this@MyPaymentsActivity.adapter
        }
    }

    private fun setupListeners() {

        binding.btnBack
            .setOnClickListener {
                finish()
            }

        binding.btnRetry
            .setOnClickListener {
                loadPayments()
            }
    }

    private fun loadPayments() {

        binding.progressPayments
            .isVisible = true

        binding.rvPayments
            .isVisible = false

        binding.stateContainer
            .isVisible = false

        lifecycleScope.launch {

            when (
                val result =
                    repository
                        .getMyPayments()
            ) {

                is ApiResult.Success -> {

                    val data =
                        result.data

                    val items =
                        data?.items.orEmpty()

                    binding.progressPayments
                        .isVisible = false

                    adapter.submitList(
                        items
                    )

                    binding.tvPaymentCount.text =
                        "Bạn có ${data?.totalCount ?: items.size} khoản thanh toán"

                    if (items.isEmpty()) {

                        binding.stateContainer
                            .isVisible = true

                        binding.btnRetry
                            .isVisible = false

                    } else {

                        binding.rvPayments
                            .isVisible = true
                    }
                }

                is ApiResult.Error -> {

                    binding.progressPayments
                        .isVisible = false

                    binding.stateContainer
                        .isVisible = true

                    binding.btnRetry
                        .isVisible = true

                    binding.tvStateMessage.text =
                        result.exception.message
                }

                ApiResult.Loading -> Unit
            }
        }
    }
}