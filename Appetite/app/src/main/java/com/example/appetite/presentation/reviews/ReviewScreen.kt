// ReviewsScreen.kt
package com.example.appetite.presentation.reviews

import android.R
import android.R.attr.maxLines
import android.R.attr.text
import android.R.attr.textStyle
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.appetite.presentation.reviews.ReviewViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ReviewsScreen(
    recipeId: String,
    viewModel: ReviewViewModel = hiltViewModel(),
    navController: NavController,
) {
    val comments by viewModel.comments.collectAsState()
    val commentText by viewModel.commentText.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    // Load comments when screen opens
    LaunchedEffect(recipeId) {
        viewModel.loadComments(recipeId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 4.dp, end = 4.dp, bottom = 12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left icon
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
            }

            // Title in the center
            Text(
                text = "Reviews",
                style = MaterialTheme.typography.headlineSmall.copy( // bigger & bolder
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 40.dp)
                , // take remaining space
                maxLines = 1,
                textAlign = TextAlign.Center
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 8.dp)
                .background(Color(0xFFF8F8F8), RoundedCornerShape(24.dp))
                .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(24.dp))
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                OutlinedTextField(
                    value = commentText,
                    onValueChange = { viewModel.onCommentChanged(it) },
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp),
                    placeholder = { Text("Add a comment...") },
                    maxLines = 4, // expands if user types more
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.DarkGray
                    ),
                    shape = RoundedCornerShape(12.dp),
                    textStyle = MaterialTheme.typography.bodyMedium
                )
                Box(
                    modifier = Modifier
                        .size(28.dp) // Adjust this for your desired circle size
                        .background(
                            if (commentText.isNotBlank()) Color(0xFF129575) else Color.Gray,
                            shape = CircleShape
                        )
                        .clickable(
                            enabled = commentText.isNotBlank(),
                            onClick = { viewModel.postComment(recipeId) }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Send,
                        contentDescription = "Send",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp) // Adjust icon size as needed
                    )
                }
            }
        }

        if (errorMessage != null) {
            Text(
                text = errorMessage ?: "",
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // COMMENTS SECTION BELOW
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            if (comments.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No reviews yet. Be the first to comment!")
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {items(comments) { comment ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp, horizontal = 8.dp)
                    ) {
                        // Profile circle (photo or initials)
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFDBF3EF)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (!comment.profilePhotoUrl.isNullOrBlank()) {
                                Image(
                                    painter = rememberAsyncImagePainter(comment.profilePhotoUrl),
                                    contentDescription = "Profile Photo",
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Text(
                                    text = (comment.userName?.firstOrNull()?.uppercase() ?: "A"),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFF129575)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        // Comment card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(1.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = comment.userName ?: "Anonymous",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = Color(0xFF129575)
                                    )
                                    Text(
                                        text = formatTimestamp(comment.createdAt),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.Gray
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = comment.text,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Color.Black
                                )
                            }
                        }
                    }
                }




                }
            }
        }
    }
}

fun formatTimestamp(timestamp: Double): String {
    val millis = (timestamp * 1000).toLong() // convert seconds → ms
    val date = Date(millis)
    val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
    return sdf.format(date)
}
