package com.example.happyplaces.Activity

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.example.happyplaces.Database.HappyPlaceEntity
import com.example.happyplaces.databinding.ActivityHappyPlaceDetailAvtivityBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class HappyPlaceDetailActivity : AppCompatActivity() {
    private var binding:ActivityHappyPlaceDetailAvtivityBinding?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHappyPlaceDetailAvtivityBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        var happyPlaceDetailEntity:HappyPlaceEntity?=null
        if (intent.hasExtra(MainActivity.EXTRA_PLACE_DETAILS)){
            happyPlaceDetailEntity=intent.getParcelableExtra(MainActivity.EXTRA_PLACE_DETAILS) as HappyPlaceEntity?
        }

        if (happyPlaceDetailEntity != null){
            setSupportActionBar(binding?.toolbarHappyPlaceDetail)
            if (supportActionBar !=null){

                    supportActionBar?.setDisplayHomeAsUpEnabled(true)
                    supportActionBar?.title = happyPlaceDetailEntity.title
                    binding?.toolbarHappyPlaceDetail?.setNavigationOnClickListener {
                        onBackPressed()
                    }
                    binding?.ivDetail?.setImageURI(Uri.parse(happyPlaceDetailEntity.image))
                    binding?.tvDetailDescription?.text = happyPlaceDetailEntity.description
                    binding?.tvDetailLocation?.text = happyPlaceDetailEntity.location
                    binding?.btnViwOnMap?.setOnClickListener {
                    val intent = Intent(this, map::class.java)
                    intent.putExtra(MainActivity.EXTRA_PLACE_DETAILS, happyPlaceDetailEntity)
                    startActivity(intent)
                }
            }


        }







    }//FUN







}