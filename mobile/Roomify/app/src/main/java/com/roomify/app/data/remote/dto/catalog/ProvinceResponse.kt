package com.roomify.app.data.remote.dto.catalog

import com.google.gson.annotations.SerializedName

data class ProvinceResponse(

    @SerializedName("provinceId")
    val provinceId: Int,

    @SerializedName("provinceCode")
    val provinceCode: String?,

    @SerializedName("provinceName")
    val provinceName: String,

    @SerializedName("provinceType")
    val provinceType: String
)