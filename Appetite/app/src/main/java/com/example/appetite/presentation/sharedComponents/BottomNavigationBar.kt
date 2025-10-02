package com.example.appetite.presentation.sharedComponents

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Environment
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.appetite.R
import com.example.appetite.presentation.NavigationRoutes
import com.example.appetite.presentation.searchSimRecipe.SearchSimRecipeViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomNavigationBar(navController: NavController) {
    val currentRoute = navController
        .currentBackStackEntryAsState().value?.destination?.route
    val context = LocalContext.current

    val photoUri = remember {
        val file = File(
            context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
            "captured_image.jpg"
        )
        FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )
    }

    var selectedMode by remember { mutableStateOf<String?>(null) }
    var showSheet by remember { mutableStateOf(false) }

    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            selectedMode?.let { mode ->
                when (mode) {
                    "post" -> navController.navigate(NavigationRoutes.newPostWithUri(Uri.encode(photoUri.toString())))
                    "recipe" -> navController.navigate(NavigationRoutes.similarRecipesWithUri(Uri.encode(photoUri.toString())))
                    "ingredient" -> navController.navigate(NavigationRoutes.searchIngredientsWithUri(Uri.encode(photoUri.toString())))
                }
            }
        }
    }

    // Gallery launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedMode?.let { mode ->
                when (mode) {
                    "post" -> navController.navigate(NavigationRoutes.newPostWithUri(Uri.encode(it.toString())))
                    "recipe" -> navController.navigate(NavigationRoutes.similarRecipesWithUri(Uri.encode(it.toString())))
                    "ingredient" -> navController.navigate(NavigationRoutes.searchIngredientsWithUri(Uri.encode(it.toString())))
                }
            }
        }
    }

    // Permission for Camera
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            cameraLauncher.launch(photoUri)
        }
    }

    // --- BottomSheet UI ---
    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Choose Action", style = MaterialTheme.typography.titleMedium)

                listOf(
                    "post" to "New Post",
                    "recipe" to "Search by Recipe",
                    "ingredient" to "Search by Ingredients"
                ).forEach { (mode, label) ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedMode = mode
                            },
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(
                            Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(label, style = MaterialTheme.typography.bodyLarge)
                            if (selectedMode == mode) {
                                Spacer(Modifier.height(8.dp))
                                Row(
                                    Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    Button(onClick = {
                                        if (ContextCompat.checkSelfPermission(
                                                context,
                                                Manifest.permission.CAMERA
                                            ) == PackageManager.PERMISSION_GRANTED
                                        ) {
                                            cameraLauncher.launch(photoUri)
                                        } else {
                                            permissionLauncher.launch(Manifest.permission.CAMERA)
                                        }
                                        showSheet = false
                                    },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFF129575),   // background
                                            contentColor = Color.White           // text/icon color
                                        ),
                                        modifier = Modifier.weight(1f)
                                    ) { Text("Camera") }
                                    Spacer(Modifier.width(8.dp))

                                    Button(onClick = {
                                        galleryLauncher.launch("image/*")
                                        showSheet = false
                                    },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFF129575),   // background
                                            contentColor = Color.White           // text/icon color
                                        ),
                                        modifier = Modifier.weight(1f)
                                    ) { Text("Gallery") }
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(20.dp))
            }
        }
    }

    // --- Bottom Nav (unchanged) ---
    val items = listOf(
        NavigationRoutes.Home,
        NavigationRoutes.Saved,
        NavigationRoutes.LeaderBoard,
        NavigationRoutes.Profile
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.navbar),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .align(Alignment.BottomCenter),
            contentScale = ContentScale.FillBounds
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 32.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEachIndexed { index, route ->
                Box(
                    modifier = Modifier.weight(0.8f),
                    contentAlignment = Alignment.Center
                ) {
                    IconOption(
                        iconRes = when (index) {
                            0 -> R.drawable.home
                            1 -> R.drawable.ic_unsaved
                            2 -> R.drawable.ic_leaderboard
                            3 -> R.drawable.ic_profile
                            else -> R.drawable.home
                        },
                        isSelected = currentRoute == route,
                        onClick = { if (currentRoute != route) navController.navigate(route) },
                        size = 30.dp
                    )
                }
            }
        }

        FloatingActionButton(
            onClick = { showSheet = true },
            containerColor = Color(0xFF00A86B),
            contentColor = Color(0xFFF8F9FA),
            shape = CircleShape,
            modifier = Modifier
                .size(64.dp)
                .align(Alignment.TopCenter)
                .offset(y = (-18).dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_camera),
                contentDescription = "Add Recipe",
                modifier = Modifier.size(30.dp)
            )
        }
    }
}


