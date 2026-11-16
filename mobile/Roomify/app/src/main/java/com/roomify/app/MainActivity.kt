
package com.roomify.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log

import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope

import com.roomify.app.data.local.TokenManager
import com.roomify.app.data.local.TokenManagerSessionStore
import com.roomify.app.data.local.UserSession
import com.roomify.app.data.remote.api.AuthApi
import com.roomify.app.data.remote.common.ApiResult
import com.roomify.app.data.remote.network.RetrofitClient
import com.roomify.app.data.repository.AuthRepository
import com.roomify.app.databinding.ActivityMainBinding

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

import com.roomify.app.ui.common.RoleRouter
class MainActivity : AppCompatActivity() {

    private suspend fun verifySessionAndRoute() {

        val result = authRepository.getMe()

        when (result) {

            is ApiResult.Success -> {

                val role = result.data?.role

                if (RoleRouter.openHome(this, role)) {

                    Log.d(
                        TAG,
                        "ROLE ROUTING SUCCESS | role=$role"
                    )

                } else {

                    // ADMIN thuộc hệ thống Web.
                    // Role không được hỗ trợ thì không mở Home.
                    tokenManager.clearSession()

                    renderSession(
                        tokenManager.getSession()
                    )

                    binding.tvStatus.text =
                        "Tài khoản này không hỗ trợ đăng nhập " +
                                "trên ứng dụng Android. ADMIN sử dụng Web."
                }
            }

            is ApiResult.Error -> {

                if (result.exception.httpStatus == 401) {

                    // JWT không thể xác thực hoặc refresh.
                    tokenManager.clearSession()

                    renderSession(
                        tokenManager.getSession()
                    )

                    binding.tvStatus.text =
                        "Phiên đăng nhập không còn hợp lệ. " +
                                "Vui lòng đăng nhập lại."

                } else {

                    // Lỗi mạng hoặc backend: giữ session.
                    binding.tvStatus.text =
                        "Không thể xác minh phiên đăng nhập: " +
                                result.exception.message
                }
            }

            ApiResult.Loading -> Unit
        }
    }


    private lateinit var binding: ActivityMainBinding

    private lateinit var tokenManager: TokenManager
    private lateinit var authRepository: AuthRepository

    private var hasSession = false
    private var isBusy = false

    private var pendingNetworkAction: (() -> Unit)? = null

    companion object {
        private const val TAG = "RoomifyAuth"
    }

    // ============================================================
    // 1. LOCAL NETWORK PERMISSION - ANDROID 17
    // ============================================================

    private val permissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->

            val action = pendingNetworkAction
            pendingNetworkAction = null

            if (granted) {
                Log.d(TAG, "ACCESS_LOCAL_NETWORK granted")
                action?.invoke()
            } else {
                binding.tvStatus.text =
                    "Chưa được cấp quyền truy cập mạng cục bộ."
            }
        }

    // ============================================================
    // 2. ON CREATE
    // ============================================================

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(
            binding.root
        ) { view, insets ->

            val bars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars()
            )

            view.setPadding(
                bars.left,
                bars.top,
                bars.right,
                bars.bottom
            )

            insets
        }

        // Khởi tạo networking
        RetrofitClient.initialize(applicationContext)

        // TokenManager dùng chung DataStore
        tokenManager = TokenManager(applicationContext)

        // Kết nối AuthRepository
        authRepository = AuthRepository(
            authApi = RetrofitClient.createService(
                AuthApi::class.java
            ),
            sessionStore = TokenManagerSessionStore(
                tokenManager
            )
        )

        // Nút đăng nhập
        binding.btnLogin.setOnClickListener {
            withLocalNetworkPermission {
                login()
            }
        }

        // Nút xem session local
        binding.btnSession.setOnClickListener {
            checkSession()
        }

        // Nút kiểm tra JWT
        binding.btnMe.setOnClickListener {
            withLocalNetworkPermission {
                getMe()
            }
        }

        updateButtons()
        loadStoredSession()
    }

    // ============================================================
    // 3. CHECK LOCAL NETWORK PERMISSION
    // ============================================================

    private fun withLocalNetworkPermission(
        action: () -> Unit
    ) {

        val needsPermission =
            BuildConfig.DEBUG &&
                    Build.VERSION.SDK_INT >= 37 &&
                    ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_LOCAL_NETWORK
                    ) != PackageManager.PERMISSION_GRANTED

        if (needsPermission) {

            pendingNetworkAction = action

            permissionLauncher.launch(
                Manifest.permission.ACCESS_LOCAL_NETWORK
            )

        } else {
            action()
        }
    }

    // ============================================================
    // 4. LOGIN
    // ============================================================

    private fun login() {

        val email = binding.edtEmail.text
            .toString()
            .trim()

        val password = binding.edtPassword.text
            .toString()

        if (email.isBlank() || password.isBlank()) {
            binding.tvStatus.text =
                "Vui lòng nhập Email và mật khẩu."
            return
        }

        setBusy(true)

        binding.tvStatus.text = "Đang đăng nhập..."

        lifecycleScope.launch {

            try {

                val result = authRepository.login(
                    email = email,
                    password = password
                )

                when (result) {


                    is ApiResult.Success -> {

                        val session = tokenManager.getSession()

                        renderSession(session)

                        binding.edtPassword.text?.clear()

                        binding.tvStatus.text =
                            "Đăng nhập thành công. Đang xác minh tài khoản..."

                        Log.d(
                            TAG,
                            "LOGIN SUCCESS | " +
                                    "saved=${session.hasStoredSession} | " +
                                    "role=${session.role}"
                        )

                        // Kiểm tra JWT trên backend và mở Home.
                        verifySessionAndRoute()
                    }


                    is ApiResult.Error -> {

                        binding.tvStatus.text =
                            "Đăng nhập thất bại: " +
                                    result.exception.message

                        Log.e(
                            TAG,
                            "LOGIN ERROR | " +
                                    "HTTP=${result.exception.httpStatus} | " +
                                    "code=${result.exception.errorCode}"
                        )
                    }

                    ApiResult.Loading -> Unit
                }

            } catch (e: CancellationException) {
                throw e

            } catch (e: Exception) {

                binding.tvStatus.text =
                    "Lỗi khi đăng nhập: ${e.message}"

                Log.e(TAG, "Login unexpected error", e)

            } finally {
                setBusy(false)
            }
        }
    }

    // ============================================================
    // 5. READ STORED SESSION
    // ============================================================


    private fun loadStoredSession() {

        lifecycleScope.launch {

            try {

                val session = tokenManager.getSession()

                renderSession(session)

                if (!session.hasStoredSession) {

                    binding.tvStatus.text =
                        "Chưa có session đăng nhập."

                    return@launch
                }

                binding.tvStatus.text =
                    "Đang kiểm tra phiên đăng nhập đã lưu..."

                // Bước 2.8 đã có phương thức kiểm tra
                // ACCESS_LOCAL_NETWORK trước khi gọi API.
                withLocalNetworkPermission {

                    setBusy(true)

                    lifecycleScope.launch {

                        try {

                            verifySessionAndRoute()

                        } catch (e: CancellationException) {

                            throw e

                        } catch (e: Exception) {

                            binding.tvStatus.text =
                                "Lỗi kiểm tra session: ${e.message}"

                        } finally {

                            setBusy(false)
                        }
                    }
                }

            } catch (e: CancellationException) {

                throw e

            } catch (e: Exception) {

                binding.tvStatus.text =
                    "Không thể đọc session: ${e.message}"
            }
        }
    }


    private fun checkSession() {

        lifecycleScope.launch {

            try {

                val session = tokenManager.getSession()

                renderSession(session)

                binding.tvStatus.text =
                    if (session.hasStoredSession) {
                        "Session đã được lưu trong DataStore."
                    } else {
                        "Chưa có session trong DataStore."
                    }

                Log.d(
                    TAG,
                    "SESSION | stored=${session.hasStoredSession} | " +
                            "role=${session.role}"
                )

            } catch (e: CancellationException) {
                throw e

            } catch (e: Exception) {

                binding.tvStatus.text =
                    "Lỗi đọc session: ${e.message}"
            }
        }
    }

    // ============================================================
    // 6. GET CURRENT USER WITH JWT
    // ============================================================

    private fun getMe() {

        setBusy(true)

        binding.tvStatus.text =
            "Đang gọi GET /api/auth/me..."

        lifecycleScope.launch {

            try {

                val result = authRepository.getMe()

                when (result) {

                    is ApiResult.Success -> {

                        val user = result.data

                        if (user == null) {

                            binding.tvStatus.text =
                                "API trả thành công nhưng không có dữ liệu."

                        } else {

                            binding.tvStatus.text =
                                "GET /api/auth/me thành công! " +
                                        "HTTP ${result.statusCode}"

                            binding.tvUser.text = """
                                User ID: ${user.userId}
                                Họ tên: ${user.fullName}
                                Email: ${user.email}
                                Role: ${user.role}
                            """.trimIndent()

                            Log.d(
                                TAG,
                                "GET ME SUCCESS | " +
                                        "HTTP=${result.statusCode} | " +
                                        "role=${user.role}"
                            )
                        }
                    }

                    is ApiResult.Error -> {

                        binding.tvStatus.text =
                            "GET /auth/me thất bại: " +
                                    result.exception.message

                        Log.e(
                            TAG,
                            "GET ME ERROR | " +
                                    "HTTP=${result.exception.httpStatus} | " +
                                    "code=${result.exception.errorCode}"
                        )
                    }

                    ApiResult.Loading -> Unit
                }

                // Kiểm tra lại session sau request
                val session = tokenManager.getSession()
                hasSession = session.hasStoredSession

            } catch (e: CancellationException) {
                throw e

            } catch (e: Exception) {

                binding.tvStatus.text =
                    "Lỗi gọi API: ${e.message}"

                Log.e(TAG, "GetMe unexpected error", e)

            } finally {
                setBusy(false)
            }
        }
    }

    // ============================================================
    // 7. DISPLAY SESSION INFORMATION
    // ============================================================

    private fun renderSession(session: UserSession) {

        hasSession = session.hasStoredSession

        if (session.hasStoredSession) {

            binding.tvUser.text = """
                User ID: ${session.userId}
                Họ tên: ${session.fullName}
                Email: ${session.email}
                Role: ${session.role}
                Status: ${session.status}
                Access Token: Đã lưu
                Refresh Token: Đã lưu
            """.trimIndent()

        } else {

            binding.tvUser.text =
                "Chưa có thông tin đăng nhập."
        }

        updateButtons()
    }

    // ============================================================
    // 8. BUTTON STATE
    // ============================================================

    private fun setBusy(value: Boolean) {
        isBusy = value
        updateButtons()
    }

    private fun updateButtons() {

        binding.btnLogin.isEnabled = !isBusy

        binding.btnSession.isEnabled = !isBusy

        binding.btnMe.isEnabled =
            !isBusy && hasSession
    }
}
