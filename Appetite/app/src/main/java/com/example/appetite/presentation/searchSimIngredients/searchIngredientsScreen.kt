package com.example.appetite.presentation.searchSimIngredients

import androidx.compose.foundation.lazy.grid.items

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import coil.compose.AsyncImage
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
fun IngredientsRecipeScreen(
    navController: NavController,
    viewModel: SearchIngredientsViewModel,
    recipeViewModel: RecipeViewModel = hiltViewModel()
) {
    val results by viewModel.results.collectAsState()
    val ingredients by viewModel.ingredients.collectAsState()
    val loading by viewModel.loading.collectAsState()

    Log.d(
        "SimilarRecipeScreen",
        "Rendering UI. Loading=$loading, Results=${results.size}, Ingredients=${ingredients.size}"
    )

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

            if (ingredients.isNotEmpty()) {
                Text(
                    text = "Detected Ingredients:",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    textAlign = TextAlign.Start
                )

                Spacer(modifier = Modifier.height(10.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    // Keep track of single selected chip
                    var selectedChip by remember { mutableStateOf<String?>(null) }

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        ingredients.forEach { ingredient ->
                            val isSelected = selectedChip == ingredient

                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = if (isSelected) Color(0xFF4CAF50) else Color(0xFFE9ECEF), // green when selected
                                tonalElevation = 2.dp,
                                modifier = Modifier
                                    .clickable {
                                        selectedChip = if (isSelected) null else ingredient
                                    }
                            ) {
                                Text(
                                    text = ingredient,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = if (isSelected) Color.White else Color.Black
                                    ),
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }




            if (loading) {
                Text(
                    text = "Searching Recipes",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )

                )
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {

                    CircularProgressIndicator()
                }
            } else {
                Text(
                    text = "Suggested Recipes",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    textAlign = TextAlign.Start
                )
                Spacer(modifier = Modifier.height(10.dp))
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
                        items(results) { recipe ->
                            RecipeCard(recipe) { selectedRecipe ->
                                recipeViewModel.selectRecipeFire(selectedRecipe)
                                navController.navigate(
                                    NavigationRoutes.recipeDetailWithId(selectedRecipe.id ?: "")
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
