
package com.roomify.app.data.remote.interceptor

import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer

import okhttp3.OkHttpClient
import okhttp3.Request

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class AuthInterceptorTest {

    private lateinit var server: MockWebServer

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
    }

    @After
    fun tearDown() {
        server.close()
    }

    private fun createClient(
        tokenProvider: suspend () -> String?
    ): OkHttpClient {

        return OkHttpClient.Builder()
            .addInterceptor(
                AuthInterceptor(
                    backendBaseUrl = server.url("/").toString(),
                    tokenProvider = tokenProvider
                )
            )
            .build()
    }

    private fun sendRequest(
        client: OkHttpClient,
        request: Request
    ) {

        server.enqueue(
            MockResponse.Builder()
                .code(200)
                .body("{}")
                .build()
        )

        client.newCall(request).execute().use {
                response ->
            assertEquals(200, response.code)
        }
    }

    // TEST 1: API có bảo vệ được gắn JWT.
    @Test
    fun protectedApiAddsBearerToken() {

        val client = createClient {
            "test-access-token"
        }

        val request = Request.Builder()
            .url(server.url("/api/auth/me"))
            .build()

        sendRequest(client, request)

        val recorded = server.takeRequest()

        assertEquals(
            "Bearer test-access-token",
            recorded.headers["Authorization"]
        )
    }

    // TEST 2: Login không nhận token.
    @Test
    fun loginApiDoesNotSendToken() {

        val client = createClient {
            "test-access-token"
        }

        val request = Request.Builder()
            .url(server.url("/api/auth/login"))
            .build()

        sendRequest(client, request)

        assertNull(
            server.takeRequest().headers["Authorization"]
        )
    }

    // TEST 3: Refresh không nhận access token cũ.
    @Test
    fun refreshApiDoesNotSendOldToken() {

        val client = createClient {
            "old-access-token"
        }

        val request = Request.Builder()
            .url(server.url("/api/auth/refresh"))
            .build()

        sendRequest(client, request)

        assertNull(
            server.takeRequest().headers["Authorization"]
        )
    }

    // TEST 4: Chưa đăng nhập thì không có Bearer.
    @Test
    fun noSessionDoesNotAddAuthorization() {

        val client = createClient {
            null
        }

        val request = Request.Builder()
            .url(server.url("/api/auth/me"))
            .build()

        sendRequest(client, request)

        assertNull(
            server.takeRequest().headers["Authorization"]
        )
    }

    // TEST 5: Header skip auth được xử lý và loại bỏ.
    @Test
    fun skipAuthHeaderIsNotForwarded() {

        val client = createClient {
            "test-access-token"
        }

        val request = Request.Builder()
            .url(server.url("/api/auth/me"))
            .header(
                AuthInterceptor.SKIP_AUTH_HEADER,
                "true"
            )
            .build()

        sendRequest(client, request)

        val recorded = server.takeRequest()

        assertNull(
            recorded.headers["Authorization"]
        )

        assertNull(
            recorded.headers[
                AuthInterceptor.SKIP_AUTH_HEADER
            ]
        )
    }
}
