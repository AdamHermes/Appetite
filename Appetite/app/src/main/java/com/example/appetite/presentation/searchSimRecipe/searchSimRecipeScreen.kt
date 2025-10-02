package com.example.appetite.presentation.searchSimRecipe

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items // Import the correct items function
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import coil.compose.AsyncImage
import androidx.compose.runtime.getValue
// Removed setValue as it's not used
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.appetite.models.RecipeFire
import com.example.appetite.presentation.NavigationRoutes
import com.example.appetite.presentation.recipeDetail.RecipeViewModel
import com.example.appetite.presentation.sharedComponents.BottomNavigationBar

// Removed unused import for RecipeCard from userProfile as it's defined locally

@Composable
fun SimilarRecipeScreen(navController: NavController, viewModel: SearchSimRecipeViewModel, recipeViewModel: RecipeViewModel = hiltViewModel()) {
    val results by viewModel.results.collectAsState()
    val loading by viewModel.loading.collectAsState()

    Log.d("SimilarRecipeScreen", "Rendering UI. Loading=$loading, Results=${results.size}")
    Scaffold(
        bottomBar = {
            BottomNavigationBar(navController)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8F9FA))
                .padding(paddingValues)
                .padding(top = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Text(
                text = "Similar Recipes",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                ),
                modifier = Modifier.padding(bottom = 6.dp)
            )
            if (loading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                if (results.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No results found")
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .padding(top = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Use the items extension that takes a list
                        items(results) { recipe -> // Pass the results list directly
                            RecipeCard(recipe) { selectedRecipe ->
                                recipeViewModel.selectRecipeFire(selectedRecipe)
                                println("SimilarRecipeScreen: Selected recipe - ${selectedRecipe.name} with ID: ${selectedRecipe.id}") // Corrected Log Tag
                                navController.navigate(
                                    NavigationRoutes.recipeDetailWithId(
                                        selectedRecipe.id ?: ""
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RecipeCard(recipe: RecipeFire, onRecipeClick: (RecipeFire) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(3f / 4f)// square card
            .clickable {
                onRecipeClick(recipe)
            },
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween // This will push the Text to the bottom
        ) {
            AsyncImage(
                model = recipe.thumbnail,
                contentDescription = recipe.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f), // Image takes up available space
                contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier.padding(8.dp)) {
                Text(recipe.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("By ${recipe.contributorName ?: recipe.contributorId ?: "Unknown"}", fontSize = 12.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("${recipe.minutes} min", fontSize = 12.sp, color = Color.Gray)
                    Text("⭐ ${recipe.ratingAvg}", fontSize = 12.sp, color = Color.Gray)
                }
            }
        }
    }
}
