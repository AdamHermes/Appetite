package com.example.appetite.presentation.recipeDetail

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.appetite.R
import com.example.appetite.models.RecipeFire
import com.example.appetite.models.PopupMenuItem
import com.example.appetite.models.Profile
import com.example.appetite.presentation.NavigationRoutes
import com.example.appetite.presentation.recipeDetail.RecipeTab.Ingredient
import com.example.appetite.presentation.recipeDetail.RecipeTab.Procedure
import com.example.appetite.presentation.savedRecipe.SavedRecipeViewModel
import com.example.appetite.presentation.sharedComponents.PopupMenu
import com.example.appetite.presentation.userProfile.UserProfileViewModel
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import com.example.appetite.repository.AuthRepository
import com.example.appetite.repository.FollowRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeoutOrNull

@Composable
fun RecipeDetailScreen(
	navController: NavController,
    viewModel: RecipeViewModel = hiltViewModel(),
	savedRecipeViewModel: SavedRecipeViewModel = hiltViewModel(),
	userProfileViewModel: UserProfileViewModel = hiltViewModel(),
    recipeId: String = "",
    onBack: () -> Unit = {}
, seeReview: () -> Unit = {}) {
	var selectedTab by remember { mutableStateOf<RecipeTab>(Ingredient) }

	var isFollowing by remember(recipeId) { mutableStateOf<Boolean?>(null) }
	var isFollowLoading by remember { mutableStateOf(false) }
	val coroutineScope = rememberCoroutineScope()
//	var isInitialFollowLoading by remember(recipeId) { mutableStateOf(true) }

	val recipe = viewModel.selectedRecipe.value ?: viewModel.featuredRecipes.firstOrNull()
	val recipeFire = viewModel.selectedRecipeFire.value
	val isLoading = viewModel.isLoading.value

	var showRatePopup by remember { mutableStateOf(false ) }
	var myRating by remember { mutableStateOf<Int?>(null) }
	var showDeleteDialog by remember { mutableStateOf(false) }
	var showDeleteSuccess by remember { mutableStateOf(false) }
	val currentUserId = viewModel.user

	LaunchedEffect(showRatePopup) {
			myRating = viewModel.getMyRating(recipeId)
	}

	LaunchedEffect(recipeId) {
		if (recipeId.isNotEmpty()) {
			isFollowing = null
			try {
				// Trigger the load for the new recipe
				viewModel.loadRecipeById(recipeId)

				// Wait until the new recipe is loaded and use its contributorId
				val contributorId = withTimeoutOrNull(3000) {
					while (viewModel.selectedRecipeFire.value?.contributorId == null) {
						delay(50)
					}
					viewModel.selectedRecipeFire.value?.contributorId
				}

				if (contributorId != null) {
					val followStatus = FollowRepository.checkFollowStatus(contributorId)
					isFollowing = followStatus.isFollowing
				} else {
					isFollowing = false
				}
			} catch (e: Exception) {
				isFollowing = false
			}
		}
	}


//	LaunchedEffect(viewModel.contributor.value?.id) {
//		viewModel.contributor.value?.id?.let { contributorId ->
//			coroutineScope.launch {
//				try {
//					val response = FollowRepository.checkFollowStatus(contributorId)
//					isFollowing = response.isFollowing
//				} catch (e: Exception) {
//					isFollowing = false
//				}
//			}
//		}
//	}
	if (isLoading || isFollowing == null) {
		Box(
			modifier = Modifier.fillMaxSize(),
			contentAlignment = Alignment.Center
		) {
			CircularProgressIndicator()
		}
		return
	}
//	if (isInitialFollowLoading) {
//		Box(
//			modifier = Modifier.fillMaxSize(),
//			contentAlignment = Alignment.Center
//		) {
//			CircularProgressIndicator()
//		}
//		return
//	}
	
	if (recipeFire == null && recipe == null) {
		if (recipeId.isNotEmpty()) {
			Box(
				modifier = Modifier.fillMaxSize(),
				contentAlignment = Alignment.Center
			) {
				CircularProgressIndicator()
			}
			return
		} else {
			LaunchedEffect(Unit) {
				onBack()
			}
			return
		}
	}
	
	// Clear selected recipes when leaving the screen
	DisposableEffect(Unit) {
		onDispose {
			viewModel.clearSelectedRecipes()
		}
	}

	Surface(modifier = Modifier.fillMaxSize()) {
		Box(modifier = Modifier.fillMaxSize()) {
			LazyColumn(
				modifier = Modifier
					.fillMaxSize()
					.background(MaterialTheme.colorScheme.background),
				contentPadding = PaddingValues(bottom = 80.dp)
			) {
				item {
					HeaderImage(
						imageRes = recipe?.imageRes
							?: R.drawable.dish1, // Assuming 'recipe' is another potentially nullable source
						recipe = recipeFire, // Pass the non-null 'nonNullRecipe'
						onBack = onBack,
						seeReview = seeReview,
						showRate = { showRatePopup = true },
						onDelete = if (recipeFire?.contributorId == currentUserId.value) { { showDeleteDialog = true } } else null
					)

				}

				item { Spacer(modifier = Modifier.height(16.dp)) }

				item {
					RecipeTitleSection(
						savedRecipeViewModel,
						recipe = recipeFire
					)
				}

				item { Spacer(modifier = Modifier.height(12.dp)) }

			item { 
				AuthorRow(
					viewModel, recipeFire,
					isFollowing = isFollowing ?: false,
					isFollowLoading = isFollowLoading || isFollowing == null,
					onFollowClick = { contributorId ->
						if (isFollowing != null) {
							coroutineScope.launch {
								isFollowLoading = true
								try {
									if (isFollowing == true) {
										FollowRepository.unfollowUser(contributorId)
										isFollowing = false
										userProfileViewModel.loadUserProfile()
									} else {
										try {
											FollowRepository.followUser(contributorId)
											isFollowing = true
											userProfileViewModel.loadUserProfile()
										} catch (e: Exception) {

										}
									}
								} finally {
									isFollowLoading = false
								}
							}
						}
					},
					onClick = {
						selectedChef ->
						userProfileViewModel.loadUserProfile(selectedChef.id)
						navController.navigate(NavigationRoutes.profileDetailWithId(selectedChef.id ?: ""))
						userProfileViewModel.loadUserProfile(null)

					}
				)
			}

				item { Spacer(modifier = Modifier.height(16.dp)) }

				item { Tabs(selectedTab = selectedTab, onTabSelected = { selectedTab = it }) }

				item { Spacer(modifier = Modifier.height(12.dp)) }

				when (selectedTab) {
					Ingredient -> {
						val ingredients = recipeFire?.ingredients ?: emptyList()
						item { MetaRow(left = "1 serve", right = "${ingredients.size} Items") }
						if (ingredients.isNotEmpty()) {
							itemsIndexed(ingredients) { _, ingredient -> IngredientItem(ingredient) }
						} else {
							item {
								Text(
									text = "No ingredients available",
									style = MaterialTheme.typography.bodyMedium,
									color = Color.Gray,
									modifier = Modifier.padding(
										horizontal = 16.dp,
										vertical = 16.dp
									)
								)
							}
						}
					}

					Procedure -> {
						val steps = recipeFire?.steps ?: emptyList()
						val instructions = recipeFire?.instructions

						// If no steps but we have instructions, split instructions into steps
						val displaySteps = if (steps.isNotEmpty()) {
							steps
						} else if (!instructions.isNullOrBlank()) {
							splitInstructionsIntoSteps(instructions)
						} else {
							emptyList()
						}

						item { MetaRow(left = "1 serve", right = "${displaySteps.size} Steps") }
						if (displaySteps.isNotEmpty()) {
							itemsIndexed(displaySteps) { index, step ->
								StepItem(
									step = index + 1,
									description = step
								)
							}
						} else {
							item {
								Text(
									text = "No steps available",
									style = MaterialTheme.typography.bodyMedium,
									color = Color.Gray,
									modifier = Modifier.padding(
										horizontal = 16.dp,
										vertical = 16.dp
									)
								)
							}
						}
					}
				}
			}

			// --- Floating voice AI agent Button ---
			FloatingActionButton(
				onClick = {
					navController.navigate("voiceCookingAgent/${recipeId}")
				},
				modifier = Modifier
					.align(Alignment.BottomEnd)
					.padding(20.dp),
				containerColor = colorResource(id = R.color.green_but),
				shape = CircleShape
			) {
				Icon(
					painter = painterResource(id = R.drawable.ic_mic_ai),
					contentDescription = "Voice Assistant",
					tint = Color.White,
					modifier = Modifier.size(28.dp)
				)
			}
		}

	}



	if (showDeleteDialog) {
		AlertDialog(
			onDismissRequest = { showDeleteDialog = false },
			title = { Text("Delete Recipe") },
			text = { Text("Are you sure you want to delete this recipe?") },
			confirmButton = {
				Button(
					onClick = {
						viewModel.deleteRecipe(recipeId) { success ->
							showDeleteDialog = false
							if (success) {
								showDeleteSuccess = true
								// Reload user profile to update recipe list
								userProfileViewModel.loadUserProfile()
							}
						}
					},
					colors = ButtonDefaults.buttonColors(
						containerColor = Color(0xFFD32F2F), // Red
						contentColor = Color.White
					),
					shape = RoundedCornerShape(12.dp)
				) { Text("Delete") }
			},
			dismissButton = {
				Button(
					onClick = { showDeleteDialog = false },
					colors = ButtonDefaults.buttonColors(
						containerColor = Color(0xFFF3F5F7), // Light gray
						contentColor = Color.Black
					),
					shape = RoundedCornerShape(12.dp)
				) { Text("Cancel") }
			},
			containerColor = Color(0xFFE8F2EE) // Soft green background
		)
	}

	// Show success dialog before navigating back
	if (showDeleteSuccess) {
		AlertDialog(
			onDismissRequest = { showDeleteSuccess = false },
			title = { Text("Recipe Deleted") },
			text = { Text("The recipe was deleted successfully.") },
			confirmButton = {
				Button(
					onClick = {
						showDeleteSuccess = false
						onBack()
					},
					colors = ButtonDefaults.buttonColors(
						containerColor = colorResource(R.color.green_but),
						contentColor = Color.White
					),
					shape = RoundedCornerShape(12.dp)
				) { Text("OK") }
			}
		)
	}

	// Show the popup
	RateRecipePopup(
		show = showRatePopup,
		currentRating = myRating,
		onRate = { rating ->
			viewModel.rateRecipe(recipeId, rating) // Implement this in your ViewModel
		},
		onDismiss = { showRatePopup = false }
	)
}

private enum class RecipeTab { Ingredient, Procedure }

/**
 * Splits cooking instructions into logical steps
 * Handles various formats commonly found in recipe instructions
 */
private fun splitInstructionsIntoSteps(instructions: String): List<String> {
    val cleanedInstructions = instructions.trim()
    
    // If instructions are already well-formatted with numbers or clear separators
    if (cleanedInstructions.contains(Regex("^\\d+\\."))) {
        // Split by numbered steps (1., 2., etc.)
        return cleanedInstructions.split(Regex("\\n\\s*\\d+\\."))
            .filter { it.trim().isNotEmpty() }
            .map { it.trim() }
    }
    
    // Split by common sentence endings followed by newlines or periods
    val sentences = cleanedInstructions.split(Regex("(?<=[.!?])\\s+(?=[A-Z])"))
        .filter { it.trim().isNotEmpty() }
    
    // If we have too many short sentences, group them
    val steps = mutableListOf<String>()
    var currentStep = StringBuilder()
    
    for (sentence in sentences) {
        val trimmed = sentence.trim()
        if (trimmed.isEmpty()) continue
        
        // If current step is getting too long, start a new one
        if (currentStep.length > 200 && currentStep.isNotEmpty()) {
            steps.add(currentStep.toString().trim())
            currentStep.clear()
        }
        
        // Add sentence to current step
        if (currentStep.isNotEmpty()) {
            currentStep.append(" ")
        }
        currentStep.append(trimmed)
    }
    
    // Add the last step if it has content
    if (currentStep.isNotEmpty()) {
        steps.add(currentStep.toString().trim())
    }
    
    // If we still have too many steps, try splitting by paragraphs
    if (steps.size > 10) {
        return cleanedInstructions.split("\n\n")
            .filter { it.trim().isNotEmpty() }
            .map { it.trim() }
    }
    
    return steps
}

@Composable
private fun HeaderImage(imageRes: Int,recipe : RecipeFire?, onBack: () -> Unit, seeReview: () -> Unit = {}, showRate: () -> Unit = {}, onDelete: (() -> Unit)? = null) {
	val thumbnail = recipe?.thumbnail ?: ""   // fallback to empty string
	val minutes = recipe?.minutes ?: 0        // fallback to 0
	val rating = recipe?.ratingAvg ?: 0.0
	Column {
		// Back and More buttons positioned above the header image
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.windowInsetsPadding(WindowInsets.statusBars)
				.padding(horizontal = 12.dp, vertical = 8.dp),
			horizontalArrangement = Arrangement.SpaceBetween,
			verticalAlignment = Alignment.CenterVertically
		) {
			IconButton(onClick = onBack, modifier = Modifier.size(32.dp)) {
				Icon(
					imageVector = Icons.Default.ArrowBack,
					contentDescription = "Back",
					modifier = Modifier.size(24.dp),
					tint = Color.Black
				)
			}

			Row(
				verticalAlignment = Alignment.CenterVertically
			) {
				if (onDelete != null) {
					IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
						Icon(
							painter = painterResource(id = R.drawable.delete_ic), // Use your trash icon
							contentDescription = "Delete Recipe",
							modifier = Modifier.size(20.dp),
						)
					}
				}
				val menuItems = listOf(
					PopupMenuItem("Rate", painterResource(id = R.drawable.star_ic)) { showRate() },
					PopupMenuItem(
						"Reviews",
						painterResource(id = R.drawable.comment_ic)
					) { seeReview() },
				)

				PopupMenu(menuItems = menuItems)
			}

		}
		
		// Header image below the buttons
		Box(
			modifier = Modifier
				.fillMaxWidth()
				.height(280.dp)
				.padding(horizontal = 16.dp, vertical = 8.dp)
				.clip(RoundedCornerShape(16.dp))
		) {
			if (thumbnail != null) {
				AsyncImage(
					model = thumbnail,
					placeholder = painterResource(id = imageRes),
					contentDescription = null,
					modifier = Modifier.fillMaxSize(),
					contentScale = ContentScale.Crop
				)
			} else {
				Image(
					painter = painterResource(id = imageRes),
					contentDescription = null,
					modifier = Modifier.fillMaxSize(),
					contentScale = ContentScale.Crop
				)
			}

			Box(
				modifier = Modifier
					.fillMaxSize()
					.background(
						Brush.verticalGradient(
							colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f)),
							startY = 300f,
							endY = Float.POSITIVE_INFINITY
						)
					)
			)


			Row(
			modifier = Modifier
				.align(Alignment.BottomEnd)
				.padding(12.dp),
			verticalAlignment = Alignment.CenterVertically
			) {
				Row(
					verticalAlignment = Alignment.CenterVertically,
					modifier = Modifier
						.background(Color(0x66000000), RoundedCornerShape(50))
						.padding(horizontal = 8.dp, vertical = 4.dp)
				) {
					Icon(
						painter = painterResource(id = R.drawable.timer),
						contentDescription = null,
						tint = Color.White,
						modifier = Modifier.size(16.dp)
					)
					Spacer(modifier = Modifier.width(4.dp))
					Text(
						text = "${minutes} min",
						color = Color.White,
						fontSize = 16.sp,
						fontWeight = FontWeight.Medium
					)
				}
			}

			if (rating != null) {
				Surface(
					modifier = Modifier
						.align(Alignment.TopEnd)
						.padding(12.dp),
					color = Color(0xFFFFEFC7),
					shape = RoundedCornerShape(50)
				) {
					Row(
						verticalAlignment = Alignment.CenterVertically,
						modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
					) {
						Icon(
							painter = painterResource(id = R.drawable.star),
							contentDescription = "Star icon",
							tint = Color(0xFFFFA000),
							modifier = Modifier.size(16.dp)
						)
						Spacer(modifier = Modifier.width(4.dp))
						Text(
							text = String.format("%.1f", rating),
							color = Color.Black,
							fontSize = 16.sp,
							fontWeight = FontWeight.Medium
						)
					}
				}
			}

		}
	}
}

@Composable
private fun RecipeTitleSection(savedRecipeViewModel: SavedRecipeViewModel, recipe: RecipeFire?) {
	val savedIds = savedRecipeViewModel.savedIds.collectAsState()

	Row(
		modifier = Modifier
			.fillMaxWidth()
			.padding(horizontal = 16.dp),
		verticalAlignment = Alignment.CenterVertically,
		horizontalArrangement = Arrangement.SpaceBetween
	) {
		Text(
			text = recipe?.name ?: "",
			style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
			maxLines = 2,
			overflow = TextOverflow.Ellipsis
		)
		IconButton(
			onClick = { recipe?.let { savedRecipeViewModel.toggleBookmark(it) } },
			modifier = Modifier.padding(8.dp)
		) {
			Icon(
				painter = painterResource(
					id = if (recipe != null && savedIds.value.contains(recipe.id)) {
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

@Composable
private fun AuthorRow(viewModel: RecipeViewModel, recipe: RecipeFire?, isFollowing: Boolean, isFollowLoading: Boolean, onFollowClick: (String) -> Unit, onClick: (Profile) -> Unit
) {
	val contributor = viewModel.contributor.value
	Row(
		modifier = Modifier
			.fillMaxWidth()
			.padding(horizontal = 16.dp),
		verticalAlignment = Alignment.CenterVertically,
		horizontalArrangement = Arrangement.SpaceBetween
	) {
		Row(verticalAlignment = Alignment.CenterVertically) {
			AsyncImage(
				model = ImageRequest.Builder(LocalContext.current)
					.data(contributor?.profileUrl)
					.placeholder(R.drawable.profile_img)
					.crossfade(true)
					.build(),
				contentDescription =  recipe?.name,
				contentScale = ContentScale.Crop,
				modifier = Modifier
					.size(48.dp)
					.clip(CircleShape)
					.clickable { contributor?.let { onClick(it) } }
			)
			Spacer(Modifier.width(12.dp))
			Column {
				Text(text = recipe?.contributorName ?: "", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium))
			}
		}
		// Only show follow button if the contributor is not the current user
		val isSelf = contributor?.id == viewModel.user.value
		if (!isSelf) {
			Button(
				onClick = {
					if (contributor?.id != null) {
						onFollowClick(contributor.id)
					}
				},
				shape = RoundedCornerShape(12.dp),
				colors = ButtonDefaults.buttonColors(
					containerColor = if (isFollowing) Color.Gray else colorResource(R.color.green_but)
				),
				enabled = !isFollowLoading
			) {
				if (isFollowLoading){
					CircularProgressIndicator(
						modifier = Modifier.size(16.dp),
						color = Color.White,
						strokeWidth = 2.dp
					)
				}
				else {
					Text(
						text = if (isFollowing) "Following" else "Follow", color = Color.White
					)
				}
			}
		}
	}
}

@Composable
private fun Tabs(selectedTab: RecipeTab, onTabSelected: (RecipeTab) -> Unit) {
	Row(
		modifier = Modifier
			.fillMaxWidth()
			.padding(horizontal = 16.dp),
		horizontalArrangement = Arrangement.spacedBy(12.dp)
	) {
		@Composable
		fun tabButton(text: String, active: Boolean, onClick: () -> Unit) {
			val bg = if (active) colorResource(R.color.green_but) else Color(0xFFE8F2EE)
			val fg = if (active) Color.White else colorResource(R.color.green_but)
			Box(
				modifier = Modifier
					.weight(1f)
					.clip(RoundedCornerShape(12.dp))
					.background(bg)
					.clickable { onClick() }
					.padding(vertical = 12.dp),
				contentAlignment = Alignment.Center
			) {
				Text(text = text, color = fg, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
			}
		}
		tabButton("Ingredient", selectedTab == Ingredient) { onTabSelected(Ingredient) }
		tabButton("Procedure", selectedTab == Procedure) { onTabSelected(Procedure) }
	}
}

@Composable
private fun MetaRow(left: String, right: String) {
	Row(
		modifier = Modifier
			.fillMaxWidth()
			.padding(horizontal = 16.dp, vertical = 8.dp),
		horizontalArrangement = Arrangement.SpaceBetween,
		verticalAlignment = Alignment.CenterVertically
	) {
		Text(text = left, color = Color.Gray)
		Text(text = right, color = Color.Gray)
	}
}

@Composable
private fun IngredientItem(ingredient: com.example.appetite.models.Ingredient) {
	Row(
		modifier = Modifier
			.fillMaxWidth()
			.padding(horizontal = 16.dp, vertical = 6.dp)
			.clip(RoundedCornerShape(12.dp))
			.background(Color(0xFFF3F5F7))
			.padding(14.dp),
		verticalAlignment = Alignment.CenterVertically
	) {
		// Use a placeholder icon since we don't have ingredient-specific icons
		Box(
			modifier = Modifier
				.size(40.dp)
				.clip(RoundedCornerShape(8.dp))
				.background(Color(0xFFE8F2EE)),
			contentAlignment = Alignment.Center
		) {
			Icon(
				imageVector = Icons.Default.Star,
				contentDescription = null,
				tint = Color(0xFF2E8B57),
				modifier = Modifier.size(20.dp)
			)
		}
		Spacer(Modifier.width(12.dp))
		Text(
			text = ingredient.getDisplayName(),
			style = MaterialTheme.typography.bodyLarge,
			modifier = Modifier.weight(1f)
		)
		Text(
			text = ingredient.getDisplayQuantity() ?: "", 
			style = MaterialTheme.typography.bodyMedium, 
			color = Color.Gray
		)
	}
}



@Composable
private fun StepItem(step: Int, description: String) {
	Column(
		modifier = Modifier
			.fillMaxWidth()
			.padding(horizontal = 16.dp, vertical = 6.dp)
			.clip(RoundedCornerShape(12.dp))
			.background(Color(0xFFF3F5F7))
			.padding(16.dp)
	) {
		Text(text = "Step $step", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
		Spacer(Modifier.height(8.dp))
		Text(text = description, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
	}
}


@Composable
fun RateRecipePopup(
	show: Boolean,
	currentRating: Int?, // null if not rated
	onRate: (Int) -> Unit,
	onDismiss: () -> Unit
) {
	if (!show) return

	var selectedRating by remember { mutableIntStateOf(currentRating ?: 0) }
	Text("Current Rating: $currentRating")

	Box(
		Modifier
			.fillMaxSize()
			.background(Color.Black.copy(alpha = 0.4f))
			.clickable { onDismiss() }
	) {
		Surface(
			modifier = Modifier
				.align(Alignment.Center)
				.width(300.dp),
			shape = RoundedCornerShape(16.dp),
			color = Color.White,
			shadowElevation = 0.dp
		) {
			Column(
				Modifier.padding(24.dp),
				horizontalAlignment = Alignment.CenterHorizontally
			) {
				Text("Rate Recipe")
				Spacer(Modifier.height(16.dp))
				Row {
					(1..5).forEachIndexed { index, star ->
						Icon(
							painter = painterResource(
								id = if ((currentRating ?: selectedRating) < star)
									R.drawable.star_unfilled_ic
								else
									R.drawable.star_ic
							),
							contentDescription = null,
							modifier = Modifier
								.size(32.dp)
								.clickable(enabled = currentRating == null) {
									selectedRating = star
								},
							tint = Color(0xFFFFC107)
						)
						if (index < 4) {
							Spacer(modifier = Modifier.width(8.dp))
						}
					}
				}
				Spacer(Modifier.height(24.dp))
				val buttonText = if (currentRating != null) "Already Rated" else "Rate"
				Button(
					onClick = {
						if (currentRating == null && selectedRating > 0) {
							onRate(selectedRating)
							onDismiss()
						}
					},
					enabled = currentRating == null && selectedRating > 0,
					shape = RoundedCornerShape(12.dp),
					colors = ButtonDefaults.buttonColors(
						containerColor = if (selectedRating > 0)  Color(0xFF129575) else Color.Gray ,
						contentColor = Color.White
					),
				) {
					Text(buttonText)
				}
			}
		}
	}
}
