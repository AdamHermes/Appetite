package com.example.appetite.presentation.signUp

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appetite.repository.AuthRepository
import com.example.appetite.repository.UserInfoRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.userProfileChangeRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor() : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    var name by mutableStateOf("")
        private set

    var email by mutableStateOf("")
        private set

    var password by mutableStateOf("")
        private set

    var confirmPassword by mutableStateOf("")
        private set

    var acceptedTerms by mutableStateOf(false)
        private set
    private val _signupState = MutableStateFlow<SignupState>(SignupState.Idle)
    val signupState: StateFlow<SignupState> = _signupState

    fun onNameChange(value: String) {
        name = value
    }
    fun onEmailChange(value: String) {
        email = value
    }
    fun onPasswordChange(value: String) {
        password = value
    }
    fun onConfirmPasswordChange(value: String) {
        confirmPassword = value
    }
    fun onTermsChecked(value: Boolean) {
        acceptedTerms = value
    }

    fun onSignUpClick(
        name: String,
        email: String,
        password: String,
        confirmPassword: String,
//        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        if (password != confirmPassword) {
            onError("Passwords do not match.")
            return
        }

        viewModelScope.launch {
            try {
                // Create account in Firebase
                auth.createUserWithEmailAndPassword(email, password).await()

                // Update display name
                val profileUpdates = userProfileChangeRequest {
                    displayName = name
                }
                auth.currentUser?.updateProfile(profileUpdates)?.await()

                val tokenResult = auth.currentUser?.getIdToken(true)?.await()
                val idToken = tokenResult?.token
                android.util.Log.d("SignupViewModel", "Firebase ID Token = $idToken")

                // Call backend repository if needed
                UserInfoRepository.createMyProfile(name, email)
                _signupState.value = SignupState.Success("Sign up successful!")

            } catch (e: Exception) {
                onError(e.message ?: "Sign-up failed.")
            }
        }
    }
}

//Temporary sealed class to represent the state of the sign-up process
sealed class SignupState {
    object Idle : SignupState()
    object Loading : SignupState()
    data class Success(val message: String) : SignupState()
    data class Error(val error: String) : SignupState()
}