package com.roomify.app.data.remote.dto.landlord

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

data class RoomResponse(

    @SerializedName("roomId")
    val roomId: Long,

    @SerializedName("propertyId")
    val propertyId: Long,

    @SerializedName("propertyName")
    val propertyName: String,

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

    @SerializedName("listingStatus")
    val listingStatus: String,

    @SerializedName("availabilityStatus")
    val availabilityStatus: String,

    @SerializedName("viewCount")
    val viewCount: Int,

    @SerializedName("rejectionReason")
    val rejectionReason: String?,

    @SerializedName("publishedAt")
    val publishedAt: String?,

    @SerializedName("amenities")
    val amenities: List<RoomAmenityResponse> = emptyList(),

    @SerializedName("createdAt")
    val createdAt: String,

    @SerializedName("updatedAt")
    val updatedAt: String
)

data class RoomAmenityResponse(

    @SerializedName("amenityId")
    val amenityId: Int,

    @SerializedName("amenityName")
    val amenityName: String,

    @SerializedName("iconUrl")
    val iconUrl: String?
)