package com.roomify.app.ui.landlord.appointment

import android.view.LayoutInflater
import android.view.ViewGroup

import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

import com.roomify.app.R
import com.roomify.app.data.remote.dto.appointments.ViewingAppointmentResponse
import com.roomify.app.databinding.ItemLandlordViewingAppointmentBinding

import java.time.Instant
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

class LandlordViewingAppointmentAdapter(
    private val onClick:
        (ViewingAppointmentResponse) -> Unit,
    private val onConfirm:
        (ViewingAppointmentResponse) -> Unit,
    private val onPropose:
        (ViewingAppointmentResponse) -> Unit,
    private val onReject:
        (ViewingAppointmentResponse) -> Unit
) : ListAdapter<
        ViewingAppointmentResponse,
        LandlordViewingAppointmentAdapter.AppointmentViewHolder
        >(DiffCallback) {

    private val appZone =
        ZoneId.of(
            "Asia/Ho_Chi_Minh"
        )

    private val formatter =
        DateTimeFormatter.ofPattern(
            "dd/MM/yyyy • HH:mm"
        )

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AppointmentViewHolder {

        val binding =
            ItemLandlordViewingAppointmentBinding.inflate(
                LayoutInflater.from(
                    parent.context
                ),
                parent,
                false
            )

        return AppointmentViewHolder(
            binding
        )
    }

    override fun onBindViewHolder(
        holder: AppointmentViewHolder,
        position: Int
    ) {

        holder.bind(
            getItem(position)
        )
    }

    inner class AppointmentViewHolder(
        private val binding:
        ItemLandlordViewingAppointmentBinding
    ) : RecyclerView.ViewHolder(
        binding.root
    ) {

        fun bind(
            item: ViewingAppointmentResponse
        ) {

            binding.tvLandlordStudentName.text =
                item.studentName
                    .ifBlank {
                        "Sinh viên"
                    }

            binding.tvLandlordRoomTitle.text =
                item.roomTitle

            binding.tvLandlordPropertyName.text =
                item.propertyName

            binding.tvLandlordScheduledAt.text =
                formatDateTime(
                    item.scheduledAt
                )

            binding.tvLandlordStudentPhone.text =
                item.studentPhone
                    ?.takeIf {
                        it.isNotBlank()
                    }
                    ?.let {
                        "SĐT: $it"
                    }
                    ?: "SĐT: Chưa cập nhật"

            binding.tvLandlordStudentNote.isVisible =
                !item.note.isNullOrBlank()

            binding.tvLandlordStudentNote.text =
                "Ghi chú: ${item.note.orEmpty()}"

            val status =
                item.status.uppercase(
                    Locale.ROOT
                )

            // =====================================================
            // ACTION VISIBILITY
            // =====================================================

            binding.actionContainer.isVisible =
                status == "PENDING" ||
                        status == "PROPOSED"

            binding.btnConfirm.isVisible =
                status == "PENDING"

            binding.btnPropose.isVisible =
                status == "PENDING"

            binding.btnReject.isVisible =
                status == "PENDING" ||
                        status == "PROPOSED"

            /*
             * Hoàn thành lịch xem chỉ xử lý trong màn Detail.
             * Không hiện nút này ở item danh sách.
             */
            binding.btnComplete.isVisible =
                false

            // =====================================================
            // PROPOSED TIME
            // =====================================================

            val proposedVisible =
                status == "PROPOSED" &&
                        !item.proposedAt.isNullOrBlank()

            binding.proposedContainer.isVisible =
                proposedVisible

            if (proposedVisible) {

                binding.tvLandlordProposedAt.text =
                    formatDateTime(
                        item.proposedAt
                    )
            }

            // =====================================================
            // STATUS
            // =====================================================

            bindStatus(
                status
            )

            // =====================================================
            // CLICK EVENTS
            // =====================================================

            binding.cardLandlordAppointment
                .setOnClickListener {

                    onClick(
                        item
                    )
                }

            binding.btnConfirm
                .setOnClickListener {

                    onConfirm(
                        item
                    )
                }

            binding.btnPropose
                .setOnClickListener {

                    onPropose(
                        item
                    )
                }

            binding.btnReject
                .setOnClickListener {

                    onReject(
                        item
                    )
                }
        }

        private fun bindStatus(
            status: String
        ) {

            val text =
                when (status) {

                    "PENDING" ->
                        "Chờ xác nhận"

                    "PROPOSED" ->
                        "Chờ sinh viên phản hồi"

                    "CONFIRMED" ->
                        "Đã xác nhận"

                    "CANCELLED" ->
                        "Sinh viên đã hủy"

                    "REJECTED" ->
                        "Đã từ chối"

                    "COMPLETED" ->
                        "Đã hoàn thành"

                    else ->
                        status
                }

            val color =
                when (status) {

                    "CONFIRMED",
                    "COMPLETED" ->
                        R.color.status_success

                    "CANCELLED",
                    "REJECTED" ->
                        R.color.status_error

                    else ->
                        R.color.primary_blue
                }

            binding.tvLandlordAppointmentStatus.text =
                text

            binding.tvLandlordAppointmentStatus
                .setTextColor(
                    ContextCompat.getColor(
                        binding.root.context,
                        color
                    )
                )
        }
    }

    // ============================================================
    // DATE TIME
    // ============================================================

    private fun formatDateTime(
        value: String?
    ): String {

        if (value.isNullOrBlank()) {
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

        return if (instant != null) {

            formatter.format(
                instant.atZone(
                    appZone
                )
            )

        } else {

            value
        }
    }

    companion object {

        private val DiffCallback =
            object :
                DiffUtil.ItemCallback<
                        ViewingAppointmentResponse
                        >() {

                override fun areItemsTheSame(
                    oldItem: ViewingAppointmentResponse,
                    newItem: ViewingAppointmentResponse
                ): Boolean {

                    return oldItem.appointmentId ==
                            newItem.appointmentId
                }

                override fun areContentsTheSame(
                    oldItem: ViewingAppointmentResponse,
                    newItem: ViewingAppointmentResponse
                ): Boolean {

                    return oldItem ==
                            newItem
                }
            }
    }
}