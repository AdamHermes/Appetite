package com.example.appetite.presentation.sharedComponents

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun IconOption(
    iconRes: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    size: Dp = 32.dp,
    color: Color = Color(0xFF00C896)
) {
    IconButton(onClick = onClick, modifier = Modifier.size(48.dp)) {

        Box(
            modifier = Modifier
                .height(size) // Fixed height container for alignment
                .width(size)
            //.wrapContentSize(Alignment.BottomCenter)
        ) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                modifier = Modifier.size(size),
                colorFilter = ColorFilter.tint(
                    if (isSelected) color else Color(0xFFD8D8D8) // Green when selected
                )
            )
        }
    }
}