
package com.roomify.app.data.repository

import com.roomify.app.data.local.AuthSessionStore
import com.roomify.app.data.remote.api.AuthApi
import com.roomify.app.data.remote.common.ApiResult
import com.roomify.app.data.remote.dto.auth.LoginResponse
import com.roomify.app.data.remote.dto.auth.RegisterStudentRequest

import kotlinx.coroutines.runBlocking

import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer

import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class AuthRepositoryTest {

    private lateinit var server: MockWebServer
    private lateinit var sessionStore: FakeSessionStore
    private lateinit var repository: AuthRepository

    private class FakeSessionStore : AuthSessionStore {

        var session: LoginResponse? = null
        var refreshToken: String? = null
        var cleared = false

        override suspend fun saveSession(
            response: LoginResponse
        ) {
            session = response
            refreshToken = response.refreshToken
        }

        override suspend fun updateTokens(
            response: LoginResponse
        ) {
            session = response
            refreshToken = response.refreshToken
        }

        override suspend fun getRefreshToken(): String? {
            return refreshToken
        }

        override suspend fun clearSession() {
            session = null
            refreshToken = null
            cleared = true
        }
    }

    private val userJson = """
        {
            "userId": 1,
            "email": "student01@roomify.demo",
            "fullName": "Test Student",
            "role": "STUDENT",
            "status": "ACTIVE",
            "createdAt": "2026-11-16T00:00:00Z"
        }
    """.trimIndent()

    private fun loginJson(
        access: String,
        refresh: String
    ): String {
        return """
            {
                "accessToken": "$access",
                "refreshToken": "$refresh",
                "tokenType": "Bearer",
                "accessTokenExpiresAt": "2026-11-17T00:00:00Z",
                "refreshTokenExpiresAt": "2026-11-23T00:00:00Z",
                "user": $userJson
            }
        """.trimIndent()
    }

    @Before
    fun setUp() {

        server = MockWebServer()
        server.start()

        sessionStore = FakeSessionStore()

        val api = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(
                GsonConverterFactory.create()
            )
            .build()
            .create(AuthApi::class.java)

        repository = AuthRepository(
            authApi = api,
            sessionStore = sessionStore
        )
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
    // TEST 1 - LOGIN SAVES TOKENS
    // ============================================================

    @Test
    fun loginSuccessSavesSession() = runBlocking {

        enqueue(
            200,
            loginJson("access-123", "refresh-123")
        )

        val result = repository.login(
            email = "student01@roomify.demo",
            password = "Test123456"
        )

        assertTrue(result is ApiResult.Success)

        assertEquals(
            "access-123",
            sessionStore.session?.accessToken
        )

        assertEquals(
            "refresh-123",
            sessionStore.refreshToken
        )

        assertEquals(
            "STUDENT",
            sessionStore.session?.user?.role
        )
    }

    // ============================================================
    // TEST 2 - WRONG LOGIN DOES NOT SAVE TOKENS
    // ============================================================

    @Test
    fun loginFailureDoesNotSaveSession() = runBlocking {

        enqueue(
            401,
            """
            {
                "success": false,
                "statusCode": 401,
                "errorCode": "INVALID_CREDENTIALS",
                "message": "Thông tin đăng nhập không đúng."
            }
            """.trimIndent()
        )

        val result = repository.login(
            email = "student01@roomify.demo",
            password = "wrong-password"
        )

        assertTrue(result is ApiResult.Error)
        assertNull(sessionStore.session)
    }

    // ============================================================
    // TEST 3 - REGISTER DOES NOT AUTO LOGIN
    // ============================================================

    @Test
    fun registerDoesNotCreateSession() = runBlocking {

        enqueue(201, userJson)

        val result = repository.registerStudent(
            RegisterStudentRequest(
                fullName = "Test Student",
                email = "student01@roomify.demo",
                password = "Test123456"
            )
        )

        assertTrue(result is ApiResult.Success)
        assertNull(sessionStore.session)
        assertNull(sessionStore.refreshToken)
    }

    // ============================================================
    // TEST 4 - REFRESH UPDATES BOTH TOKENS
    // ============================================================

    @Test
    fun refreshUpdatesBothTokens() = runBlocking {

        sessionStore.refreshToken = "old-refresh"

        enqueue(
            200,
            loginJson("new-access", "new-refresh")
        )

        val result = repository.refreshSession()

        assertTrue(result is ApiResult.Success)

        assertEquals(
            "new-access",
            sessionStore.session?.accessToken
        )

        assertEquals(
            "new-refresh",
            sessionStore.refreshToken
        )

        val request = server.takeRequest()

        assertEquals(
            "/api/auth/refresh",
            request.url.encodedPath
        )
    }

    // ============================================================
    // TEST 5 - LOGOUT CLEARS LOCAL SESSION
    // ============================================================

    @Test
    fun logoutClearsSessionEvenWhenServerFails() = runBlocking {

        sessionStore.refreshToken = "refresh-123"

        enqueue(
            500,
            """{"message":"Server error"}"""
        )

        val result = repository.logout()

        assertTrue(result is ApiResult.Error)
        assertTrue(sessionStore.cleared)
        assertNull(sessionStore.refreshToken)
    }

    // ============================================================
    // TEST 6 - GET CURRENT USER
    // ============================================================

    @Test
    fun getMeReturnsCurrentUser() = runBlocking {

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

        val result = repository.getMe()

        assertTrue(result is ApiResult.Success)

        val user = (result as ApiResult.Success).data

        assertEquals("STUDENT", user?.role)
        assertEquals("1", user?.userId)
    }


    @Test
    fun logoutSuccessClearsSession() = runBlocking {

        sessionStore.refreshToken = "refresh-123"

        enqueue(
            200,
            """{"message":"Đăng xuất thành công."}"""
        )

        val result = repository.logout()

        // Backend trả HTTP thành công
        assertTrue(result is ApiResult.Success)

        // Session local đã được xóa
        assertTrue(sessionStore.cleared)

        assertNull(sessionStore.refreshToken)

        assertNull(sessionStore.session)

        // Request gửi đúng endpoint
        val request = server.takeRequest()

        assertEquals(
            "POST",
            request.method
        )

        assertEquals(
            "/api/auth/logout",
            request.url.encodedPath
        )
    }

}
