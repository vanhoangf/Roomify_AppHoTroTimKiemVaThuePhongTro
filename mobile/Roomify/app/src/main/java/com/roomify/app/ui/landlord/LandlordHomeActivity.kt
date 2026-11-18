
package com.roomify.app.ui.landlord

import android.os.Bundle
import android.widget.Toast

import androidx.appcompat.app.AppCompatActivity

import com.roomify.app.databinding.ActivityLandlordHomeBinding
import com.roomify.app.ui.common.LogoutHandler
import com.roomify.app.ui.common.RoleSessionGuard

class LandlordHomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLandlordHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLandlordHomeBinding.inflate(
            layoutInflater
        )

        setContentView(binding.root)

        // NÚT ĐĂNG XUẤT
        binding.btnLogout.setOnClickListener {

            LogoutHandler.logout(
                activity = this,
                logoutButton = binding.btnLogout
            )
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
