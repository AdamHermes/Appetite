package com.example.appetite.presentation.editProfile

import android.R.attr.name
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.appetite.R
import com.example.appetite.presentation.NavigationRoutes
import com.example.appetite.presentation.userProfile.UserProfileViewModel
import kotlinx.coroutines.delay

@Composable
fun UpdateProfileScreen(
    viewModel: UserProfileViewModel,
    onSuccessNavigate: () -> Unit
) {
    val profile by viewModel.profile.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    var name by remember { mutableStateOf(profile.name) }
    var bio by remember { mutableStateOf(profile.bio) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var showSuccess by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
    }

    if (showSuccess) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("Success", color = Color.White, fontSize = 24.sp) },
            text = { Text("Profile updated successfully!", color = Color.White, fontSize = 18.sp) },
            confirmButton = {
                TextButton(onClick = {
                    showSuccess = false
                    onSuccessNavigate()
                }) {}
            },
            containerColor = Color(0xFF129575),
            textContentColor = Color.White
        )
        LaunchedEffect(Unit) {
            delay(2000)
            showSuccess = false
            viewModel.loadUserProfile()
            onSuccessNavigate()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFFE8F2EE), Color.White),
                    startY = 0f,
                    endY = 1000f
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            // Title
            Text(
                "Edit Profile",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF129575),
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                "Update your profile image, name, and bio",
                fontSize = 16.sp,
                color = Color(0xFF4F4F4F),
                modifier = Modifier.padding(bottom = 24.dp)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Profile Image (clickable)
                    Box(contentAlignment = Alignment.BottomEnd) {
                        AsyncImage(
                            model = imageUri ?: profile.profileUrl?.ifEmpty { R.drawable.splash_logo },
                            contentDescription = "Profile Image",
                            modifier = Modifier
                                .size(120.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFE8F2EE))
                                .border(
                                    BorderStroke(2.dp, Color(0xFF129575)),
                                    CircleShape
                                )
                                .clickable { imagePickerLauncher.launch("image/*") },
                            contentScale = ContentScale.Crop
                        )
                        IconButton(
                            onClick = { imagePickerLauncher.launch("image/*") },
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color(0xFF129575), CircleShape)
                                .align(Alignment.BottomEnd)
                                .shadow(4.dp, CircleShape)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Change", tint = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Name field
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Name") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = TextFieldDefaults.colors(
                            focusedIndicatorColor = Color(0xFF129575),
                            unfocusedIndicatorColor = Color(0xFFE8F2EE),
                            cursorColor = Color(0xFF129575),
                            focusedLabelColor = Color(0xFF129575),
                            unfocusedLabelColor = Color.Gray,
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.DarkGray,
                            focusedContainerColor = Color(0xFFF7FCFA),
                            unfocusedContainerColor = Color(0xFFF7FCFA)
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Bio field
                    OutlinedTextField(
                        value = bio,
                        onValueChange = { bio = it },
                        label = { Text("Bio") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 100.dp),
                        shape = RoundedCornerShape(12.dp),
                        maxLines = 4,
                        colors = TextFieldDefaults.colors(
                            focusedIndicatorColor = Color(0xFF129575),
                            unfocusedIndicatorColor = Color(0xFFE8F2EE),
                            cursorColor = Color(0xFF129575),
                            focusedLabelColor = Color(0xFF129575),
                            unfocusedLabelColor = Color.Gray,
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.DarkGray,
                            focusedContainerColor = Color(0xFFF7FCFA),
                            unfocusedContainerColor = Color(0xFFF7FCFA)
                        )
                    )

                    if (error != null) {
                        Text(
                            text = error.toString(),
                            color = Color.Red,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Save Button
                    Button(
                        onClick = {
                            if (imageUri != null) {
                                viewModel.uploadProfilePhoto(context, imageUri!!)
                            }
                            viewModel.updateProfile(name.trim(), bio.trim(), imageUri?.toString() ?: profile.profileUrl)
                            showSuccess = true
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF129575)),
                        shape = RoundedCornerShape(12.dp),
                        enabled = name.isNotBlank() && !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                "Save",
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
