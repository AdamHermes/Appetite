package com.example.appetite.presentation.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.appetite.presentation.NavigationRoutes
import com.example.appetite.presentation.home.HomeScreen
import com.example.appetite.presentation.home.HomeViewModel
import com.example.appetite.presentation.login.SignInScreen
import com.example.appetite.presentation.recipeDetail.RecipeViewModel
import com.example.appetite.presentation.savedRecipe.SavedRecipeScreen
import com.example.appetite.presentation.userProfile.UserProfileScreen
import com.example.appetite.presentation.userProfile.UserProfileViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.appetite.presentation.editProfile.UpdateProfileScreen
import com.example.appetite.presentation.addRecipe.AddRecipeScreen
import com.example.appetite.presentation.leaderboard.LeaderboardScreen
import com.example.appetite.presentation.leaderboard.LeaderboardViewModel
import com.example.appetite.presentation.signUp.SignUpScreen
import com.example.appetite.presentation.recipeDetail.RecipeDetailScreen
import com.example.appetite.presentation.reviews.ReviewsScreen
import com.example.appetite.presentation.savedRecipe.SavedRecipeViewModel
import com.example.appetite.presentation.searchRecipe.SearchRecipeScreen
import com.example.appetite.presentation.searchRecipe.SearchViewModel
import com.example.appetite.presentation.searchSimIngredients.IngredientsRecipeScreen
import com.example.appetite.presentation.searchSimIngredients.SearchIngredientsViewModel
import com.example.appetite.presentation.searchSimRecipe.SearchSimRecipeViewModel
import com.example.appetite.presentation.searchSimRecipe.SimilarRecipeScreen
import com.example.appetite.presentation.splash.SplashScreen
import com.example.appetite.presentation.voiceCooking.VoiceCookingScreen
import com.example.appetite.presentation.voiceCooking.VoiceCookingViewModel
import com.example.appetite.presentation.followList.FollowListScreen
import com.example.appetite.presentation.followList.FollowListViewModel

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = NavigationRoutes.Splash,
        route = "root_graph" // Parent graph for shared ViewModels
    ) {
        composable(NavigationRoutes.Home) { backStackEntry ->
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry("root_graph")
            }
            val homeViewModel: HomeViewModel =
                hiltViewModel(parentEntry)
            val profileViewModel: UserProfileViewModel =
                hiltViewModel(parentEntry)
            val recipeViewModel: RecipeViewModel =
                hiltViewModel(parentEntry)

            HomeScreen(navController, homeViewModel, profileViewModel, recipeViewModel)
        }
        composable(NavigationRoutes.Saved) { backStackEntry ->
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry("root_graph")
            }
            val savedRecipeViewModel: SavedRecipeViewModel = hiltViewModel(parentEntry)

            SavedRecipeScreen(navController, savedRecipeViewModel)
        }
        composable(NavigationRoutes.Profile) { backStackEntry ->
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry("root_graph")
            }
            val profileViewModel: UserProfileViewModel =
                hiltViewModel(parentEntry)
            val homeViewModel : HomeViewModel = hiltViewModel(parentEntry)
            UserProfileScreen(navController, profileViewModel, homeViewModel)
        }
        composable(
            route = "${NavigationRoutes.Profile}/{profileId}",
            arguments = listOf(
                navArgument("profileId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = ""
                }
            )
        ) { backStackEntry ->
            val profileViewModel: UserProfileViewModel = hiltViewModel()
            val profileId = backStackEntry.arguments?.getString("profileId") ?: ""
            profileViewModel.loadUserProfile(profileId)
            UserProfileScreen(
                navController = navController,
                profileViewModel = profileViewModel,
                viewedUserId = profileId
            )
        }
        composable(
            route = NavigationRoutes.FollowList,
            arguments = listOf(
                navArgument("userId") { type = NavType.StringType },
                navArgument("mode") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            val mode = backStackEntry.arguments?.getString("mode") ?: "followers"
            FollowListScreen(
                navController = navController,
                userId = userId,
                modeString = mode,
                viewModel = hiltViewModel<FollowListViewModel>(),
                onBack = { navController.popBackStack() }
            )
        }
        composable(NavigationRoutes.Login) {
            // No shared ViewModel needed here
            backstackEntry -> val parentEntry = remember(backstackEntry) {
                navController.getBackStackEntry("root_graph")
            }
            val userProfileViewModel : UserProfileViewModel = hiltViewModel(parentEntry)
            val savedRecipeViewModel : SavedRecipeViewModel = hiltViewModel(parentEntry)
            SignInScreen(
                onSignInSuccess = {
                    navController.navigate(NavigationRoutes.Home) {
                        userProfileViewModel.loadUserProfile()
                        savedRecipeViewModel.refreshSavedRecipes()
                        popUpTo(NavigationRoutes.Login) { inclusive = true }
                    }
                },
                onNavigateToSignUp = {
                    navController.navigate(NavigationRoutes.SignUp)
                }
            )
        }
        composable(NavigationRoutes.SignUp) {
            SignUpScreen(
                onNavigateToLogin = {
                    navController.navigate(NavigationRoutes.Login) {
                        popUpTo(NavigationRoutes.SignUp) { inclusive = true }
                    }
                }
            )
        }
        composable(
            route = "${NavigationRoutes.RecipeDetail}/{recipeId}",
            arguments = listOf(
                navArgument("recipeId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = ""
                }
            )
        ) { backStackEntry ->
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry("root_graph")
            }

            val recipeViewModel: RecipeViewModel = hiltViewModel(parentEntry)
            val savedRecipeViewModel: SavedRecipeViewModel = hiltViewModel(parentEntry)
            val userProfileViewModel: UserProfileViewModel = hiltViewModel(parentEntry)
            val recipeId = backStackEntry.arguments?.getString("recipeId") ?: ""

            RecipeDetailScreen(
                navController = navController,
                viewModel = recipeViewModel,
                savedRecipeViewModel = savedRecipeViewModel,
                userProfileViewModel = userProfileViewModel,
                recipeId = recipeId,
                onBack = { navController.popBackStack() },
                seeReview = {
                    val id = recipeViewModel.selectedRecipeFire.value?.id ?: recipeId
                    navController.navigate("reviews/$id")
                }
            )
        }

        composable(NavigationRoutes.SearchRecipe) { backStackEntry ->
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry("root_graph")
            }
            val vm: SearchViewModel = hiltViewModel(parentEntry)
            val recipeViewModel: RecipeViewModel = hiltViewModel(parentEntry)
            val userProfileViewModel: UserProfileViewModel = hiltViewModel(parentEntry)
            SearchRecipeScreen(navController=navController, viewModel= vm,userProfileViewModel = userProfileViewModel,recipeViewModel = recipeViewModel)
        }
        composable(
            route = NavigationRoutes.Reviews,
            arguments = listOf(
                navArgument("recipeId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val recipeId = backStackEntry.arguments?.getString("recipeId") ?: ""
            ReviewsScreen(recipeId = recipeId, navController = navController)
        }
        composable(NavigationRoutes.LeaderBoard) { backStackEntry ->
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry("root_graph")
            }
            val leaderboardViewModel: LeaderboardViewModel=
                hiltViewModel(parentEntry)
            LeaderboardScreen(navController, leaderboardViewModel)
        }

        composable(NavigationRoutes.Splash) { backStackEntry ->
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry("root_graph")
            }
            val userProfileViewModel: UserProfileViewModel = hiltViewModel(parentEntry)
            val savedRecipeViewModel: SavedRecipeViewModel = hiltViewModel(parentEntry)
            SplashScreen(navController, userProfileViewModel, savedRecipeViewModel)
        }
        composable(
            "similarRecipes/{uri}",
            arguments = listOf(navArgument("uri") { type = NavType.StringType })
        ) { backStackEntry ->
            val viewModel: SearchSimRecipeViewModel = hiltViewModel()
            val uriString = backStackEntry.arguments?.getString("uri")
            val uri = uriString?.let { Uri.parse(it) }
            val context = LocalContext.current
            val contentResolver = context.contentResolver
            LaunchedEffect(uri, contentResolver) {
                uri?.let { viewModel.searchFromUri(contentResolver, it) }
            }

            SimilarRecipeScreen(navController, viewModel)
        }
        composable(
            "searchIngredients/{uri}",
            arguments = listOf(navArgument("uri") { type = NavType.StringType })
        ) { backStackEntry ->
            val viewModel: SearchIngredientsViewModel = hiltViewModel()
            val uriString = backStackEntry.arguments?.getString("uri")
            val uri = uriString?.let { Uri.parse(it) }
            val context = LocalContext.current
            val contentResolver = context.contentResolver
            LaunchedEffect(uri, contentResolver) {
                uri?.let { viewModel.searchFromUri(contentResolver, it) }
            }
            IngredientsRecipeScreen(navController,viewModel)

        }
        composable(NavigationRoutes.EditProfile) { backStackEntry ->
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry("root_graph")
            }
            val profileViewModel: UserProfileViewModel = hiltViewModel(parentEntry)

            UpdateProfileScreen(
                viewModel = profileViewModel,
                onSuccessNavigate = { navController.popBackStack() }
            )
        }
        composable(
            route = "new_post?imageUri={imageUri}",
            arguments = listOf(
                navArgument("imageUri") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry("root_graph")
            }
            val profileViewModel: UserProfileViewModel = hiltViewModel(parentEntry)
            val imageUri = backStackEntry.arguments?.getString("imageUri")?.let { Uri.parse(it) }
            AddRecipeScreen(
                userViewModel = profileViewModel,
                navController = navController,
                initialImageUri = imageUri
            )
        }
        composable(
            route = NavigationRoutes.VoiceCookingAgent + "/{recipeId}",
            arguments = listOf(
                navArgument("recipeId") {
                    type = NavType.StringType
                    nullable = false
                }
            )
        ) {
            val viewModel: VoiceCookingViewModel = hiltViewModel()
            VoiceCookingScreen(
                navController = navController,
                viewModel = viewModel
            )
        }

    }
}
