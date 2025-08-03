package com.example.cravory.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cravory.model.Cuisine
import com.example.cravory.model.Dish
import com.example.cravory.repository.FoodRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeViewModel(private val repository: FoodRepository) : ViewModel() {

    private val _cartItemCount = MutableStateFlow(0)
    val cartItemCount: StateFlow<Int> = _cartItemCount
    private val _cuisines = MutableStateFlow<List<Cuisine>>(emptyList())
    val cuisines: StateFlow<List<Cuisine>> = _cuisines

    private val _topDishes = MutableStateFlow<List<Dish>>(emptyList())
    val topDishes: StateFlow<List<Dish>> = _topDishes

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        fetchCuisines()
//        fetchTopDishes()
    }

    fun fetchCuisines() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _cuisines.value = repository.getCuisines()
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load cuisines"
            } finally {
                _isLoading.value = false
            }
        }
    }
//
//    fun fetchTopDishes() {
//        viewModelScope.launch {
//            _isLoading.value = true
//            try {
//                _topDishes.value = repository.getTopDishes()
//                _error.value = null
//            } catch (e: Exception) {
//                _error.value = e.message ?: "Failed to load top dishes"
//            } finally {
//                _isLoading.value = false
//            }
//        }
//    }
//
//    fun addToCart(dish: Dish, cuisineId: String, quantity: Int) {
//        repository.addToCart(dish, cuisineId, quantity)
//    }
}