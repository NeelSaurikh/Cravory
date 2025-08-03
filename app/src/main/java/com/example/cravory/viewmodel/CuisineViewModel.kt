package com.example.cravory.viewmodel

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.cravory.model.Cuisine
import com.example.cravory.repository.FoodRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CuisineViewModel(
    private val repository: FoodRepository
) : ViewModel() {

    private val _cuisines = mutableStateListOf<Cuisine>()
    val cuisines: List<Cuisine> get() = _cuisines

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private var currentPage = 1
    private var totalPages = Int.MAX_VALUE // Default until first fetch
    private var isFetching = false
    private val pageSize = 10

    init {
        Log.d("CuisineViewModel", "ViewModel Created")
        loadCuisines()
    }

    fun loadCuisines() {
        Log.d(
            "CuisineViewModel",
            "loadCuisines called | page=$currentPage | isFetching=$isFetching"
        )

        if (isFetching || currentPage > totalPages) {
            Log.d("CuisineViewModel", "loadCuisines skipped")
            return
        }

        viewModelScope.launch {
            isFetching = true
            _isLoading.value = true
            try {
                val response = repository.getCuisines(page = currentPage, count = pageSize)

                val newCuisines = response.cuisines
                val existingCuisineNames = _cuisines.map { it.cuisineId }.toSet()
                val filteredCuisines = newCuisines.filter { it.cuisineId !in existingCuisineNames }

                _cuisines.addAll(filteredCuisines)

                totalPages = response.totalPages
                currentPage++
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load cuisines"
            } finally {
                _isLoading.value = false
                isFetching = false
            }
        }
    }
}

class CuisineViewModelFactory(private val repository: FoodRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CuisineViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CuisineViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}