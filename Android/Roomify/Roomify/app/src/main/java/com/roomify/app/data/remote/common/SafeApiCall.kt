
package com.roomify.app.data.remote.common

import kotlinx.coroutines.CancellationException
import retrofit2.Response

suspend fun <T> safeApiCall(
    apiCall: suspend () -> Response<T>
): ApiResult<T> {

    return try {

        val response = apiCall()

        if (response.isSuccessful) {

            ApiResult.Success(
                data = response.body(),
                statusCode = response.code()
            )

        } else {

            ApiResult.Error(
                exception = ApiErrorParser.fromResponse(
                    response
                )
            )

        }

    } catch (exception: CancellationException) {

        // Không được nuốt coroutine cancellation.
        throw exception

    } catch (exception: Exception) {

        ApiResult.Error(
            exception = ApiErrorParser.fromThrowable(
                exception
            )
        )
    }
}
