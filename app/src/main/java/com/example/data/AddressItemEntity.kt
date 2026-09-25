package com.example.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "address_items",
    indices = [Index(value = ["type", "district", "upazila", "name"], unique = true)]
)
data class AddressItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: String, // "POST_OFFICE" or "VILLAGE"
    val district: String,
    val upazila: String,
    val name: String
)
