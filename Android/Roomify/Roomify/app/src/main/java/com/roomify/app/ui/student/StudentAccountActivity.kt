package com.roomify.app.ui.student

import android.content.Intent
import android.os.Bundle

import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope

import com.bumptech.glide.Glide

import com.roomify.app.R
import com.roomify.app.data.remote.api.UserApi
import com.roomify.app.data.remote.common.ApiResult
import com.roomify.app.data.remote.dto.users.UserMeResponse
import com.roomify.app.data.remote.network.RetrofitClient
import com.roomify.app.data.repository.UserRepository
import com.roomify.app.databinding.ActivityStudentAccountBinding
import com.roomify.app.ui.common.LogoutHandler

import kotlinx.coroutines.launch

class StudentAccountActivity :
    AppCompatActivity() {

    private lateinit var binding:
            ActivityStudentAccountBinding

    private lateinit var repository:
            UserRepository

    private var isLoading =
        false

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        binding =
            ActivityStudentAccountBinding.inflate(
                layoutInflater
            )

        setContentView(
            binding.root
        )

        setupInsets()
        setupRepository()
        setupBottomNavigation()
        setupListeners()

        loadProfile()
    }

    override fun onResume() {
        super.onResume()

        /*
         * Khi quay lại từ Edit Profile,
         * tự tải dữ liệu mới.
         */
        if (::repository.isInitialized) {
            loadProfile()
        }
    }

    // =========================================================
    // INSETS
    // =========================================================

    private fun setupInsets() {

        ViewCompat.setOnApplyWindowInsetsListener(
            binding.accountRoot
        ) { view, insets ->

            val bars =
                insets.getInsets(
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

        ViewCompat.setOnApplyWindowInsetsListener(
            binding.bottomNavigation
        ) { view, insets ->

            view.setPadding(
                view.paddingLeft,
                view.paddingTop,
                view.paddingRight,
                0
            )

            insets
        }
    }

    // =========================================================
    // REPOSITORY
    // =========================================================

    private fun setupRepository() {

        RetrofitClient.initialize(
            applicationContext
        )

        repository =
            UserRepository(
                RetrofitClient.createService(
                    UserApi::class.java
                )
            )
    }

    // =========================================================
    // NAV
    // =========================================================

    private fun setupBottomNavigation() {

        StudentBottomNavHelper.setup(
            activity = this,
            bottomNavigation =
                binding.bottomNavigation,
            selectedItemId =
                R.id.navAccount
        )
    }

    // =========================================================
    // LISTENERS
    // =========================================================

    private fun setupListeners() {

        binding.btnRetry
            .setOnClickListener {

                loadProfile()
            }

        binding.btnEditProfile
            .setOnClickListener {

                startActivity(
                    Intent(
                        this,
                        EditStudentProfileActivity::class.java
                    )
                )
            }

        binding.btnAppointments
            .setOnClickListener {

                startActivity(
                    Intent(
                        this,
                        MyAppointmentsActivity::class.java
                    )
                )
            }

        binding.btnRentalRequests
            .setOnClickListener {

                startActivity(
                    Intent(
                        this,
                        MyRentalRequestsActivity::class.java
                    )
                )
            }

        binding.btnContracts
            .setOnClickListener {

                startActivity(
                    Intent(
                        this,
                        MyContractsActivity::class.java
                    )
                )
            }

        binding.btnPayments
            .setOnClickListener {

                startActivity(
                    Intent(
                        this,
                        MyPaymentsActivity::class.java
                    )
                )
            }

        binding.btnMessages
            .setOnClickListener {

                startActivity(
                    Intent(
                        this,
                        ConversationsActivity::class.java
                    )
                )
            }

        binding.btnNotifications
            .setOnClickListener {

                startActivity(
                    Intent(
                        this,
                        NotificationsActivity::class.java
                    )
                )
            }

        binding.btnLogout
            .setOnClickListener {

                LogoutHandler.logout(
                    activity = this,
                    logoutButton =
                        binding.btnLogout
                )
            }
    }

    // =========================================================
    // LOAD PROFILE
    // =========================================================

    private fun loadProfile() {

        if (isLoading) {
            return
        }

        isLoading =
            true

        binding.progressAccount.isVisible =
            true

        binding.scrollContent.isVisible =
            false

        binding.errorContainer.isVisible =
            false

        lifecycleScope.launch {

            try {

                when (
                    val result =
                        repository.getMe()
                ) {

                    is ApiResult.Success -> {

                        val user =
                            result.data

                        if (user == null) {

                            showError(
                                "Không nhận được thông tin tài khoản."
                            )

                            return@launch
                        }

                        if (
                            !user.role.equals(
                                "STUDENT",
                                ignoreCase = true
                            )
                        ) {

                            showError(
                                "Tài khoản hiện tại không phải sinh viên."
                            )

                            return@launch
                        }

                        showProfile(
                            user
                        )
                    }

                    is ApiResult.Error -> {

                        showError(
                            result.exception.message
                        )
                    }

                    ApiResult.Loading ->
                        Unit
                }

            } finally {

                isLoading =
                    false
            }
        }
    }

    // =========================================================
    // SHOW PROFILE
    // =========================================================

    private fun showProfile(
        user: UserMeResponse
    ) {

        binding.progressAccount.isVisible =
            false

        binding.errorContainer.isVisible =
            false

        binding.scrollContent.isVisible =
            true

        val student =
            user.studentProfile

        val fullName =
            user.fullName
                .takeIf {
                    it.isNotBlank()
                }
                ?: "Sinh viên Roomify"

        val phone =
            user.phone
                ?.takeIf {
                    it.isNotBlank()
                }
                ?: "Chưa cập nhật"

        val university =
            student
                ?.universityName
                ?.takeIf {
                    it.isNotBlank()
                }
                ?: "Chưa cập nhật"

        val studentCode =
            student
                ?.studentCode
                ?.takeIf {
                    it.isNotBlank()
                }
                ?: "Chưa cập nhật"

        binding.tvFullName.text =
            fullName

        binding.tvEmail.text =
            user.email

        binding.tvProfileUniversity.text =
            university

        binding.tvPhone.text =
            phone

        binding.tvUniversity.text =
            university

        binding.tvStudentCode.text =
            studentCode

        showVerification(
            student?.isVerified == true
        )

        Glide.with(
            binding.ivAvatar
        )
            .load(
                user.avatarUrl
            )
            .placeholder(
                R.drawable.roomify_logo
            )
            .error(
                R.drawable.roomify_logo
            )
            .circleCrop()
            .into(
                binding.ivAvatar
            )
    }

    // =========================================================
    // VERIFICATION
    // =========================================================

    private fun showVerification(
        verified: Boolean
    ) {

        binding.chipVerification.text =
            if (verified) {

                "✓ Sinh viên đã xác minh"

            } else {

                "Chưa xác minh sinh viên"
            }

        binding.chipVerification.setTextColor(
            ContextCompat.getColor(
                this,
                if (verified) {
                    R.color.status_success
                } else {
                    R.color.text_muted
                }
            )
        )
    }

    // =========================================================
    // ERROR
    // =========================================================

    private fun showError(
        message: String
    ) {

        binding.progressAccount.isVisible =
            false

        binding.scrollContent.isVisible =
            false

        binding.errorContainer.isVisible =
            true

        binding.tvError.text =
            message
    }
}