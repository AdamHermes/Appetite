// repository/AuthRepository.kt
package com.example.appetite.repository

import com.example.appetite.network.ApiClient
import com.example.appetite.models.MeResponse

object AuthRepository {
    suspend fun fetchMe(): MeResponse = ApiClient.api.me()
}


