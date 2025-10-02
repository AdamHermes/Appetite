package com.example.appetite.presentation.addRecipe

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.ViewModel
import com.example.appetite.models.RecipeFire
import com.example.appetite.repository.RecipeRepository
import javax.inject.Inject

class AddRecipeViewModel @Inject constructor() : ViewModel() {
    suspend fun createRecipe(resolver: ContentResolver, imageUri: Uri, recipeJson: String): RecipeFire? {
        return RecipeRepository.createRecipe(resolver, imageUri, recipeJson)
    }
}