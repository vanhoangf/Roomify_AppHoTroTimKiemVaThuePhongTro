package com.roomify.app.ui.landlord.appointment

import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.EditText
import android.widget.Toast

import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager

import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.roomify.app.R
import com.roomify.app.data.remote.api.LandlordViewingAppointmentApi
import com.roomify.app.data.remote.common.ApiResult
import com.roomify.app.data.remote.dto.appointments.ViewingAppointmentResponse
import com.roomify.app.data.remote.network.RetrofitClient
import com.roomify.app.data.repository.LandlordViewingAppointmentRepository
import com.roomify.app.databinding.ActivityLandlordViewingAppointmentDetailBinding
import com.roomify.app.ui.common.RoleSessionGuard
import com.google.android.material.datepicker.MaterialDatePicker

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

import java.time.Instant
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime


class LandlordViewingAppointmentDetailActivity :
    AppCompatActivity() {

    companion object {

        const val EXTRA_APPOINTMENT_ID =
            "extra_landlord_appointment_id"
    }



    private lateinit var binding:
            ActivityLandlordViewingAppointmentDetailBinding

    private lateinit var repository:
            LandlordViewingAppointmentRepository

    private var appointmentId =
        0L

    private val appZone =
        ZoneId.of(
            "Asia/Ho_Chi_Minh"
        )

    private val historyAdapter by lazy {
        LandlordViewingAppointmentHistoryAdapter()
    }

    private val formatter =
        DateTimeFormatter.ofPattern(
            "dd/MM/yyyy • HH:mm"
        )

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(
            savedInstanceState
        )

        binding =
            ActivityLandlordViewingAppointmentDetailBinding
                .inflate(
                    layoutInflater
                )

        setContentView(
            binding.root
        )

        setupInsets()

        appointmentId =
            intent.getLongExtra(
                EXTRA_APPOINTMENT_ID,
                0L
            )

        if (appointmentId <= 0L) {

            Toast.makeText(
                this,
                "Không tìm thấy lịch hẹn.",
                Toast.LENGTH_LONG
            ).show()

            finish()

            return
        }

        setupRepository()
        setupActions()
        setupHistory()
    }

    private fun loadHistory() {

        binding.progressHistory.isVisible =
            true

        binding.rvHistory.isVisible =
            false

        binding.tvHistoryEmpty.isVisible =
            false

        lifecycleScope.launch {

            try {

                when (
                    val result =
                        repository.getHistory(
                            appointmentId
                        )
                ) {

                    is ApiResult.Success -> {

                        val history =
                            result.data
                                ?: emptyList()

                        binding.progressHistory
                            .isVisible =
                            false

                        if (
                            history.isEmpty()
                        ) {

                            historyAdapter.submitList(
                                emptyList()
                            )

                            binding.rvHistory
                                .isVisible =
                                false

                            binding.tvHistoryEmpty
                                .isVisible =
                                true

                        } else {

                            historyAdapter.submitList(
                                history
                            )

                            binding.rvHistory
                                .isVisible =
                                true

                            binding.tvHistoryEmpty
                                .isVisible =
                                false
                        }
                    }

                    is ApiResult.Error -> {

                        binding.progressHistory
                            .isVisible =
                            false

                        binding.rvHistory
                            .isVisible =
                            false

                        binding.tvHistoryEmpty
                            .isVisible =
                            true

                        binding.tvHistoryEmpty.text =
                            "Không thể tải lịch sử xử lý."
                    }

                    ApiResult.Loading ->
                        Unit
                }

            } catch (
                e: CancellationException
            ) {

                throw e

            } catch (
                _: Exception
            ) {

                binding.progressHistory
                    .isVisible =
                    false

                binding.rvHistory
                    .isVisible =
                    false

                binding.tvHistoryEmpty
                    .isVisible =
                    true

                binding.tvHistoryEmpty.text =
                    "Không thể tải lịch sử xử lý."
            }
        }
    }

    private fun setupHistory() {

        binding.rvHistory.apply {

            layoutManager =
                LinearLayoutManager(
                    this@LandlordViewingAppointmentDetailActivity
                )

            adapter =
                historyAdapter

            isNestedScrollingEnabled =
                false
        }
    }

    private fun setupInsets() {

        ViewCompat.setOnApplyWindowInsetsListener(
            binding.landlordAppointmentDetailRoot
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
            LandlordViewingAppointmentRepository(
                RetrofitClient.createService(
                    LandlordViewingAppointmentApi::class.java
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

        binding.btnPropose
            .setOnClickListener {
                showProposeDatePicker()
            }

        binding.btnComplete
            .setOnClickListener {
                showCompleteDialog()
            }
    }

    private fun loadDetail() {

        showLoading()

        lifecycleScope.launch {

            try {

                when (
                    val result =
                        repository.getDetail(
                            appointmentId
                        )
                ) {

                    is ApiResult.Success -> {

                        val appointment =
                            result.data

                        if (appointment == null) {

                            showError(
                                "Không nhận được dữ liệu lịch hẹn."
                            )

                        } else {

                            bindAppointment(
                                appointment
                            )
                        }
                    }

                    is ApiResult.Error -> {

                        showError(
                            result.exception.message
                                ?: "Không thể tải lịch hẹn."
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
                        ?: "Không thể tải lịch hẹn."
                )
            }
        }
    }

    private fun bindAppointment(
        appointment:
        ViewingAppointmentResponse
    ) {

        binding.progressDetail
            .isVisible =
            false

        binding.errorContainer
            .isVisible =
            false

        binding.contentScroll
            .isVisible =
            true

        binding.tvRoomTitle.text =
            appointment.roomTitle

        binding.tvPropertyName.text =
            appointment.propertyName

        binding.tvStudentName.text =
            appointment.studentName
                .ifBlank {
                    "Sinh viên"
                }

        binding.tvStudentPhone.text =
            appointment.studentPhone
                ?.takeIf {
                    it.isNotBlank()
                }
                ?.let {
                    "SĐT: $it"
                }
                ?: "SĐT: Chưa cập nhật"

        binding.tvScheduledAt.text =
            formatDateTime(
                appointment.scheduledAt
            )

        binding.tvStudentNote.text =
            appointment.note
                ?.takeIf {
                    it.isNotBlank()
                }
                ?: "Sinh viên không để lại ghi chú."

        binding.tvLandlordNote.isVisible =
            !appointment.landlordNote
                .isNullOrBlank()

        binding.tvLandlordNote.text =
            "Phản hồi của bạn: " +
                    appointment.landlordNote
                        .orEmpty()

        val status =
            appointment.status.uppercase(
                Locale.ROOT
            )

        binding.proposedContainer
            .isVisible =
            status == "PROPOSED" &&
                    !appointment.proposedAt
                        .isNullOrBlank()

        if (
            binding.proposedContainer
                .isVisible
        ) {

            binding.tvProposedAt.text =
                formatDateTime(
                    appointment.proposedAt
                )
        }

        bindStatus(
            status
        )

        bindActions(
            status
        )

        loadHistory()
    }

    private fun bindStatus(
        status: String
    ) {

        val text =
            when (status) {

                "PENDING" ->
                    "Chờ xác nhận"

                "PROPOSED" ->
                    "Đang chờ sinh viên phản hồi"

                "CONFIRMED" ->
                    "Đã xác nhận"

                "REJECTED" ->
                    "Đã từ chối"

                "CANCELLED" ->
                    "Sinh viên đã hủy"

                "COMPLETED" ->
                    "Đã hoàn thành"

                else ->
                    status
            }

        val colorRes =
            when (status) {

                "CONFIRMED",
                "COMPLETED" ->
                    R.color.status_success

                "REJECTED",
                "CANCELLED" ->
                    R.color.status_error

                else ->
                    R.color.primary_blue
            }

        binding.tvStatus.text =
            text

        binding.tvStatus.setTextColor(
            ContextCompat.getColor(
                this,
                colorRes
            )
        )
    }

    private fun bindActions(
        status: String
    ) {

        binding.actionContainer.isVisible =
            status == "PENDING" ||
                    status == "PROPOSED" ||
                    status == "CONFIRMED"

        binding.btnConfirm.isVisible =
            status == "PENDING"

        binding.btnPropose.isVisible =
            status == "PENDING"

        binding.btnReject.isVisible =
            status == "PENDING" ||
                    status == "PROPOSED"

        binding.btnComplete.isVisible =
            status == "CONFIRMED"
    }

    private fun showProposeDatePicker() {

        val picker =
            MaterialDatePicker.Builder
                .datePicker()
                .setTitleText(
                    "Chọn ngày đề xuất"
                )
                .setSelection(
                    MaterialDatePicker.todayInUtcMilliseconds()
                )
                .build()

        picker.addOnPositiveButtonClickListener {
                selectedMillis ->

            val selectedDate =
                Instant.ofEpochMilli(
                    selectedMillis
                )
                    .atZone(
                        ZoneOffset.UTC
                    )
                    .toLocalDate()

            showProposeTimeDialog(
                selectedDate
            )
        }

        picker.show(
            supportFragmentManager,
            "landlord_propose_date"
        )
    }

    private fun showProposeTimeDialog(
        selectedDate: LocalDate
    ) {

        val slots =
            mutableListOf<String>()

        var time =
            LocalTime.of(
                8,
                0
            )

        val lastTime =
            LocalTime.of(
                19,
                0
            )

        while (
            !time.isAfter(
                lastTime
            )
        ) {

            slots.add(
                "%02d:%02d".format(
                    time.hour,
                    time.minute
                )
            )

            time =
                time.plusMinutes(
                    30
                )
        }

        MaterialAlertDialogBuilder(
            this
        )
            .setTitle(
                "Chọn giờ xem phòng"
            )
            .setItems(
                slots.toTypedArray()
            ) { _, which ->

                val selectedTime =
                    LocalTime.parse(
                        slots[which]
                    )

                showProposeNoteDialog(
                    selectedDate,
                    selectedTime
                )
            }
            .setNegativeButton(
                "Hủy",
                null
            )
            .show()
    }

    private fun showProposeNoteDialog(
        selectedDate: LocalDate,
        selectedTime: LocalTime
    ) {

        val proposedDateTime =
            selectedDate
                .atTime(
                    selectedTime
                )
                .atZone(
                    appZone
                )

        val now =
            java.time.ZonedDateTime.now(
                appZone
            )

        if (
            proposedDateTime.isBefore(
                now.plusHours(2)
            )
        ) {

            Toast.makeText(
                this,
                "Thời gian đề xuất phải cách hiện tại ít nhất 2 giờ.",
                Toast.LENGTH_LONG
            ).show()

            return
        }

        if (
            proposedDateTime.isAfter(
                now.plusDays(30)
            )
        ) {

            Toast.makeText(
                this,
                "Chỉ có thể đề xuất lịch trong 30 ngày tới.",
                Toast.LENGTH_LONG
            ).show()

            return
        }

        val input =
            createNoteInput(
                hint =
                    "Ghi chú cho sinh viên (không bắt buộc)"
            )

        val displayTime =
            DateTimeFormatter
                .ofPattern(
                    "dd/MM/yyyy • HH:mm"
                )
                .format(
                    proposedDateTime
                )

        MaterialAlertDialogBuilder(
            this
        )
            .setTitle(
                "Đề xuất thời gian mới"
            )
            .setMessage(
                "Thời gian đề xuất:\n$displayTime"
            )
            .setView(
                input
            )
            .setPositiveButton(
                "Gửi đề xuất"
            ) { _, _ ->

                val proposedAt =
                    proposedDateTime
                        .toOffsetDateTime()
                        .toString()

                proposeAppointment(
                    proposedAt =
                        proposedAt,
                    note =
                        input.text
                            ?.toString()
                )
            }
            .setNegativeButton(
                "Hủy",
                null
            )
            .show()
    }

    private fun proposeAppointment(
        proposedAt: String,
        note: String?
    ) {

        setActionLoading(
            true
        )

        lifecycleScope.launch {

            try {

                when (
                    val result =
                        repository.propose(
                            appointmentId =
                                appointmentId,
                            proposedAt =
                                proposedAt,
                            note =
                                note
                        )
                ) {

                    is ApiResult.Success -> {

                        Toast.makeText(
                            this@LandlordViewingAppointmentDetailActivity,
                            "Đã gửi thời gian đề xuất cho sinh viên.",
                            Toast.LENGTH_SHORT
                        ).show()

                        loadDetail()
                    }

                    is ApiResult.Error -> {

                        showActionError(
                            result.exception.message
                                ?: "Không thể đề xuất thời gian mới."
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

                showActionError(
                    e.message
                        ?: "Không thể đề xuất thời gian mới."
                )

            } finally {

                setActionLoading(
                    false
                )
            }
        }
    }

    private fun showCompleteDialog() {

        val input =
            createNoteInput(
                hint =
                    "Ghi chú sau khi xem phòng (không bắt buộc)"
            )

        MaterialAlertDialogBuilder(
            this
        )
            .setTitle(
                "Hoàn thành lịch xem"
            )
            .setMessage(
                "Xác nhận sinh viên đã hoàn thành buổi xem phòng?"
            )
            .setView(
                input
            )
            .setPositiveButton(
                "Hoàn thành"
            ) { _, _ ->

                completeAppointment(
                    input.text
                        ?.toString()
                )
            }
            .setNegativeButton(
                "Hủy",
                null
            )
            .show()
    }

    private fun completeAppointment(
        note: String?
    ) {

        setActionLoading(
            true
        )

        lifecycleScope.launch {

            try {

                when (
                    val result =
                        repository.complete(
                            appointmentId =
                                appointmentId,
                            note =
                                note
                        )
                ) {

                    is ApiResult.Success -> {

                        Toast.makeText(
                            this@LandlordViewingAppointmentDetailActivity,
                            "Đã hoàn thành lịch xem phòng.",
                            Toast.LENGTH_SHORT
                        ).show()

                        loadDetail()
                    }

                    is ApiResult.Error -> {

                        showActionError(
                            result.exception.message
                                ?: "Không thể hoàn thành lịch xem."
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

                showActionError(
                    e.message
                        ?: "Không thể hoàn thành lịch xem."
                )

            } finally {

                setActionLoading(
                    false
                )
            }
        }
    }

    private fun setActionLoading(
        loading: Boolean
    ) {

        binding.btnConfirm.isEnabled =
            !loading

        binding.btnReject.isEnabled =
            !loading

        binding.btnPropose.isEnabled =
            !loading

        binding.btnComplete.isEnabled =
            !loading
    }

    private fun showConfirmDialog() {

        val input =
            createNoteInput(
                hint =
                    "Ghi chú cho sinh viên (không bắt buộc)"
            )

        MaterialAlertDialogBuilder(
            this
        )
            .setTitle(
                "Xác nhận lịch xem"
            )
            .setMessage(
                "Bạn có chắc muốn xác nhận lịch xem phòng này?"
            )
            .setView(
                input
            )
            .setPositiveButton(
                "Xác nhận"
            ) { _, _ ->

                confirmAppointment(
                    input.text
                        ?.toString()
                )
            }
            .setNegativeButton(
                "Hủy",
                null
            )
            .show()
    }

    private fun showRejectDialog() {

        val input =
            createNoteInput(
                hint =
                    "Nhập lý do từ chối"
            )

        val dialog =
            MaterialAlertDialogBuilder(
                this
            )
                .setTitle(
                    "Từ chối lịch xem"
                )
                .setMessage(
                    "Vui lòng nhập lý do để sinh viên biết."
                )
                .setView(
                    input
                )
                .setPositiveButton(
                    "Từ chối",
                    null
                )
                .setNegativeButton(
                    "Hủy",
                    null
                )
                .create()

        dialog.setOnShowListener {

            dialog.getButton(
                android.app.AlertDialog.BUTTON_POSITIVE
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

                dialog.dismiss()

                rejectAppointment(
                    reason
                )
            }
        }

        dialog.show()
    }

    private fun createNoteInput(
        hint: String
    ): EditText {

        return EditText(
            this
        ).apply {

            this.hint =
                hint

            inputType =
                InputType.TYPE_CLASS_TEXT or
                        InputType.TYPE_TEXT_FLAG_MULTI_LINE

            minLines =
                2

            maxLines =
                4

            setPadding(
                48,
                24,
                48,
                24
            )
        }
    }

    private fun confirmAppointment(
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
                            appointmentId =
                                appointmentId,
                            note =
                                note
                        )
                ) {

                    is ApiResult.Success -> {

                        Toast.makeText(
                            this@LandlordViewingAppointmentDetailActivity,
                            "Đã xác nhận lịch xem phòng.",
                            Toast.LENGTH_SHORT
                        ).show()

                        loadDetail()
                    }

                    is ApiResult.Error -> {

                        showActionError(
                            result.exception.message
                                ?: "Không thể xác nhận lịch hẹn."
                        )
                    }

                    ApiResult.Loading ->
                        Unit
                }

            } catch (
                e: Exception
            ) {

                showActionError(
                    e.message
                        ?: "Không thể xác nhận lịch hẹn."
                )

            } finally {

                setActionLoading(
                    false
                )
            }
        }
    }

    private fun rejectAppointment(
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
                            appointmentId =
                                appointmentId,
                            reason =
                                reason
                        )
                ) {

                    is ApiResult.Success -> {

                        Toast.makeText(
                            this@LandlordViewingAppointmentDetailActivity,
                            "Đã từ chối lịch xem phòng.",
                            Toast.LENGTH_SHORT
                        ).show()

                        loadDetail()
                    }

                    is ApiResult.Error -> {

                        showActionError(
                            result.exception.message
                                ?: "Không thể từ chối lịch hẹn."
                        )
                    }

                    ApiResult.Loading ->
                        Unit
                }

            } catch (
                e: Exception
            ) {

                showActionError(
                    e.message
                        ?: "Không thể từ chối lịch hẹn."
                )

            } finally {

                setActionLoading(
                    false
                )
            }
        }
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

        binding.progressDetail
            .isVisible =
            true

        binding.contentScroll
            .isVisible =
            false

        binding.errorContainer
            .isVisible =
            false
    }

    private fun showError(
        message: String
    ) {

        binding.progressDetail
            .isVisible =
            false

        binding.contentScroll
            .isVisible =
            false

        binding.errorContainer
            .isVisible =
            true

        binding.tvErrorMessage.text =
            message
    }

    private fun formatDateTime(
        value: String?
    ): String {

        if (
            value.isNullOrBlank()
        ) {
            return "Chưa cập nhật"
        }

        val instant =
            runCatching {

                Instant.parse(
                    value
                )

            }.getOrNull()
                ?: runCatching {

                    OffsetDateTime
                        .parse(
                            value
                        )
                        .toInstant()

                }.getOrNull()
                ?: runCatching {

                    LocalDateTime
                        .parse(
                            value
                        )
                        .toInstant(
                            ZoneOffset.UTC
                        )

                }.getOrNull()

        return if (
            instant != null
        ) {

            formatter.format(
                instant.atZone(
                    appZone
                )
            )

        } else {

            value
        }
    }

    override fun onResume() {
        super.onResume()

        if (
            ::repository.isInitialized
        ) {

            loadDetail()
        }
    }

    override fun onStart() {
        super.onStart()

        RoleSessionGuard.verify(
            activity = this,
            expectedRole = "LANDLORD",
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