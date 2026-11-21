package com.roomify.app.data.remote.common

sealed interface ApiResult<out T> {

    data class Success<out T>(
        val data: T?,
        val statusCode: Int
    ) : ApiResult<T>

    data class Error(
        val exception: ApiException
    ) : ApiResult<Nothing>

    data object Loading : ApiResult<Nothing>
}
