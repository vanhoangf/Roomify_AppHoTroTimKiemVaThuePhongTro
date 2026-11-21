package com.roomify.app.data.remote.dto.catalog

import com.google.gson.annotations.SerializedName

data class AmenityResponse(

    @SerializedName("amenityId")
    val amenityId: Int,

    @SerializedName("amenityName")
    val amenityName: String,

    @SerializedName("iconUrl")
    val iconUrl: String?
)