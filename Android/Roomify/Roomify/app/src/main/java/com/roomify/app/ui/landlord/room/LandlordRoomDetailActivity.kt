package com.roomify.app.ui.landlord.room

import android.os.Bundle
import android.widget.Toast
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope

import com.roomify.app.R
import com.roomify.app.data.remote.api.LandlordRoomApi
import com.roomify.app.data.remote.common.ApiResult
import com.roomify.app.data.remote.dto.landlord.RoomResponse
import com.roomify.app.data.remote.network.RetrofitClient
import com.roomify.app.data.repository.LandlordRoomRepository
import com.roomify.app.databinding.ActivityLandlordRoomDetailBinding
import com.roomify.app.ui.common.RoleSessionGuard
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

import java.text.NumberFormat
import java.util.Locale

class LandlordRoomDetailActivity :
    AppCompatActivity() {

    companion object {

        const val EXTRA_ROOM_ID =
            "extra_room_id"
    }

    private var currentRoom: RoomResponse? = null

    private lateinit var binding:
            ActivityLandlordRoomDetailBinding

    private lateinit var repository:
            LandlordRoomRepository

    private var roomId =
        0L

    private var isLoading =
        false

    private val currencyFormatter =
        NumberFormat.getCurrencyInstance(
            Locale.forLanguageTag("vi-VN")
        )

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        binding =
            ActivityLandlordRoomDetailBinding.inflate(
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
        setupRepository()
        setupActions()
    }

    private fun setupInsets() {

        ViewCompat.setOnApplyWindowInsetsListener(
            binding.roomDetailRoot
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
            LandlordRoomRepository(
                RetrofitClient.createService(
                    LandlordRoomApi::class.java
                )
            )
    }

    private fun setupActions() {

        binding.btnBack
            .setOnClickListener {
                finish()
            }

        binding.btnRetry
            .setOnClickListener {
                loadRoom()
            }

        binding.btnLandlordEditRoom
            .setOnClickListener {

                startActivity(
                    Intent(
                        this,
                        LandlordEditRoomActivity::class.java
                    ).apply {

                        putExtra(
                            LandlordEditRoomActivity.EXTRA_ROOM_ID,
                            roomId
                        )
                    }
                )
            }

        binding.btnLandlordChangeAvailability
            .setOnClickListener {
                showAvailabilityDialog()
            }

        binding.btnLandlordListingAction
            .setOnClickListener {
                handleListingAction()
            }

        binding.btnLandlordManageImages
            .setOnClickListener {

                val room =
                    currentRoom
                        ?: return@setOnClickListener

                startActivity(
                    Intent(
                        this,
                        LandlordRoomImagesActivity::class.java
                    ).apply {

                        putExtra(
                            LandlordRoomImagesActivity.EXTRA_ROOM_ID,
                            room.roomId
                        )

                        putExtra(
                            LandlordRoomImagesActivity.EXTRA_ROOM_NAME,
                            room.title
                        )

                        putExtra(
                            LandlordRoomImagesActivity.EXTRA_ROOM_LISTING_STATUS,
                            room.listingStatus
                        )
                    }
                )
            }
    }

    private fun updateAvailability(
        status: String
    ) {

        binding.btnLandlordChangeAvailability
            .isEnabled =
            false

        lifecycleScope.launch {

            try {

                when (
                    val result =
                        repository
                            .updateAvailability(
                                roomId,
                                status
                            )
                ) {

                    is ApiResult.Success -> {

                        Toast.makeText(
                            this@LandlordRoomDetailActivity,
                            "Đã cập nhật trạng thái phòng.",
                            Toast.LENGTH_SHORT
                        ).show()

                        loadRoom()
                    }

                    is ApiResult.Error -> {

                        Toast.makeText(
                            this@LandlordRoomDetailActivity,
                            result.exception.message
                                ?: "Không thể cập nhật trạng thái.",
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
                    this@LandlordRoomDetailActivity,
                    e.message
                        ?: "Không thể cập nhật trạng thái.",
                    Toast.LENGTH_LONG
                ).show()

            } finally {

                binding.btnLandlordChangeAvailability
                    .isEnabled =
                    true
            }
        }
    }

    private fun showAvailabilityDialog() {

        val room =
            currentRoom
                ?: return

        val labels =
            arrayOf(
                "Còn trống",
                "Đã giữ chỗ",
                "Đã cho thuê"
            )

        val values =
            arrayOf(
                "AVAILABLE",
                "RESERVED",
                "RENTED"
            )

        val currentIndex =
            values.indexOf(
                room.availabilityStatus
                    .uppercase(
                        Locale.ROOT
                    )
            ).coerceAtLeast(0)

        var selectedIndex =
            currentIndex

        MaterialAlertDialogBuilder(this)
            .setTitle(
                "Trạng thái phòng"
            )
            .setSingleChoiceItems(
                labels,
                currentIndex
            ) { _, which ->

                selectedIndex =
                    which
            }
            .setNegativeButton(
                "Hủy",
                null
            )
            .setPositiveButton(
                "Cập nhật"
            ) { _, _ ->

                val newStatus =
                    values[selectedIndex]

                if (
                    newStatus !=
                    room.availabilityStatus
                        .uppercase(
                            Locale.ROOT
                        )
                ) {
                    updateAvailability(
                        newStatus
                    )
                }
            }
            .show()
    }

    private fun loadRoom() {

        if (isLoading) {
            return
        }

        isLoading =
            true

        showLoading()

        lifecycleScope.launch {

            try {

                when (
                    val result =
                        repository.getRoom(
                            roomId
                        )
                ) {

                    is ApiResult.Success -> {

                        val room =
                            result.data

                        if (room == null) {

                            showError(
                                "Không nhận được thông tin phòng."
                            )

                        } else {

                            showRoom(
                                room
                            )
                        }
                    }

                    is ApiResult.Error -> {

                        showError(
                            result.exception.message
                                ?: "Không thể tải thông tin phòng."
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
                        ?: "Không thể tải thông tin phòng."
                )

            } finally {

                isLoading =
                    false
            }
        }
    }

    private fun showRoom(
        room: RoomResponse
    ) {
        currentRoom = room

        binding.progressRoomDetail.isVisible =
            false

        binding.errorContainer.isVisible =
            false

        binding.scrollContent.isVisible =
            true

        binding.tvRoomTitle.text =
            room.title

        binding.tvRoomNumber.text =
            room.roomNumber
                ?.takeIf {
                    it.isNotBlank()
                }
                ?.let {
                    "Phòng $it"
                }
                ?: "Chưa có số phòng"

        binding.tvRentPrice.text =
            "${formatMoney(
                room.rentPrice
            )}/tháng"

        binding.tvPropertyName.text =
            room.propertyName

        binding.tvArea.text =
            "${room.areaM2
                .stripTrailingZeros()
                .toPlainString()} m²"

        binding.tvMaxOccupants.text =
            "${room.maxOccupants} người"

        binding.tvGender.text =
            genderText(
                room.genderPreference
            )

        binding.tvAvailableFrom.text =
            room.availableFrom
                ?.takeIf {
                    it.isNotBlank()
                }
                ?: "Chưa cập nhật"

        binding.tvDescription.text =
            room.description
                ?.takeIf {
                    it.isNotBlank()
                }
                ?: "Chưa có mô tả."

        binding.tvDeposit.text =
            "Tiền cọc: ${
                formatOptionalMoney(
                    room.depositAmount
                )
            }"

        binding.tvElectricity.text =
            "Tiền điện: ${
                formatOptionalMoney(
                    room.electricityPrice
                )
            }"

        binding.tvWater.text =
            "Tiền nước: ${
                formatOptionalMoney(
                    room.waterPrice
                )
            }"

        binding.tvInternet.text =
            "Internet: ${
                formatOptionalMoney(
                    room.internetPrice
                )
            }"

        binding.tvServiceFee.text =
            "Phí dịch vụ: ${
                formatOptionalMoney(
                    room.serviceFee
                )
            }"

        binding.tvParkingFee.text =
            "Phí gửi xe: ${
                formatOptionalMoney(
                    room.parkingFee
                )
            }"

        binding.tvAmenities.text =
            if (
                room.amenities.isEmpty()
            ) {

                "Chưa có tiện ích"

            } else {

                room.amenities
                    .joinToString(
                        ", "
                    ) {
                        it.amenityName
                    }
            }

        binding.tvViewCount.text =
            "Lượt xem: ${room.viewCount}"

        binding.tvCreatedAt.text =
            "Ngày tạo: ${
                formatDateTime(
                    room.createdAt
                )
            }"

        binding.tvUpdatedAt.text =
            "Cập nhật: ${
                formatDateTime(
                    room.updatedAt
                )
            }"

        showAvailability(
            room.availabilityStatus
        )

        binding.tvListingStatus.text =
            listingStatusText(
                room.listingStatus
            )
        updateListingActions(
            room
        )
    }

    private fun updateListingActions(
        room: RoomResponse
    ) {

        val status =
            room.listingStatus
                .uppercase(
                    Locale.ROOT
                )

        when (status) {

            "DRAFT" -> {

                binding.btnLandlordListingAction.text =
                    "Gửi duyệt"

                binding.btnLandlordListingAction.isEnabled =
                    true

                binding.btnLandlordEditRoom.isEnabled =
                    true
            }

            "REJECTED" -> {

                binding.btnLandlordListingAction.text =
                    "Cần chỉnh sửa trước khi gửi lại"

                binding.btnLandlordListingAction.isEnabled =
                    false

                binding.btnLandlordEditRoom.isEnabled =
                    true
            }

            "PENDING" -> {

                binding.btnLandlordListingAction.text =
                    "Đang chờ duyệt"

                binding.btnLandlordListingAction.isEnabled =
                    false

                binding.btnLandlordEditRoom.isEnabled =
                    false
            }

            "PUBLISHED" -> {

                binding.btnLandlordListingAction.text =
                    "Ẩn tin"

                binding.btnLandlordListingAction.isEnabled =
                    true

                binding.btnLandlordEditRoom.isEnabled =
                    true
            }

            "HIDDEN" -> {

                binding.btnLandlordListingAction.text =
                    "Tin đã ẩn"

                binding.btnLandlordListingAction.isEnabled =
                    false

                binding.btnLandlordEditRoom.isEnabled =
                    true
            }

            else -> {

                binding.btnLandlordListingAction.isEnabled =
                    false

                binding.btnLandlordEditRoom.isEnabled =
                    true
            }
        }
    }

    private fun handleListingAction() {

        val room =
            currentRoom
                ?: return

        when (
            room.listingStatus
                .uppercase(
                    Locale.ROOT
                )
        ) {

            "DRAFT" -> {
                confirmSubmitReview()
            }

            "PUBLISHED" -> {
                confirmHideRoom()
            }

            else -> {
                Unit
            }
        }
    }

    private fun confirmSubmitReview() {

        MaterialAlertDialogBuilder(this)
            .setTitle(
                "Gửi phòng để duyệt?"
            )
            .setMessage(
                "Sau khi gửi, phòng sẽ chuyển sang trạng thái chờ duyệt và tạm thời không thể chỉnh sửa."
            )
            .setNegativeButton(
                "Hủy",
                null
            )
            .setPositiveButton(
                "Gửi duyệt"
            ) { _, _ ->

                submitRoomForReview()
            }
            .show()
    }

    private fun submitRoomForReview() {

        binding.btnLandlordListingAction
            .isEnabled =
            false

        lifecycleScope.launch {

            try {

                when (
                    val result =
                        repository
                            .submitForReview(
                                roomId
                            )
                ) {

                    is ApiResult.Success -> {

                        Toast.makeText(
                            this@LandlordRoomDetailActivity,
                            "Đã gửi phòng để duyệt.",
                            Toast.LENGTH_SHORT
                        ).show()

                        loadRoom()
                    }

                    is ApiResult.Error -> {

                        Toast.makeText(
                            this@LandlordRoomDetailActivity,
                            result.exception.message
                                ?: "Không thể gửi duyệt.",
                            Toast.LENGTH_LONG
                        ).show()

                        currentRoom?.let {
                            updateListingActions(
                                it
                            )
                        }
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
                    this@LandlordRoomDetailActivity,
                    e.message
                        ?: "Không thể gửi duyệt.",
                    Toast.LENGTH_LONG
                ).show()

            }
        }
    }

    private fun confirmHideRoom() {

        MaterialAlertDialogBuilder(this)
            .setTitle(
                "Ẩn tin phòng?"
            )
            .setMessage(
                "Phòng sẽ không còn hiển thị công khai sau khi ẩn."
            )
            .setNegativeButton(
                "Hủy",
                null
            )
            .setPositiveButton(
                "Ẩn tin"
            ) { _, _ ->

                hidePublishedRoom()
            }
            .show()
    }

    private fun hidePublishedRoom() {

        binding.btnLandlordListingAction
            .isEnabled =
            false

        lifecycleScope.launch {

            try {

                when (
                    val result =
                        repository.hideRoom(
                            roomId
                        )
                ) {

                    is ApiResult.Success -> {

                        Toast.makeText(
                            this@LandlordRoomDetailActivity,
                            "Đã ẩn tin phòng.",
                            Toast.LENGTH_SHORT
                        ).show()

                        loadRoom()
                    }

                    is ApiResult.Error -> {

                        Toast.makeText(
                            this@LandlordRoomDetailActivity,
                            result.exception.message
                                ?: "Không thể ẩn tin.",
                            Toast.LENGTH_LONG
                        ).show()

                        currentRoom?.let {
                            updateListingActions(
                                it
                            )
                        }
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
                    this@LandlordRoomDetailActivity,
                    e.message
                        ?: "Không thể ẩn tin.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun showAvailability(
        status: String
    ) {

        val normalized =
            status.uppercase(
                Locale.ROOT
            )

        binding.tvAvailabilityStatus.text =
            when (normalized) {

                "AVAILABLE" ->
                    "Còn trống"

                "RESERVED" ->
                    "Đã giữ chỗ"

                "RENTED" ->
                    "Đã cho thuê"

                else ->
                    status
            }

        val color =
            when (normalized) {

                "AVAILABLE" ->
                    R.color.status_success

                "RENTED" ->
                    R.color.status_error

                else ->
                    R.color.text_muted
            }

        binding.tvAvailabilityStatus
            .setTextColor(
                ContextCompat.getColor(
                    this,
                    color
                )
            )
    }

    private fun listingStatusText(
        status: String
    ): String {

        return when (
            status.uppercase(
                Locale.ROOT
            )
        ) {

            "DRAFT" ->
                "Bản nháp"

            "PENDING" ->
                "Chờ duyệt"

            "PUBLISHED" ->
                "Đã đăng"

            "REJECTED" ->
                "Bị từ chối"

            "HIDDEN" ->
                "Đã ẩn"

            else ->
                status
        }
    }

    private fun genderText(
        value: String
    ): String {

        return when (
            value.uppercase(
                Locale.ROOT
            )
        ) {

            "MALE" ->
                "Nam"

            "FEMALE" ->
                "Nữ"

            else ->
                "Tất cả"
        }
    }

    private fun formatMoney(
        value: Number
    ): String {

        return currencyFormatter
            .format(
                value
            )
    }

    private fun formatOptionalMoney(
        value: Number?
    ): String {

        return value?.let {
            formatMoney(
                it
            )
        } ?: "Không áp dụng"
    }

    private fun formatDateTime(
        value: String
    ): String {

        if (value.isBlank()) {
            return "-"
        }

        val clean =
            value
                .replace(
                    "T",
                    " "
                )
                .substringBefore(".")

        return if (
            clean.length >= 16
        ) {
            clean.take(16)
        } else {
            clean
        }
    }

    private fun showLoading() {

        binding.progressRoomDetail.isVisible =
            true

        binding.scrollContent.isVisible =
            false

        binding.errorContainer.isVisible =
            false
    }

    private fun showError(
        message: String
    ) {

        binding.progressRoomDetail.isVisible =
            false

        binding.scrollContent.isVisible =
            false

        binding.errorContainer.isVisible =
            true

        binding.tvErrorMessage.text =
            message
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

    override fun onResume() {
        super.onResume()

        if (
            ::repository.isInitialized &&
            roomId > 0L
        ) {
            loadRoom()
        }
    }
}