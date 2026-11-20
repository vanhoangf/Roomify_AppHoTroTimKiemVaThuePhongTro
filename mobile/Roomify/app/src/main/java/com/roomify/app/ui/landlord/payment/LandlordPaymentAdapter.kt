package com.roomify.app.ui.landlord.payment

import android.view.LayoutInflater
import android.view.ViewGroup

import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

import com.roomify.app.R
import com.roomify.app.data.remote.dto.payments.PaymentResponse
import com.roomify.app.databinding.ItemLandlordPaymentBinding

import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class LandlordPaymentAdapter(
    private val onClick:
        (PaymentResponse) -> Unit
) : ListAdapter<
        PaymentResponse,
        LandlordPaymentAdapter.PaymentViewHolder
        >(DiffCallback) {

    private val money =
        NumberFormat.getCurrencyInstance(
            Locale.forLanguageTag(
                "vi-VN"
            )
        )

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PaymentViewHolder {

        return PaymentViewHolder(
            ItemLandlordPaymentBinding.inflate(
                LayoutInflater.from(
                    parent.context
                ),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(
        holder: PaymentViewHolder,
        position: Int
    ) {

        holder.bind(
            getItem(position)
        )
    }

    inner class PaymentViewHolder(
        private val binding:
        ItemLandlordPaymentBinding
    ) : RecyclerView.ViewHolder(
        binding.root
    ) {

        fun bind(
            item: PaymentResponse
        ) {

            binding.tvPaymentType.text =
                paymentTypeText(
                    item.paymentType
                )

            binding.tvAmount.text =
                money.format(
                    item.amount
                )

            binding.tvStudentName.text =
                item.studentName
                    .ifBlank {
                        "Sinh viên"
                    }

            binding.tvRoomTitle.text =
                item.roomTitle

            binding.tvContractCode.text =
                "Hợp đồng: ${item.contractCode}"

            binding.tvDueDate.text =
                item.dueDate
                    ?.let {
                        "Hạn thanh toán: ${formatDate(it)}"
                    }
                    ?: "Không có hạn thanh toán"

            bindStatus(
                item.status
            )

            binding.cardPayment
                .setOnClickListener {

                    onClick(
                        item
                    )
                }
        }

        private fun bindStatus(
            value: String
        ) {

            val status =
                value.uppercase(
                    Locale.ROOT
                )

            binding.chipStatus.text =
                when (status) {

                    "PENDING" ->
                        "Chờ xử lý"

                    "PAID" ->
                        "Đã thanh toán"

                    "FAILED" ->
                        "Đã từ chối"

                    "CANCELLED" ->
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

            binding.chipStatus.setTextColor(
                ContextCompat.getColor(
                    binding.root.context,
                    color
                )
            )
        }
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

    companion object {

        private val DiffCallback =
            object :
                DiffUtil.ItemCallback<
                        PaymentResponse
                        >() {

                override fun areItemsTheSame(
                    oldItem:
                    PaymentResponse,
                    newItem:
                    PaymentResponse
                ): Boolean {

                    return oldItem.paymentId ==
                            newItem.paymentId
                }

                override fun areContentsTheSame(
                    oldItem:
                    PaymentResponse,
                    newItem:
                    PaymentResponse
                ): Boolean {

                    return oldItem ==
                            newItem
                }
            }
    }
}