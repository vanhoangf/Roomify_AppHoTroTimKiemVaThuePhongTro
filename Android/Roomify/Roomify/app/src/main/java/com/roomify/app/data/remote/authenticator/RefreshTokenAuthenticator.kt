
package com.roomify.app.data.remote.authenticator

import com.google.gson.Gson
import com.google.gson.JsonParseException
import com.roomify.app.data.local.UserSession
import com.roomify.app.data.remote.dto.auth.LoginResponse
import com.roomify.app.data.remote.dto.auth.RefreshTokenRequest

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

import okhttp3.Authenticator
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.Route

import java.io.IOException
import java.util.Locale

class RefreshTokenAuthenticator(
    backendBaseUrl: String,
    private val sessionReader: suspend () -> UserSession,
    private val sessionWriter: suspend (LoginResponse) -> Unit,
    private val sessionClearer: suspend () -> Unit,
    private val refreshClient: OkHttpClient =
        OkHttpClient.Builder()
            .authenticator(Authenticator.NONE)
            .build()
) : Authenticator {

    private val gson = Gson()

    private val backendUrl = backendBaseUrl.toHttpUrl()

    private val refreshUrl = backendUrl.resolve(
        "api/auth/refresh"
    ) ?: error("Invalid refresh endpoint")

    private data class RefreshOutcome(
        val statusCode: Int?,
        val tokens: LoginResponse?
    )

    // ============================================================
    // HANDLE HTTP 401
    // ============================================================

    @Synchronized
    override fun authenticate(
        route: Route?,
        response: Response
    ): Request? {

        if (response.code != 401) {
            return null
        }

        val request = response.request
        val url = request.url

        // Không xử lý request đến server khác.
        val isOurBackend =
            url.scheme == backendUrl.scheme &&
                    url.host == backendUrl.host &&
                    url.port == backendUrl.port

        if (!isOurBackend) {
            return null
        }

        // Không refresh khi endpoint xác thực tự trả 401.
        val path = url.encodedPath
            .trimEnd('/')
            .lowercase(Locale.ROOT)

        if (
            path == "/api/auth/login" ||
            path == "/api/auth/refresh" ||
            path == "/api/auth/logout" ||
            path.startsWith("/api/auth/register") ||
            path == "/api/health"
        ) {
            return null
        }

        // Giới hạn retry, tránh vòng lặp 401.
        if (response.priorResponse != null) {
            return null
        }

        val sentAuthorization =
            request.header("Authorization")
                ?: return null

        if (!sentAuthorization.startsWith(
                "Bearer ",
                ignoreCase = true
            )
        ) {
            return null
        }

        val session = readSession() ?: return null

        val currentAccessToken =
            session.accessToken
                ?.takeIf { it.isNotBlank() }
                ?: return null

        val currentRefreshToken =
            session.refreshToken
                ?.takeIf { it.isNotBlank() }
                ?: return null

        val currentAuthorization =
            "Bearer $currentAccessToken"

        // Nếu request khác đã refresh xong trong lúc
        // request này chờ, sử dụng token mới đã lưu.
        if (sentAuthorization != currentAuthorization) {
            return request.newBuilder()
                .header(
                    "Authorization",
                    currentAuthorization
                )
                .build()
        }

        // Thực hiện refresh bằng HTTP client riêng.
        val outcome = performRefresh(currentRefreshToken)

        val newTokens = outcome.tokens

        if (newTokens == null) {

            // Refresh token bị từ chối hoặc tài khoản
            // không còn quyền truy cập: xóa session.
            if (
                outcome.statusCode == 401 ||
                outcome.statusCode == 403
            ) {
                clearIfUnchanged(
                    currentAccessToken,
                    currentRefreshToken
                )
            }

            // Mất mạng, timeout, lỗi 5xx:
            // không tự động xóa session.
            return null
        }

        // Tránh ghi đè phiên đăng nhập nếu người dùng
        // đã thay đổi session trong lúc refresh.
        val saved = saveIfUnchanged(
            currentAccessToken,
            currentRefreshToken,
            newTokens
        )

        if (!saved) {
            return null
        }

        // Thử lại request với access token mới.
        return request.newBuilder()
            .header(
                "Authorization",
                "Bearer ${newTokens.accessToken}"
            )
            .build()
    }

    // ============================================================
    // CALL REFRESH ENDPOINT
    // ============================================================

    private fun performRefresh(
        refreshToken: String
    ): RefreshOutcome {

        return try {

            val json = gson.toJson(
                RefreshTokenRequest(refreshToken)
            )

            val body = json.toRequestBody(
                "application/json; charset=utf-8"
                    .toMediaType()
            )

            val request = Request.Builder()
                .url(refreshUrl)
                .post(body)
                .build()

            refreshClient.newCall(request)
                .execute()
                .use { response ->

                    val status = response.code

                    if (!response.isSuccessful) {
                        return RefreshOutcome(
                            statusCode = status,
                            tokens = null
                        )
                    }

                    val responseJson =
                        response.body?.string()

                    if (responseJson.isNullOrBlank()) {
                        return RefreshOutcome(
                            statusCode = status,
                            tokens = null
                        )
                    }

                    val tokens = gson.fromJson(
                        responseJson,
                        LoginResponse::class.java
                    )

                    if (
                        tokens?.accessToken.isNullOrBlank() ||
                        tokens?.refreshToken.isNullOrBlank()
                    ) {
                        return RefreshOutcome(
                            statusCode = status,
                            tokens = null
                        )
                    }

                    RefreshOutcome(
                        statusCode = status,
                        tokens = tokens
                    )
                }

        } catch (_: IOException) {

            RefreshOutcome(null, null)

        } catch (_: JsonParseException) {

            RefreshOutcome(null, null)

        }
    }

    // ============================================================
    // DATASTORE OPERATIONS
    // ============================================================

    private fun readSession(): UserSession? {
        return try {
            runBlocking(Dispatchers.IO) {
                sessionReader()
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun saveIfUnchanged(
        oldAccessToken: String,
        oldRefreshToken: String,
        newTokens: LoginResponse
    ): Boolean {

        return try {
            runBlocking(Dispatchers.IO) {

                val latest = sessionReader()

                if (
                    latest.accessToken != oldAccessToken ||
                    latest.refreshToken != oldRefreshToken
                ) {
                    false
                } else {
                    sessionWriter(newTokens)
                    true
                }
            }
        } catch (_: Exception) {
            false
        }
    }

    private fun clearIfUnchanged(
        oldAccessToken: String,
        oldRefreshToken: String
    ) {

        try {
            runBlocking(Dispatchers.IO) {

                val latest = sessionReader()

                if (
                    latest.accessToken == oldAccessToken &&
                    latest.refreshToken == oldRefreshToken
                ) {
                    sessionClearer()
                }
            }
        } catch (_: Exception) {
            // Giữ nguyên session nếu thao tác DataStore lỗi.
        }
    }
}
