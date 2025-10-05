package com.bipin080.ecofood.ui.theme

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bipin080.ecofood.data.RecipeCardModel
import com.bipin080.ecofood.data.fakeRecipes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CookScreen() {
    val recipes by remember { mutableStateOf(fakeRecipes()) }

    Column(Modifier.fillMaxSize()) {
        TopAppBar(title = { Text("Leftover Magic") })
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 180.dp),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(recipes) { r -> RecipeCard(r) }
        }
    }
}

@Composable
private fun RecipeCard(r: RecipeCardModel) {
    Card(onClick = { /* TODO: open details */ }) {
        Column(Modifier.padding(16.dp)) {
            Text(r.title, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text("Uses: ${r.uses.joinToString()}", style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(8.dp))
            AssistChip(onClick = { /* TODO: swap */ }, label = { Text("Swap options") })
        }
    }
}
