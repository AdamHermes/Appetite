package com.example.appetite.presentation.splash

import android.R.attr.contentDescription
import android.R.attr.fontWeight
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.runtime.*
import androidx.compose.material3.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import com.example.appetite.presentation.userProfile.UserProfileViewModel
import com.example.appetite.presentation.savedRecipe.SavedRecipeViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.appetite.R

@Composable
fun SplashScreen(
    navController: NavController,
    userProfileViewModel: UserProfileViewModel = hiltViewModel(),
    savedRecipeViewModel: SavedRecipeViewModel = hiltViewModel(),
    splashViewModel: SplashViewModel = hiltViewModel()
) {
    val user by splashViewModel.currentUser.collectAsState()

    LaunchedEffect(user) {
        if (user != null) {
            userProfileViewModel.loadUserProfile()
            savedRecipeViewModel.refreshSavedRecipes()
            navController.navigate("home") {
                popUpTo("splash") { inclusive = true }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Background image
        Image(
            painter = painterResource(id = R.drawable.splash_bg),
            contentDescription = "Background",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- Top Half ---
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.splash_logo),
                    contentDescription = "Logo",
                    tint = Color.White,
                    modifier = Modifier.size(80.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .background(Color.White, RoundedCornerShape(16.dp))
                        .padding(horizontal = 24.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Appetite",
                        fontSize = 36.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF129575),
                        letterSpacing = 2.sp
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "100K+ Premium Recipe",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
            }

            // --- Bottom Half ---
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Get",
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Cooking",
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(64.dp))
                Text(
                    text = "Simple way to find Tasty Recipe",
                    fontSize = 18.sp,        // slightly bigger than default
                    fontWeight = FontWeight.Medium,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(64.dp))
                if (user == null) {
                    // Show CTA button only for logged-out users
                    Button(
                        onClick = {
                            navController.navigate("login") {
                                popUpTo("splash") { inclusive = true }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF129575)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .width(280.dp)   // narrower width
                            .height(56.dp)   // taller button
                    ) {
                        Text(
                            text = "Start Cooking",
                            fontSize = 18.sp,
                            color = Color.White,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = "Arrow",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}
