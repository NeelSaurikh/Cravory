package com.example.cravory.model

data class DishWithCuisine( // New model for UI
    val dishId: Long,
    val dishName: String,
    val cuisineName: String,
    val cuisineImageUrl: String
)