package com.roomify.app.ui.landlord.payment

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast

import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope

import com.google.android.material.datepicker.MaterialDatePicker
import com.roomify.app.data.remote.api.LandlordPaymentApi
import com.roomify.app.data.remote.common.ApiResult
import com.roomify.app.data.remote.network.RetrofitClient
import com.roomify.app.data.repository.LandlordPaymentRepository
import com.roomify.app.databinding.ActivityLandlordCreatePaymentBinding
import com.roomify.app.ui.common.RoleSessionGuard

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

import java.math.BigDecimal
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

class LandlordCreatePaymentActivity :
    AppCompatActivity() {

    companion object {

        const val EXTRA_CONTRACT_ID =
            "extra_landlord_payment_contract_id"
    }

    private lateinit var binding:
            ActivityLandlordCreatePaymentBinding

    private lateinit var repository:
            LandlordPaymentRepository

    private var contractId =
        0L

    private var selectedDueDate:
            String? =
        null

    private var submitting =
        false

    private val typeLabels =
        listOf(
            "Tiền cọc",
            "Tiền thuê",
            "Điện nước / Tiện ích",
            "Khoản khác"
        )

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(
            savedInstanceState
        )

        binding =
            ActivityLandlordCreatePaymentBinding.inflate(
                layoutInflater
            )

        setContentView(
            binding.root
        )

        contractId =
            intent.getLongExtra(
                EXTRA_CONTRACT_ID,
                0L
            )

        if (contractId <= 0L) {

            Toast.makeText(
                this,
                "Không tìm thấy hợp đồng.",
                Toast.LENGTH_LONG
            ).show()

            finish()

            return
        }

        setupInsets()
        setupRepository()
        setupTypeDropdown()
        setupActions()
    }

    private fun setupInsets() {

        ViewCompat.setOnApplyWindowInsetsListener(
            binding.createPaymentRoot
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

    private fun setupTypeDropdown() {

        binding.actPaymentType.setAdapter(
            ArrayAdapter(
                this,
                android.R.layout
                    .simple_dropdown_item_1line,
                typeLabels
            )
        )
    }

    private fun setupActions() {

        binding.btnBack
            .setOnClickListener {
                finish()
            }

        binding.edtDueDate
            .setOnClickListener {

                showDatePicker()
            }

        binding.btnCreate
            .setOnClickListener {

                createPayment()
            }
    }

    private fun showDatePicker() {

        val picker =
            MaterialDatePicker.Builder
                .datePicker()
                .setTitleText(
                    "Chọn hạn thanh toán"
                )
                .setSelection(
                    MaterialDatePicker
                        .todayInUtcMilliseconds()
                )
                .build()

        picker.addOnPositiveButtonClickListener {
                millis ->

            val date =
                Instant
                    .ofEpochMilli(
                        millis
                    )
                    .atZone(
                        ZoneOffset.UTC
                    )
                    .toLocalDate()

            selectedDueDate =
                date.toString()

            binding.edtDueDate.setText(
                date.format(
                    DateTimeFormatter.ofPattern(
                        "dd/MM/yyyy"
                    )
                )
            )
        }

        picker.show(
            supportFragmentManager,
            "landlord_payment_due_date"
        )
    }

    private fun createPayment() {

        if (submitting) {
            return
        }

        binding.tilPaymentType.error =
            null

        binding.tilAmount.error =
            null

        val type =
            when (
                binding.actPaymentType.text
                    ?.toString()
                    ?.trim()
            ) {

                "Tiền cọc" ->
                    "DEPOSIT"

                "Tiền thuê" ->
                    "RENT"

                "Điện nước / Tiện ích" ->
                    "UTILITY"

                "Khoản khác" ->
                    "OTHER"

                else ->
                    null
            }

        if (type == null) {

            binding.tilPaymentType.error =
                "Vui lòng chọn loại thanh toán."

            return
        }

        val rawAmount =
            binding.edtAmount.text
                ?.toString()
                ?.trim()
                .orEmpty()

        val amount =
            runCatching {

                BigDecimal(
                    rawAmount
                        .replace(
                            ".",
                            ""
                        )
                        .replace(
                            ",",
                            ""
                        )
                )

            }.getOrNull()

        if (
            amount == null ||
            amount <= BigDecimal.ZERO
        ) {

            binding.tilAmount.error =
                "Số tiền phải lớn hơn 0."

            return
        }

        val note =
            binding.edtNote.text
                ?.toString()
                ?.trim()
                ?.takeIf {
                    it.isNotBlank()
                }

        setLoading(
            true
        )

        lifecycleScope.launch {

            try {

                when (
                    val result =
                        repository.create(
                            contractId =
                                contractId,
                            paymentType =
                                type,
                            amount =
                                amount,
                            dueDate =
                                selectedDueDate,
                            note =
                                note
                        )
                ) {

                    is ApiResult.Success -> {

                        val payment =
                            result.data

                        Toast.makeText(
                            this@LandlordCreatePaymentActivity,
                            "Đã tạo khoản thanh toán.",
                            Toast.LENGTH_SHORT
                        ).show()

                        if (payment != null) {

                            startActivity(
                                Intent(
                                    this@LandlordCreatePaymentActivity,
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

                        finish()
                    }

                    is ApiResult.Error -> {

                        Toast.makeText(
                            this@LandlordCreatePaymentActivity,
                            result.exception.message
                                ?: "Không thể tạo khoản thanh toán.",
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
                    this@LandlordCreatePaymentActivity,
                    e.message
                        ?: "Không thể tạo khoản thanh toán.",
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
        value: Boolean
    ) {

        submitting =
            value

        binding.btnCreate.isEnabled =
            !value

        binding.btnBack.isEnabled =
            !value

        binding.btnCreate.text =
            if (value) {
                "Đang tạo..."
            } else {
                "Tạo khoản thanh toán"
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