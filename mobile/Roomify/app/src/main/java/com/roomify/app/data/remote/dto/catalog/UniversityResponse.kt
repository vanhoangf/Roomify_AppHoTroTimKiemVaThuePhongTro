package com.roomify.app.data.remote.dto.catalog

import com.google.gson.annotations.SerializedName

data class UniversityResponse(

    @SerializedName("universityId")
    val universityId: Long,

    @SerializedName("universityName")
    val universityName: String,

    @SerializedName("provinceId")
    val provinceId: Int?,

    @SerializedName("provinceName")
    val provinceName: String?,

    @SerializedName("wardId")
    val wardId: Int?,

    @SerializedName("wardName")
    val wardName: String?,

    @SerializedName("addressLine")
    val addressLine: String?
)