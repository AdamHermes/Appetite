package com.example.appetite.presentation.searchSimRecipe

import android.content.ContentResolver
import android.net.Uri
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
class SearchSimRecipeViewModel @Inject constructor() : ViewModel() {

    private val _results = MutableStateFlow<List<RecipeFire>>   (emptyList())
    val results: StateFlow<List<RecipeFire>> = _results

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    fun searchFromUri(resolver: ContentResolver, uri: Uri, topK: Int = 8) {
        if (_results.value.isNotEmpty()) return
        viewModelScope.launch {
            _loading.value = true
            Log.d("SearchSimVM", "Starting search for $uri")
            val recipes = RecipeRepository.searchSimilarFromUri(resolver, uri, topK)
            _results.value = recipes
            Log.d("SearchSimVM", "Found ${recipes.size} recipes")
            _loading.value = false
        }
    }
}


