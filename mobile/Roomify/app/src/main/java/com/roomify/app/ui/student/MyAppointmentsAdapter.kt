package com.roomify.app.ui.student

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

import com.roomify.app.R
import com.roomify.app.data.remote.dto.appointments.ViewingAppointmentResponse
import com.roomify.app.databinding.ItemViewingAppointmentBinding

import java.time.Instant
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

class MyAppointmentsAdapter(
    private val onCancel:
        (ViewingAppointmentResponse) -> Unit,
    private val onAcceptProposal:
        (ViewingAppointmentResponse) -> Unit
) : ListAdapter<
        ViewingAppointmentResponse,
        MyAppointmentsAdapter.AppointmentViewHolder
        >(DIFF) {

    private val appZone =
        ZoneId.of(
            "Asia/Ho_Chi_Minh"
        )

    private val formatter =
        DateTimeFormatter.ofPattern(
            "dd/MM/yyyy • HH:mm"
        )

    inner class AppointmentViewHolder(
        private val binding:
        ItemViewingAppointmentBinding
    ) : RecyclerView.ViewHolder(
        binding.root
    ) {

        fun bind(
            item: ViewingAppointmentResponse
        ) {

            binding.tvRoomTitle.text =
                item.roomTitle

            binding.tvPropertyName.text =
                item.propertyName

            binding.tvScheduledAt.text =
                formatDateTime(
                    item.scheduledAt
                )

            bindStatus(
                item.status
            )

            val proposed =
                item.proposedAt

            binding.proposedContainer.visibility =
                if (
                    item.status.equals(
                        "PROPOSED",
                        true
                    ) &&
                    !proposed.isNullOrBlank()
                ) {
                    View.VISIBLE
                } else {
                    View.GONE
                }

            binding.tvProposedAt.text =
                formatDateTime(
                    proposed
                )

            binding.tvNote.visibility =
                if (
                    item.note.isNullOrBlank()
                ) {
                    View.GONE
                } else {
                    View.VISIBLE
                }

            binding.tvNote.text =
                "Ghi chú của bạn: ${item.note.orEmpty()}"

            binding.tvLandlordNote.visibility =
                if (
                    item.landlordNote
                        .isNullOrBlank()
                ) {
                    View.GONE
                } else {
                    View.VISIBLE
                }

            binding.tvLandlordNote.text =
                "Phản hồi chủ trọ: " +
                        item.landlordNote.orEmpty()

            val status =
                item.status.uppercase(
                    Locale.ROOT
                )

            binding.btnAcceptProposal.visibility =
                if (
                    status == "PROPOSED"
                ) {
                    View.VISIBLE
                } else {
                    View.GONE
                }

            binding.btnCancel.visibility =
                if (
                    status in setOf(
                        "PENDING",
                        "PROPOSED",
                        "CONFIRMED"
                    )
                ) {
                    View.VISIBLE
                } else {
                    View.GONE
                }

            binding.actionContainer.visibility =
                if (
                    binding.btnCancel.visibility ==
                    View.VISIBLE ||
                    binding.btnAcceptProposal.visibility ==
                    View.VISIBLE
                ) {
                    View.VISIBLE
                } else {
                    View.GONE
                }

            binding.btnCancel
                .setOnClickListener {

                    onCancel(
                        item
                    )
                }

            binding.btnAcceptProposal
                .setOnClickListener {

                    onAcceptProposal(
                        item
                    )
                }
        }

        private fun bindStatus(
            status: String
        ) {

            val normalized =
                status.uppercase(
                    Locale.ROOT
                )

            val text =
                when (normalized) {

                    "PENDING" ->
                        "Chờ xác nhận"

                    "PROPOSED" ->
                        "Có giờ đề xuất"

                    "CONFIRMED" ->
                        "Đã xác nhận"

                    "CANCELLED" ->
                        "Đã hủy"

                    "REJECTED" ->
                        "Bị từ chối"

                    "COMPLETED" ->
                        "Đã hoàn thành"

                    else ->
                        normalized
                }

            val colorRes =
                when (normalized) {

                    "CONFIRMED",
                    "COMPLETED" ->
                        R.color.status_success

                    "REJECTED",
                    "CANCELLED" ->
                        R.color.status_error

                    else ->
                        R.color.primary_blue
                }

            binding.chipStatus.text =
                text

            binding.chipStatus.setTextColor(
                ContextCompat.getColor(
                    binding.root.context,
                    colorRes
                )
            )
        }
    }

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
                        .parse(value)
                        .toInstant()
                }.getOrNull()
                ?: runCatching {
                    LocalDateTime
                        .parse(value)
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

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AppointmentViewHolder {

        val binding =
            ItemViewingAppointmentBinding.inflate(
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

    companion object {

        private val DIFF =
            object :
                DiffUtil.ItemCallback<
                        ViewingAppointmentResponse
                        >() {

                override fun areItemsTheSame(
                    oldItem:
                    ViewingAppointmentResponse,
                    newItem:
                    ViewingAppointmentResponse
                ): Boolean {

                    return oldItem
                        .appointmentId ==
                            newItem
                                .appointmentId
                }

                override fun areContentsTheSame(
                    oldItem:
                    ViewingAppointmentResponse,
                    newItem:
                    ViewingAppointmentResponse
                ): Boolean {

                    return oldItem ==
                            newItem
                }
            }
    }
}