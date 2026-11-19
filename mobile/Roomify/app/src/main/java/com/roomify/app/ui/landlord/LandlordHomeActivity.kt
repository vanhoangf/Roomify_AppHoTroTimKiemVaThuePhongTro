package com.roomify.app.ui.landlord

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
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

class LandlordHomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLandlordHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLandlordHomeBinding.inflate(
            layoutInflater
        )

        setContentView(binding.root)

        setupInsets()
        setupNavigation()
        setupActions()
        loadLandlordName()
    }

    private fun setupInsets() {

        ViewCompat.setOnApplyWindowInsetsListener(
            binding.landlordHomeRoot
        ) { view, insets ->

            val bars = insets.getInsets(
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

    private fun setupNavigation() {

        binding.bottomNavigationLandlord.selectedItemId =
            R.id.navLandlordHome

        binding.bottomNavigationLandlord
            .setOnItemSelectedListener { item ->

                item.itemId == R.id.navLandlordHome
            }
    }

    private fun setupActions() {

        binding.btnLogout.setOnClickListener {

            LogoutHandler.logout(
                activity = this,
                logoutButton = binding.btnLogout
            )
        }
    }

    private fun loadLandlordName() {

        lifecycleScope.launch {

            try {

                val session = TokenManager(
                    applicationContext
                ).getSession()

                val fullName = session.fullName
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

            } catch (e: CancellationException) {

                throw e

            } catch (_: Exception) {

                binding.tvWelcome.text =
                    "Xin chào, Chủ trọ!"
            }
        }
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