package com.example.appetite.presentation.signUp

import android.R.attr.checked
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import com.example.appetite.R

import androidx.hilt.navigation.compose.hiltViewModel
import com.example.appetite.auth.TokenProvider.signOut
import com.example.appetite.presentation.sharedComponents.SocialButton


@Composable
fun SignUpScreen(
    viewModel: SignUpViewModel = hiltViewModel(),
    onNavigateToLogin: () -> Unit = {},
) {
    val name = viewModel.name
    val email = viewModel.email
    val password = viewModel.password
    val confirmPassword = viewModel.confirmPassword
    val acceptedTerms = viewModel.acceptedTerms
    val state by viewModel.signupState.collectAsState()
    var error by remember { mutableStateOf<String?>(null) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFEFFFEB)) // light gray background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Appetite",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF129575),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text("Create an account", fontSize = 36.sp, fontWeight = FontWeight.Bold)
                Text(
                    "Let's help you set up your account, it won’t take long.",
                    fontSize = 16.sp,
                    color = Color.Gray
                )
            }


            Spacer(modifier = Modifier.height(24.dp))

            // Name
            OutlinedTextField(
                value = name,
                onValueChange = { viewModel.onNameChange(it) },
                label = { Text("Name") },
                placeholder = { Text("Enter Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
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

            Spacer(modifier = Modifier.height(16.dp))

            // Email
            OutlinedTextField(
                value = email,
                onValueChange = { viewModel.onEmailChange(it) },
                label = { Text("Email") },
                placeholder = { Text("Enter Email") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
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

            Spacer(modifier = Modifier.height(16.dp))

            // Password
            OutlinedTextField(
                value = password,
                onValueChange = { viewModel.onPasswordChange(it) },
                label = { Text("Password") },
                placeholder = { Text("Enter Password") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
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

            Spacer(modifier = Modifier.height(16.dp))

            // Confirm Password
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { viewModel.onConfirmPasswordChange(it) },
                label = { Text("Confirm Password") },
                placeholder = { Text("Retype Password") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
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

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start, // Align left
                modifier = Modifier
                    .fillMaxWidth() // Take full width, content stays at left
                    .padding(8.dp)
            ) {
                Checkbox(
                    checked = acceptedTerms,
                    onCheckedChange = { viewModel.onTermsChecked(it) },
                    colors = CheckboxDefaults.colors(
                        checkedColor = Color(0xFFFF9800), // Orange when checked
                        uncheckedColor = Color(0xFFFF9800), // Orange border when unchecked
                        checkmarkColor = Color.White
                    ),
                    modifier = Modifier
                        .size(20.dp) // smaller size like your design
                        .clip(RoundedCornerShape(50.dp)) // Rounded edges
                )

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = "Accept terms & Condition",
                    color = Color(0xFFFF9800), // Orange text
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
            // error?.let {
            //     val friendlyMessage = getFriendlySignupError(it)
            //     Text(text = friendlyMessage, color = Color.Black, modifier = Modifier.padding(top = 8.dp))
            // }

            // Remove inline error text
            // error?.let { ... }

            // Show error dialog for local error
            if (error != null) {
                AlertDialog(
                    onDismissRequest = { error = null },
                    title = { Text("Sign Up Failed", fontWeight = FontWeight.Bold, color = Color(0xFFD32F2F)) },
                    text = { Text(getFriendlySignupError(error ?: ""), color = Color.Black) },
                    confirmButton = {
                        Button(
                            onClick = { error = null },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                        ) {
                            Text("OK", color = Color.White)
                        }
                    },
                    containerColor = Color(0xFFFFEBEE)
                )
            }

            // Sign Up Button

            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = { viewModel.onSignUpClick(name, email, password, confirmPassword, onError = { error = it } ) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF129575)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Sign Up", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = Color.White
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(
                    modifier = Modifier.weight(2f),
                )
                Text("  Or Sign Up With  ", color = Color.Gray, fontSize = 16.sp)
                HorizontalDivider(modifier = Modifier.weight(2f))
            }
            Spacer(modifier = Modifier.height(20.dp))
            // Google & Facebook buttons
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

            Spacer(modifier = Modifier.height(20.dp))

            // Bottom text
            Row {
                Text("Already a member? ", color = Color.Black,  fontSize = 16.sp)
                Text(
                    "Sign In",
                    color = Color(0xFFFF9800),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.clickable { onNavigateToLogin() }
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
            when (state) {
                is SignupState.Idle -> {}
                is SignupState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFF129575))
                    }
                }
                is SignupState.Success -> {
                    LaunchedEffect(state) {
                        showSuccessDialog = true
                    }
                }
                is SignupState.Error -> {
                    LaunchedEffect(state) {
                        showErrorDialog = getFriendlySignupError((state as SignupState.Error).error)
                    }
                }
            }

            if (showSuccessDialog) {
                AlertDialog(
                    onDismissRequest = {
                        showSuccessDialog = false
                        signOut()
                        onNavigateToLogin()
                    },
                    title = { Text("Account Created", fontWeight = FontWeight.Bold, color = Color(0xFF129575)) },
                    text = { Text("Your account has been created successfully. Please sign in.", color = Color.Black) },
                    confirmButton = {
                        Button(
                            onClick = {
                                showSuccessDialog = false
                                signOut()
                                onNavigateToLogin()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF129575))
                        ) {
                            Text("Sign In", color = Color.White)
                        }
                    },
                    containerColor = Color(0xFFE8F2EE)
                )
            }

            // Show error dialog for SignupState.Error
            showErrorDialog?.let { errorMsg ->
                AlertDialog(
                    onDismissRequest = { showErrorDialog = null },
                    title = { Text("Sign Up Failed", fontWeight = FontWeight.Bold, color = Color(0xFFD32F2F)) },
                    text = { Text(errorMsg, color = Color.Black) },
                    confirmButton = {
                        Button(
                            onClick = { showErrorDialog = null },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                        ) {
                            Text("OK", color = Color.White)
                        }
                    },
                    containerColor = Color(0xFFFFEBEE)
                )
            }
        }
    }
}

// Add this helper function at the bottom of the file
fun getFriendlySignupError(error: String): String {
    return when {
        error.contains("password", ignoreCase = true) && error.contains(
            "match",
            ignoreCase = true
        ) -> "Passwords do not match."

        error.contains("password", ignoreCase = true) -> "Password is too weak or invalid."
        error.contains("email", ignoreCase = true) -> "Please enter a valid email address."
        error.contains("already", ignoreCase = true) -> "An account with this email already exists."
        error.contains(
            "network",
            ignoreCase = true
        ) -> "Network error. Please check your connection."

        error.contains("empty", ignoreCase = true) -> "Please fill in all fields."
        else -> "Sign up failed. Please try again."
    }
}
