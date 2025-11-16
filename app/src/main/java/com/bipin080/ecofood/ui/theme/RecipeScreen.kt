package com.bipin080.ecofood.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bipin080.ecofood.data.GeneratedRecipe
import com.bipin080.ecofood.data.RecipeIngredient
import com.bipin080.ecofood.viewmodel.RecipeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeScreen(
    recipeViewModel: RecipeViewModel,
    onNavigateUp: () -> Unit
) {
    val recipeData by recipeViewModel.recipe.collectAsState()
    var showShoppingList by remember { mutableStateOf(false) }

    if (recipeData == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(recipeData!!.title, maxLines = 1) },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(recipeData!!.description, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.secondary)
                Spacer(modifier = Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    InfoCard(icon = Icons.Default.Timer, label = "Cooking Time", value = recipeData!!.cookingTime, modifier = Modifier.weight(1f))
                    InfoCard(icon = Icons.Default.People, label = "Servings", value = recipeData!!.servings, modifier = Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    InfoCard(icon = Icons.Default.Eco, label = "Waste Reduction", value = recipeData!!.wasteReduction, modifier = Modifier.weight(1f))
                    InfoCard(icon = Icons.Default.ShowChart, label = "Calories per serving", value = recipeData!!.calories, modifier = Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Ingredients", style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.weight(1f))
                    Button(onClick = { showShoppingList = true }, enabled = recipeData!!.ingredients.any { !it.inPantry }) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Shopping List")
                    }
                }
            }
            items(recipeData!!.ingredients) { ingredient ->
                IngredientItem(ingredient)
            }
        }

        if (showShoppingList) {
            val shoppingList = recipeData!!.ingredients.filter { !it.inPantry }
            AlertDialog(
                onDismissRequest = { showShoppingList = false },
                title = { Text("Shopping List") },
                text = {
                    Column {
                        shoppingList.forEach {
                            Text("- ${it.quantity} ${it.name}")
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showShoppingList = false }) {
                        Text("OK")
                    }
                }
            )
        }
    }
}

@Composable
fun InfoCard(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier, elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))
            Text(label, style = MaterialTheme.typography.bodySmall)
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun IngredientItem(ingredient: RecipeIngredient) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(if (ingredient.inPantry) SuccessGreen else MaterialTheme.colorScheme.error)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text("${ingredient.quantity} ${ingredient.name}", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
        }
        Row(modifier = Modifier.padding(start = 24.dp, top = 4.dp)) {
            ingredient.tags.forEach {
                Chip(label = it)
                Spacer(modifier = Modifier.width(8.dp))
            }
        }
        Divider(modifier = Modifier.padding(top = 16.dp))
    }
}

@Composable
fun Chip(label: String) {
    Surface(
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
        shape = CircleShape
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
        )
    }
}
