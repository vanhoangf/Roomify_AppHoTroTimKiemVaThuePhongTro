package com.roomify.app.ui.landlord.contract

import android.content.Intent
import android.os.Bundle
import android.widget.Toast

import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope

import com.roomify.app.data.remote.api.LandlordRentalContractApi
import com.roomify.app.data.remote.common.ApiResult
import com.roomify.app.data.remote.network.RetrofitClient
import com.roomify.app.data.repository.LandlordRentalContractRepository
import com.roomify.app.databinding.ActivityLandlordCreateContractBinding
import com.roomify.app.ui.common.RoleSessionGuard

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

class LandlordCreateContractActivity :
    AppCompatActivity() {

    companion object {

        const val EXTRA_RENTAL_REQUEST_ID =
            "extra_landlord_contract_rental_request_id"
    }

    private lateinit var binding:
            ActivityLandlordCreateContractBinding

    private lateinit var repository:
            LandlordRentalContractRepository

    private var rentalRequestId =
        0L

    private var isSubmitting =
        false

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(
            savedInstanceState
        )

        binding =
            ActivityLandlordCreateContractBinding
                .inflate(
                    layoutInflater
                )

        setContentView(
            binding.root
        )

        rentalRequestId =
            intent.getLongExtra(
                EXTRA_RENTAL_REQUEST_ID,
                0L
            )

        if (
            rentalRequestId <= 0L
        ) {

            Toast.makeText(
                this,
                "Không tìm thấy yêu cầu thuê.",
                Toast.LENGTH_LONG
            ).show()

            finish()

            return
        }

        setupInsets()
        setupRepository()
        setupActions()
    }

    private fun setupInsets() {

        ViewCompat.setOnApplyWindowInsetsListener(
            binding.createContractRoot
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

    private fun setupActions() {

        binding.btnBack
            .setOnClickListener {
                finish()
            }

        binding.btnCreateContract
            .setOnClickListener {

                createContract()
            }
    }

    private fun createContract() {

        if (isSubmitting) {
            return
        }

        val terms =
            binding.edtTerms.text
                ?.toString()
                ?.trim()

        val url =
            binding.edtContractFileUrl.text
                ?.toString()
                ?.trim()

        if (
            terms != null &&
            terms.length > 3000
        ) {

            binding.edtTerms.error =
                "Điều khoản tối đa 3000 ký tự."

            return
        }

        if (
            url != null &&
            url.length > 2000
        ) {

            binding.edtContractFileUrl.error =
                "URL tối đa 2000 ký tự."

            return
        }

        setLoading(
            true
        )

        lifecycleScope.launch {

            try {

                when (
                    val result =
                        repository.create(
                            rentalRequestId =
                                rentalRequestId,
                            terms =
                                terms,
                            contractFileUrl =
                                url
                        )
                ) {

                    is ApiResult.Success -> {

                        val contract =
                            result.data

                        if (
                            contract == null
                        ) {

                            Toast.makeText(
                                this@LandlordCreateContractActivity,
                                "Đã tạo hợp đồng.",
                                Toast.LENGTH_SHORT
                            ).show()

                            finish()

                        } else {

                            Toast.makeText(
                                this@LandlordCreateContractActivity,
                                "Đã tạo hợp đồng.",
                                Toast.LENGTH_SHORT
                            ).show()

                            startActivity(
                                Intent(
                                    this@LandlordCreateContractActivity,
                                    LandlordContractDetailActivity::class.java
                                ).apply {

                                    putExtra(
                                        LandlordContractDetailActivity
                                            .EXTRA_CONTRACT_ID,
                                        contract.contractId
                                    )
                                }
                            )

                            finish()
                        }
                    }

                    is ApiResult.Error -> {

                        Toast.makeText(
                            this@LandlordCreateContractActivity,
                            result.exception.message
                                ?: "Không thể tạo hợp đồng.",
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
                    this@LandlordCreateContractActivity,
                    e.message
                        ?: "Không thể tạo hợp đồng.",
                    Toast.LENGTH_LONG
                ).show()

            } finally {

                setLoading(
                    false
                )
            }
        }
    }

    private fun setLoading(
        loading: Boolean
    ) {

        isSubmitting =
            loading

        binding.btnCreateContract.isEnabled =
            !loading

        binding.btnBack.isEnabled =
            !loading

        binding.btnCreateContract.text =
            if (loading) {
                "Đang tạo..."
            } else {
                "Tạo hợp đồng"
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