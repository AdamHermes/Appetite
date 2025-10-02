// network/AuthInterceptor.kt
package com.example.appetite.network

import com.example.appetite.auth.TokenProvider
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val req = chain.request()

        // add token if available
        val token = runBlocking { TokenProvider.getIdToken(false) }
        val authed = if (!token.isNullOrBlank()) {
            req.newBuilder().addHeader("Authorization", "Bearer $token").build()
        } else req

        val resp = chain.proceed(authed)

        // if 401, try once with a refreshed token
        return if (resp.code == 401) {
            resp.close()
            val fresh = runBlocking { TokenProvider.getIdToken(true) }
            val retry = if (!fresh.isNullOrBlank()) {
                authed.newBuilder().removeHeader("Authorization")
                    .addHeader("Authorization", "Bearer $fresh").build()
            } else authed
            chain.proceed(retry)
        } else resp
    }
}