package com.example.docmate.util

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

object NotificationPermissionHelper {

    private const val PERMISSION = Manifest.permission.POST_NOTIFICATIONS
    const val REQUEST_CODE = 1001

    fun isRequired(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

    fun isGranted(context: Context): Boolean =
        !isRequired() || ContextCompat.checkSelfPermission(context, PERMISSION) == android.content.pm.PackageManager.PERMISSION_GRANTED

    fun request(activity: Activity) {
        if (!isRequired() || isGranted(activity)) return
        ActivityCompat.requestPermissions(activity, arrayOf(PERMISSION), REQUEST_CODE)
    }

    fun showRationaleDialog(activity: Activity) {
        AlertDialog.Builder(activity)
            .setTitle("Notifications Needed")
            .setMessage(
                "DocMate uses notifications to send you important appointment reminders, " +
                "lab result updates, and prescription notifications. " +
                "Please allow notifications to stay informed about your health."
            )
            .setPositiveButton("Allow") { _, _ -> request(activity) }
            .setNegativeButton("Not Now", null)
            .show()
    }

    fun showSettingsDialog(activity: Activity) {
        AlertDialog.Builder(activity)
            .setTitle("Notifications Disabled")
            .setMessage(
                "You have permanently denied notification permissions. " +
                "To receive appointment reminders and health updates, " +
                "please enable notifications in the app settings."
            )
            .setPositiveButton("Go to Settings") { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", activity.packageName, null)
                }
                activity.startActivity(intent)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    fun handleResult(
        activity: Activity,
        grantResults: IntArray,
        onGranted: () -> Unit,
        onDenied: () -> Unit,
    ) {
        if (grantResults.isNotEmpty() && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            onGranted()
        } else {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(activity, PERMISSION)) {
                showSettingsDialog(activity)
            } else {
                showRationaleDialog(activity)
            }
            onDenied()
        }
    }
}
