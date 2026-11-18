
package com.roomify.app.data.remote.dto.auth

import com.google.gson.annotations.SerializedName

data class LogoutRequest(
    @SerializedName("refreshToken")
    val refreshToken: String
)
