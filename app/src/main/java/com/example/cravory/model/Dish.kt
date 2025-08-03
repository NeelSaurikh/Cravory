package com.example.cravory.model


data class Dish(
    val id: String,
    val name: String,
    val image_url: String,
    val price: String,
    val rating: String,
    val cuisineId: String? = null // Add cuisineId
)
