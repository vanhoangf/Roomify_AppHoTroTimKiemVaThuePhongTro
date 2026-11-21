
package com.roomify.app.data.remote.dto.auth

import com.google.gson.annotations.SerializedName

data class RegisterStudentRequest(
    @SerializedName("fullName")
    val fullName: String,

    @SerializedName("email")
    val email: String,

    @SerializedName("password")
    val password: String,

    @SerializedName("phone")
    val phone: String? = null,

    @SerializedName("universityId")
    val universityId: Long? = null,

    @SerializedName("studentCode")
    val studentCode: String? = null
)
