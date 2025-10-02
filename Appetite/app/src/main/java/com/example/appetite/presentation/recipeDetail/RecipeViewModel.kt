package com.example.appetite.presentation.recipeDetail

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appetite.R
import com.example.appetite.models.Profile
import com.example.appetite.models.Recipe
import com.example.appetite.models.RecipeFire
import com.example.appetite.repository.AuthRepository
import com.example.appetite.repository.RecipeRepository
import com.example.appetite.repository.UserInfoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecipeViewModel @Inject constructor() : ViewModel() {
    val featuredRecipes = listOf(
        Recipe("Classic Greek Salad", 15, 4.5, "James", R.drawable.dish1),
        Recipe("Crunchy Nut Coleslaw", 10, 3.5, "Sarah", R.drawable.dish2),
        Recipe("Crunchy Nut Coleslaw", 10, 3.5, "John", R.drawable.dish3)

    )
    val contributor: MutableState<Profile?> = mutableStateOf(null)

    val user: MutableState<String> = mutableStateOf("")

    init {
        loadID()
    }

    val selectedRecipe: MutableState<Recipe?> = mutableStateOf(null)
    val selectedRecipeFire: MutableState<RecipeFire?> = mutableStateOf(null)
    val isLoading: MutableState<Boolean> = mutableStateOf(false)
    
    fun selectRecipeFire(recipe: RecipeFire) { selectedRecipeFire.value = recipe }
    
    fun loadRecipeById(id: String) {
        viewModelScope.launch {
            try {
                isLoading.value = true
                // Clear previous state to avoid showing stale data during transitions
                selectedRecipe.value = null
                selectedRecipeFire.value = null
                contributor.value = null
                println("RecipeViewModel: Loading recipe by ID: $id")
                val recipe = RecipeRepository.getRecipeById(id)
                selectedRecipeFire.value = recipe
                recipe?.contributorId?.let { loadContributor(it) } // 👈 fetch chef
            } catch (e: Exception) {
                println("RecipeViewModel: Error loading recipe by ID: ${e.message}")
                e.printStackTrace()
            } finally {
                isLoading.value = false
            }
        }
    }
    
    fun clearSelectedRecipes() {
        selectedRecipe.value = null
        selectedRecipeFire.value = null
        contributor.value = null
    }
    // RecipeViewModel.kt
    suspend fun getMyRating(recipeId: String): Int? {
        return RecipeRepository.getMyRating(recipeId)
    }

    fun rateRecipe(recipeId: String, rating: Int) {
        viewModelScope.launch {
            RecipeRepository.rateRecipe(recipeId, rating)
        }
    }
    fun loadContributor(userId: String) {
        viewModelScope.launch {
            try {
                val user = UserInfoRepository.getUserInfo(userId)
                contributor.value = user
            } catch (e: Exception) {
                println("Error loading contributor: ${e.message}")
            }
        }
    }

    fun deleteRecipe(recipeId: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = RecipeRepository.deleteRecipe(recipeId)
            onResult(success)
        }
    }
    fun loadID()  {
        viewModelScope.launch {
            val id = AuthRepository.fetchMe().uid
            user.value = id
        }
    }
}