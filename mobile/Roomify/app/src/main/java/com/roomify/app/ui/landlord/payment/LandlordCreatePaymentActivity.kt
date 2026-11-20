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

    private var isSubmitting =
        false

    private val paymentTypeLabels =
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
        setupPaymentTypeDropdown()
        setupActions()
    }

    // ============================================================
    // INSETS
    // ============================================================

    private fun setupInsets() {

        ViewCompat.setOnApplyWindowInsetsListener(
            binding.createPaymentRoot
        ) { view, insets ->

            val systemBars =
                insets.getInsets(
                    WindowInsetsCompat.Type.systemBars()
                )

            view.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                systemBars.bottom
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
            LandlordPaymentRepository(
                RetrofitClient.createService(
                    LandlordPaymentApi::class.java
                )
            )
    }

    // ============================================================
    // PAYMENT TYPE DROPDOWN
    // ============================================================

    private fun setupPaymentTypeDropdown() {

        val adapter =
            ArrayAdapter(
                this,
                android.R.layout
                    .simple_dropdown_item_1line,
                paymentTypeLabels
            )

        binding.actPaymentType
            .setAdapter(
                adapter
            )
    }

    // ============================================================
    // ACTIONS
    // ============================================================

    private fun setupActions() {

        binding.btnBack
            .setOnClickListener {

                if (!isSubmitting) {
                    finish()
                }
            }

        binding.edtDueDate
            .setOnClickListener {

                if (!isSubmitting) {
                    showDatePicker()
                }
            }

        binding.btnCreate
            .setOnClickListener {

                createPayment()
            }
    }

    // ============================================================
    // DATE PICKER
    // ============================================================

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
                selectedMillis ->

            val selectedDate =
                Instant
                    .ofEpochMilli(
                        selectedMillis
                    )
                    .atZone(
                        ZoneOffset.UTC
                    )
                    .toLocalDate()

            selectedDueDate =
                selectedDate.toString()

            binding.edtDueDate
                .setText(
                    selectedDate.format(
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

    // ============================================================
    // CREATE PAYMENT
    // ============================================================

    private fun createPayment() {

        if (isSubmitting) {
            return
        }

        clearErrors()

        val paymentType =
            getSelectedPaymentType()

        if (paymentType == null) {

            binding.tilPaymentType.error =
                "Vui lòng chọn loại thanh toán."

            return
        }

        val amount =
            getAmount()

        if (amount == null) {

            binding.tilAmount.error =
                "Vui lòng nhập số tiền hợp lệ."

            return
        }

        if (
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

        if (
            note != null &&
            note.length > 1000
        ) {

            binding.tilNote.error =
                "Ghi chú tối đa 1000 ký tự."

            return
        }

        sendCreatePayment(
            paymentType =
                paymentType,
            amount =
                amount,
            dueDate =
                selectedDueDate,
            note =
                note
        )
    }

    // ============================================================
    // SEND REQUEST
    // ============================================================

    private fun sendCreatePayment(
        paymentType: String,
        amount: BigDecimal,
        dueDate: String?,
        note: String?
    ) {

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
                                paymentType,
                            amount =
                                amount,
                            dueDate =
                                dueDate,
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

    // ============================================================
    // VALIDATION HELPERS
    // ============================================================

    private fun clearErrors() {

        binding.tilPaymentType.error =
            null

        binding.tilAmount.error =
            null

        binding.tilDueDate.error =
            null

        binding.tilNote.error =
            null
    }

    private fun getSelectedPaymentType():
            String? {

        val selected =
            binding.actPaymentType.text
                ?.toString()
                ?.trim()
                .orEmpty()

        return when (selected) {

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
    }

    private fun getAmount():
            BigDecimal? {

        val rawValue =
            binding.edtAmount.text
                ?.toString()
                ?.trim()
                .orEmpty()

        if (rawValue.isBlank()) {
            return null
        }

        return runCatching {

            val normalized =
                rawValue
                    .replace(
                        " ",
                        ""
                    )
                    .replace(
                        ",",
                        ""
                    )

            BigDecimal(
                normalized
            )

        }.getOrNull()
    }

    // ============================================================
    // LOADING
    // ============================================================

    private fun setLoading(
        loading: Boolean
    ) {

        isSubmitting =
            loading

        binding.btnCreate.isEnabled =
            !loading

        binding.btnBack.isEnabled =
            !loading

        binding.actPaymentType.isEnabled =
            !loading

        binding.edtAmount.isEnabled =
            !loading

        binding.edtDueDate.isEnabled =
            !loading

        binding.edtNote.isEnabled =
            !loading

        binding.btnCreate.text =
            if (loading) {

                "Đang tạo..."

            } else {

                "Tạo khoản thanh toán"
            }
    }

    // ============================================================
    // ROLE GUARD
    // ============================================================

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