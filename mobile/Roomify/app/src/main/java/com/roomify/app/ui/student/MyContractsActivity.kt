package com.roomify.app.ui.student

import android.content.Intent
import android.os.Bundle

import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager

import com.roomify.app.data.remote.api.RentalContractApi
import com.roomify.app.data.remote.common.ApiResult
import com.roomify.app.data.remote.network.RetrofitClient
import com.roomify.app.data.repository.RentalContractRepository
import com.roomify.app.databinding.ActivityMyContractsBinding

import kotlinx.coroutines.launch

class MyContractsActivity :
    AppCompatActivity() {

    private lateinit var binding:
            ActivityMyContractsBinding

    private lateinit var repository:
            RentalContractRepository

    private lateinit var adapter:
            MyContractsAdapter

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        binding =
            ActivityMyContractsBinding.inflate(
                layoutInflater
            )

        setContentView(binding.root)

        setupInsets()
        setupRepository()
        setupRecyclerView()
        setupListeners()

        loadContracts()
    }

    override fun onResume() {
        super.onResume()

        if (
            ::repository.isInitialized &&
            ::binding.isInitialized
        ) {
            loadContracts()
        }
    }

    private fun setupInsets() {

        ViewCompat
            .setOnApplyWindowInsetsListener(
                binding.myContractsRoot
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
            RentalContractRepository(
                RetrofitClient.createService(
                    RentalContractApi::class.java
                )
            )
    }

    private fun setupRecyclerView() {

        adapter =
            MyContractsAdapter {

                startActivity(
                    Intent(
                        this,
                        ContractDetailActivity::class.java
                    ).apply {

                        putExtra(
                            ContractDetailActivity
                                .EXTRA_CONTRACT_ID,
                            it.contractId
                        )
                    }
                )
            }

        binding.rvContracts.apply {

            layoutManager =
                LinearLayoutManager(
                    this@MyContractsActivity
                )

            adapter =
                this@MyContractsActivity.adapter
        }
    }

    private fun setupListeners() {

        binding.btnBack
            .setOnClickListener {
                finish()
            }

        binding.btnRetry
            .setOnClickListener {
                loadContracts()
            }
    }

    private fun loadContracts() {

        binding.progressContracts
            .isVisible = true

        binding.rvContracts
            .isVisible = false

        binding.stateContainer
            .isVisible = false

        lifecycleScope.launch {

            when (
                val result =
                    repository.getMyContracts()
            ) {

                is ApiResult.Success -> {

                    val data =
                        result.data

                    val items =
                        data?.items.orEmpty()

                    binding.progressContracts
                        .isVisible = false

                    adapter.submitList(items)

                    binding.tvContractCount.text =
                        "Bạn có ${data?.totalCount ?: items.size} hợp đồng"

                    if (items.isEmpty()) {

                        binding.stateContainer
                            .isVisible = true

                        binding.tvStateMessage.text =
                            "Bạn chưa có hợp đồng thuê."

                        binding.btnRetry
                            .isVisible = false

                    } else {

                        binding.rvContracts
                            .isVisible = true
                    }
                }

                is ApiResult.Error -> {

                    binding.progressContracts
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