package com.roomify.app.ui.student

import android.view.LayoutInflater
import android.view.ViewGroup

import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

import com.roomify.app.R
import com.roomify.app.data.remote.dto.contracts.RentalContractResponse
import com.roomify.app.databinding.ItemRentalContractBinding

import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class MyContractsAdapter(
    private val onDetail:
        (RentalContractResponse) -> Unit
) : ListAdapter<
        RentalContractResponse,
        MyContractsAdapter.ContractViewHolder
        >(DIFF) {

    private val money =
        NumberFormat.getCurrencyInstance(
            Locale.forLanguageTag("vi-VN")
        )

    inner class ContractViewHolder(
        private val binding:
        ItemRentalContractBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(
            item: RentalContractResponse
        ) {

            binding.tvContractCode.text =
                item.contractCode

            binding.tvRoomTitle.text =
                item.roomTitle

            binding.tvPropertyName.text =
                item.propertyName

            binding.tvPeriod.text =
                "${formatDate(item.startDate)} → " +
                        formatDate(item.endDate)

            binding.tvMonthlyRent.text =
                "${money.format(item.monthlyRent)}/tháng"

            val status =
                item.status.uppercase(
                    Locale.ROOT
                )

            binding.chipStatus.text =
                when (status) {

                    "PENDING" ->
                        "Chờ kích hoạt"

                    "ACTIVE" ->
                        "Đang hiệu lực"

                    "COMPLETED" ->
                        "Đã hoàn thành"

                    "CANCELLED" ->
                        "Đã hủy"

                    else ->
                        status
                }

            val color =
                when (status) {

                    "ACTIVE",
                    "COMPLETED" ->
                        R.color.status_success

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
        value: String?
    ): String {

        if (value.isNullOrBlank()) {
            return "Chưa xác định"
        }

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
    ): ContractViewHolder {

        return ContractViewHolder(
            ItemRentalContractBinding.inflate(
                LayoutInflater.from(
                    parent.context
                ),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(
        holder: ContractViewHolder,
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
                        RentalContractResponse
                        >() {

                override fun areItemsTheSame(
                    oldItem: RentalContractResponse,
                    newItem: RentalContractResponse
                ) =
                    oldItem.contractId ==
                            newItem.contractId

                override fun areContentsTheSame(
                    oldItem: RentalContractResponse,
                    newItem: RentalContractResponse
                ) =
                    oldItem == newItem
            }
    }
}