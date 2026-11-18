package com.roomify.app.data.remote.dto.users

import com.google.gson.annotations.SerializedName

data class UserMeResponse(

    @SerializedName("userId")
    val userId: Long,

    @SerializedName("email")
    val email: String,

    @SerializedName("fullName")
    val fullName: String,

    @SerializedName("phone")
    val phone: String?,

    @SerializedName("avatarUrl")
    val avatarUrl: String?,

    @SerializedName("role")
    val role: String,

    @SerializedName("status")
    val status: String,

    @SerializedName("emailVerified")
    val emailVerified: Boolean,

    @SerializedName("lastLoginAt")
    val lastLoginAt: String?,

    @SerializedName("createdAt")
    val createdAt: String,

    @SerializedName("updatedAt")
    val updatedAt: String,

    @SerializedName("studentProfile")
    val studentProfile: StudentProfileResponse?
)

data class StudentProfileResponse(

    @SerializedName("studentId")
    val studentId: Long,

    @SerializedName("universityId")
    val universityId: Long?,

    @SerializedName("universityName")
    val universityName: String?,

    @SerializedName("studentCode")
    val studentCode: String?,

    @SerializedName("studentCardUrl")
    val studentCardUrl: String?,

    @SerializedName("isVerified")
    val isVerified: Boolean,

    @SerializedName("verifiedAt")
    val verifiedAt: String?
)

data class UpdateUserRequest(

    @SerializedName("fullName")
    val fullName: String,

    @SerializedName("phone")
    val phone: String?
)

data class UpdateStudentProfileRequest(

    @SerializedName("universityId")
    val universityId: Long?,

    @SerializedName("studentCode")
    val studentCode: String?
)