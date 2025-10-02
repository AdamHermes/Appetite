package com.example.appetite.presentation.searchSimIngredients

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
class SearchIngredientsViewModel @Inject constructor() : ViewModel() {

    private val _results = MutableStateFlow<List<RecipeFire>>   (emptyList())
    val results: StateFlow<List<RecipeFire>> = _results

    private val _ingredients = MutableStateFlow<List<String>>   (emptyList())
    val ingredients: StateFlow<List<String>> = _ingredients

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    fun searchFromUri(resolver: ContentResolver, uri: Uri, topK: Int = 8) {
        if (_results.value.isNotEmpty()) return
        viewModelScope.launch {
            _loading.value = true
            Log.d("SearchSimVM", "Starting search for $uri")
            val suggested = RecipeRepository.searchIngredientsFromUri(resolver, uri, topK)
            _results.value = suggested?.results ?: emptyList()
            _ingredients.value = suggested?.detected_ingredients ?: emptyList()
            _loading.value = false
        }
    }

}


