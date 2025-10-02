package com.example.appetite.presentation.followList

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appetite.models.Profile
import com.example.appetite.presentation.userProfile.UserProfileViewModel
import com.example.appetite.repository.FollowRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FollowListViewModel @Inject constructor() : ViewModel() {

    enum class Mode { FOLLOWERS, FOLLOWING }

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _users = MutableStateFlow<List<Profile>>(emptyList())
    val users: StateFlow<List<Profile>> = _users

    private val _mode = MutableStateFlow(Mode.FOLLOWERS)
    val mode: StateFlow<Mode> = _mode
    fun load(userId: String, mode: Mode) {
        if (userId.isBlank()) return
        _mode.value = mode
        _isLoading.value = true
        _errorMessage.value = null
        viewModelScope.launch {
            try {
                val response = when (mode) {
                    Mode.FOLLOWERS -> FollowRepository.getFollowers(userId)
                    Mode.FOLLOWING -> FollowRepository.getFollowing(userId)
                }
                val list = when (mode) {
                    Mode.FOLLOWERS -> response.followers ?: emptyList()
                    Mode.FOLLOWING -> response.following ?: emptyList()
                }
                _users.value = list
            } catch (e: Exception) {
                _errorMessage.value = e.message
                _users.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }
}