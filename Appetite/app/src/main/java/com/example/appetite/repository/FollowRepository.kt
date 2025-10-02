package com.example.appetite.repository

import com.example.appetite.models.FollowListResponse
import com.example.appetite.models.FollowStatusResponse
import com.example.appetite.models.ResponseMessage
import com.example.appetite.network.ApiClient

object FollowRepository {
    suspend fun followUser(targetUid: String): ResponseMessage {
        return ApiClient.api.followUser(targetUid)
    }

    suspend fun unfollowUser(targetUid: String): ResponseMessage {
        return ApiClient.api.unfollowUser(targetUid)
    }

    suspend fun checkFollowStatus(targetUid: String): FollowStatusResponse {
        return ApiClient.api.checkFollowStatus(targetUid)
    }

    suspend fun getFollowers(userId: String): FollowListResponse {
        return ApiClient.api.getFollowers(userId)
    }

    suspend fun getFollowing(userId: String): FollowListResponse {
        return ApiClient.api.getFollowing(userId)
    }


}