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

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAddressItems(items: List<AddressItemEntity>)

    @Query("SELECT * FROM address_items ORDER BY district, upazila, name ASC")
    suspend fun getAllAddressItems(): List<AddressItemEntity>

    @Query("DELETE FROM address_items")
    suspend fun deleteAllAddressItems()
}
