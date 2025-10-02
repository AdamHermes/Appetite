package com.example.appetite.presentation.editRecipe

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun EditRecipeScreen(viewModel: EditRecipeViewModel = hiltViewModel()) {
    Text(
        text = "MyRecipeScreen",
        style = MaterialTheme.typography.headlineLarge
    )
}