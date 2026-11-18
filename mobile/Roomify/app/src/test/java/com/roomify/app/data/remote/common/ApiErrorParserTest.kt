
package com.roomify.app.data.remote.common

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response

class ApiErrorParserTest {

    private fun errorResponse(
        statusCode: Int,
        json: String
    ): Response<Unit> {

        val body = json.toResponseBody(
            "application/json".toMediaType()
        )

        return Response.error(
            statusCode,
            body
        )
    }

    @Test
    fun parseValidationError() {

        val json = """
            {
              "success": false,
              "statusCode": 400,
              "errorCode": "VALIDATION_ERROR",
              "message": "Dữ liệu đầu vào không hợp lệ.",
              "errors": {
                "Email": [
                  "Email không hợp lệ."
                ]
              }
            }
        """.trimIndent()

        val exception = ApiErrorParser.fromResponse(
            errorResponse(400, json)
        )

        assertEquals(
            400,
            exception.httpStatus
        )

        assertEquals(
            "VALIDATION_ERROR",
            exception.errorCode
        )

        assertEquals(
            "Email không hợp lệ.",
            exception.firstFieldError("Email")
        )

        assertTrue(
            exception.isValidationError()
        )
    }

    @Test
    fun parseConflictError() {

        val json = """
            {
              "success": false,
              "statusCode": 409,
              "errorCode": "ROOM_PENDING_REVIEW",
              "message": "Phòng đang chờ duyệt.",
              "errors": null
            }
        """.trimIndent()

        val exception = ApiErrorParser.fromResponse(
            errorResponse(409, json)
        )

        assertEquals(
            "ROOM_PENDING_REVIEW",
            exception.errorCode
        )

        assertTrue(
            exception.isConflict()
        )
    }

    @Test
    fun parseInvalidJsonUsesFallback() {

        val exception = ApiErrorParser.fromResponse(
            errorResponse(500, "not-json")
        )

        assertEquals(
            500,
            exception.httpStatus
        )

        assertEquals(
            "HTTP_500",
            exception.errorCode
        )
    }
}
