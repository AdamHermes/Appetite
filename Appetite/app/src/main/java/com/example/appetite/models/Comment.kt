package com.example.appetite.models

data class Comment(
    val userId: String,
    val userName: String,
    val text: String,
    val createdAt: Double,
    val profilePhotoUrl: String? = null // <-- added
)
data class CommentRequest(val text: String)
data class CommentIn(val text: String)
data class CommentResponse(val items: List<Comment>)
data class ResponseMessage(val message: String)