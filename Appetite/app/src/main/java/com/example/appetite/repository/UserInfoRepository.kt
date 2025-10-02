package com.example.appetite.repository

import com.example.appetite.models.Profile
import com.example.appetite.models.ProfileCreate
import com.example.appetite.models.ProfileUpdateRequest
import com.example.appetite.network.ApiClient
import okhttp3.MultipartBody

object UserInfoRepository {

    suspend fun getMyInfo(): Profile {
        return ApiClient.api.getMyInfo()
    }

    suspend fun getUserInfo(uid: String):  Profile {
        return ApiClient.api.getUserInfo(uid)
    }

    suspend fun getAllUsers(): List<Profile> {
        return ApiClient.api.getAllUsers()
    }

    suspend fun createMyProfile(name: String, email: String): Profile {
        return ApiClient.api.createMyProfile(ProfileCreate(name, email))
    }

    suspend fun updateProfile(update: ProfileUpdateRequest): Profile {
        return ApiClient.api.updateProfile(update)
    }

    suspend fun uploadProfilePhoto(file: MultipartBody.Part): Profile {
        return ApiClient.api.uploadProfilePhoto(file)
    }
}
