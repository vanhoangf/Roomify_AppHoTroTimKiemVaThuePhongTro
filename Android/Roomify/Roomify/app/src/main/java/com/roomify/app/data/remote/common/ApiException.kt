
package com.roomify.app.data.remote.common

class ApiException(
    val httpStatus: Int?,
    val errorCode: String,
    override val message: String,
    val fieldErrors: Map<String, List<String>> = emptyMap(),
    val traceId: String? = null,
    cause: Throwable? = null
) : Exception(message, cause) {

    fun firstFieldError(fieldName: String): String? {
        return fieldErrors.entries
            .firstOrNull {
                it.key.equals(fieldName, ignoreCase = true)
            }
            ?.value
            ?.firstOrNull()
    }

    fun isUnauthorized(): Boolean {
        return httpStatus == 401
    }

    fun isForbidden(): Boolean {
        return httpStatus == 403
    }

    fun isConflict(): Boolean {
        return httpStatus == 409
    }

    fun isValidationError(): Boolean {
        return errorCode == "VALIDATION_ERROR"
    }
}
