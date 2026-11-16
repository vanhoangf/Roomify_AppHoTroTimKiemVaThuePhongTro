
package com.roomify.app.data.local

import com.roomify.app.data.remote.dto.auth.LoginResponse

interface AuthSessionStore {

    suspend fun saveSession(response: LoginResponse)

    suspend fun updateTokens(response: LoginResponse)

    suspend fun getRefreshToken(): String?

    suspend fun clearSession()
}

// Kết nối interface với TokenManager đã có ở Bước 2.5.
class TokenManagerSessionStore(
    private val tokenManager: TokenManager
) : AuthSessionStore {

    override suspend fun saveSession(
        response: LoginResponse
    ) {
        tokenManager.saveSession(response)
    }

    override suspend fun updateTokens(
        response: LoginResponse
    ) {
        tokenManager.updateTokens(response)
    }

    override suspend fun getRefreshToken(): String? {
        return tokenManager.getRefreshToken()
    }

    override suspend fun clearSession() {
        tokenManager.clearSession()
    }
}
