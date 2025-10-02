package com.example.appetite.presentation.savedRecipe

// Required imports
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.appetite.R
import com.example.appetite.models.Recipe
import com.example.appetite.presentation.NavigationRoutes
import com.example.appetite.models.RecipeFire
import com.example.appetite.presentation.recipeDetail.RecipeViewModel
import com.example.appetite.presentation.sharedComponents.BottomNavigationBar

@Composable
fun SavedRecipeScreen(navController: NavController, viewModel: SavedRecipeViewModel = hiltViewModel(), recipeViewModel: RecipeViewModel = hiltViewModel()) {
    val savedRecipes = viewModel.savedRecipes.collectAsState()  // <-- collect here
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
                .padding(horizontal = 15.dp)
                .padding(top = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Text(
                text = "Saved recipes",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                ),
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Recipe Cards
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(savedRecipes.value) { recipe ->
                    SavedRecipeCard(recipe = recipe, viewModel = viewModel) { selectedRecipe ->
                        recipeViewModel.selectRecipeFire(selectedRecipe)
                        println("UserProfileScreen: Selected recipe - ${selectedRecipe.name} with ID: ${selectedRecipe.id}")
                        navController.navigate(NavigationRoutes.recipeDetailWithId(selectedRecipe.id ?: ""))
                    }
                }
            }
        }
    }
}

@Composable
fun SavedRecipeCard(recipe: RecipeFire, viewModel: SavedRecipeViewModel, onRecipeClick: (RecipeFire) -> Unit) {
    val savedIds = viewModel.savedIds.collectAsState()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clickable { onRecipeClick(recipe) },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Background Image
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(recipe.thumbnail)
                    .crossfade(true)
                    .build(),
                contentDescription = recipe.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .height(160.dp)
                    .fillMaxWidth()
            )

            // Overlay gradient
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.7f)
                            ),
                            startY = 0f,
                            endY = Float.POSITIVE_INFINITY
                        )
                    )
            )

            // Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top section with rating
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                color = Color.White.copy(alpha = 0.9f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Rating",
                                tint = Color(0xFFFFA726),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = recipe.ratingAvg.toString(),
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Medium,
                                    color = Color.Black
                                )
                            )
                        }
                    }
                }

                // Bottom section with recipe info
                Column {
                    Text(
                        text = recipe.name,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        ),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = "By ${recipe.contributorName}",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color.White.copy(alpha = 0.8f)
                        ),
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    Row(
                        modifier = Modifier.padding(top = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.timer),
                            contentDescription = "Time",
                            tint = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "${recipe.minutes} min",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        )
                    }
                }
            }

            // Bookmark icon (top left corner)
            IconButton(
                onClick = { viewModel.toggleBookmark(recipe) },
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp)
            ) {
                Icon(
                    painter = painterResource(
                        id = if (savedIds.value.contains(recipe.id)) {
                            R.drawable.ic_saved     // filled bookmark
                        } else {
                            R.drawable.ic_unsaved   // outline bookmark
                        }
                    ),
                    contentDescription = "Bookmark",
                    tint = Color.Unspecified,  // Let the vector define its own color
                    modifier = Modifier.size(30.dp)
                )
            }

        }
    }
}

