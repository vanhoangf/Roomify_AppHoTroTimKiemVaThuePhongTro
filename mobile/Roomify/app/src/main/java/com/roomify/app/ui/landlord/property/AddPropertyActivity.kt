package com.roomify.app.ui.landlord.property

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast

import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope

import com.roomify.app.data.remote.api.CatalogApi
import com.roomify.app.data.remote.api.LandlordPropertyApi
import com.roomify.app.data.remote.common.ApiResult
import com.roomify.app.data.remote.dto.catalog.PropertyTypeResponse
import com.roomify.app.data.remote.dto.catalog.ProvinceResponse
import com.roomify.app.data.remote.dto.catalog.WardResponse
import com.roomify.app.data.remote.dto.landlord.CreatePropertyRequest
import com.roomify.app.data.remote.network.RetrofitClient
import com.roomify.app.data.repository.CatalogRepository
import com.roomify.app.data.repository.LandlordPropertyRepository
import com.roomify.app.databinding.ActivityAddPropertyBinding
import com.roomify.app.ui.common.RoleSessionGuard

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

class AddPropertyActivity : AppCompatActivity() {

    private lateinit var binding:
            ActivityAddPropertyBinding

    private lateinit var catalogRepository:
            CatalogRepository

    private lateinit var propertyRepository:
            LandlordPropertyRepository

    private var propertyTypes:
            List<PropertyTypeResponse> =
        emptyList()

    private var provinces:
            List<ProvinceResponse> =
        emptyList()

    private var wards:
            List<WardResponse> =
        emptyList()

    private var selectedPropertyTypeId:
            Int? = null

    private var selectedProvinceId:
            Int? = null

    private var selectedWardId:
            Int? = null

    private var isLoadingCatalog =
        false

    private var isSaving =
        false

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        binding =
            ActivityAddPropertyBinding.inflate(
                layoutInflater
            )

        setContentView(
            binding.root
        )

        setupInsets()
        setupRepositories()
        setupListeners()

        loadInitialCatalog()
    }

    // ========================================================
    // INSETS
    // ========================================================

    private fun setupInsets() {

        ViewCompat.setOnApplyWindowInsetsListener(
            binding.addPropertyRoot
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

    // ========================================================
    // REPOSITORIES
    // ========================================================

    private fun setupRepositories() {

        RetrofitClient.initialize(
            applicationContext
        )

        catalogRepository =
            CatalogRepository(
                RetrofitClient.createService(
                    CatalogApi::class.java
                )
            )

        propertyRepository =
            LandlordPropertyRepository(
                propertyApi =
                    RetrofitClient.createService(
                        LandlordPropertyApi::class.java
                    )
            )
    }

    // ========================================================
    // LISTENERS
    // ========================================================

    private fun setupListeners() {

        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnCreateProperty
            .setOnClickListener {

                validateAndCreate()
            }

        // ----------------------------------------------------
        // PROPERTY TYPE
        // ----------------------------------------------------

        binding.actPropertyType
            .setOnItemClickListener {
                    _,
                    _,
                    position,
                    _ ->

                selectedPropertyTypeId =
                    propertyTypes
                        .getOrNull(position)
                        ?.propertyTypeId

                binding.tilPropertyType.error =
                    null
            }

        // ----------------------------------------------------
        // PROVINCE
        // ----------------------------------------------------

        binding.actProvince
            .setOnItemClickListener {
                    _,
                    _,
                    position,
                    _ ->

                val province =
                    provinces
                        .getOrNull(position)

                selectedProvinceId =
                    province?.provinceId

                selectedWardId =
                    null

                wards =
                    emptyList()

                binding.actWard.setText(
                    "",
                    false
                )

                binding.actWard.setAdapter(
                    null
                )

                binding.actWard.isEnabled =
                    false

                binding.tilProvince.error =
                    null

                binding.tilWard.error =
                    null

                province?.let {
                    loadWards(
                        it.provinceId
                    )
                }
            }

        // ----------------------------------------------------
        // WARD
        // ----------------------------------------------------

        binding.actWard
            .setOnItemClickListener {
                    _,
                    _,
                    position,
                    _ ->

                selectedWardId =
                    wards
                        .getOrNull(position)
                        ?.wardId

                binding.tilWard.error =
                    null
            }
    }

    // ========================================================
    // LOAD INITIAL CATALOG
    // ========================================================

    private fun loadInitialCatalog() {

        if (isLoadingCatalog) {
            return
        }

        isLoadingCatalog =
            true

        showCatalogLoading()

        lifecycleScope.launch {

            try {

                val propertyTypeResult =
                    catalogRepository
                        .getPropertyTypes()

                if (
                    propertyTypeResult
                            !is ApiResult.Success
                ) {

                    showCatalogError(
                        if (
                            propertyTypeResult
                                    is ApiResult.Error
                        ) {
                            propertyTypeResult
                                .exception
                                .message
                                ?: "Không thể tải loại khu trọ."
                        } else {
                            "Không thể tải loại khu trọ."
                        }
                    )

                    return@launch
                }

                val provinceResult =
                    catalogRepository
                        .getProvinces()

                if (
                    provinceResult
                            !is ApiResult.Success
                ) {

                    showCatalogError(
                        if (
                            provinceResult
                                    is ApiResult.Error
                        ) {
                            provinceResult
                                .exception
                                .message
                                ?: "Không thể tải tỉnh/thành phố."
                        } else {
                            "Không thể tải tỉnh/thành phố."
                        }
                    )

                    return@launch
                }

                propertyTypes =
                    propertyTypeResult
                        .data
                        .orEmpty()

                provinces =
                    provinceResult
                        .data
                        .orEmpty()

                setupPropertyTypeDropdown()
                setupProvinceDropdown()

                showForm()

            } catch (
                e: CancellationException
            ) {

                throw e

            } catch (
                e: Exception
            ) {

                showCatalogError(
                    e.message
                        ?: "Không thể tải dữ liệu."
                )

            } finally {

                isLoadingCatalog =
                    false
            }
        }
    }

    // ========================================================
    // DROPDOWNS
    // ========================================================

    private fun setupPropertyTypeDropdown() {

        val names =
            propertyTypes.map {
                it.typeName
            }

        binding.actPropertyType
            .setAdapter(
                ArrayAdapter(
                    this,
                    android.R.layout
                        .simple_dropdown_item_1line,
                    names
                )
            )
    }

    private fun setupProvinceDropdown() {

        val names =
            provinces.map {
                it.provinceName
            }

        binding.actProvince
            .setAdapter(
                ArrayAdapter(
                    this,
                    android.R.layout
                        .simple_dropdown_item_1line,
                    names
                )
            )
    }

    private fun loadWards(
        provinceId: Int
    ) {

        binding.actWard.isEnabled =
            false

        lifecycleScope.launch {

            when (
                val result =
                    catalogRepository
                        .getWards(
                            provinceId
                        )
            ) {

                is ApiResult.Success -> {

                    /*
                     * Nếu người dùng đã chuyển sang
                     * tỉnh khác trong lúc API đang chạy,
                     * không dùng kết quả cũ.
                     */
                    if (
                        selectedProvinceId !=
                        provinceId
                    ) {
                        return@launch
                    }

                    wards =
                        result.data.orEmpty()

                    setupWardDropdown()

                    binding.actWard.isEnabled =
                        wards.isNotEmpty()

                    if (wards.isEmpty()) {

                        Toast.makeText(
                            this@AddPropertyActivity,
                            "Tỉnh/thành phố này chưa có dữ liệu phường/xã.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                is ApiResult.Error -> {

                    Toast.makeText(
                        this@AddPropertyActivity,
                        result.exception.message
                            ?: "Không thể tải phường/xã.",
                        Toast.LENGTH_LONG
                    ).show()
                }

                ApiResult.Loading ->
                    Unit
            }
        }
    }

    private fun setupWardDropdown() {

        val names =
            wards.map {
                it.wardName
            }

        binding.actWard
            .setAdapter(
                ArrayAdapter(
                    this,
                    android.R.layout
                        .simple_dropdown_item_1line,
                    names
                )
            )
    }

    // ========================================================
    // VALIDATE
    // ========================================================

    private fun validateAndCreate() {

        if (isSaving) {
            return
        }

        clearErrors()

        val propertyName =
            binding.edtPropertyName
                .text
                ?.toString()
                ?.trim()
                .orEmpty()

        val description =
            binding.edtDescription
                .text
                ?.toString()
                ?.trim()
                .orEmpty()

        val addressLine =
            binding.edtAddress
                .text
                ?.toString()
                ?.trim()
                .orEmpty()

        val propertyTypeId =
            selectedPropertyTypeId

        val provinceId =
            selectedProvinceId

        val wardId =
            selectedWardId

        // PROPERTY TYPE
        if (propertyTypeId == null) {

            binding.tilPropertyType.error =
                "Vui lòng chọn loại khu trọ."

            binding.actPropertyType
                .requestFocus()

            return
        }

        // PROPERTY NAME
        if (propertyName.isBlank()) {

            binding.tilPropertyName.error =
                "Vui lòng nhập tên khu trọ."

            binding.edtPropertyName
                .requestFocus()

            return
        }

        if (propertyName.length > 255) {

            binding.tilPropertyName.error =
                "Tên khu trọ không được vượt quá 255 ký tự."

            return
        }

        // DESCRIPTION
        if (description.length > 2000) {

            binding.tilDescription.error =
                "Mô tả không được vượt quá 2000 ký tự."

            return
        }

        // PROVINCE
        if (provinceId == null) {

            binding.tilProvince.error =
                "Vui lòng chọn tỉnh/thành phố."

            binding.actProvince
                .requestFocus()

            return
        }

        // WARD
        if (wardId == null) {

            binding.tilWard.error =
                "Vui lòng chọn phường/xã."

            binding.actWard
                .requestFocus()

            return
        }

        // ADDRESS
        if (addressLine.isBlank()) {

            binding.tilAddress.error =
                "Vui lòng nhập địa chỉ chi tiết."

            binding.edtAddress
                .requestFocus()

            return
        }

        if (addressLine.length > 500) {

            binding.tilAddress.error =
                "Địa chỉ không được vượt quá 500 ký tự."

            return
        }

        createProperty(
            propertyTypeId =
                propertyTypeId,

            propertyName =
                propertyName,

            description =
                description,

            provinceId =
                provinceId,

            wardId =
                wardId,

            addressLine =
                addressLine
        )
    }

    // ========================================================
    // CREATE PROPERTY
    // ========================================================

    private fun createProperty(
        propertyTypeId: Int,
        propertyName: String,
        description: String,
        provinceId: Int,
        wardId: Int,
        addressLine: String
    ) {

        isSaving =
            true

        setSavingState(
            true
        )

        lifecycleScope.launch {

            try {

                val request =
                    CreatePropertyRequest(
                        propertyTypeId =
                            propertyTypeId,

                        propertyName =
                            propertyName,

                        description =
                            description.takeIf {
                                it.isNotBlank()
                            },

                        provinceId =
                            provinceId,

                        wardId =
                            wardId,

                        addressLine =
                            addressLine,

                        latitude =
                            null,

                        longitude =
                            null
                    )

                when (
                    val result =
                        propertyRepository
                            .createProperty(
                                request
                            )
                ) {

                    is ApiResult.Success -> {

                        Toast.makeText(
                            this@AddPropertyActivity,
                            "Đã tạo khu trọ thành công.",
                            Toast.LENGTH_SHORT
                        ).show()

                        setResult(
                            RESULT_OK
                        )

                        finish()
                    }

                    is ApiResult.Error -> {

                        Toast.makeText(
                            this@AddPropertyActivity,
                            result.exception.message
                                ?: "Không thể tạo khu trọ.",
                            Toast.LENGTH_LONG
                        ).show()
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

                Toast.makeText(
                    this@AddPropertyActivity,
                    e.message
                        ?: "Đã xảy ra lỗi khi tạo khu trọ.",
                    Toast.LENGTH_LONG
                ).show()

            } finally {

                isSaving =
                    false

                setSavingState(
                    false
                )
            }
        }
    }

    // ========================================================
    // UI STATE
    // ========================================================

    private fun showCatalogLoading() {

        binding.progressCatalog.isVisible =
            true

        binding.scrollContent.isVisible =
            false
    }

    private fun showForm() {

        binding.progressCatalog.isVisible =
            false

        binding.scrollContent.isVisible =
            true
    }

    private fun showCatalogError(
        message: String
    ) {

        binding.progressCatalog.isVisible =
            false

        binding.scrollContent.isVisible =
            false

        Toast.makeText(
            this,
            message,
            Toast.LENGTH_LONG
        ).show()
    }

    private fun setSavingState(
        saving: Boolean
    ) {

        binding.btnCreateProperty.isEnabled =
            !saving

        binding.btnBack.isEnabled =
            !saving

        binding.actPropertyType.isEnabled =
            !saving

        binding.edtPropertyName.isEnabled =
            !saving

        binding.edtDescription.isEnabled =
            !saving

        binding.actProvince.isEnabled =
            !saving

        binding.actWard.isEnabled =
            !saving &&
                    selectedProvinceId != null &&
                    wards.isNotEmpty()

        binding.edtAddress.isEnabled =
            !saving

        binding.btnCreateProperty.text =
            if (saving) {
                "Đang tạo..."
            } else {
                "Tạo khu trọ"
            }
    }

    private fun clearErrors() {

        binding.tilPropertyType.error =
            null

        binding.tilPropertyName.error =
            null

        binding.tilDescription.error =
            null

        binding.tilProvince.error =
            null

        binding.tilWard.error =
            null

        binding.tilAddress.error =
            null
    }

    // ========================================================
    // SESSION GUARD
    // ========================================================

    override fun onStart() {
        super.onStart()

        RoleSessionGuard.verify(
            activity = this,
            expectedRole = "LANDLORD",
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