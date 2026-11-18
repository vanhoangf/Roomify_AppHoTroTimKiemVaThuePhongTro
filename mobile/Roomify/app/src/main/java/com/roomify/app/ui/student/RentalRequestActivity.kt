package com.roomify.app.ui.student

import android.content.Intent
import android.app.DatePickerDialog
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.RequiresApi

import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope

import com.roomify.app.data.remote.api.RentalRequestApi
import com.roomify.app.data.remote.common.ApiResult
import com.roomify.app.data.remote.dto.rentals.CreateRentalRequestRequest
import com.roomify.app.data.remote.network.RetrofitClient
import com.roomify.app.data.repository.RentalRequestRepository
import com.roomify.app.databinding.ActivityRentalRequestBinding

import kotlinx.coroutines.launch

import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class RentalRequestActivity :
    AppCompatActivity() {

    companion object {

        const val EXTRA_ROOM_ID =
            "extra_room_id"

        const val EXTRA_ROOM_TITLE =
            "extra_room_title"

        const val EXTRA_PROPERTY_NAME =
            "extra_property_name"

        @RequiresApi(Build.VERSION_CODES.O)
        private val APP_ZONE =
            ZoneId.of(
                "Asia/Ho_Chi_Minh"
            )
    }

    private lateinit var binding:
            ActivityRentalRequestBinding

    private lateinit var repository:
            RentalRequestRepository

    private var roomId =
        -1L

    private var selectedMoveInDate:
            LocalDate? =
        null

    private var isSubmitting =
        false

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(
            savedInstanceState
        )

        binding =
            ActivityRentalRequestBinding
                .inflate(
                    layoutInflater
                )

        setContentView(
            binding.root
        )

        readIntent()
        setupInsets()
        setupRepository()
        setupListeners()
    }

    private fun readIntent() {

        roomId =
            intent.getLongExtra(
                EXTRA_ROOM_ID,
                -1L
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

        binding.tvRoomTitle.text =
            intent.getStringExtra(
                EXTRA_ROOM_TITLE
            )
                ?.takeIf {
                    it.isNotBlank()
                }
                ?: "Phòng Roomify"

        binding.tvPropertyName.text =
            intent.getStringExtra(
                EXTRA_PROPERTY_NAME
            )
                ?.takeIf {
                    it.isNotBlank()
                }
                ?: "Thông tin khu trọ"
    }

    private fun setupInsets() {

        val root =
            binding.rentalRequestRoot

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

        val api =
            RetrofitClient.createService(
                RentalRequestApi::class.java
            )

        repository =
            RentalRequestRepository(
                api
            )
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupListeners() {

        binding.btnMyRentalRequests
            .setOnClickListener {

                startActivity(
                    Intent(
                        this,
                        MyRentalRequestsActivity::class.java
                    )
                )
            }

        binding.btnBack
            .setOnClickListener {

                finish()
            }

        binding.btnSelectMoveInDate
            .setOnClickListener {

                openDatePicker()
            }

        binding.btnSubmit
            .setOnClickListener {

                submitRentalRequest()
            }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun openDatePicker() {

        val today =
            LocalDate.now(
                APP_ZONE
            )

        val initial =
            selectedMoveInDate
                ?: today

        val dialog =
            DatePickerDialog(
                this,
                {
                        _,
                        year,
                        month,
                        day ->

                    selectedMoveInDate =
                        LocalDate.of(
                            year,
                            month + 1,
                            day
                        )

                    binding
                        .btnSelectMoveInDate
                        .text =
                        selectedMoveInDate!!
                            .format(
                                DateTimeFormatter
                                    .ofPattern(
                                        "dd/MM/yyyy"
                                    )
                            )
                },
                initial.year,
                initial.monthValue - 1,
                initial.dayOfMonth
            )

        dialog.datePicker.minDate =
            today
                .atStartOfDay(
                    APP_ZONE
                )
                .toInstant()
                .toEpochMilli()

        dialog.show()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun submitRentalRequest() {

        if (isSubmitting) {
            return
        }

        binding.tilDuration.error =
            null

        val moveInDate =
            selectedMoveInDate

        if (moveInDate == null) {

            Toast.makeText(
                this,
                "Vui lòng chọn ngày dự kiến chuyển vào.",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        val durationText =
            binding.edtDuration.text
                ?.toString()
                ?.trim()
                .orEmpty()

        val duration =
            durationText
                .takeIf {
                    it.isNotBlank()
                }
                ?.toIntOrNull()

        if (
            durationText.isNotBlank() &&
            duration == null
        ) {

            binding.tilDuration.error =
                "Thời hạn không hợp lệ."

            return
        }

        if (
            duration != null &&
            duration !in 1..120
        ) {

            binding.tilDuration.error =
                "Thời hạn phải từ 1 đến 120 tháng."

            return
        }

        val note =
            binding.edtNote.text
                ?.toString()
                ?.trim()
                ?.takeIf {
                    it.isNotBlank()
                }

        val request =
            CreateRentalRequestRequest(

                requestedMoveInDate =
                    moveInDate.format(
                        DateTimeFormatter
                            .ISO_LOCAL_DATE
                    ),

                durationMonths =
                    duration,

                note =
                    note
            )

        createRentalRequest(
            request
        )
    }

    private fun createRentalRequest(
        request:
        CreateRentalRequestRequest
    ) {

        isSubmitting =
            true

        binding.btnSubmit.isEnabled =
            false

        binding.btnSubmit.text =
            "Đang gửi..."

        lifecycleScope.launch {

            try {

                when (
                    val result =
                        repository.create(
                            roomId =
                                roomId,
                            request =
                                request
                        )
                ) {

                    is ApiResult.Success -> {

                        Toast.makeText(
                            this@RentalRequestActivity,
                            "Đã gửi yêu cầu thuê phòng.",
                            Toast.LENGTH_LONG
                        ).show()

                        startActivity(
                            Intent(
                                this@RentalRequestActivity,
                                MyRentalRequestsActivity::class.java
                            )
                        )

                        finish()
                    }

                    is ApiResult.Error -> {

                        Toast.makeText(
                            this@RentalRequestActivity,
                            result.exception.message,
                            Toast.LENGTH_LONG
                        ).show()
                    }

                    ApiResult.Loading -> Unit
                }

            } finally {

                isSubmitting =
                    false

                binding.btnSubmit.isEnabled =
                    true

                binding.btnSubmit.text =
                    "Gửi yêu cầu thuê"
            }
        }
    }
}