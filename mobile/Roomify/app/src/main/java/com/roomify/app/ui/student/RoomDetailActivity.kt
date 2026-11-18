package com.roomify.app.ui.student

import android.os.Bundle
import android.view.View

import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope

import com.bumptech.glide.Glide
import com.google.android.material.chip.Chip

import com.roomify.app.R
import com.roomify.app.data.remote.api.RoomApi
import com.roomify.app.data.remote.common.ApiResult
import com.roomify.app.data.remote.dto.rooms.RoomDetailResponse
import com.roomify.app.data.remote.network.RetrofitClient
import com.roomify.app.data.repository.RoomRepository
import com.roomify.app.databinding.ActivityRoomDetailBinding

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Locale
import android.content.Intent
import android.widget.Toast
import com.roomify.app.data.remote.api.SavedRoomApi
import com.roomify.app.data.repository.SavedRoomRepository
import com.roomify.app.data.remote.api.MessagingApi
import com.roomify.app.data.repository.MessagingRepository
import android.content.res.ColorStateList

class RoomDetailActivity :
    AppCompatActivity() {

    companion object {

        const val EXTRA_ROOM_ID =
            "extra_room_id"
    }

    private fun showSavedState(
        isSaved: Boolean
    ) {

        binding.btnSaveRoom.apply {

            text = ""

            contentDescription =
                if (isSaved) {
                    "Bỏ lưu phòng"
                } else {
                    "Lưu phòng"
                }

            setIconResource(
                if (isSaved) {
                    R.drawable.ic_favorite_filled
                } else {
                    R.drawable.ic_favorite_border
                }
            )

            iconTint =
                ColorStateList.valueOf(
                    ContextCompat.getColor(
                        this@RoomDetailActivity,
                        if (isSaved) {
                            R.color.status_error
                        } else {
                            R.color.primary_blue
                        }
                    )
                )
        }
    }

    // ========================================================
// OPEN IMAGE GALLERY
// ========================================================
    private fun openGallery() {

        if (
            currentImageUrls.isEmpty()
        ) {

            Toast.makeText(
                this,
                "Phòng này chưa có ảnh.",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        val intent =
            Intent(
                this,
                RoomGalleryActivity::class.java
            )

        intent.putStringArrayListExtra(
            RoomGalleryActivity
                .EXTRA_IMAGE_URLS,
            ArrayList(
                currentImageUrls
            )
        )

        intent.putExtra(
            RoomGalleryActivity
                .EXTRA_START_POSITION,
            0
        )

        startActivity(
            intent
        )
    }

    private lateinit var binding:
            ActivityRoomDetailBinding

    private lateinit var roomRepository:
            RoomRepository

    private lateinit var savedRoomRepository:
            SavedRoomRepository

    private var isRoomSaved =
        false

    private var isSavingRoom =
        false

    private var roomId =
        -1L

    private var currentRoom:
            RoomDetailResponse? = null

    private var currentImageUrls:
            List<String> =
        emptyList()

    private val moneyFormatter =
        NumberFormat.getCurrencyInstance(
            Locale.forLanguageTag(
                "vi-VN"
            )
        )



    // ========================================================
    // WINDOW INSETS
    // ========================================================

    private fun setupWindowInsets() {

        val root =
            binding.roomDetailRoot

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

    private lateinit var messagingRepository:
            MessagingRepository
    private fun setupRepository() {
        messagingRepository =
            MessagingRepository(
                RetrofitClient.createService(
                    MessagingApi::class.java
                )
            )

        RetrofitClient.initialize(
            applicationContext
        )

        val api =
            RetrofitClient.createService(
                RoomApi::class.java
            )

        roomRepository =
            RoomRepository(
                api
            )

        savedRoomRepository =
            SavedRoomRepository(
                RetrofitClient.createService(
                    SavedRoomApi::class.java
                )
            )
    }

    // ========================================================
    // LISTENERS
    // ========================================================

    private fun openViewingAppointment() {

        val room =
            currentRoom
                ?: return

        if (
            !room.availabilityStatus.equals(
                "AVAILABLE",
                ignoreCase = true
            )
        ) {
            return
        }

        val intent =
            Intent(
                this,
                ViewingAppointmentActivity::class.java
            )

        intent.putExtra(
            ViewingAppointmentActivity
                .EXTRA_ROOM_ID,
            room.roomId
        )

        intent.putExtra(
            ViewingAppointmentActivity
                .EXTRA_ROOM_TITLE,
            room.title
        )

        intent.putExtra(
            ViewingAppointmentActivity
                .EXTRA_PROPERTY_NAME,
            room.propertyName
        )

        startActivity(
            intent
        )
    }
    private fun openRentalRequest() {

        val room =
            currentRoom
                ?: return

        if (
            !room.availabilityStatus.equals(
                "AVAILABLE",
                ignoreCase = true
            )
        ) {
            return
        }

        val intent =
            android.content.Intent(
                this,
                RentalRequestActivity::class.java
            )

        intent.putExtra(
            RentalRequestActivity.EXTRA_ROOM_ID,
            room.roomId
        )

        intent.putExtra(
            RentalRequestActivity.EXTRA_ROOM_TITLE,
            room.title
        )

        intent.putExtra(
            RentalRequestActivity.EXTRA_PROPERTY_NAME,
            room.propertyName
        )

        startActivity(
            intent
        )
    }
    private fun openConversation() {

        val room =
            currentRoom
                ?: return

        binding.btnMessage.isEnabled =
            false

        lifecycleScope.launch {

            try {

                when (
                    val result =
                        messagingRepository
                            .getOrCreateConversation(
                                room.roomId
                            )
                ) {

                    is ApiResult.Success -> {

                        val conversation =
                            result.data
                                ?: return@launch

                        startActivity(
                            Intent(
                                this@RoomDetailActivity,
                                ChatActivity::class.java
                            ).apply {

                                putExtra(
                                    ChatActivity.EXTRA_CONVERSATION_ID,
                                    conversation.conversationId
                                )
                            }
                        )
                    }

                    is ApiResult.Error -> {

                        Toast.makeText(
                            this@RoomDetailActivity,
                            result.exception.message,
                            Toast.LENGTH_LONG
                        ).show()
                    }

                    ApiResult.Loading ->
                        Unit
                }

            } finally {

                binding.btnMessage.isEnabled =
                    true
            }
        }
    }
    private fun setupListeners() {
        binding.btnMessage
            .setOnClickListener {

                openConversation()
            }

        binding.btnSaveRoom
            .setOnClickListener {

                toggleSavedRoom()
            }


        binding.btnRentalRequest
            .setOnClickListener {

                openRentalRequest()
            }
        binding.btnViewingAppointment
            .setOnClickListener {

                openViewingAppointment()
            }

        binding.btnBack
            .setOnClickListener {

                finish()
            }

        binding.btnRetry
            .setOnClickListener {

                loadRoomDetail()
            }

        binding.ivRoomImage
            .setOnClickListener {

                openGallery()
            }

        binding.tvImageCount
            .setOnClickListener {

                openGallery()
            }
    }

    // ========================================================
    // LOAD DETAIL
    // ========================================================

    private fun loadRoomDetail() {

        showLoading()

        lifecycleScope.launch {

            try {

                when (
                    val result =
                        roomRepository
                            .getRoomDetail(
                                roomId
                            )
                ) {

                    is ApiResult.Success -> {

                        val room =
                            result.data

                        if (room == null) {

                            showError(
                                "Không tìm thấy thông tin phòng."
                            )

                        } else {

                            showRoom(
                                room
                            )
                        }
                    }

                    is ApiResult.Error -> {

                        showError(
                            "Không thể tải chi tiết phòng.\n" +
                                    "Vui lòng thử lại."
                        )
                    }

                    ApiResult.Loading -> Unit
                }

            } catch (
                e: CancellationException
            ) {

                throw e

            } catch (_: Exception) {

                showError(
                    "Đã xảy ra lỗi khi tải chi tiết phòng."
                )
            }
        }
    }

    // ========================================================
    // SHOW ROOM
    // ========================================================

    private fun checkSavedState() {

        lifecycleScope.launch {

            when (
                val result =
                    savedRoomRepository
                        .getSavedRooms()
            ) {

                is ApiResult.Success -> {

                    isRoomSaved =
                        result.data
                            .orEmpty()
                            .any {

                                it.roomId ==
                                        roomId
                            }

                    updateSaveButton()
                }

                is ApiResult.Error -> {

                    isRoomSaved =
                        false

                    updateSaveButton()
                }

                ApiResult.Loading ->
                    Unit
            }
        }
    }

    private fun toggleSavedRoom() {

        if (isSavingRoom) {
            return
        }

        val room =
            currentRoom
                ?: return

        isSavingRoom =
            true

        binding.btnSaveRoom
            .isEnabled =
            false

        lifecycleScope.launch {

            try {

                if (isRoomSaved) {

                    when (
                        val result =
                            savedRoomRepository
                                .deleteSavedRoom(
                                    room.roomId
                                )
                    ) {

                        is ApiResult.Success -> {

                            isRoomSaved =
                                false

                            Toast.makeText(
                                this@RoomDetailActivity,
                                "Đã bỏ lưu phòng.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        is ApiResult.Error -> {

                            Toast.makeText(
                                this@RoomDetailActivity,
                                result.exception.message,
                                Toast.LENGTH_LONG
                            ).show()
                        }

                        ApiResult.Loading ->
                            Unit
                    }

                } else {

                    when (
                        val result =
                            savedRoomRepository
                                .saveRoom(
                                    room.roomId
                                )
                    ) {

                        is ApiResult.Success -> {

                            isRoomSaved =
                                true

                            Toast.makeText(
                                this@RoomDetailActivity,
                                "Đã lưu phòng.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        is ApiResult.Error -> {

                            Toast.makeText(
                                this@RoomDetailActivity,
                                result.exception.message,
                                Toast.LENGTH_LONG
                            ).show()
                        }

                        ApiResult.Loading ->
                            Unit
                    }
                }

            } finally {

                isSavingRoom =
                    false

                updateSaveButton()
            }
        }
    }

    private fun updateSaveButton() {

        binding.btnSaveRoom
            .isEnabled =
            !isSavingRoom

        showSavedState(
            isRoomSaved
        )
    }
    private fun showRoom(
        room: RoomDetailResponse
    ) {
        currentRoom =
            room


        binding.progressDetail
            .isVisible = false

        binding.errorContainer
            .isVisible = false

        binding.scrollContent
            .isVisible = true

        // ----------------------------------------------------
        // IMAGE
        // ----------------------------------------------------

        currentImageUrls =
            room.images
                .mapNotNull { image ->

                    val url =
                        image.imageUrl
                            .trim()

                    url.takeIf {

                        it.startsWith(
                            "https://"
                        ) ||
                                it.startsWith(
                                    "http://"
                                )
                    }
                }

        val primaryImage =
            currentImageUrls
                .firstOrNull()

        Glide.with(
            binding.ivRoomImage
        )
            .load(
                primaryImage
            )
            .placeholder(
                R.drawable
                    .room_image_placeholder
            )
            .error(
                R.drawable
                    .room_image_placeholder
            )
            .centerCrop()
            .into(
                binding.ivRoomImage
            )

        binding.tvImageCount.text =
            when (
                currentImageUrls.size
            ) {

                0 ->
                    "Chưa có ảnh"

                1 ->
                    "1 ảnh"

                else ->
                    "${currentImageUrls.size} ảnh"
            }

        // ----------------------------------------------------
        // MAIN
        // ----------------------------------------------------

        binding.tvRoomTitle.text =
            room.title

        binding.tvRoomPrice.text =
            "${formatMoney(
                room.rentPrice
            )}/tháng"

        binding.tvAddress.text =
            listOf(
                room.addressLine,
                room.wardName,
                room.provinceName
            )
                .filter {
                    it.isNotBlank()
                }
                .joinToString(
                    ", "
                )

        showStatus(
            room.availabilityStatus
        )

        // ----------------------------------------------------
        // QUICK INFO
        // ----------------------------------------------------

        binding.tvArea.text =
            "${room.areaM2
                .stripTrailingZeros()
                .toPlainString()} m²"

        binding.tvOccupants.text =
            "${room.maxOccupants} người"

        binding.tvPropertyType.text =
            room.propertyTypeName

        // ----------------------------------------------------
        // DESCRIPTION
        // ----------------------------------------------------

        binding.tvDescription.text =
            room.description
                ?.trim()
                ?.takeIf {
                    it.isNotBlank()
                }
                ?: "Chủ trọ chưa thêm mô tả cho phòng này."

        // ----------------------------------------------------
        // COSTS
        // ----------------------------------------------------

        binding.tvDeposit.text =
            formatMoneyOrUpdating(
                room.depositAmount
            )

        binding.tvElectricity.text =
            formatMoneyOrUpdating(
                room.electricityPrice
            )

        binding.tvWater.text =
            formatMoneyOrUpdating(
                room.waterPrice
            )

        binding.tvInternet.text =
            formatMoneyOrUpdating(
                room.internetPrice
            )

        binding.tvServiceFee.text =
            formatMoneyOrUpdating(
                room.serviceFee
            )

        binding.tvParkingFee.text =
            formatMoneyOrUpdating(
                room.parkingFee
            )

        // ----------------------------------------------------
        // AMENITIES
        // ----------------------------------------------------

        showAmenities(
            room
        )

        // ----------------------------------------------------
        // PROPERTY
        // ----------------------------------------------------

        binding.tvPropertyName.text =
            room.propertyName

        binding.tvPropertyDescription.text =
            room.propertyDescription
                ?.trim()
                ?.takeIf {
                    it.isNotBlank()
                }
                ?: "Chưa có mô tả khu trọ."

        binding.tvVerified.text =
            if (
                room.landlordIsVerified
            ) {

                "✓ Chủ trọ đã được xác minh"

            } else {

                "Chủ trọ chưa được xác minh"
            }

        binding.tvVerified
            .setTextColor(
                ContextCompat.getColor(
                    this,
                    if (
                        room.landlordIsVerified
                    ) {
                        R.color.status_success
                    } else {
                        R.color.text_muted
                    }
                )
            )

        binding.tvViewCount.text =
            "${room.viewCount} lượt xem"

        /*
         * CTA nghiệp vụ sẽ nối ở bước riêng.
         * Không bật button giả khi chưa có workflow.
         */

        binding.btnCall.isEnabled =
            false

        binding.btnMessage.isEnabled =
            true

        binding.btnViewingAppointment
            .isEnabled =
            room.availabilityStatus.equals(
                "AVAILABLE",
                ignoreCase = true
            )

        binding.btnRentalRequest
            .isEnabled =
            room.availabilityStatus.equals(
                "AVAILABLE",
                ignoreCase = true
            )
        checkSavedState()
    }

    // ========================================================
    // STATUS
    // ========================================================

    private fun showStatus(
        status: String
    ) {

        val normalized =
            status.uppercase(
                Locale.ROOT
            )

        val text =
            when (normalized) {

                "AVAILABLE" ->
                    "Còn trống"

                "RESERVED" ->
                    "Đã giữ chỗ"

                "RENTED" ->
                    "Đã cho thuê"

                else ->
                    "Đang cập nhật"
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

        binding.chipRoomStatus.text =
            text

        binding.chipRoomStatus
            .setTextColor(
                ContextCompat.getColor(
                    this,
                    color
                )
            )
    }

    // ========================================================
    // AMENITIES
    // ========================================================

    private fun showAmenities(
        room: RoomDetailResponse
    ) {

        binding.chipGroupAmenities
            .removeAllViews()

        if (
            room.amenities.isEmpty()
        ) {

            val chip =
                Chip(this).apply {

                    id =
                        View.generateViewId()

                    text =
                        "Chưa cập nhật tiện ích"

                    isCheckable =
                        false

                    isClickable =
                        false
                }

            binding.chipGroupAmenities
                .addView(
                    chip
                )

            return
        }

        room.amenities
            .forEach { amenity ->

                val chip =
                    Chip(this).apply {

                        id =
                            View.generateViewId()

                        text =
                            amenity.amenityName

                        isCheckable =
                            false

                        isClickable =
                            false

                        setChipBackgroundColorResource(
                            R.color.chip_bg
                        )
                    }

                binding.chipGroupAmenities
                    .addView(
                        chip
                    )
            }
    }

    // ========================================================
    // MONEY
    // ========================================================

    private fun formatMoney(
        value: BigDecimal
    ): String {

        return moneyFormatter
            .format(
                value
            )
    }

    private fun formatMoneyOrUpdating(
        value: BigDecimal?
    ): String {

        return value?.let {

            moneyFormatter
                .format(
                    it
                )

        } ?: "Chưa cập nhật"
    }

    // ========================================================
    // UI STATE
    // ========================================================

    private fun showLoading() {

        binding.progressDetail
            .isVisible = true

        binding.scrollContent
            .isVisible = false

        binding.errorContainer
            .isVisible = false
    }

    private fun showError(
        message: String
    ) {

        binding.progressDetail
            .isVisible = false

        binding.scrollContent
            .isVisible = false

        binding.errorContainer
            .isVisible = true

        binding.tvError.text =
            message
    }

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(
            savedInstanceState
        )

        binding =
            ActivityRoomDetailBinding
                .inflate(
                    layoutInflater
                )

        setContentView(
            binding.root
        )

        setupWindowInsets()
        setupRepository()
        setupListeners()

        roomId =
            intent.getLongExtra(
                EXTRA_ROOM_ID,
                -1L
            )

        if (roomId <= 0) {

            showError(
                "Không tìm thấy phòng."
            )

            return
        }

        loadRoomDetail()
    }
}