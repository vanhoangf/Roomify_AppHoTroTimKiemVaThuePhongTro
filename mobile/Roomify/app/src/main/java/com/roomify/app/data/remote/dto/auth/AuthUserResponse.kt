package com.roomify.app.data.remote.dto.auth

import com.google.gson.annotations.SerializedName

data class AuthUserResponse(
    @SerializedName("userId")
    val userId: Long,

    @SerializedName("email")
    val email: String,

    @SerializedName("fullName")
    val fullName: String,

    @SerializedName("role")
    val role: String,

    @SerializedName("status")
    val status: String,

    @SerializedName("createdAt")
    val createdAt: String
)
