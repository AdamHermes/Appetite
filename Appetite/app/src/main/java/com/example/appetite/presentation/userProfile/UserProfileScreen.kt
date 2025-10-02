package com.example.appetite.presentation.userProfile

import android.util.Log
import androidx.compose.material3.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.appetite.presentation.sharedComponents.BottomNavigationBar
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import com.example.appetite.models.Recipe
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.appetite.models.RecipeFire
import com.example.appetite.presentation.NavigationRoutes
import com.example.appetite.presentation.home.HomeViewModel
import com.example.appetite.presentation.recipeDetail.RecipeViewModel
import com.example.appetite.presentation.sharedComponents.CustomTopBar
import com.google.firebase.auth.FirebaseAuth


@Composable
fun UserProfileScreen(
    navController: NavController, 
    profileViewModel: UserProfileViewModel = hiltViewModel(),
    homeViewModel: HomeViewModel = hiltViewModel(),
    recipeViewModel: RecipeViewModel = hiltViewModel(),
    viewedUserId: String? = null,
) {
    val userProfile by profileViewModel.profile.collectAsState()
    Scaffold(
        topBar = {
            CustomTopBar(
                onSignOutClick = {
                    FirebaseAuth.getInstance().signOut()
                    homeViewModel.clearState()
                    navController.navigate(NavigationRoutes.Login) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onEditProfileClick = {
                    navController.navigate(NavigationRoutes.EditProfile)
                }
            )
        },
        bottomBar = {
            BottomNavigationBar(navController)
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(paddingValues)
                .padding(horizontal = 15.dp)
            ) {
            item{
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(userProfile.profileUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = userProfile.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                    )
                    Spacer(modifier = Modifier.width(20.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        // Recipes
                        StatItem(
                            label = "Recipes",
                            value = userProfile.recipesCount.toString()
                        )

                        // Followers
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable {
                                val currentViewedId = viewedUserId
                                    ?: profileViewModel.profile.value.id?.takeIf { it.isNotBlank() }
                                    ?: FirebaseAuth.getInstance().currentUser?.uid
                                    ?: ""

                                if (currentViewedId.isNotBlank()) {
                                    navController.navigate(
                                        NavigationRoutes.followListRoute(
                                            currentViewedId,
                                            "followers"
                                        )
                                    )
                                }
                            }
                        ) {
                            Text(
                                "${userProfile.followersCount}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                            Text("Followers", fontSize = 16.sp, color = Color.Gray)
                        }

                        // Following
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable {
                                val currentViewedId = viewedUserId
                                    ?: profileViewModel.profile.value.id?.takeIf { it.isNotBlank() }
                                    ?: FirebaseAuth.getInstance().currentUser?.uid
                                    ?: ""

                                if (currentViewedId.isNotBlank()) {
                                    navController.navigate(
                                        NavigationRoutes.followListRoute(
                                            currentViewedId,
                                            "following"
                                        )
                                    )
                                }
                            }
                        ) {
                            Text(
                                userProfile.followingCount.toString(),
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                            Text("Following", fontSize = 16.sp, color = Color.Gray)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            item{
                Column {
                    Text(userProfile.name, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(userProfile.bio, fontSize = 16.sp, color = Color.Gray, maxLines = 2)
                }

                Spacer(modifier = Modifier.height(24.dp))
            }


            item{
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TabButton(text = "My Recipes", isSelected = true)
                }

                Spacer(modifier = Modifier.height(24.dp))

            }
            item{
                // Recipe Cards
                userProfile.recipes.forEach { recipe ->
                    RecipeCard(recipe) { selectedRecipe ->
                        recipeViewModel.selectRecipeFire(selectedRecipe)
                        println("UserProfileScreen: Selected recipe - ${selectedRecipe.name} with ID: ${selectedRecipe.id}")
                        navController.navigate(com.example.appetite.presentation.NavigationRoutes.recipeDetailWithId(selectedRecipe.id ?: ""))
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }
            }

        }
    }

}

@Composable
fun StatItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.widthIn(min = 64.dp), // keeps consistent width
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = value,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = label,
            fontSize = 14.sp, // slightly smaller for better balance
            color = Color.Gray,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun TabButton(
    text: String,
    isSelected: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp) // nice tall button
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) Color(0xFF00A86B) else Color.LightGray.copy(alpha = 0.3f)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (isSelected) Color.White else Color.Black
        )
    }
    Spacer(modifier = Modifier.width(4.dp))

}

@Composable
fun RecipeCard(recipe: RecipeFire, onRecipeClick: (RecipeFire) -> Unit) {
    Card(
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onRecipeClick(recipe) },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(recipe.thumbnail)
                    .crossfade(true)
                    .build(),
                contentDescription = recipe.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .height(200.dp)
                    .fillMaxWidth()
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
