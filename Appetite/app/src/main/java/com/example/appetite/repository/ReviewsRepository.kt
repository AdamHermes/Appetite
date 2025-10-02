package com.example.appetite.repository

import com.example.appetite.network.ApiClient
import com.example.appetite.models.Comment
import com.example.appetite.models.CommentIn

object ReviewsRepository {
    suspend fun getComments(recipeId: String, limit: Int = 20): List<Comment> {
        return ApiClient.api.getComments(recipeId, limit).items
    }

    suspend fun addComment(recipeId: String, text: String) {
        ApiClient.api.addComment(recipeId, CommentIn(text))
    }
}