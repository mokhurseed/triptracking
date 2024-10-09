package com.innov.geotracking.presentation.geo_tracking.model

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.io.Serializable
import com.google.android.gms.maps.model.LatLng
open class GeoDBModel  : RealmObject() {

    @PrimaryKey
    var geoTrackingId: String? = ""
    var timeStamp: String = ""
    var geoTripName:String=""
    var latLonList: RealmList<LatLonModel> = RealmList()
    var isTripOngoing: Boolean = false
    var source: String = ""
    var destination: String = ""
    var isDataSynced  :Boolean = false
}

open class LatLonModel : RealmObject(), Serializable {
    var latitude: Double = 0.0
    var longitude: Double = 0.0
}

data class LatLon(
    var latitude: Double = 0.0, var longitude: Double = 0.0
) : Serializable

data class LatLngData(
    val latLngList: MutableList<LatLng>, val lastLatLng: LatLng? = null
)

