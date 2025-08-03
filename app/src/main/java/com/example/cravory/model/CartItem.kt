package com.example.cravory.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cart_items")
data class CartItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val cuisineId: String,
    val itemId: String,
    val itemPrice: Double,
    val itemQuantity: Int
)