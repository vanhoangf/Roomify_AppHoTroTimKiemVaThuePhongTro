package com.roomify.app.ui.landlord.room

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast

import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope

import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.roomify.app.data.remote.api.CatalogApi
import com.roomify.app.data.remote.api.LandlordRoomApi
import com.roomify.app.data.remote.common.ApiResult
import com.roomify.app.data.remote.dto.catalog.AmenityResponse
import com.roomify.app.data.remote.dto.landlord.RoomResponse
import com.roomify.app.data.remote.dto.landlord.UpdateRoomRequest
import com.roomify.app.data.remote.network.RetrofitClient
import com.roomify.app.data.repository.CatalogRepository
import com.roomify.app.data.repository.LandlordRoomRepository
import com.roomify.app.databinding.ActivityLandlordEditRoomBinding
import com.roomify.app.ui.common.RoleSessionGuard

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class LandlordEditRoomActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_ROOM_ID =
            "extra_room_id"
    }

    private lateinit var binding:
            ActivityLandlordEditRoomBinding

    private lateinit var roomRepository:
            LandlordRoomRepository

    private lateinit var catalogRepository:
            CatalogRepository

    private var roomId =
        0L

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
        super.onCreate(savedInstanceState)

        binding =
            ActivityLandlordEditRoomBinding.inflate(
                layoutInflater
            )

        setContentView(
            binding.root
        )

        roomId =
            intent.getLongExtra(
                EXTRA_ROOM_ID,
                0L
            )

        if (roomId <= 0L) {

            Toast.makeText(
                this,
                "Không tìm thấy phòng.",
                Toast.LENGTH_SHORT
            ).show()

            finish()
            return
        }

        setupInsets()
        setupRepositories()
        setupGender()
        setupActions()

        loadData()
    }

    private fun setupInsets() {

        ViewCompat.setOnApplyWindowInsetsListener(
            binding.landlordEditRoomRoot
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

        binding.actGender
            .setOnItemClickListener {
                    _, _, position, _ ->

                selectedGender =
                    when (position) {

                        1 -> "MALE"

                        2 -> "FEMALE"

                        else -> "ALL"
                    }
            }
    }

    private fun setupActions() {

        binding.btnBack.setOnClickListener {
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

        binding.btnSaveRoom
            .setOnClickListener {
                validateAndSave()
            }
    }

    private fun loadData() {

        binding.progressEditRoom.isVisible =
            true

        binding.scrollContent.isVisible =
            false

        lifecycleScope.launch {

            try {

                val amenityResult =
                    catalogRepository
                        .getAmenities()

                val roomResult =
                    roomRepository
                        .getRoom(
                            roomId
                        )

                if (
                    amenityResult
                            !is ApiResult.Success
                ) {

                    Toast.makeText(
                        this@LandlordEditRoomActivity,
                        "Không thể tải tiện ích.",
                        Toast.LENGTH_LONG
                    ).show()

                    return@launch
                }

                if (
                    roomResult
                            !is ApiResult.Success
                ) {

                    Toast.makeText(
                        this@LandlordEditRoomActivity,
                        "Không thể tải thông tin phòng.",
                        Toast.LENGTH_LONG
                    ).show()

                    return@launch
                }

                amenities =
                    amenityResult.data
                        .orEmpty()

                val room =
                    roomResult.data

                if (room == null) {

                    Toast.makeText(
                        this@LandlordEditRoomActivity,
                        "Không tìm thấy phòng.",
                        Toast.LENGTH_LONG
                    ).show()

                    finish()
                    return@launch
                }

                bindRoom(
                    room
                )

                binding.progressEditRoom.isVisible =
                    false

                binding.scrollContent.isVisible =
                    true

            } catch (
                e: CancellationException
            ) {

                throw e

            } catch (
                e: Exception
            ) {

                Toast.makeText(
                    this@LandlordEditRoomActivity,
                    e.message
                        ?: "Không thể tải dữ liệu.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun bindRoom(
        room: RoomResponse
    ) {

        binding.tvPropertyName.text =
            room.propertyName

        binding.edtRoomNumber.setText(
            room.roomNumber.orEmpty()
        )

        binding.edtTitle.setText(
            room.title
        )

        binding.edtDescription.setText(
            room.description.orEmpty()
        )

        binding.edtArea.setText(
            room.areaM2
                .stripTrailingZeros()
                .toPlainString()
        )

        binding.edtRentPrice.setText(
            room.rentPrice
                .stripTrailingZeros()
                .toPlainString()
        )

        binding.edtDeposit.setText(
            room.depositAmount
                ?.stripTrailingZeros()
                ?.toPlainString()
                .orEmpty()
        )

        binding.edtElectricity.setText(
            room.electricityPrice
                ?.stripTrailingZeros()
                ?.toPlainString()
                .orEmpty()
        )

        binding.edtWater.setText(
            room.waterPrice
                ?.stripTrailingZeros()
                ?.toPlainString()
                .orEmpty()
        )

        binding.edtInternet.setText(
            room.internetPrice
                ?.stripTrailingZeros()
                ?.toPlainString()
                .orEmpty()
        )

        binding.edtServiceFee.setText(
            room.serviceFee
                ?.stripTrailingZeros()
                ?.toPlainString()
                .orEmpty()
        )

        binding.edtParkingFee.setText(
            room.parkingFee
                ?.stripTrailingZeros()
                ?.toPlainString()
                .orEmpty()
        )

        binding.edtMaxOccupants.setText(
            room.maxOccupants.toString()
        )

        selectedGender =
            room.genderPreference
                .uppercase(
                    Locale.ROOT
                )

        binding.actGender.setText(
            when (selectedGender) {

                "MALE" ->
                    "Nam"

                "FEMALE" ->
                    "Nữ"

                else ->
                    "Tất cả"
            },
            false
        )

        availableFrom =
            room.availableFrom

        binding.edtAvailableFrom.setText(
            availableFrom.orEmpty()
        )

        selectedAmenityIds.clear()

        selectedAmenityIds.addAll(
            room.amenities.map {
                it.amenityId
            }
        )

        updateAmenitiesText()

        if (
            room.listingStatus.equals(
                "PENDING",
                ignoreCase = true
            )
        ) {

            binding.btnSaveRoom.isEnabled =
                false

            Toast.makeText(
                this,
                "Tin phòng đang chờ duyệt nên chưa thể chỉnh sửa.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun showAmenityDialog() {

        if (amenities.isEmpty()) {
            return
        }

        val names =
            amenities.map {
                it.amenityName
            }.toTypedArray()

        val checked =
            BooleanArray(
                amenities.size
            ) { index ->

                selectedAmenityIds
                    .contains(
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
            ) { _, which, selected ->

                val id =
                    amenities[which]
                        .amenityId

                if (selected) {

                    selectedAmenityIds.add(
                        id
                    )

                } else {

                    selectedAmenityIds.remove(
                        id
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

                updateAmenitiesText()
            }
            .show()
    }

    private fun updateAmenitiesText() {

        val names =
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
            if (names.isEmpty()) {

                "Chưa chọn tiện ích"

            } else {

                names.joinToString(
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
            "edit_room_date"
        )
    }

    private fun validateAndSave() {

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

        val maxOccupants =
            binding.edtMaxOccupants.text
                ?.toString()
                ?.trim()
                ?.toIntOrNull()

        if (title.isBlank()) {

            binding.tilTitle.error =
                "Vui lòng nhập tên phòng."

            return
        }

        if (
            area == null ||
            area <= BigDecimal.ZERO
        ) {

            binding.tilArea.error =
                "Diện tích phải lớn hơn 0."

            return
        }

        if (
            rent == null ||
            rent < BigDecimal.ZERO
        ) {

            binding.tilRentPrice.error =
                "Giá thuê không hợp lệ."

            return
        }

        if (
            maxOccupants == null ||
            maxOccupants < 1
        ) {

            binding.tilMaxOccupants.error =
                "Số người tối đa phải từ 1."

            return
        }

        val request =
            UpdateRoomRequest(
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
                    parseDecimal(
                        binding.edtDeposit.text
                            ?.toString()
                    ),

                electricityPrice =
                    parseDecimal(
                        binding.edtElectricity.text
                            ?.toString()
                    ),

                waterPrice =
                    parseDecimal(
                        binding.edtWater.text
                            ?.toString()
                    ),

                internetPrice =
                    parseDecimal(
                        binding.edtInternet.text
                            ?.toString()
                    ),

                serviceFee =
                    parseDecimal(
                        binding.edtServiceFee.text
                            ?.toString()
                    ),

                parkingFee =
                    parseDecimal(
                        binding.edtParkingFee.text
                            ?.toString()
                    ),

                maxOccupants =
                    maxOccupants,

                genderPreference =
                    selectedGender,

                availableFrom =
                    availableFrom
            )

        saveRoom(
            request
        )
    }

    private fun saveRoom(
        request: UpdateRoomRequest
    ) {

        isSaving =
            true

        binding.btnSaveRoom.isEnabled =
            false

        binding.btnSaveRoom.text =
            "Đang lưu..."

        lifecycleScope.launch {

            try {

                when (
                    val result =
                        roomRepository
                            .updateRoom(
                                roomId,
                                request
                            )
                ) {

                    is ApiResult.Success -> {

                        when (
                            val amenityResult =
                                roomRepository
                                    .updateAmenities(
                                        roomId,
                                        selectedAmenityIds
                                            .toList()
                                    )
                        ) {

                            is ApiResult.Success -> {

                                Toast.makeText(
                                    this@LandlordEditRoomActivity,
                                    "Đã cập nhật phòng.",
                                    Toast.LENGTH_SHORT
                                ).show()

                                setResult(
                                    RESULT_OK
                                )

                                finish()
                            }

                            is ApiResult.Error -> {

                                Toast.makeText(
                                    this@LandlordEditRoomActivity,
                                    "Thông tin phòng đã lưu nhưng chưa cập nhật được tiện ích.",
                                    Toast.LENGTH_LONG
                                ).show()
                            }

                            ApiResult.Loading ->
                                Unit
                        }
                    }

                    is ApiResult.Error -> {

                        Toast.makeText(
                            this@LandlordEditRoomActivity,
                            result.exception.message
                                ?: "Không thể cập nhật phòng.",
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
                    this@LandlordEditRoomActivity,
                    e.message
                        ?: "Không thể cập nhật phòng.",
                    Toast.LENGTH_LONG
                ).show()

            } finally {

                isSaving =
                    false

                binding.btnSaveRoom.isEnabled =
                    true

                binding.btnSaveRoom.text =
                    "Lưu thay đổi"
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

        return if (
            clean.isBlank()
        ) {
            null
        } else {
            clean.toBigDecimalOrNull()
        }
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