
package com.roomify.app.data.remote.dto.common

import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName

data class ApiErrorResponse(
    @SerializedName("success")
    val success: Boolean = false,

    @SerializedName("statusCode")
    val statusCode: Int = 0,

    @SerializedName("errorCode")
    val errorCode: String = "",

    @SerializedName("message")
    val message: String = "",

    @SerializedName("errors")
    val errors: JsonElement? = null,

    @SerializedName("traceId")
    val traceId: String? = null,

    @SerializedName("timestamp")
    val timestamp: String? = null
)
