
package com.roomify.app.data.remote.common

import com.google.gson.Gson
import com.google.gson.JsonElement
import retrofit2.HttpException
import retrofit2.Response
import com.roomify.app.data.remote.dto.common.ApiErrorResponse
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLException
import kotlinx.coroutines.CancellationException

object ApiErrorParser {

    private val gson = Gson()

    // ============================================================
    // PARSE HTTP ERROR RESPONSE
    // ============================================================

    fun fromResponse(
        response: Response<*>
    ): ApiException {

        val statusCode = response.code()

        val rawError = try {
            response.errorBody()?.string()
        } catch (_: IOException) {
            null
        }

        val parsedError = try {
            rawError
                ?.takeIf { it.isNotBlank() }
                ?.let {
                    gson.fromJson(
                        it,
                        ApiErrorResponse::class.java
                    )
                }
        } catch (_: Exception) {
            null
        }

        val errorCode = parsedError
            ?.errorCode
            ?.takeIf { it.isNotBlank() }
            ?: "HTTP_$statusCode"

        val message = parsedError
            ?.message
            ?.takeIf { it.isNotBlank() }
            ?: defaultMessage(statusCode)

        val fieldErrors = parseFieldErrors(
            parsedError?.errors
        )

        return ApiException(
            httpStatus = statusCode,
            errorCode = errorCode,
            message = message,
            fieldErrors = fieldErrors,
            traceId = parsedError?.traceId
        )
    }

    // ============================================================
    // PARSE NETWORK / OTHER EXCEPTION
    // ============================================================

    fun fromThrowable(
        throwable: Throwable
    ): ApiException {

        if (throwable is CancellationException) {
            throw throwable
        }

        return when (throwable) {

            is HttpException -> {
                val response = throwable.response()

                if (response != null) {
                    fromResponse(response)
                } else {
                    ApiException(
                        httpStatus = throwable.code(),
                        errorCode = "HTTP_ERROR",
                        message = defaultMessage(
                            throwable.code()
                        ),
                        cause = throwable
                    )
                }
            }

            is SocketTimeoutException -> {
                ApiException(
                    httpStatus = null,
                    errorCode = "NETWORK_TIMEOUT",
                    message = "Kết nối quá thời gian chờ. Vui lòng thử lại.",
                    cause = throwable
                )
            }

            is UnknownHostException,
            is ConnectException -> {
                ApiException(
                    httpStatus = null,
                    errorCode = "NETWORK_UNAVAILABLE",
                    message = "Không thể kết nối đến máy chủ Roomify.",
                    cause = throwable
                )
            }

            is SSLException -> {
                ApiException(
                    httpStatus = null,
                    errorCode = "SSL_ERROR",
                    message = "Không thể thiết lập kết nối bảo mật.",
                    cause = throwable
                )
            }

            is IOException -> {
                ApiException(
                    httpStatus = null,
                    errorCode = "NETWORK_ERROR",
                    message = "Đã xảy ra lỗi kết nối mạng.",
                    cause = throwable
                )
            }

            else -> {
                ApiException(
                    httpStatus = null,
                    errorCode = "UNKNOWN_ERROR",
                    message = "Đã xảy ra lỗi không mong muốn.",
                    cause = throwable
                )
            }
        }
    }

    // ============================================================
    // PARSE VALIDATION ERRORS
    // ============================================================

    private fun parseFieldErrors(
        errors: JsonElement?
    ): Map<String, List<String>> {

        if (errors == null || !errors.isJsonObject) {
            return emptyMap()
        }

        val result = mutableMapOf<String, List<String>>()

        for ((field, value) in errors.asJsonObject.entrySet()) {

            if (value.isJsonNull) {
                continue
            }

            val messages = when {

                value.isJsonArray -> {
                    value.asJsonArray
                        .mapNotNull { item ->
                            if (item.isJsonPrimitive) {
                                item.asString
                            } else {
                                null
                            }
                        }
                }

                value.isJsonPrimitive -> {
                    listOf(value.asString)
                }

                else -> {
                    emptyList()
                }
            }

            if (messages.isNotEmpty()) {
                result[field] = messages
            }
        }

        return result
    }

    // ============================================================
    // FALLBACK MESSAGES
    // ============================================================

    private fun defaultMessage(
        statusCode: Int
    ): String {

        return when (statusCode) {

            400 -> "Dữ liệu gửi lên không hợp lệ."

            401 -> "Phiên đăng nhập không hợp lệ hoặc đã hết hạn."

            403 -> "Bạn không có quyền thực hiện thao tác này."

            404 -> "Không tìm thấy dữ liệu yêu cầu."

            409 -> "Thao tác không thể thực hiện do xung đột dữ liệu."

            429 -> "Bạn thực hiện quá nhiều yêu cầu. Vui lòng thử lại sau."

            in 500..599 -> "Máy chủ đang gặp sự cố. Vui lòng thử lại sau."

            else -> "Yêu cầu không thành công."
        }
    }
}
