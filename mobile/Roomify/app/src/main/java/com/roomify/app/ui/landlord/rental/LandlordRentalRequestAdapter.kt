package com.roomify.app.ui.landlord.rental

import android.view.LayoutInflater
import android.view.ViewGroup

import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

import com.roomify.app.R
import com.roomify.app.data.remote.dto.rentals.RentalRequestResponse
import com.roomify.app.databinding.ItemLandlordRentalRequestBinding

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class LandlordRentalRequestAdapter(
    private val onClick:
        (RentalRequestResponse) -> Unit
) : ListAdapter<
        RentalRequestResponse,
        LandlordRentalRequestAdapter.RequestViewHolder
        >(DiffCallback) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RequestViewHolder {

        val binding =
            ItemLandlordRentalRequestBinding.inflate(
                LayoutInflater.from(
                    parent.context
                ),
                parent,
                false
            )

        return RequestViewHolder(
            binding
        )
    }

    override fun onBindViewHolder(
        holder: RequestViewHolder,
        position: Int
    ) {

        holder.bind(
            getItem(position)
        )
    }

    inner class RequestViewHolder(
        private val binding:
        ItemLandlordRentalRequestBinding
    ) : RecyclerView.ViewHolder(
        binding.root
    ) {

        fun bind(
            item: RentalRequestResponse
        ) {

            binding.tvStudentName.text =
                item.studentName
                    .ifBlank {
                        "Sinh viên"
                    }

            binding.tvRoomTitle.text =
                item.roomTitle

            binding.tvPropertyName.text =
                item.propertyName

            binding.tvMoveInDate.text =
                formatDate(
                    item.requestedMoveInDate
                )

            binding.tvDuration.text =
                item.durationMonths
                    ?.let {
                        "Thời hạn dự kiến: $it tháng"
                    }
                    ?: "Chưa xác định thời hạn thuê"

            binding.tvStudentPhone.text =
                item.studentPhone
                    ?.takeIf {
                        it.isNotBlank()
                    }
                    ?.let {
                        "SĐT: $it"
                    }
                    ?: "SĐT: Chưa cập nhật"

            binding.tvNote.isVisible =
                !item.note.isNullOrBlank()

            binding.tvNote.text =
                "Lời nhắn: ${item.note.orEmpty()}"

            binding.tvLandlordNote.isVisible =
                !item.landlordNote.isNullOrBlank()

            binding.tvLandlordNote.text =
                "Phản hồi của bạn: " +
                        item.landlordNote.orEmpty()

            bindStatus(
                item.status
            )

            binding.cardLandlordRentalRequest
                .setOnClickListener {

                    onClick(
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

            binding.chipStatus.text =
                when (normalized) {

                    "PENDING" ->
                        "Chờ xử lý"

                    "APPROVED" ->
                        "Đã duyệt"

                    "REJECTED" ->
                        "Đã từ chối"

                    "CANCELLED" ->
                        "Đã hủy"

                    else ->
                        normalized
                }

            val colorRes =
                when (normalized) {

                    "APPROVED" ->
                        R.color.status_success

                    "REJECTED",
                    "CANCELLED" ->
                        R.color.status_error

                    else ->
                        R.color.primary_blue
                }

            binding.chipStatus.setTextColor(
                ContextCompat.getColor(
                    binding.root.context,
                    colorRes
                )
            )
        }
    }

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

    companion object {

        private val DiffCallback =
            object :
                DiffUtil.ItemCallback<
                        RentalRequestResponse
                        >() {

                override fun areItemsTheSame(
                    oldItem:
                    RentalRequestResponse,
                    newItem:
                    RentalRequestResponse
                ): Boolean {

                    return oldItem.rentalRequestId ==
                            newItem.rentalRequestId
                }

                override fun areContentsTheSame(
                    oldItem:
                    RentalRequestResponse,
                    newItem:
                    RentalRequestResponse
                ): Boolean {

                    return oldItem ==
                            newItem
                }
            }
    }
}