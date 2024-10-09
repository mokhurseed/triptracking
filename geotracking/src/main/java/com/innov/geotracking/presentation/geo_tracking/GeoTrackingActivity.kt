package com.innov.geotracking.presentation.geo_tracking

import android.annotation.SuppressLint
import android.content.Intent
import android.location.Location
import android.os.Build
import android.util.Log
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import com.innov.geotracking.Constant
import com.innov.geotracking.Constant.Companion.GEO_TRACKING_ACTIVITY
import com.innov.geotracking.Constant.Companion.SOURCE_ACTIVITY
import com.innov.geotracking.R
import com.innov.geotracking.base.BaseActivity
import com.innov.geotracking.databinding.ActivityGeotrackingBinding
import com.innov.geotracking.presentation.geo_tracking.adapter.GeoTrackingAdapter
import com.innov.geotracking.presentation.geo_tracking.model.GeoDBModel
import com.innov.geotracking.presentation.geo_tracking.model.GeoLocationDataModel
import com.innov.geotracking.presentation.geo_tracking.model.LatLonModel
import com.innov.geotracking.services.GeoTrackingService
import com.innov.geotracking.utils.AppUtils
import com.innov.geotracking.utils.DialogUtils
import com.innov.geotracking.utils.LocationUtils
import com.vicpin.krealmextensions.delete
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import com.vicpin.krealmextensions.queryAll

class GeoTrackingActivity : BaseActivity() , GeoTrackingAdapter.OnItemClickListener , DialogUtils.DialogManager {
    private lateinit var binding: ActivityGeotrackingBinding
    private val tripList: ArrayList<GeoLocationDataModel> = arrayListOf()
    private lateinit var geoTrackingAdapter: GeoTrackingAdapter
    private var selectedPosition: Int =0
    private var isOnGoingTrip: Boolean = false
        private val startLocationUtilLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val intent = Intent(this, MarkAttendanceMapActivity::class.java)
                intent.putExtra(SOURCE_ACTIVITY, GEO_TRACKING_ACTIVITY)
                startActivity(intent)
            } else {
//                AppUtils.INSTANCE?.showToast(msg = getString(R.string.please_enable_location_to_continue))
            }

        }

    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGeotrackingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setUpToolbar()
        setUpListener()
    }

    override fun onResume() {
        super.onResume()
        fetchListFromDatabase()
    }

    private fun setUpToolbar() {
        binding.toolbar.tvTitle.text = getString(R.string.trip_tracking)
    }

    private fun setUpListener() {
        binding.apply {
            btnStartNewTrip.setOnClickListener {
                val intent = Intent(this@GeoTrackingActivity, LocationUtils::class.java)
                startLocationUtilLauncher.launch(intent)
            }
            toolbar.btnBack.setOnClickListener {
                finish()
            }
        }


    }

    private fun checkNoDataLayout() {
        binding.apply {
            val hasOngoingTrip = tripList.any { it.isOngoingTrip }
            btnStartNewTrip.visibility = if (hasOngoingTrip) GONE else VISIBLE

            if (tripList.isEmpty()) {
                rvTripList.visibility = GONE
                layoutNoData.root.visibility = VISIBLE
            } else {
                rvTripList.visibility = VISIBLE
                layoutNoData.root.visibility = GONE
            }
        }
    }

    private fun fetchListFromDatabase() {
        // Use a coroutine to handle the background work
        CoroutineScope(Dispatchers.IO).launch {
            // Clear the list in a background thread
            tripList.clear()

            // Fetch data from the database
            val dbAllEntriesList = GeoDBModel().queryAll()
            Log.d("list_of_trip", "fetchListFromDatabase: ${dbAllEntriesList.size}")

            // Populate the list with new data
            dbAllEntriesList.forEachIndexed { _, geoDBModel ->
                tripList.add(
                    GeoLocationDataModel(
                        tripId = geoDBModel.geoTrackingId,
                        tripName = geoDBModel.geoTripName,
                        fromLocation = getLocationName(geoDBModel.latLonList.firstOrNull()),
                        toLocation = getLocationName(geoDBModel.latLonList.lastOrNull()),
                        tripDate = geoDBModel.timeStamp,
                        distance = String.format(
                            Locale.US,
                            "%.2f km",
                            calculateTotalTripDistance(geoDBModel.latLonList) / 1000
                        ),
                        isOngoingTrip = geoDBModel.isTripOngoing
                    )
                )
            }

            // Switch context to the main thread to update UI
            withContext(Dispatchers.Main) {
                checkNoDataLayout()
                setUpAdapter()
            }
        }
    }


    @SuppressLint("NotifyDataSetChanged")
    private fun setUpAdapter() {
        if (tripList.isNotEmpty()) {
            tripList.sortByDescending {
                it.tripId
            }
        }
        if (!::geoTrackingAdapter.isInitialized) {
            geoTrackingAdapter =
                GeoTrackingAdapter(itemList = tripList, listener = this@GeoTrackingActivity) {
                    intentToMarkAttendanceMap(it)
                }
            binding.rvTripList.adapter = geoTrackingAdapter
        } else geoTrackingAdapter.notifyDataSetChanged()
    }

    private fun intentToMarkAttendanceMap(selectedTrip: GeoLocationDataModel) {
        val intent = Intent(this, MarkAttendanceMapActivity::class.java)
        intent.putExtra(Constant.SELECTED_TRIP_ID, selectedTrip.tripId)
        if (selectedTrip.isOngoingTrip) {
            intent.putExtra(SOURCE_ACTIVITY, Constant.ON_GOING_GEO_TRACKING_ACTIVITY)
        } else {
            intent.putExtra(SOURCE_ACTIVITY, Constant.EXISTING_GEO_TRACKING_ACTIVITY)
        }
        startActivity(intent)
    }

    private fun getLocationName(latLon: LatLonModel?): String {
        var header = ""
        if (latLon != null) {
            // Fetch the location name as a string
            val locationName = AppUtils.INSTANCE?.getLocationName(
                this@GeoTrackingActivity,
                latLon.latitude,
                latLon.longitude
            )

            // Split the string by comma
            val parts = locationName?.split(",") ?: emptyList()
            if (parts.size > 2) {
                header = parts[2].trim() // Get the third element (index 2) and trim any whitespace
            }
        }
        Log.d("source", "getSourceLocation: $header")
        return header
    }


    private fun calculateTotalTripDistance(latLonList: List<LatLonModel>): Double {
        var totalDistance = 0.0

        if (latLonList.size < 2) {
            return totalDistance
        }

        for (i in 0 until latLonList.size - 1) {
            val startLatLon = latLonList[i]
            val endLatLon = latLonList[i + 1]

            val distance = calculateTwoPointDistance(
                startLatLon.latitude, startLatLon.longitude,
                endLatLon.latitude, endLatLon.longitude
            )
            totalDistance += distance
        }

        return totalDistance
    }

    // Function to calculate the distance between two points using Location.distanceBetween()
    private fun calculateTwoPointDistance(
        startLat: Double,
        startLon: Double,
        endLat: Double,
        endLon: Double
    ): Double {
        val results = FloatArray(1)
        Location.distanceBetween(startLat, startLon, endLat, endLon, results)
        return results[0].toDouble()
    }

    override fun onDeleteAction(position: Int) {
        super.onDeleteAction(position)
        selectedPosition = position
        isOnGoingTrip=tripList[position].isOngoingTrip
        DialogUtils.showLogOutDialog(
            context = this,
            listener = this,
            msg = if (isOnGoingTrip) getString(R.string.your_trip_is_ongoing_are_you_sure_want_to_delete) else getString(R.string.are_you_sure_you_want_to_delete),
            title = getString(R.string.delete_trip),
            btnText = "Yes"
        )

    }

    override fun onOkClick() {
        super.onOkClick()
        val geoTrackingId = tripList[selectedPosition].tripId
        //remove from list
        if (selectedPosition >= 0 && selectedPosition < tripList.size) {
            tripList.removeAt(selectedPosition)
            geoTrackingAdapter.notifyItemRemoved(selectedPosition)
            geoTrackingAdapter.notifyItemRangeChanged(selectedPosition, tripList.size)
            //delete from database
            GeoDBModel().delete { equalTo("geoTrackingId", geoTrackingId) }
        }
        if (isOnGoingTrip){
            val geoDBModel = GeoDBModel()
            geoDBModel.apply {
                this.isTripOngoing = false
            }
            isOnGoingTrip=geoDBModel.isTripOngoing
            val intent = Intent(this@GeoTrackingActivity, GeoTrackingService::class.java)
            intent.action = GeoTrackingService.ACTION_STOP_FOREGROUND_SERVICE_TRACKING
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }
        }
        if (tripList.isEmpty() ){
            checkNoDataLayout()
        }
        val hasOngoingTrip = tripList.any { it.isOngoingTrip }
        if (!hasOngoingTrip && tripList.isNotEmpty())
            binding.btnStartNewTrip.isVisible=true
    }

}