
package com.roomify.app.data.remote.dto.auth

import com.google.gson.annotations.SerializedName

data class AuthMeResponse(
    @SerializedName("userId")
    val userId: String?,

    @SerializedName("email")
    val email: String?,

    @SerializedName("fullName")
    val fullName: String?,

    @SerializedName("role")
    val role: String?
)
