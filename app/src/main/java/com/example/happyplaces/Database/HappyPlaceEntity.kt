package com.example.happyplaces.Database

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize
import java.io.Serializable
@Parcelize
@Entity(tableName = "HappyPlaces-table")
data class HappyPlaceEntity(
    @PrimaryKey (autoGenerate = true)    val id:Int=0,
    val title:String,
    val image:String,
    val description:String,
    val date:String,
    val location:String,
    val latitude:Double,
    val longitude:Double

    // we set the entity to Parcelable, so we can put in intent extra
):Parcelable


