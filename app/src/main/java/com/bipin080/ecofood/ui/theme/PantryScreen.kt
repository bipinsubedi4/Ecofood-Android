package com.bipin080.ecofood.ui.pantry

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bipin080.ecofood.data.PantryItem
import com.bipin080.ecofood.data.PantryStatus
import com.bipin080.ecofood.data.status
import com.bipin080.ecofood.viewmodel.PantryViewModel
import java.text.SimpleDateFormat
import java.util.Locale

enum class PantryFilter {
    ALL,
    EXPIRING_SOON,
    EXPIRED
}

@Composable
fun PantryScreen(
    pantryViewModel: PantryViewModel = viewModel(),
    onGenerateRecipeClick: (List<PantryItem>) -> Unit = {}
) {
    val items by pantryViewModel.pantryItems.collectAsState()
    val (currentFilter, setFilter) = remember { mutableStateOf(PantryFilter.ALL) }

    val filteredItems = when (currentFilter) {
        PantryFilter.ALL -> items
        PantryFilter.EXPIRING_SOON -> items.filter { it.status() == PantryStatus.EXPIRING_SOON }
        PantryFilter.EXPIRED -> items.filter { it.status() == PantryStatus.EXPIRED }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Text(
            text = "Your Pantry",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Track expiry dates and keep your food fresh. Use items before they go to waste.",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        PantryFilterRow(
            currentFilter = currentFilter,
            onFilterChange = setFilter
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (filteredItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when (currentFilter) {
                        PantryFilter.ALL -> "Your pantry is empty. Add some items to get started."
                        PantryFilter.EXPIRING_SOON -> "No items are expiring soon."
                        PantryFilter.EXPIRED -> "No expired items. Great job!"
                    },
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredItems, key = { it.id }) { item ->
                    PantryItemCard(item)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                // For now, just send all *non-expired* items to the recipe generator.
                val usableItems = items.filter { it.status() != PantryStatus.EXPIRED }
                onGenerateRecipeClick(usableItems)
            }
        ) {
            Text("Use pantry ingredients for recipes")
        }
    }
}

@Composable
private fun PantryFilterRow(
    currentFilter: PantryFilter,
    onFilterChange: (PantryFilter) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        AssistChip(
            onClick = { onFilterChange(PantryFilter.ALL) },
            label = { Text("All") },
            colors = AssistChipDefaults.assistChipColors(
                containerColor = if (currentFilter == PantryFilter.ALL)
                    MaterialTheme.colorScheme.primaryContainer
                else
                    MaterialTheme.colorScheme.surface
            )
        )

        AssistChip(
            onClick = { onFilterChange(PantryFilter.EXPIRING_SOON) },
            label = { Text("Expiring soon") },
            colors = AssistChipDefaults.assistChipColors(
                containerColor = if (currentFilter == PantryFilter.EXPIRING_SOON)
                    MaterialTheme.colorScheme.tertiaryContainer
                else
                    MaterialTheme.colorScheme.surface
            )
        )

        AssistChip(
            onClick = { onFilterChange(PantryFilter.EXPIRED) },
            label = { Text("Expired") },
            colors = AssistChipDefaults.assistChipColors(
                containerColor = if (currentFilter == PantryFilter.EXPIRED)
                    MaterialTheme.colorScheme.errorContainer
                else
                    MaterialTheme.colorScheme.surface
            )
        )
    }
}

@Composable
private fun PantryItemCard(item: PantryItem) {
    val dateFormat = remember {
        SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    }
    val status = item.status()

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (status) {
                PantryStatus.FRESH -> MaterialTheme.colorScheme.surface
                PantryStatus.EXPIRING_SOON -> MaterialTheme.colorScheme.tertiaryContainer
                PantryStatus.EXPIRED -> MaterialTheme.colorScheme.errorContainer
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "${item.quantity} ${item.unit}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Purchased: ${dateFormat.format(item.purchaseDate)}",
                style = MaterialTheme.typography.bodySmall
            )

            Text(
                text = "Expires: ${dateFormat.format(item.expiryDate)}",
                style = MaterialTheme.typography.bodySmall
            )

            Spacer(modifier = Modifier.height(4.dp))

            Divider()

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = when (status) {
                    PantryStatus.FRESH -> "Fresh"
                    PantryStatus.EXPIRING_SOON -> "Use soon to avoid waste"
                    PantryStatus.EXPIRED -> "Expired – consider discarding"
                },
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}
