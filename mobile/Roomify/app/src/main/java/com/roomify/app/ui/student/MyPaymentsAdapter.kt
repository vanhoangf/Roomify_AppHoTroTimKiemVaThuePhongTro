package com.roomify.app.ui.student

import android.view.LayoutInflater
import android.view.ViewGroup

import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

import com.roomify.app.R
import com.roomify.app.data.remote.dto.payments.PaymentResponse
import com.roomify.app.databinding.ItemStudentPaymentBinding

import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class MyPaymentsAdapter(
    private val onDetail:
        (PaymentResponse) -> Unit
) : ListAdapter<
        PaymentResponse,
        MyPaymentsAdapter.PaymentViewHolder
        >(DIFF) {

    private val money =
        NumberFormat.getCurrencyInstance(
            Locale.forLanguageTag("vi-VN")
        )

    inner class PaymentViewHolder(
        private val binding:
        ItemStudentPaymentBinding
    ) : RecyclerView.ViewHolder(
        binding.root
    ) {

        fun bind(
            item: PaymentResponse
        ) {

            binding.tvPaymentType.text =
                when (
                    item.paymentType
                        .uppercase(Locale.ROOT)
                ) {

                    "DEPOSIT" -> "Tiền cọc"
                    "RENT" -> "Tiền thuê"
                    "UTILITY" -> "Điện nước / Tiện ích"
                    else -> "Khoản thanh toán"
                }

            binding.tvRoomTitle.text =
                item.roomTitle

            binding.tvAmount.text =
                money.format(
                    item.amount
                )

            binding.tvDueDate.text =
                item.dueDate?.let {
                    "Hạn thanh toán: ${formatDate(it)}"
                } ?: "Không có hạn thanh toán"

            val status =
                item.status.uppercase(
                    Locale.ROOT
                )

            binding.chipStatus.text =
                when (status) {

                    "PENDING" ->
                        "Chờ xác nhận"

                    "PAID" ->
                        "Đã thanh toán"

                    "FAILED" ->
                        "Bị từ chối"

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

            binding.btnDetail
                .setOnClickListener {
                    onDetail(item)
                }

            binding.root
                .setOnClickListener {
                    onDetail(item)
                }
        }
    }

    private fun formatDate(
        value: String
    ): String {

        return runCatching {

            LocalDate.parse(value)
                .format(
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
    ): PaymentViewHolder {

        return PaymentViewHolder(
            ItemStudentPaymentBinding.inflate(
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

    companion object {

        private val DIFF =
            object :
                DiffUtil.ItemCallback<
                        PaymentResponse
                        >() {

                override fun areItemsTheSame(
                    oldItem: PaymentResponse,
                    newItem: PaymentResponse
                ) =
                    oldItem.paymentId ==
                            newItem.paymentId

                override fun areContentsTheSame(
                    oldItem: PaymentResponse,
                    newItem: PaymentResponse
                ) =
                    oldItem == newItem
            }
    }
}