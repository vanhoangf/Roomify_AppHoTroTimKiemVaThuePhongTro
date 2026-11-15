
package com.roomify.app.data.remote.dto.auth

import com.google.gson.annotations.SerializedName

data class LogoutResponse(
    @SerializedName("message")
    val message: String
)
