package com.innov.geotracking.services

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.PixelFormat
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.TaskStackBuilder
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.innov.geotracking.Constant.IntentExtras.Companion.IS_FROM_NOTIFICATION_FLOW
import com.innov.geotracking.R
import com.innov.geotracking.base.BaseApplication
import com.innov.geotracking.utils.AppUtils
import com.innov.geotracking.utils.PreferenceUtils
import com.innov.geotracking.presentation.geo_tracking.GeoTrackingActivity
import com.innov.geotracking.presentation.geo_tracking.MarkAttendanceMapActivity
import com.innov.geotracking.Constant
import com.innov.geotracking.presentation.geo_tracking.model.GeoDBModel
import com.innov.geotracking.presentation.geo_tracking.model.LatLonModel
import io.realm.Realm
import kotlin.math.roundToInt

class GeoTrackingService : Service() {
    companion object {
        const val CHANNEL_ID = Constant.PermissionRequestCodes.CHANNEL_ID
        const val NOTIFICATION_ID = Constant.PermissionRequestCodes.NOTIFICATION_PERMISSION_CODE
        const val ACTION_STOP_FOREGROUND_SERVICE_TRACKING =
            "ACTION_STOP_FOREGROUND_SERVICE_TRACKING"
        private const val MIN_DISTANCE_METERS = 10.0
        private const val TAG = "GeoTrackingService" // Added for logging
        fun stopForegroundService(service: Service) {
            GeoDBModel().isTripOngoing = false
            service.stopForeground(STOP_FOREGROUND_REMOVE)
            service.stopSelf()
            Log.d(TAG, "Foreground service stopped")
        }
    }

    private lateinit var preferenceUtils: PreferenceUtils
    private lateinit var notificationManager: NotificationManager
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    private var geoTrackingId: String = ""
    private var geoTripNameService: String = ""
    private lateinit var locationSettingsReceiver: LocationSettingsReceiver
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate() {
        super.onCreate()
        initService()
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun initService() {

        // Register the broadcast receiver to listen for location changes
        locationSettingsReceiver = LocationSettingsReceiver()
        val intentFilter = IntentFilter(LocationManager.MODE_CHANGED_ACTION)
        registerReceiver(locationSettingsReceiver, intentFilter)

        preferenceUtils = PreferenceUtils(this)
        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        //update as per optimization
        locationRequest =
            LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000).setIntervalMillis(1000)
                .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    Log.d(
                        TAG,
                        "New location fetched: Latitude = ${location.latitude}, Longitude = ${location.longitude}"
                    ) // Log location

                    saveLocationToDatabase(location.latitude, location.longitude)
                    if (!isNotificationActive(CHANNEL_ID.toInt())) {
                        startForeground(CHANNEL_ID.toInt(), createNotification())
                    }
                } ?: run {
//                    Log.d(TAG, "Location is null") // Log null location
                }
            }
        }

        if (!isNotificationActive(CHANNEL_ID.toInt())) {
            val notificationChannel = NotificationChannel(
                CHANNEL_ID,
                Constant.PermissionRequestCodes.LOCATIONS,
                NotificationManager.IMPORTANCE_LOW
            )
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    @Suppress("MissingPermission")
    private fun createLocationRequest() {
        try {
            fusedLocationProviderClient.requestLocationUpdates(
                locationRequest, locationCallback, null
            )
            Log.d(TAG, "Location request created") // Log creation of location request
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "Failed to create location request: ${e.message}") // Log failure
        }
    }

    private fun removeLocationUpdates() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
        Log.d(TAG, "Location updates removed and service stopped") // Log service stop
    }

    private fun createNotification(): Notification {
        return try {
            val resultIntent = Intent(this, MarkAttendanceMapActivity::class.java)
            resultIntent.putExtra(IS_FROM_NOTIFICATION_FLOW, true)
            val stackBuilder = TaskStackBuilder.create(this).apply {
                addParentStack(GeoTrackingActivity::class.java)
                addNextIntent(resultIntent)
            }
            val resultPendingIntent = stackBuilder.getPendingIntent(
                Constant.PermissionRequestCodes.SERVICE_LOCATION_REQUEST_CODE,
                PendingIntent.FLAG_IMMUTABLE
            )
            Log.d(TAG, "Notification created") // Log notification creation
            NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.geo_tracking))
                .setSmallIcon(R.mipmap.ic_launcher_geo)   //add app logo
                .setPriority(NotificationCompat.PRIORITY_LOW).setContentIntent(resultPendingIntent)
                .setOngoing(true).build()
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "Failed to create notification: ${e.message}") // Log notification error
            NotificationCompat.Builder(this, CHANNEL_ID).setContentTitle("Error")
                .setContentText("Failed to create notification").setSmallIcon(R.mipmap.ic_launcher_geo)
                .setPriority(NotificationCompat.PRIORITY_HIGH).build()
        }
    }

    private fun updateTripStatusInDatabase(isOngoing: Boolean) {
        try {
            val realm = Realm.getDefaultInstance()
            realm.executeTransaction { transactionRealm ->
                val existingData = transactionRealm.where(GeoDBModel::class.java)
                    .equalTo("geoTrackingId", geoTrackingId).findFirst()

                existingData?.let {
                    it.isTripOngoing = isOngoing
                    transactionRealm.insertOrUpdate(it)
                    Log.d(TAG, "Updated isTripOngoing to $isOngoing")
                }
            }
            realm.close()
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "Failed to update trip status in database: ${e.message}")
        }
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
//        geoTrackingId = intent?.getStringExtra(Constant.IntentExtras.GEO_TRACKING_ID_LOCAL_DATABASE) ?: ""
        geoTrackingId = PreferenceUtils(BaseApplication.mContext).getValue(Constant.PRIMARY_KEY)
        geoTripNameService = intent?.getStringExtra(Constant.GEO_TRIP_NAME).toString()
        Log.d(TAG, "GeoTracking ID: $geoTripNameService") // Log GeoTracking ID
        Log.d(TAG, "GeoTracking ID: $geoTrackingId") // Log GeoTracking ID

        intent?.action?.takeIf {
            it.equals(
                ACTION_STOP_FOREGROUND_SERVICE_TRACKING, ignoreCase = true
            )
        }?.let {
            stopForegroundService(service = this as Service)
        } ?: run {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                startForeground(NOTIFICATION_ID, createNotification())
            } else {
                startService(Intent(this, GeoTrackingService::class.java))
            }
            createLocationRequest()
        }

        return START_STICKY
    }

    override fun onBind(intent: Intent): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        GeoDBModel().isTripOngoing = false
        updateTripStatusInDatabase(false)
        removeLocationUpdates()
        Log.d(TAG, "Service destroyed") // Log service destruction
        unregisterReceiver(locationSettingsReceiver)
    }

    private fun saveLocationToDatabase(latitude: Double, longitude: Double) {
        try {
            val realm = Realm.getDefaultInstance()
            realm.executeTransaction { transactionRealm ->
                val existingData = transactionRealm.where(GeoDBModel::class.java)
                    .equalTo("geoTrackingId", geoTrackingId).findFirst()

                existingData?.let {
                    val lastLatLon = it.latLonList.lastOrNull()
                    val distance = calculateDistance(lastLatLon, latitude, longitude)
                    if (distance >= MIN_DISTANCE_METERS) {
                        val newLatLonModel = LatLonModel().apply {
                            this.latitude = latitude
                            this.longitude = longitude
                        }
                        it.apply {
                            timeStamp =
                                "${AppUtils.INSTANCE?.getCurrentTime()} ${AppUtils.INSTANCE?.getCurrentDate()}"
                            latLonList.add(newLatLonModel)
                            isTripOngoing = true
                            this.geoTripName = geoTripNameService
                        }
                        transactionRealm.insertOrUpdate(it) // Save the updated model
                        sendLocationUpdateBroadcast(
                            newLatLonModel.latitude, newLatLonModel.longitude
                        )
                        Log.d(
                            TAG,
                            "Location saved to database: ${newLatLonModel.latitude}, ${newLatLonModel.longitude}"
                        )
                    } else {
                        Log.d(TAG, "Location not saved: Distance less than 5 meters")
                    }
                } ?: run {
                    // No existing data, create a new entry
                    val newEntry = GeoDBModel().apply {
                        geoTrackingId = this@GeoTrackingService.geoTrackingId
                        timeStamp =
                            "${AppUtils.INSTANCE?.getCurrentTime()} ${AppUtils.INSTANCE?.getCurrentDate()}"
                        latLonList.clear()
                        geoTripName = geoTripNameService
                        latLonList.add(LatLonModel().apply {
                            this.latitude = latitude
                            this.longitude = longitude
                        })
                        isTripOngoing = true
                    }
                    transactionRealm.insertOrUpdate(newEntry)
                    Log.d(
                        TAG,
                        "New entry created at  $geoTrackingId and location saved: Latitude = $latitude, Longitude = $longitude,"
                    )
                    sendLocationUpdateBroadcast(latitude, longitude)

                }
            }
//            logSavedData() // Log the saved data to verify it's stored correctly
            realm.close()
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "Failed to save location to database: ${e.message}")
        }
    }

    private fun sendLocationUpdateBroadcast(latitude: Double, longitude: Double) {
        val intent = Intent("com.innov.hrms.LOCATION_UPDATE")
        intent.putExtra("latitude", latitude)
        intent.putExtra("longitude", longitude)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    private fun isNotificationActive(notificationId: Int): Boolean {
        val isActive =
            notificationManager.activeNotifications?.any { it.id == notificationId } ?: false
        Log.d(TAG, "Is notification active: $isActive") // Log notification status
        return isActive
    }


    private fun logSavedData() {
        try {
            val realm = Realm.getDefaultInstance()
            realm.executeTransaction { transactionRealm ->
                val savedData = transactionRealm.where(GeoDBModel::class.java)
                    .equalTo("geoTrackingId", geoTrackingId).findFirst()

                savedData?.let {
                    Log.d(TAG, "Data found: ${it.latLonList}")
                    Log.d(TAG, "Timestamp: ${it.timeStamp}")
                    Log.d(TAG, "Is Trip Ongoing: ${it.isTripOngoing}")
                } ?: run {
                    Log.d(TAG, "No data found for geoTrackingId: $geoTrackingId")
                }
            }
            realm.close()
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "Error querying saved data: ${e.message}")
        }
    }

    private fun calculateDistance(
        lastLocation: LatLonModel?, newLatitude: Double, newLongitude: Double
    ): Double {
        lastLocation?.let {
            val loc1 = Location("").apply {
                latitude = it.latitude
                longitude = it.longitude
            }
            val loc2 = Location("").apply {
                latitude = newLatitude
                longitude = newLongitude
            }
            val distance = loc1.distanceTo(loc2).toDouble()
            Log.d(TAG, "Calculated distance: $distance meters") // Log calculated distance
            return distance
        }
        return Double.MAX_VALUE
    }

    @SuppressLint("NewApi")
    private fun isMockLocation(location: Location?): Boolean {
        return location?.isMock ?: false
    }

    class LocationSettingsReceiver : BroadcastReceiver() {
        private var isDialogVisible = false
        private var dialogView: View? = null
        private var windowManager: WindowManager? = null

        override fun onReceive(context: Context?, intent: Intent?) {
            // Check if location is enabled
            val locationMode = Settings.Secure.getInt(
                context?.contentResolver,
                Settings.Secure.LOCATION_MODE,
                Settings.Secure.LOCATION_MODE_OFF
            )
            if (locationMode == Settings.Secure.LOCATION_MODE_OFF) {
                // Location is turned off, pause the service
                AppUtils.INSTANCE?.showToast(BaseApplication.mContext.getString(R.string.location_is_turned_off_tracking_paused))
                context?.let {
                    if (Settings.canDrawOverlays(context)) {
                        showOverlayDialog(it)
                    }
                }

            } else {
                // Location is turned on, resume the service
                if (isDialogVisible) {
                    dismissOverlayDialog()
                }
                AppUtils.INSTANCE?.showToast(BaseApplication.mContext.getString(R.string.location_is_back_on_tracking_resumed))

            }
        }

        @SuppressLint("InflateParams")
        private fun showOverlayDialog(context: Context) {
            if (isDialogVisible) return

            windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            // Inflate your custom layout for the dialog
            val layoutInflater = LayoutInflater.from(context)
            dialogView = layoutInflater.inflate(R.layout.dialog_permission, null)
            // Create layout params for the overlay dialog
            val params = WindowManager.LayoutParams(
                (BaseApplication.mContext.resources.displayMetrics.widthPixels * 0.90).roundToInt(),
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSLUCENT
            )

            params.gravity = Gravity.CENTER
            dialogView?.apply {
                findViewById<androidx.appcompat.widget.AppCompatTextView>(R.id.tvMsg)?.text =
                    context.getString(R.string.please_enable_location_to_continue)
                findViewById<androidx.appcompat.widget.AppCompatTextView>(R.id.tvAllowPermission)?.text =
                    context.getString(R.string.tracking_paused)
                findViewById<androidx.appcompat.widget.AppCompatTextView>(R.id.tvDeny)?.text =
                    context.getString(R.string.end_geo_tracking)
                findViewById<androidx.appcompat.widget.AppCompatTextView>(R.id.tvGoToSettings)?.text =
                    context.getString(R.string.turn_on_location)
                // Set up the dialog buttons
                findViewById<androidx.appcompat.widget.AppCompatTextView>(R.id.tvGoToSettings)
                    ?.setOnClickListener {
                        dismissOverlayDialog()
                        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        context.startActivity(intent)
                    }

                findViewById<androidx.appcompat.widget.AppCompatTextView>(R.id.tvDeny)
                    ?.setOnClickListener {
                        dismissOverlayDialog()
                        // Ensure the correct context
                        // Khurseed
                  /*      val i = Intent(context, AttendanceMapActivity::class.java)
                        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(i)*/


//                        AppUtils.INSTANCE?.showToast(context.getString(R.string.geo_tracking_ended))
//                        stopForegroundService(service = context as Service)
                    }
            }


            // Add view to window manager
            windowManager?.addView(dialogView, params)
            isDialogVisible = true


        }

        private fun dismissOverlayDialog() {
            dialogView?.let {
                windowManager?.removeView(it)
                dialogView = null // Clear the reference to the view
                isDialogVisible = false
            }
        }
    }
}
