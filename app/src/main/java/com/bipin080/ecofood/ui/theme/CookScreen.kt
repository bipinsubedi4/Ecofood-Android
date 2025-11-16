package com.bipin080.ecofood.ui.theme

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bipin080.ecofood.BuildConfig
import com.bipin080.ecofood.R
import com.bipin080.ecofood.data.GeneratedRecipe
import com.bipin080.ecofood.data.PantryItem
import com.bipin080.ecofood.data.RecipeIngredient
import com.bipin080.ecofood.viewmodel.PantryViewModel
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CookScreen(
    onGenerateRecipe: (GeneratedRecipe) -> Unit
) {
    // Get PantryViewModel INSIDE the composable
    val pantryViewModel: PantryViewModel = viewModel()
    val pantryItems by pantryViewModel.pantryItems.collectAsState()

    var ingredientInput by rememberSaveable { mutableStateOf("") }
    val ingredients = remember { mutableStateListOf<String>() }
    var servings by rememberSaveable { mutableIntStateOf(4) }
    var isLoading by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf<String?>(null) }

    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.app_logo),
                            contentDescription = "EcoFood Logo",
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "AI Recipe Generator",
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
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

            // -------- Ingredients card --------
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        "What ingredients do you have?",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = ingredientInput,
                        onValueChange = { ingredientInput = it },
                        label = { Text("e.g., mutton, tomatoes, rice") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            if (ingredientInput.isNotBlank()) {
                                ingredients.add(ingredientInput.trim())
                                ingredientInput = ""
                            }
                        }
                    ) {
                        Text("Add Ingredient")
                    }

                    if (ingredients.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
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

            // -------- Servings card --------
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "How many people are you cooking for?",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        IconButton(onClick = { if (servings > 1) servings-- }) {
                            Icon(
                                Icons.Default.Remove,
                                contentDescription = "Decrease servings"
                            )
                        }
                        Text(
                            text = servings.toString(),
                            style = MaterialTheme.typography.headlineMedium,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                        IconButton(onClick = { servings++ }) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Increase servings"
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            errorText?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            // -------- Generate button / loader --------
            if (isLoading) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "The AI is thinking...",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                Button(
                    onClick = {
                        if (ingredients.isNotEmpty()) {
                            isLoading = true
                            errorText = null
                            coroutineScope.launch {
                                val recipe = try {
                                    generateRecipeFromGemini(
                                        userIngredients = ingredients,
                                        pantry = pantryItems,
                                        servings = servings.toString()
                                    )
                                } catch (e: Exception) {
                                    Log.e("EcoFood", "Error generating recipe", e)
                                    errorText =
                                        "Sorry, something went wrong. Showing a sample recipe."
                                    generateFallbackRecipe(
                                        userIngredients = ingredients,
                                        pantry = pantryItems,
                                        servings = servings
                                    )
                                }
                                isLoading = false
                                onGenerateRecipe(recipe)
                            }
                        } else {
                            errorText = "Please add at least one ingredient."
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        "Generate Recipe",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/* ---------------- Gemini DTOs ---------------- */

@Serializable
data class GeminiIngredientDto(
    val name: String,
    val quantity: String = "",
    val tags: List<String> = emptyList()
)

@Serializable
data class GeminiRecipeDto(
    val title: String,
    val description: String,
    val cookingTime: String,
    val servings: String,
    val wasteReduction: String,
    val calories: String,
    val ingredients: List<GeminiIngredientDto>
)

/* ------------- Gemini-powered generator ------------- */

private suspend fun generateRecipeFromGemini(
    userIngredients: List<String>,
    pantry: List<PantryItem>,
    servings: String
): GeneratedRecipe {
    if (BuildConfig.GEMINI_API_KEY.isBlank()) {
        Log.w("EcoFood", "GEMINI_API_KEY is blank – using fallback recipe.")
        return generateFallbackRecipe(userIngredients, pantry, servings.toInt())
    }

    val model = GenerativeModel(
        modelName = "gemini-2.0-flash",
        apiKey = BuildConfig.GEMINI_API_KEY
    )

    val ingredientsList = userIngredients.joinToString(", ")
    val pantryList = pantry.joinToString(", ") { it.name }

    val prompt = """
        You are EcoFood's sustainable cooking assistant.

        The user has the following key ingredients: $ingredientsList
        Their pantry items include: $pantryList
        They want to cook for $servings people.

        Please design ONE recipe that:
        - Uses as many of the user's ingredients as reasonable.
        - Minimises food waste.
        - Is realistic for a home cook.

        You MUST respond with ONLY valid JSON, no markdown, no code fences,
        matching this schema exactly:

        {
          "title": "String",
          "description": "Short 1-2 sentence description",
          "cookingTime": "e.g. '25 minutes'",
          "servings": "String version of servings",
          "wasteReduction": "e.g. 'High' or '85%'",
          "calories": "Estimated calories per serving as string",
          "ingredients": [
            {
              "name": "Ingredient name",
              "quantity": "e.g. '500 g' or '2 cups'",
              "tags": ["protein", "gluten-free"]
            }
          ]
        }

        Do not include any keys other than these.
        Do not wrap the JSON in code fences.
    """.trimIndent()

    val response = model.generateContent(
        content { text(prompt) }
    )

    val raw = response.text?.trim()
        ?: throw IllegalStateException("Gemini returned empty text")

    val jsonString = raw
        .removePrefix("```json")
        .removePrefix("```")
        .removeSuffix("```")
        .trim()

    val json = Json { ignoreUnknownKeys = true }

    val dto = try {
        json.decodeFromString(GeminiRecipeDto.serializer(), jsonString)
    } catch (e: SerializationException) {
        Log.e("EcoFood", "Failed to parse Gemini JSON, using fallback.", e)
        return generateFallbackRecipe(userIngredients, pantry, servings.toInt())
    }

    val pantryNames = pantry.map { it.name.lowercase() }
    val userNames = userIngredients.map { it.lowercase() }

    val finalIngredients = dto.ingredients.map { ing ->
        val key = ing.name.lowercase().take(4)
        val inPantry = pantryNames.any { it.contains(key) } ||
                userNames.any { it.contains(key) }

        RecipeIngredient(
            name = ing.name,
            quantity = ing.quantity,
            tags = ing.tags,
            inPantry = inPantry
        )
    }

    return GeneratedRecipe(
        title = dto.title,
        description = dto.description,
        cookingTime = dto.cookingTime,
        servings = dto.servings,
        wasteReduction = dto.wasteReduction,
        calories = dto.calories,
        ingredients = finalIngredients
    )
}

/* ------------- Fallback recipe (offline) ------------- */

private fun generateFallbackRecipe(
    userIngredients: List<String>,
    pantry: List<PantryItem>,
    servings: Int
): GeneratedRecipe {
    val defaultIngredients = listOf(
        RecipeIngredient("Chicken Breast", "500 g", listOf("protein", "gluten-free"), inPantry = false),
        RecipeIngredient("Jasmine Rice", "2 cups", listOf("grain", "vegan", "gluten-free"), inPantry = false),
        RecipeIngredient("Broccoli", "400 g", listOf("vegetable", "vegan", "gluten-free"), inPantry = false),
        RecipeIngredient("Garlic", "3 cloves", listOf("seasoning", "vegan", "gluten-free"), inPantry = false),
        RecipeIngredient("Soy Sauce", "2 tbsp", listOf("seasoning", "vegan"), inPantry = false)
    )

    val pantryNames = pantry.map { it.name.lowercase() }
    val userNames = userIngredients.map { it.lowercase() }

    val final = defaultIngredients.map { ing ->
        val key = ing.name.lowercase().take(4)
        val inPantry = pantryNames.any { it.contains(key) } ||
                userNames.any { it.contains(key) }
        ing.copy(inPantry = inPantry)
    }

    return GeneratedRecipe(
        title = "Eco-Friendly Chicken & Vegetable Rice Bowl",
        description = "A sustainable one-pot meal that minimizes waste and maximizes nutrition.",
        cookingTime = "25 minutes",
        servings = servings.toString(),
        wasteReduction = "85%",
        calories = "420",
        ingredients = final
    )
}
