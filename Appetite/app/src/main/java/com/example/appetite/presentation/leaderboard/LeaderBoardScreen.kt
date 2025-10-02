package com.example.appetite.presentation.leaderboard

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.appetite.models.Profile
import com.example.appetite.models.RecipeFire
import com.example.appetite.presentation.NavigationRoutes
import com.example.appetite.presentation.recipeDetail.RecipeViewModel
import com.example.appetite.presentation.sharedComponents.BottomNavigationBar
import com.example.appetite.presentation.userProfile.UserProfileViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun LeaderboardScreen(
    navController: NavController,
    viewModel: LeaderboardViewModel = hiltViewModel(),
    recipeViewModel: RecipeViewModel = hiltViewModel(),
    userProfileViewModel: UserProfileViewModel = hiltViewModel()
) {
    val topChefs by viewModel.topChefs.collectAsState()
    val topRecipes by viewModel.topRecipes.collectAsState()

    val pagerState = rememberPagerState(pageCount = { 2 })
    val scope = rememberCoroutineScope()

    Scaffold(
        bottomBar = {
            BottomNavigationBar(navController)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFF8F9FA),
                            Color(0xFFE8F5FF)
                        )
                    )
                )
                .padding(paddingValues)
        ) {
            // Header with gradient
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF00C896),
                                Color(0xFF00B4A6)
                            )
                        )
                    )
                    .padding(vertical = 24.dp, horizontal = 30.dp)
            ) {
                Text(
                    text = "Leaderboard",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            // Enhanced Tabs with better styling
            TabRow(
                selectedTabIndex = pagerState.currentPage,
                containerColor = Color.White,
                contentColor = Color(0xFF00C896),
                indicator = { tabPositions ->
                    Box(
                        modifier = Modifier
                            .tabIndicatorOffset(tabPositions[pagerState.currentPage])
                            .height(4.dp)
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFF00C896),
                                        Color(0xFF00B4A6)
                                    )
                                ),
                                shape = RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp)
                            )
                    )
                },
                modifier = Modifier.shadow(8.dp)
            ) {
                listOf("Top Chefs", "Top Recipes").forEachIndexed { index, title ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        },
                        modifier = Modifier.padding(vertical = 16.dp)
                    ) {
                        Text(
                            text = title,
                            fontWeight = if (pagerState.currentPage == index) FontWeight.Bold else FontWeight.Medium,
                            color = if (pagerState.currentPage == index) Color(0xFF00C896) else Color.Gray
                        )
                    }
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                when (page) {
                    0 -> TopChefsList(
                        profiles = topChefs,
                        onClick = {
                            selectedChef ->
                            userProfileViewModel.loadUserProfile(selectedChef.id)
                            navController.navigate(NavigationRoutes.profileDetailWithId(selectedChef.id ?: ""))
                            userProfileViewModel.loadUserProfile(null)
                        }
                    )

                    1 -> TopRecipesList(
                        recipes = topRecipes,
                        onClick = {
                            selectedRecipe ->
                            recipeViewModel.selectRecipeFire(selectedRecipe)
                            println("UserProfileScreen: Selected recipe - ${selectedRecipe.name} with ID: ${selectedRecipe.id}")
                            navController.navigate(NavigationRoutes.recipeDetailWithId(selectedRecipe.id ?: ""))

                        }
                    )
                }
            }
        }
    }
}

@Composable
fun TopChefsList(
    profiles: List<Profile>,
    onClick: (Profile) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 30.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        itemsIndexed(profiles) { index, profile ->


            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onClick(profile) }
                    .animateContentSize(),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(0.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (index < 3) {
                        when (index) {
                            0 -> Color(0xFFFFD700).copy(alpha = 0.1f) // Gold
                            1 -> Color(0xFFC0C0C0).copy(alpha = 0.1f) // Silver
                            2 -> Color(0xFFCD7F32).copy(alpha = 0.1f) // Bronze
                            else -> Color.White
                        }
                    } else Color.White
                )
            ) {
                Row(
                    modifier = Modifier
                        .padding(20.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Ranking badge
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                brush = when (index) {
                                    0 -> Brush.radialGradient(
                                        colors = listOf(Color(0xFFFFD700), Color(0xFFB8860B))
                                    )
                                    1 -> Brush.radialGradient(
                                        colors = listOf(Color(0xFFC0C0C0), Color(0xFF708090))
                                    )
                                    2 -> Brush.radialGradient(
                                        colors = listOf(Color(0xFFCD7F32), Color(0xFF8B4513))
                                    )
                                    else -> Brush.radialGradient(
                                        colors = listOf(Color(0xFF00C896), Color(0xFF00B4A6))
                                    )
                                },
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = when (index) {
                                0 -> "🥇"
                                1 -> "🥈"
                                2 -> "🥉"
                                else -> "#${index + 1}"
                            },
                            fontWeight = FontWeight.Bold,
                            color = if (index >= 3) Color.White else Color.Black,
                            fontSize = if (index < 3) 16.sp else 12.sp
                        )
                    }

                    Spacer(Modifier.width(16.dp))

                    // Profile picture with enhanced styling
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .shadow(4.dp, CircleShape)
                    ) {
                        AsyncImage(
                            model = profile.profileUrl,
                            contentDescription = "Profile picture",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )

                        // Crown for top 3
                        if (index < 3) {
                            Text(
                                text = "👑",
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .offset(x = 4.dp, y = (-4).dp),
                                fontSize = 16.sp
                            )
                        }
                    }

                    Spacer(Modifier.width(16.dp))

                    // Chef info with better typography
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = profile.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "📚 ${profile.recipesCount}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF00C896),
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "recipes",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray
                            )
                        }
                    }

                    // Arrow indicator
                    Text(
                        text = "→",
                        color = Color(0xFF00C896),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun TopRecipesList(
    recipes: List<RecipeFire>,
    onClick: (RecipeFire) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 30.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        itemsIndexed(recipes) { index, recipe ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onClick(recipe) }
                    .animateContentSize(),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(0.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (index < 3) {
                        when (index) {
                            0 -> Color(0xFFFFD700).copy(alpha = 0.1f)
                            1 -> Color(0xFFC0C0C0).copy(alpha = 0.1f)
                            2 -> Color(0xFFCD7F32).copy(alpha = 0.1f)
                            else -> Color.White
                        }
                    } else Color.White
                )
            ) {
                Column {
                    // Image with ranking overlay
                    Box {
                        AsyncImage(
                            model = recipe.thumbnail,
                            contentDescription = recipe.name,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)),
                            contentScale = ContentScale.Crop
                        )

                        // Gradient overlay for better text readability
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            Color.Black.copy(alpha = 0.3f)
                                        )
                                    )
                                )
                        )

                        // Ranking badge
                        Box(
                            modifier = Modifier
                                .padding(16.dp)
                                .size(48.dp)
                                .background(
                                    brush = when (index) {
                                        0 -> Brush.radialGradient(
                                            colors = listOf(Color(0xFFFFD700), Color(0xFFB8860B))
                                        )
                                        1 -> Brush.radialGradient(
                                            colors = listOf(Color(0xFFC0C0C0), Color(0xFF708090))
                                        )
                                        2 -> Brush.radialGradient(
                                            colors = listOf(Color(0xFFCD7F32), Color(0xFF8B4513))
                                        )
                                        else -> Brush.radialGradient(
                                            colors = listOf(Color(0xFF00C896), Color(0xFF00B4A6))
                                        )
                                    },
                                    shape = CircleShape
                                )
                                .shadow(4.dp, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = when (index) {
                                    0 -> "🥇"
                                    1 -> "🥈"
                                    2 -> "🥉"
                                    else -> "#${index + 1}"
                                },
                                fontWeight = FontWeight.Bold,
                                color = if (index >= 3) Color.White else Color.Black,
                                fontSize = if (index < 3) 18.sp else 14.sp
                            )
                        }
                    }

                    // Recipe details with enhanced layout
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Text(
                            text = recipe.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            color = Color(0xFF2C3E50)
                        )

                        Spacer(Modifier.height(8.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Rating with stars
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                repeat(5) { starIndex ->
                                    val starColor = if (starIndex < recipe.ratingAvg?.toInt() ?:0) {
                                        Color(0xFFFFD700)
                                    } else {
                                        Color.Gray.copy(alpha = 0.3f)
                                    }
                                    Text(
                                        text = "⭐",
                                        color = starColor,
                                        fontSize = 16.sp
                                    )
                                }
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = String.format("%.1f", recipe.ratingAvg),
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF00C896)
                                )
                            }

                        }
                    }
                }
            }
        }
    }
}

