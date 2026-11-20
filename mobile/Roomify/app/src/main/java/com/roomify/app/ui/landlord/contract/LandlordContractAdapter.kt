package com.roomify.app.ui.landlord.contract

import android.view.LayoutInflater
import android.view.ViewGroup

import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

import com.roomify.app.R
import com.roomify.app.data.remote.dto.contracts.RentalContractResponse
import com.roomify.app.databinding.ItemLandlordContractBinding

import java.math.BigDecimal
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class LandlordContractAdapter(
    private val onClick:
        (RentalContractResponse) -> Unit
) : ListAdapter<
        RentalContractResponse,
        LandlordContractAdapter.ContractViewHolder
        >(DiffCallback) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ContractViewHolder {

        return ContractViewHolder(
            ItemLandlordContractBinding.inflate(
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

    inner class ContractViewHolder(
        private val binding:
        ItemLandlordContractBinding
    ) : RecyclerView.ViewHolder(
        binding.root
    ) {

        fun bind(
            item: RentalContractResponse
        ) {

            binding.tvContractCode.text =
                item.contractCode

            binding.tvStudentName.text =
                item.studentName
                    .ifBlank {
                        "Sinh viên"
                    }

            binding.tvRoomTitle.text =
                item.roomTitle

            binding.tvPropertyName.text =
                item.propertyName

            binding.tvRentalPeriod.text =
                "${formatDate(item.startDate)} → " +
                        (
                                item.endDate
                                    ?.let {
                                        formatDate(
                                            it
                                        )
                                    }
                                    ?: "Không xác định"
                                )

            binding.tvMonthlyRent.text =
                "${formatMoney(item.monthlyRent)} / tháng"

            bindStatus(
                item.status
            )

            binding.cardContract
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
                        "Chờ xác nhận"

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

    private fun formatMoney(
        value: BigDecimal
    ): String {

        return NumberFormat
            .getCurrencyInstance(
                Locale(
                    "vi",
                    "VN"
                )
            )
            .format(
                value
            )
    }

    companion object {

        private val DiffCallback =
            object :
                DiffUtil.ItemCallback<
                        RentalContractResponse
                        >() {

                override fun areItemsTheSame(
                    oldItem:
                    RentalContractResponse,
                    newItem:
                    RentalContractResponse
                ): Boolean {

                    return oldItem.contractId ==
                            newItem.contractId
                }

                override fun areContentsTheSame(
                    oldItem:
                    RentalContractResponse,
                    newItem:
                    RentalContractResponse
                ): Boolean {

                    return oldItem ==
                            newItem
                }
            }
    }
}