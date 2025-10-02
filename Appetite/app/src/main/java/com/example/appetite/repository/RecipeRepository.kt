package com.example.appetite.repository

import android.content.ContentResolver
import android.net.Uri
import android.util.Log
import com.example.appetite.models.Profile
import com.example.appetite.models.RateIn
import com.example.appetite.models.RecipeFire
import com.example.appetite.models.SaveRecipeRequest
import com.example.appetite.models.SuggestedRecipeResponse
import com.example.appetite.network.ApiClient
import com.example.appetite.network.ApiClient.api
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

object RecipeRepository{

    suspend fun getAllRecipes(): List<RecipeFire> {
        try {
            val response = ApiClient.api.getAllRecipes()
            println("RecipeRepository: getAllRecipes response items count: ${response.items.size}")
            response.items.forEach { recipe ->
                println("RecipeRepository: Loaded recipe=${recipe.name}, saveCount=${recipe.saveCount}")
            }

            return response.items
        } catch (e: Exception) {
            println("RecipeRepository: getAllRecipes error: ${e.message}")
            e.printStackTrace()
            return emptyList()
        }

    }

    suspend fun getUserRecipes(): List<RecipeFire> {
        try {
            val response = ApiClient.api.getUserRecipes()  // now returns RecipeResponse
            println("RecipeRepository: getUserRecipes response items count: ${response.items.size}")
            if (response.items.isNotEmpty()) {
                val firstRecipe = response.items[0]
                println("RecipeRepository: First recipe - ${firstRecipe.name}")
                println("RecipeRepository: First recipe ingredients count: ${firstRecipe.ingredients?.size ?: 0}")
                println("RecipeRepository: First recipe ingredients: ${firstRecipe.ingredients}")
            }
            return response.items
        } catch (e: Exception) {
            println("RecipeRepository: getUserRecipes error: ${e.message}")
            e.printStackTrace()
            return emptyList()
        }
    }

    suspend fun getRecipeById(id: String): RecipeFire {
        try {
            println("RecipeRepository: Fetching recipe by ID: $id")
            val recipe = ApiClient.api.getRecipeById(id)
            println("RecipeRepository: Recipe fetched successfully - ${recipe.name}")
            return recipe
        } catch (e: Exception) {
            println("RecipeRepository: getRecipeById error: ${e.message}")
            e.printStackTrace()
            throw e
        }
    }
    suspend fun getOtherUserRecipes(uid: String):  List<RecipeFire> {
        return ApiClient.api.getOtherUserRecipes(uid).items
    }
    // ------------------ Save a recipe ------------------
    suspend fun saveRecipe(recipeId: String): RecipeFire {
        val response = ApiClient.api.saveRecipe(SaveRecipeRequest(recipeId))
        return response.savedrecipe
    }

    suspend fun unsaveRecipe(recipeId: String): RecipeFire {
        val response = ApiClient.api.unsaveRecipe(SaveRecipeRequest(recipeId))
        return response.removedRecipe
    }
    // ------------------ Get saved recipes ------------------
    suspend fun getSavedRecipes(): List<RecipeFire> {
        val response = ApiClient.api.getSavedRecipes()
        return response.savedrecipes
    }
    suspend fun searchRecipes(
        name: String? = null,
        sort: String? = null,
        categories: List<String>? = null,
        areas: List<String>? = null,
        minutesBucket: String? = null,
        limit: Int = 20,
        pageToken: String? = null,
        enableFuzzy: Boolean = true,
        fuzzyThreshold: Int = 60,
    ): Pair<List<RecipeFire>, String?> {
        try {
            val response = ApiClient.api.searchRecipes(
                name = name,
                sort = sort,
                categories = categories,
                areas = areas,
                minutesBucket = minutesBucket,
                limit = limit,
                pageToken = pageToken,
                enableFuzzy = enableFuzzy,
                fuzzyThreshold = fuzzyThreshold,
            )
            println("RecipeRepository: searchRecipes response items count: ${response.items.size}")
            println("RecipeRepository: searchRecipes response nextPageToken: ${response.nextPageToken}")
            return Pair(response.items, response.nextPageToken)
        } catch (e: Exception) {
            println("RecipeRepository: searchRecipes error: ${e.message}")
            e.printStackTrace()
            return Pair(emptyList(), null)
        }
    }
    suspend fun getMyRating(recipeId: String): Int? {
        val response = api.getMyRating(recipeId)
        return response.value

    }

    suspend fun rateRecipe(recipeId: String, rating: Int) {
        api.rateRecipe(recipeId, RateIn(rating))
    }
    //suspend fun addRecipe(recipe: Recipe): Recipe = ApiClient.api.addRecipe(recipe)
    //suspend fun deleteRecipe(id: String) = ApiClient.api.deleteRecipe(id)

//    suspend fun searchSimilarRecipes(imageUrl: String, topK: Int = 5): List<SimilarRecipe> {
//        return try {
//            val response = api.searchSimilarRecipes(imageUrl, topK)
//            response.results
//        } catch (e: Exception) {
//            emptyList() // or throw a domain exception
//        }
//    }
    suspend fun searchSimilarFromUri(
        resolver: ContentResolver,
        uri: Uri,
        topK: Int = 8
    ): List<RecipeFire> {
        return try {
            val inputStream = resolver.openInputStream(uri) ?: return emptyList()
            val bytes = inputStream.readBytes()
            inputStream.close()

            val requestFile = bytes.toRequestBody("image/*".toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("file", "image.jpg", requestFile)

            api.searchSimilarByUpload(body, topK).results
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun searchIngredientsFromUri(
        resolver: ContentResolver,
        uri: Uri,
        topK: Int = 8
    ): SuggestedRecipeResponse? {
        return try {
            val inputStream = resolver.openInputStream(uri) ?: return null
            val bytes = inputStream.readBytes()
            inputStream.close()

            val requestFile = bytes.toRequestBody("image/*".toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("file", "image.jpg", requestFile)

            api.searchIngredientsByUpload(body, topK)
        } catch (e: Exception) {
            null
        }
    }


    suspend fun createRecipe(
        resolver: ContentResolver,
        imageUri: Uri,
        recipeJson: String
    ): RecipeFire? {
        return try {
            val inputStream = resolver.openInputStream(imageUri) ?: return null
            val bytes = inputStream.readBytes()
            inputStream.close()

            val mimeType = resolver.getType(imageUri) ?: "image/jpeg"
            val requestFile = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
            val imagePart = MultipartBody.Part.createFormData("file", "image.jpg", requestFile)
            val dataPart = MultipartBody.Part.createFormData("data", recipeJson)

            Log.d("RecipeRepository", "Selected image MIME type: $mimeType")
            api.createRecipe(dataPart, imagePart)
        } catch (e: Exception) {
            Log.e("RecipeRepository", "createRecipe error: ${e.message}", e)
            null
        }
    }

    suspend fun deleteRecipe(id: String): Boolean {
        return try {
            api.deleteRecipe(id)
            true
        } catch (e: Exception) {
            false
        }
    }
}

