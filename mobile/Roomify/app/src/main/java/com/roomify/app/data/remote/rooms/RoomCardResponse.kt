package com.roomify.app.data.remote.dto.rooms

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

data class RoomCardResponse(

    @SerializedName("roomId")
    val roomId: Long,

    @SerializedName("propertyId")
    val propertyId: Long,

    @SerializedName("propertyName")
    val propertyName: String,

    @SerializedName("propertyTypeName")
    val propertyTypeName: String,

    @SerializedName("title")
    val title: String,

    @SerializedName("areaM2")
    val areaM2: BigDecimal,

    @SerializedName("rentPrice")
    val rentPrice: BigDecimal,

    @SerializedName("depositAmount")
    val depositAmount: BigDecimal?,

    @SerializedName("maxOccupants")
    val maxOccupants: Int,

    @SerializedName("genderPreference")
    val genderPreference: String,

    @SerializedName("listingStatus")
    val listingStatus: String,

    @SerializedName("availabilityStatus")
    val availabilityStatus: String,

    @SerializedName("availableFrom")
    val availableFrom: String?,

    @SerializedName("provinceId")
    val provinceId: Int,

    @SerializedName("provinceName")
    val provinceName: String,

    @SerializedName("wardId")
    val wardId: Int,

    @SerializedName("wardName")
    val wardName: String,

    @SerializedName("addressLine")
    val addressLine: String,

    @SerializedName("thumbnailUrl")
    val thumbnailUrl: String?,

    @SerializedName("distanceKm")
    val distanceKm: Double?,

    @SerializedName("publishedAt")
    val publishedAt: String?
)
