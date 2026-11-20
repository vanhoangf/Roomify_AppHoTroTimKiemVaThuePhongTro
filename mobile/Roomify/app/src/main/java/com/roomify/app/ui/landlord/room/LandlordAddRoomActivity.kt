package com.roomify.app.ui.landlord.room

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.roomify.app.data.remote.api.CatalogApi
import com.roomify.app.data.remote.api.LandlordRoomApi
import com.roomify.app.data.remote.common.ApiResult
import com.roomify.app.data.remote.dto.catalog.AmenityResponse
import com.roomify.app.data.remote.dto.landlord.CreateRoomRequest
import com.roomify.app.data.remote.network.RetrofitClient
import com.roomify.app.data.repository.CatalogRepository
import com.roomify.app.data.repository.LandlordRoomRepository
import com.roomify.app.databinding.ActivityAddRoomBinding
import com.roomify.app.ui.common.RoleSessionGuard

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class LandlordAddRoomActivity :
    AppCompatActivity() {

    companion object {

        const val EXTRA_PROPERTY_ID =
            "extra_property_id"

        const val EXTRA_PROPERTY_NAME =
            "extra_property_name"
    }

    private lateinit var binding:
            ActivityAddRoomBinding

    private lateinit var roomRepository:
            LandlordRoomRepository

    private lateinit var catalogRepository:
            CatalogRepository

    private var propertyId =
        0L

    private var propertyName =
        ""

    private var amenities:
            List<AmenityResponse> =
        emptyList()

    private val selectedAmenityIds =
        mutableSetOf<Int>()

    private var selectedGender =
        "ALL"

    private var availableFrom:
            String? = null

    private var isSaving =
        false

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(
            savedInstanceState
        )

        binding =
            ActivityAddRoomBinding.inflate(
                layoutInflater
            )

        setContentView(
            binding.root
        )

        setupInsets()

        propertyId =
            intent.getLongExtra(
                EXTRA_PROPERTY_ID,
                0L
            )

        propertyName =
            intent.getStringExtra(
                EXTRA_PROPERTY_NAME
            )
                ?.trim()
                .orEmpty()

        if (propertyId <= 0L) {

            Toast.makeText(
                this,
                "Không tìm thấy khu trọ.",
                Toast.LENGTH_SHORT
            ).show()

            finish()
            return
        }

        binding.tvPropertyName.text =
            propertyName.ifBlank {
                "Khu trọ"
            }

        setupRepositories()
        setupGender()
        setupActions()
        loadAmenities()
    }

    private fun setupInsets() {

        ViewCompat.setOnApplyWindowInsetsListener(
            binding.addRoomRoot
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

        roomRepository =
            LandlordRoomRepository(
                RetrofitClient.createService(
                    LandlordRoomApi::class.java
                )
            )

        catalogRepository =
            CatalogRepository(
                RetrofitClient.createService(
                    CatalogApi::class.java
                )
            )
    }

    private fun setupGender() {

        val labels =
            listOf(
                "Tất cả",
                "Nam",
                "Nữ"
            )

        binding.actGender.setAdapter(
            ArrayAdapter(
                this,
                android.R.layout
                    .simple_dropdown_item_1line,
                labels
            )
        )

        binding.actGender.setText(
            "Tất cả",
            false
        )

        binding.actGender
            .setOnItemClickListener {
                    _, _, position, _ ->

                selectedGender =
                    when (position) {

                        1 ->
                            "MALE"

                        2 ->
                            "FEMALE"

                        else ->
                            "ALL"
                    }
            }
    }

    private fun setupActions() {

        binding.btnBack
            .setOnClickListener {
                finish()
            }

        binding.edtAvailableFrom
            .setOnClickListener {
                showDatePicker()
            }

        binding.btnSelectAmenities
            .setOnClickListener {
                showAmenityDialog()
            }

        binding.btnCreateRoom
            .setOnClickListener {
                validateAndCreate()
            }
    }

    private fun loadAmenities() {

        lifecycleScope.launch {

            when (
                val result =
                    catalogRepository
                        .getAmenities()
            ) {

                is ApiResult.Success -> {

                    amenities =
                        result.data
                            .orEmpty()

                    binding.btnSelectAmenities
                        .isEnabled =
                        amenities.isNotEmpty()
                }

                is ApiResult.Error -> {

                    binding.btnSelectAmenities
                        .isEnabled =
                        false

                    Toast.makeText(
                        this@LandlordAddRoomActivity,
                        "Không thể tải danh sách tiện ích.",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                ApiResult.Loading ->
                    Unit
            }
        }
    }

    private fun showAmenityDialog() {

        if (amenities.isEmpty()) {

            Toast.makeText(
                this,
                "Chưa có danh sách tiện ích.",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        val names =
            amenities
                .map {
                    it.amenityName
                }
                .toTypedArray()

        val checked =
            BooleanArray(
                amenities.size
            ) { index ->

                selectedAmenityIds.contains(
                    amenities[index]
                        .amenityId
                )
            }

        MaterialAlertDialogBuilder(this)
            .setTitle(
                "Chọn tiện ích"
            )
            .setMultiChoiceItems(
                names,
                checked
            ) { _, which, isChecked ->

                val amenityId =
                    amenities[which]
                        .amenityId

                if (isChecked) {

                    selectedAmenityIds.add(
                        amenityId
                    )

                } else {

                    selectedAmenityIds.remove(
                        amenityId
                    )
                }
            }
            .setNegativeButton(
                "Hủy",
                null
            )
            .setPositiveButton(
                "Xong"
            ) { _, _ ->

                updateSelectedAmenitiesText()
            }
            .show()
    }

    private fun updateSelectedAmenitiesText() {

        val selectedNames =
            amenities
                .filter {

                    selectedAmenityIds
                        .contains(
                            it.amenityId
                        )
                }
                .map {
                    it.amenityName
                }

        binding.tvSelectedAmenities.text =
            if (
                selectedNames.isEmpty()
            ) {

                "Chưa chọn tiện ích"

            } else {

                selectedNames.joinToString(
                    ", "
                )
            }
    }

    private fun showDatePicker() {

        val picker =
            MaterialDatePicker.Builder
                .datePicker()
                .setTitleText(
                    "Chọn ngày có thể thuê"
                )
                .build()

        picker.addOnPositiveButtonClickListener {
                millis ->

            val formatter =
                SimpleDateFormat(
                    "yyyy-MM-dd",
                    Locale.US
                )

            formatter.timeZone =
                TimeZone.getTimeZone(
                    "UTC"
                )

            availableFrom =
                formatter.format(
                    Date(millis)
                )

            binding.edtAvailableFrom
                .setText(
                    availableFrom
                )
        }

        picker.show(
            supportFragmentManager,
            "available_from"
        )
    }

    private fun validateAndCreate() {

        if (isSaving) {
            return
        }

        clearErrors()

        val roomNumber =
            binding.edtRoomNumber.text
                ?.toString()
                ?.trim()
                .orEmpty()

        val title =
            binding.edtTitle.text
                ?.toString()
                ?.trim()
                .orEmpty()

        val description =
            binding.edtDescription.text
                ?.toString()
                ?.trim()
                .orEmpty()

        val area =
            parseDecimal(
                binding.edtArea.text
                    ?.toString()
            )

        val rent =
            parseDecimal(
                binding.edtRentPrice.text
                    ?.toString()
            )

        val deposit =
            parseDecimal(
                binding.edtDeposit.text
                    ?.toString()
            )

        val electricity =
            parseDecimal(
                binding.edtElectricity.text
                    ?.toString()
            )

        val water =
            parseDecimal(
                binding.edtWater.text
                    ?.toString()
            )

        val internet =
            parseDecimal(
                binding.edtInternet.text
                    ?.toString()
            )

        val serviceFee =
            parseDecimal(
                binding.edtServiceFee.text
                    ?.toString()
            )

        val parkingFee =
            parseDecimal(
                binding.edtParkingFee.text
                    ?.toString()
            )

        val maxOccupants =
            binding.edtMaxOccupants.text
                ?.toString()
                ?.trim()
                ?.toIntOrNull()

        if (title.isBlank()) {

            binding.tilTitle.error =
                "Vui lòng nhập tên phòng."

            binding.edtTitle.requestFocus()

            return
        }

        if (
            area == null ||
            area <= BigDecimal.ZERO
        ) {

            binding.tilArea.error =
                "Diện tích phải lớn hơn 0."

            binding.edtArea.requestFocus()

            return
        }

        if (
            rent == null ||
            rent < BigDecimal.ZERO
        ) {

            binding.tilRentPrice.error =
                "Vui lòng nhập giá thuê hợp lệ."

            binding.edtRentPrice.requestFocus()

            return
        }

        if (
            maxOccupants == null ||
            maxOccupants < 1
        ) {

            binding.tilMaxOccupants.error =
                "Số người tối đa phải từ 1 trở lên."

            binding.edtMaxOccupants
                .requestFocus()

            return
        }

        val request =
            CreateRoomRequest(

                roomNumber =
                    roomNumber.takeIf {
                        it.isNotBlank()
                    },

                title =
                    title,

                description =
                    description.takeIf {
                        it.isNotBlank()
                    },

                areaM2 =
                    area,

                rentPrice =
                    rent,

                depositAmount =
                    deposit,

                electricityPrice =
                    electricity,

                waterPrice =
                    water,

                internetPrice =
                    internet,

                serviceFee =
                    serviceFee,

                parkingFee =
                    parkingFee,

                maxOccupants =
                    maxOccupants,

                genderPreference =
                    selectedGender,

                availableFrom =
                    availableFrom,

                amenityIds =
                    selectedAmenityIds
                        .toList()
            )

        createRoom(
            request
        )
    }

    private fun createRoom(
        request: CreateRoomRequest
    ) {

        isSaving =
            true

        binding.btnCreateRoom.isEnabled =
            false

        binding.btnCreateRoom.text =
            "Đang tạo..."

        lifecycleScope.launch {

            try {

                when (
                    val result =
                        roomRepository
                            .createRoom(
                                propertyId,
                                request
                            )
                ) {

                    is ApiResult.Success -> {

                        Toast.makeText(
                            this@LandlordAddRoomActivity,
                            "Đã tạo phòng thành công.",
                            Toast.LENGTH_SHORT
                        ).show()

                        setResult(
                            RESULT_OK
                        )

                        finish()
                    }

                    is ApiResult.Error -> {

                        Toast.makeText(
                            this@LandlordAddRoomActivity,
                            result.exception.message
                                ?: "Không thể tạo phòng.",
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
                    this@LandlordAddRoomActivity,
                    e.message
                        ?: "Không thể tạo phòng.",
                    Toast.LENGTH_LONG
                ).show()

            } finally {

                isSaving =
                    false

                binding.btnCreateRoom.isEnabled =
                    true

                binding.btnCreateRoom.text =
                    "Tạo phòng"
            }
        }
    }

    private fun parseDecimal(
        value: String?
    ): BigDecimal? {

        val clean =
            value
                ?.trim()
                ?.replace(
                    ",",
                    "."
                )
                .orEmpty()

        if (clean.isBlank()) {
            return null
        }

        return clean.toBigDecimalOrNull()
    }

    private fun clearErrors() {

        binding.tilTitle.error =
            null

        binding.tilArea.error =
            null

        binding.tilRentPrice.error =
            null

        binding.tilMaxOccupants.error =
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