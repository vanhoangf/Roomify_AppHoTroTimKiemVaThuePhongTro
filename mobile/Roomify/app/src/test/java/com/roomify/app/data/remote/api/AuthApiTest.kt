
package com.roomify.app.data.remote.api

import com.roomify.app.data.remote.dto.auth.LoginRequest
import com.roomify.app.data.remote.dto.auth.LogoutRequest
import com.roomify.app.data.remote.dto.auth.RefreshTokenRequest
import com.roomify.app.data.remote.dto.auth.RegisterLandlordRequest
import com.roomify.app.data.remote.dto.auth.RegisterStudentRequest

import kotlinx.coroutines.runBlocking

import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class AuthApiTest {

    private lateinit var server: MockWebServer
    private lateinit var api: AuthApi

    private val userJson = """
        {
            "userId": 1,
            "email": "student01@roomify.demo",
            "fullName": "Test Student",
            "role": "STUDENT",
            "status": "ACTIVE",
            "createdAt": "2026-11-15T00:00:00Z"
        }
    """.trimIndent()

    private val loginJson: String
        get() = """
            {
                "accessToken": "fake-access-token",
                "refreshToken": "fake-refresh-token",
                "tokenType": "Bearer",
                "accessTokenExpiresAt": "2026-11-16T00:00:00Z",
                "refreshTokenExpiresAt": "2026-11-22T00:00:00Z",
                "user": $userJson
            }
        """.trimIndent()

    @Before
    fun setUp() {

        server = MockWebServer()
        server.start()

        api = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(
                GsonConverterFactory.create()
            )
            .build()
            .create(AuthApi::class.java)
    }

    @After
    fun tearDown() {
        server.close()
    }

    private fun enqueue(
        code: Int,
        body: String
    ) {
        server.enqueue(
            MockResponse.Builder()
                .code(code)
                .body(body)
                .build()
        )
    }

    // ============================================================
    // TEST 1 - LOGIN AND REFRESH
    // ============================================================

    @Test
    fun loginAndRefreshEndpoints() = runBlocking {

        enqueue(200, loginJson)

        val loginResult = api.login(
            LoginRequest(
                email = "student01@roomify.demo",
                password = "test-password"
            )
        )

        assertTrue(loginResult.isSuccessful)

        assertEquals(
            "fake-access-token",
            loginResult.body()?.accessToken
        )

        val loginRequest = server.takeRequest()

        assertEquals("POST", loginRequest.method)

        assertEquals(
            "/api/auth/login",
            loginRequest.url.encodedPath
        )

        enqueue(200, loginJson)

        val refreshResult = api.refreshToken(
            RefreshTokenRequest("fake-refresh-token")
        )

        assertTrue(refreshResult.isSuccessful)

        val refreshRequest = server.takeRequest()

        assertEquals("POST", refreshRequest.method)

        assertEquals(
            "/api/auth/refresh",
            refreshRequest.url.encodedPath
        )
    }

    // ============================================================
    // TEST 2 - REGISTER STUDENT AND LANDLORD
    // ============================================================

    @Test
    fun registerEndpoints() = runBlocking {

        enqueue(201, userJson)

        val studentResult = api.registerStudent(
            RegisterStudentRequest(
                fullName = "Test Student",
                email = "student01@roomify.demo",
                password = "Test123456"
            )
        )

        assertEquals(201, studentResult.code())

        assertEquals(
            "STUDENT",
            studentResult.body()?.role
        )

        val studentRequest = server.takeRequest()

        assertEquals("POST", studentRequest.method)

        assertEquals(
            "/api/auth/register/student",
            studentRequest.url.encodedPath
        )

        enqueue(201, userJson)

        val landlordResult = api.registerLandlord(
            RegisterLandlordRequest(
                fullName = "Test Landlord",
                email = "landlord01@roomify.demo",
                password = "Test123456"
            )
        )

        assertEquals(201, landlordResult.code())

        val landlordRequest = server.takeRequest()

        assertEquals("POST", landlordRequest.method)

        assertEquals(
            "/api/auth/register/landlord",
            landlordRequest.url.encodedPath
        )
    }

    // ============================================================
    // TEST 3 - LOGOUT AND GET CURRENT USER
    // ============================================================

    @Test
    fun logoutAndMeEndpoints() = runBlocking {

        enqueue(
            200,
            """{"message":"Đăng xuất thành công."}"""
        )

        val logoutResult = api.logout(
            LogoutRequest("fake-refresh-token")
        )

        assertTrue(logoutResult.isSuccessful)

        val logoutRequest = server.takeRequest()

        assertEquals("POST", logoutRequest.method)

        assertEquals(
            "/api/auth/logout",
            logoutRequest.url.encodedPath
        )

        enqueue(
            200,
            """
            {
                "userId": "1",
                "email": "student01@roomify.demo",
                "fullName": "Test Student",
                "role": "STUDENT"
            }
            """.trimIndent()
        )

        val meResult = api.getMe()

        assertTrue(meResult.isSuccessful)

        assertEquals(
            "STUDENT",
            meResult.body()?.role
        )

        val meRequest = server.takeRequest()

        assertEquals("GET", meRequest.method)

        assertEquals(
            "/api/auth/me",
            meRequest.url.encodedPath
        )
    }
}
