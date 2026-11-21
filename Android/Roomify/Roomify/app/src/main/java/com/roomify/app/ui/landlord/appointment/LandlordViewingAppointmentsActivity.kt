package com.roomify.app.ui.landlord.appointment

import android.os.Bundle
import android.widget.Toast
import android.content.Intent

import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager

import com.roomify.app.data.remote.api.LandlordViewingAppointmentApi
import com.roomify.app.data.remote.common.ApiResult
import com.roomify.app.data.remote.dto.appointments.ViewingAppointmentResponse
import com.roomify.app.data.remote.network.RetrofitClient
import com.roomify.app.data.repository.LandlordViewingAppointmentRepository
import com.roomify.app.databinding.ActivityLandlordViewingAppointmentsBinding
import com.roomify.app.ui.common.RoleSessionGuard

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

class LandlordViewingAppointmentsActivity :
    AppCompatActivity() {

    private lateinit var binding:
            ActivityLandlordViewingAppointmentsBinding

    private lateinit var repository:
            LandlordViewingAppointmentRepository

    private val adapter by lazy {

        LandlordViewingAppointmentAdapter(

            onClick = { appointment ->

                openAppointmentDetail(
                    appointment
                )
            },

            onConfirm = { appointment ->

                openAppointmentDetail(
                    appointment
                )
            },

            onPropose = { appointment ->

                openAppointmentDetail(
                    appointment
                )
            },

            onReject = { appointment ->

                openAppointmentDetail(
                    appointment
                )
            }
        )
    }

    private fun openAppointmentDetail(
        appointment:
        ViewingAppointmentResponse
    ) {

        startActivity(
            Intent(
                this,
                LandlordViewingAppointmentDetailActivity::class.java
            ).apply {

                putExtra(
                    LandlordViewingAppointmentDetailActivity
                        .EXTRA_APPOINTMENT_ID,
                    appointment.appointmentId
                )
            }
        )
    }

    private val loadedAppointments =
        mutableListOf<ViewingAppointmentResponse>()

    private var currentPage =
        1

    private var hasNextPage =
        false

    private var isLoading =
        false

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(
            savedInstanceState
        )

        binding =
            ActivityLandlordViewingAppointmentsBinding
                .inflate(
                    layoutInflater
                )

        setContentView(
            binding.root
        )

        setupInsets()
        setupRepository()
        setupRecyclerView()
        setupActions()
    }

    private fun setupInsets() {

        ViewCompat.setOnApplyWindowInsetsListener(
            binding.landlordAppointmentsRoot
        ) { view, insets ->

            val bars =
                insets.getInsets(
                    WindowInsetsCompat.Type
                        .systemBars()
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
            LandlordViewingAppointmentRepository(
                RetrofitClient.createService(
                    LandlordViewingAppointmentApi::class.java
                )
            )
    }

    private fun setupRecyclerView() {

        binding.rvAppointments.apply {

            layoutManager =
                LinearLayoutManager(
                    this@LandlordViewingAppointmentsActivity
                )

            adapter =
                this@LandlordViewingAppointmentsActivity
                    .adapter
        }
    }

    private fun setupActions() {

        binding.btnBack
            .setOnClickListener {
                finish()
            }

        binding.btnRetry
            .setOnClickListener {

                loadAppointments(
                    reset = true
                )
            }

        binding.btnLoadMore
            .setOnClickListener {

                if (
                    hasNextPage &&
                    !isLoading
                ) {

                    loadAppointments(
                        reset = false
                    )
                }
            }
    }

    private fun loadAppointments(
        reset: Boolean
    ) {

        if (isLoading) {
            return
        }

        if (reset) {

            currentPage =
                1

            loadedAppointments.clear()

            showLoading()

        } else {

            binding.btnLoadMore.isEnabled =
                false

            binding.btnLoadMore.text =
                "Đang tải..."
        }

        isLoading =
            true

        lifecycleScope.launch {

            try {

                when (
                    val result =
                        repository
                            .getAppointments(
                                page =
                                    currentPage,
                                pageSize =
                                    20
                            )
                ) {

                    is ApiResult.Success -> {

                        val response =
                            result.data

                        if (response == null) {

                            showError(
                                "Không nhận được dữ liệu lịch hẹn."
                            )

                            return@launch
                        }

                        loadedAppointments.addAll(
                            response.items
                        )

                        hasNextPage =
                            response.hasNextPage

                        if (
                            loadedAppointments
                                .isEmpty()
                        ) {

                            showEmpty()

                        } else {

                            showAppointments()

                            if (hasNextPage) {
                                currentPage++
                            }
                        }
                    }

                    is ApiResult.Error -> {

                        if (
                            loadedAppointments
                                .isEmpty()
                        ) {

                            showError(
                                result.exception.message
                                    ?: "Không thể tải lịch hẹn."
                            )

                        } else {

                            Toast.makeText(
                                this@LandlordViewingAppointmentsActivity,
                                result.exception.message
                                    ?: "Không thể tải thêm dữ liệu.",
                                Toast.LENGTH_LONG
                            ).show()
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

                if (
                    loadedAppointments
                        .isEmpty()
                ) {

                    showError(
                        e.message
                            ?: "Không thể tải lịch hẹn."
                    )

                } else {

                    Toast.makeText(
                        this@LandlordViewingAppointmentsActivity,
                        e.message
                            ?: "Không thể tải thêm dữ liệu.",
                        Toast.LENGTH_LONG
                    ).show()
                }

            } finally {

                isLoading =
                    false

                binding.btnLoadMore.isEnabled =
                    true

                binding.btnLoadMore.text =
                    "Xem thêm"

                binding.btnLoadMore.isVisible =
                    loadedAppointments
                        .isNotEmpty() &&
                            hasNextPage
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

        binding.emptyContainer
            .isVisible =
            false

        binding.errorContainer
            .isVisible =
            false

        binding.btnLoadMore
            .isVisible =
            false
    }

    private fun showAppointments() {

        binding.progressAppointments
            .isVisible =
            false

        binding.emptyContainer
            .isVisible =
            false

        binding.errorContainer
            .isVisible =
            false

        binding.rvAppointments
            .isVisible =
            true

        adapter.submitList(
            loadedAppointments.toList()
        )

        binding.btnLoadMore
            .isVisible =
            hasNextPage
    }

    private fun showEmpty() {

        binding.progressAppointments
            .isVisible =
            false

        binding.rvAppointments
            .isVisible =
            false

        binding.errorContainer
            .isVisible =
            false

        binding.emptyContainer
            .isVisible =
            true

        binding.btnLoadMore
            .isVisible =
            false

        adapter.submitList(
            emptyList()
        )
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

        binding.emptyContainer
            .isVisible =
            false

        binding.errorContainer
            .isVisible =
            true

        binding.btnLoadMore
            .isVisible =
            false

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
            ::repository.isInitialized
        ) {

            loadAppointments(
                reset = true
            )
        }
    }
}