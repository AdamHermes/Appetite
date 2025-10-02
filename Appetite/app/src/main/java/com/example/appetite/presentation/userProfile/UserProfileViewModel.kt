package com.example.appetite.presentation.userProfile

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appetite.R
import com.example.appetite.models.Profile
import com.example.appetite.models.ProfileUpdateRequest
import com.example.appetite.repository.AuthRepository
import com.example.appetite.repository.RecipeRepository
import com.example.appetite.repository.UserInfoRepository
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

@HiltViewModel
class UserProfileViewModel @Inject constructor() : ViewModel() {

    private val _profile = MutableStateFlow(
        Profile(
            profileUrl = "",
            name = "Loading...",
            bio = "",
            recipesCount = 0,
            followersCount = 0,
            followingCount = 0,
            recipes = emptyList()
        )
    )
    val profile: StateFlow<Profile> = _profile

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        Log.d("UserProfileVM", "init -> loading profile with default (me)")
//        loadUserProfile()
    }

    fun loadUserProfile(uid: String? = null) {
        viewModelScope.launch {
            try {
                Log.d("UserProfileVM", "loadUserProfile() called with uid=$uid")

                val userInfo = if (uid == null) {
                    Log.d("UserProfileVM", "Fetching MY user info")
                    UserInfoRepository.getMyInfo()
                } else {
                    Log.d("UserProfileVM", "Fetching OTHER user info for uid=$uid")
                    UserInfoRepository.getUserInfo(uid)
                }

                Log.d("UserProfileVM", "UserInfo fetched: $userInfo")

                val myRecipes = if (uid == null) {
                    Log.d("UserProfileVM", "Fetching MY recipes")
                    RecipeRepository.getUserRecipes() // calls /me
                } else {
                    Log.d("UserProfileVM", "Fetching OTHER user's recipes for uid=$uid")
                    RecipeRepository.getOtherUserRecipes(uid) // you need this endpoint already
                }

                Log.d("UserProfileVM", "Recipes fetched: count=${myRecipes.size}")

                _profile.value = Profile(
                    profileUrl = userInfo.profileUrl,
                    name = userInfo.name,
                    bio = userInfo.bio,
                    recipesCount = userInfo.recipesCount,
                    followersCount = userInfo.followersCount,
                    followingCount = userInfo.followingCount,
                    recipes = myRecipes
                )

                Log.d("UserProfileVM", "Profile updated: ${_profile.value}")

            } catch (e: Exception) {
                Log.e("UserProfileVM", "Error loading user profile", e)
                _error.value = e.message
            }
        }
    }

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun updateProfile(name: String?, bio: String?, profileUrl: String?) {
        _isLoading.value = true
        _error.value = null
        viewModelScope.launch {
            try {
                val updated = UserInfoRepository.updateProfile(
                    update = ProfileUpdateRequest(name, bio, profileUrl)
                )
                val current = _profile.value
                _profile.value = current.copy(
                    name = updated.name,
                    bio = updated.bio,
                    profileUrl = updated.profileUrl
                    // Keep recipesCount, followersCount, followingCount, recipes unchanged
                )
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }
    fun uploadProfilePhoto(context: Context, uri: Uri) {
        viewModelScope.launch {
            try {
                // Open stream from content URI
                val inputStream = context.contentResolver.openInputStream(uri)
                val tempFile = File.createTempFile("upload", ".jpg", context.cacheDir)
                val outputStream = FileOutputStream(tempFile)
                inputStream?.copyTo(outputStream)
                inputStream?.close()
                outputStream.close()

                // Build multipart body
                val requestFile = tempFile.asRequestBody("image/*".toMediaTypeOrNull())
                val body = MultipartBody.Part.createFormData("file", tempFile.name, requestFile)

                // Upload to backend
                val updatedProfile = UserInfoRepository.uploadProfilePhoto(body)

                // Update UI state
                _profile.value = _profile.value.copy(profileUrl = updatedProfile.profileUrl)

            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }
}


