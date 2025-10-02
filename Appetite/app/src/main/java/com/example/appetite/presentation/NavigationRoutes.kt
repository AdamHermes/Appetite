package com.example.appetite.presentation

object NavigationRoutes {
    const val Home = "home"
    const val Saved = "saved"
    const val Noti = "noti"
    const val Profile = "profile"
    const val Login = "login"
    const val SignUp = "sign_up"
    const val RecipeDetail = "recipe_detail"
    const val SearchRecipe = "search_recipe"
    fun profileDetailWithId(userId: String) = "$Profile/$userId"
    fun recipeDetailWithId(recipeId: String) = "$RecipeDetail/$recipeId"
    fun recipeDetailWithData(recipe: com.example.appetite.models.RecipeFire) = "$RecipeDetail/${recipe.id ?: ""}"
    const val Reviews = "reviews/{recipeId}"
    const val LeaderBoard = "leader_board"
    const val Splash = "splash"
    const val EditProfile = "edit_profile"
    const val SearchIngredients = "searchIngredients/{uri}"
    fun searchIngredientsWithUri(uri: String) = "searchIngredients/$uri"
    const val SimilarRecipes = "similarRecipes/{uri}"
    const val VoiceCookingAgent = "voiceCookingAgent"
    fun similarRecipesWithUri(uri: String) = "similarRecipes/$uri"

    const val FollowList = "follow_list/{userId}/{mode}"
    fun followListRoute(userId: String, mode: String) = "follow_list/$userId/$mode"


    fun newPostWithUri(uri: String) = "new_post?imageUri=$uri"
}