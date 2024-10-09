package com.innov.geotracking.utils

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.ActivityResultLauncher
import androidx.core.app.ActivityCompat
import com.innov.geotracking.Constant
import com.innov.geotracking.base.BaseApplication.Companion.preferenceUtils

class PermissionUtils {
    companion object{
        fun getStoragePermission(activity:Activity): Boolean {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
                return true
            }
            else {
                if (ActivityCompat.checkSelfPermission(
                        activity,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    return true
                }
                return false
            }
        }
        fun requestStoragePermissions(activity: Activity) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ),
                Constant.PermissionRequestCodes.STORAGE_PERMISSION_CODE
            )
        }

         fun getCameraPermission(activity: Activity): Boolean {
            if (ActivityCompat.checkSelfPermission(
                    activity,
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                return true
            }
            return false
        }
        fun requestCameraPermissions(activity: Activity) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(
                    Manifest.permission.CAMERA
                ),
                Constant.PermissionRequestCodes.CAMERA_PERMISSION_CODE
            )
        }
        fun requestCameraPermissions(requestPermissions: ActivityResultLauncher<Array<String>>) {
            requestPermissions.launch(arrayOf(Manifest.permission.CAMERA))
        }

        fun getCallPermission(activity: Activity):Boolean{
            if (ActivityCompat.checkSelfPermission(
                    activity,
                    Manifest.permission.CALL_PHONE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                return true
            }
            return false
        }
        fun requestCallPermissions(activity: Activity) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(
                    Manifest.permission.CALL_PHONE
                ),
                Constant.PermissionRequestCodes.CALL_PHONE_PERMISSION_CODE
            )
        }
        // New function to check if notification permissions are granted
        fun getNotificationPermission(activity: Activity): Boolean {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ActivityCompat.checkSelfPermission(
                    activity, Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                true // Automatically granted below Android 13
            }
        }

        // New function to request notification permissions
        fun requestNotificationPermission(activity: Activity) {


            val permissionAttempts =
                preferenceUtils.getIntValue("notification_permission_attempts", 0)

            if (permissionAttempts < 2) {
                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                    Constant.PermissionRequestCodes.NOTIFICATION_PERMISSION_CODE
                )
                preferenceUtils.setValue("notification_permission_attempts", permissionAttempts + 1)
            } else {
                // Show custom dialog directing to app's permission settings
                DialogUtils.showPermissionDialog(
                    activity,
                    "Notification Permission",
                    "Please enable notifications from the app settings.",
                    "Go to Settings",
                    "Not Now"
                )
            }
        }
    }
}