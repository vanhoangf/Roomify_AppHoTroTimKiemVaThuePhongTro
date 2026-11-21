
package com.roomify.app.data.remote.dto.auth

import com.google.gson.annotations.SerializedName

data class RegisterLandlordRequest(
    @SerializedName("fullName")
    val fullName: String,

    @SerializedName("email")
    val email: String,

    @SerializedName("password")
    val password: String,

    @SerializedName("phone")
    val phone: String? = null,

    @SerializedName("identityNumber")
    val identityNumber: String? = null
)
