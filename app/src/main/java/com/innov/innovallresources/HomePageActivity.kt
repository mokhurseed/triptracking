package com.innov.innovallresources

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.innov.geotracking.presentation.geo_tracking.GeoTrackingActivity
import com.innov.innovallresources.databinding.ActivityHomePageBinding

class HomePageActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomePageBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomePageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.apply {

            btnOk.setOnClickListener {

                val intent = Intent(this@HomePageActivity, GeoTrackingActivity::class.java)
                startActivity(intent)

            }
        }


    }


}