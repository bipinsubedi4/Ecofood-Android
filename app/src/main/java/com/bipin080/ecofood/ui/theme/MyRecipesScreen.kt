package com.bipin080.ecofood.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.bipin080.ecofood.data.GeneratedRecipe

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyRecipesScreen(
    recipes: List<GeneratedRecipe>,
    onOpenRecipe: (GeneratedRecipe) -> Unit,
    onDeleteRecipe: (GeneratedRecipe) -> Unit
) {
    // Which recipe we’re asking confirmation for
    var recipePendingDelete by remember { mutableStateOf<GeneratedRecipe?>(null) }

    // ─── Delete confirmation dialog ──────────────────────────────────────
    recipePendingDelete?.let { recipe ->
        AlertDialog(
            onDismissRequest = { recipePendingDelete = null },
            title = { Text("Remove recipe?") },
            text = {
                Text(
                    "Are you sure you want to remove \"${recipe.title}\" from your saved recipes?"
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteRecipe(recipe)
                        recipePendingDelete = null
                    }
                ) {
                    Text("Remove")
                }
            },
            dismissButton = {
                TextButton(onClick = { recipePendingDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
    // ─────────────────────────────────────────────────────────────────────

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Recipes") }
            )
        }
    ) { paddingValues ->
        if (recipes.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "You haven't saved any recipes yet.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(recipes) { recipe ->
                    SavedRecipeCard(
                        recipe = recipe,
                        onOpenClick = { onOpenRecipe(recipe) },
                        onDeleteClick = { recipePendingDelete = recipe }
                    )
                }
            }
        }
    }
}

@Composable
private fun SavedRecipeCard(
    recipe: GeneratedRecipe,
    onOpenClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                ) {
                    Text(
                        text = recipe.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = recipe.description,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Open",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable { onOpenClick() }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    IconButton(onClick = onDeleteClick) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete recipe",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                InfoChip(text = recipe.cookingTime)
                InfoChip(text = "${recipe.servings} servings")
                InfoChip(text = "${recipe.calories} kcal")
            }
        }
    }
}

@Composable
private fun InfoChip(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall
        )
    }
}
