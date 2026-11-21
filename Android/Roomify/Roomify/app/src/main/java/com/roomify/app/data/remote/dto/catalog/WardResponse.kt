package com.roomify.app.data.remote.dto.catalog

import com.google.gson.annotations.SerializedName

data class WardResponse(

    @SerializedName("wardId")
    val wardId: Int,

    @SerializedName("provinceId")
    val provinceId: Int,

    @SerializedName("wardCode")
    val wardCode: String?,

    @SerializedName("wardName")
    val wardName: String,

    @SerializedName("wardType")
    val wardType: String
)