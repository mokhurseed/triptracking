package com.innov.geotracking.presentation.geo_tracking.bottomsheets

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.InputFilter
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.innov.geotracking.Constant
import com.innov.geotracking.R
import com.innov.geotracking.databinding.FragmentGeoTrackingNewTripBottomsheetBinding
import com.innov.geotracking.presentation.geo_tracking.adapter.VehicleOptionsAdapter
import com.innov.geotracking.presentation.geo_tracking.model.VehicleTypes
import com.innov.geotracking.services.GeoTrackingService
import com.innov.geotracking.utils.AppUtils
import com.innov.geotracking.utils.PreferenceUtils


class GeoTrackingNewTripBottomSheet(private val listener: StartButtonClickListener) :
    BottomSheetDialogFragment(), VehicleOptionsAdapter.SelectVehicleListener {
    private lateinit var binding: FragmentGeoTrackingNewTripBottomsheetBinding
    private var isServiceOngoing: Boolean = false
    private var geoTripName: String? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            FragmentGeoTrackingNewTripBottomsheetBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        this.isCancelable = false
        setUpView()
        setUpListeners()
        setUpRV()

    }

    private fun setUpRV() {
        val vehicleTypes = listOf(
            VehicleTypes(getString(R.string.two_wheeler), R.drawable.ic_bike),
            VehicleTypes(getString(R.string.four_wheeler), R.drawable.ic_car)
        )
        val adapter = VehicleOptionsAdapter(this, vehicleTypes)
        binding.rvVehicleType.adapter = adapter
    }
    private fun getNextTripId(context: Context): Int {
        val sharedPreferences = PreferenceUtils(context)
        val lastId = sharedPreferences.getIntValue(Constant.LAST_TRIP_ID, 0) // Get last ID, default to 0
        val newId = lastId + 1

        // Save the new ID in SharedPreferences using the utility method
        sharedPreferences.setValue(Constant.LAST_TRIP_ID, newId)

        return newId
    }



    private fun setUpListeners() {
        binding.apply {
            ivClose.setOnClickListener {
                dismiss()
            }
            layoutButtons.commonButton.setOnClickListener {

                if (checkValidations()) {
                    listener.onStartButtonClicked()
                    val intent = Intent(requireActivity(), GeoTrackingService::class.java)
                    intent.action = Constant.PermissionRequestCodes.ACTION_START_FOREGROUND_SERVICE
                    intent.putExtra(Constant.GEO_TRIP_NAME, geoTripName)
                    val primaryKey = context?.let { it1 -> getNextTripId(it1).toString() }
                    if (primaryKey != null) {
                        PreferenceUtils(requireContext()).setValue(Constant.PRIMARY_KEY, primaryKey)
                    }

                    // Handle starting the service based on Android version
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        context?.startForegroundService(intent)
                    } else {
                        context?.startService(intent)
                    }

                    listener.onStartButtonClicked()
                    isServiceOngoing = true
                    dismiss()
                }

            }
        }
    }

    //check Validations
    private fun checkValidations(): Boolean {
        binding.apply {
            if (layoutTripName.etInputBox.text.toString().isEmpty()) {
                AppUtils.INSTANCE?.showToast(getString(R.string.please_enter_trip_name))
                layoutTripName.etInputBox.requestFocus()
                return false
            }
            geoTripName = layoutTripName.etInputBox.text.toString()
            return true
        }

    }

    private fun setUpView() {
        binding.apply {
//            layoutTripName.etInputBox.setBackgroundColor(resources.getColor(R.color.white))
            layoutTripName.apply {
                val customFilter = InputFilter { source, start, end, dest, dstart, dend ->
                    for (i in start until end) {
                        val char = source[i]
                        // Check if character is a lowercase letter, a space, or a special character
                        if (!char.isLowerCase() && !char.isWhitespace() && !char.isUpperCase() ) {
                            return@InputFilter ""
                        }
                    }
                    null
                }


                etInputBox.apply {
                    setHint(getString(R.string.enter_trip_here))
                    filters = arrayOf(InputFilter.LengthFilter(30),customFilter)
                    inputType = InputType.TYPE_TEXT_VARIATION_PERSON_NAME
                    setRawInputType(InputType.TYPE_TEXT_VARIATION_PERSON_NAME)
                }

                tvLabel.text = getString(R.string.trip_name)

            }

            layoutButtons.commonButton.text = getString(R.string.start)
        }

    }

    override fun onSelectVehicle(position: Int) {
    }

    interface StartButtonClickListener {
        fun onStartButtonClicked() {}
    }
}