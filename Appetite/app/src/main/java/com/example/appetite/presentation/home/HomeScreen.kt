package com.example.appetite.presentation.home
import android.content.Context
import android.content.Intent
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.compose.animation.core.animateFloatAsState
import com.example.appetite.R
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.appetite.models.RecipeFire
import com.example.appetite.presentation.recipeDetail.RecipeViewModel
import com.example.appetite.presentation.sharedComponents.BottomNavigationBar
import com.example.appetite.presentation.userProfile.UserProfileViewModel
import com.example.appetite.presentation.NavigationRoutes
import kotlinx.coroutines.flow.compose
import java.io.File
import kotlin.math.roundToInt

// Add this function to scan media files
private fun scanMediaFile(context: Context, file: File) {
    MediaScannerConnection.scanFile(
        context,
        arrayOf(file.absolutePath),
        null
    ) { path, uri ->
        Log.d("MediaScanner", "Scanned $path: $uri")
    }
}
// Or use this simpler approach
private fun refreshMediaStore(context: Context, file: File) {
    val intent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
    intent.data = Uri.fromFile(file)
    context.sendBroadcast(intent)
}
private fun scanAllImagesInPicturesFolder(context: Context) {
    val picturesDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    picturesDir?.listFiles { file ->
        file.isFile && (file.extension.lowercase() in listOf("jpg", "jpeg", "png"))
    }?.forEach { imageFile ->
        scanMediaFile(context, imageFile)
    }
}
@Composable
fun HomeScreen(
    navController: NavController,
    homeViewModel: HomeViewModel = hiltViewModel(),
    profileViewModel: UserProfileViewModel = hiltViewModel(),
    recipeViewModel : RecipeViewModel = hiltViewModel()) {

    val categories = homeViewModel.categories
    val recipes = homeViewModel.forYouRecipes.collectAsState().value
    val newRecipes = homeViewModel.newRecipes.collectAsState().value
    val mostSavedRecipes = homeViewModel.mostSavedRecipes.collectAsState().value
    val userProfile by profileViewModel.profile.collectAsState()
    val selectedCategory by homeViewModel.selectedCategory.collectAsState()
    val context = LocalContext.current

    // Run when the composable first loads
    LaunchedEffect(Unit) {
        scanAllImagesInPicturesFolder(context)
    }
    Scaffold(
        bottomBar = {
            BottomNavigationBar(navController)
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8F9FA))
                .padding(paddingValues)
                .padding(horizontal = 15.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            // Top section with greeting and profile
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Hello " + (userProfile?.name ?: "Guest"),
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "What are you cooking today?",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                    }

                    // Profile image placeholder
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(userProfile.profileUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription =  userProfile.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .clickable {navController.navigate(NavigationRoutes.Profile)}
                    )
                }
            }

            // Search bar with filter
            item {
                SearchBarWithFilter(onClick = { navController.navigate(NavigationRoutes.SearchRecipe) })
            }

            // Category chips
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(categories) { category ->
                        FilterChip(
                            category = category,
                            isSelected = category == selectedCategory,
                            onClick = { homeViewModel.updateCategory(category) }
                        )
                    }
                }
            }

            item {
                Text(
                    text = "Chef's Picks",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                )
            }

            // Featured recipes section
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(recipes) { recipe ->
                        RecipeCard(recipe = recipe) {
                            recipeViewModel.selectRecipeFire(recipe)
                            println("UserProfileScreen: Selected recipe - ${recipe.name} with ID: ${recipe.id}")
                            navController.navigate(NavigationRoutes.recipeDetailWithId(recipe.id ?: ""))
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(4.dp)) }

            // New Recipes section
            item {
                Text(
                    text = "New Recipes",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                )
            }

            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    newRecipes.forEach {
                        println("Recipe: ${it.name}, createdAt=${it.createdAt?.time}")
                    }

                    items(newRecipes) { recipe ->
                        NewRecipeCard(recipe = recipe) {
                            recipeViewModel.selectRecipeFire(recipe)
                            println("UserProfileScreen: Selected recipe - ${recipe.name} with ID: ${recipe.id}")
                            navController.navigate(NavigationRoutes.recipeDetailWithId(recipe.id ?: ""))
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(4.dp)) }
            item {
                Text(
                    text = "Most Saved Recipes",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                )
            }

            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(mostSavedRecipes) { recipe ->
                        NewRecipeCard(recipe = recipe) {
                            recipeViewModel.selectRecipeFire(recipe)
                            println("UserProfileScreen: Selected recipe - ${recipe.name} with ID: ${recipe.id}")
                            navController.navigate(NavigationRoutes.recipeDetailWithId(recipe.id ?: ""))
                        }
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(16.dp)) }

        }
    }
}

@Composable
fun FilterChip(
    category: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        color = if (isSelected) Color(0xFF00A86B) else Color.Transparent,
        shape = RoundedCornerShape(25.dp),
        modifier = Modifier
            .height(36.dp)
            .clickable { onClick() }
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Text(
                text = category,
                color = if (isSelected) Color.White else Color.Gray,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}



@Composable
fun RecipeCard(recipe: RecipeFire, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(180.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column {
            Box {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(recipe.thumbnail)
                        .crossfade(true)
                        .build(),
                    contentDescription = recipe.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .aspectRatio(1f)
                        .fillMaxWidth()
                )

                // Rating badge
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp),
                    color = Color(0xFFFFEFC7), // Light yellow background similar to your screenshot
                    shape = RoundedCornerShape(50) // Fully rounded pill shape
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.star),
                            contentDescription = "Star icon",
                            tint = Color(0xFFFFA000), // Orange star color
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${recipe.ratingAvg}",
                            color = Color.Black,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

            }
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    text = recipe.name,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    ),
                    maxLines = 2
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Time",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "${recipe.minutes} Mins",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                    }

                }
            }

        }
    }
}

@Composable
fun NewRecipeCard(recipe: RecipeFire, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .width(280.dp)
            .height(110.dp)
            .padding(end = 8.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxSize()
                .align(Alignment.BottomStart)
                .clickable { onClick() },
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = recipe.name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    ),
                    maxLines = 2, // allow wrapping
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth(0.75f)
                )

                Row {
                    repeat(recipe.ratingAvg?.roundToInt() ?:0) {
                        Icon(
                            painter = painterResource(id = R.drawable.star),
                            contentDescription = "Star",
                            tint = Color(0xFFFFC107),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "By ${recipe.contributorName}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Icon(
                        painter = painterResource(id = R.drawable.timer),
                        contentDescription = "Time",
                        tint = Color.Gray,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${recipe.minutes} mins",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
        }

        // ✅ Now the image is inside the main Box
        Box(
            modifier = Modifier
                .size(80.dp)
                .align(Alignment.TopEnd)
                .offset(y = (-20).dp) // Only move up
                .clip(CircleShape)
                .background(Color.White)
                .border(1.dp, Color.LightGray, CircleShape)
        ) {
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
        }
    }
}


@Composable
fun SearchBarWithFilter(onClick: () -> Unit) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = androidx.compose.animation.core.tween(150),
        label = "searchScale"
    )
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Search bar (clickable)
        Surface(
            modifier = Modifier
                .weight(1f)
                .height(50.dp)
                .scale(scale)
                .clickable {
                    isPressed = true
                    onClick()
                },
            color = Color(0xFFF5F5F5),
            shape = RoundedCornerShape(12.dp),
            shadowElevation = if (isPressed) 4.dp else 2.dp
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = "Search recipe",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }
            }
        }

        // Filter button
        Surface(
            modifier = Modifier.size(50.dp),
            color = Color(0xFF00A86B),
            shape = RoundedCornerShape(12.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.filter),
                    contentDescription = "Filter",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
