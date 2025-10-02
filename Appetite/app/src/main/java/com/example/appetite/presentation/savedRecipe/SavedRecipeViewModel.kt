package com.example.appetite.presentation.savedRecipe

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appetite.models.RecipeFire
import com.example.appetite.repository.RecipeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SavedRecipeViewModel @Inject constructor() : ViewModel() {

    // List of saved recipes
    private val _savedRecipes = MutableStateFlow<List<RecipeFire>>(emptyList())
    val savedRecipes: StateFlow<List<RecipeFire>> = _savedRecipes

    // Keep track of saved recipe IDs for quick toggle
    private val _savedIds = MutableStateFlow<Set<String>>(emptySet())
    val savedIds: StateFlow<Set<String>> = _savedIds

//    init {
//        refreshSavedRecipes()
//    }

    init {
        Log.d("SavedRecipeVM", "init -> refreshing saved recipes")
    }

    fun refreshSavedRecipes() {
        viewModelScope.launch {
            val recipes = RecipeRepository.getSavedRecipes()
            _savedRecipes.value = recipes
            _savedIds.value = recipes.mapNotNull { it.id }.toSet()
        }
    }

    fun toggleBookmark(recipe: RecipeFire) {
        val id = recipe.id ?: return

        // 1. Optimistic UI update (instant change)
        val current = _savedIds.value.toMutableSet()
        val isSaved = current.contains(id)

        if (isSaved) {
            current.remove(id)
        } else {
            current.add(id)
        }
        _savedIds.value = current

        // Optional: keep full list in sync too
        if (isSaved) {
            _savedRecipes.value = _savedRecipes.value.filterNot { it.id == id }
        } else {
            _savedRecipes.value = _savedRecipes.value + recipe
        }

        // 2. Do Firebase update in background
        viewModelScope.launch {
            try {
                if (isSaved) {
                    RecipeRepository.unsaveRecipe(id)
                } else {
                    RecipeRepository.saveRecipe(id)
                }
            } catch (e: Exception) {
                // 3. Rollback if failed
                val rollback = _savedIds.value.toMutableSet()
                if (isSaved) rollback.add(id) else rollback.remove(id)
                _savedIds.value = rollback
            }
        }
    }


    fun isSaved(recipe: RecipeFire): Boolean {
        val id = recipe.id ?: return false
        return _savedIds.value.contains(id)
    }
}