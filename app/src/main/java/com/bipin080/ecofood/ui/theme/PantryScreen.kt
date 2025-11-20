package com.bipin080.ecofood.ui.theme

import android.app.DatePickerDialog
import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bipin080.ecofood.data.PantryItem
import com.bipin080.ecofood.data.status
import com.bipin080.ecofood.viewmodel.PantryViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantryScreen(
    pantryViewModel: PantryViewModel = viewModel(),
) {
    val pantryItems by pantryViewModel.pantryItems.collectAsState()

    var filter by remember { mutableStateOf("All") }
    var showAddDialog by remember { mutableStateOf(false) }
    var deleteCandidate by remember { mutableStateOf<PantryItem?>(null) }

    val formatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    // Filtering
    val today = Date()
    val filteredItems = when (filter) {
        "Expiring soon" -> pantryItems.filter { it.status() == com.bipin080.ecofood.data.PantryStatus.EXPIRING_SOON }
        "Expired" -> pantryItems.filter { it.status() == com.bipin080.ecofood.data.PantryStatus.EXPIRED }
        else -> pantryItems
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Text("+")
            }
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text("Your Pantry", style = MaterialTheme.typography.headlineSmall)

            Text(
                "Track expiry dates and keep your food fresh. Use items before they go to waste.",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(Modifier.height(16.dp))

            // Filter Buttons
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                FilterChipButton("All", filter) { filter = it }
                FilterChipButton("Expiring soon", filter) { filter = it }
                FilterChipButton("Expired", filter) { filter = it }
            }

            Spacer(Modifier.height(16.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(filteredItems) { item ->
                    PantryItemCard(
                        item = item,
                        onDelete = { deleteCandidate = item }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddPantryItemDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { pantryViewModel.addItem(it); showAddDialog = false }
        )
    }

    deleteCandidate?.let { item ->
        ConfirmDeleteDialog(
            itemName = item.name,
            onConfirm = {
                pantryViewModel.deleteItem(item)
                deleteCandidate = null
            },
            onCancel = { deleteCandidate = null }
        )
    }
}

@Composable
fun FilterChipButton(title: String, selected: String, onSelected: (String) -> Unit) {
    AssistChip(
        label = { Text(title) },
        onClick = { onSelected(title) },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = if (title == selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
            else MaterialTheme.colorScheme.surfaceVariant
        )
    )
}

@Composable
fun PantryItemCard(
    item: PantryItem,
    onDelete: () -> Unit
) {
    val formatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    val bgColor = when (item.status()) {
        com.bipin080.ecofood.data.PantryStatus.EXPIRED -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
        com.bipin080.ecofood.data.PantryStatus.EXPIRING_SOON -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f)
        else -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(item.name, fontWeight = FontWeight.Bold)
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                }
            }

            Text("Purchased: ${formatter.format(item.purchaseDate)}")
            Text("Expires: ${formatter.format(item.expiryDate)}")

            Spacer(Modifier.height(6.dp))

            val statusText = when (item.status()) {
                com.bipin080.ecofood.data.PantryStatus.EXPIRED -> "Expired – consider discarding"
                com.bipin080.ecofood.data.PantryStatus.EXPIRING_SOON -> "Expiring soon"
                else -> "Fresh"
            }
            Text(statusText, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun ConfirmDeleteDialog(
    itemName: String,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { },
        title = { Text("Remove Item") },
        text = { Text("Are you sure you want to remove \"$itemName\" from the pantry?") },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text("Yes") }
        },
        dismissButton = {
            TextButton(onClick = onCancel) { Text("No") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPantryItemDialog(
    onDismiss: () -> Unit,
    onAdd: (PantryItem) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("g") }
    var purchaseDate by remember { mutableStateOf(Date()) }
    var expiryDate by remember { mutableStateOf(Date()) }

    val context = LocalContext.current

    fun pickDate(onPicked: (Date) -> Unit) {
        val cal = Calendar.getInstance()
        DatePickerDialog(
            context,
            { _, y, m, d ->
                val c = Calendar.getInstance()
                c.set(y, m, d)
                onPicked(c.time)
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Pantry Item") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") })

                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text("Quantity") }
                )

                OutlinedTextField(
                    value = unit,
                    onValueChange = { unit = it },
                    label = { Text("Unit (g, ml, litre, etc.)") }
                )

                Button(onClick = { pickDate { purchaseDate = it } }) {
                    Text("Select Purchase Date")
                }

                Button(onClick = { pickDate { expiryDate = it } }) {
                    Text("Select Expiry Date")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onAdd(
                    PantryItem(
                        name = name,
                        quantity = quantity.toIntOrNull() ?: 1,
                        unit = unit,
                        purchaseDate = purchaseDate,
                        expiryDate = expiryDate
                    )
                )
            }) { Text("Add") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
