package com.roomify.app.ui.student

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.annotation.RequiresApi

import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope

import com.roomify.app.data.remote.api.ViewingAppointmentApi
import com.roomify.app.data.remote.common.ApiResult
import com.roomify.app.data.remote.dto.appointments.CreateViewingAppointmentRequest
import com.roomify.app.data.remote.network.RetrofitClient
import com.roomify.app.data.repository.ViewingAppointmentRepository
import com.roomify.app.databinding.ActivityViewingAppointmentBinding

import kotlinx.coroutines.launch

import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class ViewingAppointmentActivity :
    AppCompatActivity() {

    companion object {

        const val EXTRA_ROOM_ID =
            "extra_room_id"

        const val EXTRA_ROOM_TITLE =
            "extra_room_title"

        const val EXTRA_PROPERTY_NAME =
            "extra_property_name"

        const val EXTRA_APPOINTMENT_ID =
            "extra_appointment_id"

        @RequiresApi(Build.VERSION_CODES.O)
        private val APP_ZONE =
            ZoneId.of(
                "Asia/Ho_Chi_Minh"
            )
    }

    private lateinit var binding:
            ActivityViewingAppointmentBinding

    private lateinit var repository:
            ViewingAppointmentRepository

    private var roomId =
        -1L

    private var selectedDate:
            LocalDate? = null

    private var selectedTime:
            LocalTime? = null

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
            ActivityViewingAppointmentBinding
                .inflate(
                    layoutInflater
                )

        setContentView(
            binding.root
        )

        readIntent()
        setupWindowInsets()
        setupRepository()
        setupTimeSlots()
        setupListeners()
    }

    // ========================================================
    // INTENT
    // ========================================================

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

    // ========================================================
    // INSETS
    // ========================================================

    private fun setupWindowInsets() {

        val root =
            binding.viewingAppointmentRoot

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
                ViewingAppointmentApi::class.java
            )

        repository =
            ViewingAppointmentRepository(
                api
            )
    }

    // ========================================================
    // TIME SLOTS
    // ========================================================

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupTimeSlots() {

        val labels =
            mutableListOf<String>()

        var time =
            LocalTime.of(
                8,
                0
            )

        val last =
            LocalTime.of(
                19,
                0
            )

        while (
            !time.isAfter(
                last
            )
        ) {

            labels.add(
                time.format(
                    DateTimeFormatter
                        .ofPattern(
                            "HH:mm"
                        )
                )
            )

            time =
                time.plusMinutes(
                    30
                )
        }

        val adapter =
            ArrayAdapter(
                this,
                android.R.layout
                    .simple_dropdown_item_1line,
                labels
            )

        binding.actTime
            .setAdapter(
                adapter
            )

        binding.actTime
            .setOnItemClickListener { _, _, position, _ ->

                selectedTime =
                    LocalTime.parse(
                        labels[position],
                        DateTimeFormatter
                            .ofPattern(
                                "HH:mm"
                            )
                    )

                binding.tilTime.error =
                    null
            }
    }

    // ========================================================
    // LISTENERS
    // ========================================================

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupListeners() {
        binding.btnMyAppointments
            .setOnClickListener {

                startActivity(
                    Intent(
                        this,
                        MyAppointmentsActivity::class.java
                    )
                )
            }

        binding.btnBack
            .setOnClickListener {

                finish()
            }

        binding.btnSelectDate
            .setOnClickListener {

                openDatePicker()
            }

        binding.btnSubmit
            .setOnClickListener {

                submitAppointment()
            }
    }

    // ========================================================
    // DATE PICKER
    // ========================================================

    @RequiresApi(Build.VERSION_CODES.O)
    private fun openDatePicker() {

        val today =
            ZonedDateTime.now(
                APP_ZONE
            )
                .toLocalDate()

        val initial =
            selectedDate
                ?: today

        val dialog =
            DatePickerDialog(
                this,
                { _,
                  year,
                  month,
                  dayOfMonth ->

                    selectedDate =
                        LocalDate.of(
                            year,
                            month + 1,
                            dayOfMonth
                        )

                    binding.btnSelectDate.text =
                        selectedDate!!
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

        dialog.datePicker.maxDate =
            today
                .plusDays(
                    30
                )
                .atStartOfDay(
                    APP_ZONE
                )
                .toInstant()
                .toEpochMilli()

        dialog.show()
    }

    // ========================================================
    // SUBMIT
    // ========================================================

    @RequiresApi(Build.VERSION_CODES.O)
    private fun submitAppointment() {

        if (isSubmitting) {
            return
        }

        val date =
            selectedDate

        val time =
            selectedTime

        if (date == null) {

            Toast.makeText(
                this,
                "Vui lòng chọn ngày xem phòng.",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        if (time == null) {

            binding.tilTime.error =
                "Vui lòng chọn khung giờ."

            return
        }

        val scheduled =
            ZonedDateTime.of(
                date,
                time,
                APP_ZONE
            )

        val now =
            ZonedDateTime.now(
                APP_ZONE
            )

        if (
            scheduled.isBefore(
                now.plusHours(
                    2
                )
            )
        ) {

            Toast.makeText(
                this,
                "Lịch xem phải cách hiện tại ít nhất 2 giờ.",
                Toast.LENGTH_LONG
            ).show()

            return
        }

        if (
            scheduled.isAfter(
                now.plusDays(
                    30
                )
            )
        ) {

            Toast.makeText(
                this,
                "Chỉ được đặt lịch trong 30 ngày tới.",
                Toast.LENGTH_LONG
            ).show()

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
            CreateViewingAppointmentRequest(

                scheduledAt =
                    scheduled
                        .toOffsetDateTime()
                        .format(
                            DateTimeFormatter
                                .ISO_OFFSET_DATE_TIME
                        ),

                note = note
            )

        createAppointment(
            request
        )
    }

    private fun createAppointment(
        request: CreateViewingAppointmentRequest
    ) {

        isSubmitting = true

        binding.btnSubmit.isEnabled =
            false

        binding.btnSubmit.text =
            "Đang gửi..."

        lifecycleScope.launch {

            try {

                when (
                    val result =
                        repository.create(
                            roomId = roomId,
                            request = request
                        )
                ) {

                    is ApiResult.Success -> {

                        val appointment =
                            result.data

                        if (appointment == null) {

                            showSubmitError(
                                "Không nhận được thông tin lịch hẹn."
                            )

                            return@launch
                        }

                        Toast.makeText(
                            this@ViewingAppointmentActivity,
                            "Đã gửi yêu cầu đặt lịch xem phòng.",
                            Toast.LENGTH_LONG
                        ).show()

                        startActivity(
                            Intent(
                                this@ViewingAppointmentActivity,
                                MyAppointmentsActivity::class.java
                            )
                        )

                        finish()
                    }

                    is ApiResult.Error -> {

                        showSubmitError(
                            result.exception.message
                        )
                    }

                    ApiResult.Loading -> {
                        // Không cần xử lý
                    }
                }

            } finally {

                isSubmitting = false

                binding.btnSubmit.isEnabled =
                    true

                binding.btnSubmit.text =
                    "Gửi yêu cầu đặt lịch"
            }
        }
    }
    private fun showSubmitError(
        message: String
    ) {

        Toast.makeText(
            this,
            message,
            Toast.LENGTH_LONG
        ).show()
    }
}