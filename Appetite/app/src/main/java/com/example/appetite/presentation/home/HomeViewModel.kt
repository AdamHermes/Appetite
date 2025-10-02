package com.example.appetite.presentation.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appetite.models.RecipeFire
import com.example.appetite.repository.RecipeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject



@HiltViewModel
class HomeViewModel @Inject constructor() : ViewModel() {

    val categories = listOf(
        "All",
        "Indian",
        "Italian",
        "Chinese",
        "British",
        "American",
        "Filipino",
        "Russian",
        "French",
        "Others"
    )

    private val _allRecipes = MutableStateFlow<List<RecipeFire>>(emptyList()) // keep raw recipes
    private val _newRecipes = MutableStateFlow<List<RecipeFire>>(emptyList())
    val newRecipes: StateFlow<List<RecipeFire>> = _newRecipes.asStateFlow()

    private val _forYouRecipes = MutableStateFlow<List<RecipeFire>>(emptyList())
    val forYouRecipes: StateFlow<List<RecipeFire>> = _forYouRecipes.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _mostSavedRecipes = MutableStateFlow<List<RecipeFire>>(emptyList())
    val mostSavedRecipes: StateFlow<List<RecipeFire>> = _mostSavedRecipes.asStateFlow()
    init {
        loadHomeScreen()
    }

    private fun loadHomeScreen() {
        viewModelScope.launch {
            try {
                Log.d("HomeViewModel", "Loading recipes from repository...")
                val recipes = RecipeRepository.getAllRecipes()
                _allRecipes.value = recipes

                // New recipes → sorted by createdAt
                _newRecipes.value = recipes.sortedByDescending { it.createdAt ?: Date(0) }

                // Default "For You" → randomized
                _forYouRecipes.value = recipes.shuffled()

                _mostSavedRecipes.value = recipes.sortedByDescending { it.saveCount }
                Log.d("HomeViewModel", "Loaded ${recipes.size} recipes")

            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error loading recipes", e)
            }
        }
    }


    fun updateCategory(category: String) {
        _selectedCategory.value = category

        viewModelScope.launch {
            val recipes = _allRecipes.value
            recipes.forEach {
                Log.d("HomeViewModel", "Filtering by category=$category, recipe.area=${it.area}")
            }
            _forYouRecipes.value = if (category == "All") {
                recipes.shuffled()
            } else if (category == "Others") {
                recipes.filter { it.area !in categories }.shuffled()
            } else {
                recipes.filter { it.area == category }.shuffled()
            }
        }
    }
    fun clearState() {
        _selectedCategory.value = "All" // or default category
    }

}





