package com.example.appetite.presentation.reviews

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.appetite.models.Comment
import com.example.appetite.repository.ReviewsRepository
import com.example.appetite.repository.UserInfoRepository

@HiltViewModel
class ReviewViewModel @Inject constructor() : ViewModel() {
    private val _comments = MutableStateFlow<List<Comment>>(emptyList())
    val comments: StateFlow<List<Comment>> = _comments

    private val _commentText = MutableStateFlow("")
    val commentText: StateFlow<String> = _commentText

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage


    // --- User Actions ---
    fun onCommentChanged(newText: String) {
        _commentText.value = newText
    }

    fun loadComments(recipeId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val result = ReviewsRepository.getComments(recipeId)
                val commentsWithPhotos = result.map { comment ->
                    val profile = try {
                        UserInfoRepository.getUserInfo(comment.userId)
                    } catch (e: Exception) {
                        null
                    }
                    comment.copy(profilePhotoUrl = profile?.profileUrl)
                }
                _comments.value = commentsWithPhotos
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("ReviewViewModel", e.message ?: "No error")
                _errorMessage.value = "Failed to load comments"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun postComment(recipeId: String) {
        val text = _commentText.value.trim()
        if (text.isEmpty()) return

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                ReviewsRepository.addComment(recipeId, text)
                _commentText.value = "" // clear input field
                loadComments(recipeId) // refresh comments
            } catch (e: Exception) {
                e.printStackTrace()
                _errorMessage.value = "Failed to post comment"
            } finally {
                _isLoading.value = false
            }
        }
    }
}