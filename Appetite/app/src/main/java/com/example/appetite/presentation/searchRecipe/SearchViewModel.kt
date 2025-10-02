package com.example.appetite.presentation.searchRecipe

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appetite.models.RecipeFire
import com.example.appetite.models.UserSearchResponse
import com.example.appetite.network.ApiClient
import com.example.appetite.network.ApiService
import com.example.appetite.repository.RecipeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

enum class SearchType {
    RECIPES, USERS
}

@HiltViewModel
class SearchViewModel @Inject constructor() : ViewModel() {
    val query = mutableStateOf("")

    val sort = mutableStateOf<String?>(null) // popularity | rating | null
    val selectedAreas = mutableStateListOf<String>()
    val selectedCategories = mutableStateListOf<String>()
    val minutesBucket = mutableStateOf<String?>(null) // quick|short|medium|long

    // Result state
    val recipeResults = mutableStateListOf<RecipeFire>()
    val userResults = mutableStateListOf<UserSearchResponse>()
    val isLoading = mutableStateOf(false)
    val errorMessage = mutableStateOf<String?>(null)

    val searchType = mutableStateOf(SearchType.RECIPES)
    private var nextPageToken: String? = null

    fun onQueryChanged(newQuery: String) {
        query.value = newQuery
        // Removed automatic search - now only updates the query value
    }

    fun setSearchType(type: SearchType) {
        searchType.value = type
        recipeResults.clear()
        userResults.clear()
        nextPageToken = null
        if (type == SearchType.USERS) {
            clearFilters()
        }
    }

    fun clearFilters() {
        sort.value = null
        selectedAreas.clear()
        selectedCategories.clear()
        minutesBucket.value = null
    }

    fun performManualSearch() {
        performSearch(reset = true)
    }

    fun applyFilters(
        sort: String?,
        areas: List<String>,
        categories: List<String>,
        minutesBucket: String?
    ) {
        this.sort.value = sort
        selectedAreas.clear(); selectedAreas.addAll(areas)
        selectedCategories.clear(); selectedCategories.addAll(categories)
        this.minutesBucket.value = minutesBucket
        performSearch(reset = true)
    }

    fun performSearch(reset: Boolean = true) {
        if (reset) {
            nextPageToken = null
            recipeResults.clear()
            userResults.clear()
        }
        isLoading.value = true
        errorMessage.value = null
        viewModelScope.launch {
            try {
                when (searchType.value) {
                    SearchType.USERS -> {
                        val users = ApiClient.api.searchUsers(
                            name = query.value.takeIf { it.isNotBlank() },
                            limit = 20
                        )
                        userResults.addAll(users)
                    }
                    SearchType.RECIPES -> {
                        val (items, next) = RecipeRepository.searchRecipes(
                            name = query.value.takeIf { it.isNotBlank() },
                            sort = sort.value,
                            categories = if (selectedCategories.isEmpty()) null else selectedCategories.toList(),
                            areas = if (selectedAreas.isEmpty()) null else selectedAreas.toList(),
                            minutesBucket = minutesBucket.value,
                            limit = 20,
                            pageToken = nextPageToken,
                            enableFuzzy = (sort.value == null),
                        )
                        recipeResults.addAll(items)
                        nextPageToken = next
                    }
                }

            } catch (e: Exception) {
                println("SearchViewModel: Search error: ${e.message}")
                e.printStackTrace()
                errorMessage.value = e.message
            } finally {
                isLoading.value = false
            }
        }
    }

    fun loadMore() {
        if (isLoading.value || nextPageToken == null || searchType.value == SearchType.USERS) return
        performSearch(reset = false)
    }
    
    fun selectRecipe(recipe: RecipeFire) {
        // Store the selected recipe for navigation
        // This will be used by the RecipeViewModel when navigating to detail screen
    }
} 