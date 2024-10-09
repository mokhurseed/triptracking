package com.innov.geotracking

class Constant {

    companion object {
        const val VIEW_ANIMATE_DURATION = 400L
        const val SELECTED_LANGUAGE = "SELECTED_LANGUAGE"
        const val DATABASE_NAME="DATABASE_NAME"
        const val SOURCE_ACTIVITY = "SOURCE_ACTIVITY"
        const val GEO_TRACKING_ACTIVITY = "GeoTrackingActivity"
        const val SELECTED_TRIP_ID = "SELECTED_TRIP_ID"
        const val ON_GOING_GEO_TRACKING_ACTIVITY = "ON_GOING_GEO_TRACKING_ACTIVITY"
        const val EXISTING_GEO_TRACKING_ACTIVITY = "EXISTING_GEO_TRACKING_ACTIVITY"
        const val IS_FROM_NOTIFICATION_FLOW ="IS_FROM_NOTIFICATION_FLOW"
        const val PRIMARY_KEY = "PRIMARY_KEY"
        const val GEO_TRIP_NAME="GEO_TRIP_NAME"
        const val MARK_ATTENDANCE_FRAGMENT = "MarkAttendanceFragment"
        const val LAST_TRIP_ID = "LAST_TRIP_ID"
        const val MARK_ATTENDANCE_MAP_ACTIVITY = "MarkAttendanceMapActivity"
    }

    class Languages {
        companion object {
            const val English = "en"
            const val Hindi = "hi"
            const val Marathi = "mr"
            const val Gujarati = "gu"
        }
    }

    class PermissionRequestCodes {
        companion object {
            const val PERMISSION_REQUEST_ACCESS_FINE_LOCATION = 11000
            const val REQUEST_CODE_CHECK_SETTINGS = 1000
            const val STORAGE_PERMISSION_CODE = 100
            const val CAMERA_PERMISSION_CODE = 101
            const val CALL_PHONE_PERMISSION_CODE = 102
            const val CHANNEL_ID = "104"
            const val NOTIFICATION_PERMISSION_CODE =103
            const val LOCATIONS="LOCATIONS"
            const val SERVICE_LOCATION_REQUEST_CODE=105
            const val ACTION_START_FOREGROUND_SERVICE = "ACTION_START_FOREGROUND_SERVICE"

        }
    }


    class AskedPermission {
        companion object {

            const val LOCATION_PERMISSION_COUNT = "LOCATION_PERMISSION_COUNT"

        }
    }

    class IntentExtras {
        companion object {
            const val EXTRA_LATITUDE = "latitude"
            const val EXTRA_LONGITUDE = "longitude"
            const val EXTRA_PICKUP_ADDRESS = "pickup_address"
            const val EXTRA_FILE_NAME = "EXTRA_FILE_NAME"
            const val EXTRA_FILE_PATH = "EXTRA_FILE_PATH"
            const val EXTRA_IMAGE_BIT_MAP = "EXTRA_IMAGE_BIT_MAP"
            const val IS_MATCHED = "IS_MATCHED"
            const val PACKAGE = "package"
            const val TITLE = "title"
            const val GEO_TRACKING_ID = "GEO_TRACKING_ID"
            const val IS_FROM_NOTIFICATION_FLOW ="IS_FROM_NOTIFICATION_FLOW"

        }
    }
}