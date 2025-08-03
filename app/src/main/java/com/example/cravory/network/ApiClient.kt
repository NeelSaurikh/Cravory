package com.example.cravory.network

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.example.cravory.model.CartItem
import com.example.cravory.model.Dish
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.math.RoundingMode
import java.net.HttpURLConnection
import java.net.URL

object ApiClient {
    private const val BASE_URL = "https://uat.onebanc.ai"
    private const val API_KEY = "uonebancservceemultrS3cg8RaL30"

    fun getItemList(page: Int, count: Int): String {
        val body = JSONObject().apply {
            put("page", page)
            put("count", count)
        }.toString()
        return makePostRequest("/emulator/interview/get_item_list", "get_item_list", body)
    }

    suspend fun getItemList(page: Int, count: Int, cuisineId: String? = null): List<Dish> = withContext(Dispatchers.IO) {
        val body = JSONObject().apply {
            put("page", page)
            put("count", count)
        }.toString()
        val response = makePostRequest("/emulator/interview/get_item_list", "get_item_list", body)
        parseDishesResponse(response, cuisineId)
    }

    suspend fun getItemByFilter(cuisineTypes: List<String>? = null, minRating: Float? = null, minPrice: Int? = null, maxPrice: Int? = null): List<Dish> = withContext(Dispatchers.IO) {
        val body = JSONObject().apply {
            cuisineTypes?.let { put("cuisine_type", JSONArray(it)) }
            minRating?.let { put("min_rating", it) }
            if (minPrice != null && maxPrice != null) {
                put("price_range", JSONObject().apply {
                    put("min_amount", minPrice)
                    put("max_amount", maxPrice)
                })
            }
        }.toString()

        val response = makePostRequest("/emulator/interview/get_item_by_filter", "get_item_by_filter", body)
        parseDishesResponse(response)
    }

    fun makePostRequest(endpoint: String, action: String, body: String): String {
        val TAG = "ApiClient"
        val url = URL("$BASE_URL$endpoint")
        val connection = url.openConnection() as HttpURLConnection

        Log.d(TAG, "Making POST request to: $url")
        Log.d(TAG, "Action: $action")
        Log.d(TAG, "Request Body: $body")

        return try {
            connection.requestMethod = "POST"
            connection.setRequestProperty("X-Partner-API-Key", API_KEY)
            connection.setRequestProperty("X-Forward-Proxy-Action", action)
            connection.setRequestProperty("Content-Type", "application/json")
            connection.doOutput = true

            connection.outputStream.use { it.write(body.toByteArray()) }

            val responseCode = connection.responseCode
            val response = when (responseCode) {
                200 -> connection.inputStream.bufferedReader().use { it.readText() }
                else -> {
                    val errorText = connection.errorStream?.bufferedReader()?.readText()
                    Log.e(TAG, "API Error [$responseCode]: $errorText")
                    throw Exception(errorText ?: "Error $responseCode")
                }
            }

            Log.d(TAG, "API Response: $response")
            response
        } catch (e: Exception) {
            Log.e(TAG, "API call failed: ${e.message}", e)
            throw e
        } finally {
            connection.disconnect()
        }
    }



    private val imageCache = mutableMapOf<String, Bitmap>()

    suspend fun loadImage(url: String): Bitmap? = withContext(Dispatchers.IO) {
        imageCache[url]?.let { return@withContext it }

        try {
            val connection = URL(url).openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 5000
            connection.readTimeout = 5000
            connection.doInput = true
            connection.connect()

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val inputStream = connection.inputStream
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream.close()
                bitmap?.let { imageCache[url] = it }
                bitmap
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun parseDishesResponse(response: String, cuisineId: String? = null): List<Dish> {
        val json = JSONObject(response)
        if (json.getInt("response_code") != 200) {
            throw Exception(json.getString("response_message"))
        }

        val cuisines = json.getJSONArray("cuisines")
        val dishes = mutableListOf<Dish>()

        for (i in 0 until cuisines.length()) {
            val cuisine = cuisines.getJSONObject(i)
            val currentCuisineId = cuisine.getString("cuisine_id")
            if (cuisineId == null || currentCuisineId == cuisineId) {
                val items = cuisine.getJSONArray("items")
                for (j in 0 until items.length()) {
                    val item = items.getJSONObject(j)
                    dishes.add(
                        Dish(
                            id = item.getString("id"),
                            name = item.getString("name"),
                            image_url = item.getString("image_url"),
                            price = item.optString("price", "0"),
                            rating = item.optString("rating", "0.0"),
                            cuisineId = currentCuisineId
                        )
                    )
                }
            }
        }
        return dishes
    }

    suspend fun getItemById(itemId: String): Dish? = withContext(Dispatchers.IO) {
        val body = JSONObject().apply {
            put("item_id", itemId)
        }.toString()
        try {
            val response = makePostRequest("/emulator/interview/get_item_by_id", "get_item_by_id", body)
            val json = JSONObject(response)
            if (json.getInt("response_code") == 200) {
                Dish(
                    id = json.getString("item_id"),
                    name = json.getString("item_name"),
                    image_url = json.getString("item_image_url"),
                    price = json.optString("item_price", "0"),
                    rating = json.optString("item_rating", "0.0"),
                    cuisineId = json.getString("cuisine_id")
                )
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("ApiClient", "getItemById failed: ${e.message}", e)
            null
        }
    }

    suspend fun makePayment(items: List<CartItem>): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        try {
            // Calculate net total as integer to avoid decimals
            val totalAmount = items.sumOf { it.itemPrice.toInt() * it.itemQuantity }
            val totalItems = items.sumOf { it.itemQuantity }
            val body = JSONObject().apply {
                put("total_amount", totalAmount.toString())
                put("total_items", totalItems)
                put("data", JSONArray().apply {
                    items.forEach { item ->
                        if (item.cuisineId.isEmpty()) {
                            throw IllegalArgumentException("Missing cuisine_id for item ${item.itemId}")
                        }
                        put(JSONObject().apply {
                            put("cuisine_id", item.cuisineId.toIntOrNull() ?: item.cuisineId)
                            put("item_id", item.itemId.toIntOrNull() ?: item.itemId)
                            put("item_price", item.itemPrice.toInt())
                            put("item_quantity", item.itemQuantity)
                        })
                    }
                })
            }.toString()

            Log.d("ApiClient", "Payment Request Body: $body")
            val response = makePostRequest("/emulator/interview/make_payment", "make_payment", body)
            val json = JSONObject(response)
            if (json.getInt("response_code") == 200) {
                Pair(true, "Order placed: ${json.getString("txn_ref_no")}")
            } else {
                val errorMessage = json.getString("response_message")
                val errorDetails = json.optString("error_details", errorMessage)
                Pair(false, "$errorMessage: $errorDetails")
            }
        } catch (e: Exception) {
            val errorMessage = when {
                e.message?.contains("400") == true -> "Invalid request. Check cart items."
                e.message?.contains("404") == true -> "Item or cuisine not found."
                e.message?.contains("500") == true -> "Server error. Try again later."
                else -> "Error: ${e.message}"
            }
            Log.e("ApiClient", "makePayment failed: $errorMessage", e)
            Pair(false, errorMessage)
        }
    }
}