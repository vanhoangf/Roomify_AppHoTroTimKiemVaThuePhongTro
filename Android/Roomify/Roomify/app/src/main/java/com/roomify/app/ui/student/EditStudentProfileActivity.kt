package com.roomify.app.ui.student

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast

import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope

import com.roomify.app.data.remote.api.CatalogApi
import com.roomify.app.data.remote.api.UserApi
import com.roomify.app.data.remote.common.ApiResult
import com.roomify.app.data.remote.dto.catalog.UniversityResponse
import com.roomify.app.data.remote.dto.users.UpdateStudentProfileRequest
import com.roomify.app.data.remote.dto.users.UpdateUserRequest
import com.roomify.app.data.remote.network.RetrofitClient
import com.roomify.app.data.repository.CatalogRepository
import com.roomify.app.data.repository.UserRepository
import com.roomify.app.databinding.ActivityEditStudentProfileBinding

import kotlinx.coroutines.launch

class EditStudentProfileActivity :
    AppCompatActivity() {

    private lateinit var binding:
            ActivityEditStudentProfileBinding

    private lateinit var userRepository:
            UserRepository

    private lateinit var catalogRepository:
            CatalogRepository

    private var universities:
            List<UniversityResponse> =
        emptyList()

    private var selectedUniversityId:
            Long? = null

    private var isSaving =
        false

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        binding =
            ActivityEditStudentProfileBinding.inflate(
                layoutInflater
            )

        setContentView(
            binding.root
        )

        setupInsets()
        setupRepositories()
        setupListeners()

        loadData()
    }

    // =========================================================
    // INSETS
    // =========================================================

    private fun setupInsets() {

        ViewCompat.setOnApplyWindowInsetsListener(
            binding.editProfileRoot
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

    // =========================================================
    // REPOSITORIES
    // =========================================================

    private fun setupRepositories() {

        RetrofitClient.initialize(
            applicationContext
        )

        userRepository =
            UserRepository(
                RetrofitClient.createService(
                    UserApi::class.java
                )
            )

        catalogRepository =
            CatalogRepository(
                RetrofitClient.createService(
                    CatalogApi::class.java
                )
            )
    }

    // =========================================================
    // LISTENERS
    // =========================================================

    private fun setupListeners() {

        binding.btnBack
            .setOnClickListener {
                finish()
            }

        binding.btnSave
            .setOnClickListener {
                saveProfile()
            }

        binding.actUniversity
            .setOnItemClickListener {
                    _,
                    _,
                    position,
                    _ ->

                selectedUniversityId =
                    universities
                        .getOrNull(position)
                        ?.universityId

                binding.tilUniversity.error =
                    null
            }
    }

    // =========================================================
    // LOAD DATA
    // =========================================================

    private fun loadData() {

        binding.progressProfile.isVisible =
            true

        binding.scrollContent.isVisible =
            false

        binding.btnSave.isEnabled =
            false

        lifecycleScope.launch {

            val userResult =
                userRepository.getMe()

            val universityResult =
                catalogRepository.getUniversities()

            if (
                userResult !is ApiResult.Success
            ) {

                showLoadError(
                    if (
                        userResult is ApiResult.Error
                    ) {
                        userResult.exception.message
                    } else {
                        "Không thể tải hồ sơ."
                    }
                )

                return@launch
            }

            if (
                universityResult !is ApiResult.Success
            ) {

                showLoadError(
                    if (
                        universityResult is ApiResult.Error
                    ) {
                        universityResult.exception.message
                    } else {
                        "Không thể tải danh sách trường đại học."
                    }
                )

                return@launch
            }

            val user =
                userResult.data

            if (user == null) {

                showLoadError(
                    "Không nhận được thông tin hồ sơ."
                )

                return@launch
            }

            universities =
                universityResult.data.orEmpty()

            setupUniversityDropdown()

            binding.edtFullName.setText(
                user.fullName
            )

            binding.edtPhone.setText(
                user.phone.orEmpty()
            )

            val student =
                user.studentProfile

            binding.edtStudentCode.setText(
                student
                    ?.studentCode
                    .orEmpty()
            )

            selectedUniversityId =
                student?.universityId

            binding.actUniversity.setText(
                student
                    ?.universityName
                    .orEmpty(),
                false
            )

            binding.progressProfile.isVisible =
                false

            binding.scrollContent.isVisible =
                true

            binding.btnSave.isEnabled =
                true
        }
    }

    private fun setupUniversityDropdown() {

        val names =
            universities.map {
                it.universityName
            }

        binding.actUniversity.setAdapter(
            ArrayAdapter(
                this,
                android.R.layout
                    .simple_dropdown_item_1line,
                names
            )
        )
    }

    private fun showLoadError(
        message: String
    ) {

        binding.progressProfile.isVisible =
            false

        binding.btnSave.isEnabled =
            false

        Toast.makeText(
            this,
            message,
            Toast.LENGTH_LONG
        ).show()
    }

    // =========================================================
    // SAVE PROFILE
    // =========================================================

    private fun saveProfile() {

        if (isSaving) {
            return
        }

        clearErrors()

        val fullName =
            binding.edtFullName.text
                ?.toString()
                ?.trim()
                .orEmpty()

        val phone =
            binding.edtPhone.text
                ?.toString()
                ?.trim()
                .orEmpty()

        val studentCode =
            binding.edtStudentCode.text
                ?.toString()
                ?.trim()
                .orEmpty()

        if (fullName.isBlank()) {

            binding.tilFullName.error =
                "Vui lòng nhập họ và tên."

            return
        }

        if (fullName.length > 150) {

            binding.tilFullName.error =
                "Họ tên không được vượt quá 150 ký tự."

            return
        }

        if (phone.length > 20) {

            binding.tilPhone.error =
                "Số điện thoại không được vượt quá 20 ký tự."

            return
        }

        if (studentCode.length > 50) {

            binding.tilStudentCode.error =
                "Mã sinh viên không được vượt quá 50 ký tự."

            return
        }

        /*
         * Nếu người dùng nhập chữ vào trường nhưng không
         * thực sự chọn một University từ dropdown.
         */
        if (
            binding.actUniversity.text
                ?.toString()
                ?.isNotBlank() == true &&
            selectedUniversityId == null
        ) {

            binding.tilUniversity.error =
                "Vui lòng chọn trường từ danh sách."

            return
        }

        updateProfile(
            fullName = fullName,
            phone = phone,
            studentCode = studentCode
        )
    }

    private fun updateProfile(
        fullName: String,
        phone: String,
        studentCode: String
    ) {

        isSaving =
            true

        binding.btnSave.isEnabled =
            false

        binding.btnSave.text =
            "Đang lưu..."

        lifecycleScope.launch {

            try {

                // =============================================
                // UPDATE COMMON USER
                // =============================================

                val userResult =
                    userRepository.updateMe(
                        UpdateUserRequest(
                            fullName = fullName,
                            phone =
                                phone.takeIf {
                                    it.isNotBlank()
                                }
                        )
                    )

                if (
                    userResult is ApiResult.Error
                ) {

                    Toast.makeText(
                        this@EditStudentProfileActivity,
                        userResult.exception.message,
                        Toast.LENGTH_LONG
                    ).show()

                    return@launch
                }

                // =============================================
                // UPDATE STUDENT PROFILE
                // =============================================

                val studentResult =
                    userRepository
                        .updateStudentProfile(
                            UpdateStudentProfileRequest(
                                universityId =
                                    selectedUniversityId,

                                studentCode =
                                    studentCode.takeIf {
                                        it.isNotBlank()
                                    }
                            )
                        )

                when (studentResult) {

                    is ApiResult.Success -> {

                        Toast.makeText(
                            this@EditStudentProfileActivity,
                            "Đã cập nhật hồ sơ.",
                            Toast.LENGTH_SHORT
                        ).show()

                        finish()
                    }

                    is ApiResult.Error -> {

                        Toast.makeText(
                            this@EditStudentProfileActivity,
                            studentResult.exception.message,
                            Toast.LENGTH_LONG
                        ).show()
                    }

                    ApiResult.Loading ->
                        Unit
                }

            } finally {

                isSaving =
                    false

                binding.btnSave.isEnabled =
                    true

                binding.btnSave.text =
                    "Lưu thay đổi"
            }
        }
    }

    private fun clearErrors() {

        binding.tilFullName.error =
            null

        binding.tilPhone.error =
            null

        binding.tilUniversity.error =
            null

        binding.tilStudentCode.error =
            null
    }
}