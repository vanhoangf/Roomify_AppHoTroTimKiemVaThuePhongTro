package com.roomify.app.ui.student

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast

import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope

import com.google.android.material.dialog.MaterialAlertDialogBuilder

import com.roomify.app.R
import com.roomify.app.data.remote.api.PaymentApi
import com.roomify.app.data.remote.common.ApiResult
import com.roomify.app.data.remote.dto.payments.PaymentResponse
import com.roomify.app.data.remote.dto.payments.SubmitPaymentRequest
import com.roomify.app.data.remote.network.RetrofitClient
import com.roomify.app.data.repository.PaymentRepository
import com.roomify.app.databinding.ActivityPaymentDetailBinding

import kotlinx.coroutines.launch

import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class PaymentDetailActivity :
    AppCompatActivity() {

    companion object {

        const val EXTRA_PAYMENT_ID =
            "extra_payment_id"
    }

    private lateinit var binding:
            ActivityPaymentDetailBinding

    private lateinit var repository:
            PaymentRepository

    private var paymentId =
        -1L

    private var currentPayment:
            PaymentResponse? =
        null

    private var isSubmitting =
        false

    private val money =
        NumberFormat.getCurrencyInstance(
            Locale.forLanguageTag("vi-VN")
        )

    private val paymentMethods =
        listOf(
            "Chuyển khoản ngân hàng",
            "Tiền mặt",
            "Khác"
        )

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        binding =
            ActivityPaymentDetailBinding.inflate(
                layoutInflater
            )

        setContentView(binding.root)

        paymentId =
            intent.getLongExtra(
                EXTRA_PAYMENT_ID,
                -1L
            )

        setupInsets()
        setupRepository()
        setupPaymentMethods()
        setupListeners()

        loadPayment()
    }

    private fun setupInsets() {

        ViewCompat
            .setOnApplyWindowInsetsListener(
                binding.paymentDetailRoot
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

    private fun setupPaymentMethods() {

        binding.actPaymentMethod
            .setAdapter(
                ArrayAdapter(
                    this,
                    android.R.layout
                        .simple_dropdown_item_1line,
                    paymentMethods
                )
            )
    }

    private fun setupListeners() {

        binding.btnBack
            .setOnClickListener {
                finish()
            }

        binding.btnSubmit
            .setOnClickListener {
                submitPayment()
            }

        binding.btnCancel
            .setOnClickListener {
                confirmCancel()
            }
    }

    private fun loadPayment() {

        if (paymentId <= 0L) {

            Toast.makeText(
                this,
                "Không tìm thấy khoản thanh toán.",
                Toast.LENGTH_SHORT
            ).show()

            finish()
            return
        }

        binding.progressDetail
            .isVisible = true

        binding.scrollContent
            .isVisible = false

        binding.actionContainer
            .isVisible = false

        lifecycleScope.launch {

            when (
                val result =
                    repository.getDetail(
                        paymentId
                    )
            ) {

                is ApiResult.Success -> {

                    val payment =
                        result.data

                    if (payment == null) {

                        Toast.makeText(
                            this@PaymentDetailActivity,
                            "Không nhận được dữ liệu thanh toán.",
                            Toast.LENGTH_LONG
                        ).show()

                        finish()
                        return@launch
                    }

                    showPayment(
                        payment
                    )
                }

                is ApiResult.Error -> {

                    binding.progressDetail
                        .isVisible = false

                    Toast.makeText(
                        this@PaymentDetailActivity,
                        result.exception.message,
                        Toast.LENGTH_LONG
                    ).show()

                    finish()
                }

                ApiResult.Loading -> Unit
            }
        }
    }

    private fun showPayment(
        payment: PaymentResponse
    ) {

        currentPayment =
            payment

        binding.progressDetail
            .isVisible = false

        binding.scrollContent
            .isVisible = true

        binding.tvPaymentType.text =
            getPaymentTypeText(
                payment.paymentType
            )

        binding.tvAmount.text =
            money.format(
                payment.amount
            )

        binding.tvRoomTitle.text =
            payment.roomTitle

        binding.tvContractCode.text =
            "Hợp đồng: ${payment.contractCode}"

        binding.tvDueDate.text =
            payment.dueDate?.let {

                "Hạn thanh toán: ${formatDate(it)}"

            } ?: "Không có hạn thanh toán"

        showStatus(
            payment.status
        )

        fillPaymentInformation(
            payment
        )

        showConfirmationInformation(
            payment
        )

        showActions(
            payment.status
        )
    }

    private fun fillPaymentInformation(
        payment: PaymentResponse
    ) {

        binding.actPaymentMethod
            .setText(
                when (
                    payment.paymentMethod
                        ?.uppercase(Locale.ROOT)
                ) {

                    "BANK_TRANSFER" ->
                        "Chuyển khoản ngân hàng"

                    "CASH" ->
                        "Tiền mặt"

                    "OTHER" ->
                        "Khác"

                    else ->
                        ""
                },
                false
            )

        binding.edtTransactionReference
            .setText(
                payment.transactionReference
                    .orEmpty()
            )

        binding.edtProofUrl
            .setText(
                payment.proofImageUrl
                    .orEmpty()
            )

        binding.edtNote
            .setText(
                payment.note
                    .orEmpty()
            )
    }

    private fun showStatus(
        statusValue: String
    ) {

        val status =
            statusValue.uppercase(
                Locale.ROOT
            )

        binding.chipStatus.text =
            when (status) {

                "PENDING" ->
                    "Chờ xác nhận"

                "PAID" ->
                    "Đã thanh toán"

                "FAILED" ->
                    "Bị từ chối"

                "CANCELLED" ->
                    "Đã hủy"

                else ->
                    status
            }

        val colorRes =
            when (status) {

                "PAID" ->
                    R.color.status_success

                "FAILED",
                "CANCELLED" ->
                    R.color.status_error

                else ->
                    R.color.primary_blue
            }

        binding.chipStatus
            .setTextColor(
                ContextCompat.getColor(
                    this,
                    colorRes
                )
            )
    }

    private fun showActions(
        statusValue: String
    ) {

        val isPending =
            statusValue.equals(
                "PENDING",
                ignoreCase = true
            )

        binding.actionContainer
            .isVisible =
            isPending

        setFormEnabled(
            isPending
        )
    }

    private fun setFormEnabled(
        enabled: Boolean
    ) {

        binding.actPaymentMethod
            .isEnabled =
            enabled

        binding.edtTransactionReference
            .isEnabled =
            enabled

        binding.edtProofUrl
            .isEnabled =
            enabled

        binding.edtNote
            .isEnabled =
            enabled
    }

    private fun showConfirmationInformation(
        payment: PaymentResponse
    ) {

        if (
            payment.confirmedAt.isNullOrBlank() &&
            payment.confirmedByUserName.isNullOrBlank() &&
            payment.paidAt.isNullOrBlank()
        ) {

            binding.tvConfirmationInfo
                .visibility =
                View.GONE

            return
        }

        binding.tvConfirmationInfo
            .visibility =
            View.VISIBLE

        binding.tvConfirmationInfo.text =
            buildString {

                payment.confirmedByUserName
                    ?.takeIf {
                        it.isNotBlank()
                    }
                    ?.let {

                        append(
                            "Xác nhận bởi: $it"
                        )
                    }

                payment.paidAt
                    ?.takeIf {
                        it.isNotBlank()
                    }
                    ?.let {

                        if (isNotEmpty()) {
                            append("\n")
                        }

                        append(
                            "Thanh toán đã được xác nhận."
                        )
                    }
            }
    }

    private fun submitPayment() {

        if (isSubmitting) {
            return
        }

        binding.tilPaymentMethod.error =
            null

        val paymentMethodText =
            binding.actPaymentMethod.text
                ?.toString()
                ?.trim()
                .orEmpty()

        val paymentMethod =
            when (paymentMethodText) {

                "Chuyển khoản ngân hàng" ->
                    "BANK_TRANSFER"

                "Tiền mặt" ->
                    "CASH"

                "Khác" ->
                    "OTHER"

                else ->
                    null
            }

        if (paymentMethod == null) {

            binding.tilPaymentMethod.error =
                "Vui lòng chọn phương thức thanh toán."

            return
        }

        val transactionReference =
            binding.edtTransactionReference
                .text
                ?.toString()
                ?.trim()
                ?.takeIf {
                    it.isNotBlank()
                }

        val proofUrl =
            binding.edtProofUrl
                .text
                ?.toString()
                ?.trim()
                ?.takeIf {
                    it.isNotBlank()
                }

        val note =
            binding.edtNote
                .text
                ?.toString()
                ?.trim()
                ?.takeIf {
                    it.isNotBlank()
                }

        val request =
            SubmitPaymentRequest(
                paymentMethod =
                    paymentMethod,

                transactionReference =
                    transactionReference,

                proofImageUrl =
                    proofUrl,

                note =
                    note
            )

        sendPayment(
            request
        )
    }

    private fun sendPayment(
        request: SubmitPaymentRequest
    ) {

        isSubmitting =
            true

        setLoadingActions(
            true
        )

        lifecycleScope.launch {

            try {

                when (
                    val result =
                        repository.submit(
                            paymentId,
                            request
                        )
                ) {

                    is ApiResult.Success -> {

                        Toast.makeText(
                            this@PaymentDetailActivity,
                            "Đã gửi thông tin thanh toán.",
                            Toast.LENGTH_LONG
                        ).show()

                        result.data
                            ?.let {
                                showPayment(it)
                            }
                    }

                    is ApiResult.Error -> {

                        Toast.makeText(
                            this@PaymentDetailActivity,
                            result.exception.message,
                            Toast.LENGTH_LONG
                        ).show()
                    }

                    ApiResult.Loading ->
                        Unit
                }

            } finally {

                isSubmitting =
                    false

                setLoadingActions(
                    false
                )
            }
        }
    }

    private fun confirmCancel() {

        MaterialAlertDialogBuilder(
            this
        )
            .setTitle(
                "Hủy khoản thanh toán?"
            )
            .setMessage(
                "Bạn có chắc muốn hủy khoản thanh toán này?"
            )
            .setNegativeButton(
                "Không",
                null
            )
            .setPositiveButton(
                "Hủy"
            ) { _, _ ->

                cancelPayment()
            }
            .show()
    }

    private fun cancelPayment() {

        setLoadingActions(
            true
        )

        lifecycleScope.launch {

            try {

                when (
                    val result =
                        repository.cancel(
                            paymentId
                        )
                ) {

                    is ApiResult.Success -> {

                        Toast.makeText(
                            this@PaymentDetailActivity,
                            "Đã hủy khoản thanh toán.",
                            Toast.LENGTH_SHORT
                        ).show()

                        result.data
                            ?.let {
                                showPayment(it)
                            }
                    }

                    is ApiResult.Error -> {

                        Toast.makeText(
                            this@PaymentDetailActivity,
                            result.exception.message,
                            Toast.LENGTH_LONG
                        ).show()
                    }

                    ApiResult.Loading ->
                        Unit
                }

            } finally {

                setLoadingActions(
                    false
                )
            }
        }
    }

    private fun setLoadingActions(
        loading: Boolean
    ) {

        binding.btnSubmit
            .isEnabled =
            !loading

        binding.btnCancel
            .isEnabled =
            !loading

        binding.btnSubmit.text =
            if (loading) {
                "Đang xử lý..."
            } else {
                "Gửi thông tin thanh toán"
            }
    }

    private fun getPaymentTypeText(
        type: String
    ): String {

        return when (
            type.uppercase(
                Locale.ROOT
            )
        ) {

            "DEPOSIT" ->
                "Tiền cọc"

            "RENT" ->
                "Tiền thuê"

            "UTILITY" ->
                "Điện nước / Tiện ích"

            "OTHER" ->
                "Khoản khác"

            else ->
                type
        }
    }

    private fun formatDate(
        value: String?
    ): String {

        if (value.isNullOrBlank()) {
            return "Không có"
        }

        return runCatching {

            LocalDate.parse(
                value
            ).format(
                DateTimeFormatter.ofPattern(
                    "dd/MM/yyyy"
                )
            )

        }.getOrElse {
            value
        }
    }
}