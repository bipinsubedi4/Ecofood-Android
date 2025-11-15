package com.bipin080.ecofood.ui.theme

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bipin080.ecofood.R
import com.bipin080.ecofood.data.GeneratedRecipe
import com.bipin080.ecofood.data.PantryItem
import com.bipin080.ecofood.data.RecipeIngredient
import com.bipin080.ecofood.viewmodel.PantryViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CookScreen(
    pantryViewModel: PantryViewModel = viewModel(),
    onGenerateRecipe: (GeneratedRecipe) -> Unit
) {
    var ingredientInput by remember { mutableStateOf("") }
    val ingredients = remember { mutableStateListOf<String>() }
    var servings by remember { mutableIntStateOf(4) }
    var isLoading by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val pantryItems by pantryViewModel.pantryItems.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.app_logo),
                            contentDescription = "EcoFood Logo",
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("AI Recipe Generator")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Ingredients Card
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("What ingredients do you have?")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = ingredientInput,
                        onValueChange = { ingredientInput = it },
                        label = { Text("e.g., chicken, tomatoes, rice") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = {
                        if (ingredientInput.isNotBlank()) {
                            ingredients.add(ingredientInput.trim())
                            ingredientInput = ""
                        }
                    }) {
                        Text("Add")
                    }
                    if (ingredients.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Your ingredients:", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ingredients.forEach { ingredient ->
                                InputChip(
                                    selected = false,
                                    onClick = { ingredients.remove(ingredient) },
                                    label = { Text(ingredient) },
                                    trailingIcon = {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = "Remove ingredient"
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Servings Card
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("How many people are you cooking for?")
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        IconButton(onClick = { if (servings > 1) servings-- }) {
                            Icon(Icons.Default.Remove, contentDescription = "Decrease servings")
                        }
                        Text(
                            text = servings.toString(),
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        IconButton(onClick = { servings++ }) {
                            Icon(Icons.Default.Add, contentDescription = "Increase servings")
                        }
                    }
                }
            }

            if (isLoading) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()){
                    CircularProgressIndicator()
                    Text("Gemini is thinking...", style = MaterialTheme.typography.bodySmall)
                }
            } else {
                Button(
                    onClick = {
                        if (ingredients.isNotEmpty()) {
                            isLoading = true
                            coroutineScope.launch {
                                val recipe = generateRecipe(ingredients, pantryItems, servings.toString())
                                onGenerateRecipe(recipe)
                                isLoading = false
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Generate Eco-Friendly Recipe")
                }
            }
        }
    }
}

suspend fun generateRecipe(
    userIngredients: List<String>,
    pantry: List<PantryItem>,
    servings: String
): GeneratedRecipe {
    // Simulate a network call and complex logic
    delay(2000)

    // A hardcoded full recipe for demonstration
    val fullRecipeIngredients = listOf(
        RecipeIngredient("Chicken Breast", "500 g", listOf("protein", "gluten-free"), inPantry = false),
        RecipeIngredient("Jasmine Rice", "2 cups", listOf("grain", "vegan", "gluten-free"), inPantry = false),
        RecipeIngredient("Broccoli", "400 g", listOf("vegetable", "vegan", "gluten-free"), inPantry = false),
        RecipeIngredient("Garlic", "3 cloves", listOf("seasoning", "vegan", "gluten-free"), inPantry = false),
        RecipeIngredient("Soy Sauce", "2 tbsp", listOf("seasoning", "vegan"), inPantry = false)
    )

    // Check pantry for ingredients
    val pantryNames = pantry.map { it.name.lowercase() }
    val finalIngredients = fullRecipeIngredients.map { ingredient ->
        val ingredientInPantry = pantryNames.any { pantryName ->
            pantryName.contains(ingredient.name.lowercase().substring(0, 4))
        } || userIngredients.any { it.lowercase().contains(ingredient.name.lowercase().substring(0, 4)) }
        ingredient.copy(inPantry = ingredientInPantry)
    }

    return GeneratedRecipe(
        title = "Eco-Friendly Chicken & Vegetable Rice Bowl",
        description = "A sustainable one-pot meal that minimizes waste and maximizes nutrition.",
        cookingTime = "25 minutes",
        servings = servings,
        wasteReduction = "85%",
        calories = "420",
        ingredients = finalIngredients
    )
}
