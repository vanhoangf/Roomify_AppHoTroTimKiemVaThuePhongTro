package com.roomify.app.ui.landlord.account

import android.os.Bundle
import android.widget.Toast

import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope

import com.roomify.app.data.remote.api.UserApi
import com.roomify.app.data.remote.common.ApiResult
import com.roomify.app.data.remote.dto.users.UpdateLandlordProfileRequest
import com.roomify.app.data.remote.dto.users.UpdateUserRequest
import com.roomify.app.data.remote.dto.users.UserMeResponse
import com.roomify.app.data.remote.network.RetrofitClient
import com.roomify.app.data.repository.UserRepository
import com.roomify.app.databinding.ActivityLandlordEditProfileBinding
import com.roomify.app.ui.common.RoleSessionGuard

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

class LandlordEditProfileActivity :
    AppCompatActivity() {

    private lateinit var binding:
            ActivityLandlordEditProfileBinding

    private lateinit var repository:
            UserRepository

    private var currentUser:
            UserMeResponse? =
        null

    private var isSaving =
        false

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(
            savedInstanceState
        )

        binding =
            ActivityLandlordEditProfileBinding.inflate(
                layoutInflater
            )

        setContentView(
            binding.root
        )

        setupInsets()
        setupRepository()
        setupActions()

        loadProfile()
    }

    private fun setupInsets() {

        ViewCompat.setOnApplyWindowInsetsListener(
            binding.editLandlordProfileRoot
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
    }

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

    private fun setupActions() {

        binding.btnBack
            .setOnClickListener {

                if (!isSaving) {
                    finish()
                }
            }

        binding.btnRetry
            .setOnClickListener {

                loadProfile()
            }

        binding.btnSave
            .setOnClickListener {

                saveProfile()
            }
    }

    private fun loadProfile() {

        binding.progressProfile.isVisible =
            true

        binding.scrollContent.isVisible =
            false

        binding.errorContainer.isVisible =
            false

        binding.btnSave.isEnabled =
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

                        if (
                            user == null ||
                            !user.role.equals(
                                "LANDLORD",
                                ignoreCase = true
                            )
                        ) {

                            showError(
                                "Không tìm thấy hồ sơ chủ trọ."
                            )

                            return@launch
                        }

                        currentUser =
                            user

                        binding.edtFullName.setText(
                            user.fullName
                        )

                        binding.edtPhone.setText(
                            user.phone.orEmpty()
                        )

                        binding.edtIdentityNumber.setText(
                            user.landlordProfile
                                ?.identityNumber
                                .orEmpty()
                        )

                        binding.progressProfile.isVisible =
                            false

                        binding.errorContainer.isVisible =
                            false

                        binding.scrollContent.isVisible =
                            true

                        binding.btnSave.isEnabled =
                            true
                    }

                    is ApiResult.Error -> {

                        showError(
                            result.exception.message
                                ?: "Không thể tải hồ sơ."
                        )
                    }

                    ApiResult.Loading ->
                        Unit
                }

            } catch (
                e: CancellationException
            ) {

                throw e

            } catch (
                e: Exception
            ) {

                showError(
                    e.message
                        ?: "Không thể tải hồ sơ."
                )
            }
        }
    }

    private fun saveProfile() {

        if (isSaving) {
            return
        }

        clearErrors()

        val user =
            currentUser
                ?: return

        val fullName =
            binding.edtFullName.text
                ?.toString()
                ?.trim()
                .orEmpty()

        val phone =
            binding.edtPhone.text
                ?.toString()
                ?.trim()
                ?.takeIf {
                    it.isNotBlank()
                }

        val identityNumber =
            binding.edtIdentityNumber.text
                ?.toString()
                ?.trim()
                ?.takeIf {
                    it.isNotBlank()
                }

        if (
            fullName.isBlank()
        ) {

            binding.tilFullName.error =
                "Vui lòng nhập họ tên."

            return
        }

        if (
            fullName.length > 150
        ) {

            binding.tilFullName.error =
                "Họ tên tối đa 150 ký tự."

            return
        }

        if (
            phone != null &&
            phone.length > 20
        ) {

            binding.tilPhone.error =
                "Số điện thoại tối đa 20 ký tự."

            return
        }

        if (
            identityNumber != null &&
            identityNumber.length > 30
        ) {

            binding.tilIdentityNumber.error =
                "Số CCCD/giấy tờ tối đa 30 ký tự."

            return
        }

        val oldPhone =
            user.phone
                ?.trim()
                ?.takeIf {
                    it.isNotBlank()
                }

        val oldIdentity =
            user.landlordProfile
                ?.identityNumber
                ?.trim()
                ?.takeIf {
                    it.isNotBlank()
                }

        val commonChanged =
            fullName != user.fullName.trim() ||
                    phone != oldPhone

        val landlordChanged =
            identityNumber != oldIdentity

        if (
            !commonChanged &&
            !landlordChanged
        ) {

            Toast.makeText(
                this,
                "Không có thay đổi để lưu.",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        setSaving(
            true
        )

        lifecycleScope.launch {

            try {

                if (
                    commonChanged
                ) {

                    when (
                        val result =
                            repository.updateMe(
                                UpdateUserRequest(
                                    fullName =
                                        fullName,
                                    phone =
                                        phone
                                )
                            )
                    ) {

                        is ApiResult.Success ->
                            Unit

                        is ApiResult.Error -> {

                            Toast.makeText(
                                this@LandlordEditProfileActivity,
                                result.exception.message
                                    ?: "Không thể cập nhật thông tin cá nhân.",
                                Toast.LENGTH_LONG
                            ).show()

                            return@launch
                        }

                        ApiResult.Loading ->
                            Unit
                    }
                }

                if (
                    landlordChanged
                ) {

                    when (
                        val result =
                            repository.updateLandlordProfile(
                                UpdateLandlordProfileRequest(
                                    identityNumber =
                                        identityNumber
                                )
                            )
                    ) {

                        is ApiResult.Success ->
                            Unit

                        is ApiResult.Error -> {

                            Toast.makeText(
                                this@LandlordEditProfileActivity,
                                result.exception.message
                                    ?: "Không thể cập nhật hồ sơ chủ trọ.",
                                Toast.LENGTH_LONG
                            ).show()

                            return@launch
                        }

                        ApiResult.Loading ->
                            Unit
                    }
                }

                Toast.makeText(
                    this@LandlordEditProfileActivity,
                    if (landlordChanged) {
                        "Đã cập nhật hồ sơ. Nếu CCCD thay đổi, trạng thái xác minh sẽ được đặt lại."
                    } else {
                        "Đã cập nhật thông tin."
                    },
                    Toast.LENGTH_LONG
                ).show()

                finish()

            } catch (
                e: CancellationException
            ) {

                throw e

            } catch (
                e: Exception
            ) {

                Toast.makeText(
                    this@LandlordEditProfileActivity,
                    e.message
                        ?: "Không thể cập nhật hồ sơ.",
                    Toast.LENGTH_LONG
                ).show()

            } finally {

                setSaving(
                    false
                )
            }
        }
    }

    private fun clearErrors() {

        binding.tilFullName.error =
            null

        binding.tilPhone.error =
            null

        binding.tilIdentityNumber.error =
            null
    }

    private fun setSaving(
        saving: Boolean
    ) {

        isSaving =
            saving

        binding.btnSave.isEnabled =
            !saving

        binding.btnBack.isEnabled =
            !saving

        binding.edtFullName.isEnabled =
            !saving

        binding.edtPhone.isEnabled =
            !saving

        binding.edtIdentityNumber.isEnabled =
            !saving

        binding.btnSave.text =
            if (saving) {

                "Đang lưu..."

            } else {

                "Lưu thay đổi"
            }
    }

    private fun showError(
        message: String
    ) {

        binding.progressProfile.isVisible =
            false

        binding.scrollContent.isVisible =
            false

        binding.errorContainer.isVisible =
            true

        binding.btnSave.isEnabled =
            false

        binding.tvErrorMessage.text =
            message
    }

    override fun onStart() {
        super.onStart()

        RoleSessionGuard.verify(
            activity =
                this,
            expectedRole =
                "LANDLORD",
            onNetworkError = { message ->

                Toast.makeText(
                    this,
                    message,
                    Toast.LENGTH_LONG
                ).show()
            }
        )
    }
}