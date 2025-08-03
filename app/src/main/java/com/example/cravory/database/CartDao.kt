package com.example.cravory.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.cravory.model.CartItem

@Dao
interface CartDao {
    @Insert
    suspend fun addItem(item: CartItem)

    @Query("SELECT * FROM cart_items")
    suspend fun getAllItems(): List<CartItem>

    @Query("UPDATE cart_items SET itemQuantity = :quantity WHERE itemId = :itemId")
    suspend fun updateQuantity(itemId: String, quantity: Int)

    @Query("DELETE FROM cart_items WHERE itemId = :itemId")
    suspend fun removeItem(itemId: String)

    @Query("DELETE FROM cart_items")
    suspend fun clearCart()
}