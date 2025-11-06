package com.bipin080.ecofood.ui.theme

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

data class ParsedRecipe(
    val title: String,
    val prepTime: String,
    val cookTime: String,
    val ingredients: List<String>,
    val optionalIngredients: List<String>,
    val instructions: List<String>,
    val ecoTip: String
)

@Composable
fun rememberParsedRecipe(recipeString: String): ParsedRecipe {
    return remember(recipeString) {
        val decodedString = URLDecoder.decode(recipeString, StandardCharsets.UTF_8.name())
        val lines = decodedString.lines().map { it.trim() }.filter { it.isNotBlank() }

        val title = lines.find { it.startsWith("Recipe Title:") }?.substringAfter(":")?.trim() ?: "Generated Recipe"
        val prepTime = lines.find { it.startsWith("Prep Time:") }?.substringAfter(":")?.trim() ?: "N/A"
        val cookTime = lines.find { it.startsWith("Cook Time:") }?.substringAfter(":")?.trim() ?: "N/A"

        val ingredientsStartIndex = lines.indexOf("Ingredients You Have:")
        val optionalIngredientsStartIndex = lines.indexOf("Optional Ingredients:")
        val instructionsStartIndex = lines.indexOf("Instructions:")
        val ecoTipStartIndex = lines.indexOf("Eco Tip:")

        val ingredients = if (ingredientsStartIndex != -1) {
            val endIndex = listOfNotNull(optionalIngredientsStartIndex.takeIf { it > 0 }, instructionsStartIndex.takeIf { it > 0 }, ecoTipStartIndex.takeIf { it > 0 }, lines.size).minOrNull()!!
            lines.subList(ingredientsStartIndex + 1, endIndex).map { it.removePrefix("-").trim() }
        } else { emptyList() }

        val optionalIngredients = if (optionalIngredientsStartIndex != -1) {
            val endIndex = listOfNotNull(instructionsStartIndex.takeIf { it > 0 }, ecoTipStartIndex.takeIf { it > 0 }, lines.size).minOrNull()!!
            lines.subList(optionalIngredientsStartIndex + 1, endIndex).map { it.removePrefix("-").trim() }
        } else { emptyList() }

        val instructions = if (instructionsStartIndex != -1) {
            val endIndex = listOfNotNull(ecoTipStartIndex.takeIf { it > 0 }, lines.size).minOrNull()!!
            lines.subList(instructionsStartIndex + 1, endIndex)
        } else { emptyList() }

        val ecoTip = if (ecoTipStartIndex != -1) {
            lines.subList(ecoTipStartIndex + 1, lines.size).joinToString("\n")
        } else { "No tip available." }

        ParsedRecipe(title, prepTime, cookTime, ingredients, optionalIngredients, instructions, ecoTip)
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeScreen(recipe: String, onNavigateUp: () -> Unit) {
    val parsedRecipe = rememberParsedRecipe(recipe)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(parsedRecipe.title, maxLines = 1) },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Timer, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Prep: ${parsedRecipe.prepTime}")
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Timer, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Cook: ${parsedRecipe.cookTime}")
                }
            }
            Divider()

            RecipeSection("Ingredients") {
                if (parsedRecipe.ingredients.isNotEmpty()){
                    Text("What You Have:", style = MaterialTheme.typography.titleMedium)
                    parsedRecipe.ingredients.forEach { Text("- $it") }
                }
                if (parsedRecipe.optionalIngredients.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Optional:", style = MaterialTheme.typography.titleMedium)
                    parsedRecipe.optionalIngredients.forEach { Text("- $it") }
                }
            }

            RecipeSection("Instructions") {
                 parsedRecipe.instructions.forEach { Text(it) }
            }

            RecipeSection("Eco Tip") {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Eco, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(parsedRecipe.ecoTip)
                }
            }
        }
    }
}

@Composable
fun RecipeSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column {
        Text(text = title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                content()
            }
        }
    }
}
