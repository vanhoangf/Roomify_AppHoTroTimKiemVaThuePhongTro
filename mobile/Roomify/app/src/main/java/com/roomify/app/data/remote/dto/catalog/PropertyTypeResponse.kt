package com.roomify.app.data.remote.dto.catalog

import com.google.gson.annotations.SerializedName

data class PropertyTypeResponse(

    @SerializedName("propertyTypeId")
    val propertyTypeId: Int,

    @SerializedName("typeName")
    val typeName: String
)