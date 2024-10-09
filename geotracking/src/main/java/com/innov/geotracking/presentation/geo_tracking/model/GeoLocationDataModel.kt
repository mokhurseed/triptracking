package com.innov.geotracking.presentation.geo_tracking.model

import androidx.annotation.DrawableRes

data class GeoLocationDataModel(
    val tripId: String? = "",
    val tripName: String? = "",
    val tripDate: String? = "",
    val fromLocation: String? = "",
    val toLocation: String? = "",
    val distance: String? = "",
    val isOngoingTrip: Boolean = false
)

data class VehicleTypes(
    val type: String? = "",
    @DrawableRes val imageResId: Int,
    var isSelected: Boolean = false
)