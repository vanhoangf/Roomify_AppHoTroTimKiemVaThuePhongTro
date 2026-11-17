package com.roomify.app.ui.auth

import android.content.Intent
import androidx.core.widget.doAfterTextChanged
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Patterns
import android.view.View

import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope

import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.roomify.app.BuildConfig
import com.roomify.app.R
import com.roomify.app.data.local.TokenManager
import com.roomify.app.data.local.TokenManagerSessionStore
import com.roomify.app.data.remote.api.AuthApi
import com.roomify.app.data.remote.common.ApiResult
import com.roomify.app.data.remote.dto.auth.RegisterStudentRequest
import com.roomify.app.data.remote.dto.auth.RegisterLandlordRequest
import com.roomify.app.data.remote.network.RetrofitClient
import com.roomify.app.data.repository.AuthRepository
import com.roomify.app.databinding.ActivityRegisterBinding

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var repository: AuthRepository

    private var isSubmitting = false
    private var registered = false

    private enum class Role {
        STUDENT,
        LANDLORD
    }

    private data class RegisterForm(
        val role: Role,
        val fullName: String,
        val email: String,
        val password: String,
        val phone: String?
    )

    private var pendingForm: RegisterForm? = null

    // Dành cho backend HTTP chạy trên máy tính
    // khi test Android 17.
    private val networkPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->

            val form = pendingForm
            pendingForm = null

            if (granted && form != null) {
                submitRegistration(form)
            } else if (!granted) {
                showStatus(
                    "Bạn cần cấp quyền mạng cục bộ để kết nối Roomify.API."
                )
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.edtFullName.doAfterTextChanged {
            binding.tilFullName.error = null
        }

        binding.edtEmail.doAfterTextChanged {
            binding.tilEmail.error = null
        }

        binding.edtPhone.doAfterTextChanged {
            binding.tilPhone.error = null
        }

        binding.edtPassword.doAfterTextChanged {
            binding.tilPassword.error = null
        }

        binding.edtConfirmPassword.doAfterTextChanged {
            binding.tilConfirmPassword.error = null
        }


        applyWindowInsets()

        RetrofitClient.initialize(applicationContext)

        repository = AuthRepository(
            authApi = RetrofitClient.createService(
                AuthApi::class.java
            ),
            sessionStore = TokenManagerSessionStore(
                TokenManager(applicationContext)
            )
        )

        binding.rgRole.setOnCheckedChangeListener { _, checkedId ->

            binding.tvRoleDescription.text =
                if (checkedId == R.id.rbLandlord) {
                    "Đăng tin, quản lý phòng trọ và xử lý yêu cầu thuê."
                } else {
                    "Tìm kiếm, lưu và thuê phòng phù hợp với bạn."
                }
        }

        binding.btnRegister.setOnClickListener {

            if (isSubmitting || registered) {
                return@setOnClickListener
            }

            val form = validateForm()
                ?: return@setOnClickListener

            checkNetworkPermissionThenRegister(form)
        }

        binding.btnBackToLogin.setOnClickListener {
            finish()
        }
    }

    private fun applyWindowInsets() {

        val root = binding.root

        val left = root.paddingLeft
        val top = root.paddingTop
        val right = root.paddingRight
        val bottom = root.paddingBottom

        ViewCompat.setOnApplyWindowInsetsListener(
            root
        ) { view, insets ->

            val bars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars()
            )

            view.setPadding(
                left + bars.left,
                top + bars.top,
                right + bars.right,
                bottom + bars.bottom
            )

            insets
        }
    }

    // ========================================================
    // VALIDATE
    // ========================================================

    private fun validateForm(): RegisterForm? {

        clearErrors()

        val fullName = binding.edtFullName.text
            ?.toString()?.trim().orEmpty()

        val email = binding.edtEmail.text
            ?.toString()?.trim().orEmpty()

        val phone = binding.edtPhone.text
            ?.toString()?.trim().orEmpty()

        val password = binding.edtPassword.text
            ?.toString().orEmpty()

        val confirm = binding.edtConfirmPassword.text
            ?.toString().orEmpty()

        if (fullName.isBlank()) {
            binding.tilFullName.error = "Vui lòng nhập họ tên."
            binding.edtFullName.requestFocus()
            return null
        }

        if (fullName.length > 150) {
            binding.tilFullName.error =
                "Họ tên không được vượt quá 150 ký tự."
            return null
        }

        if (
            email.isBlank() ||
            !Patterns.EMAIL_ADDRESS.matcher(email).matches()
        ) {
            binding.tilEmail.error = "Email không đúng định dạng."
            binding.edtEmail.requestFocus()
            return null
        }

        if (password.length !in 8..100) {
            binding.tilPassword.error =
                "Mật khẩu phải có từ 8 đến 100 ký tự."
            binding.edtPassword.requestFocus()
            return null
        }

        if (confirm != password) {
            binding.tilConfirmPassword.error =
                "Mật khẩu nhập lại không khớp."
            binding.edtConfirmPassword.requestFocus()
            return null
        }

        if (phone.length > 20) {
            binding.tilPhone.error =
                "Số điện thoại không được vượt quá 20 ký tự."
            return null
        }

        val role = if (binding.rbLandlord.isChecked) {
            Role.LANDLORD
        } else {
            Role.STUDENT
        }

        return RegisterForm(
            role = role,
            fullName = fullName,
            email = email,
            password = password,
            phone = phone.ifBlank { null }
        )
    }

    private fun clearErrors() {
        binding.tilFullName.error = null
        binding.tilEmail.error = null
        binding.tilPhone.error = null
        binding.tilPassword.error = null
        binding.tilConfirmPassword.error = null

        binding.tvStatus.visibility = View.GONE
        binding.tvStatus.text = ""
    }

    // ========================================================
    // LOCAL NETWORK PERMISSION
    // ========================================================

    private fun checkNetworkPermissionThenRegister(
        form: RegisterForm
    ) {

        val requiresPermission =
            BuildConfig.DEBUG &&
                    Build.VERSION.SDK_INT >= 37 &&
                    ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_LOCAL_NETWORK
                    ) != PackageManager.PERMISSION_GRANTED

        if (requiresPermission) {

            pendingForm = form

            networkPermissionLauncher.launch(
                Manifest.permission.ACCESS_LOCAL_NETWORK
            )

        } else {
            submitRegistration(form)
        }
    }

    // ========================================================
    // CALL BACKEND
    // ========================================================

    private fun submitRegistration(form: RegisterForm) {

        setBusy(true)
        showStatus("Đang tạo tài khoản...")

        lifecycleScope.launch {

            try {

                val result = when (form.role) {

                    Role.STUDENT -> repository.registerStudent(
                        RegisterStudentRequest(
                            fullName = form.fullName,
                            email = form.email,
                            password = form.password,
                            phone = form.phone,
                            universityId = null,
                            studentCode = null
                        )
                    )

                    Role.LANDLORD -> repository.registerLandlord(
                        RegisterLandlordRequest(
                            fullName = form.fullName,
                            email = form.email,
                            password = form.password,
                            phone = form.phone,
                            identityNumber = null
                        )
                    )
                }

                when (result) {

                    is ApiResult.Success -> {

                        registered = true

                        // Không tự lưu token: register chưa phải login.
                        val roleText =
                            if (form.role == Role.STUDENT) {
                                "Sinh viên"
                            } else {
                                "Chủ trọ"
                            }

                        MaterialAlertDialogBuilder(
                            this@RegisterActivity
                        )
                            .setTitle("Đăng ký thành công")
                            .setMessage(
                                "Đã tạo tài khoản $roleText.\n" +
                                        "Email: ${form.email}\n\n" +
                                        "Bạn có thể đăng nhập để sử dụng Roomify."
                            )
                            .setCancelable(false)

                            .setPositiveButton("Về đăng nhập") { _, _ ->

                                val resultIntent = Intent().apply {
                                    putExtra("registered_email", form.email)
                                }

                                setResult(RESULT_OK, resultIntent)

                                finish()
                            }
                            .show()
                    }

                    is ApiResult.Error -> {

                        val error = result.exception

                        binding.tilFullName.error =
                            error.firstFieldError("FullName")

                        binding.tilEmail.error =
                            error.firstFieldError("Email")

                        binding.tilPassword.error =
                            error.firstFieldError("Password")

                        binding.tilPhone.error =
                            error.firstFieldError("Phone")

                        when (error.errorCode) {
                            "EMAIL_ALREADY_EXISTS" -> {
                                binding.tilEmail.error =
                                    "Email đã được sử dụng."
                            }

                            "PHONE_ALREADY_EXISTS" -> {
                                binding.tilPhone.error =
                                    "Số điện thoại đã được sử dụng."
                            }
                        }

                        showStatus(
                            "Đăng ký thất bại: ${error.message}"
                        )
                    }

                    ApiResult.Loading -> Unit
                }

            } catch (e: CancellationException) {
                throw e

            } catch (_: Exception) {
                showStatus(
                    "Có lỗi khi đăng ký. Vui lòng thử lại."
                )

            } finally {
                setBusy(false)
            }
        }
    }

    private fun setBusy(busy: Boolean) {
        isSubmitting = busy

        binding.btnRegister.isEnabled =
            !busy && !registered

        binding.btnBackToLogin.isEnabled = !busy

        binding.btnRegister.text =
            if (busy) "Đang đăng ký..." else "Tạo tài khoản"
    }

    private fun showStatus(message: String) {
        binding.tvStatus.text = message
        binding.tvStatus.visibility = View.VISIBLE
    }
}
