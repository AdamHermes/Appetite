package com.example.appetite.models

import androidx.compose.ui.graphics.painter.Painter

data class PopupMenuItem(
    val text: String,
    val icon: Painter,
    val onClick: () -> Unit
)
data class MyRatingResponse(
    val value: Int?
)

data class RateIn(
    val value: Int
)