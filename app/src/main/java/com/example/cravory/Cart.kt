package com.example.cravory

import com.example.cravory.model.Dish
import com.example.cravory.model.CartItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object Cart {
    suspend fun addItem(dish: Dish, quantity: Int) = withContext(Dispatchers.IO) {
        if (quantity <= 0) return@withContext // Ignore invalid quantities

        val cartItem = CartItem(
            cuisineId = dish.cuisineId ?: "", // Ensure cuisineId is passed or set
            itemId = dish.id,
            itemPrice = dish.price.toDoubleOrNull() ?: 0.0,
            itemQuantity = quantity
        )

        val dao = CravoryApp.database.cartDao()
        val existingItem = dao.getAllItems().find { it.itemId == dish.id }
        if (existingItem != null) {
            // Update quantity if item exists
            dao.updateQuantity(dish.id, existingItem.itemQuantity + quantity)
        } else {
            // Add new item
            dao.addItem(cartItem)
        }
    }
}