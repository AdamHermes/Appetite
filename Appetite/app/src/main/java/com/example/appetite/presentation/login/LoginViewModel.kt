package com.example.appetite.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appetite.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor() : ViewModel() {
    private val auth = FirebaseAuth.getInstance()

    fun signIn(
        email: String,
        password: String,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                signOut() // Just to make sure no bug of sign in when There is no sign out button yet
                auth.signInWithEmailAndPassword(email, password).await()
                // Token is attached automatically by AuthInterceptor

                val me = AuthRepository.fetchMe()
                onSuccess(me.uid)
            } catch (e: Exception) {
                onError(e.message ?: "Sign-in or backend call failed.")
            }
        }
    }

    fun signOut() = auth.signOut()
}