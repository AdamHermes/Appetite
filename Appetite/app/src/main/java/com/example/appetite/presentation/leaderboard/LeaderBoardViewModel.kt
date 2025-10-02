package com.example.appetite.presentation.leaderboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appetite.models.Profile
import com.example.appetite.models.RecipeFire
import com.example.appetite.repository.RecipeRepository
import com.example.appetite.repository.UserInfoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LeaderboardViewModel @Inject constructor() : ViewModel() {

    private val _topChefs = MutableStateFlow<List<Profile>>(emptyList())
    val topChefs: StateFlow<List<Profile>> = _topChefs.asStateFlow()

    private val _topRecipes = MutableStateFlow<List<RecipeFire>>(emptyList())
    val topRecipes: StateFlow<List<RecipeFire>> = _topRecipes.asStateFlow()

    init {
        loadLeaderboard()
    }

    private fun loadLeaderboard() {
        viewModelScope.launch {
            // Get users sorted by number of recipes
            val users = UserInfoRepository.getAllUsers()
            _topChefs.value = users.sortedByDescending { it.recipesCount }

            // Get recipes sorted by rating
            val recipes = RecipeRepository.getAllRecipes()
            _topRecipes.value = recipes.sortedByDescending { it.ratingAvg }
        }
    }
}
