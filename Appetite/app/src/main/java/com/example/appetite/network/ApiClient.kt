// network/ApiClient.kt
package com.example.appetite.network

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import com.squareup.moshi.adapters.Rfc3339DateJsonAdapter
import java.util.Date
import java.util.concurrent.TimeUnit

object ApiClient {
    lateinit var api: ApiService
        private set

    private lateinit var retrofit: Retrofit
    private var baseUrl: String = ""

    fun init(baseUrl: String) {
        println("ApiClient base URL init: $baseUrl")
        this.baseUrl = ensureTrailingSlash(baseUrl)

        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        val client = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .addInterceptor(AuthInterceptor())
            .addInterceptor(logging)
            .build()

        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .add(Date::class.java, Rfc3339DateJsonAdapter())
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(ensureTrailingSlash(baseUrl)) // must end with '/'
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

        api = retrofit.create(ApiService::class.java)
    }

    fun getVoiceAgentWsUrl(recipeId: String): String {
        // Convert http:// → ws://   and https:// → wss://
        val wsBase = if (baseUrl.startsWith("https")) {
            baseUrl.replaceFirst("https", "wss")
        } else {
            baseUrl.replaceFirst("http", "ws")
        }
        return "$wsBase${ApiService.VOICE_AGENT_WS}$recipeId"
    }

    private fun ensureTrailingSlash(url: String) =
        if (url.endsWith("/")) url else "$url/"


}