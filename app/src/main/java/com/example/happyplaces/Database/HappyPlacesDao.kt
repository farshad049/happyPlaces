package com.example.happyplaces.Database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface HappyPlacesDao {

    @Insert
    suspend fun insert(happyPlaceEntity: HappyPlaceEntity)

    @Update
    suspend fun update(happyPlaceEntity: HappyPlaceEntity)

    @Delete
    suspend fun delete(happyPlaceEntity: HappyPlaceEntity)

    @Query("SELECT * FROM `HappyPlaces-table`")
    fun fetchAllHappyPlaces(): Flow<List<HappyPlaceEntity>>

    @Query("SELECT * FROM `HappyPlaces-table` WHERE ID=:id")
    fun fetchHappyPlaces(id :Int): Flow<HappyPlaceEntity>
}