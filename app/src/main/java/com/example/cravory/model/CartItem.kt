package com.example.cravory.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cart_items")
data class CartItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0, // Auto-incremented ID
    val cuisineId: String, // e.g., "234552"
    val itemId: String, // e.g., "34234234"
    val itemPrice: Double, // e.g., 199.0
    val itemQuantity: Int // e.g., 1
)
