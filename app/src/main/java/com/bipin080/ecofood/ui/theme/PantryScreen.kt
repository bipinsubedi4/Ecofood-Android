package com.bipin080.ecofood.ui.theme

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.bipin080.ecofood.data.PantryItem
import com.bipin080.ecofood.data.fakePantry
import androidx.compose.material3.ExperimentalMaterial3Api


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantryScreen() {
    val items by remember { mutableStateOf(fakePantry()) }

    Column(Modifier.fillMaxSize()) {
        TopAppBar(title = { Text("P A N T R Y (use-soon)") })
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(items) { item -> PantryCard(item) }
        }
    }
}

@Composable
private fun PantryCard(item: PantryItem) {
    Card {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    item.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text("${item.quantity} • ${item.location}", style = MaterialTheme.typography.bodyMedium)
                AssistChip(onClick = {}, label = { Text("Expires in ${item.daysToExpire}d") })
            }
            Spacer(Modifier.width(12.dp))
            FilledTonalButton(onClick = { /* TODO: open Leftover Magic */ }) { Text("Cook") }
        }
    }
}
