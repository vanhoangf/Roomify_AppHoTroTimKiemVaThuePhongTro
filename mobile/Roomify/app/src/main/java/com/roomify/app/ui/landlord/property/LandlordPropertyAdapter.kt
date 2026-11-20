package com.roomify.app.ui.landlord.property
import android.view.LayoutInflater
import android.view.ViewGroup

import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

import com.roomify.app.R
import com.roomify.app.data.remote.dto.landlord.PropertyResponse
import com.roomify.app.databinding.ItemLandlordPropertyBinding

class LandlordPropertyAdapter(
    private val onPropertyClick: (
        PropertyResponse
    ) -> Unit
) : ListAdapter<
        PropertyResponse,
        LandlordPropertyAdapter.PropertyViewHolder
        >(DiffCallback) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PropertyViewHolder {

        val binding =
            ItemLandlordPropertyBinding.inflate(
                LayoutInflater.from(
                    parent.context
                ),
                parent,
                false
            )

        return PropertyViewHolder(
            binding
        )
    }

    override fun onBindViewHolder(
        holder: PropertyViewHolder,
        position: Int
    ) {
        holder.bind(
            getItem(position)
        )
    }

    inner class PropertyViewHolder(
        private val binding:
        ItemLandlordPropertyBinding
    ) : RecyclerView.ViewHolder(
        binding.root
    ) {

        fun bind(
            item: PropertyResponse
        ) {

            binding.tvPropertyName.text =
                item.propertyName

            binding.tvPropertyType.text =
                item.propertyTypeName

            binding.tvPropertyAddress.text =
                buildString {

                    append(
                        item.addressLine
                    )

                    if (
                        item.wardName
                            .isNotBlank()
                    ) {
                        append(
                            ", ${item.wardName}"
                        )
                    }

                    if (
                        item.provinceName
                            .isNotBlank()
                    ) {
                        append(
                            ", ${item.provinceName}"
                        )
                    }
                }

            binding.tvPropertyRoomCount.text =
                "${item.roomCount} phòng"

            val active =
                item.status.equals(
                    "ACTIVE",
                    ignoreCase = true
                )

            binding.tvPropertyStatus.text =
                if (active) {
                    "Hoạt động"
                } else {
                    "Ngừng hoạt động"
                }

            binding.tvPropertyStatus
                .setBackgroundResource(
                    if (active) {
                        R.drawable
                            .bg_property_status_active
                    } else {
                        R.drawable
                            .bg_property_status_inactive
                    }
                )

            binding.tvPropertyStatus
                .setTextColor(
                    ContextCompat.getColor(
                        binding.root.context,
                        if (active) {
                            R.color.status_success
                        } else {
                            R.color.text_muted
                        }
                    )
                )

            binding.root
                .setOnClickListener {
                    onPropertyClick(
                        item
                    )
                }
        }
    }

    private object DiffCallback :
        DiffUtil.ItemCallback<
                PropertyResponse
                >() {

        override fun areItemsTheSame(
            oldItem: PropertyResponse,
            newItem: PropertyResponse
        ): Boolean {

            return oldItem.propertyId ==
                    newItem.propertyId
        }

        override fun areContentsTheSame(
            oldItem: PropertyResponse,
            newItem: PropertyResponse
        ): Boolean {

            return oldItem ==
                    newItem
        }
    }
}