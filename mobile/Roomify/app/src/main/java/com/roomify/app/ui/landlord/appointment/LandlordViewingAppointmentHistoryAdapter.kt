package com.roomify.app.ui.landlord.appointment

import android.view.LayoutInflater
import android.view.ViewGroup

import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

import com.roomify.app.data.remote.dto.landlord.LandlordViewingAppointmentHistoryResponse
import com.roomify.app.databinding.ItemLandlordViewingAppointmentHistoryBinding

import java.time.Instant
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

class LandlordViewingAppointmentHistoryAdapter :
    ListAdapter<
            LandlordViewingAppointmentHistoryResponse,
            LandlordViewingAppointmentHistoryAdapter.HistoryViewHolder
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
    ): HistoryViewHolder {

        val binding =
            ItemLandlordViewingAppointmentHistoryBinding.inflate(
                LayoutInflater.from(
                    parent.context
                ),
                parent,
                false
            )

        return HistoryViewHolder(
            binding
        )
    }

    override fun onBindViewHolder(
        holder: HistoryViewHolder,
        position: Int
    ) {

        holder.bind(
            getItem(position)
        )
    }

    inner class HistoryViewHolder(
        private val binding:
        ItemLandlordViewingAppointmentHistoryBinding
    ) : RecyclerView.ViewHolder(
        binding.root
    ) {

        fun bind(
            item:
            LandlordViewingAppointmentHistoryResponse
        ) {

            binding.tvHistoryAction.text =
                actionText(
                    item.action
                )

            binding.tvHistoryStatus.text =
                buildStatusText(
                    item.oldStatus,
                    item.newStatus
                )

            binding.tvHistoryTime.text =
                formatDateTime(
                    item.createdAt
                )

            binding.tvHistoryNote.isVisible =
                !item.note
                    .isNullOrBlank()

            binding.tvHistoryNote.text =
                "Ghi chú: ${item.note.orEmpty()}"
        }
    }

    private fun actionText(
        action: String
    ): String {

        return when (
            action.uppercase(
                Locale.ROOT
            )
        ) {

            "CREATED" ->
                "Sinh viên tạo lịch hẹn"

            "CONFIRM" ->
                "Chủ trọ xác nhận lịch"

            "REJECT" ->
                "Chủ trọ từ chối lịch"

            "PROPOSE" ->
                "Chủ trọ đề xuất thời gian mới"

            "ACCEPT_PROPOSAL" ->
                "Sinh viên chấp nhận thời gian đề xuất"

            "CANCEL" ->
                "Sinh viên hủy lịch"

            "COMPLETE" ->
                "Chủ trọ hoàn thành lịch xem"

            else ->
                action
        }
    }

    private fun buildStatusText(
        oldStatus: String?,
        newStatus: String
    ): String {

        return if (
            oldStatus.isNullOrBlank()
        ) {

            statusText(
                newStatus
            )

        } else {

            "${statusText(oldStatus)} → " +
                    statusText(newStatus)
        }
    }

    private fun statusText(
        status: String
    ): String {

        return when (
            status.uppercase(
                Locale.ROOT
            )
        ) {

            "PENDING" ->
                "Chờ xác nhận"

            "PROPOSED" ->
                "Đang đề xuất"

            "CONFIRMED" ->
                "Đã xác nhận"

            "REJECTED" ->
                "Đã từ chối"

            "CANCELLED" ->
                "Đã hủy"

            "COMPLETED" ->
                "Đã hoàn thành"

            else ->
                status
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

    companion object {

        private val DiffCallback =
            object :
                DiffUtil.ItemCallback<
                        LandlordViewingAppointmentHistoryResponse
                        >() {

                override fun areItemsTheSame(
                    oldItem:
                    LandlordViewingAppointmentHistoryResponse,
                    newItem:
                    LandlordViewingAppointmentHistoryResponse
                ): Boolean {

                    return oldItem.appointmentHistoryId ==
                            newItem.appointmentHistoryId
                }

                override fun areContentsTheSame(
                    oldItem:
                    LandlordViewingAppointmentHistoryResponse,
                    newItem:
                    LandlordViewingAppointmentHistoryResponse
                ): Boolean {

                    return oldItem ==
                            newItem
                }
            }
    }
}