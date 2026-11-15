
package com.roomify.app.data.remote.dto.common

import com.google.gson.annotations.SerializedName

data class HealthResponse(

    @SerializedName("status")
    val status: String? = null,

    @SerializedName("database")
    val database: String? = null,

    @SerializedName("serverTime")
    val serverTime: String? = null
)
