package com.roomify.app.data.remote.dto.landlord

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

data class CreateRoomRequest(

    @SerializedName("roomNumber")
    val roomNumber: String?,

    @SerializedName("title")
    val title: String,

    @SerializedName("description")
    val description: String?,

    @SerializedName("areaM2")
    val areaM2: BigDecimal,

    @SerializedName("rentPrice")
    val rentPrice: BigDecimal,

    @SerializedName("depositAmount")
    val depositAmount: BigDecimal?,

    @SerializedName("electricityPrice")
    val electricityPrice: BigDecimal?,

    @SerializedName("waterPrice")
    val waterPrice: BigDecimal?,

    @SerializedName("internetPrice")
    val internetPrice: BigDecimal?,

    @SerializedName("serviceFee")
    val serviceFee: BigDecimal?,

    @SerializedName("parkingFee")
    val parkingFee: BigDecimal?,

    @SerializedName("maxOccupants")
    val maxOccupants: Int,

    @SerializedName("genderPreference")
    val genderPreference: String,

    @SerializedName("availableFrom")
    val availableFrom: String?,

    @SerializedName("amenityIds")
    val amenityIds: List<Int> = emptyList()
)