package com.example.appetite.presentation.login

import android.R.attr.onClick
import android.R.color.white
import android.R.id.message
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImagePainter.State.Empty.painter
import com.example.appetite.R
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.ui.text.font.FontWeight
import com.example.appetite.presentation.sharedComponents.SocialButton


@Composable
fun SignInScreen(
    onSignInSuccess: () -> Unit,
    onNavigateToSignUp: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxSize()
         .background(Color(0xFFEFFFEB))
    ) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp) ,// light gray background
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Appetite",
            fontSize = 32.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF129575),
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(350.dp) // adjust size
                .clip(CircleShape)
                .background(Color.White) // white circle background
                .border(1.dp, Color.Gray, CircleShape), // border thickness, color, shape
        ) {
            Image(
                painter = painterResource(id = R.drawable.login_image), // your image
                contentDescription = "Chef",
                modifier = Modifier
                    .size(300.dp) // size of the image inside
                    .clip(CircleShape), // make the image also circular
                contentScale  = ContentScale.Fit
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Welcome Back", style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Color(0xFF129575),      // border when focused
                unfocusedIndicatorColor = Color.Gray,   // border when not focused
                cursorColor = Color.Blue,               // cursor color
                focusedLabelColor = Color.Gray,          // label when focused
                unfocusedLabelColor = Color.Gray,       // label when not focused
                focusedTextColor = Color.Black,         // input text
                unfocusedTextColor = Color.DarkGray,
                focusedContainerColor = Color.White,    // background when focused
                unfocusedContainerColor = Color.White   // background when unfocused
            )

        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Color(0xFF129575),      // border when focused
                unfocusedIndicatorColor = Color.Gray,   // border when not focused
                cursorColor = Color.Blue,               // cursor color
                focusedLabelColor = Color.Gray,          // label when focused
                unfocusedLabelColor = Color.Gray,       // label when not focused
                focusedTextColor = Color.Black,         // input text
                unfocusedTextColor = Color.DarkGray,
                focusedContainerColor = Color.White,    // background when focused
                unfocusedContainerColor = Color.White   // background when unfocused
            )
        )

         error?.let {
             // Remove old inline error text
             // Text(text = it, color = Color.Black, modifier = Modifier.padding(top = 8.dp))
             LaunchedEffect(error) {
                 if (error != null) showErrorDialog = true
             }
         }

        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = {
                viewModel.signIn(email, password,
                    onSuccess = { showSuccessDialog = true },
                    onError = {
                        error = getFriendlyErrorMessage(it)
                    }
                )
            },
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
                .height(64.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF129575))
        ) {
            Text("Sign In", color = Color.White, fontSize = 24.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = Color.White
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
// Divider
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HorizontalDivider(
                modifier = Modifier.weight(2f),
            )
            Text("  Or Sign In With  ", color = Color.Gray, fontSize = 16.sp)
            HorizontalDivider(modifier = Modifier.weight(2f))
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SocialButton(
                painter = painterResource(id = R.drawable.google_icon),
                contentDescription = "Google"
            )
            SocialButton(
                painter = painterResource(id = R.drawable.facebook_icon),
                contentDescription = "Facebook"
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Don’t have an account? ",
                color = Color.Black,
                fontSize = 16.sp
            )
            Text(
                text = "Sign up",
                color = Color(0xFFFF9800),
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.clickable {
                    // Handle sign up click here
                    onNavigateToSignUp()
                }
            )
        }
    }

    if (showErrorDialog && error != null) {
        AlertDialog(
            onDismissRequest = {
                showErrorDialog = false
                error = null
            },
            title = { Text("Sign In Failed", fontWeight = FontWeight.Bold, color = Color(0xFFD32F2F)) },
            text = { Text(error ?: "", color = Color.Black) },
            confirmButton = {
                Button(
                    onClick = {
                        showErrorDialog = false
                        error = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                ) {
                    Text("OK", color = Color.White)
                }
            },
            containerColor = Color(0xFFFFEBEE)
        )
    }
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = {
                showSuccessDialog = false
                onSignInSuccess()
            },
            title = { Text("Login Successful", fontWeight = FontWeight.Bold, color = Color(0xFF129575)) },
            text = { Text("You have logged in successfully.", color = Color.Black) },
            confirmButton = {
                Button(
                    onClick = {
                        showSuccessDialog = false
                        onSignInSuccess()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF129575))
                ) {
                    Text("Continue", color = Color.White)
                }
            },
            containerColor = Color(0xFFE8F2EE)
        )
    }
}

}

// Add this helper function at the bottom of the file
fun getFriendlyErrorMessage(error: String): String {
    return when {
        error.contains("password", ignoreCase = true) -> "Incorrect password. Please try again."
        error.contains("no user", ignoreCase = true) || error.contains("not found", ignoreCase = true) -> "No account found with this email."
        error.contains("network", ignoreCase = true) -> "Network error. Please check your connection."
        error.contains("email", ignoreCase = true) -> "Please enter a valid email address."
        error.contains("empty", ignoreCase = true) -> "Please fill in all fields."
        else -> "Sign in failed. Please try again."
    }
}
