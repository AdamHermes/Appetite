package com.example.appetite.presentation.sharedComponents

import android.R.color.white
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.font.FontWeight
import com.google.common.math.LinearTransformation.horizontal
import androidx.compose.material3.Surface
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.ui.unit.sp
import com.example.appetite.models.PopupMenuItem
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp

@Composable
fun CustomTopBar(
    onSignOutClick: () -> Unit,
    onEditProfileClick: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.statusBars) // pushes below status bar
            .background(Color.White)
            .padding(horizontal = 30.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Profile",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            )

            // Use PopupMenu instead of DropdownMenu
            PopupMenu(
                menuItems = listOf(
                    PopupMenuItem(
                        text = "Edit Profile",
                        icon = androidx.compose.ui.graphics.vector.rememberVectorPainter(Icons.Default.Edit),
                        onClick = onEditProfileClick
                    ),
                    PopupMenuItem(
                        text = "Sign Out",
                        icon = androidx.compose.ui.graphics.vector.rememberVectorPainter(Icons.Default.ExitToApp),
                        onClick = onSignOutClick
                    )
                )
            )
        }
    }
}
