package com.roomify.app.ui.landlord.room

import android.view.LayoutInflater
import android.view.ViewGroup

import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

import com.roomify.app.R
import com.roomify.app.data.remote.dto.landlord.RoomResponse
import com.roomify.app.databinding.ItemLandlordRoomBinding

import java.text.NumberFormat
import java.util.Locale

class LandlordRoomAdapter(
    private val onRoomClick:
        (RoomResponse) -> Unit
) : ListAdapter<
        RoomResponse,
        LandlordRoomAdapter.RoomViewHolder
        >(DiffCallback) {

    private val currencyFormatter =
        NumberFormat.getCurrencyInstance(
            Locale.forLanguageTag("vi-VN")
        )

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RoomViewHolder {

        val binding =
            ItemLandlordRoomBinding.inflate(
                LayoutInflater.from(
                    parent.context
                ),
                parent,
                false
            )

        return RoomViewHolder(
            binding
        )
    }

    override fun onBindViewHolder(
        holder: RoomViewHolder,
        position: Int
    ) {
        holder.bind(
            getItem(position)
        )
    }

    inner class RoomViewHolder(
        private val binding:
        ItemLandlordRoomBinding
    ) : RecyclerView.ViewHolder(
        binding.root
    ) {

        fun bind(
            room: RoomResponse
        ) {

            binding.tvRoomTitle.text =
                room.title

            binding.tvRoomNumber.text =
                room.roomNumber
                    ?.takeIf {
                        it.isNotBlank()
                    }
                    ?.let {
                        "Phòng $it"
                    }
                    ?: "Chưa có số phòng"

            binding.tvRentPrice.text =
                "${currencyFormatter.format(
                    room.rentPrice
                )}/tháng"

            val area =
                room.areaM2
                    .stripTrailingZeros()
                    .toPlainString()

            binding.tvRoomInfo.text =
                "$area m² • Tối đa ${room.maxOccupants} người"

            showAvailability(
                room.availabilityStatus
            )

            binding.tvListingStatus.text =
                "Tin: ${listingStatusText(
                    room.listingStatus
                )}"

            binding.root
                .setOnClickListener {

                    onRoomClick(
                        room
                    )
                }
        }

        private fun showAvailability(
            status: String
        ) {

            val normalized =
                status.uppercase(
                    Locale.ROOT
                )

            val text =
                when (normalized) {

                    "AVAILABLE" ->
                        "Còn trống"

                    "RESERVED" ->
                        "Đã giữ chỗ"

                    "RENTED" ->
                        "Đã cho thuê"

                    else ->
                        "Không xác định"
                }

            val color =
                when (normalized) {

                    "AVAILABLE" ->
                        R.color.status_success

                    "RENTED" ->
                        R.color.status_error

                    else ->
                        R.color.text_muted
                }

            binding.tvAvailabilityStatus.text =
                text

            binding.tvAvailabilityStatus
                .setTextColor(
                    ContextCompat.getColor(
                        binding.root.context,
                        color
                    )
                )
        }

        private fun listingStatusText(
            status: String
        ): String {

            return when (
                status.uppercase(
                    Locale.ROOT
                )
            ) {

                "DRAFT" ->
                    "Bản nháp"

                "PENDING" ->
                    "Chờ duyệt"

                "PUBLISHED" ->
                    "Đã đăng"

                "REJECTED" ->
                    "Bị từ chối"

                "HIDDEN" ->
                    "Đã ẩn"

                else ->
                    status
            }
        }
    }

    private object DiffCallback :
        DiffUtil.ItemCallback<
                RoomResponse
                >() {

        override fun areItemsTheSame(
            oldItem: RoomResponse,
            newItem: RoomResponse
        ): Boolean {

            return oldItem.roomId ==
                    newItem.roomId
        }

        override fun areContentsTheSame(
            oldItem: RoomResponse,
            newItem: RoomResponse
        ): Boolean {

            return oldItem ==
                    newItem
        }
    }
}