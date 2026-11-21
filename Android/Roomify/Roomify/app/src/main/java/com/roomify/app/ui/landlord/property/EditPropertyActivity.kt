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
import com.roomify.app.data.remote.dto.landlord.UpdatePropertyRequest
import com.roomify.app.data.remote.network.RetrofitClient
import com.roomify.app.data.repository.CatalogRepository
import com.roomify.app.data.repository.LandlordPropertyRepository
import com.roomify.app.databinding.ActivityEditPropertyBinding
import com.roomify.app.ui.common.RoleSessionGuard

import kotlinx.coroutines.launch

class EditPropertyActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_PROPERTY_ID =
            "extra_property_id"
    }

    private lateinit var binding:
            ActivityEditPropertyBinding

    private lateinit var catalogRepository:
            CatalogRepository

    private lateinit var propertyRepository:
            LandlordPropertyRepository

    private var propertyId = 0L

    private var propertyTypes:
            List<PropertyTypeResponse> = emptyList()

    private var provinces:
            List<ProvinceResponse> = emptyList()

    private var wards:
            List<WardResponse> = emptyList()

    private var selectedPropertyTypeId:
            Int? = null

    private var selectedProvinceId:
            Int? = null

    private var selectedWardId:
            Int? = null

    private var isSaving = false

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        binding =
            ActivityEditPropertyBinding.inflate(
                layoutInflater
            )

        setContentView(binding.root)

        propertyId =
            intent.getLongExtra(
                EXTRA_PROPERTY_ID,
                0L
            )

        if (propertyId <= 0L) {

            Toast.makeText(
                this,
                "Không tìm thấy khu trọ.",
                Toast.LENGTH_SHORT
            ).show()

            finish()
            return
        }

        setupInsets()
        setupRepositories()
        setupListeners()

        loadData()
    }

    private fun setupInsets() {

        ViewCompat.setOnApplyWindowInsetsListener(
            binding.editPropertyRoot
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
                RetrofitClient.createService(
                    LandlordPropertyApi::class.java
                )
            )
    }

    private fun setupListeners() {

        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnSave.setOnClickListener {
            saveProperty()
        }

        binding.actPropertyType
            .setOnItemClickListener {
                    _, _, position, _ ->

                selectedPropertyTypeId =
                    propertyTypes
                        .getOrNull(position)
                        ?.propertyTypeId

                binding.tilPropertyType.error =
                    null
            }

        binding.actProvince
            .setOnItemClickListener {
                    _, _, position, _ ->

                val province =
                    provinces.getOrNull(position)

                selectedProvinceId =
                    province?.provinceId

                selectedWardId =
                    null

                binding.actWard.setText(
                    "",
                    false
                )

                province?.let {
                    loadWards(
                        it.provinceId
                    )
                }

                binding.tilProvince.error =
                    null
            }

        binding.actWard
            .setOnItemClickListener {
                    _, _, position, _ ->

                selectedWardId =
                    wards
                        .getOrNull(position)
                        ?.wardId

                binding.tilWard.error =
                    null
            }
    }

    private fun loadData() {

        binding.progressEdit.isVisible =
            true

        binding.scrollContent.isVisible =
            false

        lifecycleScope.launch {

            val typeResult =
                catalogRepository
                    .getPropertyTypes()

            val provinceResult =
                catalogRepository
                    .getProvinces()

            val propertyResult =
                propertyRepository
                    .getProperty(
                        propertyId
                    )

            if (
                typeResult !is ApiResult.Success ||
                provinceResult !is ApiResult.Success ||
                propertyResult !is ApiResult.Success
            ) {

                Toast.makeText(
                    this@EditPropertyActivity,
                    "Không thể tải dữ liệu chỉnh sửa.",
                    Toast.LENGTH_LONG
                ).show()

                binding.progressEdit.isVisible =
                    false

                return@launch
            }

            propertyTypes =
                typeResult.data.orEmpty()

            provinces =
                provinceResult.data.orEmpty()

            val property =
                propertyResult.data

            if (property == null) {

                Toast.makeText(
                    this@EditPropertyActivity,
                    "Không tìm thấy khu trọ.",
                    Toast.LENGTH_LONG
                ).show()

                finish()
                return@launch
            }

            setupPropertyTypeDropdown()
            setupProvinceDropdown()

            selectedPropertyTypeId =
                property.propertyTypeId

            selectedProvinceId =
                property.provinceId

            selectedWardId =
                property.wardId

            binding.actPropertyType.setText(
                property.propertyTypeName,
                false
            )

            binding.edtPropertyName.setText(
                property.propertyName
            )

            binding.edtDescription.setText(
                property.description.orEmpty()
            )

            binding.actProvince.setText(
                property.provinceName,
                false
            )

            binding.edtAddress.setText(
                property.addressLine
            )

            loadWards(
                provinceId =
                    property.provinceId,

                selectedWardName =
                    property.wardName
            )

            binding.progressEdit.isVisible =
                false

            binding.scrollContent.isVisible =
                true
        }
    }

    private fun setupPropertyTypeDropdown() {

        binding.actPropertyType.setAdapter(
            ArrayAdapter(
                this,
                android.R.layout
                    .simple_dropdown_item_1line,
                propertyTypes.map {
                    it.typeName
                }
            )
        )
    }

    private fun setupProvinceDropdown() {

        binding.actProvince.setAdapter(
            ArrayAdapter(
                this,
                android.R.layout
                    .simple_dropdown_item_1line,
                provinces.map {
                    it.provinceName
                }
            )
        )
    }

    private fun loadWards(
        provinceId: Int,
        selectedWardName: String? = null
    ) {

        lifecycleScope.launch {

            when (
                val result =
                    catalogRepository
                        .getWards(
                            provinceId
                        )
            ) {

                is ApiResult.Success -> {

                    if (
                        selectedProvinceId !=
                        provinceId
                    ) {
                        return@launch
                    }

                    wards =
                        result.data.orEmpty()

                    binding.actWard.setAdapter(
                        ArrayAdapter(
                            this@EditPropertyActivity,
                            android.R.layout
                                .simple_dropdown_item_1line,
                            wards.map {
                                it.wardName
                            }
                        )
                    )

                    if (
                        selectedWardName != null
                    ) {

                        binding.actWard.setText(
                            selectedWardName,
                            false
                        )
                    }
                }

                is ApiResult.Error -> {

                    Toast.makeText(
                        this@EditPropertyActivity,
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

    private fun saveProperty() {

        if (isSaving) {
            return
        }

        clearErrors()

        val propertyName =
            binding.edtPropertyName.text
                ?.toString()
                ?.trim()
                .orEmpty()

        val description =
            binding.edtDescription.text
                ?.toString()
                ?.trim()
                .orEmpty()

        val address =
            binding.edtAddress.text
                ?.toString()
                ?.trim()
                .orEmpty()

        val propertyTypeId =
            selectedPropertyTypeId

        val provinceId =
            selectedProvinceId

        val wardId =
            selectedWardId

        if (propertyTypeId == null) {

            binding.tilPropertyType.error =
                "Vui lòng chọn loại khu trọ."

            return
        }

        if (propertyName.isBlank()) {

            binding.tilPropertyName.error =
                "Vui lòng nhập tên khu trọ."

            return
        }

        if (provinceId == null) {

            binding.tilProvince.error =
                "Vui lòng chọn tỉnh/thành phố."

            return
        }

        if (wardId == null) {

            binding.tilWard.error =
                "Vui lòng chọn phường/xã."

            return
        }

        if (address.isBlank()) {

            binding.tilAddress.error =
                "Vui lòng nhập địa chỉ."

            return
        }

        updateProperty(
            propertyTypeId,
            propertyName,
            description,
            provinceId,
            wardId,
            address
        )
    }

    private fun updateProperty(
        propertyTypeId: Int,
        propertyName: String,
        description: String,
        provinceId: Int,
        wardId: Int,
        address: String
    ) {

        isSaving = true

        binding.btnSave.isEnabled =
            false

        binding.btnSave.text =
            "Đang lưu..."

        lifecycleScope.launch {

            try {

                val request =
                    UpdatePropertyRequest(
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
                            address,

                        latitude =
                            null,

                        longitude =
                            null
                    )

                when (
                    val result =
                        propertyRepository
                            .updateProperty(
                                propertyId,
                                request
                            )
                ) {

                    is ApiResult.Success -> {

                        Toast.makeText(
                            this@EditPropertyActivity,
                            "Đã cập nhật khu trọ.",
                            Toast.LENGTH_SHORT
                        ).show()

                        setResult(
                            RESULT_OK
                        )

                        finish()
                    }

                    is ApiResult.Error -> {

                        Toast.makeText(
                            this@EditPropertyActivity,
                            result.exception.message
                                ?: "Không thể cập nhật khu trọ.",
                            Toast.LENGTH_LONG
                        ).show()
                    }

                    ApiResult.Loading ->
                        Unit
                }

            } finally {

                isSaving = false

                binding.btnSave.isEnabled =
                    true

                binding.btnSave.text =
                    "Lưu thay đổi"
            }
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