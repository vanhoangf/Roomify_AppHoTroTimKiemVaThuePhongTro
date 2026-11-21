
package com.roomify.app.data.remote.dto.auth

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    @SerializedName("accessToken")
    val accessToken: String,

    @SerializedName("refreshToken")
    val refreshToken: String,

    @SerializedName("tokenType")
    val tokenType: String,

    @SerializedName("accessTokenExpiresAt")
    val accessTokenExpiresAt: String,

    @SerializedName("refreshTokenExpiresAt")
    val refreshTokenExpiresAt: String,

    @SerializedName("user")
    val user: AuthUserResponse
)
