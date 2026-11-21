package com.roomify.app.ui.landlord.account

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast

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
import com.roomify.app.databinding.ActivityLandlordAccountBinding
import com.roomify.app.ui.common.LogoutHandler
import com.roomify.app.ui.common.RoleSessionGuard
import com.roomify.app.ui.landlord.LandlordHomeActivity
import com.roomify.app.ui.landlord.contract.LandlordContractsActivity
import com.roomify.app.ui.landlord.message.LandlordConversationsActivity
import com.roomify.app.ui.landlord.payment.LandlordPaymentsActivity
import com.roomify.app.ui.landlord.property.LandlordPropertiesActivity
import com.roomify.app.ui.landlord.rental.LandlordRentalRequestsActivity

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

class LandlordAccountActivity :
    AppCompatActivity() {

    private lateinit var binding:
            ActivityLandlordAccountBinding

    private lateinit var repository:
            UserRepository

    private var isLoading =
        false

    private var identityDocumentUrl:
            String? =
        null

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(
            savedInstanceState
        )

        binding =
            ActivityLandlordAccountBinding.inflate(
                layoutInflater
            )

        setContentView(
            binding.root
        )

        setupInsets()
        setupRepository()
        setupActions()
        setupBottomNavigation()
    }

    private fun setupInsets() {

        ViewCompat.setOnApplyWindowInsetsListener(
            binding.landlordAccountRoot
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

        binding.btnRetry
            .setOnClickListener {

                loadProfile()
            }

        binding.btnEditProfile
            .setOnClickListener {

                startActivity(
                    Intent(
                        this,
                        LandlordEditProfileActivity::class.java
                    )
                )
            }

        binding.btnContracts
            .setOnClickListener {

                startActivity(
                    Intent(
                        this,
                        LandlordContractsActivity::class.java
                    )
                )
            }

        binding.btnPayments
            .setOnClickListener {

                startActivity(
                    Intent(
                        this,
                        LandlordPaymentsActivity::class.java
                    )
                )
            }

        binding.btnMessages
            .setOnClickListener {

                startActivity(
                    Intent(
                        this,
                        LandlordConversationsActivity::class.java
                    )
                )
            }

        binding.btnIdentityDocument
            .setOnClickListener {

                openIdentityDocument()
            }

        binding.btnLogout
            .setOnClickListener {

                LogoutHandler.logout(
                    activity =
                        this,
                    logoutButton =
                        binding.btnLogout
                )
            }
    }

    private fun setupBottomNavigation() {

        binding.bottomNavigationLandlord
            .selectedItemId =
            R.id.navLandlordAccount

        binding.bottomNavigationLandlord
            .setOnItemSelectedListener {
                    item ->

                when (
                    item.itemId
                ) {

                    R.id.navLandlordAccount -> {
                        true
                    }

                    R.id.navLandlordHome -> {

                        startActivity(
                            Intent(
                                this,
                                LandlordHomeActivity::class.java
                            )
                        )

                        finish()
                        true
                    }

                    R.id.navLandlordProperties -> {

                        startActivity(
                            Intent(
                                this,
                                LandlordPropertiesActivity::class.java
                            )
                        )

                        true
                    }

                    R.id.navLandlordRequests -> {

                        startActivity(
                            Intent(
                                this,
                                LandlordRentalRequestsActivity::class.java
                            )
                        )

                        true
                    }

                    R.id.navLandlordMessages -> {

                        startActivity(
                            Intent(
                                this,
                                LandlordConversationsActivity::class.java
                            )
                        )

                        true
                    }

                    else -> {
                        false
                    }
                }
            }
    }

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
                                "LANDLORD",
                                ignoreCase = true
                            )
                        ) {

                            showError(
                                "Tài khoản hiện tại không phải chủ trọ."
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
                                ?: "Không thể tải tài khoản."
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
                        ?: "Không thể tải tài khoản."
                )

            } finally {

                isLoading =
                    false
            }
        }
    }

    private fun showProfile(
        user: UserMeResponse
    ) {

        val landlord =
            user.landlordProfile

        binding.progressAccount.isVisible =
            false

        binding.errorContainer.isVisible =
            false

        binding.scrollContent.isVisible =
            true

        binding.tvFullName.text =
            user.fullName
                .takeIf {
                    it.isNotBlank()
                }
                ?: "Chủ trọ Roomify"

        binding.tvEmail.text =
            user.email

        binding.tvPhone.text =
            user.phone
                ?.takeIf {
                    it.isNotBlank()
                }
                ?: "Chưa cập nhật"

        binding.tvIdentityNumber.text =
            landlord
                ?.identityNumber
                ?.takeIf {
                    it.isNotBlank()
                }
                ?: "Chưa cập nhật"

        binding.tvAccountStatus.text =
            when (
                user.status.uppercase()
            ) {

                "ACTIVE" ->
                    "Đang hoạt động"

                "LOCKED" ->
                    "Đã khóa"

                else ->
                    user.status
            }

        binding.tvVerifiedAt.text =
            landlord
                ?.verifiedAt
                ?.take(10)
                ?.let {
                    formatDate(
                        it
                    )
                }
                ?: "Chưa xác minh"

        showVerification(
            landlord?.isVerified == true
        )

        identityDocumentUrl =
            landlord
                ?.identityDocumentUrl
                ?.trim()
                ?.takeIf {
                    it.isNotBlank()
                }

        binding.btnIdentityDocument.isVisible =
            identityDocumentUrl != null

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

    private fun showVerification(
        verified: Boolean
    ) {

        binding.chipVerification.text =
            if (verified) {

                "✓ Chủ trọ đã xác minh"

            } else {

                "Chưa xác minh chủ trọ"
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

    private fun openIdentityDocument() {

        val url =
            identityDocumentUrl
                ?: return

        try {

            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(
                        url
                    )
                )
            )

        } catch (
            _: Exception
        ) {

            Toast.makeText(
                this,
                "Không thể mở giấy tờ xác minh.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun formatDate(
        value: String
    ): String {

        val parts =
            value.split(
                "-"
            )

        return if (
            parts.size == 3
        ) {

            "${parts[2]}/${parts[1]}/${parts[0]}"

        } else {

            value
        }
    }

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

    override fun onResume() {
        super.onResume()

        if (
            ::repository.isInitialized
        ) {

            loadProfile()
        }
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