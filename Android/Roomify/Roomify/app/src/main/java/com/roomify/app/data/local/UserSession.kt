
package com.roomify.app.data.local

data class UserSession(
    val accessToken: String? = null,
    val refreshToken: String? = null,

    val tokenType: String = "Bearer",

    val accessTokenExpiresAt: String? = null,
    val refreshTokenExpiresAt: String? = null,

    val userId: Long? = null,
    val email: String? = null,
    val fullName: String? = null,
    val role: String? = null,
    val status: String? = null
) {

    val hasStoredSession: Boolean
        get() = !accessToken.isNullOrBlank() &&
                !refreshToken.isNullOrBlank()

    val isStudent: Boolean
        get() = role.equals(
            "STUDENT",
            ignoreCase = true
        )

    val isLandlord: Boolean
        get() = role.equals(
            "LANDLORD",
            ignoreCase = true
        )

    val isAdmin: Boolean
        get() = role.equals(
            "ADMIN",
            ignoreCase = true
        )
}
