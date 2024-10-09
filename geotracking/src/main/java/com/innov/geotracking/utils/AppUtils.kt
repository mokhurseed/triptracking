package com.innov.geotracking.utils

import android.content.Context
import android.location.Geocoder
import android.util.Log
import android.view.View
import android.widget.Toast
import com.innov.geotracking.Constant
import com.innov.geotracking.base.BaseApplication
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.innov.geotracking.BuildConfig

class AppUtils {

    lateinit var preferenceUtils: PreferenceUtils
    var toast: Toast? = null

    companion object {
        var INSTANCE: AppUtils? = null

        fun setInstance() {
            if (INSTANCE == null) {
                INSTANCE = AppUtils()
            }

        }

    }

    fun showFadeView(view: View, duration: Long) {
        view.animate().alpha(1.0f).duration = duration
    }

    fun hideFadeView(view: View, duration: Long) {
        view.animate().alpha(0.2f).duration = duration
    }

    fun showView(view: View, duration: Long) {
        view.animate().alpha(1.0f).duration = 400
    }

    fun hideView(view: View, duration: Long) {
        view.animate().alpha(0f).duration = 400
    }

    fun setLang(context: Context) {
        when (preferenceUtils.getValue(Constant.SELECTED_LANGUAGE)) {
            Constant.Languages.Hindi -> {
                LocaleHelper.setLocale(context, Constant.Languages.Hindi)
                preferenceUtils.setValue(Constant.SELECTED_LANGUAGE, Constant.Languages.Hindi)
            }

            Constant.Languages.Marathi -> {
                LocaleHelper.setLocale(context, Constant.Languages.Marathi)
                preferenceUtils.setValue(Constant.SELECTED_LANGUAGE, Constant.Languages.Marathi)
            }

            Constant.Languages.Gujarati -> {
                LocaleHelper.setLocale(context, Constant.Languages.Gujarati)
                preferenceUtils.setValue(Constant.SELECTED_LANGUAGE, Constant.Languages.Gujarati)
            }

            else -> {
                LocaleHelper.setLocale(context, Constant.Languages.English)
                preferenceUtils.setValue(Constant.SELECTED_LANGUAGE, Constant.Languages.English)
            }
        }
    }

    fun showLongToast(context: Context, msg:String){
        Toast.makeText(context,msg, Toast.LENGTH_LONG).show()
    }

    fun getLocationName(context: Context, latitude: Double, longitude: Double): String {
        val geocoder = Geocoder(context, Locale.getDefault())
        return try {
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            if (addresses?.isNotEmpty() == true) {
                addresses[0].getAddressLine(0) ?: "Unknown Location"
            } else {
                "Unknown Location"
            }
        } catch (e: Exception) {
            e.printStackTrace()
            "Unknown Location"
        }
    }

    fun logMe(tag: String, message: String?) {
        if (BuildConfig.DEBUG) {
            Log.e(tag, message ?: "")
        }
    }

    fun showToast(msg: String, isLong: Boolean = false) {
        toast = Toast.makeText(
            BaseApplication.mContext,
            msg,
            if (isLong) Toast.LENGTH_LONG else Toast.LENGTH_SHORT
        )
        toast?.show()

    }

    fun getCurrentTime(): String {
        val sdf = SimpleDateFormat("hh:mm")
        return sdf.format(Date())
    }

    fun getCurrentDate(): String {
        val dateFormat: DateFormat = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)
        val date = Date()
        return "${dateFormat.format(date)}"
    }

}