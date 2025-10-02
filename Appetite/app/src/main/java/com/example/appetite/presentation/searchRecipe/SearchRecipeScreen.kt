package com.example.appetite.presentation.searchRecipe

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.appetite.R
import com.example.appetite.models.RecipeFire
import com.example.appetite.presentation.NavigationRoutes
import com.example.appetite.presentation.sharedComponents.BottomNavigationBar
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.colorResource
import com.example.appetite.models.UserSearchResponse
import com.example.appetite.presentation.sharedComponents.MultiSelectChips
import com.example.appetite.presentation.sharedComponents.FlowRowChips
import com.example.appetite.presentation.userProfile.UserProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchRecipeScreen(
    navController: NavController, 
    viewModel: SearchViewModel = hiltViewModel(),
    userProfileViewModel: UserProfileViewModel = hiltViewModel(),
    recipeViewModel: com.example.appetite.presentation.recipeDetail.RecipeViewModel = hiltViewModel()
) {
    val query by viewModel.query
    val recipeResults = viewModel.recipeResults
    val userResults = viewModel.userResults
    val searchType by viewModel.searchType
    val isLoading by viewModel.isLoading

    var isFilterOpen by remember { mutableStateOf(false) }
    var isVisible by remember { mutableStateOf(false) }
    
    // Animation for smooth entrance
    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(500),
        label = "fadeIn"
    )
    
    val slideOffset by animateDpAsState(
        targetValue = if (isVisible) 0.dp else 300.dp,
        animationSpec = tween(500),
        label = "slideIn"
    )

    LaunchedEffect(Unit) {
        isVisible = true
    }

    Scaffold(
        bottomBar = { BottomNavigationBar(navController) }
    ) { paddingValues ->
        AnimatedVisibility(
            visible = isVisible,
            enter = slideInHorizontally(
                initialOffsetX = { fullWidth -> fullWidth },
                animationSpec = tween(500)
            ),
            exit = slideOutHorizontally(
                targetOffsetX = { fullWidth -> -fullWidth },
                animationSpec = tween(300)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF8F9FA))
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
                    .alpha(alpha)
            ) {
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.Black
                        )
                    }
                    Text(
                        text = "Search",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.Black,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
                Spacer(Modifier.height(12.dp))

                FlowRowChips(
                    options = listOf(SearchType.RECIPES, SearchType.USERS),
                    labels = listOf("Recipes", "Users"),
                    selected = searchType,
                    onSelect = { viewModel.setSearchType(it) }
                )

                Spacer(Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = query,
                        onValueChange = viewModel::onQueryChanged,
                        modifier = Modifier.weight(1f),
                        leadingIcon = { 
                            var isSearchPressed by remember { mutableStateOf(false) }
                            val searchScale by animateFloatAsState(
                                targetValue = if (isSearchPressed) 0.9f else 1f,
                                animationSpec = tween(150),
                                label = "searchScale"
                            )
                            
                            Icon(
                                Icons.Default.Search, 
                                contentDescription = "Search",
                                modifier = Modifier
                                    .scale(searchScale)
                                    .clickable { 
                                        isSearchPressed = true
                                        viewModel.performManualSearch()
                                    }
                            ) 
                        },
                        placeholder = { Text("Search", color = Color.Gray) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFF5F5F5),
                            unfocusedContainerColor = Color(0xFFF5F5F5),
                            disabledContainerColor = Color(0xFFF5F5F5)
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { viewModel.performManualSearch() })
                    )
                    Spacer(Modifier.size(12.dp))


                    // only show filter button for recipe search
                    if (searchType == SearchType.RECIPES) {
                        var isFilterPressed by remember { mutableStateOf(false) }
                        val filterScale by animateFloatAsState(
                            targetValue = if (isFilterPressed) 0.9f else 1f,
                            animationSpec = tween(150),
                            label = "filterScale"
                        )
                        FilledTonalButton(
                            onClick = {
                                isFilterPressed = true
                                isFilterOpen = true
                            },
                            modifier = Modifier
                                .size(50.dp)
                                .scale(filterScale),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = Color(0xFF00A86B)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(painterResource(id = R.drawable.filter),
                                contentDescription = "Filters",
                                tint = Color.White)
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
//                Text(
//                    text = if (query.isBlank() || results.isEmpty()) "Enter a search term and click the search icon" else "Search Results",
//                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
//                )

                Spacer(Modifier.height(8.dp))

                if (isLoading && recipeResults.isEmpty() && userResults.isEmpty()) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
                when (searchType) {
                    SearchType.RECIPES -> {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            items(recipeResults) { recipe ->
                                RecipeCard(recipe) { selectedRecipe ->
                                    // Store the selected recipe in the shared ViewModel
                                    recipeViewModel.selectRecipeFire(selectedRecipe)
                                    println("SearchRecipeScreen: Selected recipe - ${selectedRecipe.name} with ID: ${selectedRecipe.id}")
                                    navController.navigate(NavigationRoutes.recipeDetailWithId(selectedRecipe.id ?: ""))
                                }
                            }
                            // TODO: add load-more trigger when reaching end; simplified for now
                        }
                    }
                    SearchType.USERS -> {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            items(userResults) { user ->
                                UserCard(user) { selectedUser ->
                                    userProfileViewModel.loadUserProfile(selectedUser.id)
                                    navController.navigate(NavigationRoutes.profileDetailWithId(selectedUser.id ?: ""))
                                    userProfileViewModel.loadUserProfile(null)

                                }
                            }
                        }
                    }
                }

            }
        }

        if (isFilterOpen) {
            FilterSheet(
                initialSort = viewModel.sort.value,
                initialAreas = viewModel.selectedAreas.toList(),
                initialCategories = viewModel.selectedCategories.toList(),
                initialMinutes = viewModel.minutesBucket.value,
                onDismiss = { isFilterOpen = false },
            ) { sort, areas, categories, minutes ->
                isFilterOpen = false
                viewModel.applyFilters(sort, areas, categories, minutes)
            }
        }
    }
}

@Composable
private fun RecipeCard(recipe: RecipeFire, onRecipeClick: (RecipeFire) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .clickable { onRecipeClick(recipe) },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Box(Modifier.fillMaxSize()) {
            AsyncImage(
                model = recipe.thumbnail,
                placeholder = painterResource(R.drawable.login_image),
                contentDescription = recipe.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))
                        )
                    )
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    RatingChip(recipe.ratingAvg ?: 0.0)
                }
                Column {
                    Text(
                        text = recipe.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        ),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (!recipe.contributorName.isNullOrBlank()) {
                        Text(
                            text = "By ${recipe.contributorName}",
                            style = MaterialTheme.typography.labelMedium.copy(color = Color.White.copy(alpha = 0.9f))
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun UserCard(user: UserSearchResponse, onUserClick: (UserSearchResponse) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onUserClick(user) },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = user.profileUrl,
                placeholder = painterResource(R.drawable.login_image),
                contentDescription = user.name,
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = user.name,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun RatingChip(rating: Double) {
    Surface(
        color = Color(0xFFFFEFC7),
        shape = RoundedCornerShape(50)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.star),
                contentDescription = null,
                tint = Color(0xFFFFA726),
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = String.format("%.1f", rating), 
                color = Color.Black,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterSheet(
    initialSort: String?,
    initialAreas: List<String>,
    initialCategories: List<String>,
    initialMinutes: String?,
    onDismiss: () -> Unit,
    onApply: (String?, List<String>, List<String>, String?) -> Unit,
) {
    var sort by remember { mutableStateOf(initialSort) }
    val areas = remember { mutableStateListOf<String>().apply { addAll(initialAreas) } }
    val categories = remember { mutableStateListOf<String>().apply { addAll(initialCategories) } }
    var minutes by remember { mutableStateOf(initialMinutes) }

    val allAreas = listOf("Italian", "French", "Spanish", "Chinese", "Indian", "American")
    val allCategories = listOf("Breakfast", "Lunch", "Dinner", "Vegetarian", "Vegan")

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState()) {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            Text("Filter Search", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))

            Text("Sort by")
            FlowRowChips(
                options = listOf(null, "rating"),
                labels = listOf("Default", "Rating"),
                selected = sort,
                onSelect = { sort = it }
            )

            Spacer(Modifier.height(12.dp))
            Text("Area")
            MultiSelectChips(allAreas, areas)

            Spacer(Modifier.height(12.dp))
            Text("Time to cook")
            FlowRowChips(
                options = listOf(null, "quick", "short", "medium", "long"),
                labels = listOf("All", "Quick (<=15)", "Short (<=30)", "Medium (<=45)", "Long (>60)"),
                selected = minutes,
                onSelect = { minutes = it }
            )

            Spacer(Modifier.height(12.dp))
            Text("Other categories")
            MultiSelectChips(allCategories, categories)

            Spacer(Modifier.height(16.dp))
            Button(
                onClick = { onApply(sort, areas.toList(), categories.toList(), minutes) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colorResource(R.color.green_but))
            ) { Text("Filter", color = Color.White) }
            Spacer(Modifier.height(24.dp))
        }
    }
}
