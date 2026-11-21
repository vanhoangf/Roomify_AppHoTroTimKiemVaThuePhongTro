package com.roomify.app.data.remote.dto.savedrooms

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

data class SavedRoomResponse(

    @SerializedName("roomId")
    val roomId: Long,

    @SerializedName("propertyId")
    val propertyId: Long,

    @SerializedName("propertyName")
    val propertyName: String,

    @SerializedName("title")
    val title: String,

    @SerializedName("rentPrice")
    val rentPrice: BigDecimal,

    @SerializedName("areaM2")
    val areaM2: BigDecimal,

    @SerializedName("availabilityStatus")
    val availabilityStatus: String,

    @SerializedName("addressLine")
    val addressLine: String,

    @SerializedName("provinceName")
    val provinceName: String,

    @SerializedName("wardName")
    val wardName: String,

    @SerializedName("thumbnailUrl")
    val thumbnailUrl: String?,

    @SerializedName("savedAt")
    val savedAt: String
)