package com.roomify.app.data.remote.dto.rooms

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

data class RoomDetailResponse(

    @SerializedName("roomId")
    val roomId: Long,

    @SerializedName("propertyId")
    val propertyId: Long,

    @SerializedName("propertyName")
    val propertyName: String,

    @SerializedName("propertyDescription")
    val propertyDescription: String?,

    @SerializedName("propertyTypeId")
    val propertyTypeId: Int,

    @SerializedName("propertyTypeName")
    val propertyTypeName: String,

    @SerializedName("propertyStatus")
    val propertyStatus: String,

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

    @SerializedName("latitude")
    val latitude: BigDecimal?,

    @SerializedName("longitude")
    val longitude: BigDecimal?,

    @SerializedName("landlordIsVerified")
    val landlordIsVerified: Boolean,

    @SerializedName("viewCount")
    val viewCount: Int,

    @SerializedName("publishedAt")
    val publishedAt: String?,

    @SerializedName("images")
    val images: List<PublicRoomImageResponse> = emptyList(),

    @SerializedName("amenities")
    val amenities: List<PublicRoomAmenityResponse> = emptyList()
)

data class PublicRoomImageResponse(

    @SerializedName("imageId")
    val imageId: Long,

    @SerializedName("imageUrl")
    val imageUrl: String,

    @SerializedName("isPrimary")
    val isPrimary: Boolean,

    @SerializedName("sortOrder")
    val sortOrder: Int
)

data class PublicRoomAmenityResponse(

    @SerializedName("amenityId")
    val amenityId: Int,

    @SerializedName("amenityName")
    val amenityName: String,

    @SerializedName("iconUrl")
    val iconUrl: String?
)