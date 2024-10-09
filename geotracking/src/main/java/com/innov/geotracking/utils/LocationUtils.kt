package com.innov.geotracking.utils

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import androidx.core.app.ActivityCompat
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority
import com.innov.geotracking.Constant
import com.innov.geotracking.R
import com.innov.geotracking.base.BaseActivity
import com.innov.geotracking.base.BaseApplication
import com.innov.geotracking.databinding.LayoutLocationUtilsBinding
import com.innov.geotracking.utils.enum.MsgTypes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

class LocationUtils : BaseActivity() {
    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private lateinit var geocoder: Geocoder
    lateinit var binding: LayoutLocationUtilsBinding
    private var addresses: List<Address>? = ArrayList()
    lateinit var preferenceUtils: PreferenceUtils
    var latitude: Double? = 0.0
    var longitude: Double? = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LayoutLocationUtilsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        preferenceUtils = PreferenceUtils(this)
        getLastLocation()

    }

    private fun getLastLocation() {
        geocoder = Geocoder(BaseApplication.mContext, Locale.getDefault())
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        if (checkPermissions()) {
            preferenceUtils.setValue(Constant.AskedPermission.LOCATION_PERMISSION_COUNT, 0)
            if (isLocationEnabled()) {
                getLocation()
            } else {
                enableLocationSettings()
            }
        } else {
            requestPermissions()
        }

    }

    @SuppressLint("MissingPermission")
    private fun getLocation() {
        toggleLoader(true)
        mFusedLocationClient.lastLocation.addOnCompleteListener {
            val location = it.result
            if (location == null) {
                requestNewLocationData()
            } else {
                val latitude = location.latitude.toString()
                val longitude = location.longitude.toString()
                intent.putExtra(Constant.IntentExtras.EXTRA_LATITUDE, latitude)
                intent.putExtra(Constant.IntentExtras.EXTRA_LONGITUDE, longitude)
                getAddressFromLatLon(latitude.toDouble(), longitude.toDouble()) {
                    val addr = it
                    intent.putExtra(Constant.IntentExtras.EXTRA_PICKUP_ADDRESS, addr)
                    setResult(Activity.RESULT_OK, intent)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        try {
                            toggleLoader(false)
                        } catch (e: java.lang.Exception) {
                        } finally {
                            finish()
                        }
                    } else {
                        toggleLoader(false)
                        finish()
                    }
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestNewLocationData() {
        val mLocationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 100)
            .setWaitForAccurateLocation(false)
            .setMinUpdateIntervalMillis(50)
            .setMaxUpdateDelayMillis(100)
            .build()

        Looper.myLooper()?.let {
            mFusedLocationClient.requestLocationUpdates(
                mLocationRequest, mLocationCallback, it
            )
        }

        getLocation()

    }

    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val mLastLocation: Location? = locationResult.lastLocation
            latitude = mLastLocation?.latitude ?: 0.00
            longitude = mLastLocation?.longitude ?: 0.00
            intent.putExtra(Constant.IntentExtras.EXTRA_LATITUDE, latitude.toString())
            intent.putExtra(Constant.IntentExtras.EXTRA_LATITUDE, latitude.toString())
            getAddressFromLatLon(latitude ?: 0.00, longitude ?: 0.00) {
                val address = it
                intent.putExtra(Constant.IntentExtras.EXTRA_PICKUP_ADDRESS, address)
                setResult(Activity.RESULT_OK, intent)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    try {
                        toggleLoader(false)
                    } catch (e: java.lang.Exception) {
                    } finally {
                        CoroutineScope(Dispatchers.Main).launch {
                            delay(500)
                            finish()
                        }
                    }
                } else {
                    toggleLoader(false)
                    finish()
                }
            }
        }
    }

    private fun getAddressFromLatLon(
        latitude: Double,
        longitude: Double,
        callbackValue: (String) -> Unit
    ) {
        /**
         * Returns Address based on lat,lon
         */
        var strAdd: String? = ""
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                geocoder.getFromLocation(latitude, longitude, 1) {
                    val addresses = it
                    val returnedAddress = addresses[0]
                    val strReturnedAddress = StringBuilder("")
                    for (i in 0..returnedAddress.maxAddressLineIndex) {
                        strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n")
                    }
                    strAdd = strReturnedAddress.toString()
                    AppUtils.INSTANCE?.logMe(
                        "My Current location address",
                        strReturnedAddress.toString()
                    )
                    callbackValue(strAdd.toString())
                }
            } else {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    geocoder.getFromLocation(
                        latitude, longitude, 1
                    ) {
                        addresses = it
                    }
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        geocoder.getFromLocation(
                            latitude, longitude, 1
                        ) {
                            addresses = it
                        }
                    } else {
                        addresses = geocoder.getFromLocation(
                            latitude, longitude, 1
                        )
                    }
                }
                if (addresses != null) {
                    val returnedAddress = addresses!![0]
                    val strReturnedAddress = StringBuilder("")
                    for (i in 0..returnedAddress.maxAddressLineIndex) {
                        strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n")
                    }
                    strAdd = strReturnedAddress.toString()
                    AppUtils.INSTANCE?.logMe(
                        "My Current location address",
                        strReturnedAddress.toString()
                    )
                    callbackValue(strAdd.toString())
                } else {
                    AppUtils.INSTANCE?.logMe("My Current location address", "No Address returned!")
                    callbackValue("null")
                }
            }
        } catch (e: Exception) {
            AppUtils.INSTANCE?.logMe("Exception :", e.toString())
            callbackValue("null")
        }
    }

    private fun enableLocationSettings() {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 100)
            .setWaitForAccurateLocation(false)
            .setMinUpdateIntervalMillis(50)
            .setMaxUpdateDelayMillis(100)
            .build()
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
        LocationServices
            .getSettingsClient(this)
            .checkLocationSettings(builder.build())
            .addOnSuccessListener(
                this
            ) { }
            .addOnFailureListener(
                this
            ) { ex: java.lang.Exception? ->
                if (ex is ResolvableApiException) {
                    // Location settings are NOT satisfied,  but this can be fixed  by showing the user a dialog.
                    try {
                        // Show the dialog by calling startResolutionForResult(),  and check the result in onActivityResult().
                        ex.startResolutionForResult(
                            this,
                            Constant.PermissionRequestCodes.REQUEST_CODE_CHECK_SETTINGS
                        )

//                        requestPermissions()
                    } catch (sendEx: IntentSender.SendIntentException) {
                        // Ignore the error.
                    }
                }
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Constant.PermissionRequestCodes.REQUEST_CODE_CHECK_SETTINGS) {
            if (Activity.RESULT_OK == resultCode) {
                getLastLocation()
            } else {
                showToast(
                    msg = getString(R.string.please_turn_on_location),
                    msgType = MsgTypes.WARNING,
                    isDialog = false
                )
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
                setResult(Activity.RESULT_CANCELED, intent)
                finish()
            }
        }
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager =
            getSystemService(LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )

    }

    private fun checkPermissions(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }
        return false
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            ),
            Constant.PermissionRequestCodes.PERMISSION_REQUEST_ACCESS_FINE_LOCATION
        )
    }

    private fun toggleLoader(showLoader: Boolean) {
        toggleFadeView(
            binding.root,
            binding.contentLoading.root,
            binding.contentLoading.imageLoading,
            showLoader
        )
    }

}
