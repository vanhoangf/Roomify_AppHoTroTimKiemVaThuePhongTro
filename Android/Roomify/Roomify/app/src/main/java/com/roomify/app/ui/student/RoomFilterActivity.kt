package com.roomify.app.ui.student

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter

import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope

import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar

import com.roomify.app.data.remote.api.CatalogApi
import com.roomify.app.data.remote.common.ApiResult
import com.roomify.app.data.remote.dto.catalog.AmenityResponse
import com.roomify.app.data.remote.dto.catalog.PropertyTypeResponse
import com.roomify.app.data.remote.dto.catalog.ProvinceResponse
import com.roomify.app.data.remote.dto.catalog.WardResponse
import com.roomify.app.data.remote.network.RetrofitClient
import com.roomify.app.data.repository.CatalogRepository
import com.roomify.app.databinding.ActivityRoomFilterBinding

import kotlinx.coroutines.launch

import java.math.BigDecimal

class RoomFilterActivity : AppCompatActivity() {

    companion object {

        const val EXTRA_PROVINCE_ID =
            "filter_province_id"

        const val EXTRA_WARD_ID =
            "filter_ward_id"

        const val EXTRA_PROPERTY_TYPE_ID =
            "filter_property_type_id"

        const val EXTRA_MIN_PRICE =
            "filter_min_price"

        const val EXTRA_MAX_PRICE =
            "filter_max_price"

        const val EXTRA_MIN_AREA =
            "filter_min_area"

        const val EXTRA_MAX_AREA =
            "filter_max_area"

        const val EXTRA_AMENITY_IDS =
            "filter_amenity_ids"
    }

    private lateinit var binding:
            ActivityRoomFilterBinding

    private lateinit var catalogRepository:
            CatalogRepository

    // ========================================================
    // CATALOG DATA
    // ========================================================

    private var provinces:
            List<ProvinceResponse> =
        emptyList()

    private var wards:
            List<WardResponse> =
        emptyList()

    private var propertyTypes:
            List<PropertyTypeResponse> =
        emptyList()

    private var amenities:
            List<AmenityResponse> =
        emptyList()

    // ========================================================
    // FILTER STATE
    // ========================================================

    private var selectedProvinceId:
            Int? = null

    private var selectedWardId:
            Int? = null

    private var selectedPropertyTypeId:
            Int? = null

    private val selectedAmenityIds =
        mutableSetOf<Int>()

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        binding =
            ActivityRoomFilterBinding.inflate(
                layoutInflater
            )

        setContentView(binding.root)

        setupWindowInsets()
        setupRepository()

        readInitialFilters()

        setupInitialState()
        setupListeners()

        loadInitialCatalogs()
    }

    // ========================================================
    // WINDOW INSETS
    // ========================================================

    private fun setupWindowInsets() {

        val root =
            binding.roomFilterRoot

        val left =
            root.paddingLeft

        val top =
            root.paddingTop

        val right =
            root.paddingRight

        val bottom =
            root.paddingBottom

        ViewCompat
            .setOnApplyWindowInsetsListener(
                root
            ) { view, insets ->

                val bars =
                    insets.getInsets(
                        WindowInsetsCompat
                            .Type.systemBars()
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
    // REPOSITORY
    // ========================================================

    private fun setupRepository() {

        RetrofitClient.initialize(
            applicationContext
        )

        val api =
            RetrofitClient.createService(
                CatalogApi::class.java
            )

        catalogRepository =
            CatalogRepository(api)
    }

    // ========================================================
    // READ CURRENT FILTER
    // ========================================================

    private fun readInitialFilters() {

        selectedProvinceId =
            intent.getIntExtraOrNull(
                EXTRA_PROVINCE_ID
            )

        selectedWardId =
            intent.getIntExtraOrNull(
                EXTRA_WARD_ID
            )

        selectedPropertyTypeId =
            intent.getIntExtraOrNull(
                EXTRA_PROPERTY_TYPE_ID
            )

        val amenityIds =
            intent.getIntArrayExtra(
                EXTRA_AMENITY_IDS
            ) ?: intArrayOf()

        selectedAmenityIds.clear()
        selectedAmenityIds.addAll(
            amenityIds.toList()
        )

        if (
            intent.hasExtra(
                EXTRA_MIN_PRICE
            )
        ) {

            binding.edtMinPrice.setText(
                intent.getLongExtra(
                    EXTRA_MIN_PRICE,
                    0L
                ).toString()
            )
        }

        if (
            intent.hasExtra(
                EXTRA_MAX_PRICE
            )
        ) {

            binding.edtMaxPrice.setText(
                intent.getLongExtra(
                    EXTRA_MAX_PRICE,
                    0L
                ).toString()
            )
        }

        binding.edtMinArea.setText(
            intent.getStringExtra(
                EXTRA_MIN_AREA
            ).orEmpty()
        )

        binding.edtMaxArea.setText(
            intent.getStringExtra(
                EXTRA_MAX_AREA
            ).orEmpty()
        )
    }

    // ========================================================
    // INITIAL STATE
    // ========================================================

    private fun setupInitialState() {

        binding.actProvince.isEnabled =
            false

        binding.actWard.isEnabled =
            false

        binding.actPropertyType.isEnabled =
            false
    }

    // ========================================================
    // LISTENERS
    // ========================================================

    private fun setupListeners() {

        binding.btnBackFilter
            .setOnClickListener {

                onBackPressedDispatcher
                    .onBackPressed()
            }

        binding.btnResetFilter
            .setOnClickListener {

                resetFilters()
            }

        binding.btnApplyFilter
            .setOnClickListener {

                if (validateFilters()) {
                    returnFilterResult()
                }
            }

        // -----------------------------
        // PROVINCE
        // -----------------------------

        binding.actProvince
            .setOnItemClickListener {
                    _, _, position, _ ->

                if (
                    position !in
                    provinces.indices
                ) {
                    return@setOnItemClickListener
                }

                val province =
                    provinces[position]

                selectedProvinceId =
                    province.provinceId

                selectedWardId = null

                binding.actWard.setText(
                    "",
                    false
                )

                binding.actWard.isEnabled =
                    false

                lifecycleScope.launch {

                    loadWards(
                        province.provinceId
                    )
                }
            }

        // -----------------------------
        // WARD
        // -----------------------------

        binding.actWard
            .setOnItemClickListener {
                    _, _, position, _ ->

                if (
                    position in wards.indices
                ) {

                    selectedWardId =
                        wards[position].wardId
                }
            }

        // -----------------------------
        // PROPERTY TYPE
        // -----------------------------

        binding.actPropertyType
            .setOnItemClickListener {
                    _, _, position, _ ->

                if (
                    position in
                    propertyTypes.indices
                ) {

                    selectedPropertyTypeId =
                        propertyTypes[
                            position
                        ].propertyTypeId
                }
            }
    }

    // ========================================================
    // LOAD CATALOGS
    // ========================================================

    private fun loadInitialCatalogs() {

        lifecycleScope.launch {

            loadProvinces()

            loadPropertyTypes()

            loadAmenities()
        }
    }

    // ========================================================
    // PROVINCES
    // ========================================================

    private suspend fun loadProvinces() {

        when (
            val result =
                catalogRepository
                    .getProvinces()
        ) {

            is ApiResult.Success -> {

                provinces =
                    result.data.orEmpty()

                val adapter =
                    ArrayAdapter(
                        this,
                        android.R.layout
                            .simple_dropdown_item_1line,
                        provinces.map {
                            it.provinceName
                        }
                    )

                binding.actProvince
                    .setAdapter(adapter)

                binding.actProvince
                    .isEnabled =
                    provinces.isNotEmpty()

                restoreProvince()
            }

            is ApiResult.Error -> {

                binding.actProvince
                    .isEnabled = false

                showCatalogError(
                    "Không thể tải tỉnh/thành phố."
                )
            }

            ApiResult.Loading -> Unit
        }
    }

    private suspend fun restoreProvince() {

        val provinceId =
            selectedProvinceId
                ?: return

        val province =
            provinces.firstOrNull {

                it.provinceId ==
                        provinceId
            } ?: return

        binding.actProvince.setText(
            province.provinceName,
            false
        )

        loadWards(
            provinceId = provinceId,
            restoreWardId =
                selectedWardId
        )
    }

    // ========================================================
    // WARDS
    // ========================================================

    private suspend fun loadWards(
        provinceId: Int,
        restoreWardId: Int? = null
    ) {

        when (
            val result =
                catalogRepository
                    .getWards(
                        provinceId
                    )
        ) {

            is ApiResult.Success -> {

                wards =
                    result.data.orEmpty()

                val adapter =
                    ArrayAdapter(
                        this,
                        android.R.layout
                            .simple_dropdown_item_1line,
                        wards.map {
                            it.wardName
                        }
                    )

                binding.actWard
                    .setAdapter(adapter)

                binding.actWard.isEnabled =
                    wards.isNotEmpty()

                if (
                    restoreWardId != null
                ) {

                    val ward =
                        wards.firstOrNull {

                            it.wardId ==
                                    restoreWardId
                        }

                    if (ward != null) {

                        binding.actWard
                            .setText(
                                ward.wardName,
                                false
                            )
                    }
                }
            }

            is ApiResult.Error -> {

                wards = emptyList()

                binding.actWard
                    .isEnabled = false

                showCatalogError(
                    "Không thể tải xã/phường."
                )
            }

            ApiResult.Loading -> Unit
        }
    }

    // ========================================================
    // PROPERTY TYPES
    // ========================================================

    private suspend fun loadPropertyTypes() {

        when (
            val result =
                catalogRepository
                    .getPropertyTypes()
        ) {

            is ApiResult.Success -> {

                propertyTypes =
                    result.data.orEmpty()

                val adapter =
                    ArrayAdapter(
                        this,
                        android.R.layout
                            .simple_dropdown_item_1line,
                        propertyTypes.map {
                            it.typeName
                        }
                    )

                binding.actPropertyType
                    .setAdapter(adapter)

                binding.actPropertyType
                    .isEnabled =
                    propertyTypes.isNotEmpty()

                val current =
                    selectedPropertyTypeId

                if (current != null) {

                    propertyTypes
                        .firstOrNull {

                            it.propertyTypeId ==
                                    current
                        }
                        ?.let {

                            binding
                                .actPropertyType
                                .setText(
                                    it.typeName,
                                    false
                                )
                        }
                }
            }

            is ApiResult.Error -> {

                binding.actPropertyType
                    .isEnabled = false

                showCatalogError(
                    "Không thể tải loại khu trọ."
                )
            }

            ApiResult.Loading -> Unit
        }
    }

    // ========================================================
    // AMENITIES
    // ========================================================

    private suspend fun loadAmenities() {

        when (
            val result =
                catalogRepository
                    .getAmenities()
        ) {

            is ApiResult.Success -> {

                amenities =
                    result.data.orEmpty()

                showAmenityChips()
            }

            is ApiResult.Error -> {

                showCatalogError(
                    "Không thể tải tiện ích."
                )
            }

            ApiResult.Loading -> Unit
        }
    }

    private fun showAmenityChips() {

        binding.chipGroupAmenities
            .removeAllViews()

        amenities.forEach { amenity ->

            val chip =
                Chip(this).apply {

                    id =
                        View.generateViewId()

                    text =
                        amenity.amenityName

                    isCheckable = true

                    isChecked =
                        amenity.amenityId in
                                selectedAmenityIds

                    setOnCheckedChangeListener {
                            _, checked ->

                        if (checked) {

                            selectedAmenityIds
                                .add(
                                    amenity
                                        .amenityId
                                )

                        } else {

                            selectedAmenityIds
                                .remove(
                                    amenity
                                        .amenityId
                                )
                        }
                    }
                }

            binding.chipGroupAmenities
                .addView(chip)
        }
    }

    // ========================================================
    // RESET
    // ========================================================

    private fun resetFilters() {

        selectedProvinceId = null
        selectedWardId = null
        selectedPropertyTypeId = null

        selectedAmenityIds.clear()

        binding.actProvince.setText(
            "",
            false
        )

        binding.actWard.setText(
            "",
            false
        )

        binding.actPropertyType.setText(
            "",
            false
        )

        binding.edtMinPrice
            .text?.clear()

        binding.edtMaxPrice
            .text?.clear()

        binding.edtMinArea
            .text?.clear()

        binding.edtMaxArea
            .text?.clear()

        binding.chipGroupAmenities
            .clearCheck()

        binding.actWard.isEnabled =
            false

        clearErrors()
    }

    // ========================================================
    // RETURN RESULT TO ST-03
    // ========================================================

    private fun returnFilterResult() {

        val minPrice =
            binding.edtMinPrice.text
                ?.toString()
                ?.trim()
                ?.toLongOrNull()

        val maxPrice =
            binding.edtMaxPrice.text
                ?.toString()
                ?.trim()
                ?.toLongOrNull()

        val minArea =
            parseDecimal(
                binding.edtMinArea.text
                    ?.toString()
                    ?.trim()
                    .orEmpty()
            )

        val maxArea =
            parseDecimal(
                binding.edtMaxArea.text
                    ?.toString()
                    ?.trim()
                    .orEmpty()
            )

        val result =
            Intent()

        selectedProvinceId?.let {
            result.putExtra(
                EXTRA_PROVINCE_ID,
                it
            )
        }

        selectedWardId?.let {
            result.putExtra(
                EXTRA_WARD_ID,
                it
            )
        }

        selectedPropertyTypeId?.let {
            result.putExtra(
                EXTRA_PROPERTY_TYPE_ID,
                it
            )
        }

        minPrice?.let {
            result.putExtra(
                EXTRA_MIN_PRICE,
                it
            )
        }

        maxPrice?.let {
            result.putExtra(
                EXTRA_MAX_PRICE,
                it
            )
        }

        minArea?.let {
            result.putExtra(
                EXTRA_MIN_AREA,
                it.toPlainString()
            )
        }

        maxArea?.let {
            result.putExtra(
                EXTRA_MAX_AREA,
                it.toPlainString()
            )
        }

        result.putExtra(
            EXTRA_AMENITY_IDS,
            selectedAmenityIds
                .sorted()
                .toIntArray()
        )

        setResult(
            RESULT_OK,
            result
        )

        finish()
    }

    // ========================================================
    // VALIDATION
    // ========================================================

    private fun validateFilters():
            Boolean {

        clearErrors()

        var valid = true

        val minPriceText =
            binding.edtMinPrice.text
                ?.toString()
                ?.trim()
                .orEmpty()

        val maxPriceText =
            binding.edtMaxPrice.text
                ?.toString()
                ?.trim()
                .orEmpty()

        val minPrice =
            minPriceText.toLongOrNull()

        val maxPrice =
            maxPriceText.toLongOrNull()

        if (
            minPriceText.isNotBlank() &&
            minPrice == null
        ) {

            binding.tilMinPrice.error =
                "Giá không hợp lệ."

            valid = false
        }

        if (
            maxPriceText.isNotBlank() &&
            maxPrice == null
        ) {

            binding.tilMaxPrice.error =
                "Giá không hợp lệ."

            valid = false
        }

        if (
            minPrice != null &&
            maxPrice != null &&
            minPrice > maxPrice
        ) {

            binding.tilMaxPrice.error =
                "Giá tối đa phải ≥ giá tối thiểu."

            valid = false
        }

        val minAreaText =
            binding.edtMinArea.text
                ?.toString()
                ?.trim()
                .orEmpty()

        val maxAreaText =
            binding.edtMaxArea.text
                ?.toString()
                ?.trim()
                .orEmpty()

        val minArea =
            parseDecimal(
                minAreaText
            )

        val maxArea =
            parseDecimal(
                maxAreaText
            )

        if (
            minAreaText.isNotBlank() &&
            minArea == null
        ) {

            binding.tilMinArea.error =
                "Diện tích không hợp lệ."

            valid = false
        }

        if (
            maxAreaText.isNotBlank() &&
            maxArea == null
        ) {

            binding.tilMaxArea.error =
                "Diện tích không hợp lệ."

            valid = false
        }

        if (
            minArea != null &&
            minArea <= BigDecimal.ZERO
        ) {

            binding.tilMinArea.error =
                "Diện tích phải lớn hơn 0."

            valid = false
        }

        if (
            maxArea != null &&
            maxArea <= BigDecimal.ZERO
        ) {

            binding.tilMaxArea.error =
                "Diện tích phải lớn hơn 0."

            valid = false
        }

        if (
            minArea != null &&
            maxArea != null &&
            minArea > maxArea
        ) {

            binding.tilMaxArea.error =
                "Diện tích tối đa phải ≥ tối thiểu."

            valid = false
        }

        return valid
    }

    private fun parseDecimal(
        text: String
    ): BigDecimal? {

        if (text.isBlank()) {
            return null
        }

        return try {

            BigDecimal(
                text.replace(",", ".")
            )

        } catch (_: NumberFormatException) {

            null
        }
    }

    private fun clearErrors() {

        binding.tilMinPrice.error =
            null

        binding.tilMaxPrice.error =
            null

        binding.tilMinArea.error =
            null

        binding.tilMaxArea.error =
            null
    }

    // ========================================================
    // ERROR
    // ========================================================

    private fun showCatalogError(
        message: String
    ) {

        Snackbar.make(
            binding.root,
            message,
            Snackbar.LENGTH_LONG
        ).show()
    }

    // ========================================================
    // INTENT HELPERS
    // ========================================================

    private fun Intent.getIntExtraOrNull(
        key: String
    ): Int? {

        return if (hasExtra(key)) {

            getIntExtra(
                key,
                0
            )

        } else {

            null
        }
    }
}