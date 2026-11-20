package com.roomify.app.ui.landlord.contract

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast

import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope

import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.roomify.app.R
import com.roomify.app.data.remote.api.LandlordRentalContractApi
import com.roomify.app.data.remote.common.ApiResult
import com.roomify.app.data.remote.dto.contracts.RentalContractResponse
import com.roomify.app.data.remote.network.RetrofitClient
import com.roomify.app.data.repository.LandlordRentalContractRepository
import com.roomify.app.databinding.ActivityLandlordContractDetailBinding
import com.roomify.app.ui.common.RoleSessionGuard

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

import java.math.BigDecimal
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class LandlordContractDetailActivity :
    AppCompatActivity() {

    companion object {

        const val EXTRA_CONTRACT_ID =
            "extra_landlord_contract_id"
    }

    private lateinit var binding:
            ActivityLandlordContractDetailBinding

    private lateinit var repository:
            LandlordRentalContractRepository

    private var contractId =
        0L

    private var currentContract:
            RentalContractResponse? =
        null

    private var actionLoading =
        false

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(
            savedInstanceState
        )

        binding =
            ActivityLandlordContractDetailBinding
                .inflate(
                    layoutInflater
                )

        setContentView(
            binding.root
        )

        contractId =
            intent.getLongExtra(
                EXTRA_CONTRACT_ID,
                0L
            )

        if (
            contractId <= 0L
        ) {

            Toast.makeText(
                this,
                "Không tìm thấy hợp đồng.",
                Toast.LENGTH_LONG
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
            binding.contractDetailRoot
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
            LandlordRentalContractRepository(
                RetrofitClient.createService(
                    LandlordRentalContractApi::class.java
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
                loadDetail()
            }

        binding.btnComplete
            .setOnClickListener {

                confirmComplete()
            }

        binding.btnCancel
            .setOnClickListener {

                confirmCancel()
            }

        binding.btnOpenContractFile
            .setOnClickListener {

                openContractFile()
            }
    }

    private fun loadDetail() {

        showLoading()

        lifecycleScope.launch {

            try {

                when (
                    val result =
                        repository.getDetail(
                            contractId
                        )
                ) {

                    is ApiResult.Success -> {

                        val data =
                            result.data

                        if (
                            data == null
                        ) {

                            showError(
                                "Không nhận được dữ liệu hợp đồng."
                            )

                        } else {

                            bindContract(
                                data
                            )
                        }
                    }

                    is ApiResult.Error -> {

                        showError(
                            result.exception.message
                                ?: "Không thể tải hợp đồng."
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
                        ?: "Không thể tải hợp đồng."
                )
            }
        }
    }

    private fun bindContract(
        contract:
        RentalContractResponse
    ) {

        currentContract =
            contract

        binding.progressDetail.isVisible =
            false

        binding.errorContainer.isVisible =
            false

        binding.contentScroll.isVisible =
            true

        binding.tvContractCode.text =
            contract.contractCode

        binding.tvStudentName.text =
            contract.studentName

        binding.tvStudentPhone.text =
            contract.studentPhone
                ?.takeIf {
                    it.isNotBlank()
                }
                ?.let {
                    "SĐT: $it"
                }
                ?: "SĐT: Chưa cập nhật"

        binding.tvRoomTitle.text =
            contract.roomTitle

        binding.tvPropertyName.text =
            contract.propertyName

        binding.tvDates.text =
            "${formatDate(contract.startDate)} → " +
                    (
                            contract.endDate
                                ?.let {
                                    formatDate(
                                        it
                                    )
                                }
                                ?: "Không xác định"
                            )

        binding.tvMonthlyRent.text =
            "Tiền thuê: ${formatMoney(contract.monthlyRent)} / tháng"

        binding.tvDeposit.text =
            "Tiền cọc: ${formatMoney(contract.depositAmount)}"

        binding.tvTerms.text =
            contract.terms
                ?.takeIf {
                    it.isNotBlank()
                }
                ?: "Chưa có điều khoản bổ sung."

        binding.btnOpenContractFile.isVisible =
            !contract.contractFileUrl
                .isNullOrBlank()

        bindStatus(
            contract.status
        )
    }

    private fun bindStatus(
        value: String
    ) {

        val status =
            value.uppercase(
                Locale.ROOT
            )

        binding.tvStatus.text =
            when (status) {

                "PENDING" ->
                    "Chờ sinh viên xác nhận"

                "ACTIVE" ->
                    "Đang hiệu lực"

                "COMPLETED" ->
                    "Đã hoàn thành"

                "CANCELLED" ->
                    "Đã hủy"

                else ->
                    status
            }

        val color =
            when (status) {

                "ACTIVE",
                "COMPLETED" ->
                    R.color.status_success

                "CANCELLED" ->
                    R.color.status_error

                else ->
                    R.color.primary_blue
            }

        binding.tvStatus.setTextColor(
            ContextCompat.getColor(
                this,
                color
            )
        )

        binding.actionContainer.isVisible =
            status == "PENDING" ||
                    status == "ACTIVE"

        binding.btnCancel.isVisible =
            status == "PENDING" ||
                    status == "ACTIVE"

        binding.btnComplete.isVisible =
            status == "ACTIVE"
    }

    private fun confirmComplete() {

        MaterialAlertDialogBuilder(
            this
        )
            .setTitle(
                "Hoàn thành hợp đồng?"
            )
            .setMessage(
                "Chỉ có thể hoàn thành khi đã đến hoặc qua ngày kết thúc hợp đồng."
            )
            .setNegativeButton(
                "Không",
                null
            )
            .setPositiveButton(
                "Hoàn thành"
            ) { _, _ ->

                completeContract()
            }
            .show()
    }

    private fun completeContract() {

        setActionLoading(
            true
        )

        lifecycleScope.launch {

            try {

                when (
                    val result =
                        repository.complete(
                            contractId
                        )
                ) {

                    is ApiResult.Success -> {

                        Toast.makeText(
                            this@LandlordContractDetailActivity,
                            "Đã hoàn thành hợp đồng.",
                            Toast.LENGTH_SHORT
                        ).show()

                        result.data
                            ?.let {
                                bindContract(
                                    it
                                )
                            }
                            ?: loadDetail()
                    }

                    is ApiResult.Error -> {

                        Toast.makeText(
                            this@LandlordContractDetailActivity,
                            result.exception.message
                                ?: "Không thể hoàn thành hợp đồng.",
                            Toast.LENGTH_LONG
                        ).show()
                    }

                    ApiResult.Loading ->
                        Unit
                }

            } finally {

                setActionLoading(
                    false
                )
            }
        }
    }

    private fun confirmCancel() {

        MaterialAlertDialogBuilder(
            this
        )
            .setTitle(
                "Hủy hợp đồng?"
            )
            .setMessage(
                "Hành động này sẽ chuyển hợp đồng sang trạng thái đã hủy."
            )
            .setNegativeButton(
                "Không",
                null
            )
            .setPositiveButton(
                "Hủy hợp đồng"
            ) { _, _ ->

                cancelContract()
            }
            .show()
    }

    private fun cancelContract() {

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
                            this@LandlordContractDetailActivity,
                            "Đã hủy hợp đồng.",
                            Toast.LENGTH_SHORT
                        ).show()

                        result.data
                            ?.let {
                                bindContract(
                                    it
                                )
                            }
                            ?: loadDetail()
                    }

                    is ApiResult.Error -> {

                        Toast.makeText(
                            this@LandlordContractDetailActivity,
                            result.exception.message
                                ?: "Không thể hủy hợp đồng.",
                            Toast.LENGTH_LONG
                        ).show()
                    }

                    ApiResult.Loading ->
                        Unit
                }

            } finally {

                setActionLoading(
                    false
                )
            }
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

        runCatching {

            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(
                        url
                    )
                )
            )

        }.onFailure {

            Toast.makeText(
                this,
                "Không thể mở file hợp đồng.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun setActionLoading(
        loading: Boolean
    ) {

        actionLoading =
            loading

        binding.btnComplete.isEnabled =
            !loading

        binding.btnCancel.isEnabled =
            !loading
    }

    private fun formatDate(
        value: String
    ): String {

        return runCatching {

            LocalDate.parse(
                value
            ).format(
                DateTimeFormatter.ofPattern(
                    "dd/MM/yyyy"
                )
            )

        }.getOrElse {
            value
        }
    }

    private fun formatMoney(
        value: BigDecimal
    ): String {

        return NumberFormat
            .getCurrencyInstance(
                Locale(
                    "vi",
                    "VN"
                )
            )
            .format(
                value
            )
    }

    private fun showLoading() {

        binding.progressDetail.isVisible =
            true

        binding.contentScroll.isVisible =
            false

        binding.errorContainer.isVisible =
            false
    }

    private fun showError(
        message: String
    ) {

        binding.progressDetail.isVisible =
            false

        binding.contentScroll.isVisible =
            false

        binding.errorContainer.isVisible =
            true

        binding.tvErrorMessage.text =
            message
    }

    override fun onResume() {
        super.onResume()

        if (
            ::repository.isInitialized &&
            !actionLoading
        ) {

            loadDetail()
        }
    }

    override fun onStart() {
        super.onStart()

        RoleSessionGuard.verify(
            activity =
                this,
            expectedRole =
                "LANDLORD",
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