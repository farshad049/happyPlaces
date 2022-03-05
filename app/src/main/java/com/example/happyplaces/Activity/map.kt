package com.example.happyplaces.Activity

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.happyplaces.Database.HappyPlaceEntity
import com.example.happyplaces.R

import com.example.happyplaces.databinding.ActivityMapBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class map : AppCompatActivity(), OnMapReadyCallback {
    private var binding: ActivityMapBinding?=null
    var happyPlaceDetailEntity: HappyPlaceEntity?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding?.root)


        if (intent.hasExtra(MainActivity.EXTRA_PLACE_DETAILS)){
            happyPlaceDetailEntity=intent.getParcelableExtra(MainActivity.EXTRA_PLACE_DETAILS) as HappyPlaceEntity?
        }

        if (happyPlaceDetailEntity != null){
            setSupportActionBar(binding?.toolbarMap)
            if (supportActionBar !=null){
                supportActionBar?.setDisplayHomeAsUpEnabled(true)
                supportActionBar?.title = happyPlaceDetailEntity!!.title
                binding?.toolbarMap?.setNavigationOnClickListener {
                    onBackPressed()
                    }
                }
            val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
                mapFragment.getMapAsync(this)
            }



    }//FUN

    override fun onMapReady(googleMap: GoogleMap) {

        /**
         * Add a marker on the location using the latitude and longitude and move the camera to it.
         */
        val position = LatLng(
            happyPlaceDetailEntity!!.latitude,
            happyPlaceDetailEntity!!.longitude
        )
        googleMap.addMarker(MarkerOptions().position(position).title(happyPlaceDetailEntity!!.location))
        val newLatLngZoom = CameraUpdateFactory.newLatLngZoom(position, 15f)
        googleMap.animateCamera(newLatLngZoom)
    }


}