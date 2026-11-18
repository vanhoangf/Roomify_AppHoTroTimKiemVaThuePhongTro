package com.roomify.app.ui.student

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

import com.roomify.app.R
import com.roomify.app.data.remote.dto.rentals.RentalRequestResponse
import com.roomify.app.databinding.ItemRentalRequestBinding

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class MyRentalRequestsAdapter(
    private val onCancel:
        (RentalRequestResponse) -> Unit
) : ListAdapter<
        RentalRequestResponse,
        MyRentalRequestsAdapter.RequestViewHolder
        >(DIFF) {

    inner class RequestViewHolder(
        private val binding:
        ItemRentalRequestBinding
    ) : RecyclerView.ViewHolder(
        binding.root
    ) {

        fun bind(
            item: RentalRequestResponse
        ) {

            binding.tvRoomTitle.text =
                item.roomTitle

            binding.tvPropertyName.text =
                item.propertyName

            binding.tvMoveInDate.text =
                formatDate(
                    item.requestedMoveInDate
                )

            binding.tvDuration.text =
                item.durationMonths?.let {
                    "Thời hạn dự kiến: $it tháng"
                }
                    ?: "Chưa xác định thời hạn thuê"

            binding.tvNote.visibility =
                if (
                    item.note.isNullOrBlank()
                ) {
                    View.GONE
                } else {
                    View.VISIBLE
                }

            binding.tvNote.text =
                "Lời nhắn: ${item.note.orEmpty()}"

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

            binding.chipStatus.text =
                when (status) {

                    "PENDING" ->
                        "Chờ xử lý"

                    "APPROVED" ->
                        "Đã chấp nhận"

                    "REJECTED" ->
                        "Bị từ chối"

                    "CANCELLED" ->
                        "Đã hủy"

                    else ->
                        status
                }

            val color =
                when (status) {

                    "APPROVED" ->
                        R.color.status_success

                    "REJECTED",
                    "CANCELLED" ->
                        R.color.status_error

                    else ->
                        R.color.primary_blue
                }

            binding.chipStatus
                .setTextColor(
                    ContextCompat.getColor(
                        binding.root.context,
                        color
                    )
                )

            binding.btnCancel.visibility =
                if (
                    status == "PENDING"
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
        }
    }

    private fun formatDate(
        value: String
    ): String {

        return runCatching {

            LocalDate.parse(
                value
            ).format(
                DateTimeFormatter
                    .ofPattern(
                        "dd/MM/yyyy"
                    )
            )

        }.getOrElse {

            value
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RequestViewHolder {

        return RequestViewHolder(
            ItemRentalRequestBinding.inflate(
                LayoutInflater.from(
                    parent.context
                ),
                parent,
                false
            )
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

    companion object {

        private val DIFF =
            object :
                DiffUtil.ItemCallback<
                        RentalRequestResponse
                        >() {

                override fun areItemsTheSame(
                    oldItem: RentalRequestResponse,
                    newItem: RentalRequestResponse
                ) =
                    oldItem.rentalRequestId ==
                            newItem.rentalRequestId

                override fun areContentsTheSame(
                    oldItem: RentalRequestResponse,
                    newItem: RentalRequestResponse
                ) =
                    oldItem == newItem
            }
    }
}