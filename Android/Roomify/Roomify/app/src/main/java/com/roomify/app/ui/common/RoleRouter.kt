
package com.roomify.app.ui.common

import android.app.Activity
import android.content.Intent

import com.roomify.app.ui.student.StudentHomeActivity
import com.roomify.app.ui.landlord.LandlordHomeActivity

import java.util.Locale

object RoleRouter {

    fun openHome(
        activity: Activity,
        role: String?
    ): Boolean {

        val destination = when (
            role?.trim()?.uppercase(Locale.ROOT)
        ) {

            "STUDENT" -> StudentHomeActivity::class.java

            "LANDLORD" -> LandlordHomeActivity::class.java

            else -> null
        }

        if (destination == null) {
            return false
        }

        val intent = Intent(
            activity,
            destination
        ).apply {

            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        activity.startActivity(intent)

        return true
    }
}
