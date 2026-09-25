package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AddressDao {
    @Query("SELECT DISTINCT name FROM address_items WHERE type = :type AND district = :district AND upazila = :upazila ORDER BY name ASC")
    fun getAddressNames(type: String, district: String, upazila: String): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAddressItem(item: AddressItemEntity)
}
