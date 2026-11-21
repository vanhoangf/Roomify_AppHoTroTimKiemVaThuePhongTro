package com.roomify.app.data.remote.api

import com.roomify.app.data.remote.dto.users.LandlordProfileResponse
import com.roomify.app.data.remote.dto.users.StudentProfileResponse
import com.roomify.app.data.remote.dto.users.UpdateLandlordProfileRequest
import com.roomify.app.data.remote.dto.users.UpdateStudentProfileRequest
import com.roomify.app.data.remote.dto.users.UpdateUserRequest
import com.roomify.app.data.remote.dto.users.UserMeResponse

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT

interface UserApi {

    @GET("api/users/me")
    suspend fun getMe():
            Response<UserMeResponse>

    @PUT("api/users/me")
    suspend fun updateMe(
        @Body
        request: UpdateUserRequest
    ): Response<UserMeResponse>

    @PUT("api/users/me/student-profile")
    suspend fun updateStudentProfile(
        @Body
        request: UpdateStudentProfileRequest
    ): Response<StudentProfileResponse>

    @PUT("api/users/me/landlord-profile")
    suspend fun updateLandlordProfile(
        @Body
        request: UpdateLandlordProfileRequest
    ): Response<LandlordProfileResponse>
}