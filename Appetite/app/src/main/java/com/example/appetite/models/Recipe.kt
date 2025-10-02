package com.example.appetite.models
import java.util.Date


data class Recipe(
    val name: String,
    val time: Int,
    val rating: Double,
    val author: String,
    val imageRes: Int
)


data class Ingredient(
    val name: String? = null,
    val quantity: String? = null,
    val ingredient: String? = null,  
    val measure: String? = null      
) {
    fun getDisplayName(): String {
        return name ?: ingredient ?: "Unknown"
    }
    fun getDisplayQuantity(): String? {
        return quantity ?: measure
    }
}

data class RecipeFire(
    val id: String? = null,
    val name: String = "Unknown Recipe",
    val minutes: Int = 0,
    val ratingAvg: Double? = null,
    val contributorId: String? = null,
    val contributorName: String? = null,
    val thumbnail: String? = null,
    val instructions: String? = null,
    val category: String? = null,
    val area: String? = null,
    val popularity: Double? = null,
    val ingredients: List<Ingredient>? = null,
    val steps: List<String>? = null,
    val description: String? = null,
    val tags: List<String>? = null,
    val images: List<String>? = null,
    val createdAt: Date? = null,
    val updatedAt: Date? = null,
    val saveCount: Int? = 0
)

data class RecipeResponse(
    val items: List<RecipeFire>,
    val nextPageToken: String? = null
)

data class SaveRecipeRequest(
    val recipe_id: String
)

// Response from saving a recipe
data class SaveRecipeResponse(
    val savedrecipe: RecipeFire
)

// Response for getting saved recipes
data class SavedRecipesResponse(
    val savedrecipes: List<RecipeFire>
)

data class UnsaveRecipeResponse(
    val removedRecipe: RecipeFire
)


data class SimilarRecipeResponse(
    val results: List<RecipeFire>
)
data class SuggestedRecipeResponse(
    val detected_ingredients: List<String>,
    val results: List<RecipeFire>
)
data class VoiceAgentResponse(
    val intent: String,
    val text_response: String,
    val new_step_index: Int
)