package com.example.happyplaces.Database

import android.app.Application

class happyPlacesApp: Application() {
    val db by lazy { HappyPlacesDatabase.getInstance(this) }
}