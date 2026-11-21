package com.roomify.app.ui.landlord.payment

import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.widget.EditText
import android.widget.Toast

import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope

import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.roomify.app.R
import com.roomify.app.data.remote.api.LandlordPaymentApi
import com.roomify.app.data.remote.common.ApiResult
import com.roomify.app.data.remote.dto.payments.PaymentResponse
import com.roomify.app.data.remote.network.RetrofitClient
import com.roomify.app.data.repository.LandlordPaymentRepository
import com.roomify.app.databinding.ActivityLandlordPaymentDetailBinding
import com.roomify.app.ui.common.RoleSessionGuard

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

import java.math.BigDecimal
import java.text.NumberFormat
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

class LandlordPaymentDetailActivity :
    AppCompatActivity() {

    companion object {

        const val EXTRA_PAYMENT_ID =
            "extra_landlord_payment_id"
    }

    private lateinit var binding:
            ActivityLandlordPaymentDetailBinding

    private lateinit var repository:
            LandlordPaymentRepository

    private var paymentId =
        0L

    private var currentPayment:
            PaymentResponse? =
        null

    private var actionLoading =
        false

    private val appZone =
        ZoneId.of(
            "Asia/Ho_Chi_Minh"
        )

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(
            savedInstanceState
        )

        binding =
            ActivityLandlordPaymentDetailBinding.inflate(
                layoutInflater
            )

        setContentView(
            binding.root
        )

        paymentId =
            intent.getLongExtra(
                EXTRA_PAYMENT_ID,
                0L
            )

        if (paymentId <= 0L) {

            Toast.makeText(
                this,
                "Không tìm thấy khoản thanh toán.",
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
            binding.paymentDetailRoot
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

    private fun setupActions() {

        binding.btnBack
            .setOnClickListener {
                finish()
            }

        binding.btnRetry
            .setOnClickListener {
                loadDetail()
            }

        binding.btnConfirm
            .setOnClickListener {
                showConfirmDialog()
            }

        binding.btnReject
            .setOnClickListener {
                showRejectDialog()
            }

        binding.btnCancel
            .setOnClickListener {
                showCancelDialog()
            }

        binding.btnOpenProof
            .setOnClickListener {
                openProof()
            }
    }

    private fun loadDetail() {

        showLoading()

        lifecycleScope.launch {

            try {

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

                            showError(
                                "Không nhận được dữ liệu thanh toán."
                            )

                        } else {

                            bindPayment(
                                payment
                            )
                        }
                    }

                    is ApiResult.Error -> {

                        showError(
                            result.exception.message
                                ?: "Không thể tải thanh toán."
                        )
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
                        ?: "Không thể tải thanh toán."
                )
            }
        }
    }

    private fun bindPayment(
        payment: PaymentResponse
    ) {

        currentPayment =
            payment

        binding.progressDetail.isVisible =
            false

        binding.errorContainer.isVisible =
            false

        binding.contentScroll.isVisible =
            true

        binding.tvPaymentType.text =
            paymentTypeText(
                payment.paymentType
            )

        binding.tvAmount.text =
            formatMoney(
                payment.amount
            )

        binding.tvStudentName.text =
            payment.studentName
                .ifBlank {
                    "Sinh viên"
                }

        binding.tvStudentPhone.text =
            payment.studentPhone
                ?.takeIf {
                    it.isNotBlank()
                }
                ?.let {
                    "SĐT: $it"
                }
                ?: "SĐT: Chưa cập nhật"

        binding.tvContractCode.text =
            "Hợp đồng: ${payment.contractCode}"

        binding.tvRoomTitle.text =
            payment.roomTitle

        binding.tvDueDate.text =
            payment.dueDate
                ?.let {
                    "Hạn thanh toán: ${formatDate(it)}"
                }
                ?: "Không có hạn thanh toán"

        binding.tvPaymentMethod.text =
            "Phương thức: " +
                    paymentMethodText(
                        payment.paymentMethod
                    )

        binding.tvTransactionReference.text =
            payment.transactionReference
                ?.takeIf {
                    it.isNotBlank()
                }
                ?.let {
                    "Mã giao dịch: $it"
                }
                ?: "Mã giao dịch: Chưa có"

        binding.tvNote.text =
            payment.note
                ?.takeIf {
                    it.isNotBlank()
                }
                ?: "Chưa có ghi chú."

        binding.btnOpenProof.isVisible =
            !payment.proofImageUrl
                .isNullOrBlank()

        val showConfirmation =
            !payment.confirmedByUserName
                .isNullOrBlank() ||
                    !payment.confirmedAt
                        .isNullOrBlank() ||
                    !payment.paidAt
                        .isNullOrBlank()

        binding.confirmationContainer.isVisible =
            showConfirmation

        binding.tvConfirmedBy.text =
            "Xác nhận bởi: " +
                    (
                            payment.confirmedByUserName
                                ?.takeIf {
                                    it.isNotBlank()
                                }
                                ?: "-"
                            )

        binding.tvConfirmedAt.text =
            "Thời gian xác nhận: " +
                    formatDateTime(
                        payment.confirmedAt
                    )

        binding.tvPaidAt.text =
            "Thanh toán lúc: " +
                    formatDateTime(
                        payment.paidAt
                    )

        bindStatus(
            payment
        )
    }

    private fun bindStatus(
        payment: PaymentResponse
    ) {

        val status =
            payment.status.uppercase(
                Locale.ROOT
            )

        val submitted =
            !payment.paymentMethod
                .isNullOrBlank()

        binding.tvStatus.text =
            when {

                status == "PENDING" &&
                        !submitted ->
                    "Chờ sinh viên thanh toán"

                status == "PENDING" &&
                        submitted ->
                    "Chờ xác nhận thanh toán"

                status == "PAID" ->
                    "Đã thanh toán"

                status == "FAILED" ->
                    "Đã từ chối"

                status == "CANCELLED" ->
                    "Đã hủy"

                else ->
                    status
            }

        val color =
            when (status) {

                "PAID" ->
                    R.color.status_success

                "FAILED",
                "CANCELLED" ->
                    R.color.status_error

                else ->
                    R.color.primary_blue
            }

        binding.tvStatus.setTextColor(
            ContextCompat.getColor(
                this,
                color
            )
        )

        val pending =
            status == "PENDING"

        /*
         * Landlord chỉ được Confirm / Reject
         * sau khi Student đã gửi PaymentMethod.
         */
        val canReview =
            pending &&
                    submitted

        binding.actionContainer.isVisible =
            pending

        binding.btnConfirm.isVisible =
            canReview

        binding.btnReject.isVisible =
            canReview

        /*
         * Landlord vẫn có quyền hủy khoản thanh toán
         * khi đang PENDING, kể cả Student chưa submit.
         */
        binding.btnCancel.isVisible =
            pending
    }

    private fun showConfirmDialog() {

        val input =
            createInput(
                "Ghi chú xác nhận (không bắt buộc)"
            )

        MaterialAlertDialogBuilder(
            this
        )
            .setTitle(
                "Xác nhận thanh toán?"
            )
            .setView(
                input
            )
            .setNegativeButton(
                "Hủy",
                null
            )
            .setPositiveButton(
                "Xác nhận"
            ) { _, _ ->

                confirmPayment(
                    input.text
                        ?.toString()
                )
            }
            .show()
    }

    private fun confirmPayment(
        note: String?
    ) {

        setActionLoading(
            true
        )

        lifecycleScope.launch {

            try {

                when (
                    val result =
                        repository.confirm(
                            paymentId =
                                paymentId,
                            note =
                                note
                        )
                ) {

                    is ApiResult.Success -> {

                        Toast.makeText(
                            this@LandlordPaymentDetailActivity,
                            "Đã xác nhận thanh toán.",
                            Toast.LENGTH_SHORT
                        ).show()

                        result.data
                            ?.let {
                                bindPayment(
                                    it
                                )
                            }
                            ?: loadDetail()
                    }

                    is ApiResult.Error -> {

                        showActionError(
                            result.exception.message
                                ?: "Không thể xác nhận thanh toán."
                        )
                    }

                    ApiResult.Loading ->
                        Unit
                }

            } finally {

                setActionLoading(
                    false
                )
            }
        }
    }

    private fun showRejectDialog() {

        val input =
            createInput(
                "Nhập lý do từ chối"
            )

        val dialog =
            MaterialAlertDialogBuilder(
                this
            )
                .setTitle(
                    "Từ chối thanh toán?"
                )
                .setMessage(
                    "Vui lòng nhập lý do từ chối."
                )
                .setView(
                    input
                )
                .setNegativeButton(
                    "Hủy",
                    null
                )
                .setPositiveButton(
                    "Từ chối",
                    null
                )
                .create()

        dialog.setOnShowListener {

            dialog.getButton(
                DialogInterface.BUTTON_POSITIVE
            ).setOnClickListener {

                val reason =
                    input.text
                        ?.toString()
                        ?.trim()
                        .orEmpty()

                if (reason.isBlank()) {

                    input.error =
                        "Vui lòng nhập lý do."

                    return@setOnClickListener
                }

                if (
                    reason.length > 1000
                ) {

                    input.error =
                        "Lý do tối đa 1000 ký tự."

                    return@setOnClickListener
                }

                dialog.dismiss()

                rejectPayment(
                    reason
                )
            }
        }

        dialog.show()
    }

    private fun rejectPayment(
        reason: String
    ) {

        setActionLoading(
            true
        )

        lifecycleScope.launch {

            try {

                when (
                    val result =
                        repository.reject(
                            paymentId =
                                paymentId,
                            reason =
                                reason
                        )
                ) {

                    is ApiResult.Success -> {

                        Toast.makeText(
                            this@LandlordPaymentDetailActivity,
                            "Đã từ chối thanh toán.",
                            Toast.LENGTH_SHORT
                        ).show()

                        result.data
                            ?.let {
                                bindPayment(
                                    it
                                )
                            }
                            ?: loadDetail()
                    }

                    is ApiResult.Error -> {

                        showActionError(
                            result.exception.message
                                ?: "Không thể từ chối thanh toán."
                        )
                    }

                    ApiResult.Loading ->
                        Unit
                }

            } finally {

                setActionLoading(
                    false
                )
            }
        }
    }

    private fun showCancelDialog() {

        MaterialAlertDialogBuilder(
            this
        )
            .setTitle(
                "Hủy khoản thanh toán?"
            )
            .setMessage(
                "Khoản thanh toán sẽ chuyển sang trạng thái đã hủy."
            )
            .setNegativeButton(
                "Không",
                null
            )
            .setPositiveButton(
                "Hủy khoản"
            ) { _, _ ->

                cancelPayment()
            }
            .show()
    }

    private fun cancelPayment() {

        setActionLoading(
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
                            this@LandlordPaymentDetailActivity,
                            "Đã hủy khoản thanh toán.",
                            Toast.LENGTH_SHORT
                        ).show()

                        result.data
                            ?.let {
                                bindPayment(
                                    it
                                )
                            }
                            ?: loadDetail()
                    }

                    is ApiResult.Error -> {

                        showActionError(
                            result.exception.message
                                ?: "Không thể hủy khoản thanh toán."
                        )
                    }

                    ApiResult.Loading ->
                        Unit
                }

            } finally {

                setActionLoading(
                    false
                )
            }
        }
    }

    private fun openProof() {

        val url =
            currentPayment
                ?.proofImageUrl
                ?.trim()
                ?.takeIf {
                    it.isNotBlank()
                }
                ?: return

        runCatching {

            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(
                        url
                    )
                )
            )

        }.onFailure {

            Toast.makeText(
                this,
                "Không thể mở minh chứng.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun createInput(
        hintText: String
    ): EditText {

        return EditText(
            this
        ).apply {

            hint =
                hintText

            minLines =
                3

            maxLines =
                6

            gravity =
                Gravity.TOP or
                        Gravity.START

            inputType =
                InputType.TYPE_CLASS_TEXT or
                        InputType.TYPE_TEXT_FLAG_MULTI_LINE or
                        InputType.TYPE_TEXT_FLAG_CAP_SENTENCES

            setPadding(
                dp(20),
                dp(14),
                dp(20),
                dp(14)
            )
        }
    }

    private fun dp(
        value: Int
    ): Int {

        return (
                value *
                        resources
                            .displayMetrics
                            .density
                ).toInt()
    }

    private fun paymentTypeText(
        value: String
    ): String {

        return when (
            value.uppercase(
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
                value
        }
    }

    private fun paymentMethodText(
        value: String?
    ): String {

        return when (
            value?.uppercase(
                Locale.ROOT
            )
        ) {

            "BANK_TRANSFER" ->
                "Chuyển khoản ngân hàng"

            "CASH" ->
                "Tiền mặt"

            "OTHER" ->
                "Khác"

            else ->
                "Chưa cập nhật"
        }
    }

    private fun formatMoney(
        value: BigDecimal
    ): String {

        return NumberFormat
            .getCurrencyInstance(
                Locale.forLanguageTag(
                    "vi-VN"
                )
            )
            .format(
                value
            )
    }

    private fun formatDate(
        value: String
    ): String {

        return runCatching {

            LocalDate
                .parse(
                    value
                )
                .format(
                    DateTimeFormatter.ofPattern(
                        "dd/MM/yyyy"
                    )
                )

        }.getOrElse {
            value
        }
    }

    private fun formatDateTime(
        value: String?
    ): String {

        if (
            value.isNullOrBlank()
        ) {
            return "-"
        }

        val formatter =
            DateTimeFormatter.ofPattern(
                "dd/MM/yyyy • HH:mm"
            )

        return runCatching {

            OffsetDateTime
                .parse(
                    value
                )
                .atZoneSameInstant(
                    appZone
                )
                .format(
                    formatter
                )

        }.recoverCatching {

            Instant
                .parse(
                    value
                )
                .atZone(
                    appZone
                )
                .format(
                    formatter
                )

        }.recoverCatching {

            LocalDateTime
                .parse(
                    value
                )
                .atZone(
                    appZone
                )
                .format(
                    formatter
                )

        }.getOrElse {
            value
        }
    }

    private fun setActionLoading(
        loading: Boolean
    ) {

        actionLoading =
            loading

        binding.btnConfirm.isEnabled =
            !loading

        binding.btnReject.isEnabled =
            !loading

        binding.btnCancel.isEnabled =
            !loading
    }

    private fun showActionError(
        message: String
    ) {

        Toast.makeText(
            this,
            message,
            Toast.LENGTH_LONG
        ).show()
    }

    private fun showLoading() {

        binding.progressDetail.isVisible =
            true

        binding.contentScroll.isVisible =
            false

        binding.errorContainer.isVisible =
            false
    }

    private fun showError(
        message: String
    ) {

        binding.progressDetail.isVisible =
            false

        binding.contentScroll.isVisible =
            false

        binding.errorContainer.isVisible =
            true

        binding.tvErrorMessage.text =
            message
    }

    override fun onResume() {
        super.onResume()

        if (
            ::repository.isInitialized &&
            !actionLoading
        ) {

            loadDetail()
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