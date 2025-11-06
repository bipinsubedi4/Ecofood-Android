package com.bipin080.ecofood.ui.theme

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CookScreen(onGenerateRecipe: (String) -> Unit) {
    var ingredientInput by remember { mutableStateOf("") }
    val ingredients = remember { mutableStateListOf<String>() }
    var servings by remember { mutableIntStateOf(4) }
    var isLoading by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Leftover Magic") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
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
                                val recipe = generateRecipe(ingredients, servings.toString())
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

suspend fun generateRecipe(ingredients: List<String>, servings: String): String {
    // Simulate a network call to a Gemini-like API
    delay(2000)

    val mainIngredient = ingredients.firstOrNull() ?: "something delicious"
    val otherIngredients = ingredients.drop(1)

    val recipeTitle = "Gemini's Fusion ${mainIngredient.replaceFirstChar { it.uppercase() }} Medley"

    val ingredientsYouHave = ingredients.joinToString("\n") { "- $it" }

    val optionalIngredients = """
        - 2 cloves of garlic
        - 1 tsp of soy sauce
        - A pinch of red pepper flakes
    """.trimIndent()

    val instructions = """
        1. Finely chop the garlic. Prepare the ${otherIngredients.joinToString(" and ")} as needed.
        2. Heat a pan over medium heat with a tablespoon of olive oil. Add the $mainIngredient and cook until golden.
        3. Add the remaining ingredients, including the optional ones if you have them. Stir for 5-7 minutes.
        4. Serve hot for $servings people and enjoy your sustainable meal!
    """.trimIndent()

    val ecoTip = "Don't forget to compost your vegetable scraps! They can enrich the soil for future plants."

    return """
        Recipe Title: $recipeTitle
        Prep Time: 15 minutes
        Cook Time: 20 minutes

        Ingredients You Have:
$ingredientsYouHave

        Optional Ingredients:
$optionalIngredients

        Instructions:
$instructions

        Eco Tip: $ecoTip
    """.trimIndent()
}
