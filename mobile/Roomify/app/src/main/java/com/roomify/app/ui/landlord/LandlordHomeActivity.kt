package com.roomify.app.ui.landlord

import android.content.Intent
import android.os.Bundle
import android.widget.Toast

import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope

import com.roomify.app.R
import com.roomify.app.data.local.TokenManager
import com.roomify.app.databinding.ActivityLandlordHomeBinding
import com.roomify.app.ui.common.LogoutHandler
import com.roomify.app.ui.common.RoleSessionGuard
import com.roomify.app.ui.landlord.property.LandlordPropertiesActivity
import com.roomify.app.ui.landlord.appointment.LandlordViewingAppointmentsActivity
import com.roomify.app.ui.landlord.rental.LandlordRentalRequestsActivity
import com.roomify.app.ui.landlord.contract.LandlordContractsActivity

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

class LandlordHomeActivity : AppCompatActivity() {

    private lateinit var binding:
            ActivityLandlordHomeBinding

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        binding =
            ActivityLandlordHomeBinding.inflate(
                layoutInflater
            )

        setContentView(
            binding.root
        )

        setupInsets()
        setupNavigation()
        setupActions()
        loadLandlordName()
    }

    // ========================================================
    // INSETS
    // ========================================================

    private fun setupInsets() {

        ViewCompat.setOnApplyWindowInsetsListener(
            binding.landlordHomeRoot
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

    // ========================================================
    // BOTTOM NAVIGATION
    // ========================================================

    private fun setupNavigation() {

        binding.bottomNavigationLandlord.selectedItemId =
            R.id.navLandlordHome

        binding.bottomNavigationLandlord
            .setOnItemSelectedListener { item ->

                when (item.itemId) {

                    R.id.navLandlordHome -> {
                        true
                    }

                    R.id.navLandlordProperties -> {

                        startActivity(
                            Intent(
                                this,
                                LandlordPropertiesActivity::class.java
                            )
                        )

                        false
                    }

                    R.id.navLandlordRequests -> {

                        startActivity(
                            Intent(
                                this,
                                LandlordRentalRequestsActivity::class.java
                            )
                        )

                        false
                    }

                    else -> {
                        false
                    }
                }
            }
    }

    // ========================================================
    // ACTIONS
    // ========================================================

    private fun setupActions() {

        binding.cardRentalRequests
            .setOnClickListener {

                startActivity(
                    Intent(
                        this,
                        LandlordRentalRequestsActivity::class.java
                    )
                )
            }

        binding.cardContractsPayments
            .setOnClickListener {

                startActivity(
                    Intent(
                        this,
                        LandlordContractsActivity::class.java
                    )
                )
            }

        binding.btnLogout
            .setOnClickListener {

                LogoutHandler.logout(
                    activity = this,
                    logoutButton =
                        binding.btnLogout
                )
            }

        binding.cardAppointments
            .setOnClickListener {

                startActivity(
                    Intent(
                        this,
                        LandlordViewingAppointmentsActivity::class.java
                    )
                )
            }

        // ----------------------------------------------------
        // PROPERTY MANAGEMENT
        // Dashboard -> Khu trọ -> Property Detail
        // ----------------------------------------------------

        binding.cardProperties
            .setOnClickListener {

                startActivity(
                    Intent(
                        this,
                        LandlordPropertiesActivity::class.java
                    )
                )
            }

        // ----------------------------------------------------
        // ROOM MANAGEMENT
        // Dashboard -> Phòng trọ
        // -> chọn khu trọ
        // -> danh sách phòng
        // ----------------------------------------------------

        binding.cardRooms
            .setOnClickListener {

                startActivity(
                    Intent(
                        this,
                        LandlordPropertiesActivity::class.java
                    ).apply {

                        putExtra(
                            LandlordPropertiesActivity
                                .EXTRA_SELECT_FOR_ROOMS,
                            true
                        )
                    }
                )
            }
    }

    // ========================================================
    // LANDLORD NAME
    // ========================================================

    private fun loadLandlordName() {

        lifecycleScope.launch {

            try {

                val session =
                    TokenManager(
                        applicationContext
                    ).getSession()

                val fullName =
                    session.fullName
                        ?.trim()
                        ?.takeIf {
                            it.isNotBlank()
                        }

                binding.tvWelcome.text =
                    if (fullName != null) {

                        "Xin chào, $fullName!"

                    } else {

                        "Xin chào, Chủ trọ!"
                    }

            } catch (
                e: CancellationException
            ) {

                throw e

            } catch (
                _: Exception
            ) {

                binding.tvWelcome.text =
                    "Xin chào, Chủ trọ!"
            }
        }
    }

    // ========================================================
    // SESSION GUARD
    // ========================================================

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