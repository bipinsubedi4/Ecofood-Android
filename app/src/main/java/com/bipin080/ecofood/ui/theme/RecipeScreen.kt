package com.bipin080.ecofood.ui.theme

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bipin080.ecofood.BuildConfig
import com.bipin080.ecofood.data.GeneratedRecipe
import com.bipin080.ecofood.data.RecipeIngredient
import com.bipin080.ecofood.viewmodel.RecipeViewModel
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeScreen(
    recipeViewModel: RecipeViewModel = viewModel(),
    onNavigateUp: () -> Unit
) {
    // Read currently selected recipe
    val recipeState = recipeViewModel.currentRecipe.collectAsState()
    val recipe = recipeState.value

    if (recipe == null) {
        // Fallback if user somehow lands here without a recipe
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Recipe Details") },
                    navigationIcon = {
                        IconButton(onClick = onNavigateUp) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("No recipe available. Please generate a recipe first.")
            }
        }
        return
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var isFullRecipeLoading by remember { mutableStateOf(false) }
    var fullRecipeText by remember { mutableStateOf<String?>(null) }
    var fullRecipeError by remember { mutableStateOf<String?>(null) }
    var showFullRecipe by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Recipe Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // ─── Title + description ──────────────────────────────────────
            Text(
                text = recipe.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = recipe.description,
                style = MaterialTheme.typography.bodyMedium
            )

            // ─── Summary chips (time, servings, calories, waste) ─────────
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                SummaryChip("Time", recipe.cookingTime)
                SummaryChip("Servings", recipe.servings)
                SummaryChip("Calories", recipe.calories)
                SummaryChip("Waste ↓", recipe.wasteReduction)
            }

            // ─── See full recipe button ──────────────────────────────────
            Button(
                onClick = {
                    if (fullRecipeText == null && !isFullRecipeLoading) {
                        // First time: call Gemini
                        isFullRecipeLoading = true
                        fullRecipeError = null
                        scope.launch {
                            try {
                                val text = askGeminiForFullRecipe(recipe)
                                fullRecipeText = text.ifBlank { "Sorry, the AI returned an empty recipe." }
                                showFullRecipe = true
                            } catch (e: Exception) {
                                fullRecipeError = "Sorry, we couldn't load the full recipe. Please try again."
                            } finally {
                                isFullRecipeLoading = false
                            }
                        }
                    } else {
                        // Already loaded: just toggle visibility
                        showFullRecipe = !showFullRecipe
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                if (isFullRecipeLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Loading full recipe…")
                } else {
                    Text(if (showFullRecipe) "Hide full recipe" else "See full recipe")
                }
            }

            if (fullRecipeError != null) {
                Text(
                    text = fullRecipeError ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            AnimatedVisibility(
                visible = showFullRecipe && fullRecipeText != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                if (fullRecipeText != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Text(
                            text = fullRecipeText!!,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }

            // ─── Ingredients section ──────────────────────────────────────
            Text(
                text = "Ingredients",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                recipe.ingredients.forEach { ingredient ->
                    IngredientRow(ingredient)
                }
            }

            // ─── Shopping list section (not in pantry) ───────────────────
            val shoppingList = recipe.ingredients.filter { !it.inPantry }
            if (shoppingList.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Shopping list (not in your pantry)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    shoppingList.forEach { ingredient ->
                        ShoppingRow(ingredient)
                    }
                }
            }

            // ─── Save to My Recipes button ───────────────────────────────
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedButton(
                onClick = {
                    recipeViewModel.saveRecipe(recipe)
                    scope.launch {
                        snackbarHostState.showSnackbar("Recipe saved to My Recipes")
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Save to My Recipes")
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SummaryChip(label: String, value: String) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
            .padding(vertical = 10.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}


@Composable
private fun IngredientRow(ingredient: RecipeIngredient) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = ingredient.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = ingredient.quantity,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AssistChip(
                    onClick = {},
                    label = { Text(if (ingredient.inPantry) "In your pantry" else "Buy") }
                )
                ingredient.tags.forEach { tag ->
                    AssistChip(onClick = {}, label = { Text(tag) })
                }
            }
        }
    }
}

@Composable
private fun ShoppingRow(ingredient: RecipeIngredient) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.background
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = ingredient.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = ingredient.quantity,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Text(
                text = "Buy",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

/**
 * Calls Gemini to generate a detailed full recipe text based on the structured recipe data.
 */
private suspend fun askGeminiForFullRecipe(recipe: GeneratedRecipe): String {
    val apiKey = BuildConfig.GEMINI_API_KEY
    if (apiKey.isBlank()) return "Missing Gemini API key."

    val model = GenerativeModel(
        modelName = "gemini-2.0-flash",
        apiKey = apiKey
    )

    val ingredientsText = recipe.ingredients.joinToString("\n") {
        "- ${it.name} (${it.quantity})"
    }

    val prompt = """
        You are an expert sustainable chef. Using the following structured recipe,
        write a clear, step-by-step recipe that a beginner can follow.

        Title: ${recipe.title}
        Description: ${recipe.description}
        Servings: ${recipe.servings}
        Cooking time: ${recipe.cookingTime}
        Waste reduction: ${recipe.wasteReduction}
        Calories per serving: ${recipe.calories}

        Ingredients:
        $ingredientsText

        Requirements:
        - Start with a short introductory paragraph.
        - Then list ingredients again in bullet points.
        - Then give numbered method steps.
        - Add 1–2 eco-friendly tips at the end (for reducing food waste / saving energy).
    """.trimIndent()

    val response = model.generateContent(prompt)
    return response.text ?: "Could not generate recipe text."
}
