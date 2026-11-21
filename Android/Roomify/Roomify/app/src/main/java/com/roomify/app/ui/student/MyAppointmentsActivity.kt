package com.roomify.app.ui.student

import android.os.Bundle
import android.widget.Toast

import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager

import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.roomify.app.data.remote.api.ViewingAppointmentApi
import com.roomify.app.data.remote.common.ApiResult
import com.roomify.app.data.remote.dto.appointments.ViewingAppointmentResponse
import com.roomify.app.data.remote.network.RetrofitClient
import com.roomify.app.data.repository.ViewingAppointmentRepository
import com.roomify.app.databinding.ActivityMyAppointmentsBinding

import kotlinx.coroutines.launch

class MyAppointmentsActivity :
    AppCompatActivity() {

    private lateinit var binding:
            ActivityMyAppointmentsBinding

    private lateinit var repository:
            ViewingAppointmentRepository

    private lateinit var adapter:
            MyAppointmentsAdapter

    private var actionRunning =
        false

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(
            savedInstanceState
        )

        binding =
            ActivityMyAppointmentsBinding
                .inflate(
                    layoutInflater
                )

        setContentView(
            binding.root
        )

        setupInsets()
        setupRepository()
        setupRecyclerView()
        setupListeners()

        loadAppointments()
    }

    private fun setupInsets() {

        val root =
            binding.myAppointmentsRoot

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
                ViewingAppointmentApi::class.java
            )

        repository =
            ViewingAppointmentRepository(
                api
            )
    }

    private fun setupRecyclerView() {

        adapter =
            MyAppointmentsAdapter(

                onCancel = {
                    confirmCancel(
                        it
                    )
                },

                onAcceptProposal = {
                    confirmAcceptProposal(
                        it
                    )
                }
            )

        binding.rvAppointments.apply {

            layoutManager =
                LinearLayoutManager(
                    this@MyAppointmentsActivity
                )

            adapter =
                this@MyAppointmentsActivity
                    .adapter
        }
    }

    private fun setupListeners() {

        binding.btnBack
            .setOnClickListener {

                finish()
            }

        binding.btnRetry
            .setOnClickListener {

                loadAppointments()
            }
    }

    private fun loadAppointments() {

        showLoading()

        lifecycleScope.launch {

            when (
                val result =
                    repository
                        .getMyAppointments(
                            page = 1,
                            pageSize = 20
                        )
            ) {

                is ApiResult.Success -> {

                    val data =
                        result.data

                    val items =
                        data?.items
                            .orEmpty()

                    adapter.submitList(
                        items
                    )

                    binding.progressAppointments
                        .isVisible =
                        false

                    binding.tvAppointmentCount.text =
                        "Bạn có ${data?.totalCount ?: items.size} lịch hẹn"

                    if (
                        items.isEmpty()
                    ) {

                        showEmpty()

                    } else {

                        binding.rvAppointments
                            .isVisible =
                            true

                        binding.stateContainer
                            .isVisible =
                            false
                    }
                }

                is ApiResult.Error -> {

                    showError(
                        result.exception.message
                    )
                }

                ApiResult.Loading ->
                    Unit
            }
        }
    }

    private fun confirmCancel(
        item: ViewingAppointmentResponse
    ) {

        MaterialAlertDialogBuilder(
            this
        )
            .setTitle(
                "Hủy lịch xem phòng?"
            )
            .setMessage(
                "Bạn có chắc muốn hủy lịch xem \"${item.roomTitle}\"?"
            )
            .setNegativeButton(
                "Không"
            ) { dialog, _ ->

                dialog.dismiss()
            }
            .setPositiveButton(
                "Hủy lịch"
            ) { _, _ ->

                cancelAppointment(
                    item.appointmentId
                )
            }
            .show()
    }

    private fun cancelAppointment(
        appointmentId: Long
    ) {

        if (actionRunning) {
            return
        }

        actionRunning = true

        binding.progressAppointments
            .isVisible = true

        lifecycleScope.launch {

            when (
                val result =
                    repository.cancel(
                        appointmentId
                    )
            ) {

                is ApiResult.Success -> {

                    Toast.makeText(
                        this@MyAppointmentsActivity,
                        "Đã hủy lịch xem phòng.",
                        Toast.LENGTH_SHORT
                    ).show()

                    actionRunning =
                        false

                    loadAppointments()
                }

                is ApiResult.Error -> {

                    actionRunning =
                        false

                    binding.progressAppointments
                        .isVisible = false

                    showToast(
                        result.exception.message
                    )
                }

                ApiResult.Loading ->
                    Unit
            }
        }
    }

    private fun confirmAcceptProposal(
        item: ViewingAppointmentResponse
    ) {

        MaterialAlertDialogBuilder(
            this
        )
            .setTitle(
                "Chấp nhận giờ mới?"
            )
            .setMessage(
                "Xác nhận sử dụng thời gian mới do chủ trọ đề xuất?"
            )
            .setNegativeButton(
                "Để sau",
                null
            )
            .setPositiveButton(
                "Chấp nhận"
            ) { _, _ ->

                acceptProposal(
                    item.appointmentId
                )
            }
            .show()
    }

    private fun acceptProposal(
        appointmentId: Long
    ) {

        if (actionRunning) {
            return
        }

        actionRunning = true

        binding.progressAppointments
            .isVisible = true

        lifecycleScope.launch {

            when (
                val result =
                    repository
                        .acceptProposal(
                            appointmentId
                        )
            ) {

                is ApiResult.Success -> {

                    Toast.makeText(
                        this@MyAppointmentsActivity,
                        "Đã chấp nhận giờ xem mới.",
                        Toast.LENGTH_SHORT
                    ).show()

                    actionRunning =
                        false

                    loadAppointments()
                }

                is ApiResult.Error -> {

                    actionRunning =
                        false

                    binding.progressAppointments
                        .isVisible = false

                    showToast(
                        result.exception.message
                    )
                }

                ApiResult.Loading ->
                    Unit
            }
        }
    }

    private fun showLoading() {

        binding.progressAppointments
            .isVisible =
            true

        binding.rvAppointments
            .isVisible =
            false

        binding.stateContainer
            .isVisible =
            false

        binding.tvAppointmentCount.text =
            "Đang tải lịch hẹn..."
    }

    private fun showEmpty() {

        binding.progressAppointments
            .isVisible =
            false

        binding.rvAppointments
            .isVisible =
            false

        binding.stateContainer
            .isVisible =
            true

        binding.btnRetry
            .isVisible =
            false

        binding.tvStateMessage.text =
            "Bạn chưa có lịch hẹn xem phòng."
    }

    private fun showError(
        message: String
    ) {

        binding.progressAppointments
            .isVisible =
            false

        binding.rvAppointments
            .isVisible =
            false

        binding.stateContainer
            .isVisible =
            true

        binding.btnRetry
            .isVisible =
            true

        binding.tvStateMessage.text =
            message

        binding.tvAppointmentCount.text =
            "Không thể tải lịch hẹn"
    }

    private fun showToast(
        message: String
    ) {

        Toast.makeText(
            this,
            message,
            Toast.LENGTH_LONG
        ).show()
    }
}