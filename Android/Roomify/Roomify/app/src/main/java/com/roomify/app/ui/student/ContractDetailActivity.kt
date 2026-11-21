package com.roomify.app.ui.student

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi

import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope

import com.google.android.material.dialog.MaterialAlertDialogBuilder

import com.roomify.app.R
import com.roomify.app.data.remote.api.RentalContractApi
import com.roomify.app.data.remote.common.ApiResult
import com.roomify.app.data.remote.dto.contracts.RentalContractResponse
import com.roomify.app.data.remote.network.RetrofitClient
import com.roomify.app.data.repository.RentalContractRepository
import com.roomify.app.databinding.ActivityContractDetailBinding

import kotlinx.coroutines.launch

import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class ContractDetailActivity :
    AppCompatActivity() {

    companion object {

        const val EXTRA_CONTRACT_ID =
            "extra_contract_id"
    }

    private lateinit var binding:
            ActivityContractDetailBinding

    private lateinit var repository:
            RentalContractRepository

    private var contractId =
        -1L

    private var currentContract:
            RentalContractResponse? =
        null

    private val money =
        NumberFormat.getCurrencyInstance(
            Locale.forLanguageTag("vi-VN")
        )

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(
            savedInstanceState
        )

        binding =
            ActivityContractDetailBinding
                .inflate(
                    layoutInflater
                )

        setContentView(
            binding.root
        )

        contractId =
            intent.getLongExtra(
                EXTRA_CONTRACT_ID,
                -1L
            )

        setupInsets()
        setupRepository()
        setupListeners()

        loadDetail()
    }

    // =========================================================
    // SYSTEM BAR
    // =========================================================

    private fun setupInsets() {

        ViewCompat
            .setOnApplyWindowInsetsListener(
                binding.contractDetailRoot
            ) { view, insets ->

                val bars =
                    insets.getInsets(
                        WindowInsetsCompat
                            .Type
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

    // =========================================================
    // REPOSITORY
    // =========================================================

    private fun setupRepository() {

        RetrofitClient.initialize(
            applicationContext
        )

        repository =
            RentalContractRepository(
                RetrofitClient.createService(
                    RentalContractApi::class.java
                )
            )
    }

    // =========================================================
    // LISTENERS
    // =========================================================

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupListeners() {

        binding.btnReview
            .setOnClickListener {

                val contract =
                    currentContract
                        ?: return@setOnClickListener

                startActivity(
                    Intent(
                        this,
                        CreateReviewActivity::class.java
                    ).apply {

                        putExtra(
                            CreateReviewActivity.EXTRA_CONTRACT_ID,
                            contract.contractId
                        )

                        putExtra(
                            CreateReviewActivity.EXTRA_ROOM_TITLE,
                            contract.roomTitle
                        )
                    }
                )
            }

        // Back
        binding.btnBack
            .setOnClickListener {

                finish()
            }

        // Activate contract
        binding.btnActivate
            .setOnClickListener {

                confirmActivate()
            }

        // Cancel contract
        binding.btnCancel
            .setOnClickListener {

                confirmCancel()
            }

        // Contract file
        binding.btnContractFile
            .setOnClickListener {

                openContractFile()
            }

        // Payments
        binding.btnPayments
            .setOnClickListener {

                startActivity(
                    Intent(
                        this,
                        MyPaymentsActivity::class.java
                    )
                )
            }
    }

    // =========================================================
    // LOAD DETAIL
    // =========================================================

    @RequiresApi(Build.VERSION_CODES.O)
    private fun loadDetail() {

        if (contractId <= 0L) {

            Toast.makeText(
                this,
                "Không tìm thấy hợp đồng.",
                Toast.LENGTH_SHORT
            ).show()

            finish()
            return
        }

        binding.progressDetail
            .isVisible =
            true

        binding.scrollContent
            .isVisible =
            false

        lifecycleScope.launch {

            when (
                val result =
                    repository.getDetail(
                        contractId
                    )
            ) {

                is ApiResult.Success -> {

                    val contract =
                        result.data

                    if (contract == null) {

                        Toast.makeText(
                            this@ContractDetailActivity,
                            "Không nhận được dữ liệu hợp đồng.",
                            Toast.LENGTH_LONG
                        ).show()

                        finish()
                        return@launch
                    }

                    showContract(
                        contract
                    )
                }

                is ApiResult.Error -> {

                    binding.progressDetail
                        .isVisible =
                        false

                    Toast.makeText(
                        this@ContractDetailActivity,
                        result.exception.message,
                        Toast.LENGTH_LONG
                    ).show()

                    finish()
                }

                ApiResult.Loading -> {
                    // Không cần xử lý
                }
            }
        }
    }

    // =========================================================
    // SHOW CONTRACT
    // =========================================================

    @RequiresApi(Build.VERSION_CODES.O)
    private fun showContract(
        contract: RentalContractResponse
    ) {

        currentContract =
            contract

        binding.progressDetail
            .isVisible =
            false

        binding.scrollContent
            .isVisible =
            true

        // Contract code
        binding.tvContractCode.text =
            contract.contractCode

        // Room
        binding.tvRoomTitle.text =
            contract.roomTitle

        binding.tvPropertyName.text =
            contract.propertyName

        // Terms
        binding.tvTerms.text =
            contract.terms
                ?.takeIf {
                    it.isNotBlank()
                }
                ?: "Chưa có điều khoản bổ sung."

        // Main contract information
        binding.tvContractInfo.text =
            buildString {

                append(
                    "Chủ trọ: "
                )

                append(
                    contract.landlordName
                )

                append("\n")

                append(
                    "Sinh viên: "
                )

                append(
                    contract.studentName
                )

                append("\n\n")

                append(
                    "Ngày bắt đầu: "
                )

                append(
                    formatDate(
                        contract.startDate
                    )
                )

                append("\n")

                append(
                    "Ngày kết thúc: "
                )

                append(
                    formatDate(
                        contract.endDate
                    )
                )

                append("\n\n")

                append(
                    "Tiền thuê: "
                )

                append(
                    money.format(
                        contract.monthlyRent
                    )
                )

                append(
                    "/tháng"
                )

                append("\n")

                append(
                    "Tiền cọc: "
                )

                append(
                    money.format(
                        contract.depositAmount
                    )
                )
            }

        showStatus(
            contract
        )

        showActions(
            contract
        )

        showContractFileButton(
            contract
        )
        val normalizedStatus =
            contract.status
                .trim()
                .uppercase(Locale.ROOT)

        binding.btnReview.visibility =
            if (
                normalizedStatus == "ACTIVE" ||
                normalizedStatus == "COMPLETED"
            ) {
                View.VISIBLE
            } else {
                View.GONE
            }
    }

    // =========================================================
    // STATUS
    // =========================================================

    private fun showStatus(
        contract: RentalContractResponse
    ) {

        val status =
            contract.status
                .uppercase(
                    Locale.ROOT
                )

        binding.chipStatus.text =
            when (status) {

                "PENDING" ->
                    "Chờ kích hoạt"

                "ACTIVE" ->
                    "Đang hiệu lực"

                "COMPLETED" ->
                    "Đã hoàn thành"

                "CANCELLED" ->
                    "Đã hủy"

                else ->
                    status
            }

        val colorRes =
            when (status) {

                "ACTIVE",
                "COMPLETED" ->
                    R.color.status_success

                "CANCELLED" ->
                    R.color.status_error

                else ->
                    R.color.primary_blue
            }

        binding.chipStatus
            .setTextColor(
                ContextCompat.getColor(
                    this,
                    colorRes
                )
            )
    }

    // =========================================================
    // ACTION VISIBILITY
    // =========================================================

    private fun showActions(
        contract: RentalContractResponse
    ) {

        val status =
            contract.status
                .trim()
                .uppercase(Locale.ROOT)

        // Review nằm trong phần scroll
        binding.btnReview.visibility =
            if (
                status == "ACTIVE" ||
                status == "COMPLETED"
            ) {
                View.VISIBLE
            } else {
                View.GONE
            }

        // Payment luôn có thể mở để xem danh sách
        binding.btnPayments.visibility =
            View.VISIBLE

        // Chỉ PENDING mới kích hoạt
        binding.btnActivate.visibility =
            if (status == "PENDING") {
                View.VISIBLE
            } else {
                View.GONE
            }

        // PENDING hoặc ACTIVE mới được hủy
        binding.btnCancel.visibility =
            if (
                status == "PENDING" ||
                status == "ACTIVE"
            ) {
                View.VISIBLE
            } else {
                View.GONE
            }

        // Thanh action dưới chỉ phục vụ Activate / Cancel
        binding.actionContainer.visibility =
            if (
                status == "PENDING" ||
                status == "ACTIVE"
            ) {
                View.VISIBLE
            } else {
                View.GONE
            }
    }

    // =========================================================
    // CONTRACT FILE
    // =========================================================

    private fun showContractFileButton(
        contract: RentalContractResponse
    ) {

        binding.btnContractFile.visibility =
            if (
                contract.contractFileUrl
                    .isNullOrBlank()
            ) {

                View.GONE

            } else {

                View.VISIBLE
            }
    }

    private fun openContractFile() {

        val url =
            currentContract
                ?.contractFileUrl
                ?.trim()
                ?.takeIf {
                    it.isNotBlank()
                }
                ?: return

        try {

            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(url)
                )
            )

        } catch (
            exception: Exception
        ) {

            Toast.makeText(
                this,
                "Không thể mở file hợp đồng.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    // =========================================================
    // ACTIVATE
    // =========================================================

    @RequiresApi(Build.VERSION_CODES.O)
    private fun confirmActivate() {

        MaterialAlertDialogBuilder(
            this
        )
            .setTitle(
                "Kích hoạt hợp đồng?"
            )
            .setMessage(
                "Sau khi kích hoạt, phòng sẽ được ghi nhận là đã thuê."
            )
            .setNegativeButton(
                "Để sau",
                null
            )
            .setPositiveButton(
                "Kích hoạt"
            ) { _, _ ->

                activate()
            }
            .show()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun activate() {

        setActionLoading(
            true
        )

        lifecycleScope.launch {

            try {

                when (
                    val result =
                        repository.activate(
                            contractId
                        )
                ) {

                    is ApiResult.Success -> {

                        Toast.makeText(
                            this@ContractDetailActivity,
                            "Hợp đồng đã được kích hoạt.",
                            Toast.LENGTH_SHORT
                        ).show()

                        result.data
                            ?.let {

                                showContract(
                                    it
                                )
                            }
                    }

                    is ApiResult.Error -> {

                        Toast.makeText(
                            this@ContractDetailActivity,
                            result.exception.message,
                            Toast.LENGTH_LONG
                        ).show()
                    }

                    ApiResult.Loading -> {
                        // Không cần xử lý
                    }
                }

            } finally {

                setActionLoading(
                    false
                )
            }
        }
    }

    // =========================================================
    // CANCEL
    // =========================================================

    @RequiresApi(Build.VERSION_CODES.O)
    private fun confirmCancel() {

        MaterialAlertDialogBuilder(
            this
        )
            .setTitle(
                "Hủy hợp đồng?"
            )
            .setMessage(
                "Bạn có chắc muốn hủy hợp đồng này?"
            )
            .setNegativeButton(
                "Không",
                null
            )
            .setPositiveButton(
                "Hủy hợp đồng"
            ) { _, _ ->

                cancel()
            }
            .show()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun cancel() {

        setActionLoading(
            true
        )

        lifecycleScope.launch {

            try {

                when (
                    val result =
                        repository.cancel(
                            contractId
                        )
                ) {

                    is ApiResult.Success -> {

                        Toast.makeText(
                            this@ContractDetailActivity,
                            "Đã hủy hợp đồng.",
                            Toast.LENGTH_SHORT
                        ).show()

                        result.data
                            ?.let {

                                showContract(
                                    it
                                )
                            }
                    }

                    is ApiResult.Error -> {

                        Toast.makeText(
                            this@ContractDetailActivity,
                            result.exception.message,
                            Toast.LENGTH_LONG
                        ).show()
                    }

                    ApiResult.Loading -> {
                        // Không cần xử lý
                    }
                }

            } finally {

                setActionLoading(
                    false
                )
            }
        }
    }

    // =========================================================
    // ACTION LOADING
    // =========================================================

    private fun setActionLoading(
        loading: Boolean
    ) {

        binding.btnActivate
            .isEnabled =
            !loading

        binding.btnCancel
            .isEnabled =
            !loading

        binding.btnPayments
            .isEnabled =
            !loading

        binding.btnContractFile
            .isEnabled =
            !loading
    }

    // =========================================================
    // DATE FORMAT
    // =========================================================

    @RequiresApi(Build.VERSION_CODES.O)
    private fun formatDate(
        value: String?
    ): String {

        if (
            value.isNullOrBlank()
        ) {

            return "Chưa xác định"
        }

        return runCatching {

            LocalDate
                .parse(
                    value
                )
                .format(
                    DateTimeFormatter
                        .ofPattern(
                            "dd/MM/yyyy"
                        )
                )

        }.getOrElse {

            value
        }
    }
}