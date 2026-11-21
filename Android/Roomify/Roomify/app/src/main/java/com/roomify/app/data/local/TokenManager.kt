
package com.roomify.app.data.local

import android.content.Context

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

import com.roomify.app.data.remote.dto.auth.LoginResponse

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

import java.io.IOException

// ============================================================
// DATASTORE SINGLETON
// ============================================================

private val Context.roomifySessionDataStore:
        DataStore<Preferences> by preferencesDataStore(
    name = "roomify_session"
)

class TokenManager(context: Context) {

    private val appContext = context.applicationContext

    private val dataStore = appContext.roomifySessionDataStore

    // ============================================================
    // PREFERENCE KEYS
    // ============================================================

    private object Keys {

        val ACCESS_TOKEN =
            stringPreferencesKey("access_token")

        val REFRESH_TOKEN =
            stringPreferencesKey("refresh_token")

        val TOKEN_TYPE =
            stringPreferencesKey("token_type")

        val ACCESS_TOKEN_EXPIRES_AT =
            stringPreferencesKey("access_token_expires_at")

        val REFRESH_TOKEN_EXPIRES_AT =
            stringPreferencesKey("refresh_token_expires_at")

        val USER_ID =
            longPreferencesKey("user_id")

        val EMAIL =
            stringPreferencesKey("email")

        val FULL_NAME =
            stringPreferencesKey("full_name")

        val ROLE =
            stringPreferencesKey("role")

        val STATUS =
            stringPreferencesKey("status")
    }

    // ============================================================
    // OBSERVE SESSION
    // ============================================================

    val sessionFlow: Flow<UserSession> =
        dataStore.data
            .catch { exception ->

                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->

                UserSession(

                    accessToken =
                        preferences[Keys.ACCESS_TOKEN],

                    refreshToken =
                        preferences[Keys.REFRESH_TOKEN],

                    tokenType =
                        preferences[Keys.TOKEN_TYPE]
                            ?: "Bearer",

                    accessTokenExpiresAt =
                        preferences[Keys.ACCESS_TOKEN_EXPIRES_AT],

                    refreshTokenExpiresAt =
                        preferences[Keys.REFRESH_TOKEN_EXPIRES_AT],

                    userId =
                        preferences[Keys.USER_ID],

                    email =
                        preferences[Keys.EMAIL],

                    fullName =
                        preferences[Keys.FULL_NAME],

                    role =
                        preferences[Keys.ROLE],

                    status =
                        preferences[Keys.STATUS]
                )
            }

    // ============================================================
    // SAVE LOGIN SESSION
    // ============================================================

    suspend fun saveSession(
        response: LoginResponse
    ) {

        dataStore.edit { preferences ->

            // TOKENS

            preferences[Keys.ACCESS_TOKEN] =
                response.accessToken

            preferences[Keys.REFRESH_TOKEN] =
                response.refreshToken

            preferences[Keys.TOKEN_TYPE] =
                response.tokenType

            preferences[Keys.ACCESS_TOKEN_EXPIRES_AT] =
                response.accessTokenExpiresAt

            preferences[Keys.REFRESH_TOKEN_EXPIRES_AT] =
                response.refreshTokenExpiresAt

            // USER

            preferences[Keys.USER_ID] =
                response.user.userId

            preferences[Keys.EMAIL] =
                response.user.email

            preferences[Keys.FULL_NAME] =
                response.user.fullName

            preferences[Keys.ROLE] =
                response.user.role

            preferences[Keys.STATUS] =
                response.user.status
        }
    }

    // ============================================================
    // UPDATE SESSION AFTER REFRESH TOKEN
    // ============================================================

    suspend fun updateTokens(
        response: LoginResponse
    ) {

        // Backend Roomify refresh trả LoginResponse mới.
        // Ghi cả accessToken và refreshToken trong một
        // transaction để tránh lưu lệch hai token.

        saveSession(response)
    }

    // ============================================================
    // GET CURRENT SESSION
    // ============================================================

    suspend fun getSession(): UserSession {

        return sessionFlow.first()
    }

    // ============================================================
    // GET ACCESS TOKEN
    // ============================================================

    suspend fun getAccessToken(): String? {

        return getSession()
            .accessToken
            ?.takeIf { it.isNotBlank() }
    }

    // ============================================================
    // GET REFRESH TOKEN
    // ============================================================

    suspend fun getRefreshToken(): String? {

        return getSession()
            .refreshToken
            ?.takeIf { it.isNotBlank() }
    }

    // ============================================================
    // CHECK STORED SESSION
    // ============================================================

    suspend fun hasSession(): Boolean {

        return getSession().hasStoredSession
    }

    // ============================================================
    // GET USER ROLE
    // ============================================================

    suspend fun getUserRole(): String? {

        return getSession().role
    }

    // ============================================================
    // CLEAR SESSION (LOGOUT)
    // ============================================================

    suspend fun clearSession() {

        dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
