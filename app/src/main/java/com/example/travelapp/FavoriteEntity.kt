package com.example.travelapp


import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey val name: String,
    val address: String,
    val rating: Double
)