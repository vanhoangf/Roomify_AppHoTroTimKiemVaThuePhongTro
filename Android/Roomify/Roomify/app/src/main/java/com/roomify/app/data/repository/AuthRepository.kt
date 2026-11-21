
package com.roomify.app.data.repository

import com.roomify.app.data.local.AuthSessionStore

import com.roomify.app.data.remote.api.AuthApi

import com.roomify.app.data.remote.common.ApiException
import com.roomify.app.data.remote.common.ApiResult
import com.roomify.app.data.remote.common.safeApiCall

import com.roomify.app.data.remote.dto.auth.AuthMeResponse
import com.roomify.app.data.remote.dto.auth.AuthUserResponse
import com.roomify.app.data.remote.dto.auth.LoginRequest
import com.roomify.app.data.remote.dto.auth.LoginResponse
import com.roomify.app.data.remote.dto.auth.LogoutRequest
import com.roomify.app.data.remote.dto.auth.LogoutResponse
import com.roomify.app.data.remote.dto.auth.RefreshTokenRequest
import com.roomify.app.data.remote.dto.auth.RegisterStudentRequest
import com.roomify.app.data.remote.dto.auth.RegisterLandlordRequest

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext

class AuthRepository(
    private val authApi: AuthApi,
    private val sessionStore: AuthSessionStore
) {

    // ============================================================
    // 1. LOGIN
    // ============================================================

    suspend fun login(
        email: String,
        password: String
    ): ApiResult<LoginResponse> {

        val result = safeApiCall {
            authApi.login(
                LoginRequest(
                    email = email.trim(),
                    password = password
                )
            )
        }

        return persistLoginResponse(
            result = result,
            save = { response ->
                sessionStore.saveSession(response)
            }
        )
    }

    // ============================================================
    // 2. REGISTER STUDENT
    // ============================================================

    suspend fun registerStudent(
        request: RegisterStudentRequest
    ): ApiResult<AuthUserResponse> {

        return safeApiCall {
            authApi.registerStudent(request)
        }
    }

    // ============================================================
    // 3. REGISTER LANDLORD
    // ============================================================

    suspend fun registerLandlord(
        request: RegisterLandlordRequest
    ): ApiResult<AuthUserResponse> {

        return safeApiCall {
            authApi.registerLandlord(request)
        }
    }

    // ============================================================
    // 4. REFRESH SESSION
    // ============================================================

    suspend fun refreshSession(): ApiResult<LoginResponse> {

        val refreshToken = try {
            sessionStore.getRefreshToken()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            return ApiResult.Error(
                ApiException(
                    httpStatus = null,
                    errorCode = "SESSION_READ_FAILED",
                    message = "Không thể đọc phiên đăng nhập.",
                    cause = e
                )
            )
        }

        if (refreshToken.isNullOrBlank()) {
            return ApiResult.Error(
                ApiException(
                    httpStatus = null,
                    errorCode = "NO_REFRESH_TOKEN",
                    message = "Không có refresh token để gia hạn phiên."
                )
            )
        }

        val result = safeApiCall {
            authApi.refreshToken(
                RefreshTokenRequest(refreshToken)
            )
        }

        return persistLoginResponse(
            result = result,
            save = { response ->
                sessionStore.updateTokens(response)
            }
        )
    }

    // ============================================================
    // 5. GET CURRENT USER
    // ============================================================

    suspend fun getMe(): ApiResult<AuthMeResponse> {

        return safeApiCall {
            authApi.getMe()
        }
    }

    // ============================================================
    // 6. LOGOUT
    // ============================================================

    suspend fun logout(): ApiResult<LogoutResponse> {

        var result: ApiResult<LogoutResponse> =
            ApiResult.Error(
                ApiException(
                    httpStatus = null,
                    errorCode = "NO_REFRESH_TOKEN",
                    message = "Không có phiên để thu hồi trên máy chủ."
                )
            )

        var clearFailure: Exception? = null

        try {

            val refreshToken = sessionStore.getRefreshToken()

            if (!refreshToken.isNullOrBlank()) {

                result = safeApiCall {
                    authApi.logout(
                        LogoutRequest(refreshToken)
                    )
                }
            }

        } catch (e: CancellationException) {

            throw e

        } catch (e: Exception) {

            result = ApiResult.Error(
                ApiException(
                    httpStatus = null,
                    errorCode = "LOGOUT_FAILED",
                    message = "Không thể hoàn tất đăng xuất trên máy chủ.",
                    cause = e
                )
            )

        } finally {

            // Người dùng đã chọn logout:
            // luôn cố gắng xóa phiên trên thiết bị,
            // kể cả khi máy chủ không phản hồi.
            try {
                withContext(NonCancellable) {
                    sessionStore.clearSession()
                }
            } catch (e: Exception) {
                clearFailure = e
            }
        }

        if (clearFailure != null) {
            return ApiResult.Error(
                ApiException(
                    httpStatus = null,
                    errorCode = "SESSION_CLEAR_FAILED",
                    message = "Không thể xóa phiên đăng nhập trên thiết bị.",
                    cause = clearFailure
                )
            )
        }

        return result
    }

    // ============================================================
    // 7. VALIDATE AND STORE LOGIN RESPONSE
    // ============================================================

    private suspend fun persistLoginResponse(
        result: ApiResult<LoginResponse>,
        save: suspend (LoginResponse) -> Unit
    ): ApiResult<LoginResponse> {

        if (result !is ApiResult.Success) {
            return result
        }

        val response = result.data

        if (
            response == null ||
            response.accessToken.isNullOrBlank() ||
            response.refreshToken.isNullOrBlank()
        ) {
            return ApiResult.Error(
                ApiException(
                    httpStatus = result.statusCode,
                    errorCode = "INVALID_AUTH_RESPONSE",
                    message = "Máy chủ không trả về token hợp lệ."
                )
            )
        }

        return try {

            save(response)

            result

        } catch (e: CancellationException) {

            throw e

        } catch (e: Exception) {

            ApiResult.Error(
                ApiException(
                    httpStatus = null,
                    errorCode = "SESSION_SAVE_FAILED",
                    message = "Không thể lưu phiên đăng nhập.",
                    cause = e
                )
            )
        }
    }
}
