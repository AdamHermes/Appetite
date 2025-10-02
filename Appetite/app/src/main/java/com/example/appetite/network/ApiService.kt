// network/ApiService.kt
package com.example.appetite.network

import com.example.appetite.models.CommentIn
import com.example.appetite.models.CommentResponse
import com.example.appetite.models.FollowListResponse
import com.example.appetite.models.FollowStatusResponse
import com.example.appetite.models.MeResponse
import com.example.appetite.models.MyRatingResponse
import com.example.appetite.models.Profile
import com.example.appetite.models.ProfileCreate
import com.example.appetite.models.ProfileUpdateRequest
import com.example.appetite.models.RateIn
import com.example.appetite.models.RecipeResponse
import com.example.appetite.models.RecipeFire
import com.example.appetite.models.ResponseMessage
import com.example.appetite.models.SaveRecipeRequest
import com.example.appetite.models.SaveRecipeResponse
import com.example.appetite.models.SavedRecipesResponse
import com.example.appetite.models.SimilarRecipeResponse
import com.example.appetite.models.SuggestedRecipeResponse
import com.example.appetite.models.UnsaveRecipeResponse
import com.example.appetite.models.VoiceAgentResponse
import com.example.appetite.models.UserSearchResponse
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @GET("/api/v1/auth/me")
    suspend fun me(): MeResponse

    @GET("/api/v1/recipes/me")
    suspend fun getUserRecipes(): RecipeResponse

    @GET("/api/v1/recipes/by-contributor/{uid}")
    suspend fun getOtherUserRecipes(@Path("uid") uid: String): RecipeResponse

    @GET("/api/v1/user/me")
    suspend fun getMyInfo(): Profile

    @GET("/api/v1/user/info/{uid}")
    suspend fun getUserInfo(@Path("uid") uid: String): Profile

    @GET("/api/v1/user/all")
    suspend fun getAllUsers(): List<Profile>

    @POST("/api/v1/user/me")
    suspend fun createMyProfile(
        @Body profile: ProfileCreate
    ): Profile

    @PATCH("/api/v1/user/me")
    suspend fun updateProfile(
        @Body update: ProfileUpdateRequest,
    ): Profile

    @Multipart
    @POST("/api/v1/user/me/photo")
    suspend fun uploadProfilePhoto(
        @Part file: MultipartBody.Part
    ): Profile

    @Multipart
    @POST("/api/v1/recipes/")
    suspend fun createRecipe(
        @Part data: MultipartBody.Part,
        @Part file: MultipartBody.Part
    ): RecipeFire

    @POST("/api/v1/recipes/save")
    suspend fun saveRecipe(
        @Body body: SaveRecipeRequest
    ): SaveRecipeResponse

    @POST("/api/v1/recipes/unsave")
    suspend fun unsaveRecipe(
        @Body body: SaveRecipeRequest
    ): UnsaveRecipeResponse

    @GET("/api/v1/recipes/saved")
    suspend fun getSavedRecipes(
    ): SavedRecipesResponse


    @GET("/api/v1/recipes/id/{id}")
    suspend fun getRecipeById(@Path("id") id: String): RecipeFire

    @DELETE("/api/v1/recipes/id/{id}")
    suspend fun deleteRecipe(@Path("id") id: String): ResponseMessage

    @GET("/api/v1/recipes/all")
    suspend fun getAllRecipes(): RecipeResponse

    @GET("/api/v1/recipes/search")
    suspend fun searchRecipes(
        @Query("name") name: String? = null,
        @Query("sort") sort: String? = null, // popularity | rating
        @Query("categories") categories: List<String>? = null,
        @Query("areas") areas: List<String>? = null,
        @Query("minutes_bucket") minutesBucket: String? = null, // quick|short|medium|long
        @Query("limit") limit: Int? = 20,
        @Query("page_token") pageToken: String? = null,
        @Query("enable_fuzzy") enableFuzzy: Boolean? = true,
        @Query("fuzzy_threshold") fuzzyThreshold: Int? = 60,
    ): RecipeResponse

    @GET("/api/v1/recipes/{recipeId}/comments")
    suspend fun getComments(
        @Path("recipeId") recipeId: String,
        @Query("limit") limit: Int = 20
    ): CommentResponse

    // 🔹 Post a new comment
    @POST("/api/v1/recipes/{recipeId}/comments")
    suspend fun addComment(
        @Path("recipeId") recipeId: String,
        @Body comment: CommentIn
    ): ResponseMessage

    @GET("/api/v1/recipes/{recipeId}/my-rating")
    suspend fun getMyRating(@Path("recipeId") recipeId: String): MyRatingResponse

    @POST("/api/v1/recipes/{recipeId}/rate")
    suspend fun rateRecipe(
        @Path("recipeId") recipeId: String,
        @Body rating: RateIn
    ): ResponseMessage


    @GET("api/v1/similarity/by-url")
    suspend fun searchSimilarRecipes(
        @Query("url") url: String,
        @Query("top_k") topK: Int = 8
    ): SimilarRecipeResponse

    @Multipart
    @POST("api/v1/similarity/by-upload")
    suspend fun searchSimilarByUpload(
        @Part file: MultipartBody.Part,
        @Query("top_k") topK: Int = 8
    ): SimilarRecipeResponse

    @Multipart
    @POST("api/v1/ingredients/by-upload")
    suspend fun searchIngredientsByUpload(
        @Part file: MultipartBody.Part,
        @Query("top_k") topK: Int = 8
    ): SuggestedRecipeResponse

    @POST("/api/v1/follow/{targetUid}")
    suspend fun followUser(@Path("targetUid") targetUid: String): ResponseMessage

    @DELETE("/api/v1/follow/{targetUid}")
    suspend fun unfollowUser(@Path("targetUid") targetUid: String): ResponseMessage

    @GET("/api/v1/follow/status/{targetUid}")
    suspend fun checkFollowStatus(@Path("targetUid") targetUid: String): FollowStatusResponse

    @GET("/api/v1/follow/followers/{user_id}")
    suspend fun getFollowers(@Path("user_id") userId: String): FollowListResponse

    @GET("/api/v1/follow/following/{user_id}")
    suspend fun getFollowing(@Path("user_id") userId: String): FollowListResponse

    @Multipart
    @POST("/api/v1/voice-agent/{recipeId}")
    suspend fun sendVoiceCommand(
        @Path("recipeId") recipeId: String,
        @Part("current_step") currentStep: Int,
        @Part file: MultipartBody.Part
    ): VoiceAgentResponse

    @GET("/api/v1/user/search")
    suspend fun searchUsers(
        @Query("name") name: String? = null,
        @Query("limit") limit: Int? = 20
    ): List<UserSearchResponse>

    companion object {
        const val VOICE_AGENT_WS = "ws/v1/voice-agent/"
    }
}