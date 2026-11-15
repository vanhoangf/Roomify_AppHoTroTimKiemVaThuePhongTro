
package com.roomify.app.data.remote.interceptor

import kotlinx.coroutines.runBlocking
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Interceptor
import okhttp3.Response
import java.util.Locale

class AuthInterceptor(
    backendBaseUrl: String,
    private val tokenProvider: suspend () -> String?
) : Interceptor {

    companion object {
        const val SKIP_AUTH_HEADER = "X-Roomify-Skip-Auth"
    }

    private val backendUrl: HttpUrl = backendBaseUrl.toHttpUrl()

    override fun intercept(
        chain: Interceptor.Chain
    ): Response {

        val originalRequest = chain.request()

        // Header này chỉ dùng để điều khiển interceptor.
        // Không gửi nó tới backend.
        val skipAuthByHeader = originalRequest
            .header(SKIP_AUTH_HEADER)
            ?.equals("true", ignoreCase = true) == true

        val requestBuilder = originalRequest
            .newBuilder()
            .removeHeader(SKIP_AUTH_HEADER)

        // Chỉ tự động gửi token đến đúng backend Roomify.
        val isRoomifyBackend =
            originalRequest.url.scheme == backendUrl.scheme &&
                    originalRequest.url.host == backendUrl.host &&
                    originalRequest.url.port == backendUrl.port

        val isPublicApi = isPublicEndpoint(
            originalRequest.url.encodedPath
        )

        // Không gửi Authorization đến API công khai
        // hoặc host không phải Roomify.
        if (
            !isRoomifyBackend ||
            skipAuthByHeader ||
            isPublicApi
        ) {
            requestBuilder.removeHeader("Authorization")
            return chain.proceed(requestBuilder.build())
        }

        // OkHttp Interceptor hoạt động đồng bộ.
        // Đọc token từ DataStore bằng runBlocking.
        // Không gọi execute() trên Main Thread.
        val accessToken = runBlocking {
            tokenProvider()
        }?.trim()

        if (!accessToken.isNullOrBlank()) {

            requestBuilder.header(
                "Authorization",
                "Bearer $accessToken"
            )

        } else {

            // Không giữ header cũ nếu không có token.
            requestBuilder.removeHeader("Authorization")
        }

        return chain.proceed(
            requestBuilder.build()
        )
    }

    private fun isPublicEndpoint(
        encodedPath: String
    ): Boolean {

        val path = encodedPath
            .trim('/')
            .lowercase(Locale.ROOT)

        return when {
            path == "api/auth/login" -> true

            path == "api/auth/refresh" -> true

            path == "api/auth/register" -> true

            path.startsWith("api/auth/register/") -> true

            path == "api/health" -> true

            else -> false
        }
    }
}
