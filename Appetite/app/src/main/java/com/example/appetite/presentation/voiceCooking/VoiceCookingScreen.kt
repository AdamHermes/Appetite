package com.example.appetite.presentation.voiceCooking

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.appetite.R

@Composable
fun VoiceCookingScreen(
    navController: NavController,
    viewModel: VoiceCookingViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            viewModel.startListening(context)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.initTTS(context) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.RECORD_AUDIO
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                viewModel.startListening(context)
            } else {
                micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }
    }


    val steps = uiState.recipeSteps
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { steps.size })

    // --- Server → Pager sync
    LaunchedEffect(uiState.currentStepIndex, steps.size) {
        val targetPage = uiState.currentStepIndex
        if (steps.isNotEmpty() && targetPage in 0 until steps.size) {
            pagerState.animateScrollToPage(targetPage)
        }
    }

    // --- Pager swipe → ViewModel sync
    LaunchedEffect(pagerState.currentPage) {
        if (pagerState.currentPage != uiState.currentStepIndex) {
            viewModel.updateCurrentStep(pagerState.currentPage)
            android.util.Log.d("VoiceCookingScreen", "Pager swiped to page = ${pagerState.currentPage}")
        }
    }


    // Animation for mic pulsing
    val infiniteTransition = rememberInfiniteTransition(label = "")
    val circleScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = ""
    )

    Surface(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize()) {

            // --- Top Right (mute button) ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { viewModel.toggleMute() },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        painter = painterResource(
                            if (uiState.isMuted) R.drawable.ic_speaker_cross else R.drawable.ic_speaker_open
                        ),
                        contentDescription = "Mute",
                        tint = Color.Black,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // --- Step pager (swipe left/right) ---
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .padding(top = 80.dp, bottom = 160.dp) // leave room for mic + buttons
            ) { page ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    shape = MaterialTheme.shapes.medium,
                    tonalElevation = 2.dp,
                    color = Color(0xFFF3F5F7)
                ) {
                    Text(
                        text = steps[page],
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            // --- Mic animation (center) ---
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .align(Alignment.Center)
                    .graphicsLayer {
                        scaleX = if (uiState.isListening || uiState.isSpeaking) circleScale else 1f
                        scaleY = if (uiState.isListening || uiState.isSpeaking) circleScale else 1f
                    }
                    .clip(CircleShape)
                    .background(Color(0xFF129575).copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_mic_ai),
                    contentDescription = "Mic Animation",
                    tint = Color.White,
                    modifier = Modifier.size(80.dp)
                )
            }

            // --- Bottom controls (mic + exit) ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 40.dp)
                    .align(Alignment.BottomCenter),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FloatingActionButton(
                    onClick = {
                        if (ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.RECORD_AUDIO
                        ) == PackageManager.PERMISSION_GRANTED
                        ) {
                            viewModel.toggleListening(context)
                        } else {
                            micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    },
                    containerColor = if (uiState.isListening) Color.Red else Color(0xFF129575),
                    shape = CircleShape
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_mic_ai),
                        contentDescription = "Mic",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }

                FloatingActionButton(
                    onClick = { navController.popBackStack() },
                    containerColor = Color.DarkGray,
                    shape = CircleShape
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_close_x),
                        contentDescription = "Exit",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}