package com.example.appetite.presentation.sharedComponents

import android.R.attr.onClick
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.appetite.models.PopupMenuItem
import kotlin.collections.forEach
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.appetite.R
import kotlin.text.compareTo

@Composable
fun PopupMenu(
    menuItems: List<PopupMenuItem>
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(
                painter = painterResource(id = R.drawable.more),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = Color.Unspecified
            )
        }
        if (expanded) {
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .width(160.dp)
                    .background(Color.Transparent),
                shape = RoundedCornerShape(16.dp),
                containerColor = Color.White,
                shadowElevation = 4.dp
            ) {
                // Inside DropdownMenu {
                    Column {
                        menuItems.forEachIndexed { index, item ->
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            painter = item.icon,
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp),
                                            tint = Color.Unspecified
                                        )
                                        Spacer(Modifier.width(12.dp))
                                        Text(item.text)
                                    }
                                },
                                modifier = Modifier
                                    .background(Color.White),
                                onClick = {
                                    expanded = false
                                    item.onClick()
                                }
                            )
                        }
                    }
                }
            }
        }
}