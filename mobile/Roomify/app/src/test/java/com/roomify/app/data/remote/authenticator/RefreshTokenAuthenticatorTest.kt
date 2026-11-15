
package com.roomify.app.data.remote.authenticator

import com.roomify.app.data.local.UserSession
import com.roomify.app.data.remote.interceptor.AuthInterceptor

import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer

import okhttp3.OkHttpClient
import okhttp3.Request

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test

class RefreshTokenAuthenticatorTest {

    private lateinit var server: MockWebServer

    private var session = UserSession(
        accessToken = "old-access-token",
        refreshToken = "old-refresh-token"
    )

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
    }

    @After
    fun tearDown() {
        server.close()
    }

    private fun createClient(): OkHttpClient {

        val baseUrl = server.url("/").toString()

        val interceptor = AuthInterceptor(
            backendBaseUrl = baseUrl,
            tokenProvider = { session.accessToken }
        )

        val authenticator =
            RefreshTokenAuthenticator(
                backendBaseUrl = baseUrl,

                sessionReader = {
                    session
                },

                sessionWriter = { response ->
                    session = session.copy(
                        accessToken = response.accessToken,
                        refreshToken = response.refreshToken
                    )
                },

                sessionClearer = {
                    session = UserSession()
                }
            )

        return OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .authenticator(authenticator)
            .build()
    }

    private fun protectedRequest(): Request {
        return Request.Builder()
            .url(server.url("/api/auth/me"))
            .build()
    }

    @Test
    fun refreshSuccessRetriesOriginalRequest() {

        server.enqueue(
            MockResponse.Builder()
                .code(401)
                .build()
        )

        val newTokenJson = """
            {
              "accessToken": "new-access-token",
              "refreshToken": "new-refresh-token",
              "tokenType": "Bearer",
              "accessTokenExpiresAt": "2026-11-16T12:00:00Z",
              "refreshTokenExpiresAt": "2026-11-22T12:00:00Z",
              "user": {
                "userId": 1,
                "email": "student01@roomify.demo",
                "fullName": "Test Student",
                "role": "STUDENT",
                "status": "ACTIVE",
                "createdAt": "2026-11-01T00:00:00Z"
              }
            }
        """.trimIndent()

        server.enqueue(
            MockResponse.Builder()
                .code(200)
                .body(newTokenJson)
                .build()
        )

        server.enqueue(
            MockResponse.Builder()
                .code(200)
                .body("{}")
                .build()
        )

        val client = createClient()

        client.newCall(protectedRequest())
            .execute()
            .use { response ->
                assertEquals(200, response.code)
            }

        val original = server.takeRequest()
        val refresh = server.takeRequest()
        val retry = server.takeRequest()

        assertEquals(
            "Bearer old-access-token",
            original.headers["Authorization"]
        )

        assertEquals(
            "/api/auth/refresh",
            refresh.url.encodedPath
        )


        assertNull(
            refresh.headers["Authorization"]
        )

        assertEquals(
            "Bearer new-access-token",
            retry.headers["Authorization"]
        )

        assertEquals(
            "new-access-token",
            session.accessToken
        )

        assertEquals(
            "new-refresh-token",
            session.refreshToken
        )
    }

    @Test
    fun invalidRefreshTokenClearsSession() {

        server.enqueue(
            MockResponse.Builder()
                .code(401)
                .build()
        )

        server.enqueue(
            MockResponse.Builder()
                .code(401)
                .body("""{"errorCode":"INVALID_REFRESH_TOKEN"}""")
                .build()
        )

        val client = createClient()

        client.newCall(protectedRequest())
            .execute()
            .use { response ->
                assertEquals(401, response.code)
            }

        assertNull(session.accessToken)
        assertNull(session.refreshToken)
        assertFalse(session.hasStoredSession)
    }

    @Test
    fun missingSessionDoesNotRefresh() {

        session = UserSession()

        server.enqueue(
            MockResponse.Builder()
                .code(401)
                .build()
        )

        val client = createClient()

        client.newCall(protectedRequest())
            .execute()
            .use { response ->
                assertEquals(401, response.code)
            }

        assertEquals(1, server.requestCount)
        assertNull(session.accessToken)
    }
}
