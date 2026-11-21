
package com.roomify.app.data.remote.network

import android.content.Context

import com.roomify.app.BuildConfig
import com.roomify.app.data.local.TokenManager
import com.roomify.app.data.remote.authenticator.RefreshTokenAuthenticator
import com.roomify.app.data.remote.interceptor.AuthInterceptor

import okhttp3.Authenticator
import okhttp3.OkHttpClient

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

import java.util.concurrent.TimeUnit

object RetrofitClient {

    private val lock = Any()

    @Volatile
    private var retrofitInstance: Retrofit? = null

    // ============================================================
    // INITIALIZE NETWORKING CORE
    // ============================================================

    fun initialize(context: Context) {

        if (retrofitInstance != null) {
            return
        }

        synchronized(lock) {

            if (retrofitInstance != null) {
                return@synchronized
            }

            val appContext = context.applicationContext

            // 1. TOKEN MANAGER
            val tokenManager = TokenManager(appContext)

            // 2. AUTH INTERCEPTOR
            val authInterceptor = AuthInterceptor(
                backendBaseUrl = BuildConfig.BASE_URL,

                tokenProvider = {
                    tokenManager.getAccessToken()
                }
            )

            // 3. SEPARATE CLIENT FOR REFRESH TOKEN
            // Không dùng Authenticator để refresh chính nó.
            val refreshHttpClient = OkHttpClient.Builder()
                .authenticator(Authenticator.NONE)
                .followRedirects(false)
                .followSslRedirects(false)
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build()

            // 4. REFRESH TOKEN AUTHENTICATOR
            val refreshAuthenticator =
                RefreshTokenAuthenticator(
                    backendBaseUrl = BuildConfig.BASE_URL,

                    sessionReader = {
                        tokenManager.getSession()
                    },

                    sessionWriter = { response ->
                        tokenManager.updateTokens(response)
                    },

                    sessionClearer = {
                        tokenManager.clearSession()
                    },

                    refreshClient = refreshHttpClient
                )

            // 5. MAIN OKHTTP CLIENT
            val okHttpClient = OkHttpClient.Builder()

                .addInterceptor(authInterceptor)

                .authenticator(refreshAuthenticator)

                .connectTimeout(15, TimeUnit.SECONDS)

                .readTimeout(30, TimeUnit.SECONDS)

                .writeTimeout(30, TimeUnit.SECONDS)

                .build()

            // 6. RETROFIT
            retrofitInstance = Retrofit.Builder()

                .baseUrl(BuildConfig.BASE_URL)

                .client(okHttpClient)

                .addConverterFactory(
                    GsonConverterFactory.create()
                )

                .build()
        }
    }

    // ============================================================
    // CREATE API SERVICE
    // ============================================================

    fun <T : Any> createService(
        serviceClass: Class<T>
    ): T {

        val retrofit = retrofitInstance
            ?: error(
                "RetrofitClient chưa được khởi tạo. " +
                        "Hãy gọi RetrofitClient.initialize(context) trước."
            )

        return retrofit.create(serviceClass)
    }
}
