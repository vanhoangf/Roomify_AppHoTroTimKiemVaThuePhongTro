
package com.roomify.app.ui.common

import android.content.Intent

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope

import com.roomify.app.MainActivity
import com.roomify.app.data.local.TokenManager
import com.roomify.app.data.local.TokenManagerSessionStore
import com.roomify.app.data.remote.api.AuthApi
import com.roomify.app.data.remote.common.ApiResult
import com.roomify.app.data.remote.network.RetrofitClient
import com.roomify.app.data.repository.AuthRepository

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

object RoleSessionGuard {

    fun verify(
        activity: AppCompatActivity,
        expectedRole: String,
        onNetworkError: (String) -> Unit
    ) {

        activity.lifecycleScope.launch {

            try {

                val tokenManager = TokenManager(
                    activity.applicationContext
                )

                // 1. Kiểm tra có token đã lưu không.
                if (!tokenManager.hasSession()) {

                    openLogin(activity)
                    return@launch
                }

                // 2. Khởi tạo networking nếu cần.
                RetrofitClient.initialize(
                    activity.applicationContext
                )

                val authRepository = AuthRepository(
                    authApi = RetrofitClient.createService(
                        AuthApi::class.java
                    ),
                    sessionStore = TokenManagerSessionStore(
                        tokenManager
                    )
                )

                // 3. Kiểm tra JWT trên backend.
                when (val result = authRepository.getMe()) {

                    is ApiResult.Success -> {

                        val actualRole = result.data?.role

                        if (!actualRole.equals(
                                expectedRole,
                                ignoreCase = true
                            )
                        ) {

                            // Role khác: chuyển tới màn hình đúng.
                            val navigated = RoleRouter.openHome(
                                activity,
                                actualRole
                            )

                            if (!navigated) {

                                // Role không hỗ trợ trên Android.
                                tokenManager.clearSession()
                                openLogin(activity)
                            }
                        }
                    }

                    is ApiResult.Error -> {

                        if (result.exception.httpStatus == 401) {

                            // Session hết hạn hoặc không hợp lệ.
                            tokenManager.clearSession()
                            openLogin(activity)

                        } else {

                            // Mất mạng, lỗi 5xx:
                            // Không xóa phiên đăng nhập.
                            onNetworkError(
                                result.exception.message
                            )
                        }
                    }

                    ApiResult.Loading -> Unit
                }

            } catch (e: CancellationException) {

                throw e

            } catch (e: Exception) {

                onNetworkError(
                    "Không thể kiểm tra phiên đăng nhập."
                )
            }
        }
    }

    private fun openLogin(
        activity: AppCompatActivity
    ) {

        val intent = Intent(
            activity,
            MainActivity::class.java
        ).apply {

            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        activity.startActivity(intent)
    }
}
