package com.example.cravory

import com.example.cravory.model.Cuisine
import com.example.cravory.model.Dish
import org.json.JSONObject


object JsonParser {
    data class CuisineResponse(
        val cuisines: List<Cuisine>,
        val totalPages: Int
    ) : List<Cuisine> {
        override fun contains(element: Cuisine): Boolean {
            TODO("Not yet implemented")
        }

        override fun containsAll(elements: Collection<Cuisine>): Boolean {
            TODO("Not yet implemented")
        }

        override fun get(index: Int): Cuisine {
            TODO("Not yet implemented")
        }

        override fun indexOf(element: Cuisine): Int {
            TODO("Not yet implemented")
        }

        override fun isEmpty(): Boolean {
            TODO("Not yet implemented")
        }

        override fun iterator(): Iterator<Cuisine> {
            TODO("Not yet implemented")
        }

        override fun lastIndexOf(element: Cuisine): Int {
            TODO("Not yet implemented")
        }

        override fun listIterator(): ListIterator<Cuisine> {
            TODO("Not yet implemented")
        }

        override fun listIterator(index: Int): ListIterator<Cuisine> {
            TODO("Not yet implemented")
        }

        override fun subList(
            fromIndex: Int,
            toIndex: Int,
        ): List<Cuisine> {
            TODO("Not yet implemented")
        }

        override val size: Int
            get() = TODO("Not yet implemented")
    }

    fun parseCuisines(jsonString: String): CuisineResponse {
        val json = JSONObject(jsonString)
        val cuisinesArray = json.getJSONArray("cuisines")
        val totalPages = json.getInt("total_pages")
        val cuisines = mutableListOf<Cuisine>()

        for (i in 0 until cuisinesArray.length()) {
            val cuisineJson = cuisinesArray.getJSONObject(i)
            val itemsArray = cuisineJson.getJSONArray("items")
            val items = mutableListOf<Dish>()

            for (j in 0 until itemsArray.length()) {
                val itemJson = itemsArray.getJSONObject(j)
                items.add(
                    Dish(
                        id = itemJson.getString("id"),
                        name = itemJson.getString("name"),
                        image_url = itemJson.getString("image_url"),
                        price = itemJson.getString("price"),
                        rating = itemJson.getString("rating")
                    )
                )
            }

            cuisines.add(
                Cuisine(
                    cuisineId = cuisineJson.getString("cuisine_id"),
                    cuisineName = cuisineJson.getString("cuisine_name"),
                    cuisineImageUrl = cuisineJson.getString("cuisine_image_url"),
                    items = items
                )
            )
        }

        return CuisineResponse(cuisines, totalPages)
    }
}