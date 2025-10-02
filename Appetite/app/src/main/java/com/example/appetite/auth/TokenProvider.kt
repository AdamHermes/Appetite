// auth/TokenProvider.kt
package com.example.appetite.auth

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await

object TokenProvider {
    private val auth by lazy { FirebaseAuth.getInstance() }

    suspend fun getIdToken(forceRefresh: Boolean = false): String? {
        val user = auth.currentUser ?: return null
        val token = user.getIdToken(forceRefresh).await().token

        // Debug print
        Log.d("JWT", "Firebase ID Token: $token")

        return token
    }

    fun signOut() = auth.signOut()
}
