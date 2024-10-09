package com.innov.geotracking.presentation.geo_tracking

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.innov.geotracking.Constant
import com.innov.geotracking.Constant.Companion.EXISTING_GEO_TRACKING_ACTIVITY
import com.innov.geotracking.Constant.Companion.GEO_TRACKING_ACTIVITY
import com.innov.geotracking.Constant.Companion.IS_FROM_NOTIFICATION_FLOW
import com.innov.geotracking.Constant.Companion.MARK_ATTENDANCE_FRAGMENT
import com.innov.geotracking.Constant.Companion.MARK_ATTENDANCE_MAP_ACTIVITY
import com.innov.geotracking.Constant.Companion.ON_GOING_GEO_TRACKING_ACTIVITY
import com.innov.geotracking.Constant.Companion.SOURCE_ACTIVITY
import com.innov.geotracking.Constant.IntentExtras.Companion.EXTRA_LATITUDE
import com.innov.geotracking.Constant.IntentExtras.Companion.EXTRA_LONGITUDE
import com.innov.geotracking.Constant.IntentExtras.Companion.EXTRA_PICKUP_ADDRESS
import com.innov.geotracking.R
import com.innov.geotracking.base.BaseActivity
import com.innov.geotracking.databinding.ActivityMarkAttendanceBinding
import com.innov.geotracking.presentation.geo_tracking.bottomsheets.GeoTrackingNewTripBottomSheet
import com.innov.geotracking.presentation.geo_tracking.model.GeoDBModel
import com.innov.geotracking.presentation.geo_tracking.model.LatLonModel
import com.innov.geotracking.services.GeoTrackingService
import com.innov.geotracking.utils.AppUtils
import com.innov.geotracking.utils.LocationUtils
import com.innov.geotracking.utils.PermissionUtils.Companion.getNotificationPermission
import com.innov.geotracking.utils.PermissionUtils.Companion.requestNotificationPermission
import com.innov.geotracking.utils.PreferenceUtils
import io.realm.Realm

class MarkAttendanceMapActivity : BaseActivity(), OnMapReadyCallback,
    GeoTrackingNewTripBottomSheet.StartButtonClickListener {
    private lateinit var binding: ActivityMarkAttendanceBinding
    private lateinit var googleMap: GoogleMap

    private var sourceActivity: String = ""
    private var isFromNotificationFlow = false
    private var isTripStarted: Boolean = false
    private var geoTrackingId = ""
    private var pendingMapAction: (() -> Unit)? = null
    private val polylinePoints = mutableListOf<LatLng>()
    private var latLonListFromDb = mutableListOf<LatLonModel>()
    private var currentPolyline: Polyline? = null
    private var polylineOptions: PolylineOptions? = null
    private var currentLat = 0.0
    private var currentLong = 0.0
    private lateinit var mapView: MapView

    private val startLocationUtilLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val data = result.data
                val currentLocation = data?.getStringExtra(EXTRA_PICKUP_ADDRESS)
                currentLat = data?.getStringExtra(EXTRA_LATITUDE)?.toDouble() ?: 0.0
                currentLong = data?.getStringExtra(EXTRA_LONGITUDE)?.toDouble() ?: 0.0
                var title = ""
                var description = ""

                currentLocation?.let { address ->
                    val addressParts = address.split(",").map { it.trim() }
                    if (addressParts.isNotEmpty()) {
                        title = addressParts.getOrNull(2) ?: ""
                    }
                    if (addressParts.size > 3) {
                        description = addressParts.filter { it != title }.joinToString(", ")
                    } else {
                        // If title is not found, use full address as description
                        description = addressParts.joinToString(", ")
                    }
                }

                binding.tvLocationTitle.text = title
                binding.tvLocationContent.text = description
                updateMapLocation()
            }
        }

    private val locationUpdateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            drawTripPolyline(googleMap)
            Log.d("OnReceiveId", "onReceive geoId:$geoTrackingId ")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMarkAttendanceBinding.inflate(layoutInflater)
        setContentView(binding.root)
        getIntentData()
        setUpLocationTV()
        setUpListeners()
        mapView = binding.mapView
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        // Register the broadcast receiver
        LocalBroadcastManager.getInstance(this).registerReceiver(
            locationUpdateReceiver, IntentFilter("com.innov.hrms.LOCATION_UPDATE")
        )
    }

    private fun setUpListeners() {

        binding.apply {

            btnConfirm.setOnClickListener {
                finish()
            }
            btnRetake.setOnClickListener {
                containerImageView.visibility = GONE
                containerMapView.visibility = VISIBLE
            }
            btnEndTrip.setOnClickListener {
//                 Retrieve the ongoing trip from the database
                isTripStarted = false
                Realm.getDefaultInstance().use { realm ->
                    realm.executeTransaction { transactionRealm ->
                        val ongoingTrip = transactionRealm.where(GeoDBModel::class.java)
                            .equalTo("geoTrackingId", geoTrackingId).findFirst()

                        ongoingTrip?.let {
                            latLonListFromDb = it.latLonList

                            if (latLonListFromDb.isNotEmpty()) {
                                val lastLocation = latLonListFromDb.last()
                                val endLatLng =
                                    LatLng(lastLocation.latitude, lastLocation.longitude)

                                // Add a marker at the last location (end point)
                                googleMap.addMarker(
                                    MarkerOptions().position(endLatLng).title("Destination")
                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_start_trip_location))

                                )
                                googleMap.moveCamera(
                                    CameraUpdateFactory.newLatLngZoom(
                                        endLatLng, 15f
                                    )
                                )
                            }
                            // Update the trip status to false
                            it.isTripOngoing = false
                            transactionRealm.insertOrUpdate(it) // Save changes
                            Log.d("MarkAttendanceMapActivity", "Trip ended successfully.")
                        } ?: run {
                            // Handle case where no ongoing trip is found
                            Log.e("MarkAttendanceMapActivity", "No ongoing trip found to end.")
                        }
                    }
                }
                val geoDBModel = GeoDBModel()
                geoDBModel.isTripOngoing = false
                val intent = Intent(this@MarkAttendanceMapActivity, GeoTrackingService::class.java)
                intent.action = GeoTrackingService.ACTION_STOP_FOREGROUND_SERVICE_TRACKING
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    startForegroundService(intent)
                } else {
                    startService(intent)
                }

                if (isFromNotificationFlow) {
                    val intent1 =
                        Intent(this@MarkAttendanceMapActivity, GeoTrackingActivity::class.java)
                    startActivity(intent1)
                    finish()
                }
                else {
                    finish()
                }

            }

            btnConfirmLocation.setOnClickListener {

                if (sourceActivity != MARK_ATTENDANCE_FRAGMENT) {
                    if (!Settings.canDrawOverlays(applicationContext)) {
                        checkOverlayPermission(applicationContext)
                    } else {
                        if (setUpNotificationPermission()) {
                            // Permission is granted, show the bottom sheet
                            GeoTrackingNewTripBottomSheet(listener = this@MarkAttendanceMapActivity).show(
                                supportFragmentManager, "GeoTrackingNewTripBottomSheet"
                            )
                        } else {
                            // Permission is denied, show a toast message
                            AppUtils.INSTANCE?.showToast("Please allow notifications to start a new trip.")
                        }
                    }

                }
            }

            binding.toolbar.btnBack.setOnClickListener {
                if (isFromNotificationFlow) {
                    val intent =
                        Intent(this@MarkAttendanceMapActivity, GeoTrackingActivity::class.java)
                    intent.putExtra(SOURCE_ACTIVITY, MARK_ATTENDANCE_MAP_ACTIVITY)
                    startActivity(intent)
                    finish()
                } else {
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        }
    }

    private fun getIntentData() {

        intent.extras?.let {
            sourceActivity = it.getString(SOURCE_ACTIVITY).toString()
            isFromNotificationFlow = it.getBoolean(IS_FROM_NOTIFICATION_FLOW, false)
            geoTrackingId = it.getString(Constant.SELECTED_TRIP_ID).toString()
            when (sourceActivity) {
                ON_GOING_GEO_TRACKING_ACTIVITY -> {
                    pendingMapAction = { drawTripPolyline(googleMap) }
                }

                EXISTING_GEO_TRACKING_ACTIVITY -> {
                    pendingMapAction = {
                        drawTripPolyline(googleMap)
                        showDestinationMarker()
                        googleMap.moveCamera(
                            CameraUpdateFactory.newLatLngZoom(
                                polylinePoints.first(), 15f
                            )
                        )
                    }

                }

                else -> {

                }
            }
        }
        setUpViews()
    }

    private fun setUpLocationTV() {

        val intent = Intent(this, LocationUtils::class.java)
        startLocationUtilLauncher.launch(intent)
    }

    private fun setUpViews() {

        binding.apply {

            when (sourceActivity) {


                GEO_TRACKING_ACTIVITY -> {
                    cardLocation.visibility = VISIBLE
                    toolbar.tvTitle.text = getString(R.string.start_new_trip)
                    setUpNotificationPermission()
                }

                ON_GOING_GEO_TRACKING_ACTIVITY -> {
                    cardLocation.visibility = VISIBLE
                    toolbar.tvTitle.text = getString(R.string.on_going_trip)
                    containerLocationDetails.visibility = GONE
                    containerOngoingTripDetails.visibility = VISIBLE
                }

                EXISTING_GEO_TRACKING_ACTIVITY -> {
                    toolbar.tvTitle.text = getString(R.string.trip_completed)
                    cardLocation.visibility = GONE
                }

                else -> {
                    cardLocation.visibility = VISIBLE
                    toolbar.tvTitle.text = getString(R.string.on_going_trip)
                    containerLocationDetails.visibility = GONE
                    containerOngoingTripDetails.visibility = VISIBLE
                    geoTrackingId =
                        PreferenceUtils(this@MarkAttendanceMapActivity).getValue(Constant.PRIMARY_KEY)
                    pendingMapAction = { drawTripPolyline(googleMap) }
                    if (::googleMap.isInitialized) drawTripPolyline(googleMap)
                }

            }
        }


    }

    private fun setUpNotificationPermission(): Boolean {

        return if (getNotificationPermission(this@MarkAttendanceMapActivity))
        {
            true
        }else{
            requestNotificationPermission(this@MarkAttendanceMapActivity)
            false
        }

      /*  return if (!PermissionUtils.getNotificationPermission(this)) {
            // Request notification permission
            PermissionUtils.requestNotificationPermission(this)
            false
        } else {
            true
        }*/
    }

    private fun drawTripPolyline(googleMap: GoogleMap) {
        // Clear the previous polyline and reset points
        polylinePoints.clear()
        val realm = Realm.getDefaultInstance()
        realm.executeTransaction { transactionRealm ->
            val savedData = transactionRealm.where(GeoDBModel::class.java)
                .equalTo("geoTrackingId", geoTrackingId).findFirst()
            latLonListFromDb = savedData?.latLonList ?: mutableListOf()
            Log.d("Debug", "latLong in database : $latLonListFromDb")
        }

        if (latLonListFromDb.isNotEmpty()) {
            // Convert LatLonModel to LatLng
            val newLatLngList = latLonListFromDb.map { LatLng(it.latitude, it.longitude) }

            // Add new points to the polyline
            polylinePoints.addAll(newLatLngList)
            Log.d("Debug", "drawTripPolyline in mark: $polylinePoints")

            // Check if polyline already exists
            if (currentPolyline == null) {
                // Create a new polyline if it doesn't exist
                polylineOptions = PolylineOptions().addAll(polylinePoints)
                    .color(ContextCompat.getColor(this, R.color.colorPrimary)).width(5f)

                currentPolyline = googleMap.addPolyline(polylineOptions ?: PolylineOptions())

                // Add start marker
                if (!isTripStarted)
                    googleMap.addMarker(
                        MarkerOptions().position(polylinePoints.first()).title("Start Point")
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_start_trip_location))
                    )

                // Move camera to start point
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(polylinePoints.first(), 15f))
            } else {
                // Update the existing polyline with new points
                currentPolyline?.apply {
                    points = polylinePoints
                    Log.d("Debug", "Polyline points updated, count: ${points.size}")
                }
            }
        } else {
            Log.d("Debug", "No latLonList found for geoTrackingId: $geoTrackingId")
        }
    }

    private fun showDestinationMarker() {
        Realm.getDefaultInstance().use { realm ->
            realm.executeTransaction { transactionRealm ->
                val ongoingTrip = transactionRealm.where(GeoDBModel::class.java)
                    .equalTo("geoTrackingId", geoTrackingId).findFirst()

                ongoingTrip?.let {
                    latLonListFromDb = it.latLonList

                    if (latLonListFromDb.isNotEmpty()) {
                        val lastLocation = latLonListFromDb.last()
                        val endLatLng = LatLng(lastLocation.latitude, lastLocation.longitude)

                        // Add a marker at the last location (end point)
                        googleMap.addMarker(
                            MarkerOptions().position(endLatLng).title("Destination")
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_start_trip_location))
                        )
                        googleMap.moveCamera(
                            CameraUpdateFactory.newLatLngZoom(endLatLng, 15f)
                        )
                    }
                } ?: run {
                    // Handle case where no ongoing trip is found
                    Log.e("MarkAttendanceMapActivity", "No ongoing trip found.")
                }
            }
        }
    }

    private fun updateMapLocation() {
        if (currentLat != 0.0 && currentLong != 0.0) {
            val currentLatLng = LatLng(currentLat, currentLong)
            Log.d("MapUpdate", "Updating map location to: $currentLatLng")
//            googleMap.clear() // Clear existing markers
//            googleMap.addMarker(
//                MarkerOptions()
//                    .position(currentLatLng)
//                    .title("You are here")
//            )
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))
        } else {
            Log.d("MapUpdate", "Invalid coordinates: lat=$currentLat, long=$currentLong")
        }
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
//        drawTripPolyline(googleMap)
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    private fun checkOverlayPermission(context: Context) {
        AppUtils.INSTANCE?.showToast(getString(R.string.allow_display_overlays_permission_to_continue))
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${context.packageName}")
        )
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        initializeMap()
        pendingMapAction?.invoke()
        pendingMapAction = null
    }

    override fun onBackPressed() {
        if (isFromNotificationFlow) {
            val intent = Intent(this, GeoTrackingActivity::class.java)
            intent.putExtra(SOURCE_ACTIVITY, MARK_ATTENDANCE_MAP_ACTIVITY)
            startActivity(intent)
            finish()
        } else {
            super.onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun initializeMap() {
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            googleMap.isMyLocationEnabled = true
            googleMap.uiSettings.isMyLocationButtonEnabled = true
            updateMapLocation()
        } else {
            requestLocationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }

    override fun onStartButtonClicked() {
        binding.apply {
            isTripStarted = true
            containerLocationDetails.visibility = GONE
            containerOngoingTripDetails.visibility = VISIBLE
            geoTrackingId =
                PreferenceUtils(this@MarkAttendanceMapActivity).getValue(Constant.PRIMARY_KEY)
            plotMarkerOnMap()
        }
    }

    private fun plotMarkerOnMap() {
        val currentLocation = LatLng(currentLat, currentLong)

        // Create a MarkerOptions object to customize the marker
        val markerOptions = MarkerOptions()
            .position(currentLocation) // Set the marker position
            .title("Source") // Set the title for the marker (optional)
            .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_start_trip_location)) // Use a custom marker icon

        // Add the marker to the GoogleMap
        if (::googleMap.isInitialized)
            googleMap.addMarker(markerOptions)

        // Move the camera to the marker position and set the zoom level
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15f))
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == Constant.PermissionRequestCodes.NOTIFICATION_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            } else {
                // Permission denied, show a message to the user
            }
        }
    }

    private val requestLocationPermissionLauncher =
        registerForActivityResult(RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                initializeMap()
            } else {
                Log.e("MapActivity", "Location permission denied.")
            }
        }
}