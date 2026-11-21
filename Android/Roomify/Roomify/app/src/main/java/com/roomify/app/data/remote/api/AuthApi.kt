
package com.roomify.app.data.remote.api

import com.roomify.app.data.remote.dto.auth.AuthMeResponse
import com.roomify.app.data.remote.dto.auth.AuthUserResponse
import com.roomify.app.data.remote.dto.auth.LoginRequest
import com.roomify.app.data.remote.dto.auth.LoginResponse
import com.roomify.app.data.remote.dto.auth.LogoutRequest
import com.roomify.app.data.remote.dto.auth.LogoutResponse
import com.roomify.app.data.remote.dto.auth.RefreshTokenRequest
import com.roomify.app.data.remote.dto.auth.RegisterLandlordRequest
import com.roomify.app.data.remote.dto.auth.RegisterStudentRequest

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AuthApi {

    // ============================================================
    // 1. LOGIN - STUDENT / LANDLORD / ADMIN
    // ============================================================

    @POST("api/auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<LoginResponse>

    // ============================================================
    // 2. REGISTER STUDENT
    // ============================================================

    @POST("api/auth/register/student")
    suspend fun registerStudent(
        @Body request: RegisterStudentRequest
    ): Response<AuthUserResponse>

    // ============================================================
    // 3. REGISTER LANDLORD
    // ============================================================

    @POST("api/auth/register/landlord")
    suspend fun registerLandlord(
        @Body request: RegisterLandlordRequest
    ): Response<AuthUserResponse>

    // ============================================================
    // 4. REFRESH TOKEN
    // ============================================================

    @POST("api/auth/refresh")
    suspend fun refreshToken(
        @Body request: RefreshTokenRequest
    ): Response<LoginResponse>

    // ============================================================
    // 5. LOGOUT
    // ============================================================

    @POST("api/auth/logout")
    suspend fun logout(
        @Body request: LogoutRequest
    ): Response<LogoutResponse>

    // ============================================================
    // 6. GET CURRENT USER
    // ============================================================

    @GET("api/auth/me")
    suspend fun getMe(): Response<AuthMeResponse>
}
