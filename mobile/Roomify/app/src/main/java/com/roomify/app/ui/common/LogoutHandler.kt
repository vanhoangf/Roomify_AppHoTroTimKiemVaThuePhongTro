
package com.roomify.app.ui.common

import android.content.Intent
import android.view.View
import android.widget.Toast

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

object LogoutHandler {

    fun logout(
        activity: AppCompatActivity,
        logoutButton: View
    ) {

        // Ngăn nhấn Đăng xuất liên tiếp
        if (!logoutButton.isEnabled) {
            return
        }

        logoutButton.isEnabled = false

        activity.lifecycleScope.launch {

            try {

                // 1. Khởi tạo networking
                RetrofitClient.initialize(
                    activity.applicationContext
                )

                // 2. TokenManager
                val tokenManager = TokenManager(
                    activity.applicationContext
                )

                // 3. AuthRepository
                val repository = AuthRepository(
                    authApi = RetrofitClient.createService(
                        AuthApi::class.java
                    ),
                    sessionStore = TokenManagerSessionStore(
                        tokenManager
                    )
                )

                // 4. Gọi API logout
                // AuthRepository đã xử lý xóa session local.
                val result = repository.logout()

                // 5. Kiểm tra token đã bị xóa khỏi DataStore
                val session = tokenManager.getSession()

                val stillHasTokens =
                    !session.accessToken.isNullOrBlank() ||
                            !session.refreshToken.isNullOrBlank()

                if (stillHasTokens) {

                    Toast.makeText(
                        activity,
                        "Không thể xóa phiên đăng nhập. " +
                                "Vui lòng thử lại.",
                        Toast.LENGTH_LONG
                    ).show()

                    return@launch
                }

                // 6. Phân biệt lỗi logout trên server
                // với logout thành công trên thiết bị.
                if (result is ApiResult.Error) {

                    val errorCode = result.exception.errorCode

                    if (errorCode == "SESSION_CLEAR_FAILED") {

                        Toast.makeText(
                            activity,
                            "Không thể hoàn tất đăng xuất.",
                            Toast.LENGTH_LONG
                        ).show()

                        return@launch
                    }

                    if (errorCode != "NO_REFRESH_TOKEN") {

                        Toast.makeText(
                            activity,
                            "Đã đăng xuất trên thiết bị. " +
                                    "Máy chủ chưa xác nhận thu hồi phiên.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

                // 7. Quay về màn hình đăng nhập
                val intent = Intent(
                    activity,
                    MainActivity::class.java
                ).apply {

                    flags =
                        Intent.FLAG_ACTIVITY_NEW_TASK or
                                Intent.FLAG_ACTIVITY_CLEAR_TASK
                }

                activity.startActivity(intent)
                activity.finish()

            } catch (e: CancellationException) {

                throw e

            } catch (e: Exception) {

                Toast.makeText(
                    activity,
                    "Lỗi đăng xuất: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()

            } finally {

                if (
                    !activity.isFinishing &&
                    !activity.isDestroyed
                ) {
                    logoutButton.isEnabled = true
                }
            }
        }
    }
}
