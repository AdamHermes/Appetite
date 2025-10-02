package com.example.appetite.models

data class Profile(
    val id: String? = null,
    val profileUrl: String? = null,
    val name: String,
    val bio: String,
    val recipesCount: Int,
    val followersCount: Long? = null,
    val followingCount: Int? = null,
    val recipes: List<RecipeFire> = emptyList()
)

data class ProfileCreate(
    val name: String,
    val email: String
)

data class ProfileUpdateRequest(
    val name: String? = null,
    val bio: String? = null,
    val profileUrl: String? = null
)

data class FollowStatusResponse(
    val success: Boolean,
    val isFollowing: Boolean
)

data class FollowListResponse(
    val count: Int,
    val followers: List<Profile>? = null,
    val following: List<Profile>? = null
)

data class UserSearchResponse(
    val id: String,
    val name: String,
    val profileUrl: String? = null
)