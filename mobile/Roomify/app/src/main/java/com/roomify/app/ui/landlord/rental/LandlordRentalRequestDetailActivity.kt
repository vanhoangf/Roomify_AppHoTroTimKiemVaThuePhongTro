package com.roomify.app.ui.landlord.rental

import android.content.DialogInterface
import android.content.Intent
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
import com.roomify.app.data.remote.api.LandlordRentalRequestApi
import com.roomify.app.data.remote.common.ApiResult
import com.roomify.app.data.remote.dto.rentals.RentalRequestResponse
import com.roomify.app.data.remote.network.RetrofitClient
import com.roomify.app.data.repository.LandlordRentalRequestRepository
import com.roomify.app.databinding.ActivityLandlordRentalRequestDetailBinding
import com.roomify.app.ui.common.RoleSessionGuard
import com.roomify.app.ui.landlord.contract.LandlordCreateContractActivity

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

class LandlordRentalRequestDetailActivity :
    AppCompatActivity() {

    companion object {

        const val EXTRA_RENTAL_REQUEST_ID =
            "extra_landlord_rental_request_id"
    }

    private lateinit var binding:
            ActivityLandlordRentalRequestDetailBinding

    private lateinit var repository:
            LandlordRentalRequestRepository

    private var rentalRequestId =
        0L

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
            ActivityLandlordRentalRequestDetailBinding.inflate(
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

        if (rentalRequestId <= 0L) {

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

    // ============================================================
    // INSETS
    // ============================================================

    private fun setupInsets() {

        ViewCompat.setOnApplyWindowInsetsListener(
            binding.landlordRentalRequestDetailRoot
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

    // ============================================================
    // REPOSITORY
    // ============================================================

    private fun setupRepository() {

        RetrofitClient.initialize(
            applicationContext
        )

        repository =
            LandlordRentalRequestRepository(
                RetrofitClient.createService(
                    LandlordRentalRequestApi::class.java
                )
            )
    }

    // ============================================================
    // ACTIONS
    // ============================================================

    private fun setupActions() {

        binding.btnBack
            .setOnClickListener {

                finish()
            }

        binding.btnRetry
            .setOnClickListener {

                loadDetail()
            }

        binding.btnApprove
            .setOnClickListener {

                showApproveDialog()
            }

        binding.btnReject
            .setOnClickListener {

                showRejectDialog()
            }

        binding.btnCreateContract
            .setOnClickListener {

                startActivity(
                    Intent(
                        this,
                        LandlordCreateContractActivity::class.java
                    ).apply {

                        putExtra(
                            LandlordCreateContractActivity
                                .EXTRA_RENTAL_REQUEST_ID,
                            rentalRequestId
                        )
                    }
                )
            }
    }

    // ============================================================
    // LOAD DETAIL
    // ============================================================

    private fun loadDetail() {

        if (actionLoading) {
            return
        }

        showLoading()

        lifecycleScope.launch {

            try {

                when (
                    val result =
                        repository.getDetail(
                            rentalRequestId
                        )
                ) {

                    is ApiResult.Success -> {

                        val request =
                            result.data

                        if (request == null) {

                            showError(
                                "Không nhận được dữ liệu yêu cầu thuê."
                            )

                        } else {

                            bindRequest(
                                request
                            )
                        }
                    }

                    is ApiResult.Error -> {

                        showError(
                            result.exception.message
                                ?: "Không thể tải yêu cầu thuê."
                        )
                    }

                    ApiResult.Loading -> Unit
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
                        ?: "Không thể tải yêu cầu thuê."
                )
            }
        }
    }

    // ============================================================
    // BIND DATA
    // ============================================================

    private fun bindRequest(
        request: RentalRequestResponse
    ) {

        binding.progressDetail.isVisible =
            false

        binding.errorContainer.isVisible =
            false

        binding.contentScroll.isVisible =
            true

        binding.tvStudentName.text =
            request.studentName.ifBlank {
                "Sinh viên"
            }

        binding.tvStudentPhone.text =
            request.studentPhone
                ?.trim()
                ?.takeIf {
                    it.isNotBlank()
                }
                ?.let {
                    "SĐT: $it"
                }
                ?: "SĐT: Chưa cập nhật"

        binding.tvRoomTitle.text =
            request.roomTitle

        binding.tvPropertyName.text =
            request.propertyName

        binding.tvMoveInDate.text =
            formatDate(
                request.requestedMoveInDate
            )

        binding.tvDuration.text =
            request.durationMonths
                ?.let {
                    "$it tháng"
                }
                ?: "Chưa xác định"

        binding.tvStudentNote.text =
            request.note
                ?.trim()
                ?.takeIf {
                    it.isNotBlank()
                }
                ?: "Sinh viên không để lại lời nhắn."

        binding.landlordResponseContainer.isVisible =
            !request.landlordNote.isNullOrBlank()

        binding.tvLandlordNote.text =
            request.landlordNote
                ?.trim()
                .orEmpty()

        binding.respondedContainer.isVisible =
            !request.respondedAt.isNullOrBlank()

        if (
            !request.respondedAt.isNullOrBlank()
        ) {

            binding.tvRespondedAt.text =
                formatDateTime(
                    request.respondedAt
                )
        }

        val status =
            request.status.uppercase(
                Locale.ROOT
            )

        bindStatus(
            status
        )

        bindActions(
            status
        )
    }

    // ============================================================
    // STATUS
    // ============================================================

    private fun bindStatus(
        status: String
    ) {

        val statusText =
            when (status) {

                "PENDING" ->
                    "Chờ xử lý"

                "APPROVED" ->
                    "Đã duyệt"

                "REJECTED" ->
                    "Đã từ chối"

                "CANCELLED" ->
                    "Sinh viên đã hủy"

                else ->
                    status
            }

        val colorRes =
            when (status) {

                "APPROVED" ->
                    R.color.status_success

                "REJECTED",
                "CANCELLED" ->
                    R.color.status_error

                else ->
                    R.color.primary_blue
            }

        binding.tvStatus.text =
            statusText

        binding.tvStatus.setTextColor(
            ContextCompat.getColor(
                this,
                colorRes
            )
        )
    }

    // ============================================================
    // ACTION VISIBILITY
    // ============================================================

    private fun bindActions(
        status: String
    ) {

        binding.actionContainer.isVisible =
            status == "PENDING" ||
                    status == "APPROVED"

        binding.btnApprove.isVisible =
            status == "PENDING"

        binding.btnReject.isVisible =
            status == "PENDING"

        binding.btnCreateContract.isVisible =
            status == "APPROVED"
    }

    // ============================================================
    // APPROVE
    // ============================================================

    private fun showApproveDialog() {

        if (actionLoading) {
            return
        }

        val input =
            createNoteInput(
                "Ghi chú cho sinh viên (không bắt buộc)"
            )

        MaterialAlertDialogBuilder(
            this
        )
            .setTitle(
                "Duyệt yêu cầu thuê?"
            )
            .setMessage(
                "Yêu cầu sẽ được chuyển sang trạng thái Đã duyệt."
            )
            .setView(
                input
            )
            .setNegativeButton(
                "Hủy",
                null
            )
            .setPositiveButton(
                "Duyệt"
            ) { _, _ ->

                approveRequest(
                    input.text
                        ?.toString()
                )
            }
            .show()
    }

    private fun approveRequest(
        note: String?
    ) {

        if (actionLoading) {
            return
        }

        setActionLoading(
            true
        )

        lifecycleScope.launch {

            try {

                when (
                    val result =
                        repository.approve(
                            rentalRequestId =
                                rentalRequestId,
                            note =
                                note
                        )
                ) {

                    is ApiResult.Success -> {

                        Toast.makeText(
                            this@LandlordRentalRequestDetailActivity,
                            "Đã duyệt yêu cầu thuê.",
                            Toast.LENGTH_SHORT
                        ).show()

                        val data =
                            result.data

                        if (data != null) {

                            bindRequest(
                                data
                            )

                        } else {

                            loadDetail()
                        }
                    }

                    is ApiResult.Error -> {

                        showActionError(
                            result.exception.message
                                ?: "Không thể duyệt yêu cầu thuê."
                        )
                    }

                    ApiResult.Loading -> Unit
                }

            } catch (
                e: CancellationException
            ) {

                throw e

            } catch (
                e: Exception
            ) {

                showActionError(
                    e.message
                        ?: "Không thể duyệt yêu cầu thuê."
                )

            } finally {

                setActionLoading(
                    false
                )
            }
        }
    }

    // ============================================================
    // REJECT
    // ============================================================

    private fun showRejectDialog() {

        if (actionLoading) {
            return
        }

        val input =
            createNoteInput(
                "Nhập lý do từ chối"
            )

        val dialog =
            MaterialAlertDialogBuilder(
                this
            )
                .setTitle(
                    "Từ chối yêu cầu thuê?"
                )
                .setMessage(
                    "Vui lòng nhập lý do để sinh viên biết nguyên nhân."
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

                if (
                    reason.isBlank()
                ) {

                    input.error =
                        "Vui lòng nhập lý do từ chối."

                    return@setOnClickListener
                }

                if (
                    reason.length > 1500
                ) {

                    input.error =
                        "Lý do tối đa 1500 ký tự."

                    return@setOnClickListener
                }

                dialog.dismiss()

                rejectRequest(
                    reason
                )
            }
        }

        dialog.show()
    }

    private fun rejectRequest(
        reason: String
    ) {

        if (actionLoading) {
            return
        }

        setActionLoading(
            true
        )

        lifecycleScope.launch {

            try {

                when (
                    val result =
                        repository.reject(
                            rentalRequestId =
                                rentalRequestId,
                            reason =
                                reason
                        )
                ) {

                    is ApiResult.Success -> {

                        Toast.makeText(
                            this@LandlordRentalRequestDetailActivity,
                            "Đã từ chối yêu cầu thuê.",
                            Toast.LENGTH_SHORT
                        ).show()

                        val data =
                            result.data

                        if (data != null) {

                            bindRequest(
                                data
                            )

                        } else {

                            loadDetail()
                        }
                    }

                    is ApiResult.Error -> {

                        showActionError(
                            result.exception.message
                                ?: "Không thể từ chối yêu cầu thuê."
                        )
                    }

                    ApiResult.Loading -> Unit
                }

            } catch (
                e: CancellationException
            ) {

                throw e

            } catch (
                e: Exception
            ) {

                showActionError(
                    e.message
                        ?: "Không thể từ chối yêu cầu thuê."
                )

            } finally {

                setActionLoading(
                    false
                )
            }
        }
    }

    // ============================================================
    // INPUT
    // ============================================================

    private fun createNoteInput(
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

    // ============================================================
    // FORMAT
    // ============================================================

    private fun formatDate(
        value: String
    ): String {

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

    private fun formatDateTime(
        value: String?
    ): String {

        if (value.isNullOrBlank()) {
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

    // ============================================================
    // UI STATE
    // ============================================================

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

    private fun setActionLoading(
        loading: Boolean
    ) {

        actionLoading =
            loading

        binding.btnApprove.isEnabled =
            !loading

        binding.btnReject.isEnabled =
            !loading

        binding.btnCreateContract.isEnabled =
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

    // ============================================================
    // LIFECYCLE
    // ============================================================

    override fun onResume() {
        super.onResume()

        if (
            ::repository.isInitialized &&
            rentalRequestId > 0L &&
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