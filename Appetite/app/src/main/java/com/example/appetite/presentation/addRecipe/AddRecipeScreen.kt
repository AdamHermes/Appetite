package com.example.appetite.presentation.addRecipe

import android.R.attr.label
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import org.json.JSONObject
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.Alignment
import androidx.compose.material3.Icon
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.TextStyle
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.window.Dialog
import com.example.appetite.presentation.userProfile.UserProfileViewModel

@Composable
fun AddRecipeScreen(
    viewModel: AddRecipeViewModel = hiltViewModel(),
    userViewModel: UserProfileViewModel,
    initialImageUri: Uri? = null,
    navController: NavController
) {
    val context = LocalContext.current

    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var area by remember { mutableStateOf("") }
    var minutes by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf("") }

    // dynamic lists
    val ingredients = remember { mutableStateListOf<Pair<String, String>>() }
    val steps = remember { mutableStateListOf<String>() }

    var imageUri by remember { mutableStateOf<Uri?>(initialImageUri) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var showSuccess by remember { mutableStateOf(false) }
    var shouldSubmit by remember { mutableStateOf(false) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> imageUri = uri }

    // ---------- Submit logic ----------
    LaunchedEffect(shouldSubmit) {
        if (shouldSubmit) {
            loading = true
            error = null
            val body = JSONObject().apply {
                put("name", name)
                put("category", category)
                put("area", area)
                put("minutes", minutes.toIntOrNull() ?: 0)
                put("tags", org.json.JSONArray(tags.split(",").map { it.trim() }))
                put("ingredients", org.json.JSONArray(
                    ingredients.map { (ing, meas) ->
                        JSONObject().apply {
                            put("ingredient", ing.trim())
                            put("measure", meas.trim())
                        }
                    }
                ))
                put("steps", org.json.JSONArray(steps.map { it.trim() }))
                put("ratingAvg", 0.0)       // default
                put("youtubeUrl", JSONObject.NULL)
                put("source", JSONObject.NULL)
            }.toString()

            val result = viewModel.createRecipe(context.contentResolver, imageUri!!, body)
            userViewModel.loadUserProfile()
            loading = false
            if (result != null) {
                showSuccess = true
            } else {
                error = "Failed to add recipe"
            }
            shouldSubmit = false
        }
    }
    if (loading) {
        Dialog(onDismissRequest = { /* block dismiss while loading */ }) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF129575))
            }
        }
    }
    if (showSuccess) {
        Dialog(onDismissRequest = { showSuccess = false }) {    
            Box(
                modifier = Modifier
                    .width(220.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = "Success",
                        tint = Color(0xFF129575),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(Modifier.height(12.dp))
                    Text("Recipe Posted!", fontSize = 18.sp, color = Color.Black)
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = {
                            showSuccess = false
                            navController.popBackStack() // go back after confirm
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF129575))
                    ) {
                        Text("OK", color = Color.White)
                    }
                }
            }
        }
    }

    // ---------- UI ----------
    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF129575))
                    .padding(top = 28.dp, bottom = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Post Your Recipe", color = Color.White, fontSize = 20.sp)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(12.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // ---------- Image ----------
            Card(
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(12.dp)
                ) {
                    imageUri?.let {
                        Image(
                            painter = rememberAsyncImagePainter(it),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth() // take full width
                                .height(200.dp) // fixed height for consistent look
                                .clip(RoundedCornerShape(16.dp)),
                            contentScale = ContentScale.Crop // crop to fill
                        )
                    } ?: Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.LightGray.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.AddCircle,
                            contentDescription = "Add Image",
                            tint = Color.Gray,
                            modifier = Modifier.size(48.dp)
                        )
                    }

                }
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Button(
                    onClick = { galleryLauncher.launch("image/*") },
                    modifier = Modifier.clip(RoundedCornerShape(12.dp)),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF129575))
                ) {
                    Text("Select Image", color = Color.White)
                }
            }


            // ---------- Recipe Info ----------
            SectionTitle("Detailed Info")
            CustomTextField(value = name, onValueChange = { name = it }, label = "Recipe Name")
            DishTypeSelector(
                selectedType = category,
                onTypeSelected = { category = it }
            )
            CustomTextField(value = area, onValueChange = { area = it }, label = "Origin (Area)", placeholder = "e.g. American, Italian")
            CustomTextField(value = minutes, onValueChange = { minutes = it }, label = "Time to cook (in minutes)")

            Spacer(Modifier.height(20.dp))

            // ---------- Ingredients ----------
            SectionTitle("Ingredients")
            IngredientList(ingredients = ingredients) { /* triggers recomposition */ }

            Spacer(Modifier.height(20.dp))

            // ---------- Steps ----------
            SectionTitle("Steps")
            StepsList(steps = steps) { /* triggers recomposition */ }

            Spacer(Modifier.height(20.dp))

            // ---------- Tags ----------
            SectionTitle("Additional Info")
            CustomTextField(value = tags, onValueChange = { tags = it }, label = "Tags (comma separated)", placeholder = "spicy, vegan, trendy 2024,... ")

            Spacer(Modifier.height(28.dp))

            // ---------- Submit ----------
            Button(
                onClick = {
                    if (name.isBlank() || category.isBlank() || area.isBlank() || minutes.isBlank() ||
                        ingredients.isEmpty() || steps.isEmpty() || imageUri == null
                    ) {
                        error = "Fill all fields and select an image"
                        return@Button
                    }
                    shouldSubmit = true
                },
                enabled = !loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(14.dp)),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF129575))
            ) {
                Text(if (loading) "Uploading..." else "Post", fontSize = 16.sp, color = Color.White)
            }

            Spacer(Modifier.height(8.dp))

            error?.let { Text(it, color = Color.Red, fontSize = 14.sp) }
        }
    }
}

@Composable
fun SectionTitle(text: String) {
    Text(text, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2C3E50), modifier = Modifier.padding(bottom = 8.dp))
}

// ---------- Custom Text Field ----------
@Composable
fun CustomTextField(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String = "",
) {
    Column(modifier = modifier.padding(bottom = 12.dp)) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = Color.DarkGray,
            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFF7F7F7))
                .border(BorderStroke(1.dp, Color(0xFFE0E0E0)), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.CenterStart
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = TextStyle(fontSize = 16.sp, color = Color.Black),
                modifier = Modifier.fillMaxWidth(),
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp), // add padding
                        contentAlignment = Alignment.CenterStart
                    ){
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            fontSize = 16.sp,
                            color = Color.Gray.copy(alpha = 0.5f)
                        )
                    }
                    innerTextField()
                    }
                }
            )
        }
    }
}

// ---------- Ingredient List ----------
@Composable
fun IngredientList(
    ingredients: MutableList<Pair<String, String>>,
    onUpdate: () -> Unit
) {
    Column {
        ingredients.forEachIndexed { index, (ing, meas) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Ingredient takes 50% width
                CustomTextField(
                    value = ing,
                    onValueChange = { ingredients[index] = it to meas; onUpdate() },
                    label = "Ingredient",
                    modifier = Modifier.weight(1f),
                    placeholder = "e.g. Sugar"
                )
                // Measure takes 50% width
                CustomTextField(
                    value = meas,
                    onValueChange = { ingredients[index] = ing to it; onUpdate() },
                    label = "Measure",
                    modifier = Modifier.weight(1f),
                    placeholder = "e.g. 1 cup"
                )
            }
        }
        Button(
            onClick = { ingredients.add("" to ""); onUpdate() },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(12.dp)),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF129575))
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text("Ingredient", color = Color.White, fontSize = 18.sp)
        }
    }
}


// ---------- Steps List ----------
@Composable
fun StepsList(
    steps: MutableList<String>,
    onUpdate: () -> Unit
) {
    Column {
        steps.forEachIndexed { index, step ->
            CustomTextField(
                value = step,
                onValueChange = { steps[index] = it; onUpdate() },
                label = "Step ${index + 1}",
                placeholder = "Describe step ${index + 1}",
            )
        }
        Button(
            onClick = { steps.add(""); onUpdate() },
            modifier = Modifier.fillMaxWidth().height(48.dp).clip(RoundedCornerShape(12.dp)),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF129575))
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text("Step", color = Color.White, fontSize = 18.sp)
        }
    }
}

@Composable
fun DishTypeSelector(
    selectedType: String,
    onTypeSelected: (String) -> Unit
) {
    val dishTypes = listOf(
        "Starter/Appetizer",
        "Side Dish",
        "Main Course",
        "Dessert",
        "Snack",
        "Drink",
        "Miscellaneous"
    )

    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
        Text(
            text = "Dish Type",
            fontSize = 14.sp,
            color = Color.DarkGray,
            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            dishTypes.forEach { type ->
                val isSelected = type == selectedType
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            if (isSelected) Color(0xFF129575) else Color.LightGray.copy(alpha = 0.3f)
                        )
                        .clickable { onTypeSelected(type) }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = type,
                        color = if (isSelected) Color.White else Color.Black
                    )
                }
            }
        }
    }
}
