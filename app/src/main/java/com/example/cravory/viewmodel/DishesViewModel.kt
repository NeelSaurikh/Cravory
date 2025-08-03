package com.example.cravory.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cravory.model.Dish
import com.example.cravory.repository.FoodRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DishesViewModel : ViewModel() {
    private val repository = FoodRepository()

    private val _uiState = MutableStateFlow<DishesUiState>(DishesUiState.Loading)
    val uiState: StateFlow<DishesUiState> = _uiState

    fun fetchDishes(cuisineId: String, sortBy: String? = null) {
        viewModelScope.launch {
            _uiState.value = DishesUiState.Loading
            Log.d("DishesViewModel", "Fetching dishes for cuisineId: $cuisineId, sortBy: $sortBy")
            try {
                var dishes = repository.getDishesForCuisine(cuisineId)
                dishes = when (sortBy) {
                    "price" -> dishes.sortedBy { it.price.toIntOrNull() ?: 0 }
                    "rating" -> dishes.sortedByDescending { it.rating.toFloatOrNull() ?: 0f }
                    "name" -> dishes.sortedBy { it.name }
                    else -> dishes
                }
                Log.d("DishesViewModel", "Fetched ${dishes.size} dishes")
                _uiState.value = DishesUiState.Success(dishes)
            } catch (e: Exception) {
                Log.e("DishesViewModel", "Error fetching dishes: ${e.message}")
                _uiState.value = DishesUiState.Error(e.message ?: "Failed to fetch dishes")
            }
        }
    }
}

sealed class DishesUiState {
    object Loading : DishesUiState()
    data class Success(val dishes: List<Dish>) : DishesUiState()
    data class Error(val message: String) : DishesUiState()
}