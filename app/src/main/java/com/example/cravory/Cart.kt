package com.example.cravory

import com.example.cravory.model.Dish


object Cart {
    private val items = mutableListOf<CartItem>()

    data class CartItem(val dish: Dish, val quantity: Int)

    fun addItem(dish: Dish, quantity: Int) {
        items.add(CartItem(dish, quantity))
    }

    fun getItems(): List<CartItem> = items
}