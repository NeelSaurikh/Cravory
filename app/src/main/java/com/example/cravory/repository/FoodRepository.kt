package com.example.cravory.repository

import android.util.Log
import com.example.cravory.JsonParser
import com.example.cravory.model.CartItem
import com.example.cravory.model.Cuisine
import com.example.cravory.model.Dish
import com.example.cravory.model.PaymentItem
import com.example.cravory.model.PaymentRequest
import com.example.cravory.network.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class FoodRepository {
    private val cartItems = mutableListOf<CartItem>()

    suspend fun getCuisines(page: Int = 1, count: Int = 10): JsonParser.CuisineResponse = withContext(Dispatchers.IO) {
        val response = ApiClient.getItemList(page, count)
        JsonParser.parseCuisines(response)
    }

//    suspend fun getDishesForCuisine(cuisineId: String): List<Dish> {
//        return ApiClient.getItemList(page = 1, count = 10, cuisineId = cuisineId)
//    }

    suspend fun getDishesForCuisine(cuisineId: String): List<Dish> = withContext(Dispatchers.IO) {
        val dishes = mutableListOf<Dish>()
        var page = 1
        val count = 10
        var totalPages = 1
        var lastDishCount = 0

        do {
            Log.d("DishRepository", "Fetching page $page for cuisineId: $cuisineId")
            // Fetch dishes for the current page, filtered by cuisineId
            val response = ApiClient.getItemList(page, count, cuisineId)
            dishes.addAll(response)
            Log.d("DishRepository", "Page $page: Added ${response.size} dishes, Total: ${dishes.size}")

            // Fetch raw response to get total_pages
            val rawResponse = ApiClient.makePostRequest(
                endpoint = "/emulator/interview/get_item_list",
                action = "get_item_list",
                body = JSONObject().apply {
                    put("page", page)
                    put("count", count)
                }.toString()
            )
            val json = JSONObject(rawResponse)
            totalPages = json.optInt("total_pages", 1)
            Log.d("DishRepository", "Total pages: $totalPages")

            // Break if no new dishes were added and we already have some dishes
            if (response.isEmpty() && dishes.isNotEmpty() && lastDishCount == dishes.size) {
                break
            }
            lastDishCount = dishes.size
            page++
        } while (page <= totalPages)

        Log.d("DishRepository", "Final dishes count for cuisineId $cuisineId: ${dishes.size}")
        dishes
    }

//    suspend fun getTopDishes(): List<Dish> = withContext(Dispatchers.IO) {
//        val response = ApiClient.getItemByFilter(minRating = 4.0f)
//        JsonParser.parseCuisines(response).flatMap { it.items }.take(3)
//    }

//    fun addToCart(dish: Dish, cuisineId: String, quantity: Int) {
//        if (quantity <= 0) return
//        val existing = cartItems.find { it.itemId == dish.id }
//        if (existing != null) {
//            cartItems.remove(existing)
//            cartItems.add(existing.copy(quantity = existing.quantity + quantity))
//        } else {
//            cartItems.add(CartItem(cuisineId, dish.id, dish.name, dish.price, quantity))
//        }
//    }
//
//    fun getCartItems(): List<CartItem> = cartItems.toList()
}