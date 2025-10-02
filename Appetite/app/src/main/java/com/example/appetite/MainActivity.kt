package com.example.appetite

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.appetite.network.ApiClient
import com.example.appetite.presentation.navigation.AppNavigation
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ApiClient.init(baseUrl = "https://a863e5b0ad28.ngrok-free.app")
        setContent {
            AppNavigation()
        }
    }
}
